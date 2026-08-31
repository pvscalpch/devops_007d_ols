package com.carmeet.ms_auth_user.exception;

import com.carmeet.ms_auth_user.dto.ApiResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 — Validacion de campos (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .success(false)
                        .message("Validacion fallida")
                        .error(errores)
                        .build());
    }

    // 401 — Credenciales invalidas
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(401).body(
                ApiResponse.builder()
                        .success(false)
                        .message("Credenciales invalidas")
                        .build());
    }

    // 403 — Sin permiso
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handle403(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(
                ApiResponse.builder()
                        .success(false)
                        .message("Acceso denegado")
                        .build());
    }

    // 409 — Conflicto: registro duplicado (ej. username ya existe)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConflict(DataIntegrityViolationException ex) {
        String msg = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        // Simplificar el mensaje para el cliente
        if (msg != null && msg.contains("Duplicate entry")) {
            String campo = msg.replaceAll(".*Duplicate entry '(.+?)' for key '(.+?)'.*", "El valor '$1' ya existe en '$2'");
            msg = campo;
        }
        return ResponseEntity.status(409).body(
                ApiResponse.builder()
                        .success(false)
                        .message(msg)
                        .build());
    }

    // 404 — Recurso no encontrado (NoSuchElementException, entidad no encontrada)
    @ExceptionHandler({
        java.util.NoSuchElementException.class,
        jakarta.persistence.EntityNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleNotFound(Exception ex) {
        return ResponseEntity.status(404).body(
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage() != null ? ex.getMessage() : "Recurso no encontrado")
                        .build());
    }

    // 400 — RuntimeException generica de negocio (parametros invalidos, etc.)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(400).body(
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    // 500 — Error inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(500).body(
                ApiResponse.builder()
                        .success(false)
                        .message("Error interno del servidor")
                        .build());
    }
}
