# 👥 Fase 3b — Miembros, consumo mensual y disponibilidad

**Navegación**: [Índice](README.md) · ← [Fase 3a — Catálogos](03a-fase-3-catalogos.md) · Siguiente → [Fase 3c — Reservas](03c-fase-3-reservas.md)

## 🎯 Qué vas a lograr

Registrar miembros (junto con su usuario), suspenderlos y consultarlos; calcular su
**consumo mensual** de horas (RF-10); y buscar **salas disponibles** en un horario (RF-03).

## 🪜 Paso a paso

### Paso 3.8 — `ServicioMiembros`

Recorré sus decisiones:

- **`crear` es una sola transacción** que guarda el `Miembro` y su `Usuario`. Como
  `Miembro.usuario` tiene `cascade = ALL`, alcanza con `miembro.setUsuario(...)` y
  `save(miembro)`: JPA guarda ambos. Si algo falla a mitad de camino, no queda ninguno
  (RNF-04).
- **Contraseña**: se codifica con `passwordEncoder.encode(...)` (BCrypt) antes de guardar.
  La contraseña original nunca se almacena ni se devuelve. Se exige un mínimo de 6
  caracteres.
- **RN-11**: antes de guardar consulta `existsByDocumento` y `existsByEmailIgnoreCase`
  (el email se compara sin distinguir mayúsculas). También rechaza un `nombreUsuario`
  ya usado.
- **`actualizar`** cambia nombre, email y plan; el documento **no** es modificable. Al
  validar el email único excluye al propio miembro (`existsByEmailIgnoreCaseAndIdNot`).
- **`suspender` / `activar`**: solo cambian el `estado`. El miembro suspendido conserva
  sus reservas pero no podrá crear nuevas (RN-05, en la Fase 3c).
- **RN-12 en `eliminar`**: si el miembro tiene **cualquier** reserva, `409`. Si no, se
  elimina y el `cascade` + `orphanRemoval` de `Miembro.usuario` elimina también su
  usuario (así, ese usuario ya no puede iniciar sesión).

**📄 `src/main/java/com/coworkhub/services/ServicioMiembros.java`**

