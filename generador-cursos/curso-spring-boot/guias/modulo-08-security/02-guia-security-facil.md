# 🔐 Guía fácil — Proteger la API con usuario, contraseña y token

> 👩‍🏫 **Versión sencilla y práctica.** Se parte de la API de estudiantes del [Módulo 07](../modulo-07-excepciones/01-guia-crud-estudiantes-con-excepciones.md) y se le agrega **login con token (JWT)**, en pasos cortos. Cada paso dice **qué se hace**, trae el **código** y termina con una **prueba** para comprobar que funciona.
> Si se quiere la explicación completa (arquitectura de Spring Security, HTTP Basic, Swagger a fondo), está la [guía completa](01-guia-crud-estudiantes-con-security.md).
>
> 📦 **Proyecto terminado:** [`students-api-security-facil/`](codigo/students-api-security-facil/) (se ejecuta con `./mvnw spring-boot:run`).
>
> ✅ **Verificada.** El código se compiló y se ejecutó con Spring Boot 3.3.5, Java 17 y MySQL 8. Las respuestas que se muestran son reales; lo que se cuenta de la pantalla de Swagger UI (Paso 7) se deduce del contrato.

<details>
<summary><strong>🎯 Qué se va a lograr</strong></summary>

> Hoy la API deja entrar a cualquiera. Al terminar:
>
> | Quién pide | Qué pasa |
> |---|---|
> | Alguien **sin token** | `401` — «falta el token» |
> | Alguien con **token**, usuario normal (`USER`) | Puede **ver y crear** estudiantes |
> | Alguien con token de **administrador** (`ADMIN`) | Además puede **borrar** estudiantes |
> | Un `USER` que intenta borrar | `403` — «no tiene permisos» |
> | Alguien con **usuario o contraseña mal** | `401` — «usuario o contraseña incorrectos» |
>
> Y todos los errores salen con el **mismo formato** (`ErrorResponse`) que ya se hizo en el Módulo 07.

</details>

<details>
<summary><strong>💡 Las ideas, en un minuto</strong></summary>

> | Idea | Se parece a… | En la API |
> |---|---|---|
> | **Login** | Mostrar el carnet en la portería | `POST /api/v1/auth/login` con usuario y contraseña |
> | **Token (JWT)** | El **pase** que da la portería: se muestra en cada puerta, y no hay que volver a mostrar el carnet | Un texto largo que se envía en cada petición |
> | **Rol** | Lo que abre tu pase: unos entran a más puertas que otros | `ADMIN` o `USER` |
> | **401** | «No sé quién eres» (falta el pase o no sirve) | Sin token, token falso o vencido |
> | **403** | «Sé quién eres, pero esta puerta no es tuya» | `USER` intentando borrar |
> | **BCrypt** | Una trituradora: la contraseña entra y sale hecha tiras; **no se puede rearmar**, solo comprobar si coincide | Así se guarda la contraseña en la base |
>
> ```text
> 1) Cliente ── usuario + contraseña ──▶ /login ──▶ recibe un TOKEN
> 2) Cliente ── "Authorization: Bearer TOKEN" ──▶ cualquier otra ruta ──▶ el filtro revisa el token y deja pasar
> ```
>
> > ⚠️ **401 y 403 no son lo mismo.** Aunque `401` se llame *Unauthorized*, significa «**no identificado**». «Sin permiso» es `403`.

</details>

<details>
<summary><strong>🧱 Qué se va a crear</strong></summary>

> | Archivo | Para qué sirve (en una frase) | Paso |
> |---|---|:-:|
> | `pom.xml` y `application.properties` | Traer las librerías y guardar la clave del token | 1 |
> | `Role`, `UserAccountEntity`, `UserAccountJpaRepository` | Guardar los usuarios en la base | 2 |
> | `PasswordConfig` | Cifrar contraseñas con BCrypt | 2 |
> | `AccountSeeder` | Crear dos usuarios de práctica al arrancar | 2 |
> | `JwtService` | **Crear** el token y **leerlo** | 3 |
> | `AuthService`, `LoginRequest`, `LoginResponse`, `AuthController` | El login | 4 |
> | `SecurityConfig` | Decir qué rutas son abiertas, cuáles piden token y cuáles son solo de `ADMIN` | 4 y 5 |
> | `JwtAuthenticationFilter` | Revisar el token en cada petición | 5 |
> | `SecurityErrorHandler` y un manejador en `GlobalExceptionHandler` | Mensajes de error claros | 6 |
> | `OpenApiConfig` (3 líneas) | Que el botón **Authorize** de Swagger sirva | 7 |
>
> **Cuentas de práctica** (solo para fines educativos): `admin` / `admin123` (rol `ADMIN`) y `ana` / `ana123` (rol `USER`).
>
> > 🧠 **Lo que NO cambia:** `StudentService`, `StudentController` y la base de estudiantes. La seguridad se agrega **alrededor**, sin tocar el negocio.

</details>

<details>
<summary><strong>🧩 Qué hace cada clase (con un edificio)</strong></summary>

