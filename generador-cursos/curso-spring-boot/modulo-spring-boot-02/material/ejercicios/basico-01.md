# 🟢 Básico 01 — Clasificar dependencias directas y transitivas

## 🧩 Problema

Biblioteca Universitaria tiene esta cadena de clases y te piden clasificar
todas las relaciones de dependencia que existen entre ellas.

## 💻 Código o contexto de partida

```java
public interface RepositorioLibros {
    Optional<Libro> buscarPorIsbn(String isbn);
}

public class RepositorioLibrosEnMemoria implements RepositorioLibros {
    @Override
    public Optional<Libro> buscarPorIsbn(String isbn) { /* ... */ return Optional.empty(); }
}

public class ServicioPrestamos {
    private final RepositorioLibros repositorioLibros;

    public ServicioPrestamos(RepositorioLibros repositorioLibros) {
        this.repositorioLibros = repositorioLibros;
    }

    public boolean prestar(String isbn) {
        return repositorioLibros.buscarPorIsbn(isbn).isPresent();
    }
}

public class CatalogoController {
    private final ServicioPrestamos servicioPrestamos;

    public CatalogoController(ServicioPrestamos servicioPrestamos) {
        this.servicioPrestamos = servicioPrestamos;
    }

    public boolean solicitarPrestamo(String isbn) {
        return servicioPrestamos.prestar(isbn);
    }
}
```

Enumerá **todas** las relaciones de dependencia entre `CatalogoController`,
`ServicioPrestamos`, `RepositorioLibros` y `RepositorioLibrosEnMemoria`, e
indicá para cada una si es directa o transitiva.

## 📏 Criterios de evaluación de la solución

- Identifica `ServicioPrestamos → RepositorioLibros` como directa.
- Identifica `CatalogoController → ServicioPrestamos` como directa.
- Identifica `CatalogoController → RepositorioLibros` como **transitiva**
  (nunca aparece mencionada en `CatalogoController`).
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
