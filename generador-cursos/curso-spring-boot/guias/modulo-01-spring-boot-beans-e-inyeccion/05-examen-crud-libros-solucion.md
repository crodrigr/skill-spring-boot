# 🧑‍🏫 Examen — CRUD de libros: solución y pauta de corrección

> 🔒 **Material docente.** Contiene la solución completa del [enunciado](04-examen-crud-libros.md): no se entrega a los estudiantes.
>
> 📦 **Código fuente:** [`codigo/examen-libros/`](codigo/examen-libros/) (proyecto Maven completo) · ✅ compilado y ejecutado con Spring Boot 3.3 y Java 21.
>
> ✅ **Verificada.** Las respuestas de la sección «Resultados reales» son salidas de la aplicación en ejecución.

<details>
<summary><strong>🏗️ Estructura esperada</strong></summary>

> ```text
> 📁 biblioteca
> ├── 📄 pom.xml
> └── 📁 src/main/java/com/biblioteca
>     ├── 📄 BibliotecaApplication.java
>     ├── 📁 presentacion
>     │   └── 📁 controlador
>     │       └── 📄 ControladorLibros.java
>     ├── 📁 negocio
>     │   ├── 📁 model
>     │   │   └── 📄 Libro.java
>     │   └── 📁 service
>     │       └── 📄 ServicioLibros.java
>     └── 📁 persistence
>         └── 📁 repository
>             ├── 📄 RepositorioLibros.java              (interfaz)
>             └── 📄 RepositorioLibrosEnMemoria.java     (@Repository)
> ```

</details>

<details>
<summary><strong>💻 Código de la solución</strong></summary>

