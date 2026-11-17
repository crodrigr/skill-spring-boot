# 🔴 Avanzado 03 — Diagnosticar un token manipulado aceptado incorrectamente

## 🧩 Problema

Otro compañero implementó su propia versión de `UtilJwt.validarToken`, y
descubre en la práctica algo grave: si modifica el payload de un JWT
(por ejemplo, para cambiarse el rol a `ROLE_ADMIN`) y lo reenvía, el
servidor **acepta** el token modificado en vez de rechazarlo — contrario
a lo explicado en el Ejemplo 07.

Esta es la implementación que escribió:

```java
public boolean validarToken(String token) {
    try {
        String[] partes = token.split("\\.");
        String payloadJson = new String(Base64.getDecoder().decode(partes[1]));
        return payloadJson.contains("\"sub\""); // ¿el payload tiene un subject?
    } catch (Exception ex) {
        return false;
    }
}
```

**Pregunta**: explicá por qué esta implementación acepta un token
manipulado, y corregila para que realmente verifique la firma.

## 💻 Código o contexto de partida

Usá como referencia la implementación correcta de `UtilJwt.validarToken`
del Ejemplo 08 (con `Jwts.parser().verifyWith(clave).build()
.parseSignedClaims(token)`).

## 📏 Criterios de evaluación de la solución

- Explica que la implementación incorrecta solo decodifica el payload de
  Base64 y comprueba que tenga cierta forma, pero **nunca recalcula ni
  compara la firma** contra la clave secreta — por eso acepta cualquier
  payload bien formado, manipulado o no.
- Corrige `validarToken` para usar el parser de JJWT
  (`Jwts.parser().verifyWith(clave).build().parseSignedClaims(token)`),
  que lanza una excepción si la firma no coincide con el contenido.
- Verifica que, tras la corrección, un token con el payload modificado (y
  la firma original sin recalcular) es rechazado.

## 🚧 Restricciones

- La corrección debe seguir devolviendo `boolean` (no debe cambiar la
  firma del método `validarToken`, solo su implementación interna).

## 📊 Dificultad

Avanzado.

## 🎓 Resultados de aprendizaje

`RA-9`.
