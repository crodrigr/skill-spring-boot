# 💡 Ejemplo 06 — CRUD completo con Spring Data JPA

## 🌍 Contexto

Hasta ahora usaste `save` para crear y algún `findBy...` para leer, pero
nunca las cuatro operaciones juntas sobre la misma entidad. CRUD (Crear,
Leer, Actualizar, Eliminar) es el ciclo de vida completo de un registro, y
Spring Data JPA lo resuelve casi sin código propio: `JpaRepository` ya trae
`save` (crear y actualizar), `findById`/`findBy...` (leer) y `deleteById`
(eliminar).

**Qué busca demostrar este ejemplo**: ejecutar las cuatro operaciones sobre
`Libro`/`RepositorioLibros` (Módulo 3, Ejemplo 08), verificando el estado
de la base de datos después de cada una.

## 📚 Caso de estudio

Biblioteca Universitaria: `Libro`, con `RepositorioLibros` ya extendido con
`findByIsbn`.

## 🌳 Árbol de archivos (como se vería en VS Code)

```text
📁 ejemplo-06-crud-completo
└── 📁 src/main
    ├── 📁 java/com/biblioteca
    │   ├── 📄 Libro.java               (del Módulo 3, reutilizada)
    │   └── 📄 RepositorioLibros.java   (del Módulo 3, reutilizada)
    ├── 📁 java
    │   └── 📄 Main.java                (▶️ clic derecho → "Run Java" en VS Code)
    └── 📁 resources
        └── 📄 application.properties   (igual que en el Ejemplo 01, adaptado a Biblioteca)
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

## 💻 Archivo: `Main.java` (▶️ clic derecho → "Run Java" en VS Code)

```java
@SpringBootApplication
public class Main implements CommandLineRunner {

    private final RepositorioLibros repositorioLibros;

    public Main(RepositorioLibros repositorioLibros) {
        this.repositorioLibros = repositorioLibros;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        // Crear
        Libro libro = repositorioLibros.save(new Libro("978-0-13-468599-1", "Effective Java"));
        System.out.println("Creado: " + libro.getTitulo());

        // Leer
        Libro encontrado = repositorioLibros.findByIsbn("978-0-13-468599-1").orElseThrow();
        System.out.println("Leído: " + encontrado.getTitulo());

        // Actualizar
        encontrado.setTitulo("Effective Java (3rd Edition)");
        repositorioLibros.save(encontrado);
        Libro actualizado = repositorioLibros.findByIsbn("978-0-13-468599-1").orElseThrow();
        System.out.println("Actualizado: " + actualizado.getTitulo());

        // Eliminar
        repositorioLibros.deleteById(actualizado.getId());
        boolean sigueExistiendo = repositorioLibros.findByIsbn("978-0-13-468599-1").isPresent();
        System.out.println("¿Sigue existiendo tras deleteById?: " + sigueExistiendo);
    }
}
```

## 🧭 Explicación paso a paso

1. **Crear**: `repositorioLibros.save(new Libro(...))` inserta un registro
   nuevo, porque el `Libro` todavía no tiene `id` asignado (es `null` hasta
   que Hibernate lo genera con `@GeneratedValue`).
2. **Leer**: `findByIsbn(...)` (Módulo 3, Ejemplo 08) devuelve el `Libro`
   recién creado, envuelto en `Optional`.
3. **Actualizar**: `save(...)` con una entidad que **ya tiene** `id`
   (porque vino de `findByIsbn`) no crea un registro nuevo: actualiza el
   existente. Es el mismo método que crea; Hibernate distingue por la
   presencia del `id`.
4. **Eliminar**: `deleteById(...)` borra el registro; la segunda llamada a
   `findByIsbn` confirma que ya no está, devolviendo un `Optional` vacío.

## ✅ Resultado esperado

Al ejecutar `Main.java`:

```text
Creado: Effective Java
Leído: Effective Java
Actualizado: Effective Java (3rd Edition)
¿Sigue existiendo tras deleteById?: false
```

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué determina si `repositorioLibros.save(...)` crea un
registro nuevo o actualiza uno existente?

- **A.** El nombre de la variable usada.
- **B.** Si la entidad ya tiene un `id` asignado o no.
- **C.** El orden en que se llama a `save`.
- **D.** `save` siempre crea; para actualizar existe otro método.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Si el `id` es `null`, Hibernate inserta un
registro nuevo; si ya tiene un `id` (por ejemplo, porque la entidad vino de
un `findBy...`), actualiza el existente.

</details>

**2. [Abierta]** Un compañero llama a `repositorioLibros.deleteById(...)` y
después, en la misma ejecución, a `findByIsbn(...)` con el mismo ISBN, y le
sorprende que devuelva un `Optional` vacío en vez de una excepción.

**Pregunta**: ¿Por qué no lanza una excepción, y cómo debería manejar ese
caso en su código?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: `findByIsbn` (como cualquier `findBy...` de Spring
Data JPA que devuelve `Optional`) representa la ausencia de un resultado
como un `Optional` vacío, no como una excepción — buscar algo que no existe
no es un error. El código que consume ese resultado es responsable de
decidir qué hacer: usar `orElseThrow()` si la ausencia es un error en ese
contexto, o `isPresent()`/`orElse(...)` si es un caso válido a manejar sin
lanzar una excepción.

</details>

**3. [Selección]** Después de crear un `Libro` con `save(new Libro(...))`,
¿qué contiene su atributo `id`?

- **A.** Siempre `null`, porque no se asignó manualmente.
- **B.** El valor generado por la base de datos, disponible en el objeto devuelto por `save`.
- **C.** Un valor aleatorio generado en memoria por Java.
- **D.** El mismo `id` del último `Libro` guardado anteriormente.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. `@GeneratedValue(strategy = GenerationType.IDENTITY)`
delega la generación del `id` a la base de datos; el objeto que devuelve
`save` ya lo trae asignado.

</details>
