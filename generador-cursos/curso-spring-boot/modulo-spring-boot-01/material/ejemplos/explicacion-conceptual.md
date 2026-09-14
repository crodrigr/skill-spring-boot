# 📚 Explicación conceptual — Módulo 1

## 🧠 Concepto: ¿Qué son los frameworks?

Un framework es un entorno de desarrollo que da una estructura predefinida para
construir aplicaciones profesionales, escalables, dinámicas y mantenibles.
Incluye librerías, herramientas y utilidades pensadas para reducir el esfuerzo
repetitivo, con el objetivo de facilitar el **desarrollo ágil de software**,
especialmente en aplicaciones complejas que gestionan grandes volúmenes de datos
e integraciones (como MediSalud, que combina citas, historias clínicas y
facturación). Sus características principales son: escalabilidad, Inversión de
Control (IoC), organización según el patrón Modelo-Vista-Controlador (MVC),
minimización de código repetitivo, y gestión integrada de aspectos transversales
como seguridad o acceso a datos. Los frameworks surgieron para evitar que cada
proyecto resolviera, una y otra vez y de forma distinta, los mismos problemas de
base de una aplicación empresarial.

## 🧠 Concepto: ¿Qué son las librerías?

Una librería es un conjunto de funciones y procedimientos reutilizables que
resuelven una necesidad puntual (por ejemplo, validar un formato de ISBN), sin
imponer ninguna estructura al resto del programa: el desarrollador la integra
libremente, donde y cuando la necesita. Existen librerías **estáticas** (su
código se copia dentro del ejecutable al compilar) y **dinámicas** (se cargan en
tiempo de ejecución, lo que permite compartir memoria entre aplicaciones y
actualizarlas sin recompilar todo el programa). En Java, una dependencia
declarada en Maven o Gradle se distribuye como un `.jar` y se comporta,
conceptualmente, como una librería dinámica.

## 🧠 Concepto: Frameworks vs. librerías

La diferencia clave es **quién controla el flujo de ejecución**. Con una
librería, el desarrollador decide cuándo llamarla: tiene el control. Con un
framework, el control se invierte —es el framework quien llama al código del
desarrollador cuando corresponde (al recibir una petición HTTP, al inyectar una
dependencia)—, lo cual es precisamente la Inversión de Control. Un framework
también suele abarcar la aplicación completa e imponer una estructura, mientras
que una librería resuelve una necesidad puntual y acotada. Ambos pueden convivir
en el mismo proyecto: Spring Boot (framework) organiza toda la aplicación, y una
librería de validación o de manejo de fechas se usa puntualmente dentro de ella.

## 🧠 Concepto: POO aplicada

Una clase describe la estructura y el comportamiento común de un conjunto de
objetos; una interfaz describe **qué puede hacer** un objeto sin decir cómo lo hace.
La herencia reutiliza comportamiento común entre clases relacionadas (una
`Estudiante` y un `Docente` son ambos un `Usuario`), y el polimorfismo permite
tratar objetos de tipos distintos de forma uniforme, ejecutando en cada caso la
versión de un método que corresponde a su tipo real. En Spring, este vocabulario
no es teórico: el contenedor administra objetos (beans) definidos como clases, y
con frecuencia programa contra interfaces (por ejemplo, un repositorio) para
poder cambiar la implementación sin tocar el código que la usa.

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

## 🧠 Concepto: ¿Qué es Spring Boot? Historia y características

Spring Boot es un framework que simplifica la creación, configuración y
despliegue de aplicaciones Java empresariales, priorizando la convención sobre la
configuración. Nació en 2014, después de que Spring (2003) resolviera el problema
original de la complejidad empresarial en Java, pero generara con el tiempo su
propio problema: una configuración cada vez más extensa a medida que los
proyectos crecían. Spring Boot no reemplaza a Spring: lo empaqueta con seis
características que eliminan configuración manual repetitiva: **configuración
automática** (autoconfigura según las dependencias presentes), **incrustación de
servidor** (Tomcat, Jetty o Undertow embebidos, sin instalar nada aparte),
**inicio rápido** (una anotación y un método `main` alcanzan para tener una
aplicación funcional), aptitud para **arquitecturas de microservicios**
(servicios independientes, desplegables y escalables por separado), **gestión de
dependencias mediante *starters*** (paquetes de dependencias ya verificadas como
compatibles entre sí) y **monitorización con actuadores** (endpoints de
administración y salud del sistema, listos para usar).

## 🧠 Concepto: Estructura general de un proyecto Spring Boot

Aunque Spring Boot no impone una única estructura, existe una convención
ampliamente adoptada: un archivo de build (`pom.xml` o `build.gradle`) en la
raíz, una clase principal anotada `@SpringBootApplication` con el método `main`,
y el código organizado en paquetes por capa —`controller` (recibe peticiones
HTTP), `service` (lógica de negocio), `repository` (acceso a datos) y `model`
(entidades del dominio)— dentro de `src/main/java`, más la configuración en
`src/main/resources/application.properties` y las pruebas en `src/test/java`
reflejando el mismo paquete que el código que prueban. Esta separación en capas
es una aplicación concreta del patrón MVC mencionado al hablar de frameworks, y
es la base sobre la que se construyen los módulos siguientes del curso.

## 🧠 Concepto: Anotaciones en Spring Boot

Una anotación es un metadato que se agrega al código fuente sin afectar
directamente su ejecución: describe la clase o el método para que una
herramienta —en este caso, Spring Boot— decida qué hacer con ella. Las
anotaciones son el mecanismo principal con el que Spring Boot define componentes
(`@Component`, `@Service`, `@Repository`), expone una API
(`@RestController`, `@GetMapping`), configura la aplicación (`@Configuration`,
`@Bean`) y gestiona la inyección de dependencias (`@Autowired`), evitando los
archivos de configuración extensos que exigía Spring en sus orígenes. Su uso
reduce el código repetitivo, mejora la legibilidad (el propósito de una clase se
entiende con solo ver sus anotaciones) y habilita una integración automática con
el resto del ecosistema Spring.

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
