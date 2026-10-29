# 🗄️ Guía paso a paso — Taller «Gestión de cursos» con base de datos (JPA)

> 👩‍🏫 **Material docente.** Guía para resolver **junto con los estudiantes**, en vivo, la
> versión con base de datos del taller «Gestión de cursos». Parte del taller ya terminado en la
> [guía de los módulos 01 y 02](../modulo-01-02/01-guia-taller-gestion-de-cursos.md) y aplica lo visto
> en el [Módulo 03](../../03-introduccion-a-jpa/) y el [Módulo 04](../../04-gestion-de-bases-de-datos-mediante-jpa/).
> Contiene código completo: no se entrega a los estudiantes antes de resolver el taller.
>
> ✅ **Verificada.** El código de esta guía se compiló y se ejecutó con Java 21+, Spring Boot 3.3 y
> H2: los escenarios, las respuestas, el SQL y los errores que se muestran son salidas reales.
>
> 🧭 **Idea central.** El **modelo del negocio no se toca**. Las clases que se guardan en la base
> (`…Entity`) viven en la capa de persistencia y el repositorio **traduce** entre ellas y el modelo.

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Migrar la API REST de **estudiantes, cursos y matrículas** de listas en memoria a una
> **base de datos relacional** con **Spring Data JPA**, manteniendo las **tres capas**, **las mismas reglas de
> negocio** y **sin modificar** el modelo, los servicios ni los controladores.
>
> Al terminar, el estudiante:
>
> - Declara **entidades JPA** (`@Entity`, `@Id`, `@GeneratedValue`, `@Column`, `@ManyToOne`) en `persistence.entity`,
>   separadas del modelo del negocio (M3 y M4).
> - Configura la conexión a **H2** en `application.properties` y lee el SQL que genera Hibernate (M3 y M4).
> - Crea interfaces `JpaRepository` con **métodos derivados** (`existsBy…`, `countBy…`) (M3).
> - Escribe **mapeadores** entre entidad y modelo y los usa en una implementación `…EnBD` del contrato de repositorio (M2 y M3).
> - Comprueba qué capas **no** cambiaron al cambiar el almacenamiento y por qué (programar contra interfaces).
> - Verifica que los datos **sobreviven al reinicio** usando H2 en archivo.

</details>

<details>
<summary><strong>📝 Introducción: el problema y qué cambia</strong></summary>

> > 🗣️ **Para abrir la sesión.** En la versión anterior, reiniciar la aplicación borraba todo, y al cerrar
> > se preguntó *«si cambiamos la lista por una base de datos, ¿qué capas hay que tocar?»*. Hoy se **comprueba la
> > respuesta con código**: solo la de persistencia.
>
> ### 🏫 El problema (el mismo)
>
> Una **academia de programación** necesita una API REST para administrar **estudiantes**, **cursos** y
> **matrículas**. Los requerimientos funcionales (RF-01 a RF-08) y las reglas de negocio (RN-01 a RN-11) **son los
> de la [guía anterior](../modulo-01-02/01-guia-taller-gestion-de-cursos.md)**; no se agregan ni se quitan.
>
> ### 🧩 Dos clases por concepto: modelo y entidad
>
> | Concepto | Modelo (capa de negocio) | Entidad (capa de persistencia) | Tabla |
> |---|---|---|---|
> | Estudiante | `Estudiante` — **sin cambios** | `EstudianteEntity` | `estudiante` |
> | Curso | `Curso` — **sin cambios** | `CursoEntity` | `curso` |
> | Matrícula | `Matricula` — **sin cambios** | `MatriculaEntity` | `matricula` (FK `estudiante_id` y `curso_id`) |
> | Ocupación (reporte) | `Ocupacion` — **sin cambios** | *(no se guarda)* | — |
>
> El modelo describe **qué es** un estudiante para el negocio; la entidad describe **cómo se guarda** en una tabla.
> `MatriculaEntity` es la **entidad intermedia** de la relación muchos a muchos entre `EstudianteEntity` y
> `CursoEntity` ([Ejemplo 04 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/04-manyToMany-y-entidad-intermedia.md)): cada una de sus dos relaciones es un `@ManyToOne`.
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
> | Pieza | ¿Cambia? | Detalle |
> |---|:-:|---|
> | `presentacion.controlador` (3 controladores y `SolicitudMatricula`) | ❌ No | No conocen cómo se guardan los datos. |
> | `negocio.model` (`Estudiante`, `Curso`, `Matricula`, `Ocupacion`) | ❌ No | Siguen siendo clases Java sencillas, **sin anotaciones de JPA**. |
> | `negocio.service` (3 servicios) | ❌ No | **Ni una línea**: dependen de las interfaces de repositorio, que no cambian. |
> | `persistence.repository` — las 3 **interfaces** (el contrato) | ❌ No | `guardar`, `listar`, `buscarPorId`… igual que antes. |
> | `persistence.repository` — las 3 clases `…EnMemoria` | 🗑️ Se borran | Las reemplazan las clases `…EnBD`. |
> | `persistence.entity` | ➕ Nuevo | `EstudianteEntity`, `CursoEntity`, `MatriculaEntity` (las `@Entity`). |
> | `persistence.mapper` | ➕ Nuevo | Convierten **entidad ↔ modelo**. |
> | `persistence.repository` — `…JpaRepository` y `…EnBD` | ➕ Nuevo | Las interfaces de Spring Data y las clases que cumplen el contrato usándolas. |
> | `pom.xml` y `application.properties` | ✅ Sí | Dependencias `spring-boot-starter-data-jpa` y `h2`; conexión a la base. |
>
> > 🔎 **Lectura clave.** Esta es la respuesta a la pregunta del cierre anterior: **solo cambió `persistence`**. Es posible
> > porque el servicio depende de una **interfaz** (M2) y porque el modelo no está atado a JPA.
>
> <details>
> <summary><strong>⚖️ ¿Por qué separar entidad y modelo? Qué se gana y qué cuesta</strong></summary>
>
> > | Se gana | Cuesta |
> > |---|---|
> > | `negocio` **no importa nada de JPA**: las reglas se pueden probar sin base de datos. | **Más clases**: 3 entidades y 3 mapeadores. |
> > | Cambiar de tecnología de persistencia (JPA, JDBC, MongoDB…) solo toca `persistence`. | Los atributos están **duplicados** (modelo y entidad) y hay que mantenerlos alineados. |
> > | La regla de dependencia se cumple de verdad: `negocio` no conoce `persistence`. | Hay que **escribir el mapeo** (a mano aquí; en proyectos grandes se usan librerías como MapStruct). |
> > | **Desaparece una trampa:** el cliente ya no puede sobrescribir un registro enviando un `id` (ver Paso 4.2). | Si el modelo crece (relaciones bidireccionales), el mapeo se complica. |
> >
> > En un proyecto pequeño, anotar el modelo directamente es válido (es lo que hacen los ejemplos de los módulos 3 y 4).
> > Esta guía elige la separación porque **refleja la arquitectura del curso** y enseña a diseñar capas independientes.
>
> </details>
>
> ### 🛠️ Requerimientos no funcionales y restricciones
>
> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Java 21 o superior, Spring Boot 3 y Maven. |
> | **RNF-02** | Persistencia con **Spring Data JPA** y **H2**. Primero en memoria; al final, en **archivo**. |
> | **RNF-03** | Se mantienen las **tres capas**: presentación → negocio → persistencia. Las anotaciones de JPA **solo** en `persistence.entity`. |
> | **RNF-04** | Inyección por constructor. Los servicios dependen de **interfaces** de repositorio. |
> | **RNF-05** | Los errores de negocio siguen siendo excepciones estándar de Java; sin manejador, Spring responde `500` (Módulo 7). |
> | **RNF-06** | La API se prueba con Postman o `curl`, con evidencia de cada escenario. |
> | **RNF-07** | Duración: 1 h 30 min. |
>
> > 💡 **Alcance.** Quedan fuera: relaciones bidireccionales, `cascade`, migraciones (Flyway), otros motores
> > (MySQL/PostgreSQL), DTO y manejo de errores con `404`/`400`/`409`.

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>

