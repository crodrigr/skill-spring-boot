# 🧪 Probar todas las rutas con curl — API de estudiantes con seguridad

> 🧾 **Solo `curl`.** Todos los comandos para probar **cada ruta** de la API protegida con JWT de [`students-api-security`](codigo/students-api-security/) (y de la [versión fácil](codigo/students-api-security-facil/), que es igual **menos** `GET /api/v1/auth/me`). Cada bloque trae el comando, el código de estado esperado y, cuando ayuda, la respuesta real.
>
> ✅ **Verificado.** Se ejecutó contra la aplicación (Spring Boot 4.1.2, MySQL 8): las **54 pruebas** del [script](codigo/probar-apis.sh) dieron el resultado esperado en la versión completa, y **52** en la fácil. Las respuestas que se muestran son reales.
>
> ⚠️ **Solo para fines educativos.** Las cuentas `admin` / `admin123` (rol `ADMIN`) y `ana` / `ana123` (rol `USER`) son de práctica.

<details>
<summary><strong>🗺️ Qué rutas tiene la API y quién puede usarlas</strong></summary>

> | Ruta | Método | Quién puede | Respuestas |
> |---|---|---|---|
> | `/api/v1/auth/login` | `POST` | **Cualquiera** | `200` · `400` · `401` |
> | `/api/v1/auth/me` *(solo versión completa)* | `GET` | Con token | `200` · `401` |
> | `/api/v1/students` | `GET` | Con token | `200` · `400` · `401` |
> | `/api/v1/students` | `POST` | Con token | `201` · `400` · `401` · `409` |
> | `/api/v1/students/{id}` | `GET` | Con token | `200` · `400` · `401` · `404` |
> | `/api/v1/students/{id}` | `PUT` | Con token | `200` · `400` · `401` · `404` · `409` |
> | `/api/v1/students/{id}/status` | `PATCH` | Con token | `200` · `400` · `401` · `404` |
> | `/api/v1/students/{id}` | `DELETE` | Solo **`ADMIN`** | `204` · `401` · `403` · `404` |
> | `/api/v1/students/by-email?email=` | `GET` | Con token | `200` · `400` · `401` · `404` |
> | `/api/v1/students/ping` | `GET` | Con token | `200` · `401` |
> | `/internal/health` | `GET` | **Cualquiera** | `200` |
> | `/internal/seed` | `POST` | Solo **`ADMIN`** | `200` · `401` · `403` |
> | `/swagger-ui.html` · `/api-docs/**` | `GET` | **Cualquiera** | `200` / `302` |
>
> > 🧠 **Recordatorio.** `401` = «no sé quién eres» (sin token, falso o vencido). `403` = «sé quién eres, pero esta puerta no es tuya».

</details>

<details>
<summary><strong>⚙️ 0. Preparar: la dirección y los dos tokens</strong></summary>

> Con la aplicación en marcha y la base de datos **vacía**:
>
> ```bash
> BASE=http://localhost:8080
>
> # Iniciar sesión y guardar cada token en una variable
> ADMIN=$(curl -s -X POST $BASE/api/v1/auth/login -H 'Content-Type: application/json' \
>   -d '{"username":"admin","password":"admin123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')
> ANA=$(curl -s -X POST $BASE/api/v1/auth/login -H 'Content-Type: application/json' \
>   -d '{"username":"ana","password":"ana123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')
>
> # Un token alterado (una letra de la carga cambiada) para probar el rechazo
> TAMPERED=$(echo "$ADMIN" | sed -E 's/^([^.]*\.)./\1X/')
>
> echo "$ADMIN" | cut -c1-40     # comprobar que se guardó algo: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIs…
> ```
>
> > 💡 **No hace falta `jq` ni Python:** el `sed` saca el texto del campo `token`. Para ver la **respuesta completa** de cualquier comando, agregar `-i` a `curl` (muestra también el código de estado y las cabeceras).
>
> > ⚠️ **El token dura 1 hora** (`app.jwt.expiration-seconds=3600`). Si pasa más tiempo y una ruta responde `401` con `El token expiró`, repetir este paso.

</details>

<details>
<summary><strong>🌐 1. Rutas públicas (sin token)</strong></summary>

