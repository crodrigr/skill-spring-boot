# 🚨 Guía paso a paso — CRUD de estudiantes con manejo de excepciones

> 👩‍🏫 **Material docente.** Continúa la [guía del Módulo 06 — CRUD de estudiantes con Swagger](../modulo-04-documentacion-apis-swagger/01-guia-crud-estudiantes-con-swagger.md): el proyecto de partida **ya tiene** DTO, mapeador, `ServiceResult`, rutas con versión (`/api/v1`), un controlador delgado y la documentación con Swagger. Sobre él se reemplaza, **paso a paso**, el manejo de errores hecho a mano por **excepciones**, con las tres herramientas del [Módulo 07](../../07-manejo-de-excepciones/material/ejemplos/explicacion-conceptual.md): `@ResponseStatus`, `@ExceptionHandler` y `@ControllerAdvice`.
> Contiene código completo: no se entrega a los estudiantes antes de resolver el módulo.
>
> 📦 **Proyecto terminado:** [`students-api-excepciones/`](codigo/students-api-excepciones/) (código final, listo para ejecutar con `./mvnw spring-boot:run`).
>
> ✅ **Verificada.** El código se compiló para Java 21 y se ejecutó con Spring Boot 4.1.2, springdoc-openapi 3.2.0 y MySQL 8. Las respuestas HTTP, el contrato OpenAPI y el mensaje del registro (*log*) que se muestran son salidas reales de la aplicación.
>
> 🧭 **Idea central.** Un error de negocio («no existe», «ya existe») **no es un valor que se devuelve: es algo que ocurrió**. El servicio **lo lanza** como excepción, y **un solo lugar** decide qué código HTTP y qué cuerpo ve el cliente. El controlador deja de preguntar «¿salió bien?» y la capa de negocio deja de saber qué es HTTP.

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Pasar la API de estudiantes del Módulo 06 de «los errores son valores (`ServiceResult`) y salen como respuestas vacías» a «los errores son **excepciones** y salen como un **cuerpo JSON uniforme**».
>
> Al terminar, el estudiante:
>
> - Reconoce **cinco problemas** del manejo de errores hecho a mano (respuestas vacías, `400` indistinguibles, un `500` que no lo es…).
> - Crea **excepciones propias** de la capa de negocio y hace que el servicio **las lance** en vez de devolver `ServiceResult`.
> - Asocia una excepción a un código HTTP con **`@ResponseStatus`** y ve **qué no puede hacer** (no controla el cuerpo).
> - Personaliza el cuerpo con **`@ExceptionHandler`** en un controlador y ve **hasta dónde llega** (solo ese controlador).
> - Centraliza el manejo con **`@RestControllerAdvice`** y devuelve un `ErrorResponse` **igual para todos los errores**.
> - Devuelve `400` con el **detalle campo por campo** para la validación (`@Valid`) y para JSON roto, ids mal escritos y ordenamientos inválidos.
> - Evita que un `catch-all` convierta un `404` o un `405` de Spring en un `500` (**`ResponseEntityExceptionHandler`**).
> - Mantiene la capa de negocio **sin conocer HTTP**.
> - **Documenta** los errores en Swagger con un modelo reutilizable (`ErrorResponse`).
> - Elige el mecanismo apropiado según el escenario.

</details>

<details>
<summary><strong>📝 Introducción: qué cambia respecto al Módulo 06</strong></summary>

> <details>
> <summary><strong>🧩 Tres ideas que se confunden</strong></summary>
>
> | Idea | Qué es | Dónde vive en este proyecto |
> |---|---|---|
> | **Excepción** | Un objeto que **interrumpe** el flujo normal y sube por las capas hasta que alguien la captura | Clases de `business.exception` |
> | **Código HTTP** | El número que el **cliente** lee (`404`, `409`, `400`, `500`) | Lo decide el manejador, en `presentation` |
> | **Cuerpo de error** | El JSON que explica **qué pasó** | `ErrorResponse` (DTO) |
>
> ```text
> Repository ─▶ Service ──── throw StudentNotFoundException ───▶ Controller (no la captura)
>                                                                     │
>                                                                     ▼
>                                              @RestControllerAdvice  ─▶  404 + ErrorResponse (JSON)
> ```
>
> > 🧠 **Jerarquía.** `Throwable` → `Exception` → `RuntimeException`. Las excepciones propias de esta guía extienden **`RuntimeException`** (no obligan a declarar `throws` en cada método): son errores de **uso de la API**, no fallos del sistema. `Error` (por ejemplo, `OutOfMemoryError`) no se captura.
>
> </details>
>
> <details>
> <summary><strong>⚙️ Cómo se ejecuta: el recorrido de una excepción</strong></summary>
>
> Ejemplo: `GET /api/v1/students/999`, un estudiante que no existe.
>
> ```mermaid
> sequenceDiagram
>     participant C as Cliente
>     participant K as StudentController
>     participant S as StudentService
>     participant SP as Spring MVC
>     participant H as GlobalExceptionHandler
>     C->>K: GET /api/v1/students/999
>     K->>S: findById(999)
>     S--xK: throw StudentNotFoundException
>     K--xSP: la excepción sale del controlador
>     SP->>H: busca un @ExceptionHandler para ese tipo
>     H-->>SP: ResponseEntity(404, ErrorResponse)
>     SP-->>C: 404 + JSON
> ```
>
> 1. El servicio no encuentra el estudiante y **lanza** `StudentNotFoundException`. El método se interrumpe: el `return` nunca se ejecuta.
> 2. El controlador **no la captura** (no tiene `try/catch`), así que la excepción sale de él.
> 3. Spring MVC la recibe y **busca quién la maneje**, en este orden:
>
>    | Orden | Quién | Qué hace |
>    |:-:|---|---|
>    | 1 | `@ExceptionHandler` (en el controlador y, después, en un `@RestControllerAdvice`) | Construye la respuesta completa: código y cuerpo |
>    | 2 | `@ResponseStatus` en la clase de la excepción | Fija solo el código; el cuerpo es el de Spring |
>    | 3 | Comportamiento por defecto de Spring | Excepciones propias de Spring MVC → su código; el resto → `500` |
>
>    **Se usa el primero que sepa manejarla**; los demás ya no se consultan.
> 4. El método manejador **recibe la excepción como parámetro** y devuelve un `ResponseEntity`. Spring lo trata como el retorno de cualquier método de un controlador y lo escribe como JSON.
> 5. Como la excepción quedó manejada, el recorrido termina ahí: nadie más la ve.
>
> > 🔑 **Ningún método llama al manejador.** Lo invoca Spring cuando detecta la excepción, igual que invoca al controlador cuando llega una petición. Por eso el controlador queda solo con el camino feliz.
>
> Las tres herramientas de esta guía son tres lugares donde puede estar el paso 3:
>
> | Herramienta | Dónde está | Alcance |
> |---|---|---|
> | `@ResponseStatus` | Sobre la clase de la excepción | Solo el código HTTP |
> | `@ExceptionHandler` | En un método de **un** controlador | Ese controlador |
> | `@RestControllerAdvice` | En una clase aparte | **Todos** los controladores |
>
> </details>
>
> <details>
> <summary><strong>🎓 El proyecto</strong></summary>
>
> La API de estudiantes del Módulo 06, con las tres capas (`presentation`, `business`, `persistence`), nombres en inglés, MySQL y Swagger.
>
> | Operación | Ruta | Errores que debe distinguir |
> |---|---|---|
> | Listar con filtro y paginación | `GET /api/v1/students?name=&page=&size=&sort=` | `400` (orden o página inválidos) |
> | Buscar por id | `GET /api/v1/students/{id}` | `400` (id no numérico) · `404` |
> | Buscar por correo (obsoleta) | `GET /api/v1/students/by-email?email=` | `400` (falta el correo) · `404` |
> | Crear | `POST /api/v1/students` | `400` (datos o JSON inválidos) · `409` |
> | Reemplazar | `PUT /api/v1/students/{id}` | `400` · `404` · `409` |
> | Cambiar el estado | `PATCH /api/v1/students/{id}/status` | `400` · `404` |
> | Eliminar | `DELETE /api/v1/students/{id}` | `404` |
>
> </details>
>
> <details>
> <summary><strong>🔁 Antes y después</strong></summary>
>
> | Pieza | Módulo 06 (valores) | Esta guía (excepciones) |
> |---|---|---|
> | Servicio | Devuelve `ServiceResult<T>` o `Optional<T>` | Devuelve el dato y **lanza** si no puede |
> | `ServiceResult` / `ServiceStatus` | Existen | **Se eliminan** |
> | Controlador | `switch` sobre el estado y `ResponseEntity.notFound()` | Solo el camino feliz |
> | `404` y `409` | Sin cuerpo | `ErrorResponse` con mensaje |
> | `400` | Cuerpo genérico de Spring, igual para cualquier causa | `ErrorResponse` con la causa y, si aplica, el detalle por campo |
> | Rutas inexistentes, métodos no permitidos | Cuerpo genérico de Spring | `ErrorResponse` |
> | Swagger | `404`/`409`/`400` sin modelo (`content = @Content`) | Referencia a un modelo de error reutilizable |
>
> </details>
>
> <details>
> <summary><strong>📏 Situaciones de error y su respuesta</strong></summary>
>
> | Código | Situación | Excepción que la origina | HTTP |
> |---|---|---|:-:|
> | **RN-01** | No existe un estudiante con ese id o correo | `StudentNotFoundException` (propia) | `404` |
> | **RN-02** | El correo ya pertenece a otro estudiante | `StudentEmailAlreadyExistsException` (propia) | `409` |
> | **RN-03** | Un campo no cumple su validación | `MethodArgumentNotValidException` (Spring) | `400` + detalle |
> | **RN-04** | El cuerpo no es JSON o trae un valor que no existe (`"status":"XYZ"`) | `HttpMessageNotReadableException` (Spring) | `400` |
> | **RN-05** | Un parámetro tiene un tipo equivocado (`/students/abc`) | `MethodArgumentTypeMismatchException` (Spring) | `400` |
> | **RN-06** | Se ordena por una propiedad que no existe (`?sort=foo`) | `PropertyReferenceException` (Spring Data) | `400` |
> | **RN-07** | Falta un parámetro obligatorio | `MissingServletRequestParameterException` (Spring) | `400` |
> | **RN-08** | Ruta inexistente / método no permitido | `NoResourceFoundException` / `HttpRequestMethodNotSupportedException` (Spring) | `404` / `405` |
> | **RN-09** | Cualquier otro fallo (por ejemplo, se cae la base) | `Exception` | `500` |
>
> </details>
>
> <details>
> <summary><strong>🛠️ Requerimientos y restricciones</strong></summary>
>
> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Java 21 o superior, Spring Boot 4.1.2, Maven, MySQL 8 y springdoc 3.2.0 (los del Módulo 06). |
> | **RNF-02** | Las **excepciones propias** viven en `business.exception`, extienden `RuntimeException` y **no** importan nada de `org.springframework.web` ni `org.springframework.http`. |
> | **RNF-03** | El **manejador global** y el DTO `ErrorResponse` viven en `presentation` (`exception` y `dto`). |
> | **RNF-04** | **Todo** error de la API tiene el mismo cuerpo: `ErrorResponse`. |
> | **RNF-05** | Un error inesperado (`500`) **no revela** detalles al cliente y **sí** se registra en el *log* con su traza. |
> | **RNF-06** | Los controladores no tienen `try/catch` ni construyen respuestas de error. |
> | **RNF-07** | Los errores quedan documentados en Swagger. |
> | **RNF-08** | Nombres del código en **inglés**; mensajes para el cliente en **español**. |
>
> > 💡 **Alcance.** `@Valid` ya venía del Módulo 05: aquí **no se agrega** validación, solo se **maneja** la excepción que lanza. Quedan fuera: `ProblemDetail` (RFC 7807), internacionalización de mensajes y autenticación.
>
> </details>

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>

> | Paso | Minutos | Actividad |
> |---|---:|---|
> | 0 | 0 – 10 | Punto de partida: el proyecto del Módulo 06 |
> | 1 | 10 – 25 | **Ver los problemas** del manejo manual |
> | 2 | 25 – 50 | Excepciones propias y `@ResponseStatus` |
> | 3 | 50 – 70 | `@ExceptionHandler` y el cuerpo de error |
> | 4 | 70 – 100 | `@RestControllerAdvice`: el manejo global |
> | 5 | 100 – 120 | Las excepciones de Spring MVC |
> | 6 | 120 – 140 | Documentar los errores en Swagger |
> | 7 | 140 – 160 | Probar la API y un `500` real |
>
> > 💡 **Ritmo.** El Paso 0 es la base y puede entregarse ya hecho; el tiempo se dedica a los Pasos 1 a 7.

</details>

<details>
<summary><strong>🏗️ Arquitectura: el árbol de archivos (🆕 nuevo · ✏️ cambia · 🗑️ se elimina)</strong></summary>

