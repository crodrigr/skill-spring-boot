# 🛠️ Taller 01 — Manejo de excepciones en la API de pacientes

## 🎯 Objetivo (RA-4, RA-6, RA-8)

Reemplazar el manejo manual de errores de `ControladorPacientes`/
`ServicioPacientes` (Taller del Módulo 5) por una excepción personalizada
con `@ResponseStatus`, centralizada en un `@ControllerAdvice`.

## 🌍 Contexto

MediSalud ya tiene `ControladorPacientes`/`ServicioPacientes` funcionando
(Taller del Módulo 5, con su estructura de paquetes MVC:
`com.medisalud.entity`/`repository`/`service`/`controller`). Le falta lo
mismo que se aplicó a `Libro` en los Ejemplos 03-06 de este módulo:
reemplazar `Optional`/`ResponseEntity.notFound()` por una excepción
personalizada y un manejador global.

## 🪜 Pasos

1. **Crear la excepción personalizada**: escribí
   `PacienteNoEncontradoException` (`@ResponseStatus(HttpStatus.NOT_FOUND)`)
   en el paquete nuevo `com.medisalud.exception`, siguiendo el mismo
   patrón de `LibroNoEncontradoException` (Ejemplo 03).
2. **Modificar el servicio y el controlador**: cambiá
   `ServicioPacientes.buscarPorId` para que devuelva `Paciente`
   directamente y lance la excepción (`orElseThrow(...)`) en vez de
   `Optional`; actualizá `actualizar` y `eliminar` para reutilizar
   `buscarPorId`; quitá de `ControladorPacientes.buscarPorId` el
   `ResponseEntity.notFound()` manual.
3. **Centralizar el manejo**: creá `ManejadorGlobalDeExcepciones`
   (`@ControllerAdvice`) en `com.medisalud.exception`, con un
   `@ExceptionHandler(PacienteNoEncontradoException.class)` que devuelva
   `{"error": "<mensaje>"}"` con código `404`.
4. **Probar en Insomnia**: documentá el caso de éxito (`GET
   /pacientes/{id}` con un id existente) y el caso de error (`GET
   /pacientes/{id}` con un id inexistente, verificando el nuevo cuerpo de
   error).

## 💡 Ejemplo resuelto (parcial)

Así se ve `PacienteNoEncontradoException`, para que uses el mismo estilo
en el resto del entregable:

```java
package com.medisalud.exception;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PacienteNoEncontradoException extends RuntimeException {

    public PacienteNoEncontradoException(Long id) {
        super("No existe un paciente con id " + id);
    }
}
```

El resto del entregable (`ServicioPacientes`/`ControladorPacientes`
modificados, y `ManejadorGlobalDeExcepciones` completo) queda a tu cargo
— la solución completa está en `solucion-taller-01.md`, pero intentá
resolverlo primero por tu cuenta.

## 📦 Entregable

```text
📁 taller-01-manejo-de-excepciones-pacientes
└── 📁 src/main
    ├── 📁 java/com/medisalud
    │   ├── 📁 entity
    │   │   └── 📄 Paciente.java
    │   ├── 📁 repository
    │   │   └── 📄 RepositorioPacientes.java
    │   ├── 📁 service
    │   │   └── 📄 ServicioPacientes.java
    │   ├── 📁 controller
    │   │   └── 📄 ControladorPacientes.java
    │   └── 📁 exception
    │       ├── 📄 PacienteNoEncontradoException.java
    │       └── 📄 ManejadorGlobalDeExcepciones.java
    └── 📁 resources
        └── 📄 application.properties
```

## 🧪 Casos de prueba

- `GET /pacientes/{id}` con un id existente: `200` con los datos del
  paciente (sin cambios respecto al Módulo 5).
- `GET /pacientes/{id}` con un id inexistente: `404` con el cuerpo
  `{"error": "No existe un paciente con id <id>"}"`, no el cuerpo por
  defecto de Spring Boot.
- `ControladorPacientes` no debe conservar ningún `ResponseEntity` de
  error construido a mano, ni ningún `@ExceptionHandler` propio.

## 📏 Criterios de evaluación

- `PacienteNoEncontradoException` vive en `com.medisalud.exception`, con
  `@ResponseStatus(HttpStatus.NOT_FOUND)`.
- `ServicioPacientes.buscarPorId` lanza la excepción en vez de devolver
  `Optional` vacío.
- `ManejadorGlobalDeExcepciones` (`@ControllerAdvice`) centraliza el
  manejo, con el cuerpo `{"error": "<mensaje>"}"`.
- Las pruebas en Insomnia documentan tanto el caso de éxito como el de
  error, con el nuevo cuerpo de error.
