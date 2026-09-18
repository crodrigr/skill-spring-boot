# 💡 Ejemplo 05 — `@ExceptionHandler`: cuerpo personalizado

## 🌍 Contexto

El Ejemplo 03 dejó `LibroNoEncontradoException` respondiendo `404`, pero
con el cuerpo de error por defecto de Spring Boot (`timestamp`, `status`,
`error`, `path`) — no el formato `{"error": "..."}"` ya usado en el
Módulo 5. Este ejemplo lo personaliza.

**Qué busca demostrar este ejemplo**: agregar un método
`@ExceptionHandler` dentro de `ControladorLibros` para controlar el
cuerpo exacto de la respuesta ante `LibroNoEncontradoException`, y
explicar que este manejo es local a ese controlador.

## 📚 Caso de estudio

Biblioteca Universitaria: mismo proyecto de los Ejemplos 03-04.

<details>
<summary>📄 Ver código completo de <code>Libro.java</code>, <code>RepositorioLibros.java</code>, <code>LibroNoEncontradoException.java</code>, <code>LibroDuplicadoException.java</code> y <code>ServicioLibros.java</code> (reutilizados de los Ejemplos 03-04)</summary>

## 💻 Archivo: `Libro.java`

```java
@Entity
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String isbn;

    private String titulo;

    protected Libro() {
    }

    public Libro(String isbn, String titulo) {
        this.isbn = isbn;
        this.titulo = titulo;
    }

    public Long getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
}
```

## 💻 Archivo: `RepositorioLibros.java`

```java
public interface RepositorioLibros extends JpaRepository<Libro, Long> {
    Optional<Libro> findByIsbn(String isbn);
}
```

## 💻 Archivo: `LibroNoEncontradoException.java`

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LibroNoEncontradoException extends RuntimeException {

    public LibroNoEncontradoException(Long id) {
        super("No existe un libro con id " + id);
    }
}
```

## 💻 Archivo: `LibroDuplicadoException.java`

```java
@ResponseStatus(HttpStatus.CONFLICT)
public class LibroDuplicadoException extends RuntimeException {

    public LibroDuplicadoException(String isbn) {
        super("Ya existe un libro con isbn " + isbn);
    }
}
```

## 💻 Archivo: `ServicioLibros.java`

```java
@Service
public class ServicioLibros {

    private final RepositorioLibros repositorioLibros;

    public ServicioLibros(RepositorioLibros repositorioLibros) {
        this.repositorioLibros = repositorioLibros;
    }

    public List<Libro> listarTodos() {
        return repositorioLibros.findAll();
    }

    public Libro buscarPorId(Long id) {
        return repositorioLibros.findById(id)
                .orElseThrow(() -> new LibroNoEncontradoException(id));
    }

    public Optional<Libro> buscarPorIsbn(String isbn) {
        return repositorioLibros.findByIsbn(isbn);
    }

    public Libro crear(Libro libro) {
        repositorioLibros.findByIsbn(libro.getIsbn())
                .ifPresent(existente -> {
                    throw new LibroDuplicadoException(libro.getIsbn());
                });
        return repositorioLibros.save(libro);
    }

    public Libro actualizar(Long id, Libro datos) {
        Libro libro = buscarPorId(id);
        libro.setTitulo(datos.getTitulo());
        return repositorioLibros.save(libro);
    }

    public void eliminar(Long id) {
        Libro libro = buscarPorId(id);
        repositorioLibros.delete(libro);
    }
}
```

</details>

## 💻 Archivo: `ControladorLibros.java` (con `@ExceptionHandler` agregado)

```java
@RestController
@RequestMapping("/libros")
public class ControladorLibros {

    private final ServicioLibros servicioLibros;

    public ControladorLibros(ServicioLibros servicioLibros) {
        this.servicioLibros = servicioLibros;
    }

    @GetMapping
    public List<Libro> listarTodos() {
        return servicioLibros.listarTodos();
    }

    @GetMapping("/{id}")
    public Libro buscarPorId(@PathVariable Long id) {
        return servicioLibros.buscarPorId(id);
    }

