# 🟢 Básico 02 — `@ManyToMany` simple vs. entidad intermedia

## 🧩 Problema

MediSalud te da dos relaciones muchos a muchos y te pide decidir, para cada
una, si alcanza con `@ManyToMany` simple o si conviene una entidad
intermedia.

## 💻 Código o contexto de partida

```text
Relación 1: Medico ↔ Especialidad.
  Un médico puede tener varias especialidades, y una especialidad puede
  tener varios médicos. No hace falta guardar ningún dato adicional sobre
  esa asociación en sí (solo interesa saber cuáles especialidades tiene
  cada médico).

Relación 2: Paciente ↔ Medico (a través de "Consulta").
  Un paciente puede haber sido atendido por varios médicos, y un médico
  puede haber atendido a varios pacientes. Para cada atención hace falta
  registrar la fecha y el diagnóstico de esa consulta puntual.
```

Para cada relación, indicá si usarías `@ManyToMany` simple o una entidad
intermedia, y justificá con el criterio correcto.

## 📏 Criterios de evaluación de la solución

- Relación 1: `@ManyToMany` simple, porque no necesita ningún atributo
  propio de la asociación.
- Relación 2: entidad intermedia (por ejemplo, `Consulta`), porque
  `fecha` y `diagnostico` son atributos propios de cada atención puntual,
  no de `Paciente` ni de `Medico` por separado.
- Justifica ambas respuestas citando la presencia o ausencia de un
  atributo propio de la relación, no otro criterio.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-5
