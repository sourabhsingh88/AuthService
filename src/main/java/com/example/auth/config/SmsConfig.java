package com.example.auth.config;

import com.example.auth.service.DevSmsService;
import com.example.auth.service.SmsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SmsConfig {

    @Bean
    @Primary
    public SmsService fallbackSmsService() {
        return new DevSmsService();
    }
}
