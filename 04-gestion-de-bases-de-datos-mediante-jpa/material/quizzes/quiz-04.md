# ❓ Quiz 04 — Gestión de Bases de Datos mediante JPA (formato entrevista técnica)

Este quiz simula las preguntas que podrías recibir en una entrevista técnica para
un puesto de desarrollador Java/Spring Boot. Cada pregunta indica su tipo
(**Selección**, **Selección múltiple** o **Abierta**). Respondé primero por tu
cuenta y después abrí "Ver respuesta" para comparar.

---

**1. [Selección]** ¿Qué herramienta se usa en este módulo para crear un
proyecto Spring Boot nuevo desde cero, sin instalar nada adicional?

- **A.** Un script de shell escrito a mano.
- **B.** Spring Initializr (`start.spring.io`).
- **C.** El comando `git init`.
- **D.** El archivo `application.properties`.

_RA: RA-10_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Spring Initializr genera el proyecto (dependencias
y estructura) a partir de un formulario, sin instalar herramientas
adicionales.

</details>

**2. [Abierta]** Un compañero te dice: "no entiendo para qué sirve elegir
dependencias en Spring Initializr si después igual tengo que configurar
`application.properties`".

**Pregunta**: ¿Qué le responderías, distinguiendo qué resuelve cada paso?

_RA: RA-11_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Son dos pasos distintos y necesarios. Elegir
dependencias en Spring Initializr (`Spring Data JPA`, `H2 Database`) agrega
las **librerías** que el proyecto necesita para poder usar JPA/Hibernate y
conectarse a H2 — sin ellas, ni siquiera compilaría una entidad `@Entity`.
`application.properties` configura **cómo** conectarse (URL, usuario,
`ddl-auto`) una vez que esas librerías ya están disponibles. Sin
dependencias, no hay con qué conectarse; sin configuración, las
dependencias no saben a qué base de datos apuntar.

</details>

**3. [Selección]** ¿Cuál de las siguientes describe mejor qué es una
relación en JPA?

- **A.** Un tipo de consulta JPQL.
- **B.** La representación en Java de una relación entre tablas de una base de datos relacional.
- **C.** Una anotación exclusiva de Spring Data JPA.
- **D.** Un mecanismo para evitar declarar claves primarias.

_RA: RA-1_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Esa es la definición general de relación en JPA.

</details>

**4. [Selección]** En una relación `@OneToOne` entre `Paciente` e
`HistoriaClinica`, donde `Paciente` declara `@JoinColumn`, ¿qué entidad es
el lado dueño?

- **A.** `HistoriaClinica`.
- **B.** `Paciente`.
- **C.** Ninguna; ambas son dueñas por igual.
- **D.** Depende del orden en que se guarden.

_RA: RA-2_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. El lado dueño es siempre el que declara
`@JoinColumn`.

</details>

**5. [Selección múltiple]** Seleccioná **todas** las afirmaciones correctas
sobre el lado dueño de una relación.

- **A.** El lado dueño es el que declara la clave foránea real (`@JoinColumn` o `@JoinTable`).
- **B.** El lado inverso usa `mappedBy` apuntando al atributo del lado dueño.
- **C.** Ambos lados de una relación pueden declarar `@JoinColumn` sin ningún problema.
- **D.** Identificar el lado dueño es necesario en los tres tipos de relación (uno a uno, uno a muchos, muchos a muchos).

_RA: RA-4_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: si ambos lados declaran
`@JoinColumn`, Hibernate genera dos relaciones independientes, no una
relación bidireccional coherente.

</details>

**6. [Selección]** En una relación `@OneToMany`/`@ManyToOne`, ¿qué lado es
el dueño?

- **A.** El lado `@OneToMany`, porque representa la colección completa.
- **B.** El lado `@ManyToOne`, porque su tabla tiene la columna de clave foránea.
- **C.** Ambos por igual.
- **D.** Ninguno; esta relación no tiene lado dueño.

_RA: RA-3_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. El lado `@ManyToOne` siempre tiene la columna de
clave foránea; el lado `@OneToMany` es el inverso (`mappedBy`).

</details>

**7. [Abierta]** En este escenario:

- Un `Paciente` tiene una colección `citas` con `fetch = FetchType.LAZY`.
- Un compañero propone cambiarlo a `EAGER` "para simplificar el código y no
  tener que pensar en transacciones".

**Pregunta**: ¿Qué le responderías?

