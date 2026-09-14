# 🟡 Intermedio 02 — Elegir el tipo de Inyección de Dependencias adecuado

## 🧩 Problema

`ServicioFacturacion` de MediSalud necesita **obligatoriamente** un
`RepositorioFacturas` para funcionar, y **opcionalmente** un
`ServicioDescuentos` que puede no configurarse (en cuyo caso no se aplica ningún
descuento).

## 💻 Código o contexto de partida

```java
import java.util.ArrayList;
import java.util.List;

public interface RepositorioFacturas {
    void registrar(double monto);
}

public class RepositorioFacturasEnMemoria implements RepositorioFacturas {
    private final List<Double> facturas = new ArrayList<>();

    @Override
    public void registrar(double monto) {
        facturas.add(monto);
        System.out.println("Factura registrada por $" + monto);
    }
}

public interface ServicioDescuentos {
    double aplicar(double montoBase);
}

public class ServicioDescuentos10 implements ServicioDescuentos {
    @Override
    public double aplicar(double montoBase) {
        return montoBase * 0.9; // 10% de descuento
    }
}

@Service
public class ServicioFacturacion {

    // TODO: declarar repositorioFacturas (obligatoria) y servicioDescuentos (opcional)
    // TODO: agregar el constructor y/o setter que correspondan a cada una

    public double calcularTotal(double montoBase) {
        // TODO: aplicar servicioDescuentos si está presente; si no, usar montoBase
        // TODO: registrar el total en repositorioFacturas antes de devolverlo
        return montoBase; // reemplazar
    }
}

public class Main {
    public static void main(String[] args) {
        ServicioFacturacion sinDescuento = new ServicioFacturacion(new RepositorioFacturasEnMemoria());
        System.out.println("Total sin descuento: " + sinDescuento.calcularTotal(1000));

        ServicioFacturacion conDescuento = new ServicioFacturacion(new RepositorioFacturasEnMemoria());
        conDescuento.setServicioDescuentos(new ServicioDescuentos10());
        System.out.println("Total con descuento: " + conDescuento.calcularTotal(1000));
    }
}
```

Decidí qué tipo de inyección usar para `repositorioFacturas` y cuál para
`servicioDescuentos`, y completá la declaración de la clase (campos y
constructor/setter, según corresponda) para que `Main` compile y produzca la
salida esperada.

## 📏 Criterios de evaluación de la solución

- `repositorioFacturas` se recibe por **constructor**, como campo `final`, porque
  es una dependencia obligatoria: sin ella la clase no puede cumplir su
  responsabilidad.
- `servicioDescuentos` se recibe por **setter** (no `final`, no en el
  constructor), porque es opcional y puede no configurarse.
- La solución justifica ambas elecciones citando el criterio de obligatoriedad de
  la dependencia, no solo "porque sí" o "porque es más corto de escribir".

## 🚧 Restricciones

- No cambies `RepositorioFacturasEnMemoria`, `ServicioDescuentos10` ni `Main`;
  solo completá `ServicioFacturacion`.

## 📊 Dificultad

Intermedio

## ✅ Salida esperada al ejecutar `Main`

```text
Factura registrada por $1000.0
Total sin descuento: 1000.0
Factura registrada por $900.0
Total con descuento: 900.0
```

## 🎓 Resultados de aprendizaje

RA-8
