package com.sourabh.AuthService.service.contract;

public interface EmailService {
    void sendOtp(String to, String otp);
}
