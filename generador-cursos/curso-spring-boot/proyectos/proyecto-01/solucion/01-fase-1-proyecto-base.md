# 🧱 Fase 1 — Proyecto base, beans y configuración

**Navegación**: [Índice](README.md) · ← [Fase 0 — Análisis](00-analisis.md) · Siguiente → [Fase 2 — Modelo de datos](02-fase-2-modelo-de-datos.md)

## 🎯 Qué vas a lograr

Un proyecto Spring Boot que **arranca** contra la base de datos que elijas —**H2** (por
defecto, sin instalar nada), **MySQL** o **PostgreSQL**—, con la estructura
de paquetes por capa y dos *beans* de configuración que usaremos en todo el proyecto:
un reloj (`Clock`) y un codificador de contraseñas (`PasswordEncoder`).

**Módulos que se aplican**: 01 (Introducción a Spring Boot) y 02 (Dependencias y Java Beans).

## 🧰 Requisitos previos

| Herramienta | Versión | Para qué |
|---|---|---|
| JDK | 17 o superior | Compilar y ejecutar (el proyecto compila con `release 17`) |
| Maven | 3.6.3 o superior (o el que trae tu IDE) | Compilar y ejecutar con `mvn spring-boot:run` |
| Insomnia (o `curl`) | cualquiera | Probar la API |
| Git | cualquiera | Un commit por fase |
| Docker *(opcional)* | cualquiera reciente | Levantar MySQL o PostgreSQL con un comando. **No hace falta** si usás H2 |

Verificá en una terminal:

```bash
java -version
mvn -version
```

## 🪜 Paso a paso

### Paso 1.1 — Crear el proyecto

**Opción A — con Spring Initializr** (recomendada): entrá a <https://start.spring.io> y
completá:

| Campo | Valor |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.3.x (la última 3.3 disponible) |
| Group | `com.coworkhub` |
| Artifact / Name | `coworkhub` |
| Packaging | Jar |
| Java | 17 |
| Dependencies | **Spring Web**, **Spring Data JPA**, **H2 Database**, y —opcionales— **MySQL Driver** y **PostgreSQL Driver** |

Descargá el `.zip`, descomprimilo y abrí la carpeta `coworkhub` en tu IDE. Luego:

1. Borrá la carpeta `src/test`: este proyecto no usa pruebas automatizadas; las pruebas
   son los escenarios de aceptación de la [Fase 7](07-pruebas-de-aceptacion-y-readme.md).
2. Renombrá la clase `CoworkhubApplication` a `Main` (así se llama en todo el curso).
3. Reemplazá el contenido de `pom.xml`, `Main.java` y `application.properties` por los de
   los pasos siguientes.

**Opción B — a mano**: creá la carpeta `coworkhub` y dentro los archivos de los pasos
1.2, 1.3 y 1.5, respetando las rutas que se indican encima de cada bloque de código.

### Paso 1.2 — `pom.xml`

El `pom.xml` declara las dependencias. Cada una cumple un papel:

| Dependencia | Para qué sirve |
|---|---|
| `spring-boot-starter-web` | Controladores REST y servidor Tomcat embebido |
| `spring-boot-starter-data-jpa` | JPA + Hibernate + Spring Data (repositorios) |
| `mysql-connector-j` (scope `runtime`) | Driver JDBC para conectarse a **MySQL** |
| `postgresql` (scope `runtime`) | Driver JDBC para conectarse a **PostgreSQL** |
| `h2` (scope `runtime`) | Base de datos en memoria; solo se necesita al ejecutar, no al compilar |
| `spring-security-crypto` | **Solo** el codificador de contraseñas BCrypt |

> ⚠️ **¿Por qué `spring-security-crypto` y no `spring-boot-starter-security`?** El *starter*
> completo activa la seguridad de inmediato: todos los endpoints exigirían autenticación
> y no podrías probar las fases 3 a 5. Como necesitamos BCrypt desde la Fase 3b (para
> guardar la contraseña de los miembros que se registren), usamos solo esa pieza. En la
> [Fase 6](06-fase-6-seguridad-jwt.md) la reemplazaremos por el *starter* completo.

