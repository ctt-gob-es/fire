-- Script de migracion desde FIRe 2.4 a 3.0


-- TABLA TB_USUARIOS
  
-- Aumentamos el tamaño del campo CLAVE a 2000
ALTER TABLE `TB_USUARIOS`
MODIFY CLAVE VARCHAR(2000);

-- Aumentamos el tamaño del campo CODIGO_RENOVACION a 100
ALTER TABLE `TB_USUARIOS`
MODIFY CODIGO_RENOVACION VARCHAR(100);







