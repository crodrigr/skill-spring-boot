# 💡 Ejemplo 01 — ¿Qué es JPA?

## 🌍 Contexto

Hasta el Módulo 2, todo lo que construiste vivía **en memoria**: cerrabas el
programa y perdías los datos. **JPA** (*Java Persistence API*) es la
especificación estándar de Java para mapear objetos a una base de datos
relacional, de forma que un `Paciente` (por ejemplo) pueda guardarse y
recuperarse **sin escribir SQL a mano**.

JPA tiene tres características principales:

- **Abstracción de la base de datos**: trabajás con objetos Java
  (`entityManager.persist(paciente)`), no con sentencias `INSERT`/`UPDATE`.
- **Estandarización**: JPA es una API; quien la ejecuta realmente es una
  **implementación** (Hibernate, EclipseLink, OpenJPA — ver Ejemplo 05). El
  código de tu aplicación no cambia si mañana cambia la implementación.
- **Flexibilidad en el mapeo de entidades**: una misma clase puede mapearse a
  una tabla de distintas formas (nombres de columna, relaciones, estrategias
  de generación de ID), sin cambiar su lógica de negocio.

Y tres funcionalidades clave que vas a usar en este mismo ejemplo:

- **Anotaciones** (`@Entity`) para declarar qué clase se mapea a una tabla.
- **JPQL** (*Java Persistence Query Language*), un lenguaje de consulta
  parecido a SQL pero que opera sobre **entidades**, no sobre tablas.
- **API de persistencia** (`persist`, `find`, …) para las operaciones CRUD.

**Qué busca demostrar este ejemplo**: que las tres funcionalidades de JPA
funcionan de punta a punta sobre una clase que ya conocés (`Paciente`, del
Módulo 1), sin necesidad todavía de Spring Data JPA (que recién se introduce
en el bloque 6): alcanza con `@Entity` y un `EntityManager` inyectado
directamente.

## 🏥 Caso de estudio

MediSalud: el `record Paciente` del Módulo 1 se convierte en la primera
entidad JPA del curso.

## 🌳 Árbol de archivos (como se vería en VS Code)

```text
📁 ejemplo-01-que-es-jpa
└── 📁 src/main
    ├── 📁 java/com/medisalud
    │   ├── 📄 Paciente.java           (nuevo — @Entity; antes era un record en el Módulo 1)
    │   ├── 📄 ServicioPacientes.java  (nuevo — usa EntityManager directamente)
    │   └── 📄 Main.java               (▶️ @SpringBootApplication — clic derecho → "Run Java" en VS Code)
    └── 📁 resources
        └── 📄 application.properties  (la explicación de cada propiedad está en el Ejemplo 07; por ahora, copiala tal cual)
```

**Dependencias Maven** (`pom.xml`, resumidas): `spring-boot-starter-data-jpa`
(trae JPA + Hibernate) y `com.h2database:h2` (la base de datos en memoria).
El detalle completo de las dependencias vive en el Ejemplo 07.

## 💻 Archivo: `Paciente.java`

```java
@Entity
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // identidad técnica de persistencia

    @Column(unique = true)
    private String codigo; // identificador de negocio, heredado del Módulo 1

    private String nombre;

    protected Paciente() {
        // JPA exige un constructor sin argumentos; no lo usa el código de la aplicación
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
    public void registrarYBuscar() {
        Paciente paciente = new Paciente("P-001", "Ana Gómez");
        entityManager.persist(paciente); // API de persistencia: inserta la fila

        Paciente encontradoPorId = entityManager.find(Paciente.class, paciente.getId());
        System.out.println("Encontrado por find(): " + encontradoPorId.getNombre());

        List<Paciente> encontradosPorJpql = entityManager
                .createQuery("SELECT p FROM Paciente p WHERE p.codigo = :codigo", Paciente.class)
                .setParameter("codigo", "P-001")
                .getResultList();
        System.out.println("Encontrado por JPQL: " + encontradosPorJpql.get(0).getNombre());
    }
}
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
        servicioPacientes.registrarYBuscar();
    }
}
```

## 💻 Archivo: `application.properties`

