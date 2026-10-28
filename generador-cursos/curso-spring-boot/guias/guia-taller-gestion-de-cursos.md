# 🧭 Guía paso a paso — Taller «Gestión de cursos»

> 👩‍🏫 **Material docente.** Guía para resolver el taller **junto con los estudiantes**, en
> vivo, aplicando lo visto en el [Módulo 01](../01-introduccion-a-spring-boot/) y el
> [Módulo 02](../02-manejo-de-dependencias-y-java-beans/). Contiene código completo: no se
> entrega a los estudiantes antes de resolver el taller.

<details>
<summary><strong>🎯 Objetivo</strong></summary>

Construir una API REST con Spring Boot que administre **estudiantes, cursos y matrículas**,
organizada en las **tres capas del curso** (presentación, negocio y persistencia), con la
persistencia **en memoria** (listas), sin base de datos ni JPA.

Al terminar, el estudiante:

- Arma un proyecto Spring Boot con Maven y lo ejecuta (M1).
- Separa responsabilidades en tres capas y respeta la regla de dependencia (M1 y M2).
- Declara beans con `@RestController`, `@Service` y `@Repository`, y los conecta con
  **inyección por constructor** contra **interfaces** (M2).
- Implementa reglas de negocio con listas, ciclos, condicionales y contadores.
- Prueba la API con Postman y lee los códigos HTTP `201`, `400`, `404` y `409`.

</details>

<details>
<summary><strong>📝 Introducción: el problema y sus requerimientos</strong></summary>

> 🗣️ **Para abrir la sesión.** Se lee el problema, se subrayan los
> requerimientos y, **antes de escribir código**, se ubica cada uno en el árbol de «Arquitectura del taller».
> Quien entiende dónde vive cada requerimiento, sabe qué clase escribir.

### 🏫 El problema

Una **academia de programación** necesita una API REST sencilla para administrar sus
**estudiantes**, sus **cursos** y las **matrículas** que los relacionan. La academia quiere
registrar estudiantes, crear cursos, matricular estudiantes y consultar qué tan llenos están
los cursos. El sistema se construye de forma progresiva, en cuatro fases.

### 🧩 Entidades

| Entidad | Atributos | Observación |
|---|---|---|
| **Estudiante** | `id`, `nombre`, `edad`, `correo` | El `correo` identifica de forma única a la persona. |
| **Curso** | `id`, `nombre`, `precio`, `cupoMaximo` | `cupoMaximo` es el tope de matriculados. |
| **Matrícula** | `id`, `estudianteId`, `cursoId` | Relaciona estudiante y curso **por identificadores**, no por objetos. |

```text
Estudiante 1 ──── * Matrícula * ──── 1 Curso
```

Un estudiante puede estar en varios cursos y un curso tiene varios estudiantes; la matrícula
es la pieza intermedia.

<details>
<summary><strong>✅ Requerimientos funcionales</strong></summary>


| Código | Fase | Requerimiento | Endpoint |
|---|:-:|---|---|
| **RF-01** | 1 | Registrar un estudiante. | `POST /estudiantes` |
| **RF-02** | 1 | Listar todos los estudiantes. | `GET /estudiantes` |
| **RF-03** | 1 | Consultar un estudiante por su identificador. | `GET /estudiantes/{id}` |
| **RF-04** | 2 | Registrar un curso. | `POST /cursos` |
| **RF-05** | 2 | Listar los cursos. | `GET /cursos` |
| **RF-06** | 2 | Listar los cursos económicos (precio inferior a $200.000). | `GET /cursos/economicos` |
| **RF-07** | 3 | Matricular un estudiante en un curso. | `POST /matriculas` |
| **RF-08** | 4 | Consultar la ocupación de un curso: nombre, cupo máximo, matriculados, cupos disponibles y porcentaje de ocupación. | `GET /cursos/{id}/ocupacion` |

</details>

<details>
<summary><strong>📏 Reglas de negocio</strong></summary>


| Código | Regla | Se aplica en | Si se incumple |
|---|---|---|:-:|
| **RN-01** | El identificador de toda entidad se genera automáticamente (1, 2, 3…). | RF-01, RF-04, RF-07 | — |
| **RN-02** | No puede haber dos estudiantes con el mismo correo. | RF-01 | `409` |
| **RN-03** | Un estudiante inexistente responde «no encontrado». | RF-03, RF-07 | `404` |
| **RN-04** | El precio de un curso debe ser mayor que cero. | RF-04 | `400` |
| **RN-05** | El cupo máximo de un curso debe ser mayor que cero. | RF-04 | `400` |
| **RN-06** | Un curso es «económico» si su precio es **menor** que 200.000. | RF-06 | — |
| **RN-07** | El curso de una matrícula debe existir. | RF-07 | `404` |
| **RN-08** | Un estudiante no puede matricularse dos veces en el mismo curso. | RF-07 | `409` |
| **RN-09** | El curso debe tener cupos disponibles (matriculados < cupo máximo). | RF-07 | `409` |
| **RN-10** | La matrícula se registra **solo si se cumplen todas** las reglas anteriores. | RF-07 | — |
| **RN-11** | Porcentaje de ocupación = (matrículas del curso ÷ cupo máximo) × 100. | RF-08 | — |

</details>

<details>
<summary><strong>🛠️ Requerimientos no funcionales y restricciones</strong></summary>


| Código | Requerimiento |
|---|---|
| **RNF-01** | Tecnologías: Java 21 o superior, Spring Boot 3 y Maven. |
| **RNF-02** | **Sin base de datos ni JPA**: los datos viven en listas en memoria y se pierden al reiniciar. |
| **RNF-03** | Organización en **tres capas** (presentación, negocio y persistencia), con la regla de dependencia presentación → negocio → persistencia. |
| **RNF-04** | Las dependencias se resuelven con **inyección por constructor** y los repositorios se usan a través de **interfaces**. |
| **RNF-05** | Errores con respuesta HTTP clara: `201` al crear, `400` dato inválido, `404` no existe, `409` conflicto. |
| **RNF-06** | La API se prueba con Postman y se conservan evidencias de cada escenario. |
| **RNF-07** | Duración: 1 h 30 min, trabajo individual. El objetivo es practicar la **lógica de programación** (listas, ciclos, condicionales, contadores) dentro de una API REST, no construir un sistema completo. |

> 💡 **Alcance.** Quedan fuera: autenticación, base de datos, validación con anotaciones,
> paginación y concurrencia. Se verán en módulos posteriores.

</details>

</details>

<details>
<summary><strong>⏱️ Plan de la sesión</strong></summary>


