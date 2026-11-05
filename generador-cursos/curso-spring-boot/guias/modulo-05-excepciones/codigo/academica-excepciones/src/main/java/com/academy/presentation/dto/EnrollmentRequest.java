package com.academy.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos que el cliente envía para matricular a un estudiante en un curso")
public record EnrollmentRequest(

        @Schema(description = "Identificador del estudiante", example = "1")
        @NotNull(message = "es obligatorio")
        Long studentId,

        @Schema(description = "Identificador del curso", example = "1")
        @NotNull(message = "es obligatorio")
        Long courseId
) {
}
