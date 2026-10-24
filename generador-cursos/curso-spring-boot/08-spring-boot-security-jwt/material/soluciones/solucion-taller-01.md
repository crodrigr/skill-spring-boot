# 🔑 Solución — Taller 01: Spring Security y JWT sobre la API de pacientes

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante. Contiene el entregable completo del Taller 01.

## 🌳 Árbol de archivos (entregable final)

```text
📁 taller-01-security-jwt-pacientes
└── 📁 src/main
    ├── 📁 java/com/medisalud
    │   ├── 📁 presentacion                                (capa de presentación)
    │   │   ├── 📁 controlador
    │   │   │   └── 📄 ControladorPacientes.java
    │   │   ├── 📁 dto
    │   │   │   ├── 📄 RespuestaError.java
    │   │   │   ├── 📄 SolicitudLogin.java
    │   │   │   └── 📄 RespuestaLogin.java
    │   │   └── 📁 excepcion
    │   │       └── 📄 ManejadorGlobalDeExcepciones.java
    │   ├── 📁 negocio                                     (capa de negocio)
    │   │   ├── 📁 model
    │   │   │   └── 📄 Paciente.java
    │   │   ├── 📁 service
    │   │   │   └── 📄 ServicioPacientes.java
    │   │   └── 📁 excepcion
    │   │       └── 📄 PacienteNoEncontradoException.java
    │   ├── 📁 persistence                                 (capa de persistencia)
    │   │   └── 📁 repository
    │   │       └── 📄 RepositorioPacientes.java
    │   └── 📁 security
    │       ├── 📁 presentacion                            (capa de presentación)
    │       │   └── 📁 controlador
    │       │       └── 📄 ControladorAutenticacion.java
    │       ├── 📁 negocio                                 (capa de negocio)
    │       │   ├── 📁 model
    │       │   │   └── 📄 Usuario.java
    │       │   └── 📁 service
    │       │       └── 📄 ServicioDetallesUsuario.java
    │       ├── 📁 persistence                             (capa de persistencia)
    │       │   └── 📁 repository
    │       │       └── 📄 RepositorioUsuarios.java
    │       ├── 📁 config
    │       │   └── 📄 ConfiguracionSeguridad.java
    │       └── 📁 jwt
    │           ├── 📄 UtilJwt.java
    │           └── 📄 FiltroAutenticacionJwt.java
    └── 📁 resources
        └── 📄 application.properties
```

**Capas**: tanto el paquete raíz `com.medisalud` como
`com.medisalud.security` respetan `presentacion` → `negocio` →
`persistence`; las entidades van en `negocio/model` y los repositorios en
`persistence/repository`. `config` y `jwt` son paquetes transversales, no
capas. Ningún controlador inyecta un
repositorio: `ControladorPacientes` usa `ServicioPacientes`, y
`ControladorAutenticacion` usa `AuthenticationManager` y
`ServicioDetallesUsuario`.

<details>
<summary>📄 Ver código completo de <code>Paciente.java</code>, <code>RepositorioPacientes.java</code>, <code>PacienteNoEncontradoException.java</code>, <code>ServicioPacientes.java</code>, <code>ControladorPacientes.java</code> y <code>ManejadorGlobalDeExcepciones.java</code> (reutilizados del Módulo 7, sin cambios)</summary>

## 📄 Archivo: `Paciente.java` (`com.medisalud.negocio.model`, sin cambios)

```java
package com.medisalud.negocio.model;

@Entity
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigo;

    private String nombre;

    protected Paciente() {
    }

    public Paciente(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
```

## 📄 Archivo: `RepositorioPacientes.java` (`com.medisalud.persistence.repository`, sin cambios)

```java
package com.medisalud.persistence.repository;

import com.medisalud.negocio.model.Paciente;

public interface RepositorioPacientes extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByCodigo(String codigo);
}
```

## 📄 Archivo: `PacienteNoEncontradoException.java` (`com.medisalud.negocio.excepcion`, sin cambios)

```java
package com.medisalud.negocio.excepcion;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PacienteNoEncontradoException extends RuntimeException {

    public PacienteNoEncontradoException(Long id) {
        super("No existe un paciente con id " + id);
    }
}
```

## 📄 Archivo: `ServicioPacientes.java` (`com.medisalud.negocio.service`, sin cambios)