| Paso | Minutos | Actividad | Quién escribe |
|---|---:|---|---|
| 0 | 0 – 10 | Preparación: proyecto, capas y plano del taller | Docente |
| 1 | 10 – 30 | Fase 1 · Estudiantes | Docente y estudiantes, a la par |
| 2 | 30 – 45 | Fase 2 · Cursos | Estudiantes (docente guía) |
| 3 | 45 – 70 | Fase 3 · Matrículas | Estudiantes (docente guía) |
| 4 | 70 – 80 | Fase 4 · Reporte de ocupación | Estudiantes; es el paso que se recorta si falta tiempo |
| 5 | 80 – 90 | Pruebas en Postman y cierre | Todos |

> 💡 **Ritmo.** La Fase 1 se hace entera en vivo: allí se fija el patrón (modelo → repositorio →
> servicio → controlador). Las fases 2 y 3 repiten el patrón y son las que los estudiantes
> escriben solos.

</details>

<details>
<summary><strong>🧠 Conceptos de los módulos 01 y 02 que se resaltan</strong></summary>


| Concepto | Dónde aparece en el taller | Módulo | Ejemplo de apoyo |
|---|---|:-:|---|
| Spring Boot y `@SpringBootApplication` | Clase `AcademiaApplication` | 01 | [¿Qué es Spring Boot?](../01-introduccion-a-spring-boot/material/ejemplos/07-que-es-spring-boot.md) |
| Estructura de un proyecto y Maven | `pom.xml`, paquetes por capa | 01 | [Estructura de un proyecto](../01-introduccion-a-spring-boot/material/ejemplos/08-estructura-de-un-proyecto-spring-boot.md) |
| Anotaciones | `@RestController`, `@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestBody` | 01 | [Anotaciones en Spring Boot](../01-introduccion-a-spring-boot/material/ejemplos/09-anotaciones-en-spring-boot.md) |
| Contenedor IoC | Spring crea los objetos; nosotros nunca escribimos `new Servicio…()` | 01 | [IoC y ciclo de vida de un bean](../01-introduccion-a-spring-boot/material/ejemplos/10-ioc-container-ciclo-de-vida-de-un-bean.md) |
| Java moderno | `record` (en `Ocupacion`), `Optional` en las búsquedas | 01 | [Java moderno](../01-introduccion-a-spring-boot/material/ejemplos/05-java-moderno-streams-lambdas-optional-records.md) |
| Dependencia e inyección | Controlador → Servicio → Repositorio | 02 | [Inyección de dependencias](../02-manejo-de-dependencias-y-java-beans/material/ejemplos/02-inyeccion-de-dependencias.md) |
| Programar contra interfaces | `RepositorioEstudiantes` (interfaz) y `RepositorioEstudiantesEnMemoria` (clase) | 02 | [Resolución de una dependencia](../02-manejo-de-dependencias-y-java-beans/material/ejemplos/04-implementacion-y-resolucion-de-una-dependencia-general.md) |
| Java Bean y `@Component` (y sus estereotipos) | `@Service`, `@Repository` | 02 | [Uso de @Component](../02-manejo-de-dependencias-y-java-beans/material/ejemplos/07-uso-de-component.md) |
| Ciclo de vida: bean **singleton** | La lista vive en el repositorio y **sobrevive entre peticiones** porque hay una sola instancia | 02 | [Ciclo de vida de un bean](../02-manejo-de-dependencias-y-java-beans/material/ejemplos/06-ciclo-de-vida-de-un-bean.md) |
| Dependencia circular | Se menciona al armar `ServicioCursos` (usa el repositorio de matrículas, no el servicio) | 02 | [Explicación conceptual](../02-manejo-de-dependencias-y-java-beans/material/ejemplos/explicacion-conceptual.md) |

</details>

<details>
<summary><strong>🏗️ Arquitectura del taller: el árbol de infraestructura</strong></summary>

Este es el árbol de paquetes que se construye durante la sesión. Cada carpeta tiene **una sola
responsabilidad**, y cada requerimiento de la sección anterior cae en una de ellas (los códigos RF, RN y RNF son los de la Introducción).

```text
📁 src/main/java/com/academia
├── 📄 AcademiaApplication.java
├── 📁 presentacion                      ← 1 · lo que ve el cliente HTTP
│   └── 📁 controlador   ControladorEstudiantes · ControladorCursos · ControladorMatriculas
├── 📁 negocio                           ← 2 · las reglas
│   ├── 📁 model         Estudiante · Curso · Matricula · Ocupacion
│   ├── 📁 service       ServicioEstudiantes · ServicioCursos · ServicioMatriculas
└── 📁 persistence                       ← 3 · dónde viven los datos (listas en memoria)
    └── 📁 repository    Repositorio… (interfaces) · Repositorio…EnMemoria (@Repository)
```

Regla de dependencia: **presentación → negocio → persistencia**. El controlador nunca toca un
repositorio; el servicio nunca conoce HTTP.

```text
HTTP ─▶ Controlador ─▶ Servicio ─▶ Repositorio (interfaz) ◀─ RepositorioEnMemoria (List)
        valida forma   reglas      contrato                  almacenamiento
```

<details>
<summary><strong>🌳 Qué hay en cada parte del árbol</strong></summary>


| Paquete | Responsabilidad | Clases | Se crea en | Anotación de Spring |
|---|---|---|:-:|---|
| `AcademiaApplication` | Arranca la aplicación y define desde dónde Spring **escanea** beans. | `AcademiaApplication` | Paso 0 | `@SpringBootApplication` |
| `presentacion.controlador` | Recibe la petición HTTP, entrega los datos al servicio y devuelve la respuesta. **Sin reglas.** | `ControladorEstudiantes`, `ControladorCursos`, `ControladorMatriculas` | Fases 1–4 | `@RestController` |
| `negocio.model` | Las entidades del dominio y el resultado del reporte. Clases Java sencillas. | `Estudiante`, `Curso`, `Matricula`, `Ocupacion` | Fases 1–4 | — |
| `negocio.service` | **Las reglas de negocio**: duplicados, validaciones, cupos, cálculo. Señala los errores con `ResponseStatusException`. | `ServicioEstudiantes`, `ServicioCursos`, `ServicioMatriculas` | Fases 1–4 | `@Service` |
| `persistence.repository` | Guarda y busca datos. La **interfaz** es el contrato; la clase `…EnMemoria` lo cumple con una `List`. | `RepositorioEstudiantes`, `RepositorioCursos`, `RepositorioMatriculas` y sus `…EnMemoria` | Fases 1–3 | `@Repository` (solo en las implementaciones) |

</details>

<details>
<summary><strong>🔗 ¿Dónde vive cada requerimiento?</strong></summary>


