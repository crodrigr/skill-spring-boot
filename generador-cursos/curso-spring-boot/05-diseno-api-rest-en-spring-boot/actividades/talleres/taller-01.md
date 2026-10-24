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

Este Taller es, además, el primero que completa las tres capas del curso:
presentación (`presentacion`), negocio (`negocio`) y persistencia
(`persistence`, que ya existía desde el Módulo 3). Lleva a la práctica la
arquitectura del Ejemplo 03 (Controller → Service → Repository → Database)
directamente a la estructura de carpetas del proyecto — no solo como un
concepto, sino como paquetes Java reales. De acá en adelante, todo el
material del curso sigue esta misma organización.

## 🏗️ Capas del proyecto

| Capa | Paquete | Qué contiene | Clase en este taller |
|---|---|---|---|
| **Presentación** | `com.medisalud.presentacion.controlador` | Endpoints HTTP: recibe la solicitud, delega en el servicio y devuelve la respuesta con su código de estado. | `ControladorPacientes` |
| **Negocio** | `com.medisalud.negocio.service` | Lógica de negocio. Es la única capa que habla con la de persistencia. | `ServicioPacientes` |
| **Persistencia** | `com.medisalud.persistence` | Acceso a datos: `model` (clases `@Entity`) y `repository` (interfaces de Spring Data JPA). | `Paciente`, `RepositorioPacientes` |

Regla de dependencia: `presentacion` → `negocio` → `persistence`. Un
controlador nunca inyecta un repositorio directamente.

## 🪜 Pasos

1. **Revisar la capa de persistencia**: confirmá que `Paciente` (Módulo 3/4) está en el
   paquete `com.medisalud.negocio.model`, y `RepositorioPacientes`
   en `com.medisalud.persistence.repository` (ambos forman la capa de
   persistencia, `persistence`). Si en tu proyecto estaban en otro paquete,
   movelos: ningún código de esas dos clases cambia, solo su ubicación y su
   línea `package`.
2. **Crear la clase de servicio**: escribí `ServicioPacientes`
   (`@Service`) en el paquete `com.medisalud.negocio.service`, inyectando
   `RepositorioPacientes` por constructor, con los cinco métodos de
   negocio (`listarTodos`, `buscarPorId`, `crear`, `actualizar`,
   `eliminar`) siguiendo el mismo patrón de `ServicioLibros`.
3. **Crear el controlador REST**: escribí `ControladorPacientes`
   (`@RestController`, `@RequestMapping("/pacientes")`) en el paquete
   `com.medisalud.presentacion.controlador`, inyectando `ServicioPacientes`, con los
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
package com.medisalud.negocio.service;

import com.medisalud.negocio.model.Paciente;
import com.medisalud.persistence.repository.RepositorioPacientes;

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
`negocio.model`); las clases de Spring/Java (`@Service`, `Optional`, etc.) se
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
    │   ├── 📁 presentacion                       (capa de presentación)
    │   │   └── 📁 controlador
    │   │       └── 📄 ControladorPacientes.java
    │   ├── 📁 negocio                            (capa de negocio)
    │   │   ├── 📁 model
    │   │   │   └── 📄 Paciente.java
    │   │   └── 📁 service
    │   │       └── 📄 ServicioPacientes.java
    │   └── 📁 persistence                        (capa de persistencia)
    │       └── 📁 repository
    │           └── 📄 RepositorioPacientes.java
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
  (`negocio.model`, `persistence.repository`, `negocio`,
  `presentacion`), con la línea `package` correcta y los `import`
  necesarios hacia las clases de otro paquete del proyecto.
- El controlador solo depende de `ServicioPacientes` y el servicio solo
  depende de `RepositorioPacientes`: ninguna capa se salta a la
  siguiente (`presentacion` → `negocio` → `persistence`).
- `ServicioPacientes` y `ControladorPacientes` siguen exactamente el
  mismo patrón de capas que `ServicioLibros`/`ControladorLibros`.
- Los cinco endpoints devuelven el código de estado correcto en cada
  caso, incluido `404` cuando el recurso no existe.
- Las cinco pruebas en Insomnia están documentadas con método, URL,
  cuerpo (si aplica), código de estado y respuesta.
