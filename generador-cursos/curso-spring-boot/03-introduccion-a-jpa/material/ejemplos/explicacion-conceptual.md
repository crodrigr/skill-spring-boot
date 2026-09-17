# 📚 Explicación conceptual — Módulo 3

## 🧠 Concepto: ¿Qué es JPA?

JPA (*Java Persistence API*) es la especificación estándar de Java para
mapear objetos a una base de datos relacional, sin escribir SQL a mano.

Sus tres características principales:

- **Abstracción de la base de datos**: trabajás con objetos Java, no con
  sentencias SQL.
- **Estandarización**: distintas implementaciones (Hibernate, EclipseLink,
  OpenJPA) ejecutan la misma API, sin cambiar el código de la aplicación.
- **Flexibilidad en el mapeo de entidades**: una clase puede mapearse a una
  tabla de distintas formas (nombres, relaciones, generación de ID) sin
  cambiar su lógica de negocio.

Sus tres funcionalidades clave:

- **Anotaciones** (`@Entity` y similares) para declarar el mapeo.
- **JPQL**, un lenguaje de consulta orientado a objetos, con sintaxis
  parecida a SQL pero que opera sobre entidades, no sobre tablas.
- **API de persistencia** (`persist`, `find`, …) para operaciones CRUD.

📎 Ver en la práctica: [Ejemplo 01 — ¿Qué es JPA?](01-que-es-jpa.md)

## 🧠 Concepto: Arquitectura de JPA

La arquitectura de JPA tiene cinco componentes:

- **`EntityManagerFactory`**: punto de entrada; crea y administra instancias
  de `EntityManager`.
- **`EntityManager`**: componente central — persiste, actualiza, elimina y
  consulta entidades.
- **Entidades (`@Entity`)**: los objetos que se mapean a una tabla.
- **`EntityTransaction`**: agrupa operaciones para que se confirmen o
  reviertan como una unidad.
- **`Query`/JPQL y la unidad de persistencia**: la interfaz de consulta y la
  configuración sobre la que trabaja todo lo anterior.

Flujo de trabajo: `EntityManagerFactory` se crea a partir de la unidad de
persistencia y genera instancias de `EntityManager`; el `EntityManager`
gestiona entidades, ejecuta consultas y maneja transacciones. En Spring
Boot, la unidad de persistencia se autoconfigura desde
`application.properties`, sin un archivo `persistence.xml` manual, y el
`EntityManager` se recibe inyectado con `@PersistenceContext`.

📎 Ver en la práctica: [Ejemplo 02 — Arquitectura de JPA](02-arquitectura-de-jpa.md)

## 🧠 Concepto: Relacionamiento entre clases

Las entidades se relacionan entre sí de tres formas:

- **Uno a uno**: como máximo una instancia de cada lado se relaciona con una
  del otro.
- **Uno a muchos** (o su inversa, muchos a uno): una instancia de un lado se
  relaciona con varias del otro, pero no al revés.
- **Muchos a muchos**: varias instancias de cada lado se relacionan con
  varias del otro, sin límite de "uno" en ningún sentido.

Las tres relaciones del módulo: `Paciente`↔`Cita` (uno a muchos),
`Paciente`↔`HistoriaClinica` (uno a uno), `Libro`↔`Autor` (muchos a
muchos). Identificar el tipo de relación es leer el dominio de negocio; la
anotación concreta que la representa en código llega en el bloque 8.

📎 Ver en la práctica: [Ejemplo 03 — Relacionamiento entre clases](03-relacionamiento-entre-clases.md)

## 🧠 Concepto: ¿Qué es ORM?

El Mapeo Objeto-Relacional (ORM) es una técnica que convierte objetos de una
aplicación en registros de una base de datos relacional, y viceversa, sin
que el desarrollador escriba SQL manualmente para esa conversión. Cubre
todas las operaciones CRUD, no solo guardar: al consultar, actualizar o
eliminar, el ORM traduce la operación sobre el objeto a la sentencia SQL
equivalente.

