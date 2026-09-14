# 🏆 Desafío 01 — Ensamblar a mano un grafo de objetos con Inyección de Dependencias

## 🧩 Problema

MediSalud necesita conectar cuatro clases que dependen unas de otras. Sin usar
Spring todavía, ensamblalas a mano en un método `main`, usando exclusivamente
inyección por constructor, y después explicá qué automatizaría un contenedor IoC
en este mismo escenario.

## 💻 Código o contexto de partida

```java
public interface RepositorioPacientes {
    Optional<Paciente> buscarPorCodigo(String codigo);
}

public class RepositorioPacientesEnMemoria implements RepositorioPacientes {
    // ... implementación con una lista o mapa en memoria
}

public class ServicioNotificaciones {
    public ServicioNotificaciones(RepositorioPacientes repositorioPacientes) { /* ... */ }
    public void avisarCitaProxima(String codigoPaciente) { /* ... */ }
}

public class ServicioCitas {
    public ServicioCitas(RepositorioPacientes repositorioPacientes,
                          ServicioNotificaciones servicioNotificaciones) { /* ... */ }
    public void agendar(String codigoPaciente) { /* ... */ }
}

public class ControladorCitas {
    public ControladorCitas(ServicioCitas servicioCitas) { /* ... */ }
    public void manejarSolicitudAgendar(String codigoPaciente) { /* ... */ }
}
```

1. Escribí un método `main` que cree, **en el orden correcto**, una instancia de
   cada clase, pasando cada dependencia por constructor, hasta construir un
   `ControladorCitas` completamente funcional.
2. Llamá a `controladorCitas.manejarSolicitudAgendar("P-001")` y verificá (con
   `System.out.println`) que la cadena completa se ejecuta sin errores.
3. Explicá, en un párrafo, qué pasos de tu `main` automatizaría un
   `ApplicationContext` de Spring si estas mismas clases estuvieran anotadas
   (`@Component`/`@Service`), y en qué se parece ese automatismo al ciclo de vida
   de un bean visto en el Ejemplo 10.

## 📏 Criterios de evaluación de la solución

- El orden de construcción respeta las dependencias: `RepositorioPacientes` se
  crea antes que `ServicioNotificaciones` y `ServicioCitas`; estos dos, antes que
  `ControladorCitas`.
- Ninguna clase crea sus propias dependencias con `new` en su interior: todas las
  reciben por constructor desde el `main`.
- La ejecución de `manejarSolicitudAgendar("P-001")` no lanza ninguna excepción y
  produce una salida coherente con la cadena de llamadas.
- La explicación final menciona explícitamente que un contenedor IoC automatizaría
  exactamente el orden de construcción y la resolución de dependencias que el
  `main` hizo a mano, y lo relaciona con las fases de instanciación e inyección de
  dependencias del ciclo de vida de un bean.

## 🚧 Restricciones

- No se puede usar ninguna anotación ni clase de Spring; el desafío se resuelve
  íntegramente en Java puro.
- No se permite que ninguna clase reciba una dependencia que no declare en su
  constructor (por ejemplo, mediante un campo estático o un *singleton* global).

## 📊 Dificultad

Desafío

## 🎓 Resultados de aprendizaje

RA-7, RA-8, RA-9
