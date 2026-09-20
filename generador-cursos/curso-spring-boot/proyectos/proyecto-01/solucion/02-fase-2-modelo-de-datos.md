# 🗄️ Fase 2 — Modelo de datos: entidades, relaciones y datos iniciales (SQL)

**Navegación**: [Índice](README.md) · ← [Fase 1 — Proyecto base](01-fase-1-proyecto-base.md) · Siguiente → [Fase 3a — Catálogos](03a-fase-3-catalogos.md)

## 🎯 Qué vas a lograr

Convertir el modelo de dominio de la [Fase 0](00-analisis.md) en entidades JPA con sus
relaciones, crear los repositorios y cargar los datos iniciales con un **script SQL** al arrancar. Al
terminar verás las tablas creadas por Hibernate y los datos en la base.

**Módulos que se aplican**: 03 (Introducción a JPA) y 04 (Gestión de bases de datos con JPA):
`@Entity`, `@OneToOne`, `@OneToMany`/`@ManyToOne`, `@ManyToMany`, `cascade`,
`orphanRemoval`, `fetch`, entidad intermedia.

> ⚠️ **Las entidades se referencian entre sí** (`Sede` conoce a `Sala`, `Sala` a `Sede`,
> etc.). Tu IDE marcará errores en rojo hasta que hayas creado **todas**. Es normal:
> creá todas las entidades y recién entonces compilá (checkpoint del paso 2.2).

## 🪜 Paso a paso

### Paso 2.1 — Enumeraciones

Cuatro enumeraciones representan los valores cerrados del dominio. Se guardan en la
base como **texto** (`@Enumerated(EnumType.STRING)`, lo verás en cada entidad): con
`ORDINAL` (el número de posición) reordenar o agregar un valor corrompería los datos
existentes.

`EstadoReserva.ACTIVOS` es una constante con los dos estados que ocupan la sala
(`PENDIENTE` y `CONFIRMADA`). Se usará en varias reglas (RN-01, RN-06, RN-12); así
"activa" se define en un solo lugar.

**📄 `src/main/java/com/coworkhub/persistences/entities/TipoSala.java`**

```java
package com.coworkhub.persistences.entities;

public enum TipoSala {
    SALA_REUNION,
    OFICINA_PRIVADA,
    ESCRITORIO
}
```

**📄 `src/main/java/com/coworkhub/persistences/entities/EstadoMiembro.java`**

```java
package com.coworkhub.persistences.entities;

public enum EstadoMiembro {
    ACTIVO,
    SUSPENDIDO
}
```

**📄 `src/main/java/com/coworkhub/persistences/entities/EstadoReserva.java`**

```java
package com.coworkhub.persistences.entities;

import java.util.List;

public enum EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    COMPLETADA,
    CANCELADA;

    // Estados que ocupan la sala y cuentan como "reserva activa".
    public static final List<EstadoReserva> ACTIVOS = List.of(PENDIENTE, CONFIRMADA);
}
```

`Rol` va en el paquete de seguridad porque pertenece al `Usuario`:

**📄 `src/main/java/com/coworkhub/security/persistences/entities/Rol.java`**

```java
package com.coworkhub.security.persistences.entities;

public enum Rol {
    ADMIN,
    RECEPCION,
    MIEMBRO
}
```

### Paso 2.2 — Entidades

Convenciones que se repiten en todas (las verás en cada archivo):

- `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`: la base genera el `id`.
- **Constructor sin argumentos `protected`**: lo exige JPA; `protected` impide que tu
  código cree entidades vacías por error.
- **Constructor público** con los datos obligatorios, y un método `actualizar(...)` que
  cambia varios campos a la vez, en lugar de un `setX` por cada campo. Así el
  servicio no puede dejar una entidad a medias.
- `@Column(nullable = false)` para lo obligatorio y `unique = true` para lo único (RN-11).
- **Dinero con `BigDecimal`** (nunca `double`, que produce errores de redondeo) y
  `precision = 12, scale = 2`.

#### `Sede`

Atributos simples y la relación **inversa** con `Sala`. `@OneToMany(mappedBy = "sede")`
significa "la clave foránea vive en `Sala.sede`, yo solo la reflejo". Es `LAZY` por
defecto y lleva `@JsonIgnore` para no serializarla (evita recursión Sede → Sala → Sede…).

**📄 `src/main/java/com/coworkhub/persistences/entities/Sede.java`**

```java
package com.coworkhub.persistences.entities;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String ciudad;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private LocalTime horaApertura;

    @Column(nullable = false)
    private LocalTime horaCierre;

    // Lado inverso: la clave foránea vive en Sala. LAZY (por defecto) e ignorada en el JSON.
    @OneToMany(mappedBy = "sede")
    @JsonIgnore
    private List<Sala> salas = new ArrayList<>();

    protected Sede() {
    }

    public Sede(String nombre, String ciudad, String direccion, LocalTime horaApertura, LocalTime horaCierre) {
        actualizar(nombre, ciudad, direccion, horaApertura, horaCierre);
    }

    public void actualizar(String nombre, String ciudad, String direccion, LocalTime horaApertura, LocalTime horaCierre) {
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.direccion = direccion;
        this.horaApertura = horaApertura;
        this.horaCierre = horaCierre;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCiudad() { return ciudad; }
    public String getDireccion() { return direccion; }
    public LocalTime getHoraApertura() { return horaApertura; }
    public LocalTime getHoraCierre() { return horaCierre; }
    public List<Sala> getSalas() { return salas; }
}
```

#### `Equipamiento`

Lado **inverso** de la relación N↔N con `Sala` (`mappedBy = "equipamientos"`): la tabla
intermedia la declara `Sala`.

**📄 `src/main/java/com/coworkhub/persistences/entities/Equipamiento.java`**

