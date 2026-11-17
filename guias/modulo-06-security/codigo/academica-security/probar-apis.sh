#!/usr/bin/env bash
# Prueba las rutas de la API de la academia con Spring Security + JWT.
# Uso: ./probar-apis.sh [URL_BASE]      (por defecto http://localhost:8080)
# Requiere la aplicación en marcha y `curl`. Cada línea muestra: esperado → obtenido.
BASE="${1:-http://localhost:8080}"
ok=0; fail=0

# check <esperado> <descripción> <argumentos de curl...>
check() {
  local expected="$1" label="$2"; shift 2
  local got
  got=$(curl -s -o /dev/null -w '%{http_code}' "$@")
  if [ "$got" = "$expected" ]; then ok=$((ok+1)); echo "✅ $expected  $label"
  else fail=$((fail+1)); echo "❌ esperado $expected, obtenido $got  $label"; fi
}
token() { curl -s -X POST "$BASE/api/v1/auth/login" -H 'Content-Type: application/json' \
  -d "{\"username\":\"$1\",\"password\":\"$2\"}" | sed -E 's/.*"token":"([^"]+)".*/\1/'; }
J='Content-Type: application/json'

echo "== Públicas =="
check 200 "GET  /internal/health"                 "$BASE/internal/health"
check 200 "GET  /swagger-ui.html"                 -L "$BASE/swagger-ui.html"
check 200 "POST /auth/login (admin)"              -X POST "$BASE/api/v1/auth/login" -H "$J" -d '{"username":"admin","password":"admin123"}'
check 401 "POST /auth/login (clave incorrecta)"   -X POST "$BASE/api/v1/auth/login" -H "$J" -d '{"username":"admin","password":"mala"}'
check 400 "POST /auth/login (sin campos)"         -X POST "$BASE/api/v1/auth/login" -H "$J" -d '{}'

ADMIN=$(token admin admin123); USER=$(token ana ana123)
A="Authorization: Bearer $ADMIN"; U="Authorization: Bearer $USER"

echo "== Sin token (401) =="
check 401 "GET  /students sin token"              "$BASE/api/v1/students"
check 401 "GET  /courses sin token"               "$BASE/api/v1/courses"
check 401 "POST /enrollments sin token"           -X POST "$BASE/api/v1/enrollments" -H "$J" -d '{"studentId":1,"courseId":1}'
check 401 "GET  /students con token falso"        -H 'Authorization: Bearer abc.def.ghi' "$BASE/api/v1/students"

echo "== Rol USER =="
check 200 "GET  /auth/me"                         -H "$U" "$BASE/api/v1/auth/me"
check 200 "GET  /students"                        -H "$U" "$BASE/api/v1/students"
check 403 "POST /courses (solo ADMIN)"            -X POST "$BASE/api/v1/courses" -H "$U" -H "$J" -d '{"name":"X","price":1,"maxCapacity":1}'
check 403 "POST /internal/seed (solo ADMIN)"      -X POST "$BASE/internal/seed" -H "$U"

echo "== Rol ADMIN =="
check 200 "POST /internal/seed"                   -X POST "$BASE/internal/seed" -H "$A"
check 201 "POST /courses"                         -X POST "$BASE/api/v1/courses" -H "$A" -H "$J" -d '{"name":"Docker","price":90000,"maxCapacity":1}'
check 400 "POST /courses inválido"                -X POST "$BASE/api/v1/courses" -H "$A" -H "$J" -d '{"name":"","price":0,"maxCapacity":0}'
check 200 "GET  /courses"                         -H "$A" "$BASE/api/v1/courses"
check 404 "GET  /courses/999999"                  -H "$A" "$BASE/api/v1/courses/999999"

echo "== Matrículas (USER autenticado) =="
EMAIL="prueba$RANDOM@mail.com"
SID=$(curl -s -X POST "$BASE/api/v1/students" -H "$U" -H "$J" -d "{\"name\":\"Prueba Uno\",\"age\":20,\"email\":\"$EMAIL\"}" | sed -E 's/.*"id":([0-9]+).*/\1/')
CID=$(curl -s -X POST "$BASE/api/v1/courses" -H "$A" -H "$J" -d '{"name":"Cupo uno","price":1000,"maxCapacity":1}' | sed -E 's/.*"id":([0-9]+).*/\1/')
check 409 "POST /students correo repetido"        -X POST "$BASE/api/v1/students" -H "$U" -H "$J" -d "{\"name\":\"Otra\",\"age\":20,\"email\":\"$EMAIL\"}"
check 201 "POST /enrollments"                     -X POST "$BASE/api/v1/enrollments" -H "$U" -H "$J" -d "{\"studentId\":$SID,\"courseId\":$CID}"
check 409 "POST /enrollments repetida"            -X POST "$BASE/api/v1/enrollments" -H "$U" -H "$J" -d "{\"studentId\":$SID,\"courseId\":$CID}"
check 404 "POST /enrollments curso inexistente"   -X POST "$BASE/api/v1/enrollments" -H "$U" -H "$J" -d "{\"studentId\":$SID,\"courseId\":999999}"
check 200 "GET  /courses/{id}/enrolled"          -H "$U" "$BASE/api/v1/courses/$CID/enrolled"

echo; echo "Resultado: $ok correctas, $fail fallidas"
[ "$fail" -eq 0 ]
