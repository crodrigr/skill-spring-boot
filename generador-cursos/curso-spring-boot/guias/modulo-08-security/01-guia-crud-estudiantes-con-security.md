# 🔐 Guía paso a paso — CRUD de estudiantes con Spring Security y JWT

> 👩‍🏫 **Material docente.** Continúa la [guía del Módulo 07 — CRUD de estudiantes con excepciones](../modulo-07-excepciones/01-guia-crud-estudiantes-con-excepciones.md): el proyecto de partida **ya tiene** DTO, un manejo uniforme de errores (`ErrorResponse`), documentación con Swagger y MySQL. Sobre él se agrega **seguridad**, **una pieza por vez**, con las ideas del [Módulo 08](../../08-spring-boot-security-jwt/material/ejemplos/explicacion-conceptual.md): Spring Security, HTTP Basic como primer paso y autenticación *stateless* con JWT.
> Contiene código completo: no se entrega a los estudiantes antes de resolver el módulo.
>
> 🪶 **¿Se busca algo más corto?** Hay una [versión sencilla y práctica](02-guia-security-facil.md) con su propio proyecto ([`students-api-security-facil/`](codigo/students-api-security-facil/)).
>
> 🧪 **Probar todas las rutas con `curl`:** [`04-curl-probar-todas-las-apis.md`](04-curl-probar-todas-las-apis.md) (y el script [`probar-apis.sh`](codigo/probar-apis.sh)).
>
> 📦 **Proyecto terminado:** [`students-api-security/`](codigo/students-api-security/) (código final, listo para ejecutar con `./mvnw spring-boot:run`).
>
> ✅ **Verificada.** El código se compiló para Java 21 y se ejecutó con Spring Boot 4.1.2, Spring Security 7.1, JJWT 0.12.6, springdoc-openapi 3.2.0 y MySQL 8. Las respuestas HTTP, el contenido del token, el contrato OpenAPI y la tabla de cuentas que se muestran son salidas reales de la aplicación. Lo que se cuenta de la pantalla de Swagger UI se deduce de ese contrato.
>
> 🧭 **Idea central.** Seguridad son **dos preguntas distintas**: *¿quién eres?* (**autenticación**, responde `401`) y *¿puedes hacer esto?* (**autorización**, responde `403`). Y se agrega **sin tocar la lógica de negocio**: `StudentService` y el CRUD no cambian; toda la novedad vive en clases nuevas.

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Proteger la API de estudiantes del Módulo 07 con **Spring Security**, primero con HTTP Basic y luego con **JWT** (autenticación sin sesión), y mantener el formato de error uniforme del módulo anterior.
>
> Al terminar, el estudiante:
>
> - Agrega `spring-boot-starter-security` y ve **qué cambia con una sola dependencia** (todo queda protegido).
> - Distingue **autenticación** (`401`) de **autorización** (`403`).
> - Declara una **`SecurityFilterChain`** *stateless*: rutas públicas, rutas protegidas y rutas solo para `ADMIN`.
> - Guarda cuentas en la base con la contraseña **cifrada con BCrypt** y las conecta con `UserDetailsService` y `PasswordEncoder`.
> - Describe la diferencia entre una API **stateful** y una **stateless**, y entiende por qué el JWT sirve para la segunda.
> - Emite un **JWT** en un endpoint de login y lo valida con un **filtro** en cada petición.
> - Lee las tres partes de un JWT y ve por qué un token **manipulado** se rechaza.
> - Hace que los errores `401` y `403` (que ocurren **antes** de Spring MVC) salgan con el mismo `ErrorResponse` que el resto de la API.
> - Hace que el botón **Authorize** de Swagger funcione de verdad y documenta `401` y `403`.

</details>

<details>
<summary><strong>📝 Introducción: qué es qué</strong></summary>

> <details>
> <summary><strong>🧩 Tres ideas que se confunden</strong></summary>
>
> | Idea | Pregunta que responde | Código HTTP si falla | En este proyecto |
> |---|---|:-:|---|
> | **Autenticación** | ¿Quién eres? | `401 Unauthorized` | Login y validación del token |
> | **Autorización** | ¿Puedes hacer esto? | `403 Forbidden` | Rol `ADMIN` para borrar y para `/internal` |
> | **Estado de la sesión** | ¿El servidor te recuerda entre peticiones? | — | **No**: cada petición trae su token (*stateless*) |
>
> ```text
> Cliente ──(1) usuario + contraseña──▶ POST /api/v1/auth/login ──▶ JWT firmado
> Cliente ──(2) Authorization: Bearer <JWT> ──▶ filtro valida firma y expiración ──▶ controlador
> ```
>
> > ⚠️ **El nombre engaña.** `401` se llama *Unauthorized*, pero significa «**no autenticado**». «Sin permiso» es `403`.
>
> </details>
>
> <details>
> <summary><strong>🏛️ La arquitectura de Spring Security en una imagen</strong></summary>
>
> ```text
> Petición HTTP
>    │
>    ▼
> SecurityFilterChain  (una cadena de filtros que corre ANTES de Spring MVC)
>    ├─ JwtAuthenticationFilter ......... lee el token y deja al usuario en el SecurityContext
>    ├─ ... filtros propios de Spring Security ...
>    └─ AuthorizationFilter ............. ¿esta ruta exige estar autenticado? ¿qué rol?
>    │
>    ▼
> DispatcherServlet ──▶ Controlador ──▶ Servicio ──▶ Repositorio
> ```
>
> | Pieza | Qué hace |
> |---|---|
> | `SecurityFilterChain` | Lista de filtros y reglas de acceso |
> | `AuthenticationManager` | Recibe usuario y contraseña y decide si son válidos |
> | `UserDetailsService` | Busca la cuenta por nombre (aquí, en MySQL) |
> | `PasswordEncoder` | Cifra y compara contraseñas (aquí, BCrypt) |
> | `SecurityContextHolder` | Guarda **quién** es el usuario de **esta** petición |
>
> </details>
>
> <details>
> <summary><strong>🎓 El proyecto</strong></summary>
>
> La API de estudiantes del Módulo 07, con las tres capas, MySQL, Swagger y `ErrorResponse`. Se agregan dos rutas (`/api/v1/auth/...`) y un paquete `security`.
>
> | Ruta | Quién puede |
> |---|---|
> | `POST /api/v1/auth/login` · `GET /internal/health` | **Cualquiera** (sin token) |
> | Swagger UI y el contrato (`/swagger-ui.html`, `/api-docs/**`) | **Cualquiera** (sin token) |
> | `GET /api/v1/auth/me` · el resto de `/api/v1/students/**` (leer, crear, reemplazar, cambiar estado) | **Cualquier usuario autenticado** |
> | `DELETE /api/v1/students/{id}` · `/internal/seed` | Solo **`ADMIN`** |
>
> **Cuentas de práctica** (solo para fines educativos): `admin` / `admin123` (rol `ADMIN`) y `ana` / `ana123` (rol `USER`).
>
> </details>
>
> <details>
> <summary><strong>🔁 Antes y después</strong></summary>
>
> | Pieza | Módulo 07 | Esta guía |
> |---|---|---|
> | Acceso | Todo público | Todo protegido salvo lo declarado público |
> | Sesión | — | Ninguna (*stateless*) |
> | Cuentas | No existen | Tabla `user_account` con contraseña BCrypt |
> | Lógica de negocio (`StudentService`) | Igual | **Igual** |
> | `401` y `403` | Cuerpo vacío o genérico | `ErrorResponse` con mensaje |
> | Swagger: botón *Authorize* | Decorativo | **Funciona** |
>
> </details>
>
> <details>
> <summary><strong>📏 Situaciones de seguridad y su respuesta</strong></summary>
>
> | Código | Situación | HTTP | Mensaje (`message`) |
> |---|---|:-:|---|
> | **RN-01** | Petición protegida **sin** token | `401` | `Se requiere autenticación para acceder a este recurso` |
> | **RN-02** | Token con firma incorrecta o mal formado | `401` | `El token no es válido` |
> | **RN-03** | Token vencido | `401` | `El token expiró` |
> | **RN-04** | Login con usuario o contraseña incorrectos | `401` | `Usuario o contraseña incorrectos` |
> | **RN-05** | Autenticado, pero con un rol que no alcanza (`USER` borrando) | `403` | `No tiene permisos para realizar esta operación` |
> | **RN-06** | Login con campos vacíos | `400` | `Los datos enviados no son válidos` + `details` |
>
> > 🔎 **Un solo mensaje para usuario y contraseña.** El login **no** dice cuál de los dos falló: si lo dijera, ayudaría a quien prueba cuentas ajenas.
>
> </details>
>
> <details>
> <summary><strong>🛠️ Requerimientos y restricciones</strong></summary>
>
> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Java 21+, Spring Boot 4.1.2, Spring Security 7, JJWT 0.12.6, MySQL 8 y springdoc 3.2.0. |
> | **RNF-02** | La API es **stateless**: sin sesión (`SessionCreationPolicy.STATELESS`) y sin CSRF (no hay cookies de sesión que proteger). |
> | **RNF-03** | Las contraseñas se guardan **solo** como hash BCrypt; nunca en texto plano. |
> | **RNF-04** | La clave de firma del JWT viene de una propiedad (`app.jwt.secret`, reemplazable con la variable `JWT_SECRET`). **La del código es solo para fines educativos.** |
> | **RNF-05** | `StudentService`, `StudentController` (salvo anotaciones de Swagger) y la capa `persistence` de estudiantes **no cambian**. |
> | **RNF-06** | Todo error de seguridad usa `ErrorResponse`, igual que el resto de la API. |
> | **RNF-07** | Las excepciones propias del negocio **no** dependen de Spring Security. |
> | **RNF-08** | Nombres del código en **inglés**; mensajes para el cliente en **español**. |
>
> > 💡 **Alcance.** Quedan fuera: OAuth2, OpenID Connect, *refresh tokens*, seguridad a nivel de método (`@PreAuthorize`) y registro de usuarios. Un token **no se puede revocar** antes de que venza: es el precio de no guardar sesión en el servidor.
>
> </details>

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>

> | Paso | Minutos | Actividad |
> |---|---:|---|
> | 0 | 0 – 10 | Punto de partida: el proyecto del Módulo 07 |
> | 1 | 10 – 35 | Una dependencia: Spring Security y HTTP Basic |
> | 2 | 35 – 80 | Cuentas propias, BCrypt y la `SecurityFilterChain` |
> | 3 | 80 – 140 | JWT: login, servicio de tokens y filtro |
> | 4 | 140 – 175 | Los errores `401` y `403` con `ErrorResponse` |
> | 5 | 175 – 200 | Swagger: que *Authorize* funcione |
> | 6 | 200 – 225 | Prueba de punta a punta |
>
> > 💡 **Ritmo.** El Paso 0 es la base y puede entregarse ya hecho; el tiempo se dedica a los Pasos 1 a 6.

</details>

<details>
<summary><strong>🏗️ Arquitectura: el árbol de archivos (🆕 nuevo · ✏️ cambia)</strong></summary>

> ```text
> 📁 students-api-security
> ├── ✏️ pom.xml                                   starter-security + JJWT
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 security
>     │   │   ├── 🆕 SecurityConfig.java            SecurityFilterChain, PasswordEncoder, AuthenticationManager
>     │   │   ├── 🆕 JwtProperties.java             app.jwt.*
>     │   │   ├── 🆕 JwtService.java                emite y verifica tokens
>     │   │   ├── 🆕 JwtAuthenticationFilter.java   lee el token de cada petición
>     │   │   ├── 🆕 AuthService.java               login: valida credenciales y emite el token
>     │   │   ├── 🆕 JpaUserDetailsService.java     busca la cuenta en la base
>     │   │   ├── 🆕 AccountSeeder.java             crea admin y ana al arrancar
>     │   │   ├── 🆕 SecurityErrorWriter.java       escribe ErrorResponse desde un filtro
>     │   │   ├── 🆕 RestAuthenticationEntryPoint.java   401
>     │   │   └── 🆕 RestAccessDeniedHandler.java        403
>     │   ├── 📁 presentation
>     │   │   ├── 📁 config
>     │   │   │   └── ✏️ OpenApiConfig.java         seguridad global, 401/403 y grupo Auth
>     │   │   ├── 📁 controller
>     │   │   │   ├── 🆕 AuthController.java        login y me
>     │   │   │   ├── ✏️ StudentController.java     solo cambia una anotación de Swagger
>     │   │   │   └── ✏️ InternalController.java    anotaciones de Swagger
>     │   │   ├── 📁 dto
>     │   │   │   ├── 🆕 LoginRequest.java · LoginResponse.java · MeResponse.java
>     │   │   │   └── ErrorResponse.java · StudentRequest.java · …
>     │   │   ├── 📁 exception
>     │   │   │   └── ✏️ GlobalExceptionHandler.java   + AuthenticationException y AccessDeniedException
>     │   │   └── 📁 mapper
>     │   │       └── StudentMapper.java
>     │   ├── 📁 business                            sin cambios
>     │   └── 📁 persistence
>     │       ├── 📁 entity
>     │       │   ├── 🆕 Role.java
>     │       │   ├── 🆕 UserAccountEntity.java
>     │       │   └── StudentEntity.java · StudentStatus.java
>     │       └── 📁 repository
>     │           ├── 🆕 UserAccountJpaRepository.java
>     │           └── StudentJpaRepository.java
>     └── 📁 resources
>         └── ✏️ application.properties             app.jwt.* y persist-authorization
> ```
>
> | Capa | Qué aporta a la seguridad |
> |---|---|
> | `security` | **Todo** lo propio de Spring Security: cadena de filtros, tokens, cuentas, manejadores de error |
> | `persistence` | La cuenta (`UserAccountEntity`) y su repositorio |
> | `business` | **Nada**: el negocio no sabe que existe la seguridad |
> | `presentation` | Login (`AuthController`), DTO de login y la traducción de errores a HTTP |

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Punto de partida: el proyecto del Módulo 07</strong></summary>

> <details>
> <summary><strong>Paso 0.1 · MySQL y el proyecto</strong></summary>
>
> > Se parte del proyecto que se termina en la [guía del Módulo 07](../modulo-07-excepciones/01-guia-crud-estudiantes-con-excepciones.md) ([`students-api-excepciones/`](../modulo-07-excepciones/codigo/students-api-excepciones/)). Se copia como `students-api-security` (y se cambia el `artifactId` del `pom.xml`) y se levanta la base:
> >
> > ```bash
> > docker run -d --name academy-mysql \
> >   -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academy \
> >   -p 3306:3306 mysql:8.0
> > ```
> >
> > > | Lo que **ya existe** | Dónde |
> > > |---|---|
> > > | `StudentEntity`, `StudentStatus`, `StudentJpaRepository` | `persistence` |
> > > | `StudentService` y las excepciones `StudentNotFoundException` y `StudentEmailAlreadyExistsException` | `business` |
> > > | `StudentController`, `InternalController`, DTO, `ErrorResponse`, `GlobalExceptionHandler` | `presentation` |
> > > | `OpenApiConfig` (Swagger, grupos, respuestas de error reutilizables) | `presentation.config` |
>
> </details>
>
> <details>
> <summary><strong>Paso 0.2 · Comprobar que funciona</strong></summary>
>
> > ```bash
> > ./mvnw spring-boot:run
> > curl -X POST localhost:8080/internal/seed          # {"created":3}
> > curl localhost:8080/api/v1/students/1              # responde sin pedir nada
> > ```
> >
> > > ✅ **Punto de control.** Todo responde **sin identificarse**: `POST /internal/seed` y `DELETE` están abiertos a cualquiera. Eso es lo que se va a corregir.
>
> </details>

</details>

---

<details>
<summary><strong>🛡️ Paso 1 — Una dependencia: Spring Security y HTTP Basic</strong></summary>

