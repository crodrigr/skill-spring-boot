package com.academy.presentation.controller;

import com.academy.business.service.CourseService;
import com.academy.persistence.entity.CourseEntity;
import com.academy.presentation.dto.CourseRequest;
import com.academy.presentation.dto.CourseResponse;
import com.academy.presentation.dto.EnrolledResponse;
import com.academy.presentation.dto.PageResponse;
import com.academy.presentation.mapper.CourseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Courses", description = "Operaciones sobre cursos")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @Operation(operationId = "listCourses", summary = "Listar cursos",
            description = "Devuelve una página de cursos. El tamaño máximo de página es 50.")
    @ApiResponse(responseCode = "200", description = "Página de cursos")
    @ApiResponse(responseCode = "400", ref = "BadRequest")
    @GetMapping
    public PageResponse<CourseResponse> findAll(@ParameterObject Pageable pageable) {
        return CourseMapper.toPageResponse(service.findAll(pageable));
    }

    @Operation(operationId = "getCourse", summary = "Buscar un curso por id")
    @ApiResponse(responseCode = "200", description = "Curso encontrado")
    @ApiResponse(responseCode = "400", ref = "BadRequest")
    @ApiResponse(responseCode = "404", ref = "NotFound")
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> findById(
            @Parameter(description = "Identificador del curso", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(CourseMapper.toResponse(service.findById(id)));
    }

    @Operation(operationId = "createCourse", summary = "Crear un curso",
            description = "Solo ADMIN. El precio debe ser mayor que cero y el cupo, al menos 1.")
    @ApiResponse(responseCode = "201", description = "Curso creado",
            headers = @Header(name = "Location", description = "URL del curso creado",
                    schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/courses/1")))
    @ApiResponse(responseCode = "400", ref = "BadRequest")
    @PostMapping
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        CourseEntity created = service.create(CourseMapper.toEntity(request));
        CourseResponse body = CourseMapper.toResponse(created);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Operation(operationId = "countCourseEnrolled", summary = "Contar los matriculados de un curso")
    @ApiResponse(responseCode = "200", description = "Cantidad de matriculados")
    @ApiResponse(responseCode = "400", ref = "BadRequest")
    @ApiResponse(responseCode = "404", ref = "NotFound")
    @GetMapping("/{id}/enrolled")
    public ResponseEntity<EnrolledResponse> countEnrolled(
            @Parameter(description = "Identificador del curso", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(new EnrolledResponse(id, service.countEnrolled(id)));
    }
}
