# 🟡 Intermedio 03 — Ordenar el ciclo de vida de un bean

## 🧩 Problema

Un `ServicioPrestamos` de Biblioteca Universitaria está administrado por el
contenedor IoC y define un método de inicialización y uno de limpieza.

## 💻 Código o contexto de partida

```java
@Component
public class ServicioPrestamos {

    private final RepositorioLibros repositorioLibros;

    public ServicioPrestamos(RepositorioLibros repositorioLibros) {
        this.repositorioLibros = repositorioLibros;
    }

    @PostConstruct
    public void precargarCache() { /* ... */ }

    public void prestar(String isbn) { /* ... */ }

    @PreDestroy
    public void cerrarConexiones() { /* ... */ }
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

Ordená los cuatro eventos y explicá, para cada uno, en qué fase del ciclo de vida
de un bean ocurre (instanciación, inyección de dependencias, inicialización, uso o
destrucción).

## 📏 Criterios de evaluación de la solución

- El orden correcto es (b) → (c) → (d) → (a).
- (b) se asocia a instanciación + inyección de dependencias (el constructor ya
  recibe `repositorioLibros` resuelto).
- (c) se asocia a inicialización (`@PostConstruct`).
- (d) se asocia a la fase de uso del bean.
- (a) se asocia a destrucción (`@PreDestroy`), disparada al cerrar el contexto.

## 🚧 Restricciones

- No es necesario ejecutar código; el ejercicio se resuelve ordenando y explicando
  en texto.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-7