> Para entender las clases conviene imaginar un edificio con portería:
>
> | En el edificio | En la API |
> |---|---|
> | Mostrar el carnet en la portería | **Login** (usuario y contraseña) |
> | El pase que da la portería | **El token (JWT)** |
> | El guardia que revisa el pase en cada puerta | **El filtro** |
> | Las reglas «aquí entran todos, aquí solo el administrador» | **`SecurityConfig`** |
>
> ```text
> 1) LOGIN:   usuario + contraseña ──▶ AuthService ──▶ JwtService crea el TOKEN ──▶ el cliente lo guarda
> 2) DESPUÉS: cualquier petición + TOKEN ──▶ JwtAuthenticationFilter lo revisa ──▶ SecurityConfig decide si pasa
> ```
>
> <details>
> <summary><strong>🧱 Las clases que guardan a las personas</strong></summary>
>
> > | Clase | Qué es, en simple |
> > |---|---|
> > | **`Role`** | Una lista con dos palabras: `ADMIN` y `USER`. Es la etiqueta del pase: «administrador» o «usuario normal». |
> > | **`UserAccountEntity`** | **La ficha de una persona** en la base: su nombre de usuario, su rol y su contraseña. Es una entidad más, como `StudentEntity`. |
> > | **`UserAccountJpaRepository`** | **El que busca fichas.** Solo sabe hacer una pregunta: «¿hay alguien con este nombre de usuario?» (`findByUsername`). |
> >
> > > ⚠️ **La contraseña nunca se guarda tal cual.** Se guarda cifrada (`$2a$10$TJq93…`). Si alguien robara la base, no vería `admin123`.
>
> </details>
>
> <details>
> <summary><strong>🔒 Las clases de la carpeta <code>security</code></strong></summary>
>
> > <details>
> > <summary><strong>1️⃣ PasswordConfig — la trituradora de contraseñas</strong></summary>
> >
> > Entrega a toda la aplicación **la trituradora de contraseñas** (BCrypt).
> >
> > - Al **guardar**: «admin123» entra y sale como una tira de símbolos.
> > - Al **comparar** (en el login): se tritura lo que escribió la persona y se mira si da **la misma tira**.
> > - **No se puede deshacer**: de la tira no se vuelve a «admin123». Solo se puede comparar.
> >
> > </details>
> >
> > <details>
> > <summary><strong>2️⃣ AccountSeeder — el que da de alta a los primeros usuarios</strong></summary>
> >
> > **Da de alta a los dos primeros usuarios** (`admin` y `ana`) cada vez que arranca la aplicación, si todavía no existen. Sin él, la tabla estaría vacía y nadie podría iniciar sesión.
> >
> > > ⚠️ Es solo para practicar: en un proyecto real no se escriben contraseñas en el código.
> >
> > </details>
> >
> > <details>
> > <summary><strong>3️⃣ JwtService — la máquina que fabrica y revisa los pases</strong></summary>
> >
> > | Método | Qué hace |
> > |---|---|
> > | `create` | Fabrica el token con **quién eres, tu rol y hasta cuándo vale**, y lo **firma** con la clave secreta |
> > | `read` | Revisa que la firma y la fecha estén bien. Si alguien **cambió una letra** o el token **venció**, avisa con un error |
> >
> > La **clave secreta** es como el sello de la portería: solo ella puede estamparlo, así que un pase falsificado se nota.
> >
> > > 💡 Dentro del token hay texto que cualquiera puede leer. **No se pone nada secreto ahí.** La firma no lo oculta; solo garantiza que **nadie lo modificó**.
> >
> > </details>
> >
> > <details>
> > <summary><strong>4️⃣ AuthService — el empleado del mostrador del login</strong></summary>
> >
> > Hace tres preguntas en orden:
> >
> > 1. ¿Existe ese usuario? Si no, error.
> > 2. ¿La contraseña (triturada) coincide con la guardada? Si no, error.
> > 3. Si todo está bien, le pide a `JwtService` un token y lo entrega.
> >
> > En los dos errores dice **lo mismo** («usuario o contraseña incorrectos»): no revela cuál falló, para no ayudar a quien prueba cuentas ajenas.
> >
> > </details>
> >
> > <details>
> > <summary><strong>5️⃣ JwtAuthenticationFilter (el filtro) — el guardia de cada puerta</strong></summary>
> >
> > Se ejecuta **en cada petición**, antes de que llegue al código de la API:
> >
> > 1. Mira si la petición trae `Authorization: Bearer <token>`.
> > 2. Si lo trae, le pide a `JwtService` que lo revise.
> > 3. Si el token es bueno, **anota «esta persona es ana, rol USER»** y la deja seguir.
> > 4. Si el token es malo (falso, alterado o vencido) o **no hay token**, **no anota a nadie**.
> >
> > El guardia **no decide** si se rechaza: solo anota quién es. Quien rechaza es `SecurityConfig`.
> >
> > > ⚠️ Esta clase **no lleva `@Component`** a propósito. Si lo llevara, Spring pondría **dos guardias** en la misma puerta.
> >
> > </details>
> >
> > <details>
> > <summary><strong>6️⃣ SecurityConfig — el reglamento del edificio</strong></summary>
> >
> > Es la clase más importante. Dice:
> >
> > | Regla | Quién entra |
> > |---|---|
> > | `permitAll()` | **Todos**: el login, `/internal/health` y Swagger |
> > | `hasRole("ADMIN")` | **Solo administradores**: borrar estudiantes y `/internal/**` |
> > | `anyRequest().authenticated()` | **Todo lo demás**: cualquiera **con pase** |
> >
> > Además configura: **sin sesiones** (`STATELESS`: cada petición trae su pase), **sin CSRF** (esa defensa es para páginas con cookies), **pone al guardia** (`addFilterBefore`) y **dice a quién avisar cuando hay un error** (`exceptionHandling`).
> >
> > > 🔎 **El orden importa.** Las reglas se leen de arriba hacia abajo y gana **la primera** que coincide. Por eso `anyRequest()` va siempre al final.
> >
> > </details>
> >
> > <details>
> > <summary><strong>7️⃣ SecurityErrorHandler — quien explica por qué no se puede pasar</strong></summary>
> >
> > Responde con el mismo formato de error de toda la API (`ErrorResponse`):
> >
> > | Situación | Código | Mensaje |
> > |---|:-:|---|
> > | **No hay pase**, o es falso o venció | `401` | «Falta el token o no es válido» |
> > | **Hay pase bueno**, pero no alcanza (un `USER` quiere borrar) | `403` | «No tiene permisos para realizar esta operación» |
> >
> > Existe porque estos errores ocurren **en la puerta**, antes de llegar a los controladores. Por eso el `GlobalExceptionHandler` de siempre **no los ve** y hace falta una clase aparte.
> >
> > </details>
>
> </details>
>
> <details>
> <summary><strong>🚪 Las clases que ve el cliente</strong></summary>
>
> > | Clase | Qué es, en simple |
> > |---|---|
> > | **`AuthController`** | **La ventanilla del login**: `POST /api/v1/auth/login`. Recibe usuario y contraseña, se los pasa a `AuthService` y devuelve el token. No decide nada: solo recibe y entrega. |
> > | **`LoginRequest`** | El formulario que se **envía**: `username` y `password`, ambos obligatorios. |
> > | **`LoginResponse`** | El formulario que se **recibe**: `token`, el tipo `Bearer` y cuántos segundos dura. |
> > | **`GlobalExceptionHandler`** (el método nuevo) | Cuando la contraseña está mal, `AuthService` **lanza un error dentro de un controlador**. Este método lo atrapa y lo convierte en un `401` con mensaje. Sin él, ese error terminaba como `500`, como si el servidor se hubiera roto. |
>
> </details>
>
> <details>
> <summary><strong>🧠 Para recordar</strong></summary>
>
> > | | `401` | `403` |
> > |---|---|---|
> > | Significa | «**No sé quién eres**» | «**Sé quién eres, pero no puedes**» |
> > | Ejemplo | Sin token, token falso o vencido | `ana` (`USER`) intenta borrar |
> >
> > Y una regla para todo: **`StudentService` no se tocó nunca**. La seguridad se pone **alrededor** del negocio, como una portería alrededor de un edificio: por dentro, el edificio sigue igual.
>
> </details>

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Preparar el proyecto</strong></summary>

