package com.carmeet.ms_auth_user.controller;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class HealthControllerTest {
    @Test
    void returnsServiceUpStatus() {
        Map<String, Object> result = new HealthController().estado();
        assertEquals("ms-auth-user", result.get("service"));
        assertEquals("UP", result.get("status"));
        assertNotNull(result.get("timestamp"));
    }
}