> > 🗣️ **Para abrir el paso.** Se agrega **una sola dependencia** y no se escribe nada más. Se observa qué hace Spring Security **por defecto**.
>
> <details>
> <summary><strong>Paso 1.1 · Agregar <code>spring-boot-starter-security</code></strong></summary>
>
> > <details>
> > <summary><strong>📄 pom.xml (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > ```xml
> >         <dependency>
> >             <groupId>org.springframework.boot</groupId>
> >             <artifactId>spring-boot-starter-security</artifactId>
> >         </dependency>
> > ```
> >
> > </details>
> >
> > Se reinicia la aplicación. En el *log* aparece algo nuevo:
> >
> > ```text
> > Using generated security password: e48bbc0f-3a3d-4995-9f43-dce0d3161e97
> > ```
> >
> > Spring Security creó **un usuario** llamado `user` con una contraseña aleatoria que cambia en cada arranque.
>
> </details>
>
> <details>
> <summary><strong>Paso 1.2 · Ver el resultado</strong></summary>
>
> > ```bash
> > curl -i localhost:8080/api/v1/students
> > curl -i localhost:8080/internal/health
> > curl -i localhost:8080/swagger-ui.html
> > ```
> >
> > **Resultado real, las tres:**
> >
> > ```text
> > HTTP/1.1 401
> > Set-Cookie: JSESSIONID=4F2573FDFE55CB9308B9666FEFB26304; Path=/; HttpOnly
> > WWW-Authenticate: Basic realm="Realm", charset="UTF-8"
> > Content-Length: 0
> > ```
> >
> > Con el usuario `user` y la contraseña del *log*, un `GET` ya pasa:
> >
> > ```bash
> > curl -i -u user:e48bbc0f-3a3d-4995-9f43-dce0d3161e97 localhost:8080/api/v1/students/1
> > ```
> >
> > ```text
> > HTTP/1.1 404
> > {"timestamp":"2026-11-02T00:41:34.367Z","status":404,"error":"Not Found","message":"No existe un estudiante con id 1","path":"/api/v1/students/1"}
> > ```
> >
> > (Un `404` del **negocio**: la base está vacía, pero la seguridad ya dejó pasar.)
> >
> > | Lo que se ve | Qué significa |
> > |---|---|
> > | `401` + `WWW-Authenticate: Basic` | «Identifícate con HTTP Basic» |
> > | **Todo** está protegido, también `health` y Swagger | Por defecto, **nada** es público |
> > | `Set-Cookie: JSESSIONID` | Por defecto Spring Security crea una **sesión**: la API es *stateful* |
> > | El cuerpo está vacío | El `401` lo escribe Spring Security, **no** nuestro `ErrorResponse` |
>
> </details>
>
> <details>
> <summary><strong>Paso 1.3 · Una sorpresa: un <code>POST</code> con la contraseña correcta falla</strong></summary>
>
> > ```bash
> > curl -i -u user:e48bbc0f-3a3d-4995-9f43-dce0d3161e97 -X POST localhost:8080/api/v1/students \
> >   -H 'Content-Type: application/json' \
> >   -d '{"name":"Ana Torres","age":22,"email":"ana@mail.com","status":"ACTIVE"}'
> > ```
> >
> > ```text
> > HTTP/1.1 401
> > ```
> >
> > Con el *log* de seguridad en `DEBUG` se ve la causa:
> >
> > ```text
> > o.s.security.web.csrf.CsrfFilter : Invalid CSRF token found for http://localhost:8080/api/v1/students
> > o.s.security.web.FilterChainProxy : Securing POST /error
> > ```
> >
> > | Paso | Qué pasó |
> > |:-:|---|
> > | 1 | Spring Security protege contra **CSRF** y exige un token en todo `POST`/`PUT`/`DELETE` |
> > | 2 | Falla el token → responde `403`, y Spring Boot **reenvía** el error a la ruta interna `/error` |
> > | 3 | `/error` **también está protegida**, y esa nueva petición no trae credenciales → termina en `401` |
> >
> > > 🧠 **Lección.** Cuando algo «raro» ocurre con la seguridad, el código HTTP puede **no ser el de la causa**. Una API que no usa cookies de sesión **no necesita CSRF**; se desactiva en el Paso 2.
>
> </details>

</details>

---

<details>
<summary><strong>👤 Paso 2 — Cuentas propias, BCrypt y la <code>SecurityFilterChain</code></strong></summary>

> > 🗣️ **Qué falta.** Un usuario `user` con clave aleatoria no sirve. Se necesitan **cuentas reales** (en la base), contraseñas **cifradas** y **reglas** de acceso propias.
>
> <details>
> <summary><strong>Paso 2.1 · La cuenta: entidad, rol y repositorio</strong></summary>
>
> > <details>
> > <summary><strong>📄 Role.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/entity/Role.java`
> >
> > ```java
> > package com.academy.persistence.entity;
> >
> > public enum Role {
> >     ADMIN,
> >     USER
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 UserAccountEntity.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/entity/UserAccountEntity.java`
> >
> > ```java
> > package com.academy.persistence.entity;
> >
> > import jakarta.persistence.Column;
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.EnumType;
> > import jakarta.persistence.Enumerated;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import jakarta.persistence.Table;
> >
> > @Entity
> > @Table(name = "user_account")
> > public class UserAccountEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     @Column(nullable = false, unique = true)
> >     private String username;
> >
> >     // Siempre el hash BCrypt, nunca la contraseña en texto plano
> >     @Column(name = "password_hash", nullable = false)
> >     private String passwordHash;
> >
> >     @Enumerated(EnumType.STRING)
> >     @Column(nullable = false)
> >     private Role role;
> >
> >     protected UserAccountEntity() {
> >     }
> >
> >     public UserAccountEntity(Long id, String username, String passwordHash, Role role) {
> >         this.id = id;
> >         this.username = username;
> >         this.passwordHash = passwordHash;
> >         this.role = role;
> >     }
> >
> >     public Long getId() { return id; }
> >     public String getUsername() { return username; }
> >     public String getPasswordHash() { return passwordHash; }
> >     public Role getRole() { return role; }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 UserAccountJpaRepository.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/repository/UserAccountJpaRepository.java`
> >
> > ```java
> > package com.academy.persistence.repository;
> >
> > import com.academy.persistence.entity.UserAccountEntity;
> > import org.springframework.data.jpa.repository.JpaRepository;
> >
> > import java.util.Optional;
> >
> > public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, Long> {
> >
> >     Optional<UserAccountEntity> findByUsername(String username);
> > }
> > ```
> >
> > </details>
> >
> > > 💡 Es una entidad **más**, como `StudentEntity`: no hay nada «mágico». Hibernate crea la tabla `user_account` al arrancar (`ddl-auto=update`).
>
> </details>
>
> <details>
> <summary><strong>Paso 2.2 · Conectar la cuenta con Spring Security: <code>UserDetailsService</code></strong></summary>
>
> > Spring Security no conoce `UserAccountEntity`. Se le explica **cómo buscar una cuenta** implementando `UserDetailsService`:
> >
> > <details>
> > <summary><strong>📄 JpaUserDetailsService.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/security/JpaUserDetailsService.java`
> >
> > ```java
> > package com.academy.security;
> >
> > import com.academy.persistence.entity.UserAccountEntity;
> > import com.academy.persistence.repository.UserAccountJpaRepository;
> > import org.springframework.security.core.userdetails.User;
> > import org.springframework.security.core.userdetails.UserDetails;
> > import org.springframework.security.core.userdetails.UserDetailsService;
> > import org.springframework.security.core.userdetails.UsernameNotFoundException;
> > import org.springframework.stereotype.Service;
> >
> > @Service
> > public class JpaUserDetailsService implements UserDetailsService {
> >
> >     private final UserAccountJpaRepository accounts;
> >
> >     public JpaUserDetailsService(UserAccountJpaRepository accounts) {
> >         this.accounts = accounts;
> >     }
> >
> >     @Override
> >     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
> >         UserAccountEntity account = accounts.findByUsername(username)
> >                 .orElseThrow(() -> new UsernameNotFoundException("No existe la cuenta " + username));
> >         return User.withUsername(account.getUsername())
> >                 .password(account.getPasswordHash())
> >                 .roles(account.getRole().name())
> >                 .build();
> >     }
> > }
> > ```
> >
> > </details>
> >
> > | Detalle | Por qué |
> > |---|---|
> > | `.roles("ADMIN")` | Spring le antepone `ROLE_`: la autoridad queda `ROLE_ADMIN` |
> > | Devuelve el **hash**, no la contraseña | Quien compara es el `PasswordEncoder`, no el servicio |
> >
> > > 🔎 **Sin escribir nada más** Spring Boot arma un `DaoAuthenticationProvider` que usa este servicio y el `PasswordEncoder` que se declara en el paso siguiente.
>
> </details>
>
> <details>
> <summary><strong>Paso 2.3 · Dos cuentas de práctica con la contraseña cifrada</strong></summary>
>
> > <details>
> > <summary><strong>📄 AccountSeeder.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/security/AccountSeeder.java`
> >
> > ```java
> > package com.academy.security;
> >
> > import com.academy.persistence.entity.Role;
> > import com.academy.persistence.entity.UserAccountEntity;
> > import com.academy.persistence.repository.UserAccountJpaRepository;
> > import org.springframework.boot.CommandLineRunner;
> > import org.springframework.security.crypto.password.PasswordEncoder;
> > import org.springframework.stereotype.Component;
> >
> > /**
> >  * Crea dos cuentas de práctica al arrancar. SOLO PARA FINES EDUCATIVOS:
> >  * en un proyecto real las cuentas no se siembran con contraseñas en el código.
> >  */
> > @Component
> > public class AccountSeeder implements CommandLineRunner {
> >
> >     private final UserAccountJpaRepository accounts;
> >     private final PasswordEncoder encoder;
> >
> >     public AccountSeeder(UserAccountJpaRepository accounts, PasswordEncoder encoder) {
> >         this.accounts = accounts;
> >         this.encoder = encoder;
> >     }
> >
> >     @Override
> >     public void run(String... args) {
> >         createIfMissing("admin", "admin123", Role.ADMIN);
> >         createIfMissing("ana", "ana123", Role.USER);
> >     }
> >
> >     private void createIfMissing(String username, String rawPassword, Role role) {
> >         if (accounts.findByUsername(username).isEmpty()) {
> >             accounts.save(new UserAccountEntity(null, username, encoder.encode(rawPassword), role));
> >         }
> >     }
> > }
> > ```
> >
> > </details>
> >
> > `encoder.encode(...)` es **BCrypt**: un cifrado **de una sola vía** (no se puede «descifrar») que además agrega una **sal** aleatoria, así que la misma contraseña da hashes distintos. Tabla real después de arrancar:
> >
> > ```text
> > mysql> select id, username, password_hash, role from user_account;
> > +----+----------+--------------------------------------------------------------+-------+
> > | id | username | password_hash                                                | role  |
> > +----+----------+--------------------------------------------------------------+-------+
> > |  1 | admin    | $2a$10$1X4wlWQ8uBAeaf/KYcX7VOlqCV.iAxJ0ycgY3qwCeMq38Cr0AFN/K | ADMIN |
> > |  2 | ana      | $2a$10$H7ByiCrlntVPK.1q9yAExeibDNhd06jLnYoYVtxmkK1MayL8tBZbW | USER  |
> > +----+----------+--------------------------------------------------------------+-------+
> > ```
> >
> > > ⚠️ **Solo educativo.** Contraseñas escritas en el código y cuentas sembradas al arrancar **no** son para un proyecto real.
>
> </details>
>
> <details>
> <summary><strong>Paso 2.4 · La cadena de filtros: <code>SecurityFilterChain</code></strong></summary>
>
> > <details>
> > <summary><strong>📄 SecurityConfig.java (versión del Paso 2)</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/security/SecurityConfig.java`
> >
> > ```java
> > package com.academy.security;
> >
> > import org.springframework.context.annotation.Bean;
> > import org.springframework.context.annotation.Configuration;
> > import org.springframework.http.HttpMethod;
> > import org.springframework.security.config.Customizer;
> > import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> > import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
> > import org.springframework.security.config.http.SessionCreationPolicy;
> > import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
> > import org.springframework.security.crypto.password.PasswordEncoder;
> > import org.springframework.security.web.SecurityFilterChain;
> >
> > @Configuration
> > @EnableWebSecurity
> > public class SecurityConfig {
> >
> >     @Bean
> >     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
> >         http
> >                 .csrf(csrf -> csrf.disable())
> >                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
> >                 .authorizeHttpRequests(auth -> auth
> >                         .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/internal/health").permitAll()
> >                         .requestMatchers(HttpMethod.DELETE, "/api/v1/students/**").hasRole("ADMIN")
> >                         .requestMatchers("/internal/**").hasRole("ADMIN")
> >                         .anyRequest().authenticated())
> >                 .httpBasic(Customizer.withDefaults());
> >         return http.build();
> >     }
> >
> >     @Bean
> >     public PasswordEncoder passwordEncoder() {
> >         return new BCryptPasswordEncoder();
> >     }
> > }
> > ```
> >
> > </details>
> >
> > | Línea | Qué decide |
> > |---|---|
> > | `csrf.disable()` | No hay cookies de sesión que proteger (RNF-02) |
> > | `STATELESS` | Spring Security **no crea ni usa sesión** (adiós `JSESSIONID`) |
> > | `permitAll()` | Rutas públicas |
> > | `hasRole("ADMIN")` | Solo ese rol (`DELETE` y `/internal/**`) |
> > | `anyRequest().authenticated()` | **Todo lo demás** exige estar autenticado |
> > | `httpBasic(...)` | El mecanismo para identificarse: usuario y contraseña en **cada** petición |
> >
> > > 🔎 **El orden importa.** Las reglas se evalúan **de arriba hacia abajo** y gana la primera que coincide. `anyRequest()` va siempre al final.
>
> </details>
>
> <details>
> <summary><strong>Paso 2.5 · Ver el resultado</strong></summary>
>
> > ```bash
> > curl -i localhost:8080/api/v1/students                                  # sin credenciales
> > curl -i -u admin:admin123 -X POST localhost:8080/internal/seed
> > curl -i -u ana:ana123 localhost:8080/api/v1/students/1
> > curl -i -u ana:ana123 -X POST localhost:8080/internal/seed              # USER en una ruta de ADMIN
> > curl -i -u ana:ana123 -X DELETE localhost:8080/api/v1/students/3
> > curl -i -u admin:admin123 -X DELETE localhost:8080/api/v1/students/3
> > curl -i -u ana:malaclave localhost:8080/api/v1/students/1
> > curl -i localhost:8080/internal/health
> > ```
> >
> > **Resultado real:**
> >
> > | # | Petición | Respuesta |
> > |:-:|---|---|
> > | 1 | `GET /students` **sin** credenciales | `401` · `WWW-Authenticate: Basic` · cuerpo vacío |
> > | 2 | `admin` → `POST /internal/seed` | `200` · `{"created":3}` |
> > | 3 | `ana` → `GET /students/1` | `200` · Ana Torres |
> > | 4 | `ana` (`USER`) → `POST /internal/seed` | **`403`** · `{"timestamp":"…","status":403,"error":"Forbidden","path":"/internal/seed"}` |
> > | 5 | `ana` (`USER`) → `DELETE /students/3` | **`403`** · el mismo cuerpo genérico |
> > | 6 | `admin` → `DELETE /students/3` | `204` |
> > | 7 | `ana` con contraseña mala | `401` · cuerpo vacío |
> > | 8 | `GET /internal/health` sin credenciales | `200` · `{"status":"UP"}` |
> >
> > > 🧠 **Para ver la diferencia.** La fila 1 es «no sé quién eres» (`401`); la fila 4 es «sé quién eres, pero no puedes» (`403`).
> >
> > | Se logró | Falta |
> > |---|---|
> > | Cuentas en la base, reglas por rol, `401` y `403` correctos | Cada petición **reenvía la contraseña**. Hay que cifrarla, compararla con BCrypt **y** consultar la base **en cada llamada** |
> > | Contraseñas cifradas | Los errores `401` y `403` **no** tienen el formato de `ErrorResponse` |
>
> </details>

</details>

---

<details>
<summary><strong>🎫 Paso 3 — JWT: login, servicio de tokens y filtro</strong></summary>

> > 🗣️ **Stateful frente a stateless.**
> >
> > | | *Stateful* (sesión) | *Stateless* (token) |
> > |---|---|---|
> > | ¿Qué recuerda el servidor? | Una sesión por usuario conectado | **Nada** |
> > | ¿Qué envía el cliente? | Una cookie con el id de sesión | Un token firmado en cada petición |
> > | Crecer con varios servidores | Hay que compartir las sesiones | Cualquier servidor valida el token |
> > | Cerrar una sesión a la fuerza | Se borra la sesión | **No se puede** hasta que el token venza |
> >
> > Un **JWT** (*JSON Web Token*) es **un texto firmado** que lleva dentro quién es el usuario y hasta cuándo vale. El servidor **no lo guarda**: solo comprueba la firma.
>
> <details>
> <summary><strong>Paso 3.1 · Dependencias y propiedades</strong></summary>
>
> > <details>
> > <summary><strong>📄 pom.xml (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > ```xml
> >         <dependency>
> >             <groupId>io.jsonwebtoken</groupId>
> >             <artifactId>jjwt-api</artifactId>
> >             <version>0.12.6</version>
> >         </dependency>
> >         <dependency>
> >             <groupId>io.jsonwebtoken</groupId>
> >             <artifactId>jjwt-impl</artifactId>
> >             <version>0.12.6</version>
> >             <scope>runtime</scope>
> >         </dependency>
> >         <dependency>
> >             <groupId>io.jsonwebtoken</groupId>
> >             <artifactId>jjwt-jackson</artifactId>
> >             <version>0.12.6</version>
> >             <scope>runtime</scope>
> >         </dependency>
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 application.properties (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > ```properties
> > # JWT -- SOLO PARA FINES EDUCATIVOS: en un proyecto real la clave se entrega por
> > # variable de entorno (JWT_SECRET) o un gestor de secretos, nunca escrita en el repositorio.
> > app.jwt.secret=${JWT_SECRET:academy-clave-solo-educativa-cambiar-en-produccion-0123456789}
> > app.jwt.expiration-seconds=3600
> > ```
> >
> > </details>
> >
> > > ⚠️ **Advertencia.** Esa clave **no es apta para producción**: quien la conozca puede fabricar tokens válidos. La construcción `${JWT_SECRET:…}` toma la variable de entorno si existe y, si no, usa el valor de práctica. Para firmar con HS256 la clave debe tener **al menos 32 caracteres**.
>
> </details>
>
> <details>
> <summary><strong>Paso 3.2 · Emitir y verificar tokens: <code>JwtProperties</code> y <code>JwtService</code></strong></summary>
>
> > <details>
> > <summary><strong>📄 JwtProperties.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/security/JwtProperties.java`
> >
> > ```java
> > package com.academy.security;
> >
> > import org.springframework.boot.context.properties.ConfigurationProperties;
> >
> > @ConfigurationProperties(prefix = "app.jwt")
> > public record JwtProperties(String secret, long expirationSeconds) {
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 JwtService.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/security/JwtService.java`
> >
> > ```java
> > package com.academy.security;
> >
> > import io.jsonwebtoken.Claims;
> > import io.jsonwebtoken.Jwts;
> > import io.jsonwebtoken.security.Keys;
> > import org.springframework.stereotype.Service;
> >
> > import javax.crypto.SecretKey;
> > import java.nio.charset.StandardCharsets;
> > import java.time.Instant;
> > import java.util.Date;
> >
> > @Service
> > public class JwtService {
> >
> >     private final SecretKey key;
> >     private final long expirationSeconds;
> >
> >     public JwtService(JwtProperties properties) {
> >         this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
> >         this.expirationSeconds = properties.expirationSeconds();
> >     }
> >
> >     public String generate(String username, String role) {
> >         Instant now = Instant.now();
> >         return Jwts.builder()
> >                 .subject(username)
> >                 .claim("role", role)
> >                 .issuedAt(Date.from(now))
> >                 .expiration(Date.from(now.plusSeconds(expirationSeconds)))
> >                 .signWith(key, Jwts.SIG.HS256)
> >                 .compact();
> >     }
> >
> >     /** Verifica la firma y la expiración; lanza JwtException si el token no sirve. */
> >     public Claims parse(String token) {
> >         return Jwts.parser()
> >                 .verifyWith(key)
> >                 .build()
> >                 .parseSignedClaims(token)
> >                 .getPayload();
> >     }
> >
> >     public long expirationSeconds() {
> >         return expirationSeconds;
> >     }
> > }
> > ```
> >
> > </details>
> >
> > | Pieza del token | Lo que escribe `generate` |
> > |---|---|
> > | `subject` | El usuario (`admin`) |
> > | `role` | Una **reclamación** propia: el rol |
> > | `iat` / `exp` | Cuándo se emitió y cuándo vence |
> > | Firma | `HS256`: un HMAC con la clave secreta |
> >
> > > 🔎 `parse` **verifica la firma y la fecha** antes de devolver nada. Si algo no cuadra, lanza una excepción (`ExpiredJwtException`, `SignatureException`, `MalformedJwtException`…), todas hijas de `JwtException`.
>
> </details>
>
> <details>
> <summary><strong>Paso 3.3 · El filtro que lee el token en cada petición</strong></summary>
>
> > <details>
> > <summary><strong>📄 JwtAuthenticationFilter.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/security/JwtAuthenticationFilter.java`
> >
> > ```java
> > package com.academy.security;
> >
> > import io.jsonwebtoken.Claims;
> > import io.jsonwebtoken.ExpiredJwtException;
> > import io.jsonwebtoken.JwtException;
> > import jakarta.servlet.FilterChain;
> > import jakarta.servlet.ServletException;
> > import jakarta.servlet.http.HttpServletRequest;
> > import jakarta.servlet.http.HttpServletResponse;
> > import org.springframework.http.HttpHeaders;
> > import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
> > import org.springframework.security.core.authority.SimpleGrantedAuthority;
> > import org.springframework.security.core.context.SecurityContextHolder;
> > import org.springframework.web.filter.OncePerRequestFilter;
> >
> > import java.io.IOException;
> > import java.util.List;
> >
> > public class JwtAuthenticationFilter extends OncePerRequestFilter {
> >
> >     /** Atributo de la petición donde el filtro deja el motivo si el token no sirve. */
> >     public static final String AUTH_ERROR_ATTRIBUTE = "auth.error";
> >
> >     private static final String BEARER_PREFIX = "Bearer ";
> >
> >     private final JwtService jwtService;
> >
> >     public JwtAuthenticationFilter(JwtService jwtService) {
> >         this.jwtService = jwtService;
> >     }
> >
> >     @Override
> >     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
> >                                     FilterChain chain) throws ServletException, IOException {
> >         String header = request.getHeader(HttpHeaders.AUTHORIZATION);
> >         if (header != null && header.startsWith(BEARER_PREFIX)) {
> >             try {
> >                 Claims claims = jwtService.parse(header.substring(BEARER_PREFIX.length()));
> >                 var authentication = new UsernamePasswordAuthenticationToken(
> >                         claims.getSubject(), null,
> >                         List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class))));
> >                 SecurityContextHolder.getContext().setAuthentication(authentication);
> >             } catch (ExpiredJwtException ex) {
> >                 request.setAttribute(AUTH_ERROR_ATTRIBUTE, "El token expiró");
> >             } catch (JwtException | IllegalArgumentException ex) {
> >                 request.setAttribute(AUTH_ERROR_ATTRIBUTE, "El token no es válido");
> >             }
> >         }
> >         chain.doFilter(request, response);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > | Qué hace | Por qué |
> > |---|---|
> > | Lee `Authorization: Bearer <token>` | Es el formato estándar |
> > | Si el token sirve, **deja al usuario en el `SecurityContext`** | Desde ahí, las reglas de `SecurityConfig` saben quién es |
> > | Si el token **no** sirve, **no lanza nada**: anota el motivo y sigue | Quien decide si se rechaza es la cadena (la ruta puede ser pública) |
> > | **No consulta la base** | Esa es la gracia del *stateless*: basta con verificar la firma |
> >
> > > ⚠️ **Es una clase normal, sin `@Component`.** Si fuera un *bean*, Spring Boot la registraría **dos veces** (una como filtro del servidor y otra dentro de la cadena de seguridad). Por eso se crea con `new` dentro de `SecurityConfig`.
>
> </details>
>
> <details>
> <summary><strong>Paso 3.4 · El login: <code>AuthService</code>, DTO y <code>AuthController</code></strong></summary>
>
> > <details>
> > <summary><strong>📄 AuthService.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/security/AuthService.java`
> >
> > ```java
> > package com.academy.security;
> >
> > import org.springframework.security.authentication.AuthenticationManager;
> > import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
> > import org.springframework.security.core.Authentication;
> > import org.springframework.stereotype.Service;
> >
> > @Service
> > public class AuthService {
> >
> >     private final AuthenticationManager authenticationManager;
> >     private final JwtService jwtService;
> >
> >     public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
> >         this.authenticationManager = authenticationManager;
> >         this.jwtService = jwtService;
> >     }
> >
> >     /** Valida usuario y contraseña (lanza AuthenticationException si no coinciden) y emite el JWT. */
> >     public String login(String username, String password) {
> >         Authentication authentication = authenticationManager.authenticate(
> >                 new UsernamePasswordAuthenticationToken(username, password));
> >         String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
> >         return jwtService.generate(authentication.getName(), role);
> >     }
> >
> >     public long expirationSeconds() {
> >         return jwtService.expirationSeconds();
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 LoginRequest.java · LoginResponse.java · MeResponse.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/`
> >
> > ```java
> > @Schema(description = "Credenciales para iniciar sesión")
> > public record LoginRequest(
> >         @Schema(description = "Nombre de usuario", example = "admin")
> >         @NotBlank(message = "es obligatorio")
> >         String username,
> >
> >         @Schema(description = "Contraseña", example = "admin123")
> >         @NotBlank(message = "es obligatoria")
> >         String password
> > ) {}
> >
> > @Schema(description = "Token que el cliente debe enviar en cada petición")
> > public record LoginResponse(String token, String tokenType, long expiresIn) {}
> >
> > @Schema(description = "Quién es el usuario autenticado")
> > public record MeResponse(String username, String role) {}
> > ```
> >
> > (En el código final cada `record` está en su archivo y con el `@Schema` de cada campo.)
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 AuthController.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/AuthController.java`
> >
> > ```java
> > @RestController
> > @RequestMapping("/api/v1/auth")
> > public class AuthController {
> >
> >     private final AuthService service;
> >
> >     public AuthController(AuthService service) {
> >         this.service = service;
> >     }
> >
> >     @PostMapping("/login")
> >     public LoginResponse login(@Valid @RequestBody LoginRequest request) {
> >         String token = service.login(request.username(), request.password());
> >         return new LoginResponse(token, "Bearer", service.expirationSeconds());
> >     }
> >
> >     @GetMapping("/me")
> >     public MeResponse me(Authentication authentication) {
> >         String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
> >         return new MeResponse(authentication.getName(), role);
> >     }
> > }
> > ```
> >
> > (El archivo final lleva además las anotaciones de Swagger del Paso 5.)
> >
> > </details>
> >
> > | Pieza | Idea |
> > |---|---|
> > | `authenticationManager.authenticate(...)` | Spring Security usa `JpaUserDetailsService` + BCrypt; si no coincide, **lanza** `AuthenticationException` |
> > | `GET /me` recibe un `Authentication` | Spring MVC lo toma del `SecurityContext`: demuestra que el filtro funcionó |
>
> </details>
>
> <details>
> <summary><strong>Paso 3.5 · Cambiar la cadena: JWT en lugar de Basic</strong></summary>
>
> > Tres cambios en `SecurityConfig`: **quitar `httpBasic`**, **agregar el filtro** antes del filtro de usuario y contraseña de Spring, y **exponer el `AuthenticationManager`**. También se hace pública la ruta de login.
> >
> > <details>
> > <summary><strong>📄 SecurityConfig.java (versión del Paso 3)</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/security/SecurityConfig.java`
> >
> > ```java
> > @Configuration
> > @EnableWebSecurity
> > @EnableConfigurationProperties(JwtProperties.class)
> > public class SecurityConfig {
> >
> >     @Bean
> >     public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
> >         http
> >                 .csrf(csrf -> csrf.disable())
> >                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
> >                 .authorizeHttpRequests(auth -> auth
> >                         .requestMatchers("/api/v1/auth/login", "/internal/health").permitAll()
> >                         .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll()
> >                         .requestMatchers(HttpMethod.DELETE, "/api/v1/students/**").hasRole("ADMIN")
> >                         .requestMatchers("/internal/**").hasRole("ADMIN")
> >                         .anyRequest().authenticated())
> >                 .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
> >         return http.build();
> >     }
> >
> >     @Bean
> >     public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
> >         return configuration.getAuthenticationManager();
> >     }
> >
> >     @Bean
> >     public PasswordEncoder passwordEncoder() {
> >         return new BCryptPasswordEncoder();
> >     }
> > }
> > ```
> >
> > </details>
>
> </details>
>
> <details>
> <summary><strong>Paso 3.6 · Iniciar sesión y mirar el token</strong></summary>
>
> > ```bash
> > curl -i -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
> >   -d '{"username":"admin","password":"admin123"}'
> > ```
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTc5MzU4MDIxNCwiZXhwIjoxNzkzNTgzODE0fQ.vxkHg0GFzftFHUfJODWru1f2VXO4g2wn--I2-fONER8","tokenType":"Bearer","expiresIn":3600}
> > ```
> >
> > El token son **tres partes separadas por puntos**. Las dos primeras son JSON codificado en Base64 (**no cifrado**: cualquiera puede leerlo):
> >
> > | Parte | Contenido (decodificado) |
> > |---|---|
> > | **Cabecera** | `{"alg":"HS256"}` |
> > | **Carga** (*payload*) | `{"sub":"admin","role":"ADMIN","iat":1793580214,"exp":1793583814}` |
> > | **Firma** | `vxkHg0GFzftFHUfJODWru1f2VXO4g2wn--I2-fONER8` |
> >
> > > ⚠️ **No se pone nada secreto en la carga.** Está a la vista. La firma **no la oculta**: solo garantiza que **nadie la modificó**. `exp − iat = 3600` s, la hora de vigencia configurada.
> >
> > Se usa el token con `Authorization: Bearer`:
> >
> > ```bash
> > TOKEN=eyJhbGciOiJIUzI1NiJ9...
> > curl -i -H "Authorization: Bearer $TOKEN" localhost:8080/api/v1/auth/me
> > ```
> >
> > ```text
> > HTTP/1.1 200
> > {"username":"ana","role":"USER"}          # con el token de ana
> > ```
> >
> > Y la matriz de acceso, con los dos tokens (`ana` = `USER`, `admin` = `ADMIN`):
> >
> > | # | Petición | Respuesta |
> > |:-:|---|---|
> > | 1 | `admin` → `POST /internal/seed` | `200` · `{"created":3}` |
> > | 2 | `ana` → `GET /students/1` | `200` |
> > | 3 | `ana` → `DELETE /students/3` | `403` · cuerpo vacío |
> > | 4 | `admin` → `DELETE /students/3` | `204` |
> > | 5 | token con **una letra de la carga cambiada** → `GET /students/1` | `403` · cuerpo vacío |
> > | 6 | `Authorization: Bearer basura` → `GET /students/1` | `403` · cuerpo vacío |
> > | 7 | **sin** token → `GET /students` | `403` · cuerpo vacío |
> > | 8 | login con contraseña mala | **`500`** · `Ocurrió un error inesperado…` |
> > | 9 | login con campos vacíos | `400` · `details` de `password` y `username` |
> >
> > > ✅ **Lo que funciona.** El token **manipulado** (fila 5) se rechaza: al cambiar un carácter la firma ya no coincide, y el servidor lo descubre **sin consultar nada**.
> >
> > Para fabricar el token alterado de la fila 5 se cambia la primera letra de la carga (la segunda parte, entre los dos puntos):
> >
> > ```bash
> > TAMPERED=$(echo "$ADMIN" | sed -E 's/^([^.]*\.)./\1X/')
> > curl -i -H "Authorization: Bearer $TAMPERED" localhost:8080/api/v1/students/1
> > ```
> >
> > > ⚠️ **No conviene cambiar la última letra del token.** En Base64 esa letra lleva bits sin uso, y a veces el cambio **no se nota** (en una prueba, el token «alterado» siguió siendo válido). Cambiar una letra de la carga siempre rompe la firma.
>
> </details>
>
> <details>
> <summary><strong>Paso 3.7 · Tres respuestas que están mal</strong></summary>
>
> > | Fila | Qué pasa | Qué debería pasar |
> > |:-:|---|---|
> > | 5, 6, 7 | Sin token o con token inválido responde **`403`** | **`401`**: «no te identificaste» |
> > | 3 | El `403` no trae cuerpo | Un `ErrorResponse` |
> > | 8 | Una contraseña mala responde **`500`** y llena el *log* de errores | **`401`** |
> >
> > Dos causas:
> >
> > | Causa | Detalle |
> > |---|---|
> > | **No hay mecanismo de autenticación configurado.** Sin `httpBasic` ni `formLogin`, Spring Security no sabe qué contestar a un anónimo y usa una entrada por defecto que responde `403`. | Hace falta un `AuthenticationEntryPoint` propio |
> > | **El login falla dentro de un controlador.** `BadCredentialsException` llega al `@ExceptionHandler(Exception.class)` del Módulo 07 y se vuelve `500`. | Hace falta un manejador para `AuthenticationException` |
> >
> > En el *log*: `ERROR … GlobalExceptionHandler : Error inesperado en /api/v1/auth/login` seguido de `org.springframework.security.authentication.BadCredentialsException: Bad credentials`. Es el **mismo tipo de trampa** del Módulo 07: el `catch-all` se traga excepciones que **no son inesperadas**.
>
> </details>

