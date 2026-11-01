package com.academy.business.service;

import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.repository.StudentJpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentJpaRepository students;

    public StudentService(StudentJpaRepository students) {
        this.students = students;
    }

    @Transactional(readOnly = true)
    public List<StudentEntity> search(String name, Pageable pageable) {
        if (name == null || name.isBlank()) {
            return students.findAll(pageable).getContent();
        }
        return students.findByNameContainingIgnoreCase(name, pageable).getContent();
    }

    @Transactional(readOnly = true)
    public Optional<StudentEntity> findById(Long id) {
        return students.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<StudentEntity> findByEmail(String email) {
        return students.findByEmailIgnoreCase(email);
    }

    @Transactional
    public Optional<StudentEntity> create(StudentEntity student) {
        if (students.existsByEmailIgnoreCase(student.getEmail())) {
            return Optional.empty();
        }
        StudentEntity toSave = new StudentEntity(
                null, student.getName(), student.getAge(), student.getEmail(), student.getStatus());
        return Optional.of(students.save(toSave));
    }

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

    @Transactional
    public boolean delete(Long id) {
        if (!students.existsById(id)) {
            return false;
        }
        students.deleteById(id);
        return true;
    }
}
