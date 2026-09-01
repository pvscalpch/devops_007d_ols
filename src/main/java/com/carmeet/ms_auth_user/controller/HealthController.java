package com.carmeet.ms_auth_user.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint publico de estado del microservicio.
 * Permite que el pipeline y otros servicios verifiquen que ms-auth-user
 * esta arriba sin necesidad de autenticarse.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public Map<String, Object> estado() {
        return Map.of(
                "service", "ms-auth-user",
                "status", "UP",
                "timestamp", Instant.now().toString());
    }
}
