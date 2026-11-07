package com.academy.presentation.dto;

import com.academy.persistence.entity.StudentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudentRequest(

        @NotBlank
        @Size(min = 2, max = 100)
        String name,

        @Min(16)
        @Max(99)
        int age,

        @NotBlank
        @Email
        String email,

        @NotNull
        StudentStatus status
) {
}
