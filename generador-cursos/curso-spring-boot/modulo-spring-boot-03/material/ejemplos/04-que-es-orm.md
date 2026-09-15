# 💡 Ejemplo 04 — ¿Qué es ORM?

## 🌍 Contexto

En el Ejemplo 01 ya guardaste un `Paciente` con `entityManager.persist(...)`
sin escribir una sola línea de SQL. Eso fue posible gracias al **Mapeo
Objeto-Relacional** (ORM, *Object-Relational Mapping*): la técnica que
convierte objetos de una aplicación en registros de una base de datos
relacional, y viceversa, sin que el desarrollador escriba las sentencias SQL
a mano.

**Qué busca demostrar este ejemplo**: el contraste concreto entre lo que
*tendrías* que escribir sin ORM, y lo que *ya escribiste* en el Ejemplo 01
con JPA. No hay código nuevo para ejecutar en este bloque; el objetivo es
que el contraste quede explícito antes de conocer, en el Ejemplo 05, qué
herramienta (Hibernate) hace ese trabajo por vos.

## 🏥 Caso de estudio

MediSalud: guardar un `Paciente`, con y sin ORM.

## 🧭 Explicación paso a paso

1. **Sin ORM**, guardar un `Paciente` exige código como este (ilustrativo,
   no forma parte del proyecto del curso):

   ```java
   String sql = "INSERT INTO paciente (codigo, nombre) VALUES (?, ?)";
   try (PreparedStatement ps = conexion.prepareStatement(sql)) {
       ps.setString(1, "P-001");
       ps.setString(2, "Ana Gómez");
       ps.executeUpdate();
   }
   ```

   Acá el desarrollador maneja el SQL, los tipos de columna, la conexión y
   los errores de bajo nivel a mano.

2. **Con ORM** (lo que ya hiciste en el Ejemplo 01):

   ```java
   entityManager.persist(new Paciente("P-001", "Ana Gómez"));
   ```

   El ORM traduce ese `persist(...)` al `INSERT` equivalente, incluyendo los
   nombres de columna (`codigo`, `nombre`) que dedujo de la clase `Paciente`
   y sus anotaciones.

3. El mismo contraste aplica para actualizar, eliminar y consultar: cada
   operación de objeto (`merge`, `remove`, una consulta JPQL) se traduce a
   la sentencia SQL equivalente, sin que el desarrollador la escriba.
4. ORM no es exclusivo de Java: es una técnica general: el objeto es lo que
   el desarrollador maneja; la fila de la tabla es un detalle de
   implementación que el ORM oculta.

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué problema resuelve principalmente el Mapeo
Objeto-Relacional (ORM)?

- **A.** Elimina la necesidad de tener una base de datos.
- **B.** Evita que el desarrollador escriba SQL manualmente para convertir objetos en filas y viceversa.
- **C.** Hace que el programa compile más rápido.
- **D.** Reemplaza al lenguaje Java por SQL.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Esa es la definición de ORM: evitar el mapeo
manual entre objetos y filas de tabla.

</details>

**2. [Selección múltiple]** Sobre el contraste de este ejemplo, seleccioná
**todas** las afirmaciones correctas.

- **A.** Sin ORM, el desarrollador escribe el SQL y maneja la conexión a bajo nivel.
- **B.** Con ORM, `entityManager.persist(...)` se traduce internamente a un `INSERT` equivalente.
- **C.** ORM solo funciona para la operación de guardar, no para consultar ni actualizar.
- **D.** El ORM deduce los nombres de columna a partir de la clase y sus anotaciones.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: ORM cubre todas las
operaciones CRUD, no solo guardar.

</details>

**3. [Abierta]** Un compañero te dice: "si igual hay una base de datos SQL
detrás, ¿para qué sirve el ORM?".

**Pregunta**: ¿Qué le responderías?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Es cierto que sigue habiendo SQL ejecutándose contra
la base de datos, pero el ORM se encarga de generarlo por vos, a partir de
las operaciones que hacés sobre objetos (`persist`, `merge`, una consulta
JPQL). Esto evita escribir y mantener SQL manualmente para cada operación,
reduce errores de tipeo o de tipos de dato, y permite razonar sobre el
dominio en términos de objetos (`Paciente`, `Cita`) en vez de en términos de
tablas y columnas.

</details>
