# 📚 Explicación conceptual — Módulo 2

## 🧠 Concepto: ¿Qué es una dependencia?

Una dependencia es una relación entre dos módulos de código en la que uno (el
**dependiente**) necesita al otro (la **dependencia**) para funcionar.

- **Directa**: se declara explícitamente (una clase recibe o usa otra
  directamente, por ejemplo en su constructor).
- **Transitiva**: se genera indirectamente — si `A` depende de `B` y `B`
  depende de `C`, `A` depende transitivamente de `C`, aunque nunca lo
  mencione en su propio código.

Usar dependencias trae:

- **Ventajas**: reutilización de código, modularidad, especialización,
  facilidad de mantenimiento.
- **Desventajas**: acoplamiento, complejidad, vulnerabilidades de seguridad
  si la dependencia es externa, y dependencia de terceros que puedan dejar
  de mantener su producto.

En Spring, el contenedor de Inversión de Control (IoC) administra estas
relaciones por el estudiante: crea los objetos (beans), resuelve sus
dependencias —incluidas las transitivas— y permite anotaciones como
`@Qualifier` para desambiguar cuando existe más de una implementación
candidata para una misma interfaz.

📎 Ver en la práctica: [Ejemplo 01 — ¿Qué es una dependencia? Tipos directas y transitivas](01-que-es-una-dependencia.md)

## 🧠 Concepto: Inyección de Dependencias

La Inyección de Dependencias (DI) es un patrón de diseño que permite a una
clase recibir los objetos que necesita para funcionar, en vez de crearlos o
gestionarlos ella misma.

- **Sin DI**: una clase que instancia su propia dependencia con `new` queda
  acoplada a esa implementación concreta — cualquier cambio, o la necesidad
  de sustituirla por una versión de prueba, obliga a modificar la clase.
