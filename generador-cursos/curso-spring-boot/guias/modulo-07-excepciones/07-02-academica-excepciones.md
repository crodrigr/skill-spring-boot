# 🚨 07-02-Guía paso a paso — Academia con manejo de excepciones

> 👩‍🏫 **Material docente.** Aplica la [guía 07-01](07-01-guia-crud-estudiantes-con-excepciones.md) (excepciones propias, `@RestControllerAdvice` y errores documentados en Swagger) al proyecto de la academia ya documentado ([`04-02-academica-swagger`](../modulo-04-documentacion-apis-swagger/04-02-academica-swagger.md), Módulo 04): **estudiantes**, **cursos** y **matrículas** con MySQL.
> Contiene código completo: no se entrega a los estudiantes antes de resolver el módulo anterior.
>
> 📦 **Proyecto terminado:** [`academica-excepciones/`](codigo/academica-excepciones/) · 🧾 **Todo el código final, listo para copiar:** [`07-03-academica-excepciones-codigo-completo.md`](07-03-academica-excepciones-codigo-completo.md).
>
> ✅ **Verificada.** El código se compiló para Java 21 y se ejecutó con Spring Boot 4.1.2, springdoc-openapi 3.2.0 y MySQL 8. Todas las respuestas que se muestran son salidas reales de la aplicación.
>
> 🧭 **Idea central.** Con tres recursos, el manejo manual se nota más: **cada** controlador repite `orElseGet(notFound)` o un `switch`, y **cada** servicio inventa su forma de decir «no existe» o «choca». Con excepciones, el servicio **lanza** y **un solo lugar** decide qué responde la API.

## 🧭 La idea en un minuto

```text
EnrollmentServiceImpl ── lanza CourseFullException ──▶ EnrollmentController (no hace nada)
                                                            │
                                                            ▼
                                       GlobalExceptionHandler ──▶ 409 + JSON con el mensaje
```

El controlador solo cuenta el **caso feliz**; todos los errores de los tres recursos salen con **el mismo formato** (`ErrorResponse`).

<details>
<summary><strong>🎯 Objetivos</strong></summary>

> Al terminar, el estudiante:
>
> - Reemplaza `ServiceResult` y `ServiceStatus` por **excepciones propias** en la capa `business`.
> - Agrupa varias excepciones en **un solo manejador** (`404` para las que «no existen», `409` para las que «chocan»).
> - Centraliza el manejo en un `@RestControllerAdvice` y devuelve **siempre** un `ErrorResponse`.
> - Distingue los errores del cliente (`4xx`) de los del servidor (`5xx`).
> - Documenta los errores **una sola vez** en el contrato OpenAPI y los reutiliza con `@ApiResponse(ref = …)`.
> - Comprueba cada respuesta con `curl`.

</details>

<details>
<summary><strong>📝 Introducción: qué cambia respecto a la guía 07-01</strong></summary>

> | Pieza | Igual que en la [guía 07-01](07-01-guia-crud-estudiantes-con-excepciones.md) |
> |---|---|
> | La idea | El servicio lanza y un manejador global responde |
> | `ErrorResponse` | Mismo cuerpo para todo error |
> | Excepciones de `Spring` (`400`, `404`, `405`, `500`) | Mismo manejador |
>
> **Lo nuevo con tres recursos:**
>
> | Novedad | Detalle |
> |---|---|
> | Más excepciones propias | `CourseNotFoundException`, `EnrollmentNotFoundException`, `AlreadyEnrolledException`, `CourseFullException` |
> | Un manejador por **familia** | `@ExceptionHandler({A.class, B.class, C.class})`: tres excepciones, un `404`; tres, un `409` |
> | La matrícula tiene **dos** `404` y **dos** `409` | Estudiante o curso inexistente; matrícula repetida o curso sin cupo: el **mensaje** dice cuál |
> | `ServiceResult<Long>` desaparece | `countEnrolled` devuelve un `long` o lanza |
>
> > 💡 `@ResponseStatus` y el manejador local en un controlador se estudian en la [guía 07-01](07-01-guia-crud-estudiantes-con-excepciones.md); aquí se va directo al **manejo global**.

</details>

<details>
<summary><strong>📏 Los errores que la API debe distinguir</strong></summary>

> | Situación | Excepción | HTTP |
> |---|---|:-:|
> | No existe el estudiante (id o correo) | `StudentNotFoundException` (propia) | `404` |
> | No existe el curso | `CourseNotFoundException` (propia) | `404` |
> | No existe la matrícula | `EnrollmentNotFoundException` (propia) | `404` |
> | El correo ya está registrado | `StudentEmailAlreadyExistsException` (propia) | `409` |
> | El estudiante ya está en ese curso | `AlreadyEnrolledException` (propia) | `409` |
> | El curso llegó a su cupo | `CourseFullException` (propia) | `409` |
> | Un campo no cumple su validación | `MethodArgumentNotValidException` | `400` + detalle |
> | JSON roto o valor no permitido | `HttpMessageNotReadableException` | `400` |
> | Parámetro con tipo equivocado (`/students/abc`) | `MethodArgumentTypeMismatchException` | `400` |
> | Orden por una propiedad inexistente (`?sort=foo`) | `PropertyReferenceException` | `400` |
> | Falta un parámetro obligatorio | `MissingServletRequestParameterException` | `400` |
> | Ruta inexistente / método no permitido | `NoResourceFoundException` / `HttpRequestMethodNotSupportedException` | `404` / `405` |
> | Cualquier otro fallo (se cae la base) | `Exception` | `500` |

</details>

<details>
<summary><strong>🛠️ Reglas del proyecto</strong></summary>

> - Las excepciones propias viven en `business.exception`, extienden `RuntimeException` y **no importan nada de Spring Web**.
> - El manejador global y `ErrorResponse` viven en `presentation`.
> - **Todo** error tiene el mismo cuerpo: `ErrorResponse`.
> - Un `500` **no revela detalles** al cliente, pero **sí** guarda la traza en el *log*.
> - Los controladores no tienen `try/catch` y los errores se documentan en Swagger.
> - Código en **inglés**; mensajes al cliente en **español**.

</details>

<details>
<summary><strong>🏗️ Arquitectura: el árbol de archivos (🆕 nuevo · ✏️ cambia · 🗑️ se elimina)</strong></summary>

> ```text
> 📁 academica-excepciones
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 presentation
>     │   │   ├── 📁 config
>     │   │   │   └── ✏️ OpenApiConfig.java           respuestas de error reutilizables
>     │   │   ├── 📁 controller
>     │   │   │   ├── ✏️ StudentController.java        solo el camino feliz
>     │   │   │   ├── ✏️ CourseController.java         solo el camino feliz
>     │   │   │   ├── ✏️ EnrollmentController.java     solo el camino feliz
>     │   │   │   └── ✏️ InternalController.java       seed con try/catch
>     │   │   ├── 📁 dto
>     │   │   │   ├── 🆕 ErrorResponse.java            el cuerpo de todo error
>     │   │   │   ├── ✏️ StudentRequest.java           mensajes en español
>     │   │   │   ├── ✏️ CourseRequest.java            mensajes en español
>     │   │   │   ├── ✏️ EnrollmentRequest.java        mensajes en español
>     │   │   │   └── 📄 StudentResponse · CourseResponse · EnrolledResponse · EnrollmentResponse · PageResponse
>     │   │   ├── 📁 exception
>     │   │   │   └── 🆕 GlobalExceptionHandler.java
>     │   │   └── 📁 mapper                            sin cambios
>     │   ├── 📁 business
>     │   │   ├── 📁 exception
>     │   │   │   ├── 🆕 StudentNotFoundException.java
>     │   │   │   ├── 🆕 StudentEmailAlreadyExistsException.java
>     │   │   │   ├── 🆕 CourseNotFoundException.java
>     │   │   │   ├── 🆕 EnrollmentNotFoundException.java
>     │   │   │   ├── 🆕 AlreadyEnrolledException.java
>     │   │   │   └── 🆕 CourseFullException.java
>     │   │   ├── 📁 service
>     │   │   │   ├── ✏️ StudentService.java           interfaz: devuelve el dato o lanza
>     │   │   │   ├── ✏️ CourseService.java
>     │   │   │   ├── ✏️ EnrollmentService.java
>     │   │   │   ├── 🗑️ ServiceResult.java
>     │   │   │   └── 🗑️ ServiceStatus.java
>     │   │   └── 📁 impl
>     │   │       ├── ✏️ StudentServiceImpl.java       lanza excepciones
>     │   │       ├── ✏️ CourseServiceImpl.java
>     │   │       └── ✏️ EnrollmentServiceImpl.java
>     │   └── 📁 persistence                           sin cambios
>     └── 📁 resources
>         └── 📄 application.properties
> ```

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Punto de partida: la academia documentada con Swagger</strong></summary>

