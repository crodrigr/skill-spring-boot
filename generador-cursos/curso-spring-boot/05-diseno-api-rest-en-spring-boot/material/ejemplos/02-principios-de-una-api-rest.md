# 💡 Ejemplo 02 — Principios de una API REST

## 🌍 Contexto

No cualquier API que use HTTP es una API REST: REST (*Representational
State Transfer*) es un conjunto de principios de diseño que, cuando se
siguen, hacen que una API sea predecible, escalable y fácil de consumir
para cualquier cliente.

**Qué busca demostrar este ejemplo**: identificar los seis principios REST
sobre un caso concreto (una futura API de `Libro` para Biblioteca
Universitaria), antes de escribir ninguna línea de código Spring.

## 🧠 Los principios de una API REST

| Principio | Qué significa | Ejemplo sobre `Libro` |
|---|---|---|
| **Sin estado** | Cada solicitud contiene toda la información que el servidor necesita; el servidor no recuerda solicitudes anteriores de ese cliente. | `GET /libros/3` siempre devuelve el mismo resultado sin importar qué solicitudes anteriores hizo el mismo cliente. |
| **Cliente-servidor** | El cliente y el servidor son entidades separadas, comunicadas por una interfaz bien definida; pueden evolucionar de forma independiente. | El cliente (una app móvil, Insomnia, un navegador) no necesita saber cómo está implementado el servidor por dentro (Java, Spring Boot, H2). |
| **Interfaz uniforme** | Se usan métodos HTTP estándar y convenciones estándar de URIs. | `GET /libros`, `POST /libros`, `PUT /libros/{id}`, `DELETE /libros/{id}` — nunca un verbo o una URL inventados para lo mismo. |
| **Basada en recursos** | Todo se modela como un recurso identificado por una URI única. | `/libros/3` identifica *a ese* libro específico; `/libros` identifica la colección completa. |
| **Sistema en capas** | Se pueden agregar componentes intermedios (balanceadores de carga, cachés, *gateways*) sin que el cliente lo note. | El cliente que llama a `/libros` no sabe (ni necesita saber) si hay un balanceador de carga entre él y el servidor. |
| **Cacheable** | Las respuestas pueden marcarse como cacheables o no, para evitar repetir trabajo innecesario. | Una respuesta de `GET /libros/3` que no cambia seguido podría cachearse; una de `POST /libros` nunca debería cachearse. |

**Código bajo demanda** (opcional, poco usado en la práctica): permite que
el servidor envíe código ejecutable al cliente. Ninguna API de este curso
lo usa; se menciona solo para completar los siete principios de la fuente
original de REST.

## 🧭 Explicación paso a paso

1. "Sin estado" es quizás el principio más importante en la práctica: si
   el servidor tuviera que recordar qué hizo cada cliente antes, escalar
   la API a muchos servidores en paralelo sería mucho más difícil (¿qué
   servidor "recuerda" a cada cliente?). Al ser sin estado, cualquier
   servidor puede atender cualquier solicitud.
2. "Basada en recursos" es lo que hace que las URLs de una API REST se
   *lean* como sustantivos (`/libros`, `/libros/3`), nunca como verbos
   (`/obtenerLibro?id=3` sería el estilo de una API no-REST).
3. Estos principios no son reglas de sintaxis que Spring Boot fuerce
   automáticamente: son decisiones de diseño que el desarrollador aplica
   al nombrar sus rutas y elegir sus verbos, tal como se verá en el
   bloque 2 de este módulo.

## ❓ Preguntas de repaso

**1. [Selección]** ¿Cuál de las siguientes describe mejor el principio
"sin estado" de REST?

- **A.** El servidor guarda un historial de todas las solicitudes de cada cliente.
- **B.** Cada solicitud contiene toda la información necesaria; el servidor no recuerda solicitudes anteriores.
- **C.** El cliente y el servidor comparten una única base de datos.
- **D.** El servidor nunca responde con un código de error.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. "Sin estado" significa que el servidor no
mantiene memoria de solicitudes anteriores de un cliente en particular.

</details>

**2. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre el principio "basado en recursos".

- **A.** Cada recurso se identifica por una URI única.
- **B.** `/libros/3` y `/libros` son URIs válidas que identifican, respectivamente, un libro puntual y la colección completa.
- **C.** Las URIs de una API REST deberían nombrarse como verbos (por ejemplo, `/obtenerLibro`).
- **D.** Los recursos se manipulan con los métodos HTTP estándar.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: en REST, las URIs se
nombran como sustantivos/recursos, no como verbos; la acción la indica el
verbo HTTP, no la URL.

</details>

**3. [Abierta]** Un compañero diseña una API con una única URL,
`/api/accion`, donde el cuerpo de cada solicitud `POST` indica con un
campo `tipo` qué operación ejecutar (`"crear"`, `"leer"`, `"actualizar"`,
`"eliminar"`).

**Pregunta**: ¿Qué principios REST no está respetando, y por qué importa?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: No respeta "interfaz uniforme" (todo pasa por
`POST`, en vez de usar `GET`/`POST`/`PUT`/`DELETE` según corresponda) ni
"basada en recursos" (una sola URL para todo, en vez de que cada recurso
tenga su propia URI). Esto importa porque cualquier herramienta o
intermediario que razone sobre HTTP estándar (un proxy, un caché, la
documentación automática de una API) no puede inferir nada mirando el
verbo o la URL: siempre es `POST /api/accion`, así que hay que leer el
cuerpo de cada solicitud para saber qué hace. Con el diseño REST estándar,
`GET /libros/3` ya comunica su intención sin necesidad de leer nada más.

</details>
