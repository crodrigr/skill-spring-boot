# 🧩 Fase 3a — Servicios y API REST: excepciones, DTOs y catálogos

**Navegación**: [Índice](README.md) · ← [Fase 2 — Modelo de datos](02-fase-2-modelo-de-datos.md) · Siguiente → [Fase 3b — Miembros, disponibilidad y consumo](03b-fase-3-miembros.md)

## 🎯 Qué vas a lograr

Exponer por REST los cinco catálogos (sedes, salas, equipamientos, servicios adicionales y
planes) con la arquitectura de capas `controller → service → repository`. Al terminar
podrás crear, consultar, modificar y borrar catálogos con Insomnia o `curl`.

**Módulo que se aplica**: 05 (Diseño de API REST). La Fase 3 se divide en tres partes para
que puedas verificar tu avance sin esperar al final:

| Parte | Contenido |
|---|---|
| **3a** (esta) | Excepciones de dominio, DTOs, validaciones y los catálogos |
| [3b](03b-fase-3-miembros.md) | Miembros, consumo mensual y disponibilidad |
| [3c](03c-fase-3-reservas.md) | Calculadora de costos, reservas y datos de ejemplo |

## 🪜 Paso a paso

### Paso 3.1 — Excepciones de dominio

Los servicios necesitan avisar que algo salió mal **sin saber nada de HTTP**. Para eso
creamos tres excepciones propias, todas `RuntimeException` (no obligan a declarar
`throws`):

| Excepción | Cuándo se lanza | Se traducirá a (Fase 4) |
|---|---|---|
| `RecursoNoEncontradoException` | Un `id` no existe | `404` |
| `ReglaNegocioException` | Se viola una regla (`RN-xx`) o hay un duplicado. Lleva un `codigo` (por ejemplo `"RN-01"`) | `409` |
| `SolicitudInvalidaException` | Falta un dato o tiene un formato inválido | `400` |

> ℹ️ **Por ahora nadie las traduce.** Hasta la Fase 4, un error de estos producirá una
> respuesta `500 Internal Server Error`. Es esperable: lo verás y lo corregirás.

**📄 `src/main/java/com/coworkhub/exception/RecursoNoEncontradoException.java`**

```java
package com.coworkhub.exception;

// Se lanza cuando un id no existe. Más adelante (Fase 4) se traduce a HTTP 404.
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Long id) {
        super("No se encontró " + recurso + " con id " + id);
    }
}
```

**📄 `src/main/java/com/coworkhub/exception/ReglaNegocioException.java`**

```java
package com.coworkhub.exception;

// Se lanza cuando una operación viola una regla de negocio (RN-xx) o un duplicado.
// Más adelante (Fase 4) se traduce a HTTP 409.
public class ReglaNegocioException extends RuntimeException {

    private final String codigo;

    public ReglaNegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
```

**📄 `src/main/java/com/coworkhub/exception/SolicitudInvalidaException.java`**

```java
package com.coworkhub.exception;

// Se lanza cuando faltan datos o tienen un formato inválido. Más adelante (Fase 4) se traduce a HTTP 400.
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
```

### Paso 3.2 — DTOs de solicitud (`record`)

Un **DTO** (*Data Transfer Object*) es un objeto que define **qué datos acepta o devuelve
la API**, separado de las entidades. Usamos `record`s de Java: inmutables, sin *getters* ni
constructores que escribir.

¿Por qué no recibir directamente la entidad `Sala` en un `POST`? Porque el cliente podría
enviar campos que no le corresponden (`id`, `estado`, `costoTotal`…) y hacer que se
guarden. Con un `record` **solo existen los campos que vos declarás**. Además:

- Los números usan tipos envoltorio (`Integer`, `Long`, `Boolean`), no primitivos: si el
  cliente omite el campo llega `null` y podemos responder `400` con un mensaje claro; con
  un `int` llegaría un `0` silencioso.
- En una solicitud de sala, `sedeId` y `equipamientoIds` son solo **ids**; el servicio los
  convierte en entidades.

**📄 `src/main/java/com/coworkhub/dto/SolicitudSede.java`**

```java
package com.coworkhub.dto;

import java.time.LocalTime;

public record SolicitudSede(String nombre, String ciudad, String direccion,
                            LocalTime horaApertura, LocalTime horaCierre) {
}
```

**📄 `src/main/java/com/coworkhub/dto/SolicitudEquipamiento.java`**

```java
package com.coworkhub.dto;

public record SolicitudEquipamiento(String nombre) {
}
```

**📄 `src/main/java/com/coworkhub/dto/SolicitudServicioAdicional.java`**

```java
package com.coworkhub.dto;

import java.math.BigDecimal;

public record SolicitudServicioAdicional(String nombre, BigDecimal precioUnitario) {
}
```

**📄 `src/main/java/com/coworkhub/dto/SolicitudPlan.java`**

