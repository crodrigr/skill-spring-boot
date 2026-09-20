# 🔑 Soluciones — Ejercicios del Módulo 3

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante. Vive aparte de `material/ejercicios/` para que ninguna solución
> aparezca junto al enunciado.

## 🟢 Básico 01 — Características y funcionalidades de JPA

**Solución propuesta**:

- **1. Correcta.** Es la característica de "abstracción de la base de datos".
- **2. Incorrecta.** JPA es una especificación; Hibernate es solo una de sus
  implementaciones posibles (junto con EclipseLink, OpenJPA, etc.).
- **3. Incorrecta.** JPQL opera sobre entidades y propiedades, no sobre
  tablas y columnas — esa es justamente la diferencia con SQL.
- **4. Correcta.** Es la funcionalidad clave de "uso de anotaciones".
- **5. Correcta.** Es la funcionalidad clave de "API de persistencia".
- **6. Incorrecta.** Es justamente lo contrario de la estandarización: el
  código de la aplicación (basado en la API de JPA) no debería cambiar al
  reemplazar la implementación.

## 🟢 Básico 02 — Componentes de la arquitectura de JPA

**Solución propuesta**:

- (a) → `EntityTransaction`
- (b) → `EntityManagerFactory`
- (c) → `Query`
- (d) → `EntityManager`
- (e) → `@Entity`

`EntityManagerFactory` crea instancias de `EntityManager`: la fábrica existe
primero (una por aplicación, en la práctica), y de ella se obtienen los
`EntityManager` que efectivamente hacen el trabajo de persistencia.

## 🟢 Básico 03 — Identificar tipos de relación entre entidades

**Solución propuesta**:

- **Par 1 — uno a muchos**: "un `Medico` puede atender varias `Cita`" fija
  el límite de "varios" del lado `Cita`; "cada `Cita` tiene un único
  `Medico`" fija el límite de "uno" del lado `Medico`.
- **Par 2 — uno a muchos**: "una `Categoria` agrupa varios `Libro`" es el
  lado "muchos" (`Libro`); "un `Libro` pertenece a una única `Categoria`" es
  el lado "uno" (`Categoria`).
