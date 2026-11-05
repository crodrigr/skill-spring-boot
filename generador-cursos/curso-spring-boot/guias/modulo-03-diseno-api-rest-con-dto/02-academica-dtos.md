# 📘 Guía paso a paso — Academia con DTO, mapeadores y controladores delgados

> 👩‍🏫 **Material docente.** Aplica las mejoras de la [guía del Módulo 05](01-guia-dto-y-estructura-de-controladores.md) (DTO, mapeador, `ServiceResult`, rutas con versión y controladores delgados) al proyecto de la [guía 01 del módulo 03-04](../modulo-02-jpa-y-bases-de-datos/01-guia-taller-gestion-de-cursos-con-bd.md): **estudiantes**, **cursos** y **matrículas** con MySQL.
> Contiene código completo: no se entrega a los estudiantes antes de resolver el taller.
>
> 📦 **Proyecto terminado:** [`academica-dtos/`](codigo/academica-dtos/) · 🧾 **Todo el código final, listo para copiar:** [`03-academica-dtos-codigo-completo.md`](03-academica-dtos-codigo-completo.md).
>
> ✅ **Verificada.** El código se compiló para Java 21 y se ejecutó con Spring Boot 4.1.2 y MySQL 8. Las respuestas HTTP, los encabezados y el contenido de la tabla que se muestran son salidas reales.
>
> 🧭 **Idea central.** El Módulo 05 enseñó el patrón con un solo recurso. Aquí se repite con **tres recursos relacionados**, y aparece un caso nuevo: la **matrícula**, que en la entidad lleva **dos entidades anidadas** y en el DTO se aplana.

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Pasar el taller de la academia de «los controladores devuelven entidades» a «los controladores reciben y devuelven DTO».
>
> Al terminar, el estudiante:
>
> - Reconoce **seis problemas** de exponer las entidades `StudentEntity`, `CourseEntity` y `EnrollmentEntity`.
> - Crea DTO de entrada (`…Request`) y de salida (`…Response`) para los tres recursos, y **aplana** la matrícula.
> - Mueve las **validaciones** de las reglas del servicio al DTO de entrada (`@Valid`).
> - Escribe un **mapeador** por recurso.
> - Hace que el servicio distinga `404` y `409` con `ServiceResult`.
> - Convierte cada servicio en una **interfaz** (`business/service`) con su **implementación** `…Impl` (`business/impl`).
> - Diseña los endpoints: **versión** en la ruta, `Location` al crear, **paginación** con tope y ids en el **cuerpo**.
> - Prueba cada endpoint con `curl` y ve qué **método del controlador** atiende cada petición.

</details>

<details>
<summary><strong>📝 Introducción: qué cambia respecto al Módulo 05</strong></summary>

> <details>
> <summary><strong>🔁 Lo que se repite</strong></summary>
>
> | Pieza | Igual que en el Módulo 05 |
> |---|---|
> | DTO como `record` | `…Request` valida (`@NotBlank`, `@Min`…), `…Response` expone solo lo necesario |
> | Mapeador estático | `toEntity`, `toResponse`, `toPageResponse` |
> | `ServiceResult` / `ServiceStatus` | `OK`, `NOT_FOUND`, `CONFLICT`; se reemplaza por excepciones en el Módulo 7 |
> | Controlador delgado | Recibe, llama al servicio, traduce a HTTP |
> | Rutas | `/api/v1/…`, `Location` al crear, `spring.data.web.pageable.*` |
>
> </details>
>
> <details>
> <summary><strong>🆕 Lo que es nuevo en la academia</strong></summary>
>
> | Novedad | Por qué importa |
> |---|---|
> | **Tres recursos** | El patrón se repite por recurso: 2–3 DTO y un mapeador cada uno. |
> | **Matrícula con relaciones** | `EnrollmentEntity` guarda un `StudentEntity` y un `CourseEntity` completos. Devolverlos tal cual arrastra todos sus campos. |
> | **DTO plano para la matrícula** | `EnrollmentResponse` lleva solo `studentId`, `studentName`, `courseId` y `courseName`. |
> | **Ids en el cuerpo** | `POST /enrollments` pasa de `?studentId=&courseId=` a un cuerpo `EnrollmentRequest`. |
> | **Un `404` real** | Antes todo era `409`; ahora «no existe» y «ya existe / sin cupo» se distinguen. |
> | **Un número se vuelve objeto** | `GET /courses/{id}/enrolled` devolvía un `Long` suelto; ahora devuelve `EnrolledResponse`. |
>
> </details>
>
> <details>
> <summary><strong>🔍 Entidad frente a DTO en la academia</strong></summary>
>
> | Recurso | Entidad (la tabla) | DTO de entrada | DTO de salida |
> |---|---|---|---|
> | Estudiante | `id`, `name`, `age`, `email`, `createdAt` | `name`, `age`, `email` | `id`, `name`, `age`, `email` |
> | Curso | `id`, `name`, `price`, `maxCapacity` | `name`, `price`, `maxCapacity` | `id`, `name`, `price`, `maxCapacity` |
> | Matrícula | `id`, `student` (entidad), `course` (entidad) | `studentId`, `courseId` | `id`, `studentId`, `studentName`, `courseId`, `courseName` |
>
> </details>
>
> <details>
> <summary><strong>🛠️ Requerimientos y restricciones</strong></summary>
>
> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Java 21 o superior, Spring Boot 4.1.2, Maven y MySQL 8 (base `academy`). |
> | **RNF-02** | **Ningún controlador importa nada de `jakarta.persistence`** ni devuelve una entidad. |
> | **RNF-03** | Los DTO son `record`. Las validaciones de forma viven **solo** en los DTO de entrada. |
> | **RNF-04** | Los errores de negocio se informan con `ServiceResult`; las excepciones propias y un manejador global se estudian en el Módulo 7. |
> | **RNF-05** | Nombres del código en **inglés**. Inyección por constructor. |
> | **RNF-06** | Duración: 1 h 30 min. |
>
> </details>

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>

> | Paso | Minutos | Actividad |
> |---|---:|---|
> | 0 | 0 – 10 | Punto de partida: el taller de la academia |
> | 1 | 10 – 20 | **Ver los problemas** de exponer las entidades |
> | 2 | 20 – 35 | Los DTO |
> | 3 | 35 – 43 | Los mapeadores |
> | 4 | 43 – 53 | Los servicios informan el resultado (`ServiceResult`) |
> | 5 | 53 – 65 | Los controladores delgados |
> | 6 | 65 – 72 | Diseño de endpoints: versión, `Location`, paginación y errores |
> | 7 | 72 – 90 | Probar la API |
>
> > 💡 **Ritmo.** El Paso 0 es el taller ya resuelto; el tiempo se dedica a los Pasos 1 a 7.

</details>

<details>
<summary><strong>🏗️ Arquitectura: el árbol de archivos (🆕 nuevo · ✏️ cambia)</strong></summary>

> ```text
> 📁 academica-dtos
> ├── ✏️ pom.xml                                   + spring-boot-starter-validation
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 presentation
>     │   │   ├── 📁 controller
>     │   │   │   ├── ✏️ StudentController.java     DTO · Location · página
>     │   │   │   ├── ✏️ CourseController.java
>     │   │   │   └── ✏️ EnrollmentController.java  cuerpo JSON · 404 / 409
>     │   │   ├── 📁 dto
>     │   │   │   ├── 🆕 StudentRequest.java
>     │   │   │   ├── 🆕 StudentResponse.java
>     │   │   │   ├── 🆕 CourseRequest.java
>     │   │   │   ├── 🆕 CourseResponse.java
>     │   │   │   ├── 🆕 EnrolledResponse.java
>     │   │   │   ├── 🆕 EnrollmentRequest.java
>     │   │   │   ├── 🆕 EnrollmentResponse.java
>     │   │   │   └── 🆕 PageResponse.java
>     │   │   └── 📁 mapper
>     │   │       ├── 🆕 StudentMapper.java
>     │   │       ├── 🆕 CourseMapper.java
>     │   │       └── 🆕 EnrollmentMapper.java
>     │   ├── 📁 business
>     │   │   ├── 📁 service
>     │   │   │   ├── ✏️ StudentService.java         ahora es una interfaz
>     │   │   │   ├── ✏️ CourseService.java          ahora es una interfaz
>     │   │   │   ├── ✏️ EnrollmentService.java      ahora es una interfaz
>     │   │   │   ├── 🆕 ServiceResult.java
>     │   │   │   └── 🆕 ServiceStatus.java
>     │   │   └── 📁 impl
>     │   │       ├── 🆕 StudentServiceImpl.java
>     │   │       ├── 🆕 CourseServiceImpl.java
>     │   │       └── 🆕 EnrollmentServiceImpl.java
>     │   └── 📁 persistence
>     │       ├── 📁 entity
>     │       │   ├── ✏️ StudentEntity.java          + campo interno createdAt
>     │       │   ├── 📄 CourseEntity.java
>     │       │   └── 📄 EnrollmentEntity.java
>     │       └── 📁 repository                      sin cambios
>     │           ├── 📄 StudentJpaRepository.java
>     │           ├── 📄 CourseJpaRepository.java
>     │           └── 📄 EnrollmentJpaRepository.java
>     └── 📁 resources
>         └── ✏️ application.properties             límites de paginación y errores
> ```

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Punto de partida: el taller de la academia</strong></summary>

