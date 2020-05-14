-- Script de migracion desde FIRe 2.4 a 3.0

-- TABLA TB_USUARIOS
  
-- Aumentamos el tamaño del campo CLAVE a 2000
ALTER TABLE TB_USUARIOS 
MODIFY CLAVE VARCHAR2(2000);

-- Aumentamos el tamaño del campo CODIGO_RENOVACION a 100
ALTER TABLE `TB_USUARIOS`
MODIFY CODIGO_RENOVACION VARCHAR2(100);

-- Añadimos las secuencias a todas las tablas necesarias

-- Secuencia tabla TB_APLICACIONES
CREATE SEQUENCE "TB_APLICACIONES_SEQ"; 

CREATE OR REPLACE TRIGGER "BI_TB_APLICACIONES"  
  before insert on "TB_APLICACIONES"              
  for each row 
begin  
  if :NEW."ID" is null then
    select "TB_APLICACIONES_SEQ".nextval into :NEW."ID" from dual;
  end if;
end;
/

ALTER TRIGGER "BI_TB_APLICACIONES" ENABLE;

-- Secuencia tabla TB_SERVIDORES_LOG
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

-- Secuencia tabla TB_FIRMAS
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

-- Secuencia tabla TB_TRANSACCIONES
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
