# 🟢 Básico 02 — Identificar en qué capa se origina un error

## 🧩 Problema

Para cada uno de los siguientes escenarios de error en una API de
Biblioteca Universitaria, identificá en qué capa de la arquitectura
(`Repository`, `Service` o `Controller`) se origina, y justificá tu
respuesta.

## 💻 Código o contexto de partida

1. La base de datos H2 no responde porque el proyecto perdió la
   conexión.
2. Un cliente intenta crear un libro con un isbn que ya existe en el
   catálogo.
3. Una solicitud HTTP llega sin el parámetro `isbn` requerido por
   `@RequestParam`.

## 📏 Criterios de evaluación de la solución

- Escenario 1: `Repository` — es un error de acceso a datos/conexión a
  la base de datos.
- Escenario 2: `Service` — es una regla de negocio (no permitir
  duplicados), no un problema de infraestructura ni de exposición HTTP.
- Escenario 3: `Controller` — es un problema de la solicitud HTTP en sí
  (falta un parámetro esperado), previo a que la lógica de negocio se
  ejecute.
- Cada justificación explica por qué el error corresponde a esa capa y
  no a las otras dos.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-3
