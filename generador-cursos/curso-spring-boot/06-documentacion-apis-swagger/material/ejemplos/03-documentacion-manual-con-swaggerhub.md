# 💡 Ejemplo 03 — Documentación manual con SwaggerHub

## 🌍 Contexto

Antes de escribir código Spring Boot para generar documentación
automáticamente (bloque 2.2), existe una alternativa: escribir la
definición OpenAPI a mano, usando una herramienta como **SwaggerHub**
(el editor web oficial de Swagger). Este ejemplo resume ese flujo a nivel
conceptual — no hace falta crear una cuenta real para entender cómo
funciona.

**Qué busca demostrar este ejemplo**: el proceso general de crear una
cuenta en SwaggerHub y documentar una API manualmente (elegir una
plantilla, editar su definición, exportarla), y cuándo tendría sentido
elegir este camino en vez del automático.

## 🧠 Creación de cuenta en SwaggerHub (resumen conceptual)

SwaggerHub (`swagger.io`) es la plataforma web oficial para crear y
compartir documentación de APIs. Crear una cuenta sigue, a grandes
rasgos, estos pasos:

1. Ingresar a la página oficial de Swagger.
2. Hacer clic en "Sign In", que redirige a la pantalla de inicio de
   sesión.
3. Como no existe una cuenta todavía, elegir "Sign Up" para crear una.
4. Elegir "Continue with Google" para crear la cuenta usando una cuenta
   de Google existente (en vez de un usuario/contraseña nuevo).
5. Seleccionar la cuenta de Google deseada, entre las que el navegador ya
   tiene asociadas.
6. Autorizar que SwaggerHub use los datos básicos de esa cuenta de
   Google.
7. Completar los datos restantes del perfil (nombre, etc.).
8. Acceder al HUB principal, el panel desde donde se administran las
   APIs documentadas.

## 🧠 Codificación manual: crear y exportar una documentación

Con una cuenta ya creada, documentar una API manualmente sigue tres
pasos generales:

1. **Crear una API nueva**: desde el HUB, elegir "Create a New API",
   completar los datos básicos (nombre, versión) y elegir una plantilla
   —la plantilla "Simple API" es la más directa para empezar— y
   confirmar con "Create API".
2. **Editar la definición**: SwaggerHub abre un editor donde se escribe
   la definición OpenAPI directamente en YAML (o JSON), con vista previa
   en vivo de cómo se vería la documentación resultante.
3. **Exportar la documentación**: desde el botón "Export" del editor,
   elegir "Documentation" y, dentro de esa opción, "HTML" para descargar
   un `.zip` con una página estática lista para compartir con cualquier
   persona, sin necesidad de que tenga cuenta en SwaggerHub.

## 🧭 Explicación paso a paso

1. El punto clave de "Sign Up" con Google es que SwaggerHub no pide crear
   ni recordar una contraseña nueva: reutiliza una cuenta de Google ya
   existente.
2. La plantilla "Simple API" no es la única opción, pero sí la más
   directa para empezar a escribir una definición desde cero sin partir
   de una estructura compleja.
3. Editar en YAML (en vez de JSON) es una elección de estilo: ambos
   formatos son válidos y equivalentes para OpenAPI; YAML suele preferirse
   por ser más legible a simple vista (sin tantas llaves ni comillas).
4. Exportar como HTML produce una página **estática**: una vez generada,
   no se actualiza sola si la API cambia — hay que repetir la exportación
   manualmente. Esa es la diferencia clave frente a la codificación
   automática del bloque 2.2, que siempre refleja el estado actual del
   código.
5. Este flujo tiene sentido cuando se quiere **diseñar** la API antes de
   programarla (documentación como contrato, acordada con un equipo antes
   de escribir código), no para documentar una API que ya existe y
   cambia seguido — ese es el caso de uso de la codificación automática.

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué plantilla se usa en SwaggerHub para empezar a
documentar una API de la forma más directa?

- **A.** "Simple API".
- **B.** "Advanced API".
- **C.** "Legacy API".
- **D.** No hace falta elegir ninguna plantilla.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: A**. "Simple API" es la plantilla más directa para
empezar a escribir una definición desde cero.

</details>

**2. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre la codificación manual con SwaggerHub.

- **A.** La documentación exportada como HTML es una página estática.
- **B.** La página exportada se actualiza sola cada vez que cambia el código de la API.
- **C.** SwaggerHub permite crear la cuenta usando una cuenta de Google existente.
- **D.** La definición se edita directamente en YAML o JSON, con vista previa en vivo.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: una vez exportada, la
página HTML es estática y no refleja cambios posteriores en la API sin
volver a exportarla manualmente.

</details>

**3. [Abierta]** Un compañero de equipo te pregunta: "si SwaggerHub
también genera documentación, ¿para qué existe la codificación
automática con springdoc-openapi? ¿No es lo mismo?".

**Pregunta**: ¿Qué le responderías, distinguiendo ambos enfoques?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Ambos producen documentación OpenAPI, pero de
formas opuestas. En SwaggerHub, la persona escribe la definición a mano
(en YAML/JSON) **antes** de que exista necesariamente el código; es útil
para diseñar y acordar el contrato de una API con un equipo, pero exporta
una página estática que no se actualiza sola si la API cambia. Con
springdoc-openapi, la documentación se genera automáticamente **a partir
del código real** que ya existe (los `@RestController`), así que siempre
refleja el estado actual de la API sin ningún paso manual — pero requiere
que el código ya exista, no sirve para diseñar antes de programar.

</details>

