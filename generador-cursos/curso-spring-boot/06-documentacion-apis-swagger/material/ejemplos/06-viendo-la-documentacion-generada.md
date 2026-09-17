# 💡 Ejemplo 06 — Viendo la documentación generada

## 🌍 Contexto

Con la dependencia agregada (Ejemplo 04) y `application.properties`
configurado (Ejemplo 05), `ControladorLibros` ya está completamente
documentado — sin escribir ninguna línea de YAML ni ninguna anotación
adicional. Este ejemplo muestra qué se ve al acceder a Swagger UI.

**Qué busca demostrar este ejemplo**: describir la documentación
generada automáticamente para los seis endpoints de `ControladorLibros`,
y mostrar, con `ControladorCitas` (Módulo 5), que un campo con
`@JsonIgnore` tampoco aparece en el esquema generado.

## 📚🏥 Caso de estudio

Biblioteca Universitaria (`ControladorLibros`) y MediSalud
(`ControladorCitas`, Módulo 5, Desafío).

<details>
<summary>📄 Ver código completo de <code>ControladorLibros.java</code> (reutilizado de los Ejemplos 04-05)</summary>

## 💻 Archivo: `ControladorLibros.java`

```java
@RestController
@RequestMapping("/libros")
public class ControladorLibros {

    private final ServicioLibros servicioLibros;

    public ControladorLibros(ServicioLibros servicioLibros) {
        this.servicioLibros = servicioLibros;
    }

    @GetMapping
    public List<Libro> listarTodos() {
        return servicioLibros.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> buscarPorId(@PathVariable Long id) {
        return servicioLibros.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<Libro> buscarPorIsbn(@RequestParam String isbn) {
        return servicioLibros.buscarPorIsbn(isbn)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Libro> crear(@RequestBody Libro libro) {
        Libro creado = servicioLibros.crear(libro);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Libro> actualizar(@PathVariable Long id, @RequestBody Libro datos) {
        return servicioLibros.actualizar(id, datos)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boolean existia = servicioLibros.eliminar(id);
        return existia ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
```

</details>

## 🌐 Documentación generada: `ControladorLibros`

Al acceder a `http://localhost:8080/doc/swagger-ui.html`, Swagger UI
muestra, agrupados bajo el nombre `ControladorLibros`, los seis
endpoints:

```text
GET    /libros            → listarTodos       → 200
GET    /libros/{id}       → buscarPorId       → 200, 404
GET    /libros/buscar     → buscarPorIsbn     → 200, 404 (parámetro: isbn, query)
POST   /libros            → crear             → 201 (cuerpo: Libro)
PUT    /libros/{id}       → actualizar        → 200, 404 (cuerpo: Libro)
DELETE /libros/{id}       → eliminar          → 200, 404
```

Cada endpoint es expandible en la interfaz y muestra sus parámetros, el
esquema del cuerpo esperado (si aplica) y los posibles códigos de
respuesta — toda esa información se extrajo directamente de las
anotaciones (`@GetMapping`, `@PathVariable`, `@RequestBody`, etc.) y de
los tipos de retorno de cada método, sin ninguna anotación adicional de
documentación.

El esquema de `Libro`, visible en la sección "Schemas" de Swagger UI, se
generó a partir de los campos y getters de la clase:

```text
Libro
├── id: integer
├── isbn: string
└── titulo: string
```

## 🌐 Documentación generada: efecto de `@JsonIgnore` en el esquema

`ControladorCitas` (Módulo 5, Desafío) expone `Cita`, que tiene una
relación `@ManyToOne` hacia `Paciente`. `Paciente.citas`, del lado
inverso, tiene `@JsonIgnore` para evitar la recursión infinita al
serializar (Módulo 5):

```java
@OneToMany(mappedBy = "paciente")
@JsonIgnore
private List<Cita> citas = new ArrayList<>();
```

Swagger UI, al generar los esquemas de `Cita` y `Paciente`, respeta esa
misma anotación de Jackson:

