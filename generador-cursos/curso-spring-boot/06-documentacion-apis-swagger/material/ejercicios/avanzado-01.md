# 🔴 Avanzado 01 — Diagnosticar por qué Swagger UI no muestra los endpoints esperados

## 🧩 Problema

Un compañero de equipo te dice: "agregué springdoc-openapi y configuré
todo, pero Swagger UI carga vacía, sin ningún endpoint de mi
`ControladorAutores`".

## 💻 Código o contexto de partida

```java
package com.biblioteca.web;

@RestController
@RequestMapping("/autores")
public class ControladorAutores {
    // ... endpoints ya implementados
}
```

```properties
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/doc/swagger-ui.html
springdoc.packages-to-scan=com.biblioteca.controllers
```

**Preguntas**:

1. ¿Por qué Swagger UI no muestra ningún endpoint de `ControladorAutores`?
2. ¿Cómo lo corregirías, respetando la arquitectura MVC del proyecto
   (capas `controllers`, `services` y `persistences`)?

## 📏 Criterios de evaluación de la solución

- Identifica que `ControladorAutores` vive en el paquete
  `com.biblioteca.web`, pero `springdoc.packages-to-scan` apunta a
  `com.biblioteca.controllers` — un paquete distinto, que no contiene
  ningún `@RestController`.
- Propone la corrección preferida: mover `ControladorAutores` al paquete
  de su capa MVC (`com.biblioteca.controllers`), de modo que coincida con
  `springdoc.packages-to-scan`. Como alternativas válidas, apuntar
  `springdoc.packages-to-scan` a `com.biblioteca.web` o a
  `com.biblioteca` (que escanea todos los subpaquetes).
- Explica por qué `web` no es un nombre de capa del proyecto: los
  controladores viven en `controllers`, igual que los servicios viven en
  `services` y las entidades/repositorios en `persistences`.
- Explica que Swagger UI puede cargar correctamente (la interfaz en sí
  funciona) aunque no encuentre ningún endpoint que documentar — no es un
  error de la herramienta, sino de configuración.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Avanzado

## 🎓 Resultados de aprendizaje

RA-6, RA-7
