# 🔴 Avanzado 01 — Mapear una relación `@OneToOne` o `@ManyToMany`

## 🧩 Problema

Biblioteca Universitaria necesita modelar **una** de estas dos relaciones,
ninguna de las cuales aparece en los ejemplos del módulo (elegí una):

- **Opción A (`@OneToOne`)**: cada `Libro` tiene una única `FichaTecnica`
  (número de páginas, editorial), y cada `FichaTecnica` pertenece a un
  único `Libro`.
- **Opción B (`@ManyToMany`)**: un `Estudiante` puede tener varios
  `Prestamo` activos (no confundir con la relación que vas a elegir: la
  relación a mapear acá es entre `Estudiante` y `Curso` — un estudiante se
  inscribe en varios cursos, y un curso tiene varios estudiantes).

## 💻 Código o contexto de partida

```java
// Opción A — capa persistences: com.biblioteca.persistences.entities
public class Libro { /* ya existe, con id, isbn, titulo */ }

public class FichaTecnica {
    private Long id;
    private int numeroPaginas;
    private String editorial;
}
```

```java
// Opción B — capa persistences: com.biblioteca.persistences.entities
public class Estudiante {
    private Long id;
    private String nombre;
}

public class Curso {
    private Long id;
    private String nombre;
}
```

Elegí una opción, convertí las clases involucradas en entidades JPA
completas (`@Entity`, `@Id`, `@GeneratedValue`, constructor sin argumentos),
y agregá la relación elegida, decidiendo explícitamente cuál es el lado
propietario. Las entidades van en el paquete
`com.biblioteca.persistences.entities` (capa `persistences`).

## 📏 Criterios de evaluación de la solución

- Ambas clases de la opción elegida son entidades JPA completas y
  compilables.
- La relación usa la anotación correcta (`@OneToOne` o `@ManyToMany`) con
  `@JoinColumn` (Opción A) o `@JoinTable`/`mappedBy` (Opción B) según
  corresponda.
- Identifica explícitamente cuál lado es el propietario y justifica por
  qué.

## 🚧 Restricciones

- No se permite reutilizar literalmente el código de `Paciente`↔
  `HistoriaClinica` ni de `Libro`↔`Autor` del Ejemplo 08: las clases deben
  ser las de este enunciado.

## 📊 Dificultad

Avanzado

## 🎓 Resultados de aprendizaje

RA-11
