# 🧭 Guía paso a paso — Taller «Gestión de cursos» (1 h 30 min)

> 👩‍🏫 **Material docente.** Guía para resolver el taller **junto con los estudiantes**, en
> vivo, aplicando lo visto en el [Módulo 01](../01-introduccion-a-spring-boot/) y el
> [Módulo 02](../02-manejo-de-dependencias-y-java-beans/). Contiene código completo: no se
> entrega a los estudiantes antes de resolver el taller.

## 🎯 Objetivo

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

> ⚠️ **Diferencia con el enunciado original.** El enunciado pide «sin clases Service y toda la
> lógica en el Controller». Esta guía **cambia esa restricción a propósito**: el curso trabaja
> siempre en tres capas, y es justo lo que se aprendió en los módulos 01 y 02. Se mantienen
> las demás restricciones: **sin base de datos ni JPA**, datos en listas en memoria.

## ⏱️ Plan de la sesión

| Tramo | Minutos | Actividad | Quién escribe |
|---|---:|---|---|
| 0 | 0 – 10 | Preparación: proyecto, capas y plano del taller | Docente |
| 1 | 10 – 30 | Fase 1 · Estudiantes | Docente y estudiantes, a la par |
| 2 | 30 – 45 | Fase 2 · Cursos | Estudiantes (docente guía) |
| 3 | 45 – 70 | Fase 3 · Matrículas | Estudiantes (docente guía) |
| 4 | 70 – 80 | Fase 4 · Reporte de ocupación | Estudiantes; es el tramo que se recorta si falta tiempo |
| 5 | 80 – 90 | Pruebas en Postman y cierre | Todos |

> 💡 **Ritmo.** La Fase 1 se hace entera en vivo: allí se fija el patrón (modelo → repositorio →
> servicio → controlador). Las fases 2 y 3 repiten el patrón y son las que los estudiantes
> escriben solos. Las clases de excepciones y el manejador de errores se entregan ya escritos
> (se copian en el minuto 0) para no gastar tiempo.

## 🧠 Conceptos de los módulos 01 y 02 que se resaltan

| Concepto | Dónde aparece en el taller | Módulo | Ejemplo de apoyo |
|---|---|:-:|---|
| Spring Boot y `@SpringBootApplication` | Clase `Main` | 01 | [¿Qué es Spring Boot?](../01-introduccion-a-spring-boot/material/ejemplos/07-que-es-spring-boot.md) |
| Estructura de un proyecto y Maven | `pom.xml`, paquetes por capa | 01 | [Estructura de un proyecto](../01-introduccion-a-spring-boot/material/ejemplos/08-estructura-de-un-proyecto-spring-boot.md) |
| Anotaciones | `@RestController`, `@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestBody` | 01 | [Anotaciones en Spring Boot](../01-introduccion-a-spring-boot/material/ejemplos/09-anotaciones-en-spring-boot.md) |
| Contenedor IoC | Spring crea los objetos; nosotros nunca escribimos `new Servicio…()` | 01 | [IoC y ciclo de vida de un bean](../01-introduccion-a-spring-boot/material/ejemplos/10-ioc-container-ciclo-de-vida-de-un-bean.md) |
| Java moderno | `record` para los DTO, `Optional` en las búsquedas | 01 | [Java moderno](../01-introduccion-a-spring-boot/material/ejemplos/05-java-moderno-streams-lambdas-optional-records.md) |
| Dependencia e inyección | Controlador → Servicio → Repositorio | 02 | [Inyección de dependencias](../02-manejo-de-dependencias-y-java-beans/material/ejemplos/02-inyeccion-de-dependencias.md) |
| Programar contra interfaces | `RepositorioEstudiantes` (interfaz) y `RepositorioEstudiantesEnMemoria` (clase) | 02 | [Resolución de una dependencia](../02-manejo-de-dependencias-y-java-beans/material/ejemplos/04-implementacion-y-resolucion-de-una-dependencia-general.md) |
| Java Bean y `@Component` (y sus estereotipos) | `@Service`, `@Repository` | 02 | [Uso de @Component](../02-manejo-de-dependencias-y-java-beans/material/ejemplos/07-uso-de-component.md) |
| Ciclo de vida: bean **singleton** | La lista vive en el repositorio y **sobrevive entre peticiones** porque hay una sola instancia | 02 | [Ciclo de vida de un bean](../02-manejo-de-dependencias-y-java-beans/material/ejemplos/06-ciclo-de-vida-de-un-bean.md) |
| Dependencia circular | Se menciona al armar `ServicioCursos` (usa el repositorio de matrículas, no el servicio) | 02 | [Explicación conceptual](../02-manejo-de-dependencias-y-java-beans/material/ejemplos/explicacion-conceptual.md) |

