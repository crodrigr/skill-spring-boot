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
> Una **academia de programación** necesita guardar **estudiantes**, **cursos** y **matrículas** en una base de datos. En esta guía se construye **solo la parte que habla con la base**; la lógica del negocio y la API REST quedan para otra guía.
>
> ### 🧩 Una entidad por concepto
>
> | Concepto | Entidad (capa de persistencia) | Tabla |
> |---|---|---|
> | Estudiante | `EstudianteEntity` | `estudiante` |
> | Curso | `CursoEntity` | `curso` |
> | Matrícula | `MatriculaEntity` | `matricula` (FK `estudiante_id` y `curso_id`) |
>
> La entidad describe **cómo se guarda** un concepto en una tabla.
> `MatriculaEntity` es la **entidad intermedia** de la relación muchos a muchos entre `EstudianteEntity` y
> `CursoEntity` ([Ejemplo 04 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/04-manyToMany-y-entidad-intermedia.md)): cada una de sus dos relaciones es un `@ManyToOne`.
>
> ### 🛠️ Requerimientos no funcionales y restricciones
>
> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Java 17 o superior, Spring Boot 3 y Maven. |
> | **RNF-02** | Persistencia con **Spring Data JPA** y **H2**. Primero en memoria; al final, en **archivo**. |
> | **RNF-03** | Las anotaciones de JPA van **solo** en `persistence.entity`. |
> | **RNF-04** | Inyección por constructor. |
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
> | 1 | 10 – 30 | Entidades JPA en `persistence.entity` (`EstudianteEntity` en vivo) | Docente y estudiantes |
> | 2 | 30 – 40 | Interfaces de Spring Data (`…JpaRepository`) | Estudiantes |
> | 3 | 40 – 60 | Guardar y consultar desde `main` y leer el SQL | Todos |
> | 4 | 60 – 75 | Persistencia real (H2 en archivo) y cierre | Todos |
>
> > 💡 **Ritmo.** El Paso 1 con `EstudianteEntity` fija el patrón; con `CursoEntity` y `MatriculaEntity` los estudiantes lo repiten. Si falta tiempo, el
> > Paso 4 se hace como demostración del docente.

</details>

<details>
<summary><strong>🧠 Conceptos de los módulos 03 y 04 que se resaltan</strong></summary>

> | Concepto | Dónde aparece en el taller | Módulo | Ejemplo de apoyo |
> |---|---|:-:|---|
> | JPA y ORM | Las clases se guardan **sin escribir SQL** | 03 | [¿Qué es JPA?](../../03-introduccion-a-jpa/material/ejemplos/01-que-es-jpa.md) · [¿Qué es ORM?](../../03-introduccion-a-jpa/material/ejemplos/04-que-es-orm.md) |
> | Arquitectura de JPA e Hibernate | Hibernate genera las tablas y el SQL (visible con `show-sql`) | 03 | [Arquitectura de JPA](../../03-introduccion-a-jpa/material/ejemplos/02-arquitectura-de-jpa.md) |
> | Relacionamiento entre clases | `MatriculaEntity` apunta a `EstudianteEntity` y a `CursoEntity` | 03 | [Relacionamiento entre clases](../../03-introduccion-a-jpa/material/ejemplos/03-relacionamiento-entre-clases.md) |
> | Spring Data JPA | `JpaRepository` y **métodos derivados** (`existsBy…`, `countBy…`) | 03 | [¿Qué es Spring Data JPA?](../../03-introduccion-a-jpa/material/ejemplos/06-que-es-spring-data-jpa.md) · [Uso básico](../../03-introduccion-a-jpa/material/ejemplos/07-uso-basico-de-spring-data-jpa.md) |
> | Anotaciones de JPA | `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@ManyToOne`, `@JoinColumn` | 03 | [Anotaciones en JPA](../../03-introduccion-a-jpa/material/ejemplos/08-anotaciones-en-jpa.md) |
> | Proyecto y conexión a la base de datos | `pom.xml`, `application.properties`, H2 | 04 | [Creación de un proyecto](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/01-creacion-de-un-proyecto-spring-boot.md) |
> | Lado dueño de la relación y `fetch` | `MatriculaEntity` es dueña de las dos claves foráneas | 04 | [OneToMany/ManyToOne y fetch](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/03-oneToMany-manyToOne-y-fetch.md) |
> | Entidad intermedia | `MatriculaEntity` une estudiante y curso | 04 | [ManyToMany y entidad intermedia](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/04-manyToMany-y-entidad-intermedia.md) |
> | CRUD | `save`, `findAll`, `count` | 04 | [CRUD completo](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/06-crud-completo.md) |
> | Inicialización del servidor | Hibernate crea las tablas al arrancar (`ddl-auto=update`) | 04 | [Inicialización del servidor](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/07-inicializacion-del-servidor.md) |