_RA: RA-6_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: `EAGER` resuelve el síntoma (evita el error de sesión
cerrada) pero a costa de cargar siempre las citas de cada paciente, incluso
en el código que nunca las usa — un costo de rendimiento innecesario en la
mayoría de los casos. La recomendación es mantener `LAZY` (el valor por
defecto) y usar `@Transactional` en los métodos que sí necesitan acceder a
la colección.

</details>

**8. [Selección múltiple]** Seleccioná **todas** las afirmaciones correctas
sobre cuándo usar una entidad intermedia en vez de `@ManyToMany` simple.

- **A.** Cuando la relación necesita un atributo propio, no perteneciente a ninguna de las dos entidades.
- **B.** Siempre; `@ManyToMany` simple nunca es una opción válida.
- **C.** La entidad intermedia reemplaza la relación muchos a muchos por dos relaciones `@ManyToOne`.
- **D.** `@ManyToMany` simple sigue siendo válida cuando la relación no necesita datos propios.

_RA: RA-5_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: `Libro`↔`Autor` (Módulo
3) es un caso donde `@ManyToMany` simple es perfectamente válida.

</details>

**9. [Abierta]** En una entrevista te plantean este escenario:

- `Estudiante` y `Curso` tienen una relación `@ManyToMany` simple.
- Ahora piden registrar la nota final de cada estudiante en cada curso.

**Pregunta**: ¿Cómo resolverías el cambio?

_RA: RA-5_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Reemplazaría la relación `@ManyToMany` simple por una
entidad intermedia explícita (por ejemplo, `Matricula` o
`EstudianteCurso`), con un atributo propio `notaFinal`, y dos relaciones
`@ManyToOne`: una hacia `Estudiante` y otra hacia `Curso`. La nota es un
atributo de esa asociación puntual, no de ninguna de las dos entidades por
separado.

</details>

**10. [Selección]** ¿Qué operación propaga `CascadeType.REMOVE`?

- **A.** Crear.
- **B.** Actualizar.
- **C.** Eliminar.
- **D.** Consultar.

_RA: RA-7_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. `REMOVE` propaga la eliminación hacia las
entidades relacionadas.

</details>

**11. [Selección múltiple]** En este escenario:

- Una relación `@OneToMany` tiene `cascade = CascadeType.ALL` pero **no**
  tiene `orphanRemoval`.
- Se quita una entidad hija de la colección y se vuelve a guardar el padre.

Seleccioná **todas** las afirmaciones correctas.

- **A.** La entidad hija removida de la colección se elimina automáticamente de la base de datos.
- **B.** La entidad hija removida de la colección permanece en la base de datos, sin `orphanRemoval`.
- **C.** `cascade` y `orphanRemoval` son atributos independientes: se puede tener uno sin el otro.
- **D.** Agregar `orphanRemoval = true` a la misma anotación resolvería el problema.

_RA: RA-8_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: B, C, D**. La A es falsa: sin `orphanRemoval`, la
entidad quitada de la colección no se elimina de la base de datos.

</details>

**12. [Abierta]** Un compañero te dice: "quito una `Cita` de la lista de un
`Paciente` y la vuelvo a guardar, pero la cita sigue en la base de datos".
Su relación tiene `cascade = CascadeType.ALL`.

**Pregunta**: ¿Qué le falta, y por qué `cascade` solo no alcanza?

_RA: RA-8_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Le falta `orphanRemoval = true`. `cascade` propaga
operaciones que el código ejecuta explícitamente sobre el padre (guardar,
actualizar, eliminar el padre completo), pero no vigila la colección para
detectar cuándo una hija dejó de pertenecer a ella. `orphanRemoval` es,
específicamente, el atributo que detecta ese caso y elimina la hija
huérfana — son dos mecanismos distintos, aunque suelen combinarse.

</details>

**13. [Selección]** ¿Cuál de las siguientes describe correctamente el rol
de `save(...)` en un CRUD con Spring Data JPA?

- **A.** Solo sirve para crear; existe un método distinto para actualizar.
- **B.** Crea un registro nuevo si la entidad no tiene `id`, y actualiza el existente si ya lo tiene.
- **C.** Siempre crea un registro nuevo, sin importar si la entidad tiene `id`.
- **D.** Solo funciona si la entidad fue previamente leída con `findById`.

_RA: RA-9_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Hibernate decide crear o actualizar según si la
entidad ya tiene un `id` asignado.

</details>

