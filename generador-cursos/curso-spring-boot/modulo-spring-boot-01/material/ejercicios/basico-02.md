# 🟢 Básico 02 — Reescribir un bucle con streams y lambdas

## 🧩 Problema

MediSalud necesita, a partir de una lista de citas del día, obtener el nombre de
los pacientes con cita de "Pediatría" que todavía **no** está confirmada.

## 💻 Código o contexto de partida

```java
public record Cita(String paciente, String especialidad, boolean confirmada) {}

List<Cita> citasDelDia = List.of(
    new Cita("Ana Gómez", "Cardiología", true),
    new Cita("Luis Pérez", "Pediatría", false),
    new Cita("Marta Ruiz", "Cardiología", true),
    new Cita("Diego Soto", "Pediatría", true),
    new Cita("Karina Ibáñez", "Pediatría", false)
);

// Versión imperativa a reescribir
List<String> pendientesPediatria = new ArrayList<>();
for (Cita cita : citasDelDia) {
    if (cita.especialidad().equals("Pediatría") && !cita.confirmada()) {
        pendientesPediatria.add(cita.paciente());
    }
}
```

Reescribí `pendientesPediatria` usando `citasDelDia.stream()` con `filter` y `map`,
sin declarar ninguna lista mutable ni usar un bucle `for`.

## 📏 Criterios de evaluación de la solución

- La solución usa `.stream()`, al menos un `filter` y un `map` (o `.toList()` al
  final).
- No declara ninguna variable mutable (`ArrayList`) ni usa `for`/`while`.
- El resultado contiene exactamente `"Luis Pérez"` y `"Karina Ibáñez"`, en ese orden
  o el que corresponda al orden de la lista original.

## 🚧 Restricciones

- No es necesario ejecutar el código; alcanza con escribir la expresión de stream
  correcta.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-2
