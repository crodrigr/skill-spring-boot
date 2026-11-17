-- migrate.sql: carga datos de ejemplo en la base `academia` de la API con Spring Security.
--   · 2 cuentas para autenticar:  admin / admin123 (ADMIN)  y  ana / ana123 (USER)
--     (las contraseñas se guardan como hash BCrypt; el login recibe el texto plano)
--   · 20 estudiantes, 4 cursos y 10 matrículas (el curso «Docker y contenedores» queda LLENO: cupo 3)
--
-- Uso:  mysql -uroot -proot < migrate.sql      (o ejecútalo desde MySQL Workbench)
--
-- ⚠️ La aplicación usa spring.jpa.hibernate.ddl-auto=create: BORRA y recrea las tablas cada vez que arranca.
--    Ejecuta este script DESPUÉS de arrancar la aplicación, o cambia a ddl-auto=update.
-- Es repetible: no duplica cuentas, estudiantes (por correo), cursos (por nombre) ni matrículas.
-- SOLO PARA FINES EDUCATIVOS: en un proyecto real no se siembran cuentas con contraseñas conocidas.

CREATE DATABASE IF NOT EXISTS academia;
USE academia;

-- Tablas (idénticas a las que genera Hibernate; no hacen nada si ya existen)
CREATE TABLE IF NOT EXISTS user_account (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  username      VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role          ENUM('ADMIN','USER') NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_account_username (username)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  name       VARCHAR(255) NULL,
  age        INT          NOT NULL,
  email      VARCHAR(255) NULL,
  created_at DATETIME(6)  NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_student_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS course (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  name         VARCHAR(255) NULL,
  price        DOUBLE       NOT NULL,
  max_capacity INT          NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS enrollment (
  id         BIGINT NOT NULL AUTO_INCREMENT,
  student_id BIGINT NULL,
  course_id  BIGINT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES student (id),
  CONSTRAINT fk_enrollment_course  FOREIGN KEY (course_id)  REFERENCES course (id)
) ENGINE=InnoDB;

-- Cuentas para autenticar (INSERT IGNORE: si el usuario ya existe, no se toca)
INSERT IGNORE INTO user_account (username, password_hash, role) VALUES
  ('admin', '$2a$10$RpzcdCeYb3qhImwhXg6PR.a2CyavpcEuqWqtyA95XU8HIuOf0Nwea', 'ADMIN'),
  ('ana',   '$2a$10$JGHYQBTzWz2g7JbSqPcao.Y1Luu72HfK.rTP23UCykhErENyMCJOC', 'USER');

-- Estudiantes (el correo es único: si ya existe, se omite)
INSERT IGNORE INTO student (name, age, email, created_at) VALUES
  ('Ana Paredes Soto', 42, 'ana1@academy.test', NOW()),
  ('Luis López Torres', 21, 'luis2@academy.test', NOW()),
  ('María Herrera Castro', 40, 'maria3@academy.test', NOW()),
  ('Carlos Flores Torres', 49, 'carlos4@academy.test', NOW()),
  ('Lucía García Torres', 22, 'lucia5@academy.test', NOW()),
  ('Pedro Ramírez Ramírez', 21, 'pedro6@academy.test', NOW()),
  ('Sofía García Castro', 52, 'sofia7@academy.test', NOW()),
  ('Diego Ramírez Torres', 53, 'diego8@academy.test', NOW()),
  ('Valentina Castro García', 57, 'valentina9@academy.test', NOW()),
  ('Andrés López Flores', 20, 'andres10@academy.test', NOW()),
  ('Camila Flores Flores', 42, 'camila11@academy.test', NOW()),
  ('Jorge Torres García', 19, 'jorge12@academy.test', NOW()),
  ('Paula Herrera Soto', 35, 'paula13@academy.test', NOW()),
  ('Mateo Ramírez Soto', 51, 'mateo14@academy.test', NOW()),
  ('Isabela Castro Flores', 36, 'isabela15@academy.test', NOW()),
  ('Tomás Herrera López', 28, 'tomas16@academy.test', NOW()),
  ('Daniela Castro Flores', 53, 'daniela17@academy.test', NOW()),
  ('Felipe López García', 40, 'felipe18@academy.test', NOW()),
  ('Laura Castro Herrera', 21, 'laura19@academy.test', NOW()),
  ('Sebastián Flores Torres', 56, 'sebastian20@academy.test', NOW());

-- Cursos (solo si no existe uno con ese nombre)
INSERT INTO course (name, price, max_capacity)
SELECT 'Java desde cero', 150000, 30 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course WHERE name = 'Java desde cero');

INSERT INTO course (name, price, max_capacity)
SELECT 'Spring Boot', 250000, 20 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course WHERE name = 'Spring Boot');