**14. [Abierta]** Te piden implementar un CRUD para una entidad
`Configuracion` del sistema, con la particularidad de que **nunca** debe
poder eliminarse una vez creada (por auditoría).

**Pregunta**: ¿Cómo adaptarías el CRUD completo visto en el Ejemplo 06 para
cumplir esa restricción?

_RA: RA-9_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Implementaría solo tres de las cuatro operaciones:
crear (`save` sin `id`), leer (`findBy...`) y actualizar (`save` con
`id`), pero **no** expondría ni llamaría a `deleteById`/`delete` en ningún
punto del código para `Configuracion`. `JpaRepository` sigue trayendo el
método disponible, pero un CRUD parcial es simplemente la decisión de no
usar la parte que no corresponde al caso de uso, igual que en el Ejercicio
Intermedio 03 con `Autor`.

</details>

**15. [Selección]** Si el log de arranque de una aplicación Spring Boot
nunca muestra una línea `Started <Clase> in ... seconds`, ¿qué se puede
concluir?

- **A.** La aplicación arrancó correctamente, pero sin mostrar esa línea.
- **B.** El `ApplicationContext` no terminó de inicializarse: algo falló antes de completar el arranque.
- **C.** La aplicación arrancó, pero tardó menos de un segundo.
- **D.** No se puede concluir nada sin ver el resto del log.

_RA: RA-11_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Esa línea solo aparece cuando Spring Boot
termina de inicializar el `ApplicationContext` sin errores; su ausencia
indica un fallo durante el arranque.

</details>

**16. [Abierta]** Un compañero te muestra un log de error largo, con varias
excepciones anidadas (`BeanCreationException`, luego otra excepción debajo,
y así sucesivamente), y no sabe por dónde empezar a leerlo.

**Pregunta**: ¿Qué estrategia le recomendarías para encontrar la causa real
del problema?

_RA: RA-11_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Le recomendaría buscar la línea `Caused by:` (o, en
el caso de errores de conexión, la excepción específica del driver de base
de datos, como `JdbcSQLNonTransientConnectionException`), que suele estar
más abajo en el log. Las excepciones que la envuelven (como
`BeanCreationException`) solo indican *qué bean* falló al crearse, pero no
*por qué*; la causa raíz real casi siempre está en la última excepción de
la cadena, y su mensaje suele contener el valor exacto de configuración
que está mal.

</details>

**17. [Selección múltiple]** Sobre el ejercicio integrador que combina un
proyecto Spring Boot nuevo, una relación padre-hijas con `cascade`/
`orphanRemoval`, y operaciones de alta y edición, seleccioná **todas** las
afirmaciones correctas.

- **A.** El repositorio de Spring Data JPA solo necesita declararse para la entidad padre, no para cada línea de detalle.
- **B.** Guardar el padre con `cascade` configurado también guarda sus líneas de detalle nuevas, sin un repositorio propio para ellas.
- **C.** Verificar que el proyecto arranca correctamente contra H2 es parte de validar el ejercicio, no solo un paso previo.
- **D.** `orphanRemoval` es opcional si el ejercicio solo pide alta y edición, nunca eliminación.

_RA: RA-12_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, C**. La D es falsa en el caso general: si el
ejercicio integrador (como el Desafío 01) pide explícitamente eliminar una
línea de la colección, `orphanRemoval` deja de ser opcional.

</details>

**18. [Abierta]** Te piden diseñar, para un sistema de Biblioteca
Universitaria, un caso integrador con una `OrdenCompra` y sus líneas
`DetalleOrdenCompra`, sin copiar directamente el patrón `Factura`/
`DetalleFactura` del Taller.

**Pregunta**: ¿Qué elementos del diseño del Taller reutilizarías como
criterio, y cuáles deberías construir de cero para que no sea una copia
mecánica?

_RA: RA-12_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Reutilizaría el **criterio** de diseño: una relación
padre-hijas (`@OneToMany`/`@ManyToOne`) con `cascade`/`orphanRemoval` desde
el padre hacia sus líneas, y un repositorio de Spring Data JPA solo para el
padre. Construiría de cero los **nombres y atributos concretos**
(`OrdenCompra`, `DetalleOrdenCompra`, `isbn`, `cantidad`), el caso de
negocio (adquisición de libros, no facturación), y las operaciones
específicas del `Main` — el patrón se transfiere, pero el modelado y el
código concreto son propios de este caso.

</details>

---
