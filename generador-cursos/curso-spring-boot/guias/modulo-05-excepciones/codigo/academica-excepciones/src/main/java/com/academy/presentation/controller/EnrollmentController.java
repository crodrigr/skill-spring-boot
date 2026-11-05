package com.academy.presentation.controller;

import com.academy.business.service.EnrollmentService;
import com.academy.presentation.dto.EnrollmentRequest;
import com.academy.presentation.dto.EnrollmentResponse;
import com.academy.presentation.mapper.EnrollmentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Enrollments", description = "Matrículas de estudiantes en cursos")
public class EnrollmentController {

    private final EnrollmentService service;

    public EnrollmentController(EnrollmentService service) {
        this.service = service;
    }

    @Operation(operationId = "getEnrollment", summary = "Buscar una matrícula por id")
    @ApiResponse(responseCode = "200", description = "Matrícula encontrada")
    @ApiResponse(responseCode = "400", ref = "BadRequest")
    @ApiResponse(responseCode = "404", ref = "NotFound")
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponse> findById(
            @Parameter(description = "Identificador de la matrícula", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(EnrollmentMapper.toResponse(service.findById(id)));
    }

    @Operation(operationId = "enroll", summary = "Matricular a un estudiante en un curso",
            description = "Responde `404` si el estudiante o el curso no existen, y `409` si ya está matriculado o el curso no tiene cupo.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Ids del estudiante y del curso",
            content = @Content(examples = @ExampleObject(name = "Matrícula", summary = "Estudiante 1 en el curso 1",
                    value = "{\"studentId\":1,\"courseId\":1}")))
    @ApiResponse(responseCode = "201", description = "Matrícula creada",
            headers = @Header(name = "Location", description = "URL de la matrícula creada",
                    schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/enrollments/1")))
    @ApiResponse(responseCode = "400", ref = "BadRequest")
    @ApiResponse(responseCode = "404", ref = "NotFound")
    @ApiResponse(responseCode = "409", ref = "Conflict")
    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse body = EnrollmentMapper.toResponse(
                service.enroll(request.studentId(), request.courseId()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }
}
