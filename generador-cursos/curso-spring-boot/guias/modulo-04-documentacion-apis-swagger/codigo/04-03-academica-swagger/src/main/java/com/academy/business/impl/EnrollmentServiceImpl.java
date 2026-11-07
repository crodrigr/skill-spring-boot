package com.academy.business.impl;

import com.academy.business.service.EnrollmentService;
import com.academy.business.service.ServiceResult;
import com.academy.persistence.entity.CourseEntity;
import com.academy.persistence.entity.EnrollmentEntity;
import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.repository.CourseJpaRepository;
import com.academy.persistence.repository.EnrollmentJpaRepository;
import com.academy.persistence.repository.StudentJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    public Optional<EnrollmentEntity> findById(Long id) {
        return enrollments.findById(id);
    }

    @Override
    @Transactional
    public ServiceResult<EnrollmentEntity> enroll(Long studentId, Long courseId) {
        Optional<StudentEntity> student = students.findById(studentId);
        Optional<CourseEntity> course = courses.findById(courseId);
        if (student.isEmpty() || course.isEmpty()) {
            return ServiceResult.notFound();
        }
        if (enrollments.existsByStudentIdAndCourseId(studentId, courseId)) {
            return ServiceResult.conflict();
        }
        if (enrollments.countByCourseId(courseId) >= course.get().getMaxCapacity()) {
            return ServiceResult.conflict();
        }
        return ServiceResult.ok(enrollments.save(new EnrollmentEntity(student.get(), course.get())));
    }
}
