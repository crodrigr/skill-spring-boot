# 🟡 Intermedio 02 — Resolver una ambigüedad de inyección con `@Qualifier`

## 🧩 Problema

Biblioteca Universitaria tiene dos formas de calcular el recargo por un
préstamo vencido, ambas implementando la misma interfaz. Al anotar las dos
como `@Component`, la aplicación deja de arrancar.

## 💻 Código o contexto de partida

```java
public interface CalculadoraRecargo {
    double calcular(int diasDeAtraso);
}

@Component
public class RecargoFijo implements CalculadoraRecargo {
    @Override
    public double calcular(int diasDeAtraso) {
        return diasDeAtraso > 0 ? 500.0 : 0.0;
    }
}

@Component
public class RecargoProgresivo implements CalculadoraRecargo {
    @Override
    public double calcular(int diasDeAtraso) {
        return diasDeAtraso * 100.0;
    }
}

@Service
public class ServicioMultas {

    private final CalculadoraRecargo calculadoraRecargo;

    public ServicioMultas(CalculadoraRecargo calculadoraRecargo) {
        this.calculadoraRecargo = calculadoraRecargo;
    }

    public double calcularMulta(int diasDeAtraso) {
        return calculadoraRecargo.calcular(diasDeAtraso);
    }
}
```

Al intentar arrancar el contenedor con estas tres clases, Spring falla con un
error de "no qualifying bean". La biblioteca decidió que, por ahora,
`ServicioMultas` debe usar siempre el `RecargoProgresivo`.

1. Explicá, citando el mensaje de error esperado, por qué falla el arranque.
2. Agregá las anotaciones `@Qualifier` necesarias (en las implementaciones y
   en el punto de inyección) para que `ServicioMultas` use específicamente
   `RecargoProgresivo`.

## 📏 Criterios de evaluación de la solución

- La explicación menciona que hay dos beans candidatos para
  `CalculadoraRecargo` y que Spring no puede elegir uno sin ayuda.
- `RecargoFijo` y `RecargoProgresivo` quedan anotadas con `@Qualifier` con
  nombres distintos (por ejemplo `"fijo"` y `"progresivo"`).
- El parámetro `calculadoraRecargo` del constructor de `ServicioMultas` queda
  anotado `@Qualifier("progresivo")`.
- La solución no elimina ninguna de las dos implementaciones: el objetivo es
  resolver la ambigüedad, no reducir a una sola opción.

## 🚧 Restricciones

- No cambies la lógica de `calcular(...)` en ninguna de las dos
  implementaciones.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-3, RA-9
