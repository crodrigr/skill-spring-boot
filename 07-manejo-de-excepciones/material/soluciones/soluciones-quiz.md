# 🔑 Soluciones — Quiz 07

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante. Tabla resumen de referencia rápida; el texto completo de cada
> respuesta vive en `quiz-07.md`.

| N.º | Tipo | Respuesta/síntesis | RA |
|---|---|---|---|
| 1 | Selección | B — evento anormal que interrumpe el flujo normal | RA-1 |
| 2 | Selección múltiple | A, B, D (C es falsa: Error no debería capturarse) | RA-1 |
| 3 | Selección | C — 500 genérico sin manejo específico | RA-2 |
| 4 | Selección múltiple | A, B, D (C es falsa: la lógica de negocio va en el Service) | RA-3 |
| 5 | Abierta | No es un bug; es una situación normal del flujo de una API REST | RA-2 |
| 6 | Selección | B — @ResponseStatus asocia la excepción a un código HTTP | RA-4 |
| 7 | Abierta | Falta @ResponseStatus; sin ningún mecanismo, cualquier excepción produce 500 | RA-4 |
| 8 | Selección múltiple | A, C, D (B es falsa: es local al controlador) | RA-5 |
| 9 | Abierta | Lógica duplicada en 3 controladores; @ControllerAdvice la centraliza | RA-5 |
| 10 | Selección | B — el manejador local (más específico) toma precedencia | RA-6 |
| 11 | Selección múltiple | A, B, D (C es engañosa: más útil con varios controladores) | RA-6 |
| 12 | Selección múltiple | A, B, C (D es falsa: pueden combinarse en un proyecto) | RA-7 |
| 13 | Abierta | Válido pero más código; @ResponseStatus resuelve casos simples con menos esfuerzo | RA-7 |
| 14 | Selección | C — 500 sin ningún mecanismo de manejo | RA-2 |
| 15 | Selección múltiple | A, C, D (B es falsa: Taller y Desafío usan controladores distintos) | RA-8 |
| 16 | Abierta | Excepciones + @ResponseStatus por caso, luego un único @ControllerAdvice centralizador | RA-8 |
