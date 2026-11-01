package com.academy.presentation.controller;

import com.academy.business.service.ServiceResult;
import com.academy.business.service.ServiceStatus;
import com.academy.business.service.StudentService;
import com.academy.persistence.entity.StudentEntity;
import com.academy.presentation.dto.PageResponse;
import com.academy.presentation.dto.StudentRequest;
import com.academy.presentation.dto.StudentResponse;
import com.academy.presentation.dto.StudentStatusRequest;
import com.academy.presentation.mapper.StudentMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<StudentResponse> search(@RequestParam(required = false) String name, Pageable pageable) {
        return StudentMapper.toPageResponse(service.search(name, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(StudentMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
        if (result.status() == ServiceStatus.CONFLICT) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        StudentResponse body = StudentMapper.toResponse(result.value());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return respond(service.update(id, StudentMapper.toEntity(request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<StudentResponse> changeStatus(@PathVariable Long id,
                                                        @Valid @RequestBody StudentStatusRequest request) {
        return respond(service.changeStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private ResponseEntity<StudentResponse> respond(ServiceResult<StudentEntity> result) {
        return switch (result.status()) {
            case OK -> ResponseEntity.ok(StudentMapper.toResponse(result.value()));
            case NOT_FOUND -> ResponseEntity.notFound().build();
            case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
        };
    }
}
