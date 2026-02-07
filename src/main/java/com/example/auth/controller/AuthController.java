package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.service.AuthService;
import com.example.auth.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Validated
@Tag(
        name = "Auth APIs",
        description = "Authentication & OTP flow APIs (ordered)"
)
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    @Operation(summary = "1. Signup user")
    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        authService.signup(
                request.getEmail(),
                request.getPhone(),
                request.getPassword()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MessageResponse("Signup successful"));
    }
    @Operation(summary = "2. Login user")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        String token = authService
                .login(request.getEmail(), request.getPassword())
                .get("token");

        return ResponseEntity.ok(new AuthResponse(token));
    }
    @Operation(
            summary = "7. Change password (authenticated)",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(
                request.getOldPassword(),
                request.getNewPassword()
        );
        return ResponseEntity.ok(new MessageResponse("Password updated"));
    }

    @Operation(summary = "3. Request phone OTP")
    @PostMapping("/otp/phone/request")
    public ResponseEntity<MessageResponse> requestPhoneOtp(
            @Valid @RequestBody PhoneOtpRequest request
    ) {
        otpService.sendPhoneOtp(request.getPhone());
        return ResponseEntity.ok(new MessageResponse("OTP sent to phone"));
    }

    @Operation(summary = "4. Verify phone OTP")
    @PostMapping("/otp/phone/verify")
    public ResponseEntity<AuthResponse> verifyPhoneOtp(
            @Valid @RequestBody VerifyPhoneOtpRequest request
    ) {
        String token = otpService
                .verifyPhoneOtp(request.getPhone(), request.getOtp())
                .get("token");

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/otp/email/request")
    @Operation(summary = "5. Request email OTP")
    public ResponseEntity<MessageResponse> requestEmailOtp(
            @Valid @RequestBody EmailOtpRequest request
    ) {
        otpService.sendEmailOtp(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("OTP sent to email"));
    }

    @PostMapping("/otp/email/verify")
    @Operation(summary = "6. Verify email OTP (reset password)")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordOtpRequest request
    ) {
        otpService.resetPassword(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword()
        );
        return ResponseEntity.ok(
                new MessageResponse("Password reset successful")
        );
    }
}


//@RestController
//@RequestMapping("/auth")
//public class AuthController {
//
//    private final AuthService authService;
//    private final OtpService otpService;
//
//    public AuthController(AuthService authService, OtpService otpService) {
//        this.authService = authService;
//        this.otpService = otpService;
//    }
//
//
//    @PostMapping("/signup")
//    public ResponseEntity<String> signup(
//            @RequestParam String email,
//            @RequestParam String phone,
//            @RequestParam String password
//    ) {
//        authService.signup(email, phone, password);
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body("Signup successful");
//    }
//
//
//    @PostMapping("/login")
//    public ResponseEntity<Map<String, String>> login(
//            @RequestParam String email,
//            @RequestParam String password
//    ) {
//        return ResponseEntity.ok(authService.login(email, password));
//    }
//
//
//    @PostMapping("/change-password")
//    public ResponseEntity<String> changePassword(
//            @RequestParam String oldPassword,
//            @RequestParam String newPassword
//    ) {
//        authService.changePassword(oldPassword, newPassword);
//        return ResponseEntity.ok("Password updated");
//    }
//
//
//    @PostMapping("/otp/phone/request")
//    public ResponseEntity<String> requestPhoneOtp(@RequestParam String phone) {
//        otpService.sendPhoneOtp(phone);
//        return ResponseEntity.ok("OTP sent to phone");
//    }
//
//    @PostMapping("/otp/phone/verify")
//    public ResponseEntity<Map<String, String>> verifyPhoneOtp(
//            @RequestParam String phone,
//            @RequestParam String otp
//    ) {
//        return ResponseEntity.ok(otpService.verifyPhoneOtp(phone, otp));
//    }
//
//
//    @PostMapping("/otp/email/request")
//    public ResponseEntity<String> requestEmailOtp(@RequestParam String email) {
//        otpService.sendEmailOtp(email);
//        return ResponseEntity.ok("OTP sent to email");
//    }
//
//    @PostMapping("/otp/email/verify")
//    public ResponseEntity<String> resetPassword(
//            @RequestParam String email,
//            @RequestParam String otp,
//            @RequestParam String newPassword
//    ) {
//        otpService.resetPassword(email, otp, newPassword);
//        return ResponseEntity.ok("Password reset successful");
//    }
//}
//
