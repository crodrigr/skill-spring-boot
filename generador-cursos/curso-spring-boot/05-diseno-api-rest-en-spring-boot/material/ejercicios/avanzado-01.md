# 🔴 Avanzado 01 — Diagnosticar un código de estado incorrecto

## 🧩 Problema

Un compañero de equipo te muestra este endpoint y te dice: "cuando pido
un libro que no existe, Insomnia me muestra `200 OK` con el cuerpo
`null`, en vez de un error. ¿Qué está mal?".

## 💻 Código o contexto de partida

```java
// ControladorLibros.java — capa controllers (com.biblioteca.controllers)
@GetMapping("/{id}")
public Libro buscarPorId(@PathVariable Long id) {
    return servicioLibros.buscarPorId(id).orElse(null);
}
```

```text
Método: GET
URL: http://localhost:8080/libros/999
Respuesta: 200 OK
null
```

## 📏 Criterios de evaluación de la solución

- Identifica que el método devuelve `Libro` directamente (no
  `ResponseEntity<Libro>`), por lo que Spring siempre responde `200`, sin
  importar si el `Optional` estaba vacío.
- Propone la corrección: cambiar la firma a `ResponseEntity<Libro>` y usar
  `.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build())`.
- Explica que, tras la corrección, la misma solicitud debería responder
  `404 Not Found`, no `200` con un cuerpo `null`.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Avanzado

## 🎓 Resultados de aprendizaje

RA-8
