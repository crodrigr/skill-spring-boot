# 📚 Fase 5 — Documentación con Swagger (OpenAPI)

**Navegación**: [Índice](README.md) · ← [Fase 4 — Excepciones](04-fase-4-excepciones.md) · Siguiente → [Fase 6 — Seguridad con JWT](06-fase-6-seguridad-jwt.md)

## 🎯 Qué vas a lograr

Documentar **automáticamente** toda la API con Swagger UI (RF-13): qué endpoints hay,
qué datos reciben, qué códigos devuelven —incluidos los de error— y con ejemplos, y poder
probarlos desde el navegador.

**Módulo que se aplica**: 06 (Documentación de APIs con Swagger).

## 🧠 Conceptos en 30 segundos

- **OpenAPI** es un estándar para describir una API REST en un documento JSON.
- **springdoc-openapi** recorre tus controladores al arrancar y **genera** ese documento
  sin que escribas nada; publica en `/v3/api-docs`.
- **Swagger UI** es una página web que lee ese documento y lo muestra de forma
  interactiva.
- Con **anotaciones** (`@Tag`, `@Operation`, `@Schema`) enriquecés lo generado: nombres,
  descripciones y ejemplos.

## 🪜 Paso a paso

### Paso 5.1 — Dependencia

Agregá al `pom.xml`, dentro de `<dependencies>`, esta dependencia:

**📄 `pom.xml`** (fragmento)

```xml
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.6.0</version>
        </dependency>
```

> La versión `2.6.0` de springdoc es la compatible con Spring Boot 3.3.x. Si usás otra
> versión de Boot, consultá la tabla de compatibilidad de springdoc antes de cambiarla.

### Paso 5.2 — Propiedades

Agregá al final de `application.properties`:

**📄 `src/main/resources/application.properties`** (fragmento)

```properties
# Documentación de la API (Swagger UI en /doc/swagger-ui.html)
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/doc/swagger-ui.html
springdoc.packages-to-scan=com.coworkhub
```

| Propiedad | Para qué |
|---|---|
| `springdoc.api-docs.enabled` | Publica el documento OpenAPI en `/v3/api-docs` |
| `springdoc.swagger-ui.enabled` | Habilita la interfaz web |
| `springdoc.swagger-ui.path` | Dirección de la interfaz: `/doc/swagger-ui.html` |
| `springdoc.packages-to-scan` | Paquete donde buscar `@RestController`. Usamos el paquete **raíz** `com.coworkhub`: springdoc escanea también los subpaquetes, así incluye `controllers` y, más adelante, `security.controllers`. Si apuntaras a un paquete sin controladores, Swagger UI cargaría vacío |

### ✅ Primer vistazo (sin escribir nada más)

Reiniciá y abrí <http://localhost:8080/doc/swagger-ui.html>. Ya ves **todos** los
endpoints agrupados por controlador, con sus parámetros y esquemas, generados solo a
partir de tu código. Probá *Try it out* en `GET /api/sedes`. El documento en crudo está
en <http://localhost:8080/v3/api-docs>.

Lo que **falta**: nombres amigables, descripciones, ejemplos y —muy importante— que se
vea qué pasa cuando algo sale mal (`404`, `409`…). Eso lo agregan los pasos siguientes.

### Paso 5.3 — Configuración global de OpenAPI

Esta clase hace tres cosas:

1. **`Info`**: título, versión y descripción de la API.
2. **Esquema de seguridad `bearerAuth`**: declara que la API usa un token `Bearer` (JWT).
   Todavía no hay seguridad, pero dejarlo declarado ahora hace que en la Fase 6 aparezca
   el botón **Authorize**.
3. **`OpenApiCustomizer` `respuestasDeErrorComunes`**: recorre **todas** las operaciones y
   les agrega las respuestas `400`, `401`, `403`, `404` y `409`, todas con el esquema
   `RespuestaError`. Así no repetís `@ApiResponse` en cada método. Solo agrega una
   respuesta si la operación no la declaró ya.

**📄 `src/main/java/com/coworkhub/config/ConfiguracionOpenApi.java`**

