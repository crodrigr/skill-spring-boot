package com.academy.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cuántos estudiantes tiene matriculados un curso")
public record EnrolledResponse(

        @Schema(description = "Identificador del curso", example = "1")
        Long courseId,

        @Schema(description = "Cantidad de matriculados", example = "2")
        long enrolled
) {
}
