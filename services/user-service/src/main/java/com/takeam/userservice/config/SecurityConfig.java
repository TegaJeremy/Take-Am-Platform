package com.takeam.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // public endpoints
                        .requestMatchers(
                                "/api/v1/traders/register",
                                "/api/v1/traders/verify-otp",
                                "/api/v1/traders/resend-otp",
                                "/api/v1/agents/register",
                                "/api/v1/agents/verify-otp",
                                "/api/v1/buyers/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/verify-otp",
                                "/api/v1/auth/login/request-otp",
                                "/api/v1/auth/login/verify-otp",
                                "/api/v1/auth/login/resend-otp",
                                "/api/v1/password/forgot",
                                "/api/v1/password/reset",
                                "/health",
                                "/actuator/**",
                                "/api/v1/admin/seed",
                                "/api/v1/buyers/register",
                                "/api/v1/buyers/verify-email",
                                "/api/v1/buyers/resend-otp"

                        ).permitAll()

                        //  SPECIFIC GET ENDPOINTS FOR CROSS-ROLE ACCESS
                        .requestMatchers(HttpMethod.GET, "/api/v1/traders/{id}").hasAnyRole("TRADER", "AGENT", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/agents/{id}").hasAnyRole("AGENT", "ADMIN", "SUPER_ADMIN")

                        // private or role endpoints
                        .requestMatchers("/api/v1/password/change").authenticated()
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/traders/**").hasRole("TRADER")
                        .requestMatchers("/api/v1/agents/**").hasRole("AGENT")
                        .requestMatchers("/api/v1/buyers/**").hasRole("BUYER")

                        // ALL OTHER REQUESTS
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
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