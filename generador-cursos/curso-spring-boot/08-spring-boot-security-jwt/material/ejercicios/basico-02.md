# 🟢 Básico 02 — Identificar las tres partes de un JWT

## 🧩 Problema

Se tiene el siguiente JWT de ejemplo:

```text
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmEiLCJyb2wiOiJVU0VSIiwiZXhwIjoxOTk5OTk5OTk5fQ.k3F9x2pQ7mLwR5vN1oJhT8sD4cA6bE0gU2iY9zX3rWc
```

**Pregunta**: separá el token en sus tres partes (header, payload,
signature), indicá cuál delimitador las separa, y explicá qué tipo de
información contiene cada una (sin necesidad de decodificar el Base64 a
mano).

## 💻 Código o contexto de partida

Este ejercicio es conceptual: usá como referencia la estructura descrita
en el [Ejemplo 06 — Estructura de un JWT](../ejemplos/06-estructura-de-un-jwt.md).

## 📏 Criterios de evaluación de la solución

- Identifica correctamente las tres partes separadas por puntos (`.`).
- Asocia cada parte con su contenido correcto (header → algoritmo y tipo;
  payload → claims del usuario; signature → verificación de integridad).
- No confunde "codificado en Base64" con "encriptado".

## 🚧 Restricciones

- No se requiere decodificar manualmente el contenido Base64 de cada
  parte; alcanza con identificar cuál es cuál y qué tipo de datos
  contiene.

## 📊 Dificultad

Básico.

## 🎓 Resultados de aprendizaje

`RA-7`.
