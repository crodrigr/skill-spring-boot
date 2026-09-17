# 🟡 Intermedio 03 — Ordenar el ciclo de vida de un bean

## 🧩 Problema

Un `ServicioPrestamos` de Biblioteca Universitaria está administrado por el
contenedor IoC y define un método de inicialización y uno de limpieza.

## 💻 Código o contexto de partida

```java
public interface RepositorioLibros {
    Optional<Libro> buscarPorIsbn(String isbn);
}

public record Libro(String isbn, String titulo) {}

@Repository
public class RepositorioLibrosEnMemoria implements RepositorioLibros {
    @Override
    public Optional<Libro> buscarPorIsbn(String isbn) {
        return Optional.of(new Libro(isbn, "Libro de ejemplo"));
    }
}

@Component
public class ServicioPrestamos {

    private final RepositorioLibros repositorioLibros;

    public ServicioPrestamos(RepositorioLibros repositorioLibros) {
        this.repositorioLibros = repositorioLibros;
        // TODO: imprimir un mensaje identificando esta fase
    }

    @PostConstruct
    public void precargarCache() {
        // TODO: imprimir un mensaje identificando esta fase
    }

    public void prestar(String isbn) {
        // TODO: imprimir un mensaje identificando esta fase, usando repositorioLibros
    }

    @PreDestroy
    public void cerrarConexiones() {
        // TODO: imprimir un mensaje identificando esta fase
    }
}

@Configuration
@ComponentScan(basePackages = "com.biblioteca")
public class ConfiguracionApp {
}

public class Main {
    public static void main(String[] args) {
        ConfigurableApplicationContext contexto =
                new AnnotationConfigApplicationContext(ConfiguracionApp.class);

        ServicioPrestamos servicioPrestamos = contexto.getBean(ServicioPrestamos.class);
        servicioPrestamos.prestar("978-3-16-148410-0");

        contexto.close();
    }
}
```

Los siguientes cuatro eventos ocurren, en algún orden, al arrancar y luego apagar
la aplicación:

```text
(a) Se ejecuta cerrarConexiones()
(b) El contenedor llama al constructor con el RepositorioLibros ya resuelto
(c) Se ejecuta precargarCache()
(d) La aplicación queda corriendo y se puede llamar a prestar(isbn) las veces que haga falta
```

Primero, ordená los cuatro eventos **razonando** y explicá, para cada uno, en qué
fase del ciclo de vida de un bean ocurre (instanciación, inyección de
dependencias, inicialización, uso o destrucción). Después, completá los `TODO`
de `ServicioPrestamos` con un mensaje impreso por cada fase, ejecutá `Main`, y
confirmá que el orden real coincide con tu razonamiento.

## 📏 Criterios de evaluación de la solución

- El orden correcto es (b) → (c) → (d) → (a).
- (b) se asocia a instanciación + inyección de dependencias (el constructor ya
  recibe `repositorioLibros` resuelto).
- (c) se asocia a inicialización (`@PostConstruct`).
- (d) se asocia a la fase de uso del bean.
- (a) se asocia a destrucción (`@PreDestroy`), disparada al cerrar el contexto.

## 🚧 Restricciones

- No cambies la firma de ningún método ni la clase `ConfiguracionApp`/`Main`;
  solo agregá los mensajes impresos dentro de `ServicioPrestamos`.

## 📊 Dificultad

Intermedio

## ✅ Salida esperada al ejecutar `Main`

El orden exacto de las líneas (con el contenido que hayas elegido imprimir en
cada `TODO`) debe reflejar: instanciación → inicialización → uso → destrucción.
Por ejemplo, con mensajes simples:

```text
Instanciación: constructor de ServicioPrestamos
Inicialización: precargarCache()
Uso: prestando 978-3-16-148410-0
Destrucción: cerrarConexiones()
```

## 🎓 Resultados de aprendizaje

RA-7
