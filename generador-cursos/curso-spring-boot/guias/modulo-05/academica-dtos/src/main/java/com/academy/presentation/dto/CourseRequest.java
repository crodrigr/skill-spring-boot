package com.academy.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CourseRequest(

        @NotBlank
        @Size(min = 2, max = 100)
        String name,

        @Positive
        double price,

        @Min(1)
        int maxCapacity
) {
}
