# 📘 04-02-Guía paso a paso — Academia con DTO documentada con Swagger (OpenAPI)

> 👩‍🏫 **Material docente.** Aplica la [guía del Módulo 04](04-01-guia-crud-estudiantes-con-swagger.md) al proyecto de la academia ya con DTO ([`03-02-academica-dtos`](../modulo-03-diseno-api-rest-con-dto/03-02-academica-dtos.md), Módulo 03): **estudiantes**, **cursos** y **matrículas** con MySQL.
> Contiene código completo: no se entrega a los estudiantes antes de resolver el módulo anterior.
>
> 📦 **Proyecto terminado:** [`academica-swagger/`](codigo/academica-swagger/) · 🧾 **Todo el código final, listo para copiar:** [`04-03-academica-swagger-codigo-completo.md`](04-03-academica-swagger-codigo-completo.md).
>
> ✅ **Verificada.** El código se compiló para Java 21 y se ejecutó con Spring Boot 4.1.2, springdoc-openapi 3.2.0 y MySQL 8. Las respuestas, el contrato OpenAPI y la lista de operaciones que se muestran son salidas reales de la aplicación. Lo que se cuenta de la pantalla de Swagger UI se deduce de ese contrato.
>
> 🧭 **Idea central.** Con tres recursos, Swagger aporta algo que con uno no se nota: **ordena** una API de varios controladores (etiquetas, nombres de operación únicos, grupos). Y como hay DTO, la documentación de los modelos va en ellos, **no en las entidades**.

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Documentar con **OpenAPI** (usando **springdoc**) la API de la academia del Módulo 03, hasta cubrir las funcionalidades principales de Swagger.
>
> Al terminar, el estudiante:
>
> - Obtiene una documentación **automática** con una dependencia y ve qué le **falta** a una API de tres controladores.
> - Describe la API con el bean `OpenAPI`, `@Tag`, `@Operation` y `@ApiResponse`, y da nombres **únicos** a las operaciones.
> - Documenta parámetros (`@Parameter`, `@ParameterObject`) y los **DTO** (`@Schema`), y ve qué reglas de **Bean Validation** pasan al contrato y cuáles no.
> - Agrega **ejemplos**, marca una operación **obsoleta**, **oculta** otra y declara un **esquema de seguridad**.
> - Separa la API en **grupos**, **configura** la interfaz, **exporta** el contrato y lo **apaga** en producción.
> - Comprueba cada comportamiento con `curl`.

</details>

<details>
<summary><strong>📝 Introducción: qué cambia respecto al Módulo 04</strong></summary>

> <details>
> <summary><strong>🔁 Lo que se repite</strong></summary>
>
> | Pieza | Igual que en la [guía 01 del módulo 04](04-01-guia-crud-estudiantes-con-swagger.md) |
> |---|---|
> | Dependencia | `springdoc-openapi-starter-webmvc-ui` 3.2.0 |
> | Anotaciones | `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`, `@ParameterObject`, `@Hidden`, `@SecurityRequirement` |
> | `@Schema` | Va en los **DTO**; las entidades no se tocan |
> | Configuración | Bean `OpenAPI`, `GroupedOpenApi` y propiedades `springdoc.*` |
>
> </details>
>
> <details>
> <summary><strong>🆕 Lo que es nuevo en la academia</strong></summary>
>
> | Novedad | Por qué importa |
> |---|---|
> | **Tres controladores** | Hay tres etiquetas (`Students`, `Courses`, `Enrollments`) y nombres de método repetidos: `findAll`, `create`, `findById`. |
> | **Nombres de operación** | Springdoc los repite y los desambigua con sufijos (`findAll_1`, `findById_2`); se arreglan con `operationId`. |
> | **Matrícula con `404` y `409`** | Una misma operación tiene cuatro respuestas distintas que hay que documentar. |
> | **Seguridad en un `POST`** | Crear un curso se marca como operación protegida (solo documentada). |
> | **DTO planos** | `EnrollmentResponse` ya viene aplanado: el contrato no muestra entidades anidadas. |
>
> </details>
>
> <details>
> <summary><strong>🎓 El proyecto</strong></summary>
>
> | Operación | Ruta | Cuerpo | Respuestas |
> |---|---|---|---|
> | Listar estudiantes | `GET /api/v1/students?page=&size=&sort=` | | `200` |
> | Buscar estudiante | `GET /api/v1/students/{id}` | | `200` · `404` |
> | Buscar por correo *(se marcará obsoleta)* | `GET /api/v1/students/by-email?email=` | | `200` · `404` |
> | Registrar estudiante | `POST /api/v1/students` | `StudentRequest` | `201` + `Location` · `400` · `409` |
> | Listar cursos | `GET /api/v1/courses` | | `200` |
> | Buscar curso | `GET /api/v1/courses/{id}` | | `200` · `404` |
> | Crear curso *(protegida)* | `POST /api/v1/courses` | `CourseRequest` | `201` + `Location` · `400` |
> | Contar matriculados | `GET /api/v1/courses/{id}/enrolled` | | `200` · `404` |
> | Buscar matrícula | `GET /api/v1/enrollments/{id}` | | `200` · `404` |
> | Matricular | `POST /api/v1/enrollments` | `EnrollmentRequest` | `201` + `Location` · `400` · `404` · `409` |
>
> </details>
>
> <details>
> <summary><strong>🛠️ Requerimientos y restricciones</strong></summary>
>
> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Java 21 o superior, Spring Boot 4.1.2 y Maven. |
> | **RNF-02** | MySQL 8 (en Docker o instalado); la base `academia` debe existir. |
> | **RNF-03** | Las anotaciones de Swagger van **solo en `presentation`**: controladores, DTO y configuración. `business` y `persistence` **no saben** que existe Swagger. |
> | **RNF-04** | Nombres del código en **inglés**. Inyección por constructor. |
> | **RNF-05** | Duración: 1 h 30 min. |
>
> > 💡 **Alcance.** Quedan fuera: la autenticación real (el esquema de seguridad solo se **documenta**), el formato de error uniforme y las excepciones (Módulo 05), y la generación de clientes.
>
> </details>

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>

> | Paso | Minutos | Actividad |
> |---|---:|---|
> | 0 | 0 – 8 | Punto de partida: la academia con DTO |
> | 1 | 8 – 18 | Primera documentación automática (y qué le falta) |
> | 2 | 18 – 23 | Información general de la API |
> | 3 | 23 – 38 | Etiquetas, nombres de operación y respuestas |
> | 4 | 38 – 44 | Parámetros |
> | 5 | 44 – 56 | Los DTO (`@Schema`) y las validaciones |
> | 6 | 56 – 61 | Ejemplos |
> | 7 | 61 – 68 | Operaciones obsoletas y ocultas |
> | 8 | 68 – 72 | Esquema de seguridad |
> | 9 | 72 – 78 | Grupos |
> | 10 | 78 – 84 | Configurar la interfaz, exportar y apagar |
> | 11 | 84 – 90 | Probar la documentación |

</details>

<details>
<summary><strong>🏗️ Arquitectura: el árbol de archivos (🆕 se agrega con Swagger · ✏️ cambia)</strong></summary>

> ```text
> 📁 academica-swagger
> ├── ✏️ pom.xml                                   dependencia springdoc
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 presentation
>     │   │   ├── 📁 config
>     │   │   │   └── 🆕 OpenApiConfig.java        información general, seguridad y grupos
>     │   │   ├── 📁 controller
>     │   │   │   ├── ✏️ StudentController.java
>     │   │   │   ├── ✏️ CourseController.java
>     │   │   │   ├── ✏️ EnrollmentController.java
>     │   │   │   └── 🆕 InternalController.java   segundo grupo del contrato
>     │   │   ├── 📁 dto                           ✏️ los ocho con @Schema
>     │   │   └── 📁 mapper                        sin cambios
>     │   ├── 📁 business
>     │   │   ├── 📁 service
>     │   │   │   ├── ✏️ StudentService.java       un método más: findByEmail
>     │   │   │   └── …                            sin cambios (otras interfaces, ServiceResult, ServiceStatus)
>     │   │   └── 📁 impl
>     │   │       ├── ✏️ StudentServiceImpl.java   un método más: findByEmail
>     │   │       └── …                            sin cambios (CourseServiceImpl, EnrollmentServiceImpl)
>     │   └── 📁 persistence
>     │       ├── 📁 entity                        sin cambios (sin Swagger)
>     │       └── 📁 repository
>     │           └── ✏️ StudentJpaRepository.java un método más: findByEmailIgnoreCase
>     └── 📁 resources
>         ├── ✏️ application.properties            propiedades de springdoc
>         └── 🆕 application-prod.properties       apaga la documentación
> ```
>
> | Capa | Qué lleva de Swagger |
> |---|---|
> | `presentation.controller` | `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`, `@Hidden`, `@SecurityRequirement` |
> | `presentation.dto` | `@Schema` |
> | `presentation.config` | Beans `OpenAPI` y `GroupedOpenApi` |
> | `business` · `persistence` | **Nada** (solo el método `findByEmail` de la operación obsoleta) |

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Punto de partida: la academia con DTO</strong></summary>

