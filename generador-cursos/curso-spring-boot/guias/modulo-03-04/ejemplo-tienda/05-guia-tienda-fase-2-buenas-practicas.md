# 🛒 Guía 4 — Tienda en línea · Fase II: buenas prácticas (modelo, entidad y mapeador)

> 🎯 **Qué vas a hacer:** mejorar la tienda terminada en las guías 1 a 3 separando el **modelo** del negocio de las **entidades** de la base de datos, con **mapeadores** y repositorios que **delegan** en Spring Data. La API **no cambia**: mismos endpoints, mismas respuestas.
> ⏱️ **Tiempo:** 45 a 60 minutos. 🧱 **Parte de:** el proyecto de las [guías 1, 2 y 3](04-guia-tienda-3-muchos-a-muchos.md).
> 📚 **Es el mismo diseño** de la [guía del taller «Gestión de cursos» con base de datos](01-guia-taller-gestion-de-cursos-con-bd.md), aplicado a la tienda.

<details open>
<summary><strong>🤔 ¿Por qué una Fase II? El problema de la Fase I</strong></summary>

> En las guías 1 a 3, **la misma clase** es a la vez entidad de la base y modelo del negocio (`Producto` lleva `@Entity`, `@Id`…). Funciona, pero deja varios síntomas:
>
> | Síntoma en la Fase I | Dónde se ve |
> |---|---|
> | El **negocio conoce JPA**: `negocio.model` importa `jakarta.persistence`. | Las cuatro entidades |
> | Anotaciones de **JSON dentro de la entidad** (`@JsonIgnore`) para no entrar en bucles. | `Pedido.detalles`, `DetallePedido.pedido`, `Cliente.favoritos` |
> | El servicio usa **`@Transactional`** (un detalle de la base) para leer colecciones *lazy*. | `ServicioPedidos`, `ServicioClientes` |
> | `@Data` obliga a **excluir** relaciones de `toString`/`equals`/`hashCode`. | `@ToString.Exclude`, `@EqualsAndHashCode.Exclude` |
> | Cambiar la tecnología de persistencia obliga a tocar **el modelo y los servicios**. | Todo `negocio` |
>
> La solución es un patrón clásico: el **repositorio como adaptador**. El servicio habla con un **contrato** (una interfaz) en términos del **modelo**; una clase `…EnBD` cumple ese contrato **delegando** en Spring Data y **traduciendo** con un mapeador.
>
> ```text
> Servicio ─▶ RepositorioProductos (contrato, habla en modelos)
>                   ▲
>                   │ cumple
>         RepositorioProductosEnBD ─▶ ProductoMapper ─▶ ProductoEntity
>                   │                  (modelo ↔ entidad)
>                   └─▶ ProductoJpaRepository (Spring Data) ─▶ Hibernate ─▶ MySQL
> ```
>
> <details>
> <summary><strong>✅ ¿Es correcto que <code>RepositorioProductosEnBD</code> delegue en <code>ProductoJpaRepository</code> y use un mapeador?</strong></summary>
>
> > **Sí.** Es el patrón *Repository* implementado como **puerto y adaptador**: el contrato pertenece al negocio y el adaptador (`…EnBD`) pertenece a la persistencia. Tiene tres matices que esta guía corrige respecto de la [guía de «Gestión de cursos»](01-guia-taller-gestion-de-cursos-con-bd.md):
> >
> > | Matiz | Cómo lo resuelve la Fase II |
> > |---|---|
> > | `guardar` **modificaba** el objeto recibido (`estudiante.setId(…)`). | Devuelve un **modelo nuevo** construido desde lo que guardó la base; el argumento no se toca. |
> > | `aEntity` **no copiaba el `id`**: servía solo para insertar, no para **actualizar**. | `aEntity` copia el `id`: `null` inserta; con valor, actualiza esa fila. El servicio pone `setId(null)` al **crear** para ignorar el `id` del cliente. |
> > | Mapear colecciones *lazy* fuera de una transacción falla (`LazyInitializationException`). | Los métodos del adaptador que recorren colecciones llevan `@Transactional`; **el servicio ya no lo necesita**. |
> >
> > **Costo:** más clases (por entidad: modelo, entidad, mapeador, contrato, interfaz de Spring Data e implementación) y atributos duplicados. En proyectos pequeños, la Fase I es válida; la Fase II compensa cuando el negocio crece o hay que cambiar de tecnología.
>
> </details>

</details>

<details open>
<summary><strong>📦 Dependencias</strong></summary>

> **Ninguna nueva.** Seguís con Spring Web, Spring Data JPA, MySQL Driver y Lombok. Tampoco cambian `application.properties`, la base ni los `curl`.

</details>

<details open>
<summary><strong>🗺️ Diagramas de clases</strong></summary>

