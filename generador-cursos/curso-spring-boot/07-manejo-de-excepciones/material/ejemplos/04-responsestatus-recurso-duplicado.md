# 💡 Ejemplo 04 — `@ResponseStatus`: recurso duplicado

## 🌍 Contexto

Hasta ahora, `ServicioLibros.crear(...)` guardaba cualquier `Libro`
recibido, sin verificar si su `isbn` ya existía en el catálogo — una
regla de negocio que faltaba desde el Módulo 5. Este ejemplo la agrega.

**Qué busca demostrar este ejemplo**: crear `LibroDuplicadoException`
anotada con `@ResponseStatus(HttpStatus.CONFLICT)`, y agregar la
verificación de duplicados a `ServicioLibros.crear`, reutilizando
`findByIsbn` (ya existente desde el Módulo 3).

## 📚 Caso de estudio

Biblioteca Universitaria: mismo proyecto del Ejemplo 03.

<details>
<summary>📄 Ver código completo de <code>Libro.java</code>, <code>RepositorioLibros.java</code> y <code>LibroNoEncontradoException.java</code> (reutilizados del Ejemplo 03)</summary>

## 💻 Archivo: `Libro.java`

```java
@Entity
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String isbn;

    private String titulo;

    protected Libro() {
    }

    public Libro(String isbn, String titulo) {
        this.isbn = isbn;
        this.titulo = titulo;
    }

    public Long getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
}
```

## 💻 Archivo: `RepositorioLibros.java`

```java
public interface RepositorioLibros extends JpaRepository<Libro, Long> {
    Optional<Libro> findByIsbn(String isbn);
}
```

## 💻 Archivo: `LibroNoEncontradoException.java`

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LibroNoEncontradoException extends RuntimeException {

    public LibroNoEncontradoException(Long id) {
        super("No existe un libro con id " + id);
    }
}
```

</details>

## 💻 Archivo: `LibroDuplicadoException.java`

```java
@ResponseStatus(HttpStatus.CONFLICT)
public class LibroDuplicadoException extends RuntimeException {

    public LibroDuplicadoException(String isbn) {
        super("Ya existe un libro con isbn " + isbn);
    }
}
```

## 💻 Archivo: `ServicioLibros.java` (método `crear` modificado)

```java
public Libro crear(Libro libro) {
    repositorioLibros.findByIsbn(libro.getIsbn())
            .ifPresent(existente -> {
                throw new LibroDuplicadoException(libro.getIsbn());
            });
    return repositorioLibros.save(libro);
}
```

(El resto de `ServicioLibros` — `listarTodos`, `buscarPorId`,
`buscarPorIsbn`, `actualizar`, `eliminar` — queda igual que en el
Ejemplo 03.)

## 🧭 Explicación paso a paso — qué cambió respecto al Módulo 5

1. **Antes** (Módulo 5): `ServicioLibros.crear(libro)` guardaba
   cualquier `Libro` recibido sin ninguna verificación previa — ni
   siquiera comprobaba si el isbn ya existía.
   **Ahora**: `crear` primero llama a `findByIsbn` (ya existente desde el
   Módulo 3, nunca antes usado para esta verificación) y, si encuentra un
   libro con ese isbn, lanza `LibroDuplicadoException` **antes** de
   guardar nada.
2. `ifPresent(...)` con una lambda que lanza la excepción es un patrón
   común para "verificar y fallar" sin necesitar una variable intermedia
   ni un `if` explícito.
3. `@ResponseStatus(HttpStatus.CONFLICT)` traduce esta excepción a `409`,
   el código estándar para "la solicitud entra en conflicto con el
   estado actual del recurso" — distinto del `404` de
   `LibroNoEncontradoException`, aunque ambas excepciones siguen
   exactamente el mismo mecanismo.
4. Esta regla de negocio nueva no existía en ningún ejemplo anterior del
   curso: es la primera vez que `ServicioLibros.crear` rechaza una
   solicitud por una razón distinta a un error técnico.

## ✅ Resultado esperado

```text
Método: POST
URL: http://localhost:8080/libros
Cuerpo:
{
  "isbn": "978-0-13-468599-1",
  "titulo": "Effective Java (copia)"
}
Respuesta: 409 Conflict
{
  "timestamp": "...",
  "status": 409,
  "error": "Conflict",
  "path": "/libros"
}
```

(Suponiendo que ya existe un libro con ese mismo isbn.)

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué código de estado HTTP se asocia convencionalmente
a un intento de crear un recurso duplicado?

- **A.** `200`.
- **B.** `404`.
- **C.** `409`.
- **D.** `500`.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. `409 Conflict` es el código estándar para una
solicitud que entra en conflicto con el estado actual del recurso (como
un duplicado).

</details>

**2. [Selección múltiple]** Sobre este ejemplo, seleccioná **todas** las
afirmaciones correctas.

- **A.** `ServicioLibros.crear` verifica el isbn antes de guardar, usando `findByIsbn`.
- **B.** `findByIsbn` es un método nuevo, creado específicamente para este ejemplo.
- **C.** `LibroDuplicadoException` y `LibroNoEncontradoException` siguen el mismo mecanismo (`@ResponseStatus`), con códigos distintos.
- **D.** Antes del Módulo 7, `crear` no verificaba si el isbn ya existía.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: `findByIsbn` ya existía
desde el Módulo 3; este ejemplo solo lo reutiliza con un propósito nuevo.

</details>

**3. [Abierta]** Un compañero te pregunta: "¿por qué no lanzar
simplemente `LibroNoEncontradoException` también para el caso de
duplicado, para no crear una clase nueva?".

**Pregunta**: ¿Qué le responderías?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Porque son dos situaciones semánticamente distintas
que deberían traducirse a códigos HTTP distintos: "no encontrado" es
`404`, "duplicado" es `409`. Si ambas usaran la misma excepción, no
habría forma de que `@ResponseStatus` distinguiera cuál código devolver
en cada caso — la anotación está fija en la clase, no puede variar según
el contexto en que se lanza. Cada situación de negocio con un
significado HTTP distinto necesita su propia clase de excepción.

</details>
