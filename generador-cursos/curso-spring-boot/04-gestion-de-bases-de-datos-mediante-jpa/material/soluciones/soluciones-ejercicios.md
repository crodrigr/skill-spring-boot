# 🔑 Soluciones — Ejercicios del Módulo 4

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante. Vive aparte de `material/ejercicios/` para que ninguna solución
> aparezca junto al enunciado.

## 🟢 Básico 03 — Crear un proyecto Spring Boot con persistencia

**Solución propuesta**:

`pom.xml` (dependencias relevantes, generadas por Spring Initializr):

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

`application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:biblioteca;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

`Main.java` (paquete raíz `com.biblioteca`), junto con los paquetes vacíos
`com.biblioteca.persistences.entities` y
`com.biblioteca.persistences.repositories` (capa `persistences`):

```java
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
```

**Verificación**: el log de arranque debe confirmar que el `ApplicationContext`
inició correctamente y que la conexión a `jdbc:h2:mem:biblioteca` quedó
lista, sin ningún `Entity` declarado todavía (es válido que el esquema esté
vacío en este ejercicio).

## 🟢 Básico 01 — Identificar el lado dueño de una relación

**Solución propuesta**:

- **Par 1 — `CarnetUniversitario`**: declara `@JoinColumn(name =
  "estudiante_id")`, la clave foránea real.
- **Par 2 — `Inscripcion`**: declara `@JoinColumn(name = "curso_id")`;
  `Curso` solo tiene `@OneToMany(mappedBy = "curso")`, el lado inverso.
- **Par 3 — `Libro`**: declara `@JoinTable(name = "libro_categoria", ...)`,
  la tabla intermedia real; `Categoria` solo tiene
  `@ManyToMany(mappedBy = "categorias")`.

En los tres casos, la señal inequívoca es la misma: el lado dueño es el que
tiene `@JoinColumn` o `@JoinTable`; `mappedBy` siempre marca al lado
inverso, nunca al dueño.

## 🟡 Intermedio 01 — Elegir `fetch` para una relación

**Solución propuesta**:

- **Escenario A (`Libro`↔`DetalleCatalogacion`)**: `LAZY`. Los detalles se
  consultan poco y en una pantalla aparte; cargarlos siempre junto con
  `Libro` sería trabajo desperdiciado en la inmensa mayoría de los casos.
- **Escenario B (`Factura`↔`LineaFactura`)**: `EAGER` (excepción
  justificada al valor por defecto `LAZY`). Las líneas se necesitan casi
  siempre que se muestra la factura; usar `LAZY` aquí solo agregaría la
  necesidad de recordar `@Transactional` en cada punto donde se muestra una
  factura, sin ningún ahorro real de rendimiento.

El criterio en ambos casos es el mismo: ¿con qué frecuencia se necesita la
colección junto con la entidad principal?

## 🟢 Básico 02 — `@ManyToMany` simple vs. entidad intermedia

**Solución propuesta**:

- **Relación 1 (`Medico`↔`Especialidad`)**: `@ManyToMany` simple. No hay
  ningún dato propio de la asociación en sí; solo interesa saber cuáles
  especialidades tiene cada médico.
- **Relación 2 (`Paciente`↔`Medico` vía `Consulta`)**: entidad intermedia
  (`Consulta`), con `fecha` y `diagnostico` como atributos propios de cada
  atención puntual, y dos relaciones `@ManyToOne` (hacia `Paciente` y hacia
  `Medico`).

El criterio decisivo es siempre el mismo: ¿la relación necesita guardar un
dato que no pertenece a ninguna de las dos entidades por separado?

## 🟡 Intermedio 02 — Agregar `cascade` a una relación padre-hijas

**Solución propuesta**:

```java
// com.biblioteca.persistences.entities.Usuario
@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.PERSIST)
    private List<Prestamo> prestamos = new ArrayList<>();

    protected Usuario() {
    }

    public Usuario(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public List<Prestamo> getPrestamos() { return prestamos; }

    public void agregarPrestamo(Prestamo prestamo) {
        prestamos.add(prestamo);
    }
}
```

```java
// Dentro de Main.run(...) — com.biblioteca.Main, paquete raíz
Usuario usuario = new Usuario("Renata Ibáñez");
Libro libro = repositorioLibros.save(new Libro("978-1-59327-584-6", "Cracking the Coding Interview"));
usuario.agregarPrestamo(new Prestamo(LocalDate.now(), usuario, libro));

repositorioUsuarios.save(usuario); // guarda usuario y su préstamo en una sola llamada
System.out.println("Préstamos guardados con cascade: " + usuario.getPrestamos().size());
```

**Salida esperada**: `Préstamos guardados con cascade: 1`.

## 🔴 Avanzado 01 — Diagnosticar una colección sin `orphanRemoval`

**Solución propuesta**: Falta `orphanRemoval = true` en `Paciente.citas`.
`cascade = CascadeType.ALL` propaga operaciones que el código ejecuta
explícitamente (guardar, actualizar, eliminar el padre), pero no vigila la
colección para detectar cuándo una `Cita` dejó de pertenecer a ella —
`quitarCita(...)` solo la saca de la lista en memoria, sin que eso, por sí
solo, la elimine de la base de datos. La corrección es:

```java
@OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Cita> citas = new ArrayList<>();
```

Con `orphanRemoval = true`, al guardar el `Paciente` después de
`quitarCita(...)`, Hibernate detecta que esa `Cita` ya no está en la
colección y la elimina de la base de datos.

## 🟡 Intermedio 03 — Implementar un CRUD parcial

**Solución propuesta** (`Autor` y `RepositorioAutores` en la capa
`persistences`; `Main` en el paquete raíz `com.biblioteca`, usando el
repositorio directamente porque todavía no existe una capa `services`):

```java
// com.biblioteca.Main
@SpringBootApplication
public class Main implements CommandLineRunner {

