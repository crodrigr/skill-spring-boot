# ❓ Cuestionario — Taller «Gestión de cursos» (30 preguntas)

Este cuestionario refuerza **todo lo trabajado en la guía del taller**: beans, JavaBeans,
capas, anotaciones, inyección de dependencias, interfaces, almacenamiento, rutas HTTP,
reglas de negocio y errores. Son preguntas de **complejidad alta**: casi todas parten de una
situación concreta del taller y piden razonar, no recordar.

Hay tres tipos de pregunta: **Selección** (una respuesta), **Selección múltiple** (una o más
respuestas correctas) y **Abierta** (se responde con tus palabras). Respondé primero por tu
cuenta y después abrí «Ver respuesta» para comparar. En las abiertas, la respuesta es un
modelo: lo importante es que tu explicación incluya las mismas ideas clave.

| Bloque | Preguntas |
|---|:-:|
| Beans, JavaBeans y contenedor | 1 – 6 |
| Capas y responsabilidades | 7 – 12 |
| Inyección de dependencias e interfaces | 13 – 18 |
| Anotaciones y HTTP | 19 – 24 |
| Reglas de negocio, errores y modelo | 25 – 30 |

---

## 🫘 Bloque 1 — Beans, JavaBeans y contenedor

**1. [Selección]** En el taller existe la clase `Estudiante` (con atributos privados,
constructor y getters) y la clase `ServicioEstudiantes` (con `@Service`). ¿Cuál afirmación
describe correctamente la diferencia entre un **JavaBean** y un **bean de Spring**?

- **A.** Son lo mismo: toda clase con getters y setters es un bean de Spring.
- **B.** Un JavaBean es una convención de escritura de una clase (atributos privados, acceso
  por getters y setters, constructor sin argumentos); un bean de Spring es un objeto que
  **crea y administra el contenedor**, tenga o no esa forma.
- **C.** Un bean de Spring siempre debe tener getters y setters; un JavaBean no.
- **D.** Solo las clases anotadas con `@Component` pueden ser JavaBeans.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuesta correcta: B.** «JavaBean» habla de **cómo está escrita** la clase; «bean de
> Spring» habla de **quién la crea y la gestiona**. En el taller, `Estudiante` se parece a un
> JavaBean pero **no es un bean de Spring** (se crea con `new` o lo crea Jackson al leer el
> JSON); `ServicioEstudiantes` sí es un bean de Spring y no sigue la convención JavaBean (no
> tiene getters/setters de datos).
>
> **¿Por qué un constructor sin argumentos?** Porque la convención JavaBean busca que una
> herramienta pueda **crear el objeto sin saber nada de él** y rellenarlo después. Una
> librería (Jackson al leer un JSON, JPA al leer una fila, un editor visual, la
> serialización) trabaja por reflexión: no conoce qué parámetros espera tu constructor ni en
> qué orden, pero sí puede hacer `new Clase()` y luego llamar a cada `setX(...)` con el valor
> que corresponde. Los **atributos privados** protegen el estado y los **getters/setters** son
> la puerta pública de acceso para esas herramientas.
>
> ```java
> // Lo que hace Jackson al recibir {"nombre":"Ana","edad":22}:
> Estudiante e = new Estudiante();   // 1. constructor sin argumentos
> e.setNombre("Ana");                // 2. un setter por cada campo del JSON
> e.setEdad(22);
> ```
>
> Sin ese constructor (y sin otra forma de que la librería sepa cómo construir el objeto, como
> un `record` o un constructor anotado), la creación falla. Con un constructor con
> argumentos, el programador sí sabe cómo llamarlo, pero la herramienta no.
>
</details>

**2. [Selección múltiple]** Seleccioná **todas** las clases del taller que son **beans de
Spring**.

- **A.** `ServicioCursos`
- **B.** `Curso`
- **C.** `RepositorioEstudiantesEnMemoria`
- **D.** `ControladorMatriculas`
- **E.** `SolicitudMatricula`
- **F.** `RepositorioCursos` (la interfaz)

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuestas correctas: A, C, D.** Son beans porque tienen `@Service`, `@Repository` y
> `@RestController`. `Curso` es un objeto del modelo (se crea con `new`), `SolicitudMatricula`
> es un `record` que crea Jackson al leer el JSON, y `RepositorioCursos` es una **interfaz**:
> Spring no instancia interfaces, instancia la implementación que la cumple.
>
</details>

