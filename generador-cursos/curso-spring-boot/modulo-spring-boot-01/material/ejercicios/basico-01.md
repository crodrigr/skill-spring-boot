# 🟢 Básico 01 — Modelar Biblioteca Universitaria con POO

## 🧩 Problema

La biblioteca quiere registrar dos tipos de usuarios (`Estudiante`, `Docente`) y
dos tipos de recursos prestables (`Libro`, `RecursoDigital`), cada uno con su propia
regla de negocio.

## 💻 Código o contexto de partida

```text
Reglas de negocio:
- Todo usuario tiene nombre y código.
- Un Estudiante puede tener como máximo 3 préstamos simultáneos.
- Un Docente puede tener como máximo 10 préstamos simultáneos.
- Todo recurso prestable puede describirse y calcular sus días de devolución.
- Un Libro se presta por 14 días; un RecursoDigital, por 7 días.
```

Diseñá (con nombres de clase, atributos, y qué es clase abstracta, interfaz o
subclase) la jerarquía de tipos que modela estas reglas. No hace falta escribir
todo el cuerpo de los métodos, solo la estructura (clases, interfaces, relaciones de
herencia/implementación y las firmas de los métodos relevantes).

## 📏 Criterios de evaluación de la solución

- Existe una clase o interfaz común para `Estudiante` y `Docente`, con un método
  para el límite de préstamos que cada subtipo redefine.
- Existe una interfaz común para `Libro` y `RecursoDigital`, con los métodos para
  describirse y calcular días de devolución.
- La solución no duplica `nombre` y `codigo` en `Estudiante` y `Docente`.
- Se distingue correctamente cuándo usar herencia (tipos que "son un" `Usuario`) y
  cuándo usar interfaz (tipos que "pueden hacer" algo, sin relación de herencia
  entre sí).

## 🚧 Restricciones

- No es necesario compilar ni ejecutar código; alcanza con la estructura de clases
  e interfaces y las firmas de los métodos.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-1
