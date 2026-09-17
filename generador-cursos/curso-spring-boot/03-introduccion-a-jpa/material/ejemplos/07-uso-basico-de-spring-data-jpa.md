# 💡 Ejemplo 07 — Uso básico de Spring Data JPA

## 🌍 Contexto

En el Ejemplo 06 viste, en el papel, que una interfaz de repositorio
reemplaza el código manual con `EntityManager`. Ahora vas a convertir
`RepositorioPacientes` —la interfaz del Módulo 2, que hoy vive respaldada
por `RepositorioPacientesEnMemoria`— en un repositorio de Spring Data JPA
real, ejecutándose contra H2.

**Qué busca demostrar este ejemplo**: el proyecto completo (dependencias,
`application.properties`, y sus `ddl-auto`) que **todo ejemplo de este
módulo** viene reutilizando desde el Ejemplo 01, presentado ahora con la
explicación completa de cada pieza; y la conversión real de
`RepositorioPacientes` en `JpaRepository<Paciente, Long>`, con un método
derivado y uno con `@Query` explícito.

## 🏥 Caso de estudio

MediSalud: `RepositorioPacientes` deja de tener una implementación manual en
memoria y pasa a ser un repositorio de Spring Data JPA.

## 🌳 Árbol de archivos (como se vería en VS Code)

```text
📁 ejemplo-07-uso-basico-de-spring-data-jpa
├── 📄 pom.xml                          (dependencias Maven)
└── 📁 src/main
    ├── 📁 java/com/medisalud
    │   ├── 📄 Paciente.java             (del Ejemplo 01, reutilizada)
    │   ├── 📄 RepositorioPacientes.java (evoluciona: del Módulo 2 a JpaRepository)
    │   └── 📄 Main.java                 (▶️ clic derecho → "Run Java" en VS Code)
    └── 📁 resources
        └── 📄 application.properties    (igual que en los Ejemplos 01-06, explicada aquí en detalle)
```

## 💻 Archivo: `pom.xml` (dependencias relevantes)

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

`spring-boot-starter-data-jpa` trae JPA, Hibernate y Spring Data JPA juntos;
`h2` es el driver de la base de datos en memoria que usa el curso. En un
proyecto contra MySQL o PostgreSQL, la única diferencia sería reemplazar
esta segunda dependencia por `mysql-connector-j` o el driver equivalente —
el resto del código no cambiaría.

<details>
<summary>📄 Ver código completo de <code>Paciente.java</code> (reutilizado del Ejemplo 01)</summary>

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

</details>

## 💻 Archivo: `RepositorioPacientes.java`

```java
public interface RepositorioPacientes extends JpaRepository<Paciente, Long> {

    // Método derivado: Spring Data JPA genera la consulta a partir del nombre
    Optional<Paciente> findByCodigo(String codigo);

    // Método con JPQL explícito, para una búsqueda que la convención de
    // nombres no resuelve directamente (coincidencia parcial de texto)
    @Query("SELECT p FROM Paciente p WHERE p.nombre LIKE %:fragmento%")
    List<Paciente> buscarPorFragmentoDeNombre(String fragmento);
}
```

## 💻 Archivo: `application.properties`

```properties
# Conexión a la base de datos H2 en memoria
spring.datasource.url=jdbc:h2:mem:medisalud;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Configuración de JPA e Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

# Consola web de H2, para inspeccionar las tablas generadas (opcional)
spring.h2.console.enabled=true
```

- **`spring.datasource.url`**: `jdbc:h2:mem:medisalud` crea una base H2 **en
  memoria** llamada `medisalud`; `DB_CLOSE_DELAY=-1` evita que H2 la borre en
  cuanto se cierra la primera conexión (útil porque Spring Boot abre y
  cierra conexiones del *pool* todo el tiempo).
- **`spring.jpa.database-platform`**: le indica a Hibernate qué dialecto SQL
  generar (aquí, el de H2); cambiaría a `MySQLDialect`, por ejemplo, contra
  MySQL.
- **`spring.jpa.hibernate.ddl-auto`**: controla qué hace Hibernate con el
  esquema (tablas) al iniciar. Cinco valores comunes:

  | Valor | Efecto | ¿Cuándo usarlo? |
  |---|---|---|
  | `none` | No toca el esquema. | Producción, con el esquema ya creado por otra vía (migraciones). |
  | `validate` | Verifica que el esquema existe y coincide con las entidades; si no, falla. | Producción, como control de seguridad. |
  | `update` | Crea/actualiza tablas y columnas **sin borrar datos** existentes. | Desarrollo (el valor usado en este curso). |
  | `create` | Borra y crea todo el esquema desde cero en cada arranque. | Pruebas puntuales, nunca con datos que importe conservar. |
  | `create-drop` | Crea el esquema al iniciar y lo borra al cerrar la aplicación. | Tests automatizados de corta duración. |

  Usar `create` o `create-drop` en un entorno con datos reales **pierde esos
  datos** en cada reinicio; por eso el curso usa `update` en desarrollo, y
  recomienda `validate` o `none` para producción.
