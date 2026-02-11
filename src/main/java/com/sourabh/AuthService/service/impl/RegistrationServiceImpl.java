package com.sourabh.AuthService.service.impl;

import com.sourabh.AuthService.dto.request.SignupRequest;
import com.sourabh.AuthService.entity.User;
import com.sourabh.AuthService.enums.OtpType;
import com.sourabh.AuthService.exceptions.BadRequestException;
import com.sourabh.AuthService.repository.UserRepository;
import com.sourabh.AuthService.service.impl.OtpServiceImpl;
import com.sourabh.AuthService.service.contract.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpServiceImpl otpService;

    @Override
    @Transactional
    public void signup(SignupRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .city(request.getCity())
                .password(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .phoneNumberVerified(false)
                .build();

        userRepository.save(user);

        otpService.generateEmailOtp(user.getEmail(), OtpType.EMAIL_VERIFICATION);
        otpService.generatePhoneOtp(user.getPhoneNumber(), OtpType.PHONE_VERIFICATION);
    }
}
