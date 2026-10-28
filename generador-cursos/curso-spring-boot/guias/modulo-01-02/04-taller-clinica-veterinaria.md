# 🐾 Taller «Clínica veterinaria» — Enunciado

> 📌 **Taller para hacer desde cero.** Este documento contiene **solo el enunciado**: qué
> construir, con qué reglas y cómo se evalúa. No incluye código ni pasos de solución. Se
> aplica todo lo visto en el [Módulo 01](../../01-introduccion-a-spring-boot/) y el
> [Módulo 02](../../02-manejo-de-dependencias-y-java-beans/), y se espera trabajar con el mismo
> patrón del taller «Gestión de cursos».

<details>
<summary><strong>🎯 Objetivo</strong></summary>

> Construir **desde cero** una API REST con Spring Boot que administre **dueños, mascotas y
> citas** de una clínica veterinaria, organizada en las **tres capas del curso**
> (presentación, negocio y persistencia), con persistencia **en memoria** (listas), sin base
> de datos ni JPA.
>
> Al terminar, el estudiante:
>
> - Crea el proyecto con Spring Initializr y lo compila y ejecuta con `./mvnw`.
> - Separa responsabilidades en tres capas y respeta la regla de dependencia.
> - Declara beans con `@RestController`, `@Service` y `@Repository`, y los conecta con
>   **inyección por constructor** contra **interfaces**.
> - Modela relaciones entre entidades **por objetos** (dueño → mascota → cita).
> - Implementa reglas de negocio y validaciones con listas, ciclos, condicionales y contadores.
> - Señala los errores de negocio con excepciones estándar de Java.
> - Prueba la API con `curl` o Postman y conserva la evidencia.

</details>

<details>
<summary><strong>📝 Introducción: el problema y sus entidades</strong></summary>

> ### 🏥 El problema
>
> **PatitasVet** es una clínica veterinaria con un único consultorio. Hoy lleva todo en
> cuadernos y quiere una API REST sencilla para:
>
> - registrar a los **dueños** de las mascotas,
> - registrar las **mascotas** de cada dueño,
> - **agendar y cancelar citas**, sin que se crucen en el consultorio,
> - y consultar, para cada dueño, un **resumen** de lo que debe pagar por sus citas.
>
> El sistema se construye de forma progresiva, en cuatro fases.
>
> ### 🧩 Entidades
>
> | Entidad | Atributos | Observación |
> |---|---|---|
> | **Dueño** | `id`, `nombre`, `telefono`, `correo` | El `correo` identifica de forma única al dueño. |
> | **Mascota** | `id`, `nombre`, `especie`, `edad`, `dueno` | Pertenece a **un** dueño; se guarda el objeto `Dueño`, no su id. |
> | **Cita** | `id`, `mascota`, `fecha`, `hora`, `motivo`, `estado` | Pertenece a **una** mascota; `estado` es `AGENDADA` o `CANCELADA`. |
>
> ```mermaid
> classDiagram
>     direction LR
>     class Dueno {
>         -Long id
>         -String nombre
>         -String telefono
>         -String correo
>     }
>     class Mascota {
>         -Long id
>         -String nombre
>         -String especie
>         -int edad
>         -Dueno dueno
>     }
>     class Cita {
>         -Long id
>         -Mascota mascota
>         -LocalDate fecha
>         -int hora
>         -String motivo
>         -String estado
>     }
>     Dueno "1" --> "*" Mascota : tiene
>     Mascota "1" --> "*" Cita : recibe
> ```
>
> > 💡 Se sugiere `Dueno` (sin ñ) como nombre de clase y de paquete para evitar problemas con
> > la codificación de archivos.
>
> ### 💰 Tarifas por motivo de cita
>
> | Motivo | Tarifa |
> |---|---:|
> | `CONSULTA` | $ 40.000 |
> | `VACUNA` | $ 25.000 |
> | `CIRUGIA` | $ 300.000 |

</details>

<details>
<summary><strong>✅ Requerimientos funcionales</strong></summary>

