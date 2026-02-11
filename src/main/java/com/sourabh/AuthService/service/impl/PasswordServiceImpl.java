package com.sourabh.AuthService.service.impl;

import com.sourabh.AuthService.dto.request.ChangePasswordRequest;
import com.sourabh.AuthService.dto.request.ResetPasswordOtpRequest;
import com.sourabh.AuthService.dto.request.VerifyEmailOtpRequest;
import com.sourabh.AuthService.entity.User;
import com.sourabh.AuthService.enums.OtpType;
import com.sourabh.AuthService.exceptions.BadRequestException;
import com.sourabh.AuthService.exceptions.NotFoundException;
import com.sourabh.AuthService.repository.UserRepository;
import com.sourabh.AuthService.service.impl.OtpServiceImpl;
import com.sourabh.AuthService.service.contract.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpServiceImpl otpService;

    @Override
    public void forgotPasswordOtp(ResetPasswordOtpRequest request) {

        userRepository.findByEmail(request.getEmail())
                .ifPresent(user ->
                        otpService.generateEmailOtp(
                                user.getEmail(),
                                OtpType.FORGOT_PASSWORD
                        )
                );
    }

    @Override
    public void resetPassword(VerifyEmailOtpRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        otpService.verifyEmailOtp(
                request.getEmail(),
                request.getOtp(),
                OtpType.FORGOT_PASSWORD
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void changePassword(User user, ChangePasswordRequest request) {

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
