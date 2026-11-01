# students-api

CRUD de estudiantes con Spring Boot 3.3, Spring Data JPA y MySQL 8, documentado con Swagger (springdoc-openapi 2.6.0).
Código final de la guía [`01-guia-crud-estudiantes-con-swagger.md`](../01-guia-crud-estudiantes-con-swagger.md).

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
│   └── controller/StudentController.java · InternalController.java
├── business/service/StudentService.java
└── persistence
    ├── entity/StudentEntity.java · StudentStatus.java
    └── repository/StudentJpaRepository.java
```
