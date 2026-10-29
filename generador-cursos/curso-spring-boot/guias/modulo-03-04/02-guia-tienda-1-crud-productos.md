# 🛒 Guía 1 — Tienda en línea: CRUD de productos

> 🎯 **Qué vas a hacer:** crear tu primera **entidad JPA** (`Producto`) y exponer un **CRUD completo** con cinco endpoints.
> ⏱️ **Tiempo:** unos 30 minutos. 🧱 **Serie:** esta guía es la 1 de 3; la tienda **crece** en las guías 2 y 3.

<details open>
<summary><strong>📦 Dependencias</strong></summary>

> El proyecto se crea en [start.spring.io](https://start.spring.io) con **Maven · Java 21 · Spring Boot 3.x · Group `com.tienda` · Artifact `tienda`** y estas **cuatro** dependencias:
>
> | Dependencia | Para qué sirve en esta guía |
> |---|---|
> | **Spring Web** | Los endpoints REST (`@RestController`) y la conversión a JSON. |
> | **Spring Data JPA** | JPA + Hibernate + `JpaRepository`: guardar y consultar sin escribir SQL. |
> | **MySQL Driver** | El **driver JDBC** (`mysql-connector-j`): permite que la aplicación hable con MySQL. Solo se necesita al ejecutar (`runtime`). |
> | **Lombok** | Genera **getters, setters y constructores** al compilar: menos código repetitivo. Solo se necesita al compilar (`optional`). |
>
> En el `pom.xml` quedan estas dos, además de las de Web y JPA:
>
> ```xml
> <dependency>
>     <groupId>com.mysql</groupId>
>     <artifactId>mysql-connector-j</artifactId>
>     <scope>runtime</scope>
> </dependency>
> <dependency>
>     <groupId>org.projectlombok</groupId>
>     <artifactId>lombok</artifactId>
>     <optional>true</optional>
> </dependency>
> ```
>
> > 💡 Las dos siguientes guías **no agregan ninguna dependencia nueva**: todo lo que necesitan ya está aquí.
> >
> > 🔧 **Lombok en el IDE:** en IntelliJ instalá el plugin *Lombok* y activá *Enable annotation processing*; en VS Code, el *Extension Pack for Java* lo soporta. Si el IDE marca error en `getNombre()` pero Maven compila, es el IDE.
> > Con **JDK 23 o superior**, además, Maven necesita `<proc>full</proc>` en la configuración de `maven-compiler-plugin`.

</details>

<details open>
<summary><strong>🐬 Preparar MySQL</strong></summary>

> Necesitás un servidor MySQL en `localhost:3306`. La forma más rápida es **Docker** (una sola línea):
>
> ```bash
> docker run --name mysql-tienda -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=tienda -p 3306:3306 -d mysql:8
> ```
>
> | Parte | Qué hace |
> |---|---|
> | `--name mysql-tienda` | Nombre del contenedor (para detenerlo y verlo después). |
> | `-e MYSQL_ROOT_PASSWORD=root` | Contraseña del usuario `root` (**solo para desarrollo**). |
> | `-e MYSQL_DATABASE=tienda` | Crea la base `tienda` al iniciar. |
> | `-p 3306:3306` | Expone el puerto de MySQL en tu equipo. |
>
> Esperá unos segundos a que arranque y comprobalo:
>
> ```bash
> docker exec mysql-tienda mysql -uroot -proot --default-character-set=utf8mb4 -e "SHOW DATABASES;"
> ```
>
> Dejalo corriendo durante las tres guías. Con `docker stop mysql-tienda` se detiene y con `docker start mysql-tienda` se retoma **con los datos** (para borrarlo todo: `docker rm -f mysql-tienda`).
>
> > 💡 Si ya tenés MySQL instalado, no necesitás Docker: la URL de la guía crea la base `tienda` si no existe (con un usuario que pueda crear bases). Ajustá usuario y contraseña en `application.properties`.

</details>

<details open>
<summary><strong>🗺️ Diagrama de clases (incremento 1)</strong></summary>

> ```mermaid
> classDiagram
>     direction TB
>     class TiendaApplication {
>         <<SpringBootApplication>>
>     }
>     class ControladorProductos {
>         <<RestController>>
>     }
>     class ServicioProductos {
>         <<Service>>
>     }
>     class RepositorioProductos {
>         <<interface>>
>     }
>     class JpaRepository {
>         <<interface · Spring Data>>
>     }
>     class Producto {
>         <<Entity>>
>     }
>
>     ControladorProductos --> ServicioProductos : usa
>     ServicioProductos --> RepositorioProductos : usa
>     JpaRepository <|-- RepositorioProductos : extiende
>     RepositorioProductos ..> Producto : gestiona
> ```
>
> Están **todas** las clases de la guía (`TiendaApplication` la genera Initializr; el resto lo escribís en los pasos). El flujo se lee de arriba hacia abajo: *controlador → servicio → repositorio → entidad*. Las entidades son las tablas: `Producto` → `producto` (`id`, `nombre`, `precio`, `stock`).

</details>

<details open>
<summary><strong>🌳 Estructura del proyecto</strong></summary>

> ```text
> 📁 tienda
> ├── 📁 src/main
> │   ├── 📁 java/com/tienda
> │   │   ├── 📁 presentacion                        (capa de presentación)
> │   │   │   └── 📁 controlador
> │   │   │       └── 📄 ControladorProductos.java   ← NUEVO
> │   │   ├── 📁 negocio                             (capa de negocio)
> │   │   │   ├── 📁 model
> │   │   │   │   └── 📄 Producto.java               ← NUEVO
> │   │   │   └── 📁 service
> │   │   │       └── 📄 ServicioProductos.java      ← NUEVO
> │   │   ├── 📁 persistence                         (capa de persistencia)
> │   │   │   └── 📁 repository
> │   │   │       └── 📄 RepositorioProductos.java   ← NUEVO
> │   │   └── 📄 TiendaApplication.java              ← generado por Initializr
> │   └── 📁 resources
> │       └── 📄 application.properties              ← NUEVO
> └── 📄 pom.xml                                     ← generado por Initializr
> ```
>
> Los archivos marcados «generado por Initializr» no se escriben: los crea [start.spring.io](https://start.spring.io).

</details>

<details open>
<summary><strong>🪜 Pasos</strong></summary>

> <details open>
> <summary><strong>1. Configurar la base de datos</strong></summary>
>
> > **`src/main/resources/application.properties`**
> >
> > ```properties
> > spring.datasource.url=jdbc:mysql://localhost:3306/tienda?createDatabaseIfNotExist=true
> > spring.datasource.username=root
> > spring.datasource.password=root
> > spring.jpa.hibernate.ddl-auto=update
> > spring.jpa.show-sql=true
> > ```
> >
> > - `spring.datasource.url`: dónde está la base: `jdbc:mysql://` + servidor (`localhost`) + puerto (`3306`) + base (`tienda`). `createDatabaseIfNotExist=true` crea la base si todavía no existe.
> > - `username` y `password`: los mismos del `docker run` (`root` / `root`). **No subas credenciales reales a un repositorio.**
> > - `ddl-auto=update`: Hibernate **crea las tablas** a partir de las entidades, sin borrar datos existentes.
> > - `show-sql=true`: ves en consola el SQL que genera.
> > - El driver y el dialecto de MySQL **se detectan solos**: no hace falta configurarlos.
>
> </details>
>
> <details open>
> <summary><strong>2. La entidad</strong></summary>
>
> > **`src/main/java/com/tienda/negocio/model/Producto.java`**
> >
> > ```java
> > package com.tienda.negocio.model;
> >
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import lombok.AccessLevel;
> > import lombok.Data;
> > import lombok.NoArgsConstructor;
> > import lombok.Setter;
> >
> > @Entity
> > @Data
> > @NoArgsConstructor(access = AccessLevel.PROTECTED)
> > public class Producto {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     @Setter(AccessLevel.NONE)
> >     private Long id;
> >
> >     private String nombre;
> >
> >     private double precio;
> >
> >     private int stock;
> >
> >     public Producto(String nombre, double precio, int stock) {
> >         this.nombre = nombre;
> >         this.precio = precio;
> >         this.stock = stock;
> >     }
> > }
> > ```
> >
> > - **`@Entity`**: la clase se guarda en una tabla. **`@Id`** + **`@GeneratedValue(IDENTITY)`**: la clave primaria la genera la base (en MySQL, una columna `auto_increment`).
> > - **Lombok** escribe por vos el código repetitivo:
> >
> > | Anotación | Qué genera |
> > |---|---|
> > | `@Data` | Todo en uno: **getters**, **setters**, `toString()`, `equals()` y `hashCode()` de todos los atributos. |
> > | `@Setter(AccessLevel.NONE)` (sobre `id`) | Le quita a `id` el setter que `@Data` generaría: **el `id` lo decide la base**. |
> > | `@NoArgsConstructor(access = AccessLevel.PROTECTED)` | El constructor vacío `protected` que exige JPA para crear el objeto. |
> >
> > - El constructor con parámetros (`nombre`, `precio`, `stock`) se deja escrito: **sin `id`**, porque todavía no existe.
> >
> > > ⚠️ **`@Data` y las relaciones.** En `Producto` no hay problema: solo tiene datos simples. Pero cuando una entidad tiene **relaciones** (guías 2 y 3), `toString()`, `equals()` y `hashCode()` también recorren la relación, y si es **bidireccional** (pedido ↔ detalle) se llaman entre sí **sin fin** y aparece un `StackOverflowError`. La solución, que vas a aplicar: excluir esos atributos con `@ToString.Exclude` y `@EqualsAndHashCode.Exclude`.
>
> </details>
>
> <details open>
> <summary><strong>3. El repositorio</strong></summary>
>
> > **`src/main/java/com/tienda/persistence/repository/RepositorioProductos.java`**
> >
> > ```java
> > package com.tienda.persistence.repository;
> >
> > import com.tienda.negocio.model.Producto;
> > import org.springframework.data.jpa.repository.JpaRepository;
> >
> > public interface RepositorioProductos extends JpaRepository<Producto, Long> {
> > }
> > ```
> >
> > Sin una sola línea de código, ya tenés `save`, `findAll`, `findById`, `delete`… **Spring Data genera la clase** al arrancar.
>
> </details>
>
> <details open>
> <summary><strong>4. El servicio</strong></summary>
>
> > **`src/main/java/com/tienda/negocio/service/ServicioProductos.java`**
> >
> > ```java
> > package com.tienda.negocio.service;
> >
> > import com.tienda.negocio.model.Producto;
> > import com.tienda.persistence.repository.RepositorioProductos;
> > import java.util.List;
> > import java.util.NoSuchElementException;
> > import lombok.RequiredArgsConstructor;
> > import org.springframework.stereotype.Service;
> >
> > @Service
> > @RequiredArgsConstructor
> > public class ServicioProductos {
> >
> >     private final RepositorioProductos repositorio;
> >
> >     public Producto crear(Producto producto) {
> >         // Se crea uno nuevo: así se ignora cualquier id que envíe el cliente
> >         return repositorio.save(new Producto(producto.getNombre(), producto.getPrecio(), producto.getStock()));
> >     }
> >
> >     public List<Producto> listar() {
> >         return repositorio.findAll();
> >     }
> >
> >     public Producto obtener(Long id) {
> >         return repositorio.findById(id)
> >                 .orElseThrow(() -> new NoSuchElementException("No existe el producto " + id));
> >     }
> >
> >     public Producto actualizar(Long id, Producto datos) {
> >         Producto producto = obtener(id);
> >         producto.setNombre(datos.getNombre());
> >         producto.setPrecio(datos.getPrecio());
> >         producto.setStock(datos.getStock());
> >         return repositorio.save(producto);
> >     }
> >
> >     public void eliminar(Long id) {
> >         repositorio.delete(obtener(id));
> >     }
> > }
> > ```
> >
> > - **`@RequiredArgsConstructor`** genera el constructor con los atributos `final` (`repositorio`). Es la misma **inyección por constructor** de siempre: Spring lo usa para entregarle el repositorio, sin escribirlo.
> >
> > > ⚠️ **Detalle importante:** en `crear` se construye un `Producto` **nuevo**. Si se guardara el que llega, un `POST` con `"id": 1` **actualizaría** el producto 1 en lugar de crear uno.
>
> </details>
>
> <details open>
> <summary><strong>5. El controlador (los cinco endpoints)</strong></summary>
>
> > **`src/main/java/com/tienda/presentacion/controlador/ControladorProductos.java`**
> >
> > ```java
> > package com.tienda.presentacion.controlador;
> >
> > import com.tienda.negocio.model.Producto;
> > import com.tienda.negocio.service.ServicioProductos;
> > import java.util.List;
> > import lombok.RequiredArgsConstructor;
> > import org.springframework.http.HttpStatus;
> > import org.springframework.web.bind.annotation.DeleteMapping;
> > import org.springframework.web.bind.annotation.GetMapping;
> > import org.springframework.web.bind.annotation.PathVariable;
> > import org.springframework.web.bind.annotation.PostMapping;
> > import org.springframework.web.bind.annotation.PutMapping;
> > import org.springframework.web.bind.annotation.RequestBody;
> > import org.springframework.web.bind.annotation.RequestMapping;
> > import org.springframework.web.bind.annotation.ResponseStatus;
> > import org.springframework.web.bind.annotation.RestController;
> >
> > @RestController
> > @RequestMapping("/productos")
> > @RequiredArgsConstructor
> > public class ControladorProductos {
> >
> >     private final ServicioProductos servicio;
> >
> >     @PostMapping
> >     @ResponseStatus(HttpStatus.CREATED)
> >     public Producto crear(@RequestBody Producto producto) {
> >         return servicio.crear(producto);
> >     }
> >
> >     @GetMapping
> >     public List<Producto> listar() {
> >         return servicio.listar();
> >     }
> >
> >     @GetMapping("/{id}")
> >     public Producto obtener(@PathVariable Long id) {
> >         return servicio.obtener(id);
> >     }
> >
> >     @PutMapping("/{id}")
> >     public Producto actualizar(@PathVariable Long id, @RequestBody Producto producto) {
> >         return servicio.actualizar(id, producto);
> >     }
> >
> >     @DeleteMapping("/{id}")
> >     @ResponseStatus(HttpStatus.NO_CONTENT)
> >     public void eliminar(@PathVariable Long id) {
> >         servicio.eliminar(id);
> >     }
> > }
> > ```
>
> </details>

</details>

<details open>
<summary><strong>🧪 Probar con Postman</strong></summary>

> Header en los `POST` y `PUT`: `Content-Type: application/json`.
>
> | # | Operación | Método y URL | Body | Esperado |
> |:-:|---|---|---|:-:|
> | 1 | Crear | `POST /productos` | `{"nombre":"Portátil","precio":2500000,"stock":10}` | `201` |
> | 2 | Crear | `POST /productos` | `{"nombre":"Ratón","precio":45000,"stock":50}` | `201` |
> | 3 | Listar | `GET /productos` | — | `200` con 2 productos |
> | 4 | Consultar | `GET /productos/2` | — | `200` |
> | 5 | Actualizar | `PUT /productos/2` | `{"nombre":"Ratón inalámbrico","precio":55000,"stock":40}` | `200` con los datos nuevos |
> | 6 | Eliminar | `DELETE /productos/2` | — | `204` |
> | 7 | Inexistente | `GET /productos/99` | — | `500` *(la consola muestra `NoSuchElementException`; el `404` llega en el Módulo 7)* |
>
> ### 💻 Los mismos pasos con `curl`
>
> Con la aplicación en marcha (`./mvnw spring-boot:run`), ejecutalos **en este orden** en una consola Bash. `-i` muestra el código de estado.
>
> ```bash
> # 1-2. Crear productos  →  201
> curl -i -X POST localhost:8080/productos -H "Content-Type: application/json" \
>   -d '{"nombre":"Portátil","precio":2500000,"stock":10}'
> curl -i -X POST localhost:8080/productos -H "Content-Type: application/json" \
>   -d '{"nombre":"Ratón","precio":45000,"stock":50}'
>
> # 3. Listar  →  200
> curl -i localhost:8080/productos
>
> # 4. Consultar uno  →  200
> curl -i localhost:8080/productos/2
>
> # 5. Actualizar  →  200
> curl -i -X PUT localhost:8080/productos/2 -H "Content-Type: application/json" \
>   -d '{"nombre":"Ratón inalámbrico","precio":55000,"stock":40}'
>
> # 6. Eliminar  →  204
> curl -i -X DELETE localhost:8080/productos/2
>
> # 7. Producto inexistente  →  500
> curl -i localhost:8080/productos/99
> ```
>
> > 💡 En PowerShell usá `curl.exe` en lugar de `curl`, y el acento grave (`` ` ``) en lugar de `\` para continuar la línea.
>
> Respuesta de la operación 1:
>
> ```json
> {"id":1,"nombre":"Portátil","precio":2500000.0,"stock":10}
> ```

</details>

<details open>
<summary><strong>🔍 Mirá el SQL</strong></summary>

> Cada operación aparece en la consola. Al arrancar, Hibernate crea la tabla (sintaxis de MySQL):
>
> ```text
> create table producto (id bigint not null auto_increment, nombre varchar(255), precio float(53) not null, stock integer not null, primary key (id)) engine=InnoDB
> ```
>
> Y cada operación genera su SQL: el `POST`, `insert into producto (nombre,precio,stock) values (?,?,?)` *(el `id` lo pone MySQL)*; el `PUT`, `update producto set nombre=?,precio=?,stock=? where id=?`; el `DELETE`, `delete from producto where id=?`.
>
> **Mirá los datos directamente en MySQL:**
>
> ```bash
> docker exec mysql-tienda mysql -uroot -proot --default-character-set=utf8mb4 tienda -e "SHOW TABLES; SELECT * FROM producto;"
> ```
>
> Como los datos están en MySQL, **sobreviven al reiniciar la aplicación** (a diferencia de una base en memoria).

</details>

<details open>
<summary><strong>✅ Qué comprobaste</strong></summary>

> - Una **clase Java** (`@Entity`) se convierte en una **tabla**.
> - Una **interfaz** (`JpaRepository`) te da el CRUD sin implementarlo.
> - Las capas: `presentacion` → `negocio` → `persistence`, cada una con su responsabilidad.

</details>

<details open>
<summary><strong>🚀 Reto</strong></summary>

> Agregá `GET /productos/agotados` que devuelva los productos con `stock` igual a `0`. *(Pista: método derivado `findByStock(int stock)` en el repositorio.)*
>
> > 📚 **Para profundizar:** [Uso básico de Spring Data JPA](../../03-introduccion-a-jpa/material/ejemplos/07-uso-basico-de-spring-data-jpa.md) · [CRUD completo](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/06-crud-completo.md).

</details>

➡️ **Siguiente:** [Guía 2 — Relación uno a muchos](03-guia-tienda-2-uno-a-muchos.md)
