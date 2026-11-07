package com.academy.business.impl;

import com.academy.business.exception.StudentEmailAlreadyExistsException;
import com.academy.business.exception.StudentNotFoundException;
import com.academy.business.service.StudentService;
import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.entity.StudentStatus;
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
    public Page<StudentEntity> search(String name, Pageable pageable) {
        if (name == null || name.isBlank()) {
            return students.findAll(pageable);
        }
        return students.findByNameContainingIgnoreCase(name, pageable);
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
    public StudentEntity create(StudentEntity data) {
        if (students.existsByEmailIgnoreCase(data.getEmail())) {
            throw new StudentEmailAlreadyExistsException(data.getEmail());
        }
        return students.save(new StudentEntity(
                null, data.getName(), data.getAge(), data.getEmail(), data.getStatus()));
    }

    @Override
    @Transactional
    public StudentEntity update(Long id, StudentEntity data) {
        if (!students.existsById(id)) {
            throw StudentNotFoundException.byId(id);
        }
        if (students.existsByEmailIgnoreCaseAndIdNot(data.getEmail(), id)) {
            throw new StudentEmailAlreadyExistsException(data.getEmail());
        }
        return students.save(new StudentEntity(
                id, data.getName(), data.getAge(), data.getEmail(), data.getStatus()));
    }

    @Override
    @Transactional
    public StudentEntity changeStatus(Long id, StudentStatus status) {
        StudentEntity student = findById(id);
        return students.save(new StudentEntity(
                id, student.getName(), student.getAge(), student.getEmail(), status));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!students.existsById(id)) {
            throw StudentNotFoundException.byId(id);
        }
        students.deleteById(id);
    }
}
