# Colección de Postman — Módulo 06

Colecciones de Postman (formato v2.1) con las peticiones de los proyectos de este módulo.

| Archivo | Proyecto | Guía |
|---|---|---|
| `students-api.postman_collection.json` | `students-api` | [04-01-guia-crud-estudiantes-con-swagger.md](../../04-01-guia-crud-estudiantes-con-swagger.md) |
| `academica-swagger.postman_collection.json` | `academica-swagger` | [04-02-academica-swagger.md](../../04-02-academica-swagger.md) |
| `academy-local.postman_environment.json` | Entorno con `baseUrl` (`http://localhost:8080`) y `token` | |

## Cómo usarlas

1. En Postman: **Import** y seleccionar los archivos `.json` de esta carpeta.
2. Elegir el entorno **Academy local** (arriba a la derecha).
3. Arrancar el proyecto (`./mvnw spring-boot:run`) con MySQL en marcha (base `academy`).
4. Abrir la colección → **Run collection** y ejecutar **en orden**. Cada petición guarda los ids que necesita la siguiente (`studentId`, `courseId`…).

> 💡 Las peticiones se pueden repetir sin reiniciar la base: los correos llevan un sufijo (`{{run}}`) que cambia en cada ejecución. Cada petición trae sus pruebas (`pm.test`) con el código HTTP esperado.
> Solo hay un proyecto activo a la vez en el puerto 8080.
