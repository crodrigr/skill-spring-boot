# 🛠️ Taller 01 — Diagnosticar dependencias y resolver una ambigüedad en Biblioteca Universitaria

## 🎯 Objetivo

Identificar dependencias directas y transitivas en un fragmento de proyecto de
Biblioteca Universitaria, resolver una ambigüedad de inyección con
`@Qualifier`, y documentar al menos dos buenas prácticas de diseño de
dependencias aplicadas o pendientes. (RA-1, RA-3, RA-7, RA-8, RA-9)

## 🌍 Contexto

La Biblioteca Universitaria está agregando un sistema de recordatorios de
devolución. El equipo ya escribió las clases, pero no las anotó ni resolvió
todavía la ambigüedad entre sus dos formas de notificar.

```java
public interface RepositorioLibros {
    Optional<Libro> buscarPorIsbn(String isbn);
}

public class RepositorioLibrosEnMemoria implements RepositorioLibros {
    @Override
    public Optional<Libro> buscarPorIsbn(String isbn) { /* ... */ return Optional.empty(); }
}

public interface CanalDeAviso {
    void avisar(String destinatario, String mensaje);
}

public class AvisoPorEmail implements CanalDeAviso {
    @Override
    public void avisar(String destinatario, String mensaje) {
        System.out.println("Email a " + destinatario + ": " + mensaje);
    }
}

public class AvisoPorApp implements CanalDeAviso {
    @Override
    public void avisar(String destinatario, String mensaje) {
        System.out.println("Notificación in-app a " + destinatario + ": " + mensaje);
    }
}

public class ServicioRecordatoriosDevolucion {

    private final RepositorioLibros repositorioLibros;
    private final CanalDeAviso canalDeAviso;

    public ServicioRecordatoriosDevolucion(RepositorioLibros repositorioLibros, CanalDeAviso canalDeAviso) {
        this.repositorioLibros = repositorioLibros;
        this.canalDeAviso = canalDeAviso;
    }

    public void recordarDevolucion(String isbn, String destinatario) {
        Libro libro = repositorioLibros.buscarPorIsbn(isbn).orElseThrow();
        canalDeAviso.avisar(destinatario, "Recordá devolver: " + libro.titulo());
    }
}

public class ControladorRecordatorios {

    private final ServicioRecordatoriosDevolucion servicioRecordatorios;

    public ControladorRecordatorios(ServicioRecordatoriosDevolucion servicioRecordatorios) {
        this.servicioRecordatorios = servicioRecordatorios;
    }

    public void solicitarRecordatorio(String isbn, String destinatario) {
        servicioRecordatorios.recordarDevolucion(isbn, destinatario);
    }
}
```

## 🪜 Pasos

1. **Diagnóstico de dependencias**: listá todas las relaciones de dependencia
   entre `ControladorRecordatorios`, `ServicioRecordatoriosDevolucion`,
   `RepositorioLibros` y `CanalDeAviso`, indicando cuáles son directas y
   cuáles transitivas.
2. **Anotar las clases**: agregá `@Repository` a `RepositorioLibrosEnMemoria`,
   `@Service` a `ServicioRecordatoriosDevolucion`, y `@Component` a las dos
   implementaciones de `CanalDeAviso` (`AvisoPorEmail`, `AvisoPorApp`).
3. **Diagnosticar la ambigüedad**: explicá por qué, apenas ambas
   implementaciones de `CanalDeAviso` quedan anotadas, el contenedor no puede
   arrancar `ServicioRecordatoriosDevolucion` sin ayuda.
4. **Resolver con `@Qualifier`**: anotá `AvisoPorEmail` y `AvisoPorApp` con
   `@Qualifier` (por ejemplo `"email"` y `"app"`), y el parámetro
   `canalDeAviso` del constructor de `ServicioRecordatoriosDevolucion` con el
   que corresponda (la biblioteca decidió usar email por defecto).
5. **Buenas prácticas**: documentá al menos dos buenas prácticas de diseño de
   dependencias (depender de abstracciones, minimizar transitivas expuestas,
   evitar dependencias circulares) que este diseño ya cumple, o que
   mejorarías.

## 💡 Ejemplo resuelto (parcial: solo el diagnóstico del paso 1)

```text
ServicioRecordatoriosDevolucion → RepositorioLibros: directa
ServicioRecordatoriosDevolucion → CanalDeAviso: directa
ControladorRecordatorios → ServicioRecordatoriosDevolucion: directa
ControladorRecordatorios → RepositorioLibros: transitiva (vía ServicioRecordatoriosDevolucion)
ControladorRecordatorios → CanalDeAviso: transitiva (vía ServicioRecordatoriosDevolucion)
```

Los pasos 2 a 5 (anotar, resolver la ambigüedad y documentar buenas prácticas)
quedan para que los completes vos.

## 📦 Entregable

Un pequeño proyecto de archivos (un `.java` por clase/interfaz), con:
`RepositorioLibros.java`, `RepositorioLibrosEnMemoria.java`,
`CanalDeAviso.java`, `AvisoPorEmail.java`, `AvisoPorApp.java`,
`ServicioRecordatoriosDevolucion.java`, `ControladorRecordatorios.java` (todas
anotadas y con la ambigüedad resuelta), más un archivo `diagnostico.md` con el
listado de dependencias del paso 1 y las buenas prácticas del paso 5.

## 🧪 Casos de prueba

| Caso | Resultado esperado |
|---|---|
| Anotar las 4 clases concretas del paso 2 | El contenedor puede escanearlas como beans, sin errores de compilación |
| Ambigüedad de `CanalDeAviso` sin `@Qualifier` | Se identifica correctamente el error de "no qualifying bean" que produciría Spring |
| Resolución con `@Qualifier` | `ServicioRecordatoriosDevolucion` queda inyectado específicamente con `AvisoPorEmail` |

## 📏 Criterios de evaluación

- El diagnóstico de dependencias (paso 1) es completo y clasifica
  correctamente directas vs. transitivas.
- Las anotaciones del paso 2 usan la especialización correcta según la capa
  (`@Repository`, `@Service`, `@Component`).
- La ambigüedad del paso 4 queda resuelta explícitamente con `@Qualifier`
  hacia `AvisoPorEmail`, sin eliminar `AvisoPorApp`.
- Se documentan al menos dos buenas prácticas concretas, no genéricas.
