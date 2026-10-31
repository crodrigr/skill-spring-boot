# 🔗 Guía 00 — Relaciones entre entidades en JPA: uno a uno, uno a muchos y muchos a muchos

<div style="background-color:#fff3b0; color:#1f1f1f; border-left:6px solid #f2b705; padding:12px 16px; border-radius:6px;">

🎯 **Qué se aprende:** a declarar las tres relaciones de JPA, **desde las entidades**, en **un sentido** y en **doble sentido**, y qué significa cada notación.

⏱️ **Tiempo:** unos 20 minutos de lectura y 25 de práctica. 📚 **Va antes de** la [guía del taller «Gestión de cursos» con base de datos](01-guia-taller-gestion-de-cursos-con-bd.md) y de las [guías de la tienda](03-guia-tienda-2-uno-a-muchos.md).

✅ **Verificada:** el código se compiló y se ejecutó con Spring Boot 3.3 y **MySQL 8**; las tablas, los resultados y los errores que se muestran son reales.

</div>

<details open>
<summary><strong>🧠 Tres ideas para entender todas las relaciones</strong></summary>

> | # | Idea | En una frase |
> |:-:|---|---|
> | 1 | **Una relación es una clave foránea** (o una tabla intermedia). | En la base de datos no existen «listas de objetos»: existe una columna `xxx_id` que apunta a otra tabla. |
> | 2 | **Lado dueño y lado inverso.** | **Un solo lado** escribe esa columna: el **dueño**. El otro, el **inverso**, solo la lee (`mappedBy`). |
> | 3 | **Un sentido o doble sentido.** | **Un sentido** = solo el dueño conoce al otro. **Doble sentido** = los dos se conocen (el inverso es opcional). |
>
> > 🔎 **Regla práctica:** el dueño es el lado que tiene `@JoinColumn` o `@JoinTable`. El inverso es el que dice `mappedBy`.

</details>

<details open>
<summary><strong>🏷️ Las notaciones</strong></summary>

> | Notación | Qué significa | Atributos que importan |
> |---|---|---|
> | `@OneToOne` | **Uno a uno**: una fila se relaciona con **una** fila de la otra tabla. | `mappedBy`, `cascade`, `fetch` |
> | `@OneToMany` | **Uno a muchos**: un objeto tiene **una lista** de otros. | `mappedBy`, `cascade`, `fetch` |
> | `@ManyToOne` | **Muchos a uno**: muchos objetos apuntan a **uno**. Es el lado que guarda la clave foránea. | `fetch`, `optional` |
> | `@ManyToMany` | **Muchos a muchos**: cada lado tiene una colección del otro. | `mappedBy`, `cascade`, `fetch` |
> | `@JoinColumn(name = "x_id")` | **Nombra la columna de clave foránea** y marca al **dueño**. | `name` |
> | `@JoinTable(...)` | **Nombra la tabla intermedia** de un muchos a muchos y marca al **dueño**. | `name`, `joinColumns`, `inverseJoinColumns` |
> | `mappedBy = "atributo"` | Marca el lado **inverso**: «la clave foránea la maneja el atributo `atributo` del otro lado». | el **nombre exacto** del atributo del dueño |
> | `cascade = CascadeType.ALL` | Las operaciones (guardar, borrar…) del objeto **se propagan** al relacionado. | `PERSIST`, `REMOVE`, `ALL`… |
> | `fetch = FetchType.LAZY / EAGER` | **Cuándo** se carga lo relacionado: bajo demanda o junto con el objeto. | Por defecto: `@…ToOne` → **EAGER**; `@…ToMany` → **LAZY** |
>
> > 💡 **Cómo leer una notación:** se lee **desde la clase donde está escrita**. `@OneToMany` en `DocenteEntity` = «un docente tiene **muchos** cursos». `@ManyToOne` en `CursoEntity` = «muchos cursos tienen **un** docente».
> >
> > `joinColumns` apunta a **este** lado de la tabla intermedia; `inverseJoinColumns`, al **otro**. Más ejemplos en [Anotaciones en JPA](../../03-introduccion-a-jpa/material/ejemplos/08-anotaciones-en-jpa.md).

</details>

<details open>
<summary><strong>🧱 Las notaciones de los campos de una entidad</strong></summary>

>
> Antes de relacionar entidades conviene conocer las notaciones de **una sola** entidad: las que dicen qué tabla es, cuál es la clave y cómo se guarda cada campo. Las relaciones (`@OneToMany`…) se agregan después sobre esta base.
>
> <details open>
> <summary><strong>📋 Las notaciones</strong></summary>
>
> > | Notación | Dónde va | Qué hace | Atributos que importan |
> > |---|---|---|---|
> > | `@Entity` | clase | Marca la clase como **entidad**: JPA la mapea a una tabla. | `name` |
> > | `@Table` | clase | **Nombra la tabla** y puede declarar restricciones e índices. | `name`, `uniqueConstraints`, `indexes` |
> > | `@Id` | campo | Marca la **clave primaria**. Toda entidad necesita una. | — |
> > | `@GeneratedValue` | campo | La clave **se genera sola**. `IDENTITY` usa el `AUTO_INCREMENT` de MySQL. | `strategy` |
> > | `@Column` | campo | **Configura la columna** del campo. Es opcional: sin ella se usa el nombre del campo. | `name`, `nullable`, `unique`, `length`, `precision`, `scale`, `updatable`, `insertable` |
> > | `@Enumerated` | campo | Cómo se guarda un `enum`: `STRING` (el nombre) u `ORDINAL` (la posición). | `value` |
> > | `@Lob` | campo | Texto o archivo **grande**: en MySQL, `longtext` o `longblob`. | — |
> > | `@Transient` | campo | El campo **no se guarda**: no tiene columna. | — |
> > | `@CreationTimestamp` | campo | Pone la fecha y hora **al insertar** (es de Hibernate). | — |
> > | `@UpdateTimestamp` | campo | Pone la fecha y hora **en cada actualización** (es de Hibernate). | — |
> > | `@Version` | campo | Control de **concurrencia optimista**: el número sube en cada cambio y evita que dos usuarios se pisen. | — |
> >
> > <details open>
> > <summary><strong>🔍 Entender <code>@Table</code>: nombre, restricción única e índice</strong></summary>
> >
> > > ```java
> > > @Table(name = "curso",                                     // tabla «curso»
> > >        uniqueConstraints = @UniqueConstraint(columnNames = "codigo"),
> > >        indexes = @Index(name = "idx_curso_nombre", columnList = "nombre"))
> > > ```
> > >
> > > `@Table` solo configura la **tabla**. Sus tres atributos, uno por línea:
> > >
> > > | Parte | Qué hace | SQL que genera |
> > > |---|---|---|
> > > | `name = "curso"` | Pone el **nombre de la tabla**. Sin él, la tabla se llamaría como la clase en `snake_case` (`curso_entity`). | `create table curso (…)` |
> > > | `uniqueConstraints = @UniqueConstraint(columnNames = "codigo")` | Declara una **restricción de unicidad**: no pueden existir dos filas con el mismo `codigo`. Con varias columnas, la combinación es la que no se repite: `columnNames = {"codigo", "sede"}`. | `alter table curso add constraint UK… unique (codigo)` |
> > > | `indexes = @Index(name = "idx_curso_nombre", columnList = "nombre")` | Crea un **índice** sobre `nombre`: las búsquedas `where nombre = …` son más rápidas. **No** impide repetidos. | `create index idx_curso_nombre on curso (nombre)` |
> > >
> > > **Diferencia clave:** la **restricción única** protege los **datos** (rechaza duplicados); el **índice** acelera las **consultas** (no rechaza nada).
> > >
> > > 💡 **`unique` en `@Column` o `@UniqueConstraint`:** en `codigo` aparecen las dos formas (`@Column(unique = true)` y `@UniqueConstraint`) solo para mostrarlas; **producen la misma restricción**, y en un proyecto real alcanza con una. `@UniqueConstraint` es la forma para unicidad de **varias columnas a la vez**; para una sola columna basta `@Column(unique = true)`.
> > >
> > > Para varios índices o restricciones se usan llaves: `indexes = { @Index(columnList = "nombre"), @Index(columnList = "fecha_inicio") }`. Dentro de `columnList` va el nombre de la **columna** (`fecha_inicio`), no el del campo Java (`fechaInicio`).
> >
> > </details>
> >
> > 🔎 **Lo que no necesita notación:** un campo sin notación se guarda igual, en una columna con su nombre en `snake_case` (`fechaInicio` → `fecha_inicio`). Los tipos `LocalDate` y `LocalDateTime` se guardan como `date` y `datetime` **sin** `@Temporal`; esa notación solo se usa con `java.util.Date`.
>
> </details>
>
> <details open>
> <summary><strong>💻 Ejemplo: CursoEntity con todos sus campos</strong></summary>
>
> > ```java
> > package com.academia.persistence.entity;
> >
> > import jakarta.persistence.*;
> > import lombok.Data;
> > import lombok.NoArgsConstructor;
> > import org.hibernate.annotations.CreationTimestamp;
> > import org.hibernate.annotations.UpdateTimestamp;
> >
> > import java.math.BigDecimal;
> > import java.time.LocalDate;
> > import java.time.LocalDateTime;
> >
> > @Entity                                                    // es una entidad
> > @Table(name = "curso",                                     // tabla «curso»
> >        uniqueConstraints = @UniqueConstraint(columnNames = "codigo"),
> >        indexes = @Index(name = "idx_curso_nombre", columnList = "nombre"))
> > @Data
> > @NoArgsConstructor
> > public class CursoEntity {
> >
> >     @Id                                                    // clave primaria
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)    // AUTO_INCREMENT
> >     private Long id;
> >
> >     @Column(name = "codigo", nullable = false, unique = true, length = 10)
> >     private String codigo;                                 // varchar(10) not null, único
> >
> >     @Column(nullable = false, length = 100)
> >     private String nombre;                                 // varchar(100) not null
> >
> >     @Lob
> >     private String descripcion;                            // longtext
> >
> >     @Column(precision = 8, scale = 2)
> >     private BigDecimal precio;                             // decimal(8,2)
> >
> >     private LocalDate fechaInicio;                         // sin notación: date
> >
> >     private boolean activo;                                // sin notación: bit not null
> >
> >     @Enumerated(EnumType.STRING)
> >     @Column(nullable = false, length = 15)
> >     private NivelCurso nivel;                              // enum guardado por su nombre
> >
> >     @Column(updatable = false)
> >     @CreationTimestamp
> >     private LocalDateTime creadoEn;                        // se llena al insertar y no cambia
> >
> >     @UpdateTimestamp
> >     private LocalDateTime actualizadoEn;                   // se llena en cada actualización
> >
> >     @Version
> >     private Long version;                                  // control de concurrencia
> >
> >     @Transient
> >     private String precioFormateado;                       // NO se guarda: no tiene columna
> > }
> > ```
> >
> > ```java
> > package com.academia.persistence.entity;
> >
> > public enum NivelCurso { BASICO, INTERMEDIO, AVANZADO }
> > ```
>
> </details>
>
> <details open>
> <summary><strong>🗄️ Así queda en la tabla</strong></summary>
>
> > Al iniciar la aplicación, Hibernate genera (ejecutado en MySQL 8):
> >
> > ```text
> > create table curso (activo bit not null, fecha_inicio date, precio decimal(8,2),
> >     actualizado_en datetime(6), creado_en datetime(6), id bigint not null auto_increment,
> >     version bigint, codigo varchar(10) not null, nombre varchar(100) not null,
> >     descripcion longtext, nivel enum ('AVANZADO','BASICO','INTERMEDIO') not null,
> >     primary key (id))
> > create index idx_curso_nombre on curso (nombre)
> > alter table curso add constraint UK… unique (codigo)
> > ```
> >
> > Cada campo y la columna que produce:
> >
> > | Campo Java | Notación | Columna en MySQL | Restricción |
> > |---|---|---|---|
> > | `id` | `@Id` + `@GeneratedValue` | `id bigint` | clave primaria, `AUTO_INCREMENT` |
> > | `codigo` | `@Column(nullable=false, unique=true, length=10)` | `codigo varchar(10)` | `NOT NULL`, `UNIQUE` |
> > | `nombre` | `@Column(nullable=false, length=100)` | `nombre varchar(100)` | `NOT NULL`, con índice `idx_curso_nombre` |
> > | `descripcion` | `@Lob` | `descripcion longtext` | — |
> > | `precio` | `@Column(precision=8, scale=2)` | `precio decimal(8,2)` | — |
> > | `fechaInicio` | *(ninguna)* | `fecha_inicio date` | — |
> > | `activo` | *(ninguna)* | `activo bit` | `NOT NULL` (un `boolean` primitivo nunca es nulo) |
> > | `nivel` | `@Enumerated(STRING)` | `nivel enum('AVANZADO','BASICO','INTERMEDIO')` | `NOT NULL` |
> > | `creadoEn` | `@CreationTimestamp` | `creado_en datetime(6)` | — |
> > | `actualizadoEn` | `@UpdateTimestamp` | `actualizado_en datetime(6)` | — |
> > | `version` | `@Version` | `version bigint` | — |
> > | `precioFormateado` | `@Transient` | **no existe** | — |
> >
> > Al guardar un curso (`em.persist`) con los datos de Java, la fila queda así:
> >
> > ```text
> > curso
> > +----+---------+--------+---------------------------+--------+--------------+--------+--------+---------------------+---------------------+---------+
> > | id | codigo  | nombre | descripcion               | precio | fecha_inicio | activo | nivel  | creado_en           | actualizado_en      | version |
> > +----+---------+--------+---------------------------+--------+--------------+--------+--------+---------------------+---------------------+---------+
> > |  1 | JAV-101 | Java   | Curso de Java desde cero  |  49.90 | 2026-03-02   |      1 | BASICO | 2026-10-31 11:45:45 | 2026-10-31 11:45:45 |       0 |
> > +----+---------+--------+---------------------------+--------+--------------+--------+--------+---------------------+---------------------+---------+
> > ```
> >
> > Las fechas las pone Hibernate, `version` empieza en `0` y `precioFormateado` no aparece porque es `@Transient`.
> >
> > ⚠️ **`@Enumerated`: usar siempre `STRING`.** Con `ORDINAL` (el valor por defecto) se guarda la posición (`0`, `1`, `2`) y, si luego se reordena o se inserta un valor en el `enum`, los datos existentes cambian de significado.
> >
> > 💡 Esta versión de `CursoEntity` es **solo para este apartado**. En el resto de la guía se usa la versión mínima (`id` y `nombre`), que basta para ver las relaciones.
>
> </details>

