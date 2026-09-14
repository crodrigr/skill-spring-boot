# 📘 Módulo 1 — Fundamentos de Java y Ecosistema Spring

Spring Boot para Aplicaciones Empresariales

---

## 🎯 Objetivos del módulo

- Entender qué son los frameworks y las librerías, y en qué se diferencian.
- Repasar POO y Java moderno aplicados a un dominio real.
- Comparar Maven y Gradle.
- Entender qué es Spring Boot, su historia y sus características.
- Reconocer la estructura de un proyecto Spring Boot y sus anotaciones.
- Comprender el contenedor IoC y el ciclo de vida de un bean.
- Aplicar y elegir el tipo correcto de Inyección de Dependencias.

---

## 🗺️ Ruta del módulo

1. ¿Qué son los frameworks?
2. ¿Qué son las librerías?
3. Frameworks vs librerías
4. POO aplicada
5. Java moderno
6. Maven vs Gradle
7. ¿Qué es Spring Boot?
8. Estructura de un proyecto Spring Boot
9. Anotaciones en Spring Boot
10. IoC Container
11. Inyección de Dependencias
12. Taller guiado y evaluación

---

## 🧠 ¿Qué es un framework?

Un entorno de desarrollo con una estructura predefinida para construir
aplicaciones profesionales, escalables y mantenibles.

```text
framework = estructura + librerías + herramientas + convenciones
```

Objetivo: desarrollo ágil, menos esfuerzo repetitivo.

---

## 🧠 Cinco características de un framework

- Escalabilidad
- Inversión de Control (IoC)
- Modelo-Vista-Controlador (MVC)
- Minimizar código repetitivo
- Bases generales auto-gestionadas (seguridad, datos, vistas)

---

## 📖 Breve historia de los frameworks

Cada proyecto resolvía, una y otra vez, los mismos problemas de base.

```text
Problema repetido → framework que lo resuelve una vez, para todos
```

En Java: Struts → Spring (2003) → Spring Boot (2014).

---

## 🧠 ¿Qué es una librería?

Funciones y procedimientos reutilizables para una necesidad puntual, **sin**
imponer estructura al resto del programa.

```java
boolean valido = ValidadorIsbn.esValido(isbn); // se llama cuando se quiere
```

---

## 🧠 Librerías estáticas vs dinámicas

| Estática | Dinámica |
|---|---|
| Se copia dentro del ejecutable al compilar | Se carga en tiempo de ejecución |
| Ejecutable más grande | Memoria compartida, actualizable sin recompilar |

Un `.jar` de Maven/Gradle se comporta como librería dinámica.

---

## 🔍 Frameworks vs librerías: quién controla el flujo

```text
Librería:  el desarrollador llama a la librería
Framework: el framework llama al código del desarrollador
```

Esa inversión de control es la diferencia clave.

---

## 🔍 Frameworks vs librerías: tabla comparativa

| | Framework | Librería |
|---|---|---|
| Estructura | Impuesta | Libre |
| Alcance | Toda la aplicación | Una necesidad puntual |
| Ejemplo | Spring Boot | Validador de ISBN |

Ambos conviven en el mismo proyecto.

---

## 🧠 POO aplicada: el vocabulario que usa Spring

Spring administra **objetos** (beans) definidos como clases, y programa contra
**interfaces** para poder cambiar la implementación sin romper el código cliente.

```text
Clase → estructura y comportamiento común
Interfaz → qué puede hacer un objeto, sin decir cómo
```

---

## 🧠 Herencia: reutilizar lo común

Un `Estudiante` y un `Docente` son ambos un `Usuario`.

```java
public abstract class Usuario { /* nombre, código */ }
public class Estudiante extends Usuario { /* límite: 3 */ }
public class Docente extends Usuario { /* límite: 10 */ }
```

---

## 🧠 Interfaces: qué puede hacer, no qué es

Un `Libro` y un `RecursoDigital` no tienen relación de herencia, pero ambos pueden
prestarse.

```java
public interface Prestable {
    int calcularDiasDevolucion();
}
```

---

## 🧠 Polimorfismo: el mismo código, distintos tipos