Los tres drivers conviven en el proyecto: **solo se usa el de la base que actives** (paso 1.6).
Además, `spring-boot-starter-parent` fija las versiones de todas las dependencias, y
`java.version` en `17` hace que el proyecto compile para Java 17 aunque tengas un JDK más
nuevo.

**📄 `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.5</version>
        <relativePath/>
    </parent>

    <groupId>com.coworkhub</groupId>
    <artifactId>coworkhub</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>coworkhub</name>
    <description>API REST de reservas de salas y espacios de trabajo</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <!-- Drivers de las tres bases de datos soportadas (solo se usa el del perfil activo) -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <!-- Solo el codificador BCrypt. Todavía NO activa Spring Security (Fase 6). -->
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-crypto</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### Paso 1.3 — Clase principal

`@SpringBootApplication` activa la configuración automática y el escaneo de
componentes. **Escanea el paquete donde está esta clase y todos sus subpaquetes**, por
eso `Main` va en el paquete raíz `com.coworkhub`, por encima de `controllers`,
`services` y `persistences`.

**📄 `src/main/java/com/coworkhub/Main.java`**

```java
package com.coworkhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
```

### Paso 1.4 — Estructura de paquetes

Dentro de `src/main/java/com/coworkhub` vas a crear estos paquetes (en tu IDE:
*New → Package*). Algunos quedarán vacíos hasta las fases siguientes; Git no versiona
carpetas vacías, así que no aparecerán hasta que tengan una clase.

```text
📁 com.coworkhub
├── 📄 Main.java
├── 📁 config                     → beans y cargador de reservas de ejemplo (transversal)
├── 📁 controllers                → capa Controller
├── 📁 services                   → capa Service
├── 📁 persistences
│   ├── 📁 entities               → clases @Entity
│   └── 📁 repositories           → interfaces JpaRepository
├── 📁 dto                        → records de solicitud y respuesta (transversal)
├── 📁 exception                  → excepciones y manejador global (transversal)
└── 📁 security
    ├── 📁 controllers
    ├── 📁 services
    ├── 📁 persistences
    │   ├── 📁 entities
    │   └── 📁 repositories
    ├── 📁 config
    └── 📁 jwt
```

Regla de dependencias que vas a respetar en todo el proyecto:
`controllers` → `services` → `persistences`. Un controlador **nunca** usa un repositorio.

### Paso 1.5 — `application.properties` (configuración común)

Este archivo tiene lo que **no depende de la base de datos**. Línea por línea:

- `spring.application.name`: nombre de la aplicación (aparece en los logs).
- `spring.profiles.active=h2`: **qué base de datos se usa**. Un *perfil* de Spring es un
  conjunto de propiedades que se suman a estas cuando está activo (paso 1.6). Por defecto es
  `h2`; podés cambiarlo a `mysql` o `postgres` sin tocar el código.
- `spring.jpa.open-in-view=true`: mantiene abierta la sesión de JPA durante toda la
  solicitud web. Lo declaramos explícitamente porque nuestras respuestas serializan
  colecciones `LAZY` (por ejemplo, los equipamientos de una sala) *después* de que el
  servicio terminó; sin esta opción fallarían con `LazyInitializationException`.
- `coworkhub.reloj.fijo`: propiedad **propia** (el prefijo `coworkhub.` es nuestro).
  Vacía = hora real. La usará el bean `Clock` del paso 1.8.

**📄 `src/main/resources/application.properties`**

```properties
spring.application.name=coworkhub

# Base de datos activa: h2 (por defecto), mysql o postgres.
# Cambiala con --spring.profiles.active=mysql o con la variable de entorno SPRING_PROFILES_ACTIVE.
# La conexión de cada una está en application-h2.properties, application-mysql.properties
# y application-postgres.properties.
spring.profiles.active=h2

spring.jpa.open-in-view=true

