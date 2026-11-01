# 🧩 Guía paso a paso — Taller «Gestión de cursos» con base de datos · Fase II: una versión profesional (modelo, mapeo y repositorio adaptador)

> 👩‍🏫 **Material docente.** Continúa la [guía 01](01-guia-taller-gestion-de-cursos-con-bd.md): se parte de su proyecto terminado y se separa **el modelo del negocio** de **la entidad de la base de datos**.
> Contiene código completo: no se entrega a los estudiantes antes de resolver el taller.
>
> ✅ **Verificada.** El código se compiló y se ejecutó con Spring Boot 3.3 y MySQL 8. Las respuestas de la API, el fallo de seguridad de la Fase I y las pruebas unitarias que se muestran son salidas reales.
>
> 🧭 **Idea central.** En la Fase I una sola clase (`StudentEntity`) es a la vez **tabla**, **regla de negocio** y **JSON**. En la Fase II cada trabajo tiene su clase: `Student` (negocio), `StudentEntity` (tabla) y `StudentMapper` (el puente).
> **Cada paso explica qué se gana con ese cambio.**

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Refactorizar el proyecto de la guía 01 para que el **negocio no dependa de JPA**: crear los **modelos**, los **mapeadores**, los **contratos de repositorio** y sus **adaptadores**, sin cambiar lo que ve el cliente de la API.
>
> Al terminar, el estudiante:
>
> - Distingue **modelo** (negocio) de **entidad** (tabla) y explica por qué son clases distintas.
> - Escribe **mapeadores** que traducen en ambos sentidos y copian el `id` de forma consciente.
> - Define un **contrato de repositorio** y lo cumple con un **adaptador** que delega en Spring Data (patrón *puerto y adaptador*).
> - Reescribe **servicios y controladores** que ya no importan nada de JPA.
> - **Prueba un servicio sin Spring ni base de datos**, gracias al contrato.
> - Compara las dos fases y decide **cuándo vale la pena** el costo extra.

</details>

<details>
<summary><strong>📝 Introducción: el problema de la Fase I</strong></summary>

> ### 🔍 Los síntomas
>
> En la guía 01 todo funciona. Pero la misma clase cumple tres trabajos y eso deja problemas que se pueden ver y probar:
>
> | # | Síntoma en la Fase I | Dónde se ve |
> |---|---|---|
> | 1 | **El negocio conoce JPA.** `StudentService` importa `StudentEntity`, una clase con anotaciones de base de datos. | Servicios |
> | 2 | **La API expone la tabla.** Cualquier atributo o columna nueva de la entidad sale en el JSON. | Controladores |
> | 3 | **El cliente puede fijar el `id`.** Un `POST` con `"id":1` **sobrescribe** una fila existente (se demuestra en el Paso 0.2). | `POST /api/students` |
> | 4 | **Probar el servicio exige Spring Data y una base.** No hay forma simple de simular el repositorio. | Pruebas |
> | 5 | **Cambiar la tecnología de persistencia obliga a tocar servicios y controladores.** | Todo `business` |
>
> ### 🧰 La solución: el repositorio como adaptador
>
> El servicio habla con un **contrato** (una interfaz) en términos del **modelo**. Una clase `…RepositoryDb` cumple ese contrato: usa el repositorio de Spring Data y un **mapeador** para traducir entre modelo y entidad.
>
> ```text
> Controller ─▶ Service ─▶ StudentRepository (contrato, habla en modelos)
>                                  ▲
>                                  │ cumple
>                         StudentRepositoryDb ─▶ StudentMapper ─▶ StudentEntity
>                                  │              (modelo ↔ entidad)
>                                  └─▶ StudentJpaRepository (Spring Data) ─▶ Hibernate ─▶ MySQL
> ```
>
> ### ⚖️ El costo
>
> Por cada concepto se pasa de **2 clases** (entidad y repositorio de Spring Data) a **6** (modelo, entidad, mapeador, contrato, repositorio de Spring Data y adaptador), con atributos repetidos.
> En un proyecto pequeño, de un solo equipo y con la persistencia fija, la Fase I es válida. La Fase II compensa cuando el proyecto crece, cuando importa la seguridad de la API o cuando se quiere probar el negocio aislado.
>
> ### 🛠️ Requerimientos y restricciones
>
> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Se parte del proyecto terminado de la [guía 01](01-guia-taller-gestion-de-cursos-con-bd.md) (Pasos 0 a 4), con MySQL en marcha. |
> | **RNF-02** | **La API no cambia**: mismas rutas, mismos códigos y mismo JSON que en la Fase I. |
> | **RNF-03** | `business` no importa nada de `jakarta.persistence` ni de `persistence.entity`. |
> | **RNF-04** | Todos los nombres del código en **inglés**. Inyección por constructor. |
> | **RNF-05** | Duración: 1 h 30 min. |

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>

> | Paso | Minutos | Actividad | Quién escribe |
> |---|---:|---|---|
> | 0 | 0 – 15 | Punto de partida y **ver el problema** (el `id` falsificado) | Docente |
> | 1 | 15 – 25 | Modelos (`business.model`) | Docente y estudiantes |
> | 2 | 25 – 30 | Ajuste de las entidades (constructor con `id`) | Estudiantes |
> | 3 | 30 – 40 | Mapeadores (`persistence.mapper`) | Estudiantes |
> | 4 | 40 – 50 | Contratos de repositorio | Estudiantes |
> | 5 | 50 – 60 | Adaptadores `…RepositoryDb` | Estudiantes |
> | 6 | 60 – 70 | Servicios con modelos | Estudiantes |
> | 7 | 70 – 75 | Controladores con modelos | Estudiantes |
> | 8 | 75 – 82 | Probar: misma API, problema resuelto | Todos |
> | 9 | 82 – 90 | Probar un servicio **sin Spring** y cierre | Todos |
>
> > 💡 **Ritmo.** Los Pasos 1 a 5 se hacen primero con `Student` en vivo; `Course` y `Enrollment` se repiten igual. Si falta tiempo, el Paso 9 es demostración del docente.

</details>

<details>
<summary><strong>🏗️ Arquitectura: el árbol de archivos (🆕 nuevo · ✏️ cambia)</strong></summary>

