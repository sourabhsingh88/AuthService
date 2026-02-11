package com.sourabh.AuthService.repository;

import com.sourabh.AuthService.entity.Otp;
import com.sourabh.AuthService.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByPhoneAndTypeAndVerifiedFalseAndExpiryAfterOrderByCreatedAtDesc(
            String phone,
            OtpType type,
            LocalDateTime now
    );

    Optional<Otp> findTopByEmailAndTypeAndVerifiedFalseAndExpiryAfterOrderByCreatedAtDesc(
            String email,
            OtpType type,
            LocalDateTime now
    );
}