> ```bash
> curl -i $BASE/internal/health
> curl -i $BASE/api-docs/students          # contrato del grupo «students»
> curl -i $BASE/api-docs/auth              # contrato del grupo «auth»
> curl -i $BASE/api-docs/internal          # contrato del grupo «internal»
> curl -iL $BASE/swagger-ui.html           # -L sigue la redirección a /swagger-ui/index.html
> ```
>
> | Comando | Esperado |
> |---|---|
> | `/internal/health` | `200` · `{"status":"UP"}` |
> | `/api-docs/students`, `/auth`, `/internal` | `200` · el contrato en JSON (OpenAPI 3.1.0) |
> | `/swagger-ui.html` | `302` (sin `-L`) y `200` (con `-L`) |

</details>

<details>
<summary><strong>🔑 2. Login: /api/v1/auth/login</strong></summary>

> ```bash
> # ✅ Correcto
> curl -i -X POST $BASE/api/v1/auth/login -H 'Content-Type: application/json' \
>   -d '{"username":"admin","password":"admin123"}'
> ```
>
> ```text
> HTTP/1.1 200
> {"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsImlhdCI6…","tokenType":"Bearer","expiresIn":3600}
> ```
>
> ```bash
> # ❌ Contraseña incorrecta  → 401
> curl -i -X POST $BASE/api/v1/auth/login -H 'Content-Type: application/json' \
>   -d '{"username":"ana","password":"mala"}'
>
> # ❌ Usuario que no existe  → 401 (el mismo mensaje: no revela cuál falló)
> curl -i -X POST $BASE/api/v1/auth/login -H 'Content-Type: application/json' \
>   -d '{"username":"nadie","password":"x"}'
>
> # ❌ Campos vacíos          → 400 con detalle por campo
> curl -i -X POST $BASE/api/v1/auth/login -H 'Content-Type: application/json' \
>   -d '{"username":"","password":""}'
>
> # ❌ JSON roto              → 400
> curl -i -X POST $BASE/api/v1/auth/login -H 'Content-Type: application/json' -d '{"username":'
> ```
>
> ```text
> HTTP/1.1 401
> {"timestamp":"2026-11-02T03:10:15.201Z","status":401,"error":"Unauthorized","message":"Usuario o contraseña incorrectos","path":"/api/v1/auth/login"}
>
> HTTP/1.1 400
> {"timestamp":"2026-11-02T03:06:52.677Z","status":400,"error":"Bad Request","message":"Los datos enviados no son válidos","path":"/api/v1/auth/login","details":[{"field":"password","message":"es obligatoria"},{"field":"username","message":"es obligatorio"}]}
> ```
>
> **Quién soy** *(solo versión completa)*:
>
> ```bash
> curl -i $BASE/api/v1/auth/me -H "Authorization: Bearer $ANA"
> ```
>
> ```text
> HTTP/1.1 200
> {"username":"ana","role":"USER"}
> ```

</details>

<details>
<summary><strong>🚫 3. Sin token o con un token malo: siempre 401</strong></summary>

> ```bash
> # Sin token: cada ruta protegida responde 401
> curl -i $BASE/api/v1/students
> curl -i $BASE/api/v1/students/1
> curl -i "$BASE/api/v1/students/by-email?email=ana@mail.com"
> curl -i -X POST   $BASE/api/v1/students -H 'Content-Type: application/json' \
>   -d '{"name":"X Y","age":20,"email":"x@mail.com","status":"ACTIVE"}'
> curl -i -X PUT    $BASE/api/v1/students/1 -H 'Content-Type: application/json' \
>   -d '{"name":"X Y","age":20,"email":"x@mail.com","status":"ACTIVE"}'
> curl -i -X PATCH  $BASE/api/v1/students/1/status -H 'Content-Type: application/json' -d '{"status":"INACTIVE"}'
> curl -i -X DELETE $BASE/api/v1/students/1
> curl -i $BASE/api/v1/students/ping
> curl -i -X POST   $BASE/internal/seed
> curl -i $BASE/api/v1/auth/me                       # solo versión completa
>
> # Con un token alterado y con basura
> curl -i $BASE/api/v1/students -H "Authorization: Bearer $TAMPERED"
> curl -i $BASE/api/v1/students -H "Authorization: Bearer basura"
> ```
>
> ```text
> HTTP/1.1 401
> WWW-Authenticate: Bearer
> {"timestamp":"2026-11-02T03:10:14.955Z","status":401,"error":"Unauthorized","message":"Se requiere autenticación para acceder a este recurso","path":"/api/v1/students"}
>
> HTTP/1.1 401
> WWW-Authenticate: Bearer
> {"timestamp":"2026-11-02T03:10:14.986Z","status":401,"error":"Unauthorized","message":"El token no es válido","path":"/api/v1/students/1"}
> ```
>
> > 🔎 La **versión fácil** responde `401` con un solo mensaje para los dos casos: `Falta el token o no es válido (puede haber vencido)`.
>
> **Token vencido:** arrancar con `--app.jwt.expiration-seconds=2`, iniciar sesión, esperar 3 segundos y repetir cualquier petición con ese token: `401` · `El token expiró` (versión completa).

