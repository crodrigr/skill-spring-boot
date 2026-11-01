# 📘 Guía paso a paso — CRUD de estudiantes con DTO documentado con Swagger (OpenAPI)

> 👩‍🏫 **Material docente.** Continúa la [guía del Módulo 05 — DTO y estructura de controladores](../modulo-05/01-guia-dto-y-estructura-de-controladores.md): el proyecto de partida **ya tiene** DTO, mapeador, `ServiceResult`, rutas con versión (`/api/v1`) y un controlador delgado. Sobre él se agregan, **una por una**, las funcionalidades de Swagger.
> Contiene código completo: no se entrega a los estudiantes antes de resolver el módulo anterior.
>
> 📦 **Proyecto terminado:** [`students-api/`](../codigo/students-api/) (código final, listo para ejecutar con `./mvnw spring-boot:run`).
>
> ✅ **Verificada.** El código se compiló y se ejecutó con Spring Boot 3.3, springdoc-openapi 2.6.0 y MySQL 8. Las respuestas, el contrato OpenAPI y la lista de operaciones que se muestran son salidas reales de la aplicación. Lo que se cuenta de la pantalla de Swagger UI se deduce de ese contrato.
>
> 🧭 **Idea central.** El código **ya describe** la API (rutas, métodos, tipos, validaciones). Swagger solo le **agrega lo que el código no puede decir**: para qué sirve cada operación, qué ejemplos usar y qué respuestas esperar. Y como ahora hay DTO, esa documentación vive en el **contrato** (los DTO y el controlador), **no en la entidad**.

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Documentar con **OpenAPI** (usando **springdoc**) la API de estudiantes del Módulo 05, hasta cubrir las funcionalidades principales de Swagger.
>
> Al terminar, el estudiante:
>
> - Distingue **OpenAPI** (el contrato), **Swagger UI** (la pantalla) y **springdoc** (la librería que los une con Spring).
> - Obtiene una documentación **automática** con solo agregar una dependencia, y ve qué le **falta**.
> - Describe la API con el bean `OpenAPI`, `@Tag`, `@Operation` y `@ApiResponse`.
> - Documenta parámetros (`@Parameter`, `@ParameterObject`) y los **DTO** (`@Schema`), y ve cómo **Bean Validation** pasa al contrato.
> - Agrega **ejemplos**, marca operaciones **obsoletas** y **oculta** otras.
> - Declara un **esquema de seguridad** (botón *Authorize*) y separa la API en **grupos**.
> - **Configura** la interfaz, **exporta** el contrato y lo **apaga** donde no debe estar.
> - Prueba cada operación con **Try it out**.

</details>

<details>
<summary><strong>📝 Introducción: qué es qué</strong></summary>

> <details>
> <summary><strong>🧩 Tres nombres que se confunden</strong></summary>
>
> | Nombre | Qué es | Dónde se ve |
> |---|---|---|
> | **OpenAPI** | La **especificación**: un documento JSON/YAML que describe la API | `/v3/api-docs` |
> | **Swagger UI** | La **pantalla web** que lee ese documento y permite probar la API | `/swagger-ui.html` |
> | **springdoc-openapi** | La **librería** que lee los controladores de Spring y genera el documento | La dependencia del `pom.xml` |
>
> ```text
> Controladores + DTO + anotaciones ─▶ springdoc ─▶ /v3/api-docs (OpenAPI) ─▶ Swagger UI
>                                                                      └──▶ Postman, generadores de clientes, etc.
> ```
>
> </details>
>
> <details>
> <summary><strong>🎓 El proyecto</strong></summary>
>
> La API de estudiantes del Módulo 05, con las tres capas (`presentation`, `business`, `persistence`), nombres en inglés y MySQL.
>
> | Operación | Ruta | Cuerpo | Respuestas |
> |---|---|---|---|
> | Listar con filtro y paginación | `GET /api/v1/students?name=&page=&size=&sort=` | | `200` |
> | Buscar por id | `GET /api/v1/students/{id}` | | `200` · `404` |
> | Crear | `POST /api/v1/students` | `StudentRequest` | `201` + `Location` · `400` · `409` |
> | Reemplazar | `PUT /api/v1/students/{id}` | `StudentRequest` | `200` · `400` · `404` · `409` |
> | Cambiar el estado | `PATCH /api/v1/students/{id}/status` | `StudentStatusRequest` | `200` · `400` · `404` |
> | Eliminar | `DELETE /api/v1/students/{id}` | | `204` · `404` |
> | Buscar por correo *(se marcará obsoleta)* | `GET /api/v1/students/by-email?email=` | | `200` · `404` |
>
> </details>
>
> <details>
> <summary><strong>💡 Qué cambia respecto a documentar una API sin DTO</strong></summary>
>
> | Aspecto | Sin DTO (entidad como contrato) | Con DTO (este proyecto) |
> |---|---|---|
> | Dónde va `@Schema` | En `StudentEntity`, mezclada con JPA | En los **DTO**; la entidad **no se toca** |
> | Nombre de los modelos | `StudentEntity` (un detalle interno) salvo que se renombre | `StudentRequest`, `StudentResponse`: ya son nombres de contrato |
> | El `id` en la petición de crear | Hay que marcarlo `READ_ONLY` | **No existe** en `StudentRequest`: no hay nada que ocultar |
> | Campos internos (`created_at`) | Aparecerían en el contrato | **Nunca** aparecen |
> | Renombrar una columna | Cambia el contrato | El contrato **no cambia** |
>
> </details>
>
> <details>
> <summary><strong>🛠️ Requerimientos y restricciones</strong></summary>
>
> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Java 17 o superior, Spring Boot 3.3 y Maven. |
> | **RNF-02** | MySQL 8 (en Docker o instalado); la base `academy` debe existir. |
> | **RNF-03** | Las anotaciones de Swagger van **solo en `presentation`**: controladores, DTO y configuración. `business` y `persistence` **no saben** que existe Swagger. |
> | **RNF-04** | Todos los nombres del código en **inglés**. Inyección por constructor. |
> | **RNF-05** | Duración: 1 h 30 min. |
>
> > 💡 **Alcance.** Quedan fuera: la autenticación real (el esquema de seguridad solo se **documenta**), el manejo de errores con excepciones y un formato de error uniforme (Módulo 7), y la generación de clientes a partir del contrato.
>
> </details>

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>

> | Paso | Minutos | Actividad |
> |---|---:|---|
> | 0 | 0 – 10 | Punto de partida: el proyecto del Módulo 05 |
> | 1 | 10 – 20 | Primera documentación automática (y qué le falta) |
> | 2 | 20 – 25 | Información general de la API |
> | 3 | 25 – 38 | Etiquetas, operaciones y respuestas |
> | 4 | 38 – 45 | Parámetros |
> | 5 | 45 – 58 | Los DTO (`@Schema`) y las validaciones |
> | 6 | 58 – 63 | Ejemplos |
> | 7 | 63 – 70 | Operaciones obsoletas y ocultas |
> | 8 | 70 – 75 | Esquema de seguridad |
> | 9 | 75 – 82 | Grupos |
> | 10 | 82 – 90 | Configurar la interfaz, exportar y apagar |
>
> > 💡 **Ritmo.** El Paso 0 es la base y puede entregarse ya hecho; el tiempo se dedica a los Pasos 1 a 10.

</details>

<details>
<summary><strong>🏗️ Arquitectura: el árbol de archivos (🆕 se agrega con Swagger · ✏️ cambia)</strong></summary>

> ```text
> 📁 students-api
> ├── ✏️ pom.xml                                 dependencia springdoc
> └── 📁 src/main
>     ├── 📁 java/com/academy
>     │   ├── 📄 AcademyApplication.java
>     │   ├── 📁 presentation
>     │   │   ├── 📁 config
>     │   │   │   └── 🆕 OpenApiConfig.java       información general, seguridad y grupos
>     │   │   ├── 📁 controller
>     │   │   │   ├── ✏️ StudentController.java   @Tag · @Operation · @ApiResponse · @Parameter · @Hidden
>     │   │   │   └── 🆕 InternalController.java  segundo grupo del contrato
>     │   │   ├── 📁 dto
>     │   │   │   ├── ✏️ StudentRequest.java      @Schema
>     │   │   │   ├── ✏️ StudentStatusRequest.java
>     │   │   │   ├── ✏️ StudentResponse.java
>     │   │   │   └── ✏️ PageResponse.java
>     │   │   └── 📁 mapper
>     │   │       └── StudentMapper.java
>     │   ├── 📁 business
>     │   │   └── 📁 service
>     │   │       ├── ✏️ StudentService.java      un método más: findByEmail
>     │   │       ├── ServiceResult.java
>     │   │       └── ServiceStatus.java
>     │   └── 📁 persistence
>     │       ├── 📁 entity
>     │       │   ├── StudentEntity.java          sin cambios (sigue sin Swagger)
>     │       │   └── StudentStatus.java
>     │       └── 📁 repository
>     │           └── StudentJpaRepository.java
>     └── 📁 resources
>         ├── ✏️ application.properties           propiedades de springdoc
>         └── 🆕 application-prod.properties      apaga la documentación
> ```
>
> | Capa | Qué lleva de Swagger |
> |---|---|
> | `presentation.controller` | `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`, `@Hidden` |
> | `presentation.dto` | `@Schema` (descripción y ejemplo de cada campo) |
> | `presentation.config` | Beans `OpenAPI` y `GroupedOpenApi` |
> | `business` | **Nada** |
> | `persistence` | **Nada**: la entidad no cambia |
>
> > ✅ **Lo que se gana con el Módulo 05.** En la versión sin DTO, `@Schema` había que ponerlo en la entidad y el contrato quedaba atado a la tabla. Ahora la entidad queda **intacta**.

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Punto de partida: el proyecto del Módulo 05</strong></summary>

