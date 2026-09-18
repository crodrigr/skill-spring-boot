# 💡 Ejemplo 01 — ¿Qué es Spring Boot Security?

## 🌍 Contexto

Hasta el Módulo 7, cualquier persona que conozca la URL de un endpoint de
`ControladorLibros`, `ControladorPacientes` o `ControladorCitas` puede
consultar, crear, modificar o eliminar datos sin ninguna restricción. Eso
es aceptable mientras el curso se enfoca en construir la API, pero una
API real necesita controlar **quién** puede acceder y **a qué**.

Spring Boot Security es la parte del framework Spring que resuelve
exactamente eso: proporciona servicios de seguridad integrales para
aplicaciones basadas en Java, con el propósito de asegurar el proyecto
mediante la gestión de dos aspectos complementarios:

- **Autenticación**: verificar que un usuario es quien dice ser (por
  ejemplo, con un usuario y una contraseña).
- **Autorización**: verificar que un usuario ya autenticado tiene permiso
  para hacer lo que está pidiendo.

**Qué busca demostrar este ejemplo**: que Spring Boot Security no es "una
librería para poner contraseñas", sino un conjunto de conceptos
(autorización basada en roles, cadena de filtros, protección CSRF,
control de sesiones) que trabajan juntos para responder dos preguntas
distintas: "¿quién sos?" y "¿qué podés hacer?".

## 🧠 Conceptos clave de Spring Boot Security

- **Autorización basada en roles y autoridades**: Spring Security utiliza
  roles y autoridades para el control de acceso, donde un rol representa
  un conjunto de autoridades concedidas (por ejemplo, el rol `ROLE_ADMIN`
  puede agrupar la autoridad de crear, modificar y eliminar recursos,
  mientras que `ROLE_USER` solo agrupa la de consultarlos).
- **Cadena de filtros de seguridad**: Spring Security procesa y filtra
  las solicitudes entrantes con una cadena de filtros, donde cada filtro
  maneja un aspecto distinto de la seguridad (por ejemplo, uno verifica
  credenciales, otro valida un token).
- **Protección CSRF**: Spring Security incluye protección contra
  Falsificación de Solicitudes entre Sitios (Cross-Site Request Forgery),
  una de las vulnerabilidades web más comunes.
- **Control de sesiones**: Spring Security controla cómo y cuándo se crea
  una sesión HTTP para un usuario autenticado — algo central para
  distinguir una API stateful de una stateless (Ejemplo 03).
- **Seguridad en protocolos HTTP**: Spring Security protege contra
  vulnerabilidades comunes en el protocolo HTTP, además de las anteriores.

## 🧭 Explicación paso a paso

1. Ninguno de estos cuatro conceptos actúa de forma aislada: la cadena de
   filtros es el mecanismo que hace cumplir tanto la autenticación como
   la autorización basada en roles, en cada solicitud HTTP que llega a la
   aplicación.
2. El Ejemplo 02 muestra cómo estos conceptos se organizan en una
   arquitectura concreta, con componentes específicos para cada
   responsabilidad.
3. El Ejemplo 05 muestra el primer paso práctico: agregar Spring Security
   a un proyecto ya construido y observar el efecto inmediato de esta
   cadena de filtros sobre un endpoint que antes era público.

## ❓ Preguntas de repaso

**1. [Selección]** ¿Cuál de las siguientes opciones describe mejor el
propósito de Spring Boot Security?

- **A.** Encriptar la base de datos de la aplicación.
- **B.** Gestionar la autenticación y la autorización de la aplicación.
- **C.** Acelerar las consultas JPA mediante caché.
- **D.** Generar automáticamente la documentación de la API.

<details><summary>🔑 Ver respuesta</summary>

**B.** Spring Boot Security proporciona servicios de seguridad integrales
cuyo propósito central es asegurar el proyecto gestionando autenticación
(quién sos) y autorización (qué podés hacer).

</details>

**2. [Selección múltiple]** ¿Cuáles de los siguientes son conceptos clave
de Spring Boot Security mencionados en este ejemplo?

- **A.** Roles y autoridades para el control de acceso.
- **B.** Una cadena de filtros de seguridad.
- **C.** Un compilador de anotaciones personalizado.
- **D.** Protección contra CSRF.

<details><summary>🔑 Ver respuesta</summary>

**A, B y D.** El compilador de anotaciones personalizado (C) no es un
concepto de Spring Security; los otros tres sí están descritos en el
material fuente de este ejemplo.

</details>

**3. [Abierta]** Una compañera de curso dice: "Spring Security solo sirve
para pedir usuario y contraseña". ¿Qué le responderías, usando los
conceptos de este ejemplo?

<details><summary>🔑 Ver respuesta modelo</summary>

Pedir usuario y contraseña es solo autenticación. Spring Security también
resuelve autorización (qué puede hacer un usuario ya autenticado, según
sus roles y autoridades), protección CSRF, y control de sesiones — no es
un único mecanismo, sino un conjunto de conceptos coordinados por una
cadena de filtros.

</details>
