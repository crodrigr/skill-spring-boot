package com.academy.business.service;

import com.academy.persistence.entity.CourseEntity;

import java.util.List;
import java.util.Optional;

public interface CourseService {

    List<CourseEntity> findAll();

    Optional<CourseEntity> create(CourseEntity course);

    Optional<Long> countEnrolled(Long courseId);
}
