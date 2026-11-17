package com.academy.presentation.controller;

import com.academy.business.service.StudentService;
import com.academy.persistence.entity.StudentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService service;

    @GetMapping
    public List<StudentEntity> findAll() {
        return service.findAll();
    }

    @PostMapping
    public ResponseEntity<StudentEntity> create(@RequestBody StudentEntity student) {
        return service.create(student)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }
}