> **🎯 Qué se hace.** Copiar el proyecto del Módulo 07 con otro nombre y levantar MySQL.
>
> 1. Copiar la carpeta [`students-api-excepciones`](../modulo-07-excepciones/codigo/students-api-excepciones/) como `students-api-security-facil`.
> 2. En el `pom.xml`, cambiar el `artifactId` a `students-api-security-facil`.
> 3. Levantar la base:
>
> ```bash
> docker run -d --name academy-mysql \
>   -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academy \
>   -p 3306:3306 mysql:8.0
> ```
>
> ▶️ **Probar.**
>
> ```bash
> ./mvnw spring-boot:run
> curl -X POST localhost:8080/internal/seed          # {"created":3}
> curl localhost:8080/api/v1/students/1              # responde sin pedir nada
> ```
>
> ✅ **Listo si** la API responde **sin pedir nada**. Eso es justo lo que se va a arreglar.

</details>

---

<details>
<summary><strong>📦 Paso 1 — Agregar las librerías y la clave</strong></summary>

> **🎯 Qué se hace.** Agregar **Spring Security** (para proteger) y **JJWT** (para crear y leer tokens). Y guardar la **clave secreta** con la que se firman los tokens.
>
> <details>
> <summary><strong>📄 pom.xml (agregar dentro de &lt;dependencies&gt;)</strong></summary>
>
> ```xml
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-security</artifactId>
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
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 application.properties (agregar)</strong></summary>
>
> ```properties
> # Token JWT -- SOLO PARA FINES EDUCATIVOS: en un proyecto real la clave NO se escribe aquí.
> # Se entrega con la variable de entorno JWT_SECRET (32 caracteres o más).
> app.jwt.secret=${JWT_SECRET:clave-solo-educativa-cambiar-en-un-proyecto-real-0123456789}
> app.jwt.expiration-seconds=3600
> ```
>
> </details>
>
> > ⚠️ **La clave del ejemplo es solo para aprender.** Quien la conozca puede fabricar tokens válidos. En un proyecto real se pasa con la variable `JWT_SECRET` y debe tener **32 caracteres o más**.
>
> ▶️ **Probar.** Reiniciar y pedir estudiantes:
>
> ```bash
> curl -i localhost:8080/api/v1/students
> ```
>
> ```text
> HTTP/1.1 401
> WWW-Authenticate: Basic realm="Realm"
> ```
>
> ✅ **Listo si** responde `401`. Con **solo agregar la librería**, Spring Security ya **cierra todas las puertas**. Hasta Swagger y `/internal/health` quedan cerradas. Ahora hay que decirle quién puede entrar.
>
> > 💡 En el *log* aparece `Using generated security password: ...`. **Se puede ignorar**: es el usuario de prueba que Spring crea por defecto y que esta guía no usa.

</details>

---

<details>
<summary><strong>👥 Paso 2 — Guardar usuarios con la contraseña cifrada</strong></summary>

> **🎯 Qué se hace.** Crear la tabla de usuarios (una entidad más, como `StudentEntity`) y dos usuarios de práctica. La contraseña **nunca** se guarda tal cual: se guarda cifrada con **BCrypt**.
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
>     // Aquí se guarda el hash (la contraseña cifrada), nunca la contraseña real
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
> <details>
> <summary><strong>📄 PasswordConfig.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/PasswordConfig.java`
>
> ```java
> package com.academy.security;
>
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
> import org.springframework.security.crypto.password.PasswordEncoder;
>
> @Configuration
> public class PasswordConfig {
>
>     /** BCrypt cifra la contraseña en un solo sentido: no se puede "descifrar", solo comparar. */
>     @Bean
>     public PasswordEncoder passwordEncoder() {
>         return new BCryptPasswordEncoder();
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
>  * Crea dos usuarios de práctica al arrancar.
>  * SOLO PARA FINES EDUCATIVOS: en un proyecto real no se escriben contraseñas en el código.
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
> ▶️ **Probar.** Reiniciar y mirar la tabla:
>
> ```bash
> docker exec academy-mysql mysql -uroot -proot academy -e "select id, username, password_hash, role from user_account"
> ```
>
> ```text
> id  username  password_hash                                                 role
> 1   admin     $2a$10$TJq93VKHCUxUnl/m8v4yMOel.Ezv7pXAEkQuybOnwmhMHUnsw8WMO  ADMIN
> 2   ana       $2a$10$Nb4vOF0xRF0lo8YwJLmKHeOJe/dA4YfO5G.boT6eHeR.RK9FaNgSO  USER
> ```
>
> ✅ **Listo si** se ven los dos usuarios y la contraseña es una **tira de símbolos** (`$2a$10$…`), no `admin123`.

</details>

---

<details>
<summary><strong>🎟️ Paso 3 — Crear y leer el token</strong></summary>

> **🎯 Qué se hace.** Escribir **una sola clase** que sabe hacer dos cosas: **crear** un token (firmado con la clave) y **leerlo** (revisando que la firma y la fecha estén bien).
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
> import org.springframework.beans.factory.annotation.Value;
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
>     public JwtService(@Value("${app.jwt.secret}") String secret,
>                       @Value("${app.jwt.expiration-seconds}") long expirationSeconds) {
>         this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
>         this.expirationSeconds = expirationSeconds;
>     }
>
>     /** Crea un token con el usuario y su rol, firmado con la clave secreta. */
>     public String create(String username, String role) {
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
>     /** Revisa la firma y la fecha. Si algo falla, lanza una JwtException. */
>     public Claims read(String token) {
>         return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
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
> | Método | Qué hace |
> |---|---|
> | `create` | Mete en el token **quién es** (`subject`), su **rol** y **hasta cuándo** vale (`exp`), y lo **firma** |
> | `read` | Comprueba la firma y la fecha. Si el token fue **cambiado** o **venció**, lanza una excepción |
>
> ▶️ **Probar.** Todavía no se puede probar sola. Se prueba en el paso siguiente, con el login.
>
> ✅ **Listo si** compila (`./mvnw compile`).

</details>

---

<details>
<summary><strong>🔑 Paso 4 — El login</strong></summary>

> **🎯 Qué se hace.** Crear la ruta `POST /api/v1/auth/login`: recibe usuario y contraseña y, si están bien, devuelve un token.
>
> <details>
> <summary><strong>📄 AuthService.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/AuthService.java`
>
> ```java
> package com.academy.security;
>
> import com.academy.persistence.entity.UserAccountEntity;
> import com.academy.persistence.repository.UserAccountJpaRepository;
> import org.springframework.security.authentication.BadCredentialsException;
> import org.springframework.security.crypto.password.PasswordEncoder;
> import org.springframework.stereotype.Service;
>
> @Service
> public class AuthService {
>
>     private final UserAccountJpaRepository accounts;
>     private final PasswordEncoder encoder;
>     private final JwtService jwtService;
>
>     public AuthService(UserAccountJpaRepository accounts, PasswordEncoder encoder, JwtService jwtService) {
>         this.accounts = accounts;
>         this.encoder = encoder;
>         this.jwtService = jwtService;
>     }
>
>     /** Si el usuario y la contraseña son correctos, devuelve un token. Si no, lanza BadCredentialsException. */
>     public String login(String username, String password) {
>         UserAccountEntity account = accounts.findByUsername(username)
>                 .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));
>         if (!encoder.matches(password, account.getPasswordHash())) {
>             throw new BadCredentialsException("Usuario o contraseña incorrectos");
>         }
>         return jwtService.create(account.getUsername(), account.getRole().name());
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
> <summary><strong>📄 LoginRequest.java y LoginResponse.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/dto/`
>
> ```java
> @Schema(description = "Usuario y contraseña para iniciar sesión")
> public record LoginRequest(
>         @Schema(description = "Nombre de usuario", example = "admin")
>         @NotBlank(message = "es obligatorio")
>         String username,
>
>         @Schema(description = "Contraseña", example = "admin123")
>         @NotBlank(message = "es obligatoria")
>         String password
> ) {}
>
> @Schema(description = "El token que se envía en cada petición")
> public record LoginResponse(String token, String tokenType, long expiresIn) {}
> ```
>
> (En el código final cada `record` está en su archivo y con el `@Schema` de cada campo.)
>
> </details>
>
> <details>
> <summary><strong>📄 AuthController.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/controller/AuthController.java`
>
> ```java
> @RestController
> @RequestMapping("/api/v1/auth")
> public class AuthController {
>
>     private final AuthService service;
>
>     public AuthController(AuthService service) {
>         this.service = service;
>     }
>
>     @PostMapping("/login")
>     public LoginResponse login(@Valid @RequestBody LoginRequest request) {
>         String token = service.login(request.username(), request.password());
>         return new LoginResponse(token, "Bearer", service.expirationSeconds());
>     }
> }
> ```
>
> (El archivo final lleva además las anotaciones de Swagger `@Tag` y `@Operation`.)
>
> </details>
>
> <details>
> <summary><strong>📄 SecurityConfig.java (primera versión)</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/SecurityConfig.java`
>
> Hay que **abrir la puerta del login** (si no, nadie podría iniciar sesión):
>
> ```java
> package com.academy.security;
>
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
> import org.springframework.security.config.http.SessionCreationPolicy;
> import org.springframework.security.web.SecurityFilterChain;
>
> @Configuration
> @EnableWebSecurity
> public class SecurityConfig {
>
>     @Bean
>     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
>         http
>                 .csrf(csrf -> csrf.disable())
>                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
>                 .authorizeHttpRequests(auth -> auth
>                         .requestMatchers("/api/v1/auth/login", "/internal/health").permitAll()
>                         .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll()
>                         .anyRequest().authenticated());
>         return http.build();
>     }
> }
> ```
>
> </details>
>
> | Línea | En palabras simples |
> |---|---|
> | `csrf.disable()` | Esta API no usa cookies de sesión, así que esa protección no hace falta |
> | `STATELESS` | El servidor **no guarda sesiones**: cada petición trae su token |
> | `permitAll()` | Puertas **abiertas** para todos |
> | `anyRequest().authenticated()` | **Todo lo demás** pide estar identificado |
>
> ▶️ **Probar.**
>
> ```bash
> curl -i -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
>   -d '{"username":"ana","password":"ana123"}'
> ```
>
> ```text
> HTTP/1.1 200
> {"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmEiLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc5MzU4Mzk2OSwiZXhwIjoxNzkzNTg3NTY5fQ.VxNfUNg5Ia1boiOQD_Vf5OiUkT8WYF0yhsCKSxMp0os","tokenType":"Bearer","expiresIn":3600}
> ```
>
> **¡Ese es el pase!** Tiene **tres partes separadas por puntos**. Las dos primeras son texto (Base64, **no está secreto**) y se pueden leer:
>
> | Parte | Qué dice |
> |---|---|
> | 1.ª | `{"alg":"HS256"}` — cómo se firmó |
> | 2.ª | `{"sub":"ana","role":"USER","iat":1793583969,"exp":1793587569}` — quién es, su rol, cuándo se creó y cuándo vence (1 hora después) |
> | 3.ª | La **firma**: prueba de que nadie cambió el contenido |
>
> > ⚠️ **No se pone nada secreto dentro del token**: cualquiera puede leerlo. La firma solo asegura que **no se modificó**.
>
> Ahora se prueba usar el token… y **todavía no funciona**:
>
> ```bash
> curl -i -H "Authorization: Bearer $TOKEN" localhost:8080/api/v1/students
> ```
>
> ```text
> HTTP/1.1 403
> ```
>
> Es normal: **nadie está leyendo el token todavía**. Eso se hace en el Paso 5.
>
> También se prueba una contraseña incorrecta:
>
> ```bash
> curl -i -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
>   -d '{"username":"ana","password":"mala"}'
> ```
>
> ```text
> HTTP/1.1 500
> {"timestamp":"2026-11-02T01:46:09.765Z","status":500,"error":"Internal Server Error","message":"Ocurrió un error inesperado. Intentar de nuevo más tarde.","path":"/api/v1/auth/login"}
> ```
>
> Debería ser `401`, no `500`. Se arregla en el Paso 6.
>
> ✅ **Listo si** el login con la contraseña correcta devuelve un token.