```java
for (Prestable recurso : recursos) {
    recurso.calcularDiasDevolucion(); // ejecuta la versión de cada tipo
}
```

Sin `instanceof`, sin *casts*.

---

## 🧠 Java moderno: streams y lambdas

De un bucle imperativo con lista mutable...

```java
for (Cita c : citas) { if (...) lista.add(c.paciente()); }
```

...a una expresión declarativa.

```java
citas.stream().filter(...).map(Cita::paciente).toList();
```

---

## 🧠 Optional: la ausencia de un valor, explícita

```java
Optional<Medico> medico = repositorio.buscarPorId(id);
medico.orElseThrow(() -> new NoSuchElementException(...));
```

Sin `null`, sin sorpresas en tiempo de ejecución.

---

## 🧠 Records: objetos de valor inmutables

```java
public record DatosContacto(String telefono, String email) {}
```

Constructor, *getters*, `equals`, `hashCode` y `toString` generados.

---

## 🧠 Maven: XML declarativo

```xml
<groupId>com.medisalud</groupId>
<artifactId>medisalud-api</artifactId>
```

Fases de build fijas: `validate → compile → test → package`.

---

## 🧠 Gradle: DSL flexible

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

Tareas configurables, con caché incremental.

---

## 🔍 Maven vs Gradle: misma meta, distinto camino

| | Maven | Gradle |
|---|---|---|
| Formato | XML | DSL (Groovy/Kotlin) |
| Build | Fases fijas | Tareas configurables |

Ambos producen el mismo artefacto Spring Boot ejecutable.

---

## 🧠 ¿Qué es Spring Boot?

Framework que simplifica crear, configurar y desplegar apps Java empresariales.

```text
Convención sobre configuración: menos configuración manual, más lógica de negocio
```

No reemplaza a Spring: lo empaqueta.

---

## 📖 Historia de Spring Boot

```text
2003: nace Spring, resuelve la complejidad empresarial de J2EE
Con el tiempo: la configuración del propio Spring se vuelve extensa
2014: nace Spring Boot, resuelve ESE nuevo problema
```

---

## 🧠 Característica 1 — Configuración automática

Detecta las dependencias del proyecto y configura la aplicación en consecuencia.

```text
spring-boot-starter-web presente → Spring MVC configurado automáticamente
```

---

## 🧠 Característica 2 — Servidor embebido

Tomcat, Jetty o Undertow **dentro** del mismo proceso.

```text
java -jar biblioteca-api.jar   →   no hace falta instalar un servidor aparte
```

---

## 🧠 Característica 3 — Inicio rápido

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) { SpringApplication.run(...); }
}
```

Una anotación + un `main` = aplicación funcional.

---

## 🧠 Característica 4 — Aptitud para microservicios

Servicios pequeños, independientes, desplegables y escalables por separado.

```text
servicio-citas.jar     servicio-facturacion.jar     servicio-catalogo.jar
```

---

## 🧠 Característica 5 — Starters

Un *starter* agrupa dependencias ya probadas como compatibles entre sí.

```text
spring-boot-starter-web → Spring MVC + Tomcat + Jackson, todo compatible
```

---

## 🧠 Característica 6 — Monitorización con actuadores

```text
GET /actuator/health → {"status":"UP"}
```

Endpoints de administración listos para usar, sin escribirlos a mano.

---

## 🔍 Qué resuelve cada característica

| Problema manual | Solución de Spring Boot |
|---|---|
| Configurar Spring MVC a mano | Autoconfiguración |
| Elegir versiones compatibles | Starters |
| Instalar un servidor aparte | Servidor embebido |
| Escribir endpoints de salud | Actuadores |

---

## 🌳 Estructura de un proyecto Spring Boot

```text
biblioteca-api/
├── pom.xml
└── src/main/java/com/biblioteca/api/
    ├── BibliotecaApiApplication.java
    ├── controller/
    ├── service/
    ├── repository/
    └── model/
