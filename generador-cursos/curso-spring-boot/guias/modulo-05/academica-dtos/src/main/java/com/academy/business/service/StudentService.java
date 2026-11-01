package com.academy.business.service;

import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.repository.StudentJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StudentService {

    private final StudentJpaRepository students;

    public StudentService(StudentJpaRepository students) {
        this.students = students;
    }

    @Transactional(readOnly = true)
    public Page<StudentEntity> findAll(Pageable pageable) {
        return students.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<StudentEntity> findById(Long id) {
        return students.findById(id);
    }

    @Transactional
    public ServiceResult<StudentEntity> create(StudentEntity student) {
        if (students.existsByEmailIgnoreCase(student.getEmail())) {
            return ServiceResult.conflict();
        }
        return ServiceResult.ok(students.save(student));
    }
}
