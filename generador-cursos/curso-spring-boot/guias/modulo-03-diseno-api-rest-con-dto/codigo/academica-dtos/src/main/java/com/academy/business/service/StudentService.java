package com.academy.business.service;

import com.academy.persistence.entity.StudentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface StudentService {

    Page<StudentEntity> findAll(Pageable pageable);

    Optional<StudentEntity> findById(Long id);

    ServiceResult<StudentEntity> create(StudentEntity student);
}
