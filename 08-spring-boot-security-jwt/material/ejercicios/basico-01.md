# 🟢 Básico 01 — Clasificar sistemas como stateful o stateless

## 🧩 Problema

Se presentan cuatro sistemas:

- Un servicio REST que responde a cada solicitud usando solo los datos
  que esa misma solicitud incluye.
- Un balanceador de carga que distribuye solicitudes entre varios
  servidores.
- Una aplicación de escritorio que mantiene abierta la sesión de un
  usuario mientras usa el programa, recordando sus acciones anteriores.
- Un motor de búsqueda que procesa cada consulta de forma independiente,
  sin depender del historial de búsquedas del usuario.

**Pregunta**: clasificá cada sistema como **stateful** o **stateless**,
justificando tu respuesta con el criterio de si el sistema almacena o no
información de interacciones pasadas del usuario.

## 💻 Código o contexto de partida

Este ejercicio es conceptual: usá como referencia la definición y el
diagrama del [Ejemplo 03 — Arquitecturas RESTful: stateful vs. stateless](../ejemplos/03-arquitecturas-stateful-vs-stateless.md).

## 📏 Criterios de evaluación de la solución

- Clasifica correctamente los cuatro sistemas.
- La justificación de cada clasificación se basa en si el sistema
  almacena o no estado de interacciones pasadas, no en otro criterio
  (por ejemplo, "usa base de datos" no es un criterio válido).

## 🚧 Restricciones

- No se requiere escribir ningún código Java para este ejercicio.

## 📊 Dificultad

Básico.

## 🎓 Resultados de aprendizaje

`RA-3`.
