# 📘 Módulo 2 — Manejo de Dependencias y Java Beans

Spring Boot para Aplicaciones Empresariales

---

## 🎯 Objetivos del módulo

- Distinguir dependencias directas de transitivas, y sus ventajas/desventajas.
- Aplicar Inyección de Dependencias por constructor y por propiedades.
- Diseñar dependencias aplicando buenas prácticas.
- Resolver ambigüedades de inyección con `@Qualifier`.
- Entender qué es un Java Bean y su ciclo de vida completo.
- Usar `@Component` para registrar beans sin configuración adicional.

---

## 🗺️ Ruta del módulo

1. ¿Qué es una dependencia?
2. Inyección de Dependencias
3. Estructura básica de una dependencia y buenas prácticas
4. Implementación y resolución de una dependencia general
5. ¿Qué es un Java Bean?
6. Ciclo de vida de un bean
7. Uso de `@Component`
8. Taller guiado y evaluación

---

## 🧠 ¿Qué es una dependencia?

Un módulo (el **dependiente**) necesita a otro (la **dependencia**) para
funcionar.

```text
dependencia directa:    A usa a B
dependencia transitiva: A usa a B, B usa a C → A depende de C sin saberlo
```

---

## 🔍 Dependencia directa vs. transitiva (MediSalud)

```text
ControladorCitas → ServicioCitas → RepositorioPacientes
     directa           directa
ControladorCitas → RepositorioPacientes:  TRANSITIVA
```

---

## ✅ Ventajas de usar dependencias

- Reutilización de código
- Modularidad
- Especialización
- Facilidad de mantenimiento

---

## 🚧 Desventajas de usar dependencias

- Acoplamiento
- Complejidad
- Vulnerabilidades de seguridad
- Dependencia de terceros

---

## 🧠 Gestión de dependencias en Spring

El contenedor IoC crea los beans y resuelve sus dependencias —incluidas las
transitivas— automáticamente.

```text
Cuando hay más de una implementación candidata → @Qualifier decide cuál usar
```

---

## 🧠 Inyección de Dependencias: el problema

```java
private final RepositorioLibros r = new RepositorioLibrosEnMemoria();
```

Acoplada a una implementación concreta: imposible de reemplazar en un test.

---

## 💡 Inyección de Dependencias: la solución

```java
public ServicioPrestamosConstructor(RepositorioLibros repositorioLibros) {
    this.repositorioLibros = repositorioLibros;
}
```

La clase depende de una interfaz, no de una implementación.

---

## 🔍 Constructor vs. propiedades (*setter*)

| | Constructor | Setter |
|---|---|---|
| Obligatoriedad | Obligatoria | Puede ser opcional |
| `final` | Sí | No |
| Momento | Al crear el objeto | Después de crearlo |

---

## ✅ Beneficios de la Inyección de Dependencias

- Mejora la modularidad
- Reduce la complejidad
- Aumenta la flexibilidad
- Facilita las pruebas unitarias (mocks/stubs)

---

## 🧠 Estructura básica de una dependencia

```text
Dependiente  →  (a través de una abstracción)  →  Dependencia
```

Ninguna clase debería depender de una implementación concreta directamente.

---

## 🔍 Buenas prácticas de diseño de dependencias

- Depender de abstracciones, no de implementaciones concretas
- Minimizar las dependencias transitivas expuestas
- Evitar dependencias circulares

---

## 🚧 Dependencia circular: el anti-patrón

```text
ServicioA necesita a ServicioB
ServicioB necesita a ServicioA
→ ninguno de los dos se puede construir primero
```

Se resuelve reorganizando responsabilidades, no cambiando la forma de inyección.

---

## 🧠 Implementación de una dependencia general

Con una sola implementación de una interfaz, Spring resuelve sin ambigüedad.

```java
@Component
public class NotificadorSms implements Notificador { ... }
```

---

## 🚧 Dos implementaciones = ambigüedad

```text
No qualifying bean of type 'Notificador' available:
expected single matching bean but found 2: notificadorSms, notificadorEmail
```

---

## 💡 Resolución con `@Qualifier`

```java
@Component @Qualifier("sms")
public class NotificadorSms implements Notificador { ... }

public ServicioRecordatorios(@Qualifier("sms") Notificador notificador) { ... }
```

---

## 🧠 ¿Qué es un Java Bean?

Objeto administrado por Spring; en sentido clásico, sigue convenciones de
propiedades con *getter*/*setter*.

---

## 🔍 Características de un Java Bean

- Reutilizable
- Manipulable visualmente
- Serializable
- Con propiedades (getter/setter)
- Con capacidad de generar eventos
- Con capacidad de introspección

---

## 📌 Java Bean ≠ bean administrado por Spring

Una clase puede cumplir una de las dos convenciones, ambas, o ninguna.

---

## 🪜 Ciclo de vida de un bean (5 fases)

1. Instanciación
2. **Configuración** (inyección de dependencias)
3. Inicialización (`@PostConstruct`)
4. Uso
5. Destrucción (`@PreDestroy`)

---

## 🔍 Instanciación vs. configuración

Con inyección por constructor, ocurren casi al mismo tiempo — pero siguen
siendo fases lógicamente distintas.

---

## 🧠 `@Component`: la anotación base

```java
@Component
public class FormateadorFechaVencimiento { ... }
```

Registra el bean sin configuración adicional.

---

## 🔍 `@Component` vs. sus especializaciones

`@Service`, `@Repository`, `@Controller` registran el bean **igual**;
la diferencia es semántica: qué responsabilidad comunican.

---

## 🛠️ Actividad práctica

Taller 01: diagnosticar dependencias en Biblioteca Universitaria, resolver una
ambigüedad con `@Qualifier`, y documentar buenas prácticas de diseño.

---

## 📌 Resumen del módulo

- Toda dependencia es directa o transitiva; trae ventajas y desventajas reales.
- La Inyección de Dependencias desacopla una clase de una implementación concreta.
- Buenas prácticas: depender de abstracciones, minimizar transitivas expuestas, evitar circulares.
- `@Qualifier` resuelve la ambigüedad cuando hay más de una implementación candidata.
- Un Java Bean sigue convenciones de propiedades; un bean de Spring es administrado por el contenedor — no son lo mismo.
- El ciclo de vida de un bean tiene 5 fases: instanciación, configuración, inicialización, uso, destrucción.
- `@Component` es la anotación base; sus especializaciones solo agregan significado.

---

## 📝 Evaluación

Quiz 02 (12 ítems) + Taller 01 + ejercicios del módulo.
