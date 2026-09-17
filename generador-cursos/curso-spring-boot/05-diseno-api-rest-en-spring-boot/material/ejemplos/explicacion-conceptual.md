# 📚 Explicación conceptual — Módulo 5

## 🧠 Concepto: ¿Qué es una API?

Una API es un conjunto de reglas que permite que dos componentes de
software se comuniquen; una API remota/web opera sobre una red, casi
siempre con HTTP.

Toda solicitud HTTP combina un verbo y una URL; toda respuesta combina un
código de estado y, normalmente, un cuerpo:

- `GET` — leer un recurso, sin modificarlo.
- `POST` — crear un recurso nuevo.
- `PUT` — reemplazar un recurso existente por completo.
- `PATCH` — modificar parcialmente un recurso existente.
- `DELETE` — eliminar un recurso existente.

Los códigos de estado se agrupan en cinco rangos:

- `1xx` — informativo.
- `2xx` — éxito (`200 OK`, `201 Created`).
- `3xx` — redirección.
- `4xx` — error del cliente (`404 Not Found`).
- `5xx` — error del servidor (`500 Internal Server Error`).

📎 Ver en la práctica: [Ejemplo 01 — ¿Qué es una API y cómo funciona HTTP?](01-que-es-una-api-y-http.md)

Una API REST sigue, además, seis principios de diseño: sin estado,
cliente-servidor, interfaz uniforme, basada en recursos, sistema en capas
y cacheable (más "código bajo demanda", opcional y poco usado).

📎 Ver en la práctica: [Ejemplo 02 — Principios de una API REST](02-principios-de-una-api-rest.md)

MVC (Modelo Vista Controlador) divide una aplicación en Modelo (datos y
reglas de negocio), Vista (lo que el usuario ve) y Controlador (coordina
entre ambos). En una API REST no hay Vista tradicional: el cuerpo JSON de
la respuesta cumple ese rol.

Spring Boot traduce MVC a cuatro capas, cada una comunicándose solo con la
de abajo:

- `Controller` (`@RestController`) — maneja HTTP.
- `Service` (`@Service`) — lógica de negocio.
- `Repository` (`JpaRepository`) — acceso a datos.
- `Database` — persistencia.

📎 Ver en la práctica: [Ejemplo 03 — MVC y la arquitectura en capas de Spring](03-mvc-y-arquitectura-en-capas.md)

## 🧠 Concepto: Implementación de un RESTful API en Spring Boot

Sobre un proyecto Spring Boot ya conectado a H2 (Módulos 3-4), agregar
`spring-boot-starter-web` habilita Spring MVC. A partir de ahí:

- La clase `@Service` inyecta el repositorio ya existente y expone
  métodos de negocio (`listarTodos`, `buscarPorId`, `crear`, `actualizar`,
  `eliminar`), sin escribir SQL/JPQL.

📎 Ver en la práctica: [Ejemplo 04 — Recapitulando el proyecto y la conexión](04-recapitulando-el-proyecto-y-la-conexion.md) · [Ejemplo 05 — Creación de la clase servicio](05-creacion-de-la-clase-servicio.md)

- La clase `@RestController` expone el servicio como endpoints HTTP:

| Anotación | Dónde | Para qué |
|---|---|---|
| `@RestController` | Clase | Marca la clase como controlador REST (retorna JSON) |
| `@RequestMapping("/ruta")` | Clase | Define la ruta base de todos sus endpoints |
| `@GetMapping` | Método | Endpoint `GET` |
| `@PostMapping` | Método | Endpoint `POST` (código de éxito `201`) |
| `@PutMapping` | Método | Endpoint `PUT` (código de éxito `200`) |
| `@DeleteMapping` | Método | Endpoint `DELETE` (código de éxito `200`) |
| `@PathVariable` | Parámetro | Captura un segmento de la URL (`/{id}`) |
| `@RequestParam` | Parámetro | Captura un parámetro de consulta (`?isbn=...`) |
| `@RequestBody` | Parámetro | Convierte el cuerpo JSON en un objeto Java |

Cuando el recurso no existe, el controlador devuelve `404` (con
`ResponseEntity`), nunca `200` con un cuerpo vacío.

📎 Ver en la práctica: [Ejemplo 06 — Controlador REST: endpoints GET](06-controlador-rest-endpoints-get.md) · [Ejemplo 07 — Controlador REST: endpoints POST, PUT, DELETE](07-controlador-rest-endpoints-post-put-delete.md)

Cada endpoint se prueba con un cliente HTTP (Insomnia): documentando
método, URL, cuerpo (si aplica), código de estado y cuerpo de la
respuesta, para cada operación CRUD — incluyendo al menos un caso de
error (`404`), no solo los casos de éxito.

📎 Ver en la práctica: [Ejemplo 08 — Pruebas en Insomnia](08-pruebas-en-insomnia.md)
