# 🏆 Desafío 01 — Extender el grafo de dependencias de MediSalud con `@Component`

## 🧩 Problema

MediSalud quiere agregar un resumen de facturación por cita, apoyado en el
grafo de objetos que ya ensamblaste a mano en el Desafío 01 del Módulo 1
(`RepositorioPacientes → ServicioNotificaciones → ServicioCitas →
ControladorCitas`). Esta vez, en vez de ensamblarlo a mano, vas a dejar que el
contenedor IoC lo resuelva.

## 💻 Código o contexto de partida

Partís de las mismas cuatro clases del Desafío 01 del Módulo 1 (podés
copiarlas de `curso-spring-boot/modulo-spring-boot-01/material/ejercicios/desafio-01.md`
o de su solución), y agregás una quinta:

```java
public class ServicioResumenCitas {

    private final ServicioCitas servicioCitas;

    public ServicioResumenCitas(ServicioCitas servicioCitas) {
        this.servicioCitas = servicioCitas;
    }

    public String generarResumen(String codigoPaciente) {
        // ... lógica que usa servicioCitas para armar el resumen
        return "Resumen de citas para " + codigoPaciente;
    }
}
```

1. Anotá las cinco clases (`RepositorioPacientesEnMemoria`,
   `ServicioNotificaciones`, `ServicioCitas`, `ControladorCitas`,
   `ServicioResumenCitas`) con `@Component`, `@Repository` o `@Service`, según
   corresponda a su capa.
2. Escribí una clase de configuración (`@Configuration` +
   `@ComponentScan`) y un `main` que arme un `AnnotationConfigApplicationContext`,
   pida el bean `ServicioResumenCitas` al contenedor, y llame a
   `generarResumen("P-001")`.
3. En un párrafo, identificá cuál es la **nueva** dependencia transitiva que
   se creó al agregar `ServicioResumenCitas` (¿de qué depende
   transitivamente, sin mencionarlo directamente?), y explicá qué pasos de tu
   `main` automatizaría el contenedor si, en cambio de anotar las clases,
   solo hubieras escrito un `main` que las ensamblara a mano (como en el
   Desafío 01 del Módulo 1).

## 📏 Criterios de evaluación de la solución

- Las cinco clases quedan anotadas con la especialización correcta de
  `@Component` según su responsabilidad.
- El `main` obtiene `ServicioResumenCitas` del contenedor (no lo construye con
  `new`) y la ejecución de `generarResumen("P-001")` no lanza ninguna
  excepción.
- La explicación identifica correctamente que `ServicioResumenCitas` depende
  transitivamente de `RepositorioPacientes` y de `ServicioNotificaciones`, a
  través de `ServicioCitas`.
- La explicación final conecta el automatismo del contenedor con las fases de
  instanciación e inyección de dependencias del ciclo de vida de un bean
  (visto en el Módulo 1, Ejemplo 10).

## 🚧 Restricciones

- No se puede ensamblar `ServicioResumenCitas` a mano con `new`; el desafío
  exige que el `ApplicationContext` lo resuelva.
- No se permite ninguna dependencia circular entre las cinco clases.

## 📊 Dificultad

Desafío

## 🎓 Resultados de aprendizaje

RA-7, RA-8, RA-9
