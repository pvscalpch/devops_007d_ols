package com.carmeet.ms_auth_user.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesBadCredentialsAsUnauthorized() {
        ResponseEntity<?> response = handler.handleBadCredentials(new BadCredentialsException("bad"));
        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void handlesGenericRuntimeAsBadRequest() {
        ResponseEntity<?> response = handler.handleRuntime(new RuntimeException("error de prueba"));
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
}
