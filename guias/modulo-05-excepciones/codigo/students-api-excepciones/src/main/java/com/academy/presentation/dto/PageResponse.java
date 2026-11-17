package com.academy.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Una página de resultados")
public record PageResponse<T>(

        @Schema(description = "Elementos de la página")
        List<T> content,

        @Schema(description = "Número de página, desde 0", example = "0")
        int page,

        @Schema(description = "Elementos por página (máximo 50)", example = "10")
        int size,

        @Schema(description = "Total de elementos que cumplen el filtro", example = "3")
        long totalElements,

        @Schema(description = "Total de páginas", example = "1")
        int totalPages
) {
}
