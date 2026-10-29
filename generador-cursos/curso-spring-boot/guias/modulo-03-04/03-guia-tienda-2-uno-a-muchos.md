# 🛒 Guía 2 — Tienda en línea: pedidos y detalles (uno a muchos)

> 🎯 **Qué vas a hacer:** relacionar un **pedido** con **varios detalles** (`@OneToMany` / `@ManyToOne`) y cada detalle con un **producto** de la guía 1.
> ⏱️ **Tiempo:** unos 30 minutos. 🧱 **Parte de:** el proyecto de la [Guía 1](02-guia-tienda-1-crud-productos.md).

## 📦 Dependencias

**Ninguna nueva.** Seguís con Spring Web, Spring Data JPA y H2.

## 🗺️ Diagrama de clases (incremento 2)

```mermaid
classDiagram
    class Pedido {
        <<Entity · NUEVA>>
        -Long id
        -LocalDate fecha
        -String estado
    }
    class DetallePedido {
        <<Entity · NUEVA>>
        -Long id
        -int cantidad
        -double precioUnitario
    }
    class Producto {
        <<Entity · existente>>
        -Long id
        -String nombre
        -double precio
        -int stock
    }
    Pedido "1" -- "*" DetallePedido : detalles / pedido
    DetallePedido "*" --> "1" Producto : producto
```

| Relación | Anotaciones | Dónde está la clave foránea |
|---|---|---|
| `Pedido` 1 — N `DetallePedido` | `@OneToMany(mappedBy)` en `Pedido`, `@ManyToOne` + `@JoinColumn` en `DetallePedido` | `detalle_pedido.pedido_id` |
| `DetallePedido` N — 1 `Producto` | `@ManyToOne` + `@JoinColumn` | `detalle_pedido.producto_id` |

Ejemplo: el **pedido #100** tiene tres detalles (2 portátiles, 1 ratón, 3 teclados); un mismo producto puede aparecer en detalles de **muchos** pedidos.

## 🪜 Pasos

### 1. Las entidades

**`src/main/java/com/tienda/negocio/model/Pedido.java`**

```java
package com.tienda.negocio.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    private String estado;

    // Lado inverso: no crea columnas; "pedido" es el atributo de DetallePedido que tiene la clave foránea
    @OneToMany(mappedBy = "pedido")
    @JsonIgnore
    private List<DetallePedido> detalles = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(LocalDate fecha, String estado) {
        this.fecha = fecha;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public String getEstado() { return estado; }
    public List<DetallePedido> getDetalles() { return detalles; }
}
```

**`src/main/java/com/tienda/negocio/model/DetallePedido.java`**

```java
package com.tienda.negocio.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int cantidad;

    private double precioUnitario;

    // Lado dueño: aquí vive la clave foránea pedido_id
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    @JsonIgnore
    private Pedido pedido;

    // Muchos detalles pueden apuntar al mismo producto
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    protected DetallePedido() {
    }

    public DetallePedido(int cantidad, double precioUnitario, Pedido pedido, Producto producto) {
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.pedido = pedido;
        this.producto = producto;
    }

    public Long getId() { return id; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public Producto getProducto() { return producto; }
}
```

- **Lado dueño:** `DetallePedido` tiene la clave foránea (`@JoinColumn(name = "pedido_id")`). En `Pedido`, `mappedBy = "pedido"` solo dice «mirá ese atributo»: **no crea columnas**.
- **`@JsonIgnore`** en `DetallePedido.pedido` y `Pedido.detalles`: sin él, el JSON entraría en un **bucle infinito** (pedido → detalles → pedido…). Es un arreglo temporal; en el Módulo 5 se resuelve con DTO.

### 2. Los repositorios

**`src/main/java/com/tienda/persistence/repository/RepositorioPedidos.java`**

```java
package com.tienda.persistence.repository;

import com.tienda.negocio.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioPedidos extends JpaRepository<Pedido, Long> {
}
```

**`src/main/java/com/tienda/persistence/repository/RepositorioDetallesPedido.java`**

```java
package com.tienda.persistence.repository;

import com.tienda.negocio.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioDetallesPedido extends JpaRepository<DetallePedido, Long> {
}
```

### 3. El servicio

**`src/main/java/com/tienda/negocio/service/ServicioPedidos.java`**

```java
package com.tienda.negocio.service;

import com.tienda.negocio.model.DetallePedido;
import com.tienda.negocio.model.Pedido;
import com.tienda.negocio.model.Producto;
import com.tienda.persistence.repository.RepositorioDetallesPedido;
import com.tienda.persistence.repository.RepositorioPedidos;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioPedidos {

    private final RepositorioPedidos repositorio;
    private final RepositorioDetallesPedido repositorioDetalles;
    private final ServicioProductos servicioProductos;

    public ServicioPedidos(RepositorioPedidos repositorio,
                           RepositorioDetallesPedido repositorioDetalles,
                           ServicioProductos servicioProductos) {
        this.repositorio = repositorio;
        this.repositorioDetalles = repositorioDetalles;
        this.servicioProductos = servicioProductos;
    }

    public Pedido crear() {
        return repositorio.save(new Pedido(LocalDate.now(), "CREADO"));
    }

    public Pedido obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el pedido " + id));
    }

    public DetallePedido agregarDetalle(Long pedidoId, Long productoId, int cantidad) {
        Pedido pedido = obtener(pedidoId);
        Producto producto = servicioProductos.obtener(productoId);
        // El precio se copia del producto: si mañana cambia, este pedido conserva el precio de hoy
        return repositorioDetalles.save(new DetallePedido(cantidad, producto.getPrecio(), pedido, producto));
    }

    // @Transactional: la lista "detalles" es LAZY y solo se puede leer con la sesión abierta
    @Transactional(readOnly = true)
    public List<DetallePedido> listarDetalles(Long pedidoId) {
        return List.copyOf(obtener(pedidoId).getDetalles());
    }
}
```

