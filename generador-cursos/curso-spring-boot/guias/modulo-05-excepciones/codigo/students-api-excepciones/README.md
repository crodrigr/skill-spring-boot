# students-api-excepciones

CRUD de estudiantes con **manejo de excepciones**: Spring Boot 4.1.2, Spring Data JPA y MySQL 8, documentado con Swagger (springdoc-openapi 3.2.0).
Parte del proyecto del módulo 04 ([`students-api`](../../../modulo-04-documentacion-apis-swagger/codigo/students-api/)) y reemplaza `ServiceResult` por **excepciones**.
Código final de la guía [`05-01-guia-crud-estudiantes-con-excepciones.md`](../../05-01-guia-crud-estudiantes-con-excepciones.md).

## Requisitos

- Java 21 o superior
- Maven 3.9+ (o el wrapper `./mvnw`)
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
| Contrato OpenAPI (JSON) | http://localhost:8080/api-docs |
| API | http://localhost:8080/api/v1/students |

Datos de ejemplo: `POST http://localhost:8080/internal/seed`.

## Probar los errores

```bash
curl -i localhost:8080/api/v1/students/999                    # 404
curl -i -X POST localhost:8080/api/v1/students \
  -H 'Content-Type: application/json' \
  -d '{"name":"","age":5,"email":"x","status":"ACTIVE"}'      # 400 con details
curl -i 'localhost:8080/api/v1/students?sort=foo'             # 400 (antes: 500)
curl -i localhost:8080/api/v1/nope                            # 404
curl -i -X DELETE localhost:8080/api/v1/students              # 405
```

Todos responden con el mismo cuerpo `ErrorResponse`: `timestamp`, `status`, `error`, `message`, `path` y, en validación, `details`.

## Estructura

```text
src/main/java/com/academy
├── AcademyApplication.java
├── presentation
│   ├── config/OpenApiConfig.java                    (+ respuestas de error reutilizables)
│   ├── controller/StudentController.java · InternalController.java
│   ├── dto/ErrorResponse · StudentRequest · StudentStatusRequest · StudentResponse · PageResponse
│   ├── exception/GlobalExceptionHandler.java        (@RestControllerAdvice)
│   └── mapper/StudentMapper.java
├── business
│   ├── exception/StudentNotFoundException · StudentEmailAlreadyExistsException
│   ├── service/StudentService.java (interfaz)
│   └── impl/StudentServiceImpl.java
└── persistence
    ├── entity/StudentEntity · StudentStatus
    └── repository/StudentJpaRepository
```
