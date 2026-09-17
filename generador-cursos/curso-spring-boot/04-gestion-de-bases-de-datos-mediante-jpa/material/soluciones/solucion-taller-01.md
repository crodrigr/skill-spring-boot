# 🔑 Solución — Taller 01: Facturación de MediSalud

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante. Contiene el entregable completo del Taller 01.

## 🌳 Árbol de archivos (entregable final)

```text
📁 taller-01-facturacion
└── 📁 src/main
    ├── 📁 java/com/medisalud
    │   ├── 📄 Paciente.java
    │   ├── 📄 RepositorioPacientes.java
    │   ├── 📄 Factura.java
    │   ├── 📄 DetalleFactura.java
    │   ├── 📄 RepositorioFacturas.java
    │   └── 📄 Main.java
    └── 📁 resources
        └── 📄 application.properties
```

## 📄 Archivo: `Paciente.java`

```java
@Entity
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigo;

    private String nombre;

    protected Paciente() {
    }

    public Paciente(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
}
```

## 📄 Archivo: `RepositorioPacientes.java`

```java
public interface RepositorioPacientes extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByCodigo(String codigo);
}
```

## 📄 Archivo: `Factura.java`

```java
@Entity
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleFactura> detalles = new ArrayList<>();

    protected Factura() {
    }

    public Factura(LocalDate fecha, Paciente paciente) {
        this.fecha = fecha;
        this.paciente = paciente;
    }

    public Long getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public Paciente getPaciente() { return paciente; }
    public List<DetalleFactura> getDetalles() { return detalles; }

    public void agregarDetalle(DetalleFactura detalle) {
        detalles.add(detalle);
    }
}
```

## 📄 Archivo: `DetalleFactura.java`

```java
@Entity
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String concepto;

    private Double monto;

    @ManyToOne
    @JoinColumn(name = "factura_id")
    private Factura factura;

    protected DetalleFactura() {
    }

    public DetalleFactura(String concepto, Double monto, Factura factura) {
        this.concepto = concepto;
        this.monto = monto;
        this.factura = factura;
    }

    public Long getId() { return id; }
    public String getConcepto() { return concepto; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
}
```

## 📄 Archivo: `RepositorioFacturas.java`

```java
public interface RepositorioFacturas extends JpaRepository<Factura, Long> {
}
```

## 📄 Archivo: `Main.java`

```java
@SpringBootApplication
public class Main implements CommandLineRunner {

    private final RepositorioPacientes repositorioPacientes;
    private final RepositorioFacturas repositorioFacturas;

    public Main(RepositorioPacientes repositorioPacientes, RepositorioFacturas repositorioFacturas) {
        this.repositorioPacientes = repositorioPacientes;
        this.repositorioFacturas = repositorioFacturas;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        Paciente paciente = repositorioPacientes.save(new Paciente("P-022", "Carla Núñez"));

        // Alta: una factura con dos detalles, guardados en una sola llamada gracias a cascade
        Factura factura = new Factura(LocalDate.now(), paciente);
        factura.agregarDetalle(new DetalleFactura("Consulta general", 25.0, factura));
        factura.agregarDetalle(new DetalleFactura("Análisis de laboratorio", 40.0, factura));
        factura = repositorioFacturas.save(factura);
        System.out.println("Detalles guardados con cascade: " + factura.getDetalles().size());

        // Edición: modificar el monto de un detalle ya guardado
        DetalleFactura detalleAEditar = factura.getDetalles().get(1);
        detalleAEditar.setMonto(45.0);
        repositorioFacturas.save(factura);

        Factura recargada = repositorioFacturas.findById(factura.getId()).orElseThrow();
        System.out.println("Monto actualizado: " + recargada.getDetalles().get(1).getMonto());
    }
}
```

## 📄 Archivo: `application.properties`

```properties
spring.datasource.url=jdbc:h2:mem:medisalud;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

## ✅ Resultado esperado

Al ejecutar `Main.java`:

```text
Detalles guardados con cascade: 2
Monto actualizado: 45.0
```
