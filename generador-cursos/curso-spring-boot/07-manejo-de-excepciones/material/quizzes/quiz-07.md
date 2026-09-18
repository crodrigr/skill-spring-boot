# ❓ Quiz 07 — Manejo de Excepciones (formato entrevista técnica)

Este quiz simula las preguntas que podrías recibir en una entrevista técnica para
un puesto de desarrollador Java/Spring Boot. Cada pregunta indica su tipo
(**Selección**, **Selección múltiple** o **Abierta**). Respondé primero por tu
cuenta y después abrí "Ver respuesta" para comparar.

---

**1. [Selección]** ¿Qué es una excepción?

- **A.** Un tipo de dato primitivo de Java.
- **B.** Un evento anormal que interrumpe el flujo normal de un programa.
- **C.** Una anotación exclusiva de Spring Boot.
- **D.** Un mensaje de log impreso por la JVM.

_RA: RA-1_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Una excepción es un evento anormal que
interrumpe el flujo normal de las instrucciones de un programa.

</details>

**2. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre la jerarquía `Throwable`.

- **A.** `Throwable` es la clase base de toda la jerarquía.
- **B.** `Exception` y `Error` son las dos ramas principales de `Throwable`.
- **C.** Un `Error` indica una condición que una aplicación razonable debería intentar capturar.
- **D.** `NullPointerException` pertenece a la rama `Exception`.

_RA: RA-1_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: un `Error` indica un
problema grave que **no** debería intentar capturarse.

</details>

**3. [Selección]** Sin un manejo de excepciones adecuado, ¿con qué código
de estado suele responder una API ante un error de negocio (por ejemplo,
un recurso no encontrado)?

- **A.** `200`.
- **B.** `404`.
- **C.** `500`.
- **D.** `301`.

_RA: RA-2_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. Sin manejo específico, cualquier excepción no
capturada termina en un `500 Internal Server Error` genérico.

</details>

**4. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre dónde se originan y manejan las excepciones en una
arquitectura en capas.

- **A.** Los errores de base de datos se originan en el `Repository`.
- **B.** Las reglas de negocio se validan y sus excepciones se lanzan desde el `Service`.
- **C.** El `Controller` debería contener la lógica de validación de reglas de negocio.
- **D.** La excepción lanzada desde el `Service` se maneja en el `Controller` o globalmente.

_RA: RA-3_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: la lógica de negocio
vive en el `Service`, no en el `Controller`.

</details>

**5. [Abierta]** Un compañero te dice: "en una API REST, un recurso no
encontrado es un bug — algo salió mal".

**Pregunta**: ¿Estás de acuerdo? ¿Por qué sí o por qué no?

_RA: RA-2_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: No estoy de acuerdo. Un recurso no encontrado
(buscar un libro con un id que no existe) es una situación esperable y
normal del flujo de cualquier API REST, no un bug — cualquier cliente
puede pedir un recurso inexistente por múltiples razones legítimas (ya
fue eliminado, el id fue mal tipeado, etc.). El objetivo del manejo de
excepciones no es "evitar que esto pase" (no se puede evitar), sino
responder con un código y un mensaje claros (`404`) cuando pase.

</details>

**6. [Selección]** ¿Qué anotación permite asociar una excepción
personalizada a un código HTTP específico, sin construir la respuesta a
mano en el controlador?

- **A.** `@RequestMapping`.
- **B.** `@ResponseStatus`.
- **C.** `@Service`.
- **D.** `@Entity`.

_RA: RA-4_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. `@ResponseStatus`, declarada sobre la clase de
la excepción, hace que Spring construya la respuesta con ese código
automáticamente.

</details>

**7. [Abierta]** Un compañero crea `PedidoInvalidoException` sin
`@ResponseStatus`, la lanza desde su `Service`, y le sorprende que la API
responda `500` en vez del código que esperaba.

**Pregunta**: ¿Qué le falta, y por qué su excepción no produjo el código
esperado?

_RA: RA-4_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Le falta anotar la clase `PedidoInvalidoException`
con `@ResponseStatus(HttpStatus.X)` (el código que corresponda, por
ejemplo `BAD_REQUEST`). Sin esa anotación (ni ningún otro mecanismo de
manejo), Spring Boot no tiene forma de saber qué código HTTP le
corresponde a esa excepción, y cualquier excepción no capturada
explícitamente termina en un `500 Internal Server Error` genérico —
exactamente el problema que este módulo busca evitar.

</details>

**8. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre `@ExceptionHandler`.

- **A.** Se define dentro de un `@Controller`/`@RestController`.
- **B.** Aplica automáticamente a todos los controladores de la aplicación.
- **C.** Permite personalizar el cuerpo de la respuesta.
- **D.** Es un paso intermedio razonable antes de centralizar errores globalmente.

_RA: RA-5_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: `@ExceptionHandler`
solo aplica al controlador donde se declara, no a toda la aplicación.

</details>

**9. [Abierta]** Un compañero tiene tres controladores (`ControladorLibros`,
`ControladorAutores`, `ControladorPacientes`), cada uno con su propio
`@ExceptionHandler` casi idéntico (mismo formato de cuerpo, distinta
excepción).

**Pregunta**: ¿Qué problema tiene este diseño a medida que crece la
aplicación?

