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

        // 1. Extract Authorization header
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token, continue to next filter
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Extract token (remove "Bearer " prefix)
            final String jwt = authHeader.substring(7);

            // 3. Extract phone number from token
            final String phoneNumber = jwtUtil.extractPhoneNumber(jwt);

            // 4. If phone number exists and user is not already authenticated
            if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Load user from database
                var user = userRepository.findByPhoneNumber(phoneNumber)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                // 6. Validate token
                if (jwtUtil.validateToken(jwt, phoneNumber)) {

                    // 7. Create authentication object
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            authorities
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 8. Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("User authenticated: {} with role: {}", phoneNumber, user.getRole());
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        // 9. Continue filter chain
        filterChain.doFilter(request, response);
    }
}