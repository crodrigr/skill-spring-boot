# 💡 Ejemplo 05 — Configurando `application.properties`

## 🌍 Contexto

Con `springdoc-openapi-starter-webmvc-ui` ya agregada (Ejemplo 04), la
documentación ya se genera con sus rutas por defecto. Este ejemplo
personaliza esa configuración: habilitarla explícitamente, elegir dónde
se sirve Swagger UI, y decirle a springdoc exactamente qué paquete
escanear.

**Qué busca demostrar este ejemplo**: agregar a `application.properties`
las cuatro propiedades de springdoc necesarias para este módulo, y
explicar qué controla cada una.

## 📚 Caso de estudio

Biblioteca Universitaria: mismo proyecto de `ControladorLibros` (Ejemplo
04), con la dependencia springdoc ya agregada.

## 💻 Archivo: `application.properties` (Módulo 3, con las propiedades de springdoc agregadas)

```properties
spring.datasource.url=jdbc:h2:mem:biblioteca;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

# Configuración de springdoc-openapi (Swagger)
# Habilita la generación del documento OpenAPI en JSON (/v3/api-docs)
springdoc.api-docs.enabled=true
# Habilita la interfaz Swagger UI
springdoc.swagger-ui.enabled=true
# Ruta donde se sirve Swagger UI
springdoc.swagger-ui.path=/doc/swagger-ui.html
# Paquete donde springdoc busca los @RestController
springdoc.packages-to-scan=com.biblioteca
```

## 🧭 Explicación paso a paso

1. `springdoc.api-docs.enabled=true` habilita el documento OpenAPI en
   formato JSON crudo, disponible por defecto en `/v3/api-docs`; es la
   fuente de datos que Swagger UI consume para dibujar su interfaz.
2. `springdoc.swagger-ui.enabled=true` habilita específicamente la
   interfaz web interactiva (Swagger UI), separada del documento JSON
   crudo del punto anterior.
3. `springdoc.swagger-ui.path=/doc/swagger-ui.html` personaliza la URL
   donde se accede a Swagger UI; sin esta propiedad, springdoc usa una
   ruta por defecto (`/swagger-ui/index.html`).
4. `springdoc.packages-to-scan=com.biblioteca` le dice a springdoc en qué
   paquete buscar los `@RestController` a documentar — en este caso, el
   paquete donde vive `ControladorLibros`. Si el proyecto tuviera
   controladores en varios paquetes, se pueden listar separados por coma.
5. Ninguna de estas cuatro propiedades requiere reiniciar la base de
   datos ni afecta el esquema de H2: son configuración exclusiva de la
   capa de documentación.

## ❓ Preguntas de repaso

**1. [Selección]** ¿Qué propiedad habilita específicamente la interfaz
web interactiva de Swagger?

- **A.** `springdoc.api-docs.enabled`.
- **B.** `springdoc.swagger-ui.enabled`.
- **C.** `springdoc.packages-to-scan`.
- **D.** `spring.jpa.hibernate.ddl-auto`.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. `springdoc.swagger-ui.enabled` habilita
específicamente Swagger UI; `springdoc.api-docs.enabled` habilita el
documento JSON crudo, una cosa distinta.

</details>

**2. [Selección múltiple]** Seleccioná **todas** las afirmaciones
correctas sobre `springdoc.packages-to-scan`.

- **A.** Indica en qué paquete buscar los `@RestController` a documentar.
- **B.** Si se configura mal, Swagger UI puede cargar sin mostrar ningún endpoint.
- **C.** Afecta el esquema de la base de datos H2.
- **D.** Puede recibir más de un paquete, separados por coma.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: esta propiedad no tiene
ninguna relación con el esquema de la base de datos.

</details>

**3. [Abierta]** Un compañero configuró
`springdoc.swagger-ui.path=/doc/swagger-ui.html`, pero al acceder a esa
URL obtiene un error 404, mientras que `/v3/api-docs` sí responde con el
JSON de la documentación.

**Pregunta**: ¿Qué propiedad revisarías primero, y por qué?

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: Revisaría primero `springdoc.swagger-ui.enabled`.
Que `/v3/api-docs` responda confirma que `springdoc.api-docs.enabled`
está activo y que springdoc encontró los controladores correctamente;
pero eso no garantiza que la interfaz Swagger UI esté habilitada — son
dos propiedades independientes. Si `springdoc.swagger-ui.enabled` está
en `false` (o ausente con un valor por defecto distinto), la ruta de
Swagger UI no respondería, aunque el JSON crudo sí funcione.

</details>
