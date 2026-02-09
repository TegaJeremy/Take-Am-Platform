package com.takeam.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class GatewayRoutesConfig {

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${services.intake-service.url}")  // ✅ ADDED
    private String intakeServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {

        String userBaseUrl = userServiceUrl.endsWith("/")
                ? userServiceUrl.substring(0, userServiceUrl.length() - 1)
                : userServiceUrl;

        String intakeBaseUrl = intakeServiceUrl.endsWith("/")  // ✅ ADDED
                ? intakeServiceUrl.substring(0, intakeServiceUrl.length() - 1)
                : intakeServiceUrl;

        log.info("🚀 Gateway Routes Config");
        log.info("   User Service URL: {}", userBaseUrl);
        log.info("   Intake Service URL: {}", intakeBaseUrl);  // ✅ ADDED
        log.info("🚀 Building routes...");

        RouteLocator locator = builder.routes()
                // ==================== USER SERVICE ROUTES ====================

                // Auth routes
                .route("auth-routes", r -> {
                    log.info("✅ Registering auth-routes");
                    return r.path("/api/v1/auth/**")
                            .uri(userBaseUrl);
                })

                // Password routes
                .route("password-routes", r -> {
                    log.info("✅ Registering password-routes");
                    return r.path("/api/v1/password/**")
                            .uri(userBaseUrl);
                })

                // Trader routes
                .route("trader-routes", r -> {
                    log.info("✅ Registering trader-routes");
                    return r.path("/api/v1/traders/**")
                            .uri(userBaseUrl);
                })

                // Agent routes
                .route("agent-routes", r -> {
                    log.info("✅ Registering agent-routes");
                    return r.path("/api/v1/agents/**")
                            .uri(userBaseUrl);
                })

                // Buyer routes
                .route("buyer-routes", r -> {
                    log.info("✅ Registering buyer-routes");
                    return r.path("/api/v1/buyers/**")
                            .uri(userBaseUrl);
                })

                // Admin routes
                .route("admin-routes", r -> {
                    log.info("✅ Registering admin-routes");
                    return r.path("/api/v1/admin/**")
                            .uri(userBaseUrl);
                })

                // User lookup routes
                .route("user-routes", r -> {
                    log.info("✅ Registering user-routes");
                    return r.path("/api/v1/users/**")
                            .uri(userBaseUrl);
                })

                // ==================== INTAKE SERVICE ROUTES ====================

                // Trader requests routes
                .route("trader-requests-routes", r -> {
                    log.info("✅ Registering trader-requests-routes");
                    return r.path("/api/v1/trader-requests/**")
                            .uri(intakeBaseUrl);
                })

                // Agent requests routes
                .route("agent-requests-routes", r -> {
                    log.info("✅ Registering agent-requests-routes");
                    return r.path("/api/v1/agent-requests/**")
                            .uri(intakeBaseUrl);
                })

                // Grading routes
                .route("gradings-routes", r -> {
                    log.info("✅ Registering gradings-routes");
                    return r.path("/api/v1/gradings/**")
                            .uri(intakeBaseUrl);
                })

                .build();

        log.info("🎉 All routes registered successfully!");
        return locator;
    }
}