> ```text
> 📁 src/main/java/com/academy
> ├── 📄 AcademyApplication.java
> ├── 📁 presentation
> │   └── 📁 controller
> │       ├── ✏️ StudentController.java         recibe y devuelve Student
> │       ├── ✏️ CourseController.java
> │       └── ✏️ EnrollmentController.java
> ├── 📁 business
> │   ├── 📁 model                              🆕 clases simples, sin JPA
> │   │   ├── 🆕 Student.java
> │   │   ├── 🆕 Course.java
> │   │   └── 🆕 Enrollment.java
> │   └── 📁 service
> │       ├── ✏️ StudentService.java            habla con el contrato, en modelos
> │       ├── ✏️ CourseService.java
> │       └── ✏️ EnrollmentService.java
> └── 📁 persistence
>     ├── 📁 entity                             la tabla
>     │   ├── ✏️ StudentEntity.java             constructor con id
>     │   ├── ✏️ CourseEntity.java
>     │   └── ✏️ EnrollmentEntity.java
>     ├── 📁 mapper                             🆕 modelo ↔ entidad
>     │   ├── 🆕 StudentMapper.java
>     │   ├── 🆕 CourseMapper.java
>     │   └── 🆕 EnrollmentMapper.java
>     └── 📁 repository
>         ├── 🆕 StudentRepository.java         contrato (modelos)
>         ├── 🆕 CourseRepository.java
>         ├── 🆕 EnrollmentRepository.java
>         ├── StudentJpaRepository.java         Spring Data (entidades), sin cambios
>         ├── CourseJpaRepository.java
>         ├── EnrollmentJpaRepository.java
>         ├── 🆕 StudentRepositoryDb.java       adaptador (@Repository)
>         ├── 🆕 CourseRepositoryDb.java
>         └── 🆕 EnrollmentRepositoryDb.java
>
> 📁 src/test/java/com/academy/business/service
> └── 🆕 EnrollmentServiceTest.java             prueba del servicio sin Spring (Paso 9)
> ```
>
> | Capa | Qué hace ahora | Qué conoce |
> |---|---|---|
> | `presentation.controller` | HTTP y códigos de respuesta | `business.model` y `business.service` |
> | `business.service` | Reglas del negocio | `business.model` y los **contratos** de repositorio |
> | `persistence.repository` | Contratos y adaptadores | Modelos **y** entidades (es el puente) |
> | `persistence.mapper` | Traducir modelo ↔ entidad | Modelos **y** entidades |
> | `persistence.entity` | La tabla | Solo JPA |
>
> <details>
> <summary><strong>🗺️ Diagrama de clases de <code>Student</code> (el mismo patrón se repite en <code>Course</code> y <code>Enrollment</code>)</strong></summary>
>
> > ```mermaid
> > classDiagram
> >     class StudentService {
> >         +create(Student) Optional~Student~
> >     }
> >     class StudentRepository {
> >         <<interface>>
> >         +existsByEmail(String) boolean
> >         +save(Student) Student
> >     }
> >     class StudentRepositoryDb {
> >         <<Repository>>
> >     }
> >     class StudentJpaRepository {
> >         <<interface>>
> >     }
> >     class StudentMapper {
> >         +toEntity(Student) StudentEntity
> >         +toModel(StudentEntity) Student
> >     }
> >     class Student {
> >         <<model>>
> >     }
> >     class StudentEntity {
> >         <<Entity>>
> >     }
> >
> >     StudentService --> StudentRepository : usa el contrato
> >     StudentRepository <|.. StudentRepositoryDb : cumple
> >     StudentRepositoryDb --> StudentJpaRepository : delega
> >     StudentRepositoryDb ..> StudentMapper : traduce con
> >     StudentJpaRepository ..> StudentEntity : gestiona
> >     StudentMapper ..> Student
> >     StudentMapper ..> StudentEntity
> > ```
> >
> > Arriba, lo que ve el **negocio** (modelos); abajo, lo que ve la **base** (entidades). `StudentRepositoryDb` y `StudentMapper` son el puente.
>
> </details>

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Punto de partida y ver el problema</strong></summary>

> <details>
> <summary><strong>Paso 0.1 · Comprobar que la Fase I funciona</strong></summary>
>
> > Se parte del proyecto de la guía 01 con MySQL en marcha (Paso 0.2 de esa guía). Se arranca la aplicación y se repiten las **nueve peticiones** del Paso 4.2 de la guía 01.
> > Sus respuestas son la **referencia**: al final de esta guía deben ser **idénticas**.
> >
> > > 💡 **Beneficio de este paso: una red de seguridad.** Un refactor cambia la estructura **sin cambiar el comportamiento**. Si se conoce el comportamiento de antes, se puede demostrar que no se rompió nada.
> >
> > ✅ **Punto de control 0:** las nueve peticiones responden `201`, `409`, `404` y `200` como en la guía 01.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 0.2 · Ver el problema: un cliente sobrescribe a otro</strong></summary>
>
> > Con la base vacía se crea a Ana y, después, se envía un `POST` que **trae un `id`** en el JSON:
> >
> > ```bash
> > curl -i -X POST localhost:8080/api/students -H "Content-Type: application/json" \
> >      -d '{"name":"Ana Torres","age":22,"email":"ana@mail.com"}'
> >
> > curl -i -X POST localhost:8080/api/students -H "Content-Type: application/json" \
> >      -d '{"id":1,"name":"Mallory","age":40,"email":"m@mail.com"}'
> > ```
> >
> > **Resultado real en la Fase I:**
> >
> > ```text
> > HTTP/1.1 201   {"id":1,"name":"Ana Torres","age":22,"email":"ana@mail.com"}
> > HTTP/1.1 201   {"id":1,"name":"Mallory","age":40,"email":"m@mail.com"}      ← ¡el mismo id!
> >
> > mysql> select id,name,email from student;
> > +----+---------+------------+
> > | id | name    | email      |
> > +----+---------+------------+
> > |  1 | Mallory | m@mail.com |        ← Ana desapareció
> > +----+---------+------------+
> > ```
> >
> > El servicio comprobó que el correo `m@mail.com` no existía y guardó. Como la entidad llegó con `id = 1`, `save` hizo un **`UPDATE`** de la fila de Ana.
> >
> > > 🔎 **Concepto (M4):** `JpaRepository.save` **inserta** si el `id` es `null` y **actualiza** si trae valor. Al aceptar la entidad completa del cliente, el cliente decide cuál de las dos cosas ocurre.
> > >
> > > 💡 **Beneficio de este paso: se siente el problema antes de ver la solución.** En el Paso 6 el servicio deja de aceptar el `id` del cliente y este mismo `curl` crea un estudiante nuevo.
> >
> > 🗣️ **Pregunta:** ¿qué capa debería impedir esto? *(El servicio: es quien decide qué se guarda. Pero hoy recibe una entidad con el `id` ya puesto por el cliente.)*
>
> </details>
>

</details>

---

<details>
<summary><strong>📦 Paso 1 — Los modelos (<code>business.model</code>)</strong></summary>