```java
package com.coworkhub.persistences.entities;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

@Entity
public class Equipamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    // Lado inverso de la relación muchos a muchos: la tabla intermedia la declara Sala.
    @ManyToMany(mappedBy = "equipamientos")
    @JsonIgnore
    private Set<Sala> salas = new HashSet<>();

    protected Equipamiento() {
    }

    public Equipamiento(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Set<Sala> getSalas() { return salas; }
}
```

#### `Sala`

Es el centro de tres relaciones:

- `@ManyToOne` hacia `Sede` con `@JoinColumn(name = "sede_id")`: **lado dueño**, tiene la
  clave foránea.
- `@ManyToMany` hacia `Equipamiento` con `@JoinTable`: **lado dueño**; JPA crea la tabla
  intermedia `sala_equipamiento` con las columnas `sala_id` y `equipamiento_id`.
- `@Table(uniqueConstraints = ...)` sobre `(sede_id, nombre)`: dos salas pueden llamarse
  igual solo si están en sedes distintas (RN-11).

**📄 `src/main/java/com/coworkhub/persistences/entities/Sala.java`**

```java
package com.coworkhub.persistences.entities;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"sede_id", "nombre"}))
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSala tipo;

    @Column(nullable = false)
    private int capacidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tarifaPorHora;

    @Column(nullable = false)
    private boolean activa = true;

    // Lado dueño (tiene la clave foránea sede_id). EAGER: toda respuesta de Sala muestra su sede.
    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id")
    private Sede sede;

    // Lado dueño de la relación muchos a muchos (declara la tabla intermedia). LAZY por defecto.
    @ManyToMany
    @JoinTable(
            name = "sala_equipamiento",
            joinColumns = @JoinColumn(name = "sala_id"),
            inverseJoinColumns = @JoinColumn(name = "equipamiento_id")
    )
    private Set<Equipamiento> equipamientos = new HashSet<>();

    protected Sala() {
    }

    public Sala(String nombre, TipoSala tipo, int capacidad, BigDecimal tarifaPorHora, boolean activa, Sede sede) {
        actualizar(nombre, tipo, capacidad, tarifaPorHora, activa, sede);
    }

    public void actualizar(String nombre, TipoSala tipo, int capacidad, BigDecimal tarifaPorHora, boolean activa, Sede sede) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.tarifaPorHora = tarifaPorHora;
        this.activa = activa;
        this.sede = sede;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoSala getTipo() { return tipo; }
    public int getCapacidad() { return capacidad; }
    public BigDecimal getTarifaPorHora() { return tarifaPorHora; }
    public boolean isActiva() { return activa; }
    public Sede getSede() { return sede; }
    public Set<Equipamiento> getEquipamientos() { return equipamientos; }
}
```

#### `PlanMembresia`

`descuentoExcedente` es un porcentaje entero (0 a 100). Su lista `miembros` es el lado
inverso, ignorado en el JSON.

**📄 `src/main/java/com/coworkhub/persistences/entities/PlanMembresia.java`**

```java
package com.coworkhub.persistences.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class PlanMembresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private int horasIncluidasMes;

    // Porcentaje entre 0 y 100 que se descuenta a las horas excedentes.
    @Column(nullable = false)
    private int descuentoExcedente;

    @Column(nullable = false)
    private int maxReservasActivas;

    @OneToMany(mappedBy = "plan")
    @JsonIgnore
    private List<Miembro> miembros = new ArrayList<>();

    protected PlanMembresia() {
    }

    public PlanMembresia(String nombre, int horasIncluidasMes, int descuentoExcedente, int maxReservasActivas) {
        actualizar(nombre, horasIncluidasMes, descuentoExcedente, maxReservasActivas);
    }

    public void actualizar(String nombre, int horasIncluidasMes, int descuentoExcedente, int maxReservasActivas) {
        this.nombre = nombre;
        this.horasIncluidasMes = horasIncluidasMes;
        this.descuentoExcedente = descuentoExcedente;
        this.maxReservasActivas = maxReservasActivas;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public int getHorasIncluidasMes() { return horasIncluidasMes; }
    public int getDescuentoExcedente() { return descuentoExcedente; }
    public int getMaxReservasActivas() { return maxReservasActivas; }
    public List<Miembro> getMiembros() { return miembros; }
}
```

#### `Usuario` (paquete `security`)

Guarda la contraseña **ya codificada** con BCrypt. Está en `security.persistences.entities`
porque es parte de la capa de persistencia del módulo de seguridad.

**📄 `src/main/java/com/coworkhub/security/persistences/entities/Usuario.java`**

```java
package com.coworkhub.security.persistences.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombreUsuario;

    // Siempre codificada con BCrypt; nunca se guarda ni se devuelve en texto plano.
    @Column(nullable = false)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    protected Usuario() {
    }

    public Usuario(String nombreUsuario, String contrasena, Rol rol) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public Long getId() { return id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getContrasena() { return contrasena; }
    public Rol getRol() { return rol; }
}
```

#### `Miembro`

Dos relaciones para observar:

- `@ManyToOne` hacia `PlanMembresia`: muchos miembros comparten un plan.
- `@OneToOne(cascade = ALL, orphanRemoval = true, fetch = LAZY)` hacia `Usuario`, con la
  clave foránea `usuario_id` en esta tabla. El `cascade` hace que al **guardar** un
  miembro se guarde también su usuario, y al **eliminarlo**, se elimine el usuario. El
  campo lleva `@JsonIgnore`: jamás debe salir en una respuesta (tiene la contraseña).

**📄 `src/main/java/com/coworkhub/persistences/entities/Miembro.java`**

