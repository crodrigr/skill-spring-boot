# 🔑 Soluciones — Ejercicios del Módulo 6

> Material docente: no enlazar ni distribuir desde el material dirigido al
> estudiante. Vive aparte de `material/ejercicios/` para que ninguna solución
> aparezca junto al enunciado.

## 🟢 Básico 01 — Identificar la característica de Swagger que resuelve un problema

**Solución propuesta**:

1. Generación de código — genera automáticamente un cliente en el
   lenguaje que se necesite, a partir de la especificación OpenAPI.
2. Interfaz gráfica interactiva (Swagger UI) — permite explorar y probar
   endpoints desde el navegador, sin instalar nada aparte.
3. Validación de entradas y salidas — la especificación define esquemas
   que Swagger usa para validar automáticamente.
4. Generación automática de documentación — se regenera a partir del
   código, sin mantenimiento manual.

## 🟢 Básico 02 — Identificar las secciones de una definición OpenAPI

**Solución propuesta**:

- Sección A (`servers`): declara que la API corre en
  `http://localhost:8080`.
- Sección B (`paths`): describe `GET /pacientes/{id}`, con su parámetro
  `id` (`in: path`) y sus respuestas `200`/`404`.
- Sección C (`components`): define el esquema reutilizable `Paciente`
  (`id`, `codigo`, `nombre`).
- El endpoint corresponde a `ControladorPacientes.buscarPorId(...)`
  (Módulo 5, Taller).

## 🟡 Intermedio 01 — Agregar springdoc-openapi a un proyecto existente

**Solución propuesta**:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.6.0</version>
    </dependency>
</dependencies>
```

**Verificación**: las tres dependencias originales quedan intactas; se
agrega exactamente una dependencia nueva, sin tocar ninguna clase Java.

## 🟡 Intermedio 02 — Configurar `application.properties` para un paquete dado

**Solución propuesta**:

```properties
spring.datasource.url=jdbc:h2:mem:biblioteca;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/doc/swagger-ui.html
springdoc.packages-to-scan=com.biblioteca
```

## 🔴 Avanzado 01 — Diagnosticar por qué Swagger UI no muestra los endpoints esperados

**Solución propuesta**:

1. `ControladorAutores` vive en `com.biblioteca.web`, pero
   `springdoc.packages-to-scan` apunta a `com.biblioteca.controllers` —
   springdoc busca en un paquete que no contiene ningún `@RestController`
   (o que ni siquiera existe), así que no encuentra nada que documentar.
2. Corrección preferida: mover la clase al paquete de su capa MVC, que es
   el que ya declara la configuración. `web` no es una de las capas del
   proyecto (`controllers`, `services`, `persistences`):

```java
package com.biblioteca.controllers;

@RestController
@RequestMapping("/autores")
public class ControladorAutores {
    // ... endpoints ya implementados
}
```

   `springdoc.packages-to-scan=com.biblioteca.controllers` queda sin
   cambios.

   Alternativa válida, si no se puede mover la clase:

```properties
springdoc.packages-to-scan=com.biblioteca.web
```

**Verificación**: Swagger UI carga correctamente en ambos casos (antes y
después); la diferencia es que, tras la corrección, aparecen los
endpoints de `ControladorAutores`.

## 🟢 Básico 03 — Elegir entre documentación manual y automática

**Solución propuesta**:

1. Documentación manual (SwaggerHub) — el código todavía no existe; hace
   falta diseñar y acordar el contrato de la API primero.
2. Documentación automática (springdoc-openapi) — ya hay código real en
   producción, y se busca que la documentación se mantenga sincronizada
   sin esfuerzo manual.
3. Ambas son válidas: springdoc-openapi documenta el proyecto en tiempo
   real (si el cliente pudiera acceder a Swagger UI), pero exportar desde
   SwaggerHub una página HTML estática (Ejemplo 03) es la opción más
   directa si el cliente no tiene ningún acceso al proyecto en ejecución.

## 🏆 Desafío 01 — Documentación automática de la API de citas

**Solución propuesta**:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

```properties
spring.datasource.url=jdbc:h2:mem:medisalud;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/doc/swagger-ui.html
springdoc.packages-to-scan=com.medisalud
```

**Documentación final visible en Swagger UI**:

```text
GET    /citas            → listarTodos       → 200
GET    /citas/{id}       → buscarPorId       → 200, 404
POST   /citas            → crear             → 201 (cuerpo: Cita)
PUT    /citas/{id}       → actualizar        → 200, 404 (cuerpo: Cita)
DELETE /citas/{id}       → eliminar          → 200, 404
```

```text
Cita
├── id: integer
├── fecha: string (date)
├── motivo: string
└── paciente: Paciente

Paciente
├── id: integer
├── codigo: string
└── nombre: string
(citas no aparece, por @JsonIgnore)
```

**Verificación**: el esquema de `Paciente` no incluye `citas`; el de
`Cita` sí incluye `paciente` completo — el mismo `@JsonIgnore` que
resolvió la recursión infinita en el Módulo 5 determina qué aparece en la
documentación.
