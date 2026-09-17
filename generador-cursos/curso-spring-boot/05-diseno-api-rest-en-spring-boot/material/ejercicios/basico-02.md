# 🟢 Básico 02 — Elegir el código de estado correcto

## 🧩 Problema

Para cada uno de los siguientes escenarios sobre la API de MediSalud,
elegí el código de estado HTTP más apropiado (indicando al menos su
rango) y justificá tu elección en una oración.

## 💻 Código o contexto de partida

1. Un cliente pide `GET /pacientes/7` y el paciente existe: se devuelve
   su información correctamente.
2. Un cliente hace `POST /pacientes` con los datos de un paciente nuevo,
   y el paciente se crea correctamente.
3. Un cliente pide `GET /pacientes/999` pero no existe ningún paciente
   con ese id.
4. Un cliente hace `POST /pacientes` sin incluir el cuerpo de la
   solicitud (falta el JSON con los datos).
5. El servidor intenta guardar el paciente, pero la conexión a la base de
   datos falla por un error interno inesperado.

## 📏 Criterios de evaluación de la solución

- Escenario 1: `200 OK` (éxito, con cuerpo de respuesta).
- Escenario 2: `201 Created` (éxito, recurso creado).
- Escenario 3: `404 Not Found` (error del cliente: recurso inexistente).
- Escenario 4: un código `4xx` (por ejemplo, `400 Bad Request`; error del
  cliente por una solicitud mal formada).
- Escenario 5: un código `5xx` (por ejemplo, `500 Internal Server
  Error`; error del servidor, no del cliente).
- La justificación de cada escenario distingue correctamente si el
  problema (si lo hay) es del cliente (`4xx`) o del servidor (`5xx`).

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-3
