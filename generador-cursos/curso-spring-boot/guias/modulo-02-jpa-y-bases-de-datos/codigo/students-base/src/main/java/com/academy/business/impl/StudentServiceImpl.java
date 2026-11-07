package com.academy.business.impl;

import com.academy.business.service.StudentService;
import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.repository.StudentJpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentJpaRepository students;

    public StudentServiceImpl(StudentJpaRepository students) {
        this.students = students;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentEntity> search(String name, Pageable pageable) {
        if (name == null || name.isBlank()) {
            return students.findAll(pageable).getContent();
        }
        return students.findByNameContainingIgnoreCase(name, pageable).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudentEntity> findById(Long id) {
        return students.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudentEntity> findByEmail(String email) {
        return students.findByEmailIgnoreCase(email);
    }

    @Override
    @Transactional
    public Optional<StudentEntity> create(StudentEntity student) {
        if (students.existsByEmailIgnoreCase(student.getEmail())) {
            return Optional.empty();
        }
        StudentEntity toSave = new StudentEntity(
                null, student.getName(), student.getAge(), student.getEmail(), student.getStatus());
        return Optional.of(students.save(toSave));
    }

    @Override
    @Transactional
    public Optional<StudentEntity> update(Long id, StudentEntity data) {
        if (!students.existsById(id)) {
            return Optional.empty();
        }
        if (students.existsByEmailIgnoreCaseAndIdNot(data.getEmail(), id)) {
            return Optional.empty();
        }
        return Optional.of(students.save(new StudentEntity(
                id, data.getName(), data.getAge(), data.getEmail(), data.getStatus())));
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!students.existsById(id)) {
            return false;
        }
        students.deleteById(id);
        return true;
    }
}
