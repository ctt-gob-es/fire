CREATE TABLE `tb_algoritmos` (
  `id_algoritmo` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id_algoritmo`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;


CREATE TABLE `tb_certificados` (
  `id_certificado` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_cert` varchar(45) NOT NULL,
  `fec_alta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cert_principal` varchar(5000) DEFAULT NULL,
  `cert_backup` varchar(5000) DEFAULT NULL,
  `huella_principal` varchar(45) DEFAULT NULL,
  `huella_backup` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id_certificado`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8 COMMENT='tabla de certificados';


CREATE TABLE `tb_aplicaciones` (
  `id` varchar(48) NOT NULL,
  `nombre` varchar(45) NOT NULL,
  `responsable` varchar(45) NOT NULL,
  `resp_correo` varchar(45) DEFAULT NULL,
  `resp_telefono` varchar(30) DEFAULT NULL,
  `fecha_alta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fk_certificado` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_certificado_idx` (`fk_certificado`),
  CONSTRAINT `fk_certificado` FOREIGN KEY (`fk_certificado`) REFERENCES `tb_certificados` (`id_certificado`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tb_configuracion` (
  `parametro` varchar(30) NOT NULL COMMENT 'Nombre con el que se identificael parámetro de configuración',
  `valor` varchar(45) NOT NULL COMMENT 'Valor que indica el parámetro de configuración',
  PRIMARY KEY (`parametro`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tb_firmas` (
  `id_firma` bigint(19) NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL,
  `formato` varchar(45) NOT NULL,
  `formato_mejorado` varchar(45) DEFAULT NULL,
  `algoritmo` varchar(45) NOT NULL,
  `proveedor` varchar(45) NOT NULL,
  `navegador` varchar(45) NOT NULL,
  `correcta` enum('false','true') NOT NULL DEFAULT 'false',
  `total` bigint(20) NOT NULL DEFAULT '0',
  `aplicacion` varchar(45) NOT NULL,
  PRIMARY KEY (`id_firma`)
) ENGINE=InnoDB AUTO_INCREMENT=527 DEFAULT CHARSET=utf8;

CREATE TABLE `tb_formatos` (
  `id_formato` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) NOT NULL,
  PRIMARY KEY (`id_formato`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;

CREATE TABLE `tb_formatos_mejorados` (
  `id_formato_mejorado` int(11) NOT NULL,
  `nombre` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id_formato_mejorado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tb_navegadores` (
  `id_navegador` int(11) NOT NULL,
  `nombre` varchar(45) NOT NULL,
  PRIMARY KEY (`id_navegador`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tb_operaciones` (
  `id_operacion` int(11) NOT NULL AUTO_INCREMENT COMMENT 'autonumérico identificativo único de la operación',
  `nombre` varchar(45) NOT NULL COMMENT 'Nombre único de la operación.',
  PRIMARY KEY (`id_operacion`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;

CREATE TABLE `tb_proveedores` (
  `id_proveedor` int(11) NOT NULL,
  `nombre` varchar(45) NOT NULL,
  `conector` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id_proveedor`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tb_servidores_log` (
  `id_servidor` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) NOT NULL,
  `url_servicio_log` varchar(500) NOT NULL,
  `clave` varchar(45) NOT NULL,
  `verificar_ssl` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_servidor`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre`),
  UNIQUE KEY `url_servicio_log_UNIQUE` (`url_servicio_log`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

CREATE TABLE `tb_transacciones` (
  `id_transaccion` bigint(19) NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL,
  `aplicacion` varchar(45) NOT NULL,
  `operacion` varchar(45) NOT NULL,
  `proveedor` varchar(45) NOT NULL,
  `proveedor_forzado` enum('false','true') NOT NULL DEFAULT 'false',
  `tamanno` bigint(20) NOT NULL DEFAULT '0',
  `correcta` enum('true','false') NOT NULL DEFAULT 'false',
  `total` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_transaccion`)
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8;

CREATE TABLE `tb_usuarios` (
  `id_usuario` int(11) NOT NULL AUTO_INCREMENT COMMENT 'auto-numérico identificativo único',
  `nombre_usuario` varchar(30) NOT NULL COMMENT 'Nombre con el que se identifica en la aplicación',
  `clave` varchar(45) NOT NULL COMMENT 'clave codificada con SHA256, con la que se registra el usuario',
  `nombre` varchar(45) NOT NULL COMMENT 'Nombre completo del usuario',
  `apellidos` varchar(120) NOT NULL COMMENT 'Apellidos del usuario',
  `correo_elec` varchar(45) DEFAULT NULL COMMENT 'Correo electrónico',
  `fec_alta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'fecha de alta del usuario.',
  `telf_contacto` varchar(45) DEFAULT NULL COMMENT 'Teléfono de contacto',
  `rol` varchar(45) NOT NULL DEFAULT 'admin' COMMENT 'Papel (rol) que desempeña dentro de la aplicación',
  `usu_defecto` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `nombre_usuario_UNIQUE` (`nombre_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8;
