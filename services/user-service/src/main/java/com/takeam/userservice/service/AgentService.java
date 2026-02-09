package com.takeam.userservice.service;

import com.takeam.userservice.dto.request.AgentRegistrationRequestDto;
import com.takeam.userservice.dto.request.AgentVerifyOTPDto;
import com.takeam.userservice.dto.request.TraderRegistrationRequestDto;
import com.takeam.userservice.dto.response.AgentDetailDto;
import com.takeam.userservice.dto.response.AuthResponseDto;
import com.takeam.userservice.dto.response.UserResponseDto;
import com.takeam.userservice.exception.BadRequestException;
import com.takeam.userservice.exception.ResourceNotFoundException;
import com.takeam.userservice.exception.UnauthorizedException;
import com.takeam.userservice.mapper.AgentMapper;
import com.takeam.userservice.mapper.UserMapper;
import com.takeam.userservice.model.*;
import com.takeam.userservice.repository.AgentRepository;
import com.takeam.userservice.repository.TraderRepository;
import com.takeam.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentService {

    private final UserRepository userRepository;
    private final AgentRepository agentRepository;
    private final TraderRepository traderRepository;
    private final OTPService otpService;
    private final AgentMapper agentMapper;
    private final UserMapper userMapper;
    private final TraderService traderService;
    private final PasswordEncoder passwordEncoder;

    // agent registration

    @Transactional
    public AuthResponseDto registerAgent(AgentRegistrationRequestDto dto) {
        log.info("Starting agent registration for email: {}", dto.getEmail());

        validateEmailNotExists(dto.getEmail());
        validatePhoneNumberNotExists(dto.getPhoneNumber());

        User user = createAndSaveAgentUser(dto);
        createAndSaveAgent(dto, user);
        sendRegistrationOTP(dto.getEmail(), dto.getFullName());

        return new AuthResponseDto(
                "Registration successful! OTP sent to your email address.",
                dto.getEmail(),
                true
        );
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }
    }

    private void validatePhoneNumberNotExists(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BadRequestException("Phone number already registered");
        }
    }

    private User createAndSaveAgentUser(AgentRegistrationRequestDto dto) {
        User user = agentMapper.toUser(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());
        return savedUser;
    }

    private void createAndSaveAgent(AgentRegistrationRequestDto dto, User user) {
        Agent agent = agentMapper.toAgent(dto);
        agent.setUser(user);
        agentRepository.save(agent);
        log.info("Agent profile created for user: {}", user.getId());
    }

    private void sendRegistrationOTP(String email, String fullName) {
        String otp = otpService.generateOTP();
        otpService.storeOTP(email, otp);
        otpService.sendOTPToEmail(email, otp, fullName);
    }

    // agent otp

    @Transactional
    public UserResponseDto verifyOTP(AgentVerifyOTPDto dto) {
        log.info("Verifying OTP for email: {}", dto.getEmail());

        validateOTP(dto.getEmail(), dto.getOtp());

        User user = findUserByEmail(dto.getEmail());

        // Keep status as PENDING - needs admin approval
        log.info("Agent OTP verified, awaiting admin approval: {}", user.getId());

        return userMapper.toUserResponseDto(user);
    }

    private void validateOTP(String identifier, String otp) {
        boolean isValid = otpService.verifyOTP(identifier, otp);
        if (!isValid) {
            throw new BadRequestException("Invalid or expired OTP");
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    //aget details

    public AgentDetailDto getAgentDetails(UUID userId) {  // ← Changed return type
        log.info("Fetching agent details for user: {}", userId);

        Agent agent = findAgentByUserId(userId);
        List<Trader> tradersRegistered = traderRepository.findByRegisteredByAgentId(userId);

        AgentDetailDto response = agentMapper.toDetailResponse(agent);

        return response;
    }

    public AgentDetailDto getAgentDetailsById(UUID agentId) {
        log.info("Fetching agent details by agent ID: {}", agentId);

        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with ID: " + agentId));

        return agentMapper.toDetailResponse(agent);
    }

    private Agent findAgentByUserId(UUID userId) {
        return agentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));
    }

    // agent register traders


    @Transactional
    public AuthResponseDto registerTraderOnBehalf(
            UUID agentId,
            TraderRegistrationRequestDto dto) {

        log.info("Agent {} registering trader on behalf", agentId);

        // 1. Verify agent is approved and active
        validateAgentCanRegisterTraders(agentId);

        // 2. Call the EXISTING trader registration service!
        AuthResponseDto response = traderService.registerTrader(dto);

        // 3. Track that this agent registered this trader
        trackAgentRegistration(dto.getPhoneNumber(), agentId);

        log.info("Agent {} successfully registered trader {}", agentId, dto.getPhoneNumber());

        return response;
    }

    private void validateAgentCanRegisterTraders(UUID agentId) {
        Agent agent = findAgentByUserId(agentId);

        if (agent.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new UnauthorizedException("Agent not approved yet");
        }

        if (agent.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Agent account not active");
        }
    }

    private void trackAgentRegistration(String phoneNumber, UUID agentId) {
        // After trader is registered, update the trader record to track the agent
        User traderUser = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Trader not found"));

        Trader trader = traderRepository.findByUserId(traderUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Trader profile not found"));

        trader.setRegisteredByAgentId(agentId);
        traderRepository.save(trader);

        log.info("Tracked: Agent {} registered trader {}", agentId, trader.getId());
    }


    public List<Trader> getTradersRegisteredByAgent(UUID agentId) {
        log.info("Fetching traders registered by agent: {}", agentId);
        return traderRepository.findByRegisteredByAgentId(agentId);
    }
}