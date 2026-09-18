# 🏆 Desafío 01 — Manejo de excepciones en la API de citas

## 🧩 Problema

MediSalud necesita el mismo tratamiento en su API de `Cita` (Desafío del
Módulo 5) — un controlador distinto del usado en el Taller. Creá
`CitaNoEncontradaException` y un `@ControllerAdvice` por tu cuenta,
modificando `ServicioCitas`/`ControladorCitas` para reemplazar el manejo
manual, documentando explícitamente el cambio de comportamiento respecto
al Módulo 5 (igual que en los Ejemplos y en el Taller).

## 💻 Código o contexto de partida

```java
// Cita.java (Módulo 3, reutilizada tal cual)
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
// RepositorioCitas.java (Módulo 3, reutilizada tal cual)
public interface RepositorioCitas extends JpaRepository<Cita, Long> {
}
```

```java
// ServicioCitas.java (Módulo 5, con buscarPorId todavía devolviendo Optional)
@Service
public class ServicioCitas {

    private final RepositorioCitas repositorioCitas;

    public ServicioCitas(RepositorioCitas repositorioCitas) {
        this.repositorioCitas = repositorioCitas;
    }

    public List<Cita> listarTodos() {
        return repositorioCitas.findAll();
    }

    public Optional<Cita> buscarPorId(Long id) {
        return repositorioCitas.findById(id);
    }

    public Cita crear(Cita cita) {
        return repositorioCitas.save(cita);
    }

    public Optional<Cita> actualizar(Long id, Cita datos) {
        return repositorioCitas.findById(id)
                .map(cita -> {
                    cita.setFecha(datos.getFecha());
                    cita.setMotivo(datos.getMotivo());
                    return repositorioCitas.save(cita);
                });
    }

    public boolean eliminar(Long id) {
        if (!repositorioCitas.existsById(id)) {
            return false;
        }
        repositorioCitas.deleteById(id);
        return true;
    }
}
```

```java
// ControladorCitas.java (Módulo 5, con manejo manual de errores)
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
    public ResponseEntity<Cita> buscarPorId(@PathVariable Long id) {
        return servicioCitas.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Cita> crear(@RequestBody Cita cita) {
        Cita creada = servicioCitas.crear(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> actualizar(@PathVariable Long id, @RequestBody Cita datos) {
        return servicioCitas.actualizar(id, datos)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boolean existia = servicioCitas.eliminar(id);
        return existia ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
```

1. Creá `CitaNoEncontradaException` (`@ResponseStatus(HttpStatus.NOT_FOUND)`).
2. Modificá `ServicioCitas.buscarPorId` para que devuelva `Cita`
   directamente y lance la excepción; actualizá `actualizar` y
   `eliminar` para reutilizarlo. **Documentá explícitamente**, en tu
   entrega, qué comportamiento tenían estos métodos antes (Módulo 5) y
   cuál tienen ahora — el mismo criterio aplicado en los Ejemplos 03-06 y
   en el Taller.
3. Quitá de `ControladorCitas` cualquier `ResponseEntity.notFound()`
   manual.
4. Creá un `@ControllerAdvice` que centralice el manejo de
   `CitaNoEncontradaException`, devolviendo `{"error": "<mensaje>"}"` con
   `404`.
5. Sin caso de duplicado: `Cita` no tiene un campo único natural (a
   diferencia de `Libro.isbn` o `Paciente.codigo`) sobre el cual definir
   esa regla de negocio.
6. Probá en Insomnia el caso de éxito y el de error, verificando el
   nuevo cuerpo de respuesta.

## 📏 Criterios de evaluación de la solución

- `CitaNoEncontradaException` extiende `RuntimeException` y tiene
  `@ResponseStatus(HttpStatus.NOT_FOUND)`.
- `ServicioCitas.buscarPorId` ya no devuelve `Optional<Cita>`: devuelve
  `Cita` y lanza la excepción si no existe.
- `ControladorCitas` no conserva ningún `ResponseEntity.notFound()`
  manual.
- Existe un `@ControllerAdvice` (puede ser una clase nueva o, si el
  proyecto ya lo tuviera, extenderlo) que centraliza el manejo de
  `CitaNoEncontradaException` con el cuerpo `{"error": "<mensaje>"}"`.
- La entrega documenta explícitamente el cambio de comportamiento de
  `buscarPorId`/`actualizar`/`eliminar` respecto al Módulo 5.

## 🚧 Restricciones

Las entidades y endpoints deben ser distintos de los usados en el
Taller: no se acepta reutilizar el mismo recurso del Taller como recurso
propio de este desafío.

## 📊 Dificultad

Desafío

## 🎓 Resultados de aprendizaje

RA-4, RA-5, RA-6, RA-8