```text
Cita
├── id: integer
├── fecha: string (date)
├── motivo: string
└── paciente: Paciente        ← SÍ aparece

Paciente
├── id: integer
├── codigo: string
├── nombre: string
└── (citas no aparece)        ← @JsonIgnore lo excluye del esquema
```

## 🧭 Explicación paso a paso

1. La documentación de `ControladorLibros` no requirió ninguna anotación
   adicional: springdoc leyó directamente `@GetMapping`/`@PostMapping`/
   etc., los tipos de parámetro (`@PathVariable`, `@RequestParam`,
   `@RequestBody`) y el tipo de retorno de cada método.
2. El esquema de una entidad (`Libro`, `Cita`, `Paciente`) se genera a
   partir de sus campos/getters, igual que lo haría Jackson al serializar
   esa clase a JSON — no es una coincidencia: springdoc usa el mismo
   mecanismo de serialización que ya usa Spring para las respuestas
   reales.
3. Por eso `Paciente.citas` no aparece en el esquema documentado de
   `Paciente`: `@JsonIgnore` le dice a Jackson (y, en consecuencia, a
   springdoc) que ese campo nunca debe serializarse — el mismo mecanismo
   que resolvió la recursión infinita en el Módulo 5 también determina
   qué campos se documentan.
4. Esto confirma una regla general: la documentación automática siempre
   refleja fielmente lo que la API realmente devuelve, incluyendo sus
   exclusiones — a diferencia de la documentación manual (Ejemplo 03),
   que podría, por error humano, describir un campo que en realidad
   nunca se serializa.
5. **Comparación con la documentación manual**: en SwaggerHub (Ejemplo
   03), este mismo esquema tendría que escribirse a mano en YAML, sin
   ninguna garantía de que coincida con lo que el código realmente
   produce; acá, el esquema se deriva directamente del código, así que
   nunca puede desincronizarse.

## ❓ Preguntas de repaso

**1. [Selección]** ¿De dónde extrae springdoc la información para
documentar un endpoint como `GET /libros/{id}`?

- **A.** De un archivo YAML escrito a mano.
- **B.** De las anotaciones (`@GetMapping`, `@PathVariable`, etc.) y el tipo de retorno del método.
- **C.** De una consulta directa a la base de datos H2.
- **D.** De un archivo de configuración separado, `openapi.json`.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Springdoc lee directamente las anotaciones de
Spring MVC y los tipos usados en el código del controlador.

</details>

**2. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre el esquema de `Paciente` generado en este ejemplo.

- **A.** El campo `citas` no aparece porque tiene `@JsonIgnore`.
- **B.** Springdoc usa el mismo mecanismo de serialización que Jackson para generar los esquemas.
- **C.** El esquema de `Paciente` se escribió a mano para excluir `citas`.
- **D.** Si se quitara `@JsonIgnore`, `citas` volvería a aparecer tanto en las respuestas JSON reales como en el esquema documentado.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: nadie escribió el
esquema a mano; se generó automáticamente respetando `@JsonIgnore`.

</details>

**3. [Abierta]** Un compañero te dice: "si `@JsonIgnore` oculta un campo
de la documentación, entonces para documentar un campo que sí quiero
mostrar en Swagger pero no en las respuestas JSON reales, ¿alcanza con
quitarle `@JsonIgnore`?".

**Pregunta**: ¿Qué le responderías?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: No exactamente — `@JsonIgnore` no es un interruptor
independiente para "solo documentación": controla la serialización JSON
real (Jackson), y springdoc simplemente respeta esa misma configuración
al generar el esquema. No existe, en lo cubierto por este módulo, una
forma de mostrar un campo en Swagger pero excluirlo de las respuestas
reales (o viceversa): ambas cosas están atadas al mismo mecanismo de
serialización. Si un campo aparece en el esquema, también aparecerá en
las respuestas JSON reales, y viceversa.

</details>
