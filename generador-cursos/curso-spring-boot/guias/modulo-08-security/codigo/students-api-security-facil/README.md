# students-api-security-facil

Versión **sencilla** de la API de estudiantes con **login y token (JWT)**: Spring Boot 4.1.2, Spring Data JPA, MySQL 8 y JJWT 0.12.6.
Parte del proyecto del módulo 05 ([`students-api-excepciones`](../../../modulo-05-excepciones/codigo/students-api-excepciones/)).
Código final de la guía [`02-guia-security-facil.md`](../../02-guia-security-facil.md). La versión completa está en [`students-api-security`](../students-api-security/).

## Ejecutar

```bash
docker run -d --name academy-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=academy -p 3306:3306 mysql:8.0
./mvnw spring-boot:run
```

Usuarios de práctica (**solo para fines educativos**): `admin` / `admin123` (`ADMIN`) y `ana` / `ana123` (`USER`).

## Probar

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

curl -i -H "Authorization: Bearer $TOKEN" -X POST localhost:8080/internal/seed
curl -i -H "Authorization: Bearer $TOKEN" localhost:8080/api/v1/students
curl -i localhost:8080/api/v1/students        # sin token: 401
```

| Ruta | Quién puede |
|---|---|
| `POST /api/v1/auth/login`, `GET /internal/health`, Swagger | Cualquiera |
| `/api/v1/students/**` (ver, crear, reemplazar, cambiar estado) | Cualquier usuario con token |
| `DELETE /api/v1/students/{id}` y `/internal/seed` | Solo `ADMIN` |

> ⚠️ La clave del token (`app.jwt.secret`) es **solo educativa**. En un proyecto real se entrega con la variable `JWT_SECRET`.
