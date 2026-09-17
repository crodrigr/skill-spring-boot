# 🟡 Intermedio 01 — Elegir `fetch` para una relación

## 🧩 Problema

Biblioteca Universitaria tiene dos relaciones `@OneToMany` en su sistema, y
te pide elegir el `fetch` apropiado para cada una, justificando la
elección.

## 💻 Código o contexto de partida

```text
Escenario A: Libro (1) → DetalleCatalogacion (N).
  Cada Libro tiene varios registros de "detalle de catalogación" (notas
  internas del bibliotecario). Estos detalles casi nunca se muestran junto
  con el libro; solo se consultan en una pantalla administrativa aparte,
  poco usada.

Escenario B: Factura (1) → LineaFactura (N).
  Cada vez que se muestra una Factura en pantalla, el sistema necesita
  mostrar también sus líneas de detalle: no tiene sentido mostrar una
  factura sin sus líneas, prácticamente en el 100% de los casos.
```

Para cada escenario, indicá qué `fetch` usarías (`LAZY` o `EAGER`) y por
qué.

## 📏 Criterios de evaluación de la solución

- Escenario A: recomienda `LAZY`, porque los detalles rara vez se
  necesitan junto con el `Libro`.
- Escenario B: recomienda `EAGER` (o discute que sería una excepción
  justificada al valor por defecto), porque las líneas casi siempre se
  necesitan junto con la `Factura`.
- Justifica ambas respuestas con el criterio correcto (frecuencia de uso
  conjunto), no solo con "porque sí" o memorizando el valor por defecto sin
  razonarlo.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-6
