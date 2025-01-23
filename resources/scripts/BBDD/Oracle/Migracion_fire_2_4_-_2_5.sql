-- Script de migracion desde FIRe 2.4 a 2.5


-- TABLA de usuarios

-- Agregamos un campo DNI con un valor preestablecido para las entradas existentes
ALTER TABLE "TB_USUARIOS" ADD DNI VARCHAR(9) NULL;
UPDATE "TB_USUARIOS" SET DNI = 'X0000000T' WHERE id_usuario = 1;
-- Agregamos un campo FEC_ULTIMO_ACCESO con un valor preestablecido para las entradas existentes
ALTER TABLE "TB_USUARIOS" ADD FEC_ULTIMO_ACCESO TIMESTAMP DEFAULT SYSDATE;