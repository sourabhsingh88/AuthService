package com.sourabh.AuthService.service.impl;

import com.sourabh.AuthService.dto.request.VerifyAccountRequest;
import com.sourabh.AuthService.entity.User;
import com.sourabh.AuthService.enums.OtpType;
import com.sourabh.AuthService.exceptions.BadRequestException;
import com.sourabh.AuthService.exceptions.NotFoundException;
import com.sourabh.AuthService.repository.UserRepository;
import com.sourabh.AuthService.service.impl.OtpServiceImpl;
import com.sourabh.AuthService.service.contract.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final UserRepository userRepository;
    private final OtpServiceImpl otpService;

    @Override
    @Transactional
    public void verifyAccount(VerifyAccountRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getPhoneNumber().equals(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number mismatch");
        }

        otpService.verifyEmailOtp(
                request.getEmail(),
                request.getEmailOtp(),
                OtpType.EMAIL_VERIFICATION
        );

        otpService.verifyPhoneOtp(
                request.getPhoneNumber(),
                request.getPhoneOtp(),
                OtpType.PHONE_VERIFICATION
        );

        user.setEmailVerified(true);
        user.setPhoneNumberVerified(true);

        userRepository.save(user);
    }
}
