# 🟢 Básico 04 — ¿Framework o librería?

## 🧩 Problema

MediSalud está evaluando incorporar cuatro herramientas nuevas a su proyecto y no
está seguro de cuáles son frameworks y cuáles son librerías.

## 💻 Código o contexto de partida

```text
1. Una herramienta que exige organizar el proyecto en controladores, servicios y
   repositorios, y que invoca automáticamente el método correcto cuando llega una
   petición HTTP.
2. Una herramienta que solo ofrece una función para dar formato a una fecha como
   "15 de marzo de 2026", que se llama donde el desarrollador la necesite.
3. Una herramienta que administra el ciclo de vida completo de los objetos de la
   aplicación (creación, inyección de dependencias, destrucción).
4. Una herramienta que solo convierte un objeto Java a texto JSON y viceversa,
   invocada explícitamente con una línea de código en el punto donde se necesita.
```

Para cada herramienta, indicá si es un framework o una librería, y justificá tu
respuesta usando el criterio de "quién controla el flujo" (Inversión de Control).

## 📏 Criterios de evaluación de la solución

- Clasifica correctamente los casos 1 y 3 como framework: imponen estructura e
  invierten el control (el framework decide cuándo llamar al código del
  desarrollador).
- Clasifica correctamente los casos 2 y 4 como librería: el desarrollador decide
  cuándo llamarlas, y no imponen estructura al resto del proyecto.
- La justificación de cada caso menciona explícitamente el criterio de control del
  flujo, no solo "porque se parece a Spring" o "porque es chico".

## 🚧 Restricciones

- No es necesario escribir código; el ejercicio se resuelve clasificando y
  justificando en texto.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-10, RA-11, RA-12
