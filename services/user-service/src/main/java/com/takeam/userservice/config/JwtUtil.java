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

/**
 * JWT Utility Class
 *
 * Responsibilities:
 * - Generate JWT access and refresh tokens
 * - Validate tokens
 * - Extract claims from tokens
 * - Handle token signing and verification
 *
 * This is the ONLY class that should handle JWT token creation and validation.
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * Generate secret key from configured secret
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate JWT access token
     *
     * Token structure:
     * - Subject: userId (UUID string)
     * - Claims: phoneNumber, role
     *
     * @param userId User's UUID as string
     * @param phoneNumber User's phone number (stored as claim for logging)
     * @param role User's role
     * @return JWT token string
     */
    public String generateToken(String userId, String phoneNumber, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("phoneNumber", phoneNumber);
        claims.put("role", role);

        return createToken(claims, userId, expiration);
    }

    /**
     * Generate JWT refresh token
     *
     * Refresh tokens are simpler and contain minimal claims
     *
     * @param userId User's UUID as string
     * @return Refresh token string
     */
    public String generateRefreshToken(String userId) {
        return createToken(new HashMap<>(), userId, refreshExpiration);
    }

    /**
     * Create JWT token with given claims and subject
     *
     * @param claims Additional claims to embed
     * @param subject Token subject (userId)
     * @param validity Token validity period in milliseconds
     * @return Signed JWT token
     */
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

    /**
     * Extract user ID from token
     *
     * The subject of the token IS the userId
     *
     * @param token JWT token
     * @return User ID (UUID string)
     */
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract phone number from token claims
     *
     * @param token JWT token
     * @return Phone number
     */
    public String extractPhoneNumber(String token) {
        return extractClaim(token, claims -> claims.get("phoneNumber", String.class));
    }

    /**
     * Extract role from token claims
     *
     * @param token JWT token
     * @return User role
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extract expiration date from token
     *
     * @param token JWT token
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract any claim from token using a custom resolver
     *
     * @param token JWT token
     * @param claimsResolver Function to extract specific claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     *
     * @param token JWT token
     * @return All claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired
     *
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate JWT token against user ID
     *
     * Checks:
     * 1. Token subject matches userId
     * 2. Token is not expired
     *
     * @param token JWT token
     * @param userId Expected user ID
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token, String phoneNumber) {
        try {
            log.info("🔍 Validating token for phone: {}", phoneNumber);

            final String extractedPhone = extractPhoneNumber(token);
            log.info("📱 Extracted phone from token: {}", extractedPhone);

            boolean isExpired = isTokenExpired(token);
            log.info("⏰ Is token expired? {}", isExpired);

            boolean matches = extractedPhone.equals(phoneNumber);
            log.info("🔄 Phone numbers match? {}", matches);

            boolean result = matches && !isExpired;
            log.info("✅ Final validation result: {}", result);

            return result;
        } catch (Exception e) {
            log.error("❌ Token validation error: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Validate token without comparing userId
     *
     * Used for initial token validation before user lookup
     *
     * @param token JWT token
     * @return true if token is valid and not expired
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}