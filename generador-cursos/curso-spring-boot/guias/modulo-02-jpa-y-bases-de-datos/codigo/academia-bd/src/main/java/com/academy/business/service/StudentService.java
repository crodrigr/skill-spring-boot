package com.academy.business.service;

import com.academy.persistence.entity.StudentEntity;

import java.util.List;
import java.util.Optional;

public interface StudentService {

    List<StudentEntity> findAll();

    Optional<StudentEntity> create(StudentEntity student);
}
