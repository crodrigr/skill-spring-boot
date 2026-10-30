# 🔗 Guía 00 — Relaciones entre entidades en JPA: uno a uno, uno a muchos y muchos a muchos

<div style="background-color:#fff3b0; color:#1f1f1f; border-left:6px solid #f2b705; padding:12px 16px; border-radius:6px;">

🎯 **Qué vas a aprender:** a declarar las tres relaciones de JPA, **desde las entidades**, en **un sentido** y en **doble sentido**, y qué significa cada notación.

⏱️ **Tiempo:** unos 20 minutos. 📚 **Va antes de** la [guía del taller «Gestión de cursos» con base de datos](01-guia-taller-gestion-de-cursos-con-bd.md) y de las [guías de la tienda](03-guia-tienda-2-uno-a-muchos.md).

✅ **Verificada:** el código se compiló y se ejecutó con Spring Boot 3.3 y **MySQL 8**; las tablas, los resultados y los errores que ves son reales.

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

</details>

<details open>
<summary><strong>1️⃣ Uno a uno — Estudiante y Carnet</strong></summary>

> ```mermaid
> classDiagram
>     direction LR
>     class EstudianteEntity {
>         <<Entity>>
>         -Long id
>         -String nombre
>         -CarnetEntity carnet
>     }
>     class CarnetEntity {
>         <<Entity>>
>         -Long id
>         -String codigo
>         -EstudianteEntity estudiante
>     }
>     EstudianteEntity "1" -- "1" CarnetEntity : carnet_id
> ```
>
> > Las flechas del diagrama no llevan punta porque la relación puede recorrerse en los dos sentidos; el atributo `estudiante` de `CarnetEntity` existe **solo** en el doble sentido.
>
> <details open>
> <summary><strong>➡️ Un sentido</strong></summary>
>
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
>
> </details>
>
> <details open>
> <summary><strong>↔️ Doble sentido</strong></summary>
>
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
>
> </details>
>
> <details open>
> <summary><strong>🛠️ Lo que genera MySQL</strong></summary>
>
> > ```text
> > create table carnet (id bigint not null auto_increment, codigo varchar(255), primary key (id))
> > create table estudiante (carnet_id bigint, id bigint not null auto_increment, nombre varchar(255), primary key (id))
> > alter table estudiante add constraint UK… unique (carnet_id)                       ← esto lo hace «uno a uno»
> > alter table estudiante add constraint FK… foreign key (carnet_id) references carnet (id)
> > ```
>
> </details>
>
> <details open>
> <summary><strong>🗄️ Así queda en las tablas (datos del escenario «Probalo»)</strong></summary>
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

> ```mermaid
> classDiagram
>     direction LR
>     class DocenteEntity {
>         <<Entity>>
>         -Long id
>         -String nombre
>         -List~CursoEntity~ cursos
>     }
>     class CursoEntity {
>         <<Entity>>
>         -Long id
>         -String nombre
>         -DocenteEntity docente
>     }
>     DocenteEntity "1" -- "*" CursoEntity : docente_id
> ```
>
> > En un sentido solo existe `DocenteEntity.cursos`; en doble sentido se agrega también `CursoEntity.docente`.
>
> Un docente dicta **varios** cursos. Se muestra primero **desde el docente** (un sentido) y después se agrega el curso → docente (doble sentido). La clave foránea siempre queda en la tabla `curso` (el lado «muchos»), pero **quién la escribe** depende del sentido.
>
> <details open>
> <summary><strong>➡️ Un sentido</strong></summary>
>
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
>
> </details>
>
> <details open>
> <summary><strong>↔️ Doble sentido</strong></summary>
>
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
>
> </details>
>
> <details open>
> <summary><strong>🛠️ Lo que genera MySQL</strong></summary>
>
> > ```text
> > create table docente (id bigint not null auto_increment, nombre varchar(255), primary key (id))
> > create table curso (docente_id bigint, id bigint not null auto_increment, nombre varchar(255), primary key (id))
> > alter table curso add constraint FK… foreign key (docente_id) references docente (id)
> > ```
>
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
> > ⚠️ **En doble sentido se asigna del lado dueño (el curso).** `docente.getCursos().add(curso)` **no** escribe `docente_id`; hay que hacer `curso.setDocente(docente)`. Se comprobó: un curso agregado **solo** a la lista del docente quedó con `docente_id = NULL`. En doble sentido, mantené **los dos lados sincronizados** con el método `agregarCurso` de `DocenteEntity` (ya incluido arriba).

