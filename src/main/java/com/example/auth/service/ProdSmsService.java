package com.example.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@ConditionalOnProperty(
        name = "otp.sms.enabled",
        havingValue = "true"
)
public class ProdSmsService implements SmsService {

    @Value("${msg91.authkey}")
    private String authKey;

    @Value("${msg91.templateId}")
    private String templateId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendOtp(String phone, String otp) {

        String url =
                "https://api.msg91.com/api/v5/otp" +
                        "?template_id=" + templateId +
                        "&mobile=91" + phone +
                        "&authkey=" + authKey +
                        "&otp=" + otp;

        try {
            ResponseEntity<String> response =
                    restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("SMS provider failed");
            }

        } catch (Exception ex) {
            // do NOT leak OTP
            throw new RuntimeException("Unable to send OTP at this time");
        }
    }
}
