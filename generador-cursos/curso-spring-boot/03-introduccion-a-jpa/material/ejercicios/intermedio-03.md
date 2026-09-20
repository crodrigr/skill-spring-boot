# 🟡 Intermedio 03 — Mapear una relación uno a muchos

## 🧩 Problema

MediSalud quiere registrar los médicos y qué citas atiende cada uno, pero
las dos entidades todavía no están relacionadas: un `Medico` puede atender
varias `Cita`, y cada `Cita` tiene un único `Medico`.

## 💻 Código o contexto de partida

```java
// Capa persistences: com.medisalud.persistences.entities
@Entity
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    protected Medico() {
    }

    public Medico(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    // TODO: agregar el lado inverso de la relación con Cita
}
```

```java
// Cita.java (com.medisalud.persistences.entities) ya existe (Ejemplo 08)
// con id, fecha, motivo y su relación con Paciente.
// TODO: agregar la relación con Medico.
```

1. Agregá a `Cita` un campo `medico` con `@ManyToOne` y `@JoinColumn(name =
   "medico_id")` — `Cita` es el lado propietario de esta nueva relación,
   igual que ya lo es de la relación con `Paciente`.
2. Agregá a `Medico` el lado inverso: una lista de `Cita` con
   `@OneToMany(mappedBy = "medico")`.
3. Identificá, en un comentario o en tu respuesta, cuál de las dos clases es
   el lado propietario y por qué.

## 📏 Criterios de evaluación de la solución

- `Medico` y `Cita` están en el mismo paquete de la capa `persistences`
  (`com.medisalud.persistences.entities`), así que no necesitan `import`
  entre sí.
- `Cita` declara `@ManyToOne @JoinColumn(name = "medico_id")` sobre un
  campo `Medico`.
- `Medico` declara `@OneToMany(mappedBy = "medico")` sobre una `List<Cita>`,
  usando el nombre exacto del campo declarado en `Cita`.
- La justificación identifica correctamente a `Cita` como lado propietario
  (tiene la columna de clave foránea).

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-11
