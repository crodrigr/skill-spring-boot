package com.academy.presentation.dto;

import com.academy.persistence.entity.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estudiante tal como lo ve el cliente")
public record StudentResponse(

        @Schema(description = "Identificador generado por la base", example = "1")
        Long id,

        @Schema(description = "Nombre completo", example = "Ana Torres")
        String name,

        @Schema(description = "Edad en años", example = "22")
        int age,

        @Schema(description = "Correo electrónico", example = "ana@mail.com")
        String email,

        @Schema(description = "Estado académico", example = "ACTIVE")
        StudentStatus status
) {
}
