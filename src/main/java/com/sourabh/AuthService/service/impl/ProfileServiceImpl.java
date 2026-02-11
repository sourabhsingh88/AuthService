package com.sourabh.AuthService.service.impl;

import com.sourabh.AuthService.dto.request.UpdateUserRequest;
import com.sourabh.AuthService.entity.User;
import com.sourabh.AuthService.enums.OtpType;
import com.sourabh.AuthService.exceptions.BadRequestException;
import com.sourabh.AuthService.repository.UserRepository;
import com.sourabh.AuthService.service.contract.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class ProfileServiceImpl implements ProfileService {
//
//    private final UserRepository userRepository;
//
//    @Override
//    public void updateProfile(User user, UpdateUserRequest request) {
//
//        if (request.getFirstName() != null) {
//            user.setFirstName(request.getFirstName());
//        }
//
//        if (request.getLastName() != null) {
//            user.setLastName(request.getLastName());
//        }
//
//        if (request.getCity() != null) {
//            user.setCity(request.getCity());
//        }
//
//        userRepository.save(user);
//    }
//}

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final OtpServiceImpl otpService;

    @Override
    public void updateProfile(User user, UpdateUserRequest request) {

        // -------- NON-SENSITIVE --------
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }

        // -------- EMAIL CHANGE --------
        if (request.getEmail() != null &&
                !request.getEmail().equals(user.getEmail())) {

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already in use");
            }

            user.setEmail(request.getEmail());
            user.setEmailVerified(false);

            otpService.generateEmailOtp(
                    request.getEmail(),
                    OtpType.EMAIL_VERIFICATION
            );
        }

        // -------- PHONE CHANGE --------
        if (request.getPhoneNumber() != null &&
                !request.getPhoneNumber().equals(user.getPhoneNumber())) {

            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BadRequestException("Phone number already in use");
            }

            user.setPhoneNumber(request.getPhoneNumber());
            user.setPhoneNumberVerified(false);

            otpService.generatePhoneOtp(
                    request.getPhoneNumber(),
                    OtpType.PHONE_VERIFICATION
            );
        }

        userRepository.save(user);
    }
}
