# 💡 Ejemplo 05 — Hibernate y su relación con JPA

## 🌍 Contexto

En el Ejemplo 04 viste **qué** hace un ORM; ahora vas a ver **quién** lo hace
realmente en este curso: **Hibernate**, un framework de mapeo
objeto-relacional para Java. Cuando llamás a `entityManager.persist(...)`,
es Hibernate quien:

- Convierte la clase `Paciente` en la tabla `paciente`.
- Convierte sus atributos (`codigo`, `nombre`) en columnas.
- Convierte relaciones entre objetos en relaciones entre tablas (se ve en
  el Ejemplo 08).
- Genera y ejecuta el SQL necesario para insertar, consultar, actualizar y
  eliminar.

**JPA es la especificación; Hibernate es una de sus implementaciones.** Por
eso se dice que "JPA define el qué, y Hibernate define el cómo": el código
de `ServicioPacientes` (Ejemplos 01-02) usa únicamente tipos de la API de
JPA (`EntityManager`, `@Entity`, `@PersistenceContext`) — ninguna clase de
Hibernate aparece en ese código —, y sin embargo es Hibernate quien lo
ejecuta por debajo, autoconfigurado por `spring-boot-starter-data-jpa`.

**Qué busca demostrar este ejemplo**: hacer visible el trabajo de Hibernate
activando el registro del SQL generado, sobre el mismo `Paciente` que ya
conocés.

## 🏥 Caso de estudio

MediSalud: ver el SQL real que Hibernate genera al guardar un `Paciente`.

## 🌳 Árbol de archivos (como se vería en VS Code)

```text
📁 ejemplo-05-hibernate
└── 📁 src/main
    ├── 📁 java/com/medisalud
    │   ├── 📄 Paciente.java           (del Ejemplo 01, reutilizada)
    │   ├── 📄 ServicioPacientes.java  (del Ejemplo 01, reutilizada tal cual)
    │   └── 📄 Main.java               (▶️ clic derecho → "Run Java" en VS Code)
    └── 📁 resources
        └── 📄 application.properties  (extendida con spring.jpa.show-sql)
```

<details>
<summary>📄 Ver código completo de <code>Paciente.java</code> y <code>ServicioPacientes.java</code> (reutilizados del Ejemplo 01)</summary>

## 💻 Archivo: `Paciente.java`

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

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }
}
```

## 💻 Archivo: `ServicioPacientes.java`

```java
@Service
public class ServicioPacientes {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void registrarUnPaciente() {
        entityManager.persist(new Paciente("P-004", "Diego Torres"));
    }
}
```

</details>

## 💻 Archivo: `application.properties`

```properties
spring.datasource.url=jdbc:h2:mem:medisalud;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

## 💻 Archivo: `Main.java` (▶️ clic derecho → "Run Java" en VS Code)

```java
@SpringBootApplication
public class Main implements CommandLineRunner {

    private final ServicioPacientes servicioPacientes;

    public Main(ServicioPacientes servicioPacientes) {
        this.servicioPacientes = servicioPacientes;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        servicioPacientes.registrarUnPaciente();
    }
}
```

## 🧭 Explicación paso a paso

1. `ServicioPacientes` y `Paciente` son exactamente los del Ejemplo 01: no
   cambia ni una línea de código de la aplicación.
2. La única novedad es `spring.jpa.show-sql=true` (más
   `hibernate.format_sql=true` para que se lea mejor): le pide a Hibernate
   que imprima en consola el SQL real que genera para cada operación.
3. Al ejecutar, Hibernate traduce `entityManager.persist(...)` a un
   `INSERT INTO paciente (...)` real, con los nombres de tabla/columna que
   dedujo de `@Entity`/`@Column` — visible en la consola, aunque el código
   de `ServicioPacientes` nunca lo menciona.
4. Esto es "JPA define el qué, Hibernate define el cómo" en la práctica: el
   *qué* (`persist` una entidad) está en el código; el *cómo* (el SQL exacto,
   con sus comillas, tipos y sintaxis específicos de H2) es responsabilidad
   de Hibernate, y cambiaría si se usara otra implementación de JPA o otra
   base de datos, sin tocar `ServicioPacientes`.

## ✅ Resultado esperado

Al ejecutar `Main.java` (salida simplificada; Hibernate agrega más detalle):

```text
Hibernate:
    insert
    into
        paciente
        (codigo, nombre, id)
    values
        (?, ?, default)
```

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué es Hibernate, en relación con JPA?

- **A.** Una versión más nueva de JPA que la reemplaza por completo.
- **B.** Una implementación de la especificación JPA.
- **C.** Un lenguaje de consulta alternativo a JPQL.
- **D.** Una base de datos relacional.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Hibernate implementa la especificación JPA;
EclipseLink u OpenJPA son otras implementaciones posibles.

</details>

**2. [Selección múltiple]** Sobre este ejemplo, seleccioná **todas** las
afirmaciones correctas.

- **A.** El código de `ServicioPacientes` no cambió respecto al Ejemplo 01.
- **B.** `spring.jpa.show-sql=true` hace que Hibernate imprima el SQL que genera.
- **C.** El `INSERT` que aparece en consola fue escrito a mano por el desarrollador.
- **D.** Hibernate dedujo los nombres de tabla y columna a partir de la clase `Paciente` y sus anotaciones.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: ese `INSERT` lo generó
Hibernate, no el desarrollador.

</details>

**3. [Abierta]** En una entrevista te preguntan: "¿qué pasaría con el código
de `ServicioPacientes` si mañana reemplazan Hibernate por otra
implementación de JPA, como EclipseLink?".

**Pregunta**: ¿Qué le responderías, y por qué?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: El código de `ServicioPacientes` no debería cambiar,
porque usa exclusivamente tipos de la API de JPA (`EntityManager`,
`@PersistenceContext`, `@Entity`), no clases propias de Hibernate. Eso es
precisamente la estandarización de JPA (Ejemplo 01): la aplicación programa
contra la especificación, y la implementación (Hibernate hoy, EclipseLink
mañana) es intercambiable por configuración, sin tocar la lógica de
negocio.

</details>
