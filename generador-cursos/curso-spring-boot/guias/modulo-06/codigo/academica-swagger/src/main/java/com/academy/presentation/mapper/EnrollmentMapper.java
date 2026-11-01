package com.academy.presentation.mapper;

import com.academy.persistence.entity.EnrollmentEntity;
import com.academy.presentation.dto.EnrollmentResponse;

public final class EnrollmentMapper {

    private EnrollmentMapper() {
    }

    public static EnrollmentResponse toResponse(EnrollmentEntity entity) {
        return new EnrollmentResponse(
                entity.getId(),
                entity.getStudent().getId(), entity.getStudent().getName(),
                entity.getCourse().getId(), entity.getCourse().getName());
    }
}