```java
package com.coworkhub.persistences.entities;

import com.coworkhub.security.persistences.entities.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Miembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String documento;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMiembro estado = EstadoMiembro.ACTIVO;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id")
    private PlanMembresia plan;

    // Lado dueño de la relación 1↔1 (tiene usuario_id). cascade + orphanRemoval:
    // al eliminar el miembro se elimina también su usuario. Nunca se muestra en el JSON.
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;

    protected Miembro() {
    }

    public Miembro(String documento, String nombre, String email, PlanMembresia plan) {
        this.documento = documento;
        this.nombre = nombre;
        this.email = email;
        this.plan = plan;
    }

    public void actualizar(String nombre, String email, PlanMembresia plan) {
        this.nombre = nombre;
        this.email = email;
        this.plan = plan;
    }

    public Long getId() { return id; }
    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public EstadoMiembro getEstado() { return estado; }
    public void setEstado(EstadoMiembro estado) { this.estado = estado; }
    public PlanMembresia getPlan() { return plan; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
```

#### `ServicioAdicional`

**📄 `src/main/java/com/coworkhub/persistences/entities/ServicioAdicional.java`**

```java
package com.coworkhub.persistences.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ServicioAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    protected ServicioAdicional() {
    }

    public ServicioAdicional(String nombre, BigDecimal precioUnitario) {
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
    }

    public void actualizar(String nombre, BigDecimal precioUnitario) {
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
}
```

#### `Reserva`

La entidad más importante. Además de las relaciones con `Sala` y `Miembro`:

- `@OneToMany(mappedBy = "reserva", cascade = ALL, orphanRemoval = true)` hacia
  `DetalleReserva`: la reserva es **la dueña del ciclo de vida** de sus detalles. Al guardarla
  se guardan sus detalles; si quitás un detalle de la lista, se **borra** de la base.
- Campos de costo separados (`costoSala`, `costoServicios`, `costoTotal`) y
  `minutosConsumidos`: guardan el resultado de las fórmulas de RN-07 y permiten resolver
  RN-08 (devolver horas y calcular el cargo) sin recalcular todo. Están justificados en el
  [análisis](00-analisis.md#5-supuestos-y-decisiones) (S-02 y S-03).
- Los métodos `agregarDetalle` y `quitarDetalle` mantienen la lista; el servicio los usa.

**📄 `src/main/java/com/coworkhub/persistences/entities/Reserva.java`**

```java
package com.coworkhub.persistences.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sala_id")
    private Sala sala;

    @ManyToOne(optional = false)
    @JoinColumn(name = "miembro_id")
    private Miembro miembro;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fin;

    @Column(nullable = false)
    private int asistentes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    // Minutos de la reserva que cuentan contra el consumo mensual del plan.
    // Vale 0 si la reserva se canceló con 24 h o más de anticipación.
    @Column(nullable = false)
    private int minutosConsumidos;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoSala = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoServicios = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cargoCancelacion = BigDecimal.ZERO;

    // Padre de DetalleReserva: cascade guarda los detalles con la reserva y
    // orphanRemoval elimina el detalle que se quita de la lista. LAZY por defecto.
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleReserva> detalles = new ArrayList<>();

    protected Reserva() {
    }

    public Reserva(Sala sala, Miembro miembro, LocalDateTime inicio, LocalDateTime fin, int asistentes) {
        this.sala = sala;
        this.miembro = miembro;
        this.inicio = inicio;
        this.fin = fin;
        this.asistentes = asistentes;
    }

    public void agregarDetalle(DetalleReserva detalle) {
        detalles.add(detalle);
    }

    public void quitarDetalle(DetalleReserva detalle) {
        detalles.remove(detalle);
    }

    public Long getId() { return id; }
    public Sala getSala() { return sala; }
    public Miembro getMiembro() { return miembro; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFin() { return fin; }
    public int getAsistentes() { return asistentes; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    public int getMinutosConsumidos() { return minutosConsumidos; }
    public void setMinutosConsumidos(int minutosConsumidos) { this.minutosConsumidos = minutosConsumidos; }
    public BigDecimal getCostoSala() { return costoSala; }
    public void setCostoSala(BigDecimal costoSala) { this.costoSala = costoSala; }
    public BigDecimal getCostoServicios() { return costoServicios; }
    public void setCostoServicios(BigDecimal costoServicios) { this.costoServicios = costoServicios; }
    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }
    public BigDecimal getCargoCancelacion() { return cargoCancelacion; }
    public void setCargoCancelacion(BigDecimal cargoCancelacion) { this.cargoCancelacion = cargoCancelacion; }
    public List<DetalleReserva> getDetalles() { return detalles; }
}
```

#### `DetalleReserva`

Es la **entidad intermedia** entre `Reserva` y `ServicioAdicional`. Podría haber sido un
`@ManyToMany`, pero eso no permite guardar datos propios de la relación. Acá guardamos la
`cantidad` y el `precioUnitarioAplicado`, que se **copia** del servicio al momento de
reservar (RN-07): si mañana sube el precio del catering, las reservas ya hechas no cambian.

`getSubtotal()` no es un campo de la base: es un cálculo que Jackson también incluye en el JSON.

**📄 `src/main/java/com/coworkhub/persistences/entities/DetalleReserva.java`**

```java
package com.coworkhub.persistences.entities;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Entidad intermedia entre Reserva y ServicioAdicional: además de la relación guarda
// datos propios (cantidad y el precio vigente al momento de reservar).
@Entity
public class DetalleReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reserva_id")
    @JsonIgnore
    private Reserva reserva;

    @ManyToOne(optional = false)
    @JoinColumn(name = "servicio_id")
    private ServicioAdicional servicio;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitarioAplicado;

    protected DetalleReserva() {
    }

    public DetalleReserva(Reserva reserva, ServicioAdicional servicio, int cantidad) {
        this.reserva = reserva;
        this.servicio = servicio;
        this.cantidad = cantidad;
        this.precioUnitarioAplicado = servicio.getPrecioUnitario();
    }

    public BigDecimal getSubtotal() {
        return precioUnitarioAplicado.multiply(BigDecimal.valueOf(cantidad));
    }

    public Long getId() { return id; }
    public Reserva getReserva() { return reserva; }
    public ServicioAdicional getServicio() { return servicio; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitarioAplicado() { return precioUnitarioAplicado; }
}
```

## ✅ Checkpoint 2a — las entidades compilan

Compilá el proyecto (`mvn compile`, o *Build* en tu IDE). Si falla, casi siempre es un
`import` faltante o un nombre mal escrito: comparalo con el código de arriba.

### Paso 2.3 — Repositorios

Un repositorio es una **interfaz** que extiende `JpaRepository<Entidad, Long>`; Spring
genera la implementación. Además de los métodos heredados (`findAll`, `findById`,
`save`, `delete`…), definimos consultas propias. Se agrupan en tres tipos:

**a) Consultas derivadas del nombre del método.** Spring interpreta el nombre:
`existsByNombreIgnoreCase` → "¿existe alguno con ese nombre, sin distinguir
mayúsculas?"; `existsByPlanId` → recorre la relación `plan` y compara su `id`;
`findByUsuarioNombreUsuario` → recorre `usuario` y compara `nombreUsuario`.

