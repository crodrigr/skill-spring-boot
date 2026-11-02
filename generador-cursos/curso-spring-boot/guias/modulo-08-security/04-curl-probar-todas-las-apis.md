# 🧪 Probar la API con curl — guía rápida

> 🎯 **Para qué sirve.** Probar **todas las rutas** de la API de estudiantes con seguridad, usando solo la terminal. Se copia un bloque de preparación **una vez** y después cada prueba es **una línea**.
>
> 📦 **Sirve para las dos versiones:** la [completa](codigo/students-api-security/) y la [fácil](codigo/students-api-security-facil/). La única diferencia: la fácil **no tiene** `GET /api/v1/auth/me`.
>
> ✅ **Verificada.** Todos los comandos se ejecutaron contra la aplicación (Spring Boot 4.1.2, MySQL 8) y los resultados que se muestran son reales.
>
> ⚠️ **Solo para practicar.** Las cuentas `admin` / `admin123` y `ana` / `ana123` son de ejemplo.

<details>
<summary><strong>🧭 1. Lo que hay que saber antes de empezar</strong></summary>

> **`curl`** es un programa que **envía peticiones** a la API desde la terminal, como lo haría un navegador.
>
> **Los códigos de respuesta** dicen cómo salió cada petición:
>
> | Código | Significa | Ejemplo |
> |:-:|---|---|
> | `200` | ✅ Salió bien | Ver un estudiante |
> | `201` | ✅ Se creó | Crear un estudiante |
> | `204` | ✅ Salió bien y no hay nada que devolver | Borrar |
> | `400` | ❌ Los datos que se enviaron están mal | Un correo sin `@` |
> | `401` | ❌ **No sé quién eres** | No se envió el token, o es falso, o venció |
> | `403` | ❌ **Sé quién eres, pero no puedes** | Un usuario normal intenta borrar |
> | `404` | ❌ No existe | Un estudiante con un id que no está |
> | `405` | ❌ Ese método no se permite en esa ruta | `DELETE` sobre toda la lista |
> | `409` | ❌ Choca con algo que ya existe | Un correo repetido |
>
> **Los dos usuarios de práctica:**
>
> | Usuario | Rol | Qué puede hacer |
> |---|---|---|
> | `ana` | `USER` | Ver, crear y editar estudiantes |
> | `admin` | `ADMIN` | Todo lo anterior, y además **borrar** y **cargar datos de ejemplo** |

</details>

<details>
<summary><strong>⚙️ 2. Preparar (una sola vez): copiar y pegar este bloque</strong></summary>

> Con la aplicación en marcha y la base de datos **vacía**, pegar esto en la terminal:
>
> ```bash
> BASE=http://localhost:8080
> JSON='Content-Type: application/json'
>
> # Tres atajos para no escribir tanto. Cada uno muestra "→ código" al final.
> sin()   { curl -s -w "\n→ código %{http_code}\n" "$@"; }                                         # sin token
> login() { curl -s -X POST $BASE/api/v1/auth/login -H "$JSON" \
>             -d "{\"username\":\"$1\",\"password\":\"$2\"}" | sed -E 's/.*"token":"([^"]+)".*/\1/'; }
>
> # Iniciar sesión y guardar los dos tokens
> ADMIN=$(login admin admin123)
> ANA=$(login ana ana123)
>
> ana()   { curl -s -w "\n→ código %{http_code}\n" -H "Authorization: Bearer $ANA"   "$@"; }       # con el token de ana
> admin() { curl -s -w "\n→ código %{http_code}\n" -H "Authorization: Bearer $ADMIN" "$@"; }       # con el token de admin
> ```
>
> | Atajo | Qué hace |
> |---|---|
> | `sin ...` | Hace la petición **sin token** (nadie identificado) |
> | `ana ...` | La hace **como ana** (usuario normal) |
> | `admin ...` | La hace **como admin** (administrador) |
>
> **Comprobar que funcionó:** `echo "$ANA" | cut -c1-30` debe mostrar algo como `eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi`. Si sale vacío, la aplicación no está en marcha o la base no estaba vacía.
>
> > 💡 **No hace falta instalar nada más:** ni `jq` ni Python. Y los tokens **duran 1 hora**: si una ruta responde `401` con «El token expiró», repetir el bloque.

