# 🟢 Básico 03 — Identificar la responsabilidad de cada capa

## 🧩 Problema

Para cada uno de los siguientes fragmentos de código, identificá a qué
capa de la arquitectura (`Controller`, `Service` o `Repository`)
pertenece, y explicá en una oración cuál sería su responsabilidad ahí.

## 💻 Código o contexto de partida

**Fragmento 1**:

```java
public interface RepositorioAutores extends JpaRepository<Autor, Long> {
}
```

**Fragmento 2**:

```java
@RestController
@RequestMapping("/autores")
public class ControladorAutores {
    // ...
}
```

**Fragmento 3**:

```java
@Service
public class ServicioAutores {
    private final RepositorioAutores repositorioAutores;
    // ...
}
```

**Pregunta adicional**: si `ControladorAutores` necesitara guardar un
`Autor`, ¿a cuál de las otras dos clases debería llamar directamente, y a
cuál nunca debería llamar directamente?

## 📏 Criterios de evaluación de la solución

- Fragmento 1: capa `Repository`; responsable del acceso a datos
  (`JpaRepository` ya trae las operaciones básicas).
- Fragmento 2: capa `Controller`; responsable de manejar la solicitud y
  respuesta HTTP sobre la ruta `/autores`.
- Fragmento 3: capa `Service`; responsable de la lógica de negocio,
  delegando en el repositorio.
- Pregunta adicional: `ControladorAutores` debería llamar a
  `ServicioAutores` (nunca directamente a `RepositorioAutores`), para
  respetar que cada capa solo se comunica con la de abajo.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-5