**3. [Selección]** Un estudiante escribe `@Service` en `ServicioEstudiantes` pero deja el
paquete como `com.otro.negocio`, fuera de `com.academia`. Al arrancar aparece
`No qualifying bean of type 'ServicioEstudiantes'`. ¿Cuál es la causa más probable?

- **A.** Falta la dependencia `spring-boot-starter-web`.
- **B.** `@SpringBootApplication` escanea solo su paquete y los que cuelgan de él; la clase
  quedó fuera del escaneo y no se registró como bean.
- **C.** `@Service` solo funciona si la clase implementa una interfaz.
- **D.** Los servicios deben declararse en el `pom.xml`.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuesta correcta: B.** `@SpringBootApplication` activa el **escaneo de componentes** a
> partir de `com.academia`. Una clase anotada fuera de ese árbol no se detecta, y el
> controlador que la pide no encuentra qué inyectar. La solución es moverla dentro del árbol
> (o ampliar el escaneo, algo que el taller evita).
>
</details>

**4. [Abierta]** Explicá con tus palabras qué es el **contenedor IoC** y qué significa
«inversión de control» tomando como ejemplo la línea
`new ServicioEstudiantes(repositorio)`: ¿quién la ejecuta en el taller y por qué importa?

<details>
<summary>🔑 Ver respuesta</summary>

> **Ideas clave:**
> - El **contenedor IoC** (el `ApplicationContext` de Spring) crea los objetos anotados,
>   resuelve qué necesita cada uno y se los entrega.
> - **Inversión de control:** el programador no llama a `new` para los componentes; es el
>   **contenedor** quien decide cuándo crearlos y con qué dependencias.
> - En el taller, quien llama al constructor de `ServicioEstudiantes` es Spring, al arrancar,
>   después de crear el repositorio que le hace falta.
> - Importa porque la clase no está atada a una implementación concreta: se puede cambiar el
>   repositorio o probar el servicio con otro sin modificarlo.
>
</details>

**5. [Selección]** `ServicioEstudiantes` y `ControladorEstudiantes` son singleton. ¿Qué
código dentro del servicio sería un **error de diseño** por esa razón?

- **A.** Un atributo `private final RepositorioEstudiantes repositorio;`
- **B.** Un atributo `private Estudiante estudianteActual;` que se asigna en cada petición
  de `registrar`.
- **C.** Una variable local `Estudiante existente = ...;` dentro de un método.
- **D.** Un método `listar()` que devuelve `repositorio.listar()`.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuesta correcta: B.** Hay **una sola instancia** compartida por todas las peticiones.
> Un atributo mutable por petición sería visto y pisado por peticiones simultáneas de otros
> clientes. Los atributos de un singleton deben ser **colaboradores** (`final`, inyectados); el
> estado de cada petición va en **variables locales y parámetros**.
>
</details>

**6. [Abierta]** Los datos del taller desaparecen al reiniciar la aplicación, pero **no**
entre dos peticiones seguidas. Explicá por qué ocurre cada cosa usando los conceptos de
singleton, ciclo de vida del contexto y almacenamiento en memoria.

<details>
<summary>🔑 Ver respuesta</summary>

> **Ideas clave:**
> - Entre peticiones: `RepositorioEstudiantesEnMemoria` es un bean **singleton**; su
>   `List<Estudiante>` y su contador `siguienteId` viven mientras viva ese único objeto, así
>   que todas las peticiones ven la misma lista.
> - Al reiniciar: se destruye el contexto de Spring, se destruyen los beans y con ellos la
>   lista; al arrancar se crea un repositorio nuevo y vacío, y el contador vuelve a 1.
> - La causa de fondo es que los datos viven en la **memoria de la JVM**, no en un
>   almacenamiento persistente. Esa limitación motiva el módulo de base de datos.
>
</details>

---

## 🏛️ Bloque 2 — Capas y responsabilidades

**7. [Selección]** Un compañero propone validar «el precio debe ser mayor que cero» dentro
de `ControladorCursos`, antes de llamar al servicio. ¿Cuál es la crítica correcta según la
arquitectura del taller?

