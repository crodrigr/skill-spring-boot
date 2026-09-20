# 🛠️ Taller 01 — Facturación de MediSalud: `Paciente`→`Factura`→`DetalleFactura`

## 🎯 Objetivo (RA-7, RA-8, RA-12)

Crear un proyecto Spring Boot desde cero con Spring Initializr, modelar
`Factura`/`DetalleFactura` como un padre con líneas de detalle relacionadas
con `cascade`/`orphanRemoval`, y ejecutar contra H2 en memoria una
operación de alta (crear una factura con varias líneas) y una de edición
(modificar una línea existente).

## 🌍 Contexto

MediSalud necesita registrar la facturación de cada `Paciente` (Módulo 3,
reutilizado): cada paciente puede tener varias `Factura`, y cada `Factura`
tiene varias líneas de detalle (`DetalleFactura`) con el concepto y el
monto de cada ítem facturado. El Ejemplo 08 mostró una vista previa
compacta de este mismo patrón con un único detalle; este taller pide
construirlo completo, desde la creación del proyecto hasta las operaciones
de alta y edición.

## 🏗️ Capas MVC del proyecto

Todo proyecto del curso organiza sus clases por capa MVC (`controllers`,
`services` y `persistences`). En este taller solo existe la capa de
**persistencia**, porque todavía no hay API ni lógica de negocio:

| Capa | Paquete | Qué contiene | Clases de este taller |
|---|---|---|---|
| **Persistence** | `com.medisalud.persistences.entities` | Clases `@Entity` | `Paciente`, `Factura`, `DetalleFactura` |
| **Persistence** | `com.medisalud.persistences.repositories` | Interfaces de Spring Data JPA | `RepositorioPacientes`, `RepositorioFacturas` |
| Punto de entrada | `com.medisalud` | `@SpringBootApplication` + `CommandLineRunner` | `Main` |

`Main` es solo un ejecutor de prueba (no es una capa) y usa los
repositorios directamente porque todavía no existe una capa `services`.
`services` y `controllers` se agregan en el Módulo 5.

## 🪜 Pasos

1. **Crear el proyecto Spring Boot**: generá un proyecto nuevo con
   [start.spring.io](https://start.spring.io) (Maven, Java 17, Spring Boot
   3.x, Group `com.medisalud`, Artifact `taller-facturacion`), con las
   dependencias **Spring Data JPA** y **H2 Database**, igual que en el
   Ejemplo 01.
2. **Definir la estructura de paquetes**: descomprimí el proyecto y
   organizá las clases bajo `src/main/java/com/medisalud`: `Main` en el
   paquete raíz, las entidades en `persistences/entities` y los
   repositorios en `persistences/repositories`. Dejá
   `application.properties` en `src/main/resources` apuntando a
   `jdbc:h2:mem:medisalud`.
3. **Declarar `Factura` y `DetalleFactura`** (en
   `com.medisalud.persistences.entities`): copiá `Paciente` del Módulo 3
   tal cual (sin relaciones nuevas hacia `Factura`, para no modificar una
   clase ya cerrada); creá `Factura` (`id`, `fecha`, `@ManyToOne` hacia
   `Paciente`) y `DetalleFactura` (`id`, `concepto`, `monto`, `@ManyToOne`
   hacia `Factura`), con la relación `Factura`→`DetalleFactura` usando
   `cascade = CascadeType.ALL` y `orphanRemoval = true` (igual que
   `Paciente`↔`Cita` en el Ejemplo 05).
4. **Declarar `RepositorioFacturas`**: una interfaz Spring Data JPA que
   extienda `JpaRepository<Factura, Long>`, en
   `com.medisalud.persistences.repositories` (junto con
   `RepositorioPacientes`, copiado del Módulo 3).
5. **Agregar y modificar registros**: escribí un `Main` que guarde un
   `Paciente`, cree una `Factura` con al menos dos `DetalleFactura` en una
   sola llamada a `RepositorioFacturas.save(...)` (aprovechando `cascade`),
   y luego modifique el `monto` de uno de los detalles ya guardados,
   verificando el cambio con una nueva consulta.

## 💡 Ejemplo resuelto (parcial)

Así se ve `DetalleFactura`, para que uses el mismo estilo en el resto del
entregable:

```java
package com.medisalud.persistences.entities;

@Entity
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String concepto;

    private Double monto;

    @ManyToOne
    @JoinColumn(name = "factura_id")
    private Factura factura;

    protected DetalleFactura() {
    }

    public DetalleFactura(String concepto, Double monto, Factura factura) {
        this.concepto = concepto;
        this.monto = monto;
        this.factura = factura;
    }

    public Long getId() { return id; }
    public String getConcepto() { return concepto; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
}
```

El resto del entregable (`Factura` con `cascade`/`orphanRemoval`,
`RepositorioFacturas` y el `Main` con las operaciones de alta y edición)
queda a tu cargo — la solución completa está en
`solucion-taller-01.md`, pero intentá resolverlo primero por tu cuenta.

## 📦 Entregable

```text
📁 taller-01-facturacion
└── 📁 src/main
    ├── 📁 java/com/medisalud
    │   ├── 📄 Main.java
    │   └── 📁 persistences
    │       ├── 📁 entities
    │       │   ├── 📄 Paciente.java
    │       │   ├── 📄 Factura.java
    │       │   └── 📄 DetalleFactura.java
    │       └── 📁 repositories
    │           ├── 📄 RepositorioPacientes.java
    │           └── 📄 RepositorioFacturas.java
    └── 📁 resources
        └── 📄 application.properties
```

## 🧪 Casos de prueba

- Guardar una `Factura` con al menos dos `DetalleFactura` usando un único
  `repositorioFacturas.save(factura)`, y confirmar que
  `factura.getDetalles().size()` devuelve `2` sin haber usado ningún
  repositorio propio de `DetalleFactura`.
- Modificar el `monto` de un `DetalleFactura` ya guardado, volver a guardar
  la `Factura`, y confirmar con una nueva lectura que el valor cambió.

## 📏 Criterios de evaluación

- `Factura` y `DetalleFactura` son entidades JPA completas y compilables,
  ubicadas en `persistences.entities`; los repositorios están en
  `persistences.repositories` y `Main` en el paquete raíz `com.medisalud`.
- La relación `Factura`→`DetalleFactura` declara `cascade =
  CascadeType.ALL` y `orphanRemoval = true`.
- El proyecto arranca correctamente contra H2 en memoria (log de arranque
  exitoso, sin errores de conexión).
- El `Main` ejecuta sin excepciones y produce una salida coherente con la
  operación de alta y la de edición realizadas.
