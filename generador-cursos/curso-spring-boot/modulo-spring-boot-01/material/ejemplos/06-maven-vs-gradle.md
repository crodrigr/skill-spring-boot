# 💡 Ejemplo 06 — Maven vs Gradle

## 🏥 Caso de estudio

Configuración de build para un proyecto `medisalud-api`, expresada primero con
Maven y luego con Gradle, con las mismas dependencias.

## 💻 Código — `pom.xml` (Maven)

```xml
<project>
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.medisalud</groupId>
    <artifactId>medisalud-api</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

## 💻 Código — `build.gradle` (Gradle, DSL de Groovy)

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.0'
}

group = 'com.medisalud'
version = '1.0.0'

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

## 💻 Código — la aplicación que ambos construyen (`App.java`)

Ni `pom.xml` ni `build.gradle` son código Java: son configuración de build. Lo
que realmente se compila y ejecuta es esta clase, idéntica sin importar cuál de
las dos herramientas se use:

```java
package com.medisalud;

public class App {
    public static void main(String[] args) {
        System.out.println("MediSalud API arrancando...");
        System.out.println("Construida con éxito, sin importar si el build fue con Maven o con Gradle.");
    }
}
```

```text
# Con Maven:
$ mvn compile exec:java -Dexec.mainClass=com.medisalud.App

# Con Gradle (agregando el plugin 'application' y mainClass en build.gradle):
$ ./gradlew run
```

## 🔍 Análisis comparado

| Elemento | Maven (`pom.xml`) | Gradle (`build.gradle`) |
|---|---|---|
| Formato | XML declarativo | DSL de Groovy o Kotlin |
| Identidad del proyecto | `groupId` / `artifactId` / `version` | `group` / nombre de carpeta / `version` |
| Dependencias | `<dependency>` dentro de `<dependencies>` | una línea por dependencia (`implementation`, `testImplementation`, …) |
| Ciclo de vida de build | Fases fijas y en orden (`validate`, `compile`, `test`, `package`, …) | Tareas configurables y con caché incremental, orden definido por dependencias entre tareas |
| Extensibilidad | *Plugins* declarados en `<build><plugins>` | *Plugins* declarados en el bloque `plugins { }`, con un DSL más flexible |

## 🧭 Explicación paso a paso

1. Ambos archivos declaran **lo mismo**: identidad del proyecto (`medisalud-api`,
   versión `1.0.0`) y dos dependencias, una de aplicación y otra solo para pruebas.
2. Maven usa **XML**: verboso pero uniforme entre proyectos; todo Maven se ve
   igual.
3. Gradle usa un **DSL de programación** (Groovy o Kotlin): más compacto y más
   flexible para lógica de build personalizada, a costa de que dos proyectos Gradle
   puedan organizarse de forma algo distinta entre sí.
4. `scope test` en Maven equivale a usar el prefijo `testImplementation` en Gradle:
   ambos significan "esta dependencia solo existe para compilar y ejecutar pruebas,
   no para el `.jar` final".
5. Ninguna de las dos herramientas cambia los conceptos de Spring Boot: ambas
   terminan produciendo el mismo artefacto ejecutable con las mismas dependencias.

## ✅ Resultado esperado

Al ejecutar `App.main(...)` — sin importar si el proyecto se construyó con Maven
o con Gradle, el resultado impreso en consola es exactamente el mismo:

```text
MediSalud API arrancando...
Construida con éxito, sin importar si el build fue con Maven o con Gradle.
```

La elección entre Maven y Gradle es una decisión de herramienta de equipo, no de
diseño de la aplicación: ambos terminan compilando y ejecutando el mismo
`App.java`, con las mismas dependencias declaradas en `pom.xml` o
`build.gradle`.
