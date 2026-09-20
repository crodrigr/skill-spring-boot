# 🗄️ Fase 2 — Modelo de datos: entidades, relaciones y datos semilla

**Navegación**: [Índice](README.md) · ← [Fase 1 — Proyecto base](01-fase-1-proyecto-base.md) · Siguiente → [Fase 3a — Catálogos](03a-fase-3-catalogos.md)

## 🎯 Qué vas a lograr

Convertir el modelo de dominio de la [Fase 0](00-analisis.md) en entidades JPA con sus
relaciones, crear los repositorios y cargar datos de ejemplo al arrancar. Al terminar
verás las tablas creadas por Hibernate y los datos en la base.

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

### Paso 2.4 — Datos semilla de catálogos

Un `CommandLineRunner` se ejecuta **una vez, justo después de arrancar** la aplicación.
Este carga sedes, equipamiento, salas, servicios adicionales, planes, usuarios y miembros
(RNF-09). Puntos a notar:

- `@Order(1)`: garantiza que corra **antes** del cargador de reservas (`@Order(2)`) que
  crearás en la fase siguiente.
- `if (repositorioSedes.count() > 0) return;`: si ya hay datos, no los duplica.
- `@Transactional`: todo el alta se hace en una sola transacción.
- Los usuarios de prueba (contraseñas codificadas con el `PasswordEncoder` de la Fase 1):

| Usuario | Contraseña | Rol | Miembro asociado |
|---|---|---|---|
| `admin` | `admin123` | `ADMIN` | — |
| `recepcion` | `recep123` | `RECEPCION` | — |
| `ana` | `ana123` | `MIEMBRO` | Ana Torres (plan Profesional) |
| `luis` | `luis123` | `MIEMBRO` | Luis Gómez (plan Básico) |

- Hay una sala **inactiva** (`Escritorio B1`) y un miembro **suspendido** (Sofía León)
  a propósito, para probar RN-04 y RN-05 más adelante.

**📄 `src/main/java/com/coworkhub/config/CargadorCatalogos.java`**

