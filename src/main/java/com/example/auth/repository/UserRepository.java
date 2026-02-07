package com.example.auth.repository;

import com.example.auth.entity.Otp;
import com.example.auth.entity.User;
import com.example.auth.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);
}