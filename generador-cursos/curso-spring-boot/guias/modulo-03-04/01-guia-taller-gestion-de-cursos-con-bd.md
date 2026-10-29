# 🗄️ Guía paso a paso — Taller «Gestión de cursos» con base de datos (JPA)

> 👩‍🏫 **Material docente.** Guía para resolver **junto con los estudiantes**, en vivo, la
> versión con base de datos del taller «Gestión de cursos». Parte del taller ya terminado en la
> [guía de los módulos 01 y 02](../modulo-01-02/01-guia-taller-gestion-de-cursos.md) y aplica lo visto
> en el [Módulo 03](../../03-introduccion-a-jpa/) y el [Módulo 04](../../04-gestion-de-bases-de-datos-mediante-jpa/).
> Contiene código completo: no se entrega a los estudiantes antes de resolver el taller.
>
> ✅ **Verificada.** El código de esta guía se compiló y se ejecutó con Java 21+, Spring Boot 3.3 y
> H2: los escenarios, las respuestas y el SQL que se muestran son salidas reales.

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Migrar la API REST de **estudiantes, cursos y matrículas** de listas en memoria a una
> **base de datos relacional** con **Spring Data JPA**, manteniendo las **tres capas** y **las mismas
> reglas de negocio**.
>
> Al terminar, el estudiante:
>
> - Declara entidades JPA (`@Entity`, `@Id`, `@GeneratedValue`, `@Column`) y la relación de
>   `Matricula` con `Estudiante` y `Curso` (`@ManyToOne`) (M3 y M4).
> - Configura la conexión a **H2** en `application.properties` y lee el SQL que genera Hibernate (M3 y M4).
> - Reemplaza los repositorios «EnMemoria» por interfaces `JpaRepository` con **métodos derivados**
>   (`existsBy…`, `countBy…`, `findBy…LessThan`) (M3).
> - Comprueba qué capas cambiaron y cuáles **no** (controladores y reglas) al cambiar el almacenamiento.
> - Verifica que los datos **sobreviven al reinicio** usando H2 en archivo.

</details>

<details>
<summary><strong>📝 Introducción: el problema y qué cambia</strong></summary>

> > 🗣️ **Para abrir la sesión.** En la versión anterior, reiniciar la aplicación borraba todo. Se
> > retoma esa frase como punto de partida: *«hoy los datos dejan de vivir en una `List` y pasan a
> > una base de datos»*. Antes de escribir código se revisa la tabla «Qué cambia y qué no».
>
> ### 🏫 El problema (el mismo)
>
> Una **academia de programación** necesita una API REST para administrar **estudiantes**,
> **cursos** y **matrículas**: registrar estudiantes, crear cursos, matricular estudiantes y consultar
> qué tan llenos están los cursos. Los requerimientos funcionales (RF-01 a RF-08) y las reglas de
> negocio (RN-01 a RN-11) **son los de la guía anterior**; no se agregan ni se quitan.
>
> ### 🧩 Entidades y tablas
>
> | Entidad (clase) | Tabla | Atributos | Observación |
> |---|---|---|---|
> | **Estudiante** | `estudiante` | `id`, `nombre`, `edad`, `correo` | `correo` es **único** (`@Column(unique = true)`). |
> | **Curso** | `curso` | `id`, `nombre`, `precio`, `cupoMaximo` | La columna se llama `cupo_maximo`. |
> | **Matrícula** | `matricula` | `id`, `estudiante`, `curso` | Dos **claves foráneas**: `estudiante_id` y `curso_id`. |
>
> `Matricula` es la **entidad intermedia** de la relación muchos a muchos entre `Estudiante` y `Curso`
> ([Ejemplo 04 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/04-manyToMany-y-entidad-intermedia.md)): cada una de sus dos relaciones es un `@ManyToOne`.
>
> ```mermaid
> erDiagram
>     ESTUDIANTE ||--o{ MATRICULA : tiene
>     CURSO ||--o{ MATRICULA : recibe
>     ESTUDIANTE {
>         bigint id PK
>         varchar nombre
>         int edad
>         varchar correo UK
>     }
>     CURSO {
>         bigint id PK
>         varchar nombre
>         float precio
>         int cupo_maximo
>     }
>     MATRICULA {
>         bigint id PK
>         bigint estudiante_id FK
>         bigint curso_id FK
>     }
> ```
>
> ### 🔄 Qué cambia y qué no
>
> | Pieza | ¿Cambia? | Qué cambia |
> |---|:-:|---|
> | `presentacion.controlador` (3 controladores y `SolicitudMatricula`) | ❌ No | Nada: no conocen cómo se guardan los datos. |
> | `negocio.model.Ocupacion` | ❌ No | Sigue siendo un `record` que no se guarda. |
> | `negocio.model` (`Estudiante`, `Curso`, `Matricula`) | ✅ Sí | Pasan a ser **entidades JPA**: anotaciones y constructor `protected`. Se quita `setId`. |
> | `persistence.repository` | ✅ Sí | Las 3 clases «EnMemoria» **desaparecen**; las 3 interfaces extienden `JpaRepository` y Spring Data genera la implementación. |
> | `negocio.service` | ⚠️ Poco | Las **reglas son las mismas**; solo cambian los nombres de las llamadas al repositorio (`guardar` → `save`, `listar` → `findAll`…) y `listarEconomicos` deja el ciclo para usar una consulta. |
> | `pom.xml` y `application.properties` | ✅ Sí | Nueva dependencia (`spring-boot-starter-data-jpa`), driver H2 y la conexión. |
>
> > 🔎 **Lectura clave.** En la guía anterior se afirmó que, al pasar a una base de datos, *«solo cambia
> > la persistencia»*. Es casi cierto: como ahora usamos la interfaz de Spring Data (`JpaRepository`) en
> > lugar de nuestro propio contrato, **los servicios cambian los nombres de las llamadas**, pero no una sola
> > regla. Si se quisiera conservar los nombres antiguos (`guardar`, `listar`…) y no tocar los servicios,
> > se necesita una clase adaptadora: es uno de los retos finales.
>
> ### 🛠️ Requerimientos no funcionales y restricciones
>
> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Java 21 o superior, Spring Boot 3 y Maven. |
> | **RNF-02** | Persistencia con **Spring Data JPA** y **H2**. Primero en memoria; al final, en **archivo** para que los datos sobrevivan al reinicio. |
> | **RNF-03** | Se mantienen las **tres capas** y la regla de dependencia presentación → negocio → persistencia. |
> | **RNF-04** | Inyección por constructor. Los servicios dependen de **interfaces** de repositorio. |
> | **RNF-05** | Los errores de negocio siguen siendo excepciones estándar de Java; sin manejador, Spring responde `500` (se resuelve en el Módulo 7). |
> | **RNF-06** | La API se prueba con Postman o `curl`, con evidencia de cada escenario. |
> | **RNF-07** | Duración: 1 h 30 min. |
>
> > 💡 **Alcance.** Quedan fuera: relaciones bidireccionales, `cascade`, migraciones (Flyway), otros
> > motores (MySQL/PostgreSQL), DTO y manejo de errores con `404`/`400`/`409`.

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>