📎 Ver en la práctica: [Ejemplo 04 — ¿Qué es ORM?](04-que-es-orm.md)

## 🧠 Concepto: Hibernate y su relación con JPA

Hibernate es un framework de mapeo objeto-relacional para Java. Se encarga
de:

- Convertir clases Java en tablas de base de datos.
- Convertir atributos en columnas.
- Convertir relaciones entre objetos en relaciones entre tablas.
- Generar y ejecutar el SQL necesario para las operaciones CRUD.

JPA es la especificación; Hibernate es una de sus implementaciones (junto
con EclipseLink, OpenJPA). Resumen: **"JPA define el qué, y Hibernate define
el cómo"** — el código de la aplicación programa contra la API de JPA, y la
implementación es intercambiable sin tocar esa lógica.

📎 Ver en la práctica: [Ejemplo 05 — Hibernate y su relación con JPA](05-hibernate-y-su-relacion-con-jpa.md)

## 🧠 Concepto: ¿Qué es Spring Data JPA?

Spring Data JPA es el módulo de Spring que evita implementar a mano el
acceso a datos: se declara una **interfaz** de repositorio, y Spring genera
su implementación en tiempo de ejecución. Ventajas:

- Menos código repetitivo.
- Creación automática de consultas por convención de nombres de método.
- Paginación y ordenamiento integrados.
- Soporte para consultas personalizadas con `@Query`.
- Compatibilidad con múltiples bases de datos (H2, MySQL, PostgreSQL, …).

📎 Ver en la práctica: [Ejemplo 06 — ¿Qué es Spring Data JPA?](06-que-es-spring-data-jpa.md)

## 🧠 Concepto: Uso básico de Spring Data JPA

Dependencias necesarias: `spring-boot-starter-data-jpa` (JPA + Hibernate +
Spring Data JPA) y el driver de la base de datos (`com.h2database:h2` en
este curso). Configuración básica en `application.properties`: la conexión
(`spring.datasource.*`) y JPA/Hibernate
(`spring.jpa.database-platform`, `spring.jpa.hibernate.ddl-auto`).

Los cinco valores de `spring.jpa.hibernate.ddl-auto`:

- `none`: no toca el esquema.
- `validate`: verifica que el esquema coincide con las entidades; si no,
  falla.
- `update`: crea/actualiza tablas y columnas sin borrar datos (usado en
  este curso).
- `create`: borra y crea todo el esquema desde cero en cada arranque.
- `create-drop`: crea el esquema al iniciar y lo borra al cerrar.

`create` y `create-drop` implican riesgo de pérdida de datos; no se usan
sobre datos que importe conservar.

📎 Ver en la práctica: [Ejemplo 07 — Uso básico de Spring Data JPA](07-uso-basico-de-spring-data-jpa.md)

## 🧠 Concepto: Anotaciones en JPA

Anotaciones principales:

- `@Entity` (clase): marca la clase como entidad persistente.
- `@Table(name="...")` (clase): define el nombre de la tabla.
- `@Id` (atributo): indica la clave primaria.
- `@GeneratedValue(strategy=...)` (atributo): define cómo se genera el ID.
- `@Column(...)` (atributo): configura la columna (`nullable`, `unique`, `length`).
- `@ManyToOne` (atributo): relación muchos a uno; el lado con la clave foránea.
- `@OneToMany(mappedBy="...")` (atributo): relación uno a muchos, lado inverso.
- `@OneToOne` (atributo): relación uno a uno.
- `@ManyToMany` (atributo): relación muchos a muchos, con tabla intermedia (`@JoinTable`).
- `@JoinColumn(name="...")` (atributo): define la columna de clave foránea.
- `@Transactional` (método/clase): asegura una transacción y mantiene la sesión abierta para cargas `LAZY`.

En toda relación, un lado es el **propietario** (declara la clave foránea o
la tabla intermedia) y el otro es el lado **inverso** (usa `mappedBy`
apuntando al campo del lado propietario).

📎 Ver en la práctica: [Ejemplo 08 — Anotaciones en JPA](08-anotaciones-en-jpa.md)
