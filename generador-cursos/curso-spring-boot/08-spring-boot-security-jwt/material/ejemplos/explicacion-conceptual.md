# 📚 Explicación conceptual — Módulo 8

## 🧠 Concepto: ¿Qué es Spring Boot Security?

Spring Boot Security es la parte del framework Spring que proporciona
servicios de seguridad integrales, con el propósito de asegurar el
proyecto gestionando autenticación y autorización.

Conceptos clave:

- Roles y autoridades para el control de acceso.
- Cadena de filtros de seguridad, donde cada filtro maneja un aspecto
  distinto (autenticación, autorización).
- Protección contra CSRF (Cross-Site Request Forgery).
- Control de sesiones y seguridad en protocolos HTTP.

Arquitectura (de la solicitud a la respuesta):

- **Security Filter Chain**: intercepta toda solicitud entrante.
- **Authentication Manager**: se activa cuando el usuario aún no está autenticado.
- **Authentication Providers**: verifican las credenciales del usuario.
- **PasswordEncoder**: compara contraseñas codificadas.
- **UserDetailsService**: obtiene los datos del usuario desde la base de datos.
- **SecurityContextHolder**: almacena el resultado de una autenticación exitosa.

📎 Ver en la práctica: [Ejemplo 01 — ¿Qué es Spring Boot Security?](01-que-es-spring-security.md), [Ejemplo 02 — Arquitectura de Spring Boot Security](02-arquitectura-de-spring-security.md)

## 🧠 Concepto: Tipos de arquitecturas RESTful API

Una arquitectura **stateful** almacena el estado de las interacciones
pasadas del usuario (por ejemplo, en una sesión HTTP); una arquitectura
**stateless** procesa cada solicitud de forma independiente, sin
depender de ningún estado guardado en el servidor.

Aplicaciones típicas de arquitecturas stateless:

- Servicios web RESTful.
- Servidores de archivos estáticos.
- Aplicaciones serverless.
- Balanceadores de carga.
- Aplicaciones de búsqueda.
- Servicios de autenticación con JWT.
- Arquitecturas basadas en microservicios.

📎 Ver en la práctica: [Ejemplo 03 — Arquitecturas RESTful: stateful vs. stateless](03-arquitecturas-stateful-vs-stateless.md)

## 🧠 Concepto: Implementación de Spring Boot Security en servicios Stateless

`FilterChainProxy`, `DelegatingFilterProxy` y `SecurityFilterChain` son
las clases concretas detrás de la "Security Filter Chain" del diagrama de
arquitectura:

- **`DelegatingFilterProxy`**: integra la cadena de filtros de Spring
  Security con la configuración de filtros de Servlet de la aplicación
  web.
- **`FilterChainProxy`**: gestiona y coordina una o más cadenas de
  filtros, decidiendo cuál aplicar a cada solicitud entrante.
- **`SecurityFilterChain`**: interfaz que define una cadena de filtros de
  seguridad específica, asociable a un patrón de URL particular.

📎 Ver en la práctica: [Ejemplo 04 — FilterChainProxy, DelegatingFilterProxy y SecurityFilterChain](04-filterchainproxy-y-securityfilterchain.md)

## 🧠 Concepto: Implementación de Spring Boot Security

Pasos para agregar Spring Security a un proyecto Spring Boot REST ya
existente:

- Agregar `spring-boot-starter-security` al `pom.xml`.
- Ejecutar el proyecto y observar la contraseña autogenerada en la consola.
- Confirmar que los endpoints, antes públicos, ahora exigen autenticación.
- Configurar "Basic Auth" en el cliente HTTP con usuario `user` y la contraseña generada.
- Verificar que el endpoint responde correctamente con esas credenciales.

📎 Ver en la práctica: [Ejemplo 05 — Primeros pasos: agregar Spring Security y probar con Basic Auth](05-implementacion-basica-con-basic-auth.md)

## 🧠 Concepto: ¿Qué es JWT?

JWT (JSON Web Token) es un estándar abierto (RFC 7519) que representa
información entre dos partes de forma compacta, autónoma y verificable,
porque está firmada digitalmente.

Estructura de tres partes, separadas por puntos y codificadas en Base64:

- **Header**: tipo de token y algoritmo de firma.
- **Payload**: claims (declaraciones) sobre el usuario y datos adicionales.
- **Signature**: firma calculada sobre header + payload + clave secreta, usada para detectar manipulación.

Ciclo de vida:

- **Emisión**: el servidor genera el JWT al autenticarse exitosamente (login).
- **Uso**: el cliente reenvía el mismo JWT en cada solicitud subsiguiente.
- **Validación**: el servidor recalcula la firma en cada solicitud; si no coincide, rechaza el token.

📎 Ver en la práctica: [Ejemplo 06 — Estructura de un JWT](06-estructura-de-un-jwt.md), [Ejemplo 07 — Ciclo de vida y firma de un JWT](07-ciclo-de-vida-y-firma-de-un-jwt.md)

Implementación completa de autenticación stateless con JWT sobre un
proyecto REST ya existente:

- Una entidad de credenciales (`Credencial`) independiente de las entidades de dominio.
- Un `UserDetailsService` (`ServicioDetallesUsuario`) que la usa para autenticar.
- Un endpoint de login (`ControladorAutenticacion`) que emite el JWT.
- Un filtro (`FiltroAutenticacionJwt`) que lo valida en cada solicitud.
- Una configuración (`ConfiguracionSeguridad`) que marca la aplicación como `STATELESS` y reemplaza Basic Auth por este filtro.

📎 Ver en la práctica: [Ejemplo 08 — Autenticación stateless completa con JWT](08-autenticacion-stateless-con-jwt.md)
