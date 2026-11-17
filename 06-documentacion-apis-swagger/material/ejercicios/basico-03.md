# 🟢 Básico 03 — Elegir entre documentación manual y automática

## 🧩 Problema

Para cada uno de los siguientes escenarios, elegí si conviene documentar
la API manualmente (SwaggerHub) o automáticamente (springdoc-openapi), y
justificá tu elección en una oración.

## 💻 Código o contexto de partida

1. Un equipo va a empezar un proyecto nuevo y quiere acordar, antes de
   escribir ningún controlador, exactamente qué endpoints va a tener la
   API y qué va a recibir/devolver cada uno.
2. Un proyecto ya tiene diez controladores REST funcionando en
   producción, y el equipo quiere que su documentación quede siempre al
   día sin mantenimiento manual.
3. Un equipo necesita compartir una página de documentación externa,
   navegable sin instalar nada, con un cliente que no tiene acceso al
   código ni al proyecto Spring Boot.

## 📏 Criterios de evaluación de la solución

- Escenario 1: documentación manual (SwaggerHub) — se necesita diseñar
  el contrato de la API antes de que exista código.
- Escenario 2: documentación automática (springdoc-openapi) — hay código
  real ya funcionando, y se busca evitar el mantenimiento manual.
- Escenario 3: cualquiera de las dos puede servir según el contexto, pero
  la justificación debe reconocer que la exportación HTML de SwaggerHub
  (Ejemplo 03) es una opción directa para compartir una página estática
  sin depender del proyecto Spring Boot en ejecución.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-4, RA-8