</details>

<details>
<summary><strong>💚 3. ¿La API está viva? (no pide token)</strong></summary>

> ```bash
> sin $BASE/internal/health
> ```
>
> ```text
> {"status":"UP"}
> → código 200
> ```
>
> ✅ **Debe pasar:** responde `200` aunque no se envió ningún token. Es una ruta **pública**.

</details>

<details>
<summary><strong>🔑 4. Iniciar sesión (login)</strong></summary>

> El login **ya se usó** en la preparación para obtener los tokens. Así se ve la respuesta completa:
>
> ```bash
> sin -X POST $BASE/api/v1/auth/login -H "$JSON" -d '{"username":"ana","password":"ana123"}'
> ```
>
> ```text
> {"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmEi…","tokenType":"Bearer","expiresIn":3600}
> → código 200
> ```
>
> **Con la contraseña mal:**
>
> ```bash
> sin -X POST $BASE/api/v1/auth/login -H "$JSON" -d '{"username":"ana","password":"mala"}'
> ```
>
> ```text
> {"timestamp":"…","status":401,"error":"Unauthorized","message":"Usuario o contraseña incorrectos","path":"/api/v1/auth/login"}
> → código 401
> ```
>
> **Quién soy** *(solo la versión completa)*:
>
> ```bash
> ana $BASE/api/v1/auth/me
> ```
>
> ```text
> {"username":"ana","role":"USER"}
> → código 200
> ```
>
> ✅ **Debe pasar:** con la clave correcta, `200` y un token. Con la clave mala, `401` con **el mismo mensaje** aunque el usuario no exista (así no se ayuda a quien prueba cuentas ajenas).

</details>

<details>
<summary><strong>🚪 5. Sin token no se entra</strong></summary>

> ```bash
> sin $BASE/api/v1/students                                       # no se envía token
> sin -H "Authorization: Bearer basura" $BASE/api/v1/students     # se envía un token falso
> ```
>
> ```text
> {"timestamp":"…","status":401,"error":"Unauthorized","message":"Se requiere autenticación para acceder a este recurso","path":"/api/v1/students"}
> → código 401
>
> {"timestamp":"…","status":401,"error":"Unauthorized","message":"El token no es válido","path":"/api/v1/students"}
> → código 401
> ```
>
> ✅ **Debe pasar:** las dos dan `401`. *(La versión fácil usa un solo mensaje para ambos casos: «Falta el token o no es válido».)*
>
> **Token vencido.** Para verlo hay que arrancar la aplicación con `--app.jwt.expiration-seconds=2`, iniciar sesión, esperar 3 segundos y repetir una petición: `401` con «El token expiró» *(versión completa)*.

</details>

<details>
<summary><strong>🌱 6. Cargar estudiantes de ejemplo (solo admin)</strong></summary>

> ```bash
> admin -X POST $BASE/internal/seed       # el administrador sí puede
> ana   -X POST $BASE/internal/seed       # ana no
> ```
>
> ```text
> {"created":3}
> → código 200
>
> {"timestamp":"…","status":403,"error":"Forbidden","message":"No tiene permisos para realizar esta operación","path":"/internal/seed"}
> → código 403
> ```
>
> ✅ **Debe pasar:** `admin` crea 3 estudiantes (Ana, Luis y María) y `ana` recibe `403`. Esa es la diferencia entre **`401`** (sin identificar) y **`403`** (identificada, pero sin permiso).

</details>

<details>
<summary><strong>📖 7. Ver estudiantes (cualquier usuario con token)</strong></summary>

> ```bash
> ana $BASE/api/v1/students                                        # la lista
> ana "$BASE/api/v1/students?size=2"                               # de 2 en 2
> ana "$BASE/api/v1/students?name=luis"                            # buscar por nombre
> ana $BASE/api/v1/students/1                                      # uno por su id
> ana $BASE/api/v1/students/999                                    # un id que no existe
> ana "$BASE/api/v1/students/by-email?email=ana@mail.com"          # buscar por correo
> ana $BASE/api/v1/students/ping                                   # ruta de prueba: responde "pong"
> ```
>
> | Comando | Resultado |
> |---|---|
> | La lista, `size=2`, `name=luis`, `/1`, `by-email`, `ping` | ✅ `200` |
> | `/999` | ❌ `404` · `No existe un estudiante con id 999` |
>
> ```text
> {"content":[{"id":1,"name":"Ana Torres","age":22,"email":"ana@mail.com","status":"ACTIVE"},{"id":2,"name":"Luis Pérez",…}],"page":0,"size":2,"totalElements":3,"totalPages":2}
> → código 200
> ```
>
> ✅ **Debe pasar:** `ana` puede leer todo con su token.

