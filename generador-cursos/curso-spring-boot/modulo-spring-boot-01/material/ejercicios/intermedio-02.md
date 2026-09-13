# 🟡 Intermedio 02 — Elegir el tipo de Inyección de Dependencias adecuado

## 🧩 Problema

`ServicioFacturacion` de MediSalud necesita **obligatoriamente** un
`RepositorioFacturas` para funcionar, y **opcionalmente** un
`ServicioDescuentos` que puede no configurarse (en cuyo caso no se aplica ningún
descuento).

## 💻 Código o contexto de partida

```java
@Service
public class ServicioFacturacion {

    // completar: ¿cómo se reciben repositorioFacturas y servicioDescuentos?

    public double calcularTotal(double montoBase) {
        // usa repositorioFacturas para registrar, y servicioDescuentos si está presente
        return montoBase; // simplificado para el ejercicio
    }
}
```

Decidí qué tipo de inyección usar para `repositorioFacturas` y cuál para
`servicioDescuentos`, y escribí la declaración de la clase (campos y
constructor/setter, según corresponda) que refleje esa decisión.

## 📏 Criterios de evaluación de la solución

- `repositorioFacturas` se recibe por **constructor**, como campo `final`, porque
  es una dependencia obligatoria: sin ella la clase no puede cumplir su
  responsabilidad.
- `servicioDescuentos` se recibe por **setter** (no `final`, no en el
  constructor), porque es opcional y puede no configurarse.
- La solución justifica ambas elecciones citando el criterio de obligatoriedad de
  la dependencia, no solo "porque sí" o "porque es más corto de escribir".

## 🚧 Restricciones

- No se pide implementar la lógica de descuentos ni de facturación, solo la forma
  de recibir las dos dependencias.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-8
