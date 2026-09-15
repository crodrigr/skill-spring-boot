# ❓ Quiz 02 — Manejo de Dependencias y Java Beans (formato entrevista técnica)

Este quiz simula las preguntas que podrías recibir en una entrevista técnica para
un puesto de desarrollador Java/Spring Boot. Cada pregunta indica su tipo
(**Selección**, **Selección múltiple** o **Abierta**). Respondé primero por tu
cuenta y después abrí "Ver respuesta" para comparar.

---

**1. [Abierta]** En una entrevista técnica te plantean esta situación:

- Tenés tres clases: `A`, `B` y `C`.
- `A` usa un método de `B`.
- `B` usa un método de `C`.
- `A` **nunca** menciona a `C` en su código.

**Pregunta**: ¿Qué tipo de dependencia tiene `A` respecto de `B`? ¿Y respecto
de `C`?

_RA: RA-1_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: `A` depende **directamente** de `B` (lo usa
explícitamente). `A` depende **transitivamente** de `C`: nunca lo menciona,
pero si `C` fallara, `A` se vería afectado porque `B` (del que sí depende
directamente) lo necesita.

**Debería mencionar**: (a) el nombre correcto de cada tipo de dependencia; (b)
que la transitiva no aparece en el código de `A`, solo se infiere siguiendo la
cadena.

</details>

---

**2. [Selección múltiple]** "En una entrevista te preguntan: ¿cuáles de las
siguientes son desventajas reales de usar dependencias? Seleccioná **todas**
las que apliquen."

- **A.** Acoplamiento entre módulos.
- **B.** Mayor reutilización de código.
- **C.** Complejidad adicional para entender el sistema completo.
- **D.** Riesgo de depender de un proveedor externo que deje de mantener su producto.

_RA: RA-2_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es una **ventaja**, no una desventaja.

</details>

---

**3. [Selección múltiple]** "Tenés dos clases anotadas `@Component` que
implementan la misma interfaz `Notificador`, y una tercera clase que la recibe
por constructor sin ninguna anotación adicional. Seleccioná **todas** las
afirmaciones correctas."

- **A.** La aplicación arranca sin problemas; Spring inyecta la primera que encuentra.
- **B.** La aplicación falla al arrancar con un error de "no qualifying bean".
- **C.** Agregar `@Qualifier` en ambas implementaciones y en el punto de inyección resuelve el problema.
- **D.** La única forma de resolverlo es borrar una de las dos implementaciones.

_RA: RA-3, RA-9_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: B, C**. La A es falsa: Spring no elige "la primera que
encuentra", falla explícitamente. La D es falsa: `@Qualifier` resuelve la
ambigüedad sin necesidad de eliminar ninguna implementación.

</details>

**4. [Abierta]** "Contame qué error concreto verías al arrancar una aplicación
Spring si tenés dos `@Component` implementando la misma interfaz y ningún
`@Qualifier`, y cómo lo solucionarías."

_RA: RA-3, RA-9_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Un error del tipo "No qualifying bean of type
'<Interfaz>' available: expected single matching bean but found 2: ...",
listando los nombres de los dos beans candidatos. Se soluciona anotando cada
implementación con un `@Qualifier` propio (por ejemplo `@Qualifier("sms")` y
`@Qualifier("email")`) y anotando el parámetro del constructor donde se
inyecta con el `@Qualifier` correspondiente al bean que se necesita en ese
punto.

</details>

---

**5. [Selección]** Una clase crea su propia dependencia con
`private final RepositorioLibros r = new RepositorioLibrosEnMemoria();`. ¿Cuál
es el problema principal?

- **A.** No compila.
- **B.** Queda acoplada a esa implementación concreta y no se puede reemplazar en un test sin modificar la clase.
- **C.** Consume más memoria que si recibiera la dependencia por constructor.
- **D.** Java prohíbe declarar dependencias como `final`.

_RA: RA-4_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. El acoplamiento a la implementación concreta es el
problema; no hay relación con compilación, memoria ni con la palabra clave
`final`.

</details>

**6. [Abierta]** "En una entrevista te piden explicar cuál es la diferencia
práctica entre recibir una dependencia por constructor y recibirla por
*setter*. ¿Qué le responderías?"

