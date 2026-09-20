# 🔑 Soluciones — Ejercicios del Módulo 7

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante. Vive aparte de `material/ejercicios/` para que ninguna solución
> aparezca junto al enunciado.

## 🟢 Básico 01 — Clasificar excepciones y errores según la jerarquía `Throwable`

**Solución propuesta**:

1. `NullPointerException` — rama `Exception`: condición capturable (usar
   un objeto que resultó `null`), no un problema del entorno de
   ejecución.
2. `StackOverflowError` — rama `Error`: la pila de llamadas se agotó
   (normalmente por una recursión sin caso base); una aplicación
   razonable no debería intentar "recuperarse" de esto en tiempo de
   ejecución.
3. `IllegalArgumentException` — rama `Exception`: un argumento inválido
   es una condición esperable y validable dentro del propio programa.
4. `OutOfMemoryError` — rama `Error`: la JVM se quedó sin memoria
   disponible; no hay una acción correctiva razonable dentro del propio
   código de la aplicación.

## 🟢 Básico 02 — Identificar en qué capa se origina un error

**Solución propuesta**:

1. `persistences` (repositorio) — la pérdida de conexión a H2 es un problema de acceso a
   datos.
2. `services` — rechazar un isbn duplicado es una regla de negocio, no un
   problema de infraestructura ni de HTTP.
3. `controllers` — falta un parámetro de la propia solicitud HTTP, antes
   de que se ejecute cualquier lógica de negocio.

## 🟡 Intermedio 01 — Crear una excepción personalizada con `@ResponseStatus`

**Solución propuesta**:

```java
// com.biblioteca.exception.AutorNoEncontradoException (paquete transversal)
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AutorNoEncontradoException extends RuntimeException {

    public AutorNoEncontradoException(Long id) {
        super("No existe un autor con id " + id);
    }
}
```

```java
// com.biblioteca.services.ServicioAutores — la excepción se lanza en la capa services
public Autor buscarPorId(Long id) {
    return repositorioAutores.findById(id)
            .orElseThrow(() -> new AutorNoEncontradoException(id));
}
```

```java
// com.biblioteca.controllers.ControladorAutores
@GetMapping("/{id}")
public Autor buscarPorId(@PathVariable Long id) {
    return servicioAutores.buscarPorId(id);
}
```

**Verificación**: `GET /autores/999` (id inexistente) debe responder
`404 Not Found`, sin que `ControladorAutores` construya ningún
`ResponseEntity` de error a mano.

## 🟡 Intermedio 02 — Agregar un `@ExceptionHandler` a un controlador

**Solución propuesta**:

```java
@ExceptionHandler(AutorNoEncontradoException.class)
public ResponseEntity<Map<String, String>> manejarAutorNoEncontrado(AutorNoEncontradoException ex) {
    Map<String, String> cuerpo = new HashMap<>();
    cuerpo.put("error", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cuerpo);
}
```

**Verificación**: `GET /autores/999` debe responder `404` con el cuerpo
`{"error": "No existe un autor con id 999"}`.

## 🔴 Avanzado 01 — Centralizar un manejo duplicado en `@ControllerAdvice`

**Solución propuesta**:

```java
// com.biblioteca.exception.ManejadorGlobalDeExcepciones
@ControllerAdvice
public class ManejadorGlobalDeExcepciones {

    @ExceptionHandler(LibroNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarLibroNoEncontrado(LibroNoEncontradoException ex) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cuerpo);
    }

    @ExceptionHandler(AutorNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarAutorNoEncontrado(AutorNoEncontradoException ex) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cuerpo);
    }
}
```

**Verificación**: tanto `GET /libros/999` como `GET /autores/999` deben
responder `404` con el cuerpo `{"error": "..."}"`, sin que
`ControladorLibros` ni `ControladorAutores` conserven ningún
`@ExceptionHandler` propio.

## 🟡 Intermedio 03 — Elegir el mecanismo de manejo apropiado

**Solución propuesta**:

1. `@ResponseStatus` — un único controlador, sin necesidad de cuerpo
   personalizado; la opción más simple resuelve el caso completo.
