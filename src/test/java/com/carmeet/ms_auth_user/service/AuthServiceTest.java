package com.carmeet.ms_auth_user.service;

import com.carmeet.ms_auth_user.dto.AuthResponse;
import com.carmeet.ms_auth_user.dto.RegisterRequest;
import com.carmeet.ms_auth_user.model.RefreshToken;
import com.carmeet.ms_auth_user.model.Usuario;
import com.carmeet.ms_auth_user.repository.RefreshTokenRepository;
import com.carmeet.ms_auth_user.repository.UsuarioRepository;
import com.carmeet.ms_auth_user.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UsuarioRepository usuarioRepo;
    @Mock RefreshTokenRepository refreshRepo;
    @Mock PasswordEncoder encoder;
    @Mock AuthenticationManager authManager;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService service;

    @Test
    void registerCreatesUserAndReturnsTokens() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("pascal_test");
        req.setPassword("password123");
        when(usuarioRepo.findByUsername("pascal_test")).thenReturn(Optional.empty());
        when(encoder.encode("password123")).thenReturn("encoded-password");
        when(jwtUtil.generarToken("pascal_test", "ROLE_USER")).thenReturn("access-token");
        when(jwtUtil.generarRefreshToken("pascal_test")).thenReturn("refresh-token");
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(refreshRepo.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse result = service.register(req);

        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals("ROLE_USER", result.getRole());
        verify(usuarioRepo).save(any(Usuario.class));
        verify(refreshRepo).save(any(RefreshToken.class));
    }

    @Test
    void registerRejectsExistingUsername() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("existente");
        req.setPassword("password123");
        when(usuarioRepo.findByUsername("existente")).thenReturn(Optional.of(new Usuario()));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.register(req));
        assertTrue(ex.getMessage().contains("ya existe"));
        verify(usuarioRepo, never()).save(any());
    }
}
