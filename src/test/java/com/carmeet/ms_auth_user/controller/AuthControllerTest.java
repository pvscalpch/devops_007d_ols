package com.carmeet.ms_auth_user.controller;

import com.carmeet.ms_auth_user.dto.AuthResponse;
import com.carmeet.ms_auth_user.dto.RegisterRequest;
import com.carmeet.ms_auth_user.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthControllerTest {
    @Test
    void registerReturnsCreatedResponse() {
        AuthService service = Mockito.mock(AuthService.class);
        AuthController controller = new AuthController(service);
        RegisterRequest request = new RegisterRequest();
        request.setUsername("usuario1");
        request.setPassword("password123");
        when(service.register(any(RegisterRequest.class)))
                .thenReturn(new AuthResponse("access", "refresh", "ROLE_USER"));

        ResponseEntity<?> response = controller.register(request);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
}