```java
package com.coworkhub.dto;

public record SolicitudPlan(String nombre, Integer horasIncluidasMes, Integer descuentoExcedente,
                            Integer maxReservasActivas) {
}
```

**📄 `src/main/java/com/coworkhub/dto/SolicitudSala.java`**

```java
package com.coworkhub.dto;

import java.math.BigDecimal;
import java.util.Set;

import com.coworkhub.persistences.entities.TipoSala;

public record SolicitudSala(String nombre, TipoSala tipo, Integer capacidad, BigDecimal tarifaPorHora,
                            Boolean activa, Long sedeId, Set<Long> equipamientoIds) {
}
```

Estos otros DTOs los usarás en las partes 3b y 3c; conviene crearlos ya:

**📄 `src/main/java/com/coworkhub/dto/SolicitudMiembro.java`**

```java
package com.coworkhub.dto;

public record SolicitudMiembro(String documento, String nombre, String email, Long planId,
                               String nombreUsuario, String contrasena) {
}
```

**📄 `src/main/java/com/coworkhub/dto/SolicitudActualizarMiembro.java`**

```java
package com.coworkhub.dto;

public record SolicitudActualizarMiembro(String nombre, String email, Long planId) {
}
```

**📄 `src/main/java/com/coworkhub/dto/ItemServicio.java`**

```java
package com.coworkhub.dto;

public record ItemServicio(Long servicioId, Integer cantidad) {
}
```

**📄 `src/main/java/com/coworkhub/dto/SolicitudReserva.java`**

```java
package com.coworkhub.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SolicitudReserva(Long miembroId, Long salaId, LocalDateTime inicio, LocalDateTime fin,
                               Integer asistentes, List<ItemServicio> servicios) {
}
```

**📄 `src/main/java/com/coworkhub/dto/ResumenConsumo.java`**

```java
package com.coworkhub.dto;

import java.math.BigDecimal;

public record ResumenConsumo(Long miembroId, String miembro, String plan, String mes,
                             BigDecimal horasIncluidas, BigDecimal horasUsadas,
                             BigDecimal horasIncluidasUsadas, BigDecimal horasRestantes,
                             BigDecimal horasExcedentes) {
}
```

### Paso 3.3 — Validaciones de forma

Una clase de utilidades (visible solo dentro del paquete `services`) con métodos
estáticos que lanzan `SolicitudInvalidaException`. Los servicios la importan con
`import static`, para escribir `texto(solicitud.nombre(), "nombre")` en vez de repetir
`if (nombre == null || nombre.isBlank()) throw ...` en cada lugar.

Distinguí dos tipos de validación:

- **Forma** (¿el dato llegó y tiene sentido?): esta clase → `400`.
- **Regla de negocio** (¿está permitido hacer esto ahora?): cada servicio → `409`.

**📄 `src/main/java/com/coworkhub/services/Validaciones.java`**

```java
package com.coworkhub.services;

import java.math.BigDecimal;

import com.coworkhub.exception.SolicitudInvalidaException;

// Validaciones de "forma" de los datos de entrada (campos obligatorios y rangos).
// Las reglas de negocio (RN-xx) se validan en cada servicio.
final class Validaciones {

    private Validaciones() {
    }

    static <T> T requerido(T valor, String campo) {
        if (valor == null) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' es obligatorio");
        }
        return valor;
    }

    static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' es obligatorio");
        }
        return valor.trim();
    }

    static int enteroPositivo(Integer valor, String campo) {
        if (requerido(valor, campo) < 1) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' debe ser mayor a cero");
        }
        return valor;
    }

    static int enteroNoNegativo(Integer valor, String campo) {
        if (requerido(valor, campo) < 0) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' no puede ser negativo");
        }
        return valor;
    }

    static int porcentaje(Integer valor, String campo) {
        if (requerido(valor, campo) < 0 || valor > 100) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' debe estar entre 0 y 100");
        }
        return valor;
    }

    static BigDecimal montoPositivo(BigDecimal valor, String campo) {
        if (requerido(valor, campo).signum() <= 0) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' debe ser mayor a cero");
        }
        return valor;
    }
}
```

### Paso 3.4 — El primer catálogo completo: Sedes

Es el molde de los demás. Estudialo con calma.

#### `ServicioSedes` (capa Service)

- `@Service`: Spring lo registra como bean para poder inyectarlo.
- `@Transactional(readOnly = true)` **a nivel de clase**: todos los métodos corren en una
  transacción de solo lectura (más eficiente). Los que modifican datos la reemplazan con
  `@Transactional` propio.
- **Inyección por constructor**: recibe sus repositorios como parámetros. Es la forma
  recomendada (permite campos `final` y facilita las pruebas).
- `orElseThrow(() -> new RecursoNoEncontradoException(...))`: `findById` devuelve un
  `Optional`; si está vacío lanzamos nuestra excepción.
- `eliminar` aplica **RN-12**: consulta `existsBySedeId` y, si hay salas, lanza
  `ReglaNegocioException("RN-12", ...)`.
