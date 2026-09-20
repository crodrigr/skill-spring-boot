# 🔴 Avanzado 02 — Diagnosticar una excepción sin mecanismo de manejo

## 🧩 Problema

Un compañero de equipo te dice: "creé `PedidoInvalidoException`, la
lanzo desde mi `ServicioPedidos`, pero mi API responde `500 Internal
Server Error` en vez del `400 Bad Request` que esperaba".

## 💻 Código o contexto de partida

```java
// PedidoInvalidoException.java — paquete transversal (com.pedidos.exception)
public class PedidoInvalidoException extends RuntimeException {

    public PedidoInvalidoException(String motivo) {
        super("Pedido inválido: " + motivo);
    }
}
```

```java
// Dentro de ServicioPedidos — capa services (com.pedidos.services)
public Pedido crear(Pedido pedido) {
    if (pedido.getCantidad() <= 0) {
        throw new PedidoInvalidoException("la cantidad debe ser mayor a cero");
    }
    return repositorioPedidos.save(pedido);
}
```

```text
Método: POST
URL: http://localhost:8080/pedidos
Cuerpo: {"cantidad": 0}
Respuesta: 500 Internal Server Error
{
  "timestamp": "...", "status": 500, "error": "Internal Server Error", "path": "/pedidos"
}
```

**Preguntas**:

1. ¿Por qué la API responde `500` en vez de un código más específico?
2. ¿Cómo lo corregirías, aplicando el mecanismo más simple posible?

## 📏 Criterios de evaluación de la solución

- Identifica que `PedidoInvalidoException` no tiene `@ResponseStatus`, ni
  ningún `@ExceptionHandler`/`@ControllerAdvice` la maneja — por eso
  Spring Boot la trata como un error no controlado y responde `500`.
- Propone la corrección más simple y proporcional al caso: agregar
  `@ResponseStatus(HttpStatus.BAD_REQUEST)` sobre la clase
  `PedidoInvalidoException` (no hace falta `@ExceptionHandler` ni
  `@ControllerAdvice` para este caso, según la tabla comparativa del
  Ejemplo 07).
- Explica que, tras la corrección, la misma solicitud debería responder
  `400 Bad Request`.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Avanzado

## 🎓 Resultados de aprendizaje

RA-2, RA-7
