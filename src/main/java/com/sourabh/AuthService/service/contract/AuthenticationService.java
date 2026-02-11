package com.sourabh.AuthService.service.contract;

import com.sourabh.AuthService.dto.request.LoginPhoneRequest;
import com.sourabh.AuthService.dto.request.LoginRequest;
import com.sourabh.AuthService.dto.request.VerifyPhoneOtpRequest;
import com.sourabh.AuthService.dto.response.LoginResponse;

public interface AuthenticationService {

    LoginResponse login(LoginRequest request);

    void sendPhoneLoginOtp(LoginPhoneRequest request);

    String verifyPhoneLoginOtp(VerifyPhoneOtpRequest request);
}
