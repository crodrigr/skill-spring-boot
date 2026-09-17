# ❓ Quiz 03 — Introducción a JPA (formato entrevista técnica)

Este quiz simula las preguntas que podrías recibir en una entrevista técnica para
un puesto de desarrollador Java/Spring Boot. Cada pregunta indica su tipo
(**Selección**, **Selección múltiple** o **Abierta**). Respondé primero por tu
cuenta y después abrí "Ver respuesta" para comparar.

---

**1. [Selección]** En una entrevista te preguntan: ¿cuál de estas es una
característica de JPA?

- **A.** Obliga a usar siempre Hibernate como única implementación posible.
- **B.** Permite trabajar con objetos Java, abstrayendo los detalles de la base de datos subyacente.
- **C.** Elimina por completo la necesidad de una base de datos relacional.
- **D.** Genera automáticamente una interfaz REST para cada entidad.

_RA: RA-1_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Esa es la característica de "abstracción de la
base de datos"; las otras tres son falsas.

</details>

**2. [Abierta]** Un compañero te dice: "no entiendo para qué sirve JPQL si
ya existe SQL".

**Pregunta**: ¿Qué le responderías, mencionando las tres funcionalidades
clave de JPA?

_RA: RA-2_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: JPQL es una de las tres funcionalidades clave de JPA
(junto con las anotaciones de mapeo y la API de persistencia). A diferencia
de SQL, JPQL opera sobre **entidades** y sus **propiedades** (por ejemplo,
`Paciente` y `codigo`), no sobre tablas y columnas — eso permite escribir
consultas sin conocer el esquema físico de la base de datos, y que esas
consultas sigan funcionando aunque cambie el nombre real de una tabla o
columna, mientras la entidad no cambie.

</details>

**3. [Selección]** ¿Cuál es el punto de entrada de la arquitectura de JPA
para obtener un `EntityManager`?

- **A.** `Query`.
- **B.** `EntityTransaction`.
- **C.** `EntityManagerFactory`.
- **D.** `@Entity`.

_RA: RA-3_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. `EntityManagerFactory` crea y administra las
instancias de `EntityManager`.

</details>

**4. [Selección múltiple]** Sobre la arquitectura de JPA en una app Spring
Boot, seleccioná **todas** las afirmaciones correctas.

- **A.** El `EntityManager` se puede recibir inyectado con `@PersistenceContext`.
- **B.** La unidad de persistencia se declara en un archivo `persistence.xml`.
- **C.** `EntityTransaction` se puede gestionar con la anotación `@Transactional`.
- **D.** `EntityManagerFactory` se autoconfigura a partir de `application.properties`.

_RA: RA-3_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: en Spring Boot no hay
`persistence.xml`; la unidad de persistencia se autoconfigura.

</details>

**5. [Selección]** En este escenario:

- Un `Autor` puede haber escrito varios `Libro`.
- Un `Libro` puede tener varios `Autor`.

**Pregunta**: ¿Qué tipo de relación es?

- **A.** Uno a uno.
- **B.** Uno a muchos.
- **C.** Muchos a muchos.
- **D.** Ninguna, son entidades independientes.

_RA: RA-4_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. Ningún lado tiene un límite de "uno": ambos
admiten varias instancias relacionadas del otro lado.

</details>

**6. [Selección múltiple]** Seleccioná **todas** las afirmaciones correctas
sobre los tres tipos de relación entre entidades.

- **A.** En una relación uno a uno, ambos lados admiten como máximo una instancia relacionada.
- **B.** En una relación uno a muchos, los dos lados admiten varias instancias relacionadas entre sí.
- **C.** En una relación muchos a muchos, ningún lado tiene un límite de "uno".
- **D.** El tipo de relación se decide leyendo el dominio de negocio, no memorizando sintaxis de anotaciones.

_RA: RA-4_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: en una relación uno a
muchos, el lado "uno" sigue admitiendo solo una instancia relacionada; solo
el lado "muchos" admite varias.

</details>

**7. [Selección]** ¿Qué problema resuelve principalmente el Mapeo
Objeto-Relacional (ORM)?

- **A.** Elimina la necesidad de una base de datos relacional.
- **B.** Evita que el desarrollador escriba SQL manualmente para convertir objetos en filas y viceversa.
- **C.** Reemplaza a Java por SQL como lenguaje principal.
- **D.** Acelera la compilación del proyecto.

_RA: RA-5_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Esa es la definición de ORM.

</details>

**8. [Abierta]** En una entrevista te preguntan: "¿qué pasaría con el código
de la aplicación si mañana reemplazan Hibernate por otra implementación de
JPA, como EclipseLink?".

**Pregunta**: ¿Qué le responderías?

_RA: RA-6_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: El código de la aplicación no debería cambiar, porque
programa contra la API de JPA (`EntityManager`, `@Entity`,
`@PersistenceContext`), no contra clases propias de Hibernate. "JPA define
el qué, y Hibernate define el cómo": la implementación es intercambiable por
configuración, sin tocar la lógica de negocio.

</details>

**9. [Selección múltiple]** Seleccioná **todas** las ventajas de Spring Data
JPA.

