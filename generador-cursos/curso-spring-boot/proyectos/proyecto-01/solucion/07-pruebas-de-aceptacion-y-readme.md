# ✅ Fase 7 — Pruebas de aceptación, colección de Insomnia y README

**Navegación**: [Índice](README.md) · ← [Fase 6 — Seguridad con JWT](06-fase-6-seguridad-jwt.md)

## 🎯 Qué vas a lograr

Demostrar que el sistema cumple los **12 escenarios de aceptación** del enunciado, dejar
esas pruebas guardadas en una **colección de Insomnia** y escribir el **`README.md`** del
proyecto. Con esto completás los entregables 2, 3 y 4 del enunciado.

> Esta fase no agrega código Java: **verifica** el sistema que construiste.

## 🪜 Paso a paso

### Paso 7.1 — Preparar el entorno de pruebas

Los escenarios 5, 6 y 7 dependen de "cuánto falta" para una reserva, así que el "ahora" se
congela (Fase 1). En `application.properties`:

```properties
coworkhub.reloj.fijo=2030-06-03T08:00:00
```

Con eso, "hoy" es el **lunes 2030-06-03 a las 08:00**. **Reiniciá la aplicación antes de
recorrer los escenarios** para partir de los datos semilla (las reservas nuevas empezarán en
el id `16`): con H2 la base nace vacía en cada arranque, y con los perfiles `mysql` y
`postgres` se recrea (`ddl-auto=create`).

Los escenarios dan **el mismo resultado con las tres bases** (H2, MySQL y PostgreSQL). Un buen
ejercicio final es repetirlos cambiando de perfil (paso 1.6).

En una terminal, definí estas funciones de ayuda (Linux/macOS/Git Bash):

```bash
BASE=http://localhost:8080

# Devuelve el token de un usuario: login usuario contraseña
login() {
  curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' \
    -d "{\"nombreUsuario\":\"$1\",\"contrasena\":\"$2\"}" | sed -E 's/.*"token":"([^"]+)".*/\1/'
}

# Llama a la API: api METODO RUTA TOKEN ['cuerpo json']
api() {
  curl -s -w "\n  -> HTTP %{http_code}\n" -X "$1" "$BASE$2" \
    -H "Authorization: Bearer $3" -H 'Content-Type: application/json' ${4:+-d "$4"}
}

ADMIN=$(login admin admin123)
RECEP=$(login recepcion recep123)
ANA=$(login ana ana123)
LUIS=$(login luis luis123)
```

> En Windows usá Insomnia (paso 7.3) o Git Bash. Los ejemplos también sirven como guía
> de qué enviar en cada solicitud.

### Paso 7.2 — Los 12 escenarios

Recorrelos **en este orden**: los ids y los números dependen de lo anterior. Para cada uno
se muestra el comando y el resultado esperado. Los datos de sala, miembro y servicio son
los de los datos semilla (`salaId 1` = Sala Andes, `2` = Sala Caribe, `5` = Sala Pacífico;
`miembroId 1` = Ana, `2` = Luis, `3` = Marta (plan Flex), `4` = Carlos).

#### Escenario 1 — Reservar una sala libre con 2 servicios adicionales → `201` con el costo

```bash
api POST /api/reservas $RECEP '{"miembroId":1,"salaId":2,"inicio":"2030-06-04T16:00:00","fin":"2030-06-04T17:30:00","asistentes":3,"servicios":[{"servicioId":1,"cantidad":2},{"servicioId":2,"cantidad":10}]}'
```

**Esperado**: `HTTP 201`; `"id": 16`, `"estado": "PENDIENTE"`, `"costoSala": 0.0`,
`"costoServicios": 100.0`, `"costoTotal": 100.0` (2 × 25 + 10 × 5; las horas de la sala
las cubre el plan de Ana).

#### Escenario 2 — Horario que se solapa → `409`

La reserva 7 ocupa `Sala Andes` de 10:00 a 12:00 el 2030-06-04.

```bash
api POST /api/reservas $RECEP '{"miembroId":2,"salaId":1,"inicio":"2030-06-04T11:00:00","fin":"2030-06-04T12:00:00","asistentes":2}'
```

**Esperado**: `HTTP 409`, `"codigo": "RN-01"`, `"error": "La sala ya está reservada en ese horario"`.

#### Escenario 3 — Extremos que se tocan → `201`

