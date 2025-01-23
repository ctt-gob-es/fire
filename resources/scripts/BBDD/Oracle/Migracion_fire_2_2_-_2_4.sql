-- Script de migracion desde FIRe 2.2/2.3 a 2.4


-- TABLA de usuarios

-- Agregamos un campo FK_ROL con un valor preestablecido para las entradas existentes
ALTER TABLE "TB_USUARIOS" ADD "FK_ROL" NUMBER;
UPDATE "TB_USUARIOS" SET "FK_ROL" = 1;
ALTER TABLE  "TB_USUARIOS" MODIFY ("FK_ROL" NUMBER NOT NULL);
ALTER TABLE "TB_USUARIOS" ADD DNI VARCHAR(9) NULL;
UPDATE "TB_USUARIOS" SET DNI = 'X0000000T' WHERE id_usuario = 1;
ALTER TABLE "TB_USUARIOS" ADD FEC_ULTIMO_ACCESO TIMESTAMP DEFAULT SYSDATE;

-- Agregamos el campo CODIGO_RENOVACION unico
ALTER TABLE "TB_USUARIOS" ADD "CODIGO_RENOVACION" VARCHAR2(100) DEFAULT NULL;
ALTER TABLE "TB_USUARIOS"
ADD CONSTRAINT  "TB_USUARIOS_UK2" UNIQUE("CODIGO_RENOVACION");

-- Agregamos el campo FEC_RENOVACION 
ALTER TABLE "TB_USUARIOS" ADD "FEC_RENOVACION" TIMESTAMP DEFAULT NULL;

-- Agregamos el campo REST_CLAVE
ALTER TABLE "TB_USUARIOS" ADD "REST_CLAVE" NUMBER(1,0) DEFAULT 0;

-- Eliminamos el campo ROL
ALTER TABLE "TB_USUARIOS" DROP COLUMN "ROL";

-- Incrementamos el tamano del campo CLAVE y hacemos que pueda contener nulos
ALTER TABLE  "TB_USUARIOS" MODIFY ("CLAVE" VARCHAR2(2000) NULL);

-- Ajustamos las restricciones del resto de campos
ALTER TABLE  "TB_USUARIOS" MODIFY ("USU_DEFECTO" NUMBER(1,0) DEFAULT 0);
ALTER TABLE  "TB_USUARIOS" MODIFY ("FEC_ALTA" TIMESTAMP(6) DEFAULT SYSDATE);

-- Insertamos como usuarios a los responsables declarados en las aplicaciones
INSERT INTO "TB_USUARIOS" ("NOMBRE_USUARIO", "NOMBRE", "APELLIDOS", "CORREO_ELEC", "TELF_CONTACTO", "FEC_ALTA", "FK_ROL")
SELECT SUBSTR("ID", 1, 30), "RESPONSABLE", "RESPONSABLE", "RESP_CORREO", "RESP_TELEFONO", "FECHA_ALTA", 2
FROM "TB_APLICACIONES";

-- Tabla para el guardado de los roles

CREATE TABLE "TB_ROLES" (
  "ID" NUMBER NOT NULL,
  "NOMBRE_ROL" VARCHAR2(45) NOT NULL,
  "PERMISOS"   VARCHAR2(45),
  CONSTRAINT "TB_ROLES_PK" PRIMARY KEY ("ID"),
  CONSTRAINT "TB_ROLES_UK" UNIQUE ("NOMBRE_ROL")
);


-- Tabla para el guardado de las referencias a los servidores de log

CREATE TABLE "TB_SERVIDORES_LOG" (
  "ID_SERVIDOR" NUMBER NOT NULL,
  "NOMBRE" VARCHAR2(45) NOT NULL, 
  "URL_SERVICIO_LOG" VARCHAR2(500) NOT NULL, 
  "CLAVE" VARCHAR2(45) NOT NULL, 
  "VERIFICAR_SSL" NUMBER(1,0) NOT NULL,
  CONSTRAINT "TB_SERVIDORES_LOG_PK" PRIMARY KEY ("ID_SERVIDOR"), 
  CONSTRAINT "TB_SERVIDORES_LOG_UK1" UNIQUE ("NOMBRE"),
  CONSTRAINT "TB_SERVIDORES_LOG_UK2" UNIQUE ("URL_SERVICIO_LOG")
);

CREATE SEQUENCE "TB_SERVIDORES_LOG_SEQ"; 

