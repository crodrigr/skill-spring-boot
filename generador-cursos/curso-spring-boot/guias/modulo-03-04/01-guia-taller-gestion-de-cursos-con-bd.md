# 🗄️ Guía paso a paso — Taller «Gestión de cursos» con base de datos (JPA): entidades y repositorios

> 👩‍🏫 **Material docente.** Guía para resolver **junto con los estudiantes**, en vivo, la capa de persistencia del taller «Gestión de cursos» con base de datos.
> Aplica lo visto en el [Módulo 03](../../03-introduccion-a-jpa/) y el [Módulo 04](../../04-gestion-de-bases-de-datos-mediante-jpa/).
> Contiene código completo: no se entrega a los estudiantes antes de resolver el taller.
>
> ✅ **Verificada.** El código de esta guía se compiló y se ejecutó con Spring Boot 3.3 y H2: las tablas, los resultados y el SQL que se muestran son salidas reales.
>
> 🧭 **Idea central.** Las clases que se guardan en la base (`…Entity`) viven en `persistence.entity`. Esta guía trabaja **solo** esa capa: las **entidades** y sus **repositorios de Spring Data**.
> El modelo del negocio, los mapeadores, los servicios y los controladores **no se trabajan aquí**: se resuelven en otra guía.

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Crear la **capa de persistencia** de **estudiantes, cursos y matrículas** con **Spring Data JPA** y una base de datos relacional (**H2**): declarar las entidades, guardarlas y consultarlas.
>
> Al terminar, el estudiante:
>
> - Declara **entidades JPA** (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@ManyToOne`, `@JoinColumn`) en `persistence.entity` (M3 y M4).
> - Configura la conexión a **H2** en `application.properties` y lee el SQL que genera Hibernate (M3 y M4).
> - Crea interfaces `JpaRepository` con **métodos derivados** (`existsBy…`, `countBy…`) (M3).
> - **Guarda y consulta** datos desde la clase `main` (`CommandLineRunner`), sin API.
> - Verifica que los datos **sobreviven al reinicio** usando H2 en archivo.

</details>

<details>
<summary><strong>📝 Introducción: el problema y qué se construye</strong></summary>

> ### 🏫 El problema
>
> Una **academia de programación** (*academy*) necesita guardar **estudiantes**, **cursos** y **matrículas** en una base de datos. En esta guía se construye **solo la parte que habla con la base**; la lógica del negocio y la API REST quedan para otra guía.
>
> ### 🧩 Una entidad por concepto
>
> | Concepto | Entidad (capa de persistencia) | Tabla |
> |---|---|---|
> | Estudiante | `StudentEntity` | `student` |
> | Curso | `CourseEntity` | `course` |
> | Matrícula | `EnrollmentEntity` | `enrollment` (FK `student_id` y `course_id`) |
>
> La entidad describe **cómo se guarda** un concepto en una tabla.
> `EnrollmentEntity` es la **entidad intermedia** de la relación muchos a muchos entre `StudentEntity` y
> `CourseEntity` ([Ejemplo 04 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/04-manyToMany-y-entidad-intermedia.md)): cada una de sus dos relaciones es un `@ManyToOne`.
>
> ### 🛠️ Requerimientos no funcionales y restricciones
>
> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Java 17 o superior, Spring Boot 3 y Maven. |
> | **RNF-02** | Persistencia con **Spring Data JPA** y **H2**. Primero en memoria; al final, en **archivo**. |
> | **RNF-03** | Tres capas: `presentation` → `business` → `persistence`. Las anotaciones de JPA van **solo** en `persistence.entity`. |
> | **RNF-04** | Inyección por constructor. Todos los nombres del código (paquetes, clases, atributos, tablas) en **inglés**. |
> | **RNF-05** | Las pruebas se hacen desde la clase `main` (`CommandLineRunner`) y con el SQL que imprime Hibernate. |
> | **RNF-06** | Duración: 1 h 15 min. |
>
> > 💡 **Alcance.** Quedan fuera: el modelo del negocio, los mapeadores, los servicios y la API REST (otra guía), las relaciones bidireccionales, `cascade`,
> > migraciones (Flyway) y otros motores (MySQL/PostgreSQL).

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>

> | Paso | Minutos | Actividad | Quién escribe |
> |---|---:|---|---|
> | 0 | 0 – 10 | Preparación: proyecto, dependencias, `application.properties` y arranque | Docente |
> | 1 | 10 – 30 | Entidades JPA en `persistence.entity` (`StudentEntity` en vivo) | Docente y estudiantes |
> | 2 | 30 – 40 | Interfaces de Spring Data (`…JpaRepository`) | Estudiantes |
> | 3 | 40 – 60 | Guardar y consultar desde `main` y leer el SQL | Todos |
> | 4 | 60 – 75 | Persistencia real (H2 en archivo) y cierre | Todos |
>
> > 💡 **Ritmo.** El Paso 1 con `StudentEntity` fija el patrón; con `CourseEntity` y `EnrollmentEntity` los estudiantes lo repiten. Si falta tiempo, el
> > Paso 4 se hace como demostración del docente.

</details>

<details>
<summary><strong>🧠 Conceptos de los módulos 03 y 04 que se resaltan</strong></summary>

> | Concepto | Dónde aparece en el taller | Módulo | Ejemplo de apoyo |
> |---|---|:-:|---|
> | JPA y ORM | Las clases se guardan **sin escribir SQL** | 03 | [¿Qué es JPA?](../../03-introduccion-a-jpa/material/ejemplos/01-que-es-jpa.md) · [¿Qué es ORM?](../../03-introduccion-a-jpa/material/ejemplos/04-que-es-orm.md) |
> | Arquitectura de JPA e Hibernate | Hibernate genera las tablas y el SQL (visible con `show-sql`) | 03 | [Arquitectura de JPA](../../03-introduccion-a-jpa/material/ejemplos/02-arquitectura-de-jpa.md) |
> | Relacionamiento entre clases | `EnrollmentEntity` apunta a `StudentEntity` y a `CourseEntity` | 03 | [Relacionamiento entre clases](../../03-introduccion-a-jpa/material/ejemplos/03-relacionamiento-entre-clases.md) |
> | Spring Data JPA | `JpaRepository` y **métodos derivados** (`existsBy…`, `countBy…`) | 03 | [¿Qué es Spring Data JPA?](../../03-introduccion-a-jpa/material/ejemplos/06-que-es-spring-data-jpa.md) · [Uso básico](../../03-introduccion-a-jpa/material/ejemplos/07-uso-basico-de-spring-data-jpa.md) |
> | Anotaciones de JPA | `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@ManyToOne`, `@JoinColumn` | 03 | [Anotaciones en JPA](../../03-introduccion-a-jpa/material/ejemplos/08-anotaciones-en-jpa.md) |
> | Proyecto y conexión a la base de datos | `pom.xml`, `application.properties`, H2 | 04 | [Creación de un proyecto](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/01-creacion-de-un-proyecto-spring-boot.md) |
> | Lado dueño de la relación y `fetch` | `EnrollmentEntity` es dueña de las dos claves foráneas | 04 | [OneToMany/ManyToOne y fetch](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/03-oneToMany-manyToOne-y-fetch.md) |
> | Entidad intermedia | `EnrollmentEntity` une `StudentEntity` y `CourseEntity` | 04 | [ManyToMany y entidad intermedia](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/04-manyToMany-y-entidad-intermedia.md) |
> | CRUD | `save`, `findAll`, `count` | 04 | [CRUD completo](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/06-crud-completo.md) |
> | Inicialización del servidor | Hibernate crea las tablas al arrancar (`ddl-auto=update`) | 04 | [Inicialización del servidor](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/07-inicializacion-del-servidor.md) |

</details>

<details>
<summary><strong>🏗️ Arquitectura del taller: las capas y el árbol de archivos</strong></summary>

> El proyecto tiene **tres capas**. En esta guía solo se **trabaja** la de persistencia; las otras dos se crean vacías y se resuelven en otra guía.
>
> ```text
> 📁 src/main/java/com/academy
> ├── 📄 AcademyApplication.java                  (➕ la clase main; ejecuta los ejemplos)
> ├── 📁 presentation                              ← 1 · otra guía
> │   └── 📁 controller
> ├── 📁 business                                  ← 2 · otra guía
> │   └── 📁 service
> └── 📁 persistence                               ← 3 · lo único que se trabaja en esta guía
>     ├── 📁 repository    StudentJpaRepository · CourseJpaRepository · EnrollmentJpaRepository   (➕ Spring Data)
>     └── 📁 entity        StudentEntity · CourseEntity · EnrollmentEntity                         (➕ las @Entity)
> 📁 src/main/resources
> └── 📄 application.properties                    (➕ conexión a H2 y JPA)
> ```
>
> | Capa | Paquetes | Qué contiene | En esta guía |
> |---|---|---|---|
> | Presentación | `presentation.controller` | Controladores REST. | Vacía |
> | Negocio | `business.service` | Servicios con las reglas del negocio. | Vacía |
> | Persistencia | `persistence.repository`, `persistence.entity` | Repositorios de Spring Data y entidades JPA. | **Se trabaja** |
>
> ```text
> main (CommandLineRunner) ─▶ …JpaRepository ─▶ Hibernate ─▶ H2
>                             Spring Data lo genera   genera el SQL   la base
> ```
>
> > 🔎 **Qué no está.** El modelo del negocio y los mapeadores no se crean aquí; pertenecen a otra guía. Todos los nombres del proyecto (paquetes, clases, atributos, tablas y columnas) están en **inglés** para mantener un solo idioma en el código.

</details>

<details>
<summary><strong>🗺️ Diagrama de clases de las entidades</strong></summary>

> ```mermaid
> classDiagram
>     direction LR
>
>     class StudentEntity {
>         <<Entity>>
>         -Long id
>         -String name
>         -int age
>         -String email
>     }
>     class CourseEntity {
>         <<Entity>>
>         -Long id
>         -String name
>         -double price
>         -int maxCapacity
>     }
>     class EnrollmentEntity {
>         <<Entity>>
>         -Long id
>         -StudentEntity student
>         -CourseEntity course
>     }
>
>     EnrollmentEntity "*" --> "1" StudentEntity : @ManyToOne student_id
>     EnrollmentEntity "*" --> "1" CourseEntity : @ManyToOne course_id
> ```
>
> - Cada flecha es una **clave foránea** (`@ManyToOne` + `@JoinColumn`).
> - `EnrollmentEntity` es el **lado dueño**: la tabla `enrollment` guarda `student_id` y `course_id`; `StudentEntity` y `CourseEntity` **no** conocen sus matrículas (relación unidireccional).

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Preparación</strong></summary>

> <details>
> <summary><strong>Paso 0.1 · Crear el proyecto con las dependencias</strong></summary>
>
> > Se crea un proyecto nuevo en [start.spring.io](https://start.spring.io): **Maven · Java 17 o superior · Group** `com.academy` · **Artifact** `academy` ·
> > dependencias **Spring Data JPA** y **H2 Database**. **No** se agrega Spring Web: aquí no hay API.
> >
> > En el `pom.xml` quedan estas dos dependencias:
> >
> > ```xml
> > <dependency>
> >     <groupId>org.springframework.boot</groupId>
> >     <artifactId>spring-boot-starter-data-jpa</artifactId>
> > </dependency>
> > <dependency>
> >     <groupId>com.h2database</groupId>
> >     <artifactId>h2</artifactId>
> >     <scope>runtime</scope>
> > </dependency>
> > ```
> >
> > > 🔎 **Concepto (M3 y M4):** `spring-boot-starter-data-jpa` trae JPA, **Hibernate** y Spring Data; `h2` es el **driver** de la base,
> > > y `runtime` indica que solo hace falta al ejecutar.
>
> </details>
>
> <details>
> <summary><strong>Paso 0.2 · Configurar la conexión: <code>application.properties</code></strong></summary>
>
> > **Archivo:** `src/main/resources/application.properties`
> >
> > ```properties
> > # H2 in-memory connection
> > spring.datasource.url=jdbc:h2:mem:academy;DB_CLOSE_DELAY=-1
> > spring.datasource.driver-class-name=org.h2.Driver
> > spring.datasource.username=sa
> > spring.datasource.password=
> >
> > # JPA and Hibernate
> > spring.jpa.hibernate.ddl-auto=update
> > spring.jpa.show-sql=true
> > ```
> >
> > | Propiedad | Para qué sirve |
> > |---|---|
> > | `spring.datasource.url` | Dirección de la base. `jdbc:h2:mem:academy` crea una base H2 **en memoria** llamada `academy`; `DB_CLOSE_DELAY=-1` evita que se borre al cerrarse la primera conexión. |
> > | `spring.datasource.driver-class-name`, `username`, `password` | Driver y credenciales por defecto de H2 (`sa` y sin contraseña). |
> > | `spring.jpa.hibernate.ddl-auto=update` | Hibernate **crea o actualiza las tablas** al arrancar, sin borrar datos ([Ejemplo 07 del M3](../../03-introduccion-a-jpa/material/ejemplos/07-uso-basico-de-spring-data-jpa.md)). |
> > | `spring.jpa.show-sql=true` | Imprime en la consola **el SQL** que ejecuta Hibernate: sirve para ver el ORM trabajando. |
> >
> > > 💡 **Diferencia con el Módulo 3.** Allí se declaraba `spring.jpa.database-platform=…H2Dialect`. Con Hibernate 6 (Spring Boot 3)
> > > el dialecto se **detecta solo**, así que aquí se omite; dejarlo no rompe nada, pero produce un aviso en el arranque.
>
> </details>
>
> <details>
> <summary><strong>Paso 0.3 · Crear las capas (paquetes)</strong></summary>
>
> > Dentro de `src/main/java/com/academy/` se crean los paquetes de las tres capas:
> >
> > ```text
> > com.academy
> > ├── presentation.controller
> > ├── business.service
> > └── persistence
> >     ├── repository
> >     └── entity
> > ```
> >
> > `presentation.controller` y `business.service` quedan **vacíos**: se llenan en otra guía. Aquí solo se escriben `persistence.entity` y `persistence.repository`.
> >
> > > 💡 Los paquetes se crean desde el IDE (*New → Package*). Un paquete vacío no aparece en Git ni en el `.jar` hasta que tenga una clase.
>
> </details>
>
> <details>
> <summary><strong>Paso 0.4 · Arrancar y comprobar</strong></summary>
>
> > Se ejecuta la aplicación (`./mvnw spring-boot:run`; en Windows, `mvnw.cmd spring-boot:run`):
> >
> > - Aparece `Started AcademyApplication in … seconds` y la aplicación **termina sola** (no hay servidor web).
> > - **No** se crea ninguna tabla (sin `@Entity`, Hibernate no tiene qué mapear).
> >
> > > ⚠️ **Trampa habitual.** `Failed to configure a DataSource: 'url' attribute is not specified` indica que falta la dependencia `h2`
> > > o que `application.properties` no está en `src/main/resources`.
> >
> > ✅ **Punto de control 0:** los paquetes existen, arranca sin errores y no se crea ninguna tabla.
>
> </details>

</details>

---

<details>
<summary><strong>🧱 Paso 1 — Entidades JPA (capa de persistencia)</strong></summary>

> <details>
> <summary><strong>Paso 1.1 · <code>StudentEntity</code> — capa de persistencia</strong></summary>
>
> > **Archivo:** `src/main/java/com/academy/persistence/entity/StudentEntity.java`
> >
> > ```java
> > package com.academy.persistence.entity;
> >
> > import jakarta.persistence.Column;
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import jakarta.persistence.Table;
> >
> > @Entity
> > @Table(name = "student")
> > public class StudentEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String name;
> >
> >     private int age;
> >
> >     @Column(unique = true)
> >     private String email;
> >
> >     protected StudentEntity() {
> >     }
> >
> >     public StudentEntity(String name, int age, String email) {
> >         this.name = name;
> >         this.age = age;
> >         this.email = email;
> >     }
> >
> >     public Long getId() { return id; }
> >     public String getName() { return name; }
> >     public int getAge() { return age; }
> >     public String getEmail() { return email; }
> > }
> > ```
> >
> > | Elemento | Por qué |
> > |---|---|
> > | `@Entity` | Le dice a JPA que la clase se guarda en una tabla. |
> > | `@Table(name = "student")` | **Necesario:** sin él, la tabla se llamaría `student_entity` (el nombre de la clase). |
> > | `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` | `id` es la **clave primaria** y la **genera la base** (autoincremental). Es el equivalente a un contador que nunca repite. |
> > | `@Column(unique = true)` en `email` | La base rechaza correos repetidos (dos estudiantes no pueden tener el mismo `email`). |
> > | Constructor `protected` sin argumentos | JPA crea el objeto vacío y rellena los campos por reflexión (misma idea del JavaBean del M2). |
> > | **Sin `setId`** | El `id` lo decide la base, no el código. |
> >
> > > 🔎 **Concepto (M3):** el nombre de cada columna sale del atributo (`age`, `name`); `maxCapacity` se convertirá en `max_capacity`.
> > > Los tipos se mapean solos: `String` → `varchar`, `int` → `integer`, `Long` → `bigint`.
>
> </details>
>
> <details>
> <summary><strong>Paso 1.2 · <code>CourseEntity</code> y <code>EnrollmentEntity</code></strong></summary>
>
> > Los estudiantes aplican el mismo patrón. La diferencia está en `EnrollmentEntity`: no guarda los datos del estudiante ni del curso, sino
> > **claves foráneas** hacia sus tablas.
> >
> > **Archivo:** `src/main/java/com/academy/persistence/entity/CourseEntity.java`
> >
> > ```java
> > package com.academy.persistence.entity;
> >
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import jakarta.persistence.Table;
> >
> > @Entity
> > @Table(name = "course")
> > public class CourseEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String name;
> >
> >     private double price;
> >
> >     private int maxCapacity;
> >
> >     protected CourseEntity() {
> >     }
> >
> >     public CourseEntity(String name, double price, int maxCapacity) {
> >         this.name = name;
> >         this.price = price;
> >         this.maxCapacity = maxCapacity;
> >     }
> >
> >     public Long getId() { return id; }
> >     public String getName() { return name; }
> >     public double getPrice() { return price; }
> >     public int getMaxCapacity() { return maxCapacity; }
> > }
> > ```
> >
> > **Archivo:** `src/main/java/com/academy/persistence/entity/EnrollmentEntity.java`
> >
> > ```java
> > package com.academy.persistence.entity;
> >
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import jakarta.persistence.JoinColumn;
> > import jakarta.persistence.ManyToOne;
> > import jakarta.persistence.Table;
> >
> > @Entity
> > @Table(name = "enrollment")
> > public class EnrollmentEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     @ManyToOne
> >     @JoinColumn(name = "student_id")
> >     private StudentEntity student;
> >
> >     @ManyToOne
> >     @JoinColumn(name = "course_id")
> >     private CourseEntity course;
> >
> >     protected EnrollmentEntity() {
> >     }
> >
> >     public EnrollmentEntity(StudentEntity student, CourseEntity course) {
> >         this.student = student;
> >         this.course = course;
> >     }
> >
> >     public Long getId() { return id; }
> >     public StudentEntity getStudent() { return student; }
> >     public CourseEntity getCourse() { return course; }
> > }
> > ```
> >
> > - **`@ManyToOne`**: *muchas matrículas* pertenecen a **un** estudiante y a **un** curso ([Ejemplo 03 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/03-oneToMany-manyToOne-y-fetch.md)).
> > - **`@JoinColumn(name = "student_id")`** nombra la **columna de clave foránea**. `EnrollmentEntity` es el **lado dueño**: guarda las dos claves
> >   ([Ejemplo 02 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/02-oneToOne-y-el-lado-dueno.md)).
> > - Las entidades **se relacionan entre sí** (`StudentEntity`, `CourseEntity`): `persistence.entity` es un mundo cerrado.
> > - La relación es **unidireccional**: basta para todas las reglas y simplifica el mapeo.
>
> </details>
>
> <details>
> <summary><strong>Paso 1.3 · Ver las tablas que crea Hibernate</strong></summary>
>
> > Se ejecuta la aplicación. Con `show-sql=true` aparece en la consola (abreviado):
> >
> > ```text
> > Hibernate: create table course (id bigint generated by default as identity, max_capacity integer not null, name varchar(255), price float(53) not null, primary key (id))
> > Hibernate: create table enrollment (id bigint generated by default as identity, course_id bigint, student_id bigint, primary key (id))
> > Hibernate: create table student (id bigint generated by default as identity, age integer not null, email varchar(255), name varchar(255), primary key (id))
> > Hibernate: alter table if exists student add constraint UK… unique (email)
> > Hibernate: alter table if exists enrollment add constraint FK… foreign key (course_id) references course
> > Hibernate: alter table if exists enrollment add constraint FK… foreign key (student_id) references student
> > ```
> >
> > Así queda cada tabla:
> >
> > ```text
> > student                               course                                      enrollment
> > +----+------+-----+-------+           +----+------+-------+--------------+      +----+-----------+------------+
> > | id | name | age | email |           | id | name | price | max_capacity |      | id | course_id | student_id |
> > +----+------+-----+-------+           +----+------+-------+--------------+      +----+-----------+------------+
> >  (email UNIQUE)                                                                       (FK a course) (FK a student)
> > ```
> >
> > > 🗣️ **Preguntas:** ¿quién escribió ese `create table`? *(Hibernate, a partir de las anotaciones.)* ¿Qué tablas tienen una clave foránea? *(Solo `enrollment`: es el lado dueño.)*
> >
> > ✅ **Punto de control 1:** aparecen **tres** `create table`, la restricción `unique` sobre `email` y las dos claves foráneas de `enrollment`.
>
> </details>

</details>

---

<details>
<summary><strong>🗃️ Paso 2 — Interfaces de Spring Data (<code>…JpaRepository</code>)</strong></summary>

> Se crean **tres interfaces**, una por entidad, en `persistence.repository`. `JpaRepository<Entidad, TipoDelId>` ya trae `save`, `findAll`, `findById`, `count`…
> ([Ejemplo 06 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/06-crud-completo.md)). Lo único que se escribe son las consultas propias, como **métodos derivados**: Spring Data lee el nombre y genera la
> consulta ([Ejemplo 07 del M3](../../03-introduccion-a-jpa/material/ejemplos/07-uso-basico-de-spring-data-jpa.md)).
>
> **Archivo:** `src/main/java/com/academy/persistence/repository/StudentJpaRepository.java`
>
> ```java
> package com.academy.persistence.repository;
>
> import com.academy.persistence.entity.StudentEntity;
> import org.springframework.data.jpa.repository.JpaRepository;
>
> public interface StudentJpaRepository extends JpaRepository<StudentEntity, Long> {
>
>     boolean existsByEmailIgnoreCase(String email);
> }
> ```
>
> **Archivo:** `src/main/java/com/academy/persistence/repository/CourseJpaRepository.java`
>
> ```java
> package com.academy.persistence.repository;
>
> import com.academy.persistence.entity.CourseEntity;
> import org.springframework.data.jpa.repository.JpaRepository;
>
> public interface CourseJpaRepository extends JpaRepository<CourseEntity, Long> {
> }
> ```
>
> **Archivo:** `src/main/java/com/academy/persistence/repository/EnrollmentJpaRepository.java`
>
> ```java
> package com.academy.persistence.repository;
>
> import com.academy.persistence.entity.EnrollmentEntity;
> import org.springframework.data.jpa.repository.JpaRepository;
>
> public interface EnrollmentJpaRepository extends JpaRepository<EnrollmentEntity, Long> {
>
>     boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
>
>     long countByCourseId(Long courseId);
> }
> ```
>
> | Método | Consulta generada | Para qué |
> |---|---|---|
> | `existsByEmailIgnoreCase(email)` | `… where upper(email)=upper(?)` | Saber si un correo ya está registrado (sin importar mayúsculas). |
> | `existsByStudentIdAndCourseId(…)` | `… where student_id=? and course_id=?` | Saber si un estudiante ya está matriculado en un curso. |
> | `countByCourseId(courseId)` | `select count(id) … where course_id=?` | Contar las matrículas de un curso. |
> | `save`, `findAll`, `count` *(heredados)* | `insert`, `select`, `select count(*)` | Guardar, listar y contar. |
>
> > 🔎 **Concepto (M3):** el nombre del método **es** la consulta: `existsBy` + `Email` + `IgnoreCase`; `countBy` + `CourseId` recorre la relación
> > (`course.id`); `And` une dos condiciones.
> >
> > 🔎 **Concepto (M2, repaso):** estas interfaces **no** llevan `@Repository` ni tienen clase: Spring Data crea la implementación al arrancar
> > y la registra como **bean singleton**.
> >
> > ⚠️ **Trampa:** si el nombre no coincide con un atributo de la **entidad**, la aplicación **no arranca**:
> > `PropertyReferenceException: No property 'emails' found for type 'StudentEntity'`.
>
> ✅ **Punto de control 2:** el proyecto compila y arranca; `CourseJpaRepository` queda vacía de métodos propios y está bien: solo necesita lo heredado.

</details>

---

<details>
<summary><strong>▶️ Paso 3 — Guardar y consultar desde <code>main</code></strong></summary>

> <details>
> <summary><strong>Paso 3.1 · Escribir los ejemplos en <code>AcademyApplication</code></strong></summary>
>
> > Se reemplaza el contenido de `AcademyApplication.java`. Es la clase del `main`: recibe los repositorios por constructor y ejecuta los ejemplos al arrancar.
> >
> > ```java
> > package com.academy;
> >
> > import com.academy.persistence.entity.CourseEntity;
> > import com.academy.persistence.entity.EnrollmentEntity;
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.persistence.repository.CourseJpaRepository;
> > import com.academy.persistence.repository.EnrollmentJpaRepository;
> > import com.academy.persistence.repository.StudentJpaRepository;
> > import org.springframework.boot.CommandLineRunner;
> > import org.springframework.boot.SpringApplication;
> > import org.springframework.boot.autoconfigure.SpringBootApplication;
> >
> > @SpringBootApplication
> > public class AcademyApplication implements CommandLineRunner {
> >
> >     private final StudentJpaRepository students;
> >     private final CourseJpaRepository courses;
> >     private final EnrollmentJpaRepository enrollments;
> >
> >     public AcademyApplication(StudentJpaRepository students, CourseJpaRepository courses,
> >                               EnrollmentJpaRepository enrollments) {
> >         this.students = students;
> >         this.courses = courses;
> >         this.enrollments = enrollments;
> >     }
> >
> >     public static void main(String[] args) {
> >         SpringApplication.run(AcademyApplication.class, args);
> >     }
> >
> >     @Override
> >     public void run(String... args) {
> >         if (students.count() == 0) {
> >             saveData();
> >         }
> >         queryData();
> >     }
> >
> >     private void saveData() {
> >         StudentEntity ana = students.save(new StudentEntity("Ana Torres", 22, "ana@mail.com"));
> >         StudentEntity luis = students.save(new StudentEntity("Luis Pérez", 25, "luis@mail.com"));
> >         CourseEntity java = courses.save(new CourseEntity("Java from scratch", 150000, 3));
> >         courses.save(new CourseEntity("Spring Boot", 450000, 10));
> >
> >         enrollments.save(new EnrollmentEntity(ana, java));
> >         enrollments.save(new EnrollmentEntity(luis, java));
> >     }
> >
> >     private void queryData() {
> >         System.out.println("=== RESULTS ===");
> >         students.findAll().forEach(s -> System.out.println("Student " + s.getId() + ": " + s.getName()));
> >         courses.findAll().forEach(c -> System.out.println("Course " + c.getId() + ": " + c.getName()));
> >         System.out.println("Does ANA@mail.com exist? " + students.existsByEmailIgnoreCase("ANA@mail.com"));
> >         System.out.println("Is Ana (1) in Java (1)? " + enrollments.existsByStudentIdAndCourseId(1L, 1L));
> >         System.out.println("Enrolled in Java (1): " + enrollments.countByCourseId(1L));
> >         System.out.println("Enrolled in Spring Boot (2): " + enrollments.countByCourseId(2L));
> >     }
> > }
> > ```
> >
> > | Parte | Qué hace |
> > |---|---|
> > | `implements CommandLineRunner` + `run` | Spring llama a `run` **justo después de arrancar**: es el punto de entrada de los ejemplos. |
> > | Constructor con los tres repositorios (`students`, `courses`, `enrollments`) | Spring los **inyecta** (inyección por constructor, M2). |
> > | `students.count() == 0` | Guarda los datos **solo si la base está vacía**. En memoria siempre lo está; en archivo (Paso 4) evita repetir los datos al reiniciar. |
> > | `save(...)` | Inserta la fila y **devuelve la entidad con su `id`** ya generado. Por eso `ana` y `java` sirven después para crear las inscripciones. |
> > | `new EnrollmentEntity(ana, java)` | La inscripción (*enrollment*) se crea con **objetos** `StudentEntity` y `CourseEntity`; JPA guarda de ellos solo la clave foránea. |
> > | `queryData()` | Lee con `findAll` y con los **métodos derivados** del Paso 2. |
>
> </details>
>
> <details>
> <summary><strong>Paso 3.2 · Ejecutar y ver el resultado</strong></summary>
>
> > Se ejecuta la aplicación. Primero aparece el SQL de Hibernate y, al final, lo que imprimen los ejemplos. **Resultado real:**
> >
> > ```text
> > === RESULTS ===
> > Student 1: Ana Torres
> > Student 2: Luis Pérez
> > Course 1: Java from scratch
> > Course 2: Spring Boot
> > Does ANA@mail.com exist? true
> > Is Ana (1) in Java (1)? true
> > Enrolled in Java (1): 2
> > Enrolled in Spring Boot (2): 0
> > ```
> >
> > Así quedaron los datos:
> >
> > ```text
> > student                                       course                                         enrollment
> > +----+------------+-----+--------------+     +----+-------------------+--------+--------------+   +----+-----------+------------+
> > | id | name       | age | email        |     | id | name              | price  | max_capacity |   | id | course_id | student_id |
> > +----+------------+-----+--------------+     +----+-------------------+--------+--------------+   +----+-----------+------------+
> > |  1 | Ana Torres |  22 | ana@mail.com |     |  1 | Java from scratch | 150000 |            3 |   |  1 |         1 |          1 |
> > |  2 | Luis Pérez |  25 | luis@mail.com|     |  2 | Spring Boot       | 450000 |           10 |   |  2 |         1 |          2 |
> > +----+------------+-----+--------------+     +----+-------------------+--------+--------------+   +----+-----------+------------+
> > ```
> >
> > ✅ **Punto de control 3:** los ocho resultados coinciden y aparecen **seis** `insert` (2 estudiantes, 2 cursos, 2 matrículas).
>
> </details>
>
> <details>
> <summary><strong>Paso 3.3 · Leer el SQL que genera cada llamada</strong></summary>
>
> > Con `show-sql=true`, cada llamada al repositorio produce su SQL (real, abreviado):
> >
> > ```text
> > students.count()                        Hibernate: select count(*) from student
> > students.save(...)                       Hibernate: insert into student (age,email,name,id) values (?,?,?,default)
> > enrollments.save(new EnrollmentEntity)   Hibernate: insert into enrollment (course_id,student_id,id) values (?,?,default)
> > students.findAll()                       Hibernate: select … from student
> > existsByEmailIgnoreCase("ANA@…")         Hibernate: select … from student where upper(email)=upper(?) fetch first ? rows only
> > existsByStudentIdAndCourseId(1, 1)       Hibernate: select … from enrollment where student_id=? and course_id=? fetch first ? rows only
> > countByCourseId(1)                       Hibernate: select count(id) from enrollment where course_id=?
> > ```
> >
> > > 🗣️ **Preguntas:** ¿quién escribió el SQL de `existsByEmailIgnoreCase`? *(Nadie: Spring Data lo deduce del nombre del método.)* ¿Por qué el `insert` de `enrollment` lleva `course_id` y `student_id` y no los datos del curso?
> > > *(Porque `@ManyToOne` guarda solo la clave foránea.)*
> > >
> > > 🔎 **Concepto (M3):** esto es el **ORM**: el código solo habla de objetos y Hibernate lo traduce a SQL ([Ejemplo 04 del M3](../../03-introduccion-a-jpa/material/ejemplos/04-que-es-orm.md)).
>
> </details>

</details>

---

<details>
<summary><strong>💾 Paso 4 — Que los datos sobrevivan al reinicio (H2 en archivo)</strong></summary>

> Hasta aquí la base está **en memoria**: es una base de datos de verdad, pero vive dentro de la JVM y muere con ella. Cambiando **solo una línea** se guarda en un archivo del disco.
>
> **1. Cambiar la URL en `application.properties`:**
>
> ```properties
> # Antes:  jdbc:h2:mem:academy;DB_CLOSE_DELAY=-1
> spring.datasource.url=jdbc:h2:file:./data/academy
> ```
>
> **2. Probar la persistencia** (se ejecuta dos veces la misma aplicación):
>
> ```text
> a) Primera ejecución   → aparecen los create table y los 6 insert; el resultado muestra 2 estudiantes y 2 inscritos en Java
> b) Se detiene la aplicación (termina sola)           → queda el archivo  data/academy.mv.db
> c) Segunda ejecución   → solo aparece  select count(*) from student  (no hay insert ni create table)
> d) El resultado es el mismo: Student 1: Ana Torres · Student 2: Luis Pérez · Enrolled in Java (1): 2     ✅ siguen ahí
> ```
>
> **3. Agregar `data/` al `.gitignore`**: el archivo de la base **no** se sube al repositorio.
>
> | Pregunta | Respuesta |
> |---|---|
> | ¿Por qué no se crean de nuevo las tablas al reiniciar? | Existen en el archivo; `ddl-auto=update` solo agrega lo que falte y **no borra datos**. |
> | ¿Por qué no se insertan de nuevo los datos? | Porque `students.count() == 0` es falso: la base ya tiene estudiantes. |
> | ¿Qué pasaría con `ddl-auto=create`? | Borraría y recrearía las tablas en **cada arranque**: se perdería todo ([tabla de valores en el Ejemplo 07 del M3](../../03-introduccion-a-jpa/material/ejemplos/07-uso-basico-de-spring-data-jpa.md)). |
> | ¿Cómo se empieza de cero? | Se borra la carpeta `data/`. |
> | ¿Dónde se crea `data/`? | **Donde se ejecuta Java** (la carpeta del proyecto con `mvnw`). |
>
> > 💡 **Y en producción:** H2 en archivo es una comodidad de desarrollo. Contra MySQL o PostgreSQL cambian el **driver** (dependencia) y las propiedades `spring.datasource.*`; las entidades y los repositorios
> > **no se tocan**. Es uno de los retos finales.

</details>

---

<details>
<summary><strong>🏁 Cierre — preguntas de síntesis</strong></summary>

> 1. ¿Qué escribió el equipo y qué generó Hibernate? *(El equipo escribió las entidades, las interfaces de Spring Data y `main`; Hibernate generó las tablas y todo el SQL.)*
> 2. ¿Quién escribe la implementación de `StudentJpaRepository`? *(Nadie: Spring Data la genera al arrancar.)*
> 3. ¿Por qué `EnrollmentEntity` es el lado dueño? *(Tiene las dos `@JoinColumn`: las claves foráneas viven en la tabla `enrollment`.)*
> 4. ¿Qué devuelve `save` y por qué importa? *(La entidad con el `id` generado; se usa para relacionarla con otras, como `ana` y `java` en la matrícula.)*
> 5. ¿Qué diferencia hay entre una base **en memoria** y una **en archivo**? *(Ambas son bases reales; solo la segunda sobrevive al reinicio.)*

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Solución |
> |---|---|---|
> | `Failed to configure a DataSource: 'url' attribute is not specified` | Falta la dependencia `h2` o `application.properties` | Agregar el driver; revisar que esté en `src/main/resources` |
> | La tabla se llama `estudiante_entity` | Falta `@Table(name = "student")` en la entidad | Agregar `@Table` |
> | `Not a managed type: class …StudentEntity` | Falta `@Entity`, o la clase quedó **fuera** de `com.academy` (el escaneo no la ve) | Anotar la clase y dejarla bajo el paquete de `AcademyApplication` |
> | `No default constructor for entity` | La entidad no tiene constructor **sin argumentos** | Agregar `protected StudentEntity() {}` |
> | `PropertyReferenceException: No property 'x' found for type 'StudentEntity'` | El nombre del método derivado no coincide con un atributo de la **entidad** | Usar el nombre exacto: `existsByEmailIgnoreCase`, `countByCourseId` |
> | `Table "STUDENT" not found` | `ddl-auto=none`, o se apuntó a otra base (`mem` vs `file`) | Usar `update` y revisar la URL |
> | `DataIntegrityViolationException` al guardar un estudiante | El `email` ya existe (restricción `unique`), por ejemplo al reiniciar con H2 en archivo sin la comprobación `count() == 0` | Guardar los datos solo si la base está vacía, o usar otro correo |
> | Los datos desaparecen al reiniciar | La URL es `jdbc:h2:mem:…` | Cambiar a `jdbc:h2:file:./data/academy` |
> | `Database may be already in use` | Dos procesos usan el mismo `data/academy.mv.db` | Cerrar la otra ejecución |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **Restricción en la base:** en `EnrollmentEntity`, agregar `@Table(name = "enrollment", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))`
>    y comprobar que guardar dos veces la misma matrícula lanza `DataIntegrityViolationException`. ¿Para qué sirve si el código ya puede preguntar con `existsByStudentIdAndCourseId`?
>    *(Dos peticiones simultáneas podrían pasar ambas la pregunta; la restricción es la última defensa.)*
> 2. **Filtro en la base:** agregar `List<CourseEntity> findByPriceLessThan(double price)` a `CourseJpaRepository` y mostrar en `main` los cursos con precio menor a 200000.
> 3. **Consulta con JPQL:** reescribir `countByCourseId` con `@Query("SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.course.id = :courseId")`.
> 4. **Otro motor:** reemplazar H2 por **MySQL** o **PostgreSQL** (dependencia del driver + `spring.datasource.*`) y comprobar que **ninguna** entidad ni repositorio cambia.
> 5. **Relación bidireccional:** agregar `List<EnrollmentEntity>` en `CourseEntity` con `@OneToMany(mappedBy = "course")` y explicar qué cambia en la tabla y en el código
>    ([Ejemplo 03 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/03-oneToMany-manyToOne-y-fetch.md) y [Guía 00](00-guia-relaciones-entre-entidades-jpa.md)).

</details>

<details>
<summary><strong>📁 Estructura final esperada</strong></summary>

> ```text
> 📁 academy
> ├── 📄 pom.xml
> ├── 📄 .gitignore                         ← incluye  data/
> ├── 📁 data                               ← lo crea H2 en el Paso 4 (no se versiona)
> │   └── 📄 academy.mv.db
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 presentation
>     │   │   └── 📁 controller    (vacío: otra guía)
>     │   ├── 📁 business
>     │   │   └── 📁 service       (vacío: otra guía)
>     │   └── 📁 persistence
>     │       ├── 📁 repository    StudentJpaRepository · CourseJpaRepository · EnrollmentJpaRepository
>     │       └── 📁 entity        StudentEntity · CourseEntity · EnrollmentEntity
>     └── 📁 resources
>         └── 📄 application.properties
> ```

</details>

<details>
<summary><strong>🏷️ Repaso de las anotaciones (JPA)</strong></summary>

> | Anotación | Dónde se usa | Por qué | Para qué sirve |
> |---|---|---|---|
> | `@Entity` | `StudentEntity`, `CourseEntity`, `EnrollmentEntity` | La clase representa una **tabla**. | Hibernate la mapea y crea la tabla (`ddl-auto`). |
> | `@Table(name = "…")` | Las tres entidades | El nombre de la clase termina en `Entity`; la tabla no. | Fija el nombre de la tabla (`student`, `course`, `enrollment`). |
> | `@Id` | Atributo `id` de cada entidad | Toda entidad necesita **clave primaria**. | Marca la columna que identifica cada fila. |
> | `@GeneratedValue(strategy = IDENTITY)` | Atributo `id` | El valor lo decide la base. | La base lo genera de forma autoincremental. |
> | `@Column(unique = true)` | `StudentEntity.email` | Un correo no puede repetirse. | Crea una restricción `UNIQUE` en la columna. |
> | `@ManyToOne` | `EnrollmentEntity.student`, `.course` | Muchas matrículas apuntan a **un** estudiante o curso. | Declara la relación de muchos a uno. |
> | `@JoinColumn(name = "…")` | Esos mismos dos atributos | Hay que nombrar la **clave foránea**. | Define `student_id` / `course_id`; `EnrollmentEntity` es el **lado dueño**. |
> | `@SpringBootApplication` | `AcademyApplication` | Es la clase que arranca Spring. | Activa el escaneo de entidades y repositorios. |
>
> ```text
> persistence   ──▶  @Entity · @Table · @Id · @GeneratedValue · @Column · @ManyToOne · @JoinColumn     (en persistence.entity)
>                    (las …JpaRepository no llevan anotaciones)
> arranque      ──▶  @SpringBootApplication
> ```
>
> > 🔎 **Lectura clave.** Las anotaciones de JPA viven **solo** en `persistence.entity`. Más ejemplos de cada una: [Anotaciones en JPA](../../03-introduccion-a-jpa/material/ejemplos/08-anotaciones-en-jpa.md).

</details>
