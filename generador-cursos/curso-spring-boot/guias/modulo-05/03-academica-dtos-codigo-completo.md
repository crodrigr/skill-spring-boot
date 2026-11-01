# 📘 Academia con DTO — Código completo

> 🧾 **Solo el código.** Versión final de la [guía `02-academica-dtos`](02-academica-dtos.md), con el árbol (🆕 nuevo · ✏️ cambia) y el código completo sin explicaciones, seguido del **Paso 7 — Probar la API**. Cada archivo es un bloque que se despliega. Los grupos van en **orden de dependencia**: cada archivo aparece después de lo que necesita.
>
> 📦 **Código fuente:** [`academica-dtos/`](academica-dtos/) (proyecto Maven completo; cada archivo de abajo enlaza a su original) · ✅ compilado y ejecutado con Spring Boot 3.3 y MySQL 8.

<details>
<summary><strong>📁 Estructura final esperada</strong></summary>

> ```text
> 📁 academica-dtos
> ├── ✏️ pom.xml                                   + spring-boot-starter-validation
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 presentation
>     │   │   ├── 📁 controller
>     │   │   │   ├── ✏️ StudentController.java
>     │   │   │   ├── ✏️ CourseController.java
>     │   │   │   └── ✏️ EnrollmentController.java
>     │   │   ├── 📁 dto
>     │   │   │   ├── 🆕 StudentRequest.java
>     │   │   │   ├── 🆕 StudentResponse.java
>     │   │   │   ├── 🆕 CourseRequest.java
>     │   │   │   ├── 🆕 CourseResponse.java
>     │   │   │   ├── 🆕 EnrolledResponse.java
>     │   │   │   ├── 🆕 EnrollmentRequest.java
>     │   │   │   ├── 🆕 EnrollmentResponse.java
>     │   │   │   └── 🆕 PageResponse.java
>     │   │   └── 📁 mapper
>     │   │       ├── 🆕 StudentMapper.java
>     │   │       ├── 🆕 CourseMapper.java
>     │   │       └── 🆕 EnrollmentMapper.java
>     │   ├── 📁 business
>     │   │   └── 📁 service
>     │   │       ├── ✏️ StudentService.java
>     │   │       ├── ✏️ CourseService.java
>     │   │       ├── ✏️ EnrollmentService.java
>     │   │       ├── 🆕 ServiceResult.java
>     │   │       └── 🆕 ServiceStatus.java
>     │   └── 📁 persistence
>     │       ├── 📁 entity
>     │       │   ├── ✏️ StudentEntity.java          + campo interno createdAt
>     │       │   ├── 📄 CourseEntity.java
>     │       │   └── 📄 EnrollmentEntity.java
>     │       └── 📁 repository
>     │           ├── 📄 StudentJpaRepository.java
>     │           ├── 📄 CourseJpaRepository.java
>     │           └── 📄 EnrollmentJpaRepository.java
>     └── 📁 resources
>         └── ✏️ application.properties             límites de paginación y errores
> ```
>
> 🆕 archivo nuevo · ✏️ archivo del taller que cambia · 📄 sin cambios respecto al taller (guía 01 del módulo 03-04)
>
> **Código final, paso a paso**
>
> ### 📁 Configuración
>
> <details>
> <summary><strong>📄 pom.xml</strong></summary>
>
> **Ruta:** [`pom.xml`](academica-dtos/pom.xml)
>
> ```xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>3.3.5</version>
>         <relativePath/>
>     </parent>
>     <groupId>com.academy</groupId>
>     <artifactId>academy</artifactId>
>     <version>0.0.1-SNAPSHOT</version>
>     <name>academy</name>
>     <description>Taller de gestión de cursos con base de datos</description>
>     <properties>
>         <java.version>17</java.version>
>     </properties>
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-data-jpa</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-validation</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>com.mysql</groupId>
>             <artifactId>mysql-connector-j</artifactId>
>             <scope>runtime</scope>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-devtools</artifactId>
>             <scope>runtime</scope>
>             <optional>true</optional>
>         </dependency>
>     </dependencies>
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 application.properties</strong></summary>
>
> **Ruta:** [`src/main/resources/application.properties`](academica-dtos/src/main/resources/application.properties)
>
> ```properties
> # MySQL connection
> spring.datasource.url=jdbc:mysql://localhost:3306/academy
> spring.datasource.username=root
> spring.datasource.password=root
>
> # JPA and Hibernate
> spring.jpa.hibernate.ddl-auto=update
> spring.jpa.show-sql=true
>
> # Pagination limits (protect the API)
> spring.data.web.pageable.default-page-size=10
> spring.data.web.pageable.max-page-size=50
>
> # Error responses: do not expose internals (DevTools shows them by default)
> server.error.include-stacktrace=never
> server.error.include-message=never
> server.error.include-binding-errors=never
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 AcademyApplication.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/AcademyApplication.java`](academica-dtos/src/main/java/com/academy/AcademyApplication.java)
>
> ```java
> package com.academy;
>
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
>
> @SpringBootApplication
> public class AcademyApplication {
>
>     public static void main(String[] args) {
>         SpringApplication.run(AcademyApplication.class, args);
>     }
> }
> ```
>
> </details>
>
> ---
>
> ### 📁 persistence
>
> #### 📁 entity
>
> <details>
> <summary><strong>📄 StudentEntity.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/persistence/entity/StudentEntity.java`](academica-dtos/src/main/java/com/academy/persistence/entity/StudentEntity.java)
>
> ```java
> package com.academy.persistence.entity;
>
> import jakarta.persistence.Column;
> import jakarta.persistence.Entity;
> import jakarta.persistence.GeneratedValue;
> import jakarta.persistence.GenerationType;
> import jakarta.persistence.Id;
> import jakarta.persistence.Table;
> import org.hibernate.annotations.CreationTimestamp;
>
> import java.time.LocalDateTime;
>
> @Entity
> @Table(name = "student")
> public class StudentEntity {
>
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>
>     private String name;
>
>     private int age;
>
>     @Column(unique = true)
>     private String email;
>
>     @CreationTimestamp
>     @Column(name = "created_at", updatable = false)
>     private LocalDateTime createdAt;
>
>     protected StudentEntity() {
>     }
>
>     public StudentEntity(String name, int age, String email) {
>         this.name = name;
>         this.age = age;
>         this.email = email;
>     }
>
>     public Long getId() { return id; }
>     public String getName() { return name; }
>     public int getAge() { return age; }
>     public String getEmail() { return email; }
>
>     public LocalDateTime getCreatedAt() { return createdAt; }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 CourseEntity.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/persistence/entity/CourseEntity.java`](academica-dtos/src/main/java/com/academy/persistence/entity/CourseEntity.java)
>
> ```java
> package com.academy.persistence.entity;
>
> import jakarta.persistence.Entity;
> import jakarta.persistence.GeneratedValue;
> import jakarta.persistence.GenerationType;
> import jakarta.persistence.Id;
> import jakarta.persistence.Table;
>
> @Entity
> @Table(name = "course")
> public class CourseEntity {
>
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>
>     private String name;
>
>     private double price;
>
>     private int maxCapacity;
>
>     protected CourseEntity() {
>     }
>
>     public CourseEntity(String name, double price, int maxCapacity) {
>         this.name = name;
>         this.price = price;
>         this.maxCapacity = maxCapacity;
>     }
>
>     public Long getId() { return id; }
>     public String getName() { return name; }
>     public double getPrice() { return price; }
>     public int getMaxCapacity() { return maxCapacity; }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 EnrollmentEntity.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/persistence/entity/EnrollmentEntity.java`](academica-dtos/src/main/java/com/academy/persistence/entity/EnrollmentEntity.java)
>
> ```java
> package com.academy.persistence.entity;
>
> import jakarta.persistence.Entity;
> import jakarta.persistence.GeneratedValue;
> import jakarta.persistence.GenerationType;
> import jakarta.persistence.Id;
> import jakarta.persistence.JoinColumn;
> import jakarta.persistence.ManyToOne;
> import jakarta.persistence.Table;
>
> @Entity
> @Table(name = "enrollment")
> public class EnrollmentEntity {
>
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>
>     @ManyToOne
>     @JoinColumn(name = "student_id")
>     private StudentEntity student;
>
>     @ManyToOne
>     @JoinColumn(name = "course_id")
>     private CourseEntity course;
>
>     protected EnrollmentEntity() {
>     }
>
>     public EnrollmentEntity(StudentEntity student, CourseEntity course) {
>         this.student = student;
>         this.course = course;
>     }
>
>     public Long getId() { return id; }
>     public StudentEntity getStudent() { return student; }
>     public CourseEntity getCourse() { return course; }
> }
> ```
>
> </details>
>
> #### 📁 repository
>
> <details>
> <summary><strong>📄 StudentJpaRepository.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/persistence/repository/StudentJpaRepository.java`](academica-dtos/src/main/java/com/academy/persistence/repository/StudentJpaRepository.java)
>
> ```java
> package com.academy.persistence.repository;
>
> import com.academy.persistence.entity.StudentEntity;
> import org.springframework.data.jpa.repository.JpaRepository;
>
> public interface StudentJpaRepository extends JpaRepository<StudentEntity, Long> {
>
>     boolean existsByEmailIgnoreCase(String email);
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 CourseJpaRepository.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/persistence/repository/CourseJpaRepository.java`](academica-dtos/src/main/java/com/academy/persistence/repository/CourseJpaRepository.java)
>
> ```java
> package com.academy.persistence.repository;
>
> import com.academy.persistence.entity.CourseEntity;
> import org.springframework.data.jpa.repository.JpaRepository;
>
> public interface CourseJpaRepository extends JpaRepository<CourseEntity, Long> {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 EnrollmentJpaRepository.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/persistence/repository/EnrollmentJpaRepository.java`](academica-dtos/src/main/java/com/academy/persistence/repository/EnrollmentJpaRepository.java)
>
> ```java
> package com.academy.persistence.repository;
>
> import com.academy.persistence.entity.EnrollmentEntity;
> import org.springframework.data.jpa.repository.JpaRepository;
>
> public interface EnrollmentJpaRepository extends JpaRepository<EnrollmentEntity, Long> {
>
>     boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
>
>     long countByCourseId(Long courseId);
> }
> ```
>
> </details>
>
> ---
>
> ### 📁 business
>
> #### 📁 service
>
> <details>
> <summary><strong>📄 ServiceStatus.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/business/service/ServiceStatus.java`](academica-dtos/src/main/java/com/academy/business/service/ServiceStatus.java)
>
> ```java
> package com.academy.business.service;
>
> public enum ServiceStatus {
>     OK,
>     NOT_FOUND,
>     CONFLICT
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 ServiceResult.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/business/service/ServiceResult.java`](academica-dtos/src/main/java/com/academy/business/service/ServiceResult.java)
>
> ```java
> package com.academy.business.service;
>
> public record ServiceResult<T>(ServiceStatus status, T value) {
>
>     public static <T> ServiceResult<T> ok(T value) {
>         return new ServiceResult<>(ServiceStatus.OK, value);
>     }
>
>     public static <T> ServiceResult<T> notFound() {
>         return new ServiceResult<>(ServiceStatus.NOT_FOUND, null);
>     }
>
>     public static <T> ServiceResult<T> conflict() {
>         return new ServiceResult<>(ServiceStatus.CONFLICT, null);
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentService.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/business/service/StudentService.java`](academica-dtos/src/main/java/com/academy/business/service/StudentService.java)
>
> ```java
> package com.academy.business.service;
>
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.persistence.repository.StudentJpaRepository;
> import org.springframework.data.domain.Page;
> import org.springframework.data.domain.Pageable;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
>
> import java.util.Optional;
>
> @Service
> public class StudentService {
>
>     private final StudentJpaRepository students;
>
>     public StudentService(StudentJpaRepository students) {
>         this.students = students;
>     }
>
>     @Transactional(readOnly = true)
>     public Page<StudentEntity> findAll(Pageable pageable) {
>         return students.findAll(pageable);
>     }
>
>     @Transactional(readOnly = true)
>     public Optional<StudentEntity> findById(Long id) {
>         return students.findById(id);
>     }
>
>     @Transactional
>     public ServiceResult<StudentEntity> create(StudentEntity student) {
>         if (students.existsByEmailIgnoreCase(student.getEmail())) {
>             return ServiceResult.conflict();
>         }
>         return ServiceResult.ok(students.save(student));
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 CourseService.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/business/service/CourseService.java`](academica-dtos/src/main/java/com/academy/business/service/CourseService.java)
>
> ```java
> package com.academy.business.service;
>
> import com.academy.persistence.entity.CourseEntity;
> import com.academy.persistence.repository.CourseJpaRepository;
> import com.academy.persistence.repository.EnrollmentJpaRepository;
> import org.springframework.data.domain.Page;
> import org.springframework.data.domain.Pageable;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
>
> import java.util.Optional;
>
> @Service
> public class CourseService {
>
>     private final CourseJpaRepository courses;
>     private final EnrollmentJpaRepository enrollments;
>
>     public CourseService(CourseJpaRepository courses, EnrollmentJpaRepository enrollments) {
>         this.courses = courses;
>         this.enrollments = enrollments;
>     }
>
>     @Transactional(readOnly = true)
>     public Page<CourseEntity> findAll(Pageable pageable) {
>         return courses.findAll(pageable);
>     }
>
>     @Transactional(readOnly = true)
>     public Optional<CourseEntity> findById(Long id) {
>         return courses.findById(id);
>     }
>
>     @Transactional
>     public CourseEntity create(CourseEntity course) {
>         return courses.save(course);
>     }
>
>     @Transactional(readOnly = true)
>     public ServiceResult<Long> countEnrolled(Long courseId) {
>         if (!courses.existsById(courseId)) {
>             return ServiceResult.notFound();
>         }
>         return ServiceResult.ok(enrollments.countByCourseId(courseId));
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 EnrollmentService.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/business/service/EnrollmentService.java`](academica-dtos/src/main/java/com/academy/business/service/EnrollmentService.java)
>
> ```java
> package com.academy.business.service;
>
> import com.academy.persistence.entity.CourseEntity;
> import com.academy.persistence.entity.EnrollmentEntity;
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.persistence.repository.CourseJpaRepository;
> import com.academy.persistence.repository.EnrollmentJpaRepository;
> import com.academy.persistence.repository.StudentJpaRepository;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
>
> import java.util.Optional;
>
> @Service
> public class EnrollmentService {
>
>     private final StudentJpaRepository students;
>     private final CourseJpaRepository courses;
>     private final EnrollmentJpaRepository enrollments;
>
>     public EnrollmentService(StudentJpaRepository students, CourseJpaRepository courses,
>                              EnrollmentJpaRepository enrollments) {
>         this.students = students;
>         this.courses = courses;
>         this.enrollments = enrollments;
>     }
>
>     @Transactional(readOnly = true)
>     public Optional<EnrollmentEntity> findById(Long id) {
>         return enrollments.findById(id);
>     }
>
>     @Transactional
>     public ServiceResult<EnrollmentEntity> enroll(Long studentId, Long courseId) {
>         Optional<StudentEntity> student = students.findById(studentId);
>         Optional<CourseEntity> course = courses.findById(courseId);
>         if (student.isEmpty() || course.isEmpty()) {
>             return ServiceResult.notFound();
>         }
>         if (enrollments.existsByStudentIdAndCourseId(studentId, courseId)) {
>             return ServiceResult.conflict();
>         }
>         if (enrollments.countByCourseId(courseId) >= course.get().getMaxCapacity()) {
>             return ServiceResult.conflict();
>         }
>         return ServiceResult.ok(enrollments.save(new EnrollmentEntity(student.get(), course.get())));
>     }
> }
> ```
>
> </details>
>
> ---
>
> ### 📁 presentation
>
> #### 📁 dto
>
> <details>
> <summary><strong>📄 StudentRequest.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/dto/StudentRequest.java`](academica-dtos/src/main/java/com/academy/presentation/dto/StudentRequest.java)
>
> ```java
> package com.academy.presentation.dto;
>
> import jakarta.validation.constraints.Email;
> import jakarta.validation.constraints.Max;
> import jakarta.validation.constraints.Min;
> import jakarta.validation.constraints.NotBlank;
> import jakarta.validation.constraints.Size;
>
> public record StudentRequest(
>
>         @NotBlank
>         @Size(min = 2, max = 100)
>         String name,
>
>         @Min(16)
>         @Max(99)
>         int age,
>
>         @NotBlank
>         @Email
>         String email
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentResponse.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/dto/StudentResponse.java`](academica-dtos/src/main/java/com/academy/presentation/dto/StudentResponse.java)
>
> ```java
> package com.academy.presentation.dto;
>
> public record StudentResponse(Long id, String name, int age, String email) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 CourseRequest.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/dto/CourseRequest.java`](academica-dtos/src/main/java/com/academy/presentation/dto/CourseRequest.java)
>
> ```java
> package com.academy.presentation.dto;
>
> import jakarta.validation.constraints.Min;
> import jakarta.validation.constraints.NotBlank;
> import jakarta.validation.constraints.Positive;
> import jakarta.validation.constraints.Size;
>
> public record CourseRequest(
>
>         @NotBlank
>         @Size(min = 2, max = 100)
>         String name,
>
>         @Positive
>         double price,
>
>         @Min(1)
>         int maxCapacity
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 CourseResponse.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/dto/CourseResponse.java`](academica-dtos/src/main/java/com/academy/presentation/dto/CourseResponse.java)
>
> ```java
> package com.academy.presentation.dto;
>
> public record CourseResponse(Long id, String name, double price, int maxCapacity) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 EnrolledResponse.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/dto/EnrolledResponse.java`](academica-dtos/src/main/java/com/academy/presentation/dto/EnrolledResponse.java)
>
> ```java
> package com.academy.presentation.dto;
>
> public record EnrolledResponse(Long courseId, long enrolled) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 EnrollmentRequest.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/dto/EnrollmentRequest.java`](academica-dtos/src/main/java/com/academy/presentation/dto/EnrollmentRequest.java)
>
> ```java
> package com.academy.presentation.dto;
>
> import jakarta.validation.constraints.NotNull;
>
> public record EnrollmentRequest(
>
>         @NotNull
>         Long studentId,
>
>         @NotNull
>         Long courseId
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 EnrollmentResponse.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/dto/EnrollmentResponse.java`](academica-dtos/src/main/java/com/academy/presentation/dto/EnrollmentResponse.java)
>
> ```java
> package com.academy.presentation.dto;
>
> public record EnrollmentResponse(Long id, Long studentId, String studentName, Long courseId, String courseName) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 PageResponse.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/dto/PageResponse.java`](academica-dtos/src/main/java/com/academy/presentation/dto/PageResponse.java)
>
> ```java
> package com.academy.presentation.dto;
>
> import java.util.List;
>
> public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
> }
> ```
>
> </details>
>
> #### 📁 mapper
>
> <details>
> <summary><strong>📄 StudentMapper.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/mapper/StudentMapper.java`](academica-dtos/src/main/java/com/academy/presentation/mapper/StudentMapper.java)
>
> ```java
> package com.academy.presentation.mapper;
>
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.presentation.dto.PageResponse;
> import com.academy.presentation.dto.StudentRequest;
> import com.academy.presentation.dto.StudentResponse;
> import org.springframework.data.domain.Page;
>
> public final class StudentMapper {
>
>     private StudentMapper() {
>     }
>
>     public static StudentEntity toEntity(StudentRequest request) {
>         return new StudentEntity(request.name(), request.age(), request.email());
>     }
>
>     public static StudentResponse toResponse(StudentEntity entity) {
>         return new StudentResponse(entity.getId(), entity.getName(), entity.getAge(), entity.getEmail());
>     }
>
>     public static PageResponse<StudentResponse> toPageResponse(Page<StudentEntity> page) {
>         return new PageResponse<>(
>                 page.getContent().stream().map(StudentMapper::toResponse).toList(),
>                 page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 CourseMapper.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/mapper/CourseMapper.java`](academica-dtos/src/main/java/com/academy/presentation/mapper/CourseMapper.java)
>
> ```java
> package com.academy.presentation.mapper;
>
> import com.academy.persistence.entity.CourseEntity;
> import com.academy.presentation.dto.CourseRequest;
> import com.academy.presentation.dto.CourseResponse;
> import com.academy.presentation.dto.PageResponse;
> import org.springframework.data.domain.Page;
>
> public final class CourseMapper {
>
>     private CourseMapper() {
>     }
>
>     public static CourseEntity toEntity(CourseRequest request) {
>         return new CourseEntity(request.name(), request.price(), request.maxCapacity());
>     }
>
>     public static CourseResponse toResponse(CourseEntity entity) {
>         return new CourseResponse(entity.getId(), entity.getName(), entity.getPrice(), entity.getMaxCapacity());
>     }
>
>     public static PageResponse<CourseResponse> toPageResponse(Page<CourseEntity> page) {
>         return new PageResponse<>(
>                 page.getContent().stream().map(CourseMapper::toResponse).toList(),
>                 page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 EnrollmentMapper.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/mapper/EnrollmentMapper.java`](academica-dtos/src/main/java/com/academy/presentation/mapper/EnrollmentMapper.java)
>
> ```java
> package com.academy.presentation.mapper;
>
> import com.academy.persistence.entity.EnrollmentEntity;
> import com.academy.presentation.dto.EnrollmentResponse;
>
> public final class EnrollmentMapper {
>
>     private EnrollmentMapper() {
>     }
>
>     public static EnrollmentResponse toResponse(EnrollmentEntity entity) {
>         return new EnrollmentResponse(
>                 entity.getId(),
>                 entity.getStudent().getId(), entity.getStudent().getName(),
>                 entity.getCourse().getId(), entity.getCourse().getName());
>     }
> }
> ```
>
> </details>
>
> #### 📁 controller
>
> <details>
> <summary><strong>📄 StudentController.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/controller/StudentController.java`](academica-dtos/src/main/java/com/academy/presentation/controller/StudentController.java)
>
> ```java
> package com.academy.presentation.controller;
>
> import com.academy.business.service.ServiceResult;
> import com.academy.business.service.ServiceStatus;
> import com.academy.business.service.StudentService;
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.presentation.dto.PageResponse;
> import com.academy.presentation.dto.StudentRequest;
> import com.academy.presentation.dto.StudentResponse;
> import com.academy.presentation.mapper.StudentMapper;
> import jakarta.validation.Valid;
> import org.springframework.data.domain.Pageable;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PathVariable;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.RequestBody;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
>
> import java.net.URI;
>
> @RestController
> @RequestMapping("/api/v1/students")
> public class StudentController {
>
>     private final StudentService service;
>
>     public StudentController(StudentService service) {
>         this.service = service;
>     }
>
>     @GetMapping
>     public PageResponse<StudentResponse> findAll(Pageable pageable) {
>         return StudentMapper.toPageResponse(service.findAll(pageable));
>     }
>
>     @GetMapping("/{id}")
>     public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
>         return service.findById(id)
>                 .map(StudentMapper::toResponse)
>                 .map(ResponseEntity::ok)
>                 .orElseGet(() -> ResponseEntity.notFound().build());
>     }
>
>     @PostMapping
>     public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
>         ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
>         if (result.status() == ServiceStatus.CONFLICT) {
>             return ResponseEntity.status(HttpStatus.CONFLICT).build();
>         }
>         StudentResponse body = StudentMapper.toResponse(result.value());
>         URI location = ServletUriComponentsBuilder.fromCurrentRequest()
>                 .path("/{id}").buildAndExpand(body.id()).toUri();
>         return ResponseEntity.created(location).body(body);
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 CourseController.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/controller/CourseController.java`](academica-dtos/src/main/java/com/academy/presentation/controller/CourseController.java)
>
> ```java
> package com.academy.presentation.controller;
>
> import com.academy.business.service.CourseService;
> import com.academy.business.service.ServiceResult;
> import com.academy.persistence.entity.CourseEntity;
> import com.academy.presentation.dto.CourseRequest;
> import com.academy.presentation.dto.CourseResponse;
> import com.academy.presentation.dto.EnrolledResponse;
> import com.academy.presentation.dto.PageResponse;
> import com.academy.presentation.mapper.CourseMapper;
> import jakarta.validation.Valid;
> import org.springframework.data.domain.Pageable;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PathVariable;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.RequestBody;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
>
> import java.net.URI;
>
> @RestController
> @RequestMapping("/api/v1/courses")
> public class CourseController {
>
>     private final CourseService service;
>
>     public CourseController(CourseService service) {
>         this.service = service;
>     }
>
>     @GetMapping
>     public PageResponse<CourseResponse> findAll(Pageable pageable) {
>         return CourseMapper.toPageResponse(service.findAll(pageable));
>     }
>
>     @GetMapping("/{id}")
>     public ResponseEntity<CourseResponse> findById(@PathVariable Long id) {
>         return service.findById(id)
>                 .map(CourseMapper::toResponse)
>                 .map(ResponseEntity::ok)
>                 .orElseGet(() -> ResponseEntity.notFound().build());
>     }
>
>     @PostMapping
>     public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
>         CourseEntity created = service.create(CourseMapper.toEntity(request));
>         CourseResponse body = CourseMapper.toResponse(created);
>         URI location = ServletUriComponentsBuilder.fromCurrentRequest()
>                 .path("/{id}").buildAndExpand(body.id()).toUri();
>         return ResponseEntity.created(location).body(body);
>     }
>
>     @GetMapping("/{id}/enrolled")
>     public ResponseEntity<EnrolledResponse> countEnrolled(@PathVariable Long id) {
>         ServiceResult<Long> result = service.countEnrolled(id);
>         return switch (result.status()) {
>             case OK -> ResponseEntity.ok(new EnrolledResponse(id, result.value()));
>             case NOT_FOUND, CONFLICT -> ResponseEntity.notFound().build();
>         };
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 EnrollmentController.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/controller/EnrollmentController.java`](academica-dtos/src/main/java/com/academy/presentation/controller/EnrollmentController.java)
>
> ```java
> package com.academy.presentation.controller;
>
> import com.academy.business.service.EnrollmentService;
> import com.academy.business.service.ServiceResult;
> import com.academy.persistence.entity.EnrollmentEntity;
> import com.academy.presentation.dto.EnrollmentRequest;
> import com.academy.presentation.dto.EnrollmentResponse;
> import com.academy.presentation.mapper.EnrollmentMapper;
> import jakarta.validation.Valid;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PathVariable;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.RequestBody;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
>
> import java.net.URI;
>
> @RestController
> @RequestMapping("/api/v1/enrollments")
> public class EnrollmentController {
>
>     private final EnrollmentService service;
>
>     public EnrollmentController(EnrollmentService service) {
>         this.service = service;
>     }
>
>     @GetMapping("/{id}")
>     public ResponseEntity<EnrollmentResponse> findById(@PathVariable Long id) {
>         return service.findById(id)
>                 .map(EnrollmentMapper::toResponse)
>                 .map(ResponseEntity::ok)
>                 .orElseGet(() -> ResponseEntity.notFound().build());
>     }
>
>     @PostMapping
>     public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
>         ServiceResult<EnrollmentEntity> result = service.enroll(request.studentId(), request.courseId());
>         return switch (result.status()) {
>             case NOT_FOUND -> ResponseEntity.notFound().build();
>             case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
>             case OK -> {
>                 EnrollmentResponse body = EnrollmentMapper.toResponse(result.value());
>                 URI location = ServletUriComponentsBuilder.fromCurrentRequest()
>                         .path("/{id}").buildAndExpand(body.id()).toUri();
>                 yield ResponseEntity.created(location).body(body);
>             }
>         };
>     }
> }
> ```
>
> </details>

</details>

---

<details>
<summary><strong>🧪 Paso 7 — Probar la API</strong></summary>

> <details>
> <summary><strong>Paso 7.1 · Las pruebas con <code>curl</code></strong></summary>
>
> > Se arranca la aplicación con MySQL en marcha (`./mvnw spring-boot:run`) y se ejecuta cada comando en otra terminal. Debajo de cada uno está la **respuesta real**.
> >
> > > 💡 La API devuelve el JSON en una sola línea; aquí se muestra con sangría para leerlo mejor. Los `404` y `409` del controlador llegan **sin cuerpo**; los `400` de validación traen el cuerpo genérico de Spring Boot.
> >
> > <details>
> > <summary><strong>1. Estudiantes · crear → <code>201</code> y la cabecera <code>Location</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `StudentController.create(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >     ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> >     if (result.status() == ServiceStatus.CONFLICT) {
> >         return ResponseEntity.status(HttpStatus.CONFLICT).build();
> >     }
> >     StudentResponse body = StudentMapper.toResponse(result.value());
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > Entra por `create`: valida el cuerpo (`@Valid`), lo convierte a entidad con el mapeador y responde `201` con `Location`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"Ana Torres","age":22,"email":"ana@mail.com"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 201
> > Location: http://localhost:8080/api/v1/students/1
> > Content-Type: application/json
> >
> > {
> >   "id": 1,
> >   "name": "Ana Torres",
> >   "age": 22,
> >   "email": "ana@mail.com"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>2. Estudiantes · crear enviando campos de más (<code>id</code> y <code>createdAt</code>) → <code>201</code>; ambos se ignoran</strong></summary>
> >
> > **Método del controlador que atiende la petición:** `StudentController.create(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >     ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> >     if (result.status() == ServiceStatus.CONFLICT) {
> >         return ResponseEntity.status(HttpStatus.CONFLICT).build();
> >     }
> >     StudentResponse body = StudentMapper.toResponse(result.value());
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > `StudentRequest` no tiene `id` ni `createdAt`: Jackson ignora lo que no existe en el `record`. Se contrasta con el Paso 1.2, donde el `id` forjado **pisaba** a Ana.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"id":99,"name":"Luis Pérez","age":25,"email":"luis@mail.com","createdAt":"2000-01-01T00:00:00"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 201
> > Location: http://localhost:8080/api/v1/students/2
> > Content-Type: application/json
> >
> > {
> >   "id": 2,
> >   "name": "Luis Pérez",
> >   "age": 25,
> >   "email": "luis@mail.com"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>3. Estudiantes · correo repetido, en mayúsculas → <code>409</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `StudentController.create(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >     ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> >     if (result.status() == ServiceStatus.CONFLICT) {
> >         return ResponseEntity.status(HttpStatus.CONFLICT).build();
> >     }
> >     StudentResponse body = StudentMapper.toResponse(result.value());
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > `StudentService.create` detecta el correo repetido y devuelve `CONFLICT`; `create` lo traduce a `409`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"Otra","age":30,"email":"ANA@MAIL.COM"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 409
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>4. Estudiantes · datos inválidos → <code>400</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `StudentController.create(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >     ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> >     if (result.status() == ServiceStatus.CONFLICT) {
> >         return ResponseEntity.status(HttpStatus.CONFLICT).build();
> >     }
> >     StudentResponse body = StudentMapper.toResponse(result.value());
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > `@Valid` falla **antes** de ejecutar el cuerpo de `create`: Spring responde `400` y el método no llega a correr.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"","age":5,"email":"x"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 400
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-01T20:15:55.151+00:00",
> >   "status": 400,
> >   "error": "Bad Request",
> >   "path": "/api/v1/students"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>5. Estudiantes · listar con tamaño excesivo y orden → <code>200</code> con <code>"size":50</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `StudentController.findAll(...)`
> >
> > ```java
> > @GetMapping
> > public PageResponse<StudentResponse> findAll(Pageable pageable) {
> >     return StudentMapper.toPageResponse(service.findAll(pageable));
> > }
> > ```
> >
> > Entra por `findAll`, que arma una `PageResponse`. El tope de 50 lo aplica Spring antes, con `spring.data.web.pageable.max-page-size`.
> >
> > ```bash
> > curl -i "http://localhost:8080/api/v1/students?size=1000&sort=name,asc"
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "content": [
> >     {
> >       "id": 1,
> >       "name": "Ana Torres",
> >       "age": 22,
> >       "email": "ana@mail.com"
> >     },
> >     {
> >       "id": 2,
> >       "name": "Luis Pérez",
> >       "age": 25,
> >       "email": "luis@mail.com"
> >     }
> >   ],
> >   "page": 0,
> >   "size": 50,
> >   "totalElements": 2,
> >   "totalPages": 1
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>6. Estudiantes · buscar por id → <code>200</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `StudentController.findById(...)`
> >
> > ```java
> > @GetMapping("/{id}")
> > public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
> >     return service.findById(id)
> >             .map(StudentMapper::toResponse)
> >             .map(ResponseEntity::ok)
> >             .orElseGet(() -> ResponseEntity.notFound().build());
> > }
> > ```
> >
> > Entra por `findById`: el `Optional` con entidad pasa por el mapeador y se envuelve en `200`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/students/1
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "id": 1,
> >   "name": "Ana Torres",
> >   "age": 22,
> >   "email": "ana@mail.com"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>7. Estudiantes · buscar un id que no existe → <code>404</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `StudentController.findById(...)`
> >
> > ```java
> > @GetMapping("/{id}")
> > public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
> >     return service.findById(id)
> >             .map(StudentMapper::toResponse)
> >             .map(ResponseEntity::ok)
> >             .orElseGet(() -> ResponseEntity.notFound().build());
> > }
> > ```
> >
> > El `Optional` está vacío y `orElseGet` responde `404`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/students/99
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>8. Cursos · crear → <code>201</code> y la cabecera <code>Location</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `CourseController.create(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
> >     CourseEntity created = service.create(CourseMapper.toEntity(request));
> >     CourseResponse body = CourseMapper.toResponse(created);
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > Entra por `create`. El precio y el cupo ya se validaron en `CourseRequest`; el servicio solo guarda.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/courses \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"Java from scratch","price":150000,"maxCapacity":1}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 201
> > Location: http://localhost:8080/api/v1/courses/1
> > Content-Type: application/json
> >
> > {
> >   "id": 1,
> >   "name": "Java from scratch",
> >   "price": 150000.0,
> >   "maxCapacity": 1
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>9. Cursos · precio <code>0</code> y cupo <code>0</code> → <code>400</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `CourseController.create(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
> >     CourseEntity created = service.create(CourseMapper.toEntity(request));
> >     CourseResponse body = CourseMapper.toResponse(created);
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > `@Positive` y `@Min(1)` fallan en `CourseRequest`. Antes esta regla vivía en el servicio y respondía `409`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/courses \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"X","price":0,"maxCapacity":0}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 400
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-01T20:15:55.257+00:00",
> >   "status": 400,
> >   "error": "Bad Request",
> >   "path": "/api/v1/courses"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>10. Cursos · listar → <code>200</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `CourseController.findAll(...)`
> >
> > ```java
> > @GetMapping
> > public PageResponse<CourseResponse> findAll(Pageable pageable) {
> >     return CourseMapper.toPageResponse(service.findAll(pageable));
> > }
> > ```
> >
> > Entra por `findAll`: devuelve la misma `PageResponse` que los estudiantes, ahora con `CourseResponse`.
> >
> > ```bash
> > curl -i "http://localhost:8080/api/v1/courses?page=0&size=5"
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "content": [
> >     {
> >       "id": 1,
> >       "name": "Java from scratch",
> >       "price": 150000.0,
> >       "maxCapacity": 1
> >     }
> >   ],
> >   "page": 0,
> >   "size": 5,
> >   "totalElements": 1,
> >   "totalPages": 1
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>11. Cursos · cuántos matriculados → <code>200</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `CourseController.countEnrolled(...)`
> >
> > ```java
> > @GetMapping("/{id}/enrolled")
> > public ResponseEntity<EnrolledResponse> countEnrolled(@PathVariable Long id) {
> >     ServiceResult<Long> result = service.countEnrolled(id);
> >     return switch (result.status()) {
> >         case OK -> ResponseEntity.ok(new EnrolledResponse(id, result.value()));
> >         case NOT_FOUND, CONFLICT -> ResponseEntity.notFound().build();
> >     };
> > }
> > ```
> >
> > Entra por `countEnrolled`: el servicio devuelve `ServiceResult<Long>` y el controlador lo envuelve en un objeto JSON (`EnrolledResponse`), no en un número suelto.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/courses/1/enrolled
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "courseId": 1,
> >   "enrolled": 0
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>12. Cursos · conteo de un curso que no existe → <code>404</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `CourseController.countEnrolled(...)`
> >
> > ```java
> > @GetMapping("/{id}/enrolled")
> > public ResponseEntity<EnrolledResponse> countEnrolled(@PathVariable Long id) {
> >     ServiceResult<Long> result = service.countEnrolled(id);
> >     return switch (result.status()) {
> >         case OK -> ResponseEntity.ok(new EnrolledResponse(id, result.value()));
> >         case NOT_FOUND, CONFLICT -> ResponseEntity.notFound().build();
> >     };
> > }
> > ```
> >
> > El servicio devuelve `NOT_FOUND` y el `switch` lo traduce a `404`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/courses/9/enrolled
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>13. Matrículas · matricular → <code>201</code> y la cabecera <code>Location</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `EnrollmentController.enroll(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
> >     ServiceResult<EnrollmentEntity> result = service.enroll(request.studentId(), request.courseId());
> >     return switch (result.status()) {
> >         case NOT_FOUND -> ResponseEntity.notFound().build();
> >         case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
> >         case OK -> {
> >             EnrollmentResponse body = EnrollmentMapper.toResponse(result.value());
> >             URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                     .path("/{id}").buildAndExpand(body.id()).toUri();
> >             yield ResponseEntity.created(location).body(body);
> >         }
> >     };
> > }
> > ```
> >
> > Entra por `enroll`: los ids viajan en el **cuerpo** (`EnrollmentRequest`) y la respuesta es plana (`EnrollmentResponse`), no dos entidades anidadas.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/enrollments \
> >      -H "Content-Type: application/json" \
> >      -d '{"studentId":1,"courseId":1}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 201
> > Location: http://localhost:8080/api/v1/enrollments/1
> > Content-Type: application/json
> >
> > {
> >   "id": 1,
> >   "studentId": 1,
> >   "studentName": "Ana Torres",
> >   "courseId": 1,
> >   "courseName": "Java from scratch"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>14. Matrículas · la misma matrícula otra vez → <code>409</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `EnrollmentController.enroll(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
> >     ServiceResult<EnrollmentEntity> result = service.enroll(request.studentId(), request.courseId());
> >     return switch (result.status()) {
> >         case NOT_FOUND -> ResponseEntity.notFound().build();
> >         case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
> >         case OK -> {
> >             EnrollmentResponse body = EnrollmentMapper.toResponse(result.value());
> >             URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                     .path("/{id}").buildAndExpand(body.id()).toUri();
> >             yield ResponseEntity.created(location).body(body);
> >         }
> >     };
> > }
> > ```
> >
> > El servicio devuelve `CONFLICT` (ya existe) y el `switch` responde `409`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/enrollments \
> >      -H "Content-Type: application/json" \
> >      -d '{"studentId":1,"courseId":1}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 409
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>15. Matrículas · curso sin cupo → <code>409</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `EnrollmentController.enroll(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
> >     ServiceResult<EnrollmentEntity> result = service.enroll(request.studentId(), request.courseId());
> >     return switch (result.status()) {
> >         case NOT_FOUND -> ResponseEntity.notFound().build();
> >         case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
> >         case OK -> {
> >             EnrollmentResponse body = EnrollmentMapper.toResponse(result.value());
> >             URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                     .path("/{id}").buildAndExpand(body.id()).toUri();
> >             yield ResponseEntity.created(location).body(body);
> >         }
> >     };
> > }
> > ```
> >
> > El curso tiene cupo `1` y ya está ocupado: `CONFLICT` → `409`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/enrollments \
> >      -H "Content-Type: application/json" \
> >      -d '{"studentId":2,"courseId":1}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 409
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>16. Matrículas · estudiante inexistente → <code>404</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `EnrollmentController.enroll(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
> >     ServiceResult<EnrollmentEntity> result = service.enroll(request.studentId(), request.courseId());
> >     return switch (result.status()) {
> >         case NOT_FOUND -> ResponseEntity.notFound().build();
> >         case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
> >         case OK -> {
> >             EnrollmentResponse body = EnrollmentMapper.toResponse(result.value());
> >             URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                     .path("/{id}").buildAndExpand(body.id()).toUri();
> >             yield ResponseEntity.created(location).body(body);
> >         }
> >     };
> > }
> > ```
> >
> > Ahora el servicio **distingue** el caso: `NOT_FOUND` → `404`. En la versión anterior era un `409` igual al resto.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/enrollments \
> >      -H "Content-Type: application/json" \
> >      -d '{"studentId":9,"courseId":1}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>17. Matrículas · falta un campo → <code>400</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `EnrollmentController.enroll(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
> >     ServiceResult<EnrollmentEntity> result = service.enroll(request.studentId(), request.courseId());
> >     return switch (result.status()) {
> >         case NOT_FOUND -> ResponseEntity.notFound().build();
> >         case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
> >         case OK -> {
> >             EnrollmentResponse body = EnrollmentMapper.toResponse(result.value());
> >             URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                     .path("/{id}").buildAndExpand(body.id()).toUri();
> >             yield ResponseEntity.created(location).body(body);
> >         }
> >     };
> > }
> > ```
> >
> > `@NotNull` en `studentId` falla antes de ejecutar `enroll`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/enrollments \
> >      -H "Content-Type: application/json" \
> >      -d '{"courseId":1}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 400
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-01T20:15:55.539+00:00",
> >   "status": 400,
> >   "error": "Bad Request",
> >   "path": "/api/v1/enrollments"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>18. Matrículas · buscar por id → <code>200</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `EnrollmentController.findById(...)`
> >
> > ```java
> > @GetMapping("/{id}")
> > public ResponseEntity<EnrollmentResponse> findById(@PathVariable Long id) {
> >     return service.findById(id)
> >             .map(EnrollmentMapper::toResponse)
> >             .map(ResponseEntity::ok)
> >             .orElseGet(() -> ResponseEntity.notFound().build());
> > }
> > ```
> >
> > Es el destino de la cabecera `Location` del `201`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/enrollments/1
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "id": 1,
> >   "studentId": 1,
> >   "studentName": "Ana Torres",
> >   "courseId": 1,
> >   "courseName": "Java from scratch"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>19. Matrículas · buscar un id que no existe → <code>404</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `EnrollmentController.findById(...)`
> >
> > ```java
> > @GetMapping("/{id}")
> > public ResponseEntity<EnrollmentResponse> findById(@PathVariable Long id) {
> >     return service.findById(id)
> >             .map(EnrollmentMapper::toResponse)
> >             .map(ResponseEntity::ok)
> >             .orElseGet(() -> ResponseEntity.notFound().build());
> > }
> > ```
> >
> > El `Optional` está vacío: `404`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/enrollments/99
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>20. La ruta sin versión ya no existe → <code>404</code></strong></summary>
> >
> > **Método del controlador:** ninguno.
> >
> > No hay ningún controlador en `/api/students`: el `404` lo genera Spring antes de llegar a `StudentController`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/students
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-01T20:15:55.586+00:00",
> >   "status": 404,
> >   "error": "Not Found",
> >   "path": "/api/students"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>21. Cursos · el conteo después de matricular → <code>200</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `CourseController.countEnrolled(...)`
> >
> > ```java
> > @GetMapping("/{id}/enrolled")
> > public ResponseEntity<EnrolledResponse> countEnrolled(@PathVariable Long id) {
> >     ServiceResult<Long> result = service.countEnrolled(id);
> >     return switch (result.status()) {
> >         case OK -> ResponseEntity.ok(new EnrolledResponse(id, result.value()));
> >         case NOT_FOUND, CONFLICT -> ResponseEntity.notFound().build();
> >     };
> > }
> > ```
> >
> > Vuelve a `countEnrolled`: ahora hay una matrícula.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/courses/1/enrolled
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "courseId": 1,
> >   "enrolled": 1
> > }
> > ```
> >
> > </details>
> >
> > > 💡 **Beneficio de este paso:** se comprueba el **contrato** completo y se ve cómo cada método del controlador traduce un resultado de negocio a un código HTTP.
> > >
> > > En MySQL, la columna interna sigue existiendo aunque la API no la muestre:
> > >
> > > ```text
> > > mysql> select id, name, created_at is not null as has_created_at from student;
> > > +----+------------+----------------+
> > > | id | name       | has_created_at |
> > > +----+------------+----------------+
> > > |  1 | Ana Torres |              1 |
> > > |  2 | Luis Pérez |              1 |
> > > +----+------------+----------------+
> > > ```
> > >
> > > 🗣️ **Preguntas:** ¿qué pasó con el `"id":99` que mandó el cliente? *(Nunca llegó: `StudentRequest` no tiene `id`.)* ¿Por qué el estudiante inexistente da `404` y antes daba `409`? *(Ahora el servicio devuelve `NOT_FOUND` y el controlador lo traduce.)*
> > >
> > > ✅ **Punto de control 7:** las respuestas coinciden con las que se muestran bajo cada comando.
>
> </details>
>

</details>

---

➡️ **Guía completa:** [02-academica-dtos.md](02-academica-dtos.md)
