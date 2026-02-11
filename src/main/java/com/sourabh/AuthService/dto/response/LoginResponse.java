package com.sourabh.AuthService.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String token;

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String city;
}