> | Código | Fase | Requerimiento | Endpoint |
> |---|:-:|---|---|
> | **RF-01** | 1 | Registrar un dueño. | `POST /duenos` |
> | **RF-02** | 1 | Listar todos los dueños. | `GET /duenos` |
> | **RF-03** | 1 | Consultar un dueño por su identificador. | `GET /duenos/{id}` |
> | **RF-04** | 2 | Registrar una mascota de un dueño. | `POST /mascotas` |
> | **RF-05** | 2 | Listar las mascotas, con filtro opcional por especie. | `GET /mascotas` y `GET /mascotas?especie=perro` |
> | **RF-06** | 2 | Listar las mascotas de un dueño. | `GET /duenos/{id}/mascotas` |
> | **RF-07** | 3 | Agendar una cita. | `POST /citas` |
> | **RF-08** | 3 | Listar las citas de una mascota. | `GET /citas?mascotaId=1` |
> | **RF-09** | 3 | Cancelar una cita. | `PUT /citas/{id}/cancelar` |
> | **RF-10** | 4 | Consultar el **resumen de un dueño**: nombre, cantidad de mascotas, citas agendadas, subtotal, descuento y total a pagar. | `GET /duenos/{id}/resumen` |
>
> **Cuerpos de petición esperados** (los `id` viajan en el JSON; el servicio busca los objetos):
>
> | Endpoint | Cuerpo |
> |---|---|
> | `POST /duenos` | `{"nombre":"Marta Gómez","telefono":"3001234567","correo":"marta@correo.com"}` |
> | `POST /mascotas` | `{"nombre":"Rocky","especie":"perro","edad":4,"duenoId":1}` |
> | `POST /citas` | `{"mascotaId":1,"fecha":"2030-03-15","hora":9,"motivo":"CONSULTA"}` |

</details>

<details>
<summary><strong>📏 Reglas de negocio</strong></summary>

> | Código | Regla | Se aplica en | Si se incumple lanza |
> |---|---|---|:-:|
> | **RN-01** | El identificador de toda entidad se genera automáticamente (1, 2, 3…). | RF-01, RF-04, RF-07 | — |
> | **RN-02** | No puede haber dos dueños con el mismo correo (sin distinguir mayúsculas). | RF-01 | `IllegalStateException` |
> | **RN-03** | El teléfono solo puede contener **dígitos** y tener entre **7 y 10** caracteres. | RF-01 | `IllegalArgumentException` |
> | **RN-04** | El nombre del dueño y el correo no pueden estar vacíos; el correo debe contener `@`. | RF-01 | `IllegalArgumentException` |
> | **RN-05** | Un dueño, mascota o cita inexistente responde «no encontrado». | RF-03, RF-04, RF-06 a RF-10 | `NoSuchElementException` |
> | **RN-06** | La especie debe ser `perro`, `gato`, `ave` o `conejo` (sin distinguir mayúsculas). | RF-04 | `IllegalArgumentException` |
> | **RN-07** | La edad de la mascota debe estar entre **0 y 30** años. | RF-04 | `IllegalArgumentException` |
> | **RN-08** | Un dueño no puede tener **dos mascotas con el mismo nombre** (sin distinguir mayúsculas). | RF-04 | `IllegalStateException` |
> | **RN-09** | El motivo de la cita debe ser `CONSULTA`, `VACUNA` o `CIRUGIA`. | RF-07 | `IllegalArgumentException` |
> | **RN-10** | La hora debe ser un entero entre **8 y 17** (la clínica atiende de 8:00 a 17:00). | RF-07 | `IllegalArgumentException` |
> | **RN-11** | La fecha de la cita **no puede ser anterior a hoy**. | RF-07 | `IllegalArgumentException` |
> | **RN-12** | El consultorio es único: no puede haber **dos citas `AGENDADA`** en la misma fecha y hora. | RF-07 | `IllegalStateException` |
> | **RN-13** | Una mascota no puede tener **más de 2 citas `AGENDADA`** a la vez. | RF-07 | `IllegalStateException` |
> | **RN-14** | La cita se registra como `AGENDADA` **solo si se cumplen todas** las reglas anteriores, evaluadas en el orden que se indica abajo. | RF-07 | — |
> | **RN-15** | Solo se puede cancelar una cita que esté `AGENDADA`; al cancelarla, la franja (fecha y hora) **queda libre**. | RF-09 | `IllegalStateException` |
> | **RN-16** | Subtotal = suma de las tarifas de las citas `AGENDADA` de **todas las mascotas del dueño**. | RF-10 | — |
> | **RN-17** | Descuento del **10 %** sobre el subtotal si el dueño tiene **3 o más mascotas**; si no, no hay descuento. Total = subtotal − descuento. | RF-10 | — |
>
> **Orden de los controles al agendar una cita (RF-07):**
>
> ```text
> POST /citas
>    ├─ 1. ¿Existe la mascota?                    no ─▶ RN-05
>    ├─ 2. ¿El motivo es válido?                  no ─▶ RN-09
>    ├─ 3. ¿La hora está entre 8 y 17?            no ─▶ RN-10
>    ├─ 4. ¿La fecha es de hoy en adelante?       no ─▶ RN-11
>    ├─ 5. ¿La mascota ya tiene 2 citas agendadas? sí ─▶ RN-13
>    ├─ 6. ¿La franja (fecha + hora) está ocupada? sí ─▶ RN-12
>    └─ 7. Registrar la cita como AGENDADA         ───▶ 201
> ```

