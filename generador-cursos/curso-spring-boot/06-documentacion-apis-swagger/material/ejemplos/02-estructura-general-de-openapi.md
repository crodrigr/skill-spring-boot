# 💡 Ejemplo 02 — Estructura general de una definición OpenAPI

## 🌍 Contexto

Una definición OpenAPI no es un documento libre: sigue una estructura
fija, organizada en secciones con un propósito específico cada una. Antes
de generarla automáticamente (bloque 2), conviene reconocer esa
estructura leyéndola directamente.

**Qué busca demostrar este ejemplo**: identificar las tres secciones
clave de una definición OpenAPI (`paths`, `components`, `servers`) sobre
un fragmento YAML basado en `GET /libros` (Módulo 5).

## 🧠 Las tres secciones clave

| Sección | Qué describe |
|---|---|
| `paths` | Las rutas de los endpoints de la API, sus métodos HTTP permitidos, parámetros y respuestas. |
| `components` | Esquemas de datos reutilizables (por ejemplo, la forma de un `Libro`) y otros elementos compartidos entre varias rutas. |
| `servers` | Los servidores donde corre la API (por ejemplo, `localhost` en desarrollo). |

## 📚 Caso de estudio

Biblioteca Universitaria: `GET /libros` y `GET /libros/{id}`
(`ControladorLibros`, Módulo 5), representados como una definición OpenAPI
en YAML.

## 💻 Fragmento ilustrativo (YAML)

```yaml
openapi: 3.0.1
info:
  title: API de Biblioteca Universitaria
  version: "1.0"
servers:
  - url: http://localhost:8080
paths:
  /libros:
    get:
      summary: Listar todos los libros
      responses:
        '200':
          description: Lista de libros
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Libro'
  /libros/{id}:
    get:
      summary: Buscar un libro por id
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: Libro encontrado
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Libro'
        '404':
          description: Libro no encontrado
components:
  schemas:
    Libro:
      type: object
      properties:
        id:
          type: integer
        isbn:
          type: string
        titulo:
          type: string
```

## 🧭 Explicación paso a paso

1. `servers` declara dónde corre la API: en este caso, `localhost:8080`
   (el mismo puerto por defecto usado en los proyectos del curso).
2. `paths` define cada ruta (`/libros`, `/libros/{id}`) con su método HTTP
   (`get`), sus parámetros (`id`, capturado con `in: path`, equivalente a
   `@PathVariable`) y sus respuestas posibles (`200`, `404`).
3. `components.schemas.Libro` define, una sola vez, la forma del objeto
   `Libro` (sus campos y tipos); ambas rutas la reutilizan con `$ref`
   (una referencia), en vez de repetir la misma definición dos veces.
4. Este archivo YAML no se escribe a mano en este curso: en el bloque 2 se
   genera automáticamente a partir del código Java ya existente
   (`ControladorLibros`, `Libro`), sin que el estudiante escriba ninguna
   línea de YAML.

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué sección de una definición OpenAPI describe las
rutas de los endpoints de una API?

- **A.** `servers`.
- **B.** `components`.
- **C.** `paths`.
- **D.** `info`.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: C**. `paths` describe cada ruta, su método HTTP,
parámetros y respuestas.

</details>

**2. [Selección múltiple]** Sobre el fragmento YAML de este ejemplo,
seleccioná **todas** las afirmaciones correctas.

- **A.** `components.schemas.Libro` se reutiliza en más de una ruta mediante `$ref`.
- **B.** `servers` indica dónde corre la API.
- **C.** El parámetro `id` de `/libros/{id}` se declara con `in: path`.
- **D.** `paths` y `components` cumplen exactamente la misma función.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, C**. La D es falsa: `paths` describe rutas
y operaciones; `components` define esquemas reutilizables, no rutas.

</details>

**3. [Abierta]** Un compañero te muestra una definición OpenAPI donde el
esquema de `Libro` está copiado y pegado dentro de cada ruta que lo usa,
en vez de estar en `components`.

**Pregunta**: ¿Qué problema tiene ese diseño, y cómo lo corregirías?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: El problema es la duplicación: si `Libro` gana un
campo nuevo, habría que actualizar la definición en cada ruta donde
aparece copiada, con el riesgo de olvidar alguna y dejar la
documentación inconsistente entre rutas. La corrección es mover la
definición de `Libro` a `components.schemas`, una sola vez, y que cada
ruta la referencie con `$ref: '#/components/schemas/Libro'`, igual que en
este ejemplo.

</details>