## 🏗️ Arquitectura del taller

```text
📁 src/main/java/com/academia
├── 📄 Main.java
├── 📁 presentacion                      ← 1 · lo que ve el cliente HTTP
│   ├── 📁 controlador   ControladorEstudiantes · ControladorCursos · ControladorMatriculas
│   ├── 📁 dto           SolicitudEstudiante · SolicitudCurso · SolicitudMatricula
│   └── 📁 excepcion     ManejadorExcepciones · RespuestaError
├── 📁 negocio                           ← 2 · las reglas
│   ├── 📁 model         Estudiante · Curso · Matricula · Ocupacion
│   ├── 📁 service       ServicioEstudiantes · ServicioCursos · ServicioMatriculas
│   └── 📁 excepcion     RecursoNoEncontradoException · SolicitudInvalidaException · ConflictoException
└── 📁 persistence                       ← 3 · dónde viven los datos (listas en memoria)
    └── 📁 repository    Repositorio… (interfaces) · Repositorio…EnMemoria (@Repository)
```

Regla de dependencia: **presentación → negocio → persistencia**. El controlador nunca toca un
repositorio; el servicio nunca conoce HTTP.

```text
HTTP ─▶ Controlador ─▶ Servicio ─▶ Repositorio (interfaz) ◀─ RepositorioEnMemoria (List)
        valida forma   reglas      contrato                  almacenamiento
```

> 🗣️ **Pregunta para el grupo (1 min):** si mañana cambiamos la lista por una base de datos,
> ¿qué capas habría que tocar? *(Respuesta: solo la de persistencia, porque el servicio depende
> de la interfaz.)*

---

## 🚀 Tramo 0 — Preparación (0 – 10 min)

### Paso 0.1 · Crear el proyecto (3 min)

