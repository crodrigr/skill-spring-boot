# 🔑 Soluciones — Ejercicios del Módulo 8

> Material docente. No enlazar ni compartir con la audiencia estudiante.

## Intermedio 02 — Ubicar componentes en la arquitectura de Spring Security

| Momento | Componente responsable |
|---|---|
| Verificar que la contraseña enviada coincide con la almacenada | `PasswordEncoder` (usado por los `Authentication Providers`) |
| Almacenar el resultado de una autenticación exitosa | `SecurityContextHolder` |
| Interceptar la solicitud entrante antes del `Controller` | `Security Filter Chain` |
| Obtener los datos del usuario desde la base de datos | `UserDetailsService` |

El `Authentication Manager` coordina el proceso completo delegando en los
`Authentication Providers`, pero no verifica contraseñas ni consulta la
base de datos directamente: esas tareas están delegadas en
`PasswordEncoder` y `UserDetailsService` respectivamente.

## Básico 01 — Clasificar sistemas como stateful o stateless

| Sistema | Clasificación | Justificación |
|---|---|---|
| Servicio REST que usa solo los datos de cada solicitud | Stateless | No depende de ninguna interacción anterior |
| Balanceador de carga | Stateless | Distribuye cada solicitud de forma independiente |
| Aplicación de escritorio con sesión abierta | Stateful | Recuerda las acciones anteriores del usuario durante la sesión |
| Motor de búsqueda que no usa historial | Stateless | Cada consulta se procesa sin depender de búsquedas previas |

## Intermedio 01 — Agregar Spring Security y probar con Basic Auth

`pom.xml` (fragmento agregado):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Pruebas en Insomnia:

```text
Método: GET
URL: http://localhost:8080/autores
(sin autenticación)
Respuesta: 401 Unauthorized
```

```text
Método: GET
URL: http://localhost:8080/autores
Autenticación: Basic Auth — usuario: user, contraseña: (la generada en consola)
Respuesta: 200 OK
[
  {"id": 1, "nombre": "Robert C. Martin"}
]
```

## Básico 02 — Identificar las tres partes de un JWT

```text
eyJhbGciOiJIUzI1NiJ9 . eyJzdWIiOiJhbmEiLCJyb2wiOiJVU0VSIiwiZXhwIjoxOTk5OTk5OTk5fQ . k3F9x2pQ7mLwR5vN1oJhT8sD4cA6bE0gU2iY9zX3rWc
    (1) Header               (2) Payload                                              (3) Signature
```

1. **Header** (`eyJhbGciOiJIUzI1NiJ9`): decodificado es
   `{"alg":"HS256"}` — indica el algoritmo de firma.
2. **Payload** (`eyJzdWIiOiJhbmEi...`): decodificado es
   `{"sub":"ana","rol":"USER","exp":1999999999}` — contiene los claims
   del usuario (nombre, rol, expiración).
3. **Signature** (`k3F9x2pQ7...`): no se decodifica a JSON; es el
   resultado de firmar header+payload con la clave secreta del servidor,
   usado para detectar manipulación.

## Intermedio 03 — Implementar el endpoint de login que emite un JWT

```java
@RestController
@RequestMapping("/auth")
public class ControladorAutenticacion {

    private final AuthenticationManager authenticationManager;
    private final ServicioDetallesUsuario servicioDetallesUsuario;
    private final UtilJwt utilJwt;

    public ControladorAutenticacion(AuthenticationManager authenticationManager,
                                     ServicioDetallesUsuario servicioDetallesUsuario,
                                     UtilJwt utilJwt) {
        this.authenticationManager = authenticationManager;
        this.servicioDetallesUsuario = servicioDetallesUsuario;
        this.utilJwt = utilJwt;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credenciales) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        credenciales.get("nombreUsuario"),
                        credenciales.get("contrasena")));

        UserDetails usuario = servicioDetallesUsuario.loadUserByUsername(credenciales.get("nombreUsuario"));
        String token = utilJwt.generarToken(usuario);

        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("token", token);
        return ResponseEntity.ok(cuerpo);
    }
}
```

## Avanzado 01 — Implementar el filtro de validación de JWT

