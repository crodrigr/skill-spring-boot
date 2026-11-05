package com.academy.business.service;

import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.entity.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    Page<StudentEntity> search(String name, Pageable pageable);

    StudentEntity findById(Long id);

    StudentEntity findByEmail(String email);

    StudentEntity create(StudentEntity data);

    StudentEntity update(Long id, StudentEntity data);

    StudentEntity changeStatus(Long id, StudentStatus status);

    void delete(Long id);
}
