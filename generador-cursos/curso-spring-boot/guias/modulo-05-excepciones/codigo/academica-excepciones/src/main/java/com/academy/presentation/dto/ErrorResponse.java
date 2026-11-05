package com.academy.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Schema(description = "Cuerpo de toda respuesta de error de la API")
public record ErrorResponse(

        @Schema(description = "Momento en que ocurrió el error, en UTC (ISO-8601)", type = "string", example = "2026-11-01T10:15:30.123Z")
        Instant timestamp,

        @Schema(description = "Código de estado HTTP", example = "404")
        int status,

        @Schema(description = "Nombre del estado HTTP", example = "Not Found")
        String error,

        @Schema(description = "Qué pasó, en lenguaje claro", example = "No existe un estudiante con id 999")
        String message,

        @Schema(description = "Ruta de la petición que falló", example = "/api/v1/students/999")
        String path,

        @Schema(description = "Detalle campo por campo (solo en errores de validación)")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<FieldIssue> details
) {

    @Schema(description = "Un campo inválido y la razón")
    public record FieldIssue(

            @Schema(description = "Nombre del campo", example = "email")
            String field,

            @Schema(description = "Por qué es inválido", example = "debe ser una dirección de correo electrónico bien formada")
            String message
    ) {
    }

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(Instant.now().truncatedTo(ChronoUnit.MILLIS), status.value(), status.getReasonPhrase(), message, path, List.of());
    }

    public static ErrorResponse of(HttpStatus status, String message, String path, List<FieldIssue> details) {
        return new ErrorResponse(Instant.now().truncatedTo(ChronoUnit.MILLIS), status.value(), status.getReasonPhrase(), message, path, details);
    }
}
