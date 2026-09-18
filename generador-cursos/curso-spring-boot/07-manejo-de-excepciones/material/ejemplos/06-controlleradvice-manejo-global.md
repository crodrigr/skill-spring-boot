# 💡 Ejemplo 06 — `@ControllerAdvice`: manejo global

## 🌍 Contexto

El Ejemplo 05 dejó el manejo de `LibroNoEncontradoException` dentro de
`ControladorLibros`. Si `LibroDuplicadoException` necesitara el mismo
tratamiento, o si el proyecto agregara un controlador nuevo, habría que
repetir el mismo código en cada lugar. Este ejemplo lo centraliza.

**Qué busca demostrar este ejemplo**: crear
`ManejadorGlobalDeExcepciones` (`@ControllerAdvice`) que maneja
`LibroNoEncontradoException` y `LibroDuplicadoException` para toda la
aplicación, dejando `ControladorLibros` sin ningún método de manejo de
errores.

## 📚 Caso de estudio

Biblioteca Universitaria: mismo proyecto de los Ejemplos 03-05.

<details>
<summary>📄 Ver código completo de <code>Libro.java</code>, <code>RepositorioLibros.java</code>, <code>LibroNoEncontradoException.java</code>, <code>LibroDuplicadoException.java</code> y <code>ServicioLibros.java</code> (reutilizados de los Ejemplos 03-05)</summary>

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

## 💻 Archivo: `ControladorLibros.java` (sin ningún `@ExceptionHandler`)

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
}
```

## 💻 Archivo: `ManejadorGlobalDeExcepciones.java`

```java
@ControllerAdvice
public class ManejadorGlobalDeExcepciones {

    @ExceptionHandler(LibroNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarLibroNoEncontrado(LibroNoEncontradoException ex) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cuerpo);
    }

    @ExceptionHandler(LibroDuplicadoException.class)
    public ResponseEntity<Map<String, String>> manejarLibroDuplicado(LibroDuplicadoException ex) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(cuerpo);
    }
}
```

## 🧭 Explicación paso a paso — qué cambió respecto al Ejemplo 05

1. **Antes** (Ejemplo 05): `manejarLibroNoEncontrado` vivía **dentro** de
   `ControladorLibros`, como un método `@ExceptionHandler` más.
   **Ahora**: se movió, sin cambiar una línea de su lógica, a una clase
   nueva `ManejadorGlobalDeExcepciones`, anotada con `@ControllerAdvice`
   en vez de vivir dentro de un controlador — `ControladorLibros` queda
   completamente libre de código de manejo de errores.
2. `@ControllerAdvice` hace que Spring registre esta clase como un
   manejador que aplica a **todos** los controladores de la aplicación,
   no solo a `ControladorLibros`.
3. Se agregó, de paso, el manejo de `LibroDuplicadoException` (que en el
   Ejemplo 05 todavía usaba el cuerpo por defecto) — ambas excepciones
   quedan con el mismo formato de cuerpo, consistente en toda la API.
4. Si este proyecto agregara mañana un controlador nuevo (por ejemplo,
   uno para `Editorial`) que también pudiera lanzar
   `LibroNoEncontradoException` o `LibroDuplicadoException`, quedaría
   cubierto automáticamente por este mismo `ManejadorGlobalDeExcepciones`
   — sin escribir ningún código de manejo adicional en ese controlador
   nuevo. Esa es la diferencia clave frente a `@ExceptionHandler` local
   (Ejemplo 05), que habría que repetir en cada controlador nuevo.

## ✅ Resultado esperado

```text
Método: GET
URL: http://localhost:8080/libros/999
Respuesta: 404 Not Found
{
  "error": "No existe un libro con id 999"
}
```

```text
Método: POST
URL: http://localhost:8080/libros
Cuerpo: {"isbn": "978-0-13-468599-1", "titulo": "Effective Java (copia)"}
Respuesta: 409 Conflict
{
  "error": "Ya existe un libro con isbn 978-0-13-468599-1"
}
```

(Mismo resultado que antes para `LibroNoEncontradoException`; nuevo cuerpo
personalizado para `LibroDuplicadoException`, que en el Ejemplo 05 no lo
tenía.)

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué anotación permite que una clase maneje
excepciones para todos los controladores de una aplicación?

- **A.** `@ExceptionHandler`.
- **B.** `@RestController`.
- **C.** `@ControllerAdvice`.
- **D.** `@ResponseStatus`.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. `@ControllerAdvice` marca una clase como
manejador global, aplicable a todos los controladores.

</details>

**2. [Selección múltiple]** Sobre este ejemplo, seleccioná **todas** las
afirmaciones correctas.

- **A.** `ControladorLibros` ya no tiene ningún método `@ExceptionHandler` propio.
- **B.** `ManejadorGlobalDeExcepciones` solo aplica a `ControladorLibros`.
- **C.** Un controlador nuevo que lance `LibroDuplicadoException` quedaría cubierto automáticamente.
- **D.** El código de los métodos dentro de `ManejadorGlobalDeExcepciones` es el mismo que tenían los `@ExceptionHandler` locales.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: `@ControllerAdvice`
aplica a todos los controladores de la aplicación, no solo a uno.

</details>

**3. [Abierta]** Un compañero pregunta: "si `@ControllerAdvice` es
claramente superior a `@ExceptionHandler` local, ¿por qué este módulo
enseñó primero el local, en vez de ir directo al global?".

**Pregunta**: ¿Qué le responderías?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: `@ControllerAdvice` no reemplaza el concepto de
`@ExceptionHandler`: lo **traslada** de un controlador a una clase
centralizada — el método sigue siendo un `@ExceptionHandler`, solo cambia
dónde vive. Entender primero cómo funciona `@ExceptionHandler` dentro de
un controlador (Ejemplo 05) deja claro qué hace cada mecanismo por
separado, antes de ver por qué centralizarlo con `@ControllerAdvice`
resuelve el problema de duplicación entre varios controladores — el mismo
criterio de progresión pedagógica (de lo simple a lo más completo) usado
en todo el curso.

</details>
