# ❓ Quiz 06 — Documentación de APIs mediante Swagger (formato entrevista técnica)

Este quiz simula las preguntas que podrías recibir en una entrevista técnica para
un puesto de desarrollador Java/Spring Boot. Cada pregunta indica su tipo
(**Selección**, **Selección múltiple** o **Abierta**). Respondé primero por tu
cuenta y después abrí "Ver respuesta" para comparar.

---

**1. [Selección]** ¿Qué es Swagger?

- **A.** Un lenguaje de programación para APIs.
- **B.** Un conjunto de reglas, especificaciones y herramientas para documentar APIs.
- **C.** Una base de datos para almacenar documentación.
- **D.** Un framework para crear controladores REST.

_RA: RA-1_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Swagger es el ecosistema de especificaciones y
herramientas (basado en OpenAPI) para documentar APIs.

</details>

**2. [Selección]** ¿Cuál de las siguientes es una de las seis
características de Swagger?

- **A.** Generación automática de tests unitarios.
- **B.** Validación de entradas y salidas.
- **C.** Compilación de código Java.
- **D.** Gestión de bases de datos.

_RA: RA-2_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. La especificación OpenAPI permite definir
esquemas de datos que Swagger usa para validar entradas y salidas.

</details>

**3. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre las secciones de una definición OpenAPI.

- **A.** `paths` describe las rutas de los endpoints.
- **B.** `components` define esquemas de datos reutilizables.
- **C.** `servers` describe los esquemas de las entidades.
- **D.** `paths` y `components` pueden combinarse mediante `$ref` para evitar duplicar un esquema en cada ruta.

_RA: RA-3_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: `servers` describe
dónde corre la API, no los esquemas de las entidades (eso es
`components`).

</details>

**4. [Abierta]** Un compañero te dice: "no entiendo para qué sirve
`components` si ya tengo `paths` con toda la información de cada
endpoint".

**Pregunta**: ¿Qué le responderías, distinguiendo el propósito de cada
sección?

_RA: RA-3_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: `paths` describe **cada ruta** por separado (su
método HTTP, parámetros, respuestas), pero varias rutas suelen compartir
la misma forma de datos (por ejemplo, `Libro` aparece tanto en `GET
/libros` como en `GET /libros/{id}`). `components` centraliza esos
esquemas reutilizables en un solo lugar, y cada ruta en `paths` los
referencia con `$ref` en vez de repetir la definición completa —
evitando que un cambio en el esquema de `Libro` obligue a actualizarlo en
cada ruta donde aparece.

</details>

**5. [Selección]** En SwaggerHub, ¿qué produce la opción de exportar como
"Documentation" en formato HTML?

- **A.** Un proyecto Spring Boot completo, listo para ejecutar.
- **B.** Una página web estática con la documentación, que no se actualiza sola.
- **C.** Un archivo `.java` con los controladores de la API.
- **D.** Una base de datos con los datos de ejemplo de la API.

_RA: RA-4_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Exportar como HTML genera una página estática;
si la API cambia después, hay que volver a exportarla manualmente.

</details>

**6. [Abierta]** Un compañero está por empezar un proyecto nuevo y te
pregunta si debería documentar su API en SwaggerHub antes de escribir
ningún código, o esperar a tener los controladores listos y usar
springdoc-openapi.

**Pregunta**: ¿Qué le recomendarías, según en qué etapa está el
proyecto?

_RA: RA-4_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Si el objetivo es **diseñar y acordar** el
contrato de la API con un equipo antes de programar nada (qué endpoints
va a tener, qué reciben y devuelven), SwaggerHub es la opción indicada:
permite escribir y discutir la definición sin que exista todavía ningún
código. Si el proyecto ya tiene controladores REST funcionando y lo que
se necesita es documentarlos sin esfuerzo manual y sin riesgo de que la
documentación quede desactualizada, springdoc-openapi (bloque 2.2) es la
opción correcta.

</details>

**7. [Selección]** ¿Qué dependencia Maven habilita springdoc-openapi con
Swagger UI incluido en un proyecto Spring Boot?

- **A.** `spring-boot-starter-web`.
- **B.** `spring-boot-starter-data-jpa`.
- **C.** `springdoc-openapi-starter-webmvc-ui`.
- **D.** `spring-boot-starter-validation`.

_RA: RA-5_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. `springdoc-openapi-starter-webmvc-ui` trae
springdoc-openapi junto con Swagger UI empaquetado.

</details>

**8. [Abierta]** Un compañero agrega `springdoc-openapi-starter-webmvc-ui`
a su `pom.xml`, pero su proyecto solo tiene `spring-boot-starter-data-jpa`
(sin `spring-boot-starter-web`), y no tiene ningún `@RestController`.

