# 🔑 Soluciones — Quiz 03

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante. Tabla resumen de referencia rápida; el texto completo de cada
> respuesta vive en `quiz-03.md`.

| N.º | Tipo | Respuesta/síntesis | RA |
|---|---|---|---|
| 1 | Selección | B — abstracción de la base de datos | RA-1 |
| 2 | Abierta | JPQL opera sobre entidades/propiedades, no tablas/columnas | RA-2 |
| 3 | Selección | C — EntityManagerFactory | RA-3 |
| 4 | Selección múltiple | A, C, D (B es falsa: no hay persistence.xml en Spring Boot) | RA-3 |
| 5 | Selección | C — muchos a muchos | RA-4 |
| 6 | Selección múltiple | A, C, D (B es falsa: en 1-N solo el lado "muchos" admite varios) | RA-4 |
| 7 | Selección | B — evita escribir SQL manualmente | RA-5 |
| 8 | Abierta | El código no cambia: programa contra la API de JPA, no contra Hibernate | RA-6 |
| 9 | Selección múltiple | A, B, D (C es falsa: compatible con múltiples BD) | RA-7 |
| 10 | Abierta | Spring Data JPA implementa el método por convención de nombres | RA-7 |
| 11 | Selección múltiple | A, C, D (B es falsa: la implementación en memoria queda obsoleta) | RA-8 |
| 12 | Selección | B — validate verifica sin modificar | RA-9 |
| 13 | Abierta | create-drop borra datos al apagar; usar update para conservarlos | RA-9 |
| 14 | Selección | B — el lado @ManyToOne es el propietario | RA-11 |
| 15 | Selección múltiple | A, C, D (B es falsa: @JoinTable va en el lado propietario) | RA-10, RA-11 |
| 16 | Abierta | Colección LAZY fuera de transacción; corregir con @Transactional | RA-12 |
