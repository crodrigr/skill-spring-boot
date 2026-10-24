# 🟡 Intermedio 04 — Anotar correctamente una mini-aplicación de MediSalud

## 🧩 Problema

El siguiente fragmento de MediSalud está escrito sin ninguna anotación de Spring.
Hay que agregar las anotaciones correctas para que Spring Boot lo reconozca y
funcione como una API.

## 💻 Código o contexto de partida

> 🏗️ **Capas**: el comentario `// com.…` sobre cada clase indica su paquete: `presentacion/controlador` (capa de presentación), `negocio/service` y `negocio/model` (capa de negocio) o `persistence/repository` (capa de persistencia). En un proyecto real cada clase va en su propio archivo.

```java
// com.medisalud.negocio.model.Paciente
public record Paciente(String codigo, String nombre) {}

// Sin anotaciones: Spring Boot no reconoce ninguna de estas clases como bean

// com.medisalud.persistence.repository.RepositorioPacientes
public class RepositorioPacientes {
    public Optional<Paciente> buscarPorCodigo(String codigo) {
        return Optional.of(new Paciente(codigo, "Paciente de ejemplo")); // simplificado
    }
}

// com.medisalud.negocio.service.ServicioCitas
public class ServicioCitas {

    private final RepositorioPacientes repositorioPacientes;

    public ServicioCitas(RepositorioPacientes repositorioPacientes) {
        this.repositorioPacientes = repositorioPacientes;
    }

    public boolean tienePacienteRegistrado(String codigo) {
        return repositorioPacientes.buscarPorCodigo(codigo).isPresent();
    }
}

// com.medisalud.presentacion.controlador.ControladorCitas
public class ControladorCitas {

    private final ServicioCitas servicioCitas;

    public ControladorCitas(ServicioCitas servicioCitas) {
        this.servicioCitas = servicioCitas;
    }

    public boolean verificar(String codigo) {
        return servicioCitas.tienePacienteRegistrado(codigo);
    }
}

// com.medisalud.config.ConfiguracionApp
@Configuration
@ComponentScan(basePackages = "com.medisalud")
public class ConfiguracionApp {
}

// com.medisalud.Main
public class Main {
    public static void main(String[] args) {
        ConfigurableApplicationContext contexto =
                new AnnotationConfigApplicationContext(ConfiguracionApp.class);

        // Si las tres clases quedaron bien anotadas, el contenedor resuelve
        // este bean sin lanzar ninguna excepción.
        ControladorCitas controlador = contexto.getBean(ControladorCitas.class);
        System.out.println("¿Paciente P-001 registrado? " + controlador.verificar("P-001"));

        contexto.close();
    }
}
```

Agregá las anotaciones necesarias para que: (a) las tres clases sean beans
administrados por el contenedor IoC, con la anotación semánticamente correcta
según su capa; (b) `ControladorCitas` exponga un endpoint `GET
/pacientes/{codigo}/verificar` que invoque el método `verificar`. Indicá también
si hace falta agregar `@Autowired` en algún constructor, y por qué. Ejecutá
`Main` para confirmar que el contenedor logra construir `ControladorCitas`: si
falta alguna anotación, `contexto.getBean(...)` lanza una excepción en tiempo de
ejecución.

## 📏 Criterios de evaluación de la solución

- `RepositorioPacientes` queda anotada `@Repository` (acceso a datos), no
  `@Component` genérico ni `@Service`.
- `ServicioCitas` queda anotada `@Service` (lógica de negocio).
- `ControladorCitas` queda anotada `@RestController`, y el método `verificar` queda
  anotado `@GetMapping("/pacientes/{codigo}/verificar")` con el parámetro `codigo`
  anotado `@PathVariable`.
- La solución explica que **no** hace falta `@Autowired` en los constructores,
  porque cada clase tiene un único constructor y Spring lo usa automáticamente
  desde la versión 4.3 (visto en el Ejemplo 09).

## 🚧 Restricciones

- No cambies `Paciente`, `ConfiguracionApp` ni `Main`; solo agregá anotaciones
  (y el mapeo HTTP) sobre `RepositorioPacientes`, `ServicioCitas` y
  `ControladorCitas`.

## 📊 Dificultad

Intermedio

## ✅ Salida esperada al ejecutar `Main`

```text
¿Paciente P-001 registrado? true
```

## 🎓 Resultados de aprendizaje

RA-14
