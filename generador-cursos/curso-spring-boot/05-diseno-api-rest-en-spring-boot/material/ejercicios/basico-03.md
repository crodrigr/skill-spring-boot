# 🟢 Básico 03 — Identificar la responsabilidad de cada capa

## 🧩 Problema

Para cada uno de los siguientes fragmentos de código, identificá a qué
capa de la arquitectura (presentación `controllers`, negocio `services` o persistencia `persistences`)
pertenece, indicá en qué paquete debería vivir (por ejemplo,
`com.biblioteca.services`) y explicá en una oración cuál sería su
responsabilidad ahí.

## 💻 Código o contexto de partida

**Fragmento 1**:

```java
package com.biblioteca.persistences.repositories;

public interface RepositorioAutores extends JpaRepository<Autor, Long> {
}
```

**Fragmento 2**:

```java
package com.biblioteca.controllers;

@RestController
@RequestMapping("/autores")
public class ControladorAutores {
    // ...
}
```

**Fragmento 3**:

```java
package com.biblioteca.services;

@Service
public class ServicioAutores {
    private final RepositorioAutores repositorioAutores;
    // ...
}
```

**Pregunta adicional**: si `ControladorAutores` necesitara guardar un
`Autor`, ¿a cuál de las otras dos clases debería llamar directamente, y a
cuál nunca debería llamar directamente?

## 📏 Criterios de evaluación de la solución

- Fragmento 1: capa de persistencia (subpaquete `repositories`, paquete
  `com.biblioteca.persistences.repositories`); responsable del acceso a
  datos (`JpaRepository` ya trae las operaciones básicas).
- Fragmento 2: capa de presentación (`com.biblioteca.controllers`);
  responsable de manejar la solicitud y respuesta HTTP sobre la ruta
  `/autores`.
- Fragmento 3: capa de negocio (`com.biblioteca.services`); responsable de
  la lógica de negocio, delegando en el repositorio.
- Pregunta adicional: `ControladorAutores` debería llamar a
  `ServicioAutores` (nunca directamente a `RepositorioAutores`), para
  respetar que cada capa solo se comunica con la de abajo
  (presentación `controllers` → negocio `services` → persistencia `persistences`).

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-5
