# 💡 Ejemplo 03 — `@ResponseStatus`: recurso no encontrado

## 🌍 Contexto

`ControladorLibros.buscarPorId(...)` (Módulo 5) construye manualmente
`ResponseEntity.notFound().build()` cuando `ServicioLibros` no encuentra
el libro. Este ejemplo reemplaza ese patrón manual por una excepción
personalizada.

**Qué busca demostrar este ejemplo**: crear `LibroNoEncontradoException`
anotada con `@ResponseStatus(HttpStatus.NOT_FOUND)`, y modificar
`ServicioLibros`/`ControladorLibros` para que la excepción —no el
`Controller`— sea la responsable de producir el `404`.

## 📚 Caso de estudio

Biblioteca Universitaria: `Libro`/`RepositorioLibros`/`ServicioLibros`/
`ControladorLibros` del Módulo 5.

## 🌳 Árbol de archivos (como se vería en VS Code)

```text
📁 gestion-bd-jpa
└── 📁 src/main
    ├── 📁 java/com/biblioteca
    │   ├── 📄 Libro.java                      (del Módulo 3, reutilizada)
    │   ├── 📄 RepositorioLibros.java          (del Módulo 3, reutilizada)
    │   ├── 📄 LibroNoEncontradoException.java
    │   ├── 📄 ServicioLibros.java             (modificada en este ejemplo)
    │   └── 📄 ControladorLibros.java          (modificada en este ejemplo)
    └── 📁 resources
        └── 📄 application.properties          (del Módulo 3, reutilizada)
```

<details>
<summary>📄 Ver código completo de <code>Libro.java</code>, <code>RepositorioLibros.java</code> y <code>application.properties</code> (reutilizados del Módulo 3)</summary>

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

## 💻 Archivo: `application.properties`

```properties
spring.datasource.url=jdbc:h2:mem:biblioteca;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

</details>

## 💻 Archivo: `LibroNoEncontradoException.java`

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LibroNoEncontradoException extends RuntimeException {

    public LibroNoEncontradoException(Long id) {
        super("No existe un libro con id " + id);
    }
}
```

## 💻 Archivo: `ServicioLibros.java` (modificada: `buscarPorId` ya no devuelve `Optional`)

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

## 💻 Archivo: `ControladorLibros.java` (modificada: `buscarPorId` ya no construye `ResponseEntity.notFound()`)

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

## 🧭 Explicación paso a paso — qué cambió respecto al Módulo 5

1. **Antes** (Módulo 5): `ServicioLibros.buscarPorId` devolvía
   `Optional<Libro>`; `ControladorLibros.buscarPorId` decidía, con
   `.map(...)`/`.orElseGet(...)`, si respondía `200` o construía
   `ResponseEntity.notFound()` manualmente.
   **Ahora**: `ServicioLibros.buscarPorId` devuelve `Libro` directamente,
   y lanza `LibroNoEncontradoException` si no existe
   (`orElseThrow(...)`); `ControladorLibros.buscarPorId` ya ni siquiera
   necesita `ResponseEntity`: devuelve `Libro` directo, y Spring Boot
   construye el `404` automáticamente al propagarse la excepción,
   gracias a `@ResponseStatus(HttpStatus.NOT_FOUND)` en la clase de la
   excepción.
2. `actualizar` y `eliminar` también cambiaron: antes manejaban su propio
   caso de "no encontrado" con `Optional`/`existsById`; ahora reutilizan
   `buscarPorId(id)` (que ya lanza si no existe), evitando repetir la
   misma verificación tres veces.
3. `@ResponseStatus` se declara sobre la **clase** de la excepción, no
   sobre el método del controlador — por eso alcanza con lanzarla desde
   cualquier lugar (incluido el `Service`) para que Spring la traduzca al
   código correcto.
4. El cuerpo de la respuesta ante este error sigue siendo el que Spring
   Boot genera por defecto (con `timestamp`, `status`, `error`, `path`),
   no un cuerpo personalizado — eso se resuelve en el Ejemplo 05 con
   `@ExceptionHandler`.

## ✅ Resultado esperado

```text
Método: GET
URL: http://localhost:8080/libros/999
Respuesta: 404 Not Found
{
  "timestamp": "...",
  "status": 404,
  "error": "Not Found",
  "path": "/libros/999"
}
```

## ❓ Preguntas de repaso

**1. [Selección]** ¿Dónde se declara la anotación `@ResponseStatus` para
asociar una excepción a un código HTTP?

- **A.** Sobre el método del controlador.
- **B.** Sobre la clase de la excepción.
- **C.** Sobre la clase del servicio.
- **D.** En `application.properties`.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. `@ResponseStatus` se declara sobre la clase de
la excepción; Spring la aplica automáticamente cuando esa excepción se
propaga sin ser capturada.

</details>

**2. [Selección múltiple]** Sobre el cambio de este ejemplo respecto al
Módulo 5, seleccioná **todas** las afirmaciones correctas.

- **A.** `ServicioLibros.buscarPorId` ya no devuelve `Optional<Libro>`.
- **B.** `ControladorLibros.buscarPorId` sigue construyendo `ResponseEntity.notFound()` manualmente.
- **C.** `LibroNoEncontradoException` extiende `RuntimeException`.
- **D.** El `404` se produce automáticamente cuando la excepción se propaga sin capturar.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: justamente ese es el
código manual que este ejemplo elimina.

</details>

**3. [Abierta]** Un compañero te pregunta: "si `@ResponseStatus` ya
resuelve el código de estado automáticamente, ¿para qué existen
`@ExceptionHandler` y `@ControllerAdvice`?".

**Pregunta**: ¿Qué le responderías, pensando en el cuerpo de la
respuesta de este ejemplo?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: `@ResponseStatus` resuelve el **código** de estado,
pero el **cuerpo** de la respuesta sigue siendo el que Spring Boot genera
por defecto (`timestamp`, `status`, `error`, `path`) — no hay forma de
personalizarlo solo con esta anotación. `@ExceptionHandler` y
`@ControllerAdvice` (próximos ejemplos) permiten controlar tanto el
código como el cuerpo exacto de la respuesta, útil cuando se necesita un
formato de error propio y consistente en toda la API.

</details>
