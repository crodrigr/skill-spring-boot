# 📝 Examen — CRUD de libros en tres capas

> 📌 **Examen práctico individual.** Este documento contiene **solo el enunciado**: qué construir, con qué
> reglas y cómo se comprueba. No incluye código ni pasos de solución. Se aplica lo visto en el
> [Módulo 01](../../01-introduccion-a-spring-boot/) y el [Módulo 02](../../02-manejo-de-dependencias-y-java-beans/),
> con el mismo patrón del taller «Gestión de cursos».
>
> ⏱️ **Duración:** 60 minutos.
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
> | **RNF-02** | La aplicación se organiza en **tres capas** —presentación, negocio y persistencia—, cada una en su propio paquete. Los nombres de paquetes y clases los decide quien resuelve el examen, en español. |
> | **RNF-03** | **Regla de dependencia:** presentación → negocio → persistencia. El controlador **no** conoce el repositorio, y el servicio **no** importa nada de presentación. |
> | **RNF-04** | El servicio depende de una **interfaz** del repositorio, no de su implementación. |
> | **RNF-05** | **Inyección por constructor.** No se usa `@Autowired` ni `new` para crear servicios o repositorios. |
> | **RNF-06** | Sin base de datos, sin JPA, sin Lombok. Los datos viven en una `List<Libro>` dentro del repositorio. |
> | **RNF-07** | `listar()` del repositorio devuelve una **copia** de la lista, para que nadie de afuera la modifique. |
> | **RNF-08** | Nombres del código en **español**, como en el taller. |

</details>

<details>
<summary><strong>🏗️ Las tres capas</strong></summary>

> La aplicación se construye en **tres capas**, que son las del curso:
>
> | Capa | Qué hace |
> |---|---|
> | **Presentación** | Recibe las peticiones HTTP y devuelve las respuestas. |
> | **Negocio** | Contiene las reglas y el modelo de datos. |
> | **Persistencia** | Guarda y busca los datos (aquí, una lista en memoria). |
>
> ```text
> HTTP ─▶ Presentación ─▶ Negocio ─▶ Persistencia
> ```
>
> Cada capa solo conoce a la que tiene **a su derecha**, como indica RNF-03.

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

> La entrega es un **repositorio en GitHub**:
>
> 1. Crear un repositorio **público** en GitHub (por ejemplo, `examen-biblioteca`) con el proyecto completo.
> 2. Subir el código **sin** la carpeta `target` (el archivo `.gitignore` que genera Initializr ya la excluye).
> 3. Agregar un `README.md` con el nombre de quien entrega, cómo ejecutar el proyecto y las **capturas** de los cuatro comandos de «Cómo se comprueba»: petición, código de estado y cuerpo.
> 4. Enviar al docente la **URL** del repositorio.
>
> > 💡 Conviene hacer **commits** a medida que se avanza: el historial muestra cómo se construyó el proyecto.

</details>

<details>
<summary><strong>🏷️ Anotaciones que se esperan ver</strong></summary>

> | Capa | Anotaciones |
> |---|---|
> | Clase principal | `@SpringBootApplication` (la genera Initializr) |
> | Presentación | `@RestController` · `@RequestMapping` · `@GetMapping` · `@PathVariable` |
> | Negocio | `@Service` |
> | Persistencia | `@Repository` (solo en la clase, no en la interfaz) |

</details>
