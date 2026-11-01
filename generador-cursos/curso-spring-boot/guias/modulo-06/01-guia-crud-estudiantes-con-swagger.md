# 📘 Guía paso a paso — CRUD de estudiantes con JPA documentado con Swagger (OpenAPI)

> 👩‍🏫 **Material docente.** Proyecto pequeño, del mismo estilo que la [guía 01 del taller de cursos](../modulo-03-04/01-guia-taller-gestion-de-cursos-con-bd.md): un CRUD de **estudiantes** con Spring Data JPA y MySQL, sobre el que se agregan, **una por una**, las funcionalidades de Swagger.
> Contiene código completo: no se entrega a los estudiantes antes de resolver el taller.
>
> 📦 **Proyecto terminado:** [`students-api/`](students-api/) (código final, listo para ejecutar con `./mvnw spring-boot:run`).
>
> ✅ **Verificada.** El código se compiló y se ejecutó con Spring Boot 3.3, springdoc-openapi 2.6.0 y MySQL 8. Las respuestas, el contrato OpenAPI y la lista de operaciones que se muestran son salidas reales. La interfaz de Swagger UI se abrió en un navegador para comprobarla.
>
> 🧭 **Idea central.** El código **ya describe** la API (rutas, métodos, tipos, validaciones). Swagger solo le **agrega lo que el código no puede decir**: para qué sirve cada operación, qué ejemplos usar y qué respuestas esperar. Cada paso agrega **una** funcionalidad y se ve el resultado en la interfaz.

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Construir un CRUD de estudiantes y documentarlo con **OpenAPI** usando **springdoc**, hasta cubrir las funcionalidades principales de Swagger.
>
> Al terminar, el estudiante:
>
> - Distingue **OpenAPI** (el contrato), **Swagger UI** (la pantalla) y **springdoc** (la librería que los une con Spring).
> - Obtiene una documentación **automática** con solo agregar una dependencia.
> - Describe la API con `@OpenAPIDefinition`/`OpenAPI`, `@Tag`, `@Operation` y `@ApiResponse`.
> - Documenta parámetros (`@Parameter`, `@ParameterObject`) y modelos (`@Schema`), y ve cómo **Bean Validation** pasa al contrato.
> - Agrega **ejemplos**, marca operaciones **obsoletas** y **oculta** otras.
> - Declara un **esquema de seguridad** (botón *Authorize*) y separa la API en **grupos**.
> - **Configura** la interfaz, **exporta** el contrato y lo **apaga** donde no debe estar.
> - Prueba cada operación con **Try it out**.

</details>

<details>
<summary><strong>📝 Introducción: qué es qué</strong></summary>

> ### 🧩 Tres nombres que se confunden
>
> | Nombre | Qué es | Dónde se ve |
> |---|---|---|
> | **OpenAPI** | La **especificación**: un documento JSON/YAML que describe la API | `/v3/api-docs` |
> | **Swagger UI** | La **pantalla web** que lee ese documento y permite probar la API | `/swagger-ui.html` |
> | **springdoc-openapi** | La **librería** que lee los controladores de Spring y genera el documento | La dependencia del `pom.xml` |
>
> ```text
> Controladores + modelos + anotaciones ─▶ springdoc ─▶ /v3/api-docs (OpenAPI) ─▶ Swagger UI
>                                                                          └──▶ Postman, generadores de clientes, etc.
> ```
>
> ### 🎓 El proyecto
>
> Un **CRUD de estudiantes** con las tres capas de la guía 01 (`presentation`, `business`, `persistence`), nombres en inglés y MySQL.
>
> | Operación | Ruta | Respuestas |
> |---|---|---|
> | Listar con filtro y paginación | `GET /api/students?name=&page=&size=&sort=` | `200` |
> | Buscar por id | `GET /api/students/{id}` | `200` · `404` |
> | Crear | `POST /api/students` | `201` · `400` · `409` |
> | Actualizar | `PUT /api/students/{id}` | `200` · `400` · `404` · `409` |
> | Eliminar | `DELETE /api/students/{id}` | `204` · `404` |
> | Buscar por correo *(se marcará obsoleta)* | `GET /api/students/by-email?email=` | `200` · `404` |
>
> ### 🛠️ Requerimientos y restricciones
>
> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Java 17 o superior, Spring Boot 3.3 y Maven. |
> | **RNF-02** | MySQL 8 (en Docker o instalado); la base `academy` debe existir. |
> | **RNF-03** | Tres capas. Los **DTO todavía no existen**: el controlador recibe y devuelve la **entidad**. Las anotaciones de Swagger van en `presentation` (controladores y configuración) y, **mientras no haya DTO**, también en la entidad. |
> | **RNF-04** | Todos los nombres del código en **inglés**. Inyección por constructor. |
> | **RNF-05** | Duración: 1 h 30 min. |
>
> > 💡 **Alcance.** Quedan fuera: los **DTO** y mapeadores (cuando existan, la documentación de los modelos se mueve a ellos; reto 9), la autenticación real (el esquema de seguridad solo se **documenta**), el manejo de errores con excepciones propias y la generación de clientes a partir del contrato.

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>

> | Paso | Minutos | Actividad |
> |---|---:|---|
> | 0 | 0 – 10 | Preparación: MySQL, proyecto y `application.properties` |
> | 1 | 10 – 30 | El CRUD **sin** Swagger |
> | 2 | 30 – 38 | Primera documentación automática |
> | 3 | 38 – 43 | Información general de la API |
> | 4 | 43 – 53 | Etiquetas, operaciones y respuestas |
> | 5 | 53 – 58 | Parámetros |
> | 6 | 58 – 66 | Modelos (`@Schema`) y validaciones |
> | 7 | 66 – 71 | Ejemplos |
> | 8 | 71 – 75 | Operaciones obsoletas y ocultas |
> | 9 | 75 – 80 | Esquema de seguridad |
> | 10 | 80 – 85 | Grupos |
> | 11 | 85 – 90 | Configurar la interfaz, exportar y apagar |
>
> > 💡 **Ritmo.** Los Pasos 0 y 1 son la base y pueden entregarse ya escritos; el tiempo se dedica a los Pasos 2 a 11.

</details>

<details>
<summary><strong>🏗️ Arquitectura: el árbol de archivos (🆕 se agrega con Swagger · ✏️ cambia)</strong></summary>

> ```text
> 📁 src/main/java/com/academy
> ├── 📄 AcademyApplication.java
> ├── 📁 presentation
> │   ├── 📁 config
> │   │   └── 🆕 OpenApiConfig.java              información general, seguridad y grupos
> │   └── 📁 controller
> │       ├── ✏️ StudentController.java          @Tag · @Operation · @ApiResponse · @Parameter · @Hidden
> │       └── 🆕 InternalController.java         segundo grupo del contrato
> ├── 📁 business
> │   └── 📁 service
> │       └── StudentService.java
> └── 📁 persistence
>     ├── 📁 entity
>     │   ├── ✏️ StudentEntity.java              @Schema + validaciones (temporal, mientras no haya DTO)
>     │   └── StudentStatus.java
>     └── 📁 repository
>         └── StudentJpaRepository.java
> 📁 src/main/resources
> └── ✏️ application.properties                  propiedades de springdoc
> ```
>
> | Capa | Qué lleva de Swagger |
> |---|---|
> | `presentation.controller` | `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`, `@Hidden` |
> | `persistence.entity` | `@Schema` en `StudentEntity` (**temporal**, porque aún no hay DTO) |
> | `presentation.config` | Beans `OpenAPI` y `GroupedOpenApi` |
> | `business` | **Nada**: no sabe que existe Swagger |
>
> > ⚠️ **Una decisión consciente.** Sin DTO, la entidad es a la vez **tabla**, **modelo** y **contrato de la API**, así que la documentación va en ella. Es lo más simple para aprender Swagger, pero ata el contrato a la tabla: renombrar un campo cambia la API. Cuando se implementen los DTO, las anotaciones `@Schema` se **mueven** a ellos y la entidad queda limpia.

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Preparación</strong></summary>

