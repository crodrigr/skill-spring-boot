# ❓ Quiz 08 — Spring Boot Security y JWT (formato entrevista técnica)

Este quiz funciona como una autoevaluación estilo entrevista técnica.
Cada ítem indica su tipo (**Selección**, **Selección múltiple** o
**Abierta**) y el resultado de aprendizaje (`RA-N`) que evalúa. Intentá
responder antes de abrir la respuesta oculta dentro de cada
`<details>`.

**1. [Selección]** _RA: RA-1_

**Pregunta:** ¿Cuáles son los dos aspectos principales que gestiona
Spring Boot Security según su propósito?

<details><summary>🔑 Ver respuesta</summary>

Autenticación (verificar quién es el usuario) y autorización (verificar
qué puede hacer ese usuario).

</details>

**2. [Selección múltiple]** _RA: RA-2_

**Pregunta:** ¿Cuáles de los siguientes componentes forman parte de la
arquitectura de Spring Security descrita en el Ejemplo 02?

- **A.** `SecurityContextHolder`.
- **B.** `UserDetailsService`.
- **C.** `EntityManagerFactory`.
- **D.** `Authentication Manager`.

<details><summary>🔑 Ver respuesta</summary>

**A, B y D.** `EntityManagerFactory` (C) es un componente de JPA, no de
Spring Security.

</details>

**3. [Selección]** _RA: RA-3_

**Pregunta:** ¿Cuál es el criterio central para clasificar una
arquitectura como stateless?

- **A.** No usa ninguna base de datos.
- **B.** Cada solicitud se procesa de forma independiente, sin depender de un estado almacenado en el servidor.
- **C.** Solo puede tener un usuario a la vez.
- **D.** No permite ningún tipo de autenticación.

<details><summary>🔑 Ver respuesta</summary>

**B.** Las otras tres opciones son afirmaciones falsas sobre las
arquitecturas stateless.

</details>

**4. [Abierta]** _RA: RA-3_

**Pregunta:** Nombrá una aplicación típica de arquitectura stateless del
material fuente distinta de "servicios REST" y explicá por qué encaja en
esa categoría.

<details><summary>🔑 Ver respuesta modelo</summary>

Cualquiera de: servidores de archivos estáticos, aplicaciones serverless,
balanceadores de carga, aplicaciones de búsqueda, autenticación JWT, o
microservicios — la justificación debe mostrar que cada solicitud se
procesa sin depender de un estado guardado del usuario en el servidor.

</details>

**5. [Selección múltiple]** _RA: RA-4_

**Pregunta:** ¿Cuáles de las siguientes afirmaciones sobre
`FilterChainProxy`, `DelegatingFilterProxy` y `SecurityFilterChain` son
correctas?

- **A.** `DelegatingFilterProxy` integra Spring Security con la configuración de filtros de Servlet.
- **B.** `FilterChainProxy` coordina una o más `SecurityFilterChain`.
- **C.** Una aplicación solo puede definir una única `SecurityFilterChain`.
- **D.** `SecurityFilterChain` puede asociarse a un patrón de URL particular.

<details><summary>🔑 Ver respuesta</summary>

**A, B y D.** C es falsa: una aplicación puede definir varias
`SecurityFilterChain`.

</details>

**6. [Abierta]** _RA: RA-4_

**Pregunta:** ¿Por qué `FilterChainProxy` necesita coordinar varias
`SecurityFilterChain` en vez de tener una única cadena para toda la
aplicación?

<details><summary>🔑 Ver respuesta modelo</summary>

Porque distintas partes de una aplicación pueden necesitar reglas de
seguridad distintas (por ejemplo, `/auth/**` sin autenticación previa
para permitir el login, y `/pacientes/**` exigiendo un JWT válido);
`FilterChainProxy` decide, según el patrón de URL, qué
`SecurityFilterChain` aplicar a cada solicitud.

</details>

**7. [Selección]** _RA: RA-5_

**Pregunta:** ¿Qué ocurre al agregar `spring-boot-starter-security` a un
proyecto Spring Boot REST sin ninguna configuración adicional?

- **A.** No cambia nada hasta que se escriba una clase de configuración.
- **B.** Todos los endpoints quedan protegidos automáticamente, y se genera una contraseña temporal para el usuario `user`.
- **C.** Solo los endpoints `POST`/`PUT`/`DELETE` quedan protegidos.
- **D.** El proyecto deja de compilar hasta agregar una base de datos de usuarios.

<details><summary>🔑 Ver respuesta</summary>

**B.** Spring Boot aplica una configuración de seguridad por defecto
apenas detecta la dependencia, sin necesidad de escribir código.

</details>

**8. [Selección múltiple]** _RA: RA-6_

**Pregunta:** Al probar un endpoint protegido con Basic Auth en un
cliente HTTP, ¿cuáles de los siguientes datos hay que configurar?

- **A.** El usuario `user`.
- **B.** La contraseña autogenerada que aparece en la consola.
- **C.** Un token JWT previamente emitido.
- **D.** El tipo de autenticación "Basic Auth" en la pestaña correspondiente del cliente.

<details><summary>🔑 Ver respuesta</summary>

