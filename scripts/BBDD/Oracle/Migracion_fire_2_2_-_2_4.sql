
-- Script de migracion desde FIRe 2.2 o 2.3 a 2.4

-- 1 Crear la tablas nuevas 

-- Tabla para el guardado de las referencias a los servidores de log

CREATE TABLE "TB_SERVIDORES_LOG" (
  "id_servidor" NUMBER NOT NULL ENABLE,
  "nombre" VARCHAR2(45) NOT NULL ENABLE, 
  "url_servicio_log" VARCHAR2(500) NOT NULL ENABLE, 
  "clave" VARCHAR2(45) NOT NULL ENABLE, 
  "verificar_ssl" NUMBER(1,0) NOT NULL ENABLE,
  CONSTRAINT "TB_SERVIDORES_LOG_PK" PRIMARY KEY ("id_servidor") ENABLE, 
  CONSTRAINT "TB_SERVIDORES_LOG_UK1" UNIQUE ("nombre") ENABLE,
  CONSTRAINT "TB_SERVIDORES_LOG_UK2" UNIQUE ("url_servicio_log") ENABLE
);

-- Tabla para el guardado de las estadisticas de las firmas

CREATE TABLE "TB_FIRMAS" (
  "id" NUMBER NOT NULL ENABLE,
  "fecha" TIMESTAMP (6) NOT NULL ENABLE,
  "aplicacion" VARCHAR2(45) NOT NULL ENABLE, 
  "formato" VARCHAR2(20) NOT NULL ENABLE, 
  "formato_mejorado" VARCHAR2(20) NOT NULL ENABLE,
  "algoritmo" VARCHAR2(20) NOT NULL ENABLE, 
  "proveedor" VARCHAR2(45) NOT NULL ENABLE, 
  "navegador" VARCHAR2(45) NOT NULL ENABLE, 
  "correcta" NUMBER(1,0) NOT NULL ENABLE,
  "total" NUMBER DEFAULT 0 NOT NULL ENABLE,
  CONSTRAINT "TB_FIRMAS_PK" PRIMARY KEY ("id") ENABLE
);

-- Tabla para el guardado de las estadisticas de las transacciones

CREATE TABLE "TB_TRANSACCIONES" (
  "id" NUMBER NOT NULL ENABLE,
  "fecha" TIMESTAMP (6) NOT NULL ENABLE,
  "aplicacion" VARCHAR2(45) NOT NULL ENABLE,
  "operacion" VARCHAR2(10) NOT NULL ENABLE,
  "proveedor" VARCHAR2(45) NOT NULL ENABLE,
  "proveedor_forzado" NUMBER(1,0) NOT NULL ENABLE,
  "correcta" NUMBER(1,0) NOT NULL ENABLE,
  "tamanno" NUMBER DEFAULT 0 NOT NULL ENABLE,
  "total" NUMBER DEFAULT 0 NOT NULL ENABLE,
  CONSTRAINT "TB_TRANSACCIONES_PK" PRIMARY KEY ("id") ENABLE
);

-- Tabla para el guardado de los roles

CREATE TABLE "TB_ROLES" (
  "id" NUMBER NOT NULL ENABLE,
  "nombre_rol" VARCHAR2(45) NOT NULL ENABLE,
  "permisos" VARCHAR2(45) DEFAULT NULL,
  CONSTRAINT "TB_ROLES_PK" PRIMARY KEY ("id") ENABLE,
  CONSTRAINT "TB_ROLES_UK1" UNIQUE ("nombre_rol") ENABLE
);

--  Insertamos los permisos del usuario al inicializar la aplicacion

CREATE sequence "TB_ROLES_SEQ"; 
   
CREATE OR REPLACE TRIGGER  "BI_TB_ROLES" 
  before insert on "TB_ROLES"               
  for each row  
begin   
  if :NEW."id" is null then 
    select "TB_ROLES_SEQ".nextval into :NEW."id" from dual; 
  end if; 
end; 



INSERT INTO  "TB_ROLES" ("nombre_rol","permisos") 
VALUES('admin','1,2');

INSERT INTO  "TB_ROLES" ("nombre_rol","permisos") 
VALUES('responsible','1');

INSERT INTO  "TB_ROLES" ("nombre_rol") 
VALUES('contact');


-- Agregamos campos a una tabla
-- Aplicaciones 

CREATE SEQUENCE "TB_APLICACIONES_SEQ"; 

CREATE OR REPLACE TRIGGER  "BI_TB_APLICACIONES" 
  before insert on "TB_APLICACIONES"               
  for each row  
begin   
  if :NEW."ID" is null then 
    select "TB_APLICACIONES_SEQ".nextval into :NEW."ID" from dual; 
  end if; 
end; 


ALTER TABLE "TB_APLICACIONES" ADD ("fk_responsable" NUMBER NOT NULL) ENABLE;
ALTER TABLE "TB_APLICACIONES" ADD "habilitado" NUMBER(1,0) NOT NULL ENABLE;

CREATE INDEX fk_responsable_idx
ON "TB_APLICACIONES" (nombre);


CREATE INDEX "fk_responsable_idx"
ON "TB_APLICACIONES" ("fk_responsable");


-- Usuarios

CREATE SEQUENCE "TB_USUARIOS_SEQ"; 

ALTER TABLE "TB_USUARIOS" ADD "codigo_renovacion" VARCHAR2(90) DEFAULT NULL;
ALTER TABLE "TB_USUARIOS" ADD "fec_renovacion" TIMESTAMP default NULL;
ALTER TABLE "TB_USUARIOS" ADD rest_clave NUMBER(4,0) default 0;

ALTER TABLE "TB_USUARIOS"
ADD CONSTRAINT  "codigo_renovacion_UNQ" UNIQUE("codigo_renovacion");
 

-- Agregar campo "fk_rol"

ALTER TABLE "TB_USUARIOS" ADD "fk_rol" NUMBER  default 0;

 
-- Asignar rol administrador a todos los usuarios actuales, ya que hasta ahora todos los usuarios eran administradores

UPDATE "TB_USUARIOS" SET "fk_rol" = 1;

-- Eliminar campo "rol"

ALTER TABLE "TB_USUARIOS" DROP COLUMN "ROL";

-- Modificamos la columna para que sea null

ALTER TABLE  "TB_USUARIOS" modify (clave VARCHAR2(45) NULL);    



-- Insertamos campos a una tabla
INSERT INTO "TB_USUARIOS" ("NOMBRE_USUARIO", "NOMBRE", "APELLIDOS", "CORREO_ELEC", "TELF_CONTACTO", "fk_rol")
SELECT "ID", "RESPONSABLE", "RESPONSABLE", "RESP_CORREO", "RESP_TELEFONO", 3
FROM "TB_APLICACIONES";         
     


 


-- Creamos las foreign key

ALTER TABLE "TB_APLICACIONES" ADD CONSTRAINT
"TB_APLICACIONES_FK" FOREIGN KEY ("fk_responsable") REFERENCES "TB_USUARIOS" ("ID_USUARIO");

ALTER TABLE "TB_USUARIOS" ADD CONSTRAINT
"TB_USUARIOS_FK" FOREIGN KEY ("fk_rol") REFERENCES "TB_ROLES" ("id");


 
-- Actualizamos los campos de responsable 

UPDATE "TB_APLICACIONES" SET "fk_responsable" = 1;


 -- Creamos el idx del rol

CREATE INDEX fk_rol_idx1
ON "TB_USUARIOS" ("fk_rol");
  
-- Elimina campos de una tabla

ALTER TABLE "TB_APLICACIONES" DROP  (responsable,  resp_correo, resp_telefono);