- `agregarDetalle` **reutiliza** `ServicioProductos` (guía 1) para validar que el producto exista y **copia su precio** al detalle: si el precio del producto cambia después, el pedido conserva el de ese día.
- `listarDetalles` es `@Transactional` porque `detalles` se carga **bajo demanda** (*lazy*): solo se puede leer con la sesión abierta.

### 4. El controlador

**`src/main/java/com/tienda/presentacion/controlador/SolicitudDetalle.java`**

```java
package com.tienda.presentacion.controlador;

public record SolicitudDetalle(Long productoId, int cantidad) {
}
```

**`src/main/java/com/tienda/presentacion/controlador/ControladorPedidos.java`**

```java
package com.tienda.presentacion.controlador;

import com.tienda.negocio.model.DetallePedido;
import com.tienda.negocio.model.Pedido;
import com.tienda.negocio.service.ServicioPedidos;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedidos")
public class ControladorPedidos {

    private final ServicioPedidos servicio;

    public ControladorPedidos(ServicioPedidos servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido crear() {
        return servicio.crear();
    }

    @GetMapping("/{id}")
    public Pedido obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping("/{id}/detalles")
    @ResponseStatus(HttpStatus.CREATED)
    public DetallePedido agregarDetalle(@PathVariable Long id, @RequestBody SolicitudDetalle solicitud) {
        return servicio.agregarDetalle(id, solicitud.productoId(), solicitud.cantidad());
    }

    @GetMapping("/{id}/detalles")
    public List<DetallePedido> listarDetalles(@PathVariable Long id) {
        return servicio.listarDetalles(id);
    }
}
```

## 🧪 Probar con Postman

Antes, creá tres productos con la guía 1 (`Portátil`, `Ratón`, `Teclado`: ids 1, 2 y 3).

| # | Operación | Método y URL | Body | Esperado |
|:-:|---|---|---|:-:|
| 1 | Crear pedido | `POST /pedidos` | — | `201` · `{"id":1,"fecha":"…","estado":"CREADO"}` |
| 2 | Detalle 1 | `POST /pedidos/1/detalles` | `{"productoId":1,"cantidad":2}` | `201` |
| 3 | Detalle 2 | `POST /pedidos/1/detalles` | `{"productoId":2,"cantidad":1}` | `201` |
| 4 | Detalle 3 | `POST /pedidos/1/detalles` | `{"productoId":3,"cantidad":3}` | `201` |
| 5 | **Consultar los detalles** | `GET /pedidos/1/detalles` | — | `200` con **3** detalles |
| 6 | Cambiar precio | `PUT /productos/1` con `precio: 3000000` | — | `200` |
| 7 | Releer detalles | `GET /pedidos/1/detalles` | — | el detalle 1 sigue con `precioUnitario` **2500000**, aunque el producto ya cuesta 3.000.000 |
| 8 | Pedido inexistente | `GET /pedidos/9/detalles` | — | `500` *(`NoSuchElementException`)* |

Un detalle se ve así:

```json
{"id":1,"cantidad":2,"precioUnitario":2500000.0,"producto":{"id":1,"nombre":"Portátil","precio":2500000.0,"stock":10}}
```

## 🔍 Mirá las tablas

```text
create table pedido (id …, estado varchar(255), fecha date, primary key (id))
create table detalle_pedido (id …, cantidad integer not null, precio_unitario float(53) not null, pedido_id bigint, producto_id bigint, primary key (id))
alter table detalle_pedido add constraint … foreign key (pedido_id)   references pedido
alter table detalle_pedido add constraint … foreign key (producto_id) references producto
```

## ✅ Qué comprobaste

- En **uno a muchos**, la clave foránea vive del lado **«muchos»** (`DetallePedido`).
- `@ManyToOne` + `@JoinColumn` define la clave; `@OneToMany(mappedBy = …)` permite navegar en el otro sentido.
- Un detalle apunta a **un** producto, y un producto puede estar en **muchos** detalles.

## 🚀 Reto

Agregá `cascade = CascadeType.ALL` y `orphanRemoval = true` a `Pedido.detalles` y creá `DELETE /pedidos/{id}`. ¿Qué pasa con sus detalles?
*([Cascade y orphanRemoval](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/05-cascade-y-orphanRemoval.md).)*

> 📚 **Para profundizar:** [OneToMany, ManyToOne y fetch](../../04-gestion-de-bases-de-datos-mediante-jpa/material/ejemplos/03-oneToMany-manyToOne-y-fetch.md).

⬅️ [Guía 1](02-guia-tienda-1-crud-productos.md) · ➡️ **Siguiente:** [Guía 3 — Relación muchos a muchos](04-guia-tienda-3-muchos-a-muchos.md)
