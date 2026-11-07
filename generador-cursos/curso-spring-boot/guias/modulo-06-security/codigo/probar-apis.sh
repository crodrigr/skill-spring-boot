#!/usr/bin/env bash
# Prueba TODAS las rutas de la API de estudiantes con seguridad (JWT) usando solo curl.
#
# Uso:      ./probar-apis.sh [URL_BASE]            (por defecto http://localhost:8080)
# Versión fácil (no tiene GET /api/v1/auth/me):   SIN_ME=1 ./probar-apis.sh
#
# Requisitos: la aplicación en marcha y la base VACÍA (se cuentan 3 estudiantes del seed).
# Solo para fines educativos: las cuentas admin/admin123 y ana/ana123 son de práctica.

BASE="${1:-http://localhost:8080}"
JSON='Content-Type: application/json'
OK=0; FAIL=0

# check "descripción" CÓDIGO_ESPERADO MÉTODO RUTA [TOKEN] [CUERPO]
check() {
  local desc="$1" expected="$2" method="$3" path="$4" token="$5" body="$6" code
  local args=(-s -o /tmp/probar-apis.out -w '%{http_code}' -X "$method")
  [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
  [ -n "$body" ]  && args+=(-H "$JSON" -d "$body")
  code=$(curl "${args[@]}" "$BASE$path")
  if [ "$code" = "$expected" ]; then OK=$((OK+1)); mark="✅"; else FAIL=$((FAIL+1)); mark="❌"; fi
  printf '%s %-3s (esperado %-3s) %-7s %-40s %s\n' "$mark" "$code" "$expected" "$method" "$path" "$desc"
}

# login USUARIO CLAVE  ->  imprime el token
login() {
  curl -s -X POST -H "$JSON" -d "{\"username\":\"$1\",\"password\":\"$2\"}" "$BASE/api/v1/auth/login" \
    | sed -E 's/.*"token":"([^"]+)".*/\1/'
}

seccion() { printf '\n── %s ──\n' "$1"; }

seccion "1. Rutas públicas (sin token)"
check "estado del servicio" 200 GET /internal/health
check "contrato OpenAPI: estudiantes" 200 GET /api-docs/students
check "contrato OpenAPI: auth" 200 GET /api-docs/auth
check "contrato OpenAPI: internal" 200 GET /api-docs/internal
check "Swagger UI (redirige)" 302 GET /swagger-ui.html

seccion "2. Login"
ADMIN=$(login admin admin123)
ANA=$(login ana ana123)
[ ${#ADMIN} -gt 50 ] && echo "✅ token de admin obtenido (${#ADMIN} caracteres)" || { echo "❌ no se pudo iniciar sesión como admin"; exit 1; }
[ ${#ANA} -gt 50 ]   && echo "✅ token de ana obtenido (${#ANA} caracteres)"   || { echo "❌ no se pudo iniciar sesión como ana"; exit 1; }
check "contraseña incorrecta" 401 POST /api/v1/auth/login "" '{"username":"ana","password":"mala"}'
check "usuario que no existe" 401 POST /api/v1/auth/login "" '{"username":"nadie","password":"x"}'
check "campos vacíos"        400 POST /api/v1/auth/login "" '{"username":"","password":""}'
check "JSON roto"            400 POST /api/v1/auth/login "" '{"username":'

seccion "3. Sin token: todo lo protegido responde 401"
check "listar"            401 GET    /api/v1/students
check "buscar por id"     401 GET    /api/v1/students/1
check "buscar por correo" 401 GET    "/api/v1/students/by-email?email=ana@mail.com"
check "crear"             401 POST   /api/v1/students "" '{"name":"X Y","age":20,"email":"x@mail.com","status":"ACTIVE"}'
check "reemplazar"        401 PUT    /api/v1/students/1 "" '{"name":"X Y","age":20,"email":"x@mail.com","status":"ACTIVE"}'
check "cambiar estado"    401 PATCH  /api/v1/students/1/status "" '{"status":"INACTIVE"}'
check "borrar"            401 DELETE /api/v1/students/1
check "ping"              401 GET    /api/v1/students/ping
check "seed"              401 POST   /internal/seed
[ -z "$SIN_ME" ] && check "quién soy" 401 GET /api/v1/auth/me

seccion "4. Token inválido: 401"
TAMPERED=$(echo "$ADMIN" | sed -E 's/^([^.]*\.)./\1X/')   # una letra de la carga cambiada
check "token alterado"      401 GET /api/v1/students "$TAMPERED"
check "token que es basura" 401 GET /api/v1/students "basura"

seccion "5. Administrador: cargar datos de ejemplo"
check "seed (crea 3 estudiantes)" 200 POST /internal/seed "$ADMIN"
check "seed otra vez (no duplica)" 200 POST /internal/seed "$ADMIN"
check "seed como USER"             403 POST /internal/seed "$ANA"

seccion "6. Usuario normal (ana, USER): leer y escribir"
[ -z "$SIN_ME" ] && check "quién soy" 200 GET /api/v1/auth/me "$ANA"
check "listar"                        200 GET "/api/v1/students" "$ANA"
check "listar de 2 en 2"              200 GET "/api/v1/students?page=0&size=2" "$ANA"
check "filtrar por nombre"            200 GET "/api/v1/students?name=ana" "$ANA"
check "ordenar por nombre"            200 GET "/api/v1/students?sort=name,desc" "$ANA"
check "ordenar por algo que no existe" 400 GET "/api/v1/students?sort=foo" "$ANA"
check "buscar por id"                 200 GET /api/v1/students/1 "$ANA"
check "buscar un id que no existe"    404 GET /api/v1/students/999 "$ANA"
check "id que no es número"           400 GET /api/v1/students/abc "$ANA"
check "buscar por correo"             200 GET "/api/v1/students/by-email?email=ana@mail.com" "$ANA"
check "correo que no existe"          404 GET "/api/v1/students/by-email?email=nadie@mail.com" "$ANA"
check "falta el parámetro correo"     400 GET /api/v1/students/by-email "$ANA"
check "ping (ruta oculta en Swagger)" 200 GET /api/v1/students/ping "$ANA"

printf '\nCrear un estudiante nuevo y guardar su id...\n'
NEW_ID=$(curl -s -i -X POST -H "Authorization: Bearer $ANA" -H "$JSON" \
  -d '{"name":"Carlos Ruiz","age":28,"email":"carlos@mail.com","status":"ACTIVE"}' "$BASE/api/v1/students" \
  | grep -i '^location:' | sed -E 's#.*/students/([0-9]+).*#\1#' | tr -d '\r')
[ -n "$NEW_ID" ] && echo "✅ creado con id $NEW_ID (201 + cabecera Location)" || { echo "❌ no se pudo crear"; exit 1; }

check "crear con el correo repetido" 409 POST /api/v1/students "$ANA" '{"name":"Otro Carlos","age":30,"email":"carlos@mail.com","status":"ACTIVE"}'
check "crear con datos inválidos"    400 POST /api/v1/students "$ANA" '{"name":"","age":5,"email":"x","status":"ACTIVE"}'
check "crear con JSON roto"          400 POST /api/v1/students "$ANA" '{"name":'
check "reemplazar"                   200 PUT  "/api/v1/students/$NEW_ID" "$ANA" '{"name":"Carlos Ruiz Díaz","age":29,"email":"carlos@mail.com","status":"ACTIVE"}'
check "reemplazar con correo ajeno"  409 PUT  "/api/v1/students/$NEW_ID" "$ANA" '{"name":"Carlos","age":29,"email":"ana@mail.com","status":"ACTIVE"}'
check "reemplazar un id inexistente" 404 PUT  /api/v1/students/999 "$ANA" '{"name":"Nadie Nadie","age":30,"email":"nadie@mail.com","status":"ACTIVE"}'
check "cambiar el estado"            200 PATCH "/api/v1/students/$NEW_ID/status" "$ANA" '{"status":"INACTIVE"}'
check "estado que no existe"         400 PATCH "/api/v1/students/$NEW_ID/status" "$ANA" '{"status":"XYZ"}'
check "estado vacío"                 400 PATCH "/api/v1/students/$NEW_ID/status" "$ANA" '{}'
check "estado de un id inexistente"  404 PATCH /api/v1/students/999/status "$ANA" '{"status":"ACTIVE"}'
check "borrar como USER"             403 DELETE "/api/v1/students/$NEW_ID" "$ANA"
check "borrar un id inexistente como USER (la regla de rol va primero)" 403 DELETE /api/v1/students/999 "$ANA"

seccion "7. Administrador: borrar"
check "borrar"                    204 DELETE "/api/v1/students/$NEW_ID" "$ADMIN"
check "borrar otra vez"           404 DELETE "/api/v1/students/$NEW_ID" "$ADMIN"
check "ya no existe"              404 GET    "/api/v1/students/$NEW_ID" "$ADMIN"

seccion "8. Rutas que no existen o métodos no permitidos"
check "ruta inexistente"          404 GET    /api/v1/nope "$ADMIN"
check "DELETE sobre la colección" 405 DELETE /api/v1/students "$ADMIN"

printf '\nResultado: %s correctas, %s con diferencias.\n' "$OK" "$FAIL"
[ "$FAIL" -eq 0 ]
