package com.sourabh.AuthService.service.contract;

import com.sourabh.AuthService.dto.request.ChangePasswordRequest;
import com.sourabh.AuthService.dto.request.ResetPasswordOtpRequest;
import com.sourabh.AuthService.dto.request.VerifyEmailOtpRequest;
import com.sourabh.AuthService.entity.User;

public interface PasswordService {

    void forgotPasswordOtp(ResetPasswordOtpRequest request);

    void resetPassword(VerifyEmailOtpRequest request);

    void changePassword(User user, ChangePasswordRequest request);
}
