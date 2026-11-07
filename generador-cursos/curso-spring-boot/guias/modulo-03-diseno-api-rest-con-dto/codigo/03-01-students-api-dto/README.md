# students-api-dto

CRUD de estudiantes con Spring Boot 4.1.2, Spring Data JPA y MySQL 8, con **DTO** y controladores delgados.
Código final de la guía [`03-01-guia-dto-y-estructura-de-controladores.md`](../../03-01-guia-dto-y-estructura-de-controladores.md).

Colección de Postman: [`students-api-dto.postman_collection.json`](../postman/students-api-dto.postman_collection.json) (con su [entorno](../postman/academy-local.postman_environment.json) y [README](../postman/README.md)).

## Requisitos

- Java 21 o superior · Maven 3.9+ (o el wrapper `./mvnw`)
- MySQL 8 con la base `academy`

```bash
docker run -d --name academy-mysql \
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academy \
  -p 3306:3306 mysql:8.0
```

## Ejecutar y probar

```bash
./mvnw spring-boot:run     # API en http://localhost:8080/api/v1/students
```

La lista de pruebas con `curl` está en el Paso 7 de la guía.

## Endpoints

| Método | Ruta | Respuestas |
|---|---|---|
| `GET` | `/api/v1/students?name=&page=&size=&sort=` | `200` (tamaño máximo de página: 50) |
| `GET` | `/api/v1/students/{id}` | `200` · `404` |
| `POST` | `/api/v1/students` | `201` + `Location` · `400` · `409` |
| `PUT` | `/api/v1/students/{id}` | `200` · `400` · `404` · `409` |
| `PATCH` | `/api/v1/students/{id}/status` | `200` · `400` · `404` |
| `DELETE` | `/api/v1/students/{id}` | `204` · `404` |

## Estructura

```text
src/main/java/com/academy
├── AcademyApplication.java
├── presentation
│   ├── controller/StudentController.java
│   ├── dto/StudentRequest · StudentStatusRequest · StudentResponse · PageResponse
│   └── mapper/StudentMapper.java
├── business/service/StudentService (interfaz) · ServiceResult · ServiceStatus
├── business/impl/StudentServiceImpl
└── persistence
    ├── entity/StudentEntity · StudentStatus
    └── repository/StudentJpaRepository
```
