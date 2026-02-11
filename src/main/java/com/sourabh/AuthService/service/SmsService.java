package com.sourabh.AuthService.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;


@ConditionalOnProperty(
        name = "otp.sms.enabled",
        havingValue = "false",
        matchIfMissing = true
)
@Service
public class SmsService {
    public void sendOtp(String phone, String otp) {
        System.out.println("[DEV] OTP for +91" + phone + " = " + otp);
    }
}


