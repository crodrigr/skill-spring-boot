# 🟡 Intermedio 01 — Refactorizar `ServicioNotificaciones` para recibir sus dependencias por constructor

## 🧩 Problema

Biblioteca Universitaria tiene un `ServicioNotificaciones` que avisa a los usuarios
sobre préstamos próximos a vencer. La clase crea sus propias dependencias con
`new`, lo que impide reemplazarlas por una versión de prueba al escribir un test.

## 💻 Código o contexto de partida

```java
public record Usuario(String codigo, String email) {}

public interface RepositorioUsuarios {
    Usuario buscarPorCodigo(String codigo);
}

public class RepositorioUsuariosJpa implements RepositorioUsuarios {
    @Override
    public Usuario buscarPorCodigo(String codigo) {
        return new Usuario(codigo, "usuario_" + codigo + "@universidad.edu"); // simplificado
    }
}

public interface ClienteEmail {
    void enviar(String destinatario, String mensaje);
}

public class ClienteEmailSmtp implements ClienteEmail {
    @Override
    public void enviar(String destinatario, String mensaje) {
        System.out.println("Email a " + destinatario + ": " + mensaje);
    }
}

public class ServicioNotificaciones {

    private RepositorioUsuarios repositorioUsuarios = new RepositorioUsuariosJpa();
    private ClienteEmail clienteEmail = new ClienteEmailSmtp();

    public void notificarVencimientoProximo(String isbn, String codigoUsuario) {
        Usuario usuario = repositorioUsuarios.buscarPorCodigo(codigoUsuario);
        clienteEmail.enviar(
            usuario.email(),
            "Tu préstamo del libro " + isbn + " vence pronto."
        );
    }
}

public class Main {
    public static void main(String[] args) {
        ServicioNotificaciones servicio = new ServicioNotificaciones();
        servicio.notificarVencimientoProximo("978-3-16-148410-0", "EST-010");
    }
}
```

Ejecutá `Main` primero para confirmar que la versión de partida funciona.
Después, refactorizá `ServicioNotificaciones` para que reciba `RepositorioUsuarios`
y `ClienteEmail` por **constructor**, en vez de instanciarlos con `new`, ajustando
`Main` para construirlos afuera y pasárselos. `Main` debe seguir imprimiendo la
misma línea después del cambio. Explicá, en 2 o 3 líneas, qué beneficio concreto
obtenés para las pruebas.

## 📏 Criterios de evaluación de la solución

- La clase ya no contiene ninguna expresión `new RepositorioUsuariosJpa()` ni
  `new ClienteEmailSmtp()` en su interior.
- `RepositorioUsuarios` y `ClienteEmail` se declaran como campos `private final` y
  se reciben como parámetros del constructor.
- La explicación menciona que ahora se puede construir
  `new ServicioNotificaciones(repositorioDePrueba, clienteEmailDePrueba)` con
  implementaciones de prueba (*test doubles*), sin depender de una base de datos
  real ni de enviar un correo real.

## 🚧 Restricciones

- No es necesario agregar anotaciones de Spring (`@Service`, `@Autowired`); el
  ejercicio se resuelve en Java puro, igual que el resto del módulo.
- No cambies la lógica de `notificarVencimientoProximo`, solo cómo se obtienen las
  dependencias.

## 📊 Dificultad

Intermedio

## ✅ Salida esperada al ejecutar `Main` (antes y después de refactorizar)

```text
Email a usuario_EST-010@universidad.edu: Tu préstamo del libro 978-3-16-148410-0 vence pronto.
```

## 🎓 Resultados de aprendizaje

RA-8, RA-9
