# 🟡 Intermedio 03 — Ordenar el ciclo de vida completo y anotar con `@Component`

## 🧩 Problema

MediSalud tiene esta clase sin anotar, y te piden anotarla correctamente y
ordenar los eventos de su ciclo de vida.

## 💻 Código o contexto de partida

```java
public class RepositorioHistorialesEnMemoria {

    public RepositorioHistorialesEnMemoria(RepositorioPacientes repositorioPacientes) {
        // TODO: guardar la referencia recibida
    }

    // TODO: anotar el método de inicialización que precarga un índice en memoria
    public void precargarIndice() { /* ... */ }

    public String buscarHistorial(String codigoPaciente) { /* ... */ return "historial"; }

    // TODO: anotar el método que libera recursos al apagar la aplicación
    public void cerrar() { /* ... */ }
}
```

Los siguientes cinco eventos ocurren, en algún orden, al arrancar y luego
apagar una aplicación que administra esta clase como bean:

```text
(a) El contenedor invoca cerrar()
(b) La aplicación queda corriendo y se puede llamar a buscarHistorial(...) las veces que haga falta
(c) El contenedor invoca precargarIndice()
(d) El contenedor llama al constructor con RepositorioPacientes ya resuelto
(e) El contenedor entrega RepositorioPacientes al constructor de RepositorioHistorialesEnMemoria
```

1. Ordená los cinco eventos y explicá, para cada uno, en qué fase del ciclo
   de vida de un bean ocurre (instanciación, configuración, inicialización,
   uso o destrucción). **Pista**: dos de los cinco eventos corresponden a la
   misma fase, en el mismo momento.
2. Completá los `TODO` de la clase con las anotaciones que correspondan
   (`@Component` o una especialización, `@PostConstruct`, `@PreDestroy`).

## 📏 Criterios de evaluación de la solución

- El orden correcto es (d)/(e) juntos → (c) → (b) → (a), y la explicación
  aclara que (d) y (e) ocurren en el mismo momento (instanciación +
  configuración, dentro del mismo constructor).
- `RepositorioHistorialesEnMemoria` queda anotada con `@Repository` (acceso a
  datos).
- `precargarIndice()` queda anotado `@PostConstruct`.
- `cerrar()` queda anotado `@PreDestroy`.

## 🚧 Restricciones

- No es necesario implementar la lógica interna de `buscarHistorial(...)`.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-11, RA-12
