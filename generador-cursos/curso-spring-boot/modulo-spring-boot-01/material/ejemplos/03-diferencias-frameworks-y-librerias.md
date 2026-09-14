# 💡 Ejemplo 03 — Diferencias entre frameworks y librerías

## 🌍 Contexto

Spring Boot (un framework) y una librería de validación de ISBN (una librería,
Ejemplo 02) conviven en el mismo proyecto de Biblioteca Universitaria, pero
cumplen roles muy distintos. Reconocer esa diferencia evita un error común de
principiante: tratar cualquier dependencia agregada al proyecto como si fuera "lo
mismo".

## 🔍 Análisis comparado

| Criterio | Framework | Librería |
|---|---|---|
| **Quién controla el flujo** | El framework llama al código del desarrollador (Inversión de Control): el desarrollador rellena "huecos" dentro de una estructura ya definida. | El código del desarrollador llama a la librería, cuando y donde la necesita. |
| **Estructura impuesta** | Sí: define paquetes, ciclo de vida, convenciones de nombrado y organización del proyecto. | No: se integra libremente, sin imponer cómo se organiza el resto del programa. |
| **Alcance** | Suele abarcar la aplicación completa (web, datos, seguridad). | Suele resolver una necesidad puntual y acotada (validar un formato, convertir JSON, formatear una fecha). |
| **Reemplazo** | Cambiar de framework generalmente implica reescribir buena parte de la aplicación. | Cambiar de librería suele afectar solo el código que la invoca directamente. |
| **Ejemplo en este curso** | Spring Boot organiza toda la aplicación (controladores, servicios, repositorios). | Una librería de validación de ISBN solo resuelve esa validación puntual. |

## 🧠 La clave: quién llama a quién

Esta diferencia se resume en una idea, ya introducida como Inversión de Control en
el Ejemplo 01: con una librería, el desarrollador tiene el control ("yo llamo a la
librería cuando quiero"); con un framework, el control se invierte ("el framework
me llama a mí cuando corresponde: al recibir una petición HTTP, al arrancar la
aplicación, al inyectar una dependencia").

```java
// Librería: el desarrollador decide cuándo llamarla
boolean valido = ValidadorIsbn.esValido(isbn); // se llama explícitamente, donde se quiera

// Framework (Spring Boot): el framework decide cuándo llamar al código del desarrollador
@RestController
public class CatalogoController {
    @GetMapping("/catalogo/{isbn}")
    public Libro buscar(@PathVariable String isbn) {
        // este método lo invoca Spring Boot al recibir una petición GET,
        // no lo invoca directamente el desarrollador
        return servicioCatalogo.buscarPorIsbn(isbn);
    }
}
```

## 🧭 Explicación paso a paso

1. En el primer bloque, el desarrollador escribe `ValidadorIsbn.esValido(isbn)`
   en el punto exacto del código donde lo necesita: tiene el control.
2. En el segundo bloque, el desarrollador **nunca** escribe una llamada a
   `buscar(...)`: ese método existe para que Spring Boot lo invoque cuando llega
   una petición HTTP a `/catalogo/{isbn}`. El control quedó invertido.
3. Ninguna forma es "mejor" en abstracto: una librería es la herramienta correcta
   para una necesidad puntual y acotada; un framework es la herramienta correcta
   cuando se necesita una estructura completa y consistente para toda la
   aplicación.

## 📌 Idea clave

Spring Boot es un framework porque organiza la aplicación completa e invierte el
control; una librería de validación, de conversión de JSON o de manejo de fechas
sigue siendo una librería aunque se use **dentro** de un proyecto Spring Boot: la
distinción depende de su rol, no del proyecto en el que aparece.
