package com.academy.business.service;

import com.academy.persistence.entity.StudentEntity;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StudentService {

    List<StudentEntity> search(String name, Pageable pageable);

    Optional<StudentEntity> findById(Long id);

    Optional<StudentEntity> findByEmail(String email);

    Optional<StudentEntity> create(StudentEntity student);

    Optional<StudentEntity> update(Long id, StudentEntity data);

    boolean delete(Long id);
}
