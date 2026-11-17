# 🔑 Soluciones — Quiz 04

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante. Tabla resumen de referencia rápida; el texto completo de cada
> respuesta vive en `quiz-04.md`.

| N.º | Tipo | Respuesta/síntesis | RA |
|---|---|---|---|
| 1 | Selección | B — Spring Initializr | RA-10 |
| 2 | Abierta | Dependencias habilitan JPA/H2; application.properties configura la conexión | RA-11 |
| 3 | Selección | B — representación de una relación entre tablas | RA-1 |
| 4 | Selección | B — Paciente declara @JoinColumn | RA-2 |
| 5 | Selección múltiple | A, B, D (C es falsa: genera dos relaciones independientes) | RA-4 |
| 6 | Selección | B — el lado @ManyToOne es el dueño | RA-3 |
| 7 | Abierta | LAZY es el default recomendado; usar @Transactional donde haga falta | RA-6 |
| 8 | Selección múltiple | A, C, D (B es falsa: @ManyToMany simple sigue siendo válida sin atributos propios) | RA-5 |
| 9 | Abierta | Reemplazar por entidad intermedia (Matricula) con notaFinal propio | RA-5 |
| 10 | Selección | C — REMOVE propaga eliminar | RA-7 |
| 11 | Selección múltiple | B, C, D (A es falsa: sin orphanRemoval no se elimina) | RA-8 |
| 12 | Abierta | Falta orphanRemoval=true; cascade no vigila la colección | RA-8 |
| 13 | Selección | B — save crea o actualiza según si la entidad tiene id | RA-9 |
| 14 | Abierta | Implementar solo crear/leer/actualizar; no exponer deleteById | RA-9 |
| 15 | Selección | B — ApplicationContext no terminó de inicializarse | RA-11 |
| 16 | Abierta | Buscar "Caused by:"; ahí está la causa raíz real, no en las excepciones que la envuelven | RA-11 |
| 17 | Selección múltiple | A, B, C (D es falsa: orphanRemoval deja de ser opcional si se pide eliminar) | RA-12 |
| 18 | Abierta | Reutilizar el criterio (cascade/orphanRemoval, repositorio del padre); construir de cero nombres, atributos y caso de negocio | RA-12 |
