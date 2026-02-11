package com.sourabh.AuthService.controller;

import com.sourabh.AuthService.dto.request.*;
import com.sourabh.AuthService.entity.User;
import com.sourabh.AuthService.exceptions.UnauthorizedException;
import com.sourabh.AuthService.service.AuthService;
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

    private final AuthService authService;

    /* ===================== SIGNUP ===================== */
    // 201 CREATED
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyAccount(
            @Valid @RequestBody VerifyAccountRequest request
    ) {
        authService.verifyAccount(request);
        return ResponseEntity.ok(
                Map.of("message", "Account verified successfully")
        );
    }


    /* ===================== LOGIN (EMAIL) ===================== */
    // 200 OK
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
//        return ResponseEntity.ok(
//                Map.of("token", authService.login(request))
//        );
//    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }


    @PatchMapping("/update")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateUserRequest request
    ) {
        if (user == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        authService.updateProfile(user, request);

        return ResponseEntity.ok(
                Map.of("message", "Profile updated successfully")
        );
    }


    /* ===================== LOGIN (PHONE OTP) ===================== */
    // 202 ACCEPTED
    @PostMapping("/login/phone")
    public ResponseEntity<?> sendPhoneOtp(@Valid @RequestBody LoginPhoneRequest request) {
        authService.sendPhoneLoginOtp(request);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(Map.of("message", "OTP sent"));
    }

    /* ===================== VERIFY PHONE OTP ===================== */
    // 200 OK
    @PostMapping("/login/phone/verify")
    public ResponseEntity<?> verifyPhoneOtp(@Valid @RequestBody VerifyPhoneOtpRequest request) {

        return ResponseEntity.ok(
                Map.of("token", authService.verifyPhoneLoginOtp(request))
        );
    }

    /* ===================== FORGOT PASSWORD ===================== */
    // 202 ACCEPTED
    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ResetPasswordOtpRequest request) {
        authService.forgotPasswordOtp(request);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(Map.of("message", "OTP sent"));
    }

    /* ===================== RESET PASSWORD ===================== */
    // 200 OK
    // Errors handled by GlobalExceptionHandler:
    // 400 → invalid OTP / password mismatch
    // 404 → user not found
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody VerifyEmailOtpRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(
                Map.of("message", "Password reset successful")
        );
    }

    /* ===================== CHANGE PASSWORD ===================== */
    // 200 OK
    // 401 UNAUTHORIZED if JWT missing/invalid
    @PostMapping("/password/change")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        if (user == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        authService.changePassword(user, request);
        return ResponseEntity.ok(
                Map.of("message", "Password changed successfully")
        );
    }
}
