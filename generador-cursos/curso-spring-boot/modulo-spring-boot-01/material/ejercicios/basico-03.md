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

1. Identificá el `groupId`, el `artifactId`, la `version` y el `packaging`.
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

- No es necesario ejecutar `mvn` ni `gradle`; el ejercicio se resuelve leyendo y
  describiendo el archivo.

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-5
