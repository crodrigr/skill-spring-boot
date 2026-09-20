# 🏆 Proyecto Integrador 01 — CoWorkHub: reservas de salas y espacios de trabajo

> **Nivel**: alto · **Módulos que integra**: 01 al 08 · **Trabajo**: individual
> **Cómo se evalúa**: [Rúbrica](rubrica.md) · **Ayuda**: [Solución paso a paso](solucion/README.md)
> (usala solo después de haberlo intentado por tu cuenta).

## 🎯 Objetivo

Construir de principio a fin una API REST con Spring Boot que resuelva un
problema de negocio realista: analizarlo, especificarlo (requisitos y reglas de
negocio), modelarlo con JPA, exponerlo con controladores REST, manejar sus errores,
documentarlo con Swagger y protegerlo con Spring Security y JWT.

## 1. Situación

**CoWorkHub** es una red de espacios de coworking con varias sedes. Hoy gestiona las
reservas de salas en una hoja de cálculo compartida, y eso le genera problemas:

- Dos personas reservan la misma sala en el mismo horario.
- Nadie sabe cuántas horas de su plan ha consumido cada miembro en el mes.
- Las cancelaciones se cobran de forma distinta según quién atienda.
- Cualquier empleado puede modificar cualquier dato.

La empresa te contrata para construir el **backend (API REST)** de un sistema que
resuelva estos problemas. No se pide interfaz gráfica: el sistema se prueba con
Insomnia y Swagger UI.

## 2. Análisis esperado (primera entrega)

Antes de programar, entregá un documento de análisis con:

1. **Actores y casos de uso.** Actores: Administrador, Recepcionista y Miembro.
   Diagrama y descripción breve de cada caso de uso.
2. **Glosario del dominio.** Definí reserva, plan, horas incluidas, horas excedentes,
   solapamiento y cargo por cancelación.
3. **Modelo de dominio (diagrama entidad-relación).** Justificá qué lado es el dueño
   de cada relación y qué `fetch` y `cascade` elegiste.
4. **Matriz de trazabilidad.** Relacioná cada RF con las RN y los endpoints que lo
   implementan.
5. **Supuestos y decisiones.** Toda ambigüedad del enunciado que resuelvas por tu
   cuenta debe quedar registrada.
6. **Fuera de alcance.** Pagos en línea, notificaciones por correo, interfaz gráfica y
   facturación fiscal.

## 3. Modelo de dominio mínimo

| Entidad | Atributos principales | Relaciones que debe modelar |
|---|---|---|
| `Sede` | nombre, ciudad, dirección, horaApertura, horaCierre | 1 → N `Sala` |
| `Sala` | nombre, tipo (`SALA_REUNION`, `OFICINA_PRIVADA`, `ESCRITORIO`), capacidad, tarifaPorHora, activa | N → 1 `Sede`; **N ↔ N** `Equipamiento` |
| `Equipamiento` | nombre (proyector, pizarra, videoconferencia…) | N ↔ N `Sala` |
| `PlanMembresia` | nombre, horasIncluidasMes, descuentoExcedente (%), maxReservasActivas | 1 → N `Miembro` |
| `Miembro` | documento (único), nombre, email (único), estado (`ACTIVO`, `SUSPENDIDO`) | N → 1 `PlanMembresia`; **1 ↔ 1** `Usuario` |
| `Usuario` | nombreUsuario, contraseña codificada, rol (`ADMIN`, `RECEPCION`, `MIEMBRO`) | 1 ↔ 1 `Miembro` (opcional para el personal) |
| `Reserva` | inicio, fin, asistentes, estado, costoTotal, cargoCancelacion | N → 1 `Sala`; N → 1 `Miembro`; 1 → N `DetalleReserva` |
| `ServicioAdicional` | nombre (catering, impresión, soporte técnico…), precioUnitario | 1 → N `DetalleReserva` |
| `DetalleReserva` | cantidad, precioUnitarioAplicado | Entidad intermedia entre `Reserva` y `ServicioAdicional`. Con `cascade` y `orphanRemoval` desde `Reserva`. |

