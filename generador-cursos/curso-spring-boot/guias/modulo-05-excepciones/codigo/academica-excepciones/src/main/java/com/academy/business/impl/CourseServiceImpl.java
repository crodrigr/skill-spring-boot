package com.academy.business.impl;

import com.academy.business.exception.CourseNotFoundException;
import com.academy.business.service.CourseService;
import com.academy.persistence.entity.CourseEntity;
import com.academy.persistence.repository.CourseJpaRepository;
import com.academy.persistence.repository.EnrollmentJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseJpaRepository courses;
    private final EnrollmentJpaRepository enrollments;

    public CourseServiceImpl(CourseJpaRepository courses, EnrollmentJpaRepository enrollments) {
        this.courses = courses;
        this.enrollments = enrollments;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseEntity> findAll(Pageable pageable) {
        return courses.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseEntity findById(Long id) {
        return courses.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    @Override
    @Transactional
    public CourseEntity create(CourseEntity course) {
        return courses.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public long countEnrolled(Long courseId) {
        if (!courses.existsById(courseId)) {
            throw new CourseNotFoundException(courseId);
        }
        return enrollments.countByCourseId(courseId);
    }
}
