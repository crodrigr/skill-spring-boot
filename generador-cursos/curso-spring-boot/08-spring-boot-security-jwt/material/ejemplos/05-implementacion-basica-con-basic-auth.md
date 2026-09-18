# 💡 Ejemplo 05 — Primeros pasos: agregar Spring Security y probar con Basic Auth

## 🌍 Contexto

Los Ejemplos 01-04 explicaron la arquitectura de Spring Security en
abstracto. Este ejemplo da el primer paso práctico: agregar
`spring-boot-starter-security` al proyecto de `ControladorLibros`
(Biblioteca Universitaria, ya con manejo de excepciones del Módulo 7) y
observar el efecto inmediato sobre un endpoint que hasta ahora era
público.

**Qué busca demostrar este ejemplo**: que agregar una única dependencia
ya activa toda la arquitectura de filtros vista en el Ejemplo 02, sin
escribir ninguna línea de configuración — y que ese primer mecanismo de
protección (HTTP Basic) es un punto de partida, no el destino final del
módulo (que llega con JWT en el Ejemplo 08).

## 📚 Caso de estudio

Biblioteca Universitaria: mismo proyecto de `ControladorLibros` del
Módulo 7, ya con `LibroNoEncontradoException`, `LibroDuplicadoException`
y `ManejadorGlobalDeExcepciones` aplicados. Este ejemplo no modifica
ninguna de esas clases: solo agrega una dependencia nueva al proyecto.

<details>
<summary>📄 Ver código completo de <code>Libro.java</code>, <code>RepositorioLibros.java</code>, <code>LibroNoEncontradoException.java</code>, <code>LibroDuplicadoException.java</code>, <code>ServicioLibros.java</code>, <code>ControladorLibros.java</code> y <code>ManejadorGlobalDeExcepciones.java</code> (reutilizados del Módulo 7, sin cambios)</summary>

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

</details>

## 🌳 Árbol de archivos (como se vería en VS Code)

```text
📁 src/main/java/
└── 📁 (paquete raíz del proyecto Biblioteca)
    ├── 📄 Libro.java (Módulo 3, sin cambios)
    ├── 📄 RepositorioLibros.java (Módulo 3, sin cambios)
    ├── 📄 LibroNoEncontradoException.java (Módulo 7, sin cambios)
    ├── 📄 LibroDuplicadoException.java (Módulo 7, sin cambios)
    ├── 📄 ServicioLibros.java (Módulo 5/7, sin cambios)
    ├── 📄 ControladorLibros.java (Módulo 5, sin cambios)
    └── 📄 ManejadorGlobalDeExcepciones.java (Módulo 7, sin cambios)
📁 src/main/resources/
└── 📄 application.properties (sin cambios)
📄 pom.xml (modificado: nueva dependencia)
```

## 💻 Archivo: `pom.xml` (fragmento modificado)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## 🧭 Explicación paso a paso

1. **Paso 1**: se agrega la dependencia `spring-boot-starter-security` al
   `pom.xml`. No se toca ninguna otra clase del proyecto.
2. **Paso 2**: se ejecuta el proyecto (`mvn spring-boot:run` o desde el
   IDE). En la consola aparece una línea nueva con una contraseña
   autogenerada, similar a:

   ```text
   Using generated security password: 32ef4fc3-a4ca-447f-9ed6-0743b30babf7
   ```

   Esta contraseña cambia en cada arranque del proyecto (mientras no se
   configure una cuenta propia, como se hará en el Taller/Desafío).
3. **Paso 3**: si se intenta acceder a `GET /libros` como antes (sin
   ninguna credencial), la solicitud ya no devuelve la lista de libros:
   Spring Security la rechaza automáticamente. Solo con la dependencia
   agregada, **todos** los endpoints quedan protegidos por defecto —
   ninguna clase de `ControladorLibros` fue modificada para lograrlo.
4. **Paso 4**: en Insomnia (o Postman), se busca la pestaña de
   autenticación de la solicitud y se selecciona la opción **"Basic
   Auth"**.
5. **Paso 5**: se completa usuario `user` y, como contraseña, la
   generada en la consola (paso 2).
6. **Paso 6**: al reenviar la solicitud con esas credenciales, el
   endpoint responde igual que antes de agregar Spring Security.

## ✅ Resultado esperado

```text
Método: GET
URL: http://localhost:8080/libros
(sin autenticación)
Respuesta: 401 Unauthorized
```

```text
Método: GET
URL: http://localhost:8080/libros
Autenticación: Basic Auth — usuario: user, contraseña: 32ef4fc3-a4ca-447f-9ed6-0743b30babf7
Respuesta: 200 OK
[
  {"id": 1, "isbn": "978-0-13-468599-1", "titulo": "Effective Java"}
]
```

## ❓ Preguntas de repaso

**1. [Selección]** Después de agregar `spring-boot-starter-security` sin
ninguna configuración adicional, ¿qué ocurre con un endpoint que antes
era público?

- **A.** Sigue siendo público, hasta que se configure explícitamente.
- **B.** Queda protegido automáticamente, exigiendo autenticación.
- **C.** Deja de funcionar por completo.
- **D.** Se elimina del proyecto.

<details><summary>🔑 Ver respuesta</summary>

**B.** Spring Boot aplica Spring Security con una configuración por
defecto que protege todos los endpoints, generando además una
contraseña temporal para el usuario `user`.

</details>

**2. [Selección múltiple]** ¿Cuáles de las siguientes afirmaciones sobre
este ejemplo son correctas?

- **A.** Ninguna clase de `ControladorLibros` fue modificada para lograr la protección.
- **B.** La contraseña autogenerada es siempre la misma en cada arranque.
- **C.** El usuario por defecto se llama `user`.
- **D.** La autenticación se probó con "Basic Auth" en el cliente HTTP.

<details><summary>🔑 Ver respuesta</summary>

**A, C y D.** B es falsa: la contraseña autogenerada cambia en cada
arranque del proyecto.

</details>

**3. [Abierta]** ¿Por qué este mecanismo (usuario `user` + contraseña
autogenerada en consola) no es apropiado para un proyecto en producción,
aunque sí sea útil para verificar rápidamente que Spring Security está
activo?

<details><summary>🔑 Ver respuesta modelo</summary>

Porque la contraseña cambia en cada arranque y no hay forma de que un
usuario real la conozca de antemano, ni de tener más de un usuario con
roles distintos. Es un mecanismo pensado solo para confirmar
rápidamente, durante el desarrollo, que la protección está activa — en
el Taller y el Desafío se reemplaza por cuentas de credenciales propias
(`Credencial`) respaldadas por base de datos.

</details>
