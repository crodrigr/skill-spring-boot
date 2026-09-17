# 🟢 Básico 02 — Componentes de la arquitectura de JPA

## 🧩 Problema

Un docente te da una lista de responsabilidades sueltas y te pide emparejar
cada una con el componente de la arquitectura de JPA que la cumple.

## 💻 Código o contexto de partida

```text
Responsabilidades a emparejar:

a) Agrupa varias operaciones para que se confirmen o reviertan como una unidad.
b) Es el punto de entrada; crea y administra instancias del componente central.
c) Ejecuta consultas JPQL sobre entidades.
d) Es el componente central: persiste, actualiza, elimina y consulta entidades.
e) Se mapea directamente a una tabla de la base de datos.

Componentes disponibles:
EntityManagerFactory · EntityManager · EntityTransaction · Query · @Entity
```

## 📏 Criterios de evaluación de la solución

- Empareja correctamente los cinco pares: (a) `EntityTransaction`, (b)
  `EntityManagerFactory`, (c) `Query`, (d) `EntityManager`, (e) `@Entity`.
- Explica con sus palabras la diferencia entre `EntityManagerFactory` y
  `EntityManager` (quién crea a quién).

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-3
