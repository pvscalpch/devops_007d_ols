package com.carmeet.ms_auth_user.security;

import com.carmeet.ms_auth_user.model.Usuario;
import com.carmeet.ms_auth_user.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @Mock UsuarioRepository repo;
    @InjectMocks CustomUserDetailsService service;

    @Test
    void loadsExistingUserAndAuthority() {
        Usuario usuario = new Usuario();
        usuario.setUsername("usuario1");
        usuario.setPassword("hash");
        usuario.setRole("ROLE_ADMIN");
        when(repo.findByUsername("usuario1")).thenReturn(Optional.of(usuario));

        UserDetails details = service.loadUserByUsername("usuario1");

        assertEquals("usuario1", details.getUsername());
        assertEquals("hash", details.getPassword());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void throwsWhenUserDoesNotExist() {
        when(repo.findByUsername("desconocido")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("desconocido"));
    }
}
