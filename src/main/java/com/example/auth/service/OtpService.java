package com.example.auth.service;

import com.example.auth.entity.Otp;
import com.example.auth.entity.User;
import com.example.auth.enums.OtpType;
import com.example.auth.exceptions.BadRequestException;
import com.example.auth.exceptions.NotFoundException;
import com.example.auth.repository.OtpRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.regex.Pattern;


@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final UserRepository userRepo;
    private final OtpRepository otpRepo;
    private final SmsService smsService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Pattern OTP_PATTERN = Pattern.compile("^\\d{6}$");

    @Value("${otp.expiry.minutes:5}")
    private int otpExpiryMinutes;

    @Value("${otp.max.attempts:3}")
    private int maxOtpAttempts;

    @Value("${otp.rate.limit.minutes:1}")
    private int rateLimitMinutes;


    @Transactional
    public void sendPhoneOtp(String phone) {
        log.info("Sending phone OTP to: {}", maskPhone(phone));


        validatePhone(phone);


        userRepo.findByPhone(phone)
                .orElseThrow(() -> {
                    log.warn("Phone OTP request failed: Phone not found: {}", maskPhone(phone));
                    return new NotFoundException("User not found with this phone number");
                });


        checkRateLimit(phone, OtpType.PHONE_LOGIN);


        String rawOtp = generateOtp();
        Otp otp = createOtp(rawOtp, OtpType.PHONE_LOGIN);
        otp.setPhone(phone);

        otpRepo.save(otp);


        smsService.sendOtp(phone, rawOtp);

        log.info("Phone OTP sent successfully to: {}", maskPhone(phone));
    }


    @Transactional
    public String verifyPhoneOtp(String phone, String otp) {
        log.info("Verifying phone OTP for: {}", maskPhone(phone));


        validatePhone(phone);
        validateOtpFormat(otp);


        Otp otpEntity = otpRepo.findTopByPhoneAndTypeOrderByIdDesc(phone, OtpType.PHONE_LOGIN)
                .orElseThrow(() -> {
                    log.warn("OTP verification failed: No OTP found for phone: {}", maskPhone(phone));
                    return new NotFoundException("No OTP found. Please request a new one.");
                });


        validateOtp(otpEntity, otp);

        User user = userRepo.findByPhone(phone)
                .orElseThrow(() -> {
                    log.error("User not found after OTP verification for phone: {}", maskPhone(phone));
                    return new NotFoundException("User not found");
                });


        String token = jwtUtil.generateToken(user.getEmail());

        log.info("Phone OTP verified successfully for: {}", maskPhone(phone));
        return token;
    }


    @Transactional
    public void sendEmailOtp(String email) {
        log.info("Sending email OTP to: {}", maskEmail(email));


        validateEmail(email);


        userRepo.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Email OTP request failed: Email not found: {}", maskEmail(email));
                    return new NotFoundException("User not found with this email address");
                });


        checkRateLimit(email, OtpType.FORGOT_PASSWORD);


        String rawOtp = generateOtp();
        Otp otp = createOtp(rawOtp, OtpType.FORGOT_PASSWORD);
        otp.setEmail(email);

        otpRepo.save(otp);


        emailService.sendOtp(email, rawOtp);

        log.info("Email OTP sent successfully to: {}", maskEmail(email));
    }


    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        log.info("Password reset request for email: {}", maskEmail(email));


        validateEmail(email);
        validateOtpFormat(otp);
        validatePassword(newPassword);


        Otp otpEntity = otpRepo.findTopByEmailAndTypeOrderByIdDesc(email, OtpType.FORGOT_PASSWORD)
                .orElseThrow(() -> {
                    log.warn("Password reset failed: No OTP found for email: {}", maskEmail(email));
                    return new NotFoundException("No OTP found. Please request a new one.");
                });

        validateOtp(otpEntity, otp);


        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found after OTP verification for email: {}", maskEmail(email));
                    return new NotFoundException("User not found");
                });


        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        log.info("Password reset successfully for email: {}", maskEmail(email));
    }


    private String generateOtp() {
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }


    private Otp createOtp(String rawOtp, OtpType type) {
        Otp otp = new Otp();
        otp.setOtpHash(passwordEncoder.encode(rawOtp));
        otp.setType(type);
        otp.setExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        otp.setVerified(false);
        otp.setAttempts(0);
        return otp;
    }


    private void validateOtp(Otp otpEntity, String otp) {
        // Check if already verified
        if (otpEntity.isVerified()) {
            log.warn("OTP validation failed: OTP already used");
            throw new BadRequestException("OTP already used. Please request a new one.");
        }


        if (otpEntity.getExpiry().isBefore(LocalDateTime.now())) {
            log.warn("OTP validation failed: OTP expired");
            throw new BadRequestException(
                    String.format("OTP expired. Please request a new one. (Valid for %d minutes)", otpExpiryMinutes)
            );
        }


        if (otpEntity.getAttempts() >= maxOtpAttempts) {
            log.warn("OTP validation failed: Maximum attempts exceeded");
            throw new BadRequestException(
                    String.format("OTP blocked due to too many failed attempts. Please request a new one.")
            );
        }


        if (!passwordEncoder.matches(otp, otpEntity.getOtpHash())) {
            otpEntity.setAttempts(otpEntity.getAttempts() + 1);
            otpRepo.save(otpEntity);

            int remainingAttempts = maxOtpAttempts - otpEntity.getAttempts();

            log.warn("OTP validation failed: Invalid OTP. Remaining attempts: {}", remainingAttempts);

            throw new BadRequestException(
                    String.format("Invalid OTP. %d attempt(s) remaining.", remainingAttempts)
            );
        }


        otpEntity.setVerified(true);
        otpRepo.save(otpEntity);

        log.debug("OTP validated successfully");
    }


    private void checkRateLimit(String identifier, OtpType otpType) {

        if (otpType == OtpType.PHONE_LOGIN) {
            otpRepo.findTopByPhoneAndTypeOrderByIdDesc(identifier, otpType)
                    .ifPresent(lastOtp -> {
                        LocalDateTime rateLimitTime = LocalDateTime.now().minusMinutes(rateLimitMinutes);
                        // Check if OTP was created recently (within rate limit window)
                        if (lastOtp.getExpiry().minusMinutes(otpExpiryMinutes).isAfter(rateLimitTime)) {
                            log.warn("Rate limit exceeded for phone: {}", maskPhone(identifier));
                            throw new BadRequestException(
                                    String.format("Please wait %d minute(s) before requesting another OTP", rateLimitMinutes)
                            );
                        }
                    });
        } else {
            otpRepo.findTopByEmailAndTypeOrderByIdDesc(identifier, otpType)
                    .ifPresent(lastOtp -> {
                        LocalDateTime rateLimitTime = LocalDateTime.now().minusMinutes(rateLimitMinutes);
                        // Check if OTP was created recently (within rate limit window)
                        if (lastOtp.getExpiry().minusMinutes(otpExpiryMinutes).isAfter(rateLimitTime)) {
                            log.warn("Rate limit exceeded for email: {}", maskEmail(identifier));
                            throw new BadRequestException(
                                    String.format("Please wait %d minute(s) before requesting another OTP", rateLimitMinutes)
                            );
                        }
                    });
        }
    }


    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email cannot be empty");
        }

        Pattern emailPattern = Pattern.compile(
                "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
        );

        if (!emailPattern.matcher(email).matches()) {
            throw new BadRequestException("Invalid email format");
        }
    }


    private void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new BadRequestException("Phone number cannot be empty");
        }

        String cleanPhone = phone.replaceAll("[\\s-]", "");
        Pattern phonePattern = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

        if (!phonePattern.matcher(cleanPhone).matches()) {
            throw new BadRequestException(
                    "Invalid phone number format. Use international format (e.g., +1234567890)"
            );
        }
    }


    private void validateOtpFormat(String otp) {
        if (otp == null || otp.trim().isEmpty()) {
            throw new BadRequestException("OTP cannot be empty");
        }

        if (!OTP_PATTERN.matcher(otp).matches()) {
            throw new BadRequestException("OTP must be exactly 6 digits");
        }
    }


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
                    "Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:',.<>?)"
            );
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Mask email for logging (show only domain).
     *
     * @param email the email to mask
     * @return masked email
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****@****.***";
        }
        String[] parts = email.split("@");
        return "****@" + parts[1];
    }

    /**
     * Mask phone number for logging (show only last 4 digits).
     *
     * @param phone the phone number to mask
     * @return masked phone number
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "****" + phone.substring(phone.length() - 4);
    }

    /**
     * Mask identifier (email or phone) for logging.
     *
     * @param identifier email or phone number
     * @return masked identifier
     */
    private String maskIdentifier(String identifier) {
        if (identifier == null) {
            return "****";
        }
        return identifier.contains("@") ? maskEmail(identifier) : maskPhone(identifier);
    }
}