# 🗄️ Guía paso a paso — Taller «Gestión de cursos» con base de datos (JPA): entidades y repositorios

> 👩‍🏫 **Material docente.** Guía para resolver **junto con los estudiantes**, en vivo, las **tres capas** del taller «Gestión de cursos» con base de datos: persistencia, negocio y presentación.
> Aplica lo visto en el [Módulo 03](../../03-introduccion-a-jpa/) y el [Módulo 04](../../04-gestion-de-bases-de-datos-mediante-jpa/).
> Contiene código completo: no se entrega a los estudiantes antes de resolver el taller.
>
> ✅ **Verificada.** El código de esta guía se compiló y se ejecutó con Spring Boot 3.3 y MySQL 8: las tablas, las consultas derivadas y la persistencia entre reinicios que se muestran son salidas reales. ⚠️ **Los requisitos de esta guía ya están en Java 21 y Spring Boot 4.1.2, pero este código no se volvió a ejecutar con esa versión** (los proyectos Maven de los módulos 03 a 08 sí).
>
> 🪄 **Versión con Lombok.** El código final (entidades, servicios y controladores con `@Getter`, `@NoArgsConstructor` y `@RequiredArgsConstructor`) se compiló con **Spring Boot 4.1.2** y JDK 25 (`release 21`), y se ejecutó contra MySQL 8 comprobando `POST /api/students` (`201` y `409`), `POST /api/courses`, `POST /api/enrollments` (`201` y `409` por cupo) y `GET /api/courses/{id}/enrolled`. Las salidas de las consultas derivadas y de las tablas son las de la ejecución anterior (Spring Boot 3.3).
>
> 🧭 **Idea central.** Cada capa tiene una sola responsabilidad: `persistence` guarda (**entidades** y **repositorios de Spring Data**), `business` decide (**servicios** con las reglas) y `presentation` atiende HTTP (**controladores**).
> El modelo del negocio (DTO) y los mapeadores **no se trabajan aquí**: se resuelven en otra guía.

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Construir las **tres capas** de **estudiantes, cursos y matrículas** con **Spring Boot**, **Spring Data JPA** y **MySQL**: entidades y repositorios, servicios con reglas de negocio y una API REST.
>
> Al terminar, el estudiante:
>
> - Declara **entidades JPA** (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@ManyToOne`, `@JoinColumn`) en `persistence.entity` (M3 y M4).
> - Configura la conexión a **MySQL** en `application.properties` y lee el SQL que genera Hibernate (M3 y M4).
> - Crea interfaces `JpaRepository` con **métodos derivados** (`existsBy…`, `countBy…`) (M3).
> - Escribe **servicios** (`@Service`) que aplican reglas del negocio y devuelven un `Optional` cuando no se cumplen.
> - Expone una **API REST** con controladores (`@RestController`) y elige el código HTTP (`201`, `404`, `409`) a partir de lo que devuelve el servicio.
> - Comprueba que las **tablas y los datos persisten** en MySQL entre reinicios.
> - Verifica que los datos **sobreviven al reinicio** en MySQL: la base vive en un servidor, no dentro de la aplicación.

</details>

<details>
<summary><strong>📝 Introducción: el problema y qué se construye</strong></summary>

> ### 🏫 El problema
>
> Una **academia de programación** (*academy*) necesita guardar **estudiantes**, **cursos** y **matrículas** en una base de datos. En esta guía se construye el proyecto completo en tres capas; la lógica del negocio y la API REST quedan para otra guía.
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
> | **RNF-01** | Java 21 o superior, Spring Boot 4 y Maven. |
> | **RNF-02** | Persistencia con **Spring Data JPA** y **MySQL 8** (en Docker o instalado). La base `academia` se crea sola al arrancar (`createDatabaseIfNotExist=true`). |
> | **RNF-03** | Tres capas: `presentation` → `business` → `persistence`. Las anotaciones de JPA van **solo** en `persistence.entity`; las de Spring Web, **solo** en `presentation.controller`. |
> | **RNF-04** | Inyección por constructor (generado con `@RequiredArgsConstructor`). Todos los nombres del código (paquetes, clases, atributos, tablas) en **inglés**. |
> | **RNF-05** | La verificación se hace con `curl` contra la API, con el SQL que imprime Hibernate y con el cliente de MySQL. |
> | **RNF-06** | Duración: 1 h 20 min. |
>
> > 💡 **Alcance.** Quedan fuera: el modelo del negocio (DTO), los mapeadores, las validaciones con Bean Validation, la seguridad, las relaciones bidireccionales, `cascade`,
> > migraciones (Flyway) y otros motores (MySQL/PostgreSQL).

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>

> | Paso | Minutos | Actividad | Quién escribe |
> |---|---:|---|---|
> | 0 | 0 – 10 | Preparación: proyecto, dependencias, `application.properties` y arranque | Docente |
> | 1 | 10 – 30 | Entidades JPA en `persistence.entity` (`StudentEntity` en vivo) | Docente y estudiantes |
> | 2 | 30 – 40 | Interfaces de Spring Data (`…JpaRepository`) | Estudiantes |
> | 3 | 40 – 55 | Capa de negocio: servicios y reglas | Estudiantes |
> | 4 | 55 – 70 | Capa de presentación: controladores y pruebas con `curl` | Todos |
> | 5 | 70 – 80 | Persistencia real (reinicio contra MySQL) y cierre | Todos |
>
> > 💡 **Ritmo.** El Paso 1 con `StudentEntity` fija el patrón; con `CourseEntity` y `EnrollmentEntity` los estudiantes lo repiten. Si falta tiempo, el
> > Paso 5 se hace como demostración del docente.

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
> | Proyecto y conexión a la base de datos | `pom.xml`, `application.properties`, MySQL | 04 | [Creación de un proyecto](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/01-creacion-de-un-proyecto-spring-boot.md) |
> | Lado dueño de la relación y `fetch` | `EnrollmentEntity` es dueña de las dos claves foráneas | 04 | [OneToMany/ManyToOne y fetch](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/03-oneToMany-manyToOne-y-fetch.md) |
> | Entidad intermedia | `EnrollmentEntity` une `StudentEntity` y `CourseEntity` | 04 | [ManyToMany y entidad intermedia](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/04-manyToMany-y-entidad-intermedia.md) |
> | CRUD | `save`, `findAll`, `count` | 04 | [CRUD completo](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/06-crud-completo.md) |
> | Inicialización del servidor | Hibernate crea las tablas al arrancar (`ddl-auto=create`) | 04 | [Inicialización del servidor](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/07-inicializacion-del-servidor.md) |

</details>

<details>
<summary><strong>🏗️ Arquitectura del taller: las capas y el árbol de archivos</strong></summary>

> El proyecto tiene **tres capas**. Cada capa solo habla con la **siguiente hacia abajo**: el controlador usa servicios, el servicio usa repositorios.
>
> ```text
> 📁 src/main/java/com/academy
> ├── 📄 AcademyApplication.java                  (la genera start.spring.io; no se modifica)
> ├── 📁 presentation                              ← 1 · Paso 4
> │   └── 📁 controller
> │       ├── 📄 StudentController.java
> │       ├── 📄 CourseController.java
> │       └── 📄 EnrollmentController.java
> ├── 📁 business                                  ← 2 · Paso 3
> │   └── 📁 service
> │       ├── 📄 StudentService.java
> │       ├── 📄 CourseService.java
> │       └── 📄 EnrollmentService.java
> └── 📁 persistence                               ← 3 · Pasos 1 y 2
>     ├── 📁 repository                            (➕ Spring Data)
>     │   ├── 📄 StudentJpaRepository.java
>     │   ├── 📄 CourseJpaRepository.java
>     │   └── 📄 EnrollmentJpaRepository.java
>     └── 📁 entity                                (➕ las @Entity)
>         ├── 📄 StudentEntity.java
>         ├── 📄 CourseEntity.java
>         └── 📄 EnrollmentEntity.java
> 📁 src/main/resources
> └── 📄 application.properties                    (➕ conexión a MySQL y JPA)
> ```
>
> | Capa | Paquetes | Qué contiene | En esta guía |
> |---|---|---|---|
> | Presentación | `presentation.controller` | Controladores REST: reciben HTTP y eligen el código de respuesta. | **Paso 4** |
> | Negocio | `business.service` | Servicios con las reglas del negocio. | **Paso 3** |
> | Persistencia | `persistence.repository`, `persistence.entity` | Repositorios de Spring Data y entidades JPA. | **Pasos 1 y 2** |
>
> ```text
> @RestController ─▶ @Service ─▶ …JpaRepository ─▶ Hibernate ─▶ MySQL
>  HTTP / JSON       reglas      Spring Data lo genera   genera el SQL   la base
> ```
>
> > 🔎 **Qué no está.** El modelo del negocio (DTO) y los mapeadores no se crean aquí; pertenecen a otra guía. Todos los nombres del proyecto (paquetes, clases, atributos, tablas y columnas) están en **inglés** para mantener un solo idioma en el código.

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
> > Se crea un proyecto nuevo en [start.spring.io](https://start.spring.io): **Maven · Java 21 o superior · Group** `com.academy` · **Artifact** `academy` ·
> > dependencias **Spring Web**, **Spring Data JPA**, **MySQL Driver**, **Lombok** y **Spring Boot DevTools**.
> >
> > En el `pom.xml` quedan estas cinco dependencias:
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
> >     <groupId>com.mysql</groupId>
> >     <artifactId>mysql-connector-j</artifactId>
> >     <scope>runtime</scope>
> > </dependency>
> > <dependency>
> >     <groupId>org.projectlombok</groupId>
> >     <artifactId>lombok</artifactId>
> >     <optional>true</optional>
> > </dependency>
> > <dependency>
> >     <groupId>org.springframework.boot</groupId>
> >     <artifactId>spring-boot-devtools</artifactId>
> >     <scope>runtime</scope>
> >     <optional>true</optional>
> > </dependency>
> > ```
> >
> > > 🪄 **¿Qué es Lombok?** Una librería que **escribe por ti** el código repetitivo (getters, constructores) **al compilar**. En el archivo `.java` se ven solo anotaciones; en la clase compilada están los métodos. Se explica en el Paso 1.1.
> >
> > > ⚠️ **Con Java 23 o superior** (por ejemplo, JDK 25), Maven **no ejecuta** Lombok por sí solo y el proyecto falla con `cannot find symbol … getEmail()`. Se corrige con una configuración en el `pom.xml` (el `maven-compiler-plugin` con `annotationProcessorPaths`), que está en el **pom final** de esta guía. Con Java 21 no hace falta.
> >
> > > 🔎 **Concepto (M3 y M4):**
> > >
> > > - `spring-boot-starter-webmvc` trae Spring MVC y un **Tomcat embebido**: es la base para los controladores REST de `presentation.controller`. En esta guía todavía no hay ninguno.
> > > - `spring-boot-starter-data-jpa` trae JPA, **Hibernate** y Spring Data.
> > > - `mysql-connector-j` es el **driver** de MySQL; `runtime` indica que solo hace falta al ejecutar.
> > > - `spring-boot-devtools` **reinicia la aplicación** al recompilar (en el IDE, *Build Project*). Solo se usa en desarrollo.
>
> </details>
>
> <details>
> <summary><strong>Paso 0.2 · Tener un servidor MySQL en marcha</strong></summary>
>
> > Hibernate crea las **tablas** y la URL de conexión (`createDatabaseIfNotExist=true`) hace que MySQL **cree la base `academia`** si no existe: solo hace falta un servidor en marcha. La forma más rápida es un contenedor de Docker:
> >
> > ```bash
> > docker run -d --name academy-mysql \
> >   -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academia \
> >   -p 3306:3306 mysql:8.0
> > ```
> >
> > Con un MySQL ya instalado no hace falta crear nada; si se prefiere crear la base a mano desde el cliente (`mysql -u root -p`):
> >
> > ```sql
> > CREATE DATABASE academia CHARACTER SET utf8mb4;
> > ```
> >
> > | Dato | Valor en esta guía |
> > |---|---|
> > | Servidor y puerto | `localhost:3306` *(si el 3306 está ocupado, usar `-p 3307:3306` y cambiar el puerto en la URL)* |
> > | Base | `academia` *(se crea sola)* |
> > | Usuario y contraseña | `root` / `root` *(solo para el taller; en un proyecto real se crea un usuario propio)* |
> >
> > > 💡 Para comprobar que responde: `docker exec academy-mysql mysql -uroot -proot -e "select version()"`.
>
> </details>
>
> <details>
> <summary><strong>Paso 0.3 · Configurar la conexión: <code>application.properties</code></strong></summary>
>
> > <details>
> > <summary><strong>📄 application.properties</strong></summary>
> >
> > **Ruta:** `src/main/resources/application.properties`
> >
> > ```properties
> > # MySQL connection
> > spring.datasource.url=jdbc:mysql://localhost:3306/academia?createDatabaseIfNotExist=true
> > spring.datasource.username=root
> > spring.datasource.password=root
> >
> > # JPA and Hibernate
> > spring.jpa.hibernate.ddl-auto=create
> > spring.jpa.show-sql=true
> > ```
> >
> > </details>
> >
> > | Propiedad | Para qué sirve |
> > |---|---|
> > | `spring.datasource.url` | Dirección de la base: `jdbc:mysql://` + servidor + puerto + nombre de la base (`academia`; `createDatabaseIfNotExist=true` la crea si no existe). |
> > | `spring.datasource.username`, `password` | Credenciales del servidor MySQL. El driver se deduce de la URL: no hace falta `driver-class-name`. |
> > | `spring.jpa.hibernate.ddl-auto=create` | Hibernate **borra y recrea las tablas** en cada arranque: se parte siempre de cero y los datos no se conservan ([Ejemplo 07 del M3](../../03-introduccion-a-jpa/material/ejemplos/07-uso-basico-de-spring-data-jpa.md)). |
> > | `spring.jpa.show-sql=true` | Imprime en la consola **el SQL** que ejecuta Hibernate: sirve para ver el ORM trabajando. |
> >
> > > 💡 **Diferencia con el Módulo 3.** Allí se declaraba `spring.jpa.database-platform=…` con el dialecto. Con Hibernate 7 (Spring Boot 4)
> > > el dialecto se **detecta solo** a partir de la conexión (aquí, MySQL), así que se omite; dejarlo no rompe nada, pero produce un aviso en el arranque.
>
> </details>
>
> <details>
> <summary><strong>Paso 0.4 · Crear las capas (paquetes)</strong></summary>
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
> > Los paquetes se llenan en orden: primero `persistence.entity` y `persistence.repository` (Pasos 1 y 2), después `business` (Paso 3) y por último `presentation.controller` (Paso 4).
> >
> > > 💡 Los paquetes se crean desde el IDE (*New → Package*). Un paquete vacío no aparece en Git ni en el `.jar` hasta que tenga una clase.
>
> </details>
>
> <details>
> <summary><strong>Paso 0.5 · Arrancar y comprobar</strong></summary>
>
> > Se ejecuta la aplicación (`./mvnw spring-boot:run`; en Windows, `mvnw.cmd spring-boot:run`):
> >
> > - Aparece `Tomcat started on port 8080` y `Started AcademyApplication in … seconds`, y la aplicación **sigue en ejecución** (hay servidor web): se detiene con `Ctrl+C`.
> > - Abrir `http://localhost:8080` responde **404**: es lo esperado, aún no hay controladores.
> > - Aparece el aviso `spring.jpa.open-in-view is enabled by default`: es normal en este punto y se atiende al construir la capa web.
> > - **No** se crea ninguna tabla (sin `@Entity`, Hibernate no tiene qué mapear).
> >
> > > ⚠️ **Trampas habituales.**
> > >
> > > - `Failed to configure a DataSource: 'url' attribute is not specified`: `application.properties` no está en `src/main/resources`.
> > > - `Communications link failure` o `Connection refused`: MySQL no está arrancado, o el puerto de la URL no es el del servidor.
> > > - `Unknown database 'academia'`: falta crear la base (Paso 0.2). `Access denied for user`: usuario o contraseña distintos de los del servidor.
> >
> > ✅ **Punto de control 0:** los paquetes existen, la aplicación se conecta a MySQL sin errores y no se crea ninguna tabla (`show tables;` en `academia` devuelve vacío).
>
> </details>

</details>

---

<details>
<summary><strong>🧱 Paso 1 — Entidades JPA (capa de persistencia)</strong></summary>

> <details>
> <summary><strong>Paso 1.1 · <code>StudentEntity</code> — capa de persistencia</strong></summary>
>
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
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import jakarta.persistence.Table;
> > import lombok.AccessLevel;
> > import lombok.Getter;
> > import lombok.NoArgsConstructor;
> >
> > @Entity
> > @Table(name = "student")
> > @Getter
> > @NoArgsConstructor(access = AccessLevel.PROTECTED)
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
> >     public StudentEntity(String name, int age, String email) {
> >         this.name = name;
> >         this.age = age;
> >         this.email = email;
> >     }
> > }
> > ```
> >
> > </details>
> >
> > | Elemento | Por qué |
> > |---|---|
> > | `@Entity` | Le dice a JPA que la clase se guarda en una tabla. |
> > | `@Table(name = "student")` | **Necesario:** sin él, la tabla se llamaría `student_entity` (el nombre de la clase). |
> > | `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` | `id` es la **clave primaria** y la **genera la base** (autoincremental). Es el equivalente a un contador que nunca repite. |
> > | `@Column(unique = true)` en `email` | La base rechaza correos repetidos (dos estudiantes no pueden tener el mismo `email`). |
> > | `@Getter` *(Lombok)* | Genera `getId()`, `getName()`, `getAge()` y `getEmail()`. **No** se escriben a mano. |
> > | `@NoArgsConstructor(access = AccessLevel.PROTECTED)` *(Lombok)* | Genera el constructor **vacío** y `protected`: JPA crea el objeto vacío y rellena los campos por reflexión (misma idea del JavaBean del M2), pero el código normal no puede usarlo. |
> > | Constructor con `name`, `age`, `email` | Se **escribe a mano** a propósito: **no incluye `id`**, porque lo decide la base. (`@AllArgsConstructor` lo incluiría.) |
> > | **Sin `@Setter`** | Tampoco hay `setId`: el `id` lo decide la base, no el código. |
> >
> > > 🔎 **Concepto (M3):** el nombre de cada columna sale del atributo (`age`, `name`); `maxCapacity` se convertirá en `max_capacity`.
> > > Los tipos se mapean solos: `String` → `varchar`, `int` → `integer`, `Long` → `bigint`.
> >
> > <details>
> > <summary><strong>🪄 Lombok: qué escribe por ti</strong></summary>
> >
> > > Sin Lombok, la entidad necesitaría esto a mano:
> > >
> > > ```java
> > > protected StudentEntity() { }                       // lo genera @NoArgsConstructor(access = PROTECTED)
> > > public Long getId() { return id; }                  // lo genera @Getter
> > > public String getName() { return name; }           // lo genera @Getter
> > > public int getAge() { return age; }                // lo genera @Getter
> > > public String getEmail() { return email; }         // lo genera @Getter
> > > ```
> > >
> > > | Anotación | Dónde | Qué genera |
> > > |---|---|---|
> > > | `@Getter` | Sobre la **clase** | Un `getX()` por cada atributo |
> > > | `@NoArgsConstructor(access = AccessLevel.PROTECTED)` | Sobre la **clase** | Un constructor vacío `protected` |
> > > | `@RequiredArgsConstructor` | Servicios y controladores (Pasos 3 y 4) | Un constructor con **todos los atributos `final`**, que es la inyección por constructor |
> > >
> > > Se ve el resultado con `javap` sobre la clase compilada, o en el panel de estructura del IDE: los métodos **existen** aunque no estén escritos.
> > >
> > > > ⚠️ En las entidades **no** se usa `@Data`: genera `equals`, `hashCode` y `toString` con todos los campos, y eso da problemas con relaciones y con el `id` que asigna la base. `@Getter` es suficiente.
> >
> > </details>
>
> </details>
>
> <details>
> <summary><strong>Paso 1.2 · <code>CourseEntity</code> y <code>EnrollmentEntity</code></strong></summary>
>
> > Los estudiantes aplican el mismo patrón. La diferencia está en `EnrollmentEntity`: no guarda los datos del estudiante ni del curso, sino
> > **claves foráneas** hacia sus tablas.
> >
> > <details>
> > <summary><strong>📄 CourseEntity.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/entity/CourseEntity.java`
> >
> > ```java
> > package com.academy.persistence.entity;
> >
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import jakarta.persistence.Table;
> > import lombok.AccessLevel;
> > import lombok.Getter;
> > import lombok.NoArgsConstructor;
> >
> > @Entity
> > @Table(name = "course")
> > @Getter
> > @NoArgsConstructor(access = AccessLevel.PROTECTED)
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
> >     public CourseEntity(String name, double price, int maxCapacity) {
> >         this.name = name;
> >         this.price = price;
> >         this.maxCapacity = maxCapacity;
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 EnrollmentEntity.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/entity/EnrollmentEntity.java`
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
> > import lombok.AccessLevel;
> > import lombok.Getter;
> > import lombok.NoArgsConstructor;
> >
> > @Entity
> > @Table(name = "enrollment")
> > @Getter
> > @NoArgsConstructor(access = AccessLevel.PROTECTED)
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
> >     public EnrollmentEntity(StudentEntity student, CourseEntity course) {
> >         this.student = student;
> >         this.course = course;
> >     }
> > }
> > ```
> >
> > </details>
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
> > Se ejecuta la aplicación. Con `show-sql=true` aparece en la consola (abreviado; `auto_increment` es la forma en que MySQL genera el `id`):
> >
> > ```text
> > Hibernate: create table course (id bigint not null auto_increment, max_capacity integer not null, name varchar(255), price float(53) not null, primary key (id)) engine=InnoDB
> > Hibernate: create table enrollment (id bigint not null auto_increment, course_id bigint, student_id bigint, primary key (id)) engine=InnoDB
> > Hibernate: create table student (id bigint not null auto_increment, age integer not null, email varchar(255), name varchar(255), primary key (id)) engine=InnoDB
> > Hibernate: alter table student add constraint UK… unique (email)
> > Hibernate: alter table enrollment add constraint FK… foreign key (course_id) references course (id)
> > Hibernate: alter table enrollment add constraint FK… foreign key (student_id) references student (id)
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
> > Para comprobarlo desde MySQL: `docker exec academy-mysql mysql -uroot -proot academy -e "show tables; show create table student\G"`.
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
> <details>
> <summary><strong>📄 StudentJpaRepository.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/repository/StudentJpaRepository.java`
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
> </details>
>
> <details>
> <summary><strong>📄 CourseJpaRepository.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/repository/CourseJpaRepository.java`
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
> </details>
>
> <details>
> <summary><strong>📄 EnrollmentJpaRepository.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/repository/EnrollmentJpaRepository.java`
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
> </details>
>
> <details>
> <summary><strong>🔍 Métodos derivados: la consulta que genera Spring</strong></summary>
>
> > | Método | Para qué |
> > |---|---|
> > | `existsByEmailIgnoreCase(email)` | Saber si un correo ya está registrado (sin importar mayúsculas). |
> > | `existsByStudentIdAndCourseId(…)` | Saber si un estudiante ya está matriculado en un curso. |
> > | `countByCourseId(courseId)` | Contar las matrículas de un curso. |
> > | `save`, `findAll`, `count` *(heredados)* | Guardar, listar y contar. |
> >
> > **La consulta que Spring genera a partir de cada nombre** (es lo que imprime `show-sql=true`; los `?` son los valores que se pasan al método):
> >
> > | Método | Se lee así | SQL que se ejecuta en MySQL |
> > |---|---|---|
> > | `existsByEmailIgnoreCase` | `exists` + `By` + `Email` + `IgnoreCase` | `select se1_0.id from student se1_0 where upper(se1_0.email)=upper(?) limit ?` |
> > | `existsByStudentIdAndCourseId` | `exists` + `By` + `Student.Id` + `And` + `Course.Id` | `select ee1_0.id from enrollment ee1_0 where ee1_0.student_id=? and ee1_0.course_id=? limit ?` |
> > | `countByCourseId` | `count` + `By` + `Course.Id` | `select count(ee1_0.id) from enrollment ee1_0 where ee1_0.course_id=?` |
> >
> > - **`exists…`** no trae la fila: pide solo el `id` y agrega `limit ?` (basta con encontrar **una**); si hay resultado devuelve `true`.
> > - **`IgnoreCase`** pasa a mayúsculas **los dos lados** (`upper(email)=upper(?)`), por eso `ANA@mail.com` encuentra `ana@mail.com`.
> > - **`StudentId` / `CourseId`** recorren la relación (`enrollment.student.id`), pero el SQL usa la **clave foránea** (`student_id`, `course_id`) y no necesita `join`.
> > - Los alias (`se1_0`, `ee1_0`) los inventa Hibernate: no hay que escribirlos.
> >
> > > 🗣️ **Para el estudiante:** ejecutar la aplicación, buscar estas tres líneas en la consola y relacionar **cada palabra del nombre** con una parte del SQL.
> >
> > > 🔎 **Concepto (M3):** el nombre del método **es** la consulta: `existsBy` + `Email` + `IgnoreCase`; `countBy` + `CourseId` recorre la relación
> > > (`course.id`); `And` une dos condiciones.
> >
> > > 🔎 **Concepto (M2, repaso):** estas interfaces **no** llevan `@Repository` ni tienen clase: Spring Data crea la implementación al arrancar
> > > y la registra como **bean singleton**.
> >
> > > ⚠️ **Trampa:** si el nombre no coincide con un atributo de la **entidad**, la aplicación **no arranca**:
> > > `PropertyReferenceException: No property 'emails' found for type 'StudentEntity'`.
>
> </details>
>
> <details>
> <summary><strong>📚 Todos los métodos derivados posibles: palabras clave y ejemplos</strong></summary>
>
> > Un método derivado se arma con **cuatro piezas**, siempre en este orden:
> >
> > ```text
> > find  [Distinct] [First3 | Top3]  By  Atributo [Operador]  [And | Or  Atributo [Operador]]  [OrderBy Atributo Asc|Desc]
> > └─1─┘ └───────────2───────────┘ └3┘ └──────────────────────4: criterios──────────────────────┘ └───────5: orden───────┘
> > ```
> >
> > **1 · Qué hace el método (prefijo antes de `By`)**
> >
> > | Prefijo | Qué devuelve | Ejemplo | SQL equivalente |
> > |---|---|---|---|
> > | `find…By`, `read…By`, `get…By`, `query…By`, `search…By`, `stream…By` | Las filas (`List`, `Optional`, la entidad, `Stream`…) | `findByEmail(String email)` | `select … from student where email=?` |
> > | `exists…By` | `boolean` | `existsByEmail(String email)` | `select id … where email=? limit 1` |
> > | `count…By` | `long` | `countByAge(int age)` | `select count(id) … where age=?` |
> > | `delete…By`, `remove…By` | Borra *(devuelve `void` o cuántas borró)* | `deleteByEmail(String email)` | `select …` y luego `delete … where id=?` por cada fila |
> >
> > **2 · Operadores de los criterios** (ejemplos con los atributos de `StudentEntity` y `CourseEntity`)
> >
> > | Palabra clave | Ejemplo | Condición generada |
> > |---|---|---|
> > | *(ninguna)*, `Is`, `Equals` | `findByName(String n)`, `findByNameIs(n)`, `findByNameEquals(n)` | `where name = ?` |
> > | `Not`, `IsNot` | `findByNameNot(String n)` | `where name <> ?` |
> > | `And` | `findByNameAndAge(String n, int a)` | `where name = ? and age = ?` |
> > | `Or` | `findByNameOrEmail(String n, String e)` | `where name = ? or email = ?` |
> > | `LessThan` | `findByPriceLessThan(double p)` | `where price < ?` |
> > | `LessThanEqual` | `findByPriceLessThanEqual(double p)` | `where price <= ?` |
> > | `GreaterThan` | `findByAgeGreaterThan(int a)` | `where age > ?` |
> > | `GreaterThanEqual` | `findByAgeGreaterThanEqual(int a)` | `where age >= ?` |
> > | `Between` | `findByAgeBetween(int min, int max)` | `where age between ? and ?` |
> > | `After`, `Before` | `findByStartDateAfter(LocalDate d)` *(atributos de fecha)* | `where start_date > ?` / `< ?` |
> > | `IsNull`, `Null` | `findByEmailIsNull()` | `where email is null` |
> > | `IsNotNull`, `NotNull` | `findByEmailIsNotNull()` | `where email is not null` |
> > | `Like` | `findByNameLike(String patron)` | `where name like ?` *(el `%` lo escribe quien llama: `"Ana%"`)* |
> > | `NotLike` | `findByNameNotLike(String patron)` | `where name not like ?` |
> > | `StartingWith` | `findByNameStartingWith("An")` | `where name like 'An%'` |
> > | `EndingWith` | `findByNameEndingWith("es")` | `where name like '%es'` |
> > | `Containing` | `findByNameContaining("or")` | `where name like '%or%'` |
> > | `In` | `findByAgeIn(List<Integer> edades)` | `where age in (?, ?, …)` |
> > | `NotIn` | `findByAgeNotIn(List<Integer> edades)` | `where age not in (?, ?, …)` |
> > | `True`, `False` | `findByActiveTrue()` *(atributo `boolean`)* | `where active = true` / `false` |
> > | `IgnoreCase` | `findByEmailIgnoreCase(String e)` | `where upper(email) = upper(?)` |
> > | `AllIgnoreCase` | `findByNameAndEmailAllIgnoreCase(n, e)` | `upper(name)=upper(?) and upper(email)=upper(?)` |
> >
> > **3 · Orden, límite y duplicados**
> >
> > | Palabra clave | Ejemplo | Qué hace |
> > |---|---|---|
> > | `OrderBy…Asc` / `Desc` | `findByAgeGreaterThanOrderByNameAsc(int a)` | `… order by name asc` *(se pueden encadenar: `OrderByAgeDescNameAsc`)* |
> > | `First` / `Top` | `findFirstByOrderByAgeDesc()` | Solo la **primera** fila (`limit 1`) |
> > | `FirstN` / `TopN` | `findTop3ByOrderByPriceDesc()` | Las **N** primeras filas (`limit 3`): los 3 cursos más caros |
> > | `Distinct` | `findDistinctByAge(int a)` | `select distinct …` |
> >
> > **4 · Recorrer relaciones**
> >
> > | Forma | Ejemplo | Qué hace |
> > |---|---|---|
> > | Atributo de la relación + atributo | `countByCourseId(Long id)` | `enrollment.course.id`; usa la clave foránea, sin `join` |
> > | Atributo de la relación + otro atributo | `findByCourseName(String n)` | `join course … where course.name = ?` |
> > | Guion bajo para separar | `findByCourse_Name(String n)` | Igual que el anterior; el `_` evita ambigüedad si hay un atributo `courseName` |
> >
> > **5 · Tipos de retorno que se pueden usar con `find…By`**
> >
> > | Tipo | Cuándo |
> > |---|---|
> > | `List<StudentEntity>` | Pueden ser varias filas (si no hay, lista vacía). |
> > | `Optional<StudentEntity>` | Se espera **una** fila o ninguna (si hay más de una, error). |
> > | `StudentEntity` | Se espera una fila; si no hay devuelve `null`. |
> > | `Stream<StudentEntity>` | Recorrido perezoso de muchas filas (debe cerrarse). |
> > | `Page<…>` / `Slice<…>` | Con un parámetro `Pageable` para paginar. |
> >
> > > 💡 **Reglas para no equivocarse.**
> > >
> > > - Los nombres de atributo van en **PascalCase** y deben existir **tal cual** en la entidad (`maxCapacity` → `MaxCapacity`).
> > > - El **orden de los parámetros** del método es el orden en que aparecen los criterios en el nombre.
> > > - `Between` recibe **dos** parámetros; `In` recibe una **colección**; `IsNull`, `True` y `False` no reciben ninguno.
> > > - Un nombre muy largo (`findByNameAndAgeAndEmailOrderByName…`) es señal de que conviene una consulta con `@Query`.
> >
> > > ⚠️ **Estado de la tabla.** Es la referencia de Spring Data JPA; el SQL se muestra de forma simplificada. Los tres métodos del taller (`existsByEmailIgnoreCase`,
> > > `existsByStudentIdAndCourseId`, `countByCourseId`) sí se ejecutaron y su SQL real está arriba. Para ver el de cualquier otro basta agregarlo a la interfaz y mirar la consola.
>
> </details>
>
> ✅ **Punto de control 2:** el proyecto compila y arranca; `CourseJpaRepository` queda vacía de métodos propios y está bien: solo necesita lo heredado.

</details>

---

<details>
<summary><strong>⚙️ Paso 3 — Capa de negocio (<code>business.service</code>)</strong></summary>

> El **servicio** aplica las **reglas del negocio** y decide cuándo se guarda algo. Usa los repositorios del Paso 2 y **no sabe nada de HTTP**: ni de rutas, ni de códigos de estado.
> Cuando una regla no se cumple, devuelve un **`Optional` vacío**: «no hay resultado». Quien lo recibe decide qué responder.
>
> | Regla del taller | Dónde se aplica | Qué repositorio usa |
> |---|---|---|
> | El correo de un estudiante no puede repetirse (sin importar mayúsculas) | `StudentService.create` | `existsByEmailIgnoreCase` |
> | Un curso necesita precio y cupo mayores que cero | `CourseService.create` | *(solo valida)* |
> | El estudiante y el curso deben existir | `EnrollmentService.enroll` | `findById` |
> | Un estudiante no se matricula dos veces en el mismo curso | `EnrollmentService.enroll` | `existsByStudentIdAndCourseId` |
> | No se matricula si el curso ya llegó a su cupo | `EnrollmentService.enroll` | `countByCourseId` |
>
> <details>
> <summary><strong>Paso 3.1 · <code>StudentService</code> y <code>CourseService</code></strong></summary>
>
> > Cada servicio recibe sus repositorios **por constructor** y lleva `@Service` para que Spring lo cree. El constructor **lo genera Lombok** con `@RequiredArgsConstructor` (uno por cada atributo `final`).
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
> > import lombok.RequiredArgsConstructor;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
> >
> > import java.util.List;
> > import java.util.Optional;
> >
> > @Service
> > @RequiredArgsConstructor
> > public class StudentService {
> >
> >     private final StudentJpaRepository students;
> >
> >     @Transactional(readOnly = true)
> >     public List<StudentEntity> findAll() {
> >         return students.findAll();
> >     }
> >
> >     @Transactional
> >     public Optional<StudentEntity> create(StudentEntity student) {
> >         if (students.existsByEmailIgnoreCase(student.getEmail())) {
> >             return Optional.empty();
> >         }
> >         return Optional.of(students.save(student));
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
> > import com.academy.persistence.entity.CourseEntity;
> > import com.academy.persistence.repository.CourseJpaRepository;
> > import com.academy.persistence.repository.EnrollmentJpaRepository;
> > import lombok.RequiredArgsConstructor;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
> >
> > import java.util.List;
> > import java.util.Optional;
> >
> > @Service
> > @RequiredArgsConstructor
> > public class CourseService {
> >
> >     private final CourseJpaRepository courses;
> >     private final EnrollmentJpaRepository enrollments;
> >
> >     @Transactional(readOnly = true)
> >     public List<CourseEntity> findAll() {
> >         return courses.findAll();
> >     }
> >
> >     @Transactional
> >     public Optional<CourseEntity> create(CourseEntity course) {
> >         if (course.getPrice() <= 0 || course.getMaxCapacity() <= 0) {
> >             return Optional.empty();
> >         }
> >         return Optional.of(courses.save(course));
> >     }
> >
> >     @Transactional(readOnly = true)
> >     public Optional<Long> countEnrolled(Long courseId) {
> >         if (!courses.existsById(courseId)) {
> >             return Optional.empty();
> >         }
> >         return Optional.of(enrollments.countByCourseId(courseId));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > - **`@Service`** registra la clase como *bean* de la capa de negocio (el mismo mecanismo de inyección del M2).
> > - `create` **valida antes de guardar**: si la regla falla devuelve `Optional.empty()` y no se ejecuta ningún `insert`.
> > - **`@Transactional`** (de `org.springframework.transaction.annotation`) abre una transacción al entrar al método y la cierra al salir: si algo falla, se deshace todo (*rollback*). Va en los **servicios**, nunca en el controlador.
> > - **`@Transactional(readOnly = true)`** en las consultas (`findAll`, `countEnrolled`): indica que no se escribe, y Hibernate evita trabajo innecesario.
> > - `save` devuelve la entidad con su `id` ya generado; se envuelve en `Optional.of(…)`.
> > - `countEnrolled` devuelve vacío cuando el curso no existe, para no confundirlo con «cero inscritos».
>
> </details>
>
>
> <details>
> <summary><strong>Paso 3.2 · <code>EnrollmentService</code>: la regla más completa</strong></summary>
>
> > Matricular junta tres repositorios y **cuatro** comprobaciones, en este orden: existen el estudiante y el curso, no está ya matriculado y hay cupo.
> >
> > <details>
> > <summary><strong>📄 EnrollmentService.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/EnrollmentService.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > import com.academy.persistence.entity.CourseEntity;
> > import com.academy.persistence.entity.EnrollmentEntity;
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.persistence.repository.CourseJpaRepository;
> > import com.academy.persistence.repository.EnrollmentJpaRepository;
> > import com.academy.persistence.repository.StudentJpaRepository;
> > import lombok.RequiredArgsConstructor;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
> >
> > import java.util.Optional;
> >
> > @Service
> > @RequiredArgsConstructor
> > public class EnrollmentService {
> >
> >     private final StudentJpaRepository students;
> >     private final CourseJpaRepository courses;
> >     private final EnrollmentJpaRepository enrollments;
> >
> >     @Transactional
> >     public Optional<EnrollmentEntity> enroll(Long studentId, Long courseId) {
> >         Optional<StudentEntity> student = students.findById(studentId);
> >         Optional<CourseEntity> course = courses.findById(courseId);
> >
> >         if (student.isEmpty() || course.isEmpty()) {
> >             return Optional.empty();
> >         }
> >         if (enrollments.existsByStudentIdAndCourseId(studentId, courseId)) {
> >             return Optional.empty();
> >         }
> >         if (enrollments.countByCourseId(courseId) >= course.get().getMaxCapacity()) {
> >             return Optional.empty();
> >         }
> >         return Optional.of(enrollments.save(new EnrollmentEntity(student.get(), course.get())));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > | Línea | Qué hace |
> > |---|---|
> > | `@Transactional` | Todo `enroll` ocurre en **una sola transacción**: lecturas y `insert` van juntos o no va ninguno. |
> > | `students.findById(…)` y `courses.findById(…)` | Devuelven `Optional`: vacío si no existe la fila. |
> > | `existsByStudentIdAndCourseId` | Evita la matrícula duplicada. |
> > | `countByCourseId(courseId) >= course.get().getMaxCapacity()` | Compara los inscritos con el cupo del curso. |
> > | `new EnrollmentEntity(student.get(), course.get())` | Se construye con **objetos**; JPA guarda solo las claves foráneas. |
> >
> > > 🗣️ **Preguntas:** ¿por qué el servicio llama a `existsBy…` y no a `findAll` para ver si ya está matriculado? *(Porque deja a la base filtrar; traer todas las filas a memoria no escala.)*
> > > ¿Qué se pierde con un `Optional` vacío? *(No se sabe **por qué** falló: no existe, está repetido o el curso está lleno. Las excepciones propias resuelven eso en una guía posterior.)*
> >
> > ✅ **Punto de control 3:** el proyecto compila. Aún no se puede probar: falta quien reciba las peticiones (Paso 4).
>
> </details>
>
>
</details>

---

<details>
<summary><strong>🌐 Paso 4 — Capa de presentación (<code>presentation.controller</code>)</strong></summary>

> El **controlador** recibe la petición HTTP, **delega** en el servicio y devuelve la respuesta. No contiene reglas del negocio ni habla con los repositorios.
> Aquí se decide el **código HTTP** a partir del `Optional` que devuelve el servicio.
>
> | Método | Ruta | Qué hace | Respuesta |
> |---|---|---|---|
> | `GET` | `/api/students` | Lista los estudiantes | `200` |
> | `POST` | `/api/students` | Crea un estudiante (JSON) | `201` · `409` si el correo existe |
> | `GET` | `/api/courses` | Lista los cursos | `200` |
> | `POST` | `/api/courses` | Crea un curso (JSON) | `201` · `409` si precio o cupo no son válidos |
> | `GET` | `/api/courses/{id}/enrolled` | Cuántos inscritos tiene el curso | `200` · `404` si el curso no existe |
> | `POST` | `/api/enrollments?studentId=…&courseId=…` | Matricula a un estudiante | `201` · `409` si no se puede matricular |
>
> <details>
> <summary><strong>Paso 4.1 · Los controladores</strong></summary>
>
> > Cada controlador lleva `@RestController` y `@RequestMapping` con su ruta base, y recibe el servicio **por constructor** (generado con `@RequiredArgsConstructor`).
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
> > import lombok.RequiredArgsConstructor;
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
> > @RequiredArgsConstructor
> > public class StudentController {
> >
> >     private final StudentService service;
> >
> >     @GetMapping
> >     public List<StudentEntity> findAll() {
> >         return service.findAll();
> >     }
> >
> >     @PostMapping
> >     public ResponseEntity<StudentEntity> create(@RequestBody StudentEntity student) {
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
> > import com.academy.business.service.CourseService;
> > import com.academy.persistence.entity.CourseEntity;
> > import lombok.RequiredArgsConstructor;
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
> > @RequiredArgsConstructor
> > public class CourseController {
> >
> >     private final CourseService service;
> >
> >     @GetMapping
> >     public List<CourseEntity> findAll() {
> >         return service.findAll();
> >     }
> >
> >     @PostMapping
> >     public ResponseEntity<CourseEntity> create(@RequestBody CourseEntity course) {
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
> > import com.academy.business.service.EnrollmentService;
> > import com.academy.persistence.entity.EnrollmentEntity;
> > import lombok.RequiredArgsConstructor;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.http.ResponseEntity;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.RequestMapping;
> > import org.springframework.web.bind.annotation.RequestParam;
> > import org.springframework.web.bind.annotation.RestController;
> >
> > @RestController
> > @RequestMapping("/api/enrollments")
> > @RequiredArgsConstructor
> > public class EnrollmentController {
> >
> >     private final EnrollmentService service;
> >
> >     @PostMapping
> >     public ResponseEntity<EnrollmentEntity> enroll(@RequestParam Long studentId, @RequestParam Long courseId) {
> >         return service.enroll(studentId, courseId)
> >                 .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
> >                 .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
> >     }
> > }
> > ```
> >
> > </details>
> >
> > | Anotación / clase | Para qué sirve |
> > |---|---|
> > | `@RestController` | La clase atiende HTTP y devuelve **JSON** (los objetos se convierten solos). |
> > | `@RequestMapping("/api/students")` | Ruta base de todos los métodos de la clase. |
> > | `@GetMapping` / `@PostMapping` | Método HTTP que atiende cada método Java. |
> > | `@RequestBody` | Convierte el JSON de la petición en un objeto. |
> > | `@PathVariable` / `@RequestParam` | Toma un dato de la ruta (`/{id}`) o de la consulta (`?studentId=1`). |
> > | `ResponseEntity` | Permite elegir el **código HTTP** y el cuerpo de la respuesta. |
> > | `.map(…).orElse(…)` | Si el `Optional` trae valor responde `201`/`200`; si está vacío, responde `409` o `404`. |
> >
> > > ⚠️ **Simplificación del taller.** Los controladores reciben y devuelven las **entidades** directamente. En un proyecto real se usan objetos de transferencia (DTO) y mapeadores para no exponer la tabla: eso se resuelve en otra guía.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 4.2 · Arrancar y probar con <code>curl</code></strong></summary>
>
> > Se arranca la aplicación (`./mvnw spring-boot:run`) y, desde otra terminal, se prueba cada regla. **Resultados reales** (MySQL 8):
> >
> > ```bash
> > # 1. Crear estudiantes
> > curl -i -X POST localhost:8080/api/students -H "Content-Type: application/json" \
> >      -d '{"name":"Ana Torres","age":22,"email":"ana@mail.com"}'
> > curl -i -X POST localhost:8080/api/students -H "Content-Type: application/json" \
> >      -d '{"name":"Luis Pérez","age":25,"email":"luis@mail.com"}'
> >
> > # 2. Crear un curso con cupo de 1
> > curl -i -X POST localhost:8080/api/courses -H "Content-Type: application/json" \
> >      -d '{"name":"Java from scratch","price":150000,"maxCapacity":1}'
> >
> > # 3. Matricular
> > curl -i -X POST "localhost:8080/api/enrollments?studentId=1&courseId=1"
> > ```
> >
> > | Petición | Respuesta real | Regla que se comprueba |
> > |---|---|---|
> > | `POST /api/students` (Ana) | `201` · `{"name":"Ana Torres","age":22,"email":"ana@mail.com","id":1}` | Crear |
> > | `POST /api/students` con `ANA@mail.com` | `409` · sin cuerpo | Correo único, sin importar mayúsculas |
> > | `POST /api/courses` con precio `0` | `409` · sin cuerpo | Precio y cupo válidos |
> > | `POST /api/enrollments?studentId=1&courseId=1` | `201` · la matrícula, con los datos del estudiante y del curso | Matricular |
> > | La misma matrícula otra vez | `409` · sin cuerpo | Sin duplicados |
> > | `studentId=2&courseId=1` (cupo de 1) | `409` · sin cuerpo | Cupo |
> > | `studentId=9&courseId=1` | `409` · sin cuerpo | Estudiante inexistente |
> > | `GET /api/courses/1/enrolled` | `200` · `1` | Conteo (`countByCourseId`) |
> > | `GET /api/courses/9/enrolled` | `404` · sin cuerpo | Curso inexistente |
> >
> > > 🗣️ **Preguntas:** ¿qué capa decidió que el segundo correo era un `409`? *(El servicio detectó la regla con un `Optional` vacío; el controlador la tradujo a `409`.)*
> > > ¿Por qué tres casos distintos devuelven el mismo `409`? *(El `Optional` vacío no dice el motivo; las excepciones propias permitirán distinguirlos.)*
> > > ¿Dónde se ve el SQL de cada petición? *(En la consola, por `show-sql=true`: aparece el `select … where upper(email)=upper(?) limit ?` del Paso 2.)*
> >
> > ✅ **Punto de control 4:** las nueve peticiones responden lo de la tabla.
>
> </details>