CREATE OR REPLACE TRIGGER "BI_TB_SERVIDORES_LOG"  
  before insert on "TB_SERVIDORES_LOG"              
  for each row 
begin  
  if :NEW."ID_SERVIDOR" is null then
    select "TB_SERVIDORES_LOG_SEQ".nextval into :NEW."ID_SERVIDOR" from dual;
  end if;
end;
/

ALTER TRIGGER "BI_TB_SERVIDORES_LOG" ENABLE;

-- Tabla de estadisticas de las firmas

CREATE TABLE "TB_FIRMAS" (
  "ID" NUMBER NOT NULL,
  "FECHA" TIMESTAMP (6) NOT NULL,
  "APLICACION" VARCHAR2(45) NOT NULL, 
  "FORMATO" VARCHAR2(20) NOT NULL, 
  "FORMATO_MEJORADO" VARCHAR2(20),
  "ALGORITMO" VARCHAR2(20) NOT NULL, 
  "PROVEEDOR" VARCHAR2(45) NOT NULL, 
  "NAVEGADOR" VARCHAR2(45) NOT NULL, 
  "CORRECTA" NUMBER(1,0) NOT NULL,
  "TOTAL" NUMBER DEFAULT 0 NOT NULL,
  CONSTRAINT "TB_FIRMAS_PK" PRIMARY KEY ("ID")
);

CREATE SEQUENCE "TB_FIRMAS_SEQ"; 

CREATE OR REPLACE TRIGGER "BI_TB_FIRMAS"  
  before insert on "TB_FIRMAS"              
  for each row 
begin  
  if :NEW."ID" is null then
    select "TB_FIRMAS_SEQ".nextval into :NEW."ID" from dual;
  end if;
end;
/

ALTER TRIGGER "BI_TB_FIRMAS" ENABLE;

-- Tabla de estadisticas de las transacciones

CREATE TABLE "TB_TRANSACCIONES" (
  "ID" NUMBER NOT NULL,
  "FECHA" TIMESTAMP (6) NOT NULL,
  "APLICACION" VARCHAR2(45) NOT NULL,
  "OPERACION" VARCHAR2(10) NOT NULL,
  "PROVEEDOR" VARCHAR2(45) NOT NULL,
  "PROVEEDOR_FORZADO" NUMBER(1,0) NOT NULL,
  "CORRECTA" NUMBER(1,0) NOT NULL,
  "TAMANNO" NUMBER DEFAULT 0 NOT NULL,
  "TOTAL" NUMBER DEFAULT 0 NOT NULL,
  CONSTRAINT "TB_TRANSACCIONES_PK" PRIMARY KEY ("ID")
);

CREATE SEQUENCE "TB_TRANSACCIONES_SEQ"; 

CREATE OR REPLACE TRIGGER "BI_TB_TRANSACCIONES"  
  before insert on "TB_TRANSACCIONES"              
  for each row 
begin  
  if :NEW."ID" is null then
    select "TB_TRANSACCIONES_SEQ".nextval into :NEW."ID" from dual;
  end if;
end;
/

ALTER TRIGGER "BI_TB_TRANSACCIONES" ENABLE;

--   Insertamos los permisos de los roles
INSERT INTO TB_ROLES ("ID","NOMBRE_ROL","PERMISOS") 
VALUES(1,'admin','1,2');

INSERT INTO TB_ROLES ("ID","NOMBRE_ROL","PERMISOS") 
VALUES(2,'responsible','2');

INSERT INTO TB_ROLES ("ID","NOMBRE_ROL","PERMISOS") 
VALUES(3,'contact',NULL);

-- Establecemos la relacion entre la tabla de usuarios y la tabla de roles
ALTER TABLE "TB_USUARIOS" ADD CONSTRAINT
"TB_USUARIOS_FK" FOREIGN KEY ("FK_ROL") REFERENCES "TB_ROLES" ("ID");

-- Tabla de auditoria de las transacciones