</details>

<details>
<summary><strong>🏗️ Arquitectura del taller: el árbol de archivos</strong></summary>

> ```text
> 📁 src/main/java/com/academia
> ├── 📄 AcademiaApplication.java                  (➕ la clase main; ejecuta los ejemplos)
> └── 📁 persistence                               ← lo único que se trabaja en esta guía
>     ├── 📁 entity        EstudianteEntity · CursoEntity · MatriculaEntity            (➕ las @Entity)
>     └── 📁 repository    EstudianteJpaRepository · CursoJpaRepository · MatriculaJpaRepository   (➕ Spring Data)
> 📁 src/main/resources
> └── 📄 application.properties                    (➕ conexión a H2 y JPA)
> ```
>
> ```text
> main (CommandLineRunner) ─▶ …JpaRepository ─▶ Hibernate ─▶ H2
>                             Spring Data lo genera   genera el SQL   la base
> ```
>
> > 🔎 **Qué no está.** Las carpetas `negocio` (modelo y servicios), `presentacion` (controladores) y `persistence.mapper` **no se crean aquí**: pertenecen a otra guía.

</details>

<details>
<summary><strong>🗺️ Diagrama de clases de las entidades</strong></summary>

> ```mermaid
> classDiagram
>     direction LR
>
>     class EstudianteEntity {
>         <<Entity>>
>         -Long id
>         -String nombre
>         -int edad
>         -String correo
>     }
>     class CursoEntity {
>         <<Entity>>
>         -Long id
>         -String nombre
>         -double precio
>         -int cupoMaximo
>     }
>     class MatriculaEntity {
>         <<Entity>>
>         -Long id
>         -EstudianteEntity estudiante
>         -CursoEntity curso
>     }
>
>     MatriculaEntity "*" --> "1" EstudianteEntity : @ManyToOne estudiante_id
>     MatriculaEntity "*" --> "1" CursoEntity : @ManyToOne curso_id
> ```
>
> - Cada flecha es una **clave foránea** (`@ManyToOne` + `@JoinColumn`).
> - `MatriculaEntity` es el **lado dueño**: la tabla `matricula` guarda `estudiante_id` y `curso_id`; `EstudianteEntity` y `CursoEntity` **no** conocen sus matrículas (relación unidireccional).

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Preparación</strong></summary>

