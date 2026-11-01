package com.academy.presentation.mapper;

import com.academy.persistence.entity.StudentEntity;
import com.academy.presentation.dto.PageResponse;
import com.academy.presentation.dto.StudentRequest;
import com.academy.presentation.dto.StudentResponse;
import org.springframework.data.domain.Page;

public final class StudentMapper {

    private StudentMapper() {
    }

    public static StudentEntity toEntity(StudentRequest request) {
        return new StudentEntity(null, request.name(), request.age(), request.email(), request.status());
    }

    public static StudentResponse toResponse(StudentEntity entity) {
        return new StudentResponse(entity.getId(), entity.getName(), entity.getAge(),
                entity.getEmail(), entity.getStatus());
    }

    public static PageResponse<StudentResponse> toPageResponse(Page<StudentEntity> page) {
        return new PageResponse<>(
                page.getContent().stream().map(StudentMapper::toResponse).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
