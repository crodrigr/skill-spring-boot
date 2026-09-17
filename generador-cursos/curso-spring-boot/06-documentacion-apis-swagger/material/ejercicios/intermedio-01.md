# 🟡 Intermedio 01 — Agregar springdoc-openapi a un proyecto existente

## 🧩 Problema

Biblioteca Universitaria tiene otro proyecto Spring Boot con
`ControladorAutores` (Módulo 5) ya funcionando, pero sin ninguna
dependencia de documentación. Te piden agregarla.

## 💻 Código o contexto de partida

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
    <!-- TODO: agregar la dependencia de springdoc-openapi -->
</dependencies>
```

Agregá la dependencia `springdoc-openapi-starter-webmvc-ui` (versión
2.6.0) al bloque `<dependencies>`, sin modificar ninguna de las
dependencias ya existentes ni ninguna clase Java del proyecto.

## 📏 Criterios de evaluación de la solución

- El `pom.xml` resultante conserva las tres dependencias originales sin
  cambios.
- Se agrega exactamente un bloque `<dependency>` nuevo, con
  `groupId=org.springdoc`, `artifactId=springdoc-openapi-starter-webmvc-ui`
  y `version=2.6.0`.
- No se modifica ninguna clase Java (`ControladorAutores`,
  `ServicioAutores`, etc.).

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-5