| Requerimiento | Presentación | Negocio | Persistencia |
|---|---|---|---|
| **RF-01** Registrar estudiante · **RN-02** | `ControladorEstudiantes.registrar` | `ServicioEstudiantes.registrar` (rechaza el correo repetido) | `existePorCorreo`, `guardar` (asigna el id, **RN-01**) |
| **RF-02 / RF-03** Listar y consultar · **RN-03** | `listar`, `obtener` | `ServicioEstudiantes.obtener` (responde `404` con `ResponseStatusException`) | `listar`, `buscarPorId` |
| **RF-04** Registrar curso · **RN-04, RN-05** | `ControladorCursos.registrar` | `ServicioCursos.registrar` (valida precio y cupo) | `RepositorioCursos.guardar` |
| **RF-05 / RF-06** Listar y económicos · **RN-06** | `listar`, `listarEconomicos` | `ServicioCursos.listarEconomicos` (filtro con ciclo) | `RepositorioCursos.listar` |
| **RF-07** Matricular · **RN-03, RN-07 a RN-10** | `ControladorMatriculas.matricular` | `ServicioMatriculas.matricular` (las cinco reglas, en orden) | `existe`, `contarPorCurso`, `guardar` |
| **RF-08** Ocupación · **RN-11** | `ControladorCursos.ocupacion` | `ServicioCursos.calcularOcupacion` | `RepositorioMatriculas.contarPorCurso` |
| **RNF-05** Códigos HTTP | — | `ResponseStatusException` en los servicios | — |

> 🔎 **Lectura clave.** Todas las reglas (RN) terminan en `negocio.service`; los controladores
> y los repositorios casi no tienen `if`. Esa es la ventaja de las capas: si una regla cambia,
> se sabe en qué archivo buscarla.

</details>

> 🗣️ **Pregunta para el grupo:** si mañana cambiamos la lista por una base de datos,
> ¿qué capas habría que tocar? *(Respuesta: solo la de persistencia, porque el servicio depende
> de la interfaz.)*

</details>

---

<details>
<summary><strong>🚀 Paso 0 — Preparación</strong></summary>

<details>
<summary><strong>Paso 0.1 · Crear el proyecto</strong></summary>


En [start.spring.io](https://start.spring.io):

| Campo | Valor |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.x (última estable) |
| Group | `com.academia` |
| Artifact | `academia` |
| Packaging / Java | Jar / 21 (o superior) |
| Dependencies | **Spring Web** (únicamente) |

Se descarga, se descomprime y se abre en el IDE. El `pom.xml` queda con una sola dependencia
funcional:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

> 🔎 **Concepto (M1):** `starter` = conjunto de dependencias ya compatibles entre sí. No hay
> `spring-boot-starter-data-jpa` porque no usamos base de datos.

</details>

<details>
<summary><strong>Paso 0.2 · Revisar la clase principal y crear los paquetes</strong></summary>

Initializr ya genera la clase principal, `AcademiaApplication`; **no se renombra ni se
modifica**. Solo se crean los paquetes vacíos de la sección anterior, siempre dentro de
`com.academia`.

**Archivo (generado por Initializr, tal cual):** `src/main/java/com/academia/AcademiaApplication.java`

```java
package com.academia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AcademiaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcademiaApplication.class, args);
    }
}
```

> 🔎 **Concepto (M1):** `@SpringBootApplication` escanea **su propio paquete y los de abajo**.
> Por eso todo vive bajo `com.academia`; una clase fuera de ese árbol no sería detectada como
> bean.

</details>

<details>
<summary><strong>Paso 0.3 · Compilar y ejecutar con Maven desde la consola</strong></summary>

El proyecto de Initializr incluye el **Maven Wrapper** (`mvnw` y `mvnw.cmd`): permite
compilar sin tener Maven instalado, solo con Java. Todos los comandos se ejecutan **en la
carpeta del proyecto** (donde está el `pom.xml`). La guía usa **`./mvnw`** (Bash en Linux)
como forma principal; en Windows se reemplaza por `mvnw.cmd` (CMD) o `.\mvnw.cmd` (PowerShell).

**1. Verificar las herramientas**

| Consola | Comandos |
|---|---|
| **Bash** (Linux / macOS) | `java -version` · `chmod +x mvnw` (solo una vez) · `./mvnw -v` |
| **CMD** (Windows) | `java -version` · `mvnw.cmd -v` |
| **PowerShell** (Windows) | `java -version` · `.\mvnw.cmd -v` |

`java -version` debe mostrar la versión **21 o superior**. Si no, se apunta `JAVA_HOME` al
JDK correcto (solo para la sesión actual de la consola):

```bash
# Bash (Linux)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
```

```bat
:: CMD
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
```

```powershell
# PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

**2. Compilar y empaquetar**

```bash
# Bash (Linux / macOS)
./mvnw clean package
```

```bat
:: CMD
mvnw.cmd clean package
```

```powershell
# PowerShell
.\mvnw.cmd clean package
```

Termina con `BUILD SUCCESS` y deja el ejecutable en la carpeta `target`.

**3. Comandos de Maven y para qué sirven**

Se muestran con `./mvnw` (Linux); en las otras consolas cambia solo el prefijo del comando.

*Ciclo de vida (se ejecutan en orden; cada fase incluye las anteriores):*

| Comando | Para qué sirve |
|---|---|
| `./mvnw validate` | Comprueba que el `pom.xml` y la estructura del proyecto sean correctos |
| `./mvnw compile` | Compila el código fuente (`src/main/java`) hacia `target/classes` |
| `./mvnw test` | Compila y ejecuta las pruebas unitarias (`src/test/java`) |
| `./mvnw package` | Empaqueta la aplicación en un `.jar` dentro de `target/` |
| `./mvnw verify` | Ejecuta además las pruebas de integración y validaciones de calidad |
| `./mvnw install` | Copia el `.jar` al repositorio local (`~/.m2`) para usarlo desde otros proyectos |
| `./mvnw clean` | Borra la carpeta `target/` (compilaciones anteriores) |

*Combinaciones y opciones habituales:*

| Comando | Para qué sirve |
|---|---|
| `./mvnw clean compile` | Compilar desde cero, sin empaquetar |
| `./mvnw clean package` | Compilar desde cero, probar y empaquetar (el más usado) |
| `./mvnw clean package -DskipTests` | Empaquetar **sin ejecutar** las pruebas (compila las pruebas pero no las corre) |
| `./mvnw clean package -Dmaven.test.skip=true` | Empaquetar sin compilar ni ejecutar pruebas |
| `./mvnw clean package -U` | Forzar la descarga de dependencias y actualizaciones |
| `./mvnw clean package -o` | Modo sin conexión (*offline*): usa solo lo ya descargado en `~/.m2` |
| `./mvnw test -Dtest=NombreDeLaClaseTest` | Ejecuta una sola clase de pruebas |
| `./mvnw -q clean package` | Salida silenciosa (*quiet*): solo errores |
| `./mvnw -X clean package` | Salida de depuración (*debug*) para diagnosticar fallos |
| `./mvnw -v` | Muestra la versión de Maven, de Java y del sistema operativo |

*Objetivos (goals) de plugins, con formato `plugin:objetivo`:*

| Comando | Para qué sirve |
|---|---|
| `./mvnw spring-boot:run` | Arranca la aplicación Spring Boot directamente desde el código (desarrollo) |
| `./mvnw dependency:tree` | Muestra el árbol de dependencias (útil ante conflictos de versiones) |
| `./mvnw dependency:resolve` | Descarga y lista las dependencias del proyecto |
| `./mvnw help:effective-pom` | Muestra el `pom.xml` final, con lo heredado de `spring-boot-starter-parent` |
| `./mvnw versions:display-dependency-updates` | Lista las dependencias que tienen una versión más nueva |

> 💡 **Los `-D` son propiedades**: `-DskipTests` equivale a `-DskipTests=true`. Se pueden
> encadenar varios objetivos en una línea: `./mvnw clean test spring-boot:run`.
> En **PowerShell** escribí `.\mvnw.cmd`; si Maven está instalado en el equipo se puede usar
> directamente `mvn clean package` en cualquiera de las tres consolas.

**4. Ejecutar la aplicación**

Dos formas equivalentes:

```bash
# Bash · con Maven (desarrollo)
./mvnw spring-boot:run

# Bash · con el jar generado
java -jar target/academia-0.0.1-SNAPSHOT.jar
```

```bat
:: CMD · con Maven (desarrollo)
mvnw.cmd spring-boot:run

:: CMD · con el jar generado
java -jar target\academia-0.0.1-SNAPSHOT.jar
```

```powershell
# PowerShell · con Maven (desarrollo)
.\mvnw.cmd spring-boot:run

# PowerShell · con el jar generado
java -jar target\academia-0.0.1-SNAPSHOT.jar
```

La aplicación se detiene con `Ctrl + C`. Si el puerto 8080 está ocupado, se cambia así:

```bash
# Bash
java -jar target/academia-0.0.1-SNAPSHOT.jar --server.port=8081
# o, con Maven
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

```bat
:: CMD
java -jar target\academia-0.0.1-SNAPSHOT.jar --server.port=8081
```

```powershell
# PowerShell
java -jar target\academia-0.0.1-SNAPSHOT.jar --server.port=8081
```

> ⚠️ **Trampas habituales.** (1) En Linux, si aparece `Permission denied` al ejecutar
> `./mvnw`, falta `chmod +x mvnw`. (2) El nombre del jar depende de `artifactId` y `version`
> del `pom.xml`: si no coincide con el de arriba, se lista con `ls target/*.jar` (Linux) o
> `dir target\*.jar` (Windows). (3) En **PowerShell**, `curl` es un alias de
> `Invoke-WebRequest` y no acepta las opciones de los ejemplos de esta guía: se usa
> **`curl.exe`**, o directamente Postman. Además, el carácter `\` de continuación de línea
> de Bash en PowerShell es el acento grave (`` ` ``) y en CMD es el acento circunflejo (`^`).

