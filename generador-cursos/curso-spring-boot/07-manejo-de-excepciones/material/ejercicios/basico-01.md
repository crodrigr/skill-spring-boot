# 🟢 Básico 01 — Clasificar excepciones y errores según la jerarquía `Throwable`

## 🧩 Problema

Para cada una de las siguientes clases, indicá si pertenece a la rama
`Exception` o a la rama `Error` de la jerarquía `Throwable`, y justificá
tu respuesta con el criterio oficial de la distinción (qué tan grave es
el problema y si una aplicación razonable debería intentar capturarlo).

## 💻 Código o contexto de partida

1. `NullPointerException`
2. `StackOverflowError`
3. `IllegalArgumentException`
4. `OutOfMemoryError`

## 📏 Criterios de evaluación de la solución

- `NullPointerException`: rama `Exception` (condición que el programa
  podría capturar y manejar, por ejemplo validando antes de usar un
  objeto).
- `StackOverflowError`: rama `Error` (problema grave del entorno de
  ejecución — una recursión sin fin agotó la pila — que una aplicación
  razonable no debería intentar capturar).
- `IllegalArgumentException`: rama `Exception` (un argumento inválido es
  una condición esperable que el programa puede validar y manejar).
- `OutOfMemoryError`: rama `Error` (la JVM se quedó sin memoria; no hay
  una acción correctiva razonable dentro del propio programa).
- Cada justificación menciona el criterio oficial (gravedad del problema
  y si una aplicación razonable debería capturarlo), no solo el nombre de
  la rama.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-1
