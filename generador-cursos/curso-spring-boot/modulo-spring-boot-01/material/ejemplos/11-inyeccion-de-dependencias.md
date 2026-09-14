# 💡 Ejemplo 11 — Inyección de Dependencias: constructor, setter y campo

## 📚 Caso de estudio

Biblioteca Universitaria: un `ServicioPrestamos` que depende de un
`RepositorioLibros`, implementado con las tres formas de inyección para poder
compararlas.

## 💻 Código — dependencia común a las tres versiones

```java
public interface RepositorioLibros {
    Optional<Libro> buscarPorIsbn(String isbn);
}

public record Libro(String isbn, String titulo) {}

public class RepositorioLibrosEnMemoria implements RepositorioLibros {

    private final List<Libro> libros = List.of(
            new Libro("978-3-16-148410-0", "Estructuras de Datos")
    );

    @Override
    public Optional<Libro> buscarPorIsbn(String isbn) {
        return libros.stream().filter(libro -> libro.isbn().equals(isbn)).findFirst();
    }
}
```

## 💻 Código — por constructor (recomendada)

```java
@Service
public class ServicioPrestamosConstructor {

    private final RepositorioLibros repositorioLibros; // puede ser final

    public ServicioPrestamosConstructor(RepositorioLibros repositorioLibros) {
        this.repositorioLibros = repositorioLibros;
    }

    public boolean prestar(String isbn) {
        return repositorioLibros.buscarPorIsbn(isbn).isPresent();
    }
}
```

## 💻 Código — por setter

```java
@Service
public class ServicioPrestamosSetter {

    private RepositorioLibros repositorioLibros; // no puede ser final

    @Autowired
    public void setRepositorioLibros(RepositorioLibros repositorioLibros) {
        this.repositorioLibros = repositorioLibros;
    }

    public boolean prestar(String isbn) {
        return repositorioLibros.buscarPorIsbn(isbn).isPresent();
    }
}
```

## 💻 Código — por campo

```java
@Service
public class ServicioPrestamosCampo {

    @Autowired
    private RepositorioLibros repositorioLibros; // Spring lo asigna por reflexión

    public boolean prestar(String isbn) {
        return repositorioLibros.buscarPorIsbn(isbn).isPresent();
    }
}
```

## 🔍 Análisis comparado

| Forma | Ventaja | Desventaja |
|---|---|---|
| Constructor | Dependencia obligatoria y explícita; permite `final`; se puede instanciar en un test con `new ServicioPrestamosConstructor(repositorioDePrueba)` sin contenedor | Constructores largos si hay muchas dependencias (suele ser una señal de que la clase hace demasiado) |
| Setter | Útil para dependencias realmente opcionales, que pueden cambiarse después de construir el objeto | El objeto puede existir temporalmente sin la dependencia asignada (estado inconsistente) |
| Campo | Muy compacta de escribir | Oculta las dependencias reales de la clase (no aparecen en ningún constructor ni setter); para instanciarla en un test sin contenedor, hay que recurrir a reflexión o a un framework de mocks |

## 🧭 Explicación paso a paso

1. Las tres versiones son **funcionalmente equivalentes** en una aplicación Spring
   en ejecución: el contenedor resuelve `RepositorioLibros` y se lo entrega a
   `ServicioPrestamos` de una forma u otra.
2. La diferencia aparece en las **pruebas** y en la **legibilidad**: con
   `ServicioPrestamosConstructor`, un test puede escribir
   `new ServicioPrestamosConstructor(repositorioFalso)` directamente, sin arrancar
   ningún `ApplicationContext`.
3. Con `ServicioPrestamosCampo`, no hay ningún constructor ni setter que reciba
   `RepositorioLibros`: para probarlo sin Spring hay que asignar el campo con
   reflexión o usar un framework como Mockito con `@InjectMocks`, lo cual agrega
   complejidad innecesaria.
4. Por eso Spring recomienda, desde hace varias versiones, **inyección por
   constructor** como forma preferida para dependencias obligatorias, dejando
   setter para casos realmente opcionales y evitando la inyección por campo en
   código nuevo.
5. `Main` hace visible esa diferencia: arma `porConstructor` y `porSetter` con una
   línea de código normal, pero necesita `java.lang.reflect.Field` para asignar
   `repositorioLibros` en `porCampo`, exactamente el "costo extra" del que habla
   el análisis comparado.

## 💻 Código — clase principal (`Main`)

Para comparar las tres formas en un mismo programa (sin necesidad de un contenedor
Spring), `Main` las instancia a mano, exactamente como lo haría un test unitario.
Las versiones por constructor y por setter se arman directamente; la de campo
**no tiene ningún constructor ni setter público** para `repositorioLibros`, así
que `Main` tiene que recurrir a reflexión para asignarlo — el mismo costo extra
que enfrentaría un test unitario real, y la prueba concreta de la desventaja de
esta forma señalada en el análisis comparado.

```java
import java.lang.reflect.Field;

public class Main {

    public static void main(String[] args) throws Exception {
        RepositorioLibros repositorio = new RepositorioLibrosEnMemoria();
        String isbnExistente = "978-3-16-148410-0";
        String isbnInexistente = "000-0-00-000000-0";

        // Por constructor: se arma completo en una sola línea
        ServicioPrestamosConstructor porConstructor = new ServicioPrestamosConstructor(repositorio);
        System.out.println("Por constructor -> " + porConstructor.prestar(isbnExistente));

        // Por setter: se arma en dos pasos
        ServicioPrestamosSetter porSetter = new ServicioPrestamosSetter();
        porSetter.setRepositorioLibros(repositorio);
        System.out.println("Por setter -> " + porSetter.prestar(isbnExistente));

        // Por campo: sin constructor ni setter, hace falta reflexión para asignarlo
        // (fuera de un contenedor Spring, esto es exactamente lo que habría que
        // hacer a mano en un test, o delegarlo en un framework como Mockito)
        ServicioPrestamosCampo porCampo = new ServicioPrestamosCampo();
        Field campoRepositorio = ServicioPrestamosCampo.class.getDeclaredField("repositorioLibros");
        campoRepositorio.setAccessible(true);
        campoRepositorio.set(porCampo, repositorio);
        System.out.println("Por campo -> " + porCampo.prestar(isbnExistente));

        System.out.println("ISBN inexistente por constructor -> " + porConstructor.prestar(isbnInexistente));
    }
}
```

## ✅ Resultado esperado

Al ejecutar `Main.main(...)`:

```text
Por constructor -> true
Por setter -> true
Por campo -> true
ISBN inexistente por constructor -> false
```

Las tres clases, con el mismo `RepositorioLibros`, devuelven el mismo resultado de
negocio; la elección entre las tres no cambia **qué** responden, solo qué tan
fácil es armarlas fuera de un contenedor Spring, como acaba de hacer `Main`.