</details>

<details open>
<summary><strong>3️⃣ Muchos a muchos — Estudiante y Curso</strong></summary>

> ```mermaid
> classDiagram
>     direction LR
>     class EstudianteEntity {
>         <<Entity>>
>         -Long id
>         -String nombre
>         -Set~CursoEntity~ cursos
>     }
>     class CursoEntity {
>         <<Entity>>
>         -Long id
>         -String nombre
>         -Set~EstudianteEntity~ estudiantes
>     }
>     EstudianteEntity "*" -- "*" CursoEntity : estudiante_curso
> ```
>
> > El atributo `estudiantes` de `CursoEntity` existe **solo** en el doble sentido.
>
> Ningún lado puede guardar la clave en su propia tabla: hace falta una **tabla intermedia** con las dos claves foráneas. JPA la crea por vos.
>
> <details open>
> <summary><strong>➡️ Un sentido</strong></summary>
>
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
>
> </details>
>
> <details open>
> <summary><strong>↔️ Doble sentido</strong></summary>
>
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
>
> </details>
>
> <details open>
> <summary><strong>🛠️ Lo que genera MySQL</strong></summary>
>
> > ```text
> > create table estudiante_curso (curso_id bigint not null, estudiante_id bigint not null, primary key (curso_id, estudiante_id))
> > alter table estudiante_curso add constraint FK… foreign key (curso_id)      references curso (id)
> > alter table estudiante_curso add constraint FK… foreign key (estudiante_id) references estudiante (id)
> > ```
>
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
> | Solo preguntás «¿cuál es el carnet de este estudiante?» | **No** |
> | Preguntás «¿de quién es este carnet?» pocas veces | **No**: usá un método del repositorio, por ejemplo `findByCarnetCodigo("C-001")` en `EstudianteJpaRepository` |
> | Recorrés **siempre en las dos direcciones** y querés `carnet.getEstudiante()` directo | **Sí** |
>
> > ⚠️ **Costo:** hay que mantener los dos lados coherentes, excluir el inverso de `toString`/`equals` (con `@Data`) y evitar el bucle infinito en JSON.
>
> | | **Un sentido** (solo el dueño) | **Doble sentido** (dueño + inverso) |
> |---|---|---|
> | Qué agregás | Solo la notación del dueño | Además, la del inverso con `mappedBy` |
> | Cambia la base de datos | — | **No**: el inverso no crea columnas ni tablas |
> | Podés navegar | Solo desde el dueño | Desde los dos lados |
> | Costo | El más simple | Hay que **sincronizar** ambos lados y evitar ciclos en JSON y `toString` |
> | Conviene cuando… | Solo necesitás consultar desde un lado | De verdad necesitás recorrer en las dos direcciones |
>
> **Reglas para no equivocarte:**
>
> 1. **Empezá en un sentido.** Agregá el inverso solo si lo vas a usar.
> 2. **Siempre se escribe del lado dueño.** El inverso solo sirve para **leer**.
> 3. **`mappedBy` lleva el nombre exacto** del atributo del dueño (`"docente"`, `"carnet"`, `"cursos"`).
> 4. **Con `@Data` y doble sentido, excluí el lado inverso** con `@ToString.Exclude` y `@EqualsAndHashCode.Exclude`: si no, `toString`, `equals` y `hashCode` se llaman entre sí **sin fin** (`StackOverflowError`). Así están las clases de esta guía.
> 5. Al devolver entidades como **JSON**, el doble sentido genera un bucle infinito: por eso en la [guía 2 de la tienda](03-guia-tienda-2-uno-a-muchos.md) se usa `@JsonIgnore`, y en la [Fase II](05-guia-tienda-fase-2-buenas-practicas.md) se separan modelo y entidad.

