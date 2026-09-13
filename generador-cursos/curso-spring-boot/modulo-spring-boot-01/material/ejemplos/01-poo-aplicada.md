# 💡 Ejemplo 01 — POO aplicada: clases, interfaces, herencia, polimorfismo

## 🏥📚 Caso de estudio

Biblioteca Universitaria: modelamos usuarios (`Estudiante`, `Docente`) que heredan
de una clase común `Usuario`, y recursos que se pueden prestar (`Libro`,
`RecursoDigital`) implementando una interfaz común `Prestable`.

## 💻 Código

```java
// Clase base: comportamiento y datos comunes a todo usuario de la biblioteca
public abstract class Usuario {

    private final String nombre;
    private final String codigo;

    protected Usuario(String nombre, String codigo) {
        this.nombre = nombre;
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    // Cada tipo de usuario define su propio límite de préstamos simultáneos
    public abstract int limitePrestamosSimultaneos();
}

public class Estudiante extends Usuario {

    public Estudiante(String nombre, String codigo) {
        super(nombre, codigo);
    }

    @Override
    public int limitePrestamosSimultaneos() {
        return 3;
    }
}

public class Docente extends Usuario {

    public Docente(String nombre, String codigo) {
        super(nombre, codigo);
    }

    @Override
    public int limitePrestamosSimultaneos() {
        return 10;
    }
}

// Interfaz: qué puede hacer un recurso prestable, sin decir cómo lo calcula cada uno
public interface Prestable {
    int calcularDiasDevolucion();
    String descripcion();
}

public class Libro implements Prestable {

    private final String titulo;

    public Libro(String titulo) {
        this.titulo = titulo;
    }

    @Override
    public int calcularDiasDevolucion() {
        return 14; // los libros físicos se prestan por 14 días
    }

    @Override
    public String descripcion() {
        return "Libro: " + titulo;
    }
}

public class RecursoDigital implements Prestable {

    private final String titulo;

    public RecursoDigital(String titulo) {
        this.titulo = titulo;
    }

    @Override
    public int calcularDiasDevolucion() {
        return 7; // el acceso digital es más corto: hay más rotación posible
    }

    @Override
    public String descripcion() {
        return "Recurso digital: " + titulo;
    }
}
```

## 🧭 Explicación paso a paso

1. `Usuario` es una clase **abstracta**: agrupa el nombre y el código, comunes a
   cualquier usuario, y declara `limitePrestamosSimultaneos()` sin implementarlo,
   porque cada subtipo lo define distinto.
2. `Estudiante` y `Docente` **heredan** de `Usuario` y solo agregan lo que las hace
   diferentes: su propio límite de préstamos. No duplican `nombre` ni `codigo`.
3. `Prestable` es una **interfaz**: no le importa si el recurso es un libro físico o
   digital, solo que pueda responder cuántos días dura el préstamo y describirse.
4. `Libro` y `RecursoDigital` **implementan** `Prestable`, cada uno con su propia
   regla de negocio para `calcularDiasDevolucion()`.
5. El **polimorfismo** aparece al recorrer una lista de `Prestable` (ver más abajo):
   el mismo código llama a `calcularDiasDevolucion()` en cada elemento, y cada uno
   ejecuta su propia versión, sin que el código que recorre la lista necesite saber
   si es un `Libro` o un `RecursoDigital`.

```java
List<Prestable> recursos = List.of(
    new Libro("Estructuras de Datos"),
    new RecursoDigital("Curso de Spring Boot (video)")
);

for (Prestable recurso : recursos) {
    System.out.println(recurso.descripcion() + " -> " + recurso.calcularDiasDevolucion() + " días");
}
```

## ✅ Resultado esperado

```text
Libro: Estructuras de Datos -> 14 días
Recurso digital: Curso de Spring Boot (video) -> 7 días
```