# Reloj de la aplicación. Vacío = hora real. Ejemplo para pruebas:
# coworkhub.reloj.fijo=2030-06-03T08:00:00
coworkhub.reloj.fijo=
```

### Paso 1.6 — Elegir la base de datos: perfiles `h2`, `mysql` y `postgres`

El proyecto funciona igual con **tres bases de datos**. Para cada una hay un archivo
`application-<perfil>.properties` con **solo lo que cambia**: la conexión. Cuando activás un
perfil, Spring carga `application.properties` y, encima, el archivo de ese perfil.

| | **H2** (por defecto) | **MySQL** | **PostgreSQL** |
|---|---|---|---|
| Perfil | `h2` | `mysql` | `postgres` |
| Instalación | **Ninguna** | Docker o instalación local | Docker o instalación local |
| Los datos | En memoria: se pierden al cerrar la aplicación | En disco: sobreviven | En disco: sobreviven |
| Puerto | — | 3306 | 5432 |
| `ddl-auto` | `update` | `create` | `create` |
| Conviene para | Aprender y probar rápido; **es la que usa esta guía** | Ver el sistema sobre una base "real" | Ver el sistema sobre una base "real" |

> ✅ **El resultado es el mismo con las tres.** Los 12 escenarios de aceptación y las pruebas
> de concurrencia de esta guía se ejecutaron contra H2, **MySQL 8.4** y **PostgreSQL 16**, con
> el mismo código. Podés seguir toda la guía con H2 y, cuando quieras, repetir las pruebas
> cambiando de perfil.

**`ddl-auto`, ¿por qué `create` en MySQL y PostgreSQL?** Con `create`, Hibernate **borra y
vuelve a crear las tablas en cada arranque**. Lo hacemos porque los datos iniciales
(`data.sql`, Fase 2) se insertan en cada arranque: sobre una base que conserva datos
duplicarían filas y fallarían por las restricciones `unique`. En H2 no importa, porque la
base nace vacía cada vez. En la [Fase 2](02-fase-2-modelo-de-datos.md) verás cómo conservar
los datos entre ejecuciones si lo necesitás.

**Los parámetros de conexión son configurables.** En los perfiles `mysql` y `postgres` la
conexión se arma con propiedades propias (`coworkhub.db.host`, `.port`, `.nombre`,
`.usuario`, `.contrasena`) que podés cambiar **sin editar el archivo**, con una variable de
entorno o un argumento. Por ejemplo, si tu puerto 5432 ya está ocupado por otro PostgreSQL:

- variable de entorno: `COWORKHUB_DB_PORT=5433`
- argumento: `--coworkhub.db.port=5433`

Los valores por defecto (base, usuario y contraseña `coworkhub`) coinciden con el
`docker-compose.yml` del paso 1.7. Son credenciales **solo para desarrollo**: en un
entorno real la contraseña nunca va en el archivo.

Los archivos de cada perfil:

**📄 `src/main/resources/application-h2.properties`**

```properties
# Base de datos H2 en memoria (opción por defecto). No requiere instalar nada.
spring.datasource.url=jdbc:h2:mem:coworkhub;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Hibernate crea las tablas al arrancar. La base nace vacía en cada ejecución.
spring.jpa.hibernate.ddl-auto=update
```

**📄 `src/main/resources/application-mysql.properties`**

```properties
# MySQL 8 (perfil "mysql"). Se activa con --spring.profiles.active=mysql
# Los valores por defecto coinciden con docker-compose.yml. Cualquiera se puede cambiar con
# una variable de entorno, por ejemplo COWORKHUB_DB_PORT=3307.
# ⚠️ Solo para desarrollo: en un entorno real la contraseña no va en el archivo.
coworkhub.db.host=localhost
coworkhub.db.port=3306
coworkhub.db.nombre=coworkhub
coworkhub.db.usuario=coworkhub
coworkhub.db.contrasena=coworkhub

spring.datasource.url=jdbc:mysql://${coworkhub.db.host}:${coworkhub.db.port}/${coworkhub.db.nombre}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=${coworkhub.db.usuario}
spring.datasource.password=${coworkhub.db.contrasena}

# "create" borra y vuelve a crear las tablas en CADA arranque, para que data.sql no duplique datos.
# Para conservar los datos entre ejecuciones, ver la Fase 2 (paso 2.4).
spring.jpa.hibernate.ddl-auto=create

