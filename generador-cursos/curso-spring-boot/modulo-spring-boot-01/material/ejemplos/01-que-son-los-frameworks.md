# 💡 Ejemplo 01 — ¿Qué son los frameworks?

## 🌍 Contexto

Antes de escribir la primera línea de Spring Boot conviene entender **qué tipo de
herramienta es**: un framework. Un framework es un entorno de desarrollo que
proporciona una estructura predefinida para construir aplicaciones profesionales,
de forma que resulten escalables, dinámicas y mantenibles. Incluye librerías,
herramientas y utilidades pensadas para reducir el esfuerzo repetitivo en
proyectos grandes, como sería construir desde cero el sistema de citas de
MediSalud o el de préstamos de la Biblioteca Universitaria.

El objetivo principal de un framework es facilitar el **desarrollo ágil de
software**: construir aplicaciones de forma más eficiente y en menos tiempo. Esto
es especialmente valioso en aplicaciones web complejas, donde hay que gestionar
grandes volúmenes de datos e integraciones entre módulos (por ejemplo, citas,
historias clínicas y facturación dentro de un mismo sistema MediSalud).

## 🧠 Concepto: las cinco características de un framework

| Característica | Qué significa |
|---|---|
| **Escalabilidad** | Permite expandir y adaptar el proyecto a necesidades nuevas del negocio sin afectar su estructura principal. |
| **Inversión de Control (IoC)** | Desacopla la gestión de dependencias, delegándola a un contenedor del framework; mejora la reutilización de código y la modularidad. |
| **Modelo Vista-Controlador (MVC)** | Estandariza la organización del código, separando responsabilidades (qué se muestra, qué se procesa, qué datos existen) y facilitando el mantenimiento. |
| **Minimizar código repetitivo** | Gracias a su estructura modular y a componentes reutilizables, reduce la necesidad de escribir el mismo código una y otra vez. |
| **Bases generales auto-gestionadas** | Maneja de forma integrada aspectos transversales (seguridad, acceso a datos, presentación de vistas), reduciendo la complejidad del desarrollo. |

## 📖 Historia, en breve

Los frameworks surgieron como respuesta a un problema recurrente: cada equipo que
construía una aplicación empresarial terminaba resolviendo, una y otra vez, los
mismos problemas de base (cómo organizar el código, cómo conectar con una base de
datos, cómo exponer una página web), muchas veces de forma distinta e
incompatible entre proyectos. Los frameworks empaquetan esas soluciones ya
probadas, para que cada equipo parta de una base común en vez de reinventarla. En
el mundo Java, esta evolución llevó primero a frameworks como Struts, luego a
Spring (2003), y más adelante a Spring Boot (2014), que se estudia en detalle más
adelante en este módulo.

## 💻 Ejemplo aplicado

Sin un framework, construir el endpoint web de MediSalud que lista las citas del
día implicaría escribir a mano: el servidor que escucha peticiones HTTP, el
código que interpreta la URL solicitada, la conversión de los datos a un formato
de respuesta (por ejemplo JSON), y el manejo de errores. Con un framework como
Spring Boot (que se detalla en el Ejemplo 07), gran parte de eso ya está resuelto:
el desarrollador solo declara **qué** debe pasar para una ruta dada.

```java
// Con framework (Spring Boot): el desarrollador solo declara el "qué"
@RestController
public class CitasController {

    @GetMapping("/citas/hoy")
    public List<Cita> citasDeHoy() {
        return servicioCitas.obtenerCitasDeHoy(); // el framework hace el resto
    }
}
```

## 🧭 Explicación paso a paso

1. El desarrollador no escribe el código que abre un socket de red, interpreta el
   protocolo HTTP ni convierte objetos Java a JSON: **el framework ya lo resuelve**.
2. El framework impone una estructura (una clase anotada como controlador, un
   método por ruta) a cambio de todo ese trabajo resuelto.
3. Esta es la esencia de un framework: no es solo código reutilizable, es una
   **estructura completa** dentro de la cual el desarrollador escribe su lógica de
   negocio.

## ✅ Resultado esperado

El endpoint `/citas/hoy` responde con la lista de citas del día en formato JSON,
sin que el desarrollador haya escrito ninguna línea de manejo de sockets, HTTP ni
serialización.
