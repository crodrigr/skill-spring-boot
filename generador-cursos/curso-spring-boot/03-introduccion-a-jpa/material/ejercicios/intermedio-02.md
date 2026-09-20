# 🟡 Intermedio 02 — Convertir una clase en entidad JPA

## 🧩 Problema

Biblioteca Universitaria necesita registrar categorías de libros (por
ejemplo, "Novela", "Ensayo") en la base de datos, pero la clase todavía no
tiene ninguna anotación de JPA.

## 💻 Código o contexto de partida

```java
// Capa persistences: com.biblioteca.persistences.entities
public class Categoria {

    private Long id;
    private String nombre;

    public Categoria(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
```

**Capas MVC**: `Categoria` va en `com.biblioteca.persistences.entities` y
`RepositorioCategorias` en `com.biblioteca.persistences.repositories`
(capa `persistences`).

1. Convertí `Categoria` en una entidad JPA: agregá `@Entity`, `@Id` con
   `@GeneratedValue` sobre `id`, y `@Column(nullable = false)` sobre
   `nombre` (no puede quedar sin valor).
2. Agregá el constructor sin argumentos que JPA necesita.
3. Escribí un repositorio `RepositorioCategorias` que extienda
   `JpaRepository<Categoria, Long>`, en el paquete
   `com.biblioteca.persistences.repositories`.

## 📏 Criterios de evaluación de la solución

- `Categoria` compila con `@Entity`, `@Id`, `@GeneratedValue` y
  `@Column(nullable = false)` sobre `nombre`.
- Incluye un constructor sin argumentos (puede ser `protected`) además del
  constructor con `nombre`.
- `RepositorioCategorias` extiende `JpaRepository<Categoria, Long>` sin
  declarar ningún método propio (no es necesario para este ejercicio).
- `Categoria` y `RepositorioCategorias` viven cada una en el paquete de
  la capa `persistences` que les corresponde (`entities` y
  `repositories`).

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-10
