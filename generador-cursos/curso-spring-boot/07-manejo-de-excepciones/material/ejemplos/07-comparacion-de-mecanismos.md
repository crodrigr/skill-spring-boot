# 💡 Ejemplo 07 — Comparación de los tres mecanismos de manejo

## 🌍 Contexto

Los Ejemplos 03, 05 y 06 mostraron tres formas distintas de manejar
exactamente el mismo error (`LibroNoEncontradoException`). Antes de
cerrar el bloque, conviene compararlas lado a lado — y ver qué pasa
cuando no se usa ninguna.

**Qué busca demostrar este ejemplo**: comparar `@ResponseStatus`,
`@ExceptionHandler` y `@ControllerAdvice` según nivel de uso, alcance,
centralización y uso recomendado, y mostrar el resultado de no aplicar
ninguno.

## 🧠 Tabla comparativa

| Característica | `@ResponseStatus` | `@ExceptionHandler` | `@ControllerAdvice` |
|---|---|---|---|
| Nivel de uso | Excepción | Controlador | Aplicación |
| Alcance | Puntual | Local | Global |
| Centralización | No | Parcial | Sí |
| Uso recomendado | Errores simples | Casos específicos | Manejo general |

## 📚 El mismo error, tres soluciones

**Solo `@ResponseStatus`** (Ejemplo 03): código `404` correcto, cuerpo
por defecto de Spring Boot.

```text
Respuesta: 404 Not Found
{
  "timestamp": "...", "status": 404, "error": "Not Found", "path": "/libros/999"
}
```

**`@ExceptionHandler` local** (Ejemplo 05): código y cuerpo controlados,
pero solo para `ControladorLibros`.

```text
Respuesta: 404 Not Found
{
  "error": "No existe un libro con id 999"
}
```

**`@ControllerAdvice`** (Ejemplo 06): mismo código y cuerpo que el
anterior, pero aplicable a cualquier controlador de la aplicación, sin
duplicar código.

```text
Respuesta: 404 Not Found
{
  "error": "No existe un libro con id 999"
}
```

## 📚 Qué pasa sin ningún mecanismo

Si `LibroNoEncontradoException` no tuviera `@ResponseStatus`, ni ningún
`@ExceptionHandler`/`@ControllerAdvice` la manejara:

```text
Método: GET
URL: http://localhost:8080/libros/999
Respuesta: 500 Internal Server Error
{
  "timestamp": "...",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/libros/999"
}
```

## 🧭 Explicación paso a paso

1. Los tres mecanismos pueden resolver el **mismo** problema (traducir
   una excepción a una respuesta HTTP), pero difieren en cuánto control
   dan sobre el cuerpo y en qué tan ampliamente aplican.
2. `@ResponseStatus` es la opción más simple: alcanza cuando el error
   tiene un único significado HTTP claro y el cuerpo por defecto de
   Spring Boot es aceptable.
3. `@ExceptionHandler` suma control sobre el cuerpo, pero a costa de
   quedar atado a un controlador específico — útil como paso intermedio,
   o cuando un controlador necesita un tratamiento realmente particular.
4. `@ControllerAdvice` combina lo mejor de ambos: control total sobre el
   cuerpo, aplicado globalmente — la fuente lo señala como "ideal para
   aplicaciones reales", y es el punto de llegada de este módulo.
5. Sin ningún mecanismo, cualquier excepción termina en un `500`
   genérico — el problema exacto que motivó este módulo completo (Ejemplo
   02): el cliente no entiende qué ocurrió realmente.

## ❓ Preguntas de repaso

**1. [Selección]** Según la tabla comparativa, ¿cuál de los tres
mecanismos tiene alcance "global"?

- **A.** `@ResponseStatus`.
- **B.** `@ExceptionHandler`.
- **C.** `@ControllerAdvice`.
- **D.** Ninguno de los tres.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. `@ControllerAdvice` es el único de los tres
con alcance global (aplicación completa).

</details>

**2. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre la comparación de los tres mecanismos.

- **A.** `@ResponseStatus` tiene centralización "No"; se declara en cada clase de excepción por separado.
- **B.** `@ExceptionHandler` tiene centralización "Sí", igual que `@ControllerAdvice`.
- **C.** Sin ningún mecanismo de manejo, una excepción produce un `500 Internal Server Error`.
- **D.** Los tres mecanismos pueden usarse para resolver exactamente el mismo tipo de error.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: `@ExceptionHandler`
tiene centralización "Parcial" (dentro de un controlador), no "Sí" como
`@ControllerAdvice`.

</details>

**3. [Abierta]** Un compañero te pregunta: "si `@ControllerAdvice` es lo
más completo, ¿por qué no usarlo siempre para todo, y olvidarme de
`@ResponseStatus`?".

**Pregunta**: ¿Qué le responderías?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: `@ControllerAdvice` es más completo, pero también
más código para mantener: una clase aparte, un método por excepción.
Para un error verdaderamente simple, con un único significado HTTP y sin
necesidad de un cuerpo personalizado (por ejemplo, un caso de uso interno
sin demasiados clientes distintos), `@ResponseStatus` resuelve el
problema en una sola línea, sin agregar ninguna clase nueva. La elección
no es "cuál es mejor en abstracto", sino cuál mecanismo es proporcional a
la complejidad real del caso — la misma idea de proporcionalidad aplicada
en el resto del curso.

</details>
