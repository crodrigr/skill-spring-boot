# 📏 Rúbrica — Proyecto Integrador 01: CoWorkHub

Esta rúbrica evalúa el [enunciado](enunciado.md) sobre **100 puntos**. Podés usarla
para autoevaluarte antes de entregar.

## 🧮 Cómo se califica

- Cada criterio tiene **indicadores** con un puntaje máximo. Un indicador se otorga
  completo, a la mitad o en cero según lo que se pueda **demostrar** (código que
  funciona, prueba ejecutada, documento entregado). Lo que "debería funcionar" pero no
  se demuestra, no suma.
- Cada criterio se ubica además en un **nivel de desempeño** (tabla de cada sección),
  como referencia cualitativa para la devolución.

| Nivel | Porcentaje del criterio | Significado |
|---|---|---|
| 🟢 **Sobresaliente** | 90 – 100 % | Cumple todo, justifica sus decisiones y no tiene errores relevantes |
| 🔵 **Logrado** | 70 – 89 % | Cumple lo esencial, con fallas menores o justificaciones incompletas |
| 🟡 **En desarrollo** | 50 – 69 % | Cumple parte de lo pedido; hay reglas o capas incompletas |
| 🔴 **Insuficiente** | menos de 50 % | No se puede demostrar el funcionamiento o incumple lo básico |

| Calificación final | Puntaje |
|---|---|
| Aprobado con distinción | 90 – 100 |
| Aprobado | 70 – 89 |
| Aprobado con observaciones (debe corregir y reentregar lo señalado) | 60 – 69 |
| No aprobado | menos de 60 |

## 1. 📝 Análisis — 15 puntos

| Indicador | Pts |
|---|---|
| Actores y casos de uso: los tres actores, con diagrama y descripción breve de cada caso de uso | 3 |
| Glosario: define los 6 términos pedidos (reserva, plan, horas incluidas, horas excedentes, solapamiento, cargo por cancelación) sin contradecir las reglas de negocio | 2 |
| Diagrama entidad-relación completo, con **justificación** del lado dueño, `fetch` y `cascade` de cada relación | 4 |
| Matriz de trazabilidad: cada RF enlazado a sus RN y a sus endpoints, sin RF huérfanos | 3 |
| Supuestos y decisiones: registra las ambigüedades que resolvió (por ejemplo, qué pasa con las horas al cancelar tarde) | 2 |
| Fuera de alcance declarado | 1 |

| Nivel | Descriptor |
|---|---|
| 🟢 | El documento permite implementar el sistema sin preguntar nada; las decisiones están justificadas |
| 🔵 | Completo, pero justifica poco o la trazabilidad tiene huecos |
| 🟡 | Faltan secciones o el diagrama no coincide con lo implementado |
| 🔴 | No entrega análisis, o es una copia del enunciado sin trabajo propio |

## 2. 🗄️ Modelo JPA — 15 puntos

| Indicador | Pts |
|---|---|
| Las 9 entidades con sus atributos, tipos correctos (`BigDecimal` para dinero, `LocalDateTime`, `enum` con `@Enumerated(STRING)`) | 3 |
| Relaciones correctas y **lado dueño** correcto: `Sala`→`Sede`, `Sala`↔`Equipamiento` (N↔N), `Miembro`→`Plan`, `Miembro`↔`Usuario` (1↔1), `Reserva`→`Sala`/`Miembro`, `DetalleReserva` como entidad intermedia | 5 |
| `cascade` + `orphanRemoval` en `Reserva`→`DetalleReserva`; quitar un servicio elimina el detalle de la base | 3 |
| `fetch` justificado: colecciones `LAZY`, sin `LazyInitializationException` ni referencias circulares en el JSON | 2 |
| Restricciones de datos: `unique` en documento, email y (sede, nombre de sala); `nullable = false` donde corresponde | 2 |

| Nivel | Descriptor |
|---|---|
| 🟢 | El esquema generado es el esperado y todas las decisiones de mapeo están justificadas |
| 🔵 | Mapeo correcto; alguna decisión de `fetch` o `cascade` sin justificar |
| 🟡 | Hay relaciones con el lado dueño equivocado o `cascade` innecesario (`CascadeType.ALL` sin criterio) |
| 🔴 | No arranca contra H2 o faltan relaciones obligatorias |

