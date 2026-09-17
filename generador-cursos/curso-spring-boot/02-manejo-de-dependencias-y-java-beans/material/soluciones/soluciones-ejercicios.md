# 🔑 Soluciones — Ejercicios Módulo 2

Material docente. No enlazar desde archivos de audiencia estudiante (salvo la
subsección "Soluciones" de `specs/02-manejo-de-dependencias-y-java-beans.md`).

## 🟢 Básico 01 — Clasificar dependencias directas y transitivas

**Respuesta esperada**:

- `ServicioPrestamos → RepositorioLibros`: **directa** (aparece en el
  constructor de `ServicioPrestamos`).
- `CatalogoController → ServicioPrestamos`: **directa** (aparece en el
  constructor de `CatalogoController`).
- `CatalogoController → RepositorioLibros`: **transitiva** (nunca aparece en
  `CatalogoController`; depende de él solo porque `ServicioPrestamos` lo
  necesita).
- `RepositorioLibrosEnMemoria implements RepositorioLibros`: no es una
  dependencia, es una relación de **implementación** (cumple un contrato).

**Verificación**: se revisa que las tres relaciones de dependencia estén
correctamente clasificadas y que la relación de implementación no se confunda
con una dependencia.

## 🟢 Básico 02 — Ventajas y desventajas de una nueva dependencia

**Respuesta esperada**:

- **Ventaja**: reutilización de código — el equipo no necesita escribir ni
  mantener su propia lógica de generación de PDF, aprovechando una solución ya
  probada por otros equipos de la universidad.
- **Desventaja**: dependencia de terceros — si el proveedor de la librería
  discontinúa el producto o dejara de mantenerlo, la universidad tendría que
  migrar todos los reportes a otra solución, con el costo que eso implica.

**Verificación**: se acepta cualquier ventaja/desventaja de las listas del
Ejemplo 01 siempre que la explicación esté conectada específicamente al
escenario de los reportes en PDF, no repetida en abstracto.

## 🟡 Intermedio 02 — Resolver una ambigüedad de inyección con `@Qualifier`

**Explicación del error**: con `RecargoFijo` y `RecargoProgresivo` anotadas
`@Component`, Spring encuentra dos beans candidatos para el parámetro
`CalculadoraRecargo calculadoraRecargo` del constructor de `ServicioMultas`, y
falla al arrancar con `No qualifying bean of type 'CalculadoraRecargo'...
found 2: recargoFijo, recargoProgresivo`.

**Solución propuesta**:

```java
@Component
@Qualifier("fijo")
public class RecargoFijo implements CalculadoraRecargo {
    @Override
    public double calcular(int diasDeAtraso) {
        return diasDeAtraso > 0 ? 500.0 : 0.0;
    }
}

@Component
@Qualifier("progresivo")
public class RecargoProgresivo implements CalculadoraRecargo {
    @Override
    public double calcular(int diasDeAtraso) {
        return diasDeAtraso * 100.0;
    }
}

@Service
public class ServicioMultas {

    private final CalculadoraRecargo calculadoraRecargo;

    public ServicioMultas(@Qualifier("progresivo") CalculadoraRecargo calculadoraRecargo) {
        this.calculadoraRecargo = calculadoraRecargo;
    }

    public double calcularMulta(int diasDeAtraso) {
        return calculadoraRecargo.calcular(diasDeAtraso);
    }
}
```

**Verificación**: se revisa que ambas implementaciones conserven su
`@Qualifier` propio (no se elimina ninguna), y que el constructor de
`ServicioMultas` reciba específicamente `RecargoProgresivo` vía
`@Qualifier("progresivo")`.

## 🟡 Intermedio 01 — Refactorizar `ServicioRecetas`

**Solución propuesta (por constructor)**:

```java
public class ServicioRecetas {

    private final RepositorioMedicamentos repositorioMedicamentos;

    public ServicioRecetas(RepositorioMedicamentos repositorioMedicamentos) {
        this.repositorioMedicamentos = repositorioMedicamentos;
    }

    public boolean puedeRecetarse(String codigoMedicamento) {
        return repositorioMedicamentos.buscarPorCodigo(codigoMedicamento)
                .map(Medicamento::requiereReceta)
                .orElse(false);
    }
}

public class Main {
    public static void main(String[] args) {
        RepositorioMedicamentos repositorioMedicamentos = new RepositorioMedicamentosEnMemoria();
        ServicioRecetas servicioRecetas = new ServicioRecetas(repositorioMedicamentos);
        System.out.println(servicioRecetas.puedeRecetarse("MED-001"));
    }
}
```

