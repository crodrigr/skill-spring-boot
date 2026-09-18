# 📚 Explicación conceptual — Módulo 7

## 🧠 Concepto: ¿Qué es una excepción?

Una excepción es un evento anormal que interrumpe el flujo normal de un
programa; su manejo permite tomar una acción controlada en vez de que el
programa termine abruptamente. Toda excepción y error es subclase de
`Throwable`:

- `Exception` — condiciones que una aplicación razonable podría capturar
  (ej. `NullPointerException`).
- `Error` — problemas graves que no deberían capturarse (ej.
  `StackOverflowError`).

Causas típicas: pérdida de conectividad de red, datos de entrada
inválidos, archivos ausentes, límites de memoria de la JVM, errores de
código.

📎 Ver en la práctica: [Ejemplo 01 — ¿Qué es una excepción?](01-que-es-una-excepcion.md)

En una API REST, los errores (recurso inexistente, duplicado, datos
inválidos, regla de negocio incumplida) son parte normal del flujo, no
fallos inesperados; sin manejo adecuado, la API responde con `500`
genéricos. En una arquitectura en capas:

- `Repository` — errores de base de datos.
- `Service` — reglas de negocio (aquí se lanzan las excepciones).
- `Controller` — exposición HTTP (aquí, o globalmente, se manejan).

📎 Ver en la práctica: [Ejemplo 02 — Excepciones en una API REST](02-excepciones-en-una-api-rest.md)

## 🧠 Concepto: Excepciones en Spring Boot

Spring Boot ofrece tres herramientas para manejar excepciones más allá
de `try/catch`, tratándolo como una preocupación transversal:

**`@ResponseStatus`**: asocia una excepción a un código HTTP específico,
declarada sobre la clase de la excepción.

- Simple y directa; ideal para errores con un único significado HTTP
  (recurso no encontrado → `404`, conflicto por duplicado → `409`).
- No permite personalizar el cuerpo de la respuesta (usa el formato por
  defecto de Spring Boot).

📎 Ver en la práctica: [Ejemplo 03 — @ResponseStatus: recurso no encontrado](03-responsestatus-recurso-no-encontrado.md) · [Ejemplo 04 — @ResponseStatus: recurso duplicado](04-responsestatus-recurso-duplicado.md)

**`@ExceptionHandler`**: método dentro de un `@Controller`/
`@RestController` que captura y maneja una excepción específica.

- Permite controlar código HTTP, mensaje y cuerpo de la respuesta.
- Alcance **local**: solo aplica al controlador donde se declara.
- Cuando una excepción tiene tanto `@ResponseStatus` como un
  `@ExceptionHandler` que la maneja, el `@ExceptionHandler` (más
  específico) toma precedencia.

📎 Ver en la práctica: [Ejemplo 05 — @ExceptionHandler: cuerpo personalizado](05-exceptionhandler-cuerpo-personalizado.md)

**`@ControllerAdvice`**: componente que centraliza el manejo de
excepciones para toda la aplicación.

- Aplica a todos los controladores, no a uno solo.
- Evita duplicar la misma lógica de manejo en cada controlador.
- Un controlador nuevo queda cubierto automáticamente, sin código
  adicional.

📎 Ver en la práctica: [Ejemplo 06 — @ControllerAdvice: manejo global](06-controlleradvice-manejo-global.md)

**Comparación**:

| Característica | `@ResponseStatus` | `@ExceptionHandler` | `@ControllerAdvice` |
|---|---|---|---|
| Nivel de uso | Excepción | Controlador | Aplicación |
| Alcance | Puntual | Local | Global |
| Centralización | No | Parcial | Sí |
| Uso recomendado | Errores simples | Casos específicos | Manejo general |

Sin ningún mecanismo de manejo, cualquier excepción no capturada produce
un `500 Internal Server Error` genérico.

📎 Ver en la práctica: [Ejemplo 07 — Comparación de los tres mecanismos](07-comparacion-de-mecanismos.md)
