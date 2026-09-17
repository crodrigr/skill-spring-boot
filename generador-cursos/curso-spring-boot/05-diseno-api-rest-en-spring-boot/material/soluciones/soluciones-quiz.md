# 🔑 Soluciones — Quiz 05

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante. Tabla resumen de referencia rápida; el texto completo de cada
> respuesta vive en `quiz-05.md`.

| N.º | Tipo | Respuesta/síntesis | RA |
|---|---|---|---|
| 1 | Selección | B — conjunto de reglas para comunicar dos componentes | RA-1 |
| 2 | Selección | A — GET es de solo lectura | RA-2 |
| 3 | Selección múltiple | A, C, D (B es falsa: 4xx es error del cliente) | RA-3 |
| 4 | Abierta | No respeta interfaz uniforme (ni basada en recursos); rompe la inferencia por verbo HTTP | RA-4 |
| 5 | Selección | B — Service encapsula la lógica de negocio | RA-5 |
| 6 | Selección múltiple | A, C (B y D son falsas) | RA-5 |
| 7 | Selección | B — deja al Controller decidir el código de estado | RA-6 |
| 8 | Abierta | Inyección por constructor: inmutable, testeable, explícita | RA-6 |
| 9 | Selección | C — @RequestBody convierte JSON en objeto Java | RA-7 |
| 10 | Selección múltiple | A, C, D (B es falsa: debe ser 404) | RA-7 |
| 11 | Abierta | Falta verificar existencia antes de eliminar; devolver 404 si no existía | RA-8 |
| 12 | Selección | B — confirma que el recurso desapareció realmente | RA-9 |
| 13 | Abierta | Faltan casos de error; ahí suelen esconderse los bugs reales | RA-9 |
| 14 | Selección múltiple | A, C, D (B es falsa: no hace falta eliminar la relación) | RA-10 |
| 15 | Abierta | @JsonIgnore en Paciente.citas deja /citas mostrar su paciente | RA-10 |
| 16 | Selección múltiple | A, B, D (C es falsa: probar solo el camino feliz no alcanza) | RA-11 |
| 17 | Abierta | No hace falta; sin relación inversa no hay ciclo que cortar | RA-11 |
