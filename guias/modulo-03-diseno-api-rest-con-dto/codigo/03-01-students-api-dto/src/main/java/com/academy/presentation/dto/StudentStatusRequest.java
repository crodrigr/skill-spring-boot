package com.academy.presentation.dto;

import com.academy.persistence.entity.StudentStatus;
import jakarta.validation.constraints.NotNull;

public record StudentStatusRequest(@NotNull StudentStatus status) {
}
