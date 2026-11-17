# 🟡 Intermedio 03 — Elegir el mecanismo de manejo apropiado

## 🧩 Problema

Para cada uno de los siguientes escenarios, elegí cuál de los tres
mecanismos (`@ResponseStatus`, `@ExceptionHandler` o `@ControllerAdvice`)
aplicarías, y justificá tu elección.

## 💻 Código o contexto de partida

1. Una API pequeña, con un único controlador, necesita responder `404`
   ante un recurso no encontrado — sin ningún requisito de un cuerpo de
   error particular.
2. Un controlador específico necesita devolver un cuerpo de error con un
   formato distinto al resto de la aplicación, solo para un caso muy
   puntual de ese controlador.
3. Una aplicación con seis controladores distintos necesita que todos
   respondan con el mismo formato de error ante los mismos tipos de
   excepción.

## 📏 Criterios de evaluación de la solución

- Escenario 1: `@ResponseStatus` — el caso más simple, sin necesidad de
  personalizar el cuerpo ni centralizar nada.
- Escenario 2: `@ExceptionHandler` local — el requisito es específico de
  un controlador, no general a toda la aplicación.
- Escenario 3: `@ControllerAdvice` — varios controladores necesitan
  responder de forma uniforme; centralizar evita duplicar la lógica seis
  veces.
- Cada justificación explica por qué los otros dos mecanismos no serían
  la elección más proporcional para ese escenario.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-7
