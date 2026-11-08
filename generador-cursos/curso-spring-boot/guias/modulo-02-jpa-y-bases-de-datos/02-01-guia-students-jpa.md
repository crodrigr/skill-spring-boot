# 🧱 02-01-Guía paso a paso — Taller de estudiantes (punto de partida del Módulo 03)

> 👩‍🏫 **Material docente.** Es la versión **reducida** de la [guía 02-02 (proyecto académico JPA completo)](02-02-guia-proyecto-academico-jpa-completo.md): se conserva **solo `Student`** y se completa con un CRUD (crear, listar, buscar, reemplazar y borrar). El resultado es el **punto de partida** de la guía [03-01 DTO y estructura de controladores](../modulo-03-diseno-api-rest-con-dto/03-01-guia-dto-y-estructura-de-controladores.md).
>
> 📦 **Proyecto terminado:** [`codigo/02-01-students-base/`](codigo/02-01-students-base/) (listo para ejecutar con `./mvnw spring-boot:run`).
>
> ✅ **Verificada.** El código se compiló para Java 21 y se ejecutó con Spring Boot 4.1.2 y MySQL 8; las respuestas HTTP de la tabla del Paso 5 son salidas reales.
>
> 🧭 **Idea central.** Tres capas con la capa de negocio **desacoplada**: el controlador conoce la **interfaz** `StudentService`, y Spring le entrega `StudentServiceImpl`. El controlador, a propósito, todavía **recibe y devuelve la entidad**: ese es el problema que el Módulo 03 resuelve con DTO.

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Dejar listo un CRUD de estudiantes con Spring Data JPA y MySQL, organizado en `presentation` → `business` → `persistence`.
>
> Al terminar, el estudiante:
>
> - Declara la entidad `StudentEntity` con sus **validaciones** (`@NotBlank`, `@Email`, `@Min`, `@Max`) y un `enum` de estado.
> - Escribe un `JpaRepository` con **métodos derivados** (`existsByEmailIgnoreCase`, `findByNameContainingIgnoreCase`…).
> - Define el servicio como **interfaz** (`business.service`) con su **implementación** `@Service` (`business.impl`).
> - Expone la API con un controlador que usa la **interfaz** y elige el código HTTP (`201`, `404`, `409`…) a partir del `Optional`.
> - Reconoce los **límites** de devolver la entidad (se retoman en el Módulo 03).

</details>

<details>
<summary><strong>🛠️ Requerimientos y restricciones</strong></summary>

> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Java 21 o superior, Spring Boot 4.1.2 y Maven. |
> | **RNF-02** | **MySQL 8** (Docker o instalado). La base `academia` se crea sola (`createDatabaseIfNotExist=true`). |
> | **RNF-03** | Tres capas: `presentation` → `business` → `persistence`. Las anotaciones de JPA van **solo** en `persistence.entity`; las de Spring Web, **solo** en `presentation.controller`. |
> | **RNF-04** | La capa `business` está desacoplada: `StudentService` (interfaz) en `business.service` y `StudentServiceImpl` en `business.impl`. El controlador inyecta la **interfaz**. |
> | **RNF-05** | Inyección por constructor y nombres del código en **inglés**. |
> | **RNF-06** | Duración: 1 h. |
>
> > 💡 **Alcance.** Quedan fuera los DTO, los mapeadores, `ServiceResult`, las excepciones propias y la seguridad: son los temas de los módulos siguientes.

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>

> | Paso | Minutos | Actividad |
> |---|---:|---|
> | 0 | 0 – 10 | Proyecto, dependencias, `application.properties` y arranque |
> | 1 | 10 – 20 | Entidad `StudentEntity` y `StudentStatus` |
> | 2 | 20 – 25 | Repositorio `StudentJpaRepository` |
> | 3 | 25 – 40 | Negocio: interfaz `StudentService` e implementación `StudentServiceImpl` |
> | 4 | 40 – 50 | Presentación: `StudentController` |
> | 5 | 50 – 60 | Pruebas con `curl` y cierre |

</details>

<details>
<summary><strong>🏗️ Arquitectura: el árbol de archivos</strong></summary>

