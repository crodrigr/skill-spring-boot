# 📝 Examen — CRUD de libros en tres capas

> 📌 **Examen práctico individual.** Este documento contiene **solo el enunciado**: qué construir, con qué
> reglas y cómo se evalúa. No incluye código ni pasos de solución. Se aplica lo visto en el
> [Módulo 01](../../01-introduccion-a-spring-boot/) y el [Módulo 02](../../02-manejo-de-dependencias-y-java-beans/),
> con el mismo patrón del taller «Gestión de cursos».
>
> ⏱️ **Duración:** 60 minutos · 🎯 **Puntaje:** 100 puntos · ✅ **Aprobado:** 60 puntos.
> 📚 **Se permite** consultar los apuntes y las guías del módulo. **No se permite** copiar el trabajo de otra persona.

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Construir **desde cero** una API REST con Spring Boot que consulte el catálogo de una biblioteca,
> organizada en las **tres capas del curso** (presentación, negocio y persistencia), con los datos
> **en memoria** (una lista), sin base de datos ni JPA.
>
> Al terminar, el estudiante demuestra que:
>
> - Crea el proyecto con Spring Initializr y lo compila y ejecuta con `./mvnw`.
> - Separa responsabilidades en tres capas y respeta la regla de dependencia.
> - Declara beans con `@RestController`, `@Service` y `@Repository`, y los conecta con **inyección por constructor** contra una **interfaz**.
> - Expone dos endpoints de consulta con `@GetMapping`, uno con `@PathVariable`.
> - Señala el error «no existe» con una excepción estándar de Java.
> - Prueba la API con `curl` o Postman y conserva la evidencia.

</details>

<details>
<summary><strong>📝 El problema</strong></summary>

> ### 📚 Biblioteca «Palabras»
>
> **Palabras** es una biblioteca de barrio que quiere publicar su catálogo mediante una API REST sencilla.
> Por ahora el catálogo es **fijo**: se carga al arrancar la aplicación y solo se **consulta**.
>
> La API debe permitir:
>
> - **listar** todos los libros del catálogo, y
> - **buscar** un libro por su identificador.
>
> ### 🧩 Entidad
>
> | Entidad | Atributos | Observación |
> |---|---|---|
> | **Libro** | `id` (`Long`), `titulo` (`String`), `autor` (`String`), `anioPublicacion` (`int`) | Los objetos no cambian después de crearse. |
>
> ### 📦 Datos iniciales (catálogo)
>
> El repositorio debe arrancar con **estos cinco libros**, exactamente:
>
> | id | titulo | autor | anioPublicacion |
> |---:|---|---|---:|
> | 1 | Cien años de soledad | Gabriel García Márquez | 1967 |
> | 2 | El amor en los tiempos del cólera | Gabriel García Márquez | 1985 |
> | 3 | La ciudad y los perros | Mario Vargas Llosa | 1963 |
> | 4 | Rayuela | Julio Cortázar | 1963 |
> | 5 | Ficciones | Jorge Luis Borges | 1944 |
>
> > 💡 Los ids se escriben a mano en el catálogo; en este examen **no se crean libros nuevos**.

</details>

<details>
<summary><strong>📋 Requerimientos</strong></summary>

> ### ✅ Funcionales
>
> | Código | Endpoint | Qué debe hacer | Respuesta esperada |
> |---|---|---|---|
> | **RF-01** | `GET /libros` | Devuelve **todos** los libros del catálogo, en el orden de la tabla. | `200` y un arreglo JSON con 5 libros |
> | **RF-02** | `GET /libros/{id}` | Devuelve el libro cuyo `id` coincide. | `200` y un objeto JSON |
> | **RF-03** | `GET /libros/{id}` con un `id` que **no existe** | La capa de **negocio** lanza `NoSuchElementException` con el mensaje `No existe el libro {id}`. | `500` (en este módulo todavía no se traducen las excepciones a códigos HTTP) |
>
> ### 🛠️ No funcionales
>
> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Java 21 (o superior), Maven y **una sola** dependencia funcional: *Spring Web*. Group `com.biblioteca`, Artifact `biblioteca`. |
> | **RNF-02** | Tres capas, cada clase en su paquete y con **estos nombres** (ver «Estructura esperada»). |
> | **RNF-03** | **Regla de dependencia:** presentación → negocio → persistencia. El controlador **no** conoce el repositorio, y el servicio **no** importa nada de presentación. |
> | **RNF-04** | El servicio depende de la **interfaz** `RepositorioLibros`, no de la implementación. |
> | **RNF-05** | **Inyección por constructor.** No se usa `@Autowired` ni `new` para crear servicios o repositorios. |
> | **RNF-06** | Sin base de datos, sin JPA, sin Lombok. Los datos viven en una `List<Libro>` dentro del repositorio. |
> | **RNF-07** | `listar()` del repositorio devuelve una **copia** de la lista, para que nadie de afuera la modifique. |
> | **RNF-08** | Nombres del código en **español**, como en el taller. |

</details>

<details>
<summary><strong>🏗️ Estructura esperada</strong></summary>