</details>

<details open>
<summary><strong>🗺️ El ejemplo: la academia</strong></summary>

> Se usan cuatro entidades del mundo de la [guía del taller](01-guia-taller-gestion-de-cursos-con-bd.md), todas en `persistence.entity` (solo entidades: sin servicios ni API). Cada relación trae su **diagrama** y las **clases completas**.
>
> | Relación | Ejemplo | Lado **dueño** (tiene la clave) | Qué crea en MySQL |
> |---|---|---|---|
> | **Uno a uno** | Un estudiante tiene **un** carnet | `EstudianteEntity.carnet` | columna `estudiante.carnet_id` (con `UNIQUE`) |
> | **Uno a muchos** | Un docente dicta **varios** cursos | un sentido: `DocenteEntity.cursos`; doble sentido: `CursoEntity.docente` | columna `curso.docente_id` |
> | **Muchos a muchos** | Estudiantes y cursos | `EstudianteEntity.cursos` | tabla intermedia `estudiante_curso` |
>
> > 🔎 En el taller, el muchos a muchos entre estudiantes y cursos se resuelve con una **entidad intermedia** (`MatriculaEntity`), porque la matrícula necesita sus propios datos. Aquí se muestra la forma **directa**, sin datos propios.
>
> > 🧑‍💻 Los repositorios (`docentes`, `cursos`, `estudiantes`, `carnets`) y la clase `main` que los usa se arman **paso a paso** en la sección **«🧑‍💻 Práctica paso a paso»**, casi al final de esta guía.

</details>

<details open>
<summary><strong>1️⃣ Uno a uno — Estudiante y Carnet</strong></summary>

>
> <details open>
> <summary><strong>➡️ Un sentido</strong></summary>
>
> > ```mermaid
> > classDiagram
> >     direction LR
> >     class EstudianteEntity {
> >         <<Entity>>
> >         -Long id
> >         -String nombre
> >         -CarnetEntity carnet
> >     }
> >     class CarnetEntity {
> >         <<Entity>>
> >         -Long id
> >         -String codigo
> >     }
> >     EstudianteEntity "1" --> "1" CarnetEntity : carnet_id
> > ```
> >
> > La **flecha** indica quién conoce a quién: solo `EstudianteEntity` conoce a `CarnetEntity`. El carnet **no tiene** atributo `estudiante`.
> >
> > **Un sentido** — solo el estudiante conoce su carnet. Es el **dueño**: la clave foránea vive en su tabla. Clases completas:
> >
> > ```java
> > package com.academia.persistence.entity;
> >
> > import jakarta.persistence.*;
> > import lombok.Data;
> > import lombok.NoArgsConstructor;
> >
> > @Entity
> > @Table(name = "estudiante")
> > @Data
> > @NoArgsConstructor
> > public class EstudianteEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String nombre;
> >
> >     // Lado DUEÑO: la clave foránea carnet_id vive en la tabla estudiante
> >     @OneToOne(cascade = CascadeType.ALL)
> >     @JoinColumn(name = "carnet_id")
> >     private CarnetEntity carnet;
> >
> >     public EstudianteEntity(String nombre) {
> >         this.nombre = nombre;
> >     }
> > }
> > ```
> >
> > ```java
> > package com.academia.persistence.entity;
> >
> > import jakarta.persistence.*;
> > import lombok.Data;
> > import lombok.NoArgsConstructor;
> >
> > @Entity
> > @Table(name = "carnet")
> > @Data
> > @NoArgsConstructor
> > public class CarnetEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String codigo;
> >
> >     public CarnetEntity(String codigo) {
> >         this.codigo = codigo;
> >     }
> > }
> > ```
> >
> > - `@JoinColumn` crea `estudiante.carnet_id` y marca al **dueño**.
> > - `cascade = ALL`: al guardar el estudiante se guarda **también** su carnet (sin el `cascade` habría que guardar el carnet primero).
> >
> > <details open>
> > <summary><strong>🛠️ Al iniciar el servidor: lo que genera MySQL</strong></summary>
> >
> > Al **iniciar la aplicación**, Spring Data JPA (Hibernate) **crea las tablas** a partir de las entidades (con `spring.jpa.hibernate.ddl-auto=update`). Este es el SQL que genera:
> >
> > ```text
> > create table carnet (id bigint not null auto_increment, codigo varchar(255), primary key (id))
> > create table estudiante (carnet_id bigint, id bigint not null auto_increment, nombre varchar(255), primary key (id))
> > alter table estudiante add constraint UK… unique (carnet_id)                       ← esto lo hace «uno a uno»
> > alter table estudiante add constraint FK… foreign key (carnet_id) references carnet (id)
> > ```
> >
> > </details>
> >
> > <details open>
> > <summary><strong>▶️ Ejemplo: guardar y consultar datos</strong></summary>
> >
> > > <details open>
> > > <summary><strong>1️⃣ Guardar datos</strong></summary>
> > >
> > > > ```java
> > > > ana.setCarnet(new CarnetEntity("C-001"));   // se asigna en el dueño
> > > > estudiantes.save(ana);                       // cascade guarda también el carnet
> > > > ```
> > > >
> > > > **Así queda en la base de datos:**
> > > >
> > > > ```text
> > > > carnet                     estudiante
> > > > +----+--------+            +-----------+----+--------+
> > > > | id | codigo |            | carnet_id | id | nombre |
> > > > +----+--------+            +-----------+----+--------+
> > > > |  1 | C-001  |  <───────  |         1 |  1 | Ana    |
> > > > +----+--------+            +-----------+----+--------+
> > > > ```
> > > >
> > > > <details>
> > > > <summary>🔬 Ver el SQL del guardado</summary>
> > > >
> > > > ```text
> > > > insert into carnet (codigo) values ('C-001');
> > > > insert into estudiante (carnet_id, nombre) values (1, 'Ana');   -- el carnet se guarda primero para tener su id
> > > > ```
> > > >
> > > > </details>
> > >
> > > </details>
> > >
> > > <details open>
> > > <summary><strong>2️⃣ Consultar datos (en otra transacción)</strong></summary>
> > >
> > > > | Pregunta | Código | Resultado |
> > > > |---|---|---|
> > > > | ¿Cuál es el carnet de Ana? | `ana.getCarnet().getCodigo()` | **C-001** |
> > > > | ¿De quién es el carnet C-001? | `estudiantes.findByCarnetCodigo("C-001").orElseThrow().getNombre()` | **Ana** |
> > > >
> > > > > 💡 **La primera pregunta se resuelve navegando**: el estudiante tiene el atributo `carnet`.
> > > > >
> > > > > **La segunda no se puede navegar**: en un sentido `CarnetEntity` no tiene el atributo `estudiante`, así que no existe `carnet.getEstudiante()`. Por eso se **le pregunta al repositorio**.
> > > >
> > > > El método se declara en la interfaz del repositorio y **Spring Data lo implementa solo**, leyendo el nombre:
> > > >
> > > > ```java
> > > > Optional<EstudianteEntity> findByCarnetCodigo(String codigo);   // estudiante.carnet.codigo = ?
> > > > ```
> > > >
> > > > Se lee así: **find** (buscar) **By** (donde) **Carnet** (el carnet del estudiante) **Codigo** (tenga este código). O sea: «buscar el estudiante cuyo carnet tenga este código».
> > > >
> > > > Devuelve un `Optional` porque en uno a uno hay **como máximo un** estudiante por carnet (puede que no exista ninguno). `orElseThrow()` entrega el estudiante o lanza un error si no lo encuentra.
> > > >
> > > > <details>
> > > > <summary>🔬 Ver el SQL de las consultas</summary>
> > > >
> > > > ```text
> > > > ana.getCarnet().getCodigo()
> > > >   select id, codigo from carnet where id = 1                  -- 1 = carnet_id de Ana
> > > >   fila: (1, 'C-001')
> > > >
> > > > estudiantes.findByCarnetCodigo("C-001")
> > > >   select e.id, e.carnet_id, e.nombre
> > > >   from estudiante e left join carnet c on c.id = e.carnet_id
> > > >   where c.codigo = 'C-001'
> > > >   fila: (1, 1, 'Ana')
> > > > ```
> > > >
> > > > </details>
> > >
> > > </details>
> >
> > </details>
> </details>
>
> <details open>
> <summary><strong>↔️ Doble sentido</strong></summary>
>
> > ```mermaid
> > classDiagram
> >     direction LR
> >     class EstudianteEntity {
> >         <<Entity>>
> >         -Long id
> >         -String nombre
> >         -CarnetEntity carnet
> >     }
> >     class CarnetEntity {
> >         <<Entity>>
> >         -Long id
> >         -String codigo
> >         -EstudianteEntity estudiante
> >     }
> >     EstudianteEntity "1" -- "1" CarnetEntity : carnet_id
> > ```
> >
> > La línea **sin flecha** indica que se conocen los dos: el carnet **gana** el atributo `estudiante` (lado inverso).
> >
> > **Doble sentido** — `EstudianteEntity` **no cambia**; se agrega, **opcional**, el lado inverso en el carnet. Clase completa:
> >
> > ```java
> > package com.academia.persistence.entity;
> >
> > import jakarta.persistence.*;
> > import lombok.Data;
> > import lombok.EqualsAndHashCode;
> > import lombok.NoArgsConstructor;
> > import lombok.ToString;
> >
> > @Entity
> > @Table(name = "carnet")
> > @Data
> > @NoArgsConstructor
> > public class CarnetEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String codigo;
> >
> >     // Lado INVERSO (opcional): no crea columnas
> >     @OneToOne(mappedBy = "carnet")
> >     @ToString.Exclude
> >     @EqualsAndHashCode.Exclude
> >     private EstudianteEntity estudiante;
> >
> >     public CarnetEntity(String codigo) {
> >         this.codigo = codigo;
> >     }
> > }
> > ```
> >
> > - `mappedBy = "carnet"` = «la clave la maneja el atributo `carnet` de `EstudianteEntity`». **No crea columnas.**
> >
> > 🔎 Las tablas son **las mismas** que en un sentido: el lado inverso no crea columnas ni tablas.
> >
> > <details open>
> > <summary><strong>▶️ Ejemplo: guardar y consultar datos</strong></summary>
> >
> > > <details open>
> > > <summary><strong>1️⃣ Guardar datos</strong></summary>
> > >
> > > > ```java
> > > > ana.setCarnet(new CarnetEntity("C-001"));   // se asigna en el dueño
> > > > estudiantes.save(ana);                       // cascade guarda también el carnet
> > > > ```
> > > >
> > > > **Así queda en la base de datos:**
> > > >
> > > > ```text
> > > > carnet                     estudiante
> > > > +----+--------+            +-----------+----+--------+
> > > > | id | codigo |            | carnet_id | id | nombre |
> > > > +----+--------+            +-----------+----+--------+
> > > > |  1 | C-001  |  <───────  |         1 |  1 | Ana    |
> > > > +----+--------+            +-----------+----+--------+
> > > > ```
> > > >
> > > > <details>
> > > > <summary>🔬 Ver el SQL del guardado</summary>
> > > >
> > > > ```text
> > > > insert into carnet (codigo) values ('C-001');
> > > > insert into estudiante (carnet_id, nombre) values (1, 'Ana');   -- el carnet se guarda primero para tener su id
> > > > ```
> > > >
> > > > </details>
> > >
> > > </details>
> > >
> > > <details open>
> > > <summary><strong>2️⃣ Consultar datos (en otra transacción)</strong></summary>
> > >
> > > > | Pregunta | Código | Resultado |
> > > > |---|---|---|
> > > > | ¿Cuál es el carnet de Ana? | `ana.getCarnet().getCodigo()` | **C-001** |
> > > > | ¿De quién es el carnet C-001? | `carnet.getEstudiante().getNombre()` | **Ana** |
> > > >
> > > > > 💡 **Las dos preguntas se resuelven navegando.** Como `CarnetEntity` ahora tiene el atributo `estudiante`, no hace falta escribir ningún método en el repositorio.
> > > >
> > > > <details>
> > > > <summary>🔬 Ver el SQL de las consultas</summary>
> > > >
> > > > ```text
> > > > ana.getCarnet().getCodigo()
> > > >   select id, codigo from carnet where id = 1
> > > >   fila: (1, 'C-001')
> > > >
> > > > carnet.getEstudiante().getNombre()
> > > >   select e.id, e.carnet_id, e.nombre from estudiante e where e.carnet_id = 1     -- 1 = id del carnet
> > > >   fila: (1, 1, 'Ana')
> > > > ```
> > > >
> > > > </details>
> > >
> > > </details>
> >
> > </details>
> </details>
>
> <details open>
> <summary><strong>🗄️ Así queda en las tablas (datos de la práctica)</strong></summary>
>
> > ```text
> > carnet                      estudiante
> > +----+--------+             +----------+----+--------+
> > | id | codigo |             | carnet_id| id | nombre |
> > +----+--------+             +----------+----+--------+
> > |  1 | C-001  |  <────────  |        1 |  1 | Ana    |
> > +----+--------+             |     NULL |  2 | Luis   |   ← Luis no tiene carnet
> >                             +----------+----+--------+
> > ```
> >
> > La clave `carnet_id` está en `estudiante`; `UNIQUE` impide que otro estudiante use el carnet 1.
>
> </details>
>
> > 🔎 La restricción **`UNIQUE`** sobre `carnet_id` es lo que impide que dos estudiantes compartan el mismo carnet: eso **es** el «uno a uno».

