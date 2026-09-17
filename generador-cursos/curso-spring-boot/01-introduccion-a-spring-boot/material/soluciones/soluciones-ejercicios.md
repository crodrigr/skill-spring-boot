# 🔑 Soluciones — Ejercicios Módulo 1

Material docente. No enlazar desde archivos de audiencia estudiante (salvo la
subsección "Soluciones" de `specs/01-introduccion-a-spring-boot.md`).

## 🟢 Básico 01 — Modelar Biblioteca Universitaria con POO

**Solución propuesta**:

```java
public abstract class Usuario {
    protected final String nombre;
    protected final String codigo;

    protected Usuario(String nombre, String codigo) {
        this.nombre = nombre;
        this.codigo = codigo;
    }

    public abstract int limitePrestamosSimultaneos();
}

public class Estudiante extends Usuario {
    public Estudiante(String nombre, String codigo) { super(nombre, codigo); }
    @Override public int limitePrestamosSimultaneos() { return 3; }
}

public class Docente extends Usuario {
    public Docente(String nombre, String codigo) { super(nombre, codigo); }
    @Override public int limitePrestamosSimultaneos() { return 10; }
}

public interface Prestable {
    int calcularDiasDevolucion();
    String descripcion();
}

public class Libro implements Prestable {
    @Override public int calcularDiasDevolucion() { return 14; }
    @Override public String descripcion() { return "Libro"; }
}

public class RecursoDigital implements Prestable {
    @Override public int calcularDiasDevolucion() { return 7; }
    @Override public String descripcion() { return "Recurso digital"; }
}
```

**Explicación**: `Usuario` agrupa lo común (nombre, código) y deja abstracto solo
lo que varía (el límite). `Prestable` no tiene relación de herencia con `Usuario`:
modela una capacidad ("puede prestarse"), no un tipo de usuario.

**Verificación**: se revisa que no se dupliquen `nombre`/`codigo` en `Estudiante` o
`Docente`, y que `Libro`/`RecursoDigital` no hereden de una clase común innecesaria.

## 🟢 Básico 02 — Reescribir un bucle con streams y lambdas

**Solución propuesta**:

```java
List<String> pendientesPediatria = citasDelDia.stream()
        .filter(cita -> cita.especialidad().equals("Pediatría"))
        .filter(cita -> !cita.confirmada())
        .map(Cita::paciente)
        .toList();
```

**Resultado esperado**: `["Luis Pérez", "Karina Ibáñez"]`.

**Verificación**: se revisa que la solución no declare ninguna lista mutable ni use
`for`/`while`, y que use dos condiciones de filtro (especialidad y confirmación).

## 🟢 Básico 03 — Leer un `pom.xml` y traducirlo a Gradle

**Respuesta esperada**:

- `groupId=com.biblioteca`, `artifactId=biblioteca-api`, `version=0.1.0`,
  `packaging=jar`.
- 2 dependencias; `spring-boot-starter-test` es de prueba, identificable por
  `<scope>test</scope>`.
- Traducción a Gradle:

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

**Verificación**: se revisa que la traducción use `testImplementation` (no
`implementation`) para la dependencia con `<scope>test</scope>`.

## 🟢 Básico 04 — ¿Framework o librería?

**Respuesta esperada**:

1. **Framework**: exige una estructura (controladores, servicios, repositorios) y
   es la herramienta la que invoca al código del desarrollador cuando llega una
   petición HTTP (Inversión de Control).
2. **Librería**: resuelve una necesidad puntual (formatear una fecha) y es el
   desarrollador quien decide cuándo llamarla; no impone ninguna estructura.
3. **Framework**: administra el ciclo de vida completo de los objetos (creación,
   inyección de dependencias, destrucción), invirtiendo el control sobre cuándo y
   cómo se crean esos objetos.
4. **Librería**: convierte JSON de forma puntual, invocada explícitamente por el
   desarrollador en el punto exacto donde se necesita.

