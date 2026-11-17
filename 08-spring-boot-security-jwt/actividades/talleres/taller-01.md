# 🛠️ Taller 01 — Spring Security y JWT sobre la API de pacientes

## 🎯 Objetivo (`RA-5`, `RA-9`, `RA-10`)

Proteger la API de pacientes (MediSalud) con autenticación stateless
completa: primero un vistazo rápido a Basic Auth, y luego un login que
emite un JWT validado por un filtro propio en cada solicitud.

## 🌍 Contexto

Este Taller parte de la solución del Taller 01 del Módulo 7
(`ControladorPacientes`/`ServicioPacientes`, ya con `PacienteNoEncontradoException`
y `ManejadorGlobalDeExcepciones` aplicados, en sus paquetes por capa `com.medisalud.presentacion.controlador`, `com.medisalud.negocio.service`
`com.medisalud.negocio.model` y `com.medisalud.persistence.repository`, más
`com.medisalud.negocio.excepcion` y `com.medisalud.presentacion.excepcion`).
Ninguna de esas clases cambia su comportamiento en este Taller: solo se
les agrega una funcionalidad nueva de seguridad, en el paquete
`com.medisalud.security`.

## 🏗️ Capas del paquete `security`

La seguridad no rompe la arquitectura del proyecto: respeta las mismas tres
capas, replicadas dentro de `com.medisalud.security`, y suma dos paquetes
transversales propios de Spring Security.

| Capa / paquete | Paquete | Clase en este taller |
|---|---|---|
| **Presentación** | `com.medisalud.security.presentacion.controlador` | `ControladorAutenticacion` (`POST /auth/login`) |
| **Negocio** | `com.medisalud.security.negocio.service` | `ServicioDetallesUsuario` |
| **Negocio** | `com.medisalud.security.negocio.model` | `Usuario` |
| **Persistencia** | `com.medisalud.security.persistence.repository` | `RepositorioUsuarios` |
| Transversal | `com.medisalud.security.config` | `ConfiguracionSeguridad` |
| Transversal | `com.medisalud.security.jwt` | `UtilJwt`, `FiltroAutenticacionJwt` |

Regla de dependencia: `presentacion` → `negocio` → `persistence`.
`ControladorAutenticacion` no accede a `RepositorioUsuarios` directamente:
lo hace a través de `AuthenticationManager` y `ServicioDetallesUsuario`.

## 🪜 Pasos

1. **Agregar Spring Security y probar Basic Auth**: agregá
   `spring-boot-starter-security` al `pom.xml` del proyecto, ejecutalo, y
   confirmá con Insomnia que `GET /pacientes` ahora exige autenticación
   (usuario `user` + contraseña autogenerada en consola).
2. **Crear `Usuario` y `RepositorioUsuarios`**
   (`com.medisalud.security.negocio.model`/`com.medisalud.security.persistence.repository`):
   una entidad con `nombreUsuario`, `contrasena` (codificada) y `rol`, y
   su repositorio con `findByNombreUsuario`.
3. **Crear `ServicioDetallesUsuario`** (`com.medisalud.security.negocio.service`,
   implementa `UserDetailsService`), que busca el `Usuario` por
   nombre de usuario y construye el `UserDetails` correspondiente.
4. **Configurar `ConfiguracionSeguridad`** (`com.medisalud.security.config`)
   como `SessionCreationPolicy.STATELESS`, permitiendo `/auth/login` sin
   autenticación previa y exigiendo autenticación para el resto —
   **desactivando Basic Auth** en favor de JWT.
5. **Implementar `ControladorAutenticacion`** (`com.medisalud.security.presentacion.controlador`,
   `POST /auth/login`) que verifica las credenciales con el
   `AuthenticationManager` y devuelve un JWT (`UtilJwt.generarToken`,
   `com.medisalud.security.jwt`). Recibe un DTO `SolicitudLogin` y responde
   con un DTO `RespuestaLogin`, ambos `record` en `com.medisalud.presentacion.dto`.
6. **Implementar y registrar `FiltroAutenticacionJwt`**
   (`com.medisalud.security.jwt`, extiende `OncePerRequestFilter`) que
   valida el JWT del encabezado `Authorization` en cada solicitud, y
   registralo en `ConfiguracionSeguridad` antes del filtro estándar de
   usuario/contraseña.
