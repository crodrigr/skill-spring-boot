package com.academy.business.service;

import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.entity.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface StudentService {

    Page<StudentEntity> search(String name, Pageable pageable);

    Optional<StudentEntity> findById(Long id);

    ServiceResult<StudentEntity> create(StudentEntity data);

    ServiceResult<StudentEntity> update(Long id, StudentEntity data);

    ServiceResult<StudentEntity> changeStatus(Long id, StudentStatus status);

    boolean delete(Long id);
}