- El servicio trabaja con la entidad (`Sede`) y devuelve entidades; **no conoce
  `ResponseEntity` ni códigos HTTP**.

**📄 `src/main/java/com/coworkhub/services/ServicioSedes.java`**

```java
package com.coworkhub.services;

import static com.coworkhub.services.Validaciones.requerido;
import static com.coworkhub.services.Validaciones.texto;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.dto.SolicitudSede;
import com.coworkhub.exception.ReglaNegocioException;
import com.coworkhub.exception.RecursoNoEncontradoException;
import com.coworkhub.exception.SolicitudInvalidaException;
import com.coworkhub.persistences.entities.Sede;
import com.coworkhub.persistences.repositories.RepositorioSalas;
import com.coworkhub.persistences.repositories.RepositorioSedes;

@Service
@Transactional(readOnly = true)
public class ServicioSedes {

    private final RepositorioSedes repositorioSedes;
    private final RepositorioSalas repositorioSalas;

    public ServicioSedes(RepositorioSedes repositorioSedes, RepositorioSalas repositorioSalas) {
        this.repositorioSedes = repositorioSedes;
        this.repositorioSalas = repositorioSalas;
    }

    public List<Sede> listarTodas() {
        return repositorioSedes.findAll();
    }

    public Sede buscarPorId(Long id) {
        return repositorioSedes.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede", id));
    }

    @Transactional
    public Sede crear(SolicitudSede solicitud) {
        validar(solicitud);
        Sede sede = new Sede(texto(solicitud.nombre(), "nombre"), texto(solicitud.ciudad(), "ciudad"),
                texto(solicitud.direccion(), "direccion"), solicitud.horaApertura(), solicitud.horaCierre());
        return repositorioSedes.save(sede);
    }

    @Transactional
    public Sede actualizar(Long id, SolicitudSede solicitud) {
        validar(solicitud);
        Sede sede = buscarPorId(id);
        sede.actualizar(texto(solicitud.nombre(), "nombre"), texto(solicitud.ciudad(), "ciudad"),
                texto(solicitud.direccion(), "direccion"), solicitud.horaApertura(), solicitud.horaCierre());
        return repositorioSedes.save(sede);
    }

    @Transactional
    public void eliminar(Long id) {
        Sede sede = buscarPorId(id);
        if (repositorioSalas.existsBySedeId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar la sede: tiene salas asociadas");
        }
        repositorioSedes.delete(sede);
    }

    private void validar(SolicitudSede solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        requerido(solicitud.horaApertura(), "horaApertura");
        requerido(solicitud.horaCierre(), "horaCierre");
        if (!solicitud.horaApertura().isBefore(solicitud.horaCierre())) {
            throw new SolicitudInvalidaException("horaApertura debe ser anterior a horaCierre");
        }
    }
}
```

#### `ControladorSedes` (capa Controller)

Un controlador **solo traduce HTTP ↔ llamadas al servicio**. No tiene lógica.

| Anotación | Significado |
|---|---|
| `@RestController` | Los métodos devuelven datos (JSON), no vistas |
| `@RequestMapping("/api/sedes")` | Prefijo de todas las rutas de la clase |
| `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` | Verbo HTTP del método |
| `@PathVariable Long id` | Toma `{id}` de la URL |
| `@RequestBody SolicitudSede` | Convierte el JSON del cuerpo en el `record` |
| `@ResponseStatus(HttpStatus.CREATED)` | El `POST` responde `201` en vez de `200` |

Observá que el controlador **solo recibe `ServicioSedes`**, nunca un repositorio (RNF-02).
El `DELETE` devuelve `void`: Spring responde `200` con cuerpo vacío.

**📄 `src/main/java/com/coworkhub/controllers/ControladorSedes.java`**

```java
package com.coworkhub.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coworkhub.dto.SolicitudSede;
import com.coworkhub.persistences.entities.Sede;
import com.coworkhub.services.ServicioSedes;

@RestController
@RequestMapping("/api/sedes")
public class ControladorSedes {

    private final ServicioSedes servicioSedes;

    public ControladorSedes(ServicioSedes servicioSedes) {
        this.servicioSedes = servicioSedes;
    }

    @GetMapping
    public List<Sede> listarTodas() {
        return servicioSedes.listarTodas();
    }

    @GetMapping("/{id}")
    public Sede buscarPorId(@PathVariable Long id) {
        return servicioSedes.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Sede crear(@RequestBody SolicitudSede solicitud) {
        return servicioSedes.crear(solicitud);
    }

    @PutMapping("/{id}")
    public Sede actualizar(@PathVariable Long id, @RequestBody SolicitudSede solicitud) {
        return servicioSedes.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicioSedes.eliminar(id);
    }
}
```

## ✅ Checkpoint 3a-1 — Sedes funciona

Ejecutá `mvn spring-boot:run` y probá (con `curl` o Insomnia):

```bash
# Listar
curl -s http://localhost:8080/api/sedes
```

