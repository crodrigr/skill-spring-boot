package com.academy.presentation.controller;

import com.academy.business.exception.StudentEmailAlreadyExistsException;
import com.academy.business.service.StudentService;
import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.entity.StudentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal")
@Tag(name = "Internal", description = "Operaciones de apoyo para el equipo (no son parte de la API pública)")
public class InternalController {

    private final StudentService service;

    public InternalController(StudentService service) {
        this.service = service;
    }

    @Operation(summary = "Estado del servicio")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @Operation(summary = "Cargar estudiantes de ejemplo", description = "Crea tres estudiantes si no existen.")
    @PostMapping("/seed")
    public Map<String, Integer> seed() {
        int created = 0;
        StudentEntity[] samples = {
                new StudentEntity(null, "Ana Torres", 22, "ana@mail.com", StudentStatus.ACTIVE),
                new StudentEntity(null, "Luis Pérez", 25, "luis@mail.com", StudentStatus.ACTIVE),
                new StudentEntity(null, "María Gómez", 31, "maria@mail.com", StudentStatus.INACTIVE)
        };
        for (StudentEntity sample : samples) {
            try {
                service.create(sample);
                created++;
            } catch (StudentEmailAlreadyExistsException ignored) {
                // ya existe: no se vuelve a crear
            }
        }
        return Map.of("created", created);
    }
}
