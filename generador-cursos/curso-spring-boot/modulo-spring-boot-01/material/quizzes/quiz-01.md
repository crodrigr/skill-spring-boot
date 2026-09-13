# ❓ Quiz 01 — Fundamentos de Java y Ecosistema Spring

**1. [Selección múltiple]** ¿Cuál es la diferencia principal entre una clase
abstracta como `Usuario` y una interfaz como `Prestable`?

A. No hay ninguna diferencia real; son intercambiables.
B. Una clase abstracta puede compartir estado y comportamiento común entre subclases relacionadas; una interfaz solo declara qué puede hacer un tipo, sin relación de herencia entre las implementaciones.
C. Las interfaces solo se usan en Spring; las clases abstractas solo en Java puro.
D. Una interfaz siempre debe implementarse en una única clase.

_RA: RA-1_

---

**2. [Identificación]** Dada una lista de `Prestable` (`Libro`, `RecursoDigital`) y
un bucle que llama a `recurso.calcularDiasDevolucion()` en cada elemento, ¿qué
principio de POO permite que ese mismo código funcione para ambos tipos sin usar
`instanceof`?

A. Encapsulamiento.
B. Polimorfismo.
C. Sobrecarga de operadores.
D. Composición.

_RA: RA-1_

---

**3. [Selección múltiple]** ¿Qué logra reescribir un bucle `for` que filtra y
transforma una lista usando `stream().filter(...).map(...)`?

A. Ejecuta el código más rápido siempre, sin excepción.
B. Expresa la operación de forma declarativa (qué se quiere obtener), sin una lista mutable ni una variable de control del bucle.
C. Convierte automáticamente el código a Spring Boot.
D. Elimina la necesidad de manejar excepciones.

_RA: RA-2_

---

**4. [Identificación]** Un método devuelve `Optional<Medico>` en vez de
`Medico`. ¿Qué gana el código que lo consume?

A. Nada distinto; `Optional` es solo una forma más larga de escribir lo mismo.
B. El tipo deja constancia de que el valor puede no existir, obligando a manejar ese caso explícitamente (`orElse`, `orElseThrow`) en vez de arriesgar un `NullPointerException`.
C. El método se ejecuta más rápido.
D. El método ya no puede lanzar excepciones.

_RA: RA-3_

---

**5. [Selección múltiple]** ¿Cuál de estas afirmaciones describe correctamente la
diferencia entre Maven y Gradle?

A. Gradle es una versión más nueva de Maven que hace exactamente lo mismo, sin ninguna diferencia real.
B. Maven describe el proyecto de forma declarativa en XML (`pom.xml`), con un ciclo de vida de fases fijo; Gradle usa un DSL de Groovy/Kotlin (`build.gradle`) más flexible para declarar tareas.
C. Solo Maven puede construir proyectos Spring Boot; Gradle no es compatible con Spring.
D. Gradle no permite declarar dependencias de prueba.

_RA: RA-5_

---

**6. [Selección múltiple]** ¿Qué resuelve principalmente Spring Boot respecto de
Spring "clásico"?

A. Reemplaza los conceptos de Spring (IoC, DI, beans) por otros nuevos e incompatibles.
B. Reduce la configuración manual mediante autoconfiguración, agrupa dependencias compatibles en starters, y evita instalar un servidor aparte gracias al servidor embebido.
C. Elimina la necesidad de escribir cualquier código Java.
D. Solo sirve para aplicaciones que no usan una base de datos.

_RA: RA-6_

---

**7. [Identificación]** ¿Qué es un "bean" en el contexto del contenedor IoC de
Spring?

A. Cualquier variable local declarada dentro de un método.
B. Un objeto cuya creación, configuración y ciclo de vida administra el contenedor (`ApplicationContext`), en vez de instanciarse manualmente con `new` en el código cliente.
C. Un archivo de configuración XML.
D. Una anotación exclusiva de Maven.

_RA: RA-7_

---

**8. [Identificación]** Ordená correctamente las fases del ciclo de vida de un
bean: (I) inicialización (`@PostConstruct`), (II) instanciación, (III)
destrucción (`@PreDestroy`), (IV) inyección de dependencias.

A. II, IV, I, III.
B. I, II, III, IV.
C. IV, II, I, III.
D. II, I, IV, III.

_RA: RA-7_

---

**9. [Selección múltiple]** ¿Por qué se prefiere la Inyección de Dependencias por
constructor frente a la inyección por campo para una dependencia obligatoria?

A. Porque por campo es imposible de escribir en Java.
B. Porque por constructor la dependencia queda explícita, puede declararse `final`, y la clase se puede instanciar en un test con valores de prueba sin necesitar un contenedor Spring.
C. Porque por campo Spring nunca logra inyectar el valor correctamente.
D. Porque por constructor la aplicación arranca más rápido, sin relación con las pruebas.

_RA: RA-8_

---

**10. [Identificación]** Una clase crea sus dependencias así:
`private ClienteEmail clienteEmail = new ClienteEmailSmtp();` dentro de su cuerpo.
¿Qué problema concreto genera esto para escribir un test unitario de esa clase?

A. Ninguno; los tests siempre pueden reemplazar cualquier `new` sin cambiar el código.
B. La clase queda acoplada a `ClienteEmailSmtp`: un test no puede sustituirlo por una versión de prueba sin modificar el código de la clase, porque la dependencia nunca se recibe desde afuera.
C. El código directamente no compila.
D. Spring lo resuelve automáticamente sin necesidad de refactorizar nada.

_RA: RA-9_
