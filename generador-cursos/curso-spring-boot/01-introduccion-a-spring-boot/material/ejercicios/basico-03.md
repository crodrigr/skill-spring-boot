# 🟢 Básico 03 — Leer un `pom.xml` y traducirlo conceptualmente a Gradle

## 🧩 Problema

Te entregan el siguiente `pom.xml` de un proyecto `biblioteca-api` y te piden
identificar sus partes y describir (sin escribirlo completo) cómo se vería la
misma información en `build.gradle`.

## 💻 Código o contexto de partida

```xml
<project>
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.biblioteca</groupId>
    <artifactId>biblioteca-api</artifactId>
    <version>0.1.0</version>
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

Completá este programa transcribiendo los valores que leas del `pom.xml` de
arriba, y ejecutalo para confirmar que los identificaste bien:

```java
public class Main {
    public static void main(String[] args) {
        String groupId = null;         // TODO: completar
        String artifactId = null;      // TODO: completar
        String version = null;         // TODO: completar
        String packaging = null;       // TODO: completar
        int cantidadDependencias = 0;  // TODO: completar
        String dependenciaDePrueba = null; // TODO: completar (artifactId de la dependencia con scope test)

        System.out.println(groupId + ":" + artifactId + ":" + version + ":" + packaging);
        System.out.println("Cantidad de dependencias: " + cantidadDependencias);
        System.out.println("Dependencia de prueba: " + dependenciaDePrueba);
    }
}
```

Además:

1. Identificá el `groupId`, el `artifactId`, la `version` y el `packaging`
   (ya completados arriba en `Main`).
2. Indicá cuántas dependencias tiene y cuál de ellas es solo para pruebas, y con
   qué la reconociste.
3. Describí (en prosa o con una línea de ejemplo) cómo se expresaría cada
   dependencia en `build.gradle`, usando `implementation` o `testImplementation`
   según corresponda.

## 📏 Criterios de evaluación de la solución

- Identifica correctamente `groupId=com.biblioteca`, `artifactId=biblioteca-api`,
  `version=0.1.0`, `packaging=jar`.
- Reconoce que hay 2 dependencias y que `spring-boot-starter-test` es la de prueba,
  por el `<scope>test</scope>`.
- La traducción a Gradle usa `implementation` para la dependencia de aplicación y
  `testImplementation` para la de prueba.

## 🚧 Restricciones

- No es necesario ejecutar `mvn` ni `gradle`; solo el pequeño programa `Main`
  (ejecutable con `java`), que sirve como autocorrección de la lectura del
  `pom.xml`.

## 📊 Dificultad

Básico

## ✅ Salida esperada al ejecutar `Main`

```text
com.biblioteca:biblioteca-api:0.1.0:jar
Cantidad de dependencias: 2
Dependencia de prueba: spring-boot-starter-test
```

## 🎓 Resultados de aprendizaje

RA-5