**Explicación**: el criterio decisivo en los cuatro casos es quién controla el
flujo de ejecución: si la herramienta llama al código del desarrollador
(framework) o si el código del desarrollador llama a la herramienta (librería).

**Verificación**: se revisa que la justificación de cada caso mencione
explícitamente el control del flujo, no una descripción genérica de "es grande" o
"es pequeña".

## 🟢 Básico 05 — Ubicar clases en la estructura por capas

**Respuesta esperada**:

| Clase | Paquete | Justificación |
|---|---|---|
| `Libro` | `model` | Representa una entidad del dominio; no tiene lógica de negocio ni acceso a datos. |
| `CatalogoController` | `controller` | Recibe peticiones HTTP del catálogo. |
| `RepositorioLibros` | `repository` | Responsable de consultar los datos de los libros. |
| `ServicioPrestamos` | `service` | Contiene la regla de negocio de si un usuario puede llevarse un libro. |

**Verificación**: se revisa que cada justificación describa la responsabilidad de
la clase (qué hace), no solo repita su nombre.

## 🟡 Intermedio 01 — Refactorizar `ServicioNotificaciones`

**Solución propuesta**:

```java
public class ServicioNotificaciones {

    private final RepositorioUsuarios repositorioUsuarios;
    private final ClienteEmail clienteEmail;

    public ServicioNotificaciones(RepositorioUsuarios repositorioUsuarios, ClienteEmail clienteEmail) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.clienteEmail = clienteEmail;
    }

    public void notificarVencimientoProximo(String isbn, String codigoUsuario) {
        Usuario usuario = repositorioUsuarios.buscarPorCodigo(codigoUsuario);
        clienteEmail.enviar(
            usuario.email(),
            "Tu préstamo del libro " + isbn + " vence pronto."
        );
    }
}

public class Main {
    public static void main(String[] args) {
        RepositorioUsuarios repositorioUsuarios = new RepositorioUsuariosJpa();
        ClienteEmail clienteEmail = new ClienteEmailSmtp();

        ServicioNotificaciones servicio = new ServicioNotificaciones(repositorioUsuarios, clienteEmail);
        servicio.notificarVencimientoProximo("978-3-16-148410-0", "EST-010");
    }
}
```

**Salida al ejecutar `Main`** (idéntica a la de la versión acoplada original):

```text
Email a usuario_EST-010@universidad.edu: Tu préstamo del libro 978-3-16-148410-0 vence pronto.
```

**Beneficio para las pruebas**: ahora se puede escribir
`new ServicioNotificaciones(repositorioDePrueba, clienteEmailDePrueba)` con un
`RepositorioUsuarios` en memoria y un `ClienteEmail` que solo registra el mensaje
en una lista, sin conectarse a una base de datos real ni enviar un correo real.

**Verificación**: se revisa que no quede ningún `new RepositorioUsuariosJpa()` ni
`new ClienteEmailSmtp()` dentro de la clase, y que ambos campos sean `private
final` recibidos por constructor.

## 🟡 Intermedio 02 — Elegir el tipo de Inyección de Dependencias

**Solución propuesta**:

```java
@Service
public class ServicioFacturacion {

    private final RepositorioFacturas repositorioFacturas;
    private ServicioDescuentos servicioDescuentos; // opcional

    public ServicioFacturacion(RepositorioFacturas repositorioFacturas) {
        this.repositorioFacturas = repositorioFacturas;
    }

    @Autowired(required = false)
    public void setServicioDescuentos(ServicioDescuentos servicioDescuentos) {
        this.servicioDescuentos = servicioDescuentos;
    }

    public double calcularTotal(double montoBase) {
        double total = servicioDescuentos != null
                ? servicioDescuentos.aplicar(montoBase)
                : montoBase;
        repositorioFacturas.registrar(total);
        return total;
    }
}
```