</details>

---

<details>
<summary><strong>💾 Paso 5 — Los datos sobreviven al reinicio (MySQL)</strong></summary>

> La aplicación **no necesita cambiar nada** para que los datos persistan: la base vive en el **servidor**, fuera de la JVM, y conserva lo guardado aunque la aplicación se detenga.
>
> **1. Guardar un estudiante por la API** (si ya se hizo en el Paso 4.2, se omite):
>
> ```bash
> curl -X POST localhost:8080/api/students -H "Content-Type: application/json" \
>      -d '{"name":"Ana Torres","age":22,"email":"ana@mail.com"}'
> ```
>
> **2. Reiniciar la aplicación** (`Ctrl+C` y volver a ejecutar) y comprobar:
>
> ```text
> a) Primer arranque     → aparecen los create table y las restricciones (alter table …)
> b) Se detiene la aplicación (Ctrl+C)                  → las tablas siguen en MySQL
> c) Segundo arranque    → NO aparece ningún  create table  ni  alter table   (solo  Started AcademyApplication)
> d) curl localhost:8080/api/students                   → el estudiante sigue ahí
> ```
>
> **3. Ver que la fila sigue ahí:**
>
> ```bash
> docker exec academy-mysql mysql -uroot -proot academy -e "show tables; select * from student"
> ```
>
> ```text
> Tables_in_academia
> course
> enrollment
> student
>
> id  age  email          name
> 1   22   ana@mail.com   Ana Torres
> ```
>
> | Pregunta | Respuesta |
> |---|---|
> | ¿Se crean de nuevo las tablas al reiniciar? | Sí se vuelven a crear: con `ddl-auto=create` Hibernate borra y recrea las tablas en **cada arranque** (para conservar los datos se usa `update`). |
> | ¿Qué pasaría con `ddl-auto=update`? | Conservaría los datos y solo agregaría lo que falte; es lo habitual fuera del taller ([tabla de valores en el Ejemplo 07 del M3](../../03-introduccion-a-jpa/material/ejemplos/07-uso-basico-de-spring-data-jpa.md)). |
> | ¿Cómo se empieza de cero? | `docker exec academy-mysql mysql -uroot -proot -e "drop database academia; create database academia"`. |
> | ¿Y si se borra el contenedor? | Se pierden los datos, porque viven dentro de él. Para conservarlos se monta un volumen (`-v academy-data:/var/lib/mysql`). |
>
> > 💡 **Y con otro motor:** si se cambia a PostgreSQL u otro, cambian el **driver** (dependencia) y las propiedades `spring.datasource.*`; las entidades y los repositorios
> > **no se tocan**. Es uno de los retos finales.

