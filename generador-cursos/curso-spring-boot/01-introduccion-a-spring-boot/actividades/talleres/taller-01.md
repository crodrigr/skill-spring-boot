# 🛠️ Taller 01 — Modelar MediSalud en Java puro e identificar acoplamientos

## 🎯 Objetivo

Modelar en Java puro (sin ningún framework) un fragmento del dominio MediSalud con
las clases `Paciente`, `Cita` y `Medico`, y luego identificar manualmente en qué
puntos del código aparecería un acoplamiento que un contenedor IoC resolvería
después. (RA-1, RA-7, RA-9)

## 🏥 Contexto

MediSalud necesita registrar pacientes, médicos y las citas que los conectan. En
este taller el estudiante construye ese modelo **sin Spring**, para poder comparar
después, con criterio propio, qué cambiaría si el mismo código se administrara con
un contenedor IoC.

## 🪜 Pasos

1. Modelá la clase `Paciente` con al menos nombre y código de historia clínica.
2. Modelá la clase `Medico` con al menos nombre y especialidad.
3. Modelá la clase `Cita`, que relaciona un `Paciente` con un `Medico` en una fecha
   dada, y agregá un método `confirmar()` que cambie su estado.
4. Escribí una clase `ServicioCitas` que **cree sus propias instancias** de
   `RepositorioPacientes` y `RepositorioMedicos` con `new` dentro de su
   constructor (a propósito, para poder analizarlo después), y que ofrezca un
   método `agendar(String codigoPaciente, String codigoMedico, String fecha)` que
   use esos repositorios para crear y devolver una `Cita`.
5. Escribí un método `main` que use `ServicioCitas` para agendar al menos dos citas
   y las imprima.
6. **Análisis**: sin cambiar el código todavía, escribí una lista de los puntos
   exactos donde `ServicioCitas` queda acoplado a una implementación concreta (por
   ejemplo, `new RepositorioPacientesEnMemoria()`), y explicá qué pasaría si
   quisieras reemplazar esos repositorios por una versión que consulte una base de
   datos real, sin tocar el código de `ServicioCitas`.
7. Reescribí `ServicioCitas` para que reciba `RepositorioPacientes` y
   `RepositorioMedicos` por constructor, y ajustá el `main` para construir esos
   repositorios afuera y pasárselos.

## 💡 Ejemplo resuelto (versión acoplada, punto de partida del paso 4)

```java
public class ServicioCitas {

    private final RepositorioPacientes repositorioPacientes = new RepositorioPacientesEnMemoria();
    private final RepositorioMedicos repositorioMedicos = new RepositorioMedicosEnMemoria();

    public Cita agendar(String codigoPaciente, String codigoMedico, String fecha) {
        Paciente paciente = repositorioPacientes.buscarPorCodigo(codigoPaciente);
        Medico medico = repositorioMedicos.buscarPorCodigo(codigoMedico);
        return new Cita(paciente, medico, fecha);
    }
}
```

**Acoplamientos identificados en esta versión**:

- `ServicioCitas` conoce la clase concreta `RepositorioPacientesEnMemoria`, no solo
  el contrato `RepositorioPacientes`.
- No hay forma de pasarle a `ServicioCitas` un repositorio de prueba (por ejemplo,
  uno con datos fijos para un test) sin modificar la clase.
- Si `RepositorioPacientesEnMemoria` cambiara su constructor (por ejemplo, para
  pedir una configuración), habría que tocar `ServicioCitas` aunque su
  responsabilidad (agendar citas) no cambió en nada.

## 📦 Entregable

Un pequeño proyecto de archivos (en Java, cada clase o interfaz pública va en su
propio `.java`), con esta estructura:

```text
taller-01-medisalud/
├── Paciente.java
├── Medico.java
├── Cita.java
├── RepositorioPacientes.java
├── RepositorioMedicos.java
├── RepositorioPacientesEnMemoria.java
├── RepositorioMedicosEnMemoria.java
├── ServicioCitas.java     (versión final, con inyección por constructor)
├── Main.java              (agenda al menos dos citas)
└── acoplamientos.md       (lista del paso 6, redactada antes de refactorizar)
```

`ServicioCitas.java` y `Main.java` se entregan en su versión final del paso 7; la
versión acoplada del paso 4 no se entrega como archivo aparte, solo se documenta en
`acoplamientos.md`.

## 🧪 Casos de prueba

| Caso | Resultado esperado |
|---|---|
| Agendar una cita con un paciente y un médico existentes | Se crea una `Cita` con los datos correctos y se imprime sin errores |
| Listar los acoplamientos del paso 6 | Al menos dos acoplamientos concretos identificados, no una respuesta genérica como "está todo acoplado" |
| `ServicioCitas` refactorizado (paso 7) | El constructor recibe `RepositorioPacientes` y `RepositorioMedicos` como parámetros; no queda ningún `new` de un repositorio dentro de la clase |

## 📏 Criterios de evaluación

- Las tres clases de dominio (`Paciente`, `Medico`, `Cita`) están completas y
  coherentes con el enunciado.
- El análisis de acoplamientos del paso 6 es específico (cita líneas o expresiones
  concretas del código), no una afirmación general.
- La refactorización final del paso 7 elimina por completo la creación de
  repositorios con `new` dentro de `ServicioCitas`.
- El estudiante puede explicar, con sus palabras, qué haría un contenedor IoC en
  lugar del `main` manual del paso 7 (sin necesidad de implementarlo con Spring en
  este taller).
