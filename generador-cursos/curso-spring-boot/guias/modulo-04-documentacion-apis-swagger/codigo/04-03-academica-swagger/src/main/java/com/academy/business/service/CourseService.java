package com.academy.business.service;

import com.academy.persistence.entity.CourseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CourseService {

    Page<CourseEntity> findAll(Pageable pageable);

    Optional<CourseEntity> findById(Long id);

    CourseEntity create(CourseEntity course);

    ServiceResult<Long> countEnrolled(Long courseId);
}
