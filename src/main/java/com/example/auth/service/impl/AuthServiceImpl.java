package com.example.auth.service.impl;

import com.example.auth.entity.User;
import com.example.auth.exceptions.BadRequestException;
import com.example.auth.exceptions.NotFoundException;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.AuthService;
import com.example.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(readOnly = true)
    public String login(String email, String password) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        return jwtUtil.generateToken(email);
    }

    @Override
    @Transactional
    public void signup(String email, String phone, String password) {
        if (userRepo.findByEmail(email).isPresent()) {
            throw new BadRequestException("Email already exists");
        }
        if (userRepo.findByPhone(phone).isPresent()) {
            throw new BadRequestException("Phone already exists");
        }

        validatePassword(password);

        User user = new User();
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));

        userRepo.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {

        validatePassword(newPassword);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public String refreshToken(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return jwtUtil.generateToken(user.getEmail());
    }

    // ==================== PASSWORD VALIDATION ====================

    private void validatePassword(String password) {

        if (password == null || password.isEmpty()) {
            throw new BadRequestException("Password cannot be empty");
        }

        if (password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long");
        }

        if (password.length() > 128) {
            throw new BadRequestException("Password must not exceed 128 characters");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new BadRequestException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new BadRequestException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new BadRequestException("Password must contain at least one digit");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new BadRequestException(
                    "Password must contain at least one special character"
            );
        }
    }
}

