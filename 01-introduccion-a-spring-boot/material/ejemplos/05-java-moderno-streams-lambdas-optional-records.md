# 💡 Ejemplo 05 — Java moderno: streams, lambdas, Optional y records

## 🌍 Contexto

Spring y sus ejemplos posteriores (repositorios, servicios) asumen un estilo de
código concreto, apoyado en cuatro herramientas de Java moderno:

- **Streams**: describen una operación sobre una colección (filtrar,
  transformar, agregar) sin escribir el bucle que la recorre.
- **Lambdas**: son funciones anónimas y compactas que se pasan como argumento a
  esas operaciones, en vez de escribir una clase aparte para cada una.
- **`Optional`**: representa explícitamente, a nivel de tipo, que un valor
  puede no existir, en vez de arriesgar un `NullPointerException` con un `null`
  silencioso.
- **Records**: son una forma compacta de declarar una clase inmutable que solo
  transporta datos, sin escribir a mano constructor, *getters*, `equals`,
  `hashCode` ni `toString`.

Antes de usar Spring, conviene dominar estas cuatro herramientas comparándolas
contra su equivalente "tradicional".

**Qué busca demostrar este ejemplo**: que, para cada una de las cuatro
herramientas, la versión moderna produce **el mismo resultado** que su
equivalente imperativo (bucle mutable, chequeo manual de `null`, clase escrita a
mano), pero con menos código y menos superficie para introducir errores —
comparación que el propio `main` deja a la vista, imprimiendo ambas versiones
una junto a la otra.

## 🏥 Caso de estudio

MediSalud: filtramos y transformamos una lista de citas médicas, buscamos la
especialidad de un paciente (que puede no existir), y modelamos un dato de
contacto como objeto inmutable.

## 💻 Código completo

```java
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class JavaModernoDemo {

    record Cita(String paciente, String especialidad, boolean confirmada) {}

    record DatosContacto(String telefono, String email) {}

    static List<Cita> citasDelDia() {
        return List.of(
                new Cita("Ana Gómez", "Cardiología", true),
                new Cita("Luis Pérez", "Pediatría", false),
                new Cita("Marta Ruiz", "Cardiología", true),
                new Cita("Diego Soto", "Pediatría", true)
        );
    }

    // Forma imperativa tradicional: bucle + lista mutable
    static List<String> pacientesConfirmadosCardiologiaImperativo(List<Cita> citas) {
        List<String> resultado = new java.util.ArrayList<>();
        for (Cita cita : citas) {
            if (cita.especialidad().equals("Cardiología") && cita.confirmada()) {
                resultado.add(cita.paciente());
            }
        }
        return resultado;
    }

    // Forma con streams y lambdas: mismo resultado, sin bucle ni lista mutable intermedia
    static List<String> pacientesConfirmadosCardiologia(List<Cita> citas) {
        return citas.stream()
                .filter(cita -> cita.especialidad().equals("Cardiología"))
                .filter(Cita::confirmada)
                .map(Cita::paciente)
                .toList();
    }

    // Forma tradicional: bucle + posible null, sin ninguna garantía en el tipo
    static String buscarEspecialidadPorPacienteTradicional(List<Cita> citas, String nombrePaciente) {
        for (Cita cita : citas) {
            if (cita.paciente().equals(nombrePaciente)) {
                return cita.especialidad();
            }
        }
        return null; // riesgo: quien llama puede olvidarse de comprobarlo
    }

    // Forma con Optional: el tipo deja constancia de que el valor puede no existir
    static Optional<String> buscarEspecialidadPorPaciente(List<Cita> citas, String nombrePaciente) {
        return citas.stream()
                .filter(cita -> cita.paciente().equals(nombrePaciente))
                .map(Cita::especialidad)
                .findFirst(); // puede no encontrar ninguna coincidencia
    }

    // Forma tradicional: clase escrita a mano, con todo lo que un record genera solo
    static class DatosContactoTradicional {
        private final String telefono;
        private final String email;

        DatosContactoTradicional(String telefono, String email) {
            this.telefono = telefono;
            this.email = email;
        }

        String getTelefono() {
            return telefono;
        }

        String getEmail() {
            return email;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DatosContactoTradicional otro)) return false;
            return telefono.equals(otro.telefono) && email.equals(otro.email);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(telefono, email);
        }

        @Override
        public String toString() {
            return "DatosContactoTradicional{telefono='" + telefono + "', email='" + email + "'}";
        }
    }

    public static void main(String[] args) {
        List<Cita> citas = citasDelDia();

        System.out.println("--- Streams y lambdas ---");
        System.out.println("Imperativo: " + pacientesConfirmadosCardiologiaImperativo(citas));
        System.out.println("Con streams: " + pacientesConfirmadosCardiologia(citas));

        System.out.println("--- Optional ---");
        String especialidadTradicional = buscarEspecialidadPorPacienteTradicional(citas, "Luis Pérez");
        System.out.println("Tradicional (puede ser null): " + especialidadTradicional);

        String especialidad = buscarEspecialidadPorPaciente(citas, "Luis Pérez")
                .orElse("Sin citas registradas");
        System.out.println("Con Optional: " + especialidad);

        String especialidadTradicionalInexistente =
                buscarEspecialidadPorPacienteTradicional(citas, "Paciente Inexistente");
        System.out.println("Tradicional, paciente inexistente (null, sin avisar): " + especialidadTradicionalInexistente);

        try {
            buscarEspecialidadPorPaciente(citas, "Paciente Inexistente")
                    .orElseThrow(() -> new NoSuchElementException("No se encontró una cita para ese paciente"));
        } catch (NoSuchElementException e) {
            System.out.println("Con Optional, paciente inexistente (excepción explícita): " + e.getMessage());
        }

        System.out.println("--- Records ---");
        DatosContactoTradicional contactoTradicional =
                new DatosContactoTradicional("+54 11 5555-0100", "ana.gomez@mail.com");
        System.out.println("Tradicional: " + contactoTradicional);

        DatosContacto contacto = new DatosContacto("+54 11 5555-0100", "ana.gomez@mail.com");
        System.out.println("Con record: " + contacto);
    }
}
```