INSERT INTO course (name, price, max_capacity)
SELECT 'MySQL esencial', 120000, 25 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course WHERE name = 'MySQL esencial');

INSERT INTO course (name, price, max_capacity)
SELECT 'Docker y contenedores', 180000, 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course WHERE name = 'Docker y contenedores');

-- Matrículas (solo si esa pareja estudiante-curso no existe)
INSERT INTO enrollment (student_id, course_id)
SELECT s.id, c.id FROM student s, course c
WHERE s.email = 'luis2@academy.test' AND c.name = 'Java desde cero'
  AND NOT EXISTS (SELECT 1 FROM enrollment x WHERE x.student_id = s.id AND x.course_id = c.id);

INSERT INTO enrollment (student_id, course_id)
SELECT s.id, c.id FROM student s, course c
WHERE s.email = 'maria3@academy.test' AND c.name = 'Java desde cero'
  AND NOT EXISTS (SELECT 1 FROM enrollment x WHERE x.student_id = s.id AND x.course_id = c.id);

INSERT INTO enrollment (student_id, course_id)
SELECT s.id, c.id FROM student s, course c
WHERE s.email = 'carlos4@academy.test' AND c.name = 'Java desde cero'
  AND NOT EXISTS (SELECT 1 FROM enrollment x WHERE x.student_id = s.id AND x.course_id = c.id);

INSERT INTO enrollment (student_id, course_id)
SELECT s.id, c.id FROM student s, course c
WHERE s.email = 'luis2@academy.test' AND c.name = 'Spring Boot'
  AND NOT EXISTS (SELECT 1 FROM enrollment x WHERE x.student_id = s.id AND x.course_id = c.id);

INSERT INTO enrollment (student_id, course_id)
SELECT s.id, c.id FROM student s, course c
WHERE s.email = 'lucia5@academy.test' AND c.name = 'Spring Boot'
  AND NOT EXISTS (SELECT 1 FROM enrollment x WHERE x.student_id = s.id AND x.course_id = c.id);

INSERT INTO enrollment (student_id, course_id)
SELECT s.id, c.id FROM student s, course c
WHERE s.email = 'pedro6@academy.test' AND c.name = 'Spring Boot'
  AND NOT EXISTS (SELECT 1 FROM enrollment x WHERE x.student_id = s.id AND x.course_id = c.id);

INSERT INTO enrollment (student_id, course_id)
SELECT s.id, c.id FROM student s, course c
WHERE s.email = 'sofia7@academy.test' AND c.name = 'MySQL esencial'
  AND NOT EXISTS (SELECT 1 FROM enrollment x WHERE x.student_id = s.id AND x.course_id = c.id);

INSERT INTO enrollment (student_id, course_id)
SELECT s.id, c.id FROM student s, course c
WHERE s.email = 'diego8@academy.test' AND c.name = 'Docker y contenedores'
  AND NOT EXISTS (SELECT 1 FROM enrollment x WHERE x.student_id = s.id AND x.course_id = c.id);

INSERT INTO enrollment (student_id, course_id)
SELECT s.id, c.id FROM student s, course c
WHERE s.email = 'valentina9@academy.test' AND c.name = 'Docker y contenedores'
  AND NOT EXISTS (SELECT 1 FROM enrollment x WHERE x.student_id = s.id AND x.course_id = c.id);

INSERT INTO enrollment (student_id, course_id)
SELECT s.id, c.id FROM student s, course c
WHERE s.email = 'andres10@academy.test' AND c.name = 'Docker y contenedores'
  AND NOT EXISTS (SELECT 1 FROM enrollment x WHERE x.student_id = s.id AND x.course_id = c.id);

-- Comprobación rápida
SELECT username, role, LEFT(password_hash, 7) AS hash_prefix FROM user_account;
SELECT (SELECT COUNT(*) FROM student) AS estudiantes, (SELECT COUNT(*) FROM course) AS cursos, (SELECT COUNT(*) FROM enrollment) AS matriculas;
