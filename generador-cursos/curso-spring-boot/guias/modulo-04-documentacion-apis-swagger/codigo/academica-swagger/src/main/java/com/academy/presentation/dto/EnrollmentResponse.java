package com.academy.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Matrícula tal como la ve el cliente (aplanada)")
public record EnrollmentResponse(

        @Schema(description = "Identificador de la matrícula", example = "1")
        Long id,

        @Schema(description = "Identificador del estudiante", example = "1")
        Long studentId,

        @Schema(description = "Nombre del estudiante", example = "Ana Torres")
        String studentName,

        @Schema(description = "Identificador del curso", example = "1")
        Long courseId,

        @Schema(description = "Nombre del curso", example = "Java desde cero")
        String courseName
) {
}
