package com.takeam.userservice.service;

import com.takeam.userservice.dto.request.ClockInRequest;
import com.takeam.userservice.dto.request.ClockOutRequest;
//import com.takeam.userservice.dto.response.AgentStatusResponse;
import com.takeam.userservice.dto.response.AttendanceResponse;
import com.takeam.userservice.exception.BadRequestException;
import com.takeam.userservice.exception.ResourceNotFoundException;
import com.takeam.userservice.mapper.AttendanceMapper;
import com.takeam.userservice.model.Agent;
import com.takeam.userservice.model.AgentAttendance;
import com.takeam.userservice.model.Role;
import com.takeam.userservice.model.User;
import com.takeam.userservice.repository.AgentAttendanceRepository;
import com.takeam.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentAttendanceService {

    private final AgentAttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final AttendanceMapper attendanceMapper;

    // Market zone configuration
    private static final double MARKET_CENTER_LAT = 6.4550;
    private static final double MARKET_CENTER_LNG = 3.3941;
    private static final double MARKET_RADIUS_KM = 2.0;

    // Working hours configuration
    private static final LocalTime WORK_START_TIME = LocalTime.of(10, 0);
    private static final LocalTime WORK_END_TIME = LocalTime.of(18, 0);


    @Transactional
    public AttendanceResponse clockIn(UUID agentId, ClockInRequest request) {
        log.info("Clock-in request for agent ID: {}", agentId);

        validateAgentExists(agentId);
        validateNotAlreadyClockedIn(agentId);
        validateWorkingHours();
        validateLocationInMarketZone(request.getLatitude(), request.getLongitude());

        AgentAttendance attendance = createClockInRecord(agentId, request);
        AgentAttendance saved = attendanceRepository.save(attendance);

        log.info("Agent {} clocked in successfully at {}", agentId, saved.getClockInTime());
        return mapToResponse(saved, "Clocked in successfully!");
    }


    @Transactional
    public AttendanceResponse clockOut(UUID agentId, ClockOutRequest request) {
        log.info("Clock-out request for agent ID: {}", agentId);

        AgentAttendance attendance = getTodayAttendanceRecord(agentId);
        validateNotAlreadyClockedOut(attendance);

        updateClockOutDetails(attendance, request);
        calculateAndSetTotalHours(attendance);

        AgentAttendance saved = attendanceRepository.save(attendance);
        log.info("Agent {} clocked out. Total hours: {}", agentId, saved.getTotalHoursWorked());

        return mapToResponse(saved,
                String.format("Clocked out successfully! You worked %.2f hours today.",
                        saved.getTotalHoursWorked()));
    }


    public AttendanceResponse getClockInStatus(UUID agentId) {
        return attendanceRepository.findByAgentIdAndDate(agentId, LocalDate.now())
                .map(attendance -> mapToResponse(attendance, null))
                .orElseGet(() -> createNotClockedInResponse(agentId));
    }




    public boolean isAgentClockedIn(UUID agentId) {
        return attendanceRepository.isAgentClockedIn(agentId, LocalDate.now());
    }


    public List<AttendanceResponse> getAttendanceHistory(UUID agentId, int limit) {
        return attendanceRepository.findByAgentIdOrderByDateDesc(agentId)
                .stream()
                .limit(limit)
                .map(attendance -> mapToResponse(attendance, null))
                .collect(Collectors.toList());
    }


    @Transactional
    public void incrementCompletedPickups(UUID agentId) {
        attendanceRepository.findByAgentIdAndDate(agentId, LocalDate.now())
                .ifPresent(attendance -> {
                    attendance.setCompletedPickups(attendance.getCompletedPickups() + 1);
                    attendanceRepository.save(attendance);
                    log.info("Incremented pickups for agent {}: {}",
                            agentId, attendance.getCompletedPickups());
                });
    }

    // ==================== VALIDATION METHODS ====================

    private void validateAgentExists(UUID agentId) {
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        if (!agent.getRole().equals(Role.AGENT)) {
            throw new BadRequestException("Only agents can clock in");
        }
    }

    private void validateNotAlreadyClockedIn(UUID agentId) {
        attendanceRepository.findByAgentIdAndDate(agentId, LocalDate.now())
                .ifPresent(attendance -> {
                    if (attendance.getStatus() == AgentAttendance.AttendanceStatus.CLOCKED_IN) {
                        throw new BadRequestException("You are already clocked in today");
                    }
                });
    }

    private void validateWorkingHours() {
        LocalTime currentTime = LocalTime.now();
        if (currentTime.isBefore(WORK_START_TIME)) {
            throw new BadRequestException(
                    String.format("Cannot clock in before %s", WORK_START_TIME));
        }
    }

    private void validateLocationInMarketZone(double latitude, double longitude) {
        if (!isLocationInMarketZone(latitude, longitude)) {
            throw new BadRequestException(
                    "You must be within the market area to clock in. " +
                            "Please ensure you are at the correct location.");
        }
    }

    private void validateNotAlreadyClockedOut(AgentAttendance attendance) {
        if (attendance.getStatus() == AgentAttendance.AttendanceStatus.CLOCKED_OUT) {
            throw new BadRequestException("You have already clocked out today");
        }
    }

    // ==================== RECORD CREATION METHODS ====================

    private AgentAttendance createClockInRecord(UUID agentId, ClockInRequest request) {
        AgentAttendance attendance = new AgentAttendance();
        attendance.setAgentId(agentId);
        attendance.setDate(LocalDate.now());
        attendance.setClockInTime(LocalDateTime.now());
        attendance.setClockInLatitude(request.getLatitude());
        attendance.setClockInLongitude(request.getLongitude());
        attendance.setClockInAddress(request.getAddress());
        attendance.setIsInMarketZone(true);
        attendance.setStatus(AgentAttendance.AttendanceStatus.CLOCKED_IN);
        attendance.setCompletedPickups(0);
        return attendance;
    }

    private void updateClockOutDetails(AgentAttendance attendance, ClockOutRequest request) {
        attendance.setClockOutTime(LocalDateTime.now());
        attendance.setClockOutLatitude(request.getLatitude());
        attendance.setClockOutLongitude(request.getLongitude());
        attendance.setClockOutAddress(request.getAddress());
        attendance.setStatus(AgentAttendance.AttendanceStatus.CLOCKED_OUT);
    }

    private void calculateAndSetTotalHours(AgentAttendance attendance) {
        Duration duration = Duration.between(
                attendance.getClockInTime(),
                attendance.getClockOutTime()
        );
        double hoursWorked = duration.toMinutes() / 60.0;
        double roundedHours = Math.round(hoursWorked * 100.0) / 100.0;
        attendance.setTotalHoursWorked(roundedHours);
    }

    // ==================== RETRIEVAL METHODS ====================

    private AgentAttendance getTodayAttendanceRecord(UUID agentId) {
        return attendanceRepository.findByAgentIdAndDate(agentId, LocalDate.now())
                .orElseThrow(() -> new BadRequestException("You are not clocked in today"));
    }

    private AttendanceResponse createNotClockedInResponse(UUID agentId) {
        AttendanceResponse response = new AttendanceResponse();
        response.setAgentId(agentId);
        response.setStatus("NOT_CLOCKED_IN");
        response.setMessage("You are not clocked in today");
        return response;
    }

    // ==================== GEOLOCATION METHODS ====================

    private boolean isLocationInMarketZone(double latitude, double longitude) {
        double distance = calculateDistance(
                MARKET_CENTER_LAT, MARKET_CENTER_LNG,
                latitude, longitude
        );
        log.debug("Distance from market center: {} km", distance);
        return distance <= MARKET_RADIUS_KM;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    // ==================== MAPPING METHODS ====================

    private AttendanceResponse mapToResponse(AgentAttendance attendance, String message) {
        AttendanceResponse response = attendanceMapper.toResponse(attendance);
        if (message != null) {
            response.setMessage(message);
        }
        return response;
    }
}