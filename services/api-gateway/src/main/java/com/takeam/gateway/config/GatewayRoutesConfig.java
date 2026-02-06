package com.takeam.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${USER_SERVICE_URL:http://localhost:8081}")
    private String userServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {

        // Clean the URL - remove trailing slash
        String baseUrl = userServiceUrl.endsWith("/")
                ? userServiceUrl.substring(0, userServiceUrl.length() - 1)
                : userServiceUrl;

        System.out.println("🚀 Gateway Routes Config - Base URL: " + baseUrl);
        System.out.println("🚀 Building routes...");

        try {
            RouteLocator locator = builder.routes()
                    // Auth routes (public - no auth needed)
                    .route("auth-routes", r -> {
                        System.out.println("✅ Registering auth-routes");
                        return r.path("/api/v1/auth/**")
                                .uri(baseUrl);
                    })

                    // Password routes (public - no auth needed)
                    .route("password-routes", r -> {
                        System.out.println("✅ Registering password-routes");
                        return r.path("/api/v1/password/**")
                                .uri(baseUrl);
                    })

                    // Trader routes (for now, no auth - we'll add it back later)
                    .route("trader-routes", r -> {
                        System.out.println("✅ Registering trader-routes");
                        return r.path("/api/v1/traders/**")
                                .uri(baseUrl);
                    })

                    // Agent routes
                    .route("agent-routes", r -> {
                        System.out.println("✅ Registering agent-routes");
                        return r.path("/api/v1/agents/**")
                                .uri(baseUrl);
                    })

                    // Buyer routes
                    .route("buyer-routes", r -> {
                        System.out.println("✅ Registering buyer-routes");
                        return r.path("/api/v1/buyers/**")
                                .uri(baseUrl);
                    })

                    // Admin routes
                    .route("admin-routes", r -> {
                        System.out.println("✅ Registering admin-routes");
                        return r.path("/api/v1/admin/**")
                                .uri(baseUrl);
                    })

                    .build();

            System.out.println("🎉 All routes registered successfully!");
            return locator;

        } catch (Exception e) {
            System.err.println("❌ ERROR building routes: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}