package com.academy.persistence.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "Student", description = "Estudiante de la academia")
@Entity
@Table(name = "student")
public class StudentEntity {

    @Schema(description = "Identificador generado por la base", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Nombre completo", example = "Ana Torres", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false)
    private String name;

    @Schema(description = "Edad en años", example = "22", minimum = "16", maximum = "99")
    @Min(16)
    @Max(99)
    private int age;

    @Schema(description = "Correo único (sin importar mayúsculas)", example = "ana@mail.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Schema(description = "Estado del estudiante", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus status;

    protected StudentEntity() {
    }

    public StudentEntity(Long id, String name, int age, String email, StudentStatus status) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getEmail() { return email; }
    public StudentStatus getStatus() { return status; }
}
