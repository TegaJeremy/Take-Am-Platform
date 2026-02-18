package com.takeam.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/internal")
public class WakeController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/wake-services")
    public ResponseEntity<?> wakeServices() {

        List<String> services = List.of(
                "https://takeam-intake-service-latest.onrender.com/health",
                "https://takeam-marketplace-service-latest.onrender.com/health",
                "https://takeam-user-service.onrender.com/actuator/health"
        );

        List<String> results = new ArrayList<>();

        for (String url : services) {
            try {
                ResponseEntity<String> response =
                        restTemplate.getForEntity(url, String.class);
                results.add(url + " → " + response.getStatusCode());
            } catch (Exception e) {
                results.add(url + " → ERROR");
            }
        }

        return ResponseEntity.ok(results);
    }
}
