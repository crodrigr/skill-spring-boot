# 🔴 Avanzado 01 — Detectar y romper una dependencia circular aplicando buenas prácticas

## 🧩 Problema

Biblioteca Universitaria tiene este diseño para gestionar préstamos y multas,
y no arranca: hay una dependencia circular entre dos clases.

## 💻 Código o contexto de partida

```java
public class ServicioPrestamos {
    private final ServicioMultas servicioMultas;

    public ServicioPrestamos(ServicioMultas servicioMultas) {
        this.servicioMultas = servicioMultas;
    }

    public boolean prestar(String isbn, String codigoUsuario) {
        if (servicioMultas.tieneMultasPendientes(codigoUsuario)) {
            return false;
        }
        // ... lógica de préstamo
        return true;
    }
}

public class ServicioMultas {
    private final ServicioPrestamos servicioPrestamos;

    public ServicioMultas(ServicioPrestamos servicioPrestamos) {
        this.servicioPrestamos = servicioPrestamos;
    }

    public boolean tieneMultasPendientes(String codigoUsuario) {
        // necesita consultar el historial de préstamos activos del usuario
        return servicioPrestamos.tienePrestamosActivos(codigoUsuario);
    }
}
```

1. Explicá por qué este diseño no se puede ensamblar con inyección por
   constructor (ni por ninguna otra forma de DI sin cambiar el diseño).
2. Rediseñá las clases aplicando al menos dos buenas prácticas de diseño de
   dependencias (depender de abstracciones, minimizar transitivas expuestas,
   evitar dependencias circulares) para que `ServicioPrestamos` pueda
   consultar si un usuario tiene multas pendientes, sin que exista ninguna
   dependencia circular.
3. Escribí un `Main` que ensamble tu rediseño y llame a `prestar(...)`.

## 📏 Criterios de evaluación de la solución

- La explicación identifica correctamente que ninguna de las dos clases puede
  construirse primero, porque cada una exige que la otra ya exista.
- El rediseño extrae la información que ambas necesitan (por ejemplo, si un
  usuario tiene préstamos activos) a un tercer módulo del que ambas dependen
  en un solo sentido (por ejemplo, un `RepositorioPrestamos` compartido), en
  vez de que se llamen mutuamente.
- No queda ninguna dependencia circular en el rediseño.
- El `Main` ensambla y ejecuta el rediseño sin errores.

## 🚧 Restricciones

- No es necesario usar anotaciones de Spring; el ejercicio se resuelve en
  Java puro, ensamblando los objetos a mano en un `main`.

## 📊 Dificultad

Avanzado

## 🎓 Resultados de aprendizaje

RA-7, RA-8