</details>

---

<details>
<summary><strong>🛂 Paso 5 — El filtro que lee el token y las reglas por rol</strong></summary>

> **🎯 Qué se hace.** Crear el **filtro** que, en cada petición, mira si viene un token bueno y, si es así, **deja entrar** al usuario. Y definir qué rutas son solo de `ADMIN`.
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
> /** Se ejecuta en cada petición: si trae un token bueno, deja entrar al usuario. */
> public class JwtAuthenticationFilter extends OncePerRequestFilter {
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
>         if (header != null && header.startsWith("Bearer ")) {
>             try {
>                 Claims claims = jwtService.read(header.substring(7));
>                 var user = new UsernamePasswordAuthenticationToken(
>                         claims.getSubject(), null,
>                         List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class))));
>                 SecurityContextHolder.getContext().setAuthentication(user);
>             } catch (JwtException | IllegalArgumentException ex) {
>                 // Token falso, mal escrito o vencido: no se deja entrar a nadie
>                 SecurityContextHolder.clearContext();
>             }
>         }
>         chain.doFilter(request, response);
>     }
> }
> ```
>
> </details>
>
> | Qué hace el filtro | Por qué |
> |---|---|
> | Lee la cabecera `Authorization: Bearer <token>` | Es la forma estándar de enviar un token |
> | Si el token sirve, **registra al usuario y su rol** | Desde ahí, Spring sabe quién es |
> | Si no sirve, **no deja entrar a nadie** y sigue | Quien responde el error es la configuración (Paso 6) |
>
> > ⚠️ **Esta clase no lleva `@Component`.** Si lo llevara, Spring la ejecutaría **dos veces** por petición. Se crea con `new` en `SecurityConfig`.
>
> <details>
> <summary><strong>📄 SecurityConfig.java (agregar el filtro y las reglas de ADMIN)</strong></summary>
>
> Se cambia la firma del método (ahora recibe `JwtService`) y se agregan **tres cosas**: las reglas de `ADMIN`, el filtro y los `import` nuevos.
>
> ```java
>     @Bean
>     public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
>         http
>                 .csrf(csrf -> csrf.disable())
>                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
>                 .authorizeHttpRequests(auth -> auth
>                         // Abiertas para todos
>                         .requestMatchers("/api/v1/auth/login", "/internal/health").permitAll()
>                         .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll()
>                         // Solo ADMIN
>                         .requestMatchers(HttpMethod.DELETE, "/api/v1/students/**").hasRole("ADMIN")
>                         .requestMatchers("/internal/**").hasRole("ADMIN")
>                         // Todo lo demás: cualquier usuario con token
>                         .anyRequest().authenticated())
>                 .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
>         return http.build();
>     }
> ```
>
> Imports nuevos: `org.springframework.http.HttpMethod` y `org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter`.
>
> </details>
>
> > 🔎 **El orden importa.** Spring lee las reglas **de arriba hacia abajo** y se queda con la **primera** que coincide. `anyRequest()` va **siempre al final**. Y `hasRole("ADMIN")` se escribe **sin** el `ROLE_`: Spring lo agrega solo.
>
> ▶️ **Probar.** Con los dos tokens (`ana` = `USER`, `admin` = `ADMIN`):
>
> ```bash
> curl -i -H "Authorization: Bearer $ANA"   localhost:8080/api/v1/students              # ana mira
> curl -i -H "Authorization: Bearer $ANA"   -X DELETE localhost:8080/api/v1/students/3  # ana borra
> curl -i -H "Authorization: Bearer $ADMIN" -X POST localhost:8080/internal/seed        # admin
> curl -i localhost:8080/api/v1/students                                                # sin token
> curl -i -H "Authorization: Bearer ${ADMIN%?}X" localhost:8080/api/v1/students          # token con 1 letra cambiada
> ```
>
> | # | Quién | Pide | Respuesta |
> |:-:|---|---|---|
> | 1 | `ana` | Ver estudiantes | **`200`** ✅ |
> | 2 | `ana` (`USER`) | Borrar | **`403`** (sin cuerpo todavía) |
> | 3 | `admin` | `POST /internal/seed` | **`200`** · `{"created":3}` ✅ |
> | 4 | nadie | Ver estudiantes **sin token** | `403` (sin cuerpo; debería ser `401`) |
> | 5 | token **alterado** | Ver estudiantes | `403` (sin cuerpo; debería ser `401`) |
>
> ✅ **Listo si** las filas 1 y 3 dan `200` y la 2 da `403`.
>
> > 🧠 **La fila 5 es la gracia del token.** Cambiar **una sola letra** rompe la firma y el servidor lo detecta **sin consultar la base**.
>
> Quedan tres detalles por mejorar: los `403` no tienen mensaje y las filas 4 y 5 deberían ser `401`. Eso se arregla ahora.

</details>

---

<details>
<summary><strong>💬 Paso 6 — Mensajes de error claros</strong></summary>

> **🎯 Qué se hace.** Que los errores de seguridad salgan con el **mismo formato** (`ErrorResponse`) que el resto de la API. Hay que arreglar **dos lugares**, porque los errores de seguridad pasan **antes** de llegar al controlador:
>
> ```text
> Petición ─▶ [ Filtros de seguridad ] ─▶ [ Controlador ]
>               401 y 403 (token)           login incorrecto
>               → SecurityErrorHandler      → GlobalExceptionHandler
> ```
>
> <details>
> <summary><strong>📄 SecurityErrorHandler.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/SecurityErrorHandler.java`
>
> ```java
> package com.academy.security;
>
> import com.academy.presentation.dto.ErrorResponse;
> import com.fasterxml.jackson.databind.ObjectMapper;
> import jakarta.servlet.http.HttpServletRequest;
> import jakarta.servlet.http.HttpServletResponse;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.MediaType;
> import org.springframework.security.access.AccessDeniedException;
> import org.springframework.security.core.AuthenticationException;
> import org.springframework.security.web.AuthenticationEntryPoint;
> import org.springframework.security.web.access.AccessDeniedHandler;
> import org.springframework.stereotype.Component;
>
> import java.io.IOException;
>
> /**
>  * Responde los errores de seguridad con el mismo ErrorResponse del resto de la API.
>  * - 401: no hay token o no sirve.   - 403: hay token, pero el rol no alcanza.
>  */
> @Component
> public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
>
>     private final ObjectMapper objectMapper;
>
>     public SecurityErrorHandler(ObjectMapper objectMapper) {
>         this.objectMapper = objectMapper;
>     }
>
>     @Override
>     public void commence(HttpServletRequest request, HttpServletResponse response,
>                          AuthenticationException ex) throws IOException {
>         write(request, response, HttpStatus.UNAUTHORIZED,
>                 "Falta el token o no es válido (puede haber vencido)");
>     }
>
>     @Override
>     public void handle(HttpServletRequest request, HttpServletResponse response,
>                        AccessDeniedException ex) throws IOException {
>         write(request, response, HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación");
>     }
>
>     private void write(HttpServletRequest request, HttpServletResponse response,
>                        HttpStatus status, String message) throws IOException {
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
> | Método | Cuándo se usa | Respuesta |
> |---|---|:-:|
> | `commence` | **No** hay token, o no sirve (falso, alterado, vencido) | `401` |
> | `handle` | Hay token bueno, pero el **rol no alcanza** | `403` |
>
> <details>
> <summary><strong>📄 SecurityConfig.java (agregar al final de la cadena)</strong></summary>
>
> La firma ahora recibe también el `SecurityErrorHandler`, y se agrega este bloque **antes** de `.authorizeHttpRequests(...)`:
>
> ```java
>     public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService,
>                                            SecurityErrorHandler errorHandler) throws Exception {
>         http
>                 .csrf(csrf -> csrf.disable())
>                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
>                 .exceptionHandling(errors -> errors
>                         .authenticationEntryPoint(errorHandler)
>                         .accessDeniedHandler(errorHandler))
>                 .authorizeHttpRequests(auth -> auth
>                         ...
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 GlobalExceptionHandler.java (agregar un método)</strong></summary>
>
> El login incorrecto **sí** pasa por el controlador, así que se maneja en el manejador del Módulo 07:
>
> ```java
>     // La contraseña incorrecta del login se lanza dentro de un controlador, así que se maneja aquí.
>     @ExceptionHandler(AuthenticationException.class)
>     public ResponseEntity<Object> handleBadLogin(AuthenticationException ex, WebRequest request) {
>         return build(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", request, null);
>     }
> ```
>
> Import nuevo: `org.springframework.security.core.AuthenticationException`.
>
> </details>
>
> ▶️ **Probar.** Las mismas pruebas de antes, ahora con mensajes:
>
> ```text
> $ curl -i localhost:8080/api/v1/students                          # sin token
> HTTP/1.1 401
> {"timestamp":"2026-11-02T01:46:42.054Z","status":401,"error":"Unauthorized","message":"Falta el token o no es válido (puede haber vencido)","path":"/api/v1/students"}
>
> $ curl -i -H "Authorization: Bearer ${ADMIN%?}X" localhost:8080/api/v1/students   # token alterado
> HTTP/1.1 401
> {"timestamp":"2026-11-02T01:46:42.418Z","status":401,"error":"Unauthorized","message":"Falta el token o no es válido (puede haber vencido)","path":"/api/v1/students"}
>
> $ curl -i -H "Authorization: Bearer $ANA" -X DELETE localhost:8080/api/v1/students/3   # USER borrando
> HTTP/1.1 403
> {"timestamp":"2026-11-02T01:46:42.220Z","status":403,"error":"Forbidden","message":"No tiene permisos para realizar esta operación","path":"/api/v1/students/3"}
>
> $ curl -i -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' -d '{"username":"ana","password":"mala"}'
> HTTP/1.1 401
> {"timestamp":"2026-11-02T01:46:42.508Z","status":401,"error":"Unauthorized","message":"Usuario o contraseña incorrectos","path":"/api/v1/auth/login"}
>
> $ curl -i -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' -d '{"username":"","password":""}'
> HTTP/1.1 400
> {"timestamp":"2026-11-02T01:46:42.528Z","status":400,"error":"Bad Request","message":"Los datos enviados no son válidos","path":"/api/v1/auth/login","details":[{"field":"password","message":"es obligatoria"},{"field":"username","message":"es obligatorio"}]}
> ```
>
> Y los errores del Módulo 07 **siguen igual**: con token de `admin`, `GET /api/v1/students/999` responde `404` con `No existe un estudiante con id 999`.
>
> ✅ **Listo si** el login con contraseña mala da `401` (ya no `500`) y los errores de token dan `401` o `403` **con mensaje**.

</details>

---

<details>
<summary><strong>🧪 Paso 7 (opcional) — Que el botón Authorize de Swagger funcione</strong></summary>

> **🎯 Qué se hace.** Decirle a Swagger que la API **pide token**, para poder probarla desde el navegador.
>
> <details>
> <summary><strong>📄 OpenApiConfig.java (agregar)</strong></summary>
>
> 1. En el bean `OpenAPI`, **antes** de `.components(components())`, agregar:
>
> ```java
>                 .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))   // la API pide token
> ```
>
> Import nuevo: `io.swagger.v3.oas.models.security.SecurityRequirement`.
>
> 2. Agregar un grupo para ver el login en Swagger:
>
> ```java
>     @Bean
>     public GroupedOpenApi authGroup() {
>         return GroupedOpenApi.builder()
>                 .group("auth")
>                 .displayName("Auth")
>                 .pathsToMatch("/api/v1/auth/**")
>                 .build();
>     }
> ```
>
> </details>
>
> ▶️ **Probar en el navegador** (`http://localhost:8080/swagger-ui.html`, que sigue abierta):
>
> 1. Elegir el grupo **Auth**, abrir `POST /api/v1/auth/login` y pulsar **Try it out** con `admin` / `admin123`.
> 2. Copiar el valor de `token`.
> 3. Pulsar **Authorize**, pegar el token (**sin** escribir `Bearer`) y confirmar.
> 4. Elegir el grupo **Students** y probar una operación: ahora funciona. Con el token de `ana`, `DELETE` responde `403`; con el de `admin`, `204`.
>
> ✅ **Listo si** `GET /api/v1/students` en Swagger responde `200` con token y `401` sin él.

</details>

---

<details>
<summary><strong>📋 Resumen en una tabla</strong></summary>

> | Pregunta | Respuesta |
> |---|---|
> | ¿Qué hace el **login**? | Revisa usuario y contraseña y entrega un **token** |
> | ¿Qué es el **token**? | Un texto **firmado** con quién eres, tu rol y cuándo vence |
> | ¿Quién revisa el token? | `JwtAuthenticationFilter`, **en cada petición** |
> | ¿Dónde se dice qué rutas piden token o rol? | En `SecurityConfig` |
> | ¿Cómo se guarda la contraseña? | **Cifrada con BCrypt**, nunca en texto plano |
> | ¿Cuándo es `401`? | No hay token, está alterado o venció |
> | ¿Cuándo es `403`? | Hay token bueno, pero el rol **no alcanza** |
> | ¿Qué cambió en `StudentService`? | **Nada** |
>
> | Ruta | Quién puede |
> |---|---|
> | `POST /api/v1/auth/login`, `GET /internal/health`, Swagger | Cualquiera |
> | `GET`, `POST`, `PUT`, `PATCH` de `/api/v1/students/**` | Cualquier usuario **con token** |
> | `DELETE /api/v1/students/{id}` y `/internal/seed` | Solo **`ADMIN`** |

</details>

<details>
<summary><strong>🩺 Si algo falla</strong></summary>

> | Qué se ve | Qué probar |
> |---|---|
> | Todo responde `401` después del Paso 1 | Es lo esperado hasta configurar `SecurityConfig` (Paso 4) |
> | El login responde `403` | Falta el `permitAll()` de `/api/v1/auth/login` en `SecurityConfig` |
> | Con token bueno sigue dando `403` | Falta `.addFilterBefore(...)` o el filtro no se creó (Paso 5) |
> | Con token de `admin` el `DELETE` da `403` | Se escribió `hasRole("ROLE_ADMIN")`: debe ser `hasRole("ADMIN")` |
> | Contraseña mala da `500` | Falta el método para `AuthenticationException` en `GlobalExceptionHandler` (Paso 6) |
> | `WeakKeyException` al arrancar | La clave tiene menos de 32 caracteres |
> | `Encoded password does not look like BCrypt` | La contraseña se guardó sin cifrar: usar `encoder.encode(...)` |
> | El filtro actúa dos veces | Tiene `@Component`: quitarlo |
> | Después de cambiar de proyecto, los usuarios no existen | La base es distinta: el `AccountSeeder` los crea al arrancar |
> | `Communications link failure` | MySQL no está arrancado (`docker ps`) |

</details>

<details>
<summary><strong>🚀 Retos fáciles</strong></summary>

> 1. **Otro usuario.** Agregar al `AccountSeeder` el usuario `luis` / `luis123` con rol `USER` y probar su login.
> 2. **Un token más corto.** Poner `app.jwt.expiration-seconds=10`, iniciar sesión, esperar 11 segundos y repetir una petición. ¿Qué respuesta da?
> 3. **Mirar el token.** Pegar un token en [jwt.io](https://jwt.io) y leer su contenido. ¿Qué datos ve cualquiera?
> 4. **Otra regla.** Hacer que `PUT /api/v1/students/**` también sea solo de `ADMIN`. ¿Qué línea se agrega y dónde (recordar el orden)?
> 5. **Quién soy.** Crear `GET /api/v1/auth/me` que devuelva el usuario y el rol del token (pista: el controlador puede recibir un `Authentication`).
> 6. **Clave por variable de entorno.** Arrancar con `JWT_SECRET` distinto y comprobar que un token del arranque anterior **deja de valer**.

</details>

<details>
<summary><strong>📁 Estructura final esperada</strong></summary>

> ```text
> 📁 students-api-security-facil
> ├── 📄 pom.xml
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 security
>     │   │   ├── 📄 SecurityConfig.java
>     │   │   ├── 📄 PasswordConfig.java
>     │   │   ├── 📄 JwtService.java
>     │   │   ├── 📄 JwtAuthenticationFilter.java
>     │   │   ├── 📄 AuthService.java
>     │   │   ├── 📄 AccountSeeder.java
>     │   │   └── 📄 SecurityErrorHandler.java
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
>         <version>3.3.5</version>
>         <relativePath/>
>     </parent>
>     <groupId>com.academy</groupId>
>     <artifactId>students-api-security-facil</artifactId>
>     <version>0.0.1-SNAPSHOT</version>
>
>     <properties>
>         <java.version>17</java.version>
>     </properties>
>
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
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
>             <groupId>org.springdoc</groupId>
>             <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
>             <version>2.6.0</version>
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
> # Token JWT -- SOLO PARA FINES EDUCATIVOS: en un proyecto real la clave NO se escribe aquí.
> # Se entrega con la variable de entorno JWT_SECRET (32 caracteres o más).
> app.jwt.secret=${JWT_SECRET:clave-solo-educativa-cambiar-en-un-proyecto-real-0123456789}
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
> springdoc.default-produces-media-type=application/json
> springdoc.default-consumes-media-type=application/json
> # Conserva ErrorResponse: solo lo usan las respuestas reutilizables de components
> springdoc.remove-broken-reference-definitions=false
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
>     // Aquí se guarda el hash (la contraseña cifrada), nunca la contraseña real
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
> <summary><strong>📄 PasswordConfig.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/PasswordConfig.java`
>
> ```java
> package com.academy.security;
>
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
> import org.springframework.security.crypto.password.PasswordEncoder;
>
> @Configuration
> public class PasswordConfig {
>
>     /** BCrypt cifra la contraseña en un solo sentido: no se puede "descifrar", solo comparar. */
>     @Bean
>     public PasswordEncoder passwordEncoder() {
>         return new BCryptPasswordEncoder();
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
>  * Crea dos usuarios de práctica al arrancar.
>  * SOLO PARA FINES EDUCATIVOS: en un proyecto real no se escriben contraseñas en el código.
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
> import org.springframework.beans.factory.annotation.Value;
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
>     public JwtService(@Value("${app.jwt.secret}") String secret,
>                       @Value("${app.jwt.expiration-seconds}") long expirationSeconds) {
>         this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
>         this.expirationSeconds = expirationSeconds;
>     }
>
>     /** Crea un token con el usuario y su rol, firmado con la clave secreta. */
>     public String create(String username, String role) {
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
>     /** Revisa la firma y la fecha. Si algo falla, lanza una JwtException. */
>     public Claims read(String token) {
>         return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
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
> <summary><strong>📄 AuthService.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/AuthService.java`
>
> ```java
> package com.academy.security;
>
> import com.academy.persistence.entity.UserAccountEntity;
> import com.academy.persistence.repository.UserAccountJpaRepository;
> import org.springframework.security.authentication.BadCredentialsException;
> import org.springframework.security.crypto.password.PasswordEncoder;
> import org.springframework.stereotype.Service;
>
> @Service
> public class AuthService {
>
>     private final UserAccountJpaRepository accounts;
>     private final PasswordEncoder encoder;
>     private final JwtService jwtService;
>
>     public AuthService(UserAccountJpaRepository accounts, PasswordEncoder encoder, JwtService jwtService) {
>         this.accounts = accounts;
>         this.encoder = encoder;
>         this.jwtService = jwtService;
>     }
>
>     /** Si el usuario y la contraseña son correctos, devuelve un token. Si no, lanza BadCredentialsException. */
>     public String login(String username, String password) {
>         UserAccountEntity account = accounts.findByUsername(username)
>                 .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));
>         if (!encoder.matches(password, account.getPasswordHash())) {
>             throw new BadCredentialsException("Usuario o contraseña incorrectos");
>         }
>         return jwtService.create(account.getUsername(), account.getRole().name());
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
> <summary><strong>📄 JwtAuthenticationFilter.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/JwtAuthenticationFilter.java`
>
> ```java
> package com.academy.security;
>
> import io.jsonwebtoken.Claims;
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
> /** Se ejecuta en cada petición: si trae un token bueno, deja entrar al usuario. */
> public class JwtAuthenticationFilter extends OncePerRequestFilter {
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
>         if (header != null && header.startsWith("Bearer ")) {
>             try {
>                 Claims claims = jwtService.read(header.substring(7));
>                 var user = new UsernamePasswordAuthenticationToken(
>                         claims.getSubject(), null,
>                         List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class))));
>                 SecurityContextHolder.getContext().setAuthentication(user);
>             } catch (JwtException | IllegalArgumentException ex) {
>                 // Token falso, mal escrito o vencido: no se deja entrar a nadie
>                 SecurityContextHolder.clearContext();
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
> <summary><strong>📄 SecurityErrorHandler.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/SecurityErrorHandler.java`
>
> ```java
> package com.academy.security;
>
> import com.academy.presentation.dto.ErrorResponse;
> import com.fasterxml.jackson.databind.ObjectMapper;
> import jakarta.servlet.http.HttpServletRequest;
> import jakarta.servlet.http.HttpServletResponse;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.MediaType;
> import org.springframework.security.access.AccessDeniedException;
> import org.springframework.security.core.AuthenticationException;
> import org.springframework.security.web.AuthenticationEntryPoint;
> import org.springframework.security.web.access.AccessDeniedHandler;
> import org.springframework.stereotype.Component;
>
> import java.io.IOException;
>
> /**
>  * Responde los errores de seguridad con el mismo ErrorResponse del resto de la API.
>  * - 401: no hay token o no sirve.   - 403: hay token, pero el rol no alcanza.
>  */
> @Component
> public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
>
>     private final ObjectMapper objectMapper;
>
>     public SecurityErrorHandler(ObjectMapper objectMapper) {
>         this.objectMapper = objectMapper;
>     }
>
>     @Override
>     public void commence(HttpServletRequest request, HttpServletResponse response,
>                          AuthenticationException ex) throws IOException {
>         write(request, response, HttpStatus.UNAUTHORIZED,
>                 "Falta el token o no es válido (puede haber vencido)");
>     }
>
>     @Override
>     public void handle(HttpServletRequest request, HttpServletResponse response,
>                        AccessDeniedException ex) throws IOException {
>         write(request, response, HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación");
>     }
>
>     private void write(HttpServletRequest request, HttpServletResponse response,
>                        HttpStatus status, String message) throws IOException {
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
> <summary><strong>📄 SecurityConfig.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/security/SecurityConfig.java`
>
> ```java
> package com.academy.security;
>
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.http.HttpMethod;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
> import org.springframework.security.config.http.SessionCreationPolicy;
> import org.springframework.security.web.SecurityFilterChain;
> import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
>
> @Configuration
> @EnableWebSecurity
> public class SecurityConfig {
>
>     @Bean
>     public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService,
>                                            SecurityErrorHandler errorHandler) throws Exception {
>         http
>                 .csrf(csrf -> csrf.disable())
>                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
>                 .exceptionHandling(errors -> errors
>                         .authenticationEntryPoint(errorHandler)
>                         .accessDeniedHandler(errorHandler))
>                 .authorizeHttpRequests(auth -> auth
>                         // Abiertas para todos
>                         .requestMatchers("/api/v1/auth/login", "/internal/health").permitAll()
>                         .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll()
>                         // Solo ADMIN
>                         .requestMatchers(HttpMethod.DELETE, "/api/v1/students/**").hasRole("ADMIN")
>                         .requestMatchers("/internal/**").hasRole("ADMIN")
>                         // Todo lo demás: cualquier usuario con token
>                         .anyRequest().authenticated())
>                 .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
>         return http.build();
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
> @Schema(description = "Usuario y contraseña para iniciar sesión")
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
> @Schema(description = "El token que se envía en cada petición")
> public record LoginResponse(
>
>         @Schema(description = "El token (JWT)", example = "eyJhbGciOiJIUzI1NiJ9...")
>         String token,
>
>         @Schema(description = "Cómo se envía: `Authorization: Bearer <token>`", example = "Bearer")
>         String tokenType,
>
>         @Schema(description = "Segundos que dura el token", example = "3600")
>         long expiresIn
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
> import org.springframework.data.mapping.PropertyReferenceException;
> import org.springframework.http.HttpHeaders;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.HttpStatusCode;
> import org.springframework.http.ResponseEntity;
> import org.springframework.http.converter.HttpMessageNotReadableException;
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
>     // La contraseña incorrecta del login se lanza dentro de un controlador, así que se maneja aquí.
>     @ExceptionHandler(AuthenticationException.class)
>     public ResponseEntity<Object> handleBadLogin(AuthenticationException ex, WebRequest request) {
>         return build(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", request, null);
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
>                         .description("CRUD de estudiantes protegido con JWT. Para probar: **POST /api/v1/auth/login**, copiar el token y pulsar **Authorize**.")
>                         .contact(new Contact().name("Equipo Academy").email("academy@mail.com"))
>                         .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
>                 .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
>                 .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))   // la API pide token
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
>                         example(409, "Conflict", "El correo ana@mail.com ya está registrado", "/api/v1/students", null)));
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
>     @Bean
>     public GroupedOpenApi studentsGroup() {
>         return GroupedOpenApi.builder()
>                 .group("students")
>                 .displayName("Students")
>                 .pathsToMatch("/api/v1/students/**")
>                 .build();
>     }
>
>     @Bean
>     public GroupedOpenApi authGroup() {
>         return GroupedOpenApi.builder()
>                 .group("auth")
>                 .displayName("Auth")
>                 .pathsToMatch("/api/v1/auth/**")
>                 .build();
>     }
>
>     @Bean
>     public GroupedOpenApi internalGroup() {
>         return GroupedOpenApi.builder()
>                 .group("internal")
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
> import com.academy.security.AuthService;
> import io.swagger.v3.oas.annotations.Operation;
> import io.swagger.v3.oas.annotations.tags.Tag;
> import jakarta.validation.Valid;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.RequestBody;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
>
> @RestController
> @RequestMapping("/api/v1/auth")
> @Tag(name = "Auth", description = "Iniciar sesión")
> public class AuthController {
>
>     private final AuthService service;
>
>     public AuthController(AuthService service) {
>         this.service = service;
>     }
>
>     @Operation(summary = "Iniciar sesión",
>             description = "Devuelve un token. Usuarios de práctica: `admin` / `admin123` (ADMIN) y `ana` / `ana123` (USER).")
>     @PostMapping("/login")
>     public LoginResponse login(@Valid @RequestBody LoginRequest request) {
>         String token = service.login(request.username(), request.password());
>         return new LoginResponse(token, "Bearer", service.expirationSeconds());
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
> import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
>     @Operation(summary = "Eliminar un estudiante", security = @SecurityRequirement(name = "bearerAuth"))
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
>     @Operation(summary = "Estado del servicio")
>     @GetMapping("/health")
>     public Map<String, String> health() {
>         return Map.of("status", "UP");
>     }
>
>     @Operation(summary = "Cargar estudiantes de ejemplo", description = "Crea tres estudiantes si no existen.")
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