**Salida al ejecutar `Main`** (idéntica antes y después): `true`.

**Verificación**: se revisa que no quede ningún `new
RepositorioMedicamentosEnMemoria()` dentro de `ServicioRecetas`, y que la
dependencia se reciba por constructor (o por *setter*, según la forma
elegida por el estudiante) sin cambiar la lógica de `puedeRecetarse(...)`.

## 🔴 Avanzado 01 — Romper una dependencia circular

**Explicación del problema**: ninguna de las dos clases puede construirse
primero: `ServicioPrestamos` exige un `ServicioMultas` ya construido, y
`ServicioMultas` exige un `ServicioPrestamos` ya construido. No hay ningún
orden de `new` que funcione; es una dependencia circular real, no solo un
error de estilo.

**Solución propuesta** (se extrae la información compartida a un
`RepositorioPrestamos` del que ambos dependen en un solo sentido):

```java
public interface RepositorioPrestamos {
    boolean tienePrestamosActivos(String codigoUsuario);
}

public class RepositorioPrestamosEnMemoria implements RepositorioPrestamos {
    @Override
    public boolean tienePrestamosActivos(String codigoUsuario) {
        return false; // simplificado para el ejercicio
    }
}

public class ServicioMultas {
    private final RepositorioPrestamos repositorioPrestamos;

    public ServicioMultas(RepositorioPrestamos repositorioPrestamos) {
        this.repositorioPrestamos = repositorioPrestamos;
    }

    public boolean tieneMultasPendientes(String codigoUsuario) {
        return repositorioPrestamos.tienePrestamosActivos(codigoUsuario);
    }
}

public class ServicioPrestamos {
    private final ServicioMultas servicioMultas;
    private final RepositorioPrestamos repositorioPrestamos;

    public ServicioPrestamos(ServicioMultas servicioMultas, RepositorioPrestamos repositorioPrestamos) {
        this.servicioMultas = servicioMultas;
        this.repositorioPrestamos = repositorioPrestamos;
    }

    public boolean prestar(String isbn, String codigoUsuario) {
        if (servicioMultas.tieneMultasPendientes(codigoUsuario)) {
            return false;
        }
        return true; // ... lógica de préstamo
    }
}

public class Main {
    public static void main(String[] args) {
        RepositorioPrestamos repositorioPrestamos = new RepositorioPrestamosEnMemoria();
        ServicioMultas servicioMultas = new ServicioMultas(repositorioPrestamos);
        ServicioPrestamos servicioPrestamos = new ServicioPrestamos(servicioMultas, repositorioPrestamos);

        System.out.println(servicioPrestamos.prestar("978-3-16-148410-0", "EST-010"));
    }
}
```

**Buenas prácticas aplicadas**: (1) depender de una abstracción compartida
(`RepositorioPrestamos`) en vez de que cada servicio dependa del otro; (2)
evitar la dependencia circular reorganizando la responsabilidad de consultar
préstamos activos en un tercer módulo, del que ambos dependen en un solo
sentido.

**Salida al ejecutar `Main`**: `true`.

**Verificación**: se revisa que `ServicioMultas` y `ServicioPrestamos` ya no
se inyecten mutuamente, que ambos dependan de `RepositorioPrestamos` (no uno
del otro para esa información), y que el `main` construya el grafo sin
errores.

## 🏆 Desafío 01 — Extender el grafo de MediSalud con `@Component`

**Solución propuesta**:

