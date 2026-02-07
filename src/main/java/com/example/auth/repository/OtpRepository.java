package com.example.auth.repository;

import com.example.auth.entity.Otp;
import com.example.auth.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByPhoneAndTypeOrderByIdDesc(String phone, OtpType type);

    Optional<Otp> findTopByEmailAndTypeOrderByIdDesc(String email, OtpType type);
}