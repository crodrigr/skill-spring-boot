# 💡 Ejemplo 06 — ¿Qué es Spring Data JPA?

## 🌍 Contexto

En los Ejemplos 01-05 usaste `EntityManager` directamente: funciona, pero
cada búsqueda nueva exige escribir su propia consulta JPQL a mano dentro de
un `@Service`. **Spring Data JPA** es el módulo de Spring que evita ese
código repetitivo: en vez de implementar los métodos de acceso a datos, se
declara una **interfaz** de repositorio, y Spring genera la implementación
en tiempo de ejecución.

Ventajas concretas de Spring Data JPA:

- **Menos código repetitivo**: no hay que escribir la implementación de cada
  operación CRUD.
- **Creación automática de consultas**: un método como `findByCodigo(String
  codigo)` genera su consulta a partir del **nombre del método**, sin
  escribir JPQL.
- **Paginación y ordenamiento integrados**: soporte nativo para manejar
  grandes volúmenes de datos.
- **Soporte para consultas personalizadas**: cuando la convención de
  nombres no alcanza, se puede escribir JPQL explícito con `@Query`.
- **Compatibilidad con múltiples bases de datos**: el mismo código de
  repositorio funciona igual con H2, MySQL, PostgreSQL, etc.

**Qué busca demostrar este ejemplo**: contrastar, sobre el mismo
`Paciente`, la búsqueda manual con `EntityManager`/JPQL (Ejemplo 01) contra
el mismo resultado declarando solo una **interfaz**, sin implementarla. El
repositorio completo, con su código y ejecución real, llega en el Ejemplo
07.

## 🏥 Caso de estudio

MediSalud: buscar un `Paciente` por código, con y sin Spring Data JPA.

## 🧭 Explicación paso a paso

1. **Sin Spring Data JPA** (Ejemplo 01), buscar un paciente por código
   exige escribir la consulta JPQL a mano dentro de un método:

   ```java
   entityManager
       .createQuery("SELECT p FROM Paciente p WHERE p.codigo = :codigo", Paciente.class)
       .setParameter("codigo", codigo)
       .getResultList();
   ```

2. **Con Spring Data JPA**, alcanza con declarar una interfaz, sin cuerpo:

   ```java
   public interface RepositorioPacientes extends JpaRepository<Paciente, Long> {
       Optional<Paciente> findByCodigo(String codigo);
   }
   ```

   Spring Data JPA lee el nombre del método (`findBy` + `Codigo`) y genera,
   en tiempo de ejecución, exactamente la misma consulta JPQL que escribiste
   a mano en el paso 1 — sin que el desarrollador la escriba.
3. Además, por extender `JpaRepository<Paciente, Long>`, el repositorio ya
   trae gratis operaciones CRUD completas (`save`, `findById`, `findAll`,
   `deleteById`, …) que en el Ejemplo 01 había que invocar una por una sobre
   el `EntityManager`.
4. Este ejemplo no se ejecuta todavía (no hay `Main`, ni `application.
   properties`): es un contraste de código para entender el "antes/después".
   La conversión real de `RepositorioPacientes` en un repositorio de Spring
   Data JPA, ejecutándose contra H2, es el contenido del Ejemplo 07.

## ❓ Preguntas de repaso

**1. [Selección múltiple]** Seleccioná **todas** las ventajas de Spring Data
JPA mencionadas en este ejemplo.

- **A.** Menos código repetitivo.
- **B.** Creación automática de consultas por convención de nombres.
- **C.** Obliga a usar siempre MySQL como base de datos.
- **D.** Soporte para consultas personalizadas con `@Query`.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: Spring Data JPA es
compatible con múltiples bases de datos, no obliga a ninguna en particular.

</details>

**2. [Abierta]** En este escenario:

- `RepositorioPacientes` es una interfaz que extiende `JpaRepository<Paciente, Long>`.
- Declara `Optional<Paciente> findByCodigo(String codigo)`, sin cuerpo ni implementación.

**Pregunta**: ¿Quién genera el código que ejecuta esa consulta, y a partir de
qué información?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Spring Data JPA genera la implementación en tiempo de
ejecución, a partir del **nombre del método**: interpreta `findBy` como "una
consulta que busca por" y `Codigo` como el nombre de la propiedad de la
entidad (`Paciente.codigo`), y construye automáticamente la consulta JPQL
equivalente a `SELECT p FROM Paciente p WHERE p.codigo = :codigo`, sin que
el desarrollador la escriba.

</details>
