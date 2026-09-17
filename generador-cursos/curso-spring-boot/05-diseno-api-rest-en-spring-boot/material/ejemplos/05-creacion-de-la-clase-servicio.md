# 💡 Ejemplo 05 — Creación de la clase servicio

## 🌍 Contexto

Con `spring-boot-starter-web` ya agregado (Ejemplo 04), el siguiente paso
no es escribir el controlador directamente: primero se crea la capa
`Service`, que va a encapsular las operaciones de negocio sobre `Libro`
antes de exponerlas vía HTTP.

**Qué busca demostrar este ejemplo**: crear `ServicioLibros` (`@Service`),
inyectando `RepositorioLibros` y exponiendo los cinco métodos de negocio
que el controlador del Ejemplo 06 va a consumir.

## 📚 Caso de estudio

Biblioteca Universitaria: `Libro`/`RepositorioLibros` del Módulo 3.

## 🌳 Árbol de archivos (como se vería en VS Code)

```text
📁 gestion-bd-jpa
└── 📁 src/main
    ├── 📁 java/com/biblioteca
    │   ├── 📄 Libro.java              (del Módulo 3, reutilizada)
    │   ├── 📄 RepositorioLibros.java  (del Módulo 3, reutilizada)
    │   └── 📄 ServicioLibros.java
    └── 📁 resources
        └── 📄 application.properties  (del Módulo 3, reutilizada)
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

## 🧭 Explicación paso a paso

1. `@Service` marca la clase como un componente de la capa de negocio;
   Spring la registra e inyecta automáticamente donde se necesite (por
   ejemplo, en el `Controller` del Ejemplo 06).
2. El constructor recibe `RepositorioLibros` — inyección de dependencias
   por constructor, la misma forma usada desde el Módulo 1.
3. Ninguno de los cinco métodos escribe SQL ni JPQL: todos delegan en los
   métodos que `JpaRepository` ya trae (`findAll`, `findById`, `save`,
   `existsById`, `deleteById`).
4. `actualizar` usa `Optional.map(...)`: si el `Libro` existe, actualiza
   su `titulo` y lo guarda, devolviendo el resultado envuelto en
   `Optional`; si no existe, `map` nunca se ejecuta y devuelve
   `Optional.empty()` automáticamente — sin necesitar un `if` explícito.
5. `eliminar` devuelve `boolean` en vez de no devolver nada, precisamente
   para que el `Controller` (Ejemplo 07) pueda distinguir si había algo
   para eliminar o no, y responder `200`/`404` según corresponda.

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué anotación marca una clase como parte de la capa
de lógica de negocio en Spring?

- **A.** `@RestController`.
- **B.** `@Entity`.
- **C.** `@Service`.
- **D.** `@Repository`.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. `@Service` es la anotación estándar para la
capa de lógica de negocio.

</details>

**2. [Abierta]** Un compañero te pregunta: "¿por qué `actualizar` y
`eliminar` en `ServicioLibros` devuelven `Optional<Libro>` y `boolean` en
vez de simplemente lanzar una excepción si el `id` no existe?".

**Pregunta**: ¿Qué le responderías?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Devolver `Optional`/`boolean` deja la decisión de
qué hacer ante un `id` inexistente en manos de quien llama al servicio —
en este caso, el `Controller`, que es quien sabe traducir esa ausencia al
código de estado HTTP correcto (`404`). Si `ServicioLibros` lanzara una
excepción directamente, estaría tomando una decisión que en realidad le
corresponde a la capa de arriba, y acoplaría la lógica de negocio a un
detalle de HTTP que no debería conocer.

</details>
