package com.academy.business.impl;

import com.academy.business.service.StudentService;
import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.repository.StudentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentJpaRepository students;

    @Override
    @Transactional(readOnly = true)
    public List<StudentEntity> findAll() {
        return students.findAll();
    }

    @Override
    @Transactional
    public Optional<StudentEntity> create(StudentEntity student) {
        if (students.existsByEmailIgnoreCase(student.getEmail())) {
            return Optional.empty();
        }
        return Optional.of(students.save(student));
    }
}
