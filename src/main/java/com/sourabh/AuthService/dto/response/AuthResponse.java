package com.sourabh.AuthService.dto.response;

public class AuthResponse {

    public String token ;

    public AuthResponse(String token) {
        this.token = token;
    }
    public String getToken() {
        return token ;
    }
}