# Al usar "create", en el primer arranque Hibernate intenta borrar tablas que todavía no existen y
# lo avisa con mensajes largos pero inofensivos. Los ocultamos para no confundir.
logging.level.org.hibernate.tool.schema.internal.ExceptionHandlerLoggedImpl=ERROR
logging.level.org.hibernate.engine.jdbc.spi.SqlExceptionHelper=ERROR
```

**📄 `src/main/resources/application-postgres.properties`**

```properties
# PostgreSQL (perfil "postgres"). Se activa con --spring.profiles.active=postgres
# Los valores por defecto coinciden con docker-compose.yml. Cualquiera se puede cambiar con
# una variable de entorno, por ejemplo COWORKHUB_DB_PORT=5433.
# ⚠️ Solo para desarrollo: en un entorno real la contraseña no va en el archivo.
coworkhub.db.host=localhost
coworkhub.db.port=5432
coworkhub.db.nombre=coworkhub
coworkhub.db.usuario=coworkhub
coworkhub.db.contrasena=coworkhub

spring.datasource.url=jdbc:postgresql://${coworkhub.db.host}:${coworkhub.db.port}/${coworkhub.db.nombre}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=${coworkhub.db.usuario}
spring.datasource.password=${coworkhub.db.contrasena}

# "create" borra y vuelve a crear las tablas en CADA arranque, para que data.sql no duplique datos.
# Para conservar los datos entre ejecuciones, ver la Fase 2 (paso 2.4).
spring.jpa.hibernate.ddl-auto=create

# Al usar "create", en el primer arranque Hibernate intenta borrar tablas que todavía no existen y
# lo avisa con mensajes largos pero inofensivos. Los ocultamos para no confundir.
logging.level.org.hibernate.tool.schema.internal.ExceptionHandlerLoggedImpl=ERROR
logging.level.org.hibernate.engine.jdbc.spi.SqlExceptionHelper=ERROR
```

Las últimas líneas de los perfiles `mysql` y `postgres` (`logging.level...=ERROR`) ocultan
un aviso largo pero inofensivo: en el primer arranque, `create` intenta borrar tablas que
todavía no existen. Los **errores reales** (por ejemplo, una contraseña incorrecta) se
siguen mostrando.

#### Cómo activar cada perfil

| Cómo ejecutás la aplicación | Cómo elegir el perfil |
|---|---|
| `mvn spring-boot:run` | `mvn spring-boot:run -Dspring-boot.run.profiles=mysql` |
| `java -jar target/coworkhub-0.0.1-SNAPSHOT.jar` | `java -jar target/coworkhub-0.0.1-SNAPSHOT.jar --spring.profiles.active=mysql` |
| Cualquiera (variable de entorno) | `SPRING_PROFILES_ACTIVE=mysql` (en PowerShell: `$env:SPRING_PROFILES_ACTIVE="mysql"`) |
| Desde tu IDE | En la configuración de ejecución, *Active profiles* = `mysql` (o `-Dspring.profiles.active=mysql` en las VM options) |
| De forma permanente | Cambiá `spring.profiles.active=h2` en `application.properties` |

Para pasar además parámetros de conexión con Maven:
`mvn spring-boot:run -Dspring-boot.run.profiles=postgres -Dspring-boot.run.arguments="--coworkhub.db.port=5433"`.

### Paso 1.7 — (Opcional) Levantar MySQL o PostgreSQL

**Si vas a usar H2, saltá este paso.**

#### Opción A — Con Docker Compose (recomendada)

El `docker-compose.yml` define un MySQL 8.4 y un PostgreSQL 16, cada uno con la base
`coworkhub` y el usuario `coworkhub` ya creados. Cada servicio pertenece a un *perfil de
Compose* (`mysql` o `postgres`), así que **levantás solo el que usás**.

**📄 `docker-compose.yml`**

```yaml
# Bases de datos opcionales para desarrollo. Levantá solo la que uses:
#   docker compose --profile mysql up -d
#   docker compose --profile postgres up -d
# Para detenerlas:  docker compose --profile mysql down   (agregá -v para borrar sus datos)
# Si el puerto ya está ocupado, cambialo:  POSTGRES_PORT=5433 docker compose --profile postgres up -d
# (y arrancá la aplicación con COWORKHUB_DB_PORT=5433).
services:
  mysql:
    image: mysql:8.4
    container_name: coworkhub-mysql
    profiles: ["mysql"]
    environment:
      MYSQL_DATABASE: coworkhub
      MYSQL_USER: coworkhub
      MYSQL_PASSWORD: coworkhub
      MYSQL_ROOT_PASSWORD: root
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    ports:
      - "${MYSQL_PORT:-3306}:3306"
    volumes:
      - coworkhub-mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-proot"]
      interval: 5s
      timeout: 5s
      retries: 20

  postgres:
    image: postgres:16
    container_name: coworkhub-postgres
    profiles: ["postgres"]
    environment:
      POSTGRES_DB: coworkhub
      POSTGRES_USER: coworkhub
      POSTGRES_PASSWORD: coworkhub
    ports:
      - "${POSTGRES_PORT:-5432}:5432"
    volumes:
      - coworkhub-postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U coworkhub -d coworkhub"]
      interval: 5s
      timeout: 5s
      retries: 20