_RA: RA-5_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: La lógica de manejo de errores (el formato del
cuerpo, la estructura de la respuesta) está duplicada en tres lugares. Si
mañana se decide cambiar ese formato (por ejemplo, agregar un campo
`codigo` al cuerpo de error), habría que modificar los tres controladores
por separado, con el riesgo de olvidar alguno y dejar respuestas
inconsistentes entre distintas partes de la API. Este es exactamente el
problema que resuelve centralizar el manejo con `@ControllerAdvice`.

</details>

**10. [Selección]** Si `ControladorLibros` tiene su propio
`@ExceptionHandler` para `LibroNoEncontradoException`, y además existe un
`@ControllerAdvice` que también la maneja, ¿cuál de los dos se ejecuta
al lanzarse la excepción desde ese controlador?

- **A.** Ninguno; Spring lanza un error de configuración.
- **B.** El `@ExceptionHandler` local del controlador (más específico).
- **C.** Ambos se ejecutan, uno después del otro.
- **D.** Solo el `@ControllerAdvice`, siempre.

_RA: RA-6_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. El manejador más específico (local al
controlador) toma precedencia sobre el manejador global.

</details>

**11. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre `@ControllerAdvice`.

- **A.** Centraliza el manejo de excepciones para toda la aplicación.
- **B.** Evita duplicar la misma lógica de manejo en cada controlador.
- **C.** Es ideal para aplicaciones con un único controlador simple.
- **D.** Permite respuestas de error consistentes en toda la API.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es engañosa: `@ControllerAdvice`
es especialmente valioso cuando hay **varios** controladores, no cuando
hay uno solo (ahí, un `@ExceptionHandler` local ya alcanzaría).

</details>

**12. [Selección múltiple]** Según la tabla comparativa de los tres
mecanismos, seleccioná **todas** las afirmaciones correctas.

- **A.** `@ResponseStatus` es el mecanismo recomendado para errores simples.
- **B.** `@ExceptionHandler` tiene alcance local (a su propio controlador).
- **C.** `@ControllerAdvice` es el único con centralización total.
- **D.** Los tres mecanismos son mutuamente excluyentes: un proyecto solo puede usar uno de ellos.

_RA: RA-7_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, C**. La D es falsa: un mismo proyecto puede
combinar los tres según el caso (como el propio Módulo 7 hizo con
`Libro`).

</details>

**13. [Abierta]** Un compañero te dice: "para simplificar, voy a manejar
absolutamente todas las excepciones de mi proyecto con
`@ControllerAdvice`, incluso los casos más simples".

**Pregunta**: ¿Es una mala idea? ¿Qué le dirías?

_RA: RA-7_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: No es necesariamente una mala idea — es una opción
válida y consistente. El costo es que, para un error verdaderamente
simple con un único significado HTTP, escribir un método completo en
`@ControllerAdvice` es más código que una sola anotación
`@ResponseStatus` sobre la excepción. No es incorrecto usar
`@ControllerAdvice` para todo, pero vale la pena reconocer que
`@ResponseStatus` resuelve los casos simples con menos código, y
reservar `@ControllerAdvice` para cuando realmente aporta valor
(centralizar varios controladores con un cuerpo de error personalizado).

</details>

**14. [Selección]** Si una excepción no tiene `@ResponseStatus` y ningún
`@ExceptionHandler`/`@ControllerAdvice` la maneja, ¿con qué código
responde la API al propagarse sin capturar?

- **A.** `200 OK`.
- **B.** `404 Not Found`.
- **C.** `500 Internal Server Error`.
- **D.** La aplicación no arranca.

_RA: RA-2_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. Sin ningún mecanismo de manejo, Spring Boot
trata la excepción como un error no controlado y responde `500`.

</details>

**15. [Selección múltiple]** Sobre el ejercicio integrador que reemplaza
el manejo manual de errores de una API REST completa, seleccioná
**todas** las afirmaciones correctas.

- **A.** Ningún controlador debería seguir construyendo `ResponseEntity.notFound()` a mano al finalizar la integración.
- **B.** El Taller y el Desafío deben usar el mismo controlador para que el ejercicio sea válido.
- **C.** La excepción se lanza desde el `Service`, no desde el `Controller`.
- **D.** Todo cambio de comportamiento sobre código ya existente debe documentarse explícitamente.

_RA: RA-8_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: el Taller
(`ControladorPacientes`) y el Desafío (`ControladorCitas`) usan
controladores distintos precisamente para no ser una copia mecánica.

</details>

**16. [Abierta]** Te piden integrar excepciones personalizadas sobre una
API nueva que ya tiene diez controladores distintos, todos con el mismo
patrón de manejo manual de errores del Módulo 5.

**Pregunta**: ¿Qué combinación de mecanismos del Módulo 7 aplicarías, y
en qué orden?

_RA: RA-8_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Para cada tipo de error de negocio, crearía una
excepción personalizada con `@ResponseStatus` (el código correcto para
cada caso), modificaría los `Service` correspondientes para lanzarla en
vez de manejar el error manualmente, y centralizaría el manejo de todas
esas excepciones en un único `@ControllerAdvice` — dado que hay diez
controladores, `@ExceptionHandler` local en cada uno duplicaría la misma
lógica diez veces, exactamente el problema que `@ControllerAdvice`
resuelve. El orden sería: primero las excepciones y su
`@ResponseStatus`, después el `@ControllerAdvice` que las centraliza,
igual que se hizo en este módulo con `Libro`.

</details>

---
