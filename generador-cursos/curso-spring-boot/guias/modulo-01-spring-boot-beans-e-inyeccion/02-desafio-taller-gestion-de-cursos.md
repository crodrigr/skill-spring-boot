# 🏆 Desafío — Cancelaciones y límite de matrículas

> 🎯 **Qué se practica:** reglas de negocio, validaciones, servicio, repositorio y
> controlador, **sin ayuda de la guía**. El desafío extiende el taller «Gestión de cursos»
> ya terminado (con los puntos de control 0 a 4 funcionando).
>
> ⏱️ **Se hace en:** la clase siguiente o como trabajo autónomo. Se entrega el código y la
> evidencia de los comandos `curl` de la sección «Cómo se comprueba».

---

## 📖 El problema

La academia detectó tres situaciones que la API todavía no maneja:

1. Se registran **estudiantes menores de edad** o con **correos sin forma de correo**.
2. Un mismo estudiante puede **matricularse en todos los cursos** y acaparar los cupos.
3. Cuando alguien se retira, **no hay forma de cancelar su matrícula**, así que el cupo se
   pierde para siempre.

Se debe resolver con **nuevas reglas de negocio** y **dos endpoints nuevos**, respetando las
tres capas del taller.

---

## 📏 Reglas de negocio nuevas

| Código | Regla | Si se incumple lanza |
|---|---|---|
| **RN-12** | La edad mínima para registrar un estudiante es **16 años**. | `IllegalArgumentException` |
| **RN-13** | El correo del estudiante no puede ser `null`, ni estar vacío, ni dejar de contener `@`. | `IllegalArgumentException` |
| **RN-14** | Un estudiante no puede tener **más de 3 matrículas** a la vez. | `IllegalStateException` |
| **RN-15** | Solo se puede cancelar una matrícula que **existe**. | `NoSuchElementException` |
| **RN-16** | Al cancelar, el **cupo se libera**: el curso vuelve a tener un lugar disponible. | — |

> 🔎 **Pistas de diseño**
> - **RN-12 y RN-13** son validaciones de datos: ¿en qué capa viven?
> - **RN-14** debe evaluarse en `matricular`. El **orden** de los controles ahora es:
>   estudiante existe → curso existe → ya matriculado → **límite del estudiante** → hay cupos.
> - **RN-16** no necesita código propio si la ocupación se calcula contando las matrículas
>   guardadas. Hay que **comprobarlo**, no suponerlo.

---

## 🌐 Endpoints nuevos

| Método | Ruta | Para qué sirve | Éxito |
|:-:|---|---|:-:|
| `GET` | `/matriculas?estudianteId=1` | Listar las matrículas de un estudiante | `200` |
| `DELETE` | `/matriculas/{id}` | Cancelar una matrícula | `204` |

---

## 🧩 Qué hay que construir

| Capa | Trabajo |
|---|---|
| **Persistencia** | Ampliar `RepositorioMatriculas` y `RepositorioMatriculasEnMemoria` con tres operaciones: buscar una matrícula por id (devuelve `Optional`), eliminarla y listar las de un estudiante. |
| **Negocio** | En `ServicioEstudiantes.registrar`: RN-12 y RN-13. En `ServicioMatriculas`: RN-14 dentro de `matricular`, y dos métodos nuevos, `cancelar` (RN-15) y `listarPorEstudiante` (que debe fallar si el estudiante no existe). |
| **Presentación** | En `ControladorMatriculas`: un `GET` que reciba `estudianteId` como **parámetro de consulta** y un `DELETE` con el id en la ruta. |

**Anotaciones nuevas que hay que investigar y usar:** `@DeleteMapping`, `@RequestParam` y
`@ResponseStatus(HttpStatus.NO_CONTENT)`. Al terminar, explicar con palabras propias para qué
sirve cada una y en qué se diferencia `@RequestParam` de `@PathVariable`.

### Restricciones

- Las reglas van **solo en el servicio**; el controlador no lleva `if`.
- Los servicios lanzan **excepciones estándar de Java** (como en el resto del taller).
- Las constantes (edad mínima, máximo de matrículas) **no se escriben sueltas** dentro de los
  `if`: se declaran como constantes con nombre.
- No se pueden cambiar las firmas existentes ni romper los puntos de control anteriores.
- `ServicioCursos` **no puede depender de `ServicioMatriculas`** (recordar el ciclo).