</details>

---

<details>
<summary><strong>🚦 Paso 4 — Los errores <code>401</code> y <code>403</code> con <code>ErrorResponse</code></strong></summary>

> > 🗣️ **Por qué no basta el `@RestControllerAdvice`.** Los filtros de seguridad se ejecutan **antes** de Spring MVC. Si el filtro rechaza la petición, **nunca llega** a un controlador y `GlobalExceptionHandler` **ni se entera**. Hay que arreglar dos lugares distintos:
> >
> > ```text
> > Petición ─▶ [ Filtros de Spring Security ] ─▶ [ Spring MVC: controlador ] 
> >                  │                                      │
> >                  ▼                                      ▼
> >     EntryPoint (401) · AccessDeniedHandler (403)   GlobalExceptionHandler
> > ```
>
> <details>
> <summary><strong>Paso 4.1 · Escribir un <code>ErrorResponse</code> desde un filtro</strong></summary>
>
> > <details>
> > <summary><strong>📄 SecurityErrorWriter.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/security/SecurityErrorWriter.java`
> >
> > ```java
> > package com.academy.security;
> >
> > import com.academy.presentation.dto.ErrorResponse;
> > import tools.jackson.databind.ObjectMapper;
> > import jakarta.servlet.http.HttpServletRequest;
> > import jakarta.servlet.http.HttpServletResponse;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.http.MediaType;
> > import org.springframework.stereotype.Component;
> >
> > import java.io.IOException;
> >
> > /**
> >  * Escribe un ErrorResponse a mano. Hace falta porque los filtros de seguridad corren ANTES de Spring MVC:
> >  * el @RestControllerAdvice nunca llega a ver estos errores.
> >  */
> > @Component
> > public class SecurityErrorWriter {
> >
> >     private final ObjectMapper objectMapper;
> >
> >     public SecurityErrorWriter(ObjectMapper objectMapper) {
> >         this.objectMapper = objectMapper;
> >     }
> >
> >     public void write(HttpServletRequest request, HttpServletResponse response,
> >                       HttpStatus status, String message) throws IOException {
> >         response.setStatus(status.value());
> >         response.setContentType(MediaType.APPLICATION_JSON_VALUE);
> >         response.setCharacterEncoding("UTF-8");
> >         objectMapper.writeValue(response.getWriter(),
> >                 ErrorResponse.of(status, message, request.getRequestURI()));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > 💡 Usa el `ObjectMapper` de Spring Boot, así que el `timestamp` sale con el mismo formato que en el resto de la API.
> > >
> > > ⚠️ **Cambio de Spring Boot 4.** Ahora el `ObjectMapper` es el de **Jackson 3** y se importa de `tools.jackson.databind.ObjectMapper`. Con Spring Boot 3 era `com.fasterxml.jackson.databind.ObjectMapper`: si se deja ese `import`, la aplicación **no arranca** (`required a bean of type 'com.fasterxml.jackson.databind.ObjectMapper' that could not be found`). Las anotaciones (`@JsonInclude`…) siguen en `com.fasterxml.jackson.annotation`.
>
> </details>
>
> <details>
> <summary><strong>Paso 4.2 · <code>401</code>: <code>AuthenticationEntryPoint</code></strong></summary>
>
> > <details>
> > <summary><strong>📄 RestAuthenticationEntryPoint.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/security/RestAuthenticationEntryPoint.java`
> >
> > ```java
> > package com.academy.security;
> >
> > import jakarta.servlet.http.HttpServletRequest;
> > import jakarta.servlet.http.HttpServletResponse;
> > import org.springframework.http.HttpHeaders;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.security.core.AuthenticationException;
> > import org.springframework.security.web.AuthenticationEntryPoint;
> > import org.springframework.stereotype.Component;
> >
> > import java.io.IOException;
> >
> > /** Se ejecuta cuando alguien SIN autenticar pide un recurso protegido: responde 401. */
> > @Component
> > public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
> >
> >     private final SecurityErrorWriter errors;
> >
> >     public RestAuthenticationEntryPoint(SecurityErrorWriter errors) {
> >         this.errors = errors;
> >     }
> >
> >     @Override
> >     public void commence(HttpServletRequest request, HttpServletResponse response,
> >                          AuthenticationException authException) throws IOException {
> >         Object reason = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE);
> >         String message = reason != null ? reason.toString() : "Se requiere autenticación para acceder a este recurso";
> >         response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
> >         errors.write(request, response, HttpStatus.UNAUTHORIZED, message);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > 🔎 **Dónde se usa el motivo del filtro.** Si el filtro anotó «El token expiró» o «El token no es válido», el mensaje del `401` lo dice; si no había token, usa el mensaje general. La cabecera `WWW-Authenticate: Bearer` le avisa al cliente **qué esquema** se espera.
>
> </details>
>
> <details>
> <summary><strong>Paso 4.3 · <code>403</code>: <code>AccessDeniedHandler</code></strong></summary>
>
> > <details>
> > <summary><strong>📄 RestAccessDeniedHandler.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/security/RestAccessDeniedHandler.java`
> >
> > ```java
> > package com.academy.security;
> >
> > import jakarta.servlet.http.HttpServletRequest;
> > import jakarta.servlet.http.HttpServletResponse;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.security.access.AccessDeniedException;
> > import org.springframework.security.web.access.AccessDeniedHandler;
> > import org.springframework.stereotype.Component;
> >
> > import java.io.IOException;
> >
> > /** Se ejecuta cuando alguien YA autenticado pide algo que su rol no permite: responde 403. */
> > @Component
> > public class RestAccessDeniedHandler implements AccessDeniedHandler {
> >
> >     private final SecurityErrorWriter errors;
> >
> >     public RestAccessDeniedHandler(SecurityErrorWriter errors) {
> >         this.errors = errors;
> >     }
> >
> >     @Override
> >     public void handle(HttpServletRequest request, HttpServletResponse response,
> >                        AccessDeniedException accessDeniedException) throws IOException {
> >         errors.write(request, response, HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación");
> >     }
> > }
> > ```
> >
> > </details>
>
> </details>
>
> <details>
> <summary><strong>Paso 4.4 · Registrarlos en la cadena</strong></summary>
>
> > <details>
> > <summary><strong>📄 SecurityConfig.java</strong> <em>(solo el cambio)</em></summary>
> >
> > ```java
> >     @Bean
> >     public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService,
> >                                            RestAuthenticationEntryPoint entryPoint,
> >                                            RestAccessDeniedHandler accessDeniedHandler) throws Exception {
> >         http
> >                 .csrf(csrf -> csrf.disable())
> >                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
> >                 .exceptionHandling(errors -> errors
> >                         .authenticationEntryPoint(entryPoint)
> >                         .accessDeniedHandler(accessDeniedHandler))
> >                 .authorizeHttpRequests(auth -> auth
> >                         ...
> > ```
> >
> > Con esto, la **versión final** de `SecurityConfig` (completa) es la de «Estructura final esperada».
> >
> > </details>
>
> </details>
>
> <details>
> <summary><strong>Paso 4.5 · El login: otra vez el <code>GlobalExceptionHandler</code></strong></summary>
>
> > El login **sí** pasa por Spring MVC (la excepción nace dentro del controlador), así que se arregla en el manejador del Módulo 07:
> >
> > <details>
> > <summary><strong>📄 GlobalExceptionHandler.java (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > ```java
> >     // El login falla dentro de un controlador (AuthenticationManager), así que SÍ pasa por este manejador.
> >     @ExceptionHandler(AuthenticationException.class)
> >     public ResponseEntity<Object> handleAuthentication(AuthenticationException ex, WebRequest request) {
> >         return build(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", request, null);
> >     }
> >
> >     // Sin este manejador, el catch-all convertiría un AccessDeniedException en un 500.
> >     @ExceptionHandler(AccessDeniedException.class)
> >     public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
> >         return build(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación", request, null);
> >     }
> > ```
> >
> > Imports: `org.springframework.security.access.AccessDeniedException` y `org.springframework.security.core.AuthenticationException`.
> >
> > </details>
> >
> > | Excepción | Dónde nace | Quién la maneja | HTTP |
> > |---|---|---|:-:|
> > | Falta o falla el token | Filtro de seguridad | `RestAuthenticationEntryPoint` | `401` |
> > | Rol insuficiente en una regla de la cadena | Filtro de seguridad | `RestAccessDeniedHandler` | `403` |
> > | Contraseña incorrecta en el login | **Controlador** (`AuthService`) | `GlobalExceptionHandler` | `401` |
> > | `AccessDeniedException` lanzada dentro de MVC | **Controlador / servicio** | `GlobalExceptionHandler` | `403` |
>
> </details>
>
> <details>
> <summary><strong>Paso 4.6 · Ver el resultado</strong></summary>
>
> > ```text
> > $ curl -i localhost:8080/api/v1/students                                  # sin token
> > HTTP/1.1 401
> > WWW-Authenticate: Bearer
> > {"timestamp":"2026-11-02T00:44:16.254Z","status":401,"error":"Unauthorized","message":"Se requiere autenticación para acceder a este recurso","path":"/api/v1/students"}
> >
> > $ curl -i -H 'Authorization: Bearer basura' localhost:8080/api/v1/students/1
> > HTTP/1.1 401
> > WWW-Authenticate: Bearer
> > {"timestamp":"2026-11-02T00:44:16.307Z","status":401,"error":"Unauthorized","message":"El token no es válido","path":"/api/v1/students/1"}
> >
> > $ curl -i -H "Authorization: Bearer $ANA" -X DELETE localhost:8080/api/v1/students/3
> > HTTP/1.1 403
> > {"timestamp":"2026-11-02T00:44:16.325Z","status":403,"error":"Forbidden","message":"No tiene permisos para realizar esta operación","path":"/api/v1/students/3"}
> >
> > $ curl -i -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
> >     -d '{"username":"admin","password":"mala"}'
> > HTTP/1.1 401
> > {"timestamp":"2026-11-02T00:44:16.434Z","status":401,"error":"Unauthorized","message":"Usuario o contraseña incorrectos","path":"/api/v1/auth/login"}
> > ```
> >
> > Y los errores del Módulo 07 **siguen igual**: con un token válido, `GET /students/999` da `404` con `No existe un estudiante con id 999` y `GET /api/v1/nope` da `404` con `No existe el recurso solicitado`. **Sin** token, `GET /api/v1/nope` da `401`: **primero** se pregunta quién eres, **después** si la ruta existe.
> >
> > **El token vencido.** Con la propiedad `app.jwt.expiration-seconds=2`, se inicia sesión, se espera 3 segundos y se repite la petición:
> >
> > ```text
> > $ curl -i -H "Authorization: Bearer $T" localhost:8080/api/v1/auth/me      # al instante
> > HTTP/1.1 200
> > {"username":"admin","role":"ADMIN"}
> >
> > $ curl -i -H "Authorization: Bearer $T" localhost:8080/api/v1/auth/me      # 3 segundos después
> > HTTP/1.1 401
> > WWW-Authenticate: Bearer
> > {"timestamp":"2026-11-02T00:45:53.054Z","status":401,"error":"Unauthorized","message":"El token expiró","path":"/api/v1/auth/me"}
> > ```
> >
> > > ✅ **Mismo formato, mismo lugar.** Los seis escenarios de seguridad (RN-01 a RN-06) usan el `ErrorResponse` del Módulo 07: el cliente escribe **un solo** código para leer cualquier error de la API.
>
> </details>

</details>

---

<details>
<summary><strong>📚 Paso 5 — Swagger: que <em>Authorize</em> funcione</strong></summary>

> > 🗣️ **El vínculo con los Módulos 06 y 07.** Desde el Módulo 06 el contrato tenía el esquema `bearerAuth` y el botón **Authorize**, pero era **decorativo** (nadie validaba nada). Ahora el token **importa**, y hay que documentar tres cosas: que **toda** la API pide token, que el **login no**, y que existen los errores `401` y `403`.
>
> <details>
> <summary><strong>Paso 5.1 · Seguridad global y rutas públicas</strong></summary>
>
> > <details>
> > <summary><strong>📄 OpenApiConfig.java</strong> <em>(solo el cambio)</em></summary>
> >
> > ```java
> >         return new OpenAPI()
> >                 .info(...)
> >                 .servers(...)
> >                 .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))   // toda la API pide token
> >                 .components(components());
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 AuthController.java e InternalController.java</strong> <em>(solo el cambio)</em></summary>
> >
> > Las rutas **públicas** se marcan con `@SecurityRequirements` **vacío**, que quita el requisito global:
> >
> > ```java
> >     @Operation(summary = "Iniciar sesión", description = "... Cuentas de práctica: `admin` / `admin123` (ADMIN) y `ana` / `ana123` (USER).")
> >     @SecurityRequirements
> >     @ApiResponse(responseCode = "200", description = "Sesión iniciada: devuelve el token")
> >     @ApiResponse(responseCode = "400", ref = "BadRequest")
> >     @ApiResponse(responseCode = "401", ref = "Unauthorized")
> >     @PostMapping("/login")
> >     public LoginResponse login(...)
> > ```
> >
> > ```java
> >     @Operation(summary = "Estado del servicio", description = "Público: no pide token.")
> >     @SecurityRequirements
> >     @GetMapping("/health")
> >     public Map<String, String> health()
> > ```
> >
> > En `StudentController`, el `DELETE` ya **no** necesita su `@SecurityRequirement` propio (ahora es global) y solo se aclara en la descripción: `Solo ADMIN.`
> >
> > </details>
>
> </details>
>
> <details>
> <summary><strong>Paso 5.2 · Las respuestas <code>401</code> y <code>403</code>, sin repetirlas operación por operación</strong></summary>
>
> > Se reutiliza la técnica del Módulo 07 (`components.responses`) y un **personalizador** que agrega el `401` a **toda** operación protegida y el `403` a las del rol `ADMIN`:
> >
> > <details>
> > <summary><strong>📄 OpenApiConfig.java</strong> <em>(solo el cambio)</em></summary>
> >
> > ```java
> >                 .addResponses("Unauthorized", errorResponse("No autenticado: falta el token, no es válido o expiró",
> >                         example(401, "Unauthorized", "Se requiere autenticación para acceder a este recurso", "/api/v1/students", null)))
> >                 .addResponses("Forbidden", errorResponse("Autenticado, pero su rol no permite la operación",
> >                         example(403, "Forbidden", "No tiene permisos para realizar esta operación", "/api/v1/students/1", null)));
> >
> >     /**
> >      * Agrega 401 a toda operación protegida y 403 a las que exigen el rol ADMIN (ver SecurityConfig).
> >      * Se agrega a cada grupo: con GroupedOpenApi, los personalizadores globales no se aplican.
> >      */
> >     private OpenApiCustomizer securityResponses() {
> >         return openApi -> openApi.getPaths().forEach((path, item) ->
> >                 item.readOperationsMap().forEach((method, operation) -> {
> >                     boolean isPublic = operation.getSecurity() != null && operation.getSecurity().isEmpty();
> >                     if (isPublic) {
> >                         return;
> >                     }
> >                     operation.getResponses().addApiResponse("401",
> >                             new ApiResponse().$ref("#/components/responses/Unauthorized"));
> >                     if (method == PathItem.HttpMethod.DELETE || path.startsWith("/internal/")) {
> >                         operation.getResponses().addApiResponse("403",
> >                                 new ApiResponse().$ref("#/components/responses/Forbidden"));
> >                     }
> >                 }));
> >     }
> > ```
> >
> > Y cada `GroupedOpenApi` lo recibe con `.addOpenApiCustomizer(securityResponses())`. Además se agrega un grupo `auth` para `/api/v1/auth/**`.
> >
> > </details>
> >
> > > ⚠️ **Tropiezo real.** La primera versión definía el personalizador como un *bean* global, y **no hizo nada**: en el contrato de cada grupo no aparecían `401` ni `403`. Con `GroupedOpenApi`, los personalizadores globales **no se aplican** a los grupos; hay que agregarlos a cada uno.
> >
> > > 🔎 **Limitación.** El personalizador repite en código la regla de `SecurityConfig` («`DELETE` e `/internal/**` son de `ADMIN`»). Si esa regla cambia, hay que actualizar los dos lugares (ver retos).
>
> </details>
>
> <details>
> <summary><strong>Paso 5.3 · Ver el resultado</strong></summary>
>
> > Contrato de cada grupo (`/api-docs/students`, `/api-docs/auth`, `/api-docs/internal`):
> >
> > ```text
> > seguridad global:  bearerAuth
> >
> > GET     /api/v1/students/{id}            200 · 400 · 401 · 404
> > PUT     /api/v1/students/{id}            200 · 400 · 401 · 404 · 409
> > DELETE  /api/v1/students/{id}            204 · 401 · 403 · 404
> > GET     /api/v1/students                 200 · 400 · 401
> > POST    /api/v1/students                 201 · 400 · 401 · 409
> > PATCH   /api/v1/students/{id}/status     200 · 400 · 401 · 404
> > GET     /api/v1/students/by-email        200 · 400 · 401 · 404
> > POST    /api/v1/auth/login               200 · 400 · 401            (pública: security = [])
> > GET     /api/v1/auth/me                  200 · 401
> > POST    /internal/seed                   200 · 401 · 403
> > GET     /internal/health                 200                        (pública: security = [])
> > ```
> >
> > **Probar en Swagger UI** (`http://localhost:8080/swagger-ui.html`, que sigue siendo pública):
> >
> > 1. En el grupo **Auth**, abrir `POST /api/v1/auth/login`, **Try it out** con `admin` / `admin123` y copiar el valor de `token`.
> > 2. Pulsar **Authorize**, pegar el token (sin escribir `Bearer`) y **Authorize**.
> > 3. Probar `GET /api/v1/auth/me` en **Auth**: ahora responde con el usuario.
> > 4. **Logout** en el diálogo y repetir: responde `401` con el cuerpo `ErrorResponse`.
> >
> > > ✅ El botón **Authorize**, decorativo en el Módulo 06, ahora **cambia el resultado**: sin token, `401`; con el token de `ana`, `DELETE` responde `403`; con el de `admin`, `204`. La propiedad `springdoc.swagger-ui.persist-authorization=true` conserva el token al recargar la página.
>
> </details>

</details>

---

<details>
<summary><strong>🧪 Paso 6 — Prueba de punta a punta</strong></summary>

> <details>
> <summary><strong>Paso 6.1 · Matriz completa</strong></summary>
>
> > Con la aplicación recién arrancada, los tokens de `admin` y `ana`:
> >
> > | # | Quién | Petición | Esperado |
> > |:-:|---|---|---|
> > | 1 | — | `POST /auth/login` (`admin`/`admin123`) | `200` · token |
> > | 2 | — | `POST /auth/login` con contraseña mala | `401` · `Usuario o contraseña incorrectos` |
> > | 3 | — | `POST /auth/login` con campos vacíos | `400` · `details` |
> > | 4 | — | `GET /internal/health` | `200` |
> > | 5 | — | `GET /students` | `401` · `Se requiere autenticación…` |
> > | 6 | — | `GET /students/1` con `Bearer basura` | `401` · `El token no es válido` |
> > | 7 | token vencido | `GET /auth/me` | `401` · `El token expiró` |
> > | 8 | `ana` | `GET /auth/me` | `200` · `{"username":"ana","role":"USER"}` |
> > | 9 | `ana` | `GET /students/1` | `200` o `404` (según haya datos) |
> > | 10 | `ana` | `POST /internal/seed` | `403` · `No tiene permisos…` |
> > | 11 | `ana` | `DELETE /students/3` | `403` |
> > | 12 | `admin` | `POST /internal/seed` | `200` · `{"created":3}` |
> > | 13 | `admin` | `GET /students?size=2` | `200` · dos estudiantes, `totalElements: 3` |
> > | 14 | `admin` | `POST /students` válido | `201` · `Location: …/students/4` |
> > | 15 | `admin` | `DELETE /students/4` | `204` |
> > | 16 | `admin` | `GET /students/999` | `404` · `No existe un estudiante con id 999` |
> > | 17 | `admin` | `POST /students` con correo repetido | `409` |
> > | 18 | `admin` | `GET /api/v1/nope` | `404` · `No existe el recurso solicitado` |
> >
> > ```text
> > $ curl -i -H "Authorization: Bearer $ADMIN" 'localhost:8080/api/v1/students?size=2'
> > HTTP/1.1 200
> > {"content":[{"id":1,"name":"Ana Torres","age":22,"email":"ana@mail.com","status":"ACTIVE"},{"id":2,"name":"Luis Pérez","age":25,"email":"luis@mail.com","status":"ACTIVE"}],"page":0,"size":2,"totalElements":3,"totalPages":2}
> >
> > $ curl -i -H "Authorization: Bearer $ADMIN" -X DELETE localhost:8080/api/v1/students/4
> > HTTP/1.1 204
> > ```
> >
> > > 🔎 **Lo que no se tocó.** El CRUD, las validaciones, el paginado y los errores del Módulo 07 funcionan **exactamente igual**: la seguridad rodea al negocio, no lo modifica (RNF-05).
>
> </details>

</details>

---

<details>
<summary><strong>🗺️ Mapa: pieza → qué hace → dónde vive</strong></summary>

> | Pieza | Qué hace | Dónde | Paso |
> |---|---|---|:-:|
> | `spring-boot-starter-security` | Protege **todo** por defecto (Basic, sesión, CSRF) | `pom.xml` | 1 |
> | `UserAccountEntity` | La cuenta y su hash | `persistence.entity` | 2 |
> | `UserDetailsService` | Busca la cuenta por nombre | `security.JpaUserDetailsService` | 2 |
> | `PasswordEncoder` (BCrypt) | Cifra y compara contraseñas | `SecurityConfig` | 2 |
> | `SecurityFilterChain` | Reglas de acceso, *stateless*, filtros | `SecurityConfig` | 2 y 3 |
> | `AuthenticationManager` | Valida usuario y contraseña | `SecurityConfig` → `AuthService` | 3 |
> | `JwtService` | Emite y verifica tokens | `security` | 3 |
> | `JwtAuthenticationFilter` | Convierte el token de cada petición en un usuario | `security` | 3 |
> | `AuthenticationEntryPoint` | `401` con `ErrorResponse` | `security.RestAuthenticationEntryPoint` | 4 |
> | `AccessDeniedHandler` | `403` con `ErrorResponse` | `security.RestAccessDeniedHandler` | 4 |
> | Manejadores de `AuthenticationException` y `AccessDeniedException` | Errores que nacen **dentro** de MVC | `GlobalExceptionHandler` | 4 |
> | `SecurityRequirement`, `@SecurityRequirements` | Qué operaciones piden token en Swagger | `OpenApiConfig` y controladores | 5 |
>
> > 🔎 **Lectura clave.** Hay **dos mundos de errores**: los de **antes** de MVC (filtros de seguridad) y los de **dentro** (controladores y servicios). El formato uniforme del Módulo 07 solo se logra si **los dos** escriben el mismo `ErrorResponse`.

</details>

<details>
<summary><strong>🏁 Cierre — preguntas de síntesis</strong></summary>

> 1. ¿Cuál es la diferencia entre autenticación y autorización, y qué código HTTP corresponde a cada una? *(¿Quién eres? → `401`. ¿Puedes hacerlo? → `403`.)*
> 2. ¿Por qué con una sola dependencia **todo** quedó protegido, incluso Swagger? *(Por defecto, Spring Security protege cualquier ruta; lo público hay que declararlo.)*
> 3. ¿Por qué un `POST` con la contraseña correcta devolvió `401` en el Paso 1? *(El CSRF falló; el error se reenvió a `/error`, que también estaba protegida.)*
> 4. ¿Qué significa que la API sea *stateless*? ¿Qué se gana y qué se pierde? *(Sin sesión en el servidor: escala fácil; no se puede revocar un token antes de que venza.)*
> 5. ¿Un JWT está cifrado? *(No: está **firmado**. Cualquiera puede leer su contenido; nadie puede modificarlo sin que se note.)*
> 6. ¿Por qué un token manipulado se rechaza sin consultar la base? *(La firma ya no coincide con el contenido.)*
> 7. ¿Por qué el `@RestControllerAdvice` no ve el `401` de un token inválido? *(Los filtros de seguridad corren antes de Spring MVC.)*
> 8. ¿Por qué `JwtAuthenticationFilter` no lleva `@Component`? *(Se registraría dos veces; se crea con `new` dentro de la cadena.)*
> 9. ¿Por qué el login responde igual si falla el usuario o la contraseña? *(Para no ayudar a quien prueba cuentas ajenas.)*
> 10. ¿Qué parte de `StudentService` cambió con la seguridad? *(Ninguna.)*

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Solución |
> |---|---|---|
> | Todo responde `401` tras agregar la dependencia | Es el comportamiento por defecto | Declarar la `SecurityFilterChain` con las rutas públicas |
> | Un `POST`/`PUT`/`DELETE` con credenciales correctas responde `401` o `403` | CSRF activo | `csrf.disable()` en una API *stateless* |
> | Sin token responde `403` en vez de `401` | No hay `AuthenticationEntryPoint` ni mecanismo de autenticación configurado | Configurar `authenticationEntryPoint` |
> | Una contraseña incorrecta responde `500` | El `catch-all` se traga `AuthenticationException` | Un `@ExceptionHandler(AuthenticationException.class)` → `401` |
> | El `@RestControllerAdvice` no se ejecuta para un `401` o `403` | Esos errores ocurren **antes** de MVC | `AuthenticationEntryPoint` y `AccessDeniedHandler` |
> | Un usuario autenticado recibe siempre `403` y su rol aparece como `ROLE_null` | El token no tiene la reclamación `role` | Emitir siempre el rol en `generate` |
> | `WeakKeyException` al arrancar | La clave tiene menos de 32 caracteres | Usar una clave de 32 o más |
> | El filtro se ejecuta dos veces por petición | Se anotó con `@Component` y también se agregó a la cadena | Quitar `@Component` y usar `new` |
> | Siempre `403` con un `ADMIN` | La autoridad es `ROLE_ADMIN` pero se escribió `hasRole("ROLE_ADMIN")` | `hasRole("ADMIN")` (Spring agrega el prefijo) |
> | `Encoded password does not look like BCrypt` | Se guardó la contraseña sin cifrar | `encoder.encode(...)` antes de guardar |
> | Los `401`/`403` no aparecen en Swagger | El personalizador no está en el grupo | `.addOpenApiCustomizer(...)` en cada `GroupedOpenApi` |
> | Swagger UI pide token para abrirse | Falta el `permitAll` de `/swagger-ui/**` y `/api-docs/**` | Agregar las rutas públicas |
> | `Communications link failure` al arrancar | MySQL no está arrancado | `docker ps` y revisar la URL |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **Registro.** Agregar `POST /api/v1/auth/register` (público) que cree una cuenta `USER` con la contraseña cifrada. ¿Qué `409` debería lanzar si el usuario ya existe? Manejarlo como en el Módulo 07.
> 2. **Contraseña mínima.** Validar con `@Size(min = 8)` en el registro y ver el `400` con su detalle.
> 3. **Un token por tiempo corto.** Bajar la vigencia a 60 s y comprobar qué pasa en Swagger UI al vencer.
> 4. **Rol en la base, no en el token.** Hoy el rol sale del token. ¿Qué pasa si se cambia el rol de `ana` en la base y ella conserva su token viejo? Proponer cómo evitarlo.
> 5. **Revocar.** Un usuario fue despedido pero su token vale 1 hora. Listar al menos dos formas de manejar el problema y su costo.
> 6. **`403` derivado de las reglas.** Hacer que el personalizador de Swagger **no repita** la regla de `ADMIN`: por ejemplo, con una anotación propia o una lista compartida entre `SecurityConfig` y `OpenApiConfig`.
> 7. **Seguridad a nivel de método.** Reemplazar `hasRole` de la cadena por `@PreAuthorize("hasRole('ADMIN')")` en el controlador y comprobar el `403` (¿hace falta el manejador de `AccessDeniedException`?).
> 8. **Clave por variable de entorno.** Arrancar con `JWT_SECRET=...` distinto y comprobar que un token del arranque anterior deja de valer.
> 9. **Prueba automática.** Con `MockMvc` y `spring-security-test`, escribir una prueba por cada fila de la matriz del Paso 6.
> 10. **Un recurso nuevo.** Proteger un `CourseController` (leer: cualquier usuario; escribir: `ADMIN`). ¿Qué hay que tocar en `SecurityConfig`?

</details>

<details>
<summary><strong>📁 Estructura final esperada</strong></summary>

> ```text
> 📁 students-api-security
> ├── 📄 pom.xml
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 security
>     │   │   ├── 📄 SecurityConfig.java
>     │   │   ├── 📄 JwtProperties.java
>     │   │   ├── 📄 JwtService.java
>     │   │   ├── 📄 JwtAuthenticationFilter.java
>     │   │   ├── 📄 AuthService.java
>     │   │   ├── 📄 JpaUserDetailsService.java
>     │   │   ├── 📄 AccountSeeder.java
>     │   │   ├── 📄 SecurityErrorWriter.java
>     │   │   ├── 📄 RestAuthenticationEntryPoint.java
>     │   │   └── 📄 RestAccessDeniedHandler.java
>     │   ├── 📁 presentation
>     │   │   ├── 📁 config
>     │   │   │   └── 📄 OpenApiConfig.java
>     │   │   ├── 📁 controller
>     │   │   │   ├── 📄 AuthController.java
>     │   │   │   ├── 📄 StudentController.java
>     │   │   │   └── 📄 InternalController.java
>     │   │   ├── 📁 dto
>     │   │   │   ├── 📄 LoginRequest.java
>     │   │   │   ├── 📄 LoginResponse.java
>     │   │   │   ├── 📄 MeResponse.java
>     │   │   │   ├── 📄 ErrorResponse.java
>     │   │   │   ├── 📄 StudentRequest.java
>     │   │   │   ├── 📄 StudentStatusRequest.java
>     │   │   │   ├── 📄 StudentResponse.java
>     │   │   │   └── 📄 PageResponse.java
>     │   │   ├── 📁 exception
>     │   │   │   └── 📄 GlobalExceptionHandler.java
>     │   │   └── 📁 mapper
>     │   │       └── 📄 StudentMapper.java
>     │   ├── 📁 business
>     │   │   ├── 📁 exception
>     │   │   │   ├── 📄 StudentNotFoundException.java
>     │   │   │   └── 📄 StudentEmailAlreadyExistsException.java
>     │   │   └── 📁 service
>     │   │       └── 📄 StudentService.java
>     │   └── 📁 persistence
>     │       ├── 📁 entity
>     │       │   ├── 📄 Role.java
>     │       │   ├── 📄 StudentEntity.java
>     │       │   ├── 📄 StudentStatus.java
>     │       │   └── 📄 UserAccountEntity.java
>     │       └── 📁 repository
>     │           ├── 📄 StudentJpaRepository.java
>     │           └── 📄 UserAccountJpaRepository.java
>     └── 📁 resources
>         ├── 📄 application.properties
>         └── 📄 application-prod.properties
> ```
>
> **Código final, paso a paso**
>
> ### 📁 Configuración
>

> <details>
> <summary><strong>📄 pom.xml</strong></summary>
>
> **Ruta:** `pom.xml`
>
> ```xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>4.1.2</version>
>         <relativePath/>
>     </parent>
>     <groupId>com.academy</groupId>
>     <artifactId>students-api-security</artifactId>
>     <version>0.0.1-SNAPSHOT</version>
>
>     <properties>
>         <java.version>21</java.version>
>     </properties>
>
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-webmvc</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-data-jpa</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-validation</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-security</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springdoc</groupId>
>             <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
>             <version>3.2.0</version>
>         </dependency>
>         <dependency>
>             <groupId>io.jsonwebtoken</groupId>
>             <artifactId>jjwt-api</artifactId>
>             <version>0.12.6</version>
>         </dependency>
>         <dependency>
>             <groupId>io.jsonwebtoken</groupId>
>             <artifactId>jjwt-impl</artifactId>
>             <version>0.12.6</version>
>             <scope>runtime</scope>
>         </dependency>
>         <dependency>
>             <groupId>io.jsonwebtoken</groupId>
>             <artifactId>jjwt-jackson</artifactId>
>             <version>0.12.6</version>
>             <scope>runtime</scope>
>         </dependency>
>         <dependency>
>             <groupId>com.mysql</groupId>
>             <artifactId>mysql-connector-j</artifactId>
>             <scope>runtime</scope>
>         </dependency>
>     </dependencies>
>
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 application.properties</strong></summary>
>
> **Ruta:** `src/main/resources/application.properties`
>
> ```properties
> # MySQL connection
> spring.datasource.url=jdbc:mysql://localhost:3306/academy
> spring.datasource.username=root
> spring.datasource.password=root
>
> # JPA and Hibernate
> spring.jpa.hibernate.ddl-auto=update
> spring.jpa.show-sql=true
>
> # Pagination limits (protect the API)
> spring.data.web.pageable.default-page-size=10
> spring.data.web.pageable.max-page-size=50
>
> # JWT -- SOLO PARA FINES EDUCATIVOS: en un proyecto real la clave se entrega por
> # variable de entorno (JWT_SECRET) o un gestor de secretos, nunca escrita en el repositorio.
> app.jwt.secret=${JWT_SECRET:academy-clave-solo-educativa-cambiar-en-produccion-0123456789}
> app.jwt.expiration-seconds=3600
>
> # Swagger (springdoc)
> springdoc.api-docs.path=/api-docs
> springdoc.swagger-ui.path=/swagger-ui.html
> springdoc.swagger-ui.urls-primary-name=Students
> springdoc.swagger-ui.deep-linking=true
> springdoc.swagger-ui.operations-sorter=method
> springdoc.swagger-ui.tags-sorter=alpha
> springdoc.swagger-ui.display-request-duration=true
> springdoc.swagger-ui.doc-expansion=list
> springdoc.swagger-ui.try-it-out-enabled=true
> springdoc.swagger-ui.persist-authorization=true
> springdoc.default-produces-media-type=application/json
> springdoc.default-consumes-media-type=application/json
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 application-prod.properties</strong></summary>
>
> **Ruta:** `src/main/resources/application-prod.properties`
>
> ```properties
> springdoc.api-docs.enabled=false
> springdoc.swagger-ui.enabled=false
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 AcademyApplication.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/AcademyApplication.java`
>
> ```java
> package com.academy;
>
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
>
> @SpringBootApplication
> public class AcademyApplication {
>
>     public static void main(String[] args) {
>         SpringApplication.run(AcademyApplication.class, args);
>     }
> }
> ```
>
> </details>
>
> ---
>
> ### 📁 persistence
>
> #### 📁 entity
>
> <details>
> <summary><strong>📄 Role.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/entity/Role.java`
>
> ```java
> package com.academy.persistence.entity;
>
> public enum Role {
>     ADMIN,
>     USER
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentStatus.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/entity/StudentStatus.java`
>
> ```java
> package com.academy.persistence.entity;
>
> public enum StudentStatus {
>     ACTIVE,
>     INACTIVE
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentEntity.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/entity/StudentEntity.java`
>
> ```java
> package com.academy.persistence.entity;
>
> import jakarta.persistence.Column;
> import jakarta.persistence.Entity;
> import jakarta.persistence.EnumType;
> import jakarta.persistence.Enumerated;
> import jakarta.persistence.GeneratedValue;
> import jakarta.persistence.GenerationType;
> import jakarta.persistence.Id;
> import jakarta.persistence.Table;
> import org.hibernate.annotations.CreationTimestamp;
>
> import java.time.LocalDateTime;
>
> @Entity
> @Table(name = "student")
> public class StudentEntity {
>
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>
>     @Column(nullable = false)
>     private String name;
>
>     private int age;
>
>     @Column(nullable = false, unique = true)
>     private String email;
>
>     @Enumerated(EnumType.STRING)
>     @Column(nullable = false)
>     private StudentStatus status;
>
>     @CreationTimestamp
>     @Column(name = "created_at", updatable = false)
>     private LocalDateTime createdAt;
>
>     protected StudentEntity() {
>     }
>
>     public StudentEntity(Long id, String name, int age, String email, StudentStatus status) {
>         this.id = id;
>         this.name = name;
>         this.age = age;
>         this.email = email;
>         this.status = status;
>     }
>
>     public Long getId() { return id; }
>     public String getName() { return name; }
>     public int getAge() { return age; }
>     public String getEmail() { return email; }
>     public StudentStatus getStatus() { return status; }
>     public LocalDateTime getCreatedAt() { return createdAt; }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 UserAccountEntity.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/entity/UserAccountEntity.java`
>
> ```java
> package com.academy.persistence.entity;
>
> import jakarta.persistence.Column;
> import jakarta.persistence.Entity;
> import jakarta.persistence.EnumType;
> import jakarta.persistence.Enumerated;
> import jakarta.persistence.GeneratedValue;
> import jakarta.persistence.GenerationType;
> import jakarta.persistence.Id;
> import jakarta.persistence.Table;
>
> @Entity
> @Table(name = "user_account")
> public class UserAccountEntity {
>
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>
>     @Column(nullable = false, unique = true)
>     private String username;
>
>     // Siempre el hash BCrypt, nunca la contraseña en texto plano
>     @Column(name = "password_hash", nullable = false)
>     private String passwordHash;
>
>     @Enumerated(EnumType.STRING)
>     @Column(nullable = false)
>     private Role role;
>
>     protected UserAccountEntity() {
>     }
>
>     public UserAccountEntity(Long id, String username, String passwordHash, Role role) {
>         this.id = id;
>         this.username = username;
>         this.passwordHash = passwordHash;
>         this.role = role;
>     }
>
>     public Long getId() { return id; }
>     public String getUsername() { return username; }
>     public String getPasswordHash() { return passwordHash; }
>     public Role getRole() { return role; }
> }
> ```
>
> </details>
>
> #### 📁 repository
>
> <details>
> <summary><strong>📄 StudentJpaRepository.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/repository/StudentJpaRepository.java`
>
> ```java
> package com.academy.persistence.repository;
>
> import com.academy.persistence.entity.StudentEntity;
> import org.springframework.data.domain.Page;
> import org.springframework.data.domain.Pageable;
> import org.springframework.data.jpa.repository.JpaRepository;
>
> import java.util.Optional;
>
> public interface StudentJpaRepository extends JpaRepository<StudentEntity, Long> {
>
>     Page<StudentEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
>
>     Optional<StudentEntity> findByEmailIgnoreCase(String email);
>
>     boolean existsByEmailIgnoreCase(String email);
>
>     boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 UserAccountJpaRepository.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/repository/UserAccountJpaRepository.java`
>
> ```java
> package com.academy.persistence.repository;
>
> import com.academy.persistence.entity.UserAccountEntity;
> import org.springframework.data.jpa.repository.JpaRepository;
>
> import java.util.Optional;
>
> public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, Long> {
>
>     Optional<UserAccountEntity> findByUsername(String username);
> }
> ```
>
> </details>
>
> ---
>
> ### 📁 business
>
> #### 📁 exception
>
> <details>
> <summary><strong>📄 StudentNotFoundException.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/business/exception/StudentNotFoundException.java`
>
> ```java
> package com.academy.business.exception;
>
> public class StudentNotFoundException extends RuntimeException {
>
>     public StudentNotFoundException(String message) {
>         super(message);
>     }
>
>     public static StudentNotFoundException byId(Long id) {
>         return new StudentNotFoundException("No existe un estudiante con id " + id);
>     }
>
>     public static StudentNotFoundException byEmail(String email) {
>         return new StudentNotFoundException("No existe un estudiante con el correo " + email);
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentEmailAlreadyExistsException.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/business/exception/StudentEmailAlreadyExistsException.java`
>
> ```java
> package com.academy.business.exception;
>
> public class StudentEmailAlreadyExistsException extends RuntimeException {
>
>     public StudentEmailAlreadyExistsException(String email) {
>         super("El correo " + email + " ya está registrado");
>     }
> }
> ```
>
> </details>
>
> #### 📁 service
>
> <details>
> <summary><strong>📄 StudentService.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/business/service/StudentService.java`
>
> ```java
> package com.academy.business.service;
>
> import com.academy.business.exception.StudentEmailAlreadyExistsException;
> import com.academy.business.exception.StudentNotFoundException;
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.persistence.entity.StudentStatus;
> import com.academy.persistence.repository.StudentJpaRepository;
> import org.springframework.data.domain.Page;
> import org.springframework.data.domain.Pageable;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
>
> @Service
> public class StudentService {
>
>     private final StudentJpaRepository students;
>
>     public StudentService(StudentJpaRepository students) {
>         this.students = students;
>     }
>
>     @Transactional(readOnly = true)
>     public Page<StudentEntity> search(String name, Pageable pageable) {
>         if (name == null || name.isBlank()) {
>             return students.findAll(pageable);
>         }
>         return students.findByNameContainingIgnoreCase(name, pageable);
>     }
>
>     @Transactional(readOnly = true)
>     public StudentEntity findById(Long id) {
>         return students.findById(id)
>                 .orElseThrow(() -> StudentNotFoundException.byId(id));
>     }
>
>     @Transactional(readOnly = true)
>     public StudentEntity findByEmail(String email) {
>         return students.findByEmailIgnoreCase(email)
>                 .orElseThrow(() -> StudentNotFoundException.byEmail(email));
>     }
>
>     @Transactional
>     public StudentEntity create(StudentEntity data) {
>         if (students.existsByEmailIgnoreCase(data.getEmail())) {
>             throw new StudentEmailAlreadyExistsException(data.getEmail());
>         }
>         return students.save(new StudentEntity(
>                 null, data.getName(), data.getAge(), data.getEmail(), data.getStatus()));
>     }
>
>     @Transactional
>     public StudentEntity update(Long id, StudentEntity data) {
>         if (!students.existsById(id)) {
>             throw StudentNotFoundException.byId(id);
>         }
>         if (students.existsByEmailIgnoreCaseAndIdNot(data.getEmail(), id)) {
>             throw new StudentEmailAlreadyExistsException(data.getEmail());
>         }
>         return students.save(new StudentEntity(
>                 id, data.getName(), data.getAge(), data.getEmail(), data.getStatus()));
>     }
>
>     @Transactional
>     public StudentEntity changeStatus(Long id, StudentStatus status) {
>         StudentEntity student = findById(id);
>         return students.save(new StudentEntity(
>                 id, student.getName(), student.getAge(), student.getEmail(), status));
>     }
>
>     @Transactional
>     public void delete(Long id) {
>         if (!students.existsById(id)) {
>             throw StudentNotFoundException.byId(id);
>         }
>         students.deleteById(id);
>     }
> }
> ```
>
> </details>
>
> ---
>
> ### 📁 security
>
> <details>
> <summary><strong>📄 JwtProperties.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/JwtProperties.java`
>
> ```java
> package com.academy.security;
>
> import org.springframework.boot.context.properties.ConfigurationProperties;
>
> @ConfigurationProperties(prefix = "app.jwt")
> public record JwtProperties(String secret, long expirationSeconds) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 JwtService.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/JwtService.java`
>
> ```java
> package com.academy.security;
>
> import io.jsonwebtoken.Claims;
> import io.jsonwebtoken.Jwts;
> import io.jsonwebtoken.security.Keys;
> import org.springframework.stereotype.Service;
>
> import javax.crypto.SecretKey;
> import java.nio.charset.StandardCharsets;
> import java.time.Instant;
> import java.util.Date;
>
> @Service
> public class JwtService {
>
>     private final SecretKey key;
>     private final long expirationSeconds;
>
>     public JwtService(JwtProperties properties) {
>         this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
>         this.expirationSeconds = properties.expirationSeconds();
>     }
>
>     public String generate(String username, String role) {
>         Instant now = Instant.now();
>         return Jwts.builder()
>                 .subject(username)
>                 .claim("role", role)
>                 .issuedAt(Date.from(now))
>                 .expiration(Date.from(now.plusSeconds(expirationSeconds)))
>                 .signWith(key, Jwts.SIG.HS256)
>                 .compact();
>     }
>
>     /** Verifica la firma y la expiración; lanza JwtException si el token no sirve. */
>     public Claims parse(String token) {
>         return Jwts.parser()
>                 .verifyWith(key)
>                 .build()
>                 .parseSignedClaims(token)
>                 .getPayload();
>     }
>
>     public long expirationSeconds() {
>         return expirationSeconds;
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 JwtAuthenticationFilter.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/JwtAuthenticationFilter.java`
>
> ```java
> package com.academy.security;
>
> import io.jsonwebtoken.Claims;
> import io.jsonwebtoken.ExpiredJwtException;
> import io.jsonwebtoken.JwtException;
> import jakarta.servlet.FilterChain;
> import jakarta.servlet.ServletException;
> import jakarta.servlet.http.HttpServletRequest;
> import jakarta.servlet.http.HttpServletResponse;
> import org.springframework.http.HttpHeaders;
> import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
> import org.springframework.security.core.authority.SimpleGrantedAuthority;
> import org.springframework.security.core.context.SecurityContextHolder;
> import org.springframework.web.filter.OncePerRequestFilter;
>
> import java.io.IOException;
> import java.util.List;
>
> public class JwtAuthenticationFilter extends OncePerRequestFilter {
>
>     /** Atributo de la petición donde el filtro deja el motivo si el token no sirve. */
>     public static final String AUTH_ERROR_ATTRIBUTE = "auth.error";
>
>     private static final String BEARER_PREFIX = "Bearer ";
>
>     private final JwtService jwtService;
>
>     public JwtAuthenticationFilter(JwtService jwtService) {
>         this.jwtService = jwtService;
>     }
>
>     @Override
>     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
>                                     FilterChain chain) throws ServletException, IOException {
>         String header = request.getHeader(HttpHeaders.AUTHORIZATION);
>         if (header != null && header.startsWith(BEARER_PREFIX)) {
>             try {
>                 Claims claims = jwtService.parse(header.substring(BEARER_PREFIX.length()));
>                 var authentication = new UsernamePasswordAuthenticationToken(
>                         claims.getSubject(), null,
>                         List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class))));
>                 SecurityContextHolder.getContext().setAuthentication(authentication);
>             } catch (ExpiredJwtException ex) {
>                 request.setAttribute(AUTH_ERROR_ATTRIBUTE, "El token expiró");
>             } catch (JwtException | IllegalArgumentException ex) {
>                 request.setAttribute(AUTH_ERROR_ATTRIBUTE, "El token no es válido");
>             }
>         }
>         chain.doFilter(request, response);
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 AuthService.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/AuthService.java`
>
> ```java
> package com.academy.security;
>
> import org.springframework.security.authentication.AuthenticationManager;
> import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
> import org.springframework.security.core.Authentication;
> import org.springframework.stereotype.Service;
>
> @Service
> public class AuthService {
>
>     private final AuthenticationManager authenticationManager;
>     private final JwtService jwtService;
>
>     public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
>         this.authenticationManager = authenticationManager;
>         this.jwtService = jwtService;
>     }
>
>     /** Valida usuario y contraseña (lanza AuthenticationException si no coinciden) y emite el JWT. */
>     public String login(String username, String password) {
>         Authentication authentication = authenticationManager.authenticate(
>                 new UsernamePasswordAuthenticationToken(username, password));
>         String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
>         return jwtService.generate(authentication.getName(), role);
>     }
>
>     public long expirationSeconds() {
>         return jwtService.expirationSeconds();
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 JpaUserDetailsService.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/JpaUserDetailsService.java`
>
> ```java
> package com.academy.security;
>
> import com.academy.persistence.entity.UserAccountEntity;
> import com.academy.persistence.repository.UserAccountJpaRepository;
> import org.springframework.security.core.userdetails.User;
> import org.springframework.security.core.userdetails.UserDetails;
> import org.springframework.security.core.userdetails.UserDetailsService;
> import org.springframework.security.core.userdetails.UsernameNotFoundException;
> import org.springframework.stereotype.Service;
>
> @Service
> public class JpaUserDetailsService implements UserDetailsService {
>
>     private final UserAccountJpaRepository accounts;
>
>     public JpaUserDetailsService(UserAccountJpaRepository accounts) {
>         this.accounts = accounts;
>     }
>
>     @Override
>     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
>         UserAccountEntity account = accounts.findByUsername(username)
>                 .orElseThrow(() -> new UsernameNotFoundException("No existe la cuenta " + username));
>         return User.withUsername(account.getUsername())
>                 .password(account.getPasswordHash())
>                 .roles(account.getRole().name())
>                 .build();
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 AccountSeeder.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/AccountSeeder.java`
>
> ```java
> package com.academy.security;
>
> import com.academy.persistence.entity.Role;
> import com.academy.persistence.entity.UserAccountEntity;
> import com.academy.persistence.repository.UserAccountJpaRepository;
> import org.springframework.boot.CommandLineRunner;
> import org.springframework.security.crypto.password.PasswordEncoder;
> import org.springframework.stereotype.Component;
>
> /**
>  * Crea dos cuentas de práctica al arrancar. SOLO PARA FINES EDUCATIVOS:
>  * en un proyecto real las cuentas no se siembran con contraseñas en el código.
>  */
> @Component
> public class AccountSeeder implements CommandLineRunner {
>
>     private final UserAccountJpaRepository accounts;
>     private final PasswordEncoder encoder;
>
>     public AccountSeeder(UserAccountJpaRepository accounts, PasswordEncoder encoder) {
>         this.accounts = accounts;
>         this.encoder = encoder;
>     }
>
>     @Override
>     public void run(String... args) {
>         createIfMissing("admin", "admin123", Role.ADMIN);
>         createIfMissing("ana", "ana123", Role.USER);
>     }
>
>     private void createIfMissing(String username, String rawPassword, Role role) {
>         if (accounts.findByUsername(username).isEmpty()) {
>             accounts.save(new UserAccountEntity(null, username, encoder.encode(rawPassword), role));
>         }
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 SecurityErrorWriter.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/SecurityErrorWriter.java`
>
> ```java
> package com.academy.security;
>
> import com.academy.presentation.dto.ErrorResponse;
> import tools.jackson.databind.ObjectMapper;
> import jakarta.servlet.http.HttpServletRequest;
> import jakarta.servlet.http.HttpServletResponse;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.MediaType;
> import org.springframework.stereotype.Component;
>
> import java.io.IOException;
>
> /**
>  * Escribe un ErrorResponse a mano. Hace falta porque los filtros de seguridad corren ANTES de Spring MVC:
>  * el @RestControllerAdvice nunca llega a ver estos errores.
>  */
> @Component
> public class SecurityErrorWriter {
>
>     private final ObjectMapper objectMapper;
>
>     public SecurityErrorWriter(ObjectMapper objectMapper) {
>         this.objectMapper = objectMapper;
>     }
>
>     public void write(HttpServletRequest request, HttpServletResponse response,
>                       HttpStatus status, String message) throws IOException {
>         response.setStatus(status.value());
>         response.setContentType(MediaType.APPLICATION_JSON_VALUE);
>         response.setCharacterEncoding("UTF-8");
>         objectMapper.writeValue(response.getWriter(),
>                 ErrorResponse.of(status, message, request.getRequestURI()));
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 RestAuthenticationEntryPoint.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/RestAuthenticationEntryPoint.java`
>
> ```java
> package com.academy.security;
>
> import jakarta.servlet.http.HttpServletRequest;
> import jakarta.servlet.http.HttpServletResponse;
> import org.springframework.http.HttpHeaders;
> import org.springframework.http.HttpStatus;
> import org.springframework.security.core.AuthenticationException;
> import org.springframework.security.web.AuthenticationEntryPoint;
> import org.springframework.stereotype.Component;
>
> import java.io.IOException;
>
> /** Se ejecuta cuando alguien SIN autenticar pide un recurso protegido: responde 401. */
> @Component
> public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
>
>     private final SecurityErrorWriter errors;
>
>     public RestAuthenticationEntryPoint(SecurityErrorWriter errors) {
>         this.errors = errors;
>     }
>
>     @Override
>     public void commence(HttpServletRequest request, HttpServletResponse response,
>                          AuthenticationException authException) throws IOException {
>         Object reason = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE);
>         String message = reason != null ? reason.toString() : "Se requiere autenticación para acceder a este recurso";
>         response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
>         errors.write(request, response, HttpStatus.UNAUTHORIZED, message);
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 RestAccessDeniedHandler.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/RestAccessDeniedHandler.java`
>
> ```java
> package com.academy.security;
>
> import jakarta.servlet.http.HttpServletRequest;
> import jakarta.servlet.http.HttpServletResponse;
> import org.springframework.http.HttpStatus;
> import org.springframework.security.access.AccessDeniedException;
> import org.springframework.security.web.access.AccessDeniedHandler;
> import org.springframework.stereotype.Component;
>
> import java.io.IOException;
>
> /** Se ejecuta cuando alguien YA autenticado pide algo que su rol no permite: responde 403. */
> @Component
> public class RestAccessDeniedHandler implements AccessDeniedHandler {
>
>     private final SecurityErrorWriter errors;
>
>     public RestAccessDeniedHandler(SecurityErrorWriter errors) {
>         this.errors = errors;
>     }
>
>     @Override
>     public void handle(HttpServletRequest request, HttpServletResponse response,
>                        AccessDeniedException accessDeniedException) throws IOException {
>         errors.write(request, response, HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación");
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 SecurityConfig.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/SecurityConfig.java`
>
> ```java
> package com.academy.security;
>
> import org.springframework.boot.context.properties.EnableConfigurationProperties;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.http.HttpMethod;
> import org.springframework.security.authentication.AuthenticationManager;
> import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
> import org.springframework.security.config.http.SessionCreationPolicy;
> import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
> import org.springframework.security.crypto.password.PasswordEncoder;
> import org.springframework.security.web.SecurityFilterChain;
> import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
>
> @Configuration
> @EnableWebSecurity
> @EnableConfigurationProperties(JwtProperties.class)
> public class SecurityConfig {
>
>     @Bean
>     public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService,
>                                            RestAuthenticationEntryPoint entryPoint,
>                                            RestAccessDeniedHandler accessDeniedHandler) throws Exception {
>         http
>                 .csrf(csrf -> csrf.disable())
>                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
>                 .exceptionHandling(errors -> errors
>                         .authenticationEntryPoint(entryPoint)
>                         .accessDeniedHandler(accessDeniedHandler))
>                 .authorizeHttpRequests(auth -> auth
>                         .requestMatchers("/api/v1/auth/login", "/internal/health").permitAll()
>                         .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll()
>                         .requestMatchers(HttpMethod.DELETE, "/api/v1/students/**").hasRole("ADMIN")
>                         .requestMatchers("/internal/**").hasRole("ADMIN")
>                         .anyRequest().authenticated())
>                 .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
>         return http.build();
>     }
>
>     @Bean
>     public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
>         return configuration.getAuthenticationManager();
>     }
>
>     @Bean
>     public PasswordEncoder passwordEncoder() {
>         return new BCryptPasswordEncoder();
>     }
> }
> ```
>
> </details>
>
> ---
>
> ### 📁 presentation
>
> #### 📁 dto
>
> <details>
> <summary><strong>📄 ErrorResponse.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/dto/ErrorResponse.java`
>
> ```java
> package com.academy.presentation.dto;
>
> import com.fasterxml.jackson.annotation.JsonInclude;
> import io.swagger.v3.oas.annotations.media.Schema;
> import org.springframework.http.HttpStatus;
>
> import java.time.Instant;
> import java.time.temporal.ChronoUnit;
> import java.util.List;
>
> @Schema(description = "Cuerpo de toda respuesta de error de la API")
> public record ErrorResponse(
>
>         @Schema(description = "Momento en que ocurrió el error, en UTC (ISO-8601)", type = "string", example = "2026-11-01T10:15:30.123Z")
>         Instant timestamp,
>
>         @Schema(description = "Código de estado HTTP", example = "404")
>         int status,
>
>         @Schema(description = "Nombre del estado HTTP", example = "Not Found")
>         String error,
>
>         @Schema(description = "Qué pasó, en lenguaje claro", example = "No existe un estudiante con id 999")
>         String message,
>
>         @Schema(description = "Ruta de la petición que falló", example = "/api/v1/students/999")
>         String path,
>
>         @Schema(description = "Detalle campo por campo (solo en errores de validación)")
>         @JsonInclude(JsonInclude.Include.NON_EMPTY)
>         List<FieldIssue> details
> ) {
>
>     @Schema(description = "Un campo inválido y la razón")
>     public record FieldIssue(
>
>             @Schema(description = "Nombre del campo", example = "email")
>             String field,
>
>             @Schema(description = "Por qué es inválido", example = "debe ser una dirección de correo electrónico bien formada")
>             String message
>     ) {
>     }
>
>     public static ErrorResponse of(HttpStatus status, String message, String path) {
>         return new ErrorResponse(Instant.now().truncatedTo(ChronoUnit.MILLIS), status.value(), status.getReasonPhrase(), message, path, List.of());
>     }
>
>     public static ErrorResponse of(HttpStatus status, String message, String path, List<FieldIssue> details) {
>         return new ErrorResponse(Instant.now().truncatedTo(ChronoUnit.MILLIS), status.value(), status.getReasonPhrase(), message, path, details);
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 LoginRequest.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/dto/LoginRequest.java`
>
> ```java
> package com.academy.presentation.dto;
>
> import io.swagger.v3.oas.annotations.media.Schema;
> import jakarta.validation.constraints.NotBlank;
>
> @Schema(description = "Credenciales para iniciar sesión")
> public record LoginRequest(
>
>         @Schema(description = "Nombre de usuario", example = "admin")
>         @NotBlank(message = "es obligatorio")
>         String username,
>
>         @Schema(description = "Contraseña", example = "admin123")
>         @NotBlank(message = "es obligatoria")
>         String password
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 LoginResponse.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/dto/LoginResponse.java`
>
> ```java
> package com.academy.presentation.dto;
>
> import io.swagger.v3.oas.annotations.media.Schema;
>
> @Schema(description = "Token que el cliente debe enviar en cada petición")
> public record LoginResponse(
>
>         @Schema(description = "JWT firmado", example = "eyJhbGciOiJIUzI1NiJ9...")
>         String token,
>
>         @Schema(description = "Tipo de token: se envía como `Authorization: Bearer <token>`", example = "Bearer")
>         String tokenType,
>
>         @Schema(description = "Segundos de vigencia del token", example = "3600")
>         long expiresIn
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 MeResponse.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/dto/MeResponse.java`
>
> ```java
> package com.academy.presentation.dto;
>
> import io.swagger.v3.oas.annotations.media.Schema;
>
> @Schema(description = "Quién es el usuario autenticado")
> public record MeResponse(
>
>         @Schema(description = "Nombre de usuario", example = "ana")
>         String username,
>
>         @Schema(description = "Rol", example = "USER")
>         String role
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentRequest.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/dto/StudentRequest.java`
>
> ```java
> package com.academy.presentation.dto;
>
> import com.academy.persistence.entity.StudentStatus;
> import io.swagger.v3.oas.annotations.media.Schema;
> import jakarta.validation.constraints.Email;
> import jakarta.validation.constraints.Max;
> import jakarta.validation.constraints.Min;
> import jakarta.validation.constraints.NotBlank;
> import jakarta.validation.constraints.NotNull;
> import jakarta.validation.constraints.Size;
>
> @Schema(description = "Datos que el cliente envía para crear o reemplazar un estudiante")
> public record StudentRequest(
>
>         @Schema(description = "Nombre completo", example = "Ana Torres")
>         @NotBlank(message = "es obligatorio")
>         @Size(min = 2, max = 100, message = "debe tener entre 2 y 100 caracteres")
>         String name,
>
>         @Schema(description = "Edad en años", example = "22")
>         @Min(value = 16, message = "debe ser 16 o más")
>         @Max(value = 99, message = "debe ser 99 o menos")
>         int age,
>
>         @Schema(description = "Correo electrónico, único entre estudiantes", example = "ana@mail.com")
>         @NotBlank(message = "es obligatorio")
>         @Email(message = "no tiene formato de correo electrónico")
>         String email,
>
>         @Schema(description = "Estado académico", example = "ACTIVE")
>         @NotNull(message = "es obligatorio")
>         StudentStatus status
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentStatusRequest.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/dto/StudentStatusRequest.java`
>
> ```java
> package com.academy.presentation.dto;
>
> import com.academy.persistence.entity.StudentStatus;
> import io.swagger.v3.oas.annotations.media.Schema;
> import jakarta.validation.constraints.NotNull;
>
> @Schema(description = "Nuevo estado del estudiante (es lo único que cambia)")
> public record StudentStatusRequest(
>
>         @Schema(description = "Estado académico", example = "INACTIVE")
>         @NotNull(message = "es obligatorio")
>         StudentStatus status
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentResponse.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/dto/StudentResponse.java`
>
> ```java
> package com.academy.presentation.dto;
>
> import com.academy.persistence.entity.StudentStatus;
> import io.swagger.v3.oas.annotations.media.Schema;
>
> @Schema(description = "Estudiante tal como lo ve el cliente")
> public record StudentResponse(
>
>         @Schema(description = "Identificador generado por la base", example = "1")
>         Long id,
>
>         @Schema(description = "Nombre completo", example = "Ana Torres")
>         String name,
>
>         @Schema(description = "Edad en años", example = "22")
>         int age,
>
>         @Schema(description = "Correo electrónico", example = "ana@mail.com")
>         String email,
>
>         @Schema(description = "Estado académico", example = "ACTIVE")
>         StudentStatus status
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 PageResponse.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/dto/PageResponse.java`
>
> ```java
> package com.academy.presentation.dto;
>
> import io.swagger.v3.oas.annotations.media.Schema;
>
> import java.util.List;
>
> @Schema(description = "Una página de resultados")
> public record PageResponse<T>(
>
>         @Schema(description = "Elementos de la página")
>         List<T> content,
>
>         @Schema(description = "Número de página, desde 0", example = "0")
>         int page,
>
>         @Schema(description = "Elementos por página (máximo 50)", example = "10")
>         int size,
>
>         @Schema(description = "Total de elementos que cumplen el filtro", example = "3")
>         long totalElements,
>
>         @Schema(description = "Total de páginas", example = "1")
>         int totalPages
> ) {
> }
> ```
>
> </details>
>
> #### 📁 exception
>
> <details>
> <summary><strong>📄 GlobalExceptionHandler.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/exception/GlobalExceptionHandler.java`
>
> ```java
> package com.academy.presentation.exception;
>
> import com.academy.business.exception.StudentEmailAlreadyExistsException;
> import com.academy.business.exception.StudentNotFoundException;
> import com.academy.presentation.dto.ErrorResponse;
> import com.academy.presentation.dto.ErrorResponse.FieldIssue;
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> import org.springframework.beans.TypeMismatchException;
> import org.springframework.data.core.PropertyReferenceException;
> import org.springframework.http.HttpHeaders;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.HttpStatusCode;
> import org.springframework.http.ResponseEntity;
> import org.springframework.http.converter.HttpMessageNotReadableException;
> import org.springframework.security.access.AccessDeniedException;
> import org.springframework.security.core.AuthenticationException;
> import org.springframework.web.bind.MethodArgumentNotValidException;
> import org.springframework.web.bind.MissingServletRequestParameterException;
> import org.springframework.web.bind.annotation.ExceptionHandler;
> import org.springframework.web.bind.annotation.RestControllerAdvice;
> import org.springframework.web.context.request.ServletWebRequest;
> import org.springframework.web.context.request.WebRequest;
> import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
>
> import java.util.Comparator;
> import java.util.List;
>
> @RestControllerAdvice
> public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
>
>     private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
>
>     // ---------- Excepciones de la aplicación ----------
>
>     @ExceptionHandler(StudentNotFoundException.class)
>     public ResponseEntity<Object> handleNotFound(StudentNotFoundException ex, WebRequest request) {
>         return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
>     }
>
>     @ExceptionHandler(StudentEmailAlreadyExistsException.class)
>     public ResponseEntity<Object> handleDuplicateEmail(StudentEmailAlreadyExistsException ex, WebRequest request) {
>         return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
>     }
>
>     // El login falla dentro de un controlador (AuthenticationManager), así que SÍ pasa por este manejador.
>     @ExceptionHandler(AuthenticationException.class)
>     public ResponseEntity<Object> handleAuthentication(AuthenticationException ex, WebRequest request) {
>         return build(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", request, null);
>     }
>
>     // Sin este manejador, el catch-all convertiría un AccessDeniedException en un 500.
>     @ExceptionHandler(AccessDeniedException.class)
>     public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
>         return build(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación", request, null);
>     }
>
>     @ExceptionHandler(PropertyReferenceException.class)
>     public ResponseEntity<Object> handleUnknownSortProperty(PropertyReferenceException ex, WebRequest request) {
>         return build(HttpStatus.BAD_REQUEST, "No se puede ordenar por '" + ex.getPropertyName() + "'", request, null);
>     }
>
>     @ExceptionHandler(Exception.class)
>     public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
>         log.error("Error inesperado en {}", path(request), ex);
>         return build(HttpStatus.INTERNAL_SERVER_ERROR,
>                 "Ocurrió un error inesperado. Intentar de nuevo más tarde.", request, null);
>     }
>
>     // ---------- Excepciones de Spring MVC (las maneja la clase base; aquí se les da el cuerpo propio) ----------
>
>     @Override
>     protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
>                                                                   HttpHeaders headers, HttpStatusCode status,
>                                                                   WebRequest request) {
>         List<FieldIssue> details = ex.getBindingResult().getFieldErrors().stream()
>                 .map(error -> new FieldIssue(error.getField(), error.getDefaultMessage()))
>                 .sorted(Comparator.comparing(FieldIssue::field).thenComparing(FieldIssue::message))
>                 .toList();
>         return build(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos", request, details);
>     }
>
>     @Override
>     protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
>                                                                   HttpHeaders headers, HttpStatusCode status,
>                                                                   WebRequest request) {
>         return build(HttpStatus.BAD_REQUEST,
>                 "El cuerpo de la petición no es un JSON válido o tiene un valor no permitido", request, null);
>     }
>
>     @Override
>     protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
>                                                         HttpStatusCode status, WebRequest request) {
>         return build(HttpStatus.BAD_REQUEST,
>                 "El parámetro '" + ex.getPropertyName() + "' tiene un valor no válido: " + ex.getValue(),
>                 request, null);
>     }
>
>     @Override
>     protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
>                                                                           HttpHeaders headers, HttpStatusCode status,
>                                                                           WebRequest request) {
>         return build(HttpStatus.BAD_REQUEST,
>                 "Falta el parámetro obligatorio '" + ex.getParameterName() + "'", request, null);
>     }
>
>     // Todas las demás excepciones de Spring MVC (ruta inexistente, método no permitido...) pasan por aquí.
>     @Override
>     protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
>                                                              HttpStatusCode statusCode, WebRequest request) {
>         HttpStatus status = HttpStatus.valueOf(statusCode.value());
>         String message = switch (status) {
>             case NOT_FOUND -> "No existe el recurso solicitado";
>             case METHOD_NOT_ALLOWED -> "Ese método HTTP no está permitido en esta ruta";
>             case UNSUPPORTED_MEDIA_TYPE -> "El tipo de contenido enviado no está soportado";
>             case NOT_ACCEPTABLE -> "No se puede responder en el formato pedido";
>             default -> status.getReasonPhrase();
>         };
>         ResponseEntity<Object> response = build(status, message, request, null);
>         return ResponseEntity.status(status).headers(headers).body(response.getBody());
>     }
>
>     // ---------- Apoyo ----------
>
>     private ResponseEntity<Object> build(HttpStatus status, String message, WebRequest request,
>                                          List<FieldIssue> details) {
>         ErrorResponse body = details == null
>                 ? ErrorResponse.of(status, message, path(request))
>                 : ErrorResponse.of(status, message, path(request), details);
>         return ResponseEntity.status(status).body(body);
>     }
>
>     private String path(WebRequest request) {
>         return request instanceof ServletWebRequest servletRequest
>                 ? servletRequest.getRequest().getRequestURI()
>                 : "";
>     }
> }
> ```
>
> </details>
>
> #### 📁 mapper
>
> <details>
> <summary><strong>📄 StudentMapper.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/mapper/StudentMapper.java`
>
> ```java
> package com.academy.presentation.mapper;
>
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.presentation.dto.PageResponse;
> import com.academy.presentation.dto.StudentRequest;
> import com.academy.presentation.dto.StudentResponse;
> import org.springframework.data.domain.Page;
>
> public final class StudentMapper {
>
>     private StudentMapper() {
>     }
>
>     public static StudentEntity toEntity(StudentRequest request) {
>         return new StudentEntity(null, request.name(), request.age(), request.email(), request.status());
>     }
>
>     public static StudentResponse toResponse(StudentEntity entity) {
>         return new StudentResponse(entity.getId(), entity.getName(), entity.getAge(),
>                 entity.getEmail(), entity.getStatus());
>     }
>
>     public static PageResponse<StudentResponse> toPageResponse(Page<StudentEntity> page) {
>         return new PageResponse<>(
>                 page.getContent().stream().map(StudentMapper::toResponse).toList(),
>                 page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
>     }
> }
> ```
>
> </details>
>
> #### 📁 config
>
> <details>
> <summary><strong>📄 OpenApiConfig.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/config/OpenApiConfig.java`
>
> ```java
> package com.academy.presentation.config;
>
> import com.academy.presentation.dto.ErrorResponse;
> import io.swagger.v3.core.converter.ModelConverters;
> import io.swagger.v3.oas.models.Components;
> import io.swagger.v3.oas.models.OpenAPI;
> import io.swagger.v3.oas.models.PathItem;
> import io.swagger.v3.oas.models.info.Contact;
> import io.swagger.v3.oas.models.info.Info;
> import io.swagger.v3.oas.models.info.License;
> import io.swagger.v3.oas.models.media.Content;
> import io.swagger.v3.oas.models.media.MediaType;
> import io.swagger.v3.oas.models.media.Schema;
> import io.swagger.v3.oas.models.responses.ApiResponse;
> import io.swagger.v3.oas.models.security.SecurityRequirement;
> import io.swagger.v3.oas.models.security.SecurityScheme;
> import io.swagger.v3.oas.models.servers.Server;
> import org.springdoc.core.customizers.OpenApiCustomizer;
> import org.springdoc.core.models.GroupedOpenApi;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
>
> import java.util.LinkedHashMap;
> import java.util.List;
> import java.util.Map;
>
> @Configuration
> public class OpenApiConfig {
>
>     @Bean
>     public OpenAPI academyOpenApi() {
>         return new OpenAPI()
>                 .info(new Info()
>                         .title("Academy API")
>                         .version("1.0.0")
>                         .description("CRUD de estudiantes con DTO, excepciones y seguridad JWT, documentado con OpenAPI. Para probar: **POST /api/v1/auth/login**, copiar el token y pulsar **Authorize**.")
>                         .contact(new Contact().name("Equipo Academy").email("academy@mail.com"))
>                         .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
>                 .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
>                 .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
>                 .components(components());
>     }
>
>     private Components components() {
>         Components components = new Components()
>                 .addSecuritySchemes("bearerAuth", new SecurityScheme()
>                         .type(SecurityScheme.Type.HTTP)
>                         .scheme("bearer")
>                         .bearerFormat("JWT"))
>                 .addResponses("BadRequest", errorResponse("La petición no es válida",
>                         example(400, "Bad Request", "Los datos enviados no son válidos", "/api/v1/students",
>                                 List.of(Map.of("field", "email", "message", "no tiene formato de correo electrónico")))))
>                 .addResponses("NotFound", errorResponse("El recurso no existe",
>                         example(404, "Not Found", "No existe un estudiante con id 999", "/api/v1/students/999", null)))
>                 .addResponses("Conflict", errorResponse("El correo ya está registrado",
>                         example(409, "Conflict", "El correo ana@mail.com ya está registrado", "/api/v1/students", null)))
>                 .addResponses("Unauthorized", errorResponse("No autenticado: falta el token, no es válido o expiró",
>                         example(401, "Unauthorized", "Se requiere autenticación para acceder a este recurso", "/api/v1/students", null)))
>                 .addResponses("Forbidden", errorResponse("Autenticado, pero su rol no permite la operación",
>                         example(403, "Forbidden", "No tiene permisos para realizar esta operación", "/api/v1/students/1", null)));
>         // El esquema ErrorResponse se agrega a mano porque ninguna operación lo declara como tipo de retorno.
>         ModelConverters.getInstance().readAll(ErrorResponse.class).forEach(components::addSchemas);
>         return components;
>     }
>
>     private Map<String, Object> example(int status, String error, String message, String path, List<?> details) {
>         Map<String, Object> example = new LinkedHashMap<>();
>         example.put("timestamp", "2026-11-01T10:15:30.123Z");
>         example.put("status", status);
>         example.put("error", error);
>         example.put("message", message);
>         example.put("path", path);
>         if (details != null) {
>             example.put("details", details);
>         }
>         return example;
>     }
>
>     private ApiResponse errorResponse(String description, Map<String, Object> example) {
>         return new ApiResponse()
>                 .description(description)
>                 .content(new Content().addMediaType("application/json", new MediaType()
>                         .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
>                         .example(example)));
>     }
>
>     /**
>      * Agrega 401 a toda operación protegida y 403 a las que exigen el rol ADMIN (ver SecurityConfig).
>      * Se agrega a cada grupo: con GroupedOpenApi, los personalizadores globales no se aplican.
>      */
>     private OpenApiCustomizer securityResponses() {
>         return openApi -> openApi.getPaths().forEach((path, item) ->
>                 item.readOperationsMap().forEach((method, operation) -> {
>                     boolean isPublic = operation.getSecurity() != null && operation.getSecurity().isEmpty();
>                     if (isPublic) {
>                         return;
>                     }
>                     operation.getResponses().addApiResponse("401",
>                             new ApiResponse().$ref("#/components/responses/Unauthorized"));
>                     if (method == PathItem.HttpMethod.DELETE || path.startsWith("/internal/")) {
>                         operation.getResponses().addApiResponse("403",
>                                 new ApiResponse().$ref("#/components/responses/Forbidden"));
>                     }
>                 }));
>     }
>
>     @Bean
>     public GroupedOpenApi studentsGroup() {
>         return GroupedOpenApi.builder()
>                 .group("students")
>                 .addOpenApiCustomizer(securityResponses())
>                 .displayName("Students")
>                 .pathsToMatch("/api/v1/students/**")
>                 .build();
>     }
>
>     @Bean
>     public GroupedOpenApi authGroup() {
>         return GroupedOpenApi.builder()
>                 .group("auth")
>                 .addOpenApiCustomizer(securityResponses())
>                 .displayName("Auth")
>                 .pathsToMatch("/api/v1/auth/**")
>                 .build();
>     }
>
>     @Bean
>     public GroupedOpenApi internalGroup() {
>         return GroupedOpenApi.builder()
>                 .group("internal")
>                 .addOpenApiCustomizer(securityResponses())
>                 .displayName("Internal")
>                 .pathsToMatch("/internal/**")
>                 .build();
>     }
> }
> ```
>
> </details>
>
> #### 📁 controller
>
> <details>
> <summary><strong>📄 AuthController.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/controller/AuthController.java`
>
> ```java
> package com.academy.presentation.controller;
>
> import com.academy.presentation.dto.LoginRequest;
> import com.academy.presentation.dto.LoginResponse;
> import com.academy.presentation.dto.MeResponse;
> import com.academy.security.AuthService;
> import io.swagger.v3.oas.annotations.Operation;
> import io.swagger.v3.oas.annotations.responses.ApiResponse;
> import io.swagger.v3.oas.annotations.security.SecurityRequirements;
> import io.swagger.v3.oas.annotations.tags.Tag;
> import jakarta.validation.Valid;
> import org.springframework.security.core.Authentication;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.RequestBody;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
>
> @RestController
> @RequestMapping("/api/v1/auth")
> @Tag(name = "Auth", description = "Inicio de sesión y datos del usuario autenticado")
> public class AuthController {
>
>     private final AuthService service;
>
>     public AuthController(AuthService service) {
>         this.service = service;
>     }
>
>     @Operation(summary = "Iniciar sesión",
>             description = "Valida usuario y contraseña y devuelve un JWT. Cuentas de práctica: `admin` / `admin123` (ADMIN) y `ana` / `ana123` (USER).")
>     @SecurityRequirements
>     @ApiResponse(responseCode = "200", description = "Sesión iniciada: devuelve el token")
>     @ApiResponse(responseCode = "400", ref = "BadRequest")
>     @ApiResponse(responseCode = "401", ref = "Unauthorized")
>     @PostMapping("/login")
>     public LoginResponse login(@Valid @RequestBody LoginRequest request) {
>         String token = service.login(request.username(), request.password());
>         return new LoginResponse(token, "Bearer", service.expirationSeconds());
>     }
>
>     @Operation(summary = "Quién soy", description = "Devuelve el usuario y el rol que salen del token enviado.")
>     @ApiResponse(responseCode = "200", description = "Usuario autenticado")
>     @GetMapping("/me")
>     public MeResponse me(Authentication authentication) {
>         String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
>         return new MeResponse(authentication.getName(), role);
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentController.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
>
> ```java
> package com.academy.presentation.controller;
>
> import com.academy.business.service.StudentService;
> import com.academy.presentation.dto.PageResponse;
> import com.academy.presentation.dto.StudentRequest;
> import com.academy.presentation.dto.StudentResponse;
> import com.academy.presentation.dto.StudentStatusRequest;
> import com.academy.presentation.mapper.StudentMapper;
> import io.swagger.v3.oas.annotations.Hidden;
> import io.swagger.v3.oas.annotations.Operation;
> import io.swagger.v3.oas.annotations.Parameter;
> import io.swagger.v3.oas.annotations.headers.Header;
> import io.swagger.v3.oas.annotations.media.Content;
> import io.swagger.v3.oas.annotations.media.ExampleObject;
> import io.swagger.v3.oas.annotations.media.Schema;
> import io.swagger.v3.oas.annotations.responses.ApiResponse;
> import io.swagger.v3.oas.annotations.tags.Tag;
> import jakarta.validation.Valid;
> import org.springdoc.core.annotations.ParameterObject;
> import org.springframework.data.domain.Pageable;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.DeleteMapping;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PatchMapping;
> import org.springframework.web.bind.annotation.PathVariable;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.PutMapping;
> import org.springframework.web.bind.annotation.RequestBody;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RequestParam;
> import org.springframework.web.bind.annotation.RestController;
> import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
>
> import java.net.URI;
>
> @RestController
> @RequestMapping("/api/v1/students")
> @Tag(name = "Students", description = "Operaciones sobre estudiantes")
> public class StudentController {
>
>     private final StudentService service;
>
>     public StudentController(StudentService service) {
>         this.service = service;
>     }
>
>     @Operation(summary = "Listar estudiantes",
>             description = "Devuelve una página de estudiantes. Con `name` filtra por nombre (contiene, sin importar mayúsculas). El tamaño máximo de página es 50.")
>     @ApiResponse(responseCode = "200", description = "Página de estudiantes")
>     @ApiResponse(responseCode = "400", ref = "BadRequest")
>     @GetMapping
>     public PageResponse<StudentResponse> search(
>             @Parameter(description = "Parte del nombre a buscar", example = "ana")
>             @RequestParam(required = false) String name,
>             @ParameterObject Pageable pageable) {
>         return StudentMapper.toPageResponse(service.search(name, pageable));
>     }
>
>     @Operation(summary = "Buscar un estudiante por id")
>     @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
>     @ApiResponse(responseCode = "400", ref = "BadRequest")
>     @ApiResponse(responseCode = "404", ref = "NotFound")
>     @GetMapping("/{id}")
>     public ResponseEntity<StudentResponse> findById(
>             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
>         return ResponseEntity.ok(StudentMapper.toResponse(service.findById(id)));
>     }
>
>     @Operation(summary = "Buscar un estudiante por correo",
>             description = "Obsoleto: usar `GET /api/v1/students?name=` o `GET /api/v1/students/{id}`.",
>             deprecated = true)
>     @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
>     @ApiResponse(responseCode = "400", ref = "BadRequest")
>     @ApiResponse(responseCode = "404", ref = "NotFound")
>     @GetMapping("/by-email")
>     public ResponseEntity<StudentResponse> findByEmail(
>             @Parameter(description = "Correo exacto", example = "ana@mail.com") @RequestParam String email) {
>         return ResponseEntity.ok(StudentMapper.toResponse(service.findByEmail(email)));
>     }
>
>     @Operation(summary = "Crear un estudiante",
>             description = "El `id` lo genera la base y no se envía. Si el correo ya existe responde `409`.")
>     @io.swagger.v3.oas.annotations.parameters.RequestBody(
>             description = "Datos del estudiante",
>             content = @Content(examples = {
>                     @ExampleObject(name = "Estudiante activo", summary = "Un estudiante activo",
>                             value = "{\"name\":\"Ana Torres\",\"age\":22,\"email\":\"ana@mail.com\",\"status\":\"ACTIVE\"}"),
>                     @ExampleObject(name = "Estudiante inactivo", summary = "Un estudiante inactivo",
>                             value = "{\"name\":\"Luis Pérez\",\"age\":25,\"email\":\"luis@mail.com\",\"status\":\"INACTIVE\"}")
>             }))
>     @ApiResponse(responseCode = "201", description = "Estudiante creado",
>             headers = @Header(name = "Location", description = "URL del estudiante creado",
>                     schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/students/1")))
>     @ApiResponse(responseCode = "400", ref = "BadRequest")
>     @ApiResponse(responseCode = "409", ref = "Conflict")
>     @PostMapping
>     public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
>         StudentResponse body = StudentMapper.toResponse(service.create(StudentMapper.toEntity(request)));
>         URI location = ServletUriComponentsBuilder.fromCurrentRequest()
>                 .path("/{id}").buildAndExpand(body.id()).toUri();
>         return ResponseEntity.created(location).body(body);
>     }
>
>     @Operation(summary = "Reemplazar un estudiante",
>             description = "Reemplaza todos los datos del estudiante. Responde `404` si no existe y `409` si el correo es de otro estudiante.")
>     @ApiResponse(responseCode = "200", description = "Estudiante actualizado")
>     @ApiResponse(responseCode = "400", ref = "BadRequest")
>     @ApiResponse(responseCode = "404", ref = "NotFound")
>     @ApiResponse(responseCode = "409", ref = "Conflict")
>     @PutMapping("/{id}")
>     public ResponseEntity<StudentResponse> update(
>             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id,
>             @Valid @RequestBody StudentRequest request) {
>         return ResponseEntity.ok(StudentMapper.toResponse(service.update(id, StudentMapper.toEntity(request))));
>     }
>
>     @Operation(summary = "Cambiar el estado de un estudiante",
>             description = "Cambia solo el estado; el resto de los datos no se toca.")
>     @ApiResponse(responseCode = "200", description = "Estado actualizado")
>     @ApiResponse(responseCode = "400", ref = "BadRequest")
>     @ApiResponse(responseCode = "404", ref = "NotFound")
>     @PatchMapping("/{id}/status")
>     public ResponseEntity<StudentResponse> changeStatus(
>             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id,
>             @Valid @RequestBody StudentStatusRequest request) {
>         return ResponseEntity.ok(StudentMapper.toResponse(service.changeStatus(id, request.status())));
>     }
>
>     @Operation(summary = "Eliminar un estudiante", description = "Solo ADMIN.")
>     @ApiResponse(responseCode = "204", description = "Estudiante eliminado")
>     @ApiResponse(responseCode = "404", ref = "NotFound")
>     @DeleteMapping("/{id}")
>     public ResponseEntity<Void> delete(
>             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
>         service.delete(id);
>         return ResponseEntity.noContent().build();
>     }
>
>     @Hidden
>     @GetMapping("/ping")
>     public String ping() {
>         return "pong";
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 InternalController.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/controller/InternalController.java`
>
> ```java
> package com.academy.presentation.controller;
>
> import com.academy.business.exception.StudentEmailAlreadyExistsException;
> import com.academy.business.service.StudentService;
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.persistence.entity.StudentStatus;
> import io.swagger.v3.oas.annotations.Operation;
> import io.swagger.v3.oas.annotations.security.SecurityRequirements;
> import io.swagger.v3.oas.annotations.tags.Tag;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
>
> import java.util.Map;
>
> @RestController
> @RequestMapping("/internal")
> @Tag(name = "Internal", description = "Operaciones de apoyo para el equipo (no son parte de la API pública)")
> public class InternalController {
>
>     private final StudentService service;
>
>     public InternalController(StudentService service) {
>         this.service = service;
>     }
>
>     @Operation(summary = "Estado del servicio", description = "Público: no pide token.")
>     @SecurityRequirements
>     @GetMapping("/health")
>     public Map<String, String> health() {
>         return Map.of("status", "UP");
>     }
>
>     @Operation(summary = "Cargar estudiantes de ejemplo", description = "Crea tres estudiantes si no existen. Solo ADMIN.")
>     @PostMapping("/seed")
>     public Map<String, Integer> seed() {
>         int created = 0;
>         StudentEntity[] samples = {
>                 new StudentEntity(null, "Ana Torres", 22, "ana@mail.com", StudentStatus.ACTIVE),
>                 new StudentEntity(null, "Luis Pérez", 25, "luis@mail.com", StudentStatus.ACTIVE),
>                 new StudentEntity(null, "María Gómez", 31, "maria@mail.com", StudentStatus.INACTIVE)
>         };
>         for (StudentEntity sample : samples) {
>             try {
>                 service.create(sample);
>                 created++;
>             } catch (StudentEmailAlreadyExistsException ignored) {
>                 // ya existe: no se vuelve a crear
>             }
>         }
>         return Map.of("created", created);
>     }
> }
> ```
>
> </details>

</details>