- **A.** Está bien: el controlador es quien recibe los datos, así que debe validarlos.
- **B.** Está mal: las reglas de negocio viven en `negocio.service`; si la regla está en el
  controlador, otro punto de entrada (otro controlador, un proceso por lotes) la saltaría.
- **C.** Está mal porque los controladores no pueden usar `if`.
- **D.** Está bien siempre que el repositorio repita la validación.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuesta correcta: B.** El controlador **solo traduce**: lee el JSON, llama al servicio y
> devuelve el resultado. Las reglas (RN-04, RN-05) van en `ServicioCursos`, de modo que se
> cumplan sin importar quién lo invoque.
>
</details>

**8. [Selección múltiple]** ¿Qué elementos pertenecen **solo** a la capa de **presentación**?
Seleccioná todos los correctos.

- **A.** `@GetMapping`
- **B.** La regla «no hay dos estudiantes con el mismo correo»
- **C.** `@RequestBody`
- **D.** `@ResponseStatus(HttpStatus.CREATED)`
- **E.** `List<Matricula>`
- **F.** `@PathVariable`

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuestas correctas: A, C, D, F.** Rutas, lectura del cuerpo y de la URL, y el código de
> estado son detalles de **HTTP**. La regla del correo duplicado es de **negocio** (servicio) y
> `List<Matricula>` es un detalle de **persistencia** (el repositorio en memoria).
>
</details>

**9. [Abierta]** La regla de dependencia del taller dice que las flechas van
**presentación → negocio → persistencia** y nunca al revés. Explicá con un ejemplo concreto
qué problema aparecería si `RepositorioEstudiantesEnMemoria` importara y usara
`ControladorEstudiantes`.

<details>
<summary>🔑 Ver respuesta</summary>

> **Ideas clave:**
> - La persistencia pasaría a depender de HTTP: no se podría reutilizar ni cambiar sin tocar la
>   capa web.
> - Se crearía una **dependencia circular** (controlador → servicio → repositorio →
>   controlador): Spring no puede construir los beans y la aplicación no arranca
>   (`UnsatisfiedDependencyException … circular`).
> - Se pierde la posibilidad de cambiar de almacenamiento o de probar el repositorio de forma
>   aislada.
>
</details>

**10. [Selección]** `ServicioCursos` necesita saber cuántos estudiantes hay matriculados
para calcular la ocupación, pero **no** usa `ServicioMatriculas`. ¿Por qué usa
`RepositorioMatriculas`?

- **A.** Porque los servicios no pueden llamarse entre sí en Spring.
- **B.** Porque `ServicioMatriculas` ya depende de `ServicioCursos`; si `ServicioCursos`
  dependiera también de `ServicioMatriculas` habría un ciclo y la aplicación no arrancaría.
- **C.** Porque el repositorio es más rápido que el servicio.
- **D.** Porque el servicio de matrículas no tiene un método de conteo.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuesta correcta: B.** `ServicioMatriculas` → `ServicioCursos` ya existe (para obtener el
> curso). Agregar `ServicioCursos` → `ServicioMatriculas` cerraría un **ciclo**. Al depender
> del repositorio se mantiene un grafo sin vueltas.
>
</details>

**11. [Abierta]** En el taller, `ServicioMatriculas.matricular` recibe dos `Long` y no un
objeto `Matricula`. Explicá por qué la **petición** (`SolicitudMatricula`) y la **entidad**
(`Matricula`) son clases distintas y qué haría el servicio si recibiera la entidad armada
por el cliente.

<details>
<summary>🔑 Ver respuesta</summary>

> **Ideas clave:**
> - La petición trae **identificadores**; la entidad guarda **objetos** `Estudiante` y `Curso`.
>   No son la misma forma de datos.
> - Si el cliente enviara la entidad, podría mandar un estudiante o un curso **inventado** o
>   con datos alterados, y el servicio tendría que desconfiar de ellos.
> - Con ids, el servicio busca los objetos reales (y responde «no existe» si faltan) y arma él
>   mismo la `Matricula`. El cliente solo dice **cuáles**, no **cómo son**.
> - Es la misma práctica que se usará con JPA: la solicitud lleva ids, la entidad lleva
>   relaciones.
>
</details>

