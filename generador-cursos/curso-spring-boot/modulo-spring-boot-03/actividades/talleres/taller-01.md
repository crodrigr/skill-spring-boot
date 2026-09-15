# 🛠️ Taller 01 — Mapear `Libro` y `Etiqueta` con una relación muchos a muchos

## 🎯 Objetivo (RA-10, RA-11)

Convertir `Libro` y una nueva clase `Etiqueta` en entidades JPA relacionadas
entre sí con `@ManyToMany`, y verificar con una consulta derivada que la
relación persiste correctamente sobre H2.

## 🌍 Contexto

Biblioteca Universitaria quiere poder etiquetar sus libros temáticamente
(por ejemplo, "Novela", "Ciencia", "Historia"), sabiendo que:

- Un `Libro` puede tener **varias** etiquetas.
- Una `Etiqueta` se aplica a **varios** libros.

Esta relación es muchos a muchos — la misma categoría de relación que
`Libro`↔`Autor` en el Ejemplo 08, pero con clases y datos distintos: no se
puede copiar esa solución tal cual, hay que aplicar el mismo criterio sobre
un par nuevo.

## 🪜 Pasos

1. Convertí `Libro` (`isbn`, `titulo`) en una entidad JPA, reutilizando el
   diseño ya usado en el Ejemplo 01/07 (`@Entity`, `@Id`,
   `@GeneratedValue`, `@Column(unique = true)` sobre `isbn`).
2. Creá la entidad `Etiqueta`, con `id` y `nombre`.
3. Agregá la relación `@ManyToMany` entre ambas: decidí cuál de las dos
   clases es el lado propietario (la que declara `@JoinTable`) y cuál es el
   lado inverso (con `mappedBy`).
4. Creá los repositorios `RepositorioLibros` y `RepositorioEtiquetas`,
   ambos extendiendo `JpaRepository`.
5. Agregá al repositorio del lado que te parezca más natural un método
   derivado que permita buscar por nombre (por ejemplo,
   `findByNombre(String nombre)` en `RepositorioEtiquetas`, o
   `findByIsbn(String isbn)` en `RepositorioLibros`, si no lo tenés ya).
6. Escribí un `Main` (`@SpringBootApplication` + `CommandLineRunner`) que
   guarde un libro con dos etiquetas, y luego verifique con el método
   derivado del paso 5 que la relación persistió correctamente.

## 💡 Ejemplo resuelto (parcial)

Así se ve la entidad `Etiqueta`, para que uses el mismo estilo en el resto
del entregable:

```java
@Entity
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    protected Etiqueta() {
    }

    public Etiqueta(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
```

El resto del entregable (la relación en `Libro`, los dos repositorios y el
`Main`) queda a tu cargo — la solución completa está en
`solucion-taller-01.md`, pero intentá resolverlo primero por tu cuenta.

## 📦 Entregable

```text
📁 taller-01-libro-etiqueta
└── 📁 src/main
    ├── 📁 java/com/biblioteca
    │   ├── 📄 Libro.java
    │   ├── 📄 Etiqueta.java
    │   ├── 📄 RepositorioLibros.java
    │   └── 📄 RepositorioEtiquetas.java
    ├── 📁 java
    │   └── 📄 Main.java
    └── 📁 resources
        └── 📄 application.properties
```

## 🧪 Casos de prueba

- Guardar un `Libro` con dos `Etiqueta` distintas y confirmar que
  `libro.getEtiquetas().size()` (o el getter equivalente) devuelve `2`.
- Buscar una `Etiqueta` por nombre y confirmar que el libro guardado
  aparece entre los que la tienen asignada (si se implementó la consulta en
  ese sentido).

## 📏 Criterios de evaluación

- `Libro` y `Etiqueta` son entidades JPA completas y compilables.
- La relación `@ManyToMany` está correctamente declarada, con un lado
  propietario (`@JoinTable`) y un lado inverso (`mappedBy`) explícitos.
- El `Main` ejecuta sin excepciones contra H2 y produce una salida
  coherente con los datos guardados.
