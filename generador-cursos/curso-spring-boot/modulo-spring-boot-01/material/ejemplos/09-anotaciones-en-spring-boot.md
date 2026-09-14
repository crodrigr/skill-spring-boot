# 💡 Ejemplo 09 — Anotaciones en Spring Boot

## 🌍 Contexto

Las anotaciones en Java son metadatos que agregan información al código fuente
sin afectar directamente su ejecución: son una forma de "etiquetar" una clase o
un método para que una herramienta (en este caso, Spring Boot) sepa qué hacer con
ella. En Spring Boot, las anotaciones son el mecanismo principal para definir
componentes, configurar la aplicación y administrar el ciclo de vida de los
objetos, evitando los archivos de configuración extensos (por ejemplo, XML) que
exigía Spring en sus primeras versiones.

## 🧠 Propósito y ventajas

**Propósito**: reemplazar la configuración manual (archivos XML, registros
explícitos) por marcas directamente sobre el código, para mapear rutas HTTP,
declarar beans, configurar inyección de dependencias y fijar parámetros de
configuración con el mínimo esfuerzo.

**Ventajas**:

- **Menos código repetitivo**: no hace falta declarar cada bean en un archivo de
  configuración aparte.
- **Mayor legibilidad**: el propósito de una clase o método se entiende con solo
  mirar sus anotaciones (`@RestController` ya dice "esta clase expone endpoints
  HTTP").
- **Integración automática con el ecosistema Spring**: al reconocer una
  anotación, Spring Boot activa el comportamiento correspondiente sin
  configuración adicional.

## 🔍 Anotaciones más comunes en Spring Boot

| Anotación | Dónde se usa | Qué hace |
|---|---|---|
| `@SpringBootApplication` | Clase principal | Combina configuración, autoconfiguración y escaneo de componentes (Ejemplo 07). |
| `@Component` | Cualquier clase | Marca una clase como bean genérico administrado por el contenedor IoC (Ejemplo 10). |
| `@Service` | Clase de lógica de negocio | Es un `@Component` especializado semánticamente: indica que la clase contiene reglas de negocio (por ejemplo, `ServicioPrestamos`). |
| `@Repository` | Clase de acceso a datos | Es un `@Component` especializado para el acceso a datos; en módulos posteriores también traduce excepciones de la base de datos. |
| `@RestController` | Clase que expone una API | Combina `@Controller` (bean web) y `@ResponseBody` (la respuesta de cada método se serializa, por ejemplo a JSON) automáticamente. |
| `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` | Método dentro de un `@RestController` | Asocian ese método a una ruta HTTP y a un verbo (`GET`, `POST`, `PUT`, `DELETE`). |
| `@Autowired` | Constructor, setter o campo | Le indica a Spring qué dependencia inyectar (Ejemplo 11 la explica en profundidad). |
| `@Configuration` | Clase de configuración | Marca una clase como fuente de definición de beans mediante métodos `@Bean`. |

## 💻 Ejemplo aplicado

```java
@Component // (1) bean genérico administrado por el contenedor
public class ValidadorIsbn {
    public boolean esValido(String isbn) { /* ... */ return true; }
}

@Repository // (2) especialización de @Component para acceso a datos
public class RepositorioLibrosEnMemoria implements RepositorioLibros {
    @Override
    public Optional<Libro> buscarPorIsbn(String isbn) { /* ... */ return Optional.empty(); }
}

@Service // (3) especialización de @Component para lógica de negocio
public class ServicioPrestamos {

    private final RepositorioLibros repositorioLibros;

    public ServicioPrestamos(RepositorioLibros repositorioLibros) { // (4) ver Ejemplo 11
        this.repositorioLibros = repositorioLibros;
    }

    public boolean prestar(String isbn) {
        return repositorioLibros.buscarPorIsbn(isbn).isPresent();
    }
}

@RestController // (5) expone endpoints HTTP
public class PrestamosController {

    private final ServicioPrestamos servicioPrestamos;

    public PrestamosController(ServicioPrestamos servicioPrestamos) {
        this.servicioPrestamos = servicioPrestamos;
    }

    @GetMapping("/prestamos/{isbn}") // (6) GET /prestamos/{isbn}
    public boolean prestar(@PathVariable String isbn) {
        return servicioPrestamos.prestar(isbn);
    }
}

@SpringBootApplication // (7) clase principal: arranca el contenedor y escanea los @Component de arriba
public class BibliotecaApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BibliotecaApiApplication.class, args);
    }
}
```

## 🧭 Explicación paso a paso

1. `@Component`, `@Service` y `@Repository` hacen exactamente lo mismo a nivel
   técnico (registran la clase como bean), pero comunican una **intención**
   distinta: qué tipo de responsabilidad tiene cada clase dentro de la
   arquitectura por capas del Ejemplo 08.
2. El constructor de `ServicioPrestamos` no necesita escribir `@Autowired`
   explícitamente: desde Spring 4.3, si una clase tiene un **único** constructor,
   Spring lo usa automáticamente para inyectar sus dependencias (Ejemplo 11).
3. `@RestController` sobre `PrestamosController` es lo que le permite a Spring
   Boot invocar el método `prestar(...)` cuando llega una petición `GET` a
   `/prestamos/{isbn}` — el mismo mecanismo de inversión de control explicado en
   el Ejemplo 03: el framework llama al método, no el desarrollador.
4. `@PathVariable` es una anotación adicional, a nivel de parámetro, que le indica
   a Spring de dónde extraer el valor `isbn` (de la propia URL).
5. `BibliotecaApiApplication` es la clase principal: su `main` (`SpringApplication.run(...)`)
   es lo único que el desarrollador ejecuta directamente; a partir de ahí, Spring
   Boot escanea el paquete, registra los cuatro `@Component` de arriba como beans,
   y queda esperando peticiones HTTP.

## ✅ Resultado esperado

```text
GET http://localhost:8080/prestamos/978-3-16-148410-0
→ 200 OK
→ true   (o false, según si el libro existe en el repositorio)
```

## 📌 Idea clave

Ninguna de estas anotaciones ejecuta código por sí misma en el momento en que
Java compila la clase: son leídas por Spring Boot al arrancar la aplicación, que
decide qué hacer según cada anotación. Por eso son "metadatos": describen el
código, y es el framework quien actúa en consecuencia.
