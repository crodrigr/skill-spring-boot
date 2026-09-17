# 🖥️ Presentación — Módulo 6: Documentación de APIs mediante Swagger

## Slide 1 — Título

**Módulo 6 — Documentación de APIs mediante Swagger**
Curso Spring Boot para Aplicaciones Empresariales

## Slide 2 — Objetivos del módulo

Al finalizar, vas a poder documentar automáticamente tu API REST con
Swagger/OpenAPI, sin escribir YAML a mano ni modificar tus controladores.

## Slide 3 — Ruta de la sesión

2 bloques: ¿Qué es Swagger? → Creación de Documentación API.

## Slide 4 — De una API sin documentar a una navegable

En el Módulo 5 construiste `ControladorLibros`, `ControladorPacientes` y
`ControladorCitas`. Nadie más que vos sabe qué exponen sin leer el
código — hasta hoy.

## Slide 5 — ¿Qué es Swagger?

Un conjunto de reglas, especificaciones y herramientas para documentar
APIs, basado en la especificación **OpenAPI**.

## Slide 6 — Las seis características de Swagger

Especificación OpenAPI · Swagger UI · generación automática de
documentación · generación de código · validación de entradas/salidas ·
integración con frameworks.

## Slide 7 — Este módulo se enfoca en dos

La especificación OpenAPI (bloque 1) y la generación automática de
documentación (bloque 2).

## Slide 8 — Actividad práctica: Ejemplo 01 y Básico 01

Identificar qué característica de Swagger resuelve cada escenario dado.

## Slide 9 — Estructura general de una definición OpenAPI

```text
paths       → rutas, métodos HTTP, parámetros, respuestas
components  → esquemas de datos reutilizables
servers     → dónde corre la API
```

## Slide 10 — `$ref`: evitar duplicar esquemas

Cada ruta en `paths` referencia el esquema definido una sola vez en
`components`, en vez de repetirlo.

## Slide 11 — Actividad práctica: Ejemplo 02 y Básico 02

Identificar `paths`, `components` y `servers` en un fragmento OpenAPI
dado.

## Slide 12 — Documentación manual: SwaggerHub

Plataforma web oficial para escribir una definición OpenAPI a mano, en
YAML o JSON.

## Slide 13 — Crear cuenta en SwaggerHub (panorama)

Sign In → Sign Up → Continue with Google → autorizar → completar datos →
HUB principal.

## Slide 14 — Codificación manual: crear, editar, exportar

Plantilla "Simple API" → editor YAML con vista previa → exportar como
HTML (página estática).

## Slide 15 — Este curso no requiere una cuenta real

SwaggerHub se trata como contenido conceptual; el foco práctico y
evaluable es la codificación automática.

## Slide 16 — Actividad práctica: Ejemplo 03

Explicar el proceso conceptual de documentación manual en SwaggerHub.

## Slide 17 — Codificación automática: la dependencia

`springdoc-openapi-starter-webmvc-ui`, agregada al `pom.xml` de un
proyecto que ya tiene controladores REST.

## Slide 18 — Ninguna clase Java cambia

Springdoc detecta automáticamente los `@RestController` existentes, sin
anotaciones adicionales.

## Slide 19 — Actividad práctica: Ejemplo 04 e Intermedio 01

Agregar springdoc-openapi a un `pom.xml` dado.

## Slide 20 — Configurar `application.properties`

`springdoc.api-docs.enabled` · `springdoc.swagger-ui.enabled` ·
`springdoc.swagger-ui.path` · `springdoc.packages-to-scan`.

## Slide 21 — `packages-to-scan`: el paquete correcto importa

Si apunta a un paquete sin controladores, Swagger UI carga vacía, sin
ningún error visible.

## Slide 22 — Actividad práctica: Ejemplo 05, Intermedio 02 y Avanzado 01

Configurar `application.properties`; diagnosticar por qué Swagger UI no
muestra los endpoints esperados.

## Slide 23 — Viendo la documentación generada

Cada endpoint, sus parámetros y sus códigos de respuesta, extraídos
directamente del código.

## Slide 24 — El esquema respeta `@JsonIgnore`

Un campo ignorado por Jackson (como `Paciente.citas`, Módulo 5) tampoco
aparece en el esquema documentado.

## Slide 25 — Por qué eso importa

La documentación automática nunca puede mentir sobre lo que la API
realmente devuelve.

## Slide 26 — Actividad práctica: Ejemplo 06 y Básico 03

Ver la documentación generada; elegir entre documentación manual y
automática según el escenario.

## Slide 27 — Manual vs. automática: cuándo usar cada una

Manual: diseñar antes de programar. Automática: documentar código que ya
existe, sin mantenimiento manual.

## Slide 28 — Actividad práctica: Taller 01 y Desafío 01

Taller: documentar `ControladorPacientes`. Desafío: documentar
`ControladorCitas`, verificando el esquema con `@JsonIgnore`.

## Slide 29 — Resumen del módulo

Qué es Swagger/OpenAPI → estructura de una definición → documentación
manual (panorama) → dependencia y configuración → documentación generada.

## Slide 30 — Evaluación y cierre

Quiz de 14 ítems + 6 ejercicios (incluido 1 Desafío) + 1 Taller,
cubriendo los 8 resultados de aprendizaje. El curso continúa con
seguridad de APIs en un módulo posterior.
