# students-api

CRUD de estudiantes con **DTO**, Spring Boot 4.1.2, Spring Data JPA y MySQL 8, documentado con Swagger (springdoc-openapi 3.2.0).
Parte del proyecto del módulo 03 ([`students-api-dto`](../../../modulo-03-diseno-api-rest-con-dto/codigo/students-api-dto/)) y le agrega la documentación.
Código final de la guía [`04-01-guia-crud-estudiantes-con-swagger.md`](../../04-01-guia-crud-estudiantes-con-swagger.md).

Colección de Postman: [`students-api.postman_collection.json`](../postman/students-api.postman_collection.json) (con su [entorno](../postman/academy-local.postman_environment.json) y [README](../postman/README.md)).

## Requisitos

- Java 21 o superior
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
├── business/service/StudentService (interfaz) · ServiceResult · ServiceStatus
├── business/impl/StudentServiceImpl
└── persistence
    ├── entity/StudentEntity · StudentStatus
    └── repository/StudentJpaRepository
```