> <details>
> <summary><strong>Paso 0.1 · El proyecto</strong></summary>
>
> > Se parte del proyecto que se termina en la [guía 01 del módulo 03-04](../modulo-02-jpa-y-bases-de-datos/01-guia-taller-gestion-de-cursos-con-bd.md) (su sección **Estructura final esperada** trae todo el código). Si hace falta, se crea la base:
> >
> > ```bash
> > docker run -d --name academy-mysql \
> >   -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academy \
> >   -p 3306:3306 mysql:8.0
> > ```
> >
> > > | Lo que **ya existe** | Dónde |
> > > |---|---|
> > > | `StudentEntity`, `CourseEntity`, `EnrollmentEntity` | `persistence.entity` |
> > > | `StudentJpaRepository`, `CourseJpaRepository`, `EnrollmentJpaRepository` | `persistence.repository` |
> > > | `StudentService`, `CourseService`, `EnrollmentService` (devuelven `Optional`) | `business.service` |
> > > | Tres controladores en `/api/students`, `/api/courses` y `/api/enrollments` | `presentation.controller` |
> > >
> > > En esta versión los controladores reciben y devuelven **entidades**, y todo error es un `409`.
>
> </details>
>

</details>

---

<details>
<summary><strong>🔎 Paso 1 — Ver los problemas de exponer las entidades</strong></summary>