```java
package com.coworkhub.services;

import static com.coworkhub.services.Validaciones.requerido;
import static com.coworkhub.services.Validaciones.texto;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.dto.SolicitudActualizarMiembro;
import com.coworkhub.dto.SolicitudMiembro;
import com.coworkhub.exception.ReglaNegocioException;
import com.coworkhub.exception.RecursoNoEncontradoException;
import com.coworkhub.exception.SolicitudInvalidaException;
import com.coworkhub.persistences.entities.EstadoMiembro;
import com.coworkhub.persistences.entities.Miembro;
import com.coworkhub.persistences.entities.PlanMembresia;
import com.coworkhub.persistences.repositories.RepositorioMiembros;
import com.coworkhub.persistences.repositories.RepositorioPlanes;
import com.coworkhub.persistences.repositories.RepositorioReservas;
import com.coworkhub.security.persistences.entities.Rol;
import com.coworkhub.security.persistences.entities.Usuario;
import com.coworkhub.security.persistences.repositories.RepositorioUsuarios;

@Service
@Transactional(readOnly = true)
public class ServicioMiembros {

    private static final int LARGO_MINIMO_CONTRASENA = 6;

    private final RepositorioMiembros repositorioMiembros;
    private final RepositorioPlanes repositorioPlanes;
    private final RepositorioUsuarios repositorioUsuarios;
    private final RepositorioReservas repositorioReservas;
    private final PasswordEncoder passwordEncoder;

    public ServicioMiembros(RepositorioMiembros repositorioMiembros, RepositorioPlanes repositorioPlanes,
                            RepositorioUsuarios repositorioUsuarios, RepositorioReservas repositorioReservas,
                            PasswordEncoder passwordEncoder) {
        this.repositorioMiembros = repositorioMiembros;
        this.repositorioPlanes = repositorioPlanes;
        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioReservas = repositorioReservas;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Miembro> listarTodos() {
        return repositorioMiembros.findAll();
    }

    public Miembro buscarPorId(Long id) {
        return repositorioMiembros.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Miembro", id));
    }

    // RF-02: registra al miembro junto con su usuario, en una sola transacción.
    @Transactional
    public Miembro crear(SolicitudMiembro solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String documento = texto(solicitud.documento(), "documento");
        String nombre = texto(solicitud.nombre(), "nombre");
        String email = texto(solicitud.email(), "email");
        String nombreUsuario = texto(solicitud.nombreUsuario(), "nombreUsuario");
        String contrasena = texto(solicitud.contrasena(), "contrasena");
        if (contrasena.length() < LARGO_MINIMO_CONTRASENA) {
            throw new SolicitudInvalidaException(
                    "La contrasena debe tener al menos " + LARGO_MINIMO_CONTRASENA + " caracteres");
        }
        PlanMembresia plan = buscarPlan(solicitud.planId());

        if (repositorioMiembros.existsByDocumento(documento)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un miembro con el documento " + documento);
        }
        if (repositorioMiembros.existsByEmailIgnoreCase(email)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un miembro con el email " + email);
        }
        if (repositorioUsuarios.existsByNombreUsuario(nombreUsuario)) {
            throw new ReglaNegocioException("RN-11", "El nombre de usuario '" + nombreUsuario + "' ya está en uso");
        }

        Miembro miembro = new Miembro(documento, nombre, email, plan);
        miembro.setUsuario(new Usuario(nombreUsuario, passwordEncoder.encode(contrasena), Rol.MIEMBRO));
        return repositorioMiembros.save(miembro);
    }

    @Transactional
    public Miembro actualizar(Long id, SolicitudActualizarMiembro solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String nombre = texto(solicitud.nombre(), "nombre");
        String email = texto(solicitud.email(), "email");
        PlanMembresia plan = buscarPlan(solicitud.planId());
        Miembro miembro = buscarPorId(id);
        if (repositorioMiembros.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un miembro con el email " + email);
        }
        miembro.actualizar(nombre, email, plan);
        return repositorioMiembros.save(miembro);
    }

    @Transactional
    public Miembro suspender(Long id) {
        Miembro miembro = buscarPorId(id);
        miembro.setEstado(EstadoMiembro.SUSPENDIDO);
        return repositorioMiembros.save(miembro);
    }

    @Transactional
    public Miembro activar(Long id) {
        Miembro miembro = buscarPorId(id);
        miembro.setEstado(EstadoMiembro.ACTIVO);
        return repositorioMiembros.save(miembro);
    }

    // RN-12: no se elimina un miembro con reservas. Si tiene usuario, cascade lo elimina también.
    @Transactional
    public void eliminar(Long id) {
        Miembro miembro = buscarPorId(id);
        if (repositorioReservas.existsByMiembroId(id)) {
            throw new ReglaNegocioException("RN-12",
                    "No se puede eliminar el miembro: tiene reservas. Suspendelo en su lugar");
        }
        repositorioMiembros.delete(miembro);
    }

    private PlanMembresia buscarPlan(Long planId) {
        Long id = requerido(planId, "planId");
        return repositorioPlanes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Plan", id));
    }
}
```

### Paso 3.9 — `ServicioConsumo` (RF-10)

Calcula el resumen mensual de un miembro a partir de la suma de `minutosConsumidos` de
sus reservas cuyo `inicio` cae en ese mes (consulta `sumarMinutosConsumidos` de la
Fase 2). Con ese total y las horas del plan obtiene cuatro números:

| Campo | Fórmula (en minutos) |
|---|---|
| `horasIncluidas` | `plan.horasIncluidasMes × 60` |
| `horasUsadas` | suma de `minutosConsumidos` del mes |
| `horasIncluidasUsadas` | `min(usadas, incluidas)` |
| `horasRestantes` | `max(0, incluidas − usadas)` |
| `horasExcedentes` | `max(0, usadas − incluidas)` |

**Ejemplo** (escenario 12): plan con 2 h incluidas y 3 h reservadas en el mes →
`horasUsadas = 3`, `horasIncluidasUsadas = 2`, `horasRestantes = 0`, `horasExcedentes = 1`.

Si no se indica el mes se usa el actual, tomado del **`Clock` inyectado** (nunca de
`LocalDateTime.now()` directo). Los minutos se convierten a horas con dos decimales.

