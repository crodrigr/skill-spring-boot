package com.academy.presentation.controller;

import com.academy.business.service.CourseService;
import com.academy.business.service.ServiceResult;
import com.academy.persistence.entity.CourseEntity;
import com.academy.presentation.dto.CourseRequest;
import com.academy.presentation.dto.CourseResponse;
import com.academy.presentation.dto.EnrolledResponse;
import com.academy.presentation.dto.PageResponse;
import com.academy.presentation.mapper.CourseMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<CourseResponse> findAll(Pageable pageable) {
        return CourseMapper.toPageResponse(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(CourseMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        CourseEntity created = service.create(CourseMapper.toEntity(request));
        CourseResponse body = CourseMapper.toResponse(created);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}/enrolled")
    public ResponseEntity<EnrolledResponse> countEnrolled(@PathVariable Long id) {
        ServiceResult<Long> result = service.countEnrolled(id);
        return switch (result.status()) {
            case OK -> ResponseEntity.ok(new EnrolledResponse(id, result.value()));
            case NOT_FOUND, CONFLICT -> ResponseEntity.notFound().build();
        };
    }
}
