package com.sourabh.AuthService.service;

import com.sourabh.AuthService.dto.request.*;
import com.sourabh.AuthService.dto.response.LoginResponse;
import com.sourabh.AuthService.entity.User;
import com.sourabh.AuthService.enums.OtpType;
import com.sourabh.AuthService.exceptions.BadRequestException;
import com.sourabh.AuthService.exceptions.NotFoundException;
import com.sourabh.AuthService.repository.UserRepository;
import com.sourabh.AuthService.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    /* ===================== SIGNUP ===================== */

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

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCity(request.getCity());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);
        user.setPhoneNumberVerified(false);

        userRepository.save(user);

        otpService.generateEmailOtp(user.getEmail(), OtpType.EMAIL_VERIFICATION);
        otpService.generatePhoneOtp(user.getPhoneNumber(), OtpType.PHONE_VERIFICATION);
    }

    /* ===================== VERIFY ACCOUNT ===================== */

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

    /* ===================== LOGIN (EMAIL) ===================== */

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        if (!user.isEmailVerified() || !user.isPhoneNumberVerified()) {
            throw new BadRequestException("Account not verified");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return LoginResponse.builder()
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .city(user.getCity())
                .build();
    }

    /* ===================== UPDATE PROFILE ===================== */

    public void updateProfile(User user, UpdateUserRequest request) {

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }

        userRepository.save(user);
    }

    /* ===================== PHONE LOGIN OTP ===================== */

    public void sendPhoneLoginOtp(LoginPhoneRequest request) {

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isEmailVerified() || !user.isPhoneNumberVerified()) {
            throw new BadRequestException("Account not verified");
        }

        otpService.generatePhoneOtp(user.getPhoneNumber(), OtpType.PHONE_LOGIN);
    }

    public String verifyPhoneLoginOtp(VerifyPhoneOtpRequest request) {

        otpService.verifyPhoneOtp(
                request.getPhoneNumber(),
                request.getOtp(),
                OtpType.PHONE_LOGIN
        );

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isEmailVerified() || !user.isPhoneNumberVerified()) {
            throw new BadRequestException("Account not verified");
        }

        return jwtUtil.generateToken(user.getEmail());
    }

    /* ===================== FORGOT PASSWORD ===================== */

    public void forgotPasswordOtp(ResetPasswordOtpRequest request) {

        userRepository.findByEmail(request.getEmail())
                .ifPresent(user ->
                        otpService.generateEmailOtp(
                                user.getEmail(),
                                OtpType.FORGOT_PASSWORD
                        )
                );
    }

    /* ===================== RESET PASSWORD ===================== */

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

    /* ===================== CHANGE PASSWORD ===================== */

    public void changePassword(User user, ChangePasswordRequest request) {

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
