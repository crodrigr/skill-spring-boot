# 🟡 Intermedio 02 — Agregar `cascade` a una relación padre-hijas

## 🧩 Problema

Biblioteca Universitaria tiene `Usuario` y `Prestamo` (Ejemplo 04), pero
cada vez que se registra un usuario nuevo con préstamos ya conocidos, hay
que guardar cada `Prestamo` por separado con `RepositorioPrestamos`. Te
piden simplificarlo con `cascade`.

## 💻 Código o contexto de partida

```java
// Capa persistences: com.biblioteca.persistences.entities
@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    // TODO: agregar el lado inverso de la relación con Prestamo, con cascade

    protected Usuario() {
    }

    public Usuario(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
}
```

```java
// Prestamo.java (com.biblioteca.persistences.entities) ya existe (Ejemplo 04),
// con id, fechaPrestamo, usuario y libro.
```

1. Agregá a `Usuario` un campo `prestamos` con
   `@OneToMany(mappedBy = "usuario", cascade = CascadeType.PERSIST)`.
2. Escribí un `Main` (paquete raíz `com.biblioteca`) que cree un
   `Usuario` con un `Prestamo` ya asignado en su colección, y lo guarde
   con una sola llamada a `RepositorioUsuarios.save(...)`, verificando que
   el préstamo también quedó guardado.

## 📏 Criterios de evaluación de la solución

- `Usuario.prestamos` declara `cascade = CascadeType.PERSIST` (o `ALL`).
- El `Main` guarda el `Usuario` una sola vez, sin llamar a
  `RepositorioPrestamos` para el préstamo inicial, y verifica que se
  guardó correctamente.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-7
