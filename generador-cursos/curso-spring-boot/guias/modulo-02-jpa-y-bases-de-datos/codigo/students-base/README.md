# students-base — CRUD de estudiantes (punto de partida del Módulo 03)

Código de [`03-guia-taller-students-punto-de-partida.md`](../../03-guia-taller-students-punto-de-partida.md). Es la base de [`03-01-guia-dto-y-estructura-de-controladores.md`](../../../modulo-03-diseno-api-rest-con-dto/03-01-guia-dto-y-estructura-de-controladores.md).

Capas: `presentation.controller` → `business` (interfaz `StudentService` en `service`, `StudentServiceImpl` en `impl`) → `persistence` (`entity`, `repository`).

Requiere Java 21+ y MySQL 8 en `localhost:3306` (`root`/`root`, ver `application.properties`).

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
curl -i -X POST localhost:8080/api/students -H 'Content-Type: application/json' \
  -d '{"name":"Ana","age":20,"email":"ana@x.com","status":"ACTIVE"}'
```

> ⚠️ `ddl-auto=create` borra y recrea las tablas en cada arranque.
