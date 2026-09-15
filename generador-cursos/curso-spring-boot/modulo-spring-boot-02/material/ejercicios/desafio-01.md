# 🏆 Desafío 01 — Extender el grafo de dependencias de MediSalud con `@Component`

## 🧩 Problema

MediSalud quiere agregar un resumen de facturación por cita, apoyado en el
grafo de objetos que ya ensamblaste a mano en el Desafío 01 del Módulo 1
(`RepositorioPacientes → ServicioNotificaciones → ServicioCitas →
ControladorCitas`). Esta vez, en vez de ensamblarlo a mano, vas a dejar que el
contenedor IoC lo resuelva.

## 💻 Código o contexto de partida

Partís de las mismas cuatro clases del Desafío 01 del Módulo 1, ya
completamente implementadas (tal como quedaron en el Ejemplo 01 de este
módulo), y agregás una quinta:

<details>
<summary>📄 Ver código completo de <code>Paciente.java</code>, <code>RepositorioPacientes.java</code>, <code>RepositorioPacientesEnMemoria.java</code>, <code>ServicioNotificaciones.java</code>, <code>ServicioCitas.java</code> y <code>ControladorCitas.java</code> (reutilizados del Ejemplo 01 de este módulo, resolución del Desafío 01 del Módulo 1)</summary>

```java
public record Paciente(String codigo, String nombre) {}

public interface RepositorioPacientes {
    Optional<Paciente> buscarPorCodigo(String codigo);
}

public class RepositorioPacientesEnMemoria implements RepositorioPacientes {
    private final Map<String, Paciente> pacientes = Map.of(
            "P-001", new Paciente("P-001", "Ana Gómez")
    );

    @Override
    public Optional<Paciente> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(pacientes.get(codigo));
    }
}

public class ServicioNotificaciones {

    private final RepositorioPacientes repositorioPacientes;

    public ServicioNotificaciones(RepositorioPacientes repositorioPacientes) {
        this.repositorioPacientes = repositorioPacientes;
    }

    public void avisarCitaProxima(String codigoPaciente) {
        Paciente paciente = repositorioPacientes.buscarPorCodigo(codigoPaciente)
                .orElseThrow();
        System.out.println("Avisando a " + paciente.nombre() + " sobre su cita próxima.");
    }
}

public class ServicioCitas {

    private final RepositorioPacientes repositorioPacientes;
    private final ServicioNotificaciones servicioNotificaciones;

    public ServicioCitas(RepositorioPacientes repositorioPacientes,
                          ServicioNotificaciones servicioNotificaciones) {
        this.repositorioPacientes = repositorioPacientes;
        this.servicioNotificaciones = servicioNotificaciones;
    }

    public void agendar(String codigoPaciente) {
        repositorioPacientes.buscarPorCodigo(codigoPaciente).orElseThrow();
        System.out.println("Cita agendada para " + codigoPaciente);
        servicioNotificaciones.avisarCitaProxima(codigoPaciente);
    }
}

public class ControladorCitas {

    private final ServicioCitas servicioCitas;

    public ControladorCitas(ServicioCitas servicioCitas) {
        this.servicioCitas = servicioCitas;
    }

    public void manejarSolicitudAgendar(String codigoPaciente) {
        servicioCitas.agendar(codigoPaciente);
    }
}
```

</details>

```java
public class ServicioResumenCitas {

    private final ServicioCitas servicioCitas;

    public ServicioResumenCitas(ServicioCitas servicioCitas) {
        this.servicioCitas = servicioCitas;
    }

    public String generarResumen(String codigoPaciente) {
        // ... lógica que usa servicioCitas para armar el resumen
        return "Resumen de citas para " + codigoPaciente;
    }
}
```

1. Anotá las cinco clases (`RepositorioPacientesEnMemoria`,
   `ServicioNotificaciones`, `ServicioCitas`, `ControladorCitas`,
   `ServicioResumenCitas`) con `@Component`, `@Repository` o `@Service`, según
   corresponda a su capa.
2. Escribí una clase de configuración (`@Configuration` +
   `@ComponentScan`) y un `main` que arme un `AnnotationConfigApplicationContext`,
   pida el bean `ServicioResumenCitas` al contenedor, y llame a
   `generarResumen("P-001")`.
3. En un párrafo, identificá cuál es la **nueva** dependencia transitiva que
   se creó al agregar `ServicioResumenCitas` (¿de qué depende
   transitivamente, sin mencionarlo directamente?), y explicá qué pasos de tu
   `main` automatizaría el contenedor si, en cambio de anotar las clases,
   solo hubieras escrito un `main` que las ensamblara a mano (como en el
   Desafío 01 del Módulo 1).

## 📏 Criterios de evaluación de la solución

- Las cinco clases quedan anotadas con la especialización correcta de
  `@Component` según su responsabilidad.
- El `main` obtiene `ServicioResumenCitas` del contenedor (no lo construye con
  `new`) y la ejecución de `generarResumen("P-001")` no lanza ninguna
  excepción.
- La explicación identifica correctamente que `ServicioResumenCitas` depende
  transitivamente de `RepositorioPacientes` y de `ServicioNotificaciones`, a
  través de `ServicioCitas`.
- La explicación final conecta el automatismo del contenedor con las fases de
  instanciación e inyección de dependencias del ciclo de vida de un bean
  (visto en el Módulo 1, Ejemplo 10).

## 🚧 Restricciones

- No se puede ensamblar `ServicioResumenCitas` a mano con `new`; el desafío
  exige que el `ApplicationContext` lo resuelva.
- No se permite ninguna dependencia circular entre las cinco clases.

## 📊 Dificultad

Desafío

## 🎓 Resultados de aprendizaje

RA-7, RA-8, RA-9