</details>

<details>
<summary><strong>🌱 4. Administrador: cargar los datos de ejemplo</strong></summary>

> ```bash
> curl -i -X POST $BASE/internal/seed -H "Authorization: Bearer $ADMIN"     # ✅ 200
> curl -i -X POST $BASE/internal/seed -H "Authorization: Bearer $ADMIN"     # ✅ 200, no duplica
> curl -i -X POST $BASE/internal/seed -H "Authorization: Bearer $ANA"       # ❌ 403: ana no es ADMIN
> ```
>
> ```text
> HTTP/1.1 200
> {"created":3}
>
> HTTP/1.1 200
> {"created":0}
>
> HTTP/1.1 403
> {"timestamp":"2026-11-02T03:10:15.026Z","status":403,"error":"Forbidden","message":"No tiene permisos para realizar esta operación","path":"/internal/seed"}
> ```

</details>

<details>
<summary><strong>📖 5. Leer estudiantes (cualquier usuario con token)</strong></summary>

> ```bash
> curl -i "$BASE/api/v1/students"                              -H "Authorization: Bearer $ANA"   # 200
> curl -i "$BASE/api/v1/students?page=0&size=2"                -H "Authorization: Bearer $ANA"   # 200, de 2 en 2
> curl -i "$BASE/api/v1/students?name=ana"                     -H "Authorization: Bearer $ANA"   # 200, filtra por nombre
> curl -i "$BASE/api/v1/students?sort=name,desc"               -H "Authorization: Bearer $ANA"   # 200, ordenado
> curl -i "$BASE/api/v1/students?sort=foo"                     -H "Authorization: Bearer $ANA"   # 400
> curl -i  $BASE/api/v1/students/1                             -H "Authorization: Bearer $ANA"   # 200
> curl -i  $BASE/api/v1/students/999                           -H "Authorization: Bearer $ANA"   # 404
> curl -i  $BASE/api/v1/students/abc                           -H "Authorization: Bearer $ANA"   # 400
> curl -i "$BASE/api/v1/students/by-email?email=ana@mail.com"  -H "Authorization: Bearer $ANA"   # 200
> curl -i "$BASE/api/v1/students/by-email?email=nadie@mail.com" -H "Authorization: Bearer $ANA"  # 404
> curl -i  $BASE/api/v1/students/by-email                      -H "Authorization: Bearer $ANA"   # 400, falta el parámetro
> curl -i  $BASE/api/v1/students/ping                          -H "Authorization: Bearer $ANA"   # 200, ruta oculta en Swagger
> ```
>
> ```text
> HTTP/1.1 200
> {"content":[{"id":1,"name":"Ana Torres","age":22,"email":"ana@mail.com","status":"ACTIVE"},{"id":2,"name":"Luis Pérez","age":25,"email":"luis@mail.com","status":"ACTIVE"}],"page":0,"size":2,"totalElements":3,"totalPages":2}
>
> HTTP/1.1 404
> {"timestamp":"2026-11-02T03:00:36.595Z","status":404,"error":"Not Found","message":"No existe un estudiante con id 999","path":"/api/v1/students/999"}
>
> HTTP/1.1 400
> {"timestamp":"2026-11-02T03:00:36.778Z","status":400,"error":"Bad Request","message":"El parámetro 'id' tiene un valor no válido: abc","path":"/api/v1/students/abc"}
>
> HTTP/1.1 400
> {"timestamp":"2026-11-02T03:00:36.804Z","status":400,"error":"Bad Request","message":"No se puede ordenar por 'foo'","path":"/api/v1/students"}
>
> HTTP/1.1 400
> {"timestamp":"2026-11-02T03:00:36.857Z","status":400,"error":"Bad Request","message":"Falta el parámetro obligatorio 'email'","path":"/api/v1/students/by-email"}
> ```
>
> > 💡 `by-email` es una operación **obsoleta** (`deprecated`) en Swagger; sigue funcionando.