- **Con DI**: la clase depende de una abstracción (una interfaz), y recibe
  la implementación concreta desde afuera, por **constructor** (en el
  momento de crear el objeto) o por **propiedades**/*setter* (después de
  crearlo).

Beneficios concretos:

- Mejora la **modularidad** (cada clase se enfoca en su propia
  responsabilidad).
- Reduce la **complejidad** (no hay que resolver cómo crear cada
  dependencia dentro de cada clase que la usa).
- Aumenta la **flexibilidad** (la misma clase funciona con distintas
  implementaciones sin cambiar su código).
- Facilita las **pruebas unitarias** (se puede inyectar una implementación
  de prueba —un *mock* o un *stub*— en vez de la real).

📎 Ver en la práctica: [Ejemplo 02 — Inyección de Dependencias: de la clase acoplada a la clase desacoplada](02-inyeccion-de-dependencias.md)

## 🧠 Concepto: Estructura básica de una dependencia y buenas prácticas

Toda dependencia tiene una estructura mínima: un módulo dependiente, un
módulo dependencia, y —idealmente— una abstracción de por medio que los
conecta sin que el dependiente conozca los detalles internos de la
dependencia.

Buenas prácticas concretas para diseñar bien esa estructura:

- **Depender de abstracciones** (interfaces) en vez de implementaciones
  concretas, para poder cambiar la implementación sin tocar el código
  dependiente.
- **Minimizar las dependencias transitivas expuestas**, para que quien use
  un módulo no necesite conocer todo lo que ese módulo usa por dentro.
- **Evitar dependencias circulares**, en las que dos módulos terminan
  necesitándose mutuamente, directa o transitivamente.

Una dependencia circular casi nunca se resuelve con más código o una forma
de inyección distinta: suele ser una señal de que las responsabilidades
entre esos módulos están mal repartidas, y la solución real es
reorganizarlas.

📎 Ver en la práctica: [Ejemplo 03 — Estructura básica de una dependencia y buenas prácticas de diseño](03-estructura-basica-de-una-dependencia.md)

## 🧠 Concepto: Implementación y resolución de una dependencia general

Cuando una dependencia se declara como una interfaz, puede tener **más de
una implementación candidata**.

- Mientras exista una sola, Spring la resuelve automáticamente.
- En cuanto aparece una segunda `@Component` de la misma interfaz, el
  contenedor no puede decidir cuál usar y falla explícitamente al arrancar,
  con un error que lista los beans candidatos.
- `@Qualifier` resuelve esa ambigüedad: se anota cada implementación con un
  nombre distinto, y se anota el punto de inyección (por ejemplo, un
  parámetro del constructor) indicando cuál de esos nombres corresponde.

Esto es "implementar y resolver una dependencia general": no alcanza con
declarar el contrato (la interfaz) y sus implementaciones, también hay que
decirle a Spring, quien resuelve las dependencias, cómo elegir entre ellas
cuando hay más de una.

📎 Ver en la práctica: [Ejemplo 04 — Implementación y resolución de una dependencia general](04-implementacion-y-resolucion-de-una-dependencia-general.md)

## 🧠 Concepto: ¿Qué es un Java Bean?

Un Java Bean es un objeto que, en el contexto de Spring, el contenedor
administra, crea y controla; en un sentido más general y clásico, es una
clase que sigue un conjunto de convenciones:

- **Reutilizable**.
- **Manipulable visualmente** en herramientas de desarrollo.
- **Serializable**.
- Expone sus **propiedades** (de lectura o de lectura/escritura) mediante
  métodos **getter/setter** en vez de campos públicos.
- Puede **generar eventos** para notificar cambios de estado.
- Admite **introspección** (examinarse automáticamente desde herramientas
  externas).

"Ser un Java Bean" (cumplir esas convenciones) y "ser un bean administrado
por Spring" (estar anotado, por ejemplo, con `@Component`) son conceptos
relacionados pero distintos: una clase puede cumplir uno, el otro, ambos, o
ninguno.

📎 Ver en la práctica: [Ejemplo 05 — ¿Qué es un Java Bean?](05-que-es-un-java-bean.md)

## 🧠 Concepto: Ciclo de vida de un bean

El ciclo de vida de un bean en Spring Boot tiene cinco fases:

- **Instanciación**: el contenedor crea el objeto.
- **Configuración**: el contenedor le entrega sus dependencias — la
  inyección de dependencias ocurre exactamente en esta fase, no en la
  instanciación.
- **Inicialización**: se ejecutan métodos de arranque, como los anotados
  `@PostConstruct`, una vez que el bean ya está completamente configurado.
- **Listo para su uso**: el bean queda disponible mientras la aplicación
  corre.
- **Destrucción**: el contenedor libera el bean al apagarse, ejecutando
  métodos como los anotados `@PreDestroy`.

Cuando la dependencia se recibe por constructor, instanciación y
configuración ocurren casi al mismo tiempo (dentro de la misma llamada al
constructor), pero siguen siendo fases lógicamente distintas.

📎 Ver en la práctica: [Ejemplo 06 — Ciclo de vida de un bean: la fase de configuración, en detalle](06-ciclo-de-vida-de-un-bean.md)

## 🧠 Concepto: Uso de @Component

`@Component` es la anotación base de Spring para registrar una clase como
bean en el contenedor, sin ningún efecto técnico adicional: alcanza por sí
sola, sin configuración extra, para que Spring la detecte al escanear el
paquete y la administre.

Sus especializaciones semánticas registran el bean exactamente de la misma
forma técnica; la diferencia es principalmente semántica (comunican con
mayor precisión qué responsabilidad cumple la clase dentro de la
arquitectura por capas):

- `@Service` para lógica de negocio.
- `@Repository` para acceso a datos.
- `@Controller`/`@RestController` para exponer una API.

Cuando una clase no encaja claramente en ninguna de esas capas (por
ejemplo, una utilidad genérica), `@Component` es la anotación correcta.

📎 Ver en la práctica: [Ejemplo 07 — Uso de `@Component`](07-uso-de-component.md)