**Pregunta**: ¿Qué documentación generaría springdoc en ese caso, y por
qué?

_RA: RA-5_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Prácticamente ninguna documentación útil, porque
springdoc-openapi documenta específicamente los `@RestController` de
Spring MVC — sin `spring-boot-starter-web`, ni siquiera podría declarar
uno. La dependencia por sí sola no genera nada que documentar: necesita
que el proyecto tenga controladores REST reales para escanear.

</details>

**9. [Selección]** ¿Cuál de las siguientes propiedades le dice a
springdoc en qué paquete buscar los controladores a documentar?

- **A.** `springdoc.api-docs.enabled`.
- **B.** `springdoc.swagger-ui.path`.
- **C.** `springdoc.packages-to-scan`.
- **D.** `spring.datasource.url`.

_RA: RA-6_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. `springdoc.packages-to-scan` indica dónde
buscar los `@RestController`.

</details>

**10. [Selección múltiple]** Un proyecto tiene `springdoc.packages-to-scan`
apuntando a un paquete que no contiene ningún `@RestController`.
Seleccioná **todas** las afirmaciones correctas.

- **A.** Swagger UI puede seguir cargando correctamente como interfaz.
- **B.** El proyecto necesariamente falla al arrancar.
- **C.** No aparecerá ningún endpoint documentado.
- **D.** La corrección es apuntar `packages-to-scan` al paquete real de los controladores.

_RA: RA-7_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: una configuración
incorrecta de `packages-to-scan` no impide que el proyecto arranque, solo
deja la documentación vacía.

</details>

**11. [Abierta]** Un compañero te pregunta: "si `Paciente.citas` tiene
`@JsonIgnore` y por eso no aparece en las respuestas JSON reales, ¿por
qué debería importarme que tampoco aparezca en el esquema de Swagger?".

**Pregunta**: ¿Qué le responderías?

_RA: RA-7_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Le respondería que es justamente lo esperable, y
por una buena razón: si Swagger mostrara `citas` en el esquema pero la
respuesta real nunca lo incluyera, la documentación estaría mintiendo
sobre lo que la API realmente devuelve — alguien que la consulte
esperaría ese campo y nunca lo recibiría. Que springdoc respete
`@JsonIgnore` es justamente lo que garantiza que la documentación
generada automáticamente sea siempre fiel al comportamiento real de la
API.

</details>

**12. [Selección]** ¿Cuál es la ventaja principal de la documentación
automática (springdoc-openapi) frente a la manual (SwaggerHub) para una
API que ya tiene código funcionando?

- **A.** Permite diseñar la API antes de escribir código.
- **B.** Nunca puede desincronizarse del código real, porque se genera a partir de él.
- **C.** No requiere ninguna dependencia adicional.
- **D.** Genera una página HTML estática para compartir sin depender del proyecto en ejecución.

_RA: RA-8_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. La documentación automática se deriva
directamente del código, así que siempre refleja su estado actual sin
mantenimiento manual.

</details>

**13. [Selección múltiple]** Sobre el ejercicio integrador que documenta
automáticamente una API REST completa ya construida, seleccioná
**todas** las afirmaciones correctas.

- **A.** No hace falta modificar ninguna clase Java existente para documentarla.
- **B.** Si la API tiene una relación con `@JsonIgnore` ya resuelta, ese mismo campo queda excluido del esquema documentado.
- **C.** El Taller y el Desafío deben documentar el mismo controlador para que el ejercicio sea válido.
- **D.** La configuración necesaria es la misma dependencia y las mismas cuatro propiedades, cambiando solo el paquete a escanear.

_RA: RA-7_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: el Taller y el Desafío
usan controladores distintos (`ControladorPacientes` y
`ControladorCitas`) precisamente para no ser una copia mecánica.

</details>

**14. [Abierta]** Te piden documentar automáticamente una API nueva que
tiene una entidad `Factura` con una relación hacia `Paciente`, donde
`Paciente` **no** tiene ningún campo que apunte de vuelta a `Factura`.

**Pregunta**: ¿Esperarías encontrar algún campo excluido en el esquema
generado por `@JsonIgnore`, como pasó con `Cita`↔`Paciente`? ¿Por qué sí
o por qué no?

_RA: RA-7_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: No necesariamente — `@JsonIgnore` se usó en
`Paciente.citas` específicamente para resolver la recursión infinita de
una relación **bidireccional** (`Cita`↔`Paciente`, con `mappedBy`). Si
`Paciente` no declara ningún campo que apunte de vuelta a `Factura`, no
hay ningún ciclo que romper, así que no habría ninguna razón para usar
`@JsonIgnore` en esta relación, y el esquema documentado incluiría todos
los campos sin exclusiones.

</details>

---
