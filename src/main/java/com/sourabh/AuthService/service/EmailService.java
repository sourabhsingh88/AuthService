//package com.sourabh.AuthService.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//    public void sendOtp(String to, String otp) {
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject("Your OTP Code");
//        message.setText(
//                "Your OTP is: " + otp + "\n\n" +
//                        "This OTP is valid for 5 minutes.\n" +
//                        "Do not share this OTP with anyone."
//        );
//
//        mailSender.send(message);
//    }
//}