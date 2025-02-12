-- Script de migracion desde FIRe 2.4 a 2.5


-- TABLA de usuarios

-- Agregamos un campo DNI con un valor preestablecido para las entradas existentes
ALTER TABLE "TB_USUARIOS" ADD DNI VARCHAR(9) NULL;
UPDATE "TB_USUARIOS" SET DNI = 'X0000000T' WHERE id_usuario = 1;
-- Agregamos un campo FEC_ULTIMO_ACCESO con un valor preestablecido para las entradas existentes
ALTER TABLE "TB_USUARIOS" ADD FEC_ULTIMO_ACCESO TIMESTAMP DEFAULT SYSDATE;

-- TABLA de certificados

ALTER TABLE "TB_CERTIFICADOS" RENAME COLUMN CERT_PRINCIPAL TO CERTIFICADO;
ALTER TABLE "TB_CERTIFICADOS" RENAME COLUMN HUELLA_PRINCIPAL TO HUELLA;
ALTER TABLE "TB_CERTIFICADOS" DROP COLUMN CERT_BACKUP;
ALTER TABLE "TB_CERTIFICADOS" DROP COLUMN HUELLA_BACKUP;
ALTER TABLE "TB_CERTIFICADOS" ADD FEC_INICIO TIMESTAMP NULL;
ALTER TABLE "TB_CERTIFICADOS" ADD FEC_CADUCIDAD TIMESTAMP NULL;
ALTER TABLE "TB_CERTIFICADOS" ADD SUBJECT VARCHAR(4000) NULL;

-- Tabla de relacion de certificados y aplicaciones

CREATE TABLE "TB_CERTIFICADOS_DE_APLICACION" (
  "ID_CERTIFICADOS" NUMBER NOT NULL,
  "ID_APLICACIONES" VARCHAR2(48) NOT NULL,
  CONSTRAINT "TB_CERT_APP_PK" PRIMARY KEY ("ID_CERTIFICADOS","ID_APLICACIONES")
);

-- TABLA aplicaciones

ALTER TABLE "TB_APLICACIONES" DROP COLUMN FK_CERTIFICADO;

-- Tabla TIPO_PLANIFICADOR
CREATE TABLE "TB_TIPO_PLANIFICADOR" (
  "ID_TIPO_PLANIFICADOR" NUMBER(19) NOT NULL,
  "NOMBRE_TOKEN" VARCHAR2(30) NOT NULL,
  CONSTRAINT "PK_ID_TIPO_PLANIFICADOR" PRIMARY KEY ("ID_TIPO_PLANIFICADOR")
);

-- Tabla PLANIFICADOR
CREATE TABLE "TB_PLANIFICADOR" (
  "ID_PLANIFICADOR" NUMBER(19) NOT NULL,
  "HORA_PERIODO" NUMBER(3),
  "MINUTO_PERIODO" NUMBER(3),
  "SEGUNDO_PERIODO" NUMBER(3),
  "DIA_INICIO" TIMESTAMP,
  "ID_TIPO_PLANIFICADOR" NUMBER(19) NOT NULL,
  "AVISO_ANTICIPADO" NUMBER(3),
  CONSTRAINT "PK_ID_PLANIFICADOR" PRIMARY KEY ("ID_PLANIFICADOR"),
  CONSTRAINT "FK_TIPO_PLANIFICADOR" FOREIGN KEY ("ID_TIPO_PLANIFICADOR") REFERENCES "TB_TIPO_PLANIFICADOR" ("ID_TIPO_PLANIFICADOR")
);

-- Tabla PROGRAMADOR
CREATE TABLE "TB_PROGRAMADOR" (
  "ID_PROGRAMADOR" NUMBER(19) NOT NULL,
  "NOMBRE_TOKEN" VARCHAR2(30) NOT NULL,
  "NOMBRE_CLASE" VARCHAR2(255) NOT NULL,
  "ESTA_ACTIVO" CHAR(1) NOT NULL,
  "NUM_HILOS" NUMBER(19),
  "NUM_PROCESOS" NUMBER(19),
  "PERIODO_EXPIRADO" NUMBER(19),
  "TIEMPO_REASIGNACION" NUMBER(19),
  "TIEMPO_REACTIVACION" NUMBER(19),
  "TIEMPO_COMPROBACION" NUMBER(19),
  "DIAS_PREAVISO" NUMBER(19),
  "PERIODO_COMUNICACION" NUMBER(19),
  "FECHA_ULTIMA_COMUNICACION" TIMESTAMP
  "ID_PLANIFICADOR" NUMBER(19) NOT NULL,
  "NOMBRE_PROGRAMADOR" VARCHAR2(50) NOT NULL,
  CONSTRAINT "PK_ID_PROGRAMADOR" PRIMARY KEY ("ID_PROGRAMADOR"),
  CONSTRAINT "UNICO_NOMBRE_PROGRAMADOR" UNIQUE ("NOMBRE_PROGRAMADOR"),
  CONSTRAINT "FK_PLANIFICADOR" FOREIGN KEY ("ID_PLANIFICADOR") REFERENCES "TB_PLANIFICADOR" ("ID_PLANIFICADOR")
);

CREATE SEQUENCE SQ_PLANIFICADOR START WITH 1 INCREMENT BY 1 NOMAXVALUE NOCYCLE;

-- Insertar valores en la tabla TIPO_PLANIFICADOR
INSERT INTO TB_TIPO_PLANIFICADOR (ID_TIPO_PLANIFICADOR, NOMBRE_TOKEN) 
VALUES (0, 'TIPO_PLANIFICADOR00');

INSERT INTO TB_TIPO_PLANIFICADOR (ID_TIPO_PLANIFICADOR, NOMBRE_TOKEN) 
VALUES (1, 'TIPO_PLANIFICADOR01');

INSERT INTO TB_TIPO_PLANIFICADOR (ID_TIPO_PLANIFICADOR, NOMBRE_TOKEN) 
VALUES (2, 'TIPO_PLANIFICADOR02');

-- Insertar valores en la tabla PLANIFICADOR
INSERT INTO TB_PLANIFICADOR (ID_PLANIFICADOR, HORA_PERIODO, MINUTO_PERIODO, SEGUNDO_PERIODO, DIA_INICIO, ID_TIPO_PLANIFICADOR) 
VALUES (1, 24, 0, 0, TO_TIMESTAMP('01/01/2012 00:00:00', 'MM/DD/YYYY HH24:MI:SS'), 1);

-- Insertar valores en la tabla PROGRAMADOR
INSERT INTO TB_PROGRAMADOR (ID_PROGRAMADOR, NOMBRE_TOKEN, NOMBRE_CLASE, ESTA_ACTIVO, NUM_HILOS, NUM_PROCESOS, PERIODO_EXPIRADO, TIEMPO_REASIGNACION, TIEMPO_REACTIVACION, TIEMPO_COMPROBACION, DIAS_PREAVISO, PERIODO_COMUNICACION, ID_PLANIFICADOR, NOMBRE_PROGRAMADOR) 
VALUES (1, 'PROGRAMADOR01', 'es.gob.fire.control.tasks.TaskVerifyCertExpired', 'N', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'TaskVerifyCertExpired');