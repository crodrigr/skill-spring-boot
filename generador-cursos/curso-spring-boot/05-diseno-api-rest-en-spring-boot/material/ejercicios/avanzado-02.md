# 🔴 Avanzado 02 — Diagnosticar una recursión infinita en JSON

## 🧩 Problema

Un compañero de equipo expuso `Cita` vía REST y te muestra este error:

```text
Método: GET
URL: http://localhost:8080/citas/1
Respuesta: 500 Internal Server Error
com.fasterxml.jackson.databind.JsonMappingException: Infinite recursion (StackOverflowError)
```

**Preguntas**:

1. ¿Por qué serializar `Cita` a JSON entra en recursión infinita?
2. ¿Cómo lo corregirías, sin dejar de incluir los datos del `Paciente` en
   la respuesta de `Cita`?

## 💻 Código o contexto de partida

```java
// Paciente.java — capa persistences (com.medisalud.persistences.entities)
// (Módulo 3, reutilizada tal cual)
@Entity
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigo;

    private String nombre;

    @OneToMany(mappedBy = "paciente") // lado inverso: la FK vive en Cita
    private List<Cita> citas = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "historia_clinica_id")
    private HistoriaClinica historiaClinica;

    protected Paciente() {
    }

    public Paciente(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public List<Cita> getCitas() { return citas; }

    public void asignarHistoriaClinica(HistoriaClinica historiaClinica) {
        this.historiaClinica = historiaClinica;
    }
}
```

```java
// Cita.java — capa persistences (com.medisalud.persistences.entities)
// (Módulo 3, reutilizada; se agrega getId(), que el Módulo 3
// no necesitaba porque nunca se expuso vía REST)
@Entity
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    private String motivo;

    @ManyToOne // lado propietario: esta tabla tiene la columna paciente_id
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
    public String getMotivo() { return motivo; }
    public Paciente getPaciente() { return paciente; }
}
```

```java
// RepositorioCitas.java — capa persistences (com.medisalud.persistences.repositories)
// (Módulo 3, reutilizada tal cual)
public interface RepositorioCitas extends JpaRepository<Cita, Long> {
}
```

```java
// ServicioCitas.java — capa services (com.medisalud.services)
// (nueva, mismo patrón que ServicioLibros)
@Service
public class ServicioCitas {

    private final RepositorioCitas repositorioCitas;

    public ServicioCitas(RepositorioCitas repositorioCitas) {
        this.repositorioCitas = repositorioCitas;
    }

    public Optional<Cita> buscarPorId(Long id) {
        return repositorioCitas.findById(id);
    }
}
```

```java
// ControladorCitas.java — capa controllers (com.medisalud.controllers)
@RestController
@RequestMapping("/citas")
public class ControladorCitas {

    private final ServicioCitas servicioCitas;

    public ControladorCitas(ServicioCitas servicioCitas) {
        this.servicioCitas = servicioCitas;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> buscarPorId(@PathVariable Long id) {
        return servicioCitas.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
```

## 📏 Criterios de evaluación de la solución

- Identifica que `Cita.getPaciente()` serializa un `Paciente`, cuyo
  `getCitas()` vuelve a serializar la misma `Cita` (y así sucesivamente):
  `Cita` → `paciente` → `citas` → `Cita` → ... sin fin.
- Propone la corrección: agregar `@JsonIgnore` sobre `Paciente.citas` (el
  lado que causa el ciclo), de modo que serializar una `Cita` incluya su
  `Paciente`, pero serializar un `Paciente` no incluya de vuelta sus
  `citas`.
- Explica que la corrección no afecta el resto del código: `Paciente`
  sigue teniendo su lista de `citas` en memoria y en la base de datos,
  solo deja de incluirse en la representación JSON.

## 🚧 Restricciones

La corrección debe mantener el dato del `Paciente` visible en la
respuesta JSON de `Cita` (no alcanza con quitar `Cita.getPaciente()`).

## 📊 Dificultad

Avanzado

## 🎓 Resultados de aprendizaje

RA-10