**b) `@EntityGraph`.** Trae asociaciones en la **misma consulta**. Sin él, listar 50
reservas emitiría 1 consulta + 50 por sus salas + 50 por sus miembros (el
**problema N+1**, RNF-08). Con `@EntityGraph(attributePaths = {...})` es una sola.

**c) `@Query` con JPQL** (consultas escritas sobre entidades, no tablas), donde el nombre
del método no alcanza:

- `haySolapamiento`: implementa RN-01 con `inicio < :fin and fin > :inicio`.
- `buscarDisponibles`: salas activas, con capacidad suficiente, **sin** reservas activas
  que se solapen (`not exists (subconsulta)`).
- `sumarMinutosConsumidos`: suma los minutos consumidos de un miembro en un rango de
  fechas (el consumo mensual).
- `buscarParaReservar` con `@Lock(PESSIMISTIC_WRITE)`: **bloquea la fila de la sala**
  hasta que termine la transacción. Es lo que hace cumplir RN-01 aunque lleguen dos
  solicitudes a la vez (lo verás en la [Fase 3c](03c-fase-3-reservas.md)).

**📄 `src/main/java/com/coworkhub/persistences/repositories/RepositorioSedes.java`**

```java
package com.coworkhub.persistences.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coworkhub.persistences.entities.Sede;

public interface RepositorioSedes extends JpaRepository<Sede, Long> {
}
```

**📄 `src/main/java/com/coworkhub/persistences/repositories/RepositorioEquipamientos.java`**

```java
package com.coworkhub.persistences.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coworkhub.persistences.entities.Equipamiento;

public interface RepositorioEquipamientos extends JpaRepository<Equipamiento, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
```

**📄 `src/main/java/com/coworkhub/persistences/repositories/RepositorioServiciosAdicionales.java`**

```java
package com.coworkhub.persistences.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coworkhub.persistences.entities.ServicioAdicional;

public interface RepositorioServiciosAdicionales extends JpaRepository<ServicioAdicional, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
```

**📄 `src/main/java/com/coworkhub/persistences/repositories/RepositorioPlanes.java`**

```java
package com.coworkhub.persistences.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coworkhub.persistences.entities.PlanMembresia;

public interface RepositorioPlanes extends JpaRepository<PlanMembresia, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
```

**📄 `src/main/java/com/coworkhub/persistences/repositories/RepositorioMiembros.java`**

```java
package com.coworkhub.persistences.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.coworkhub.persistences.entities.Miembro;

public interface RepositorioMiembros extends JpaRepository<Miembro, Long> {

    // Trae el plan en la misma consulta (evita una consulta extra por cada miembro al listar).
    @Override
    @EntityGraph(attributePaths = "plan")
    List<Miembro> findAll();

    @Override
    @EntityGraph(attributePaths = "plan")
    Optional<Miembro> findById(Long id);

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByPlanId(Long planId);

    // Busca al miembro dueño de un usuario: recorre la relación Miembro.usuario.nombreUsuario
    Optional<Miembro> findByUsuarioNombreUsuario(String nombreUsuario);
}
```

**📄 `src/main/java/com/coworkhub/persistences/repositories/RepositorioSalas.java`**

```java
package com.coworkhub.persistences.repositories;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.coworkhub.persistences.entities.EstadoReserva;
import com.coworkhub.persistences.entities.Sala;

import jakarta.persistence.LockModeType;

public interface RepositorioSalas extends JpaRepository<Sala, Long> {

    // Carga sede y equipamientos en la misma consulta (evita el problema N+1 al listar).
    @Override
    @EntityGraph(attributePaths = {"sede", "equipamientos"})
    List<Sala> findAll();

    @Override
    @EntityGraph(attributePaths = {"sede", "equipamientos"})
    Optional<Sala> findById(Long id);

    @EntityGraph(attributePaths = {"sede", "equipamientos"})
    List<Sala> findBySedeIdOrderByNombre(Long sedeId);

    boolean existsBySedeId(Long sedeId);

    boolean existsBySedeIdAndNombreIgnoreCase(Long sedeId, String nombre);

    boolean existsBySedeIdAndNombreIgnoreCaseAndIdNot(Long sedeId, String nombre, Long id);

    // Bloquea la fila de la sala hasta terminar la transacción: dos reservas simultáneas
    // sobre la misma sala se procesan una después de la otra (RN-01).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Sala s where s.id = :id")
    Optional<Sala> buscarParaReservar(@Param("id") Long id);

    // Salas activas de una sede, con capacidad suficiente y sin reservas que se solapen.
    @EntityGraph(attributePaths = {"sede", "equipamientos"})
    @Query("""
            select s from Sala s
            where s.sede.id = :sedeId
              and s.activa = true
              and s.capacidad >= :capacidadMinima
              and not exists (
                    select r.id from Reserva r
                    where r.sala = s
                      and r.estado in :estadosActivos
                      and r.inicio < :fin
                      and r.fin > :inicio)
            order by s.nombre
            """)
    List<Sala> buscarDisponibles(@Param("sedeId") Long sedeId,
                                 @Param("capacidadMinima") int capacidadMinima,
                                 @Param("inicio") LocalDateTime inicio,
                                 @Param("fin") LocalDateTime fin,
                                 @Param("estadosActivos") Collection<EstadoReserva> estadosActivos);
}
```

