# ❓ Quiz 05 — Diseño API REST en Spring Boot (formato entrevista técnica)

Este quiz simula las preguntas que podrías recibir en una entrevista técnica para
un puesto de desarrollador Java/Spring Boot. Cada pregunta indica su tipo
(**Selección**, **Selección múltiple** o **Abierta**). Respondé primero por tu
cuenta y después abrí "Ver respuesta" para comparar.

---

**1. [Selección]** ¿Cuál de las siguientes describe mejor qué es una API?

- **A.** Un lenguaje de programación.
- **B.** Un conjunto de reglas que permite que dos componentes de software se comuniquen.
- **C.** Una base de datos relacional.
- **D.** Un protocolo exclusivo de Internet.

_RA: RA-1_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Esa es la definición general de API; puede ser
local (dentro del mismo proceso) o remota/web (sobre una red).

</details>

**2. [Selección]** ¿Qué verbo HTTP se usa para solicitar información sin
modificar ningún dato?

- **A.** `GET`.
- **B.** `POST`.
- **C.** `DELETE`.
- **D.** `PUT`.

_RA: RA-2_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: A**. `GET` es de solo lectura; los demás verbos
modifican el estado del servidor.

</details>

**3. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre los rangos de códigos de estado HTTP.

- **A.** Un código `2xx` indica éxito.
- **B.** Un código `4xx` indica un error del servidor.
- **C.** Un código `5xx` indica un error del servidor.
- **D.** Un código `404` pertenece al rango `4xx`.

_RA: RA-3_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: `4xx` es error del
**cliente**, no del servidor.

</details>

**4. [Abierta]** Un compañero te dice: "para mi API, decidí que todas las
operaciones (crear, leer, actualizar, eliminar) se hagan con `POST`,
total el servidor entiende igual qué hacer según el cuerpo del JSON".

**Pregunta**: ¿Qué principios de una API REST no está respetando, y por
qué eso puede ser un problema en la práctica?

_RA: RA-4_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: No respeta la "interfaz uniforme" (usar los verbos
HTTP estándar según la operación) ni, según cómo nombre sus URLs, es
probable que tampoco respete estar "basada en recursos". El problema
práctico es que cualquier herramienta, proxy, o documentación automática
que razone sobre el verbo HTTP para inferir la intención de la solicitud
deja de funcionar: todo se ve igual (`POST`), y hay que leer el cuerpo de
cada solicitud para saber qué hace en realidad.

</details>

**5. [Selección]** En la arquitectura en capas de Spring, ¿qué capa es
responsable de la lógica de negocio?

- **A.** `Controller`.
- **B.** `Service`.
- **C.** `Repository`.
- **D.** `Database`.

_RA: RA-5_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. `Service` encapsula la lógica de negocio;
`Controller` maneja HTTP y `Repository` accede a datos.

</details>

**6. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre MVC y su relación con Spring.

- **A.** En una API REST, el cuerpo JSON de la respuesta cumple el rol que tenía la Vista en MVC tradicional.
- **B.** El `Controller` de Spring nunca debería depender del `Service`.
- **C.** Cada capa de la arquitectura de Spring solo debería comunicarse con la capa inmediatamente inferior.
- **D.** MVC es un patrón exclusivo de las APIs REST, no existía antes.

_RA: RA-5_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C**. La B es falsa: el `Controller` sí debe
depender del `Service` (para delegar la lógica de negocio); la D es falsa:
MVC es un patrón general, anterior a las APIs REST.

</details>

**7. [Selección]** ¿Por qué `ServicioLibros.actualizar(...)` devuelve
`Optional<Libro>` en vez de lanzar una excepción cuando el `id` no existe?

- **A.** Porque `Optional` es obligatorio en cualquier método de un `@Service`.
- **B.** Para dejar en manos de quien llama (el `Controller`) la decisión de qué código de estado HTTP devolver.
- **C.** Porque `JpaRepository` no permite lanzar excepciones.
- **D.** No hay ninguna razón particular; es una elección de estilo sin consecuencias.

