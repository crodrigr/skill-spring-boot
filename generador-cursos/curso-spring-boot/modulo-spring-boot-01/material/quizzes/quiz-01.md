# ❓ Quiz 01 — Fundamentos de Java y Ecosistema Spring

**1. [Selección múltiple]** ¿Cuál de las siguientes es una característica de un
framework?

A. Nunca impone ninguna estructura al proyecto.
B. Ofrece una estructura predefinida (por ejemplo, Inversión de Control o el patrón Modelo-Vista-Controlador) para reducir el código repetitivo y facilitar el desarrollo ágil.
C. Solo sirve para hacer cálculos matemáticos.
D. Debe reescribirse desde cero en cada proyecto nuevo.

_RA: RA-10_

---

**2. [Identificación]** Una librería **dinámica** (*shared library*), a diferencia
de una librería estática, ¿qué hace?

A. Se copia dentro del ejecutable en el momento de compilar.
B. Se carga en tiempo de ejecución, lo que permite compartir memoria entre aplicaciones y actualizarla sin recompilar todo el programa.
C. No puede usarse en proyectos Java.
D. Siempre aumenta el tamaño del ejecutable final.

_RA: RA-11_

---

**3. [Selección múltiple]** ¿Cuál es la diferencia clave entre un framework y una
librería?

A. Las librerías siempre ocupan más espacio en disco que los frameworks.
B. Con una librería, el desarrollador decide cuándo llamarla; con un framework, es el framework quien llama al código del desarrollador (Inversión de Control).
C. Los frameworks no pueden usar ninguna librería en su interior.
D. No hay ninguna diferencia real entre ambos conceptos.

_RA: RA-12_

---

**4. [Selección múltiple]** ¿Cuál es la diferencia principal entre una clase
abstracta como `Usuario` y una interfaz como `Prestable`?

A. No hay ninguna diferencia real; son intercambiables.
B. Una clase abstracta puede compartir estado y comportamiento común entre subclases relacionadas; una interfaz solo declara qué puede hacer un tipo, sin relación de herencia entre las implementaciones.
C. Las interfaces solo se usan en Spring; las clases abstractas solo en Java puro.
D. Una interfaz siempre debe implementarse en una única clase.

_RA: RA-1_

---

**5. [Identificación]** Dada una lista de `Prestable` (`Libro`, `RecursoDigital`) y
un bucle que llama a `recurso.calcularDiasDevolucion()` en cada elemento, ¿qué
principio de POO permite que ese mismo código funcione para ambos tipos sin usar
`instanceof`?

A. Encapsulamiento.
B. Polimorfismo.
C. Sobrecarga de operadores.
D. Composición.

_RA: RA-1_

---

**6. [Selección múltiple]** ¿Qué logra reescribir un bucle `for` que filtra y
transforma una lista usando `stream().filter(...).map(...)`?

A. Ejecuta el código más rápido siempre, sin excepción.
B. Expresa la operación de forma declarativa (qué se quiere obtener), sin una lista mutable ni una variable de control del bucle.
C. Convierte automáticamente el código a Spring Boot.
D. Elimina la necesidad de manejar excepciones.

_RA: RA-2_

---

**7. [Identificación]** Un método devuelve `Optional<Medico>` en vez de
`Medico`. ¿Qué gana el código que lo consume?

A. Nada distinto; `Optional` es solo una forma más larga de escribir lo mismo.
B. El tipo deja constancia de que el valor puede no existir, obligando a manejar ese caso explícitamente (`orElse`, `orElseThrow`) en vez de arriesgar un `NullPointerException`.
C. El método se ejecuta más rápido.
D. El método ya no puede lanzar excepciones.

_RA: RA-3_

---

**8. [Identificación]** ¿Qué obtiene automáticamente una clase declarada como
`record` en Java?

A. Nada distinto de una clase normal; es solo una forma más corta de escribir lo mismo.
B. Constructor, *getters*, `equals`, `hashCode` y `toString` generados automáticamente, con inmutabilidad por diseño.
C. La posibilidad de heredar de varias clases a la vez.
D. Un método `main` generado automáticamente.

_RA: RA-4_

---