> <details>
> <summary><strong>Paso 0.1 · El proyecto</strong></summary>
>
> > No se crea un proyecto nuevo: se parte de [`academica-dtos`](../modulo-03-diseno-api-rest-con-dto/codigo/academica-dtos/), que se termina en la [guía `03-02-academica-dtos`](../modulo-03-diseno-api-rest-con-dto/03-02-academica-dtos.md) del Módulo 03.
> >
> > ```bash
> > cp -r academica-dtos academica-swagger
> > ```
> >
> > Si hace falta, se crea la base:
> >
> > ```bash
> > docker run -d --name academy-mysql \
> >   -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academia \
> >   -p 3306:3306 mysql:8.0
> > ```
> >
> > > | Lo que **ya existe** | Dónde |
> > > |---|---|
> > > | Ocho DTO (`record`) y tres mapeadores | `presentation.dto` · `presentation.mapper` |
> > > | Interfaces `StudentService`, `CourseService`, `EnrollmentService`, más `ServiceResult` y `ServiceStatus` | `business.service` |
> > > | Implementaciones `StudentServiceImpl`, `CourseServiceImpl`, `EnrollmentServiceImpl` | `business.impl` |
> > > | Tres controladores en `/api/v1/…`, delgados | `presentation.controller` |
> > > | Límites de paginación y de errores | `application.properties` |
> > >
> > > **Swagger todavía no está**: es lo que se agrega ahora.
> >
> > ✅ **Punto de control 0:** la aplicación arranca y `POST /api/v1/students` responde `201`. Todavía **no hay documentación**: `GET /swagger-ui.html` devuelve `404`.
>
> </details>
>

</details>

---

<details>
<summary><strong>✨ Paso 1 — Primera documentación: una dependencia</strong></summary>

> <details>
> <summary><strong>Paso 1.1 · Agregar springdoc</strong></summary>
>
> > Se agrega **una** dependencia al `pom.xml` y no se escribe nada más:
> >
> > <details>
> > <summary><strong>📄 pom.xml (agregar)</strong></summary>
> >
> > **Ruta:** `pom.xml`
> >
> > ```xml
> > <dependency>
> >     <groupId>org.springdoc</groupId>
> >     <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
> >     <version>3.2.0</version>
> > </dependency>
> > ```
> >
> > </details>
> >
> > > Se reinicia la aplicación y se abren dos direcciones:
> > >
> > > | Dirección | Qué muestra |
> > > |---|---|
> > > | `http://localhost:8080/swagger-ui.html` | La pantalla interactiva (redirige con `302` a `/swagger-ui/index.html`) |
> > > | `http://localhost:8080/v3/api-docs` | El contrato OpenAPI en **JSON** |
>
> </details>
>
>
> <details>
> <summary><strong>Paso 1.2 · Qué se obtiene sin escribir nada más</strong></summary>
>
> > **Resultado real** de `/v3/api-docs` con la dependencia y nada más:
> >
> > > | Qué se obtiene | Valor real |
> > > |---|---|
> > > | Operaciones descubiertas | Las **nueve** de los tres controladores |
> > > | Agrupación | Tres etiquetas con nombres técnicos: `student-controller`, `course-controller`, `enrollment-controller` |
> > > | Título y versión | `OpenAPI definition` · `v0` (valores por defecto) |
> > > | Respuestas | **Solo `200` en todas**, aun `POST` (que responde `201`) y las que dan `404` o `409` |
> > > | Paginación | Un único parámetro `pageable`, **obligatorio**, con el modelo `Pageable` |
> > > | Modelos | Los siete DTO propios, `PageResponseStudentResponse`, `PageResponseCourseResponse` y `Pageable` (diez en total) |
> > >
> > > **🔁 Nombres de operación repetidos.** Los tres controladores tienen métodos con el mismo nombre. Springdoc usa el nombre del método como `operationId` y, cuando se repite, le agrega un sufijo:
> > >
> > > | Ruta | `operationId` real |
> > > |---|---|
> > > | `GET /api/v1/students` | `findAll` |
> > > | `GET /api/v1/courses` | `findAll_1` |
> > > | `POST /api/v1/students` | `create` |
> > > | `POST /api/v1/courses` | `create_1` |
> > > | `GET /api/v1/students/{id}` | `findById` |
> > > | `GET /api/v1/enrollments/{id}` | `findById_1` |
> > > | `GET /api/v1/courses/{id}` | `findById_2` |
> > >
> > > Quien genere un cliente a partir del contrato recibiría métodos llamados `findAll_1` o `findById_2`.
> > >
> > > **🎁 Lo que los DTO ya resuelven.** El modelo `EnrollmentRequest` que genera springdoc **sin ninguna anotación**:
> > >
> > > ```json
> > > {
> > >   "required": ["courseId", "studentId"],
> > >   "type": "object",
> > >   "properties": {
> > >     "studentId": { "type": "integer", "format": "int64" },
> > >     "courseId":  { "type": "integer", "format": "int64" }
> > >   }
> > > }
> > > ```
> > >
> > > `@NotNull` ya marca los dos campos como obligatorios, y las matrículas no anidan entidades. **No hay `id` ni `createdAt`** en ningún modelo de entrada.
> > >
> > > **🕳️ Lo que falta.** Los códigos reales (`201`, `404`, `409`), la descripción de cada operación, los ejemplos, la paginación usable, el nombre y la versión de la API y los nombres de operación únicos.
> > >
> > > 💡 **Beneficio de este paso:** la documentación **no se escribe a mano ni se desactualiza**: cambia cuando cambia el código.
> > >
> > > 🗣️ **Pregunta:** ¿qué falta? *(Todo lo que es intención humana: para qué sirve cada operación, qué puede salir mal, qué ejemplos usar.)*
>
> </details>
>
>
> <details>
> <summary><strong>Paso 1.3 · Probar una operación con <em>Try it out</em></strong></summary>
>
> > En Swagger UI: abrir `POST /api/v1/enrollments` → **Try it out** → editar el JSON → **Execute**. Se ve la petición `curl` equivalente, la URL, el código de respuesta, el cuerpo y las cabeceras.
> >
> > | Zona de la pantalla | Qué hace |
> > |---|---|
> > | Botón **Try it out** | Convierte los campos en editables |
> > | Botón **Execute** | Envía la petición real al servidor |
> > | **Curl** | Muestra el comando equivalente (útil para copiar) |
> > | **Server response** | Código, cuerpo y cabeceras reales |
> > | **Responses** | Las respuestas **documentadas** (hasta ahora, solo `200`) |
> >
> > ✅ **Punto de control 1:** `/swagger-ui.html` redirige (`302`) a `/swagger-ui/index.html` y `/v3/api-docs` devuelve JSON con las nueve operaciones.
>
> </details>
>

</details>

---

<details>
<summary><strong>🏷️ Paso 2 — Información general de la API</strong></summary>

> <details>
> <summary><strong>Paso 2.1 · El bean <code>OpenAPI</code></strong></summary>
>
> > Se crea `presentation.config.OpenApiConfig`. Un bean `OpenAPI` describe la API **completa**: nombre, versión, descripción, contacto, licencia y servidores.
> >
> > <details>
> > <summary><strong>📄 OpenApiConfig.java (versión del Paso 2)</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/config/OpenApiConfig.java`
> >
> > ```java
> > package com.academy.presentation.config;
> >
> > import io.swagger.v3.oas.models.OpenAPI;
> > import io.swagger.v3.oas.models.info.Contact;
> > import io.swagger.v3.oas.models.info.Info;
> > import io.swagger.v3.oas.models.info.License;
> > import io.swagger.v3.oas.models.servers.Server;
> > import org.springframework.context.annotation.Bean;
> > import org.springframework.context.annotation.Configuration;
> >
> > import java.util.List;
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
> >                 .servers(List.of(new Server().url("http://localhost:8080").description("Local")));
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Pieza | Dónde se ve en Swagger UI |
> > > |---|---|
> > > | `title` · `version` | El encabezado grande: **Academy API** `1.0.0` `OAS 3.0` |
> > > | `description` | El párrafo bajo el título |
> > > | `contact` | El enlace «Contact Equipo Academy» |
> > > | `license` | El enlace «Apache 2.0» |
> > > | `servers` | La lista **Servers** (elegir *local*, *pruebas* o *producción* sin cambiar el código) |
> > >
> > > 💡 **Beneficio de este paso:** quien abre la documentación sabe **qué API es**, qué versión es y a quién preguntar.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 2.2 · Ver el resultado</strong></summary>
>
> > Se reinicia. **Resultado real** de `/v3/api-docs`: `info.title` es **Academy API**, `info.version` es `1.0.0`, y `servers` trae `http://localhost:8080` con la descripción `Local`.
> >
> > ✅ **Punto de control 2:** la cabecera de la documentación muestra los datos de la API.
>
> </details>
>

</details>

---

<details>
<summary><strong>🧭 Paso 3 — Etiquetas, nombres de operación y respuestas</strong></summary>