---

## ✅ Cómo se comprueba

Con la aplicación recién arrancada (datos vacíos), ejecutar en este orden:

```bash
# Preparación: 2 estudiantes y 4 cursos
curl -s -X POST localhost:8080/estudiantes -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Torres","edad":22,"correo":"ana@correo.com"}'
curl -s -X POST localhost:8080/estudiantes -H "Content-Type: application/json" \
  -d '{"nombre":"Luis Pérez","edad":25,"correo":"luis@correo.com"}'
for n in A B C D; do
  curl -s -X POST localhost:8080/cursos -H "Content-Type: application/json" \
    -d "{\"nombre\":\"Curso $n\",\"precio\":100000,\"cupoMaximo\":1}"
done
```

| # | Escenario | Petición | Esperado |
|:-:|---|---|---|
| 1 | Menor de edad | `POST /estudiantes` con `"edad":15` | `500` · `IllegalArgumentException` |
| 2 | Correo sin `@` | `POST /estudiantes` con `"correo":"ana.correo.com"` | `500` · `IllegalArgumentException` |
| 3 | Edad límite | `POST /estudiantes` con `"edad":16` y correo válido | `201` |
| 4 | Ana en los cursos 1, 2 y 3 | `POST /matriculas` ×3 | `201`, `201`, `201` |
| 5 | Ana en el curso 4 (4.ª matrícula) | `POST /matriculas` | `500` · `IllegalStateException` (RN-14) |
| 6 | Listar las de Ana | `GET /matriculas?estudianteId=1` | `200` con **3** matrículas |
| 7 | Listar con estudiante inexistente | `GET /matriculas?estudianteId=99` | `500` · `NoSuchElementException` |
| 8 | Luis en el curso 1 (cupo 1, ocupado por Ana) | `POST /matriculas` | `500` · `IllegalStateException` (sin cupos) |
| 9 | Cancelar la matrícula de Ana en el curso 1 | `DELETE /matriculas/1` | `204` sin cuerpo |
| 10 | Cupo liberado | `GET /cursos/1/ocupacion` | `matriculados` = 0 y `cuposDisponibles` = 1 (RN-16) |
| 11 | Luis ahora sí entra al curso 1 | `POST /matriculas` | `201` |
| 12 | Cancelar una que no existe | `DELETE /matriculas/99` | `500` · `NoSuchElementException` |
| 13 | Ana vuelve a tener lugar | `POST /matriculas` Ana en el curso 4 | `201` (ahora tiene 2 y suma 3) |

> 💡 Para ver el código de estado de cada comando, usar `curl -i`. Si algún resultado no
> coincide, revisar primero **el orden de los controles** de `matricular`.

---

## 📊 Criterios de evaluación

| Criterio | Peso |
|---|:-:|
| Las reglas RN-12 a RN-16 se cumplen (escenarios 1 a 13) | 45 % |
| Cada regla está en la **capa correcta**; el controlador no tiene reglas | 25 % |
| Repositorio: interfaz ampliada e implementación en memoria coherentes | 15 % |
| Calidad del código: constantes con nombre, mensajes claros, nombres consistentes | 15 % |

---

## 🚀 Retos adicionales (opcionales)

1. **No cancelar dos veces:** si se repite `DELETE /matriculas/1`, el segundo intento ya
   cae en RN-15. Comprobarlo con un `curl`.
2. **Matrículas con detalle:** que `GET /matriculas?estudianteId=1` devuelva, además, el
   **total** de matrículas del estudiante. ¿Qué clase nueva hace falta y en qué capa va?
3. **Cursos llenos:** agregar `GET /cursos/llenos` con los cursos cuyo cupo está agotado.
   ¿Se calcula en el repositorio o en el servicio?
4. **Límite configurable:** que el máximo de matrículas por estudiante se lea de
   `application.properties` con `@Value`.

---

<details>
<summary><strong>🔑</strong></summary>

