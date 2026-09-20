# 🔑 Solución — Taller 01: Documentación automática de la API de pacientes

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante. Contiene el entregable completo del Taller 01.

## 🌳 Árbol de archivos (entregable final)

```text
📁 taller-01-documentacion-pacientes
├── 📄 pom.xml
└── 📁 src/main
    ├── 📁 java/com/medisalud
    │   ├── 📁 controllers
    │   │   └── 📄 ControladorPacientes.java
    │   ├── 📁 services
    │   │   └── 📄 ServicioPacientes.java
    │   └── 📁 persistences
    │       ├── 📁 entities
    │       │   └── 📄 Paciente.java
    │       └── 📁 repositories
    │           └── 📄 RepositorioPacientes.java
    └── 📁 resources
        └── 📄 application.properties
```

## 📄 Archivo: `pom.xml` (dependencia agregada)

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

## 📄 Archivo: `Paciente.java` (`com.medisalud.persistences.entities`, Módulo 5, sin cambios)

```java
package com.medisalud.persistences.entities;

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
    public void setNombre(String nombre) { this.nombre = nombre; }
}
```

## 📄 Archivo: `RepositorioPacientes.java` (`com.medisalud.persistences.repositories`, Módulo 5, sin cambios)

```java
package com.medisalud.persistences.repositories;

import com.medisalud.persistences.entities.Paciente;

public interface RepositorioPacientes extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByCodigo(String codigo);
}
```

## 📄 Archivo: `ServicioPacientes.java` (`com.medisalud.services`, Módulo 5, sin cambios)

```java
package com.medisalud.services;

import com.medisalud.persistences.entities.Paciente;
import com.medisalud.persistences.repositories.RepositorioPacientes;

@Service
public class ServicioPacientes {

    private final RepositorioPacientes repositorioPacientes;

    public ServicioPacientes(RepositorioPacientes repositorioPacientes) {
        this.repositorioPacientes = repositorioPacientes;
    }

    public List<Paciente> listarTodos() {
        return repositorioPacientes.findAll();
    }

    public Optional<Paciente> buscarPorId(Long id) {
        return repositorioPacientes.findById(id);
    }

    public Paciente crear(Paciente paciente) {
        return repositorioPacientes.save(paciente);
    }

    public Optional<Paciente> actualizar(Long id, Paciente datos) {
        return repositorioPacientes.findById(id)
                .map(paciente -> {
                    paciente.setNombre(datos.getNombre());
                    return repositorioPacientes.save(paciente);
                });
    }

    public boolean eliminar(Long id) {
        if (!repositorioPacientes.existsById(id)) {
            return false;
        }
        repositorioPacientes.deleteById(id);
        return true;
    }
}
```

## 📄 Archivo: `ControladorPacientes.java` (`com.medisalud.controllers`, Módulo 5, sin cambios)

```java
package com.medisalud.controllers;

import com.medisalud.persistences.entities.Paciente;
import com.medisalud.services.ServicioPacientes;

@RestController
@RequestMapping("/pacientes")
public class ControladorPacientes {

    private final ServicioPacientes servicioPacientes;

    public ControladorPacientes(ServicioPacientes servicioPacientes) {
        this.servicioPacientes = servicioPacientes;
    }

    @GetMapping
    public List<Paciente> listarTodos() {
        return servicioPacientes.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Paciente> buscarPorId(@PathVariable Long id) {
        return servicioPacientes.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Paciente> crear(@RequestBody Paciente paciente) {
        Paciente creado = servicioPacientes.crear(paciente);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Paciente> actualizar(@PathVariable Long id, @RequestBody Paciente datos) {
        return servicioPacientes.actualizar(id, datos)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boolean existia = servicioPacientes.eliminar(id);
        return existia ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
```

## 📄 Archivo: `application.properties` (con configuración de springdoc agregada)

```properties
spring.datasource.url=jdbc:h2:mem:medisalud;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/doc/swagger-ui.html
springdoc.packages-to-scan=com.medisalud
```

**Nota**: `springdoc.packages-to-scan=com.medisalud` alcanza para
documentar `ControladorPacientes`, aunque viva en el subpaquete
`com.medisalud.controllers` — springdoc escanea recursivamente todos los
subpaquetes del paquete indicado.

## 🌐 Documentación final visible en Swagger UI

Al acceder a `http://localhost:8080/doc/swagger-ui.html`:

```text
GET    /pacientes            → listarTodos       → 200
GET    /pacientes/{id}       → buscarPorId       → 200, 404
POST   /pacientes            → crear             → 201 (cuerpo: Paciente)
PUT    /pacientes/{id}       → actualizar        → 200, 404 (cuerpo: Paciente)
DELETE /pacientes/{id}       → eliminar          → 200, 404
```

Esquema de `Paciente`:

```text
Paciente
├── id: integer
├── codigo: string
└── nombre: string
```