> ### 1️⃣ Una pieza completa: «Producto», desde el contrato
>
> ```mermaid
> classDiagram
>     direction TB
>
>     class RepositorioProductos {
>         <<interface>>
>     }
>     class RepositorioProductosEnBD {
>         <<Repository>>
>     }
>     class ProductoJpaRepository {
>         <<interface>>
>     }
>     class JpaRepository {
>         <<interface · Spring Data>>
>     }
>     class ProductoMapper
>     class Producto {
>         <<model>>
>     }
>     class ProductoEntity {
>         <<Entity>>
>     }
>
>     RepositorioProductos <|.. RepositorioProductosEnBD : cumple el contrato
>     RepositorioProductos ..> Producto : recibe y devuelve
>     RepositorioProductosEnBD --> ProductoJpaRepository : delega
>     RepositorioProductosEnBD ..> ProductoMapper : traduce con
>     JpaRepository <|-- ProductoJpaRepository : extiende
>     ProductoJpaRepository ..> ProductoEntity : gestiona
>     ProductoMapper ..> Producto
>     ProductoMapper ..> ProductoEntity
> ```
>
> Arriba, el **contrato** que usa el servicio (habla en **modelos**); abajo, la **base** (habla en **entidades**). `RepositorioProductosEnBD` es el puente.
>
> ### 2️⃣ Modelo y entidad de toda la tienda
>
> ```mermaid
> classDiagram
>     direction TB
>
>     class Producto {
>         <<model>>
>     }
>     class Pedido {
>         <<model>>
>     }
>     class DetallePedido {
>         <<model>>
>     }
>     class Cliente {
>         <<model>>
>     }
>     class ProductoEntity {
>         <<Entity>>
>     }
>     class PedidoEntity {
>         <<Entity>>
>     }
>     class DetallePedidoEntity {
>         <<Entity>>
>     }
>     class ClienteEntity {
>         <<Entity>>
>     }
>
>     DetallePedido --> Producto : producto
>     PedidoEntity "1" -- "*" DetallePedidoEntity : detalles / pedido
>     DetallePedidoEntity "*" --> "1" ProductoEntity : producto
>     ClienteEntity "*" --> "*" ProductoEntity : favoritos
>
>     Producto ..> ProductoEntity : ProductoMapper
>     Pedido ..> PedidoEntity : PedidoMapper
>     DetallePedido ..> DetallePedidoEntity : DetallePedidoMapper
>     Cliente ..> ClienteEntity : ClienteMapper
> ```
>
> - En el **modelo**, un detalle solo conoce su producto; no hay listas ni claves foráneas.
> - En las **entidades** viven las relaciones de JPA (`@OneToMany`, `@ManyToOne`, `@ManyToMany`) y las tablas.
> - Las líneas punteadas son los **mapeadores**, la única pieza que conoce ambos lados.
>
> ### 3️⃣ Todas las clases, por paquete
>
> | Paquete | Clases | Qué cambia respecto de la Fase I |
> |---|---|---|
> | `presentacion.controlador` | `ControladorProductos`, `ControladorPedidos`, `ControladorClientes`, `SolicitudDetalle` | **Nada** |
> | `negocio.model` | `Producto`, `Pedido`, `DetallePedido`, `Cliente` | Pasan a ser clases **simples** (sin JPA, sin `@JsonIgnore`) |
> | `negocio.service` | `ServicioProductos`, `ServicioPedidos`, `ServicioClientes` | Usan los **contratos**; sin `@Transactional` |
> | `persistence.entity` | `ProductoEntity`, `PedidoEntity`, `DetallePedidoEntity`, `ClienteEntity` | ➕ Nuevas (antes eran el modelo) |
> | `persistence.mapper` | `ProductoMapper`, `PedidoMapper`, `DetallePedidoMapper`, `ClienteMapper` | ➕ Nuevas |
> | `persistence.repository` | 3 contratos · 4 `…JpaRepository` · 3 `…EnBD` | ➕ Contratos y `…EnBD`; las interfaces de Spring Data ahora son de **entidades** |

</details>

<details open>
<summary><strong>🪜 Pasos</strong></summary>

