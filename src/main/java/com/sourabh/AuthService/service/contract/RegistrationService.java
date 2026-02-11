package com.sourabh.AuthService.service.contract;

import com.sourabh.AuthService.dto.request.SignupRequest;

public interface RegistrationService {
    void signup(SignupRequest request);
}