## 3. ⚙️ Reglas de negocio — 25 puntos

Cada regla se otorga si está **implementada en la capa `services`** y hay una prueba que
la demuestra (idealmente uno de los [escenarios de aceptación](enunciado.md#10-escenarios-de-aceptación-mínimos)).

| Regla | Qué se demuestra | Pts |
|---|---|---|
| RN-01 | Solapamiento rechazado; extremos que se tocan aceptados; **dos solicitudes simultáneas** a la misma sala/hora: solo una gana | 3 |
| RN-02 | 45 min, más de 8 h y cruzar la medianoche rechazados | 2 |
| RN-03 | Fuera de horario, en el pasado y con menos de 1 h de anticipación rechazados | 2 |
| RN-04 | Sala inactiva y asistentes sobre la capacidad rechazados | 1 |
| RN-05 | Miembro suspendido no puede reservar | 1 |
| RN-06 | Se respeta `maxReservasActivas` del plan | 2 |
| RN-07 | Costo correcto: horas incluidas sin cobro, excedente con descuento, servicios con precio vigente al reservar (cambiar el precio después no altera reservas existentes) | 4 |
| RN-08 | Cancelación con 24 h o más (sin cargo y horas devueltas), entre 24 h y 2 h (50 %), con menos de 2 h o iniciada (rechazada). Casos límite exactos de 24 h y 2 h | 4 |
| RN-09 | Solo las transiciones de estado permitidas | 2 |
| RN-10 | Servicios modificables solo con la reserva `PENDIENTE` | 1 |
| RN-11 | Documento, email (sin distinguir mayúsculas) y nombre de sala por sede únicos | 1 |
| RN-12 | No se eliminan salas, planes ni miembros con reservas asociadas | 1 |
| RN-13 | Un miembro no ve ni opera reservas ajenas; solo `ADMIN` modifica catálogos | 1 |
| **Total** | | **25** |

| Nivel | Descriptor |
|---|---|
| 🟢 | 23 – 25 puntos: todas las reglas, incluidos casos límite y concurrencia |
| 🔵 | 18 – 22: reglas correctas en el caso normal; casos límite o concurrencia sin resolver |
| 🟡 | 13 – 17: faltan reglas de cálculo (RN-07, RN-08) o de estados |
| 🔴 | menos de 13, o las reglas viven en los controladores |

## 4. 🏗️ Arquitectura MVC y calidad del código — 15 puntos

| Indicador | Pts |
|---|---|
| Paquetes `controllers`, `services`, `persistences` (`entities`, `repositories`); `exception`, `config` y `jwt` como paquetes transversales. Ningún controlador usa un repositorio y ningún servicio usa un controlador | 5 |
| Toda la lógica de negocio en `services`; inyección **por constructor**; el cálculo de costos en un bean propio (`@Component`) inyectado en el servicio de reservas | 3 |
| Operaciones que escriben varias entidades son `@Transactional`; una falla no deja datos a medias (se prueba con una solicitud que falla a mitad de camino) | 3 |
| Los listados no producen el problema N+1 (se comprueba con `spring.jpa.show-sql=true`) | 2 |
| Nombres en español y coherentes; sin código duplicado entre servicios; cada clase con una sola responsabilidad | 2 |

| Nivel | Descriptor |
|---|---|
| 🟢 | Un lector nuevo encuentra cada cosa donde espera; no hay lógica fuera de su capa |
| 🔵 | Buena separación, con alguna lógica de negocio filtrada al controlador o duplicada |
| 🟡 | Capas mezcladas: el controlador consulta repositorios o arma respuestas de negocio |
| 🔴 | Todo en pocas clases, sin paquetes por capa |

## 5. 🚨 Manejo de excepciones y códigos HTTP — 10 puntos

| Indicador | Pts |
|---|---|
| Un único `@ControllerAdvice` traduce las excepciones; los servicios lanzan excepciones propias (no conocen HTTP) | 3 |
| **Todas** las respuestas de error usan el mismo cuerpo `{"error", "codigo", "timestamp"}`, incluidos JSON mal formado, ruta inexistente y método no permitido | 3 |
| Códigos correctos: `400`, `404`, `409` (y `401`/`403` en seguridad) según RNF-06 | 3 |
| Ninguna regla de negocio responde `500`; los errores inesperados no filtran detalles internos | 1 |

| Nivel | Descriptor |
|---|---|
| 🟢 | Cualquier entrada inválida produce un error claro, con el código correcto y el cuerpo estándar |
| 🔵 | Códigos correctos, pero algún error de Spring MVC usa el cuerpo por defecto |
| 🟡 | Mezcla `ResponseEntity` manual en controladores con el manejador global |
| 🔴 | Responde `500` o el error por defecto de Spring ante reglas de negocio |

## 6. 🔐 Seguridad con JWT y roles — 10 puntos

| Indicador | Pts |
|---|---|
| `POST /auth/login` valida credenciales y devuelve un JWT con el rol; credenciales inválidas → `401` | 3 |
| Autenticación **stateless**: filtro que valida el token en cada solicitud, sin sesión; token alterado o vencido → `401` | 2 |
| Contraseñas con BCrypt (nunca en texto plano ni en el JSON), token que expira a los 60 min, clave leída de `application.properties` con la advertencia educativa | 2 |
| Autorización: catálogos solo `ADMIN`; un miembro solo accede a lo suyo; `RECEPCION` opera reservas; sin permiso → `403` | 3 |

| Nivel | Descriptor |
|---|---|
| 🟢 | Se demuestra con pruebas cada combinación rol/recurso relevante, incluidos `401` y `403` |
| 🔵 | Funciona para los casos principales; falta probar algún rol o el token alterado |
| 🟡 | Autentica, pero la autorización por rol o por dueño está incompleta |
| 🔴 | Los endpoints quedan abiertos, o la clave/contraseñas están expuestas |

## 7. 📚 Swagger, README y pruebas de aceptación — 10 puntos

| Indicador | Pts |
|---|---|
| Swagger UI muestra todos los endpoints, con resúmenes, códigos de respuesta (incluidos los de error) y ejemplos; permite autenticarse con el token | 4 |
| `README.md`: cómo ejecutar, usuarios de prueba y dónde está Swagger UI | 2 |
| Colección de Insomnia exportada con los 12 escenarios de aceptación y su resultado esperado | 4 |

| Nivel | Descriptor |
|---|---|
| 🟢 | Alguien que no conoce el proyecto lo ejecuta y reproduce los 12 escenarios siguiendo solo el README |
| 🔵 | Documentación correcta pero faltan ejemplos, o la colección cubre menos de 12 escenarios |
| 🟡 | Swagger sin configurar (nombres por defecto) o README incompleto |
| 🔴 | Sin documentación ni pruebas entregadas |

## ➖ Penalizaciones

Se descuentan del puntaje total:

| Situación | Puntos |
|---|---|
| El proyecto no arranca con `mvn spring-boot:run` | −20 |
| Clave de firma JWT, contraseñas reales o datos personales en el repositorio sin la advertencia educativa | −5 |
| No hay un commit por fase (historial de un solo commit) | −3 |
| Código copiado de otra persona o de la solución sin haberlo entendido (no puede explicar sus decisiones) | hasta −20 |

## ✅ Lista de autoevaluación rápida

Antes de entregar, confirmá que podés marcar todo:

- [ ] Arranca con `mvn spring-boot:run` y carga los datos de prueba.
- [ ] Los 12 escenarios de aceptación dan el resultado esperado.
- [ ] Seis solicitudes simultáneas a la misma sala y horario: solo una responde `201`.
- [ ] Cambiar el precio de un servicio no modifica el costo de reservas existentes.
- [ ] Ningún error devuelve el cuerpo por defecto de Spring (probá una ruta inexistente y un JSON roto).
- [ ] Con `show-sql=true`, listar reservas emite una sola consulta, no una por fila.
- [ ] Un miembro no puede leer la reserva de otro ni listar todas las reservas.
- [ ] El análisis, el ER y la matriz de trazabilidad coinciden con el código.
