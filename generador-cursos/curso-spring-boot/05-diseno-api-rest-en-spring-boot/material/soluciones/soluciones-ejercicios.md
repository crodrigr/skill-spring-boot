# 🔑 Soluciones — Ejercicios del Módulo 5

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante. Vive aparte de `material/ejercicios/` para que ninguna solución
> aparezca junto al enunciado.

## 🟢 Básico 01 — Elegir el verbo HTTP correcto

**Solución propuesta**:

1. `GET` — solo lee la lista, no modifica nada.
2. `POST` — crea un recurso nuevo (un libro que no existía).
3. `PATCH` — modifica un único campo (`titulo`), dejando el resto igual.
4. `PUT` — reemplaza el recurso completo (todos sus campos).
5. `DELETE` — elimina el recurso existente.

La distinción clave entre 3 y 4: `PATCH` alcanza cuando solo cambia una
parte del recurso; `PUT` se usa cuando se envía (y reemplaza) el recurso
completo.

## 🟢 Básico 02 — Elegir el código de estado correcto

**Solución propuesta**:

1. `200 OK` — la lectura fue exitosa y devuelve un cuerpo.
2. `201 Created` — la creación fue exitosa; se creó un recurso nuevo.
3. `404 Not Found` — el cliente pidió un recurso que no existe.
4. `400 Bad Request` (`4xx`) — la solicitud del cliente está mal formada
   (falta el cuerpo esperado).
5. `500 Internal Server Error` (`5xx`) — el problema es del servidor
   (falla la conexión a la base de datos), no de la solicitud del
   cliente.

La distinción clave entre 4 y 5: si el problema está en lo que envió el
cliente, es `4xx`; si el servidor falla al procesar una solicitud
correctamente formada, es `5xx`.

## 🟢 Básico 03 — Identificar la responsabilidad de cada capa

**Solución propuesta**:

| Fragmento | Clase | Capa MVC | Paquete | Responsabilidad |
|---|---|---|---|---|
| 1 | `RepositorioAutores` | `persistences` | `com.biblioteca.persistences.repositories` | Acceso a datos |
| 2 | `ControladorAutores` | `controllers` | `com.biblioteca.controllers` | Maneja HTTP sobre `/autores` |
| 3 | `ServicioAutores` | `services` | `com.biblioteca.services` | Lógica de negocio; delega en `RepositorioAutores` |

- Pregunta adicional: `ControladorAutores` debe llamar a `ServicioAutores`;
  nunca debe llamar directamente a `RepositorioAutores`, para no saltarse
  la capa de lógica de negocio (`controllers` → `services` →
  `persistences`).

## 🟡 Intermedio 01 — Crear una clase de servicio

**Solución propuesta**:

```java
package com.biblioteca.services;

import com.biblioteca.persistences.entities.Autor;
import com.biblioteca.persistences.repositories.RepositorioAutores;

@Service
public class ServicioAutores {

    private final RepositorioAutores repositorioAutores;

    public ServicioAutores(RepositorioAutores repositorioAutores) {
        this.repositorioAutores = repositorioAutores;
    }

    public List<Autor> listarTodos() {
        return repositorioAutores.findAll();
    }

    public Optional<Autor> buscarPorId(Long id) {
        return repositorioAutores.findById(id);
    }

    public Autor crear(Autor autor) {
        return repositorioAutores.save(autor);
    }

    public Optional<Autor> actualizar(Long id, Autor datos) {
        return repositorioAutores.findById(id)
                .map(autor -> {
                    autor.setNombre(datos.getNombre());
                    return repositorioAutores.save(autor);
                });
    }

    public boolean eliminar(Long id) {
        if (!repositorioAutores.existsById(id)) {
            return false;
        }
        repositorioAutores.deleteById(id);
        return true;
    }
}
```

**Verificación**: los cinco métodos siguen exactamente el mismo patrón de
`ServicioLibros` (Ejemplo 05), sin escribir SQL/JPQL en ningún punto.

## 🟡 Intermedio 02 — Crear los endpoints `GET` de un controlador

**Solución propuesta**:

```java
package com.biblioteca.controllers;

import com.biblioteca.persistences.entities.Autor;
import com.biblioteca.services.ServicioAutores;

@RestController
@RequestMapping("/autores")
public class ControladorAutores {

    private final ServicioAutores servicioAutores;

    public ControladorAutores(ServicioAutores servicioAutores) {
        this.servicioAutores = servicioAutores;
    }

    @GetMapping
    public List<Autor> listarTodos() {
        return servicioAutores.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Autor> buscarPorId(@PathVariable Long id) {
        return servicioAutores.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
```

## 🟡 Intermedio 03 — Crear los endpoints `POST`, `PUT`, `DELETE` de un controlador

**Solución propuesta** (métodos agregados a `ControladorAutores`, en
`com.biblioteca.controllers`; siguen delegando solo en `ServicioAutores`):