**12. [Selección]** ¿Cuál de estas ubicaciones es la correcta para la clase
`SolicitudMatricula` y por qué?

- **A.** `negocio.model`, porque es un dato del dominio.
- **B.** `persistence.repository`, porque se guarda.
- **C.** `presentacion.controlador`, porque modela lo que llega por HTTP y el negocio no
  debe conocerla.
- **D.** En cualquier paquete, porque es un `record`.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuesta correcta: C.** Describe el **formato de la petición HTTP**. El servicio recibe
> solo `estudianteId` y `cursoId`, así que la capa de negocio no depende de una clase pensada
> para el cliente web.
>
</details>

---

## 💉 Bloque 3 — Inyección de dependencias e interfaces

**13. [Selección]** Observá este servicio:

```java
@Service
public class ServicioEstudiantes {
    private final RepositorioEstudiantes repositorio;

    public ServicioEstudiantes(RepositorioEstudiantes repositorio) {
        this.repositorio = repositorio;
    }
}
```

¿Cómo obtiene el `repositorio`, si no hay `@Autowired` ni `new`?

- **A.** Spring detecta el único constructor, busca un bean que cumpla el tipo
  `RepositorioEstudiantes` y se lo pasa como argumento.
- **B.** Java lo asigna automáticamente por ser `final`.
- **C.** Se crea con reflexión leyendo el `pom.xml`.
- **D.** Es `null` hasta que alguien llame a un setter.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuesta correcta: A.** Es **inyección por constructor**: con un único constructor, Spring
> no necesita `@Autowired`. Resuelve el tipo de la interfaz buscando un bean que lo implemente
> (`RepositorioEstudiantesEnMemoria`) y lo entrega al construir el servicio.
>
</details>

**14. [Selección múltiple]** ¿Qué **ventajas** da la inyección por constructor con campos
`final`, frente a crear el repositorio con `new` dentro del servicio? Seleccioná todas las
correctas.

- **A.** El servicio puede probarse pasándole cualquier implementación del repositorio.
- **B.** El servicio queda atado a una implementación concreta y es más difícil cambiarla.
- **C.** Las dependencias quedan explícitas: el constructor muestra qué necesita la clase.
- **D.** Un objeto no puede quedar a medio construir sin sus dependencias, y no se pueden
  reasignar después.
- **E.** Spring ya no necesita escanear el paquete.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuestas correctas: A, C, D.** La B es lo contrario de lo que se logra, y la E es falsa:
> el escaneo sigue siendo necesario para encontrar los beans.
>
</details>

**15. [Abierta]** Si en vez de la interfaz el servicio declarara
`private final RepositorioEstudiantesEnMemoria repositorio;`, ¿qué se perdería? Explicá
con el caso de querer pasar a otro tipo de almacenamiento.

<details>
<summary>🔑 Ver respuesta</summary>

> **Ideas clave:**
> - El servicio quedaría **acoplado a la implementación**: nombraría una clase concreta en su
>   constructor y en su tipo.
> - Cambiar de almacenamiento obligaría a **editar el servicio**, violando el principio de
>   que la regla de negocio no debe saber dónde se guardan los datos.
> - Con la **interfaz**, el servicio depende de un contrato (`guardar`, `listar`,
>   `buscarPorId`…); Spring inyecta cualquier clase que lo cumpla.
> - Además, se pierde la facilidad de pruebas con implementaciones falsas.
>
</details>

**16. [Abierta]** Querés guardar los estudiantes en un `Map<Long, Estudiante>` en lugar de
una `List`. Describí **paso a paso** qué clases creás, cuáles modificás, cuáles **no** tocás
y qué anotaciones deben cambiar. Pensá en el reto final del taller.

<details>
<summary>🔑 Ver respuesta</summary>

> **Ideas clave:**
> 1. Crear `RepositorioEstudiantesAlternativo` que **implemente** `RepositorioEstudiantes`
>    con un `Map<Long, Estudiante>` (y su propio contador de ids).
> 2. Anotarla con `@Repository` y **quitar `@Repository`** de la versión con `List` (o dejar
>    solo una activa). Si ambas estuvieran anotadas, Spring no sabría cuál inyectar y fallaría
>    por ambigüedad.
> 3. **No se modifican:** `RepositorioEstudiantes` (la interfaz), `ServicioEstudiantes`,
>    `ControladorEstudiantes`, `ServicioMatriculas` ni el resto de las capas superiores.
> 4. Resultado: cambia solo la capa de **persistence**. Es la demostración práctica de por qué
>    se programa contra interfaces.
>
</details>

