# academica-security

Taller de gestión de cursos (estudiantes, cursos y matrículas) con **DTO**, **Swagger**, **manejo de excepciones** y **Spring Security con JWT**: login, contraseñas con BCrypt, rutas públicas, rutas para cualquier cuenta autenticada y rutas solo para `ADMIN`. Los errores `401` y `403` salen con el mismo `ErrorResponse` que el resto de la API.
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

## 👤 Usuarios para autenticar

Se crean al arrancar la aplicación (`AccountSeeder`) y también los carga [`migrate.sql`](migrate.sql). En la base solo se guarda el **hash BCrypt**; el login recibe la contraseña en texto plano.

| Usuario | Contraseña | Rol | Qué puede hacer |
|---|---|---|---|
| `admin` | `admin123` | `ADMIN` | Todo: además crea cursos y carga datos de ejemplo |
| `ana` | `ana123` | `USER` | Leer, registrar estudiantes y matricular; **no** crea cursos ni usa `/internal/seed` |

> ⚠️ **Solo para fines educativos.** Cuentas con contraseñas conocidas y una clave de firma escrita en `application.properties` (`app.jwt.secret`, reemplazable con la variable `JWT_SECRET`). En un proyecto real nada de esto va en el repositorio.

### Iniciar sesión

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')

curl -H "Authorization: Bearer $TOKEN" localhost:8080/api/v1/auth/me      # {"username":"admin","role":"ADMIN"}
```

El token dura **1 hora** (`app.jwt.expiration-seconds`). En Swagger UI: `POST /api/v1/auth/login` → copiar `token` → **Authorize** → pegarlo.

## 🔐 APIs y su seguridad

| Método y ruta | Acceso | Sin token | `USER` | `ADMIN` |
|---|---|:-:|:-:|:-:|
| `POST /api/v1/auth/login` | 🌐 Público | ✅ | ✅ | ✅ |
| `GET /internal/health` | 🌐 Público | ✅ | ✅ | ✅ |
| `GET /swagger-ui.html` · `/swagger-ui/**` · `/api-docs/**` | 🌐 Público | ✅ | ✅ | ✅ |
| `GET /api/v1/auth/me` | 🔑 Autenticado | `401` | ✅ | ✅ |
| `GET /api/v1/students` · `GET /api/v1/students/{id}` · `GET /api/v1/students/by-email` | 🔑 Autenticado | `401` | ✅ | ✅ |
| `POST /api/v1/students` | 🔑 Autenticado | `401` | ✅ | ✅ |
| `GET /api/v1/courses` · `GET /api/v1/courses/{id}` · `GET /api/v1/courses/{id}/enrolled` | 🔑 Autenticado | `401` | ✅ | ✅ |
| **`POST /api/v1/courses`** | 👑 Solo `ADMIN` | `401` | **`403`** | ✅ |
| `GET /api/v1/enrollments/{id}` · `POST /api/v1/enrollments` | 🔑 Autenticado | `401` | ✅ | ✅ |
| **`POST /internal/seed`** | 👑 Solo `ADMIN` | `401` | **`403`** | ✅ |

La regla vive en un solo lugar, [`SecurityConfig`](src/main/java/com/academy/security/SecurityConfig.java), y se lee de arriba abajo (gana la primera que coincide):

```java
.requestMatchers("/api/v1/auth/login", "/internal/health").permitAll()
.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll()
.requestMatchers(HttpMethod.POST, "/api/v1/courses").hasRole("ADMIN")
.requestMatchers("/internal/**").hasRole("ADMIN")
.anyRequest().authenticated()
```

### Errores de seguridad

| Situación | HTTP | `message` |
|---|:-:|---|
| Ruta protegida sin token | `401` | Se requiere autenticación para acceder a este recurso |
| Token manipulado o mal formado | `401` | El token no es válido |
| Token vencido | `401` | El token expiró |
| Login con usuario o contraseña incorrectos | `401` | Usuario o contraseña incorrectos |
| Login con campos vacíos | `400` | Los datos enviados no son válidos (+ `details`) |
| Rol insuficiente | `403` | No tiene permisos para realizar esta operación |

Los de negocio (`404`, `409`, `400`, `500`) son los de [`academica-excepciones`](../../../modulo-05-excepciones/codigo/academica-excepciones/): estudiante, curso o matrícula inexistente `404`; correo repetido, matrícula repetida o curso sin cupo `409`.

## 🗄️ Datos de ejemplo: `migrate.sql`

[`migrate.sql`](migrate.sql) crea las tablas si no existen y carga **2 cuentas** (`admin`, `ana`), **20 estudiantes**, **4 cursos** y **10 matrículas**. El curso «Docker y contenedores» queda **lleno** (cupo 3), útil para probar el `409` de cupo.

```bash
mysql -uroot -proot < migrate.sql
# con Docker:  docker exec -i academy-mysql mysql -uroot -proot < migrate.sql
```

- Es **repetible**: no duplica cuentas, estudiantes (por correo), cursos (por nombre) ni matrículas.
- ⚠️ La aplicación usa `spring.jpa.hibernate.ddl-auto=create` y **borra las tablas cada vez que arranca**. Ejecutar el script **después** de arrancar, o cambiar a `update`.
- Otra opción sin SQL: `POST /internal/seed` (como `admin`) crea 3 estudiantes y 2 cursos.

## 📮 Colección de Postman

En [`postman/`](postman/):

| Archivo | Qué es |
|---|---|
| `06-05-academica-security.postman_collection.json` | **48 peticiones** en 8 carpetas, con sus pruebas (`pm.test`) |
| `academy-local.postman_environment.json` | Entorno **Academy local**: solo `baseUrl` (`http://localhost:8080`) |

1. En Postman: **Import** y seleccionar los dos archivos.
2. Elegir el entorno **Academy local**.
3. Arrancar la aplicación con MySQL en marcha (con o sin `migrate.sql`).
4. Abrir la colección → **Run collection** y ejecutar **en orden**.

| Carpeta | Qué prueba | Códigos |
|---|---|---|
| 1. Público | `health` y contratos OpenAPI, sin token | `200` |
| 2. Login | Entra como `admin` y `ana` (**guarda los tokens**) y prueba los errores | `200` · `401` · `400` |
| 3. Sin token o con token malo | Todo lo protegido rechaza | `401` |
| 4. Cargar datos de ejemplo | `admin` carga; `ana` no puede | `200` · `403` |
| 5. Cursos | Leer, crear como `ana` (rechazado) y como `admin` | `200` · `403` · `201` · `400` · `404` |
| 6. Estudiantes | Registrar, listar, buscar por id y por correo | `201` · `200` · `409` · `400` · `404` |
| 7. Matrículas | Matricular, repetida, curso lleno, inexistentes | `201` · `409` · `404` · `400` · `200` |
| 8. Rutas que no existen | Ruta inexistente y método no permitido | `404` · `405` |

Los tokens (`tokenAdmin`, `tokenAna`) y los ids se guardan solos como variables de la colección. Los correos llevan un sufijo (`{{run}}`) que cambia en cada ejecución, así que **se puede repetir sin vaciar la base**. Los tokens duran 1 hora: si pasa más tiempo, volver a ejecutar la carpeta 2.

## 🧪 Probar con `curl`

```bash
./probar-apis.sh                       # o: ./probar-apis.sh http://localhost:8080
```

Inicia sesión como `admin` y `ana`, recorre las rutas y compara cada código HTTP. Resultado esperado: `Resultado: 23 correctas, 0 fallidas`.

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
