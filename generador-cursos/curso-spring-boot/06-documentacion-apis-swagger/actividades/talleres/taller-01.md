# 🛠️ Taller 01 — Documentación automática de la API de pacientes

## 🎯 Objetivo (RA-5, RA-6, RA-7)

Documentar automáticamente con springdoc-openapi la API REST de
`Paciente` (Taller del Módulo 5), y verificar en Swagger UI que sus cinco
endpoints CRUD aparecen correctamente, sin modificar ninguna clase Java.

## 🌍 Contexto

MediSalud ya tiene `ControladorPacientes` funcionando (Taller del Módulo
5), con sus cinco endpoints CRUD. Le falta exactamente lo mismo que a
`ControladorLibros` en los Ejemplos 04-06 de este módulo: la dependencia
y la configuración de springdoc-openapi.

## 🪜 Pasos

1. **Agregar la dependencia**: agregá
   `springdoc-openapi-starter-webmvc-ui` (versión `2.6.0`) al `pom.xml`
   del proyecto de `ControladorPacientes`, siguiendo el mismo patrón del
   Ejemplo 04.
2. **Configurar `application.properties`**: agregá
   `springdoc.api-docs.enabled=true`, `springdoc.swagger-ui.enabled=true`,
   `springdoc.swagger-ui.path=/doc/swagger-ui.html` y
   `springdoc.packages-to-scan=com.medisalud`, siguiendo el mismo patrón
   del Ejemplo 05.
3. **Verificar en Swagger UI**: accedé a
   `http://localhost:8080/doc/swagger-ui.html` y confirmá que los cinco
   endpoints de `ControladorPacientes` (`GET /pacientes`, `GET
   /pacientes/{id}`, `POST /pacientes`, `PUT /pacientes/{id}`, `DELETE
   /pacientes/{id}`) aparecen documentados, con sus parámetros y códigos
   de respuesta.

## 💡 Ejemplo resuelto (parcial)

Así se ve el bloque de dependencia a agregar en el `pom.xml`, para que
uses el mismo estilo en el resto del entregable:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

El resto del entregable (la configuración completa de
`application.properties` y la verificación en Swagger UI) queda a tu
cargo — la solución completa está en `solucion-taller-01.md`, pero
intentá resolverlo primero por tu cuenta.

## 📦 Entregable

```text
📁 taller-01-documentacion-pacientes
└── 📁 src/main
    ├── 📁 java/com/medisalud
    │   ├── 📄 Paciente.java
    │   ├── 📄 RepositorioPacientes.java
    │   ├── 📄 ServicioPacientes.java
    │   └── 📄 ControladorPacientes.java
    └── 📁 resources
        └── 📄 application.properties
```

(Las cuatro clases Java quedan exactamente igual que en el Módulo 5; el
`pom.xml` y `application.properties` son los únicos archivos que
cambian.)

## 🧪 Casos de prueba

- Acceder a Swagger UI y confirmar que aparecen los cinco endpoints de
  `ControladorPacientes`.
- Confirmar que el esquema de `Paciente` documentado incluye `id`,
  `codigo` y `nombre`.
- Confirmar que `GET /pacientes/{id}` documenta tanto `200` como `404`
  como posibles respuestas.

## 📏 Criterios de evaluación

- La dependencia springdoc se agrega sin modificar ninguna clase Java
  existente.
- `application.properties` incluye las cuatro propiedades de springdoc,
  con `packages-to-scan` apuntando al paquete correcto (`com.medisalud`).
- Swagger UI muestra los cinco endpoints, sin necesitar ninguna anotación
  adicional sobre `ControladorPacientes`/`Paciente`.
