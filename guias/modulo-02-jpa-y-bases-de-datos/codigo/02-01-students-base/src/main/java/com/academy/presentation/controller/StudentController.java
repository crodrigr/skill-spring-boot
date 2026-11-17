package com.academy.presentation.controller;

import com.academy.business.service.StudentService;
import com.academy.persistence.entity.StudentEntity;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    public List<StudentEntity> search(@RequestParam(required = false) String name, Pageable pageable) {
        return service.search(name, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentEntity> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-email")
    public ResponseEntity<StudentEntity> findByEmail(@RequestParam String email) {
        return service.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StudentEntity> create(@Valid @RequestBody StudentEntity student) {
        return service.create(student)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentEntity> update(@PathVariable Long id, @Valid @RequestBody StudentEntity student) {
        if (service.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return service.update(id, student)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