**📄 `src/main/java/com/coworkhub/services/ServicioConsumo.java`**

```java
package com.coworkhub.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.YearMonth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.dto.ResumenConsumo;
import com.coworkhub.exception.RecursoNoEncontradoException;
import com.coworkhub.persistences.entities.Miembro;
import com.coworkhub.persistences.repositories.RepositorioMiembros;
import com.coworkhub.persistences.repositories.RepositorioReservas;

@Service
@Transactional(readOnly = true)
public class ServicioConsumo {

    private final RepositorioMiembros repositorioMiembros;
    private final RepositorioReservas repositorioReservas;
    private final Clock reloj;

    public ServicioConsumo(RepositorioMiembros repositorioMiembros, RepositorioReservas repositorioReservas,
                           Clock reloj) {
        this.repositorioMiembros = repositorioMiembros;
        this.repositorioReservas = repositorioReservas;
        this.reloj = reloj;
    }

    // RF-10: resumen mensual del consumo de horas del plan. Si no se indica el mes, usa el actual.
    public ResumenConsumo resumenMensual(Long miembroId, YearMonth mes) {
        Miembro miembro = repositorioMiembros.findById(miembroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Miembro", miembroId));
        YearMonth mesConsultado = mes != null ? mes : YearMonth.now(reloj);

        long minutosUsados = repositorioReservas.sumarMinutosConsumidos(miembroId,
                mesConsultado.atDay(1).atStartOfDay(),
                mesConsultado.plusMonths(1).atDay(1).atStartOfDay());
        long minutosIncluidos = miembro.getPlan().getHorasIncluidasMes() * 60L;

        return new ResumenConsumo(
                miembro.getId(),
                miembro.getNombre(),
                miembro.getPlan().getNombre(),
                mesConsultado.toString(),
                horas(minutosIncluidos),
                horas(minutosUsados),
                horas(Math.min(minutosUsados, minutosIncluidos)),
                horas(Math.max(0, minutosIncluidos - minutosUsados)),
                horas(Math.max(0, minutosUsados - minutosIncluidos)));
    }

    private BigDecimal horas(long minutos) {
        return BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}
```

### Paso 3.10 — `ControladorMiembros`

Además del CRUD, expone:

- `PATCH /api/miembros/{id}/suspender` y `/activar`: `PATCH` es el verbo para
  **modificaciones parciales** (acá, cambiar solo el estado).
- `GET /api/miembros/{id}/consumo?mes=2030-06`: el parámetro `mes` es un `YearMonth`;
  Spring lo convierte desde el formato `yyyy-MM`. Es opcional.

El controlador recibe **dos** servicios (`ServicioMiembros` y `ServicioConsumo`): es
correcto, porque sigue sin tocar ningún repositorio.

**📄 `src/main/java/com/coworkhub/controllers/ControladorMiembros.java`**

```java
package com.coworkhub.controllers;

import java.time.YearMonth;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coworkhub.dto.ResumenConsumo;
import com.coworkhub.dto.SolicitudActualizarMiembro;
import com.coworkhub.dto.SolicitudMiembro;
import com.coworkhub.persistences.entities.Miembro;
import com.coworkhub.services.ServicioConsumo;
import com.coworkhub.services.ServicioMiembros;

@RestController
@RequestMapping("/api/miembros")
public class ControladorMiembros {

    private final ServicioMiembros servicioMiembros;
    private final ServicioConsumo servicioConsumo;

    public ControladorMiembros(ServicioMiembros servicioMiembros, ServicioConsumo servicioConsumo) {
        this.servicioMiembros = servicioMiembros;
        this.servicioConsumo = servicioConsumo;
    }

    @GetMapping
    public List<Miembro> listarTodos() {
        return servicioMiembros.listarTodos();
    }

    @GetMapping("/{id}")
    public Miembro buscarPorId(@PathVariable Long id) {
        return servicioMiembros.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Miembro crear(@RequestBody SolicitudMiembro solicitud) {
        return servicioMiembros.crear(solicitud);
    }

    @PutMapping("/{id}")
    public Miembro actualizar(@PathVariable Long id, @RequestBody SolicitudActualizarMiembro solicitud) {
        return servicioMiembros.actualizar(id, solicitud);
    }

    @PatchMapping("/{id}/suspender")
    public Miembro suspender(@PathVariable Long id) {
        return servicioMiembros.suspender(id);
    }

    @PatchMapping("/{id}/activar")
    public Miembro activar(@PathVariable Long id) {
        return servicioMiembros.activar(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicioMiembros.eliminar(id);
    }

    // GET /api/miembros/1/consumo?mes=2030-06   (sin "mes" usa el mes actual)
    @GetMapping("/{id}/consumo")
    public ResumenConsumo consumo(@PathVariable Long id, @RequestParam(required = false) YearMonth mes) {
        return servicioConsumo.resumenMensual(id, mes);
    }
}
```

