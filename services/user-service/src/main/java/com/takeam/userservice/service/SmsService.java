package com.takeam.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    @Value("${termii.api-key")
    private String apiKey;

//    @Value("${termii.sender-id:talert}")
//    private String senderId;

    @Value("${termii.api-url")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public void sendSms(String phoneNumber, String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("to", phoneNumber);
            body.put("sms", message);
            body.put("type", "plain");
            body.put("channel", "generic"); // ✅ use generic
            body.put("api_key", apiKey);

//            body.put("to", phoneNumber);
//            body.put("from", senderId);
//            body.put("sms", message);
//            body.put("type", "plain");
//            body.put("channel", "dnd");
//            body.put("api_key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            log.info("Calling Termii URL: {}", apiUrl);
            log.info("API Key starts with: {}", apiKey.isEmpty() ? "EMPTY!" : apiKey.substring(0, 10) + "...");
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SMS sent successfully to: {}", phoneNumber);
            } else {
                log.error("SMS sending failed for: {} - Status: {}", phoneNumber, response.getStatusCode());
                logSmsToConsole(phoneNumber, message);
            }

        } catch (Exception e) {
            log.error("Failed to send SMS to: {} - Error: {}", phoneNumber, e.getMessage());
            logSmsToConsole(phoneNumber, message);
        }
    }

    public void sendOtpSms(String phoneNumber, String otp) {
        String message = String.format(
                "Your TakeAm verification code is: %s\nValid for 5 minutes. Do not share this code.",
                otp
        );
        sendSms(phoneNumber, message);
    }

    public void sendWelcomeSms(String phoneNumber, String name) {
        String message = String.format(
                "Welcome to TakeAm, %s! Your account is now active. Start buying and selling fresh produce today!",
                name
        );
        sendSms(phoneNumber, message);
    }

    public void sendAccountApprovedSms(String phoneNumber, String name) {
        String message = String.format(
                "Congratulations %s! Your TakeAm agent account has been approved. You can now log in and start registering traders.",
                name
        );
        sendSms(phoneNumber, message);
    }

    public void sendAccountLockedSms(String phoneNumber) {
        String message = "Your TakeAm account has been temporarily locked due to multiple failed login attempts. Try again in 30 minutes.";
        sendSms(phoneNumber, message);
    }

    private void logSmsToConsole(String phoneNumber, String message) {
        log.info("=".repeat(50));
        log.info("SMS TO: {}", phoneNumber);
        log.info("MESSAGE: {}", message);
        log.info("=".repeat(50));
    }
}