</details>

<details open>
<summary><strong>🧪 Probalo</strong></summary>

> El proyecto de prueba tiene **solo tres dependencias**: Spring Data JPA, MySQL Driver y Lombok (sin Web: aquí no hay API). Con MySQL en `localhost:3306` (ver «Preparar MySQL» en la [guía 1 de la tienda](02-guia-tienda-1-crud-productos.md)):
>
> ```properties
> spring.datasource.url=jdbc:mysql://localhost:3306/academia?createDatabaseIfNotExist=true
> spring.datasource.username=root
> spring.datasource.password=root
> spring.jpa.hibernate.ddl-auto=update
> spring.jpa.show-sql=true
> ```
>
> Se guardan datos asignando **siempre del lado dueño** (un `CommandLineRunner` con `@Transactional` y los repositorios `…JpaRepository` de cada entidad):
>
> ```java
> // 1:1 — se asigna el lado dueño; cascade guarda también el carnet
> EstudianteEntity ana = new EstudianteEntity("Ana");
> ana.setCarnet(new CarnetEntity("C-001"));
> estudiantes.save(ana);
> EstudianteEntity luis = estudiantes.save(new EstudianteEntity("Luis"));
>
> // 1:N (doble sentido) — el lado dueño es el curso
> DocenteEntity marta = docentes.save(new DocenteEntity("Marta"));
> CursoEntity java = new CursoEntity("Java");
> java.setDocente(marta);
> cursos.save(java);
> CursoEntity spring = new CursoEntity("Spring");
> spring.setDocente(marta);
> cursos.save(spring);
>
> // Error típico: solo del lado inverso. NO escribe docente_id
> CursoEntity html = cursos.save(new CursoEntity("HTML"));
> marta.getCursos().add(html);
>
> // N:M — se agrega en el lado dueño
> ana.getCursos().add(java);
> ana.getCursos().add(spring);
> luis.getCursos().add(java);
> ```
>
> En otra transacción se leen las relaciones desde ambos lados. Resultado **real**:
>
> ```text
> 1:1 desde el dueño   : carnet de Ana = C-001
> 1:1 desde el inverso : dueño del carnet 1 = Ana
> 1:N desde el inverso : cursos de Marta = 2       ← se guardaron 3, pero HTML solo se agregó del lado inverso
> N:1 desde el dueño   : docente de HTML = null
> N:M desde el dueño   : cursos de Ana = 2
> N:M desde el inverso : estudiantes de Java = 2
> ```
>
> > 🔎 **Qué demuestra:** el lado **inverso** (`carnet.getEstudiante()`, `docente.getCursos()`, `curso.getEstudiantes()`) se **lee de la base** aunque nunca se asignó; en cambio, lo que se agrega **solo** en el inverso **no se guarda** (HTML sin docente).

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

> 1. **Cascade:** agregá `cascade = CascadeType.ALL` y `orphanRemoval = true` a `DocenteEntity.cursos` y comprobá qué pasa con los cursos al borrar un docente. ([Cascade y orphanRemoval](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/05-cascade-y-orphanRemoval.md).)
> 2. **Un sentido:** quitá el lado inverso de `CarnetEntity` y comprobá que **las tablas no cambian**.
> 3. **Dato propio:** agregá la **fecha de inscripción** a la relación estudiante–curso: ¿qué entidad nueva necesitás y qué relaciones declara? *(Pista: la `MatriculaEntity` del taller.)*

</details>

➡️ **Siguiente:** [Guía 01 — Taller «Gestión de cursos» con base de datos](01-guia-taller-gestion-de-cursos-con-bd.md)
