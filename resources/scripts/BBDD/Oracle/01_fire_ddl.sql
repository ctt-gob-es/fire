-- ********************************************************
-- **************** Creacion de Tablas ********************
-- ********************************************************

CREATE TABLE "TB_CERTIFICADOS" (
    "ID_CERTIFICADO"   NUMBER NOT NULL,
    "NOMBRE_CERT"      VARCHAR2(45) NOT NULL,
    "FEC_ALTA"         TIMESTAMP NOT NULL,
    "CERTIFICADO"   CLOB,
    "HUELLA" VARCHAR2(45),
    "FEC_INICIO" TIMESTAMP NULL,
    "FEC_CADUCIDAD" TIMESTAMP NULL,
    "SUBJECT" VARCHAR2(4000) NULL,
    constraint  "TB_CERTIFICADOS_PK" primary key ("ID_CERTIFICADO")
);

ALTER TABLE  "TB_CERTIFICADOS" modify
("FEC_ALTA" TIMESTAMP default SYSDATE);

CREATE SEQUENCE "TB_CERTIFICADOS_SEQ"; 

CREATE OR REPLACE TRIGGER "BI_TB_CERTIFICADOS"  
  before insert on "TB_CERTIFICADOS"              
  for each row 
begin  
  if :NEW."ID_CERTIFICADO" is null then
    select "TB_CERTIFICADOS_SEQ".nextval into :NEW."ID_CERTIFICADO" from dual;
  end if;
end;
/

ALTER TRIGGER "BI_TB_CERTIFICADOS" ENABLE;

CREATE TABLE "TB_APLICACIONES" (
  "ID" VARCHAR2(48) NOT NULL,
  "NOMBRE" VARCHAR2(45) NOT NULL,
  "FECHA_ALTA" TIMESTAMP NOT NULL,
  "HABILITADO" NUMBER(1,0) DEFAULT 1, 
  constraint  "TB_APLICACIONES_PK" primary key ("ID")
); 

ALTER TABLE  "TB_APLICACIONES" modify
("FECHA_ALTA" TIMESTAMP default SYSDATE);

CREATE TABLE  "TB_USUARIOS" 
   ("ID_USUARIO" NUMBER NOT NULL, 
	"NOMBRE_USUARIO" VARCHAR2(30) NOT NULL, 
	"CLAVE" VARCHAR2(2000), 
	"NOMBRE" VARCHAR2(45) NOT NULL, 
	"APELLIDOS" VARCHAR2(120) NOT NULL, 
	"CORREO_ELEC" VARCHAR2(45), 
	"TELF_CONTACTO" VARCHAR2(45), 
	"FK_ROL" NUMBER NOT NULL,
	"FEC_ALTA" TIMESTAMP (6) DEFAULT SYSDATE, 
	"USU_DEFECTO" NUMBER(1,0) DEFAULT 0, 
	"CODIGO_RENOVACION" VARCHAR2(100) DEFAULT NULL,
	"FEC_RENOVACION" TIMESTAMP DEFAULT NULL,
	"REST_CLAVE" NUMBER(1,0) DEFAULT 0,	
	"DNI" VARCHAR(9) NULL,
	"FEC_ULTIMO_ACCESO" TIMESTAMP NOT NULL DEFAULT SYSDATE,
	 CONSTRAINT "TB_USUARIOS_PK" PRIMARY KEY ("ID_USUARIO"), 
	 CONSTRAINT "TB_USUARIOS_UK1" UNIQUE ("NOMBRE_USUARIO"),
	 CONSTRAINT "TB_USUARIOS_UK2" UNIQUE ("CODIGO_RENOVACION")
);

CREATE SEQUENCE "TB_USUARIOS_SEQ"; 

CREATE OR REPLACE TRIGGER  "BI_TB_USUARIOS" 
  before insert on "TB_USUARIOS"               
  for each row  
begin   
  if :NEW."ID_USUARIO" is null then 
    select "TB_USUARIOS_SEQ".nextval into :NEW."ID_USUARIO" from dual; 
  end if; 
end;
/

ALTER TRIGGER "BI_TB_USUARIOS" ENABLE;


-- Tabla para el guardado de los roles

CREATE TABLE "TB_ROLES" (
  "ID" NUMBER NOT NULL,
  "NOMBRE_ROL" VARCHAR2(45) NOT NULL,
  "PERMISOS"   VARCHAR2(45),
  CONSTRAINT "TB_ROLES_PK" PRIMARY KEY ("ID"),
  CONSTRAINT "TB_ROLES_UK" UNIQUE ("NOMBRE_ROL")
);

ALTER TABLE "TB_USUARIOS" ADD CONSTRAINT
"TB_USUARIOS_FK" FOREIGN KEY ("FK_ROL") REFERENCES "TB_ROLES" ("ID");

-- Tabla de relacion de responsables y aplicaciones

CREATE TABLE "TB_RESPONSABLE_DE_APLICACIONES" (
  "ID_RESPONSABLES" NUMBER NOT NULL,
  "ID_APLICACIONES" VARCHAR2(48) NOT NULL,
  CONSTRAINT "TB_RESPONSABLES_PK" PRIMARY KEY ("ID_RESPONSABLES","ID_APLICACIONES")
);

-- Tabla de relacion de certificados y aplicaciones

CREATE TABLE "TB_CERTIFICADOS_DE_APLICACION" (
  "ID_CERTIFICADOS" NUMBER NOT NULL,
  "ID_APLICACIONES" VARCHAR2(48) NOT NULL,
  CONSTRAINT "TB_CERT_APP_PK" PRIMARY KEY ("ID_CERTIFICADOS","ID_APLICACIONES")
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
  "NAVEGADOR" VARCHAR2(20) NOT NULL, 
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
  "ID_PLANIFICADOR" NUMBER(19) NOT NULL,
  "NOMBRE_PROGRAMADOR" VARCHAR2(50) NOT NULL,
  CONSTRAINT "PK_ID_PROGRAMADOR" PRIMARY KEY ("ID_PROGRAMADOR"),
  CONSTRAINT "UNICO_NOMBRE_PROGRAMADOR" UNIQUE ("NOMBRE_PROGRAMADOR"),
  CONSTRAINT "FK_PLANIFICADOR" FOREIGN KEY ("ID_PLANIFICADOR") REFERENCES "TB_PLANIFICADOR" ("ID_PLANIFICADOR")
);

CREATE SEQUENCE SQ_PLANIFICADOR START WITH 1 INCREMENT BY 1 NOMAXVALUE NOCYCLE;