**17. [Selección]** Dos clases, `RepositorioEstudiantesEnMemoria` y
`RepositorioEstudiantesAlternativo`, implementan `RepositorioEstudiantes` y **ambas** tienen
`@Repository`. ¿Qué ocurre al arrancar?

- **A.** Spring usa la primera por orden alfabético.
- **B.** Spring usa la última que encuentre.
- **C.** Falla con `NoUniqueBeanDefinitionException`: hay dos candidatos para el tipo y no
  sabe cuál inyectar (se resuelve con `@Primary`, con `@Qualifier` o dejando una sola).
- **D.** Crea una instancia mezclada de ambas.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuesta correcta: C.** Cuando hay varios beans de un mismo tipo, la inyección es
> ambigua. En el taller se elige dejar **una sola** anotada; los otros caminos
> (`@Primary`, `@Qualifier`) son herramientas que se verán más adelante.
>
</details>

**18. [Abierta]** Dibujá (con texto) el **grafo de dependencias** de
`ServicioMatriculas` y explicá por qué el grafo del taller no tiene dependencias
circulares. ¿Qué cambio concreto en `ServicioCursos` lo rompería?

<details>
<summary>🔑 Ver respuesta</summary>

> **Ideas clave:**
> ```text
> ServicioMatriculas ─▶ RepositorioMatriculas
>                    ─▶ ServicioEstudiantes ─▶ RepositorioEstudiantes
>                    ─▶ ServicioCursos      ─▶ RepositorioCursos
>                                           ─▶ RepositorioMatriculas
> ```
> - Todas las flechas van «hacia abajo»; ninguna vuelve a un nodo ya visitado.
> - Lo rompería inyectar `ServicioMatriculas` en `ServicioCursos` (por ejemplo, para el
>   reporte): `ServicioMatriculas → ServicioCursos → ServicioMatriculas`. Spring no podría
>   crear ninguno de los dos primero y lanzaría una excepción por dependencia circular.
> - Solución usada: `ServicioCursos` depende del **repositorio** de matrículas.
>
</details>

---

## 🏷️ Bloque 4 — Anotaciones y HTTP

**19. [Selección]** ¿Qué diferencia práctica hay entre `@Controller` y `@RestController`, y
por qué el taller usa la segunda?

- **A.** Ninguna; son alias.
- **B.** `@RestController` equivale a `@Controller` + `@ResponseBody`: lo que devuelve cada
  método se escribe directamente como JSON en la respuesta, en lugar de interpretarse como
  el nombre de una vista.
- **C.** `@RestController` solo acepta `GET`.
- **D.** `@Controller` es para la capa de negocio.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuesta correcta: B.** El taller es una API REST: devuelve datos (objetos convertidos a
> JSON), no páginas HTML.
>
</details>

**20. [Selección múltiple]** `@Repository`, `@Service` y `@RestController` comparten algo.
Seleccioná **todas** las afirmaciones correctas.

- **A.** Son especializaciones de `@Component`.
- **B.** Hacen que Spring cree un bean de la clase.
- **C.** El bean resultante es singleton por defecto.
- **D.** Cambian el comportamiento de los métodos para que se ejecuten en hilos separados.
- **E.** Sirven para documentar a qué capa pertenece la clase.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuestas correctas: A, B, C, E.** La D es falsa: ninguna crea hilos. Los tres nombres
> documentan la **capa** (persistencia, negocio, presentación); la creación del bean y el
> *scope* singleton los aportan `@Component` y el contenedor.
>
</details>

**21. [Selección]** El controlador tiene `@RequestMapping("/estudiantes")` en la clase y
`@GetMapping("/{id}")` en un método. ¿Qué petición atiende ese método?

- **A.** `GET /{id}`
- **B.** `GET /estudiantes/{id}`, por ejemplo `GET /estudiantes/7`
- **C.** `POST /estudiantes/7`
- **D.** `GET /estudiantes`

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuesta correcta: B.** La ruta del método **se suma** a la ruta base de la clase.
> `{id}` es una variable de ruta que se recibe con `@PathVariable`.
>
</details>

