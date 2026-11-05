# 🧱 Guía paso a paso — DTO y una mejor estructura de controladores (endpoints)

> 👩‍🏫 **Material docente.** Se parte de un CRUD de **estudiantes** con Spring Data JPA y MySQL (el estilo de la [guía 01 del taller de cursos](../modulo-02-jpa-y-bases-de-datos/01-guia-taller-gestion-de-cursos-con-bd.md)) cuyo controlador **recibe y devuelve la entidad**, y se mejora paso a paso: **DTO**, mapeador, servicio que informa el resultado y controlador delgado con endpoints bien diseñados.
> Contiene código completo: no se entrega a los estudiantes antes de resolver el taller.
>
> 📦 **Proyecto terminado:** [`students-api-dto/`](codigo/students-api-dto/) (código final, listo para ejecutar con `./mvnw spring-boot:run`).
>
> ✅ **Verificada.** El código se compiló para Java 21 y se ejecutó con Spring Boot 4.1.2 y MySQL 8. Las respuestas HTTP, los encabezados que se muestran son salidas reales.
>
> 🧭 **Idea central.** La **entidad** describe cómo se guarda un dato; el **DTO** describe cómo viaja por la API. Son dos contratos distintos que cambian por razones distintas. **Cada paso explica qué se gana con ese cambio.**

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Separar el **contrato de la API** de la **tabla de la base de datos** y dejar los controladores **delgados**, con endpoints coherentes.
>
> Al terminar, el estudiante:
>
> - Explica **qué es un DTO**, qué problema resuelve y cuándo **no** hace falta.
> - Distingue **DTO de entrada** (`Request`) y **de salida** (`Response`) y escribe uno por caso de uso.
> - Escribe un **mapeador** que concentra las conversiones entre DTO y entidad.
> - Mueve las **validaciones** de la entidad al DTO de entrada.
> - Diseña endpoints con **versión** en la ruta, **verbos** y **códigos** correctos, `Location` al crear y una **acción parcial** con `PATCH`.
> - Devuelve **páginas** con un envoltorio y **limita** el tamaño máximo.
> - Mantiene el controlador **delgado**: traduce HTTP, no decide reglas.
> - Separa el servicio en una **interfaz** (el contrato) y su **implementación** (`…Impl`), de modo que el controlador depende del contrato.

</details>

<details>
<summary><strong>📝 Introducción: qué es un DTO</strong></summary>

> <details>
> <summary><strong>📦 La idea</strong></summary>
>
> **DTO** (*Data Transfer Object*) es una clase **solo de datos** que se usa para **transportar** información entre capas o entre sistemas. En una API REST, es la forma exacta de lo que **entra** (`Request`) y de lo que **sale** (`Response`).
>
> ```text
> JSON  ─▶  StudentRequest  ─▶ [Mapper] ─▶  StudentEntity  ─▶ tabla
> JSON  ◀─  StudentResponse ◀─ [Mapper] ◀─  StudentEntity  ◀─ tabla
>           └── lo que ve el cliente ──┘    └── lo que ve la base ──┘
> ```
>
> </details>
>
> <details>
> <summary><strong>🔍 Entidad frente a DTO</strong></summary>
>
> | | Entidad (`StudentEntity`) | DTO (`StudentRequest` / `StudentResponse`) |
> |---|---|---|
> | Responde a | **La base de datos** | **El cliente de la API** |
> | Cambia cuando | Cambia la tabla (columnas, relaciones) | Cambia el contrato (campos que se envían o muestran) |
> | Lleva | `@Entity`, `@Id`, `@Column`… | Validaciones (`@NotBlank`…) en el de entrada |
> | Puede tener | Campos internos (`created_at`, versión, notas) | **Solo** lo que el cliente debe ver o enviar |
> | Se parece a | La tabla | Un formulario o una respuesta |
>
> </details>
>
> <details>
> <summary><strong>🧩 Tipos de DTO que se usan aquí</strong></summary>
>
> | DTO | Para qué | Campos |
> |---|---|---|
> | `StudentRequest` | Crear (`POST`) y reemplazar (`PUT`) | `name`, `age`, `email`, `status` |
> | `StudentStatusRequest` | Cambiar **solo** el estado (`PATCH`) | `status` |
> | `StudentResponse` | Toda respuesta con un estudiante | `id`, `name`, `age`, `email`, `status` |
> | `PageResponse<T>` | Una página de resultados | `content`, `page`, `size`, `totalElements`, `totalPages` |
>
> </details>
>
> <details>
> <summary><strong>⚖️ El costo</strong></summary>
>
> Más clases (cuatro DTO y un mapeador) y campos repetidos. En un CRUD pequeño que **nadie más consume** y que no tiene campos internos, devolver la entidad es una opción válida. El DTO compensa cuando la API la usan otros equipos, cuando la tabla tiene campos internos o cuando se quiere cambiar la base sin romper a los clientes.
>
> </details>
>
> <details>
> <summary><strong>🛠️ Requerimientos y restricciones</strong></summary>
>
> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Java 21 o superior, Spring Boot 4.1.2 y Maven. MySQL 8 con la base `academy`. |
> | **RNF-02** | **El controlador no importa nada de `jakarta.persistence`** y nunca devuelve una entidad. |
> | **RNF-03** | Los DTO son `record` (inmutables). Las validaciones viven **solo** en el DTO de entrada. |
> | **RNF-04** | Los errores de negocio se informan con un **resultado** (`ServiceResult`); traducirlos con excepciones y un manejador global se estudia en el Módulo 7. |
> | **RNF-05** | Todos los nombres del código en **inglés**. Inyección por constructor. |
> | **RNF-06** | Duración: 1 h 30 min. |
>
> </details>

</details>

<details>
<summary><strong>💬 En palabras simples: de qué trata esta mejora</strong></summary>

> <details>
> <summary><strong>🍽️ El problema: mostrar la cocina al cliente</strong></summary>
>
> Hasta ahora el controlador recibía y devolvía `StudentEntity`, la clase que representa la **tabla**. Es como un restaurante que saca la cocina a la mesa: el cliente ve todo, incluso lo que no debería, y hasta puede tocarlo.
>
> | Problema | Qué pasa |
> |---|---|
> | **Se filtran datos internos** | Si se agrega `createdAt` a la tabla, aparece solo en el JSON, aunque nadie lo decidió. |
> | **El cliente manda campos que no debe** | Puede enviar `"id": 99` o un `createdAt` falso y la entidad los acepta (*asignación masiva*). |
> | **La API queda atada a la tabla** | Si se renombra una columna, se rompen los clientes. |
> | **Las validaciones se mezclan** | `@NotBlank` y `@Email` conviven con `@Entity` y `@Column`. |
>
> </details>
>
> <details>
> <summary><strong>📋 La solución: el DTO es el menú del restaurante</strong></summary>
>
> Un **DTO** es una clase pequeña que describe **solo lo que viaja** entre el cliente y la API. El menú muestra lo que se puede pedir y lo que se entrega, sin exponer la cocina.
>
> - **`StudentRequest`**: lo que el cliente envía. No tiene `id`, así que no puede mandarlo.
> - **`StudentStatusRequest`**: lo que se envía para cambiar solo el estado.
> - **`StudentResponse`**: lo que la API devuelve. No incluye `createdAt`.
> - **`PageResponse<T>`**: un formato propio para listas paginadas.
>
> </details>
>
> <details>
> <summary><strong>🔁 El mapeador: el mesero que traduce</strong></summary>
>
> Alguien debe convertir `StudentRequest` en entidad y la entidad en `StudentResponse`. Esa tarea vive en **un solo lugar**, `StudentMapper`. Si cambia un campo, se edita ahí y no en cada método del controlador.
>
> </details>
>
> <details>
> <summary><strong>🌐 Controladores más delgados</strong></summary>
>
> Un controlador hace tres cosas: **recibe** la petición, **llama** al servicio y **responde**. No contiene reglas de negocio. El servicio devuelve un `ServiceResult` (`OK`, `NOT_FOUND` o `CONFLICT`) y el método `respond(...)` lo traduce a un código HTTP.
>
> > 💡 `ServiceResult` es una solución **temporal**: en el Módulo 7 se reemplaza por excepciones y un manejador global.
>
> </details>
>
> <details>
> <summary><strong>🧭 Endpoints mejor diseñados</strong></summary>
>
> | Mejora | Por qué importa |
> |---|---|
> | `/api/v1/students` | Permite una versión 2 sin romper a los clientes de la 1. |
> | `POST` devuelve `201` y `Location` | El cliente sabe dónde quedó el recurso nuevo. |
> | `DELETE` devuelve `204` | Se borró y no hay nada más que devolver. |
> | `404` si no existe, `409` si el email está repetido | Cada situación tiene su código. |
> | `PATCH /{id}/status` | Cambiar un dato no exige reenviar todo el objeto. |
> | Tamaño de página máximo de 50 | Con `?size=1000` la API devuelve `"size": 50`. |
>
> </details>
>
> <details>
> <summary><strong>⚖️ Antes y después</strong></summary>
>
> | | Antes | Después |
> |---|---|---|
> | Contrato de la API | La tabla | Lo que se decide mostrar |
> | Campos internos | Se filtran | Quedan ocultos |
> | Lo que el cliente puede enviar | Cualquier campo | Solo lo permitido |
> | Validaciones | Dentro de la entidad | En el DTO |
> | Controlador | Lógica mezclada | Recibe, delega y responde |
> | Cambiar la tabla | Rompe la API | La API no cambia |
>
> </details>

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>

