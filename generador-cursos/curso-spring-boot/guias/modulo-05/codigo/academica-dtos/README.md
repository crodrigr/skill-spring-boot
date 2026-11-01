# academica-dtos

Taller de gestión de cursos (estudiantes, cursos y matrículas) con **DTO**, mapeadores y controladores delgados.
Es el proyecto de la [guía 01 del módulo 03-04](../../../modulo-03-04/01-guia-taller-gestion-de-cursos-con-bd.md) con las mejoras del [Módulo 05](../../01-guia-dto-y-estructura-de-controladores.md).
Guía: [`02-academica-dtos.md`](../../02-academica-dtos.md) · Código completo: [`03-academica-dtos-codigo-completo.md`](../../03-academica-dtos-codigo-completo.md).

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
./mvnw spring-boot:run     # API en http://localhost:8080/api/v1
```

## Endpoints

| Método | Ruta | Respuestas |
|---|---|---|
| `GET` | `/api/v1/students?page=&size=&sort=` | `200` (tamaño máximo de página: 50) |
| `GET` | `/api/v1/students/{id}` | `200` · `404` |
| `POST` | `/api/v1/students` | `201` + `Location` · `400` · `409` |
| `GET` | `/api/v1/courses?page=&size=&sort=` | `200` |
| `GET` | `/api/v1/courses/{id}` | `200` · `404` |
| `POST` | `/api/v1/courses` | `201` + `Location` · `400` |
| `GET` | `/api/v1/courses/{id}/enrolled` | `200` · `404` |
| `POST` | `/api/v1/enrollments` | `201` + `Location` · `400` · `404` · `409` |
| `GET` | `/api/v1/enrollments/{id}` | `200` · `404` |

## Estructura

```text
src/main/java/com/academy
├── AcademyApplication.java
├── presentation
│   ├── controller/StudentController · CourseController · EnrollmentController
│   ├── dto/StudentRequest · StudentResponse · CourseRequest · CourseResponse · EnrolledResponse · EnrollmentRequest · EnrollmentResponse · PageResponse
│   └── mapper/StudentMapper · CourseMapper · EnrollmentMapper
├── business/service/StudentService · CourseService · EnrollmentService · ServiceResult · ServiceStatus
└── persistence
    ├── entity/StudentEntity · CourseEntity · EnrollmentEntity
    └── repository/StudentJpaRepository · CourseJpaRepository · EnrollmentJpaRepository
```
