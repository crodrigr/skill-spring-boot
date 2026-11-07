package com.academy.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token que el cliente debe enviar en cada petición")
public record LoginResponse(

        @Schema(description = "JWT firmado", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,

        @Schema(description = "Tipo de token: se envía como `Authorization: Bearer <token>`", example = "Bearer")
        String tokenType,

        @Schema(description = "Segundos de vigencia del token", example = "3600")
        long expiresIn
) {
}
