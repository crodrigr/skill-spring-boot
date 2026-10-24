# 🟡 Intermedio 01 — Refactorizar una clase acoplada aplicando Inyección de Dependencias

## 🧩 Problema

MediSalud tiene un `ServicioRecetas` que crea su propia dependencia con `new`,
lo que impide reemplazarla en un test.

## 💻 Código o contexto de partida

> 🏗️ **Capas**: el comentario `// com.…` sobre cada clase indica su paquete: `controllers` (capa de presentación), `services` (capa de negocio) o `persistences` (capa de persistencia). En un proyecto real cada clase va en su propio archivo.

```java
// com.medisalud.persistences.repositories.RepositorioMedicamentos
public interface RepositorioMedicamentos {
    Optional<Medicamento> buscarPorCodigo(String codigo);
}

// com.medisalud.persistences.entities.Medicamento
public record Medicamento(String codigo, String nombre, boolean requiereReceta) {}

// com.medisalud.persistences.repositories.RepositorioMedicamentosEnMemoria
public class RepositorioMedicamentosEnMemoria implements RepositorioMedicamentos {
    @Override
    public Optional<Medicamento> buscarPorCodigo(String codigo) {
        return Optional.of(new Medicamento(codigo, "Medicamento de ejemplo", true));
    }
}

// com.medisalud.services.ServicioRecetas
public class ServicioRecetas {

    private RepositorioMedicamentos repositorioMedicamentos = new RepositorioMedicamentosEnMemoria();

    public boolean puedeRecetarse(String codigoMedicamento) {
        return repositorioMedicamentos.buscarPorCodigo(codigoMedicamento)
                .map(Medicamento::requiereReceta)
                .orElse(false);
    }
}

// com.medisalud.Main
public class Main {
    public static void main(String[] args) {
        ServicioRecetas servicioRecetas = new ServicioRecetas();
        System.out.println(servicioRecetas.puedeRecetarse("MED-001"));
    }
}
```

Ejecutá `Main` para confirmar que la versión de partida funciona. Después,
elegí **una** forma de Inyección de Dependencias (constructor o propiedades)
y refactorizá `ServicioRecetas` para que reciba `RepositorioMedicamentos`
desde afuera, en vez de crearlo con `new`. Ajustá `Main` según corresponda.
`Main` debe seguir imprimiendo la misma salida después del cambio.

## 📏 Criterios de evaluación de la solución

- `ServicioRecetas` ya no contiene ningún `new RepositorioMedicamentosEnMemoria()`
  en su interior.
- La dependencia se recibe por constructor (campo `private final`) o por un
  método `set...` (campo sin `final`), de forma coherente con la forma
  elegida.
- `Main` construye `RepositorioMedicamentosEnMemoria` afuera de
  `ServicioRecetas` y se lo entrega explícitamente.
- La lógica de `puedeRecetarse(...)` no cambia.

## 🚧 Restricciones

- No es necesario agregar anotaciones de Spring; el ejercicio se resuelve en
  Java puro.

## 📊 Dificultad

Intermedio

## ✅ Salida esperada al ejecutar `Main` (antes y después de refactorizar)

```text
true
```

## 🎓 Resultados de aprendizaje

RA-4, RA-5, RA-6