</details>

---

<details>
<summary><strong>🏁 Cierre — preguntas de síntesis</strong></summary>

> 1. ¿Qué escribió el equipo y qué generó Hibernate? *(El equipo escribió entidades, repositorios, servicios y controladores; Hibernate generó las tablas y todo el SQL.)*
> 2. ¿Quién escribe la implementación de `StudentJpaRepository`? *(Nadie: Spring Data la genera al arrancar.)*
> 3. ¿Por qué `EnrollmentEntity` es el lado dueño? *(Tiene las dos `@JoinColumn`: las claves foráneas viven en la tabla `enrollment`.)*
> 4. ¿Quién decide que un correo repetido es un error y quién lo convierte en `409`? *(El servicio detecta la regla y devuelve un `Optional` vacío; el controlador lo traduce a `409`.)*
> 5. ¿Por qué el controlador no llama directamente al repositorio? *(Para que las reglas vivan en un solo lugar y se puedan reutilizar sin HTTP.)*
> 6. ¿Quién crea la base `academia` y quién crea las tablas? *(La base la crea MySQL gracias a `createDatabaseIfNotExist=true`; las tablas las crea Hibernate al arrancar.)*

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Solución |
> |---|---|---|
> | `Failed to configure a DataSource: 'url' attribute is not specified` | `application.properties` no se encuentra | Revisar que esté en `src/main/resources` |
> | `Communications link failure` / `Connection refused` | MySQL no está arrancado o el puerto es otro | `docker ps`; arrancar el contenedor; revisar el puerto en la URL |
> | `Unknown database 'academia'` | Falta `createDatabaseIfNotExist=true` en la URL | Agregarlo a `spring.datasource.url` o ejecutar `create database academia` |
> | `Access denied for user 'root'@…` | Usuario o contraseña distintos de los del servidor | Corregir `spring.datasource.username` / `password` |
> | `No suitable driver found for jdbc:mysql…` | Falta la dependencia `mysql-connector-j` | Agregarla al `pom.xml` |
> | La tabla se llama `student_entity` | Falta `@Table(name = "student")` en la entidad | Agregar `@Table` |
> | `Not a managed type: class …StudentEntity` | Falta `@Entity`, o la clase quedó **fuera** de `com.academy` (el escaneo no la ve) | Anotar la clase y dejarla bajo el paquete de `AcademyApplication` |
> | `cannot find symbol … getEmail()` al compilar | Lombok **no se está ejecutando**: falta la dependencia, o con Java 23+ falta `annotationProcessorPaths` en el `pom.xml` | Agregar la configuración del pom final; en el IDE, activar el plugin de Lombok y «annotation processing» |
> | `No default constructor for entity` | La entidad no tiene constructor **sin argumentos** | Agregar `@NoArgsConstructor(access = AccessLevel.PROTECTED)` a la entidad |
> | `PropertyReferenceException: No property 'x' found for type 'StudentEntity'` | El nombre del método derivado no coincide con un atributo de la **entidad** | Usar el nombre exacto: `existsByEmailIgnoreCase`, `countByCourseId` |
> | `Table 'academia.student' doesn't exist` | `ddl-auto=none`, o la URL apunta a otra base | Usar `create` y revisar el nombre de la base en la URL |
> | `DataIntegrityViolationException` al guardar un estudiante | El `email` ya existe y la comprobación del servicio no se ejecutó (se usó el repositorio directo) | Guardar siempre a través de `StudentService`, o usar otro correo |
> | Los datos desaparecen al reiniciar | `ddl-auto=create` o `create-drop` (es lo esperado en el taller) | Usar `update` si se quieren conservar |
> | `Parameter 0 of constructor in …Service required a bean of type …Repository` | El repositorio no está bajo `com.academy`, o no es una interfaz que extienda `JpaRepository` | Revisar el paquete y la herencia |
> | `No qualifying bean` para un servicio | Falta `@Service` en la clase | Agregar `@Service` |
> | `404` en una ruta que sí existe | El controlador no tiene `@RestController`, o la ruta de `@RequestMapping` no coincide | Revisar anotaciones y la ruta |
> | `415 Unsupported Media Type` al hacer `POST` | Falta el encabezado `Content-Type: application/json` | Agregar `-H "Content-Type: application/json"` |
> | `400 Bad Request` con un JSON válido | Un nombre del JSON no coincide con un atributo de la entidad (`maxCapacity`, no `max_capacity`) | Usar los nombres de los atributos |
> | `Public Key Retrieval is not allowed` | MySQL 8 con un usuario que no es `root` y conexión sin SSL | Agregar `?allowPublicKeyRetrieval=true&useSSL=false` a la URL (solo en desarrollo) |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **Restricción en la base:** en `EnrollmentEntity`, agregar `@Table(name = "enrollment", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))`
>    y comprobar que guardar dos veces la misma matrícula lanza `DataIntegrityViolationException`. ¿Para qué sirve si el código ya puede preguntar con `existsByStudentIdAndCourseId`?
>    *(Dos peticiones simultáneas podrían pasar ambas la pregunta; la restricción es la última defensa.)*
> 2. **Filtro de punta a punta:** agregar `findByPriceLessThan(double price)` en `CourseJpaRepository`, un método en `CourseService` y `GET /api/courses?maxPrice=200000` en `CourseController`; probarlo con `curl`. Probarlo con datos queda para la capa de servicio.
> 3. **Consulta con JPQL:** reescribir `countByCourseId` con `@Query("SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.course.id = :courseId")`.
> 4. **Otro motor:** reemplazar MySQL por **PostgreSQL** (dependencia del driver + `spring.datasource.*`) y comprobar que **ninguna** entidad ni repositorio cambia.
> 5. **Excepciones propias:** reemplazar los `Optional` vacíos por excepciones (`NotFoundException`, `BusinessRuleException`) con mensaje, y un `@RestControllerAdvice` que las traduzca a `404`/`409` con cuerpo JSON. ¿Qué información se gana?
> 6. **Desmatricular:** agregar `DELETE /api/enrollments/{id}` (`404` si no existe) pasando por el servicio.
> 7. **Relación bidireccional:** agregar `List<EnrollmentEntity>` en `CourseEntity` con `@OneToMany(mappedBy = "course")` y explicar qué cambia en la tabla y en el código
>    ([Ejemplo 03 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/03-oneToMany-manyToOne-y-fetch.md) y [Guía 00](00-guia-relaciones-entre-entidades-jpa.md)).