Respuesta esperada (`200`; el JSON aparece formateado acá solo para leerlo):

```json
[
  {
    "id": 1,
    "nombre": "Sede Centro",
    "ciudad": "Bogotá",
    "direccion": "Carrera 7 # 32-16",
    "horaApertura": "07:00:00",
    "horaCierre": "22:00:00"
  },
  { "id": 2, "nombre": "Sede Norte", "ciudad": "Medellín", "direccion": "Calle 10 # 43-20",
    "horaApertura": "08:00:00", "horaCierre": "20:00:00" }
]
```

```bash
# Crear (debe responder 201)
curl -s -X POST http://localhost:8080/api/sedes \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Sede Sur","ciudad":"Cali","direccion":"Avenida 3 # 12-40","horaApertura":"08:00","horaCierre":"18:00"}'
```

```json
{ "id": 3, "nombre": "Sede Sur", "ciudad": "Cali", "direccion": "Avenida 3 # 12-40",
  "horaApertura": "08:00:00", "horaCierre": "18:00:00" }
```

```bash
# Eliminar la sede recién creada (200, sin cuerpo)
curl -s -o /dev/null -w "%{http_code}\n" -X DELETE http://localhost:8080/api/sedes/3

# Pedir una sede que no existe
curl -s -w "\nHTTP %{http_code}\n" http://localhost:8080/api/sedes/999
```

La última llamada responde **`500`** con el cuerpo por defecto de Spring
(`{"timestamp": ..., "status": 500, "error": "Internal Server Error", ...}`). Es
**exactamente lo esperado** en esta fase: `RecursoNoEncontradoException` se lanza bien,
pero nadie la convierte en `404`. La [Fase 4](04-fase-4-excepciones.md) lo resuelve.

También podés probar `DELETE /api/sedes/1` (tiene salas): otro `500` por ahora, que será
un `409` con la regla `RN-12`.

### Paso 3.5 — Equipamientos, servicios adicionales y planes

Siguen el mismo molde. Mirá qué agrega cada uno sobre `Sedes`:

| Servicio | Lo nuevo |
|---|---|
| `ServicioEquipamientos` | Nombre **único** sin distinguir mayúsculas → `ReglaNegocioException("DUPLICADO")`. No se elimina si alguna sala lo usa (`getSalas()` no vacío → `EN_USO`). |
| `ServicioServiciosAdicionales` | Precio obligatorio y mayor a cero. Al **actualizar el precio** no se tocan reservas viejas (cada detalle guarda su `precioUnitarioAplicado`). No se elimina si algún detalle de reserva lo usa (`RN-12`). |
| `ServicioPlanes` | Valida rangos (`descuentoExcedente` entre 0 y 100, `maxReservasActivas` mayor a 0). No se elimina si tiene miembros (`RN-12`). |

Los controladores son idénticos en estructura a `ControladorSedes`.

**📄 `src/main/java/com/coworkhub/services/ServicioEquipamientos.java`**

```java
package com.coworkhub.services;

import static com.coworkhub.services.Validaciones.requerido;
import static com.coworkhub.services.Validaciones.texto;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.dto.SolicitudEquipamiento;
import com.coworkhub.exception.ReglaNegocioException;
import com.coworkhub.exception.RecursoNoEncontradoException;
import com.coworkhub.persistences.entities.Equipamiento;
import com.coworkhub.persistences.repositories.RepositorioEquipamientos;

@Service
@Transactional(readOnly = true)
public class ServicioEquipamientos {

    private final RepositorioEquipamientos repositorioEquipamientos;

    public ServicioEquipamientos(RepositorioEquipamientos repositorioEquipamientos) {
        this.repositorioEquipamientos = repositorioEquipamientos;
    }

    public List<Equipamiento> listarTodos() {
        return repositorioEquipamientos.findAll();
    }

    public Equipamiento buscarPorId(Long id) {
        return repositorioEquipamientos.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipamiento", id));
    }

    @Transactional
    public Equipamiento crear(SolicitudEquipamiento solicitud) {
        String nombre = texto(requerido(solicitud, "cuerpo de la solicitud").nombre(), "nombre");
        if (repositorioEquipamientos.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("DUPLICADO", "Ya existe un equipamiento llamado '" + nombre + "'");
        }
        return repositorioEquipamientos.save(new Equipamiento(nombre));
    }

    @Transactional
    public Equipamiento actualizar(Long id, SolicitudEquipamiento solicitud) {
        String nombre = texto(requerido(solicitud, "cuerpo de la solicitud").nombre(), "nombre");
        Equipamiento equipamiento = buscarPorId(id);
        if (repositorioEquipamientos.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("DUPLICADO", "Ya existe un equipamiento llamado '" + nombre + "'");
        }
        equipamiento.setNombre(nombre);
        return repositorioEquipamientos.save(equipamiento);
    }

    @Transactional
    public void eliminar(Long id) {
        Equipamiento equipamiento = buscarPorId(id);
        if (!equipamiento.getSalas().isEmpty()) {
            throw new ReglaNegocioException("EN_USO", "No se puede eliminar el equipamiento: hay salas que lo usan");
        }
        repositorioEquipamientos.delete(equipamiento);
    }
}
```