**Explicación**: `repositorioFacturas` es obligatoria (sin ella la clase no puede
registrar nada) → constructor. `servicioDescuentos` es opcional (puede no existir)
→ setter, permitiendo que el campo quede en `null` si no se configura.

**Salida al ejecutar `Main`**:

```text
Factura registrada por $1000.0
Total sin descuento: 1000.0
Factura registrada por $900.0
Total con descuento: 900.0
```

**Verificación**: se revisa que `repositorioFacturas` sea `final` y esté en el
constructor, y que `servicioDescuentos` no sea `final` ni esté en el constructor.

## 🟡 Intermedio 03 — Ordenar el ciclo de vida de un bean

**Respuesta esperada**: orden correcto (b) → (c) → (d) → (a).

- (b) instanciación + inyección de dependencias (constructor con
  `RepositorioLibros` ya resuelto).
- (c) inicialización (`@PostConstruct` → `precargarCache()`).
- (d) uso (la aplicación corre y se puede llamar a `prestar(isbn)`).
- (a) destrucción (`@PreDestroy` → `cerrarConexiones()`, al cerrar el contexto).

**Mensajes propuestos para los `TODO`** (constructor, `precargarCache`,
`prestar`, `cerrarConexiones`, en ese orden en el código, pero ejecutados en el
orden b→c→d→a):

```java
public ServicioPrestamos(RepositorioLibros repositorioLibros) {
    this.repositorioLibros = repositorioLibros;
    System.out.println("Instanciación: constructor de ServicioPrestamos");
}

@PostConstruct
public void precargarCache() {
    System.out.println("Inicialización: precargarCache()");
}

public void prestar(String isbn) {
    System.out.println("Uso: prestando " + isbn);
}

@PreDestroy
public void cerrarConexiones() {
    System.out.println("Destrucción: cerrarConexiones()");
}
```

**Salida al ejecutar `Main`** (confirma empíricamente el orden razonado arriba):

```text
Instanciación: constructor de ServicioPrestamos
Inicialización: precargarCache()
Uso: prestando 978-3-16-148410-0
Destrucción: cerrarConexiones()
```

**Verificación**: se revisa que el orden razonado sea exactamente b→c→d→a, que
cada evento se asocie a la fase correcta del ciclo de vida, y que la salida real
de `Main` coincida con ese orden.

## 🟡 Intermedio 04 — Anotar correctamente una mini-aplicación de MediSalud

**Solución propuesta**:

```java
@Repository
public class RepositorioPacientes {
    public Optional<Paciente> buscarPorCodigo(String codigo) {
        return Optional.of(new Paciente(codigo, "Paciente de ejemplo"));
    }
}

@Service
public class ServicioCitas {

    private final RepositorioPacientes repositorioPacientes;

    public ServicioCitas(RepositorioPacientes repositorioPacientes) {
        this.repositorioPacientes = repositorioPacientes;
    }

    public boolean tienePacienteRegistrado(String codigo) {
        return repositorioPacientes.buscarPorCodigo(codigo).isPresent();
    }
}

@RestController
public class CitasController {

    private final ServicioCitas servicioCitas;

    public CitasController(ServicioCitas servicioCitas) {
        this.servicioCitas = servicioCitas;
    }

    @GetMapping("/pacientes/{codigo}/verificar")
    public boolean verificar(@PathVariable String codigo) {
        return servicioCitas.tienePacienteRegistrado(codigo);
    }
}
```

**Sobre `@Autowired`**: no hace falta agregarlo en ningún constructor. Las tres
clases tienen un único constructor, y desde Spring 4.3 el framework lo usa
automáticamente para inyectar las dependencias sin necesidad de la anotación
explícita (Ejemplo 09).

**Salida al ejecutar `Main`** (confirma que el contenedor pudo resolver las tres
dependencias sin lanzar ninguna excepción):

```text
¿Paciente P-001 registrado? true
```

