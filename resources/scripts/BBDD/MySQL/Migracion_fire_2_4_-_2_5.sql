-- Script de migracion desde FIRe 2.4 a 2.5

-- TABLA DE USUARIOS

-- Agregamos los campos necesarios
ALTER TABLE `tb_usuarios`
ADD `dni` VARCHAR(9) NULL,
ADD `fec_ultimo_acceso` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE `tb_usuarios`
SET `dni` = 'X0000000T';


-- TABLA DE CERTIFICADOS

ALTER TABLE `tb_certificados` CHANGE COLUMN `cert_principal` `certificado` varchar(5000);
ALTER TABLE `tb_certificados` CHANGE COLUMN `huella_principal` `huella` varchar(45);
ALTER TABLE `tb_certificados` DROP COLUMN `cert_backup`;
ALTER TABLE `tb_certificados` DROP COLUMN `huella_backup`;

-- TABLA DE CERTIFICADOS DE APLICACION

CREATE TABLE `tb_certificados_de_aplicacion` (
  `id_certificados` int(11) NOT NULL,
  `id_aplicaciones` varchar(48) NOT NULL,
  PRIMARY KEY (`id_certificados`,`id_aplicaciones`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- TABLA DE APLICACIONES

ALTER TABLE `tb_aplicaciones` DROP COLUMN `fk_certificado`;