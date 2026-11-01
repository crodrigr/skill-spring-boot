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
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Students", description = "Operaciones sobre estudiantes")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @Operation(summary = "Listar estudiantes",
            description = "Devuelve una página de estudiantes. Con `name` filtra por nombre (contiene, sin importar mayúsculas). El tamaño máximo de página es 50.")
    @ApiResponse(responseCode = "200", description = "Página de estudiantes")
    @GetMapping
    public PageResponse<StudentResponse> search(
            @Parameter(description = "Parte del nombre a buscar", example = "ana")
            @RequestParam(required = false) String name,
            @ParameterObject Pageable pageable) {
        return StudentMapper.toPageResponse(service.search(name, pageable));
    }

    @Operation(summary = "Buscar un estudiante por id")
    @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> findById(
            @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
        return service.findById(id)
                .map(StudentMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar un estudiante por correo",
            description = "Obsoleto: usar `GET /api/v1/students?name=` o `GET /api/v1/students/{id}`.",
            deprecated = true)
    @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese correo", content = @Content)
    @GetMapping("/by-email")
    public ResponseEntity<StudentResponse> findByEmail(
            @Parameter(description = "Correo exacto", example = "ana@mail.com") @RequestParam String email) {
        return service.findByEmail(email)
                .map(StudentMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear un estudiante",
            description = "El `id` lo genera la base y no se envía. Si el correo ya existe responde `409`.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del estudiante",
            content = @Content(examples = {
                    @ExampleObject(name = "Estudiante activo", summary = "Un estudiante activo",
                            value = "{\"name\":\"Ana Torres\",\"age\":22,\"email\":\"ana@mail.com\",\"status\":\"ACTIVE\"}"),
                    @ExampleObject(name = "Estudiante inactivo", summary = "Un estudiante inactivo",
                            value = "{\"name\":\"Luis Pérez\",\"age\":25,\"email\":\"luis@mail.com\",\"status\":\"INACTIVE\"}")
            }))
    @ApiResponse(responseCode = "201", description = "Estudiante creado",
            headers = @Header(name = "Location", description = "URL del estudiante creado",
                    schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/students/1")))
    @ApiResponse(responseCode = "400", description = "Datos inválidos (Bean Validation)", content = @Content)
    @ApiResponse(responseCode = "409", description = "El correo ya está registrado", content = @Content)
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

    @Operation(summary = "Reemplazar un estudiante",
            description = "Reemplaza todos los datos del estudiante. Responde `404` si no existe y `409` si el correo es de otro estudiante.")
    @ApiResponse(responseCode = "200", description = "Estudiante actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
    @ApiResponse(responseCode = "409", description = "El correo pertenece a otro estudiante", content = @Content)
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> update(
            @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {
        return respond(service.update(id, StudentMapper.toEntity(request)));
    }

    @Operation(summary = "Cambiar el estado de un estudiante",
            description = "Cambia solo el estado; el resto de los datos no se toca.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "El estado es obligatorio", content = @Content)
    @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
    @PatchMapping("/{id}/status")
    public ResponseEntity<StudentResponse> changeStatus(
            @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id,
            @Valid @RequestBody StudentStatusRequest request) {
        return respond(service.changeStatus(id, request.status()));
    }

    @Operation(summary = "Eliminar un estudiante", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Estudiante eliminado")
    @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Hidden
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    private ResponseEntity<StudentResponse> respond(ServiceResult<StudentEntity> result) {
        return switch (result.status()) {
            case OK -> ResponseEntity.ok(StudentMapper.toResponse(result.value()));
            case NOT_FOUND -> ResponseEntity.notFound().build();
            case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
        };
    }
}
