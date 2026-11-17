# 🔴 Avanzado 01 — Implementar el filtro de validación de JWT

## 🧩 Problema

El endpoint `POST /auth/login` del ejercicio anterior (Intermedio 03) ya
emite un JWT correctamente, pero ningún otro endpoint lo valida todavía:
`GET /autores` sigue exigiendo Basic Auth (Intermedio 01), no un token.

**Pregunta**: implementá `FiltroAutenticacionJwt` (extendiendo
`OncePerRequestFilter`) que lea el encabezado `Authorization: Bearer
<token>`, valide el token con `UtilJwt.validarToken`, y —si es válido—
autentique al usuario en el `SecurityContextHolder`. Registralo en
`ConfiguracionSeguridad` para que se ejecute antes del filtro estándar de
usuario/contraseña, y configurá la aplicación como `STATELESS`,
desactivando Basic Auth.

## 💻 Código o contexto de partida

Partí de la solución de Intermedio 03 (`ControladorAutenticacion` ya
emitiendo JWT) más `Credencial`/`RepositorioCredenciales`/
`ServicioDetallesUsuario`/`UtilJwt` del mismo ejercicio.

## 📏 Criterios de evaluación de la solución

- `FiltroAutenticacionJwt` lee el encabezado `Authorization`, valida el
  token, y solo autentica si `UtilJwt.validarToken` devuelve `true`.
- Si el token falta o es inválido, el filtro **no** rechaza la solicitud
  directamente (no lanza una excepción ni corta el flujo): deja que la
  `SecurityFilterChain` la rechace si el endpoint requiere autenticación.
- `ConfiguracionSeguridad` registra el filtro con `addFilterBefore(...,
  UsernamePasswordAuthenticationFilter.class)`, configura
  `SessionCreationPolicy.STATELESS`, y desactiva Basic Auth
  (`httpBasic(basic -> basic.disable())`).

## 🚧 Restricciones

- `FiltroAutenticacionJwt` no debe consultar `RepositorioAutores` ni
  ninguna entidad de dominio: solo interactúa con `UtilJwt` y
  `ServicioDetallesUsuario`.

## 📊 Dificultad

Avanzado.

## 🎓 Resultados de aprendizaje

`RA-9`.
