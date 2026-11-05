package com.academy.business.impl;

import com.academy.business.exception.StudentEmailAlreadyExistsException;
import com.academy.business.exception.StudentNotFoundException;
import com.academy.business.service.StudentService;
import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.repository.StudentJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentJpaRepository students;

    public StudentServiceImpl(StudentJpaRepository students) {
        this.students = students;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentEntity> findAll(Pageable pageable) {
        return students.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentEntity findById(Long id) {
        return students.findById(id)
                .orElseThrow(() -> StudentNotFoundException.byId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentEntity findByEmail(String email) {
        return students.findByEmailIgnoreCase(email)
                .orElseThrow(() -> StudentNotFoundException.byEmail(email));
    }

    @Override
    @Transactional
    public StudentEntity create(StudentEntity student) {
        if (students.existsByEmailIgnoreCase(student.getEmail())) {
            throw new StudentEmailAlreadyExistsException(student.getEmail());
        }
        return students.save(student);
    }
}