**22. [Abierta]** Un `POST /cursos` con el JSON `{"nombre":"Java","precio":0,"cupoMaximo":5}`
llega al servidor. Describí el recorrido completo por las tres capas (qué anotación o clase
interviene en cada paso) y en qué punto se detecta que el precio es inválido.

<details>
<summary>🔑 Ver respuesta</summary>

> **Ideas clave:**
> 1. **Presentación:** `@PostMapping` en `ControladorCursos` recibe la petición; `@RequestBody`
>    convierte el JSON en un objeto `Curso`.
> 2. El controlador llama a `ServicioCursos.registrar(curso)`.
> 3. **Negocio:** el servicio comprueba `precio > 0` (RN-04) y detecta el error; lanza
>    `IllegalArgumentException` y **no llega** a persistencia.
> 4. Si fuera válido: el servicio llama al repositorio, que asigna el id (RN-01) y guarda en
>    la lista.
> 5. De vuelta en el controlador, `@ResponseStatus(HttpStatus.CREATED)` fija `201` y el objeto
>    se devuelve como JSON.
> 6. En este taller, al no haber manejador de errores, la excepción termina como `500`.
>
</details>

**23. [Selección]** Un estudiante recibe `415 Unsupported Media Type` al hacer
`POST /estudiantes` con Postman. ¿Cuál es la causa más probable?

- **A.** El servicio lanzó `IllegalStateException`.
- **B.** Falta el encabezado `Content-Type: application/json`, por lo que `@RequestBody` no
  sabe cómo convertir el cuerpo.
- **C.** El puerto 8080 está ocupado.
- **D.** El repositorio no tiene `@Repository`.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuesta correcta: B.** Sin ese encabezado, Spring no sabe que el cuerpo es JSON.
> Una anotación faltante en el repositorio haría fallar el **arranque**, no devolvería `415`.
>
</details>

**24. [Abierta]** ¿Por qué los métodos `POST` llevan `@ResponseStatus(HttpStatus.CREATED)` y
los `GET` no? ¿Qué código devolvería un `POST` sin esa anotación y qué significa cada uno?

<details>
<summary>🔑 Ver respuesta</summary>

> **Ideas clave:**
> - Por defecto, un método que termina bien responde `200 OK`.
> - `201 Created` indica que **se creó un recurso nuevo**; es lo que corresponde a un `POST`
>   de registro.
> - Los `GET` solo consultan, así que `200 OK` es el código correcto y no necesitan la
>   anotación.
>
</details>

---

## ⚖️ Bloque 5 — Reglas de negocio, errores y modelo

**25. [Selección]** El orden de los controles en `matricular` es: ¿existe el estudiante?,
¿existe el curso?, ¿ya está matriculado?, ¿quedan cupos? ¿Qué pasa si se invierten los dos
últimos?

- **A.** Nada: el resultado final siempre es el mismo.
- **B.** Cambiaría **qué error ve primero el cliente**: un estudiante ya matriculado en un
  curso lleno recibiría «sin cupos» en lugar de «ya matriculado».
- **C.** La aplicación no compilaría.
- **D.** Se guardaría la matrícula dos veces.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuesta correcta: B.** Ambos controles lanzan `IllegalStateException`, pero con mensajes
> distintos. El orden decide el mensaje y, en general, qué información recibe el cliente
> primero.
>
</details>

**26. [Selección múltiple]** En `RepositorioMatriculasEnMemoria`, ¿qué afirmaciones sobre
`matricula.getCurso().getId().equals(cursoId)` son correctas?

- **A.** Compara los valores de dos objetos `Long`.
- **B.** Usar `==` en su lugar podría funcionar con ids pequeños y fallar con ids mayores de
  127.
- **C.** `equals` es obligatorio porque `Long` es un objeto, no un primitivo.
- **D.** `==` y `equals` son siempre intercambiables con `Long`.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuestas correctas: A, B, C.** `Long` guarda en caché los valores entre −128 y 127, por
> lo que `==` parece funcionar con ids pequeños y falla con mayores. Es un error clásico.
>
</details>

**27. [Selección]** En el taller, ¿qué ocurre cuando un servicio lanza
`NoSuchElementException("No existe el estudiante 99")` y **nadie** la captura?

