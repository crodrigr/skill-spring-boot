# 🟡 Intermedio 01 — Convertir `RepositorioLibros` en un repositorio de Spring Data JPA

## 🧩 Problema

Biblioteca Universitaria quiere que `RepositorioLibros` deje de tener una
implementación manual en memoria y pase a persistir sobre H2, igual que
`RepositorioPacientes` en el Ejemplo 07.

## 💻 Código o contexto de partida

```java
// Libro.java — todavía es el record del Módulo 1; convertilo en @Entity
// como se hizo con Paciente en el Ejemplo 01.
public record Libro(String isbn, String titulo) {}

// RepositorioLibros.java — todavía tiene su propio método, sin Spring Data JPA
public interface RepositorioLibros {
    Optional<Libro> buscarPorIsbn(String isbn);
}
```

```text
application.properties del proyecto (igual que en el Ejemplo 07, ya
configurado contra H2 en memoria — no hace falta tocarlo).
```

1. Convertí `Libro` en una entidad JPA (`@Entity`, `@Id`,
   `@GeneratedValue`, `@Column(unique = true)` sobre `isbn`), siguiendo el
   mismo patrón que `Paciente` en el Ejemplo 01.
2. Convertí `RepositorioLibros` para que extienda
   `JpaRepository<Libro, Long>`, con un método derivado `findByIsbn(String
   isbn)` que reemplace a `buscarPorIsbn`.
3. Escribí un `Main` (`@SpringBootApplication` + `CommandLineRunner`) que
   guarde un libro y lo busque por `findByIsbn`, imprimiendo su título.

## 📏 Criterios de evaluación de la solución

- `Libro` compila como clase (no como `record`), con constructor sin
  argumentos y *getters*.
- `RepositorioLibros` extiende `JpaRepository<Libro, Long>` y no declara
  ninguna implementación propia para `findByIsbn`.
- El `Main` ejecuta sin excepciones y el título encontrado coincide con el
  guardado.

## 🚧 Restricciones

- No se puede usar `EntityManager` directamente en este ejercicio: la
  búsqueda debe resolverse exclusivamente con un método derivado del
  repositorio.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-8