</details>

<details>
<summary><strong>✏️ 6. Crear, reemplazar y cambiar el estado (cualquier usuario con token)</strong></summary>

> ```bash
> # ✅ Crear → 201 y la cabecera Location con el id nuevo
> curl -i -X POST $BASE/api/v1/students -H "Authorization: Bearer $ANA" -H 'Content-Type: application/json' \
>   -d '{"name":"Carlos Ruiz","age":28,"email":"carlos@mail.com","status":"ACTIVE"}'
> ```
>
> ```text
> HTTP/1.1 201
> Location: http://localhost:8080/api/v1/students/4
> {"id":4,"name":"Carlos Ruiz","age":28,"email":"carlos@mail.com","status":"ACTIVE"}
> ```
>
> ```bash
> ID=4      # el id que devolvió la cabecera Location
>
> # ❌ Errores al crear
> curl -i -X POST $BASE/api/v1/students -H "Authorization: Bearer $ANA" -H 'Content-Type: application/json' \
>   -d '{"name":"Otro Carlos","age":30,"email":"carlos@mail.com","status":"ACTIVE"}'     # 409 correo repetido
> curl -i -X POST $BASE/api/v1/students -H "Authorization: Bearer $ANA" -H 'Content-Type: application/json' \
>   -d '{"name":"","age":5,"email":"x","status":"ACTIVE"}'                                # 400 datos inválidos
> curl -i -X POST $BASE/api/v1/students -H "Authorization: Bearer $ANA" -H 'Content-Type: application/json' \
>   -d '{"name":'                                                                          # 400 JSON roto
>
> # ✅ Reemplazar (PUT)
> curl -i -X PUT $BASE/api/v1/students/$ID -H "Authorization: Bearer $ANA" -H 'Content-Type: application/json' \
>   -d '{"name":"Carlos Ruiz Díaz","age":29,"email":"carlos@mail.com","status":"ACTIVE"}'  # 200
> # ❌ Errores al reemplazar
> curl -i -X PUT $BASE/api/v1/students/$ID -H "Authorization: Bearer $ANA" -H 'Content-Type: application/json' \
>   -d '{"name":"Carlos","age":29,"email":"ana@mail.com","status":"ACTIVE"}'              # 409 correo de otro estudiante
> curl -i -X PUT $BASE/api/v1/students/999 -H "Authorization: Bearer $ANA" -H 'Content-Type: application/json' \
>   -d '{"name":"Nadie Nadie","age":30,"email":"nadie@mail.com","status":"ACTIVE"}'       # 404 id inexistente
>
> # ✅ Cambiar solo el estado (PATCH)
> curl -i -X PATCH $BASE/api/v1/students/$ID/status -H "Authorization: Bearer $ANA" -H 'Content-Type: application/json' \
>   -d '{"status":"INACTIVE"}'                                                             # 200
> # ❌ Errores al cambiar el estado
> curl -i -X PATCH $BASE/api/v1/students/$ID/status -H "Authorization: Bearer $ANA" -H 'Content-Type: application/json' \
>   -d '{"status":"XYZ"}'                                                                  # 400 valor que no existe
> curl -i -X PATCH $BASE/api/v1/students/$ID/status -H "Authorization: Bearer $ANA" -H 'Content-Type: application/json' \
>   -d '{}'                                                                                # 400 falta el estado
> curl -i -X PATCH $BASE/api/v1/students/999/status -H "Authorization: Bearer $ANA" -H 'Content-Type: application/json' \
>   -d '{"status":"ACTIVE"}'                                                               # 404 id inexistente
> ```
>
> ```text
> HTTP/1.1 409
> {"timestamp":"2026-11-02T03:00:36.704Z","status":409,"error":"Conflict","message":"El correo ana@mail.com ya está registrado","path":"/api/v1/students"}
>
> HTTP/1.1 400
> {"timestamp":"2026-11-02T03:00:36.725Z","status":400,"error":"Bad Request","message":"Los datos enviados no son válidos","path":"/api/v1/students","details":[{"field":"age","message":"debe ser 16 o más"},{"field":"email","message":"no tiene formato de correo electrónico"},{"field":"name","message":"debe tener entre 2 y 100 caracteres"},{"field":"name","message":"es obligatorio"}]}
>
> HTTP/1.1 400
> {"timestamp":"2026-11-02T03:00:36.742Z","status":400,"error":"Bad Request","message":"El cuerpo de la petición no es un JSON válido o tiene un valor no permitido","path":"/api/v1/students"}
> ```

