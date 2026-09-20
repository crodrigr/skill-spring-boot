# 🚨 Fase 4 — Manejo de excepciones

**Navegación**: [Índice](README.md) · ← [Fase 3c — Reservas](03c-fase-3-reservas.md) · Siguiente → [Fase 5 — Swagger](05-fase-5-swagger.md)

## 🎯 Qué vas a lograr

Que **toda** respuesta de error de la API tenga el mismo formato y el código HTTP
correcto: `404` cuando algo no existe, `409` cuando se viola una regla de negocio, `400`
cuando los datos están mal, y nunca un `500` por una regla de negocio (RNF-05 y RNF-06).

**Módulo que se aplica**: 07 (Manejo de excepciones).

## 🧠 El problema que viste en la Fase 3

Cuando un servicio lanzaba `ReglaNegocioException`, la API respondía `500 Internal Server
Error` con un cuerpo genérico. Para un cliente, un `500` significa *"el servidor se
rompió"*, no *"tu solicitud viola una regla"*. Necesitamos traducir cada excepción a la
respuesta HTTP adecuada, y hacerlo **en un solo lugar**.

De los tres mecanismos del Módulo 07 elegimos el más adecuado para este caso:

| Mecanismo | Alcance | ¿Sirve acá? |
|---|---|---|
| `@ResponseStatus` sobre la excepción | Solo fija el código, no el cuerpo | ❌ No permite un cuerpo uniforme con `codigo` y `timestamp` |
| `@ExceptionHandler` dentro de un controlador | Solo ese controlador | ❌ Habría que repetirlo en cada uno |
| **`@ControllerAdvice`** | **Todos los controladores** | ✅ Un único lugar, un único formato |

## 🌳 Archivos de esta fase

```text
📁 coworkhub
├── 📄 .gitignore
├── 📄 docker-compose.yml
├── 📄 pom.xml
├── 📁 docs  (documentación del proyecto)
│   └── 📄 analisis.md
├── 📁 src/main/java/com/coworkhub
│   ├── 📄 Main.java
│   ├── 📁 config  (beans, datos de ejemplo y OpenAPI)
│   │   ├── 📄 CargadorReservasDemo.java
│   │   └── 📄 ConfiguracionBeans.java
│   ├── 📁 controllers  (capa Controller, HTTP)
│   │   ├── 📄 ControladorDisponibilidad.java
│   │   ├── 📄 ControladorEquipamientos.java
│   │   ├── 📄 ControladorMiembros.java
│   │   ├── 📄 ControladorPlanes.java
│   │   ├── 📄 ControladorReservas.java
│   │   ├── 📄 ControladorSalas.java
│   │   ├── 📄 ControladorSedes.java
│   │   └── 📄 ControladorServiciosAdicionales.java
│   ├── 📁 dto  (solicitudes y respuestas, en records)
│   │   ├── 📄 ItemServicio.java
│   │   ├── 📄 ResumenConsumo.java
│   │   ├── 📄 SolicitudActualizarMiembro.java
│   │   ├── 📄 SolicitudEquipamiento.java
│   │   ├── 📄 SolicitudMiembro.java
│   │   ├── 📄 SolicitudPlan.java
│   │   ├── 📄 SolicitudReserva.java
│   │   ├── 📄 SolicitudSala.java
│   │   ├── 📄 SolicitudSede.java
│   │   └── 📄 SolicitudServicioAdicional.java
│   ├── 📁 exception  (excepciones y manejador global)
│   │   ├── 📄 ManejadorGlobalDeExcepciones.java               ◀ 🆕 nuevo
│   │   ├── 📄 RecursoNoEncontradoException.java
│   │   ├── 📄 ReglaNegocioException.java
│   │   ├── 📄 RespuestaError.java                             ◀ 🆕 nuevo
│   │   └── 📄 SolicitudInvalidaException.java
│   ├── 📁 persistences  (capa Persistence)
│   │   ├── 📁 entities  (clases @Entity)
│   │   │   ├── 📄 DetalleReserva.java
│   │   │   ├── 📄 Equipamiento.java
│   │   │   ├── 📄 EstadoMiembro.java
│   │   │   ├── 📄 EstadoReserva.java
│   │   │   ├── 📄 Miembro.java
│   │   │   ├── 📄 PlanMembresia.java
│   │   │   ├── 📄 Reserva.java
│   │   │   ├── 📄 Sala.java
│   │   │   ├── 📄 Sede.java
│   │   │   ├── 📄 ServicioAdicional.java
│   │   │   └── 📄 TipoSala.java
│   │   └── 📁 repositories  (interfaces JpaRepository)
│   │       ├── 📄 RepositorioEquipamientos.java
│   │       ├── 📄 RepositorioMiembros.java
│   │       ├── 📄 RepositorioPlanes.java
│   │       ├── 📄 RepositorioReservas.java
│   │       ├── 📄 RepositorioSalas.java
│   │       ├── 📄 RepositorioSedes.java
│   │       └── 📄 RepositorioServiciosAdicionales.java
│   ├── 📁 security  (autenticación y autorización)
│   │   └── 📁 persistences  (capa Persistence de seguridad)
│   │       ├── 📁 entities  (Usuario y Rol)
│   │       │   ├── 📄 Rol.java
│   │       │   └── 📄 Usuario.java
│   │       └── 📁 repositories  (RepositorioUsuarios)
│   │           └── 📄 RepositorioUsuarios.java
│   └── 📁 services  (capa Service, reglas de negocio)
│       ├── 📄 CalculadoraCostoReserva.java
│       ├── 📄 ServicioConsumo.java
│       ├── 📄 ServicioDisponibilidad.java
│       ├── 📄 ServicioEquipamientos.java
│       ├── 📄 ServicioMiembros.java
│       ├── 📄 ServicioPlanes.java
│       ├── 📄 ServicioReservas.java
│       ├── 📄 ServicioSalas.java
│       ├── 📄 ServicioSedes.java
│       ├── 📄 ServicioServiciosAdicionales.java
│       └── 📄 Validaciones.java
└── 📁 src/main/resources
    ├── 📄 application-h2.properties
    ├── 📄 application-mysql.properties
    ├── 📄 application-postgres.properties
    ├── 📄 application.properties
    └── 📄 data.sql
```