**📄 `src/main/java/com/coworkhub/persistences/repositories/RepositorioReservas.java`**

```java
package com.coworkhub.persistences.repositories;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.coworkhub.persistences.entities.EstadoReserva;
import com.coworkhub.persistences.entities.Reserva;

public interface RepositorioReservas extends JpaRepository<Reserva, Long> {

    @Override
    @EntityGraph(attributePaths = {"sala", "sala.sede", "sala.equipamientos", "miembro", "miembro.plan",
            "detalles", "detalles.servicio"})
    Optional<Reserva> findById(Long id);

    @EntityGraph(attributePaths = {"sala", "sala.sede", "sala.equipamientos", "miembro", "miembro.plan",
            "detalles", "detalles.servicio"})
    List<Reserva> findByMiembroIdOrderByInicioDesc(Long miembroId);

    @EntityGraph(attributePaths = {"sala", "sala.sede", "sala.equipamientos", "miembro", "miembro.plan",
            "detalles", "detalles.servicio"})
    List<Reserva> findBySalaIdAndInicioBetweenOrderByInicio(Long salaId, LocalDateTime desde, LocalDateTime hasta);

    @EntityGraph(attributePaths = {"sala", "sala.sede", "sala.equipamientos", "miembro", "miembro.plan",
            "detalles", "detalles.servicio"})
    List<Reserva> findByEstadoOrderByInicio(EstadoReserva estado);

    boolean existsBySalaId(Long salaId);

    boolean existsBySalaIdAndEstadoIn(Long salaId, Collection<EstadoReserva> estados);

    boolean existsByMiembroId(Long miembroId);

    boolean existsByMiembroIdAndEstadoIn(Long miembroId, Collection<EstadoReserva> estados);

    // ¿Algún detalle de alguna reserva usa este servicio adicional?
    boolean existsByDetallesServicioId(Long servicioId);

    // Reservas activas y futuras de un miembro (RN-06).
    long countByMiembroIdAndEstadoInAndInicioAfter(Long miembroId, Collection<EstadoReserva> estados,
                                                   LocalDateTime instante);

    // Hay solapamiento si inicioNuevo < finExistente y finNuevo > inicioExistente (RN-01).
    @Query("""
            select count(r) > 0 from Reserva r
            where r.sala.id = :salaId
              and r.estado in :estadosActivos
              and r.inicio < :fin
              and r.fin > :inicio
            """)
    boolean haySolapamiento(@Param("salaId") Long salaId,
                            @Param("inicio") LocalDateTime inicio,
                            @Param("fin") LocalDateTime fin,
                            @Param("estadosActivos") Collection<EstadoReserva> estadosActivos);

    // Minutos que un miembro lleva consumidos de su plan en un rango de fechas (un mes).
    @Query("""
            select coalesce(sum(r.minutosConsumidos), 0) from Reserva r
            where r.miembro.id = :miembroId
              and r.inicio >= :desde
              and r.inicio < :hasta
            """)
    long sumarMinutosConsumidos(@Param("miembroId") Long miembroId,
                                @Param("desde") LocalDateTime desde,
                                @Param("hasta") LocalDateTime hasta);
}
```

**📄 `src/main/java/com/coworkhub/security/persistences/repositories/RepositorioUsuarios.java`**

```java
package com.coworkhub.security.persistences.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coworkhub.security.persistences.entities.Usuario;

public interface RepositorioUsuarios extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuario(String nombreUsuario);
}
```

### Paso 2.4 — Datos iniciales con un script SQL (`data.sql`)

Los catálogos (sedes, salas, planes, miembros…) son **datos**, no lógica. Por eso los
cargamos con un **script SQL**, no con código Java (RNF-09). Spring Boot ejecuta
automáticamente el archivo `src/main/resources/data.sql` cada vez que arranca.

**¿Por qué un script y no una clase Java?** Se lee de un vistazo (es una tabla de datos, no
un programa), se edita sin tocar el código y separa los datos de la lógica. Es además lo
habitual en proyectos reales, donde estos datos suelen venir de scripts (o de herramientas
de migración como Flyway o Liquibase).

#### Configuración: que el script corra *después* de crear las tablas

Por defecto Spring Boot ejecuta `data.sql` **antes** de que Hibernate cree las tablas a
partir de tus entidades, y el script fallaría con `Table "SEDE" not found`. Estas tres
propiedades lo corrigen. Agregalas a `application.properties`, **justo debajo de
`spring.jpa.open-in-view=true`**:

**📄 `src/main/resources/application.properties`** (fragmento)

```properties
# Datos iniciales: Hibernate crea las tablas y DESPUÉS Spring ejecuta src/main/resources/data.sql
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
spring.sql.init.encoding=UTF-8
```

| Propiedad | Para qué |
|---|---|
| `spring.jpa.defer-datasource-initialization` | Posterga la ejecución de `data.sql` hasta que Hibernate haya creado las tablas |
| `spring.sql.init.mode=always` | Ejecuta el script siempre (con una base embebida como H2 ya es el comportamiento por defecto; lo dejamos explícito) |
| `spring.sql.init.encoding=UTF-8` | Lee el script en UTF-8, para que `Bogotá`, `Pacífico` o `Impresión` no se corrompan según el sistema operativo |

#### El script

Cómo está escrito, para que puedas modificarlo o ampliarlo:

- **Los nombres de tabla y de columna son los que genera Hibernate** a partir de tus
  entidades, en `snake_case`: `Sede.horaApertura` → `hora_apertura`; `Sala.sede` →
  columna `sede_id`; `Miembro.usuario` → `usuario_id`. Si un `INSERT` falla con `Column
  "x" not found`, comparalo con las sentencias `create table` que muestra el checkpoint 2b.
