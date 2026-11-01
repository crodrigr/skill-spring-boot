package com.academy.business.service;

import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.entity.StudentStatus;
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
    public Page<StudentEntity> search(String name, Pageable pageable) {
        if (name == null || name.isBlank()) {
            return students.findAll(pageable);
        }
        return students.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<StudentEntity> findById(Long id) {
        return students.findById(id);
    }

    @Transactional
    public ServiceResult<StudentEntity> create(StudentEntity data) {
        if (students.existsByEmailIgnoreCase(data.getEmail())) {
            return ServiceResult.conflict();
        }
        StudentEntity toSave = new StudentEntity(
                null, data.getName(), data.getAge(), data.getEmail(), data.getStatus());
        return ServiceResult.ok(students.save(toSave));
    }

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

    @Transactional
    public boolean delete(Long id) {
        if (!students.existsById(id)) {
            return false;
        }
        students.deleteById(id);
        return true;
    }
}