- **A.** Menos código repetitivo para operaciones CRUD.
- **B.** Creación automática de consultas por convención de nombres de método.
- **C.** Solo funciona con MySQL.
- **D.** Soporte para consultas personalizadas con `@Query`.

_RA: RA-7_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: es compatible con
múltiples bases de datos.

</details>

**10. [Abierta]** Una interfaz `RepositorioLibros` extiende
`JpaRepository<Libro, Long>` y declara `Optional<Libro> findByIsbn(String
isbn)`, sin ningún cuerpo.

**Pregunta**: ¿Quién implementa ese método, y cómo sabe qué consulta
ejecutar?

_RA: RA-7_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Spring Data JPA genera la implementación en tiempo de
ejecución, interpretando el nombre del método: `findBy` indica una
búsqueda, e `Isbn` indica la propiedad de la entidad (`Libro.isbn`) por la
que filtrar, construyendo la consulta equivalente sin que el desarrollador
la escriba.

</details>

**11. [Selección múltiple]** Sobre un repositorio `RepositorioLibros`
convertido en `JpaRepository<Libro, Long>` con un método
`findByIsbn(String isbn)` sin cuerpo, seleccioná **todas** las afirmaciones
correctas.

- **A.** Spring Data JPA implementa `findByIsbn` en tiempo de ejecución, sin que el desarrollador escriba su cuerpo.
- **B.** `RepositorioLibrosEnMemoria` sigue siendo necesaria para que `findByIsbn` funcione.
- **C.** El repositorio ya trae `save` y `findById` sin declararlos, por extender `JpaRepository`.
- **D.** El nombre del método (`findBy` + `Isbn`) es lo que Spring Data JPA usa para construir la consulta.

_RA: RA-8_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: la implementación manual
en memoria queda obsoleta al convertir el repositorio a Spring Data JPA.

</details>

**12. [Selección]** ¿Qué efecto tiene `spring.jpa.hibernate.ddl-auto=validate`
al arrancar la aplicación?

- **A.** Crea el esquema si no existe.
- **B.** Verifica que el esquema exista y coincida con las entidades; si no, falla.
- **C.** Borra y recrea el esquema en cada arranque.
- **D.** Ignora completamente las entidades mapeadas.

_RA: RA-9_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. `validate` solo comprueba, nunca modifica el
esquema.

</details>

**13. [Abierta]** En este escenario:

- Un compañero configuró `spring.jpa.hibernate.ddl-auto=create-drop` en su
  proyecto de MediSalud.
- Cada vez que reinicia la aplicación, los pacientes cargados el día
  anterior desaparecen.

**Pregunta**: ¿Qué está pasando, y qué valor le recomendarías?

_RA: RA-9_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: `create-drop` borra el esquema completo (con todos
sus datos) cada vez que la aplicación se apaga, y lo recrea vacío al
arrancar de nuevo — por eso los pacientes cargados el día anterior
desaparecen. Para conservar los datos entre ejecuciones durante el
desarrollo, conviene usar `update`, que crea o ajusta el esquema sin borrar
los datos existentes.

</details>

**14. [Selección]** En una relación `@OneToMany`/`@ManyToOne`, ¿qué lado es
el propietario de la relación?

- **A.** El lado `@OneToMany`, porque representa "el dueño" de la colección.
- **B.** El lado `@ManyToOne`, porque su tabla tiene la columna de clave foránea.
- **C.** Ambos lados son propietarios por igual.
- **D.** Ninguno; JPA decide el lado propietario en tiempo de ejecución.

_RA: RA-11_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. El lado propietario es siempre el que declara la
clave foránea (`@JoinColumn`); en una 1-N, ese es el lado `@ManyToOne`.

</details>

**15. [Selección múltiple]** Sobre las anotaciones de JPA, seleccioná
**todas** las afirmaciones correctas.

- **A.** `@GeneratedValue` define cómo se genera el valor de la clave primaria.
- **B.** `@JoinTable` se usa en el lado inverso de una relación `@ManyToMany`.
- **C.** `@Column(unique = true)` impide que dos filas tengan el mismo valor en esa columna.
- **D.** `@Table` permite definir un nombre de tabla distinto al de la clase.

_RA: RA-10, RA-11_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: `@JoinTable` se declara en
el lado **propietario** de la relación `@ManyToMany`; el lado inverso usa
`mappedBy`.

</details>

**16. [Abierta]** En este escenario:

- Un método de servicio consulta un `Paciente` y después llama a
  `paciente.getCitas().size()`.
- El método **no** está anotado `@Transactional`.
- La aplicación lanza una excepción al ejecutar esa línea.

**Pregunta**: ¿Qué está pasando, y cómo lo solucionarías?

_RA: RA-12_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: `citas` es una colección `@OneToMany`, cargada de
forma perezosa (*LAZY*) por defecto: Hibernate no la trae de la base de
datos hasta que se accede a ella. Si la sesión de persistencia ya se cerró
(porque el método no mantuvo una transacción abierta), ese acceso tardío
falla con un error de sesión cerrada. La solución es anotar el método con
`@Transactional`, que mantiene la sesión abierta durante toda su ejecución,
permitiendo que la colección se cargue en el momento en que se accede a
ella.

</details>

---