> <details>
> <summary><strong>Paso 3.1 · <code>@Tag</code> en cada controlador</strong></summary>
>
> > Un **tag** agrupa operaciones bajo un título. Sin él, springdoc usa el nombre de la clase. Cada controlador recibe el suyo:
> >
> > <details>
> > <summary><strong>📄 Los tres controladores</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/`
> >
> > ```java
> > @Tag(name = "Students", description = "Operaciones sobre estudiantes")
> > public class StudentController { ... }
> >
> > @Tag(name = "Courses", description = "Operaciones sobre cursos")
> > public class CourseController { ... }
> >
> > @Tag(name = "Enrollments", description = "Matrículas de estudiantes en cursos")
> > public class EnrollmentController { ... }
> > ```
> >
> > </details>
> >
> > > 💡 **Beneficio:** tres secciones claras (**Students**, **Courses**, **Enrollments**) en lugar de `student-controller`, `course-controller` y `enrollment-controller`.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 3.2 · <code>@Operation</code>: resumen, descripción y nombre único</strong></summary>
>
> > Cada método recibe un **resumen** (una línea), una **descripción** (admite Markdown) y un **`operationId`** propio, que arregla los nombres repetidos del Paso 1.2.
> >
> > <details>
> > <summary><strong>📄 Listar, registrar y matricular</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/`
> >
> > ```java
> > @Operation(operationId = "listStudents", summary = "Listar estudiantes",
> >         description = "Devuelve una página de estudiantes. El tamaño máximo de página es 50.")
> > @GetMapping
> > public PageResponse<StudentResponse> findAll(...) { ... }
> >
> > @Operation(operationId = "createStudent", summary = "Registrar un estudiante",
> >         description = "El `id` lo genera la base y no se envía. Si el correo ya existe responde `409`.")
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(...) { ... }
> >
> > @Operation(operationId = "enroll", summary = "Matricular a un estudiante en un curso",
> >         description = "Responde `404` si el estudiante o el curso no existen, y `409` si ya está matriculado o el curso no tiene cupo.")
> > @PostMapping
> > public ResponseEntity<EnrollmentResponse> enroll(...) { ... }
> > ```
> >
> > </details>
> >
> > > | Atributo | Dónde se ve |
> > > |---|---|
> > > | `summary` | Al lado de la ruta, en la lista de operaciones |
> > > | `description` | Dentro de la operación, bajo el título |
> > > | `operationId` | En el contrato, y como **nombre del método** en los clientes generados |
> > >
> > > **Resultado real** de los `operationId` (ya sin sufijos): `listStudents`, `getStudent`, `getStudentByEmail`, `createStudent`, `listCourses`, `getCourse`, `createCourse`, `countCourseEnrolled`, `getEnrollment` y `enroll`.
> > >
> > > 💡 **Beneficio:** el equipo de frontend entiende **para qué sirve** cada operación, y los clientes generados tienen métodos con nombre legible y único.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 3.3 · <code>@ApiResponse</code>: documentar las respuestas</strong></summary>
>
> > Springdoc detecta `200`, pero **no puede adivinar** que el `201` crea algo, que el `404` es «no existe» y el `409` «ya existe o no hay cupo», ni que el `Location` viaja en la cabecera. Cada respuesta se declara con `@ApiResponse`. En las que **no devuelven cuerpo** se usa `content = @Content` (vacío) para que Swagger no muestre un modelo que no existe.
> >
> > <details>
> > <summary><strong>📄 Respuestas de POST /api/v1/enrollments</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/`
> >
> > ```java
> > @ApiResponse(responseCode = "201", description = "Matrícula creada",
> >         headers = @Header(name = "Location", description = "URL de la matrícula creada",
> >                 schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/enrollments/1")))
> > @ApiResponse(responseCode = "400", description = "Falta un id (Bean Validation)", content = @Content)
> > @ApiResponse(responseCode = "404", description = "El estudiante o el curso no existen", content = @Content)
> > @ApiResponse(responseCode = "409", description = "Matrícula repetida o curso sin cupo", content = @Content)
> > @PostMapping
> > public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) { ... }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 Respuestas de GET /api/v1/students/{id}</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/`
> >
> > ```java
> > @Operation(operationId = "getStudent", summary = "Buscar un estudiante por id")
> > @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
> > @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
> > @GetMapping("/{id}")
> > public ResponseEntity<StudentResponse> findById(...) { ... }
> > ```
> >
> > </details>
> >
> > > **Resultado real** (tomado de `/api-docs`):
> > >
> > > | Operación | Respuestas documentadas |
> > > |---|---|
> > > | `GET /api/v1/students` · `GET /api/v1/courses` | `200` |
> > > | `GET /api/v1/students/{id}` · `/courses/{id}` · `/enrollments/{id}` | `200` · `404` |
> > > | `GET /api/v1/courses/{id}/enrolled` | `200` · `404` |
> > > | `POST /api/v1/students` | `201` (con `Location`) · `400` · `409` |
> > > | `POST /api/v1/courses` | `201` (con `Location`) · `400` |
> > > | `POST /api/v1/enrollments` | `201` (con `Location`) · `400` · `404` · `409` |
> > >
> > > El cuerpo de cada `201` ya apunta al DTO correcto (`StudentResponse`, `CourseResponse`, `EnrollmentResponse`): springdoc lo sabe por el tipo del método.
> > >
> > > 💡 **Beneficio:** el contrato dice **qué puede salir mal** y qué significa cada código. Con la matrícula, quien consume la API distingue «el curso no existe» (`404`) de «no hay cupo» (`409`) sin probar a ciegas.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 3.4 · Ver el resultado</strong></summary>
>
> > En Swagger UI, `POST /api/v1/enrollments` lista en **Responses** `201`, `400`, `404` y `409` con su descripción, y la `201` muestra la cabecera `Location`.
> >
> > ✅ **Punto de control 3:** cada operación tiene resumen, `operationId` único y todas sus respuestas.
>
> </details>
>

</details>

---

<details>
<summary><strong>🎛️ Paso 4 — Documentar los parámetros</strong></summary>

> <details>
> <summary><strong>Paso 4.1 · <code>@Parameter</code>: la ruta</strong></summary>
>
> > Springdoc ya conoce el **nombre**, el **tipo** y si es obligatorio. `@Parameter` agrega una **descripción** y un **ejemplo**, que Swagger UI usa para **rellenar el campo**.
> >
> > <details>
> > <summary><strong>📄 Parámetros de ruta y de consulta</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/`
> >
> > ```java
> > @GetMapping("/{id}")
> > public ResponseEntity<CourseResponse> findById(
> >         @Parameter(description = "Identificador del curso", example = "1") @PathVariable Long id) { ... }
> >
> > @GetMapping("/by-email")
> > public ResponseEntity<StudentResponse> findByEmail(
> >         @Parameter(description = "Correo exacto", example = "ana@mail.com") @RequestParam String email) { ... }
> > ```
> >
> > </details>
> >
> > > 💡 **Beneficio:** el ejemplo `1` o `ana@mail.com` aparece ya escrito en el campo; quien prueba solo pulsa **Execute**.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 4.2 · <code>@ParameterObject</code>: la paginación</strong></summary>
>
> > `Pageable` es un objeto con varios campos (`page`, `size`, `sort`). Sin ayuda, Swagger lo muestra como **un solo cuadro JSON** (el parámetro `pageable` del Paso 1). Con `@ParameterObject` se **despliega en tres parámetros** de consulta.
> >
> > <details>
> > <summary><strong>📄 Listados</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/`
> >
> > ```java
> > @GetMapping
> > public PageResponse<StudentResponse> findAll(@ParameterObject Pageable pageable) { ... }
> > ```
> >
> > </details>
> >
> > > **Resultado real** (de `/api-docs`), en `GET /api/v1/students` y `GET /api/v1/courses`:
> > >
> > > | Parámetro | Dónde | Tipo | Detalle |
> > > |---|---|---|---|
> > > | `page` | query | integer | mínimo `0`, valor por defecto `0` |
> > > | `size` | query | integer | mínimo `1`, valor por defecto `10` |
> > > | `sort` | query | array de string | `propiedad,(asc\|desc)` |
> > >
> > > ⚠️ **Lo que el contrato no dice.** El tope de **50** por página lo aplica Spring (`max-page-size`), pero springdoc **no lo lee**: `size` solo declara «mínimo 1». Por eso el tope se escribió en la descripción de cada listado (Paso 3.2).
> > >
> > > 💡 **Beneficio:** la paginación se prueba rellenando tres campos sencillos en vez de escribir un JSON a mano.
> >
> > ✅ **Punto de control 4:** los listados muestran `page`, `size` y `sort`.
>
> </details>
>

</details>

---

<details>
<summary><strong>📐 Paso 5 — Los DTO: <code>@Schema</code> y las validaciones</strong></summary>

> <details>
> <summary><strong>Paso 5.1 · <code>@Schema</code> en los ocho DTO</strong></summary>
>
> > `@Schema` describe cada **campo** (descripción, ejemplo) y cada **clase** (qué representa). Se escribe en los **DTO**. **Las entidades no se tocan.** Cada DTO es un `record`: la anotación va sobre cada **componente**.
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
> >         @NotBlank
> >         @Size(min = 2, max = 100)
> >         String name,
> >
> >         @Schema(description = "Edad en años", example = "22")
> >         @Min(16)
> >         @Max(99)
> >         int age,
> >
> >         @Schema(description = "Correo electrónico, único entre estudiantes", example = "ana@mail.com")
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
> > import io.swagger.v3.oas.annotations.media.Schema;
> >
> > @Schema(description = "Estudiante tal como lo ve el cliente")
> > public record StudentResponse(
> >
> >         @Schema(description = "Identificador generado por la base", example = "1")
> >         Long id,
> >
> >         @Schema(description = "Nombre completo", example = "Ana Torres")
> >         String name,
> >
> >         @Schema(description = "Edad en años", example = "22")
> >         int age,
> >
> >         @Schema(description = "Correo electrónico", example = "ana@mail.com")
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
> >         @NotBlank
> >         @Size(min = 2, max = 100)
> >         String name,
> >
> >         @Schema(description = "Precio en pesos; debe ser mayor que cero", example = "150000")
> >         @Positive
> >         double price,
> >
> >         @Schema(description = "Cupo máximo de matriculados; al menos 1", example = "30")
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
> > import io.swagger.v3.oas.annotations.media.Schema;
> >
> > @Schema(description = "Curso tal como lo ve el cliente")
> > public record CourseResponse(
> >
> >         @Schema(description = "Identificador generado por la base", example = "1")
> >         Long id,
> >
> >         @Schema(description = "Nombre del curso", example = "Java desde cero")
> >         String name,
> >
> >         @Schema(description = "Precio en pesos", example = "150000")
> >         double price,
> >
> >         @Schema(description = "Cupo máximo de matriculados", example = "30")
> >         int maxCapacity
> > ) {
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
> > import io.swagger.v3.oas.annotations.media.Schema;
> >
> > @Schema(description = "Cuántos estudiantes tiene matriculados un curso")
> > public record EnrolledResponse(
> >
> >         @Schema(description = "Identificador del curso", example = "1")
> >         Long courseId,
> >
> >         @Schema(description = "Cantidad de matriculados", example = "2")
> >         long enrolled
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
> >         @NotNull
> >         Long studentId,
> >
> >         @Schema(description = "Identificador del curso", example = "1")
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
> > import io.swagger.v3.oas.annotations.media.Schema;
> >
> > @Schema(description = "Matrícula tal como la ve el cliente (aplanada)")
> > public record EnrollmentResponse(
> >
> >         @Schema(description = "Identificador de la matrícula", example = "1")
> >         Long id,
> >
> >         @Schema(description = "Identificador del estudiante", example = "1")
> >         Long studentId,
> >
> >         @Schema(description = "Nombre del estudiante", example = "Ana Torres")
> >         String studentName,
> >
> >         @Schema(description = "Identificador del curso", example = "1")
> >         Long courseId,
> >
> >         @Schema(description = "Nombre del curso", example = "Java desde cero")
> >         String courseName
> > ) {
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 PageResponse.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/PageResponse.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import io.swagger.v3.oas.annotations.media.Schema;
> >
> > import java.util.List;
> >
> > @Schema(description = "Una página de resultados")
> > public record PageResponse<T>(
> >
> >         @Schema(description = "Elementos de la página")
> >         List<T> content,
> >
> >         @Schema(description = "Número de página, desde 0", example = "0")
> >         int page,
> >
> >         @Schema(description = "Elementos por página (máximo 50)", example = "10")
> >         int size,
> >
> >         @Schema(description = "Total de elementos", example = "3")
> >         long totalElements,
> >
> >         @Schema(description = "Total de páginas", example = "1")
> >         int totalPages
> > ) {
> > }
> > ```
> >
> > </details>
> >
> > > | Atributo de `@Schema` | Efecto en Swagger UI |
> > > |---|---|
> > > | `description` (sobre la clase) | El texto bajo el nombre del modelo, en **Schemas** |
> > > | `description` (sobre un campo) | El texto junto al campo |
> > > | `example` | El valor que se rellena al probar |
> > >
> > > 💡 **Beneficio de este paso:** la documentación del contrato vive **junto al contrato**. Cambiar la tabla no obliga a tocarla.
> > >
> > > 🔎 **¿Y `required` y `readOnly`?** No hacen falta: `@NotNull` y `@NotBlank` ya marcan los obligatorios, y los DTO de entrada **no tienen `id`**.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 5.2 · Bean Validation pasa solo al contrato… en parte</strong></summary>
>
> > Las restricciones **no hay que repetirlas**: springdoc las lee. **Resultado real** del modelo `CourseRequest` en `/api-docs`, ahora con `@Schema`:
> >
> > ```json
> > {
> >   "description": "Datos que el cliente envía para crear un curso",
> >   "required": ["name"],
> >   "properties": {
> >     "name":        { "type": "string", "minLength": 2, "maxLength": 100, "description": "Nombre del curso", "example": "Java desde cero" },
> >     "price":       { "type": "number", "format": "double", "exclusiveMinimum": 0, "description": "Precio en pesos; debe ser mayor que cero", "example": 150000 },
> >     "maxCapacity": { "type": "integer", "format": "int32", "minimum": 1, "description": "Cupo máximo de matriculados; al menos 1", "example": 30 }
> >   }
> > }
> > ```
> >
> > | En el código | En el contrato |
> > |---|---|
> > | `@NotBlank` | `name` en `required` |
> > | `@Size(min = 2, max = 100)` | `minLength` y `maxLength` |
> > | `@Min(1)` | `minimum: 1` |
> > | `@Positive` en `price` | `exclusiveMinimum: 0` (springdoc 3.x lo traduce solo; con springdoc 2.x no aparecía y solo lo decía la descripción) |
> > | `double price` e `int maxCapacity` (primitivos) | **No** entran en `required`, aunque siempre tienen valor |
> > | `@Email` (en `StudentRequest`) | `format: email`, y `@NotBlank` agrega `minLength: 1` |
> >
> > > 💡 **Beneficio de este paso:** **una sola fuente de verdad**. La regla se escribe una vez (en la validación) y sirve para **rechazar** datos y para **documentar**.
> > >
> > > ⚠️ **Lectura crítica.** `@Positive` y `@Email` sí llegan al contrato, pero la descripción de `price` («debe ser mayor que cero») y la de `maxCapacity` («al menos 1») **siguen siendo útiles**: dicen la regla **con palabras**, y es lo primero que lee quien abre Swagger UI. Lo que **no** llega es que un tipo primitivo (`double`, `int`) siempre tiene valor: no se marca obligatorio.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 5.3 · Ver el resultado</strong></summary>
>
> > En la sección **Schemas** aparecen los nueve modelos (`StudentRequest`, `StudentResponse`, `CourseRequest`, `CourseResponse`, `EnrolledResponse`, `EnrollmentRequest`, `EnrollmentResponse`, `PageResponseStudentResponse` y `PageResponseCourseResponse`). `EnrollmentResponse` muestra los cuatro campos planos, **no** dos objetos anidados.
> >
> > ✅ **Punto de control 5:** los modelos muestran descripción, ejemplos y obligatorios. Las entidades no cambiaron.
>
> </details>
>

</details>

---

<details>
<summary><strong>🧪 Paso 6 — Ejemplos del cuerpo de la petición</strong></summary>

> <details>
> <summary><strong>Paso 6.1 · <code>@ExampleObject</code>: varios ejemplos para elegir</strong></summary>
>
> > El ejemplo que genera `@Schema` es **uno** y se arma campo por campo. Con `@RequestBody` de Swagger y `@ExampleObject` se ofrecen **ejemplos completos**, con nombre, en un **desplegable**.
> >
> > La anotación se escribe con **nombre completo** para no chocar con la `@RequestBody` de Spring:
> >
> > <details>
> > <summary><strong>📄 POST /api/v1/students</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/`
> >
> > ```java
> > @io.swagger.v3.oas.annotations.parameters.RequestBody(
> >         description = "Datos del estudiante",
> >         content = @Content(examples = {
> >                 @ExampleObject(name = "Estudiante adulto", summary = "Un estudiante de 22 años",
> >                         value = "{\"name\":\"Ana Torres\",\"age\":22,\"email\":\"ana@mail.com\"}"),
> >                 @ExampleObject(name = "Estudiante mayor", summary = "Un estudiante de 31 años",
> >                         value = "{\"name\":\"María Gómez\",\"age\":31,\"email\":\"maria@mail.com\"}")
> >         }))
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) { ... }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 POST /api/v1/enrollments</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/`
> >
> > ```java
> > @io.swagger.v3.oas.annotations.parameters.RequestBody(
> >         description = "Ids del estudiante y del curso",
> >         content = @Content(examples = @ExampleObject(name = "Matrícula", summary = "Estudiante 1 en el curso 1",
> >                 value = "{\"studentId\":1,\"courseId\":1}")))
> > @PostMapping
> > public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) { ... }
> > ```
> >
> > </details>
> >
> > > | Pieza | Para qué |
> > > |---|---|
> > > | `io.swagger…parameters.RequestBody` | La de **Swagger** (documenta); la de Spring (`@RequestBody`) **lee** el cuerpo |
> > > | `@ExampleObject(name, summary, value)` | Un ejemplo completo en JSON |
> > > | `content = @Content(examples = …)` | Agrupa los ejemplos |
> > >
> > > **Resultado real** (de `/api-docs`): el cuerpo de `POST /api/v1/enrollments` apunta a `EnrollmentRequest`, trae el ejemplo `Matrícula` con `{"studentId":1,"courseId":1}` y `"required": true`.
> > >
> > > 💡 **Beneficio:** quien prueba elige un caso válido con un clic.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 6.2 · Ver el resultado</strong></summary>
>
> > En `POST /api/v1/students` → **Try it out** aparece, sobre el cuadro de texto, el desplegable **Examples** con los dos nombres.
> >
> > ✅ **Punto de control 6:** al elegir un ejemplo, el cuadro se rellena con ese JSON.
>
> </details>
>

</details>

---

<details>
<summary><strong>🚧 Paso 7 — Operaciones obsoletas y ocultas</strong></summary>

> <details>
> <summary><strong>Paso 7.1 · <code>deprecated</code>: avisar sin romper</strong></summary>
>
> > Se agrega una operación vieja para marcarla: `GET /api/v1/students/by-email`. Necesita un método en el repositorio y otro en el servicio —primero en la interfaz y luego en su implementación— (sin Swagger):
> >
> > <details>
> > <summary><strong>📄 StudentJpaRepository.java (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/persistence/repository/StudentJpaRepository.java`
> >
> > ```java
> > Optional<StudentEntity> findByEmailIgnoreCase(String email);
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 StudentService.java (agregar)</strong> <em>(interfaz · solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/StudentService.java`
> >
> > ```java
> > Optional<StudentEntity> findByEmail(String email);
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 StudentServiceImpl.java (agregar)</strong> <em>(implementación · solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/impl/StudentServiceImpl.java`
> >
> > ```java
> > @Override
> > @Transactional(readOnly = true)
> > public Optional<StudentEntity> findByEmail(String email) {
> >     return students.findByEmailIgnoreCase(email);
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 GET /api/v1/students/by-email</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/`
> >
> > ```java
> > @Operation(operationId = "getStudentByEmail", summary = "Buscar un estudiante por correo",
> >         description = "Obsoleto: usar `GET /api/v1/students/{id}`.",
> >         deprecated = true)
> > @GetMapping("/by-email")
> > public ResponseEntity<StudentResponse> findByEmail(
> >         @Parameter(description = "Correo exacto", example = "ana@mail.com") @RequestParam String email) {
> >     return service.findByEmail(email)
> >             .map(StudentMapper::toResponse)
> >             .map(ResponseEntity::ok)
> >             .orElseGet(() -> ResponseEntity.notFound().build());
> > }
> > ```
> >
> > </details>
> >
> > > Se **marca obsoleta**: no se borra, se avisa. En Swagger UI la operación aparece **tachada y en gris**.
> > >
> > > 💡 **Beneficio:** los clientes ven el aviso **antes** de que la operación desaparezca, y hay tiempo para migrar.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 7.2 · <code>@Hidden</code>: funciona, pero no se documenta</strong></summary>
>
> > Hay rutas que existen pero **no deben aparecer** en el contrato público (una comprobación de vida, una prueba interna).
> >
> > <details>
> > <summary><strong>📄 GET /api/v1/students/ping</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/`
> >
> > ```java
> > @Hidden
> > @GetMapping("/ping")
> > public String ping() {
> >     return "pong";
> > }
> > ```
> >
> > </details>
> >
> > > `@Hidden` también se puede poner **sobre la clase** para ocultar un controlador entero. En el Paso 11 se comprueba que la ruta responde `200` y que `ping` **no aparece** en el contrato.
> > >
> > > ⚠️ **No es seguridad.** Ocultar una ruta de la documentación **no la protege**: cualquiera que conozca la URL puede llamarla.
> > >
> > > 💡 **Beneficio:** el contrato público muestra solo lo que se quiere ofrecer.
> >
> > ✅ **Punto de control 7:** una operación tachada y otra invisible, las dos funcionando.
>
> </details>
>

</details>

---

<details>
<summary><strong>🔐 Paso 8 — Esquema de seguridad (botón Authorize)</strong></summary>

> <details>
> <summary><strong>Paso 8.1 · Declarar el esquema y exigirlo en una operación</strong></summary>
>
> > Dos piezas: **declarar** que la API usa un token *Bearer* (en `OpenApiConfig`) y **marcar** qué operaciones lo exigen. En la academia, **crear un curso** es la operación protegida.
> >
> > <details>
> > <summary><strong>📄 OpenApiConfig.java (agregar al bean OpenAPI)</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/config/OpenApiConfig.java`
> >
> > ```java
> > .components(new Components().addSecuritySchemes("bearerAuth",
> >         new SecurityScheme()
> >                 .type(SecurityScheme.Type.HTTP)
> >                 .scheme("bearer")
> >                 .bearerFormat("JWT")));
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 POST /api/v1/courses</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/`
> >
> > ```java
> > @Operation(operationId = "createCourse", summary = "Crear un curso",
> >         description = "Requiere autenticación (esquema `bearerAuth`). El precio debe ser mayor que cero y el cupo, al menos 1.",
> >         security = @SecurityRequirement(name = "bearerAuth"))
> > @PostMapping
> > public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) { ... }
> > ```
> >
> > </details>
> >
> > > | Pieza | Dónde se ve |
> > > |---|---|
> > > | `SecurityScheme` `HTTP` / `bearer` / `JWT` | El botón **Authorize** 🔓 arriba a la derecha |
> > > | `@SecurityRequirement(name = "bearerAuth")` | Un **candado** 🔒 en `POST /api/v1/courses` |
> > >
> > > Al pulsar **Authorize** se escribe el token; Swagger UI lo envía en la cabecera `Authorization: Bearer …` de **todas** las operaciones con candado.
> > >
> > > ⚠️ **Esto solo documenta.** La aplicación **no valida** ningún token (no hay Spring Security): crear un curso responde `201` con o sin token (se comprueba en el Paso 11).
> > >
> > > 💡 **Beneficio:** el contrato dice **cómo autenticarse**; cuando se agregue la seguridad, Swagger UI ya sabrá mandar el token.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 8.2 · Ver el resultado</strong></summary>
>
> > **Resultado real:** en `/api-docs`, `components.securitySchemes` contiene `bearerAuth` (`http`, `bearer`, `JWT`) y **solo** `POST /api/v1/courses` trae `security`.
> >
> > ✅ **Punto de control 8:** botón *Authorize* visible y un candado solo en la creación de cursos.
>
> </details>
>

