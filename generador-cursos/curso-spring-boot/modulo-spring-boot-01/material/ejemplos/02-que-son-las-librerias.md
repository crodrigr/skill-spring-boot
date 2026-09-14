# 💡 Ejemplo 02 — ¿Qué son las librerías?

## 🌍 Contexto

Una librería es un conjunto de archivos que contiene funciones y procedimientos
reutilizables, diseñados para resolver una necesidad puntual dentro de un
programa: desde operaciones matemáticas hasta el acceso a una base de datos
relacional. A diferencia de un framework, una librería **no impone una
estructura** de desarrollo: ofrece herramientas que el desarrollador integra
libremente, donde y cuando las necesita.

Por ejemplo, en Biblioteca Universitaria se podría necesitar validar que el
código de un libro tenga el formato correcto de ISBN. Para eso no hace falta un
framework completo: alcanza con una librería de validación que se invoca en el
punto exacto del código donde se necesita.

**Qué busca demostrar este ejemplo**: que una librería no exige ninguna
estructura nueva ni cambia el flujo del programa — el mismo `main` que ya existía
simplemente gana una línea de código que llama a la librería cuando lo decide,
en contraste con lo que se vio en el Ejemplo 01 con un framework.

## 🧠 Concepto: dos tipos de librerías

| Tipo | Cómo funciona | Consecuencia |
|---|---|---|
| **Librerías estáticas** (*static libraries*) | El código de la librería se copia dentro del ejecutable final en el momento de compilar. | El ejecutable resulta más grande, pero no depende de archivos externos en tiempo de ejecución. |
| **Librerías dinámicas** (*dynamic* o *shared libraries*) | La librería se carga en tiempo de **ejecución**, no en compilación. | Se optimiza el uso de memoria (varias aplicaciones pueden compartir la misma librería cargada) y se puede actualizar la librería sin recompilar todo el programa. |

En el ecosistema Java, una dependencia declarada en `pom.xml` o `build.gradle`
(por ejemplo, una librería de validación) se distribuye como un archivo `.jar` y
se agrega al *classpath* de la aplicación: conceptualmente se comporta como una
librería dinámica, cargada por la JVM al ejecutar el programa, en vez de quedar
copiada dentro del código fuente.

## 💻 Código completo

```java
public class ValidadorIsbnDemo {

    // Sin ninguna librería: validar un ISBN a mano, línea por línea
    static boolean esIsbnValidoManual(String isbn) {
        String limpio = isbn.replace("-", "");
        if (limpio.length() != 13) {
            return false;
        }
        for (char c : limpio.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true; // simplificado: un caso real también verifica el dígito de control
    }

    // "Librería" ya escrita y probada por otros: el desarrollador solo la invoca
    static class ValidadorIsbn {
        static boolean esValido(String isbn) {
            return esIsbnValidoManual(isbn); // misma lógica, ya empaquetada y reutilizable
        }
    }

    public static void main(String[] args) {
        System.out.println("Validación manual:");
        System.out.println(esIsbnValidoManual("978-3-16-148410-0"));
        System.out.println(esIsbnValidoManual("123"));

        System.out.println("Validación con la 'librería' ValidadorIsbn:");
        System.out.println(ValidadorIsbn.esValido("978-3-16-148410-0"));
        System.out.println(ValidadorIsbn.esValido("123"));
    }
}
```

## 🧭 Explicación paso a paso

1. `esIsbnValidoManual` resuelve el problema, pero si otro proyecto de la
   biblioteca necesita la misma validación, tendría que copiar y mantener esta
   misma lógica por separado.
2. `ValidadorIsbn.esValido(...)` representa cómo se vería usar una librería ya
   escrita y probada: el desarrollador la llama en el punto exacto donde la
   necesita (en `main`), sin que la librería le imponga ninguna estructura al
   resto del programa.
3. A diferencia de un framework, la librería **no decide** cuándo se ejecuta el
   código del desarrollador: es el propio `main` quien decide, línea por línea,
   cuándo llamar a `ValidadorIsbn.esValido(...)`.

## ✅ Resultado esperado

```text
Validación manual:
true
false
Validación con la 'librería' ValidadorIsbn:
true
false
```