    @GetMapping("/buscar")
    public ResponseEntity<Libro> buscarPorIsbn(@RequestParam String isbn) {
        return servicioLibros.buscarPorIsbn(isbn)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Libro> crear(@RequestBody Libro libro) {
        Libro creado = servicioLibros.crear(libro);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public Libro actualizar(@PathVariable Long id, @RequestBody Libro datos) {
        return servicioLibros.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicioLibros.eliminar(id);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(LibroNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarLibroNoEncontrado(LibroNoEncontradoException ex) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cuerpo);
    }
}
```

## 🧭 Explicación paso a paso

1. `@ExceptionHandler(LibroNoEncontradoException.class)` marca un método
   de `ControladorLibros` como el responsable de manejar esa excepción
   **para este controlador únicamente** — si existiera otro controlador
   (por ejemplo, `ControladorAutores`) que también pudiera lanzar una
   excepción de ese mismo tipo, este método no lo cubriría.
2. El método recibe la excepción como parámetro (`ex`), y construye
   explícitamente el `ResponseEntity`: tanto el código de estado (`404`,
   repetido acá aunque `LibroNoEncontradoException` ya tenga
   `@ResponseStatus`) como el cuerpo (`{"error": "<mensaje>"}"`, ahora sí
   con el formato del Módulo 5).
3. **Sobre la precedencia**: `LibroNoEncontradoException` sigue teniendo
   `@ResponseStatus(HttpStatus.NOT_FOUND)` (Ejemplo 03), pero como
   `ControladorLibros` ahora define un `@ExceptionHandler` específico
   para esa excepción, es **este método** el que se ejecuta al lanzarla
   — no el mecanismo de `@ResponseStatus`. Cuando ambos mecanismos
   podrían aplicar a la misma excepción, `@ExceptionHandler` (más
   específico) toma precedencia sobre la anotación de clase.
4. `LibroDuplicadoException` **no** tiene un `@ExceptionHandler` en este
   controlador todavía: sigue devolviendo el cuerpo por defecto de
   `@ResponseStatus`, ya que este ejemplo se concentra en un solo caso
   para mostrar el contraste con claridad.

## ✅ Resultado esperado

```text
Método: GET
URL: http://localhost:8080/libros/999
Respuesta: 404 Not Found
{
  "error": "No existe un libro con id 999"
}
```

(Contrastar con el cuerpo por defecto del Ejemplo 03, antes de agregar
este `@ExceptionHandler`.)

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué le agrega `@ExceptionHandler` frente a usar
solamente `@ResponseStatus`?

- **A.** Nada; ambos producen exactamente el mismo resultado siempre.
- **B.** La posibilidad de controlar el cuerpo exacto de la respuesta, no solo el código de estado.
- **C.** La posibilidad de aplicar el manejo a todos los controladores de la aplicación.
- **D.** Genera automáticamente pruebas unitarias para la excepción.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. `@ExceptionHandler` permite construir
explícitamente el cuerpo de la respuesta, algo que `@ResponseStatus` solo
no permite.

</details>

**2. [Selección múltiple]** Sobre el alcance de `@ExceptionHandler` en
este ejemplo, seleccioná **todas** las afirmaciones correctas.

- **A.** El método `manejarLibroNoEncontrado` solo aplica a excepciones lanzadas dentro de `ControladorLibros`.
- **B.** Si `ControladorAutores` lanzara `LibroNoEncontradoException`, este método también lo cubriría.
- **C.** `LibroNoEncontradoException` sigue teniendo `@ResponseStatus`, aunque ahora también tenga un `@ExceptionHandler`.
- **D.** El `@ExceptionHandler` toma precedencia sobre el `@ResponseStatus` de la clase cuando ambos aplican.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: `@ExceptionHandler` es
local al controlador donde se declara, no se extiende a otros
controladores (y, en este caso, `LibroNoEncontradoException` tampoco la
lanzaría `ControladorAutores`, que tiene su propia excepción).

</details>

**3. [Abierta]** Un compañero agrega un `@ExceptionHandler` para
`LibroNoEncontradoException` en `ControladorAutores`, copiando y pegando
el mismo código que en `ControladorLibros`, "para que también tenga un
cuerpo bonito".

**Pregunta**: ¿Tiene sentido ese cambio? ¿Qué le recomendarías en su
lugar?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: No tiene sentido: `ControladorAutores` nunca lanza
`LibroNoEncontradoException` (lanza `AutorNoEncontradoException`, de la
Intermedio 01), así que ese `@ExceptionHandler` nunca se ejecutaría ahí.
Si lo que quiere es que `ControladorAutores` también devuelva un cuerpo
personalizado ante su propia excepción, necesitaría su propio
`@ExceptionHandler(AutorNoEncontradoException.class)` — pero copiar y
pegar el mismo manejador en cada controlador es exactamente el problema
que el Ejemplo 06 (`@ControllerAdvice`) resuelve, centralizando el manejo
en un solo lugar en vez de duplicarlo.

</details>