```java
package com.coworkhub.config;

import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.coworkhub.exception.RespuestaError;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class ConfiguracionOpenApi {

    private static final String ESQUEMA_SEGURIDAD = "bearerAuth";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CoWorkHub API")
                        .version("1.0")
                        .description("Reservas de salas y espacios de trabajo. "
                                + "Los errores siempre responden con {error, codigo, timestamp}."))
                .components(new Components().addSecuritySchemes(ESQUEMA_SEGURIDAD,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_SEGURIDAD));
    }

    // Agrega a TODAS las operaciones las respuestas de error comunes, con el esquema RespuestaError,
    // para no repetir @ApiResponse en cada método.
    @Bean
    public OpenApiCustomizer respuestasDeErrorComunes() {
        return openApi -> {
            Map<String, Schema> esquemas = ModelConverters.getInstance().readAll(RespuestaError.class);
            esquemas.forEach((nombre, esquema) -> openApi.getComponents().addSchemas(nombre, esquema));

            openApi.getPaths().values().forEach(ruta -> ruta.readOperations().forEach(operacion -> {
                agregar(operacion, "400", "Solicitud mal formada o con datos inválidos");
                agregar(operacion, "401", "No autenticado: falta el token o no es válido");
                agregar(operacion, "403", "Sin permiso para esta operación");
                agregar(operacion, "404", "El recurso no existe");
                agregar(operacion, "409", "Se viola una regla de negocio (RN-xx) o hay un valor duplicado");
            }));
        };
    }

    private void agregar(Operation operacion, String codigo, String descripcion) {
        if (operacion.getResponses().containsKey(codigo)) {
            return;
        }
        Schema<?> referencia = new Schema<>().$ref("#/components/schemas/RespuestaError");
        operacion.getResponses().addApiResponse(codigo, new ApiResponse()
                .description(descripcion)
                .content(new Content().addMediaType("application/json", new MediaType().schema(referencia))));
    }
}
```

### Paso 5.4 — Ejemplos en los DTOs con `@Schema`

`@Schema` describe cada campo y su valor de ejemplo. Swagger UI lo usa para rellenar el
cuerpo de ejemplo del `POST`. Reemplazá el contenido de estos cuatro archivos:

> ℹ️ En los campos de fecha y hora (`inicio`, `fin`) usamos `type = "string"` junto con
> `example`: sin él, springdoc descarta el ejemplo porque no puede interpretar el formato
> de fecha del valor.

**📄 `src/main/java/com/coworkhub/dto/SolicitudReserva.java`**

```java
package com.coworkhub.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record SolicitudReserva(
        @Schema(description = "Id del miembro que reserva", example = "1") Long miembroId,
        @Schema(description = "Id de la sala", example = "2") Long salaId,
        @Schema(description = "Inicio de la reserva", type = "string", example = "2030-06-04T16:00:00") LocalDateTime inicio,
        @Schema(description = "Fin de la reserva", type = "string", example = "2030-06-04T17:30:00") LocalDateTime fin,
        @Schema(description = "Cantidad de asistentes", example = "3") Integer asistentes,
        @Schema(description = "Servicios adicionales (opcional)") List<ItemServicio> servicios) {
}
```

**📄 `src/main/java/com/coworkhub/dto/ItemServicio.java`**

```java
package com.coworkhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ItemServicio(
        @Schema(description = "Id del servicio adicional", example = "1") Long servicioId,
        @Schema(description = "Cantidad de unidades", example = "2") Integer cantidad) {
}
```

**📄 `src/main/java/com/coworkhub/dto/SolicitudMiembro.java`**

```java
package com.coworkhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SolicitudMiembro(
        @Schema(example = "9090909") String documento,
        @Schema(example = "Mateo Mini") String nombre,
        @Schema(example = "mateo@coworkhub.test") String email,
        @Schema(description = "Id del plan de membresía", example = "2") Long planId,
        @Schema(example = "mateo") String nombreUsuario,
        @Schema(description = "Mínimo 6 caracteres", example = "mateo123") String contrasena) {
}
```

**📄 `src/main/java/com/coworkhub/exception/RespuestaError.java`**

```java
package com.coworkhub.exception;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

// Cuerpo único de todas las respuestas de error (RNF-05).
public record RespuestaError(
        @Schema(description = "Mensaje para el usuario", example = "La sala ya está reservada en ese horario") String error,
        @Schema(description = "Código estable: RN-xx, NO_ENCONTRADO, SOLICITUD_INVALIDA...", example = "RN-01") String codigo,
        @Schema(description = "Instante del error (UTC)", example = "2030-06-03T13:00:00Z") String timestamp) {

    public static RespuestaError de(String codigo, String mensaje) {
        return new RespuestaError(mensaje, codigo, Instant.now().toString());
    }
}
```

