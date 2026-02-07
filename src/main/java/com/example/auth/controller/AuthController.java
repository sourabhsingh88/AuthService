package com.example.auth.controller;

import com.example.auth.service.AuthService;
import com.example.auth.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }


    @PostMapping("/signup")
    public ResponseEntity<String> signup(
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password
    ) {
        authService.signup(email, phone, password);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Signup successful");
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestParam String email,
            @RequestParam String password
    ) {
        return ResponseEntity.ok(authService.login(email, password));
    }


    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {
        authService.changePassword(oldPassword, newPassword);
        return ResponseEntity.ok("Password updated");
    }


    @PostMapping("/otp/phone/request")
    public ResponseEntity<String> requestPhoneOtp(@RequestParam String phone) {
        otpService.sendPhoneOtp(phone);
        return ResponseEntity.ok("OTP sent to phone");
    }

    @PostMapping("/otp/phone/verify")
    public ResponseEntity<Map<String, String>> verifyPhoneOtp(
            @RequestParam String phone,
            @RequestParam String otp
    ) {
        return ResponseEntity.ok(otpService.verifyPhoneOtp(phone, otp));
    }


    @PostMapping("/otp/email/request")
    public ResponseEntity<String> requestEmailOtp(@RequestParam String email) {
        otpService.sendEmailOtp(email);
        return ResponseEntity.ok("OTP sent to email");
    }

    @PostMapping("/otp/email/verify")
    public ResponseEntity<String> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword
    ) {
        otpService.resetPassword(email, otp, newPassword);
        return ResponseEntity.ok("Password reset successful");
    }
}

