package com.takeam.userservice.repository;

import com.takeam.userservice.model.Trader;
import com.takeam.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TraderRepository extends JpaRepository<Trader, UUID> {
    Optional<Trader> findByUser(User user);

    Optional<Trader> findByUserId(UUID userId);

//    List<Traders> findByMarketId(String marketId);

    boolean existsByUserId(UUID userId);

    List<Trader> findByRegisteredByAgentId(UUID userId);
}