En [start.spring.io](https://start.spring.io):

| Campo | Valor |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.x (última estable) |
| Group | `com.academia` |
| Artifact | `academia` |
| Packaging / Java | Jar / 17 |
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

### Paso 0.2 · Renombrar la clase principal y crear los paquetes (3 min)

Initializr genera `AcademiaApplication`. Se renombra a `Main` (convención del curso) y se
crean los paquetes vacíos de la sección anterior.

**Archivo:** `src/main/java/com/academia/Main.java`

```java
package com.academia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
```

> 🔎 **Concepto (M1):** `@SpringBootApplication` escanea **su propio paquete y los de abajo**.
> Por eso todo vive bajo `com.academia`; una clase fuera de ese árbol no sería detectada como
> bean.

### Paso 0.3 · Copiar el kit de errores (4 min)

Cuatro clases pequeñas que se entregan hechas. Se explica **qué hacen**, no se escriben.

**Archivo:** `src/main/java/com/academia/negocio/excepcion/RecursoNoEncontradoException.java`

```java
package com.academia.negocio.excepcion;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
```

**Archivo:** `src/main/java/com/academia/negocio/excepcion/SolicitudInvalidaException.java`

```java
package com.academia.negocio.excepcion;

public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
```

**Archivo:** `src/main/java/com/academia/negocio/excepcion/ConflictoException.java`

```java
package com.academia.negocio.excepcion;

public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
```

**Archivo:** `src/main/java/com/academia/presentacion/excepcion/RespuestaError.java`

```java
package com.academia.presentacion.excepcion;

public record RespuestaError(int estado, String mensaje) {
}
```

**Archivo:** `src/main/java/com/academia/presentacion/excepcion/ManejadorExcepciones.java`

```java
package com.academia.presentacion.excepcion;

import com.academia.negocio.excepcion.ConflictoException;
import com.academia.negocio.excepcion.RecursoNoEncontradoException;
import com.academia.negocio.excepcion.SolicitudInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> noEncontrado(RecursoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<RespuestaError> solicitudInvalida(SolicitudInvalidaException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<RespuestaError> conflicto(ConflictoException e) {
        return responder(HttpStatus.CONFLICT, e.getMessage());
    }

    private ResponseEntity<RespuestaError> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(new RespuestaError(estado.value(), mensaje));
    }
}
```

> 🗣️ **Idea clave para decir en voz alta:** el servicio lanza una excepción de *negocio* («no
> existe», «duplicado»); **no sabe qué es un 404**. La capa de presentación traduce a HTTP.
> El manejo completo de excepciones se estudia en el Módulo 7: hoy es un adelanto.

| Excepción | Código HTTP | Cuándo |
|---|:-:|---|
| `RecursoNoEncontradoException` | `404` | El estudiante o curso no existe |
| `SolicitudInvalidaException` | `400` | Precio ≤ 0, cupo ≤ 0 |
| `ConflictoException` | `409` | Correo duplicado, matrícula repetida, sin cupos |

✅ **Punto de control 0:** `./mvnw spring-boot:run` arranca y muestra
`Started Main in … seconds`. Todavía no hay endpoints.

---

## 👤 Tramo 1 — Fase 1: Estudiantes (10 – 30 min)

**Endpoints:** `POST /estudiantes` · `GET /estudiantes` · `GET /estudiantes/{id}`

Se construye **de abajo hacia arriba**: primero el dato, luego quien lo guarda, luego la regla
y al final la puerta HTTP.

### Paso 1.1 · Modelo (3 min) — capa de negocio

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

### Paso 1.2 · Contrato del repositorio (3 min) — capa de persistencia

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

### Paso 1.3 · Implementación en memoria (5 min)

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

### Paso 1.4 · Servicio con las reglas (4 min) — capa de negocio

**Archivo:** `src/main/java/com/academia/negocio/service/ServicioEstudiantes.java`

```java
package com.academia.negocio.service;

import com.academia.negocio.excepcion.ConflictoException;
import com.academia.negocio.excepcion.RecursoNoEncontradoException;
import com.academia.negocio.model.Estudiante;
import com.academia.persistence.repository.RepositorioEstudiantes;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ServicioEstudiantes {

    private final RepositorioEstudiantes repositorio;

    public ServicioEstudiantes(RepositorioEstudiantes repositorio) {
        this.repositorio = repositorio;
    }

    public Estudiante registrar(String nombre, int edad, String correo) {
        if (repositorio.existePorCorreo(correo)) {
            throw new ConflictoException("Ya existe un estudiante con el correo " + correo);
        }
        return repositorio.guardar(new Estudiante(nombre, edad, correo));
    }

    public List<Estudiante> listar() {
        return repositorio.listar();
    }

    public Estudiante obtener(Long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estudiante " + id));
    }
}
```

> 🔎 **Concepto (M2) — el momento central de la sesión.** El servicio **declara** lo que
> necesita (un constructor con `RepositorioEstudiantes`) y Spring **se lo entrega**. Eso es
> inyección por constructor: el campo es `final`, no hay `@Autowired` y la clase se puede
> probar pasándole cualquier implementación. Pregunta: *¿quién llama a este constructor?*
> *(El contenedor IoC, no nosotros.)*

### Paso 1.5 · DTO de solicitud y controlador (5 min) — capa de presentación

**Archivo:** `src/main/java/com/academia/presentacion/dto/SolicitudEstudiante.java`

```java
package com.academia.presentacion.dto;

public record SolicitudEstudiante(String nombre, int edad, String correo) {
}
```

**Archivo:** `src/main/java/com/academia/presentacion/controlador/ControladorEstudiantes.java`

```java
package com.academia.presentacion.controlador;

import com.academia.negocio.model.Estudiante;
import com.academia.negocio.service.ServicioEstudiantes;
import com.academia.presentacion.dto.SolicitudEstudiante;
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
    public Estudiante registrar(@RequestBody SolicitudEstudiante solicitud) {
        return servicio.registrar(solicitud.nombre(), solicitud.edad(), solicitud.correo());
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

- El **DTO** (`record`, Java moderno de M1) define **qué datos acepta la API**. El cliente no
  puede enviar un `id`: lo decide el sistema.
- El controlador solo **traduce**: lee el JSON, llama al servicio, devuelve el resultado. No
  tiene ningún `if` de negocio.
- Se encadenan tres beans: `ControladorEstudiantes` ← `ServicioEstudiantes` ←
  `RepositorioEstudiantesEnMemoria`. Spring los crea en el orden correcto.

### ✅ Punto de control 1 (en vivo, 2 min)

Reiniciar y probar con Postman o `curl`:

```bash
curl -i -X POST localhost:8080/estudiantes -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Torres","edad":22,"correo":"ana@correo.com"}'
# 201 Created  {"id":1,"nombre":"Ana Torres","edad":22,"correo":"ana@correo.com"}

curl -i -X POST localhost:8080/estudiantes -H "Content-Type: application/json" \
  -d '{"nombre":"Otra Ana","edad":30,"correo":"ANA@correo.com"}'
# 409 Conflict  {"estado":409,"mensaje":"Ya existe un estudiante con el correo ANA@correo.com"}

curl -i localhost:8080/estudiantes/99
# 404 Not Found {"estado":404,"mensaje":"No existe el estudiante 99"}
```

> ⚠️ **Error frecuente:** reiniciar la aplicación **borra los datos** (viven en memoria). No es
> un fallo: es la consecuencia de no tener base de datos, y lo que motivará el Módulo 3.

---

## 📚 Tramo 2 — Fase 2: Cursos (30 – 45 min)

**Endpoints:** `POST /cursos` · `GET /cursos` · `GET /cursos/economicos`

Se repite el patrón de la Fase 1. El docente solo proyecta el cuadro de pasos; los
estudiantes escriben. Se entrega el código de referencia para cotejar.

| # | Paso | Clase | Capa |
|:-:|---|---|---|
| 2.1 | Modelo | `Curso` | negocio |
| 2.2 | Contrato | `RepositorioCursos` | persistencia |
| 2.3 | Implementación | `RepositorioCursosEnMemoria` | persistencia |
| 2.4 | Reglas | `ServicioCursos` | negocio |
| 2.5 | API | `SolicitudCurso` + `ControladorCursos` | presentación |

### Paso 2.1 · Modelo

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

### Paso 2.2 y 2.3 · Repositorio (interfaz e implementación)

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

### Paso 2.4 · Servicio: validaciones y filtro

Aquí se practican los **operadores lógicos** y el **filtro con ciclo**. De momento el
servicio solo necesita `RepositorioCursos`; el reporte de la Fase 4 agregará lo demás.

**Archivo:** `src/main/java/com/academia/negocio/service/ServicioCursos.java`

```java
package com.academia.negocio.service;

import com.academia.negocio.excepcion.RecursoNoEncontradoException;
import com.academia.negocio.excepcion.SolicitudInvalidaException;
import com.academia.negocio.model.Curso;
import com.academia.persistence.repository.RepositorioCursos;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ServicioCursos {

    private static final double PRECIO_ECONOMICO = 200_000;

    private final RepositorioCursos repositorio;

    public ServicioCursos(RepositorioCursos repositorio) {
        this.repositorio = repositorio;
    }

    public Curso registrar(String nombre, double precio, int cupoMaximo) {
        if (precio <= 0) {
            throw new SolicitudInvalidaException("El precio debe ser mayor que cero");
        }
        if (cupoMaximo <= 0) {
            throw new SolicitudInvalidaException("El cupo máximo debe ser mayor que cero");
        }
        return repositorio.guardar(new Curso(nombre, precio, cupoMaximo));
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
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el curso " + id));
    }
}
```

> 🗣️ **Pregunta:** ¿por qué `PRECIO_ECONOMICO` es una constante y no un `200000` suelto en el
> `if`? *(Tiene nombre, se cambia en un solo lugar y documenta la regla.)*
>
> 💡 **Reto opcional para quien termine antes:** reescribir `listarEconomicos()` con un
> `stream().filter(...)` (ejemplo de Java moderno del M1).

### Paso 2.5 · DTO y controlador

**Archivo:** `src/main/java/com/academia/presentacion/dto/SolicitudCurso.java`

```java
package com.academia.presentacion.dto;

public record SolicitudCurso(String nombre, double precio, int cupoMaximo) {
}
```

**Archivo:** `src/main/java/com/academia/presentacion/controlador/ControladorCursos.java`

```java
package com.academia.presentacion.controlador;

import com.academia.negocio.model.Curso;
import com.academia.negocio.service.ServicioCursos;
import com.academia.presentacion.dto.SolicitudCurso;
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
    public Curso registrar(@RequestBody SolicitudCurso solicitud) {
        return servicio.registrar(solicitud.nombre(), solicitud.precio(), solicitud.cupoMaximo());
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

### ✅ Punto de control 2

```bash
curl -i -X POST localhost:8080/cursos -H "Content-Type: application/json" \
  -d '{"nombre":"Java desde cero","precio":150000,"cupoMaximo":3}'      # 201
curl -i -X POST localhost:8080/cursos -H "Content-Type: application/json" \
  -d '{"nombre":"Spring Boot","precio":450000,"cupoMaximo":10}'         # 201
curl -i -X POST localhost:8080/cursos -H "Content-Type: application/json" \
  -d '{"nombre":"Roto","precio":0,"cupoMaximo":5}'                      # 400
curl localhost:8080/cursos/economicos                                    # solo "Java desde cero"
```

---

## 🎓 Tramo 3 — Fase 3: Matrículas (45 – 70 min)

**Endpoint:** `POST /matriculas` con `{"estudianteId":1,"cursoId":1}`

Es la fase de mayor peso en la evaluación (35 %) y la que muestra **por qué existen las
capas**: la regla involucra tres entidades y ninguna capa la hace sola.

### Paso 3.1 · Dibujar las reglas antes de programar (3 min)

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

### Paso 3.2 · Modelo y repositorio (7 min)

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

### Paso 3.3 · Servicio de matrículas (10 min)

El servicio **reutiliza** `ServicioEstudiantes` y `ServicioCursos` (que ya lanzan el 404) en
lugar de repetir la búsqueda.

**Archivo:** `src/main/java/com/academia/negocio/service/ServicioMatriculas.java`

```java
package com.academia.negocio.service;

import com.academia.negocio.excepcion.ConflictoException;
import com.academia.negocio.model.Curso;
import com.academia.negocio.model.Matricula;
import com.academia.persistence.repository.RepositorioMatriculas;
import org.springframework.stereotype.Service;

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

    public Matricula matricular(Long estudianteId, Long cursoId) {
        servicioEstudiantes.obtener(estudianteId);
        Curso curso = servicioCursos.obtener(cursoId);

        if (repositorio.existe(estudianteId, cursoId)) {
            throw new ConflictoException(
                    "El estudiante " + estudianteId + " ya está matriculado en el curso " + cursoId);
        }
        if (repositorio.contarPorCurso(cursoId) >= curso.getCupoMaximo()) {
            throw new ConflictoException("El curso " + cursoId + " no tiene cupos disponibles");
        }
        return repositorio.guardar(new Matricula(estudianteId, cursoId));
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

### Paso 3.4 · DTO y controlador (5 min)

**Archivo:** `src/main/java/com/academia/presentacion/dto/SolicitudMatricula.java`

```java
package com.academia.presentacion.dto;

public record SolicitudMatricula(Long estudianteId, Long cursoId) {
}
```

**Archivo:** `src/main/java/com/academia/presentacion/controlador/ControladorMatriculas.java`

```java
package com.academia.presentacion.controlador;

import com.academia.negocio.model.Matricula;
import com.academia.negocio.service.ServicioMatriculas;
import com.academia.presentacion.dto.SolicitudMatricula;
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
    public Matricula matricular(@RequestBody SolicitudMatricula solicitud) {
        return servicio.matricular(solicitud.estudianteId(), solicitud.cursoId());
    }
}
```

### ✅ Punto de control 3 (5 min)

Con el curso de 3 cupos (`id` 1) y dos estudiantes registrados:

| Petición | Resultado esperado |
|---|---|
| `{"estudianteId":1,"cursoId":1}` | `201` |
| `{"estudianteId":1,"cursoId":1}` (repetida) | `409` ya matriculado |
| `{"estudianteId":99,"cursoId":1}` | `404` no existe el estudiante |
| `{"estudianteId":1,"cursoId":99}` | `404` no existe el curso |
| Matricular a un 3.º y un 4.º estudiante en el curso 1 | el 3.º `201`; el 4.º `409` sin cupos |

---

## 📊 Tramo 4 — Fase 4: Reporte de ocupación (70 – 80 min)

**Endpoint:** `GET /cursos/{id}/ocupacion`

> ⏳ Si el grupo va atrasado, esta fase se deja como **reto final** (lo permite el
> enunciado) y se pasa directamente a las pruebas.

### Paso 4.1 · Modelo del resultado (2 min) — negocio

**Archivo:** `src/main/java/com/academia/negocio/model/Ocupacion.java`

```java
package com.academia.negocio.model;

public record Ocupacion(String curso, int cupoMaximo, int matriculados,
                        int cuposDisponibles, double porcentajeOcupacion) {
}
```

### Paso 4.2 · Cálculo en el servicio (5 min)

`ServicioCursos` incorpora una segunda dependencia: el **repositorio** de matrículas. Se
modifican el constructor y se agrega el método.

**Archivo:** `src/main/java/com/academia/negocio/service/ServicioCursos.java` *(cambios)*

```java
package com.academia.negocio.service;

import com.academia.negocio.excepcion.RecursoNoEncontradoException;
import com.academia.negocio.excepcion.SolicitudInvalidaException;
import com.academia.negocio.model.Curso;
import com.academia.negocio.model.Ocupacion;
import com.academia.persistence.repository.RepositorioCursos;
import com.academia.persistence.repository.RepositorioMatriculas;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ServicioCursos {

    private static final double PRECIO_ECONOMICO = 200_000;

    private final RepositorioCursos repositorio;
    private final RepositorioMatriculas repositorioMatriculas;

    public ServicioCursos(RepositorioCursos repositorio, RepositorioMatriculas repositorioMatriculas) {
        this.repositorio = repositorio;
        this.repositorioMatriculas = repositorioMatriculas;
    }

    public Curso registrar(String nombre, double precio, int cupoMaximo) {
        if (precio <= 0) {
            throw new SolicitudInvalidaException("El precio debe ser mayor que cero");
        }
        if (cupoMaximo <= 0) {
            throw new SolicitudInvalidaException("El cupo máximo debe ser mayor que cero");
        }
        return repositorio.guardar(new Curso(nombre, precio, cupoMaximo));
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
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el curso " + id));
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

### Paso 4.3 · Endpoint (3 min) — presentación

Se agrega un método a `ControladorCursos` (más el `import` de `Ocupacion` y de
`PathVariable`):

```java
    @GetMapping("/{id}/ocupacion")
    public Ocupacion ocupacion(@PathVariable Long id) {
        return servicio.calcularOcupacion(id);
    }
```

### ✅ Punto de control 4

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

> 🔎 **Mejora a mencionar (sin hacerla):** el controlador devuelve directamente el modelo
> `Ocupacion` y las entidades. Con DTO de respuesta se desacopla la API del dominio; esa
> práctica se formaliza en los módulos siguientes.

---

## 🧪 Tramo 5 — Pruebas y cierre (80 – 90 min)

### Escenarios en Postman (7 min)

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

### Cierre (3 min) — preguntas de síntesis

1. ¿Qué anotación convierte una clase en bean y cuáles usamos hoy? *(`@RestController`,
   `@Service`, `@Repository`; todas son estereotipos de `@Component`.)*
2. ¿Por qué los datos no se pierden entre dos peticiones, pero sí al reiniciar? *(Bean
   singleton: la lista vive mientras viva el contexto.)*
3. ¿Qué clases cambiarían si pasáramos a una base de datos? *(Solo las de `persistence`.)*
4. ¿Qué diferencia hay entre un `404`, un `400` y un `409`? *(No existe / dato inválido /
   choca con el estado actual.)*

---

## 🩺 Errores frecuentes y cómo se detectan

| Síntoma | Causa probable | Solución |
|---|---|---|
| `404` en todas las rutas (página «Whitelabel») | Una clase quedó **fuera** de `com.academia` y el escaneo no la ve | Moverla al árbol de paquetes de `Main` |
| `No qualifying bean of type …` al arrancar | Falta `@Repository` / `@Service` | Anotar la clase |
| `UnsatisfiedDependencyException … circular` | `ServicioCursos` y `ServicioMatriculas` se inyectan mutuamente | `ServicioCursos` usa `RepositorioMatriculas` |
| `415 Unsupported Media Type` | Falta el encabezado `Content-Type: application/json` | Agregarlo en Postman |
| Los campos llegan en `0` / `null` | Nombres del JSON distintos a los del `record` | Igualarlos (`cupoMaximo`, no `cupo_maximo`) |
| `66.67 %` sale como `0.0` | División entera | `(double) matriculados / cupoMaximo` |
| `Port 8080 was already in use` | Otra ejecución sigue abierta | Detenerla o cambiar `server.port` |

## 📋 Correspondencia con la rúbrica del taller

| Criterio | Peso | Dónde se logra |
|---|--:|---|
| Endpoints de estudiantes y cursos | 30 % | Tramos 1 y 2 |
| Registro y validación de matrículas | 35 % | Tramo 3 |
| Cálculo del reporte de ocupación | 20 % | Tramo 4 |
| Pruebas y manejo de errores | 15 % | Kit de errores (tramo 0) y tramo 5 |

> Con esta guía se añade, además, la **organización en tres capas** como criterio de calidad:
> el controlador sin reglas, el servicio sin HTTP y el repositorio detrás de una interfaz.

## 🏁 Retos finales (para quien termine antes)

1. **Sincronización:** hacer `guardar` de los repositorios `synchronized` y explicar qué
   problema evita (dos peticiones simultáneas con el mismo `siguienteId`).
2. **Más consultas:** `GET /cursos/{id}` y `GET /estudiantes/{id}/matriculas`.
3. **Filtro con streams:** reescribir `listarEconomicos()` y `existePorCorreo()` con
   `stream()`.
4. **DTO de respuesta:** crear `RespuestaCurso` y convertir el modelo en el controlador.
5. **Cambio de almacenamiento:** crear `RepositorioEstudiantesAlternativo` (por ejemplo, con un
   `Map<Long, Estudiante>`), anotar solo una de las dos con `@Repository` y comprobar que el
   servicio **no cambia ni una línea**.

## 📁 Estructura final esperada

```text
📁 academia
├── 📄 pom.xml
└── 📁 src/main/java/com/academia
    ├── 📄 Main.java
    ├── 📁 presentacion
    │   ├── 📁 controlador   ControladorEstudiantes · ControladorCursos · ControladorMatriculas
    │   ├── 📁 dto           SolicitudEstudiante · SolicitudCurso · SolicitudMatricula
    │   └── 📁 excepcion     ManejadorExcepciones · RespuestaError
    ├── 📁 negocio
    │   ├── 📁 model         Estudiante · Curso · Matricula · Ocupacion
    │   ├── 📁 service       ServicioEstudiantes · ServicioCursos · ServicioMatriculas
    │   └── 📁 excepcion     RecursoNoEncontradoException · SolicitudInvalidaException · ConflictoException
    └── 📁 persistence
        └── 📁 repository    3 interfaces · 3 implementaciones «EnMemoria»
```
