# 🔑 Solución — Taller 01: API REST de pacientes para MediSalud

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante. Contiene el entregable completo del Taller 01.

## 🌳 Árbol de archivos (entregable final)

```text
📁 taller-01-api-pacientes
└── 📁 src/main
    ├── 📁 java/com/medisalud
    │   ├── 📁 entity
    │   │   └── 📄 Paciente.java
    │   ├── 📁 repository
    │   │   └── 📄 RepositorioPacientes.java
    │   ├── 📁 service
    │   │   └── 📄 ServicioPacientes.java
    │   └── 📁 controller
    │       └── 📄 ControladorPacientes.java
    └── 📁 resources
        └── 📄 application.properties
```

**Nota sobre paquetes**: este Taller reorganiza en paquetes por capa
(`entity`, `repository`, `service`, `controller`) la misma arquitectura
ya explicada en el Ejemplo 03 (Controller → Service → Repository →
Database). Cada clase declara su `package` y solo importa explícitamente
las clases del proyecto que vienen de otro paquete; las clases de
Spring/Java (`@Service`, `Optional`, etc.) se omiten por brevedad, igual
que en el resto del curso.

## 📄 Archivo: `Paciente.java` (`com.medisalud.entity`)

```java
package com.medisalud.entity;

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

## 📄 Archivo: `RepositorioPacientes.java` (`com.medisalud.repository`)

```java
package com.medisalud.repository;

import com.medisalud.entity.Paciente;

public interface RepositorioPacientes extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByCodigo(String codigo);
}
```

## 📄 Archivo: `ServicioPacientes.java` (`com.medisalud.service`)

```java
package com.medisalud.service;

import com.medisalud.entity.Paciente;
import com.medisalud.repository.RepositorioPacientes;

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

## 📄 Archivo: `ControladorPacientes.java` (`com.medisalud.controller`)

```java
package com.medisalud.controller;

import com.medisalud.entity.Paciente;
import com.medisalud.service.ServicioPacientes;

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

## 📄 Archivo: `application.properties`

```properties
spring.datasource.url=jdbc:h2:mem:medisalud;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

## 🧪 Pruebas en Insomnia

**1. Crear (`POST`)**

```text
Método: POST
URL: http://localhost:8080/pacientes
Cuerpo:
{
  "codigo": "P-030",
  "nombre": "Diego Marín"
}
Respuesta: 201 Created
{
  "id": 1,
  "codigo": "P-030",
  "nombre": "Diego Marín"
}
```

**2. Buscar por id (`GET`)**

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

**3. Actualizar (`PUT`)**

```text
Método: PUT
URL: http://localhost:8080/pacientes/1
Cuerpo:
{
  "codigo": "P-030",
  "nombre": "Diego A. Marín"
}
Respuesta: 200 OK
{
  "id": 1,
  "codigo": "P-030",
  "nombre": "Diego A. Marín"
}
```

**4. Eliminar (`DELETE`)**

```text
Método: DELETE
URL: http://localhost:8080/pacientes/1
Respuesta: 200 OK
```

**5. Caso de error (`GET` tras eliminar)**

```text
Método: GET
URL: http://localhost:8080/pacientes/1
Respuesta: 404 Not Found
```
