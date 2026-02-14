package com.takeam.userservice.repository;

import com.takeam.userservice.model.AgentAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentAttendanceRepository extends JpaRepository<AgentAttendance, UUID> {

    Optional<AgentAttendance> findByAgentIdAndDate(UUID agentId, LocalDate date);

    List<AgentAttendance> findByAgentIdOrderByDateDesc(UUID agentId);

    @Query("SELECT a FROM AgentAttendance a WHERE a.agentId = :agentId AND a.date BETWEEN :startDate AND :endDate ORDER BY a.date DESC")
    List<AgentAttendance> findByAgentIdAndDateBetween(
            @Param("agentId") UUID agentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AgentAttendance a WHERE a.agentId = :agentId AND a.date = :date AND a.status = 'CLOCKED_IN'")
    boolean isAgentClockedIn(@Param("agentId") UUID agentId, @Param("date") LocalDate date);

    @Query("SELECT a FROM AgentAttendance a WHERE a.date = :date AND a.status = 'CLOCKED_IN'")
    List<AgentAttendance> findAllClockedInToday(@Param("date") LocalDate date);

    long countByAgentId(UUID agentId);
}