> ```text
> 📁 biblioteca
> ├── 📄 pom.xml
> └── 📁 src/main/java/com/biblioteca
>     ├── 📄 BibliotecaApplication.java
>     ├── 📁 presentacion                      ← 1 · lo que ve el cliente HTTP
>     │   └── 📁 controlador
>     │       └── 📄 ControladorLibros.java
>     ├── 📁 negocio                           ← 2 · las reglas
>     │   ├── 📁 model
>     │   │   └── 📄 Libro.java
>     │   └── 📁 service
>     │       └── 📄 ServicioLibros.java
>     └── 📁 persistence                       ← 3 · dónde viven los datos (lista en memoria)
>         └── 📁 repository
>             ├── 📄 RepositorioLibros.java              (interfaz)
>             └── 📄 RepositorioLibrosEnMemoria.java     (@Repository)
> ```
>
> ```text
> HTTP ─▶ ControladorLibros ─▶ ServicioLibros ─▶ RepositorioLibros (interfaz)
>                                                        ▲
>                                          RepositorioLibrosEnMemoria (implementa)
> ```
>
> | Clase | Capa | Responsabilidad |
> |---|---|---|
> | `Libro` | Negocio (modelo) | Los datos de un libro. |
> | `RepositorioLibros` | Persistencia | **Contrato**: `listar()` y `buscarPorId(Long)` (este devuelve `Optional<Libro>`). |
> | `RepositorioLibrosEnMemoria` | Persistencia | Guarda los cinco libros en una lista. |
> | `ServicioLibros` | Negocio | `listar()` y `obtener(Long)`; decide qué hacer si el libro no existe. |
> | `ControladorLibros` | Presentación | Traduce las rutas HTTP a llamadas del servicio. |

</details>

<details>
<summary><strong>🧭 Cómo avanzar (sin código)</strong></summary>

> Se construye **de abajo hacia arriba**: primero el dato, luego quien lo guarda, luego la regla y al final la puerta HTTP.
>
> 1. Crear el proyecto y comprobar que **arranca** (`Started BibliotecaApplication`).
> 2. Crear los paquetes y la clase `Libro`.
> 3. Escribir la interfaz del repositorio y su implementación con los cinco libros.
> 4. Escribir el servicio con `listar()` y `obtener(id)`.
> 5. Escribir el controlador con los dos `GET`.
> 6. Probar con `curl` o Postman y guardar las capturas.
>
> > 🔎 **Pista:** si `obtener` no encuentra el libro, ¿qué te devuelve el repositorio? ¿Y qué debería hacer el servicio con eso?

</details>

<details>
<summary><strong>🧪 Cómo se comprueba</strong></summary>

> Con la aplicación en marcha (`./mvnw spring-boot:run`) se ejecutan estos comandos. Cada uno debe dar el resultado indicado.
>
> | # | Comando | Resultado esperado |
> |:-:|---|---|
> | 1 | `curl -i http://localhost:8080/libros` | `200` · arreglo de **5** libros, en el orden de la tabla |
> | 2 | `curl -i http://localhost:8080/libros/1` | `200` · el libro `Cien años de soledad` |
> | 3 | `curl -i http://localhost:8080/libros/4` | `200` · el libro `Rayuela` |
> | 4 | `curl -i http://localhost:8080/libros/99` | `500` (la excepción `NoSuchElementException` no se captura) |
>
> Forma esperada de un libro en el JSON (ejemplo del comando 2):
>
> ```json
> {
>   "id": 1,
>   "titulo": "Cien años de soledad",
>   "autor": "Gabriel García Márquez",
>   "anioPublicacion": 1967
> }
> ```

</details>

<details>
<summary><strong>📦 Entrega</strong></summary>

> - El **proyecto comprimido** (`.zip`), **sin** la carpeta `target`.
> - Un documento con las **capturas** de los cuatro comandos de «Cómo se comprueba»: petición, código de estado y cuerpo.
> - Una frase por capa explicando **qué hace** y **qué no debe hacer**.

</details>

<details>
<summary><strong>📊 Rúbrica (100 puntos)</strong></summary>

> | Criterio | Puntos |
> |---|---:|
> | El proyecto **compila y arranca** con `./mvnw` | 10 |
> | **Tres capas:** cada clase está en su paquete y con el nombre pedido | 20 |
> | **Persistencia:** interfaz + implementación con `@Repository`, cinco libros, `listar()` con copia y `buscarPorId` con `Optional` | 15 |
> | **Negocio:** `@Service`, constructor con la **interfaz**, `listar()` y `obtener(id)` con `NoSuchElementException` y el mensaje pedido | 15 |
> | **Presentación:** `@RestController`, `@RequestMapping("/libros")`, dos `@GetMapping`, `@PathVariable` | 15 |
> | **Regla de dependencia:** el controlador no usa el repositorio; el servicio no importa presentación; no hay `@Autowired` | 10 |
> | **Evidencia:** los cuatro comandos con petición, código y cuerpo | 10 |
> | **Calidad:** nombres claros, sin código sobrante ni comentarios inútiles | 5 |
> | **Total** | **100** |
>
> > ⚠️ **Descuentos:** −10 si hay base de datos o JPA; −10 si el servicio depende de la **clase** `RepositorioLibrosEnMemoria` en vez de la interfaz; −5 por cada endpoint que devuelva datos distintos a los de la tabla.

</details>

<details>
<summary><strong>🏷️ Anotaciones que se esperan ver</strong></summary>

> | Anotación | Dónde |
> |---|---|
> | `@SpringBootApplication` | `BibliotecaApplication` (la genera Initializr) |
> | `@RestController` · `@RequestMapping` | `ControladorLibros` |
> | `@GetMapping` · `@PathVariable` | Métodos del controlador |
> | `@Service` | `ServicioLibros` |
> | `@Repository` | `RepositorioLibrosEnMemoria` (solo en la clase, no en la interfaz) |

</details>

➡️ **Solución para docentes:** [05-examen-crud-libros-solucion.md](05-examen-crud-libros-solucion.md)
