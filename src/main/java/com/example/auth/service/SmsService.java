package com.example.auth.service;




public interface SmsService {
    void sendOtp(String phone, String otp);
}