</details>

<details>
<summary><strong>✏️ 8. Crear, cambiar y editar (cualquier usuario con token)</strong></summary>

> **Crear** un estudiante (se agrega `-i` para ver la cabecera `Location`):
>
> ```bash
> ana -i -X POST $BASE/api/v1/students -H "$JSON" \
>   -d '{"name":"Carlos Ruiz","age":28,"email":"carlos@mail.com","status":"ACTIVE"}'
> ```
>
> ```text
> HTTP/1.1 201
> Location: http://localhost:8080/api/v1/students/4
> …
> {"id":4,"name":"Carlos Ruiz","age":28,"email":"carlos@mail.com","status":"ACTIVE"}
> → código 201
> ```
>
> > 💡 El **`4`** del final de `Location` es el id del estudiante nuevo. Los comandos de abajo lo usan.
>
> **Equivocarse al crear:**
>
> ```bash
> ana -X POST $BASE/api/v1/students -H "$JSON" \
>   -d '{"name":"Otro","age":30,"email":"carlos@mail.com","status":"ACTIVE"}'          # correo repetido
> ana -X POST $BASE/api/v1/students -H "$JSON" \
>   -d '{"name":"","age":5,"email":"x","status":"ACTIVE"}'                              # datos mal
> ```
>
> ```text
> {"…","status":409,"error":"Conflict","message":"El correo carlos@mail.com ya está registrado","path":"/api/v1/students"}
> → código 409
>
> {"…","status":400,"error":"Bad Request","message":"Los datos enviados no son válidos","path":"/api/v1/students","details":[{"field":"age","message":"debe ser 16 o más"},{"field":"email","message":"no tiene formato de correo electrónico"},…]}
> → código 400
> ```
>
> **Editar todo** (`PUT`) y **cambiar solo el estado** (`PATCH`):
>
> ```bash
> ana -X PUT $BASE/api/v1/students/4 -H "$JSON" \
>   -d '{"name":"Carlos Ruiz Díaz","age":29,"email":"carlos@mail.com","status":"ACTIVE"}'   # 200
> ana -X PATCH $BASE/api/v1/students/4/status -H "$JSON" -d '{"status":"INACTIVE"}'          # 200
> ana -X PATCH $BASE/api/v1/students/4/status -H "$JSON" -d '{"status":"XYZ"}'               # 400
> ```
>
> | Comando | Resultado |
> |---|---|
> | Crear | ✅ `201` |
> | Crear con el correo repetido | ❌ `409` |
> | Crear con datos mal | ❌ `400` (y la lista `details` dice **qué campo** falló) |
> | `PUT` y `PATCH` correctos | ✅ `200` |
> | `PATCH` con un estado que no existe | ❌ `400` |

</details>

<details>
<summary><strong>🗑️ 9. Borrar (solo admin)</strong></summary>

> ```bash
> ana   -X DELETE $BASE/api/v1/students/4     # ana intenta borrar
> admin -X DELETE $BASE/api/v1/students/4     # el administrador borra
> admin -X DELETE $BASE/api/v1/students/4     # borrar otra vez
> ```
>
> ```text
> {"…","status":403,"error":"Forbidden","message":"No tiene permisos para realizar esta operación","path":"/api/v1/students/4"}
> → código 403
>
> → código 204
>
> {"…","status":404,"error":"Not Found","message":"No existe un estudiante con id 4","path":"/api/v1/students/4"}
> → código 404
> ```
>
> ✅ **Debe pasar:** `403`, luego `204` (sin cuerpo: ya no hay nada que devolver) y luego `404` (ya no existe).
>
> > 🧠 **Un detalle curioso.** Si `ana` intenta borrar un id que **no existe** (`/999`), también recibe `403`, no `404`. La seguridad revisa **el rol primero**: un usuario normal nunca llega a preguntar si el id existe.