</details>

<details>
<summary><strong>🗑️ 7. Borrar: solo ADMIN</strong></summary>

> ```bash
> # ❌ Un USER no puede borrar → 403 (la regla de rol se evalúa ANTES de buscar el id)
> curl -i -X DELETE $BASE/api/v1/students/$ID -H "Authorization: Bearer $ANA"
> curl -i -X DELETE $BASE/api/v1/students/999 -H "Authorization: Bearer $ANA"
>
> # ✅ ADMIN borra → 204 (sin cuerpo)
> curl -i -X DELETE $BASE/api/v1/students/$ID -H "Authorization: Bearer $ADMIN"
>
> # ❌ Borrar otra vez → 404, y ya no se puede consultar
> curl -i -X DELETE $BASE/api/v1/students/$ID -H "Authorization: Bearer $ADMIN"
> curl -i          $BASE/api/v1/students/$ID -H "Authorization: Bearer $ADMIN"
> ```
>
> ```text
> HTTP/1.1 403
> {"timestamp":"2026-11-02T03:10:15.026Z","status":403,"error":"Forbidden","message":"No tiene permisos para realizar esta operación","path":"/api/v1/students/4"}
>
> HTTP/1.1 204
>
> HTTP/1.1 404
> {"timestamp":"…","status":404,"error":"Not Found","message":"No existe un estudiante con id 4","path":"/api/v1/students/4"}
> ```
>
> > 🧠 **¿Por qué `403` y no `404` al borrar el id `999` como `USER`?** Spring Security revisa el **rol** antes de que la petición llegue al controlador. Un `USER` nunca llega a preguntar si el id existe.

</details>

<details>
<summary><strong>🧭 8. Rutas que no existen y métodos no permitidos</strong></summary>

> ```bash
> curl -i $BASE/api/v1/nope                  -H "Authorization: Bearer $ADMIN"   # 404
> curl -i -X DELETE $BASE/api/v1/students    -H "Authorization: Bearer $ADMIN"   # 405
> ```
>
> ```text
> HTTP/1.1 404
> {"timestamp":"2026-11-02T03:10:15.224Z","status":404,"error":"Not Found","message":"No existe el recurso solicitado","path":"/api/v1/nope"}
>
> HTTP/1.1 405
> Allow: GET, POST
> {"timestamp":"2026-11-02T03:00:36.840Z","status":405,"error":"Method Not Allowed","message":"Ese método HTTP no está permitido en esta ruta","path":"/api/v1/students"}
> ```
>
> > 🔎 Sin token, `/api/v1/nope` responde `401`: **primero** se pregunta quién eres y **después** si la ruta existe.

</details>

<details>
<summary><strong>🔐 9. Matriz de permisos: quién puede qué</strong></summary>

> | Ruta | Sin token | `USER` (ana) | `ADMIN` (admin) |
> |---|:-:|:-:|:-:|
> | `POST /api/v1/auth/login` | ✅ | ✅ | ✅ |
> | `GET /internal/health` | ✅ | ✅ | ✅ |
> | `GET /api/v1/students` y `/{id}` y `/by-email` | `401` | ✅ | ✅ |
> | `POST` · `PUT` · `PATCH` de estudiantes | `401` | ✅ | ✅ |
> | `DELETE /api/v1/students/{id}` | `401` | **`403`** | ✅ `204` |
> | `POST /internal/seed` | `401` | **`403`** | ✅ `200` |
> | `GET /api/v1/auth/me` *(solo versión completa)* | `401` | ✅ | ✅ |