</details>

<details>
<summary><strong>📁 Estructura final esperada</strong></summary>

> ```text
> 📁 academy
> ├── 📄 pom.xml
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 presentation
>     │   │   └── 📁 controller
>     │   │       ├── 📄 StudentController.java
>     │   │       ├── 📄 CourseController.java
>     │   │       └── 📄 EnrollmentController.java
>     │   ├── 📁 business
>     │   │   └── 📁 service
>     │   │       ├── 📄 StudentService.java
>     │   │       ├── 📄 CourseService.java
>     │   │       └── 📄 EnrollmentService.java
>     │   └── 📁 persistence
>     │       ├── 📁 repository
>     │       │   ├── 📄 StudentJpaRepository.java
>     │       │   ├── 📄 CourseJpaRepository.java
>     │       │   └── 📄 EnrollmentJpaRepository.java
>     │       └── 📁 entity
>     │           ├── 📄 StudentEntity.java
>     │           ├── 📄 CourseEntity.java
>     │           └── 📄 EnrollmentEntity.java
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
>     <artifactId>academy</artifactId>
>     <version>0.0.1-SNAPSHOT</version>
>     <name>academy</name>
>     <description>Taller de gestión de cursos con base de datos</description>
>     <properties>
>         <java.version>21</java.version>
>     </properties>
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
>             <groupId>com.mysql</groupId>
>             <artifactId>mysql-connector-j</artifactId>
>             <scope>runtime</scope>
>         </dependency>
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-devtools</artifactId>
>             <scope>runtime</scope>
>             <optional>true</optional>
>         </dependency>
>     </dependencies>
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.apache.maven.plugins</groupId>
>                 <artifactId>maven-compiler-plugin</artifactId>
>                 <configuration>
>                     <annotationProcessorPaths>
>                         <path>
>                             <groupId>org.projectlombok</groupId>
>                             <artifactId>lombok</artifactId>
>                             <version>${lombok.version}</version>
>                         </path>
>                     </annotationProcessorPaths>
>                 </configuration>
>             </plugin>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>                 <configuration>
>                     <excludes>
>                         <exclude>
>                             <groupId>org.projectlombok</groupId>
>                             <artifactId>lombok</artifactId>
>                         </exclude>
>                     </excludes>
>                 </configuration>
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
> <summary><strong>📄 StudentEntity.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/entity/StudentEntity.java`
>
> ```java
> package com.academy.persistence.entity;
>
> import jakarta.persistence.Column;
> import jakarta.persistence.Entity;
> import jakarta.persistence.GeneratedValue;
> import jakarta.persistence.GenerationType;
> import jakarta.persistence.Id;
> import jakarta.persistence.Table;
> import lombok.AccessLevel;
> import lombok.Getter;
> import lombok.NoArgsConstructor;
>
> @Entity
> @Table(name = "student")
> @Getter
> @NoArgsConstructor(access = AccessLevel.PROTECTED)
> public class StudentEntity {
>
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>
>     private String name;
>
>     private int age;
>
>     @Column(unique = true)
>     private String email;
>
>     public StudentEntity(String name, int age, String email) {
>         this.name = name;
>         this.age = age;
>         this.email = email;
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 CourseEntity.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/entity/CourseEntity.java`
>
> ```java
> package com.academy.persistence.entity;
>
> import jakarta.persistence.Entity;
> import jakarta.persistence.GeneratedValue;
> import jakarta.persistence.GenerationType;
> import jakarta.persistence.Id;
> import jakarta.persistence.Table;
> import lombok.AccessLevel;
> import lombok.Getter;
> import lombok.NoArgsConstructor;
>
> @Entity
> @Table(name = "course")
> @Getter
> @NoArgsConstructor(access = AccessLevel.PROTECTED)
> public class CourseEntity {
>
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>
>     private String name;
>
>     private double price;
>
>     private int maxCapacity;
>
>     public CourseEntity(String name, double price, int maxCapacity) {
>         this.name = name;
>         this.price = price;
>         this.maxCapacity = maxCapacity;
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 EnrollmentEntity.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/entity/EnrollmentEntity.java`
>
> ```java
> package com.academy.persistence.entity;
>
> import jakarta.persistence.Entity;
> import jakarta.persistence.GeneratedValue;
> import jakarta.persistence.GenerationType;
> import jakarta.persistence.Id;
> import jakarta.persistence.JoinColumn;
> import jakarta.persistence.ManyToOne;
> import jakarta.persistence.Table;
> import lombok.AccessLevel;
> import lombok.Getter;
> import lombok.NoArgsConstructor;
>
> @Entity
> @Table(name = "enrollment")
> @Getter
> @NoArgsConstructor(access = AccessLevel.PROTECTED)
> public class EnrollmentEntity {
>
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>
>     @ManyToOne
>     @JoinColumn(name = "student_id")
>     private StudentEntity student;
>
>     @ManyToOne
>     @JoinColumn(name = "course_id")
>     private CourseEntity course;
>
>     public EnrollmentEntity(StudentEntity student, CourseEntity course) {
>         this.student = student;
>         this.course = course;
>     }
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
> import org.springframework.data.jpa.repository.JpaRepository;
>
> public interface StudentJpaRepository extends JpaRepository<StudentEntity, Long> {
>
>     boolean existsByEmailIgnoreCase(String email);
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 CourseJpaRepository.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/repository/CourseJpaRepository.java`
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
> </details>
>
> <details>
> <summary><strong>📄 EnrollmentJpaRepository.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/repository/EnrollmentJpaRepository.java`
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
> </details>
>
> ---
>
> ### 📁 business
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
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.persistence.repository.StudentJpaRepository;
> import lombok.RequiredArgsConstructor;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
>
> import java.util.List;
> import java.util.Optional;
>
> @Service
> @RequiredArgsConstructor
> public class StudentService {
>
>     private final StudentJpaRepository students;
>
>     @Transactional(readOnly = true)
>     public List<StudentEntity> findAll() {
>         return students.findAll();
>     }
>
>     @Transactional
>     public Optional<StudentEntity> create(StudentEntity student) {
>         if (students.existsByEmailIgnoreCase(student.getEmail())) {
>             return Optional.empty();
>         }
>         return Optional.of(students.save(student));
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 CourseService.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/business/service/CourseService.java`
>
> ```java
> package com.academy.business.service;
>
> import com.academy.persistence.entity.CourseEntity;
> import com.academy.persistence.repository.CourseJpaRepository;
> import com.academy.persistence.repository.EnrollmentJpaRepository;
> import lombok.RequiredArgsConstructor;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
>
> import java.util.List;
> import java.util.Optional;
>
> @Service
> @RequiredArgsConstructor
> public class CourseService {
>
>     private final CourseJpaRepository courses;
>     private final EnrollmentJpaRepository enrollments;
>
>     @Transactional(readOnly = true)
>     public List<CourseEntity> findAll() {
>         return courses.findAll();
>     }
>
>     @Transactional
>     public Optional<CourseEntity> create(CourseEntity course) {
>         if (course.getPrice() <= 0 || course.getMaxCapacity() <= 0) {
>             return Optional.empty();
>         }
>         return Optional.of(courses.save(course));
>     }
>
>     @Transactional(readOnly = true)
>     public Optional<Long> countEnrolled(Long courseId) {
>         if (!courses.existsById(courseId)) {
>             return Optional.empty();
>         }
>         return Optional.of(enrollments.countByCourseId(courseId));
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 EnrollmentService.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/business/service/EnrollmentService.java`
>
> ```java
> package com.academy.business.service;
>
> import com.academy.persistence.entity.CourseEntity;
> import com.academy.persistence.entity.EnrollmentEntity;
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.persistence.repository.CourseJpaRepository;
> import com.academy.persistence.repository.EnrollmentJpaRepository;
> import com.academy.persistence.repository.StudentJpaRepository;
> import lombok.RequiredArgsConstructor;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
>
> import java.util.Optional;
>
> @Service
> @RequiredArgsConstructor
> public class EnrollmentService {
>
>     private final StudentJpaRepository students;
>     private final CourseJpaRepository courses;
>     private final EnrollmentJpaRepository enrollments;
>
>     @Transactional
>     public Optional<EnrollmentEntity> enroll(Long studentId, Long courseId) {
>         Optional<StudentEntity> student = students.findById(studentId);
>         Optional<CourseEntity> course = courses.findById(courseId);
>
>         if (student.isEmpty() || course.isEmpty()) {
>             return Optional.empty();
>         }
>         if (enrollments.existsByStudentIdAndCourseId(studentId, courseId)) {
>             return Optional.empty();
>         }
>         if (enrollments.countByCourseId(courseId) >= course.get().getMaxCapacity()) {
>             return Optional.empty();
>         }
>         return Optional.of(enrollments.save(new EnrollmentEntity(student.get(), course.get())));
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
> import com.academy.business.service.StudentService;
> import com.academy.persistence.entity.StudentEntity;
> import lombok.RequiredArgsConstructor;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.RequestBody;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
>
> import java.util.List;
>
> @RestController
> @RequestMapping("/api/students")
> @RequiredArgsConstructor
> public class StudentController {
>
>     private final StudentService service;
>
>     @GetMapping
>     public List<StudentEntity> findAll() {
>         return service.findAll();
>     }
>
>     @PostMapping
>     public ResponseEntity<StudentEntity> create(@RequestBody StudentEntity student) {
>         return service.create(student)
>                 .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
>                 .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 CourseController.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/controller/CourseController.java`
>
> ```java
> package com.academy.presentation.controller;
>
> import com.academy.business.service.CourseService;
> import com.academy.persistence.entity.CourseEntity;
> import lombok.RequiredArgsConstructor;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PathVariable;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.RequestBody;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
>
> import java.util.List;
>
> @RestController
> @RequestMapping("/api/courses")
> @RequiredArgsConstructor
> public class CourseController {
>
>     private final CourseService service;
>
>     @GetMapping
>     public List<CourseEntity> findAll() {
>         return service.findAll();
>     }
>
>     @PostMapping
>     public ResponseEntity<CourseEntity> create(@RequestBody CourseEntity course) {
>         return service.create(course)
>                 .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
>                 .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
>     }
>
>     @GetMapping("/{id}/enrolled")
>     public ResponseEntity<Long> countEnrolled(@PathVariable Long id) {
>         return service.countEnrolled(id)
>                 .map(ResponseEntity::ok)
>                 .orElse(ResponseEntity.notFound().build());
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 EnrollmentController.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/controller/EnrollmentController.java`
>
> ```java
> package com.academy.presentation.controller;
>
> import com.academy.business.service.EnrollmentService;
> import com.academy.persistence.entity.EnrollmentEntity;
> import lombok.RequiredArgsConstructor;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RequestParam;
> import org.springframework.web.bind.annotation.RestController;
>
> @RestController
> @RequestMapping("/api/enrollments")
> @RequiredArgsConstructor
> public class EnrollmentController {
>
>     private final EnrollmentService service;
>
>     @PostMapping
>     public ResponseEntity<EnrollmentEntity> enroll(@RequestParam Long studentId, @RequestParam Long courseId) {
>         return service.enroll(studentId, courseId)
>                 .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
>                 .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
>     }
> }
> ```
>
> </details>

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
> | `@Service` | `StudentService`, `CourseService`, `EnrollmentService` | Son la capa de negocio. | Registra la clase como *bean* para inyectarla. |
> | `@Transactional` · `@Transactional(readOnly = true)` | Métodos de los servicios (`create`, `enroll` · `findAll`, `countEnrolled`) | Una operación del negocio debe cumplirse completa o no cumplirse. | Abre y cierra la transacción; `readOnly` marca las consultas. |
> | `@RestController` | Los tres controladores | Atienden HTTP y devuelven JSON. | Combina `@Controller` y `@ResponseBody`. |
> | `@RequestMapping` · `@GetMapping` · `@PostMapping` | Controladores | Cada método atiende una ruta y un verbo. | Enlaza la petición HTTP con el método Java. |
> | `@RequestBody` · `@PathVariable` · `@RequestParam` | Parámetros de los controladores | Cada dato llega de un sitio distinto. | Cuerpo JSON, parte de la ruta y parámetro de consulta. |
> | `ResponseEntity` *(clase, no anotación)* | Retorno de `create`, `enroll` y `countEnrolled` | La respuesta cambia según el caso. | Fija el código HTTP (`201`, `404`, `409`) y el cuerpo. |
> | `@Getter` · `@NoArgsConstructor(access = PROTECTED)` *(Lombok)* | Las tres entidades | Evitar escribir getters y el constructor vacío. | Generan los `getX()` y un constructor vacío `protected` al compilar. |
> | `@RequiredArgsConstructor` *(Lombok)* | Servicios y controladores | Reciben sus dependencias por constructor. | Genera el constructor con los atributos `final`. |
> | `@SpringBootApplication` | `AcademyApplication` | Es la clase que arranca Spring. | Activa el escaneo de entidades y repositorios. |
>
> ```text
> persistence   ──▶  @Entity · @Table · @Id · @GeneratedValue · @Column · @ManyToOne · @JoinColumn · @Getter · @NoArgsConstructor     (en persistence.entity)
>                    (las …JpaRepository no llevan anotaciones)
> business      ──▶  @Service · @Transactional · @RequiredArgsConstructor                          (en business.service)
> presentation  ──▶  @RestController · @RequestMapping · @GetMapping · @PostMapping · @RequiredArgsConstructor
> arranque      ──▶  @SpringBootApplication
> ```
>
> > 🔎 **Lectura clave.** Las anotaciones de JPA viven **solo** en `persistence.entity`; las de Spring Web, **solo** en `presentation.controller`. Más ejemplos de cada una: [Anotaciones en JPA](../../03-introduccion-a-jpa/material/ejemplos/08-anotaciones-en-jpa.md).

</details>
