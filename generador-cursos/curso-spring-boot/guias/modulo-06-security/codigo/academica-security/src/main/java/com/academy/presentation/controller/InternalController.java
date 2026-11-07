package com.academy.presentation.controller;

import com.academy.business.service.CourseService;
import com.academy.business.exception.StudentEmailAlreadyExistsException;
import com.academy.business.service.StudentService;
import com.academy.persistence.entity.CourseEntity;
import com.academy.persistence.entity.StudentEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal")
@Tag(name = "Internal", description = "Operaciones de apoyo para el equipo (no son parte de la API pública)")
public class InternalController {

    private final StudentService students;
    private final CourseService courses;

    public InternalController(StudentService students, CourseService courses) {
        this.students = students;
        this.courses = courses;
    }

    @Operation(summary = "Estado del servicio", description = "Público: no pide token.")
    @SecurityRequirements
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @Operation(summary = "Cargar datos de ejemplo",
            description = "Crea tres estudiantes (si sus correos no existen) y dos cursos (solo si no hay ninguno). Solo ADMIN.")
    @PostMapping("/seed")
    public Map<String, Integer> seed() {
        int createdStudents = 0;
        StudentEntity[] sampleStudents = {
                new StudentEntity("Ana Torres", 22, "ana@mail.com"),
                new StudentEntity("Luis Pérez", 25, "luis@mail.com"),
                new StudentEntity("María Gómez", 31, "maria@mail.com")
        };
        for (StudentEntity sample : sampleStudents) {
            try {
                students.create(sample);
                createdStudents++;
            } catch (StudentEmailAlreadyExistsException ignored) {
                // ya existe: no se vuelve a crear
            }
        }
        int createdCourses = 0;
        if (courses.findAll(PageRequest.of(0, 1)).isEmpty()) {
            courses.create(new CourseEntity("Java desde cero", 150000, 30));
            courses.create(new CourseEntity("Spring Boot", 250000, 20));
            createdCourses = 2;
        }
        return Map.of("students", createdStudents, "courses", createdCourses);
    }
}
