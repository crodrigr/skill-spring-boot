# Colección de Postman — Módulo 08 (seguridad)

Colección de Postman (formato v2.1) para probar **todas las rutas** de la API de estudiantes protegida con Spring Security y JWT.

| Archivo | Qué es |
|---|---|
| `students-api-security.postman_collection.json` | **41 peticiones** en 8 carpetas, con sus pruebas (`pm.test`) |
| `academy-local.postman_environment.json` | Entorno **Academy local**: solo `baseUrl` (`http://localhost:8080`) |

Sirve para [`students-api-security`](../students-api-security/) (versión completa) y para [`students-api-security-facil`](../students-api-security-facil/) (versión fácil). En la fácil, la petición **«Quién soy»** falla porque `GET /api/v1/auth/me` no existe allí: es lo esperado.

Guías: [01-guia-crud-estudiantes-con-security.md](../../01-guia-crud-estudiantes-con-security.md) · [02-guia-security-facil.md](../../02-guia-security-facil.md) · [04-curl-probar-todas-las-apis.md](../../04-curl-probar-todas-las-apis.md)

## Cómo usarla

1. En Postman: **Import** y seleccionar los dos archivos `.json` de esta carpeta.
2. Elegir el entorno **Academy local** (arriba a la derecha).
3. Arrancar la aplicación (`./mvnw spring-boot:run`) con MySQL en marcha y la base `academy` **vacía**.
4. Abrir la colección → **Run collection** y ejecutar **en orden**.

## Qué hay en la colección

| Carpeta | Qué prueba | Códigos |
|---|---|---|
| 1. Público | `health` y los contratos OpenAPI, sin token | `200` |
| 2. Login | Entra como `admin` y como `ana` (**guarda los tokens**) y prueba los errores | `200` · `401` · `400` |
| 3. Sin token o con token malo | Todo lo protegido rechaza | `401` |
| 4. Cargar datos de ejemplo | El `admin` carga a Ana, Luis y María; `ana` no puede | `200` · `403` |
| 5. Ver estudiantes | Lista, páginas, filtro, orden, id, correo y `ping` | `200` · `400` · `404` |
| 6. Crear y editar | `POST`, `PUT` y `PATCH` con sus errores | `201` · `200` · `409` · `400` · `404` |
| 7. Borrar | `ana` no puede; `admin` sí | `403` · `204` · `404` |
| 8. Rutas que no existen | Ruta inexistente y método no permitido | `404` · `405` |

## Cómo funciona por dentro

- **Los tokens se guardan solos.** Las peticiones de la carpeta 2 guardan `tokenAdmin` y `tokenAna` como **variables de la colección**; las demás peticiones los envían en la cabecera `Authorization: Bearer {{tokenAna}}`.
- **Se guardan también** `studentId` y `email` (el estudiante que crea la carpeta 6) para editarlo y borrarlo después.
- **Se puede repetir sin vaciar la base:** el correo del estudiante nuevo lleva un sufijo (`{{run}}`) que cambia en cada ejecución. La petición «Reemplazar con el correo de otro» sí necesita que `ana@mail.com` exista, y lo crea la carpeta 4.
- **Cada petición trae sus pruebas** con el código HTTP esperado. Las de error comprueban además que el cuerpo sea un `ErrorResponse` (`message` y `status`).

> ⚠️ **Solo para fines educativos.** Las cuentas `admin` / `admin123` y `ana` / `ana123`, y la clave del token, son de práctica.
> 💡 Los tokens duran 1 hora: si pasa más tiempo, volver a ejecutar la carpeta 2.