- **A.** La aplicación se detiene.
- **B.** Spring devuelve `404 Not Found` automáticamente.
- **C.** La excepción sube hasta el contenedor web y el cliente recibe `500 Internal Server
  Error`; el mensaje queda en la consola. Convertirla en `404` exige un manejo explícito
  (tema del Módulo 7).
- **D.** El método devuelve `null`.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuesta correcta: C.** Las excepciones estándar de Java no llevan información HTTP; sin
> un manejador, la respuesta es un `500` genérico. La aplicación **no** se detiene: sigue
> atendiendo peticiones.
>
</details>

**28. [Abierta]** `Matricula` guarda objetos `Estudiante` y `Curso` en vez de ids. Explicá
qué ventajas tiene ese diseño, por qué se parece a lo que se hará con JPA y qué información
nueva aparece en la respuesta de `POST /matriculas`.

<details>
<summary>🔑 Ver respuesta</summary>

> **Ideas clave:**
> - El modelo expresa la relación real: una matrícula **tiene** un estudiante y un curso; se
>   puede navegar `matricula.getCurso().getNombre()` sin buscar de nuevo.
> - Es el mismo modelo que JPA mapeará con `@ManyToOne`; al migrar solo se agregan anotaciones,
>   no se rediseña la clase.
> - La respuesta ya no trae `estudianteId` y `cursoId` sueltos: trae los objetos completos
>   anidados (`{"id":1,"estudiante":{…},"curso":{…}}`).
> - El costo: para comparar hay que usar `getEstudiante().getId()` y `getCurso().getId()`, y
>   el servicio debe obtener los objetos antes de crear la matrícula.
>
</details>

**29. [Abierta]** Escribí cómo calcularías el **porcentaje de ocupación** de un curso con
3 cupos y 2 matriculados, y explicá por qué `matriculados / cupoMaximo` con enteros daría
`0` en lugar de `66.67`. ¿Qué capa hace el cálculo y por qué?

<details>
<summary>🔑 Ver respuesta</summary>

> **Ideas clave:**
> - Se castea uno de los operandos a `double` **antes** de dividir:
>   `(double) matriculados / cupoMaximo * 100` → `66.666…`, y se redondea a dos decimales.
> - Con dos `int`, `2 / 3` es una **división entera** que da `0`.
> - Lo calcula la capa de **negocio** (`ServicioCursos.calcularOcupacion`): es una regla
>   (RN-11) y no depende de HTTP ni de cómo se almacenan los datos; el controlador solo
>   entrega el resultado y el repositorio solo cuenta matrículas.
>
</details>

**30. [Selección múltiple]** Un estudiante dice: «cambié `RepositorioCursosEnMemoria` por
una versión que usa una base de datos y tuve que modificar `ServicioCursos`». ¿Qué
conclusiones son **correctas** sobre su diseño?

- **A.** Probablemente `ServicioCursos` dependía de la clase concreta en lugar de la
  interfaz `RepositorioCursos`.
- **B.** Es lo esperado: al cambiar el almacenamiento siempre cambia el servicio.
- **C.** Si hubiera programado contra la interfaz, solo habría cambiado la capa
  `persistence`.
- **D.** El servicio probablemente tenía código que dependía de detalles de la lista en
  memoria (por ejemplo, recorrerla directamente) que debió ocultar el repositorio.
- **E.** El controlador también debería haber cambiado.

<details>
<summary>🔑 Ver respuesta</summary>

> **Respuestas correctas: A, C, D.** El objetivo de las tres capas y de las interfaces es que
> **cambiar el almacenamiento solo toque `persistence`**. Si el servicio tuvo que cambiar, la
> capa de negocio estaba filtrando detalles de la persistencia. El controlador no debería
> verse afectado.
>
</details>

---

## 📊 Resumen del cuestionario

| Tipo | Cantidad | Preguntas |
|---|:-:|---|
| Selección | 13 | 1, 3, 5, 7, 10, 12, 13, 17, 19, 21, 23, 25, 27 |
| Selección múltiple | 6 | 2, 8, 14, 20, 26, 30 |
| Abierta | 11 | 4, 6, 9, 11, 15, 16, 18, 22, 24, 28, 29 |
| **Total** | **30** | |
