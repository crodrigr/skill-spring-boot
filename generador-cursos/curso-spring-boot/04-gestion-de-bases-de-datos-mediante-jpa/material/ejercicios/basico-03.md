# 🟢 Básico 03 — Crear un proyecto Spring Boot con persistencia

## 🧩 Problema

Biblioteca Universitaria quiere empezar un proyecto nuevo desde cero para
experimentar con persistencia, sin reutilizar ninguno de los proyectos
anteriores del curso.

## 💻 Código o contexto de partida

Ninguno: este ejercicio empieza sin ningún proyecto previo.

1. Creá un proyecto Spring Boot nuevo con Spring Initializr (Maven, Java
   17, Spring Boot 3.x), agregando las dependencias `Spring Data JPA` y
   `H2 Database`.
2. Configurá `application.properties` para conectar el proyecto a una base
   de datos H2 en memoria llamada `biblioteca`, con
   `spring.jpa.hibernate.ddl-auto=update`.
3. Verificá que el proyecto arranca sin errores (con una clase principal
   `@SpringBootApplication` vacía, sin entidades todavía).

## 📏 Criterios de evaluación de la solución

- El `pom.xml` generado incluye `spring-boot-starter-data-jpa` y
  `com.h2database:h2`.
- `application.properties` conecta correctamente a H2 en memoria, con una
  URL distinta a la usada en los ejemplos de MediSalud (para reflejar el
  dominio de Biblioteca Universitaria).
- La aplicación arranca sin errores, confirmado por el log de consola.

## 🚧 Restricciones

- No se permite copiar ningún archivo de un proyecto anterior del curso
  (el objetivo es practicar la creación desde cero).

## 📊 Dificultad

Básico

## 🎓 Resultados de aprendizaje

RA-10, RA-11