✅ **Punto de control 0:** `./mvnw spring-boot:run` (o `mvnw.cmd spring-boot:run` en Windows)
arranca y muestra `Started AcademiaApplication in … seconds`. Todavía no hay endpoints.

</details>

</details>

---

<details>
<summary><strong>👤 Paso 1 — Fase 1: Estudiantes</strong></summary>

**Endpoints:** `POST /estudiantes` · `GET /estudiantes` · `GET /estudiantes/{id}`

Se construye **de abajo hacia arriba**: primero el dato, luego quien lo guarda, luego la regla
y al final la puerta HTTP.

<details>
<summary><strong>Paso 1.1 · Modelo — capa de negocio</strong></summary>


**Archivo:** `src/main/java/com/academia/negocio/model/Estudiante.java`

```java
package com.academia.negocio.model;

public class Estudiante {

    private Long id;
    private String nombre;
    private int edad;
    private String correo;

    public Estudiante(String nombre, int edad, String correo) {
        this.nombre = nombre;
        this.edad = edad;
        this.correo = correo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public int getEdad() { return edad; }
    public String getCorreo() { return correo; }
}
```

> 🔎 El `id` **no** se recibe en el constructor: lo asigna el repositorio. Los getters existen
> porque Jackson los usa para convertir el objeto en JSON.

</details>

<details>
<summary><strong>Paso 1.2 · Contrato del repositorio — capa de persistencia</strong></summary>


**Archivo:** `src/main/java/com/academia/persistence/repository/RepositorioEstudiantes.java`

```java
package com.academia.persistence.repository;

import com.academia.negocio.model.Estudiante;
import java.util.List;
import java.util.Optional;

public interface RepositorioEstudiantes {

    Estudiante guardar(Estudiante estudiante);

    List<Estudiante> listar();

    Optional<Estudiante> buscarPorId(Long id);

    boolean existePorCorreo(String correo);
}
```

> 🔎 **Concepto (M2):** el servicio dependerá de **esta interfaz**, no de la lista. Es lo que
> permitirá cambiar el almacenamiento sin tocar las reglas.

</details>

<details>
<summary><strong>Paso 1.3 · Implementación en memoria</strong></summary>


**Archivo:** `src/main/java/com/academia/persistence/repository/RepositorioEstudiantesEnMemoria.java`

```java
package com.academia.persistence.repository;

import com.academia.negocio.model.Estudiante;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioEstudiantesEnMemoria implements RepositorioEstudiantes {

    private final List<Estudiante> estudiantes = new ArrayList<>();
    private long siguienteId = 1;

    @Override
    public Estudiante guardar(Estudiante estudiante) {
        estudiante.setId(siguienteId++);
        estudiantes.add(estudiante);
        return estudiante;
    }

    @Override
    public List<Estudiante> listar() {
        return new ArrayList<>(estudiantes);
    }

    @Override
    public Optional<Estudiante> buscarPorId(Long id) {
        for (Estudiante estudiante : estudiantes) {
            if (estudiante.getId().equals(id)) {
                return Optional.of(estudiante);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existePorCorreo(String correo) {
        for (Estudiante estudiante : estudiantes) {
            if (estudiante.getCorreo().equalsIgnoreCase(correo)) {
                return true;
            }
        }
        return false;
    }
}
```

Puntos a resaltar:

- **`@Repository`** es un estereotipo de `@Component` (M2): le dice a Spring «creá un bean de
  esta clase». Nadie escribe `new RepositorioEstudiantesEnMemoria()`.
