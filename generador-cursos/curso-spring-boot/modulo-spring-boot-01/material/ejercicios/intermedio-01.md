# 🟡 Intermedio 01 — Refactorizar `ServicioNotificaciones` para recibir sus dependencias por constructor

## 🧩 Problema

Biblioteca Universitaria tiene un `ServicioNotificaciones` que avisa a los usuarios
sobre préstamos próximos a vencer. La clase crea sus propias dependencias con
`new`, lo que impide reemplazarlas por una versión de prueba al escribir un test.

## 💻 Código o contexto de partida

```java
public class ServicioNotificaciones {

    private RepositorioUsuarios repositorioUsuarios = new RepositorioUsuariosJpa();
    private ClienteEmail clienteEmail = new ClienteEmailSmtp();

    public void notificarVencimientoProximo(String isbn, String codigoUsuario) {
        Usuario usuario = repositorioUsuarios.buscarPorCodigo(codigoUsuario);
        clienteEmail.enviar(
            usuario.getEmail(),
            "Tu préstamo del libro " + isbn + " vence pronto."
        );
    }
}
```

Refactorizá `ServicioNotificaciones` para que reciba `RepositorioUsuarios` y
`ClienteEmail` por **constructor**, en vez de instanciarlos con `new`. Explicá, en
2 o 3 líneas, qué beneficio concreto obtenés para las pruebas.

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

## 🎓 Resultados de aprendizaje

RA-8, RA-9
