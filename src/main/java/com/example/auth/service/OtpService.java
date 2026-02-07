package com.example.auth.service;

import com.example.auth.entity.Otp;
import com.example.auth.entity.User;
import com.example.auth.enums.OtpType;
import com.example.auth.exceptions.BadRequestException;
import com.example.auth.exceptions.NotFoundException;
import com.example.auth.repository.OtpRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OtpRepository otpRepo;

    @Autowired
    private SmsService smsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


    public void sendPhoneOtp(String phone) {

        userRepo.findByPhone(phone).orElseThrow(() -> new NotFoundException("User not found"));

        String rawOtp = generateOtp();

        Otp o = createOtp(rawOtp, OtpType.PHONE_LOGIN);
        o.setPhone(phone);

        otpRepo.save(o);
        smsService.sendOtp(phone, rawOtp);
    }

    public Map<String, String> verifyPhoneOtp(String phone, String otp) {

        Otp o = otpRepo.findTopByPhoneAndTypeOrderByIdDesc(phone, OtpType.PHONE_LOGIN).orElseThrow(() -> new NotFoundException("OTP not found"));

        validateOtp(o, otp);

        User user = userRepo.findByPhone(phone).orElseThrow(() -> new NotFoundException("User not found"));

        return Map.of("token", jwtUtil.generateToken(user.getEmail()));
    }


    public void sendEmailOtp(String email) {

        userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));

        String rawOtp = generateOtp();

        Otp o = createOtp(rawOtp, OtpType.FORGOT_PASSWORD);
        o.setEmail(email);

        otpRepo.save(o);
        emailService.sendOtp(email, rawOtp);
    }

    public void resetPassword(String email, String otp, String newPassword) {

        Otp o = otpRepo.findTopByEmailAndTypeOrderByIdDesc(email, OtpType.FORGOT_PASSWORD).orElseThrow(() -> new NotFoundException("OTP not found"));

        validateOtp(o, otp);

        User user = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }


    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private Otp createOtp(String rawOtp, OtpType type) {
        Otp o = new Otp();
        o.setOtpHash(passwordEncoder.encode(rawOtp));
        o.setType(type);
        o.setExpiry(LocalDateTime.now().plusMinutes(5));
        o.setVerified(false);
        o.setAttempts(0);
        return o;
    }

    private void validateOtp(Otp o, String otp) {

        if (o.isVerified()) throw new BadRequestException("OTP already used");

        if (o.getExpiry().isBefore(LocalDateTime.now())) throw new BadRequestException("OTP expired");

        if (o.getAttempts() >= 3) throw new BadRequestException("OTP blocked");

        if (!passwordEncoder.matches(otp, o.getOtpHash())) {
            o.setAttempts(o.getAttempts() + 1);
            otpRepo.save(o);
            throw new BadRequestException("Invalid OTP");
        }

        o.setVerified(true);
        otpRepo.save(o);
    }
}