> | Paso | Minutos | Actividad | Quién escribe |
> |---|---:|---|---|
> | 0 | 0 – 10 | Preparación: dependencias, `application.properties` y arranque | Docente |
> | 1 | 10 – 25 | Entidades JPA en `persistence.entity` (`EstudianteEntity` en vivo) | Docente y estudiantes |
> | 2 | 25 – 32 | Interfaces de Spring Data (`…JpaRepository`) | Estudiantes |
> | 3 | 32 – 42 | Mapeadores entidad ↔ modelo | Estudiantes (docente guía) |
> | 4 | 42 – 57 | Implementaciones `…EnBD` y retiro de `…EnMemoria` | Estudiantes |
> | 5 | 57 – 62 | Comprobar que modelo, servicios y controladores **no cambiaron** | Todos |
> | 6 | 62 – 80 | Pruebas con `curl`/Postman, SQL en consola y consola de H2 | Todos |
> | 7 | 80 – 90 | Persistencia real (H2 en archivo) y cierre | Todos |
>
> > 💡 **Ritmo.** Los pasos 1 y 3 con `Estudiante` fijan el patrón; con `Curso` y `Matricula` los estudiantes lo repiten.
> > **Servicios y controladores no se abren hasta el Paso 5:** que nadie tenga que tocarlos es parte de la lección. Si falta tiempo, el
> > Paso 7 se hace como demostración del docente.

</details>

<details>
<summary><strong>🧠 Conceptos de los módulos 03 y 04 que se resaltan</strong></summary>

> | Concepto | Dónde aparece en el taller | Módulo | Ejemplo de apoyo |
> |---|---|:-:|---|
> | JPA y ORM | Las clases se guardan **sin escribir SQL** | 03 | [¿Qué es JPA?](../../03-introduccion-a-jpa/material/ejemplos/01-que-es-jpa.md) · [¿Qué es ORM?](../../03-introduccion-a-jpa/material/ejemplos/04-que-es-orm.md) |
> | Arquitectura de JPA e Hibernate | Hibernate genera las tablas y el SQL (visible con `show-sql`) | 03 | [Arquitectura de JPA](../../03-introduccion-a-jpa/material/ejemplos/02-arquitectura-de-jpa.md) · [Hibernate y JPA](../../03-introduccion-a-jpa/material/ejemplos/05-hibernate-y-su-relacion-con-jpa.md) |
> | Relacionamiento entre clases | `MatriculaEntity` apunta a `EstudianteEntity` y a `CursoEntity` | 03 | [Relacionamiento entre clases](../../03-introduccion-a-jpa/material/ejemplos/03-relacionamiento-entre-clases.md) |
> | Spring Data JPA | `JpaRepository` y **métodos derivados** (`existsBy…`, `countBy…`) | 03 | [¿Qué es Spring Data JPA?](../../03-introduccion-a-jpa/material/ejemplos/06-que-es-spring-data-jpa.md) · [Uso básico](../../03-introduccion-a-jpa/material/ejemplos/07-uso-basico-de-spring-data-jpa.md) |
> | Anotaciones de JPA | `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@ManyToOne`, `@JoinColumn` | 03 | [Anotaciones en JPA](../../03-introduccion-a-jpa/material/ejemplos/08-anotaciones-en-jpa.md) |
> | Proyecto y conexión a la base de datos | `pom.xml`, `application.properties`, H2 | 04 | [Creación de un proyecto](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/01-creacion-de-un-proyecto-spring-boot.md) |
> | Lado dueño de la relación y `fetch` | `MatriculaEntity` es dueña de las dos claves foráneas | 04 | [OneToMany/ManyToOne y fetch](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/03-oneToMany-manyToOne-y-fetch.md) |
> | Entidad intermedia | `MatriculaEntity` une estudiante y curso | 04 | [ManyToMany y entidad intermedia](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/04-manyToMany-y-entidad-intermedia.md) |
> | CRUD | `save`, `findAll`, `findById` | 04 | [CRUD completo](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/06-crud-completo.md) |
> | Inicialización del servidor | Hibernate crea las tablas al arrancar (`ddl-auto=update`) | 04 | [Inicialización del servidor](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/07-inicializacion-del-servidor.md) |
> | Programar contra interfaces (repaso M2) | Los servicios **no cambian** porque dependen del contrato | 02 | [Resolución de una dependencia](../../02-manejo-de-dependencias-y-java-beans/material/ejemplos/04-implementacion-y-resolucion-de-una-dependencia-general.md) |
> | Un solo bean por tipo (repaso M2) | Dejar `…EnMemoria` y `…EnBD` a la vez rompe el arranque | 02 | [Explicación conceptual del M2](../../02-manejo-de-dependencias-y-java-beans/material/ejemplos/explicacion-conceptual.md) |
> | **Mapeo entidad ↔ modelo** *(concepto nuevo de esta guía)* | `persistence.mapper` | — | Se explica en el Paso 3 |

</details>

<details>
<summary><strong>🏗️ Arquitectura del taller: el árbol de infraestructura</strong></summary>