```java
package com.coworkhub.config;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.persistences.entities.Equipamiento;
import com.coworkhub.persistences.entities.EstadoMiembro;
import com.coworkhub.persistences.entities.Miembro;
import com.coworkhub.persistences.entities.PlanMembresia;
import com.coworkhub.persistences.entities.Sala;
import com.coworkhub.persistences.entities.Sede;
import com.coworkhub.persistences.entities.ServicioAdicional;
import com.coworkhub.persistences.entities.TipoSala;
import com.coworkhub.persistences.repositories.RepositorioEquipamientos;
import com.coworkhub.persistences.repositories.RepositorioMiembros;
import com.coworkhub.persistences.repositories.RepositorioPlanes;
import com.coworkhub.persistences.repositories.RepositorioSalas;
import com.coworkhub.persistences.repositories.RepositorioSedes;
import com.coworkhub.persistences.repositories.RepositorioServiciosAdicionales;
import com.coworkhub.security.persistences.entities.Rol;
import com.coworkhub.security.persistences.entities.Usuario;
import com.coworkhub.security.persistences.repositories.RepositorioUsuarios;

// Datos semilla de catálogos (RNF-09). Se ejecuta primero (@Order(1)); las reservas
// de ejemplo las carga otra clase, después, cuando existan los servicios de negocio.
@Component
@Order(1)
public class CargadorCatalogos implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CargadorCatalogos.class);

    private final RepositorioSedes repositorioSedes;
    private final RepositorioSalas repositorioSalas;
    private final RepositorioEquipamientos repositorioEquipamientos;
    private final RepositorioServiciosAdicionales repositorioServiciosAdicionales;
    private final RepositorioPlanes repositorioPlanes;
    private final RepositorioMiembros repositorioMiembros;
    private final RepositorioUsuarios repositorioUsuarios;
    private final PasswordEncoder passwordEncoder;

    public CargadorCatalogos(RepositorioSedes repositorioSedes,
                             RepositorioSalas repositorioSalas,
                             RepositorioEquipamientos repositorioEquipamientos,
                             RepositorioServiciosAdicionales repositorioServiciosAdicionales,
                             RepositorioPlanes repositorioPlanes,
                             RepositorioMiembros repositorioMiembros,
                             RepositorioUsuarios repositorioUsuarios,
                             PasswordEncoder passwordEncoder) {
        this.repositorioSedes = repositorioSedes;
        this.repositorioSalas = repositorioSalas;
        this.repositorioEquipamientos = repositorioEquipamientos;
        this.repositorioServiciosAdicionales = repositorioServiciosAdicionales;
        this.repositorioPlanes = repositorioPlanes;
        this.repositorioMiembros = repositorioMiembros;
        this.repositorioUsuarios = repositorioUsuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (repositorioSedes.count() > 0) {
            return;
        }

        // Sedes
        Sede centro = repositorioSedes.save(
                new Sede("Sede Centro", "Bogotá", "Carrera 7 # 32-16", LocalTime.of(7, 0), LocalTime.of(22, 0)));
        Sede norte = repositorioSedes.save(
                new Sede("Sede Norte", "Medellín", "Calle 10 # 43-20", LocalTime.of(8, 0), LocalTime.of(20, 0)));

        // Equipamiento
        Equipamiento proyector = repositorioEquipamientos.save(new Equipamiento("Proyector"));
        Equipamiento pizarra = repositorioEquipamientos.save(new Equipamiento("Pizarra"));
        Equipamiento videoconferencia = repositorioEquipamientos.save(new Equipamiento("Videoconferencia"));
        Equipamiento aire = repositorioEquipamientos.save(new Equipamiento("Aire acondicionado"));

        // Salas (la última está inactiva a propósito, para probar RN-04)
        crearSala("Sala Andes", TipoSala.SALA_REUNION, 8, "30.00", true, centro, proyector, pizarra, videoconferencia);
        crearSala("Sala Caribe", TipoSala.SALA_REUNION, 4, "20.00", true, centro, pizarra);
        crearSala("Oficina 101", TipoSala.OFICINA_PRIVADA, 3, "15.00", true, centro, aire);
        crearSala("Escritorio A1", TipoSala.ESCRITORIO, 1, "5.00", true, centro);
        crearSala("Sala Pacífico", TipoSala.SALA_REUNION, 12, "40.00", true, norte,
                proyector, pizarra, videoconferencia, aire);
        crearSala("Oficina 201", TipoSala.OFICINA_PRIVADA, 4, "18.00", true, norte, pizarra);
        crearSala("Escritorio B1", TipoSala.ESCRITORIO, 1, "5.00", false, norte);

        // Servicios adicionales
        repositorioServiciosAdicionales.save(new ServicioAdicional("Catering", new BigDecimal("25.00")));
        repositorioServiciosAdicionales.save(new ServicioAdicional("Impresión", new BigDecimal("5.00")));
        repositorioServiciosAdicionales.save(new ServicioAdicional("Soporte técnico", new BigDecimal("40.00")));

        // Planes: nombre, horas incluidas al mes, % de descuento en excedentes, máximo de reservas activas
        PlanMembresia flex = repositorioPlanes.save(new PlanMembresia("Flex", 0, 0, 2));
        PlanMembresia basico = repositorioPlanes.save(new PlanMembresia("Básico", 10, 10, 3));
        PlanMembresia profesional = repositorioPlanes.save(new PlanMembresia("Profesional", 40, 20, 5));
        PlanMembresia corporativo = repositorioPlanes.save(new PlanMembresia("Corporativo", 120, 30, 10));

        // Personal (sin miembro asociado)
        repositorioUsuarios.save(new Usuario("admin", passwordEncoder.encode("admin123"), Rol.ADMIN));
        repositorioUsuarios.save(new Usuario("recepcion", passwordEncoder.encode("recep123"), Rol.RECEPCION));

        // Miembros (Ana y Luis tienen usuario para poder iniciar sesión)
        crearMiembro("1010101", "Ana Torres", "ana@coworkhub.test", profesional, "ana", "ana123");
        crearMiembro("2020202", "Luis Gómez", "luis@coworkhub.test", basico, "luis", "luis123");
        crearMiembro("3030303", "Marta Ríos", "marta@coworkhub.test", flex, null, null);
        crearMiembro("4040404", "Carlos Peña", "carlos@coworkhub.test", corporativo, null, null);
        Miembro sofia = crearMiembro("5050505", "Sofía León", "sofia@coworkhub.test", basico, null, null);
        sofia.setEstado(EstadoMiembro.SUSPENDIDO);
        repositorioMiembros.save(sofia);

        log.info("Catálogos cargados: {} sedes, {} salas, {} planes, {} miembros, {} usuarios",
                repositorioSedes.count(), repositorioSalas.count(), repositorioPlanes.count(),
                repositorioMiembros.count(), repositorioUsuarios.count());
    }

    private void crearSala(String nombre, TipoSala tipo, int capacidad, String tarifa, boolean activa,
                           Sede sede, Equipamiento... equipamientos) {
        Sala sala = new Sala(nombre, tipo, capacidad, new BigDecimal(tarifa), activa, sede);
        sala.getEquipamientos().addAll(List.of(equipamientos));
        repositorioSalas.save(sala);
    }

    private Miembro crearMiembro(String documento, String nombre, String email, PlanMembresia plan,
                                 String nombreUsuario, String contrasena) {
        Miembro miembro = new Miembro(documento, nombre, email, plan);
        if (nombreUsuario != null) {
            miembro.setUsuario(new Usuario(nombreUsuario, passwordEncoder.encode(contrasena), Rol.MIEMBRO));
        }
        return repositorioMiembros.save(miembro);
    }
}
```

