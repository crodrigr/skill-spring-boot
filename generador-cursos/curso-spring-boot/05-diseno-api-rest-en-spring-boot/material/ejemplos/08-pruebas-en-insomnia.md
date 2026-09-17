# 💡 Ejemplo 08 — Pruebas en Insomnia

## 🌍 Contexto

`ControladorLibros` ya expone los cinco endpoints CRUD (Ejemplos 06-07)
más el endpoint de búsqueda por `isbn`. Antes de dar el módulo por
terminado, hace falta verificar que cada uno responde exactamente como se
espera — no alcanza con que el código compile.

**Qué busca demostrar este ejemplo**: probar los seis endpoints de
`ControladorLibros` con Insomnia (o cualquier cliente HTTP equivalente),
documentando método, URL, cuerpo, código de estado y respuesta de cada
uno, incluyendo un caso de error `404`.

## 📚 Caso de estudio

Biblioteca Universitaria: `ControladorLibros` completo (Ejemplos 06-07).

<details>
<summary>📄 Ver código completo de <code>ControladorLibros.java</code> (reutilizado de los Ejemplos 06-07)</summary>

## 💻 Archivo: `ControladorLibros.java`

```java
@RestController
@RequestMapping("/libros")
public class ControladorLibros {

    private final ServicioLibros servicioLibros;

    public ControladorLibros(ServicioLibros servicioLibros) {
        this.servicioLibros = servicioLibros;
    }

    @GetMapping
    public List<Libro> listarTodos() {
        return servicioLibros.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> buscarPorId(@PathVariable Long id) {
        return servicioLibros.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<Libro> buscarPorIsbn(@RequestParam String isbn) {
        return servicioLibros.buscarPorIsbn(isbn)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Libro> crear(@RequestBody Libro libro) {
        Libro creado = servicioLibros.crear(libro);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Libro> actualizar(@PathVariable Long id, @RequestBody Libro datos) {
        return servicioLibros.actualizar(id, datos)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boolean existia = servicioLibros.eliminar(id);
        return existia ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
```

</details>

## 🧪 Pruebas en Insomnia

**1. Crear un libro (`POST`)**

```text
Método: POST
URL: http://localhost:8080/libros
Cuerpo:
{
  "isbn": "978-0-13-468599-1",
  "titulo": "Effective Java"
}
Respuesta: 201 Created
{
  "id": 1,
  "isbn": "978-0-13-468599-1",
  "titulo": "Effective Java"
}
```

**2. Listar todos (`GET`)**

```text
Método: GET
URL: http://localhost:8080/libros
Respuesta: 200 OK
[
  {
    "id": 1,
    "isbn": "978-0-13-468599-1",
    "titulo": "Effective Java"
  }
]
```

**3. Buscar por id (`GET`)**

```text
Método: GET
URL: http://localhost:8080/libros/1
Respuesta: 200 OK
{
  "id": 1,
  "isbn": "978-0-13-468599-1",
  "titulo": "Effective Java"
}
```

**4. Buscar por isbn (`GET` + `@RequestParam`)**

```text
Método: GET
URL: http://localhost:8080/libros/buscar?isbn=978-0-13-468599-1
Respuesta: 200 OK
{
  "id": 1,
  "isbn": "978-0-13-468599-1",
  "titulo": "Effective Java"
}
```

**5. Actualizar (`PUT`)**

```text
Método: PUT
URL: http://localhost:8080/libros/1
Cuerpo:
{
  "isbn": "978-0-13-468599-1",
  "titulo": "Effective Java (3rd Edition)"
}
Respuesta: 200 OK
{
  "id": 1,
  "isbn": "978-0-13-468599-1",
  "titulo": "Effective Java (3rd Edition)"
}
```

**6. Eliminar (`DELETE`) y caso de error (`404`)**

```text
Método: DELETE
URL: http://localhost:8080/libros/1
Respuesta: 200 OK
```

```text
Método: GET
URL: http://localhost:8080/libros/1
Respuesta: 404 Not Found
```

## 🧭 Explicación paso a paso

1. El orden de prueba importa: crear primero (para tener un `id` real),
   después leer/actualizar sobre ese mismo `id`, y eliminar al final.
2. La prueba 6 combina dos solicitudes: el `DELETE` exitoso, y un `GET`
   posterior sobre el mismo `id` para confirmar que ya no existe (`404`)
   — probar solo el `DELETE` no demuestra que el recurso realmente
   desapareció.
3. La prueba 4 verifica específicamente el endpoint con `@RequestParam`
   del Ejemplo 07, que es fácil de olvidar probar porque no forma parte
   del CRUD "clásico" de cinco operaciones.
4. Cada prueba documentada acá es exactamente lo que se vería en la
   pestaña de respuesta de Insomnia: el código de estado en la parte
   superior, y el cuerpo JSON (si lo hay) debajo.

## ✅ Resultado esperado

Las seis pruebas de la sección anterior, en el orden mostrado, confirman
que `ControladorLibros` responde correctamente a cada operación, incluido
el caso de error `404` tras eliminar un recurso.

## ❓ Preguntas de repaso

**1. [Selección]** ¿Por qué la prueba 6 incluye un `GET` después del
`DELETE`, en vez de probar solo el `DELETE`?

- **A.** Porque Insomnia lo exige.
- **B.** Para confirmar que el recurso realmente desapareció de la base de datos, no solo que el `DELETE` respondió `200`.
- **C.** Porque `DELETE` nunca funciona sin un `GET` previo.
- **D.** No hay ninguna razón; es un paso opcional sin valor.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Un `DELETE` que responde `200` no prueba por
sí solo que el recurso desapareció; el `GET` posterior con `404` es la
confirmación real.

</details>

**2. [Abierta]** Un compañero probó únicamente los endpoints `GET` y
`POST` de su API, y te dice que "ya está todo probado".

**Pregunta**: ¿Qué le falta probar, y por qué es importante no saltarse
esos casos?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Le falta probar `PUT` (actualizar) y `DELETE`
(eliminar), además de al menos un caso de error (por ejemplo, `GET` o
`PUT` sobre un id inexistente, esperando `404`). Probar solo los casos de
éxito de `GET`/`POST` no garantiza que el resto del CRUD funcione, ni que
los errores se manejen correctamente — que es, en la práctica, donde
suelen aparecer los bugs (como el de código de estado incorrecto visto en
el Ejercicio Avanzado 01).

</details>