**📄 `src/main/java/com/coworkhub/controllers/ControladorEquipamientos.java`**

```java
package com.coworkhub.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coworkhub.dto.SolicitudEquipamiento;
import com.coworkhub.persistences.entities.Equipamiento;
import com.coworkhub.services.ServicioEquipamientos;

@RestController
@RequestMapping("/api/equipamientos")
public class ControladorEquipamientos {

    private final ServicioEquipamientos servicioEquipamientos;

    public ControladorEquipamientos(ServicioEquipamientos servicioEquipamientos) {
        this.servicioEquipamientos = servicioEquipamientos;
    }

    @GetMapping
    public List<Equipamiento> listarTodos() {
        return servicioEquipamientos.listarTodos();
    }

    @GetMapping("/{id}")
    public Equipamiento buscarPorId(@PathVariable Long id) {
        return servicioEquipamientos.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Equipamiento crear(@RequestBody SolicitudEquipamiento solicitud) {
        return servicioEquipamientos.crear(solicitud);
    }

    @PutMapping("/{id}")
    public Equipamiento actualizar(@PathVariable Long id, @RequestBody SolicitudEquipamiento solicitud) {
        return servicioEquipamientos.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicioEquipamientos.eliminar(id);
    }
}
```

**📄 `src/main/java/com/coworkhub/services/ServicioServiciosAdicionales.java`**

```java
package com.coworkhub.services;

import static com.coworkhub.services.Validaciones.montoPositivo;
import static com.coworkhub.services.Validaciones.requerido;
import static com.coworkhub.services.Validaciones.texto;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.dto.SolicitudServicioAdicional;
import com.coworkhub.exception.ReglaNegocioException;
import com.coworkhub.exception.RecursoNoEncontradoException;
import com.coworkhub.persistences.entities.ServicioAdicional;
import com.coworkhub.persistences.repositories.RepositorioReservas;
import com.coworkhub.persistences.repositories.RepositorioServiciosAdicionales;

@Service
@Transactional(readOnly = true)
public class ServicioServiciosAdicionales {

    private final RepositorioServiciosAdicionales repositorioServicios;
    private final RepositorioReservas repositorioReservas;

    public ServicioServiciosAdicionales(RepositorioServiciosAdicionales repositorioServicios,
                                        RepositorioReservas repositorioReservas) {
        this.repositorioServicios = repositorioServicios;
        this.repositorioReservas = repositorioReservas;
    }

    public List<ServicioAdicional> listarTodos() {
        return repositorioServicios.findAll();
    }

    public ServicioAdicional buscarPorId(Long id) {
        return repositorioServicios.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio adicional", id));
    }

    @Transactional
    public ServicioAdicional crear(SolicitudServicioAdicional solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String nombre = texto(solicitud.nombre(), "nombre");
        montoPositivo(solicitud.precioUnitario(), "precioUnitario");
        if (repositorioServicios.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("DUPLICADO", "Ya existe un servicio adicional llamado '" + nombre + "'");
        }
        return repositorioServicios.save(new ServicioAdicional(nombre, solicitud.precioUnitario()));
    }

    // Cambiar el precio no afecta a las reservas existentes: cada detalle guarda su precioUnitarioAplicado.
    @Transactional
    public ServicioAdicional actualizar(Long id, SolicitudServicioAdicional solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String nombre = texto(solicitud.nombre(), "nombre");
        montoPositivo(solicitud.precioUnitario(), "precioUnitario");
        ServicioAdicional servicio = buscarPorId(id);
        if (repositorioServicios.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("DUPLICADO", "Ya existe un servicio adicional llamado '" + nombre + "'");
        }
        servicio.actualizar(nombre, solicitud.precioUnitario());
        return repositorioServicios.save(servicio);
    }

    @Transactional
    public void eliminar(Long id) {
        ServicioAdicional servicio = buscarPorId(id);
        if (repositorioReservas.existsByDetallesServicioId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar el servicio: está usado en reservas");
        }
        repositorioServicios.delete(servicio);
    }
}
```

**📄 `src/main/java/com/coworkhub/controllers/ControladorServiciosAdicionales.java`**

```java
package com.coworkhub.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coworkhub.dto.SolicitudServicioAdicional;
import com.coworkhub.persistences.entities.ServicioAdicional;
import com.coworkhub.services.ServicioServiciosAdicionales;

@RestController
@RequestMapping("/api/servicios-adicionales")
public class ControladorServiciosAdicionales {

    private final ServicioServiciosAdicionales servicioServicios;

    public ControladorServiciosAdicionales(ServicioServiciosAdicionales servicioServicios) {
        this.servicioServicios = servicioServicios;
    }

    @GetMapping
    public List<ServicioAdicional> listarTodos() {
        return servicioServicios.listarTodos();
    }

    @GetMapping("/{id}")
    public ServicioAdicional buscarPorId(@PathVariable Long id) {
        return servicioServicios.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioAdicional crear(@RequestBody SolicitudServicioAdicional solicitud) {
        return servicioServicios.crear(solicitud);
    }

    @PutMapping("/{id}")
    public ServicioAdicional actualizar(@PathVariable Long id, @RequestBody SolicitudServicioAdicional solicitud) {
        return servicioServicios.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicioServicios.eliminar(id);
    }
}
```

