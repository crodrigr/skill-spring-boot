# 🟢 Básico 01 — Modelar Biblioteca Universitaria con POO

## 🧩 Problema

La biblioteca quiere registrar dos tipos de usuarios (`Estudiante`, `Docente`) y
dos tipos de recursos prestables (`Libro`, `RecursoDigital`), cada uno con su propia
regla de negocio.

## 💻 Código o contexto de partida

```text
Reglas de negocio:
- Todo usuario tiene nombre y código.
- Un Estudiante puede tener como máximo 3 préstamos simultáneos.
- Un Docente puede tener como máximo 10 préstamos simultáneos.
- Todo recurso prestable puede describirse y calcular sus días de devolución.
- Un Libro se presta por 14 días; un RecursoDigital, por 7 días.
```

Este esqueleto ya compila (con los métodos sin terminar de implementar,
marcados `TODO`), pero `Main` no imprime todavía lo que debería. Completá las
clases para que `Main` compile **y** produzca la salida esperada.

```java
import java.util.List;

public abstract class Usuario {
    // TODO: agregar nombre y código, y el método abstracto que falta

    protected Usuario(String nombre, String codigo) {
        // TODO
    }
}

public class Estudiante extends Usuario {
    // TODO: constructor y límite de préstamos (3)
}

public class Docente extends Usuario {
    // TODO: constructor y límite de préstamos (10)
}

public interface Prestable {
    // TODO: método(s) que debe implementar todo recurso prestable
}

public class Libro implements Prestable {
    // TODO: 14 días de devolución
}

public class RecursoDigital implements Prestable {
    // TODO: 7 días de devolución
}

public class Main {
    public static void main(String[] args) {
        Usuario estudiante = new Estudiante("Valentina Ríos", "EST-010");
        Usuario docente = new Docente("Marta Sosa", "DOC-002");

        System.out.println(estudiante.getNombre() + ": " + estudiante.limitePrestamosSimultaneos());
        System.out.println(docente.getNombre() + ": " + docente.limitePrestamosSimultaneos());

        List<Prestable> recursos = List.of(
                new Libro("Bases de Datos"),
                new RecursoDigital("Podcast de Arquitectura")
        );

        for (Prestable recurso : recursos) {
            System.out.println(recurso.descripcion() + " -> " + recurso.calcularDiasDevolucion() + " días");
        }
    }
}
```

## 📏 Criterios de evaluación de la solución

- Existe una clase o interfaz común para `Estudiante` y `Docente`, con un método
  para el límite de préstamos que cada subtipo redefine.
- Existe una interfaz común para `Libro` y `RecursoDigital`, con los métodos para
  describirse y calcular días de devolución.
- La solución no duplica `nombre` y `codigo` en `Estudiante` y `Docente`.
- Se distingue correctamente cuándo usar herencia (tipos que "son un" `Usuario`) y
  cuándo usar interfaz (tipos que "pueden hacer" algo, sin relación de herencia
  entre sí).
- `Main` compila y, al ejecutarse, produce exactamente la salida esperada.

## 🚧 Restricciones

- No agregues atributos ni métodos que `Main` no necesite.

## 📊 Dificultad

Básico

## ✅ Salida esperada al ejecutar `Main`

```text
Valentina Ríos: 3
Marta Sosa: 10
Libro: Bases de Datos -> 14 días
Recurso digital: Podcast de Arquitectura -> 7 días
```

## 🎓 Resultados de aprendizaje

RA-1
