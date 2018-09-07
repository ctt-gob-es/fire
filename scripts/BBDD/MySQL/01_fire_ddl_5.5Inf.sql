-- ********************************************************
-- **************** Creación de Tablas ********************
-- ********************************************************

CREATE TABLE `tb_aplicaciones` (
  `id` varchar(48) NOT NULL,
  `nombre` varchar(45) NOT NULL,
  `responsable` varchar(45) NOT NULL,
  `resp_correo` varchar(45) DEFAULT NULL,
  `resp_telefono` varchar(30) DEFAULT NULL,
  `fecha_alta` TIMESTAMP NOT NULL,
  `fk_certificado` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tb_certificados` (
  `id_certificado` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_cert` varchar(45) NOT NULL,
  `fec_alta` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cert_principal` varchar(5000) DEFAULT NULL,
  `cert_backup` varchar(5000) DEFAULT NULL,
  `huella_principal` varchar(45) DEFAULT NULL,
  `huella_backup` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id_certificado`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='tabla de certificados';


CREATE TABLE `tb_usuarios` (
  `id_usuario` int(11) NOT NULL AUTO_INCREMENT COMMENT 'auto-numérico identificativo único',
  `nombre_usuario` varchar(30) NOT NULL COMMENT 'Nombre con el que se identifica en la aplicación',
  `clave` varchar(45) NOT NULL COMMENT 'clave condificada con SHA256, con la que se registra el usuario',
  `nombre` varchar(45) NOT NULL COMMENT 'Nombre completo del usuario',
  `apellidos` varchar(120) NOT NULL COMMENT 'Apellidos del usuario',
  `correo_elec` varchar(45) DEFAULT NULL COMMENT 'Correo electrónico',
  `fec_alta` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'fecha de alta del usuario.',
  `telf_contacto` varchar(45) DEFAULT NULL COMMENT 'Teléfono de contacto',
  `rol` varchar(45) NOT NULL DEFAULT 'admin' COMMENT 'Papel (rol) que desempeña dentro de la aplicación',
  `usu_defecto` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `nombre_usuario_UNIQUE` (`nombre_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


ALTER TABLE `tb_aplicaciones` 
ADD INDEX `fk_certificado_idx` (`fk_certificado` ASC);

ALTER TABLE `tb_aplicaciones` 
ADD CONSTRAINT `fk_certificado`
FOREIGN KEY (`fk_certificado`)
  REFERENCES `tb_certificados` (`id_certificado`)
ON DELETE RESTRICT
  ON UPDATE CASCADE;
