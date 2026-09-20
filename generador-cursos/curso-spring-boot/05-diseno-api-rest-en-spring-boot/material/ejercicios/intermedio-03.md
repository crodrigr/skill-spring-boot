# 🟡 Intermedio 03 — Crear los endpoints `POST`, `PUT`, `DELETE` de un controlador

## 🧩 Problema

Continuando el `ControladorAutores` del ejercicio anterior, completá el
CRUD REST agregando crear, actualizar y eliminar.

## 💻 Código o contexto de partida

```java
// ControladorAutores.java — capa controllers (com.biblioteca.controllers)
// ya existe (Intermedio 02), con los endpoints GET /autores y
// GET /autores/{id}.
```

1. Agregá `POST /autores` (crear), devolviendo `201` con el autor creado.
2. Agregá `PUT /autores/{id}` (actualizar `nombre`), devolviendo `200` si
   existía, `404` si no.
3. Agregá `DELETE /autores/{id}`, devolviendo `200` si existía, `404` si
   no.

## 📏 Criterios de evaluación de la solución

- `POST /autores` usa `@RequestBody` y devuelve `201 Created`.
- `PUT /autores/{id}` usa `@PathVariable` + `@RequestBody`, devuelve `200`
  o `404` según corresponda.
- `DELETE /autores/{id}` usa `@PathVariable`, devuelve `200` o `404` según
  corresponda.
- Ningún endpoint devuelve `200` con cuerpo vacío cuando el recurso no
  existe.
- Los tres endpoints nuevos delegan siempre en `ServicioAutores`; el
  controlador no accede a `RepositorioAutores`.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-7, RA-8
