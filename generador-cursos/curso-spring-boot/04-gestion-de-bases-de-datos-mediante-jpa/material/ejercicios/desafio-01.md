# 🏆 Desafío 01 — Órdenes de compra de Biblioteca Universitaria

## 🧩 Problema

Biblioteca Universitaria necesita gestionar sus órdenes de compra de libros
nuevos: cada `OrdenCompra` tiene varias líneas de detalle
(`DetalleOrdenCompra`), cada una con el `isbn` y la `cantidad` de un libro
solicitado. A diferencia del Taller (un caso de facturación de MediSalud,
sobre otro par de entidades padre-hijas), este caso es de Biblioteca
Universitaria: diseñalo vos mismo, sin copiar directamente las entidades
del Taller.

Te piden implementar el **ciclo completo**: crear una orden con sus líneas
en una sola operación, leerla, actualizar la cantidad de una línea, y
eliminar otra línea de la colección verificando que desaparece también de
la base de datos.

## 💻 Código o contexto de partida

No se provee ningún scaffold: diseñá vos las entidades y su repositorio,
siguiendo el mismo criterio aplicado en el Taller (una relación padre-hijas
con `cascade`/`orphanRemoval`) pero sobre este par nuevo.

**Capas MVC**: las entidades van en
`com.biblioteca.persistences.entities`, el repositorio en
`com.biblioteca.persistences.repositories` y `Main` en el paquete raíz
`com.biblioteca`. Todavía no existen `services` ni `controllers`
(Módulo 5).

1. Diseñá `OrdenCompra` (con al menos `id` y `fecha`) y
   `DetalleOrdenCompra` (con al menos `id`, `isbn`, `cantidad`, y una
   relación `@ManyToOne` hacia `OrdenCompra`), con la relación
   `OrdenCompra`→`DetalleOrdenCompra` usando `cascade` y `orphanRemoval`.
2. Creá `RepositorioOrdenesCompra`, extendiendo `JpaRepository`, en
   `com.biblioteca.persistences.repositories`.
3. Escribí un `Main` que, sobre H2 en memoria:
   - **Cree** una `OrdenCompra` con al menos dos `DetalleOrdenCompra`, en
     una sola llamada a `save(...)` (aprovechando `cascade`).
   - **Lea** esa orden y muestre sus líneas.
   - **Actualice** la `cantidad` de una de las líneas y la guarde.
   - **Elimine** otra línea quitándola de la colección y volviendo a
     guardar la orden, verificando con una nueva lectura que
     `orphanRemoval` la eliminó de la base de datos.

## 📏 Criterios de evaluación de la solución

- `OrdenCompra` y `DetalleOrdenCompra` son entidades con nombres, paquete y
  caso de negocio propios de Biblioteca Universitaria, distintas de las
  usadas en el Taller (no una copia con los nombres cambiados).
- La relación `OrdenCompra`→`DetalleOrdenCompra` declara `cascade` (al
  menos `PERSIST`, recomendado `ALL`) y `orphanRemoval = true`.
- El `Main` ejecuta, en este orden, las cuatro operaciones: crear (con
  cascade), leer, actualizar una cantidad, y eliminar una línea (con
  orphanRemoval), verificando el estado tras cada una.
- Las entidades y el repositorio viven en los paquetes de la capa
  `persistences` (`entities` y `repositories`), con su línea `package`.
- El proyecto ejecuta sin excepciones contra H2 en memoria.

## 🚧 Restricciones

Las entidades deben ser distintas de las usadas en el Taller 01: no se
acepta reutilizar esos nombres ni esa relación, solo el mismo criterio de
diseño aplicado a un caso de negocio distinto.

## 📊 Dificultad

Desafío

## 🎓 Resultados de aprendizaje

RA-7, RA-8, RA-9, RA-12
