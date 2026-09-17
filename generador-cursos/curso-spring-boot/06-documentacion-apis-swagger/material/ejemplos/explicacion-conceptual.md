# 📚 Explicación conceptual — Módulo 6

## 🧠 Concepto: ¿Qué es Swagger?

Swagger es un conjunto de reglas, especificaciones y herramientas para
documentar APIs, basado en la especificación **OpenAPI** (JSON/YAML).
Sus seis características principales:

- Especificación OpenAPI (estándar para describir la API).
- Swagger UI (interfaz gráfica interactiva, generada automáticamente).
- Generación automática de documentación.
- Generación de código cliente.
- Validación de entradas y salidas.
- Integración con frameworks y plataformas (incluido Spring Boot).

📎 Ver en la práctica: [Ejemplo 01 — ¿Qué es Swagger?](01-que-es-swagger.md)

Una definición OpenAPI se organiza en tres secciones clave:

- `paths` — rutas de los endpoints, métodos HTTP, parámetros y respuestas.
- `components` — esquemas de datos reutilizables entre varias rutas.
- `servers` — dónde corre la API.

📎 Ver en la práctica: [Ejemplo 02 — Estructura general de una definición OpenAPI](02-estructura-general-de-openapi.md)

## 🧠 Concepto: Creación de Documentación API

Documentar una API a mano en **SwaggerHub** sigue tres pasos generales:
crear una cuenta (puede usarse una cuenta de Google existente), crear una
API nueva desde una plantilla (por ejemplo, "Simple API") y editar su
definición en YAML/JSON con vista previa en vivo, y exportarla como una
página HTML estática para compartirla.

📎 Ver en la práctica: [Ejemplo 03 — Documentación manual con SwaggerHub](03-documentacion-manual-con-swaggerhub.md)

La codificación automática agrega una única dependencia a un proyecto
Spring Boot ya existente: `springdoc-openapi-starter-webmvc-ui`. No hace
falta modificar ninguna clase Java ni agregar ninguna anotación: springdoc
detecta automáticamente los `@RestController` ya existentes.

📎 Ver en la práctica: [Ejemplo 04 — Agregando springdoc-openapi](04-agregando-springdoc-openapi.md)

Cuatro propiedades de `application.properties` configuran springdoc:

| Propiedad | Para qué |
|---|---|
| `springdoc.api-docs.enabled` | Habilita el documento OpenAPI en JSON (`/v3/api-docs`) |
| `springdoc.swagger-ui.enabled` | Habilita la interfaz Swagger UI |
| `springdoc.swagger-ui.path` | Ruta donde se sirve Swagger UI |
| `springdoc.packages-to-scan` | Paquete(s) donde springdoc busca los `@RestController` |

📎 Ver en la práctica: [Ejemplo 05 — Configurando application.properties](05-configurando-application-properties.md)

Springdoc genera la documentación (endpoints, parámetros, esquemas) leyendo
directamente las anotaciones y tipos del código, sin necesitar ninguna
anotación de personalización de OpenAPI ni ningún archivo YAML manual. El esquema de una
entidad respeta las mismas anotaciones de Jackson usadas para
serializarla a JSON: un campo con `@JsonIgnore` tampoco aparece en el
esquema documentado. Frente a la documentación manual (SwaggerHub), la
automática nunca puede desincronizarse del código real.

📎 Ver en la práctica: [Ejemplo 06 — Viendo la documentación generada](06-viendo-la-documentacion-generada.md)