- **Singleton (M2):** Spring crea **una sola instancia**; por eso `estudiantes` y
  `siguienteId` conservan su valor entre peticiones. Si el bean se creara por petición, la
  lista siempre estaría vacía.
- `listar()` devuelve una **copia**: nadie fuera del repositorio puede modificar la lista
  interna.
- `buscarPorId` y `existePorCorreo` usan un **ciclo `for`** y un **condicional**: es la
  lógica de programación que el taller quiere practicar. `Optional` evita devolver `null`.

</details>

<details>
<summary><strong>Paso 1.4 · Servicio con las reglas — capa de negocio</strong></summary>


**Archivo:** `src/main/java/com/academia/negocio/service/ServicioEstudiantes.java`

```java
package com.academia.negocio.service;

import com.academia.negocio.model.Estudiante;
import com.academia.persistence.repository.RepositorioEstudiantes;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServicioEstudiantes {

    private final RepositorioEstudiantes repositorio;

    public ServicioEstudiantes(RepositorioEstudiantes repositorio) {
        this.repositorio = repositorio;
    }

    public Estudiante registrar(Estudiante estudiante) {
        if (repositorio.existePorCorreo(estudiante.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un estudiante con el correo " + estudiante.getCorreo());
        }
        return repositorio.guardar(estudiante);
    }

    public List<Estudiante> listar() {
        return repositorio.listar();
    }

    public Estudiante obtener(Long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el estudiante " + id));
    }
}
```

> 🔎 **Errores con `ResponseStatusException`.** Spring ya trae una excepción que lleva el
> código HTTP: `new ResponseStatusException(HttpStatus.CONFLICT, "mensaje")` responde `409`
> sin escribir clases propias ni manejador. Se usa `NOT_FOUND` (`404`), `BAD_REQUEST` (`400`) y
> `CONFLICT` (`409`). Para que el cuerpo JSON incluya el campo `message`, se agrega en
> `src/main/resources/application.properties`: `server.error.include-message=always`.

> 🔎 **Concepto (M2) — el momento central de la sesión.** El servicio **declara** lo que
> necesita (un constructor con `RepositorioEstudiantes`) y Spring **se lo entrega**. Eso es
> inyección por constructor: el campo es `final`, no hay `@Autowired` y la clase se puede
> probar pasándole cualquier implementación. Pregunta: *¿quién llama a este constructor?*
> *(El contenedor IoC, no nosotros.)*

</details>

<details>
<summary><strong>Paso 1.5 · Controlador — capa de presentación</strong></summary>


**Archivo:** `src/main/java/com/academia/presentacion/controlador/ControladorEstudiantes.java`

```java
package com.academia.presentacion.controlador;

import com.academia.negocio.model.Estudiante;
import com.academia.negocio.service.ServicioEstudiantes;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/estudiantes")
public class ControladorEstudiantes {

    private final ServicioEstudiantes servicio;

    public ControladorEstudiantes(ServicioEstudiantes servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Estudiante registrar(@RequestBody Estudiante estudiante) {
        return servicio.registrar(estudiante);
    }

    @GetMapping
    public List<Estudiante> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Estudiante obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }
}
```

Puntos a resaltar:

- `@RequestBody` convierte el JSON de la petición en un objeto `Estudiante` (Jackson usa el
  constructor y los nombres de sus parámetros). Si el cliente enviara un `id`, el repositorio
  lo **sobrescribe**: lo decide el sistema.
- El controlador solo **traduce**: lee el JSON, llama al servicio, devuelve el resultado. No
  tiene ningún `if` de negocio.
- Se encadenan tres beans: `ControladorEstudiantes` ← `ServicioEstudiantes` ←
  `RepositorioEstudiantesEnMemoria`. Spring los crea en el orden correcto.

</details>

<details>
<summary><strong>✅ Punto de control 1</strong></summary>


Reiniciar y probar con Postman o `curl`:

```bash
curl -i -X POST localhost:8080/estudiantes -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Torres","edad":22,"correo":"ana@correo.com"}'
# 201 Created  {"id":1,"nombre":"Ana Torres","edad":22,"correo":"ana@correo.com"}

curl -i -X POST localhost:8080/estudiantes -H "Content-Type: application/json" \
  -d '{"nombre":"Otra Ana","edad":30,"correo":"ANA@correo.com"}'
# 409 Conflict  {"status":409,"error":"Conflict","message":"Ya existe un estudiante con el correo ANA@correo.com",…}

curl -i localhost:8080/estudiantes/99
# 404 Not Found {"status":404,"error":"Not Found","message":"No existe el estudiante 99",…}
```

> ⚠️ **Error frecuente:** reiniciar la aplicación **borra los datos** (viven en memoria). No es
> un fallo: es la consecuencia de no tener base de datos, y lo que motivará el Módulo 3.

</details>

</details>

---

<details>
<summary><strong>📚 Paso 2 — Fase 2: Cursos</strong></summary>

**Endpoints:** `POST /cursos` · `GET /cursos` · `GET /cursos/economicos`

Se repite el patrón de la Fase 1. El docente solo proyecta el cuadro de pasos; los
estudiantes escriben. Se entrega el código de referencia para cotejar.

| # | Paso | Clase | Capa |
|:-:|---|---|---|
| 2.1 | Modelo | `Curso` | negocio |
| 2.2 | Contrato | `RepositorioCursos` | persistencia |
| 2.3 | Implementación | `RepositorioCursosEnMemoria` | persistencia |
| 2.4 | Reglas | `ServicioCursos` | negocio |
| 2.5 | API | `ControladorCursos` | presentación |

<details>
<summary><strong>Paso 2.1 · Modelo</strong></summary>


**Archivo:** `src/main/java/com/academia/negocio/model/Curso.java`

```java
package com.academia.negocio.model;

public class Curso {

    private Long id;
    private String nombre;
    private double precio;
    private int cupoMaximo;

    public Curso(String nombre, double precio, int cupoMaximo) {
        this.nombre = nombre;
        this.precio = precio;
        this.cupoMaximo = cupoMaximo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getCupoMaximo() { return cupoMaximo; }
}
```

</details>

<details>
<summary><strong>Paso 2.2 y 2.3 · Repositorio (interfaz e implementación)</strong></summary>


**Archivo:** `src/main/java/com/academia/persistence/repository/RepositorioCursos.java`

```java
package com.academia.persistence.repository;

import com.academia.negocio.model.Curso;
import java.util.List;
import java.util.Optional;

public interface RepositorioCursos {

    Curso guardar(Curso curso);

    List<Curso> listar();

    Optional<Curso> buscarPorId(Long id);
}
```

**Archivo:** `src/main/java/com/academia/persistence/repository/RepositorioCursosEnMemoria.java`

