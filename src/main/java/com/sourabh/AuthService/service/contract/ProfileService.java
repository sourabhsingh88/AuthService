package com.sourabh.AuthService.service.contract;

import com.sourabh.AuthService.dto.request.UpdateUserRequest;
import com.sourabh.AuthService.entity.User;

public interface ProfileService {
    void updateProfile(User user, UpdateUserRequest request);
}
