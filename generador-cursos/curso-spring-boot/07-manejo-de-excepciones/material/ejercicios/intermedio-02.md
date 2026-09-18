# 🟡 Intermedio 02 — Agregar un `@ExceptionHandler` a un controlador

## 🧩 Problema

Con `AutorNoEncontradoException` ya creada (Intermedio 01),
`ControladorAutores` responde `404` con el cuerpo por defecto de Spring
Boot. Te piden personalizar ese cuerpo, igual que se hizo con
`ControladorLibros` en el Ejemplo 05.

## 💻 Código o contexto de partida

```java
// AutorNoEncontradoException.java ya existe (Intermedio 01)
```

```java
// ControladorAutores.java (con buscarPorId ya modificado en Intermedio 01)
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
    public Autor buscarPorId(@PathVariable Long id) {
        return servicioAutores.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Autor> crear(@RequestBody Autor autor) {
        Autor creado = servicioAutores.crear(autor);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // TODO: agregar el @ExceptionHandler para AutorNoEncontradoException
}
```

Agregá un método `@ExceptionHandler(AutorNoEncontradoException.class)`
que devuelva un cuerpo `{"error": "<mensaje>"}"` con código `404`.

## 📏 Criterios de evaluación de la solución

- El método está anotado con `@ExceptionHandler(AutorNoEncontradoException.class)`.
- Devuelve `ResponseEntity` con código `404` y un cuerpo que incluye la
  clave `error` con el mensaje de la excepción.
- El método vive dentro de `ControladorAutores` (alcance local).

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-5
