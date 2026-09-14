# 💡 Ejemplo 04 — POO aplicada: clases, interfaces, herencia, polimorfismo

## 🌍 Contexto

Spring administra objetos (beans) que el propio equipo de desarrollo define como
clases, y con frecuencia programa contra interfaces para poder cambiar una
implementación sin tocar el código que la usa. Antes de delegarle esa gestión a
un framework, conviene dominar cómo se modelan esas clases e interfaces a mano:
cuándo usar **herencia** (tipos que "son un" algo más general) y cuándo usar una
**interfaz** (tipos que "pueden hacer" algo, sin relación de herencia entre sí).

**Qué busca demostrar este ejemplo**: que herencia e interfaz resuelven
problemas distintos —reutilizar comportamiento común (`Usuario`) frente a
garantizar una capacidad compartida entre tipos no relacionados
(`Prestable`)— y que el **polimorfismo** permite que un mismo fragmento de
código (`Main`) trate objetos de tipos concretos distintos de manera uniforme,
sin `instanceof` ni *casts*.

## 🏥📚 Caso de estudio

Biblioteca Universitaria: modelamos usuarios (`Estudiante`, `Docente`) que heredan
de una clase común `Usuario`, y recursos que se pueden prestar (`Libro`,
`RecursoDigital`) implementando una interfaz común `Prestable`.

## 💻 Código — clases del dominio

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

## 💻 Código — clase principal (`Main`)

Cada clase de arriba iría en su propio archivo `.java` en un proyecto real
(`Usuario.java`, `Estudiante.java`, etc.); `Main` es el punto de entrada que las
pone a todas en juego.

```java
public class Main {

    public static void main(String[] args) {

        // --- Herencia: Estudiante y Docente son ambos un Usuario ---
        Usuario estudiante = new Estudiante("Ana Torres", "EST-001");
        Usuario docente = new Docente("Carlos Ibáñez", "DOC-045");

        System.out.println(estudiante.getNombre() + " puede tener hasta "
                + estudiante.limitePrestamosSimultaneos() + " préstamos simultáneos.");
        System.out.println(docente.getNombre() + " puede tener hasta "
                + docente.limitePrestamosSimultaneos() + " préstamos simultáneos.");

        // --- Polimorfismo: recorrer distintos tipos de Prestable con el mismo código ---
        List<Prestable> recursos = List.of(
                new Libro("Estructuras de Datos"),
                new RecursoDigital("Curso de Spring Boot (video)")
        );

        for (Prestable recurso : recursos) {
            System.out.println(recurso.descripcion() + " -> "
                    + recurso.calcularDiasDevolucion() + " días");
        }
    }
}
```

## 🧭 Explicación paso a paso

1. `Usuario` es una clase **abstracta**: agrupa el nombre y el código, comunes a
   cualquier usuario, y declara `limitePrestamosSimultaneos()` sin implementarlo,
   porque cada subtipo lo define distinto.
2. `Estudiante` y `Docente` **heredan** de `Usuario` y solo agregan lo que las hace
   diferentes: su propio límite de préstamos. No duplican `nombre` ni `codigo`.
3. En `Main`, `estudiante` y `docente` se declaran como tipo `Usuario` (la
   superclase), pero cada uno ejecuta la versión de `limitePrestamosSimultaneos()`
   de su propia subclase: esto también es polimorfismo, aplicado a la jerarquía de
   `Usuario`.
4. `Prestable` es una **interfaz**: no le importa si el recurso es un libro físico o
   digital, solo que pueda responder cuántos días dura el préstamo y describirse.
5. `Libro` y `RecursoDigital` **implementan** `Prestable`, cada uno con su propia
   regla de negocio para `calcularDiasDevolucion()`.
6. El segundo bloque de `Main` recorre una `List<Prestable>`: el mismo código llama
   a `calcularDiasDevolucion()` en cada elemento, y cada uno ejecuta su propia
   versión, sin que el bucle necesite saber si es un `Libro` o un `RecursoDigital`
   (sin `instanceof`, sin *casts*).

## ✅ Resultado esperado

Al ejecutar `Main.main(...)`, la salida por consola es:

```text
Ana Torres puede tener hasta 3 préstamos simultáneos.
Carlos Ibáñez puede tener hasta 10 préstamos simultáneos.
Libro: Estructuras de Datos -> 14 días
Recurso digital: Curso de Spring Boot (video) -> 7 días
```

## 📌 Idea clave

Las primeras dos líneas de la salida vienen de la jerarquía `Usuario` (herencia +
polimorfismo sobre `limitePrestamosSimultaneos()`); las últimas dos vienen de la
jerarquía `Prestable` (interfaz + polimorfismo sobre `calcularDiasDevolucion()`).
Son dos aplicaciones distintas del mismo principio: el código que usa el objeto
(`Main`) no necesita conocer su tipo concreto, solo el tipo declarado
(`Usuario` o `Prestable`).

## ❓ Preguntas de repaso

**1. [Selección]** ¿Cuándo conviene modelar una relación con una interfaz en vez
de con herencia?

- **A.** Cuando los tipos "son un" tipo más general y comparten estado, como
  `Usuario`.
- **B.** Cuando los tipos "pueden hacer" algo en común, sin relación de
  herencia entre sí, como `Prestable`.
- **C.** Nunca; en Java moderno la herencia siempre reemplaza a las interfaces.
- **D.** Solo dentro de proyectos que usan Spring.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. La interfaz `Prestable` conecta a `Libro` y
`RecursoDigital`, que no comparten ningún ancestro común más allá de `Object`;
la relación es "puede hacer", no "es un".

</details>

**2. [Selección múltiple]** Sobre el `Main` de este ejemplo, seleccioná
**todas** las afirmaciones correctas.

- **A.** `estudiante` y `docente` se declaran con el tipo `Usuario`, no con su
  tipo concreto.
- **B.** El bucle sobre `List<Prestable>` usa `instanceof` para distinguir
  `Libro` de `RecursoDigital`.
- **C.** Cada elemento de `List<Prestable>` ejecuta su propia versión de
  `calcularDiasDevolucion()`.
- **D.** `Estudiante` y `Docente` duplican los campos `nombre` y `codigo`.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C**. La B es falsa: el bucle no usa `instanceof`, esa
es justamente la ventaja del polimorfismo. La D es falsa: esos campos viven una
sola vez, en `Usuario`.

</details>

**3. [Abierta]** Si `Prestable` no existiera y `Libro`/`RecursoDigital` no
tuvieran ninguna interfaz en común, ¿qué habría que cambiar en el bucle de
`Main` para seguir imprimiendo la descripción y los días de devolución de
ambos?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Sin una interfaz común, `Main` no podría declarar
`List<Prestable>`; habría que usar listas separadas (una de `Libro`, otra de
`RecursoDigital`) o una lista de `Object` con `instanceof` y *casts* para
distinguir cada tipo antes de llamar al método correspondiente — perdiendo
exactamente la ventaja de tratar ambos tipos de forma uniforme.

</details>
