# 🔑 Solución — Taller 01 (Diagnóstico de dependencias y `@Qualifier`)

Material docente. No enlazar desde archivos de audiencia estudiante (salvo la
subsección "Soluciones" de `specs/02-manejo-de-dependencias-y-java-beans.md`).

## 🌳 Árbol de archivos (entregable final)

```text
📁 taller-01-biblioteca
├── 📁 src/com/biblioteca
│   ├── 📁 controllers                      (capa de presentación)
│   │   └── 📄 ControladorRecordatorios.java
│   ├── 📁 services                         (capa de negocio)
│   │   ├── 📄 ServicioRecordatoriosDevolucion.java
│   │   ├── 📄 CanalDeAviso.java
│   │   ├── 📄 AvisoPorEmail.java
│   │   └── 📄 AvisoPorApp.java
│   └── 📁 persistences                     (capa de persistencia)
│       └── 📁 repositories
│           ├── 📄 RepositorioLibros.java
│           └── 📄 RepositorioLibrosEnMemoria.java
└── 📄 diagnostico.md
```

## 📄 Archivo: `diagnostico.md` (paso 1 y paso 5)

```text
Dependencias:
- ServicioRecordatoriosDevolucion → RepositorioLibros: directa
- ServicioRecordatoriosDevolucion → CanalDeAviso: directa
- ControladorRecordatorios → ServicioRecordatoriosDevolucion: directa
- ControladorRecordatorios → RepositorioLibros: transitiva (vía ServicioRecordatoriosDevolucion)
- ControladorRecordatorios → CanalDeAviso: transitiva (vía ServicioRecordatoriosDevolucion)

Buenas prácticas aplicadas:
1. Depender de abstracciones: ServicioRecordatoriosDevolucion depende de las
   interfaces RepositorioLibros y CanalDeAviso, no de RepositorioLibrosEnMemoria
   ni de AvisoPorEmail/AvisoPorApp directamente.
2. Minimizar dependencias transitivas expuestas: ControladorRecordatorios solo
   conoce a ServicioRecordatoriosDevolucion; nunca importa RepositorioLibros ni
   CanalDeAviso, aunque dependa de ellos transitivamente.
```

## 📄 Archivo: `RepositorioLibrosEnMemoria.java`

```java
package com.biblioteca.persistences.repositories;

@Repository
public class RepositorioLibrosEnMemoria implements RepositorioLibros {
    @Override
    public Optional<Libro> buscarPorIsbn(String isbn) {
        return Optional.of(new Libro(isbn, "Libro de ejemplo"));
    }
}
```

## 📄 Archivo: `AvisoPorEmail.java`

```java
package com.biblioteca.services;

@Component
@Qualifier("email")
public class AvisoPorEmail implements CanalDeAviso {
    @Override
    public void avisar(String destinatario, String mensaje) {
        System.out.println("Email a " + destinatario + ": " + mensaje);
    }
}
```

## 📄 Archivo: `AvisoPorApp.java`

```java
package com.biblioteca.services;

@Component
@Qualifier("app")
public class AvisoPorApp implements CanalDeAviso {
    @Override
    public void avisar(String destinatario, String mensaje) {
        System.out.println("Notificación in-app a " + destinatario + ": " + mensaje);
    }
}
```

## 📄 Archivo: `ServicioRecordatoriosDevolucion.java`

```java
package com.biblioteca.services;

@Service
public class ServicioRecordatoriosDevolucion {

    private final RepositorioLibros repositorioLibros;
    private final CanalDeAviso canalDeAviso;

    public ServicioRecordatoriosDevolucion(RepositorioLibros repositorioLibros,
                                            @Qualifier("email") CanalDeAviso canalDeAviso) {
        this.repositorioLibros = repositorioLibros;
        this.canalDeAviso = canalDeAviso;
    }

    public void recordarDevolucion(String isbn, String destinatario) {
        Libro libro = repositorioLibros.buscarPorIsbn(isbn).orElseThrow();
        canalDeAviso.avisar(destinatario, "Recordá devolver: " + libro.titulo());
    }
}
```

## 📄 Archivo: `ControladorRecordatorios.java`

```java
package com.biblioteca.controllers;

@Component
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

## 🧭 Explicación

Sin `@Qualifier`, al anotar `AvisoPorEmail` y `AvisoPorApp` con `@Component`,
Spring encontraría dos beans candidatos para el parámetro `CanalDeAviso
canalDeAviso` del constructor de `ServicioRecordatoriosDevolucion`, y fallaría
al arrancar con `No qualifying bean of type 'CanalDeAviso'... found 2:
avisoPorEmail, avisoPorApp`. Anotar cada implementación con un `@Qualifier`
distinto, y el parámetro del constructor con `@Qualifier("email")`, resuelve
la ambigüedad sin eliminar ninguna de las dos implementaciones: `AvisoPorApp`
sigue existiendo como bean, disponible para inyectarse en otro lugar que lo
pida explícitamente con `@Qualifier("app")`.

## 📏 Verificación

Se revisa que: (a) el diagnóstico de dependencias sea completo y correcto;
(b) las cuatro clases concretas tengan la anotación de capa correcta
(`@Repository`, `@Service`, `@Component`); (c) ambas implementaciones de
`CanalDeAviso` conserven su `@Qualifier` propio; y (d) el constructor de
`ServicioRecordatoriosDevolucion` reciba específicamente `AvisoPorEmail` vía
`@Qualifier("email")`.
