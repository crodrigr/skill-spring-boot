# 📘 Módulo 1 — Fundamentos de Java y Ecosistema Spring

Spring Boot para Aplicaciones Empresariales

---

## 🎯 Objetivos del módulo

- Repasar POO y Java moderno aplicados a un dominio real.
- Comparar Maven y Gradle.
- Entender qué resuelve Spring Boot frente a Spring clásico.
- Comprender el contenedor IoC y el ciclo de vida de un bean.
- Aplicar y elegir el tipo correcto de Inyección de Dependencias.

---

## 🗺️ Ruta del módulo

1. POO aplicada
2. Java moderno
3. Maven vs Gradle
4. Ecosistema Spring vs Spring Boot
5. IoC Container
6. Inyección de Dependencias
7. Taller guiado y evaluación

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

## 🧠 Spring clásico: todo configurado a mano

- Beans declarados uno por uno.
- `DispatcherServlet` registrado en `web.xml`.
- Servidor externo (Tomcat/Jetty) instalado aparte.

---

## 🧠 Spring Boot: lo mismo, automatizado

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) { SpringApplication.run(...); }
}
```

Autoconfiguración + starters + servidor embebido.

---

## 🔍 Qué resuelve cada pieza de Spring Boot

| Problema manual | Solución de Spring Boot |
|---|---|
| Configurar Spring MVC a mano | Autoconfiguración |
| Elegir versiones compatibles | Starter (`spring-boot-starter-web`) |
| Instalar un servidor aparte | Servidor embebido |

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

- POO e interfaces son la base de cómo Spring organiza objetos.
- Java moderno (streams, `Optional`, records) es el estilo de código del curso.
- Spring Boot automatiza configuración que Spring clásico exige a mano.
- El contenedor IoC crea y administra beans siguiendo un ciclo de vida ordenado.
- La inyección por constructor es la forma preferida de recibir dependencias
  obligatorias.

---

## 📝 Evaluación

Quiz 01 (10 ítems) + Taller 01 + ejercicios del módulo.
