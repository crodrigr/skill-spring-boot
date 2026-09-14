# 💡 Ejemplo 01 — ¿Qué es una dependencia? Tipos directas y transitivas

## 🌍 Contexto

En programación, una **dependencia** es una relación entre dos módulos de
código en la que uno (el **dependiente**) necesita al otro (la
**dependencia**) para funcionar correctamente: el módulo dependiente no puede
operar de forma autónoma sin él. Ya usaste dependencias sin nombrarlas así en
el Módulo 1 —cada vez que una clase recibía otra por constructor, estaba
declarando una dependencia—; ahora se formaliza el vocabulario y se distinguen
dos tipos:

- **Dependencia directa**: se establece explícitamente entre dos módulos. Si
  `A` usa un método de `B`, `A` depende directamente de `B`.
- **Dependencia transitiva**: se genera de forma indirecta. Si `A` depende de
  `B`, y `B` depende de `C`, entonces `A` depende transitivamente de `C`,
  aunque `A` nunca mencione a `C` en su propio código.

**Qué busca demostrar este ejemplo**: que en el mismo grafo de objetos que ya
ensamblaste a mano en el Desafío 01 del Módulo 1 (MediSalud) existen ambos
tipos de dependencia, y que identificarlos no requiere escribir código nuevo,
sino leer con el vocabulario correcto el código que ya funciona.

## 🏥 Caso de estudio

MediSalud: la cadena `ControladorCitas → ServicioCitas → RepositorioPacientes`
(más `ServicioNotificaciones`, de la que `ServicioCitas` también depende), ya
construida en el Desafío 01 del Módulo 1.

## 💻 Código

```java
public interface RepositorioPacientes {
    Optional<Paciente> buscarPorCodigo(String codigo);
}

public class RepositorioPacientesEnMemoria implements RepositorioPacientes {
    private final Map<String, Paciente> pacientes = Map.of(
            "P-001", new Paciente("Ana Gómez", "P-001")
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

public class Main {
    public static void main(String[] args) {
        RepositorioPacientes repositorioPacientes = new RepositorioPacientesEnMemoria();
        ServicioNotificaciones servicioNotificaciones = new ServicioNotificaciones(repositorioPacientes);
        ServicioCitas servicioCitas = new ServicioCitas(repositorioPacientes, servicioNotificaciones);
        ControladorCitas controladorCitas = new ControladorCitas(servicioCitas);

        controladorCitas.manejarSolicitudAgendar("P-001");
    }
}
```

## 🔍 Análisis: quién depende de quién

| Relación | Tipo | Por qué |
|---|---|---|
| `ServicioCitas → RepositorioPacientes` | Directa | `ServicioCitas` recibe `RepositorioPacientes` en su propio constructor y lo usa directamente. |
| `ServicioCitas → ServicioNotificaciones` | Directa | Mismo criterio: aparece en el constructor de `ServicioCitas`. |
| `ControladorCitas → ServicioCitas` | Directa | `ControladorCitas` recibe `ServicioCitas` en su constructor. |
| `ControladorCitas → RepositorioPacientes` | **Transitiva** | `ControladorCitas` **nunca** menciona `RepositorioPacientes` en su código; depende de él solo porque `ServicioCitas` (del que sí depende directamente) lo necesita. |
| `ControladorCitas → ServicioNotificaciones` | **Transitiva** | Mismo razonamiento: `ControladorCitas` no conoce esa clase, pero no podría funcionar sin ella. |

## 🧭 Explicación paso a paso

1. Cada línea de un constructor que recibe un parámetro de tipo interfaz o
   clase (`RepositorioPacientes repositorioPacientes`, por ejemplo) es una
   dependencia **directa** declarada explícitamente.
2. Para encontrar una dependencia **transitiva**, hay que "saltar" un nivel:
   `ControladorCitas` no importa ni menciona `RepositorioPacientes` en ningún
   lado, pero si `RepositorioPacientes` dejara de funcionar, `ControladorCitas`
   tampoco podría cumplir su trabajo, porque depende de `ServicioCitas`, que sí
   lo necesita.
3. Esto es exactamente lo que el Módulo 1 llamó "ensamblar a mano el grafo de
   objetos" en el Desafío 01: ese grafo **es** una cadena de dependencias
   directas que, vista de punta a punta, también contiene dependencias
   transitivas.

## ✅ Resultado esperado

```text
Cita agendada para P-001
Avisando a Ana Gómez sobre su cita próxima.
```

## 🔍 Ventajas y desventajas de usar dependencias

