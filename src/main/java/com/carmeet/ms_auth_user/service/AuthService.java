package com.carmeet.ms_auth_user.service;

import java.util.Date;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.carmeet.ms_auth_user.dto.AuthResponse;
import com.carmeet.ms_auth_user.dto.LoginRequest;
import com.carmeet.ms_auth_user.dto.RegisterRequest;
import com.carmeet.ms_auth_user.model.RefreshToken;
import com.carmeet.ms_auth_user.model.Usuario;
import com.carmeet.ms_auth_user.repository.RefreshTokenRepository;
import com.carmeet.ms_auth_user.repository.UsuarioRepository;
import com.carmeet.ms_auth_user.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    // REGISTER - asigna ROLE_USER por defecto
    public AuthResponse register(RegisterRequest req) {

        if (usuarioRepo.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("El usuario '" + req.getUsername() + "' ya existe");
        }

        Usuario user = new Usuario();
        user.setUsername(req.getUsername());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole("ROLE_USER");

        usuarioRepo.save(user);

        String access = jwtUtil.generarToken(user.getUsername(), user.getRole());
        String refresh = generarRefreshToken(user.getUsername());

        return new AuthResponse(access, refresh, user.getRole());
    }

    // LOGIN
    public AuthResponse login(LoginRequest req) {

        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        Usuario user = usuarioRepo.findByUsername(req.getUsername()).get();

        String access = jwtUtil.generarToken(user.getUsername(), user.getRole());
        String refresh = generarRefreshToken(user.getUsername());

        return new AuthResponse(access, refresh, user.getRole());
    }

    // REFRESH
    public AuthResponse refresh(String refreshToken) {

        RefreshToken token = refreshRepo.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh inválido"));

        if (!jwtUtil.esValido(refreshToken) || !jwtUtil.esRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh token inválido");
        }

        Usuario user = usuarioRepo.findByUsername(token.getUsername()).get();

        String newAccess = jwtUtil.generarToken(user.getUsername(), user.getRole());

        return new AuthResponse(newAccess, refreshToken, user.getRole());
    }

    // PROMOVER A ADMIN
    public Usuario promoverAAdmin(String username) {
        Usuario user = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        user.setRole("ROLE_ADMIN");
        return usuarioRepo.save(user);
    }

    // DEGRADAR A USER
    public Usuario degradarAUser(String username) {
        Usuario user = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        user.setRole("ROLE_USER");
        return usuarioRepo.save(user);
    }

    // OBTENER USUARIO POR USERNAME
    public Usuario obtenerPorUsername(String username) {
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    private String generarRefreshToken(String username) {

        String token = jwtUtil.generarRefreshToken(username);

        RefreshToken rt = new RefreshToken();
        rt.setToken(token);
        rt.setUsername(username);
        rt.setExpiryDate(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24));

        refreshRepo.save(rt);

        return token;
    }
}
