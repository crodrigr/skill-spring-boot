# 🏆 Desafío 01 — API REST de citas para MediSalud

## 🧩 Problema

MediSalud necesita exponer `Cita` como API REST — una entidad distinta de
la usada en el Taller (`Paciente`), y con una complicación adicional: `Cita`
tiene una relación bidireccional hacia `Paciente` (`Paciente.citas`,
mapeada con `mappedBy`), lo que provoca recursión infinita al serializarla
a JSON si no se maneja correctamente.

Te piden construir el CRUD REST completo (`ServicioCitas` +
`ControladorCitas`), resolviendo ese problema antes de que los endpoints
puedan devolver una respuesta válida, y probando el resultado en
Insomnia.

## 💻 Código o contexto de partida

No se provee ningún scaffold de `Service`/`Controller`: diseñalos vos
mismo, siguiendo el mismo patrón usado en `ServicioLibros`/
`ControladorLibros` (Ejemplos 05-07) y en el Taller (`ServicioPacientes`/
`ControladorPacientes`), pero aplicado a `Cita`.

```java
// Paciente.java (Módulo 3, reutilizada tal cual)
@Entity
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigo;

    private String nombre;

    @OneToMany(mappedBy = "paciente")
    private List<Cita> citas = new ArrayList<>();

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
}
```

```java
// Cita.java (Módulo 3, reutilizada; se agrega getId() y setFecha()/
// setMotivo(), que el Módulo 3 no necesitaba porque nunca se expuso ni
// se actualizó vía REST)
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

1. Resolvé primero el problema de recursión infinita: agregá
   `@JsonIgnore` en el lado apropiado de la relación `Paciente`↔`Cita`
   (pista: el mismo lado que en el Ejercicio Avanzado 02).
2. Creá `ServicioCitas` (`@Service`) con los cinco métodos de negocio
   (`listarTodos`, `buscarPorId`, `crear`, `actualizar`, `eliminar`).
3. Creá `ControladorCitas` (`@RestController`, `@RequestMapping("/citas")`)
   con los cinco endpoints CRUD, devolviendo el código de estado correcto
   en cada caso.
4. Probá los cinco endpoints en Insomnia, incluyendo al menos un caso de
   error (`404`), y verificá que la respuesta JSON de una `Cita` incluye
   los datos de su `Paciente` sin entrar en recursión infinita.

## 📏 Criterios de evaluación de la solución

- `Paciente.citas` (o el lado equivalente) tiene `@JsonIgnore`, y
  `GET /citas/{id}` responde con el `Paciente` incluido, sin error de
  serialización.
- `ServicioCitas` y `ControladorCitas` siguen el mismo patrón de capas
  que `ServicioLibros`/`ControladorLibros` y que el Taller.
- Los cinco endpoints devuelven el código de estado correcto, incluido
  `404` cuando el recurso no existe.
- Las pruebas en Insomnia están documentadas para los cinco endpoints,
  con al menos un caso de error.

## 🚧 Restricciones

Las entidades y endpoints deben ser distintos de los usados en el Taller:
no se acepta reutilizar el mismo recurso del Taller como recurso propio de
este desafío.

## 📊 Dificultad

Desafío

## 🎓 Resultados de aprendizaje

RA-7, RA-8, RA-9, RA-10, RA-11
