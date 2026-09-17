# 🔑 Solución — Taller 01

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante.

## 🌳 Árbol de archivos (entregable final)

```text
📁 taller-01-libro-etiqueta
└── 📁 src/main
    ├── 📁 java/com/biblioteca
    │   ├── 📄 Libro.java
    │   ├── 📄 Etiqueta.java
    │   ├── 📄 RepositorioLibros.java
    │   └── 📄 RepositorioEtiquetas.java
    ├── 📁 java
    │   └── 📄 Main.java
    └── 📁 resources
        └── 📄 application.properties
```

## 📄 Archivo: `Libro.java`

```java
@Entity
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String isbn;

    private String titulo;

    @ManyToMany
    @JoinTable(
            name = "libro_etiqueta",
            joinColumns = @JoinColumn(name = "libro_id"),
            inverseJoinColumns = @JoinColumn(name = "etiqueta_id")
    )
    private Set<Etiqueta> etiquetas = new HashSet<>();

    protected Libro() {
    }

    public Libro(String isbn, String titulo) {
        this.isbn = isbn;
        this.titulo = titulo;
    }

    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public Set<Etiqueta> getEtiquetas() { return etiquetas; }

    public void agregarEtiqueta(Etiqueta etiqueta) {
        this.etiquetas.add(etiqueta);
    }
}
```

**Decisión de diseño**: `Libro` es el lado propietario (declara
`@JoinTable`) porque, en el flujo de uso más común de la biblioteca, se
etiqueta un libro (no al revés); es una elección razonable, no la única
correcta.

## 📄 Archivo: `Etiqueta.java`

```java
@Entity
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @ManyToMany(mappedBy = "etiquetas")
    private Set<Libro> libros = new HashSet<>();

    protected Etiqueta() {
    }

    public Etiqueta(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }
    public Set<Libro> getLibros() { return libros; }
}
```

## 📄 Archivo: `RepositorioLibros.java`

```java
public interface RepositorioLibros extends JpaRepository<Libro, Long> {
    Optional<Libro> findByIsbn(String isbn);
}
```

## 📄 Archivo: `RepositorioEtiquetas.java`

```java
public interface RepositorioEtiquetas extends JpaRepository<Etiqueta, Long> {
    Optional<Etiqueta> findByNombre(String nombre);
}
```

## 📄 Archivo: `Main.java`

```java
@SpringBootApplication
public class Main implements CommandLineRunner {

    private final RepositorioLibros repositorioLibros;
    private final RepositorioEtiquetas repositorioEtiquetas;

    public Main(RepositorioLibros repositorioLibros, RepositorioEtiquetas repositorioEtiquetas) {
        this.repositorioLibros = repositorioLibros;
        this.repositorioEtiquetas = repositorioEtiquetas;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    @Transactional
    public void run(String... args) {
        Libro libro = new Libro("978-0-596-00712-6", "Cuentos de Ciencia");
        libro.agregarEtiqueta(new Etiqueta("Novela"));
        libro.agregarEtiqueta(new Etiqueta("Ciencia"));
        repositorioLibros.save(libro);

        Libro recargado = repositorioLibros.findByIsbn("978-0-596-00712-6").orElseThrow();
        System.out.println("Etiquetas de \"" + recargado.getTitulo() + "\": " + recargado.getEtiquetas().size());

        Etiqueta etiqueta = repositorioEtiquetas.findByNombre("Ciencia").orElseThrow();
        System.out.println("Libros con la etiqueta \"Ciencia\": " + etiqueta.getLibros().size());
    }
}
```

## 📄 Archivo: `application.properties`

```properties
spring.datasource.url=jdbc:h2:mem:biblioteca;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

## ✅ Salida esperada

```text
Etiquetas de "Cuentos de Ciencia": 2
Libros con la etiqueta "Ciencia": 1
```

**Nota sobre `@Transactional`**: igual que en el Ejemplo 08, `run(...)` está
anotado `@Transactional` porque `recargado.getEtiquetas()` y
`etiqueta.getLibros()` acceden a colecciones `@ManyToMany`, cargadas de
forma perezosa por defecto.
