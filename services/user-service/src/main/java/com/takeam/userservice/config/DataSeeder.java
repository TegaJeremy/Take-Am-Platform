package com.takeam.userservice.config;

import com.takeam.userservice.models.Role;
import com.takeam.userservice.models.User;
import com.takeam.userservice.models.UserStatus;
import com.takeam.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedSuperAdmin();
    }

    private void seedSuperAdmin() {
        // Check if super admin already exists
        if (userRepository.findByEmail("oghenedemartin@gmail.com").isPresent()) {
            log.info("Super admin already exists");
            return;
        }

        // Create super admin
        User superAdmin = new User();
        superAdmin.setEmail("oghenedemartin@gmail.com");
        superAdmin.setFullName("Super Administrator");
        superAdmin.setPasswordHash(passwordEncoder.encode("SuperAdmin@2024"));
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setStatus(UserStatus.ACTIVE);
        superAdmin.setLoginAttempts(0);
        superAdmin.setPhoneNumber("+234808182545");

        userRepository.save(superAdmin);
        log.info("✅ Super admin created: superadmin@takeam.ng");
    }
}