**Verificación**: se revisa que `RepositorioPacientes` use `@Repository` (no
`@Component` ni `@Service`), que `ServicioCitas` use `@Service`, que
`CitasController` use `@RestController` con `@GetMapping` y `@PathVariable`
correctamente aplicados, y que la respuesta explique por qué no hace falta
`@Autowired`.

## 🔴 Avanzado 01 — Combinar polimorfismo con Inyección de Dependencias

**Solución propuesta**:

```java
public class NotificadorSms implements Notificador {
    @Override
    public void enviar(String destinatario, String mensaje) {
        System.out.println("SMS a " + destinatario + ": " + mensaje);
    }
}

public class NotificadorEmail implements Notificador {
    @Override
    public void enviar(String destinatario, String mensaje) {
        System.out.println("Email a " + destinatario + ": " + mensaje);
    }
}

public class ServicioRecordatorios {
    private final Notificador notificador;

    public ServicioRecordatorios(Notificador notificador) {
        this.notificador = notificador;
    }

    public void enviarRecordatorio(String destinatario, String mensaje) {
        notificador.enviar(destinatario, mensaje);
    }
}

public class Demo {
    public static void main(String[] args) {
        ServicioRecordatorios recordatoriosMediSalud = new ServicioRecordatorios(new NotificadorSms());
        recordatoriosMediSalud.enviarRecordatorio("+54 11 5555-0100", "Su cita es mañana a las 10:00.");

        ServicioRecordatorios recordatoriosBiblioteca = new ServicioRecordatorios(new NotificadorEmail());
        recordatoriosBiblioteca.enviarRecordatorio("estudiante@universidad.edu", "Su préstamo vence en 2 días.");
    }
}
```

**Explicación**: es polimorfismo porque `NotificadorSms` y `NotificadorEmail`
implementan el mismo contrato con comportamientos distintos; es inyección de
dependencias porque `ServicioRecordatorios` no decide cuál usar: la implementación
concreta se le entrega desde afuera, por constructor.

**Verificación**: se revisa que `ServicioRecordatorios` no mencione
`NotificadorSms` ni `NotificadorEmail` en su código, solo `Notificador`.

## 🏆 Desafío 01 — Ensamblar a mano un grafo de objetos con Inyección de Dependencias

**Solución propuesta**:

```java
public static void main(String[] args) {
    RepositorioPacientes repositorioPacientes = new RepositorioPacientesEnMemoria();
    ServicioNotificaciones servicioNotificaciones = new ServicioNotificaciones(repositorioPacientes);
    ServicioCitas servicioCitas = new ServicioCitas(repositorioPacientes, servicioNotificaciones);
    ControladorCitas controladorCitas = new ControladorCitas(servicioCitas);

    controladorCitas.manejarSolicitudAgendar("P-001");
}
```

**Orden de construcción**: `RepositorioPacientes` primero (no depende de nadie),
luego `ServicioNotificaciones` (depende del repositorio), luego `ServicioCitas`
(depende del repositorio y de las notificaciones), y por último
`ControladorCitas` (depende de `ServicioCitas`). Invertir este orden es imposible
en Java: no se puede pasar por constructor una referencia que todavía no existe.

**Qué automatizaría un `ApplicationContext`**: si las cuatro clases estuvieran
anotadas (`@Component`/`@Service`), el contenedor escanearía las clases, calcularía
este mismo orden de dependencias, instanciaría cada bean e inyectaría las
dependencias resueltas por constructor — exactamente lo que el `main` hizo a mano.
Esto corresponde a las fases de **instanciación** e **inyección de dependencias**
del ciclo de vida de un bean (Ejemplo 10): el contenedor decide el orden y ejecuta
la resolución de dependencias que aquí se escribió explícitamente.

**Verificación**: se revisa que el orden de construcción en el `main` respete las
dependencias declaradas, que ninguna clase use `new` sobre sus propias
dependencias, y que la explicación final relacione el automatismo con
instanciación e inyección de dependencias, no solo con "Spring hace magia".
