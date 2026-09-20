# 🏆 Desafío 01 — Agregar `Medico` al esquema de MediSalud

## 🧩 Problema

MediSalud ya tiene `Paciente`, `Cita` y `HistoriaClinica` persistidas y
relacionadas (Ejemplo 08). Ahora quiere registrar qué médico atiende cada
cita: un `Medico` puede atender varias `Cita`, y cada `Cita` tiene un único
`Medico` — la misma relación que ya practicaste en el Ejercicio Intermedio
03, ahora integrada en una aplicación completa y ejecutable.

## 💻 Código o contexto de partida

Partís de `Paciente`, `Cita`, `HistoriaClinica` y sus repositorios, ya
completamente mapeados en el Ejemplo 08:

<details>
<summary>📄 Ver código completo de <code>Paciente.java</code>, <code>Cita.java</code>, <code>HistoriaClinica.java</code>, <code>RepositorioPacientes.java</code> y <code>RepositorioCitas.java</code> (reutilizados del Ejemplo 08)</summary>

```java
// com.medisalud.persistences.entities.Paciente
@Entity
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigo;

    private String nombre;

    @OneToMany(mappedBy = "paciente")
    private List<Cita> citas = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "historia_clinica_id")
    private HistoriaClinica historiaClinica;

    protected Paciente() {
    }

    public Paciente(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public List<Cita> getCitas() { return citas; }

    public void asignarHistoriaClinica(HistoriaClinica historiaClinica) {
        this.historiaClinica = historiaClinica;
    }
}

// com.medisalud.persistences.entities.Cita
@Entity
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    private String motivo;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    protected Cita() {
    }

    public Cita(LocalDate fecha, String motivo, Paciente paciente) {
        this.fecha = fecha;
        this.motivo = motivo;
        this.paciente = paciente;
    }

    public LocalDate getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public Paciente getPaciente() { return paciente; }

    // TODO (este Desafío): agregar la relación con Medico
}

// com.medisalud.persistences.entities.HistoriaClinica
@Entity
public class HistoriaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String antecedentes;

    protected HistoriaClinica() {
    }

    public HistoriaClinica(String antecedentes) {
        this.antecedentes = antecedentes;
    }

    public String getAntecedentes() { return antecedentes; }
}

// com.medisalud.persistences.repositories.RepositorioPacientes
public interface RepositorioPacientes extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByCodigo(String codigo);
}

// com.medisalud.persistences.repositories.RepositorioCitas
public interface RepositorioCitas extends JpaRepository<Cita, Long> {
}
```

</details>

## 🎯 Tu tarea

**Capas MVC**: las entidades (`Paciente`, `Cita`, `HistoriaClinica` y la
nueva `Medico`) van en `com.medisalud.persistences.entities`; los
repositorios (incluido `RepositorioMedicos`) en
`com.medisalud.persistences.repositories`; y `Main`, en el paquete raíz
`com.medisalud`. Todavía no existen `services` ni `controllers` (Módulo 5).

1. Creá la entidad `Medico` (id, `nombre`), con el lado inverso de la
   relación con `Cita` (`@OneToMany(mappedBy = "medico")`), en
   `com.medisalud.persistences.entities`.
2. Agregá a `Cita` el campo `medico` con `@ManyToOne` y
   `@JoinColumn(name = "medico_id")`.
3. Creá `RepositorioMedicos extends JpaRepository<Medico, Long>`, con un
   método derivado `findByNombre(String nombre)`, en
   `com.medisalud.persistences.repositories`.
4. Escribí un `Main` (`@SpringBootApplication` + `CommandLineRunner`) que:
   - Guarde un `Paciente` con su `HistoriaClinica`.
   - Guarde un `Medico`.
   - Guarde dos `Cita` de ese paciente, ambas atendidas por ese médico.
   - Recargue al `Medico` por `findByNombre` y verifique, con
     `medico.getCitas().size()`, que atiende las dos citas.

## 📏 Criterios de evaluación de la solución

- `Medico` y la relación con `Cita` compilan y ejecutan sin excepciones
  contra H2.
- `Cita` es el lado propietario de la nueva relación (tiene `medico_id`);
  `Medico` es el lado inverso (`mappedBy`).
- `Medico` y `RepositorioMedicos` están en los paquetes de la capa
  `persistences` (`entities` y `repositories`), y los `import` entre
  paquetes son correctos.
- El `Main` verifica correctamente que el médico recargado tiene dos citas
  asociadas, sin lanzar ningún error de sesión cerrada (pensá si necesitás
  `@Transactional`, y dónde).

## 🚧 Restricciones

- No se puede ensamblar nada a mano con `new` fuera de un repositorio de
  Spring Data JPA: toda persistencia pasa por `RepositorioPacientes`,
  `RepositorioCitas` o `RepositorioMedicos`.
- No se permite ninguna dependencia circular entre las anotaciones de
  relación (cada relación tiene un único lado propietario).

## 📊 Dificultad

Desafío

## 🎓 Resultados de aprendizaje

RA-10, RA-11, RA-12
