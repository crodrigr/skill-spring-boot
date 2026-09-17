# 🟢 Básico 02 — Identificar las secciones de una definición OpenAPI

## 🧩 Problema

Dado el siguiente fragmento de una definición OpenAPI para la API de
Pacientes de MediSalud, identificá qué representa cada una de las tres
secciones marcadas con un comentario, y a qué endpoint concreto del
Módulo 5 corresponde `paths`.

## 💻 Código o contexto de partida

```yaml
openapi: 3.0.1
info:
  title: API de MediSalud
  version: "1.0"
servers:                          # Sección A
  - url: http://localhost:8080
paths:                            # Sección B
  /pacientes/{id}:
    get:
      summary: Buscar un paciente por id
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: Paciente encontrado
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Paciente'
        '404':
          description: Paciente no encontrado
components:                       # Sección C
  schemas:
    Paciente:
      type: object
      properties:
        id:
          type: integer
        codigo:
          type: string
        nombre:
          type: string
```

## 📏 Criterios de evaluación de la solución

- Sección A (`servers`): dónde corre la API.
- Sección B (`paths`): las rutas, sus métodos HTTP, parámetros y
  respuestas.
- Sección C (`components`): el esquema reutilizable de `Paciente`.
- El endpoint de `paths` corresponde a `GET /pacientes/{id}` de
  `ControladorPacientes` (Módulo 5, Taller), que devuelve `200` con el
  paciente o `404` si no existe.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-3
