package com.academy.presentation.mapper;

import com.academy.persistence.entity.CourseEntity;
import com.academy.presentation.dto.CourseRequest;
import com.academy.presentation.dto.CourseResponse;
import com.academy.presentation.dto.PageResponse;
import org.springframework.data.domain.Page;

public final class CourseMapper {

    private CourseMapper() {
    }

    public static CourseEntity toEntity(CourseRequest request) {
        return new CourseEntity(request.name(), request.price(), request.maxCapacity());
    }

    public static CourseResponse toResponse(CourseEntity entity) {
        return new CourseResponse(entity.getId(), entity.getName(), entity.getPrice(), entity.getMaxCapacity());
    }

    public static PageResponse<CourseResponse> toPageResponse(Page<CourseEntity> page) {
        return new PageResponse<>(
                page.getContent().stream().map(CourseMapper::toResponse).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
