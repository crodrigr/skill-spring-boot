# academica-security

Taller de gestión de cursos (estudiantes, cursos y matrículas) con **DTO**, **Swagger**, **manejo de excepciones** y ahora **Spring Security con JWT**: login, contraseñas con BCrypt, rutas públicas, rutas autenticadas y rutas solo para `ADMIN`. Los errores `401` y `403` salen con el mismo `ErrorResponse` que el resto de la API.
Parte del proyecto [`academica-excepciones`](../../../modulo-05-excepciones/codigo/academica-excepciones/) del Módulo 05; el negocio (`business`) no cambia.
Guía: [`06-05-academica-security.md`](../../06-05-academica-security.md) · Código completo: [`06-06-academica-security-codigo-completo.md`](../../06-06-academica-security-codigo-completo.md) · Versión de un solo recurso: [`06-01-guia-crud-estudiantes-con-security.md`](../../06-01-guia-crud-estudiantes-con-security.md).

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
| Swagger UI (botón **Authorize**) | http://localhost:8080/swagger-ui.html |
| Contrato OpenAPI | http://localhost:8080/api-docs |
| API | http://localhost:8080/api/v1 |

Cuentas de práctica (se crean al arrancar, solo con fines educativos):

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | `ADMIN` |
| `ana` | `ana123` | `USER` |

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')
curl -X POST localhost:8080/internal/seed -H "Authorization: Bearer $TOKEN"   # datos de ejemplo
./probar-apis.sh                                                              # recorre las rutas y compara códigos
```

## Quién puede qué

| Ruta | Sin token | `USER` | `ADMIN` |
|---|:-:|:-:|:-:|
| `POST /api/v1/auth/login` · `GET /internal/health` · Swagger | ✅ | ✅ | ✅ |
| `GET /api/v1/auth/me` · `GET`/`POST` de estudiantes · `GET` de cursos · matrículas | `401` | ✅ | ✅ |
| `POST /api/v1/courses` · `/internal/seed` | `401` | `403` | ✅ |

## Errores de seguridad

| Situación | HTTP | Mensaje |
|---|:-:|---|
| Sin token | `401` | Se requiere autenticación para acceder a este recurso |
| Token manipulado o mal formado | `401` | El token no es válido |
| Token vencido | `401` | El token expiró |
| Login con datos incorrectos | `401` | Usuario o contraseña incorrectos |
| Rol insuficiente | `403` | No tiene permisos para realizar esta operación |

Los de negocio (`404`, `409`, `400`, `500`) son los de [`academica-excepciones`](../../../modulo-05-excepciones/codigo/academica-excepciones/).

## Estructura

```text
src/main/java/com/academy
├── AcademyApplication.java
├── security                      🆕 todo lo propio de Spring Security
│   ├── SecurityConfig · JwtProperties · JwtService · JwtAuthenticationFilter · AuthService
│   ├── JpaUserDetailsService · AccountSeeder
│   └── SecurityErrorWriter · RestAuthenticationEntryPoint · RestAccessDeniedHandler
├── presentation
│   ├── config/OpenApiConfig.java                  ✏️ Authorize, 401 y 403
│   ├── controller/AuthController 🆕 · StudentController · CourseController ✏️ · EnrollmentController · InternalController ✏️
│   ├── dto/LoginRequest · LoginResponse · MeResponse 🆕 · el resto sin cambios
│   ├── exception/GlobalExceptionHandler.java      ✏️ + AuthenticationException y AccessDeniedException
│   └── mapper/…
├── business                      sin cambios (no sabe que existe la seguridad)
└── persistence
    ├── entity/Role · UserAccountEntity 🆕 · Student · Course · Enrollment
    └── repository/UserAccountJpaRepository 🆕 · Student · Course · Enrollment
```