> ```text
> 📁 src/main/java/com/academia
> ├── 📄 AcademiaApplication.java                  (sin cambios)
> ├── 📁 presentacion                              ← 1 · sin cambios
> │   └── 📁 controlador   ControladorEstudiantes · ControladorCursos · ControladorMatriculas · SolicitudMatricula
> ├── 📁 negocio                                   ← 2 · sin cambios
> │   ├── 📁 model         Estudiante · Curso · Matricula · Ocupacion
> │   └── 📁 service       ServicioEstudiantes · ServicioCursos · ServicioMatriculas
> └── 📁 persistence                               ← 3 · aquí ocurre todo el cambio
>     ├── 📁 entity        EstudianteEntity · CursoEntity · MatriculaEntity            (➕ las @Entity)
>     ├── 📁 mapper        EstudianteMapper · CursoMapper · MatriculaMapper            (➕ entidad ↔ modelo)
>     └── 📁 repository    Repositorio… (3 interfaces = el contrato, sin cambios)
>                          …JpaRepository (3, ➕ Spring Data) · Repositorio…EnBD (3, ➕ @Repository)
> 📁 src/main/resources
> └── 📄 application.properties                    (➕ conexión a H2 y JPA)
> ```
>
> ```text
> HTTP ─▶ Controlador ─▶ Servicio ─▶ Repositorio (interfaz)  ◀─ RepositorioEnBD ─▶ Mapper ─▶ …JpaRepository ─▶ Hibernate ─▶ H2
>                                    contrato (sin cambios)    cumple el contrato   modelo ↔ entidad   Spring Data lo genera
>         └──────── no se tocan ────────┘                      └─────────────────── persistence (todo lo nuevo) ───────────────────┘
> ```
>
> > 🔎 **Regla de dependencia.** `persistence` **conoce** a `negocio.model` (el repositorio devuelve y recibe modelos),
> > pero `negocio` **no conoce** a `persistence.entity`. Si algún `import com.academia.persistence.entity…` aparece en un
> > servicio o un controlador, la separación se rompió.
> >
> > 🗣️ **Pregunta para el grupo:** ¿quién escribe la clase que implementa `…JpaRepository`? *(Nadie: Spring Data la crea
> > al arrancar y la registra como bean. Lo que sí escribimos es `…EnBD`, que cumple **nuestro** contrato.)*

</details>

<details>
<summary><strong>🗺️ Diagramas de clases: cómo encaja todo</strong></summary>

> ### 1️⃣ La capa de persistencia de «Estudiante», desde el contrato
>
> Arriba está `RepositorioEstudiantes`, la **interfaz con la que interactúa `ServicioEstudiantes`** (es lo único que el servicio conoce). Todo lo que cuelga de ella es persistencia.
>
> ```mermaid
> classDiagram
>     direction TB
>
>     class RepositorioEstudiantes {
>         <<interface>>
>         +guardar(Estudiante)
>         +listar()
>         +buscarPorId(Long)
>         +existePorCorreo(String)
>     }
>     class RepositorioEstudiantesEnBD {
>         @Repository
>     }
>     class EstudianteJpaRepository {
>         <<interface>>
>         +existsByCorreoIgnoreCase(String)
>     }
>     class JpaRepository {
>         <<interface>>
>         Spring Data
>     }
>     class EstudianteMapper {
>         +aEntity(Estudiante)$
>         +aModelo(EstudianteEntity)$
>     }
>     class Estudiante {
>         modelo
>     }
>     class EstudianteEntity {
>         <<Entity>>
>     }
>
>     RepositorioEstudiantes <|.. RepositorioEstudiantesEnBD : cumple el contrato
>     RepositorioEstudiantes ..> Estudiante : recibe y devuelve
>     RepositorioEstudiantesEnBD --> EstudianteJpaRepository : delega
>     RepositorioEstudiantesEnBD ..> EstudianteMapper : traduce con
>     JpaRepository <|-- EstudianteJpaRepository : extiende
>     EstudianteJpaRepository ..> EstudianteEntity : gestiona
>     EstudianteMapper ..> Estudiante
>     EstudianteMapper ..> EstudianteEntity
> ```
>
> | Pieza | Quién la escribe | Qué hace |
> |---|---|---|
> | `RepositorioEstudiantes` | Nosotros (ya existía) | **Contrato** que conoce el servicio. Habla en modelos. |
> | `RepositorioEstudiantesEnBD` | Nosotros | Cumple el contrato: delega en Spring Data y traduce con el mapeador. |
> | `EstudianteJpaRepository` | Nosotros la declaramos; **Spring Data genera la clase** | Guarda y consulta `EstudianteEntity`. |
> | `EstudianteMapper` | Nosotros | Convierte entre `Estudiante` y `EstudianteEntity`. |
>
> > 🔎 **Cómo leerlo.** Arriba, el contrato (habla en **modelos**); abajo, la base (habla en **entidades**). `RepositorioEstudiantesEnBD` es el puente: por eso es la única clase que
> > toca los dos lados. Hacia arriba solo se ve la interfaz: el servicio y el controlador no aparecen porque **no dependen de nada de este diagrama** más que del contrato.
> >
> > 🗣️ **Pregunta:** si se cambiara JPA por JDBC puro, ¿qué clases del diagrama se reemplazan? *(Todas las de abajo del contrato; el contrato y el modelo se quedan.)*
>
> ### 2️⃣ Las relaciones de «Matrícula»: el modelo y la entidad tienen la misma forma
>
> ```mermaid
> classDiagram
>     direction TB
>
>     namespace negocio_model {
>         class Matricula {
>             -Long id
>             -Estudiante estudiante
>             -Curso curso
>         }
>         class Estudiante {
>             -Long id
>             -String nombre
>             -int edad
>             -String correo
>         }
>         class Curso {
>             -Long id
>             -String nombre
>             -double precio
>             -int cupoMaximo
>         }
>     }
>
>     namespace persistence_entity {
>         class MatriculaEntity {
>             <<Entity>>
>             -Long id
>             -EstudianteEntity estudiante
>             -CursoEntity curso
>         }
>         class EstudianteEntity {
>             <<Entity>>
>             -Long id
>             -String nombre
>             -int edad
>             -String correo
>         }
>         class CursoEntity {
>             <<Entity>>
>             -Long id
>             -String nombre
>             -double precio
>             -int cupoMaximo
>         }
>     }
>
>     Matricula --> "1" Estudiante
>     Matricula --> "1" Curso
>     MatriculaEntity "*" --> "1" EstudianteEntity : @ManyToOne estudiante_id
>     MatriculaEntity "*" --> "1" CursoEntity : @ManyToOne curso_id
>
>     Matricula ..> MatriculaEntity : MatriculaMapper
>     Estudiante ..> EstudianteEntity : EstudianteMapper
>     Curso ..> CursoEntity : CursoMapper
> ```
>
> - Arriba, el **modelo**: objetos que se referencian entre sí, sin anotaciones. Abajo, las **entidades**: la misma forma, pero cada flecha es una **clave foránea** (`@ManyToOne` + `@JoinColumn`).
> - Las líneas punteadas son los **mapeadores**: la única pieza que conoce ambos lados.
> - `MatriculaEntity` es el **lado dueño**: la tabla `matricula` guarda `estudiante_id` y `curso_id`; `EstudianteEntity` y `CursoEntity` **no** conocen sus matrículas (relación unidireccional).
>
> > 💡 **Para dibujar en la pizarra:** dos columnas. A la izquierda, el modelo (lo que entiende el negocio); a la derecha, las entidades (lo que entiende la base); en el centro, los mapeadores como «traductores».

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Preparación</strong></summary>

