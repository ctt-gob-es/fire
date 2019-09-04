-- ********************************************************
-- **************** Creación de Tablas ********************
-- ********************************************************

CREATE TABLE `tb_certificados` (
  `id_certificado` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_cert` varchar(45) NOT NULL,
  `fec_alta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cert_principal` varchar(5000) DEFAULT NULL,
  `cert_backup` varchar(5000) DEFAULT NULL,
  `huella_principal` varchar(45) DEFAULT NULL,
  `huella_backup` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id_certificado`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='tabla de certificados'


CREATE TABLE `tb_aplicaciones` (
  `id` varchar(48) NOT NULL,
  `nombre` varchar(45) NOT NULL,
  `fk_responsable` int(11) NOT NULL,
  `fecha_alta` datetime NOT NULL,
  `fk_certificado` int(11) NOT NULL,
  `habilitado` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_certificado_idx` (`fk_certificado`),
  KEY `fk_responsable_idx` (`nombre`),
  KEY `fk_responsable_idx1` (`fk_responsable`),
  CONSTRAINT `fk_certificado` FOREIGN KEY (`fk_certificado`) REFERENCES `tb_certificados` (`id_certificado`) ON UPDATE CASCADE,
  CONSTRAINT `fk_responsable` FOREIGN KEY (`fk_responsable`) REFERENCES `tb_usuarios` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8


CREATE TABLE `tb_usuarios` (
  `id_usuario` int(11) NOT NULL AUTO_INCREMENT COMMENT 'auto-numérico identificativo único',
  `nombre_usuario` varchar(30) NOT NULL COMMENT 'Nombre con el que se identifica en la aplicación',
  `clave` varchar(45) DEFAULT NULL COMMENT 'clave condificada con SHA256, con la que se registra el usuario',
  `nombre` varchar(45) NOT NULL COMMENT 'Nombre completo del usuario',
  `apellidos` varchar(120) NOT NULL COMMENT 'Apellidos del usuario',
  `correo_elec` varchar(45) DEFAULT NULL COMMENT 'Correo electrónico',
  `fk_rol` int(11) NOT NULL,
  `fec_alta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'fecha de alta del usuario.',
  `telf_contacto` varchar(45) DEFAULT NULL COMMENT 'Teléfono de contacto',
  `usu_defecto` tinyint(4) NOT NULL DEFAULT '0',
  `codigo_renovacion` varchar(90) DEFAULT NULL,
  `fec_renovacion` datetime DEFAULT NULL,
  `rest_clave` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `nombre_usuario_UNIQUE` (`nombre_usuario`),
  UNIQUE KEY `codigo_renovacion_UNIQUE` (`codigo_renovacion`),
  KEY `fk_role_idx` (`fk_rol`),
  CONSTRAINT `fk_role` FOREIGN KEY (`fk_rol`) REFERENCES `tb_roles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8;


CREATE TABLE `tb_servidores_log` (
  `id_servidor` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) NOT NULL,
  `url_servicio_log` varchar(500) NOT NULL,
  `clave` varchar(45) NOT NULL,
  `verificar_ssl` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id_servidor`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre`),
  UNIQUE KEY `url_servicio_log_UNIQUE` (`url_servicio_log`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE `tb_firmas` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fecha` TIMESTAMP DEFAULT NULL COMMENT 'Fecha de la operacion',
  `aplicacion` varchar(45) DEFAULT NULL COMMENT 'Aplicacion que solicito la operacion',
  `formato` varchar(20) DEFAULT NULL COMMENT 'Formato de firma',
  `formato_mejorado` varchar(20) DEFAULT NULL COMMENT 'Formato longevo al que actualizar',
  `algoritmo` varchar(20) DEFAULT NULL COMMENT 'Algoritmo de firma',
  `proveedor` varchar(45) DEFAULT NULL COMMENT 'Nombre del proveedor de certificados utilizado',
  `navegador` varchar(20) NOT NULL COMMENT 'Navegador web',
  `correcta` tinyint(1) DEFAULT NULL COMMENT 'Si la firma es correcta o no',
  `total` int(11) DEFAULT NULL COMMENT 'Numero de operaciones con la esta configuracion',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE `tb_transacciones` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fecha` TIMESTAMP DEFAULT NULL COMMENT 'Fecha de la operacion',
  `aplicacion` varchar(45) DEFAULT NULL COMMENT 'Aplicacion que solicito la operacion',
  `operacion` varchar(10) NOT NULL COMMENT 'Tipo de operacion',
  `proveedor` varchar(45) DEFAULT NULL COMMENT 'Nombre del proveedor de firma',
  `proveedor_forzado` tinyint(1) DEFAULT '0' COMMENT 'Si solo habia un proveedor o si la aplicacion forzo que se usase ese',
  `correcta` tinyint(1) DEFAULT '0' COMMENT 'Si termino correctamente o no',
  `tamanno` int(11) DEFAULT '0' COMMENT 'Tamano total de los datos procesados',
  `total` int(11) DEFAULT '1' COMMENT 'Numero de transacciones con esta configuracion',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE `tb_roles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_rol` varchar(45) NOT NULL,
  `permisos` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

