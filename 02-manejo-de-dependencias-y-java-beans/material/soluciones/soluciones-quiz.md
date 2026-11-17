# 🔑 Clave del Quiz 02 — Manejo de Dependencias y Java Beans

Material docente. No enlazar desde archivos de audiencia estudiante (salvo la
subsección "Soluciones" de `specs/02-manejo-de-dependencias-y-java-beans.md`).

Desde esta iteración, `quizzes/quiz-02.md` (formato entrevista técnica) ya
incluye la respuesta de cada ítem oculta en un bloque `<details>` colapsado.
Esta tabla es un resumen de referencia rápida para el docente.

| N.º | Tipo | Respuesta correcta / síntesis | RA |
|---|---|---|---|
| 1 | Abierta | `A→B` directa; `A→C` transitiva (vía `B`, sin mencionarlo). | RA-1 |
| 2 | Selección múltiple | A, C, D — B es una ventaja, no una desventaja. | RA-2 |
| 3 | Selección múltiple | B, C — Spring falla explícitamente; `@Qualifier` resuelve sin borrar implementaciones. | RA-3, RA-9 |
| 4 | Abierta | Error "No qualifying bean... found 2: ..."; se resuelve con `@Qualifier` en cada implementación y en el punto de inyección. | RA-3, RA-9 |
| 5 | Selección | B — acoplamiento a la implementación concreta, no se puede reemplazar en un test. | RA-4 |
| 6 | Abierta | Constructor: obligatoria, permite `final`; setter: opcional, asignable después, no puede ser `final`. | RA-5, RA-6 |
| 7 | Selección | B — reparto de responsabilidades incorrecto, no un problema de sintaxis. | RA-7 |
| 8 | Abierta | Reorganizar responsabilidades (extraer a un tercer módulo), no cambiar la forma de inyección. | RA-8 |
| 9 | Selección | B — expone propiedades mediante getter/setter. | RA-10 |
| 10 | Selección múltiple | A, C, D — B es falsa, son conceptos relacionados pero distintos. | RA-10 |
| 11 | Selección | A — instanciación → configuración → inicialización → uso → destrucción. | RA-11 |
| 12 | Abierta | `@Component` para una utilidad genérica; `@Service` funcionaría igual técnicamente pero comunicaría mal la responsabilidad. | RA-12 |
