package com.academy.presentation.controller;

import com.academy.business.service.StudentService;
import com.academy.persistence.entity.StudentEntity;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Students", description = "Operaciones sobre estudiantes")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @Operation(summary = "Listar estudiantes",
            description = "Devuelve los estudiantes de la página pedida. Con `name` filtra por nombre (contiene, sin importar mayúsculas).")
    @ApiResponse(responseCode = "200", description = "Lista de estudiantes de la página pedida")
    @GetMapping
    public List<StudentEntity> search(
            @Parameter(description = "Parte del nombre a buscar", example = "ana")
            @RequestParam(required = false) String name,
            @ParameterObject Pageable pageable) {
        return service.search(name, pageable);
    }

    @Operation(summary = "Buscar un estudiante por id")
    @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<StudentEntity> findById(
            @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar un estudiante por correo",
            description = "Obsoleto: usar `GET /api/students?name=` o `GET /api/students/{id}`.",
            deprecated = true)
    @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese correo", content = @Content)
    @GetMapping("/by-email")
    public ResponseEntity<StudentEntity> findByEmail(
            @Parameter(description = "Correo exacto", example = "ana@mail.com") @RequestParam String email) {
        return service.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear un estudiante",
            description = "El `id` lo genera la base. Si el correo ya existe responde `409`.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del estudiante",
            content = @Content(examples = {
                    @ExampleObject(name = "Estudiante activo", summary = "Un estudiante activo",
                            value = "{\"name\":\"Ana Torres\",\"age\":22,\"email\":\"ana@mail.com\",\"status\":\"ACTIVE\"}"),
                    @ExampleObject(name = "Estudiante inactivo", summary = "Un estudiante inactivo",
                            value = "{\"name\":\"Luis Pérez\",\"age\":25,\"email\":\"luis@mail.com\",\"status\":\"INACTIVE\"}")
            }))
    @ApiResponse(responseCode = "201", description = "Estudiante creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (Bean Validation)", content = @Content)
    @ApiResponse(responseCode = "409", description = "El correo ya está registrado", content = @Content)
    @PostMapping
    public ResponseEntity<StudentEntity> create(@Valid @RequestBody StudentEntity student) {
        return service.create(student)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @Operation(summary = "Actualizar un estudiante",
            description = "Reemplaza todos los datos del estudiante. Responde `404` si no existe y `409` si el correo es de otro estudiante.")
    @ApiResponse(responseCode = "200", description = "Estudiante actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
    @ApiResponse(responseCode = "409", description = "El correo pertenece a otro estudiante", content = @Content)
    @PutMapping("/{id}")
    public ResponseEntity<StudentEntity> update(
            @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id,
            @Valid @RequestBody StudentEntity student) {
        if (service.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return service.update(id, student)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
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
}
