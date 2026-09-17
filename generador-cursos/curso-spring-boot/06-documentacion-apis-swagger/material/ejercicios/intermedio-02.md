# 🟡 Intermedio 02 — Configurar `application.properties` para un paquete dado

## 🧩 Problema

Continuando el proyecto del ejercicio anterior (`ControladorAutores`, en
el paquete `com.biblioteca`), agregá la configuración de springdoc que
falta en `application.properties`.

## 💻 Código o contexto de partida

```properties
spring.datasource.url=jdbc:h2:mem:biblioteca;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

# TODO: agregar la configuración de springdoc-openapi
```

Agregá las cuatro propiedades necesarias para que Swagger UI quede
disponible en `/doc/swagger-ui.html`, escaneando `com.biblioteca`.

## 📏 Criterios de evaluación de la solución

- `springdoc.api-docs.enabled=true` y `springdoc.swagger-ui.enabled=true`
  presentes.
- `springdoc.swagger-ui.path=/doc/swagger-ui.html`.
- `springdoc.packages-to-scan=com.biblioteca`.
- Las propiedades de conexión a H2 quedan sin cambios.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Intermedio

## 🎓 Resultados de aprendizaje

RA-6