> | Paso | Minutos | Actividad |
> |---|---:|---|
> | 0 | 0 – 15 | Punto de partida: el CRUD que devuelve la entidad |
> | 1 | 15 – 25 | **Ver los problemas** de exponer la entidad |
> | 2 | 25 – 35 | Los DTO |
> | 3 | 35 – 42 | El mapeador |
> | 4 | 42 – 52 | El servicio informa el resultado (`ServiceResult`) y se separa en interfaz + implementación |
> | 5 | 52 – 65 | El controlador delgado |
> | 6 | 65 – 72 | Diseño de endpoints: versión, `Location`, `PATCH` y límites |
> | 7 | 72 – 90 | Probar la API |
>
> > 💡 **Ritmo.** El Paso 0 puede entregarse ya escrito. El tiempo se dedica a los Pasos 1 a 7.

</details>

<details>
<summary><strong>🏗️ Arquitectura: el árbol de archivos (🆕 nuevo · ✏️ cambia)</strong></summary>

> ```text
> 📁 src/main/java/com/academy
> ├── 📄 AcademyApplication.java
> ├── 📁 presentation
> │   ├── 📁 controller
> │   │   └── ✏️ StudentController.java        delgado: HTTP ↔ DTO
> │   ├── 📁 dto                               🆕
> │   │   ├── 🆕 StudentRequest.java           entrada: crear y reemplazar
> │   │   ├── 🆕 StudentStatusRequest.java     entrada: solo el estado
> │   │   ├── 🆕 StudentResponse.java          salida
> │   │   └── 🆕 PageResponse.java             salida: una página
> │   └── 📁 mapper                            🆕
> │       └── 🆕 StudentMapper.java            DTO ↔ entidad
> ├── 📁 business
> │   ├── 📁 service
> │   │   ├── ✏️ StudentService.java           devuelve un resultado, no un Optional ambiguo
> │   │   ├── 🆕 ServiceResult.java
> │   │   └── 🆕 ServiceStatus.java
> │   └── 📁 impl
> │       └── 🆕 StudentServiceImpl.java           devuelve un resultado, no un Optional ambiguo
> └── 📁 persistence
>     ├── 📁 entity
>     │   ├── ✏️ StudentEntity.java            sin validaciones (pasan al DTO) y con campo interno
>     │   └── StudentStatus.java
>     └── 📁 repository
>         └── StudentJpaRepository.java
> 📁 src/main/resources
> └── ✏️ application.properties                límites de paginación
> ```
>
> ```text
> Controller            Service               Repository
> ──────────            ───────               ──────────
> recibe JSON  ───▶  StudentRequest
> valida (@Valid)
> mapea        ───▶  StudentEntity  ───▶  reglas  ───▶  save()
> elige HTTP   ◀───  ServiceResult  ◀───  decide
> mapea        ◀───  StudentResponse
> ```
>
> | Capa | Qué sabe | Qué NO sabe |
> |---|---|---|
> | `controller` | HTTP, DTO, códigos de estado | Reglas del negocio, la base de datos |
> | `dto` y `mapper` | La forma del contrato y cómo convertirla | Cómo se guarda |
> | `service` | Reglas y resultados | HTTP, JSON |
> | `entity` y `repository` | La tabla | Que existe una API |

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Punto de partida: el CRUD que devuelve la entidad</strong></summary>