</details>

<details open>
<summary><strong>2️⃣ Uno a muchos — Docente y Curso</strong></summary>

>
> Un docente dicta **varios** cursos. Se muestra primero **desde el docente** (un sentido) y después se agrega el curso → docente (doble sentido). La clave foránea siempre queda en la tabla `curso` (el lado «muchos»), pero **quién la escribe** depende del sentido.
>
> <details open>
> <summary><strong>➡️ Un sentido</strong></summary>
>
> > ```mermaid
> > classDiagram
> >     direction LR
> >     class DocenteEntity {
> >         <<Entity>>
> >         -Long id
> >         -String nombre
> >         -List~CursoEntity~ cursos
> >     }
> >     class CursoEntity {
> >         <<Entity>>
> >         -Long id
> >         -String nombre
> >     }
> >     DocenteEntity "1" --> "*" CursoEntity : docente_id
> > ```
> >
> > La **flecha** va del docente a sus cursos: solo `DocenteEntity` conoce a `CursoEntity`. El curso **no tiene** atributo `docente`.
> >
> > **Un sentido** — **desde el docente**: un docente tiene **varios** cursos y el curso no sabe de quién es. Clases completas:
> >
> > ```java
> > package com.academia.persistence.entity;
> >
> > import jakarta.persistence.*;
> > import lombok.Data;
> > import java.util.ArrayList;
> > import java.util.List;
> > import lombok.NoArgsConstructor;
> >
> > @Entity
> > @Table(name = "docente")
> > @Data
> > @NoArgsConstructor
> > public class DocenteEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String nombre;
> >
> >     // Lado DUEÑO (un sentido): tiene @JoinColumn. La columna docente_id se crea en la tabla curso
> >     @OneToMany(cascade = CascadeType.ALL)
> >     @JoinColumn(name = "docente_id")
> >     private List<CursoEntity> cursos = new ArrayList<>();
> >
> >     public DocenteEntity(String nombre) {
> >         this.nombre = nombre;
> >     }
> > }
> > ```
> >
> > ```java
> > package com.academia.persistence.entity;
> >
> > import jakarta.persistence.*;
> > import lombok.Data;
> > import lombok.NoArgsConstructor;
> >
> > @Entity
> > @Table(name = "curso")
> > @Data
> > @NoArgsConstructor
> > public class CursoEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String nombre;
> >
> >     // CursoEntity no sabe quién es su docente
> >
> >     public CursoEntity(String nombre) {
> >         this.nombre = nombre;
> >     }
> > }
> > ```
> >
> > - `@OneToMany` + `@JoinColumn(name = "docente_id")`: la clave `docente_id` se crea en la tabla `curso`, aunque `CursoEntity` no tenga ese atributo. Como `@JoinColumn` está aquí, **el dueño es `DocenteEntity`**.
> > - `cascade = ALL`: al guardar el docente se guardan también sus cursos.
> > - Se comprobó en MySQL: Hibernate hace primero un `insert into curso` por cada curso y después un `update curso set docente_id=?` por cada uno. Es normal en esta variante.
> >
> > <details open>
> > <summary><strong>🛠️ Al iniciar el servidor: lo que genera MySQL</strong></summary>
> >
> > Al **iniciar la aplicación**, Spring Data JPA (Hibernate) **crea las tablas** a partir de las entidades (con `spring.jpa.hibernate.ddl-auto=update`). Este es el SQL que genera:
> >
> > ```text
> > create table docente (id bigint not null auto_increment, nombre varchar(255), primary key (id))
> > create table curso (docente_id bigint, id bigint not null auto_increment, nombre varchar(255), primary key (id))
> > alter table curso add constraint FK… foreign key (docente_id) references docente (id)
> > ```
> >
> > </details>
> >
> > <details open>
> > <summary><strong>▶️ Ejemplo: guardar y consultar datos</strong></summary>
> >
> > > <details open>
> > > <summary><strong>1️⃣ Guardar datos</strong></summary>
> > >
> > > > ```java
> > > > marta.getCursos().add(java);
> > > > marta.getCursos().add(spring);
> > > > docentes.save(marta);      // guarda a Marta y, por cascade, sus cursos
> > > > ```
> > > >
> > > > **Así queda en la base de datos:**
> > > >
> > > > ```text
> > > > docente                 curso
> > > > +----+--------+         +------------+----+--------+
> > > > | id | nombre |         | docente_id | id | nombre |
> > > > +----+--------+         +------------+----+--------+
> > > > |  1 | Marta  |  <────  |          1 |  1 | Java   |
> > > > +----+--------+         |          1 |  2 | Spring |
> > > >                         +------------+----+--------+
> > > > ```
> > > >
> > > > <details>
> > > > <summary>🔬 Ver el SQL del guardado</summary>
> > > >
> > > > ```text
> > > > insert into docente (nombre) values ('Marta');
> > > > insert into curso (nombre) values ('Java');            -- el curso entra SIN docente_id
> > > > insert into curso (nombre) values ('Spring');
> > > > update curso set docente_id = 1 where id = 1;          -- Hibernate completa la clave después
> > > > update curso set docente_id = 1 where id = 2;
> > > > ```
> > > >
> > > > </details>
> > >
> > > </details>
> > >
> > > <details open>
> > > <summary><strong>2️⃣ Consultar datos (en otra transacción)</strong></summary>
> > >
> > > > | Pregunta | Código | Resultado |
> > > > |---|---|---|
> > > > | ¿Qué cursos dicta Marta? | `marta.getCursos()` | **[Java, Spring]** |
> > > > | ¿Quién dicta Java? | `docentes.findByCursosNombre("Java")` | **[Marta]** (una lista) |
> > > >
> > > > > 💡 **La primera pregunta se resuelve navegando**: el docente tiene el atributo `cursos`.
> > > > >
> > > > > **La segunda no se puede navegar**: en un sentido `CursoEntity` no tiene el atributo `docente`. Por eso se **le pregunta al repositorio**.
> > > >
> > > > El método se declara en la interfaz del repositorio y **Spring Data lo implementa solo**, leyendo el nombre:
> > > >
> > > > ```java
> > > > List<DocenteEntity> findByCursosNombre(String nombre);   // docente.cursos.nombre = ?
> > > > ```
> > > >
> > > > Se lee así: **find** (buscar) **By** (donde) **Cursos** (los cursos del docente) **Nombre** (tengan este nombre). O sea: «buscar los docentes que dicten un curso con este nombre».
> > > >
> > > > <details>
> > > > <summary>🔬 Ver el SQL de las consultas</summary>
> > > >
> > > > ```text
> > > > marta.getCursos()
> > > >   select docente_id, id, nombre from curso where docente_id = 1
> > > >   filas: (1, 1, 'Java'), (1, 2, 'Spring')
> > > >
> > > > docentes.findByCursosNombre("Java")
> > > >   select d.id, d.nombre
> > > >   from docente d left join curso c on d.id = c.docente_id
> > > >   where c.nombre = 'Java'
> > > >   fila: (1, 'Marta')
> > > > ```
> > > >
> > > > </details>
> > >
> > > </details>
> >
> > </details>
> </details>
>
> <details open>
> <summary><strong>↔️ Doble sentido</strong></summary>
>
> > ```mermaid
> > classDiagram
> >     direction LR
> >     class DocenteEntity {
> >         <<Entity>>
> >         -Long id
> >         -String nombre
> >         -List~CursoEntity~ cursos
> >     }
> >     class CursoEntity {
> >         <<Entity>>
> >         -Long id
> >         -String nombre
> >         -DocenteEntity docente
> >     }
> >     DocenteEntity "1" -- "*" CursoEntity : docente_id
> > ```
> >
> > La línea **sin flecha** indica que se conocen los dos: el curso **gana** el atributo `docente` y pasa a ser el dueño; el docente queda como lado inverso.
> >
> > **Doble sentido** — se agrega `@ManyToOne` en el curso y **el dueño pasa a ser `CursoEntity`**. El docente deja de tener `@JoinColumn` y queda como lado **inverso** (`mappedBy`). Clases completas:
> >
> > ```java
> > package com.academia.persistence.entity;
> >
> > import jakarta.persistence.*;
> > import lombok.Data;
> > import lombok.NoArgsConstructor;
> >
> > @Entity
> > @Table(name = "curso")
> > @Data
> > @NoArgsConstructor
> > public class CursoEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String nombre;
> >
> >     // Lado DUEÑO (doble sentido): ahora la clave la escribe el curso
> >     @ManyToOne
> >     @JoinColumn(name = "docente_id")
> >     private DocenteEntity docente;
> >
> >     public CursoEntity(String nombre) {
> >         this.nombre = nombre;
> >     }
> > }
> > ```
> >
> > ```java
> > package com.academia.persistence.entity;
> >
> > import jakarta.persistence.*;
> > import lombok.Data;
> > import java.util.ArrayList;
> > import java.util.List;
> > import lombok.EqualsAndHashCode;
> > import lombok.NoArgsConstructor;
> > import lombok.ToString;
> >
> > @Entity
> > @Table(name = "docente")
> > @Data
> > @NoArgsConstructor
> > public class DocenteEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String nombre;
> >
> >     // Lado INVERSO (opcional): ya no lleva @JoinColumn, solo mappedBy
> >     @OneToMany(mappedBy = "docente")
> >     @ToString.Exclude
> >     @EqualsAndHashCode.Exclude
> >     private List<CursoEntity> cursos = new ArrayList<>();
> >
> >     public DocenteEntity(String nombre) {
> >         this.nombre = nombre;
> >     }
> >
> >     // Mantiene sincronizados los dos lados
> >     public void agregarCurso(CursoEntity curso) {
> >         cursos.add(curso);
> >         curso.setDocente(this);
> >     }
> > }
> > ```
> >
> > > ⚠️ Al pasar a doble sentido **cambia el dueño**: hay que quitar `@JoinColumn` y `cascade` del `@OneToMany` y poner `mappedBy = "docente"`. Si los dos lados llevaran `@JoinColumn`, los dos intentarían escribir `docente_id`. Una ventaja: sin el `UPDATE` extra, el curso se guarda ya con su `docente_id`.
> >
> > 🔎 Las tablas son **las mismas** que en un sentido: el lado inverso no crea columnas ni tablas.
> >
> > <details open>
> > <summary><strong>▶️ Ejemplo: guardar y consultar datos</strong></summary>
> >
> > > <details open>
> > > <summary><strong>1️⃣ Guardar datos</strong></summary>
> > >
> > > > ```java
> > > > marta = docentes.save(new DocenteEntity("Marta"));
> > > > marta.agregarCurso(java);      // sincroniza los dos lados: curso.docente = marta
> > > > marta.agregarCurso(spring);
> > > > cursos.save(java);             // el dueño es el curso: se guarda cada curso
> > > > cursos.save(spring);
> > > > ```
> > > >
> > > > **Así queda en la base de datos:**
> > > >
> > > > ```text
> > > > docente                 curso
> > > > +----+--------+         +------------+----+--------+
> > > > | id | nombre |         | docente_id | id | nombre |
> > > > +----+--------+         +------------+----+--------+
> > > > |  1 | Marta  |  <────  |          1 |  1 | Java   |
> > > > +----+--------+         |          1 |  2 | Spring |
> > > >                         +------------+----+--------+
> > > > ```
> > > >
> > > > <details>
> > > > <summary>🔬 Ver el SQL del guardado</summary>
> > > >
> > > > ```text
> > > > insert into docente (nombre) values ('Marta');
> > > > insert into curso (docente_id, nombre) values (1, 'Java');     -- el curso entra CON docente_id
> > > > insert into curso (docente_id, nombre) values (1, 'Spring');   -- sin update extra
> > > > ```
> > > >
> > > > </details>
> > >
> > > </details>
> > >
> > > <details open>
> > > <summary><strong>2️⃣ Consultar datos (en otra transacción)</strong></summary>
> > >
> > > > | Pregunta | Código | Resultado |
> > > > |---|---|---|
> > > > | ¿Qué cursos dicta Marta? | `marta.getCursos()` | **[Java, Spring]** |
> > > > | ¿Quién dicta Java? | `java.getDocente().getNombre()` | **Marta** |
> > > >
> > > > > 💡 **Las dos preguntas se resuelven navegando.** El docente tiene `cursos` y el curso tiene `docente`: no hace falta ningún método extra en el repositorio.
> > > >
> > > > <details>
> > > > <summary>🔬 Ver el SQL de las consultas</summary>
> > > >
> > > > ```text
> > > > marta.getCursos()
> > > >   select docente_id, id, nombre from curso where docente_id = 1
> > > >   filas: (1, 1, 'Java'), (1, 2, 'Spring')
> > > >
> > > > java.getDocente().getNombre()
> > > >   select id, docente_id, nombre from curso                      -- el curso trae docente_id = 1
> > > >   select id, nombre from docente where id = 1                   -- si Marta aún no está cargada
> > > >   fila: (1, 'Marta')
> > > > ```
> > > >
> > > > </details>
> > >
> > > </details>
> >
> > </details>
> </details>
>
> <details open>
> <summary><strong>🗄️ Así queda en las tablas</strong></summary>
>
> > ```text
> > docente                     curso
> > +----+--------+             +-----------+----+--------+
> > | id | nombre |             | docente_id| id | nombre |
> > +----+--------+             +-----------+----+--------+
> > |  1 | Marta  |  <────────  |         1 |  1 | Java   |
> > +----+--------+             |         1 |  2 | Spring |
> >                             |      NULL |  3 | HTML   |   ← se agregó solo a docente.getCursos()
> >                             +-----------+----+--------+
> > ```
> >
> > La clave `docente_id` está en `curso` (lado «muchos»); `docente` **no** tiene ninguna columna de cursos. Es igual en un sentido y en doble sentido.
>
> </details>
>
> > ⚠️ **En doble sentido se asigna del lado dueño (el curso).** `docente.getCursos().add(curso)` **no** escribe `docente_id`; hay que hacer `curso.setDocente(docente)`. Se comprobó: un curso agregado **solo** a la lista del docente quedó con `docente_id = NULL`. En doble sentido, se mantienen **los dos lados sincronizados** con el método `agregarCurso` de `DocenteEntity` (ya incluido arriba).