## ✅ Checkpoint 2b — tablas y datos

1. Agregá **temporalmente** esta línea a `application.properties` para que Hibernate
   muestre las sentencias SQL que ejecuta:

   ```properties
   spring.jpa.show-sql=true
   ```

   Ejecutá `mvn spring-boot:run`. Debés ver **10 sentencias `create table`** (una por
   cada una de las 9 entidades, más `sala_equipamiento`, la tabla intermedia de la
   relación N↔N), por ejemplo:

   ```text
   Hibernate: create table sala_equipamiento (sala_id bigint not null, equipamiento_id bigint not null, primary key (...))
   ```

   Después, los `insert` de los datos semilla y esta línea del cargador:

   ```text
   Catálogos cargados: 2 sedes, 7 salas, 4 planes, 5 miembros, 4 usuarios
   ```

   Quitá `spring.jpa.show-sql` cuando termines: llena la consola de mensajes.

2. **Mirar la base (opcional, muy recomendable).** Agregá **temporalmente** esta línea a
   `application.properties`:

   ```properties
   spring.h2.console.enabled=true
   ```

   Reiniciá, abrí <http://localhost:8080/h2-console>, y conectate con:

   | Campo | Valor |
   |---|---|
   | JDBC URL | `jdbc:h2:mem:coworkhub` |
   | User Name | `sa` |
   | Password | *(vacía)* |

   Probá: `SELECT * FROM SALA;`, `SELECT * FROM SALA_EQUIPAMIENTO;` y
   `SELECT id, nombre_usuario, rol FROM USUARIO;`. Verificá que la contraseña **no** está
   en texto plano (empieza con `$2a$`).

   > ⚠️ **Quitá esa línea antes de la Fase 6.** La consola de H2 no debe quedar
   > habilitada en un proyecto protegido.

3. Confirmá en `SALA` que existe la columna `sede_id`, en `MIEMBRO` las columnas
   `plan_id` y `usuario_id`, y que **no** existe ninguna columna en `SEDE` que apunte a
   sus salas (el lado inverso no crea columnas).

| Si ves… | Causa probable |
|---|---|
| `Unknown entity` / `Not a managed type` | La entidad no tiene `@Entity`, o está fuera del paquete de `Main` |
| `Could not determine recommended JdbcType` | Un campo `enum` sin `@Enumerated` o un tipo no soportado |
| `Repeated column in mapping` | Dos campos apuntan a la misma columna (por ejemplo, olvidaste `mappedBy` en el lado inverso) |
| `PropertyReferenceException: No property 'x' found` | El nombre de un método derivado no coincide con un atributo (`findByUsuarioNombreUsuario` exige `Miembro.usuario.nombreUsuario`) |

### Paso 2.5 — Commit

```bash
git add .
git commit -m "fase 2: modelo de datos, repositorios y datos semilla"
```

**Siguiente →** [Fase 3a — Catálogos](03a-fase-3-catalogos.md)