Que `ServicioCitas` dependa de `RepositorioPacientes` (en vez de resolver el
acceso a pacientes por su cuenta) trae ventajas y desventajas concretas, no
solo teóricas:

| | Ejemplo concreto en esta cadena |
|---|---|
| ✅ **Reutilización de código** | `RepositorioPacientes` lo puede usar tanto `ServicioCitas` como `ServicioNotificaciones`, sin duplicar la lógica de búsqueda de pacientes. |
| ✅ **Modularidad** | `ServicioCitas` se puede desarrollar y probar sin conocer cómo `RepositorioPacientesEnMemoria` guarda los datos por dentro. |
| ✅ **Especialización** | Quien trabaja en `ServicioNotificaciones` no necesita saber cómo se agenda una cita, solo cómo notificar. |
| ✅ **Facilidad de mantenimiento** | Cambiar cómo se buscan pacientes (por ejemplo, agregar caché) solo toca `RepositorioPacientesEnMemoria`, no `ServicioCitas` ni `ControladorCitas`. |
| ⚠️ **Acoplamiento** | Si `RepositorioPacientes.buscarPorCodigo(...)` cambiara su firma, `ServicioCitas` y `ServicioNotificaciones` tendrían que ajustarse. |
| ⚠️ **Complejidad** | Para entender qué hace `ControladorCitas.manejarSolicitudAgendar(...)` de punta a punta, hay que revisar tres clases más, no solo una. |
| ⚠️ **Vulnerabilidades de seguridad** | Si `RepositorioPacientes` fuera una librería externa (no propia), una falla de seguridad en ella afectaría a todo lo que depende de ella. |
| ⚠️ **Dependencia de terceros** | Si `RepositorioPacientes` viniera de un proveedor externo que deja de mantenerlo, reemplazarlo podría requerir tocar toda la cadena. |

## 🧠 Gestión de dependencias en Spring (adelanto)

En una aplicación Spring, esta misma cadena se resuelve automáticamente: el
contenedor IoC crea `RepositorioPacientesEnMemoria`, se lo entrega a
`ServicioNotificaciones` y a `ServicioCitas`, y a `ControladorCitas` le entrega
`ServicioCitas` — sin que nadie escriba el `main` manual de este ejemplo. Esto
incluye resolver automáticamente dependencias transitivas como
`ControladorCitas → RepositorioPacientes`. Cuando existe más de una
implementación candidata para una misma interfaz, Spring necesita ayuda para
decidir cuál usar: eso se ve en detalle, con un ejemplo completo, en el bloque
"Implementación y resolución de una dependencia general".

## ❓ Preguntas de repaso

**1. [Selección]** `A` usa un método de `B`, y `B` usa un método de `C`. `A`
nunca menciona a `C` en su código. ¿Qué tipo de dependencia tiene `A` respecto
de `C`?

- **A.** Directa.
- **B.** Transitiva.
- **C.** Ninguna; `A` no depende de `C` porque no lo menciona.
- **D.** Circular.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. `A` depende transitivamente de `C` a través de `B`,
aunque nunca lo mencione directamente: si `C` falla, `A` también se ve
afectado.

</details>

**2. [Selección múltiple]** Sobre la cadena `ControladorCitas → ServicioCitas →
RepositorioPacientes` de este ejemplo, seleccioná **todas** las afirmaciones
correctas.

- **A.** `ControladorCitas` depende directamente de `RepositorioPacientes`.
- **B.** `ServicioCitas` depende directamente de `RepositorioPacientes`.
- **C.** `ControladorCitas` depende transitivamente de `RepositorioPacientes`.
- **D.** `ControladorCitas` no depende de `RepositorioPacientes` de ninguna forma.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: B, C**. La A es falsa: `ControladorCitas` solo conoce a
`ServicioCitas` en su constructor. La D es falsa: sí depende, pero
transitivamente.

</details>

**3. [Abierta]** Explicá por qué identificar una dependencia transitiva puede
ser más difícil que identificar una directa, especialmente en un proyecto
grande.

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Una dependencia directa es visible con solo mirar el
constructor (o los campos) de una clase: aparece explícitamente en su código.
Una dependencia transitiva no aparece en ningún lado del código de la clase
que la sufre; hay que rastrear, clase por clase, de qué depende cada
dependencia directa, y así sucesivamente. En un proyecto con muchas capas
(como menciona la definición de dependencia transitiva), esa cadena puede
tener varios niveles, haciendo que un cambio en una clase "lejana" afecte a
clases que ni siquiera la conocen.

</details>
