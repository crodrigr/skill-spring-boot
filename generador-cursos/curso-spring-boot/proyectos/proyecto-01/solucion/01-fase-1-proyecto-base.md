# 🧱 Fase 1 — Proyecto base, beans y configuración

**Navegación**: [Índice](README.md) · ← [Fase 0 — Análisis](00-analisis.md) · Siguiente → [Fase 2 — Modelo de datos](02-fase-2-modelo-de-datos.md)

## 🎯 Qué vas a lograr

Un proyecto Spring Boot que **arranca** contra una base H2 en memoria, con la estructura
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
| Dependencies | **Spring Web**, **Spring Data JPA**, **H2 Database** |

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
| `h2` (scope `runtime`) | Base de datos en memoria; solo se necesita al ejecutar, no al compilar |
| `spring-security-crypto` | **Solo** el codificador de contraseñas BCrypt |

> ⚠️ **¿Por qué `spring-security-crypto` y no `spring-boot-starter-security`?** El *starter*
> completo activa la seguridad de inmediato: todos los endpoints exigirían autenticación
> y no podrías probar las fases 3 a 5. Como necesitamos BCrypt desde la Fase 2 (para
> guardar contraseñas de usuarios semilla), usamos solo esa pieza. En la
> [Fase 6](06-fase-6-seguridad-jwt.md) la reemplazaremos por el *starter* completo.

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
├── 📁 config                     → beans y cargadores de datos (transversal)
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

### Paso 1.5 — `application.properties`

Línea por línea:

- `spring.application.name`: nombre de la aplicación (aparece en los logs).
- `spring.datasource.*`: conexión a H2 **en memoria** (`jdbc:h2:mem:coworkhub`). Con
  `DB_CLOSE_DELAY=-1` la base no se cierra mientras la aplicación siga viva.
- `spring.jpa.hibernate.ddl-auto=update`: Hibernate crea o ajusta las tablas a partir de
  tus entidades al arrancar.
- `spring.jpa.open-in-view=true`: mantiene abierta la sesión de JPA durante toda la
  solicitud web. Lo declaramos explícitamente porque nuestras respuestas serializan
  colecciones `LAZY` (por ejemplo, los equipamientos de una sala) *después* de que el
  servicio terminó; sin esta opción fallarían con `LazyInitializationException`.
- `coworkhub.reloj.fijo`: propiedad **propia** (el prefijo `coworkhub.` es nuestro).
  Vacía = hora real. La usará el bean `Clock` del paso siguiente.

**📄 `src/main/resources/application.properties`**

```properties
spring.application.name=coworkhub

# Base de datos H2 en memoria
spring.datasource.url=jdbc:h2:mem:coworkhub;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=true

# Reloj de la aplicación. Vacío = hora real. Ejemplo para pruebas:
# coworkhub.reloj.fijo=2030-06-03T08:00:00
coworkhub.reloj.fijo=
```

### Paso 1.6 — Beans de configuración (Módulo 02)

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

### Paso 1.7 — `.gitignore` y primer commit

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

Debés ver, entre otras, una línea como esta (el tiempo puede variar):

```text
Started Main in 3.4 seconds (process running for 3.8)
```

Detenelo con `Ctrl + C`.

| Si ves… | Causa probable | Solución |
|---|---|---|
| `Port 8080 was already in use` | Otro programa usa el puerto 8080 | Agregá `server.port=8081` a `application.properties`, o cerrá el otro programa |
| `Failed to determine a suitable driver class` | Falta la dependencia `h2` | Revisá el `pom.xml` |
| `release version 17 not supported` | Tu JDK es anterior a 17 | Instalá JDK 17 o superior |

**Siguiente →** [Fase 2 — Modelo de datos](02-fase-2-modelo-de-datos.md)
