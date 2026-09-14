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

## 💻 Ejemplo aplicado

```java
// Sin ninguna librería: validar un ISBN a mano, línea por línea
public boolean esIsbnValido(String isbn) {
    String limpio = isbn.replace("-", "");
    if (limpio.length() != 13) return false;
    for (char c : limpio.toCharArray()) {
        if (!Character.isDigit(c)) return false;
    }
    return true; // simplificado: un caso real también verifica el dígito de control
}

// Con una librería (por ejemplo, una utilidad ya probada de validación de formatos)
boolean valido = ValidadorIsbn.esValido("978-3-16-148410-0");
```

## 🧭 Explicación paso a paso

1. La primera versión resuelve el problema, pero el desarrollador debe mantener y
   probar esa lógica de validación en cada proyecto donde la necesite.
2. La versión con librería delega esa responsabilidad puntual a código ya escrito
   y probado por otros, sin exigir ninguna estructura adicional al resto del
   programa: solo se llama al método cuando se necesita.
3. A diferencia de un framework, la librería **no decide** cómo se organiza la
   aplicación ni cuándo se ejecuta el código del desarrollador: es el
   desarrollador quien decide cuándo llamar a la librería.

## ✅ Resultado esperado

```text
esIsbnValido("978-3-16-148410-0") → true
esIsbnValido("123") → false
```