CREATE TABLE "TB_AUDIT_TRANSACCIONES" (
	"ID" NUMBER NOT NULL,
	"FECHA" TIMESTAMP (6) NOT NULL,
	"ID_APLICACION" VARCHAR2(48) NOT NULL,
	"NOMBRE_APLICACION" VARCHAR2(45) NOT NULL,
	"ID_TRANSACCION" VARCHAR2(45) NOT NULL,
  	"OPERACION" VARCHAR2(20) NOT NULL,
  	"OPERACION_CRIPTOGRAFICA" VARCHAR2(20) NOT NULL,
  	"FORMATO" VARCHAR2(20) NOT NULL, 
  	"FORMATO_ACTUALIZADO" VARCHAR2(20),
  	"ALGORITMO" VARCHAR2(20) NOT NULL, 
  	"PROVEEDOR" VARCHAR2(45) NOT NULL,
  	"PROVEEDOR_FORZADO" NUMBER(1,0) NOT NULL,
  	"NAVEGADOR" VARCHAR2(20) NOT NULL, 
  	"TAMANNO" NUMBER DEFAULT 0,
  	"NODO" VARCHAR2(45),
  	"RESULTADO" NUMBER(1,0) NOT NULL,
  	"ERROR_DETALLE" VARCHAR2(150),
  	CONSTRAINT "TB_PETICIONES_PK" PRIMARY KEY ("ID")
);

CREATE SEQUENCE "TB_AUDIT_TRANSACCIONES_SEQ"; 

CREATE OR REPLACE TRIGGER "BI_TB_AUDIT_TRANSACCIONES"  
  before insert on "TB_AUDIT_TRANSACCIONES"              
  for each row 
begin  
  if :NEW."ID" is null then
    select "TB_AUDIT_TRANSACCIONES_SEQ".nextval into :NEW."ID" from dual;
  end if;
end;
/

ALTER TRIGGER "BI_TB_AUDIT_TRANSACCIONES" ENABLE;

-- Tabla de auditoria de las firmas

CREATE TABLE "TB_AUDIT_FIRMAS" (
	"ID" NUMBER NOT NULL,
	"ID_TRANSACCION" VARCHAR2(45) NOT NULL,
	"ID_INT_LOTE" VARCHAR2(45), 
  	"OPERACION_CRIPTOGRAFICA" VARCHAR2(20),
  	"FORMATO" VARCHAR2(20) NOT NULL, 
  	"FORMATO_ACTUALIZADO" VARCHAR2(20),
  	"TAMANNO" NUMBER DEFAULT 0,
  	"RESULTADO" NUMBER(1,0) NOT NULL,
  	"ERROR_DETALLE" VARCHAR2(150),
  	CONSTRAINT "TB_PETICIONES_FIRMAS_LOTE_PK" PRIMARY KEY ("ID")
);

CREATE SEQUENCE "TB_AUDIT_FIRMAS_SEQ"; 

CREATE OR REPLACE TRIGGER "BI_TB_AUDIT_FIRMAS"  
  before insert on "TB_AUDIT_FIRMAS"              
  for each row 
begin  
  if :NEW."ID" is null then
    select "TB_AUDIT_FIRMAS_SEQ".nextval into :NEW."ID" from dual;
  end if;
end;
/

ALTER TRIGGER "BI_TB_AUDIT_FIRMAS" ENABLE;

-- Tabla de relacion de responsables y aplicaciones

CREATE TABLE "TB_RESPONSABLE_DE_APLICACIONES" (
  "ID_RESPONSABLES" NUMBER NOT NULL,
  "ID_APLICACIONES" VARCHAR2(48) NOT NULL,
  CONSTRAINT "TB_RESPONSABLES_PK" PRIMARY KEY ("ID_RESPONSABLES","ID_APLICACIONES")
);

-- Agregamos la relacion entre las aplicaciones y los usuarios responsables de ellas
INSERT INTO "TB_RESPONSABLE_DE_APLICACIONES" ("ID_RESPONSABLES", "ID_APLICACIONES")
SELECT "ID_USUARIO", "NOMBRE_USUARIO"
FROM "TB_USUARIOS"
WHERE "FK_ROL" = 2;


-- TABLA de aplicaciones

-- Eliminamos los campos innecesarios de las aplicaciones
ALTER TABLE "TB_APLICACIONES"
DROP  (responsable,  resp_correo, resp_telefono);

-- Agregamos el campo de 'habilitado' dejando las aplicaciones habilitadas por defecto
ALTER TABLE "TB_APLICACIONES"
ADD HABILITADO NUMBER(1,0) DEFAULT 1;

ALTER TABLE "TB_APLICACIONES"
MODIFY ("FK_CERTIFICADO" NUMBER NOT NULL);
