# 🔑 Solución — Taller 01: Manejo de excepciones en la API de pacientes

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante. Contiene el entregable completo del Taller 01.

## 🌳 Árbol de archivos (entregable final)

```text
📁 taller-01-manejo-de-excepciones-pacientes
└── 📁 src/main
    ├── 📁 java/com/medisalud
    │   ├── 📁 controllers
    │   │   └── 📄 ControladorPacientes.java
    │   ├── 📁 services
    │   │   └── 📄 ServicioPacientes.java
    │   ├── 📁 persistences
    │   │   ├── 📁 entities
    │   │   │   └── 📄 Paciente.java
    │   │   └── 📁 repositories
    │   │       └── 📄 RepositorioPacientes.java
    │   └── 📁 exception
    │       ├── 📄 PacienteNoEncontradoException.java
    │       └── 📄 ManejadorGlobalDeExcepciones.java
    └── 📁 resources
        └── 📄 application.properties
```

**Capas MVC**: `controllers` → `services` → `persistences`
(`entities` + `repositories`). `exception` es un paquete transversal: la
excepción se lanza desde `services` y el `@ControllerAdvice` la traduce a
HTTP para `controllers`.

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

## 📄 Archivo: `PacienteNoEncontradoException.java` (`com.medisalud.exception`, nueva)

```java
package com.medisalud.exception;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PacienteNoEncontradoException extends RuntimeException {

    public PacienteNoEncontradoException(Long id) {
        super("No existe un paciente con id " + id);
    }
}
```

## 📄 Archivo: `ServicioPacientes.java` (`com.medisalud.services`, modificada)

```java
package com.medisalud.services;

import com.medisalud.persistences.entities.Paciente;
import com.medisalud.exception.PacienteNoEncontradoException;
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

    public Paciente buscarPorId(Long id) {
        return repositorioPacientes.findById(id)
                .orElseThrow(() -> new PacienteNoEncontradoException(id));
    }

    public Paciente crear(Paciente paciente) {
        return repositorioPacientes.save(paciente);
    }

    public Paciente actualizar(Long id, Paciente datos) {
        Paciente paciente = buscarPorId(id);
        paciente.setNombre(datos.getNombre());
        return repositorioPacientes.save(paciente);
    }

    public void eliminar(Long id) {
        Paciente paciente = buscarPorId(id);
        repositorioPacientes.delete(paciente);
    }
}
```

**Qué cambió respecto al Módulo 5**: `buscarPorId` devolvía
`Optional<Paciente>`; ahora devuelve `Paciente` directamente y lanza
`PacienteNoEncontradoException` si no existe. `actualizar` y `eliminar`
ya no verifican por su cuenta (`Optional.map`/`existsById`): reutilizan
`buscarPorId`, que ya lanza si corresponde.

## 📄 Archivo: `ControladorPacientes.java` (`com.medisalud.controllers`, modificada)

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
    public Paciente buscarPorId(@PathVariable Long id) {
        return servicioPacientes.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Paciente> crear(@RequestBody Paciente paciente) {
        Paciente creado = servicioPacientes.crear(paciente);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public Paciente actualizar(@PathVariable Long id, @RequestBody Paciente datos) {
        return servicioPacientes.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicioPacientes.eliminar(id);
        return ResponseEntity.ok().build();
    }
}
```

**Qué cambió respecto al Módulo 5**: `buscarPorId` construía
`ResponseEntity.notFound()` a mano con `.map(...)`/`.orElseGet(...)`;
ahora devuelve `Paciente` directamente, sin ningún `ResponseEntity` de
error — el `404` lo produce la excepción propagada desde el `Service`.
`actualizar` y `eliminar` tampoco construyen más `ResponseEntity.notFound()`.

## 📄 Archivo: `ManejadorGlobalDeExcepciones.java` (`com.medisalud.exception`, nueva)

```java
package com.medisalud.exception;

@ControllerAdvice
public class ManejadorGlobalDeExcepciones {

    @ExceptionHandler(PacienteNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarPacienteNoEncontrado(PacienteNoEncontradoException ex) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cuerpo);
    }
}
```

## 📄 Archivo: `application.properties` (Módulo 5, sin cambios)

```properties
spring.datasource.url=jdbc:h2:mem:medisalud;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

## 🧪 Pruebas en Insomnia

**1. Caso de éxito (`GET`)**

```text
Método: GET
URL: http://localhost:8080/pacientes/1
Respuesta: 200 OK
{
  "id": 1,
  "codigo": "P-030",
  "nombre": "Diego Marín"
}
```

**2. Caso de error (`GET` con id inexistente)**

```text
Método: GET
URL: http://localhost:8080/pacientes/999
Respuesta: 404 Not Found
{
  "error": "No existe un paciente con id 999"
}
```

(Contrastar con el Módulo 5, donde esta misma solicitud respondía `404`
con el cuerpo vacío por defecto de `ResponseEntity.notFound()`.)
