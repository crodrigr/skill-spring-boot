# academia-bd — Taller de gestión de cursos con base de datos (M2)

Código de [`02-02-guia-proyecto-academico-jpa-completo.md`](../../02-02-guia-proyecto-academico-jpa-completo.md).

Tres capas: `presentation.controller` → `business` (interfaz en `service`, implementación `…Impl` en `impl`) → `persistence` (`repository`, `entity`).

## Ejecutar

Requiere Java 21+ y MySQL 8 en `localhost:3306` (usuario `root`, clave `root`, ver `application.properties`). La base `academia` se crea sola.

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

> ⚠️ `spring.jpa.hibernate.ddl-auto=create` borra y recrea las tablas en cada arranque.

## Probar

```bash
curl -X POST localhost:8080/api/students -H 'Content-Type: application/json' -d '{"name":"Ana","age":20,"email":"ana@x.com"}'
curl -X POST localhost:8080/api/courses  -H 'Content-Type: application/json' -d '{"name":"Java","price":100,"maxCapacity":1}'
curl -X POST 'localhost:8080/api/enrollments?studentId=1&courseId=1'
curl localhost:8080/api/courses/1/enrolled
```

Verificado: `201` al crear, `409` por correo repetido (sin importar mayúsculas), matrícula duplicada, curso lleno o ids inexistentes; `404` en `/enrolled` de un curso que no existe.
