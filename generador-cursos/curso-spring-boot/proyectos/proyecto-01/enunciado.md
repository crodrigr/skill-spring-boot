# 🏆 Proyecto final — CoWorkHub: reservas de salas y espacios de trabajo

> **Nivel**: alto · **Módulos que integra**: 01 al 08 (todo el curso) · **Trabajo**: individual
> **Cómo se evalúa**: [Rúbrica](rubrica.md) · **Ayuda**: [Solución paso a paso](solucion/README.md)
> (usala solo después de haberlo intentado por tu cuenta).

## 🎯 Objetivo

Construir de principio a fin una API REST con Spring Boot que resuelva un problema de
negocio realista: analizarlo, especificarlo (requisitos y reglas de negocio), modelarlo
con JPA, exponerlo con controladores REST, manejar sus errores, documentarlo con
Swagger y protegerlo con Spring Security y JWT.

Es el proyecto final del curso: **cada tema de los módulos 01 a 08 tiene un lugar
concreto en él** (sección 3). Las secciones 8, 9 y 10 fijan exactamente qué requisitos
funcionales, qué endpoints y qué datos tenés que construir.

## 📑 Contenido

| # | Sección | # | Sección |
|---|---|---|---|
| 1 | [Situación](#1-situación) | 10 | [Contratos de datos](#10-contratos-de-datos) |
| 2 | [Alcance](#2-alcance) | 11 | [Requisitos no funcionales](#11-requisitos-no-funcionales) |
| 3 | [Qué se aplica de cada módulo](#3-qué-se-aplica-de-cada-módulo) | 12 | [Estructura esperada](#12-estructura-esperada) |
| 4 | [Análisis esperado](#4-análisis-esperado-primera-entrega) | 13 | [Fases de trabajo](#13-fases-de-trabajo) |
| 5 | [Actores y permisos](#5-actores-y-permisos) | 14 | [Entregables](#14-entregables) |
| 6 | [Modelo de dominio mínimo](#6-modelo-de-dominio-mínimo) | 15 | [Escenarios de aceptación](#15-escenarios-de-aceptación-mínimos) |
| 7 | [Reglas de negocio](#7-reglas-de-negocio) | 16 | [Preguntas de sustentación](#16-preguntas-de-sustentación) |
| 8 | [Requisitos funcionales](#8-requisitos-funcionales) | 17 | [Criterios de evaluación](#17-criterios-de-evaluación) |
| 9 | [Catálogo de endpoints](#9-catálogo-de-endpoints) | 18 | [Extensiones opcionales](#18-extensiones-opcionales) |

## 1. Situación

**CoWorkHub** es una red de espacios de coworking con varias sedes. Hoy gestiona las
reservas de salas en una hoja de cálculo compartida, y eso le genera problemas:

- Dos personas reservan la misma sala en el mismo horario.
- Nadie sabe cuántas horas de su plan ha consumido cada miembro en el mes.
- Las cancelaciones se cobran de forma distinta según quién atienda.
- Cualquier empleado puede modificar cualquier dato.

La empresa te contrata para construir el **backend (API REST)** de un sistema que
resuelva estos problemas. No se pide interfaz gráfica: el sistema se prueba con
Insomnia (o Postman), `curl` y Swagger UI.

## 2. Alcance

**Dentro del alcance**

- Catálogos: sedes, salas, equipamiento, servicios adicionales y planes de membresía.
- Miembros con su usuario de acceso, y su consumo mensual de horas.
- Búsqueda de salas disponibles.
- Ciclo de vida completo de una reserva: creación con costo calculado, servicios
  adicionales, confirmación, cancelación con cargo y cierre.
- Autenticación con JWT, autorización por rol y por dueño del recurso.
- Documentación interactiva de la API y datos de prueba cargados al arrancar.

**Fuera del alcance**

- Pagos en línea y facturación fiscal (el sistema solo calcula montos).
- Notificaciones por correo.
- Interfaz gráfica.
- Gestión por API de los usuarios del personal (`ADMIN` y `RECEPCION` vienen en los
  datos de prueba).
- Recuperación de contraseña y renovación de tokens (*refresh tokens*).

## 3. Qué se aplica de cada módulo

| Módulo | Temas del módulo | Cómo se aplican en CoWorkHub | Dónde se pide |
|---|---|---|---|
| **01** Introducción a Spring Boot | Frameworks e inversión de control, POO, Java moderno (streams, lambdas, `Optional`, `record`), Maven, *starters*, estructura del proyecto, anotaciones, contenedor IoC, inyección de dependencias, actuadores | Proyecto Maven con *starters*; `Main` con `@SpringBootApplication`; paquetes por capas; DTO como `record`; `Optional` en las búsquedas; streams en cálculos y conversiones; inyección por constructor; salud del servicio con Actuator | RNF-01, RNF-02, RNF-04, RF-15, fase 1 |
| **02** Dependencias y Java Beans | Dependencias directas y transitivas, inyección de dependencias, depender de abstracciones, resolución con `@Qualifier`, Java Beans, ciclo de vida de un bean (`@PostConstruct`, `@PreDestroy`), `@Component` y sus especializaciones | Servicios como interfaz + implementación; `@Configuration` con `@Bean` para `Clock` y `PasswordEncoder`; `@Value` para la configuración; un punto de inyección resuelto con `@Qualifier` o `@Primary`; validación de la configuración en `@PostConstruct`; `CalculadoraCostoReserva` como `@Component` | RNF-03, fase 1 |
| **03** Introducción a JPA | JPA, ORM, Hibernate, `EntityManager` y transacciones, Spring Data JPA (repositorios, consultas derivadas, `@Query` con JPQL, paginación y orden), anotaciones JPA, `ddl-auto` | 9 entidades y sus repositorios; consultas derivadas (`existsBy…`, `findBy…`); JPQL para solapamientos y disponibilidad; listado de miembros paginado y ordenable; valor de `ddl-auto` justificado | RNF-05, RNF-11, RF-02, RF-03, fase 2 |
| **04** Gestión de bases de datos con JPA | Spring Initializr, conexión, `@OneToOne`, `@OneToMany`/`@ManyToOne`, `@ManyToMany`, lado dueño, `fetch`, `cascade`, `orphanRemoval`, entidad intermedia, CRUD completo, inicialización del servidor | `Miembro`↔`Usuario` (1↔1), `Sede`→`Sala` (1→N), `Sala`↔`Equipamiento` (N↔N), `Reserva`→`DetalleReserva` con `cascade` y `orphanRemoval`; CRUD de catálogos; `data.sql`; guía de diagnóstico de arranque en el README | §6, RNF-05, RNF-12, RNF-15, fase 2 |
| **05** Diseño de API REST | HTTP (verbos y códigos), principios REST, MVC y capas, `@RestController` y *mappings*, `@PathVariable`, `@RequestParam`, `@RequestBody`, `ResponseEntity`, DTO, pruebas con `curl` | Los 47 endpoints de la §9 con su verbo y su código; DTO de entrada y de salida con mapeadores; validación con Bean Validation; colección de pruebas | §8, §9, §10, RNF-08, fase 3 |
| **06** Documentación con Swagger | OpenAPI (`paths`, `components`, `servers`), Swagger UI, documentación manual (SwaggerHub) frente a automática (springdoc), propiedades de springdoc, esquemas | springdoc con sus cuatro propiedades; `info`, `servers` y esquema de seguridad *bearer*; `@Tag`, `@Operation`, `@ApiResponse` y `@Schema`; `openapi.json` exportado y validado en un editor de OpenAPI | RF-13, RNF-10, fase 5 |
| **07** Manejo de excepciones | `Throwable`, `Exception` y `Error`; excepciones por capa; `@ResponseStatus`, `@ExceptionHandler`, `@ControllerAdvice` y cuándo usar cada uno | Excepciones de negocio propias con `@ResponseStatus`; un `@ExceptionHandler` local en el login; un `@RestControllerAdvice` global con un único cuerpo de error y un catálogo de códigos | RNF-07, §10.3, fase 4 |
| **08** Spring Security y JWT | Arquitectura de Spring Security (cadena de filtros, `AuthenticationManager`, *providers*, `PasswordEncoder`, `UserDetailsService`, `SecurityContextHolder`), *stateful* frente a *stateless*, `DelegatingFilterProxy`, `FilterChainProxy` y `SecurityFilterChain`, Basic Auth, JWT (estructura, firma y ciclo de vida) | Primero Basic Auth (fase 6a) y después JWT (fase 6b); BCrypt; roles y dueño del recurso con `@PreAuthorize`; respuestas `401` y `403` con el cuerpo estándar; `/auth/perfil` lee el usuario del contexto de seguridad | RF-11, RF-12, RF-14, RNF-09, fase 6 |

Los temas conceptuales que no se ven en el código (frameworks frente a librerías, Maven
frente a Gradle, *stateful* frente a *stateless*…) se evalúan en la
[sustentación](#16-preguntas-de-sustentación).

## 4. Análisis esperado (primera entrega)

Antes de programar, entregá un documento de análisis (`docs/analisis.md`) con:

1. **Actores y casos de uso.** Actores: Administrador, Recepcionista y Miembro.
   Diagrama y descripción breve de cada caso de uso.
2. **Glosario del dominio.** Definí reserva, plan, horas incluidas, horas excedentes,
   solapamiento y cargo por cancelación.
3. **Modelo de dominio (diagrama entidad-relación).** Justificá qué lado es el dueño
   de cada relación y qué `fetch` y `cascade` elegiste.
4. **Matriz de trazabilidad.** Relacioná cada RF con las RN y los endpoints (E-xx) que
   lo implementan.
5. **Supuestos y decisiones.** Toda ambigüedad del enunciado que resuelvas por tu
   cuenta debe quedar registrada.
6. **Fuera de alcance.** Lo de la sección 2, más lo que agregues.
7. **Cobertura de módulos.** Para cada fila de la sección 3, la clase o el archivo donde
   lo aplicaste (se completa al final del proyecto).

## 5. Actores y permisos

| Actor | Rol | Qué hace |
|---|---|---|
| Administrador | `ADMIN` | Gestiona los catálogos. Puede hacer todo lo que hace recepción. |
| Recepcionista | `RECEPCION` | Registra y gestiona miembros. Opera todas las reservas: crea, confirma, cancela y completa. |
| Miembro | `MIEMBRO` | Consulta catálogos y disponibilidad. Crea, consulta, modifica y cancela **solo sus propias** reservas, y consulta su propio consumo. |

En el resto del documento, **personal** significa `ADMIN` o `RECEPCION`.

**Quién puede hacer qué (RN-13)**

| Operación | `ADMIN` | `RECEPCION` | `MIEMBRO` |
|---|---|---|---|
| Iniciar sesión, ver la salud del servicio y la documentación | ✅ público | ✅ público | ✅ público |
| Consultar su propio perfil | ✅ | ✅ | ✅ |
| Leer catálogos y consultar disponibilidad | ✅ | ✅ | ✅ |
| Crear, modificar o eliminar catálogos | ✅ | ❌ `403` | ❌ `403` |
| Listar, registrar, actualizar, suspender, activar o eliminar miembros | ✅ | ✅ | ❌ `403` |
| Ver la ficha o el consumo de un miembro | ✅ | ✅ | Solo los suyos |
| Crear una reserva | ✅ | ✅ | Solo a su nombre |
| Ver, listar por miembro, modificar servicios o cancelar una reserva | ✅ | ✅ | Solo las suyas |
| Listar reservas por sala y fecha o por estado; confirmar; completar | ✅ | ✅ | ❌ `403` |

## 6. Modelo de dominio mínimo

| Entidad | Atributos principales | Relaciones que debe modelar |
|---|---|---|
| `Sede` | nombre, ciudad, dirección, horaApertura, horaCierre | 1 → N `Sala` |
| `Sala` | nombre, tipo, capacidad, tarifaPorHora, activa | N → 1 `Sede`; **N ↔ N** `Equipamiento` |
| `Equipamiento` | nombre (proyector, pizarra, videoconferencia…) | N ↔ N `Sala` |
| `PlanMembresia` | nombre, horasIncluidasMes, descuentoExcedente (%), maxReservasActivas | 1 → N `Miembro` |
| `Miembro` | documento (único), nombre, email (único), estado | N → 1 `PlanMembresia`; **1 ↔ 1** `Usuario` |
| `Usuario` | nombreUsuario (único), contraseña codificada, rol | 1 ↔ 1 `Miembro` (opcional para el personal) |
| `Reserva` | inicio, fin, asistentes, estado, costoSala, costoServicios, costoTotal, cargoCancelacion | N → 1 `Sala`; N → 1 `Miembro`; 1 → N `DetalleReserva` |
| `ServicioAdicional` | nombre (catering, impresión, soporte técnico…), precioUnitario | 1 → N `DetalleReserva` |
| `DetalleReserva` | cantidad, precioUnitarioAplicado | Entidad intermedia entre `Reserva` y `ServicioAdicional`. Con `cascade` y `orphanRemoval` desde `Reserva`. |

**Enumeraciones**

| Enumeración | Valores |
|---|---|
| `TipoSala` | `SALA_REUNION`, `OFICINA_PRIVADA`, `ESCRITORIO` |
| `EstadoMiembro` | `ACTIVO`, `SUSPENDIDO` |
| `EstadoReserva` | `PENDIENTE`, `CONFIRMADA`, `COMPLETADA`, `CANCELADA` |
| `Rol` | `ADMIN`, `RECEPCION`, `MIEMBRO` |

Podés agregar los atributos que necesites (por ejemplo, para poder devolver las horas
consumidas al cancelar); registrá el motivo en el análisis.

## 7. Reglas de negocio

| Código | Regla |
|---|---|
| **RN-01** | Una sala no puede tener dos reservas `PENDIENTE` o `CONFIRMADA` que se solapen. Hay solapamiento si `inicioNuevo < finExistente` y `finNuevo > inicioExistente`. Si una reserva termina justo cuando empieza otra, **no** hay solapamiento. La regla se cumple aunque lleguen dos solicitudes simultáneas. |
| **RN-02** | La duración mínima es de 30 min, la máxima de 8 h y debe ser múltiplo de 30 min. La reserva debe empezar y terminar el mismo día. |
| **RN-03** | La reserva debe caer dentro del horario de la sede, debe ser futura y hacerse con al menos 1 h de anticipación. |
| **RN-04** | No se puede reservar una sala inactiva. Los asistentes no pueden superar la capacidad de la sala. |
| **RN-05** | Solo un miembro `ACTIVO` puede reservar. |
| **RN-06** | Un miembro no puede tener más reservas activas futuras que `maxReservasActivas` de su plan. |
| **RN-07** | **Costo.** Las horas del mes ya cubiertas por `horasIncluidasMes` del plan no se cobran. Las horas excedentes se cobran a `tarifaPorHora × (1 − descuentoExcedente)`. Los servicios adicionales suman `cantidad × precioUnitario`. Al crear el detalle, `precioUnitarioAplicado` guarda el precio vigente en ese momento: cambiar el precio después no altera las reservas existentes. |
| **RN-08** | **Cancelación.** Con 24 h o más de anticipación no hay cargo y las horas incluidas se devuelven al consumo del mes. Entre 24 h y 2 h se cobra el 50 % del costo de la sala. Con menos de 2 h, o si la reserva ya empezó, no se puede cancelar. |
| **RN-09** | **Estados.** `PENDIENTE → CONFIRMADA → COMPLETADA`. También `PENDIENTE/CONFIRMADA → CANCELADA`. Las transiciones son irreversibles y cualquier otra se rechaza. |
| **RN-10** | Los servicios adicionales solo se pueden agregar o quitar mientras la reserva esté `PENDIENTE`. |
| **RN-11** | Son únicos: el documento del miembro; su email, sin distinguir mayúsculas; el nombre de usuario; el nombre de la sala dentro de su sede; y el nombre de cada equipamiento, servicio adicional y plan. |
| **RN-12** | No se puede eliminar una sede que tenga salas, un plan que tenga miembros, un equipamiento asignado a alguna sala, un servicio adicional usado en alguna reserva, ni una sala o un miembro con reservas activas o futuras. Decidí, y registrá como supuesto, qué pasa con los que solo tienen historial. |
| **RN-13** | Un miembro solo ve y opera sus propias reservas y su propia ficha. `RECEPCION` opera todas las reservas y los miembros. Solo `ADMIN` modifica catálogos. El detalle está en la tabla de permisos de la sección 5. |

## 8. Requisitos funcionales

### 8.1 Resumen

| RF | Requisito | Actores | Endpoints |
|---|---|---|---|
| **RF-01** | Gestionar los catálogos | Modifica `ADMIN`; leen todos | E-06 a E-30 |
| **RF-02** | Gestionar los miembros | Personal; el miembro ve su ficha | E-31 a E-37 |
| **RF-03** | Consultar la disponibilidad de salas | Todos | E-39 |
| **RF-04** | Crear una reserva | Personal; el miembro, a su nombre | E-40 |
| **RF-05** | Consultar reservas | Personal; el miembro, las suyas | E-41, E-42 |
| **RF-06** | Agregar o quitar servicios de una reserva | Personal; el miembro, las suyas | E-43, E-44 |
| **RF-07** | Confirmar una reserva | Personal | E-45 |
| **RF-08** | Cancelar una reserva | Personal; el miembro, las suyas | E-46 |
| **RF-09** | Completar una reserva | Personal | E-47 |
| **RF-10** | Consultar el consumo mensual de un miembro | Personal; el miembro, el suyo | E-38 |
| **RF-11** | Iniciar sesión | Público | E-01 |
| **RF-12** | Proteger la API por rol y por dueño | — | Todos, salvo los públicos |
| **RF-13** | Documentar la API | Público | E-04, E-05 |
| **RF-14** | Consultar el perfil del usuario autenticado | Todos | E-02 |
| **RF-15** | Informar la salud del servicio | Público | E-03 |

### 8.2 Detalle

#### RF-01 · Gestionar los catálogos

El administrador crea, consulta, actualiza y elimina los cinco catálogos. Cualquier
usuario autenticado puede leerlos. Los listados salen ordenados por nombre.

| Catálogo | Endpoints | Datos de entrada | Reglas propias |
|---|---|---|---|
| Sedes | E-06 a E-10 | nombre, ciudad, dirección, hora de apertura y de cierre | La apertura es anterior al cierre (`400`). No se elimina si tiene salas (RN-12). |
| Salas | E-11 a E-15 | nombre, tipo, capacidad, tarifa por hora, activa, sede y lista de ids de equipamiento | Nombre único dentro de su sede (RN-11). La sede y cada equipamiento deben existir (`404`). Se puede listar por sede. Una sala desactivada deja de aparecer en la disponibilidad y no se puede reservar, pero sus reservas existentes no cambian. |
| Equipamiento | E-16 a E-20 | nombre | Nombre único (RN-11). No se elimina si está asignado a una sala (RN-12). |
| Servicios adicionales | E-21 a E-25 | nombre y precio unitario | Nombre único (RN-11). Cambiar el precio no altera las reservas existentes (RN-07). No se elimina si se usó en una reserva (RN-12). |
| Planes | E-26 a E-30 | nombre, horas incluidas por mes, % de descuento sobre las horas excedentes, máximo de reservas activas | Nombre único (RN-11). No se elimina si tiene miembros (RN-12). |

#### RF-02 · Gestionar los miembros

- **Registrar** (E-33): crea el `Miembro` y su `Usuario` con rol `MIEMBRO` **en una sola
  transacción**. La contraseña se guarda codificada con BCrypt y el estado inicial es
  `ACTIVO`. Valida RN-11 (documento, email y nombre de usuario) y que el plan exista.
- **Listar** (E-31): **paginado y ordenable**. Filtros opcionales: `nombre` (contiene el
  texto, sin distinguir mayúsculas) y `estado`.
- **Consultar** (E-32), **actualizar** nombre, email y plan (E-34), **suspender** y
  **activar** (E-35, E-36) y **eliminar** (E-37, RN-12).
- Un miembro suspendido conserva sus reservas, pero no puede crear nuevas (RN-05).
- Ninguna respuesta incluye la contraseña ni su hash.

#### RF-03 · Consultar la disponibilidad de salas

- **Entrada** (E-39): obligatorios `sedeId`, `fecha` (`yyyy-MM-dd`), `horaInicio` y
  `horaFin` (`HH:mm`); opcionales `capacidadMinima` (por defecto 1) y `equipamientoIds`
  (la sala debe tener **todos** los indicados).
- **Salida**: las salas activas de la sede, con capacidad suficiente y sin reservas
  `PENDIENTE` o `CONFIRMADA` que se solapen con el rango (RN-01, RN-04).
- Si el rango cae fuera del horario de la sede, la lista sale vacía (RN-03). Si falta un
  parámetro obligatorio o `horaInicio` no es anterior a `horaFin`, `400`. Si la sede no
  existe, `404`.

#### RF-04 · Crear una reserva

- **Entrada** (E-40): miembro, sala, inicio, fin, asistentes y, opcionalmente, una lista
  de servicios adicionales con su cantidad.
- **Proceso**: valida la forma de los datos (`400`), que la sala, el miembro y los
  servicios existan (`404`) y las reglas RN-01 a RN-06 (`409`). Calcula los costos con
  RN-07 y guarda la reserva en estado `PENDIENTE`, junto con sus detalles, **en una sola
  transacción**.
- **Salida**: `201` con la reserva creada: `costoSala`, `costoServicios`, `costoTotal` y
  el detalle de cada servicio.
- Un miembro solo puede reservar a su propio nombre (RN-13).

#### RF-05 · Consultar reservas

- **Por id** (E-41).
- **Listar** (E-42) con **uno** de estos filtros: `miembroId`; `salaId` junto con
  `fecha`; o `estado`. Sin filtro, `400`. Los resultados salen ordenados por `inicio`.
- Un miembro solo consulta sus reservas y solo puede filtrar por su propio `miembroId`.

#### RF-06 · Agregar o quitar servicios de una reserva

- **Agregar** (E-43): servicio y cantidad. **Quitar** (E-44): por id del servicio.
- Solo mientras la reserva esté `PENDIENTE` (RN-10).
- Recalcula `costoServicios` y `costoTotal`. Los detalles que ya existían conservan su
  `precioUnitarioAplicado`.
- Agregar un servicio que la reserva ya tiene: decidí si suma la cantidad o se rechaza,
  y registralo como supuesto. Quitar un servicio que la reserva no tiene: `404`.

#### RF-07 · Confirmar una reserva

E-45. Solo el personal. `PENDIENTE → CONFIRMADA`; cualquier otro estado de partida,
`409` (RN-09).

#### RF-08 · Cancelar una reserva

E-46. El personal o el dueño de la reserva. Aplica RN-08 y RN-09. La respuesta incluye
el `cargoCancelacion` calculado. Cuando corresponde, las horas incluidas vuelven al
consumo del mes del miembro.

#### RF-09 · Completar una reserva

E-47. Solo el personal. `CONFIRMADA → COMPLETADA`; cualquier otro estado de partida,
`409` (RN-09).

#### RF-10 · Consultar el consumo mensual de un miembro

- **Entrada** (E-38): `mes` opcional con el formato `yyyy-MM`. Si no se indica, se usa el
  mes actual según el reloj de la aplicación.
- **Salida**: horas incluidas en el plan, horas usadas, horas incluidas usadas, horas
  restantes y horas excedentes del mes.
- Cuentan las reservas del mes cuyas horas no fueron devueltas (RN-08). El personal ve
  el consumo de cualquier miembro; un miembro, solo el suyo.

#### RF-11 · Iniciar sesión

E-01. Recibe nombre de usuario y contraseña. Si son correctos, devuelve un **JWT firmado**
con el usuario (`sub`), el rol, la fecha de emisión y la de vencimiento (60 min), más el
rol y los minutos de validez. Si no lo son, `401` con el mismo mensaje exista o no el
usuario (no se revela cuál de los dos datos falló).

#### RF-12 · Proteger la API por rol y por dueño

- Todos los endpoints, salvo los públicos (E-01, E-03, E-04 y E-05), exigen el
  encabezado `Authorization: Bearer <token>`.
- Sin token, o con un token alterado o vencido: `401`. Con un token válido pero sin
  permiso (por rol o porque el recurso es de otro miembro): `403`.
- Los permisos son los de la tabla de la sección 5. Ambos errores usan el cuerpo
  estándar de la §10.3.

#### RF-13 · Documentar la API

E-04 y E-05. Toda la API queda documentada en Swagger UI: cada endpoint con su grupo
(*tag*), un resumen, sus parámetros, sus respuestas (también las de error) y ejemplos.
Swagger UI permite autenticarse con el token (botón **Authorize**); los endpoints
públicos se muestran sin candado.

#### RF-14 · Consultar el perfil del usuario autenticado

E-02. Devuelve el nombre de usuario, el rol y, si es un miembro, su `miembroId` (para el
personal, `null`). El usuario se obtiene del **contexto de seguridad** (el token ya
validado), nunca de un parámetro ni del cuerpo. Así un miembro averigua su `miembroId`
para usar E-38 y E-42.

#### RF-15 · Informar la salud del servicio

E-03. Público. Responde `{"status":"UP"}` cuando la aplicación y la base de datos
funcionan. Es el único endpoint de Actuator expuesto, y no muestra detalles a usuarios
anónimos.

## 9. Catálogo de endpoints

**URL base:** `http://localhost:8080`. Los cuerpos van en JSON (UTF-8). Las fechas y
horas usan ISO-8601 sin zona horaria: `2030-06-04T16:00:00`, `2030-06-04`, `16:00`. Los
montos llevan dos decimales.

**Convenciones**

- Recursos en plural, en minúsculas y con guiones (`/api/servicios-adicionales`).
- El id va en la ruta; los filtros, la paginación y el orden, como parámetros de
  consulta.
- Crear → `POST` y `201`. Leer → `GET` y `200`. Reemplazar → `PUT` y `200`. Eliminar →
  `DELETE` y `200` sin cuerpo.
- Un cambio de estado simple del miembro → `PATCH` (`/suspender`, `/activar`).
- Una acción de negocio sobre la reserva, que valida reglas y recalcula montos → `POST`
  a un subrecurso (`/confirmar`, `/cancelar`, `/completar`).
- Ninguna respuesta devuelve una entidad JPA: siempre un DTO (§10).

**Columna Acceso:** *Público* (sin token) · *Autenticado* (cualquier rol) · *ADMIN* ·
*Personal* (`ADMIN` o `RECEPCION`) · *Personal o dueño* (o el propio miembro).
Los `401` y `403` por falta de token o de rol valen para todo endpoint no público y no se
repiten en cada fila; la columna **Errores propios** lista los demás.

### 9.1 Autenticación y plataforma

| # | Método | Ruta | Qué hace | Acceso | Respuesta | Errores propios | RF |
|---|---|---|---|---|---|---|---|
| E-01 | `POST` | `/auth/login` | Inicia sesión y devuelve un JWT | Público | `200` `RespuestaLogin` | `400`, `401` | RF-11 |
| E-02 | `GET` | `/auth/perfil` | Datos del usuario autenticado | Autenticado | `200` `RespuestaPerfil` | — | RF-14 |
| E-03 | `GET` | `/actuator/health` | Salud del servicio | Público | `200` `{"status":"UP"}` | — | RF-15 |
| E-04 | `GET` | `/doc/swagger-ui.html` | Swagger UI | Público | `200` (página web) | — | RF-13 |
| E-05 | `GET` | `/v3/api-docs` | Documento OpenAPI | Público | `200` (JSON) | — | RF-13 |

### 9.2 Sedes

| # | Método | Ruta | Qué hace | Acceso | Respuesta | Errores propios | RF |
|---|---|---|---|---|---|---|---|
| E-06 | `GET` | `/api/sedes` | Lista las sedes | Autenticado | `200` lista de `RespuestaSede` | — | RF-01 |
| E-07 | `GET` | `/api/sedes/{id}` | Consulta una sede | Autenticado | `200` `RespuestaSede` | `404` | RF-01 |
| E-08 | `POST` | `/api/sedes` | Crea una sede | ADMIN | `201` `RespuestaSede` | `400` | RF-01 |
| E-09 | `PUT` | `/api/sedes/{id}` | Actualiza una sede | ADMIN | `200` `RespuestaSede` | `400`, `404` | RF-01 |
| E-10 | `DELETE` | `/api/sedes/{id}` | Elimina una sede | ADMIN | `200` sin cuerpo | `404`, `409` RN-12 | RF-01 |

### 9.3 Salas

| # | Método | Ruta | Qué hace | Acceso | Respuesta | Errores propios | RF |
|---|---|---|---|---|---|---|---|
| E-11 | `GET` | `/api/salas` · `?sedeId=` | Lista las salas, o las de una sede | Autenticado | `200` lista de `RespuestaSala` | — | RF-01 |
| E-12 | `GET` | `/api/salas/{id}` | Consulta una sala | Autenticado | `200` `RespuestaSala` | `404` | RF-01 |
| E-13 | `POST` | `/api/salas` | Crea una sala con su equipamiento | ADMIN | `201` `RespuestaSala` | `400`, `404` (sede o equipamiento), `409` RN-11 | RF-01 |
| E-14 | `PUT` | `/api/salas/{id}` | Actualiza una sala y su equipamiento | ADMIN | `200` `RespuestaSala` | `400`, `404`, `409` RN-11 | RF-01 |
| E-15 | `DELETE` | `/api/salas/{id}` | Elimina una sala | ADMIN | `200` sin cuerpo | `404`, `409` RN-12 | RF-01 |

### 9.4 Equipamiento

| # | Método | Ruta | Qué hace | Acceso | Respuesta | Errores propios | RF |
|---|---|---|---|---|---|---|---|
| E-16 | `GET` | `/api/equipamientos` | Lista el equipamiento | Autenticado | `200` lista de `RespuestaEquipamiento` | — | RF-01 |
| E-17 | `GET` | `/api/equipamientos/{id}` | Consulta un equipamiento | Autenticado | `200` `RespuestaEquipamiento` | `404` | RF-01 |
| E-18 | `POST` | `/api/equipamientos` | Crea un equipamiento | ADMIN | `201` `RespuestaEquipamiento` | `400`, `409` RN-11 | RF-01 |
| E-19 | `PUT` | `/api/equipamientos/{id}` | Actualiza un equipamiento | ADMIN | `200` `RespuestaEquipamiento` | `400`, `404`, `409` RN-11 | RF-01 |
| E-20 | `DELETE` | `/api/equipamientos/{id}` | Elimina un equipamiento | ADMIN | `200` sin cuerpo | `404`, `409` RN-12 | RF-01 |

### 9.5 Servicios adicionales

| # | Método | Ruta | Qué hace | Acceso | Respuesta | Errores propios | RF |
|---|---|---|---|---|---|---|---|
| E-21 | `GET` | `/api/servicios-adicionales` | Lista los servicios | Autenticado | `200` lista de `RespuestaServicioAdicional` | — | RF-01 |
| E-22 | `GET` | `/api/servicios-adicionales/{id}` | Consulta un servicio | Autenticado | `200` `RespuestaServicioAdicional` | `404` | RF-01 |
| E-23 | `POST` | `/api/servicios-adicionales` | Crea un servicio | ADMIN | `201` `RespuestaServicioAdicional` | `400`, `409` RN-11 | RF-01 |
| E-24 | `PUT` | `/api/servicios-adicionales/{id}` | Actualiza un servicio (incluido su precio) | ADMIN | `200` `RespuestaServicioAdicional` | `400`, `404`, `409` RN-11 | RF-01 |
| E-25 | `DELETE` | `/api/servicios-adicionales/{id}` | Elimina un servicio | ADMIN | `200` sin cuerpo | `404`, `409` RN-12 | RF-01 |

### 9.6 Planes de membresía

| # | Método | Ruta | Qué hace | Acceso | Respuesta | Errores propios | RF |
|---|---|---|---|---|---|---|---|
| E-26 | `GET` | `/api/planes` | Lista los planes | Autenticado | `200` lista de `RespuestaPlan` | — | RF-01 |
| E-27 | `GET` | `/api/planes/{id}` | Consulta un plan | Autenticado | `200` `RespuestaPlan` | `404` | RF-01 |
| E-28 | `POST` | `/api/planes` | Crea un plan | ADMIN | `201` `RespuestaPlan` | `400`, `409` RN-11 | RF-01 |
| E-29 | `PUT` | `/api/planes/{id}` | Actualiza un plan | ADMIN | `200` `RespuestaPlan` | `400`, `404`, `409` RN-11 | RF-01 |
| E-30 | `DELETE` | `/api/planes/{id}` | Elimina un plan | ADMIN | `200` sin cuerpo | `404`, `409` RN-12 | RF-01 |

### 9.7 Miembros

| # | Método | Ruta | Qué hace | Acceso | Respuesta | Errores propios | RF |
|---|---|---|---|---|---|---|---|
| E-31 | `GET` | `/api/miembros` · `?nombre=&estado=&page=&size=&sort=` | Lista paginada de miembros, con filtros | Personal | `200` `Pagina<RespuestaMiembro>` | `400` (`sort` o `estado` inválidos) | RF-02 |
| E-32 | `GET` | `/api/miembros/{id}` | Consulta un miembro | Personal o dueño | `200` `RespuestaMiembro` | `404` | RF-02 |
| E-33 | `POST` | `/api/miembros` | Registra un miembro con su usuario | Personal | `201` `RespuestaMiembro` | `400`, `404` (plan), `409` RN-11 | RF-02 |
| E-34 | `PUT` | `/api/miembros/{id}` | Actualiza nombre, email y plan | Personal | `200` `RespuestaMiembro` | `400`, `404`, `409` RN-11 | RF-02 |
| E-35 | `PATCH` | `/api/miembros/{id}/suspender` | Suspende al miembro | Personal | `200` `RespuestaMiembro` | `404` | RF-02 |
| E-36 | `PATCH` | `/api/miembros/{id}/activar` | Reactiva al miembro | Personal | `200` `RespuestaMiembro` | `404` | RF-02 |
| E-37 | `DELETE` | `/api/miembros/{id}` | Elimina un miembro y su usuario | Personal | `200` sin cuerpo | `404`, `409` RN-12 | RF-02 |
| E-38 | `GET` | `/api/miembros/{id}/consumo` · `?mes=yyyy-MM` | Resumen mensual de consumo | Personal o dueño | `200` `ResumenConsumo` | `400` (mes inválido), `404` | RF-10 |

Paginación de E-31: `page` empieza en 0; `size` vale 10 por defecto y como máximo 50;
`sort` acepta `nombre`, `documento`, `email` o `estado`, con `,asc` o `,desc` (por defecto
`nombre,asc`).

### 9.8 Disponibilidad

| # | Método | Ruta | Qué hace | Acceso | Respuesta | Errores propios | RF |
|---|---|---|---|---|---|---|---|
| E-39 | `GET` | `/api/disponibilidad` · `?sedeId=&fecha=&horaInicio=&horaFin=[&capacidadMinima=][&equipamientoIds=]` | Salas libres de una sede en una fecha y rango horario | Autenticado | `200` lista de `RespuestaSala` | `400`, `404` (sede) | RF-03 |

### 9.9 Reservas

| # | Método | Ruta | Qué hace | Acceso | Respuesta | Errores propios | RF |
|---|---|---|---|---|---|---|---|
| E-40 | `POST` | `/api/reservas` | Crea una reserva con sus servicios | Personal, o el miembro a su nombre | `201` `RespuestaReserva` | `400`, `404`, `409` RN-01 a RN-07 | RF-04 |
| E-41 | `GET` | `/api/reservas/{id}` | Consulta una reserva | Personal o dueño | `200` `RespuestaReserva` | `404` | RF-05 |
| E-42 | `GET` | `/api/reservas` · `?miembroId=` · `?salaId=&fecha=` · `?estado=` | Lista reservas con uno de los tres filtros | Personal; el miembro, solo con su `miembroId` | `200` lista de `RespuestaReserva` | `400` (sin filtro o filtro inválido) | RF-05 |
| E-43 | `POST` | `/api/reservas/{id}/servicios` | Agrega un servicio adicional | Personal o dueño | `200` `RespuestaReserva` | `400`, `404`, `409` RN-10 | RF-06 |
| E-44 | `DELETE` | `/api/reservas/{id}/servicios/{servicioId}` | Quita un servicio adicional | Personal o dueño | `200` `RespuestaReserva` | `404`, `409` RN-10 | RF-06 |
| E-45 | `POST` | `/api/reservas/{id}/confirmar` | `PENDIENTE → CONFIRMADA` | Personal | `200` `RespuestaReserva` | `404`, `409` RN-09 | RF-07 |
| E-46 | `POST` | `/api/reservas/{id}/cancelar` | Cancela y calcula el cargo | Personal o dueño | `200` `RespuestaReserva` | `404`, `409` RN-08, RN-09 | RF-08 |
| E-47 | `POST` | `/api/reservas/{id}/completar` | `CONFIRMADA → COMPLETADA` | Personal | `200` `RespuestaReserva` | `404`, `409` RN-09 | RF-09 |

**Total: 47 endpoints**: 42 de negocio, 2 de autenticación y 3 de plataforma (salud y
documentación).

## 10. Contratos de datos

Todos los DTO son `record` del paquete `presentacion.dto`. Los de entrada se validan con
Bean Validation (`@Valid` en el controlador); los de salida se arman con un mapeador a
partir de las entidades.

### 10.1 DTO de entrada

| DTO | Campos y validaciones | Lo usan |
|---|---|---|
| `SolicitudSede` | `nombre` obligatorio (máx. 80) · `ciudad` obligatorio (máx. 60) · `direccion` obligatorio (máx. 120) · `horaApertura` y `horaCierre` obligatorias (`HH:mm`) | E-08, E-09 |
| `SolicitudSala` | `nombre` obligatorio (máx. 80) · `tipo` obligatorio (`TipoSala`) · `capacidad` obligatoria, mayor que 0 · `tarifaPorHora` obligatoria, mayor que 0 · `activa` opcional (por defecto `true`) · `sedeId` obligatorio · `equipamientoIds` opcional (lista de ids) | E-13, E-14 |
| `SolicitudEquipamiento` | `nombre` obligatorio (máx. 60) | E-18, E-19 |
| `SolicitudServicioAdicional` | `nombre` obligatorio (máx. 60) · `precioUnitario` obligatorio, mayor que 0 | E-23, E-24 |
| `SolicitudPlan` | `nombre` obligatorio (máx. 60) · `horasIncluidasMes` obligatorio, 0 o más · `descuentoExcedente` obligatorio, entre 0 y 100 · `maxReservasActivas` obligatorio, mayor que 0 | E-28, E-29 |
| `SolicitudMiembro` | `documento` obligatorio (máx. 20) · `nombre` obligatorio (máx. 100) · `email` obligatorio, con formato de email · `planId` obligatorio · `nombreUsuario` obligatorio (3 a 30 caracteres) · `contrasena` obligatoria (mínimo 6 caracteres) | E-33 |
| `SolicitudActualizarMiembro` | `nombre` obligatorio · `email` obligatorio, con formato de email · `planId` obligatorio | E-34 |
| `SolicitudReserva` | `miembroId`, `salaId`, `inicio` y `fin` obligatorios · `asistentes` obligatorio, mayor que 0 · `servicios` opcional: lista de `ItemServicio` (cada uno validado) | E-40 |
| `ItemServicio` | `servicioId` obligatorio · `cantidad` obligatoria, mayor que 0 | E-40, E-43 |
| `SolicitudLogin` | `nombreUsuario` y `contrasena` obligatorios | E-01 |

Un dato que falta o tiene un formato inválido es un `400`. Una regla de negocio violada
es un `409`, aunque los datos estén bien formados (por ejemplo, una reserva de 45 min).

### 10.2 DTO de salida

| DTO | Campos | Lo devuelven |
|---|---|---|
| `RespuestaSede` | `id`, `nombre`, `ciudad`, `direccion`, `horaApertura`, `horaCierre` | E-06 a E-09 |
| `RespuestaSala` | `id`, `nombre`, `tipo`, `capacidad`, `tarifaPorHora`, `activa`, `sedeId`, `sede` (nombre), `equipamientos` (lista de `RespuestaEquipamiento`) | E-11 a E-14, E-39 |
| `RespuestaEquipamiento` | `id`, `nombre` | E-16 a E-19 |
| `RespuestaServicioAdicional` | `id`, `nombre`, `precioUnitario` | E-21 a E-24 |
| `RespuestaPlan` | `id`, `nombre`, `horasIncluidasMes`, `descuentoExcedente`, `maxReservasActivas` | E-26 a E-29 |
| `RespuestaMiembro` | `id`, `documento`, `nombre`, `email`, `estado`, `planId`, `plan` (nombre), `nombreUsuario` | E-32 a E-36 |
| `RespuestaReserva` | `id`, `miembroId`, `miembro` (nombre), `salaId`, `sala` (nombre), `sede` (nombre), `inicio`, `fin`, `asistentes`, `estado`, `costoSala`, `costoServicios`, `costoTotal`, `cargoCancelacion`, `detalles` (lista de `RespuestaDetalleReserva`) | E-40 a E-47 |
| `RespuestaDetalleReserva` | `servicioId`, `servicio` (nombre), `cantidad`, `precioUnitarioAplicado`, `subtotal` | dentro de `RespuestaReserva` |
| `ResumenConsumo` | `miembroId`, `miembro`, `plan`, `mes`, `horasIncluidas`, `horasUsadas`, `horasIncluidasUsadas`, `horasRestantes`, `horasExcedentes` | E-38 |
| `RespuestaLogin` | `token`, `rol`, `expiraEnMinutos` | E-01 |
| `RespuestaPerfil` | `nombreUsuario`, `rol`, `miembroId` (`null` para el personal) | E-02 |
| `Pagina<T>` | `contenido` (lista de `T`), `pagina`, `tamano`, `totalElementos`, `totalPaginas` | E-31 |
| `RespuestaError` | `error` (mensaje), `codigo`, `timestamp` | todo error |

### 10.3 Cuerpo de error y catálogo de códigos

Todo error, de cualquier endpoint y también de la capa de seguridad, responde con el
mismo cuerpo:

```json
{"error": "La sala ya está reservada en ese horario", "codigo": "RN-01", "timestamp": "2030-06-03T13:00:00.123Z"}
```

El `error` es para personas y puede cambiar de redacción; el `codigo` es estable, para
que un programa decida qué hacer.

| `codigo` | HTTP | Cuándo |
|---|---|---|
| `SOLICITUD_INVALIDA` | `400` | JSON mal formado, campo obligatorio ausente, valor fuera de rango, tipo inválido (`/api/reservas/abc`), parámetro faltante, `sort` por un campo inexistente |
| `NO_AUTENTICADO` | `401` | Sin token, token alterado o vencido, credenciales incorrectas en el login |
| `ACCESO_DENEGADO` | `403` | El rol no alcanza, o el recurso es de otro miembro (RN-13) |
| `NO_ENCONTRADO` | `404` | El id no existe, o la ruta no existe |
| `METODO_NO_PERMITIDO` | `405` | Verbo no soportado en la ruta (por ejemplo, `PATCH /api/sedes/1`) |
| `TIPO_NO_SOPORTADO` | `415` | El cuerpo no es `application/json` |
| `RN-01` … `RN-12` | `409` | Se violó esa regla de negocio |
| `CONFLICTO_DE_DATOS` | `409` | La base de datos rechazó la operación por integridad (respaldo de RN-11 y RN-12) |
| `ERROR_INTERNO` | `500` | Un error inesperado. Nunca por una regla de negocio. |

### 10.4 Ejemplos

**Login** (E-01)

```http
POST /auth/login
Content-Type: application/json

{"nombreUsuario": "ana", "contrasena": "ana123"}
```

```json
{"token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbmEiLCJyb2wiOiJNSUVNQlJPIiwi…", "rol": "MIEMBRO", "expiraEnMinutos": 60}
```

**Perfil** (E-02, con el token de `ana`)

```json
{"nombreUsuario": "ana", "rol": "MIEMBRO", "miembroId": 1}
```

**Crear una reserva** (E-40, con el token de `ana`). Ana tiene el plan Profesional
(40 h incluidas por mes), así que las 2 h de sala no se cobran; los servicios, sí.

```http
POST /api/reservas
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9…
Content-Type: application/json

{
  "miembroId": 1,
  "salaId": 2,
  "inicio": "2030-06-04T16:00:00",
  "fin": "2030-06-04T18:00:00",
  "asistentes": 3,
  "servicios": [
    {"servicioId": 1, "cantidad": 2},
    {"servicioId": 2, "cantidad": 10}
  ]
}
```

```json
{
  "id": 16,
  "miembroId": 1, "miembro": "Ana Torres",
  "salaId": 2, "sala": "Sala Caribe", "sede": "Sede Centro",
  "inicio": "2030-06-04T16:00:00", "fin": "2030-06-04T18:00:00",
  "asistentes": 3,
  "estado": "PENDIENTE",
  "costoSala": 0.00, "costoServicios": 100.00, "costoTotal": 100.00, "cargoCancelacion": 0.00,
  "detalles": [
    {"servicioId": 1, "servicio": "Catering", "cantidad": 2, "precioUnitarioAplicado": 25.00, "subtotal": 50.00},
    {"servicioId": 2, "servicio": "Impresión", "cantidad": 10, "precioUnitarioAplicado": 5.00, "subtotal": 50.00}
  ]
}
```

**Página de miembros** (E-31, con el token de `recepcion`):
`GET /api/miembros?page=0&size=2&sort=nombre,asc`

```json
{
  "contenido": [
    {"id": 1, "documento": "1010101", "nombre": "Ana Torres", "email": "ana@coworkhub.test", "estado": "ACTIVO", "planId": 3, "plan": "Profesional", "nombreUsuario": "ana"},
    {"id": 4, "documento": "4040404", "nombre": "Carlos Peña", "email": "carlos@coworkhub.test", "estado": "ACTIVO", "planId": 4, "plan": "Corporativo", "nombreUsuario": null}
  ],
  "pagina": 0, "tamano": 2, "totalElementos": 5, "totalPaginas": 3
}
```

Los datos de los ejemplos suponen los datos de prueba de la solución y el reloj fijo en
`2030-06-03T08:00:00` (ver las pistas). Tus ids pueden variar.

## 11. Requisitos no funcionales

| Código | Categoría | Requisito |
|---|---|---|
| **RNF-01** | Tecnología | Java 21, Spring Boot 4.1.x y Maven, con el proyecto generado desde Spring Initializr. *Starters*: `webmvc`, `data-jpa`, `validation`, `security` y `actuator`; además, H2 en memoria, `springdoc-openapi-starter-webmvc-ui` 3.x y JJWT 0.12.x. Se ejecuta solo con `mvn spring-boot:run`, sin instalar nada más. |
| **RNF-02** | Arquitectura | Tres capas: presentación (`presentacion`: `controlador`, `dto`, `mapper`, `excepcion`), negocio (`negocio`: `model`, `service`, `impl`, `excepcion`) y persistencia (`persistence`: `repository`). `config` y `security` son transversales. Cada capa solo conoce a la de abajo: el controlador depende de la **interfaz** del servicio y nunca de un repositorio; el servicio no conoce HTTP. Ninguna entidad sale de la capa de presentación: se convierte en DTO con un mapeador. |
| **RNF-03** | Beans e inyección | Inyección **por constructor**, sin `@Autowired` en atributos. Cada servicio de negocio es una interfaz (`negocio/service`) con su implementación `@Service` (`negocio/impl`). Los beans de clases que no son tuyas (`Clock`, `PasswordEncoder`) se declaran con `@Bean` en una clase `@Configuration`. La configuración propia se lee con `@Value`. Al menos un punto de inyección con dos candidatos del mismo tipo se resuelve con `@Qualifier` o `@Primary`, y se justifica en el análisis. Al menos un bean valida su configuración en `@PostConstruct` y hace fallar el arranque si es inválida (por ejemplo, una clave JWT de menos de 32 caracteres). El cálculo de costos es un bean propio (`@Component`) inyectado en el servicio de reservas. |
| **RNF-04** | Java moderno | DTO como `record`. Las búsquedas por id usan `Optional` (`orElseThrow`), nunca devuelven `null`. Las transformaciones y los cálculos sobre colecciones usan streams y lambdas. El dinero se maneja con `BigDecimal`. |
| **RNF-05** | Persistencia | Anotaciones JPA completas: `@Table`, `@Column` con `nullable`, `length` o `unique` según corresponda, enumeraciones como texto (`@Enumerated(EnumType.STRING)`) y montos con `precision` y `scale`. Repositorios de Spring Data JPA con consultas derivadas, al menos una consulta `@Query` en JPQL y una consulta paginada (`Pageable`). El valor de `spring.jpa.hibernate.ddl-auto` se justifica en el análisis. La consola de H2 queda disponible durante el desarrollo. |
| **RNF-06** | Consistencia | Las operaciones que escriben más de una entidad (registrar un miembro, crear o cancelar una reserva) son `@Transactional`: una falla no deja datos a medias. Las de solo lectura usan `@Transactional(readOnly = true)`. RN-01 se cumple con solicitudes simultáneas. |
| **RNF-07** | Errores | Las reglas se validan en `negocio` y se informan con excepciones propias (`negocio/excepcion`), cada una con `@ResponseStatus` que declara su código. Un único `@RestControllerAdvice` (`presentacion/excepcion`) centraliza el manejo de errores, y **todas** las respuestas de error usan `RespuestaError`. Como único caso local, el controlador del login traduce las credenciales inválidas con su propio `@ExceptionHandler`. Los `401` y `403` de la cadena de filtros usan el mismo cuerpo (`AuthenticationEntryPoint` y `AccessDeniedHandler`). Ningún error responde `500` por una regla de negocio. |
| **RNF-08** | Códigos HTTP | `200` y `201` para éxito; `400` solicitud mal formada o datos inválidos; `401` sin autenticación; `403` sin permiso; `404` recurso o ruta inexistente; `405` método no permitido; `409` regla de negocio violada o duplicado; `415` tipo de contenido no soportado; `500` solo para errores inesperados. |
| **RNF-09** | Seguridad | Autenticación *stateless* (`SessionCreationPolicy.STATELESS`) con JWT firmado con HMAC. El token expira a los 60 min. La clave de firma y la expiración se leen de `application.properties`, con la advertencia de que la clave es solo educativa. Contraseñas codificadas con BCrypt. Un `UserDetailsService` carga los usuarios desde la base y el login verifica las credenciales con el `AuthenticationManager`. CSRF, Basic Auth y el formulario de login quedan desactivados al final, y el análisis explica por qué es seguro desactivar CSRF en esta API. Las reglas por dueño se resuelven con `@EnableMethodSecurity` y `@PreAuthorize`. Ninguna respuesta ni ningún log incluye contraseñas o tokens completos. |
| **RNF-10** | Documentación de la API | springdoc configurado con `springdoc.api-docs.enabled`, `springdoc.swagger-ui.enabled`, `springdoc.swagger-ui.path=/doc/swagger-ui.html` y `springdoc.packages-to-scan`. El documento OpenAPI tiene `info` (título, descripción, versión y contacto), `servers` y un esquema de seguridad *bearer* JWT. Cada controlador tiene `@Tag`, cada endpoint `@Operation` y sus respuestas de error (`@ApiResponse`), y los DTO tienen `@Schema` con ejemplos. |
| **RNF-11** | Rendimiento | Las colecciones usan `fetch = LAZY`. Los listados no provocan una consulta extra por fila (problema N+1): se resuelve con `@EntityGraph` o `JOIN FETCH`. El listado de miembros se pagina en la base de datos, no en memoria. Justificá en el análisis cualquier excepción. |
| **RNF-12** | Datos de prueba | Al arrancar se cargan, al menos: 2 sedes, 6 salas (una inactiva), 4 equipamientos, 3 servicios adicionales, 3 planes, 5 miembros (uno `SUSPENDIDO`), 4 usuarios (uno `ADMIN`, uno `RECEPCION` y dos `MIEMBRO`) y 15 reservas en distintos estados. Se recomienda un script SQL (`data.sql`) para los catálogos. Las contraseñas se guardan codificadas. |
| **RNF-13** | Configuración y operación | `application.properties` ordenado por secciones (servidor, base de datos, JPA, springdoc, JWT, Actuator, reloj de la aplicación). Las propiedades propias usan el prefijo `coworkhub.`. Actuator expone solo `health` (`management.endpoints.web.exposure.include=health`). |
| **RNF-14** | Mantenibilidad | Nombres en español y coherentes con el resto del curso. Sin código duplicado entre servicios. Cada clase tiene una sola responsabilidad. |
| **RNF-15** | Documentación del proyecto | El `README.md` explica cómo ejecutar el proyecto, qué usuarios de prueba existen, dónde está Swagger UI, el resumen de endpoints y una sección **"Si no arranca"** con los errores de arranque más comunes y cómo leer el `Caused by:` del log. |

## 12. Estructura esperada

```text
📁 coworkhub
├── 📄 pom.xml
├── 📄 README.md
├── 📁 docs
│   ├── 📄 analisis.md
│   ├── 📄 openapi.json
│   └── 📄 coleccion-coworkhub.json        (Insomnia o Postman)
└── 📁 src/main
    ├── 📁 java/com/coworkhub
    │   ├── 📄 Main.java
    │   ├── 📁 config                       (beans propios, OpenAPI, datos de ejemplo)
    │   ├── 📁 presentacion                 (capa de presentación)
    │   │   ├── 📁 controlador
    │   │   ├── 📁 dto                      (Solicitud*, Respuesta*, Pagina)
    │   │   ├── 📁 mapper                   (entidad → DTO de salida)
    │   │   └── 📁 excepcion                (@RestControllerAdvice)
    │   ├── 📁 negocio                      (capa de negocio)
    │   │   ├── 📁 model                    (entidades y enumeraciones)
    │   │   ├── 📁 service                  (interfaces de los servicios, CalculadoraCostoReserva)
    │   │   ├── 📁 impl                     (implementaciones @Service)
    │   │   └── 📁 excepcion                (excepciones de negocio)
    │   ├── 📁 persistence                  (capa de persistencia)
    │   │   └── 📁 repository
    │   └── 📁 security                     (autenticación y autorización)
    │       ├── 📁 presentacion
    │       │   └── 📁 controlador          (login y perfil)
    │       ├── 📁 negocio
    │       │   ├── 📁 model                (Usuario, Rol)
    │       │   └── 📁 service              (UserDetailsService, autorización por dueño)
    │       ├── 📁 persistence
    │       │   └── 📁 repository
    │       ├── 📁 config                   (SecurityFilterChain, 401 y 403)
    │       └── 📁 jwt                      (creación y validación del token, filtro)
    └── 📁 resources
        ├── 📄 application.properties
        └── 📄 data.sql
```

## 13. Fases de trabajo

Hacé **un commit al terminar cada fase**.

| Fase | Contenido | Módulos |
|---|---|---|
| 0 | Análisis, diagrama ER, trazabilidad y supuestos | — |
| 1 | Proyecto desde Spring Initializr, `pom.xml` con los *starters*, paquetes, `Main`, `application.properties`, clase `@Configuration` con los beans `Clock` y `PasswordEncoder`, `@Value`, `@PostConstruct` y la salud con Actuator | 01, 02 |
| 2 | Entidades y enumeraciones; relaciones 1↔1, 1→N y N↔N con entidad intermedia; `cascade`, `orphanRemoval` y `fetch`; repositorios con consultas derivadas, JPQL y paginación; datos semilla. Verificá las tablas en la consola de H2. | 03, 04 |
| 3 | Servicios (interfaz + implementación) con las reglas de negocio y la calculadora de costos; DTO de entrada y de salida con validaciones; mapeadores; controladores REST; reservas de ejemplo. Probá cada endpoint con `curl`. | 05 (y 01, 02) |
| 4 | Excepciones de negocio, `@ResponseStatus`, `@ExceptionHandler` y manejador global con el catálogo de códigos | 07 |
| 5 | Documentación con springdoc: propiedades, `info`, `servers`, *tags*, operaciones, respuestas, esquemas y ejemplos; exportar `docs/openapi.json` | 06 |
| 6a | Spring Security con **Basic Auth**: agregar el *starter*, observar la contraseña generada, cargar los usuarios desde la base con `UserDetailsService` y BCrypt, y proteger las rutas por rol | 08 |
| 6b | Reemplazar Basic Auth por **JWT**: login, filtro, configuración *stateless*, permisos por dueño con `@PreAuthorize`, `401` y `403` con el cuerpo estándar, `/auth/perfil` y el botón **Authorize** de Swagger | 08 |
| 7 | Escenarios de aceptación, colección de pruebas, `README.md` y cobertura de módulos en el análisis | todos |

## 14. Entregables

1. **Documento de análisis** (`docs/analisis.md`, sección 4).
2. **Proyecto completo** en un repositorio, con un commit por fase (al menos 9).
3. **Colección de pruebas** de Insomnia o Postman, exportada en `docs/`, con los
   escenarios de la sección 15 agrupados por carpeta y el token guardado en una
   variable.
4. **Documento OpenAPI** exportado desde `/v3/api-docs` como `docs/openapi.json`, y
   validado sin errores en un editor de OpenAPI (SwaggerHub o `editor.swagger.io`).
5. **`README.md`** (RNF-15).

## 15. Escenarios de aceptación mínimos

| # | Escenario | Resultado esperado |
|---|---|---|
| 1 | Reservar una sala libre con 2 servicios adicionales | `201` con costo calculado |
| 2 | Reservar una sala en un horario que se solapa con otra reserva | `409` `RN-01` |
| 3 | Reservar de 10:00 a 11:00 con otra reserva de 09:00 a 10:00 | `201` (los extremos no se solapan) |
| 4 | Reservar 45 min, o fuera del horario de la sede | `409` con mensaje claro |
| 5 | Cancelar con 30 h de anticipación | `200`, cargo 0, horas devueltas |
| 6 | Cancelar con 5 h de anticipación | `200`, cargo del 50 % |
| 7 | Cancelar con 1 h de anticipación | `409` |
| 8 | Confirmar una reserva ya cancelada | `409` `RN-09` |
| 9 | Un miembro consulta la reserva de otro miembro | `403` `ACCESO_DENEGADO` |
| 10 | Llamar a un endpoint protegido sin token, o con un token alterado | `401` `NO_AUTENTICADO` |
| 11 | Eliminar una sala con reservas futuras | `409` `RN-12` |
| 12 | Consultar el resumen mensual de un miembro con 3 h de reservas y un plan de 2 h incluidas | 2 h incluidas usadas, 1 h excedente |
| 13 | Crear una sede sin `nombre`, o con la apertura posterior al cierre | `400` `SOLICITUD_INVALIDA`, con un mensaje que nombra el campo |
| 14 | Iniciar sesión con una contraseña incorrecta | `401` `NO_AUTENTICADO`, sin revelar si el usuario existe |
| 15 | Consultar `/auth/perfil` con el token de un miembro | `200` con rol `MIEMBRO` y su `miembroId` |
| 16 | Listar miembros con `?page=0&size=2&sort=nombre,asc` | `200`, 2 miembros, y el total de elementos y de páginas |
| 17 | Recepción intenta crear una sede | `403` `ACCESO_DENEGADO` |
| 18 | Enviar dos solicitudes simultáneas para la misma sala y el mismo horario | Una responde `201` y la otra `409` `RN-01` |
| 19 | Consultar `/actuator/health` y Swagger UI sin token | `200` en ambos |
| 20 | Cambiar el precio de un servicio adicional y consultar una reserva existente que lo usa | El costo de la reserva no cambia |

## 16. Preguntas de sustentación

Si tu docente pide una sustentación, estas son las preguntas posibles. Respondelas con
ejemplos de **tu** código.

| Módulo | Preguntas |
|---|---|
| 01 | ¿Qué diferencia a un framework de una librería, y dónde se ve la inversión de control en tu proyecto? ¿Qué hace `@SpringBootApplication`? ¿Qué cambiaría si usaras Gradle en lugar de Maven? ¿Qué te dio cada *starter*? |
| 02 | Mostrá una dependencia transitiva de tu proyecto (`mvn dependency:tree`). ¿En qué fase del ciclo de vida se ejecuta tu `@PostConstruct`? ¿Qué pasaría si hubiera dos implementaciones de una interfaz sin `@Qualifier`? ¿Un Java Bean es lo mismo que un bean de Spring? |
| 03 | ¿Qué diferencia hay entre JPA, Hibernate y Spring Data JPA? ¿Qué hace el `EntityManager` cuando llamás a `save`? ¿Por qué elegiste ese valor de `ddl-auto`? ¿En qué se diferencia tu consulta JPQL de una SQL? |
| 04 | ¿Cuál es el lado dueño de `Reserva`–`DetalleReserva` y por qué? ¿Qué pasaría sin `orphanRemoval`? ¿Cómo detectaste o evitaste el problema N+1? ¿Cómo diagnosticás un arranque fallido? |
| 05 | ¿Qué principios REST cumple tu API? ¿Por qué `POST` para confirmar una reserva y `PATCH` para suspender a un miembro? ¿Cuándo `PUT` y cuándo `PATCH`? ¿Por qué DTO y no entidades en las respuestas? |
| 06 | ¿Qué hay en `paths`, `components` y `servers` de tu `openapi.json`? ¿Qué ventaja tiene la documentación generada desde el código frente a escribirla a mano en SwaggerHub? ¿Cómo lograste que Swagger UI envíe el token? |
| 07 | ¿Qué diferencia hay entre una excepción *checked* y una *unchecked*, y cuál usaste? ¿Qué mecanismo tiene prioridad si una excepción tiene `@ResponseStatus` y además un `@ExceptionHandler`? ¿Por qué una regla violada es `409` y no `500`? |
| 08 | ¿Qué diferencia hay entre una arquitectura *stateful* y una *stateless*? Recorré una solicitud desde que entra hasta que llega al controlador (`DelegatingFilterProxy`, `FilterChainProxy`, `SecurityFilterChain`, tu filtro, `SecurityContextHolder`). ¿Qué partes tiene un JWT y por qué no se puede alterar su contenido sin que se note? ¿Por qué desactivaste CSRF? ¿Qué diferencia hay entre `401` y `403`? |

## 17. Criterios de evaluación

| Criterio | Peso |
|---|---|
| Análisis: trazabilidad, ER justificado y supuestos explícitos | 15 % |
| Modelo JPA: relaciones, lado dueño, `cascade` y `fetch` correctos | 15 % |
| Reglas de negocio: RN-01 a RN-13 implementadas y probadas | 25 % |
| Arquitectura por capas y calidad del código | 15 % |
| Manejo de excepciones y códigos HTTP | 10 % |
| Seguridad con JWT y roles | 10 % |
| Swagger, README y pruebas de aceptación | 10 % |

El detalle de cada criterio, con niveles de desempeño, está en la [Rúbrica](rubrica.md).
Dónde cuenta lo que no tiene criterio propio: los DTO, los mapeadores, las interfaces de
servicio y los beans de configuración, en *Arquitectura*; la paginación y las consultas,
en *Modelo JPA*; la validación de datos y el catálogo de códigos, en *Excepciones*; la
fase de Basic Auth y `/auth/perfil`, en *Seguridad*; el `openapi.json` y Actuator, en
*Swagger, README y pruebas*.

## 18. Extensiones opcionales

- Reservas recurrentes (por ejemplo, todos los martes durante un mes).
- Lista de espera cuando una sala está ocupada.
- Reporte de ocupación por sala y por sede.
- Encabezado `Location` en las respuestas `201`, con la URL del recurso creado.
- Que el proyecto funcione también con **MySQL** y **PostgreSQL**, eligiendo la base con
  un perfil de Spring (`h2`, `mysql`, `postgres`). Probá tus escenarios, en especial las
  solicitudes simultáneas, con más de un motor: pueden comportarse distinto.

## 💡 Pistas para empezar

Ninguna es obligatoria; están para que no te bloquees en lo que no es el foco del proyecto.

- **El "ahora" en las pruebas.** Las reglas RN-03 y RN-08 dependen de la hora actual y
  los escenarios 5, 6 y 7 piden "30 h", "5 h" y "1 h" de anticipación. Si tu código
  llama directamente a `LocalDateTime.now()`, esos escenarios serán difíciles de
  repetir. Pensá cómo hacer que la hora sea un dato que se pueda controlar (pista: un
  bean `Clock` inyectado, con una propiedad como `coworkhub.reloj.fijo`). El JWT, en
  cambio, debe vencer con la hora real: ahí tenés un buen caso para `@Qualifier`.
- **Reservas de ejemplo en el pasado.** Los datos semilla incluyen reservas
  `COMPLETADA`, que están en el pasado; si las creás con las mismas reglas que una
  reserva nueva, serán rechazadas (RN-03). Pensá cómo cargarlas sin pasar por esas
  reglas.
- **Dos solicitudes a la vez.** RN-01 debe cumplirse aunque lleguen dos solicitudes
  simultáneas para la misma sala y el mismo horario. Verificar el solapamiento y
  guardar en dos pasos separados no alcanza por sí solo.
- **No devuelvas `Page` tal cual.** Spring Data no garantiza que el JSON de un `Page`
  sea estable. Convertilo en tu propio `record` `Pagina<T>`. Un `sort` por un campo que
  no existe lanza `PropertyReferenceException` (en Spring Boot 4, del paquete
  `org.springframework.data.core`): traducila a `400`.
- **Un `@ExceptionHandler(Exception.class)` atrapa demasiado.** Si tu manejador global
  no extiende `ResponseEntityExceptionHandler`, convierte en `500` los `404` y `405` de
  Spring. Y atrapa también las excepciones de seguridad que lanzan `@PreAuthorize` y el
  login: traducilas explícitamente a `403` y `401`.
- **Basic Auth y CSRF (fase 6a).** Los `GET` funcionan con Basic Auth, pero los `POST`
  fallan aunque las credenciales sean correctas: es la protección CSRF. Entendé por qué
  antes de desactivarla.
- **Alterar un token para el escenario 10.** Cambiá una letra del medio de la carga (la
  parte entre los dos puntos), no la última del token: por cómo funciona Base64, cambiar
  el último carácter no siempre cambia los bytes firmados.

## 🆘 ¿Te trabaste?

Existe una [solución paso a paso](solucion/README.md), pensada para que puedas
transcribirla y ejecutarla fase por fase, entendiendo el porqué de cada decisión.
Intentá primero cada fase por tu cuenta; si te bloqueás, mirá solo esa fase y volvé a
tu propio proyecto.

La solución resuelve el núcleo del proyecto: el modelo, las reglas de negocio, los
endpoints, los errores, Swagger y JWT. Algunos requisitos de esta versión no están en
ella: los DTO de salida con mapeadores, Bean Validation, la paginación de miembros, los
servicios como interfaz + implementación, `@Qualifier` y `@PostConstruct`, la fase de
Basic Auth, `/auth/perfil` y Actuator. Para esos, apoyate en las guías de los módulos.
