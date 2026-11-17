# 🟢 Básico 03 — Identificar tipos de relación entre entidades

## 🧩 Problema

Antes de escribir ninguna anotación, un buen desarrollador identifica qué
tipo de relación describe cada par de clases. Analizá los siguientes tres
pares de MediSalud y Biblioteca Universitaria.

## 💻 Código o contexto de partida

```text
Par 1: Un Medico puede atender varias Cita, pero cada Cita tiene un único Medico.
Par 2: Un Libro pertenece a una única Categoria (por ejemplo, "Novela"), pero
       una Categoria agrupa varios Libro.
Par 3: Un Estudiante puede inscribirse en varios Curso, y un Curso puede tener
       varios Estudiante inscriptos.
```

Para cada par, indicá el tipo de relación (uno a uno, uno a muchos o muchos a
muchos) y cuál de las dos clases es el lado "muchos" (si aplica).

## 📏 Criterios de evaluación de la solución

- Identifica correctamente: Par 1 = uno a muchos (`Medico` es "uno", `Cita`
  es "muchos"); Par 2 = uno a muchos (`Categoria` es "uno", `Libro` es
  "muchos"); Par 3 = muchos a muchos.
- Justifica cada respuesta señalando qué dato del enunciado indica el límite
  ("varios"/"única") de cada lado.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-4
