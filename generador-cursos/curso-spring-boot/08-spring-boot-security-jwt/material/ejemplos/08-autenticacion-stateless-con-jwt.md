# 💡 Ejemplo 08 — Autenticación stateless completa con JWT

## 🌍 Contexto

Los Ejemplos 06 y 07 explicaron qué es un JWT y su ciclo de vida. Este
ejemplo lo implementa por completo sobre el proyecto de `ControladorLibros`
(el mismo ya protegido con Basic Auth en el Ejemplo 05): un endpoint de
login que emite un JWT, y un filtro que lo valida en cada solicitud
subsiguiente, reemplazando la autenticación básica por un mecanismo
stateless.

**Qué busca demostrar este ejemplo**: que "agregar seguridad" y "cambiar
las reglas de negocio" son cosas distintas — `Libro`, `ServicioLibros` y
`ControladorLibros` no cambian ni una línea; toda la novedad vive en
clases nuevas, completamente separadas del dominio.

> ⚠️ **Advertencia**: la clave secreta usada para firmar los JWT en este
> ejemplo es **solo para fines educativos**. En un proyecto real, esa
> clave debe gestionarse fuera del código fuente (variable de entorno,
> gestor de secretos), nunca como una constante en el repositorio.

## 📚 Caso de estudio

Biblioteca Universitaria: mismo proyecto de `ControladorLibros` del
Ejemplo 05 (Módulo 7, sin cambios).

<details>
<summary>📄 Ver código completo de <code>Libro.java</code>, <code>RepositorioLibros.java</code>, <code>LibroNoEncontradoException.java</code>, <code>LibroDuplicadoException.java</code>, <code>ServicioLibros.java</code>, <code>ControladorLibros.java</code> y <code>ManejadorGlobalDeExcepciones.java</code> (reutilizados del Ejemplo 05, sin cambios)</summary>

## 💻 Archivo: `Libro.java`

```java
@Entity
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String isbn;

    private String titulo;

    protected Libro() {
    }

    public Libro(String isbn, String titulo) {
        this.isbn = isbn;
        this.titulo = titulo;
    }

    public Long getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
}
```

## 💻 Archivo: `RepositorioLibros.java`

```java
public interface RepositorioLibros extends JpaRepository<Libro, Long> {
    Optional<Libro> findByIsbn(String isbn);
}
```

## 💻 Archivo: `LibroNoEncontradoException.java`

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LibroNoEncontradoException extends RuntimeException {

    public LibroNoEncontradoException(Long id) {
        super("No existe un libro con id " + id);
    }
}
```

## 💻 Archivo: `LibroDuplicadoException.java`

```java
@ResponseStatus(HttpStatus.CONFLICT)
public class LibroDuplicadoException extends RuntimeException {

    public LibroDuplicadoException(String isbn) {
        super("Ya existe un libro con isbn " + isbn);
    }
}
```

## 💻 Archivo: `ServicioLibros.java`

```java
@Service
public class ServicioLibros {

    private final RepositorioLibros repositorioLibros;

    public ServicioLibros(RepositorioLibros repositorioLibros) {
        this.repositorioLibros = repositorioLibros;
    }

    public List<Libro> listarTodos() {
        return repositorioLibros.findAll();
    }

    public Libro buscarPorId(Long id) {
        return repositorioLibros.findById(id)
                .orElseThrow(() -> new LibroNoEncontradoException(id));
    }

    public Optional<Libro> buscarPorIsbn(String isbn) {
        return repositorioLibros.findByIsbn(isbn);
    }

    public Libro crear(Libro libro) {
        repositorioLibros.findByIsbn(libro.getIsbn())
                .ifPresent(existente -> {
                    throw new LibroDuplicadoException(libro.getIsbn());
                });
        return repositorioLibros.save(libro);
    }

    public Libro actualizar(Long id, Libro datos) {
        Libro libro = buscarPorId(id);
        libro.setTitulo(datos.getTitulo());
        return repositorioLibros.save(libro);
    }

    public void eliminar(Long id) {
        Libro libro = buscarPorId(id);
        repositorioLibros.delete(libro);
    }
}
```

## 💻 Archivo: `ControladorLibros.java`

```java
@RestController
@RequestMapping("/libros")
public class ControladorLibros {

    private final ServicioLibros servicioLibros;

    public ControladorLibros(ServicioLibros servicioLibros) {
        this.servicioLibros = servicioLibros;
    }

    @GetMapping
    public List<Libro> listarTodos() {
        return servicioLibros.listarTodos();
    }

    @GetMapping("/{id}")
    public Libro buscarPorId(@PathVariable Long id) {
        return servicioLibros.buscarPorId(id);
    }