```java
public record Paciente(String codigo, String nombre) {}

public interface RepositorioPacientes {
    Optional<Paciente> buscarPorCodigo(String codigo);
}

@Repository
public class RepositorioPacientesEnMemoria implements RepositorioPacientes {
    private final Map<String, Paciente> pacientes = Map.of(
            "P-001", new Paciente("P-001", "Ana Gómez")
    );

    @Override
    public Optional<Paciente> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(pacientes.get(codigo));
    }
}

@Service
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

@Service
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

@Component
public class ControladorCitas {

    private final ServicioCitas servicioCitas;

    public ControladorCitas(ServicioCitas servicioCitas) {
        this.servicioCitas = servicioCitas;
    }

    public void manejarSolicitudAgendar(String codigoPaciente) {
        servicioCitas.agendar(codigoPaciente);
    }
}

@Service
public class ServicioResumenCitas {

    private final ServicioCitas servicioCitas;

    public ServicioResumenCitas(ServicioCitas servicioCitas) {
        this.servicioCitas = servicioCitas;
    }

    public String generarResumen(String codigoPaciente) {
        return "Resumen de citas para " + codigoPaciente;
    }
}

@Configuration
@ComponentScan(basePackages = "com.medisalud")
public class ConfiguracionApp {
}

public class Main {
    public static void main(String[] args) {
        ConfigurableApplicationContext contexto =
                new AnnotationConfigApplicationContext(ConfiguracionApp.class);

        ServicioResumenCitas servicioResumenCitas = contexto.getBean(ServicioResumenCitas.class);
        System.out.println(servicioResumenCitas.generarResumen("P-001"));

        contexto.close();
    }
}
```

**Salida al ejecutar `Main`**: `Resumen de citas para P-001`.

**Nueva dependencia transitiva**: `ServicioResumenCitas` depende
directamente de `ServicioCitas`, y transitivamente de `RepositorioPacientes`
y de `ServicioNotificaciones` (las mismas dependencias transitivas que ya
tenía `ControladorCitas`, heredadas ahora también por
`ServicioResumenCitas`).

**Qué automatizaría el contenedor**: exactamente el orden de construcción que
el Desafío 01 del Módulo 1 hizo a mano (`RepositorioPacientes` →
`ServicioNotificaciones` → `ServicioCitas` → `ServicioResumenCitas` /
`ControladorCitas`), correspondiente a las fases de instanciación e inyección
de dependencias del ciclo de vida de un bean.

**Verificación**: se revisa que las cinco clases tengan la anotación
correcta según su capa, que `contexto.getBean(ServicioResumenCitas.class)` no
lance ninguna excepción, y que la explicación identifique correctamente la
nueva dependencia transitiva.

## 🟢 Básico 03 — Identificar características de un Java Bean

**Respuesta esperada**:

1. Sí cumple la convención de propiedades: `isbn` y `diasRestantes` son
   campos privados, cada uno con su `getter` y su `setter`.
2. Para ser serializable, `DatosPrestamo` tendría que declarar
   `implements java.io.Serializable`.
3. Sí puede ser un Java Bean sin ninguna anotación de Spring: la convención de
   propiedades (características clásicas de un Java Bean) es independiente de
   si el contenedor de Spring administra o no la clase.

**Verificación**: se revisa que las tres respuestas distingan correctamente
entre la convención de propiedades (Java Bean clásico) y la administración
por parte del contenedor de Spring.

## 🟡 Intermedio 03 — Ciclo de vida completo y `@Component`

**Orden correcto**: (d) y (e) ocurren juntos (instanciación + configuración,
en el mismo constructor) → (c) → (b) → (a).

- (d)/(e): instanciación + configuración — el contenedor crea el objeto y le
  entrega `RepositorioPacientes` en la misma llamada al constructor.
- (c): inicialización (`@PostConstruct` → `precargarIndice()`).
- (b): uso.
- (a): destrucción (`@PreDestroy` → `cerrar()`).

**Solución propuesta**:

```java
@Repository
public class RepositorioHistorialesEnMemoria {

    private final RepositorioPacientes repositorioPacientes;

    public RepositorioHistorialesEnMemoria(RepositorioPacientes repositorioPacientes) {
        this.repositorioPacientes = repositorioPacientes;
    }

    @PostConstruct
    public void precargarIndice() { /* ... */ }

    public String buscarHistorial(String codigoPaciente) { /* ... */ return "historial"; }

    @PreDestroy
    public void cerrar() { /* ... */ }
}
```

**Verificación**: se revisa que el orden identifique correctamente que (d) y
(e) son la misma fase, y que las tres anotaciones (`@Repository`,
`@PostConstruct`, `@PreDestroy`) estén en el lugar correcto.
