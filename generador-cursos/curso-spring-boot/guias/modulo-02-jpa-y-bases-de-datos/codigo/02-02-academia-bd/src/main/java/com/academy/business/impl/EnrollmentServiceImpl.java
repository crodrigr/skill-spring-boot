package com.academy.business.impl;

import com.academy.business.service.EnrollmentService;
import com.academy.persistence.entity.CourseEntity;
import com.academy.persistence.entity.EnrollmentEntity;
import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.repository.CourseJpaRepository;
import com.academy.persistence.repository.EnrollmentJpaRepository;
import com.academy.persistence.repository.StudentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final StudentJpaRepository students;
    private final CourseJpaRepository courses;
    private final EnrollmentJpaRepository enrollments;

    @Override
    @Transactional
    public Optional<EnrollmentEntity> enroll(Long studentId, Long courseId) {
        Optional<StudentEntity> student = students.findById(studentId);
        Optional<CourseEntity> course = courses.findById(courseId);

        if (student.isEmpty() || course.isEmpty()) {
            return Optional.empty();
        }
        if (enrollments.existsByStudentIdAndCourseId(studentId, courseId)) {
            return Optional.empty();
        }
        if (enrollments.countByCourseId(courseId) >= course.get().getMaxCapacity()) {
            return Optional.empty();
        }
        return Optional.of(enrollments.save(new EnrollmentEntity(student.get(), course.get())));
    }
}
