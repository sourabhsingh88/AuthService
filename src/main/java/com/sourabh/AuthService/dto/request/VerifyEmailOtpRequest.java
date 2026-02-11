package com.sourabh.AuthService.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VerifyEmailOtpRequest {

    @Email(message = "Invalid email format")
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String otp;

    @NotBlank
    @Size(min = 8)
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}