volumes:
  coworkhub-mysql-data:
  coworkhub-postgres-data:
```

```bash
# MySQL
docker compose --profile mysql up -d
# PostgreSQL
docker compose --profile postgres up -d

# Ver el estado (esperá a que diga "healthy")
docker compose ps

# Detener (los datos del contenedor se conservan)
docker compose --profile mysql down
# Detener y borrar también los datos
docker compose --profile mysql down -v
```

(Con instalaciones antiguas de Docker el comando es `docker-compose` con guion.)

Si el puerto ya está ocupado —por ejemplo, ya tenés un PostgreSQL en el 5432—, cambiá el
puerto del **contenedor** y avisale a la aplicación:

```bash
POSTGRES_PORT=5433 docker compose --profile postgres up -d
COWORKHUB_DB_PORT=5433 mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

#### Opción B — Con una instalación local

Instalá MySQL 8 o PostgreSQL 14+ y creá la base y el usuario con los mismos nombres que
esperan los perfiles. Conectate como administrador y ejecutá:

**MySQL** (`mysql -u root -p`):

```sql
CREATE DATABASE coworkhub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'coworkhub'@'%' IDENTIFIED BY 'coworkhub';
GRANT ALL PRIVILEGES ON coworkhub.* TO 'coworkhub'@'%';
```

**PostgreSQL** (`psql -U postgres`):

```sql
CREATE USER coworkhub WITH PASSWORD 'coworkhub';
CREATE DATABASE coworkhub OWNER coworkhub;
```

Si preferís otros nombres o contraseña, cambiá `coworkhub.db.nombre`, `.usuario` y
`.contrasena` (por variable de entorno o en el archivo del perfil). El juego de caracteres
`utf8mb4` en MySQL es importante: sin él, letras como `á` o `ñ` podrían guardarse mal.

#### Cómo mirar la base con un cliente

Cualquier cliente sirve (DBeaver, DataGrip, la extensión de tu IDE…). Desde la terminal,
con los contenedores de Compose:

```bash
# MySQL (--default-character-set evita que se vean mal los acentos en la terminal)
docker exec -it coworkhub-mysql mysql --default-character-set=utf8mb4 -ucoworkhub -pcoworkhub coworkhub

# PostgreSQL
docker exec -it coworkhub-postgres psql -U coworkhub coworkhub
```

### Paso 1.8 — Beans de configuración (Módulo 02)

Un *bean* es un objeto que Spring crea y administra, y que puede inyectar en otras clases.
Hasta ahora usaste `@Component`, `@Service`, etc. sobre **tus** clases. Cuando necesitás
un bean de una clase que **no es tuya** (como `Clock` o `BCryptPasswordEncoder`), lo
declarás en una clase `@Configuration` con un método `@Bean`.

