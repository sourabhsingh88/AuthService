package com.example.auth.service;

import com.example.auth.entity.User;
import com.example.auth.exceptions.BadRequestException;
import com.example.auth.exceptions.NotFoundException;
import com.example.auth.repository.UserRepository;
import com.example.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public void signup(String email, String phone, String password) {

        if (userRepo.findByEmail(email).isPresent()) throw new BadRequestException("Email already exists");

        if (userRepo.findByPhone(phone).isPresent()) throw new BadRequestException("Phone already exists");

        User user = new User();
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));

        userRepo.save(user);
    }

    public Map<String, String> login(String email, String password) {

        User user = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new BadRequestException("Invalid credentials");

//        return Map.of("token", jwtUtil.generateToken(email, 60));
        String token = jwtUtil.generateToken(email);

        return Map.of("token", token);

    }

    public void changePassword(String oldPassword, String newPassword) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw new BadRequestException("Old password is incorrect");

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }
}