**9. [Selección múltiple]** ¿Cuál de estas afirmaciones describe correctamente la
diferencia entre Maven y Gradle?

A. Gradle es una versión más nueva de Maven que hace exactamente lo mismo, sin ninguna diferencia real.
B. Maven describe el proyecto de forma declarativa en XML (`pom.xml`), con un ciclo de vida de fases fijo; Gradle usa un DSL de Groovy/Kotlin (`build.gradle`) más flexible para declarar tareas.
C. Solo Maven puede construir proyectos Spring Boot; Gradle no es compatible con Spring.
D. Gradle no permite declarar dependencias de prueba.

_RA: RA-5_

---

**10. [Selección múltiple]** ¿Qué resuelve principalmente Spring Boot respecto de
Spring "clásico"?

A. Reemplaza los conceptos de Spring (IoC, DI, beans) por otros nuevos e incompatibles.
B. Reduce la configuración manual mediante autoconfiguración, agrupa dependencias compatibles en starters, y evita instalar un servidor aparte gracias al servidor embebido.
C. Elimina la necesidad de escribir cualquier código Java.
D. Solo sirve para aplicaciones que no usan una base de datos.

_RA: RA-6_

---

**11. [Identificación]** En la convención de un proyecto Spring Boot organizado
por capas, ¿qué contiene el paquete `repository`?

A. Las clases que reciben peticiones HTTP.
B. Las clases o interfaces responsables de acceder a los datos.
C. La configuración de `application.properties`.
D. Las pruebas automatizadas del proyecto.

_RA: RA-13_

---

**12. [Identificación]** ¿Qué logra anotar una clase con `@RestController` y un
método con `@GetMapping("/catalogo")`?

A. Nada, hasta que se escriba código adicional para registrar la ruta manualmente.
B. Que Spring Boot invoque ese método automáticamente cuando llegue una petición HTTP `GET` a `/catalogo`, devolviendo el resultado serializado en la respuesta.
C. Que el método se ejecute una sola vez, apenas arranca la aplicación.
D. Que la clase se convierta en una librería reutilizable en otros proyectos.

_RA: RA-14_

---

**13. [Identificación]** ¿Qué es un "bean" en el contexto del contenedor IoC de
Spring?

A. Cualquier variable local declarada dentro de un método.
B. Un objeto cuya creación, configuración y ciclo de vida administra el contenedor (`ApplicationContext`), en vez de instanciarse manualmente con `new` en el código cliente.
C. Un archivo de configuración XML.
D. Una anotación exclusiva de Maven.

_RA: RA-7_

---

**14. [Identificación]** Ordená correctamente las fases del ciclo de vida de un
bean: (I) inicialización (`@PostConstruct`), (II) instanciación, (III)
destrucción (`@PreDestroy`), (IV) inyección de dependencias.

A. II, IV, I, III.
B. I, II, III, IV.
C. IV, II, I, III.
D. II, I, IV, III.

_RA: RA-7_

---

**15. [Selección múltiple]** ¿Por qué se prefiere la Inyección de Dependencias por
constructor frente a la inyección por campo para una dependencia obligatoria?

A. Porque por campo es imposible de escribir en Java.
B. Porque por constructor la dependencia queda explícita, puede declararse `final`, y la clase se puede instanciar en un test con valores de prueba sin necesitar un contenedor Spring.
C. Porque por campo Spring nunca logra inyectar el valor correctamente.
D. Porque por constructor la aplicación arranca más rápido, sin relación con las pruebas.

_RA: RA-8_

---

**16. [Identificación]** Una clase crea sus dependencias así:
`private ClienteEmail clienteEmail = new ClienteEmailSmtp();` dentro de su cuerpo.
¿Qué problema concreto genera esto para escribir un test unitario de esa clase?

A. Ninguno; los tests siempre pueden reemplazar cualquier `new` sin cambiar el código.
B. La clase queda acoplada a `ClienteEmailSmtp`: un test no puede sustituirlo por una versión de prueba sin modificar el código de la clase, porque la dependencia nunca se recibe desde afuera.
C. El código directamente no compila.
D. Spring lo resuelve automáticamente sin necesidad de refactorizar nada.

_RA: RA-9_
