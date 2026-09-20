# 🧭 Solución paso a paso — Proyecto 01: CoWorkHub

Esta carpeta contiene la **solución completa del [enunciado](../enunciado.md)**, escrita
para que puedas **transcribirla y construirla vos mismo, fase por fase**, entendiendo qué
hace cada archivo y por qué. Si no lograste resolver el proyecto por tu cuenta, o te
bloqueaste en una parte, está pensada para ayudarte a retomar y mejorar.

> ⚠️ **Usala como ayuda, no como atajo.** El objetivo no es tener el proyecto terminado:
> es que aprendas a construirlo. Antes de abrir una fase, intentá resolverla solo; si te
> trabás, leé **solo esa fase**, cerrala y volvé a tu propio código.

## ✅ Está verificada

Todo el código de estas páginas salió de un proyecto que **compila, arranca y pasa las
pruebas de aceptación** (los 12 escenarios del enunciado, más casos límite, errores de
validación, seguridad por rol y concurrencia). Además, se reconstruyó el proyecto
siguiendo estas páginas *en el orden en que aparecen* y el resultado fue idéntico al
proyecto verificado. Si algo no te funciona, la causa está en una transcripción (un
`import`, un nombre, una llave), no en la guía: comparalo carácter por carácter.

## 🗺️ El recorrido

| # | Fase | Qué construís | Módulos | Verificás con |
|---|---|---|---|---|
| 0 | [Análisis](00-analisis.md) | Actores, glosario, diagrama ER, trazabilidad, supuestos | — | Tu documento vs. el de la guía |
| 1 | [Proyecto base](01-fase-1-proyecto-base.md) | Proyecto Maven, paquetes, `Clock` y `PasswordEncoder` | 01, 02 | La aplicación arranca |
| 2 | [Modelo de datos](02-fase-2-modelo-de-datos.md) | 9 entidades, relaciones, 8 repositorios, datos semilla | 03, 04 | Tablas y datos en la consola H2 |
| 3a | [Catálogos](03a-fase-3-catalogos.md) | Excepciones, DTOs, servicios y controladores de catálogos | 05 | `curl` a `/api/sedes`, `/api/salas`… |
| 3b | [Miembros y disponibilidad](03b-fase-3-miembros.md) | Miembros con usuario, consumo mensual, disponibilidad | 05 | `curl` a miembros y disponibilidad |
| 3c | [Reservas](03c-fase-3-reservas.md) | Calculadora de costos, reglas RN-01…RN-10, reservas de ejemplo | 05 | Crear, servicios, confirmar, N+1 |
| 4 | [Excepciones](04-fase-4-excepciones.md) | `@ControllerAdvice` y cuerpo de error único | 07 | Todos los errores con su código |
| 5 | [Swagger](05-fase-5-swagger.md) | OpenAPI, `@Tag`, `@Operation`, `@Schema`, errores comunes | 06 | Swagger UI completa |
| 6 | [Seguridad con JWT](06-fase-6-seguridad-jwt.md) | Login, filtro JWT, roles y dueño del recurso | 08 | Matriz de permisos `401`/`403` |
| 7 | [Pruebas y README](07-pruebas-de-aceptacion-y-readme.md) | 12 escenarios, colección de Insomnia, `README.md` | todos | Los 12 escenarios |

Hacé **un commit al terminar cada fase** (cada documento sugiere el mensaje).

## 🧰 Cómo usar estas páginas

1. **Leé el objetivo** de la fase y la sección "Qué vas a lograr".
2. **Seguí los pasos en orden.** Cada bloque de código indica arriba su **ruta exacta**
   (`📄 src/main/java/...`): creá el archivo ahí. Si dice *(fragmento)*, no reemplaces el
   archivo entero: agregá o cambiá solo esas líneas, donde se indica.
3. **Escribí el código en vez de pegarlo.** Es más lento, pero te obliga a leer cada línea,
   y así aprendés. Tu IDE te ayudará con los `import` (en la mayoría de los IDE, `Alt+Enter`
   o `Ctrl+.` sobre el nombre en rojo).
4. **No avances sin pasar el checkpoint** (✅) de cada fase. Un error que no arreglás ahora
   se multiplica en las fases siguientes.
5. Al final de cada fase, **compará tu proyecto** con lo que dice la guía y hacé el commit.

## 🔑 Datos que vas a necesitar