```java
package com.medisalud.negocio.service;

import com.medisalud.negocio.model.Paciente;
import com.medisalud.negocio.excepcion.PacienteNoEncontradoException;
import com.medisalud.persistence.repository.RepositorioPacientes;

@Service
public class ServicioPacientes {

    private final RepositorioPacientes repositorioPacientes;

    public ServicioPacientes(RepositorioPacientes repositorioPacientes) {
        this.repositorioPacientes = repositorioPacientes;
    }

    public List<Paciente> listarTodos() {
        return repositorioPacientes.findAll();
    }

    public Paciente buscarPorId(Long id) {
        return repositorioPacientes.findById(id)
                .orElseThrow(() -> new PacienteNoEncontradoException(id));
    }

    public Paciente crear(Paciente paciente) {
        return repositorioPacientes.save(paciente);
    }

    public Paciente actualizar(Long id, Paciente datos) {
        Paciente paciente = buscarPorId(id);
        paciente.setNombre(datos.getNombre());
        return repositorioPacientes.save(paciente);
    }

    public void eliminar(Long id) {
        Paciente paciente = buscarPorId(id);
        repositorioPacientes.delete(paciente);
    }
}
```

## 📄 Archivo: `ControladorPacientes.java` (`com.medisalud.presentacion.controlador`, sin cambios)

```java
package com.medisalud.presentacion.controlador;

import com.medisalud.negocio.model.Paciente;
import com.medisalud.negocio.service.ServicioPacientes;

@RestController
@RequestMapping("/pacientes")
public class ControladorPacientes {

    private final ServicioPacientes servicioPacientes;

    public ControladorPacientes(ServicioPacientes servicioPacientes) {
        this.servicioPacientes = servicioPacientes;
    }

    @GetMapping
    public List<Paciente> listarTodos() {
        return servicioPacientes.listarTodos();
    }

    @GetMapping("/{id}")
    public Paciente buscarPorId(@PathVariable Long id) {
        return servicioPacientes.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Paciente> crear(@RequestBody Paciente paciente) {
        Paciente creado = servicioPacientes.crear(paciente);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public Paciente actualizar(@PathVariable Long id, @RequestBody Paciente datos) {
        return servicioPacientes.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicioPacientes.eliminar(id);
        return ResponseEntity.ok().build();
    }
}
```

## 📄 Archivo: `RespuestaError.java` (`com.medisalud.presentacion.dto`, nueva)

DTO del Módulo 7 (cuerpo de error `{"error": "<mensaje>"}`), sin cambios.

```java
package com.medisalud.presentacion.dto;

public record RespuestaError(String error) {
}
```

## 📄 Archivo: `ManejadorGlobalDeExcepciones.java` (`com.medisalud.presentacion.excepcion`, sin cambios)

```java
package com.medisalud.presentacion.excepcion;

import com.medisalud.presentacion.dto.RespuestaError;

@ControllerAdvice
public class ManejadorGlobalDeExcepciones {

    @ExceptionHandler(PacienteNoEncontradoException.class)
    public ResponseEntity<RespuestaError> manejarPacienteNoEncontrado(PacienteNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RespuestaError(ex.getMessage()));
    }
}
```

</details>

## 📄 Archivo: `Usuario.java` (`com.medisalud.security.negocio.model`, nueva)

```java
package com.medisalud.security.negocio.model;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nombreUsuario;

    private String contrasena; // codificada con PasswordEncoder

    private String rol;

    protected Usuario() {
    }

    public Usuario(String nombreUsuario, String contrasena, String rol) {
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

## 📄 Archivo: `RepositorioUsuarios.java` (`com.medisalud.security.persistence.repository`, nueva)

```java
package com.medisalud.security.persistence.repository;

import com.medisalud.security.negocio.model.Usuario;

public interface RepositorioUsuarios extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
}
```

## 📄 Archivo: `ServicioDetallesUsuario.java` (`com.medisalud.security.negocio.service`, nueva)

```java
package com.medisalud.security.negocio.service;

import com.medisalud.security.negocio.model.Usuario;
import com.medisalud.security.persistence.repository.RepositorioUsuarios;

@Service
public class ServicioDetallesUsuario implements UserDetailsService {

    private final RepositorioUsuarios repositorioUsuarios;

    public ServicioDetallesUsuario(RepositorioUsuarios repositorioUsuarios) {
        this.repositorioUsuarios = repositorioUsuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        Usuario usuario = repositorioUsuarios.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UsernameNotFoundException("No existe la cuenta " + nombreUsuario));

        return User.builder()
                .username(usuario.getNombreUsuario())
                .password(usuario.getContrasena())
                .authorities(usuario.getRol())
                .build();
    }
}
```

## 📄 Archivo: `UtilJwt.java` (`com.medisalud.security.jwt`, nueva)

```java
package com.medisalud.security.jwt;

