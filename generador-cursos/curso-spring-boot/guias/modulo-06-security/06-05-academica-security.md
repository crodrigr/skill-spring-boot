# 🔐 06-05 · Guía paso a paso — Academia con Spring Security y JWT

> 👩‍🏫 **Material docente.** Aplica la [guía 06-01](06-01-guia-crud-estudiantes-con-security.md) (Spring Security, BCrypt y JWT) al proyecto de la academia ya con excepciones ([`05-02-academica-excepciones`](../modulo-05-excepciones/05-02-academica-excepciones.md), Módulo 05): **estudiantes**, **cursos** y **matrículas** con MySQL. Sobre él se agrega **seguridad**, sin tocar la lógica de negocio.
> Contiene código completo: no se entrega a los estudiantes antes de resolver el módulo.
>
> 🚦 **Punto de partida.** Esta guía se puede iniciar directamente desde la [guía 05-02](../modulo-05-excepciones/05-02-academica-excepciones.md). Si no se hizo, se descarga su código terminado desde [`academica-excepciones/`](../modulo-05-excepciones/codigo/academica-excepciones/) y se continúa desde el Paso 1.
>
> 📦 **Proyecto terminado:** [`academica-security/`](codigo/academica-security/) · 🧾 **Todo el código final, listo para copiar:** [`06-06-academica-security-codigo-completo.md`](06-06-academica-security-codigo-completo.md).
>
> 🧪 **Probar todas las rutas:** el script [`probar-apis.sh`](codigo/academica-security/probar-apis.sh) (23 comprobaciones de código HTTP).
>
> ✅ **Verificada.** El código se compiló para Java 21 y se ejecutó con Spring Boot 4.1.2, Spring Security 7.1, JJWT 0.12.6, springdoc-openapi 3.2.0 y MySQL 8. Las respuestas HTTP, el contenido del token, las cuentas de la base y el contrato OpenAPI que se muestran son salidas reales de la aplicación. Lo que se cuenta de la pantalla de Swagger UI se deduce de ese contrato.
>
> 🧭 **Idea central.** Seguridad son **dos preguntas distintas**: *¿quién eres?* (**autenticación**, `401`) y *¿puedes hacer esto?* (**autorización**, `403`). Con tres recursos se nota la ventaja de hacerlo **en un solo lugar**: una `SecurityFilterChain` decide para **todas** las rutas, y `StudentService`, `CourseService` y `EnrollmentService` no cambian.

## 🧭 La idea en un minuto

```text
Cliente ──(1) usuario + contraseña──▶ POST /api/v1/auth/login ──▶ JWT firmado
Cliente ──(2) Authorization: Bearer <JWT> ──▶ JwtAuthenticationFilter ──▶ SecurityFilterChain ──▶ controlador
                                                    │ token malo ──▶ 401   │ rol insuficiente ──▶ 403
```

Los filtros de seguridad corren **antes** de Spring MVC: el `@RestControllerAdvice` nunca ve los `401` ni los `403` de la cadena, así que se escriben a mano con el mismo `ErrorResponse`.


<details>
<summary><strong>🎯 Objetivos</strong></summary>

> Al terminar, el estudiante:
>
> - Agrega `spring-boot-starter-security` y JJWT, y comprende **qué cambia** con ellas (todo queda protegido).
> - Distingue **autenticación** (`401`) de **autorización** (`403`).
> - Declara una **`SecurityFilterChain`** *stateless* con tres niveles: rutas públicas, rutas para cualquier cuenta autenticada y rutas solo para `ADMIN`.
> - Guarda cuentas en la base con la contraseña **cifrada con BCrypt** y las conecta con `UserDetailsService`.
> - Emite un **JWT** en un endpoint de login y lo valida con un **filtro** en cada petición.
> - Hace que los errores `401` y `403` salgan con el mismo `ErrorResponse` que los de negocio (`404`, `409`…).
> - Hace que **Authorize** de Swagger funcione y documenta `401` y `403` **una sola vez**, para los tres recursos.
> - Comprueba cada regla con `curl` y con el script `probar-apis.sh`.

</details>

<details>
<summary><strong>📝 Introducción: qué cambia respecto a la guía 06-01</strong></summary>

> | Pieza | Igual que en la [guía 06-01](06-01-guia-crud-estudiantes-con-security.md) |
> |---|---|
> | La idea | Login → JWT → filtro → `SecurityFilterChain` |
> | Cuentas | `UserAccountEntity`, `Role`, BCrypt, `JpaUserDetailsService`, `AccountSeeder` |
> | Tokens | `JwtService`, `JwtAuthenticationFilter`, `AuthService`, `AuthController` |
> | Errores de seguridad | `SecurityErrorWriter`, `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler` |
>
> **Lo nuevo con tres recursos:**
>
> | Novedad | Detalle |
> |---|---|
> | Más rutas que proteger | Estudiantes, cursos y matrículas bajo `/api/v1/**` |
> | La regla `ADMIN` cambia de lugar | En 06-01 era `DELETE /students/**`; la academia **no tiene** `DELETE`. Aquí es `POST /api/v1/courses` (crear cursos) y `/internal/**` |
> | Una matrícula puede fallar de **seis** maneras | `401` y `403` (seguridad) se suman a `400`, `404` y `409` (negocio): todas con el mismo `ErrorResponse` |
> | Un solo grupo de Swagger | `academy` (`/api/v1/**`) más `internal`; el personalizador agrega `401`/`403` a todas las operaciones |
> | `GlobalExceptionHandler` | Pasa a extender `ResponseEntityExceptionHandler` (como en 06-01) y conserva los agrupados `404` y `409` de la academia |
>
> > 💡 La explicación detallada de cada pieza (por qué `csrf` se desactiva, qué es *stateless*, cómo se lee un JWT) está en la [guía 06-01](06-01-guia-crud-estudiantes-con-security.md). Aquí se aplica y se prueba sobre la academia.

</details>

<details>
<summary><strong>📏 Quién puede qué</strong></summary>

> | Ruta | Sin token | `USER` (`ana`) | `ADMIN` (`admin`) |
> |---|:-:|:-:|:-:|
> | `POST /api/v1/auth/login` · `GET /internal/health` · Swagger | ✅ | ✅ | ✅ |
> | `GET /api/v1/auth/me` | `401` | ✅ | ✅ |
> | Estudiantes: `GET` y `POST` · Cursos: `GET` · Matrículas: `GET` y `POST` | `401` | ✅ | ✅ |
> | `POST /api/v1/courses` (crear un curso) | `401` | `403` | ✅ |
> | `POST /internal/seed` | `401` | `403` | ✅ |
>
> | Código | Situación | HTTP | Mensaje (`message`) |
> |---|---|:-:|---|
> | **RN-01** | Ruta protegida **sin** token | `401` | `Se requiere autenticación para acceder a este recurso` |
> | **RN-02** | Token con firma incorrecta o mal formado | `401` | `El token no es válido` |
> | **RN-03** | Token vencido | `401` | `El token expiró` |
> | **RN-04** | Login con usuario o contraseña incorrectos | `401` | `Usuario o contraseña incorrectos` |
> | **RN-05** | Autenticado, pero con rol insuficiente (`USER` creando un curso) | `403` | `No tiene permisos para realizar esta operación` |
> | **RN-06** | Login con campos vacíos | `400` | `Los datos enviados no son válidos` + `details` |
>
> > 🔎 **Un solo mensaje para usuario y contraseña.** El login **no** dice cuál de los dos falló: si lo dijera, ayudaría a quien prueba cuentas ajenas.

</details>

<details>
<summary><strong>🛠️ Requerimientos y restricciones</strong></summary>

> - Java 21+, Spring Boot 4.1.2, Spring Security 7.1, JJWT 0.12.6, springdoc 3.2.0 y MySQL 8.
> - API **stateless**: sin sesión y sin CSRF (no hay cookies de sesión que proteger).
> - Contraseñas **solo** como hash BCrypt.
> - La clave del JWT sale de `app.jwt.secret` (reemplazable con la variable `JWT_SECRET`). **La del código es solo para fines educativos.**
> - La capa `business` y los tres servicios **no cambian**; las excepciones de negocio **no** importan nada de Spring Security.
> - Todo error de seguridad usa `ErrorResponse`.
> - Código en **inglés**; mensajes al cliente en **español**.
>
> > 💡 **Alcance.** Quedan fuera: OAuth2, *refresh tokens*, `@PreAuthorize` y registro de usuarios. Un token **no se puede revocar** antes de que venza.

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>