    private final RepositorioAutores repositorioAutores;

    public Main(RepositorioAutores repositorioAutores) {
        this.repositorioAutores = repositorioAutores;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        // Crear
        Autor autor = repositorioAutores.save(new Autor("Robert C. Martin"));

        // Leer
        Autor encontrado = repositorioAutores.findById(autor.getId()).orElseThrow();
        System.out.println("Leído: " + encontrado.getNombre());

        // Actualizar
        encontrado.setNombre("Robert Cecil Martin");
        repositorioAutores.save(encontrado);
        System.out.println("Actualizado: " + repositorioAutores.findById(autor.getId()).orElseThrow().getNombre());

        // Intencionalmente no se implementa ninguna operación de eliminación.
    }
}
```

**Verificación**: no aparece ninguna llamada a `deleteById` ni a `delete`
en ningún punto del código.

## 🔴 Avanzado 02 — Diagnosticar un arranque fallido a partir del log

**Solución propuesta**:

1. La causa raíz está en la línea
   `Unknown URL format "jdbc:h3:mem:biblioteca"` (repetida en el `Caused
   by:` final) — no en las líneas `BeanCreationException` ni
   `Application run failed`, que solo indican que un bean falló y que el
   arranque se abortó, respectivamente.
2. La parte mal configurada es `spring.datasource.url` en
   `application.properties`: dice `jdbc:h3:mem:biblioteca` en vez de
   `jdbc:h2:mem:biblioteca` (error de tipeo: `h3` en vez de `h2`).
3. Corrección:

```properties
spring.datasource.url=jdbc:h2:mem:biblioteca;DB_CLOSE_DELAY=-1
```

**Verificación**: con la URL corregida, el log debe mostrar
`HikariPool-1 - Start completed.` seguido de `Started Main in ... seconds`.

## 🏆 Desafío 01 — Órdenes de compra de Biblioteca Universitaria

**Solución propuesta** (entidades en
`com.biblioteca.persistences.entities`, repositorio en
`com.biblioteca.persistences.repositories`, `Main` en el paquete raíz
`com.biblioteca`):

```java
// com.biblioteca.persistences.entities.OrdenCompra
@Entity
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrdenCompra> detalles = new ArrayList<>();

    protected OrdenCompra() {
    }

    public OrdenCompra(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public List<DetalleOrdenCompra> getDetalles() { return detalles; }

    public void agregarDetalle(DetalleOrdenCompra detalle) {
        detalles.add(detalle);
    }

    public void quitarDetalle(DetalleOrdenCompra detalle) {
        detalles.remove(detalle);
    }
}
```

```java
// com.biblioteca.persistences.entities.DetalleOrdenCompra
@Entity
public class DetalleOrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String isbn;

    private Integer cantidad;

    @ManyToOne
    @JoinColumn(name = "orden_compra_id")
    private OrdenCompra ordenCompra;

    protected DetalleOrdenCompra() {
    }

    public DetalleOrdenCompra(String isbn, Integer cantidad, OrdenCompra ordenCompra) {
        this.isbn = isbn;
        this.cantidad = cantidad;
        this.ordenCompra = ordenCompra;
    }

    public String getIsbn() { return isbn; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
```

```java
// com.biblioteca.persistences.repositories.RepositorioOrdenesCompra
public interface RepositorioOrdenesCompra extends JpaRepository<OrdenCompra, Long> {
}
```

```java
// com.biblioteca.Main
@SpringBootApplication
public class Main implements CommandLineRunner {

    private final RepositorioOrdenesCompra repositorioOrdenesCompra;

    public Main(RepositorioOrdenesCompra repositorioOrdenesCompra) {
        this.repositorioOrdenesCompra = repositorioOrdenesCompra;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        // Crear (cascade)
        OrdenCompra orden = new OrdenCompra(LocalDate.now());
        orden.agregarDetalle(new DetalleOrdenCompra("978-0-13-468599-1", 5, orden));
        orden.agregarDetalle(new DetalleOrdenCompra("978-1-59327-584-6", 3, orden));
        orden = repositorioOrdenesCompra.save(orden);
        System.out.println("Líneas guardadas con cascade: " + orden.getDetalles().size());

        // Leer
        OrdenCompra leida = repositorioOrdenesCompra.findById(orden.getId()).orElseThrow();

        // Actualizar
        DetalleOrdenCompra detalleAActualizar = leida.getDetalles().get(0);
        detalleAActualizar.setCantidad(10);
        repositorioOrdenesCompra.save(leida);

        // Eliminar (orphanRemoval)
        DetalleOrdenCompra detalleAEliminar = leida.getDetalles().get(1);
        leida.quitarDetalle(detalleAEliminar);
        repositorioOrdenesCompra.save(leida);

        OrdenCompra recargada = repositorioOrdenesCompra.findById(orden.getId()).orElseThrow();
        System.out.println("Líneas después de eliminar una con orphanRemoval: " + recargada.getDetalles().size());
    }
}
```

**Verificación**: la salida esperada es
`Líneas guardadas con cascade: 2` seguida de
`Líneas después de eliminar una con orphanRemoval: 1`.
