package com.academy.business.impl;

import com.academy.business.service.ServiceResult;
import com.academy.business.service.StudentService;
import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.entity.StudentStatus;
import com.academy.persistence.repository.StudentJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    public ServiceResult<StudentEntity> create(StudentEntity data) {
        if (students.existsByEmailIgnoreCase(data.getEmail())) {
            return ServiceResult.conflict();
        }
        StudentEntity toSave = new StudentEntity(
                null, data.getName(), data.getAge(), data.getEmail(), data.getStatus());
        return ServiceResult.ok(students.save(toSave));
    }

    @Override
    @Transactional
    public ServiceResult<StudentEntity> update(Long id, StudentEntity data) {
        if (!students.existsById(id)) {
            return ServiceResult.notFound();
        }
        if (students.existsByEmailIgnoreCaseAndIdNot(data.getEmail(), id)) {
            return ServiceResult.conflict();
        }
        StudentEntity toSave = new StudentEntity(
                id, data.getName(), data.getAge(), data.getEmail(), data.getStatus());
        return ServiceResult.ok(students.save(toSave));
    }

    @Override
    @Transactional
    public ServiceResult<StudentEntity> changeStatus(Long id, StudentStatus status) {
        Optional<StudentEntity> current = students.findById(id);
        if (current.isEmpty()) {
            return ServiceResult.notFound();
        }
        StudentEntity student = current.get();
        StudentEntity toSave = new StudentEntity(
                id, student.getName(), student.getAge(), student.getEmail(), status);
        return ServiceResult.ok(students.save(toSave));
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
