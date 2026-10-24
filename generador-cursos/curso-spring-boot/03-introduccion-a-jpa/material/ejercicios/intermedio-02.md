# 🟡 Intermedio 02 — Convertir una clase en entidad JPA

## 🧩 Problema

Biblioteca Universitaria necesita registrar categorías de libros (por
ejemplo, "Novela", "Ensayo") en la base de datos, pero la clase todavía no
tiene ninguna anotación de JPA.

## 💻 Código o contexto de partida

```java
package com.biblioteca.negocio.model;

// capa de negocio (modelo): com.biblioteca.negocio.model
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

**Capas**: `Categoria` va en `com.biblioteca.negocio.model` y
`RepositorioCategorias` en `com.biblioteca.persistence.repository`
(capas de negocio y de persistencia).

1. Convertí `Categoria` en una entidad JPA: agregá `@Entity`, `@Id` con
   `@GeneratedValue` sobre `id`, y `@Column(nullable = false)` sobre
   `nombre` (no puede quedar sin valor).
2. Agregá el constructor sin argumentos que JPA necesita.
3. Escribí un repositorio `RepositorioCategorias` que extienda
   `JpaRepository<Categoria, Long>`, en el paquete
   `com.biblioteca.persistence.repository`.

## 📏 Criterios de evaluación de la solución

- `Categoria` compila con `@Entity`, `@Id`, `@GeneratedValue` y
  `@Column(nullable = false)` sobre `nombre`.
- Incluye un constructor sin argumentos (puede ser `protected`) además del
  constructor con `nombre`.
- `RepositorioCategorias` extiende `JpaRepository<Categoria, Long>` sin
  declarar ningún método propio (no es necesario para este ejercicio).
- `Categoria` y `RepositorioCategorias` viven cada una en el paquete que les corresponde (`negocio/model` y
  `persistence/repository`).

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-10