> <details>
> <summary><strong>Paso 0.1 · MySQL y el proyecto</strong></summary>
>
> > **MySQL.** Hibernate crea las tablas, pero no la base `academy`. Con Docker:
> >
> > ```bash
> > docker run -d --name academy-mysql \
> >   -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academy \
> >   -p 3306:3306 mysql:8.0
> > ```
> >
> > **Proyecto.** No se crea uno nuevo: se parte del que se terminó en la [guía del Módulo 05](../modulo-05/01-guia-dto-y-estructura-de-controladores.md) (carpeta [`students-api-dto`](../codigo/students-api-dto/)).
> >
> > ```bash
> > cp -r students-api-dto students-api
> > ```
> >
> > En el `pom.xml` de la copia se cambia solo el nombre: `<artifactId>students-api</artifactId>`.
> >
> > > 💡 Quien no tenga el proyecto del Módulo 05 puede abrir [`students-api-dto/`](../codigo/students-api-dto/) y usarlo tal cual: es el punto de partida.
> >
> > > | Lo que **ya existe** | Dónde |
> > > |---|---|
> > > | `StudentRequest`, `StudentStatusRequest`, `StudentResponse`, `PageResponse<T>` | `presentation.dto` |
> > > | `StudentMapper` | `presentation.mapper` |
> > > | `ServiceResult` y `ServiceStatus` | `business.service` |
> > > | `StudentController` delgado, en `/api/v1/students` | `presentation.controller` |
> > > | `spring.data.web.pageable.max-page-size=50` | `application.properties` |
> > >
> > > **Swagger todavía no está**: es lo que se agrega ahora.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 0.2 · Comprobar que el CRUD funciona</strong></summary>
>
> > Se arranca la aplicación (`./mvnw spring-boot:run`) y se crea un estudiante:
> >
> > ```bash
> > curl -i -X POST http://localhost:8080/api/v1/students \
> >      -H "Content-Type: application/json" \
> >      -d '{"name":"Ana Torres","age":22,"email":"ana@mail.com","status":"ACTIVE"}'
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
> >   "email": "ana@mail.com",
> >   "status": "ACTIVE"
> > }
> > ```
> >
> > ✅ **Punto de control 0:** la API responde `201` con `Location`. Y todavía **no hay documentación**: `GET /swagger-ui.html` devuelve `404`.
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
> >     <version>2.6.0</version>
> > </dependency>
> > ```
> >
> > </details>
> >
> > > | Pieza | Qué trae |
> > > |---|---|
> > > | `springdoc-openapi-starter-webmvc-ui` | El generador del contrato **y** la interfaz de Swagger UI (para Spring MVC) |
> > > | `2.6.0` | La línea 2.x es la de **Spring Boot 3**; la 1.x era para Boot 2 |
> > >
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
> > > | Operaciones descubiertas | Las **seis** (`GET` lista, `POST`, `GET /{id}`, `PUT`, `PATCH /{id}/status`, `DELETE`) |
> > > | Agrupación | Una etiqueta con el nombre técnico `student-controller` |
> > > | Título y versión | `OpenAPI definition` · `v0` (valores por defecto) |
> > > | Servidor | `Generated server url` |
> > > | Respuestas | **Solo `200` en todas**, aun `POST` (que responde `201`) y `DELETE` (que responde `204`/`404`) |
> > > | Paginación | Dos parámetros: `name` y un único `pageable`, **obligatorio**, con el modelo `Pageable` |
> > > | Modelos | `StudentRequest`, `StudentResponse`, `StudentStatusRequest`, `PageResponseStudentResponse` y `Pageable` |
> > >
> > > **🎁 Lo que los DTO ya resuelven**
> > >
> > > El modelo `StudentRequest` que genera springdoc **sin ninguna anotación**:
> > >
> > > ```json
> > > {
> > >   "required": ["email", "name", "status"],
> > >   "type": "object",
> > >   "properties": {
> > >     "name":   { "type": "string", "minLength": 2, "maxLength": 100 },
> > >     "age":    { "type": "integer", "format": "int32", "minimum": 16, "maximum": 99 },
> > >     "email":  { "type": "string" },
> > >     "status": { "type": "string", "enum": ["ACTIVE", "INACTIVE"] }
> > >   }
> > > }
> > > ```
> > >
> > > - Las **validaciones** (`@NotBlank`, `@Size`, `@Min`, `@Max`, `@NotNull`) ya aparecen: springdoc las lee de Bean Validation.
> > > - **No hay `id` ni `createdAt`**: el DTO de entrada no los tiene, así que el contrato nunca los pide.
> > >
> > > **🕳️ Lo que falta**
> > >
> > > Lo que el compilador no puede saber queda **mal o vacío**: los códigos reales de `ResponseEntity` (`201`, `404`, `409`), la descripción de cada operación, los ejemplos, la paginación usable y el nombre y la versión de la API.
> > >
> > > 💡 **Beneficio de este paso:** la documentación **no se escribe a mano ni se desactualiza**: cambia cuando cambia el código.
> > >
> > > 🗣️ **Pregunta:** ¿qué falta? *(El nombre y la versión de la API, una descripción de cada operación, los códigos de respuesta verdaderos, ejemplos y una paginación usable: eso lo sabe una persona, no el compilador.)*
>
> </details>
>
>
> <details>
> <summary><strong>Paso 1.3 · Probar una operación con <em>Try it out</em></strong></summary>
>
> > En Swagger UI: abrir `POST /api/v1/students` → **Try it out** → editar el JSON → **Execute**. Se ve la petición `curl` equivalente, la URL, el código de respuesta, el cuerpo y las cabeceras.
> >
> > | Zona de la pantalla | Qué hace |
> > |---|---|
> > | Botón **Try it out** | Convierte los campos en editables |
> > | Botón **Execute** | Envía la petición real al servidor |
> > | **Curl** | Muestra el comando equivalente (útil para copiar) |
> > | **Server response** | Código, cuerpo y cabeceras reales |
> > | **Responses** | Las respuestas **documentadas** (hasta ahora, solo `200`) |
> >
> > ✅ **Punto de control 1:** `/swagger-ui.html` redirige (`302`) a `/swagger-ui/index.html` y `/v3/api-docs` devuelve JSON con las seis operaciones.
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
> >                         .description("CRUD de estudiantes con DTO, Spring Boot, JPA y MySQL, documentado con OpenAPI.")
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
> > > | `servers` | La lista **Servers** (útil para elegir *local*, *pruebas* o *producción* sin cambiar el código) |
> > >
> > > 💡 **Beneficio de este paso:** quien abre la documentación sabe **qué API es**, qué versión es y a quién preguntar.
> > >
> > > 🔎 **Alternativa.** La anotación `@OpenAPIDefinition(info = @Info(…))` sobre cualquier clase hace lo mismo. El bean es más cómodo cuando se agregan después la seguridad y otros elementos (Pasos 8 y 9).
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
<summary><strong>🧭 Paso 3 — Etiquetas, operaciones y respuestas</strong></summary>

> <details>
> <summary><strong>Paso 3.1 · <code>@Tag</code> en el controlador</strong></summary>
>
> > Un **tag** agrupa operaciones bajo un título. Sin él, springdoc usa el nombre de la clase (`student-controller`).
> >
> > <details>
> > <summary><strong>📄 StudentController.java</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @RestController
> > @RequestMapping("/api/v1/students")
> > @Tag(name = "Students", description = "Operaciones sobre estudiantes")
> > public class StudentController {
> >     ...
> > }
> > ```
> >
> > </details>
> >
> > > 💡 **Beneficio:** el grupo se llama **Students** y lleva una descripción, en lugar del nombre técnico de la clase.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 3.2 · <code>@Operation</code></strong></summary>
>
> > Cada método recibe un **resumen** (una línea, aparece en la lista) y una **descripción** (detalle, aparece al abrir la operación y admite Markdown).
> >
> > <details>
> > <summary><strong>📄 Listar, crear y cambiar el estado</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @Operation(summary = "Listar estudiantes",
> >         description = "Devuelve una página de estudiantes. Con `name` filtra por nombre (contiene, sin importar mayúsculas). El tamaño máximo de página es 50.")
> > @GetMapping
> > public PageResponse<StudentResponse> search(...) { ... }
> >
> > @Operation(summary = "Crear un estudiante",
> >         description = "El `id` lo genera la base y no se envía. Si el correo ya existe responde `409`.")
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(...) { ... }
> >
> > @Operation(summary = "Cambiar el estado de un estudiante",
> >         description = "Cambia solo el estado; el resto de los datos no se toca.")
> > @PatchMapping("/{id}/status")
> > public ResponseEntity<StudentResponse> changeStatus(...) { ... }
> > ```
> >
> > </details>
> >
> > > | Atributo | Dónde se ve |
> > > |---|---|
> > > | `summary` | Al lado de la ruta, en la lista de operaciones |
> > > | `description` | Dentro de la operación, bajo el título |
> > >
> > > 💡 **Beneficio:** el equipo de frontend entiende **para qué sirve** cada operación sin leer el código. Aquí se anota, por ejemplo, el **tope de 50** por página, que el contrato no puede deducir.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 3.3 · <code>@ApiResponse</code>: documentar las respuestas</strong></summary>
>
> > Springdoc detecta `200`, pero **no puede adivinar** que el `201` crea algo, que el `409` significa «correo repetido» ni que el `Location` viaja en la cabecera. Cada respuesta se declara con `@ApiResponse`. En las que **no devuelven cuerpo** se usa `content = @Content` (vacío) para que Swagger no muestre un modelo que no existe.
> >
> > <details>
> > <summary><strong>📄 Respuestas de POST /api/v1/students</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @ApiResponse(responseCode = "201", description = "Estudiante creado",
> >         headers = @Header(name = "Location", description = "URL del estudiante creado",
> >                 schema = @Schema(type = "string", example = "http://localhost:8080/api/v1/students/1")))
> > @ApiResponse(responseCode = "400", description = "Datos inválidos (Bean Validation)", content = @Content)
> > @ApiResponse(responseCode = "409", description = "El correo ya está registrado", content = @Content)
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) { ... }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 Respuestas de GET /api/v1/students/{id}</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @Operation(summary = "Buscar un estudiante por id")
> > @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
> > @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
> > @GetMapping("/{id}")
> > public ResponseEntity<StudentResponse> findById(...) { ... }
> > ```
> >
> > </details>
> >
> > > **Resultado real** (tomado de `/v3/api-docs`):
> > >
> > > | Operación | Respuestas documentadas |
> > > |---|---|
> > > | `GET /api/v1/students` | `200` |
> > > | `GET /api/v1/students/{id}` | `200` · `404` |
> > > | `POST /api/v1/students` | `201` (con la cabecera `Location`) · `400` · `409` |
> > > | `PUT /api/v1/students/{id}` | `200` · `400` · `404` · `409` |
> > > | `PATCH /api/v1/students/{id}/status` | `200` · `400` · `404` |
> > > | `DELETE /api/v1/students/{id}` | `204` · `404` |
> > >
> > > La respuesta `201` del contrato trae:
> > >
> > > ```json
> > > {
> > >   "description": "Estudiante creado",
> > >   "headers": { "Location": { "description": "URL del estudiante creado", "schema": { "type": "string", "example": "http://localhost:8080/api/v1/students/1" } } },
> > >   "content": { "application/json": { "schema": { "$ref": "#/components/schemas/StudentResponse" } } }
> > > }
> > > ```
> > >
> > > El cuerpo de la `201` ya apunta a **`StudentResponse`**: springdoc lo sabe por el tipo del método, no hizo falta decirlo.
> > >
> > > 💡 **Beneficio:** el contrato dice **qué puede salir mal**, qué significa cada código y dónde viene el `Location`. Quien consume la API sabe qué manejar sin probar a ciegas.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 3.4 · Ver el resultado</strong></summary>
>
> > En Swagger UI, al abrir `POST /api/v1/students` la sección **Responses** lista `201`, `400` y `409` con su descripción, y la `201` muestra la cabecera `Location`. La lista de operaciones muestra el `summary` de cada una.
> >
> > ✅ **Punto de control 3:** cada operación tiene resumen y todas sus respuestas.
>
> </details>
>

