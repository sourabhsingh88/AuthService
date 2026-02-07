package com.example.auth.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "otp.sms.enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class DevSmsService implements SmsService {

    @Override
    public void sendOtp(String phone, String otp) {
        System.out.println("[DEV] OTP for +91" + phone + " = " + otp);
    }
}