</details>

<details>
<summary><strong>🛠️ Requerimientos no funcionales y restricciones</strong></summary>

> | Código | Requerimiento |
> |---|---|
> | **RNF-01** | Tecnologías: Java 21 o superior, Spring Boot 3 y Maven (se ejecuta con `./mvnw`). |
> | **RNF-02** | **Sin base de datos ni JPA**: los datos viven en listas en memoria y se pierden al reiniciar. |
> | **RNF-03** | Organización en **tres capas** (presentación, negocio y persistencia), con la regla de dependencia presentación → negocio → persistencia. |
> | **RNF-04** | Las dependencias se resuelven con **inyección por constructor**; los repositorios se usan a través de **interfaces** y las implementaciones en memoria llevan el sufijo `EnMemoria`. |
> | **RNF-05** | Las reglas de negocio viven **solo en los servicios**. El controlador no lleva `if` de negocio. |
> | **RNF-06** | Los errores de negocio se señalan con excepciones estándar de Java (`NoSuchElementException`, `IllegalArgumentException`, `IllegalStateException`). Sin manejador de errores, Spring los responde como `500`. Los creados responden `201`. |
> | **RNF-07** | Las **peticiones** (`SolicitudMascota`, `SolicitudCita`) son clases separadas de las **entidades**; el controlador las convierte en llamadas al servicio con identificadores. |
> | **RNF-08** | Los valores fijos (hora mínima y máxima, edad máxima, tarifas, porcentaje de descuento) se declaran como **constantes con nombre**, no como números sueltos dentro de los `if`. |
> | **RNF-09** | No se permiten dependencias circulares entre servicios. |
> | **RNF-10** | La API se prueba con `curl` o Postman y se conservan **evidencias** de cada escenario. |
>
> > 💡 **Alcance.** Quedan fuera: autenticación, base de datos, validación con anotaciones,
> > paginación, concurrencia y un manejador global de errores. Se verán en módulos posteriores.

</details>

<details>
<summary><strong>🏗️ Arquitectura esperada</strong></summary>