> <details>
> <summary><strong>Paso 0.1 · Partir del taller terminado y agregar dos dependencias</strong></summary>
>
> > El punto de partida es el proyecto `academia` **terminado** en la [guía de los módulos 01 y 02](../modulo-01-02/01-guia-taller-gestion-de-cursos.md) (puntos de control 0 a 4).
> > Se agregan al `pom.xml` las dos dependencias nuevas; la de Spring Web ya estaba:
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
> > > 💡 **Alternativa:** crear un proyecto nuevo en [start.spring.io](https://start.spring.io) (Maven · Java 21 · Group `com.academia` ·
> > > Artifact `academia` · **Spring Web**, **Spring Data JPA**, **H2 Database**) y copiarle los paquetes `presentacion`, `negocio` y
> > > `persistence` del taller anterior.
> > >
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
> >
> > # Consola web de H2 (opcional)
> > spring.h2.console.enabled=true
> > ```
> >
> > | Propiedad | Para qué sirve |
> > |---|---|
> > | `spring.datasource.url` | Dirección de la base. `jdbc:h2:mem:academia` crea una base H2 **en memoria** llamada `academia`; `DB_CLOSE_DELAY=-1` evita que se borre al cerrarse la primera conexión. |
> > | `spring.datasource.driver-class-name`, `username`, `password` | Driver y credenciales por defecto de H2 (`sa` y sin contraseña). |
> > | `spring.jpa.hibernate.ddl-auto=update` | Hibernate **crea o actualiza las tablas** al arrancar, sin borrar datos ([Ejemplo 07 del M3](../../03-introduccion-a-jpa/material/ejemplos/07-uso-basico-de-spring-data-jpa.md)). |
> > | `spring.jpa.show-sql=true` | Imprime en la consola **el SQL** que ejecuta Hibernate: sirve para ver el ORM trabajando. |
> > | `spring.h2.console.enabled=true` | Habilita la consola web de H2 en `/h2-console`. |
> >
> > > 💡 **Diferencia con el Módulo 3.** Allí se declaraba `spring.jpa.database-platform=…H2Dialect`. Con Hibernate 6 (Spring Boot 3)
> > > el dialecto se **detecta solo**, así que aquí se omite; dejarlo no rompe nada, pero produce un aviso en el arranque.
>
> </details>
>
> <details>
> <summary><strong>Paso 0.3 · Arrancar y comprobar que nada se rompió</strong></summary>
>
> > Se ejecuta la aplicación (`./mvnw spring-boot:run`; en Windows, `mvnw.cmd spring-boot:run`):
> >
> > - Aparece `Started AcademiaApplication in … seconds`.
> > - **No** se crea ninguna tabla (sin `@Entity`, Hibernate no tiene qué mapear).
> > - Los escenarios de la guía anterior **siguen funcionando**: todavía se guardan en las listas.
> >
> > > ⚠️ **Trampa habitual.** `Failed to configure a DataSource: 'url' attribute is not specified` indica que falta la dependencia `h2`
> > > o que `application.properties` no está en `src/main/resources`.
> >
> > ✅ **Punto de control 0:** arranca sin errores, sin tablas, y la API sigue respondiendo con las listas.
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
> > > 🗣️ **Se muestra el modelo `Estudiante` al lado**: es el mismo de siempre (sin anotaciones, con `setId`). Dos clases, dos responsabilidades.
> >
> > | Elemento | Por qué |
> > |---|---|
> > | `@Entity` | Le dice a JPA que la clase se guarda en una tabla. |
> > | `@Table(name = "estudiante")` | **Necesario:** sin él, la tabla se llamaría `estudiante_entity` (el nombre de la clase). |
> > | `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` | `id` es la **clave primaria** y la **genera la base** (autoincremental). Reemplaza al contador `siguienteId` (**RN-01**). |
> > | `@Column(unique = true)` en `correo` | La base **también** rechaza correos repetidos (red de seguridad de **RN-02**). |
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
> > Los estudiantes aplican el mismo patrón. La diferencia está en `MatriculaEntity`: el modelo `Matricula` guarda **objetos** `Estudiante` y `Curso`, y la
> > entidad los guarda como **claves foráneas**.
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
> > - Las entidades **se relacionan entre sí** (`EstudianteEntity`, `CursoEntity`), no con los modelos: `persistence.entity` es un mundo cerrado.
> > - La relación es **unidireccional**: basta para todas las reglas y simplifica el mapeo.
>
> </details>
>
> <details>
> <summary><strong>Paso 1.3 · Ver las tablas que crea Hibernate</strong></summary>
>
> > Se reinicia la aplicación. Con `show-sql=true` aparece en la consola (abreviado):
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
> > Para mirarlas en el navegador: `http://localhost:8080/h2-console`
> >
> > | Campo | Valor |
> > |---|---|
> > | JDBC URL | `jdbc:h2:mem:academia` |
> > | User Name / Password | `sa` / *(vacío)* |
> >
> > > 🗣️ **Preguntas:** ¿quién escribió ese `create table`? *(Hibernate, a partir de las anotaciones.)* ¿Qué pasa con la API en este momento?
> > > *(Sigue funcionando con las listas: las tablas existen, pero **nadie las usa todavía**.)*
> >
> > ✅ **Punto de control 1:** aparecen **tres** `create table`, la consola de H2 muestra `ESTUDIANTE`, `CURSO` y `MATRICULA`, y la API de la guía anterior sigue respondiendo igual.
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
> | Método | Consulta generada | Lo usará |
> |---|---|---|
> | `existsByCorreoIgnoreCase(correo)` | `… where upper(correo)=upper(?)` | `existePorCorreo` (**RN-02**) |
> | `existsByEstudianteIdAndCursoId(…)` | `… where estudiante_id=? and curso_id=?` | `existe` (**RN-08**) |
> | `countByCursoId(cursoId)` | `select count(id) … where curso_id=?` | `contarPorCurso` (**RN-09**, **RN-11**) |
> | `save`, `findAll`, `findById` *(heredados)* | `insert`, `select`, `select … where id=?` | `guardar`, `listar`, `buscarPorId` |
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
<summary><strong>🔁 Paso 3 — Mapeadores: de entidad a modelo y de vuelta (<code>persistence.mapper</code>)</strong></summary>

