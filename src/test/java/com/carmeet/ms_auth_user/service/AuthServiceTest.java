package com.carmeet.ms_auth_user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.carmeet.ms_auth_user.dto.AuthResponse;
import com.carmeet.ms_auth_user.dto.LoginRequest;
import com.carmeet.ms_auth_user.dto.RegisterRequest;
import com.carmeet.ms_auth_user.model.Usuario;
import com.carmeet.ms_auth_user.repository.RefreshTokenRepository;
import com.carmeet.ms_auth_user.repository.UsuarioRepository;
import com.carmeet.ms_auth_user.security.JwtUtil;

/**
 * Pruebas unitarias del servicio de autenticacion.
 *
 * Todas las dependencias se simulan con Mockito, de modo que las
 * pruebas no necesitan base de datos ni contexto de Spring y se
 * ejecutan en milisegundos dentro del pipeline.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private RefreshTokenRepository refreshRepo;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest solicitudDeRegistro(String usuario, String clave) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(usuario);
        req.setPassword(clave);
        return req;
    }

    private LoginRequest solicitudDeLogin(String usuario, String clave) {
        LoginRequest req = new LoginRequest();
        req.setUsername(usuario);
        req.setPassword(clave);
        return req;
    }

    private Usuario usuarioExistente(String nombre, String rol) {
        Usuario user = new Usuario();
        user.setUsername(nombre);
        user.setPassword("hash-guardado");
        user.setRole(rol);
        return user;
    }

    @Test
    @DisplayName("No permite registrar un usuario que ya existe")
    void rechazaRegistroDuplicado() {
        when(usuarioRepo.findByUsername("cristian"))
                .thenReturn(Optional.of(usuarioExistente("cristian", "ROLE_USER")));

        assertThatThrownBy(() -> authService.register(solicitudDeRegistro("cristian", "secreta")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya existe");

        // El usuario duplicado nunca debe llegar a la base de datos
        verify(usuarioRepo, never()).save(any());
    }

    @Test
    @DisplayName("Al registrar guarda la clave cifrada y asigna ROLE_USER")
    void registraConClaveCifradaYRolPorDefecto() {
        when(usuarioRepo.findByUsername("nuevo")).thenReturn(Optional.empty());
        when(encoder.encode("secreta")).thenReturn("clave-cifrada");
        when(jwtUtil.generarToken("nuevo", "ROLE_USER")).thenReturn("token-de-acceso");
        when(jwtUtil.generarRefreshToken("nuevo")).thenReturn("token-de-refresco");

        AuthResponse respuesta = authService.register(solicitudDeRegistro("nuevo", "secreta"));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepo).save(guardado.capture());

        assertThat(guardado.getValue().getPassword())
                .as("la clave nunca debe guardarse en texto plano")
                .isEqualTo("clave-cifrada")
                .isNotEqualTo("secreta");
        assertThat(guardado.getValue().getRole()).isEqualTo("ROLE_USER");

        assertThat(respuesta.getAccessToken()).isEqualTo("token-de-acceso");
        assertThat(respuesta.getRefreshToken()).isEqualTo("token-de-refresco");
        assertThat(respuesta.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("Un login con credenciales invalidas no emite ningun token")
    void rechazaLoginConCredencialesInvalidas() {
        when(authManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales incorrectas"));

        assertThatThrownBy(() -> authService.login(solicitudDeLogin("cristian", "equivocada")))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtUtil, never()).generarToken(anyString(), anyString());
        verify(jwtUtil, never()).generarRefreshToken(anyString());
    }

    @Test
    @DisplayName("Un login correcto devuelve los tokens y el rol del usuario")
    void emiteTokensEnLoginCorrecto() {
        when(usuarioRepo.findByUsername("admin"))
                .thenReturn(Optional.of(usuarioExistente("admin", "ROLE_ADMIN")));
        when(jwtUtil.generarToken("admin", "ROLE_ADMIN")).thenReturn("token-de-acceso");
        when(jwtUtil.generarRefreshToken("admin")).thenReturn("token-de-refresco");

        AuthResponse respuesta = authService.login(solicitudDeLogin("admin", "correcta"));

        assertThat(respuesta.getAccessToken()).isEqualTo("token-de-acceso");
        assertThat(respuesta.getRole()).isEqualTo("ROLE_ADMIN");
        verify(refreshRepo).save(any());
    }

    @Test
    @DisplayName("Promover a administrador cambia el rol del usuario")
    void promueveUsuarioAAdministrador() {
        Usuario user = usuarioExistente("cristian", "ROLE_USER");
        when(usuarioRepo.findByUsername("cristian")).thenReturn(Optional.of(user));
        when(usuarioRepo.save(any())).thenReturn(user);

        authService.promoverAAdmin("cristian");

        assertThat(user.getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Promover a un usuario inexistente falla")
    void fallaAlPromoverUsuarioInexistente() {
        when(usuarioRepo.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.promoverAAdmin("fantasma"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }
}