> | Paso | Minutos | Actividad |
> |---|---:|---|
> | 0 | 0 – 10 | Punto de partida: la academia con excepciones |
> | 1 | 10 – 25 | Dependencias y propiedades |
> | 2 | 25 – 65 | Cuentas propias, BCrypt y `UserDetailsService` |
> | 3 | 65 – 125 | JWT: servicio de tokens, filtro y login |
> | 4 | 125 – 165 | La cadena de filtros y los errores `401`/`403` con `ErrorResponse` |
> | 5 | 165 – 190 | Swagger: que *Authorize* funcione |
> | 6 | 190 – 225 | Prueba de punta a punta |

</details>

<details>
<summary><strong>🏗️ Arquitectura: el árbol de archivos (🆕 nuevo · ✏️ cambia)</strong></summary>

> ```text
> 📁 academica-security
> ├── ✏️ pom.xml                                   starter-security + JJWT
> ├── 🆕 probar-apis.sh
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 security
>     │   │   ├── 🆕 SecurityConfig.java            SecurityFilterChain, PasswordEncoder, AuthenticationManager
>     │   │   ├── 🆕 JwtProperties.java · JwtService.java · JwtAuthenticationFilter.java
>     │   │   ├── 🆕 AuthService.java · JpaUserDetailsService.java · AccountSeeder.java
>     │   │   └── 🆕 SecurityErrorWriter.java · RestAuthenticationEntryPoint.java · RestAccessDeniedHandler.java
>     │   ├── 📁 presentation
>     │   │   ├── 📁 config
>     │   │   │   └── ✏️ OpenApiConfig.java         Authorize, 401 y 403
>     │   │   ├── 📁 controller
>     │   │   │   ├── 🆕 AuthController.java        login y me
>     │   │   │   ├── ✏️ CourseController.java      crear curso: solo ADMIN
>     │   │   │   ├── ✏️ InternalController.java    health público, seed solo ADMIN
>     │   │   │   └── 📄 StudentController.java · EnrollmentController.java
>     │   │   ├── 📁 dto
>     │   │   │   ├── 🆕 LoginRequest.java · LoginResponse.java · MeResponse.java
>     │   │   │   └── 📄 ErrorResponse · StudentRequest · … (sin cambios)
>     │   │   ├── 📁 exception
>     │   │   │   └── ✏️ GlobalExceptionHandler.java   + AuthenticationException y AccessDeniedException
>     │   │   └── 📁 mapper                         sin cambios
>     │   ├── 📁 business                           sin cambios
>     │   └── 📁 persistence
>     │       ├── 📁 entity
>     │       │   ├── 🆕 Role.java · UserAccountEntity.java
>     │       │   └── 📄 StudentEntity · CourseEntity · EnrollmentEntity
>     │       └── 📁 repository
>     │           ├── 🆕 UserAccountJpaRepository.java
>     │           └── 📄 Student… · Course… · Enrollment…
>     └── 📁 resources
>         └── ✏️ application.properties             app.jwt.* y persist-authorization
> ```
>
> | Capa | Qué aporta a la seguridad |
> |---|---|
> | `security` | **Todo** lo propio de Spring Security: cadena de filtros, tokens, cuentas, manejadores de error |
> | `persistence` | La cuenta (`UserAccountEntity`) y su repositorio |
> | `business` | **Nada**: los tres servicios no saben que existe la seguridad |
> | `presentation` | Login (`AuthController`), DTO de login y la traducción de errores a HTTP |

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Punto de partida: la academia con excepciones</strong></summary>

> <details>
> <summary><strong>Paso 0.1 · MySQL y el proyecto</strong></summary>
>
> > Se parte del proyecto que se termina en la [guía 05-02](../modulo-05-excepciones/05-02-academica-excepciones.md) ([`academica-excepciones/`](../modulo-05-excepciones/codigo/academica-excepciones/)). Se copia como `academica-security` y se levanta la base:
> >
> > ```bash
> > cp -r academica-excepciones academica-security
> > docker run -d --name academy-mysql \
> >   -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academia \
> >   -p 3306:3306 mysql:8.0
> > ```
> >
> > > | Lo que **ya existe** | Dónde |
> > > |---|---|
> > > | Tres entidades y tres repositorios | `persistence` |
> > > | `StudentService`, `CourseService`, `EnrollmentService` y sus `…Impl`, con seis excepciones propias | `business` |
> > > | Cuatro controladores, DTO, `ErrorResponse` y `GlobalExceptionHandler` | `presentation` |
> > > | `OpenApiConfig` (Swagger, grupos, respuestas de error reutilizables) | `presentation.config` |
>
> </details>
>
> <details>
> <summary><strong>Paso 0.2 · Comprobar que funciona</strong></summary>
>
> > ```bash
> > ./mvnw spring-boot:run
> > curl -X POST localhost:8080/internal/seed          # {"courses":2,"students":3}
> > curl localhost:8080/api/v1/courses                 # responde sin pedir nada
> > ```
> >
> > > ✅ **Punto de control 0.** Todo responde **sin identificarse**: cualquiera puede crear cursos y ejecutar `POST /internal/seed`. Eso es lo que se va a corregir.
>
> </details>

</details>

---

<details>
<summary><strong>🛡️ Paso 1 — Dependencias y propiedades</strong></summary>

