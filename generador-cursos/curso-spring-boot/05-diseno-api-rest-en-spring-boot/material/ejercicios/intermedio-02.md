# 🟡 Intermedio 02 — Crear los endpoints `GET` de un controlador

## 🧩 Problema

Con `ServicioAutores` ya creado (Intermedio 01), te piden exponerlo como
API REST: por ahora, solo los endpoints de lectura.

## 💻 Código o contexto de partida

```java
// ServicioAutores.java ya existe (Intermedio 01), con listarTodos,
// buscarPorId, crear, actualizar y eliminar.
```

1. Creá `ControladorAutores` (`@RestController`, `@RequestMapping("/autores")`),
   inyectando `ServicioAutores`.
2. Agregá `GET /autores` (listar todos) y `GET /autores/{id}` (buscar por
   id), devolviendo `404` cuando el autor no existe.

## 📏 Criterios de evaluación de la solución

- `ControladorAutores` inyecta `ServicioAutores` (nunca
  `RepositorioAutores` directamente).
- `GET /autores` devuelve la lista completa con `200`.
- `GET /autores/{id}` usa `@PathVariable` y devuelve `200` con el autor si
  existe, `404` si no.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-7