## 4. Reglas de negocio

| Código | Regla |
|---|---|
| **RN-01** | Una sala no puede tener dos reservas `PENDIENTE` o `CONFIRMADA` que se solapen. Hay solapamiento si `inicioNuevo < finExistente` y `finNuevo > inicioExistente`. Si una reserva termina justo cuando empieza otra, **no** hay solapamiento. |
| **RN-02** | La duración mínima es de 30 min, la máxima de 8 h y debe ser múltiplo de 30 min. La reserva debe empezar y terminar el mismo día. |
| **RN-03** | La reserva debe caer dentro del horario de la sede, debe ser futura y hacerse con al menos 1 h de anticipación. |
| **RN-04** | No se puede reservar una sala inactiva. Los asistentes no pueden superar la capacidad de la sala. |
| **RN-05** | Solo un miembro `ACTIVO` puede reservar. |
| **RN-06** | Un miembro no puede tener más reservas activas futuras que `maxReservasActivas` de su plan. |
| **RN-07** | **Costo.** Las horas del mes ya cubiertas por `horasIncluidasMes` del plan no se cobran. Las horas excedentes se cobran a `tarifaPorHora × (1 − descuentoExcedente)`. Los servicios adicionales suman `cantidad × precioUnitario`. Al crear el detalle, `precioUnitarioAplicado` guarda el precio vigente en ese momento. |
| **RN-08** | **Cancelación.** Con 24 h o más de anticipación no hay cargo y las horas incluidas se devuelven al consumo del mes. Entre 24 h y 2 h se cobra el 50 % del costo de la sala. Con menos de 2 h, o si la reserva ya empezó, no se puede cancelar. |
| **RN-09** | **Estados.** `PENDIENTE → CONFIRMADA → COMPLETADA`. También `PENDIENTE/CONFIRMADA → CANCELADA`. Las transiciones son irreversibles y cualquier otra se rechaza. |
| **RN-10** | Los servicios adicionales solo se pueden agregar o quitar mientras la reserva esté `PENDIENTE`. |
| **RN-11** | Son únicos el documento y el email del miembro, y el nombre de la sala dentro de su sede. |
| **RN-12** | No se puede eliminar una sala, un plan ni un miembro que tenga reservas activas o futuras asociadas. |
| **RN-13** | Un miembro solo ve y opera sus propias reservas. `RECEPCION` opera todas. Solo `ADMIN` gestiona catálogos (sedes, salas, planes, servicios). |

## 5. Requisitos funcionales

**Catálogos**

- **RF-01** CRUD de sedes, salas (con su equipamiento), equipamiento, servicios adicionales y planes.

**Miembros**

- **RF-02** Registrar un miembro junto con su `Usuario`, y consultarlo, actualizarlo o suspenderlo.

**Disponibilidad**

- **RF-03** Listar las salas disponibles de una sede para una fecha y un rango horario, con capacidad mínima y equipamiento requerido como filtros opcionales.

**Reservas**

- **RF-04** Crear una reserva con sus servicios adicionales. La respuesta incluye el costo calculado.
- **RF-05** Consultar una reserva por id, y listar reservas por miembro, por sala y fecha, y por estado.
- **RF-06** Agregar o quitar servicios de una reserva `PENDIENTE`, con recálculo del costo.
- **RF-07** Confirmar una reserva (recepción o admin).
- **RF-08** Cancelar una reserva aplicando RN-08, con el cargo en la respuesta.
- **RF-09** Marcar una reserva como completada (solo recepción o admin).
- **RF-10** Consultar el resumen mensual de consumo de un miembro: horas incluidas, usadas y restantes, y horas excedentes.

**Seguridad y documentación**