> ⚠️ Conviene intentarlo primero. Esta es **una** forma de resolverlo, no la única.
>
> **Archivo:** `persistence/repository/RepositorioMatriculas.java` (se agregan tres métodos)
>
> ```java
> Optional<Matricula> buscarPorId(Long id);
>
> void eliminar(Long id);
>
> List<Matricula> listarPorEstudiante(Long estudianteId);
> ```
>
> **Archivo:** `persistence/repository/RepositorioMatriculasEnMemoria.java` (se agregan)
>
> ```java
> @Override
> public Optional<Matricula> buscarPorId(Long id) {
>     for (Matricula matricula : matriculas) {
>         if (matricula.getId().equals(id)) {
>             return Optional.of(matricula);
>         }
>     }
>     return Optional.empty();
> }
>
> @Override
> public void eliminar(Long id) {
>     matriculas.removeIf(matricula -> matricula.getId().equals(id));
> }
>
> @Override
> public List<Matricula> listarPorEstudiante(Long estudianteId) {
>     List<Matricula> resultado = new ArrayList<>();
>     for (Matricula matricula : matriculas) {
>         if (matricula.getEstudiante().getId().equals(estudianteId)) {
>             resultado.add(matricula);
>         }
>     }
>     return resultado;
> }
> ```
>
> **Archivo:** `negocio/service/ServicioEstudiantes.java` (cambia `registrar`)
>
> ```java
> private static final int EDAD_MINIMA = 16;
>
> public Estudiante registrar(Estudiante estudiante) {
>     if (estudiante.getEdad() < EDAD_MINIMA) {
>         throw new IllegalArgumentException("La edad mínima es " + EDAD_MINIMA + " años");
>     }
>     String correo = estudiante.getCorreo();
>     if (correo == null || correo.isBlank() || !correo.contains("@")) {
>         throw new IllegalArgumentException("El correo no es válido");
>     }
>     if (repositorio.existePorCorreo(correo)) {
>         throw new IllegalStateException("Ya existe un estudiante con el correo " + correo);
>     }
>     return repositorio.guardar(estudiante);
> }
> ```
>
> **Archivo:** `negocio/service/ServicioMatriculas.java` (cambios)
>
> ```java
> private static final int MAXIMO_MATRICULAS_POR_ESTUDIANTE = 3;
>
> public Matricula matricular(Long estudianteId, Long cursoId) {
>     Estudiante estudiante = servicioEstudiantes.obtener(estudianteId);
>     Curso curso = servicioCursos.obtener(cursoId);
>
>     if (repositorio.existe(estudianteId, cursoId)) {
>         throw new IllegalStateException(
>                 "El estudiante " + estudianteId + " ya está matriculado en el curso " + cursoId);
>     }
>     if (repositorio.listarPorEstudiante(estudianteId).size() >= MAXIMO_MATRICULAS_POR_ESTUDIANTE) {
>         throw new IllegalStateException(
>                 "El estudiante " + estudianteId + " ya tiene " + MAXIMO_MATRICULAS_POR_ESTUDIANTE + " matrículas");
>     }
>     if (repositorio.contarPorCurso(cursoId) >= curso.getCupoMaximo()) {
>         throw new IllegalStateException("El curso " + cursoId + " no tiene cupos disponibles");
>     }
>     return repositorio.guardar(new Matricula(estudiante, curso));
> }
>
> public void cancelar(Long id) {
>     repositorio.buscarPorId(id)
>             .orElseThrow(() -> new NoSuchElementException("No existe la matrícula " + id));
>     repositorio.eliminar(id);
> }
>
> public List<Matricula> listarPorEstudiante(Long estudianteId) {
>     servicioEstudiantes.obtener(estudianteId);
>     return repositorio.listarPorEstudiante(estudianteId);
> }
> ```
>
> **Archivo:** `presentacion/controlador/ControladorMatriculas.java` (se agregan)
>
> ```java
> @GetMapping
> public List<Matricula> listarPorEstudiante(@RequestParam Long estudianteId) {
>     return servicio.listarPorEstudiante(estudianteId);
> }
>
> @DeleteMapping("/{id}")
> @ResponseStatus(HttpStatus.NO_CONTENT)
> public void cancelar(@PathVariable Long id) {
>     servicio.cancelar(id);
> }
> ```
>
> Imports que se agregan: `java.util.List`, `java.util.NoSuchElementException`,
> `java.util.Optional`, `java.util.ArrayList` (donde falten), y en el controlador
> `DeleteMapping`, `GetMapping`, `PathVariable` y `RequestParam` de
> `org.springframework.web.bind.annotation`.
>
> **Por qué funciona RN-16:** `contarPorCurso` recorre las matrículas guardadas; al eliminar
> una, el conteo baja solo y `calcularOcupacion` refleja el cupo liberado.

</details>