> El repositorio recibe y devuelve **modelos** (así lo exige el contrato), pero la base guarda **entidades**. Alguien debe traducir; esa
> tarea vive en una clase pequeña por concepto, sin Spring y sin estado (métodos `static`).
>
> **Archivo:** `src/main/java/com/academia/persistence/mapper/EstudianteMapper.java`
>
> ```java
> package com.academia.persistence.mapper;
>
> import com.academia.negocio.model.Estudiante;
> import com.academia.persistence.entity.EstudianteEntity;
>
> public final class EstudianteMapper {
>
>     private EstudianteMapper() {
>     }
>
>     // El id no se copia: lo genera la base al guardar (RN-01)
>     public static EstudianteEntity aEntity(Estudiante estudiante) {
>         return new EstudianteEntity(estudiante.getNombre(), estudiante.getEdad(), estudiante.getCorreo());
>     }
>
>     public static Estudiante aModelo(EstudianteEntity entity) {
>         Estudiante estudiante = new Estudiante(entity.getNombre(), entity.getEdad(), entity.getCorreo());
>         estudiante.setId(entity.getId());
>         return estudiante;
>     }
> }
> ```
>
> **Archivo:** `src/main/java/com/academia/persistence/mapper/CursoMapper.java`
>
> ```java
> package com.academia.persistence.mapper;
>
> import com.academia.negocio.model.Curso;
> import com.academia.persistence.entity.CursoEntity;
>
> public final class CursoMapper {
>
>     private CursoMapper() {
>     }
>
>     public static CursoEntity aEntity(Curso curso) {
>         return new CursoEntity(curso.getNombre(), curso.getPrecio(), curso.getCupoMaximo());
>     }
>
>     public static Curso aModelo(CursoEntity entity) {
>         Curso curso = new Curso(entity.getNombre(), entity.getPrecio(), entity.getCupoMaximo());
>         curso.setId(entity.getId());
>         return curso;
>     }
> }
> ```
>
> **Archivo:** `src/main/java/com/academia/persistence/mapper/MatriculaMapper.java`
>
> ```java
> package com.academia.persistence.mapper;
>
> import com.academia.negocio.model.Matricula;
> import com.academia.persistence.entity.MatriculaEntity;
>
> public final class MatriculaMapper {
>
>     private MatriculaMapper() {
>     }
>
>     public static Matricula aModelo(MatriculaEntity entity) {
>         Matricula matricula = new Matricula(
>                 EstudianteMapper.aModelo(entity.getEstudiante()),
>                 CursoMapper.aModelo(entity.getCurso()));
>         matricula.setId(entity.getId());
>         return matricula;
>     }
> }
> ```
>
> | Dirección | Método | Qué hace |
> |---|---|---|
> | modelo → entidad | `aEntity(estudiante)` | Crea una `EstudianteEntity` **nueva** con los datos del negocio y **sin `id`**. |
> | entidad → modelo | `aModelo(entity)` | Crea el `Estudiante`, le pone el `id` con el `setId` que el modelo ya tenía y lo devuelve. |
>
> - `MatriculaMapper` **solo** tiene `aModelo`: reutiliza los otros dos para convertir los objetos anidados. El camino inverso lo hace
>   `RepositorioMatriculasEnBD` en el paso siguiente, con referencias a filas existentes.
> - **`aEntity` no copia el `id`.** Esa línea que no está es la que resuelve el problema del `id` enviado por el cliente (Paso 4.2).
> - `final` + constructor `private`: es una clase de utilidades; nadie la instancia.
>
> > 🗣️ **Pregunta:** ¿por qué los mapeadores están en `persistence` y no en `negocio`? *(Porque conocen **ambos** mundos; y `negocio` no debe conocer las
> > entidades. La regla de dependencia solo permite que `persistence` mire hacia `negocio`.)*

</details>

---

<details>
<summary><strong>🏛️ Paso 4 — Implementaciones <code>…EnBD</code> del contrato</strong></summary>

> <details>
> <summary><strong>Paso 4.1 · El contrato no cambia</strong></summary>
>
> > Las tres interfaces de la guía anterior **se dejan tal cual**. Ese es el contrato que los servicios conocen:
> >
> > ```java
> > public interface RepositorioEstudiantes {
> >     Estudiante guardar(Estudiante estudiante);
> >     List<Estudiante> listar();
> >     Optional<Estudiante> buscarPorId(Long id);
> >     boolean existePorCorreo(String correo);
> > }
> >
> > public interface RepositorioCursos {
> >     Curso guardar(Curso curso);
> >     List<Curso> listar();
> >     Optional<Curso> buscarPorId(Long id);
> > }
> >
> > public interface RepositorioMatriculas {
> >     Matricula guardar(Matricula matricula);
> >     boolean existe(Long estudianteId, Long cursoId);
> >     int contarPorCurso(Long cursoId);
> > }
> > ```
> >
> > > 🔎 Trabajan con **modelos** (`Estudiante`, `Curso`, `Matricula`), nunca con entidades.
>
> </details>
>
> <details>
> <summary><strong>Paso 4.2 · Las clases <code>…EnBD</code> (reemplazan a <code>…EnMemoria</code>)</strong></summary>
>
> > Cada una **cumple el contrato** delegando en Spring Data y traduciendo con el mapeador.
> >
> > **Archivo:** `src/main/java/com/academia/persistence/repository/RepositorioEstudiantesEnBD.java`
> >
> > ```java
> > package com.academia.persistence.repository;
> >
> > import com.academia.negocio.model.Estudiante;
> > import com.academia.persistence.entity.EstudianteEntity;
> > import com.academia.persistence.mapper.EstudianteMapper;
> > import java.util.List;
> > import java.util.Optional;
> > import org.springframework.stereotype.Repository;
> >
> > @Repository
> > public class RepositorioEstudiantesEnBD implements RepositorioEstudiantes {
> >
> >     private final EstudianteJpaRepository jpa;
> >
> >     public RepositorioEstudiantesEnBD(EstudianteJpaRepository jpa) {
> >         this.jpa = jpa;
> >     }
> >
> >     @Override
> >     public Estudiante guardar(Estudiante estudiante) {
> >         EstudianteEntity guardado = jpa.save(EstudianteMapper.aEntity(estudiante));
> >         estudiante.setId(guardado.getId());
> >         return estudiante;
> >     }
> >
> >     @Override
> >     public List<Estudiante> listar() {
> >         return jpa.findAll().stream().map(EstudianteMapper::aModelo).toList();
> >     }
> >
> >     @Override
> >     public Optional<Estudiante> buscarPorId(Long id) {
> >         return jpa.findById(id).map(EstudianteMapper::aModelo);
> >     }
> >
> >     @Override
> >     public boolean existePorCorreo(String correo) {
> >         return jpa.existsByCorreoIgnoreCase(correo);
> >     }
> > }
> > ```
> >
> > **Archivo:** `src/main/java/com/academia/persistence/repository/RepositorioCursosEnBD.java`
> >
> > ```java
> > package com.academia.persistence.repository;
> >
> > import com.academia.negocio.model.Curso;
> > import com.academia.persistence.entity.CursoEntity;
> > import com.academia.persistence.mapper.CursoMapper;
> > import java.util.List;
> > import java.util.Optional;
> > import org.springframework.stereotype.Repository;
> >
> > @Repository
> > public class RepositorioCursosEnBD implements RepositorioCursos {
> >
> >     private final CursoJpaRepository jpa;
> >
> >     public RepositorioCursosEnBD(CursoJpaRepository jpa) {
> >         this.jpa = jpa;
> >     }
> >
> >     @Override
> >     public Curso guardar(Curso curso) {
> >         CursoEntity guardado = jpa.save(CursoMapper.aEntity(curso));
> >         curso.setId(guardado.getId());
> >         return curso;
> >     }
> >
> >     @Override
> >     public List<Curso> listar() {
> >         return jpa.findAll().stream().map(CursoMapper::aModelo).toList();
> >     }
> >
> >     @Override
> >     public Optional<Curso> buscarPorId(Long id) {
> >         return jpa.findById(id).map(CursoMapper::aModelo);
> >     }
> > }
> > ```
> >
> > **Archivo:** `src/main/java/com/academia/persistence/repository/RepositorioMatriculasEnBD.java`
> >
> > ```java
> > package com.academia.persistence.repository;
> >
> > import com.academia.negocio.model.Matricula;
> > import com.academia.persistence.entity.CursoEntity;
> > import com.academia.persistence.entity.EstudianteEntity;
> > import com.academia.persistence.entity.MatriculaEntity;
> > import org.springframework.stereotype.Repository;
> >
> > @Repository
> > public class RepositorioMatriculasEnBD implements RepositorioMatriculas {
> >
> >     private final MatriculaJpaRepository jpa;
> >     private final EstudianteJpaRepository estudiantesJpa;
> >     private final CursoJpaRepository cursosJpa;
> >
> >     public RepositorioMatriculasEnBD(MatriculaJpaRepository jpa,
> >                                      EstudianteJpaRepository estudiantesJpa,
> >                                      CursoJpaRepository cursosJpa) {
> >         this.jpa = jpa;
> >         this.estudiantesJpa = estudiantesJpa;
> >         this.cursosJpa = cursosJpa;
> >     }
> >
> >     @Override
> >     public Matricula guardar(Matricula matricula) {
> >         // Solo se necesita la referencia (la clave foránea): no hace falta cargar las filas completas
> >         EstudianteEntity estudiante = estudiantesJpa.getReferenceById(matricula.getEstudiante().getId());
> >         CursoEntity curso = cursosJpa.getReferenceById(matricula.getCurso().getId());
> >         MatriculaEntity guardada = jpa.save(new MatriculaEntity(estudiante, curso));
> >         matricula.setId(guardada.getId());
> >         return matricula;
> >     }
> >
> >     @Override
> >     public boolean existe(Long estudianteId, Long cursoId) {
> >         return jpa.existsByEstudianteIdAndCursoId(estudianteId, cursoId);
> >     }
> >
> >     @Override
> >     public int contarPorCurso(Long cursoId) {
> >         return (int) jpa.countByCursoId(cursoId);
> >     }
> > }
> > ```
> >
> > Puntos a resaltar:
> >
> > - **`@Repository`** va aquí, en la clase que **cumple el contrato** (igual que en `…EnMemoria`); no en las interfaces de Spring Data.
> > - **`guardar`:** se convierte el modelo en entidad, se guarda y se le asigna al modelo el `id` generado (`estudiante.setId(…)`), igual que hacía la
> >   versión en memoria con `siguienteId++`. El servicio recibe de vuelta **su mismo objeto**, ya con `id`.
> > - **`listar` / `buscarPorId`:** `findAll()` y `findById()` devuelven entidades; `.map(Mapper::aModelo)` las convierte. `Optional` se conserva.
> > - **`getReferenceById`** (en matrículas): entrega una **referencia** a la fila por su `id`, sin leerla. Alcanza para escribir la clave foránea, y evita
> >   dos consultas innecesarias (el servicio ya validó que existen).
> > - **`(int) countByCursoId`:** Spring Data cuenta con `long`; el contrato (y el resto del código) usa `int`.
> >
> > > ⚠️ **El problema del `id` enviado por el cliente.** Si un `POST /estudiantes` trae `{"id":1,…}`, Jackson lo deja en el modelo (por `setId`). Pero
> > > `aEntity` **no lo copia**, así que la base siempre inserta una fila nueva. Se probó: el estudiante «Intruso» recibió el `id` 3 y **Ana no se modificó**.
> > > (Si en cambio se anotara el modelo con `@Entity` y se hiciera `save` con ese `id`, JPA lo tomaría como una **actualización** y sobrescribiría a Ana.)
>
> </details>
>
> <details>
> <summary><strong>Paso 4.3 · Retirar las clases <code>…EnMemoria</code></strong></summary>
>
> > Se **eliminan** `RepositorioEstudiantesEnMemoria`, `RepositorioCursosEnMemoria` y `RepositorioMatriculasEnMemoria`. Se pide primero al grupo **no** borrarlas, para ver el error:
> >
> > ```text
> > Parameter 0 of constructor in com.academia.negocio.service.ServicioEstudiantes required a single bean, but 2 were found:
> >     - repositorioEstudiantesEnMemoria: defined in …
> >     - repositorioEstudiantesEnBD: defined in …
> > ```
> >
> > > 🔎 **Concepto (M2, repaso):** hay dos beans que cumplen `RepositorioEstudiantes` y Spring **no sabe cuál inyectar**: es la ambigüedad de la pregunta 17 del
> > > cuestionario del M2 y el «reto final» de la guía anterior (*cambiar de almacenamiento dejando una sola clase anotada*). Aquí se resuelve **borrando** la
> > > versión en memoria. Con `@Primary` o `@Qualifier` convivirían, pero es otro tema.
> >
> > ✅ **Punto de control 4:** el proyecto compila y arranca; en el registro **no** aparece ningún error de beans.
>
> </details>

