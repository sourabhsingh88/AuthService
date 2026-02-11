package com.sourabh.AuthService.service.contract;

import com.sourabh.AuthService.enums.OtpType;

public interface OtpService {

    void generateEmailOtp(String email, OtpType type);

    void generatePhoneOtp(String phone, OtpType type);

    void verifyEmailOtp(String email, String otp, OtpType type);

    void verifyPhoneOtp(String phone, String otp, OtpType type);
}