- **No se escriben los `id`**: la base los genera (`IDENTITY`). Por eso las claves foráneas
  se resuelven con **subconsultas por nombre**, por ejemplo
  `(SELECT id FROM sede WHERE nombre = 'Sede Centro')`, y el script no depende de qué
  número le tocó a cada fila.
- **El orden importa**: primero lo que no depende de nada (sedes, equipamiento, planes,
  servicios, usuarios) y después lo que apunta a eso (salas, miembros).
- **La tabla intermedia** `sala_equipamiento` se llena con `INSERT ... SELECT`, que
  combina cada sala con sus equipamientos.
- **Las contraseñas están codificadas con BCrypt**: el script guarda el *hash*
  (`$2a$10$...`), nunca la contraseña en texto plano. Cada hash incluye una "sal"
  aleatoria, por eso dos hashes de la misma contraseña son distintos y ninguno se puede
  revertir; `BCryptPasswordEncoder.matches(...)` sabe verificarlos. Si quisieras otra
  contraseña, generá su hash (por ejemplo, imprimiendo `passwordEncoder.encode("tuClave")`
  una vez al arrancar) y reemplazalo en el script.
- Hay una sala **inactiva** (`Escritorio B1`) y un miembro **suspendido** (Sofía León) a
  propósito, para probar RN-04 y RN-05 más adelante.

Los usuarios de prueba y lo que carga el script:

| Usuario | Contraseña | Rol | Miembro asociado |
|---|---|---|---|
| `admin` | `admin123` | `ADMIN` | — |
| `recepcion` | `recep123` | `RECEPCION` | — |
| `ana` | `ana123` | `MIEMBRO` | Ana Torres (plan Profesional) |
| `luis` | `luis123` | `MIEMBRO` | Luis Gómez (plan Básico) |

| Tabla | Filas | Detalle |
|---|---|---|
| `sede` | 2 | Sede Centro (07:00–22:00, Bogotá) y Sede Norte (08:00–20:00, Medellín) |
| `equipamiento` | 4 | Proyector, Pizarra, Videoconferencia, Aire acondicionado |
| `sala` | 7 | 4 en Sede Centro y 3 en Sede Norte; `Escritorio B1` inactiva |
| `sala_equipamiento` | 10 | Qué equipamiento tiene cada sala |
| `servicio_adicional` | 3 | Catering (25), Impresión (5), Soporte técnico (40) |
| `plan_membresia` | 4 | Flex (0 h), Básico (10 h), Profesional (40 h), Corporativo (120 h) |
| `usuario` | 4 | Los de la tabla de arriba |
| `miembro` | 5 | Ana, Luis, Marta, Carlos y Sofía (suspendida) |

**📄 `src/main/resources/data.sql`**

