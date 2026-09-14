# 💡 Ejemplo 05 — Java moderno: streams, lambdas, Optional y records

## 🌍 Contexto

Spring y sus ejemplos posteriores (repositorios, servicios) asumen un estilo de
código concreto: **streams** para describir operaciones sobre colecciones sin
escribir el bucle que las recorre, **lambdas** para pasar comportamiento como
argumento, **`Optional`** para representar explícitamente que un valor puede no
existir, y **records** para modelar objetos de valor inmutables sin código
repetitivo. Antes de usar Spring, conviene dominar estas cuatro herramientas
comparándolas contra su equivalente "tradicional".

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

    static Optional<String> buscarEspecialidadPorPaciente(List<Cita> citas, String nombrePaciente) {
        return citas.stream()
                .filter(cita -> cita.paciente().equals(nombrePaciente))
                .map(Cita::especialidad)
                .findFirst(); // puede no encontrar ninguna coincidencia
    }

    public static void main(String[] args) {
        List<Cita> citas = citasDelDia();

        System.out.println("--- Streams y lambdas ---");
        System.out.println("Imperativo: " + pacientesConfirmadosCardiologiaImperativo(citas));
        System.out.println("Con streams: " + pacientesConfirmadosCardiologia(citas));

        System.out.println("--- Optional ---");
        String especialidad = buscarEspecialidadPorPaciente(citas, "Luis Pérez")
                .orElse("Sin citas registradas");
        System.out.println("Especialidad de Luis Pérez: " + especialidad);

        try {
            buscarEspecialidadPorPaciente(citas, "Paciente Inexistente")
                    .orElseThrow(() -> new NoSuchElementException("No se encontró una cita para ese paciente"));
        } catch (NoSuchElementException e) {
            System.out.println("Excepción esperada: " + e.getMessage());
        }

        System.out.println("--- Records ---");
        DatosContacto contacto = new DatosContacto("+54 11 5555-0100", "ana.gomez@mail.com");
        System.out.println(contacto.telefono());
        System.out.println(contacto);
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

5. `findFirst()` sobre un stream ya devuelve un `Optional<String>`: el propio
   tipo deja constancia de que el resultado puede no existir.
6. `orElse("Sin citas registradas")` da un valor por defecto cuando no hay
   coincidencia, sin arriesgarse a un `NullPointerException`.
7. `orElseThrow(...)` es la forma correcta de expresar "este valor es obligatorio
   en este punto; si falta, es un error de negocio": por eso el `main` lo captura
   con un `try/catch`, a propósito, para mostrar que la excepción ocurre cuando
   corresponde y no antes.

### Records

8. `DatosContacto` reemplaza a una clase tradicional con campos `private final`,
   constructor, *getters*, `equals`, `hashCode` y `toString` escritos a mano.
9. Un `record` es **inmutable** por diseño: no tiene setters; para "cambiar" un
   dato de contacto se crea una instancia nueva.
10. `Cita`, usado en la sección de streams, también es un `record`: un objeto de
    valor simple es un candidato natural para modelarse así.

## ✅ Resultado esperado

Al ejecutar `JavaModernoDemo.main(...)`:

```text
--- Streams y lambdas ---
Imperativo: [Ana Gómez, Marta Ruiz]
Con streams: [Ana Gómez, Marta Ruiz]
--- Optional ---
Especialidad de Luis Pérez: Pediatría
Excepción esperada: No se encontró una cita para ese paciente
--- Records ---
+54 11 5555-0100
DatosContacto[telefono=+54 11 5555-0100, email=ana.gomez@mail.com]
```
