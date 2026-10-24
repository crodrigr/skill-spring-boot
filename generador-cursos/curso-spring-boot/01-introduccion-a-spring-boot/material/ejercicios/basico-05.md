# 🟢 Básico 05 — Ubicar clases en la estructura por capas

## 🧩 Problema

Biblioteca Universitaria va a organizar su proyecto Spring Boot siguiendo la
arquitectura por capas del curso: **presentación** (paquete `controllers`),
**negocio** (paquete `services`) y **persistencia** (paquete `persistences`, con
los subpaquetes `entities` y `repositories`), pero todavía no decidió en qué
capa y en qué paquete va cada clase.

## 💻 Código o contexto de partida

```text
Clases a ubicar:
1. Libro — representa un libro del catálogo, con título, autor e ISBN.
2. ControladorCatalogo — recibe peticiones HTTP para consultar el catálogo.
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
        // paqueteDeCadaClase.put("Libro", "persistences.entities");

        paqueteDeCadaClase.forEach((clase, paquete) ->
                System.out.println(clase + " -> " + paquete));
    }
}
```

Indicá, para cada clase, a qué capa pertenece y en qué paquete (`controllers`,
`services`, `persistences.repositories` o `persistences.entities`) debería
ubicarse, y justificá brevemente cada elección (podés escribir la justificación como comentario junto
a cada `put`).

## 📏 Criterios de evaluación de la solución

- `Libro` → capa de persistencia, `persistences.entities` (representa una entidad
  del dominio, sin lógica de negocio ni acceso a datos).
- `ControladorCatalogo` → capa de presentación, `controllers` (recibe peticiones
  HTTP).
- `RepositorioLibros` → capa de persistencia, `persistences.repositories`
  (accede a los datos).
- `ServicioPrestamos` → capa de negocio, `services` (contiene la regla de negocio
  de si se puede prestar un libro).
- Cada justificación menciona la responsabilidad de la clase, no solo el nombre.

## 🚧 Restricciones

- La salida esperada no se publica en este archivo porque coincide con la
  respuesta del ejercicio; verificá tu razonamiento contra los criterios de
  evaluación de arriba (y, como docente, contra `soluciones-ejercicios.md`).

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-13