La reserva 15 ocupa `Sala Andes` de 09:00 a 10:00 el 2030-06-08. Reservá de 10:00 a 11:00:

```bash
api POST /api/reservas $RECEP '{"miembroId":4,"salaId":1,"inicio":"2030-06-08T10:00:00","fin":"2030-06-08T11:00:00","asistentes":2}'
```

**Esperado**: `HTTP 201` (`"id": 17`). Como una termina justo cuando empieza la otra, no hay solapamiento.

#### Escenario 4 — 45 minutos, o fuera del horario → `409` con mensaje claro

```bash
# 4a) 45 minutos
api POST /api/reservas $RECEP '{"miembroId":4,"salaId":1,"inicio":"2030-06-09T10:00:00","fin":"2030-06-09T10:45:00","asistentes":2}'
# 4b) de 06:00 a 07:00 (la sede abre a las 07:00)
api POST /api/reservas $RECEP '{"miembroId":4,"salaId":1,"inicio":"2030-06-09T06:00:00","fin":"2030-06-09T07:00:00","asistentes":2}'
```

**Esperado**: ambas `HTTP 409`. La 4a con `"codigo": "RN-02"` ("La duración debe ser de entre 30
minutos y 8 horas, en múltiplos de 30 minutos"); la 4b con `"codigo": "RN-03"` ("La reserva
debe estar dentro del horario de la sede (07:00 a 22:00)").

#### Escenario 5 — Cancelar con 30 h de anticipación → `200`, cargo 0, horas devueltas

Marta (plan Flex, sin horas incluidas) reserva mañana a las 14:00, que son 30 h desde las
08:00 de hoy. Antes de cancelar, mirá su consumo de junio:

```bash
api POST /api/reservas $RECEP '{"miembroId":3,"salaId":2,"inicio":"2030-06-04T14:00:00","fin":"2030-06-04T15:00:00","asistentes":2}'
api GET "/api/miembros/3/consumo?mes=2030-06" $RECEP
api POST /api/reservas/18/cancelar $RECEP
api GET "/api/miembros/3/consumo?mes=2030-06" $RECEP
```

**Esperado**:

| Llamada | Resultado |
|---|---|
| 1.ª (crear) | `201`, `"id": 18`, `"costoSala": 20.0`, `"minutosConsumidos": 60` |
| 2.ª (consumo) | `"horasUsadas": 4.5` |
| 3.ª (cancelar) | `200`, `"estado": "CANCELADA"`, `"cargoCancelacion": 0`, `"minutosConsumidos": 0`, `"costoTotal": 0` |
| 4.ª (consumo) | `"horasUsadas": 3.5` — **la hora volvió al cupo** |

#### Escenario 6 — Cancelar con 5 h de anticipación → `200`, cargo del 50 %

Hoy son las 08:00; una reserva de 13:00 a 14:00 empieza en 5 horas:

```bash
api POST /api/reservas $RECEP '{"miembroId":3,"salaId":2,"inicio":"2030-06-03T13:00:00","fin":"2030-06-03T14:00:00","asistentes":2}'
api POST /api/reservas/19/cancelar $RECEP
```

**Esperado**: la creación responde `201` (`"id": 19`, `"costoSala": 20.0`). La cancelación,
`200`, `"estado": "CANCELADA"`, **`"cargoCancelacion": 10.0`**, `"costoTotal": 10.0` (50 %
de 20) y `"minutosConsumidos": 60` (las horas **no** se devuelven).

#### Escenario 7 — Cancelar con 1 h de anticipación → `409`

Una reserva de 09:30 a 10:30 empieza en 1,5 h: se puede **crear** (mínimo 1 h de
anticipación) pero ya no **cancelar** (mínimo 2 h):

```bash
api POST /api/reservas $RECEP '{"miembroId":3,"salaId":2,"inicio":"2030-06-03T09:30:00","fin":"2030-06-03T10:30:00","asistentes":2}'
api POST /api/reservas/20/cancelar $RECEP
```

**Esperado**: `201` (`"id": 20`); y luego `HTTP 409`, `"codigo": "RN-08"`, `"error": "No se
puede cancelar con menos de 2 horas de anticipación"`.

#### Escenario 8 — Confirmar una reserva ya cancelada → `409`

La reserva 18 se canceló en el escenario 5:

```bash
api POST /api/reservas/18/confirmar $RECEP
```

**Esperado**: `HTTP 409`, `"codigo": "RN-09"`, `"error": "No se puede confirmar una reserva en
estado CANCELADA (debe estar PENDIENTE)"`.

#### Escenario 9 — Un miembro consulta la reserva de otro → `403`

La reserva 7 es de Ana; Luis es otro miembro:

```bash
api GET /api/reservas/7 $LUIS
api GET /api/reservas/8 $LUIS      # la 8 sí es de Luis
```

**Esperado**: la primera `HTTP 403`, `"codigo": "ACCESO_DENEGADO"`; la segunda `200`.

#### Escenario 10 — Sin token, o con un token alterado → `401`

```bash
curl -s -w "\n  -> HTTP %{http_code}\n" $BASE/api/reservas/7                                   # sin token
curl -s -w "\n  -> HTTP %{http_code}\n" $BASE/api/reservas/7 -H "Authorization: Bearer ${ANA%???}AAA"   # token alterado
```

**Esperado**: ambas `HTTP 401`, `"codigo": "NO_AUTENTICADO"`.

#### Escenario 11 — Eliminar una sala con reservas futuras → `409`

```bash
api DELETE /api/salas/1 $ADMIN
```

**Esperado**: `HTTP 409`, `"codigo": "RN-12"`, `"error": "No se puede eliminar la sala: tiene
reservas activas"`.

#### Escenario 12 — Resumen mensual: 3 h reservadas con un plan de 2 h → 2 h incluidas y 1 h excedente

Creá un plan de 2 horas, un miembro con ese plan y tres reservas de 1 hora el mismo día:

```bash
api POST /api/planes $ADMIN '{"nombre":"Mini","horasIncluidasMes":2,"descuentoExcedente":10,"maxReservasActivas":5}'
api POST /api/miembros $RECEP '{"documento":"9090909","nombre":"Mateo Mini","email":"mateo@coworkhub.test","planId":5,"nombreUsuario":"mateo","contrasena":"mateo123"}'
api POST /api/reservas $RECEP '{"miembroId":6,"salaId":2,"inicio":"2030-06-11T09:00:00","fin":"2030-06-11T10:00:00","asistentes":2}'
api POST /api/reservas $RECEP '{"miembroId":6,"salaId":2,"inicio":"2030-06-11T10:00:00","fin":"2030-06-11T11:00:00","asistentes":2}'
api POST /api/reservas $RECEP '{"miembroId":6,"salaId":2,"inicio":"2030-06-11T11:00:00","fin":"2030-06-11T12:00:00","asistentes":2}'
api GET "/api/miembros/6/consumo?mes=2030-06" $RECEP
```

**Esperado**: el plan `Mini` tiene `"id": 5` y el miembro `"id": 6`. Las tres reservas se crean
con `201` (ids 21, 22 y 23); las dos primeras con `"costoSala": 0.0` y la **tercera con
`"costoSala": 18.0`** (la hora excedente: 20 × 1 × 0,90). El resumen:

```json
{
  "miembroId": 6, "miembro": "Mateo Mini", "plan": "Mini", "mes": "2030-06",
  "horasIncluidas": 2.0, "horasUsadas": 3.0, "horasIncluidasUsadas": 2.0,
  "horasRestantes": 0.0, "horasExcedentes": 1.0
}
```

#### Extra — RN-01 bajo carga (solicitudes simultáneas)

Lanzá seis solicitudes **a la vez** para la misma sala y horario:

```bash
for m in 1 2 4 6 1 4; do
  curl -s -o /dev/null -w "miembro $m -> HTTP %{http_code}\n" -X POST $BASE/api/reservas \
    -H "Authorization: Bearer $RECEP" -H 'Content-Type: application/json' \
    -d "{\"miembroId\":$m,\"salaId\":5,\"inicio\":\"2030-06-20T10:00:00\",\"fin\":\"2030-06-20T11:00:00\",\"asistentes\":2}" &
done; wait
```

**Esperado**: exactamente **una** respuesta `201` y cinco `409`, sin importar el orden. Si
obtenés más de un `201`, falta el bloqueo de la sala (`buscarParaReservar`, Fase 2 y 3c) o el
nivel de aislamiento `READ_COMMITTED` de `crear` (imprescindible en MySQL; ver la Fase 3c).

### Paso 7.3 — La colección de Insomnia (entregable 3)

Creá en Insomnia una colección **`CoWorkHub`**:

1. **Entorno** (*Environment*): definí variables para no repetir la URL ni los tokens:

   ```json
   {
     "base_url": "http://localhost:8080",
     "token_admin": "",
     "token_recepcion": "",
     "token_ana": "",
     "token_luis": ""
   }
   ```

2. **Solicitudes de login**: una por usuario (`POST {{ base_url }}/auth/login`). Tras
   ejecutarlas, copiá el `token` de cada respuesta a la variable correspondiente del entorno.
   (Opcional: Insomnia permite encadenar solicitudes referenciando el atributo `token` del
   cuerpo de la respuesta del login, para no copiar a mano.)
3. **En cada solicitud protegida**, pestaña *Auth* → *Bearer Token* → `{{ token_recepcion }}`
   (o el usuario que corresponda al escenario).
4. **Una carpeta por escenario** (`E01 …`, `E12`) con sus solicitudes en el orden de arriba.
   En el nombre de cada solicitud escribí el **resultado esperado** (por ejemplo,
   `E02 solapamiento → 409 RN-01`) para que cualquiera pueda verificarlo de un vistazo.
5. **Exportá** la colección (*Export* del espacio de trabajo, formato Insomnia v4 o
   superior) y guardala en el repositorio, por ejemplo en `docs/insomnia-coworkhub.json`.
   Recordá que **los tokens no se deben exportar con valor real**: dejá las variables vacías.

### Paso 7.4 — El `README.md` del proyecto (entregable 4)

El README es la puerta de entrada: alguien que no conoce el proyecto debe poder ejecutarlo
y reproducir los escenarios leyendo solo eso. Ponelo en la raíz del repositorio. Este es un
modelo que podés adaptar:

````markdown
# CoWorkHub — API de reservas de salas y espacios de trabajo

Backend REST para gestionar sedes, salas, miembros y reservas de una red de coworking:
evita reservas superpuestas, controla el consumo mensual de horas de cada plan,
aplica una política de cancelación y protege la API con JWT y roles.

## Tecnologías

Java 17 · Spring Boot 3.3 · Spring Data JPA · H2 (por defecto), MySQL o PostgreSQL ·
Spring Security + JWT · springdoc-openapi (Swagger UI) · Maven

## Cómo ejecutarlo

Requisitos: JDK 17 o superior y Maven 3.6.3 o superior.

```bash
mvn spring-boot:run
```

La aplicación queda en <http://localhost:8080>. Por defecto usa H2 **en memoria**: no hay que
instalar nada, y al reiniciar se pierden los datos y se recargan los de ejemplo (2 sedes,
7 salas, 4 planes, 5 miembros y 15 reservas).

## Bases de datos

| Base | Perfil | Cómo ejecutarla |
|---|---|---|
| H2 (por defecto) | `h2` | `mvn spring-boot:run` |
| MySQL 8 | `mysql` | `docker compose --profile mysql up -d` y luego `mvn spring-boot:run -Dspring-boot.run.profiles=mysql` |
| PostgreSQL 14+ | `postgres` | `docker compose --profile postgres up -d` y luego `mvn spring-boot:run -Dspring-boot.run.profiles=postgres` |

Los datos de conexión (host, puerto, base, usuario y contraseña) se pueden cambiar con
variables de entorno: `COWORKHUB_DB_HOST`, `COWORKHUB_DB_PORT`, `COWORKHUB_DB_NOMBRE`,
`COWORKHUB_DB_USUARIO` y `COWORKHUB_DB_CONTRASENA`. Con MySQL y PostgreSQL las tablas se
recrean en cada arranque; para conservar los datos, arrancá a partir de la segunda vez con
`--spring.jpa.hibernate.ddl-auto=update --spring.sql.init.mode=never`.


## Documentación de la API (Swagger UI)

<http://localhost:8080/doc/swagger-ui.html> (el documento OpenAPI en `/v3/api-docs`).
Para probar endpoints protegidos: ejecutá `POST /auth/login`, copiá el `token`, botón
**Authorize** y pegá solo el token.

## Usuarios de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN: todo, incluida la gestión de catálogos |
| `recepcion` | `recep123` | RECEPCION: miembros y reservas de cualquier miembro |
| `ana` | `ana123` | MIEMBRO (plan Profesional): solo lo suyo |
| `luis` | `luis123` | MIEMBRO (plan Básico): solo lo suyo |

> ⚠️ Credenciales y clave JWT **solo para desarrollo**. En un entorno real la clave se lee
> de una variable de entorno y las contraseñas no se publican.

## Pruebas de aceptación

Para repetir los escenarios con horas exactas, congelá el "ahora" en
`application.properties`:

```properties
coworkhub.reloj.fijo=2030-06-03T08:00:00
```

La colección de Insomnia con los 12 escenarios está en `docs/insomnia-coworkhub.json`
(el resultado esperado figura en el nombre de cada solicitud).

## Estructura

```text
com.coworkhub
├── controllers   → endpoints REST
├── services      → reglas de negocio (+ CalculadoraCostoReserva)
├── persistences  → entities y repositories (JPA)
├── dto           → objetos de solicitud y respuesta
├── exception     → excepciones de negocio y manejador global
├── config        → beans, datos de ejemplo y configuración de OpenAPI
└── security      → JWT, filtros y autorización
```

## Documentación de análisis

Análisis, diagrama entidad-relación, matriz de trazabilidad y supuestos: `docs/analisis.md`.
````

Guardá también tu documento de análisis (Fase 0) en `docs/analisis.md`.

### Paso 7.5 — Historial de commits (entregable 2)

El enunciado pide **un commit por fase**. Tu historial debería verse así:

```bash
git log --oneline
```

```text
xxxxxxx fase 7: pruebas de aceptación, colección de Insomnia y README
xxxxxxx fase 6: seguridad con JWT y roles
xxxxxxx fase 5: documentación con Swagger
xxxxxxx fase 4: manejo global de excepciones
xxxxxxx fase 3c: reservas, calculadora de costos y datos de ejemplo
xxxxxxx fase 3b: miembros, consumo mensual y disponibilidad
xxxxxxx fase 3a: excepciones, DTOs y catálogos REST
xxxxxxx fase 2: modelo de datos, repositorios y datos semilla
xxxxxxx fase 1: proyecto base
```

Para subirlo a GitHub: creá un repositorio vacío y ejecutá
`git remote add origin <url>`, `git branch -M main`, `git push -u origin main`.

```bash
git add .
git commit -m "fase 7: pruebas de aceptación, colección de Insomnia y README"
```

### Paso 7.6 — Autoevaluación

Antes de entregar, recorré la [Rúbrica](../rubrica.md), en particular su
[lista de autoevaluación rápida](../rubrica.md#-lista-de-autoevaluación-rápida).

## 🩺 Problemas frecuentes

| Síntoma | Causa probable | Qué hacer |
|---|---|---|
| Los escenarios 5, 6 o 7 dan resultados distintos | El reloj no está congelado, o no reiniciaste desde los datos semilla | Verificá `coworkhub.reloj.fijo` y reiniciá |
| `400` con "no es un JSON válido" | Fecha con formato incorrecto (usá `2030-06-04T16:00:00`, sin zona) o comillas simples dentro del JSON | Revisá el cuerpo enviado |
| `401` en todo | Token vencido (dura 60 min), mal copiado, o falta `Bearer ` | Volvé a hacer login |
| `403` inesperado | El rol o el dueño de la reserva no coincide | Revisá la matriz de permisos de la [Fase 0](00-analisis.md#4-matriz-de-trazabilidad) |
| Los ids no coinciden con esta guía | Ejecutaste otras solicitudes antes | Reiniciá la aplicación |
| `409` `RN-06` al reservar | El miembro ya alcanzó el límite de reservas activas de su plan | Cancelá una, o usá otro miembro |
| `500` con `ERROR_INTERNO` | Error inesperado | Mirá la consola: el detalle completo está en el log |
| `LazyInitializationException` | Falta `spring.jpa.open-in-view=true`, o una colección `LAZY` se usa fuera de la transacción | Revisá `application.properties` |
| No conecta con MySQL o PostgreSQL | La base no está levantada, el puerto está ocupado o las credenciales no coinciden | Tabla de problemas del [Checkpoint 1](01-fase-1-proyecto-base.md#-checkpoint-1--el-proyecto-arranca) |
| Con MySQL, la prueba de solicitudes simultáneas da más de un `201` | Falta `isolation = READ_COMMITTED` en `ServicioReservas.crear` | Ver la [Fase 3c](03c-fase-3-reservas.md) |

## 🏁 Fin del recorrido

Si llegaste hasta acá con todas las fases verificadas, construiste una API completa:
modelo JPA, reglas de negocio con concurrencia controlada, manejo uniforme de errores,
documentación viva y seguridad por roles. Volvé a la [Rúbrica](../rubrica.md) y evaluá tu
propio proyecto.