> <details>
> <summary><strong>Paso 0.1 · MySQL y el proyecto</strong></summary>
>
> > **MySQL.** Hibernate crea las tablas, pero no la base `academy`. Con Docker:
> >
> > ```bash
> > docker run -d --name academy-mysql \
> >   -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academy \
> >   -p 3306:3306 mysql:8.0
> > ```
> >
> > **Proyecto.** En [start.spring.io](https://start.spring.io): **Maven · Java 17 o superior · Group** `com.academy` · **Artifact** `students-api` · dependencias **Spring Web**, **Spring Data JPA**, **Validation** y **MySQL Driver**.
> > **Swagger todavía no se agrega**: primero se construye el CRUD.
> >
> > <details>
> > <summary><strong>📄 pom.xml (dependencias)</strong></summary>
> >
> > **Ruta:** `pom.xml`
> >
> > ```xml
> > <dependency>
> >     <groupId>org.springframework.boot</groupId>
> >     <artifactId>spring-boot-starter-web</artifactId>
> > </dependency>
> > <dependency>
> >     <groupId>org.springframework.boot</groupId>
> >     <artifactId>spring-boot-starter-data-jpa</artifactId>
> > </dependency>
> > <dependency>
> >     <groupId>org.springframework.boot</groupId>
> >     <artifactId>spring-boot-starter-validation</artifactId>
> > </dependency>
> > <dependency>
> >     <groupId>com.mysql</groupId>
> >     <artifactId>mysql-connector-j</artifactId>
> >     <scope>runtime</scope>
> > </dependency>
> > ```
> >
> > </details>
> >
> > > 🔎 **Concepto:** `spring-boot-starter-validation` trae **Bean Validation** (`@NotBlank`, `@Email`, `@Min`…). Más adelante se verá cómo esas reglas aparecen solas en la documentación.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 0.2 · <code>application.properties</code> y los paquetes</strong></summary>
>
> > Se crean los paquetes `presentation.controller`, `business.service`, `persistence.entity` y `persistence.repository` dentro de `com.academy`.
> >
> > <details>
> > <summary><strong>📄 application.properties</strong></summary>
> >
> > **Ruta:** `src/main/resources/application.properties`
> >
> > ```properties
> > # MySQL connection
> > spring.datasource.url=jdbc:mysql://localhost:3306/academy
> > spring.datasource.username=root
> > spring.datasource.password=root
> >
> > # JPA and Hibernate
> > spring.jpa.hibernate.ddl-auto=update
> > spring.jpa.show-sql=true
> > ```
> >
> > </details>
> >
> > ✅ **Punto de control 0:** la aplicación arranca con `Started AcademyApplication` y no crea tablas (aún no hay entidades). Sigue en ejecución porque hay servidor web: se detiene con `Ctrl+C`.
>
> </details>
>

</details>

---

<details>
<summary><strong>🧱 Paso 1 — El CRUD de estudiantes, sin Swagger</strong></summary>

> <details>
> <summary><strong>Paso 1.1 · Persistencia y negocio</strong></summary>
>
> > Entidad, enumeración, repositorio de Spring Data y servicio. Es lo mismo que en la guía 01, con cuatro cambios: un atributo `status` (una **enumeración**, útil para ver cómo la documenta Swagger), **validaciones** en la entidad, búsqueda por nombre con **paginación** y actualización por `id`.
> >
> > <details>
> > <summary><strong>📄 StudentStatus.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/entity/StudentStatus.java`
> >
> > ```java
> > package com.academy.persistence.entity;
> >
> > public enum StudentStatus {
> >     ACTIVE,
> >     INACTIVE
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 StudentEntity.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/entity/StudentEntity.java`
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
> > import jakarta.validation.constraints.Email;
> > import jakarta.validation.constraints.Max;
> > import jakarta.validation.constraints.Min;
> > import jakarta.validation.constraints.NotBlank;
> > import jakarta.validation.constraints.NotNull;
> > import jakarta.validation.constraints.Size;
> >
> > @Entity
> > @Table(name = "student")
> > public class StudentEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     @NotBlank
> >     @Size(min = 2, max = 100)
> >     @Column(nullable = false)
> >     private String name;
> >
> >     @Min(16)
> >     @Max(99)
> >     private int age;
> >
> >     @NotBlank
> >     @Email
> >     @Column(nullable = false, unique = true)
> >     private String email;
> >
> >     @NotNull
> >     @Enumerated(EnumType.STRING)
> >     @Column(nullable = false)
> >     private StudentStatus status;
> >
> >     protected StudentEntity() {
> >     }
> >
> >     public StudentEntity(Long id, String name, int age, String email, StudentStatus status) {
> >         this.id = id;
> >         this.name = name;
> >         this.age = age;
> >         this.email = email;
> >         this.status = status;
> >     }
> >
> >     public Long getId() { return id; }
> >     public String getName() { return name; }
> >     public int getAge() { return age; }
> >     public String getEmail() { return email; }
> >     public StudentStatus getStatus() { return status; }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 StudentJpaRepository.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/repository/StudentJpaRepository.java`
> >
> > ```java
> > package com.academy.persistence.repository;
> >
> > import com.academy.persistence.entity.StudentEntity;
> > import org.springframework.data.domain.Page;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.data.jpa.repository.JpaRepository;
> >
> > import java.util.Optional;
> >
> > public interface StudentJpaRepository extends JpaRepository<StudentEntity, Long> {
> >
> >     Page<StudentEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
> >
> >     Optional<StudentEntity> findByEmailIgnoreCase(String email);
> >
> >     boolean existsByEmailIgnoreCase(String email);
> >
> >     boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 StudentService.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/StudentService.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.persistence.repository.StudentJpaRepository;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
> >
> > import java.util.List;
> > import java.util.Optional;
> >
> > @Service
> > public class StudentService {
> >
> >     private final StudentJpaRepository students;
> >
> >     public StudentService(StudentJpaRepository students) {
> >         this.students = students;
> >     }
> >
> >     @Transactional(readOnly = true)
> >     public List<StudentEntity> search(String name, Pageable pageable) {
> >         if (name == null || name.isBlank()) {
> >             return students.findAll(pageable).getContent();
> >         }
> >         return students.findByNameContainingIgnoreCase(name, pageable).getContent();
> >     }
> >
> >     @Transactional(readOnly = true)
> >     public Optional<StudentEntity> findById(Long id) {
> >         return students.findById(id);
> >     }
> >
> >     @Transactional(readOnly = true)
> >     public Optional<StudentEntity> findByEmail(String email) {
> >         return students.findByEmailIgnoreCase(email);
> >     }
> >
> >     @Transactional
> >     public Optional<StudentEntity> create(StudentEntity student) {
> >         if (students.existsByEmailIgnoreCase(student.getEmail())) {
> >             return Optional.empty();
> >         }
> >         StudentEntity toSave = new StudentEntity(
> >                 null, student.getName(), student.getAge(), student.getEmail(), student.getStatus());
> >         return Optional.of(students.save(toSave));
> >     }
> >
> >     @Transactional
> >     public Optional<StudentEntity> update(Long id, StudentEntity data) {
> >         if (!students.existsById(id)) {
> >             return Optional.empty();
> >         }
> >         if (students.existsByEmailIgnoreCaseAndIdNot(data.getEmail(), id)) {
> >             return Optional.empty();
> >         }
> >         return Optional.of(students.save(new StudentEntity(
> >                 id, data.getName(), data.getAge(), data.getEmail(), data.getStatus())));
> >     }
> >
> >     @Transactional
> >     public boolean delete(Long id) {
> >         if (!students.existsById(id)) {
> >             return false;
> >         }
> >         students.deleteById(id);
> >         return true;
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Elemento | Idea |
> > > |---|---|
> > > | `@Enumerated(EnumType.STRING)` | Guarda `ACTIVE` o `INACTIVE` como texto en la columna. |
> > > | `Page<StudentEntity> findByNameContainingIgnoreCase(name, Pageable)` | Consulta derivada con filtro y paginación. |
> > > | `@NotBlank`, `@Size`, `@Min`, `@Max`, `@Email`, `@NotNull` | Reglas de **Bean Validation** sobre los campos; con `@Valid` en el controlador, un cuerpo inválido responde `400`. |
> > > | `create` crea `new StudentEntity(null, …)` | **Descarta el `id`** que mande el cliente: así `save` siempre **inserta** y nadie sobrescribe una fila ajena. |
> > > | `update` crea `new StudentEntity(id, …)` | Con `id` informado, `save` **actualiza** esa fila. |
> > > | `@Transactional` / `readOnly = true` | Va en los métodos del **servicio**. |
>
> </details>
>
>
> <details>
> <summary><strong>Paso 1.2 · El controlador</strong></summary>
>
> > Todavía **no hay DTO**: el controlador recibe y devuelve directamente la entidad `StudentEntity`, como en la guía 01. El listado devuelve los estudiantes de la página pedida (`List`).
> >
> > El controlador **no lleva ninguna anotación de Swagger todavía**.
> >
> > <details>
> > <summary><strong>📄 StudentController.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > package com.academy.presentation.controller;
> >
> > import com.academy.business.service.StudentService;
> > import com.academy.persistence.entity.StudentEntity;
> > import jakarta.validation.Valid;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.http.ResponseEntity;
> > import org.springframework.web.bind.annotation.DeleteMapping;
> > import org.springframework.web.bind.annotation.GetMapping;
> > import org.springframework.web.bind.annotation.PathVariable;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.PutMapping;
> > import org.springframework.web.bind.annotation.RequestBody;
> > import org.springframework.web.bind.annotation.RequestMapping;
> > import org.springframework.web.bind.annotation.RequestParam;
> > import org.springframework.web.bind.annotation.RestController;
> >
> > import java.util.List;
> >
> > @RestController
> > @RequestMapping("/api/students")
> > public class StudentController {
> >
> >     private final StudentService service;
> >
> >     public StudentController(StudentService service) {
> >         this.service = service;
> >     }
> >
> >     @GetMapping
> >     public List<StudentEntity> search(@RequestParam(required = false) String name, Pageable pageable) {
> >         return service.search(name, pageable);
> >     }
> >
> >     @GetMapping("/{id}")
> >     public ResponseEntity<StudentEntity> findById(@PathVariable Long id) {
> >         return service.findById(id)
> >                 .map(ResponseEntity::ok)
> >                 .orElse(ResponseEntity.notFound().build());
> >     }
> >
> >     @GetMapping("/by-email")
> >     public ResponseEntity<StudentEntity> findByEmail(@RequestParam String email) {
> >         return service.findByEmail(email)
> >                 .map(ResponseEntity::ok)
> >                 .orElse(ResponseEntity.notFound().build());
> >     }
> >
> >     @PostMapping
> >     public ResponseEntity<StudentEntity> create(@Valid @RequestBody StudentEntity student) {
> >         return service.create(student)
> >                 .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
> >                 .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
> >     }
> >
> >     @PutMapping("/{id}")
> >     public ResponseEntity<StudentEntity> update(@PathVariable Long id, @Valid @RequestBody StudentEntity student) {
> >         if (service.findById(id).isEmpty()) {
> >             return ResponseEntity.notFound().build();
> >         }
> >         return service.update(id, student)
> >                 .map(ResponseEntity::ok)
> >                 .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
> >     }
> >
> >     @DeleteMapping("/{id}")
> >     public ResponseEntity<Void> delete(@PathVariable Long id) {
> >         return service.delete(id)
> >                 ? ResponseEntity.noContent().build()
> >                 : ResponseEntity.notFound().build();
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Respuesta | Cuándo |
> > > |---|---|
> > > | `201` | Estudiante creado |
> > > | `400` | Falla una validación (`@Valid`) |
> > > | `409` | El correo ya existe (el servicio devolvió `Optional` vacío) |
> > > | `404` | No existe el `id` |
> > > | `204` | Eliminado, sin cuerpo |
> > >
> > > 🔎 **Concepto (M2):** `Pageable` lo construye Spring a partir de `?page=0&size=2&sort=name,asc`; el controlador solo lo pasa al servicio.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 1.3 · Probar el CRUD con <code>curl</code></strong></summary>
>
> > ```bash
> > curl -i -X POST localhost:8080/api/students -H "Content-Type: application/json" \
> >      -d '{"name":"Ana Torres","age":22,"email":"ana@mail.com","status":"ACTIVE"}'
> > ```
> >
> > **Resultados reales:**
> >
> > | Petición | Respuesta |
> > |---|---|
> > | `POST` Ana (correo nuevo) | `201` · `{"id":1,"name":"Ana Torres","age":22,"email":"ana@mail.com","status":"ACTIVE"}` |
> > | `POST` con el mismo correo en mayúsculas | `409` |
> > | `POST` con nombre vacío, edad `5` y correo `x` | `400` |
> > | `GET /api/students?page=0&size=2` | `200` · un arreglo con los estudiantes de la página |
> > | `POST` que **trae** `"id":1` en el JSON | `201` · se guarda con **otro** `id` (el servicio descarta el del cliente) |
> > | `GET /api/students/1` · `GET /api/students/999` | `200` · `404` |
> > | `PUT /api/students/{id}` con el correo de otro | `409` |
> > | `DELETE /api/students/{id}` · otra vez | `204` · `404` |
> > | `GET /swagger-ui.html` | `404`: **todavía no hay Swagger** |
> >
> > ✅ **Punto de control 1:** las respuestas coinciden. Este es el CRUD **sin documentar**: la API funciona, pero para saber cómo usarla hay que leer el código.
>
> </details>
>

</details>

---

<details>
<summary><strong>✨ Paso 2 — Primera documentación: una dependencia</strong></summary>

> <details>
> <summary><strong>Paso 2.1 · Agregar springdoc</strong></summary>
>
> > Se agrega **una** dependencia al `pom.xml` y no se escribe nada más:
> >
> > <details>
> > <summary><strong>📄 pom.xml (agregar)</strong></summary>
> >
> > **Ruta:** `pom.xml`
> >
> > ```xml
> > <dependency>
> >     <groupId>org.springdoc</groupId>
> >     <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
> >     <version>2.6.0</version>
> > </dependency>
> > ```
> >
> > </details>
> >
> > > | Pieza | Qué trae |
> > > |---|---|
> > > | `springdoc-openapi-starter-webmvc-ui` | El generador del contrato **y** la interfaz de Swagger UI (para Spring MVC) |
> > > | `2.6.0` | La línea 2.x es la de **Spring Boot 3**; la 1.x era para Boot 2 |
> > >
> > > Se reinicia la aplicación y se abren dos direcciones:
> > >
> > > | Dirección | Qué muestra |
> > > |---|---|
> > > | `http://localhost:8080/swagger-ui.html` | La pantalla interactiva (redirige con `302` a `/swagger-ui/index.html`) |
> > > | `http://localhost:8080/v3/api-docs` | El contrato OpenAPI en **JSON** |
> > >
> > >
> > > **Resultado real** de `/v3/api-docs` sin escribir nada más:
> > >
> > > | Qué se obtiene | Valor real |
> > > |---|---|
> > > | Operaciones descubiertas | Las **seis** (`GET`, `POST`, `GET /{id}`, `PUT`, `DELETE`, `GET /by-email`) |
> > > | Agrupación | Una etiqueta con el nombre técnico `student-controller` |
> > > | Título y versión | `OpenAPI definition` · `v0` (valores por defecto) |
> > > | Respuestas | **Solo `200` en todas**, aun `POST` (que responde `201`) y `DELETE` (que responde `204`/`404`) |
> > > | Paginación | Un único parámetro `pageable`, **obligatorio**, con el modelo `Pageable` |
> > > | Modelos | `StudentEntity` y `Pageable` (la entidad ya trae los límites de las validaciones) |
> > >
> > > Los tipos, las rutas y los modelos salen de **leer el código**. Lo que el compilador no puede saber queda **mal o vacío**: los códigos reales de `ResponseEntity`, la descripción de cada operación, los ejemplos y la paginación.
> > >
> > > 💡 **Beneficio de este paso:** la documentación **no se escribe a mano ni se desactualiza**: cambia cuando cambia el código.
> > >
> > > 🗣️ **Pregunta:** ¿qué falta? *(El nombre y la versión de la API, una descripción de cada operación, los códigos de respuesta verdaderos, ejemplos y una paginación usable: eso lo sabe una persona, no el compilador.)*
>
> </details>
>
>
> <details>
> <summary><strong>Paso 2.2 · Probar una operación con <em>Try it out</em></strong></summary>
>
> > En Swagger UI: abrir `POST /api/students` → **Try it out** → editar el JSON → **Execute**. Se ve la petición `curl` equivalente, la URL, el código de respuesta, el cuerpo y las cabeceras.
> >
> > | Zona de la pantalla | Qué hace |
> > |---|---|
> > | Botón **Try it out** | Convierte los campos en editables |
> > | Botón **Execute** | Envía la petición real al servidor |
> > | **Curl** | Muestra el comando equivalente (útil para copiar) |
> > | **Server response** | Código, cuerpo y cabeceras reales |
> > | **Responses** | Las respuestas **documentadas** (hasta ahora, solo `200`) |
> >
> > ✅ **Punto de control 2:** `/swagger-ui.html` redirige (`302`) a `/swagger-ui/index.html` y `/v3/api-docs` devuelve JSON con las seis operaciones.
>
> </details>
>

</details>

---

<details>
<summary><strong>🏷️ Paso 3 — Información general de la API</strong></summary>

> <details>
> <summary><strong>Paso 3.1 · El bean <code>OpenAPI</code></strong></summary>
>
> > Se crea `presentation.config.OpenApiConfig`. Un bean `OpenAPI` describe la API **completa**: nombre, versión, descripción, contacto, licencia y servidores.
> >
> > <details>
> > <summary><strong>📄 OpenApiConfig.java (versión del Paso 3)</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/config/OpenApiConfig.java`
> >
> > ```java
> > package com.academy.presentation.config;
> >
> > import io.swagger.v3.oas.models.OpenAPI;
> > import io.swagger.v3.oas.models.info.Contact;
> > import io.swagger.v3.oas.models.info.Info;
> > import io.swagger.v3.oas.models.info.License;
> > import io.swagger.v3.oas.models.servers.Server;
> > import org.springframework.context.annotation.Bean;
> > import org.springframework.context.annotation.Configuration;
> >
> > import java.util.List;
> >
> > @Configuration
> > public class OpenApiConfig {
> >
> >     @Bean
> >     public OpenAPI academyOpenApi() {
> >         return new OpenAPI()
> >                 .info(new Info()
> >                         .title("Academy API")
> >                         .version("1.0.0")
> >                         .description("CRUD de estudiantes con Spring Boot, JPA y MySQL, documentado con OpenAPI.")
> >                         .contact(new Contact().name("Equipo Academy").email("academy@mail.com"))
> >                         .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
> >                 .servers(List.of(new Server().url("http://localhost:8080").description("Local")));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Pieza | Dónde se ve en Swagger UI |
> > > |---|---|
> > > | `title` · `version` | El encabezado grande: **Academy API** `1.0.0` `OAS 3.0` |
> > > | `description` | El párrafo bajo el título |
> > > | `contact` | El enlace «Contact Equipo Academy» |
> > > | `license` | El enlace «Apache 2.0» |
> > > | `servers` | La lista **Servers** (útil para elegir *local*, *pruebas* o *producción* sin cambiar el código) |
> > >
> > > 💡 **Beneficio de este paso:** quien abre la documentación sabe **qué API es**, qué versión es y a quién preguntar.
> > >
> > > 🔎 **Alternativa.** La anotación `@OpenAPIDefinition(info = @Info(…))` sobre cualquier clase hace lo mismo. El bean es más cómodo cuando se agregan después la seguridad y otros elementos (Pasos 9 y 10).
>
> </details>
>
>
> <details>
> <summary><strong>Paso 3.2 · Ver el resultado</strong></summary>
>
> > Se reinicia y se abre Swagger UI. **Resultado real** (parte superior de la pantalla): título **Academy API**, insignias `1.0.0` y `OAS 3.0`, la descripción, los enlaces de contacto y licencia, y el selector **Servers** con `http://localhost:8080 - Local`.
> >
> > ✅ **Punto de control 3:** la cabecera muestra los datos de la API.
>
> </details>
>

</details>

---

<details>
<summary><strong>🧭 Paso 4 — Etiquetas, operaciones y respuestas</strong></summary>

> <details>
> <summary><strong>Paso 4.1 · <code>@Tag</code> en el controlador</strong></summary>
>
> > Un **tag** agrupa operaciones bajo un título. Sin él, springdoc usa el nombre de la clase (`student-controller`).
> >
> > <details>
> > <summary><strong>📄 StudentController.java</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @RestController
> > @RequestMapping("/api/students")
> > @Tag(name = "Students", description = "Operaciones sobre estudiantes")
> > public class StudentController {
> >     ...
> > }
> > ```
> >
> > </details>
> >
> > > 💡 **Beneficio:** el grupo se llama **Students** y lleva una descripción, en lugar del nombre técnico de la clase.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 4.2 · <code>@Operation</code></strong></summary>
>
> > Cada método recibe un **resumen** (una línea, aparece en la lista) y una **descripción** (detalle, aparece al abrir la operación y admite Markdown).
> >
> > <details>
> > <summary><strong>📄 Crear y listar</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @Operation(summary = "Listar estudiantes",
> >         description = "Devuelve una página de estudiantes. Con `name` filtra por nombre (contiene, sin importar mayúsculas).")
> > @GetMapping
> > public List<StudentEntity> search(...) { ... }
> >
> > @Operation(summary = "Crear un estudiante",
> >         description = "El `id` lo genera la base. Si el correo ya existe responde `409`.")
> > @PostMapping
> > public ResponseEntity<StudentEntity> create(...) { ... }
> > ```
> >
> > </details>
> >
> > > | Atributo | Dónde se ve |
> > > |---|---|
> > > | `summary` | Al lado de la ruta, en la lista de operaciones |
> > > | `description` | Dentro de la operación, bajo el título |
> > >
> > > 💡 **Beneficio:** el equipo de frontend entiende **para qué sirve** cada operación sin leer el código.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 4.3 · <code>@ApiResponse</code>: documentar las respuestas</strong></summary>
>
> > Springdoc detecta `200` o `201`, pero **no puede adivinar** que el `409` significa «correo repetido». Cada respuesta se declara con `@ApiResponse`. En las que **no devuelven cuerpo** se usa `content = @Content` (vacío) para que Swagger no muestre un modelo que no existe.
> >
> > <details>
> > <summary><strong>📄 Respuestas de POST /api/students</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @ApiResponse(responseCode = "201", description = "Estudiante creado")
> > @ApiResponse(responseCode = "400", description = "Datos inválidos (Bean Validation)", content = @Content)
> > @ApiResponse(responseCode = "409", description = "El correo ya está registrado", content = @Content)
> > @PostMapping
> > public ResponseEntity<StudentEntity> create(@Valid @RequestBody StudentEntity student) { ... }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 Respuestas de GET /api/students/{id}</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @Operation(summary = "Buscar un estudiante por id")
> > @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
> > @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
> > @GetMapping("/{id}")
> > public ResponseEntity<StudentEntity> findById(...) { ... }
> > ```
> >
> > </details>
> >
> > > | Operación | Respuestas documentadas (reales, tomadas de `/v3/api-docs`) |
> > > |---|---|
> > > | `GET /api/students` | `200` |
> > > | `GET /api/students/{id}` | `200` · `404` |
> > > | `POST /api/students` | `201` · `400` · `409` |
> > > | `PUT /api/students/{id}` | `200` · `400` · `404` · `409` |
> > > | `DELETE /api/students/{id}` | `204` · `404` |
> > >
> > > 💡 **Beneficio:** el contrato dice **qué puede salir mal** y qué significa cada código. Quien consume la API sabe qué manejar sin probar a ciegas.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 4.4 · Ver el resultado</strong></summary>
>
> > En Swagger UI, al abrir `POST /api/students` la sección **Responses** ahora lista `201`, `400` y `409` con su descripción. La lista de operaciones muestra el `summary` de cada una.
> >
> > ✅ **Punto de control 4:** cada operación tiene resumen y todas sus respuestas.
>
> </details>
>

</details>

---

<details>
<summary><strong>🎛️ Paso 5 — Documentar los parámetros</strong></summary>

> <details>
> <summary><strong>Paso 5.1 · <code>@Parameter</code>: ruta y consulta</strong></summary>
>
> > Springdoc ya conoce el **nombre**, el **tipo** y si es obligatorio. `@Parameter` agrega lo que falta: una **descripción** y un **ejemplo**, que Swagger UI usa para **rellenar el campo**.
> >
> > <details>
> > <summary><strong>📄 Parámetros de ruta y de consulta</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @GetMapping("/{id}")
> > public ResponseEntity<StudentEntity> findById(
> >         @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) { ... }
> >
> > @GetMapping
> > public List<StudentEntity> search(
> >         @Parameter(description = "Parte del nombre a buscar", example = "ana")
> >         @RequestParam(required = false) String name,
> >         @ParameterObject Pageable pageable) { ... }
> > ```
> >
> > </details>
> >
> > > - `@PathVariable` → parámetro **de ruta** (obligatorio). `@RequestParam` → parámetro **de consulta**; con `required = false` es opcional.
> > >
> > > 💡 **Beneficio:** el ejemplo `1` o `ana` aparece ya escrito en el campo; quien prueba solo pulsa **Execute**.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 5.2 · <code>@ParameterObject</code>: la paginación</strong></summary>
>
> > `Pageable` es un objeto con varios campos (`page`, `size`, `sort`). Sin ayuda, Swagger lo muestra como **un solo cuadro JSON**. Con `@ParameterObject` se **despliega en tres parámetros** de consulta.
> >
> > Se agrega el import `org.springdoc.core.annotations.ParameterObject`.
> >
> > > **Resultado real** (de `/v3/api-docs`):
> > >
> > > | Parámetro | Dónde | Tipo | Ejemplo |
> > > |---|---|---|---|
> > > | `name` | query | string | `ana` |
> > > | `page` | query | integer | |
> > > | `size` | query | integer | |
> > > | `sort` | query | array | |
> > >
> > > Con la URL de ejemplo `?name=a&page=0&size=2&sort=name,asc` la respuesta real fue un arreglo con los estudiantes que coinciden, ordenados por nombre.
> > >
> > > 💡 **Beneficio:** la paginación se prueba rellenando tres campos sencillos en vez de escribir un JSON a mano.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 5.3 · Ver el resultado</strong></summary>
>
> > En `GET /api/students` aparecen los cuatro parámetros con su descripción; `name` viene con `ana` de ejemplo.
> >
> > ✅ **Punto de control 5:** `GET /api/students` muestra `name`, `page`, `size` y `sort`.
>
> </details>
>

</details>

---

<details>
<summary><strong>📐 Paso 6 — Documentar el modelo (<code>@Schema</code>) y las validaciones</strong></summary>

> <details>
> <summary><strong>Paso 6.1 · <code>@Schema</code> en la entidad</strong></summary>
>
> > `@Schema` describe cada **campo** (descripción, ejemplo, obligatorio, mínimos y máximos, solo lectura). Como todavía **no hay DTO**, se escribe en `StudentEntity`, que es la clase que viaja por la API.
> >
> > Con `@Schema(name = "Student")` sobre la clase, el modelo se llama **Student** en el contrato (sin él se llamaría `StudentEntity`, un detalle interno).
> >
> > <details>
> > <summary><strong>📄 StudentEntity.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/entity/StudentEntity.java`
> >
> > ```java
> > package com.academy.persistence.entity;
> >
> > import io.swagger.v3.oas.annotations.media.Schema;
> > import jakarta.persistence.Column;
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.EnumType;
> > import jakarta.persistence.Enumerated;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import jakarta.persistence.Table;
> > import jakarta.validation.constraints.Email;
> > import jakarta.validation.constraints.Max;
> > import jakarta.validation.constraints.Min;
> > import jakarta.validation.constraints.NotBlank;
> > import jakarta.validation.constraints.NotNull;
> > import jakarta.validation.constraints.Size;
> >
> > @Schema(name = "Student", description = "Estudiante de la academia")
> > @Entity
> > @Table(name = "student")
> > public class StudentEntity {
> >
> >     @Schema(description = "Identificador generado por la base", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     @Schema(description = "Nombre completo", example = "Ana Torres", requiredMode = Schema.RequiredMode.REQUIRED)
> >     @NotBlank
> >     @Size(min = 2, max = 100)
> >     @Column(nullable = false)
> >     private String name;
> >
> >     @Schema(description = "Edad en años", example = "22", minimum = "16", maximum = "99")
> >     @Min(16)
> >     @Max(99)
> >     private int age;
> >
> >     @Schema(description = "Correo único (sin importar mayúsculas)", example = "ana@mail.com",
> >             requiredMode = Schema.RequiredMode.REQUIRED)
> >     @NotBlank
> >     @Email
> >     @Column(nullable = false, unique = true)
> >     private String email;
> >
> >     @Schema(description = "Estado del estudiante", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
> >     @NotNull
> >     @Enumerated(EnumType.STRING)
> >     @Column(nullable = false)
> >     private StudentStatus status;
> >
> >     protected StudentEntity() {
> >     }
> >
> >     public StudentEntity(Long id, String name, int age, String email, StudentStatus status) {
> >         this.id = id;
> >         this.name = name;
> >         this.age = age;
> >         this.email = email;
> >         this.status = status;
> >     }
> >
> >     public Long getId() { return id; }
> >     public String getName() { return name; }
> >     public int getAge() { return age; }
> >     public String getEmail() { return email; }
> >     public StudentStatus getStatus() { return status; }
> > }
> > ```
> >
> > </details>
> >
> > > | Atributo de `@Schema` | Efecto en Swagger UI |
> > > |---|---|
> > > | `description` | El texto junto al campo |
> > > | `example` | El valor que se rellena al probar |
> > > | `requiredMode = REQUIRED` | Marca el campo como obligatorio (asterisco rojo) |
> > > | `accessMode = READ_ONLY` | El `id` aparece **solo en la respuesta**; no se pide al crear (no sale en el ejemplo de la petición) |
> > > | `minimum` / `maximum` | Rango de valores permitido |
> > > | `name` (sobre la clase) | Nombre del modelo en la sección **Schemas** |
> > >
> > > > ⚠️ **Costo de hacerlo sobre la entidad.** `StudentEntity` ahora mezcla JPA, validación y Swagger, y el contrato queda atado a la tabla. Es temporal: con DTO, estas anotaciones se mueven al DTO.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 6.2 · Bean Validation pasa solo al contrato</strong></summary>
>
> > Las restricciones `@NotBlank`, `@Size`, `@Min`, `@Max` y `@NotNull` **no hay que repetirlas**: springdoc las lee. **Resultado real** del esquema `Student` en `/v3/api-docs` (abreviado):
> >
> > ```json
> > {
> >   "description": "Estudiante de la academia",
> >   "required": ["email", "name", "status"],
> >   "properties": {
> >     "id":     { "type": "integer", "format": "int64", "readOnly": true, "example": 1 },
> >     "name":   { "type": "string", "minLength": 2, "maxLength": 100, "example": "Ana Torres" },
> >     "age":    { "type": "integer", "format": "int32", "minimum": 16, "maximum": 99, "example": 22 },
> >     "email":  { "type": "string", "example": "ana@mail.com" },
> >     "status": { "type": "string", "enum": ["ACTIVE", "INACTIVE"], "example": "ACTIVE" }
> >   }
> > }
> > ```
> >
> > | En el código | En el contrato |
> > |---|---|
> > | `@NotBlank`, `@NotNull` | Campo en `required` |
> > | `@Size(min = 2, max = 100)` | `minLength` y `maxLength` |
> > | `@Min(16)` · `@Max(99)` | `minimum` y `maximum` |
> > | `enum StudentStatus` | `enum: ["ACTIVE","INACTIVE"]`: en Swagger UI, un **desplegable** |
> > | `@Email` | **No** aparece como `format` en esta versión: la validación funciona en el servidor, pero el contrato no lo dice |
> >
> > > 💡 **Beneficio de este paso:** **una sola fuente de verdad**. La regla se escribe una vez (en la validación) y sirve para **rechazar** datos y para **documentar**. El contrato no puede contradecir al código.
> > >
> > > ⚠️ **Lectura crítica.** No todo pasa: `@Email` no llega al contrato. Si importa que el cliente lo sepa, se agrega a mano con `@Schema(format = "email")`.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 6.3 · Ver el resultado</strong></summary>
>
> > En la sección **Schemas** (al final de la pantalla) aparece `Student`. Al expandirlo se ven los cinco campos con su descripción, los obligatorios marcados, el `id` de solo lectura y el desplegable de `status`. En `GET /api/students`, la respuesta `200` ahora es un **arreglo de `Student`**.
> >
> > ✅ **Punto de control 6:** los modelos muestran descripción, ejemplos, obligatorios y el desplegable del enumerado.
>
> </details>
>

</details>

---

<details>
<summary><strong>🧪 Paso 7 — Ejemplos del cuerpo de la petición</strong></summary>

> <details>
> <summary><strong>Paso 7.1 · <code>@ExampleObject</code>: varios ejemplos para elegir</strong></summary>
>
> > El ejemplo que genera `@Schema` es **uno** y se arma campo por campo. Con `@RequestBody` de Swagger y `@ExampleObject` se ofrecen **varios ejemplos completos**, con nombre, que se eligen en un **desplegable**.
> >
> > La anotación se escribe con **nombre completo** para no chocar con la `@RequestBody` de Spring:
> >
> > <details>
> > <summary><strong>📄 POST /api/students</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @io.swagger.v3.oas.annotations.parameters.RequestBody(
> >         description = "Datos del estudiante",
> >         content = @Content(examples = {
> >                 @ExampleObject(name = "Estudiante activo", summary = "Un estudiante activo",
> >                         value = "{\"name\":\"Ana Torres\",\"age\":22,\"email\":\"ana@mail.com\",\"status\":\"ACTIVE\"}"),
> >                 @ExampleObject(name = "Estudiante inactivo", summary = "Un estudiante inactivo",
> >                         value = "{\"name\":\"Luis Pérez\",\"age\":25,\"email\":\"luis@mail.com\",\"status\":\"INACTIVE\"}")
> >         }))
> > @PostMapping
> > public ResponseEntity<StudentEntity> create(@Valid @RequestBody StudentEntity student) { ... }
> > ```
> >
> > </details>
> >
> > > | Pieza | Para qué |
> > > |---|---|
> > > | `io.swagger…parameters.RequestBody` | La de **Swagger** (documenta); la de Spring (`@RequestBody`) **lee** el cuerpo |
> > > | `@ExampleObject(name, summary, value)` | Un ejemplo completo en JSON |
> > > | `content = @Content(examples = …)` | Agrupa los ejemplos |
> > >
> > > **Resultado real** (de `/v3/api-docs`): el cuerpo de `POST /api/students` trae los ejemplos `Estudiante activo` y `Estudiante inactivo`, y `"required": true`.
> > >
> > > 💡 **Beneficio:** quien prueba elige un caso válido con un clic. Sirve también para mostrar casos **de borde** (un ejemplo «nombre vacío» para provocar el `400`).
>
> </details>
>
>
> <details>
> <summary><strong>Paso 7.2 · Ver el resultado</strong></summary>
>
> > En `POST /api/students` → **Try it out** aparece, sobre el cuadro de texto, el desplegable **Examples** con los dos nombres.
> >
> > ✅ **Punto de control 7:** al elegir un ejemplo, el cuadro se rellena con ese JSON.
>
> </details>
>

</details>

---

<details>
<summary><strong>🚧 Paso 8 — Operaciones obsoletas y ocultas</strong></summary>

> <details>
> <summary><strong>Paso 8.1 · <code>deprecated</code>: avisar sin romper</strong></summary>
>
> > `GET /api/students/by-email` sigue funcionando, pero se prefiere `GET /api/students?name=`. Se **marca obsoleta**: no se borra, se avisa.
> >
> > <details>
> > <summary><strong>📄 GET /api/students/by-email</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @Operation(summary = "Buscar un estudiante por correo",
> >         description = "Obsoleto: usar `GET /api/students?name=` o `GET /api/students/{id}`.",
> >         deprecated = true)
> > @GetMapping("/by-email")
> > public ResponseEntity<StudentEntity> findByEmail(...) { ... }
> > ```
> >
> > </details>
> >
> > > En Swagger UI la operación aparece **tachada y en gris**.
> > >
> > > 💡 **Beneficio:** los clientes ven el aviso **antes** de que la operación desaparezca, y hay tiempo para migrar.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 8.2 · <code>@Hidden</code>: funciona, pero no se documenta</strong></summary>
>
> > Hay rutas que existen pero **no deben aparecer** en el contrato público (una comprobación de vida, una prueba interna).
> >
> > <details>
> > <summary><strong>📄 GET /api/students/ping</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @Hidden
> > @GetMapping("/ping")
> > public String ping() {
> >     return "pong";
> > }
> > ```
> >
> > </details>
> >
> > > | Comprobación real | Resultado |
> > > |---|---|
> > > | `GET /api/students/ping` | `200` · `pong` (la ruta **sí funciona**) |
> > > | `/ping` en `/v3/api-docs` | **No aparece** |
> > >
> > > `@Hidden` también se puede poner **sobre la clase** para ocultar un controlador entero.
> > >
> > > ⚠️ **No es seguridad.** Ocultar una ruta de la documentación **no la protege**: cualquiera que conozca la URL puede llamarla.
> > >
> > > 💡 **Beneficio:** el contrato público muestra solo lo que se quiere ofrecer.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 8.3 · Ver el resultado</strong></summary>
>
> > La lista muestra `by-email` tachada y **no** muestra `ping`.
> >
> > ✅ **Punto de control 8:** una operación tachada y otra invisible.
>
> </details>
>

</details>

---

<details>
<summary><strong>🔐 Paso 9 — Esquema de seguridad (botón Authorize)</strong></summary>

> <details>
> <summary><strong>Paso 9.1 · Declarar el esquema y exigirlo en una operación</strong></summary>
>
> > Dos piezas: **declarar** que la API usa un token *Bearer* (en `OpenApiConfig`) y **marcar** qué operaciones lo exigen (`@SecurityRequirement`).
> >
> > <details>
> > <summary><strong>📄 OpenApiConfig.java (agregar al bean OpenAPI)</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/config/OpenApiConfig.java`
> >
> > ```java
> > .components(new Components().addSecuritySchemes("bearerAuth",
> >         new SecurityScheme()
> >                 .type(SecurityScheme.Type.HTTP)
> >                 .scheme("bearer")
> >                 .bearerFormat("JWT")));
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 DELETE /api/students/{id}</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @Operation(summary = "Eliminar un estudiante", security = @SecurityRequirement(name = "bearerAuth"))
> > @DeleteMapping("/{id}")
> > public ResponseEntity<Void> delete(...) { ... }
> > ```
> >
> > </details>
> >
> > > | Pieza | Dónde se ve |
> > > |---|---|
> > > | `SecurityScheme` `HTTP` / `bearer` / `JWT` | El botón **Authorize** 🔓 arriba a la derecha |
> > > | `@SecurityRequirement(name = "bearerAuth")` | Un **candado** 🔒 en `DELETE /api/students/{id}` |
> > >
> > > Al pulsar **Authorize** se escribe el token; Swagger UI lo envía en la cabecera `Authorization: Bearer …` de **todas** las operaciones con candado.
> > >
> > > ⚠️ **Esto solo documenta.** La aplicación **no valida** ningún token (no hay Spring Security): `DELETE` responde `204` con o sin token. Para exigirlo de verdad se necesita seguridad real (otra guía).
> > >
> > > 💡 **Beneficio:** el contrato dice **cómo autenticarse**; cuando se agregue la seguridad, Swagger UI ya sabrá mandar el token.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 9.2 · Ver el resultado</strong></summary>
>
> > **Resultado real:** arriba a la derecha aparece **Authorize** y, en la lista, solo `DELETE` lleva candado. En `/v3/api-docs`, `securitySchemes` contiene `bearerAuth` y solo `DELETE` trae `security`.
> >
> > ✅ **Punto de control 9:** botón *Authorize* visible y un candado en `DELETE`.
>
> </details>
>

</details>

---

<details>
<summary><strong>🗂️ Paso 10 — Grupos: varios contratos en uno</strong></summary>

> <details>
> <summary><strong>Paso 10.1 · <code>GroupedOpenApi</code> e <code>InternalController</code></strong></summary>
>
> > Una API grande se divide en **grupos** (pública, interna, administración…). Cada grupo tiene **su propio contrato** y aparece en el selector **Select a definition**.
> >
> > Se agrega un segundo controlador con operaciones de apoyo y los dos grupos en la configuración. Este es el `OpenApiConfig` **completo y final**:
> >
> > <details>
> > <summary><strong>📄 OpenApiConfig.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/config/OpenApiConfig.java`
> >
> > ```java
> > package com.academy.presentation.config;
> >
> > import io.swagger.v3.oas.models.Components;
> > import io.swagger.v3.oas.models.OpenAPI;
> > import io.swagger.v3.oas.models.info.Contact;
> > import io.swagger.v3.oas.models.info.Info;
> > import io.swagger.v3.oas.models.info.License;
> > import io.swagger.v3.oas.models.security.SecurityScheme;
> > import io.swagger.v3.oas.models.servers.Server;
> > import org.springdoc.core.models.GroupedOpenApi;
> > import org.springframework.context.annotation.Bean;
> > import org.springframework.context.annotation.Configuration;
> >
> > import java.util.List;
> >
> > @Configuration
> > public class OpenApiConfig {
> >
> >     @Bean
> >     public OpenAPI academyOpenApi() {
> >         return new OpenAPI()
> >                 .info(new Info()
> >                         .title("Academy API")
> >                         .version("1.0.0")
> >                         .description("CRUD de estudiantes con Spring Boot, JPA y MySQL, documentado con OpenAPI.")
> >                         .contact(new Contact().name("Equipo Academy").email("academy@mail.com"))
> >                         .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
> >                 .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
> >                 .components(new Components().addSecuritySchemes("bearerAuth",
> >                         new SecurityScheme()
> >                                 .type(SecurityScheme.Type.HTTP)
> >                                 .scheme("bearer")
> >                                 .bearerFormat("JWT")));
> >     }
> >
> >     @Bean
> >     public GroupedOpenApi studentsGroup() {
> >         return GroupedOpenApi.builder()
> >                 .group("students")
> >                 .displayName("Students")
> >                 .pathsToMatch("/api/students/**")
> >                 .build();
> >     }
> >
> >     @Bean
> >     public GroupedOpenApi internalGroup() {
> >         return GroupedOpenApi.builder()
> >                 .group("internal")
> >                 .displayName("Internal")
> >                 .pathsToMatch("/internal/**")
> >                 .build();
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 InternalController.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/InternalController.java`
> >
> > ```java
> > package com.academy.presentation.controller;
> >
> > import com.academy.business.service.StudentService;
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.persistence.entity.StudentStatus;
> > import io.swagger.v3.oas.annotations.Operation;
> > import io.swagger.v3.oas.annotations.tags.Tag;
> > import org.springframework.web.bind.annotation.GetMapping;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.RequestMapping;
> > import org.springframework.web.bind.annotation.RestController;
> >
> > import java.util.Map;
> >
> > @RestController
> > @RequestMapping("/internal")
> > @Tag(name = "Internal", description = "Operaciones de apoyo para el equipo (no son parte de la API pública)")
> > public class InternalController {
> >
> >     private final StudentService service;
> >
> >     public InternalController(StudentService service) {
> >         this.service = service;
> >     }
> >
> >     @Operation(summary = "Estado del servicio")
> >     @GetMapping("/health")
> >     public Map<String, String> health() {
> >         return Map.of("status", "UP");
> >     }
> >
> >     @Operation(summary = "Cargar estudiantes de ejemplo", description = "Crea tres estudiantes si no existen.")
> >     @PostMapping("/seed")
> >     public Map<String, Integer> seed() {
> >         int created = 0;
> >         StudentEntity[] samples = {
> >                 new StudentEntity(null, "Ana Torres", 22, "ana@mail.com", StudentStatus.ACTIVE),
> >                 new StudentEntity(null, "Luis Pérez", 25, "luis@mail.com", StudentStatus.ACTIVE),
> >                 new StudentEntity(null, "María Gómez", 31, "maria@mail.com", StudentStatus.INACTIVE)
> >         };
> >         for (StudentEntity sample : samples) {
> >             if (service.create(sample).isPresent()) {
> >                 created++;
> >             }
> >         }
> >         return Map.of("created", created);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Pieza | Qué hace |
> > > |---|---|
> > > | `GroupedOpenApi.builder().group("students")` | Nombre técnico del grupo (`/v3/api-docs/students`) |
> > > | `.displayName("Students")` | Nombre que se ve en el selector |
> > > | `.pathsToMatch("/api/students/**")` | Qué rutas entran en el grupo |
> > > | `InternalController` | `GET /internal/health` y `POST /internal/seed` (carga tres estudiantes de ejemplo) |
> > >
> > > **Resultado real:**
> > >
> > > | Grupo | Rutas |
> > > |---|---|
> > > | `students` | `/api/students`, `/api/students/{id}`, `/api/students/by-email` |
> > > | `internal` | `/internal/seed`, `/internal/health` |
> > >
> > > 💡 **Beneficio:** cada audiencia ve **solo su parte**. El contrato público no mezcla operaciones de mantenimiento, y los clientes de la API de estudiantes no se enteran de `/internal`.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 10.2 · Ver el resultado</strong></summary>
>
> > Arriba a la derecha aparece **Select a definition** con `Internal` y `Students`. Al cambiar de grupo la lista cambia. Con `POST /internal/seed` se cargan tres estudiantes de ejemplo (respuesta real con Ana ya creada: `{"created":2}`).
> >
> > ✅ **Punto de control 10:** el selector muestra los dos grupos.
>
> </details>
>

</details>

---

<details>
<summary><strong>⚙️ Paso 11 — Configurar la interfaz, exportar el contrato y apagarlo</strong></summary>

> <details>
> <summary><strong>Paso 11.1 · Propiedades de springdoc</strong></summary>
>
> > Todo lo que no se hace con código se ajusta en `application.properties`:
> >
> > <details>
> > <summary><strong>📄 application.properties (agregar)</strong></summary>
> >
> > **Ruta:** `src/main/resources/application.properties`
> >
> > ```properties
> > # Swagger (springdoc)
> > springdoc.api-docs.path=/api-docs
> > springdoc.swagger-ui.path=/swagger-ui.html
> > springdoc.swagger-ui.urls-primary-name=Students
> > springdoc.swagger-ui.deep-linking=true
> > springdoc.swagger-ui.operations-sorter=method
> > springdoc.swagger-ui.tags-sorter=alpha
> > springdoc.swagger-ui.display-request-duration=true
> > springdoc.swagger-ui.doc-expansion=list
> > springdoc.swagger-ui.try-it-out-enabled=true
> > springdoc.default-produces-media-type=application/json
> > springdoc.default-consumes-media-type=application/json
> > ```
> >
> > </details>
> >
> > > | Propiedad | Para qué sirve |
> > > |---|---|
> > > | `springdoc.api-docs.path` | Dirección del contrato (por defecto `/v3/api-docs`) |
> > > | `springdoc.swagger-ui.path` | Dirección de la pantalla |
> > > | `springdoc.swagger-ui.urls-primary-name` | Grupo que se muestra **primero** (sin ella, el primero por orden alfabético: `Internal`) |
> > > | `springdoc.swagger-ui.deep-linking` | Permite enlazar a una operación concreta con `#/Students/post_api_students` |
> > > | `springdoc.swagger-ui.operations-sorter` | Orden de las operaciones: `alpha` (por ruta) o `method` (por método HTTP) |
> > > | `springdoc.swagger-ui.tags-sorter` | Orden de los grupos de etiquetas |
> > > | `springdoc.swagger-ui.display-request-duration` | Muestra cuánto tardó cada petición de prueba |
> > > | `springdoc.swagger-ui.doc-expansion` | `none` (todo cerrado), `list` (operaciones visibles) o `full` (todo abierto) |
> > > | `springdoc.swagger-ui.try-it-out-enabled` | Abre las operaciones ya en modo edición |
> > > | `springdoc.default-produces-media-type` · `default-consumes-media-type` | Formato por defecto de entrada y salida |
> > >
> > > **Comprobación real:** con `api-docs.path=/api-docs`, la dirección `/v3/api-docs` responde `404` y `/api-docs` responde el contrato.
> > >
> > > 💡 **Beneficio:** la presentación se ajusta **sin tocar el código** de los controladores.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 11.2 · Exportar el contrato</strong></summary>
>
> > El contrato es un archivo: se puede **descargar** y entregar a otros equipos o importar en otras herramientas.
> >
> > ```bash
> > curl localhost:8080/api-docs        -o openapi.json    # JSON
> > curl localhost:8080/api-docs.yaml   -o openapi.yaml    # YAML
> > curl localhost:8080/api-docs/students -o students.json # solo un grupo
> > ```
> >
> > **Resultado real** (inicio del YAML):
> >
> > ```text
> > openapi: 3.0.1
> > info:
> >   title: Academy API
> >   description: CRUD de estudiantes con Spring Boot, JPA y MySQL, documentado con OpenAPI.
> > ```
> >
> > > | Quién usa el archivo | Para qué |
> > > |---|---|
> > > | **Postman / Insomnia** | Importar la colección completa |
> > > | **Generadores de clientes** (OpenAPI Generator) | Crear el cliente en TypeScript, Java, etc. |
> > > | **Revisores / otros equipos** | Revisar el contrato **sin** ejecutar la aplicación |
> > >
> > > 💡 **Beneficio:** el contrato es un **entregable** versionable, no solo una pantalla.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 11.3 · Apagar Swagger donde no debe estar</strong></summary>
>
> > En producción, la documentación pública **revela la API completa**. Se desactiva con dos propiedades, mejor en un **perfil**:
> >
> > <details>
> > <summary><strong>📄 application-prod.properties</strong></summary>
> >
> > **Ruta:** `src/main/resources/application-prod.properties`
> >
> > ```properties
> > springdoc.api-docs.enabled=false
> > springdoc.swagger-ui.enabled=false
> > ```
> >
> > </details>
> >
> > > Se arranca con `--spring.profiles.active=prod`. **Resultado real** (con las dos propiedades en `false`):
> > >
> > > | Dirección | Respuesta |
> > > |---|---|
> > > | `/api-docs` | `404` |
> > > | `/swagger-ui.html` | `404` |
> > > | `/api/students` | `200` (la API **sigue funcionando**) |
> > >
> > > 💡 **Beneficio:** la misma aplicación documenta en desarrollo y se calla en producción, sin cambiar código.
> > >
> > > ⚠️ **Y si se necesita en producción**, se protege con autenticación; apagarlo es lo más simple y seguro por defecto.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 11.4 · Prueba de punta a punta</strong></summary>
>
> > Con todo activo se comprobó el contrato completo. **Resultado real** (lista de operaciones de `/api-docs`):
> >
> > | Método | Ruta | Resumen | Marcas | Respuestas |
> > |---|---|---|---|---|
> > | `GET` | `/api/students` | Listar estudiantes | | `200` |
> > | `POST` | `/api/students` | Crear un estudiante | | `201` · `400` · `409` |
> > | `GET` | `/api/students/{id}` | Buscar un estudiante por id | | `200` · `404` |
> > | `PUT` | `/api/students/{id}` | Actualizar un estudiante | | `200` · `400` · `404` · `409` |
> > | `DELETE` | `/api/students/{id}` | Eliminar un estudiante | 🔒 seguridad | `204` · `404` |
> > | `GET` | `/api/students/by-email` | Buscar un estudiante por correo | ~~obsoleta~~ | `200` · `404` |
> > | `GET` | `/internal/health` | Estado del servicio | | `200` |
> > | `POST` | `/internal/seed` | Cargar estudiantes de ejemplo | | `200` |
> >
> > Esquema: `Student`. Esquema de seguridad: `bearerAuth`. La ruta `/api/students/ping` **no aparece**.
> >
> > El controlador **final**, con todas las anotaciones juntas:
> >
> > <details>
> > <summary><strong>📄 StudentController.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > package com.academy.presentation.controller;
> >
> > import com.academy.business.service.StudentService;
> > import com.academy.persistence.entity.StudentEntity;
> > import io.swagger.v3.oas.annotations.Hidden;
> > import io.swagger.v3.oas.annotations.Operation;
> > import io.swagger.v3.oas.annotations.Parameter;
> > import io.swagger.v3.oas.annotations.media.Content;
> > import io.swagger.v3.oas.annotations.media.ExampleObject;
> > import io.swagger.v3.oas.annotations.media.Schema;
> > import io.swagger.v3.oas.annotations.responses.ApiResponse;
> > import io.swagger.v3.oas.annotations.security.SecurityRequirement;
> > import io.swagger.v3.oas.annotations.tags.Tag;
> > import jakarta.validation.Valid;
> > import org.springdoc.core.annotations.ParameterObject;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.http.ResponseEntity;
> > import org.springframework.web.bind.annotation.DeleteMapping;
> > import org.springframework.web.bind.annotation.GetMapping;
> > import org.springframework.web.bind.annotation.PathVariable;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.PutMapping;
> > import org.springframework.web.bind.annotation.RequestBody;
> > import org.springframework.web.bind.annotation.RequestMapping;
> > import org.springframework.web.bind.annotation.RequestParam;
> > import org.springframework.web.bind.annotation.RestController;
> >
> > import java.util.List;
> >
> > @RestController
> > @RequestMapping("/api/students")
> > @Tag(name = "Students", description = "Operaciones sobre estudiantes")
> > public class StudentController {
> >
> >     private final StudentService service;
> >
> >     public StudentController(StudentService service) {
> >         this.service = service;
> >     }
> >
> >     @Operation(summary = "Listar estudiantes",
> >             description = "Devuelve los estudiantes de la página pedida. Con `name` filtra por nombre (contiene, sin importar mayúsculas).")
> >     @ApiResponse(responseCode = "200", description = "Lista de estudiantes de la página pedida")
> >     @GetMapping
> >     public List<StudentEntity> search(
> >             @Parameter(description = "Parte del nombre a buscar", example = "ana")
> >             @RequestParam(required = false) String name,
> >             @ParameterObject Pageable pageable) {
> >         return service.search(name, pageable);
> >     }
> >
> >     @Operation(summary = "Buscar un estudiante por id")
> >     @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
> >     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
> >     @GetMapping("/{id}")
> >     public ResponseEntity<StudentEntity> findById(
> >             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
> >         return service.findById(id)
> >                 .map(ResponseEntity::ok)
> >                 .orElse(ResponseEntity.notFound().build());
> >     }
> >
> >     @Operation(summary = "Buscar un estudiante por correo",
> >             description = "Obsoleto: usar `GET /api/students?name=` o `GET /api/students/{id}`.",
> >             deprecated = true)
> >     @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
> >     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese correo", content = @Content)
> >     @GetMapping("/by-email")
> >     public ResponseEntity<StudentEntity> findByEmail(
> >             @Parameter(description = "Correo exacto", example = "ana@mail.com") @RequestParam String email) {
> >         return service.findByEmail(email)
> >                 .map(ResponseEntity::ok)
> >                 .orElse(ResponseEntity.notFound().build());
> >     }
> >
> >     @Operation(summary = "Crear un estudiante",
> >             description = "El `id` lo genera la base. Si el correo ya existe responde `409`.")
> >     @io.swagger.v3.oas.annotations.parameters.RequestBody(
> >             description = "Datos del estudiante",
> >             content = @Content(examples = {
> >                     @ExampleObject(name = "Estudiante activo", summary = "Un estudiante activo",
> >                             value = "{\"name\":\"Ana Torres\",\"age\":22,\"email\":\"ana@mail.com\",\"status\":\"ACTIVE\"}"),
> >                     @ExampleObject(name = "Estudiante inactivo", summary = "Un estudiante inactivo",
> >                             value = "{\"name\":\"Luis Pérez\",\"age\":25,\"email\":\"luis@mail.com\",\"status\":\"INACTIVE\"}")
> >             }))
> >     @ApiResponse(responseCode = "201", description = "Estudiante creado")
> >     @ApiResponse(responseCode = "400", description = "Datos inválidos (Bean Validation)", content = @Content)
> >     @ApiResponse(responseCode = "409", description = "El correo ya está registrado", content = @Content)
> >     @PostMapping
> >     public ResponseEntity<StudentEntity> create(@Valid @RequestBody StudentEntity student) {
> >         return service.create(student)
> >                 .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
> >                 .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
> >     }
> >
> >     @Operation(summary = "Actualizar un estudiante",
> >             description = "Reemplaza todos los datos del estudiante. Responde `404` si no existe y `409` si el correo es de otro estudiante.")
> >     @ApiResponse(responseCode = "200", description = "Estudiante actualizado")
> >     @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
> >     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
> >     @ApiResponse(responseCode = "409", description = "El correo pertenece a otro estudiante", content = @Content)
> >     @PutMapping("/{id}")
> >     public ResponseEntity<StudentEntity> update(
> >             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id,
> >             @Valid @RequestBody StudentEntity student) {
> >         if (service.findById(id).isEmpty()) {
> >             return ResponseEntity.notFound().build();
> >         }
> >         return service.update(id, student)
> >                 .map(ResponseEntity::ok)
> >                 .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
> >     }
> >
> >     @Operation(summary = "Eliminar un estudiante", security = @SecurityRequirement(name = "bearerAuth"))
> >     @ApiResponse(responseCode = "204", description = "Estudiante eliminado")
> >     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
> >     @DeleteMapping("/{id}")
> >     public ResponseEntity<Void> delete(
> >             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
> >         return service.delete(id)
> >                 ? ResponseEntity.noContent().build()
> >                 : ResponseEntity.notFound().build();
> >     }
> >
> >     @Hidden
> >     @GetMapping("/ping")
> >     public String ping() {
> >         return "pong";
> >     }
> > }
> > ```
> >
> > </details>
> >
> > ✅ **Punto de control 11:** el contrato coincide con la tabla.
>
> </details>
>

</details>

---

<details>
<summary><strong>🗺️ Mapa: funcionalidad de Swagger → cómo se usa</strong></summary>

> | Funcionalidad | Cómo se activa | Paso |
> |---|---|:-:|
> | Documentación automática | Dependencia `springdoc-openapi-starter-webmvc-ui` | 2 |
> | Probar desde el navegador | **Try it out** de Swagger UI | 2 |
> | Nombre, versión, contacto, licencia, servidores | Bean `OpenAPI` (o `@OpenAPIDefinition`) | 3 |
> | Agrupar operaciones por etiqueta | `@Tag` | 4 |
> | Resumen y descripción de cada operación | `@Operation` | 4 |
> | Respuestas posibles (`201`, `404`, `409`…) | `@ApiResponse` | 4 |
> | Descripción y ejemplo de un parámetro | `@Parameter` | 5 |
> | Desplegar un objeto (`Pageable`) en parámetros | `@ParameterObject` | 5 |
> | Descripción, ejemplo y obligatoriedad de un campo | `@Schema` (hoy en la entidad) | 6 |
> | Campo solo de lectura | `@Schema(accessMode = READ_ONLY)` | 6 |
> | Reglas de validación en el contrato | **Automático** desde Bean Validation | 6 |
> | Enumerados como desplegable | **Automático** desde `enum` | 6 |
> | Varios ejemplos del cuerpo | `@RequestBody` de Swagger + `@ExampleObject` | 7 |
> | Operación obsoleta | `@Operation(deprecated = true)` | 8 |
> | Ocultar operación o controlador | `@Hidden` | 8 |
> | Autenticación (*Authorize*) | `SecurityScheme` + `@SecurityRequirement` | 9 |
> | Varios contratos (grupos) | `GroupedOpenApi` | 10 |
> | Orden, expansión, rutas, grupo inicial | Propiedades `springdoc.*` | 11 |
> | Exportar el contrato (JSON/YAML) | `/api-docs`, `/api-docs.yaml`, `/api-docs/{grupo}` | 11 |
> | Apagar la documentación | `springdoc.api-docs.enabled` y `swagger-ui.enabled` | 11 |
>
> > 🔎 **Lectura clave.** De las 20 funcionalidades, **cuatro** funcionan sin escribir documentación (la dependencia, *Try it out*, las validaciones y los enumerados). El resto se agrega con anotaciones **solo en `presentation`** o con propiedades.

</details>

<details>
<summary><strong>🏁 Cierre — preguntas de síntesis</strong></summary>

> 1. ¿Qué diferencia hay entre OpenAPI, Swagger UI y springdoc? *(El contrato, la pantalla y la librería que genera el primero y sirve la segunda.)*
> 2. ¿Qué costo tiene documentar sobre la entidad? *(La entidad mezcla tabla, validación y contrato: renombrar un campo cambia la API. Con DTO, esas anotaciones se mueven al DTO.)*
> 3. ¿Qué parte de la documentación es automática y qué parte hay que escribir? *(Rutas, tipos, validaciones y enumerados son automáticos; descripciones, ejemplos y significados de los códigos los escribe una persona.)*
> 4. ¿Qué pasa con `@Email`? ¿Qué se aprende de eso? *(No llega al contrato; conviene **leer** el resultado y no suponer que todo pasa.)*
> 5. ¿`@Hidden` protege una ruta? *(No: solo la quita de la documentación.)*
> 6. ¿Qué hace realmente el botón *Authorize* aquí? *(Guarda un token y lo envía; como no hay Spring Security, nadie lo valida.)*
> 7. ¿Por qué se apaga Swagger en producción? *(Revela la API completa; se desactiva con propiedades y un perfil.)*

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Solución |
> |---|---|---|
> | `404` en `/swagger-ui.html` | Falta la dependencia, o `springdoc.swagger-ui.enabled=false` | Revisar el `pom.xml` y las propiedades |
> | `404` en `/v3/api-docs` | Se cambió `springdoc.api-docs.path` | Usar la ruta configurada |
> | `NoSuchMethodError` o fallos al arrancar con springdoc | Versión de springdoc incompatible con Spring Boot (1.x en Boot 3) | Usar `springdoc-openapi-starter-webmvc-ui` **2.x** con Boot 3 |
> | `Failed to load API definition` en la pantalla | El contrato no se genera (error en una anotación o en un controlador) | Abrir `/v3/api-docs` directamente y leer el mensaje |
> | El `Pageable` se muestra como un solo cuadro JSON | Falta `@ParameterObject` | Agregarlo |
> | El 409 aparece con un modelo de respuesta inexistente | Falta `content = @Content` en esa `@ApiResponse` | Agregarlo |
> | Dos `@RequestBody` en el mismo parámetro no compilan | Se importaron las dos | Usar el nombre completo de la de Swagger |
> | El `id` aparece en el ejemplo de la petición de crear | El `id` no está marcado como solo lectura | Agregar `accessMode = READ_ONLY` al `@Schema` del `id` |
> | Los cambios de documentación no se ven | Caché del navegador o no se reinició la aplicación | Reiniciar y recargar con `Ctrl+F5` |
> | `Communications link failure` al arrancar | MySQL no está arrancado o el puerto es otro | `docker ps` y revisar la URL |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **`@Email` en el contrato.** Agregar `format = "email"` al `@Schema` de `email` y comprobar que aparece en `/api-docs`.
> 2. **Caso de error con ejemplo.** Agregar un tercer `@ExampleObject` con nombre vacío para provocar el `400` desde Swagger UI.
> 3. **Un tag por controlador.** Dar a `InternalController` la etiqueta «Internal» y comprobar el orden con `tags-sorter`.
> 4. **Modelo de error.** Crear un `ErrorResponse` y documentarlo en el `409` con `content = @Content(schema = @Schema(implementation = ErrorResponse.class))`.
> 5. **Un segundo servidor.** Agregar `https://api.academy.com` como servidor de producción y probar el selector.
> 6. **Un recurso nuevo.** Agregar `CourseController` (CRUD de cursos) con su propio tag y su grupo.
> 7. **Seguridad real.** Agregar Spring Security con un token fijo y comprobar que *Authorize* ahora **sí** cambia el resultado de `DELETE`.
> 8. **Generar un cliente.** Con el YAML exportado, generar un cliente con OpenAPI Generator.
> 9. **Pasar a DTO.** Crear `StudentRequest` y `StudentResponse` (y un mapeador), mover los `@Schema` y las validaciones a ellos, y comprobar que el contrato queda igual salvo el nombre de los modelos. ¿Qué queda en la entidad?

</details>

<details>
<summary><strong>📁 Estructura final esperada</strong></summary>

> ```text
> 📁 students-api
> ├── 📄 pom.xml
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 presentation
>     │   │   ├── 📁 config        OpenApiConfig
>     │   │   └── 📁 controller    StudentController · InternalController
>     │   ├── 📁 business
>     │   │   └── 📁 service       StudentService
>     │   └── 📁 persistence
>     │       ├── 📁 entity        StudentEntity · StudentStatus
>     │       └── 📁 repository    StudentJpaRepository
>     └── 📁 resources
>         ├── 📄 application.properties
>         └── 📄 application-prod.properties
> ```

</details>

<details>
<summary><strong>🏷️ Repaso de anotaciones de Swagger (OpenAPI)</strong></summary>

> | Anotación | Dónde se usa | Para qué sirve |
> |---|---|---|
> | `@Tag` | Clase del controlador | Nombre y descripción del grupo de operaciones |
> | `@Operation` | Método | Resumen, descripción, `deprecated` y seguridad |
> | `@ApiResponse` | Método (repetible) | Una respuesta posible |
> | `@Content` · `@Schema` · `@ExampleObject` | Dentro de las anteriores | Cuerpo vacío, modelo y ejemplos |
> | `@Parameter` | Parámetro del método | Descripción y ejemplo |
> | `@ParameterObject` | Parámetro `Pageable` u objeto | Despliega sus campos como parámetros |
> | `@io.swagger…RequestBody` | Método | Documenta el cuerpo (con ejemplos) |
> | `@Schema` | Clase y campos de la entidad (luego, del DTO) | Descripción, ejemplo, obligatorio, solo lectura |
> | `@Hidden` | Método o clase | Quita la operación del contrato |
> | `@SecurityRequirement` | Método o clase | Exige un esquema de seguridad |
> | Beans `OpenAPI` y `GroupedOpenApi` | `presentation.config` | Información general, seguridad y grupos |
>
> ```text
> persistence  ──▶  @Schema (temporal: pasará a los DTO)
> business     ──▶  (sin Swagger)
> presentation ──▶  @Tag · @Operation · @ApiResponse · @Parameter · @ParameterObject · @Hidden · @SecurityRequirement + config
> ```
>
> > 🔎 **Lectura clave.** Swagger vive en `presentation`; solo `@Schema` está, por ahora, en la entidad. `business` no sabe que existe.

</details>

➡️ **Anterior:** [Guía 01 del módulo 03-04 — Taller «Gestión de cursos» con base de datos](../modulo-03-04/01-guia-taller-gestion-de-cursos-con-bd.md)