</details>

---

<details>
<summary><strong>✋ Paso 5 — Lo que NO se toca: modelo, servicios y controladores</strong></summary>

> Se abren, **sin modificarlos**, `ServicioEstudiantes`, `ServicioCursos`, `ServicioMatriculas`, los tres controladores y las clases del modelo, y se confirma:
>
> | Archivo | ¿Menciona JPA, entidades o la base? | Cambios |
> |---|:-:|:-:|
> | `negocio.model.*` | No (sin una anotación de JPA) | 0 líneas |
> | `negocio.service.*` | No (solo conocen `RepositorioXxx`, el contrato) | 0 líneas |
> | `presentacion.controlador.*` | No (solo conocen los servicios) | 0 líneas |
>
> Ejemplo, el corazón de la regla de matrícula, **idéntico** al de la guía anterior:
>
> ```java
> if (repositorio.existe(estudianteId, cursoId)) {
>     throw new IllegalStateException("El estudiante " + estudianteId + " ya está matriculado en el curso " + cursoId);
> }
> if (repositorio.contarPorCurso(cursoId) >= curso.getCupoMaximo()) {
>     throw new IllegalStateException("El curso " + cursoId + " no tiene cupos disponibles");
> }
> ```
>
> > 🗣️ **Pregunta para el grupo:** ¿qué habría pasado si los servicios dependieran de `JpaRepository` en lugar del contrato? *(Habría que cambiar `guardar` por `save`,
> > `listar` por `findAll`… en **cada** servicio, y el negocio quedaría atado a Spring Data. Con el contrato, el cambio se queda dentro de `persistence`.)*
> >
> > 🔎 **Concepto (M2):** por esto se programa contra **interfaces**: el servicio pide «alguien que cumpla `RepositorioEstudiantes`» y no sabe si detrás hay una `List` o
> > una base de datos.
>
> ✅ **Punto de control 5:** la comparación de carpetas del IDE (o `git diff`, si el proyecto está en Git) muestra **cero** cambios en `negocio` y `presentacion` respecto del taller anterior.

</details>

---

<details>
<summary><strong>🧪 Paso 6 — Pruebas de la API y de la base de datos</strong></summary>