> | Paso | Minutos | Actividad | Quién escribe |
> |---|---:|---|---|
> | 0 | 0 – 10 | Preparación: dependencias, `application.properties` y arranque | Docente |
> | 1 | 10 – 30 | Entidades JPA (`Estudiante` en vivo; `Curso` y `Matricula` los estudiantes) | Docente y estudiantes |
> | 2 | 30 – 45 | Repositorios: de la clase «EnMemoria» a `JpaRepository` | Estudiantes (docente guía) |
> | 3 | 45 – 60 | Ajustar los servicios (mismas reglas) | Estudiantes |
> | 4 | 60 – 80 | Pruebas con `curl`/Postman, SQL en consola y consola de H2 | Todos |
> | 5 | 80 – 90 | Persistencia real (H2 en archivo) y cierre | Todos |
>
> > 💡 **Ritmo.** El Paso 1 con `Estudiante` fija el patrón de anotaciones; los pasos 2 y 3 son
> > mecánicos y los resuelven los estudiantes con la tabla «antes → ahora» a la vista. Si falta
> > tiempo, el Paso 5 se hace como demostración del docente.

</details>

<details>
<summary><strong>🧠 Conceptos de los módulos 03 y 04 que se resaltan</strong></summary>

> | Concepto | Dónde aparece en el taller | Módulo | Ejemplo de apoyo |
> |---|---|:-:|---|
> | JPA y ORM | Las clases del modelo se guardan **sin escribir SQL** | 03 | [¿Qué es JPA?](../../03-introduccion-a-jpa/material/ejemplos/01-que-es-jpa.md) · [¿Qué es ORM?](../../03-introduccion-a-jpa/material/ejemplos/04-que-es-orm.md) |
> | Arquitectura de JPA e Hibernate | Hibernate genera las tablas y el SQL (visible con `show-sql`) | 03 | [Arquitectura de JPA](../../03-introduccion-a-jpa/material/ejemplos/02-arquitectura-de-jpa.md) · [Hibernate y JPA](../../03-introduccion-a-jpa/material/ejemplos/05-hibernate-y-su-relacion-con-jpa.md) |
> | Relacionamiento entre clases | `Matricula` apunta a `Estudiante` y a `Curso` | 03 | [Relacionamiento entre clases](../../03-introduccion-a-jpa/material/ejemplos/03-relacionamiento-entre-clases.md) |
> | Spring Data JPA | `JpaRepository` y **métodos derivados** (`existsBy…`, `countBy…`) | 03 | [¿Qué es Spring Data JPA?](../../03-introduccion-a-jpa/material/ejemplos/06-que-es-spring-data-jpa.md) · [Uso básico](../../03-introduccion-a-jpa/material/ejemplos/07-uso-basico-de-spring-data-jpa.md) |
> | Anotaciones de JPA | `@Entity`, `@Id`, `@GeneratedValue`, `@Column`, `@ManyToOne`, `@JoinColumn` | 03 | [Anotaciones en JPA](../../03-introduccion-a-jpa/material/ejemplos/08-anotaciones-en-jpa.md) |
> | Proyecto y conexión a la base de datos | `pom.xml`, `application.properties`, H2 | 04 | [Creación de un proyecto](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/01-creacion-de-un-proyecto-spring-boot.md) |
> | Lado dueño de la relación y `fetch` | `Matricula` es dueña de las dos claves foráneas | 04 | [OneToMany/ManyToOne y fetch](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/03-oneToMany-manyToOne-y-fetch.md) |
> | Entidad intermedia | `Matricula` une `Estudiante` y `Curso` | 04 | [ManyToMany y entidad intermedia](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/04-manyToMany-y-entidad-intermedia.md) |
> | CRUD | `save`, `findAll`, `findById` | 04 | [CRUD completo](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/06-crud-completo.md) |
> | Inicialización del servidor | Hibernate crea las tablas al arrancar (`ddl-auto=update`) | 04 | [Inicialización del servidor](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/07-inicializacion-del-servidor.md) |
> | Interfaces e inyección (repaso M2) | Spring crea **el bean del repositorio** a partir de una interfaz | 02 | [Resolución de una dependencia](../../02-manejo-de-dependencias-y-java-beans/material/ejemplos/04-implementacion-y-resolucion-de-una-dependencia-general.md) |

</details>

<details>
<summary><strong>🏗️ Arquitectura del taller: el árbol de infraestructura</strong></summary>

> ```text
> 📁 src/main/java/com/academia
> ├── 📄 AcademiaApplication.java                  (sin cambios)
> ├── 📁 presentacion                              ← 1 · sin cambios
> │   └── 📁 controlador   ControladorEstudiantes · ControladorCursos · ControladorMatriculas · SolicitudMatricula
> ├── 📁 negocio                                   ← 2 · las mismas reglas
> │   ├── 📁 model         Estudiante · Curso · Matricula  (ahora @Entity)  ·  Ocupacion (record)
> │   └── 📁 service       ServicioEstudiantes · ServicioCursos · ServicioMatriculas  (cambian las llamadas)
> └── 📁 persistence                               ← 3 · aquí está el cambio grande
>     └── 📁 repository    RepositorioEstudiantes · RepositorioCursos · RepositorioMatriculas  (extienden JpaRepository)
> 📁 src/main/resources
> └── 📄 application.properties                    (NUEVO: conexión a H2 y JPA)
> ```
>
> ```text
> Antes:  HTTP ─▶ Controlador ─▶ Servicio ─▶ Repositorio (interfaz propia) ◀─ RepositorioEnMemoria (List)
>
> Ahora:  HTTP ─▶ Controlador ─▶ Servicio ─▶ Repositorio (JpaRepository) ◀─ implementación que Spring Data genera
>                                                                               │
>                                                                               ▼  Hibernate ─▶ JDBC ─▶ H2
> ```
>
> > 🗣️ **Pregunta para el grupo:** ¿quién escribe ahora la clase que implementa el repositorio?
> > *(Nadie: Spring Data la crea en tiempo de ejecución a partir de la interfaz y la registra como bean.)*

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Preparación</strong></summary>

