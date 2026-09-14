# 💡 Ejemplo 05 — Java moderno: streams, lambdas, Optional y records

## 🏥 Caso de estudio

MediSalud: filtramos y transformamos una lista de citas médicas, buscamos un
médico por su identificador (que puede no existir), y modelamos un dato de
contacto como objeto inmutable.

## 💻 Código — streams y lambdas

```java
public record Cita(String paciente, String especialidad, boolean confirmada) {}

List<Cita> citasDelDia = List.of(
    new Cita("Ana Gómez", "Cardiología", true),
    new Cita("Luis Pérez", "Pediatría", false),
    new Cita("Marta Ruiz", "Cardiología", true),
    new Cita("Diego Soto", "Pediatría", true)
);

// Forma imperativa tradicional
List<String> pacientesConfirmadosCardiologiaImperativo = new ArrayList<>();
for (Cita cita : citasDelDia) {
    if (cita.especialidad().equals("Cardiología") && cita.confirmada()) {
        pacientesConfirmadosCardiologiaImperativo.add(cita.paciente());
    }
}

// Forma con streams y lambdas: mismo resultado, sin bucle ni lista mutable intermedia
List<String> pacientesConfirmadosCardiologia = citasDelDia.stream()
        .filter(cita -> cita.especialidad().equals("Cardiología"))
        .filter(Cita::confirmada)
        .map(Cita::paciente)
        .toList();
```

## 🧭 Explicación paso a paso (streams)

1. La versión imperativa necesita una lista mutable (`ArrayList`), una variable de
   control del bucle y una condición anidada.
2. La versión con streams describe **qué** se quiere (filtrar por especialidad,
   filtrar por confirmada, quedarse con el nombre del paciente), no **cómo**
   recorrerlo.
3. `Cita::confirmada` y `Cita::paciente` son *method references*: una forma aún más
   corta de escribir una lambda que solo invoca un método existente.
4. Ambas versiones producen el mismo resultado; la de streams es más corta y más
   difícil de romper con un error de índice o de inicialización.

## 💻 Código — Optional

```java
public Optional<String> buscarEspecialidadPorPaciente(List<Cita> citas, String nombrePaciente) {
    return citas.stream()
            .filter(cita -> cita.paciente().equals(nombrePaciente))
            .map(Cita::especialidad)
            .findFirst(); // puede no encontrar ninguna coincidencia
}

// Uso: nunca se accede al valor sin manejar el caso de ausencia
String especialidad = buscarEspecialidadPorPaciente(citasDelDia, "Luis Pérez")
        .orElse("Sin citas registradas");

String especialidadObligatoria = buscarEspecialidadPorPaciente(citasDelDia, "Paciente Inexistente")
        .orElseThrow(() -> new NoSuchElementException("No se encontró una cita para ese paciente"));
```

## 🧭 Explicación paso a paso (Optional)

1. `findFirst()` sobre un stream ya devuelve un `Optional<String>`: el propio tipo
   deja constancia de que el resultado puede no existir.
2. `orElse("Sin citas registradas")` da un valor por defecto cuando no hay
   coincidencia, sin arriesgarse a un `NullPointerException`.
3. `orElseThrow(...)` es la forma correcta de expresar "este valor es obligatorio
   en este punto; si falta, es un error de negocio", en vez de dejar que un `null`
   se propague silenciosamente.

## 💻 Código — record

```java
public record DatosContacto(String telefono, String email) {}

DatosContacto contacto = new DatosContacto("+54 11 5555-0100", "ana.gomez@mail.com");

// Generados automáticamente por el record: getters, equals, hashCode y toString
System.out.println(contacto.telefono());
System.out.println(contacto);
```

## ✅ Resultado esperado

```text
+54 11 5555-0100
DatosContacto[telefono=+54 11 5555-0100, email=ana.gomez@mail.com]
```

## 🧭 Explicación paso a paso (record)

1. `DatosContacto` reemplaza a una clase tradicional con campos `private final`,
   constructor, *getters*, `equals`, `hashCode` y `toString` escritos a mano.
2. Un `record` es **inmutable** por diseño: no tiene setters; para "cambiar" un
   dato de contacto se crea una instancia nueva.
3. `Cita`, usado en la sección de streams, también es un `record`: un objeto de
   valor simple es un candidato natural para modelarse así.