7. **Probar el flujo completo en Insomnia**, documentando los 5 casos:
   - Login exitoso (`POST /auth/login` con credenciales válidas → JWT).
   - Acceso a `GET /pacientes` con el token recibido → `200 OK`.
   - Acceso a `GET /pacientes` sin ningún token → `401 Unauthorized`.
   - Login con credenciales inválidas → `401 Unauthorized`.
   - Acceso con un token manipulado (payload alterado) → `401 Unauthorized`.

## 💡 Ejemplo resuelto (parcial)

```java
package com.medisalud.security.negocio.model;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nombreUsuario;

    private String contrasena;

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

(El resto de las clases —`RepositorioUsuarios`,
`ServicioDetallesUsuario`, `ConfiguracionSeguridad`, `UtilJwt`,
`FiltroAutenticacionJwt`, `ControladorAutenticacion`— seguís el mismo
patrón mostrado en el [Ejemplo 08](../../material/ejemplos/08-autenticacion-stateless-con-jwt.md),
adaptado al paquete `com.medisalud.security`.)

## 📦 Entregable

```text
📁 taller-01-security-jwt-pacientes
└── 📁 src/main
    ├── 📁 java/com/medisalud
    │   ├── 📁 presentacion
    │   │   ├── 📁 controlador
    │   │   │   └── 📄 ControladorPacientes.java (sin cambios)
    │   │   ├── 📁 dto
    │   │   │   ├── 📄 RespuestaError.java (sin cambios)
    │   │   │   ├── 📄 SolicitudLogin.java
    │   │   │   └── 📄 RespuestaLogin.java
    │   │   └── 📁 excepcion
    │   │       └── 📄 ManejadorGlobalDeExcepciones.java (sin cambios)
    │   ├── 📁 negocio
    │   │   ├── 📁 model
    │   │   │   └── 📄 Paciente.java (sin cambios)
    │   │   ├── 📁 service
    │   │   │   └── 📄 ServicioPacientes.java (sin cambios)
    │   │   └── 📁 excepcion
    │   │       └── 📄 PacienteNoEncontradoException.java (sin cambios)
    │   ├── 📁 persistence
    │   │   └── 📁 repository
    │   │       └── 📄 RepositorioPacientes.java (sin cambios)
    │   └── 📁 security
    │       ├── 📁 presentacion
    │       │   └── 📁 controlador
    │       │       └── 📄 ControladorAutenticacion.java
    │       ├── 📁 negocio
    │       │   ├── 📁 model
    │       │   │   └── 📄 Usuario.java
    │       │   └── 📁 service
    │       │       └── 📄 ServicioDetallesUsuario.java
    │       ├── 📁 persistence
    │       │   └── 📁 repository
    │       │       └── 📄 RepositorioUsuarios.java
    │       ├── 📁 config
    │       │   └── 📄 ConfiguracionSeguridad.java
    │       └── 📁 jwt
    │           ├── 📄 UtilJwt.java
    │           └── 📄 FiltroAutenticacionJwt.java
    └── 📁 resources
        └── 📄 application.properties (sin cambios)
```

## 🧪 Casos de prueba

Los 5 casos del Paso 7 (login exitoso, acceso con token válido, acceso
sin token, login con credenciales inválidas, acceso con token
manipulado), cada uno documentado con Método, URL, Cuerpo (si aplica) y
Respuesta exacta.

## 📏 Criterios de evaluación

- `Paciente`, `ServicioPacientes` y `ControladorPacientes` no cambiaron
  ninguna línea de su lógica de negocio (FR-015).
- Las clases nuevas de `security` viven en el paquete de su capa
  (`presentacion`, `negocio`, `persistence`) o en un paquete transversal
  (`config`, `jwt`), y ningún controlador accede a un repositorio
  directamente.
- Las 5 pruebas de Insomnia del Paso 7 están documentadas con su
  respuesta exacta.
- La clave secreta de JWT usada incluye la advertencia de que es solo
  para fines educativos (FR-026).
- `FiltroAutenticacionJwt` está registrado antes del filtro estándar de
  usuario/contraseña en `ConfiguracionSeguridad`.