```java
    @PostMapping
    public ResponseEntity<Autor> crear(@RequestBody Autor autor) {
        Autor creado = servicioAutores.crear(autor);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Autor> actualizar(@PathVariable Long id, @RequestBody Autor datos) {
        return servicioAutores.actualizar(id, datos)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boolean existia = servicioAutores.eliminar(id);
        return existia ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
```

## 🔴 Avanzado 01 — Diagnosticar un código de estado incorrecto

**Solución propuesta**: el método devuelve `Libro` en vez de
`ResponseEntity<Libro>`, así que Spring siempre responde `200`, sin
importar si `buscarPorId` encontró algo o no. Corrección:

```java
// ControladorLibros.java — com.biblioteca.controllers
@GetMapping("/{id}")
public ResponseEntity<Libro> buscarPorId(@PathVariable Long id) {
    return servicioLibros.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}
```

**Verificación**: con la corrección, `GET /libros/999` (id inexistente)
debe responder `404 Not Found`, no `200` con cuerpo `null`.

## 🔴 Avanzado 02 — Diagnosticar una recursión infinita en JSON

**Solución propuesta**:

1. La recursión ocurre porque `Cita.getPaciente()` serializa un
   `Paciente`, y `Paciente.getCitas()` vuelve a serializar la lista de
   `Cita` que incluye la original — Jackson entra en un ciclo sin fin
   (`Cita` → `paciente` → `citas` → `Cita` → ...).
2. Corrección: agregar `@JsonIgnore` sobre `Paciente.citas`.

```java
// Paciente.java — com.medisalud.persistences.entities
@OneToMany(mappedBy = "paciente")
@JsonIgnore
private List<Cita> citas = new ArrayList<>();
```

**Verificación**: `GET /citas/1` debe responder `200 OK` con el JSON de
la cita, incluyendo los datos de su `paciente`; `GET /pacientes/1` (si se
expusiera) ya no incluiría la lista de `citas` en su respuesta.

## 🏆 Desafío 01 — API REST de citas para MediSalud

**Solución propuesta** (cada clase en el paquete de su capa MVC):

```java
// com.medisalud.persistences.entities.Paciente
@Entity
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigo;

    private String nombre;

    @OneToMany(mappedBy = "paciente")
    @JsonIgnore
    private List<Cita> citas = new ArrayList<>();

    protected Paciente() {
    }

    public Paciente(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public List<Cita> getCitas() { return citas; }
}
```

```java
// com.medisalud.services.ServicioCitas
@Service
public class ServicioCitas {

    private final RepositorioCitas repositorioCitas;

    public ServicioCitas(RepositorioCitas repositorioCitas) {
        this.repositorioCitas = repositorioCitas;
    }

    public List<Cita> listarTodos() {
        return repositorioCitas.findAll();
    }

    public Optional<Cita> buscarPorId(Long id) {
        return repositorioCitas.findById(id);
    }

    public Cita crear(Cita cita) {
        return repositorioCitas.save(cita);
    }

    public Optional<Cita> actualizar(Long id, Cita datos) {
        return repositorioCitas.findById(id)
                .map(cita -> {
                    cita.setFecha(datos.getFecha());
                    cita.setMotivo(datos.getMotivo());
                    return repositorioCitas.save(cita);
                });
    }

    public boolean eliminar(Long id) {
        if (!repositorioCitas.existsById(id)) {
            return false;
        }
        repositorioCitas.deleteById(id);
        return true;
    }
}
```

```java
// com.medisalud.controllers.ControladorCitas
@RestController
@RequestMapping("/citas")
public class ControladorCitas {

    private final ServicioCitas servicioCitas;

    public ControladorCitas(ServicioCitas servicioCitas) {
        this.servicioCitas = servicioCitas;
    }

    @GetMapping
    public List<Cita> listarTodos() {
        return servicioCitas.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> buscarPorId(@PathVariable Long id) {
        return servicioCitas.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Cita> crear(@RequestBody Cita cita) {
        Cita creada = servicioCitas.crear(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> actualizar(@PathVariable Long id, @RequestBody Cita datos) {
        return servicioCitas.actualizar(id, datos)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boolean existia = servicioCitas.eliminar(id);
        return existia ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
```

**Pruebas en Insomnia** (extracto — crear y el caso de error):

```text
Método: POST
URL: http://localhost:8080/citas
Cuerpo:
{
  "fecha": "2027-04-10",
  "motivo": "Control anual",
  "paciente": { "id": 1 }
}
Respuesta: 201 Created
{
  "id": 1,
  "fecha": "2027-04-10",
  "motivo": "Control anual",
  "paciente": { "id": 1, "codigo": "P-030", "nombre": "Diego Marín" }
}
```

```text
Método: GET
URL: http://localhost:8080/citas/999
Respuesta: 404 Not Found
```

**Verificación**: la respuesta de `POST /citas` incluye el `paciente`
completo, sin entrar en recursión infinita, gracias a `@JsonIgnore` en
`Paciente.citas`.