</details>

<details>
<summary><strong>🤖 10. Probar todo de una vez: probar-apis.sh</strong></summary>

> El script [`codigo/probar-apis.sh`](codigo/probar-apis.sh) ejecuta **todas las pruebas anteriores** en orden y marca cada una con ✅ o ❌ según el código de estado esperado.
>
> ```bash
> cd codigo
> ./probar-apis.sh                  # versión completa (http://localhost:8080)
> SIN_ME=1 ./probar-apis.sh         # versión fácil (no tiene /auth/me)
> ./probar-apis.sh http://localhost:9090     # otra dirección
> ```
>
> > ⚠️ **Antes de ejecutarlo:** la aplicación en marcha y la **base vacía** (el script crea y borra un estudiante y cuenta con los 3 del `seed`). Para reiniciar la base:
> >
> > ```bash
> > docker exec academy-mysql mysql -uroot -proot -e "DROP DATABASE academy; CREATE DATABASE academy"
> > ```
> > y reiniciar la aplicación.
>
> **Resultado real** (versión completa, 54 pruebas):
>
> ```text
> ── 1. Rutas públicas (sin token) ──
> ✅ 200 (esperado 200) GET     /internal/health                         estado del servicio
> ✅ 302 (esperado 302) GET     /swagger-ui.html                         Swagger UI (redirige)
> ── 3. Sin token: todo lo protegido responde 401 ──
> ✅ 401 (esperado 401) GET     /api/v1/students                         listar
> ✅ 401 (esperado 401) DELETE  /api/v1/students/1                       borrar
> ── 6. Usuario normal (ana, USER): leer y escribir ──
> ✅ 403 (esperado 403) DELETE  /api/v1/students/4                       borrar como USER
> ── 7. Administrador: borrar ──
> ✅ 204 (esperado 204) DELETE  /api/v1/students/4                       borrar
> ✅ 404 (esperado 404) DELETE  /api/v1/students/4                       borrar otra vez
> …
> Resultado: 54 correctas, 0 con diferencias.
> ```
>
> **Si algún ❌ aparece:** el código obtenido está junto al esperado. Un `401` donde se esperaba `200` casi siempre es un token vencido o una base que no estaba vacía.
>
> <details>
> <summary><strong>📄 probar-apis.sh (código completo)</strong></summary>
>
> **Ruta:** [`codigo/probar-apis.sh`](codigo/probar-apis.sh)
>
> ```bash
> #!/usr/bin/env bash
> # Prueba TODAS las rutas de la API de estudiantes con seguridad (JWT) usando solo curl.
> #
> # Uso:      ./probar-apis.sh [URL_BASE]            (por defecto http://localhost:8080)
> # Versión fácil (no tiene GET /api/v1/auth/me):   SIN_ME=1 ./probar-apis.sh
> #
> # Requisitos: la aplicación en marcha y la base VACÍA (se cuentan 3 estudiantes del seed).
> # Solo para fines educativos: las cuentas admin/admin123 y ana/ana123 son de práctica.
>
> BASE="${1:-http://localhost:8080}"
> JSON='Content-Type: application/json'
> OK=0; FAIL=0
>
> # check "descripción" CÓDIGO_ESPERADO MÉTODO RUTA [TOKEN] [CUERPO]
> check() {
>   local desc="$1" expected="$2" method="$3" path="$4" token="$5" body="$6" code
>   local args=(-s -o /tmp/probar-apis.out -w '%{http_code}' -X "$method")
>   [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
>   [ -n "$body" ]  && args+=(-H "$JSON" -d "$body")
>   code=$(curl "${args[@]}" "$BASE$path")
>   if [ "$code" = "$expected" ]; then OK=$((OK+1)); mark="✅"; else FAIL=$((FAIL+1)); mark="❌"; fi
>   printf '%s %-3s (esperado %-3s) %-7s %-40s %s\n' "$mark" "$code" "$expected" "$method" "$path" "$desc"
> }
>
> # login USUARIO CLAVE  ->  imprime el token
> login() {
>   curl -s -X POST -H "$JSON" -d "{\"username\":\"$1\",\"password\":\"$2\"}" "$BASE/api/v1/auth/login" \
>     | sed -E 's/.*"token":"([^"]+)".*/\1/'
> }
>
> seccion() { printf '\n── %s ──\n' "$1"; }
>
> seccion "1. Rutas públicas (sin token)"
> check "estado del servicio" 200 GET /internal/health
> check "contrato OpenAPI: estudiantes" 200 GET /api-docs/students
> check "contrato OpenAPI: auth" 200 GET /api-docs/auth
> check "contrato OpenAPI: internal" 200 GET /api-docs/internal
> check "Swagger UI (redirige)" 302 GET /swagger-ui.html
>
> seccion "2. Login"
> ADMIN=$(login admin admin123)
> ANA=$(login ana ana123)
> [ ${#ADMIN} -gt 50 ] && echo "✅ token de admin obtenido (${#ADMIN} caracteres)" || { echo "❌ no se pudo iniciar sesión como admin"; exit 1; }
> [ ${#ANA} -gt 50 ]   && echo "✅ token de ana obtenido (${#ANA} caracteres)"   || { echo "❌ no se pudo iniciar sesión como ana"; exit 1; }
> check "contraseña incorrecta" 401 POST /api/v1/auth/login "" '{"username":"ana","password":"mala"}'
> check "usuario que no existe" 401 POST /api/v1/auth/login "" '{"username":"nadie","password":"x"}'
> check "campos vacíos"        400 POST /api/v1/auth/login "" '{"username":"","password":""}'
> check "JSON roto"            400 POST /api/v1/auth/login "" '{"username":'
>
> seccion "3. Sin token: todo lo protegido responde 401"
> check "listar"            401 GET    /api/v1/students
> check "buscar por id"     401 GET    /api/v1/students/1
> check "buscar por correo" 401 GET    "/api/v1/students/by-email?email=ana@mail.com"
> check "crear"             401 POST   /api/v1/students "" '{"name":"X Y","age":20,"email":"x@mail.com","status":"ACTIVE"}'
> check "reemplazar"        401 PUT    /api/v1/students/1 "" '{"name":"X Y","age":20,"email":"x@mail.com","status":"ACTIVE"}'
> check "cambiar estado"    401 PATCH  /api/v1/students/1/status "" '{"status":"INACTIVE"}'
> check "borrar"            401 DELETE /api/v1/students/1
> check "ping"              401 GET    /api/v1/students/ping
> check "seed"              401 POST   /internal/seed
> [ -z "$SIN_ME" ] && check "quién soy" 401 GET /api/v1/auth/me
>
> seccion "4. Token inválido: 401"
> TAMPERED=$(echo "$ADMIN" | sed -E 's/^([^.]*\.)./\1X/')   # una letra de la carga cambiada
> check "token alterado"      401 GET /api/v1/students "$TAMPERED"
> check "token que es basura" 401 GET /api/v1/students "basura"
>
> seccion "5. Administrador: cargar datos de ejemplo"
> check "seed (crea 3 estudiantes)" 200 POST /internal/seed "$ADMIN"
> check "seed otra vez (no duplica)" 200 POST /internal/seed "$ADMIN"
> check "seed como USER"             403 POST /internal/seed "$ANA"
>
> seccion "6. Usuario normal (ana, USER): leer y escribir"
> [ -z "$SIN_ME" ] && check "quién soy" 200 GET /api/v1/auth/me "$ANA"
> check "listar"                        200 GET "/api/v1/students" "$ANA"
> check "listar de 2 en 2"              200 GET "/api/v1/students?page=0&size=2" "$ANA"
> check "filtrar por nombre"            200 GET "/api/v1/students?name=ana" "$ANA"
> check "ordenar por nombre"            200 GET "/api/v1/students?sort=name,desc" "$ANA"
> check "ordenar por algo que no existe" 400 GET "/api/v1/students?sort=foo" "$ANA"
> check "buscar por id"                 200 GET /api/v1/students/1 "$ANA"
> check "buscar un id que no existe"    404 GET /api/v1/students/999 "$ANA"
> check "id que no es número"           400 GET /api/v1/students/abc "$ANA"
> check "buscar por correo"             200 GET "/api/v1/students/by-email?email=ana@mail.com" "$ANA"
> check "correo que no existe"          404 GET "/api/v1/students/by-email?email=nadie@mail.com" "$ANA"
> check "falta el parámetro correo"     400 GET /api/v1/students/by-email "$ANA"
> check "ping (ruta oculta en Swagger)" 200 GET /api/v1/students/ping "$ANA"
>
> printf '\nCrear un estudiante nuevo y guardar su id...\n'
> NEW_ID=$(curl -s -i -X POST -H "Authorization: Bearer $ANA" -H "$JSON" \
>   -d '{"name":"Carlos Ruiz","age":28,"email":"carlos@mail.com","status":"ACTIVE"}' "$BASE/api/v1/students" \
>   | grep -i '^location:' | sed -E 's#.*/students/([0-9]+).*#\1#' | tr -d '\r')
> [ -n "$NEW_ID" ] && echo "✅ creado con id $NEW_ID (201 + cabecera Location)" || { echo "❌ no se pudo crear"; exit 1; }
>
> check "crear con el correo repetido" 409 POST /api/v1/students "$ANA" '{"name":"Otro Carlos","age":30,"email":"carlos@mail.com","status":"ACTIVE"}'
> check "crear con datos inválidos"    400 POST /api/v1/students "$ANA" '{"name":"","age":5,"email":"x","status":"ACTIVE"}'
> check "crear con JSON roto"          400 POST /api/v1/students "$ANA" '{"name":'
> check "reemplazar"                   200 PUT  "/api/v1/students/$NEW_ID" "$ANA" '{"name":"Carlos Ruiz Díaz","age":29,"email":"carlos@mail.com","status":"ACTIVE"}'
> check "reemplazar con correo ajeno"  409 PUT  "/api/v1/students/$NEW_ID" "$ANA" '{"name":"Carlos","age":29,"email":"ana@mail.com","status":"ACTIVE"}'
> check "reemplazar un id inexistente" 404 PUT  /api/v1/students/999 "$ANA" '{"name":"Nadie Nadie","age":30,"email":"nadie@mail.com","status":"ACTIVE"}'
> check "cambiar el estado"            200 PATCH "/api/v1/students/$NEW_ID/status" "$ANA" '{"status":"INACTIVE"}'
> check "estado que no existe"         400 PATCH "/api/v1/students/$NEW_ID/status" "$ANA" '{"status":"XYZ"}'
> check "estado vacío"                 400 PATCH "/api/v1/students/$NEW_ID/status" "$ANA" '{}'
> check "estado de un id inexistente"  404 PATCH /api/v1/students/999/status "$ANA" '{"status":"ACTIVE"}'
> check "borrar como USER"             403 DELETE "/api/v1/students/$NEW_ID" "$ANA"
> check "borrar un id inexistente como USER (la regla de rol va primero)" 403 DELETE /api/v1/students/999 "$ANA"
>
> seccion "7. Administrador: borrar"
> check "borrar"                    204 DELETE "/api/v1/students/$NEW_ID" "$ADMIN"
> check "borrar otra vez"           404 DELETE "/api/v1/students/$NEW_ID" "$ADMIN"
> check "ya no existe"              404 GET    "/api/v1/students/$NEW_ID" "$ADMIN"
>
> seccion "8. Rutas que no existen o métodos no permitidos"
> check "ruta inexistente"          404 GET    /api/v1/nope "$ADMIN"
> check "DELETE sobre la colección" 405 DELETE /api/v1/students "$ADMIN"
>
> printf '\nResultado: %s correctas, %s con diferencias.\n' "$OK" "$FAIL"
> [ "$FAIL" -eq 0 ]
> ```
>
> </details>

</details>

---

➡️ **Guía completa:** [01-guia-crud-estudiantes-con-security.md](01-guia-crud-estudiantes-con-security.md) · **Guía fácil:** [02-guia-security-facil.md](02-guia-security-facil.md) · **Todo el código:** [03-security-facil-codigo-completo.md](03-security-facil-codigo-completo.md)
