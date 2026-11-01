# academica-swagger

Taller de gestión de cursos (estudiantes, cursos y matrículas) con **DTO** y documentado con **Swagger** (springdoc-openapi 2.6.0).
Parte del proyecto [`academica-dtos`](../../../modulo-05/codigo/academica-dtos/) del Módulo 05 y le agrega la documentación.
Guía: [`02-academica-swagger.md`](../../02-academica-swagger.md) · Código completo: [`03-academica-swagger-codigo-completo.md`](../../03-academica-swagger-codigo-completo.md).

Colección de Postman: [`academica-swagger.postman_collection.json`](../postman/academica-swagger.postman_collection.json) (con su [entorno](../postman/academy-local.postman_environment.json) y [README](../postman/README.md)).

## Requisitos

- Java 17 o superior · Maven 3.9+ (o el wrapper `./mvnw`)
- MySQL 8 con la base `academy`

```bash
docker run -d --name academy-mysql \
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academy \
  -p 3306:3306 mysql:8.0
```

## Ejecutar

```bash
./mvnw spring-boot:run
```

| Qué | Dirección |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Contrato OpenAPI (JSON) | http://localhost:8080/api-docs |
| Contrato OpenAPI (YAML) | http://localhost:8080/api-docs.yaml |
| Contrato de un grupo | http://localhost:8080/api-docs/academy · `/api-docs/internal` |
| API | http://localhost:8080/api/v1 |

Datos de ejemplo: `POST http://localhost:8080/internal/seed`.

## Sin documentación (producción)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Estructura

```text
src/main/java/com/academy
├── AcademyApplication.java
├── presentation
│   ├── config/OpenApiConfig.java
│   ├── controller/StudentController · CourseController · EnrollmentController · InternalController
│   ├── dto/StudentRequest · StudentResponse · CourseRequest · CourseResponse · EnrolledResponse · EnrollmentRequest · EnrollmentResponse · PageResponse   (con @Schema)
│   └── mapper/StudentMapper · CourseMapper · EnrollmentMapper
├── business/service/StudentService · CourseService · EnrollmentService · ServiceResult · ServiceStatus
└── persistence
    ├── entity/StudentEntity · CourseEntity · EnrollmentEntity
    └── repository/StudentJpaRepository · CourseJpaRepository · EnrollmentJpaRepository
```
