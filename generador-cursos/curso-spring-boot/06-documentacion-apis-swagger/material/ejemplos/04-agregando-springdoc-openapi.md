# 💡 Ejemplo 04 — Agregando springdoc-openapi

## 🌍 Contexto

El Módulo 5 ya dejó `ControladorLibros` funcionando, con sus seis
endpoints REST. Este módulo no vuelve a explicar cómo se construyó ese
controlador: solo agrega la dependencia que va a permitir documentarlo
automáticamente.

**Qué busca demostrar este ejemplo**: agregar
`springdoc-openapi-starter-webmvc-ui` al `pom.xml` del proyecto de
`ControladorLibros`, sin tocar ninguna línea de código Java existente.

## 📚 Caso de estudio

Biblioteca Universitaria: `ControladorLibros`/`ServicioLibros`/`Libro`
del Módulo 5, reutilizados sin ningún cambio de código.

## 🌳 Árbol de archivos (como se vería en VS Code)

```text
📁 gestion-bd-jpa (mismo proyecto de los Módulos 3-5)
├── 📄 pom.xml                     (se agrega springdoc-openapi-starter-webmvc-ui)
└── 📁 src/main
    ├── 📁 java/com/biblioteca
    │   ├── 📄 Libro.java              (del Módulo 3, reutilizada)
    │   ├── 📄 RepositorioLibros.java  (del Módulo 3, reutilizada)
    │   ├── 📄 ServicioLibros.java     (del Módulo 5, reutilizada)
    │   └── 📄 ControladorLibros.java  (del Módulo 5, reutilizada)
    └── 📁 resources
        └── 📄 application.properties  (del Módulo 3, reutilizada)
```

<details>
<summary>📄 Ver código completo de <code>ControladorLibros.java</code> y sus clases reutilizadas (Módulos 3 y 5)</summary>

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

## 💻 Archivo: `pom.xml` (única dependencia nueva)

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

## 🧭 Explicación paso a paso

1. Ninguna clase Java del proyecto cambia en este ejemplo: `Libro`,
   `RepositorioLibros`, `ServicioLibros` y `ControladorLibros` son
   exactamente las del Módulo 5.
2. `springdoc-openapi-starter-webmvc-ui` es la única dependencia nueva:
   trae springdoc-openapi junto con Swagger UI empaquetado, lista para
   usar sin descargar nada aparte.
3. Con la dependencia agregada pero sin ninguna propiedad configurada
   todavía (Ejemplo 05), Spring Boot ya expone por defecto la
   documentación JSON cruda en `/v3/api-docs` y Swagger UI en
   `/swagger-ui/index.html` — el Ejemplo 05 personaliza esa ruta.
4. Agregar la dependencia no requiere anotar `ControladorLibros` ni
   `Libro` con nada adicional: springdoc detecta automáticamente
   cualquier `@RestController` ya existente en el classpath.

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué cambia en las clases Java (`Libro`,
`ControladorLibros`, etc.) al agregar `springdoc-openapi-starter-webmvc-ui`?

- **A.** Hay que anotarlas todas con `@Schema`.
- **B.** Nada; ninguna clase Java se modifica.
- **C.** Hay que convertirlas en interfaces.
- **D.** Hay que eliminar `@RestController` y reemplazarlo por una anotación de springdoc.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Springdoc detecta automáticamente los
`@RestController` existentes; no requiere modificar ninguna clase.

</details>

**2. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre este ejemplo.

- **A.** `springdoc-openapi-starter-webmvc-ui` incluye Swagger UI empaquetado.
- **B.** Es necesario reescribir `ServicioLibros` para que springdoc lo detecte.
- **C.** La dependencia se agrega en el mismo `pom.xml` que ya tenía `spring-boot-starter-web`.
- **D.** Sin ninguna propiedad configurada, springdoc ya expone rutas por defecto.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: ninguna clase existente
necesita reescribirse.

</details>

**3. [Abierta]** Un compañero agrega `springdoc-openapi-starter-webmvc-ui`
a su `pom.xml`, pero olvida que su proyecto todavía no tiene
`spring-boot-starter-web`.

**Pregunta**: ¿Qué pasaría en ese caso, y por qué?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: springdoc-openapi documenta específicamente
controladores de Spring MVC (`@RestController`), que dependen de
`spring-boot-starter-web` para existir. Sin esa dependencia, no habría
ningún controlador que documentar — el proyecto probablemente ni
compilaría, porque `ControladorLibros` usa anotaciones
(`@RestController`, `@GetMapping`, etc.) que vienen de
`spring-boot-starter-web`, no de springdoc. Las dos dependencias resuelven
problemas distintos y complementarios: una expone la API, la otra la
documenta.

</details>
