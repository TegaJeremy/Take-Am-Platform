package com.takeam.userservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    public String generateToken(String userId, String phoneNumber, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("phoneNumber", phoneNumber);
        claims.put("role", role);

        return createToken(claims, userId, expiration);
    }


    public String generateRefreshToken(String userId) {
        return createToken(new HashMap<>(), userId, refreshExpiration);
    }


    private String createToken(Map<String, Object> claims, String subject, Long validity) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public String extractPhoneNumber(String token) {
        return extractClaim(token, claims -> claims.get("phoneNumber", String.class));
    }


    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }


    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    public boolean validateToken(String token, String phoneNumber) {
        try {
            log.info("🔍 Validating token for phone: {}", phoneNumber);

            final String extractedPhone = extractPhoneNumber(token);
            log.info(" Extracted phone from token: {}", extractedPhone);

            boolean isExpired = isTokenExpired(token);
            log.info(" Is token expired? {}", isExpired);

            boolean matches = extractedPhone.equals(phoneNumber);
            log.info(" Phone numbers match? {}", matches);

            boolean result = matches && !isExpired;
            log.info("Final validation result: {}", result);

            return result;
        } catch (Exception e) {
            log.error(" Token validation error: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}