package com.academy.business.service;

import com.academy.persistence.entity.StudentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    Page<StudentEntity> findAll(Pageable pageable);

    StudentEntity findById(Long id);

    StudentEntity findByEmail(String email);

    StudentEntity create(StudentEntity student);
}
