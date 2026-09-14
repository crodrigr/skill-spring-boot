# 💡 Ejemplo 08 — Estructura general de un proyecto Spring Boot

## 🌍 Contexto

Antes de escribir la primera aplicación Spring Boot conviene reconocer cómo se
organizan sus archivos y carpetas. Spring Boot no obliga una única estructura,
pero sí existe una convención ampliamente adoptada que este curso sigue en todos
sus ejemplos, aplicada aquí a un proyecto `biblioteca-api`.

## 🌳 Árbol de carpetas

```text
biblioteca-api/
├── pom.xml                                  (o build.gradle, ver Ejemplo 06)
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/biblioteca/api/
    │   │       ├── BibliotecaApiApplication.java   (clase principal)
    │   │       ├── controller/
    │   │       │   └── CatalogoController.java
    │   │       ├── service/
    │   │       │   └── ServicioPrestamos.java
    │   │       ├── repository/
    │   │       │   └── RepositorioLibros.java
    │   │       └── model/
    │   │           └── Libro.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/
            └── com/biblioteca/api/
                └── ServicioPrestamosTest.java
```

## 🔍 Qué contiene cada parte

| Ruta | Propósito |
|---|---|
| `pom.xml` / `build.gradle` | Declara las dependencias del proyecto (starters) y cómo construirlo (Ejemplo 06). |
| `BibliotecaApiApplication.java` | Clase principal, anotada `@SpringBootApplication`; contiene el método `main` que arranca la aplicación. |
| `controller/` | Clases que reciben peticiones HTTP y devuelven una respuesta (`@RestController`); no contienen lógica de negocio. |
| `service/` | Clases con la lógica de negocio (por ejemplo, las reglas para prestar un libro); es lo que en este módulo se vio como `ServicioPrestamos`. |
| `repository/` | Clases o interfaces responsables de acceder a los datos (en este módulo, en memoria; en el módulo de persistencia, contra una base de datos real). |
| `model/` | Clases que representan las entidades del dominio (`Libro`, `Usuario`), como las vistas en POO aplicada (Ejemplo 04). |
| `src/main/resources/application.properties` | Configuración de la aplicación (por ejemplo, `server.port`), vista en el Ejemplo 07. |
| `src/test/java/...` | Pruebas automatizadas, organizadas con el mismo paquete que el código que prueban. |

## 🧭 Explicación paso a paso

1. La separación en `controller/`, `service/` y `repository/` no es exigida por el
   compilador de Java: es una **convención de arquitectura por capas**, adoptada
   por la comunidad Spring, que separa "cómo llega la petición" (controller), "qué
   regla de negocio se aplica" (service) y "de dónde vienen los datos"
   (repository). Este curso profundiza esta arquitectura en un módulo posterior.
2. `BibliotecaApiApplication.java` suele ubicarse en el paquete raíz
   (`com.biblioteca.api`) porque, por convención, Spring Boot escanea ese paquete
   y todos sus subpaquetes en busca de componentes (`@Component`, `@Service`,
   `@RestController`, etc.) para registrarlos como beans.
3. `src/main/resources/application.properties` es el lugar donde Spring Boot
   busca configuración externa; si no existe, la aplicación igual arranca con
   valores por defecto, coherente con la autoconfiguración vista en el Ejemplo 07.
4. La carpeta `src/test/` es paralela a `src/main/`, con la misma estructura de
   paquetes: esto permite que una prueba de `ServicioPrestamos` acceda a sus
   miembros `package-private` si los tuviera, y facilita ubicar qué prueba
   corresponde a qué clase.

## ✅ Resultado esperado

Al ejecutar `BibliotecaApiApplication`, Spring Boot escanea `com.biblioteca.api` y
sus subpaquetes, registra `CatalogoController`, `ServicioPrestamos` y
`RepositorioLibros` como beans, lee `application.properties`, y deja la aplicación
escuchando en el puerto configurado (por defecto, `8080`).

## 📌 Idea clave

Esta estructura por capas es la que los ejemplos y ejercicios de los módulos
siguientes del curso asumen como punto de partida: controlador → servicio →
repositorio → modelo, la misma separación de responsabilidades que promueve el
patrón MVC mencionado en el Ejemplo 01.
