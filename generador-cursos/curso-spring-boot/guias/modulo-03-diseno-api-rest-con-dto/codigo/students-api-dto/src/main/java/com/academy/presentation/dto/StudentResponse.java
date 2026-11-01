package com.academy.presentation.dto;

import com.academy.persistence.entity.StudentStatus;

public record StudentResponse(Long id, String name, int age, String email, StudentStatus status) {
}
