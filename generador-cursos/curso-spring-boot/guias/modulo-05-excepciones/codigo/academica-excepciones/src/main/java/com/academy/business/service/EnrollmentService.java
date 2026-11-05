package com.academy.business.service;

import com.academy.persistence.entity.EnrollmentEntity;

public interface EnrollmentService {

    EnrollmentEntity findById(Long id);

    EnrollmentEntity enroll(Long studentId, Long courseId);
}