</details>

<details open>
<summary><strong>3️⃣ Muchos a muchos — Estudiante y Curso</strong></summary>

>
> Ningún lado puede guardar la clave en su propia tabla: hace falta una **tabla intermedia** con las dos claves foráneas. JPA la crea automáticamente.
>
> <details open>
> <summary><strong>➡️ Un sentido</strong></summary>
>
> > ```mermaid
> > classDiagram
> >     direction LR
> >     class EstudianteEntity {
> >         <<Entity>>
> >         -Long id
> >         -String nombre
> >         -Set~CursoEntity~ cursos
> >     }
> >     class CursoEntity {
> >         <<Entity>>
> >         -Long id
> >         -String nombre
> >     }
> >     EstudianteEntity "*" --> "*" CursoEntity : estudiante_curso
> > ```
> >
> > La **flecha** va del estudiante a sus cursos: solo `EstudianteEntity` conoce a `CursoEntity`. El curso **no tiene** atributo `estudiantes`.
> >
> > **Un sentido** — solo el estudiante conoce sus cursos. Es el **dueño** (`@JoinTable`). Clases completas:
> >
> > ```java
> > package com.academia.persistence.entity;
> >
> > import jakarta.persistence.*;
> > import java.util.HashSet;
> > import java.util.Set;
> > import lombok.Data;
> > import lombok.NoArgsConstructor;
> >
> > @Entity
> > @Table(name = "estudiante")
> > @Data
> > @NoArgsConstructor
> > public class EstudianteEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String nombre;
> >
> >     // Lado DUEÑO: JPA crea la tabla intermedia estudiante_curso
> >     @ManyToMany
> >     @JoinTable(name = "estudiante_curso",
> >             joinColumns = @JoinColumn(name = "estudiante_id"),
> >             inverseJoinColumns = @JoinColumn(name = "curso_id"))
> >     private Set<CursoEntity> cursos = new HashSet<>();
> >
> >     public EstudianteEntity(String nombre) {
> >         this.nombre = nombre;
> >     }
> > }
> > ```
> >
> > ```java
> > package com.academia.persistence.entity;
> >
> > import jakarta.persistence.*;
> > import lombok.Data;
> > import lombok.NoArgsConstructor;
> >
> > @Entity
> > @Table(name = "curso")
> > @Data
> > @NoArgsConstructor
> > public class CursoEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String nombre;
> >
> >     public CursoEntity(String nombre) {
> >         this.nombre = nombre;
> >     }
> > }
> > ```
> >
> > - `name`: nombre de la tabla intermedia. `joinColumns`: la columna que apunta a **este** lado (`estudiante_id`). `inverseJoinColumns`: la que apunta al **otro** (`curso_id`).
> > - Se usa un `Set`: un curso **no puede repetirse** para el mismo estudiante.
> >
> > <details open>
> > <summary><strong>🛠️ Al iniciar el servidor: lo que genera MySQL</strong></summary>
> >
> > Al **iniciar la aplicación**, Spring Data JPA (Hibernate) **crea las tablas** a partir de las entidades (con `spring.jpa.hibernate.ddl-auto=update`). Este es el SQL que genera:
> >
> > ```text
> > create table estudiante_curso (curso_id bigint not null, estudiante_id bigint not null, primary key (curso_id, estudiante_id))
> > alter table estudiante_curso add constraint FK… foreign key (curso_id)      references curso (id)
> > alter table estudiante_curso add constraint FK… foreign key (estudiante_id) references estudiante (id)
> > ```
> >
> > </details>
> >
> > <details open>
> > <summary><strong>▶️ Ejemplo: guardar y consultar datos</strong></summary>
> >
> > > <details open>
> > > <summary><strong>1️⃣ Guardar datos</strong></summary>
> > >
> > > > ```java
> > > > ana.getCursos().add(java);     // se agrega en el dueño
> > > > ana.getCursos().add(spring);
> > > > luis.getCursos().add(java);
> > > > estudiantes.save(ana);
> > > > estudiantes.save(luis);
> > > > ```
> > > >
> > > > **Así queda en la base de datos:**
> > > >
> > > > ```text
> > > > estudiante          estudiante_curso (tabla intermedia)        curso
> > > > +----+--------+     +---------------+----------+               +----+--------+
> > > > | id | nombre |     | estudiante_id | curso_id |               | id | nombre |
> > > > +----+--------+     +---------------+----------+               +----+--------+
> > > > |  1 | Ana    | <── |             1 |        1 | ──>           |  1 | Java   |
> > > > |  2 | Luis   | <── |             1 |        2 | ──>           |  2 | Spring |
> > > > +----+--------+     |             2 |        1 |               +----+--------+
> > > >                     +---------------+----------+
> > > > (solo se muestran las columnas de esta relación)
> > > > ```
> > > >
> > > > <details>
> > > > <summary>🔬 Ver el SQL del guardado</summary>
> > > >
> > > > ```text
> > > > insert into estudiante_curso (estudiante_id, curso_id) values (1, 1);
> > > > insert into estudiante_curso (estudiante_id, curso_id) values (1, 2);
> > > > insert into estudiante_curso (estudiante_id, curso_id) values (2, 1);
> > > > ```
> > > >
> > > > </details>
> > >
> > > >
> > > > <details open>
> > > > <summary><strong>🔍 Entender <code>ana.getCursos().add(java)</code></strong></summary>
> > > >
> > > > `ana.getCursos()` devuelve **la colección de cursos que está dentro de `ana`**: un `Set<CursoEntity>` que empieza **vacío**. `.add(java)` mete en esa colección el objeto `java`, el `CursoEntity` que se creó antes con `new CursoEntity("Java")`. (`java` es solo el nombre de una variable.)
> > > >
> > > > | Después de ejecutar… | `ana.getCursos()` | `luis.getCursos()` |
> > > > |---|---|---|
> > > > | `new EstudianteEntity("Ana")` y `("Luis")` | `[]` | `[]` |
> > > > | `ana.getCursos().add(java)` | `[Java]` | `[]` |
> > > > | `ana.getCursos().add(spring)` | `[Java, Spring]` | `[]` |
> > > > | `luis.getCursos().add(java)` | `[Java, Spring]` | `[Java]` |
> > > >
> > > > Hasta aquí todo está **solo en memoria**. Al hacer `estudiantes.save(ana)` y `estudiantes.save(luis)`, JPA lee esas colecciones y escribe **una fila por curso** en la tabla intermedia:
> > > >
> > > > ```text
> > > > ana.cursos   = [Java, Spring]            estudiante_curso
> > > > luis.cursos  = [Java]                    +---------------+----------+
> > > >                                          | estudiante_id | curso_id |
> > > >                                          +---------------+----------+
> > > >                                          |             1 |        1 |   ← Ana – Java
> > > >                                          |             1 |        2 |   ← Ana – Spring
> > > >                                          |             2 |        1 |   ← Luis – Java
> > > >                                          +---------------+----------+
> > > > ```
> > > >
> > > > > 🔎 Al **consultar**, `ana` se carga de la base (`findById`) y JPA llena su colección con esas filas: por eso allí `ana.getCursos()` ya devuelve `[Java, Spring]`.
> > > >
> > > > </details>
> > > >
> > > </details>
> > >
> > > <details open>
> > > <summary><strong>2️⃣ Consultar datos (en otra transacción)</strong></summary>
> > >
> > > > | Pregunta | Código | Resultado |
> > > > |---|---|---|
> > > > | ¿Qué cursos tiene Ana? | `ana.getCursos()` | **[Java, Spring]** |
> > > > | ¿Qué estudiantes tiene Java? | `estudiantes.findByCursosNombre("Java")` | **[Ana, Luis]** (una lista) |
> > > >
> > > > > 💡 **La primera pregunta se resuelve navegando**: el estudiante tiene el atributo `cursos`.
> > > > >
> > > > > **La segunda no se puede navegar**: en un sentido `CursoEntity` no tiene el atributo `estudiantes`. Por eso se **le pregunta al repositorio**.
> > > >
> > > > El método se declara en la interfaz del repositorio y **Spring Data lo implementa solo**, leyendo el nombre:
> > > >
> > > > ```java
> > > > List<EstudianteEntity> findByCursosNombre(String nombre);   // estudiante.cursos.nombre = ?
> > > > ```
> > > >
> > > > Se lee así: **find** (buscar) **By** (donde) **Cursos** (los cursos del estudiante) **Nombre** (tengan este nombre). O sea: «buscar los estudiantes que cursen un curso con este nombre».
> > > >
> > > > <details>
> > > > <summary>🔬 Ver el SQL de las consultas</summary>
> > > >
> > > > ```text
> > > > ana.getCursos()
> > > >   select ec.estudiante_id, c.id, c.nombre
> > > >   from estudiante_curso ec join curso c on c.id = ec.curso_id
> > > >   where ec.estudiante_id = 1
> > > >   filas: (1, 1, 'Java'), (1, 2, 'Spring')
> > > >
> > > > estudiantes.findByCursosNombre("Java")
> > > >   select e.id, e.carnet_id, e.nombre
> > > >   from estudiante e
> > > >   left join estudiante_curso ec on e.id = ec.estudiante_id
> > > >   left join curso c on c.id = ec.curso_id
> > > >   where c.nombre = 'Java'
> > > >   filas: (1, NULL, 'Ana'), (2, NULL, 'Luis')
> > > > ```
> > > >
> > > > </details>
> > >
> > > </details>
> >
> > </details>
> </details>
>
> <details open>
> <summary><strong>↔️ Doble sentido</strong></summary>
>
> > ```mermaid
> > classDiagram
> >     direction LR
> >     class EstudianteEntity {
> >         <<Entity>>
> >         -Long id
> >         -String nombre
> >         -Set~CursoEntity~ cursos
> >     }
> >     class CursoEntity {
> >         <<Entity>>
> >         -Long id
> >         -String nombre
> >         -Set~EstudianteEntity~ estudiantes
> >     }
> >     EstudianteEntity "*" -- "*" CursoEntity : estudiante_curso
> > ```
> >
> > La línea **sin flecha** indica que se conocen los dos: el curso **gana** el atributo `estudiantes` (lado inverso).
> >
> > **Doble sentido** — `EstudianteEntity` **no cambia**; el curso también ve a sus estudiantes (lado **inverso**, sin repetir la tabla). Clase completa:
> >
> > ```java
> > package com.academia.persistence.entity;
> >
> > import jakarta.persistence.*;
> > import java.util.HashSet;
> > import java.util.Set;
> > import lombok.Data;
> > import lombok.EqualsAndHashCode;
> > import lombok.NoArgsConstructor;
> > import lombok.ToString;
> >
> > @Entity
> > @Table(name = "curso")
> > @Data
> > @NoArgsConstructor
> > public class CursoEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String nombre;
> >
> >     // Lado INVERSO (opcional): la tabla intermedia ya la define EstudianteEntity.cursos
> >     @ManyToMany(mappedBy = "cursos")
> >     @ToString.Exclude
> >     @EqualsAndHashCode.Exclude
> >     private Set<EstudianteEntity> estudiantes = new HashSet<>();
> >
> >     public CursoEntity(String nombre) {
> >         this.nombre = nombre;
> >     }
> > }
> > ```
> >
> > 🔎 Las tablas son **las mismas** que en un sentido: el lado inverso no crea columnas ni tablas.
> >
> > <details open>
> > <summary><strong>▶️ Ejemplo: guardar y consultar datos</strong></summary>
> >
> > > <details open>
> > > <summary><strong>1️⃣ Guardar datos</strong></summary>
> > >
> > > > ```java
> > > > ana.getCursos().add(java);     // se registra igual: en el dueño
> > > > ana.getCursos().add(spring);
> > > > luis.getCursos().add(java);
> > > > estudiantes.save(ana);
> > > > estudiantes.save(luis);
> > > > ```
> > > >
> > > > **Así queda en la base de datos:**
> > > >
> > > > ```text
> > > > estudiante          estudiante_curso (tabla intermedia)        curso
> > > > +----+--------+     +---------------+----------+               +----+--------+
> > > > | id | nombre |     | estudiante_id | curso_id |               | id | nombre |
> > > > +----+--------+     +---------------+----------+               +----+--------+
> > > > |  1 | Ana    | <── |             1 |        1 | ──>           |  1 | Java   |
> > > > |  2 | Luis   | <── |             1 |        2 | ──>           |  2 | Spring |
> > > > +----+--------+     |             2 |        1 |               +----+--------+
> > > >                     +---------------+----------+
> > > > (solo se muestran las columnas de esta relación)
> > > > ```
> > > >
> > > > <details>
> > > > <summary>🔬 Ver el SQL del guardado</summary>
> > > >
> > > > ```text
> > > > insert into estudiante_curso (estudiante_id, curso_id) values (1, 1);
> > > > insert into estudiante_curso (estudiante_id, curso_id) values (1, 2);
> > > > insert into estudiante_curso (estudiante_id, curso_id) values (2, 1);
> > > > ```
> > > >
> > > > </details>
> > >
> > > >
> > > > <details open>
> > > > <summary><strong>🔍 Entender <code>ana.getCursos().add(java)</code></strong></summary>
> > > >
> > > > `ana.getCursos()` devuelve **la colección de cursos que está dentro de `ana`**: un `Set<CursoEntity>` que empieza **vacío**. `.add(java)` mete en esa colección el objeto `java`, el `CursoEntity` que se creó antes con `new CursoEntity("Java")`. (`java` es solo el nombre de una variable.)
> > > >
> > > > | Después de ejecutar… | `ana.getCursos()` | `luis.getCursos()` |
> > > > |---|---|---|
> > > > | `new EstudianteEntity("Ana")` y `("Luis")` | `[]` | `[]` |
> > > > | `ana.getCursos().add(java)` | `[Java]` | `[]` |
> > > > | `ana.getCursos().add(spring)` | `[Java, Spring]` | `[]` |
> > > > | `luis.getCursos().add(java)` | `[Java, Spring]` | `[Java]` |
> > > >
> > > > Hasta aquí todo está **solo en memoria**. Al hacer `estudiantes.save(ana)` y `estudiantes.save(luis)`, JPA lee esas colecciones y escribe **una fila por curso** en la tabla intermedia:
> > > >
> > > > ```text
> > > > ana.cursos   = [Java, Spring]            estudiante_curso
> > > > luis.cursos  = [Java]                    +---------------+----------+
> > > >                                          | estudiante_id | curso_id |
> > > >                                          +---------------+----------+
> > > >                                          |             1 |        1 |   ← Ana – Java
> > > >                                          |             1 |        2 |   ← Ana – Spring
> > > >                                          |             2 |        1 |   ← Luis – Java
> > > >                                          +---------------+----------+
> > > > ```
> > > >
> > > > > 🔎 Al **consultar**, `ana` se carga de la base (`findById`) y JPA llena su colección con esas filas: por eso allí `ana.getCursos()` ya devuelve `[Java, Spring]`.
> > > >
> > > > </details>
> > > >
> > > </details>
> > >
> > > <details open>
> > > <summary><strong>2️⃣ Consultar datos (en otra transacción)</strong></summary>
> > >
> > > > | Pregunta | Código | Resultado |
> > > > |---|---|---|
> > > > | ¿Qué cursos tiene Ana? | `ana.getCursos()` | **[Java, Spring]** |
> > > > | ¿Qué estudiantes tiene Java? | `java.getEstudiantes()` | **[Ana, Luis]** |
> > > >
> > > > > 💡 **Las dos preguntas se resuelven navegando.** El estudiante tiene `cursos` y el curso tiene `estudiantes`: no hace falta ningún método extra en el repositorio.
> > > >
> > > > <details>
> > > > <summary>🔬 Ver el SQL de las consultas</summary>
> > > >
> > > > ```text
> > > > ana.getCursos()
> > > >   select ec.estudiante_id, c.id, c.nombre
> > > >   from estudiante_curso ec join curso c on c.id = ec.curso_id
> > > >   where ec.estudiante_id = 1
> > > >   filas: (1, 1, 'Java'), (1, 2, 'Spring')
> > > >
> > > > java.getEstudiantes()
> > > >   select ec.curso_id, e.id, e.nombre
> > > >   from estudiante_curso ec join estudiante e on e.id = ec.estudiante_id
> > > >   where ec.curso_id = 1
> > > >   filas: (1, 1, 'Ana'), (1, 2, 'Luis')
> > > > ```
> > > >
> > > > </details>
> > >
> > > </details>
> >
> > </details>
> </details>
>
> <details open>
> <summary><strong>🗄️ Así queda en las tablas</strong></summary>
>
> > ```text
> > estudiante            estudiante_curso (tabla intermedia)      curso
> > +----+--------+       +---------------+----------+             +----+--------+
> > | id | nombre |       | estudiante_id | curso_id |             | id | nombre |
> > +----+--------+       +---------------+----------+             +----+--------+
> > |  1 | Ana    | <──── |             1 |        1 | ────>       |  1 | Java   |
> > |  2 | Luis   | <──── |             1 |        2 | ────>       |  2 | Spring |
> > +----+--------+       |             2 |        1 |             +----+--------+
> >                       +---------------+----------+
> > ```
> >
> > Ana cursa Java y Spring; Luis, solo Java. Ninguna de las dos tablas principales guarda la relación: solo la tabla intermedia.
>
> </details>
>
> > 🔎 La **clave primaria compuesta** `(curso_id, estudiante_id)` impide repetir un par a nivel de base.
> >
> > 💡 **Cuándo pasar a una entidad intermedia:** si la relación necesita **sus propios datos** (la fecha de matrícula, una nota, un estado), un `@ManyToMany` simple no tiene dónde guardarlos. Se reemplaza por una entidad con **dos `@ManyToOne`**, como `MatriculaEntity` en el taller ([Ejemplo 04 del M4](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/04-manyToMany-y-entidad-intermedia.md)).

