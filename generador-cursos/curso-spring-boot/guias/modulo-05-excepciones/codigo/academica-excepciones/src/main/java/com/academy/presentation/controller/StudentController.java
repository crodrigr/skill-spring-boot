package com.academy.presentation.controller;

import com.academy.business.service.StudentService;
import com.academy.presentation.dto.PageResponse;
import com.academy.presentation.dto.StudentRequest;
import com.academy.presentation.dto.StudentResponse;
import com.academy.presentation.mapper.StudentMapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Students", description = "Operaciones sobre estudiantes")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @Operation(operationId = "listStudents", summary = "Listar estudiantes",
            description = "Devuelve una página de estudiantes. El tamaño máximo de página es 50.")
    @ApiResponse(responseCode = "200", description = "Página de estudiantes")
    @ApiResponse(responseCode = "400", ref = "BadRequest")
    @GetMapping
    public PageResponse<StudentResponse> findAll(@ParameterObject Pageable pageable) {
        return StudentMapper.toPageResponse(service.findAll(pageable));
    }

    @Operation(operationId = "getStudent", summary = "Buscar un estudiante por id")
    @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
    @ApiResponse(responseCode = "400", ref = "BadRequest")
    @ApiResponse(responseCode = "404", ref = "NotFound")
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> findById(
            @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(StudentMapper.toResponse(service.findById(id)));
    }

    @Operation(operationId = "getStudentByEmail", summary = "Buscar un estudiante por correo",
            description = "Obsoleto: usar `GET /api/v1/students/{id}`.",
            deprecated = true)
    @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
    @ApiResponse(responseCode = "400", ref = "BadRequest")
    @ApiResponse(responseCode = "404", ref = "NotFound")
    @GetMapping("/by-email")
    public ResponseEntity<StudentResponse> findByEmail(
            @Parameter(description = "Correo exacto", example = "ana@mail.com") @RequestParam String email) {
        return ResponseEntity.ok(StudentMapper.toResponse(service.findByEmail(email)));
    }

    @Operation(operationId = "createStudent", summary = "Registrar un estudiante",
            description = "El `id` lo genera la base y no se envía. Si el correo ya existe responde `409`.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del estudiante",
            content = @Content(examples = {
                    @ExampleObject(name = "Estudiante adulto", summary = "Un estudiante de 22 años",
                            value = "{\"name\":\"Ana Torres\",\"age\":22,\"email\":\"ana@mail.com\"}"),
                    @ExampleObject(name = "Estudiante mayor", summary = "Un estudiante de 31 años",
                            value = "{\"name\":\"María Gómez\",\"age\":31,\"email\":\"maria@mail.com\"}")
            }))
    @ApiResponse(responseCode = "201", description = "Estudiante registrado",
            headers = @Header(name = "Location", description = "URL del estudiante creado",
                    schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/students/1")))
    @ApiResponse(responseCode = "400", ref = "BadRequest")
    @ApiResponse(responseCode = "409", ref = "Conflict")
    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        StudentResponse body = StudentMapper.toResponse(service.create(StudentMapper.toEntity(request)));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Hidden
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
