# 🔴 Avanzado 02 — Diagnosticar un JWT válido rechazado

## 🧩 Problema

Un compañero de curso terminó el Avanzado 01, pero al probar en Insomnia
un JWT recién emitido por `POST /auth/login` contra `GET /autores`,
sigue recibiendo `401 Unauthorized` — aunque el token es válido (lo
verificó decodificándolo manualmente y comprobó que no expiró).

Esta es la configuración que escribió:

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
            .build();
}
```

**Pregunta**: identificá qué falta en esta configuración para que
`FiltroAutenticacionJwt` (ya implementado en el Avanzado 01) realmente
participe en el proceso de autenticación, y corregilo.

## 💻 Código o contexto de partida

Usá como referencia `ConfiguracionSeguridad` completa del Ejemplo 08 y
tu propia solución del Avanzado 01.

## 📏 Criterios de evaluación de la solución

- Identifica correctamente que falta la línea
  `.addFilterBefore(filtroAutenticacionJwt, UsernamePasswordAuthenticationFilter.class)`:
  sin registrar el filtro en la cadena, `FiltroAutenticacionJwt` nunca se
  ejecuta, aunque la clase exista y esté bien implementada.
- Explica que el resto de la configuración (`STATELESS`, `permitAll` en
  `/auth/login`, `authenticated()` para el resto) es correcta — el
  problema es específicamente la falta de registro del filtro.
- Corrige la configuración agregando la línea faltante.

## 🚧 Restricciones

- No modificar `FiltroAutenticacionJwt` ni `UtilJwt`: el error está
  únicamente en `ConfiguracionSeguridad`.

## 📊 Dificultad

Avanzado.

## 🎓 Resultados de aprendizaje

`RA-9`.
