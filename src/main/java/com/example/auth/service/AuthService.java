package com.example.auth.service;

public interface AuthService {

    String login(String email, String password);

    void signup(String email, String phone, String password);

    void changePassword(String email, String oldPassword, String newPassword);

    String refreshToken(String email);
}
