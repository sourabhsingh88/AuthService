package com.sourabh.AuthService.service.impl;

import com.sourabh.AuthService.dto.request.LoginPhoneRequest;
import com.sourabh.AuthService.dto.request.LoginRequest;
import com.sourabh.AuthService.dto.request.VerifyPhoneOtpRequest;
import com.sourabh.AuthService.dto.response.LoginResponse;
import com.sourabh.AuthService.entity.User;
import com.sourabh.AuthService.enums.OtpType;
import com.sourabh.AuthService.exceptions.BadRequestException;
import com.sourabh.AuthService.exceptions.NotFoundException;
import com.sourabh.AuthService.repository.UserRepository;
import com.sourabh.AuthService.service.impl.OtpServiceImpl;
import com.sourabh.AuthService.service.contract.AuthenticationService;
import com.sourabh.AuthService.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpServiceImpl otpService;

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        if (!user.isEmailVerified() || !user.isPhoneNumberVerified()) {
            throw new BadRequestException("Account not verified");
        }

        return LoginResponse.builder()
                .token(jwtUtil.generateToken(user.getEmail()))
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .city(user.getCity())
                .build();
    }

    @Override
    public void sendPhoneLoginOtp(LoginPhoneRequest request) {

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isEmailVerified() || !user.isPhoneNumberVerified()) {
            throw new BadRequestException("Account not verified");
        }

        otpService.generatePhoneOtp(user.getPhoneNumber(), OtpType.PHONE_LOGIN);
    }

    @Override
    public String verifyPhoneLoginOtp(VerifyPhoneOtpRequest request) {

        otpService.verifyPhoneOtp(
                request.getPhoneNumber(),
                request.getOtp(),
                OtpType.PHONE_LOGIN
        );

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return jwtUtil.generateToken(user.getEmail());
    }
}
