package com.academy.presentation.controller;

import com.academy.business.service.EnrollmentService;
import com.academy.business.service.ServiceResult;
import com.academy.persistence.entity.EnrollmentEntity;
import com.academy.presentation.dto.EnrollmentRequest;
import com.academy.presentation.dto.EnrollmentResponse;
import com.academy.presentation.mapper.EnrollmentMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    private final EnrollmentService service;

    public EnrollmentController(EnrollmentService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(EnrollmentMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        ServiceResult<EnrollmentEntity> result = service.enroll(request.studentId(), request.courseId());
        return switch (result.status()) {
            case NOT_FOUND -> ResponseEntity.notFound().build();
            case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
            case OK -> {
                EnrollmentResponse body = EnrollmentMapper.toResponse(result.value());
                URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}").buildAndExpand(body.id()).toUri();
                yield ResponseEntity.created(location).body(body);
            }
        };
    }
}
