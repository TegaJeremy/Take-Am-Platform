package com.takeam.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

       public void sendOTPEmail(String toEmail, String otp, String recipientName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@takeam.ng");
            message.setTo(toEmail);
            message.setSubject("TakeAm - Verify Your Email");
            message.setText(buildOTPEmailBody(otp, recipientName));

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);

            log.info("=".repeat(50));
            log.info(" EMAIL OTP FOR: {}", toEmail);
            log.info(" OTP CODE: {}", otp);
            log.info(" Valid for: 5 minutes");
            log.info("=".repeat(50));
        }
    }


    private String buildOTPEmailBody(String otp, String recipientName) {
        return String.format("""
            Hello %s,
            
            Your TakeAm verification code is:
            
            %s
            
            This code will expire in 5 minutes.
            
            If you didn't request this code, please ignore this email.
            
            Best regards,
            TakeAm Team
            """, recipientName, otp);
    }
}