</details>

<details open>
<summary><strong>↔️ Un sentido o doble sentido: ¿cuál elegir?</strong></summary>

> ### 🔄 ¿Qué hace el doble sentido? (ejemplo Estudiante ↔ Carnet)
>
> ```java
> estudiante.getCarnet()   // ✅ un sentido: el dueño tiene la clave carnet_id
> carnet.getEstudiante()   // ❌ no existe... hasta que se agrega el lado inverso
> ```
>
> Con el lado inverso (`@OneToOne(mappedBy = "carnet")` en `CarnetEntity`) las dos líneas funcionan.
>
> En el diagrama, las flechas continuas son lo que **se puede navegar en Java**; la flecha punteada es la **única que existe en la base de datos** (la columna `carnet_id`):
>
> ```mermaid
> flowchart LR
>     subgraph UN["➡️ Un sentido"]
>         direction LR
>         A1["Ana<br/>(EstudianteEntity)"] -->|"getCarnet()"| C1["C-001<br/>(CarnetEntity)"]
>     end
>     subgraph DOBLE["↔️ Doble sentido"]
>         direction LR
>         A2["Ana<br/>(EstudianteEntity)"] -->|"getCarnet()"| C2["C-001<br/>(CarnetEntity)"]
>         C2 -->|"getEstudiante()"| A2
>     end
>     A1 -.->|"columna carnet_id"| C1
>     A2 -.->|"columna carnet_id"| C2
> ```
>
> Cabe notar que la columna `carnet_id` es la misma en los dos casos: el doble sentido **solo agrega la flecha de vuelta en Java**, no cambia las tablas.
>
> | | |
> |---|---|
> | **Qué hace** | Permite **navegar en sentido contrario**: al pedir `carnet.getEstudiante()`, JPA busca al estudiante cuyo `carnet_id` sea el de ese carnet. |
> | **Qué NO hace** | No crea columnas ni tablas, y **no escribe**: `carnet.setEstudiante(ana)` no cambia la base. Siempre se asigna con `estudiante.setCarnet(carnet)`. |
> | **Para qué sirve** | Evitar una consulta extra. Ejemplo: se escanea un carnet y se muestra de quién es → `carnet.getEstudiante().getNombre()` → `"Ana"`. |
>
> **¿Cuándo es necesario?** Casi nunca; es una comodidad.
>
> | Situación | ¿Doble sentido? |
> |---|---|
> | Solo se pregunta «¿cuál es el carnet de este estudiante?» | **No** |
> | Se pregunta «¿de quién es este carnet?» pocas veces | **No**: se usa un método del repositorio, por ejemplo `findByCarnetCodigo("C-001")` en `EstudianteJpaRepository` |
> | Se recorre **siempre en las dos direcciones** y se quiere `carnet.getEstudiante()` directo | **Sí** |
>
> > ⚠️ **Costo:** hay que mantener los dos lados coherentes, excluir el inverso de `toString`/`equals` (con `@Data`) y evitar el bucle infinito en JSON.
>
> | | **Un sentido** (solo el dueño) | **Doble sentido** (dueño + inverso) |
> |---|---|---|
> | Qué se agrega | Solo la notación del dueño | Además, la del inverso con `mappedBy` |
> | Cambia la base de datos | — | **No**: el inverso no crea columnas ni tablas |
> | Se puede navegar | Solo desde el dueño | Desde los dos lados |
> | Costo | El más simple | Hay que **sincronizar** ambos lados y evitar ciclos en JSON y `toString` |
> | Conviene cuando… | Solo se necesita consultar desde un lado | De verdad se necesita recorrer en las dos direcciones |
>
> **Reglas para no equivocarse:**
>
> 1. **Se empieza en un sentido.** El inverso se agrega solo si se va a usar.
> 2. **Siempre se escribe del lado dueño.** El inverso solo sirve para **leer**.
> 3. **`mappedBy` lleva el nombre exacto** del atributo del dueño (`"docente"`, `"carnet"`, `"cursos"`).
> 4. **Con `@Data` y doble sentido, se excluye el lado inverso** con `@ToString.Exclude` y `@EqualsAndHashCode.Exclude`: si no, `toString`, `equals` y `hashCode` se llaman entre sí **sin fin** (`StackOverflowError`). Así están las clases de esta guía.
> 5. Al devolver entidades como **JSON**, el doble sentido genera un bucle infinito: por eso en la [guía 2 de la tienda](03-guia-tienda-2-uno-a-muchos.md) se usa `@JsonIgnore`, y en la [Fase II](05-guia-tienda-fase-2-buenas-practicas.md) se separan modelo y entidad.

</details>

<details open>
<summary><strong>🐢⚡ LAZY y EAGER: cómo se cargan los datos</strong></summary>

