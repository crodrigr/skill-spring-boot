# 🏆 Desafío 01 — Documentación automática de la API de citas

## 🧩 Problema

MediSalud necesita documentar automáticamente su API de `Cita` (Desafío
del Módulo 5) — un controlador distinto del usado en el Taller de este
módulo. La complicación adicional: `Cita` tiene una
relación con `Paciente`, y `Paciente.citas` ya tiene `@JsonIgnore` (para
evitar la recursión infinita, resuelta en el Módulo 5). Te piden verificar
que ese `@JsonIgnore` también afecta la documentación generada.

Agregá y configurá springdoc-openapi por tu cuenta (sin scaffold provisto,
igual que en los desafíos de módulos anteriores), y verificá el resultado
en Swagger UI.

## 💻 Código o contexto de partida

```java
// Paciente.java (Módulo 5, con @JsonIgnore ya aplicado — reutilizada tal cual)
@Entity
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigo;

    private String nombre;

    @OneToMany(mappedBy = "paciente")
    @JsonIgnore
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
// ControladorCitas.java (Módulo 5, reutilizada tal cual)
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

1. Agregá `springdoc-openapi-starter-webmvc-ui` al `pom.xml` de este
   proyecto.
2. Configurá `application.properties` para habilitar Swagger UI en
   `/doc/swagger-ui.html`, escaneando `com.medisalud`.
3. Verificá en Swagger UI que los cinco endpoints de `ControladorCitas`
   aparecen documentados.
4. Verificá, en el esquema de `Paciente`, que el campo `citas` **no**
   aparece (por `@JsonIgnore`), mientras que el esquema de `Cita` sí
   incluye el campo `paciente`.

## 📏 Criterios de evaluación de la solución

- La dependencia y la configuración son las mismas usadas en el Taller,
  con `packages-to-scan=com.medisalud` (el mismo paquete, distinto
  controlador).
- Ninguna clase Java (`Paciente`, `Cita`, `ControladorCitas`, etc.) se
  modifica: la documentación es exclusivamente configuración.
- Los cinco endpoints de `ControladorCitas` aparecen documentados en
  Swagger UI, con sus códigos de estado (`200`, `201`, `404`).
- El esquema de `Paciente` no incluye `citas`; el esquema de `Cita` sí
  incluye `paciente`.

## 🚧 Restricciones

Las entidades y endpoints deben ser distintos de los usados en el Taller:
no se acepta reutilizar el mismo recurso del Taller como recurso propio
de este desafío.

## 📊 Dificultad

Desafío

## 🎓 Resultados de aprendizaje

RA-5, RA-6, RA-7, RA-8
