package com.academy.business.impl;

import com.academy.business.service.CourseService;
import com.academy.persistence.entity.CourseEntity;
import com.academy.persistence.repository.CourseJpaRepository;
import com.academy.persistence.repository.EnrollmentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseJpaRepository courses;
    private final EnrollmentJpaRepository enrollments;

    @Override
    @Transactional(readOnly = true)
    public List<CourseEntity> findAll() {
        return courses.findAll();
    }

    @Override
    @Transactional
    public Optional<CourseEntity> create(CourseEntity course) {
        if (course.getPrice() <= 0 || course.getMaxCapacity() <= 0) {
            return Optional.empty();
        }
        return Optional.of(courses.save(course));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> countEnrolled(Long courseId) {
        if (!courses.existsById(courseId)) {
            return Optional.empty();
        }
        return Optional.of(enrollments.countByCourseId(courseId));
    }
}
