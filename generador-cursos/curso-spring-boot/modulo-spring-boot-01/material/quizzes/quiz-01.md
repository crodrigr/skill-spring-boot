# ❓ Quiz 01 — Fundamentos de Java y Ecosistema Spring (formato entrevista técnica)

Este quiz simula las preguntas que podrías recibir en una entrevista técnica para
un puesto de desarrollador Java/Spring Boot junior. Cada pregunta indica su tipo
(**Selección**, **Selección múltiple** o **Abierta**). Respondé primero por tu
cuenta y después abrí "Ver respuesta" para comparar.

---

**1. [Selección]** Un entrevistador te pregunta: "¿Cuál de las siguientes es una
característica de un framework?"

- **A.** Nunca impone ninguna estructura al proyecto.
- **B.** Ofrece una estructura predefinida (por ejemplo, Inversión de Control o el patrón Modelo-Vista-Controlador) para reducir el código repetitivo y facilitar el desarrollo ágil.
- **C.** Solo sirve para hacer cálculos matemáticos.
- **D.** Debe reescribirse desde cero en cada proyecto nuevo.

_RA: RA-10_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**

Un framework ofrece una estructura predefinida (IoC, MVC, minimización de código
repetitivo, gestión integrada de aspectos transversales) para facilitar el
desarrollo ágil. Una librería, en cambio, no impone estructura.

</details>

---

**2. [Selección múltiple]** El entrevistador te muestra dos tipos de librerías y
te pide: "Seleccioná **todas** las afirmaciones correctas sobre las librerías
**dinámicas** (*shared libraries*)."

- **A.** Se cargan en tiempo de ejecución, no en tiempo de compilación.
- **B.** Permiten que varias aplicaciones compartan la misma copia cargada en memoria.
- **C.** Siempre aumentan el tamaño del ejecutable final porque se copian dentro de él.
- **D.** Pueden actualizarse sin necesidad de recompilar la aplicación que las usa.
- **E.** Son exclusivas del lenguaje Java.

_RA: RA-11_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**

C es falsa: copiarse dentro del ejecutable describe a una librería **estática**,
no dinámica. E es falsa: el concepto de librería dinámica existe en muchos
lenguajes y sistemas operativos, no es exclusivo de Java (en Java, un `.jar` en el
classpath se comporta conceptualmente como una librería dinámica).

</details>

---

**3. [Abierta]** "Explicame con tus palabras la diferencia entre un framework y
una librería, y dame un ejemplo de cada uno de este curso."

_RA: RA-12_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: La diferencia clave es quién controla el flujo de
ejecución (Inversión de Control). Con una librería, el desarrollador decide
cuándo llamarla (por ejemplo, un validador de ISBN que se invoca donde se
necesita). Con un framework, es el framework quien llama al código del
desarrollador cuando corresponde (por ejemplo, Spring Boot invoca el método de un
`@RestController` cuando llega una petición HTTP). Un framework también suele
imponer una estructura a todo el proyecto; una librería resuelve una necesidad
puntual sin imponer nada al resto del código.

**Debería mencionar**: (a) el criterio de "quién llama a quién"; (b) que un
framework impone estructura y una librería no; (c) un ejemplo concreto de cada
uno (Spring Boot como framework, una librería de validación/formateo como
librería).

</details>

---

**4. [Selección]** "Tengo una clase abstracta `Usuario` y una interfaz
`Prestable`. ¿Cuál es la diferencia principal entre ambas?"

- **A.** No hay ninguna diferencia real; son intercambiables.
- **B.** Una clase abstracta puede compartir estado y comportamiento común entre subclases relacionadas; una interfaz solo declara qué puede hacer un tipo, sin relación de herencia entre las implementaciones.
- **C.** Las interfaces solo se usan en Spring; las clases abstractas solo en Java puro.
- **D.** Una interfaz siempre debe implementarse en una única clase.

_RA: RA-1_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**

Una clase abstracta comparte estado/comportamiento entre subclases relacionadas
por herencia; una interfaz solo fija un contrato de comportamiento, sin exigir
relación de herencia entre quienes la implementan.

</details>

---

**5. [Abierta]** "Tenés una lista de objetos `Prestable` que en realidad son
`Libro` y `RecursoDigital`. Te piden escribir un método que sume los días de
devolución de todos, **sin usar `instanceof` ni *casts***. ¿Cómo lo resolverías y
qué principio de POO estás aplicando?"

_RA: RA-1_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Se recorre la lista de `Prestable` e invoca directamente
`recurso.calcularDiasDevolucion()` sobre cada elemento; en tiempo de ejecución,
cada objeto ejecuta su propia implementación (la de `Libro` o la de
`RecursoDigital`) sin que el código que recorre la lista necesite saber de qué
tipo concreto es cada uno. El principio aplicado es **polimorfismo**.