> > 🗣️ **Para abrir el paso.** Con solo `spring-boot-starter-security` en el `pom.xml`, Spring Boot protege **todas** las rutas con HTTP Basic y una contraseña aleatoria que imprime en la consola; un `POST` además falla por CSRF. Ese recorrido está explicado y medido en el [Paso 1 de la guía 06-01](06-01-guia-crud-estudiantes-con-security.md): aquí se pasa directo a lo definitivo.
>
> <details>
> <summary><strong>Paso 1.1 · Spring Security y JJWT</strong></summary>
>
> > <details>
> > <summary><strong>📄 pom.xml (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > > ```xml
> > > <!-- pom.xml: dentro de <dependencies> -->
> > > <dependency>
> > >     <groupId>org.springframework.boot</groupId>
> > >     <artifactId>spring-boot-starter-security</artifactId>
> > > </dependency>
> > > <dependency>
> > >     <groupId>io.jsonwebtoken</groupId>
> > >     <artifactId>jjwt-api</artifactId>
> > >     <version>0.12.6</version>
> > > </dependency>
> > > <dependency>
> > >     <groupId>io.jsonwebtoken</groupId>
> > >     <artifactId>jjwt-impl</artifactId>
> > >     <version>0.12.6</version>
> > >     <scope>runtime</scope>
> > > </dependency>
> > > <dependency>
> > >     <groupId>io.jsonwebtoken</groupId>
> > >     <artifactId>jjwt-jackson</artifactId>
> > >     <version>0.12.6</version>
> > >     <scope>runtime</scope>
> > > </dependency>
> > > ```
> >
> > </details>
> >
> > `spring-boot-starter-security` trae el motor de seguridad; **JJWT** crea y verifica los tokens (`jjwt-impl` y `jjwt-jackson` solo hacen falta al ejecutar).
>
> </details>
>
> <details>
> <summary><strong>Paso 1.2 · La clave y la vigencia del token</strong></summary>
>
> > <details>
> > <summary><strong>📄 application.properties (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > > ```properties
> > > # JWT -- SOLO PARA FINES EDUCATIVOS: en un proyecto real la clave se entrega por
> > > # variable de entorno (JWT_SECRET) o un gestor de secretos, nunca escrita en el repositorio.
> > > app.jwt.secret=${JWT_SECRET:academy-clave-solo-educativa-cambiar-en-produccion-0123456789}
> > > app.jwt.expiration-seconds=3600
> > >
> > > # Swagger: recuerda el token entre recargas de la página
> > > springdoc.swagger-ui.persist-authorization=true
> > > ```
> >
> > </details>
> >
> > > ⚠️ La clave del ejemplo es **solo educativa**. Con `JWT_SECRET` en el entorno se reemplaza sin tocar el código. Debe tener **al menos 32 caracteres** (HS256).
>
> </details>

</details>

---

<details>
<summary><strong>👤 Paso 2 — Cuentas propias, BCrypt y `UserDetailsService`</strong></summary>

> > 🗣️ **Para abrir el paso.** Spring Security no sabe de la tabla de cuentas: hace falta una entidad, un repositorio y un puente (`UserDetailsService`). La contraseña **nunca** se guarda en texto plano.
>
> <details>
> <summary><strong>Paso 2.1 · La cuenta: rol, entidad y repositorio</strong></summary>
>
> > <details>
> > <summary><strong>📄 Role.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/persistence/entity/Role.java`](codigo/academica-security/src/main/java/com/academy/persistence/entity/Role.java)
> > >
> > > ```java
> > > package com.academy.persistence.entity;
> > >
> > > public enum Role {
> > >     ADMIN,
> > >     USER
> > > }
> > > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 UserAccountEntity.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/persistence/entity/UserAccountEntity.java`](codigo/academica-security/src/main/java/com/academy/persistence/entity/UserAccountEntity.java)
> > >
> > > ```java
> > > package com.academy.persistence.entity;
> > >
> > > import jakarta.persistence.Column;
> > > import jakarta.persistence.Entity;
> > > import jakarta.persistence.EnumType;
> > > import jakarta.persistence.Enumerated;
> > > import jakarta.persistence.GeneratedValue;
> > > import jakarta.persistence.GenerationType;
> > > import jakarta.persistence.Id;
> > > import jakarta.persistence.Table;
> > >
> > > @Entity
> > > @Table(name = "user_account")
> > > public class UserAccountEntity {
> > >
> > >     @Id
> > >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> > >     private Long id;
> > >
> > >     @Column(nullable = false, unique = true)
> > >     private String username;
> > >
> > >     // Siempre el hash BCrypt, nunca la contraseña en texto plano
> > >     @Column(name = "password_hash", nullable = false)
> > >     private String passwordHash;
> > >
> > >     @Enumerated(EnumType.STRING)
> > >     @Column(nullable = false)
> > >     private Role role;
> > >
> > >     protected UserAccountEntity() {
> > >     }
> > >
> > >     public UserAccountEntity(Long id, String username, String passwordHash, Role role) {
> > >         this.id = id;
> > >         this.username = username;
> > >         this.passwordHash = passwordHash;
> > >         this.role = role;
> > >     }
> > >
> > >     public Long getId() { return id; }
> > >     public String getUsername() { return username; }
> > >     public String getPasswordHash() { return passwordHash; }
> > >     public Role getRole() { return role; }
> > > }
> > > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 UserAccountJpaRepository.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/persistence/repository/UserAccountJpaRepository.java`](codigo/academica-security/src/main/java/com/academy/persistence/repository/UserAccountJpaRepository.java)
> > >
> > > ```java
> > > package com.academy.persistence.repository;
> > >
> > > import com.academy.persistence.entity.UserAccountEntity;
> > > import org.springframework.data.jpa.repository.JpaRepository;
> > >
> > > import java.util.Optional;
> > >
> > > public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, Long> {
> > >
> > >     Optional<UserAccountEntity> findByUsername(String username);
> > > }
> > > ```
> >
> > </details>
> >
> > > 💡 La entidad guarda `password_hash`, nunca la contraseña. `ddl-auto=create` crea la tabla `user_account` junto con `student`, `course` y `enrollment`.
>
> </details>
>
> <details>
> <summary><strong>Paso 2.2 · Conectar la cuenta con Spring Security</strong></summary>
>
> > <details>
> > <summary><strong>📄 JpaUserDetailsService.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/security/JpaUserDetailsService.java`](codigo/academica-security/src/main/java/com/academy/security/JpaUserDetailsService.java)
> > >
> > > ```java
> > > package com.academy.security;
> > >
> > > import com.academy.persistence.entity.UserAccountEntity;
> > > import com.academy.persistence.repository.UserAccountJpaRepository;
> > > import org.springframework.security.core.userdetails.User;
> > > import org.springframework.security.core.userdetails.UserDetails;
> > > import org.springframework.security.core.userdetails.UserDetailsService;
> > > import org.springframework.security.core.userdetails.UsernameNotFoundException;
> > > import org.springframework.stereotype.Service;
> > >
> > > @Service
> > > public class JpaUserDetailsService implements UserDetailsService {
> > >
> > >     private final UserAccountJpaRepository accounts;
> > >
> > >     public JpaUserDetailsService(UserAccountJpaRepository accounts) {
> > >         this.accounts = accounts;
> > >     }
> > >
> > >     @Override
> > >     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
> > >         UserAccountEntity account = accounts.findByUsername(username)
> > >                 .orElseThrow(() -> new UsernameNotFoundException("No existe la cuenta " + username));
> > >         return User.withUsername(account.getUsername())
> > >                 .password(account.getPasswordHash())
> > >                 .roles(account.getRole().name())
> > >                 .build();
> > >     }
> > > }
> > > ```
> >
> > </details>
> >
> > Spring Security llama a `loadUserByUsername` durante el login y compara la contraseña recibida con el hash usando el `PasswordEncoder` (se declara en el Paso 4).
>
> </details>
>
> <details>
> <summary><strong>Paso 2.3 · Dos cuentas de práctica con la contraseña cifrada</strong></summary>
>
> > <details>
> > <summary><strong>📄 AccountSeeder.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/security/AccountSeeder.java`](codigo/academica-security/src/main/java/com/academy/security/AccountSeeder.java)
> > >
> > > ```java
> > > package com.academy.security;
> > >
> > > import com.academy.persistence.entity.Role;
> > > import com.academy.persistence.entity.UserAccountEntity;
> > > import com.academy.persistence.repository.UserAccountJpaRepository;
> > > import org.springframework.boot.CommandLineRunner;
> > > import org.springframework.security.crypto.password.PasswordEncoder;
> > > import org.springframework.stereotype.Component;
> > >
> > > /**
> > >  * Crea dos cuentas de práctica al arrancar. SOLO PARA FINES EDUCATIVOS:
> > >  * en un proyecto real las cuentas no se siembran con contraseñas en el código.
> > >  */
> > > @Component
> > > public class AccountSeeder implements CommandLineRunner {
> > >
> > >     private final UserAccountJpaRepository accounts;
> > >     private final PasswordEncoder encoder;
> > >
> > >     public AccountSeeder(UserAccountJpaRepository accounts, PasswordEncoder encoder) {
> > >         this.accounts = accounts;
> > >         this.encoder = encoder;
> > >     }
> > >
> > >     @Override
> > >     public void run(String... args) {
> > >         createIfMissing("admin", "admin123", Role.ADMIN);
> > >         createIfMissing("ana", "ana123", Role.USER);
> > >     }
> > >
> > >     private void createIfMissing(String username, String rawPassword, Role role) {
> > >         if (accounts.findByUsername(username).isEmpty()) {
> > >             accounts.save(new UserAccountEntity(null, username, encoder.encode(rawPassword), role));
> > >         }
> > >     }
> > > }
> > > ```
> >
> > </details>
> >
> > Al arrancar se crean `admin` / `admin123` (`ADMIN`) y `ana` / `ana123` (`USER`). En la tabla solo quedan los hashes:
> >
> > ```text
> > mysql> select username, left(password_hash,10) hash, role from user_account;
> > username  hash        role
> > admin     $2a$10$Xhx  ADMIN
> > ana       $2a$10$vJy  USER
> > ```
> >
> > > 🔎 Cada ejecución produce hashes distintos para la misma contraseña (BCrypt agrega una *sal* aleatoria), y el prefijo `$2a$10$` indica el algoritmo y el costo.
>
> </details>

</details>

---

<details>
<summary><strong>🎫 Paso 3 — JWT: servicio de tokens, filtro y login</strong></summary>

> > 🗣️ **Para abrir el paso.** Un JWT son tres partes separadas por puntos: cabecera, **datos** (*claims*) y **firma**. El servidor no guarda nada: en cada petición **verifica la firma y la fecha de vencimiento**.
>
> <details>
> <summary><strong>Paso 3.1 · Emitir y verificar tokens</strong></summary>
>
> > <details>
> > <summary><strong>📄 JwtProperties.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/security/JwtProperties.java`](codigo/academica-security/src/main/java/com/academy/security/JwtProperties.java)
> > >
> > > ```java
> > > package com.academy.security;
> > >
> > > import org.springframework.boot.context.properties.ConfigurationProperties;
> > >
> > > @ConfigurationProperties(prefix = "app.jwt")
> > > public record JwtProperties(String secret, long expirationSeconds) {
> > > }
> > > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 JwtService.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/security/JwtService.java`](codigo/academica-security/src/main/java/com/academy/security/JwtService.java)
> > >
> > > ```java
> > > package com.academy.security;
> > >
> > > import io.jsonwebtoken.Claims;
> > > import io.jsonwebtoken.Jwts;
> > > import io.jsonwebtoken.security.Keys;
> > > import org.springframework.stereotype.Service;
> > >
> > > import javax.crypto.SecretKey;
> > > import java.nio.charset.StandardCharsets;
> > > import java.time.Instant;
> > > import java.util.Date;
> > >
> > > @Service
> > > public class JwtService {
> > >
> > >     private final SecretKey key;
> > >     private final long expirationSeconds;
> > >
> > >     public JwtService(JwtProperties properties) {
> > >         this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
> > >         this.expirationSeconds = properties.expirationSeconds();
> > >     }
> > >
> > >     public String generate(String username, String role) {
> > >         Instant now = Instant.now();
> > >         return Jwts.builder()
> > >                 .subject(username)
> > >                 .claim("role", role)
> > >                 .issuedAt(Date.from(now))
> > >                 .expiration(Date.from(now.plusSeconds(expirationSeconds)))
> > >                 .signWith(key, Jwts.SIG.HS256)
> > >                 .compact();
> > >     }
> > >
> > >     /** Verifica la firma y la expiración; lanza JwtException si el token no sirve. */
> > >     public Claims parse(String token) {
> > >         return Jwts.parser()
> > >                 .verifyWith(key)
> > >                 .build()
> > >                 .parseSignedClaims(token)
> > >                 .getPayload();
> > >     }
> > >
> > >     public long expirationSeconds() {
> > >         return expirationSeconds;
> > >     }
> > > }
> > > ```
> >
> > </details>
> >
> > `JwtProperties` lee `app.jwt.*`. `JwtService.generate` firma un token con el usuario (`sub`) y el rol (`role`); `parse` verifica firma y vencimiento y lanza `JwtException` si algo falla.
>
> </details>
>
> <details>
> <summary><strong>Paso 3.2 · El filtro que lee el token en cada petición</strong></summary>
>
> > <details>
> > <summary><strong>📄 JwtAuthenticationFilter.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/security/JwtAuthenticationFilter.java`](codigo/academica-security/src/main/java/com/academy/security/JwtAuthenticationFilter.java)
> > >
> > > ```java
> > > package com.academy.security;
> > >
> > > import io.jsonwebtoken.Claims;
> > > import io.jsonwebtoken.ExpiredJwtException;
> > > import io.jsonwebtoken.JwtException;
> > > import jakarta.servlet.FilterChain;
> > > import jakarta.servlet.ServletException;
> > > import jakarta.servlet.http.HttpServletRequest;
> > > import jakarta.servlet.http.HttpServletResponse;
> > > import org.springframework.http.HttpHeaders;
> > > import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
> > > import org.springframework.security.core.authority.SimpleGrantedAuthority;
> > > import org.springframework.security.core.context.SecurityContextHolder;
> > > import org.springframework.web.filter.OncePerRequestFilter;
> > >
> > > import java.io.IOException;
> > > import java.util.List;
> > >
> > > public class JwtAuthenticationFilter extends OncePerRequestFilter {
> > >
> > >     /** Atributo de la petición donde el filtro deja el motivo si el token no sirve. */
> > >     public static final String AUTH_ERROR_ATTRIBUTE = "auth.error";
> > >
> > >     private static final String BEARER_PREFIX = "Bearer ";
> > >
> > >     private final JwtService jwtService;
> > >
> > >     public JwtAuthenticationFilter(JwtService jwtService) {
> > >         this.jwtService = jwtService;
> > >     }
> > >
> > >     @Override
> > >     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
> > >                                     FilterChain chain) throws ServletException, IOException {
> > >         String header = request.getHeader(HttpHeaders.AUTHORIZATION);
> > >         if (header != null && header.startsWith(BEARER_PREFIX)) {
> > >             try {
> > >                 Claims claims = jwtService.parse(header.substring(BEARER_PREFIX.length()));
> > >                 var authentication = new UsernamePasswordAuthenticationToken(
> > >                         claims.getSubject(), null,
> > >                         List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class))));
> > >                 SecurityContextHolder.getContext().setAuthentication(authentication);
> > >             } catch (ExpiredJwtException ex) {
> > >                 request.setAttribute(AUTH_ERROR_ATTRIBUTE, "El token expiró");
> > >             } catch (JwtException | IllegalArgumentException ex) {
> > >                 request.setAttribute(AUTH_ERROR_ATTRIBUTE, "El token no es válido");
> > >             }
> > >         }
> > >         chain.doFilter(request, response);
> > >     }
> > > }
> > > ```
> >
> > </details>
> >
> > El filtro **no responde** errores: si el token no sirve, deja el motivo en un atributo de la petición y sigue la cadena. Quien responde `401` es el `AuthenticationEntryPoint` (Paso 4), que lee ese motivo.
>
> </details>
>
> <details>
> <summary><strong>Paso 3.3 · El login: servicio, DTO y controlador</strong></summary>
>
> > <details>
> > <summary><strong>📄 AuthService.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/security/AuthService.java`](codigo/academica-security/src/main/java/com/academy/security/AuthService.java)
> > >
> > > ```java
> > > package com.academy.security;
> > >
> > > import org.springframework.security.authentication.AuthenticationManager;
> > > import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
> > > import org.springframework.security.core.Authentication;
> > > import org.springframework.stereotype.Service;
> > >
> > > @Service
> > > public class AuthService {
> > >
> > >     private final AuthenticationManager authenticationManager;
> > >     private final JwtService jwtService;
> > >
> > >     public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
> > >         this.authenticationManager = authenticationManager;
> > >         this.jwtService = jwtService;
> > >     }
> > >
> > >     /** Valida usuario y contraseña (lanza AuthenticationException si no coinciden) y emite el JWT. */
> > >     public String login(String username, String password) {
> > >         Authentication authentication = authenticationManager.authenticate(
> > >                 new UsernamePasswordAuthenticationToken(username, password));
> > >         String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
> > >         return jwtService.generate(authentication.getName(), role);
> > >     }
> > >
> > >     public long expirationSeconds() {
> > >         return jwtService.expirationSeconds();
> > >     }
> > > }
> > > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 LoginRequest.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/presentation/dto/LoginRequest.java`](codigo/academica-security/src/main/java/com/academy/presentation/dto/LoginRequest.java)
> > >
> > > ```java
> > > package com.academy.presentation.dto;
> > >
> > > import io.swagger.v3.oas.annotations.media.Schema;
> > > import jakarta.validation.constraints.NotBlank;
> > >
> > > @Schema(description = "Credenciales para iniciar sesión")
> > > public record LoginRequest(
> > >
> > >         @Schema(description = "Nombre de usuario", example = "admin")
> > >         @NotBlank(message = "es obligatorio")
> > >         String username,
> > >
> > >         @Schema(description = "Contraseña", example = "admin123")
> > >         @NotBlank(message = "es obligatoria")
> > >         String password
> > > ) {
> > > }
> > > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 LoginResponse.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/presentation/dto/LoginResponse.java`](codigo/academica-security/src/main/java/com/academy/presentation/dto/LoginResponse.java)
> > >
> > > ```java
> > > package com.academy.presentation.dto;
> > >
> > > import io.swagger.v3.oas.annotations.media.Schema;
> > >
> > > @Schema(description = "Token que el cliente debe enviar en cada petición")
> > > public record LoginResponse(
> > >
> > >         @Schema(description = "JWT firmado", example = "eyJhbGciOiJIUzI1NiJ9...")
> > >         String token,
> > >
> > >         @Schema(description = "Tipo de token: se envía como `Authorization: Bearer <token>`", example = "Bearer")
> > >         String tokenType,
> > >
> > >         @Schema(description = "Segundos de vigencia del token", example = "3600")
> > >         long expiresIn
> > > ) {
> > > }
> > > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 MeResponse.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/presentation/dto/MeResponse.java`](codigo/academica-security/src/main/java/com/academy/presentation/dto/MeResponse.java)
> > >
> > > ```java
> > > package com.academy.presentation.dto;
> > >
> > > import io.swagger.v3.oas.annotations.media.Schema;
> > >
> > > @Schema(description = "Quién es el usuario autenticado")
> > > public record MeResponse(
> > >
> > >         @Schema(description = "Nombre de usuario", example = "ana")
> > >         String username,
> > >
> > >         @Schema(description = "Rol", example = "USER")
> > >         String role
> > > ) {
> > > }
> > > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 AuthController.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/presentation/controller/AuthController.java`](codigo/academica-security/src/main/java/com/academy/presentation/controller/AuthController.java)
> > >
> > > ```java
> > > package com.academy.presentation.controller;
> > >
> > > import com.academy.presentation.dto.LoginRequest;
> > > import com.academy.presentation.dto.LoginResponse;
> > > import com.academy.presentation.dto.MeResponse;
> > > import com.academy.security.AuthService;
> > > import io.swagger.v3.oas.annotations.Operation;
> > > import io.swagger.v3.oas.annotations.responses.ApiResponse;
> > > import io.swagger.v3.oas.annotations.security.SecurityRequirements;
> > > import io.swagger.v3.oas.annotations.tags.Tag;
> > > import jakarta.validation.Valid;
> > > import org.springframework.security.core.Authentication;
> > > import org.springframework.web.bind.annotation.GetMapping;
> > > import org.springframework.web.bind.annotation.PostMapping;
> > > import org.springframework.web.bind.annotation.RequestBody;
> > > import org.springframework.web.bind.annotation.RequestMapping;
> > > import org.springframework.web.bind.annotation.RestController;
> > >
> > > @RestController
> > > @RequestMapping("/api/v1/auth")
> > > @Tag(name = "Auth", description = "Inicio de sesión y datos del usuario autenticado")
> > > public class AuthController {
> > >
> > >     private final AuthService service;
> > >
> > >     public AuthController(AuthService service) {
> > >         this.service = service;
> > >     }
> > >
> > >     @Operation(summary = "Iniciar sesión",
> > >             description = "Valida usuario y contraseña y devuelve un JWT. Cuentas de práctica: `admin` / `admin123` (ADMIN) y `ana` / `ana123` (USER).")
> > >     @SecurityRequirements
> > >     @ApiResponse(responseCode = "200", description = "Sesión iniciada: devuelve el token")
> > >     @ApiResponse(responseCode = "400", ref = "BadRequest")
> > >     @ApiResponse(responseCode = "401", ref = "Unauthorized")
> > >     @PostMapping("/login")
> > >     public LoginResponse login(@Valid @RequestBody LoginRequest request) {
> > >         String token = service.login(request.username(), request.password());
> > >         return new LoginResponse(token, "Bearer", service.expirationSeconds());
> > >     }
> > >
> > >     @Operation(summary = "Quién soy", description = "Devuelve el usuario y el rol que salen del token enviado.")
> > >     @ApiResponse(responseCode = "200", description = "Usuario autenticado")
> > >     @GetMapping("/me")
> > >     public MeResponse me(Authentication authentication) {
> > >         String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
> > >         return new MeResponse(authentication.getName(), role);
> > >     }
> > > }
> > > ```
> >
> > </details>
> >
> > `AuthService` delega en el `AuthenticationManager`: si usuario o contraseña no coinciden lanza `AuthenticationException`, y el manejador global la convierte en `401` (Paso 4.3). `GET /auth/me` devuelve el usuario y el rol que salen **del token**.
>
> </details>