@Component
public class UtilJwt {

    // ⚠️ Solo para fines educativos: en producción, esta clave debe venir
    // de una variable de entorno o un gestor de secretos, nunca del código.
    private static final String CLAVE_SECRETA_EDUCATIVA =
            "esta-clave-es-solo-para-fines-educativos-del-taller-0987654321";

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
            return false;
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

## 📄 Archivo: `FiltroAutenticacionJwt.java` (`com.medisalud.security.jwt`, nueva)

```java
package com.medisalud.security.jwt;

import com.medisalud.security.negocio.service.ServicioDetallesUsuario;

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

## 📄 Archivo: `SolicitudLogin.java` (`com.medisalud.presentacion.dto`, nueva)

DTO de entrada del login: declara exactamente qué campos acepta `POST /auth/login`
(antes se recibía un `Map<String, String>` sin forma definida).

```java
package com.medisalud.presentacion.dto;

public record SolicitudLogin(String nombreUsuario, String contrasena) {
}
```

## 📄 Archivo: `RespuestaLogin.java` (`com.medisalud.presentacion.dto`, nueva)

DTO de salida: el cuerpo `{"token": "<jwt>"}`.

```java
package com.medisalud.presentacion.dto;

public record RespuestaLogin(String token) {
}
```

## 📄 Archivo: `ControladorAutenticacion.java` (`com.medisalud.security.presentacion.controlador`, nueva)

```java
package com.medisalud.security.presentacion.controlador;

import com.medisalud.security.jwt.UtilJwt;
import com.medisalud.security.negocio.service.ServicioDetallesUsuario;
import com.medisalud.presentacion.dto.SolicitudLogin;
import com.medisalud.presentacion.dto.RespuestaLogin;

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
    public ResponseEntity<RespuestaLogin> login(@RequestBody SolicitudLogin solicitud) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        solicitud.nombreUsuario(),
                        solicitud.contrasena()));

        UserDetails usuario = servicioDetallesUsuario.loadUserByUsername(solicitud.nombreUsuario());
        String token = utilJwt.generarToken(usuario);

        return ResponseEntity.ok(new RespuestaLogin(token));
    }
}
```

## 📄 Archivo: `ConfiguracionSeguridad.java` (`com.medisalud.security.config`, nueva)

```java
package com.medisalud.security.config;

import com.medisalud.security.jwt.FiltroAutenticacionJwt;

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
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers("/auth/login").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(filtroAutenticacionJwt, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```

## 📄 Archivo: `application.properties` (Módulo 5, sin cambios)

```properties
spring.datasource.url=jdbc:h2:mem:medisalud;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

## 🧪 Pruebas en Insomnia (los 5 casos del Paso 7)

**1. Login exitoso**

```text
Método: POST
URL: http://localhost:8080/auth/login
Cuerpo: {"nombreUsuario": "diego", "contrasena": "clave123"}
Respuesta: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWVnbyIsInJvbCI6IlJPTEVfVVNFUiJ9.abc123..."
}
```

**2. Acceso con token válido**

```text
Método: GET
URL: http://localhost:8080/pacientes/1
Autorización: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWVnbyIsInJvbCI6IlJPTEVfVVNFUiJ9.abc123...
Respuesta: 200 OK
{
  "id": 1,
  "codigo": "P-030",
  "nombre": "Diego Marín"
}
```

**3. Acceso sin ningún token**

```text
Método: GET
URL: http://localhost:8080/pacientes/1
(sin encabezado Authorization)
Respuesta: 401 Unauthorized
```

**4. Login con credenciales inválidas**

```text
Método: POST
URL: http://localhost:8080/auth/login
Cuerpo: {"nombreUsuario": "diego", "contrasena": "clave-incorrecta"}
Respuesta: 401 Unauthorized
```

**5. Acceso con token manipulado**

```text
Método: GET
URL: http://localhost:8080/pacientes/1
Autorización: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWVnbyIsInJvbCI6IlJPTEVfQURNSU4ifQ.abc123... (payload alterado, firma sin recalcular)
Respuesta: 401 Unauthorized
```

**Nota sobre FR-015**: `Paciente`, `ServicioPacientes` y
`ControladorPacientes` son exactamente los del Taller 01 del Módulo 7 —
ninguna línea de su lógica de negocio cambió; toda la novedad de este
Taller vive en el paquete nuevo `com.medisalud.security`.