> <details>
> <summary><strong>Paso 0.1 · Crear el proyecto con las dependencias</strong></summary>
>
> > Se crea un proyecto nuevo en [start.spring.io](https://start.spring.io): **Maven · Java 17 o superior · Group** `com.academia` · **Artifact** `academia` ·
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
> > # Conexión a H2 en memoria
> > spring.datasource.url=jdbc:h2:mem:academia;DB_CLOSE_DELAY=-1
> > spring.datasource.driver-class-name=org.h2.Driver
> > spring.datasource.username=sa
> > spring.datasource.password=
> >
> > # JPA e Hibernate
> > spring.jpa.hibernate.ddl-auto=update
> > spring.jpa.show-sql=true
> > ```
> >
> > | Propiedad | Para qué sirve |
> > |---|---|
> > | `spring.datasource.url` | Dirección de la base. `jdbc:h2:mem:academia` crea una base H2 **en memoria** llamada `academia`; `DB_CLOSE_DELAY=-1` evita que se borre al cerrarse la primera conexión. |
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
> <summary><strong>Paso 0.3 · Arrancar y comprobar</strong></summary>
>
> > Se ejecuta la aplicación (`./mvnw spring-boot:run`; en Windows, `mvnw.cmd spring-boot:run`):
> >
> > - Aparece `Started AcademiaApplication in … seconds` y la aplicación **termina sola** (no hay servidor web).
> > - **No** se crea ninguna tabla (sin `@Entity`, Hibernate no tiene qué mapear).
> >
> > > ⚠️ **Trampa habitual.** `Failed to configure a DataSource: 'url' attribute is not specified` indica que falta la dependencia `h2`
> > > o que `application.properties` no está en `src/main/resources`.
> >
> > ✅ **Punto de control 0:** arranca sin errores y sin tablas.
>
> </details>

</details>

---

<details>
<summary><strong>🧱 Paso 1 — Entidades JPA (capa de persistencia)</strong></summary>

> <details>
> <summary><strong>Paso 1.1 · <code>EstudianteEntity</code> — capa de persistencia</strong></summary>
>
> > **Archivo:** `src/main/java/com/academia/persistence/entity/EstudianteEntity.java`
> >
> > ```java
> > package com.academia.persistence.entity;
> >
> > import jakarta.persistence.Column;
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import jakarta.persistence.Table;
> >
> > @Entity
> > @Table(name = "estudiante")
> > public class EstudianteEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String nombre;
> >
> >     private int edad;
> >
> >     @Column(unique = true)
> >     private String correo;
> >
> >     protected EstudianteEntity() {
> >     }
> >
> >     public EstudianteEntity(String nombre, int edad, String correo) {
> >         this.nombre = nombre;
> >         this.edad = edad;
> >         this.correo = correo;
> >     }
> >
> >     public Long getId() { return id; }
> >     public String getNombre() { return nombre; }
> >     public int getEdad() { return edad; }
> >     public String getCorreo() { return correo; }
> > }
> > ```
> >
> > | Elemento | Por qué |
> > |---|---|
> > | `@Entity` | Le dice a JPA que la clase se guarda en una tabla. |
> > | `@Table(name = "estudiante")` | **Necesario:** sin él, la tabla se llamaría `estudiante_entity` (el nombre de la clase). |
> > | `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` | `id` es la **clave primaria** y la **genera la base** (autoincremental). Es el equivalente a un contador que nunca repite. |
> > | `@Column(unique = true)` en `correo` | La base rechaza correos repetidos. |
> > | Constructor `protected` sin argumentos | JPA crea el objeto vacío y rellena los campos por reflexión (misma idea del JavaBean del M2). |
> > | **Sin `setId`** | El `id` lo decide la base, no el código. |
> >
> > > 🔎 **Concepto (M3):** el nombre de cada columna sale del atributo (`edad`, `nombre`); `cupoMaximo` se convertirá en `cupo_maximo`.
> > > Los tipos se mapean solos: `String` → `varchar`, `int` → `integer`, `Long` → `bigint`.
>
> </details>
>
> <details>
> <summary><strong>Paso 1.2 · <code>CursoEntity</code> y <code>MatriculaEntity</code></strong></summary>
>
> > Los estudiantes aplican el mismo patrón. La diferencia está en `MatriculaEntity`: no guarda los datos del estudiante ni del curso, sino
> > **claves foráneas** hacia sus tablas.
> >
> > **Archivo:** `src/main/java/com/academia/persistence/entity/CursoEntity.java`
> >
> > ```java
> > package com.academia.persistence.entity;
> >
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import jakarta.persistence.Table;
> >
> > @Entity
> > @Table(name = "curso")
> > public class CursoEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String nombre;
> >
> >     private double precio;
> >
> >     private int cupoMaximo;
> >
> >     protected CursoEntity() {
> >     }
> >
> >     public CursoEntity(String nombre, double precio, int cupoMaximo) {
> >         this.nombre = nombre;
> >         this.precio = precio;
> >         this.cupoMaximo = cupoMaximo;
> >     }
> >
> >     public Long getId() { return id; }
> >     public String getNombre() { return nombre; }
> >     public double getPrecio() { return precio; }
> >     public int getCupoMaximo() { return cupoMaximo; }
> > }
> > ```
> >
> > **Archivo:** `src/main/java/com/academia/persistence/entity/MatriculaEntity.java`
> >
> > ```java
> > package com.academia.persistence.entity;
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
> > @Table(name = "matricula")
> > public class MatriculaEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     @ManyToOne
> >     @JoinColumn(name = "estudiante_id")
> >     private EstudianteEntity estudiante;
> >
> >     @ManyToOne
> >     @JoinColumn(name = "curso_id")
> >     private CursoEntity curso;
> >
> >     protected MatriculaEntity() {
> >     }
> >
> >     public MatriculaEntity(EstudianteEntity estudiante, CursoEntity curso) {
> >         this.estudiante = estudiante;
> >         this.curso = curso;
> >     }
> >
> >     public Long getId() { return id; }
> >     public EstudianteEntity getEstudiante() { return estudiante; }
> >     public CursoEntity getCurso() { return curso; }
> > }
> > ```
> >
> > - **`@ManyToOne`**: *muchas matrículas* pertenecen a **un** estudiante y a **un** curso ([Ejemplo 03 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/03-oneToMany-manyToOne-y-fetch.md)).
> > - **`@JoinColumn(name = "estudiante_id")`** nombra la **columna de clave foránea**. `MatriculaEntity` es el **lado dueño**: guarda las dos claves
> >   ([Ejemplo 02 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/02-oneToOne-y-el-lado-dueno.md)).
> > - Las entidades **se relacionan entre sí** (`EstudianteEntity`, `CursoEntity`): `persistence.entity` es un mundo cerrado.
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
> > Hibernate: create table curso (id bigint generated by default as identity, cupo_maximo integer not null, nombre varchar(255), precio float(53) not null, primary key (id))
> > Hibernate: create table estudiante (id bigint generated by default as identity, correo varchar(255), edad integer not null, nombre varchar(255), primary key (id))
> > Hibernate: create table matricula (id bigint generated by default as identity, curso_id bigint, estudiante_id bigint, primary key (id))
> > Hibernate: alter table if exists estudiante add constraint UK… unique (correo)
> > Hibernate: alter table if exists matricula add constraint FK… foreign key (curso_id) references curso
> > Hibernate: alter table if exists matricula add constraint FK… foreign key (estudiante_id) references estudiante
> > ```
> >
> > Así queda cada tabla:
> >
> > ```text
> > estudiante                            curso                                       matricula
> > +----+--------+------+--------+       +----+--------+--------+-------------+      +----+----------+---------------+
> > | id | nombre | edad | correo |       | id | nombre | precio | cupo_maximo |      | id | curso_id | estudiante_id |
> > +----+--------+------+--------+       +----+--------+--------+-------------+      +----+----------+---------------+
> >  (correo UNIQUE)                                                                      (FK a curso)   (FK a estudiante)
> > ```
> >
> > > 🗣️ **Preguntas:** ¿quién escribió ese `create table`? *(Hibernate, a partir de las anotaciones.)* ¿Qué tablas tienen una clave foránea? *(Solo `matricula`: es el lado dueño.)*
> >
> > ✅ **Punto de control 1:** aparecen **tres** `create table`, la restricción `unique` sobre `correo` y las dos claves foráneas de `matricula`.
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
> **Archivo:** `src/main/java/com/academia/persistence/repository/EstudianteJpaRepository.java`
>
> ```java
> package com.academia.persistence.repository;
>
> import com.academia.persistence.entity.EstudianteEntity;
> import org.springframework.data.jpa.repository.JpaRepository;
>
> public interface EstudianteJpaRepository extends JpaRepository<EstudianteEntity, Long> {
>
>     boolean existsByCorreoIgnoreCase(String correo);
> }
> ```
>
> **Archivo:** `src/main/java/com/academia/persistence/repository/CursoJpaRepository.java`
>
> ```java
> package com.academia.persistence.repository;
>
> import com.academia.persistence.entity.CursoEntity;
> import org.springframework.data.jpa.repository.JpaRepository;
>
> public interface CursoJpaRepository extends JpaRepository<CursoEntity, Long> {
> }
> ```
>
> **Archivo:** `src/main/java/com/academia/persistence/repository/MatriculaJpaRepository.java`
>
> ```java
> package com.academia.persistence.repository;
>
> import com.academia.persistence.entity.MatriculaEntity;
> import org.springframework.data.jpa.repository.JpaRepository;
>
> public interface MatriculaJpaRepository extends JpaRepository<MatriculaEntity, Long> {
>
>     boolean existsByEstudianteIdAndCursoId(Long estudianteId, Long cursoId);
>
>     long countByCursoId(Long cursoId);
> }
> ```
>
> | Método | Consulta generada | Para qué |
> |---|---|---|
> | `existsByCorreoIgnoreCase(correo)` | `… where upper(correo)=upper(?)` | Saber si un correo ya está registrado (sin importar mayúsculas). |
> | `existsByEstudianteIdAndCursoId(…)` | `… where estudiante_id=? and curso_id=?` | Saber si un estudiante ya está matriculado en un curso. |
> | `countByCursoId(cursoId)` | `select count(id) … where curso_id=?` | Contar las matrículas de un curso. |
> | `save`, `findAll`, `count` *(heredados)* | `insert`, `select`, `select count(*)` | Guardar, listar y contar. |
>
> > 🔎 **Concepto (M3):** el nombre del método **es** la consulta: `existsBy` + `Correo` + `IgnoreCase`; `countBy` + `CursoId` recorre la relación
> > (`curso.id`); `And` une dos condiciones.
> >
> > 🔎 **Concepto (M2, repaso):** estas interfaces **no** llevan `@Repository` ni tienen clase: Spring Data crea la implementación al arrancar
> > y la registra como **bean singleton**.
> >
> > ⚠️ **Trampa:** si el nombre no coincide con un atributo de la **entidad**, la aplicación **no arranca**:
> > `PropertyReferenceException: No property 'correos' found for type 'EstudianteEntity'`.
>
> ✅ **Punto de control 2:** el proyecto compila y arranca; `CursoJpaRepository` queda vacía de métodos propios y está bien: solo necesita lo heredado.

