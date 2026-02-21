package com.takeam.gateway.controller;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/internal")
public class WakeController {

    private final RestTemplate restTemplate;

    public WakeController(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(90)) // allow cold start
                .build();
    }

    @GetMapping("/wake-services")
    public ResponseEntity<?> wakeServices() {

        List<String> services = List.of(
                "https://takeam-intake-service-latest.onrender.com/health",
                "https://takeam-marketplace-service-latest.onrender.com/health",
                "https://takeam-user-service.onrender.com/actuator/health"
        );

        // Thread-safe list because we’re using parallel stream
        List<String> results = new CopyOnWriteArrayList<>();

        services.parallelStream().forEach(url -> {
            try {
                long start = System.currentTimeMillis();

                ResponseEntity<String> response =
                        restTemplate.getForEntity(url, String.class);

                long duration = System.currentTimeMillis() - start;

                results.add(
                        url + " → " +
                                response.getStatusCode() +
                                " (" + duration + "ms)"
                );

            } catch (Exception e) {
                results.add(
                        url + " → ERROR: " + e.getMessage()
                );
            }
        });

        return ResponseEntity.ok(results);
    }
}