```sql
-- Datos iniciales de catálogos (RNF-09).
-- Spring Boot ejecuta este script al arrancar, DESPUÉS de que Hibernate crea las tablas
-- (ver spring.jpa.defer-datasource-initialization en application.properties).
-- Las claves foráneas se resuelven con subconsultas por nombre, para no depender de los ids.

-- ============================================================ Sedes
INSERT INTO sede (nombre, ciudad, direccion, hora_apertura, hora_cierre) VALUES
    ('Sede Centro', 'Bogotá',   'Carrera 7 # 32-16', '07:00:00', '22:00:00'),
    ('Sede Norte',  'Medellín', 'Calle 10 # 43-20',  '08:00:00', '20:00:00');

-- ============================================================ Equipamiento
INSERT INTO equipamiento (nombre) VALUES
    ('Proyector'),
    ('Pizarra'),
    ('Videoconferencia'),
    ('Aire acondicionado');

-- ============================================================ Salas
-- Escritorio B1 está inactiva a propósito, para probar RN-04.
INSERT INTO sala (nombre, tipo, capacidad, tarifa_por_hora, activa, sede_id) VALUES
    ('Sala Andes',    'SALA_REUNION',    8,  30.00, TRUE,  (SELECT id FROM sede WHERE nombre = 'Sede Centro')),
    ('Sala Caribe',   'SALA_REUNION',    4,  20.00, TRUE,  (SELECT id FROM sede WHERE nombre = 'Sede Centro')),
    ('Oficina 101',   'OFICINA_PRIVADA', 3,  15.00, TRUE,  (SELECT id FROM sede WHERE nombre = 'Sede Centro')),
    ('Escritorio A1', 'ESCRITORIO',      1,   5.00, TRUE,  (SELECT id FROM sede WHERE nombre = 'Sede Centro')),
    ('Sala Pacífico', 'SALA_REUNION',    12, 40.00, TRUE,  (SELECT id FROM sede WHERE nombre = 'Sede Norte')),
    ('Oficina 201',   'OFICINA_PRIVADA', 4,  18.00, TRUE,  (SELECT id FROM sede WHERE nombre = 'Sede Norte')),
    ('Escritorio B1', 'ESCRITORIO',      1,   5.00, FALSE, (SELECT id FROM sede WHERE nombre = 'Sede Norte'));

-- Equipamiento de cada sala (tabla intermedia de la relación muchos a muchos)
INSERT INTO sala_equipamiento (sala_id, equipamiento_id)
    SELECT s.id, e.id FROM sala s, equipamiento e
    WHERE s.nombre = 'Sala Andes' AND e.nombre IN ('Proyector', 'Pizarra', 'Videoconferencia');
INSERT INTO sala_equipamiento (sala_id, equipamiento_id)
    SELECT s.id, e.id FROM sala s, equipamiento e
    WHERE s.nombre = 'Sala Caribe' AND e.nombre = 'Pizarra';
INSERT INTO sala_equipamiento (sala_id, equipamiento_id)
    SELECT s.id, e.id FROM sala s, equipamiento e
    WHERE s.nombre = 'Oficina 101' AND e.nombre = 'Aire acondicionado';
INSERT INTO sala_equipamiento (sala_id, equipamiento_id)
    SELECT s.id, e.id FROM sala s, equipamiento e
    WHERE s.nombre = 'Sala Pacífico' AND e.nombre IN ('Proyector', 'Pizarra', 'Videoconferencia', 'Aire acondicionado');
INSERT INTO sala_equipamiento (sala_id, equipamiento_id)
    SELECT s.id, e.id FROM sala s, equipamiento e
    WHERE s.nombre = 'Oficina 201' AND e.nombre = 'Pizarra';

-- ============================================================ Servicios adicionales
INSERT INTO servicio_adicional (nombre, precio_unitario) VALUES
    ('Catering',        25.00),
    ('Impresión',        5.00),
    ('Soporte técnico', 40.00);

-- ============================================================ Planes de membresía
-- nombre, horas incluidas al mes, % de descuento en horas excedentes, máximo de reservas activas
INSERT INTO plan_membresia (nombre, horas_incluidas_mes, descuento_excedente, max_reservas_activas) VALUES
    ('Flex',         0,  0, 2),
    ('Básico',      10, 10, 3),
    ('Profesional', 40, 20, 5),
    ('Corporativo', 120, 30, 10);

-- ============================================================ Usuarios
-- Las contraseñas están codificadas con BCrypt (nunca en texto plano):
--   admin123 · recep123 · ana123 · luis123
INSERT INTO usuario (nombre_usuario, contrasena, rol) VALUES
    ('admin',     '$2a$10$39hh5RkT67Qi1SIf.dAnR.WiiX5RzSm9YweaAveBaa9vAOlR1mm/C', 'ADMIN'),
    ('recepcion', '$2a$10$MDhaCnzClH1kMKofpB.FauB/PKiKhl5ijGPL883MHJ5s/5Eq9X2PK', 'RECEPCION'),
    ('ana',       '$2a$10$Oa2oRCfTWRe0X9Lxd1yU3er.aYHdSYRqKSuqXUs07sOYcJzS7hZlu', 'MIEMBRO'),
    ('luis',      '$2a$10$A.WYqrDC1F7hlx66LOB10e096ni/0MVq/oEdUak35V8aXUXgf9y22', 'MIEMBRO');

-- ============================================================ Miembros
-- Ana y Luis tienen usuario (pueden iniciar sesión). Sofía está suspendida a propósito, para probar RN-05.
INSERT INTO miembro (documento, nombre, email, estado, plan_id, usuario_id) VALUES
    ('1010101', 'Ana Torres',  'ana@coworkhub.test',    'ACTIVO',
        (SELECT id FROM plan_membresia WHERE nombre = 'Profesional'),
        (SELECT id FROM usuario WHERE nombre_usuario = 'ana')),
    ('2020202', 'Luis Gómez',  'luis@coworkhub.test',   'ACTIVO',
        (SELECT id FROM plan_membresia WHERE nombre = 'Básico'),
        (SELECT id FROM usuario WHERE nombre_usuario = 'luis')),
    ('3030303', 'Marta Ríos',  'marta@coworkhub.test',  'ACTIVO',
        (SELECT id FROM plan_membresia WHERE nombre = 'Flex'), NULL),
    ('4040404', 'Carlos Peña', 'carlos@coworkhub.test', 'ACTIVO',
        (SELECT id FROM plan_membresia WHERE nombre = 'Corporativo'), NULL),
    ('5050505', 'Sofía León',  'sofia@coworkhub.test',  'SUSPENDIDO',
        (SELECT id FROM plan_membresia WHERE nombre = 'Básico'), NULL);
```

> ℹ️ **Las reservas de ejemplo no van en este script.** Sus fechas son relativas a "hoy"
> (según el `Clock` de la aplicación) y sus costos los calcula la calculadora de la
> Fase 3c, así que se cargan con una clase Java ahí. El script solo contiene datos que no
> cambian con el tiempo.

#### El script funciona igual en H2, MySQL y PostgreSQL

Está escrito con SQL estándar (inserciones de varias filas, subconsultas, `INSERT ... SELECT`),
sin nada específico de un motor. Los mismos datos se cargan en las tres bases sin cambios
(lo verificamos contra H2, MySQL 8.4 y PostgreSQL 16).

#### ¿Y si la base conserva los datos? (MySQL y PostgreSQL)

`data.sql` se ejecuta **en cada arranque**. En H2 en memoria no hay problema: la base nace
vacía cada vez. En una base que guarda los datos en disco, un segundo arranque intentaría
insertar de nuevo las mismas filas. Por eso los perfiles `mysql` y `postgres` usan
`spring.jpa.hibernate.ddl-auto=create`: **borran y recrean las tablas en cada arranque**, así
que el script siempre parte de una base vacía. La consecuencia es que **esos datos no
sobreviven a un reinicio** (para desarrollo es lo cómodo: siempre el mismo punto de partida).

Si querés que **sí sobrevivan** —por ejemplo, para probar con datos que vas creando—,
arrancá **la primera vez** con el perfil tal cual (carga los datos) y **desde la segunda**
desactivá el script y el borrado:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres \
  -Dspring-boot.run.arguments="--spring.jpa.hibernate.ddl-auto=update --spring.sql.init.mode=never"