_RA: RA-6_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. La capa `Service` no conoce HTTP; devolver
`Optional` deja esa decisión (por ejemplo, `404`) en manos del
`Controller`.

</details>

**8. [Abierta]** Un compañero escribe su `@Service` inyectando
`RepositorioLibros` con `@Autowired` sobre un campo, en vez de recibirlo
por constructor.

**Pregunta**: ¿Qué le recomendarías, y por qué?

_RA: RA-6_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Le recomendaría inyección por constructor, la misma
forma usada desde el Módulo 1 del curso. Con inyección por constructor, la
dependencia puede declararse `final` (inmutable una vez creado el objeto),
la clase puede probarse fácilmente pasando un repositorio de prueba sin
necesitar el contenedor de Spring, y queda explícito en la firma del
constructor qué necesita la clase para funcionar — con `@Autowired` sobre
un campo, esa dependencia queda oculta hasta que se lee el cuerpo de la
clase.

</details>

**9. [Selección]** ¿Qué anotación se usa para convertir el cuerpo JSON de
una solicitud `POST` en un objeto Java?

- **A.** `@PathVariable`.
- **B.** `@RequestParam`.
- **C.** `@RequestBody`.
- **D.** `@ResponseBody`.

_RA: RA-7_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. `@RequestBody` convierte el cuerpo JSON de la
solicitud en el objeto Java del parámetro.

</details>

**10. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre los códigos de estado en un controlador REST.

- **A.** Un endpoint `POST` que crea un recurso exitosamente debería devolver `201`, no `200`.
- **B.** Un endpoint que busca un recurso por id y no lo encuentra debería devolver `200` con un cuerpo vacío.
- **C.** Usar `ResponseEntity<T>` permite elegir explícitamente el código de estado de la respuesta.
- **D.** Un endpoint `DELETE` exitoso puede devolver `200` (con o sin cuerpo) o `204` (sin cuerpo).

_RA: RA-7_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: cuando el recurso no
existe, el código correcto es `404`, no `200`.

</details>

**11. [Abierta]** Un compañero te muestra un endpoint `DELETE` que
siempre devuelve `200 OK`, incluso cuando el `id` que se le pasa no
corresponde a ningún registro existente.

**Pregunta**: ¿Qué le falta a ese endpoint, y cómo lo corregirías?

_RA: RA-8_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Le falta verificar si el recurso existía antes de
intentar eliminarlo (o verificar el resultado de esa verificación) y
devolver `404` cuando no existía. La corrección sigue el mismo patrón que
`ServicioLibros.eliminar(...)`: el servicio devuelve `boolean` indicando
si había algo para eliminar, y el controlador traduce ese resultado a
`200` (existía) o `404` (no existía), en vez de asumir siempre éxito.

</details>

**12. [Selección]** Al probar una API, ¿por qué conviene probar un
`DELETE` seguido de un `GET` sobre el mismo recurso, en vez de probar
solo el `DELETE`?

- **A.** Porque Insomnia exige encadenar solicitudes.
- **B.** Para confirmar que el recurso realmente desapareció, no solo que el `DELETE` respondió con éxito.
- **C.** Porque un `GET` siempre debe ejecutarse antes que un `DELETE`.
- **D.** No hay ninguna razón real; es solo una convención sin valor práctico.

_RA: RA-9_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. El `GET` posterior confirma, con un `404`, que
el recurso efectivamente ya no existe.

</details>

**13. [Abierta]** Un compañero prueba su API únicamente con los casos de
éxito (todas las solicitudes bien formadas, todos los ids existentes).

**Pregunta**: ¿Qué le falta a su plan de pruebas, y por qué importa
probar también los casos de error?

