package com.takeam.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPService {
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private static final String OTP_PREFIX = "otp:";
    private static final int OTP_LENGTH = 6;
    private static final Duration OTP_VALIDITY = Duration.ofMinutes(5);


    public String generateOTP(){
        SecureRandom random = new SecureRandom();
        int otp = 10000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public void storeOTP(String identifier, String otp  ){
        String key =  OTP_PREFIX + identifier;
        redisTemplate.opsForValue().set(key, otp,  OTP_VALIDITY);
        log.info("OTP stored for: {} (expires in 5 minutes)", identifier);

    }

    public boolean verifyOTP(String identifier, String otp) {
        String key = OTP_PREFIX + identifier;
        String storedOTP = redisTemplate.opsForValue().get(key);

        if (storedOTP == null) {
            log.warn("OTP not found or expired for: {}", identifier);
            return false;
        }

        boolean isValid = storedOTP.equals(otp);

        if (isValid) {
            redisTemplate.delete(key);
            log.info("OTP verified successfully for: {}", identifier);
        } else {
            log.warn("Invalid OTP attempt for: {}", identifier);
        }

        return isValid;
    }

    public void sendOTPToPhone(String phoneNumber, String otp) {
        log.info("=".repeat(50));
        log.info("SENDING SMS OTP TO: {}", phoneNumber);
        log.info("OTP CODE: {}", otp);
        log.info(" Valid for: 5 minutes");
        log.info("=".repeat(50));

    }

    public void sendOTPToEmail(String email, String otp, String recipientName) {
        emailService.sendOTPEmail(email, otp, recipientName);
    }


    public boolean hasValidOTP(String identifier) {
        String key = OTP_PREFIX + identifier;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

}