> ```text
> 📁 src/main/java/com/academy
> ├── 📄 AcademyApplication.java                  (la genera start.spring.io; no se modifica)
> ├── 📁 presentation                              ← Paso 4
> │   └── 📁 controller
> │       └── 📄 StudentController.java
> ├── 📁 business                                  ← Paso 3
> │   ├── 📁 service                               (➕ la interfaz: el contrato)
> │   │   └── 📄 StudentService.java
> │   └── 📁 impl                                  (➕ la implementación @Service)
> │       └── 📄 StudentServiceImpl.java
> └── 📁 persistence                               ← Pasos 1 y 2
>     ├── 📁 entity
>     │   ├── 📄 StudentStatus.java
>     │   └── 📄 StudentEntity.java
>     └── 📁 repository
>         └── 📄 StudentJpaRepository.java
> 📁 src/main/resources
> └── 📄 application.properties                    (➕ conexión a MySQL y JPA)
> ```
>
> ```text
> @RestController ─▶ StudentService ─▶ StudentServiceImpl ─▶ StudentJpaRepository ─▶ Hibernate ─▶ MySQL
>  HTTP / JSON       (interfaz)        @Service · reglas      Spring Data lo genera    genera el SQL   la base
> ```

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Proyecto y conexión</strong></summary>

