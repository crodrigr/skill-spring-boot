# 📘 Academia con Swagger — Código completo

> 🧾 **Solo el código.** Versión final de la [guía `02-academica-swagger`](02-academica-swagger.md), con el árbol (🆕 nuevo · ✏️ cambia) y el código completo sin explicaciones, seguido del **Paso 11 — Probar la documentación**. Cada archivo es un bloque que se despliega. Los grupos van en **orden de dependencia**: cada archivo aparece después de lo que necesita.
>
> 📦 **Código fuente:** [`academica-swagger/`](codigo/academica-swagger/) (proyecto Maven completo; cada archivo de abajo enlaza a su original) · ✅ compilado para Java 21 y ejecutado con Spring Boot 4.1.2, springdoc-openapi 3.2.0 y MySQL 8.

<details>
<summary><strong>📁 Estructura final esperada</strong></summary>

> ```text
> 📁 academica-swagger
> ├── ✏️ pom.xml                                   + springdoc-openapi-starter-webmvc-ui
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 presentation
>     │   │   ├── 📁 config
>     │   │   │   └── 🆕 OpenApiConfig.java
>     │   │   ├── 📁 controller
>     │   │   │   ├── ✏️ StudentController.java
>     │   │   │   ├── ✏️ CourseController.java
>     │   │   │   ├── ✏️ EnrollmentController.java
>     │   │   │   └── 🆕 InternalController.java
>     │   │   ├── 📁 dto
>     │   │   │   ├── ✏️ StudentRequest.java
>     │   │   │   ├── ✏️ StudentResponse.java
>     │   │   │   ├── ✏️ CourseRequest.java
>     │   │   │   ├── ✏️ CourseResponse.java
>     │   │   │   ├── ✏️ EnrolledResponse.java
>     │   │   │   ├── ✏️ EnrollmentRequest.java
>     │   │   │   ├── ✏️ EnrollmentResponse.java
>     │   │   │   └── ✏️ PageResponse.java
>     │   │   └── 📁 mapper
>     │   │       ├── 📄 StudentMapper.java
>     │   │       ├── 📄 CourseMapper.java
>     │   │       └── 📄 EnrollmentMapper.java
>     │   ├── 📁 business
>     │   │   ├── 📁 service
>     │   │   │   ├── ✏️ StudentService.java          + findByEmail
>     │   │   │   ├── 📄 CourseService.java
>     │   │   │   ├── 📄 EnrollmentService.java
>     │   │   │   ├── 📄 ServiceResult.java
>     │   │   │   └── 📄 ServiceStatus.java
>     │   │   └── 📁 impl
>     │   │       ├── ✏️ StudentServiceImpl.java          + findByEmail
>     │   │       ├── 📄 CourseServiceImpl.java
>     │   │       └── 📄 EnrollmentServiceImpl.java
>     │   └── 📁 persistence
>     │       ├── 📁 entity
>     │       │   ├── 📄 StudentEntity.java
>     │       │   ├── 📄 CourseEntity.java
>     │       │   └── 📄 EnrollmentEntity.java
>     │       └── 📁 repository
>     │           ├── ✏️ StudentJpaRepository.java    + findByEmailIgnoreCase
>     │           ├── 📄 CourseJpaRepository.java
>     │           └── 📄 EnrollmentJpaRepository.java
>     └── 📁 resources
>         ├── ✏️ application.properties             + propiedades de springdoc
>         └── 🆕 application-prod.properties
> ```
>
> 🆕 archivo nuevo · ✏️ archivo de `academica-dtos` (Módulo 05) que cambia · 📄 sin cambios respecto a `academica-dtos`
>
> **Código final, paso a paso**
>
> ### 📁 Configuración
>
> <details>
> <summary><strong>📄 pom.xml</strong></summary>
>
> **Ruta:** [`pom.xml`](codigo/academica-swagger/pom.xml)
>
> ```xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>4.1.2</version>
>         <relativePath/>
>     </parent>
>     <groupId>com.academy</groupId>
>     <artifactId>academy-swagger</artifactId>
>     <version>0.0.1-SNAPSHOT</version>
>     <name>academy</name>
>     <description>Taller de gestión de cursos con base de datos</description>
>     <properties>
>         <java.version>21</java.version>
>     </properties>
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-webmvc</artifactId>
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
>             <groupId>org.springdoc</groupId>
>             <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
>             <version>3.2.0</version>
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
> **Ruta:** [`src/main/resources/application.properties`](codigo/academica-swagger/src/main/resources/application.properties)
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
>
> # Swagger (springdoc)
> springdoc.api-docs.path=/api-docs
> springdoc.swagger-ui.path=/swagger-ui.html
> springdoc.swagger-ui.urls-primary-name=Academy
> springdoc.swagger-ui.deep-linking=true
> springdoc.swagger-ui.operations-sorter=method
> springdoc.swagger-ui.tags-sorter=alpha
> springdoc.swagger-ui.display-request-duration=true
> springdoc.swagger-ui.doc-expansion=list
> springdoc.swagger-ui.try-it-out-enabled=true
> springdoc.default-produces-media-type=application/json
> springdoc.default-consumes-media-type=application/json
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 application-prod.properties</strong></summary>
>
> **Ruta:** [`src/main/resources/application-prod.properties`](codigo/academica-swagger/src/main/resources/application-prod.properties)
>
> ```properties
> springdoc.api-docs.enabled=false
> springdoc.swagger-ui.enabled=false
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 AcademyApplication.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/AcademyApplication.java`](codigo/academica-swagger/src/main/java/com/academy/AcademyApplication.java)
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
> **Ruta:** [`src/main/java/com/academy/persistence/entity/StudentEntity.java`](codigo/academica-swagger/src/main/java/com/academy/persistence/entity/StudentEntity.java)
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
> **Ruta:** [`src/main/java/com/academy/persistence/entity/CourseEntity.java`](codigo/academica-swagger/src/main/java/com/academy/persistence/entity/CourseEntity.java)
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
> **Ruta:** [`src/main/java/com/academy/persistence/entity/EnrollmentEntity.java`](codigo/academica-swagger/src/main/java/com/academy/persistence/entity/EnrollmentEntity.java)
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
> **Ruta:** [`src/main/java/com/academy/persistence/repository/StudentJpaRepository.java`](codigo/academica-swagger/src/main/java/com/academy/persistence/repository/StudentJpaRepository.java)
>
> ```java
> package com.academy.persistence.repository;
>
> import com.academy.persistence.entity.StudentEntity;
> import org.springframework.data.jpa.repository.JpaRepository;
>
> import java.util.Optional;
>
> public interface StudentJpaRepository extends JpaRepository<StudentEntity, Long> {
>
>     Optional<StudentEntity> findByEmailIgnoreCase(String email);
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
> **Ruta:** [`src/main/java/com/academy/persistence/repository/CourseJpaRepository.java`](codigo/academica-swagger/src/main/java/com/academy/persistence/repository/CourseJpaRepository.java)
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
> **Ruta:** [`src/main/java/com/academy/persistence/repository/EnrollmentJpaRepository.java`](codigo/academica-swagger/src/main/java/com/academy/persistence/repository/EnrollmentJpaRepository.java)
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
> **Ruta:** [`src/main/java/com/academy/business/service/ServiceStatus.java`](codigo/academica-swagger/src/main/java/com/academy/business/service/ServiceStatus.java)
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
> **Ruta:** [`src/main/java/com/academy/business/service/ServiceResult.java`](codigo/academica-swagger/src/main/java/com/academy/business/service/ServiceResult.java)
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
> <summary><strong>📄 StudentService.java</strong> <em>(interfaz)</em></summary>
>
> **Ruta:** [`src/main/java/com/academy/business/service/StudentService.java`](codigo/academica-swagger/src/main/java/com/academy/business/service/StudentService.java)
>
> ```java
> package com.academy.business.service;
>
> import com.academy.persistence.entity.StudentEntity;
> import org.springframework.data.domain.Page;
> import org.springframework.data.domain.Pageable;
>
> import java.util.Optional;
>
> public interface StudentService {
>
>     Page<StudentEntity> findAll(Pageable pageable);
>
>     Optional<StudentEntity> findById(Long id);
>
>     Optional<StudentEntity> findByEmail(String email);
>
>     ServiceResult<StudentEntity> create(StudentEntity student);
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 CourseService.java</strong> <em>(interfaz)</em></summary>
>
> **Ruta:** [`src/main/java/com/academy/business/service/CourseService.java`](codigo/academica-swagger/src/main/java/com/academy/business/service/CourseService.java)
>
> ```java
> package com.academy.business.service;
>
> import com.academy.persistence.entity.CourseEntity;
> import org.springframework.data.domain.Page;
> import org.springframework.data.domain.Pageable;
>
> import java.util.Optional;
>
> public interface CourseService {
>
>     Page<CourseEntity> findAll(Pageable pageable);
>
>     Optional<CourseEntity> findById(Long id);
>
>     CourseEntity create(CourseEntity course);
>
>     ServiceResult<Long> countEnrolled(Long courseId);
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 EnrollmentService.java</strong> <em>(interfaz)</em></summary>
>
> **Ruta:** [`src/main/java/com/academy/business/service/EnrollmentService.java`](codigo/academica-swagger/src/main/java/com/academy/business/service/EnrollmentService.java)
>
> ```java
> package com.academy.business.service;
>
> import com.academy.persistence.entity.EnrollmentEntity;
>
> import java.util.Optional;
>
> public interface EnrollmentService {
>
>     Optional<EnrollmentEntity> findById(Long id);
>
>     ServiceResult<EnrollmentEntity> enroll(Long studentId, Long courseId);
> }
> ```
>
> </details>
>
> #### 📁 impl
>
> > 💡 Cada servicio es una `interface` en `business/service`; su clase `@Service` vive en `business/impl` con el sufijo `Impl`.
>
> <details>
> <summary><strong>📄 StudentServiceImpl.java</strong> <em>(implementación)</em></summary>
>
> **Ruta:** [`src/main/java/com/academy/business/impl/StudentServiceImpl.java`](codigo/academica-swagger/src/main/java/com/academy/business/impl/StudentServiceImpl.java)
>
> ```java
> package com.academy.business.impl;
>
> import com.academy.business.service.ServiceResult;
> import com.academy.business.service.StudentService;
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
> public class StudentServiceImpl implements StudentService {
>
>     private final StudentJpaRepository students;
>
>     public StudentServiceImpl(StudentJpaRepository students) {
>         this.students = students;
>     }
>
>     @Override
>     @Transactional(readOnly = true)
>     public Page<StudentEntity> findAll(Pageable pageable) {
>         return students.findAll(pageable);
>     }
>
>     @Override
>     @Transactional(readOnly = true)
>     public Optional<StudentEntity> findById(Long id) {
>         return students.findById(id);
>     }
>
>     @Override
>     @Transactional(readOnly = true)
>     public Optional<StudentEntity> findByEmail(String email) {
>         return students.findByEmailIgnoreCase(email);
>     }
>
>     @Override
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
> <summary><strong>📄 CourseServiceImpl.java</strong> <em>(implementación)</em></summary>
>
> **Ruta:** [`src/main/java/com/academy/business/impl/CourseServiceImpl.java`](codigo/academica-swagger/src/main/java/com/academy/business/impl/CourseServiceImpl.java)
>
> ```java
> package com.academy.business.impl;
>
> import com.academy.business.service.CourseService;
> import com.academy.business.service.ServiceResult;
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
> public class CourseServiceImpl implements CourseService {
>
>     private final CourseJpaRepository courses;
>     private final EnrollmentJpaRepository enrollments;
>
>     public CourseServiceImpl(CourseJpaRepository courses, EnrollmentJpaRepository enrollments) {
>         this.courses = courses;
>         this.enrollments = enrollments;
>     }
>
>     @Override
>     @Transactional(readOnly = true)
>     public Page<CourseEntity> findAll(Pageable pageable) {
>         return courses.findAll(pageable);
>     }
>
>     @Override
>     @Transactional(readOnly = true)
>     public Optional<CourseEntity> findById(Long id) {
>         return courses.findById(id);
>     }
>
>     @Override
>     @Transactional
>     public CourseEntity create(CourseEntity course) {
>         return courses.save(course);
>     }
>
>     @Override
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
> <summary><strong>📄 EnrollmentServiceImpl.java</strong> <em>(implementación)</em></summary>
>
> **Ruta:** [`src/main/java/com/academy/business/impl/EnrollmentServiceImpl.java`](codigo/academica-swagger/src/main/java/com/academy/business/impl/EnrollmentServiceImpl.java)
>
> ```java
> package com.academy.business.impl;
>
> import com.academy.business.service.EnrollmentService;
> import com.academy.business.service.ServiceResult;
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
> public class EnrollmentServiceImpl implements EnrollmentService {
>
>     private final StudentJpaRepository students;
>     private final CourseJpaRepository courses;
>     private final EnrollmentJpaRepository enrollments;
>
>     public EnrollmentServiceImpl(StudentJpaRepository students, CourseJpaRepository courses,
>                                  EnrollmentJpaRepository enrollments) {
>         this.students = students;
>         this.courses = courses;
>         this.enrollments = enrollments;
>     }
>
>     @Override
>     @Transactional(readOnly = true)
>     public Optional<EnrollmentEntity> findById(Long id) {
>         return enrollments.findById(id);
>     }
>
>     @Override
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
> **Ruta:** [`src/main/java/com/academy/presentation/dto/StudentRequest.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/dto/StudentRequest.java)
>
> ```java
> package com.academy.presentation.dto;
>
> import io.swagger.v3.oas.annotations.media.Schema;
> import jakarta.validation.constraints.Email;
> import jakarta.validation.constraints.Max;
> import jakarta.validation.constraints.Min;
> import jakarta.validation.constraints.NotBlank;
> import jakarta.validation.constraints.Size;
>
> @Schema(description = "Datos que el cliente envía para registrar un estudiante")
> public record StudentRequest(
>
>         @Schema(description = "Nombre completo", example = "Ana Torres")
>         @NotBlank
>         @Size(min = 2, max = 100)
>         String name,
>
>         @Schema(description = "Edad en años", example = "22")
>         @Min(16)
>         @Max(99)
>         int age,
>
>         @Schema(description = "Correo electrónico, único entre estudiantes", example = "ana@mail.com")
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
> **Ruta:** [`src/main/java/com/academy/presentation/dto/StudentResponse.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/dto/StudentResponse.java)
>
> ```java
> package com.academy.presentation.dto;
>
> import io.swagger.v3.oas.annotations.media.Schema;
>
> @Schema(description = "Estudiante tal como lo ve el cliente")
> public record StudentResponse(
>
>         @Schema(description = "Identificador generado por la base", example = "1")
>         Long id,
>
>         @Schema(description = "Nombre completo", example = "Ana Torres")
>         String name,
>
>         @Schema(description = "Edad en años", example = "22")
>         int age,
>
>         @Schema(description = "Correo electrónico", example = "ana@mail.com")
>         String email
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 CourseRequest.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/dto/CourseRequest.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/dto/CourseRequest.java)
>
> ```java
> package com.academy.presentation.dto;
>
> import io.swagger.v3.oas.annotations.media.Schema;
> import jakarta.validation.constraints.Min;
> import jakarta.validation.constraints.NotBlank;
> import jakarta.validation.constraints.Positive;
> import jakarta.validation.constraints.Size;
>
> @Schema(description = "Datos que el cliente envía para crear un curso")
> public record CourseRequest(
>
>         @Schema(description = "Nombre del curso", example = "Java desde cero")
>         @NotBlank
>         @Size(min = 2, max = 100)
>         String name,
>
>         @Schema(description = "Precio en pesos; debe ser mayor que cero", example = "150000")
>         @Positive
>         double price,
>
>         @Schema(description = "Cupo máximo de matriculados; al menos 1", example = "30")
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
> **Ruta:** [`src/main/java/com/academy/presentation/dto/CourseResponse.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/dto/CourseResponse.java)
>
> ```java
> package com.academy.presentation.dto;
>
> import io.swagger.v3.oas.annotations.media.Schema;
>
> @Schema(description = "Curso tal como lo ve el cliente")
> public record CourseResponse(
>
>         @Schema(description = "Identificador generado por la base", example = "1")
>         Long id,
>
>         @Schema(description = "Nombre del curso", example = "Java desde cero")
>         String name,
>
>         @Schema(description = "Precio en pesos", example = "150000")
>         double price,
>
>         @Schema(description = "Cupo máximo de matriculados", example = "30")
>         int maxCapacity
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 EnrolledResponse.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/dto/EnrolledResponse.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/dto/EnrolledResponse.java)
>
> ```java
> package com.academy.presentation.dto;
>
> import io.swagger.v3.oas.annotations.media.Schema;
>
> @Schema(description = "Cuántos estudiantes tiene matriculados un curso")
> public record EnrolledResponse(
>
>         @Schema(description = "Identificador del curso", example = "1")
>         Long courseId,
>
>         @Schema(description = "Cantidad de matriculados", example = "2")
>         long enrolled
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 EnrollmentRequest.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/dto/EnrollmentRequest.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/dto/EnrollmentRequest.java)
>
> ```java
> package com.academy.presentation.dto;
>
> import io.swagger.v3.oas.annotations.media.Schema;
> import jakarta.validation.constraints.NotNull;
>
> @Schema(description = "Datos que el cliente envía para matricular a un estudiante en un curso")
> public record EnrollmentRequest(
>
>         @Schema(description = "Identificador del estudiante", example = "1")
>         @NotNull
>         Long studentId,
>
>         @Schema(description = "Identificador del curso", example = "1")
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
> **Ruta:** [`src/main/java/com/academy/presentation/dto/EnrollmentResponse.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/dto/EnrollmentResponse.java)
>
> ```java
> package com.academy.presentation.dto;
>
> import io.swagger.v3.oas.annotations.media.Schema;
>
> @Schema(description = "Matrícula tal como la ve el cliente (aplanada)")
> public record EnrollmentResponse(
>
>         @Schema(description = "Identificador de la matrícula", example = "1")
>         Long id,
>
>         @Schema(description = "Identificador del estudiante", example = "1")
>         Long studentId,
>
>         @Schema(description = "Nombre del estudiante", example = "Ana Torres")
>         String studentName,
>
>         @Schema(description = "Identificador del curso", example = "1")
>         Long courseId,
>
>         @Schema(description = "Nombre del curso", example = "Java desde cero")
>         String courseName
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 PageResponse.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/dto/PageResponse.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/dto/PageResponse.java)
>
> ```java
> package com.academy.presentation.dto;
>
> import io.swagger.v3.oas.annotations.media.Schema;
>
> import java.util.List;
>
> @Schema(description = "Una página de resultados")
> public record PageResponse<T>(
>
>         @Schema(description = "Elementos de la página")
>         List<T> content,
>
>         @Schema(description = "Número de página, desde 0", example = "0")
>         int page,
>
>         @Schema(description = "Elementos por página (máximo 50)", example = "10")
>         int size,
>
>         @Schema(description = "Total de elementos", example = "3")
>         long totalElements,
>
>         @Schema(description = "Total de páginas", example = "1")
>         int totalPages
> ) {
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
> **Ruta:** [`src/main/java/com/academy/presentation/mapper/StudentMapper.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/mapper/StudentMapper.java)
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
> **Ruta:** [`src/main/java/com/academy/presentation/mapper/CourseMapper.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/mapper/CourseMapper.java)
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
> **Ruta:** [`src/main/java/com/academy/presentation/mapper/EnrollmentMapper.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/mapper/EnrollmentMapper.java)
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
> #### 📁 config
>
> <details>
> <summary><strong>📄 OpenApiConfig.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/config/OpenApiConfig.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/config/OpenApiConfig.java)
>
> ```java
> package com.academy.presentation.config;
>
> import io.swagger.v3.oas.models.Components;
> import io.swagger.v3.oas.models.OpenAPI;
> import io.swagger.v3.oas.models.info.Contact;
> import io.swagger.v3.oas.models.info.Info;
> import io.swagger.v3.oas.models.info.License;
> import io.swagger.v3.oas.models.security.SecurityScheme;
> import io.swagger.v3.oas.models.servers.Server;
> import org.springdoc.core.models.GroupedOpenApi;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
>
> import java.util.List;
>
> @Configuration
> public class OpenApiConfig {
>
>     @Bean
>     public OpenAPI academyOpenApi() {
>         return new OpenAPI()
>                 .info(new Info()
>                         .title("Academy API")
>                         .version("1.0.0")
>                         .description("Estudiantes, cursos y matrículas con DTO, Spring Boot, JPA y MySQL, documentado con OpenAPI.")
>                         .contact(new Contact().name("Equipo Academy").email("academy@mail.com"))
>                         .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
>                 .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
>                 .components(new Components().addSecuritySchemes("bearerAuth",
>                         new SecurityScheme()
>                                 .type(SecurityScheme.Type.HTTP)
>                                 .scheme("bearer")
>                                 .bearerFormat("JWT")));
>     }
>
>     @Bean
>     public GroupedOpenApi academyGroup() {
>         return GroupedOpenApi.builder()
>                 .group("academy")
>                 .displayName("Academy")
>                 .pathsToMatch("/api/v1/**")
>                 .build();
>     }
>
>     @Bean
>     public GroupedOpenApi internalGroup() {
>         return GroupedOpenApi.builder()
>                 .group("internal")
>                 .displayName("Internal")
>                 .pathsToMatch("/internal/**")
>                 .build();
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
> **Ruta:** [`src/main/java/com/academy/presentation/controller/StudentController.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/controller/StudentController.java)
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
> import io.swagger.v3.oas.annotations.Hidden;
> import io.swagger.v3.oas.annotations.Operation;
> import io.swagger.v3.oas.annotations.Parameter;
> import io.swagger.v3.oas.annotations.headers.Header;
> import io.swagger.v3.oas.annotations.media.Content;
> import io.swagger.v3.oas.annotations.media.ExampleObject;
> import io.swagger.v3.oas.annotations.media.Schema;
> import io.swagger.v3.oas.annotations.responses.ApiResponse;
> import io.swagger.v3.oas.annotations.tags.Tag;
> import jakarta.validation.Valid;
> import org.springdoc.core.annotations.ParameterObject;
> import org.springframework.data.domain.Pageable;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PathVariable;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.RequestBody;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RequestParam;
> import org.springframework.web.bind.annotation.RestController;
> import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
>
> import java.net.URI;
>
> @RestController
> @RequestMapping("/api/v1/students")
> @Tag(name = "Students", description = "Operaciones sobre estudiantes")
> public class StudentController {
>
>     private final StudentService service;
>
>     public StudentController(StudentService service) {
>         this.service = service;
>     }
>
>     @Operation(operationId = "listStudents", summary = "Listar estudiantes",
>             description = "Devuelve una página de estudiantes. El tamaño máximo de página es 50.")
>     @ApiResponse(responseCode = "200", description = "Página de estudiantes")
>     @GetMapping
>     public PageResponse<StudentResponse> findAll(@ParameterObject Pageable pageable) {
>         return StudentMapper.toPageResponse(service.findAll(pageable));
>     }
>
>     @Operation(operationId = "getStudent", summary = "Buscar un estudiante por id")
>     @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
>     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
>     @GetMapping("/{id}")
>     public ResponseEntity<StudentResponse> findById(
>             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
>         return service.findById(id)
>                 .map(StudentMapper::toResponse)
>                 .map(ResponseEntity::ok)
>                 .orElseGet(() -> ResponseEntity.notFound().build());
>     }
>
>     @Operation(operationId = "getStudentByEmail", summary = "Buscar un estudiante por correo",
>             description = "Obsoleto: usar `GET /api/v1/students/{id}`.",
>             deprecated = true)
>     @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
>     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese correo", content = @Content)
>     @GetMapping("/by-email")
>     public ResponseEntity<StudentResponse> findByEmail(
>             @Parameter(description = "Correo exacto", example = "ana@mail.com") @RequestParam String email) {
>         return service.findByEmail(email)
>                 .map(StudentMapper::toResponse)
>                 .map(ResponseEntity::ok)
>                 .orElseGet(() -> ResponseEntity.notFound().build());
>     }
>
>     @Operation(operationId = "createStudent", summary = "Registrar un estudiante",
>             description = "El `id` lo genera la base y no se envía. Si el correo ya existe responde `409`.")
>     @io.swagger.v3.oas.annotations.parameters.RequestBody(
>             description = "Datos del estudiante",
>             content = @Content(examples = {
>                     @ExampleObject(name = "Estudiante adulto", summary = "Un estudiante de 22 años",
>                             value = "{\"name\":\"Ana Torres\",\"age\":22,\"email\":\"ana@mail.com\"}"),
>                     @ExampleObject(name = "Estudiante mayor", summary = "Un estudiante de 31 años",
>                             value = "{\"name\":\"María Gómez\",\"age\":31,\"email\":\"maria@mail.com\"}")
>             }))
>     @ApiResponse(responseCode = "201", description = "Estudiante registrado",
>             headers = @Header(name = "Location", description = "URL del estudiante creado",
>                     schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/students/1")))
>     @ApiResponse(responseCode = "400", description = "Datos inválidos (Bean Validation)", content = @Content)
>     @ApiResponse(responseCode = "409", description = "El correo ya está registrado", content = @Content)
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
>
>     @Hidden
>     @GetMapping("/ping")
>     public String ping() {
>         return "pong";
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 CourseController.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/controller/CourseController.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/controller/CourseController.java)
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
> import io.swagger.v3.oas.annotations.Operation;
> import io.swagger.v3.oas.annotations.Parameter;
> import io.swagger.v3.oas.annotations.headers.Header;
> import io.swagger.v3.oas.annotations.media.Content;
> import io.swagger.v3.oas.annotations.media.Schema;
> import io.swagger.v3.oas.annotations.responses.ApiResponse;
> import io.swagger.v3.oas.annotations.security.SecurityRequirement;
> import io.swagger.v3.oas.annotations.tags.Tag;
> import jakarta.validation.Valid;
> import org.springdoc.core.annotations.ParameterObject;
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
> @Tag(name = "Courses", description = "Operaciones sobre cursos")
> public class CourseController {
>
>     private final CourseService service;
>
>     public CourseController(CourseService service) {
>         this.service = service;
>     }
>
>     @Operation(operationId = "listCourses", summary = "Listar cursos",
>             description = "Devuelve una página de cursos. El tamaño máximo de página es 50.")
>     @ApiResponse(responseCode = "200", description = "Página de cursos")
>     @GetMapping
>     public PageResponse<CourseResponse> findAll(@ParameterObject Pageable pageable) {
>         return CourseMapper.toPageResponse(service.findAll(pageable));
>     }
>
>     @Operation(operationId = "getCourse", summary = "Buscar un curso por id")
>     @ApiResponse(responseCode = "200", description = "Curso encontrado")
>     @ApiResponse(responseCode = "404", description = "No existe un curso con ese id", content = @Content)
>     @GetMapping("/{id}")
>     public ResponseEntity<CourseResponse> findById(
>             @Parameter(description = "Identificador del curso", example = "1") @PathVariable Long id) {
>         return service.findById(id)
>                 .map(CourseMapper::toResponse)
>                 .map(ResponseEntity::ok)
>                 .orElseGet(() -> ResponseEntity.notFound().build());
>     }
>
>     @Operation(operationId = "createCourse", summary = "Crear un curso",
>             description = "Requiere autenticación (esquema `bearerAuth`). El precio debe ser mayor que cero y el cupo, al menos 1.",
>             security = @SecurityRequirement(name = "bearerAuth"))
>     @ApiResponse(responseCode = "201", description = "Curso creado",
>             headers = @Header(name = "Location", description = "URL del curso creado",
>                     schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/courses/1")))
>     @ApiResponse(responseCode = "400", description = "Datos inválidos (Bean Validation)", content = @Content)
>     @PostMapping
>     public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
>         CourseEntity created = service.create(CourseMapper.toEntity(request));
>         CourseResponse body = CourseMapper.toResponse(created);
>         URI location = ServletUriComponentsBuilder.fromCurrentRequest()
>                 .path("/{id}").buildAndExpand(body.id()).toUri();
>         return ResponseEntity.created(location).body(body);
>     }
>
>     @Operation(operationId = "countCourseEnrolled", summary = "Contar los matriculados de un curso")
>     @ApiResponse(responseCode = "200", description = "Cantidad de matriculados")
>     @ApiResponse(responseCode = "404", description = "No existe un curso con ese id", content = @Content)
>     @GetMapping("/{id}/enrolled")
>     public ResponseEntity<EnrolledResponse> countEnrolled(
>             @Parameter(description = "Identificador del curso", example = "1") @PathVariable Long id) {
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
> **Ruta:** [`src/main/java/com/academy/presentation/controller/EnrollmentController.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/controller/EnrollmentController.java)
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
> import io.swagger.v3.oas.annotations.Operation;
> import io.swagger.v3.oas.annotations.Parameter;
> import io.swagger.v3.oas.annotations.headers.Header;
> import io.swagger.v3.oas.annotations.media.Content;
> import io.swagger.v3.oas.annotations.media.ExampleObject;
> import io.swagger.v3.oas.annotations.media.Schema;
> import io.swagger.v3.oas.annotations.responses.ApiResponse;
> import io.swagger.v3.oas.annotations.tags.Tag;
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
> @Tag(name = "Enrollments", description = "Matrículas de estudiantes en cursos")
> public class EnrollmentController {
>
>     private final EnrollmentService service;
>
>     public EnrollmentController(EnrollmentService service) {
>         this.service = service;
>     }
>
>     @Operation(operationId = "getEnrollment", summary = "Buscar una matrícula por id")
>     @ApiResponse(responseCode = "200", description = "Matrícula encontrada")
>     @ApiResponse(responseCode = "404", description = "No existe una matrícula con ese id", content = @Content)
>     @GetMapping("/{id}")
>     public ResponseEntity<EnrollmentResponse> findById(
>             @Parameter(description = "Identificador de la matrícula", example = "1") @PathVariable Long id) {
>         return service.findById(id)
>                 .map(EnrollmentMapper::toResponse)
>                 .map(ResponseEntity::ok)
>                 .orElseGet(() -> ResponseEntity.notFound().build());
>     }
>
>     @Operation(operationId = "enroll", summary = "Matricular a un estudiante en un curso",
>             description = "Responde `404` si el estudiante o el curso no existen, y `409` si ya está matriculado o el curso no tiene cupo.")
>     @io.swagger.v3.oas.annotations.parameters.RequestBody(
>             description = "Ids del estudiante y del curso",
>             content = @Content(examples = @ExampleObject(name = "Matrícula", summary = "Estudiante 1 en el curso 1",
>                     value = "{\"studentId\":1,\"courseId\":1}")))
>     @ApiResponse(responseCode = "201", description = "Matrícula creada",
>             headers = @Header(name = "Location", description = "URL de la matrícula creada",
>                     schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/enrollments/1")))
>     @ApiResponse(responseCode = "400", description = "Falta un id (Bean Validation)", content = @Content)
>     @ApiResponse(responseCode = "404", description = "El estudiante o el curso no existen", content = @Content)
>     @ApiResponse(responseCode = "409", description = "Matrícula repetida o curso sin cupo", content = @Content)
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
>
> <details>
> <summary><strong>📄 InternalController.java</strong></summary>
>
> **Ruta:** [`src/main/java/com/academy/presentation/controller/InternalController.java`](codigo/academica-swagger/src/main/java/com/academy/presentation/controller/InternalController.java)
>
> ```java
> package com.academy.presentation.controller;
>
> import com.academy.business.service.CourseService;
> import com.academy.business.service.ServiceStatus;
> import com.academy.business.service.StudentService;
> import com.academy.persistence.entity.CourseEntity;
> import com.academy.persistence.entity.StudentEntity;
> import io.swagger.v3.oas.annotations.Operation;
> import io.swagger.v3.oas.annotations.tags.Tag;
> import org.springframework.data.domain.PageRequest;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
>
> import java.util.Map;
>
> @RestController
> @RequestMapping("/internal")
> @Tag(name = "Internal", description = "Operaciones de apoyo para el equipo (no son parte de la API pública)")
> public class InternalController {
>
>     private final StudentService students;
>     private final CourseService courses;
>
>     public InternalController(StudentService students, CourseService courses) {
>         this.students = students;
>         this.courses = courses;
>     }
>
>     @Operation(summary = "Estado del servicio")
>     @GetMapping("/health")
>     public Map<String, String> health() {
>         return Map.of("status", "UP");
>     }
>
>     @Operation(summary = "Cargar datos de ejemplo",
>             description = "Crea tres estudiantes (si sus correos no existen) y dos cursos (solo si no hay ninguno).")
>     @PostMapping("/seed")
>     public Map<String, Integer> seed() {
>         int createdStudents = 0;
>         StudentEntity[] sampleStudents = {
>                 new StudentEntity("Ana Torres", 22, "ana@mail.com"),
>                 new StudentEntity("Luis Pérez", 25, "luis@mail.com"),
>                 new StudentEntity("María Gómez", 31, "maria@mail.com")
>         };
>         for (StudentEntity sample : sampleStudents) {
>             if (students.create(sample).status() == ServiceStatus.OK) {
>                 createdStudents++;
>             }
>         }
>         int createdCourses = 0;
>         if (courses.findAll(PageRequest.of(0, 1)).isEmpty()) {
>             courses.create(new CourseEntity("Java desde cero", 150000, 30));
>             courses.create(new CourseEntity("Spring Boot", 250000, 20));
>             createdCourses = 2;
>         }
>         return Map.of("students", createdStudents, "courses", createdCourses);
>     }
> }
> ```
>
> </details>

</details>

---

<details>
<summary><strong>🧪 Paso 11 — Probar la documentación</strong></summary>

> <details>
> <summary><strong>Paso 11.1 · Las pruebas con <code>curl</code></strong></summary>
>
> > Se arranca la aplicación con MySQL en marcha (`./mvnw spring-boot:run`) y se ejecuta cada comando en otra terminal. Debajo de cada uno está la **respuesta real**.
> >
> > > 💡 Los cuerpos JSON se muestran con sangría para leerlos mejor. Los últimos tres comandos se ejecutaron con el perfil `prod`.
> >
> > <details>
> > <summary><strong>1. Swagger UI redirige a su página → <code>302</code></strong></summary>
> >
> > `/swagger-ui.html` no es la pantalla: redirige (`302`) a `/swagger-ui/index.html`.
> >
> > ```bash
> > curl -i http://localhost:8080/swagger-ui.html
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 302
> > Location: /swagger-ui/index.html
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>2. La ruta por defecto del contrato ya no existe → <code>404</code></strong></summary>
> >
> > Se cambió `springdoc.api-docs.path` a `/api-docs`, así que la ruta por defecto responde `404`.
> >
> > ```bash
> > curl -i http://localhost:8080/v3/api-docs
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-01T20:43:30.574Z",
> >   "status": 404,
> >   "error": "Not Found",
> >   "path": "/v3/api-docs"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>3. La configuración de la pantalla: los grupos → <code>200</code></strong></summary>
> >
> > `urls` lista los grupos del selector **Select a definition**; `urls.primaryName` es el que se muestra primero.
> >
> > ```bash
> > curl -s http://localhost:8080/api-docs/swagger-config
> > ```
> >
> > Respuesta:
> >
> > ```text
> > {
> >   "configUrl": "/api-docs/swagger-config",
> >   "deepLinking": true,
> >   "displayRequestDuration": true,
> >   "docExpansion": "list",
> >   "oauth2RedirectUrl": "http://localhost:8080/swagger-ui/oauth2-redirect.html",
> >   "operationsSorter": "method",
> >   "tagsSorter": "alpha",
> >   "tryItOutEnabled": true,
> >   "urls": [
> >     {
> >       "url": "/api-docs/academy",
> >       "name": "Academy"
> >     },
> >     {
> >       "url": "/api-docs/internal",
> >       "name": "Internal"
> >     }
> >   ],
> >   "urls.primaryName": "Academy",
> >   "validatorUrl": ""
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>4. El contrato de un solo grupo → <code>200</code></strong></summary>
> >
> > Cada grupo tiene su propio documento OpenAPI: este solo trae `/internal/seed` y `/internal/health`.
> >
> > ```bash
> > curl -s http://localhost:8080/api-docs/internal
> > ```
> >
> > Respuesta:
> >
> > ```text
> > {
> >   "openapi": "3.1.0",
> >   "info": {
> >     "title": "Academy API",
> >     "description": "Estudiantes, cursos y matrículas con DTO, Spring Boot, JPA y MySQL, documentado con OpenAPI.",
> >     "contact": {
> >       "name": "Equipo Academy",
> >       "email": "academy@mail.com"
> >     },
> >     "license": {
> >       "name": "Apache 2.0",
> >       "url": "https://www.apache.org/licenses/LICENSE-2.0"
> >     },
> >     "version": "1.0.0"
> >   },
> >   "servers": [
> >     {
> >       "url": "http://localhost:8080",
> >       "description": "Local"
> >     }
> >   ],
> >   "tags": [
> >     {
> >       "name": "Internal",
> >       "description": "Operaciones de apoyo para el equipo (no son parte de la API pública)"
> >     }
> >   ],
> >   "paths": {
> >     "/internal/seed": {
> >       "post": {
> >         "tags": [
> >           "Internal"
> >         ],
> >         "summary": "Cargar datos de ejemplo",
> >         "description": "Crea tres estudiantes (si sus correos no existen) y dos cursos (solo si no hay ninguno).",
> >         "operationId": "seed",
> >         "responses": {
> >           "200": {
> >             "description": "OK",
> >             "content": {
> >               "application/json": {
> >                 "schema": {
> >                   "type": "object",
> >                   "additionalProperties": {
> >                     "type": "integer",
> >                     "format": "int32"
> >                   }
> >                 }
> >               }
> >             }
> >           }
> >         }
> >       }
> >     },
> >     "/internal/health": {
> >       "get": {
> >         "tags": [
> >           "Internal"
> >         ],
> >         "summary": "Estado del servicio",
> >         "operationId": "health",
> >         "responses": {
> >           "200": {
> >             "description": "OK",
> >             "content": {
> >               "application/json": {
> >                 "schema": {
> >                   "type": "object",
> >                   "additionalProperties": {
> >                     "type": "string"
> >                   }
> >                 }
> >               }
> >             }
> >           }
> >         }
> >       }
> >     }
> >   },
> >   "components": {
> >     "securitySchemes": {
> >       "bearerAuth": {
> >         "type": "http",
> >         "scheme": "bearer",
> >         "bearerFormat": "JWT"
> >       }
> >     }
> >   }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>5. El contrato en YAML (primeras líneas) → <code>200</code></strong></summary>
> >
> > El mismo contrato en YAML, listo para importarlo en otras herramientas.
> >
> > ```bash
> > curl -s http://localhost:8080/api-docs.yaml | head -12
> > ```
> >
> > Respuesta:
> >
> > ```text
> > openapi: 3.1.0
> > info:
> >   title: Academy API
> >   description: "Estudiantes, cursos y matrículas con DTO, Spring Boot, JPA y MySQL,\
> >     \ documentado con OpenAPI."
> >   contact:
> >     name: Equipo Academy
> >     email: academy@mail.com
> >   license:
> >     name: Apache 2.0
> >     url: https://www.apache.org/licenses/LICENSE-2.0
> >   version: 1.0.0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>6. Cargar datos de ejemplo → <code>200</code></strong></summary>
> >
> > Crea tres estudiantes y dos cursos.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/internal/seed
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "students": 3,
> >   "courses": 2
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>7. Cargar datos de ejemplo otra vez → <code>200</code></strong></summary>
> >
> > Los correos ya existen y ya hay cursos, así que no crea nada.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/internal/seed
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "students": 0,
> >   "courses": 0
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>8. Una operación oculta sigue funcionando → <code>200</code></strong></summary>
> >
> > `@Hidden` no desactiva la ruta: solo la quita de la documentación.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/students/ping
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: text/plain;charset=UTF-8
> > Content-Length: 4
> >
> > pong
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>9. …pero no aparece en el contrato</strong></summary>
> >
> > Cuenta cuántas veces aparece `ping` en el contrato: **cero**.
> >
> > ```bash
> > curl -s http://localhost:8080/api-docs | grep -c ping
> > ```
> >
> > Respuesta:
> >
> > ```text
> > 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>10. Una operación obsoleta sigue funcionando → <code>200</code></strong></summary>
> >
> > `deprecated = true` solo avisa: la operación responde igual.
> >
> > ```bash
> > curl -i "http://localhost:8080/api/v1/students/by-email?email=ana@mail.com"
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
> > <summary><strong>11. La seguridad solo se documenta: crear un curso sin token → <code>201</code></strong></summary>
> >
> > `createCourse` declara `bearerAuth`, pero la aplicación no valida ningún token: responde `201`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/courses \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"Docker básico","price":90000,"maxCapacity":15}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 201
> > Location: http://localhost:8080/api/v1/courses/3
> > Content-Type: application/json
> >
> > {
> >   "id": 3,
> >   "name": "Docker básico",
> >   "price": 90000.0,
> >   "maxCapacity": 15
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>12. Un curso inválido sigue dando <code>400</code></strong></summary>
> >
> > `@Positive` y `@Min(1)` rechazan el cuerpo, aunque `@Positive` no se vea en el contrato.
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
> >   "timestamp": "2026-11-01T20:43:31.098Z",
> >   "status": 400,
> >   "error": "Bad Request",
> >   "path": "/api/v1/courses"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>13. Con el perfil <code>prod</code>: el contrato se apaga → <code>404</code></strong></summary>
> >
> > La aplicación se arranca con `-Dspring-boot.run.profiles=prod` (o `-Dspring.profiles.active=prod` con el `.jar`).
> >
> > ```bash
> > curl -i http://localhost:8080/api-docs
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-01T20:43:40.546Z",
> >   "status": 404,
> >   "error": "Not Found",
> >   "path": "/api-docs"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>14. Con el perfil <code>prod</code>: la pantalla se apaga → <code>404</code></strong></summary>
> >
> > Mismo perfil: `springdoc.swagger-ui.enabled=false`.
> >
> > ```bash
> > curl -i http://localhost:8080/swagger-ui.html
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-01T20:43:40.567Z",
> >   "status": 404,
> >   "error": "Not Found",
> >   "path": "/swagger-ui.html"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>15. Con el perfil <code>prod</code>: la API sigue funcionando → <code>200</code></strong></summary>
> >
> > Apagar la documentación no toca la API.
> >
> > ```bash
> > curl -i "http://localhost:8080/api/v1/students?size=2"
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
> >   "size": 2,
> >   "totalElements": 3,
> >   "totalPages": 2
> > }
> > ```
> >
> > </details>
> >
> > > 💡 **Beneficio de este paso:** se comprueba que **cada** funcionalidad de Swagger hace lo que se dijo: redirección, rutas configuradas, grupos, YAML, operaciones ocultas y obsoletas, seguridad solo documentada y apagado en producción.
> > >
> > > ✅ **Punto de control 11:** las respuestas coinciden con las que se muestran bajo cada comando.
>
> </details>
>

</details>

---

➡️ **Guía completa:** [02-academica-swagger.md](02-academica-swagger.md)
