# 🟡 Intermedio 01 — Crear una clase de servicio

## 🧩 Problema

Biblioteca Universitaria ya tiene `Autor` convertida en entidad JPA con su
repositorio (Módulo 3). Te piden crear la capa de servicio que el futuro
controlador de autores va a necesitar.

## 💻 Código o contexto de partida

```java
// Autor.java — capa persistences (com.biblioteca.persistences.entities)
// (Módulo 3, reutilizada; se agregan getId() y setNombre(),
// que el Módulo 3 no necesitaba porque nunca se usó en un CRUD)
@Entity
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @ManyToMany(mappedBy = "autores") // lado inverso: no repite la tabla intermedia
    private Set<Libro> libros = new HashSet<>();

    protected Autor() {
    }

    public Autor(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Set<Libro> getLibros() { return libros; }
}
```

```java
// RepositorioAutores.java — capa persistences (com.biblioteca.persistences.repositories)
public interface RepositorioAutores extends JpaRepository<Autor, Long> {
}
```

1. Creá `ServicioAutores` (`@Service`) en el paquete de la capa de
   servicios (`com.biblioteca.services`), inyectando `RepositorioAutores`
   por constructor.
2. Agregá los mismos cinco métodos de negocio del Ejemplo 05:
   `listarTodos`, `buscarPorId`, `crear`, `actualizar` (actualiza
   `nombre`) y `eliminar`.

## 📏 Criterios de evaluación de la solución

- `ServicioAutores` está anotada con `@Service`, vive en
  `com.biblioteca.services` e inyecta `RepositorioAutores` por
  constructor.
- Los cinco métodos delegan en el repositorio, sin escribir SQL/JPQL.
- `actualizar` devuelve `Optional<Autor>` (vacío si no existe); `eliminar`
  devuelve `boolean` (indicando si había algo para eliminar).

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-6