</details>

---

<details>
<summary><strong>▶️ Paso 3 — Guardar y consultar desde <code>main</code></strong></summary>

> <details>
> <summary><strong>Paso 3.1 · Escribir los ejemplos en <code>AcademiaApplication</code></strong></summary>
>
> > Se reemplaza el contenido de `AcademiaApplication.java`. Es la clase del `main`: recibe los repositorios por constructor y ejecuta los ejemplos al arrancar.
> >
> > ```java
> > package com.academia;
> >
> > import com.academia.persistence.entity.CursoEntity;
> > import com.academia.persistence.entity.EstudianteEntity;
> > import com.academia.persistence.entity.MatriculaEntity;
> > import com.academia.persistence.repository.CursoJpaRepository;
> > import com.academia.persistence.repository.EstudianteJpaRepository;
> > import com.academia.persistence.repository.MatriculaJpaRepository;
> > import org.springframework.boot.CommandLineRunner;
> > import org.springframework.boot.SpringApplication;
> > import org.springframework.boot.autoconfigure.SpringBootApplication;
> >
> > @SpringBootApplication
> > public class AcademiaApplication implements CommandLineRunner {
> >
> >     private final EstudianteJpaRepository estudiantes;
> >     private final CursoJpaRepository cursos;
> >     private final MatriculaJpaRepository matriculas;
> >
> >     public AcademiaApplication(EstudianteJpaRepository estudiantes, CursoJpaRepository cursos,
> >                                MatriculaJpaRepository matriculas) {
> >         this.estudiantes = estudiantes;
> >         this.cursos = cursos;
> >         this.matriculas = matriculas;
> >     }
> >
> >     public static void main(String[] args) {
> >         SpringApplication.run(AcademiaApplication.class, args);
> >     }
> >
> >     @Override
> >     public void run(String... args) {
> >         if (estudiantes.count() == 0) {
> >             guardarDatos();
> >         }
> >         consultarDatos();
> >     }
> >
> >     private void guardarDatos() {
> >         EstudianteEntity ana = estudiantes.save(new EstudianteEntity("Ana Torres", 22, "ana@correo.com"));
> >         EstudianteEntity luis = estudiantes.save(new EstudianteEntity("Luis Pérez", 25, "luis@correo.com"));
> >         CursoEntity java = cursos.save(new CursoEntity("Java desde cero", 150000, 3));
> >         cursos.save(new CursoEntity("Spring Boot", 450000, 10));
> >
> >         matriculas.save(new MatriculaEntity(ana, java));
> >         matriculas.save(new MatriculaEntity(luis, java));
> >     }
> >
> >     private void consultarDatos() {
> >         System.out.println("=== RESULTADOS ===");
> >         estudiantes.findAll().forEach(e -> System.out.println("Estudiante " + e.getId() + ": " + e.getNombre()));
> >         cursos.findAll().forEach(c -> System.out.println("Curso " + c.getId() + ": " + c.getNombre()));
> >         System.out.println("¿Existe ANA@correo.com? " + estudiantes.existsByCorreoIgnoreCase("ANA@correo.com"));
> >         System.out.println("¿Ana (1) está en Java (1)? " + matriculas.existsByEstudianteIdAndCursoId(1L, 1L));
> >         System.out.println("Matriculados en Java (1): " + matriculas.countByCursoId(1L));
> >         System.out.println("Matriculados en Spring Boot (2): " + matriculas.countByCursoId(2L));
> >     }
> > }
> > ```
> >
> > | Parte | Qué hace |
> > |---|---|
> > | `implements CommandLineRunner` + `run` | Spring llama a `run` **justo después de arrancar**: es el punto de entrada de los ejemplos. |
> > | Constructor con los tres repositorios | Spring los **inyecta** (inyección por constructor, M2). |
> > | `estudiantes.count() == 0` | Guarda los datos **solo si la base está vacía**. En memoria siempre lo está; en archivo (Paso 4) evita repetir los datos al reiniciar. |
> > | `save(...)` | Inserta la fila y **devuelve la entidad con su `id`** ya generado. Por eso `ana` y `java` sirven después para crear las matrículas. |
> > | `new MatriculaEntity(ana, java)` | La matrícula se crea con **objetos** `EstudianteEntity` y `CursoEntity`; JPA guarda de ellos solo la clave foránea. |
> > | `consultarDatos()` | Lee con `findAll` y con los **métodos derivados** del Paso 2. |
>
> </details>
>
> <details>
> <summary><strong>Paso 3.2 · Ejecutar y ver el resultado</strong></summary>
>
> > Se ejecuta la aplicación. Primero aparece el SQL de Hibernate y, al final, lo que imprimen los ejemplos. **Resultado real:**
> >
> > ```text
> > === RESULTADOS ===
> > Estudiante 1: Ana Torres
> > Estudiante 2: Luis Pérez
> > Curso 1: Java desde cero
> > Curso 2: Spring Boot
> > ¿Existe ANA@correo.com? true
> > ¿Ana (1) está en Java (1)? true
> > Matriculados en Java (1): 2
> > Matriculados en Spring Boot (2): 0
> > ```
> >
> > Así quedaron los datos:
> >
> > ```text
> > estudiante                                        curso                                              matricula
> > +----+------------+------+-----------------+     +----+-----------------+--------+-------------+   +----+----------+---------------+
> > | id | nombre     | edad | correo          |     | id | nombre          | precio | cupo_maximo |   | id | curso_id | estudiante_id |
> > +----+------------+------+-----------------+     +----+-----------------+--------+-------------+   +----+----------+---------------+
> > |  1 | Ana Torres |   22 | ana@correo.com  |     |  1 | Java desde cero | 150000 |           3 |   |  1 |        1 |             1 |
> > |  2 | Luis Pérez |   25 | luis@correo.com |     |  2 | Spring Boot     | 450000 |          10 |   |  2 |        1 |             2 |
> > +----+------------+------+-----------------+     +----+-----------------+--------+-------------+   +----+----------+---------------+
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
> > estudiantes.count()                      Hibernate: select count(*) from estudiante
> > estudiantes.save(...)                    Hibernate: insert into estudiante (correo,edad,nombre,id) values (?,?,?,default)
> > matriculas.save(new MatriculaEntity)     Hibernate: insert into matricula (curso_id,estudiante_id,id) values (?,?,default)
> > estudiantes.findAll()                    Hibernate: select … from estudiante
> > existsByCorreoIgnoreCase("ANA@…")        Hibernate: select … from estudiante where upper(correo)=upper(?) fetch first ? rows only
> > existsByEstudianteIdAndCursoId(1, 1)     Hibernate: select … from matricula where estudiante_id=? and curso_id=? fetch first ? rows only
> > countByCursoId(1)                        Hibernate: select count(id) from matricula where curso_id=?
> > ```
> >
> > > 🗣️ **Preguntas:** ¿quién escribió el SQL de `existsByCorreoIgnoreCase`? *(Nadie: Spring Data lo deduce del nombre del método.)* ¿Por qué el `insert` de la matrícula lleva `curso_id` y `estudiante_id` y no los datos del curso?
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
> # Antes:  jdbc:h2:mem:academia;DB_CLOSE_DELAY=-1
> spring.datasource.url=jdbc:h2:file:./data/academia
> ```
>
> **2. Probar la persistencia** (se ejecuta dos veces la misma aplicación):
>
> ```text
> a) Primera ejecución   → aparecen los create table y los 6 insert; el resultado muestra 2 estudiantes y 2 matriculados en Java
> b) Se detiene la aplicación (termina sola)           → queda el archivo  data/academia.mv.db
> c) Segunda ejecución   → solo aparece  select count(*) from estudiante  (no hay insert ni create table)
> d) El resultado es el mismo: Estudiante 1: Ana Torres · Estudiante 2: Luis Pérez · Matriculados en Java (1): 2     ✅ siguen ahí
> ```
>
> **3. Agregar `data/` al `.gitignore`**: el archivo de la base **no** se sube al repositorio.
>
> | Pregunta | Respuesta |
> |---|---|
> | ¿Por qué no se crean de nuevo las tablas al reiniciar? | Existen en el archivo; `ddl-auto=update` solo agrega lo que falte y **no borra datos**. |
> | ¿Por qué no se insertan de nuevo los datos? | Porque `estudiantes.count() == 0` es falso: la base ya tiene estudiantes. |
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
> 2. ¿Quién escribe la implementación de `EstudianteJpaRepository`? *(Nadie: Spring Data la genera al arrancar.)*
> 3. ¿Por qué `MatriculaEntity` es el lado dueño? *(Tiene las dos `@JoinColumn`: las claves foráneas viven en la tabla `matricula`.)*
> 4. ¿Qué devuelve `save` y por qué importa? *(La entidad con el `id` generado; se usa para relacionarla con otras, como `ana` y `java` en la matrícula.)*
> 5. ¿Qué diferencia hay entre una base **en memoria** y una **en archivo**? *(Ambas son bases reales; solo la segunda sobrevive al reinicio.)*

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Solución |
> |---|---|---|
> | `Failed to configure a DataSource: 'url' attribute is not specified` | Falta la dependencia `h2` o `application.properties` | Agregar el driver; revisar que esté en `src/main/resources` |
> | La tabla se llama `estudiante_entity` | Falta `@Table(name = "estudiante")` en la entidad | Agregar `@Table` |
> | `Not a managed type: class …EstudianteEntity` | Falta `@Entity`, o la clase quedó **fuera** de `com.academia` (el escaneo no la ve) | Anotar la clase y dejarla bajo el paquete de `AcademiaApplication` |
> | `No default constructor for entity` | La entidad no tiene constructor **sin argumentos** | Agregar `protected EstudianteEntity() {}` |
> | `PropertyReferenceException: No property 'x' found for type 'EstudianteEntity'` | El nombre del método derivado no coincide con un atributo de la **entidad** | Usar el nombre exacto: `existsByCorreoIgnoreCase`, `countByCursoId` |
> | `Table "ESTUDIANTE" not found` | `ddl-auto=none`, o se apuntó a otra base (`mem` vs `file`) | Usar `update` y revisar la URL |
> | `DataIntegrityViolationException` al guardar un estudiante | El `correo` ya existe (restricción `unique`), por ejemplo al reiniciar con H2 en archivo sin la comprobación `count() == 0` | Guardar los datos solo si la base está vacía, o usar otro correo |
> | Los datos desaparecen al reiniciar | La URL es `jdbc:h2:mem:…` | Cambiar a `jdbc:h2:file:./data/academia` |
> | `Database may be already in use` | Dos procesos usan el mismo `data/academia.mv.db` | Cerrar la otra ejecución |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **Restricción en la base:** en `MatriculaEntity`, agregar `@Table(name = "matricula", uniqueConstraints = @UniqueConstraint(columnNames = {"estudiante_id", "curso_id"}))`
>    y comprobar que guardar dos veces la misma matrícula lanza `DataIntegrityViolationException`. ¿Para qué sirve si el código ya puede preguntar con `existsByEstudianteIdAndCursoId`?
>    *(Dos peticiones simultáneas podrían pasar ambas la pregunta; la restricción es la última defensa.)*
> 2. **Filtro en la base:** agregar `List<CursoEntity> findByPrecioLessThan(double precio)` a `CursoJpaRepository` y mostrar en `main` los cursos con precio menor a 200000.
> 3. **Consulta con JPQL:** reescribir `countByCursoId` con `@Query("SELECT COUNT(m) FROM MatriculaEntity m WHERE m.curso.id = :cursoId")`.
> 4. **Otro motor:** reemplazar H2 por **MySQL** o **PostgreSQL** (dependencia del driver + `spring.datasource.*`) y comprobar que **ninguna** entidad ni repositorio cambia.
> 5. **Relación bidireccional:** agregar `List<MatriculaEntity>` en `CursoEntity` con `@OneToMany(mappedBy = "curso")` y explicar qué cambia en la tabla y en el código
>    ([Ejemplo 03 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/03-oneToMany-manyToOne-y-fetch.md) y [Guía 00](00-guia-relaciones-entre-entidades-jpa.md)).

</details>

<details>
<summary><strong>📁 Estructura final esperada</strong></summary>

> ```text
> 📁 academia
> ├── 📄 pom.xml
> ├── 📄 .gitignore                         ← incluye  data/
> ├── 📁 data                               ← lo crea H2 en el Paso 4 (no se versiona)
> │   └── 📄 academia.mv.db
> └── 📁 src/main
>     ├── 📁 java/com/academia
>     │   ├── 📄 AcademiaApplication.java
>     │   └── 📁 persistence
>     │       ├── 📁 entity        EstudianteEntity · CursoEntity · MatriculaEntity
>     │       └── 📁 repository    EstudianteJpaRepository · CursoJpaRepository · MatriculaJpaRepository
>     └── 📁 resources
>         └── 📄 application.properties
> ```

</details>

<details>
<summary><strong>🏷️ Repaso de las anotaciones (JPA)</strong></summary>

> | Anotación | Dónde se usa | Por qué | Para qué sirve |
> |---|---|---|---|
> | `@Entity` | `EstudianteEntity`, `CursoEntity`, `MatriculaEntity` | La clase representa una **tabla**. | Hibernate la mapea y crea la tabla (`ddl-auto`). |
> | `@Table(name = "…")` | Las tres entidades | El nombre de la clase termina en `Entity`; la tabla no. | Fija el nombre de la tabla (`estudiante`, `curso`, `matricula`). |
> | `@Id` | Atributo `id` de cada entidad | Toda entidad necesita **clave primaria**. | Marca la columna que identifica cada fila. |
> | `@GeneratedValue(strategy = IDENTITY)` | Atributo `id` | El valor lo decide la base. | La base lo genera de forma autoincremental. |
> | `@Column(unique = true)` | `EstudianteEntity.correo` | Un correo no puede repetirse. | Crea una restricción `UNIQUE` en la columna. |
> | `@ManyToOne` | `MatriculaEntity.estudiante`, `.curso` | Muchas matrículas apuntan a **un** estudiante o curso. | Declara la relación de muchos a uno. |
> | `@JoinColumn(name = "…")` | Esos mismos dos atributos | Hay que nombrar la **clave foránea**. | Define `estudiante_id` / `curso_id`; `MatriculaEntity` es el **lado dueño**. |
> | `@SpringBootApplication` | `AcademiaApplication` | Es la clase que arranca Spring. | Activa el escaneo de entidades y repositorios. |
>
> ```text
> persistence   ──▶  @Entity · @Table · @Id · @GeneratedValue · @Column · @ManyToOne · @JoinColumn     (en persistence.entity)
>                    (las …JpaRepository no llevan anotaciones)
> arranque      ──▶  @SpringBootApplication
> ```
>
> > 🔎 **Lectura clave.** Las anotaciones de JPA viven **solo** en `persistence.entity`. Más ejemplos de cada una: [Anotaciones en JPA](../../03-introduccion-a-jpa/material/ejemplos/08-anotaciones-en-jpa.md).

</details>