```java
package com.academia.persistence.repository;

import com.academia.negocio.model.Curso;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioCursosEnMemoria implements RepositorioCursos {

    private final List<Curso> cursos = new ArrayList<>();
    private long siguienteId = 1;

    @Override
    public Curso guardar(Curso curso) {
        curso.setId(siguienteId++);
        cursos.add(curso);
        return curso;
    }

    @Override
    public List<Curso> listar() {
        return new ArrayList<>(cursos);
    }

    @Override
    public Optional<Curso> buscarPorId(Long id) {
        for (Curso curso : cursos) {
            if (curso.getId().equals(id)) {
                return Optional.of(curso);
            }
        }
        return Optional.empty();
    }
}
```

</details>

<details>
<summary><strong>Paso 2.4 · Servicio: validaciones y filtro</strong></summary>


Aquí se practican los **operadores lógicos** y el **filtro con ciclo**. De momento el
servicio solo necesita `RepositorioCursos`; el reporte de la Fase 4 agregará lo demás.

**Archivo:** `src/main/java/com/academia/negocio/service/ServicioCursos.java`

```java
package com.academia.negocio.service;

import com.academia.negocio.model.Curso;
import com.academia.persistence.repository.RepositorioCursos;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServicioCursos {

    private static final double PRECIO_ECONOMICO = 200_000;

    private final RepositorioCursos repositorio;

    public ServicioCursos(RepositorioCursos repositorio) {
        this.repositorio = repositorio;
    }

    public Curso registrar(Curso curso) {
        if (curso.getPrecio() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor que cero");
        }
        if (curso.getCupoMaximo() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cupo máximo debe ser mayor que cero");
        }
        return repositorio.guardar(curso);
    }

    public List<Curso> listar() {
        return repositorio.listar();
    }

    public List<Curso> listarEconomicos() {
        List<Curso> economicos = new ArrayList<>();
        for (Curso curso : repositorio.listar()) {
            if (curso.getPrecio() < PRECIO_ECONOMICO) {
                economicos.add(curso);
            }
        }
        return economicos;
    }

    public Curso obtener(Long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el curso " + id));
    }
}
```

> 🗣️ **Pregunta:** ¿por qué `PRECIO_ECONOMICO` es una constante y no un `200000` suelto en el
> `if`? *(Tiene nombre, se cambia en un solo lugar y documenta la regla.)*
>
> 💡 **Reto opcional para quien termine antes:** reescribir `listarEconomicos()` con un
> `stream().filter(...)` (ejemplo de Java moderno del M1).

</details>

<details>
<summary><strong>Paso 2.5 · Controlador</strong></summary>


**Archivo:** `src/main/java/com/academia/presentacion/controlador/ControladorCursos.java`

```java
package com.academia.presentacion.controlador;

import com.academia.negocio.model.Curso;
import com.academia.negocio.service.ServicioCursos;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cursos")
public class ControladorCursos {

    private final ServicioCursos servicio;

    public ControladorCursos(ServicioCursos servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Curso registrar(@RequestBody Curso curso) {
        return servicio.registrar(curso);
    }

    @GetMapping
    public List<Curso> listar() {
        return servicio.listar();
    }

    @GetMapping("/economicos")
    public List<Curso> listarEconomicos() {
        return servicio.listarEconomicos();
    }
}
```

</details>

<details>
<summary><strong>✅ Punto de control 2</strong></summary>


```bash
curl -i -X POST localhost:8080/cursos -H "Content-Type: application/json" \
  -d '{"nombre":"Java desde cero","precio":150000,"cupoMaximo":3}'      # 201
curl -i -X POST localhost:8080/cursos -H "Content-Type: application/json" \
  -d '{"nombre":"Spring Boot","precio":450000,"cupoMaximo":10}'         # 201
curl -i -X POST localhost:8080/cursos -H "Content-Type: application/json" \
  -d '{"nombre":"Roto","precio":0,"cupoMaximo":5}'                      # 400
curl localhost:8080/cursos/economicos                                    # solo "Java desde cero"
```

</details>

</details>

---

<details>
<summary><strong>🎓 Paso 3 — Fase 3: Matrículas</strong></summary>

**Endpoint:** `POST /matriculas` con `{"estudianteId":1,"cursoId":1}`

Es la fase de mayor peso en la evaluación (35 %) y la que muestra **por qué existen las
capas**: la regla involucra tres entidades y ninguna capa la hace sola.

<details>
<summary><strong>Paso 3.1 · Dibujar las reglas antes de programar</strong></summary>


Se pide al grupo ordenar los controles. El orden importa porque define **qué error ve el
cliente primero**:

```text
POST /matriculas
   │
   ├─ 1. ¿Existe el estudiante?     no ─▶ 404
   ├─ 2. ¿Existe el curso?          no ─▶ 404
   ├─ 3. ¿Ya está matriculado?      sí ─▶ 409
   ├─ 4. ¿Quedan cupos?             no ─▶ 409
   └─ 5. Registrar la matrícula     ───▶ 201
```

</details>

<details>
<summary><strong>Paso 3.2 · Modelo y repositorio</strong></summary>


Una matrícula **no guarda objetos**, guarda **identificadores**: es la forma de relacionar
entidades sin base de datos.

**Archivo:** `src/main/java/com/academia/negocio/model/Matricula.java`

```java
package com.academia.negocio.model;

public class Matricula {

    private Long id;
    private Long estudianteId;
    private Long cursoId;

    public Matricula(Long estudianteId, Long cursoId) {
        this.estudianteId = estudianteId;
        this.cursoId = cursoId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEstudianteId() { return estudianteId; }
    public Long getCursoId() { return cursoId; }
}
```

**Archivo:** `src/main/java/com/academia/persistence/repository/RepositorioMatriculas.java`

```java
package com.academia.persistence.repository;

import com.academia.negocio.model.Matricula;

public interface RepositorioMatriculas {

    Matricula guardar(Matricula matricula);

    boolean existe(Long estudianteId, Long cursoId);

    int contarPorCurso(Long cursoId);
}
```

**Archivo:** `src/main/java/com/academia/persistence/repository/RepositorioMatriculasEnMemoria.java`

```java
package com.academia.persistence.repository;

import com.academia.negocio.model.Matricula;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioMatriculasEnMemoria implements RepositorioMatriculas {

    private final List<Matricula> matriculas = new ArrayList<>();
    private long siguienteId = 1;

    @Override
    public Matricula guardar(Matricula matricula) {
        matricula.setId(siguienteId++);
        matriculas.add(matricula);
        return matricula;
    }

    @Override
    public boolean existe(Long estudianteId, Long cursoId) {
        for (Matricula matricula : matriculas) {
            if (matricula.getEstudianteId().equals(estudianteId)
                    && matricula.getCursoId().equals(cursoId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int contarPorCurso(Long cursoId) {
        int contador = 0;
        for (Matricula matricula : matriculas) {
            if (matricula.getCursoId().equals(cursoId)) {
                contador++;
            }
        }
        return contador;
    }
}
```