> <details>
> <summary><strong>Paso 0.1 · El proyecto</strong></summary>
>
> > No se crea un proyecto nuevo: se parte de [`academica-swagger`](../modulo-04-documentacion-apis-swagger/codigo/academica-swagger/), que se termina en la [guía 04-02](../modulo-04-documentacion-apis-swagger/04-02-academica-swagger.md) del Módulo 04.
> >
> > ```bash
> > cp -r academica-swagger academica-excepciones
> > ```
> >
> > Si hace falta, se crea la base:
> >
> > ```bash
> > docker run -d --name academy-mysql \
> >   -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academy \
> >   -p 3306:3306 mysql:8.0
> > ```
> >
> > > | Lo que **ya existe** | Dónde |
> > > |---|---|
> > > | Tres entidades y tres repositorios | `persistence` |
> > > | Interfaces `StudentService`, `CourseService`, `EnrollmentService`, más `ServiceResult` y `ServiceStatus` | `business.service` |
> > > | Implementaciones `…ServiceImpl` (devuelven `Optional` y `ServiceResult`) | `business.impl` |
> > > | Ocho DTO y tres mapeadores | `presentation.dto` · `presentation.mapper` |
> > > | Cuatro controladores, documentados con Swagger | `presentation.controller` |
> > > | `OpenApiConfig` con dos grupos | `presentation.config` |
> > >
> > > **Las excepciones todavía no están**: es lo que se agrega ahora.
> >
> > ✅ **Punto de control 0:** la aplicación arranca y `POST /internal/seed` responde `{"courses":2,"students":3}`.
>
> </details>

</details>

---

<details>
<summary><strong>🔎 Paso 1 — Ver los problemas del manejo manual</strong></summary>

> > 💬 Antes de cambiar nada, se provocan errores y se mira qué recibe el cliente.
>
> <details>
> <summary><strong>Paso 1.1 · Provocar los errores</strong></summary>
>
> > ```bash
> > curl -i localhost:8080/api/v1/students/999
> > curl -i -X POST localhost:8080/api/v1/students -H 'Content-Type: application/json' \
> >   -d '{"name":"Ana Otra","age":30,"email":"ANA@mail.com"}'
> > curl -i -X POST localhost:8080/api/v1/students -H 'Content-Type: application/json' \
> >   -d '{"name":"A","age":12,"email":"no-es-correo"}'
> > curl -i -X POST localhost:8080/api/v1/students -H 'Content-Type: application/json' -d '{"name":"Ana"'
> > curl -i localhost:8080/api/v1/students/abc
> > curl -i 'localhost:8080/api/v1/students?sort=foo,asc'
> > curl -i localhost:8080/api/v1/courses/999
> > curl -i -X POST localhost:8080/api/v1/enrollments -H 'Content-Type: application/json' \
> >   -d '{"studentId":999,"courseId":1}'
> > curl -i -X POST localhost:8080/api/v1/enrollments -H 'Content-Type: application/json' \
> >   -d '{"studentId":1,"courseId":1}'     # se envía dos veces
> > curl -i localhost:8080/api/v1/nada
> > ```
> >
> > **Resultado real** (el cuerpo de los `400` y `500` trae además un campo `trace` con la traza de la pila, que aquí se recorta):
> >
> > | # | Petición | Respuesta |
> > |:-:|---|---|
> > | 1 | `GET /students/999` | `404` · `Content-Length: 0` (**sin cuerpo**) |
> > | 2 | `POST /students` con correo repetido | `409` · **sin cuerpo** |
> > | 3 | `POST /students` con edad 12 y correo `no-es-correo` | `400` · `{"timestamp":"…","status":400,"error":"Bad Request","trace":"org.springframework.web.bind.MethodArgumentNotValidException…"}` |
> > | 4 | `POST /students` con JSON cortado | `400` · el mismo formato, otra traza |
> > | 5 | `GET /students/abc` | `400` · el mismo formato |
> > | 6 | `GET /students?sort=foo,asc` | **`500`** · `"error":"Internal Server Error"` y `trace` |
> > | 7 | `GET /courses/999` | `404` · **sin cuerpo** |
> > | 8 | `POST /enrollments` con estudiante inexistente | `404` · **sin cuerpo** |
> > | 9 | `POST /enrollments` repetida | `409` · **sin cuerpo** |
> > | 10 | `GET /nada` | `404` · `{"timestamp":"…","status":404,"error":"Not Found","trace":"…NoResourceFoundException…"}` |
>
> </details>
>
> <details>
> <summary><strong>Paso 1.2 · Los cinco problemas</strong></summary>
>
> > | # | Problema | Evidencia |
> > |:-:|---|---|
> > | 1 | **Respuestas vacías.** El cliente sabe que falló, no **por qué**: ¿no existe el estudiante o el curso? ¿está repetida o no hay cupo? | Filas 1, 2, 7, 8 y 9 |
> > | 2 | **Las mismas dos respuestas para causas distintas.** Una matrícula repetida y un curso sin cupo son ambas un `409` sin cuerpo. | Fila 9 y el curso sin cupo |
> > | 3 | **Un error del cliente que parece del servidor.** Ordenar por una propiedad inexistente es culpa de quien llama, pero responde `500`. | Fila 6 |
> > | 4 | **Los errores filtran detalles internos.** El campo `trace` muestra clases y rutas del servidor. | Filas 3 a 6 y 10 |
> > | 5 | **Controladores y servicios hablan el mismo idioma de códigos.** `ServiceResult`, `switch` y `orElseGet(notFound)` se repiten en cada controlador. | Código de abajo |
> >
> > <details>
> > <summary><strong>📄 El código que causa el problema 5</strong> <em>(Módulo 04)</em></summary>
> >
> > ```java
> > // EnrollmentServiceImpl
> > if (student.isEmpty() || course.isEmpty()) {
> >     return ServiceResult.notFound();          // ¿cuál de los dos faltaba?
> > }
> > if (enrollments.existsByStudentIdAndCourseId(studentId, courseId)) {
> >     return ServiceResult.conflict();          // repetida...
> > }
> > if (enrollments.countByCourseId(courseId) >= course.get().getMaxCapacity()) {
> >     return ServiceResult.conflict();          // ...o sin cupo: indistinguible
> > }
> >
> > // EnrollmentController
> > return switch (result.status()) {
> >     case NOT_FOUND -> ResponseEntity.notFound().build();
> >     case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
> >     case OK -> { ... }
> > };
> > ```
> >
> > </details>
>
> </details>

</details>

---

<details>
<summary><strong>🏷️ Paso 2 — Excepciones propias: el servicio lanza en vez de devolver</strong></summary>