- **`Clock reloj(...)`**: en vez de llamar a `LocalDateTime.now()` en cualquier parte
  (imposible de controlar en pruebas), el código pedirá un `Clock` y usará
  `LocalDateTime.now(reloj)`. Si la propiedad `coworkhub.reloj.fijo` tiene un valor
  (por ejemplo `2030-06-03T08:00:00`), el reloj queda **congelado** en ese instante;
  así los escenarios de "30 h", "5 h" y "1 h de anticipación" son repetibles.
  `@Value("${coworkhub.reloj.fijo:}")` lee la propiedad, con vacío como valor por defecto.
- **`PasswordEncoder passwordEncoder()`**: BCrypt, para no guardar contraseñas en texto plano.

**📄 `src/main/java/com/coworkhub/config/ConfiguracionBeans.java`**

```java
package com.coworkhub.config;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ConfiguracionBeans {

    // Reloj inyectable: si coworkhub.reloj.fijo tiene valor, "ahora" queda congelado
    // en ese instante (útil para probar reglas que dependen de la hora).
    @Bean
    public Clock reloj(@Value("${coworkhub.reloj.fijo:}") String instanteFijo) {
        ZoneId zona = ZoneId.systemDefault();
        if (instanteFijo.isBlank()) {
            return Clock.system(zona);
        }
        return Clock.fixed(LocalDateTime.parse(instanteFijo).atZone(zona).toInstant(), zona);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Paso 1.9 — `.gitignore` y primer commit

**📄 `.gitignore`**

```
target/
```

```bash
git init
git add .
git commit -m "fase 1: proyecto base"
```

## ✅ Checkpoint 1 — el proyecto arranca

Ejecutá desde la carpeta del proyecto:

```bash
mvn spring-boot:run
```

Debés ver, entre otras, estas líneas (los tiempos pueden variar):

```text
The following 1 profile is active: "h2"
Started Main in 3.4 seconds (process running for 3.8)
```

La primera confirma **qué base de datos estás usando**. Detenelo con `Ctrl + C`.

**Si elegiste MySQL o PostgreSQL** (paso 1.7), arrancá con su perfil, por ejemplo
`mvn spring-boot:run -Dspring-boot.run.profiles=mysql`, y además verás la conexión al motor:

```text
The following 1 profile is active: "mysql"
HikariPool-1 - Starting...
HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@...
HikariPool-1 - Start completed.
Started Main in 7.1 seconds (process running for 7.4)
```

Como todavía no hay entidades, no se crea ninguna tabla; lo que importa es que la
conexión se establezca. (En PostgreSQL, `ConnectionImpl` aparece como
`org.postgresql.jdbc.PgConnection`.)

| Si ves… | Causa probable | Solución |
|---|---|---|
| `Port 8080 was already in use` | Otro programa usa el puerto 8080 | Agregá `server.port=8081` a `application.properties`, o cerrá el otro programa |
| `Failed to determine a suitable driver class` | Falta la dependencia de la base, o el perfil `h2` no está activo | Revisá el `pom.xml` y `spring.profiles.active` |
| `release version 17 not supported` | Tu JDK es anterior a 17 | Instalá JDK 17 o superior |
| `Communications link failure` (MySQL) o `Connection to localhost:5432 refused` (PostgreSQL) | La base no está levantada, o el puerto es otro | `docker compose ps`; revisá el puerto (`COWORKHUB_DB_PORT`) |
| `Access denied for user 'coworkhub'@...` (MySQL) o `password authentication failed for user "coworkhub"` (PostgreSQL) | Usuario o contraseña distintos a los que creó la base | Revisá `coworkhub.db.usuario` y `.contrasena` |
| `Access denied for user ... to database 'x'` (MySQL) o `database "x" does not exist` (PostgreSQL) | La base no existe, o el usuario no tiene permisos sobre ella | Creala y otorgá permisos (paso 1.7, opción B), o revisá `coworkhub.db.nombre` |
| MySQL: `Public Key Retrieval is not allowed` | Servidor MySQL sin SSL con autenticación `caching_sha2_password` | Agregá `?allowPublicKeyRetrieval=true` al final de `spring.datasource.url` en el perfil |
| Se conecta a una base distinta de la que querías | El perfil activo no es el esperado | Fijate en la línea `The following 1 profile is active` |

**Siguiente →** [Fase 2 — Modelo de datos](02-fase-2-modelo-de-datos.md)