> <details>
> <summary><strong>Paso 0.1 · Partir del proyecto y agregar las dependencias</strong></summary>
>
> > Hay dos caminos equivalentes; el docente elige uno:
> >
> > | Camino | Cómo |
> > |---|---|
> > | **A. Proyecto nuevo** (recomendado) | En [start.spring.io](https://start.spring.io): Maven · Java 21 · Spring Boot 3.x · Group `com.academia` · Artifact `academia` · dependencias **Spring Web**, **Spring Data JPA** y **H2 Database**. |
> > | **B. Continuar el taller anterior** | Copiar el proyecto `academia` terminado (puntos de control 0 a 4) y agregar a mano las dos dependencias nuevas. |
> >
> > El `pom.xml` queda con tres dependencias funcionales:
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
> >     <groupId>com.h2database</groupId>
> >     <artifactId>h2</artifactId>
> >     <scope>runtime</scope>
> > </dependency>
> > ```
> >
> > > 🔎 **Concepto (M3 y M4):** `spring-boot-starter-data-jpa` trae JPA, **Hibernate** y Spring Data;
> > > `h2` es el **driver** de la base de datos, y `runtime` indica que solo hace falta al ejecutar.
> > > Si se copió el proyecto anterior (camino B), los paquetes `presentacion`, `negocio` y `persistence`
> > > ya existen; los pasos 1 a 3 modifican lo que allí haya.
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
> >
> > # Consola web de H2 (opcional)
> > spring.h2.console.enabled=true
> > ```
> >
> > | Propiedad | Para qué sirve |
> > |---|---|
> > | `spring.datasource.url` | Dirección de la base. `jdbc:h2:mem:academia` crea una base H2 **en memoria** llamada `academia`; `DB_CLOSE_DELAY=-1` evita que se borre al cerrarse la primera conexión. |
> > | `spring.datasource.driver-class-name`, `username`, `password` | Driver y credenciales por defecto de H2 (`sa` y sin contraseña). |
> > | `spring.jpa.hibernate.ddl-auto=update` | Hibernate **crea o actualiza las tablas** al arrancar, sin borrar datos existentes ([Ejemplo 07 del M3](../../03-introduccion-a-jpa/material/ejemplos/07-uso-basico-de-spring-data-jpa.md)). |
> > | `spring.jpa.show-sql=true` | Imprime en la consola **el SQL** que Hibernate ejecuta: sirve para ver el ORM trabajando. |
> > | `spring.h2.console.enabled=true` | Habilita la consola web de H2 en `/h2-console` para mirar las tablas. |
> >
> > > 💡 **Diferencia con el Módulo 3.** Allí se declaraba `spring.jpa.database-platform=…H2Dialect`.
> > > Con Hibernate 6 (Spring Boot 3) el dialecto se **detecta solo**, por lo que aquí se omite; dejarlo
> > > no rompe nada, pero produce un aviso en el arranque.
>
> </details>
>
> <details>
> <summary><strong>Paso 0.3 · Arrancar «vacío»</strong></summary>
>
> > Todavía no hay entidades. Se ejecuta la aplicación (`./mvnw spring-boot:run`, o `mvnw.cmd spring-boot:run`
> > en Windows) y se comprueba:
> >
> > - Aparece `Started AcademiaApplication in … seconds`.
> > - En el registro hay líneas de **HikariPool** y de **Hibernate** (la conexión a H2 quedó abierta).
> > - **No** se crean tablas: sin `@Entity`, Hibernate no tiene nada que mapear.
> >
> > > ⚠️ **Trampa habitual.** Si aparece `Failed to configure a DataSource: 'url' attribute is not
> > > specified`, falta la dependencia `h2` o el archivo `application.properties` no está en
> > > `src/main/resources`.
> >
> > ✅ **Punto de control 0:** arranca sin errores y sin tablas. Todavía no hay datos.
>
> </details>

</details>

---

<details>
<summary><strong>🧱 Paso 1 — Entidades JPA (capa de negocio)</strong></summary>

> <details>
> <summary><strong>Paso 1.1 · <code>Estudiante</code> como entidad — capa de negocio</strong></summary>
>
> > Se parte de la clase de la guía anterior y se le agregan anotaciones. **En vivo, comparando.**
> >
> > **Archivo:** `src/main/java/com/academia/negocio/model/Estudiante.java`
> >
> > ```java
> > package com.academia.negocio.model;
> >
> > import jakarta.persistence.Column;
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> >
> > @Entity
> > public class Estudiante {
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
> >     protected Estudiante() {
> >     }
> >
> >     public Estudiante(String nombre, int edad, String correo) {
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
> > | Cambio respecto a la versión en memoria | Por qué |
> > |---|---|
> > | `@Entity` | Le dice a JPA que la clase se guarda en una tabla (`estudiante`). |
> > | `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` | `id` es la **clave primaria** y la **genera la base de datos** (autoincremental). Reemplaza al contador `siguienteId` del repositorio (**RN-01**). |
> > | `@Column(unique = true)` en `correo` | La base **también** rechaza correos repetidos (red de seguridad de **RN-02**). |
> > | Constructor `protected Estudiante()` | JPA crea el objeto con el constructor **sin argumentos** y luego rellena los campos por reflexión (misma idea del JavaBean del M2). |
> > | Se **elimina** `setId` | El `id` lo decide la base, no el código. |
> >
> > > 🔎 **Concepto (M3):** `@Entity` es lo que hace que Hibernate cree la tabla. El nombre de la
> > > columna `edad` o `nombre` sale del nombre del atributo; `cupoMaximo` se convertirá en `cupo_maximo`.
> > > Todos los tipos se mapean solos: `String` → `varchar`, `int` → `integer`, `Long` → `bigint`.
>
> </details>
>
> <details>
> <summary><strong>Paso 1.2 · <code>Curso</code> y <code>Matricula</code></strong></summary>
>
> > Los estudiantes aplican el mismo patrón. La diferencia está en `Matricula`: en la versión anterior
> > guardaba **objetos** `Estudiante` y `Curso`, y ahora esos objetos se vuelven **claves foráneas**.
> >
> > **Archivo:** `src/main/java/com/academia/negocio/model/Curso.java`
> >
> > ```java
> > package com.academia.negocio.model;
> >
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> >
> > @Entity
> > public class Curso {
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
> >     protected Curso() {
> >     }
> >
> >     public Curso(String nombre, double precio, int cupoMaximo) {
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
> > **Archivo:** `src/main/java/com/academia/negocio/model/Matricula.java`
> >
> > ```java
> > package com.academia.negocio.model;
> >
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import jakarta.persistence.JoinColumn;
> > import jakarta.persistence.ManyToOne;
> >
> > @Entity
> > public class Matricula {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     @ManyToOne
> >     @JoinColumn(name = "estudiante_id")
> >     private Estudiante estudiante;
> >
> >     @ManyToOne
> >     @JoinColumn(name = "curso_id")
> >     private Curso curso;
> >
> >     protected Matricula() {
> >     }
> >
> >     public Matricula(Estudiante estudiante, Curso curso) {
> >         this.estudiante = estudiante;
> >         this.curso = curso;
> >     }
> >
> >     public Long getId() { return id; }
> >     public Estudiante getEstudiante() { return estudiante; }
> >     public Curso getCurso() { return curso; }
> > }
> > ```
> >
> > Puntos a resaltar:
> >
> > - **`@ManyToOne`**: *muchas matrículas* pertenecen a **un** estudiante (y a **un** curso). Es el mismo
> >   modelo de objetos que ya se tenía; solo se le agregan las anotaciones ([Ejemplo 03 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/03-oneToMany-manyToOne-y-fetch.md)).
> > - **`@JoinColumn(name = "estudiante_id")`** nombra la **columna de clave foránea** en la tabla `matricula`.
> >   `Matricula` es el **lado dueño**: es quien guarda las dos claves foráneas ([Ejemplo 02 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/02-oneToOne-y-el-lado-dueno.md)).
> > - La relación es **unidireccional** (`Estudiante` y `Curso` no conocen sus matrículas): basta para todas las
> >   reglas del taller y evita ciclos al convertir a JSON.
> > - `Ocupacion` **no se toca**: es un `record` que solo transporta el resultado del reporte; no se guarda.
>
> </details>
>
> <details>
> <summary><strong>Paso 1.3 · Ver las tablas que crea Hibernate</strong></summary>
>
> > Con las tres entidades escritas, se reinicia la aplicación. Gracias a `show-sql=true` aparece en la consola
> > (abreviado):
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
> > Para mirarlas con el navegador: abrir `http://localhost:8080/h2-console` y entrar con
> >
> > | Campo | Valor |
> > |---|---|
> > | JDBC URL | `jdbc:h2:mem:academia` |
> > | User Name / Password | `sa` / *(vacío)* |
> >
> > > 🗣️ **Preguntas:** ¿quién escribió ese `create table`? *(Hibernate, a partir de las anotaciones.)*
> > > ¿Qué columnas de `matricula` salen de `@JoinColumn`? *(`estudiante_id` y `curso_id`.)*
> >
> > ✅ **Punto de control 1:** la aplicación arranca, aparecen **tres** `create table` y la consola de H2 muestra
> > las tablas `ESTUDIANTE`, `CURSO` y `MATRICULA`. *Aún no compilan los repositorios antiguos: se arreglan en el paso siguiente.*
>
> </details>

</details>

---

<details>
<summary><strong>🗃️ Paso 2 — Repositorios con Spring Data JPA (capa de persistencia)</strong></summary>

> <details>
> <summary><strong>Paso 2.1 · Borrar las implementaciones «EnMemoria»</strong></summary>
>
> > Se eliminan las **tres clases** `RepositorioEstudiantesEnMemoria`, `RepositorioCursosEnMemoria` y
> > `RepositorioMatriculasEnMemoria`. Ya no hay listas, ni contadores `siguienteId`, ni ciclos `for` para buscar.
> >
> > > 🗣️ **Pregunta:** ¿qué pasaría si se dejaran, aunque sin usarse? *(Compilarían mal: implementan una
> > > interfaz con métodos que ya no existen y, además, dos beans del mismo tipo causarían ambigüedad.)*
>
> </details>
>
> <details>
> <summary><strong>Paso 2.2 · Las interfaces ahora extienden <code>JpaRepository</code></strong></summary>
>
> > Cada repositorio es solo una **interfaz**. `JpaRepository<Entidad, TipoDelId>` ya trae `save`, `findAll`, `findById`,
> > `deleteById`, `count`… ([Ejemplo 06 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/06-crud-completo.md)). Lo único que se escribe son las consultas propias, como
> > **métodos derivados**: Spring Data lee el nombre y genera la consulta ([Ejemplo 07 del M3](../../03-introduccion-a-jpa/material/ejemplos/07-uso-basico-de-spring-data-jpa.md)).
> >
> > **Archivo:** `src/main/java/com/academia/persistence/repository/RepositorioEstudiantes.java`
> >
> > ```java
> > package com.academia.persistence.repository;
> >
> > import com.academia.negocio.model.Estudiante;
> > import org.springframework.data.jpa.repository.JpaRepository;
> >
> > public interface RepositorioEstudiantes extends JpaRepository<Estudiante, Long> {
> >
> >     boolean existsByCorreoIgnoreCase(String correo);
> > }
> > ```
> >
> > **Archivo:** `src/main/java/com/academia/persistence/repository/RepositorioCursos.java`
> >
> > ```java
> > package com.academia.persistence.repository;
> >
> > import com.academia.negocio.model.Curso;
> > import java.util.List;
> > import org.springframework.data.jpa.repository.JpaRepository;
> >
> > public interface RepositorioCursos extends JpaRepository<Curso, Long> {
> >
> >     List<Curso> findByPrecioLessThan(double precio);
> > }
> > ```
> >
> > **Archivo:** `src/main/java/com/academia/persistence/repository/RepositorioMatriculas.java`
> >
> > ```java
> > package com.academia.persistence.repository;
> >
> > import com.academia.negocio.model.Matricula;
> > import org.springframework.data.jpa.repository.JpaRepository;
> >
> > public interface RepositorioMatriculas extends JpaRepository<Matricula, Long> {
> >
> >     boolean existsByEstudianteIdAndCursoId(Long estudianteId, Long cursoId);
> >
> >     long countByCursoId(Long cursoId);
> > }
> > ```
> >
> > **De la guía anterior a esta: cómo se traduce cada método**
> >
> > | Antes (clase «EnMemoria») | Ahora (Spring Data) | Consulta generada |
> > |---|---|---|
> > | `guardar(x)` | `save(x)` *(heredado)* | `insert into …` |
> > | `listar()` | `findAll()` *(heredado)* | `select … from …` |
> > | `buscarPorId(id)` | `findById(id)` *(heredado)* | `select … where id=?` |
> > | `existePorCorreo(correo)` | `existsByCorreoIgnoreCase(correo)` | `… where upper(correo)=upper(?)` |
> > | ciclo `for` + `if (precio < 200000)` | `findByPrecioLessThan(precio)` | `… where precio < ?` |
> > | `existe(estudianteId, cursoId)` | `existsByEstudianteIdAndCursoId(…)` | `… where estudiante_id=? and curso_id=?` |
> > | `contarPorCurso(cursoId)` | `countByCursoId(cursoId)` | `select count(id) … where curso_id=?` |
> >
> > > 🔎 **Concepto (M3):** el nombre del método **es** la consulta. `existsBy` + `Correo` + `IgnoreCase`;
> > > `countBy` + `CursoId` recorre la relación (`curso.id`); `And` combina dos condiciones.
> > >
> > > 🔎 **Concepto (M2, repaso):** el servicio sigue dependiendo de una **interfaz**; la diferencia es que
> > > la implementación ya no la escribe una persona: Spring Data la crea al arrancar y la registra como
> > > **bean singleton**, igual que antes con `@Repository`. Por eso aquí **no** hay `@Repository`.
> > >
> > > ⚠️ **Trampa:** si el nombre no coincide con un atributo (`existsByCorreos…`), la aplicación **no arranca**:
> > > `PropertyReferenceException: No property 'correos' found for type 'Estudiante'`.
>
> </details>
>
> <details>
> <summary><strong>Paso 2.3 · ¿Por qué <code>findByPrecioLessThan</code> y no un <code>for</code>?</strong></summary>
>
> > La **regla** (*«económico = precio menor que 200.000»*, **RN-06**) sigue viviendo en el servicio, como la constante
> > `PRECIO_ECONOMICO`. Lo que se delega a la base es **el trabajo de filtrar**: en lugar de traer todos los cursos a
> > memoria y recorrerlos, la base devuelve solo los que cumplen. Con miles de filas, la diferencia es enorme.
> >
> > > 🗣️ **Pregunta:** ¿dónde está la regla y dónde está la mecánica? *(Regla: el número 200.000, en el servicio.
> > > Mecánica: filtrar, en el repositorio.)*
> >
> > ✅ **Punto de control 2:** el proyecto **compila** una vez actualizados los servicios (paso siguiente). Si el
> > IDE marca errores en los servicios, es lo esperado: llaman a métodos que ya no existen.
>
> </details>

</details>

---

<details>
<summary><strong>⚙️ Paso 3 — Servicios y controladores (mismas reglas)</strong></summary>

> <details>
> <summary><strong>Paso 3.1 · <code>ServicioEstudiantes</code></strong></summary>
>
> > **Archivo:** `src/main/java/com/academia/negocio/service/ServicioEstudiantes.java`
> >
> > ```java
> > package com.academia.negocio.service;
> >
> > import com.academia.negocio.model.Estudiante;
> > import com.academia.persistence.repository.RepositorioEstudiantes;
> > import java.util.List;
> > import java.util.NoSuchElementException;
> > import org.springframework.stereotype.Service;
> >
> > @Service
> > public class ServicioEstudiantes {
> >
> >     private final RepositorioEstudiantes repositorio;
> >
> >     public ServicioEstudiantes(RepositorioEstudiantes repositorio) {
> >         this.repositorio = repositorio;
> >     }
> >
> >     public Estudiante registrar(Estudiante estudiante) {
> >         if (repositorio.existsByCorreoIgnoreCase(estudiante.getCorreo())) {
> >             throw new IllegalStateException("Ya existe un estudiante con el correo " + estudiante.getCorreo());
> >         }
> >         // Se crea una copia nueva: así se descarta cualquier id enviado por el cliente (RN-01)
> >         return repositorio.save(new Estudiante(estudiante.getNombre(), estudiante.getEdad(), estudiante.getCorreo()));
> >     }
> >
> >     public List<Estudiante> listar() {
> >         return repositorio.findAll();
> >     }
> >
> >     public Estudiante obtener(Long id) {
> >         return repositorio.findById(id)
> >                 .orElseThrow(() -> new NoSuchElementException("No existe el estudiante " + id));
> >     }
> > }
> > ```
> >
> > | Línea | Antes | Ahora |
> > |---|---|---|
> > | Regla RN-02 | `repositorio.existePorCorreo(…)` | `repositorio.existsByCorreoIgnoreCase(…)` |
> > | Guardar | `repositorio.guardar(estudiante)` | `repositorio.save(new Estudiante(…))` |
> > | Listar | `repositorio.listar()` | `repositorio.findAll()` |
> > | Obtener | `buscarPorId` | `findById` (devuelve `Optional`, igual que antes) |
> >
> > > ⚠️ **El detalle que más importa: el `id` que envía el cliente.** Con JPA, `save` de un objeto que **ya trae
> > > `id`** no inserta: **actualiza** (hace un *merge*). Probado sin la copia: un `POST /estudiantes` con
> > > `{"id":1,"nombre":"Intruso",…}` **sobrescribía a Ana**. En la versión en memoria el repositorio descartaba ese
> > > `id`; ahora lo hace el servicio **creando un objeto nuevo** con solo los datos permitidos. Así se
> > > cumple **RN-01** (*el identificador lo decide el sistema*). La solución de fondo —un objeto de entrada
> > > que ni siquiera tenga `id`— son los DTO, que llegan más adelante.
>
> </details>
>
> <details>
> <summary><strong>Paso 3.2 · <code>ServicioCursos</code></strong></summary>
>
> > **Archivo:** `src/main/java/com/academia/negocio/service/ServicioCursos.java`
> >
> > ```java
> > package com.academia.negocio.service;
> >
> > import com.academia.negocio.model.Curso;
> > import com.academia.negocio.model.Ocupacion;
> > import com.academia.persistence.repository.RepositorioCursos;
> > import com.academia.persistence.repository.RepositorioMatriculas;
> > import java.util.List;
> > import java.util.NoSuchElementException;
> > import org.springframework.stereotype.Service;
> >
> > @Service
> > public class ServicioCursos {
> >
> >     private static final double PRECIO_ECONOMICO = 200_000;
> >
> >     private final RepositorioCursos repositorio;
> >     private final RepositorioMatriculas repositorioMatriculas;
> >
> >     public ServicioCursos(RepositorioCursos repositorio, RepositorioMatriculas repositorioMatriculas) {
> >         this.repositorio = repositorio;
> >         this.repositorioMatriculas = repositorioMatriculas;
> >     }
> >
> >     public Curso registrar(Curso curso) {
> >         if (curso.getPrecio() <= 0) {
> >             throw new IllegalArgumentException("El precio debe ser mayor que cero");
> >         }
> >         if (curso.getCupoMaximo() <= 0) {
> >             throw new IllegalArgumentException("El cupo máximo debe ser mayor que cero");
> >         }
> >         // Se crea una copia nueva: así se descarta cualquier id enviado por el cliente (RN-01)
> >         return repositorio.save(new Curso(curso.getNombre(), curso.getPrecio(), curso.getCupoMaximo()));
> >     }
> >
> >     public List<Curso> listar() {
> >         return repositorio.findAll();
> >     }
> >
> >     public List<Curso> listarEconomicos() {
> >         return repositorio.findByPrecioLessThan(PRECIO_ECONOMICO);
> >     }
> >
> >     public Curso obtener(Long id) {
> >         return repositorio.findById(id)
> >                 .orElseThrow(() -> new NoSuchElementException("No existe el curso " + id));
> >     }
> >
> >     public Ocupacion calcularOcupacion(Long id) {
> >         Curso curso = obtener(id);
> >         int matriculados = (int) repositorioMatriculas.countByCursoId(id);
> >         int disponibles = curso.getCupoMaximo() - matriculados;
> >         double porcentaje = (double) matriculados / curso.getCupoMaximo() * 100;
> >         porcentaje = Math.round(porcentaje * 100.0) / 100.0;
> >         return new Ocupacion(curso.getNombre(), curso.getCupoMaximo(), matriculados, disponibles, porcentaje);
> >     }
> > }
> > ```
> >
> > > 🔎 Cambios: `save`, `findAll`, `findById`; `listarEconomicos()` ahora delega en
> > > `findByPrecioLessThan(PRECIO_ECONOMICO)`; el conteo usa `countByCursoId`, que devuelve `long`
> > > (por eso el *cast* `(int)` al armar la `Ocupacion`). El cálculo del porcentaje **no cambia** y conserva
> > > el *cast* a `double` **antes** de dividir (RN-11).
>
> </details>
>
> <details>
> <summary><strong>Paso 3.3 · <code>ServicioMatriculas</code></strong></summary>
>
> > **Archivo:** `src/main/java/com/academia/negocio/service/ServicioMatriculas.java`
> >
> > ```java
> > package com.academia.negocio.service;
> >
> > import com.academia.negocio.model.Curso;
> > import com.academia.negocio.model.Estudiante;
> > import com.academia.negocio.model.Matricula;
> > import com.academia.persistence.repository.RepositorioMatriculas;
> > import org.springframework.stereotype.Service;
> >
> > @Service
> > public class ServicioMatriculas {
> >
> >     private final RepositorioMatriculas repositorio;
> >     private final ServicioEstudiantes servicioEstudiantes;
> >     private final ServicioCursos servicioCursos;
> >
> >     public ServicioMatriculas(RepositorioMatriculas repositorio,
> >                               ServicioEstudiantes servicioEstudiantes,
> >                               ServicioCursos servicioCursos) {
> >         this.repositorio = repositorio;
> >         this.servicioEstudiantes = servicioEstudiantes;
> >         this.servicioCursos = servicioCursos;
> >     }
> >
> >     public Matricula matricular(Long estudianteId, Long cursoId) {
> >         Estudiante estudiante = servicioEstudiantes.obtener(estudianteId);
> >         Curso curso = servicioCursos.obtener(cursoId);
> >
> >         if (repositorio.existsByEstudianteIdAndCursoId(estudianteId, cursoId)) {
> >             throw new IllegalStateException(
> >                     "El estudiante " + estudianteId + " ya está matriculado en el curso " + cursoId);
> >         }
> >         if (repositorio.countByCursoId(cursoId) >= curso.getCupoMaximo()) {
> >             throw new IllegalStateException("El curso " + cursoId + " no tiene cupos disponibles");
> >         }
> >         return repositorio.save(new Matricula(estudiante, curso));
> >     }
> > }
> > ```
> >
> > > 🔎 **El orden de los cinco controles es exactamente el mismo** (estudiante existe → curso existe → ya
> > > matriculado → quedan cupos → guardar): define qué error ve el cliente primero. Cambian solo las
> > > dos consultas y el `save`. La dependencia de `ServicioMatriculas` sigue siendo la misma de tres
> > > beans y **sigue sin haber ciclos**: `ServicioCursos` usa el **repositorio** de matrículas, no el servicio.
>
> </details>
>
> <details>
> <summary><strong>Paso 3.4 · Los controladores no cambian</strong></summary>
>
> > `ControladorEstudiantes`, `ControladorCursos`, `ControladorMatriculas` y `SolicitudMatricula` quedan **idénticos** a los
> > de la guía anterior. Se puede comprobar con el docente proyectando ambos proyectos lado a lado.
> >
> > > 🗣️ **Pregunta para el grupo:** ¿por qué el controlador no se entera de que ahora hay una base de datos?
> > > *(Solo habla con el servicio; el servicio habla con la interfaz del repositorio; nadie más conoce H2.)*
> >
> > ✅ **Punto de control 3:** el proyecto compila y arranca. Los datos aún no se han probado: eso sigue.
>
> </details>

</details>

---

<details>
<summary><strong>🧪 Paso 4 — Pruebas de la API y de la base de datos</strong></summary>

> <details>
> <summary><strong>Paso 4.1 · Escenarios (los mismos de la guía anterior)</strong></summary>
>
> > Se reinicia la aplicación (la base en memoria parte vacía) y se ejecutan **en este orden**. Los resultados son
> > **idénticos** a los de la versión en memoria: esa es la prueba de que la migración respetó las reglas.
> >
> > | # | Escenario | Petición | Esperado |
> > |:-:|---|---|:-:|
> > | 1 | Registrar estudiante 1 | `POST /estudiantes` Ana | `201` · `{"id":1,"nombre":"Ana Torres","edad":22,"correo":"ana@correo.com"}` |
> > | 2 | Registrar estudiante 2 | `POST /estudiantes` Luis | `201` |
> > | 3 | Correo duplicado | `POST /estudiantes` con `ANA@correo.com` | `500` · `IllegalStateException` |
> > | 4 | Estudiante inexistente | `GET /estudiantes/99` | `500` · `NoSuchElementException` |
> > | 5 | Crear curso de 3 cupos | `POST /cursos` | `201` |
> > | 6 | Curso inválido | `POST /cursos` con `precio: 0` | `500` · `IllegalArgumentException` |
> > | 7 | Matricular a Ana | `POST /matriculas` | `201` (con el estudiante y el curso anidados) |
> > | 8 | Matricular a Luis | `POST /matriculas` | `201` |
> > | 9 | Matrícula repetida | `POST /matriculas` Ana otra vez | `500` · `IllegalStateException` |
> > | 10 | Ocupación | `GET /cursos/1/ocupacion` | `1` cupo disponible y `66.67 %` |
> > | 11 | `id` enviado por el cliente | `POST /estudiantes` con `{"id":1,…}` | `201`, pero **con un `id` nuevo**; Ana no se modifica |
> >
> > Respuesta del escenario 10:
> >
> > ```json
> > {"curso":"Java desde cero","cupoMaximo":3,"matriculados":2,"cuposDisponibles":1,"porcentajeOcupacion":66.67}
> > ```
> >
> > Respuesta del escenario 7 (la matrícula ya no trae `estudianteId` y `cursoId` sueltos, sino los objetos completos):
> >
> > ```json
> > {"id":1,"estudiante":{"id":1,"nombre":"Ana Torres","edad":22,"correo":"ana@correo.com"},
> >  "curso":{"id":1,"nombre":"Java desde cero","precio":150000.0,"cupoMaximo":3}}
> > ```
>
> </details>
>
> <details>
> <summary><strong>Paso 4.2 · Leer el SQL que genera cada petición</strong></summary>
>
> > Con `show-sql=true`, **una misma matrícula** produce estas consultas (reales, abreviadas). Se proyectan junto al código del servicio:
> >
> > ```text
> > POST /matriculas  {"estudianteId":1,"cursoId":1}
> > Hibernate: select … from estudiante e1_0 where e1_0.id=?                              ← servicioEstudiantes.obtener  (findById)
> > Hibernate: select … from curso c1_0 where c1_0.id=?                                   ← servicioCursos.obtener       (findById)
> > Hibernate: select m1_0.id from matricula m1_0 where m1_0.estudiante_id=? and m1_0.curso_id=? fetch first ? rows only
> >                                                                                       ← existsByEstudianteIdAndCursoId
> > Hibernate: select count(m1_0.id) from matricula m1_0 where m1_0.curso_id=?            ← countByCursoId
> > Hibernate: insert into matricula (curso_id,estudiante_id,id) values (?,?,default)     ← save
> > ```
> >
> > > 🗣️ **Pregunta:** ¿en qué orden están las consultas y a qué regla corresponde cada una?
> > > *(Mismo orden que los controles: RN-03, RN-07, RN-08, RN-09 y, si todo pasa, RN-10.)*
> > >
> > > 🔎 **Concepto (M3):** esto es el **ORM**: el código solo habla de objetos (`Estudiante`, `Curso`) y
> > > Hibernate lo traduce a SQL ([Ejemplo 04 del M3](../../03-introduccion-a-jpa/material/ejemplos/04-que-es-orm.md)).
>
> </details>
>
> <details>
> <summary><strong>Paso 4.3 · Mirar los datos en la consola de H2</strong></summary>
>
> > En `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:academia`, usuario `sa`) se ejecutan:
> >
> > ```sql
> > SELECT * FROM estudiante;
> > SELECT * FROM matricula;
> >
> > -- Quién está en cada curso (un JOIN que el código Java nunca escribió)
> > SELECT e.nombre, c.nombre AS curso
> > FROM matricula m
> > JOIN estudiante e ON e.id = m.estudiante_id
> > JOIN curso c      ON c.id = m.curso_id;
> > ```
> >
> > ✅ **Punto de control 4:** los escenarios 1 a 11 dan el resultado esperado y las filas de `MATRICULA` coinciden con
> > lo enviado por la API. **Se detiene la aplicación y se vuelve a arrancar: la base en memoria vuelve a estar vacía.**
> > Ese es el problema que resuelve el paso siguiente.
>
> </details>

</details>

---

<details>
<summary><strong>💾 Paso 5 — Que los datos sobrevivan al reinicio (H2 en archivo)</strong></summary>

> Hasta aquí la base está **en memoria**: es una base de datos de verdad, pero vive dentro de la JVM y muere con ella.
> Cambiando **solo una línea** se guarda en un archivo del disco.
>
> **1. Cambiar la URL en `application.properties`:**
>
> ```properties
> # Antes:  jdbc:h2:mem:academia;DB_CLOSE_DELAY=-1
> spring.datasource.url=jdbc:h2:file:./data/academia
> ```
>
> **2. Probar la persistencia:**
>
> ```text
> a) Arrancar y ejecutar los escenarios 1, 5 y 7        (estudiante Ana, curso Java desde cero, una matrícula)
> b) Detener la aplicación con Ctrl + C                  → queda el archivo  data/academia.mv.db
> c) Volver a arrancar
> d) GET /estudiantes                                    → [{"id":1,"nombre":"Ana Torres",…}]   ✅ sigue ahí
>    GET /cursos/1/ocupacion                             → matriculados: 1, cuposDisponibles: 2, porcentajeOcupacion: 33.33
> ```
>
> **3. Agregar `data/` al `.gitignore`**: el archivo de la base **no** se sube al repositorio.
>
> | Pregunta | Respuesta |
> |---|---|
> | ¿Por qué no se crean de nuevo las tablas al reiniciar? | Existen en el archivo; `ddl-auto=update` solo agrega lo que falte y **no borra datos**. |
> | ¿Qué pasaría con `ddl-auto=create`? | Borraría y recrearía las tablas en **cada arranque**: se perdería todo ([tabla de valores en el Ejemplo 07 del M3](../../03-introduccion-a-jpa/material/ejemplos/07-uso-basico-de-spring-data-jpa.md)). |
> | ¿Cómo se empieza de cero? | Se detiene la aplicación y se borra la carpeta `data/`. |
> | ¿Dónde se crea `data/`? | **Donde se ejecuta Java** (la carpeta del proyecto con `mvnw`). |
>
> > 🔎 **Cierre de la historia.** En el taller de los módulos 01 y 02 reiniciar borraba todo y se dijo que era
> > *«lo que motivará el Módulo 3»*. Ahora el mismo escenario conserva los datos **sin cambiar ni una regla**.
> >
> > 💡 **Y en producción:** H2 en archivo es una comodidad de desarrollo. Contra MySQL o PostgreSQL cambian el
> > **driver** (dependencia) y las propiedades `spring.datasource.*`; el código de las tres capas **no se toca**.
> > Es uno de los retos finales.

</details>

---

<details>
<summary><strong>🏁 Cierre — preguntas de síntesis</strong></summary>

> 1. ¿Qué anotación convierte una clase en tabla y cuál marca la clave primaria? *(`@Entity` y `@Id`; con
>    `@GeneratedValue` la base genera el valor.)*
> 2. ¿Quién escribe la implementación de `RepositorioEstudiantes`? *(Nadie: Spring Data la genera al arrancar.)*
> 3. ¿Qué clases **no** cambiaron al pasar de listas a base de datos y por qué? *(Los controladores y las reglas:
>    dependen de interfaces y no saben cómo se guardan los datos.)*
> 4. ¿Qué diferencia hay entre una base **en memoria** y una **en archivo**? *(Ambas son bases de datos reales; solo la
>    segunda sobrevive al reinicio de la aplicación.)*
> 5. ¿Por qué se crea un objeto nuevo en `registrar` en lugar de guardar el que llega? *(Para ignorar el `id` del
>    cliente: con JPA, `save` de un objeto con `id` actualiza en lugar de insertar.)*
> 6. ¿Qué consulta SQL corresponde a `countByCursoId`? *(`select count(id) from matricula where curso_id=?`.)*

</details>

<details>
<summary><strong>🌐 Endpoints y comandos curl</strong></summary>

> Son **los mismos endpoints** de la guía anterior; la API no cambió. Con la aplicación en `localhost:8080`:
>
> | # | Método | Ruta | Para qué sirve | Éxito |
> |:-:|:-:|---|---|:-:|
> | 1 | `POST` | `/estudiantes` | Registrar un estudiante | `201` |
> | 2 | `GET` | `/estudiantes` | Listar los estudiantes | `200` |
> | 3 | `GET` | `/estudiantes/{id}` | Consultar un estudiante | `200` |
> | 4 | `POST` | `/cursos` | Registrar un curso | `201` |
> | 5 | `GET` | `/cursos` | Listar los cursos | `200` |
> | 6 | `GET` | `/cursos/economicos` | Listar los cursos económicos | `200` |
> | 7 | `POST` | `/matriculas` | Matricular un estudiante en un curso | `201` |
> | 8 | `GET` | `/cursos/{id}/ocupacion` | Consultar la ocupación de un curso | `200` |
>
> ```bash
> curl -i -X POST localhost:8080/estudiantes -H "Content-Type: application/json" \
>   -d '{"nombre":"Ana Torres","edad":22,"correo":"ana@correo.com"}'
> curl -i -X POST localhost:8080/estudiantes -H "Content-Type: application/json" \
>   -d '{"nombre":"Luis Pérez","edad":25,"correo":"luis@correo.com"}'
> curl -i -X POST localhost:8080/cursos -H "Content-Type: application/json" \
>   -d '{"nombre":"Java desde cero","precio":150000,"cupoMaximo":3}'
> curl -i -X POST localhost:8080/cursos -H "Content-Type: application/json" \
>   -d '{"nombre":"Spring Boot","precio":450000,"cupoMaximo":10}'
> curl -i -X POST localhost:8080/matriculas -H "Content-Type: application/json" \
>   -d '{"estudianteId":1,"cursoId":1}'
>
> curl -i localhost:8080/estudiantes
> curl -i localhost:8080/cursos/economicos          # solo «Java desde cero»
> curl -i localhost:8080/cursos/1/ocupacion
>
> # Casos de error (sin manejador, Spring responde 500; el mensaje se lee en la consola)
> curl -i localhost:8080/estudiantes/99                                                   # NoSuchElementException
> curl -i -X POST localhost:8080/estudiantes -H "Content-Type: application/json" \
>   -d '{"nombre":"Otra Ana","edad":30,"correo":"ANA@correo.com"}'                         # IllegalStateException
> curl -i -X POST localhost:8080/cursos -H "Content-Type: application/json" \
>   -d '{"nombre":"Roto","precio":0,"cupoMaximo":5}'                                       # IllegalArgumentException
> ```
>
> > 💡 Qué significa cada opción de `curl` está explicado en la
> > [guía de los módulos 01 y 02](../modulo-01-02/01-guia-taller-gestion-de-cursos.md). En PowerShell se usa `curl.exe`.

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Solución |
> |---|---|---|
> | `Failed to configure a DataSource: 'url' attribute is not specified` | Falta la dependencia `h2` o `application.properties` | Agregar el driver; revisar que esté en `src/main/resources` |
> | `Not a managed type: class …Estudiante` o repositorios sin entidad | Falta `@Entity`, o la clase quedó **fuera** de `com.academia` (el escaneo no la ve) | Anotar la clase y dejarla bajo el paquete de `AcademiaApplication` |
> | `No default constructor for entity` / `InstantiationException` | La entidad no tiene constructor **sin argumentos** | Agregar `protected Estudiante() {}` |
> | `PropertyReferenceException: No property 'x' found for type 'Estudiante'` | El nombre del método derivado no coincide con un atributo | Escribirlo con el nombre exacto del atributo: `existsByCorreoIgnoreCase`, `countByCursoId` |
> | `Table "ESTUDIANTE" not found` | `ddl-auto=none`, o se apuntó a otra base (`mem` vs `file`) | Usar `update` y revisar la URL |
> | `Port 8080 was already in use` | Otra ejecución sigue abierta | Detenerla o cambiar `server.port` |
> | Los datos desaparecen al reiniciar | La URL es `jdbc:h2:mem:…` | Cambiar a `jdbc:h2:file:./data/academia` |
> | Un `POST` con `id` **modifica** un registro existente | Se guardó el objeto recibido en lugar de uno nuevo | Crear el objeto nuevo en el servicio (Paso 3.1) |
> | `Database may be already in use` al abrir el archivo | Dos procesos usan el mismo `data/academia.mv.db` | Cerrar la otra ejecución |
> | `415 Unsupported Media Type` | Falta `Content-Type: application/json` | Agregarlo en Postman o `curl` |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **Restricción en la base:** en `Matricula`, agregar `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"estudiante_id", "curso_id"}))`
>    y comprobar en la consola de H2 que la base **también** impide la matrícula repetida (RN-08). ¿Para qué sirve si el servicio ya lo valida?
>    *(Dos peticiones simultáneas podrían pasar ambas la validación; la restricción es la última defensa.)*
> 2. **Transacción:** anotar `matricular` con `@Transactional` y explicar qué ventaja da cuando se hacen varias operaciones
>    ([Ejemplo 08 del M3](../../03-introduccion-a-jpa/material/ejemplos/08-anotaciones-en-jpa.md)).
> 3. **Consulta con JPQL:** reescribir `countByCursoId` con `@Query("SELECT COUNT(m) FROM Matricula m WHERE m.curso.id = :cursoId")`.
> 4. **Más consultas:** `GET /estudiantes/{id}/matriculas` con un método derivado `findByEstudianteId`.
> 5. **Adaptador:** conservar los nombres antiguos (`guardar`, `listar`, `buscarPorId`…) creando una clase `RepositorioEstudiantesJpa`
>    que implemente la interfaz antigua y delegue en `JpaRepository`; así los servicios **no cambian ni una línea**.
> 6. **Otro motor:** reemplazar H2 por **MySQL** o **PostgreSQL** (dependencia del driver + `spring.datasource.*`) y comprobar que ninguna
>    clase de las tres capas cambia.
> 7. **Relación bidireccional:** agregar `List<Matricula>` en `Curso` con `@OneToMany(mappedBy = "curso")` y explicar por qué hay que
>    evitar el ciclo al convertir a JSON ([Ejemplo 03 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/03-oneToMany-manyToOne-y-fetch.md)).

</details>

<details>
<summary><strong>📁 Estructura final esperada</strong></summary>

> ```text
> 📁 academia
> ├── 📄 pom.xml
> ├── 📄 .gitignore                         ← incluye  data/
> ├── 📁 data                               ← lo crea H2 en el Paso 5 (no se versiona)
> │   └── 📄 academia.mv.db
> └── 📁 src/main
>     ├── 📁 java/com/academia
>     │   ├── 📄 AcademiaApplication.java
>     │   ├── 📁 presentacion
>     │   │   └── 📁 controlador   ControladorEstudiantes · ControladorCursos · ControladorMatriculas · SolicitudMatricula
>     │   ├── 📁 negocio
>     │   │   ├── 📁 model         Estudiante · Curso · Matricula (@Entity)  ·  Ocupacion (record)
>     │   │   └── 📁 service       ServicioEstudiantes · ServicioCursos · ServicioMatriculas
>     │   └── 📁 persistence
>     │       └── 📁 repository    3 interfaces que extienden JpaRepository  (sin clases «EnMemoria»)
>     └── 📁 resources
>         └── 📄 application.properties
> ```

</details>

<details>
<summary><strong>🏷️ Repaso de las anotaciones nuevas (JPA)</strong></summary>

> Se **suman** a las del taller anterior (`@RestController`, `@Service`, `@GetMapping`, `@PostMapping`, `@PathVariable`,
> `@RequestBody`, `@ResponseStatus`, `@SpringBootApplication`). Se **dejan de usar** `@Repository` y las clases `…EnMemoria`.
>
> | Anotación | Dónde se usa | Por qué | Para qué sirve |
> |---|---|---|---|
> | `@Entity` | `Estudiante`, `Curso`, `Matricula` | La clase representa una **tabla**. | Hibernate la mapea y crea la tabla (`ddl-auto`). |
> | `@Id` | Atributo `id` de cada entidad | Toda entidad necesita **clave primaria**. | Marca la columna que identifica cada fila. |
> | `@GeneratedValue(strategy = IDENTITY)` | Atributo `id` | El valor lo debe decidir el sistema (**RN-01**). | La base lo genera de forma autoincremental. |
> | `@Column(unique = true)` | `Estudiante.correo` | Un correo no puede repetirse (**RN-02**). | Crea una restricción `UNIQUE` en la columna. |
> | `@ManyToOne` | `Matricula.estudiante`, `Matricula.curso` | Muchas matrículas apuntan a **un** estudiante o curso. | Declara la relación de muchos a uno. |
> | `@JoinColumn(name = "…")` | Esos mismos dos atributos | Hay que nombrar la **clave foránea**. | Define la columna `estudiante_id` / `curso_id`; `Matricula` es el **lado dueño**. |
>
> ```text
> presentacion  ──▶  @RestController · @RequestMapping · @GetMapping · @PostMapping · @PathVariable · @RequestBody · @ResponseStatus
> negocio       ──▶  @Service  ·  @Entity · @Id · @GeneratedValue · @Column · @ManyToOne · @JoinColumn   (en negocio.model)
> persistence   ──▶  (ninguna)  interfaces que extienden JpaRepository; Spring Data crea el bean
> arranque      ──▶  @SpringBootApplication
> ```
>
> > 🔎 **Lectura clave.** Las anotaciones de JPA viven en `negocio.model` porque describen **cómo se guarda el dato**, pero
> > **no aparecen** en servicios ni en controladores. Más ejemplos de cada una: [Anotaciones en JPA](../../03-introduccion-a-jpa/material/ejemplos/08-anotaciones-en-jpa.md).

</details>