### Paso 5.5 — Agrupar los endpoints con `@Tag`

`@Tag` da nombre y descripción al **grupo** de endpoints de un controlador. En cada uno de
los siete controladores siguientes agregá (1) el `import` junto a los demás y (2) la
anotación `@Tag`, **encima de `@RestController`**:

`ControladorSedes`:

**📄 `src/main/java/com/coworkhub/controllers/ControladorSedes.java`** (fragmento)

```java
import io.swagger.v3.oas.annotations.tags.Tag;
...
@Tag(name = "Sedes", description = "Catálogo de sedes")
```

`ControladorSalas`:

**📄 `src/main/java/com/coworkhub/controllers/ControladorSalas.java`** (fragmento)

```java
import io.swagger.v3.oas.annotations.tags.Tag;
...
@Tag(name = "Salas", description = "Catálogo de salas y su equipamiento")
```

`ControladorEquipamientos`:

**📄 `src/main/java/com/coworkhub/controllers/ControladorEquipamientos.java`** (fragmento)

```java
import io.swagger.v3.oas.annotations.tags.Tag;
...
@Tag(name = "Equipamientos", description = "Catálogo de equipamiento (proyector, pizarra...)")
```

`ControladorServiciosAdicionales`:

**📄 `src/main/java/com/coworkhub/controllers/ControladorServiciosAdicionales.java`** (fragmento)

```java
import io.swagger.v3.oas.annotations.tags.Tag;
...
@Tag(name = "Servicios adicionales", description = "Catálogo de servicios que se pueden agregar a una reserva")
```

`ControladorPlanes`:

**📄 `src/main/java/com/coworkhub/controllers/ControladorPlanes.java`** (fragmento)

```java
import io.swagger.v3.oas.annotations.tags.Tag;
...
@Tag(name = "Planes de membresía", description = "Catálogo de planes")
```

`ControladorMiembros`:

**📄 `src/main/java/com/coworkhub/controllers/ControladorMiembros.java`** (fragmento)

```java
import io.swagger.v3.oas.annotations.tags.Tag;
...
@Tag(name = "Miembros", description = "Registro de miembros y resumen de consumo mensual")
```

`ControladorDisponibilidad`:

**📄 `src/main/java/com/coworkhub/controllers/ControladorDisponibilidad.java`** (fragmento)

```java
import io.swagger.v3.oas.annotations.tags.Tag;
...
@Tag(name = "Disponibilidad", description = "Consulta de salas libres en un horario")
```

### Paso 5.6 — Describir cada operación de reservas con `@Operation`

Las reservas son lo más importante de la API, así que cada método lleva `@Operation` con
un resumen y —donde importa— la regla que aplica. Reemplazá `ControladorReservas`
completo (tiene el `@Tag` y todos los `@Operation`):

**📄 `src/main/java/com/coworkhub/controllers/ControladorReservas.java`**