    @GetMapping("/buscar")
    public ResponseEntity<Libro> buscarPorIsbn(@RequestParam String isbn) {
        return servicioLibros.buscarPorIsbn(isbn)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Libro> crear(@RequestBody Libro libro) {
        Libro creado = servicioLibros.crear(libro);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public Libro actualizar(@PathVariable Long id, @RequestBody Libro datos) {
        return servicioLibros.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicioLibros.eliminar(id);
        return ResponseEntity.ok().build();
    }
}
```

## 💻 Archivo: `ManejadorGlobalDeExcepciones.java`

```java
@ControllerAdvice
public class ManejadorGlobalDeExcepciones {

    @ExceptionHandler(LibroNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarLibroNoEncontrado(LibroNoEncontradoException ex) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cuerpo);
    }

    @ExceptionHandler(LibroDuplicadoException.class)
    public ResponseEntity<Map<String, String>> manejarLibroDuplicado(LibroDuplicadoException ex) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(cuerpo);
    }
}
```

</details>

## 🌳 Árbol de archivos (como se vería en VS Code)

```text
📁 src/main/java/
└── 📁 (paquete raíz del proyecto Biblioteca — sin cambios)
    ├── 📄 Libro.java (Módulo 3, sin cambios)
    ├── 📄 RepositorioLibros.java (Módulo 3, sin cambios)
    ├── 📄 LibroNoEncontradoException.java (Módulo 7, sin cambios)
    ├── 📄 LibroDuplicadoException.java (Módulo 7, sin cambios)
    ├── 📄 ServicioLibros.java (Módulo 5/7, sin cambios)
    ├── 📄 ControladorLibros.java (Módulo 5, sin cambios)
    ├── 📄 ManejadorGlobalDeExcepciones.java (Módulo 7, sin cambios)
    ├── 📄 Credencial.java (nueva)
    ├── 📄 RepositorioCredenciales.java (nueva)
    ├── 📄 ServicioDetallesUsuario.java (nueva)
    ├── 📄 ConfiguracionSeguridad.java (nueva)
    ├── 📄 UtilJwt.java (nueva)
    ├── 📄 FiltroAutenticacionJwt.java (nueva)
    └── 📄 ControladorAutenticacion.java (nueva)
📁 src/main/resources/
└── 📄 application.properties (sin cambios)
📄 pom.xml (modificado: JJWT)
```

## 💻 Archivo: `pom.xml` (fragmento agregado)

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

## 💻 Archivo: `Credencial.java`

```java
@Entity
public class Credencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nombreUsuario;

    private String contrasena; // codificada con PasswordEncoder, nunca en texto plano

    private String rol; // ej. "ROLE_USER"

    protected Credencial() {
    }

    public Credencial(String nombreUsuario, String contrasena, String rol) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public Long getId() { return id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getContrasena() { return contrasena; }
    public String getRol() { return rol; }
}
```

## 💻 Archivo: `RepositorioCredenciales.java`

```java
public interface RepositorioCredenciales extends JpaRepository<Credencial, Long> {
    Optional<Credencial> findByNombreUsuario(String nombreUsuario);
}
```

## 💻 Archivo: `ServicioDetallesUsuario.java`

```java
@Service
public class ServicioDetallesUsuario implements UserDetailsService {

    private final RepositorioCredenciales repositorioCredenciales;

    public ServicioDetallesUsuario(RepositorioCredenciales repositorioCredenciales) {
        this.repositorioCredenciales = repositorioCredenciales;
    }

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        Credencial credencial = repositorioCredenciales.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UsernameNotFoundException("No existe la cuenta " + nombreUsuario));

        return User.builder()
                .username(credencial.getNombreUsuario())
                .password(credencial.getContrasena())
                .authorities(credencial.getRol())
                .build();
    }
}
```

## 💻 Archivo: `UtilJwt.java`

```java
@Component
public class UtilJwt {

    // ⚠️ Solo para fines educativos: en producción, esta clave debe venir
    // de una variable de entorno o un gestor de secretos, nunca del código.
    private static final String CLAVE_SECRETA_EDUCATIVA =
            "esta-clave-es-solo-para-fines-educativos-del-curso-1234567890";

    private final SecretKey clave = Keys.hmacShaKeyFor(CLAVE_SECRETA_EDUCATIVA.getBytes(StandardCharsets.UTF_8));

