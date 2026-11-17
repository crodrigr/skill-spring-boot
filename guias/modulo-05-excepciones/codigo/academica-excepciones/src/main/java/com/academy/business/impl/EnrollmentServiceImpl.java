package com.academy.business.impl;

import com.academy.business.exception.AlreadyEnrolledException;
import com.academy.business.exception.CourseFullException;
import com.academy.business.exception.CourseNotFoundException;
import com.academy.business.exception.EnrollmentNotFoundException;
import com.academy.business.exception.StudentNotFoundException;
import com.academy.business.service.EnrollmentService;
import com.academy.persistence.entity.CourseEntity;
import com.academy.persistence.entity.EnrollmentEntity;
import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.repository.CourseJpaRepository;
import com.academy.persistence.repository.EnrollmentJpaRepository;
import com.academy.persistence.repository.StudentJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final StudentJpaRepository students;
    private final CourseJpaRepository courses;
    private final EnrollmentJpaRepository enrollments;

    public EnrollmentServiceImpl(StudentJpaRepository students, CourseJpaRepository courses,
                                 EnrollmentJpaRepository enrollments) {
        this.students = students;
        this.courses = courses;
        this.enrollments = enrollments;
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentEntity findById(Long id) {
        return enrollments.findById(id)
                .orElseThrow(() -> new EnrollmentNotFoundException(id));
    }

    @Override
    @Transactional
    public EnrollmentEntity enroll(Long studentId, Long courseId) {
        StudentEntity student = students.findById(studentId)
                .orElseThrow(() -> StudentNotFoundException.byId(studentId));
        CourseEntity course = courses.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        if (enrollments.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new AlreadyEnrolledException(studentId, courseId);
        }
        if (enrollments.countByCourseId(courseId) >= course.getMaxCapacity()) {
            throw new CourseFullException(courseId, course.getMaxCapacity());
        }
        return enrollments.save(new EnrollmentEntity(student, course));
    }
}
