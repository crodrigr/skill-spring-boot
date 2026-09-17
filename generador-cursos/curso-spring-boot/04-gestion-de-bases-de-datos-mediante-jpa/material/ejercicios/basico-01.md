# 🟢 Básico 01 — Identificar el lado dueño de una relación

## 🧩 Problema

Un docente te da tres pares de entidades ya relacionadas y te pide que
identifiques, en cada caso, cuál es el lado dueño de la relación (el que
tiene la clave foránea real).

## 💻 Código o contexto de partida

```text
Par 1 (uno a uno): Estudiante ↔ CarnetUniversitario.
  CarnetUniversitario declara:
  @OneToOne
  @JoinColumn(name = "estudiante_id")
  private Estudiante estudiante;

Par 2 (uno a muchos): Curso ↔ Inscripcion.
  Inscripcion declara:
  @ManyToOne
  @JoinColumn(name = "curso_id")
  private Curso curso;

  Curso declara:
  @OneToMany(mappedBy = "curso")
  private List<Inscripcion> inscripciones;

Par 3 (muchos a muchos): Libro ↔ Categoria.
  Libro declara:
  @ManyToMany
  @JoinTable(name = "libro_categoria", ...)
  private Set<Categoria> categorias;

  Categoria declara:
  @ManyToMany(mappedBy = "categorias")
  private Set<Libro> libros;
```

Para cada par, indicá cuál entidad es el lado dueño y qué anotación/atributo
lo delata.

## 📏 Criterios de evaluación de la solución

- Identifica correctamente: Par 1 = `CarnetUniversitario` (declara
  `@JoinColumn`); Par 2 = `Inscripcion` (declara `@JoinColumn`, no
  `mappedBy`); Par 3 = `Libro` (declara `@JoinTable`, no `mappedBy`).
- Justifica cada respuesta señalando la anotación concreta que delata al
  lado dueño (`@JoinColumn` o `@JoinTable`), no `mappedBy`.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-4