| Dato | Valor |
|---|---|
| URL base | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/doc/swagger-ui.html` (desde la Fase 5) |
| Usuarios de prueba (desde la Fase 2) | `admin`/`admin123` · `recepcion`/`recep123` · `ana`/`ana123` · `luis`/`luis123` |
| Reloj para pruebas | `coworkhub.reloj.fijo=2030-06-03T08:00:00` en `application.properties` |
| Ejecutar | `mvn spring-boot:run` |

## 📚 Convenciones de estas páginas

- 📄 marca la **ruta** de cada archivo. `(fragmento)` indica que es solo una parte.
- Los bloques `bash` con `curl` se pueden ejecutar en Linux, macOS o Git Bash; en
  Insomnia son las mismas solicitudes.
- Las respuestas JSON de ejemplo están **formateadas** para leerlas mejor; la API las
  devuelve en una sola línea. Los `timestamp` cambian en cada llamada.
- Cuando una respuesta trae más campos de los que interesan, se muestran solo los
  relevantes.
- Las fases 3 a 6 **modifican archivos de fases anteriores**; la guía te lo indica
  siempre con el nombre del archivo y qué cambiar.

## 🗃️ Índice de archivos por documento

| Documento | Archivos que se crean |
|---|---|
| [01-fase-1-proyecto-base.md](01-fase-1-proyecto-base.md) | `pom.xml` · `Main.java` · `application.properties` · `config/ConfiguracionBeans.java` · `.gitignore` |
| [02-fase-2-modelo-de-datos.md](02-fase-2-modelo-de-datos.md) | `persistences/entities/TipoSala.java` · `persistences/entities/EstadoMiembro.java` · `persistences/entities/EstadoReserva.java` · `security/persistences/entities/Rol.java` · `persistences/entities/Sede.java` · `persistences/entities/Equipamiento.java` · `persistences/entities/Sala.java` · `persistences/entities/PlanMembresia.java` · `security/persistences/entities/Usuario.java` · `persistences/entities/Miembro.java` · `persistences/entities/ServicioAdicional.java` · `persistences/entities/Reserva.java` · `persistences/entities/DetalleReserva.java` · `persistences/repositories/RepositorioSedes.java` · `persistences/repositories/RepositorioEquipamientos.java` · `persistences/repositories/RepositorioServiciosAdicionales.java` · `persistences/repositories/RepositorioPlanes.java` · `persistences/repositories/RepositorioMiembros.java` · `persistences/repositories/RepositorioSalas.java` · `persistences/repositories/RepositorioReservas.java` · `security/persistences/repositories/RepositorioUsuarios.java` · `config/CargadorCatalogos.java` |
| [03a-fase-3-catalogos.md](03a-fase-3-catalogos.md) | `exception/RecursoNoEncontradoException.java` · `exception/ReglaNegocioException.java` · `exception/SolicitudInvalidaException.java` · `dto/SolicitudSede.java` · `dto/SolicitudEquipamiento.java` · `dto/SolicitudServicioAdicional.java` · `dto/SolicitudPlan.java` · `dto/SolicitudSala.java` · `dto/SolicitudMiembro.java` · `dto/SolicitudActualizarMiembro.java` · `dto/ItemServicio.java` · `dto/SolicitudReserva.java` · `dto/ResumenConsumo.java` · `services/Validaciones.java` · `services/ServicioSedes.java` · `controllers/ControladorSedes.java` · `services/ServicioEquipamientos.java` · `controllers/ControladorEquipamientos.java` · `services/ServicioServiciosAdicionales.java` · `controllers/ControladorServiciosAdicionales.java` · `services/ServicioPlanes.java` · `controllers/ControladorPlanes.java` · `services/ServicioSalas.java` · `controllers/ControladorSalas.java` |
| [03b-fase-3-miembros.md](03b-fase-3-miembros.md) | `services/ServicioMiembros.java` · `services/ServicioConsumo.java` · `controllers/ControladorMiembros.java` · `services/ServicioDisponibilidad.java` · `controllers/ControladorDisponibilidad.java` |
| [03c-fase-3-reservas.md](03c-fase-3-reservas.md) | `services/CalculadoraCostoReserva.java` · `services/ServicioReservas.java` · `controllers/ControladorReservas.java` · `config/CargadorReservasDemo.java` |
| [04-fase-4-excepciones.md](04-fase-4-excepciones.md) | `exception/RespuestaError.java` · `exception/ManejadorGlobalDeExcepciones.java` |
| [05-fase-5-swagger.md](05-fase-5-swagger.md) | `config/ConfiguracionOpenApi.java` |
| [06-fase-6-seguridad-jwt.md](06-fase-6-seguridad-jwt.md) | `dto/SolicitudLogin.java` · `dto/RespuestaLogin.java` · `security/jwt/UtilJwt.java` · `security/services/ServicioDetallesUsuario.java` · `security/services/ServicioAutorizacion.java` · `security/jwt/FiltroAutenticacionJwt.java` · `security/config/PuntoEntradaJwt.java` · `security/config/ManejadorAccesoDenegado.java` · `security/config/Permisos.java` · `security/config/ConfiguracionSeguridad.java` · `security/controllers/ControladorAutenticacion.java` |

**Total: 74 archivos.**

## 🏗️ Vista de la arquitectura final

```mermaid
flowchart LR
    C(["Cliente<br/>Insomnia / Swagger"]) --> SEC["Seguridad<br/>FiltroAutenticacionJwt<br/>+ ConfiguracionSeguridad"]
    SEC --> CTRL["controllers<br/>(HTTP)"]
    CTRL --> SVC["services<br/>(reglas de negocio)<br/>+ CalculadoraCostoReserva"]
    SVC --> REP["persistences.repositories<br/>(Spring Data JPA)"]
    REP --> ENT["persistences.entities"]
    ENT --> DB[("H2 en memoria")]
    SVC -. lanza .-> EXC["exception<br/>(reglas violadas)"]
    EXC -. traduce a HTTP .-> ADV["ManejadorGlobalDeExcepciones<br/>@ControllerAdvice"]
    ADV -.-> C
```

Un controlador **nunca** usa un repositorio; un servicio **nunca** conoce HTTP.

**Empezá por →** [Fase 0 — Análisis](00-analisis.md)
