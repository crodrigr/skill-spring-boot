# 🟢 Básico 01 — Elegir el verbo HTTP correcto

## 🧩 Problema

Para cada una de las siguientes operaciones sobre la API de Biblioteca
Universitaria, elegí el verbo HTTP más apropiado y justificá tu elección
en una oración.

## 💻 Código o contexto de partida

1. Obtener la lista completa de libros disponibles.
2. Registrar un libro nuevo en el catálogo.
3. Cambiar el título de un libro ya existente, sin tocar sus demás datos.
4. Reemplazar por completo los datos de un libro existente (isbn y
   título).
5. Sacar un libro del catálogo porque se dio de baja definitivamente.

## 📏 Criterios de evaluación de la solución

- Escenario 1: `GET` (solo lectura, no modifica nada).
- Escenario 2: `POST` (crea un recurso nuevo).
- Escenario 3: `PATCH` (modificación parcial, un solo campo).
- Escenario 4: `PUT` (reemplazo completo del recurso).
- Escenario 5: `DELETE` (eliminación del recurso).
- La justificación de cada elección menciona explícitamente por qué los
  demás verbos no encajan tan bien (en particular, la diferencia entre
  `PUT` y `PATCH` en los escenarios 3 y 4).

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-2
