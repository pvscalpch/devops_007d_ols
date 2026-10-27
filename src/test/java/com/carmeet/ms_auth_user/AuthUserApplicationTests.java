package com.carmeet.ms_auth_user;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de INTEGRACION: levanta el contexto completo de Spring y
 * requiere una base de datos MySQL disponible.
 *
 * Esta marcada con @Tag("integration") para que el job de pruebas
 * unitarias del pipeline la excluya: ese job corre sin base de datos
 * y debe ser rapido. Se ejecuta en el job `integration-tests`, que si
 * dispone de un MySQL efimero.
 */
@Tag("integration")
@SpringBootTest
class AuthUserApplicationTests {

	@Test
	void contextLoads() {
	}

}