🆕 archivo nuevo en esta fase · ✏️ archivo que ya existía y se modifica en esta fase · sin marca: ya existe de fases anteriores.

**En esta fase**: 2 archivos nuevos.

## 🪜 Paso a paso

### Paso 4.1 — El cuerpo estándar de error: `RespuestaError`

Un `record` con tres campos, que **todas** las respuestas de error usarán:

| Campo | Contenido |
|---|---|
| `error` | Mensaje legible para una persona |
| `codigo` | Código **estable** para que un programa decida qué hacer: `RN-01`, `NO_ENCONTRADO`, `SOLICITUD_INVALIDA`… |
| `timestamp` | Instante del error, en UTC |

La razón de tener `codigo` además del mensaje: el mensaje puede cambiar de redacción (o
de idioma); el código, no. Un cliente puede hacer `if (codigo == "RN-01")` sin depender
del texto.

**📄 `src/main/java/com/coworkhub/exception/RespuestaError.java`**

```java
package com.coworkhub.exception;

import java.time.Instant;

// Cuerpo único de todas las respuestas de error (RNF-05).
public record RespuestaError(String error, String codigo, String timestamp) {

    public static RespuestaError de(String codigo, String mensaje) {
        return new RespuestaError(mensaje, codigo, Instant.now().toString());
    }
}
```

### Paso 4.2 — El manejador global

Cómo leerlo:

- `@ControllerAdvice` hace que los métodos `@ExceptionHandler` de esta clase valgan para
  **todos** los controladores.
- **Extiende `ResponseEntityExceptionHandler`**, una clase de Spring que ya sabe manejar
  los errores propios de Spring MVC (JSON mal formado, parámetro faltante, ruta que no
  existe, método HTTP no permitido…). Sin ella, esos errores saldrían con el cuerpo por
  defecto de Spring y romperían la promesa de un formato único (RNF-05). Sobrescribimos
  `handleExceptionInternal` —por donde pasan todos— para reemplazar su cuerpo por un
  `RespuestaError`, y algunos métodos más para redactar mensajes claros en español.
- Cada `@ExceptionHandler` recibe la excepción y devuelve un `ResponseEntity` con el
  código y el cuerpo.