**📄 `src/main/java/com/coworkhub/services/ServicioPlanes.java`**

```java
package com.coworkhub.services;

import static com.coworkhub.services.Validaciones.enteroNoNegativo;
import static com.coworkhub.services.Validaciones.enteroPositivo;
import static com.coworkhub.services.Validaciones.porcentaje;
import static com.coworkhub.services.Validaciones.requerido;
import static com.coworkhub.services.Validaciones.texto;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.dto.SolicitudPlan;
import com.coworkhub.exception.ReglaNegocioException;
import com.coworkhub.exception.RecursoNoEncontradoException;
import com.coworkhub.persistences.entities.PlanMembresia;
import com.coworkhub.persistences.repositories.RepositorioMiembros;
import com.coworkhub.persistences.repositories.RepositorioPlanes;

@Service
@Transactional(readOnly = true)
public class ServicioPlanes {

    private final RepositorioPlanes repositorioPlanes;
    private final RepositorioMiembros repositorioMiembros;

    public ServicioPlanes(RepositorioPlanes repositorioPlanes, RepositorioMiembros repositorioMiembros) {
        this.repositorioPlanes = repositorioPlanes;
        this.repositorioMiembros = repositorioMiembros;
    }

    public List<PlanMembresia> listarTodos() {
        return repositorioPlanes.findAll();
    }

    public PlanMembresia buscarPorId(Long id) {
        return repositorioPlanes.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan", id));
    }

    @Transactional
    public PlanMembresia crear(SolicitudPlan solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String nombre = texto(solicitud.nombre(), "nombre");
        int horas = enteroNoNegativo(solicitud.horasIncluidasMes(), "horasIncluidasMes");
        int descuento = porcentaje(solicitud.descuentoExcedente(), "descuentoExcedente");
        int maxReservas = enteroPositivo(solicitud.maxReservasActivas(), "maxReservasActivas");
        if (repositorioPlanes.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("DUPLICADO", "Ya existe un plan llamado '" + nombre + "'");
        }
        return repositorioPlanes.save(new PlanMembresia(nombre, horas, descuento, maxReservas));
    }

    @Transactional
    public PlanMembresia actualizar(Long id, SolicitudPlan solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String nombre = texto(solicitud.nombre(), "nombre");
        int horas = enteroNoNegativo(solicitud.horasIncluidasMes(), "horasIncluidasMes");
        int descuento = porcentaje(solicitud.descuentoExcedente(), "descuentoExcedente");
        int maxReservas = enteroPositivo(solicitud.maxReservasActivas(), "maxReservasActivas");
        PlanMembresia plan = buscarPorId(id);
        if (repositorioPlanes.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("DUPLICADO", "Ya existe un plan llamado '" + nombre + "'");
        }
        plan.actualizar(nombre, horas, descuento, maxReservas);
        return repositorioPlanes.save(plan);
    }

    @Transactional
    public void eliminar(Long id) {
        PlanMembresia plan = buscarPorId(id);
        if (repositorioMiembros.existsByPlanId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar el plan: tiene miembros asociados");
        }
        repositorioPlanes.delete(plan);
    }
}
```

**📄 `src/main/java/com/coworkhub/controllers/ControladorPlanes.java`**

```java
package com.coworkhub.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coworkhub.dto.SolicitudPlan;
import com.coworkhub.persistences.entities.PlanMembresia;
import com.coworkhub.services.ServicioPlanes;

@RestController
@RequestMapping("/api/planes")
public class ControladorPlanes {

    private final ServicioPlanes servicioPlanes;

    public ControladorPlanes(ServicioPlanes servicioPlanes) {
        this.servicioPlanes = servicioPlanes;
    }

    @GetMapping
    public List<PlanMembresia> listarTodos() {
        return servicioPlanes.listarTodos();
    }

    @GetMapping("/{id}")
    public PlanMembresia buscarPorId(@PathVariable Long id) {
        return servicioPlanes.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanMembresia crear(@RequestBody SolicitudPlan solicitud) {
        return servicioPlanes.crear(solicitud);
    }

    @PutMapping("/{id}")
    public PlanMembresia actualizar(@PathVariable Long id, @RequestBody SolicitudPlan solicitud) {
        return servicioPlanes.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicioPlanes.eliminar(id);
    }
}
```

### Paso 3.6 — Salas

`Sala` es el catálogo más rico porque tiene relaciones. Fijate cómo `ServicioSalas`
convierte los **ids** de la solicitud en **entidades**:

- `buscarSede(sedeId)`: busca la sede o lanza `404`.
- `buscarEquipamientos(ids)`: busca cada equipamiento; si alguno no existe, `404`.
- Al **actualizar**, `getEquipamientos().clear()` y luego `addAll(...)`: se reemplaza el
  conjunto completo, y JPA ajusta la tabla intermedia `sala_equipamiento`.
- **RN-11**: `existsBySedeIdAndNombreIgnoreCase` impide dos salas con el mismo nombre en
  la misma sede (pero permite el mismo nombre en sedes distintas). En la actualización se
  excluye a la propia sala (`...AndIdNot`).
- **RN-12** en `eliminar`: primero rechaza si hay reservas **activas**; luego, si hay solo
  historial, también rechaza (la base no permitiría borrarla) y sugiere desactivarla.
- Si el cliente omite `activa`, la sala se crea activa.

En el controlador, `GET /api/salas` acepta un filtro opcional `?sedeId=`. Que sea
opcional (`required = false`) permite tener **una sola ruta** para "todas" y "por sede".

**📄 `src/main/java/com/coworkhub/services/ServicioSalas.java`**

```java
package com.coworkhub.services;

import static com.coworkhub.services.Validaciones.enteroPositivo;
import static com.coworkhub.services.Validaciones.montoPositivo;
import static com.coworkhub.services.Validaciones.requerido;
import static com.coworkhub.services.Validaciones.texto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.dto.SolicitudSala;
import com.coworkhub.exception.ReglaNegocioException;
import com.coworkhub.exception.RecursoNoEncontradoException;
import com.coworkhub.persistences.entities.Equipamiento;
import com.coworkhub.persistences.entities.EstadoReserva;
import com.coworkhub.persistences.entities.Sala;
import com.coworkhub.persistences.entities.Sede;
import com.coworkhub.persistences.repositories.RepositorioEquipamientos;
import com.coworkhub.persistences.repositories.RepositorioReservas;
import com.coworkhub.persistences.repositories.RepositorioSalas;
import com.coworkhub.persistences.repositories.RepositorioSedes;

@Service
@Transactional(readOnly = true)
public class ServicioSalas {

    private final RepositorioSalas repositorioSalas;
    private final RepositorioSedes repositorioSedes;
    private final RepositorioEquipamientos repositorioEquipamientos;
    private final RepositorioReservas repositorioReservas;

    public ServicioSalas(RepositorioSalas repositorioSalas, RepositorioSedes repositorioSedes,
                         RepositorioEquipamientos repositorioEquipamientos,
                         RepositorioReservas repositorioReservas) {
        this.repositorioSalas = repositorioSalas;
        this.repositorioSedes = repositorioSedes;
        this.repositorioEquipamientos = repositorioEquipamientos;
        this.repositorioReservas = repositorioReservas;
    }

    public List<Sala> listarTodas() {
        return repositorioSalas.findAll();
    }

    public List<Sala> listarPorSede(Long sedeId) {
        return repositorioSalas.findBySedeIdOrderByNombre(sedeId);
    }

    public Sala buscarPorId(Long id) {
        return repositorioSalas.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sala", id));
    }

    @Transactional
    public Sala crear(SolicitudSala solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String nombre = texto(solicitud.nombre(), "nombre");
        Sede sede = buscarSede(solicitud.sedeId());
        if (repositorioSalas.existsBySedeIdAndNombreIgnoreCase(sede.getId(), nombre)) {
            throw new ReglaNegocioException("RN-11", "Ya existe una sala llamada '" + nombre + "' en la sede " + sede.getNombre());
        }
        Sala sala = new Sala(nombre, requerido(solicitud.tipo(), "tipo"),
                enteroPositivo(solicitud.capacidad(), "capacidad"),
                montoPositivo(solicitud.tarifaPorHora(), "tarifaPorHora"),
                solicitud.activa() == null || solicitud.activa(), sede);
        sala.getEquipamientos().addAll(buscarEquipamientos(solicitud.equipamientoIds()));
        return repositorioSalas.save(sala);
    }

    @Transactional
    public Sala actualizar(Long id, SolicitudSala solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String nombre = texto(solicitud.nombre(), "nombre");
        Sala sala = buscarPorId(id);
        Sede sede = buscarSede(solicitud.sedeId());
        if (repositorioSalas.existsBySedeIdAndNombreIgnoreCaseAndIdNot(sede.getId(), nombre, id)) {
            throw new ReglaNegocioException("RN-11", "Ya existe una sala llamada '" + nombre + "' en la sede " + sede.getNombre());
        }
        sala.actualizar(nombre, requerido(solicitud.tipo(), "tipo"),
                enteroPositivo(solicitud.capacidad(), "capacidad"),
                montoPositivo(solicitud.tarifaPorHora(), "tarifaPorHora"),
                solicitud.activa() == null || solicitud.activa(), sede);
        sala.getEquipamientos().clear();
        sala.getEquipamientos().addAll(buscarEquipamientos(solicitud.equipamientoIds()));
        return repositorioSalas.save(sala);
    }

    @Transactional
    public void eliminar(Long id) {
        Sala sala = buscarPorId(id);
        if (repositorioReservas.existsBySalaIdAndEstadoIn(id, EstadoReserva.ACTIVOS)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar la sala: tiene reservas activas");
        }
        if (repositorioReservas.existsBySalaId(id)) {
            throw new ReglaNegocioException("RN-12",
                    "No se puede eliminar la sala: tiene historial de reservas. Desactivala (activa=false) en su lugar");
        }
        repositorioSalas.delete(sala);
    }

    private Sede buscarSede(Long sedeId) {
        Long id = requerido(sedeId, "sedeId");
        return repositorioSedes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Sede", id));
    }

    private Set<Equipamiento> buscarEquipamientos(Set<Long> ids) {
        Set<Equipamiento> resultado = new HashSet<>();
        if (ids == null) {
            return resultado;
        }
        for (Long id : ids) {
            resultado.add(repositorioEquipamientos.findById(requerido(id, "equipamientoIds"))
                    .orElseThrow(() -> new RecursoNoEncontradoException("Equipamiento", id)));
        }
        return resultado;
    }
}
```

