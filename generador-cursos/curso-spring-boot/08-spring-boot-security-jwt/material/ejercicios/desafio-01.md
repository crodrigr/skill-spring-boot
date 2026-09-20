# 🏆 Desafío 01 — Spring Security y JWT sobre la API de citas

## 🧩 Problema

MediSalud necesita el mismo tratamiento en su API de `Cita` (Desafío del
Módulo 5/7) — un controlador distinto del usado en el Taller. Integrá
Spring Security con JWT sobre `ControladorCitas` por tu cuenta: creá
`Usuario`, `RepositorioUsuarios`, `ServicioDetallesUsuario`,
`ConfiguracionSeguridad`, `UtilJwt`, `FiltroAutenticacionJwt` y
`ControladorAutenticacion`, sin ningún scaffold provisto (a diferencia
del Taller, donde sí se guían los pasos), verificando el resultado con
al menos: login exitoso, acceso con token válido, rechazo sin token, y
rechazo con credenciales inválidas en Insomnia.

## 💻 Código o contexto de partida

Este es el estado de `Cita`/`ServicioCitas`/`ControladorCitas` ya
resuelto en el Desafío del Módulo 7 (con `CitaNoEncontradaException` y su
manejo global ya aplicados). Ninguna de estas clases debe cambiar su
comportamiento en este Desafío.

```java
// Cita.java (Módulo 3, sin cambios)
@Entity
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    private String motivo;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    protected Cita() {
    }

    public Cita(LocalDate fecha, String motivo, Paciente paciente) {
        this.fecha = fecha;
        this.motivo = motivo;
        this.paciente = paciente;
    }

    public Long getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Paciente getPaciente() { return paciente; }
}
```

```java
// RepositorioCitas.java (Módulo 3, sin cambios)
public interface RepositorioCitas extends JpaRepository<Cita, Long> {
}
```

```java
// CitaNoEncontradaException.java (Módulo 7, sin cambios)
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CitaNoEncontradaException extends RuntimeException {

    public CitaNoEncontradaException(Long id) {
        super("No existe una cita con id " + id);
    }
}
```

```java
// ServicioCitas.java (Módulo 7, sin cambios)
@Service
public class ServicioCitas {

    private final RepositorioCitas repositorioCitas;

    public ServicioCitas(RepositorioCitas repositorioCitas) {
        this.repositorioCitas = repositorioCitas;
    }

    public List<Cita> listarTodos() {
        return repositorioCitas.findAll();
    }

    public Cita buscarPorId(Long id) {
        return repositorioCitas.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(id));
    }

    public Cita crear(Cita cita) {
        return repositorioCitas.save(cita);
    }

    public Cita actualizar(Long id, Cita datos) {
        Cita cita = buscarPorId(id);
        cita.setFecha(datos.getFecha());
        cita.setMotivo(datos.getMotivo());
        return repositorioCitas.save(cita);
    }

    public void eliminar(Long id) {
        Cita cita = buscarPorId(id);
        repositorioCitas.delete(cita);
    }
}
```

```java
// ControladorCitas.java (Módulo 7, sin cambios)
@RestController
@RequestMapping("/citas")
public class ControladorCitas {

    private final ServicioCitas servicioCitas;

    public ControladorCitas(ServicioCitas servicioCitas) {
        this.servicioCitas = servicioCitas;
    }

    @GetMapping
    public List<Cita> listarTodos() {
        return servicioCitas.listarTodos();
    }

    @GetMapping("/{id}")
    public Cita buscarPorId(@PathVariable Long id) {
        return servicioCitas.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Cita> crear(@RequestBody Cita cita) {
        Cita creada = servicioCitas.crear(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public Cita actualizar(@PathVariable Long id, @RequestBody Cita datos) {
        return servicioCitas.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicioCitas.eliminar(id);
        return ResponseEntity.ok().build();
    }
}
```

1. Creá `Usuario` (entidad con `nombreUsuario`, `contrasena`
   codificada y `rol`) y `RepositorioUsuarios`.
2. Creá `ServicioDetallesUsuario` (`UserDetailsService`) respaldado por
   `RepositorioUsuarios`.
3. Creá `ControladorAutenticacion` (`POST /auth/login`) que emita un JWT
   usando un `UtilJwt` propio.
4. Creá `FiltroAutenticacionJwt` que valide el JWT en cada solicitud.
5. Configurá `ConfiguracionSeguridad` como `STATELESS`, permitiendo
   `/auth/login` sin autenticación previa y exigiendo autenticación para
   el resto, con `FiltroAutenticacionJwt` registrado antes del filtro
   estándar de usuario/contraseña.
6. Probá en Insomnia: login exitoso, acceso a `GET /citas` con token
   válido, rechazo sin ningún token, y rechazo con credenciales
   inválidas en el login.

## 📏 Criterios de evaluación de la solución

- `Cita`, `ServicioCitas` y `ControladorCitas` no cambian ninguna línea
  de su comportamiento (FR-015).
- El login con credenciales válidas devuelve un JWT; con credenciales
  inválidas, responde `401`.
- `GET /citas` responde `200` con un token válido y `401` sin ningún
  token.
- `FiltroAutenticacionJwt` no consulta `RepositorioCitas` ni ninguna
  entidad de dominio.

## 🚧 Restricciones

Las entidades y endpoints deben ser distintos de los usados en el
Taller: no se acepta reutilizar el mismo controlador del Taller como
recurso propio de este desafío.

## 📊 Dificultad

Desafío

## 🎓 Resultados de aprendizaje

RA-5, RA-9, RA-10
