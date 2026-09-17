# 💡 Ejemplo 06 — Controlador REST: endpoints `GET`

## 🌍 Contexto

Con `ServicioLibros` ya creado (Ejemplo 05), llega el momento de exponerlo
como una API REST real. Este ejemplo se concentra en los dos endpoints de
lectura: listar todos los libros y buscar uno por id.

**Qué busca demostrar este ejemplo**: crear `ControladorLibros`
(`@RestController`) que delega en `ServicioLibros`, con `@GetMapping`
(listar todos) y `@GetMapping("/{id}")` + `@PathVariable` (buscar por
id), devolviendo `200` o `404` según corresponda.

## 📚 Caso de estudio

Biblioteca Universitaria: `Libro`/`RepositorioLibros`/`ServicioLibros`.

## 🌳 Árbol de archivos (como se vería en VS Code)

```text
📁 gestion-bd-jpa
└── 📁 src/main
    ├── 📁 java/com/biblioteca
    │   ├── 📄 Libro.java              (del Módulo 3, reutilizada)
    │   ├── 📄 RepositorioLibros.java  (del Módulo 3, reutilizada)
    │   ├── 📄 ServicioLibros.java     (del Ejemplo 05, reutilizada)
    │   └── 📄 ControladorLibros.java
    └── 📁 resources
        └── 📄 application.properties  (del Módulo 3, reutilizada)
```

<details>
<summary>📄 Ver código completo de <code>Libro.java</code>, <code>RepositorioLibros.java</code>, <code>ServicioLibros.java</code> y <code>application.properties</code> (reutilizados)</summary>

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

## 💻 Archivo: `ControladorLibros.java`

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
}
```

## 🧭 Explicación paso a paso

1. `@RestController` marca la clase como un controlador que devuelve
   directamente el cuerpo de la respuesta (JSON), sin renderizar ninguna
   vista.
2. `@RequestMapping("/libros")` define la ruta base: todos los endpoints
   de esta clase empiezan con `/libros`.
3. `listarTodos()` no necesita ningún parámetro especial: `@GetMapping`
   (sin ruta adicional) mapea `GET /libros`, y Spring convierte
   automáticamente la `List<Libro>` devuelta en un arreglo JSON, con
   `200 OK` implícito.
4. `buscarPorId(...)` usa `@PathVariable` para capturar el `{id}` de la
   URL (`GET /libros/3` → `id = 3`), y `ResponseEntity<Libro>` para poder
   elegir explícitamente el código de estado: `200` con el libro si
   existe, `404` si `buscarPorId` del servicio devuelve `Optional.empty()`.
5. El `Controller` nunca inyecta `RepositorioLibros` directamente: solo
   conoce `ServicioLibros`, respetando la arquitectura en capas del
   Ejemplo 03.

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué anotación captura un segmento de la URL (como el
`{id}` en `/libros/{id}`) como parámetro del método?

- **A.** `@RequestParam`.
- **B.** `@RequestBody`.
- **C.** `@PathVariable`.
- **D.** `@RequestMapping`.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. `@PathVariable` captura un segmento de la ruta
declarada en la anotación de mapeo (`@GetMapping("/{id}")`).

</details>

**2. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre `ControladorLibros`.

- **A.** `@RequestMapping("/libros")` en la clase define la ruta base para todos sus endpoints.
- **B.** `listarTodos()` siempre devuelve `404` si no hay ningún libro guardado.
- **C.** `buscarPorId(...)` devuelve `404` cuando el servicio no encuentra el libro.
- **D.** `ControladorLibros` inyecta `ServicioLibros`, no `RepositorioLibros`.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: una lista vacía sigue
siendo una respuesta exitosa (`200 OK` con un arreglo JSON vacío `[]`),
no un error.

</details>

**3. [Abierta]** Un compañero escribe `buscarPorId` así:

```java
@GetMapping("/{id}")
public Libro buscarPorId(@PathVariable Long id) {
    return servicioLibros.buscarPorId(id).orElse(null);
}
```

**Pregunta**: ¿Qué problema tiene este código frente al de este ejemplo,
y qué le recomendarías?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Cuando el libro no existe, este código devuelve
`null` con un código de estado `200 OK` (porque Spring, al no ver
`ResponseEntity`, asume éxito por defecto), lo cual es engañoso: el
cliente recibe un cuerpo vacío pensando que la solicitud tuvo éxito,
cuando en realidad el recurso no existe. Le recomendaría usar
`ResponseEntity<Libro>` como en el ejemplo, devolviendo `404` explícito
con `ResponseEntity.notFound().build()` cuando el `Optional` está vacío.

</details>
