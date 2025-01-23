-- Script de migracion desde FIRe 2.4 a 2.5

-- TABLA DE USUARIOS

-- Agregamos los campos necesarios
ALTER TABLE `tb_usuarios`
ADD `dni` VARCHAR(9) NULL,
ADD `fec_ultimo_acceso` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE `tb_usuarios`
SET `dni` = 'X0000000T';