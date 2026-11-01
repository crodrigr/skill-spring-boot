# examen-libros

Solución del [examen «CRUD de libros en tres capas»](../../04-examen-crud-libros.md) (material docente: [pauta de corrección](../../05-examen-crud-libros-solucion.md)).

API REST en memoria con Spring Boot 3.3, organizada en presentación, negocio y persistencia.

## Requisitos

- Java 21 o superior · Maven 3.9+ (o el wrapper `./mvnw`)

## Ejecutar y probar

```bash
./mvnw spring-boot:run

curl -i http://localhost:8080/libros       # 200 · 5 libros
curl -i http://localhost:8080/libros/1     # 200 · Cien años de soledad
curl -i http://localhost:8080/libros/99    # 500 · NoSuchElementException (sin traducir)
```

## Estructura

```text
src/main/java/com/biblioteca
├── BibliotecaApplication.java
├── presentacion/controlador/ControladorLibros.java
├── negocio
│   ├── model/Libro.java
│   └── service/ServicioLibros.java
└── persistence/repository/RepositorioLibros.java · RepositorioLibrosEnMemoria.java
```
