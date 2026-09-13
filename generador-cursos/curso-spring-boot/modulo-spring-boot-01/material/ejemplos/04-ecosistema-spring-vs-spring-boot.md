# 💡 Ejemplo 04 — Ecosistema Spring vs Spring Boot

## 📚 Caso de estudio

Levantar un servicio web mínimo para Biblioteca Universitaria que responda al
verbo HTTP GET, comparando lo que exige Spring "clásico" frente a Spring Boot.

## 💻 Código — Spring clásico (configuración manual)

```java
// 1) Configuración explícita del contexto: qué beans existen y cómo se conectan
@Configuration
@EnableWebMvc
public class WebConfig {

    @Bean
    public CatalogoController catalogoController() {
        return new CatalogoController();
    }
}
```

```xml
<!-- 2) web.xml: registrar manualmente el DispatcherServlet de Spring MVC -->
<servlet>
    <servlet-name>dispatcher</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>com.biblioteca.config.WebConfig</param-value>
    </init-param>
</servlet>
```

```text
3) Empaquetar como .war e instalar/configurar un servidor de aplicaciones
   (Tomcat, Jetty, WildFly) por separado, con la versión y el puerto correctos.
```

## 💻 Código — Spring Boot (mismo resultado)

```java
@SpringBootApplication // agrupa @Configuration + autoconfiguración + escaneo de componentes
public class BibliotecaApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BibliotecaApiApplication.class, args);
    }
}

@RestController
public class CatalogoController {

    @GetMapping("/catalogo")
    public String catalogo() {
        return "Catálogo de la Biblioteca Universitaria";
    }
}
```

```properties
# application.properties — configuración mínima, con valores por defecto razonables
server.port=8080
```

## 🔍 Análisis comparado

| Paso manual en Spring clásico | Qué hace Spring Boot en su lugar |
|---|---|
| Declarar cada bean web a mano (`WebConfig`) | **Autoconfiguración**: detecta `spring-boot-starter-web` en el classpath y configura Spring MVC con valores por defecto razonables |
| Registrar el `DispatcherServlet` en `web.xml` | Ya viene configurado por el *starter*; solo se agregan controladores con `@RestController` |
| Instalar y configurar un servidor externo (Tomcat/Jetty) | **Servidor embebido**: la aplicación se ejecuta con `java -jar`, sin instalar nada aparte |
| Elegir a mano versiones de Spring MVC, Jackson, el servidor, etc. compatibles entre sí | **Starter** (`spring-boot-starter-web`): agrupa dependencias ya probadas como compatibles entre sí |

## 🧭 Explicación paso a paso

1. Ambas versiones terminan exponiendo el mismo endpoint (`/catalogo`) con Spring
   MVC por debajo: Spring Boot **no reemplaza** los conceptos de Spring.
2. Spring Boot elimina la configuración repetitiva (`WebConfig`, `web.xml`,
   servidor externo) reemplazándola por una anotación (`@SpringBootApplication`) y
   un archivo de propiedades opcional.
3. El *starter* `spring-boot-starter-web` es lo que le indica a la autoconfiguración
   "esta aplicación necesita web": si no estuviera en el classpath, Spring Boot no
   activaría esa configuración automática.
4. El servidor embebido (por defecto, Tomcat) es el que atiende `server.port=8080`;
   no hace falta instalar ni configurar un Tomcat aparte.

## ✅ Resultado esperado

```text
GET http://localhost:8080/catalogo
→ 200 OK
→ Catálogo de la Biblioteca Universitaria
```
