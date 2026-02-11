package com.sourabh.AuthService.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordOtpRequest {

    @Email(message = "Invalid email format")
    @NotBlank
    private String email;
}