| Excepción | HTTP | `codigo` |
|---|---|---|
| `RecursoNoEncontradoException` | `404` | `NO_ENCONTRADO` |
| `ReglaNegocioException` | `409` | el de la excepción (`RN-01`, `RN-12`, `DUPLICADO`…) |
| `SolicitudInvalidaException` | `400` | `SOLICITUD_INVALIDA` |
| JSON ilegible, tipo de dato o enumeración inválidos, parámetro faltante o mal tipado | `400` | `SOLICITUD_INVALIDA` |
| Ruta inexistente | `404` | `NO_ENCONTRADO` |
| Método HTTP no permitido | `405` | `METODO_NO_PERMITIDO` |
| `DataIntegrityViolationException` (restricción de la base) | `409` | `CONFLICTO_DE_DATOS` |
| Cualquier otra `Exception` | `500` | `ERROR_INTERNO` |

Dos decisiones para destacar:

- **Red de seguridad `DataIntegrityViolationException`**: aunque los servicios verifican
  duplicados antes de guardar, dos solicitudes simultáneas podrían pasar ambas la
  verificación y la restricción `unique` de la base rechazaría la segunda. Eso sigue
  siendo un conflicto del cliente (`409`), no un error del servidor.
- **El `500` no revela nada**: para cualquier error inesperado se escribe el detalle
  completo en el **log** (`log.error(..., ex)`), pero al cliente solo se le dice
  *"Ocurrió un error inesperado"*. Devolver el mensaje o el *stack trace* filtraría
  información interna.

**📄 `src/main/java/com/coworkhub/exception/ManejadorGlobalDeExcepciones.java`**

```java
package com.coworkhub.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// Único lugar donde se traducen las excepciones a respuestas HTTP (RNF-05 y RNF-06).
// Extiende ResponseEntityExceptionHandler para cubrir también los errores propios de Spring MVC
// (JSON mal formado, parámetro faltante, ruta inexistente, método no permitido...).
@ControllerAdvice
public class ManejadorGlobalDeExcepciones extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalDeExcepciones.class);

    // ---------------------------------------------------------------- nuestras excepciones

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<RespuestaError> manejarReglaDeNegocio(ReglaNegocioException ex) {
        return responder(HttpStatus.CONFLICT, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<RespuestaError> manejarSolicitudInvalida(SolicitudInvalidaException ex) {
        return responder(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", ex.getMessage());
    }

    // Red de seguridad: una restricción de la base de datos (por ejemplo, un único duplicado
    // que ganó una carrera entre dos solicitudes) también es un conflicto, no un error 500.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaError> manejarIntegridadDeDatos(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos", ex);
        return responder(HttpStatus.CONFLICT, "CONFLICTO_DE_DATOS",
                "La operación viola una restricción de los datos (por ejemplo, un valor duplicado)");
    }

    // Cualquier otra excepción es un error nuestro: se registra completo y se responde algo genérico.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarErrorInesperado(Exception ex) {
        log.error("Error inesperado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrió un error inesperado");
    }

    // ---------------------------------------------------------------- errores propios de Spring MVC

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return construir(status, "SOLICITUD_INVALIDA",
                "El cuerpo de la solicitud falta o no es un JSON válido (revisá tipos, fechas y valores de enumeraciones)");
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return construir(status, "SOLICITUD_INVALIDA", "Falta el parámetro obligatorio '" + ex.getParameterName() + "'");
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String parametro = ex instanceof MethodArgumentTypeMismatchException m ? m.getName() : ex.getPropertyName();
        return construir(status, "SOLICITUD_INVALIDA",
                "El valor '" + ex.getValue() + "' no es válido para '" + parametro + "'");
    }

    // Cualquier otro error de Spring MVC llega acá: se reemplaza su cuerpo por RespuestaError.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        String mensajeDeSpring = body instanceof ProblemDetail detalle && detalle.getDetail() != null
                ? detalle.getDetail()
                : ex.getMessage();
        return construir(statusCode, codigoPara(statusCode), mensajePara(statusCode, mensajeDeSpring));
    }

    // ---------------------------------------------------------------- utilitarios

    private ResponseEntity<RespuestaError> responder(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(RespuestaError.de(codigo, mensaje));
    }

    private ResponseEntity<Object> construir(HttpStatusCode estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(RespuestaError.de(codigo, mensaje));
    }

    // Spring redacta estos mensajes en inglés; los reemplazamos por uno propio.
    private String mensajePara(HttpStatusCode estado, String mensajePorDefecto) {
        return switch (estado.value()) {
            case 404 -> "La ruta solicitada no existe";
            case 405 -> "El método HTTP no está permitido para esta ruta";
            case 415 -> "El tipo de contenido no es soportado (usá application/json)";
            default -> mensajePorDefecto;
        };
    }

    private String codigoPara(HttpStatusCode estado) {
        return switch (estado.value()) {
            case 400 -> "SOLICITUD_INVALIDA";
            case 404 -> "NO_ENCONTRADO";
            case 405 -> "METODO_NO_PERMITIDO";
            case 415 -> "TIPO_NO_SOPORTADO";
            default -> estado.is5xxServerError() ? "ERROR_INTERNO" : "ERROR";
        };
    }
}
```

