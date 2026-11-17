package com.academy.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Quién es el usuario autenticado")
public record MeResponse(

        @Schema(description = "Nombre de usuario", example = "ana")
        String username,

        @Schema(description = "Rol", example = "USER")
        String role
) {
}
