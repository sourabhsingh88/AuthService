package com.sourabh.AuthService.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String oldPassword ;

    @NotBlank
    @Size(min = 8)
    private String newPassword ;

    private String confirmPassward;
}
