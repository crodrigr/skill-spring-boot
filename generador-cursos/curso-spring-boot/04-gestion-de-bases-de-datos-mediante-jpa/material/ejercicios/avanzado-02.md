# 🔴 Avanzado 02 — Diagnosticar un arranque fallido a partir del log

## 🧩 Problema

Un compañero de equipo te pasa este log y te dice: "mi aplicación no
arranca y no sé por qué".

## 💻 Código o contexto de partida

```text
INFO --- [           main] c.b.gestionbdjpa.Main    : Starting Main using Java 17
INFO --- [           main] com.zaxxer.hikari.HikariDataSource      : HikariPool-1 - Starting...
ERROR --- [           main] com.zaxxer.hikari.pool.HikariPool      : HikariPool-1 - Exception during pool initialization.
org.h2.jdbc.JdbcSQLNonTransientConnectionException: Unknown URL format "jdbc:h3:mem:biblioteca"
    at org.h2.message.DbException.getJdbcSQLException(DbException.java)
    ...
ERROR --- [           main] o.s.boot.SpringApplication              : Application run failed
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'dataSource'
Caused by: java.sql.SQLException: Unknown URL format "jdbc:h3:mem:biblioteca"

Process finished with exit code 1
```

```properties
spring.datasource.url=jdbc:h3:mem:biblioteca;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

**Preguntas**:

1. ¿En qué línea del log está la causa raíz del error?
2. ¿Qué parte exacta de `application.properties` está mal configurada?
3. ¿Cómo la corregirías?

## 📏 Criterios de evaluación de la solución

- Identifica que el error ocurre antes de cualquier línea `Started Main`,
  y que por lo tanto la aplicación nunca terminó de inicializar el
  `ApplicationContext`.
- Señala la línea `Unknown URL format "jdbc:h3:mem:biblioteca"` (o la
  equivalente `Caused by:`) como la causa raíz, no las líneas de
  `BeanCreationException` que la envuelven.
- Identifica el error de tipeo exacto: `jdbc:h3:mem:...` en vez de
  `jdbc:h2:mem:...` en `spring.datasource.url`.
- Propone la corrección concreta: cambiar `h3` por `h2` en la URL.

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Avanzado

## 🎓 Resultados de aprendizaje

RA-11
