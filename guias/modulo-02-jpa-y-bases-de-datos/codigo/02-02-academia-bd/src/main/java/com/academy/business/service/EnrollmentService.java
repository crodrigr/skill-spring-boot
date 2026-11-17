package com.academy.business.service;

import com.academy.persistence.entity.EnrollmentEntity;

import java.util.Optional;

public interface EnrollmentService {

    Optional<EnrollmentEntity> enroll(Long studentId, Long courseId);
}