**Debería mencionar**: (a) que se itera sobre el tipo `Prestable`, no sobre los
tipos concretos; (b) que no hace falta `instanceof` porque cada tipo resuelve su
propia versión del método; (c) el nombre correcto del principio (polimorfismo).

</details>

---

**6. [Selección múltiple]** "¿Cuáles de las siguientes son ventajas reales de
usar streams y lambdas en vez de un bucle `for` tradicional? Seleccioná **todas**
las que apliquen."

- **A.** Expresan la operación de forma declarativa (qué se quiere obtener, no cómo iterarlo).
- **B.** Evitan declarar una lista mutable intermedia para acumular resultados.
- **C.** Garantizan que el código se ejecute en paralelo automáticamente y siempre más rápido.
- **D.** Suelen ser más cortos y legibles para encadenar filtros y transformaciones.
- **E.** Eliminan por completo la necesidad de manejar cualquier tipo de excepción.

_RA: RA-2_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**

C es falsa: un stream secuencial (`.stream()`) no se ejecuta en paralelo; eso
requeriría `.parallelStream()`, y ni así se garantiza que sea siempre más rápido.
E es falsa: los streams no eliminan el manejo de excepciones (las lambdas que
lanzan excepciones *checked* incluso complican un poco su manejo).

</details>

---

**7. [Selección]** "¿Por qué un método debería devolver `Optional<Medico>` en vez
de `Medico`?"

- **A.** No cambia nada; `Optional` es solo una forma más larga de escribir lo mismo.
- **B.** El tipo deja constancia de que el valor puede no existir, obligando a manejar ese caso explícitamente (`orElse`, `orElseThrow`) en vez de arriesgar un `NullPointerException`.
- **C.** El método se ejecuta más rápido.
- **D.** El método ya no puede lanzar excepciones.

_RA: RA-3_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**

`Optional` hace explícita, a nivel de tipo, la posibilidad de ausencia de valor,
forzando a decidir qué hacer en ese caso en vez de arriesgar un
`NullPointerException` más adelante.

</details>

---

**8. [Abierta]** "¿Qué es un `record` en Java y en qué situación lo usarías en vez
de una clase tradicional?"

_RA: RA-4_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Un `record` es una forma compacta de declarar una clase
inmutable que solo transporta datos: el compilador genera automáticamente el
constructor, los *getters*, `equals`, `hashCode` y `toString`. Conviene usarlo
para modelar objetos de valor simples (por ejemplo, `DatosContacto` o `Cita`)
donde no se necesita mutabilidad ni lógica de negocio compleja, evitando escribir
a mano ese código repetitivo.

**Debería mencionar**: (a) que genera constructor/getters/equals/hashCode/toString
automáticamente; (b) que es inmutable por diseño; (c) un caso de uso apropiado
(objeto de valor simple, no una entidad con comportamiento complejo).

</details>

---

**9. [Selección múltiple]** "¿Cuáles de las siguientes afirmaciones sobre Maven y
Gradle son correctas? Seleccioná **todas** las que apliquen."

- **A.** Maven usa un DSL de Groovy/Kotlin; Gradle usa XML declarativo.
- **B.** Maven tiene un ciclo de vida de fases fijo (`validate`, `compile`, `test`, `package`, …).
- **C.** Gradle permite tareas configurables y aprovecha caché incremental para builds más rápidos.
- **D.** Ambos pueden gestionar dependencias y construir un proyecto Spring Boot.
- **E.** Una dependencia con `scope test` en Maven equivale a `testImplementation` en Gradle.

_RA: RA-5_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: B, C, D, E**

A está invertida: es al revés (Maven = XML declarativo, Gradle = DSL de
Groovy/Kotlin).

</details>

---

**10. [Abierta]** "Contame qué es Spring Boot, de dónde viene, y qué problema
resuelve respecto a Spring 'clásico'. Mencioná al menos tres características."

_RA: RA-6_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Spring Boot es un framework construido sobre Spring
(nacido en 2003 para simplificar el desarrollo empresarial en Java) que en 2014
resolvió el problema de que la propia configuración de Spring se había vuelto
extensa con el tiempo. Prioriza "convención sobre configuración" mediante:
**configuración automática** (autoconfigura según las dependencias presentes),
**servidor embebido** (Tomcat/Jetty/Undertow sin instalar nada aparte), **inicio
rápido** (una anotación y un `main` alcanzan), aptitud para **microservicios**,
**starters** (dependencias agrupadas y compatibles) y **actuadores**
(monitorización lista para usar).

**Debería mencionar**: (a) que no reemplaza a Spring, lo empaqueta; (b) al menos
tres de las seis características; (c) idealmente, una mención a la relación
temporal Spring (2003) → Spring Boot (2014).

</details>

---

**11. [Selección]** "En un proyecto Spring Boot organizado por capas, ¿qué
contiene el paquete `repository`?"

