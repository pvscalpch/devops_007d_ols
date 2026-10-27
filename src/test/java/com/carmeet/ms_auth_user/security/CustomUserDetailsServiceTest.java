package com.carmeet.ms_auth_user.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.carmeet.ms_auth_user.model.Usuario;
import com.carmeet.ms_auth_user.repository.UsuarioRepository;

/**
 * Pruebas unitarias de la carga de usuarios para Spring Security.
 *
 * Es el punto donde el rol almacenado en la base se convierte en la
 * autoridad que decide a que endpoints puede entrar cada persona.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository repo;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("Carga el usuario con su clave cifrada y su rol como autoridad")
    void cargaUsuarioConSuRol() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cristian");
        usuario.setPassword("clave-cifrada");
        usuario.setRole("ROLE_ADMIN");

        when(repo.findByUsername("cristian")).thenReturn(Optional.of(usuario));

        UserDetails detalles = userDetailsService.loadUserByUsername("cristian");

        assertThat(detalles.getUsername()).isEqualTo("cristian");
        assertThat(detalles.getPassword()).isEqualTo("clave-cifrada");
        assertThat(detalles.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Un usuario inexistente lanza UsernameNotFoundException")
    void fallaConUsuarioInexistente() {
        when(repo.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("fantasma"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("fantasma");
    }
}