> <details>
> <summary><strong>Paso 6.1 · Escenarios (los mismos de la guía anterior)</strong></summary>
>
> > Se reinicia la aplicación (la base en memoria parte vacía) y se ejecutan **en este orden**. Los resultados son **idénticos** a los de la versión con listas, incluidas las
> > excepciones de la consola: esa es la prueba de que la migración respetó las reglas.
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
> > | 11 | `id` enviado por el cliente | `POST /estudiantes` con `{"id":1,"nombre":"Intruso",…}` | `201` con un **`id` nuevo**; Ana **no** se modifica |
> >
> > Respuesta del escenario 10:
> >
> > ```json
> > {"curso":"Java desde cero","cupoMaximo":3,"matriculados":2,"cuposDisponibles":1,"porcentajeOcupacion":66.67}
> > ```
> >
> > Respuesta del escenario 7 (la matrícula trae los objetos completos, no `estudianteId` y `cursoId` sueltos):
> >
> > ```json
> > {"id":1,"estudiante":{"id":1,"nombre":"Ana Torres","edad":22,"correo":"ana@correo.com"},
> >  "curso":{"id":1,"nombre":"Java desde cero","precio":150000.0,"cupoMaximo":3}}
> > ```
>
> </details>
>
> <details>
> <summary><strong>Paso 6.2 · Leer el SQL que genera cada petición</strong></summary>
>
> > Con `show-sql=true`, **una misma matrícula** produce estas consultas (reales, abreviadas). Se proyectan junto al código de `ServicioMatriculas`:
> >
> > ```text
> > POST /matriculas  {"estudianteId":1,"cursoId":1}
> > Hibernate: select … from estudiante ee1_0 where ee1_0.id=?                              ← servicioEstudiantes.obtener  → findById
> > Hibernate: select … from curso ce1_0 where ce1_0.id=?                                   ← servicioCursos.obtener       → findById
> > Hibernate: select me1_0.id from matricula me1_0 where me1_0.estudiante_id=? and me1_0.curso_id=? fetch first ? rows only
> >                                                                                         ← existe → existsByEstudianteIdAndCursoId
> > Hibernate: select count(me1_0.id) from matricula me1_0 where me1_0.curso_id=?           ← contarPorCurso → countByCursoId
> > Hibernate: insert into matricula (curso_id,estudiante_id,id) values (?,?,default)       ← guardar → save
> > ```
> >
> > > 🗣️ **Preguntas:** ¿en qué orden están las consultas y a qué regla corresponde cada una? *(El mismo orden de los controles: RN-03, RN-07, RN-08, RN-09 y, si todo
> > > pasa, RN-10.)* ¿Por qué **no** hay `select` de las filas antes del `insert`? *(`getReferenceById` solo toma el `id`: no lee la fila.)*
> > >
> > > 🔎 **Concepto (M3):** esto es el **ORM**: el código solo habla de objetos y Hibernate lo traduce a SQL ([Ejemplo 04 del M3](../../03-introduccion-a-jpa/material/ejemplos/04-que-es-orm.md)).
>
> </details>
>
> <details>
> <summary><strong>Paso 6.3 · Mirar los datos en la consola de H2</strong></summary>
>
> > En `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:academia`, usuario `sa`):
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
> > ✅ **Punto de control 6:** los escenarios 1 a 11 dan el resultado esperado y las filas de `MATRICULA` coinciden con lo enviado a la API. **Se detiene la aplicación y se vuelve a
> > arrancar: la base en memoria vuelve a estar vacía.** Ese es el problema que resuelve el paso siguiente.
>
> </details>

</details>

---

<details>
<summary><strong>💾 Paso 7 — Que los datos sobrevivan al reinicio (H2 en archivo)</strong></summary>

> Hasta aquí la base está **en memoria**: es una base de datos de verdad, pero vive dentro de la JVM y muere con ella. Cambiando **solo una línea** se guarda en un archivo del disco.
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
> > 🔎 **Cierre de la historia.** En el taller de los módulos 01 y 02 reiniciar borraba todo y se dijo que era *«lo que motivará el Módulo 3»*. Ahora el mismo escenario conserva los
> > datos **sin cambiar ni una regla**, ni un servicio, ni un controlador.
> >
> > 💡 **Y en producción:** H2 en archivo es una comodidad de desarrollo. Contra MySQL o PostgreSQL cambian el **driver** (dependencia) y las propiedades `spring.datasource.*`; el código de
> > las tres capas **no se toca**. Es uno de los retos finales.

</details>

---

<details>
<summary><strong>🏁 Cierre — preguntas de síntesis</strong></summary>