_RA: RA-9_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Le falta probar al menos un caso de error (por
ejemplo, un `id` inexistente esperando `404`, o una solicitud mal
formada). Los casos de error importan porque es ahí donde suelen
esconderse los bugs reales — un endpoint puede funcionar perfectamente en
el camino feliz y, aun así, devolver un código de estado incorrecto (por
ejemplo, `200` en vez de `404`) cuando algo no sale como se espera, como
se vio en el Ejercicio Avanzado 01.

</details>

**14. [Selección múltiple]** Sobre la recursión infinita al serializar a
JSON una relación bidireccional (`Cita`↔`Paciente`), seleccioná **todas**
las afirmaciones correctas.

- **A.** Ocurre porque `Cita` serializa su `Paciente`, y `Paciente` vuelve a serializar sus `Cita`, sin fin.
- **B.** Se resuelve quitando por completo la relación entre `Cita` y `Paciente`.
- **C.** `@JsonIgnore` sobre `Paciente.citas` corta el ciclo, sin afectar el mapeo JPA de la relación.
- **D.** El problema solo aparece porque la relación es bidireccional (con `mappedBy`); una relación unidireccional no lo tendría.

_RA: RA-10_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: no hace falta eliminar
la relación, alcanza con evitar que **ambos** lados se serialicen
mutuamente.

</details>

**15. [Abierta]** Un compañero soluciona el problema de recursión
infinita agregando `@JsonIgnore` directamente sobre `Cita.getPaciente()`,
en vez de sobre `Paciente.getCitas()`.

**Pregunta**: ¿Qué diferencia práctica tiene esa elección, y cuál
preferirías vos?

_RA: RA-10_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Técnicamente ambas opciones cortan el ciclo (con
que uno de los dos lados no se serialice, alcanza), pero tienen efectos
distintos: ignorar `Cita.getPaciente()` hace que la respuesta de `/citas`
nunca incluya los datos del paciente asociado, mientras que ignorar
`Paciente.getCitas()` (la opción de este ejercicio) permite que `/citas`
siga mostrando su paciente, y solo `/pacientes` deja de mostrar la lista
de citas. La elección depende de qué información necesita cada endpoint;
en este caso, tiene más sentido que una cita muestre a su paciente que al
revés.

</details>

**16. [Selección múltiple]** Sobre el ejercicio integrador que combina
`Service`, `Controller` REST completo y pruebas en Insomnia sobre una
entidad ya persistida, seleccioná **todas** las afirmaciones correctas.

- **A.** El `Controller` debe inyectar el `Service`, no el `Repository` directamente.
- **B.** Si la entidad tiene una relación bidireccional, hay que resolver la recursión infinita de JSON antes de que sus endpoints puedan devolver una respuesta válida.
- **C.** Probar solo los casos de éxito alcanza para dar por terminado el ejercicio.
- **D.** Cada endpoint debe devolver el código de estado correcto según el resultado de la operación (éxito o recurso no encontrado).

_RA: RA-11_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: probar solo el camino
feliz deja sin verificar los casos donde suelen aparecer los bugs reales
(como códigos de estado incorrectos).

</details>

**17. [Abierta]** Te piden construir una API REST completa para una
entidad nueva, `Consulta` (con una relación `@ManyToOne` hacia
`Paciente`, sin relación inversa declarada en `Paciente`).

**Pregunta**: ¿Hace falta `@JsonIgnore` en este caso? ¿Por qué sí o por
qué no?

_RA: RA-11_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: No hace falta, porque el problema de recursión
infinita ocurre específicamente cuando la relación es **bidireccional**
(ambos lados se referencian mutuamente, como `Cita`↔`Paciente` con
`mappedBy`). Si `Paciente` no declara ningún campo que apunte de vuelta a
`Consulta`, serializar una `Consulta` a JSON solo recorre `Consulta` →
`paciente`, sin ningún camino de regreso — no hay ciclo que cortar.

</details>

---
