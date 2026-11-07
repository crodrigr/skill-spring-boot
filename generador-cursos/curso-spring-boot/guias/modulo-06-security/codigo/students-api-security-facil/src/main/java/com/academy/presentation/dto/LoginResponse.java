package com.academy.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "El token que se envía en cada petición")
public record LoginResponse(

        @Schema(description = "El token (JWT)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,

        @Schema(description = "Cómo se envía: `Authorization: Bearer <token>`", example = "Bearer")
        String tokenType,

        @Schema(description = "Segundos que dura el token", example = "3600")
        long expiresIn
) {
}