```

---

## 🔍 Qué hace cada capa

- **controller** → recibe peticiones HTTP.
- **service** → lógica de negocio.
- **repository** → acceso a datos.
- **model** → entidades del dominio.

Convención de arquitectura por capas (aplica el patrón MVC).

---

## 🧠 ¿Qué es una anotación?

Metadato sobre el código que Spring Boot lee para decidir qué hacer.

```java
@Service // "esta clase tiene lógica de negocio"
public class ServicioPrestamos { ... }
```

No se ejecuta como código: se **interpreta** al arrancar la aplicación.

---

## 🔍 Anotaciones más comunes

| Anotación | Uso |
|---|---|
| `@SpringBootApplication` | Clase principal |
| `@Component` / `@Service` / `@Repository` | Beans por capa |
| `@RestController` + `@GetMapping` | Exponer un endpoint HTTP |
| `@Autowired` | Inyección de dependencias |

---

## 💡 Ventajas de usar anotaciones

- Menos código repetitivo (sin XML extenso).
- Mayor legibilidad: el propósito se ve en la anotación.
- Integración automática con el resto de Spring.

---

## 🧠 Inversión de Control: quién decide crear el objeto

```text
Antes: la clase crea sus dependencias con new
Con IoC: el contenedor las crea y se las entrega
```

El objeto administrado por el contenedor se llama **bean**.

---

## 🧠 ApplicationContext: el contenedor IoC

Al arrancar, escanea las clases anotadas y decide qué beans crear, configurar y
entregar.

```java
ApplicationContext contexto = new AnnotationConfigApplicationContext(Config.class);
```

---

## 🪜 Ciclo de vida de un bean

1. Instanciación
2. Inyección de dependencias
3. Inicialización (`@PostConstruct`)
4. Uso
5. Destrucción (`@PreDestroy`)

---

## 🧠 Inyección por constructor

```java
public ServicioPrestamos(RepositorioLibros repositorioLibros) {
    this.repositorioLibros = repositorioLibros;
}
```

Explícita, permite `final`, fácil de probar sin contenedor.

---

## 🧠 Inyección por setter

```java
@Autowired
public void setRepositorioLibros(RepositorioLibros r) { ... }
```

Útil para dependencias realmente **opcionales**.

---

## 🧠 Inyección por campo

```java
@Autowired
private RepositorioLibros repositorioLibros;
```

Compacta, pero oculta las dependencias reales de la clase.

---

## 🔍 Las tres formas, comparadas

| Forma | Ventaja principal | Riesgo principal |
|---|---|---|
| Constructor | Explícita, testeable | Constructores largos si hay muchas dependencias |
| Setter | Dependencias opcionales | Objeto puede quedar a medio configurar |
| Campo | Muy compacta | Difícil de probar sin contenedor |

---

## 🚧 El problema del acoplamiento con `new`

```java
private ClienteEmail clienteEmail = new ClienteEmailSmtp();
```

No se puede reemplazar por una versión de prueba sin modificar la clase.

---

## 💡 La solución: recibir la dependencia, no crearla

```java
public ServicioNotificaciones(RepositorioUsuarios r, ClienteEmail c) {
    this.repositorioUsuarios = r;
    this.clienteEmail = c;
}
```

Ahora sí: `new ServicioNotificaciones(repositorioDePrueba, clienteDePrueba)`.

---

## 🛠️ Actividad práctica

Taller 01: modelar `Paciente`, `Cita` y `Medico` de MediSalud en Java puro, e
identificar los acoplamientos que un contenedor IoC resolvería después.

---

## 📌 Resumen del módulo

- Un framework impone estructura e invierte el control; una librería no.
- POO e interfaces son la base de cómo Spring organiza objetos.
- Java moderno (streams, `Optional`, records) es el estilo de código del curso.
- Spring Boot automatiza configuración con seis características clave.
- Un proyecto Spring Boot se organiza por capas y se anota, no se configura a mano.
- El contenedor IoC crea y administra beans siguiendo un ciclo de vida ordenado.
- La inyección por constructor es la forma preferida de recibir dependencias
  obligatorias.

---

## 📝 Evaluación

Quiz 01 (16 ítems) + Taller 01 + ejercicios del módulo.
