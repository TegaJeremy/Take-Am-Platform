package com.takeam.gateway.config;

import com.takeam.gateway.filter.AuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class GatewayRoutesConfig {

    private final AuthenticationFilter authenticationFilter;

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${services.intake-service.url}")
    private String intakeServiceUrl;

    @Value("${services.marketplace-service.url}")
    private String marketplaceServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {

        String userBaseUrl = userServiceUrl.endsWith("/")
                ? userServiceUrl.substring(0, userServiceUrl.length() - 1)
                : userServiceUrl;

        String intakeBaseUrl = intakeServiceUrl.endsWith("/")
                ? intakeServiceUrl.substring(0, intakeServiceUrl.length() - 1)
                : intakeServiceUrl;

        String marketplaceBaseUrl = marketplaceServiceUrl.endsWith("/")
                ? marketplaceServiceUrl.substring(0, marketplaceServiceUrl.length() - 1)
                : marketplaceServiceUrl;

        log.info("Gateway Routes Config");
        log.info("   User Service URL: {}", userBaseUrl);
        log.info("   Intake Service URL: {}", intakeBaseUrl);
        log.info("   Marketplace Service URL: {}", marketplaceBaseUrl);
        log.info("Building routes...");

        RouteLocator locator = builder.routes()

                // PAYSTACK WEBHOOK - NO AUTH (must be first, public access)
                .route("marketplace-webhook", r -> {
                    log.info("Registering marketplace-webhook (public)");
                    return r.path("/api/v1/marketplace/payment/webhook")
                            .uri(marketplaceBaseUrl);
                })

                // MARKETPLACE ADMIN PRODUCTS - WITH AUTH
                .route("marketplace-admin-products", r -> {
                    log.info("Registering marketplace-admin-products");
                    return r.path("/api/v1/admin/products/**")
                            .filters(f -> f.filter(authenticationFilter.apply(
                                    new AuthenticationFilter.Config())))
                            .uri(marketplaceBaseUrl);
                })

                // MARKETPLACE PROTECTED (CART/ORDERS/PAYMENT) - WITH AUTH
                .route("marketplace-protected", r -> {
                    log.info("Registering marketplace-protected (cart/orders/payment)");
                    return r.path(
                                    "/api/v1/marketplace/cart/**",
                                    "/api/v1/marketplace/orders/**",
                                    "/api/v1/marketplace/checkout/**",
                                    "/api/v1/marketplace/payment/initialize/**",
                                    "/api/v1/marketplace/payment/verify/**"
                            )
                            .filters(f -> f.filter(authenticationFilter.apply(
                                    new AuthenticationFilter.Config())))
                            .uri(marketplaceBaseUrl);
                })

                // MARKETPLACE PUBLIC - NO AUTH
                .route("marketplace-public", r -> {
                    log.info("Registering marketplace-public");
                    return r.path("/api/v1/marketplace/**")
                            .uri(marketplaceBaseUrl);
                })

                // USER SERVICE ROUTES
                .route("auth-routes", r -> {
                    log.info("Registering auth-routes");
                    return r.path("/api/v1/auth/**")
                            .uri(userBaseUrl);
                })

                .route("password-routes", r -> {
                    log.info("Registering password-routes");
                    return r.path("/api/v1/password/**")
                            .uri(userBaseUrl);
                })

                .route("trader-routes", r -> {
                    log.info("Registering trader-routes");
                    return r.path("/api/v1/traders/**")
                            .uri(userBaseUrl);
                })

                .route("agent-routes", r -> {
                    log.info("Registering agent-routes");
                    return r.path("/api/v1/agents/**")
                            .uri(userBaseUrl);
                })

                .route("buyer-routes", r -> {
                    log.info("Registering buyer-routes");
                    return r.path("/api/v1/buyers/**")
                            .uri(userBaseUrl);
                })

                .route("admin-routes", r -> {
                    log.info("Registering admin-routes");
                    return r.path("/api/v1/admin/**")
                            .uri(userBaseUrl);
                })

                .route("user-routes", r -> {
                    log.info("Registering user-routes");
                    return r.path("/api/v1/users/**")
                            .uri(userBaseUrl);
                })

                // INTAKE SERVICE ROUTES
                .route("trader-requests-routes", r -> {
                    log.info("Registering trader-requests-routes");
                    return r.path("/api/v1/trader-requests/**")
                            .uri(intakeBaseUrl);
                })

                .route("agent-requests-routes", r -> {
                    log.info("Registering agent-requests-routes");
                    return r.path("/api/v1/agent-requests/**")
                            .uri(intakeBaseUrl);
                })

                .route("gradings-routes", r -> {
                    log.info("Registering gradings-routes");
                    return r.path("/api/v1/gradings/**")
                            .uri(intakeBaseUrl);
                })

                .build();

        log.info("All routes registered successfully!");
        return locator;
    }
}
