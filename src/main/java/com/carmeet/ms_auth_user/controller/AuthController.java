package com.carmeet.ms_auth_user.controller;

import com.carmeet.ms_auth_user.dto.*;
import com.carmeet.ms_auth_user.model.Usuario;
import com.carmeet.ms_auth_user.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService service;

    // POST /auth/register  (publico)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        log.info("POST /auth/register - usuario: {}", req.getUsername());

        AuthResponse res = service.register(req);

        return ResponseEntity.status(201).body(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Usuario registrado con rol: " + res.getRole())
                        .data(res)
                        .build()
        );
    }

    // POST /auth/login  (publico)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        log.info("POST /auth/login - usuario: {}", req.getUsername());

        AuthResponse res = service.login(req);

        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Login exitoso - rol: " + res.getRole())
                        .data(res)
                        .build()
        );
    }

    // POST /auth/refresh  (publico)
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody RefreshRequest req) {

        AuthResponse res = service.refresh(req.getRefreshToken());

        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Token renovado")
                        .data(res)
                        .build()
        );
    }

    // GET /auth/me  (requiere token JWT cualquier rol)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, String>>> me(Authentication auth) {

        String username = auth.getName();
        Usuario user = service.obtenerPorUsername(username);

        Map<String, String> info = Map.of(
                "username", user.getUsername(),
                "role", user.getRole()
        );

        return ResponseEntity.ok(
                ApiResponse.<Map<String, String>>builder()
                        .success(true)
                        .message("Usuario actual")
                        .data(info)
                        .build()
        );
    }

    // PUT /auth/promote/{username}  (solo ROLE_ADMIN puede promover)
    @PutMapping("/promote/{username}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> promote(@PathVariable String username) {

        Usuario user = service.promoverAAdmin(username);

        Map<String, String> info = Map.of(
                "username", user.getUsername(),
                "role", user.getRole()
        );

        return ResponseEntity.ok(
                ApiResponse.<Map<String, String>>builder()
                        .success(true)
                        .message("Usuario promovido a ADMIN exitosamente")
                        .data(info)
                        .build()
        );
    }

    // PUT /auth/demote/{username}  (solo ROLE_ADMIN puede degradar)
    @PutMapping("/demote/{username}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> demote(@PathVariable String username) {

        Usuario user = service.degradarAUser(username);

        Map<String, String> info = Map.of(
                "username", user.getUsername(),
                "role", user.getRole()
        );

        return ResponseEntity.ok(
                ApiResponse.<Map<String, String>>builder()
                        .success(true)
                        .message("Usuario degradado a USER exitosamente")
                        .data(info)
                        .build()
        );
    }
}