## 🧭 Explicación paso a paso

### Streams y lambdas

1. `pacientesConfirmadosCardiologiaImperativo` necesita una lista mutable
   (`ArrayList`), una variable de control del bucle y una condición anidada.
2. `pacientesConfirmadosCardiologia` describe **qué** se quiere (filtrar por
   especialidad, filtrar por confirmada, quedarse con el nombre del paciente), no
   **cómo** recorrerlo.
3. `Cita::confirmada` y `Cita::paciente` son *method references*: una forma aún
   más corta de escribir una lambda que solo invoca un método existente.
4. Ambas versiones producen el mismo resultado; la de streams es más corta y más
   difícil de romper con un error de índice o de inicialización.

### Optional

5. `buscarEspecialidadPorPacienteTradicional` puede devolver `null`, y **nada en
   su firma** avisa de eso: quien la llama tiene que acordarse, por su cuenta,
   de comprobarlo antes de usar el resultado.
6. `findFirst()` sobre un stream ya devuelve un `Optional<String>`: el propio
   tipo deja constancia, a simple vista, de que el resultado puede no existir.
7. `orElse("Sin citas registradas")` da un valor por defecto cuando no hay
   coincidencia, sin arriesgarse a un `NullPointerException`.
8. `orElseThrow(...)` es la forma correcta de expresar "este valor es obligatorio
   en este punto; si falta, es un error de negocio": por eso el `main` lo captura
   con un `try/catch`, a propósito, para mostrar que la excepción ocurre cuando
   corresponde y no antes — a diferencia de la versión tradicional, que
   simplemente devuelve `null` en silencio.

### Records

9. `DatosContactoTradicional` es la clase tradicional: campos `private final`,
   constructor, *getters*, `equals`, `hashCode` y `toString`, todo escrito a
   mano, línea por línea.
10. `DatosContacto` (un `record`) genera automáticamente exactamente lo mismo
    que `DatosContactoTradicional` escribe a mano: constructor, *getters*,
    `equals`, `hashCode` y `toString`, en una sola línea de declaración.
11. Un `record` es **inmutable** por diseño: no tiene setters; para "cambiar" un
    dato de contacto se crea una instancia nueva.
12. `Cita`, usado en la sección de streams, también es un `record`: un objeto de
    valor simple es un candidato natural para modelarse así.

## ✅ Resultado esperado

Al ejecutar `JavaModernoDemo.main(...)`:

```text
--- Streams y lambdas ---
Imperativo: [Ana Gómez, Marta Ruiz]
Con streams: [Ana Gómez, Marta Ruiz]
--- Optional ---
Tradicional (puede ser null): Pediatría
Con Optional: Pediatría
Tradicional, paciente inexistente (null, sin avisar): null
Con Optional, paciente inexistente (excepción explícita): No se encontró una cita para ese paciente
--- Records ---
Tradicional: DatosContactoTradicional{telefono='+54 11 5555-0100', email='ana.gomez@mail.com'}
Con record: DatosContacto[telefono=+54 11 5555-0100, email=ana.gomez@mail.com]
```

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué gana un método que devuelve `Optional<String>` en vez de
`String`?

- **A.** Se ejecuta más rápido que su versión sin `Optional`.
- **B.** El tipo deja constancia de que el valor puede no existir.
- **C.** Ya no puede lanzar ninguna excepción.
- **D.** El código resultante es necesariamente más corto.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. `Optional` no cambia el rendimiento ni impide
excepciones; su valor es hacer explícita, a nivel de tipo, la posibilidad de
ausencia.

</details>

**2. [Selección múltiple]** Sobre `pacientesConfirmadosCardiologia` (la versión
con streams), seleccioná **todas** las afirmaciones correctas.

- **A.** Evita declarar una lista mutable intermedia.
- **B.** Garantiza que el filtrado se ejecute en paralelo.
- **C.** Permite encadenar `filter` y `map` de forma declarativa.
- **D.** `Cita::paciente` es una forma más corta de escribir una lambda que solo
  invoca un método existente.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: `.stream()` es secuencial por
defecto; el paralelismo requeriría `.parallelStream()` explícitamente.

</details>

**3. [Abierta]** Comparando `DatosContactoTradicional` con el `record
DatosContacto`, ¿qué escribió a mano la clase tradicional que el record generó
automáticamente?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: `DatosContactoTradicional` escribe a mano los campos
`private final`, el constructor, los *getters* (`getTelefono`, `getEmail`), y
sobreescribe `equals`, `hashCode` y `toString`. El `record DatosContacto`
obtiene exactamente esas mismas piezas (constructor, *getters* con el nombre del
campo, `equals`, `hashCode`, `toString`) solo con declarar
`record DatosContacto(String telefono, String email) {}`.

</details>