- **`spring.h2.console.enabled`**: habilita una consola web (por defecto en
  `/h2-console`) para ver, con tus propios ojos, las tablas y filas que
  Hibernate generó — útil para comprobar visualmente el efecto de
  `ddl-auto`, aunque no es un requisito de ningún ejercicio.

## 💻 Archivo: `Main.java` (▶️ clic derecho → "Run Java" en VS Code)

```java
@SpringBootApplication
public class Main implements CommandLineRunner {

    private final RepositorioPacientes repositorioPacientes;

    public Main(RepositorioPacientes repositorioPacientes) {
        this.repositorioPacientes = repositorioPacientes;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        repositorioPacientes.save(new Paciente("P-005", "Elena Vidal"));

        Paciente porCodigo = repositorioPacientes.findByCodigo("P-005").orElseThrow();
        System.out.println("Encontrado por findByCodigo: " + porCodigo.getNombre());

        List<Paciente> porFragmento = repositorioPacientes.buscarPorFragmentoDeNombre("Vidal");
        System.out.println("Encontrado por JPQL (@Query): " + porFragmento.get(0).getNombre());
    }
}
```

## 🧭 Explicación paso a paso

1. `RepositorioPacientes` deja de ser una interfaz con métodos propios
   respaldada por `RepositorioPacientesEnMemoria` (Módulo 2): ahora extiende
   `JpaRepository<Paciente, Long>`, y esa implementación manual ya no se usa
   en este módulo.
2. Por extender `JpaRepository`, el repositorio ya trae `save`, `findById`,
   `findAll`, `deleteById`, etc., sin escribir una sola línea de más.
3. `findByCodigo(String codigo)` es un **método derivado**: Spring Data JPA
   lo implementa a partir del nombre (`findBy` + `Codigo`), generando la
   misma consulta que escribirías a mano con JPQL.
4. `buscarPorFragmentoDeNombre(...)`, con `@Query`, muestra la otra cara de
   Spring Data JPA: cuando la convención de nombres no alcanza (acá, una
   coincidencia parcial con `LIKE`), se escribe la consulta JPQL
   explícitamente, y Spring Data JPA la ejecuta igual.
5. `Main` ya no necesita `EntityManager` en absoluto: `RepositorioPacientes`
   es la única dependencia que `run(...)` usa para guardar y consultar.

## ✅ Resultado esperado

Al ejecutar `Main.java`:

```text
Encontrado por findByCodigo: Elena Vidal
Encontrado por JPQL (@Query): Elena Vidal
```

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué efecto tiene `spring.jpa.hibernate.ddl-auto=create`
al arrancar la aplicación?

- **A.** No toca el esquema existente.
- **B.** Verifica que el esquema coincida con las entidades, sin modificarlo.
- **C.** Borra y crea todo el esquema desde cero, perdiendo los datos existentes.
- **D.** Crea el esquema al iniciar y lo borra al cerrar la aplicación.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. `create` recrea el esquema completo en cada
arranque; para eso primero lo borra.

</details>

**2. [Selección múltiple]** Sobre `RepositorioPacientes` en este ejemplo,
seleccioná **todas** las afirmaciones correctas.

- **A.** `findByCodigo` no tiene cuerpo porque Spring Data JPA lo implementa por convención de nombres.
- **B.** `buscarPorFragmentoDeNombre` necesita `@Query` porque la convención de nombres no resuelve un `LIKE` parcial directamente.
- **C.** `RepositorioPacientesEnMemoria` del Módulo 2 sigue siendo necesaria para que esto funcione.
- **D.** Al extender `JpaRepository`, el repositorio ya tiene `save` y `findById` sin declararlos.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: `RepositorioPacientesEnMemoria`
queda obsoleta en este módulo; Spring Data JPA genera su propia
implementación.

</details>

**3. [Abierta]** Dejaste `spring.jpa.hibernate.ddl-auto=create-drop` en un
ambiente donde varios compañeros cargan datos de prueba durante el día, y al
otro día se quejan de que sus datos desaparecieron.

**Pregunta**: ¿Qué pasó, y qué valor usarías en su lugar?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: `create-drop` borra el esquema completo (con todos sus
datos) cada vez que la aplicación se apaga, y lo vuelve a crear vacío al
arrancar de nuevo — por eso los datos cargados el día anterior desaparecen.
Para un ambiente donde varias personas cargan datos que quieren conservar
entre reinicios, conviene usar `update`, que crea o ajusta el esquema sin
borrar los datos existentes.

</details>
