-- ********************************************************
-- **************** Creación de Tablas ********************
-- ********************************************************

CREATE TABLE `tb_aplicaciones` (
  `id` varchar(48) NOT NULL,
  `nombre` varchar(45) NOT NULL,
  `responsable` varchar(45) NOT NULL,
  `resp_correo` varchar(45) DEFAULT NULL,
  `resp_telefono` varchar(30) DEFAULT NULL,
  `fecha_alta` datetime NOT NULL,
  `fk_certificado` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tb_certificados` (
  `id_certificado` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_cert` varchar(45) NOT NULL,
  `fec_alta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
  `fec_alta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'fecha de alta del usuario.',
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

 /*
Creación de tabla tb_servidores_log para la gestión de logs
*/

CREATE TABLE `tb_servidores_log` (
  `id_servidor` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) NOT NULL,
  `url_servicio_log` varchar(500) NOT NULL,
  `clave` varchar(45) NOT NULL,
  PRIMARY KEY (`id_servidor`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre`),
  UNIQUE KEY `url_servicio_log_UNIQUE` (`url_servicio_log`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

/*
Creación  de tabla algoritmos para gestión de estadísticas
*/
CREATE TABLE `tb_algoritmos` (
  `id_algoritmo` int(11) NOT NULL ,
  `nombre` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id_algoritmo`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;



/*
Creación  de tabla formatos para gestión de estadísticas
*/

CREATE TABLE `tb_formatos` (
  `id_formato` int(11) NOT NULL ,
  `nombre` varchar(45) NOT NULL,
  PRIMARY KEY (`id_formato`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


/*
Creación  de tabla formatos mejorados para gestión de estadísticas
*/
CREATE TABLE `tb_formatos_mejorados` (
  `id_formato_mejorado` int(11) NOT NULL,
  `nombre` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id_formato_mejorado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



/*
Creación  de tabla proveedores para gestión de estadísticas 
*/

CREATE TABLE `tb_proveedores` (
  `id_proveedor` int(11) NOT NULL,
  `nombre` varchar(45) NOT NULL,
  `conector` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id_proveedor`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*
Creación  de tabla navegadores para gestión de estadísticas 
*/
CREATE TABLE `tb_navegadores` (
  `id_navegador` int(11) NOT NULL,
  `nombre` varchar(45) NOT NULL,
  PRIMARY KEY (`id_navegador`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



/*
Creación  de tabla firmas para gestión de estadísticas 
*/

CREATE TABLE `tb_firmas` (
  `fecha` datetime NOT NULL,
  `id_formato` int(11) NOT NULL,
  `id_formato_mejorado` int(11) DEFAULT NULL,
  `id_algoritmo` int(11) NOT NULL,
  `id_proveedor` int(11) NOT NULL,
  `id_navegador` int(11) NOT NULL,
  `version_navegador` varchar(45) NOT NULL,
  `correcta` enum('false','true') NOT NULL DEFAULT 'false',
  `id_transaccion` varchar(45) NOT NULL,
  `tamanno` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`fecha`,`id_formato`,`id_algoritmo`,`id_proveedor`,`id_navegador`),
  KEY `FK_ALGORITMO_idx` (`id_algoritmo`),
  KEY `FK_FORMATO_idx` (`id_formato`),
  KEY `FK_PROVEEDORES_idx` (`id_proveedor`),
  KEY `FK_NAVEGADOR_idx` (`id_navegador`),
  CONSTRAINT `FK_ALGORITMO` FOREIGN KEY (`id_algoritmo`) REFERENCES `tb_algoritmos` (`id_algoritmo`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_FORMATO` FOREIGN KEY (`id_formato`) REFERENCES `tb_formatos` (`id_formato`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_NAVEGADOR` FOREIGN KEY (`id_navegador`) REFERENCES `tb_navegadores` (`id_navegador`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_PROVEEDORES` FOREIGN KEY (`id_proveedor`) REFERENCES `tb_proveedores` (`id_proveedor`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*
Creación  de tabla Operaciones para gestión de estadísticas  
*/
CREATE TABLE `tb_operaciones` (
  `id_operacion` int(11) NOT NULL AUTO_INCREMENT COMMENT 'autonumérico identificativo único de la operación',
  `nombre` varchar(45) NOT NULL COMMENT 'Nombre único de la operación.',
  PRIMARY KEY (`id_operacion`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;


/*
Creación  de tabla transacciones para gestión de estadísticas  
*/

CREATE TABLE `tb_transacciones` (
  `fecha` datetime NOT NULL,
  `id_aplicacion` varchar(45) NOT NULL,
  `id_operacion` int(11) NOT NULL,
  `id_proveedor` int(11) NOT NULL,
  `proveedor_forzado` enum('false','true') NOT NULL DEFAULT 'false',
  `correcta` enum('true','false') NOT NULL DEFAULT 'false',
  `id_transaccion` varchar(45) NOT NULL,
  PRIMARY KEY (`fecha`,`id_transaccion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 
  