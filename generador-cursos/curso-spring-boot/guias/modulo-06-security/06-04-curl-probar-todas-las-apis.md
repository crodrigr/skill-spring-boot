# 🧪 curl para probar la API — resumen

> Una línea de `curl` por endpoint, sin variables. Con la aplicación en marcha en `http://localhost:8080` y la base **vacía**.
>
> 🔑 **Solo una cosa que cambiar:** después del login (paso 1), reemplazar `TOKEN_ANA` y `TOKEN_ADMIN` por el token que devuelve cada login.
>
> ✅ Verificado con Spring Boot 4.1.2. El número al final de cada línea es el **código esperado**.
>
> ⚠️ Cuentas de práctica: `admin` / `admin123` (puede todo) y `ana` / `ana123` (no puede borrar ni cargar datos).

## 1. Login

**`POST /api/v1/auth/login`** — devuelve el token (copiarlo del campo `token`)

```bash
curl -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"ana","password":"ana123"}'          # 200 → copiar el token en TOKEN_ANA
curl -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'      # 200 → copiar el token en TOKEN_ADMIN
curl -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"ana","password":"mala"}'            # 401
```

## 2. Rutas públicas

**`GET /internal/health`**

```bash
curl http://localhost:8080/internal/health          # 200
```

## 3. Cargar datos (solo admin)

**`POST /internal/seed`**

```bash
curl -X POST http://localhost:8080/internal/seed -H "Authorization: Bearer TOKEN_ADMIN"          # 200
curl -X POST http://localhost:8080/internal/seed -H "Authorization: Bearer TOKEN_ANA"            # 403
```

## 4. Ver estudiantes (cualquier usuario con token)

**`GET /api/v1/students`** — la lista

```bash
curl http://localhost:8080/api/v1/students -H "Authorization: Bearer TOKEN_ANA"          # 200
curl http://localhost:8080/api/v1/students                                               # 401 (sin token)
```

**`GET /api/v1/students?name=luis`** — buscar por nombre

```bash
curl "http://localhost:8080/api/v1/students?name=luis" -H "Authorization: Bearer TOKEN_ANA"          # 200
```

**`GET /api/v1/students/{id}`** — uno por su id

```bash
curl http://localhost:8080/api/v1/students/1 -H "Authorization: Bearer TOKEN_ANA"            # 200
curl http://localhost:8080/api/v1/students/999 -H "Authorization: Bearer TOKEN_ANA"          # 404
```

**`GET /api/v1/students/by-email?email=`** — buscar por correo

```bash
curl "http://localhost:8080/api/v1/students/by-email?email=ana@mail.com" -H "Authorization: Bearer TOKEN_ANA"          # 200
```

**`GET /api/v1/students/ping`** — ruta de prueba (responde `pong`)

```bash
curl http://localhost:8080/api/v1/students/ping -H "Authorization: Bearer TOKEN_ANA"          # 200
```

**`GET /api/v1/auth/me`** — quién soy *(solo la versión completa)*

```bash
curl http://localhost:8080/api/v1/auth/me -H "Authorization: Bearer TOKEN_ANA"          # 200
```

## 5. Crear y editar (cualquier usuario con token)

**`POST /api/v1/students`** — crear

```bash
curl -X POST http://localhost:8080/api/v1/students -H "Authorization: Bearer TOKEN_ANA" -H "Content-Type: application/json" -d '{"name":"Carlos Ruiz","age":28,"email":"carlos@mail.com","status":"ACTIVE"}'          # 201
curl -X POST http://localhost:8080/api/v1/students -H "Authorization: Bearer TOKEN_ANA" -H "Content-Type: application/json" -d '{"name":"Otro","age":30,"email":"carlos@mail.com","status":"ACTIVE"}'                # 409 (correo repetido)
curl -X POST http://localhost:8080/api/v1/students -H "Authorization: Bearer TOKEN_ANA" -H "Content-Type: application/json" -d '{"name":"","age":5,"email":"x","status":"ACTIVE"}'                                   # 400 (datos mal)
```

**`PUT /api/v1/students/{id}`** — reemplazar todos los datos *(el estudiante nuevo tiene id 4)*

```bash
curl -X PUT http://localhost:8080/api/v1/students/4 -H "Authorization: Bearer TOKEN_ANA" -H "Content-Type: application/json" -d '{"name":"Carlos Ruiz Díaz","age":29,"email":"carlos@mail.com","status":"ACTIVE"}'          # 200
```

**`PATCH /api/v1/students/{id}/status`** — cambiar solo el estado

```bash
curl -X PATCH http://localhost:8080/api/v1/students/4/status -H "Authorization: Bearer TOKEN_ANA" -H "Content-Type: application/json" -d '{"status":"INACTIVE"}'          # 200
curl -X PATCH http://localhost:8080/api/v1/students/4/status -H "Authorization: Bearer TOKEN_ANA" -H "Content-Type: application/json" -d '{"status":"XYZ"}'               # 400
```

## 6. Borrar (solo admin)

**`DELETE /api/v1/students/{id}`**

```bash
curl -X DELETE http://localhost:8080/api/v1/students/4 -H "Authorization: Bearer TOKEN_ANA"          # 403
curl -X DELETE http://localhost:8080/api/v1/students/4 -H "Authorization: Bearer TOKEN_ADMIN"        # 204
curl -X DELETE http://localhost:8080/api/v1/students/4 -H "Authorization: Bearer TOKEN_ADMIN"        # 404 (ya no existe)
```

## 7. Resumen de códigos

| Código | Significa |
|:-:|---|
| `200` · `201` · `204` | ✅ Salió bien · se creó · se borró |
| `400` | ❌ Los datos enviados están mal |
| `401` | ❌ Falta el token, es falso o venció |
| `403` | ❌ Hay token, pero el rol no alcanza |
| `404` | ❌ No existe |
| `409` | ❌ Choca con algo que ya existe (correo repetido) |

> 💡 Para ver **todo** de una vez: [`codigo/probar-apis.sh`](codigo/probar-apis.sh) corre las 54 pruebas y marca cada una con ✅ o ❌.
>
> 📮 ¿Se prefiere una interfaz? La [colección de Postman](codigo/postman/) trae las mismas pruebas listas para importar.

---

➡️ [Guía completa](06-01-guia-crud-estudiantes-con-security.md) · [Guía fácil](06-02-guia-security-facil.md)