> ```text
> 📁 students-api-excepciones
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 presentation
>     │   │   ├── 📁 config
>     │   │   │   └── ✏️ OpenApiConfig.java         respuestas de error reutilizables + esquema ErrorResponse
>     │   │   ├── 📁 controller
>     │   │   │   ├── ✏️ StudentController.java     solo el camino feliz · @ApiResponse con ref
>     │   │   │   └── ✏️ InternalController.java    seed con try/catch
>     │   │   ├── 📁 dto
>     │   │   │   ├── 🆕 ErrorResponse.java         el cuerpo de todo error
>     │   │   │   ├── ✏️ StudentRequest.java        mensajes de validación en español
>     │   │   │   ├── ✏️ StudentStatusRequest.java
>     │   │   │   ├── StudentResponse.java
>     │   │   │   └── PageResponse.java
>     │   │   ├── 📁 exception
>     │   │   │   └── 🆕 GlobalExceptionHandler.java  @RestControllerAdvice
>     │   │   └── 📁 mapper
>     │   │       └── StudentMapper.java
>     │   ├── 📁 business
>     │   │   ├── 📁 exception
>     │   │   │   ├── 🆕 StudentNotFoundException.java
>     │   │   │   └── 🆕 StudentEmailAlreadyExistsException.java
>     │   │   └── 📁 service
>     │   │       ├── ✏️ StudentService.java        lanza excepciones
>     │   │       ├── 🗑️ ServiceResult.java
>     │   │       └── 🗑️ ServiceStatus.java
>     │   └── 📁 persistence                        sin cambios
>     └── 📁 resources
>         └── 📄 application.properties             sin cambios
> ```
>
> | Capa | Qué aporta a las excepciones |
> |---|---|
> | `persistence` | **Nada**: sin cambios |
> | `business` | **Lanza** las excepciones de dominio (`business.exception`) |
> | `presentation` | **Traduce** cada excepción a HTTP (`GlobalExceptionHandler`) y define el cuerpo (`ErrorResponse`) |

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Punto de partida: el proyecto del Módulo 06</strong></summary>

> <details>
> <summary><strong>Paso 0.1 · MySQL y el proyecto</strong></summary>
>
> > Se parte del proyecto que se termina en la [guía del Módulo 06](../modulo-04-documentacion-apis-swagger/01-guia-crud-estudiantes-con-swagger.md) ([`students-api/`](../modulo-04-documentacion-apis-swagger/codigo/students-api/)). Se copia como `students-api-excepciones` y se levanta la base:
> >
> > ```bash
> > docker run -d --name academy-mysql \
> >   -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academy \
> >   -p 3306:3306 mysql:8.0
> > ```
> >
> > > | Lo que **ya existe** | Dónde |
> > > |---|---|
> > > | `StudentEntity`, `StudentStatus`, `StudentJpaRepository` | `persistence` |
> > > | `StudentService` (devuelve `Optional` y `ServiceResult`) | `business.service` |
> > > | `StudentController` e `InternalController` | `presentation.controller` |
> > > | DTO con `@Schema` y validaciones, `StudentMapper` | `presentation.dto` · `presentation.mapper` |
> > > | `OpenApiConfig` (Swagger, seguridad y grupos) | `presentation.config` |
>
> </details>
>
> <details>
> <summary><strong>Paso 0.2 · Comprobar que funciona</strong></summary>
>
> > ```bash
> > ./mvnw spring-boot:run
> > curl -X POST localhost:8080/internal/seed
> > ```
> >
> > ```json
> > {"created":3}
> > ```
> >
> > > ✅ **Punto de control.** Hay tres estudiantes (Ana, Luis y María) y Swagger UI abre en `http://localhost:8080/swagger-ui.html`.
>
> </details>

</details>

---

<details>
<summary><strong>🔎 Paso 1 — Ver los problemas del manejo manual</strong></summary>

> > 🗣️ **Para abrir el paso.** Antes de cambiar nada se **provocan los errores** y se mira, **con `curl -i`**, qué recibe el cliente. Es la mejor forma de que el grupo sienta el problema.
>
> <details>
> <summary><strong>Paso 1.1 · Provocar nueve errores</strong></summary>
>
> > ```bash
> > curl -i localhost:8080/api/v1/students/999
> > curl -i -X POST localhost:8080/api/v1/students -H 'Content-Type: application/json' \
> >   -d '{"name":"Otra Ana","age":23,"email":"ana@mail.com","status":"ACTIVE"}'
> > curl -i -X POST localhost:8080/api/v1/students -H 'Content-Type: application/json' \
> >   -d '{"name":"","age":5,"email":"x","status":"ACTIVE"}'
> > curl -i -X POST localhost:8080/api/v1/students -H 'Content-Type: application/json' -d '{"name":'
> > curl -i -X PATCH localhost:8080/api/v1/students/1/status -H 'Content-Type: application/json' \
> >   -d '{"status":"XYZ"}'
> > curl -i localhost:8080/api/v1/students/abc
> > curl -i 'localhost:8080/api/v1/students?sort=foo'
> > curl -i -X PUT localhost:8080/api/v1/students/999 -H 'Content-Type: application/json' \
> >   -d '{"name":"Nadie","age":30,"email":"nadie@mail.com","status":"ACTIVE"}'
> > ```
> >
> > **Resultado real:**
> >
> > | # | Petición | Respuesta |
> > |:-:|---|---|
> > | 1 | `GET /students/999` | `404` · `Content-Length: 0` (**sin cuerpo**) |
> > | 2 | `POST` con correo repetido | `409` · `Content-Length: 0` (**sin cuerpo**) |
> > | 3 | `POST` con nombre vacío, edad 5 y correo `x` | `400` · `{"timestamp":"…","status":400,"error":"Bad Request","path":"/api/v1/students"}` |
> > | 4 | `POST` con JSON cortado | `400` · **el mismo cuerpo** que el anterior |
> > | 5 | `PATCH` con `"status":"XYZ"` | `400` · **el mismo cuerpo** |
> > | 6 | `GET /students/abc` | `400` · **el mismo cuerpo** |
> > | 7 | `GET /students?sort=foo` | **`500`** · `{"timestamp":"…","status":500,"error":"Internal Server Error","path":"/api/v1/students"}` |
> > | 8 | `PUT /students/999` | `404` · **sin cuerpo** |
>
> </details>
>
> <details>
> <summary><strong>Paso 1.2 · Los cinco problemas</strong></summary>
>
> > | # | Problema | Evidencia |
> > |:-:|---|---|
> > | 1 | **Respuestas vacías.** El cliente sabe que falló, no **por qué**. | Filas 1, 2 y 8 |
> > | 2 | **Cuatro causas, una sola respuesta.** Validación, JSON roto, valor de enumerado inexistente e id mal escrito dan el mismo `400` sin explicación. | Filas 3 a 6 |
> > | 3 | **Un error del cliente que parece del servidor.** Ordenar por una propiedad inexistente es culpa de quien llama, pero responde `500`. | Fila 7 |
> > | 4 | **El controlador mezcla camino feliz y errores.** Cada método decide el código HTTP con `orElseGet(() -> notFound())` o con un `switch`. | Código de abajo |
> > | 5 | **El servicio devuelve códigos.** `ServiceResult` y `ServiceStatus` son una forma de «lanzar» un error a mano, y cada controlador nuevo tiene que repetir la traducción. | Código de abajo |
> >
> > <details>
> > <summary><strong>📄 El código que causa los problemas 4 y 5</strong> <em>(Módulo 06)</em></summary>
> >
> > ```java
> > // StudentService
> > @Transactional
> > public ServiceResult<StudentEntity> create(StudentEntity data) {
> >     if (students.existsByEmailIgnoreCase(data.getEmail())) {
> >         return ServiceResult.conflict();
> >     }
> >     ...
> > }
> >
> > // StudentController
> > ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
> > if (result.status() == ServiceStatus.CONFLICT) {
> >     return ResponseEntity.status(HttpStatus.CONFLICT).build();
> > }
> > ...
> > private ResponseEntity<StudentResponse> respond(ServiceResult<StudentEntity> result) {
> >     return switch (result.status()) {
> >         case OK -> ResponseEntity.ok(StudentMapper.toResponse(result.value()));
> >         case NOT_FOUND -> ResponseEntity.notFound().build();
> >         case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
> >     };
> > }
> > ```
> >
> > </details>
> >
> > > 🧠 **Para preguntar:** si mañana se agrega un `CourseController`, ¿qué hay que copiar? *(El `switch`, el `respond` y el `ServiceResult` de cada servicio.)*
>
> </details>

</details>

---

<details>
<summary><strong>🏷️ Paso 2 — Excepciones propias y <code>@ResponseStatus</code></strong></summary>

