# 🟢 Básico 05 — Ubicar clases en la estructura por capas

## 🧩 Problema

Biblioteca Universitaria va a organizar su proyecto Spring Boot siguiendo la
convención por capas (`controller`, `service`, `repository`, `model`), pero
todavía no decidió en qué paquete va cada clase.

## 💻 Código o contexto de partida

```text
Clases a ubicar:
1. Libro — representa un libro del catálogo, con título, autor e ISBN.
2. CatalogoController — recibe peticiones HTTP para consultar el catálogo.
3. RepositorioLibros — consulta los libros disponibles (por ahora, en memoria).
4. ServicioPrestamos — decide si un usuario puede llevarse un libro prestado,
   aplicando las reglas de negocio de la biblioteca.
```

Completá y ejecutá este programa con tu decisión para cada clase:

```java
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, String> paqueteDeCadaClase = new LinkedHashMap<>();

        // TODO: completar, por ejemplo:
        // paqueteDeCadaClase.put("Libro", "model");

        paqueteDeCadaClase.forEach((clase, paquete) ->
                System.out.println(clase + " -> " + paquete));
    }
}
```

Indicá, para cada clase, en qué paquete de la estructura del Ejemplo 08
(`controller`, `service`, `repository` o `model`) debería ubicarse, y justificá
brevemente cada elección (podés escribir la justificación como comentario junto
a cada `put`).

## 📏 Criterios de evaluación de la solución

- `Libro` → `model` (representa una entidad del dominio, sin lógica de negocio ni
  acceso a datos).
- `CatalogoController` → `controller` (recibe peticiones HTTP).
- `RepositorioLibros` → `repository` (accede a los datos).
- `ServicioPrestamos` → `service` (contiene la regla de negocio de si se puede
  prestar un libro).
- Cada justificación menciona la responsabilidad de la clase, no solo el nombre.

## 🚧 Restricciones

- La salida esperada no se publica en este archivo porque coincide con la
  respuesta del ejercicio; verificá tu razonamiento contra los criterios de
  evaluación de arriba (y, como docente, contra `soluciones-ejercicios.md`).

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-13
