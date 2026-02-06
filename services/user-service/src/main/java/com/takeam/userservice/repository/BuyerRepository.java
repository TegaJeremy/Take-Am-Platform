package com.takeam.userservice.repository;

import com.takeam.userservice.model.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, UUID> {

    Optional<Buyer> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}