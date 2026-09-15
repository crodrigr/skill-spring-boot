# 🔴 Avanzado 02 — Diagnosticar una pérdida de datos por `ddl-auto`

## 🧩 Problema

Un compañero de equipo te escribe: "cada vez que reinicio mi proyecto de
MediSalud, los pacientes que había cargado el día anterior desaparecen, y no
toqué la base de datos a mano".

## 💻 Código o contexto de partida

```properties
spring.datasource.url=jdbc:h2:mem:medisalud;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

## 📏 Criterios de evaluación de la solución

- Identifica que `ddl-auto=create-drop` borra el esquema completo (con sus
  datos) al **apagarse** la aplicación, y lo recrea vacío al arrancar de
  nuevo.
- Propone cambiar el valor a `update` (el usado en el resto del curso) para
  conservar los datos entre ejecuciones, explicando la diferencia frente a
  `create`/`create-drop`.
- Explica por qué `validate` o `none` no serían apropiados en este momento
  del proyecto (exigen que el esquema ya exista, y en desarrollo temprano
  todavía no está creado).

## 🚧 Restricciones

Ninguna.

## 📊 Dificultad

Avanzado

## 🎓 Resultados de aprendizaje

RA-9
