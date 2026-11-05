package com.academy.business.service;

import com.academy.persistence.entity.CourseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {

    Page<CourseEntity> findAll(Pageable pageable);

    CourseEntity findById(Long id);

    CourseEntity create(CourseEntity course);

    long countEnrolled(Long courseId);
}
