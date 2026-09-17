# 📚 Explicación conceptual — Módulo 4

## 🧠 Concepto: Manejo de conexión a la base de datos y creación de un proyecto Spring Boot

Un proyecto Spring Boot se crea con **Spring Initializr**
(`start.spring.io`, o el asistente equivalente integrado en IntelliJ o
Spring Tools para VS Code), eligiendo:

- Tipo de build (Maven o Gradle), lenguaje y versión de Spring Boot.
- Las dependencias necesarias: `Spring Data JPA` (JPA + Hibernate +
  repositorios) y `H2 Database` (el driver de la base de datos en memoria
  del curso).

Esas elecciones generan el `pom.xml` con las dependencias correspondientes
y la estructura de carpetas `src/main/java`/`src/main/resources` — no hace
falta editarlas a mano. La conexión a la base de datos se configura después
en `application.properties`, igual que en el Módulo 3.

📎 Ver en la práctica: [Ejemplo 01 — Creación de un proyecto Spring Boot](01-creacion-de-un-proyecto-spring-boot.md)

## 🧠 Concepto: Mapeo de relaciones entre clases

Una relación en JPA es la representación en Java de una relación entre
tablas de una base de datos relacional. Ejemplos reales: un paciente tiene
una historia clínica; un paciente tiene muchas citas; un libro puede tener
muchos autores; un usuario puede tener muchos préstamos.

En toda relación, uno de los dos lados es el **dueño** (declara la clave
foránea real, con `@JoinColumn`), y el otro es el **inverso** (solo refleja
la relación, con `mappedBy` apuntando al atributo del lado dueño). Esta
distinción aplica a los tres tipos de relación:

- **`@OneToOne`**: una entidad se relaciona con exactamente una de la otra.
- **`@OneToMany`/`@ManyToOne`**: las más comunes; el lado "muchos"
  (`@ManyToOne`) es el dueño.
- **`@ManyToMany`**: cuando necesita atributos propios, se reemplaza por
  una entidad intermedia explícita.

📎 Ver en la práctica: [Ejemplo 02 — `@OneToOne` y el lado dueño de la relación](02-oneToOne-y-el-lado-dueno.md)

El atributo `fetch` define cuándo se cargan los datos relacionados:

| Tipo | Qué hace |
|---|---|
| `LAZY` | Carga solo cuando se accede a la colección (por defecto en `@OneToMany`/`@ManyToMany`). |
| `EAGER` | Carga inmediatamente, junto con la entidad principal (por defecto en `@ManyToOne`/`@OneToOne`). |

Uso común: siempre `LAZY` por defecto, salvo que haya una razón fuerte para
`EAGER` (los datos relacionados casi siempre se necesitan junto con la
entidad principal).

📎 Ver en la práctica: [Ejemplo 03 — `@OneToMany`/`@ManyToOne` y `fetch`](03-oneToMany-manyToOne-y-fetch.md)

`@ManyToMany` simple alcanza cuando la relación no necesita más información
que "estos dos están relacionados". En cuanto la relación necesita un
**atributo propio** (por ejemplo, la fecha de un préstamo), conviene
reemplazarla por una **entidad intermedia** explícita, con sus propias
relaciones `@ManyToOne` hacia ambos lados.

📎 Ver en la práctica: [Ejemplo 04 — `@ManyToMany` y cuándo usar una entidad intermedia](04-manyToMany-y-entidad-intermedia.md)

`cascade` propaga una operación desde una entidad padre hacia sus entidades
relacionadas:

| Tipo | Qué propaga |
|---|---|
| `PERSIST` | `save` (crear) |
| `MERGE` | actualizar |
| `REMOVE` | eliminar |
| `ALL` | todo lo anterior |

`orphanRemoval = true` elimina automáticamente una entidad hija que deja de
estar en la colección de su padre (y el padre se vuelve a guardar).

📎 Ver en la práctica: [Ejemplo 05 — `cascade` y `orphanRemoval`](05-cascade-y-orphanRemoval.md)

## 🧠 Concepto: Ejemplo de CRUD completo

CRUD (Crear, Leer, Actualizar, Eliminar) es el ciclo de vida completo de un
registro, y `JpaRepository` ya trae las cuatro operaciones sin necesidad de
código propio:

| Operación | Método |
|---|---|
| Crear | `save(entidadSinId)` |
| Leer | `findById(...)` / `findBy...` |
| Actualizar | `save(entidadConId)` |
| Eliminar | `deleteById(...)` |

`save` sirve tanto para crear como para actualizar: si la entidad no tiene
`id`, inserta un registro nuevo; si ya tiene `id` (por ejemplo, porque vino
de un `findBy...`), actualiza el existente.

📎 Ver en la práctica: [Ejemplo 06 — CRUD completo con Spring Data JPA](06-crud-completo.md)

## 🧠 Concepto: Inicialización del servidor

El log que Spring Boot imprime al arrancar no es solo ruido: es la
principal herramienta de diagnóstico cuando la aplicación falla. Un
arranque exitoso siempre incluye una línea `Started <Clase> in ...
seconds`; si esa línea nunca aparece, el `ApplicationContext` no terminó de
inicializarse (típicamente porque falló la conexión a la base de datos).

Ante un arranque fallido, conviene buscar la línea `Caused by:` (la causa
raíz de la excepción) y compararla contra los valores configurados en
`application.properties`, en vez de asumir que el error está en el código
Java.

📎 Ver en la práctica: [Ejemplo 07 — Inicialización del servidor](07-inicializacion-del-servidor.md)

## 🧠 Concepto: Ejercicio de JPA aplicado

El cierre del módulo integra todo lo anterior en un caso completo: un
proyecto Spring Boot creado desde cero, un padre con varias líneas de
detalle relacionadas con `cascade`/`orphanRemoval`, su repositorio de
Spring Data JPA, y operaciones de alta y edición sobre esos registros — no
un concepto aislado, sino todos combinados en un solo flujo de trabajo.

📎 Ver en la práctica: [Ejemplo 08 — Vista previa: un padre con varias líneas de detalle](08-vista-previa-padre-hijas.md) · [Taller 01](../../actividades/talleres/taller-01.md)
