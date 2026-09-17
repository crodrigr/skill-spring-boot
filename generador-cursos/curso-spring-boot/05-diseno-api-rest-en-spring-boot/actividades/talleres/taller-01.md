# 🛠️ Taller 01 — API REST de pacientes para MediSalud

## 🎯 Objetivo (RA-7, RA-8, RA-11)

Construir el CRUD REST completo (`Service` + `Controller`) sobre
`Paciente` (versión básica, sin relaciones), y probar cada uno de sus
cinco endpoints en Insomnia, incluyendo un caso de error `404`.

## 🌍 Contexto

MediSalud ya tiene `Paciente` convertida en entidad JPA y persistida
contra H2 (Módulo 3/4). Le falta exponerla como API REST, siguiendo
exactamente el mismo patrón que `ServicioLibros`/`ControladorLibros`
(Ejemplos 05-07 de este módulo), pero aplicado a `Paciente`.

Este Taller es, además, el primero del curso en organizar sus clases en
paquetes separados por capa (`entity`, `repository`, `service`,
`controller`), llevando a la práctica la arquitectura del Ejemplo 03
(Controller → Service → Repository → Database) directamente a la
estructura de carpetas del proyecto — no solo como un concepto, sino como
paquetes Java reales. De acá en adelante, todo el material del curso
sigue esta misma organización.

## 🪜 Pasos

1. **Reorganizar en paquetes por capa**: movés `Paciente` (Módulo 3/4) al
   paquete `com.medisalud.entity`, y `RepositorioPacientes` al paquete
   `com.medisalud.repository` — ningún código de esas dos clases cambia,
   solo su ubicación y su línea `package`.
2. **Crear la clase de servicio**: escribí `ServicioPacientes`
   (`@Service`) en el paquete `com.medisalud.service`, inyectando
   `RepositorioPacientes` por constructor, con los cinco métodos de
   negocio (`listarTodos`, `buscarPorId`, `crear`, `actualizar`,
   `eliminar`) siguiendo el mismo patrón de `ServicioLibros`.
3. **Crear el controlador REST**: escribí `ControladorPacientes`
   (`@RestController`, `@RequestMapping("/pacientes")`) en el paquete
   `com.medisalud.controller`, inyectando `ServicioPacientes`, con los
   cinco endpoints CRUD: `GET /pacientes`, `GET /pacientes/{id}`, `POST
   /pacientes`, `PUT /pacientes/{id}` y `DELETE /pacientes/{id}`,
   devolviendo el código de estado correcto en cada caso (`200`, `201`,
   `404`).
4. **Probar en Insomnia**: documentá, para cada uno de los cinco
   endpoints, la solicitud y la respuesta obtenida (método, URL, cuerpo
   si aplica, código de estado, cuerpo de la respuesta), incluyendo al
   menos un caso de error `404` (por ejemplo, buscar un paciente después
   de eliminarlo).

## 💡 Ejemplo resuelto (parcial)

Así se ve el encabezado y el método `actualizar` de `ServicioPacientes`,
para que uses el mismo estilo en el resto del entregable:

```java
package com.medisalud.service;

import com.medisalud.entity.Paciente;
import com.medisalud.repository.RepositorioPacientes;

// ...

public Optional<Paciente> actualizar(Long id, Paciente datos) {
    return repositorioPacientes.findById(id)
            .map(paciente -> {
                paciente.setNombre(datos.getNombre());
                return repositorioPacientes.save(paciente);
            });
}
```

**Nota sobre paquetes**: cada clase declara su propio `package` según la
capa a la que pertenece, y solo importa explícitamente las clases del
proyecto que vienen de **otro** paquete (por ejemplo, `Paciente` desde
`service`); las clases de Spring/Java (`@Service`, `Optional`, etc.) se
omiten de estos fragmentos por brevedad, igual que en el resto del curso.

**Nota**: `Paciente` necesita un método `setNombre(String nombre)` que el
Módulo 3/4 no le agregó (nunca hizo falta actualizarla); agregalo vos como
parte de este taller, junto al resto de sus getters ya existentes.

El resto del entregable (`ServicioPacientes` completo,
`ControladorPacientes` y las pruebas en Insomnia) queda a tu cargo — la
solución completa está en `solucion-taller-01.md`, pero intentá
resolverlo primero por tu cuenta.

## 📦 Entregable

```text
📁 taller-01-api-pacientes
└── 📁 src/main
    ├── 📁 java/com/medisalud
    │   ├── 📁 entity
    │   │   └── 📄 Paciente.java
    │   ├── 📁 repository
    │   │   └── 📄 RepositorioPacientes.java
    │   ├── 📁 service
    │   │   └── 📄 ServicioPacientes.java
    │   └── 📁 controller
    │       └── 📄 ControladorPacientes.java
    └── 📁 resources
        └── 📄 application.properties
```

## 🧪 Casos de prueba

- Crear un `Paciente` (`POST /pacientes`) y verificar que la respuesta es
  `201` con el paciente creado (incluido su `id`).
- Buscarlo por id (`GET /pacientes/{id}`) y verificar `200` con sus
  datos.
- Actualizar su `nombre` (`PUT /pacientes/{id}`) y verificar `200` con el
  nombre actualizado.
- Eliminarlo (`DELETE /pacientes/{id}`) y verificar `200`.
- Buscarlo de nuevo por el mismo id y verificar `404` (ya no existe).

## 📏 Criterios de evaluación

- `Paciente`, `RepositorioPacientes`, `ServicioPacientes` y
  `ControladorPacientes` viven cada una en su paquete correspondiente
  (`entity`, `repository`, `service`, `controller`), con la línea
  `package` correcta y los `import` necesarios hacia las clases de otro
  paquete del proyecto.
- `ServicioPacientes` y `ControladorPacientes` siguen exactamente el
  mismo patrón de capas que `ServicioLibros`/`ControladorLibros`.
- Los cinco endpoints devuelven el código de estado correcto en cada
  caso, incluido `404` cuando el recurso no existe.
- Las cinco pruebas en Insomnia están documentadas con método, URL,
  cuerpo (si aplica), código de estado y respuesta.