2. `@ExceptionHandler` local — el requisito es específico de ese
   controlador, no debería propagarse al resto de la aplicación.
3. `@ControllerAdvice` — seis controladores necesitan uniformidad;
   centralizar evita escribir (y mantener) la misma lógica seis veces.

## 🔴 Avanzado 02 — Diagnosticar una excepción sin mecanismo de manejo

**Solución propuesta**:

1. `PedidoInvalidoException` no tiene `@ResponseStatus`, y ningún
   `@ExceptionHandler`/`@ControllerAdvice` la maneja; Spring Boot la
   trata como una excepción no controlada y responde `500 Internal
   Server Error` — el mismo código para cualquier problema, sin importar
   la causa real.
2. Corrección (mecanismo más simple, proporcional a un caso de un único
   significado HTTP):

```java
// com.pedidos.exception.PedidoInvalidoException (paquete transversal)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PedidoInvalidoException extends RuntimeException {

    public PedidoInvalidoException(String motivo) {
        super("Pedido inválido: " + motivo);
    }
}
```

**Verificación**: con la corrección, `POST /pedidos` con `cantidad: 0`
debe responder `400 Bad Request`, no `500`.

## 🏆 Desafío 01 — Manejo de excepciones en la API de citas

**Solución propuesta**:

```java
// com.medisalud.exception.CitaNoEncontradaException (paquete transversal)
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CitaNoEncontradaException extends RuntimeException {

    public CitaNoEncontradaException(Long id) {
        super("No existe una cita con id " + id);
    }
}
```

```java
// com.medisalud.services.ServicioCitas — lanza la excepción (capa services)
@Service
public class ServicioCitas {

    private final RepositorioCitas repositorioCitas;

    public ServicioCitas(RepositorioCitas repositorioCitas) {
        this.repositorioCitas = repositorioCitas;
    }

    public List<Cita> listarTodos() {
        return repositorioCitas.findAll();
    }

    public Cita buscarPorId(Long id) {
        return repositorioCitas.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(id));
    }

    public Cita crear(Cita cita) {
        return repositorioCitas.save(cita);
    }

    public Cita actualizar(Long id, Cita datos) {
        Cita cita = buscarPorId(id);
        cita.setFecha(datos.getFecha());
        cita.setMotivo(datos.getMotivo());
        return repositorioCitas.save(cita);
    }

    public void eliminar(Long id) {
        Cita cita = buscarPorId(id);
        repositorioCitas.delete(cita);
    }
}
```

**Qué cambió respecto al Módulo 5**: `buscarPorId` devolvía
`Optional<Cita>`; ahora devuelve `Cita` directamente y lanza
`CitaNoEncontradaException` si no existe. `actualizar` y `eliminar`
reutilizan `buscarPorId` en vez de verificar por su cuenta.

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
    public Cita buscarPorId(@PathVariable Long id) {
        return servicioCitas.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Cita> crear(@RequestBody Cita cita) {
        Cita creada = servicioCitas.crear(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public Cita actualizar(@PathVariable Long id, @RequestBody Cita datos) {
        return servicioCitas.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicioCitas.eliminar(id);
        return ResponseEntity.ok().build();
    }
}
```

**Qué cambió respecto al Módulo 5**: `buscarPorId` construía
`ResponseEntity.notFound()` a mano; ahora devuelve `Cita` directamente,
sin ningún `ResponseEntity` de error.

```java
// com.medisalud.exception.ManejadorGlobalDeExcepciones
@ControllerAdvice
public class ManejadorGlobalDeExcepciones {

    @ExceptionHandler(CitaNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> manejarCitaNoEncontrada(CitaNoEncontradaException ex) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cuerpo);
    }
}
```

**Pruebas en Insomnia**:

```text
Método: GET
URL: http://localhost:8080/citas/999
Respuesta: 404 Not Found
{
  "error": "No existe una cita con id 999"
}
```

**Verificación**: ningún método de `ControladorCitas` construye una
respuesta de error a mano; el cuerpo de error es consistente con el
usado en `Libro`/`Paciente`.