> <details>
> <summary><strong>Paso 2.1 · Seis excepciones de la capa de negocio</strong></summary>
>
> > Una excepción por situación. Todas extienden `RuntimeException` (no obligan a escribir `throws`) y **no saben nada de HTTP**: el código `404` o `409` lo decide la capa de presentación.
> >
> > <details>
> > <summary><strong>📄 StudentNotFoundException.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/exception/StudentNotFoundException.java`
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
> > <details>
> > <summary><strong>📄 CourseNotFoundException.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/exception/CourseNotFoundException.java`
> >
> > ```java
> > package com.academy.business.exception;
> >
> > public class CourseNotFoundException extends RuntimeException {
> >
> >     public CourseNotFoundException(Long id) {
> >         super("No existe un curso con id " + id);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 EnrollmentNotFoundException.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/exception/EnrollmentNotFoundException.java`
> >
> > ```java
> > package com.academy.business.exception;
> >
> > public class EnrollmentNotFoundException extends RuntimeException {
> >
> >     public EnrollmentNotFoundException(Long id) {
> >         super("No existe una matrícula con id " + id);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 AlreadyEnrolledException.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/exception/AlreadyEnrolledException.java`
> >
> > ```java
> > package com.academy.business.exception;
> >
> > public class AlreadyEnrolledException extends RuntimeException {
> >
> >     public AlreadyEnrolledException(Long studentId, Long courseId) {
> >         super("El estudiante " + studentId + " ya está matriculado en el curso " + courseId);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 CourseFullException.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/exception/CourseFullException.java`
> >
> > ```java
> > package com.academy.business.exception;
> >
> > public class CourseFullException extends RuntimeException {
> >
> >     public CourseFullException(Long courseId, int maxCapacity) {
> >         super("El curso " + courseId + " ya alcanzó su cupo de " + maxCapacity);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > 💡 **Por qué `RuntimeException`.** La excepción **sube sola** por las capas hasta quien la maneje, sin `throws` en cada método.
> > >
> > > 💡 **El mensaje lleva el dato** (`"No existe un curso con id 999"`): el cliente entiende qué falló sin abrir el *log*.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 2.2 · Los tres servicios lanzan en vez de devolver</strong></summary>
>
> > Se **eliminan** `ServiceResult` y `ServiceStatus`. Las interfaces devuelven el dato directamente (sin `Optional` ni `ServiceResult`) y las implementaciones **lanzan** cuando algo falla:
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
> > public interface StudentService {
> >
> >     Page<StudentEntity> findAll(Pageable pageable);
> >
> >     StudentEntity findById(Long id);
> >
> >     StudentEntity findByEmail(String email);
> >
> >     StudentEntity create(StudentEntity student);
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
> > import com.academy.business.exception.StudentEmailAlreadyExistsException;
> > import com.academy.business.exception.StudentNotFoundException;
> > import com.academy.business.service.StudentService;
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.persistence.repository.StudentJpaRepository;
> > import org.springframework.data.domain.Page;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
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
> >     public StudentEntity findById(Long id) {
> >         return students.findById(id)
> >                 .orElseThrow(() -> StudentNotFoundException.byId(id));
> >     }
> >
> >     @Override
> >     @Transactional(readOnly = true)
> >     public StudentEntity findByEmail(String email) {
> >         return students.findByEmailIgnoreCase(email)
> >                 .orElseThrow(() -> StudentNotFoundException.byEmail(email));
> >     }
> >
> >     @Override
> >     @Transactional
> >     public StudentEntity create(StudentEntity student) {
> >         if (students.existsByEmailIgnoreCase(student.getEmail())) {
> >             throw new StudentEmailAlreadyExistsException(student.getEmail());
> >         }
> >         return students.save(student);
> >     }
> > }
> > ```
> >
> > </details>
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
> > public interface CourseService {
> >
> >     Page<CourseEntity> findAll(Pageable pageable);
> >
> >     CourseEntity findById(Long id);
> >
> >     CourseEntity create(CourseEntity course);
> >
> >     long countEnrolled(Long courseId);
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
> > import com.academy.business.exception.CourseNotFoundException;
> > import com.academy.business.service.CourseService;
> > import com.academy.persistence.entity.CourseEntity;
> > import com.academy.persistence.repository.CourseJpaRepository;
> > import com.academy.persistence.repository.EnrollmentJpaRepository;
> > import org.springframework.data.domain.Page;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
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
> >     public CourseEntity findById(Long id) {
> >         return courses.findById(id)
> >                 .orElseThrow(() -> new CourseNotFoundException(id));
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
> >     public long countEnrolled(Long courseId) {
> >         if (!courses.existsById(courseId)) {
> >             throw new CourseNotFoundException(courseId);
> >         }
> >         return enrollments.countByCourseId(courseId);
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
> > public interface EnrollmentService {
> >
> >     EnrollmentEntity findById(Long id);
> >
> >     EnrollmentEntity enroll(Long studentId, Long courseId);
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
> > import com.academy.business.exception.AlreadyEnrolledException;
> > import com.academy.business.exception.CourseFullException;
> > import com.academy.business.exception.CourseNotFoundException;
> > import com.academy.business.exception.EnrollmentNotFoundException;
> > import com.academy.business.exception.StudentNotFoundException;
> > import com.academy.business.service.EnrollmentService;
> > import com.academy.persistence.entity.CourseEntity;
> > import com.academy.persistence.entity.EnrollmentEntity;
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.persistence.repository.CourseJpaRepository;
> > import com.academy.persistence.repository.EnrollmentJpaRepository;
> > import com.academy.persistence.repository.StudentJpaRepository;
> > import org.springframework.stereotype.Service;
> > import org.springframework.transaction.annotation.Transactional;
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
> >     public EnrollmentEntity findById(Long id) {
> >         return enrollments.findById(id)
> >                 .orElseThrow(() -> new EnrollmentNotFoundException(id));
> >     }
> >
> >     @Override
> >     @Transactional
> >     public EnrollmentEntity enroll(Long studentId, Long courseId) {
> >         StudentEntity student = students.findById(studentId)
> >                 .orElseThrow(() -> StudentNotFoundException.byId(studentId));
> >         CourseEntity course = courses.findById(courseId)
> >                 .orElseThrow(() -> new CourseNotFoundException(courseId));
> >         if (enrollments.existsByStudentIdAndCourseId(studentId, courseId)) {
> >             throw new AlreadyEnrolledException(studentId, courseId);
> >         }
> >         if (enrollments.countByCourseId(courseId) >= course.getMaxCapacity()) {
> >             throw new CourseFullException(courseId, course.getMaxCapacity());
> >         }
> >         return enrollments.save(new EnrollmentEntity(student, course));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Método | Antes | Ahora |
> > > |---|---|---|
> > > | `StudentService.findById` / `findByEmail` | `Optional<StudentEntity>` | `StudentEntity` — lanza `StudentNotFoundException` |
> > > | `StudentService.create` | `ServiceResult<StudentEntity>` | `StudentEntity` — lanza `StudentEmailAlreadyExistsException` |
> > > | `CourseService.findById` | `Optional<CourseEntity>` | `CourseEntity` — lanza `CourseNotFoundException` |
> > > | `CourseService.countEnrolled` | `ServiceResult<Long>` | `long` — lanza `CourseNotFoundException` |
> > > | `EnrollmentService.findById` | `Optional<EnrollmentEntity>` | `EnrollmentEntity` — lanza `EnrollmentNotFoundException` |
> > > | `EnrollmentService.enroll` | `ServiceResult<EnrollmentEntity>` | `EnrollmentEntity` — lanza `StudentNotFoundException`, `CourseNotFoundException`, `AlreadyEnrolledException` o `CourseFullException` |
> > >
> > > 💡 **Beneficio:** `enroll` antes decía `NOT_FOUND` sin aclarar **qué** faltaba y `CONFLICT` para dos causas distintas. Ahora cada causa tiene su excepción y su mensaje.
> > >
> > > 💡 Las interfaces cambian de firma: cualquier clase que dependa de ellas (los controladores) debe actualizarse, y el compilador lo señala.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 2.3 · Los controladores quedan con el camino feliz</strong></summary>
>
> > Desaparecen `orElseGet(notFound)`, el `switch` y los `if` de `CONFLICT`. Cada método cuenta solo lo que pasa cuando todo sale bien; los errores se declaran en `@ApiResponse` (se reutilizan en el Paso 5).
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
> > import com.academy.presentation.dto.PageResponse;
> > import com.academy.presentation.dto.StudentRequest;
> > import com.academy.presentation.dto.StudentResponse;
> > import com.academy.presentation.mapper.StudentMapper;
> > import io.swagger.v3.oas.annotations.Hidden;
> > import io.swagger.v3.oas.annotations.Operation;
> > import io.swagger.v3.oas.annotations.Parameter;
> > import io.swagger.v3.oas.annotations.headers.Header;
> > import io.swagger.v3.oas.annotations.media.Content;
> > import io.swagger.v3.oas.annotations.media.ExampleObject;
> > import io.swagger.v3.oas.annotations.media.Schema;
> > import io.swagger.v3.oas.annotations.responses.ApiResponse;
> > import io.swagger.v3.oas.annotations.tags.Tag;
> > import jakarta.validation.Valid;
> > import org.springdoc.core.annotations.ParameterObject;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.http.ResponseEntity;
> > import org.springframework.web.bind.annotation.GetMapping;
> > import org.springframework.web.bind.annotation.PathVariable;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.RequestBody;
> > import org.springframework.web.bind.annotation.RequestMapping;
> > import org.springframework.web.bind.annotation.RequestParam;
> > import org.springframework.web.bind.annotation.RestController;
> > import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
> >
> > import java.net.URI;
> >
> > @RestController
> > @RequestMapping("/api/v1/students")
> > @Tag(name = "Students", description = "Operaciones sobre estudiantes")
> > public class StudentController {
> >
> >     private final StudentService service;
> >
> >     public StudentController(StudentService service) {
> >         this.service = service;
> >     }
> >
> >     @Operation(operationId = "listStudents", summary = "Listar estudiantes",
> >             description = "Devuelve una página de estudiantes. El tamaño máximo de página es 50.")
> >     @ApiResponse(responseCode = "200", description = "Página de estudiantes")
> >     @ApiResponse(responseCode = "400", ref = "BadRequest")
> >     @GetMapping
> >     public PageResponse<StudentResponse> findAll(@ParameterObject Pageable pageable) {
> >         return StudentMapper.toPageResponse(service.findAll(pageable));
> >     }
> >
> >     @Operation(operationId = "getStudent", summary = "Buscar un estudiante por id")
> >     @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
> >     @ApiResponse(responseCode = "400", ref = "BadRequest")
> >     @ApiResponse(responseCode = "404", ref = "NotFound")
> >     @GetMapping("/{id}")
> >     public ResponseEntity<StudentResponse> findById(
> >             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
> >         return ResponseEntity.ok(StudentMapper.toResponse(service.findById(id)));
> >     }
> >
> >     @Operation(operationId = "getStudentByEmail", summary = "Buscar un estudiante por correo",
> >             description = "Obsoleto: usar `GET /api/v1/students/{id}`.",
> >             deprecated = true)
> >     @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
> >     @ApiResponse(responseCode = "400", ref = "BadRequest")
> >     @ApiResponse(responseCode = "404", ref = "NotFound")
> >     @GetMapping("/by-email")
> >     public ResponseEntity<StudentResponse> findByEmail(
> >             @Parameter(description = "Correo exacto", example = "ana@mail.com") @RequestParam String email) {
> >         return ResponseEntity.ok(StudentMapper.toResponse(service.findByEmail(email)));
> >     }
> >
> >     @Operation(operationId = "createStudent", summary = "Registrar un estudiante",
> >             description = "El `id` lo genera la base y no se envía. Si el correo ya existe responde `409`.")
> >     @io.swagger.v3.oas.annotations.parameters.RequestBody(
> >             description = "Datos del estudiante",
> >             content = @Content(examples = {
> >                     @ExampleObject(name = "Estudiante adulto", summary = "Un estudiante de 22 años",
> >                             value = "{\"name\":\"Ana Torres\",\"age\":22,\"email\":\"ana@mail.com\"}"),
> >                     @ExampleObject(name = "Estudiante mayor", summary = "Un estudiante de 31 años",
> >                             value = "{\"name\":\"María Gómez\",\"age\":31,\"email\":\"maria@mail.com\"}")
> >             }))
> >     @ApiResponse(responseCode = "201", description = "Estudiante registrado",
> >             headers = @Header(name = "Location", description = "URL del estudiante creado",
> >                     schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/students/1")))
> >     @ApiResponse(responseCode = "400", ref = "BadRequest")
> >     @ApiResponse(responseCode = "409", ref = "Conflict")
> >     @PostMapping
> >     public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
> >         StudentResponse body = StudentMapper.toResponse(service.create(StudentMapper.toEntity(request)));
> >         URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                 .path("/{id}").buildAndExpand(body.id()).toUri();
> >         return ResponseEntity.created(location).body(body);
> >     }
> >
> >     @Hidden
> >     @GetMapping("/ping")
> >     public String ping() {
> >         return "pong";
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
> > import com.academy.presentation.dto.CourseRequest;
> > import com.academy.presentation.dto.CourseResponse;
> > import com.academy.presentation.dto.EnrolledResponse;
> > import com.academy.presentation.dto.PageResponse;
> > import com.academy.presentation.mapper.CourseMapper;
> > import io.swagger.v3.oas.annotations.Operation;
> > import io.swagger.v3.oas.annotations.Parameter;
> > import io.swagger.v3.oas.annotations.headers.Header;
> > import io.swagger.v3.oas.annotations.media.Content;
> > import io.swagger.v3.oas.annotations.media.Schema;
> > import io.swagger.v3.oas.annotations.responses.ApiResponse;
> > import io.swagger.v3.oas.annotations.security.SecurityRequirement;
> > import io.swagger.v3.oas.annotations.tags.Tag;
> > import jakarta.validation.Valid;
> > import org.springdoc.core.annotations.ParameterObject;
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
> > @Tag(name = "Courses", description = "Operaciones sobre cursos")
> > public class CourseController {
> >
> >     private final CourseService service;
> >
> >     public CourseController(CourseService service) {
> >         this.service = service;
> >     }
> >
> >     @Operation(operationId = "listCourses", summary = "Listar cursos",
> >             description = "Devuelve una página de cursos. El tamaño máximo de página es 50.")
> >     @ApiResponse(responseCode = "200", description = "Página de cursos")
> >     @ApiResponse(responseCode = "400", ref = "BadRequest")
> >     @GetMapping
> >     public PageResponse<CourseResponse> findAll(@ParameterObject Pageable pageable) {
> >         return CourseMapper.toPageResponse(service.findAll(pageable));
> >     }
> >
> >     @Operation(operationId = "getCourse", summary = "Buscar un curso por id")
> >     @ApiResponse(responseCode = "200", description = "Curso encontrado")
> >     @ApiResponse(responseCode = "400", ref = "BadRequest")
> >     @ApiResponse(responseCode = "404", ref = "NotFound")
> >     @GetMapping("/{id}")
> >     public ResponseEntity<CourseResponse> findById(
> >             @Parameter(description = "Identificador del curso", example = "1") @PathVariable Long id) {
> >         return ResponseEntity.ok(CourseMapper.toResponse(service.findById(id)));
> >     }
> >
> >     @Operation(operationId = "createCourse", summary = "Crear un curso",
> >             description = "Requiere autenticación (esquema `bearerAuth`). El precio debe ser mayor que cero y el cupo, al menos 1.",
> >             security = @SecurityRequirement(name = "bearerAuth"))
> >     @ApiResponse(responseCode = "201", description = "Curso creado",
> >             headers = @Header(name = "Location", description = "URL del curso creado",
> >                     schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/courses/1")))
> >     @ApiResponse(responseCode = "400", ref = "BadRequest")
> >     @PostMapping
> >     public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
> >         CourseEntity created = service.create(CourseMapper.toEntity(request));
> >         CourseResponse body = CourseMapper.toResponse(created);
> >         URI location = ServletUriComponentsBuilder.fromCurrentRequest()
> >                 .path("/{id}").buildAndExpand(body.id()).toUri();
> >         return ResponseEntity.created(location).body(body);
> >     }
> >
> >     @Operation(operationId = "countCourseEnrolled", summary = "Contar los matriculados de un curso")
> >     @ApiResponse(responseCode = "200", description = "Cantidad de matriculados")
> >     @ApiResponse(responseCode = "400", ref = "BadRequest")
> >     @ApiResponse(responseCode = "404", ref = "NotFound")
> >     @GetMapping("/{id}/enrolled")
> >     public ResponseEntity<EnrolledResponse> countEnrolled(
> >             @Parameter(description = "Identificador del curso", example = "1") @PathVariable Long id) {
> >         return ResponseEntity.ok(new EnrolledResponse(id, service.countEnrolled(id)));
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
> > import com.academy.presentation.dto.EnrollmentRequest;
> > import com.academy.presentation.dto.EnrollmentResponse;
> > import com.academy.presentation.mapper.EnrollmentMapper;
> > import io.swagger.v3.oas.annotations.Operation;
> > import io.swagger.v3.oas.annotations.Parameter;
> > import io.swagger.v3.oas.annotations.headers.Header;
> > import io.swagger.v3.oas.annotations.media.Content;
> > import io.swagger.v3.oas.annotations.media.ExampleObject;
> > import io.swagger.v3.oas.annotations.media.Schema;
> > import io.swagger.v3.oas.annotations.responses.ApiResponse;
> > import io.swagger.v3.oas.annotations.tags.Tag;
> > import jakarta.validation.Valid;
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
> > @Tag(name = "Enrollments", description = "Matrículas de estudiantes en cursos")
> > public class EnrollmentController {
> >
> >     private final EnrollmentService service;
> >
> >     public EnrollmentController(EnrollmentService service) {
> >         this.service = service;
> >     }
> >
> >     @Operation(operationId = "getEnrollment", summary = "Buscar una matrícula por id")
> >     @ApiResponse(responseCode = "200", description = "Matrícula encontrada")
> >     @ApiResponse(responseCode = "400", ref = "BadRequest")
> >     @ApiResponse(responseCode = "404", ref = "NotFound")
> >     @GetMapping("/{id}")
> >     public ResponseEntity<EnrollmentResponse> findById(
> >             @Parameter(description = "Identificador de la matrícula", example = "1") @PathVariable Long id) {
> >         return ResponseEntity.ok(EnrollmentMapper.toResponse(service.findById(id)));
> >     }
> >
> >     @Operation(operationId = "enroll", summary = "Matricular a un estudiante en un curso",
> >             description = "Responde `404` si el estudiante o el curso no existen, y `409` si ya está matriculado o el curso no tiene cupo.")
> >     @io.swagger.v3.oas.annotations.parameters.RequestBody(
> >             description = "Ids del estudiante y del curso",
> >             content = @Content(examples = @ExampleObject(name = "Matrícula", summary = "Estudiante 1 en el curso 1",
> >                     value = "{\"studentId\":1,\"courseId\":1}")))
> >     @ApiResponse(responseCode = "201", description = "Matrícula creada",
> >             headers = @Header(name = "Location", description = "URL de la matrícula creada",
> >                     schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/enrollments/1")))
> >     @ApiResponse(responseCode = "400", ref = "BadRequest")
> >     @ApiResponse(responseCode = "404", ref = "NotFound")
> >     @ApiResponse(responseCode = "409", ref = "Conflict")
> >     @PostMapping
> >     public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
> >         EnrollmentResponse body = EnrollmentMapper.toResponse(
> >                 service.enroll(request.studentId(), request.courseId()));
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
> > <summary><strong>📄 InternalController.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/InternalController.java`
> >
> > ```java
> > package com.academy.presentation.controller;
> >
> > import com.academy.business.service.CourseService;
> > import com.academy.business.exception.StudentEmailAlreadyExistsException;
> > import com.academy.business.service.StudentService;
> > import com.academy.persistence.entity.CourseEntity;
> > import com.academy.persistence.entity.StudentEntity;
> > import io.swagger.v3.oas.annotations.Operation;
> > import io.swagger.v3.oas.annotations.tags.Tag;
> > import org.springframework.data.domain.PageRequest;
> > import org.springframework.web.bind.annotation.GetMapping;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.RequestMapping;
> > import org.springframework.web.bind.annotation.RestController;
> >
> > import java.util.Map;
> >
> > @RestController
> > @RequestMapping("/internal")
> > @Tag(name = "Internal", description = "Operaciones de apoyo para el equipo (no son parte de la API pública)")
> > public class InternalController {
> >
> >     private final StudentService students;
> >     private final CourseService courses;
> >
> >     public InternalController(StudentService students, CourseService courses) {
> >         this.students = students;
> >         this.courses = courses;
> >     }
> >
> >     @Operation(summary = "Estado del servicio")
> >     @GetMapping("/health")
> >     public Map<String, String> health() {
> >         return Map.of("status", "UP");
> >     }
> >
> >     @Operation(summary = "Cargar datos de ejemplo",
> >             description = "Crea tres estudiantes (si sus correos no existen) y dos cursos (solo si no hay ninguno).")
> >     @PostMapping("/seed")
> >     public Map<String, Integer> seed() {
> >         int createdStudents = 0;
> >         StudentEntity[] sampleStudents = {
> >                 new StudentEntity("Ana Torres", 22, "ana@mail.com"),
> >                 new StudentEntity("Luis Pérez", 25, "luis@mail.com"),
> >                 new StudentEntity("María Gómez", 31, "maria@mail.com")
> >         };
> >         for (StudentEntity sample : sampleStudents) {
> >             try {
> >                 students.create(sample);
> >                 createdStudents++;
> >             } catch (StudentEmailAlreadyExistsException ignored) {
> >                 // ya existe: no se vuelve a crear
> >             }
> >         }
> >         int createdCourses = 0;
> >         if (courses.findAll(PageRequest.of(0, 1)).isEmpty()) {
> >             courses.create(new CourseEntity("Java desde cero", 150000, 30));
> >             courses.create(new CourseEntity("Spring Boot", 250000, 20));
> >             createdCourses = 2;
> >         }
> >         return Map.of("students", createdStudents, "courses", createdCourses);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > 🔎 `InternalController.seed` es el único lugar con `try/catch`: **ignora a propósito** el correo repetido, porque cargar datos de ejemplo dos veces no es un error.
>
> </details>

</details>

---

<details>
<summary><strong>🌐 Paso 3 — <code>@RestControllerAdvice</code>: el manejo global</strong></summary>

> <details>
> <summary><strong>Paso 3.1 · El cuerpo de todo error: <code>ErrorResponse</code></strong></summary>
>
> > Un solo formato para **todos** los errores, de los tres recursos:
> >
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
> >         return new ErrorResponse(Instant.now().truncatedTo(ChronoUnit.MILLIS), status.value(), status.getReasonPhrase(), message, path, List.of());
> >     }
> >
> >     public static ErrorResponse of(HttpStatus status, String message, String path, List<FieldIssue> details) {
> >         return new ErrorResponse(Instant.now().truncatedTo(ChronoUnit.MILLIS), status.value(), status.getReasonPhrase(), message, path, details);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Campo | Para qué sirve |
> > > |---|---|
> > > | `timestamp` | Cuándo ocurrió (UTC) |
> > > | `status` y `error` | El código HTTP y su nombre |
> > > | `message` | Qué pasó, en español |
> > > | `path` | Qué ruta se llamó |
> > > | `details` | Solo en validación: un elemento por campo inválido |
>
> </details>
>
>
> <details>
> <summary><strong>Paso 3.2 · El manejador global</strong></summary>
>
> > Una clase con `@RestControllerAdvice` atiende las excepciones de **todos** los controladores. Los métodos con **varias** excepciones en `@ExceptionHandler({…})` son la novedad de esta guía:
> >
> > <details>
> > <summary><strong>📄 GlobalExceptionHandler.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/exception/GlobalExceptionHandler.java`
> >
> > ```java
> > package com.academy.presentation.exception;
> >
> > import com.academy.business.exception.AlreadyEnrolledException;
> > import com.academy.business.exception.CourseFullException;
> > import com.academy.business.exception.CourseNotFoundException;
> > import com.academy.business.exception.EnrollmentNotFoundException;
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
> > import org.springframework.web.bind.MissingServletRequestParameterException;
> > import org.springframework.web.bind.annotation.ExceptionHandler;
> > import org.springframework.web.bind.annotation.RestControllerAdvice;
> > import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
> >
> > import java.util.Comparator;
> > import java.util.List;
> >
> > @RestControllerAdvice
> > public class GlobalExceptionHandler {
> >
> >     private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
> >
> >     @ExceptionHandler({StudentNotFoundException.class, CourseNotFoundException.class,
> >             EnrollmentNotFoundException.class})
> >     public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
> >         return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
> >     }
> >
> >     @ExceptionHandler({StudentEmailAlreadyExistsException.class, AlreadyEnrolledException.class,
> >             CourseFullException.class})
> >     public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
> >         return build(HttpStatus.CONFLICT, ex.getMessage(), request);
> >     }
> >
> >     @ExceptionHandler(MethodArgumentNotValidException.class)
> >     public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
> >                                                           HttpServletRequest request) {
> >         List<FieldIssue> details = ex.getBindingResult().getFieldErrors().stream()
> >                 .map(error -> new FieldIssue(error.getField(), error.getDefaultMessage()))
> >                 .sorted(Comparator.comparing(FieldIssue::field).thenComparing(FieldIssue::message))
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
> >     @ExceptionHandler(MissingServletRequestParameterException.class)
> >     public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex,
> >                                                                 HttpServletRequest request) {
> >         return build(HttpStatus.BAD_REQUEST,
> >                 "Falta el parámetro obligatorio '" + ex.getParameterName() + "'", request);
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
> >         // Ruta inexistente (404), método no permitido (405)...: Spring ya sabe su código; no es un 500.
> >         if (ex instanceof org.springframework.web.ErrorResponse springError) {
> >             HttpStatus status = HttpStatus.valueOf(springError.getStatusCode().value());
> >             String message = switch (status) {
> >                 case NOT_FOUND -> "No existe el recurso solicitado";
> >                 case METHOD_NOT_ALLOWED -> "Ese método HTTP no está permitido en esta ruta";
> >                 default -> status.getReasonPhrase();
> >             };
> >             return ResponseEntity.status(status).headers(springError.getHeaders())
> >                     .body(ErrorResponse.of(status, message, request.getRequestURI()));
> >         }
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
> > > | Método | Atiende | Responde |
> > > |---|---|:-:|
> > > | `handleNotFound` | `StudentNotFoundException`, `CourseNotFoundException`, `EnrollmentNotFoundException` | `404` |
> > > | `handleConflict` | `StudentEmailAlreadyExistsException`, `AlreadyEnrolledException`, `CourseFullException` | `409` |
> > > | `handleValidation` | `MethodArgumentNotValidException` | `400` + `details` |
> > > | `handleUnreadableBody` | `HttpMessageNotReadableException` | `400` |
> > > | `handleTypeMismatch` | `MethodArgumentTypeMismatchException` | `400` |
> > > | `handleMissingParameter` | `MissingServletRequestParameterException` | `400` |
> > > | `handleUnknownSortProperty` | `PropertyReferenceException` | `400` |
> > > | `handleUnexpected` | `Exception` (cualquier otra) | `404`/`405` de Spring, o `500` |
> > >
> > > ⚠️ **`@ExceptionHandler(Exception.class)` atrapa también** las excepciones de Spring (ruta inexistente, método no permitido). Por eso el método primero pregunta `ex instanceof org.springframework.web.ErrorResponse`: Spring ya sabe su código y no es un `500`.
> > >
> > > 🔒 El `500` **no revela detalles**: responde un mensaje genérico y deja la traza completa en el *log* con `log.error(…)`.
> > >
> > > 💡 **Beneficio:** una excepción nueva (por ejemplo, `CourseClosedException`) se agrega a **una lista** del manejador; los controladores no se tocan.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 3.3 · Ver el resultado</strong></summary>
>
> > Se vuelve a ejecutar los comandos del Paso 1. **Resultado real:**
> >
> > **Estudiante inexistente → `404`**
> >
> > ```bash
> > curl -i localhost:8080/api/v1/students/999
> > ```
> >
> > ```text
> > HTTP/1.1 404
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-05T21:33:23.365Z",
> >   "status": 404,
> >   "error": "Not Found",
> >   "message": "No existe un estudiante con id 999",
> >   "path": "/api/v1/students/999"
> > }
> > ```
> >
> > **Curso inexistente → `404`** (el mismo manejador, otra excepción)
> >
> > ```bash
> > curl -i localhost:8080/api/v1/courses/999
> > ```
> >
> > ```text
> > HTTP/1.1 404
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-05T21:33:23.596Z",
> >   "status": 404,
> >   "error": "Not Found",
> >   "message": "No existe un curso con id 999",
> >   "path": "/api/v1/courses/999"
> > }
> > ```
> >
> > **Matrícula repetida → `409`**
> >
> > ```bash
> > curl -i -X POST localhost:8080/api/v1/enrollments -H 'Content-Type: application/json' \
> >   -d '{"studentId":1,"courseId":1}'     # la segunda vez
> > ```
> >
> > ```text
> > HTTP/1.1 409
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-05T21:33:23.729Z",
> >   "status": 409,
> >   "error": "Conflict",
> >   "message": "El estudiante 1 ya está matriculado en el curso 1",
> >   "path": "/api/v1/enrollments"
> > }
> > ```
> >
> > **Curso sin cupo → `409`** (el curso 3 tiene cupo 1 y ya hay una matrícula)
> >
> > ```bash
> > curl -i -X POST localhost:8080/api/v1/enrollments -H 'Content-Type: application/json' \
> >   -d '{"studentId":2,"courseId":3}'
> > ```
> >
> > ```text
> > HTTP/1.1 409
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-05T21:33:23.800Z",
> >   "status": 409,
> >   "error": "Conflict",
> >   "message": "El curso 3 ya alcanzó su cupo de 1",
> >   "path": "/api/v1/enrollments"
> > }
> > ```
> >
> > Las dos causas del `409`, que antes eran **la misma respuesta vacía**, ahora se distinguen por el `message`:
> >
> > | Petición | `message` |
> > |---|---|
> > | Correo de estudiante repetido | `El correo ANA@mail.com ya está registrado` |
> > | Matrícula repetida | `El estudiante 1 ya está matriculado en el curso 1` |
> > | Curso sin cupo | `El curso 3 ya alcanzó su cupo de 1` |
> > | Estudiante inexistente al matricular | `No existe un estudiante con id 999` |
> > | Curso inexistente al matricular | `No existe un curso con id 999` |
>
> </details>
>
>
> <details>
> <summary><strong>Paso 3.4 · Rutas y métodos que no existen</strong></summary>
>
> > ```bash
> > curl -i localhost:8080/api/v1/nada
> > curl -i -X DELETE localhost:8080/api/v1/students/1
> > ```
> >
> > ```text
> > HTTP/1.1 404
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-05T21:33:23.860Z",
> >   "status": 404,
> >   "error": "Not Found",
> >   "message": "No existe el recurso solicitado",
> >   "path": "/api/v1/nada"
> > }
> > ```
> >
> > ```text
> > HTTP/1.1 405
> > Allow: GET
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-05T21:33:23.871Z",
> >   "status": 405,
> >   "error": "Method Not Allowed",
> >   "message": "Ese método HTTP no está permitido en esta ruta",
> >   "path": "/api/v1/students/1"
> > }
> > ```
> >
> > Spring ya sabía el código (`404` y `405`); el manejador solo le da **el mismo formato** de siempre. La cabecera `Allow` se conserva.
>
> </details>

</details>

---

<details>
<summary><strong>🇪🇸 Paso 4 — Mensajes de validación en español</strong></summary>

> <details>
> <summary><strong>Paso 4.1 · Los tres DTO de entrada</strong></summary>
>
> > Bean Validation responde en inglés por defecto (`must be greater than or equal to 16`). Como el cliente lee `details`, cada anotación lleva su `message` en español:
> >
> > <details>
> > <summary><strong>📄 StudentRequest.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/StudentRequest.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import io.swagger.v3.oas.annotations.media.Schema;
> > import jakarta.validation.constraints.Email;
> > import jakarta.validation.constraints.Max;
> > import jakarta.validation.constraints.Min;
> > import jakarta.validation.constraints.NotBlank;
> > import jakarta.validation.constraints.Size;
> >
> > @Schema(description = "Datos que el cliente envía para registrar un estudiante")
> > public record StudentRequest(
> >
> >         @Schema(description = "Nombre completo", example = "Ana Torres")
> >         @NotBlank(message = "es obligatorio")
> >         @Size(min = 2, max = 100, message = "debe tener entre 2 y 100 caracteres")
> >         String name,
> >
> >         @Schema(description = "Edad en años", example = "22")
> >         @Min(value = 16, message = "debe ser 16 o más")
> >         @Max(value = 99, message = "debe ser 99 o menos")
> >         int age,
> >
> >         @Schema(description = "Correo electrónico, único entre estudiantes", example = "ana@mail.com")
> >         @NotBlank(message = "es obligatorio")
> >         @Email(message = "no tiene formato de correo electrónico")
> >         String email
> > ) {
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 CourseRequest.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/CourseRequest.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import io.swagger.v3.oas.annotations.media.Schema;
> > import jakarta.validation.constraints.Min;
> > import jakarta.validation.constraints.NotBlank;
> > import jakarta.validation.constraints.Positive;
> > import jakarta.validation.constraints.Size;
> >
> > @Schema(description = "Datos que el cliente envía para crear un curso")
> > public record CourseRequest(
> >
> >         @Schema(description = "Nombre del curso", example = "Java desde cero")
> >         @NotBlank(message = "es obligatorio")
> >         @Size(min = 2, max = 100, message = "debe tener entre 2 y 100 caracteres")
> >         String name,
> >
> >         @Schema(description = "Precio en pesos; debe ser mayor que cero", example = "150000")
> >         @Positive(message = "debe ser mayor que cero")
> >         double price,
> >
> >         @Schema(description = "Cupo máximo de matriculados; al menos 1", example = "30")
> >         @Min(value = 1, message = "debe ser 1 o más")
> >         int maxCapacity
> > ) {
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 EnrollmentRequest.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/EnrollmentRequest.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import io.swagger.v3.oas.annotations.media.Schema;
> > import jakarta.validation.constraints.NotNull;
> >
> > @Schema(description = "Datos que el cliente envía para matricular a un estudiante en un curso")
> > public record EnrollmentRequest(
> >
> >         @Schema(description = "Identificador del estudiante", example = "1")
> >         @NotNull(message = "es obligatorio")
> >         Long studentId,
> >
> >         @Schema(description = "Identificador del curso", example = "1")
> >         @NotNull(message = "es obligatorio")
> >         Long courseId
> > ) {
> > }
> > ```
> >
> > </details>
> >
> > > 💡 Los mensajes describen **la regla** («debe ser 16 o más»), no el campo: el campo ya viaja en `field`.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 4.2 · Ver el resultado</strong></summary>
>
> > ```bash
> > curl -i -X POST localhost:8080/api/v1/students -H 'Content-Type: application/json' \
> >   -d '{"name":"A","age":12,"email":"no-es-correo"}'
> > ```
> >
> > ```text
> > HTTP/1.1 400
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-05T21:33:23.511Z",
> >   "status": 400,
> >   "error": "Bad Request",
> >   "message": "Los datos enviados no son válidos",
> >   "path": "/api/v1/students",
> >   "details": [
> >     {
> >       "field": "age",
> >       "message": "debe ser 16 o más"
> >     },
> >     {
> >       "field": "email",
> >       "message": "no tiene formato de correo electrónico"
> >     },
> >     {
> >       "field": "name",
> >       "message": "debe tener entre 2 y 100 caracteres"
> >     }
> >   ]
> > }
> > ```
> >
> > ```bash
> > curl -i -X POST localhost:8080/api/v1/enrollments -H 'Content-Type: application/json' -d '{}'
> > ```
> >
> > ```text
> > HTTP/1.1 400
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-05T21:33:23.812Z",
> >   "status": 400,
> >   "error": "Bad Request",
> >   "message": "Los datos enviados no son válidos",
> >   "path": "/api/v1/enrollments",
> >   "details": [
> >     {
> >       "field": "courseId",
> >       "message": "es obligatorio"
> >     },
> >     {
> >       "field": "studentId",
> >       "message": "es obligatorio"
> >     }
> >   ]
> > }
> > ```
> >
> > Los `400` que antes eran iguales ahora se distinguen:
> >
> > | Petición | `message` |
> > |---|---|
> > | `POST /students` con datos inválidos | `Los datos enviados no son válidos` + `details` campo por campo |
> > | `POST /students` con JSON cortado | `El cuerpo de la petición no es un JSON válido o tiene un valor no permitido` |
> > | `GET /students/abc` | `El parámetro 'id' tiene un valor no válido: abc` |
> > | `GET /students?sort=foo,asc` | `No se puede ordenar por 'foo'` — **ya es `400`, no `500`** |
> > | `GET /students/by-email` (falta `email`) | `Falta el parámetro obligatorio 'email'` |
>
> </details>

</details>

---

<details>
<summary><strong>📚 Paso 5 — Documentar los errores en Swagger</strong></summary>

> <details>
> <summary><strong>Paso 5.1 · Respuestas de error reutilizables en el contrato</strong></summary>
>
> > Los errores ya se devuelven, pero el contrato OpenAPI todavía no los describe. En `OpenApiConfig` se declaran **una sola vez** tres respuestas (`BadRequest`, `NotFound`, `Conflict`) en `components.responses`, con el esquema `ErrorResponse` y un ejemplo:
> >
> > <details>
> > <summary><strong>📄 OpenApiConfig.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/config/OpenApiConfig.java`
> >
> > ```java
> > package com.academy.presentation.config;
> >
> > import com.academy.presentation.dto.ErrorResponse;
> > import io.swagger.v3.core.converter.ModelConverters;
> > import io.swagger.v3.oas.models.Components;
> > import io.swagger.v3.oas.models.OpenAPI;
> > import io.swagger.v3.oas.models.info.Contact;
> > import io.swagger.v3.oas.models.info.Info;
> > import io.swagger.v3.oas.models.info.License;
> > import io.swagger.v3.oas.models.media.Content;
> > import io.swagger.v3.oas.models.media.MediaType;
> > import io.swagger.v3.oas.models.media.Schema;
> > import io.swagger.v3.oas.models.responses.ApiResponse;
> > import io.swagger.v3.oas.models.security.SecurityScheme;
> > import io.swagger.v3.oas.models.servers.Server;
> > import org.springdoc.core.models.GroupedOpenApi;
> > import org.springframework.context.annotation.Bean;
> > import org.springframework.context.annotation.Configuration;
> >
> > import java.util.LinkedHashMap;
> > import java.util.List;
> > import java.util.Map;
> >
> > @Configuration
> > public class OpenApiConfig {
> >
> >     @Bean
> >     public OpenAPI academyOpenApi() {
> >         return new OpenAPI()
> >                 .info(new Info()
> >                         .title("Academy API")
> >                         .version("1.0.0")
> >                         .description("Estudiantes, cursos y matrículas con DTO, Spring Boot, JPA y MySQL, documentado con OpenAPI.")
> >                         .contact(new Contact().name("Equipo Academy").email("academy@mail.com"))
> >                         .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
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
> >                 .addResponses("Conflict", errorResponse(
> >                         "La operación choca con el estado actual (correo repetido, matrícula repetida o curso sin cupo)",
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
> >
> >     @Bean
> >     public GroupedOpenApi academyGroup() {
> >         return GroupedOpenApi.builder()
> >                 .group("academy")
> >                 .displayName("Academy")
> >                 .pathsToMatch("/api/v1/**")
> >                 .build();
> >     }
> >
> >     @Bean
> >     public GroupedOpenApi internalGroup() {
> >         return GroupedOpenApi.builder()
> >                 .group("internal")
> >                 .displayName("Internal")
> >                 .pathsToMatch("/internal/**")
> >                 .build();
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > 🔎 `ErrorResponse` se agrega **a mano** con `ModelConverters` porque ninguna operación lo declara como tipo de retorno, y sin eso el contrato no lo incluiría.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 5.2 · Usarlas con <code>@ApiResponse(ref = …)</code></strong></summary>
>
> > Los controladores del Paso 2.3 ya las usan; por ejemplo, en la matrícula:
> >
> > ```java
> > @ApiResponse(responseCode = "201", description = "Matrícula creada", headers = …)
> > @ApiResponse(responseCode = "400", ref = "BadRequest")
> > @ApiResponse(responseCode = "404", ref = "NotFound")
> > @ApiResponse(responseCode = "409", ref = "Conflict")
> > @PostMapping
> > public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) { … }
> > ```
> >
> > Antes había que repetir la descripción y el ejemplo en cada operación; ahora cada `ref` apunta a la misma respuesta.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 5.3 · Ver el resultado</strong></summary>
>
> > ```bash
> > curl -s localhost:8080/api-docs | python3 -m json.tool
> > ```
> >
> > **Resultado real:** en `components.responses` aparecen `BadRequest`, `NotFound` y `Conflict`; en `components.schemas`, `ErrorResponse` y `FieldIssue`. Y las respuestas de `POST /api/v1/enrollments`:
> >
> > ```json
> > "responses": {
> >   "201": { "description": "Matrícula creada", … },
> >   "400": { "$ref": "#/components/responses/BadRequest" },
> >   "404": { "$ref": "#/components/responses/NotFound" },
> >   "409": { "$ref": "#/components/responses/Conflict" }
> > }
> > ```
> >
> > En Swagger UI (`http://localhost:8080/swagger-ui.html`), cada operación lista sus errores con el cuerpo de ejemplo.
>
> </details>

</details>

---

<details>
<summary><strong>🧪 Paso 6 — Probar la API de punta a punta</strong></summary>

> <details>
> <summary><strong>Paso 6.1 · Una sesión completa</strong></summary>
>
> > ```bash
> > curl -s -X POST localhost:8080/internal/seed                      # {"courses":2,"students":3}
> > curl -i -X POST localhost:8080/api/v1/enrollments -H 'Content-Type: application/json' \
> >   -d '{"studentId":1,"courseId":1}'                               # 201 + Location
> > curl -s localhost:8080/api/v1/courses/1/enrolled                  # {"courseId":1,"enrolled":1}
> > ```
> >
> > **Matrícula creada → `201`**
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
> >   "courseName": "Java desde cero"
> > }
> > ```
> >
> > **Conteo después de matricular → `200`**
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
> > | Petición | Respuesta |
> > |---|---|
> > | `GET /students/999` | `404` · `No existe un estudiante con id 999` |
> > | `GET /students/by-email?email=nadie@mail.com` | `404` · `No existe un estudiante con el correo nadie@mail.com` |
> > | `GET /courses/999/enrolled` | `404` · `No existe un curso con id 999` |
> > | `GET /enrollments/999` | `404` · `No existe una matrícula con id 999` |
> > | `POST /enrollments` con `{}` | `400` · `courseId` y `studentId`: `es obligatorio` |
> > | `POST /enrollments` repetida | `409` · `El estudiante 1 ya está matriculado en el curso 1` |
> > | `POST /enrollments` en curso lleno | `409` · `El curso 3 ya alcanzó su cupo de 1` |
>
> </details>
>
>
> <details>
> <summary><strong>Paso 6.2 · Un <code>500</code> de verdad</strong></summary>
>
> > Si se detiene MySQL con la aplicación en marcha y se llama a `GET /api/v1/courses`, no hay excepción propia ni de Spring que lo explique: llega a `handleUnexpected`. El cliente recibe **solo** un mensaje genérico, sin traza:
> >
> > ```json
> > {
> >   "timestamp": "…",
> >   "status": 500,
> >   "error": "Internal Server Error",
> >   "message": "Ocurrió un error inesperado. Intentar de nuevo más tarde.",
> >   "path": "/api/v1/courses"
> > }
> > ```
> >
> > Y la traza completa queda en el *log* del servidor (`log.error("Error inesperado en {}", …)`). La guía [07-01](07-01-guia-crud-estudiantes-con-excepciones.md) muestra la prueba paso a paso con el cierre real de la base.
>
> </details>

</details>

---

<details>
<summary><strong>🗺️ Mapa: situación → mecanismo → dónde se hace</strong></summary>

> | Situación | Mecanismo | Dónde |
> |---|---|---|
> | Algo del negocio falla | Lanzar una excepción propia | `business.impl` |
> | Varias excepciones, una misma respuesta | `@ExceptionHandler({A.class, B.class})` | `GlobalExceptionHandler` |
> | Un campo no cumple la validación | `MethodArgumentNotValidException` → `400` + `details` | `GlobalExceptionHandler` |
> | Todo lo demás | `Exception` → `500` genérico + *log* | `GlobalExceptionHandler` |
> | Describir el error en el contrato | `components.responses` + `@ApiResponse(ref = …)` | `OpenApiConfig` · controladores |

</details>

<details>
<summary><strong>🏁 Cierre — preguntas de síntesis</strong></summary>

> 1. ¿Por qué las excepciones de negocio no deben importar nada de Spring Web? *(Porque el código HTTP lo decide la capa de presentación; el negocio se puede reutilizar sin ella.)*
> 2. ¿Qué ganó la matrícula frente a `ServiceResult`? *(Cada causa tiene su excepción y su mensaje: ya no es un `404` o un `409` mudo.)*
> 3. ¿Qué pasa si se agrega una excepción nueva y no se registra en el manejador? *(Cae en `handleUnexpected` y responde `500`.)*
> 4. ¿Por qué `handleUnexpected` revisa `instanceof org.springframework.web.ErrorResponse`? *(Para no convertir un `404` o `405` de Spring en un `500`.)*
> 5. ¿Dónde se declara una vez la descripción de un error y dónde se reutiliza? *(En `components.responses` de `OpenApiConfig`; en `@ApiResponse(ref = …)`.)*

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Qué hacer |
> |---|---|---|
> | Un `404` de ruta inexistente responde `500` | Falta el `instanceof` en `handleUnexpected` | Revisar el Paso 3.2 |
> | El contrato no incluye `ErrorResponse` | Falta `ModelConverters.…readAll(ErrorResponse.class)` | Revisar el Paso 5.1 |
> | Los mensajes de validación salen en inglés | Falta `message = "…"` en la anotación | Revisar el Paso 4.1 |
> | El controlador no compila tras cambiar el servicio | Aún usa `Optional`, `ServiceResult` o `ServiceStatus` | Dejar solo el camino feliz (Paso 2.3) |
> | El `seed` falla la segunda vez | Falta el `try/catch` de `StudentEmailAlreadyExistsException` | Revisar `InternalController` |
> | Un error aparece con un `trace` en el cuerpo | El manejador no atiende esa excepción y responde DevTools | Agregar el `@ExceptionHandler` correspondiente |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **Una regla nueva.** Impedir matricular a un estudiante en más de 3 cursos: lanzar `StudentOverloadedException` y registrarla en `handleConflict`.
> 2. **Un `422`.** Cambiar `CourseFullException` a `422 Unprocessable Entity` y actualizar el contrato.
> 3. **Un `ProblemDetail`.** Reemplazar `ErrorResponse` por `ProblemDetail` (RFC 9457) y comparar el cuerpo.
> 4. **Una jerarquía.** Crear `NotFoundException` y `ConflictException` como clases base, y dejar `handleNotFound`/`handleConflict` con **una** excepción cada uno.
> 5. **Probar el manejador.** Escribir una prueba con `MockMvc` que verifique el `409` de una matrícula repetida.

</details>

---

➡️ **Anterior:** [Guía 07-01 — CRUD de estudiantes con manejo de excepciones](07-01-guia-crud-estudiantes-con-excepciones.md) · **Código completo:** [07-03-academica-excepciones-codigo-completo.md](07-03-academica-excepciones-codigo-completo.md)
