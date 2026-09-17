# 💡 Ejemplo 07 — Controlador REST: endpoints `POST`, `PUT`, `DELETE`

## 🌍 Contexto

`ControladorLibros` ya sabe leer (Ejemplo 06). Este ejemplo completa el
CRUD REST agregando crear, actualizar y eliminar, además de un endpoint
de búsqueda por `@RequestParam`.

**Qué busca demostrar este ejemplo**: extender `ControladorLibros` con
`@PostMapping` (crear, `201`), `@PutMapping("/{id}")` (actualizar, `200`
o `404`), `@DeleteMapping("/{id}")` (eliminar, `200` o `404`), y un
endpoint adicional con `@RequestParam` para buscar por `isbn`.

## 📚 Caso de estudio

Biblioteca Universitaria: mismo proyecto de los Ejemplos 04-06.

<details>
<summary>📄 Ver código completo de <code>Libro.java</code>, <code>RepositorioLibros.java</code>, <code>ServicioLibros.java</code> y los endpoints `GET` de <code>ControladorLibros.java</code> (ya mostrados en el Ejemplo 06)</summary>

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

    public Optional<Libro> buscarPorId(Long id) {
        return repositorioLibros.findById(id);
    }

    public Optional<Libro> buscarPorIsbn(String isbn) {
        return repositorioLibros.findByIsbn(isbn);
    }

    public Libro crear(Libro libro) {
        return repositorioLibros.save(libro);
    }

    public Optional<Libro> actualizar(Long id, Libro datos) {
        return repositorioLibros.findById(id)
                .map(libro -> {
                    libro.setTitulo(datos.getTitulo());
                    return repositorioLibros.save(libro);
                });
    }

    public boolean eliminar(Long id) {
        if (!repositorioLibros.existsById(id)) {
            return false;
        }
        repositorioLibros.deleteById(id);
        return true;
    }
}
```

**Nota**: `buscarPorIsbn` es el único método nuevo agregado a
`ServicioLibros` en este ejemplo, para soportar el endpoint con
`@RequestParam` de más abajo.

## 💻 Archivo: `ControladorLibros.java` (endpoints `GET` del Ejemplo 06)

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
    public ResponseEntity<Libro> buscarPorId(@PathVariable Long id) {
        return servicioLibros.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Los endpoints POST/PUT/DELETE y el de @RequestParam se agregan más abajo
}
```

</details>

## 💻 Archivo: `ControladorLibros.java` (endpoints nuevos de este ejemplo)

```java
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
    public ResponseEntity<Libro> actualizar(@PathVariable Long id, @RequestBody Libro datos) {
        return servicioLibros.actualizar(id, datos)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boolean existia = servicioLibros.eliminar(id);
        return existia ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
```

(Estos cuatro métodos se agregan dentro de la misma clase
`ControladorLibros`, junto a los del Ejemplo 06.)

## 🧭 Explicación paso a paso

1. `@RequestParam` captura un parámetro de consulta (`?isbn=...`), a
   diferencia de `@PathVariable`, que captura un segmento de la ruta
   (`/{id}`); `GET /libros/buscar?isbn=978-0-13-468599-1` usa el primero.
2. `@PostMapping` + `@RequestBody` reciben el cuerpo JSON de la solicitud
   y lo convierten automáticamente en un objeto `Libro`; el método
   devuelve `201 Created` (`HttpStatus.CREATED`), no `200`, porque se creó
   un recurso nuevo.
3. `@PutMapping("/{id}")` combina `@PathVariable` (qué libro) con
   `@RequestBody` (los datos nuevos); devuelve `200` si existía, `404` si
   no.
4. `@DeleteMapping("/{id}")` no necesita `@RequestBody`: la eliminación
   solo depende del `id` en la URL. `ResponseEntity<Void>` indica que la
   respuesta no tiene cuerpo, solo un código de estado.
5. Los cinco endpoints CRUD ya están completos; el Ejemplo 08 los prueba
   todos —incluido este endpoint de `@RequestParam`— con Insomnia.

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué código de estado debería devolver un endpoint
`POST` que crea un recurso exitosamente?

- **A.** `200`.
- **B.** `201`.
- **C.** `204`.
- **D.** `404`.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. `201 Created` indica que la solicitud creó un
recurso nuevo; `200` se reserva para éxito sin creación (como `GET` o
`PUT`).

</details>

**2. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre `@RequestParam` y `@PathVariable`.

- **A.** `@PathVariable` captura un segmento de la ruta URL.
- **B.** `@RequestParam` captura un parámetro de consulta (query param).
- **C.** Ambas anotaciones cumplen exactamente la misma función y son intercambiables.
- **D.** Un mismo endpoint puede combinar `@PathVariable` y `@RequestBody`, como en `actualizar(...)`.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: cada una captura una
parte distinta de la solicitud HTTP (ruta vs. query string), no son
intercambiables.

</details>

**3. [Abierta]** Un compañero escribe el endpoint `POST` así:

```java
@PostMapping
public Libro crear(Libro libro) {
    return servicioLibros.crear(libro);
}
```

Al probarlo, el campo `titulo` siempre llega como `null` al servidor,
aunque el cliente lo envía correctamente en el cuerpo JSON.

**Pregunta**: ¿Qué le falta a este código, y por qué produce ese
resultado?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Le falta la anotación `@RequestBody` en el
parámetro `libro`. Sin ella, Spring no sabe que ese parámetro debe
completarse a partir del cuerpo JSON de la solicitud, y lo trata como si
fuera un parámetro de consulta o de formulario — que no está presente en
una solicitud `POST` con cuerpo JSON — por eso el objeto llega con sus
campos vacíos. La corrección es declarar el parámetro como
`@RequestBody Libro libro`.

</details>