</details>

<details>
<summary><strong>🧭 10. Rutas que no existen</strong></summary>

> ```bash
> admin $BASE/api/v1/nope                    # una ruta que no existe
> admin -X DELETE $BASE/api/v1/students      # borrar "toda la lista" no está permitido
> ```
>
> ```text
> {"…","status":404,"error":"Not Found","message":"No existe el recurso solicitado","path":"/api/v1/nope"}
> → código 404
>
> {"…","status":405,"error":"Method Not Allowed","message":"Ese método HTTP no está permitido en esta ruta","path":"/api/v1/students"}
> → código 405
> ```
>
> > 🔎 Sin token, `/api/v1/nope` responde `401`: **primero** se pregunta quién eres y **después** si la ruta existe.

</details>

<details>
<summary><strong>🔐 11. Resumen: quién puede qué</strong></summary>

> | Ruta | Sin token | `ana` (USER) | `admin` (ADMIN) |
> |---|:-:|:-:|:-:|
> | `POST /api/v1/auth/login` | ✅ | ✅ | ✅ |
> | `GET /internal/health` | ✅ | ✅ | ✅ |
> | Ver estudiantes (`GET`) | `401` | ✅ | ✅ |
> | Crear (`POST`), editar (`PUT`), cambiar estado (`PATCH`) | `401` | ✅ | ✅ |
> | Borrar (`DELETE`) | `401` | **`403`** | ✅ |
> | Cargar datos (`POST /internal/seed`) | `401` | **`403`** | ✅ |
> | Quién soy (`GET /api/v1/auth/me`) *(solo completa)* | `401` | ✅ | ✅ |

</details>

<details>
<summary><strong>🤖 12. Probar todo de una vez</strong></summary>

> El script [`codigo/probar-apis.sh`](codigo/probar-apis.sh) hace **todas las pruebas de esta guía y más** (54 en total), una detrás de otra, y marca cada una con ✅ o ❌ según el código esperado.
>
> ```bash
> cd codigo
> ./probar-apis.sh                  # versión completa
> SIN_ME=1 ./probar-apis.sh         # versión fácil (no tiene /auth/me)
> ```
>
> **Resultado real:**
>
> ```text
> ✅ 200 (esperado 200) GET     /internal/health                         estado del servicio
> ✅ 401 (esperado 401) GET     /api/v1/students                         listar
> ✅ 403 (esperado 403) POST    /internal/seed                           seed como USER
> ✅ 201 …  creado con id 4
> ✅ 204 (esperado 204) DELETE  /api/v1/students/4                       borrar
> …
> Resultado: 54 correctas, 0 con diferencias.
> ```
>
> > ⚠️ **Antes de correrlo:** la aplicación en marcha y la **base vacía**. Para vaciarla:
> >
> > ```bash
> > docker exec academy-mysql mysql -uroot -proot -e "DROP DATABASE academy; CREATE DATABASE academy"
> > ```
> >
> > y reiniciar la aplicación.

</details>

<details>
<summary><strong>🩺 13. Si algo falla</strong></summary>

> | Qué se ve | Qué probar |
> |---|---|
> | `curl: (7) Failed to connect` | La aplicación no está en marcha (`./mvnw spring-boot:run`) |
> | `ANA` o `ADMIN` salen vacíos | La aplicación no responde, o el login falló: probar el login a mano (prueba 4) |
> | Todo responde `401`, incluso con token | El token venció (dura 1 hora): repetir la preparación |
> | `409` al crear a Carlos | Ya existe de una ejecución anterior: borrarlo o usar otro correo |
> | `404` en `/students/4` | El id del estudiante creado fue otro: mirar el `Location` de la prueba 8 |
> | Los resultados no coinciden con los de esta guía | La base no estaba vacía: vaciarla y reiniciar |

</details>

---

➡️ **Guía completa:** [01-guia-crud-estudiantes-con-security.md](01-guia-crud-estudiantes-con-security.md) · **Guía fácil:** [02-guia-security-facil.md](02-guia-security-facil.md)