> ℹ️ En la [Fase 6](06-fase-6-seguridad-jwt.md) agregarás a este manejador dos métodos más
> (para `AccessDeniedException` y `AuthenticationException`), porque esas clases aún no
> están disponibles: se incorporan con Spring Security.

## ✅ Checkpoint 4 — los errores ahora son correctos

Reiniciá (con `coworkhub.reloj.fijo=2030-06-03T08:00:00`) y repetí llamadas que en la
Fase 3 daban `500`:

| Llamada | Antes | Ahora |
|---|---|---|
| `GET /api/sedes/999` | `500` | `404` `NO_ENCONTRADO` |
| `POST /api/reservas` solapada con la reserva 7 | `500` | `409` `RN-01` |
| `DELETE /api/sedes/1` (tiene salas) | `500` | `409` `RN-12` |

Y estas son las respuestas reales (los `timestamp` cambian en cada llamada):

```bash
curl -s -w "\nHTTP %{http_code}\n" http://localhost:8080/api/sedes/999
```

```json
{"error":"No se encontró Sede con id 999","codigo":"NO_ENCONTRADO","timestamp":"2030-06-03T13:00:00.123Z"}
HTTP 404
```

```bash
# Solapamiento con la reserva 7 (Sala Andes, 10:00-12:00)
curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:8080/api/reservas \
  -H 'Content-Type: application/json' \
  -d '{"miembroId":2,"salaId":1,"inicio":"2030-06-04T11:00:00","fin":"2030-06-04T12:00:00","asistentes":2}'
```

```json
{"error":"La sala ya está reservada en ese horario","codigo":"RN-01","timestamp":"..."}
HTTP 409
```

Probá también estos casos, uno por uno, y comprobá el código y el `codigo` del cuerpo:

| Solicitud | HTTP | `codigo` | `error` |
|---|---|---|---|
| Reserva de 45 min (`10:00` a `10:45`) | `409` | `RN-02` | La duración debe ser de entre 30 minutos y 8 horas, en múltiplos de 30 minutos |
| Reserva de `06:00` a `07:00` en la Sede Centro | `409` | `RN-03` | La reserva debe estar dentro del horario de la sede (07:00 a 22:00) |
| `POST /api/sedes` con `{"ciudad":"X"}` | `400` | `SOLICITUD_INVALIDA` | El campo 'horaApertura' es obligatorio |
| `POST /api/salas` con `"tipo":"OTRO"` | `400` | `SOLICITUD_INVALIDA` | El cuerpo de la solicitud falta o no es un JSON válido… |
| `POST /api/sedes` con el cuerpo `{nombre` (JSON roto) | `400` | `SOLICITUD_INVALIDA` | (el mismo mensaje anterior) |
| `GET /api/reservas/abc` | `400` | `SOLICITUD_INVALIDA` | El valor 'abc' no es válido para 'id' |
| `GET /api/reservas` (sin filtros) | `400` | `SOLICITUD_INVALIDA` | Indicá miembroId, o salaId junto con fecha, o estado |
| `GET /api/ruta-que-no-existe` | `404` | `NO_ENCONTRADO` | La ruta solicitada no existe |
| `PATCH /api/sedes/1` | `405` | `METODO_NO_PERMITIDO` | El método HTTP no está permitido para esta ruta |

**Lo importante**: fijate que **todas** las respuestas, incluidas las de rutas
inexistentes y JSON roto —que las genera Spring MVC, no tu código—, tienen exactamente
los tres campos `error`, `codigo` y `timestamp`. Si alguna sale con la forma
`{"timestamp":..., "status":..., "path":...}`, falta sobrescribir su método en el
manejador.

### Paso 4.3 — Commit

```bash
git add .
git commit -m "fase 4: manejo global de excepciones"
```

**Siguiente →** [Fase 5 — Documentación con Swagger](05-fase-5-swagger.md)
