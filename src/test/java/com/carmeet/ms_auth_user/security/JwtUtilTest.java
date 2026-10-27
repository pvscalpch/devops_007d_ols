package com.carmeet.ms_auth_user.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de la emision y validacion de tokens JWT.
 *
 * Es el caso mas critico del microservicio: si un token falsificado
 * se aceptara, cualquiera podria suplantar a un usuario del sistema.
 * No levanta el contexto de Spring ni necesita base de datos.
 */
class JwtUtilTest {

    /** HS256 exige 256 bits, es decir 32 caracteres como minimo. */
    private static final String SECRETO =
            "clave-de-pruebas-suficientemente-larga-para-hs256-1234567890";

    private JwtUtil jwtUtil;

    @BeforeEach
    void prepararEmisor() {
        jwtUtil = new JwtUtil(SECRETO);
    }

    @Test
    @DisplayName("Un token recien emitido es valido y conserva usuario y rol")
    void emiteTokenValidoConUsuarioYRol() {
        String token = jwtUtil.generarToken("cristian", "ROLE_USER");

        assertThat(jwtUtil.esValido(token)).isTrue();
        assertThat(jwtUtil.obtenerUsuario(token)).isEqualTo("cristian");
        assertThat(jwtUtil.obtenerRole(token)).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("Un token con la firma alterada se rechaza")
    void rechazaTokenAdulterado() {
        String token = jwtUtil.generarToken("cristian", "ROLE_USER");
        String adulterado = token.substring(0, token.length() - 4) + "abcd";

        assertThat(jwtUtil.esValido(adulterado)).isFalse();
    }

    @Test
    @DisplayName("Un token firmado con otra clave se rechaza")
    void rechazaTokenDeOtroEmisor() {
        JwtUtil otroEmisor =
                new JwtUtil("otra-clave-distinta-pero-igual-de-larga-0987654321");
        String tokenAjeno = otroEmisor.generarToken("intruso", "ROLE_ADMIN");

        assertThat(jwtUtil.esValido(tokenAjeno)).isFalse();
    }

    @Test
    @DisplayName("Distingue un refresh token de un token de acceso")
    void distingueRefreshDeTokenDeAcceso() {
        String refresh = jwtUtil.generarRefreshToken("cristian");
        String acceso = jwtUtil.generarToken("cristian", "ROLE_USER");

        assertThat(jwtUtil.esRefreshToken(refresh)).isTrue();
        assertThat(jwtUtil.esRefreshToken(acceso)).isFalse();
    }

    @Test
    @DisplayName("Una cadena que no es un JWT se rechaza sin lanzar excepcion")
    void rechazaCadenaQueNoEsUnToken() {
        assertThat(jwtUtil.esValido("esto-no-es-un-token")).isFalse();
        assertThat(jwtUtil.esValido("")).isFalse();
    }
}
