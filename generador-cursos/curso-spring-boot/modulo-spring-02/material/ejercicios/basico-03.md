# 🟢 Básico 03 — Identificar características de un Java Bean

## 🧩 Problema

Biblioteca Universitaria tiene esta clase y te piden evaluar si cumple con
las características de un Java Bean.

## 💻 Código o contexto de partida

```java
public class DatosPrestamo {

    private String isbn;
    private int diasRestantes;

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getDiasRestantes() {
        return diasRestantes;
    }

    public void setDiasRestantes(int diasRestantes) {
        this.diasRestantes = diasRestantes;
    }
}
```

1. Indicá si `DatosPrestamo` cumple con la convención de propiedades con
   *getter*/*setter* de un Java Bean, y por qué.
2. ¿Qué le faltaría agregar a esta clase para que también sea
   **serializable**?
3. `DatosPrestamo` no tiene ninguna anotación de Spring. ¿Es igualmente un
   Java Bean? Justificá.

## 📏 Criterios de evaluación de la solución

- Reconoce que `DatosPrestamo` sí cumple la convención de propiedades:
  campos privados, cada uno con su *getter* y su *setter* correspondiente.
- Indica correctamente que haría falta implementar `java.io.Serializable`
  para que sea serializable.
- Responde correctamente que sí puede ser un Java Bean sin ninguna anotación
  de Spring: "ser un Java Bean" (en el sentido clásico de propiedades) no
  depende de estar administrado por el contenedor de Spring.

## 🚧 Restricciones

- No es necesario ejecutar código; el ejercicio se resuelve analizando la
  clase dada.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-10
