# students-api

CRUD de estudiantes con **DTO**, Spring Boot 3.3, Spring Data JPA y MySQL 8, documentado con Swagger (springdoc-openapi 2.6.0).
Parte del proyecto del módulo 05 ([`students-api-dto`](../../../modulo-05/codigo/students-api-dto/)) y le agrega la documentación.
Código final de la guía [`01-guia-crud-estudiantes-con-swagger.md`](../../01-guia-crud-estudiantes-con-swagger.md).

## Requisitos

- Java 17 o superior
- Maven 3.9+ (o el wrapper `./mvnw`)
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
| Contrato de un grupo | http://localhost:8080/api-docs/students · `/api-docs/internal` |
| API | http://localhost:8080/api/v1/students |

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
│   ├── controller/StudentController.java · InternalController.java
│   ├── dto/StudentRequest · StudentStatusRequest · StudentResponse · PageResponse   (con @Schema)
│   └── mapper/StudentMapper.java
├── business/service/StudentService · ServiceResult · ServiceStatus
└── persistence
    ├── entity/StudentEntity · StudentStatus
    └── repository/StudentJpaRepository
```
