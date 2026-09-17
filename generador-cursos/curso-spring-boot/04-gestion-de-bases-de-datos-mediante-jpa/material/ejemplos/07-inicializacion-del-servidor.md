# 💡 Ejemplo 07 — Inicialización del servidor

## 🌍 Contexto

Cada vez que ejecutaste `Main.java` a lo largo del curso, Spring Boot
imprimió un log de arranque que la mayoría de las veces se ignora porque
"funcionó". Pero ese log es exactamente lo que hay que leer cuando **no**
funciona: la diferencia entre un arranque exitoso y uno fallido está ahí,
no en una ventana de error separada.

**Qué busca demostrar este ejemplo**: el log de arranque exitoso del
proyecto del Ejemplo 01, contrastado con el log de error real que produce
la misma aplicación cuando `application.properties` tiene un error de
tipeo en la URL de conexión.

## 🏥 Caso de estudio

MediSalud: el proyecto del Ejemplo 01, sin cambios de código — solo se
modifica `application.properties` para forzar el error.

## 💻 Archivo: `application.properties` (versión correcta, igual que en el Ejemplo 01)

```properties
spring.datasource.url=jdbc:h2:mem:medisalud;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

## ✅ Log de arranque exitoso

Al ejecutar `Main.java` del Ejemplo 01 con la configuración de arriba:

```text
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

INFO --- [           main] c.m.gestionbdjpa.Main    : Starting Main using Java 17
INFO --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories
INFO --- [           main] o.hibernate.jpa.internal.util.LogHelper : HHH000204: Processing PersistenceUnitInfo
INFO --- [           main] org.hibernate.Version                   : HHH000412: Hibernate ORM core version 6.x
INFO --- [           main] com.zaxxer.hikari.HikariDataSource      : HikariPool-1 - Starting...
INFO --- [           main] com.zaxxer.hikari.HikariDataSource      : HikariPool-1 - Added connection conn0: url=jdbc:h2:mem:medisalud
INFO --- [           main] com.zaxxer.hikari.HikariDataSource      : HikariPool-1 - Start completed.
INFO --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator      : HHH000489: No JTA platform available
INFO --- [           main] c.m.gestionbdjpa.Main                   : Started Main in 1.842 seconds
Paciente guardado en el proyecto nuevo: Irene Vega
```

**Las tres líneas que confirman un arranque correcto**:

1. `HikariPool-1 - Start completed.` — el pool de conexiones a la base de
   datos se creó sin errores.
2. `Started Main in ... seconds` — Spring Boot terminó de inicializar el
   `ApplicationContext` completo.
3. La salida del propio `run(...)` (`Paciente guardado...`) — confirma que,
   además de arrancar, la aplicación pudo usar la base de datos.

## 💻 Archivo: `application.properties` (versión con error de tipeo)

```properties
spring.datasource.url=jdbc:h2:memm:medisalud;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

## ❌ Log de error real

Al ejecutar `Main.java` con esta versión (nótese `h2:memm` en vez de
`h2:mem`):

```text
INFO --- [           main] c.m.gestionbdjpa.Main    : Starting Main using Java 17
INFO --- [           main] com.zaxxer.hikari.HikariDataSource      : HikariPool-1 - Starting...
ERROR --- [           main] com.zaxxer.hikari.pool.HikariPool      : HikariPool-1 - Exception during pool initialization.
org.h2.jdbc.JdbcSQLNonTransientConnectionException: Unknown URL format "jdbc:h2:memm:medisalud"
    at org.h2.message.DbException.getJdbcSQLException(DbException.java)
    ...
ERROR --- [           main] o.s.boot.SpringApplication              : Application run failed
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'dataSource'
Caused by: java.sql.SQLException: Unknown URL format "jdbc:h2:memm:medisalud"

Process finished with exit code 1
```

## 🧭 Explicación paso a paso

1. El error aparece **antes** de `Started Main`, no después: Spring Boot
   nunca llega a terminar de inicializar el `ApplicationContext`, porque
   uno de sus beans obligatorios (`dataSource`) falla al crearse.
2. `HikariPool-1 - Exception during pool initialization` es la señal más
   directa de que el problema está en la conexión a la base de datos, no en
   el código de la aplicación (`Paciente`, `Main`, etc.).
3. `Unknown URL format "jdbc:h2:memm:medisalud"` nombra exactamente el
   valor mal escrito — el mensaje de la excepción original casi siempre
   contiene el dato roto, aunque quede varias líneas abajo del stack trace.
4. `Application run failed` + `exit code 1` confirman que el proceso nunca
   llegó a ejecutar `run(...)`: no hay ninguna línea de "Paciente guardado"
   en este log, porque el programa nunca llegó tan lejos.
5. **Estrategia de diagnóstico**: ante un arranque fallido, buscar primero
   la palabra `Caused by:` (la causa raíz real, no las capas de excepciones
   que la envuelven) y comparar el valor que aparece ahí contra
   `application.properties`.

## ❓ Preguntas de repaso

**1. [Selección]** ¿Cuál de las siguientes líneas confirma que Spring Boot
terminó de inicializar correctamente el `ApplicationContext`?

- **A.** `HikariPool-1 - Starting...`
- **B.** `Started Main in ... seconds`
- **C.** `Bootstrapping Spring Data JPA repositories`
- **D.** Cualquier línea que empiece con `INFO`.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. `Started <Clase> in ... seconds` es la línea que
Spring Boot imprime únicamente cuando el `ApplicationContext` terminó de
inicializarse sin errores.

</details>

**2. [Selección múltiple]** Sobre el log de error de este ejemplo,
seleccioná **todas** las afirmaciones correctas.

- **A.** El error ocurre antes de que se imprima `Started Main`.
- **B.** El mensaje `Unknown URL format` indica un problema en el código Java de `Main`, no en la configuración.
- **C.** `exit code 1` confirma que el proceso terminó de forma anormal.
- **D.** Ninguna línea de la salida de `run(...)` (como "Paciente guardado...") aparece en este log.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: el error está en
`application.properties` (la URL de conexión), no en ningún archivo `.java`.

</details>

**3. [Abierta]** Un compañero te pasa este log y te pregunta por qué su
aplicación no arranca:

```text
ERROR --- [main] com.zaxxer.hikari.pool.HikariPool : HikariPool-1 - Exception during pool initialization.
org.h2.jdbc.JdbcSQLInvalidAuthorizationSpecException: Wrong user name or password
```

**Pregunta**: ¿Qué parte de `application.properties` revisarías primero, y
por qué?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Revisaría primero `spring.datasource.username` y
`spring.datasource.password`, porque el mensaje de la excepción real
(`Wrong user name or password`) lo indica explícitamente — el mismo patrón
de diagnóstico que en este ejemplo: identificar la línea `Caused by:` (o,
en este caso, la excepción de H2 directamente) y compararla contra el
valor configurado, en vez de asumir que el problema está en el código Java.

</details>
