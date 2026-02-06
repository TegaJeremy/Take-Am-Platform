package com.takeam.userservice.config;

import com.takeam.userservice.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ✅ ADD THIS LINE AT THE VERY TOP
        log.info("🔍 JWT Filter hit for: {} {}", request.getMethod(), request.getRequestURI());

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("❌ No valid Authorization header found");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String phoneNumber = jwtUtil.extractPhoneNumber(jwt);
            log.info("📱 Extracted phone number: {}", phoneNumber); // ← ADD THIS

            if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.info("🔎 Looking up user in database for: {}", phoneNumber);

                var user = userRepository.findByPhoneNumber(phoneNumber)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                log.info("👤 Found user: {} with role: {}", user.getPhoneNumber(), user.getRole());
                log.info("🔐 About to validate token...");
                if (jwtUtil.validateToken(jwt, phoneNumber)) {
                    log.info("✅ Token is VALID");

                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                    log.info("🎫 Setting authorities: {}", authorities);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user,  // ← Keep the User object here
                            null,
                            authorities
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("User authenticated: {} with role: {}", phoneNumber, user.getRole());
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
//            log.warn("❌ Token validation FAILED for: {}", phoneNumber);
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}