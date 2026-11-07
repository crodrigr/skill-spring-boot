# students-api-security

CRUD de estudiantes protegido con **Spring Security y JWT**: Spring Boot 4.1.2, Spring Data JPA, MySQL 8, JJWT 0.12.6 y Swagger (springdoc-openapi 3.2.0).
Parte del proyecto del módulo 05 ([`students-api-excepciones`](../../../modulo-05-excepciones/codigo/students-api-excepciones/)) y le agrega la seguridad.
Código final de la guía [`06-01-guia-crud-estudiantes-con-security.md`](../../06-01-guia-crud-estudiantes-con-security.md).

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

Al arrancar se crean dos cuentas de práctica (**solo para fines educativos**):

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | `ADMIN` |
| `ana` | `ana123` | `USER` |

## Probar

```bash
# 1. Iniciar sesión y guardar el token
TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

# 2. Usarlo
curl -i -H "Authorization: Bearer $TOKEN" localhost:8080/api/v1/auth/me
curl -i -H "Authorization: Bearer $TOKEN" -X POST localhost:8080/internal/seed
curl -i -H "Authorization: Bearer $TOKEN" localhost:8080/api/v1/students

# 3. Sin token: 401 con ErrorResponse
curl -i localhost:8080/api/v1/students
```

| Ruta | Quién puede |
|---|---|
| `POST /api/v1/auth/login`, `GET /internal/health`, Swagger UI y `/api-docs/**` | Cualquiera |
| `GET /api/v1/auth/me` y `/api/v1/students/**` (leer, crear, reemplazar, cambiar estado) | Cualquier usuario autenticado |
| `DELETE /api/v1/students/{id}` y `/internal/seed` | Solo `ADMIN` |

| Qué | Dirección |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html (botón **Authorize**) |
| Contrato OpenAPI | http://localhost:8080/api-docs/students · `/auth` · `/internal` |

## Configuración del token

```properties
app.jwt.secret=${JWT_SECRET:...clave solo educativa...}
app.jwt.expiration-seconds=3600
```

> ⚠️ La clave del repositorio es **solo para fines educativos**. En un proyecto real se entrega por la variable de entorno `JWT_SECRET` (32 caracteres o más) o un gestor de secretos.

## Estructura

```text
src/main/java/com/academy
├── AcademyApplication.java
├── security
│   ├── SecurityConfig · JwtProperties · JwtService · JwtAuthenticationFilter · AuthService
│   ├── JpaUserDetailsService · AccountSeeder
│   └── SecurityErrorWriter · RestAuthenticationEntryPoint · RestAccessDeniedHandler
├── presentation
│   ├── config/OpenApiConfig.java
│   ├── controller/AuthController · StudentController · InternalController
│   ├── dto/LoginRequest · LoginResponse · MeResponse · ErrorResponse · Student… · PageResponse
│   ├── exception/GlobalExceptionHandler.java
│   └── mapper/StudentMapper.java
├── business
│   ├── exception/StudentNotFoundException · StudentEmailAlreadyExistsException
│   ├── service/StudentService.java (interfaz)
│   └── impl/StudentServiceImpl.java
└── persistence
    ├── entity/Role · UserAccountEntity · StudentEntity · StudentStatus
    └── repository/UserAccountJpaRepository · StudentJpaRepository
```
