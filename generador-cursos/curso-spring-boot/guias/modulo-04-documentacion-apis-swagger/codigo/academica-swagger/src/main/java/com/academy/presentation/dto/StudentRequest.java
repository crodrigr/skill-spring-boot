package com.academy.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos que el cliente envía para registrar un estudiante")
public record StudentRequest(

        @Schema(description = "Nombre completo", example = "Ana Torres")
        @NotBlank
        @Size(min = 2, max = 100)
        String name,

        @Schema(description = "Edad en años", example = "22")
        @Min(16)
        @Max(99)
        int age,

        @Schema(description = "Correo electrónico, único entre estudiantes", example = "ana@mail.com")
        @NotBlank
        @Email
        String email
) {
}
