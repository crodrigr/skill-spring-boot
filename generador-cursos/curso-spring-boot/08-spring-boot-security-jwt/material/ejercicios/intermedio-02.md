# 🟡 Intermedio 02 — Ubicar componentes en la arquitectura de Spring Security

## 🧩 Problema

Un compañero de curso describe estos cuatro momentos del proceso de
autenticación de una API protegida con Spring Security, pero no sabe qué
componente de la arquitectura es responsable de cada uno:

- Verificar que la contraseña enviada coincide con la almacenada (codificada).
- Almacenar el resultado de una autenticación exitosa para el resto de la solicitud.
- Interceptar la solicitud entrante antes de que llegue al `Controller`.
- Obtener los datos del usuario (nombre, contraseña codificada, rol) desde la base de datos.

**Pregunta**: identificá, para cada uno de los cuatro momentos, el
componente de la arquitectura de Spring Security responsable
(`Security Filter Chain`, `Authentication Manager`, `Authentication
Providers`, `PasswordEncoder`, `UserDetailsService` o
`SecurityContextHolder`), y justificá brevemente cada elección.

## 💻 Código o contexto de partida

Este ejercicio es conceptual: no requiere un proyecto Spring Boot propio.
Usá como referencia el diagrama y la explicación del
[Ejemplo 02 — Arquitectura de Spring Boot Security](../ejemplos/02-arquitectura-de-spring-security.md).

## 📏 Criterios de evaluación de la solución

- Asigna correctamente los cuatro momentos a su componente responsable.
- La justificación de cada elección es coherente con el flujo descrito en
  el Ejemplo 02 (qué componente actúa antes/después de cuál).
- No confunde `Authentication Providers` (que verifican) con
  `SecurityContextHolder` (que solo almacena el resultado ya verificado).

## 🚧 Restricciones

- No se requiere escribir ningún código Java para este ejercicio.

## 📊 Dificultad

Intermedio.

## 🎓 Resultados de aprendizaje

`RA-1`, `RA-2`.
