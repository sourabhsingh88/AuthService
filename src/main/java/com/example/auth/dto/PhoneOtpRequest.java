package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneOtpRequest {

    @NotBlank
    private String phone;


}
