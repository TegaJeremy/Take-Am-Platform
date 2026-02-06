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

    @Value("${services.user-service.url}")  // CHANGED FROM ${USER_SERVICE_URL}
    private String userServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {

        String baseUrl = userServiceUrl.endsWith("/")
                ? userServiceUrl.substring(0, userServiceUrl.length() - 1)
                : userServiceUrl;

        log.info("🚀 Gateway Routes Config - Base URL: {}", baseUrl);
        log.info("🚀 Building routes...");

        RouteLocator locator = builder.routes()
                // Auth routes
                .route("auth-routes", r -> {
                    log.info("✅ Registering auth-routes");
                    return r.path("/api/v1/auth/**")
                            .uri(baseUrl);
                })

                // Password routes
                .route("password-routes", r -> {
                    log.info("✅ Registering password-routes");
                    return r.path("/api/v1/password/**")
                            .uri(baseUrl);
                })

                // Trader routes
                .route("trader-routes", r -> {
                    log.info("✅ Registering trader-routes");
                    return r.path("/api/v1/traders/**")
                            .uri(baseUrl);
                })

                // Agent routes
                .route("agent-routes", r -> {
                    log.info("✅ Registering agent-routes");
                    return r.path("/api/v1/agents/**")
                            .uri(baseUrl);
                })

                // Buyer routes
                .route("buyer-routes", r -> {
                    log.info("✅ Registering buyer-routes");
                    return r.path("/api/v1/buyers/**")
                            .uri(baseUrl);
                })

                // Admin routes
                .route("admin-routes", r -> {
                    log.info("✅ Registering admin-routes");
                    return r.path("/api/v1/admin/**")
                            .uri(baseUrl);
                })

                .build();

        log.info("🎉 All routes registered successfully!");
        return locator;
    }
}