**📄 `src/main/java/com/coworkhub/controllers/ControladorSalas.java`**

```java
package com.coworkhub.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coworkhub.dto.SolicitudSala;
import com.coworkhub.persistences.entities.Sala;
import com.coworkhub.services.ServicioSalas;

@RestController
@RequestMapping("/api/salas")
public class ControladorSalas {

    private final ServicioSalas servicioSalas;

    public ControladorSalas(ServicioSalas servicioSalas) {
        this.servicioSalas = servicioSalas;
    }

    // GET /api/salas            → todas
    // GET /api/salas?sedeId=1   → solo las de una sede
    @GetMapping
    public List<Sala> listar(@RequestParam(required = false) Long sedeId) {
        return sedeId == null ? servicioSalas.listarTodas() : servicioSalas.listarPorSede(sedeId);
    }

    @GetMapping("/{id}")
    public Sala buscarPorId(@PathVariable Long id) {
        return servicioSalas.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Sala crear(@RequestBody SolicitudSala solicitud) {
        return servicioSalas.crear(solicitud);
    }

    @PutMapping("/{id}")
    public Sala actualizar(@PathVariable Long id, @RequestBody SolicitudSala solicitud) {
        return servicioSalas.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicioSalas.eliminar(id);
    }
}
```

## ✅ Checkpoint 3a-2 — Catálogos completos

```bash
# Salas de la sede 2 (ordenadas por nombre; cada sala trae su sede y su equipamiento)
curl -s "http://localhost:8080/api/salas?sedeId=2"

# Crear una sala con equipamiento (debe responder 201)
curl -s -X POST http://localhost:8080/api/salas \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Sala Amazonas","tipo":"SALA_REUNION","capacidad":6,"tarifaPorHora":25.00,"activa":true,"sedeId":1,"equipamientoIds":[1,2]}'
```

Respuesta esperada de la creación:

```json
{
  "id": 8,
  "nombre": "Sala Amazonas",
  "tipo": "SALA_REUNION",
  "capacidad": 6,
  "tarifaPorHora": 25.0,
  "activa": true,
  "sede": { "id": 1, "nombre": "Sede Centro", "ciudad": "Bogotá",
            "direccion": "Carrera 7 # 32-16", "horaApertura": "07:00:00", "horaCierre": "22:00:00" },
  "equipamientos": [ { "id": 1, "nombre": "Proyector" }, { "id": 2, "nombre": "Pizarra" } ]
}
```

Verificá también:

| Llamada | Resultado esperado en esta fase |
|---|---|
| `GET /api/planes` | `200`, 4 planes (`Flex`, `Básico`, `Profesional`, `Corporativo`) |
| `GET /api/servicios-adicionales` | `200`, `Catering` (25.0), `Impresión` (5.0), `Soporte técnico` (40.0) |
| `GET /api/equipamientos` | `200`, 4 equipamientos |
| Crear una sala con el mismo nombre en la misma sede | `500` por ahora (será `409`, RN-11) |
| Enviar `"tipo": "OTRO"` | `400` (Spring rechaza el valor de la enumeración) |

Comprobaste algo importante: en `GET /api/salas` la respuesta incluye la `sede` y los
`equipamientos`, pero **no** aparece la lista `salas` dentro de la sede ni las `salas`
dentro de cada equipamiento. Es el efecto de los `@JsonIgnore` de la Fase 2.

### Paso 3.7 — Commit

```bash
git add .
git commit -m "fase 3a: excepciones, DTOs y catálogos REST"
```

**Siguiente →** [Fase 3b — Miembros, disponibilidad y consumo](03b-fase-3-miembros.md)