- **RF-11** `POST /auth/login` devuelve un JWT con el rol del usuario.
- **RF-12** Todos los endpoints, excepto el login y Swagger UI, exigen un JWT válido. El acceso se restringe según RN-13.
- **RF-13** Toda la API queda documentada en Swagger UI, con códigos de respuesta y ejemplos.

## 6. Requisitos no funcionales

| Código | Categoría | Requisito |
|---|---|---|
| **RNF-01** | Tecnología | Java 17, Spring Boot 3.x, Maven, Spring Data JPA y H2 en memoria. Debe ejecutarse solo con `mvn spring-boot:run`. |
| **RNF-02** | Arquitectura | Arquitectura MVC por capas, con los paquetes `controllers`, `services` y `persistences` (`entities`, `repositories`). `exception`, `config` y `jwt` son paquetes transversales. El controlador solo depende del servicio, y el servicio solo del repositorio. |
| **RNF-03** | Diseño | Toda la lógica de negocio vive en `services`. La inyección de dependencias es por constructor. El cálculo de costos es un bean propio (`@Component`) inyectado en el servicio de reservas. |
| **RNF-04** | Consistencia | Las operaciones que escriben más de una entidad (crear o cancelar una reserva) son transaccionales. Una falla no deja datos a medias. |
| **RNF-05** | Errores | Todas las respuestas de error usan el mismo cuerpo JSON: `{"error": "...", "codigo": "...", "timestamp": "..."}`. Se genera desde un único `@ControllerAdvice`. Ningún error debe responder `500` por una regla de negocio. |
| **RNF-06** | Códigos HTTP | `200` y `201` para éxito, `400` para solicitud mal formada o datos inválidos, `401` sin autenticación, `403` sin permiso, `404` recurso inexistente, `409` violación de regla de negocio o duplicado. |
| **RNF-07** | Seguridad | Autenticación stateless con JWT y contraseñas codificadas con BCrypt. El token expira a los 60 min. La clave de firma se lee de `application.properties`, con la advertencia de que es solo educativa. |
| **RNF-08** | Rendimiento | Las colecciones usan `fetch = LAZY`. Los listados no provocan consultas repetidas por cada fila (problema N+1). Justificá en el análisis cualquier excepción. |
| **RNF-09** | Datos de prueba | Al arrancar, un `CommandLineRunner` carga al menos 2 sedes, 6 salas, 3 planes, 5 miembros, 3 usuarios (uno por rol) y 15 reservas en distintos estados. |
| **RNF-10** | Mantenibilidad | Nombres en español y coherentes con el resto del curso. Sin código duplicado entre servicios. Cada clase tiene una sola responsabilidad. |
| **RNF-11** | Documentación | El `README.md` explica cómo ejecutar el proyecto, qué usuarios de prueba existen y dónde está Swagger UI. |

## 7. Estructura esperada

```text
📁 coworkhub
└── 📁 src/main/java/com/coworkhub
    ├── 📄 Main.java
    ├── 📁 controllers      → Sedes, Salas, Miembros, Reservas, Disponibilidad, Autenticación
    ├── 📁 services         → Lógica de negocio + CalculadoraCostoReserva
    ├── 📁 persistences
    │   ├── 📁 entities
    │   └── 📁 repositories
    ├── 📁 exception        → Excepciones de negocio + ManejadorGlobalDeExcepciones
    └── 📁 security
        ├── 📁 controllers  📁 services  📁 persistences
        ├── 📁 config       📁 jwt
```

## 8. Fases de trabajo

| Fase | Contenido | Módulos que se aplican |
|---|---|---|
| 0 | Análisis, diagrama ER y trazabilidad | — |
| 1 | Proyecto base, beans y configuración | 01, 02 |
| 2 | Entidades, relaciones (1↔1, 1→N, N↔N con entidad intermedia), `cascade`, `orphanRemoval` y `fetch`. Datos semilla. | 03, 04 |
| 3 | Servicios, reglas de negocio y controladores REST | 05 |
| 4 | Excepciones de negocio y manejador global | 07 |
| 5 | Documentación con Swagger | 06 |
| 6 | Spring Security con JWT y roles | 08 |

