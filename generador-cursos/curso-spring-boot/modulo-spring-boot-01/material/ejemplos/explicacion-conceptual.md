# 📚 Explicación conceptual — Módulo 1

## 🧠 Concepto: POO aplicada

Una clase describe la estructura y el comportamiento común de un conjunto de
objetos; una interfaz describe **qué puede hacer** un objeto sin decir cómo lo hace.
La herencia reutiliza comportamiento común entre clases relacionadas (una
`Estudiante` y un `Docente` son ambos un `Usuario`), y el polimorfismo permite
tratar objetos de tipos distintos de forma uniforme, ejecutando en cada caso la
versión de un método que corresponde a su tipo real. En Spring, este vocabulario no
es teórico: el contenedor administra objetos (beans) definidos como clases, y con
frecuencia programa contra interfaces (por ejemplo, un repositorio) para poder
cambiar la implementación sin tocar el código que la usa.

## 🧠 Concepto: Java moderno (streams, lambdas, Optional, records)

Un **stream** describe una operación sobre una colección de datos (filtrar,
transformar, agregar) sin escribir el bucle que la recorre; una **lambda** es una
función anónima y compacta que se pasa como argumento a esa operación. **Optional**
representa explícitamente que un valor puede estar ausente, obligando a manejar ese
caso en el código en vez de arriesgarse a un `NullPointerException`. Un **record**
es una forma compacta de declarar una clase inmutable que solo transporta datos
(constructor, *getters*, `equals`, `hashCode` y `toString` generados
automáticamente). Estas cuatro herramientas son el estilo de código que Spring y
sus ejemplos asumen como base a partir de este módulo.

## 🧠 Concepto: Maven vs Gradle

Ambos son herramientas de **build**: descargan dependencias, compilan el código,
ejecutan pruebas y empaquetan la aplicación. Maven describe el proyecto de forma
**declarativa en XML** (`pom.xml`), con un ciclo de vida de fases fijo (`validate`,
`compile`, `test`, `package`, …). Gradle usa un **DSL** (Groovy o Kotlin,
`build.gradle` o `build.gradle.kts`) que permite describir tareas de forma más
flexible y, en general, con builds más rápidos gracias al cacheo incremental. Un
proyecto Spring Boot puede generarse con cualquiera de los dos; la elección no
cambia los conceptos de Spring, solo la forma de declarar dependencias y de
ejecutar tareas.

## 🧠 Concepto: Ecosistema Spring vs Spring Boot

Spring (el "Spring clásico") es un conjunto de módulos que resuelven problemas de
una aplicación empresarial (inyección de dependencias, acceso a datos, web, etc.),
pero requiere configurar a mano cada pieza: qué beans existen, qué versiones de
dependencias son compatibles entre sí, y cómo desplegar la aplicación en un
servidor. **Spring Boot** no reemplaza a Spring: lo empaqueta con **autoconfiguración**
(detecta qué hay en el classpath y configura beans razonables por defecto),
**starters** (dependencias agrupadas y compatibles entre sí, por ejemplo
`spring-boot-starter-web`) y un **servidor embebido** (la aplicación se ejecuta con
`java -jar`, sin instalar un servidor aparte). El resultado es el mismo Spring, con
mucho menos código de configuración manual.

## 🧠 Concepto: IoC Container (ApplicationContext y ciclo de vida de un bean)

La Inversión de Control invierte quién decide cuándo y cómo se crea un objeto: en
vez de que una clase cree sus propias dependencias con `new`, un contenedor
(`ApplicationContext` en Spring) las crea, las configura y se las entrega. Un
objeto administrado así se llama **bean**. El contenedor sigue, para cada bean, un
ciclo de vida ordenado: **instanciación** (crea el objeto), **inyección de
dependencias** (le entrega lo que necesita), **inicialización** (ejecuta lógica de
arranque, por ejemplo un método anotado `@PostConstruct`), **uso** (el bean queda
disponible mientras la aplicación corre) y **destrucción** (libera recursos al
apagarse, por ejemplo con `@PreDestroy`). Entender este ciclo es lo que permite
entender qué automatiza Spring cuando "inyecta" algo.

## 🧠 Concepto: Inyección de Dependencias (constructor, setter, campo)

Inyección de Dependencias es el mecanismo concreto con el que el contenedor le da
a un bean lo que necesita, en vez de que el bean lo cree él mismo. Hay tres formas:
**por constructor** (las dependencias se pasan como parámetros del constructor;
permite declarar campos `final` y hace imposible crear un objeto a medio
configurar), **por setter** (un método `set...` recibe la dependencia después de
construir el objeto; útil para dependencias opcionales) y **por campo** (Spring
asigna el valor directamente sobre un campo anotado `@Autowired`, sin pasar por
constructor ni setter; es la más compacta de escribir, pero oculta las dependencias
reales de la clase y dificulta las pruebas sin un contenedor). La inyección por
constructor es la forma recomendada por convención en Spring para dependencias
obligatorias, precisamente por ser explícita, inmutable y fácil de instanciar con
valores de prueba en un test unitario.
