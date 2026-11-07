# academica-excepciones

Taller de gestión de cursos (estudiantes, cursos y matrículas) con **DTO**, documentado con **Swagger** y con **manejo de excepciones**: el servicio **lanza** excepciones propias y un `@RestControllerAdvice` responde siempre con el mismo cuerpo (`ErrorResponse`).
Parte del proyecto [`academica-swagger`](../../../modulo-04-documentacion-apis-swagger/codigo/academica-swagger/) del Módulo 04 y reemplaza `ServiceResult`/`ServiceStatus` por excepciones.
Guía: [`05-02-academica-excepciones.md`](../../05-02-academica-excepciones.md) · Código completo: [`05-03-academica-excepciones-codigo-completo.md`](../../05-03-academica-excepciones-codigo-completo.md) · Versión de un solo recurso: [`05-01-guia-crud-estudiantes-con-excepciones.md`](../../05-01-guia-crud-estudiantes-con-excepciones.md).

## Requisitos

- Java 21 o superior · Maven 3.9+ (o el wrapper `./mvnw`)
- MySQL 8 con la base `academia`

```bash
docker run -d --name academy-mysql \
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academia \
  -p 3306:3306 mysql:8.0
```

## Ejecutar

```bash
./mvnw spring-boot:run
```

| Qué | Dirección |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Contrato OpenAPI | http://localhost:8080/api-docs |
| API | http://localhost:8080/api/v1 |

Datos de ejemplo: `POST http://localhost:8080/internal/seed`.

## Errores que devuelve la API

| Situación | HTTP | Excepción |
|---|:-:|---|
| Estudiante, curso o matrícula inexistente | `404` | `StudentNotFoundException` · `CourseNotFoundException` · `EnrollmentNotFoundException` |
| Correo repetido, matrícula repetida o curso sin cupo | `409` | `StudentEmailAlreadyExistsException` · `AlreadyEnrolledException` · `CourseFullException` |
| Datos inválidos, JSON roto, parámetro mal escrito, orden inexistente | `400` | las de Spring Web / Bean Validation |
| Ruta inexistente / método no permitido | `404` / `405` | las de Spring Web |
| Cualquier otro fallo | `500` | `Exception` (mensaje genérico; la traza va al *log*) |

## Estructura

```text
src/main/java/com/academy
├── AcademyApplication.java
├── presentation
│   ├── config/OpenApiConfig.java
│   ├── controller/StudentController · CourseController · EnrollmentController · InternalController
│   ├── dto/ErrorResponse · StudentRequest · StudentResponse · CourseRequest · CourseResponse · EnrolledResponse · EnrollmentRequest · EnrollmentResponse · PageResponse
│   ├── exception/GlobalExceptionHandler.java
│   └── mapper/StudentMapper · CourseMapper · EnrollmentMapper
├── business
│   ├── exception/StudentNotFoundException · StudentEmailAlreadyExistsException · CourseNotFoundException · EnrollmentNotFoundException · AlreadyEnrolledException · CourseFullException
│   ├── service/StudentService · CourseService · EnrollmentService   (interfaces)
│   └── impl/StudentServiceImpl · CourseServiceImpl · EnrollmentServiceImpl
└── persistence
    ├── entity/StudentEntity · CourseEntity · EnrollmentEntity
    └── repository/StudentJpaRepository · CourseJpaRepository · EnrollmentJpaRepository
```