```

- `ddl-auto=update`: Hibernate crea o ajusta las tablas **sin borrarlas**.
- `sql.init.mode=never`: no ejecuta `data.sql`.

> ⚠️ Si usás `ddl-auto=update` **sin** `sql.init.mode=never`, el segundo arranque falla:
> `data.sql` intenta insertar filas que ya existen y la base rechaza los duplicados
> (`Failed to execute SQL script statement`), dejando además cargada solo una parte.
> Si te pasa, borrá los datos (`docker compose --profile postgres down -v`) o arrancá una
> vez con `ddl-auto=create`.

Las reservas de ejemplo (Fase 3c) no se duplican en ningún caso: su cargador no hace nada si
ya existen reservas.

## ✅ Checkpoint 2b — tablas y datos

1. **Las tablas.** Agregá **temporalmente** esta línea a `application.properties` para que
   Hibernate muestre las sentencias SQL que ejecuta:

   ```properties
   spring.jpa.show-sql=true
   ```

   Ejecutá `mvn spring-boot:run` (con el perfil de tu base: `h2` por defecto, o
   `-Dspring-boot.run.profiles=mysql` / `postgres`). Debés ver **10 sentencias `create table`** (una por
   cada una de las 9 entidades, más `sala_equipamiento`, la tabla intermedia de la
   relación N↔N), por ejemplo:

   ```text
   Hibernate: create table sala_equipamiento (sala_id bigint not null, equipamiento_id bigint not null, primary key (...))
   ```

   Y la aplicación debe arrancar sin errores (`Started Main in ...`). Si el script fallara,
   la aplicación **no arranca** (ver la tabla de problemas de abajo). Quitá
   `spring.jpa.show-sql` cuando termines: llena la consola de mensajes.

2. **Los datos.** Ejecutá la siguiente consulta, que cuenta las filas de cada tabla. **Cómo
   ejecutarla depende de tu base**:

   - **H2**: agregá **temporalmente** `spring.h2.console.enabled=true` a
     `application.properties`, reiniciá, abrí <http://localhost:8080/h2-console> y conectate
     con JDBC URL `jdbc:h2:mem:coworkhub`, usuario `sa` y contraseña vacía.
     ⚠️ **Quitá esa línea antes de la Fase 6**: la consola no debe quedar habilitada en un
     proyecto protegido.
   - **MySQL** o **PostgreSQL**: usá cualquier cliente, o la terminal (ver el paso 1.7):
     `docker exec -it coworkhub-mysql mysql --default-character-set=utf8mb4 -ucoworkhub -pcoworkhub coworkhub`
     o `docker exec -it coworkhub-postgres psql -U coworkhub coworkhub`.

   ```sql
   SELECT (SELECT COUNT(*) FROM sede) AS sedes,
          (SELECT COUNT(*) FROM sala) AS salas,
          (SELECT COUNT(*) FROM equipamiento) AS equipamientos,
          (SELECT COUNT(*) FROM sala_equipamiento) AS equipamiento_por_sala,
          (SELECT COUNT(*) FROM servicio_adicional) AS servicios,
          (SELECT COUNT(*) FROM plan_membresia) AS planes,
          (SELECT COUNT(*) FROM usuario) AS usuarios,
          (SELECT COUNT(*) FROM miembro) AS miembros;
   ```

   Debe devolver una fila con: `2, 7, 4, 10, 3, 4, 4, 5` (en las tres bases).

   Probá también:

   ```sql
   SELECT m.nombre, m.estado, p.nombre AS plan, u.nombre_usuario, u.rol
   FROM miembro m
   JOIN plan_membresia p ON p.id = m.plan_id
   LEFT JOIN usuario u ON u.id = m.usuario_id;
   ```

   y verificá que Ana y Luis tienen usuario, que los otros tres miembros tienen
   `NULL` en esas columnas, y que Sofía figura `SUSPENDIDO`. Con
   `SELECT nombre_usuario, contrasena FROM usuario;` comprobá que ninguna contraseña está
   en texto plano (todas empiezan con `$2a$`).

3. Confirmá en `SALA` que existe la columna `sede_id`, en `MIEMBRO` las columnas
   `plan_id` y `usuario_id`, y que **no** existe ninguna columna en `SEDE` que apunte a
   sus salas (el lado inverso no crea columnas).

| Si ves… | Causa probable |
|---|---|
| `Unknown entity` / `Not a managed type` | La entidad no tiene `@Entity`, o está fuera del paquete de `Main` |
| `Could not determine recommended JdbcType` | Un campo `enum` sin `@Enumerated` o un tipo no soportado |
| `Repeated column in mapping` | Dos campos apuntan a la misma columna (por ejemplo, olvidaste `mappedBy` en el lado inverso) |
| `PropertyReferenceException: No property 'x' found` | El nombre de un método derivado no coincide con un atributo (`findByUsuarioNombreUsuario` exige `Miembro.usuario.nombreUsuario`) |
| `Failed to execute SQL script statement #1 ... Table "SEDE" not found` | Falta `spring.jpa.defer-datasource-initialization=true`: el script corre antes de que existan las tablas |
| `Column "X" not found` en un `INSERT` | El nombre de columna no coincide con el que generó Hibernate (usá `snake_case`: `hora_apertura`, no `horaApertura`) |
| `NULL not allowed for column "X"` | Al `INSERT` le falta una columna obligatoria (`nullable = false` en la entidad) |
| Letras rotas (`BogotÃ¡`) | Falta `spring.sql.init.encoding=UTF-8` o el archivo no está guardado como UTF-8 |
| MySQL o PostgreSQL: el segundo arranque falla con `Failed to execute SQL script statement` | Se usó `ddl-auto=update` sin `spring.sql.init.mode=never`, y `data.sql` reinserta filas existentes | Ver "¿Y si la base conserva los datos?", en el paso 2.4 |
| MySQL: en la terminal se ven `Pac�fico` o `Bogot�` | Es el cliente `mysql` de la terminal, no la base | Conectate con `--default-character-set=utf8mb4`; la aplicación y la base están bien |
| `Value too long for column "CONTRASENA"` / no se puede iniciar sesión luego | El hash BCrypt se copió incompleto (mide 60 caracteres) |

### Paso 2.5 — Commit

```bash
git add .
git commit -m "fase 2: modelo de datos, repositorios y datos iniciales"
```

**Siguiente →** [Fase 3a — Catálogos](03a-fase-3-catalogos.md)