> Cuando JPA lee una entidad, hay que decidir **cuándo** trae lo que está relacionado con ella (el carnet de Ana, sus cursos…).
>
> | | Cuándo carga lo relacionado | Idea |
> |---|---|---|
> | **EAGER** («ansioso») | **Al mismo tiempo** que la entidad principal | Trae todo de una vez |
> | **LAZY** («perezoso») | **Solo cuando se pide** (`getXxx()`) | Trae lo básico y espera a que se use |
>
> **No hace falta escribirlos**: cada relación ya trae un valor por defecto.
>
> | Relación | Por defecto |
> |---|---|
> | `@OneToOne`, `@ManyToOne` | **EAGER** (un solo objeto es barato de traer) |
> | `@OneToMany`, `@ManyToMany` | **LAZY** (una lista puede ser enorme: se trae solo si hace falta) |
>
> **El ejemplo.** Siempre el mismo código, leyendo a Ana de la base (ejecutado en MySQL con la práctica; el SQL se muestra sin alias):
>
> ```java
> EstudianteEntity ana = estudiantes.findById(1L).orElseThrow();   // paso 1
> ana.getCarnet().getCodigo();                                     // paso 2
> ana.getCursos();                                                 // paso 3
> ```
>
> <details open>
> <summary><strong>1️⃣ Caso 1 — Por defecto: carnet EAGER, cursos LAZY</strong></summary>
>
> > **En la entidad no se escribe nada de `fetch`**: JPA aplica el valor por defecto. Así están los atributos en la práctica, con lo que JPA entiende en cada uno:
> >
> > ```java
> > // ===== Clase EstudianteEntity (tal como está en la práctica) =====
> >
> > // Relación 1:1 con CarnetEntity
> > @OneToOne(cascade = CascadeType.ALL)        // = fetch EAGER (por defecto en @OneToOne)
> > @JoinColumn(name = "carnet_id")
> > private CarnetEntity carnet;
> >
> > // Relación N:M con CursoEntity
> > @ManyToMany                                  // = fetch LAZY (por defecto en @ManyToMany)
> > @JoinTable(...)
> > private Set<CursoEntity> cursos = new HashSet<>();
> > ```
> >
> > Por eso, al hacer `findById`, el carnet **ya viene** en el mismo `SELECT` (con un `join`) y los cursos **no**: se piden recién en el paso 3.
> >
> > ```text
> > paso 1: findById(1)
> >   select e.id, c.id, c.codigo, e.nombre
> >   from estudiante e left join carnet c on c.id = e.carnet_id      ← el carnet viene en el mismo SELECT
> >   where e.id = 1
> >
> > paso 2: ana.getCarnet().getCodigo()   → C-001   (no ejecuta SQL: ya estaba cargado)
> >
> > paso 3: ana.getCursos()
> >   select ec.estudiante_id, c.id, c.nombre                         ← recién ahora se piden los cursos
> >   from estudiante_curso ec join curso c on c.id = ec.curso_id
> >   where ec.estudiante_id = 1
> >   → [Java, Spring]
> > ```
>
> </details>
>
> <details open>
> <summary><strong>2️⃣ Caso 2 — Cursos EAGER: todo en un solo SELECT</strong></summary>
>
> > **Qué se cambia en la entidad:** se agrega `fetch = FetchType.EAGER` a los cursos.
> >
> > ```java
> > // ===== Clase EstudianteEntity =====
> >
> > // Relación 1:1 con CarnetEntity
> > @OneToOne(cascade = CascadeType.ALL)                    // EAGER (por defecto)
> > @JoinColumn(name = "carnet_id")
> > private CarnetEntity carnet;
> >
> > // Relación N:M con CursoEntity
> > @ManyToMany(fetch = FetchType.EAGER)                    // ← cambia: de LAZY a EAGER
> > @JoinTable(...)
> > private Set<CursoEntity> cursos = new HashSet<>();
> > ```
> >
> > Ahora los cursos llegan **junto con el estudiante**:
> >
> > ```text
> > paso 1: findById(1)
> >   select e.id, c.id, c.codigo, ec.estudiante_id, cu.id, cu.nombre, e.nombre
> >   from estudiante e
> >   left join carnet c on c.id = e.carnet_id
> >   left join estudiante_curso ec on e.id = ec.estudiante_id        ← los cursos también vienen aquí
> >   left join curso cu on cu.id = ec.curso_id
> >   where e.id = 1
> >
> > paso 3: ana.getCursos()   → [Java, Spring]   (no ejecuta SQL)
> > ```
>
> </details>
>
> <details open>
> <summary><strong>3️⃣ Caso 3 — Carnet LAZY: se pide al usarlo</strong></summary>
>
> > **Qué se cambia en la entidad:** se agrega `fetch = FetchType.LAZY` al carnet.
> >
> > ```java
> > // ===== Clase EstudianteEntity =====
> >
> > // Relación 1:1 con CarnetEntity
> > @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)   // ← cambia: de EAGER a LAZY
> > @JoinColumn(name = "carnet_id")
> > private CarnetEntity carnet;
> >
> > // Relación N:M con CursoEntity
> > @ManyToMany                                                   // LAZY (por defecto)
> > @JoinTable(...)
> > private Set<CursoEntity> cursos = new HashSet<>();
> > ```
> >
> > Ahora `findById` trae **solo el estudiante** y el carnet se pide cuando se usa:
> >
> > ```text
> > paso 1: findById(1)
> >   select e.id, e.carnet_id, e.nombre from estudiante e where e.id = 1      ← sin carnet
> >
> > paso 2: ana.getCarnet().getCodigo()
> >   select id, codigo from carnet where id = 1                               ← se pide al usarlo
> >   → C-001
> > ```
>
> </details>
>
> <details open>
> <summary><strong>⚠️ El problema típico de LAZY: LazyInitializationException</strong></summary>
>
> > Se entiende en tres pasos:
> >
> > 1. Con **LAZY**, cuando JPA carga a Ana **no trae sus cursos**. En `ana.cursos` deja una *promesa*: «cuando alguien me los pida, voy a la base de datos y los traigo».
> > 2. Para cumplir esa promesa, JPA necesita que **la conexión con la base (la transacción) siga abierta**.
> > 3. Si la transacción **ya se cerró** cuando se piden los cursos, la promesa no se puede cumplir y salta el error.
> >
> > **❌ Lo que falla.** Cada método del repositorio (`findById`, `save`…) abre su transacción, hace su trabajo y **la cierra al terminar**:
> >
> > ```java
> > EstudianteEntity ana = estudiantes.findById(1L).orElseThrow();   // 1) abre transacción, lee a Ana y la CIERRA
> > ana.getCursos().size();                                          // 2) pide los cursos con la transacción cerrada → 💥
> > ```
> >
> > ```text
> > LazyInitializationException: failed to lazily initialize a collection of role: ...EstudianteEntity.cursos
> > ```
> >
> > Es como pedir algo a un teléfono que ya colgó: la información existe, pero ya no hay línea para pedirla.
> >
> > **✅ Dos formas de arreglarlo**
> >
> > | | Solución | Código | Qué hace |
> > |---|---|---|---|
> > | A | **Pedir los cursos mientras la transacción sigue abierta** (la recomendada) | `tx.executeWithoutResult(status -> { ... })` o `@Transactional` en un método | Se lee a Ana **y** sus cursos dentro de la misma transacción |
> > | B | Poner `EAGER` en `cursos` | `@ManyToMany(fetch = FetchType.EAGER)` | Los cursos ya vienen cargados con Ana; no hay nada que pedir después |
> >
> > ```java
> > // A) Dentro de una transacción (así lo hace la práctica)
> > tx.executeWithoutResult(status -> {
> >     EstudianteEntity ana = estudiantes.findById(1L).orElseThrow();
> >     ana.getCursos().size();        // ✅ la transacción sigue abierta: la promesa se cumple
> > });
> > ```
> >
> > Con `cursos` en `EAGER`, el mismo código que fallaba **sí funciona** (devuelve 2), porque los cursos ya vinieron en el `SELECT` de Ana. Ejecutado en MySQL, comprobado.
>
> </details>
>
> <details open>
> <summary><strong>🤔 ¿Cuándo cambiar los valores por defecto?</strong></summary>
>
> > Regla simple: **no se cambian hasta que haya una razón.** Estas son las razones:
> >
> > | Si ocurre esto… | Se hace esto | Por qué |
> > |---|---|---|
> > | Todo funciona | **Nada** | Los valores por defecto son los adecuados casi siempre |
> > | Aparece `LazyInitializationException` | **Leer la lista dentro de una transacción** (opción A) | Es lo correcto y no cuesta rendimiento |
> > | *Siempre* que se carga la entidad también se necesita esa lista (y es corta) | `EAGER` en esa relación | Evita pedirla aparte cada vez |
> > | La lista puede ser enorme (por ejemplo, un curso con miles de estudiantes) | Dejar `LAZY` | Si fuera EAGER, cada consulta del curso trae miles de filas aunque no se usen |
> > | Una relación a un solo objeto que casi nunca se usa | `@OneToOne(fetch = FetchType.LAZY)` | No se trae si no se necesita |
>
> </details>
>
> > ⚠️ Poner `EAGER` en todo parece cómodo, pero con muchas relaciones cada consulta termina trayendo **media base de datos**. Conviene empezar con los valores por defecto.
> >
> > 🧑‍💻 Estos casos se pueden repetir en la **práctica**: se cambia el `fetch` en `EstudianteEntity` y se observa el SQL que Hibernate imprime en la consola.

</details>

<details open>
<summary><strong>🧑‍💻 Práctica paso a paso: un proyecto propio desde cero</strong></summary>

