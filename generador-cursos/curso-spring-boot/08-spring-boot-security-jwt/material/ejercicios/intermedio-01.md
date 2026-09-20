# 🟡 Intermedio 01 — Agregar Spring Security y probar con Basic Auth

## 🧩 Problema

El siguiente proyecto (Biblioteca Universitaria, hilo `Autor`/
`ControladorAutores` del Módulo 5, ya con manejo de excepciones del
Módulo 7) no tiene ninguna dependencia de seguridad: cualquiera puede
consultar, crear, modificar o eliminar autores sin autenticarse.

**Pregunta**: agregá `spring-boot-starter-security` al `pom.xml` de este
proyecto, ejecutalo, y probá el endpoint `GET /autores` en Insomnia
primero sin credenciales y luego con autenticación "Basic Auth" usando el
usuario `user` y la contraseña autogenerada que aparece en la consola.

## 💻 Código o contexto de partida

<details>
<summary>📄 Ver código completo de <code>Autor.java</code>, <code>RepositorioAutores.java</code>, <code>AutorNoEncontradoException.java</code>, <code>ServicioAutores.java</code>, <code>ControladorAutores.java</code> y <code>ManejadorGlobalDeExcepciones.java</code> (reutilizados de los Módulos 5 y 7)</summary>

## 💻 Archivo: `Autor.java` (`com.biblioteca.persistences.entities`)

```java
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

## 💻 Archivo: `RepositorioAutores.java` (`com.biblioteca.persistences.repositories`)

```java
public interface RepositorioAutores extends JpaRepository<Autor, Long> {
}
```

## 💻 Archivo: `AutorNoEncontradoException.java` (`com.biblioteca.exception`, paquete transversal)

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AutorNoEncontradoException extends RuntimeException {

    public AutorNoEncontradoException(Long id) {
        super("No existe un autor con id " + id);
    }
}
```

## 💻 Archivo: `ServicioAutores.java` (`com.biblioteca.services`)

```java
@Service
public class ServicioAutores {

    private final RepositorioAutores repositorioAutores;

    public ServicioAutores(RepositorioAutores repositorioAutores) {
        this.repositorioAutores = repositorioAutores;
    }

    public List<Autor> listarTodos() {
        return repositorioAutores.findAll();
    }

    public Autor buscarPorId(Long id) {
        return repositorioAutores.findById(id)
                .orElseThrow(() -> new AutorNoEncontradoException(id));
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

## 💻 Archivo: `ControladorAutores.java` (`com.biblioteca.controllers`)

```java
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
    public Autor buscarPorId(@PathVariable Long id) {
        return servicioAutores.buscarPorId(id);
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

## 💻 Archivo: `ManejadorGlobalDeExcepciones.java` (`com.biblioteca.exception`, paquete transversal)

```java
@ControllerAdvice
public class ManejadorGlobalDeExcepciones {

    @ExceptionHandler(LibroNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarLibroNoEncontrado(LibroNoEncontradoException ex) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cuerpo);
    }

    @ExceptionHandler(AutorNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarAutorNoEncontrado(AutorNoEncontradoException ex) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cuerpo);
    }
}
```

</details>

## 📏 Criterios de evaluación de la solución

- Agrega correctamente `spring-boot-starter-security` al `pom.xml`, sin
  modificar ninguna otra clase del proyecto.
- Documenta la solicitud sin credenciales (rechazada) y la solicitud con
  Basic Auth (usuario `user` + contraseña de consola), aceptada.
- No confunde la contraseña autogenerada con una contraseña fija: cada
  reinicio del proyecto genera una nueva.
- Reconoce que las capas MVC del proyecto (`controllers`, `services`,
  `persistences`) no cambian: Spring Security se agrega como una
  preocupación transversal, sin tocar ninguna de esas clases.

## 🚧 Restricciones

- No se agrega ninguna configuración de seguridad propia todavía (eso
  llega en el Ejemplo 08 y el Taller/Desafío); este ejercicio usa
  únicamente el comportamiento por defecto de `spring-boot-starter-security`.

## 📊 Dificultad

Intermedio.

## 🎓 Resultados de aprendizaje

`RA-5`, `RA-6`.
