# 💡 Ejemplo 01 — ¿Qué es Swagger?

## 🌍 Contexto

En el Módulo 5 construiste una API REST completa (`ControladorLibros`,
`ControladorPacientes`, `ControladorCitas`), pero nadie más que vos sabe,
sin leer el código Java, qué endpoints expone, qué reciben, ni qué
devuelven. Swagger resuelve exactamente ese problema.

**Qué busca demostrar este ejemplo**: qué es Swagger, qué es la
especificación OpenAPI, y cuáles son las seis características que hacen
de Swagger una herramienta estándar para documentar APIs REST.

## 🧠 ¿Qué es Swagger?

Swagger es un conjunto de reglas, especificaciones y herramientas
diseñadas para documentar APIs. Permite generar documentación
comprensible para cualquier persona que consulte una API, sin que esa
persona necesite leer el código fuente del servidor.

**OpenAPI** es la especificación central que usa Swagger: un estándar
(basado en JSON o YAML) para describir y documentar APIs RESTful de forma
clara y consistente — qué rutas expone una API, qué parámetros acepta
cada una, y qué respuestas puede devolver.

## 🧠 Las seis características de Swagger

| Característica | Qué aporta |
|---|---|
| **Especificación OpenAPI** | Estándar (JSON/YAML) para describir rutas, parámetros, respuestas y modelos de datos de una API. |
| **Interfaz gráfica interactiva (Swagger UI)** | Página web generada automáticamente a partir de la especificación OpenAPI, para explorar y probar la API desde el navegador. |
| **Generación automática de documentación** | La documentación se genera a partir de la especificación, sin escribirla a mano. |
| **Generación de código** | Permite generar automáticamente código cliente en distintos lenguajes para consumir la API. |
| **Validación de entradas y salidas** | La especificación define esquemas de datos, que Swagger puede usar para validar que las solicitudes y respuestas cumplen lo esperado. |
| **Integración con frameworks** | Swagger es compatible con una amplia variedad de frameworks y plataformas (incluido Spring Boot, bloque 2 de este módulo). |

## 🧭 Explicación paso a paso

1. Swagger no es un único producto: es un ecosistema (especificación +
   herramientas) alrededor de OpenAPI, la especificación que realmente
   describe la API.
2. La característica más visible para quien consulta una API es Swagger
   UI: una página web interactiva, no un simple documento de texto.
3. La generación automática de documentación y de código cliente son las
   dos características que más tiempo ahorran: sin ellas, documentar una
   API a mano (bloque 2.1 de este módulo) requiere mantener manualmente
   sincronizados el código y su documentación.
4. Este módulo se enfoca en dos de las seis características: la
   especificación OpenAPI (bloque 1) y la generación automática de
   documentación (bloque 2) — las otras cuatro se mencionan como parte
   del panorama general de Swagger, sin desarrollarlas en profundidad.

## ❓ Preguntas de repaso

**1. [Selección]** ¿Cuál es la especificación central que usa Swagger
para describir una API?

- **A.** HTML.
- **B.** OpenAPI.
- **C.** SQL.
- **D.** Markdown.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. OpenAPI es el estándar (JSON/YAML) que
describe rutas, parámetros, respuestas y modelos de datos de una API.

</details>

**2. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre Swagger UI.

- **A.** Es una interfaz gráfica interactiva.
- **B.** Se escribe completamente a mano, sin generarse a partir de nada.
- **C.** Se genera automáticamente a partir de la especificación OpenAPI.
- **D.** Permite explorar y probar la API desde el navegador.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: Swagger UI se genera
automáticamente a partir de la especificación OpenAPI, no se escribe a
mano.

</details>

**3. [Abierta]** Un compañero te dice: "documentar una API a mano en un
archivo de texto aparte ya cumple la misma función que Swagger, ¿para
qué complicarse?".

**Pregunta**: ¿Qué le responderías, pensando en las seis características
de Swagger?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Un archivo de texto aparte puede describir la API,
pero no ofrece ninguna de las otras ventajas de Swagger: no genera una
interfaz interactiva para probar los endpoints (Swagger UI), no permite
generar código cliente automáticamente, no valida entradas/salidas contra
un esquema definido, y —el problema más común en la práctica— no hay
ninguna garantía de que ese archivo de texto se actualice cuando cambia
el código real de la API. Con springdoc-openapi (bloque 2 de este
módulo), la documentación se genera directamente desde el código, así que
nunca queda desactualizada.

</details>