</details>

---

<details>
<summary><strong>🎛️ Paso 4 — Documentar los parámetros</strong></summary>

> <details>
> <summary><strong>Paso 4.1 · <code>@Parameter</code>: ruta y consulta</strong></summary>
>
> > Springdoc ya conoce el **nombre**, el **tipo** y si es obligatorio. `@Parameter` agrega lo que falta: una **descripción** y un **ejemplo**, que Swagger UI usa para **rellenar el campo**.
> >
> > <details>
> > <summary><strong>📄 Parámetros de ruta y de consulta</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @GetMapping("/{id}")
> > public ResponseEntity<StudentResponse> findById(
> >         @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) { ... }
> >
> > @GetMapping
> > public PageResponse<StudentResponse> search(
> >         @Parameter(description = "Parte del nombre a buscar", example = "ana")
> >         @RequestParam(required = false) String name,
> >         @ParameterObject Pageable pageable) { ... }
> > ```
> >
> > </details>
> >
> > > - `@PathVariable` → parámetro **de ruta** (obligatorio). `@RequestParam` → parámetro **de consulta**; con `required = false` es opcional.
> > >
> > > 💡 **Beneficio:** el ejemplo `1` o `ana` aparece ya escrito en el campo; quien prueba solo pulsa **Execute**.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 4.2 · <code>@ParameterObject</code>: la paginación</strong></summary>
>
> > `Pageable` es un objeto con varios campos (`page`, `size`, `sort`). Sin ayuda, Swagger lo muestra como **un solo cuadro JSON** (el parámetro `pageable` del Paso 1). Con `@ParameterObject` se **despliega en tres parámetros** de consulta.
> >
> > Se agrega el import `org.springdoc.core.annotations.ParameterObject`.
> >
> > > **Resultado real** (de `/v3/api-docs`):
> > >
> > > | Parámetro | Dónde | Tipo | Detalle |
> > > |---|---|---|---|
> > > | `name` | query | string | «Parte del nombre a buscar» · ejemplo `ana` |
> > > | `page` | query | integer | mínimo `0`, valor por defecto `0` |
> > > | `size` | query | integer | mínimo `1`, valor por defecto `10` |
> > > | `sort` | query | array de string | `propiedad,(asc\|desc)` |
> > >
> > > Con la URL de ejemplo `?name=a&page=0&size=2&sort=name,asc`, la respuesta real fue una `PageResponse` con los estudiantes que coinciden, ordenados por nombre.
> > >
> > > ⚠️ **Lo que el contrato no dice.** El tope de **50** por página lo aplica Spring (`max-page-size`), pero springdoc **no lo lee**: `size` solo declara «mínimo 1». Por eso el tope se escribió en la descripción de la operación (Paso 3.2).
> > >
> > > 💡 **Beneficio:** la paginación se prueba rellenando tres campos sencillos en vez de escribir un JSON a mano.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 4.3 · Ver el resultado</strong></summary>
>
> > En `GET /api/v1/students` aparecen los cuatro parámetros con su descripción; `name` viene con `ana` de ejemplo.
> >
> > ✅ **Punto de control 4:** `GET /api/v1/students` muestra `name`, `page`, `size` y `sort`.
>
> </details>
>

</details>

---

<details>
<summary><strong>📐 Paso 5 — Los DTO: <code>@Schema</code> y las validaciones</strong></summary>

> <details>
> <summary><strong>Paso 5.1 · <code>@Schema</code> en los DTO</strong></summary>
>
> > `@Schema` describe cada **campo** (descripción, ejemplo) y cada **clase** (qué representa). Se escribe en los **DTO**, que son la forma del contrato. **La entidad `StudentEntity` no se toca.**
> >
> > Cada DTO es un `record`: la anotación va sobre cada **componente** del `record`.
> >
> > <details>
> > <summary><strong>📄 StudentRequest.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/StudentRequest.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import com.academy.persistence.entity.StudentStatus;
> > import io.swagger.v3.oas.annotations.media.Schema;
> > import jakarta.validation.constraints.Email;
> > import jakarta.validation.constraints.Max;
> > import jakarta.validation.constraints.Min;
> > import jakarta.validation.constraints.NotBlank;
> > import jakarta.validation.constraints.NotNull;
> > import jakarta.validation.constraints.Size;
> >
> > @Schema(description = "Datos que el cliente envía para crear o reemplazar un estudiante")
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
> >         String email,
> >
> >         @Schema(description = "Estado académico", example = "ACTIVE")
> >         @NotNull
> >         StudentStatus status
> > ) {
> > }
> > ```
> >
> > </details>
> >
> > <details>
> > <summary><strong>📄 StudentStatusRequest.java</strong></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/dto/StudentStatusRequest.java`
> >
> > ```java
> > package com.academy.presentation.dto;
> >
> > import com.academy.persistence.entity.StudentStatus;
> > import io.swagger.v3.oas.annotations.media.Schema;
> > import jakarta.validation.constraints.NotNull;
> >
> > @Schema(description = "Nuevo estado del estudiante (es lo único que cambia)")
> > public record StudentStatusRequest(
> >
> >         @Schema(description = "Estado académico", example = "INACTIVE")
> >         @NotNull
> >         StudentStatus status
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
> > import com.academy.persistence.entity.StudentStatus;
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
> >         String email,
> >
> >         @Schema(description = "Estado académico", example = "ACTIVE")
> >         StudentStatus status
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
> >         @Schema(description = "Total de elementos que cumplen el filtro", example = "3")
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
> > > 💡 **Beneficio de este paso:** la documentación del contrato vive **junto al contrato**. Cambiar la tabla no obliga a tocar la documentación, y la entidad queda libre de anotaciones de Swagger.
> > >
> > > 🔎 **¿Y `required`, `readOnly` y el `id`?** No hacen falta: `@NotBlank`/`@NotNull` ya marcan los campos obligatorios, y `StudentRequest` **no tiene `id`**, así que no hay nada que declarar de solo lectura.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 5.2 · Bean Validation pasa solo al contrato</strong></summary>
>
> > Las restricciones **no hay que repetirlas**: springdoc las lee de Bean Validation. **Resultado real** del modelo `StudentRequest` en `/v3/api-docs`, ahora con `@Schema`:
> >
> > ```json
> > {
> >   "description": "Datos que el cliente envía para crear o reemplazar un estudiante",
> >   "required": ["email", "name", "status"],
> >   "properties": {
> >     "name":   { "type": "string", "minLength": 2, "maxLength": 100, "description": "Nombre completo", "example": "Ana Torres" },
> >     "age":    { "type": "integer", "format": "int32", "minimum": 16, "maximum": 99, "description": "Edad en años", "example": 22 },
> >     "email":  { "type": "string", "description": "Correo electrónico, único entre estudiantes", "example": "ana@mail.com" },
> >     "status": { "type": "string", "enum": ["ACTIVE", "INACTIVE"], "description": "Estado académico", "example": "ACTIVE" }
> >   }
> > }
> > ```
> >
> > | En el código | En el contrato |
> > |---|---|
> > | `@NotBlank`, `@NotNull` | Campo en `required` |
> > | `@Size(min = 2, max = 100)` | `minLength` y `maxLength` |
> > | `@Min(16)` · `@Max(99)` | `minimum` y `maximum` |
> > | `enum StudentStatus` | `enum: ["ACTIVE","INACTIVE"]`: en Swagger UI, un **desplegable** |
> > | `@Email` | **No** aparece como `format` en esta versión: la validación funciona en el servidor, pero el contrato no lo dice |
> > | `int age` (tipo primitivo) | **No** entra en `required`, aunque siempre tiene valor |
> >
> > > 💡 **Beneficio de este paso:** **una sola fuente de verdad**. La regla se escribe una vez (en la validación) y sirve para **rechazar** datos y para **documentar**. El contrato no puede contradecir al código.
> > >
> > > ⚠️ **Lectura crítica.** No todo pasa: `@Email` no llega al contrato y un `int` no se marca obligatorio. Si importa que el cliente lo sepa, se agrega a mano con `@Schema(format = "email")` o `@Schema(requiredMode = REQUIRED)`.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 5.3 · Ver el resultado</strong></summary>
>
> > En la sección **Schemas** (al final de la pantalla) aparecen `StudentRequest`, `StudentResponse`, `StudentStatusRequest` y `PageResponseStudentResponse`. Al expandirlos se ven los campos con su descripción, los obligatorios marcados y el desplegable de `status`. En `GET /api/v1/students`, la respuesta `200` es una `PageResponseStudentResponse`.
> >
> > > 🔎 El nombre `PageResponseStudentResponse` lo arma springdoc con el nombre del genérico (`PageResponse<T>`) y el de su parámetro (`StudentResponse`).
> >
> > ✅ **Punto de control 5:** los modelos muestran descripción, ejemplos, obligatorios y el desplegable del enumerado. La entidad no cambió.
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
> > El ejemplo que genera `@Schema` es **uno** y se arma campo por campo. Con `@RequestBody` de Swagger y `@ExampleObject` se ofrecen **varios ejemplos completos**, con nombre, que se eligen en un **desplegable**.
> >
> > La anotación se escribe con **nombre completo** para no chocar con la `@RequestBody` de Spring:
> >
> > <details>
> > <summary><strong>📄 POST /api/v1/students</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @io.swagger.v3.oas.annotations.parameters.RequestBody(
> >         description = "Datos del estudiante",
> >         content = @Content(examples = {
> >                 @ExampleObject(name = "Estudiante activo", summary = "Un estudiante activo",
> >                         value = "{\"name\":\"Ana Torres\",\"age\":22,\"email\":\"ana@mail.com\",\"status\":\"ACTIVE\"}"),
> >                 @ExampleObject(name = "Estudiante inactivo", summary = "Un estudiante inactivo",
> >                         value = "{\"name\":\"Luis Pérez\",\"age\":25,\"email\":\"luis@mail.com\",\"status\":\"INACTIVE\"}")
> >         }))
> > @PostMapping
> > public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) { ... }
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
> > > **Resultado real** (de `/v3/api-docs`): el cuerpo de `POST /api/v1/students` apunta a `StudentRequest`, trae los ejemplos `Estudiante activo` y `Estudiante inactivo`, y `"required": true`.
> > >
> > > 💡 **Beneficio:** quien prueba elige un caso válido con un clic. Sirve también para mostrar casos **de borde** (un ejemplo «nombre vacío» para provocar el `400`).
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
> > Se agrega una operación vieja para marcarla: `GET /api/v1/students/by-email`. Necesita un método más en el servicio (sin Swagger) que usa la consulta derivada que ya existe en el repositorio:
> >
> > <details>
> > <summary><strong>📄 StudentService.java (agregar)</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/business/service/StudentService.java`
> >
> > ```java
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
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @Operation(summary = "Buscar un estudiante por correo",
> >         description = "Obsoleto: usar `GET /api/v1/students?name=` o `GET /api/v1/students/{id}`.",
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
> > > Se **marca obsoleta**: no se borra, se avisa. En Swagger UI la operación aparece **tachada y en gris**. El controlador sigue devolviendo un `StudentResponse`, no la entidad.
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
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
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
> > > | Comprobación real | Resultado |
> > > |---|---|
> > > | `GET /api/v1/students/ping` | `200` · `pong` (la ruta **sí funciona**) |
> > > | `ping` en `/api-docs` | **No aparece** |
> > >
> > > `@Hidden` también se puede poner **sobre la clase** para ocultar un controlador entero.
> > >
> > > ⚠️ **No es seguridad.** Ocultar una ruta de la documentación **no la protege**: cualquiera que conozca la URL puede llamarla.
> > >
> > > 💡 **Beneficio:** el contrato público muestra solo lo que se quiere ofrecer.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 7.3 · Ver el resultado</strong></summary>
>
> > La lista muestra `by-email` tachada y **no** muestra `ping`. **Resultado real** de `GET /api/v1/students/by-email?email=ana@mail.com`:
> >
> > ```text
> > HTTP/1.1 200
> > Content-Type: application/json
> >
> > {"id":1,"name":"Ana Torres","age":22,"email":"ana@mail.com","status":"ACTIVE"}
> > ```
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
> > Dos piezas: **declarar** que la API usa un token *Bearer* (en `OpenApiConfig`) y **marcar** qué operaciones lo exigen (`@SecurityRequirement`).
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
> > <summary><strong>📄 DELETE /api/v1/students/{id}</strong> <em>(solo el cambio)</em></summary>
> >
> > **Ruta:** `src/main/java/com/academy/presentation/controller/StudentController.java`
> >
> > ```java
> > @Operation(summary = "Eliminar un estudiante", security = @SecurityRequirement(name = "bearerAuth"))
> > @DeleteMapping("/{id}")
> > public ResponseEntity<Void> delete(...) { ... }
> > ```
> >
> > </details>
> >
> > > | Pieza | Dónde se ve |
> > > |---|---|
> > > | `SecurityScheme` `HTTP` / `bearer` / `JWT` | El botón **Authorize** 🔓 arriba a la derecha |
> > > | `@SecurityRequirement(name = "bearerAuth")` | Un **candado** 🔒 en `DELETE /api/v1/students/{id}` |
> > >
> > > Al pulsar **Authorize** se escribe el token; Swagger UI lo envía en la cabecera `Authorization: Bearer …` de **todas** las operaciones con candado.
> > >
> > > ⚠️ **Esto solo documenta.** La aplicación **no valida** ningún token (no hay Spring Security): `DELETE` responde `204` con o sin token. Para exigirlo de verdad se necesita seguridad real (otra guía).
> > >
> > > 💡 **Beneficio:** el contrato dice **cómo autenticarse**; cuando se agregue la seguridad, Swagger UI ya sabrá mandar el token.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 8.2 · Ver el resultado</strong></summary>
>
> > **Resultado real:** en `/api-docs`, `components.securitySchemes` contiene `bearerAuth` (`http`, `bearer`, `JWT`) y **solo** `DELETE /api/v1/students/{id}` trae `security`. En la pantalla aparece **Authorize** arriba a la derecha y un candado en esa operación.
> >
> > ✅ **Punto de control 8:** botón *Authorize* visible y un candado en `DELETE`.
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
> > Una API grande se divide en **grupos** (pública, interna, administración…). Cada grupo tiene **su propio contrato** y aparece en el selector **Select a definition**.
> >
> > Se agrega un segundo controlador con operaciones de apoyo y los dos grupos en la configuración. Este es el `OpenApiConfig` **completo y final**:
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
> >                         .description("CRUD de estudiantes con DTO, Spring Boot, JPA y MySQL, documentado con OpenAPI.")
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
> >     public GroupedOpenApi studentsGroup() {
> >         return GroupedOpenApi.builder()
> >                 .group("students")
> >                 .displayName("Students")
> >                 .pathsToMatch("/api/v1/students/**")
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
> > import com.academy.business.service.ServiceStatus;
> > import com.academy.business.service.StudentService;
> > import com.academy.persistence.entity.StudentEntity;
> > import com.academy.persistence.entity.StudentStatus;
> > import io.swagger.v3.oas.annotations.Operation;
> > import io.swagger.v3.oas.annotations.tags.Tag;
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
> >     private final StudentService service;
> >
> >     public InternalController(StudentService service) {
> >         this.service = service;
> >     }
> >
> >     @Operation(summary = "Estado del servicio")
> >     @GetMapping("/health")
> >     public Map<String, String> health() {
> >         return Map.of("status", "UP");
> >     }
> >
> >     @Operation(summary = "Cargar estudiantes de ejemplo", description = "Crea tres estudiantes si no existen.")
> >     @PostMapping("/seed")
> >     public Map<String, Integer> seed() {
> >         int created = 0;
> >         StudentEntity[] samples = {
> >                 new StudentEntity(null, "Ana Torres", 22, "ana@mail.com", StudentStatus.ACTIVE),
> >                 new StudentEntity(null, "Luis Pérez", 25, "luis@mail.com", StudentStatus.ACTIVE),
> >                 new StudentEntity(null, "María Gómez", 31, "maria@mail.com", StudentStatus.INACTIVE)
> >         };
> >         for (StudentEntity sample : samples) {
> >             if (service.create(sample).status() == ServiceStatus.OK) {
> >                 created++;
> >             }
> >         }
> >         return Map.of("created", created);
> >     }
> > }
> > ```
> >
> > </details>
> >
> > > | Pieza | Qué hace |
> > > |---|---|
> > > | `GroupedOpenApi.builder().group("students")` | Nombre técnico del grupo (`/api-docs/students`) |
> > > | `.displayName("Students")` | Nombre que se ve en el selector |
> > > | `.pathsToMatch("/api/v1/students/**")` | Qué rutas entran en el grupo |
> > > | `InternalController` | `GET /internal/health` y `POST /internal/seed` (carga tres estudiantes de ejemplo) |
> > >
> > > 🔎 `InternalController` usa la entidad y `ServiceResult` porque es un apoyo **interno** que no devuelve estudiantes. Aun así, la regla de crear sigue en `StudentService`.
> > >
> > > **Resultado real:**
> > >
> > > | Grupo | Rutas |
> > > |---|---|
> > > | `students` | `/api/v1/students`, `/api/v1/students/{id}`, `/api/v1/students/{id}/status`, `/api/v1/students/by-email` |
> > > | `internal` | `/internal/seed`, `/internal/health` |
> > >
> > > 💡 **Beneficio:** cada audiencia ve **solo su parte**. El contrato público no mezcla operaciones de mantenimiento, y los clientes de la API de estudiantes no se enteran de `/internal`.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 9.2 · Ver el resultado</strong></summary>
>
> > Arriba a la derecha aparece **Select a definition** con `Internal` y `Students`. Al cambiar de grupo la lista cambia.
> >
> > **Resultado real** de `POST /internal/seed` ejecutado dos veces:
> >
> > ```text
> > POST /internal/seed  →  {"created":3}
> > POST /internal/seed  →  {"created":0}
> > ```
> >
> > La segunda vez no crea nada, porque los correos ya existen (el servicio responde `CONFLICT`).
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
> > Todo lo que no se hace con código se ajusta en `application.properties` (debajo de lo que ya tiene: conexión a MySQL, JPA y límites de paginación):
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
> > springdoc.swagger-ui.urls-primary-name=Students
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
> > > | `springdoc.swagger-ui.urls-primary-name` | Grupo que se muestra **primero** (sin ella, el primero por orden alfabético: `Internal`) |
> > > | `springdoc.swagger-ui.deep-linking` | Permite enlazar a una operación concreta |
> > > | `springdoc.swagger-ui.operations-sorter` | Orden de las operaciones: `alpha` (por ruta) o `method` (por método HTTP) |
> > > | `springdoc.swagger-ui.tags-sorter` | Orden de los grupos de etiquetas |
> > > | `springdoc.swagger-ui.display-request-duration` | Muestra cuánto tardó cada petición de prueba |
> > > | `springdoc.swagger-ui.doc-expansion` | `none` (todo cerrado), `list` (operaciones visibles) o `full` (todo abierto) |
> > > | `springdoc.swagger-ui.try-it-out-enabled` | Abre las operaciones ya en modo edición |
> > > | `springdoc.default-produces-media-type` · `default-consumes-media-type` | Formato por defecto de entrada y salida |
> > >
> > > **Comprobación real:** con `api-docs.path=/api-docs`, la dirección `/v3/api-docs` responde `404` y `/api-docs` responde el contrato.
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
> > curl http://localhost:8080/api-docs/students -o students.json   # solo un grupo
> > ```
> >
> > **Resultado real** (inicio del YAML):
> >
> > ```text
> > openapi: 3.0.1
> > info:
> >   title: Academy API
> >   description: "CRUD de estudiantes con DTO, Spring Boot, JPA y MySQL, documentado\
> >     \ con OpenAPI."
> >   contact:
> >     name: Equipo Academy
> >     email: academy@mail.com
> > ```
> >
> > > | Quién usa el archivo | Para qué |
> > > |---|---|
> > > | **Postman / Insomnia** | Importar la colección completa |
> > > | **Generadores de clientes** (OpenAPI Generator) | Crear el cliente en TypeScript, Java, etc. |
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
> > > Se arranca con `./mvnw spring-boot:run -Dspring-boot.run.profiles=prod` (o `--spring.profiles.active=prod` con el `.jar`). **Resultado real:**
> > >
> > > | Dirección | Respuesta |
> > > |---|---|
> > > | `/api-docs` | `404` |
> > > | `/swagger-ui.html` | `404` |
> > > | `/api/v1/students` | `200` (la API **sigue funcionando**) |
> > >
> > > 💡 **Beneficio:** la misma aplicación documenta en desarrollo y se calla en producción, sin cambiar código.
> > >
> > > ⚠️ **Y si se necesita en producción**, se protege con autenticación; apagarlo es lo más simple y seguro por defecto.
>
> </details>
>
>
> <details>
> <summary><strong>Paso 10.4 · Prueba de punta a punta</strong></summary>
>
> > Con todo activo se comprobó el contrato completo. **Resultado real** (lista de operaciones de `/api-docs`):
> >
> > | Método | Ruta | Resumen | Marcas | Respuestas |
> > |---|---|---|---|---|
> > | `GET` | `/api/v1/students` | Listar estudiantes | | `200` |
> > | `POST` | `/api/v1/students` | Crear un estudiante | | `201` · `400` · `409` |
> > | `GET` | `/api/v1/students/{id}` | Buscar un estudiante por id | | `200` · `404` |
> > | `PUT` | `/api/v1/students/{id}` | Reemplazar un estudiante | | `200` · `400` · `404` · `409` |
> > | `PATCH` | `/api/v1/students/{id}/status` | Cambiar el estado de un estudiante | | `200` · `400` · `404` |
> > | `DELETE` | `/api/v1/students/{id}` | Eliminar un estudiante | 🔒 seguridad | `204` · `404` |
> > | `GET` | `/api/v1/students/by-email` | Buscar un estudiante por correo | ~~obsoleta~~ | `200` · `404` |
> > | `GET` | `/internal/health` | Estado del servicio | | `200` |
> > | `POST` | `/internal/seed` | Cargar estudiantes de ejemplo | | `200` |
> >
> > Modelos: `StudentRequest`, `StudentResponse`, `StudentStatusRequest`, `PageResponseStudentResponse`. Esquema de seguridad: `bearerAuth`. La ruta `/api/v1/students/ping` **no aparece**.
> >
> > El controlador **final**, con todas las anotaciones juntas:
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
> > import com.academy.presentation.dto.StudentStatusRequest;
> > import com.academy.presentation.mapper.StudentMapper;
> > import io.swagger.v3.oas.annotations.Hidden;
> > import io.swagger.v3.oas.annotations.Operation;
> > import io.swagger.v3.oas.annotations.Parameter;
> > import io.swagger.v3.oas.annotations.headers.Header;
> > import io.swagger.v3.oas.annotations.media.Content;
> > import io.swagger.v3.oas.annotations.media.ExampleObject;
> > import io.swagger.v3.oas.annotations.media.Schema;
> > import io.swagger.v3.oas.annotations.responses.ApiResponse;
> > import io.swagger.v3.oas.annotations.security.SecurityRequirement;
> > import io.swagger.v3.oas.annotations.tags.Tag;
> > import jakarta.validation.Valid;
> > import org.springdoc.core.annotations.ParameterObject;
> > import org.springframework.data.domain.Pageable;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.http.ResponseEntity;
> > import org.springframework.web.bind.annotation.DeleteMapping;
> > import org.springframework.web.bind.annotation.GetMapping;
> > import org.springframework.web.bind.annotation.PatchMapping;
> > import org.springframework.web.bind.annotation.PathVariable;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.PutMapping;
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
> >     @Operation(summary = "Listar estudiantes",
> >             description = "Devuelve una página de estudiantes. Con `name` filtra por nombre (contiene, sin importar mayúsculas). El tamaño máximo de página es 50.")
> >     @ApiResponse(responseCode = "200", description = "Página de estudiantes")
> >     @GetMapping
> >     public PageResponse<StudentResponse> search(
> >             @Parameter(description = "Parte del nombre a buscar", example = "ana")
> >             @RequestParam(required = false) String name,
> >             @ParameterObject Pageable pageable) {
> >         return StudentMapper.toPageResponse(service.search(name, pageable));
> >     }
> >
> >     @Operation(summary = "Buscar un estudiante por id")
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
> >     @Operation(summary = "Buscar un estudiante por correo",
> >             description = "Obsoleto: usar `GET /api/v1/students?name=` o `GET /api/v1/students/{id}`.",
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
> >     @Operation(summary = "Crear un estudiante",
> >             description = "El `id` lo genera la base y no se envía. Si el correo ya existe responde `409`.")
> >     @io.swagger.v3.oas.annotations.parameters.RequestBody(
> >             description = "Datos del estudiante",
> >             content = @Content(examples = {
> >                     @ExampleObject(name = "Estudiante activo", summary = "Un estudiante activo",
> >                             value = "{\"name\":\"Ana Torres\",\"age\":22,\"email\":\"ana@mail.com\",\"status\":\"ACTIVE\"}"),
> >                     @ExampleObject(name = "Estudiante inactivo", summary = "Un estudiante inactivo",
> >                             value = "{\"name\":\"Luis Pérez\",\"age\":25,\"email\":\"luis@mail.com\",\"status\":\"INACTIVE\"}")
> >             }))
> >     @ApiResponse(responseCode = "201", description = "Estudiante creado",
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
> >     @Operation(summary = "Reemplazar un estudiante",
> >             description = "Reemplaza todos los datos del estudiante. Responde `404` si no existe y `409` si el correo es de otro estudiante.")
> >     @ApiResponse(responseCode = "200", description = "Estudiante actualizado")
> >     @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
> >     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
> >     @ApiResponse(responseCode = "409", description = "El correo pertenece a otro estudiante", content = @Content)
> >     @PutMapping("/{id}")
> >     public ResponseEntity<StudentResponse> update(
> >             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id,
> >             @Valid @RequestBody StudentRequest request) {
> >         return respond(service.update(id, StudentMapper.toEntity(request)));
> >     }
> >
> >     @Operation(summary = "Cambiar el estado de un estudiante",
> >             description = "Cambia solo el estado; el resto de los datos no se toca.")
> >     @ApiResponse(responseCode = "200", description = "Estado actualizado")
> >     @ApiResponse(responseCode = "400", description = "El estado es obligatorio", content = @Content)
> >     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
> >     @PatchMapping("/{id}/status")
> >     public ResponseEntity<StudentResponse> changeStatus(
> >             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id,
> >             @Valid @RequestBody StudentStatusRequest request) {
> >         return respond(service.changeStatus(id, request.status()));
> >     }
> >
> >     @Operation(summary = "Eliminar un estudiante", security = @SecurityRequirement(name = "bearerAuth"))
> >     @ApiResponse(responseCode = "204", description = "Estudiante eliminado")
> >     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
> >     @DeleteMapping("/{id}")
> >     public ResponseEntity<Void> delete(
> >             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
> >         return service.delete(id)
> >                 ? ResponseEntity.noContent().build()
> >                 : ResponseEntity.notFound().build();
> >     }
> >
> >     @Hidden
> >     @GetMapping("/ping")
> >     public String ping() {
> >         return "pong";
> >     }
> >
> >     private ResponseEntity<StudentResponse> respond(ServiceResult<StudentEntity> result) {
> >         return switch (result.status()) {
> >             case OK -> ResponseEntity.ok(StudentMapper.toResponse(result.value()));
> >             case NOT_FOUND -> ResponseEntity.notFound().build();
> >             case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
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
<summary><strong>🗺️ Mapa: funcionalidad de Swagger → cómo se usa</strong></summary>

> | Funcionalidad | Cómo se activa | Paso |
> |---|---|:-:|
> | Documentación automática | Dependencia `springdoc-openapi-starter-webmvc-ui` | 1 |
> | Probar desde el navegador | **Try it out** de Swagger UI | 1 |
> | Nombre, versión, contacto, licencia, servidores | Bean `OpenAPI` (o `@OpenAPIDefinition`) | 2 |
> | Agrupar operaciones por etiqueta | `@Tag` | 3 |
> | Resumen y descripción de cada operación | `@Operation` | 3 |
> | Respuestas posibles (`201`, `404`, `409`…) y la cabecera `Location` | `@ApiResponse` (+ `@Header`) | 3 |
> | Descripción y ejemplo de un parámetro | `@Parameter` | 4 |
> | Desplegar un objeto (`Pageable`) en parámetros | `@ParameterObject` | 4 |
> | Descripción y ejemplo de un campo del DTO | `@Schema` en los `record` | 5 |
> | Reglas de validación en el contrato | **Automático** desde Bean Validation | 5 |
> | Enumerados como desplegable | **Automático** desde `enum` | 5 |
> | Nombres de los modelos (`StudentRequest`…) | **Automático** desde los DTO | 5 |
> | Varios ejemplos del cuerpo | `@RequestBody` de Swagger + `@ExampleObject` | 6 |
> | Operación obsoleta | `@Operation(deprecated = true)` | 7 |
> | Ocultar operación o controlador | `@Hidden` | 7 |
> | Autenticación (*Authorize*) | `SecurityScheme` + `@SecurityRequirement` | 8 |
> | Varios contratos (grupos) | `GroupedOpenApi` | 9 |
> | Orden, expansión, rutas, grupo inicial | Propiedades `springdoc.*` | 10 |
> | Exportar el contrato (JSON/YAML) | `/api-docs`, `/api-docs.yaml`, `/api-docs/{grupo}` | 10 |
> | Apagar la documentación | `springdoc.api-docs.enabled` y `swagger-ui.enabled` | 10 |
>
> > 🔎 **Lectura clave.** Varias funcionalidades funcionan **sin escribir documentación**: la dependencia, *Try it out*, las validaciones, los enumerados y los nombres de los modelos. Con los DTO, además, el contrato **nunca** muestra un `id` en la petición ni un campo interno. El resto se agrega con anotaciones **solo en `presentation`** o con propiedades.

</details>

<details>
<summary><strong>🏁 Cierre — preguntas de síntesis</strong></summary>

> 1. ¿Qué diferencia hay entre OpenAPI, Swagger UI y springdoc? *(El contrato, la pantalla y la librería que genera el primero y sirve la segunda.)*
> 2. ¿Por qué `@Schema` va en los DTO y no en la entidad? *(La entidad es la tabla; el DTO es el contrato. Documentar el contrato en el DTO deja la entidad libre y evita que renombrar una columna cambie la API.)*
> 3. ¿Qué parte de la documentación es automática y qué parte hay que escribir? *(Rutas, tipos, validaciones, enumerados y nombres de modelos son automáticos; descripciones, ejemplos y significados de los códigos los escribe una persona.)*
> 4. ¿Qué pasa con `@Email` y con el tope de 50 por página? ¿Qué se aprende de eso? *(Ninguno llega al contrato; conviene **leer** el resultado y no suponer que todo pasa.)*
> 5. ¿`@Hidden` protege una ruta? *(No: solo la quita de la documentación.)*
> 6. ¿Qué hace realmente el botón *Authorize* aquí? *(Guarda un token y lo envía; como no hay Spring Security, nadie lo valida.)*
> 7. ¿Por qué se apaga Swagger en producción? *(Revela la API completa; se desactiva con propiedades y un perfil.)*
> 8. ¿Qué respuestas siguen sin cuerpo explicativo? *(`400`, `404` y `409`: un formato de error uniforme se estudia en el Módulo 7.)*

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

> | Síntoma | Causa probable | Solución |
> |---|---|---|
> | `404` en `/swagger-ui.html` | Falta la dependencia, o `springdoc.swagger-ui.enabled=false` | Revisar el `pom.xml` y las propiedades |
> | `404` en `/v3/api-docs` | Se cambió `springdoc.api-docs.path` | Usar la ruta configurada |
> | `NoSuchMethodError` o fallos al arrancar con springdoc | Versión de springdoc incompatible con Spring Boot (1.x en Boot 3) | Usar `springdoc-openapi-starter-webmvc-ui` **2.x** con Boot 3 |
> | `Failed to load API definition` en la pantalla | El contrato no se genera (error en una anotación o en un controlador) | Abrir `/api-docs` directamente y leer el mensaje |
> | El `Pageable` se muestra como un solo cuadro JSON | Falta `@ParameterObject` | Agregarlo |
> | El `409` aparece con un modelo de respuesta inexistente | Falta `content = @Content` en esa `@ApiResponse` | Agregarlo |
> | Dos `@RequestBody` en el mismo parámetro no compilan | Se importaron las dos | Usar el nombre completo de la de Swagger |
> | Los `@Schema` no aparecen en el contrato | Se pusieron en la entidad, que la API ya no expone | Ponerlos en los `record` de `presentation.dto` |
> | El `@Schema` de un campo del `record` se ignora | Se escribió sobre el `record` en vez de sobre el componente | Anotar cada componente |
> | Los cambios de documentación no se ven | Caché del navegador o no se reinició la aplicación | Reiniciar y recargar con `Ctrl+F5` |
> | `Communications link failure` al arrancar | MySQL no está arrancado o el puerto es otro | `docker ps` y revisar la URL |

</details>

<details>
<summary><strong>🚀 Retos finales (para quien termine antes)</strong></summary>

> 1. **`@Email` en el contrato.** Agregar `format = "email"` al `@Schema` de `email` y comprobar que aparece en `/api-docs`.
> 2. **`age` obligatorio.** Agregar `requiredMode = REQUIRED` al `@Schema` de `age` y comprobar que entra en `required`.
> 3. **Caso de error con ejemplo.** Agregar un tercer `@ExampleObject` con nombre vacío para provocar el `400` desde Swagger UI.
> 4. **El tope de 50 en el contrato.** Buscar cómo declarar el máximo de `size` y comprobar que aparece en `/api-docs`.
> 5. **Un tag por controlador.** Dar a `InternalController` la etiqueta «Internal» y comprobar el orden con `tags-sorter`.
> 6. **Un segundo servidor.** Agregar `https://api.academy.com` como servidor de producción y probar el selector.
> 7. **Un recurso nuevo.** Repetir el patrón del Módulo 05 para `Course` (DTO, mapeador, servicio, controlador), documentarlo con su propio tag y su grupo.
> 8. **Seguridad real.** Agregar Spring Security con un token fijo y comprobar que *Authorize* ahora **sí** cambia el resultado de `DELETE`.
> 9. **Generar un cliente.** Con el YAML exportado, generar un cliente con OpenAPI Generator.
> 10. **Modelo de error.** Cuando se estudien las excepciones (Módulo 7), crear un `ErrorResponse` y documentarlo en `400`, `404` y `409` con `content = @Content(schema = @Schema(implementation = ErrorResponse.class))`.

</details>

<details>
<summary><strong>📁 Estructura final esperada</strong></summary>

> ```text
> 📁 students-api
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
>     │   │   │   ├── 📄 StudentRequest.java
>     │   │   │   ├── 📄 StudentStatusRequest.java
>     │   │   │   ├── 📄 StudentResponse.java
>     │   │   │   └── 📄 PageResponse.java
>     │   │   └── 📁 mapper
>     │   │       └── 📄 StudentMapper.java
>     │   ├── 📁 business
>     │   │   └── 📁 service
>     │   │       ├── 📄 StudentService.java
>     │   │       ├── 📄 ServiceResult.java
>     │   │       └── 📄 ServiceStatus.java
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
>         <version>3.3.5</version>
>         <relativePath/>
>     </parent>
>     <groupId>com.academy</groupId>
>     <artifactId>students-api</artifactId>
>     <version>0.0.1-SNAPSHOT</version>
>
>     <properties>
>         <java.version>17</java.version>
>     </properties>
>
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
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
>             <version>2.6.0</version>
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
> #### 📁 service
>
> <details>
> <summary><strong>📄 ServiceStatus.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/business/service/ServiceStatus.java`
>
> ```java
> package com.academy.business.service;
>
> public enum ServiceStatus {
>     OK,
>     NOT_FOUND,
>     CONFLICT
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 ServiceResult.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/business/service/ServiceResult.java`
>
> ```java
> package com.academy.business.service;
>
> public record ServiceResult<T>(ServiceStatus status, T value) {
>
>     public static <T> ServiceResult<T> ok(T value) {
>         return new ServiceResult<>(ServiceStatus.OK, value);
>     }
>
>     public static <T> ServiceResult<T> notFound() {
>         return new ServiceResult<>(ServiceStatus.NOT_FOUND, null);
>     }
>
>     public static <T> ServiceResult<T> conflict() {
>         return new ServiceResult<>(ServiceStatus.CONFLICT, null);
>     }
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 StudentService.java</strong></summary>
>
> **Ruta:** `src/main/java/com/academy/business/service/StudentService.java`
>
> ```java
> package com.academy.business.service;
>
> import com.academy.persistence.entity.StudentEntity;
> import com.academy.persistence.entity.StudentStatus;
> import com.academy.persistence.repository.StudentJpaRepository;
> import org.springframework.data.domain.Page;
> import org.springframework.data.domain.Pageable;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
>
> import java.util.Optional;
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
>     public Optional<StudentEntity> findById(Long id) {
>         return students.findById(id);
>     }
>
>     @Transactional(readOnly = true)
>     public Optional<StudentEntity> findByEmail(String email) {
>         return students.findByEmailIgnoreCase(email);
>     }
>
>     @Transactional
>     public ServiceResult<StudentEntity> create(StudentEntity data) {
>         if (students.existsByEmailIgnoreCase(data.getEmail())) {
>             return ServiceResult.conflict();
>         }
>         StudentEntity toSave = new StudentEntity(
>                 null, data.getName(), data.getAge(), data.getEmail(), data.getStatus());
>         return ServiceResult.ok(students.save(toSave));
>     }
>
>     @Transactional
>     public ServiceResult<StudentEntity> update(Long id, StudentEntity data) {
>         if (!students.existsById(id)) {
>             return ServiceResult.notFound();
>         }
>         if (students.existsByEmailIgnoreCaseAndIdNot(data.getEmail(), id)) {
>             return ServiceResult.conflict();
>         }
>         StudentEntity toSave = new StudentEntity(
>                 id, data.getName(), data.getAge(), data.getEmail(), data.getStatus());
>         return ServiceResult.ok(students.save(toSave));
>     }
>
>     @Transactional
>     public ServiceResult<StudentEntity> changeStatus(Long id, StudentStatus status) {
>         Optional<StudentEntity> current = students.findById(id);
>         if (current.isEmpty()) {
>             return ServiceResult.notFound();
>         }
>         StudentEntity student = current.get();
>         StudentEntity toSave = new StudentEntity(
>                 id, student.getName(), student.getAge(), student.getEmail(), status);
>         return ServiceResult.ok(students.save(toSave));
>     }
>
>     @Transactional
>     public boolean delete(Long id) {
>         if (!students.existsById(id)) {
>             return false;
>         }
>         students.deleteById(id);
>         return true;
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
>         @NotBlank
>         @Size(min = 2, max = 100)
>         String name,
>
>         @Schema(description = "Edad en años", example = "22")
>         @Min(16)
>         @Max(99)
>         int age,
>
>         @Schema(description = "Correo electrónico, único entre estudiantes", example = "ana@mail.com")
>         @NotBlank
>         @Email
>         String email,
>
>         @Schema(description = "Estado académico", example = "ACTIVE")
>         @NotNull
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
>         @NotNull
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
> import io.swagger.v3.oas.models.Components;
> import io.swagger.v3.oas.models.OpenAPI;
> import io.swagger.v3.oas.models.info.Contact;
> import io.swagger.v3.oas.models.info.Info;
> import io.swagger.v3.oas.models.info.License;
> import io.swagger.v3.oas.models.security.SecurityScheme;
> import io.swagger.v3.oas.models.servers.Server;
> import org.springdoc.core.models.GroupedOpenApi;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
>
> import java.util.List;
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
>                 .components(new Components().addSecuritySchemes("bearerAuth",
>                         new SecurityScheme()
>                                 .type(SecurityScheme.Type.HTTP)
>                                 .scheme("bearer")
>                                 .bearerFormat("JWT")));
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
> import com.academy.business.service.ServiceResult;
> import com.academy.business.service.ServiceStatus;
> import com.academy.business.service.StudentService;
> import com.academy.persistence.entity.StudentEntity;
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
> import org.springframework.http.HttpStatus;
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
>     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
>     @GetMapping("/{id}")
>     public ResponseEntity<StudentResponse> findById(
>             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
>         return service.findById(id)
>                 .map(StudentMapper::toResponse)
>                 .map(ResponseEntity::ok)
>                 .orElseGet(() -> ResponseEntity.notFound().build());
>     }
>
>     @Operation(summary = "Buscar un estudiante por correo",
>             description = "Obsoleto: usar `GET /api/v1/students?name=` o `GET /api/v1/students/{id}`.",
>             deprecated = true)
>     @ApiResponse(responseCode = "200", description = "Estudiante encontrado")
>     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese correo", content = @Content)
>     @GetMapping("/by-email")
>     public ResponseEntity<StudentResponse> findByEmail(
>             @Parameter(description = "Correo exacto", example = "ana@mail.com") @RequestParam String email) {
>         return service.findByEmail(email)
>                 .map(StudentMapper::toResponse)
>                 .map(ResponseEntity::ok)
>                 .orElseGet(() -> ResponseEntity.notFound().build());
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
>     @ApiResponse(responseCode = "400", description = "Datos inválidos (Bean Validation)", content = @Content)
>     @ApiResponse(responseCode = "409", description = "El correo ya está registrado", content = @Content)
>     @PostMapping
>     public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
>         ServiceResult<StudentEntity> result = service.create(StudentMapper.toEntity(request));
>         if (result.status() == ServiceStatus.CONFLICT) {
>             return ResponseEntity.status(HttpStatus.CONFLICT).build();
>         }
>         StudentResponse body = StudentMapper.toResponse(result.value());
>         URI location = ServletUriComponentsBuilder.fromCurrentRequest()
>                 .path("/{id}").buildAndExpand(body.id()).toUri();
>         return ResponseEntity.created(location).body(body);
>     }
>
>     @Operation(summary = "Reemplazar un estudiante",
>             description = "Reemplaza todos los datos del estudiante. Responde `404` si no existe y `409` si el correo es de otro estudiante.")
>     @ApiResponse(responseCode = "200", description = "Estudiante actualizado")
>     @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
>     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
>     @ApiResponse(responseCode = "409", description = "El correo pertenece a otro estudiante", content = @Content)
>     @PutMapping("/{id}")
>     public ResponseEntity<StudentResponse> update(
>             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id,
>             @Valid @RequestBody StudentRequest request) {
>         return respond(service.update(id, StudentMapper.toEntity(request)));
>     }
>
>     @Operation(summary = "Cambiar el estado de un estudiante",
>             description = "Cambia solo el estado; el resto de los datos no se toca.")
>     @ApiResponse(responseCode = "200", description = "Estado actualizado")
>     @ApiResponse(responseCode = "400", description = "El estado es obligatorio", content = @Content)
>     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
>     @PatchMapping("/{id}/status")
>     public ResponseEntity<StudentResponse> changeStatus(
>             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id,
>             @Valid @RequestBody StudentStatusRequest request) {
>         return respond(service.changeStatus(id, request.status()));
>     }
>
>     @Operation(summary = "Eliminar un estudiante", security = @SecurityRequirement(name = "bearerAuth"))
>     @ApiResponse(responseCode = "204", description = "Estudiante eliminado")
>     @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese id", content = @Content)
>     @DeleteMapping("/{id}")
>     public ResponseEntity<Void> delete(
>             @Parameter(description = "Identificador del estudiante", example = "1") @PathVariable Long id) {
>         return service.delete(id)
>                 ? ResponseEntity.noContent().build()
>                 : ResponseEntity.notFound().build();
>     }
>
>     @Hidden
>     @GetMapping("/ping")
>     public String ping() {
>         return "pong";
>     }
>
>     private ResponseEntity<StudentResponse> respond(ServiceResult<StudentEntity> result) {
>         return switch (result.status()) {
>             case OK -> ResponseEntity.ok(StudentMapper.toResponse(result.value()));
>             case NOT_FOUND -> ResponseEntity.notFound().build();
>             case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
>         };
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
> import com.academy.business.service.ServiceStatus;
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
>             if (service.create(sample).status() == ServiceStatus.OK) {
>                 created++;
>             }
>         }
>         return Map.of("created", created);
>     }
> }
> ```
>
> </details>

</details>

<details>
<summary><strong>🏷️ Repaso de anotaciones de Swagger (OpenAPI)</strong></summary>

> | Anotación | Dónde se usa | Para qué sirve |
> |---|---|---|
> | `@Tag` | Clase del controlador | Nombre y descripción del grupo de operaciones |
> | `@Operation` | Método | Resumen, descripción, `deprecated` y seguridad |
> | `@ApiResponse` | Método (repetible) | Una respuesta posible |
> | `@Header` | Dentro de `@ApiResponse` | Una cabecera de la respuesta (`Location`) |
> | `@Content` · `@Schema` · `@ExampleObject` | Dentro de las anteriores | Cuerpo vacío, modelo y ejemplos |
> | `@Parameter` | Parámetro del método | Descripción y ejemplo |
> | `@ParameterObject` | Parámetro `Pageable` u objeto | Despliega sus campos como parámetros |
> | `@io.swagger…RequestBody` | Método | Documenta el cuerpo (con ejemplos) |
> | `@Schema` | Clase y componentes de los `record` (DTO) | Descripción, ejemplo, obligatorio, solo lectura |
> | `@Hidden` | Método o clase | Quita la operación del contrato |
> | `@SecurityRequirement` | Método o clase | Exige un esquema de seguridad |
> | Beans `OpenAPI` y `GroupedOpenApi` | `presentation.config` | Información general, seguridad y grupos |
>
> ```text
> persistence  ──▶  (sin Swagger: la entidad no cambia)
> business     ──▶  (sin Swagger)
> presentation ──▶  @Tag · @Operation · @ApiResponse · @Parameter · @ParameterObject · @Hidden · @SecurityRequirement
>                   @Schema en los DTO · config (OpenAPI, GroupedOpenApi)
> ```
>
> > 🔎 **Lectura clave.** Swagger vive **solo en `presentation`**. `business` y `persistence` no saben que existe.

</details>

➡️ **Anterior:** [Guía del Módulo 05 — DTO y estructura de controladores](../modulo-05/01-guia-dto-y-estructura-de-controladores.md)
