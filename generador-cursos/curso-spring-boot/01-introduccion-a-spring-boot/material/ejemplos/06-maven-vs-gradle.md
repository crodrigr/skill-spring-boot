# 💡 Ejemplo 06 — Maven vs Gradle

## 🌍 Contexto

Maven y Gradle son las dos herramientas de **build** más usadas para proyectos
Java/Spring Boot: descargan dependencias, compilan el código, ejecutan pruebas y
empaquetan la aplicación. Difieren en cómo se declara esa configuración (XML
declarativo vs. un DSL de programación), pero ninguna de las dos cambia los
conceptos de la aplicación en sí — eso es exactamente lo que este ejemplo pone a
prueba.

**Qué busca demostrar este ejemplo**: que un mismo proyecto (`medisalud-api`),
descrito con las mismas dependencias en `pom.xml` y en `build.gradle`, compila y
ejecuta **la misma clase Java** (`App.java`) con **la misma salida**; la
elección de herramienta no altera en nada el comportamiento de la aplicación.

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

## 🔬 Qué significa cada elemento

### `pom.xml` (Maven)

| Elemento | Qué es | En este ejemplo |
|---|---|---|
| `<project>` | Raíz del archivo: todo el `pom.xml` vive dentro de este elemento. | — |
| `<modelVersion>` | Versión del esquema del POM (*Project Object Model*). Siempre es `4.0.0`. | `4.0.0` |
| `<groupId>` | Organización o paquete base que identifica a quien publica el proyecto; suele ser un dominio invertido. | `com.medisalud` |
| `<artifactId>` | Nombre del proyecto; también es el nombre base del `.jar` generado. | `medisalud-api` |
| `<version>` | Versión del proyecto. Juntos, `groupId` + `artifactId` + `version` identifican de forma única al artefacto. | `1.0.0` |
| `<packaging>` | Tipo de artefacto que se produce: `jar`, `war`, `pom`… | `jar` |
| `<dependencies>` | Lista de todas las dependencias del proyecto. | — |
| `<dependency>` | Una dependencia, identificada con sus propios `groupId`, `artifactId` y, si hace falta, `version`. | `spring-boot-starter-web` y `spring-boot-starter-test` |
| `<scope>` | En qué fases se necesita la dependencia. Sin `scope`, vale `compile` (compilación, ejecución y `.jar` final). Con `test`, solo para compilar y ejecutar pruebas. | `test` en `spring-boot-starter-test` |

### `build.gradle` (Gradle)

| Elemento | Qué es | En este ejemplo |
|---|---|---|
| `plugins { }` | Bloque que activa *plugins*: cada uno agrega tareas y convenciones al build. | — |
| `id 'java'` | Plugin que sabe compilar código Java, ejecutar pruebas y generar el `.jar`. | `java` |
| `id 'org.springframework.boot' version '…'` | Plugin de Spring Boot: permite empaquetar la aplicación como `.jar` ejecutable. Aquí se fija su versión. | `3.3.0` |
| `group` | Equivale a `groupId` de Maven. | `com.medisalud` |
| `version` | Equivale a `<version>` de Maven. | `1.0.0` |
| `dependencies { }` | Bloque con las dependencias, una línea por cada una, con la forma `configuración 'grupo:artefacto'`. | — |
| `implementation` | Dependencia necesaria para compilar y ejecutar la aplicación; equivale al `scope` `compile` de Maven. | `spring-boot-starter-web` |
| `testImplementation` | Dependencia solo para pruebas; equivale a `<scope>test</scope>`. | `spring-boot-starter-test` |

En Gradle no hay un `artifactId` explícito: el nombre del proyecto sale del
nombre de la carpeta (o de `settings.gradle`).

> 📝 **Este ejemplo está simplificado.** Un proyecto real generado con Spring
> Initializr añade más piezas: en Maven, un `<parent>` (`spring-boot-starter-parent`)
> que fija las versiones de las dependencias de Spring, por eso aquí no se escribe
> `<version>` en los starters; en Gradle, un bloque `repositories { mavenCentral() }`
> que indica de dónde se descargan las dependencias, y el plugin
> `io.spring.dependency-management` que cumple el mismo papel de versiones.

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

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué formato usa Gradle para declarar la configuración de
build?

- **A.** XML declarativo.
- **B.** Un DSL de Groovy o Kotlin.
- **C.** JSON.
- **D.** YAML.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. XML declarativo es Maven; Gradle usa un DSL de
programación (Groovy o Kotlin).

</details>

**2. [Selección múltiple]** Seleccioná **todas** las afirmaciones correctas
sobre Maven y Gradle.

- **A.** Maven tiene un ciclo de vida de fases fijo (`validate`, `compile`,
  `test`, `package`, …).
- **B.** Gradle no puede construir proyectos Spring Boot.
- **C.** Ambos pueden compilar y ejecutar la misma clase `App.java`.
- **D.** Un `scope test` en Maven equivale a `testImplementation` en Gradle.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, C, D**. La B es falsa: Gradle es tan capaz como Maven
de construir un proyecto Spring Boot.

</details>

**3. [Abierta]** ¿Por qué la elección entre Maven y Gradle no cambia en nada el
comportamiento de `App.java`?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Porque ni `pom.xml` ni `build.gradle` son código Java: son
solo configuración de build (qué dependencias descargar, cómo compilar y
empaquetar). El código que realmente se ejecuta —`App.java`— es idéntico en
ambos casos; ninguna de las dos herramientas modifica ni reemplaza esa clase, solo
la compilan y la empaquetan de formas distintas.

</details>
