package com.takeam.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (we're using JWT tokens, not cookies)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure authorization
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (anyone can access)
                        .requestMatchers(
                                "/api/v1/traders/register",       // Trader registration
                                "/api/v1/traders/verify-otp",     // Trader OTP verification
                                "/api/v1/traders/resend-otp",     // Trader resend OTP

                                "/api/v1/agents/register",        // Agent registration (to be built)
                                "/api/v1/buyers/register",        // Buyer registration (to be built)

                                //unified login path
                                "/api/v1/auth/login",           // ✅ Unified login
                                "/api/v1/auth/verify-otp",

                                "/api/v1/auth/login/request-otp", // Login OTP request
                                "/api/v1/auth/login/verify-otp",  // Login OTP verification
                                "/api/v1/auth/login/resend-otp",  // Resend login OTP

                                "/api/v1/password/forgot",    // ← Add this
                                "/api/v1/password/reset",

                                "/health",                         // Health check
                                "/actuator/**" ,            // Spring actuator (optional)

                                "/api/v1/admin/seed"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // Protected endpoints (need JWT token)
                        .requestMatchers("/api/v1/password/change").authenticated()
                        .requestMatchers("/api/v1/traders/**").hasRole("TRADER")
                        .requestMatchers("/api/v1/agents/**").hasRole("AGENT")
                        .requestMatchers("/api/v1/buyers/**").hasRole("BUYER")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )

                // Stateless session (no session storage, pure JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Add JWT filter before Spring Security's authentication filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}