> <details>
> <summary><strong>Paso 1.1 · Un campo interno aparece en el JSON</strong></summary>
>
> > Se agrega a `StudentEntity` un campo de **auditoría interna**, `createdAt`, que Hibernate rellena solo. Es un cambio razonable en la base; no se toca ningún controlador.
> >
> > <details>
> > <summary><strong>📄 StudentEntity.java (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/entity/StudentEntity.java`
> >
> > ```java
> > import jakarta.persistence.Column;
> > import org.hibernate.annotations.CreationTimestamp;
> > import java.time.LocalDateTime;
> >
> >     @CreationTimestamp
> >     @Column(name = "created_at", updatable = false)
> >     private LocalDateTime createdAt;
> >
> >     public LocalDateTime getCreatedAt() { return createdAt; }
> > ```
> >
> > </details>
> >
> > Se crea un estudiante y se mira la respuesta. **Resultado real:**
> >
> > ```text
> > POST /api/students  →  201
> > ```
> >
> > ```json
> > {
> >   "id": 1,
> >   "name": "Ana Torres",
> >   "age": 22,
> >   "email": "ana@mail.com",
> >   "createdAt": "2026-11-01T15:14:16.728928"   // ⚠️ campo interno que se filtró
> > }
> > ```
> >
> > > ⚠️ **Nadie lo pidió.** `createdAt` es de **auditoría interna**, pero el cliente ya lo recibe: la API **cambió sin que se tocara el controlador**.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 1.2 · El cliente puede pisar una fila ajena</strong></summary>
>
> > La misma clase sirve de **entrada**. Como la entidad expone `getId()`, Jackson también **escribe** el `id`. Se envía un `POST` que trae `"id": 1`, el de Ana:
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"id":1,"name":"Intruso","age":99,"email":"intruso@mail.com","createdAt":"2000-01-01T00:00:00"}'
> > ```
> >
> > **Resultado real:** `201` con el cuerpo
> >
> > ```json
> > {
> >   "id": 1,
> >   "name": "Intruso",
> >   "age": 99,
> >   "email": "intruso@mail.com",
> >   "createdAt": "2000-01-01T00:00:00"
> > }
> > ```
> >
> > Y el listado, después (`GET /api/students`):
> >
> > ```json
> > [
> >   {
> >     "id": 1,
> >     "name": "Intruso",
> >     "age": 99,
> >     "email": "intruso@mail.com",
> >     "createdAt": "2026-11-01T15:14:16.728928"
> >   }
> > ]
> > ```
> >
> > Ana ya no está. (La fecha real se conservó porque la columna es `updatable = false`, pero el resto de la fila fue reemplazado.)
> >
> > Un `POST` que debía **crear** terminó **reemplazando a Ana**: `save` con `id` informado actualiza esa fila.
> >
> > > 🔎 **Concepto:** esto se llama **asignación masiva** (*mass assignment*): enviar campos de más que se enlazan al objeto sin que nadie los haya autorizado. Aquí es más grave que un dato falso: es **sobrescribir una fila**.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 1.3 · No hay validación de forma</strong></summary>
>
> > Las reglas del servicio solo miran el correo repetido. Un estudiante sin nombre, con edad negativa y un correo que no es correo:
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"","age":-5,"email":"x"}'
> > ```
> >
> > **Resultado real:** `201` con `{"id":2,"name":"","age":-5,"email":"x",…}`. Quedó guardado en la tabla.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 1.4 · La matrícula devuelve entidades anidadas</strong></summary>
>
> > `EnrollmentEntity` guarda un estudiante y un curso **completos**. Al matricular, la respuesta los repite enteros:
> >
> > ```json
> > {
> >   "id": 1,
> >   "student": {
> >     "id": 1,
> >     "name": "Intruso",
> >     "age": 99,
> >     "email": "intruso@mail.com",
> >     "createdAt": "2026-11-01T15:14:16.728928"
> >   },
> >   "course": {
> >     "id": 1,
> >     "name": "Java from scratch",
> >     "price": 150000.0,
> >     "maxCapacity": 1
> >   }
> > }
> > ```
> >
> > Dos problemas: se **filtra** todo lo que tenga el estudiante (incluido `createdAt`) y la respuesta crece cada vez que las entidades ganan campos.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 1.5 · Todo fallo es un <code>409</code></strong></summary>
>
> > Cuatro causas distintas dan **la misma** respuesta. **Resultado real:**
> >
> > | Petición | Causa | Respuesta |
> > |---|---|---|
> > | `POST /api/enrollments?studentId=1&courseId=1` (otra vez) | Matrícula repetida | `409` |
> > | `POST /api/enrollments?studentId=9&courseId=1` | **El estudiante no existe** | `409` |
> > | `POST /api/enrollments?studentId=2&courseId=1` | Curso sin cupo | `409` |
> > | `POST /api/courses` con precio `0` | **Dato inválido** | `409` |
> >
> > El cliente no puede distinguir «no existe» de «ya existe», ni un dato inválido de un conflicto.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 1.6 · El resumen de problemas</strong></summary>
>
> > | # | Problema | Dónde se ve |
> > |---|---|---|
> > | 1 | Un campo interno llega al cliente | `createdAt` en el JSON (Paso 1.1, 1.4) |
> > | 2 | El cliente manda campos que no debe y pisa filas | `"id":1` reemplaza a Ana (Paso 1.2) |
> > | 3 | No se valida la forma de los datos | Nombre vacío y edad `-5` guardados (Paso 1.3) |
> > | 4 | La respuesta arrastra entidades completas | La matrícula anida estudiante y curso (Paso 1.4) |
> > | 5 | Los errores no se distinguen | Todo es `409` (Paso 1.5) |
> > | 6 | Contrato atado a la tabla, sin versión ni paginación | Renombrar una columna rompe a los clientes; listados sin tope; rutas `/api/...` |
> >
> > > 🗣️ **Pregunta:** ¿cuál de los seis es el más peligroso? *(El 2: no es un dato feo, es una fila sobrescrita.)*
> > >
> > > ✅ **Punto de control 1:** los problemas se reproducen en el proyecto de partida.
>
> </details>
>

</details>

---

<details>
<summary><strong>📦 Paso 2 — Los DTO (<code>presentation.dto</code>)</strong></summary>

> <details>
> <summary><strong>Paso 2.1 · Validación y paginación</strong></summary>
>
> > Los DTO de entrada llevan las reglas de forma, así que se agrega la dependencia de **Bean Validation** al `pom.xml`:
> >
> > <details>
> > <summary><strong>📄 pom.xml (agregar)</strong></summary>
> >
> > **Ruta:** `pom.xml`
> >
> > ```xml
> > <dependency>
> >     <groupId>org.springframework.boot</groupId>
> >     <artifactId>spring-boot-starter-validation</artifactId>
> > </dependency>
> > ```
> >
> > </details>
> >
> > > 💡 **Beneficio:** `@NotBlank`, `@Min`, `@Positive`… se escriben **una vez**, en el DTO, y `@Valid` en el controlador las ejecuta.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 2.2 · Estudiantes: <code>StudentRequest</code> y <code>StudentResponse</code></strong></summary>
>
> > Lo que **entra** (con reglas) y lo que **sale** (sin `createdAt`). `StudentRequest` no tiene `id`: el cliente no puede mandarlo.
> >
> > <details>
> > <summary><strong>📄 StudentRequest.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/StudentRequest.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import jakarta.validation.constraints.Email;
> > import jakarta.validation.constraints.Max;
> > import jakarta.validation.constraints.Min;
> > import jakarta.validation.constraints.NotBlank;
> > import jakarta.validation.constraints.Size;
> >
> > public record StudentRequest(
> >
> >         @NotBlank
> >         @Size(min = 2, max = 100)
> >         String name,
> >
> >         @Min(16)
> >         @Max(99)
> >         int age,
> >
> >         @NotBlank
> >         @Email
> >         String email
> > ) {
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 StudentResponse.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/StudentResponse.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > public record StudentResponse(Long id, String name, int age, String email) {
> > }
> > ```
> >
> > </details>
> >
> > > | Anotación | Regla |
> > > |---|---|
> > > | `@NotBlank` · `@Size(min = 2, max = 100)` | Nombre con contenido y de largo razonable |
> > > | `@Min(16)` · `@Max(99)` | Rango de edad |
> > > | `@NotBlank` · `@Email` | Correo con forma de correo |
> > >
> > > 💡 **Beneficio:** el problema 3 queda resuelto en el borde de la API, antes de llegar al servicio. Y el 1 y el 2 también: el DTO simplemente **no tiene** esos campos.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 2.3 · Cursos: <code>CourseRequest</code>, <code>CourseResponse</code> y <code>EnrolledResponse</code></strong></summary>
>
> > La regla «precio y cupo mayores que cero» deja el servicio y pasa al DTO como anotaciones. `EnrolledResponse` envuelve el conteo en un objeto.
> >
> > <details>
> > <summary><strong>📄 CourseRequest.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/CourseRequest.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import jakarta.validation.constraints.Min;
> > import jakarta.validation.constraints.NotBlank;
> > import jakarta.validation.constraints.Positive;
> > import jakarta.validation.constraints.Size;
> >
> > public record CourseRequest(
> >
> >         @NotBlank
> >         @Size(min = 2, max = 100)
> >         String name,
> >
> >         @Positive
> >         double price,
> >
> >         @Min(1)
> >         int maxCapacity
> > ) {
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 CourseResponse.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/CourseResponse.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > public record CourseResponse(Long id, String name, double price, int maxCapacity) {
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 EnrolledResponse.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/EnrolledResponse.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > public record EnrolledResponse(Long courseId, long enrolled) {
> > }
> > ```
> >
> > </details>
> >
> > > | Antes | Después |
> > > |---|---|
> > > | `if (price <= 0 \|\| maxCapacity <= 0) return Optional.empty();` en el servicio | `@Positive double price` y `@Min(1) int maxCapacity` en `CourseRequest` |
> > > | Respuesta `409` sin explicación | Respuesta `400` (dato inválido) |
> > > | `GET /courses/{id}/enrolled` devolvía `1` | Devuelve `{"courseId":1,"enrolled":1}` (extensible) |
> > >
> > > 💡 **Beneficio:** un dato **inválido** (`400`) se separa de un **conflicto** (`409`), y un número suelto se vuelve un objeto al que se le pueden agregar campos sin romper a nadie.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 2.4 · Matrículas: <code>EnrollmentRequest</code> y <code>EnrollmentResponse</code></strong></summary>
>
> > La entrada trae solo los **ids**. La salida **aplana** la relación: en lugar de dos entidades anidadas, cuatro campos.
> >
> > <details>
> > <summary><strong>📄 EnrollmentRequest.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/EnrollmentRequest.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import jakarta.validation.constraints.NotNull;
> >
> > public record EnrollmentRequest(
> >
> >         @NotNull
> >         Long studentId,
> >
> >         @NotNull
> >         Long courseId
> > ) {
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 EnrollmentResponse.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/EnrollmentResponse.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > public record EnrollmentResponse(Long id, Long studentId, String studentName, Long courseId, String courseName) {
> > }
> > ```
> >
> > </details>
> >
> > > | | Entidad | DTO |
> > > |---|---|---|
> > > | Quién se matricula | `student` (objeto con 5 campos) | `studentId` y `studentName` |
> > > | Qué curso | `course` (objeto con 4 campos) | `courseId` y `courseName` |
> > > | Entrada | `?studentId=&courseId=` en la URL | Cuerpo JSON con `@NotNull` en cada id |
> > >
> > > 💡 **Beneficio:** el problema 4 queda resuelto. La respuesta muestra lo que el cliente necesita para pintar «Ana en Java», no el expediente completo de cada uno.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 2.5 · <code>PageResponse</code>: las páginas</strong></summary>
>
> > Un único envoltorio genérico para todos los listados, igual al del Módulo 05:
> >
> > <details>
> > <summary><strong>📄 PageResponse.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/PageResponse.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import java.util.List;
> >
> > public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
> > }
> > ```
> >
> > </details>
> >
> > > 💡 **Beneficio:** el cliente recibe `content` más `page`, `size`, `totalElements` y `totalPages` sin depender de cómo Spring serialice `Page`.
> >
> > ✅ **Punto de control 2:** el proyecto **compila** con los ocho DTO (todavía nada los usa).
>
> </details>
>

</details>

---

<details>
<summary><strong>🔁 Paso 3 — Los mapeadores (<code>presentation.mapper</code>)</strong></summary>

> <details>
> <summary><strong>Paso 3.1 · Un mapeador por recurso</strong></summary>
>
> > Una clase `final` con métodos estáticos y constructor privado. Convierte en los dos sentidos y construye la página.
> >
> > <details>
> > <summary><strong>📄 StudentMapper.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/mapper/StudentMapper.java`
> >
> > ```java
> > package com.academy.presentation.mapper;
> >
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.presentation.dto.PageResponse;
> > import com.academy.presentation.dto.StudentRequest;
> > import com.academy.presentation.dto.StudentResponse;
> > import org.springframework.data.domain.Page;
> >
> > public final class StudentMapper {
> >
> >     private StudentMapper() {
> >     }
> >
> >     public static StudentEntity toEntity(StudentRequest request) {
> >         return new StudentEntity(request.name(), request.age(), request.email());
> >     }
> >
> >     public static StudentResponse toResponse(StudentEntity entity) {
> >         return new StudentResponse(entity.getId(), entity.getName(), entity.getAge(), entity.getEmail());
> >     }
> >
> >     public static PageResponse<StudentResponse> toPageResponse(Page<StudentEntity> page) {
> >         return new PageResponse<>(
> >                 page.getContent().stream().map(StudentMapper::toResponse).toList(),
> >                 page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 CourseMapper.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/mapper/CourseMapper.java`
> >
> > ```java
> > package com.academy.presentation.mapper;
> >
> > import com.academy.persistence.entity.CourseEntity;
> > import com.academy.presentation.dto.CourseRequest;
> > import com.academy.presentation.dto.CourseResponse;
> > import com.academy.presentation.dto.PageResponse;
> > import org.springframework.data.domain.Page;
> >
> > public final class CourseMapper {
> >
> >     private CourseMapper() {
> >     }
> >
> >     public static CourseEntity toEntity(CourseRequest request) {
> >         return new CourseEntity(request.name(), request.price(), request.maxCapacity());
> >     }
> >
> >     public static CourseResponse toResponse(CourseEntity entity) {
> >         return new CourseResponse(entity.getId(), entity.getName(), entity.getPrice(), entity.getMaxCapacity());
> >     }
> >
> >     public static PageResponse<CourseResponse> toPageResponse(Page<CourseEntity> page) {
> >         return new PageResponse<>(
> >                 page.getContent().stream().map(CourseMapper::toResponse).toList(),
> >                 page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 EnrollmentMapper.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/mapper/EnrollmentMapper.java`
> >
> > ```java
> > package com.academy.presentation.mapper;
> >
> > import com.academy.persistence.entity.EnrollmentEntity;
> > import com.academy.presentation.dto.EnrollmentResponse;
> >
> > public final class EnrollmentMapper {
> >
> >     private EnrollmentMapper() {
> >     }
> >
> >     public static EnrollmentResponse toResponse(EnrollmentEntity entity) {
> >         return new EnrollmentResponse(
> >                 entity.getId(),
> >                 entity.getStudent().getId(), entity.getStudent().getName(),
> >                 entity.getCourse().getId(), entity.getCourse().getName());
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Método | Qué hace |
> > > |---|---|
> > > | `toEntity(request)` | Del DTO de entrada a la entidad. **No** pasa `id`: la entidad nace sin él y `save` siempre inserta |
> > > | `toResponse(entity)` | De la entidad al DTO de salida, copiando solo lo permitido |
> > > | `toPageResponse(page)` | De una `Page` de entidades a una `PageResponse` de DTO |
> > > | `EnrollmentMapper.toResponse` | **Aplana** la relación: lee `getStudent().getName()` y `getCourse().getName()` |
> > >
> > > 💡 **Beneficio:** la traducción vive en **un solo lugar**. Si la entidad cambia, se edita el mapeador y el contrato no se mueve. El problema 2 queda resuelto del todo: aunque alguien agregara un `id` al DTO, el mapeador no lo copia.
> > >
> > > ✅ **Punto de control 3:** compila. `EnrollmentMapper` es la única clase que conoce la forma de la matrícula.
>
> </details>
>

</details>

---

<details>
<summary><strong>⚙️ Paso 4 — Los servicios informan el resultado (<code>ServiceResult</code>)</strong></summary>

> <details>
> <summary><strong>Paso 4.1 · Un resultado en lugar de un <code>Optional</code> ambiguo</strong></summary>
>
> > Un `Optional` vacío no dice **por qué** falló. `ServiceResult` lleva el estado (`OK`, `NOT_FOUND` o `CONFLICT`) y el valor. Son las mismas dos clases del Módulo 05:
> >
> > <details>
> > <summary><strong>📄 ServiceStatus.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/ServiceStatus.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > public enum ServiceStatus {
> >     OK,
> >     NOT_FOUND,
> >     CONFLICT
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 ServiceResult.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/ServiceResult.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > public record ServiceResult<T>(ServiceStatus status, T value) {
> >
> >     public static <T> ServiceResult<T> ok(T value) {
> >         return new ServiceResult<>(ServiceStatus.OK, value);
> >     }
> >
> >     public static <T> ServiceResult<T> notFound() {
> >         return new ServiceResult<>(ServiceStatus.NOT_FOUND, null);
> >     }
> >
> >     public static <T> ServiceResult<T> conflict() {
> >         return new ServiceResult<>(ServiceStatus.CONFLICT, null);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > 💡 **Beneficio:** el servicio **decide** el motivo y el controlador solo lo **traduce** a HTTP. Cuando lleguen las excepciones (Módulo 7), este puente se reemplaza sin tocar las reglas.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 4.2 · Los tres servicios</strong></summary>
>
> > Cada servicio se separa en una **interfaz** (`StudentService`, `CourseService`, `EnrollmentService` en `business/service`) y una clase `@Service` que la implementa (`…ServiceImpl` en `business/impl`). Los controladores siguen inyectando la interfaz.
> >
> > Qué cambia en cada uno:
> >
> > | Servicio | Cambio |
> > |---|---|
> > | `StudentService` | `findAll(Pageable)` devuelve una `Page`; `create` devuelve `ServiceResult` (`CONFLICT` si el correo existe); nuevo `findById` |
> > | `CourseService` | `findAll(Pageable)`, `findById` y `create` **sin** la validación de precio y cupo; `countEnrolled` devuelve `ServiceResult<Long>` |
> > | `EnrollmentService` | `enroll` distingue `NOT_FOUND` (estudiante o curso inexistente) de `CONFLICT` (repetida o sin cupo); nuevo `findById` |
> >
> > <details>
> > <summary><strong>📄 StudentService.java</strong> <em>(interfaz)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/StudentService.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > import com.academy.persistence.entity.StudentEntity;
> > import org.springframework.data.domain.Page;
> > import org.springframework.data.domain.Pageable;
> >
> > import java.util.Optional;
> >
> > public interface StudentService {
> >
> >     Page<StudentEntity> findAll(Pageable pageable);
> >
> >     Optional<StudentEntity> findById(Long id);
> >
> >     ServiceResult<StudentEntity> create(StudentEntity student);
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 StudentServiceImpl.java</strong> <em>(implementación)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/impl/StudentServiceImpl.java`
> >
> > ```java
> > package com.academy.business.impl;
> >
> > import com.academy.business.service.ServiceResult;
> > import com.academy.business.service.StudentService;
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.persistence.repository.StudentJpaRepository;
> > import org.springframework.data.domain.Page;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
> >
> > import java.util.Optional;
> >
> > @Service
> > public class StudentServiceImpl implements StudentService {
> >
> >     private final StudentJpaRepository students;
> >
> >     public StudentServiceImpl(StudentJpaRepository students) {
> >         this.students = students;
> >     }
> >
> >     @Override
> >     @Transactional(readOnly = true)
> >     public Page<StudentEntity> findAll(Pageable pageable) {
> >         return students.findAll(pageable);
> >     }
> >
> >     @Override
> >     @Transactional(readOnly = true)
> >     public Optional<StudentEntity> findById(Long id) {
> >         return students.findById(id);
> >     }
> >
> >     @Override
> >     @Transactional
> >     public ServiceResult<StudentEntity> create(StudentEntity student) {
> >         if (students.existsByEmailIgnoreCase(student.getEmail())) {
> >             return ServiceResult.conflict();
> >         }
> >         return ServiceResult.ok(students.save(student));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > 💡 **Interfaz e implementación:** `StudentService` es ahora un **contrato** (una `interface` con las operaciones) y `StudentServiceImpl` es la clase `@Service` que lo cumple. Los controladores siguen inyectando `StudentService`; Spring entrega la implementación. Así la capa de presentación depende del contrato y no de la clase concreta.
> >
> > <details>
> > <summary><strong>📄 CourseService.java</strong> <em>(interfaz)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/CourseService.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > import com.academy.persistence.entity.CourseEntity;
> > import org.springframework.data.domain.Page;
> > import org.springframework.data.domain.Pageable;
> >
> > import java.util.Optional;
> >
> > public interface CourseService {
> >
> >     Page<CourseEntity> findAll(Pageable pageable);
> >
> >     Optional<CourseEntity> findById(Long id);
> >
> >     CourseEntity create(CourseEntity course);
> >
> >     ServiceResult<Long> countEnrolled(Long courseId);
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 CourseServiceImpl.java</strong> <em>(implementación)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/impl/CourseServiceImpl.java`
> >
> > ```java
> > package com.academy.business.impl;
> >
> > import com.academy.business.service.CourseService;
> > import com.academy.business.service.ServiceResult;
> > import com.academy.persistence.entity.CourseEntity;
> > import com.academy.persistence.repository.CourseJpaRepository;
> > import com.academy.persistence.repository.EnrollmentJpaRepository;
> > import org.springframework.data.domain.Page;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
> >
> > import java.util.Optional;
> >
> > @Service
> > public class CourseServiceImpl implements CourseService {
> >
> >     private final CourseJpaRepository courses;
> >     private final EnrollmentJpaRepository enrollments;
> >
> >     public CourseServiceImpl(CourseJpaRepository courses, EnrollmentJpaRepository enrollments) {
> >         this.courses = courses;
> >         this.enrollments = enrollments;
> >     }
> >
> >     @Override
> >     @Transactional(readOnly = true)
> >     public Page<CourseEntity> findAll(Pageable pageable) {
> >         return courses.findAll(pageable);
> >     }
> >
> >     @Override
> >     @Transactional(readOnly = true)
> >     public Optional<CourseEntity> findById(Long id) {
> >         return courses.findById(id);
> >     }
> >
> >     @Override
> >     @Transactional
> >     public CourseEntity create(CourseEntity course) {
> >         return courses.save(course);
> >     }
> >
> >     @Override
> >     @Transactional(readOnly = true)
> >     public ServiceResult<Long> countEnrolled(Long courseId) {
> >         if (!courses.existsById(courseId)) {
> >             return ServiceResult.notFound();
> >         }
> >         return ServiceResult.ok(enrollments.countByCourseId(courseId));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 EnrollmentService.java</strong> <em>(interfaz)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/EnrollmentService.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > import com.academy.persistence.entity.EnrollmentEntity;
> >
> > import java.util.Optional;
> >
> > public interface EnrollmentService {
> >
> >     Optional<EnrollmentEntity> findById(Long id);
> >
> >     ServiceResult<EnrollmentEntity> enroll(Long studentId, Long courseId);
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 EnrollmentServiceImpl.java</strong> <em>(implementación)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/impl/EnrollmentServiceImpl.java`
> >
> > ```java
> > package com.academy.business.impl;
> >
> > import com.academy.business.service.EnrollmentService;
> > import com.academy.business.service.ServiceResult;
> > import com.academy.persistence.entity.CourseEntity;
> > import com.academy.persistence.entity.EnrollmentEntity;
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.persistence.repository.CourseJpaRepository;
> > import com.academy.persistence.repository.EnrollmentJpaRepository;
> > import com.academy.persistence.repository.StudentJpaRepository;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
> >
> > import java.util.Optional;
> >
> > @Service
> > public class EnrollmentServiceImpl implements EnrollmentService {
> >
> >     private final StudentJpaRepository students;
> >     private final CourseJpaRepository courses;
> >     private final EnrollmentJpaRepository enrollments;
> >
> >     public EnrollmentServiceImpl(StudentJpaRepository students, CourseJpaRepository courses,
> >                                  EnrollmentJpaRepository enrollments) {
> >         this.students = students;
> >         this.courses = courses;
> >         this.enrollments = enrollments;
> >     }
> >
> >     @Override
> >     @Transactional(readOnly = true)
> >     public Optional<EnrollmentEntity> findById(Long id) {
> >         return enrollments.findById(id);
> >     }
> >
> >     @Override
> >     @Transactional
> >     public ServiceResult<EnrollmentEntity> enroll(Long studentId, Long courseId) {
> >         Optional<StudentEntity> student = students.findById(studentId);
> >         Optional<CourseEntity> course = courses.findById(courseId);
> >         if (student.isEmpty() || course.isEmpty()) {
> >             return ServiceResult.notFound();
> >         }
> >         if (enrollments.existsByStudentIdAndCourseId(studentId, courseId)) {
> >             return ServiceResult.conflict();
> >         }
> >         if (enrollments.countByCourseId(courseId) >= course.get().getMaxCapacity()) {
> >             return ServiceResult.conflict();
> >         }
> >         return ServiceResult.ok(enrollments.save(new EnrollmentEntity(student.get(), course.get())));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > 💡 **Beneficio:** el problema 5 queda resuelto. «No existe» (`404`) y «no se puede» (`409`) son casos distintos desde el servicio. Y el servicio se queda con las **reglas de negocio** (correo único, cupo, matrícula repetida); las de **forma** ya no están aquí.
> > >
> > > ✅ **Punto de control 4:** compila (los controladores viejos aún no coinciden: se reescriben en el Paso 5).
>
> </details>
>

</details>

---

<details>
<summary><strong>🌐 Paso 5 — Los controladores delgados</strong></summary>

> <details>
> <summary><strong>Paso 5.1 · Los tres controladores</strong></summary>
>
> > Cada método hace tres cosas: **valida** (`@Valid`), **llama** al servicio con el resultado del mapeador y **traduce** a HTTP. Ninguno importa `jakarta.persistence`.
> >
> > <details>
> > <summary><strong>📄 StudentController.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > package com.academy.presentation.controller;
> >
> > import com.academy.business.service.ServiceResult;
> > import com.academy.business.service.ServiceStatus;
> > import com.academy.business.service.StudentService;
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.presentation.dto.PageResponse;
> > import com.academy.presentation.dto.StudentRequest;
> > import com.academy.presentation.dto.StudentResponse;
> > import com.academy.presentation.mapper.StudentMapper;
> > import jakarta.validation.Valid;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.http.ResponseEntity;
> > import org.springframework.web.bind.annotation.GetMapping;
> > import org.springframework.web.bind.annotation.PathVariable;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.RequestBody;
> > import org.springframework.web.bind.annotation.RequestMapping;
> > import org.springframework.web.bind.annotation.RestController;
> > import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
> >
> > import java.net.URI;
> >
> > @RestController
> > @RequestMapping("/api/v1/students")
> > public class StudentController {
> >
> >     private final StudentService service;
> >
> >     public StudentController(StudentService service) {
> >         this.service = service;
> >     }
> >
> >     @GetMapping
> >     public PageResponse<StudentResponse> findAll(Pageable pageable) {
> >         return StudentMapper.toPageResponse(service.findAll(pageable));
> >     }
> >
> >     @GetMapping("/{id}")
> >     public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
> >         return service.findById(id)
> >                 .map(StudentMapper::toResponse)
> >                 .map(ResponseEntity::ok)
> >                 .orElseGet(() -> ResponseEntity.notFound().build());
> >     }
> >
> >     @PostMapping
> >     public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >         ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> >         if (result.status() == ServiceStatus.CONFLICT) {
> >             return ResponseEntity.status(HttpStatus.CONFLICT).build();
> >         }
> >         StudentResponse body = StudentMapper.toResponse(result.value());
> >         URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                 .path("/{id}").buildAndExpand(body.id()).toUri();
> >         return ResponseEntity.created(location).body(body);
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
> > import com.academy.business.service.ServiceResult;
> > import com.academy.persistence.entity.CourseEntity;
> > import com.academy.presentation.dto.CourseRequest;
> > import com.academy.presentation.dto.CourseResponse;
> > import com.academy.presentation.dto.EnrolledResponse;
> > import com.academy.presentation.dto.PageResponse;
> > import com.academy.presentation.mapper.CourseMapper;
> > import jakarta.validation.Valid;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.http.ResponseEntity;
> > import org.springframework.web.bind.annotation.GetMapping;
> > import org.springframework.web.bind.annotation.PathVariable;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.RequestBody;
> > import org.springframework.web.bind.annotation.RequestMapping;
> > import org.springframework.web.bind.annotation.RestController;
> > import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
> >
> > import java.net.URI;
> >
> > @RestController
> > @RequestMapping("/api/v1/courses")
> > public class CourseController {
> >
> >     private final CourseService service;
> >
> >     public CourseController(CourseService service) {
> >         this.service = service;
> >     }
> >
> >     @GetMapping
> >     public PageResponse<CourseResponse> findAll(Pageable pageable) {
> >         return CourseMapper.toPageResponse(service.findAll(pageable));
> >     }
> >
> >     @GetMapping("/{id}")
> >     public ResponseEntity<CourseResponse> findById(@PathVariable Long id) {
> >         return service.findById(id)
> >                 .map(CourseMapper::toResponse)
> >                 .map(ResponseEntity::ok)
> >                 .orElseGet(() -> ResponseEntity.notFound().build());
> >     }
> >
> >     @PostMapping
> >     public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
> >         CourseEntity created = service.create(CourseMapper.toEntity(request));
> >         CourseResponse body = CourseMapper.toResponse(created);
> >         URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                 .path("/{id}").buildAndExpand(body.id()).toUri();
> >         return ResponseEntity.created(location).body(body);
> >     }
> >
> >     @GetMapping("/{id}/enrolled")
> >     public ResponseEntity<EnrolledResponse> countEnrolled(@PathVariable Long id) {
> >         ServiceResult<Long> result = service.countEnrolled(id);
> >         return switch (result.status()) {
> >             case OK -> ResponseEntity.ok(new EnrolledResponse(id, result.value()));
> >             case NOT_FOUND, CONFLICT -> ResponseEntity.notFound().build();
> >         };
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
> > import com.academy.business.service.ServiceResult;
> > import com.academy.persistence.entity.EnrollmentEntity;
> > import com.academy.presentation.dto.EnrollmentRequest;
> > import com.academy.presentation.dto.EnrollmentResponse;
> > import com.academy.presentation.mapper.EnrollmentMapper;
> > import jakarta.validation.Valid;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.http.ResponseEntity;
> > import org.springframework.web.bind.annotation.GetMapping;
> > import org.springframework.web.bind.annotation.PathVariable;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.RequestBody;
> > import org.springframework.web.bind.annotation.RequestMapping;
> > import org.springframework.web.bind.annotation.RestController;
> > import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
> >
> > import java.net.URI;
> >
> > @RestController
> > @RequestMapping("/api/v1/enrollments")
> > public class EnrollmentController {
> >
> >     private final EnrollmentService service;
> >
> >     public EnrollmentController(EnrollmentService service) {
> >         this.service = service;
> >     }
> >
> >     @GetMapping("/{id}")
> >     public ResponseEntity<EnrollmentResponse> findById(@PathVariable Long id) {
> >         return service.findById(id)
> >                 .map(EnrollmentMapper::toResponse)
> >                 .map(ResponseEntity::ok)
> >                 .orElseGet(() -> ResponseEntity.notFound().build());
> >     }
> >
> >     @PostMapping
> >     public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
> >         ServiceResult<EnrollmentEntity> result = service.enroll(request.studentId(), request.courseId());
> >         return switch (result.status()) {
> >             case NOT_FOUND -> ResponseEntity.notFound().build();
> >             case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
> >             case OK -> {
> >                 EnrollmentResponse body = EnrollmentMapper.toResponse(result.value());
> >                 URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                         .path("/{id}").buildAndExpand(body.id()).toUri();
> >                 yield ResponseEntity.created(location).body(body);
> >             }
> >         };
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Cambio | Dónde |
> > > |---|---|
> > > | `@RequestMapping("/api/v1/…")` | Los tres: la versión va en la ruta |
> > > | `@Valid @RequestBody …Request` | Los `POST`: dispara las validaciones y responde `400` |
> > > | `ResponseEntity.created(location)` | Los `POST`: `201` con `Location` |
> > > | `PageResponse<…>` | `findAll` de estudiantes y de cursos |
> > > | `switch` sobre `result.status()` | `countEnrolled` y `enroll`: cada estado a su código |
> > > | `GET /{id}` | Nuevos: son el destino de `Location` |
> > > | `@RequestBody EnrollmentRequest` | `enroll` ya no lee `?studentId=&courseId=` |
> > >
> > > 💡 **Beneficio:** cada controlador se lee **de arriba abajo en segundos**. Las reglas se pueden cambiar sin tocarlos.
> > >
> > > ✅ **Punto de control 5:** el proyecto arranca (`Started AcademyApplication`).
>
> </details>
>

</details>

---

<details>
<summary><strong>🧭 Paso 6 — Diseño de los endpoints</strong></summary>

> <details>
> <summary><strong>Paso 6.1 · Convenciones que se aplicaron</strong></summary>
>
> > | Convención | Antes | Después | Por qué |
> > |---|---|---|---|
> > | Versión en la ruta | `/api/students` | `/api/v1/students` | Permite una versión 2 sin romper a los clientes de la 1 |
> > | Crear devuelve `201` + `Location` | `201` sin cabecera | `201` + `Location: …/students/1` | El cliente sabe dónde quedó el recurso |
> > | Dato inválido | `409` | `400` | Un dato mal formado no es un conflicto |
> > | Recurso inexistente | `409` | `404` | «No existe» es distinto de «ya existe» |
> > | Listado | Arreglo con **todo** | `PageResponse` con tope de 50 | Nadie pide la tabla completa de golpe |
> > | Datos de una creación | `?studentId=&courseId=` en la URL | Cuerpo JSON `EnrollmentRequest` | `POST` lleva datos en el cuerpo, y se validan |
> > | Un número | `1` | `{"courseId":1,"enrolled":1}` | Se puede ampliar sin romper |
> >
> > > 🔎 **Concepto:** los ids que identifican el recurso (`/students/{id}`) van en la **ruta**; los datos que crean algo (`studentId`, `courseId`) van en el **cuerpo**.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 6.2 · Limitar la página y esconder los detalles del error</strong></summary>
>
> > Dos grupos de propiedades en `application.properties`:
> >
> > <details>
> > <summary><strong>📄 application.properties (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/resources/application.properties`
> >
> > ```properties
> > # Pagination limits (protect the API)
> > spring.data.web.pageable.default-page-size=10
> > spring.data.web.pageable.max-page-size=50
> >
> > # Error responses: do not expose internals (DevTools shows them by default)
> > server.error.include-stacktrace=never
> > server.error.include-message=never
> > server.error.include-binding-errors=never
> > ```
> >
> > </details>
> >
> > > | Propiedad | Para qué sirve |
> > > |---|---|
> > > | `spring.data.web.pageable.default-page-size` | Tamaño de página cuando el cliente no pide uno |
> > > | `spring.data.web.pageable.max-page-size` | **Tope**: con `?size=1000` Spring responde con `size` 50 |
> > > | `server.error.include-stacktrace` · `include-message` · `include-binding-errors` | Con **DevTools** (que usa este proyecto), Spring Boot muestra por defecto la traza completa en los errores. `never` evita exponer el interior de la aplicación |
> > >
> > > ⚠️ **Detalle real.** Sin estas tres líneas, el `400` de validación incluía un campo `trace` con la **traza completa** de la excepción, porque DevTools activa esos datos en desarrollo.
> > >
> > > 💡 **Beneficio:** la API se protege de pedidos enormes y no filtra su interior. Un formato de error propio y uniforme se estudia en el Módulo 7.
>
> </details>
>

</details>

---

<details>
<summary><strong>🧪 Paso 7 — Probar la API</strong></summary>

> <details>
> <summary><strong>Paso 7.1 · Las pruebas con <code>curl</code></strong></summary>
>
> > Se arranca la aplicación con MySQL en marcha (`./mvnw spring-boot:run`) y se ejecuta cada comando en otra terminal. Debajo de cada uno está la **respuesta real**.
> >
> > > 💡 La API devuelve el JSON en una sola línea; aquí se muestra con sangría para leerlo mejor. Los `404` y `409` del controlador llegan **sin cuerpo**; los `400` de validación traen el cuerpo genérico de Spring Boot.
> >
> > <details>
> > <summary><strong>1. Estudiantes · crear → <code>201</code> y la cabecera <code>Location</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `StudentController.create(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >     ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> >     if (result.status() == ServiceStatus.CONFLICT) {
> >         return ResponseEntity.status(HttpStatus.CONFLICT).build();
> >     }
> >     StudentResponse body = StudentMapper.toResponse(result.value());
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > Entra por `create`: valida el cuerpo (`@Valid`), lo convierte a entidad con el mapeador y responde `201` con `Location`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"Ana Torres","age":22,"email":"ana@mail.com"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 201
> > Location: http://localhost:8080/api/v1/students/1
> > Content-Type: application/json
> >
> > {
> >   "id": 1,
> >   "name": "Ana Torres",
> >   "age": 22,
> >   "email": "ana@mail.com"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>2. Estudiantes · crear enviando campos de más (<code>id</code> y <code>createdAt</code>) → <code>201</code>; ambos se ignoran</strong></summary>
> >
> > **Método del controlador que atiende la petición:** `StudentController.create(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >     ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> >     if (result.status() == ServiceStatus.CONFLICT) {
> >         return ResponseEntity.status(HttpStatus.CONFLICT).build();
> >     }
> >     StudentResponse body = StudentMapper.toResponse(result.value());
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > `StudentRequest` no tiene `id` ni `createdAt`: Jackson ignora lo que no existe en el `record`. Se contrasta con el Paso 1.2, donde el `id` forjado **pisaba** a Ana.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"id":99,"name":"Luis Pérez","age":25,"email":"luis@mail.com","createdAt":"2000-01-01T00:00:00"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 201
> > Location: http://localhost:8080/api/v1/students/2
> > Content-Type: application/json
> >
> > {
> >   "id": 2,
> >   "name": "Luis Pérez",
> >   "age": 25,
> >   "email": "luis@mail.com"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>3. Estudiantes · correo repetido, en mayúsculas → <code>409</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `StudentController.create(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >     ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> >     if (result.status() == ServiceStatus.CONFLICT) {
> >         return ResponseEntity.status(HttpStatus.CONFLICT).build();
> >     }
> >     StudentResponse body = StudentMapper.toResponse(result.value());
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > `StudentService.create` detecta el correo repetido y devuelve `CONFLICT`; `create` lo traduce a `409`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"Otra","age":30,"email":"ANA@MAIL.COM"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 409
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>4. Estudiantes · datos inválidos → <code>400</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `StudentController.create(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >     ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> >     if (result.status() == ServiceStatus.CONFLICT) {
> >         return ResponseEntity.status(HttpStatus.CONFLICT).build();
> >     }
> >     StudentResponse body = StudentMapper.toResponse(result.value());
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > `@Valid` falla **antes** de ejecutar el cuerpo de `create`: Spring responde `400` y el método no llega a correr.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"","age":5,"email":"x"}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 400
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-01T20:15:55.151Z",
> >   "status": 400,
> >   "error": "Bad Request",
> >   "path": "/api/v1/students"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>5. Estudiantes · listar con tamaño excesivo y orden → <code>200</code> con <code>"size":50</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `StudentController.findAll(...)`
> >
> > ```java
> > @GetMapping
> > public PageResponse<StudentResponse> findAll(Pageable pageable) {
> >     return StudentMapper.toPageResponse(service.findAll(pageable));
> > }
> > ```
> >
> > Entra por `findAll`, que arma una `PageResponse`. El tope de 50 lo aplica Spring antes, con `spring.data.web.pageable.max-page-size`.
> >
> > ```bash
> > curl -i "http://localhost:8080/api/v1/students?size=1000&sort=name,asc"
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "content": [
> >     {
> >       "id": 1,
> >       "name": "Ana Torres",
> >       "age": 22,
> >       "email": "ana@mail.com"
> >     },
> >     {
> >       "id": 2,
> >       "name": "Luis Pérez",
> >       "age": 25,
> >       "email": "luis@mail.com"
> >     }
> >   ],
> >   "page": 0,
> >   "size": 50,
> >   "totalElements": 2,
> >   "totalPages": 1
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>6. Estudiantes · buscar por id → <code>200</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `StudentController.findById(...)`
> >
> > ```java
> > @GetMapping("/{id}")
> > public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
> >     return service.findById(id)
> >             .map(StudentMapper::toResponse)
> >             .map(ResponseEntity::ok)
> >             .orElseGet(() -> ResponseEntity.notFound().build());
> > }
> > ```
> >
> > Entra por `findById`: el `Optional` con entidad pasa por el mapeador y se envuelve en `200`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/students/1
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "id": 1,
> >   "name": "Ana Torres",
> >   "age": 22,
> >   "email": "ana@mail.com"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>7. Estudiantes · buscar un id que no existe → <code>404</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `StudentController.findById(...)`
> >
> > ```java
> > @GetMapping("/{id}")
> > public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
> >     return service.findById(id)
> >             .map(StudentMapper::toResponse)
> >             .map(ResponseEntity::ok)
> >             .orElseGet(() -> ResponseEntity.notFound().build());
> > }
> > ```
> >
> > El `Optional` está vacío y `orElseGet` responde `404`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/students/99
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>8. Cursos · crear → <code>201</code> y la cabecera <code>Location</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `CourseController.create(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
> >     CourseEntity created = service.create(CourseMapper.toEntity(request));
> >     CourseResponse body = CourseMapper.toResponse(created);
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > Entra por `create`. El precio y el cupo ya se validaron en `CourseRequest`; el servicio solo guarda.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/courses \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"Java from scratch","price":150000,"maxCapacity":1}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 201
> > Location: http://localhost:8080/api/v1/courses/1
> > Content-Type: application/json
> >
> > {
> >   "id": 1,
> >   "name": "Java from scratch",
> >   "price": 150000.0,
> >   "maxCapacity": 1
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>9. Cursos · precio <code>0</code> y cupo <code>0</code> → <code>400</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `CourseController.create(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
> >     CourseEntity created = service.create(CourseMapper.toEntity(request));
> >     CourseResponse body = CourseMapper.toResponse(created);
> >     URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >             .path("/{id}").buildAndExpand(body.id()).toUri();
> >     return ResponseEntity.created(location).body(body);
> > }
> > ```
> >
> > `@Positive` y `@Min(1)` fallan en `CourseRequest`. Antes esta regla vivía en el servicio y respondía `409`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/courses \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"X","price":0,"maxCapacity":0}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 400
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-01T20:15:55.257Z",
> >   "status": 400,
> >   "error": "Bad Request",
> >   "path": "/api/v1/courses"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>10. Cursos · listar → <code>200</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `CourseController.findAll(...)`
> >
> > ```java
> > @GetMapping
> > public PageResponse<CourseResponse> findAll(Pageable pageable) {
> >     return CourseMapper.toPageResponse(service.findAll(pageable));
> > }
> > ```
> >
> > Entra por `findAll`: devuelve la misma `PageResponse` que los estudiantes, ahora con `CourseResponse`.
> >
> > ```bash
> > curl -i "http://localhost:8080/api/v1/courses?page=0&size=5"
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "content": [
> >     {
> >       "id": 1,
> >       "name": "Java from scratch",
> >       "price": 150000.0,
> >       "maxCapacity": 1
> >     }
> >   ],
> >   "page": 0,
> >   "size": 5,
> >   "totalElements": 1,
> >   "totalPages": 1
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>11. Cursos · cuántos matriculados → <code>200</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `CourseController.countEnrolled(...)`
> >
> > ```java
> > @GetMapping("/{id}/enrolled")
> > public ResponseEntity<EnrolledResponse> countEnrolled(@PathVariable Long id) {
> >     ServiceResult<Long> result = service.countEnrolled(id);
> >     return switch (result.status()) {
> >         case OK -> ResponseEntity.ok(new EnrolledResponse(id, result.value()));
> >         case NOT_FOUND, CONFLICT -> ResponseEntity.notFound().build();
> >     };
> > }
> > ```
> >
> > Entra por `countEnrolled`: el servicio devuelve `ServiceResult<Long>` y el controlador lo envuelve en un objeto JSON (`EnrolledResponse`), no en un número suelto.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/courses/1/enrolled
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "courseId": 1,
> >   "enrolled": 0
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>12. Cursos · conteo de un curso que no existe → <code>404</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `CourseController.countEnrolled(...)`
> >
> > ```java
> > @GetMapping("/{id}/enrolled")
> > public ResponseEntity<EnrolledResponse> countEnrolled(@PathVariable Long id) {
> >     ServiceResult<Long> result = service.countEnrolled(id);
> >     return switch (result.status()) {
> >         case OK -> ResponseEntity.ok(new EnrolledResponse(id, result.value()));
> >         case NOT_FOUND, CONFLICT -> ResponseEntity.notFound().build();
> >     };
> > }
> > ```
> >
> > El servicio devuelve `NOT_FOUND` y el `switch` lo traduce a `404`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/courses/9/enrolled
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>13. Matrículas · matricular → <code>201</code> y la cabecera <code>Location</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `EnrollmentController.enroll(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
> >     ServiceResult<EnrollmentEntity> result = service.enroll(request.studentId(), request.courseId());
> >     return switch (result.status()) {
> >         case NOT_FOUND -> ResponseEntity.notFound().build();
> >         case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
> >         case OK -> {
> >             EnrollmentResponse body = EnrollmentMapper.toResponse(result.value());
> >             URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                     .path("/{id}").buildAndExpand(body.id()).toUri();
> >             yield ResponseEntity.created(location).body(body);
> >         }
> >     };
> > }
> > ```
> >
> > Entra por `enroll`: los ids viajan en el **cuerpo** (`EnrollmentRequest`) y la respuesta es plana (`EnrollmentResponse`), no dos entidades anidadas.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/enrollments \
> >      -H "Content-Type: application/json" \
> >      -d '{"studentId":1,"courseId":1}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 201
> > Location: http://localhost:8080/api/v1/enrollments/1
> > Content-Type: application/json
> >
> > {
> >   "id": 1,
> >   "studentId": 1,
> >   "studentName": "Ana Torres",
> >   "courseId": 1,
> >   "courseName": "Java from scratch"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>14. Matrículas · la misma matrícula otra vez → <code>409</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `EnrollmentController.enroll(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
> >     ServiceResult<EnrollmentEntity> result = service.enroll(request.studentId(), request.courseId());
> >     return switch (result.status()) {
> >         case NOT_FOUND -> ResponseEntity.notFound().build();
> >         case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
> >         case OK -> {
> >             EnrollmentResponse body = EnrollmentMapper.toResponse(result.value());
> >             URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                     .path("/{id}").buildAndExpand(body.id()).toUri();
> >             yield ResponseEntity.created(location).body(body);
> >         }
> >     };
> > }
> > ```
> >
> > El servicio devuelve `CONFLICT` (ya existe) y el `switch` responde `409`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/enrollments \
> >      -H "Content-Type: application/json" \
> >      -d '{"studentId":1,"courseId":1}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 409
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>15. Matrículas · curso sin cupo → <code>409</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `EnrollmentController.enroll(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
> >     ServiceResult<EnrollmentEntity> result = service.enroll(request.studentId(), request.courseId());
> >     return switch (result.status()) {
> >         case NOT_FOUND -> ResponseEntity.notFound().build();
> >         case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
> >         case OK -> {
> >             EnrollmentResponse body = EnrollmentMapper.toResponse(result.value());
> >             URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                     .path("/{id}").buildAndExpand(body.id()).toUri();
> >             yield ResponseEntity.created(location).body(body);
> >         }
> >     };
> > }
> > ```
> >
> > El curso tiene cupo `1` y ya está ocupado: `CONFLICT` → `409`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/enrollments \
> >      -H "Content-Type: application/json" \
> >      -d '{"studentId":2,"courseId":1}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 409
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>16. Matrículas · estudiante inexistente → <code>404</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `EnrollmentController.enroll(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
> >     ServiceResult<EnrollmentEntity> result = service.enroll(request.studentId(), request.courseId());
> >     return switch (result.status()) {
> >         case NOT_FOUND -> ResponseEntity.notFound().build();
> >         case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
> >         case OK -> {
> >             EnrollmentResponse body = EnrollmentMapper.toResponse(result.value());
> >             URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                     .path("/{id}").buildAndExpand(body.id()).toUri();
> >             yield ResponseEntity.created(location).body(body);
> >         }
> >     };
> > }
> > ```
> >
> > Ahora el servicio **distingue** el caso: `NOT_FOUND` → `404`. En la versión anterior era un `409` igual al resto.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/enrollments \
> >      -H "Content-Type: application/json" \
> >      -d '{"studentId":9,"courseId":1}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>17. Matrículas · falta un campo → <code>400</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `EnrollmentController.enroll(...)`
> >
> > ```java
> > @PostMapping
> > public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
> >     ServiceResult<EnrollmentEntity> result = service.enroll(request.studentId(), request.courseId());
> >     return switch (result.status()) {
> >         case NOT_FOUND -> ResponseEntity.notFound().build();
> >         case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
> >         case OK -> {
> >             EnrollmentResponse body = EnrollmentMapper.toResponse(result.value());
> >             URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                     .path("/{id}").buildAndExpand(body.id()).toUri();
> >             yield ResponseEntity.created(location).body(body);
> >         }
> >     };
> > }
> > ```
> >
> > `@NotNull` en `studentId` falla antes de ejecutar `enroll`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/enrollments \
> >      -H "Content-Type: application/json" \
> >      -d '{"courseId":1}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 400
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-01T20:15:55.539Z",
> >   "status": 400,
> >   "error": "Bad Request",
> >   "path": "/api/v1/enrollments"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>18. Matrículas · buscar por id → <code>200</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `EnrollmentController.findById(...)`
> >
> > ```java
> > @GetMapping("/{id}")
> > public ResponseEntity<EnrollmentResponse> findById(@PathVariable Long id) {
> >     return service.findById(id)
> >             .map(EnrollmentMapper::toResponse)
> >             .map(ResponseEntity::ok)
> >             .orElseGet(() -> ResponseEntity.notFound().build());
> > }
> > ```
> >
> > Es el destino de la cabecera `Location` del `201`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/enrollments/1
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "id": 1,
> >   "studentId": 1,
> >   "studentName": "Ana Torres",
> >   "courseId": 1,
> >   "courseName": "Java from scratch"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>19. Matrículas · buscar un id que no existe → <code>404</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `EnrollmentController.findById(...)`
> >
> > ```java
> > @GetMapping("/{id}")
> > public ResponseEntity<EnrollmentResponse> findById(@PathVariable Long id) {
> >     return service.findById(id)
> >             .map(EnrollmentMapper::toResponse)
> >             .map(ResponseEntity::ok)
> >             .orElseGet(() -> ResponseEntity.notFound().build());
> > }
> > ```
> >
> > El `Optional` está vacío: `404`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/enrollments/99
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>20. La ruta sin versión ya no existe → <code>404</code></strong></summary>
> >
> > **Método del controlador:** ninguno.
> >
> > No hay ningún controlador en `/api/students`: el `404` lo genera Spring antes de llegar a `StudentController`.
> >
> > ```bash
> > curl -i http://localhost:8080/api/students
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-01T20:15:55.586Z",
> >   "status": 404,
> >   "error": "Not Found",
> >   "path": "/api/students"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>21. Cursos · el conteo después de matricular → <code>200</code></strong></summary>
> >
> > **Método del controlador que atiende la petición:** `CourseController.countEnrolled(...)`
> >
> > ```java
> > @GetMapping("/{id}/enrolled")
> > public ResponseEntity<EnrolledResponse> countEnrolled(@PathVariable Long id) {
> >     ServiceResult<Long> result = service.countEnrolled(id);
> >     return switch (result.status()) {
> >         case OK -> ResponseEntity.ok(new EnrolledResponse(id, result.value()));
> >         case NOT_FOUND, CONFLICT -> ResponseEntity.notFound().build();
> >     };
> > }
> > ```
> >
> > Vuelve a `countEnrolled`: ahora hay una matrícula.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/courses/1/enrolled
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "courseId": 1,
> >   "enrolled": 1
> > }
> > ```
> >
> > </details>
> >
> > > 💡 **Beneficio de este paso:** se comprueba el **contrato** completo y se ve cómo cada método del controlador traduce un resultado de negocio a un código HTTP.
> > >
> > > En MySQL, la columna interna sigue existiendo aunque la API no la muestre:
> > >
> > > ```text
> > > mysql> select id, name, created_at is not null as has_created_at from student;
> > > +----+------------+----------------+
> > > | id | name       | has_created_at |
> > > +----+------------+----------------+
> > > |  1 | Ana Torres |              1 |
> > > |  2 | Luis Pérez |              1 |
> > > +----+------------+----------------+
> > > ```
> > >
> > > 🗣️ **Preguntas:** ¿qué pasó con el `"id":99` que mandó el cliente? *(Nunca llegó: `StudentRequest` no tiene `id`.)* ¿Por qué el estudiante inexistente da `404` y antes daba `409`? *(Ahora el servicio devuelve `NOT_FOUND` y el controlador lo traduce.)*
> > >
> > > ✅ **Punto de control 7:** las respuestas coinciden con las que se muestran bajo cada comando.
>
> </details>
>

</details>

---

<details>
<summary><strong>⚖️ Balance: antes y después</strong></summary>

> | Aspecto | Antes (entidades como contrato) | Después (DTO y controladores delgados) |
> |---|---|---|
> | Contrato de la API | La tabla | Lo que se decide mostrar |
> | Campos internos | `createdAt` se filtra | Quedan ocultos |
> | Lo que el cliente puede enviar | Cualquier campo, incluido `id` | Solo lo permitido; el `id` no existe en la entrada |
> | Crear con un `id` ajeno | **Pisa** la fila | Se ignora; siempre inserta |
> | Validación de forma | Ninguna | En el DTO (`400`) |
> | Matrícula | Dos entidades anidadas | Cuatro campos planos |
> | Errores | Todo `409` | `400` · `404` · `409` según el caso |
> | Rutas | `/api/students` | `/api/v1/students` |
> | Listados | Todo el contenido | Página con tope de 50 |
> | Cambiar una columna | Rompe a los clientes | Solo cambia el mapeador |
>
> > ⚠️ **Lo que aún no está resuelto.** Los `404` y `409` **no traen cuerpo**, y el `400` trae solo un mensaje genérico que **no dice qué campo falló**. Un formato de error uniforme y las excepciones propias se estudian en el Módulo 7. Y la entidad todavía llega al servicio: separar también un **modelo** del negocio se ve en la [guía de la Fase II](../modulo-02-jpa-y-bases-de-datos/02-guia-taller-gestion-de-cursos-una-version-profesional.md).

</details>

<details>
<summary><strong>🏁 Cierre — preguntas de síntesis</strong></summary>

> 1. ¿Por qué el `POST` con `"id":1` pisaba a Ana, y por qué ya no pasa? *(La entidad exponía `getId()` y `save` actualiza si hay `id`; `StudentRequest` no tiene `id` y el mapeador no lo copia.)*
> 2. ¿Qué se gana aplanando la matrícula? *(La respuesta no arrastra los campos de dos entidades y no crece cuando ellas cambian.)*
> 3. ¿Por qué la validación de precio y cupo pasó del servicio al DTO? *(Es una regla de **forma** del dato: se rechaza en el borde con `400`, antes de entrar a la lógica de negocio.)*
> 4. ¿Qué regla se queda en el servicio? *(Las que necesitan consultar la base: correo único, matrícula repetida, cupo.)*
> 5. ¿Qué diferencia hay entre `404` y `409` en la matrícula? *(`404`: el estudiante o el curso no existen; `409`: existen, pero la matrícula no se puede hacer.)*
> 6. ¿Por qué los ids de la matrícula pasaron al cuerpo? *(Son los datos que crean el recurso; en el cuerpo se validan con `@NotNull`.)*

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Solución |
> |---|---|---|
> | La validación **no** se ejecuta | Falta `@Valid` o la dependencia `spring-boot-starter-validation` | Agregarlas |
> | `@NotBlank` en un `int` falla al arrancar | Esa anotación es para texto | Usar `@Min` / `@Positive` para números |
> | `404` en una ruta que existe | Falta el prefijo de versión (`/api/v1`) | Revisar `@RequestMapping` |
> | `NullPointerException` en `toResponse` | Se pasó `null` (un `ServiceResult` no `OK`) | Comprobar el estado **antes** de mapear |
> | `EnrollmentMapper` falla con `LazyInitializationException` | Se cambió la relación a `LAZY` y se mapea fuera de la transacción | Mapear dentro del servicio o cargar la relación |
> | El `400` trae una traza larga | Falta `server.error.include-stacktrace=never` con DevTools | Agregar las tres propiedades |
> | El tamaño de página no se limita | Falta `spring.data.web.pageable.max-page-size` | Agregarlo |
> | `Communications link failure` al arrancar | MySQL no está arrancado o el puerto es otro | `docker ps` y revisar la URL |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **`PUT` y `DELETE`.** Agregar `PUT /students/{id}` (con `StudentRequest`) y `DELETE /students/{id}`; ¿qué estado devuelve el servicio si el correo es de otro?
> 2. **`PATCH` de precio.** Agregar `PATCH /courses/{id}/price` con un `CoursePriceRequest` de un solo campo.
> 3. **Matrículas de un estudiante.** `GET /students/{id}/enrollments` que devuelva una lista de `EnrollmentResponse`.
> 4. **Respuesta resumida.** `CourseSummaryResponse` (`id`, `name`) para el listado y `CourseResponse` para el detalle.
> 5. **Cupos disponibles.** Agregar `available` a `EnrolledResponse` (cupo máximo menos matriculados).
> 6. **Error con cuerpo.** Devolver un `ProblemDetail` en los `404` y `409`. ¿Qué cambiaría al llegar al Módulo 7?
> 7. **MapStruct.** Reemplazar los tres mapeadores por interfaces de MapStruct y comparar.
> 8. **Documentarla.** Agregar Swagger siguiendo la [guía del Módulo 06](../modulo-04-documentacion-apis-swagger/01-guia-crud-estudiantes-con-swagger.md): los `@Schema` van en los DTO.

</details>

<details>
<summary><strong>🏷️ Repaso de conceptos y anotaciones</strong></summary>

> | Elemento | Dónde | Para qué sirve |
> |---|---|---|
> | `record` (DTO) | `presentation.dto` | Contrato de entrada o salida, inmutable |
> | `@NotBlank` · `@Size` · `@Min` · `@Max` · `@Email` · `@Positive` · `@NotNull` | DTO de entrada | Reglas de forma del cuerpo |
> | `@Valid` | Parámetro `@RequestBody` | Dispara las validaciones: `400` si fallan |
> | `@RequestBody` · `@PathVariable` | Controlador | Cuerpo JSON y parte de la ruta |
> | `@GetMapping` · `@PostMapping` | Controlador | Verbo HTTP de cada método |
> | `ResponseEntity` | Controlador | Elegir código, encabezados y cuerpo |
> | `ServletUriComponentsBuilder` | Controlador | Construir la URL del `Location` |
> | `ServiceResult` · `ServiceStatus` | `business.service` | Informar `OK`, `NOT_FOUND` o `CONFLICT` |
> | `spring.data.web.pageable.*` | `application.properties` | Tamaño de página por defecto y máximo |
>
> ```text
> JSON ─▶ …Request ─▶ Mapper ─▶ …Entity ─▶ Service ─▶ Repository ─▶ MySQL
> JSON ◀─ …Response ◀─ Mapper ◀─ …Entity ◀─ Service ◀─ Repository ◀─ MySQL
> ```

</details>

➡️ **Anterior:** [Guía del Módulo 05 — DTO y estructura de controladores](01-guia-dto-y-estructura-de-controladores.md) · **Código completo:** [03-academica-dtos-codigo-completo.md](03-academica-dtos-codigo-completo.md)