> <details open>
> <summary><strong>1. Preparar los paquetes</strong></summary>
>
> > Con la refactorización del IDE (*Rename* y *Move*, que actualizan los `import` solos):
> >
> > 1. Creá `persistence.entity` y `persistence.mapper`.
> > 2. **Renombrá y mové** `Producto`, `Pedido`, `DetallePedido` y `Cliente` de `negocio.model` a `persistence.entity`, con el sufijo `Entity`: `ProductoEntity`, `PedidoEntity`, `DetallePedidoEntity`, `ClienteEntity`.
> > 3. Volvé a crear en `negocio.model` las cuatro clases originales, pero **simples** (paso 2).
> >
> > > 🔎 Hacerlo en este orden conserva las entidades que ya funcionan y evita escribirlas de nuevo.
>
> </details>
>
>
> <details open>
> <summary><strong>2. Producto: de punta a punta</strong></summary>
>
> > Se hace **una** pieza completa; con `Pedido` y `Cliente` solo se repite el patrón.
> >
> > **① El modelo** — una clase simple, sin JPA. `@Data` es seguro aquí: no hay relaciones ni ciclos.
> >
> > **`src/main/java/com/tienda/negocio/model/Producto.java`**
> >
> > ```java
> > package com.tienda.negocio.model;
> >
> > import lombok.AllArgsConstructor;
> > import lombok.Data;
> > import lombok.NoArgsConstructor;
> >
> > @Data
> > @NoArgsConstructor
> > @AllArgsConstructor
> > public class Producto {
> >
> >     private Long id;
> >     private String nombre;
> >     private double precio;
> >     private int stock;
> > }
> > ```
> >
> > **② La entidad** — el mismo `Producto` de la Fase I, ahora en `persistence.entity`. Solo `@Getter`: ya no hace falta `@Data` ni setters.
> >
> > **`src/main/java/com/tienda/persistence/entity/ProductoEntity.java`**
> >
> > ```java
> > package com.tienda.persistence.entity;
> >
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import jakarta.persistence.Table;
> > import lombok.AccessLevel;
> > import lombok.AllArgsConstructor;
> > import lombok.Getter;
> > import lombok.NoArgsConstructor;
> >
> > @Entity
> > @Table(name = "producto")
> > @Getter
> > @NoArgsConstructor(access = AccessLevel.PROTECTED)
> > @AllArgsConstructor
> > public class ProductoEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String nombre;
> >
> >     private double precio;
> >
> >     private int stock;
> > }
> > ```
> >
> > **③ El mapeador** — traduce en ambos sentidos. `aEntity` **copia el `id`**.
> >
> > **`src/main/java/com/tienda/persistence/mapper/ProductoMapper.java`**
> >
> > ```java
> > package com.tienda.persistence.mapper;
> >
> > import com.tienda.negocio.model.Producto;
> > import com.tienda.persistence.entity.ProductoEntity;
> >
> > public final class ProductoMapper {
> >
> >     private ProductoMapper() {
> >     }
> >
> >     // El id se copia: si es null la base inserta; si trae valor, actualiza esa fila
> >     public static ProductoEntity aEntity(Producto producto) {
> >         return new ProductoEntity(producto.getId(), producto.getNombre(), producto.getPrecio(), producto.getStock());
> >     }
> >
> >     public static Producto aModelo(ProductoEntity entity) {
> >         return new Producto(entity.getId(), entity.getNombre(), entity.getPrecio(), entity.getStock());
> >     }
> > }
> > ```
> >
> > **④ El contrato** — habla solo en modelos. Lo que el servicio necesita, nada más.
> >
> > **`src/main/java/com/tienda/persistence/repository/RepositorioProductos.java`**
> >
> > ```java
> > package com.tienda.persistence.repository;
> >
> > import com.tienda.negocio.model.Producto;
> > import java.util.List;
> > import java.util.Optional;
> >
> > public interface RepositorioProductos {
> >
> >     Producto guardar(Producto producto);
> >
> >     List<Producto> listar();
> >
> >     Optional<Producto> buscarPorId(Long id);
> >
> >     void eliminar(Long id);
> > }
> > ```
> >
> > **⑤ Spring Data** — la interfaz de siempre, pero ahora de la **entidad**.
> >
> > **`src/main/java/com/tienda/persistence/repository/ProductoJpaRepository.java`**
> >
> > ```java
> > package com.tienda.persistence.repository;
> >
> > import com.tienda.persistence.entity.ProductoEntity;
> > import org.springframework.data.jpa.repository.JpaRepository;
> >
> > public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {
> > }
> > ```
> >
> > **⑥ El adaptador** — cumple el contrato **delegando** y **traduciendo**.
> >
> > **`src/main/java/com/tienda/persistence/repository/RepositorioProductosEnBD.java`**
> >
> > ```java
> > package com.tienda.persistence.repository;
> >
> > import com.tienda.negocio.model.Producto;
> > import com.tienda.persistence.mapper.ProductoMapper;
> > import java.util.List;
> > import java.util.Optional;
> > import lombok.RequiredArgsConstructor;
> > import org.springframework.stereotype.Repository;
> >
> > @Repository
> > @RequiredArgsConstructor
> > public class RepositorioProductosEnBD implements RepositorioProductos {
> >
> >     private final ProductoJpaRepository jpa;
> >
> >     @Override
> >     public Producto guardar(Producto producto) {
> >         return ProductoMapper.aModelo(jpa.save(ProductoMapper.aEntity(producto)));
> >     }
> >
> >     @Override
> >     public List<Producto> listar() {
> >         return jpa.findAll().stream().map(ProductoMapper::aModelo).toList();
> >     }
> >
> >     @Override
> >     public Optional<Producto> buscarPorId(Long id) {
> >         return jpa.findById(id).map(ProductoMapper::aModelo);
> >     }
> >
> >     @Override
> >     public void eliminar(Long id) {
> >         jpa.deleteById(id);
> >     }
> > }
> > ```
> >
> > - `@Repository` va aquí (la clase que cumple el contrato). Además activa la **traducción de excepciones** de Spring (por ejemplo, una violación de clave única llega como `DataIntegrityViolationException`).
> > - `guardar` **no modifica** el argumento: devuelve un `Producto` nuevo, armado con lo que guardó la base (con su `id`).
> >
> > **⑦ El servicio** — solo conoce modelos y el contrato. Mirá lo que **desaparece**: el `new Producto(...)` para copiar y cualquier cosa de JPA.
> >
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
> >         producto.setId(null); // el id lo decide la base: se ignora el que envíe el cliente
> >         return repositorio.guardar(producto);
> >     }
> >
> >     public List<Producto> listar() {
> >         return repositorio.listar();
> >     }
> >
> >     public Producto obtener(Long id) {
> >         return repositorio.buscarPorId(id)
> >                 .orElseThrow(() -> new NoSuchElementException("No existe el producto " + id));
> >     }
> >
> >     public Producto actualizar(Long id, Producto datos) {
> >         Producto producto = obtener(id);
> >         producto.setNombre(datos.getNombre());
> >         producto.setPrecio(datos.getPrecio());
> >         producto.setStock(datos.getStock());
> >         return repositorio.guardar(producto); // trae id: la base actualiza esa fila
> >     }
> >
> >     public void eliminar(Long id) {
> >         obtener(id);
> >         repositorio.eliminar(id);
> >     }
> > }
> > ```
> >
> > > ⚠️ **El `id` del cliente.** En la Fase I se copiaba el producto para ignorar un `id` enviado. Ahora la regla es **explícita** en `crear`: `producto.setId(null)`. En `actualizar`, en cambio, el producto **trae** su `id` y por eso `guardar` hace un `UPDATE` de esa fila.
> > >
> > > 🗣️ **Pregunta:** ¿por qué `aEntity` copia el `id` si el servicio ya puso `null`? *(Porque el mismo mapeador sirve para **insertar** —con `null`— y para **actualizar** —con un valor—.)*
>
> </details>
>
>
> <details open>
> <summary><strong>3. Pedido y DetallePedido: relaciones</strong></summary>
>
> > El patrón es el mismo. Lo nuevo: las **relaciones** viven solo en las entidades.
> >
> > **Modelos** (sin listas ni claves foráneas):
> >
> > **`src/main/java/com/tienda/negocio/model/Pedido.java`**
> >
> > ```java
> > package com.tienda.negocio.model;
> >
> > import java.time.LocalDate;
> > import lombok.AllArgsConstructor;
> > import lombok.Data;
> > import lombok.NoArgsConstructor;
> >
> > @Data
> > @NoArgsConstructor
> > @AllArgsConstructor
> > public class Pedido {
> >
> >     private Long id;
> >     private LocalDate fecha;
> >     private String estado;
> > }
> > ```
> >
> > **`src/main/java/com/tienda/negocio/model/DetallePedido.java`**
> >
> > ```java
> > package com.tienda.negocio.model;
> >
> > import lombok.AllArgsConstructor;
> > import lombok.Data;
> > import lombok.NoArgsConstructor;
> >
> > @Data
> > @NoArgsConstructor
> > @AllArgsConstructor
> > public class DetallePedido {
> >
> >     private Long id;
> >     private int cantidad;
> >     private double precioUnitario;
> >     private Producto producto;
> > }
> > ```
> >
> > **Entidades** — con `@OneToMany`, `@ManyToOne` y `@JoinColumn`, **sin** `@JsonIgnore`: nadie serializa entidades.
> >
> > **`src/main/java/com/tienda/persistence/entity/PedidoEntity.java`**
> >
> > ```java
> > package com.tienda.persistence.entity;
> >
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import jakarta.persistence.OneToMany;
> > import jakarta.persistence.Table;
> > import java.time.LocalDate;
> > import java.util.ArrayList;
> > import java.util.List;
> > import lombok.AccessLevel;
> > import lombok.Getter;
> > import lombok.NoArgsConstructor;
> >
> > @Entity
> > @Table(name = "pedido")
> > @Getter
> > @NoArgsConstructor(access = AccessLevel.PROTECTED)
> > public class PedidoEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private LocalDate fecha;
> >
> >     private String estado;
> >
> >     // Lado inverso: no crea columnas; "pedido" es el atributo de DetallePedidoEntity que tiene la clave foránea
> >     @OneToMany(mappedBy = "pedido")
> >     private List<DetallePedidoEntity> detalles = new ArrayList<>();
> >
> >     public PedidoEntity(Long id, LocalDate fecha, String estado) {
> >         this.id = id;
> >         this.fecha = fecha;
> >         this.estado = estado;
> >     }
> > }
> > ```
> >
> > **`src/main/java/com/tienda/persistence/entity/DetallePedidoEntity.java`**
> >
> > ```java
> > package com.tienda.persistence.entity;
> >
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import jakarta.persistence.JoinColumn;
> > import jakarta.persistence.ManyToOne;
> > import jakarta.persistence.Table;
> > import lombok.AccessLevel;
> > import lombok.Getter;
> > import lombok.NoArgsConstructor;
> >
> > @Entity
> > @Table(name = "detalle_pedido")
> > @Getter
> > @NoArgsConstructor(access = AccessLevel.PROTECTED)
> > public class DetallePedidoEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private int cantidad;
> >
> >     private double precioUnitario;
> >
> >     // Lado dueño: aquí vive la clave foránea pedido_id
> >     @ManyToOne
> >     @JoinColumn(name = "pedido_id")
> >     private PedidoEntity pedido;
> >
> >     // Muchos detalles pueden apuntar al mismo producto
> >     @ManyToOne
> >     @JoinColumn(name = "producto_id")
> >     private ProductoEntity producto;
> >
> >     public DetallePedidoEntity(int cantidad, double precioUnitario, PedidoEntity pedido, ProductoEntity producto) {
> >         this.cantidad = cantidad;
> >         this.precioUnitario = precioUnitario;
> >         this.pedido = pedido;
> >         this.producto = producto;
> >     }
> > }
> > ```
> >
> > **Mapeadores:**
> >
> > **`src/main/java/com/tienda/persistence/mapper/PedidoMapper.java`**
> >
> > ```java
> > package com.tienda.persistence.mapper;
> >
> > import com.tienda.negocio.model.Pedido;
> > import com.tienda.persistence.entity.PedidoEntity;
> >
> > public final class PedidoMapper {
> >
> >     private PedidoMapper() {
> >     }
> >
> >     public static PedidoEntity aEntity(Pedido pedido) {
> >         return new PedidoEntity(pedido.getId(), pedido.getFecha(), pedido.getEstado());
> >     }
> >
> >     public static Pedido aModelo(PedidoEntity entity) {
> >         return new Pedido(entity.getId(), entity.getFecha(), entity.getEstado());
> >     }
> > }
> > ```
> >
> > **`src/main/java/com/tienda/persistence/mapper/DetallePedidoMapper.java`**
> >
> > ```java
> > package com.tienda.persistence.mapper;
> >
> > import com.tienda.negocio.model.DetallePedido;
> > import com.tienda.persistence.entity.DetallePedidoEntity;
> >
> > public final class DetallePedidoMapper {
> >
> >     private DetallePedidoMapper() {
> >     }
> >
> >     // Solo entidad → modelo: la entidad se arma en el repositorio, con referencias al pedido y al producto
> >     public static DetallePedido aModelo(DetallePedidoEntity entity) {
> >         return new DetallePedido(entity.getId(), entity.getCantidad(), entity.getPrecioUnitario(),
> >                 ProductoMapper.aModelo(entity.getProducto()));
> >     }
> > }
> > ```
> >
> > **Contrato** — el pedido es la raíz: los detalles se agregan y se consultan a través de él.
> >
> > **`src/main/java/com/tienda/persistence/repository/RepositorioPedidos.java`**
> >
> > ```java
> > package com.tienda.persistence.repository;
> >
> > import com.tienda.negocio.model.DetallePedido;
> > import com.tienda.negocio.model.Pedido;
> > import java.util.List;
> > import java.util.Optional;
> >
> > public interface RepositorioPedidos {
> >
> >     Pedido guardar(Pedido pedido);
> >
> >     Optional<Pedido> buscarPorId(Long id);
> >
> >     DetallePedido agregarDetalle(Long pedidoId, DetallePedido detalle);
> >
> >     List<DetallePedido> listarDetalles(Long pedidoId);
> > }
> > ```
> >
> > **Spring Data** — una interfaz por entidad (`PedidoJpaRepository` y `DetallePedidoJpaRepository`), iguales a la de `Producto` pero de su entidad.
> >
> > **Adaptador:**
> >
> > **`src/main/java/com/tienda/persistence/repository/RepositorioPedidosEnBD.java`**
> >
> > ```java
> > package com.tienda.persistence.repository;
> >
> > import com.tienda.negocio.model.DetallePedido;
> > import com.tienda.negocio.model.Pedido;
> > import com.tienda.persistence.entity.DetallePedidoEntity;
> > import com.tienda.persistence.mapper.DetallePedidoMapper;
> > import com.tienda.persistence.mapper.PedidoMapper;
> > import java.util.List;
> > import java.util.Optional;
> > import lombok.RequiredArgsConstructor;
> > import org.springframework.stereotype.Repository;
> > import org.springframework.transaction.annotation.Transactional;
> >
> > @Repository
> > @RequiredArgsConstructor
> > public class RepositorioPedidosEnBD implements RepositorioPedidos {
> >
> >     private final PedidoJpaRepository jpa;
> >     private final DetallePedidoJpaRepository detallesJpa;
> >     private final ProductoJpaRepository productosJpa;
> >
> >     @Override
> >     public Pedido guardar(Pedido pedido) {
> >         return PedidoMapper.aModelo(jpa.save(PedidoMapper.aEntity(pedido)));
> >     }
> >
> >     @Override
> >     public Optional<Pedido> buscarPorId(Long id) {
> >         return jpa.findById(id).map(PedidoMapper::aModelo);
> >     }
> >
> >     @Override
> >     public DetallePedido agregarDetalle(Long pedidoId, DetallePedido detalle) {
> >         // Solo hacen falta las referencias (las claves foráneas): no se leen las filas completas
> >         DetallePedidoEntity guardado = detallesJpa.save(new DetallePedidoEntity(
> >                 detalle.getCantidad(), detalle.getPrecioUnitario(),
> >                 jpa.getReferenceById(pedidoId),
> >                 productosJpa.getReferenceById(detalle.getProducto().getId())));
> >         return new DetallePedido(guardado.getId(), detalle.getCantidad(), detalle.getPrecioUnitario(), detalle.getProducto());
> >     }
> >
> >     // @Transactional: la lista "detalles" es LAZY y solo se puede leer con la sesión abierta
> >     @Override
> >     @Transactional(readOnly = true)
> >     public List<DetallePedido> listarDetalles(Long pedidoId) {
> >         return jpa.findById(pedidoId)
> >                 .map(pedido -> pedido.getDetalles().stream().map(DetallePedidoMapper::aModelo).toList())
> >                 .orElse(List.of());
> >     }
> > }
> > ```
> >
> > - **`getReferenceById`** entrega una referencia a la fila (su clave foránea) **sin leerla**. Alcanza para escribir `pedido_id` y `producto_id`.
> > - Por eso `agregarDetalle` devuelve un `DetallePedido` armado con **datos que ya se tienen** (`detalle.getProducto()`), no mapeando esas referencias: una referencia fuera de la transacción es un *proxy* que **no se puede leer** (`LazyInitializationException`).
> > - **`@Transactional(readOnly = true)`** en `listarDetalles`: la lista `detalles` es *lazy* y se mapea **dentro** de la transacción. Antes esa anotación estaba en el servicio; ahora vive donde pertenece: en persistencia.
> >
> > **Servicio** — sin `@Transactional`:
> >
> > **`src/main/java/com/tienda/negocio/service/ServicioPedidos.java`**
> >
> > ```java
> > package com.tienda.negocio.service;
> >
> > import com.tienda.negocio.model.DetallePedido;
> > import com.tienda.negocio.model.Pedido;
> > import com.tienda.negocio.model.Producto;
> > import com.tienda.persistence.repository.RepositorioPedidos;
> > import java.time.LocalDate;
> > import java.util.List;
> > import java.util.NoSuchElementException;
> > import lombok.RequiredArgsConstructor;
> > import org.springframework.stereotype.Service;
> >
> > @Service
> > @RequiredArgsConstructor
> > public class ServicioPedidos {
> >
> >     private final RepositorioPedidos repositorio;
> >     private final ServicioProductos servicioProductos;
> >
> >     public Pedido crear() {
> >         return repositorio.guardar(new Pedido(null, LocalDate.now(), "CREADO"));
> >     }
> >
> >     public Pedido obtener(Long id) {
> >         return repositorio.buscarPorId(id)
> >                 .orElseThrow(() -> new NoSuchElementException("No existe el pedido " + id));
> >     }
> >
> >     public DetallePedido agregarDetalle(Long pedidoId, Long productoId, int cantidad) {
> >         obtener(pedidoId);
> >         Producto producto = servicioProductos.obtener(productoId);
> >         // El precio se copia del producto: si mañana cambia, este pedido conserva el precio de hoy
> >         return repositorio.agregarDetalle(pedidoId, new DetallePedido(null, cantidad, producto.getPrecio(), producto));
> >     }
> >
> >     public List<DetallePedido> listarDetalles(Long pedidoId) {
> >         obtener(pedidoId);
> >         return repositorio.listarDetalles(pedidoId);
> >     }
> > }
> > ```
>
> </details>
>
>
> <details open>
> <summary><strong>4. Cliente: muchos a muchos</strong></summary>
>
> > **Modelo y entidad:**
> >
> > **`src/main/java/com/tienda/negocio/model/Cliente.java`**
> >
> > ```java
> > package com.tienda.negocio.model;
> >
> > import lombok.AllArgsConstructor;
> > import lombok.Data;
> > import lombok.NoArgsConstructor;
> >
> > @Data
> > @NoArgsConstructor
> > @AllArgsConstructor
> > public class Cliente {
> >
> >     private Long id;
> >     private String nombre;
> >     private String correo;
> > }
> > ```
> >
> > **`src/main/java/com/tienda/persistence/entity/ClienteEntity.java`**
> >
> > ```java
> > package com.tienda.persistence.entity;
> >
> > import jakarta.persistence.Entity;
> > import jakarta.persistence.GeneratedValue;
> > import jakarta.persistence.GenerationType;
> > import jakarta.persistence.Id;
> > import jakarta.persistence.JoinColumn;
> > import jakarta.persistence.JoinTable;
> > import jakarta.persistence.ManyToMany;
> > import jakarta.persistence.Table;
> > import java.util.HashSet;
> > import java.util.Set;
> > import lombok.AccessLevel;
> > import lombok.Getter;
> > import lombok.NoArgsConstructor;
> >
> > @Entity
> > @Table(name = "cliente")
> > @Getter
> > @NoArgsConstructor(access = AccessLevel.PROTECTED)
> > public class ClienteEntity {
> >
> >     @Id
> >     @GeneratedValue(strategy = GenerationType.IDENTITY)
> >     private Long id;
> >
> >     private String nombre;
> >
> >     private String correo;
> >
> >     // Lado dueño: JPA crea la tabla intermedia cliente_favorito con dos claves foráneas
> >     @ManyToMany
> >     @JoinTable(name = "cliente_favorito",
> >             joinColumns = @JoinColumn(name = "cliente_id"),
> >             inverseJoinColumns = @JoinColumn(name = "producto_id"))
> >     private Set<ProductoEntity> favoritos = new HashSet<>();
> >
> >     public ClienteEntity(Long id, String nombre, String correo) {
> >         this.id = id;
> >         this.nombre = nombre;
> >         this.correo = correo;
> >     }
> > }
> > ```
> >
> > **Mapeador:**
> >
> > **`src/main/java/com/tienda/persistence/mapper/ClienteMapper.java`**
> >
> > ```java
> > package com.tienda.persistence.mapper;
> >
> > import com.tienda.negocio.model.Cliente;
> > import com.tienda.persistence.entity.ClienteEntity;
> >
> > public final class ClienteMapper {
> >
> >     private ClienteMapper() {
> >     }
> >
> >     public static ClienteEntity aEntity(Cliente cliente) {
> >         return new ClienteEntity(cliente.getId(), cliente.getNombre(), cliente.getCorreo());
> >     }
> >
> >     public static Cliente aModelo(ClienteEntity entity) {
> >         return new Cliente(entity.getId(), entity.getNombre(), entity.getCorreo());
> >     }
> > }
> > ```
> >
> > **Contrato** — las operaciones de favoritos hablan en ids y en `Producto`:
> >
> > **`src/main/java/com/tienda/persistence/repository/RepositorioClientes.java`**
> >
> > ```java
> > package com.tienda.persistence.repository;
> >
> > import com.tienda.negocio.model.Cliente;
> > import com.tienda.negocio.model.Producto;
> > import java.util.List;
> > import java.util.Optional;
> >
> > public interface RepositorioClientes {
> >
> >     Cliente guardar(Cliente cliente);
> >
> >     Optional<Cliente> buscarPorId(Long id);
> >
> >     void agregarFavorito(Long clienteId, Long productoId);
> >
> >     List<Producto> listarFavoritos(Long clienteId);
> >
> >     void quitarFavorito(Long clienteId, Long productoId);
> > }
> > ```
> >
> > **Adaptador** (con `ClienteJpaRepository`, igual a las otras interfaces de Spring Data):
> >
> > **`src/main/java/com/tienda/persistence/repository/RepositorioClientesEnBD.java`**
> >
> > ```java
> > package com.tienda.persistence.repository;
> >
> > import com.tienda.negocio.model.Cliente;
> > import com.tienda.negocio.model.Producto;
> > import com.tienda.persistence.entity.ClienteEntity;
> > import com.tienda.persistence.mapper.ClienteMapper;
> > import com.tienda.persistence.mapper.ProductoMapper;
> > import java.util.List;
> > import java.util.Optional;
> > import lombok.RequiredArgsConstructor;
> > import org.springframework.stereotype.Repository;
> > import org.springframework.transaction.annotation.Transactional;
> >
> > @Repository
> > @RequiredArgsConstructor
> > public class RepositorioClientesEnBD implements RepositorioClientes {
> >
> >     private final ClienteJpaRepository jpa;
> >     private final ProductoJpaRepository productosJpa;
> >
> >     @Override
> >     public Cliente guardar(Cliente cliente) {
> >         return ClienteMapper.aModelo(jpa.save(ClienteMapper.aEntity(cliente)));
> >     }
> >
> >     @Override
> >     public Optional<Cliente> buscarPorId(Long id) {
> >         return jpa.findById(id).map(ClienteMapper::aModelo);
> >     }
> >
> >     // Al terminar el método, JPA detecta el cambio en la colección y inserta la fila en cliente_favorito
> >     @Override
> >     @Transactional
> >     public void agregarFavorito(Long clienteId, Long productoId) {
> >         ClienteEntity cliente = jpa.findById(clienteId).orElseThrow();
> >         cliente.getFavoritos().add(productosJpa.findById(productoId).orElseThrow());
> >     }
> >
> >     @Override
> >     @Transactional(readOnly = true)
> >     public List<Producto> listarFavoritos(Long clienteId) {
> >         return jpa.findById(clienteId).orElseThrow()
> >                 .getFavoritos().stream().map(ProductoMapper::aModelo).toList();
> >     }
> >
> >     @Override
> >     @Transactional
> >     public void quitarFavorito(Long clienteId, Long productoId) {
> >         ClienteEntity cliente = jpa.findById(clienteId).orElseThrow();
> >         cliente.getFavoritos().remove(productosJpa.findById(productoId).orElseThrow());
> >     }
> > }
> > ```
> >
> > - `agregarFavorito` y `quitarFavorito` son `@Transactional`: al terminar, JPA detecta el cambio en `favoritos` y escribe en `cliente_favorito`.
> > - Se busca el producto con `findById` **dentro** de la misma transacción: así es la **misma instancia** que ya está en la colección y el `Set` no duplica favoritos.
> >
> > **Servicio:**
> >
> > **`src/main/java/com/tienda/negocio/service/ServicioClientes.java`**
> >
> > ```java
> > package com.tienda.negocio.service;
> >
> > import com.tienda.negocio.model.Cliente;
> > import com.tienda.negocio.model.Producto;
> > import com.tienda.persistence.repository.RepositorioClientes;
> > import java.util.List;
> > import java.util.NoSuchElementException;
> > import lombok.RequiredArgsConstructor;
> > import org.springframework.stereotype.Service;
> >
> > @Service
> > @RequiredArgsConstructor
> > public class ServicioClientes {
> >
> >     private final RepositorioClientes repositorio;
> >     private final ServicioProductos servicioProductos;
> >
> >     public Cliente crear(Cliente cliente) {
> >         cliente.setId(null);
> >         return repositorio.guardar(cliente);
> >     }
> >
> >     public Cliente obtener(Long id) {
> >         return repositorio.buscarPorId(id)
> >                 .orElseThrow(() -> new NoSuchElementException("No existe el cliente " + id));
> >     }
> >
> >     public List<Producto> agregarFavorito(Long clienteId, Long productoId) {
> >         obtener(clienteId);
> >         servicioProductos.obtener(productoId);
> >         repositorio.agregarFavorito(clienteId, productoId);
> >         return repositorio.listarFavoritos(clienteId);
> >     }
> >
> >     public List<Producto> listarFavoritos(Long clienteId) {
> >         obtener(clienteId);
> >         return repositorio.listarFavoritos(clienteId);
> >     }
> >
> >     public void quitarFavorito(Long clienteId, Long productoId) {
> >         obtener(clienteId);
> >         servicioProductos.obtener(productoId);
> >         repositorio.quitarFavorito(clienteId, productoId);
> >     }
> > }
> > ```
>
> </details>
>
>
> <details open>
> <summary><strong>5. Los controladores no se tocan</strong></summary>
>
> > `ControladorProductos`, `ControladorPedidos`, `ControladorClientes` y `SolicitudDetalle` quedan **idénticos**. Compilan igual porque ahora reciben y devuelven **modelos** con los mismos campos que antes.
> >
> > > 🗣️ **Pregunta:** ¿qué `import` desaparece de los servicios y del modelo? *(Todo `jakarta.persistence`, `@JsonIgnore` y `@Transactional`: `negocio` ya no sabe que existe una base.)*
>
> </details>

</details>

<details open>
<summary><strong>🧪 Probar: todo debe dar lo mismo que en las guías 1 a 3</strong></summary>

> Con MySQL en marcha (ver la [guía 1](02-guia-tienda-1-crud-productos.md)), **ejecutá los mismos `curl`** de las guías 1, 2 y 3 y compará con la columna «Esperado». Debe dar **idéntico**: mismos códigos y el mismo JSON.
>
> | Guía | Qué repetir | Debe dar |
> |:-:|---|---|
> | 1 | Los 7 `curl` del CRUD | `201 201 200 200 200 204 500` |
> | 2 | Los 12 `curl` de pedidos | 3 detalles; el detalle 1 conserva `precioUnitario` 2500000 tras subir el precio |
> | 3 | Los 16 `curl` de favoritos | Ana con 3 y luego 2; Carlos con 2; repetir un favorito no duplica |
>
> **Tres comprobaciones nuevas de la Fase II:**
>
> ```bash
> # 1. El id enviado por el cliente se ignora  →  201 con un id NUEVO; el producto 1 no cambia
> curl -i -X POST localhost:8080/productos -H "Content-Type: application/json" \
>   -d '{"id":1,"nombre":"Intruso","precio":1,"stock":1}'
> curl -i localhost:8080/productos/1
>
> # 2. El PUT actualiza la fila (no inserta otra)  →  200
> curl -i -X PUT localhost:8080/productos/1 -H "Content-Type: application/json" \
>   -d '{"nombre":"Portátil gamer","precio":3200000,"stock":8}'
>
> # 3. Contá las filas: el PUT no creó una nueva
> docker exec mysql-tienda mysql -uroot -proot --default-character-set=utf8mb4 tienda -e "SELECT id, nombre FROM producto;"
> ```
>
> > 🔎 El SQL de la consola es el mismo de la Fase I; lo que cambió es **quién** lo provoca: ahora el adaptador, no el servicio.

</details>

<details open>
<summary><strong>✅ Qué comprobaste</strong></summary>

> - El **negocio no conoce la base**: `negocio` no importa `jakarta.persistence`, ni `@JsonIgnore`, ni `@Transactional`.
> - Un **mapeador** traduce entre modelo y entidad, y el adaptador `…EnBD` **delega** en Spring Data.
> - Cambiar la tecnología de persistencia (por ejemplo, a MongoDB) solo obligaría a reescribir `persistence`: **contratos, modelo y servicios quedan igual**.
> - **Buenas prácticas** aplicadas: no mutar el argumento, mapeador que copia el `id`, transacciones en el adaptador, referencias con `getReferenceById`, y `@Data` solo en clases sin relaciones.
>
> | | Fase I (guías 1–3) | Fase II (esta guía) |
> |---|---|---|
> | Clases por concepto | 1 (entidad = modelo) | 6 (modelo, entidad, mapeador, contrato, Spring Data, adaptador) |
> | JPA en `negocio` | Sí | **No** |
> | `@Transactional` en servicios | Sí | **No** (en el adaptador) |
> | `@JsonIgnore` en entidades | Sí | **No** |
> | Ideal para | Aprender JPA; proyectos pequeños | Proyectos que crecen; cambiar de tecnología |

</details>

<details open>
<summary><strong>🚀 Retos</strong></summary>

> 1. **Probá el mapeador:** escribí una prueba unitaria de `ProductoMapper` (ida y vuelta: `aEntity(aModelo(entity))` conserva `id`, `nombre`, `precio` y `stock`). No necesita Spring ni base de datos.
> 2. **Automatizá el mapeo:** reemplazá los mapeadores manuales por **MapStruct** y compará cuánto código desaparece.
> 3. **Otra tecnología:** creá `RepositorioProductosEnMemoria` (con una `List`) que cumpla el mismo contrato y dejá solo una anotada con `@Repository`. ¿Cuántos archivos fuera de `persistence` cambian? *(Ninguno.)*
> 4. **Un caso nuevo:** agregá `GET /productos/agotados` (stock `0`): ¿qué piezas tocás y en qué orden? *(Contrato → `ProductoJpaRepository` con `findByStock` → adaptador → servicio → controlador.)*
>
> > 📚 **Para profundizar:** [Anotaciones en JPA](../../03-introduccion-a-jpa/material/ejemplos/08-anotaciones-en-jpa.md) · [CRUD completo](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/06-crud-completo.md).

</details>

⬅️ [Guía 3](04-guia-tienda-3-muchos-a-muchos.md)
