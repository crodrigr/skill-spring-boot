# 💡 Ejemplo 06 — Inyección de Dependencias: constructor, setter y campo

## 📚 Caso de estudio

Biblioteca Universitaria: un `ServicioPrestamos` que depende de un
`RepositorioLibros`, implementado con las tres formas de inyección para poder
compararlas.

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

## ✅ Resultado esperado

Las tres clases, con un `RepositorioLibros` que encuentra el ISBN pedido, devuelven
`true` al llamar a `prestar(...)`; la elección entre las tres no cambia el
resultado de negocio, solo la facilidad de mantener y probar el código.
