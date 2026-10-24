# 🟢 Básico 01 — Clasificar dependencias directas y transitivas

## 🧩 Problema

Biblioteca Universitaria tiene esta cadena de clases y te piden clasificar
todas las relaciones de dependencia que existen entre ellas.

## 💻 Código o contexto de partida

> 🏗️ **Capas**: el comentario `// com.…` sobre cada clase indica su paquete: `presentacion/controlador` (capa de presentación), `negocio/service` y `negocio/model` (capa de negocio) o `persistence/repository` (capa de persistencia). En un proyecto real cada clase va en su propio archivo.

```java
// com.biblioteca.persistence.repository.RepositorioLibros
public interface RepositorioLibros {
    Optional<Libro> buscarPorIsbn(String isbn);
}

// com.biblioteca.persistence.repository.RepositorioLibrosEnMemoria
public class RepositorioLibrosEnMemoria implements RepositorioLibros {
    @Override
    public Optional<Libro> buscarPorIsbn(String isbn) { /* ... */ return Optional.empty(); }
}

// com.biblioteca.negocio.service.ServicioPrestamos
public class ServicioPrestamos {
    private final RepositorioLibros repositorioLibros;

    public ServicioPrestamos(RepositorioLibros repositorioLibros) {
        this.repositorioLibros = repositorioLibros;
    }

    public boolean prestar(String isbn) {
        return repositorioLibros.buscarPorIsbn(isbn).isPresent();
    }
}

// com.biblioteca.presentacion.controlador.ControladorCatalogo
public class ControladorCatalogo {
    private final ServicioPrestamos servicioPrestamos;

    public ControladorCatalogo(ServicioPrestamos servicioPrestamos) {
        this.servicioPrestamos = servicioPrestamos;
    }

    public boolean solicitarPrestamo(String isbn) {
        return servicioPrestamos.prestar(isbn);
    }
}
```

Enumerá **todas** las relaciones de dependencia entre `ControladorCatalogo`,
`ServicioPrestamos`, `RepositorioLibros` y `RepositorioLibrosEnMemoria`, e
indicá para cada una si es directa o transitiva.

## 📏 Criterios de evaluación de la solución

- Identifica `ServicioPrestamos → RepositorioLibros` como directa.
- Identifica `ControladorCatalogo → ServicioPrestamos` como directa.
- Identifica `ControladorCatalogo → RepositorioLibros` como **transitiva**
  (nunca aparece mencionada en `ControladorCatalogo`).
- No confunde la relación de **implementación** (`RepositorioLibrosEnMemoria
  implements RepositorioLibros`) con una dependencia: es un contrato que
  cumple, no algo de lo que la interfaz dependa.

## 🚧 Restricciones

- No es necesario escribir código; el ejercicio se resuelve enumerando y
  clasificando las relaciones en texto.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-1