```java
package com.coworkhub.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coworkhub.dto.ItemServicio;
import com.coworkhub.dto.SolicitudReserva;
import com.coworkhub.persistences.entities.EstadoReserva;
import com.coworkhub.persistences.entities.Reserva;
import com.coworkhub.services.ServicioReservas;

@Tag(name = "Reservas", description = "Crear, consultar, modificar y cancelar reservas")
@RestController
@RequestMapping("/api/reservas")
public class ControladorReservas {

    private final ServicioReservas servicioReservas;

    public ControladorReservas(ServicioReservas servicioReservas) {
        this.servicioReservas = servicioReservas;
    }

    @Operation(summary = "Crear una reserva",
            description = "Valida RN-01 a RN-06 y calcula el costo (RN-07). Responde 201 con el costo calculado.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva crear(@RequestBody SolicitudReserva solicitud) {
        return servicioReservas.crear(solicitud);
    }

    @Operation(summary = "Consultar una reserva por id")
    @GetMapping("/{id}")
    public Reserva buscarPorId(@PathVariable Long id) {
        return servicioReservas.buscarPorId(id);
    }

    // GET /api/reservas?miembroId=1
    // GET /api/reservas?salaId=1&fecha=2030-06-04
    // GET /api/reservas?estado=PENDIENTE
    @Operation(summary = "Listar reservas",
            description = "Filtrar por miembroId, o por salaId junto con fecha (yyyy-MM-dd), o por estado.")
    @GetMapping
    public List<Reserva> listar(@RequestParam(required = false) Long miembroId,
                                @RequestParam(required = false) Long salaId,
                                @RequestParam(required = false) LocalDate fecha,
                                @RequestParam(required = false) EstadoReserva estado) {
        return servicioReservas.buscar(miembroId, salaId, fecha, estado);
    }

    @Operation(summary = "Agregar un servicio adicional",
            description = "Solo con la reserva PENDIENTE (RN-10). Si el servicio ya está, suma la cantidad. Recalcula el costo.")
    @PostMapping("/{id}/servicios")
    public Reserva agregarServicio(@PathVariable Long id, @RequestBody ItemServicio item) {
        return servicioReservas.agregarServicio(id, item);
    }

    @Operation(summary = "Quitar un servicio adicional",
            description = "Solo con la reserva PENDIENTE (RN-10). Recalcula el costo.")
    @DeleteMapping("/{id}/servicios/{servicioId}")
    public Reserva quitarServicio(@PathVariable Long id, @PathVariable Long servicioId) {
        return servicioReservas.quitarServicio(id, servicioId);
    }

    @Operation(summary = "Confirmar una reserva", description = "PENDIENTE → CONFIRMADA (RN-09).")
    @PostMapping("/{id}/confirmar")
    public Reserva confirmar(@PathVariable Long id) {
        return servicioReservas.confirmar(id);
    }

    @Operation(summary = "Cancelar una reserva",
            description = "RN-08: 24 h o más sin cargo; entre 24 h y 2 h cargo del 50 %; con menos de 2 h no se puede.")
    @PostMapping("/{id}/cancelar")
    public Reserva cancelar(@PathVariable Long id) {
        return servicioReservas.cancelar(id);
    }

    @Operation(summary = "Marcar una reserva como completada", description = "CONFIRMADA → COMPLETADA (RN-09).")
    @PostMapping("/{id}/completar")
    public Reserva completar(@PathVariable Long id) {
        return servicioReservas.completar(id);
    }
}
```

## ✅ Checkpoint 5 — Swagger UI completa

Reiniciá y abrí <http://localhost:8080/doc/swagger-ui.html>. Verificá:

- [ ] Aparecen **8 grupos** con sus nombres (Sedes, Salas, Equipamientos, Servicios
      adicionales, Planes de membresía, Miembros, Disponibilidad, Reservas).
- [ ] Arriba se lee *"CoWorkHub API 1.0"* con su descripción.
- [ ] En **Reservas → `POST /api/reservas`**, el cuerpo de ejemplo aparece relleno con
      los valores de `@Schema` (`miembroId: 1`, `salaId: 2`, `inicio: 2030-06-04T16:00:00`…).
- [ ] Ese mismo endpoint muestra respuestas **`201`, `400`, `401`, `403`, `404` y `409`**.
      Al abrir la `409`, el esquema es `RespuestaError`, con su ejemplo (`"codigo": "RN-01"`).
- [ ] En **Reservas → `POST /api/reservas/{id}/cancelar`** se lee la descripción con la
      política de anticipación (RN-08).
- [ ] Al final de la página, en **Schemas**, figura `RespuestaError`.
- [ ] Con *Try it out* podés crear una reserva desde el navegador y ver la respuesta.

| Si ves… | Causa probable |
|---|---|
| Swagger UI carga pero **vacía** | `springdoc.packages-to-scan` apunta a un paquete sin controladores |
| `404` en `/doc/swagger-ui.html` | Falta la dependencia, o `springdoc.swagger-ui.enabled=false` |
| Falta el ejemplo de `inicio` y `fin` | Olvidaste `type = "string"` en el `@Schema` de esos campos |
| No aparecen las respuestas de error | No se creó el bean `respuestasDeErrorComunes` en `ConfiguracionOpenApi` |

### Paso 5.7 — Commit

```bash
git add .
git commit -m "fase 5: documentación con Swagger"
```

**Siguiente →** [Fase 6 — Seguridad con JWT](06-fase-6-seguridad-jwt.md)