> 🔎 Aquí aparecen el **contador** y el operador **`&&`**: ambos conceptos del enunciado.
> Se compara con `.equals()` y no con `==` porque `Long` es un objeto (error clásico: `==`
> funciona con ids pequeños y falla con ids mayores de 127).

</details>

<details>
<summary><strong>Paso 3.3 · Servicio de matrículas</strong></summary>


El servicio **reutiliza** `ServicioEstudiantes` y `ServicioCursos` (que ya lanzan el 404) en
lugar de repetir la búsqueda.

**Archivo:** `src/main/java/com/academia/negocio/service/ServicioMatriculas.java`

```java
package com.academia.negocio.service;

import com.academia.negocio.model.Curso;
import com.academia.negocio.model.Matricula;
import com.academia.persistence.repository.RepositorioMatriculas;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServicioMatriculas {

    private final RepositorioMatriculas repositorio;
    private final ServicioEstudiantes servicioEstudiantes;
    private final ServicioCursos servicioCursos;

    public ServicioMatriculas(RepositorioMatriculas repositorio,
                              ServicioEstudiantes servicioEstudiantes,
                              ServicioCursos servicioCursos) {
        this.repositorio = repositorio;
        this.servicioEstudiantes = servicioEstudiantes;
        this.servicioCursos = servicioCursos;
    }

    public Matricula matricular(Matricula matricula) {
        Long estudianteId = matricula.getEstudianteId();
        Long cursoId = matricula.getCursoId();
        servicioEstudiantes.obtener(estudianteId);
        Curso curso = servicioCursos.obtener(cursoId);

        if (repositorio.existe(estudianteId, cursoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El estudiante " + estudianteId + " ya está matriculado en el curso " + cursoId);
        }
        if (repositorio.contarPorCurso(cursoId) >= curso.getCupoMaximo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El curso " + cursoId + " no tiene cupos disponibles");
        }
        return repositorio.guardar(matricula);
    }
}
```

> 🔎 **Concepto (M2):** este bean tiene **tres dependencias** y Spring las resuelve todas por
> constructor. Para que los estudiantes lo vean, se les pide dibujar el grafo:
>
> ```text
> ServicioMatriculas ─▶ RepositorioMatriculas
>                    ─▶ ServicioEstudiantes ─▶ RepositorioEstudiantes
>                    ─▶ ServicioCursos      ─▶ RepositorioCursos
> ```
>
> No hay flechas de vuelta: **no hay dependencia circular**. Pregunta: *¿qué pasaría si
> `ServicioCursos` necesitara a `ServicioMatriculas` (para el reporte de la Fase 4)?* *(Ciclo:
> la aplicación no arranca. Por eso en la Fase 4 `ServicioCursos` usará el **repositorio** de
> matrículas, no el servicio.)*

</details>

<details>
<summary><strong>Paso 3.4 · Controlador</strong></summary>


**Archivo:** `src/main/java/com/academia/presentacion/controlador/ControladorMatriculas.java`

```java
package com.academia.presentacion.controlador;

import com.academia.negocio.model.Matricula;
import com.academia.negocio.service.ServicioMatriculas;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matriculas")
public class ControladorMatriculas {

    private final ServicioMatriculas servicio;

    public ControladorMatriculas(ServicioMatriculas servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Matricula matricular(@RequestBody Matricula matricula) {
        return servicio.matricular(matricula);
    }
}
```

</details>

<details>
<summary><strong>✅ Punto de control 3</strong></summary>


Con el curso de 3 cupos (`id` 1) y dos estudiantes registrados:

| Petición | Resultado esperado |
|---|---|
| `{"estudianteId":1,"cursoId":1}` | `201` |
| `{"estudianteId":1,"cursoId":1}` (repetida) | `409` ya matriculado |
| `{"estudianteId":99,"cursoId":1}` | `404` no existe el estudiante |
| `{"estudianteId":1,"cursoId":99}` | `404` no existe el curso |
| Matricular a un 3.º y un 4.º estudiante en el curso 1 | el 3.º `201`; el 4.º `409` sin cupos |

</details>

</details>

---

<details>
<summary><strong>📊 Paso 4 — Fase 4: Reporte de ocupación</strong></summary>

**Endpoint:** `GET /cursos/{id}/ocupacion`

> ⏳ Si el grupo va atrasado, esta fase se deja como **reto final** (lo permite el
> enunciado) y se pasa directamente a las pruebas.

<details>
<summary><strong>Paso 4.1 · Modelo del resultado — negocio</strong></summary>


**Archivo:** `src/main/java/com/academia/negocio/model/Ocupacion.java`

```java
package com.academia.negocio.model;

public record Ocupacion(String curso, int cupoMaximo, int matriculados,
                        int cuposDisponibles, double porcentajeOcupacion) {
}
```

</details>

<details>
<summary><strong>Paso 4.2 · Cálculo en el servicio</strong></summary>


`ServicioCursos` incorpora una segunda dependencia: el **repositorio** de matrículas. Se
modifican el constructor y se agrega el método.

**Archivo:** `src/main/java/com/academia/negocio/service/ServicioCursos.java` *(cambios)*

```java
package com.academia.negocio.service;

import com.academia.negocio.model.Curso;
import com.academia.negocio.model.Ocupacion;
import com.academia.persistence.repository.RepositorioCursos;
import com.academia.persistence.repository.RepositorioMatriculas;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServicioCursos {

    private static final double PRECIO_ECONOMICO = 200_000;

    private final RepositorioCursos repositorio;
    private final RepositorioMatriculas repositorioMatriculas;

    public ServicioCursos(RepositorioCursos repositorio, RepositorioMatriculas repositorioMatriculas) {
        this.repositorio = repositorio;
        this.repositorioMatriculas = repositorioMatriculas;
    }

    public Curso registrar(Curso curso) {
        if (curso.getPrecio() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor que cero");
        }
        if (curso.getCupoMaximo() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cupo máximo debe ser mayor que cero");
        }
        return repositorio.guardar(curso);
    }

    public List<Curso> listar() {
        return repositorio.listar();
    }

    public List<Curso> listarEconomicos() {
        List<Curso> economicos = new ArrayList<>();
        for (Curso curso : repositorio.listar()) {
            if (curso.getPrecio() < PRECIO_ECONOMICO) {
                economicos.add(curso);
            }
        }
        return economicos;
    }

    public Curso obtener(Long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el curso " + id));
    }

    public Ocupacion calcularOcupacion(Long id) {
        Curso curso = obtener(id);
        int matriculados = repositorioMatriculas.contarPorCurso(id);
        int disponibles = curso.getCupoMaximo() - matriculados;
        double porcentaje = (double) matriculados / curso.getCupoMaximo() * 100;
        porcentaje = Math.round(porcentaje * 100.0) / 100.0;
        return new Ocupacion(curso.getNombre(), curso.getCupoMaximo(), matriculados, disponibles, porcentaje);
    }
}
```