> <details>
> <summary><strong>Paso 0.1 · MySQL y el proyecto</strong></summary>
>
> > **MySQL.** Hibernate crea las tablas, pero no la base `academy`:
> >
> > ```bash
> > docker run -d --name academy-mysql \
> >   -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academy \
> >   -p 3306:3306 mysql:8.0
> > ```
> >
> > **Proyecto.** En [start.spring.io](https://start.spring.io):
> >
> > - **Project:** Maven
> > - **Java:** 17 o superior
> > - **Group:** `com.academy`
> > - **Artifact:** `students-api-dto`
> > - **Dependencias:**
> >   - **Spring Web**
> >   - **Spring Data JPA**
> >   - **Validation**
> >   - **MySQL Driver**
> >
> > <details>
> > <summary><strong>📄 pom.xml (dependencias)</strong></summary>
> >
> > **Ruta:** `pom.xml`
> >
> > ```xml
> > <dependency>
> >     <groupId>org.springframework.boot</groupId>
> >     <artifactId>spring-boot-starter-webmvc</artifactId>
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
> > ✅ **Punto de control 0.1:** la aplicación arranca (`Started AcademyApplication`) y sigue en ejecución.
>
> </details>
>
> <details>
> <summary><strong>Paso 0.2 · Entidad, repositorio y servicio</strong></summary>
>
> > Es el CRUD de siempre. Dos detalles que se retomarán: la entidad ya trae **validaciones** (`@NotBlank`, `@Email`…) y el servicio **descarta el `id`** que llegue del cliente (siempre inserta con `id` en `null`).
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
>
> </details>
>
> <details>
> <summary><strong>Paso 0.3 · El controlador que devuelve la entidad</strong></summary>
>
> > El controlador recibe un `StudentEntity` y devuelve un `StudentEntity`. Funciona y es corto:
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
> > > | Petición | Respuesta |
> > > |---|---|
> > > | `POST /api/students` | `201` con la entidad · `409` si el correo existe · `400` si es inválido |
> > > | `GET /api/students?name=&page=&size=` | `200` con un arreglo de entidades |
> > > | `GET`, `PUT`, `DELETE /api/students/{id}` | `200`/`204` · `404` · `409` |
> > >
> > > ✅ **Punto de control 0:** el CRUD responde. **Este es el punto de partida que se va a mejorar.**
>
> </details>
>

</details>

---

<details>
<summary><strong>🔎 Paso 1 — Ver los problemas de exponer la entidad</strong></summary>

> <details>
> <summary><strong>Paso 1.1 · Un campo interno aparece en el JSON</strong></summary>
>
> > Un requisito normal: **auditar** cuándo se creó cada estudiante. Se agrega un campo a la entidad (y a la tabla, con `ddl-auto=update`):
> >
> > <details>
> > <summary><strong>📄 StudentEntity.java (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/entity/StudentEntity.java`
> >
> > ```java
> > @CreationTimestamp
> > @Column(name = "created_at", updatable = false)
> > private LocalDateTime createdAt;
> >
> > public LocalDateTime getCreatedAt() { return createdAt; }
> > ```
> >
> > </details>
> >
> > Se agregan los imports `org.hibernate.annotations.CreationTimestamp` y `java.time.LocalDateTime`. Se crea un estudiante y se mira la respuesta. **Resultado real:**
> >
> > Petición:
> >
> > ```text
> > POST /api/students  →  201
> > ```
> >
> > Respuesta:
> >
> > ```json
> > {
> >   "id": 1,
> >   "name": "Ana Torres",
> >   "age": 22,
> >   "email": "ana@mail.com",
> >   "status": "ACTIVE",
> >   "createdAt": "2026-10-31T20:42:41.558465"   // ⚠️ campo interno que se filtró
> > }
> > ```
> >
> > > ⚠️ **Nadie lo pidió.** El campo `createdAt` es de **auditoría interna**, pero el cliente ya lo recibe: la API **cambió sin que se tocara el controlador**.
>
> </details>
>
> <details>
> <summary><strong>Paso 1.2 · El cliente puede mandar campos que no debería</strong></summary>
>
> > La misma clase sirve de **entrada**: el cliente puede incluir en el JSON cualquier campo de la entidad. **Resultado real** al enviar un `createdAt` falso:
> >
> > Cuerpo enviado en `POST /api/students`:
> >
> > ```json
> > {
> >   "name": "Luis",
> >   "age": 22,
> >   "email": "l@mail.com",
> >   "status": "ACTIVE",
> >   "createdAt": "2000-01-01T00:00:00"   // ⚠️ valor falso enviado por el cliente
> > }
> > ```
> >
> > Respuesta `201`:
> >
> > ```json
> > {
> >   "id": 2,
> >   "name": "Luis",
> >   "age": 22,
> >   "email": "l@mail.com",
> >   "status": "ACTIVE",
> >   "createdAt": "2026-10-31T20:42:41.612476"   // la fecha real, no la enviada
> > }
> > ```
> >
> > Hibernate lo pisó con la fecha real, **pero solo por suerte**: el código de entrada aceptó el campo. Con un campo que no se rellene solo (por ejemplo un `role` o un `balance`), el dato falso se guardaría.
> >
> > > 🔎 **Concepto:** esto se llama **asignación masiva** (*mass assignment*): enviar campos de más que se enlazan al objeto sin que nadie los haya autorizado.
>
> </details>
>
> <details>
> <summary><strong>Paso 1.3 · El resumen de problemas</strong></summary>
>
> > | # | Problema | Se ve en | Lo resuelve |
> > |---|---|---|---|
> > | 1 | **Campos internos** de la tabla salen en el JSON | `createdAt` en las respuestas | DTO de salida (Paso 2) |
> > | 2 | **El cliente decide qué campos manda** (asignación masiva) | `createdAt` en el cuerpo del `POST` | DTO de entrada (Paso 2) |
> > | 3 | **El contrato está atado a la tabla**: renombrar un atributo de la entidad cambia el JSON | *(razonamiento: si `name` pasara a `fullName`, los clientes dejarían de funcionar)* | DTO + mapeador (Pasos 2 y 3) |
> > | 4 | **Las validaciones viven en la entidad**, que ya hace de tabla | `@NotBlank`, `@Email` sobre la entidad | Validar en el DTO de entrada (Paso 2) |
> > | 5 | **Dos consultas para saber qué falló**: el controlador pregunta si existe y luego actualiza | `update` en el Paso 0.3 | `ServiceResult` (Pasos 4 y 5) |
> > | 6 | **Respuestas pobres**: `201` sin `Location`, listado sin totales, página sin límite | `GET` devuelve un arreglo; `?size=1000` se acepta | Diseño de endpoints (Paso 6) |
> > | 7 | **Rutas sin versión** | `/api/students` | `/api/v1/students` (Paso 6) |
> >
> > > 💡 **Beneficio de este paso:** se entiende **por qué** se agrega cada pieza. Cada paso siguiente resuelve una fila de esta tabla.
> > >
> > > 🗣️ **Pregunta:** ¿qué pasaría con los clientes de la API si mañana se renombra una columna? *(Si la entidad es el contrato, se rompen. Con DTO, solo cambia el mapeador.)*
>
> </details>
>

</details>

---

<details>
<summary><strong>📦 Paso 2 — Los DTO (<code>presentation.dto</code>)</strong></summary>

> <details>
> <summary><strong>Paso 2.1 · <code>StudentRequest</code> y <code>StudentStatusRequest</code>: lo que entra</strong></summary>
>
> > Se crea el paquete `presentation.dto`. Un DTO de entrada declara **solo los campos que el cliente puede enviar**, con sus validaciones. Se escribe **uno por caso de uso**.
> >
> > <details>
> > <summary><strong>📄 StudentRequest.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/StudentRequest.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import com.academy.persistence.entity.StudentStatus;
> > import jakarta.validation.constraints.Email;
> > import jakarta.validation.constraints.Max;
> > import jakarta.validation.constraints.Min;
> > import jakarta.validation.constraints.NotBlank;
> > import jakarta.validation.constraints.NotNull;
> > import jakarta.validation.constraints.Size;
> >
> > public record StudentRequest(
> >
> >         @NotBlank
> >         @Size(min = 2, max = 100)
> >         String name,
> >
> >         @Min(16)
> >         @Max(99)
> >         int age,
> >
> >         @NotBlank
> >         @Email
> >         String email,
> >
> >         @NotNull
> >         StudentStatus status
> > ) {
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 StudentStatusRequest.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/StudentStatusRequest.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import com.academy.persistence.entity.StudentStatus;
> > import jakarta.validation.constraints.NotNull;
> >
> > public record StudentStatusRequest(@NotNull StudentStatus status) {
> > }
> > ```
> >
> > </details>
> >
> > > | Decisión | Por qué |
> > > |---|---|
> > > | `record` | Inmutable y sin código repetido (accesores, `equals`, `toString`). |
> > > | Sin `id` ni `createdAt` | El cliente **no puede** enviarlos: no existen en el contrato. Jackson ignora los campos desconocidos. |
> > > | Validaciones aquí | `@Valid` en el controlador devuelve `400` antes de llegar al servicio. |
> > > | `StudentStatusRequest` aparte | Cambiar el estado es **otra acción** con **otro cuerpo** (solo `status`). |
> > >
> > > 💡 **Beneficio de este paso:** el contrato de **entrada** queda explícito y cerrado. El problema 2 (asignación masiva) desaparece, porque lo que no está en el DTO no se enlaza.
>
> </details>
>
> <details>
> <summary><strong>Paso 2.2 · <code>StudentResponse</code> y <code>PageResponse</code>: lo que sale</strong></summary>
>
> > Un DTO de salida declara **solo lo que el cliente debe ver**. `PageResponse<T>` es un envoltorio genérico para cualquier lista paginada.
> >
> > <details>
> > <summary><strong>📄 StudentResponse.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/StudentResponse.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import com.academy.persistence.entity.StudentStatus;
> >
> > public record StudentResponse(Long id, String name, int age, String email, StudentStatus status) {
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 PageResponse.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/PageResponse.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import java.util.List;
> >
> > public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
> > }
> > ```
> >
> > </details>
> >
> > > | Decisión | Por qué |
> > > |---|---|
> > > | Sin `createdAt` | Es interno: no aparece en el JSON aunque exista en la tabla. |
> > > | `PageResponse<T>` genérico | Un solo tipo sirve para estudiantes, cursos, etc. |
> > > | `page`, `size`, `totalElements`, `totalPages` | El cliente sabe **cuántas páginas hay** sin adivinar. |
> > >
> > > 💡 **Beneficio de este paso:** los problemas 1 y 6 (campos internos y listados pobres) se resuelven. Además se puede **agregar un campo a la tabla sin que cambie la API**.
>
> </details>
>
> <details>
> <summary><strong>Paso 2.3 · Quitar las validaciones de la entidad</strong></summary>
>
> > Las validaciones ya viven en `StudentRequest`. En `StudentEntity` se eliminan `@NotBlank`, `@Size`, `@Min`, `@Max`, `@Email` y `@NotNull`, junto con sus imports. La entidad conserva solo lo que la base necesita:
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
> > import org.hibernate.annotations.CreationTimestamp;
> >
> > import java.time.LocalDateTime;
> >
> > @Entity
> > @Table(name = "student")
> > public class StudentEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     @Column(nullable = false)
> >     private String name;
> >
> >     private int age;
> >
> >     @Column(nullable = false, unique = true)
> >     private String email;
> >
> >     @Enumerated(EnumType.STRING)
> >     @Column(nullable = false)
> >     private StudentStatus status;
> >
> >     @CreationTimestamp
> >     @Column(name = "created_at", updatable = false)
> >     private LocalDateTime createdAt;
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
> >     public LocalDateTime getCreatedAt() { return createdAt; }
> > }
> > ```
> >
> > </details>
> >
> > > | Lo que se queda | Para qué |
> > > |---|---|
> > > | `@Column(nullable = false)` · `unique = true` | La **base** impone sus propias restricciones (última defensa) |
> > > | `@Enumerated(EnumType.STRING)` | Cómo se guarda el enumerado |
> > > | `createdAt` | Campo interno: existe en la tabla, no en la API |
> > >
> > > 💡 **Beneficio de este paso:** cada clase tiene **una sola razón para cambiar**. La entidad cambia cuando cambia la tabla; el DTO, cuando cambia el contrato.
>
> </details>
>

</details>

---

<details>
<summary><strong>🔁 Paso 3 — El mapeador (<code>presentation.mapper</code>)</strong></summary>

> <details>
> <summary><strong>Paso 3.1 · <code>StudentMapper</code></strong></summary>
>
> > Se crea el paquete `presentation.mapper`. Una clase `final` con métodos estáticos (constructor privado) concentra **todas** las conversiones:
> >
> > <details>
> > <summary><strong>📄 StudentMapper.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/mapper/StudentMapper.java`
> >
> > ```java
> > package com.academy.presentation.mapper;
> >
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.presentation.dto.PageResponse;
> > import com.academy.presentation.dto.StudentRequest;
> > import com.academy.presentation.dto.StudentResponse;
> > import org.springframework.data.domain.Page;
> >
> > public final class StudentMapper {
> >
> >     private StudentMapper() {
> >     }
> >
> >     public static StudentEntity toEntity(StudentRequest request) {
> >         return new StudentEntity(null, request.name(), request.age(), request.email(), request.status());
> >     }
> >
> >     public static StudentResponse toResponse(StudentEntity entity) {
> >         return new StudentResponse(entity.getId(), entity.getName(), entity.getAge(),
> >                 entity.getEmail(), entity.getStatus());
> >     }
> >
> >     public static PageResponse<StudentResponse> toPageResponse(Page<StudentEntity> page) {
> >         return new PageResponse<>(
> >                 page.getContent().stream().map(StudentMapper::toResponse).toList(),
> >                 page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Método | Qué hace |
> > > |---|---|
> > > | `toEntity(StudentRequest)` | Crea una entidad **sin `id`** (`null`): el servicio insertará o, en `PUT`, le pondrá el `id` de la ruta. |
> > > | `toResponse(StudentEntity)` | Copia **solo** los campos del contrato; `createdAt` queda fuera. |
> > > | `toPageResponse(Page<StudentEntity>)` | Convierte una página de Spring Data en el envoltorio `PageResponse`. |
> > >
> > > - Si se renombra un atributo de la entidad, **solo cambia este archivo**; el JSON no se entera.
> > > - No tiene lógica de negocio: solo copia atributos.
> > >
> > > 💡 **Beneficio de este paso:** la traducción vive en **un solo lugar** y se puede cambiar sin tocar el controlador. El problema 3 queda resuelto: el contrato ya no depende de los nombres de la entidad.
> > >
> > > 🔎 **Concepto:** hacerlo a mano permite ver qué pasa. Herramientas como **MapStruct** generan este código; conviene entender primero el manual.
>
> </details>
>

</details>

---

<details>
<summary><strong>⚙️ Paso 4 — El servicio informa el resultado (<code>ServiceResult</code>)</strong></summary>

> <details>
> <summary><strong>Paso 4.1 · Un resultado en lugar de un <code>Optional</code> ambiguo</strong></summary>
>
> > En el Paso 0, `update` devolvía un `Optional` vacío tanto si el estudiante **no existía** como si el correo era **de otro**. Para distinguirlos, el controlador tenía que **preguntar dos veces**. Se crea un resultado con tres estados:
> >
> > <details>
> > <summary><strong>📄 ServiceStatus.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/ServiceStatus.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > public enum ServiceStatus {
> >     OK,
> >     NOT_FOUND,
> >     CONFLICT
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 ServiceResult.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/ServiceResult.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > public record ServiceResult<T>(ServiceStatus status, T value) {
> >
> >     public static <T> ServiceResult<T> ok(T value) {
> >         return new ServiceResult<>(ServiceStatus.OK, value);
> >     }
> >
> >     public static <T> ServiceResult<T> notFound() {
> >         return new ServiceResult<>(ServiceStatus.NOT_FOUND, null);
> >     }
> >
> >     public static <T> ServiceResult<T> conflict() {
> >         return new ServiceResult<>(ServiceStatus.CONFLICT, null);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Estado | Significa | El controlador responde |
> > > |---|---|---|
> > > | `OK` | La operación se cumplió | `200` o `201` con el cuerpo |
> > > | `NOT_FOUND` | No existe lo que se buscaba | `404` |
> > > | `CONFLICT` | Choca con una regla (correo repetido) | `409` |
> > >
> > > - `ServiceResult<T>` es un `record` con fábricas `ok`, `notFound` y `conflict`.
> > > - El servicio **no conoce HTTP**: dice *qué pasó*, no *qué código usar*.
>
> </details>
>
> <details>
> <summary><strong>Paso 4.2 · <code>StudentService</code> (interfaz) y <code>StudentServiceImpl</code></strong></summary>
>
> > Las reglas son las mismas; cambia lo que devuelven los métodos. Se agrega `changeStatus`, que cambia **solo** el estado. Además, `StudentService` deja de ser una clase: pasa a ser una **interfaz** (el contrato) en `business/service`, y la lógica se mueve a `StudentServiceImpl`, en `business/impl`.
> >
> > <details>
> > <summary><strong>📄 StudentService.java</strong> <em>(interfaz)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/StudentService.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.persistence.entity.StudentStatus;
> > import org.springframework.data.domain.Page;
> > import org.springframework.data.domain.Pageable;
> >
> > import java.util.Optional;
> >
> > public interface StudentService {
> >
> >     Page<StudentEntity> search(String name, Pageable pageable);
> >
> >     Optional<StudentEntity> findById(Long id);
> >
> >     ServiceResult<StudentEntity> create(StudentEntity data);
> >
> >     ServiceResult<StudentEntity> update(Long id, StudentEntity data);
> >
> >     ServiceResult<StudentEntity> changeStatus(Long id, StudentStatus status);
> >
> >     boolean delete(Long id);
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 StudentServiceImpl.java</strong> <em>(implementación)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/impl/StudentServiceImpl.java`
> >
> > ```java
> > package com.academy.business.impl;
> >
> > import com.academy.business.service.ServiceResult;
> > import com.academy.business.service.StudentService;
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.persistence.entity.StudentStatus;
> > import com.academy.persistence.repository.StudentJpaRepository;
> > import org.springframework.data.domain.Page;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
> >
> > import java.util.Optional;
> >
> > @Service
> > public class StudentServiceImpl implements StudentService {
> >
> >     private final StudentJpaRepository students;
> >
> >     public StudentServiceImpl(StudentJpaRepository students) {
> >         this.students = students;
> >     }
> >
> >     @Override
> >     @Transactional(readOnly = true)
> >     public Page<StudentEntity> search(String name, Pageable pageable) {
> >         if (name == null || name.isBlank()) {
> >             return students.findAll(pageable);
> >         }
> >         return students.findByNameContainingIgnoreCase(name, pageable);
> >     }
> >
> >     @Override
> >     @Transactional(readOnly = true)
> >     public Optional<StudentEntity> findById(Long id) {
> >         return students.findById(id);
> >     }
> >
> >     @Override
> >     @Transactional
> >     public ServiceResult<StudentEntity> create(StudentEntity data) {
> >         if (students.existsByEmailIgnoreCase(data.getEmail())) {
> >             return ServiceResult.conflict();
> >         }
> >         StudentEntity toSave = new StudentEntity(
> >                 null, data.getName(), data.getAge(), data.getEmail(), data.getStatus());
> >         return ServiceResult.ok(students.save(toSave));
> >     }
> >
> >     @Override
> >     @Transactional
> >     public ServiceResult<StudentEntity> update(Long id, StudentEntity data) {
> >         if (!students.existsById(id)) {
> >             return ServiceResult.notFound();
> >         }
> >         if (students.existsByEmailIgnoreCaseAndIdNot(data.getEmail(), id)) {
> >             return ServiceResult.conflict();
> >         }
> >         StudentEntity toSave = new StudentEntity(
> >                 id, data.getName(), data.getAge(), data.getEmail(), data.getStatus());
> >         return ServiceResult.ok(students.save(toSave));
> >     }
> >
> >     @Override
> >     @Transactional
> >     public ServiceResult<StudentEntity> changeStatus(Long id, StudentStatus status) {
> >         Optional<StudentEntity> current = students.findById(id);
> >         if (current.isEmpty()) {
> >             return ServiceResult.notFound();
> >         }
> >         StudentEntity student = current.get();
> >         StudentEntity toSave = new StudentEntity(
> >                 id, student.getName(), student.getAge(), student.getEmail(), status);
> >         return ServiceResult.ok(students.save(toSave));
> >     }
> >
> >     @Override
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
> > > 💡 **Interfaz e implementación:** `StudentService` es ahora un **contrato** (una `interface` con las operaciones) y `StudentServiceImpl` es la clase `@Service` que lo cumple. Los controladores siguen inyectando `StudentService`; Spring entrega la implementación. Así la capa de presentación depende del contrato y no de la clase concreta.
> >
> > > | Método | Devuelve | Qué decide |
> > > |---|---|---|
> > > | `search` | `Page<StudentEntity>` | Filtro opcional por nombre y paginación |
> > > | `create` | `ServiceResult` | `CONFLICT` si el correo existe; siempre inserta (descarta el `id`) |
> > > | `update` | `ServiceResult` | `NOT_FOUND` si no existe, `CONFLICT` si el correo es de otro |
> > > | `changeStatus` | `ServiceResult` | `NOT_FOUND` si no existe; si no, cambia solo el estado |
> > > | `delete` | `boolean` | `false` si no existía |
> > >
> > > 💡 **Beneficio de este paso:** **una sola llamada** responde a la pregunta «¿qué pasó?». Se acaba la doble consulta del controlador (problema 5) y las reglas siguen en el servicio.
> > >
> > > ⚠️ **Es un puente.** `ServiceResult` resuelve el problema **ahora**. En el Módulo 7 se reemplaza por excepciones y un manejador global, que quitan incluso el `if` del controlador.
>
> </details>
>

</details>

---

<details>
<summary><strong>🌐 Paso 5 — El controlador delgado</strong></summary>

> <details>
> <summary><strong>Paso 5.1 · <code>StudentController</code></strong></summary>
>
> > El controlador hace **cinco cosas y nada más**: recibe el JSON, valida (`@Valid`), convierte con el mapeador, llama al servicio y elige el código HTTP. No decide reglas ni conoce la base.
> >
> > <details>
> > <summary><strong>📄 StudentController.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > package com.academy.presentation.controller;
> >
> > import com.academy.business.service.ServiceResult;
> > import com.academy.business.service.ServiceStatus;
> > import com.academy.business.service.StudentService;
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.presentation.dto.PageResponse;
> > import com.academy.presentation.dto.StudentRequest;
> > import com.academy.presentation.dto.StudentResponse;
> > import com.academy.presentation.dto.StudentStatusRequest;
> > import com.academy.presentation.mapper.StudentMapper;
> > import jakarta.validation.Valid;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.http.ResponseEntity;
> > import org.springframework.web.bind.annotation.DeleteMapping;
> > import org.springframework.web.bind.annotation.GetMapping;
> > import org.springframework.web.bind.annotation.PatchMapping;
> > import org.springframework.web.bind.annotation.PathVariable;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.PutMapping;
> > import org.springframework.web.bind.annotation.RequestBody;
> > import org.springframework.web.bind.annotation.RequestMapping;
> > import org.springframework.web.bind.annotation.RequestParam;
> > import org.springframework.web.bind.annotation.RestController;
> > import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
> >
> > import java.net.URI;
> >
> > @RestController
> > @RequestMapping("/api/v1/students")
> > public class StudentController {
> >
> >     private final StudentService service;
> >
> >     public StudentController(StudentService service) {
> >         this.service = service;
> >     }
> >
> >     @GetMapping
> >     public PageResponse<StudentResponse> search(@RequestParam(required = false) String name, Pageable pageable) {
> >         return StudentMapper.toPageResponse(service.search(name, pageable));
> >     }
> >
> >     @GetMapping("/{id}")
> >     public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
> >         return service.findById(id)
> >                 .map(StudentMapper::toResponse)
> >                 .map(ResponseEntity::ok)
> >                 .orElseGet(() -> ResponseEntity.notFound().build());
> >     }
> >
> >     @PostMapping
> >     public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >         ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> >         if (result.status() == ServiceStatus.CONFLICT) {
> >             return ResponseEntity.status(HttpStatus.CONFLICT).build();
> >         }
> >         StudentResponse body = StudentMapper.toResponse(result.value());
> >         URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                 .path("/{id}").buildAndExpand(body.id()).toUri();
> >         return ResponseEntity.created(location).body(body);
> >     }
> >
> >     @PutMapping("/{id}")
> >     public ResponseEntity<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
> >         return respond(service.update(id, StudentMapper.toEntity(request)));
> >     }
> >
> >     @PatchMapping("/{id}/status")
> >     public ResponseEntity<StudentResponse> changeStatus(@PathVariable Long id,
> >                                                         @Valid @RequestBody StudentStatusRequest request) {
> >         return respond(service.changeStatus(id, request.status()));
> >     }
> >
> >     @DeleteMapping("/{id}")
> >     public ResponseEntity<Void> delete(@PathVariable Long id) {
> >         return service.delete(id)
> >                 ? ResponseEntity.noContent().build()
> >                 : ResponseEntity.notFound().build();
> >     }
> >
> >     private ResponseEntity<StudentResponse> respond(ServiceResult<StudentEntity> result) {
> >         return switch (result.status()) {
> >             case OK -> ResponseEntity.ok(StudentMapper.toResponse(result.value()));
> >             case NOT_FOUND -> ResponseEntity.notFound().build();
> >             case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
> >         };
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Anotación / pieza | Para qué sirve |
> > > |---|---|
> > > | `@RequestMapping("/api/v1/students")` | Ruta base **con versión** |
> > > | `@Valid @RequestBody StudentRequest` | Convierte el JSON en el DTO y lo valida (`400` si falla) |
> > > | `ServletUriComponentsBuilder` + `ResponseEntity.created(location)` | `201` con el encabezado **`Location`** del recurso nuevo |
> > > | `respond(ServiceResult)` | Un solo método traduce `OK`/`NOT_FOUND`/`CONFLICT` a HTTP (con `switch` exhaustivo) |
> > > | `@PatchMapping("/{id}/status")` | Acción **parcial**: solo cambia el estado |
> > >
> > > | Antes (Paso 0.3) | Ahora |
> > > |---|---|
> > > | Recibía y devolvía `StudentEntity` | Recibe `StudentRequest`, devuelve `StudentResponse` |
> > > | `update`: dos consultas y dos `if` | `update`: **una** llamada y `respond(...)` |
> > > | `201` sin `Location` | `201` con `Location` |
> > > | Listado: arreglo sin totales | `PageResponse` con totales |
> > > | Importaba la entidad para recibirla | Solo usa la entidad para pasarla al servicio vía el mapeador |
> > >
> > > 💡 **Beneficio de este paso:** el controlador se lee **de arriba abajo en segundos**. Las reglas se pueden cambiar sin tocarlo.
> > >
> > > > 🔎 **Regla de oro de un controlador delgado.** Si dentro de un método aparece un `if` que habla de **negocio** (un cupo, un correo repetido), esa decisión pertenece al servicio. Los únicos `if` aceptables son los que traducen un resultado a un código HTTP.
>
> </details>
>

</details>

---

<details>
<summary><strong>🧭 Paso 6 — Diseño de los endpoints</strong></summary>

> <details>
> <summary><strong>Paso 6.1 · Convenciones que se aplicaron</strong></summary>
>
> > Un buen endpoint **dice qué hace sin leer el código**. Esta es la tabla de decisiones que ya están en el controlador:
> >
> > > | Convención | Aplicación en el taller | Beneficio |
> > > |---|---|---|
> > > | **Sustantivos en plural**, nunca verbos en la ruta | `/students`, no `/getStudents` | La ruta nombra el **recurso**; el verbo HTTP dice la acción |
> > > | **Versión en la ruta** | `/api/v1/students` | Se puede publicar `/v2` sin romper a quien usa `/v1` |
> > > | **Verbo según la acción** | `GET` lee · `POST` crea · `PUT` reemplaza · `PATCH` cambia una parte · `DELETE` borra | Cualquier cliente adivina la operación |
> > > | **`201 Created` + `Location`** al crear | `Location: http://localhost:8080/api/v1/students/1` | El cliente sabe **dónde** quedó el recurso, sin buscarlo |
> > > | **`204 No Content`** al borrar | `DELETE` sin cuerpo | No se envía nada inútil |
> > > | **`404`** si no existe · **`409`** si choca con una regla · **`400`** si el cuerpo es inválido | Códigos distintos para fallas distintas | El cliente sabe **qué corregir** |
> > > | **Acción parcial como subrecurso** | `PATCH /students/{id}/status` | No se obliga a reenviar todo el estudiante para cambiar un dato |
> > > | **Filtros en la consulta** | `GET /students?name=ana` | El mismo recurso sirve para listar y buscar |
> > > | **Paginación con envoltorio** | `content`, `page`, `size`, `totalElements`, `totalPages` | El cliente sabe cuántas páginas hay |
>
> </details>
>
> <details>
> <summary><strong>Paso 6.2 · Limitar el tamaño de página</strong></summary>
>
> > Sin un límite, `?size=1000000` pediría **toda la tabla** de una vez. Se fijan los valores en `application.properties`:
> >
> > <details>
> > <summary><strong>📄 application.properties (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/resources/application.properties`
> >
> > ```properties
> > # Pagination limits (protect the API)
> > spring.data.web.pageable.default-page-size=10
> > spring.data.web.pageable.max-page-size=50
> > ```
> >
> > </details>
> >
> > > **Resultado real** al pedir `?size=1000`:
> > >
> > > ```text
> > > GET /api/v1/students?name=a&size=1000&sort=name,asc
> > > → 200  {"content":[{"id":1,"name":"Ana Torres",…}],"page":0,"size":50,"totalElements":1,"totalPages":1}
> > > ```
> > >
> > > Spring Data **recortó** el tamaño a **50**, el máximo configurado.
> > >
> > > 💡 **Beneficio de este paso:** un cliente descuidado (o malicioso) no puede tumbar la base pidiendo millones de filas. Es una defensa **sin escribir código**.
>
> </details>
>

</details>

---

<details>
<summary><strong>🧪 Paso 7 — Probar la API</strong></summary>

> <details>
> <summary><strong>Paso 7.1 · Las pruebas con <code>curl</code></strong></summary>
>
> > Se arranca la aplicación con MySQL en marcha (`./mvnw spring-boot:run`) y se ejecuta cada comando en otra terminal. Debajo de cada uno está la **respuesta real**.
> >
> > > 💡 La API devuelve el JSON en una sola línea; aquí se muestra con sangría para leerlo mejor. Los `404` y `409` del controlador llegan **sin cuerpo**; los `400` de validación traen el cuerpo genérico de Spring Boot.
> >
> > <details>
> > <summary><strong>1. Crear un estudiante → <code>201</code> y la cabecera <code>Location</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >     ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> >     if (result.status() == ServiceStatus.CONFLICT) {
> >         return ResponseEntity.status(HttpStatus.CONFLICT).build();
> >     }
> >     StudentResponse body = StudentMapper.toResponse(result.value());
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > Entra por `create`, que llama a `StudentService.create(...)`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"Ana Torres","age":22,"email":"ana@mail.com","status":"ACTIVE"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 201
> > Location: http://localhost:8080/api/v1/students/1
> > Content-Type: application/json
> > Transfer-Encoding: chunked
> >
> > {
> >   "id": 1,
> >   "name": "Ana Torres",
> >   "age": 22,
> >   "email": "ana@mail.com",
> >   "status": "ACTIVE"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>2. Crear enviando campos de más (<code>id</code> y <code>createdAt</code>) → <code>201</code>; ambos se ignoran</strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >     ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> >     if (result.status() == ServiceStatus.CONFLICT) {
> >         return ResponseEntity.status(HttpStatus.CONFLICT).build();
> >     }
> >     StudentResponse body = StudentMapper.toResponse(result.value());
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > Entra por `create`. El `id` y el `createdAt` no existen en `StudentRequest`, así que nunca llegan al método.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"id":99,"name":"Luis","age":22,"email":"luis@mail.com","status":"ACTIVE","createdAt":"2000-01-01T00:00:00"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 201
> > Location: http://localhost:8080/api/v1/students/2
> > Content-Type: application/json
> > Transfer-Encoding: chunked
> >
> > {
> >   "id": 2,
> >   "name": "Luis",
> >   "age": 22,
> >   "email": "luis@mail.com",
> >   "status": "ACTIVE"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>3. Correo repetido, en mayúsculas → <code>409</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >     ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> >     if (result.status() == ServiceStatus.CONFLICT) {
> >         return ResponseEntity.status(HttpStatus.CONFLICT).build();
> >     }
> >     StudentResponse body = StudentMapper.toResponse(result.value());
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > Entra por `create`; `StudentService.create(...)` detecta el correo repetido y devuelve `CONFLICT`, que `create` traduce a `409`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"Otra","age":30,"email":"ANA@MAIL.COM","status":"ACTIVE"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 409
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>4. Datos inválidos → <code>400</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >     ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> >     if (result.status() == ServiceStatus.CONFLICT) {
> >         return ResponseEntity.status(HttpStatus.CONFLICT).build();
> >     }
> >     StudentResponse body = StudentMapper.toResponse(result.value());
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > `@Valid` falla **antes** de ejecutar el cuerpo de `create`: Spring responde `400` y el método no llega a correr.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"","age":5,"email":"x","status":null}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 400
> > Content-Type: application/json
> > Transfer-Encoding: chunked
> > Connection: close
> >
> > {
> >   "timestamp": "2026-11-01T16:58:06.438Z",
> >   "status": 400,
> >   "error": "Bad Request",
> >   "path": "/api/v1/students"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>5. Listar con filtro, tamaño excesivo y orden → <code>200</code> con <code>"size":50</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @GetMapping
> > public PageResponse<StudentResponse> search(@RequestParam(required = false) String name, Pageable pageable) {
> >     return StudentMapper.toPageResponse(service.search(name, pageable));
> > }
> > ```
> >
> > Entra por `search`, que llama a `StudentService.search(...)`. El tope de 50 lo aplica Spring antes, con `spring.data.web.pageable.max-page-size`.
> >
> > ```bash
> > curl -i "http://localhost:8080/api/v1/students?name=a&size=1000&sort=name,asc"
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> > Transfer-Encoding: chunked
> >
> > {
> >   "content": [
> >     {
> >       "id": 1,
> >       "name": "Ana Torres",
> >       "age": 22,
> >       "email": "ana@mail.com",
> >       "status": "ACTIVE"
> >     }
> >   ],
> >   "page": 0,
> >   "size": 50,
> >   "totalElements": 1,
> >   "totalPages": 1
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>6. Buscar por id → <code>200</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @GetMapping("/{id}")
> > public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
> >     return service.findById(id)
> >             .map(StudentMapper::toResponse)
> >             .map(ResponseEntity::ok)
> >             .orElseGet(() -> ResponseEntity.notFound().build());
> > }
> > ```
> >
> > Entra por `findById`, que llama a `StudentService.findById(...)`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/students/1
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> > Transfer-Encoding: chunked
> >
> > {
> >   "id": 1,
> >   "name": "Ana Torres",
> >   "age": 22,
> >   "email": "ana@mail.com",
> >   "status": "ACTIVE"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>7. Buscar un id que no existe → <code>404</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @GetMapping("/{id}")
> > public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
> >     return service.findById(id)
> >             .map(StudentMapper::toResponse)
> >             .map(ResponseEntity::ok)
> >             .orElseGet(() -> ResponseEntity.notFound().build());
> > }
> > ```
> >
> > Entra por `findById`; el servicio no encuentra el estudiante y el método responde `404`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/students/99
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>8. Reemplazar (<code>PUT</code>) con datos válidos → <code>200</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @PutMapping("/{id}")
> > public ResponseEntity<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
> >     return respond(service.update(id, StudentMapper.toEntity(request)));
> > }
> > ```
> >
> > Entra por `update`, que llama a `StudentService.update(...)` y traduce el resultado con `respond(...)`.
> >
> > ```bash
> > curl -i -X PUT http://localhost:8080/api/v1/students/1 \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"Ana T.","age":23,"email":"ana@mail.com","status":"ACTIVE"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> > Transfer-Encoding: chunked
> >
> > {
> >   "id": 1,
> >   "name": "Ana T.",
> >   "age": 23,
> >   "email": "ana@mail.com",
> >   "status": "ACTIVE"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>9. Reemplazar con el correo de otro estudiante → <code>409</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @PutMapping("/{id}")
> > public ResponseEntity<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
> >     return respond(service.update(id, StudentMapper.toEntity(request)));
> > }
> > ```
> >
> > Entra por `update`; el servicio devuelve `CONFLICT` y `respond(...)` lo traduce a `409`.
> >
> > ```bash
> > curl -i -X PUT http://localhost:8080/api/v1/students/1 \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"Ana T.","age":23,"email":"luis@mail.com","status":"ACTIVE"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 409
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>10. Reemplazar un id que no existe → <code>404</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @PutMapping("/{id}")
> > public ResponseEntity<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
> >     return respond(service.update(id, StudentMapper.toEntity(request)));
> > }
> > ```
> >
> > Entra por `update`; el servicio devuelve `NOT_FOUND` y `respond(...)` lo traduce a `404`.
> >
> > ```bash
> > curl -i -X PUT http://localhost:8080/api/v1/students/99 \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"Nadie","age":30,"email":"nadie@mail.com","status":"ACTIVE"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>11. Cambiar solo el estado (<code>PATCH</code>) → <code>200</code>; el nombre y la edad no cambian</strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @PatchMapping("/{id}/status")
> > public ResponseEntity<StudentResponse> changeStatus(@PathVariable Long id,
> >                                                     @Valid @RequestBody StudentStatusRequest request) {
> >     return respond(service.changeStatus(id, request.status()));
> > }
> > ```
> >
> > Entra por `changeStatus`, que recibe un `StudentStatusRequest` y llama a `StudentService.changeStatus(...)`.
> >
> > ```bash
> > curl -i -X PATCH http://localhost:8080/api/v1/students/1/status \
> >      -H "Content-Type: application/json" \
> >      -d '{"status":"INACTIVE"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> > Transfer-Encoding: chunked
> >
> > {
> >   "id": 1,
> >   "name": "Ana T.",
> >   "age": 23,
> >   "email": "ana@mail.com",
> >   "status": "INACTIVE"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>12. <code>PATCH</code> con estado nulo → <code>400</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @PatchMapping("/{id}/status")
> > public ResponseEntity<StudentResponse> changeStatus(@PathVariable Long id,
> >                                                     @Valid @RequestBody StudentStatusRequest request) {
> >     return respond(service.changeStatus(id, request.status()));
> > }
> > ```
> >
> > `@Valid` falla **antes** de ejecutar `changeStatus`: `StudentStatusRequest.status` es `@NotNull`.
> >
> > ```bash
> > curl -i -X PATCH http://localhost:8080/api/v1/students/1/status \
> >      -H "Content-Type: application/json" \
> >      -d '{"status":null}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 400
> > Content-Type: application/json
> > Transfer-Encoding: chunked
> > Connection: close
> >
> > {
> >   "timestamp": "2026-11-01T16:58:06.741Z",
> >   "status": 400,
> >   "error": "Bad Request",
> >   "path": "/api/v1/students/1/status"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>13. <code>PATCH</code> de un id que no existe → <code>404</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @PatchMapping("/{id}/status")
> > public ResponseEntity<StudentResponse> changeStatus(@PathVariable Long id,
> >                                                     @Valid @RequestBody StudentStatusRequest request) {
> >     return respond(service.changeStatus(id, request.status()));
> > }
> > ```
> >
> > Entra por `changeStatus`; el servicio devuelve `NOT_FOUND` y `respond(...)` lo traduce a `404`.
> >
> > ```bash
> > curl -i -X PATCH http://localhost:8080/api/v1/students/99/status \
> >      -H "Content-Type: application/json" \
> >      -d '{"status":"ACTIVE"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>14. Eliminar → <code>204</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @DeleteMapping("/{id}")
> > public ResponseEntity<Void> delete(@PathVariable Long id) {
> >     return service.delete(id)
> >             ? ResponseEntity.noContent().build()
> >             : ResponseEntity.notFound().build();
> > }
> > ```
> >
> > Entra por `delete`, que llama a `StudentService.delete(...)` y responde `204` sin cuerpo.
> >
> > ```bash
> > curl -i -X DELETE http://localhost:8080/api/v1/students/2
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 204
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>15. Eliminar otra vez → <code>404</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:**
> >
> > ```java
> > @DeleteMapping("/{id}")
> > public ResponseEntity<Void> delete(@PathVariable Long id) {
> >     return service.delete(id)
> >             ? ResponseEntity.noContent().build()
> >             : ResponseEntity.notFound().build();
> > }
> > ```
> >
> > Entra por `delete`; el estudiante ya no existe y responde `404`.
> >
> > ```bash
> > curl -i -X DELETE http://localhost:8080/api/v1/students/2
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>16. La ruta sin versión ya no existe → <code>404</code></strong></summary>
> >
> > **Método del controlador:** ninguno. La ruta no existe: el `404` lo genera Spring antes de llegar a `StudentController`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/students
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Type: application/json
> > Transfer-Encoding: chunked
> >
> > {
> >   "timestamp": "2026-11-01T16:58:06.807Z",
> >   "status": 404,
> >   "error": "Not Found",
> >   "path": "/api/students"
> > }
> > ```
> >
> > </details>
> >
> > Y en MySQL, la columna interna sigue existiendo:
> >
> > ```text
> > mysql> select id, name, status, created_at is not null as has_created_at from student;
> > +----+--------+----------+----------------+
> > | id | name   | status   | has_created_at |
> > +----+--------+----------+----------------+
> > |  1 | Ana T. | INACTIVE |              1 |
> > +----+--------+----------+----------------+
> > ```
> >
> > > 💡 **Beneficio de este paso:** se comprueba el **contrato** completo. Y se ve la separación en acción: `created_at` está en la **tabla** y **no** sale por la **API**.
> > >
> > > 🗣️ **Preguntas:** ¿qué pasó con el `"id":99` que mandó el cliente? *(Nunca llegó al servicio: el DTO no tiene `id`, y el servicio, además, inserta siempre con `id` en `null`.)* ¿Por qué `PATCH` no cambió el nombre? *(Su DTO solo tiene `status`: el contrato lo impide.)*
> > >
> > > ✅ **Punto de control 7:** las respuestas coinciden con las que se muestran bajo cada comando.
>
> </details>
>

</details>

---

<details>
<summary><strong>⚖️ Balance: antes y después</strong></summary>

> | Aspecto | Antes (entidad como contrato) | Después (DTO y controlador delgado) |
> |---|---|---|
> | Clases del recurso | Entidad, repositorio, servicio, controlador | + 4 DTO, mapeador, resultado y estado |
> | Campos internos | Salen por la API (`createdAt`) | **No salen** |
> | Campos que envía el cliente | Cualquiera de la entidad | **Solo** los del DTO |
> | Renombrar un atributo de la entidad | Rompe a los clientes | Cambia el mapeador |
> | Validaciones | En la entidad | En el DTO de entrada |
> | `update` | Dos consultas | Una |
> | `201` | Sin `Location` | Con `Location` |
> | Listado | Arreglo sin totales y sin tope | Página con totales y tope de 50 |
> | Ruta | `/api/students` | `/api/v1/students` |
>
> > 🔎 **Lectura clave.** El DTO no hace la API «más corta»: la hace **más segura de cambiar**. Cuanto más grande es el proyecto y más equipos consumen la API, más se nota.
> >
> > ⚠️ **Lo que aún no está resuelto.** Los `404` y `409` **no traen cuerpo**, y el `400` de validación trae solo un mensaje genérico que **no dice qué campo falló**. Un formato de error uniforme (por ejemplo `ProblemDetail`) y las excepciones propias se estudian en el Módulo 7. Y la entidad todavía llega al servicio: separar también un **modelo** del negocio se ve en la [guía de la Fase II](../modulo-02-jpa-y-bases-de-datos/02-guia-taller-gestion-de-cursos-una-version-profesional.md).

</details>

<details>
<summary><strong>🏁 Cierre — preguntas de síntesis</strong></summary>

> 1. ¿Qué es un DTO y qué diferencia hay con una entidad? *(Un DTO es la forma del dato **en la API**; la entidad es la forma **en la base**. Cambian por razones distintas.)*
> 2. ¿Por qué `StudentRequest` no tiene `id` ni `createdAt`? *(Para que el cliente no pueda enviarlos: lo que no está en el DTO no se enlaza.)*
> 3. ¿Por qué hay un `StudentStatusRequest` si ya existe `StudentRequest`? *(Es otra acción con otro cuerpo; así `PATCH` solo puede cambiar el estado.)*
> 4. ¿Dónde viven las validaciones y qué las dispara? *(En el DTO de entrada; `@Valid` en el controlador.)*
> 5. ¿Qué ventaja tiene `ServiceResult` frente a un `Optional` vacío? *(Distingue por qué falló sin una segunda consulta.)*
> 6. ¿Qué hace un controlador y qué no? *(Traduce HTTP ↔ DTO y elige códigos; no decide reglas ni toca la base.)*
> 7. ¿Para qué sirve `Location` en un `201`? *(Indica dónde quedó el recurso creado.)*
> 8. ¿Cuándo **no** vale la pena usar DTO? *(En un CRUD pequeño, sin campos internos y que nadie más consume.)*

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Solución |
> |---|---|---|
> | El JSON sigue trayendo `createdAt` | El controlador aún devuelve la entidad | Devolver `StudentResponse` con `StudentMapper.toResponse` |
> | `400` con un cuerpo que parece válido | Un campo del JSON no coincide con el DTO (`maxCapacity` frente a `max_capacity`) o falta un obligatorio | Usar los nombres exactos de los campos del `record` |
> | `400` aunque el DTO está bien | Falta `@Valid` o la dependencia `spring-boot-starter-validation` | Agregar ambos |
> | La validación **no** se ejecuta | `@Valid` falta en el parámetro `@RequestBody` | Agregarlo |
> | `201` sin encabezado `Location` | Se usó `ResponseEntity.status(201)` en lugar de `created(uri)` | Usar `ResponseEntity.created(location)` |
> | `404` en una ruta que existe | Falta el prefijo de versión en la ruta (`/api/v1`) | Revisar `@RequestMapping` |
> | `The constructor StudentEntity(…) is undefined` | El mapeador usa un constructor distinto del de la entidad | Alinear los parámetros (`id` primero) |
> | `NullPointerException` en `toResponse` | Se pasó `null` (un `ServiceResult` no `OK`) | Comprobar el estado **antes** de mapear |
> | `switch` no compila («no cubre todos los casos») | Se agregó un estado a `ServiceStatus` y falta su rama | Agregar el caso en `respond` |
> | El tamaño de página no se limita | Falta `spring.data.web.pageable.max-page-size` | Agregarlo en `application.properties` |
> | `Communications link failure` al arrancar | MySQL no está arrancado o el puerto es otro | `docker ps` y revisar la URL |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **Renombrar sin romper.** Cambiar el atributo `name` de la entidad a `fullName` (y la columna) y comprobar que el JSON de la API **no cambia**: solo hay que tocar el mapeador, la entidad y el repositorio.
> 2. **Un campo nuevo.** Agregar `phone` (opcional) a `StudentEntity` y exponerlo solo en `StudentResponse`; comprobar que `StudentRequest` no lo acepta hasta que se agregue.
> 3. **Respuesta resumida.** Crear `StudentSummaryResponse` (`id`, `name`) y usarlo en el listado, dejando `StudentResponse` para el detalle.
> 4. **`PUT` frente a `PATCH`.** Agregar `PATCH /students/{id}` con un `StudentPatchRequest` de campos opcionales y aplicar solo los que lleguen.
> 5. **Error con cuerpo.** Devolver un `ProblemDetail` en los `404` y `409` del controlador. ¿Qué cambiaría al llegar a las excepciones del Módulo 7?
> 6. **Un recurso nuevo.** Repetir el patrón completo para `Course` (DTO, mapeador, servicio, controlador) y comprobar cuánto código se puede **reutilizar** (`PageResponse`, `ServiceResult`).
> 7. **MapStruct.** Reemplazar `StudentMapper` por una interfaz de MapStruct y comparar.
> 8. **Contrato de la API.** Documentar este proyecto con Swagger siguiendo la [guía del Módulo 04](../modulo-04-documentacion-apis-swagger/01-guia-crud-estudiantes-con-swagger.md): los `@Schema` van en los DTO y la entidad no se toca.

</details>

<details>
<summary><strong>📁 Estructura final esperada</strong></summary>

> ```text
> 📁 students-api-dto
> ├── 📄 pom.xml
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 presentation
>     │   │   ├── 📁 controller
>     │   │   │   └── 📄 StudentController.java
>     │   │   ├── 📁 dto
>     │   │   │   ├── 📄 StudentRequest.java
>     │   │   │   ├── 📄 StudentStatusRequest.java
>     │   │   │   ├── 📄 StudentResponse.java
>     │   │   │   └── 📄 PageResponse.java
>     │   │   └── 📁 mapper
>     │   │       └── 📄 StudentMapper.java
>     │   ├── 📁 business
>     │   │   ├── 📁 service
>     │   │   │   ├── 📄 StudentService.java
>     │   │   │   ├── 📄 ServiceResult.java
>     │   │   │   └── 📄 ServiceStatus.java
>     │   │   └── 📁 impl
>     │   │       └── 📄 StudentServiceImpl.java
>     │   └── 📁 persistence
>     │       ├── 📁 entity
>     │       │   ├── 📄 StudentEntity.java
>     │       │   └── 📄 StudentStatus.java
>     │       └── 📁 repository
>     │           └── 📄 StudentJpaRepository.java
>     └── 📁 resources
>         └── 📄 application.properties
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
>     <artifactId>students-api-dto</artifactId>
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
> ---
>
> ### 📁 business
>
> #### 📁 service
>
> <details>
> <summary><strong>📄 ServiceStatus.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/business/service/ServiceStatus.java`
>
> ```java
> package com.academy.business.service;
>
> public enum ServiceStatus {
>     OK,
>     NOT_FOUND,
>     CONFLICT
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 ServiceResult.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/business/service/ServiceResult.java`
>
> ```java
> package com.academy.business.service;
>
> public record ServiceResult<T>(ServiceStatus status, T value) {
>
>     public static <T> ServiceResult<T> ok(T value) {
>         return new ServiceResult<>(ServiceStatus.OK, value);
>     }
>
>     public static <T> ServiceResult<T> notFound() {
>         return new ServiceResult<>(ServiceStatus.NOT_FOUND, null);
>     }
>
>     public static <T> ServiceResult<T> conflict() {
>         return new ServiceResult<>(ServiceStatus.CONFLICT, null);
>     }
> }
> ```
>
> </details>
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
> import com.academy.persistence.entity.StudentStatus;
> import org.springframework.data.domain.Page;
> import org.springframework.data.domain.Pageable;
>
> import java.util.Optional;
>
> public interface StudentService {
>
>     Page<StudentEntity> search(String name, Pageable pageable);
>
>     Optional<StudentEntity> findById(Long id);
>
>     ServiceResult<StudentEntity> create(StudentEntity data);
>
>     ServiceResult<StudentEntity> update(Long id, StudentEntity data);
>
>     ServiceResult<StudentEntity> changeStatus(Long id, StudentStatus status);
>
>     boolean delete(Long id);
> }
> ```
>
> </details>
>
> #### 📁 impl
>
> > 💡 Cada servicio es una `interface` en `business/service`; su clase `@Service` vive en `business/impl` con el sufijo `Impl`.
>
> <details>
> <summary><strong>📄 StudentServiceImpl.java</strong> <em>(implementación)</em></summary>
>
> **Ruta:** `src/main/java/com/academy/business/impl/StudentServiceImpl.java`
>
> ```java
> package com.academy.business.impl;
>
> import com.academy.business.service.ServiceResult;
> import com.academy.business.service.StudentService;
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.persistence.entity.StudentStatus;
> import com.academy.persistence.repository.StudentJpaRepository;
> import org.springframework.data.domain.Page;
> import org.springframework.data.domain.Pageable;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
>
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
>     public Page<StudentEntity> search(String name, Pageable pageable) {
>         if (name == null || name.isBlank()) {
>             return students.findAll(pageable);
>         }
>         return students.findByNameContainingIgnoreCase(name, pageable);
>     }
>
>     @Override
>     @Transactional(readOnly = true)
>     public Optional<StudentEntity> findById(Long id) {
>         return students.findById(id);
>     }
>
>     @Override
>     @Transactional
>     public ServiceResult<StudentEntity> create(StudentEntity data) {
>         if (students.existsByEmailIgnoreCase(data.getEmail())) {
>             return ServiceResult.conflict();
>         }
>         StudentEntity toSave = new StudentEntity(
>                 null, data.getName(), data.getAge(), data.getEmail(), data.getStatus());
>         return ServiceResult.ok(students.save(toSave));
>     }
>
>     @Override
>     @Transactional
>     public ServiceResult<StudentEntity> update(Long id, StudentEntity data) {
>         if (!students.existsById(id)) {
>             return ServiceResult.notFound();
>         }
>         if (students.existsByEmailIgnoreCaseAndIdNot(data.getEmail(), id)) {
>             return ServiceResult.conflict();
>         }
>         StudentEntity toSave = new StudentEntity(
>                 id, data.getName(), data.getAge(), data.getEmail(), data.getStatus());
>         return ServiceResult.ok(students.save(toSave));
>     }
>
>     @Override
>     @Transactional
>     public ServiceResult<StudentEntity> changeStatus(Long id, StudentStatus status) {
>         Optional<StudentEntity> current = students.findById(id);
>         if (current.isEmpty()) {
>             return ServiceResult.notFound();
>         }
>         StudentEntity student = current.get();
>         StudentEntity toSave = new StudentEntity(
>                 id, student.getName(), student.getAge(), student.getEmail(), status);
>         return ServiceResult.ok(students.save(toSave));
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
> ---
>
> ### 📁 presentation
>
> #### 📁 dto
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
> import jakarta.validation.constraints.Email;
> import jakarta.validation.constraints.Max;
> import jakarta.validation.constraints.Min;
> import jakarta.validation.constraints.NotBlank;
> import jakarta.validation.constraints.NotNull;
> import jakarta.validation.constraints.Size;
>
> public record StudentRequest(
>
>         @NotBlank
>         @Size(min = 2, max = 100)
>         String name,
>
>         @Min(16)
>         @Max(99)
>         int age,
>
>         @NotBlank
>         @Email
>         String email,
>
>         @NotNull
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
> import jakarta.validation.constraints.NotNull;
>
> public record StudentStatusRequest(@NotNull StudentStatus status) {
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
>
> public record StudentResponse(Long id, String name, int age, String email, StudentStatus status) {
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
> import java.util.List;
>
> public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
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
> #### 📁 controller
>
> <details>
> <summary><strong>📄 StudentController.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
>
> ```java
> package com.academy.presentation.controller;
>
> import com.academy.business.service.ServiceResult;
> import com.academy.business.service.ServiceStatus;
> import com.academy.business.service.StudentService;
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.presentation.dto.PageResponse;
> import com.academy.presentation.dto.StudentRequest;
> import com.academy.presentation.dto.StudentResponse;
> import com.academy.presentation.dto.StudentStatusRequest;
> import com.academy.presentation.mapper.StudentMapper;
> import jakarta.validation.Valid;
> import org.springframework.data.domain.Pageable;
> import org.springframework.http.HttpStatus;
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
> public class StudentController {
>
>     private final StudentService service;
>
>     public StudentController(StudentService service) {
>         this.service = service;
>     }
>
>     @GetMapping
>     public PageResponse<StudentResponse> search(@RequestParam(required = false) String name, Pageable pageable) {
>         return StudentMapper.toPageResponse(service.search(name, pageable));
>     }
>
>     @GetMapping("/{id}")
>     public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
>         return service.findById(id)
>                 .map(StudentMapper::toResponse)
>                 .map(ResponseEntity::ok)
>                 .orElseGet(() -> ResponseEntity.notFound().build());
>     }
>
>     @PostMapping
>     public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
>         ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
>         if (result.status() == ServiceStatus.CONFLICT) {
>             return ResponseEntity.status(HttpStatus.CONFLICT).build();
>         }
>         StudentResponse body = StudentMapper.toResponse(result.value());
>         URI location = ServletUriComponentsBuilder.fromCurrentRequest()
>                 .path("/{id}").buildAndExpand(body.id()).toUri();
>         return ResponseEntity.created(location).body(body);
>     }
>
>     @PutMapping("/{id}")
>     public ResponseEntity<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
>         return respond(service.update(id, StudentMapper.toEntity(request)));
>     }
>
>     @PatchMapping("/{id}/status")
>     public ResponseEntity<StudentResponse> changeStatus(@PathVariable Long id,
>                                                         @Valid @RequestBody StudentStatusRequest request) {
>         return respond(service.changeStatus(id, request.status()));
>     }
>
>     @DeleteMapping("/{id}")
>     public ResponseEntity<Void> delete(@PathVariable Long id) {
>         return service.delete(id)
>                 ? ResponseEntity.noContent().build()
>                 : ResponseEntity.notFound().build();
>     }
>
>     private ResponseEntity<StudentResponse> respond(ServiceResult<StudentEntity> result) {
>         return switch (result.status()) {
>             case OK -> ResponseEntity.ok(StudentMapper.toResponse(result.value()));
>             case NOT_FOUND -> ResponseEntity.notFound().build();
>             case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
>         };
>     }
> }
> ```
>
> </details>

</details>

<details>
<summary><strong>🏷️ Repaso de conceptos y anotaciones</strong></summary>

> | Elemento | Dónde | Para qué sirve |
> |---|---|---|
> | `record` (DTO) | `presentation.dto` | Contrato de entrada o salida, inmutable |
> | `@NotBlank` · `@Size` · `@Min` · `@Max` · `@Email` · `@NotNull` | DTO de entrada | Reglas de forma del cuerpo |
> | `@Valid` | Parámetro `@RequestBody` | Dispara las validaciones: `400` si fallan |
> | `@RequestBody` · `@PathVariable` · `@RequestParam` | Controlador | Cuerpo JSON, parte de la ruta, parámetro de consulta |
> | `@GetMapping` · `@PostMapping` · `@PutMapping` · `@PatchMapping` · `@DeleteMapping` | Controlador | Verbo HTTP de cada método |
> | `ResponseEntity` | Controlador | Elegir código, encabezados y cuerpo |
> | `ServletUriComponentsBuilder` | Controlador | Construir la URL del `Location` |
> | `spring.data.web.pageable.*` | `application.properties` | Tamaño de página por defecto y máximo |
>
> ```text
> JSON ─▶ StudentRequest ─▶ Mapper ─▶ StudentEntity ─▶ Service ─▶ Repository ─▶ MySQL
> JSON ◀─ StudentResponse ◀─ Mapper ◀─ StudentEntity ◀─ Service ◀─ Repository ◀─ MySQL
> ```
>
> > 🔎 **Lectura clave.** El DTO vive en `presentation`; la entidad, en `persistence`. El **mapeador** es el puente, y el **controlador** nunca devuelve una entidad.

</details>

➡️ **Anterior:** [Guía 01 del módulo 02 — Taller «Gestión de cursos» con base de datos](../modulo-02-jpa-y-bases-de-datos/01-guia-taller-gestion-de-cursos-con-bd.md) · **Siguiente:** [Guía del Módulo 04 — CRUD de estudiantes con Swagger](../modulo-04-documentacion-apis-swagger/01-guia-crud-estudiantes-con-swagger.md)