> <details>
> <summary><strong>Paso 1.1 · <code>Student</code>, <code>Course</code> y <code>Enrollment</code></strong></summary>
>
> > Se crea el paquete `business.model` y tres `record` de Java. Son clases **simples**: solo datos, sin anotaciones de JPA ni de Jackson.
> >
> > | Modelo | Atributos | Diferencia con su entidad |
> > |---|---|---|
> > | `Student` | `id`, `name`, `age`, `email` | Sin `@Entity`, `@Id`, `@Column`… |
> > | `Course` | `id`, `name`, `price`, `maxCapacity` | Ídem |
> > | `Enrollment` | `id`, `student`, `course` | Contiene **modelos** `Student` y `Course`, no entidades |
> >
> > <details>
> > <summary><strong>📄 Student.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/model/Student.java`
> >
> > ```java
> > package com.academy.business.model;
> >
> > public record Student(Long id, String name, int age, String email) {
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 Course.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/model/Course.java`
> >
> > ```java
> > package com.academy.business.model;
> >
> > public record Course(Long id, String name, double price, int maxCapacity) {
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 Enrollment.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/model/Enrollment.java`
> >
> > ```java
> > package com.academy.business.model;
> >
> > public record Enrollment(Long id, Student student, Course course) {
> > }
> > ```
> >
> > </details>
> >
> > > - **`record`** (Java 16+) genera constructor, accesores (`student.name()`), `equals`, `hashCode` y `toString`. Es **inmutable**: una vez creado, no cambia.
> > > - Un modelo **no se modifica**: para «cambiar» algo se crea otro (se verá en el Paso 6).
> > >
> > > 💡 **Beneficio de este paso: el negocio tiene su propio vocabulario.** Las reglas se escriben sobre `Student`, que no sabe nada de tablas. Si mañana la base cambia, estas tres clases no se tocan. Además, al ser inmutables, nadie puede alterar un estudiante «por accidente» a mitad de una regla.
> >
> > ✅ **Punto de control 1:** el proyecto compila. `business.model` no tiene ni un `import jakarta`.
>
> </details>
>

</details>

---

<details>
<summary><strong>🔧 Paso 2 — Ajustar las entidades (constructor con <code>id</code>)</strong></summary>

> <details>
> <summary><strong>Paso 2.1 · Agregar el <code>id</code> al constructor</strong></summary>
>
> > En la Fase I el constructor de la entidad no recibía el `id`: solo servía para **insertar**. El mapeador necesita construir una entidad **con** `id` para poder **actualizar** una fila existente. Se reemplaza el constructor público de cada entidad (el `protected` sin argumentos se queda):
> >
> > <details>
> > <summary><strong>📄 StudentEntity.java</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/entity/StudentEntity.java`
> >
> > ```java
> > public StudentEntity(Long id, String name, int age, String email) {
> >     this.id = id;
> >     this.name = name;
> >     this.age = age;
> >     this.email = email;
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 CourseEntity.java</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/entity/CourseEntity.java`
> >
> > ```java
> > public CourseEntity(Long id, String name, double price, int maxCapacity) {
> >     this.id = id;
> >     this.name = name;
> >     this.price = price;
> >     this.maxCapacity = maxCapacity;
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 EnrollmentEntity.java</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/entity/EnrollmentEntity.java`
> >
> > ```java
> > public EnrollmentEntity(Long id, StudentEntity student, CourseEntity course) {
> >     this.id = id;
> >     this.student = student;
> >     this.course = course;
> > }
> > ```
> >
> > </details>
> >
> > > | Constructor | Qué permite |
> > > |---|---|
> > > | `new StudentEntity(null, …)` | **Insertar**: el `id` lo genera la base. |
> > > | `new StudentEntity(5L, …)` | **Actualizar** la fila 5. |
> > >
> > > 💡 **Beneficio de este paso: insertar y actualizar son decisiones explícitas.** El `id` ya no «se cuela» por casualidad; quien construye la entidad elige. La decisión de quién puede poner un `id` se toma en un solo sitio: el servicio (Paso 6).
> >
> > ✅ **Punto de control 2:** el proyecto **no compila** todavía en los servicios (aún usan el constructor viejo). Es esperado: se arreglan en el Paso 6.
>
> </details>
>

</details>

---

<details>
<summary><strong>🔁 Paso 3 — Los mapeadores (<code>persistence.mapper</code>)</strong></summary>

> <details>
> <summary><strong>Paso 3.1 · <code>StudentMapper</code>, <code>CourseMapper</code> y <code>EnrollmentMapper</code></strong></summary>
>
> > Se crea el paquete `persistence.mapper`. Cada mapeador es una clase `final` con **métodos estáticos** y constructor privado (no se instancia):
> >
> > - `toEntity(modelo)` → la entidad que la base entiende.
> > - `toModel(entidad)` → el modelo que el negocio entiende.
> >
> > <details>
> > <summary><strong>📄 StudentMapper.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/mapper/StudentMapper.java`
> >
> > ```java
> > package com.academy.persistence.mapper;
> >
> > import com.academy.business.model.Student;
> > import com.academy.persistence.entity.StudentEntity;
> >
> > public final class StudentMapper {
> >
> >     private StudentMapper() {
> >     }
> >
> >     public static StudentEntity toEntity(Student student) {
> >         return new StudentEntity(student.id(), student.name(), student.age(), student.email());
> >     }
> >
> >     public static Student toModel(StudentEntity entity) {
> >         return new Student(entity.getId(), entity.getName(), entity.getAge(), entity.getEmail());
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 CourseMapper.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/mapper/CourseMapper.java`
> >
> > ```java
> > package com.academy.persistence.mapper;
> >
> > import com.academy.business.model.Course;
> > import com.academy.persistence.entity.CourseEntity;
> >
> > public final class CourseMapper {
> >
> >     private CourseMapper() {
> >     }
> >
> >     public static CourseEntity toEntity(Course course) {
> >         return new CourseEntity(course.id(), course.name(), course.price(), course.maxCapacity());
> >     }
> >
> >     public static Course toModel(CourseEntity entity) {
> >         return new Course(entity.getId(), entity.getName(), entity.getPrice(), entity.getMaxCapacity());
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 EnrollmentMapper.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/mapper/EnrollmentMapper.java`
> >
> > ```java
> > package com.academy.persistence.mapper;
> >
> > import com.academy.business.model.Enrollment;
> > import com.academy.persistence.entity.EnrollmentEntity;
> >
> > public final class EnrollmentMapper {
> >
> >     private EnrollmentMapper() {
> >     }
> >
> >     public static EnrollmentEntity toEntity(Enrollment enrollment) {
> >         return new EnrollmentEntity(
> >                 enrollment.id(),
> >                 StudentMapper.toEntity(enrollment.student()),
> >                 CourseMapper.toEntity(enrollment.course()));
> >     }
> >
> >     public static Enrollment toModel(EnrollmentEntity entity) {
> >         return new Enrollment(
> >                 entity.getId(),
> >                 StudentMapper.toModel(entity.getStudent()),
> >                 CourseMapper.toModel(entity.getCourse()));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > - **`toEntity` copia el `id`**: `null` insertará y con valor actualizará.
> > > - `EnrollmentMapper` **reutiliza** los otros dos para traducir los objetos anidados.
> > > - Los mapeadores van en `persistence` porque son los únicos que conocen **las dos** clases.
> > >
> > > 💡 **Beneficio de este paso: la traducción vive en un solo lugar.** Si se agrega la columna `phone`, se cambian la entidad, el modelo y **este** archivo; los servicios y los controladores no se enteran. Sin mapeador, ese cambio se esparce por todo el proyecto.
> > >
> > > 🔎 **Concepto:** hacerlo a mano permite ver qué pasa. Herramientas como **MapStruct** generan este código, pero conviene entender primero el manual.
> >
> > ✅ **Punto de control 3:** los mapeadores compilan. Ninguno tiene lógica de negocio: solo copia atributos.
>
> </details>
>

</details>

---

<details>
<summary><strong>📜 Paso 4 — Los contratos de repositorio</strong></summary>

> <details>
> <summary><strong>Paso 4.1 · <code>StudentRepository</code>, <code>CourseRepository</code> y <code>EnrollmentRepository</code></strong></summary>
>
> > En `persistence.repository` se crean tres **interfaces** con lo que el servicio **necesita**, hablando solo en **modelos**. No extienden `JpaRepository`.
> >
> > <details>
> > <summary><strong>📄 StudentRepository.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/repository/StudentRepository.java`
> >
> > ```java
> > package com.academy.persistence.repository;
> >
> > import com.academy.business.model.Student;
> >
> > import java.util.List;
> > import java.util.Optional;
> >
> > public interface StudentRepository {
> >
> >     List<Student> findAll();
> >
> >     Optional<Student> findById(Long id);
> >
> >     boolean existsByEmail(String email);
> >
> >     Student save(Student student);
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 CourseRepository.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/repository/CourseRepository.java`
> >
> > ```java
> > package com.academy.persistence.repository;
> >
> > import com.academy.business.model.Course;
> >
> > import java.util.List;
> > import java.util.Optional;
> >
> > public interface CourseRepository {
> >
> >     List<Course> findAll();
> >
> >     Optional<Course> findById(Long id);
> >
> >     Course save(Course course);
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 EnrollmentRepository.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/repository/EnrollmentRepository.java`
> >
> > ```java
> > package com.academy.persistence.repository;
> >
> > import com.academy.business.model.Enrollment;
> >
> > public interface EnrollmentRepository {
> >
> >     boolean existsByStudentAndCourse(Long studentId, Long courseId);
> >
> >     long countByCourse(Long courseId);
> >
> >     Enrollment save(Enrollment enrollment);
> > }
> > ```
> >
> > </details>
> >
> > > | Contrato | Métodos | De dónde salen |
> > > |---|---|---|
> > > | `StudentRepository` | `findAll`, `findById`, `existsByEmail`, `save` | Lo que usan `StudentService` y `EnrollmentService` |
> > > | `CourseRepository` | `findAll`, `findById`, `save` | Lo que usan `CourseService` y `EnrollmentService` |
> > > | `EnrollmentRepository` | `existsByStudentAndCourse`, `countByCourse`, `save` | Las reglas de matrícula |
> > >
> > > - Los nombres son **del negocio** (`existsByEmail`), no de Spring Data (`existsByEmailIgnoreCase`): el contrato no revela cómo se resuelve.
> > > - Solo están los métodos que el negocio usa: **nada de `deleteAll`, `flush` o `findAll(Pageable)`**.
> > >
> > > 💡 **Beneficio de este paso: el servicio depende de lo que necesita, no de una tecnología.** Con `JpaRepository` el servicio «ve» más de cien métodos; con el contrato ve cuatro. Y, lo más importante, **cualquier clase puede cumplirlo**: una con MySQL, una en memoria, una de prueba (Paso 9).
> >
> > ✅ **Punto de control 4:** tres interfaces nuevas. Todavía nadie las implementa.
>
> </details>
>

</details>

---

<details>
<summary><strong>🔌 Paso 5 — Los adaptadores (<code>…RepositoryDb</code>)</strong></summary>

> <details>
> <summary><strong>Paso 5.1 · <code>StudentRepositoryDb</code>, <code>CourseRepositoryDb</code> y <code>EnrollmentRepositoryDb</code></strong></summary>
>
> > Cada adaptador **cumple el contrato** y trabaja con tres piezas: el repositorio de Spring Data (la base), el mapeador (la traducción) y el modelo (lo que devuelve). Lleva `@Repository` porque es una **clase concreta** de la capa de persistencia.
> >
> > <details>
> > <summary><strong>📄 StudentRepositoryDb.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/repository/StudentRepositoryDb.java`
> >
> > ```java
> > package com.academy.persistence.repository;
> >
> > import com.academy.business.model.Student;
> > import com.academy.persistence.mapper.StudentMapper;
> > import org.springframework.stereotype.Repository;
> >
> > import java.util.List;
> > import java.util.Optional;
> >
> > @Repository
> > public class StudentRepositoryDb implements StudentRepository {
> >
> >     private final StudentJpaRepository jpa;
> >
> >     public StudentRepositoryDb(StudentJpaRepository jpa) {
> >         this.jpa = jpa;
> >     }
> >
> >     @Override
> >     public List<Student> findAll() {
> >         return jpa.findAll().stream().map(StudentMapper::toModel).toList();
> >     }
> >
> >     @Override
> >     public Optional<Student> findById(Long id) {
> >         return jpa.findById(id).map(StudentMapper::toModel);
> >     }
> >
> >     @Override
> >     public boolean existsByEmail(String email) {
> >         return jpa.existsByEmailIgnoreCase(email);
> >     }
> >
> >     @Override
> >     public Student save(Student student) {
> >         return StudentMapper.toModel(jpa.save(StudentMapper.toEntity(student)));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 CourseRepositoryDb.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/repository/CourseRepositoryDb.java`
> >
> > ```java
> > package com.academy.persistence.repository;
> >
> > import com.academy.business.model.Course;
> > import com.academy.persistence.mapper.CourseMapper;
> > import org.springframework.stereotype.Repository;
> >
> > import java.util.List;
> > import java.util.Optional;
> >
> > @Repository
> > public class CourseRepositoryDb implements CourseRepository {
> >
> >     private final CourseJpaRepository jpa;
> >
> >     public CourseRepositoryDb(CourseJpaRepository jpa) {
> >         this.jpa = jpa;
> >     }
> >
> >     @Override
> >     public List<Course> findAll() {
> >         return jpa.findAll().stream().map(CourseMapper::toModel).toList();
> >     }
> >
> >     @Override
> >     public Optional<Course> findById(Long id) {
> >         return jpa.findById(id).map(CourseMapper::toModel);
> >     }
> >
> >     @Override
> >     public Course save(Course course) {
> >         return CourseMapper.toModel(jpa.save(CourseMapper.toEntity(course)));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 EnrollmentRepositoryDb.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/repository/EnrollmentRepositoryDb.java`
> >
> > ```java
> > package com.academy.persistence.repository;
> >
> > import com.academy.business.model.Enrollment;
> > import com.academy.persistence.mapper.EnrollmentMapper;
> > import org.springframework.stereotype.Repository;
> >
> > @Repository
> > public class EnrollmentRepositoryDb implements EnrollmentRepository {
> >
> >     private final EnrollmentJpaRepository jpa;
> >
> >     public EnrollmentRepositoryDb(EnrollmentJpaRepository jpa) {
> >         this.jpa = jpa;
> >     }
> >
> >     @Override
> >     public boolean existsByStudentAndCourse(Long studentId, Long courseId) {
> >         return jpa.existsByStudentIdAndCourseId(studentId, courseId);
> >     }
> >
> >     @Override
> >     public long countByCourse(Long courseId) {
> >         return jpa.countByCourseId(courseId);
> >     }
> >
> >     @Override
> >     public Enrollment save(Enrollment enrollment) {
> >         return EnrollmentMapper.toModel(jpa.save(EnrollmentMapper.toEntity(enrollment)));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Línea | Qué hace |
> > > |---|---|
> > > | `implements StudentRepository` | Se compromete a cumplir el contrato. |
> > > | `private final StudentJpaRepository jpa` | Recibe Spring Data **por constructor** y delega en él. |
> > > | `jpa.findAll().stream().map(StudentMapper::toModel).toList()` | La base devuelve entidades; el adaptador devuelve **modelos**. |
> > > | `jpa.save(StudentMapper.toEntity(student))` | Traduce hacia la base, guarda y traduce de vuelta: **devuelve un modelo nuevo** con el `id` ya generado. |
> > > | `existsByEmail` → `jpa.existsByEmailIgnoreCase` | El nombre del negocio se resuelve con el método derivado de Spring Data. |
> > >
> > > - **`@Repository`** va aquí (clase concreta); las interfaces `…JpaRepository` no lo necesitan. Además traduce las excepciones propias de la base a las de Spring.
> > > - `StudentJpaRepository` **no cambia**: sigue gestionando entidades.
> > >
> > > 💡 **Beneficio de este paso: un único punto de contacto con la base.** Es el único lugar donde conviven modelo y entidad. Para pasar a otra base, o a otra tecnología, se escribe **otro adaptador** que cumpla el mismo contrato; los servicios no se tocan.
> > >
> > > ⚠️ **Detalle importante.** `save` no modifica el objeto recibido: **devuelve** un modelo nuevo. Los records son inmutables, así que no hay otra forma, y es mejor así: no hay efectos ocultos.
> >
> > ✅ **Punto de control 5:** cada contrato tiene un adaptador. Aún no arranca (los servicios no se han ajustado).
>
> </details>
>

</details>

---

<details>
<summary><strong>⚙️ Paso 6 — Servicios con modelos (<code>business.service</code>)</strong></summary>

> <details>
> <summary><strong>Paso 6.1 · <code>StudentService</code> y <code>CourseService</code></strong></summary>
>
> > Los servicios cambian **de colaboradores**, no de reglas: ahora reciben los **contratos** y trabajan con **modelos**. Las reglas (correo único, precio y cupo mayores que cero) son las mismas.
> >
> > <details>
> > <summary><strong>📄 StudentService.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/StudentService.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > import com.academy.business.model.Student;
> > import com.academy.persistence.repository.StudentRepository;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
> >
> > import java.util.List;
> > import java.util.Optional;
> >
> > @Service
> > public class StudentService {
> >
> >     private final StudentRepository students;
> >
> >     public StudentService(StudentRepository students) {
> >         this.students = students;
> >     }
> >
> >     @Transactional(readOnly = true)
> >     public List<Student> findAll() {
> >         return students.findAll();
> >     }
> >
> >     @Transactional
> >     public Optional<Student> create(Student student) {
> >         if (students.existsByEmail(student.email())) {
> >             return Optional.empty();
> >         }
> >         Student toSave = new Student(null, student.name(), student.age(), student.email());
> >         return Optional.of(students.save(toSave));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 CourseService.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/CourseService.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > import com.academy.business.model.Course;
> > import com.academy.persistence.repository.CourseRepository;
> > import com.academy.persistence.repository.EnrollmentRepository;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
> >
> > import java.util.List;
> > import java.util.Optional;
> >
> > @Service
> > public class CourseService {
> >
> >     private final CourseRepository courses;
> >     private final EnrollmentRepository enrollments;
> >
> >     public CourseService(CourseRepository courses, EnrollmentRepository enrollments) {
> >         this.courses = courses;
> >         this.enrollments = enrollments;
> >     }
> >
> >     @Transactional(readOnly = true)
> >     public List<Course> findAll() {
> >         return courses.findAll();
> >     }
> >
> >     @Transactional
> >     public Optional<Course> create(Course course) {
> >         if (course.price() <= 0 || course.maxCapacity() <= 0) {
> >             return Optional.empty();
> >         }
> >         Course toSave = new Course(null, course.name(), course.price(), course.maxCapacity());
> >         return Optional.of(courses.save(toSave));
> >     }
> >
> >     @Transactional(readOnly = true)
> >     public Optional<Long> countEnrolled(Long courseId) {
> >         if (courses.findById(courseId).isEmpty()) {
> >             return Optional.empty();
> >         }
> >         return Optional.of(enrollments.countByCourse(courseId));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Cambio | Antes (Fase I) | Ahora (Fase II) |
> > > |---|---|---|
> > > | Tipo del repositorio | `StudentJpaRepository` | `StudentRepository` (contrato) |
> > > | Tipo que circula | `StudentEntity` | `Student` |
> > > | Regla del correo | `existsByEmailIgnoreCase` | `existsByEmail` |
> > > | Guardar | `students.save(student)` | `students.save(new Student(null, …))` |
> > >
> > > - **`new Student(null, student.name(), …)`** crea un modelo **sin `id`**, sin importar el que llegó. Así `save` siempre **inserta**.
> > > - Los servicios ya no importan `persistence.entity` ni `jakarta`: solo `business.model` y el contrato.
> > >
> > > 💡 **Beneficio de este paso: se cierra el hueco de seguridad.** El `id` que mande el cliente se descarta aquí. El `curl` del Paso 0.2 ahora crea un **tercer** estudiante en lugar de pisar al primero (se comprueba en el Paso 8).
>
> </details>
>
>
> <details>
> <summary><strong>Paso 6.2 · <code>EnrollmentService</code></strong></summary>
>
> > Mismas cuatro comprobaciones de la Fase I (existen los dos, no hay duplicado, hay cupo), ahora sobre contratos y modelos.
> >
> > <details>
> > <summary><strong>📄 EnrollmentService.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/EnrollmentService.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > import com.academy.business.model.Course;
> > import com.academy.business.model.Enrollment;
> > import com.academy.business.model.Student;
> > import com.academy.persistence.repository.CourseRepository;
> > import com.academy.persistence.repository.EnrollmentRepository;
> > import com.academy.persistence.repository.StudentRepository;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
> >
> > import java.util.Optional;
> >
> > @Service
> > public class EnrollmentService {
> >
> >     private final StudentRepository students;
> >     private final CourseRepository courses;
> >     private final EnrollmentRepository enrollments;
> >
> >     public EnrollmentService(StudentRepository students, CourseRepository courses,
> >                              EnrollmentRepository enrollments) {
> >         this.students = students;
> >         this.courses = courses;
> >         this.enrollments = enrollments;
> >     }
> >
> >     @Transactional
> >     public Optional<Enrollment> enroll(Long studentId, Long courseId) {
> >         Optional<Student> student = students.findById(studentId);
> >         Optional<Course> course = courses.findById(courseId);
> >
> >         if (student.isEmpty() || course.isEmpty()) {
> >             return Optional.empty();
> >         }
> >         if (enrollments.existsByStudentAndCourse(studentId, courseId)) {
> >             return Optional.empty();
> >         }
> >         if (enrollments.countByCourse(courseId) >= course.get().maxCapacity()) {
> >             return Optional.empty();
> >         }
> >         return Optional.of(enrollments.save(new Enrollment(null, student.get(), course.get())));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Línea | Qué cambia |
> > > |---|---|
> > > | `course.get().maxCapacity()` | Se lee con el accesor del `record`, no con `getMaxCapacity()`. |
> > > | `enrollments.existsByStudentAndCourse(…)` | Nombre del negocio; el adaptador lo traduce a `existsByStudentIdAndCourseId`. |
> > > | `new Enrollment(null, student.get(), course.get())` | La matrícula nueva se arma con **modelos** y sin `id`. |
> > > | `@Transactional` | Se mantiene: las lecturas y el `insert` siguen siendo **una sola operación**. |
> > >
> > > 💡 **Beneficio de este paso: las reglas se leen como reglas.** Nada de entidades, `Optional` de JPA o claves foráneas: solo estudiantes, cursos y matrículas.
> > >
> > > > 🔎 **Regla de oro de la capa.** Se puede comprobar con una búsqueda: el siguiente comando **no debe devolver nada**.
> > > >
> > > > ```bash
> > > > grep -rn -E "jakarta|\.entity\.|JpaRepository" src/main/java/com/academy/business
> > > > ```
> >
> > ✅ **Punto de control 6:** el proyecto compila. La búsqueda de arriba no encuentra ninguna línea.
>
> </details>
>

</details>

---

<details>
<summary><strong>🌐 Paso 7 — Controladores con modelos (<code>presentation.controller</code>)</strong></summary>

> <details>
> <summary><strong>Paso 7.1 · Cambiar el tipo, no la lógica</strong></summary>
>
> > Los tres controladores cambian **solo los tipos**: donde decía `StudentEntity` ahora dice `Student` (y `import com.academy.business.model.Student`). Rutas, códigos HTTP y `.map(…).orElse(…)` son los mismos.
> >
> > <details>
> > <summary><strong>📄 StudentController.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > package com.academy.presentation.controller;
> >
> > import com.academy.business.model.Student;
> > import com.academy.business.service.StudentService;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.http.ResponseEntity;
> > import org.springframework.web.bind.annotation.GetMapping;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.RequestBody;
> > import org.springframework.web.bind.annotation.RequestMapping;
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
> >     public List<Student> findAll() {
> >         return service.findAll();
> >     }
> >
> >     @PostMapping
> >     public ResponseEntity<Student> create(@RequestBody Student student) {
> >         return service.create(student)
> >                 .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
> >                 .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 CourseController.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/CourseController.java`
> >
> > ```java
> > package com.academy.presentation.controller;
> >
> > import com.academy.business.model.Course;
> > import com.academy.business.service.CourseService;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.http.ResponseEntity;
> > import org.springframework.web.bind.annotation.GetMapping;
> > import org.springframework.web.bind.annotation.PathVariable;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.RequestBody;
> > import org.springframework.web.bind.annotation.RequestMapping;
> > import org.springframework.web.bind.annotation.RestController;
> >
> > import java.util.List;
> >
> > @RestController
> > @RequestMapping("/api/courses")
> > public class CourseController {
> >
> >     private final CourseService service;
> >
> >     public CourseController(CourseService service) {
> >         this.service = service;
> >     }
> >
> >     @GetMapping
> >     public List<Course> findAll() {
> >         return service.findAll();
> >     }
> >
> >     @PostMapping
> >     public ResponseEntity<Course> create(@RequestBody Course course) {
> >         return service.create(course)
> >                 .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
> >                 .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
> >     }
> >
> >     @GetMapping("/{id}/enrolled")
> >     public ResponseEntity<Long> countEnrolled(@PathVariable Long id) {
> >         return service.countEnrolled(id)
> >                 .map(ResponseEntity::ok)
> >                 .orElse(ResponseEntity.notFound().build());
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 EnrollmentController.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/EnrollmentController.java`
> >
> > ```java
> > package com.academy.presentation.controller;
> >
> > import com.academy.business.model.Enrollment;
> > import com.academy.business.service.EnrollmentService;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.http.ResponseEntity;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.RequestMapping;
> > import org.springframework.web.bind.annotation.RequestParam;
> > import org.springframework.web.bind.annotation.RestController;
> >
> > @RestController
> > @RequestMapping("/api/enrollments")
> > public class EnrollmentController {
> >
> >     private final EnrollmentService service;
> >
> >     public EnrollmentController(EnrollmentService service) {
> >         this.service = service;
> >     }
> >
> >     @PostMapping
> >     public ResponseEntity<Enrollment> enroll(@RequestParam Long studentId, @RequestParam Long courseId) {
> >         return service.enroll(studentId, courseId)
> >                 .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
> >                 .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > - `@RequestBody Student` deserializa el JSON directamente en el `record` (Jackson lo soporta).
> > > - La respuesta se serializa **desde el modelo**: sale lo que el modelo tiene, no lo que la tabla tenga.
> > >
> > > 💡 **Beneficio de este paso: la API ya no depende de la tabla.** Si se agrega la columna `created_at` a `StudentEntity`, el JSON **no cambia**. Para exponerla habría que agregarla al modelo a propósito.
> > >
> > > ⚠️ **Lo que todavía no está resuelto.** El controlador y el negocio comparten `Student`. El siguiente nivel serían **DTO** de entrada y salida (reto 2): por ejemplo, para que `POST` no acepte un `id` ni siquiera en el JSON.
> >
> > ✅ **Punto de control 7:** el proyecto compila y arranca.
>
> </details>
>

</details>

---

<details>
<summary><strong>🧪 Paso 8 — Probar: misma API, problema resuelto</strong></summary>

> <details>
> <summary><strong>Paso 8.1 · Las nueve peticiones: idénticas a la Fase I</strong></summary>
>
> > Con una base vacía se repiten las nueve peticiones del Paso 0.1. **Resultado real:**
> >
> > | Petición | Respuesta real |
> > |---|---|
> > | `POST /api/students` (Ana) | `201` · `{"id":1,"name":"Ana Torres","age":22,"email":"ana@mail.com"}` |
> > | `POST /api/students` (Luis) | `201` · `{"id":2,"name":"Luis Pérez","age":25,"email":"luis@mail.com"}` |
> > | `POST /api/students` con `ANA@mail.com` | `409` |
> > | `POST /api/courses` (cupo 1) | `201` · `{"id":1,"name":"Java from scratch","price":150000.0,"maxCapacity":1}` |
> > | `POST /api/courses` con precio `0` | `409` |
> > | `POST /api/enrollments?studentId=1&courseId=1` | `201` · `{"id":1,"student":{…},"course":{…}}` |
> > | La misma matrícula otra vez | `409` |
> > | `studentId=2&courseId=1` (curso lleno) | `409` |
> > | `studentId=9&courseId=1` | `409` |
> > | `GET /api/courses/1/enrolled` · `GET /api/courses/9/enrolled` | `200` · `1` — `404` |
> > | `GET /api/students` | `200` · lista de los dos estudiantes |
> >
> > > 💡 **Beneficio de este paso: el refactor es invisible para el cliente.** Mismas rutas, mismos códigos, mismo JSON. Esa es la definición de un buen refactor.
> >
> > ✅ **Punto de control 8.1:** las respuestas coinciden con las del Paso 0.1.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 8.2 · Repetir el ataque del <code>id</code> falsificado</strong></summary>
>
> > Con los dos estudiantes ya creados:
> >
> > ```bash
> > curl -i -X POST localhost:8080/api/students -H "Content-Type: application/json" \
> >      -d '{"id":99,"name":"Mallory","age":40,"email":"m@mail.com"}'
> > ```
> >
> > **Resultado real:**
> >
> > ```text
> > HTTP/1.1 201   {"id":3,"name":"Mallory","age":40,"email":"m@mail.com"}      ← id 3, no 99
> >
> > mysql> select id,name from student;
> > +----+------------+
> > | id | name       |
> > +----+------------+
> > |  1 | Ana Torres |
> > |  2 | Luis Pérez |
> > |  3 | Mallory    |
> > +----+------------+
> > ```
> >
> > Ana y Luis siguen intactos: el `99` se descartó en `StudentService` y la base asignó el `3`.
> >
> > > 💡 **Beneficio de este paso: la regla es del negocio y está en el negocio.** No depende de que el cliente «se porte bien» ni de que la entidad tenga o no setters.
> >
> > 🗣️ **Pregunta:** en la Fase I, ¿habría bastado quitar el `id` de la entidad? *(No: sin `id` no se podría actualizar nunca. La separación modelo/entidad permite que el `id` exista en las dos clases pero que solo el servicio decida cuándo se usa.)*
>
> </details>
>

</details>

---

<details>
<summary><strong>🧩 Paso 9 — Probar un servicio sin Spring ni base de datos</strong></summary>

> <details>
> <summary><strong>Paso 9.1 · <code>EnrollmentServiceTest</code> con repositorios falsos</strong></summary>
>
> > Como el servicio depende de **interfaces**, una prueba puede darle implementaciones propias que guardan en una lista. No arranca Spring, no abre MySQL y corre en milisegundos.
> >
> > Se agrega la dependencia de pruebas al `pom.xml` (si no está):
> >
> > ```xml
> > <dependency>
> >     <groupId>org.springframework.boot</groupId>
> >     <artifactId>spring-boot-starter-test</artifactId>
> >     <scope>test</scope>
> > </dependency>
> > ```
> >
> > <details>
> > <summary><strong>📄 EnrollmentServiceTest.java</strong></summary>
> >
> > **Ruta:** `src/test/java/com/academy/business/service/EnrollmentServiceTest.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > import com.academy.business.model.Course;
> > import com.academy.business.model.Enrollment;
> > import com.academy.business.model.Student;
> > import com.academy.persistence.repository.CourseRepository;
> > import com.academy.persistence.repository.EnrollmentRepository;
> > import com.academy.persistence.repository.StudentRepository;
> > import org.junit.jupiter.api.BeforeEach;
> > import org.junit.jupiter.api.Test;
> >
> > import java.util.ArrayList;
> > import java.util.List;
> > import java.util.Optional;
> >
> > import static org.junit.jupiter.api.Assertions.assertEquals;
> > import static org.junit.jupiter.api.Assertions.assertTrue;
> >
> > class EnrollmentServiceTest {
> >
> >     private final List<Enrollment> saved = new ArrayList<>();
> >     private EnrollmentService service;
> >
> >     @BeforeEach
> >     void setUp() {
> >         StudentRepository students = new StudentRepository() {
> >             public List<Student> findAll() { return List.of(); }
> >             public Optional<Student> findById(Long id) {
> >                 return id == 1 || id == 2 ? Optional.of(new Student(id, "Student " + id, 20, id + "@mail.com")) : Optional.empty();
> >             }
> >             public boolean existsByEmail(String email) { return false; }
> >             public Student save(Student student) { return student; }
> >         };
> >         CourseRepository courses = new CourseRepository() {
> >             public List<Course> findAll() { return List.of(); }
> >             public Optional<Course> findById(Long id) {
> >                 return id == 1 ? Optional.of(new Course(1L, "Java", 150000, 1)) : Optional.empty();
> >             }
> >             public Course save(Course course) { return course; }
> >         };
> >         EnrollmentRepository enrollments = new EnrollmentRepository() {
> >             public boolean existsByStudentAndCourse(Long studentId, Long courseId) {
> >                 return saved.stream().anyMatch(e -> e.student().id().equals(studentId) && e.course().id().equals(courseId));
> >             }
> >             public long countByCourse(Long courseId) {
> >                 return saved.stream().filter(e -> e.course().id().equals(courseId)).count();
> >             }
> >             public Enrollment save(Enrollment enrollment) {
> >                 Enrollment withId = new Enrollment((long) saved.size() + 1, enrollment.student(), enrollment.course());
> >                 saved.add(withId);
> >                 return withId;
> >             }
> >         };
> >         service = new EnrollmentService(students, courses, enrollments);
> >     }
> >
> >     @Test
> >     void enrollsWhenThereIsRoom() {
> >         assertTrue(service.enroll(1L, 1L).isPresent());
> >         assertEquals(1, saved.size());
> >     }
> >
> >     @Test
> >     void rejectsTheSameEnrollmentTwice() {
> >         service.enroll(1L, 1L);
> >         assertTrue(service.enroll(1L, 1L).isEmpty());
> >     }
> >
> >     @Test
> >     void rejectsWhenTheCourseIsFull() {
> >         service.enroll(1L, 1L);
> >         assertTrue(service.enroll(2L, 1L).isEmpty());
> >         assertEquals(1, saved.size());
> >     }
> >
> >     @Test
> >     void rejectsUnknownStudentOrCourse() {
> >         assertTrue(service.enroll(9L, 1L).isEmpty());
> >         assertTrue(service.enroll(1L, 9L).isEmpty());
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Elemento | Para qué sirve |
> > > |---|---|
> > > | `new StudentRepository() { … }` | Una clase anónima que **cumple el contrato** con datos fijos: el estudiante 1 y el 2 existen, el 9 no. |
> > > | `saved` (una `List`) | Hace de «base de datos»: `save` agrega, `countByCourse` cuenta. |
> > > | `@Test` | Cada método comprueba **una** regla: cupo, duplicado, inexistentes. |
> > > | `service.enroll(2L, 1L).isEmpty()` | Con el curso de cupo 1 ya ocupado, la segunda matrícula se rechaza. |
> > >
> > > Se ejecuta con `./mvnw test`. **Resultado real:**
> > >
> > > ```text
> > > Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
> > > BUILD SUCCESS
> > > ```
> > >
> > > 💡 **Beneficio de este paso: es la ganancia que más se nota.** En la Fase I, probar `EnrollmentService` exigía una base real o simular tres repositorios de Spring Data (más de cien métodos cada uno). Ahora son cuatro pruebas de unos 60 milisegundos, sin infraestructura. Cada regla del negocio se puede verificar **en segundos** y en cualquier máquina.
> >
> > ✅ **Punto de control 9:** `Tests run: 4, Failures: 0`.
>
> </details>
>

</details>

---

<details>
<summary><strong>⚖️ Balance: Fase I frente a Fase II</strong></summary>

> | Aspecto | Fase I (guía 01) | Fase II (esta guía) |
> |---|---|---|
> | Clases por concepto | 2 | 6 |
> | ¿`business` conoce JPA? | Sí | **No** (comprobable con `grep`) |
> | ¿El JSON depende de la tabla? | Sí | **No** |
> | ¿El cliente puede fijar el `id`? | **Sí** (sobrescribe filas) | **No** |
> | Probar un servicio | Base real o simulación pesada | Repositorio falso de 10 líneas |
> | Cambiar de tecnología de persistencia | Tocar servicios y controladores | Escribir otro adaptador |
> | Agregar un atributo | Entidad (y por arrastre, API) | Entidad, modelo y mapeador |
> | Tiempo de escritura | Menor | Mayor (unos 25 min más) |
>
> > 🔎 **Lectura clave.** La Fase II no hace el programa «más rápido» ni «más corto»: lo hace **más seguro de cambiar**. Su valor crece con el tamaño del proyecto y con el número de personas que lo tocan.
> >
> > ⚠️ **Una limitación honesta.** Los contratos viven en `persistence.repository` y mencionan `business.model`; los servicios importan esos contratos. Hay una **dependencia circular entre paquetes**. Una versión más estricta (arquitectura hexagonal) pondría los contratos en `business`. Aquí se mantiene el diseño del taller por simplicidad.

</details>

<details>
<summary><strong>🏁 Cierre — preguntas de síntesis</strong></summary>

> 1. ¿Qué trabajo hace cada una de las tres clases `Student`, `StudentEntity` y `StudentMapper`? *(Negocio, tabla y traducción.)*
> 2. ¿Por qué `toEntity` copia el `id`? *(Para poder actualizar; el servicio decide cuándo ese `id` es `null`.)*
> 3. ¿Quién decide si `save` inserta o actualiza? *(El `id` del objeto que se guarda; y el servicio controla ese `id`.)*
> 4. ¿Por qué el contrato tiene cuatro métodos y no los de `JpaRepository`? *(El servicio solo debe depender de lo que usa.)*
> 5. ¿Dónde va `@Repository` y por qué no en `StudentJpaRepository`? *(En el adaptador: es una clase concreta. Las interfaces de Spring Data se registran solas.)*
> 6. ¿Qué cambia en el servicio si MySQL se reemplaza por otra base? *(Nada: se escribe otro adaptador.)*
> 7. ¿Cuándo **no** vale la pena la Fase II? *(Proyectos pequeños, de un solo equipo y con la persistencia fija.)*

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Solución |
> |---|---|---|
> | `Parameter 0 of constructor in …StudentService required a bean of type …StudentRepository` | Falta `@Repository` en `StudentRepositoryDb` | Agregarlo |
> | `NoUniqueBeanDefinitionException` para un contrato | Dos clases cumplen el mismo contrato | Dejar una, o usar `@Primary` en la que debe ganar |
> | `The constructor StudentEntity(String, int, String) is undefined` | Quedó el constructor viejo en algún lado | Usar el de cuatro parámetros con `id` primero |
> | `TransientPropertyValueException` / `object references an unsaved transient instance` al matricular | Se mapeó una matrícula con un estudiante o curso **sin `id`** | Cargar primero el modelo desde la base (como hace `EnrollmentService`) |
> | El estudiante nuevo se guarda con el `id` que mandó el cliente | El servicio guardó el modelo recibido en lugar de uno con `id` `null` | Crear `new Student(null, …)` antes de guardar |
> | `NullPointerException` en un mapeador | Se pasó `null` a `toEntity` o `toModel` | Comprobar el `Optional` antes de mapear |
> | La respuesta JSON trae campos que no están en el modelo | Se devolvió la entidad desde el controlador | Devolver el modelo |
> | `Tests run … Errors: 1` con `NoSuchBeanDefinition` | La prueba usó `@SpringBootTest` sin necesidad | Para probar un servicio con repositorios falsos no se usa Spring |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **Una columna nueva.** Agregar `phone` a `StudentEntity` y comprobar que el JSON de `GET /api/students` **no cambia**. Después agregarlo al modelo y al mapeador, y ver cómo aparece.
> 2. **DTO de entrada.** Crear `presentation/dto/StudentRequest` (`name`, `age`, `email`) y convertirlo a `Student` en el controlador, para que `POST` ni siquiera tenga un campo `id`.
> 3. **Actualizar.** Agregar `PUT /api/students/{id}`: el servicio carga el estudiante, crea `new Student(id, …)` con los datos nuevos y llama a `save` (que ahora **actualiza**).
> 4. **Repositorio en memoria.** Escribir `StudentRepositoryInMemory` que cumpla `StudentRepository` con una `List`, y cambiar el adaptador con `@Primary`. Comprobar que **ningún** servicio se modifica.
> 5. **MapStruct.** Reemplazar los tres mapeadores por interfaces de MapStruct y comparar la cantidad de código.
> 6. **Pruebas para `StudentService`.** Con un repositorio falso, comprobar el correo repetido sin importar mayúsculas.

</details>

<details>
<summary><strong>📁 Estructura final esperada</strong></summary>

> ```text
> 📁 academy
> ├── 📄 pom.xml
> └── 📁 src
>     ├── 📁 main/java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 presentation
>     │   │   └── 📁 controller    StudentController · CourseController · EnrollmentController
>     │   ├── 📁 business
>     │   │   ├── 📁 model         Student · Course · Enrollment
>     │   │   └── 📁 service       StudentService · CourseService · EnrollmentService
>     │   └── 📁 persistence
>     │       ├── 📁 entity        StudentEntity · CourseEntity · EnrollmentEntity
>     │       ├── 📁 mapper        StudentMapper · CourseMapper · EnrollmentMapper
>     │       └── 📁 repository    StudentRepository · CourseRepository · EnrollmentRepository
>     │                            StudentJpaRepository · CourseJpaRepository · EnrollmentJpaRepository
>     │                            StudentRepositoryDb · CourseRepositoryDb · EnrollmentRepositoryDb
>     ├── 📁 main/resources
>     │   └── 📄 application.properties
>     └── 📁 test/java/com/academy/business/service
>         └── 📄 EnrollmentServiceTest.java
> ```

</details>

<details>
<summary><strong>🏷️ Repaso de conceptos y anotaciones</strong></summary>

> | Elemento | Dónde | Para qué sirve |
> |---|---|---|
> | `record` | `business.model` | Clase de datos inmutable: accesores, `equals` y `toString` automáticos. |
> | Mapeador (`final`, métodos `static`) | `persistence.mapper` | Traduce modelo ↔ entidad en un solo lugar. |
> | Interfaz de contrato | `persistence.repository` | Lo que el negocio necesita de la persistencia, en modelos. |
> | `@Repository` | `…RepositoryDb` | Registra el adaptador y traduce excepciones de la base. |
> | `@Service` · `@Transactional` | `business.service` | Servicio con reglas; una operación completa o ninguna. |
> | `@RestController` · `@RequestMapping` · `@GetMapping` · `@PostMapping` | `presentation.controller` | API REST. |
> | `@Entity` · `@Table` · `@Id` · `@GeneratedValue` · `@Column` · `@ManyToOne` · `@JoinColumn` | `persistence.entity` | La tabla. **Solo** aquí. |
>
> ```text
> HTTP ─▶ Controller ─▶ Service ─▶ Repository (contrato) ◀─ RepositoryDb ─▶ Mapper ─▶ Entity ─▶ MySQL
>        (Student)     (Student)     (Student)                                (StudentEntity)
> ```
>
> > 🔎 **Lectura clave.** Los modelos viven en el negocio; las entidades, en la persistencia; el mapeador y el adaptador son el único lugar donde se encuentran.

</details>

➡️ **Anterior:** [Guía 01 — Taller «Gestión de cursos» con base de datos](01-guia-taller-gestion-de-cursos-con-bd.md) · **Misma idea en otro proyecto:** [Guía 05 — Tienda, Fase II](ejemplo-tienda/04-guia-tienda-fase-2-buenas-practicas.md)
