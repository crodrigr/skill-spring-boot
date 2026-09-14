# 🟢 Básico 02 — Ventajas y desventajas de una nueva dependencia

## 🧩 Problema

El equipo de Biblioteca Universitaria está evaluando agregar una librería
externa para generar reportes en PDF del historial de préstamos, en vez de
escribir esa lógica desde cero.

## 💻 Código o contexto de partida

```text
Situación: la librería de reportes en PDF es mantenida por un proveedor
externo, tiene buena documentación, y ya la usan varios equipos de la
universidad para necesidades similares.
```

Analizá esta decisión de diseño: dá **una ventaja concreta** y **una
desventaja concreta** de agregar esta dependencia externa, explicando cada una
en el contexto específico de este caso (no una definición genérica).

## 📏 Criterios de evaluación de la solución

- La ventaja mencionada es una de las cuatro vistas en el Ejemplo 01
  (reutilización, modularidad, especialización, mantenibilidad) y está
  explicada en términos del caso (por ejemplo, "reutilización: no hay que
  escribir ni mantener lógica de generación de PDF propia").
- La desventaja mencionada es una de las cuatro vistas en el Ejemplo 01
  (acoplamiento, complejidad, vulnerabilidades, dependencia de terceros) y
  está explicada en términos del caso (por ejemplo, "dependencia de
  terceros: si el proveedor discontinúa la librería, hay que migrar los
  reportes a otra solución").
- No repite la ventaja o la desventaja en abstracto sin conectarla con el
  escenario de los reportes en PDF.

## 🚧 Restricciones

- No es necesario escribir código; el ejercicio se resuelve con el análisis
  en texto.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-2
