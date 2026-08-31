package com.carmeet.ms_auth_user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username es obligatorio")
    private String username;

    @NotBlank(message = "Password es obligatorio")
    private String password;
}
