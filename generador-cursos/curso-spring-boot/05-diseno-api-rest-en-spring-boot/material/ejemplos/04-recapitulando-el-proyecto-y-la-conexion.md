# 💡 Ejemplo 04 — Recapitulando el proyecto y la conexión

## 🌍 Contexto

Los Módulos 3-4 ya construyeron un proyecto Spring Boot completo con
`Libro`/`RepositorioLibros` persistidos contra H2 en memoria. Este módulo
no vuelve a explicar esa parte: solo recapitula lo estrictamente necesario
para ubicarse, y agrega la única dependencia nueva que hace falta para
exponer una API web.

**Qué busca demostrar este ejemplo**: partir del proyecto ya existente y
agregarle `spring-boot-starter-web`, la dependencia que habilita
`@RestController` y el resto de las anotaciones de Spring MVC.

## 📚 Caso de estudio

Biblioteca Universitaria: `Libro`/`RepositorioLibros` del Módulo 3,
reutilizados sin ningún cambio de código.

## 🌳 Árbol de archivos (como se vería en VS Code)

```text
📁 gestion-bd-jpa (mismo proyecto de los Módulos 3-4)
├── 📄 pom.xml                  (se agrega spring-boot-starter-web)
└── 📁 src/main
    ├── 📁 java/com/biblioteca
    │   ├── 📄 Libro.java              (del Módulo 3, reutilizada)
    │   └── 📄 RepositorioLibros.java  (del Módulo 3, reutilizada)
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

## 💻 Archivo: `pom.xml` (única dependencia nueva)

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
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

## 🧭 Explicación paso a paso

1. `Libro`, `RepositorioLibros` y `application.properties` no cambian ni
   una línea respecto al Módulo 3: la persistencia ya está resuelta.
2. `spring-boot-starter-web` es la única dependencia nueva del módulo:
   agrega Spring MVC (el módulo que provee `@RestController` y las
   anotaciones de mapeo HTTP que se usan a partir del Ejemplo 06).
3. Si el proyecto ya se generó desde Spring Initializr sin esta
   dependencia (como en el Módulo 4), se puede agregar después, en
   cualquier momento, editando el `pom.xml` a mano — no hace falta
   regenerar el proyecto desde cero.
4. Con `spring-boot-starter-web` agregado, la aplicación ya puede
   arrancar un servidor HTTP embebido (Tomcat, por defecto), aunque
   todavía no exponga ningún endpoint propio — eso se agrega en los
   Ejemplos 05-07.

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué dependencia de Spring Boot habilita
`@RestController` y las anotaciones de mapeo HTTP?

- **A.** `spring-boot-starter-data-jpa`.
- **B.** `spring-boot-starter-web`.
- **C.** `h2`.
- **D.** Ninguna; `@RestController` viene incluido por defecto en todo proyecto Spring Boot.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. `spring-boot-starter-web` agrega Spring MVC,
que incluye `@RestController` y el resto de las anotaciones de mapeo
HTTP.

</details>

**2. [Abierta]** Un compañero te dice: "para agregar la capa web, voy a
crear un proyecto Spring Boot nuevo desde cero con Spring Initializr,
para no romper el que ya tengo funcionando".

**Pregunta**: ¿Es necesario crear un proyecto nuevo? ¿Qué le
recomendarías?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: No es necesario. Agregar una dependencia a un
proyecto Spring Boot existente es tan simple como agregar el bloque
`<dependency>` correspondiente al `pom.xml` ya existente; no hace falta
regenerar el proyecto ni perder el código de `Libro`/`RepositorioLibros`
ya escrito. Crear un proyecto nuevo solo tendría sentido si se quisiera
empezar un dominio completamente distinto, no para agregar una capa sobre
uno que ya funciona.

</details>

**3. [Selección múltiple]** Sobre este ejemplo, seleccioná **todas** las
afirmaciones correctas.

- **A.** `Libro` y `RepositorioLibros` no cambian ni una línea respecto al Módulo 3.
- **B.** `spring-boot-starter-web` reemplaza a `spring-boot-starter-data-jpa`.
- **C.** Después de agregar `spring-boot-starter-web`, la aplicación puede arrancar un servidor HTTP embebido.
- **D.** El proyecto sigue usando H2 en memoria, sin ningún cambio de base de datos.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: ambas dependencias
conviven en el mismo `pom.xml`; una no reemplaza a la otra, cada una
habilita una capa distinta (persistencia y web, respectivamente).

</details>
