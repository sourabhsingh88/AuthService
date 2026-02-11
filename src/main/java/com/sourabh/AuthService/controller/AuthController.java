package com.sourabh.AuthService.controller;

import com.sourabh.AuthService.dto.request.*;
import com.sourabh.AuthService.entity.User;
import com.sourabh.AuthService.exceptions.UnauthorizedException;
import com.sourabh.AuthService.service.contract.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegistrationService registrationService;
    private final VerificationService verificationService;
    private final AuthenticationService authenticationService;
    private final ProfileService profileService;
    private final PasswordService passwordService;

    /* ===================== SIGNUP ===================== */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {

        registrationService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully"));
    }

    /* ===================== VERIFY ACCOUNT ===================== */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyAccount(
            @Valid @RequestBody VerifyAccountRequest request
    ) {
        verificationService.verifyAccount(request);

        return ResponseEntity.ok(
                Map.of("message", "Account verified successfully")
        );
    }

    /* ===================== LOGIN (EMAIL) ===================== */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authenticationService.login(request)
        );
    }

    /* ===================== UPDATE PROFILE ===================== */
    @PatchMapping("/update")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateUserRequest request
    ) {
        if (user == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        profileService.updateProfile(user, request);

        return ResponseEntity.ok(
                Map.of("message", "Profile updated successfully")
        );
    }

    /* ===================== LOGIN (PHONE OTP) ===================== */
    @PostMapping("/login/phone")
    public ResponseEntity<?> sendPhoneOtp(
            @Valid @RequestBody LoginPhoneRequest request
    ) {
        authenticationService.sendPhoneLoginOtp(request);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(Map.of("message", "OTP sent"));
    }

    /* ===================== VERIFY PHONE OTP ===================== */
    @PostMapping("/login/phone/verify")
    public ResponseEntity<?> verifyPhoneOtp(
            @Valid @RequestBody VerifyPhoneOtpRequest request
    ) {
        return ResponseEntity.ok(
                Map.of(
                        "token",
                        authenticationService.verifyPhoneLoginOtp(request)
                )
        );
    }

    /* ===================== FORGOT PASSWORD ===================== */
    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ResetPasswordOtpRequest request
    ) {
        passwordService.forgotPasswordOtp(request);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(Map.of("message", "OTP sent"));
    }

    /* ===================== RESET PASSWORD ===================== */
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody VerifyEmailOtpRequest request
    ) {
        passwordService.resetPassword(request);

        return ResponseEntity.ok(
                Map.of("message", "Password reset successful")
        );
    }

    /* ===================== CHANGE PASSWORD ===================== */
    @PostMapping("/password/change")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        if (user == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        passwordService.changePassword(user, request);

        return ResponseEntity.ok(
                Map.of("message", "Password changed successfully")
        );
    }
}
