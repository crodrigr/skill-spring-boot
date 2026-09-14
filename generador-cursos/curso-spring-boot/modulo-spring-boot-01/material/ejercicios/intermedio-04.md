# 🟡 Intermedio 04 — Anotar correctamente una mini-aplicación de MediSalud

## 🧩 Problema

El siguiente fragmento de MediSalud está escrito sin ninguna anotación de Spring.
Hay que agregar las anotaciones correctas para que Spring Boot lo reconozca y
funcione como una API.

## 💻 Código o contexto de partida

```java
// Sin anotaciones: Spring Boot no reconoce ninguna de estas clases como bean

public class RepositorioPacientes {
    public Optional<Paciente> buscarPorCodigo(String codigo) { /* ... */ return Optional.empty(); }
}

public class ServicioCitas {

    private final RepositorioPacientes repositorioPacientes;

    public ServicioCitas(RepositorioPacientes repositorioPacientes) {
        this.repositorioPacientes = repositorioPacientes;
    }

    public boolean tienePacienteRegistrado(String codigo) {
        return repositorioPacientes.buscarPorCodigo(codigo).isPresent();
    }
}

public class CitasController {

    private final ServicioCitas servicioCitas;

    public CitasController(ServicioCitas servicioCitas) {
        this.servicioCitas = servicioCitas;
    }

    public boolean verificar(String codigo) {
        return servicioCitas.tienePacienteRegistrado(codigo);
    }
}
```

Agregá las anotaciones necesarias para que: (a) las tres clases sean beans
administrados por el contenedor IoC, con la anotación semánticamente correcta
según su capa; (b) `CitasController` exponga un endpoint `GET
/pacientes/{codigo}/verificar` que invoque el método `verificar`. Indicá también
si hace falta agregar `@Autowired` en algún constructor, y por qué.

## 📏 Criterios de evaluación de la solución

- `RepositorioPacientes` queda anotada `@Repository` (acceso a datos), no
  `@Component` genérico ni `@Service`.
- `ServicioCitas` queda anotada `@Service` (lógica de negocio).
- `CitasController` queda anotada `@RestController`, y el método `verificar` queda
  anotado `@GetMapping("/pacientes/{codigo}/verificar")` con el parámetro `codigo`
  anotado `@PathVariable`.
- La solución explica que **no** hace falta `@Autowired` en los constructores,
  porque cada clase tiene un único constructor y Spring lo usa automáticamente
  desde la versión 4.3 (visto en el Ejemplo 09).

## 🚧 Restricciones

- No se pide implementar la lógica interna de `buscarPorCodigo`; alcanza con
  anotar correctamente las clases y el método del controlador.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-14
