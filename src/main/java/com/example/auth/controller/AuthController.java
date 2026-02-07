package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.service.AuthService;
import com.example.auth.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Validated
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "APIs for user authentication, registration, and password management"
)
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;


    @Operation(
            summary = "Register new user",
            description = "Create a new user account with email, phone number, and password"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or user already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email or phone number already registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        log.info("Signup request received for email: {}", request.getEmail());

        authService.signup(
                request.getEmail(),
                request.getPhone(),
                request.getPassword()
        );

        log.info("User registered successfully: {}", request.getEmail());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MessageResponse("User registered successfully"));
    }


    @Operation(
            summary = "Login user",
            description = "Authenticate user with email and password, returns JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login request received for email: {}", request.getEmail());

        String token = authService.login(
                request.getEmail(),
                request.getPassword()
        );

        log.info("User logged in successfully: {}", request.getEmail());
        return ResponseEntity.ok(new AuthResponse(token));
    }


    @Operation(
            summary = "Request phone OTP",
            description = "Send OTP to the specified phone number for verification"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP sent successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Phone number not registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many OTP requests",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/login-by-phone-num")
    public ResponseEntity<MessageResponse> requestPhoneOtp(
            @Valid @RequestBody PhoneOtpRequest request
    ) {
        log.info("Phone OTP request received for: {}", maskPhone(request.getPhone()));

        otpService.sendPhoneOtp(request.getPhone());

        log.info("Phone OTP sent successfully to: {}", maskPhone(request.getPhone()));
        return ResponseEntity.ok(
                new MessageResponse("OTP sent to phone number")
        );
    }

    @Operation(
            summary = "Verify phone OTP",
            description = "Verify OTP sent to phone number and authenticate user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP verified successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired OTP",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Phone number not registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/verify-phone-num")
    public ResponseEntity<AuthResponse> verifyPhoneOtp(
            @Valid @RequestBody VerifyPhoneOtpRequest request
    ) {
        log.info("Phone OTP verification request for: {}", maskPhone(request.getPhone()));

        String token = otpService.verifyPhoneOtp(
                request.getPhone(),
                request.getOtp()
        );

        log.info("Phone OTP verified successfully for: {}", maskPhone(request.getPhone()));
        return ResponseEntity.ok(new AuthResponse(token));
    }


    @Operation(
            summary = "Request email OTP for password reset",
            description = "Send OTP to email address for password reset verification"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP sent successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Email not registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many OTP requests",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/reset-password-by-email")
    public ResponseEntity<MessageResponse> requestEmailOtp(
            @Valid @RequestBody EmailOtpRequest request
    ) {
        log.info("Email OTP request received for: {}", maskEmail(request.getEmail()));

        otpService.sendEmailOtp(request.getEmail());

        log.info("Email OTP sent successfully to: {}", maskEmail(request.getEmail()));
        return ResponseEntity.ok(
                new MessageResponse("OTP sent to email address")
        );
    }


    @Operation(
            summary = "Verify email OTP and reset password",
            description = "Verify OTP sent to email and set new password"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired OTP",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Email not registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordOtpRequest request
    ) {
        log.info("Password reset request received for: {}", maskEmail(request.getEmail()));

        otpService.resetPassword(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword()
        );

        log.info("Password reset successfully for: {}", maskEmail(request.getEmail()));
        return ResponseEntity.ok(
                new MessageResponse("Password reset successful")
        );
    }


    @Operation(
            summary = "Change password",
            description = "Change password for authenticated user (requires valid JWT token)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid old password or weak new password",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        log.info("Password change request received for user: {}", email);

        authService.changePassword(
                email,
                request.getOldPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(new MessageResponse("Password updated successfully"));
    }

    /**
     * Refresh JWT token for authenticated user.
     *
     * @param userDetails the authenticated user details
     * @return ResponseEntity containing new JWT token
     */
    @Operation(
            summary = "Refresh JWT token",
            description = "Get a new JWT token for authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/token/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Token refresh request for user: {}", userDetails.getUsername());

        String newToken = authService.refreshToken(userDetails.getUsername());

        log.info("Token refreshed successfully for user: {}", userDetails.getUsername());
        return ResponseEntity.ok(new AuthResponse(newToken));
    }


    @Operation(
            summary = "Logout user",
            description = "Logout authenticated user (client should discard the token)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logged out successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
//    @PostMapping("/logout")
//    public ResponseEntity<MessageResponse> logout(
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        log.info("Logout request for user: {}", userDetails.getUsername());
//
//
//        return ResponseEntity.ok(
//                new MessageResponse("Logged out successfully")
//        );
//    }


    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "****" + phone.substring(phone.length() - 4);
    }


    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****@****.***";
        }
        String[] parts = email.split("@");
        return "****@" + parts[1];
    }
}