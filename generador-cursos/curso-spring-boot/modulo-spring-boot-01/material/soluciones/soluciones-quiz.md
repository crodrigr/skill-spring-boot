# 🔑 Clave del Quiz 01 — Fundamentos de Java y Ecosistema Spring

Material docente. No enlazar desde archivos de audiencia estudiante (salvo la
subsección "Soluciones" de `specs/modulo-spring-boot-01.md`).

| N.º | Respuesta correcta | Explicación breve | RA |
|---|---|---|---|
| 1 | B | Un framework ofrece una estructura predefinida (IoC, MVC, etc.) para reducir código repetitivo y facilitar el desarrollo ágil; no es "libre de estructura" como una librería. | RA-10 |
| 2 | B | Una librería dinámica se carga en tiempo de ejecución; una estática se copia dentro del ejecutable al compilar. | RA-11 |
| 3 | B | La diferencia clave es la Inversión de Control: quién llama a quién. | RA-12 |
| 4 | B | Una clase abstracta comparte estado/comportamiento entre subclases relacionadas por herencia; una interfaz solo fija un contrato de comportamiento, sin exigir relación de herencia entre quienes la implementan. | RA-1 |
| 5 | B | El polimorfismo permite que el mismo código invoque `calcularDiasDevolucion()` y cada tipo concreto ejecute su propia versión, sin `instanceof`. | RA-1 |
| 6 | B | Streams describen la operación de forma declarativa; no implican por sí solos una mejora de rendimiento garantizada. | RA-2 |
| 7 | B | `Optional` obliga a decidir explícitamente qué hacer ante la ausencia de valor, en vez de arriesgar un `NullPointerException`. | RA-3 |
| 8 | B | Un `record` genera automáticamente constructor, *getters*, `equals`, `hashCode` y `toString`, y es inmutable por diseño. | RA-4 |
| 9 | B | Maven es XML declarativo con fases fijas; Gradle es un DSL de Groovy/Kotlin más flexible para tareas. Ambos son compatibles con Spring Boot. | RA-5 |
| 10 | B | Spring Boot no reemplaza los conceptos de Spring: automatiza su configuración (autoconfiguración, starters, servidor embebido). | RA-6 |
| 11 | B | El paquete `repository` contiene las clases o interfaces responsables de acceder a los datos, por convención de la arquitectura por capas. | RA-13 |
| 12 | B | `@RestController` + `@GetMapping` hacen que Spring Boot invoque el método al recibir la petición HTTP correspondiente, devolviendo el resultado serializado. | RA-14 |
| 13 | B | Un bean es un objeto cuyo ciclo de vida administra el contenedor IoC, no el código cliente. | RA-7 |
| 14 | A | Orden correcto: instanciación (II) → inyección de dependencias (IV) → inicialización (I) → destrucción (III). | RA-7 |
| 15 | B | La inyección por constructor hace explícita la dependencia, permite `final`, y facilita instanciar la clase en un test sin contenedor. | RA-8 |
| 16 | B | Instanciar con `new` dentro de la clase acopla la clase a esa implementación concreta e impide sustituirla en un test sin modificar el código. | RA-9 |

## 📏 Criterios de corrección para ítems abiertos

Ningún ítem de este quiz es de respuesta abierta (los 16 son de selección múltiple
o identificación con una única opción correcta); no se requieren criterios de
corrección adicionales más allá de la opción marcada en la tabla.