> Se debe respetar este árbol de paquetes (solo se indican nombres sugeridos; las clases se
> escriben desde cero):
>
> ```text
> 📁 src/main/java/com/clinica
> ├── 📄 ClinicaApplication.java           (la genera Spring Initializr)
> ├── 📁 presentacion
> │   └── 📁 controlador   ControladorDuenos · ControladorMascotas · ControladorCitas
> │                        SolicitudMascota · SolicitudCita
> ├── 📁 negocio
> │   ├── 📁 model         Dueno · Mascota · Cita · ResumenDueno
> │   └── 📁 service       ServicioDuenos · ServicioMascotas · ServicioCitas
> └── 📁 persistence
>     └── 📁 repository    RepositorioDuenos · RepositorioMascotas · RepositorioCitas
>                          y sus implementaciones «…EnMemoria»
> ```
>
> | Capa | Responsabilidad |
> |---|---|
> | **Presentación** | Recibe la petición HTTP, entrega los datos al servicio y devuelve la respuesta. **Sin reglas.** |
> | **Negocio** | Las reglas: validaciones, duplicados, límites, cálculo del resumen. Lanza excepciones estándar. |
> | **Persistencia** | Guarda y busca datos. La **interfaz** es el contrato y la clase `…EnMemoria` lo cumple con una `List`. |
>
> **Preguntas para decidir antes de escribir código** (se responden en el informe):
>
> 1. ¿En qué capa vive cada regla RN-01 a RN-17?
> 2. ¿Qué servicio necesita a cuál? Dibujar el grafo de dependencias y comprobar que no tiene
>    ciclos. Pista: el resumen del dueño necesita datos de mascotas y de citas.
> 3. ¿Qué operaciones debe ofrecer cada repositorio para que los servicios no tengan que
>    conocer cómo se guardan los datos?

</details>

<details>
<summary><strong>🪜 Fases del taller</strong></summary>