> **MySQL.** Con Docker:
>
> ```bash
> docker run -d --name academy-mysql \
>   -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academia \
>   -p 3306:3306 mysql:8.0
> ```
>
> **Proyecto.** En [start.spring.io](https://start.spring.io): Maven · Java 21 · Group `com.academy` · Artifact `students-api-base` · dependencias **Spring Web**, **Spring Data JPA**, **Validation** y **MySQL Driver**.
>
> Dentro de `src/main/java/com/academy/` se crean los paquetes `presentation.controller`, `business.service`, `business.impl`, `persistence.entity` y `persistence.repository`.
>
> <details>
> <summary><strong>📄 application.properties</strong></summary>
>
> **Ruta:** `src/main/resources/application.properties`
>
> ```properties
> # MySQL connection
> spring.datasource.url=jdbc:mysql://localhost:3306/academia?createDatabaseIfNotExist=true
> spring.datasource.username=root
> spring.datasource.password=root
>
> # JPA and Hibernate
> spring.jpa.hibernate.ddl-auto=create
> spring.jpa.show-sql=true
> ```
>
> </details>
>
> > ⚠️ `ddl-auto=create` **borra y recrea** las tablas en cada arranque: sirve para el taller, no para producción.
>
> ✅ **Punto de control 0:** la aplicación arranca (`Started AcademyApplication`) y sigue en ejecución.

</details>

---

<details>
<summary><strong>🗃️ Paso 1 — Entidad (<code>persistence.entity</code>)</strong></summary>

> `StudentEntity` representa la tabla `student`. Lleva las anotaciones de JPA y, en esta versión, también las **validaciones** de Bean Validation (el Módulo 03 las moverá al DTO). `StudentStatus` es un `enum` que se guarda como texto (`EnumType.STRING`).
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
> import jakarta.validation.constraints.Email;
> import jakarta.validation.constraints.Max;
> import jakarta.validation.constraints.Min;
> import jakarta.validation.constraints.NotBlank;
> import jakarta.validation.constraints.NotNull;
> import jakarta.validation.constraints.Size;
>
> @Entity
> @Table(name = "student")
> public class StudentEntity {
>
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>
>     @NotBlank
>     @Size(min = 2, max = 100)
>     @Column(nullable = false)
>     private String name;
>
>     @Min(16)
>     @Max(99)
>     private int age;
>
>     @NotBlank
>     @Email
>     @Column(nullable = false, unique = true)
>     private String email;
>
>     @NotNull
>     @Enumerated(EnumType.STRING)
>     @Column(nullable = false)
>     private StudentStatus status;
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
> }
> ```
>
> </details>
>
> | Anotación | Para qué sirve |
> |---|---|
> | `@Entity` · `@Table(name = "student")` | La clase es una tabla; se fija su nombre. |
> | `@Id` · `@GeneratedValue(IDENTITY)` | Clave primaria autoincremental generada por MySQL. |
> | `@Column(nullable = false, unique = true)` | Restricciones en la base; `unique` evita correos repetidos. |
> | `@Enumerated(EnumType.STRING)` | Guarda `ACTIVE`/`INACTIVE` como texto, no como número. |
> | `@NotBlank` · `@Size` · `@Email` · `@Min` · `@Max` · `@NotNull` | Validan el JSON cuando el controlador usa `@Valid`. |
>
> ✅ **Punto de control 1:** el proyecto compila.

</details>

---

<details>
<summary><strong>🔌 Paso 2 — Repositorio (<code>persistence.repository</code>)</strong></summary>

> Una **interfaz** que extiende `JpaRepository`: Spring Data crea la implementación al arrancar. Los métodos derivados se leen por su nombre.
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
> | Método | Consulta que genera |
> |---|---|
> | `findByNameContainingIgnoreCase` | `where upper(name) like upper('%…%')`, con paginación |
> | `findByEmailIgnoreCase` | Busca por correo sin distinguir mayúsculas |
> | `existsByEmailIgnoreCase` | ¿Existe ya ese correo? (al crear) |
> | `existsByEmailIgnoreCaseAndIdNot` | ¿Lo usa **otro** estudiante? (al reemplazar) |
>
> ✅ **Punto de control 2:** el proyecto arranca y aparece `create table student`.

</details>

---

<details>
<summary><strong>⚙️ Paso 3 — Negocio desacoplado (<code>business.service</code> y <code>business.impl</code>)</strong></summary>

> El servicio aplica las **reglas del negocio** y no sabe nada de HTTP. Se separa en dos piezas:
>
> - **Interfaz** (`business.service`): el **contrato**, qué operaciones existen.
> - **Implementación** (`business.impl`): cómo se cumplen; lleva `@Service` y `@Transactional`.
>
> Cuando una regla no se cumple, la implementación devuelve un `Optional` vacío.
>
> | Regla | Dónde se aplica |
> |---|---|
> | El correo no puede repetirse (sin importar mayúsculas) | `create`, `update` |
> | El `id` que llegue del cliente se descarta al crear | `create` |
> | Solo se reemplaza o borra lo que existe | `update`, `delete` |
>
> <details>
> <summary><strong>📄 StudentService.java</strong> <em>(interfaz)</em></summary>
>
> **Ruta:** `src/main/java/com/academy/business/service/StudentService.java`
>
> ```java
> package com.academy.business.service;
>
> import com.academy.persistence.entity.StudentEntity;
> import org.springframework.data.domain.Pageable;
>
> import java.util.List;
> import java.util.Optional;
>
> public interface StudentService {
>
>     List<StudentEntity> search(String name, Pageable pageable);
>
>     Optional<StudentEntity> findById(Long id);
>
>     Optional<StudentEntity> findByEmail(String email);
>
>     Optional<StudentEntity> create(StudentEntity student);
>
>     Optional<StudentEntity> update(Long id, StudentEntity data);
>
>     boolean delete(Long id);
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentServiceImpl.java</strong> <em>(implementación)</em></summary>
>
> **Ruta:** `src/main/java/com/academy/business/impl/StudentServiceImpl.java`
>
> ```java
> package com.academy.business.impl;
>
> import com.academy.business.service.StudentService;
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.persistence.repository.StudentJpaRepository;
> import org.springframework.data.domain.Pageable;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
>
> import java.util.List;
> import java.util.Optional;
>
> @Service
> public class StudentServiceImpl implements StudentService {
>
>     private final StudentJpaRepository students;
>
>     public StudentServiceImpl(StudentJpaRepository students) {
>         this.students = students;
>     }
>
>     @Override
>     @Transactional(readOnly = true)
>     public List<StudentEntity> search(String name, Pageable pageable) {
>         if (name == null || name.isBlank()) {
>             return students.findAll(pageable).getContent();
>         }
>         return students.findByNameContainingIgnoreCase(name, pageable).getContent();
>     }
>
>     @Override
>     @Transactional(readOnly = true)
>     public Optional<StudentEntity> findById(Long id) {
>         return students.findById(id);
>     }
>
>     @Override
>     @Transactional(readOnly = true)
>     public Optional<StudentEntity> findByEmail(String email) {
>         return students.findByEmailIgnoreCase(email);
>     }
>
>     @Override
>     @Transactional
>     public Optional<StudentEntity> create(StudentEntity student) {
>         if (students.existsByEmailIgnoreCase(student.getEmail())) {
>             return Optional.empty();
>         }
>         StudentEntity toSave = new StudentEntity(
>                 null, student.getName(), student.getAge(), student.getEmail(), student.getStatus());
>         return Optional.of(students.save(toSave));
>     }
>
>     @Override
>     @Transactional
>     public Optional<StudentEntity> update(Long id, StudentEntity data) {
>         if (!students.existsById(id)) {
>             return Optional.empty();
>         }
>         if (students.existsByEmailIgnoreCaseAndIdNot(data.getEmail(), id)) {
>             return Optional.empty();
>         }
>         return Optional.of(students.save(new StudentEntity(
>                 id, data.getName(), data.getAge(), data.getEmail(), data.getStatus())));
>     }
>
>     @Override
>     @Transactional
>     public boolean delete(Long id) {
>         if (!students.existsById(id)) {
>             return false;
>         }
>         students.deleteById(id);
>         return true;
>     }
> }
> ```
>
> </details>
>
> - **`implements` + `@Override`**: el compilador verifica que se cumpla todo el contrato.
> - **`@Service`** va en la `Impl`; al haber una sola implementación, Spring la entrega donde se pida `StudentService`.
> - **`@Transactional`** abre y cierra la transacción (con `readOnly = true` en las consultas). Va en el servicio, nunca en el controlador.
> - `create` construye una entidad **nueva con `id` en `null`**: así nadie inserta con un `id` impuesto.
>
> > 🗣️ **Pregunta:** ¿qué se gana con la interfaz? *(El controlador depende del contrato: se puede cambiar la implementación o sustituirla por un doble en las pruebas sin tocar la capa de presentación.)*
>
> ✅ **Punto de control 3:** el proyecto compila. Aún no se puede probar: falta el controlador.

</details>

---

<details>
<summary><strong>🌐 Paso 4 — Presentación (<code>presentation.controller</code>)</strong></summary>

> El controlador recibe la petición, **delega** en el servicio y traduce el `Optional` a un código HTTP. Declara `StudentService` (la **interfaz**) e ignora que existe `StudentServiceImpl`.
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
> import com.academy.persistence.entity.StudentEntity;
> import jakarta.validation.Valid;
> import org.springframework.data.domain.Pageable;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.DeleteMapping;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PathVariable;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.PutMapping;
> import org.springframework.web.bind.annotation.RequestBody;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RequestParam;
> import org.springframework.web.bind.annotation.RestController;
>
> import java.util.List;
>
> @RestController
> @RequestMapping("/api/students")
> public class StudentController {
>
>     private final StudentService service;
>
>     public StudentController(StudentService service) {
>         this.service = service;
>     }
>
>     @GetMapping
>     public List<StudentEntity> search(@RequestParam(required = false) String name, Pageable pageable) {
>         return service.search(name, pageable);
>     }
>
>     @GetMapping("/{id}")
>     public ResponseEntity<StudentEntity> findById(@PathVariable Long id) {
>         return service.findById(id)
>                 .map(ResponseEntity::ok)
>                 .orElse(ResponseEntity.notFound().build());
>     }
>
>     @GetMapping("/by-email")
>     public ResponseEntity<StudentEntity> findByEmail(@RequestParam String email) {
>         return service.findByEmail(email)
>                 .map(ResponseEntity::ok)
>                 .orElse(ResponseEntity.notFound().build());
>     }
>
>     @PostMapping
>     public ResponseEntity<StudentEntity> create(@Valid @RequestBody StudentEntity student) {
>         return service.create(student)
>                 .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
>                 .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
>     }
>
>     @PutMapping("/{id}")
>     public ResponseEntity<StudentEntity> update(@PathVariable Long id, @Valid @RequestBody StudentEntity student) {
>         if (service.findById(id).isEmpty()) {
>             return ResponseEntity.notFound().build();
>         }
>         return service.update(id, student)
>                 .map(ResponseEntity::ok)
>                 .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
>     }
>
>     @DeleteMapping("/{id}")
>     public ResponseEntity<Void> delete(@PathVariable Long id) {
>         return service.delete(id)
>                 ? ResponseEntity.noContent().build()
>                 : ResponseEntity.notFound().build();
>     }
> }
> ```
>
> </details>
>
> | Método | Ruta | Respuesta |
> |---|---|---|
> | `GET` | `/api/students?name=&page=&size=` | `200` con un arreglo |
> | `GET` | `/api/students/{id}` | `200` · `404` |
> | `GET` | `/api/students/by-email?email=` | `200` · `404` |
> | `POST` | `/api/students` | `201` · `409` si el correo existe · `400` si es inválido |
> | `PUT` | `/api/students/{id}` | `200` · `404` · `409` |
> | `DELETE` | `/api/students/{id}` | `204` · `404` |
>
> ✅ **Punto de control 4:** el proyecto compila y arranca.

</details>

---

<details>
<summary><strong>🧪 Paso 5 — Pruebas con <code>curl</code></strong></summary>

> Con la aplicación en marcha (`./mvnw spring-boot:run`; en Windows, `mvnw.cmd spring-boot:run`):
>
> ```bash
> curl -i -X POST localhost:8080/api/students -H 'Content-Type: application/json' \
>   -d '{"name":"Ana","age":20,"email":"ana@x.com","status":"ACTIVE"}'
> curl -i "localhost:8080/api/students?name=an&page=0&size=5"
> curl -i localhost:8080/api/students/1
> curl -i -X PUT localhost:8080/api/students/1 -H 'Content-Type: application/json' \
>   -d '{"name":"Ana M","age":21,"email":"ana@x.com","status":"INACTIVE"}'
> curl -i -X DELETE localhost:8080/api/students/1
> ```
>
> **Resultados reales** (MySQL 8):
>
> | Petición | Respuesta |
> |---|---|
> | `POST` con datos válidos | `201` con `{"id":1,"name":"Ana","age":20,"email":"ana@x.com","status":"ACTIVE"}` |
> | `POST` con el mismo correo en mayúsculas (`ANA@x.com`) | `409` |
> | `POST` con `name` corto, `age` 5 y correo inválido | `400` |
> | `GET /api/students?name=an&page=0&size=5` | `200` con un arreglo de un estudiante |
> | `GET /api/students/1` · `/by-email?email=ANA@x.com` | `200` |
> | `GET /api/students/99` | `404` |
> | `PUT /api/students/1` con otro estado | `200` con el estudiante actualizado |
> | `DELETE /api/students/1` (la primera vez · la segunda) | `204` · `404` |
>
> ✅ **Punto de control 5:** las respuestas coinciden con la tabla.

</details>

---

<details>
<summary><strong>🔭 Cierre: qué problemas deja este punto de partida (Módulo 03)</strong></summary>

> Este CRUD funciona, pero el controlador **expone la entidad**:
>
> | Problema | Qué pasa |
> |---|---|
> | Se filtran datos internos | Un campo nuevo en la tabla (`createdAt`) aparece solo en el JSON. |
> | Asignación masiva | El cliente puede enviar campos que no debe. |
> | API atada a la tabla | Renombrar una columna rompe a los clientes. |
> | Validaciones mezcladas con `@Entity` | Dos responsabilidades en una clase. |
> | `Optional` vacío ambiguo | No dice **por qué** falló (no existe o está repetido). |
>
> La guía [03-01 DTO y estructura de controladores](../modulo-03-diseno-api-rest-con-dto/03-01-guia-dto-y-estructura-de-controladores.md) parte de este proyecto y los resuelve con DTO, mapeador, `ServiceResult` y endpoints mejor diseñados.

</details>

<details>
<summary><strong>🏷️ Repaso de las anotaciones</strong></summary>

> | Anotación | Dónde | Para qué sirve |
> |---|---|---|
> | `@Entity` · `@Table` · `@Id` · `@GeneratedValue` · `@Column` · `@Enumerated` | `StudentEntity` | Mapeo a la tabla `student`. |
> | `@NotBlank` · `@Size` · `@Email` · `@Min` · `@Max` · `@NotNull` | `StudentEntity` | Validaciones del JSON. |
> | `@Service` | `StudentServiceImpl` | Registra la implementación como *bean*; la interfaz no se anota. |
> | `@Transactional` | Métodos de `StudentServiceImpl` | Una operación se cumple completa o no se cumple. |
> | `@RestController` · `@RequestMapping` · `@GetMapping` · `@PostMapping` · `@PutMapping` · `@DeleteMapping` | `StudentController` | Atienden HTTP y devuelven JSON. |
> | `@RequestBody` · `@PathVariable` · `@RequestParam` · `@Valid` | Parámetros del controlador | Cuerpo JSON, ruta, consulta y validación. |
> | `@SpringBootApplication` | `AcademyApplication` | Arranca Spring y activa el escaneo. |

</details>