> Se construye **desde cero** un proyecto Spring Boot con las cuatro entidades de esta guía **en un solo sentido**, sus repositorios y una clase `main` que **guarda** datos y los **consulta** en MySQL, con las tres relaciones a la vez. Al final se ven las tablas y se compara con lo que explican las secciones anteriores.
>
> **Se necesita:** JDK 17 o superior, Maven (o el `mvnw` que trae el proyecto), Docker (o un MySQL instalado) y un IDE. Tiempo: unos 25 minutos.
>
> <details open>
> <summary><strong>1️⃣ Paso 1 — Crear el proyecto</strong></summary>
>
> > Se entra a **[start.spring.io](https://start.spring.io)** y se completa:
> >
> > | Campo | Valor |
> > |---|---|
> > | Project | **Maven** |
> > | Language | **Java** |
> > | Spring Boot | **3.3.x** (la versión estable 3.3) |
> > | Group | `com.academia` |
> > | Artifact / Name | `academia` |
> > | Packaging | **Jar** |
> > | Java | **17** (o superior) |
> >
> > **Dependencies** (botón *Add dependencies*): solo estas tres. **No** se agrega Spring Web: aquí no hay API.
> >
> > | Dependencia | Para qué sirve |
> > |---|---|
> > | **Spring Data JPA** | JPA, Hibernate y los repositorios `JpaRepository` |
> > | **MySQL Driver** | el conector para hablar con MySQL |
> > | **Lombok** | `@Data`, `@NoArgsConstructor`, `@RequiredArgsConstructor` |
> >
> > Se pulsa **Generate**, se descomprime el `.zip` y se abre la carpeta `academia` en el IDE.
> >
> > > ⚠️ **Con JDK 23 o superior**, Lombok necesita dos ajustes en el `pom.xml` (se probó con JDK 25): una versión reciente de Lombok y la opción `proc full` del compilador.
> > >
> > > ```xml
> > > <properties>
> > >     <java.version>17</java.version>
> > >     <lombok.version>1.18.44</lombok.version>
> > > </properties>
> > > ```
> > >
> > > ```xml
> > > <plugin>
> > >     <groupId>org.apache.maven.plugins</groupId>
> > >     <artifactId>maven-compiler-plugin</artifactId>
> > >     <configuration>
> > >         <proc>full</proc>
> > >     </configuration>
> > > </plugin>
> > > ```
> > >
> > > (El plugin va dentro de `<build><plugins>`, junto al `spring-boot-maven-plugin`.) Con JDK 17 o 21 no hace falta.
>
> </details>
>
> <details open>
> <summary><strong>2️⃣ Paso 2 — Levantar MySQL y configurar la conexión</strong></summary>
>
> > **a) Levantar MySQL.** Con Docker (un solo comando; ver también «Preparar MySQL» en la [guía 1 de la tienda](02-guia-tienda-1-crud-productos.md)):
> >
> > ```bash
> > docker run --name mysql-academia -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8
> > ```
> >
> > > Si ya hay un MySQL instalado, se usa ese: solo se cambian el usuario y la contraseña en el paso siguiente.
> >
> > **b) Configurar la conexión** en `src/main/resources/application.properties`:
> >
> > ```properties
> > spring.application.name=academia
> >
> > # Conexión a MySQL (la base «academia» se crea sola si no existe)
> > spring.datasource.url=jdbc:mysql://localhost:3306/academia?createDatabaseIfNotExist=true
> > spring.datasource.username=root
> > spring.datasource.password=root
> >
> > # JPA / Hibernate
> > spring.jpa.hibernate.ddl-auto=create
> > spring.jpa.show-sql=true
> > ```
> >
> > | Propiedad | Qué hace |
> > |---|---|
> > | `spring.datasource.url` | Dónde está MySQL (`localhost:3306`) y qué base usar (`academia`). `createDatabaseIfNotExist=true` **crea la base** si no existe. |
> > | `spring.datasource.username` / `password` | Usuario y clave de MySQL (`root`/`root` es solo para desarrollo). |
> > | `spring.jpa.hibernate.ddl-auto=create` | Al iniciar, Hibernate **borra y crea** las tablas a partir de las entidades. Así se puede ejecutar la práctica las veces que quieras con datos limpios. *(En un proyecto real se usa `update` o `validate`.)* |
> > | `spring.jpa.show-sql=true` | Muestra en la consola el SQL que ejecuta Hibernate. |
>
> </details>
>
> <details open>
> <summary><strong>3️⃣ Paso 3 — Crear el árbol de archivos</strong></summary>
>
> > Dentro de `src/main/java/com/academia/` se crean los paquetes `persistence.entity` y `persistence.repository`, y los archivos vacíos que se ven abajo. La clase `AcademiaApplication` ya la generó Spring Initializr.
> >
> > ```text
> > academia/
> > ├── pom.xml
> > └── src/
> >     └── main/
> >         ├── java/
> >         │   └── com/
> >         │       └── academia/
> >         │           ├── AcademiaApplication.java          ← main + los ejemplos
> >         │           └── persistence/
> >         │               ├── entity/
> >         │               │   ├── CarnetEntity.java
> >         │               │   ├── CursoEntity.java
> >         │               │   ├── DocenteEntity.java
> >         │               │   └── EstudianteEntity.java
> >         │               └── repository/
> >         │                   ├── DocenteJpaRepository.java
> >         │                   └── EstudianteJpaRepository.java
> >         └── resources/
> >             └── application.properties                    ← conexión a MySQL
> > ```
> >
> > > Spring Initializr además genera `mvnw`, `.gitignore` y `src/test`. No hace falta tocarlos.
>
> </details>
>
> <details open>
> <summary><strong>4️⃣ Paso 4 — Crear las entidades</strong></summary>
>
> > Cuatro clases en el paquete `com.academia.persistence.entity`, creadas **relación por relación** y en el **mismo orden que los diagramas de clases**: primero el lado dueño y después el otro. Son las mismas de las secciones de **un sentido** de arriba.
> >
> > <details open>
> > <summary><strong>🔹 4.1 Uno a uno — Estudiante y Carnet</strong></summary>
> >
> > > Primero el **dueño** (`EstudianteEntity`, tiene la clave `carnet_id`) y después `CarnetEntity`, como en el diagrama de clases. Por ahora `EstudianteEntity` solo lleva el carnet.
> > >
> > > 📄 **EstudianteEntity.java  (lado dueño: tiene @JoinColumn)**
> > >
> > > ```java
> > > package com.academia.persistence.entity;
> > >
> > > import jakarta.persistence.*;
> > > import lombok.Data;
> > > import lombok.NoArgsConstructor;
> > >
> > > @Entity
> > > @Table(name = "estudiante")
> > > @Data
> > > @NoArgsConstructor
> > > public class EstudianteEntity {
> > >
> > >     @Id
> > >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> > >     private Long id;
> > >
> > >     private String nombre;
> > >
> > >     // 1:1 — lado DUEÑO: la clave foránea carnet_id vive en la tabla estudiante
> > >     @OneToOne(cascade = CascadeType.ALL)
> > >     @JoinColumn(name = "carnet_id")
> > >     private CarnetEntity carnet;
> > >
> > >     public EstudianteEntity(String nombre) {
> > >         this.nombre = nombre;
> > >     }
> > > }
> > > ```
> > >
> > > 📄 **CarnetEntity.java  (no conoce a su estudiante)**
> > >
> > > ```java
> > > package com.academia.persistence.entity;
> > >
> > > import jakarta.persistence.*;
> > > import lombok.Data;
> > > import lombok.NoArgsConstructor;
> > >
> > > @Entity
> > > @Table(name = "carnet")
> > > @Data
> > > @NoArgsConstructor
> > > public class CarnetEntity {
> > >
> > >     @Id
> > >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> > >     private Long id;
> > >
> > >     private String codigo;
> > >
> > >     public CarnetEntity(String codigo) {
> > >         this.codigo = codigo;
> > >     }
> > > }
> > > ```
> >
> > </details>
> >
> > <details open>
> > <summary><strong>🔹 4.2 Uno a muchos — Docente y Curso</strong></summary>
> >
> > > Primero el **dueño** (`DocenteEntity`, tiene la clave `docente_id` en la tabla `curso`) y después `CursoEntity`.
> > >
> > > 📄 **DocenteEntity.java  (lado dueño: tiene @JoinColumn)**
> > >
> > > ```java
> > > package com.academia.persistence.entity;
> > >
> > > import jakarta.persistence.*;
> > > import lombok.Data;
> > > import lombok.NoArgsConstructor;
> > >
> > > import java.util.ArrayList;
> > > import java.util.List;
> > >
> > > @Entity
> > > @Table(name = "docente")
> > > @Data
> > > @NoArgsConstructor
> > > public class DocenteEntity {
> > >
> > >     @Id
> > >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> > >     private Long id;
> > >
> > >     private String nombre;
> > >
> > >     // 1:N — lado DUEÑO (un sentido): tiene @JoinColumn. La columna docente_id se crea en la tabla curso
> > >     @OneToMany(cascade = CascadeType.ALL)
> > >     @JoinColumn(name = "docente_id")
> > >     private List<CursoEntity> cursos = new ArrayList<>();
> > >
> > >     public DocenteEntity(String nombre) {
> > >         this.nombre = nombre;
> > >     }
> > > }
> > > ```
> > >
> > > 📄 **CursoEntity.java  (no conoce a su docente)**
> > >
> > > ```java
> > > package com.academia.persistence.entity;
> > >
> > > import jakarta.persistence.*;
> > > import lombok.Data;
> > > import lombok.NoArgsConstructor;
> > >
> > > @Entity
> > > @Table(name = "curso")
> > > @Data
> > > @NoArgsConstructor
> > > public class CursoEntity {
> > >
> > >     @Id
> > >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> > >     private Long id;
> > >
> > >     private String nombre;
> > >
> > >     public CursoEntity(String nombre) {
> > >         this.nombre = nombre;
> > >     }
> > > }
> > > ```
> >
> > </details>
> >
> > <details open>
> > <summary><strong>🔹 4.3 Muchos a muchos — Estudiante y Curso</strong></summary>
> >
> > > Primero el **dueño** (`EstudianteEntity`, tiene la tabla intermedia `estudiante_curso`). Se amplía con el atributo `cursos`. Se agregan estos imports y este atributo:
> > >
> > > ```java
> > > // imports nuevos
> > > import java.util.HashSet;
> > > import java.util.Set;
> > >
> > > // atributo nuevo, después de "carnet"
> > > @ManyToMany
> > > @JoinTable(name = "estudiante_curso",
> > >         joinColumns = @JoinColumn(name = "estudiante_id"),
> > >         inverseJoinColumns = @JoinColumn(name = "curso_id"))
> > > private Set<CursoEntity> cursos = new HashSet<>();
> > > ```
> > >
> > > 📄 **EstudianteEntity.java  (versión final, completa)**
> > >
> > > ```java
> > > package com.academia.persistence.entity;
> > >
> > > import jakarta.persistence.*;
> > > import lombok.Data;
> > > import lombok.NoArgsConstructor;
> > >
> > > import java.util.HashSet;
> > > import java.util.Set;
> > >
> > > @Entity
> > > @Table(name = "estudiante")
> > > @Data
> > > @NoArgsConstructor
> > > public class EstudianteEntity {
> > >
> > >     @Id
> > >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> > >     private Long id;
> > >
> > >     private String nombre;
> > >
> > >     // 1:1 — lado DUEÑO: la clave foránea carnet_id vive en la tabla estudiante
> > >     @OneToOne(cascade = CascadeType.ALL)
> > >     @JoinColumn(name = "carnet_id")
> > >     private CarnetEntity carnet;
> > >
> > >     // N:M — lado DUEÑO: JPA crea la tabla intermedia estudiante_curso
> > >     @ManyToMany
> > >     @JoinTable(name = "estudiante_curso",
> > >             joinColumns = @JoinColumn(name = "estudiante_id"),
> > >             inverseJoinColumns = @JoinColumn(name = "curso_id"))
> > >     private Set<CursoEntity> cursos = new HashSet<>();
> > >
> > >     public EstudianteEntity(String nombre) {
> > >         this.nombre = nombre;
> > >     }
> > > }
> > > ```
> > >
> > > `CursoEntity` **no cambia**: sigue sin conocer a sus estudiantes.
> >
> > </details>
> >
> > > 🔎 **¿Quién es el dueño?** Quien tiene `@JoinColumn` o `@JoinTable`: `EstudianteEntity` (`carnet` y `cursos`) y `DocenteEntity` (`cursos`). `CarnetEntity` y `CursoEntity` **no conocen** a quien los referencia: eso es **un sentido**. Por eso aquí no hace falta `@ToString.Exclude`.
>
> </details>
>
> <details open>
> <summary><strong>5️⃣ Paso 5 — Crear los repositorios</strong></summary>
>
> > Dos interfaces en `com.academia.persistence.repository`. **No tienen implementación**: Spring Data la crea solo al arrancar. De ahí salen las variables `docentes` y `estudiantes` de los ejemplos.
> >
> > 📄 **DocenteJpaRepository.java**
> >
> > ```java
> > package com.academia.persistence.repository;
> >
> > import com.academia.persistence.entity.DocenteEntity;
> > import org.springframework.data.jpa.repository.JpaRepository;
> >
> > import java.util.List;
> >
> > public interface DocenteJpaRepository extends JpaRepository<DocenteEntity, Long> {
> >
> >     List<DocenteEntity> findByCursosNombre(String nombre);   // docente.cursos.nombre = ?
> > }
> > ```
> >
> > 📄 **EstudianteJpaRepository.java**
> >
> > ```java
> > package com.academia.persistence.repository;
> >
> > import com.academia.persistence.entity.EstudianteEntity;
> > import org.springframework.data.jpa.repository.JpaRepository;
> >
> > import java.util.List;
> > import java.util.Optional;
> >
> > public interface EstudianteJpaRepository extends JpaRepository<EstudianteEntity, Long> {
> >
> >     Optional<EstudianteEntity> findByCarnetCodigo(String codigo);   // estudiante.carnet.codigo = ?
> >
> >     List<EstudianteEntity> findByCursosNombre(String nombre);       // estudiante.cursos.nombre = ?
> > }
> > ```
> >
> > > 💡 **Solo dos repositorios.** `Carnet` y `Curso` no necesitan el suyo: se guardan por `cascade` desde su dueño (el estudiante guarda su carnet; el docente guarda sus cursos).
> > >
> > > 💡 Los métodos `findByCarnetCodigo` y `findByCursosNombre` son la **consulta del sentido contrario**: como `CarnetEntity` y `CursoEntity` no conocen a su dueño, se **pregunta al repositorio del dueño**. Spring Data los implementa leyendo el nombre.
>
> </details>
>
> <details open>
> <summary><strong>6️⃣ Paso 6 — Escribir los ejemplos en la clase principal</strong></summary>
>
> > Se reemplaza el contenido de `AcademiaApplication.java`. Es la clase del `main`: recibe los repositorios por constructor y ejecuta los ejemplos al arrancar.
> >
> > ```java
> > package com.academia;
> >
> > import com.academia.persistence.entity.*;
> > import com.academia.persistence.repository.*;
> > import lombok.RequiredArgsConstructor;
> > import org.springframework.boot.CommandLineRunner;
> > import org.springframework.boot.SpringApplication;
> > import org.springframework.boot.autoconfigure.SpringBootApplication;
> > import org.springframework.transaction.support.TransactionTemplate;
> >
> > import java.util.Collection;
> > import java.util.List;
> >
> > @SpringBootApplication
> > @RequiredArgsConstructor
> > public class AcademiaApplication implements CommandLineRunner {
> >
> >     private final DocenteJpaRepository docentes;
> >     private final EstudianteJpaRepository estudiantes;
> >     private final TransactionTemplate tx;
> >
> >     public static void main(String[] args) {
> >         SpringApplication.run(AcademiaApplication.class, args);
> >     }
> >
> >     @Override
> >     public void run(String... args) {
> >         tx.executeWithoutResult(status -> guardarDatos());       // transacción 1
> >         tx.executeWithoutResult(status -> consultarDatos());     // transacción 2 (otra distinta)
> >     }
> >
> >     private void guardarDatos() {
> >         // 1:1 — se asigna el carnet en el dueño (el estudiante); cascade lo guarda
> >         EstudianteEntity ana = new EstudianteEntity("Ana");
> >         ana.setCarnet(new CarnetEntity("C-001"));
> >         EstudianteEntity luis = new EstudianteEntity("Luis");
> >
> >         // 1:N — el dueño es el docente; cascade guarda sus cursos
> >         DocenteEntity marta = new DocenteEntity("Marta");
> >         CursoEntity java = new CursoEntity("Java");
> >         CursoEntity spring = new CursoEntity("Spring");
> >         marta.getCursos().add(java);
> >         marta.getCursos().add(spring);
> >         docentes.save(marta);
> >
> >         // N:M — se agrega en el dueño (el estudiante); los cursos ya están guardados
> >         ana.getCursos().add(java);
> >         ana.getCursos().add(spring);
> >         luis.getCursos().add(java);
> >         estudiantes.save(ana);
> >         estudiantes.save(luis);
> >     }
> >
> >     private void consultarDatos() {
> >         EstudianteEntity ana = estudiantes.findById(1L).orElseThrow();
> >         DocenteEntity marta = docentes.findById(1L).orElseThrow();
> >
> >         System.out.println("===== NAVEGANDO (desde el dueño) =====");
> >         System.out.println("Carnet de Ana          : " + ana.getCarnet().getCodigo());
> >         System.out.println("Cursos de Marta        : " + nombresCursos(marta.getCursos()));
> >         System.out.println("Cursos de Ana          : " + nombresCursos(ana.getCursos()));
> >
> >         System.out.println("===== CON EL REPOSITORIO (sentido contrario) =====");
> >         System.out.println("Dueño del carnet C-001 : " + estudiantes.findByCarnetCodigo("C-001").orElseThrow().getNombre());
> >         System.out.println("Docentes de Java       : " + docentes.findByCursosNombre("Java").stream().map(DocenteEntity::getNombre).toList());
> >         System.out.println("Estudiantes de Java    : " + nombresEstudiantes(estudiantes.findByCursosNombre("Java")));
> >     }
> >
> >     private List<String> nombresCursos(Collection<CursoEntity> lista) {
> >         return lista.stream().map(CursoEntity::getNombre).sorted().toList();
> >     }
> >
> >     private List<String> nombresEstudiantes(Collection<EstudianteEntity> lista) {
> >         return lista.stream().map(EstudianteEntity::getNombre).sorted().toList();
> >     }
> > }
> > ```
> >
> > | Parte | Qué hace |
> > |---|---|
> > | `main` | Arranca Spring Boot. |
> > | `@RequiredArgsConstructor` | Lombok genera el constructor que **recibe los repositorios** (así Spring los inyecta). |
> > | `implements CommandLineRunner` + `run` | Spring llama a `run` **justo después de arrancar**: es el punto de entrada para los ejemplos. |
> > | `TransactionTemplate` | Ejecuta cada bloque dentro de **una transacción**. Se usan **dos**: una para guardar y otra, distinta, para consultar. Así las consultas leen **de la base de datos** y no de la memoria. |
> > | `guardarDatos()` | Registra todo asignando **siempre del lado dueño**; el carnet y los cursos se guardan por `cascade`. |
> > | `consultarDatos()` | Responde **navegando desde el dueño** y **con el repositorio** (sentido contrario). |
> >
> > <details open>
> > <summary><strong>🔍 Entender <code>ana.getCursos().add(java)</code></strong></summary>
> >
> > `ana.getCursos()` devuelve **la colección de cursos que está dentro de `ana`**: un `Set<CursoEntity>` que empieza **vacío**. `.add(java)` mete en esa colección el objeto `java`, el `CursoEntity` que se creó antes con `new CursoEntity("Java")`. (`java` es solo el nombre de una variable.)
> >
> > | Después de ejecutar… | `ana.getCursos()` | `luis.getCursos()` |
> > |---|---|---|
> > | `new EstudianteEntity("Ana")` y `("Luis")` | `[]` | `[]` |
> > | `ana.getCursos().add(java)` | `[Java]` | `[]` |
> > | `ana.getCursos().add(spring)` | `[Java, Spring]` | `[]` |
> > | `luis.getCursos().add(java)` | `[Java, Spring]` | `[Java]` |
> >
> > Hasta aquí todo está **solo en memoria**. Al hacer `estudiantes.save(ana)` y `estudiantes.save(luis)`, JPA lee esas colecciones y escribe **una fila por curso** en la tabla intermedia:
> >
> > ```text
> > ana.cursos   = [Java, Spring]            estudiante_curso
> > luis.cursos  = [Java]                    +---------------+----------+
> >                                          | estudiante_id | curso_id |
> >                                          +---------------+----------+
> >                                          |             1 |        1 |   ← Ana – Java
> >                                          |             1 |        2 |   ← Ana – Spring
> >                                          |             2 |        1 |   ← Luis – Java
> >                                          +---------------+----------+
> > ```
> >
> > > 🔎 Al **consultar**, `ana` se carga de la base (`findById`) y JPA llena su colección con esas filas: por eso allí `ana.getCursos()` ya devuelve `[Java, Spring]`.
> >
> > </details>
>
> </details>
>
> <details open>
> <summary><strong>7️⃣ Paso 7 — Ejecutar</strong></summary>
>
> > Desde la terminal, en la carpeta del proyecto:
> >
> > ```bash
> > ./mvnw spring-boot:run
> > ```
> >
> > *(En Windows: `mvnw.cmd spring-boot:run`. También se puede pulsar **Run** sobre `AcademiaApplication` en el IDE.)*
> >
> > Primero aparece el SQL de Hibernate (`create table…`, `insert into…`) y, al final, lo que imprimen los ejemplos. **Resultado real:**
> >
> > ```text
> > ===== NAVEGANDO (desde el dueño) =====
> > Carnet de Ana          : C-001
> > Cursos de Marta        : [Java, Spring]
> > Cursos de Ana          : [Java, Spring]
> > ===== CON EL REPOSITORIO (sentido contrario) =====
> > Dueño del carnet C-001 : Ana
> > Docentes de Java       : [Marta]
> > Estudiantes de Java    : [Ana, Luis]
> > ```
> >
> > > 🔎 En el SQL de la consola se ve `insert into curso (nombre)` y **después** `update curso set docente_id=?`: es lo normal en un `@OneToMany` de un sentido (el curso entra primero sin docente y Hibernate completa la clave).
> > >
> > > 🔎 La aplicación termina sola al acabar `run` (no hay servidor web).
>
> </details>
>
> <details open>
> <summary><strong>8️⃣ Paso 8 — Ver cómo quedaron las tablas</strong></summary>
>
> > Con la aplicación ya ejecutada, se miran las tablas en MySQL (con Docker; o con MySQL Workbench / DBeaver con los mismos datos de conexión):
> >
> > ```bash
> > docker exec -it mysql-academia mysql -uroot -proot academia \
> >   -e "select * from carnet; select * from estudiante; select * from docente; select * from curso; select * from estudiante_curso"
> > ```
> >
> > **Resultado real:**
> >
> > ```text
> > carnet                 estudiante                        docente
> > +----+--------+        +-----------+----+--------+      +----+--------+
> > | id | codigo |        | carnet_id | id | nombre |      | id | nombre |
> > +----+--------+        +-----------+----+--------+      +----+--------+
> > |  1 | C-001  |        |         1 |  1 | Ana    |      |  1 | Marta  |
> > +----+--------+        |      NULL |  2 | Luis   |      +----+--------+
> >                        +-----------+----+--------+
> >
> > curso                              estudiante_curso
> > +------------+----+--------+       +----------+---------------+
> > | docente_id | id | nombre |       | curso_id | estudiante_id |
> > +------------+----+--------+       +----------+---------------+
> > |          1 |  1 | Java   |       |        1 |             1 |
> > |          1 |  2 | Spring |       |        2 |             1 |
> > +------------+----+--------+       |        1 |             2 |
> >                                    +----------+---------------+
> > ```
> >
> > Se compara con lo esperado: la clave `carnet_id` está en `estudiante`, `docente_id` en `curso`, y la relación muchos a muchos vive en la tabla `estudiante_curso`.
>
> </details>
>
> <details open>
> <summary><strong>9️⃣ Paso 9 — Experimento: pasar a doble sentido</strong></summary>
>
> > Hasta ahora `CarnetEntity` no conoce a su estudiante. Se agrega el **lado inverso** (opcional) en `CarnetEntity.java`:
> >
> > ```java
> > // imports nuevos
> > import lombok.EqualsAndHashCode;
> > import lombok.ToString;
> >
> > // dentro de la clase, después de "private String codigo;"
> > @OneToOne(mappedBy = "carnet")
> > @ToString.Exclude
> > @EqualsAndHashCode.Exclude
> > private EstudianteEntity estudiante;
> > ```
> >
> > Y en `consultarDatos()`, justo después de la línea «Carnet de Ana», esta línea:
> >
> > ```java
> > System.out.println("Dueño del carnet C-001 : " + ana.getCarnet().getEstudiante().getNombre());
> > ```
> >
> > Se ejecuta de nuevo. **Resultado real:**
> >
> > ```text
> > ===== NAVEGANDO (desde el dueño) =====
> > Carnet de Ana          : C-001
> > Dueño del carnet C-001 : Ana          ← ahora se navega también desde el carnet
> > ```
> >
> > Se comprueba que **las tablas no cambiaron**:
> >
> > ```bash
> > docker exec -it mysql-academia mysql -uroot -proot academia -e "describe carnet"
> > ```
> >
> > ```text
> > +--------+--------------+------+-----+---------+----------------+
> > | Field  | Type         | Null | Key | Default | Extra          |
> > +--------+--------------+------+-----+---------+----------------+
> > | id     | bigint       | NO   | PRI | NULL    | auto_increment |
> > | codigo | varchar(255) | YES  |     | NULL    |                |
> > +--------+--------------+------+-----+---------+----------------+
> > ```
> >
> > > 🔎 `carnet` sigue sin columna del estudiante: el lado inverso (`mappedBy`) **no crea nada** y solo sirve para **navegar** en sentido contrario. Eso es el **doble sentido**. Ahora se puede volver a quitar.
>
> </details>