### Paso 3.11 — `ServicioDisponibilidad` y `ControladorDisponibilidad` (RF-03)

El servicio valida los parámetros obligatorios (`sedeId`, `fecha`, `horaInicio`,
`horaFin`) y delega la búsqueda pesada a la consulta `buscarDisponibles` (Fase 2), que
ya excluye las salas ocupadas y las inactivas. Luego aplica los filtros opcionales:

- **`capacidadMinima`**: por defecto 1; la consulta descarta salas más chicas.
- **`equipamientoIds`**: se filtra en memoria; una sala califica solo si tiene **todos**
  los equipamientos pedidos (`containsAll`).
- **Fuera del horario de la sede** devuelve lista vacía: nada es reservable ahí (RN-03).

El controlador recibe todo por `@RequestParam` (con `required = false` para que el
**servicio** decida qué falta y responda con un mensaje claro, en lugar de que Spring
responda su propio error). Los tipos `LocalDate` y `LocalTime` se convierten desde
`yyyy-MM-dd` y `HH:mm`. `equipamientoIds=1,4` llega como `Set<Long>`.

**📄 `src/main/java/com/coworkhub/services/ServicioDisponibilidad.java`**

```java
package com.coworkhub.services;

import static com.coworkhub.services.Validaciones.requerido;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.exception.RecursoNoEncontradoException;
import com.coworkhub.exception.SolicitudInvalidaException;
import com.coworkhub.persistences.entities.Equipamiento;
import com.coworkhub.persistences.entities.EstadoReserva;
import com.coworkhub.persistences.entities.Sala;
import com.coworkhub.persistences.entities.Sede;
import com.coworkhub.persistences.repositories.RepositorioSalas;
import com.coworkhub.persistences.repositories.RepositorioSedes;

@Service
@Transactional(readOnly = true)
public class ServicioDisponibilidad {

    private final RepositorioSalas repositorioSalas;
    private final RepositorioSedes repositorioSedes;

    public ServicioDisponibilidad(RepositorioSalas repositorioSalas, RepositorioSedes repositorioSedes) {
        this.repositorioSalas = repositorioSalas;
        this.repositorioSedes = repositorioSedes;
    }

    // RF-03: salas de una sede libres en una fecha y rango horario.
    public List<Sala> buscar(Long sedeId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                             Integer capacidadMinima, Set<Long> equipamientoIds) {
        Long idSede = requerido(sedeId, "sedeId");
        requerido(fecha, "fecha");
        requerido(horaInicio, "horaInicio");
        requerido(horaFin, "horaFin");
        if (!horaInicio.isBefore(horaFin)) {
            throw new SolicitudInvalidaException("horaInicio debe ser anterior a horaFin");
        }
        int capacidad = capacidadMinima == null ? 1 : capacidadMinima;

        Sede sede = repositorioSedes.findById(idSede)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede", idSede));

        // Fuera del horario de la sede no hay nada reservable (RN-03).
        if (horaInicio.isBefore(sede.getHoraApertura()) || horaFin.isAfter(sede.getHoraCierre())) {
            return List.of();
        }

        LocalDateTime inicio = fecha.atTime(horaInicio);
        LocalDateTime fin = fecha.atTime(horaFin);
        List<Sala> libres = repositorioSalas.buscarDisponibles(idSede, capacidad, inicio, fin, EstadoReserva.ACTIVOS);

        if (equipamientoIds == null || equipamientoIds.isEmpty()) {
            return libres;
        }
        return libres.stream()
                .filter(sala -> sala.getEquipamientos().stream()
                        .map(Equipamiento::getId)
                        .collect(Collectors.toSet())
                        .containsAll(equipamientoIds))
                .toList();
    }
}
```