**A, B y D.** El token JWT (C) no aplica a Basic Auth; es el mecanismo
que se implementa más adelante en el módulo (Ejemplo 08).

</details>

**9. [Abierta]** _RA: RA-5_, _RA: RA-6_

**Pregunta:** Un compañero prueba `GET /libros` sin ninguna credencial y
recibe `401 Unauthorized`. ¿Qué dos pasos le indicarías para resolverlo
usando lo visto en este bloque?

<details><summary>🔑 Ver respuesta modelo</summary>

(1) Ejecutar el proyecto y copiar la contraseña autogenerada que aparece
en la consola al iniciar; (2) en el cliente HTTP, configurar
autenticación "Basic Auth" con usuario `user` y esa contraseña, y
reenviar la solicitud.

</details>

**10. [Selección múltiple]** _RA: RA-7_

**Pregunta:** ¿Cuáles de las siguientes afirmaciones sobre la estructura
de un JWT son correctas?

- **A.** Tiene tres partes separadas por puntos.
- **B.** El header y el payload están encriptados, no solo codificados.
- **C.** La signature se calcula con el header, el payload y una clave secreta.
- **D.** El payload puede incluir claims como el rol del usuario o su fecha de expiración.

<details><summary>🔑 Ver respuesta</summary>

**A, C y D.** B es falsa: el header y el payload están codificados en
Base64, no encriptados — cualquiera puede decodificarlos y leerlos.

</details>

**11. [Abierta]** _RA: RA-8_

**Pregunta:** Describí, en orden, los tres momentos del ciclo de vida de
un JWT (emisión, uso, validación).

<details><summary>🔑 Ver respuesta modelo</summary>

(1) Emisión: el servidor genera el JWT cuando el login es exitoso. (2)
Uso: el cliente reenvía ese mismo token en el encabezado `Authorization`
de cada solicitud subsiguiente. (3) Validación: el servidor recalcula la
firma en cada solicitud recibida y la compara con la incluida en el
token, rechazándolo si no coinciden.

</details>

**12. [Selección]** _RA: RA-9_

**Pregunta:** ¿Qué componente del Ejemplo 08 es responsable de emitir el
JWT cuando el login es exitoso?

- **A.** `FiltroAutenticacionJwt`.
- **B.** `ConfiguracionSeguridad`.
- **C.** `ControladorAutenticacion`, usando `UtilJwt.generarToken`.
- **D.** `Credencial`.

<details><summary>🔑 Ver respuesta</summary>

**C.** `FiltroAutenticacionJwt` valida tokens en solicitudes
subsiguientes; no emite ninguno.

</details>

**13. [Selección múltiple]** _RA: RA-9_

**Pregunta:** ¿Cuáles de las siguientes afirmaciones sobre
`FiltroAutenticacionJwt` son correctas?

- **A.** Se registra antes del filtro estándar de usuario/contraseña.
- **B.** Consulta `RepositorioAutores` para verificar el token.
- **C.** Si el token es inválido, deja que la `SecurityFilterChain` rechace la solicitud.
- **D.** Solo se ejecuta si el encabezado `Authorization` empieza con `Bearer `.

<details><summary>🔑 Ver respuesta</summary>

**A, C y D.** B es falsa: el filtro nunca consulta entidades de dominio,
solo interactúa con `UtilJwt` y `ServicioDetallesUsuario`.

</details>

**14. [Abierta]** _RA: RA-9_

**Pregunta:** Un `UtilJwt.validarToken` decodifica el payload de Base64 y
solo verifica que tenga cierta forma, sin comparar ninguna firma. ¿Qué
problema de seguridad concreto introduce esta implementación?

<details><summary>🔑 Ver respuesta modelo</summary>

Acepta cualquier token cuyo payload tenga la forma esperada, incluido uno
manipulado (por ejemplo, con un rol distinto), porque nunca verifica que
la firma coincida con el contenido — el mecanismo central de detección
de manipulación de JWT queda completamente ausente.

</details>

**15. [Selección múltiple]** _RA: RA-10_

**Pregunta:** ¿Cuáles de las siguientes verificaciones debe pasar una
API integrada con Spring Security y JWT antes de considerarse
correctamente protegida de punta a punta?

- **A.** El login con credenciales válidas emite un JWT.
- **B.** El acceso con un token válido a un endpoint protegido funciona.
- **C.** El acceso sin ningún token es aceptado igual que con token.
- **D.** El acceso con credenciales inválidas es rechazado.

<details><summary>🔑 Ver respuesta</summary>

**A, B y D.** C es falsa: un endpoint protegido debe rechazar el acceso
sin token, no aceptarlo.

</details>

**16. [Abierta]** _RA: RA-10_

**Pregunta:** ¿Por qué es importante probar tanto los casos de éxito
(login válido, token válido) como los de error (sin token, credenciales
inválidas, token manipulado) al integrar Spring Security con JWT sobre
una API completa, y no solo el caso feliz?

<details><summary>🔑 Ver respuesta modelo</summary>

Porque una protección que solo funciona en el caso feliz no protege
nada: el objetivo de agregar autenticación es justamente que los casos
de error (sin credenciales, credenciales incorrectas, tokens
manipulados) sean rechazados. Verificar solo el login exitoso no
demuestra que la API esté realmente protegida.

</details>