> | Fase | Qué se construye | Requerimientos | Reglas |
> |:-:|---|---|---|
> | **0** | Proyecto Spring Boot con Maven, paquetes de las tres capas y primera ejecución | — | — |
> | **1** | **Dueños**: modelo, repositorio, servicio y controlador | RF-01 a RF-03 | RN-01 a RN-05 |
> | **2** | **Mascotas**: relación con el dueño, filtro por especie y listado por dueño | RF-04 a RF-06 | RN-05 a RN-08 |
> | **3** | **Citas**: agendar con todos los controles, listar y cancelar | RF-07 a RF-09 | RN-05, RN-09 a RN-15 |
> | **4** | **Resumen del dueño**: cálculo con tarifas y descuento | RF-10 | RN-16, RN-17 |
>
> **Fase 0 — Preparación.** Crear el proyecto en [start.spring.io](https://start.spring.io)
> con Maven, Java 21 o superior y la dependencia **Spring Web**. Grupo `com.clinica`,
> artefacto `clinica`. Crear los paquetes de las tres capas y comprobar que
> `./mvnw spring-boot:run` arranca con el mensaje `Started ClinicaApplication`.
>
> **Forma de trabajo.** En cada fase se construye en el mismo orden: **modelo → contrato del
> repositorio → implementación en memoria → servicio con las reglas → controlador**. Al final
> de cada fase se prueba con `curl` antes de pasar a la siguiente.

</details>

<details>
<summary><strong>🧪 Casos mínimos de prueba</strong></summary>

> Con la aplicación recién arrancada, la API debe comportarse así (el resultado `500`
> corresponde a un error de negocio sin manejador):
>
> | # | Escenario | Petición | Esperado |
> |:-:|---|---|---|
> | 1 | Registrar dueño | `POST /duenos` Marta | `201` |
> | 2 | Correo repetido (otra mayúscula) | `POST /duenos` con `MARTA@correo.com` | `500` · `IllegalStateException` |
> | 3 | Teléfono con letras | `POST /duenos` con `"telefono":"30012ab"` | `500` · `IllegalArgumentException` |
> | 4 | Dueño inexistente | `GET /duenos/99` | `500` · `NoSuchElementException` |
> | 5 | Registrar mascota | `POST /mascotas` Rocky, perro, 4, dueño 1 | `201` |
> | 6 | Especie inválida | `POST /mascotas` con `"especie":"dragón"` | `500` · `IllegalArgumentException` |
> | 7 | Edad fuera de rango | `POST /mascotas` con `"edad":31` | `500` · `IllegalArgumentException` |
> | 8 | Nombre repetido en el mismo dueño | `POST /mascotas` «rocky», dueño 1 | `500` · `IllegalStateException` |
> | 9 | Filtrar por especie | `GET /mascotas?especie=PERRO` | `200` con Rocky |
> | 10 | Agendar cita válida | `POST /citas` mascota 1, hora 9, `CONSULTA` | `201` |
> | 11 | Hora fuera de horario | `POST /citas` con `"hora":18` | `500` · `IllegalArgumentException` |
> | 12 | Fecha pasada | `POST /citas` con una fecha anterior a hoy | `500` · `IllegalArgumentException` |
> | 13 | Franja ocupada | `POST /citas` misma fecha y hora que la 10 | `500` · `IllegalStateException` |
> | 14 | Tercera cita de la misma mascota | agendar 2 citas más en horas distintas | la 2.ª `201`; la 3.ª `500` · `IllegalStateException` |
> | 15 | Cancelar | `PUT /citas/1/cancelar` | `200` con estado `CANCELADA` |
> | 16 | Cancelar otra vez | `PUT /citas/1/cancelar` | `500` · `IllegalStateException` |
> | 17 | Franja liberada | agendar de nuevo la fecha y hora de la cita 1 | `201` |
> | 18 | Resumen sin descuento | `GET /duenos/1/resumen` con 1 mascota | `200`; descuento `0` |
> | 19 | Resumen con descuento | dueño con 3 mascotas y citas `CONSULTA` + `VACUNA` | `200`; descuento = 10 % de $ 65.000 = $ 6.500; total $ 58.500 |

</details>

<details>
<summary><strong>📦 Entregable</strong></summary>

> 1. **Proyecto** Maven completo que compile y arranque con `./mvnw spring-boot:run`, con la
>    estructura de paquetes de la sección «Arquitectura esperada».
> 2. **Evidencias**: capturas (o un archivo `evidencias.md` con los comandos y sus
>    respuestas) de los 19 casos mínimos de prueba.
> 3. **Informe breve** (`informe.md`) con:
>    - la tabla «regla de negocio → clase y método donde se cumple»,
>    - el grafo de dependencias de los servicios,
>    - la respuesta a la pregunta: *si mañana los datos pasaran a una base de datos, ¿qué
>      capa cambia y cuáles no? ¿por qué?*,
>    - una lista de las **anotaciones** usadas, con una frase de para qué sirve cada una.
>
> ```text
> 📁 clinica
> ├── 📄 pom.xml
> ├── 📁 src/main/java/com/clinica      (las tres capas)
> ├── 📄 evidencias.md
> └── 📄 informe.md
> ```

</details>

<details>
<summary><strong>📊 Criterios de evaluación</strong></summary>

> | Criterio | Peso |
> |---|:-:|
> | **Fase 1 · Dueños** completa y con sus reglas (RN-01 a RN-05) | 15 % |
> | **Fase 2 · Mascotas**: relación por objetos, filtro y listado por dueño | 15 % |
> | **Fase 3 · Citas**: los siete controles en el orden indicado, cancelación y franja liberada | 30 % |
> | **Fase 4 · Resumen**: subtotal, descuento y total correctos | 10 % |
> | **Arquitectura**: tres capas, interfaces, inyección por constructor, sin reglas en controladores, sin ciclos | 15 % |
> | **Calidad del código**: constantes con nombre, mensajes claros, nombres consistentes | 5 % |
> | **Evidencias e informe** | 10 % |

</details>

<details>
<summary><strong>🏁 Retos finales (para quien termine antes)</strong></summary>

> 1. **Cambio de almacenamiento:** crear una segunda implementación de `RepositorioMascotas`
>    con un `Map<Long, Mascota>` y comprobar que **ningún servicio cambia**.
> 2. **Citas del día:** agregar `GET /citas?fecha=2030-03-15` con las citas agendadas de esa
>    fecha, ordenadas por hora.
> 3. **Franjas libres:** agregar `GET /citas/disponibles?fecha=2030-03-15` con las horas
>    libres entre 8 y 17.
> 4. **Mascota más atendida:** que el resumen del dueño incluya el nombre de la mascota con
>    más citas agendadas. ¿Qué pasa si hay un empate?
> 5. **Tarifa configurable:** leer el porcentaje de descuento de `application.properties`
>    con `@Value`.

</details>
