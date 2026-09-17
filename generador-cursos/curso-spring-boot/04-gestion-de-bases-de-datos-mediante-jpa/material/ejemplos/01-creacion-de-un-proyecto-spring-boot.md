# 💡 Ejemplo 01 — Creación de un proyecto Spring Boot

## 🌍 Contexto

En los Módulos 1-3 siempre partiste de un proyecto ya armado, con su
`pom.xml` y su estructura de carpetas ya decididos. En cualquier trabajo
real, ese proyecto no existe todavía: alguien tiene que crearlo. Este
módulo empieza justo ahí: cómo nace un proyecto Spring Boot desde cero, y
cómo se conecta a una base de datos.

**Qué busca demostrar este ejemplo**: el camino completo desde "no hay
ningún proyecto" hasta "una aplicación Spring Boot que persiste un
`Paciente` en H2", usando **Spring Initializr** (`start.spring.io`), la
forma estándar y gratuita de generar un proyecto Spring Boot sin instalar
nada adicional.

## 🏥 Caso de estudio

MediSalud: el `Paciente` del Módulo 3, ahora dentro de un proyecto creado
por vos desde cero.

## 🪜 Paso 1 — Generar el proyecto con Spring Initializr

1. Entrá a [start.spring.io](https://start.spring.io) (o generalo desde el
   asistente "Spring Boot" de tu IDE — IntelliJ y Spring Tools para VS Code
   ofrecen el mismo formulario integrado).
2. Elegí:
   - **Project**: Maven.
   - **Language**: Java.
   - **Spring Boot**: la versión estable más reciente de la serie 3.x.
   - **Group**: `com.medisalud`.
   - **Artifact**: `gestion-bd-jpa`.
   - **Packaging**: Jar.
   - **Java**: 17.
3. En "Dependencies", agregá:
   - **Spring Data JPA** (repositorios + integración con Hibernate).
   - **H2 Database** (el driver de la base de datos en memoria del curso).
4. Hacé clic en "Generate": se descarga un `.zip` con el proyecto completo.
5. Descomprimí el `.zip` y abrí la carpeta en VS Code.

**Nota**: elegir las dependencias en el formulario de Spring Initializr es
exactamente lo que agrega estas líneas al `pom.xml` generado — no hace
falta escribirlas a mano.

## 🌳 Árbol de archivos (como quedaría después de descomprimir y agregar el código)

```text
📁 gestion-bd-jpa
├── 📄 pom.xml                  (generado por Spring Initializr)
└── 📁 src/main
    ├── 📁 java/com/medisalud
    │   ├── 📄 Paciente.java    (del Módulo 3, reutilizada)
    │   └── 📄 Main.java        (▶️ clic derecho → "Run Java" en VS Code)
    └── 📁 resources
        └── 📄 application.properties
```

## 💻 Archivo: `pom.xml` (dependencias relevantes, ya agregadas por Spring Initializr)

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

## 💻 Archivo: `Paciente.java` (del Módulo 3, reutilizada)

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

**Nota**: esta versión de `Paciente` es la básica del Módulo 3 (Ejemplo
01/07), sin las relaciones `citas`/`historiaClinica` que se agregaron
después en el Ejemplo 08 — se irán retomando a lo largo de este módulo a
medida que cada bloque las necesite.

## 💻 Archivo: `application.properties`

```properties
spring.datasource.url=jdbc:h2:mem:medisalud;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

## 💻 Archivo: `Main.java` (▶️ clic derecho → "Run Java" en VS Code)

```java
@SpringBootApplication
public class Main implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    @Transactional
    public void run(String... args) {
        Paciente paciente = new Paciente("P-010", "Irene Vega");
        entityManager.persist(paciente);

        Paciente encontrado = entityManager.find(Paciente.class, paciente.getId());
        System.out.println("Paciente guardado en el proyecto nuevo: " + encontrado.getNombre());
    }
}
```

## 🧭 Explicación paso a paso

1. Spring Initializr genera el `pom.xml` con exactamente las dependencias
   que elegiste en el formulario — no hay ningún paso manual de edición de
   XML.
2. `Paciente` se copia tal cual del Módulo 3: convertirla en entidad JPA ya
   se hizo antes, y ese trabajo no cambia por vivir en un proyecto nuevo.
3. `application.properties` es el mismo tipo de configuración usada en
   todo el Módulo 3: conecta el proyecto a H2 en memoria.
4. `Main` usa `EntityManager` directamente (como en el Ejemplo 01 del
   Módulo 3) para demostrar la persistencia con el mínimo código posible;
   los ejemplos siguientes de este módulo ya usan Spring Data JPA.
5. Al ejecutar `Main.java`, Spring Boot arranca el contenedor, autoconfigura
   la conexión a H2 a partir de `application.properties`, y el método
   `run(...)` persiste y recupera el paciente.

## ✅ Resultado esperado

Al ejecutar `Main.java`:

```text
Paciente guardado en el proyecto nuevo: Irene Vega
```

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué dependencias hay que agregar en Spring Initializr
para persistencia con H2 en un proyecto Spring Boot?

- **A.** Spring Web y Spring Security.
- **B.** Spring Data JPA y H2 Database.
- **C.** Spring Data JPA y MySQL Driver.
- **D.** Ninguna; Spring Boot ya incluye persistencia por defecto.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Spring Data JPA aporta JPA/Hibernate y los
repositorios; H2 Database es el driver de la base de datos en memoria
usada en el curso.

</details>

**2. [Selección múltiple]** Sobre la creación de un proyecto con Spring
Initializr, seleccioná **todas** las afirmaciones correctas.

- **A.** Elegir las dependencias en el formulario agrega las líneas correspondientes al `pom.xml` generado.
- **B.** Es obligatorio usar un IDE específico; Spring Initializr no puede usarse desde el navegador.
- **C.** El `.zip` generado ya trae la estructura de carpetas `src/main/java` y `src/main/resources`.
- **D.** El asistente "Spring Boot" de IntelliJ o Spring Tools para VS Code ofrece el mismo formulario integrado en el IDE.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: `start.spring.io` funciona
directamente desde el navegador, sin ningún IDE.

</details>

**3. [Abierta]** En este escenario:

- Creaste un proyecto Spring Boot nuevo con Spring Initializr.
- Copiaste `Paciente.java` del Módulo 3 tal cual, sin cambiarle una línea.
- El proyecto persiste y recupera un paciente correctamente.

**Pregunta**: ¿Por qué no hizo falta modificar `Paciente.java` para que
funcione en este proyecto nuevo?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: `Paciente` ya es una entidad JPA completa desde el
Módulo 3 (con `@Entity`, `@Id`, `@GeneratedValue` y `@Column`); esas
anotaciones no dependen de en qué proyecto vive la clase, sino de que el
proyecto tenga las dependencias de JPA/Hibernate en su classpath —
exactamente lo que Spring Initializr agregó al elegir `Spring Data JPA`.
Por eso la misma clase funciona igual en cualquier proyecto Spring Boot que
tenga esa dependencia.

</details>