- **A.** Las clases que reciben peticiones HTTP.
- **B.** Las clases o interfaces responsables de acceder a los datos.
- **C.** La configuración de `application.properties`.
- **D.** Las pruebas automatizadas del proyecto.

_RA: RA-13_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**

`repository` agrupa las clases o interfaces responsables de acceder a los datos,
por convención de la arquitectura por capas (`controller` → `service` →
`repository` → `model`).

</details>

---

**12. [Selección múltiple]** "Te muestro esta clase:

```java
@Service
public class ServicioPrestamos {
    private final RepositorioLibros repositorioLibros;

    public ServicioPrestamos(RepositorioLibros repositorioLibros) {
        this.repositorioLibros = repositorioLibros;
    }
}
```

No tiene ningún `@Autowired`. Seleccioná **todas** las afirmaciones correctas."

- **A.** Spring no podrá inyectar `RepositorioLibros` porque falta `@Autowired` explícito, y la aplicación fallará al arrancar.
- **B.** Spring usará automáticamente este único constructor para inyectar `RepositorioLibros`, sin necesitar `@Autowired`.
- **C.** `@Service` es una especialización de `@Component` que comunica que la clase contiene lógica de negocio.
- **D.** Si se agregara `@Autowired` sobre el constructor, seguiría funcionando igual: sería redundante, pero no incorrecto.

_RA: RA-14_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: B, C, D**

A es falsa: desde Spring 4.3, si una clase tiene un único constructor, Spring lo
usa automáticamente para inyectar sus dependencias, sin necesitar `@Autowired`
explícito.

</details>

---

**13. [Abierta]** "¿Qué es el contenedor IoC de Spring (`ApplicationContext`) y
qué es un *bean*?"

_RA: RA-7_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: El `ApplicationContext` es el contenedor de Inversión de
Control de Spring: al arrancar, escanea las clases anotadas y decide qué objetos
crear, configurar y administrar. Un *bean* es exactamente ese objeto: uno cuya
creación, inyección de dependencias y ciclo de vida delega en el contenedor, en
vez de que el código cliente lo cree con `new`.

**Debería mencionar**: (a) que el `ApplicationContext` escanea y administra
objetos; (b) que un bean es un objeto administrado por el contenedor, no
instanciado a mano; (c) idealmente, que esto es la base de la Inyección de
Dependencias.

</details>

---

**14. [Selección]** "Ordená correctamente las fases del ciclo de vida de un bean:
(I) inicialización (`@PostConstruct`), (II) instanciación, (III) destrucción
(`@PreDestroy`), (IV) inyección de dependencias."

- **A.** II, IV, I, III.
- **B.** I, II, III, IV.
- **C.** IV, II, I, III.
- **D.** II, I, IV, III.

_RA: RA-7_

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: A**

Orden correcto: instanciación (II) → inyección de dependencias (IV) →
inicialización (I) → destrucción (III).

</details>

---

**15. [Abierta]** "Un compañero te muestra este código y te pregunta si está
bien:

```java
@Service
public class ServicioPrestamos {
    @Autowired
    private RepositorioLibros repositorioLibros;
}
```

¿Qué le dirías?"

_RA: RA-8_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Funciona, pero no es la forma recomendada. La inyección por
campo oculta la dependencia real de la clase (no aparece en ningún constructor ni
setter) y dificulta instanciar `ServicioPrestamos` en un test unitario sin un
contenedor Spring (o sin recurrir a reflexión/frameworks de mocks). Le
sugeriría cambiarlo a inyección por constructor: declarar `repositorioLibros`
como `private final` y recibirlo como parámetro del constructor, lo que además
permite escribir `new ServicioPrestamos(repositorioDePrueba)` directamente en un
test.

**Debería mencionar**: (a) que la inyección por campo "funciona" pero no es la
preferida; (b) el motivo (oculta dependencias, dificulta testear sin contenedor);
(c) la alternativa recomendada (constructor).

</details>

---

**16. [Abierta]** "En una revisión de código encontrás esta línea dentro de una
clase: `private ClienteEmail clienteEmail = new ClienteEmailSmtp();`. ¿Qué
problema le ves y cómo lo resolverías?"

_RA: RA-9_

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: La clase crea su propia dependencia con `new`, quedando
acoplada a la implementación concreta `ClienteEmailSmtp`. Esto impide
sustituirla por una versión de prueba (por ejemplo, un `ClienteEmail` falso que
solo registra el mensaje) sin modificar el código de la clase, lo que complica
escribir un test unitario aislado. La solución es refactorizar la clase para que
reciba `ClienteEmail` por constructor (`private final ClienteEmail clienteEmail`,
asignado en el constructor), de modo que se pueda instanciar con
`new MiClase(clienteEmailDePrueba)` en un test.

**Debería mencionar**: (a) el acoplamiento a la implementación concreta; (b) la
consecuencia concreta para las pruebas; (c) la solución (inyección por
constructor).

</details>