</details>

<details open>
<summary><strong>🩺 Errores frecuentes</strong></summary>

> | Síntoma | Causa | Solución |
> |---|---|---|
> | `AnnotationException: Collection '…DocenteEntity.cursos' is 'mappedBy' a property named 'profesor' which does not exist in the target entity '…CursoEntity'` | El `mappedBy` no coincide con el atributo del dueño | Escribir el nombre **exacto**: `mappedBy = "docente"` |
> | La relación «no se guarda» (la clave queda en `NULL`) | Se asignó solo en el lado **inverso** | Asignar en el **dueño** (`curso.setDocente(…)`) o usar un método que sincronice los dos lados |
> | `LazyInitializationException: failed to lazily initialize a collection of role: …DocenteEntity.cursos … no Session` | Se leyó una colección `LAZY` **fuera** de una transacción | Leerla dentro de un método `@Transactional` |
> | `StackOverflowError` al imprimir o comparar entidades | `@Data` (o `toString`/`equals`) recorre una relación en doble sentido | Poner `@ToString.Exclude` y `@EqualsAndHashCode.Exclude` en el atributo del lado inverso |
> | `TransientObjectException` / «unsaved transient instance» | Se relacionó un objeto que **todavía no se guardó** | Guardarlo antes, o usar `cascade` |
> | Una tabla intermedia sin columnas para datos propios | `@ManyToMany` no admite atributos de la relación | Pasar a una **entidad intermedia** con dos `@ManyToOne` |

</details>

<details open>
<summary><strong>🧾 Chuleta</strong></summary>

> | Quiero… | Uso | Dueño (clave) |
> |---|---|---|
> | Que **un** objeto tenga **un** objeto | `@OneToOne` + `@JoinColumn` | Quien tiene `@JoinColumn` |
> | Que **muchos** objetos apunten a **uno** | `@ManyToOne` + `@JoinColumn` | El lado «muchos» |
> | Que **uno** tenga su **lista** de muchos, sin que ellos lo sepan (un sentido) | `@OneToMany` + `@JoinColumn` | El lado «uno» |
> | Que **uno** vea su **lista** de muchos, con `@ManyToOne` en el otro lado | `@OneToMany(mappedBy = "…")` | **Inverso** (opcional) |
> | Que **muchos** se relacionen con **muchos** | `@ManyToMany` + `@JoinTable` | Quien tiene `@JoinTable` |
> | Ver la relación desde el otro lado | `mappedBy = "atributoDelDueño"` | **Inverso** |
> | Que guardar uno guarde el relacionado | `cascade = CascadeType.ALL` | — |
> | La relación necesita datos propios | Entidad intermedia con dos `@ManyToOne` | La intermedia |

</details>

<details open>
<summary><strong>🚀 Retos</strong></summary>

> 1. **Cascade:** agregar `cascade = CascadeType.ALL` y `orphanRemoval = true` a `DocenteEntity.cursos` y comprobar qué pasa con los cursos al borrar un docente. ([Cascade y orphanRemoval](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/05-cascade-y-orphanRemoval.md).)
> 2. **Un sentido:** quitar el lado inverso de `CarnetEntity` y comprobar que **las tablas no cambian**.
> 3. **Dato propio:** agregar la **fecha de inscripción** a la relación estudiante–curso: ¿qué entidad nueva hace falta y qué relaciones declara? *(Pista: la `MatriculaEntity` del taller.)*

</details>

➡️ **Siguiente:** [Guía 01 — Taller «Gestión de cursos» con base de datos](01-guia-taller-gestion-de-cursos-con-bd.md)