> > 🗣️ **Primer mecanismo.** `@ResponseStatus` se pone **sobre la clase de la excepción** y la asocia a un código HTTP. Es lo más simple que existe: ideal cuando una excepción siempre significa lo mismo.
>
> <details>
> <summary><strong>Paso 2.1 · Dos excepciones de la capa de negocio</strong></summary>
>
> > <details>
> > <summary><strong>📄 StudentNotFoundException.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/exception/StudentNotFoundException.java`
> >
> > ```java
> > package com.academy.business.exception;
> >
> > import org.springframework.http.HttpStatus;
> > import org.springframework.web.bind.annotation.ResponseStatus;
> >
> > @ResponseStatus(HttpStatus.NOT_FOUND)
> > public class StudentNotFoundException extends RuntimeException {
> >
> >     public StudentNotFoundException(String message) {
> >         super(message);
> >     }
> >
> >     public static StudentNotFoundException byId(Long id) {
> >         return new StudentNotFoundException("No existe un estudiante con id " + id);
> >     }
> >
> >     public static StudentNotFoundException byEmail(String email) {
> >         return new StudentNotFoundException("No existe un estudiante con el correo " + email);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 StudentEmailAlreadyExistsException.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/exception/StudentEmailAlreadyExistsException.java`
> >
> > ```java
> > package com.academy.business.exception;
> >
> > import org.springframework.http.HttpStatus;
> > import org.springframework.web.bind.annotation.ResponseStatus;
> >
> > @ResponseStatus(HttpStatus.CONFLICT)
> > public class StudentEmailAlreadyExistsException extends RuntimeException {
> >
> >     public StudentEmailAlreadyExistsException(String email) {
> >         super("El correo " + email + " ya está registrado");
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > 💡 **Por qué `RuntimeException`.** No obliga a escribir `throws` en cada método del servicio ni del controlador; la excepción **sube sola** hasta quien la maneje.
>
> </details>
>
> <details>
> <summary><strong>Paso 2.2 · El servicio lanza en vez de devolver</strong></summary>
>
> > Se **eliminan** `ServiceResult` y `ServiceStatus`, y `StudentService` pasa a devolver el dato **o lanzar**:
> >
> > <details>
> > <summary><strong>📄 StudentService.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/StudentService.java`
> >
> > ```java
> > package com.academy.business.service;
> >
> > import com.academy.business.exception.StudentEmailAlreadyExistsException;
> > import com.academy.business.exception.StudentNotFoundException;
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.persistence.entity.StudentStatus;
> > import com.academy.persistence.repository.StudentJpaRepository;
> > import org.springframework.data.domain.Page;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
> >
> > @Service
> > public class StudentService {
> >
> >     private final StudentJpaRepository students;
> >
> >     public StudentService(StudentJpaRepository students) {
> >         this.students = students;
> >     }
> >
> >     @Transactional(readOnly = true)
> >     public Page<StudentEntity> search(String name, Pageable pageable) {
> >         if (name == null || name.isBlank()) {
> >             return students.findAll(pageable);
> >         }
> >         return students.findByNameContainingIgnoreCase(name, pageable);
> >     }
> >
> >     @Transactional(readOnly = true)
> >     public StudentEntity findById(Long id) {
> >         return students.findById(id)
> >                 .orElseThrow(() -> StudentNotFoundException.byId(id));
> >     }
> >
> >     @Transactional(readOnly = true)
> >     public StudentEntity findByEmail(String email) {
> >         return students.findByEmailIgnoreCase(email)
> >                 .orElseThrow(() -> StudentNotFoundException.byEmail(email));
> >     }
> >
> >     @Transactional
> >     public StudentEntity create(StudentEntity data) {
> >         if (students.existsByEmailIgnoreCase(data.getEmail())) {
> >             throw new StudentEmailAlreadyExistsException(data.getEmail());
> >         }
> >         return students.save(new StudentEntity(
> >                 null, data.getName(), data.getAge(), data.getEmail(), data.getStatus()));
> >     }
> >
> >     @Transactional
> >     public StudentEntity update(Long id, StudentEntity data) {
> >         if (!students.existsById(id)) {
> >             throw StudentNotFoundException.byId(id);
> >         }
> >         if (students.existsByEmailIgnoreCaseAndIdNot(data.getEmail(), id)) {
> >             throw new StudentEmailAlreadyExistsException(data.getEmail());
> >         }
> >         return students.save(new StudentEntity(
> >                 id, data.getName(), data.getAge(), data.getEmail(), data.getStatus()));
> >     }
> >
> >     @Transactional
> >     public StudentEntity changeStatus(Long id, StudentStatus status) {
> >         StudentEntity student = findById(id);
> >         return students.save(new StudentEntity(
> >                 id, student.getName(), student.getAge(), student.getEmail(), status));
> >     }
> >
> >     @Transactional
> >     public void delete(Long id) {
> >         if (!students.existsById(id)) {
> >             throw StudentNotFoundException.byId(id);
> >         }
> >         students.deleteById(id);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > | Antes | Ahora |
> > |---|---|
> > | `Optional<StudentEntity> findById(...)` | `StudentEntity findById(...)` — lanza si no existe |
> > | `ServiceResult<StudentEntity> create(...)` | `StudentEntity create(...)` — lanza si el correo existe |
> > | `boolean delete(...)` | `void delete(...)` — lanza si no existe |
> > | `changeStatus` repetía el `NOT_FOUND` | reutiliza `findById` |
> >
> > > ⚠️ **Los `@Transactional` y las excepciones.** Una `RuntimeException` que sale de un método `@Transactional` hace **rollback**. Aquí es lo correcto: las excepciones se lanzan **antes** de `save`, así que no hay nada que deshacer.
>
> </details>
>
> <details>
> <summary><strong>Paso 2.3 · El controlador queda con el camino feliz</strong></summary>
>
> > <details>
> > <summary><strong>📄 StudentController.java</strong> <em>(solo el cambio)</em></summary>
> >
> > Se quitan `ServiceResult`, `ServiceStatus`, los `HttpStatus` y el método `respond`. Los métodos quedan así:
> >
> > ```java
> >     @GetMapping("/{id}")
> >     public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
> >         return ResponseEntity.ok(StudentMapper.toResponse(service.findById(id)));
> >     }
> >
> >     @GetMapping("/by-email")
> >     public ResponseEntity<StudentResponse> findByEmail(@RequestParam String email) {
> >         return ResponseEntity.ok(StudentMapper.toResponse(service.findByEmail(email)));
> >     }
> >
> >     @PostMapping
> >     public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >         StudentResponse body = StudentMapper.toResponse(service.create(StudentMapper.toEntity(request)));
> >         URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                 .path("/{id}").buildAndExpand(body.id()).toUri();
> >         return ResponseEntity.created(location).body(body);
> >     }
> >
> >     @PutMapping("/{id}")
> >     public ResponseEntity<StudentResponse> update(@PathVariable Long id,
> >                                                   @Valid @RequestBody StudentRequest request) {
> >         return ResponseEntity.ok(StudentMapper.toResponse(service.update(id, StudentMapper.toEntity(request))));
> >     }
> >
> >     @PatchMapping("/{id}/status")
> >     public ResponseEntity<StudentResponse> changeStatus(@PathVariable Long id,
> >                                                         @Valid @RequestBody StudentStatusRequest request) {
> >         return ResponseEntity.ok(StudentMapper.toResponse(service.changeStatus(id, request.status())));
> >     }
> >
> >     @DeleteMapping("/{id}")
> >     public ResponseEntity<Void> delete(@PathVariable Long id) {
> >         service.delete(id);
> >         return ResponseEntity.noContent().build();
> >     }
> > ```
> >
> > (Las anotaciones de Swagger de cada método se conservan; aquí se omiten para ver el cambio.)
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 InternalController.java</strong> <em>(solo el cambio)</em></summary>
> >
> > El `seed` ya no puede preguntar `status() == OK`. Aquí **sí** hay un `try/catch` legítimo: lanzar la excepción es la forma en que el servicio avisa que el estudiante ya existe, y el `seed` decide **ignorarlo**.
> >
> > ```java
> >         for (StudentEntity sample : samples) {
> >             try {
> >                 service.create(sample);
> >                 created++;
> >             } catch (StudentEmailAlreadyExistsException ignored) {
> >                 // ya existe: no se vuelve a crear
> >             }
> >         }
> > ```
> >
> > Se cambia el `import` de `ServiceStatus` por `com.academy.business.exception.StudentEmailAlreadyExistsException`.
> >
> > </details>
>
> </details>
>
> <details>
> <summary><strong>Paso 2.4 · Ver el resultado</strong></summary>
>
> > ```bash
> > curl -i localhost:8080/api/v1/students/999
> > ```
> >
> > ```text
> > HTTP/1.1 404
> > Content-Type: application/json
> >
> > {"timestamp":"2026-11-01T23:55:43.737Z","status":404,"error":"Not Found","path":"/api/v1/students/999"}
> > ```
> >
> > ```bash
> > curl -i -X POST localhost:8080/api/v1/students -H 'Content-Type: application/json' \
> >   -d '{"name":"Otra Ana","age":23,"email":"ana@mail.com","status":"ACTIVE"}'
> > ```
> >
> > ```text
> > HTTP/1.1 409
> > Content-Type: application/json
> >
> > {"timestamp":"2026-11-01T23:55:43.862Z","status":409,"error":"Conflict","path":"/api/v1/students"}
> > ```
> >
> > Y `POST /internal/seed` repetido devuelve `{"created":0}`: no falla aunque los tres ya existen.
> >
> > | Se logró | Falta |
> > |---|---|
> > | `404` y `409` correctos, sin `switch` en el controlador | El cuerpo **no dice por qué**: el `message` de la excepción **no sale**. |
> > | El servicio dice qué pasó (`throw`) | `@ResponseStatus` **no permite personalizar el cuerpo**. |
> >
> > > 🧠 **¿Y `server.error.include-message=always`?** Haría salir el mensaje, pero **para todas** las excepciones, incluidas las inesperadas, y eso puede **revelar detalles internos**. Por eso conviene controlar el cuerpo **a propósito**.
> >
> > > ⚠️ **Dependencia al revés.** `StudentNotFoundException` está en `business` pero importa `org.springframework.web`. La capa de negocio **conoce HTTP**. Se corrige en el Paso 4.
>
> </details>

</details>

---

<details>
<summary><strong>🎛️ Paso 3 — <code>@ExceptionHandler</code>: controlar el cuerpo</strong></summary>

> > 🗣️ **Segundo mecanismo.** `@ExceptionHandler` es un **método dentro del controlador** que atrapa una excepción y construye la respuesta completa: código, mensaje y cuerpo.
>
> <details>
> <summary><strong>Paso 3.1 · El cuerpo de error: <code>ErrorResponse</code></strong></summary>
>
> > <details>
> > <summary><strong>📄 ErrorResponse.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/ErrorResponse.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import com.fasterxml.jackson.annotation.JsonInclude;
> > import io.swagger.v3.oas.annotations.media.Schema;
> > import org.springframework.http.HttpStatus;
> >
> > import java.time.Instant;
> > import java.time.temporal.ChronoUnit;
> > import java.util.List;
> >
> > @Schema(description = "Cuerpo de toda respuesta de error de la API")
> > public record ErrorResponse(
> >
> >         @Schema(description = "Momento en que ocurrió el error, en UTC (ISO-8601)", type = "string", example = "2026-11-01T10:15:30.123Z")
> >         Instant timestamp,
> >
> >         @Schema(description = "Código de estado HTTP", example = "404")
> >         int status,
> >
> >         @Schema(description = "Nombre del estado HTTP", example = "Not Found")
> >         String error,
> >
> >         @Schema(description = "Qué pasó, en lenguaje claro", example = "No existe un estudiante con id 999")
> >         String message,
> >
> >         @Schema(description = "Ruta de la petición que falló", example = "/api/v1/students/999")
> >         String path,
> >
> >         @Schema(description = "Detalle campo por campo (solo en errores de validación)")
> >         @JsonInclude(JsonInclude.Include.NON_EMPTY)
> >         List<FieldIssue> details
> > ) {
> >
> >     @Schema(description = "Un campo inválido y la razón")
> >     public record FieldIssue(
> >
> >             @Schema(description = "Nombre del campo", example = "email")
> >             String field,
> >
> >             @Schema(description = "Por qué es inválido", example = "debe ser una dirección de correo electrónico bien formada")
> >             String message
> >     ) {
> >     }
> >
> >     public static ErrorResponse of(HttpStatus status, String message, String path) {
> >         return new ErrorResponse(Instant.now().truncatedTo(ChronoUnit.MILLIS), status.value(),
> >                 status.getReasonPhrase(), message, path, List.of());
> >     }
> >
> >     public static ErrorResponse of(HttpStatus status, String message, String path, List<FieldIssue> details) {
> >         return new ErrorResponse(Instant.now().truncatedTo(ChronoUnit.MILLIS), status.value(),
> >                 status.getReasonPhrase(), message, path, details);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > | Campo | Para qué |
> > |---|---|
> > | `status`, `error` | El código y su nombre, para quien solo lee el JSON |
> > | `message` | **Qué pasó**, en español claro |
> > | `path` | Qué petición falló (útil en un *log* o en un reporte) |
> > | `details` | Lista de `{field, message}`; **desaparece** del JSON cuando está vacía (`@JsonInclude`) |
> >
> > > 💡 No lleva la traza ni el nombre de la clase de la excepción: el cliente no debe verlos.
>
> </details>
>
> <details>
> <summary><strong>Paso 3.2 · Un manejador en el controlador</strong></summary>
>
> > <details>
> > <summary><strong>📄 StudentController.java (agregar al final de la clase)</strong> <em>(solo el cambio)</em></summary>
> >
> > ```java
> >     @ExceptionHandler(StudentEmailAlreadyExistsException.class)
> >     public ResponseEntity<ErrorResponse> handleDuplicateEmail(StudentEmailAlreadyExistsException ex,
> >                                                               HttpServletRequest request) {
> >         ErrorResponse body = ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
> >         return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
> >     }
> > ```
> >
> > Imports nuevos: `StudentEmailAlreadyExistsException`, `ErrorResponse`, `HttpServletRequest`, `HttpStatus` y `ExceptionHandler`.
> >
> > </details>
>
> </details>
>
> <details>
> <summary><strong>Paso 3.3 · Ver el resultado</strong></summary>
>
> > ```bash
> > curl -i -X POST localhost:8080/api/v1/students -H 'Content-Type: application/json' \
> >   -d '{"name":"Otra Ana","age":23,"email":"ana@mail.com","status":"ACTIVE"}'
> > ```
> >
> > ```text
> > HTTP/1.1 409
> > Content-Type: application/json
> >
> > {"timestamp":"2026-11-01T23:56:30.132778218Z","status":409,"error":"Conflict","message":"El correo ana@mail.com ya está registrado","path":"/api/v1/students"}
> > ```
> >
> > El `PUT` con el correo de otro estudiante también responde con el mismo cuerpo. Y el `404`:
> >
> > ```text
> > $ curl -i localhost:8080/api/v1/students/999
> > HTTP/1.1 404
> >
> > {"timestamp":"2026-11-01T23:56:30.364Z","status":404,"error":"Not Found","path":"/api/v1/students/999"}
> > ```
> >
> > **Sigue con el cuerpo genérico de Spring**: no tiene `message` y su formato es otro.
> >
> > > 🧠 **Precedencia.** `StudentEmailAlreadyExistsException` **todavía** tiene `@ResponseStatus(CONFLICT)`. Cuando una excepción tiene las dos cosas, **gana `@ExceptionHandler`** (es más específico).
>
> </details>
>
> <details>
> <summary><strong>Paso 3.4 · Hasta dónde llega</strong></summary>
>
> > | Pregunta | Respuesta |
> > |---|---|
> > | ¿Cubre el `404`? | **No.** Habría que agregar **otro** método igual en el controlador. |
> > | ¿Cubre a `InternalController`, o a un `CourseController` futuro? | **No.** Un `@ExceptionHandler` solo vale para el controlador donde está. |
> > | ¿Y si hay cinco controladores y seis excepciones? | Treinta métodos casi idénticos. |
> >
> > > ✅ **Conclusión.** `@ExceptionHandler` sirve para casos **específicos de un controlador**. Para algo que vale para **toda** la aplicación hace falta un mecanismo global.
>
> </details>

</details>

---

<details>
<summary><strong>🌐 Paso 4 — <code>@RestControllerAdvice</code>: el manejo global</strong></summary>

> > 🗣️ **Tercer mecanismo.** `@ControllerAdvice` (en una API REST, **`@RestControllerAdvice`**, que además escribe el cuerpo como JSON) es **una clase** con métodos `@ExceptionHandler` que valen para **todos** los controladores.
>
> <details>
> <summary><strong>Paso 4.1 · Mover el manejo a un solo lugar</strong></summary>
>
> > Se **borra** el `@ExceptionHandler` del `StudentController` (y sus imports) y se crea la clase global. Esta primera versión maneja las dos excepciones propias, la validación, el JSON roto, el tipo equivocado, el orden inválido y **cualquier otra** (`500`).
> >
> > <details>
> > <summary><strong>📄 GlobalExceptionHandler.java (versión del Paso 4)</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/exception/GlobalExceptionHandler.java`
> >
> > ```java
> > package com.academy.presentation.exception;
> >
> > import com.academy.business.exception.StudentEmailAlreadyExistsException;
> > import com.academy.business.exception.StudentNotFoundException;
> > import com.academy.presentation.dto.ErrorResponse;
> > import com.academy.presentation.dto.ErrorResponse.FieldIssue;
> > import jakarta.servlet.http.HttpServletRequest;
> > import org.slf4j.Logger;
> > import org.slf4j.LoggerFactory;
> > import org.springframework.data.core.PropertyReferenceException;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.http.ResponseEntity;
> > import org.springframework.http.converter.HttpMessageNotReadableException;
> > import org.springframework.web.bind.MethodArgumentNotValidException;
> > import org.springframework.web.bind.annotation.ExceptionHandler;
> > import org.springframework.web.bind.annotation.RestControllerAdvice;
> > import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
> >
> > import java.util.List;
> >
> > @RestControllerAdvice
> > public class GlobalExceptionHandler {
> >
> >     private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
> >
> >     @ExceptionHandler(StudentNotFoundException.class)
> >     public ResponseEntity<ErrorResponse> handleNotFound(StudentNotFoundException ex, HttpServletRequest request) {
> >         return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
> >     }
> >
> >     @ExceptionHandler(StudentEmailAlreadyExistsException.class)
> >     public ResponseEntity<ErrorResponse> handleDuplicateEmail(StudentEmailAlreadyExistsException ex,
> >                                                               HttpServletRequest request) {
> >         return build(HttpStatus.CONFLICT, ex.getMessage(), request);
> >     }
> >
> >     @ExceptionHandler(MethodArgumentNotValidException.class)
> >     public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
> >                                                           HttpServletRequest request) {
> >         List<FieldIssue> details = ex.getBindingResult().getFieldErrors().stream()
> >                 .map(error -> new FieldIssue(error.getField(), error.getDefaultMessage()))
> >                 .toList();
> >         ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST,
> >                 "Los datos enviados no son válidos", request.getRequestURI(), details);
> >         return ResponseEntity.badRequest().body(body);
> >     }
> >
> >     @ExceptionHandler(HttpMessageNotReadableException.class)
> >     public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex,
> >                                                               HttpServletRequest request) {
> >         return build(HttpStatus.BAD_REQUEST,
> >                 "El cuerpo de la petición no es un JSON válido o tiene un valor no permitido", request);
> >     }
> >
> >     @ExceptionHandler(MethodArgumentTypeMismatchException.class)
> >     public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
> >                                                             HttpServletRequest request) {
> >         return build(HttpStatus.BAD_REQUEST,
> >                 "El parámetro '" + ex.getName() + "' tiene un valor no válido: " + ex.getValue(), request);
> >     }
> >
> >     @ExceptionHandler(PropertyReferenceException.class)
> >     public ResponseEntity<ErrorResponse> handleUnknownSortProperty(PropertyReferenceException ex,
> >                                                                    HttpServletRequest request) {
> >         return build(HttpStatus.BAD_REQUEST,
> >                 "No se puede ordenar por '" + ex.getPropertyName() + "'", request);
> >     }
> >
> >     @ExceptionHandler(Exception.class)
> >     public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
> >         log.error("Error inesperado en {}", request.getRequestURI(), ex);
> >         return build(HttpStatus.INTERNAL_SERVER_ERROR,
> >                 "Ocurrió un error inesperado. Intentar de nuevo más tarde.", request);
> >     }
> >
> >     private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
> >         return ResponseEntity.status(status).body(ErrorResponse.of(status, message, request.getRequestURI()));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > | Manejador | Excepción | Código |
> > |---|---|:-:|
> > | `handleNotFound` | `StudentNotFoundException` | `404` |
> > | `handleDuplicateEmail` | `StudentEmailAlreadyExistsException` | `409` |
> > | `handleValidation` | `MethodArgumentNotValidException` (`@Valid`) | `400` + `details` |
> > | `handleUnreadableBody` | `HttpMessageNotReadableException` | `400` |
> > | `handleTypeMismatch` | `MethodArgumentTypeMismatchException` | `400` |
> > | `handleUnknownSortProperty` | `PropertyReferenceException` | `400` |
> > | `handleUnexpected` | `Exception` (todo lo demás) | `500` + *log* |
> >
> > > ⚠️ **Cambio de Spring Boot 4.** `PropertyReferenceException` ahora está en `org.springframework.data.core` (con Spring Boot 3 estaba en `org.springframework.data.mapping`). Con el `import` viejo, el proyecto **no compila**.
> >
> > > 🔎 **Cuál se elige.** Si una excepción encaja con varios manejadores, Spring usa **el más específico** (el de la clase más cercana). Por eso `Exception.class` solo atrapa lo que **nadie más** manejó.
> >
> > > ⚠️ **El `500` no cuenta nada.** Al cliente le llega un mensaje genérico; la **traza completa** va al *log* con `log.error(..., ex)` (RNF-05).
>
> </details>
>
> <details>
> <summary><strong>Paso 4.2 · La capa de negocio no debe conocer HTTP</strong></summary>
>
> > Ahora que **un solo lugar** traduce excepciones a códigos, el `@ResponseStatus` de las dos excepciones sobra y además ensucia la capa de negocio. Se quita (y con él los dos `import`):
> >
> > <details>
> > <summary><strong>📄 StudentNotFoundException.java y StudentEmailAlreadyExistsException.java</strong> <em>(versión final)</em></summary>
> >
> > ```java
> > package com.academy.business.exception;
> >
> > public class StudentNotFoundException extends RuntimeException {
> >
> >     public StudentNotFoundException(String message) {
> >         super(message);
> >     }
> >
> >     public static StudentNotFoundException byId(Long id) {
> >         return new StudentNotFoundException("No existe un estudiante con id " + id);
> >     }
> >
> >     public static StudentNotFoundException byEmail(String email) {
> >         return new StudentNotFoundException("No existe un estudiante con el correo " + email);
> >     }
> > }
> > ```
> >
> > ```java
> > package com.academy.business.exception;
> >
> > public class StudentEmailAlreadyExistsException extends RuntimeException {
> >
> >     public StudentEmailAlreadyExistsException(String email) {
> >         super("El correo " + email + " ya está registrado");
> >     }
> > }
> > ```
> >
> > </details>
> >
> > | | `@ResponseStatus` en la excepción | Manejador global |
> > |---|---|---|
> > | ¿Quién decide el código? | La propia excepción (capa de negocio) | `presentation` |
> > | ¿`business` importa `org.springframework.web`? | **Sí** | **No** (RNF-02) |
> > | ¿Se puede cambiar el cuerpo? | No | Sí |
> >
> > > 🧠 **Regla.** La capa de negocio dice **qué pasó**; la de presentación decide **cómo se cuenta por HTTP**. Si mañana el negocio se usa desde un proceso por lotes, no arrastra Spring Web.
>
> </details>
>
> <details>
> <summary><strong>Paso 4.3 · Mensajes de validación en español</strong></summary>
>
> > `@Valid` ya estaba desde el Módulo 05, pero sus mensajes por defecto salen en el idioma de la máquina. Para que sean **siempre los mismos** se escriben en las anotaciones:
> >
> > <details>
> > <summary><strong>📄 StudentRequest.java</strong> <em>(solo el cambio)</em></summary>
> >
> > ```java
> >         @NotBlank(message = "es obligatorio")
> >         @Size(min = 2, max = 100, message = "debe tener entre 2 y 100 caracteres")
> >         String name,
> >
> >         @Min(value = 16, message = "debe ser 16 o más")
> >         @Max(value = 99, message = "debe ser 99 o menos")
> >         int age,
> >
> >         @NotBlank(message = "es obligatorio")
> >         @Email(message = "no tiene formato de correo electrónico")
> >         String email,
> >
> >         @NotNull(message = "es obligatorio")
> >         StudentStatus status
> > ```
> >
> > En `StudentStatusRequest`: `@NotNull(message = "es obligatorio")`.
> >
> > </details>
>
> </details>
>
> <details>
> <summary><strong>Paso 4.4 · Ver el resultado</strong></summary>
>
> > ```bash
> > curl -i -X POST localhost:8080/api/v1/students -H 'Content-Type: application/json' \
> >   -d '{"name":"","age":5,"email":"x","status":"ACTIVE"}'
> > ```
> >
> > ```text
> > HTTP/1.1 400
> > Content-Type: application/json
> >
> > {"timestamp":"2026-11-01T23:57:10.657Z","status":400,"error":"Bad Request","message":"Los datos enviados no son válidos","path":"/api/v1/students","details":[{"field":"email","message":"no tiene formato de correo electrónico"},{"field":"name","message":"debe tener entre 2 y 100 caracteres"},{"field":"age","message":"debe ser 16 o más"},{"field":"name","message":"es obligatorio"}]}
> > ```
> >
> > Los **cuatro `400` que antes eran iguales** ahora se distinguen:
> >
> > | Petición | `message` |
> > |---|---|
> > | `POST` con datos inválidos | `Los datos enviados no son válidos` + `details` campo por campo |
> > | `POST` con `{"name":` | `El cuerpo de la petición no es un JSON válido o tiene un valor no permitido` |
> > | `PATCH` con `"status":"XYZ"` | el mismo mensaje del JSON (el valor no existe en el enumerado) |
> > | `PATCH` con `{}` | `Los datos enviados no son válidos` + `{"field":"status","message":"es obligatorio"}` |
> > | `GET /students/abc` | `El parámetro 'id' tiene un valor no válido: abc` |
> > | `GET /students?sort=foo` | `No se puede ordenar por 'foo'` — **ya es `400`, no `500`** |
> >
> > > 🔎 **Detalle.** El orden de `details` no está garantizado en esta versión (las validaciones se evalúan en cualquier orden). El Paso 5 lo ordena.
>
> </details>
>
> <details>
> <summary><strong>Paso 4.5 · Lo que se escapa: un <code>catch-all</code> que se traga demasiado</strong></summary>
>
> > ```bash
> > curl -i localhost:8080/api/v1/nope
> > curl -i -X DELETE localhost:8080/api/v1/students
> > curl -i localhost:8080/api/v1/students/by-email
> > ```
> >
> > **Resultado real, las tres:**
> >
> > ```text
> > HTTP/1.1 500
> > {"timestamp":"2026-11-01T23:57:10.774Z","status":500,"error":"Internal Server Error","message":"Ocurrió un error inesperado. Intentar de nuevo más tarde.","path":"/api/v1/nope"}
> > ```
> >
> > | Petición | Debería ser | Es |
> > |---|:-:|:-:|
> > | `GET /api/v1/nope` (ruta que no existe) | `404` | **`500`** |
> > | `DELETE /api/v1/students` (método no permitido) | `405` | **`500`** |
> > | `GET /students/by-email` (falta `email`) | `400` | **`500`** |
> >
> > > ⚠️ **La trampa.** Spring MVC lanza excepciones propias para estos casos (`NoResourceFoundException`, `HttpRequestMethodNotSupportedException`, `MissingServletRequestParameterException`). Antes de este paso Spring las convertía en el código correcto; ahora **`@ExceptionHandler(Exception.class)` las atrapa primero** y las vuelve `500`. Además llenan el *log* de errores falsos.
>
> </details>

</details>

---

<details>
<summary><strong>🧰 Paso 5 — Las excepciones de Spring MVC: <code>ResponseEntityExceptionHandler</code></strong></summary>

> > 🗣️ **La solución de Spring.** `ResponseEntityExceptionHandler` es una clase base que **ya sabe** el código correcto de unas quince excepciones de Spring MVC. Si el manejador global **la extiende**, esas excepciones dejan de caer en el `catch-all`, y solo hace falta **darles el cuerpo propio**.
>
> <details>
> <summary><strong>Paso 5.1 · Extender la clase base</strong></summary>
>
> > Tres cambios sobre la versión del Paso 4:
> >
> > | Cambio | Por qué |
> > |---|---|
> > | `extends ResponseEntityExceptionHandler` | La clase base ya trae un `@ExceptionHandler` para las excepciones de Spring MVC |
> > | `handleValidation`, `handleUnreadableBody` y `handleTypeMismatch` pasan de `@ExceptionHandler` a **`@Override`** | Declarar un `@ExceptionHandler` para una excepción que la clase base ya maneja produce el error **`Ambiguous @ExceptionHandler method`** |
> > | Se agrega `handleExceptionInternal` | **Un único punto** por el que pasan todas las demás excepciones de Spring MVC (`404` de ruta, `405`, `415`…) |
> >
> > También se agrega `handleMissingServletRequestParameter` y se **ordenan** los `details`.
> >
> > <details>
> > <summary><strong>📄 GlobalExceptionHandler.java (versión final)</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/exception/GlobalExceptionHandler.java`
> >
> > ```java
> > package com.academy.presentation.exception;
> >
> > import com.academy.business.exception.StudentEmailAlreadyExistsException;
> > import com.academy.business.exception.StudentNotFoundException;
> > import com.academy.presentation.dto.ErrorResponse;
> > import com.academy.presentation.dto.ErrorResponse.FieldIssue;
> > import org.slf4j.Logger;
> > import org.slf4j.LoggerFactory;
> > import org.springframework.beans.TypeMismatchException;
> > import org.springframework.data.core.PropertyReferenceException;
> > import org.springframework.http.HttpHeaders;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.http.HttpStatusCode;
> > import org.springframework.http.ResponseEntity;
> > import org.springframework.http.converter.HttpMessageNotReadableException;
> > import org.springframework.web.bind.MethodArgumentNotValidException;
> > import org.springframework.web.bind.MissingServletRequestParameterException;
> > import org.springframework.web.bind.annotation.ExceptionHandler;
> > import org.springframework.web.bind.annotation.RestControllerAdvice;
> > import org.springframework.web.context.request.ServletWebRequest;
> > import org.springframework.web.context.request.WebRequest;
> > import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
> >
> > import java.util.Comparator;
> > import java.util.List;
> >
> > @RestControllerAdvice
> > public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
> >
> >     private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
> >
> >     // ---------- Excepciones de la aplicación ----------
> >
> >     @ExceptionHandler(StudentNotFoundException.class)
> >     public ResponseEntity<Object> handleNotFound(StudentNotFoundException ex, WebRequest request) {
> >         return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
> >     }
> >
> >     @ExceptionHandler(StudentEmailAlreadyExistsException.class)
> >     public ResponseEntity<Object> handleDuplicateEmail(StudentEmailAlreadyExistsException ex, WebRequest request) {
> >         return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
> >     }
> >
> >     @ExceptionHandler(PropertyReferenceException.class)
> >     public ResponseEntity<Object> handleUnknownSortProperty(PropertyReferenceException ex, WebRequest request) {
> >         return build(HttpStatus.BAD_REQUEST, "No se puede ordenar por '" + ex.getPropertyName() + "'", request, null);
> >     }
> >
> >     @ExceptionHandler(Exception.class)
> >     public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
> >         log.error("Error inesperado en {}", path(request), ex);
> >         return build(HttpStatus.INTERNAL_SERVER_ERROR,
> >                 "Ocurrió un error inesperado. Intentar de nuevo más tarde.", request, null);
> >     }
> >
> >     // ---------- Excepciones de Spring MVC (las maneja la clase base; aquí se les da el cuerpo propio) ----------
> >
> >     @Override
> >     protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
> >                                                                   HttpHeaders headers, HttpStatusCode status,
> >                                                                   WebRequest request) {
> >         List<FieldIssue> details = ex.getBindingResult().getFieldErrors().stream()
> >                 .map(error -> new FieldIssue(error.getField(), error.getDefaultMessage()))
> >                 .sorted(Comparator.comparing(FieldIssue::field).thenComparing(FieldIssue::message))
> >                 .toList();
> >         return build(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos", request, details);
> >     }
> >
> >     @Override
> >     protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
> >                                                                   HttpHeaders headers, HttpStatusCode status,
> >                                                                   WebRequest request) {
> >         return build(HttpStatus.BAD_REQUEST,
> >                 "El cuerpo de la petición no es un JSON válido o tiene un valor no permitido", request, null);
> >     }
> >
> >     @Override
> >     protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
> >                                                         HttpStatusCode status, WebRequest request) {
> >         return build(HttpStatus.BAD_REQUEST,
> >                 "El parámetro '" + ex.getPropertyName() + "' tiene un valor no válido: " + ex.getValue(),
> >                 request, null);
> >     }
> >
> >     @Override
> >     protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
> >                                                                           HttpHeaders headers, HttpStatusCode status,
> >                                                                           WebRequest request) {
> >         return build(HttpStatus.BAD_REQUEST,
> >                 "Falta el parámetro obligatorio '" + ex.getParameterName() + "'", request, null);
> >     }
> >
> >     // Todas las demás excepciones de Spring MVC (ruta inexistente, método no permitido...) pasan por aquí.
> >     @Override
> >     protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
> >                                                              HttpStatusCode statusCode, WebRequest request) {
> >         HttpStatus status = HttpStatus.valueOf(statusCode.value());
> >         String message = switch (status) {
> >             case NOT_FOUND -> "No existe el recurso solicitado";
> >             case METHOD_NOT_ALLOWED -> "Ese método HTTP no está permitido en esta ruta";
> >             case UNSUPPORTED_MEDIA_TYPE -> "El tipo de contenido enviado no está soportado";
> >             case NOT_ACCEPTABLE -> "No se puede responder en el formato pedido";
> >             default -> status.getReasonPhrase();
> >         };
> >         ResponseEntity<Object> response = build(status, message, request, null);
> >         return ResponseEntity.status(status).headers(headers).body(response.getBody());
> >     }
> >
> >     // ---------- Apoyo ----------
> >
> >     private ResponseEntity<Object> build(HttpStatus status, String message, WebRequest request,
> >                                          List<FieldIssue> details) {
> >         ErrorResponse body = details == null
> >                 ? ErrorResponse.of(status, message, path(request))
> >                 : ErrorResponse.of(status, message, path(request), details);
> >         return ResponseEntity.status(status).body(body);
> >     }
> >
> >     private String path(WebRequest request) {
> >         return request instanceof ServletWebRequest servletRequest
> >                 ? servletRequest.getRequest().getRequestURI()
> >                 : "";
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > 🔎 **Dos detalles.** (1) Los métodos devuelven `ResponseEntity<Object>` porque así los declara la clase base. (2) `handleExceptionInternal` conserva las **cabeceras** que la clase base calcula (por ejemplo `Allow` en un `405`).
>
> </details>
>
> <details>
> <summary><strong>Paso 5.2 · Ver el resultado</strong></summary>
>
> > ```bash
> > curl -i localhost:8080/api/v1/nope
> > curl -i -X DELETE localhost:8080/api/v1/students
> > curl -i localhost:8080/api/v1/students/by-email
> > ```
> >
> > ```text
> > HTTP/1.1 404
> > {"timestamp":"2026-11-01T23:57:49.863Z","status":404,"error":"Not Found","message":"No existe el recurso solicitado","path":"/api/v1/nope"}
> >
> > HTTP/1.1 405
> > Allow: GET, POST
> > {"timestamp":"2026-11-01T23:57:49.879Z","status":405,"error":"Method Not Allowed","message":"Ese método HTTP no está permitido en esta ruta","path":"/api/v1/students"}
> >
> > HTTP/1.1 400
> > {"timestamp":"2026-11-01T23:57:49.894Z","status":400,"error":"Bad Request","message":"Falta el parámetro obligatorio 'email'","path":"/api/v1/students/by-email"}
> > ```
> >
> > Y la validación, ahora con los `details` **ordenados** por campo:
> >
> > ```text
> > "details":[{"field":"age","message":"debe ser 16 o más"},{"field":"email","message":"no tiene formato de correo electrónico"},{"field":"name","message":"debe tener entre 2 y 100 caracteres"},{"field":"name","message":"es obligatorio"}]
> > ```
> >
> > > ✅ **Punto de control.** Ya **no hay ningún `500` falso**. Las demás respuestas del Paso 4 no cambiaron.
>
> </details>

</details>

---

<details>
<summary><strong>📚 Paso 6 — Documentar los errores en Swagger</strong></summary>

> > 🗣️ **El vínculo con el Módulo 06.** En esa guía los `400`, `404` y `409` se documentaban **sin cuerpo** (`content = @Content`) porque aún no había un formato de error. Ahora existe `ErrorResponse`, y se documenta **una sola vez**.
>
> <details>
> <summary><strong>Paso 6.1 · Respuestas de error reutilizables en el contrato</strong></summary>
>
> > En vez de repetir el modelo en cada `@ApiResponse`, se declaran **tres respuestas** en `components.responses` (`BadRequest`, `NotFound`, `Conflict`) que apuntan al esquema `ErrorResponse`, con un ejemplo real cada una.
> >
> > <details>
> > <summary><strong>📄 OpenApiConfig.java</strong> <em>(solo el cambio)</em></summary>
> >
> > ```java
> >                 .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
> >                 .components(components());
> >     }
> >
> >     private Components components() {
> >         Components components = new Components()
> >                 .addSecuritySchemes("bearerAuth", new SecurityScheme()
> >                         .type(SecurityScheme.Type.HTTP)
> >                         .scheme("bearer")
> >                         .bearerFormat("JWT"))
> >                 .addResponses("BadRequest", errorResponse("La petición no es válida",
> >                         example(400, "Bad Request", "Los datos enviados no son válidos", "/api/v1/students",
> >                                 List.of(Map.of("field", "email", "message", "no tiene formato de correo electrónico")))))
> >                 .addResponses("NotFound", errorResponse("El recurso no existe",
> >                         example(404, "Not Found", "No existe un estudiante con id 999", "/api/v1/students/999", null)))
> >                 .addResponses("Conflict", errorResponse("El correo ya está registrado",
> >                         example(409, "Conflict", "El correo ana@mail.com ya está registrado", "/api/v1/students", null)));
> >         // El esquema ErrorResponse se agrega a mano porque ninguna operación lo declara como tipo de retorno.
> >         ModelConverters.getInstance().readAll(ErrorResponse.class).forEach(components::addSchemas);
> >         return components;
> >     }
> >
> >     private Map<String, Object> example(int status, String error, String message, String path, List<?> details) {
> >         Map<String, Object> example = new LinkedHashMap<>();
> >         example.put("timestamp", "2026-11-01T10:15:30.123Z");
> >         example.put("status", status);
> >         example.put("error", error);
> >         example.put("message", message);
> >         example.put("path", path);
> >         if (details != null) {
> >             example.put("details", details);
> >         }
> >         return example;
> >     }
> >
> >     private ApiResponse errorResponse(String description, Map<String, Object> example) {
> >         return new ApiResponse()
> >                 .description(description)
> >                 .content(new Content().addMediaType("application/json", new MediaType()
> >                         .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
> >                         .example(example)));
> >     }
> > ```
> >
> > Imports nuevos: `ErrorResponse`, `ModelConverters`, `Content`, `MediaType`, `Schema`, `ApiResponse` (de `io.swagger.v3.oas.models`), `LinkedHashMap` y `Map`. El código completo está en «Estructura final esperada».
> >
> > </details>
>
> </details>
>
> <details>
> <summary><strong>Paso 6.2 · Usarlas con <code>@ApiResponse(ref = …)</code></strong></summary>
>
> > <details>
> > <summary><strong>📄 StudentController.java</strong> <em>(solo el cambio)</em></summary>
> >
> > Cada `@ApiResponse` de error pasa de `content = @Content` a una **referencia**, y se agregan los `400` que faltaban (listar, buscar por id y por correo):
> >
> > ```java
> >     @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
> >     @ApiResponse(responseCode = "400", ref = "BadRequest")
> >     @ApiResponse(responseCode = "404", ref = "NotFound")
> >     @GetMapping("/{id}")
> >     public ResponseEntity<StudentResponse> findById(...)
> >
> >     @ApiResponse(responseCode = "201", description = "Estudiante creado", headers = ...)
> >     @ApiResponse(responseCode = "400", ref = "BadRequest")
> >     @ApiResponse(responseCode = "409", ref = "Conflict")
> >     @PostMapping
> >     public ResponseEntity<StudentResponse> create(...)
> > ```
> >
> > | Operación | Errores documentados |
> > |---|---|
> > | `GET /students` | `400` |
> > | `GET /students/{id}` · `GET /students/by-email` | `400` · `404` |
> > | `POST /students` | `400` · `409` |
> > | `PUT /students/{id}` | `400` · `404` · `409` |
> > | `PATCH /students/{id}/status` | `400` · `404` |
> > | `DELETE /students/{id}` | `404` |
> >
> > </details>
>
> </details>
>
> <details>
> <summary><strong>Paso 6.3 · Un tropiezo real (y su arreglo)</strong></summary>
>
> > Al armar esta documentación apareció **un aviso** en el *log*. Conviene mostrarlo.
> >
> > | Síntoma | Causa | Arreglo |
> > |---|---|---|
> > | En el *log* aparece `Json Processing Exception occurred: Java 8 date/time type java.time.OffsetDateTime not supported by default` al pedir `/api-docs` | El `timestamp` es un `Instant`: Swagger lo modela como `date-time` y su ejemplo se vuelve `OffsetDateTime`, que su serializador no sabe escribir | Declarar el campo como texto: `@Schema(type = "string", example = "2026-11-01T10:15:30.123Z")` (ya está en `ErrorResponse`) |
> >
> > > 💡 **Una versión menos de problemas.** Con springdoc **2.x** (Spring Boot 3) había un segundo tropiezo: el esquema `ErrorResponse` desaparecía del contrato porque solo lo usaban las respuestas de `components`, y hacía falta `springdoc.remove-broken-reference-definitions=false`. Con springdoc **3.2.0** (Spring Boot 4) ya **no** hace falta: `ErrorResponse` aparece solo.
>
> </details>
>
> <details>
> <summary><strong>Paso 6.4 · Ver el resultado</strong></summary>
>
> > `GET http://localhost:8080/api-docs/students`:
> >
> > ```text
> > schemas:    ErrorResponse, FieldIssue, StudentRequest, StudentResponse, StudentStatusRequest, PageResponseStudentResponse
> > responses:  BadRequest, NotFound, Conflict
> >
> > GET    /api/v1/students                  200 · 400 → #/components/responses/BadRequest
> > GET    /api/v1/students/{id}             200 · 400 · 404 → NotFound
> > GET    /api/v1/students/by-email         200 · 400 · 404
> > POST   /api/v1/students                  201 · 400 · 409 → Conflict
> > PUT    /api/v1/students/{id}             200 · 400 · 404 · 409
> > PATCH  /api/v1/students/{id}/status      200 · 400 · 404
> > DELETE /api/v1/students/{id}             204 · 404
> > ```
> >
> > Y la respuesta `NotFound`:
> >
> > ```json
> > {"description": "El recurso no existe",
> >  "content": {"application/json": {
> >    "schema": {"$ref": "#/components/schemas/ErrorResponse"},
> >    "example": {"timestamp": "2026-11-01T10:15:30.123Z", "status": 404, "error": "Not Found",
> >                "message": "No existe un estudiante con id 999", "path": "/api/v1/students/999"}}}}
> > ```
> >
> > En Swagger UI cada `400`, `404` y `409` muestra ahora el **modelo** `ErrorResponse` y su ejemplo; con **Try it out** se provoca el error y se compara la respuesta real con el ejemplo.
> >
> > > ✅ **Mismo nombre, mismo formato.** Lo que Swagger promete y lo que `GlobalExceptionHandler` devuelve salen del **mismo** `ErrorResponse`: si cambia uno, cambia el otro.
>
> </details>

</details>

---

<details>
<summary><strong>🧪 Paso 7 — Probar la API y un <code>500</code> real</strong></summary>

> <details>
> <summary><strong>Paso 7.1 · Prueba de punta a punta</strong></summary>
>
> > Con la aplicación recién arrancada y la base vacía:
> >
> > | # | Acción | Resultado esperado |
> > |:-:|---|---|
> > | 1 | `POST /internal/seed` | `200` · `{"created":3}` |
> > | 2 | `GET /students/1` | `200` · Ana Torres |
> > | 3 | `GET /students/999` | `404` · `No existe un estudiante con id 999` |
> > | 4 | `POST /students` con `ana@mail.com` | `409` · `El correo ana@mail.com ya está registrado` |
> > | 5 | `POST /students` con `{"name":"","age":5,"email":"x","status":"ACTIVE"}` | `400` · cuatro `details` |
> > | 6 | `POST /students` con `{"name":` | `400` · JSON no válido |
> > | 7 | `PATCH /students/1/status` con `{"status":"XYZ"}` | `400` · JSON no válido |
> > | 8 | `GET /students/abc` | `400` · `El parámetro 'id' tiene un valor no válido: abc` |
> > | 9 | `GET /students?sort=foo` | `400` · `No se puede ordenar por 'foo'` |
> > | 10 | `GET /nope` bajo `/api/v1` | `404` · `No existe el recurso solicitado` |
> > | 11 | `DELETE /students` | `405` · cabecera `Allow: GET, POST` |
> > | 12 | `GET /students/by-email` | `400` · `Falta el parámetro obligatorio 'email'` |
> > | 13 | `DELETE /students/3` y de nuevo `DELETE /students/3` | `204` y luego `404` |
> >
> > ```text
> > $ curl -i -X DELETE localhost:8080/api/v1/students/3
> > HTTP/1.1 204
> >
> > $ curl -i -X DELETE localhost:8080/api/v1/students/3
> > HTTP/1.1 404
> > {"timestamp":"2026-11-01T23:59:51.070Z","status":404,"error":"Not Found","message":"No existe un estudiante con id 3","path":"/api/v1/students/3"}
> > ```
>
> </details>
>
> <details>
> <summary><strong>Paso 7.2 · Un <code>500</code> de verdad: se cae la base</strong></summary>
>
> > Se detiene MySQL y se pide un estudiante que existe:
> >
> > ```bash
> > docker stop academy-mysql
> > curl -i localhost:8080/api/v1/students/1        # tarda unos 30 s: el pool espera una conexión
> > ```
> >
> > **Lo que ve el cliente:**
> >
> > ```text
> > HTTP/1.1 500
> > Content-Type: application/json
> >
> > {"timestamp":"2026-11-02T00:00:26.595Z","status":500,"error":"Internal Server Error","message":"Ocurrió un error inesperado. Intentar de nuevo más tarde.","path":"/api/v1/students/1"}
> > ```
> >
> > **Lo que ve el equipo, en el *log*:**
> >
> > ```text
> > ERROR ... c.a.p.exception.GlobalExceptionHandler   : Error inesperado en /api/v1/students/1
> >
> > org.springframework.transaction.CannotCreateTransactionException: Could not open JPA EntityManager for transaction
> > ...
> > Caused by: org.hibernate.exception.JDBCConnectionException: Unable to acquire JDBC Connection [HikariPool-1 - Connection is not available, request timed out after 30001ms ...
> > Caused by: com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
> > ```
> >
> > Al volver a arrancar la base (`docker start academy-mysql`), la API se recupera sola: el mismo `GET` responde `200`.
> >
> > | Quién | Qué recibe | Por qué |
> > |---|---|---|
> > | **Cliente** | Un mensaje genérico, sin clases, sin SQL, sin rutas internas | No debe conocer la implementación (RNF-05) |
> > | **Equipo** | La traza completa y la causa raíz | Es lo que hace falta para diagnosticar |
>
> </details>

</details>

---

<details>
<summary><strong>🗺️ Mapa: situación → mecanismo → dónde se hace</strong></summary>

> | Situación | Mecanismo | Dónde vive | Paso |
> |---|---|---|:-:|
> | Un error de negocio con un único significado HTTP y sin necesidad de cuerpo propio | `@ResponseStatus` | Sobre la clase de la excepción | 2 |
> | Un caso **propio de un controlador**, con cuerpo personalizado | `@ExceptionHandler` | Método del controlador | 3 |
> | Un formato de error **igual para toda la API** | `@RestControllerAdvice` | Clase en `presentation.exception` | 4 |
> | Validación (`@Valid`), JSON roto, tipo equivocado | `@ExceptionHandler` / `@Override` en el *advice* | `presentation.exception` | 4 y 5 |
> | `404` de ruta, `405`, `415`… | `ResponseEntityExceptionHandler` | Clase base del *advice* | 5 |
> | Cualquier fallo no previsto | `@ExceptionHandler(Exception.class)` + *log* | `presentation.exception` | 4 |
> | Avisar de un caso ya conocido y seguir (seed) | `try/catch` | Donde se decide qué hacer | 2 |
> | Documentar el error en el contrato | `@ApiResponse(ref = …)` + `components.responses` | `OpenApiConfig` y el controlador | 6 |
>
> | Característica | `@ResponseStatus` | `@ExceptionHandler` | `@RestControllerAdvice` |
> |---|---|---|---|
> | Nivel | La excepción | Un controlador | Toda la aplicación |
> | Controla el cuerpo | No | Sí | Sí |
> | Evita repetir | Parcial | No | **Sí** |
> | En esta guía | Paso 2 (luego se retira) | Paso 3 (luego se mueve) | Pasos 4 y 5 (queda) |
>
> > 🔎 **Lectura clave.** Los tres mecanismos **conviven** y se pasan la antorcha: se empezó con el más simple y se terminó con el más general, porque los **errores de toda la API** tienen que verse **iguales**.

</details>

<details>
<summary><strong>🏁 Cierre — preguntas de síntesis</strong></summary>

> 1. ¿Por qué una excepción de negocio es mejor que devolver un `ServiceResult`? *(Dice **qué pasó**, sube sola por las capas y no obliga a cada controlador a traducirla.)*
> 2. ¿Por qué las excepciones propias extienden `RuntimeException`? *(No obligan a declarar `throws` en cada método; son errores de uso de la API.)*
> 3. ¿Por qué `@ResponseStatus` no alcanza? *(No permite personalizar el cuerpo y deja al negocio conociendo HTTP.)*
> 4. ¿Qué excepción gana cuando una clase tiene `@ResponseStatus` y hay un `@ExceptionHandler` para ella? *(El manejador.)*
> 5. ¿Qué diferencia hay entre `@ExceptionHandler` en un controlador y en un `@RestControllerAdvice`? *(El alcance: un controlador frente a toda la aplicación.)*
> 6. ¿Por qué `GET /nope` devolvía `500` con el primer *advice*? *(El manejador de `Exception.class` atrapaba las excepciones de Spring MVC antes de que se convirtieran en `404`, `405` o `400`.)*
> 7. ¿Por qué el `500` no cuenta nada al cliente, pero sí al *log*? *(Detalles internos son una fuga de información; la traza es lo que necesita el equipo.)*
> 8. ¿Qué cambia en la documentación al tener un `ErrorResponse` único? *(Se documenta una vez en `components` y cada operación solo lo referencia.)*

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Solución |
> |---|---|---|
> | Un `404` o `409` llega **sin `message`** | La excepción tiene solo `@ResponseStatus` y ningún manejador | Agregar el `@ExceptionHandler` o el `@RestControllerAdvice` |
> | El manejador **no se ejecuta** | La clase del *advice* no tiene `@RestControllerAdvice` (o está fuera del paquete escaneado) | Anotarla y dejarla bajo `com.academy` |
> | `Ambiguous @ExceptionHandler method mapped for …` al arrancar | Se declaró un `@ExceptionHandler` para una excepción que `ResponseEntityExceptionHandler` ya maneja | Sustituirlo por un `@Override` del método `handleXxx` |
> | Rutas inexistentes o métodos no permitidos devuelven `500` | `@ExceptionHandler(Exception.class)` sin extender `ResponseEntityExceptionHandler` | Extenderla (Paso 5) |
> | El *log* se llena de trazas por un `404` | El mismo caso anterior: se registra como error inesperado | Igual |
> | El cliente ve la traza o el SQL | Se devolvió `ex.getMessage()` en el `500` | Mensaje genérico en el cliente y `log.error(..., ex)` |
> | El `catch` del servicio se traga el error | Un `catch (Exception e)` que no relanza | Capturar solo lo que se sabe resolver, o relanzar |
> | `OffsetDateTime not supported by default` en el *log* | `timestamp` modelado como `date-time` | `@Schema(type = "string", …)` |
> | `details` aparece vacío en el JSON | Falta `@JsonInclude(NON_EMPTY)` en el campo | Agregarlo |
> | El mensaje de validación sale en inglés | Se usa el mensaje por defecto de Bean Validation | Escribir `message = "…"` en la anotación |
> | `Communications link failure` al arrancar | MySQL no está arrancado | `docker ps` y revisar la URL |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **Estado repetido.** Lanzar una nueva excepción `StudentAlreadyInStatusException` (`409`) cuando `PATCH …/status` pide el estado que el estudiante ya tiene. Manejarla en el *advice* y documentarla en Swagger.
> 2. **Choque de correos en la base.** Dos peticiones simultáneas con el mismo correo pasan la verificación y chocan en la restricción `UNIQUE`: Spring lanza `DataIntegrityViolationException`. Manejarla como `409`.
> 3. **`ProblemDetail`.** Reemplazar `ErrorResponse` por `ProblemDetail` (RFC 7807) y comparar el JSON.
> 4. **Un identificador de seguimiento.** Agregar un `traceId` al `ErrorResponse` y escribirlo también en el *log* para cruzar cliente y equipo.
> 5. **Prueba automática.** Con `MockMvc`, escribir una prueba por cada fila de la tabla del Paso 7.1.
> 6. **Un recurso nuevo.** Repetir el patrón para `Course`: `CourseNotFoundException` y su manejador (¿hay que tocar algo más?).
> 7. **Menos repetición en Swagger.** Agregar la respuesta `500` a **todas** las operaciones con un `OpenApiCustomizer`.
> 8. **Un manejador local con sentido.** Encontrar un caso que **sí** convenga dejar en un `@ExceptionHandler` de controlador y justificar por qué.

</details>

<details>
<summary><strong>📁 Estructura final esperada</strong></summary>

> ```text
> 📁 students-api-excepciones
> ├── 📄 pom.xml
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 presentation
>     │   │   ├── 📁 config
>     │   │   │   └── 📄 OpenApiConfig.java
>     │   │   ├── 📁 controller
>     │   │   │   ├── 📄 StudentController.java
>     │   │   │   └── 📄 InternalController.java
>     │   │   ├── 📁 dto
>     │   │   │   ├── 📄 ErrorResponse.java
>     │   │   │   ├── 📄 StudentRequest.java
>     │   │   │   ├── 📄 StudentStatusRequest.java
>     │   │   │   ├── 📄 StudentResponse.java
>     │   │   │   └── 📄 PageResponse.java
>     │   │   ├── 📁 exception
>     │   │   │   └── 📄 GlobalExceptionHandler.java
>     │   │   └── 📁 mapper
>     │   │       └── 📄 StudentMapper.java
>     │   ├── 📁 business
>     │   │   ├── 📁 exception
>     │   │   │   ├── 📄 StudentNotFoundException.java
>     │   │   │   └── 📄 StudentEmailAlreadyExistsException.java
>     │   │   └── 📁 service
>     │   │       └── 📄 StudentService.java
>     │   └── 📁 persistence
>     │       ├── 📁 entity
>     │       │   ├── 📄 StudentEntity.java
>     │       │   └── 📄 StudentStatus.java
>     │       └── 📁 repository
>     │           └── 📄 StudentJpaRepository.java
>     └── 📁 resources
>         ├── 📄 application.properties
>         └── 📄 application-prod.properties
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
>     <artifactId>students-api</artifactId>
>     <version>0.0.1-SNAPSHOT</version>
>
>     <properties>
>         <java.version>21</java.version>
>     </properties>
>
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
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-validation</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springdoc</groupId>
>             <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
>             <version>3.2.0</version>
>         </dependency>
>         <dependency>
>             <groupId>com.mysql</groupId>
>             <artifactId>mysql-connector-j</artifactId>
>             <scope>runtime</scope>
>         </dependency>
>     </dependencies>
>
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
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
> spring.datasource.url=jdbc:mysql://localhost:3306/academy
> spring.datasource.username=root
> spring.datasource.password=root
>
> # JPA and Hibernate
> spring.jpa.hibernate.ddl-auto=update
> spring.jpa.show-sql=true
>
> # Pagination limits (protect the API)
> spring.data.web.pageable.default-page-size=10
> spring.data.web.pageable.max-page-size=50
>
> # Swagger (springdoc)
> springdoc.api-docs.path=/api-docs
> springdoc.swagger-ui.path=/swagger-ui.html
> springdoc.swagger-ui.urls-primary-name=Students
> springdoc.swagger-ui.deep-linking=true
> springdoc.swagger-ui.operations-sorter=method
> springdoc.swagger-ui.tags-sorter=alpha
> springdoc.swagger-ui.display-request-duration=true
> springdoc.swagger-ui.doc-expansion=list
> springdoc.swagger-ui.try-it-out-enabled=true
> springdoc.default-produces-media-type=application/json
> springdoc.default-consumes-media-type=application/json
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 application-prod.properties</strong></summary>
>
> **Ruta:** `src/main/resources/application-prod.properties`
>
> ```properties
> springdoc.api-docs.enabled=false
> springdoc.swagger-ui.enabled=false
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
> <summary><strong>📄 StudentStatus.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/persistence/entity/StudentStatus.java`
>
> ```java
> package com.academy.persistence.entity;
>
> public enum StudentStatus {
>     ACTIVE,
>     INACTIVE
> }
> ```
>
> </details>
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
> import jakarta.persistence.EnumType;
> import jakarta.persistence.Enumerated;
> import jakarta.persistence.GeneratedValue;
> import jakarta.persistence.GenerationType;
> import jakarta.persistence.Id;
> import jakarta.persistence.Table;
> import org.hibernate.annotations.CreationTimestamp;
>
> import java.time.LocalDateTime;
>
> @Entity
> @Table(name = "student")
> public class StudentEntity {
>
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>
>     @Column(nullable = false)
>     private String name;
>
>     private int age;
>
>     @Column(nullable = false, unique = true)
>     private String email;
>
>     @Enumerated(EnumType.STRING)
>     @Column(nullable = false)
>     private StudentStatus status;
>
>     @CreationTimestamp
>     @Column(name = "created_at", updatable = false)
>     private LocalDateTime createdAt;
>
>     protected StudentEntity() {
>     }
>
>     public StudentEntity(Long id, String name, int age, String email, StudentStatus status) {
>         this.id = id;
>         this.name = name;
>         this.age = age;
>         this.email = email;
>         this.status = status;
>     }
>
>     public Long getId() { return id; }
>     public String getName() { return name; }
>     public int getAge() { return age; }
>     public String getEmail() { return email; }
>     public StudentStatus getStatus() { return status; }
>     public LocalDateTime getCreatedAt() { return createdAt; }
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
> import org.springframework.data.domain.Page;
> import org.springframework.data.domain.Pageable;
> import org.springframework.data.jpa.repository.JpaRepository;
>
> import java.util.Optional;
>
> public interface StudentJpaRepository extends JpaRepository<StudentEntity, Long> {
>
>     Page<StudentEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
>
>     Optional<StudentEntity> findByEmailIgnoreCase(String email);
>
>     boolean existsByEmailIgnoreCase(String email);
>
>     boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
> }
> ```
>
> </details>
>
> ---
>
> ### 📁 business
>
> #### 📁 exception
>
> <details>
> <summary><strong>📄 StudentNotFoundException.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/business/exception/StudentNotFoundException.java`
>
> ```java
> package com.academy.business.exception;
>
> public class StudentNotFoundException extends RuntimeException {
>
>     public StudentNotFoundException(String message) {
>         super(message);
>     }
>
>     public static StudentNotFoundException byId(Long id) {
>         return new StudentNotFoundException("No existe un estudiante con id " + id);
>     }
>
>     public static StudentNotFoundException byEmail(String email) {
>         return new StudentNotFoundException("No existe un estudiante con el correo " + email);
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentEmailAlreadyExistsException.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/business/exception/StudentEmailAlreadyExistsException.java`
>
> ```java
> package com.academy.business.exception;
>
> public class StudentEmailAlreadyExistsException extends RuntimeException {
>
>     public StudentEmailAlreadyExistsException(String email) {
>         super("El correo " + email + " ya está registrado");
>     }
> }
> ```
>
> </details>
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
> import com.academy.business.exception.StudentEmailAlreadyExistsException;
> import com.academy.business.exception.StudentNotFoundException;
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.persistence.entity.StudentStatus;
> import com.academy.persistence.repository.StudentJpaRepository;
> import org.springframework.data.domain.Page;
> import org.springframework.data.domain.Pageable;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
>
> @Service
> public class StudentService {
>
>     private final StudentJpaRepository students;
>
>     public StudentService(StudentJpaRepository students) {
>         this.students = students;
>     }
>
>     @Transactional(readOnly = true)
>     public Page<StudentEntity> search(String name, Pageable pageable) {
>         if (name == null || name.isBlank()) {
>             return students.findAll(pageable);
>         }
>         return students.findByNameContainingIgnoreCase(name, pageable);
>     }
>
>     @Transactional(readOnly = true)
>     public StudentEntity findById(Long id) {
>         return students.findById(id)
>                 .orElseThrow(() -> StudentNotFoundException.byId(id));
>     }
>
>     @Transactional(readOnly = true)
>     public StudentEntity findByEmail(String email) {
>         return students.findByEmailIgnoreCase(email)
>                 .orElseThrow(() -> StudentNotFoundException.byEmail(email));
>     }
>
>     @Transactional
>     public StudentEntity create(StudentEntity data) {
>         if (students.existsByEmailIgnoreCase(data.getEmail())) {
>             throw new StudentEmailAlreadyExistsException(data.getEmail());
>         }
>         return students.save(new StudentEntity(
>                 null, data.getName(), data.getAge(), data.getEmail(), data.getStatus()));
>     }
>
>     @Transactional
>     public StudentEntity update(Long id, StudentEntity data) {
>         if (!students.existsById(id)) {
>             throw StudentNotFoundException.byId(id);
>         }
>         if (students.existsByEmailIgnoreCaseAndIdNot(data.getEmail(), id)) {
>             throw new StudentEmailAlreadyExistsException(data.getEmail());
>         }
>         return students.save(new StudentEntity(
>                 id, data.getName(), data.getAge(), data.getEmail(), data.getStatus()));
>     }
>
>     @Transactional
>     public StudentEntity changeStatus(Long id, StudentStatus status) {
>         StudentEntity student = findById(id);
>         return students.save(new StudentEntity(
>                 id, student.getName(), student.getAge(), student.getEmail(), status));
>     }
>
>     @Transactional
>     public void delete(Long id) {
>         if (!students.existsById(id)) {
>             throw StudentNotFoundException.byId(id);
>         }
>         students.deleteById(id);
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
> #### 📁 dto
>
> <details>
> <summary><strong>📄 ErrorResponse.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/dto/ErrorResponse.java`
>
> ```java
> package com.academy.presentation.dto;
>
> import com.fasterxml.jackson.annotation.JsonInclude;
> import io.swagger.v3.oas.annotations.media.Schema;
> import org.springframework.http.HttpStatus;
>
> import java.time.Instant;
> import java.time.temporal.ChronoUnit;
> import java.util.List;
>
> @Schema(description = "Cuerpo de toda respuesta de error de la API")
> public record ErrorResponse(
>
>         @Schema(description = "Momento en que ocurrió el error, en UTC (ISO-8601)", type = "string", example = "2026-11-01T10:15:30.123Z")
>         Instant timestamp,
>
>         @Schema(description = "Código de estado HTTP", example = "404")
>         int status,
>
>         @Schema(description = "Nombre del estado HTTP", example = "Not Found")
>         String error,
>
>         @Schema(description = "Qué pasó, en lenguaje claro", example = "No existe un estudiante con id 999")
>         String message,
>
>         @Schema(description = "Ruta de la petición que falló", example = "/api/v1/students/999")
>         String path,
>
>         @Schema(description = "Detalle campo por campo (solo en errores de validación)")
>         @JsonInclude(JsonInclude.Include.NON_EMPTY)
>         List<FieldIssue> details
> ) {
>
>     @Schema(description = "Un campo inválido y la razón")
>     public record FieldIssue(
>
>             @Schema(description = "Nombre del campo", example = "email")
>             String field,
>
>             @Schema(description = "Por qué es inválido", example = "debe ser una dirección de correo electrónico bien formada")
>             String message
>     ) {
>     }
>
>     public static ErrorResponse of(HttpStatus status, String message, String path) {
>         return new ErrorResponse(Instant.now().truncatedTo(ChronoUnit.MILLIS), status.value(), status.getReasonPhrase(), message, path, List.of());
>     }
>
>     public static ErrorResponse of(HttpStatus status, String message, String path, List<FieldIssue> details) {
>         return new ErrorResponse(Instant.now().truncatedTo(ChronoUnit.MILLIS), status.value(), status.getReasonPhrase(), message, path, details);
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentRequest.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/dto/StudentRequest.java`
>
> ```java
> package com.academy.presentation.dto;
>
> import com.academy.persistence.entity.StudentStatus;
> import io.swagger.v3.oas.annotations.media.Schema;
> import jakarta.validation.constraints.Email;
> import jakarta.validation.constraints.Max;
> import jakarta.validation.constraints.Min;
> import jakarta.validation.constraints.NotBlank;
> import jakarta.validation.constraints.NotNull;
> import jakarta.validation.constraints.Size;
>
> @Schema(description = "Datos que el cliente envía para crear o reemplazar un estudiante")
> public record StudentRequest(
>
>         @Schema(description = "Nombre completo", example = "Ana Torres")
>         @NotBlank(message = "es obligatorio")
>         @Size(min = 2, max = 100, message = "debe tener entre 2 y 100 caracteres")
>         String name,
>
>         @Schema(description = "Edad en años", example = "22")
>         @Min(value = 16, message = "debe ser 16 o más")
>         @Max(value = 99, message = "debe ser 99 o menos")
>         int age,
>
>         @Schema(description = "Correo electrónico, único entre estudiantes", example = "ana@mail.com")
>         @NotBlank(message = "es obligatorio")
>         @Email(message = "no tiene formato de correo electrónico")
>         String email,
>
>         @Schema(description = "Estado académico", example = "ACTIVE")
>         @NotNull(message = "es obligatorio")
>         StudentStatus status
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentStatusRequest.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/dto/StudentStatusRequest.java`
>
> ```java
> package com.academy.presentation.dto;
>
> import com.academy.persistence.entity.StudentStatus;
> import io.swagger.v3.oas.annotations.media.Schema;
> import jakarta.validation.constraints.NotNull;
>
> @Schema(description = "Nuevo estado del estudiante (es lo único que cambia)")
> public record StudentStatusRequest(
>
>         @Schema(description = "Estado académico", example = "INACTIVE")
>         @NotNull(message = "es obligatorio")
>         StudentStatus status
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentResponse.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/dto/StudentResponse.java`
>
> ```java
> package com.academy.presentation.dto;
>
> import com.academy.persistence.entity.StudentStatus;
> import io.swagger.v3.oas.annotations.media.Schema;
>
> @Schema(description = "Estudiante tal como lo ve el cliente")
> public record StudentResponse(
>
>         @Schema(description = "Identificador generado por la base", example = "1")
>         Long id,
>
>         @Schema(description = "Nombre completo", example = "Ana Torres")
>         String name,
>
>         @Schema(description = "Edad en años", example = "22")
>         int age,
>
>         @Schema(description = "Correo electrónico", example = "ana@mail.com")
>         String email,
>
>         @Schema(description = "Estado académico", example = "ACTIVE")
>         StudentStatus status
> ) {
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 PageResponse.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/dto/PageResponse.java`
>
> ```java
> package com.academy.presentation.dto;
>
> import io.swagger.v3.oas.annotations.media.Schema;
>
> import java.util.List;
>
> @Schema(description = "Una página de resultados")
> public record PageResponse<T>(
>
>         @Schema(description = "Elementos de la página")
>         List<T> content,
>
>         @Schema(description = "Número de página, desde 0", example = "0")
>         int page,
>
>         @Schema(description = "Elementos por página (máximo 50)", example = "10")
>         int size,
>
>         @Schema(description = "Total de elementos que cumplen el filtro", example = "3")
>         long totalElements,
>
>         @Schema(description = "Total de páginas", example = "1")
>         int totalPages
> ) {
> }
> ```
>
> </details>
>
> #### 📁 exception
>
> <details>
> <summary><strong>📄 GlobalExceptionHandler.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/exception/GlobalExceptionHandler.java`
>
> ```java
> package com.academy.presentation.exception;
>
> import com.academy.business.exception.StudentEmailAlreadyExistsException;
> import com.academy.business.exception.StudentNotFoundException;
> import com.academy.presentation.dto.ErrorResponse;
> import com.academy.presentation.dto.ErrorResponse.FieldIssue;
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> import org.springframework.beans.TypeMismatchException;
> import org.springframework.data.core.PropertyReferenceException;
> import org.springframework.http.HttpHeaders;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.HttpStatusCode;
> import org.springframework.http.ResponseEntity;
> import org.springframework.http.converter.HttpMessageNotReadableException;
> import org.springframework.web.bind.MethodArgumentNotValidException;
> import org.springframework.web.bind.MissingServletRequestParameterException;
> import org.springframework.web.bind.annotation.ExceptionHandler;
> import org.springframework.web.bind.annotation.RestControllerAdvice;
> import org.springframework.web.context.request.ServletWebRequest;
> import org.springframework.web.context.request.WebRequest;
> import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
>
> import java.util.Comparator;
> import java.util.List;
>
> @RestControllerAdvice
> public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
>
>     private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
>
>     // ---------- Excepciones de la aplicación ----------
>
>     @ExceptionHandler(StudentNotFoundException.class)
>     public ResponseEntity<Object> handleNotFound(StudentNotFoundException ex, WebRequest request) {
>         return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
>     }
>
>     @ExceptionHandler(StudentEmailAlreadyExistsException.class)
>     public ResponseEntity<Object> handleDuplicateEmail(StudentEmailAlreadyExistsException ex, WebRequest request) {
>         return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
>     }
>
>     @ExceptionHandler(PropertyReferenceException.class)
>     public ResponseEntity<Object> handleUnknownSortProperty(PropertyReferenceException ex, WebRequest request) {
>         return build(HttpStatus.BAD_REQUEST, "No se puede ordenar por '" + ex.getPropertyName() + "'", request, null);
>     }
>
>     @ExceptionHandler(Exception.class)
>     public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
>         log.error("Error inesperado en {}", path(request), ex);
>         return build(HttpStatus.INTERNAL_SERVER_ERROR,
>                 "Ocurrió un error inesperado. Intentar de nuevo más tarde.", request, null);
>     }
>
>     // ---------- Excepciones de Spring MVC (las maneja la clase base; aquí se les da el cuerpo propio) ----------
>
>     @Override
>     protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
>                                                                   HttpHeaders headers, HttpStatusCode status,
>                                                                   WebRequest request) {
>         List<FieldIssue> details = ex.getBindingResult().getFieldErrors().stream()
>                 .map(error -> new FieldIssue(error.getField(), error.getDefaultMessage()))
>                 .sorted(Comparator.comparing(FieldIssue::field).thenComparing(FieldIssue::message))
>                 .toList();
>         return build(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos", request, details);
>     }
>
>     @Override
>     protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
>                                                                   HttpHeaders headers, HttpStatusCode status,
>                                                                   WebRequest request) {
>         return build(HttpStatus.BAD_REQUEST,
>                 "El cuerpo de la petición no es un JSON válido o tiene un valor no permitido", request, null);
>     }
>
>     @Override
>     protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
>                                                         HttpStatusCode status, WebRequest request) {
>         return build(HttpStatus.BAD_REQUEST,
>                 "El parámetro '" + ex.getPropertyName() + "' tiene un valor no válido: " + ex.getValue(),
>                 request, null);
>     }
>
>     @Override
>     protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
>                                                                           HttpHeaders headers, HttpStatusCode status,
>                                                                           WebRequest request) {
>         return build(HttpStatus.BAD_REQUEST,
>                 "Falta el parámetro obligatorio '" + ex.getParameterName() + "'", request, null);
>     }
>
>     // Todas las demás excepciones de Spring MVC (ruta inexistente, método no permitido...) pasan por aquí.
>     @Override
>     protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
>                                                              HttpStatusCode statusCode, WebRequest request) {
>         HttpStatus status = HttpStatus.valueOf(statusCode.value());
>         String message = switch (status) {
>             case NOT_FOUND -> "No existe el recurso solicitado";
>             case METHOD_NOT_ALLOWED -> "Ese método HTTP no está permitido en esta ruta";
>             case UNSUPPORTED_MEDIA_TYPE -> "El tipo de contenido enviado no está soportado";
>             case NOT_ACCEPTABLE -> "No se puede responder en el formato pedido";
>             default -> status.getReasonPhrase();
>         };
>         ResponseEntity<Object> response = build(status, message, request, null);
>         return ResponseEntity.status(status).headers(headers).body(response.getBody());
>     }
>
>     // ---------- Apoyo ----------
>
>     private ResponseEntity<Object> build(HttpStatus status, String message, WebRequest request,
>                                          List<FieldIssue> details) {
>         ErrorResponse body = details == null
>                 ? ErrorResponse.of(status, message, path(request))
>                 : ErrorResponse.of(status, message, path(request), details);
>         return ResponseEntity.status(status).body(body);
>     }
>
>     private String path(WebRequest request) {
>         return request instanceof ServletWebRequest servletRequest
>                 ? servletRequest.getRequest().getRequestURI()
>                 : "";
>     }
> }
> ```
>
> </details>
>
> #### 📁 mapper
>
> <details>
> <summary><strong>📄 StudentMapper.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/mapper/StudentMapper.java`
>
> ```java
> package com.academy.presentation.mapper;
>
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.presentation.dto.PageResponse;
> import com.academy.presentation.dto.StudentRequest;
> import com.academy.presentation.dto.StudentResponse;
> import org.springframework.data.domain.Page;
>
> public final class StudentMapper {
>
>     private StudentMapper() {
>     }
>
>     public static StudentEntity toEntity(StudentRequest request) {
>         return new StudentEntity(null, request.name(), request.age(), request.email(), request.status());
>     }
>
>     public static StudentResponse toResponse(StudentEntity entity) {
>         return new StudentResponse(entity.getId(), entity.getName(), entity.getAge(),
>                 entity.getEmail(), entity.getStatus());
>     }
>
>     public static PageResponse<StudentResponse> toPageResponse(Page<StudentEntity> page) {
>         return new PageResponse<>(
>                 page.getContent().stream().map(StudentMapper::toResponse).toList(),
>                 page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
>     }
> }
> ```
>
> </details>
>
> #### 📁 config
>
> <details>
> <summary><strong>📄 OpenApiConfig.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/config/OpenApiConfig.java`
>
> ```java
> package com.academy.presentation.config;
>
> import com.academy.presentation.dto.ErrorResponse;
> import io.swagger.v3.core.converter.ModelConverters;
> import io.swagger.v3.oas.models.Components;
> import io.swagger.v3.oas.models.OpenAPI;
> import io.swagger.v3.oas.models.info.Contact;
> import io.swagger.v3.oas.models.info.Info;
> import io.swagger.v3.oas.models.info.License;
> import io.swagger.v3.oas.models.media.Content;
> import io.swagger.v3.oas.models.media.MediaType;
> import io.swagger.v3.oas.models.media.Schema;
> import io.swagger.v3.oas.models.responses.ApiResponse;
> import io.swagger.v3.oas.models.security.SecurityScheme;
> import io.swagger.v3.oas.models.servers.Server;
> import org.springdoc.core.models.GroupedOpenApi;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
>
> import java.util.LinkedHashMap;
> import java.util.List;
> import java.util.Map;
>
> @Configuration
> public class OpenApiConfig {
>
>     @Bean
>     public OpenAPI academyOpenApi() {
>         return new OpenAPI()
>                 .info(new Info()
>                         .title("Academy API")
>                         .version("1.0.0")
>                         .description("CRUD de estudiantes con DTO, Spring Boot, JPA y MySQL, documentado con OpenAPI.")
>                         .contact(new Contact().name("Equipo Academy").email("academy@mail.com"))
>                         .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
>                 .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
>                 .components(components());
>     }
>
>     private Components components() {
>         Components components = new Components()
>                 .addSecuritySchemes("bearerAuth", new SecurityScheme()
>                         .type(SecurityScheme.Type.HTTP)
>                         .scheme("bearer")
>                         .bearerFormat("JWT"))
>                 .addResponses("BadRequest", errorResponse("La petición no es válida",
>                         example(400, "Bad Request", "Los datos enviados no son válidos", "/api/v1/students",
>                                 List.of(Map.of("field", "email", "message", "no tiene formato de correo electrónico")))))
>                 .addResponses("NotFound", errorResponse("El recurso no existe",
>                         example(404, "Not Found", "No existe un estudiante con id 999", "/api/v1/students/999", null)))
>                 .addResponses("Conflict", errorResponse("El correo ya está registrado",
>                         example(409, "Conflict", "El correo ana@mail.com ya está registrado", "/api/v1/students", null)));
>         // El esquema ErrorResponse se agrega a mano porque ninguna operación lo declara como tipo de retorno.
>         ModelConverters.getInstance().readAll(ErrorResponse.class).forEach(components::addSchemas);
>         return components;
>     }
>
>     private Map<String, Object> example(int status, String error, String message, String path, List<?> details) {
>         Map<String, Object> example = new LinkedHashMap<>();
>         example.put("timestamp", "2026-11-01T10:15:30.123Z");
>         example.put("status", status);
>         example.put("error", error);
>         example.put("message", message);
>         example.put("path", path);
>         if (details != null) {
>             example.put("details", details);
>         }
>         return example;
>     }
>
>     private ApiResponse errorResponse(String description, Map<String, Object> example) {
>         return new ApiResponse()
>                 .description(description)
>                 .content(new Content().addMediaType("application/json", new MediaType()
>                         .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
>                         .example(example)));
>     }
>
>     @Bean
>     public GroupedOpenApi studentsGroup() {
>         return GroupedOpenApi.builder()
>                 .group("students")
>                 .displayName("Students")
>                 .pathsToMatch("/api/v1/students/**")
>                 .build();
>     }
>
>     @Bean
>     public GroupedOpenApi internalGroup() {
>         return GroupedOpenApi.builder()
>                 .group("internal")
>                 .displayName("Internal")
>                 .pathsToMatch("/internal/**")
>                 .build();
>     }
> }
> ```
>
> </details>
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
> import com.academy.presentation.dto.PageResponse;
> import com.academy.presentation.dto.StudentRequest;
> import com.academy.presentation.dto.StudentResponse;
> import com.academy.presentation.dto.StudentStatusRequest;
> import com.academy.presentation.mapper.StudentMapper;
> import io.swagger.v3.oas.annotations.Hidden;
> import io.swagger.v3.oas.annotations.Operation;
> import io.swagger.v3.oas.annotations.Parameter;
> import io.swagger.v3.oas.annotations.headers.Header;
> import io.swagger.v3.oas.annotations.media.Content;
> import io.swagger.v3.oas.annotations.media.ExampleObject;
> import io.swagger.v3.oas.annotations.media.Schema;
> import io.swagger.v3.oas.annotations.responses.ApiResponse;
> import io.swagger.v3.oas.annotations.security.SecurityRequirement;
> import io.swagger.v3.oas.annotations.tags.Tag;
> import jakarta.validation.Valid;
> import org.springdoc.core.annotations.ParameterObject;
> import org.springframework.data.domain.Pageable;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.DeleteMapping;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PatchMapping;
> import org.springframework.web.bind.annotation.PathVariable;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.PutMapping;
> import org.springframework.web.bind.annotation.RequestBody;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RequestParam;
> import org.springframework.web.bind.annotation.RestController;
> import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
>
> import java.net.URI;
>
> @RestController
> @RequestMapping("/api/v1/students")
> @Tag(name = "Students", description = "Operaciones sobre estudiantes")
> public class StudentController {
>
>     private final StudentService service;
>
>     public StudentController(StudentService service) {
>         this.service = service;
>     }
>
>     @Operation(summary = "Listar estudiantes",
>             description = "Devuelve una página de estudiantes. Con `name` filtra por nombre (contiene, sin importar mayúsculas). El tamaño máximo de página es 50.")
>     @ApiResponse(responseCode = "200", description = "Página de estudiantes")
>     @ApiResponse(responseCode = "400", ref = "BadRequest")
>     @GetMapping
>     public PageResponse<StudentResponse> search(
>             @Parameter(description = "Parte del nombre a buscar", example = "ana")
>             @RequestParam(required = false) String name,
>             @ParameterObject Pageable pageable) {
>         return StudentMapper.toPageResponse(service.search(name, pageable));
>     }
>
>     @Operation(summary = "Buscar un estudiante por id")
>     @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
>     @ApiResponse(responseCode = "400", ref = "BadRequest")
>     @ApiResponse(responseCode = "404", ref = "NotFound")
>     @GetMapping("/{id}")
>     public ResponseEntity<StudentResponse> findById(
>             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
>         return ResponseEntity.ok(StudentMapper.toResponse(service.findById(id)));
>     }
>
>     @Operation(summary = "Buscar un estudiante por correo",
>             description = "Obsoleto: usar `GET /api/v1/students?name=` o `GET /api/v1/students/{id}`.",
>             deprecated = true)
>     @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
>     @ApiResponse(responseCode = "400", ref = "BadRequest")
>     @ApiResponse(responseCode = "404", ref = "NotFound")
>     @GetMapping("/by-email")
>     public ResponseEntity<StudentResponse> findByEmail(
>             @Parameter(description = "Correo exacto", example = "ana@mail.com") @RequestParam String email) {
>         return ResponseEntity.ok(StudentMapper.toResponse(service.findByEmail(email)));
>     }
>
>     @Operation(summary = "Crear un estudiante",
>             description = "El `id` lo genera la base y no se envía. Si el correo ya existe responde `409`.")
>     @io.swagger.v3.oas.annotations.parameters.RequestBody(
>             description = "Datos del estudiante",
>             content = @Content(examples = {
>                     @ExampleObject(name = "Estudiante activo", summary = "Un estudiante activo",
>                             value = "{\"name\":\"Ana Torres\",\"age\":22,\"email\":\"ana@mail.com\",\"status\":\"ACTIVE\"}"),
>                     @ExampleObject(name = "Estudiante inactivo", summary = "Un estudiante inactivo",
>                             value = "{\"name\":\"Luis Pérez\",\"age\":25,\"email\":\"luis@mail.com\",\"status\":\"INACTIVE\"}")
>             }))
>     @ApiResponse(responseCode = "201", description = "Estudiante creado",
>             headers = @Header(name = "Location", description = "URL del estudiante creado",
>                     schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/students/1")))
>     @ApiResponse(responseCode = "400", ref = "BadRequest")
>     @ApiResponse(responseCode = "409", ref = "Conflict")
>     @PostMapping
>     public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
>         StudentResponse body = StudentMapper.toResponse(service.create(StudentMapper.toEntity(request)));
>         URI location = ServletUriComponentsBuilder.fromCurrentRequest()
>                 .path("/{id}").buildAndExpand(body.id()).toUri();
>         return ResponseEntity.created(location).body(body);
>     }
>
>     @Operation(summary = "Reemplazar un estudiante",
>             description = "Reemplaza todos los datos del estudiante. Responde `404` si no existe y `409` si el correo es de otro estudiante.")
>     @ApiResponse(responseCode = "200", description = "Estudiante actualizado")
>     @ApiResponse(responseCode = "400", ref = "BadRequest")
>     @ApiResponse(responseCode = "404", ref = "NotFound")
>     @ApiResponse(responseCode = "409", ref = "Conflict")
>     @PutMapping("/{id}")
>     public ResponseEntity<StudentResponse> update(
>             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id,
>             @Valid @RequestBody StudentRequest request) {
>         return ResponseEntity.ok(StudentMapper.toResponse(service.update(id, StudentMapper.toEntity(request))));
>     }
>
>     @Operation(summary = "Cambiar el estado de un estudiante",
>             description = "Cambia solo el estado; el resto de los datos no se toca.")
>     @ApiResponse(responseCode = "200", description = "Estado actualizado")
>     @ApiResponse(responseCode = "400", ref = "BadRequest")
>     @ApiResponse(responseCode = "404", ref = "NotFound")
>     @PatchMapping("/{id}/status")
>     public ResponseEntity<StudentResponse> changeStatus(
>             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id,
>             @Valid @RequestBody StudentStatusRequest request) {
>         return ResponseEntity.ok(StudentMapper.toResponse(service.changeStatus(id, request.status())));
>     }
>
>     @Operation(summary = "Eliminar un estudiante", security = @SecurityRequirement(name = "bearerAuth"))
>     @ApiResponse(responseCode = "204", description = "Estudiante eliminado")
>     @ApiResponse(responseCode = "404", ref = "NotFound")
>     @DeleteMapping("/{id}")
>     public ResponseEntity<Void> delete(
>             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
>         service.delete(id);
>         return ResponseEntity.noContent().build();
>     }
>
>     @Hidden
>     @GetMapping("/ping")
>     public String ping() {
>         return "pong";
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 InternalController.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/presentation/controller/InternalController.java`
>
> ```java
> package com.academy.presentation.controller;
>
> import com.academy.business.exception.StudentEmailAlreadyExistsException;
> import com.academy.business.service.StudentService;
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.persistence.entity.StudentStatus;
> import io.swagger.v3.oas.annotations.Operation;
> import io.swagger.v3.oas.annotations.tags.Tag;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PostMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
>
> import java.util.Map;
>
> @RestController
> @RequestMapping("/internal")
> @Tag(name = "Internal", description = "Operaciones de apoyo para el equipo (no son parte de la API pública)")
> public class InternalController {
>
>     private final StudentService service;
>
>     public InternalController(StudentService service) {
>         this.service = service;
>     }
>
>     @Operation(summary = "Estado del servicio")
>     @GetMapping("/health")
>     public Map<String, String> health() {
>         return Map.of("status", "UP");
>     }
>
>     @Operation(summary = "Cargar estudiantes de ejemplo", description = "Crea tres estudiantes si no existen.")
>     @PostMapping("/seed")
>     public Map<String, Integer> seed() {
>         int created = 0;
>         StudentEntity[] samples = {
>                 new StudentEntity(null, "Ana Torres", 22, "ana@mail.com", StudentStatus.ACTIVE),
>                 new StudentEntity(null, "Luis Pérez", 25, "luis@mail.com", StudentStatus.ACTIVE),
>                 new StudentEntity(null, "María Gómez", 31, "maria@mail.com", StudentStatus.INACTIVE)
>         };
>         for (StudentEntity sample : samples) {
>             try {
>                 service.create(sample);
>                 created++;
>             } catch (StudentEmailAlreadyExistsException ignored) {
>                 // ya existe: no se vuelve a crear
>             }
>         }
>         return Map.of("created", created);
>     }
> }
> ```
>
> </details>

</details>
