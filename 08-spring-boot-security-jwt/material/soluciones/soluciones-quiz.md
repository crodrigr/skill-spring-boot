# 🔑 Soluciones — Quiz 08

> Material docente. No enlazar ni compartir con la audiencia estudiante.
> Tabla resumen de referencia rápida; el texto completo de cada respuesta
> vive en `quiz-08.md`, dentro de su bloque `<details>`.

| N.º | Tipo | Respuesta/síntesis | RA |
|---|---|---|---|
| 1 | Selección | Autenticación (quién es) + autorización (qué puede hacer) | RA-1 |
| 2 | Selección múltiple | A, B, D (no `EntityManagerFactory`, es de JPA) | RA-2 |
| 3 | Selección | B — cada solicitud se procesa de forma independiente | RA-3 |
| 4 | Abierta | Cualquiera de las 7 aplicaciones típicas, con justificación correcta | RA-3 |
| 5 | Selección múltiple | A, B, D (no C, puede haber varias `SecurityFilterChain`) | RA-4 |
| 6 | Abierta | Distintas partes de la app necesitan reglas de seguridad distintas | RA-4 |
| 7 | Selección | B — protección automática + contraseña temporal generada | RA-5 |
| 8 | Selección múltiple | A, B, D (no token JWT, es de Basic Auth) | RA-6 |
| 9 | Abierta | Copiar la contraseña de consola + configurar Basic Auth con usuario `user` | RA-5, RA-6 |
| 10 | Selección múltiple | A, C, D (no B, Base64 no es encriptación) | RA-7 |
| 11 | Abierta | Emisión (login) → uso (reenvío en cada solicitud) → validación (recalcular firma) | RA-8 |
| 12 | Selección | C — `ControladorAutenticacion` emite el token | RA-9 |
| 13 | Selección múltiple | A, C, D (no B, el filtro no consulta entidades de dominio) | RA-9 |
| 14 | Abierta | Acepta tokens manipulados porque nunca verifica la firma | RA-9 |
| 15 | Selección múltiple | A, B, D (no C, sin token debe rechazarse) | RA-10 |
| 16 | Abierta | Los casos de error prueban que la protección realmente funciona, no solo el caso feliz | RA-10 |