**📄 `src/main/java/com/coworkhub/controllers/ControladorDisponibilidad.java`**

```java
package com.coworkhub.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coworkhub.persistences.entities.Sala;
import com.coworkhub.services.ServicioDisponibilidad;

@RestController
@RequestMapping("/api/disponibilidad")
public class ControladorDisponibilidad {

    private final ServicioDisponibilidad servicioDisponibilidad;

    public ControladorDisponibilidad(ServicioDisponibilidad servicioDisponibilidad) {
        this.servicioDisponibilidad = servicioDisponibilidad;
    }

    // GET /api/disponibilidad?sedeId=1&fecha=2030-06-04&horaInicio=10:00&horaFin=12:00
    //     [&capacidadMinima=6][&equipamientoIds=1,2]
    @GetMapping
    public List<Sala> buscar(@RequestParam(required = false) Long sedeId,
                             @RequestParam(required = false) LocalDate fecha,
                             @RequestParam(required = false) LocalTime horaInicio,
                             @RequestParam(required = false) LocalTime horaFin,
                             @RequestParam(required = false) Integer capacidadMinima,
                             @RequestParam(required = false) Set<Long> equipamientoIds) {
        return servicioDisponibilidad.buscar(sedeId, fecha, horaInicio, horaFin, capacidadMinima, equipamientoIds);
    }
}
```

## ✅ Checkpoint 3b

```bash
# Registrar un miembro con su usuario (201)
curl -s -X POST http://localhost:8080/api/miembros -H 'Content-Type: application/json' \
  -d '{"documento":"9090909","nombre":"Mateo Mini","email":"mateo@coworkhub.test","planId":2,"nombreUsuario":"mateo","contrasena":"mateo123"}'
```

```json
{
  "id": 6,
  "documento": "9090909",
  "nombre": "Mateo Mini",
  "email": "mateo@coworkhub.test",
  "estado": "ACTIVO",
  "plan": { "id": 2, "nombre": "Básico", "horasIncluidasMes": 10,
            "descuentoExcedente": 10, "maxReservasActivas": 3 }
}
```

Fijate que la respuesta **no** contiene el usuario ni la contraseña.

```bash
# Suspender al miembro 6 → "estado": "SUSPENDIDO"
curl -s -X PATCH http://localhost:8080/api/miembros/6/suspender

# Consumo mensual de Ana (todavía no hay reservas)
curl -s "http://localhost:8080/api/miembros/1/consumo?mes=2030-06"
```

```json
{
  "miembroId": 1, "miembro": "Ana Torres", "plan": "Profesional", "mes": "2030-06",
  "horasIncluidas": 40.0, "horasUsadas": 0.0, "horasIncluidasUsadas": 0.0,
  "horasRestantes": 40.0, "horasExcedentes": 0.0
}
```

```bash
# Salas libres de la sede 1 el 2030-06-04 de 10:00 a 12:00
curl -s "http://localhost:8080/api/disponibilidad?sedeId=1&fecha=2030-06-04&horaInicio=10:00&horaFin=12:00"
```

Como todavía no hay reservas, aparecen **todas** las salas activas de la sede 1
(`Escritorio A1`, `Oficina 101`, `Sala Andes`, `Sala Caribe`, más las que hayas creado).
`Escritorio B1` no aparece: es de otra sede y además está inactiva.

```bash
# Con filtros: sede 2, capacidad mínima 6, con Proyector (1) y Aire acondicionado (4)
curl -s "http://localhost:8080/api/disponibilidad?sedeId=2&fecha=2030-06-04&horaInicio=09:00&horaFin=10:00&capacidadMinima=6&equipamientoIds=1,4"
```

Debe devolver solo `Sala Pacífico`.

### Paso 3.12 — Commit

```bash
git add .
git commit -m "fase 3b: miembros, consumo mensual y disponibilidad"
```

**Siguiente →** [Fase 3c — Reservas](03c-fase-3-reservas.md)
