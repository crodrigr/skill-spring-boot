# 🔴 Avanzado 01 — Centralizar un manejo duplicado en `@ControllerAdvice`

## 🧩 Problema

`ControladorLibros` y `ControladorAutores` tienen cada uno su propio
`@ExceptionHandler`, casi idéntico, para `LibroNoEncontradoException` y
`AutorNoEncontradoException` respectivamente. Te piden centralizar ambos
en una única clase `@ControllerAdvice`.

## 💻 Código o contexto de partida

```java
// Dentro de ControladorLibros (capa de presentación):
@ExceptionHandler(LibroNoEncontradoException.class)
public ResponseEntity<RespuestaError> manejarLibroNoEncontrado(LibroNoEncontradoException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RespuestaError(ex.getMessage()));
}
```

```java
// Dentro de ControladorAutores (capa de presentación):
@ExceptionHandler(AutorNoEncontradoException.class)
public ResponseEntity<RespuestaError> manejarAutorNoEncontrado(AutorNoEncontradoException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RespuestaError(ex.getMessage()));
}
```

1. Creá una clase `ManejadorGlobalDeExcepciones` (`@ControllerAdvice`)
   en el paquete `com.biblioteca.presentacion.excepcion` que incluya ambos
   métodos.
2. Quitá los dos métodos `@ExceptionHandler` de `ControladorLibros` y
   `ControladorAutores`.

## 📏 Criterios de evaluación de la solución

- `ManejadorGlobalDeExcepciones` está anotada con `@ControllerAdvice`.
- Incluye un método `@ExceptionHandler` para cada una de las dos
  excepciones, con el mismo comportamiento que tenían los métodos
  originales.
- Ni `ControladorLibros` ni `ControladorAutores` conservan ningún método
  `@ExceptionHandler` propio.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Avanzado

## 🎓 Resultados de aprendizaje

RA-6
