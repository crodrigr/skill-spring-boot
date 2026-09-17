# 🔴 Avanzado 01 — Diagnosticar una colección sin `orphanRemoval`

## 🧩 Problema

Un compañero de equipo te escribe: "quito una `Cita` de la lista de un
`Paciente` y vuelvo a guardar el paciente, pero la cita sigue apareciendo
en la base de datos. ¿Qué me falta?".

## 💻 Código o contexto de partida

```java
@Entity
public class Paciente {

    // ...

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<Cita> citas = new ArrayList<>();

    // ...

    public void quitarCita(Cita cita) {
        citas.remove(cita);
    }
}
```

```java
Paciente paciente = repositorioPacientes.findByCodigo("P-020").orElseThrow();
Cita citaAEliminar = paciente.getCitas().get(0);
paciente.quitarCita(citaAEliminar);
repositorioPacientes.save(paciente);
// La cita sigue en la base de datos, sin ningún paciente asociado (columna paciente_id = NULL)
```

## 📏 Criterios de evaluación de la solución

- Identifica que falta `orphanRemoval = true` en `Paciente.citas`: sin él,
  `cascade` propaga guardar/actualizar, pero no elimina automáticamente una
  hija que dejó de estar en la colección.
- Explica que, sin `orphanRemoval`, la `Cita` removida de la lista queda
  "huérfana" en la base de datos (con `paciente_id = NULL` o sin cambios,
  según el mapeo), en vez de eliminarse.
- Propone la corrección: agregar `orphanRemoval = true` a la anotación
  `@OneToMany` existente.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Avanzado

## 🎓 Resultados de aprendizaje

RA-8
