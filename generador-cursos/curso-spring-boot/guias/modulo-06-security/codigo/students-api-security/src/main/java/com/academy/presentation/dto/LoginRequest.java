package com.academy.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para iniciar sesión")
public record LoginRequest(

        @Schema(description = "Nombre de usuario", example = "admin")
        @NotBlank(message = "es obligatorio")
        String username,

        @Schema(description = "Contraseña", example = "admin123")
        @NotBlank(message = "es obligatoria")
        String password
) {
}
