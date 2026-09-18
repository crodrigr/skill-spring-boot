# 💡 Ejemplo 07 — Ciclo de vida y firma de un JWT

## 🌍 Contexto

El Ejemplo 06 explicó de qué está hecho un JWT. Este ejemplo muestra
**cuándo** se crea, **cómo** se usa, y **qué pasa** si alguien intenta
manipularlo — el recorrido completo que se implementará en código en el
Ejemplo 08.

**Qué busca demostrar este ejemplo**: que la firma no es un detalle
técnico secundario, sino el mecanismo concreto que hace que un intento de
manipulación sea detectado y rechazado por el servidor.

## 🗺️ Diagrama

```mermaid
sequenceDiagram
    participant C as Cliente
    participant S as Servidor

    C->>S: POST /auth/login (usuario + contraseña)
    S->>S: Verifica credenciales
    S->>S: Genera JWT (header + payload + firma)
    S-->>C: 200 OK + JWT

    Note over C: El cliente guarda el JWT<br/>(no hay sesión en el servidor)

    C->>S: GET /libros (Authorization: Bearer <JWT>)
    S->>S: Valida la firma del JWT
    S-->>C: 200 OK + datos

    Note over C,S: Cada solicitud repite el mismo token;<br/>el servidor no recuerda nada entre solicitudes
```

## 🧭 Explicación paso a paso

1. **Emisión**: cuando el usuario inicia sesión con credenciales válidas,
   el servidor responde con un JWT en vez de un mensaje común — ese token
   contiene los tres cabezales ya vistos (header, payload, signature) y,
   al decodificarlo, se obtiene un texto legible en formato JSON (rol,
   tiempo de expiración, nombre de usuario, etc.).
2. **Uso**: el cliente incluye ese JWT en cada solicitud subsiguiente
   (típicamente en el encabezado `Authorization: Bearer <token>`), sin
   necesidad de volver a enviar usuario y contraseña.
3. **Validación**: el servidor recalcula la firma con la misma clave
   secreta y algoritmo, y la compara con la firma incluida en el token.
   Si coinciden, el contenido es confiable; si no, el token se rechaza.
4. **Detección de manipulación**: si alguien decodifica el payload,
   cambia un valor (por ejemplo, el rol) y lo vuelve a codificar, **puede
   hacerlo** — Base64 no lo impide —, pero al enviar ese token modificado,
   el servidor recalcula la firma sobre el contenido alterado y obtiene
   un resultado distinto al de la firma original incluida en el token.
   El servidor rechaza la solicitud porque las firmas no coinciden.

## ✅ Resultado esperado

```text
Método: POST
URL: http://localhost:8080/auth/login
Cuerpo: {"nombreUsuario": "ana", "contrasena": "clave123"}
Respuesta: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmEiLCJyb2wiOiJVU0VSIn0.4f2a8c..."
}
```

```text
Método: GET
URL: http://localhost:8080/libros
Autorización: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmEiLCJyb2wiOiJBRE1JTiJ9.XXXX (payload modificado, firma sin recalcular)
Respuesta: 401 Unauthorized
{
  "error": "Token inválido"
}
```

## ❓ Preguntas de repaso

**1. [Selección]** ¿En qué momento del ciclo de vida se genera el JWT?

- **A.** En cada solicitud a un endpoint protegido.
- **B.** Cuando el cliente decide cerrar sesión.
- **C.** Cuando el usuario se autentica exitosamente (login).
- **D.** Cuando el servidor arranca.

<details><summary>🔑 Ver respuesta</summary>

**C.** El JWT se emite una única vez, al momento del login exitoso; las
solicitudes siguientes solo lo reutilizan.

</details>

**2. [Abierta]** Un compañero modifica el payload de su JWT para
cambiarse a sí mismo el rol de `USER` a `ADMIN`, y lo vuelve a codificar
en Base64. ¿Por qué el servidor rechaza igual la solicitud, aunque el
formato del token sea válido?

<details><summary>🔑 Ver respuesta modelo</summary>

Porque la firma del JWT se calculó sobre el contenido **original** del
payload, usando la clave secreta del servidor. Al modificar el payload
sin volver a firmarlo con esa misma clave (que el cliente no conoce), la
firma incluida en el token ya no coincide con la que el servidor recalcula
al validar — el servidor detecta la discrepancia y rechaza la solicitud.

</details>

**3. [Selección múltiple]** Según el diagrama de secuencia, ¿cuáles de
las siguientes afirmaciones sobre el uso de un JWT son correctas?

- **A.** El cliente reenvía el mismo token en cada solicitud protegida.
- **B.** El servidor guarda una sesión con el estado de cada cliente autenticado.
- **C.** El servidor valida la firma del token en cada solicitud, de forma independiente.
- **D.** El login solo ocurre una vez; las solicitudes siguientes no vuelven a enviar usuario y contraseña.

<details><summary>🔑 Ver respuesta</summary>

**A, C y D.** B es falsa: precisamente porque la autenticación es
stateless, el servidor no guarda ninguna sesión — cada solicitud se
valida de forma independiente usando el propio token.

</details>
