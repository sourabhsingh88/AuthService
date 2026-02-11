package com.sourabh.AuthService.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {

    private String firstName;
    private String lastName;
    private String city;

    // sensitive
    private String email;
    private String phoneNumber;
}