> 1. ¿Qué capas cambiaron al pasar de listas a base de datos? *(Solo `persistence`: entidades, mapeadores, interfaces de Spring Data y las clases `…EnBD`.)*
> 2. ¿Por qué el servicio **no cambió** ni una línea? *(Depende del contrato `RepositorioEstudiantes`, que sigue igual; no sabe si detrás hay una lista o una base.)*
> 3. ¿Qué diferencia hay entre el modelo `Estudiante` y `EstudianteEntity`? *(El modelo es el concepto del negocio y no conoce JPA; la entidad describe cómo se guarda en una tabla.)*
> 4. ¿Quién escribe la implementación de `EstudianteJpaRepository`? ¿Y la de `RepositorioEstudiantesEnBD`? *(Spring Data genera la primera; la segunda la escribimos nosotros para cumplir el contrato.)*
> 5. ¿Por qué `aEntity` no copia el `id`? *(Para que la base siempre genere uno nuevo: el cliente no puede sobrescribir registros enviando un `id`.)*
> 6. ¿Qué diferencia hay entre una base **en memoria** y una **en archivo**? *(Ambas son bases reales; solo la segunda sobrevive al reinicio.)*

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
>
> # El id que envía el cliente se ignora
> curl -i -X POST localhost:8080/estudiantes -H "Content-Type: application/json" \
>   -d '{"id":1,"nombre":"Intruso","edad":40,"correo":"intruso@correo.com"}'               # 201 con id nuevo; Ana no cambia
> ```
>
> > 💡 Qué significa cada opción de `curl` está explicado en la [guía de los módulos 01 y 02](../modulo-01-02/01-guia-taller-gestion-de-cursos.md). En PowerShell se usa `curl.exe`.

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Solución |
> |---|---|---|
> | `Failed to configure a DataSource: 'url' attribute is not specified` | Falta la dependencia `h2` o `application.properties` | Agregar el driver; revisar que esté en `src/main/resources` |
> | `required a single bean, but 2 were found` (`…EnMemoria` y `…EnBD`) | Quedaron las dos implementaciones del mismo contrato | Borrar las clases `…EnMemoria` (Paso 4.3) |
> | La tabla se llama `estudiante_entity` | Falta `@Table(name = "estudiante")` en la entidad | Agregar `@Table` |
> | `Not a managed type: class …EstudianteEntity` | Falta `@Entity`, o la clase quedó **fuera** de `com.academia` (el escaneo no la ve) | Anotar la clase y dejarla bajo el paquete de `AcademiaApplication` |
> | `No default constructor for entity` | La entidad no tiene constructor **sin argumentos** | Agregar `protected EstudianteEntity() {}` |
> | `PropertyReferenceException: No property 'x' found for type 'EstudianteEntity'` | El nombre del método derivado no coincide con un atributo de la **entidad** | Usar el nombre exacto: `existsByCorreoIgnoreCase`, `countByCursoId` |
> | `Table "ESTUDIANTE" not found` | `ddl-auto=none`, o se apuntó a otra base (`mem` vs `file`) | Usar `update` y revisar la URL |
> | `NullPointerException` en un mapeador al leer una matrícula | La fila tiene `estudiante_id` o `curso_id` nulos | Revisar los datos; las matrículas siempre se crean con ambos |
> | Los datos desaparecen al reiniciar | La URL es `jdbc:h2:mem:…` | Cambiar a `jdbc:h2:file:./data/academia` |
> | `Database may be already in use` | Dos procesos usan el mismo `data/academia.mv.db` | Cerrar la otra ejecución |
> | `Port 8080 was already in use` | Otra ejecución sigue abierta | Detenerla o cambiar `server.port` |
> | `415 Unsupported Media Type` | Falta `Content-Type: application/json` | Agregarlo en Postman o `curl` |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **Restricción en la base:** en `MatriculaEntity`, agregar `@Table(name = "matricula", uniqueConstraints = @UniqueConstraint(columnNames = {"estudiante_id", "curso_id"}))`
>    y comprobar en la consola de H2 que la base **también** impide la matrícula repetida (RN-08). ¿Para qué sirve si el servicio ya lo valida?
>    *(Dos peticiones simultáneas podrían pasar ambas la validación; la restricción es la última defensa.)*
> 2. **Filtro en la base:** `listarEconomicos()` recorre **todos** los cursos con un ciclo. Agregar `listarConPrecioMenorA(double)` al contrato `RepositorioCursos`,
>    implementarlo con `findByPrecioLessThan` en `CursoJpaRepository` y usarlo en el servicio. ¿Qué capas se tocan ahora? *(Contrato, `…EnBD` y el servicio:
>    es un **cambio del contrato**, no solo de almacenamiento.)*
> 3. **Transacción:** anotar `matricular` con `@Transactional` y explicar qué ventaja da ([Ejemplo 08 del M3](../../03-introduccion-a-jpa/material/ejemplos/08-anotaciones-en-jpa.md)). ¿Dónde conviene ponerla: en el servicio o en el repositorio?
> 4. **Consulta con JPQL:** reescribir `countByCursoId` con `@Query("SELECT COUNT(m) FROM MatriculaEntity m WHERE m.curso.id = :cursoId")`.
> 5. **Otro motor:** reemplazar H2 por **MySQL** o **PostgreSQL** (dependencia del driver + `spring.datasource.*`) y comprobar que **ninguna** clase de las tres capas cambia.
> 6. **Automatizar el mapeo:** reemplazar los mapeadores manuales por **MapStruct** y comparar cuánto código desaparece.
> 7. **Probar el mapeo:** escribir una prueba unitaria de `EstudianteMapper` (ida y vuelta: modelo → entidad → modelo conserva `nombre`, `edad` y `correo`, y `aEntity` no copia el `id`).
> 8. **Relación bidireccional:** agregar `List<MatriculaEntity>` en `CursoEntity` con `@OneToMany(mappedBy = "curso")` y explicar por qué complica el mapeo y el JSON
>    ([Ejemplo 03 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/03-oneToMany-manyToOne-y-fetch.md)).

</details>

<details>
<summary><strong>📁 Estructura final esperada</strong></summary>

> ```text
> 📁 academia
> ├── 📄 pom.xml
> ├── 📄 .gitignore                         ← incluye  data/
> ├── 📁 data                               ← lo crea H2 en el Paso 7 (no se versiona)
> │   └── 📄 academia.mv.db
> └── 📁 src/main
>     ├── 📁 java/com/academia
>     │   ├── 📄 AcademiaApplication.java
>     │   ├── 📁 presentacion
>     │   │   └── 📁 controlador   ControladorEstudiantes · ControladorCursos · ControladorMatriculas · SolicitudMatricula
>     │   ├── 📁 negocio
>     │   │   ├── 📁 model         Estudiante · Curso · Matricula · Ocupacion
>     │   │   └── 📁 service       ServicioEstudiantes · ServicioCursos · ServicioMatriculas
>     │   └── 📁 persistence
>     │       ├── 📁 entity        EstudianteEntity · CursoEntity · MatriculaEntity
>     │       ├── 📁 mapper        EstudianteMapper · CursoMapper · MatriculaMapper
>     │       └── 📁 repository    3 interfaces (contrato) · 3 …JpaRepository (Spring Data) · 3 …EnBD (@Repository)
>     └── 📁 resources
>         └── 📄 application.properties
> ```
>
> > 🔎 Comparado con la guía anterior: **sin cambios** en `presentacion` y `negocio`; en `persistence` desaparecen las 3 clases `…EnMemoria` y se suman `entity`, `mapper`, las 3 `…JpaRepository` y las 3 `…EnBD`.

</details>

<details>
<summary><strong>🏷️ Repaso de las anotaciones nuevas (JPA)</strong></summary>

> Se **suman** a las del taller anterior (`@RestController`, `@Service`, `@Repository`, `@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestBody`, `@ResponseStatus`,
> `@SpringBootApplication`). `@Repository` se mantiene, ahora en las clases `…EnBD`. Las nuevas viven **solo** en `persistence.entity`.
>
> | Anotación | Dónde se usa | Por qué | Para qué sirve |
> |---|---|---|---|
> | `@Entity` | `EstudianteEntity`, `CursoEntity`, `MatriculaEntity` | La clase representa una **tabla**. | Hibernate la mapea y crea la tabla (`ddl-auto`). |
> | `@Table(name = "…")` | Las tres entidades | El nombre de la clase termina en `Entity`; la tabla no. | Fija el nombre de la tabla (`estudiante`, `curso`, `matricula`). |
> | `@Id` | Atributo `id` de cada entidad | Toda entidad necesita **clave primaria**. | Marca la columna que identifica cada fila. |
> | `@GeneratedValue(strategy = IDENTITY)` | Atributo `id` | El valor lo decide el sistema (**RN-01**). | La base lo genera de forma autoincremental. |
> | `@Column(unique = true)` | `EstudianteEntity.correo` | Un correo no puede repetirse (**RN-02**). | Crea una restricción `UNIQUE` en la columna. |
> | `@ManyToOne` | `MatriculaEntity.estudiante`, `.curso` | Muchas matrículas apuntan a **un** estudiante o curso. | Declara la relación de muchos a uno. |
> | `@JoinColumn(name = "…")` | Esos mismos dos atributos | Hay que nombrar la **clave foránea**. | Define `estudiante_id` / `curso_id`; `MatriculaEntity` es el **lado dueño**. |
>
> ```text
> presentacion  ──▶  @RestController · @RequestMapping · @GetMapping · @PostMapping · @PathVariable · @RequestBody · @ResponseStatus
> negocio       ──▶  @Service                                  (model: ninguna anotación de JPA)
> persistence   ──▶  @Entity · @Table · @Id · @GeneratedValue · @Column · @ManyToOne · @JoinColumn     (en persistence.entity)
>                    @Repository                               (en las clases …EnBD; las …JpaRepository no llevan)
> arranque      ──▶  @SpringBootApplication
> ```
>
> > 🔎 **Lectura clave.** Las anotaciones de JPA **no aparecen** ni en `negocio` ni en `presentacion`: por eso esas capas no cambiaron. Más ejemplos de cada una: [Anotaciones en JPA](../../03-introduccion-a-jpa/material/ejemplos/08-anotaciones-en-jpa.md).

</details>
