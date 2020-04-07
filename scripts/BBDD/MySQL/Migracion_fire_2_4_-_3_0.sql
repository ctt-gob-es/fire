-- Script de migracion desde FIRe 2.4 a 3.0


-- TABLA TB_USUARIOS
  
-- Eliminamos los campos del responsable
ALTER TABLE `TB_USUARIOS`
MODIFY CLAVE VARCHAR(2000);