## 9. Entregables

1. Documento de análisis (punto 2).
2. Proyecto completo en un repositorio, con un commit por fase.
3. Colección de pruebas de Insomnia (exportada) con los escenarios de aceptación.
4. `README.md`.

## 10. Escenarios de aceptación mínimos

| # | Escenario | Resultado esperado |
|---|---|---|
| 1 | Reservar una sala libre con 2 servicios adicionales | `201` con costo calculado |
| 2 | Reservar una sala en un horario que se solapa con otra reserva | `409` |
| 3 | Reservar de 10:00 a 11:00 con otra reserva de 09:00 a 10:00 | `201` (los extremos no se solapan) |
| 4 | Reservar 45 min, o fuera del horario de la sede | `409` con mensaje claro |
| 5 | Cancelar con 30 h de anticipación | `200`, cargo 0, horas devueltas |
| 6 | Cancelar con 5 h de anticipación | `200`, cargo del 50 % |
| 7 | Cancelar con 1 h de anticipación | `409` |
| 8 | Confirmar una reserva ya cancelada | `409` |
| 9 | Un miembro consulta la reserva de otro miembro | `403` |
| 10 | Llamar a un endpoint protegido sin token, o con un token alterado | `401` |
| 11 | Eliminar una sala con reservas futuras | `409` |
| 12 | Consultar el resumen mensual de un miembro con 3 h de reservas y un plan de 2 h incluidas | 2 h incluidas usadas, 1 h excedente |

## 11. Criterios de evaluación

| Criterio | Peso |
|---|---|
| Análisis: trazabilidad, ER justificado y supuestos explícitos | 15 % |
| Modelo JPA: relaciones, lado dueño, `cascade` y `fetch` correctos | 15 % |
| Reglas de negocio: RN-01 a RN-13 implementadas y probadas | 25 % |
| Arquitectura MVC y calidad del código | 15 % |
| Manejo de excepciones y códigos HTTP | 10 % |
| Seguridad con JWT y roles | 10 % |
| Swagger, README y pruebas de aceptación | 10 % |

El detalle de cada criterio, con niveles de desempeño, está en la [Rúbrica](rubrica.md).

## 12. Extensiones opcionales

- Reservas recurrentes (por ejemplo, todos los martes durante un mes).
- Lista de espera cuando una sala está ocupada.
- Reporte de ocupación por sala y por sede.

## 💡 Pistas para empezar

Ninguna es obligatoria; están para que no te bloquees en lo que no es el foco del proyecto.

- **El "ahora" en las pruebas.** Las reglas RN-03 y RN-08 dependen de la hora actual y
  los escenarios 5, 6 y 7 piden "30 h", "5 h" y "1 h" de anticipación. Si tu código
  llama directamente a `LocalDateTime.now()`, esos escenarios serán difíciles de
  repetir. Pensá cómo hacer que la hora sea un dato que se pueda controlar (pista: un
  bean `Clock` inyectado).
- **Reservas de ejemplo en el pasado.** Los datos semilla incluyen reservas
  `COMPLETADA`, que están en el pasado; si las creás con las mismas reglas que una
  reserva nueva, serán rechazadas (RN-03). Pensá cómo cargarlas sin pasar por esas
  reglas.
- **Dos solicitudes a la vez.** RN-01 debe cumplirse aunque lleguen dos solicitudes
  simultáneas para la misma sala y el mismo horario. Verificar el solapamiento y
  guardar en dos pasos separados no alcanza por sí solo.

## 🆘 ¿Te trabaste?

Existe una [solución paso a paso](solucion/README.md), pensada para que puedas
transcribirla y ejecutarla fase por fase, entendiendo el porqué de cada decisión.
Intentá primero cada fase por tu cuenta; si te bloqueás, mirá solo esa fase y volvé a
tu propio proyecto.