> ⚠️ **Error frecuente (matemático):** `matriculados / cupoMaximo` con dos `int` es una
> **división entera** (`2 / 3 = 0`). Hay que convertir uno a `double` **antes** de dividir:
> `(double) matriculados / cupoMaximo`. Se muestra el error en vivo con 2 de 3 cupos: el
> reporte daría `0.0 %` en lugar de `66.67 %`.

</details>

<details>
<summary><strong>Paso 4.3 · Endpoint — presentación</strong></summary>


Se agrega un método a `ControladorCursos` (más el `import` de `Ocupacion` y de
`PathVariable`):

```java
    @GetMapping("/{id}/ocupacion")
    public Ocupacion ocupacion(@PathVariable Long id) {
        return servicio.calcularOcupacion(id);
    }
```

</details>

<details>
<summary><strong>✅ Punto de control 4</strong></summary>


Con 2 matriculados en el curso de 3 cupos, `GET /cursos/1/ocupacion` devuelve:

```json
{
  "curso": "Java desde cero",
  "cupoMaximo": 3,
  "matriculados": 2,
  "cuposDisponibles": 1,
  "porcentajeOcupacion": 66.67
}
```

> 🔎 **A mencionar (sin hacerlo):** por ahora los controladores reciben y devuelven las
> entidades directamente, sin DTO. Más adelante se separan los datos de la API de los del
> dominio.

</details>

</details>

---

<details>
<summary><strong>🧪 Paso 5 — Pruebas y cierre</strong></summary>

<details>
<summary><strong>Escenarios en Postman</strong></summary>


Crear una colección **«Academia»** y ejecutar en este orden (reiniciar la aplicación antes
para partir con las listas vacías):

| # | Escenario | Petición | Esperado |
|:-:|---|---|:-:|
| 1 | Registrar estudiante 1 | `POST /estudiantes` Ana | `201` |
| 2 | Registrar estudiante 2 | `POST /estudiantes` Luis | `201` |
| 3 | Correo duplicado | `POST /estudiantes` con el correo de Ana | `409` |
| 4 | Estudiante inexistente | `GET /estudiantes/99` | `404` |
| 5 | Crear curso de 3 cupos | `POST /cursos` | `201` |
| 6 | Curso inválido | `POST /cursos` con `precio: 0` | `400` |
| 7 | Matricular a Ana | `POST /matriculas` | `201` |
| 8 | Matricular a Luis | `POST /matriculas` | `201` |
| 9 | Matrícula repetida | `POST /matriculas` Ana otra vez | `409` |
| 10 | Ocupación | `GET /cursos/1/ocupacion` | `1` cupo disponible, `66.67 %` |

Cada estudiante guarda **capturas** (petición, código de estado y cuerpo) como evidencia.

</details>

<details>
<summary><strong>Cierre — preguntas de síntesis</strong></summary>


1. ¿Qué anotación convierte una clase en bean y cuáles usamos hoy? *(`@RestController`,
   `@Service`, `@Repository`; todas son estereotipos de `@Component`.)*
2. ¿Por qué los datos no se pierden entre dos peticiones, pero sí al reiniciar? *(Bean
   singleton: la lista vive mientras viva el contexto.)*
3. ¿Qué clases cambiarían si pasáramos a una base de datos? *(Solo las de `persistence`.)*
4. ¿Qué diferencia hay entre un `404`, un `400` y un `409`? *(No existe / dato inválido /
   choca con el estado actual.)*

</details>

</details>

---

<details>
<summary><strong>🩺 Errores frecuentes y cómo se detectan</strong></summary>

| Síntoma | Causa probable | Solución |
|---|---|---|
| `404` en todas las rutas (página «Whitelabel») | Una clase quedó **fuera** de `com.academia` y el escaneo no la ve | Moverla al árbol de paquetes de `AcademiaApplication` |
| `No qualifying bean of type …` al arrancar | Falta `@Repository` / `@Service` | Anotar la clase |
| `UnsatisfiedDependencyException … circular` | `ServicioCursos` y `ServicioMatriculas` se inyectan mutuamente | `ServicioCursos` usa `RepositorioMatriculas` |
| `415 Unsupported Media Type` | Falta el encabezado `Content-Type: application/json` | Agregarlo en Postman |
| Los campos llegan en `0` / `null` | Nombres del JSON distintos a los del `record` | Igualarlos (`cupoMaximo`, no `cupo_maximo`) |
| `66.67 %` sale como `0.0` | División entera | `(double) matriculados / cupoMaximo` |
| `Port 8080 was already in use` | Otra ejecución sigue abierta | Detenerla o cambiar `server.port` |

</details>

<details>
<summary><strong>🏁 Retos finales (para quien termine antes)</strong></summary>

1. **Sincronización:** hacer `guardar` de los repositorios `synchronized` y explicar qué
   problema evita (dos peticiones simultáneas con el mismo `siguienteId`).
2. **Más consultas:** `GET /cursos/{id}` y `GET /estudiantes/{id}/matriculas`.
3. **Filtro con streams:** reescribir `listarEconomicos()` y `existePorCorreo()` con
   `stream()`.
4. **Adelanto de DTO:** crear `SolicitudCurso` (un `record`) y convertirlo a `Curso` en el controlador, para que el cliente nunca pueda enviar un `id`.
5. **Cambio de almacenamiento:** crear `RepositorioEstudiantesAlternativo` (por ejemplo, con un
   `Map<Long, Estudiante>`), anotar solo una de las dos con `@Repository` y comprobar que el
   servicio **no cambia ni una línea**.

</details>

## 📁 Estructura final esperada

```text
📁 academia
├── 📄 pom.xml
└── 📁 src/main/java/com/academia
    ├── 📄 AcademiaApplication.java
    ├── 📁 presentacion
    │   └── 📁 controlador   ControladorEstudiantes · ControladorCursos · ControladorMatriculas
    ├── 📁 negocio
    │   ├── 📁 model         Estudiante · Curso · Matricula · Ocupacion
    │   ├── 📁 service       ServicioEstudiantes · ServicioCursos · ServicioMatriculas
    └── 📁 persistence
        └── 📁 repository    3 interfaces · 3 implementaciones «EnMemoria»
```
