package com.takeam.userservice.repository;

import com.takeam.userservice.models.Role;
import com.takeam.userservice.models.User;
import com.takeam.userservice.models.UserStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository  extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> phoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    Optional<User> findByPhoneNumberAndStatus(String phoneNumber, UserStatus status);
    Optional<User> findByPhoneNumber(String phoneNumber);

    //admin
    boolean existsByRole(Role role);

    List<User> findByRole(Role role);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByRoleAndStatus(Role role, UserStatus status, Pageable pageable);

    Long countByRole(Role role);

    Long countByStatus(UserStatus status);

    Long countByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt < :endDate")
    Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}