```properties
spring.datasource.url=jdbc:h2:mem:medisalud;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

## 🧭 Explicación paso a paso

1. `Paciente` deja de ser el `record` inmutable del Módulo 1: JPA/Hibernate
   necesitan una clase con constructor sin argumentos y campos que puedan
   asignarse, así que pasa a ser una clase con *getters* (sin *setters*, para
   mantenerla lo más cercana posible a su espíritu original).
2. `@Entity` es la anotación que le dice a JPA "esta clase se mapea a una
   tabla". `@Id`/`@GeneratedValue` declaran la clave primaria técnica
   (`id`); `codigo` sigue siendo el identificador de negocio, ahora con
   `@Column(unique = true)`.
3. `ServicioPacientes` recibe un `EntityManager` con `@PersistenceContext`
   —el objeto central de JPA (se profundiza en el Ejemplo 02)— y lo usa
   directamente, sin pasar todavía por un repositorio de Spring Data JPA
   (eso llega en el bloque 6).
4. `entityManager.persist(...)` y `entityManager.find(...)` son la **API de
   persistencia**: insertar y buscar por clave primaria, sin una sola línea
   de SQL.
5. `entityManager.createQuery(...)` con una cadena `"SELECT p FROM Paciente
   p WHERE p.codigo = :codigo"` es **JPQL**: la sintaxis se parece a SQL,
   pero `Paciente`/`p.codigo` son la entidad y su propiedad, no una tabla ni
   una columna.
6. `@Transactional` envuelve todo el método en una única transacción: si
   algo fallara a mitad de camino, ninguna de las operaciones se confirmaría
   (se profundiza en el Ejemplo 02 y en el Ejemplo 08).
7. `Main` es una aplicación Spring Boot real (`@SpringBootApplication`):
   arranca el contenedor, que autoconfigura la conexión a H2 y el
   `EntityManagerFactory` a partir de `application.properties`, y ejecuta
   `run(...)` como `CommandLineRunner`.

## ✅ Resultado esperado

Al ejecutar `Main.java`:

```text
Encontrado por find(): Ana Gómez
Encontrado por JPQL: Ana Gómez
```

## ❓ Preguntas de repaso

**1. [Selección]** ¿Cuál de estas es una de las tres características
principales de JPA mencionadas en este ejemplo?

- **A.** Generación automática de interfaces REST.
- **B.** Estandarización: distintas implementaciones (Hibernate, EclipseLink) sin cambiar el código de la aplicación.
- **C.** Compilación más rápida del proyecto.
- **D.** Eliminación total de la necesidad de una base de datos.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Las otras tres no son características de JPA; la
estandarización es, específicamente, la independencia de una implementación
concreta.

</details>

**2. [Selección múltiple]** Sobre `ServicioPacientes` en este ejemplo,
seleccioná **todas** las afirmaciones correctas.

- **A.** `entityManager.persist(...)` inserta la fila sin que el código escriba SQL.
- **B.** `entityManager.createQuery(...)` con esa cadena es una consulta SQL nativa.
- **C.** `entityManager.find(...)` busca una entidad por su clave primaria.
- **D.** El método está anotado `@Transactional` para que las operaciones se confirmen como una unidad.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: esa consulta es JPQL
(opera sobre la entidad `Paciente` y su propiedad `codigo`), no SQL nativo
sobre una tabla.

</details>

**3. [Abierta]** En este escenario:

- `Paciente` era un `record` inmutable en el Módulo 1.
- En este ejemplo pasa a ser una clase con constructor sin argumentos y
  *getters*, sin *setters*.

**Pregunta**: ¿Por qué fue necesario ese cambio para convertir `Paciente` en
una entidad JPA?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Un `record` es inmutable por diseño: todos sus campos
son `final` y no tiene un constructor sin argumentos. JPA/Hibernate
necesitan poder instanciar la entidad sin argumentos (por ejemplo, al leerla
desde la base de datos) y asignarle los valores después, algo que un
`record` no permite. Por eso `Paciente` pasa a ser una clase mutable con un
constructor sin argumentos (usado solo por JPA) y otro con argumentos (usado
por el código de la aplicación), conservando *getters* pero no *setters*
para no perder más inmutabilidad de la estrictamente necesaria.

</details>
