# 🖥️ Presentación — Módulo 7: Manejo de Excepciones

## Slide 1 — Título

**Módulo 7 — Manejo de Excepciones**
Curso Spring Boot para Aplicaciones Empresariales

## Slide 2 — Objetivos del módulo

Al finalizar, vas a poder reemplazar el manejo manual de errores de tu
API REST por excepciones personalizadas y un manejo profesional,
consistente en toda la aplicación.

## Slide 3 — Ruta de la sesión

2 bloques: ¿Qué es una excepción? → Excepciones en Spring Boot.

## Slide 4 — El problema de hoy

`ControladorLibros.buscarPorId` construye `ResponseEntity.notFound()` a
mano. Funciona, pero no escala a medida que crecen los controladores.

## Slide 5 — ¿Qué es una excepción?

Un evento anormal que interrumpe el flujo normal de un programa; su
manejo permite tomar una acción controlada en vez de terminar
abruptamente.

## Slide 6 — La jerarquía `Throwable`

```text
Throwable
├── Exception   (capturable: NullPointerException)
└── Error       (no debería capturarse: StackOverflowError)
```

## Slide 7 — Causas típicas de excepciones

Conectividad de red · datos inválidos · archivos ausentes · límites de
memoria de la JVM · errores de código.

## Slide 8 — Actividad práctica: Ejemplo 01 y Básico 01

Clasificar clases según pertenezcan a `Exception` o `Error`.

## Slide 9 — Por qué importa en una API REST

Recurso inexistente, duplicado, datos inválidos, regla de negocio
incumplida: son parte normal del flujo, no fallos inesperados.

## Slide 10 — Sin manejo adecuado

La API responde `500` genéricos, el cliente no entiende qué ocurrió, y el
código se llena de `try/catch` innecesarios.

## Slide 11 — Dónde se producen los errores en capas

```text
Repository → errores de base de datos
Service    → reglas de negocio (acá se lanza la excepción)
Controller → exposición HTTP (acá, o global, se maneja)
```

## Slide 12 — Actividad práctica: Ejemplo 02 y Básico 02

Identificar en qué capa se origina un error dado.

## Slide 13 — Las tres herramientas de Spring Boot

`@ResponseStatus` · `@ExceptionHandler` · `@ControllerAdvice`.

## Slide 14 — `@ResponseStatus`

Asocia una excepción a un código HTTP, declarada sobre la clase de la
excepción. Simple, ideal para errores con un único significado HTTP.

## Slide 15 — `LibroNoEncontradoException` → 404

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LibroNoEncontradoException extends RuntimeException { ... }
```

## Slide 16 — Una regla de negocio nueva: duplicados

`LibroDuplicadoException` → `409 Conflict`, verificando `findByIsbn`
antes de guardar.

## Slide 17 — Actividad práctica: Ejemplo 03-04 e Intermedio 01

Crear una excepción personalizada con `@ResponseStatus` para un
escenario dado.

## Slide 18 — Límite de `@ResponseStatus`

Resuelve el código, pero el cuerpo sigue siendo el que Spring Boot genera
por defecto (`timestamp`, `status`, `error`, `path`).

## Slide 19 — `@ExceptionHandler`

Método dentro de un controlador que captura una excepción y controla
código, mensaje y cuerpo de la respuesta — alcance **local**.

## Slide 20 — Precedencia

Si una excepción tiene `@ResponseStatus` y también un
`@ExceptionHandler` que la maneja, el `@ExceptionHandler` (más
específico) gana.

## Slide 21 — Actividad práctica: Ejemplo 05 e Intermedio 02

Agregar un `@ExceptionHandler` a un controlador para personalizar el
cuerpo de la respuesta.

## Slide 22 — El problema de duplicar `@ExceptionHandler`

Cada controlador nuevo repetiría la misma lógica de manejo de errores.

## Slide 23 — `@ControllerAdvice`

Centraliza el manejo de excepciones para **toda** la aplicación — "ideal
para aplicaciones reales".

## Slide 24 — Un controlador nuevo, cobertura automática

Si se agrega un controlador que lanza la misma excepción, ya queda
cubierto sin código adicional.

## Slide 25 — Actividad práctica: Ejemplo 06 y Avanzado 01

Centralizar en `@ControllerAdvice` un manejo antes duplicado en varios
controladores.

## Slide 26 — Comparación de los tres mecanismos

| | `@ResponseStatus` | `@ExceptionHandler` | `@ControllerAdvice` |
|---|---|---|---|
| Alcance | Puntual | Local | Global |
| Centralización | No | Parcial | Sí |

## Slide 27 — Y si no se usa ninguno

Cualquier excepción no capturada produce un `500 Internal Server Error`
genérico — el problema original, sin resolver.

## Slide 28 — Actividad práctica: Ejemplo 07, Intermedio 03 y Avanzado 02

Comparar los tres mecanismos; diagnosticar una excepción sin manejo.

## Slide 29 — Elegir el mecanismo correcto

No hay un "mejor" absoluto: la elección depende de cuántos controladores
y qué tan específico es el error.

## Slide 30 — Actividad práctica: Taller 01 y Desafío 01

Taller: excepciones + `@ControllerAdvice` sobre `Paciente`. Desafío:
mismo patrón sobre `Cita`.

## Slide 31 — Documentar cada cambio

Todo cambio de comportamiento sobre código del Módulo 5 se documenta
explícitamente: qué hacía antes, qué hace ahora.

## Slide 32 — Resumen del módulo

Qué es una excepción → por qué y dónde se maneja → `@ResponseStatus` →
`@ExceptionHandler` → `@ControllerAdvice` → comparación → integración.

## Slide 33 — Evaluación

Quiz de 16 ítems + 7 ejercicios (incluido 1 Desafío) + 1 Taller,
cubriendo los 8 resultados de aprendizaje del módulo.

## Slide 34 — Lo que no cubrimos

La validación automática de datos de entrada mediante anotaciones (Bean
Validation): queda para un módulo posterior del curso.

## Slide 35 — Próximo módulo

El curso continúa construyendo sobre esta API ya robusta, con manejo de
errores profesional en cada capa.
