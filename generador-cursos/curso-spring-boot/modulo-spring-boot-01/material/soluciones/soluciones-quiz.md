# 🔑 Clave del Quiz 01 — Fundamentos de Java y Ecosistema Spring

Material docente. No enlazar desde archivos de audiencia estudiante (salvo la
subsección "Soluciones" de `specs/modulo-spring-boot-01.md`).

Desde esta iteración, `quizzes/quiz-01.md` (formato entrevista técnica) ya incluye
la respuesta de cada ítem oculta en un bloque `<details>` colapsado, pensada como
autoevaluación inmediata para el estudiante. Esta tabla es un resumen de
referencia rápida para el docente (por ejemplo, para corregir en grupo o
verificar respuestas sin abrir cada bloque uno por uno).

| N.º | Tipo | Respuesta correcta / síntesis | RA |
|---|---|---|---|
| 1 | Selección | B — un framework impone estructura (IoC, MVC) para reducir código repetitivo. | RA-10 |
| 2 | Selección múltiple | A, B, D — una dinámica se carga en ejecución, comparte memoria y se actualiza sin recompilar. | RA-11 |
| 3 | Abierta | Diferencia = quién controla el flujo (Inversión de Control); framework impone estructura, librería no. | RA-12 |
| 4 | Selección | B — clase abstracta comparte estado/comportamiento; interfaz solo fija un contrato. | RA-1 |
| 5 | Abierta | Polimorfismo: invocar el método sobre el tipo `Prestable`, cada implementación resuelve su propia versión. | RA-1 |
| 6 | Selección múltiple | A, B, D — streams son declarativos, evitan listas mutables intermedias y son más legibles; NO paralelizan solos ni eliminan excepciones. | RA-2 |
| 7 | Selección | B — `Optional` obliga a manejar explícitamente la ausencia de valor. | RA-3 |
| 8 | Abierta | `record`: constructor/getters/equals/hashCode/toString generados, inmutable; para objetos de valor simples. | RA-4 |
| 9 | Selección múltiple | B, C, D, E — (A está invertida: Maven es XML, Gradle es DSL). | RA-5 |
| 10 | Abierta | Spring Boot (2014) simplifica la configuración que Spring (2003) había acumulado; mencionar ≥3 de sus 6 características. | RA-6 |
| 11 | Selección | B — `repository` accede a los datos. | RA-13 |
| 12 | Selección múltiple | B, C, D — Spring usa el único constructor automáticamente desde 4.3; `@Service` especializa `@Component`; agregar `@Autowired` sería redundante pero válido. | RA-14 |
| 13 | Abierta | `ApplicationContext` = contenedor IoC que escanea y administra beans; bean = objeto administrado por el contenedor, no creado con `new`. | RA-7 |
| 14 | Selección | A — instanciación → inyección de dependencias → inicialización → destrucción. | RA-7 |
| 15 | Abierta | Inyección por campo funciona pero oculta dependencias y dificulta testear sin contenedor; recomendar constructor. | RA-8 |
| 16 | Abierta | `new` dentro de la clase acopla a la implementación concreta e impide sustituirla en un test; solución: inyección por constructor. | RA-9 |

## 📏 Criterios de corrección para ítems abiertos

Para los ítems marcados **[Abierta]**, la respuesta del estudiante no necesita
coincidir palabra por palabra con la "respuesta modelo" del quiz: se considera
correcta si cubre los puntos listados en "Debería mencionar" de cada ítem en
`quiz-01.md`. Para los ítems de **Selección** y **Selección múltiple**, se
considera correcta únicamente la combinación exacta de opciones indicada arriba.