</details>

---

<details>
<summary><strong>🚦 Paso 4 — La cadena de filtros y los errores `401` y `403`</strong></summary>

> > 🗣️ **Para abrir el paso.** La `SecurityFilterChain` decide, ruta por ruta, quién entra. Y como corre **antes** de Spring MVC, sus errores no pasan por el `@RestControllerAdvice`: hay que escribirlos a mano para que tengan el mismo formato que el resto de la API.
>
> <details>
> <summary><strong>Paso 4.1 · Escribir un `ErrorResponse` desde un filtro</strong></summary>
>
> > <details>
> > <summary><strong>📄 SecurityErrorWriter.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/security/SecurityErrorWriter.java`](codigo/academica-security/src/main/java/com/academy/security/SecurityErrorWriter.java)
> > >
> > > ```java
> > > package com.academy.security;
> > >
> > > import com.academy.presentation.dto.ErrorResponse;
> > > import tools.jackson.databind.ObjectMapper;
> > > import jakarta.servlet.http.HttpServletRequest;
> > > import jakarta.servlet.http.HttpServletResponse;
> > > import org.springframework.http.HttpStatus;
> > > import org.springframework.http.MediaType;
> > > import org.springframework.stereotype.Component;
> > >
> > > import java.io.IOException;
> > >
> > > /**
> > >  * Escribe un ErrorResponse a mano. Hace falta porque los filtros de seguridad corren ANTES de Spring MVC:
> > >  * el @RestControllerAdvice nunca llega a ver estos errores.
> > >  */
> > > @Component
> > > public class SecurityErrorWriter {
> > >
> > >     private final ObjectMapper objectMapper;
> > >
> > >     public SecurityErrorWriter(ObjectMapper objectMapper) {
> > >         this.objectMapper = objectMapper;
> > >     }
> > >
> > >     public void write(HttpServletRequest request, HttpServletResponse response,
> > >                       HttpStatus status, String message) throws IOException {
> > >         response.setStatus(status.value());
> > >         response.setContentType(MediaType.APPLICATION_JSON_VALUE);
> > >         response.setCharacterEncoding("UTF-8");
> > >         objectMapper.writeValue(response.getWriter(),
> > >                 ErrorResponse.of(status, message, request.getRequestURI()));
> > >     }
> > > }
> > > ```
> >
> > </details>
> >
> > Reutiliza el mismo `ErrorResponse` de los errores de negocio: el cliente recibe **un solo formato** venga el error de donde venga.
>
> </details>
>
> <details>
> <summary><strong>Paso 4.2 · `401` y `403`: dos manejadores</strong></summary>
>
> > <details>
> > <summary><strong>📄 RestAuthenticationEntryPoint.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/security/RestAuthenticationEntryPoint.java`](codigo/academica-security/src/main/java/com/academy/security/RestAuthenticationEntryPoint.java)
> > >
> > > ```java
> > > package com.academy.security;
> > >
> > > import jakarta.servlet.http.HttpServletRequest;
> > > import jakarta.servlet.http.HttpServletResponse;
> > > import org.springframework.http.HttpHeaders;
> > > import org.springframework.http.HttpStatus;
> > > import org.springframework.security.core.AuthenticationException;
> > > import org.springframework.security.web.AuthenticationEntryPoint;
> > > import org.springframework.stereotype.Component;
> > >
> > > import java.io.IOException;
> > >
> > > /** Se ejecuta cuando alguien SIN autenticar pide un recurso protegido: responde 401. */
> > > @Component
> > > public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
> > >
> > >     private final SecurityErrorWriter errors;
> > >
> > >     public RestAuthenticationEntryPoint(SecurityErrorWriter errors) {
> > >         this.errors = errors;
> > >     }
> > >
> > >     @Override
> > >     public void commence(HttpServletRequest request, HttpServletResponse response,
> > >                          AuthenticationException authException) throws IOException {
> > >         Object reason = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE);
> > >         String message = reason != null ? reason.toString() : "Se requiere autenticación para acceder a este recurso";
> > >         response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
> > >         errors.write(request, response, HttpStatus.UNAUTHORIZED, message);
> > >     }
> > > }
> > > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 RestAccessDeniedHandler.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/security/RestAccessDeniedHandler.java`](codigo/academica-security/src/main/java/com/academy/security/RestAccessDeniedHandler.java)
> > >
> > > ```java
> > > package com.academy.security;
> > >
> > > import jakarta.servlet.http.HttpServletRequest;
> > > import jakarta.servlet.http.HttpServletResponse;
> > > import org.springframework.http.HttpStatus;
> > > import org.springframework.security.access.AccessDeniedException;
> > > import org.springframework.security.web.access.AccessDeniedHandler;
> > > import org.springframework.stereotype.Component;
> > >
> > > import java.io.IOException;
> > >
> > > /** Se ejecuta cuando alguien YA autenticado pide algo que su rol no permite: responde 403. */
> > > @Component
> > > public class RestAccessDeniedHandler implements AccessDeniedHandler {
> > >
> > >     private final SecurityErrorWriter errors;
> > >
> > >     public RestAccessDeniedHandler(SecurityErrorWriter errors) {
> > >         this.errors = errors;
> > >     }
> > >
> > >     @Override
> > >     public void handle(HttpServletRequest request, HttpServletResponse response,
> > >                        AccessDeniedException accessDeniedException) throws IOException {
> > >         errors.write(request, response, HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación");
> > >     }
> > > }
> > > ```
> >
> > </details>
> >
> > | Manejador | Cuándo se ejecuta | Respuesta |
> > |---|---|:-:|
> > | `AuthenticationEntryPoint` | Alguien **sin autenticar** pide un recurso protegido | `401` |
> > | `AccessDeniedHandler` | Alguien **ya autenticado** pide algo que su rol no permite | `403` |
>
> </details>
>
> <details>
> <summary><strong>Paso 4.3 · El login y el manejador global</strong></summary>
>
> > <details>
> > <summary><strong>📄 GlobalExceptionHandler.java (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > > ```java
> > > // imports nuevos
> > > import org.springframework.security.access.AccessDeniedException;
> > > import org.springframework.security.core.AuthenticationException;
> > >
> > > // la clase pasa a extender la base de Spring MVC (así se controlan 404/405/415… con el mismo cuerpo)
> > > public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
> > >
> > >     // Se conservan los dos manejadores agrupados de la academia
> > >     @ExceptionHandler({StudentNotFoundException.class, CourseNotFoundException.class,
> > >             EnrollmentNotFoundException.class})
> > >     public ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) { /* 404 */ }
> > >
> > >     @ExceptionHandler({StudentEmailAlreadyExistsException.class, AlreadyEnrolledException.class,
> > >             CourseFullException.class})
> > >     public ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) { /* 409 */ }
> > >
> > >     // El login falla dentro de un controlador (AuthenticationManager), así que SÍ pasa por este manejador.
> > >     @ExceptionHandler(AuthenticationException.class)
> > >     public ResponseEntity<Object> handleAuthentication(AuthenticationException ex, WebRequest request) {
> > >         return build(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", request, null);
> > >     }
> > >
> > >     // Sin este manejador, el catch-all convertiría un AccessDeniedException en un 500.
> > >     @ExceptionHandler(AccessDeniedException.class)
> > >     public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
> > >         return build(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación", request, null);
> > >     }
> > > }
> > > ```
> >
> > </details>
> >
> > > 🔎 En el proyecto terminado el archivo completo está en [06-06](06-06-academica-security-codigo-completo.md). Lo importante: **se conserva** el manejo de la academia (`404` para las tres excepciones «no existe», `409` para las tres que «chocan») y se suman `401` y `403`.
>
> </details>
>
> <details>
> <summary><strong>Paso 4.4 · La cadena: rutas públicas, autenticadas y de `ADMIN`</strong></summary>
>
> > <details>
> > <summary><strong>📄 SecurityConfig.java</strong></summary>
> >
> > > **Ruta:** [`src/main/java/com/academy/security/SecurityConfig.java`](codigo/academica-security/src/main/java/com/academy/security/SecurityConfig.java)
> > >
> > > ```java
> > > package com.academy.security;
> > >
> > > import org.springframework.boot.context.properties.EnableConfigurationProperties;
> > > import org.springframework.context.annotation.Bean;
> > > import org.springframework.context.annotation.Configuration;
> > > import org.springframework.http.HttpMethod;
> > > import org.springframework.security.authentication.AuthenticationManager;
> > > import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
> > > import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> > > import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
> > > import org.springframework.security.config.http.SessionCreationPolicy;
> > > import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
> > > import org.springframework.security.crypto.password.PasswordEncoder;
> > > import org.springframework.security.web.SecurityFilterChain;
> > > import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
> > >
> > > @Configuration
> > > @EnableWebSecurity
> > > @EnableConfigurationProperties(JwtProperties.class)
> > > public class SecurityConfig {
> > >
> > >     @Bean
> > >     public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService,
> > >                                            RestAuthenticationEntryPoint entryPoint,
> > >                                            RestAccessDeniedHandler accessDeniedHandler) throws Exception {
> > >         http
> > >                 .csrf(csrf -> csrf.disable())
> > >                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
> > >                 .exceptionHandling(errors -> errors
> > >                         .authenticationEntryPoint(entryPoint)
> > >                         .accessDeniedHandler(accessDeniedHandler))
> > >                 .authorizeHttpRequests(auth -> auth
> > >                         .requestMatchers("/api/v1/auth/login", "/internal/health").permitAll()
> > >                         .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll()
> > >                         .requestMatchers(HttpMethod.POST, "/api/v1/courses").hasRole("ADMIN")
> > >                         .requestMatchers("/internal/**").hasRole("ADMIN")
> > >                         .anyRequest().authenticated())
> > >                 .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
> > >         return http.build();
> > >     }
> > >
> > >     @Bean
> > >     public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
> > >         return configuration.getAuthenticationManager();
> > >     }
> > >
> > >     @Bean
> > >     public PasswordEncoder passwordEncoder() {
> > >         return new BCryptPasswordEncoder();
> > >     }
> > > }
> > > ```
> >
> > </details>
> >
> > Las reglas se leen **de arriba abajo y gana la primera que coincide**:
> >
> > | Regla | Efecto |
> > |---|---|
> > | `permitAll` en `/api/v1/auth/login`, `/internal/health` y Swagger | Públicas |
> > | `POST /api/v1/courses` → `hasRole("ADMIN")` | Solo `ADMIN` crea cursos |
> > | `/internal/**` → `hasRole("ADMIN")` | Solo `ADMIN` carga datos de ejemplo |
> > | `anyRequest().authenticated()` | Todo lo demás: cualquier cuenta con token válido |
> >
> > > ⚠️ **El orden importa.** `/internal/health` va **antes** que `/internal/**`; si se invirtieran, `health` pediría ser `ADMIN`.
> >
> > También se declaran `PasswordEncoder` (BCrypt) y `AuthenticationManager`, y la sesión es `STATELESS`.
>
> </details>
>
> <details>
> <summary><strong>Paso 4.5 · Ver el resultado</strong></summary>
>
> > ```bash
> > curl -i localhost:8080/api/v1/courses
> > ```
> > ```text
> > HTTP/1.1 401
> > WWW-Authenticate: Bearer
> > {"timestamp":"2026-11-07T21:15:03.164Z","status":401,"error":"Unauthorized","message":"Se requiere autenticación para acceder a este recurso","path":"/api/v1/courses"}
> > ```
> >
> > ```bash
> > curl -s -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"x"}'
> > ```
> > ```json
> > {"timestamp":"2026-11-07T21:15:03.238Z","status":401,"error":"Unauthorized","message":"Usuario o contraseña incorrectos","path":"/api/v1/auth/login"}
> > ```
> >
> > > ✅ **Punto de control 4.** Sin token, `401` con `WWW-Authenticate: Bearer`; con credenciales incorrectas, `401` con un mensaje que no dice cuál de los dos datos falló.
>
> </details>

</details>

---

<details>
<summary><strong>📚 Paso 5 — Swagger: que *Authorize* funcione</strong></summary>

> > 🗣️ **Para abrir el paso.** La seguridad ya funciona, pero el contrato OpenAPI no lo sabe: Swagger UI no ofrece **Authorize** ni documenta `401`/`403`. En la academia hay más de diez operaciones protegidas: repetir `@ApiResponse(401)` en cada una sería ruido; se hace **una vez**.
>
> <details>
> <summary><strong>Paso 5.1 · Seguridad global y respuestas `401`/`403` reutilizables</strong></summary>
>
> > <details>
> > <summary><strong>📄 OpenApiConfig.java (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > > ```java
> > > // Dos respuestas reutilizables nuevas, junto a BadRequest, NotFound y Conflict
> > > .addResponses("Unauthorized", errorResponse("No autenticado: falta el token, no es válido o expiró",
> > >         example(401, "Unauthorized", "Se requiere autenticación para acceder a este recurso", "/api/v1/students", null)))
> > > .addResponses("Forbidden", errorResponse("Autenticado, pero su rol no permite la operación",
> > >         example(403, "Forbidden", "No tiene permisos para realizar esta operación", "/api/v1/courses", null)));
> > >
> > > // En academyOpenApi(): el esquema bearerAuth se aplica a toda operación
> > > .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
> > > ```
> >
> > </details>
>
> </details>
>
> <details>
> <summary><strong>Paso 5.2 · Agregar `401`/`403` a las operaciones, sin repetirlas</strong></summary>
>
> > <details>
> > <summary><strong>📄 OpenApiConfig.java (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > > ```java
> > > /**
> > >  * Agrega 401 a toda operación protegida y 403 a las que exigen el rol ADMIN (ver SecurityConfig).
> > >  * Se agrega a cada grupo: con GroupedOpenApi, los personalizadores globales no se aplican.
> > >  */
> > > private OpenApiCustomizer securityResponses() {
> > >     return openApi -> openApi.getPaths().forEach((path, item) ->
> > >             item.readOperationsMap().forEach((method, operation) -> {
> > >                 boolean isPublic = operation.getSecurity() != null && operation.getSecurity().isEmpty();
> > >                 if (isPublic) {
> > >                     return;
> > >                 }
> > >                 operation.getResponses().addApiResponse("401",
> > >                         new ApiResponse().$ref("#/components/responses/Unauthorized"));
> > >                 boolean adminOnly = path.startsWith("/internal/")
> > >                         || (method == PathItem.HttpMethod.POST && path.equals("/api/v1/courses"));
> > >                 if (adminOnly) {
> > >                     operation.getResponses().addApiResponse("403",
> > >                             new ApiResponse().$ref("#/components/responses/Forbidden"));
> > >                 }
> > >             }));
> > > }
> > >
> > > // y en cada GroupedOpenApi (academy e internal):
> > > .addOpenApiCustomizer(securityResponses())
> > > ```
> >
> > </details>
> >
> > El personalizador agrega `401` a toda operación protegida y `403` solo a las de `ADMIN`. Como el contrato se arma **desde la cadena de seguridad**, ambos listados deben coincidir: si se agrega una regla `ADMIN` en `SecurityConfig`, se agrega también aquí.
>
> </details>
>
> <details>
> <summary><strong>Paso 5.3 · Marcar lo público y lo exclusivo de `ADMIN`</strong></summary>
>
> > <details>
> > <summary><strong>📄 Controladores (cambios)</strong> <em>(solo el cambio)</em></summary>
> >
> > > ```java
> > > // InternalController.java
> > > @Operation(summary = "Estado del servicio", description = "Público: no pide token.")
> > > @SecurityRequirements            // lista vacía = esta operación NO pide el esquema bearerAuth
> > > @GetMapping("/health")
> > > public Map<String, String> health() { ... }
> > >
> > > @Operation(summary = "Cargar datos de ejemplo",
> > >         description = "Crea tres estudiantes (si sus correos no existen) y dos cursos (solo si no hay ninguno). Solo ADMIN.")
> > > @PostMapping("/seed")
> > >
> > > // CourseController.java: antes pedía bearerAuth en la propia operación; ahora lo hace la configuración global
> > > @Operation(operationId = "createCourse", summary = "Crear un curso",
> > >         description = "Solo ADMIN. El precio debe ser mayor que cero y el cupo, al menos 1.")
> > > ```
> >
> > </details>
>
> </details>
>
> <details>
> <summary><strong>Paso 5.4 · Ver el resultado</strong></summary>
>
> > El contrato real (`GET /api-docs/academy`), con los códigos de respuesta por operación:
> >
> > ```text
> > POST /api/v1/auth/login          ['200', '400', '401']          público (sin candado)
> > GET  /api/v1/auth/me             ['200', '401']
> > GET  /api/v1/students            ['200', '400', '401']
> > POST /api/v1/students            ['201', '400', '401', '409']
> > GET  /api/v1/students/{id}       ['200', '400', '401', '404']
> > GET  /api/v1/courses             ['200', '400', '401']
> > POST /api/v1/courses             ['201', '400', '401', '403']   solo ADMIN
> > GET  /api/v1/courses/{id}        ['200', '400', '401', '404']
> > GET  /api/v1/courses/{id}/enrolled ['200', '400', '401', '404']
> > POST /api/v1/enrollments         ['201', '400', '401', '404', '409']
> > GET  /api/v1/enrollments/{id}    ['200', '400', '401', '404']
> > ```
> >
> > > 🖱️ **En Swagger UI** (se deduce de ese contrato): aparece el botón **Authorize**; se hace `POST /api/v1/auth/login`, se copia el valor de `token`, se pega en **Authorize** y las peticiones salen con `Authorization: Bearer …`. Con `persist-authorization=true` el token sobrevive a recargar la página.
>
> </details>

</details>

---

<details>
<summary><strong>🧪 Paso 6 — Prueba de punta a punta</strong></summary>

> <details>
> <summary><strong>Paso 6.1 · Iniciar sesión y mirar el token</strong></summary>
>
> > ```bash
> > curl -s -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
> >   -d '{"username":"ana","password":"ana123"}'
> > ```
> > ```json
> > {"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmEiLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc5NDA4NjEwMywiZXhwIjoxNzk0MDg5NzAzfQ.6yzXjb…","tokenType":"Bearer","expiresIn":3600}
> > ```
> >
> > La parte central (el *payload*) es JSON en Base64; **no está cifrada**, solo firmada, así que no debe llevar secretos:
> >
> > ```bash
> > echo "<la parte central del token>" | base64 -d
> > ```
> > ```json
> > {"sub":"ana","role":"USER","iat":1794086103,"exp":1794089703}
> > ```
> >
> > ```bash
> > TOKEN=...   # el valor de "token"
> > curl -s -H "Authorization: Bearer $TOKEN" localhost:8080/api/v1/auth/me
> > ```
> > ```json
> > {"username":"ana","role":"USER"}
> > ```
>
> </details>
>
> <details>
> <summary><strong>Paso 6.2 · Un `USER` puede leer y matricular, pero no crear cursos</strong></summary>
>
> > ```bash
> > curl -s -H "Authorization: Bearer $TOKEN" "localhost:8080/api/v1/courses?size=2"
> > ```
> > ```json
> > {"content":[{"id":1,"name":"Java desde cero","price":150000.0,"maxCapacity":30},{"id":2,"name":"Spring Boot","price":250000.0,"maxCapacity":20}],"page":0,"size":2,"totalElements":3,"totalPages":2}
> > ```
> >
> > ```bash
> > curl -s -X POST localhost:8080/api/v1/courses -H "Authorization: Bearer $TOKEN" \
> >   -H 'Content-Type: application/json' -d '{"name":"X","price":1,"maxCapacity":1}'
> > ```
> > ```json
> > {"timestamp":"2026-11-07T21:15:03.345Z","status":403,"error":"Forbidden","message":"No tiene permisos para realizar esta operación","path":"/api/v1/courses"}
> > ```
> >
> > ```bash
> > curl -s -X POST localhost:8080/internal/seed -H "Authorization: Bearer $TOKEN"
> > ```
> > ```json
> > {"timestamp":"2026-11-07T21:16:01.029Z","status":403,"error":"Forbidden","message":"No tiene permisos para realizar esta operación","path":"/internal/seed"}
> > ```
>
> </details>
>
> <details>
> <summary><strong>Paso 6.3 · Un `ADMIN` carga datos y crea cursos</strong></summary>
>
> > ```bash
> > ADMIN=$(curl -s -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
> >   -d '{"username":"admin","password":"admin123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')
> > curl -s -X POST localhost:8080/internal/seed -H "Authorization: Bearer $ADMIN"
> > ```
> > ```json
> > {"students":3,"courses":2}
> > ```
> >
> > ```bash
> > curl -si -X POST localhost:8080/api/v1/courses -H "Authorization: Bearer $ADMIN" \
> >   -H 'Content-Type: application/json' -d '{"name":"Docker","price":90000,"maxCapacity":10}'
> > ```
> > ```text
> > HTTP/1.1 201
> > Location: http://localhost:8080/api/v1/courses/3
> > {"id":3,"name":"Docker","price":90000.0,"maxCapacity":10}
> > ```
>
> </details>
>
> <details>
> <summary><strong>Paso 6.4 · Los errores de negocio siguen igual (y con el mismo formato)</strong></summary>
>
> > Seguridad y negocio conviven: cada error conserva su código y su `ErrorResponse`.
> >
> > ```bash
> > # El curso "Un cupo" (id 5) tiene un solo lugar: la segunda matrícula ya no cabe
> > curl -s -X POST localhost:8080/api/v1/enrollments -H "Authorization: Bearer $TOKEN" \
> >   -H 'Content-Type: application/json' -d '{"studentId":1,"courseId":5}'    # 201
> > curl -s -X POST localhost:8080/api/v1/enrollments -H "Authorization: Bearer $TOKEN" \
> >   -H 'Content-Type: application/json' -d '{"studentId":2,"courseId":5}'    # 409
> > ```
> > ```json
> > {"id":2,"studentId":1,"studentName":"Ana Torres","courseId":5,"courseName":"Un cupo"}
> > {"timestamp":"2026-11-07T21:15:03.482Z","status":409,"error":"Conflict","message":"El curso 5 ya alcanzó su cupo de 1","path":"/api/v1/enrollments"}
> > ```
> >
> > ```bash
> > curl -s -X POST localhost:8080/api/v1/enrollments -H "Authorization: Bearer $TOKEN" \
> >   -H 'Content-Type: application/json' -d '{"studentId":1,"courseId":999}'
> > ```
> > ```json
> > {"timestamp":"2026-11-07T21:16:01.117Z","status":404,"error":"Not Found","message":"No existe un curso con id 999","path":"/api/v1/enrollments"}
> > ```
> >
> > ```bash
> > curl -s -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' -d '{}'
> > ```
> > ```json
> > {"timestamp":"2026-11-07T21:16:01.076Z","status":400,"error":"Bad Request","message":"Los datos enviados no son válidos","path":"/api/v1/auth/login","details":[{"field":"password","message":"es obligatoria"},{"field":"username","message":"es obligatorio"}]}
> > ```
>
> </details>
>
> <details>
> <summary><strong>Paso 6.5 · Un token manipulado se rechaza</strong></summary>
>
> > Se cambia el último carácter de la firma (o se envía un texto que no es un JWT):
> >
> > ```bash
> > curl -s -H "Authorization: Bearer ${TOKEN%?}x" localhost:8080/api/v1/courses
> > curl -s -H "Authorization: Bearer abc.def.ghi"  localhost:8080/api/v1/courses
> > ```
> > ```json
> > {"timestamp":"2026-11-07T21:16:01.046Z","status":401,"error":"Unauthorized","message":"El token no es válido","path":"/api/v1/courses"}
> > ```
> >
> > > 🔎 Basta cambiar un carácter para que la firma deje de coincidir: **el servidor no necesita recordar el token, solo recalcular la firma**. Y HTTP Basic ya no sirve: `curl -u admin:admin123 …/courses` responde `401`, porque la cadena solo entiende `Bearer`.
>
> </details>
>
> <details>
> <summary><strong>Paso 6.6 · Matriz completa con el script</strong></summary>
>
> > ```bash
> > ./probar-apis.sh          # o ./probar-apis.sh http://localhost:8080
> > ```
> >
> > | Bloque | Comprobaciones |
> > |---|---|
> > | Públicas | `health` `200` · Swagger `200` · login `200` · clave incorrecta `401` · sin campos `400` |
> > | Sin token | estudiantes, cursos y matrículas `401` · token falso `401` |
> > | Rol `USER` | `me` `200` · estudiantes `200` · crear curso `403` · `seed` `403` |
> > | Rol `ADMIN` | `seed` `200` · crear curso `201` · curso inválido `400` · listar `200` · curso inexistente `404` |
> > | Matrículas | correo repetido `409` · matricular `201` · repetida `409` · curso inexistente `404` · matriculados `200` |
> >
> > ```text
> > Resultado: 23 correctas, 0 fallidas
> > ```
>
> </details>

</details>

---

<details>
<summary><strong>🗺️ Mapa: pieza → qué hace → dónde vive</strong></summary>

> | Pieza | Qué hace | Dónde |
> |---|---|---|
> | `UserAccountEntity` · `Role` | Guarda cuentas con el hash BCrypt y su rol | `persistence.entity` |
> | `JpaUserDetailsService` | Busca la cuenta para que Spring Security valide la contraseña | `security` |
> | `JwtService` | Emite y verifica tokens | `security` |
> | `JwtAuthenticationFilter` | Lee `Authorization: Bearer …` en cada petición | `security` |
> | `SecurityConfig` | Rutas públicas, autenticadas y de `ADMIN`; sin sesión | `security` |
> | `RestAuthenticationEntryPoint` · `RestAccessDeniedHandler` | `401` y `403` con `ErrorResponse` | `security` |
> | `GlobalExceptionHandler` | Convierte el fallo de login en `401` y conserva `404`/`409` | `presentation.exception` |
> | `OpenApiConfig` | *Authorize*, `401` y `403` en el contrato | `presentation.config` |

</details>

<details>
<summary><strong>🏁 Cierre — preguntas de síntesis</strong></summary>

> 1. ¿Qué diferencia hay entre `401` y `403`? *(`401`: no se sabe quién eres; `403`: se sabe, pero tu rol no alcanza.)*
> 2. ¿Por qué el `@RestControllerAdvice` no ve los `401` de un token malo? *(Porque ocurren en un filtro, antes de Spring MVC; por eso existe `SecurityErrorWriter`.)*
> 3. ¿Qué pasa si `/internal/**` se declara antes que `/internal/health`? *(`health` dejaría de ser público: gana la primera regla que coincide.)*
> 4. ¿Por qué cambiar un carácter del token produce `401`? *(La firma se recalcula y ya no coincide.)*
> 5. ¿Qué servicios de la academia hubo que modificar? *(Ninguno: la seguridad vive en `security` y en la configuración.)*
> 6. ¿Por qué el payload del JWT no debe llevar contraseñas? *(Está en Base64, no cifrado: cualquiera puede leerlo.)*

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Qué hacer |
> |---|---|---|
> | Todo responde `401`, incluso el login | `/api/v1/auth/login` no está en `permitAll` | Revisar el Paso 4.4 |
> | `403` en una ruta que debería abrirse | Un `hasRole` aparece **antes** de la regla específica | Ordenar de lo específico a lo general |
> | Un `USER` recibe `500` en vez de `403` | Falta el manejador de `AccessDeniedException` | Revisar el Paso 4.3 |
> | `WeakKeyException` al arrancar | `app.jwt.secret` tiene menos de 32 caracteres | Usar una clave más larga |
> | El login con clave correcta devuelve `401` | La contraseña se guardó sin cifrar | Guardar con `PasswordEncoder.encode` (Paso 2.3) |
> | Los `401` salen sin el formato de la API | El `AuthenticationEntryPoint` no está registrado | Revisar el Paso 4.4 |
> | Swagger no muestra *Authorize* | Falta el `bearerAuth` global en `OpenApiConfig` | Revisar el Paso 5.1 |
> | `POST` con token correcto da `403` | El rol se emitió sin el prefijo `ROLE_` en el filtro | Revisar `JwtAuthenticationFilter` |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **Un tercer rol.** Agregar `TEACHER`, que pueda crear cursos pero no ejecutar `/internal/seed`.
> 2. **Matrículas propias.** Que un `USER` solo pueda matricular a **su** estudiante (relacionar la cuenta con `StudentEntity`).
> 3. **Seguridad por método.** Reemplazar la regla de `POST /courses` por `@PreAuthorize("hasRole('ADMIN')")` y comparar.
> 4. **Clave por entorno.** Arrancar con `JWT_SECRET` y comprobar que un token emitido antes deja de valer.
> 5. **Probar el filtro.** Escribir una prueba con `MockMvc` y `spring-security-test` que verifique el `403` de un `USER`.

</details>

---

➡️ **Anterior:** [Guía 05-02 — Academia con manejo de excepciones](../modulo-05-excepciones/05-02-academica-excepciones.md) · **Versión de un solo recurso:** [06-01](06-01-guia-crud-estudiantes-con-security.md) · **Código completo:** [06-06-academica-security-codigo-completo.md](06-06-academica-security-codigo-completo.md)
