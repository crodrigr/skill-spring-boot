package com.academy.presentation.dto;

import com.academy.persistence.entity.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Nuevo estado del estudiante (es lo único que cambia)")
public record StudentStatusRequest(

        @Schema(description = "Estado académico", example = "INACTIVE")
        @NotNull(message = "es obligatorio")
        StudentStatus status
) {
}
