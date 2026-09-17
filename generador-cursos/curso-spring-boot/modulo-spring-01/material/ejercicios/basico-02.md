# 🟢 Básico 02 — Reescribir un bucle con streams y lambdas

## 🧩 Problema

MediSalud necesita, a partir de una lista de citas del día, obtener el nombre de
los pacientes con cita de "Pediatría" que todavía **no** está confirmada.

## 💻 Código o contexto de partida

```java
import java.util.ArrayList;
import java.util.List;

public class Main {

    public record Cita(String paciente, String especialidad, boolean confirmada) {}

    public static void main(String[] args) {
        List<Cita> citasDelDia = List.of(
                new Cita("Ana Gómez", "Cardiología", true),
                new Cita("Luis Pérez", "Pediatría", false),
                new Cita("Marta Ruiz", "Cardiología", true),
                new Cita("Diego Soto", "Pediatría", true),
                new Cita("Karina Ibáñez", "Pediatría", false)
        );

        // Versión imperativa a reescribir
        List<String> pendientesPediatriaImperativo = new ArrayList<>();
        for (Cita cita : citasDelDia) {
            if (cita.especialidad().equals("Pediatría") && !cita.confirmada()) {
                pendientesPediatriaImperativo.add(cita.paciente());
            }
        }
        System.out.println("Imperativo: " + pendientesPediatriaImperativo);

        // TODO: reescribir usando citasDelDia.stream() con filter y map,
        // sin declarar ninguna lista mutable ni usar un bucle for.
        List<String> pendientesPediatria = null; // reemplazar por la versión con streams
        System.out.println("Con streams: " + pendientesPediatria);
    }
}
```

Completá `pendientesPediatria` usando `citasDelDia.stream()` con `filter` y `map`,
sin declarar ninguna lista mutable ni usar un bucle `for`, de modo que `Main`
imprima el mismo resultado en ambas líneas.

## 📏 Criterios de evaluación de la solución

- La solución usa `.stream()`, al menos un `filter` y un `map` (o `.toList()` al
  final).
- No declara ninguna variable mutable (`ArrayList`) ni usa `for`/`while`.
- El resultado contiene exactamente `"Luis Pérez"` y `"Karina Ibáñez"`, en ese orden
  o el que corresponda al orden de la lista original.

## 🚧 Restricciones

- No cambies la versión imperativa ni la lista `citasDelDia`; solo completá
  `pendientesPediatria`.

## 📊 Dificultad

Básico

## ✅ Salida esperada al ejecutar `Main`

```text
Imperativo: [Luis Pérez, Karina Ibáñez]
Con streams: [Luis Pérez, Karina Ibáñez]
```

## 🎓 Resultados de aprendizaje

RA-2
