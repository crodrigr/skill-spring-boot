# 🟡 Intermedio 01 — Crear una excepción personalizada con `@ResponseStatus`

## 🧩 Problema

Biblioteca Universitaria tiene `ControladorAutores`/`ServicioAutores`
(Módulo 5) funcionando, pero `buscarPorId` todavía construye
`ResponseEntity.notFound()` a mano. Te piden reemplazar ese patrón por
una excepción personalizada, igual que se hizo con `Libro` en el
Ejemplo 03.

## 💻 Código o contexto de partida

```java
// Autor.java (Módulo 3, reutilizada)
@Entity
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @ManyToMany(mappedBy = "autores")
    private Set<Libro> libros = new HashSet<>();

    protected Autor() {
    }

    public Autor(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Set<Libro> getLibros() { return libros; }
}
```

```java
public interface RepositorioAutores extends JpaRepository<Autor, Long> {
}
```

```java
// ServicioAutores.java (Módulo 5, reutilizada)
@Service
public class ServicioAutores {

    private final RepositorioAutores repositorioAutores;

    public ServicioAutores(RepositorioAutores repositorioAutores) {
        this.repositorioAutores = repositorioAutores;
    }

    public List<Autor> listarTodos() {
        return repositorioAutores.findAll();
    }

    public Optional<Autor> buscarPorId(Long id) {
        return repositorioAutores.findById(id);
    }

    public Autor crear(Autor autor) {
        return repositorioAutores.save(autor);
    }

    public Optional<Autor> actualizar(Long id, Autor datos) {
        return repositorioAutores.findById(id)
                .map(autor -> {
                    autor.setNombre(datos.getNombre());
                    return repositorioAutores.save(autor);
                });
    }

    public boolean eliminar(Long id) {
        if (!repositorioAutores.existsById(id)) {
            return false;
        }
        repositorioAutores.deleteById(id);
        return true;
    }
}
```

```java
// ControladorAutores.java (Módulo 5, reutilizada)
@RestController
@RequestMapping("/autores")
public class ControladorAutores {

    private final ServicioAutores servicioAutores;

    public ControladorAutores(ServicioAutores servicioAutores) {
        this.servicioAutores = servicioAutores;
    }

    @GetMapping
    public List<Autor> listarTodos() {
        return servicioAutores.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Autor> buscarPorId(@PathVariable Long id) {
        return servicioAutores.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Autor> crear(@RequestBody Autor autor) {
        Autor creado = servicioAutores.crear(autor);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Autor> actualizar(@PathVariable Long id, @RequestBody Autor datos) {
        return servicioAutores.actualizar(id, datos)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boolean existia = servicioAutores.eliminar(id);
        return existia ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
```

1. Creá `AutorNoEncontradoException` (`@ResponseStatus(HttpStatus.NOT_FOUND)`),
   siguiendo el mismo patrón de `LibroNoEncontradoException` (Ejemplo
   03).
2. Modificá `ServicioAutores.buscarPorId` para que devuelva `Autor`
   directamente y lance la excepción en vez de `Optional`.
3. Modificá `ControladorAutores.buscarPorId` para que ya no construya
   `ResponseEntity.notFound()`.

## 📏 Criterios de evaluación de la solución

- `AutorNoEncontradoException` extiende `RuntimeException` y tiene
  `@ResponseStatus(HttpStatus.NOT_FOUND)`.
- `ServicioAutores.buscarPorId` devuelve `Autor` (no `Optional<Autor>`) y
  usa `orElseThrow(...)`.
- `ControladorAutores.buscarPorId` devuelve `Autor` directamente, sin
  ningún `ResponseEntity` construido a mano.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-4