    public String generarToken(UserDetails usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("rol", usuario.getAuthorities().iterator().next().getAuthority())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(1, ChronoUnit.HOURS)))
                .signWith(clave)
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser().verifyWith(clave).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false; // token manipulado, expirado o mal formado
        }
    }

    public String extraerNombreUsuario(String token) {
        return Jwts.parser().verifyWith(clave).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
```

## 💻 Archivo: `FiltroAutenticacionJwt.java`

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
            // Si el token no es válido, no se autentica: la SecurityFilterChain
            // rechazará la solicitud al llegar a un endpoint protegido.
        }

        filterChain.doFilter(request, response);
    }
}
```

## 💻 Archivo: `ControladorAutenticacion.java`

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

## 💻 Archivo: `ConfiguracionSeguridad.java`

```java
@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    private final FiltroAutenticacionJwt filtroAutenticacionJwt;

    public ConfiguracionSeguridad(FiltroAutenticacionJwt filtroAutenticacionJwt) {
        this.filtroAutenticacionJwt = filtroAutenticacionJwt;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // sin sesión, no aplica CSRF de formularios
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers("/auth/login").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.disable()) // Basic Auth (Ejemplo 05) queda reemplazada por JWT
                .addFilterBefore(filtroAutenticacionJwt, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```

## 🧭 Explicación paso a paso

1. `Credencial` es una entidad nueva, completamente separada de `Libro`:
   no tiene ninguna relación JPA con él. Representa una cuenta de acceso,
   no un recurso de negocio.
2. `ServicioDetallesUsuario` implementa la interfaz estándar
   `UserDetailsService` de Spring Security (Ejemplo 02): busca la
   `Credencial` por nombre de usuario y construye un `UserDetails` que
   Spring Security puede usar para verificar contraseñas y roles.
3. `UtilJwt` encapsula toda la lógica de JWT: generar un token firmado
   (`generarToken`), validar su firma (`validarToken`), y leer el nombre
   de usuario de un token ya validado (`extraerNombreUsuario`) — usando
   la biblioteca JJWT.
4. `ControladorAutenticacion.login` verifica las credenciales recibidas
   con el `AuthenticationManager` (que a su vez usa `ServicioDetallesUsuario`
   y el `PasswordEncoder`, como en el diagrama del Ejemplo 02), y si son
   válidas, genera y devuelve un JWT.
5. `FiltroAutenticacionJwt` se ejecuta en cada solicitud: si encuentra un
   encabezado `Authorization: Bearer <token>` válido, autentica al
   usuario en el `SecurityContextHolder` **antes** de que la solicitud
   llegue al `Controller`. Si el token es inválido o falta, no autentica
   nada — la `SecurityFilterChain` se encarga de rechazar la solicitud si
   el endpoint requiere autenticación.
6. `ConfiguracionSeguridad` conecta todo: marca la aplicación como
   `STATELESS` (sin sesión HTTP), permite `/auth/login` sin autenticación
   previa (para poder loguearse), exige autenticación para el resto de
   los endpoints, **desactiva Basic Auth** (Edge Case de spec.md: JWT
   reemplaza, no complementa, al mecanismo del Ejemplo 05), y registra
   `FiltroAutenticacionJwt` antes del filtro estándar de usuario/
   contraseña de Spring Security.
7. Ninguna línea de `Libro`, `ServicioLibros` o `ControladorLibros`
   cambió: siguen exactamente como en el Ejemplo 05 (FR-015).

## ✅ Resultado esperado

```text
Método: POST
URL: http://localhost:8080/auth/login
Cuerpo: {"nombreUsuario": "ana", "contrasena": "clave123"}
Respuesta: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmEiLCJyb2wiOiJST0xFX1VTRVIifQ.abc123..."
}
```

```text
Método: GET
URL: http://localhost:8080/libros
Autorización: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmEiLCJyb2wiOiJST0xFX1VTRVIifQ.abc123...
Respuesta: 200 OK
[
  {"id": 1, "isbn": "978-0-13-468599-1", "titulo": "Effective Java"}
]
```

```text
Método: GET
URL: http://localhost:8080/libros
(sin encabezado Authorization)
Respuesta: 401 Unauthorized
```

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué componente de este ejemplo es responsable de
verificar la firma de un JWT en cada solicitud entrante?

- **A.** `ControladorAutenticacion`.
- **B.** `FiltroAutenticacionJwt`, a través de `UtilJwt.validarToken`.
- **C.** `Credencial`.
- **D.** `ServicioLibros`.

<details><summary>🔑 Ver respuesta</summary>

**B.** `ControladorAutenticacion` solo emite el token en el login;
`FiltroAutenticacionJwt` es quien valida el token en cada solicitud
subsiguiente.

</details>

**2. [Selección múltiple]** ¿Cuáles de las siguientes afirmaciones sobre
este ejemplo son correctas?

- **A.** `Credencial` tiene una relación JPA directa con `Libro`.
- **B.** `ConfiguracionSeguridad` desactiva Basic Auth y activa el filtro JWT.
- **C.** La aplicación se configura como `STATELESS`.
- **D.** `ServicioLibros` y `ControladorLibros` no cambiaron respecto al Ejemplo 05.

<details><summary>🔑 Ver respuesta</summary>

**B, C y D.** A es falsa: `Credencial` es una entidad de seguridad
independiente, sin ninguna relación con `Libro`.

</details>

**3. [Abierta]** Si `FiltroAutenticacionJwt` se registrara **después**
del filtro estándar de usuario/contraseña en vez de antes
(`addFilterBefore`), ¿qué problema podría causar?

<details><summary>🔑 Ver respuesta modelo</summary>

El filtro estándar de usuario/contraseña podría rechazar la solicitud
(o procesarla de forma incorrecta) antes de que `FiltroAutenticacionJwt`
tuviera oportunidad de autenticar al usuario a partir del token JWT — el
orden de los filtros en la cadena importa porque cada uno decide si la
solicitud continúa hacia el siguiente.

</details>