- **Par 3 — muchos a muchos**: ambos lados admiten "varios" ("un
  `Estudiante` puede inscribirse en varios `Curso`", "un `Curso` puede tener
  varios `Estudiante`"), sin ningún límite de "uno" en ninguno de los dos
  sentidos.

## 🟡 Intermedio 01 — Convertir `RepositorioLibros` en un repositorio de Spring Data JPA

**Solución propuesta** (capa `persistences`: entidades en
`com.biblioteca.persistences.entities`, repositorios en
`com.biblioteca.persistences.repositories`; `Main` en el paquete raíz
`com.biblioteca`):

```java
// com.biblioteca.persistences.entities.Libro
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
}

// com.biblioteca.persistences.repositories.RepositorioLibros
public interface RepositorioLibros extends JpaRepository<Libro, Long> {
    Optional<Libro> findByIsbn(String isbn);
}

// com.biblioteca.Main (paquete raíz; todavía no existe capa services)
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
        repositorioLibros.save(new Libro("978-3-16-148410-0", "Estructuras de Datos"));
        Libro encontrado = repositorioLibros.findByIsbn("978-3-16-148410-0").orElseThrow();
        System.out.println(encontrado.getTitulo());
    }
}
```

**Salida esperada**: `Estructuras de Datos`.

## 🔴 Avanzado 02 — Diagnosticar una pérdida de datos por `ddl-auto`

**Solución propuesta**: `create-drop` borra el esquema completo —incluidos
todos sus datos— cada vez que la aplicación se apaga, y lo recrea vacío al
volver a arrancar; por eso los pacientes cargados el día anterior
desaparecen en cada reinicio. La corrección es cambiar
`spring.jpa.hibernate.ddl-auto` a `update`, que crea o ajusta el esquema
(tablas/columnas) sin borrar los datos ya existentes. `validate` y `none`
no son apropiados todavía porque ambos requieren que el esquema ya exista
de antemano (`validate` falla si no coincide; `none` no lo toca en
absoluto), y en esta etapa del proyecto el esquema se sigue generando desde
las entidades.

## 🟡 Intermedio 02 — Convertir una clase en entidad JPA

**Solución propuesta**:

```java
// com.biblioteca.persistences.entities.Categoria
@Entity
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    protected Categoria() {
    }

    public Categoria(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}

// com.biblioteca.persistences.repositories.RepositorioCategorias
public interface RepositorioCategorias extends JpaRepository<Categoria, Long> {
}
```

## 🟡 Intermedio 03 — Mapear una relación uno a muchos

**Solución propuesta**:

```java
// com.medisalud.persistences.entities.Medico
@Entity
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @OneToMany(mappedBy = "medico")
    private List<Cita> citas = new ArrayList<>();

    protected Medico() {
    }

    public Medico(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }
    public List<Cita> getCitas() { return citas; }
}

// En com.medisalud.persistences.entities.Cita, se agrega:
@ManyToOne
@JoinColumn(name = "medico_id")
private Medico medico;
```

`Cita` es el lado propietario (tiene la columna `medico_id`, igual que ya
tiene `paciente_id` para su relación con `Paciente`); `Medico` es el lado
inverso, con `mappedBy = "medico"` apuntando al nombre del campo declarado
en `Cita`.

## 🔴 Avanzado 01 — Mapear una relación `@OneToOne` o `@ManyToMany`

**Solución propuesta (Opción A, `@OneToOne`)**:

```java
// com.biblioteca.persistences.entities.Libro
@Entity
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String isbn;

    private String titulo;

    @OneToOne
    @JoinColumn(name = "ficha_tecnica_id")
    private FichaTecnica fichaTecnica;

    protected Libro() {
    }

    public Libro(String isbn, String titulo) {
        this.isbn = isbn;
        this.titulo = titulo;
    }

    public void asignarFichaTecnica(FichaTecnica fichaTecnica) {
        this.fichaTecnica = fichaTecnica;
    }
}

// com.biblioteca.persistences.entities.FichaTecnica
@Entity
public class FichaTecnica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numeroPaginas;
    private String editorial;

    protected FichaTecnica() {
    }

    public FichaTecnica(int numeroPaginas, String editorial) {
        this.numeroPaginas = numeroPaginas;
        this.editorial = editorial;
    }
}
```

`Libro` es el lado propietario (tiene `ficha_tecnica_id`); la relación es
unidireccional, igual que `Paciente`↔`HistoriaClinica` en el Ejemplo 08.

**Solución propuesta (Opción B, `@ManyToMany`)**: análoga a
`Libro`↔`Autor` del Ejemplo 08 y a `Libro`↔`Etiqueta` del Taller 01: una de
las dos clases (`Estudiante` o `Curso`) declara `@ManyToMany` con
`@JoinTable`, y la otra usa `@ManyToMany(mappedBy = "...")`.

## 🏆 Desafío 01 — Agregar `Medico` al esquema de MediSalud

**Solución propuesta**:

```java
// com.medisalud.persistences.entities.Medico
@Entity
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @OneToMany(mappedBy = "medico")
    private List<Cita> citas = new ArrayList<>();

    protected Medico() {
    }

    public Medico(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }
    public List<Cita> getCitas() { return citas; }
}

// com.medisalud.persistences.entities.Cita se extiende con:
@ManyToOne
@JoinColumn(name = "medico_id")
private Medico medico;
// + constructor y getter actualizados para recibir/exponer medico

// com.medisalud.persistences.repositories.RepositorioMedicos
public interface RepositorioMedicos extends JpaRepository<Medico, Long> {
    Optional<Medico> findByNombre(String nombre);
}

// com.medisalud.Main (paquete raíz; todavía no existe capa services)
@SpringBootApplication
public class Main implements CommandLineRunner {

    private final RepositorioPacientes repositorioPacientes;
    private final RepositorioCitas repositorioCitas;
    private final RepositorioMedicos repositorioMedicos;

    public Main(RepositorioPacientes repositorioPacientes,
                RepositorioCitas repositorioCitas,
                RepositorioMedicos repositorioMedicos) {
        this.repositorioPacientes = repositorioPacientes;
        this.repositorioCitas = repositorioCitas;
        this.repositorioMedicos = repositorioMedicos;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    @Transactional // necesario para leer medico.getCitas() más abajo (colección LAZY)
    public void run(String... args) {
        Paciente paciente = repositorioPacientes.save(new Paciente("P-007", "Gabriela Soto"));
        paciente.asignarHistoriaClinica(new HistoriaClinica("Sin antecedentes"));
        repositorioPacientes.save(paciente);

        Medico medico = repositorioMedicos.save(new Medico("Dra. Helena Paz"));

        repositorioCitas.save(new Cita(LocalDate.of(2026, 12, 1), "Consulta general", paciente, medico));
        repositorioCitas.save(new Cita(LocalDate.of(2026, 12, 20), "Control", paciente, medico));

        Medico recargado = repositorioMedicos.findByNombre("Dra. Helena Paz").orElseThrow();
        System.out.println("Citas de " + recargado.getNombre() + ": " + recargado.getCitas().size());
    }
}
```

**Salida esperada**: `Citas de Dra. Helena Paz: 2`.

**Verificación**: `Cita` sigue siendo el lado propietario tanto de su
relación con `Paciente` como de la nueva relación con `Medico` (dos columnas
de clave foránea: `paciente_id` y `medico_id`); ninguna de las dos relaciones
es circular, porque ambas tienen un único lado propietario bien definido.