> ### 📁 Configuración
>
> <details>
> <summary><strong>📄 pom.xml</strong></summary>
>
> **Archivo:** [`pom.xml`](codigo/examen-libros/pom.xml)
>
> ```xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>3.3.5</version>
>         <relativePath/>
>     </parent>
>     <groupId>com.biblioteca</groupId>
>     <artifactId>biblioteca</artifactId>
>     <version>0.0.1-SNAPSHOT</version>
>     <name>biblioteca</name>
>     <description>Examen: API de libros (en memoria)</description>
>     <properties>
>         <java.version>21</java.version>
>     </properties>
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>     </dependencies>
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 BibliotecaApplication.java</strong></summary>
>
> **Archivo:** [`src/main/java/com/biblioteca/BibliotecaApplication.java`](codigo/examen-libros/src/main/java/com/biblioteca/BibliotecaApplication.java)
>
> ```java
> package com.biblioteca;
>
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
>
> @SpringBootApplication
> public class BibliotecaApplication {
>
>     public static void main(String[] args) {
>         SpringApplication.run(BibliotecaApplication.class, args);
>     }
> }
> ```
>
> </details>
>
> ---
>
> ### 📁 negocio — model
>
> <details>
> <summary><strong>📄 Libro.java</strong></summary>
>
> **Archivo:** [`src/main/java/com/biblioteca/negocio/model/Libro.java`](codigo/examen-libros/src/main/java/com/biblioteca/negocio/model/Libro.java)
>
> ```java
> package com.biblioteca.negocio.model;
>
> public class Libro {
>
>     private final Long id;
>     private final String titulo;
>     private final String autor;
>     private final int anioPublicacion;
>
>     public Libro(Long id, String titulo, String autor, int anioPublicacion) {
>         this.id = id;
>         this.titulo = titulo;
>         this.autor = autor;
>         this.anioPublicacion = anioPublicacion;
>     }
>
>     public Long getId() { return id; }
>     public String getTitulo() { return titulo; }
>     public String getAutor() { return autor; }
>     public int getAnioPublicacion() { return anioPublicacion; }
> }
> ```
>
> </details>
>
> ---
>
> ### 📁 persistence — repository
>
> <details>
> <summary><strong>📄 RepositorioLibros.java</strong></summary>
>
> **Archivo:** [`src/main/java/com/biblioteca/persistence/repository/RepositorioLibros.java`](codigo/examen-libros/src/main/java/com/biblioteca/persistence/repository/RepositorioLibros.java)
>
> ```java
> package com.biblioteca.persistence.repository;
>
> import com.biblioteca.negocio.model.Libro;
> import java.util.List;
> import java.util.Optional;
>
> public interface RepositorioLibros {
>
>     List<Libro> listar();
>
>     Optional<Libro> buscarPorId(Long id);
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>📄 RepositorioLibrosEnMemoria.java</strong></summary>
>
> **Archivo:** [`src/main/java/com/biblioteca/persistence/repository/RepositorioLibrosEnMemoria.java`](codigo/examen-libros/src/main/java/com/biblioteca/persistence/repository/RepositorioLibrosEnMemoria.java)
>
> ```java
> package com.biblioteca.persistence.repository;
>
> import com.biblioteca.negocio.model.Libro;
> import java.util.ArrayList;
> import java.util.List;
> import java.util.Optional;
> import org.springframework.stereotype.Repository;
>
> @Repository
> public class RepositorioLibrosEnMemoria implements RepositorioLibros {
>
>     private final List<Libro> libros = new ArrayList<>();
>
>     public RepositorioLibrosEnMemoria() {
>         libros.add(new Libro(1L, "Cien años de soledad", "Gabriel García Márquez", 1967));
>         libros.add(new Libro(2L, "El amor en los tiempos del cólera", "Gabriel García Márquez", 1985));
>         libros.add(new Libro(3L, "La ciudad y los perros", "Mario Vargas Llosa", 1963));
>         libros.add(new Libro(4L, "Rayuela", "Julio Cortázar", 1963));
>         libros.add(new Libro(5L, "Ficciones", "Jorge Luis Borges", 1944));
>     }
>
>     @Override
>     public List<Libro> listar() {
>         return new ArrayList<>(libros);
>     }
>
>     @Override
>     public Optional<Libro> buscarPorId(Long id) {
>         for (Libro libro : libros) {
>             if (libro.getId().equals(id)) {
>                 return Optional.of(libro);
>             }
>         }
>         return Optional.empty();
>     }
> }
> ```
>
> </details>
>
> ---
>
> ### 📁 negocio — service
>
> <details>
> <summary><strong>📄 ServicioLibros.java</strong></summary>
>
> **Archivo:** [`src/main/java/com/biblioteca/negocio/service/ServicioLibros.java`](codigo/examen-libros/src/main/java/com/biblioteca/negocio/service/ServicioLibros.java)
>
> ```java
> package com.biblioteca.negocio.service;
>
> import com.biblioteca.negocio.model.Libro;
> import com.biblioteca.persistence.repository.RepositorioLibros;
> import java.util.List;
> import java.util.NoSuchElementException;
> import org.springframework.stereotype.Service;
>
> @Service
> public class ServicioLibros {
>
>     private final RepositorioLibros repositorio;
>
>     public ServicioLibros(RepositorioLibros repositorio) {
>         this.repositorio = repositorio;
>     }
>
>     public List<Libro> listar() {
>         return repositorio.listar();
>     }
>
>     public Libro obtener(Long id) {
>         return repositorio.buscarPorId(id)
>                 .orElseThrow(() -> new NoSuchElementException("No existe el libro " + id));
>     }
> }
> ```
>
> </details>
>
> ---
>
> ### 📁 presentacion — controlador
>
> <details>
> <summary><strong>📄 ControladorLibros.java</strong></summary>
>
> **Archivo:** [`src/main/java/com/biblioteca/presentacion/controlador/ControladorLibros.java`](codigo/examen-libros/src/main/java/com/biblioteca/presentacion/controlador/ControladorLibros.java)
>
> ```java
> package com.biblioteca.presentacion.controlador;
>
> import com.biblioteca.negocio.model.Libro;
> import com.biblioteca.negocio.service.ServicioLibros;
> import java.util.List;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PathVariable;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
>
> @RestController
> @RequestMapping("/libros")
> public class ControladorLibros {
>
>     private final ServicioLibros servicio;
>
>     public ControladorLibros(ServicioLibros servicio) {
>         this.servicio = servicio;
>     }
>
>     @GetMapping
>     public List<Libro> listar() {
>         return servicio.listar();
>     }
>
>     @GetMapping("/{id}")
>     public Libro obtener(@PathVariable Long id) {
>         return servicio.obtener(id);
>     }
> }
> ```
>
> </details>

</details>

<details>
<summary><strong>🧪 Resultados reales</strong></summary>

> Con la aplicación en marcha (`./mvnw spring-boot:run`). El JSON se muestra con sangría; la aplicación lo devuelve en una sola línea.
>
> <details>
> <summary><strong>1. Listar todos → <code>200</code></strong></summary>
>
> ```bash
> curl -i http://localhost:8080/libros
> ```
>
> Respuesta:
>
> ```text
> HTTP/1.1 200 
> Content-Type: application/json
>
> [
>   {
>     "id": 1,
>     "titulo": "Cien años de soledad",
>     "autor": "Gabriel García Márquez",
>     "anioPublicacion": 1967
>   },
>   {
>     "id": 2,
>     "titulo": "El amor en los tiempos del cólera",
>     "autor": "Gabriel García Márquez",
>     "anioPublicacion": 1985
>   },
>   {
>     "id": 3,
>     "titulo": "La ciudad y los perros",
>     "autor": "Mario Vargas Llosa",
>     "anioPublicacion": 1963
>   },
>   {
>     "id": 4,
>     "titulo": "Rayuela",
>     "autor": "Julio Cortázar",
>     "anioPublicacion": 1963
>   },
>   {
>     "id": 5,
>     "titulo": "Ficciones",
>     "autor": "Jorge Luis Borges",
>     "anioPublicacion": 1944
>   }
> ]
> ```
>
> </details>
>
> <details>
> <summary><strong>2. Buscar por id → <code>200</code></strong></summary>
>
> ```bash
> curl -i http://localhost:8080/libros/1
> ```
>
> Respuesta:
>
> ```text
> HTTP/1.1 200 
> Content-Type: application/json
>
> {
>   "id": 1,
>   "titulo": "Cien años de soledad",
>   "autor": "Gabriel García Márquez",
>   "anioPublicacion": 1967
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>3. Buscar un id que no existe → <code>500</code></strong></summary>
>
> ```bash
> curl -i http://localhost:8080/libros/99
> ```
>
> Respuesta:
>
> ```text
> HTTP/1.1 500 
> Content-Type: application/json
>
> {
>   "timestamp": "2026-11-01T21:40:00.760+00:00",
>   "status": 500,
>   "error": "Internal Server Error",
>   "path": "/libros/99"
> }
> ```
>
> </details>
>
> <details>
> <summary><strong>4. Un id que no es número → <code>400</code></strong></summary>
>
> ```bash
> curl -i http://localhost:8080/libros/abc
> ```
>
> Respuesta:
>
> ```text
> HTTP/1.1 400 
> Content-Type: application/json
>
> {
>   "timestamp": "2026-11-01T21:40:00.788+00:00",
>   "status": 400,
>   "error": "Bad Request",
>   "path": "/libros/abc"
> }
> ```
>
> </details>
>
>
> > 🔎 **Lectura.** Un `id` inexistente da `500` porque la excepción llega sin traducir hasta Spring; un `id` que no es número da `400` porque Spring no puede convertirlo a `Long`. Traducir el primero a `404` se estudia más adelante.

</details>

<details>
<summary><strong>📊 Pauta de corrección (100 puntos)</strong></summary>

> | Criterio | Puntos | Completo | Parcial | Sin puntos |
> |---|---:|---|---|---|
> | Compila y arranca | 10 | `Started BibliotecaApplication` | Arranca tras corregir un detalle menor | No compila o no arranca |
> | Tres capas | 20 | Las 5 clases en su paquete con el nombre pedido | 1 o 2 clases mal ubicadas o mal nombradas (−5 c/u) | Todo en un solo paquete |
> | Persistencia | 15 | Interfaz + `@Repository` + cinco libros + copia en `listar()` + `Optional` | Falta la copia o usa `null` en vez de `Optional` (−4 c/u) | Sin repositorio o sin interfaz |
> | Negocio | 15 | `@Service`, constructor con la interfaz, `listar()` y `obtener()` con la excepción y el mensaje exactos | Mensaje distinto o excepción genérica (−4) | El servicio no existe o contiene la lista de datos |
> | Presentación | 15 | `@RestController`, `@RequestMapping("/libros")`, dos `@GetMapping`, `@PathVariable` | Falta una anotación (−4 c/u) | Un solo endpoint |
> | Regla de dependencia | 10 | Sin atajos entre capas, sin `@Autowired` | Un atajo o un `@Autowired` (−5) | El controlador accede al repositorio |
> | Evidencia | 10 | Los cuatro comandos con petición, código y cuerpo | Faltan uno o dos (−2,5 c/u) | Sin evidencia |
> | Calidad | 5 | Nombres claros, sin sobrantes | Algún sobrante (−2) | Código confuso |
>
> **Descuentos:** −10 con base de datos o JPA · −10 si el servicio depende de la clase concreta · −5 por endpoint con datos distintos.

</details>

<details>
<summary><strong>🩺 Errores frecuentes y cómo puntuarlos</strong></summary>

> | Qué se ve | Qué significa | Cómo se puntúa |
> |---|---|---|
> | `@Repository` sobre la **interfaz** | Se confunde dónde va el estereotipo | −2 en Persistencia |
> | El servicio declara `RepositorioLibrosEnMemoria` | Rompe la inversión de dependencias | −10 (descuento) y −4 en Negocio |
> | `@Autowired` en un atributo | Inyección por campo, no por constructor | −5 en Regla de dependencia |
> | `listar()` devuelve la lista interna | Cualquiera puede modificar el catálogo | −4 en Persistencia |
> | `buscarPorId` devuelve `null` | Se pierde la intención del `Optional` | −4 en Persistencia |
> | El controlador llama al repositorio | Salta la capa de negocio | 0 en Regla de dependencia |
> | La excepción se lanza en el **controlador** | La regla de negocio está en la capa equivocada | −4 en Negocio y −4 en Presentación |
> | Se usa `stream().filter()` en vez de un `for` | Es válido | Sin descuento |
> | Se devuelve `ResponseEntity` con `404` | Va más allá de lo pedido y es correcto | Sin descuento; se puede dar una felicitación |
> | `404` en todas las rutas | La clase principal está fuera del paquete raíz o falta `@RestController` | Revisar «Tres capas» y «Presentación» |
> | `Whitelabel` o `404` en `/libros` | Falta `@RequestMapping("/libros")` o el paquete no cuelga de `com.biblioteca` | −4 en Presentación |

</details>

<details>
<summary><strong>🗒️ Preguntas orales (opcionales, 10 min)</strong></summary>

> 1. ¿Por qué `ServicioLibros` recibe `RepositorioLibros` y no `RepositorioLibrosEnMemoria`? *(Para poder cambiar la implementación —por ejemplo, a una base de datos— sin tocar el servicio.)*
> 2. ¿Qué pasa si se quita `@Repository` de la implementación? *(Spring no crea el bean y el arranque falla porque el servicio no encuentra qué inyectar.)*
> 3. ¿Por qué `listar()` devuelve una copia? *(Para que quien reciba la lista no pueda modificar los datos del repositorio.)*
> 4. ¿Qué capa decide que un libro «no existe» y por qué? *(Negocio: es una regla; el repositorio solo informa con un `Optional` vacío.)*
> 5. ¿Cómo se cambiaría el catálogo para agregar un libro sin tocar el controlador? *(Solo en el repositorio, y un método nuevo en el servicio si hiciera falta.)*

</details>

⬅️ **Enunciado:** [04-examen-crud-libros.md](04-examen-crud-libros.md)
