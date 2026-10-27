package com.carmeet.ms_auth_user.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("clave-de-prueba-con-mas-de-32-caracteres-123456");
    }

    @Test
    void accessTokenContainsUsernameAndRole() {
        String token = jwtUtil.generarToken("usuario1", "ROLE_USER");
        assertTrue(jwtUtil.esValido(token));
        assertEquals("usuario1", jwtUtil.obtenerUsuario(token));
        assertEquals("ROLE_USER", jwtUtil.obtenerRole(token));
        assertFalse(jwtUtil.esRefreshToken(token));
    }

    @Test
    void refreshTokenIsRecognized() {
        String token = jwtUtil.generarRefreshToken("usuario1");
        assertTrue(jwtUtil.esValido(token));
        assertEquals("usuario1", jwtUtil.obtenerUsuario(token));
        assertTrue(jwtUtil.esRefreshToken(token));
    }

    @Test
    void invalidTokenReturnsFalse() {
        assertFalse(jwtUtil.esValido("token-invalido"));
    }
}