_RA: RA-5, RA-6_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Por constructor, la dependencia es obligatoria: el
objeto no puede existir sin ella, y el campo puede declararse `final`. Por
*setter*, la dependencia se asigna después de construir el objeto, lo que
permite que exista momentáneamente sin ella (útil para dependencias
realmente opcionales), pero el campo no puede ser `final` y el objeto podría
usarse antes de que la dependencia esté asignada si no se tiene cuidado. En
ambos casos se gana flexibilidad y facilidad para pruebas frente a crear la
dependencia con `new` dentro de la clase.

</details>

---

**7. [Selección]** ¿Cuál es la causa típica de una dependencia circular entre
dos clases?

- **A.** Una de las dos clases tiene demasiados métodos públicos.
- **B.** Las responsabilidades entre ambas clases están mal repartidas: cada una necesita algo que en realidad pertenece a la otra.
- **C.** Ambas clases implementan la misma interfaz.
- **D.** Una de las dos clases no tiene ningún constructor.

_RA: RA-7_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Una dependencia circular casi siempre señala un
reparto de responsabilidades incorrecto, no un problema de sintaxis.

</details>

**8. [Abierta]** "En una revisión de código encontrás dos clases que se
inyectan mutuamente por constructor y ni siquiera compilan. ¿Qué le dirías al
equipo sobre cómo solucionarlo?"

_RA: RA-8_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Le diría que el problema no se resuelve cambiando la
forma de inyección (por ejemplo, a *setter*), porque la dependencia mutua
seguiría existiendo; hay que reorganizar las responsabilidades entre ambas
clases —por ejemplo, extrayendo a un tercer módulo la información que ambas
necesitan— para que la relación quede en un solo sentido, aplicando buenas
prácticas como depender de abstracciones y minimizar las dependencias
transitivas expuestas.

</details>

---

**9. [Selección]** ¿Cuál de estas es una característica de un Java Bean?

- **A.** Debe estar anotado obligatoriamente con `@Component`.
- **B.** Expone sus propiedades mediante métodos *getter*/*setter*.
- **C.** No puede tener propiedades de solo lectura.
- **D.** Solo puede existir dentro de un `ApplicationContext`.

_RA: RA-10_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Ninguna de las otras tres es una característica
real de un Java Bean.

</details>

**10. [Selección múltiple]** Seleccioná **todas** las afirmaciones correctas
sobre un Java Bean.

- **A.** Puede ser serializable.
- **B.** Es siempre lo mismo que "un bean administrado por Spring".
- **C.** Puede ser examinado por introspección desde herramientas externas.
- **D.** Encapsula datos y comportamiento mediante propiedades.

_RA: RA-10_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: son conceptos relacionados
pero distintos.

</details>

---

**11. [Selección]** Ordená correctamente las fases del ciclo de vida de un
bean: (I) inicialización, (II) instanciación, (III) configuración, (IV) uso,
(V) destrucción.

- **A.** II, III, I, IV, V.
- **B.** II, I, III, IV, V.
- **C.** III, II, I, IV, V.
- **D.** II, III, IV, I, V.

_RA: RA-11_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: A**. Orden correcto: instanciación (II) → configuración
(III) → inicialización (I) → uso (IV) → destrucción (V).

</details>

**12. [Abierta]** "En una entrevista te muestran una clase sin ninguna
anotación de Spring y te preguntan: ¿qué anotarías para que el contenedor la
registre como bean, sin que encaje en ninguna capa de negocio, datos o web
específica? ¿Y qué pasaría si igual le agregás `@Service` en vez de esa
anotación?"

_RA: RA-12_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: La anotaría con `@Component`, que es la anotación base
para registrar cualquier bean sin comunicar una capa específica. Si en cambio
le agregara `@Service`, el contenedor la registraría exactamente igual (el
mecanismo técnico es el mismo), pero comunicaría, de forma incorrecta, que la
clase contiene lógica de negocio, cuando en realidad es una utilidad genérica
— una decisión semántica desprolija, aunque no rompa nada en tiempo de
ejecución.

</details>