```java
@Component
public class FiltroAutenticacionJwt extends OncePerRequestFilter {

    private final UtilJwt utilJwt;
    private final ServicioDetallesUsuario servicioDetallesUsuario;

    public FiltroAutenticacionJwt(UtilJwt utilJwt, ServicioDetallesUsuario servicioDetallesUsuario) {
        this.utilJwt = utilJwt;
        this.servicioDetallesUsuario = servicioDetallesUsuario;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            String token = encabezado.substring(7);
            if (utilJwt.validarToken(token)) {
                String nombreUsuario = utilJwt.extraerNombreUsuario(token);
                UserDetails usuario = servicioDetallesUsuario.loadUserByUsername(nombreUsuario);
                UsernamePasswordAuthenticationToken autenticacion =
                        new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

`ConfiguracionSeguridad` (fragmento agregado):

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(autorizacion -> autorizacion
                    .requestMatchers("/auth/login").permitAll()
                    .anyRequest().authenticated())
            .httpBasic(basic -> basic.disable())
            .addFilterBefore(filtroAutenticacionJwt, UsernamePasswordAuthenticationFilter.class)
            .build();
}
```

## Avanzado 02 — Diagnosticar un JWT válido rechazado

**Diagnóstico**: falta la línea
`.addFilterBefore(filtroAutenticacionJwt, UsernamePasswordAuthenticationFilter.class)`.
Sin registrar `FiltroAutenticacionJwt` en la cadena, la clase existe y
compila, pero Spring Security nunca la invoca — ninguna solicitud pasa
por su lógica de validación, así que `SecurityContextHolder` nunca se
completa con el usuario del token, y la solicitud es rechazada como si
no hubiera ninguna autenticación.

**Corrección**: agregar `.addFilterBefore(filtroAutenticacionJwt,
UsernamePasswordAuthenticationFilter.class)` antes de `.build()`.

## Avanzado 03 — Diagnosticar un token manipulado aceptado incorrectamente

**Diagnóstico**: la implementación incorrecta decodifica el payload de
Base64 y solo comprueba que contenga la subcadena `"sub"`, pero nunca
recalcula la firma ni la compara con la incluida en el token. Como
Base64 no es cifrado, cualquiera puede modificar el payload (por
ejemplo, cambiar el rol) y volver a codificarlo — el resultado sigue
conteniendo `"sub"`, así que esta validación lo acepta igual.

**Corrección**:

```java
public boolean validarToken(String token) {
    try {
        Jwts.parser().verifyWith(clave).build().parseSignedClaims(token);
        return true;
    } catch (JwtException | IllegalArgumentException ex) {
        return false; // firma inválida, token expirado o mal formado
    }
}
```

`Jwts.parser().verifyWith(clave)` recalcula la firma esperada con la
clave secreta del servidor y la compara contra la incluida en el token;
si no coinciden, lanza una excepción que este método captura devolviendo
`false`.

## Desafío 01 — Spring Security y JWT sobre la API de citas

La solución sigue exactamente el mismo patrón del
[Taller 01](solucion-taller-01.md) (`Credencial`, `RepositorioCredenciales`,
`ServicioDetallesUsuario`, `UtilJwt`, `FiltroAutenticacionJwt`,
`ControladorAutenticacion`, `ConfiguracionSeguridad`), aplicado al
proyecto de `Cita`/`ServicioCitas`/`ControladorCitas` en vez de
`Paciente`/`ServicioPacientes`/`ControladorPacientes`. `Cita`,
`ServicioCitas` y `ControladorCitas` no cambian ninguna línea.

Pruebas en Insomnia:

```text
Método: POST
URL: http://localhost:8080/auth/login
Cuerpo: {"nombreUsuario": "laura", "contrasena": "clave456"}
Respuesta: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsYXVyYSIsInJvbCI6IlJPTEVfVVNFUiJ9.def456..."
}
```

```text
Método: GET
URL: http://localhost:8080/citas
Autorización: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsYXVyYSIsInJvbCI6IlJPTEVfVVNFUiJ9.def456...
Respuesta: 200 OK
[]
```

```text
Método: GET
URL: http://localhost:8080/citas
(sin encabezado Authorization)
Respuesta: 401 Unauthorized
```

```text
Método: POST
URL: http://localhost:8080/auth/login
Cuerpo: {"nombreUsuario": "laura", "contrasena": "clave-incorrecta"}
Respuesta: 401 Unauthorized
```