</details>

---

<details>
<summary><strong>🗂️ Paso 9 — Grupos: varios contratos en uno</strong></summary>

> <details>
> <summary><strong>Paso 9.1 · <code>GroupedOpenApi</code> e <code>InternalController</code></strong></summary>
>
> > Una API grande se divide en **grupos**. Cada grupo tiene **su propio contrato** y aparece en el selector **Select a definition**.
> >
> > Se agrega un controlador de apoyo (`/internal`) y los dos grupos. Este es el `OpenApiConfig` **completo y final**:
> >
> > <details>
> > <summary><strong>📄 OpenApiConfig.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/config/OpenApiConfig.java`
> >
> > ```java
> > package com.academy.presentation.config;
> >
> > import io.swagger.v3.oas.models.Components;
> > import io.swagger.v3.oas.models.OpenAPI;
> > import io.swagger.v3.oas.models.info.Contact;
> > import io.swagger.v3.oas.models.info.Info;
> > import io.swagger.v3.oas.models.info.License;
> > import io.swagger.v3.oas.models.security.SecurityScheme;
> > import io.swagger.v3.oas.models.servers.Server;
> > import org.springdoc.core.models.GroupedOpenApi;
> > import org.springframework.context.annotation.Bean;
> > import org.springframework.context.annotation.Configuration;
> >
> > import java.util.List;
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
> >                 .components(new Components().addSecuritySchemes("bearerAuth",
> >                         new SecurityScheme()
> >                                 .type(SecurityScheme.Type.HTTP)
> >                                 .scheme("bearer")
> >                                 .bearerFormat("JWT")));
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
> > <details>
> > <summary><strong>📄 InternalController.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/InternalController.java`
> >
> > ```java
> > package com.academy.presentation.controller;
> >
> > import com.academy.business.service.CourseService;
> > import com.academy.business.service.ServiceStatus;
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
> >             if (students.create(sample).status() == ServiceStatus.OK) {
> >                 createdStudents++;
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
> > > | Pieza | Qué hace |
> > > |---|---|
> > > | `GroupedOpenApi.builder().group("academy")` | Nombre técnico del grupo (`/api-docs/academy`) |
> > > | `.displayName("Academy")` | Nombre que se ve en el selector |
> > > | `.pathsToMatch("/api/v1/**")` | Qué rutas entran en el grupo |
> > > | `InternalController` | `GET /internal/health` y `POST /internal/seed` (carga tres estudiantes y dos cursos) |
> > >
> > > **Resultado real:**
> > >
> > > | Grupo | Rutas | Modelos |
> > > |---|---|---|
> > > | `academy` | Las ocho rutas de `/api/v1/…` (estudiantes, cursos y matrículas) | Los nueve |
> > > | `internal` | `/internal/seed`, `/internal/health` | Ninguno |
> > >
> > > 💡 **Beneficio:** cada audiencia ve **solo su parte**. El contrato público no mezcla operaciones de mantenimiento.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 9.2 · Ver el resultado</strong></summary>
>
> > Arriba a la derecha aparece **Select a definition** con `Academy` e `Internal`. Al cambiar de grupo la lista cambia. Los contratos de cada grupo se piden en `/api-docs/academy` y `/api-docs/internal` (Paso 11).
> >
> > ✅ **Punto de control 9:** el selector muestra los dos grupos.
>
> </details>
>

</details>

---

<details>
<summary><strong>⚙️ Paso 10 — Configurar la interfaz, exportar el contrato y apagarlo</strong></summary>

> <details>
> <summary><strong>Paso 10.1 · Propiedades de springdoc</strong></summary>
>
> > Todo lo que no se hace con código se ajusta en `application.properties` (debajo de lo que ya tiene: MySQL, JPA, paginación y errores):
> >
> > <details>
> > <summary><strong>📄 application.properties (agregar)</strong></summary>
> >
> > **Ruta:** `src/main/resources/application.properties`
> >
> > ```properties
> > # Swagger (springdoc)
> > springdoc.api-docs.path=/api-docs
> > springdoc.swagger-ui.path=/swagger-ui.html
> > springdoc.swagger-ui.urls-primary-name=Academy
> > springdoc.swagger-ui.deep-linking=true
> > springdoc.swagger-ui.operations-sorter=method
> > springdoc.swagger-ui.tags-sorter=alpha
> > springdoc.swagger-ui.display-request-duration=true
> > springdoc.swagger-ui.doc-expansion=list
> > springdoc.swagger-ui.try-it-out-enabled=true
> > springdoc.default-produces-media-type=application/json
> > springdoc.default-consumes-media-type=application/json
> > ```
> >
> > </details>
> >
> > > | Propiedad | Para qué sirve |
> > > |---|---|
> > > | `springdoc.api-docs.path` | Dirección del contrato (por defecto `/v3/api-docs`) |
> > > | `springdoc.swagger-ui.path` | Dirección de la pantalla |
> > > | `springdoc.swagger-ui.urls-primary-name` | Grupo que se muestra **primero** (sin ella, el primero por orden alfabético: `Academy`) |
> > > | `springdoc.swagger-ui.deep-linking` | Permite enlazar a una operación concreta |
> > > | `springdoc.swagger-ui.operations-sorter` | Orden de las operaciones: `alpha` (por ruta) o `method` (por método HTTP) |
> > > | `springdoc.swagger-ui.tags-sorter` | Orden de las etiquetas |
> > > | `springdoc.swagger-ui.display-request-duration` | Muestra cuánto tardó cada petición de prueba |
> > > | `springdoc.swagger-ui.doc-expansion` | `none` (todo cerrado), `list` (operaciones visibles) o `full` (todo abierto) |
> > > | `springdoc.swagger-ui.try-it-out-enabled` | Abre las operaciones ya en modo edición |
> > > | `springdoc.default-produces-media-type` · `default-consumes-media-type` | Formato por defecto de entrada y salida |
> > >
> > > 💡 **Beneficio:** la presentación se ajusta **sin tocar el código** de los controladores.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 10.2 · Exportar el contrato</strong></summary>
>
> > El contrato es un archivo: se puede **descargar** y entregar a otros equipos o importar en otras herramientas.
> >
> > ```bash
> > curl http://localhost:8080/api-docs          -o openapi.json    # JSON
> > curl http://localhost:8080/api-docs.yaml     -o openapi.yaml    # YAML
> > curl http://localhost:8080/api-docs/academy  -o academy.json    # solo un grupo
> > ```
> >
> > > | Quién usa el archivo | Para qué |
> > > |---|---|
> > > | **Postman / Insomnia** | Importar la colección completa |
> > > | **Generadores de clientes** (OpenAPI Generator) | Crear el cliente en TypeScript, Java, etc. (con los `operationId` del Paso 3.2 como nombres de método) |
> > > | **Revisores / otros equipos** | Revisar el contrato **sin** ejecutar la aplicación |
> > >
> > > 💡 **Beneficio:** el contrato es un **entregable** versionable, no solo una pantalla.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 10.3 · Apagar Swagger donde no debe estar</strong></summary>
>
> > En producción, la documentación pública **revela la API completa**. Se desactiva con dos propiedades, mejor en un **perfil**:
> >
> > <details>
> > <summary><strong>📄 application-prod.properties</strong></summary>
> >
> > **Ruta:** `src/main/resources/application-prod.properties`
> >
> > ```properties
> > springdoc.api-docs.enabled=false
> > springdoc.swagger-ui.enabled=false
> > ```
> >
> > </details>
> >
> > > Se arranca con `./mvnw spring-boot:run -Dspring-boot.run.profiles=prod`. **Resultado real:** `/api-docs` → `404`, `/swagger-ui.html` → `404` y `/api/v1/students` → `200` (la API **sigue funcionando**).
> > >
> > > 💡 **Beneficio:** la misma aplicación documenta en desarrollo y se calla en producción, sin cambiar código.
> > >
> > > ⚠️ **Y si se necesita en producción**, se protege con autenticación; apagarlo es lo más simple y seguro por defecto.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 10.4 · El contrato completo y los controladores finales</strong></summary>
>
> > Con todo activo, **resultado real** (lista de operaciones de `/api-docs`):
> >
> > | Método | Ruta | `operationId` | Marcas | Respuestas |
> > |---|---|---|---|---|
> > | `GET` | `/api/v1/students` | `listStudents` | | `200` |
> > | `POST` | `/api/v1/students` | `createStudent` | | `201` · `400` · `409` |
> > | `GET` | `/api/v1/students/{id}` | `getStudent` | | `200` · `404` |
> > | `GET` | `/api/v1/students/by-email` | `getStudentByEmail` | ~~obsoleta~~ | `200` · `404` |
> > | `GET` | `/api/v1/courses` | `listCourses` | | `200` |
> > | `POST` | `/api/v1/courses` | `createCourse` | 🔒 seguridad | `201` · `400` |
> > | `GET` | `/api/v1/courses/{id}` | `getCourse` | | `200` · `404` |
> > | `GET` | `/api/v1/courses/{id}/enrolled` | `countCourseEnrolled` | | `200` · `404` |
> > | `GET` | `/api/v1/enrollments/{id}` | `getEnrollment` | | `200` · `404` |
> > | `POST` | `/api/v1/enrollments` | `enroll` | | `201` · `400` · `404` · `409` |
> > | `GET` | `/internal/health` | `health` | | `200` |
> > | `POST` | `/internal/seed` | `seed` | | `200` |
> >
> > La ruta `/api/v1/students/ping` **no aparece**. Los controladores **finales**, con todas las anotaciones juntas:
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
> > import org.springframework.http.HttpStatus;
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
> >     @GetMapping
> >     public PageResponse<StudentResponse> findAll(@ParameterObject Pageable pageable) {
> >         return StudentMapper.toPageResponse(service.findAll(pageable));
> >     }
> >
> >     @Operation(operationId = "getStudent", summary = "Buscar un estudiante por id")
> >     @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
> >     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
> >     @GetMapping("/{id}")
> >     public ResponseEntity<StudentResponse> findById(
> >             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
> >         return service.findById(id)
> >                 .map(StudentMapper::toResponse)
> >                 .map(ResponseEntity::ok)
> >                 .orElseGet(() -> ResponseEntity.notFound().build());
> >     }
> >
> >     @Operation(operationId = "getStudentByEmail", summary = "Buscar un estudiante por correo",
> >             description = "Obsoleto: usar `GET /api/v1/students/{id}`.",
> >             deprecated = true)
> >     @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
> >     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese correo", content = @Content)
> >     @GetMapping("/by-email")
> >     public ResponseEntity<StudentResponse> findByEmail(
> >             @Parameter(description = "Correo exacto", example = "ana@mail.com") @RequestParam String email) {
> >         return service.findByEmail(email)
> >                 .map(StudentMapper::toResponse)
> >                 .map(ResponseEntity::ok)
> >                 .orElseGet(() -> ResponseEntity.notFound().build());
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
> >     @ApiResponse(responseCode = "400", description = "Datos inválidos (Bean Validation)", content = @Content)
> >     @ApiResponse(responseCode = "409", description = "El correo ya está registrado", content = @Content)
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
> > import com.academy.business.service.ServiceResult;
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
> >     @GetMapping
> >     public PageResponse<CourseResponse> findAll(@ParameterObject Pageable pageable) {
> >         return CourseMapper.toPageResponse(service.findAll(pageable));
> >     }
> >
> >     @Operation(operationId = "getCourse", summary = "Buscar un curso por id")
> >     @ApiResponse(responseCode = "200", description = "Curso encontrado")
> >     @ApiResponse(responseCode = "404", description = "No existe un curso con ese id", content = @Content)
> >     @GetMapping("/{id}")
> >     public ResponseEntity<CourseResponse> findById(
> >             @Parameter(description = "Identificador del curso", example = "1") @PathVariable Long id) {
> >         return service.findById(id)
> >                 .map(CourseMapper::toResponse)
> >                 .map(ResponseEntity::ok)
> >                 .orElseGet(() -> ResponseEntity.notFound().build());
> >     }
> >
> >     @Operation(operationId = "createCourse", summary = "Crear un curso",
> >             description = "Requiere autenticación (esquema `bearerAuth`). El precio debe ser mayor que cero y el cupo, al menos 1.",
> >             security = @SecurityRequirement(name = "bearerAuth"))
> >     @ApiResponse(responseCode = "201", description = "Curso creado",
> >             headers = @Header(name = "Location", description = "URL del curso creado",
> >                     schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/courses/1")))
> >     @ApiResponse(responseCode = "400", description = "Datos inválidos (Bean Validation)", content = @Content)
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
> >     @ApiResponse(responseCode = "404", description = "No existe un curso con ese id", content = @Content)
> >     @GetMapping("/{id}/enrolled")
> >     public ResponseEntity<EnrolledResponse> countEnrolled(
> >             @Parameter(description = "Identificador del curso", example = "1") @PathVariable Long id) {
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
> > import io.swagger.v3.oas.annotations.Operation;
> > import io.swagger.v3.oas.annotations.Parameter;
> > import io.swagger.v3.oas.annotations.headers.Header;
> > import io.swagger.v3.oas.annotations.media.Content;
> > import io.swagger.v3.oas.annotations.media.ExampleObject;
> > import io.swagger.v3.oas.annotations.media.Schema;
> > import io.swagger.v3.oas.annotations.responses.ApiResponse;
> > import io.swagger.v3.oas.annotations.tags.Tag;
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
> >     @ApiResponse(responseCode = "404", description = "No existe una matrícula con ese id", content = @Content)
> >     @GetMapping("/{id}")
> >     public ResponseEntity<EnrollmentResponse> findById(
> >             @Parameter(description = "Identificador de la matrícula", example = "1") @PathVariable Long id) {
> >         return service.findById(id)
> >                 .map(EnrollmentMapper::toResponse)
> >                 .map(ResponseEntity::ok)
> >                 .orElseGet(() -> ResponseEntity.notFound().build());
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
> >     @ApiResponse(responseCode = "400", description = "Falta un id (Bean Validation)", content = @Content)
> >     @ApiResponse(responseCode = "404", description = "El estudiante o el curso no existen", content = @Content)
> >     @ApiResponse(responseCode = "409", description = "Matrícula repetida o curso sin cupo", content = @Content)
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
> > ✅ **Punto de control 10:** el contrato coincide con la tabla.
>
> </details>
>

</details>

---

<details>
<summary><strong>🧪 Paso 11 — Probar la documentación</strong></summary>

> <details>
> <summary><strong>Paso 11.1 · Las pruebas con <code>curl</code></strong></summary>
>
> > Se arranca la aplicación con MySQL en marcha (`./mvnw spring-boot:run`) y se ejecuta cada comando en otra terminal. Debajo de cada uno está la **respuesta real**.
> >
> > > 💡 Los cuerpos JSON se muestran con sangría para leerlos mejor. Los últimos tres comandos se ejecutaron con el perfil `prod`.
> >
> > <details>
> > <summary><strong>1. Swagger UI redirige a su página → <code>302</code></strong></summary>
> >
> > `/swagger-ui.html` no es la pantalla: redirige (`302`) a `/swagger-ui/index.html`.
> >
> > ```bash
> > curl -i http://localhost:8080/swagger-ui.html
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 302
> > Location: /swagger-ui/index.html
> > Content-Length: 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>2. La ruta por defecto del contrato ya no existe → <code>404</code></strong></summary>
> >
> > Se cambió `springdoc.api-docs.path` a `/api-docs`, así que la ruta por defecto responde `404`.
> >
> > ```bash
> > curl -i http://localhost:8080/v3/api-docs
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-01T20:43:30.574Z",
> >   "status": 404,
> >   "error": "Not Found",
> >   "path": "/v3/api-docs"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>3. La configuración de la pantalla: los grupos → <code>200</code></strong></summary>
> >
> > `urls` lista los grupos del selector **Select a definition**; `urls.primaryName` es el que se muestra primero.
> >
> > ```bash
> > curl -s http://localhost:8080/api-docs/swagger-config
> > ```
> >
> > Respuesta:
> >
> > ```text
> > {
> >   "configUrl": "/api-docs/swagger-config",
> >   "deepLinking": true,
> >   "displayRequestDuration": true,
> >   "docExpansion": "list",
> >   "oauth2RedirectUrl": "http://localhost:8080/swagger-ui/oauth2-redirect.html",
> >   "operationsSorter": "method",
> >   "tagsSorter": "alpha",
> >   "tryItOutEnabled": true,
> >   "urls": [
> >     {
> >       "url": "/api-docs/academy",
> >       "name": "Academy"
> >     },
> >     {
> >       "url": "/api-docs/internal",
> >       "name": "Internal"
> >     }
> >   ],
> >   "urls.primaryName": "Academy",
> >   "validatorUrl": ""
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>4. El contrato de un solo grupo → <code>200</code></strong></summary>
> >
> > Cada grupo tiene su propio documento OpenAPI: este solo trae `/internal/seed` y `/internal/health`.
> >
> > ```bash
> > curl -s http://localhost:8080/api-docs/internal
> > ```
> >
> > Respuesta:
> >
> > ```text
> > {
> >   "openapi": "3.1.0",
> >   "info": {
> >     "title": "Academy API",
> >     "description": "Estudiantes, cursos y matrículas con DTO, Spring Boot, JPA y MySQL, documentado con OpenAPI.",
> >     "contact": {
> >       "name": "Equipo Academy",
> >       "email": "academy@mail.com"
> >     },
> >     "license": {
> >       "name": "Apache 2.0",
> >       "url": "https://www.apache.org/licenses/LICENSE-2.0"
> >     },
> >     "version": "1.0.0"
> >   },
> >   "servers": [
> >     {
> >       "url": "http://localhost:8080",
> >       "description": "Local"
> >     }
> >   ],
> >   "tags": [
> >     {
> >       "name": "Internal",
> >       "description": "Operaciones de apoyo para el equipo (no son parte de la API pública)"
> >     }
> >   ],
> >   "paths": {
> >     "/internal/seed": {
> >       "post": {
> >         "tags": [
> >           "Internal"
> >         ],
> >         "summary": "Cargar datos de ejemplo",
> >         "description": "Crea tres estudiantes (si sus correos no existen) y dos cursos (solo si no hay ninguno).",
> >         "operationId": "seed",
> >         "responses": {
> >           "200": {
> >             "description": "OK",
> >             "content": {
> >               "application/json": {
> >                 "schema": {
> >                   "type": "object",
> >                   "additionalProperties": {
> >                     "type": "integer",
> >                     "format": "int32"
> >                   }
> >                 }
> >               }
> >             }
> >           }
> >         }
> >       }
> >     },
> >     "/internal/health": {
> >       "get": {
> >         "tags": [
> >           "Internal"
> >         ],
> >         "summary": "Estado del servicio",
> >         "operationId": "health",
> >         "responses": {
> >           "200": {
> >             "description": "OK",
> >             "content": {
> >               "application/json": {
> >                 "schema": {
> >                   "type": "object",
> >                   "additionalProperties": {
> >                     "type": "string"
> >                   }
> >                 }
> >               }
> >             }
> >           }
> >         }
> >       }
> >     }
> >   },
> >   "components": {
> >     "securitySchemes": {
> >       "bearerAuth": {
> >         "type": "http",
> >         "scheme": "bearer",
> >         "bearerFormat": "JWT"
> >       }
> >     }
> >   }
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>5. El contrato en YAML (primeras líneas) → <code>200</code></strong></summary>
> >
> > El mismo contrato en YAML, listo para importarlo en otras herramientas.
> >
> > ```bash
> > curl -s http://localhost:8080/api-docs.yaml | head -12
> > ```
> >
> > Respuesta:
> >
> > ```text
> > openapi: 3.1.0
> > info:
> >   title: Academy API
> >   description: "Estudiantes, cursos y matrículas con DTO, Spring Boot, JPA y MySQL,\
> >     \ documentado con OpenAPI."
> >   contact:
> >     name: Equipo Academy
> >     email: academy@mail.com
> >   license:
> >     name: Apache 2.0
> >     url: https://www.apache.org/licenses/LICENSE-2.0
> >   version: 1.0.0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>6. Cargar datos de ejemplo → <code>200</code></strong></summary>
> >
> > Crea tres estudiantes y dos cursos.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/internal/seed
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "students": 3,
> >   "courses": 2
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>7. Cargar datos de ejemplo otra vez → <code>200</code></strong></summary>
> >
> > Los correos ya existen y ya hay cursos, así que no crea nada.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/internal/seed
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {
> >   "students": 0,
> >   "courses": 0
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>8. Una operación oculta sigue funcionando → <code>200</code></strong></summary>
> >
> > `@Hidden` no desactiva la ruta: solo la quita de la documentación.
> >
> > ```bash
> > curl -i http://localhost:8080/api/v1/students/ping
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: text/plain;charset=UTF-8
> > Content-Length: 4
> >
> > pong
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>9. …pero no aparece en el contrato</strong></summary>
> >
> > Cuenta cuántas veces aparece `ping` en el contrato: **cero**.
> >
> > ```bash
> > curl -s http://localhost:8080/api-docs | grep -c ping
> > ```
> >
> > Respuesta:
> >
> > ```text
> > 0
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>10. Una operación obsoleta sigue funcionando → <code>200</code></strong></summary>
> >
> > `deprecated = true` solo avisa: la operación responde igual.
> >
> > ```bash
> > curl -i "http://localhost:8080/api/v1/students/by-email?email=ana@mail.com"
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
> > <summary><strong>11. La seguridad solo se documenta: crear un curso sin token → <code>201</code></strong></summary>
> >
> > `createCourse` declara `bearerAuth`, pero la aplicación no valida ningún token: responde `201`.
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/courses \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"Docker básico","price":90000,"maxCapacity":15}'
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 201
> > Location: http://localhost:8080/api/v1/courses/3
> > Content-Type: application/json
> >
> > {
> >   "id": 3,
> >   "name": "Docker básico",
> >   "price": 90000.0,
> >   "maxCapacity": 15
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>12. Un curso inválido sigue dando <code>400</code></strong></summary>
> >
> > `@Positive` y `@Min(1)` rechazan el cuerpo, aunque `@Positive` no se vea en el contrato.
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
> >   "timestamp": "2026-11-01T20:43:31.098Z",
> >   "status": 400,
> >   "error": "Bad Request",
> >   "path": "/api/v1/courses"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>13. Con el perfil <code>prod</code>: el contrato se apaga → <code>404</code></strong></summary>
> >
> > La aplicación se arranca con `-Dspring-boot.run.profiles=prod` (o `-Dspring.profiles.active=prod` con el `.jar`).
> >
> > ```bash
> > curl -i http://localhost:8080/api-docs
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-01T20:43:40.546Z",
> >   "status": 404,
> >   "error": "Not Found",
> >   "path": "/api-docs"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>14. Con el perfil <code>prod</code>: la pantalla se apaga → <code>404</code></strong></summary>
> >
> > Mismo perfil: `springdoc.swagger-ui.enabled=false`.
> >
> > ```bash
> > curl -i http://localhost:8080/swagger-ui.html
> > ```
> >
> > Respuesta:
> >
> > ```text
> > HTTP/1.1 404
> > Content-Type: application/json
> >
> > {
> >   "timestamp": "2026-11-01T20:43:40.567Z",
> >   "status": 404,
> >   "error": "Not Found",
> >   "path": "/swagger-ui.html"
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>15. Con el perfil <code>prod</code>: la API sigue funcionando → <code>200</code></strong></summary>
> >
> > Apagar la documentación no toca la API.
> >
> > ```bash
> > curl -i "http://localhost:8080/api/v1/students?size=2"
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
> >   "size": 2,
> >   "totalElements": 3,
> >   "totalPages": 2
> > }
> > ```
> >
> > </details>
> >
> > > 💡 **Beneficio de este paso:** se comprueba que **cada** funcionalidad de Swagger hace lo que se dijo: redirección, rutas configuradas, grupos, YAML, operaciones ocultas y obsoletas, seguridad solo documentada y apagado en producción.
> > >
> > > ✅ **Punto de control 11:** las respuestas coinciden con las que se muestran bajo cada comando.
>
> </details>
>

</details>

---

<details>
<summary><strong>🗺️ Mapa: funcionalidad de Swagger → cómo se usa</strong></summary>

> | Funcionalidad | Cómo se activa | Paso |
> |---|---|:-:|
> | Documentación automática | Dependencia `springdoc-openapi-starter-webmvc-ui` | 1 |
> | Probar desde el navegador | **Try it out** de Swagger UI | 1 |
> | Nombre, versión, contacto, licencia, servidores | Bean `OpenAPI` | 2 |
> | Agrupar operaciones por etiqueta | `@Tag` en cada controlador | 3 |
> | Resumen, descripción y nombre único de operación | `@Operation` (`summary`, `description`, `operationId`) | 3 |
> | Respuestas posibles y la cabecera `Location` | `@ApiResponse` (+ `@Header`) | 3 |
> | Descripción y ejemplo de un parámetro | `@Parameter` | 4 |
> | Desplegar `Pageable` en parámetros | `@ParameterObject` | 4 |
> | Descripción y ejemplo de un campo del DTO | `@Schema` en los `record` | 5 |
> | Reglas de validación en el contrato | **Automático** desde Bean Validation (con límites) | 5 |
> | Varios ejemplos del cuerpo | `@RequestBody` de Swagger + `@ExampleObject` | 6 |
> | Operación obsoleta | `@Operation(deprecated = true)` | 7 |
> | Ocultar operación o controlador | `@Hidden` | 7 |
> | Autenticación (*Authorize*) | `SecurityScheme` + `@SecurityRequirement` | 8 |
> | Varios contratos (grupos) | `GroupedOpenApi` | 9 |
> | Orden, expansión, rutas, grupo inicial | Propiedades `springdoc.*` | 10 |
> | Exportar el contrato (JSON/YAML) | `/api-docs`, `/api-docs.yaml`, `/api-docs/{grupo}` | 10 |
> | Apagar la documentación | `springdoc.api-docs.enabled` y `swagger-ui.enabled` | 10 |
>
> > 🔎 **Lectura clave.** Con varios controladores, lo que más aporta Swagger es **orden**: etiquetas, `operationId` únicos y grupos. Y gracias a los DTO, el contrato no muestra entidades ni campos internos.

</details>

<details>
<summary><strong>🏁 Cierre — preguntas de síntesis</strong></summary>

> 1. ¿Por qué aparecían `findAll_1` y `findById_2`, y cómo se arreglan? *(Los tres controladores repiten nombres de método; `operationId` en `@Operation` da un nombre único a cada operación.)*
> 2. ¿Por qué `@Schema` va en los DTO y no en las entidades? *(La entidad es la tabla; el DTO es el contrato. Así renombrar una columna no cambia la API.)*
> 3. ¿Qué reglas de validación llegan al contrato y cuáles no? *(`@NotBlank`, `@Size`, `@Min`, `@Positive` y `@Email` llegan (`required`, `minLength`/`maxLength`, `minimum`, `exclusiveMinimum`, `format`); los tipos primitivos no se marcan obligatorios. Se dicen en la descripción o se agregan con `@Schema`.)*
> 4. ¿Qué distingue el `404` del `409` en la matrícula, y dónde se documenta? *(«No existe» frente a «ya existe o no hay cupo»; en las `@ApiResponse` de `enroll`.)*
> 5. ¿`@Hidden` protege una ruta? *(No: solo la quita de la documentación; `ping` sigue respondiendo.)*
> 6. ¿Qué hace el botón *Authorize* aquí? *(Guarda un token y lo envía; nadie lo valida, así que crear un curso sin token responde `201`.)*
> 7. ¿Por qué se apaga Swagger en producción? *(Revela la API completa; se desactiva con propiedades y un perfil.)*

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Solución |
> |---|---|---|
> | `404` en `/swagger-ui.html` | Falta la dependencia, o `springdoc.swagger-ui.enabled=false` | Revisar el `pom.xml` y las propiedades |
> | `404` en `/v3/api-docs` | Se cambió `springdoc.api-docs.path` | Usar la ruta configurada (`/api-docs`) |
> | `NoSuchMethodError` o fallos al arrancar con springdoc | Versión de springdoc incompatible con Spring Boot (1.x en Boot 3) | Usar `springdoc-openapi-starter-webmvc-ui` **2.x** con Boot 3 |
> | `Failed to load API definition` en la pantalla | El contrato no se genera (error en una anotación o en un controlador) | Abrir `/api-docs` directamente y leer el mensaje |
> | `operationId` con sufijos (`findAll_1`) | Métodos con el mismo nombre en varios controladores | `@Operation(operationId = "…")` |
> | El `Pageable` se muestra como un solo cuadro JSON | Falta `@ParameterObject` | Agregarlo |
> | El `409` aparece con un modelo de respuesta inexistente | Falta `content = @Content` en esa `@ApiResponse` | Agregarlo |
> | Dos `@RequestBody` en el mismo parámetro no compilan | Se importaron las dos | Usar el nombre completo de la de Swagger |
> | Los `@Schema` no aparecen en el contrato | Se pusieron en la entidad, que la API no expone | Ponerlos en los `record` de `presentation.dto` |
> | Los cambios de documentación no se ven | Caché del navegador o no se reinició la aplicación | Reiniciar y recargar con `Ctrl+F5` |
> | `Communications link failure` al arrancar | MySQL no está arrancado o el puerto es otro | `docker ps` y revisar la URL |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **Obligatorio de verdad.** Agregar `requiredMode = REQUIRED` al `@Schema` de `price` y de `maxCapacity` (son primitivos) y comprobar que entran en `required` en `/api-docs`.
> 2. **Campos obligatorios.** Agregar `requiredMode = REQUIRED` al `@Schema` de `price` y de `maxCapacity`.
> 3. **Caso de error con ejemplo.** Agregar un ejemplo de `StudentRequest` con nombre vacío para provocar el `400` desde Swagger UI.
> 4. **El tope de 50 en el contrato.** Buscar cómo declarar el máximo de `size` y comprobar que aparece en `/api-docs`.
> 5. **Un grupo por recurso.** Separar `Courses` y `Students` en grupos distintos con `pathsToMatch`.
> 6. **Un segundo servidor.** Agregar `https://api.academy.com` como servidor de producción y probar el selector.
> 7. **Seguridad real.** Agregar Spring Security con un token fijo y comprobar que *Authorize* ahora **sí** cambia el resultado de `POST /courses`.
> 8. **Generar un cliente.** Con el YAML exportado, generar un cliente con OpenAPI Generator y ver los nombres de método (`listStudents`, `enroll`…).
> 9. **Modelo de error.** Cuando se estudien las excepciones (Módulo 05), crear un `ErrorResponse` y documentarlo en `400`, `404` y `409`.

</details>

<details>
<summary><strong>🏷️ Repaso de anotaciones de Swagger (OpenAPI)</strong></summary>

> | Anotación | Dónde se usa | Para qué sirve |
> |---|---|---|
> | `@Tag` | Clase del controlador | Nombre y descripción del grupo de operaciones |
> | `@Operation` | Método | Resumen, descripción, `operationId`, `deprecated` y seguridad |
> | `@ApiResponse` | Método (repetible) | Una respuesta posible |
> | `@Header` | Dentro de `@ApiResponse` | Una cabecera de la respuesta (`Location`) |
> | `@Content` · `@Schema` · `@ExampleObject` | Dentro de las anteriores | Cuerpo vacío, modelo y ejemplos |
> | `@Parameter` | Parámetro del método | Descripción y ejemplo |
> | `@ParameterObject` | Parámetro `Pageable` u objeto | Despliega sus campos como parámetros |
> | `@io.swagger…RequestBody` | Método | Documenta el cuerpo (con ejemplos) |
> | `@Schema` | Clase y componentes de los `record` (DTO) | Descripción y ejemplo |
> | `@Hidden` | Método o clase | Quita la operación del contrato |
> | `@SecurityRequirement` | Dentro de `@Operation` | Exige un esquema de seguridad |
> | Beans `OpenAPI` y `GroupedOpenApi` | `presentation.config` | Información general, seguridad y grupos |
>
> ```text
> persistence  ──▶  (sin Swagger: las entidades no cambian)
> business     ──▶  (sin Swagger)
> presentation ──▶  @Tag · @Operation · @ApiResponse · @Parameter · @ParameterObject · @Hidden · @SecurityRequirement
>                   @Schema en los DTO · config (OpenAPI, GroupedOpenApi)
> ```

</details>

➡️ **Anterior:** [Guía `03-02-academica-dtos` del Módulo 03](../modulo-03-diseno-api-rest-con-dto/03-02-academica-dtos.md) · **Código completo:** [04-03-academica-swagger-codigo-completo.md](04-03-academica-swagger-codigo-completo.md)
