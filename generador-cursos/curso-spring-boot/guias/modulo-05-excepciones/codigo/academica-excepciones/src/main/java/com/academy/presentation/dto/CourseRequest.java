package com.academy.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos que el cliente envía para crear un curso")
public record CourseRequest(

        @Schema(description = "Nombre del curso", example = "Java desde cero")
        @NotBlank(message = "es obligatorio")
        @Size(min = 2, max = 100, message = "debe tener entre 2 y 100 caracteres")
        String name,

        @Schema(description = "Precio en pesos; debe ser mayor que cero", example = "150000")
        @Positive(message = "debe ser mayor que cero")
        double price,

        @Schema(description = "Cupo máximo de matriculados; al menos 1", example = "30")
        @Min(value = 1, message = "debe ser 1 o más")
        int maxCapacity
) {
}
