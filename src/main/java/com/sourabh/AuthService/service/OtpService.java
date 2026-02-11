package com.sourabh.AuthService.service;

import com.sourabh.AuthService.entity.Otp;
import com.sourabh.AuthService.enums.OtpType;
import com.sourabh.AuthService.exceptions.BadRequestException;
import com.sourabh.AuthService.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int MAX_ATTEMPTS = 5;

    public void generateEmailOtp(String email, OtpType type) {
        createOtp(email, null, type);
    }

    public void generatePhoneOtp(String phone, OtpType type) {
        createOtp(null, phone, type);
    }
    private void createOtp(String email, String phone, OtpType type) {

        LocalDateTime now = LocalDateTime.now();

        Otp existingOtp = null;

        if (email != null) {
            existingOtp = otpRepository
                    .findTopByEmailAndTypeAndVerifiedFalseAndExpiryAfterOrderByCreatedAtDesc(
                            email, type, now
                    )
                    .orElse(null);
        }

        if (phone != null) {
            existingOtp = otpRepository
                    .findTopByPhoneAndTypeAndVerifiedFalseAndExpiryAfterOrderByCreatedAtDesc(
                            phone, type, now
                    )
                    .orElse(null);
        }

        // 🔒 RESEND THROTTLING (cooldown = 1 minute)
        if (existingOtp != null && existingOtp.getExpiry().isAfter(now.plusMinutes(-1))) {
            throw new BadRequestException("Please wait before requesting another OTP");
        }

        // ---------- generate new OTP ----------
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        Otp entity = new Otp();
        entity.setEmail(email);
        entity.setPhone(phone);
        entity.setType(type);
        entity.setOtpHash(passwordEncoder.encode(otp));
        entity.setExpiry(now.plusMinutes(5));

        otpRepository.save(entity);

        if (email != null) {
            emailService.sendOtp(email, otp);
        }

        if (phone != null) {
            // DEMO ONLY
            System.out.println("[DEV] Phone OTP for " + phone + " = " + otp);
        }
    }

//    private void createOtp(String email, String phone, OtpType type) {
//        String otp = String.valueOf(100000 + new Random().nextInt(900000));
//
//        Otp entity = new Otp();
//        entity.setEmail(email);
//        entity.setPhone(phone);
//        entity.setType(type);
//        entity.setOtpHash(passwordEncoder.encode(otp));
//        entity.setExpiry(LocalDateTime.now().plusMinutes(5));
//
//        otpRepository.save(entity);
//
//        if (email != null) {
//            emailService.sendOtp(email, otp);
//        }
//
//        if (phone != null) {
//            // DEMO ONLY
//            System.out.println("[DEV] Phone OTP for " + phone + " = " + otp);
//        }
//    }

    public void verifyEmailOtp(String email, String otp, OtpType type) {
        Otp entity = otpRepository
                .findTopByEmailAndTypeAndVerifiedFalseAndExpiryAfterOrderByCreatedAtDesc(
                        email, type, LocalDateTime.now()
                )
                .orElseThrow(() -> new BadRequestException("OTP expired or invalid"));

        validateOtp(entity, otp);
    }

    public void verifyPhoneOtp(String phone, String otp, OtpType type) {
        Otp entity = otpRepository
                .findTopByPhoneAndTypeAndVerifiedFalseAndExpiryAfterOrderByCreatedAtDesc(
                        phone, type, LocalDateTime.now()
                )
                .orElseThrow(() -> new BadRequestException("OTP expired or invalid"));

        validateOtp(entity, otp);
    }

    private void validateOtp(Otp otp, String rawOtp) {
        if (otp.getAttempts() >= MAX_ATTEMPTS) {
            throw new BadRequestException("OTP attempts exceeded");
        }

        if (!passwordEncoder.matches(rawOtp, otp.getOtpHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            throw new BadRequestException("Invalid OTP");
        }

        otp.setVerified(true);
        otpRepository.save(otp);
    }
}
