# 🔑 Solución — Taller 01 (Modelar MediSalud e identificar acoplamientos)

Material docente. No enlazar desde archivos de audiencia estudiante (salvo la
subsección "Soluciones" de `specs/modulo-spring-boot-01.md`).

## 💻 Paso 1-3 — Clases de dominio

```java
public class Paciente {

    private final String nombre;
    private final String codigoHistoriaClinica;

    public Paciente(String nombre, String codigoHistoriaClinica) {
        this.nombre = nombre;
        this.codigoHistoriaClinica = codigoHistoriaClinica;
    }

    public String getNombre() { return nombre; }
    public String getCodigoHistoriaClinica() { return codigoHistoriaClinica; }
}

public class Medico {

    private final String nombre;
    private final String especialidad;

    public Medico(String nombre, String especialidad) {
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    public String getNombre() { return nombre; }
    public String getEspecialidad() { return especialidad; }
}

public class Cita {

    private final Paciente paciente;
    private final Medico medico;
    private final String fecha;
    private boolean confirmada = false;

    public Cita(Paciente paciente, Medico medico, String fecha) {
        this.paciente = paciente;
        this.medico = medico;
        this.fecha = fecha;
    }

    public void confirmar() {
        this.confirmada = true;
    }

    @Override
    public String toString() {
        return "Cita[" + paciente.getNombre() + " con " + medico.getNombre()
                + " (" + medico.getEspecialidad() + ") el " + fecha
                + ", confirmada=" + confirmada + "]";
    }
}
```

## 💻 Paso 4-5 — `ServicioCitas` acoplada y `main` inicial

```java
public interface RepositorioPacientes {
    Paciente buscarPorCodigo(String codigo);
}

public interface RepositorioMedicos {
    Medico buscarPorCodigo(String codigo);
}

public class RepositorioPacientesEnMemoria implements RepositorioPacientes {
    @Override
    public Paciente buscarPorCodigo(String codigo) {
        return new Paciente("Ana Gómez", codigo); // simplificado para el taller
    }
}

public class RepositorioMedicosEnMemoria implements RepositorioMedicos {
    @Override
    public Medico buscarPorCodigo(String codigo) {
        return new Medico("Dr. Carlos Ibáñez", "Cardiología"); // simplificado
    }
}

public class ServicioCitas {

    private final RepositorioPacientes repositorioPacientes = new RepositorioPacientesEnMemoria();
    private final RepositorioMedicos repositorioMedicos = new RepositorioMedicosEnMemoria();

    public Cita agendar(String codigoPaciente, String codigoMedico, String fecha) {
        Paciente paciente = repositorioPacientes.buscarPorCodigo(codigoPaciente);
        Medico medico = repositorioMedicos.buscarPorCodigo(codigoMedico);
        return new Cita(paciente, medico, fecha);
    }
}

public class MainAcoplado {
    public static void main(String[] args) {
        ServicioCitas servicioCitas = new ServicioCitas();
        System.out.println(servicioCitas.agendar("P-001", "M-010", "2026-09-20"));
        System.out.println(servicioCitas.agendar("P-002", "M-011", "2026-09-21"));
    }
}
```

## 🔍 Paso 6 — Lista completa de acoplamientos identificados

1. `ServicioCitas` instancia directamente `RepositorioPacientesEnMemoria` y
   `RepositorioMedicosEnMemoria` con `new`, en vez de depender solo de las
   interfaces `RepositorioPacientes` y `RepositorioMedicos`.
2. No existe ningún punto de entrada (constructor, setter) para reemplazar esos
   repositorios por una versión de prueba: un test de `ServicioCitas` quedaría
   forzado a usar los datos fijos y simplificados de la implementación en memoria.
3. Si se quisiera reemplazar `RepositorioPacientesEnMemoria` por una versión que
   consulte una base de datos real (por ejemplo `RepositorioPacientesJpa`), habría
   que **modificar el código fuente de `ServicioCitas`** para cambiar el `new`,
   aunque la responsabilidad de `ServicioCitas` (agendar citas) no cambió en nada.
4. Si `RepositorioPacientesEnMemoria` cambiara su propio constructor (por ejemplo,
   para pedir una configuración de conexión), ese cambio se propagaría a
   `ServicioCitas` sin que `ServicioCitas` tenga ninguna razón de negocio para
   cambiar.

**Qué pasaría al intentar usar una base de datos real sin refactorizar**: cualquier
cambio de implementación de los repositorios obliga a recompilar y modificar
`ServicioCitas`, aunque su lógica de agendar citas sea exactamente la misma; el
acoplamiento a una clase concreta convierte un cambio de infraestructura en un
cambio de código de negocio.

## 💻 Paso 7 — `ServicioCitas` refactorizada y `main` final

```java
public class ServicioCitas {

    private final RepositorioPacientes repositorioPacientes;
    private final RepositorioMedicos repositorioMedicos;

    public ServicioCitas(RepositorioPacientes repositorioPacientes, RepositorioMedicos repositorioMedicos) {
        this.repositorioPacientes = repositorioPacientes;
        this.repositorioMedicos = repositorioMedicos;
    }

    public Cita agendar(String codigoPaciente, String codigoMedico, String fecha) {
        Paciente paciente = repositorioPacientes.buscarPorCodigo(codigoPaciente);
        Medico medico = repositorioMedicos.buscarPorCodigo(codigoMedico);
        return new Cita(paciente, medico, fecha);
    }
}

public class MainDesacoplado {
    public static void main(String[] args) {
        RepositorioPacientes repositorioPacientes = new RepositorioPacientesEnMemoria();
        RepositorioMedicos repositorioMedicos = new RepositorioMedicosEnMemoria();

        ServicioCitas servicioCitas = new ServicioCitas(repositorioPacientes, repositorioMedicos);

        System.out.println(servicioCitas.agendar("P-001", "M-010", "2026-09-20"));
        System.out.println(servicioCitas.agendar("P-002", "M-011", "2026-09-21"));
    }
}
```

## ✅ Resultado esperado (ambas versiones)

```text
Cita[Ana Gómez con Dr. Carlos Ibáñez (Cardiología) el 2026-09-20, confirmada=false]
Cita[Ana Gómez con Dr. Carlos Ibáñez (Cardiología) el 2026-09-21, confirmada=false]
```

## 🧭 Explicación paso a paso

1. La versión acoplada y la refactorizada producen exactamente el mismo resultado
   de negocio: la refactorización no cambia **qué** hace `ServicioCitas`, solo
   **de dónde** obtiene sus dependencias.
2. En la versión refactorizada, `RepositorioPacientesEnMemoria` podría
   reemplazarse por `new RepositorioPacientesFalso()` en un test, sin tocar una
   sola línea de `ServicioCitas`.
3. Lo que hizo el `main` a mano en el Paso 7 (crear los repositorios y pasárselos
   a `ServicioCitas`) es exactamente lo que automatizaría un `ApplicationContext`
   de Spring si estas clases estuvieran anotadas (`@Component`/`@Repository`): el
   contenedor resolvería el mismo orden de construcción e inyectaría las mismas
   dependencias por constructor.

## 📏 Verificación

Se revisa que: (a) las tres clases de dominio existan y sean coherentes con el
enunciado; (b) la lista de acoplamientos del Paso 6 cite expresiones concretas del
código (no una afirmación genérica); (c) la versión final de `ServicioCitas` no
contenga ningún `new` de un repositorio en su interior; y (d) ambos `main`
impriman una salida equivalente a la mostrada arriba.
