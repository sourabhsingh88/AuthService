package com.sourabh.AuthService.service.contract;

import com.sourabh.AuthService.dto.request.VerifyAccountRequest;

public interface VerificationService {
    void verifyAccount(VerifyAccountRequest request);
}
