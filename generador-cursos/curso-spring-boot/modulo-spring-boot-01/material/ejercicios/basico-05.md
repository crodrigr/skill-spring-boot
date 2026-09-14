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

Indicá, para cada clase, en qué paquete de la estructura del Ejemplo 08
(`controller`, `service`, `repository` o `model`) debería ubicarse, y justificá
brevemente cada elección.

## 📏 Criterios de evaluación de la solución

- `Libro` → `model` (representa una entidad del dominio, sin lógica de negocio ni
  acceso a datos).
- `CatalogoController` → `controller` (recibe peticiones HTTP).
- `RepositorioLibros` → `repository` (accede a los datos).
- `ServicioPrestamos` → `service` (contiene la regla de negocio de si se puede
  prestar un libro).
- Cada justificación menciona la responsabilidad de la clase, no solo el nombre.

## 🚧 Restricciones

- No es necesario escribir código ni crear el proyecto; alcanza con indicar el
  paquete y justificarlo.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-13
