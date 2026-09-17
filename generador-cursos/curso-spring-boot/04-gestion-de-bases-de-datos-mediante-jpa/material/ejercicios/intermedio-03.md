# 🟡 Intermedio 03 — Implementar un CRUD parcial

## 🧩 Problema

Biblioteca Universitaria necesita registrar, consultar y corregir el
nombre de sus `Autor` (Módulo 3), pero **no** necesita eliminarlos todavía
(los autores nunca se borran, aunque dejen de tener libros asociados).

## 💻 Código o contexto de partida

```java
@Entity
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    protected Autor() {
    }

    public Autor(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
```

```java
public interface RepositorioAutores extends JpaRepository<Autor, Long> {
}
```

1. Escribí un `Main` que:
   - **Cree** un `Autor` con `save(...)`.
   - **Lea** ese mismo `Autor` con `findById(...)`.
   - **Actualice** su `nombre` y lo guarde de nuevo con `save(...)`.
2. **No** implementes ninguna operación de eliminación: es intencional que
   este CRUD sea parcial.

## 📏 Criterios de evaluación de la solución

- Usa `save(new Autor(...))` para crear (sin `id` asignado).
- Usa `findById(...)` para leer, manejando el `Optional` devuelto.
- Actualiza `nombre` sobre la entidad leída y la guarda con `save(...)`,
  aprovechando que ya tiene `id` (no crea un registro duplicado).
- No incluye ninguna llamada a `deleteById` ni a `delete`.

## 🚧 Restricciones

No implementar ninguna operación de eliminación.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-9
