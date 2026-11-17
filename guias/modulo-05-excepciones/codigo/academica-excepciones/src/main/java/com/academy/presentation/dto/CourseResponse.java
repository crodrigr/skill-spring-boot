package com.academy.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Curso tal como lo ve el cliente")
public record CourseResponse(

        @Schema(description = "Identificador generado por la base", example = "1")
        Long id,

        @Schema(description = "Nombre del curso", example = "Java desde cero")
        String name,

        @Schema(description = "Precio en pesos", example = "150000")
        double price,

        @Schema(description = "Cupo máximo de matriculados", example = "30")
        int maxCapacity
) {
}
