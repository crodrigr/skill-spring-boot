# 🟡 Intermedio 03 — Implementar el endpoint de login que emite un JWT

## 🧩 Problema

El proyecto de `ControladorAutores` (Intermedio 01) ya tiene
`spring-boot-starter-security` agregado. Ahora se agregaron `Credencial`,
`RepositorioCredenciales`, `ServicioDetallesUsuario` y `UtilJwt` (dados
como código de partida), pero todavía no existe ningún endpoint de
login.

**Pregunta**: implementá `ControladorAutenticacion`, con un endpoint
`POST /auth/login` que reciba `nombreUsuario` y `contrasena`, los
verifique usando el `AuthenticationManager`, y devuelva un JWT generado
con `UtilJwt.generarToken` si son válidos.

## 💻 Código o contexto de partida

<details>
<summary>📄 Ver código completo de <code>Credencial.java</code>, <code>RepositorioCredenciales.java</code>, <code>ServicioDetallesUsuario.java</code> y <code>UtilJwt.java</code> (ya dados, no crear de nuevo)</summary>

## 💻 Archivo: `Credencial.java`

```java
@Entity
public class Credencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nombreUsuario;

    private String contrasena;

    private String rol;

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

</details>

## 📏 Criterios de evaluación de la solución

- `POST /auth/login` recibe `nombreUsuario`/`contrasena` y usa
  `AuthenticationManager.authenticate` para verificarlas (no compara
  contraseñas a mano).
- Si las credenciales son válidas, devuelve `200 OK` con un cuerpo que
  incluye el JWT generado por `UtilJwt.generarToken`.
- No crea ningún endpoint que devuelva la contraseña de la `Credencial`
  en texto plano.

## 🚧 Restricciones

- No implementar todavía el filtro que valida el JWT en las solicitudes
  siguientes (eso se resuelve en el Avanzado 01).

## 📊 Dificultad

Intermedio.

## 🎓 Resultados de aprendizaje

`RA-9`.
