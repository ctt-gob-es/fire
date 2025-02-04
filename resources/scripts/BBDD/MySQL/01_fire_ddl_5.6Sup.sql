-- ********************************************************
-- **************** Creación de Tablas ********************
-- ********************************************************
SET character_set_client = UTF8MB4 ;
CREATE TABLE `tb_certificados` (
  `id_certificado` int(11) AUTO_INCREMENT,
  `nombre_cert` varchar(45) NOT NULL,
  `fec_alta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `certificado` varchar(5000) DEFAULT NULL,
  `huella` varchar(45) DEFAULT NULL,
  `fec_inicio` datetime NULL,
  `fec_caducidad` datetime NULL,
  `subject` varchar(4000) NULL,
  PRIMARY KEY (`id_certificado`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4 COMMENT='tabla de certificados';


CREATE TABLE `tb_aplicaciones` (
  `id` varchar(48) NOT NULL,
  `nombre` varchar(45) NOT NULL,
  `fecha_alta` datetime NOT NULL,
  `habilitado` tinyint(4) DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;


CREATE TABLE `tb_usuarios` (
  `id_usuario` int(11) NOT NULL AUTO_INCREMENT COMMENT 'auto-numérico identificativo único',
  `nombre_usuario` varchar(30) NOT NULL COMMENT 'Nombre con el que se identifica en la aplicación',
  `clave` varchar(2000) DEFAULT NULL COMMENT 'clave condificada con SHA256, con la que se registra el usuario',
  `nombre` varchar(45) NOT NULL COMMENT 'Nombre completo del usuario',
  `apellidos` varchar(120) NOT NULL COMMENT 'Apellidos del usuario',
  `correo_elec` varchar(45) DEFAULT NULL COMMENT 'Correo electrónico',
  `telf_contacto` varchar(45) DEFAULT NULL COMMENT 'Teléfono de contacto',
  `fk_rol` int(11) NOT NULL,
  `fec_alta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de alta del usuario.',
  `usu_defecto` tinyint(4) NOT NULL DEFAULT '0',
  `codigo_renovacion` varchar(100) DEFAULT NULL,
  `fec_renovacion` datetime DEFAULT NULL,
  `rest_clave` tinyint(4) DEFAULT '0',
  `dni` varchar(9) NULL,
  `fec_ultimo_acceso` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `nombre_usuario_UNIQUE` (`nombre_usuario`),
  UNIQUE KEY `codigo_renovacion_UNIQUE` (`codigo_renovacion`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4;


CREATE TABLE `tb_servidores_log` (
  `id_servidor` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) NOT NULL,
  `url_servicio_log` varchar(500) NOT NULL,
  `clave` varchar(45) NOT NULL,
  `verificar_ssl` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id_servidor`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre`),
  UNIQUE KEY `url_servicio_log_UNIQUE` (`url_servicio_log`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4;


CREATE TABLE `tb_firmas` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fecha` datetime DEFAULT NULL COMMENT 'Fecha de la operacion',
  `aplicacion` varchar(45) DEFAULT NULL COMMENT 'Aplicacion que solicito la operacion',
  `formato` varchar(20) DEFAULT NULL COMMENT 'Formato de firma',
  `formato_mejorado` varchar(20) DEFAULT NULL COMMENT 'Formato longevo al que actualizar',
  `algoritmo` varchar(20) DEFAULT NULL COMMENT 'Algoritmo de firma',
  `proveedor` varchar(45) DEFAULT NULL COMMENT 'Nombre del proveedor de certificados utilizado',
  `navegador` varchar(20) NOT NULL COMMENT 'Navegador web',
  `correcta` tinyint(1) DEFAULT NULL COMMENT 'Si la firma es correcta o no',
  `total` int(11) DEFAULT NULL COMMENT 'Numero de operaciones con la esta configuracion',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4;


CREATE TABLE `tb_transacciones` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL COMMENT 'Fecha de la operacion',
  `aplicacion` varchar(45) NOT NULL COMMENT 'Aplicacion que solicito la operacion',
  `operacion` varchar(10) NOT NULL COMMENT 'Tipo de operacion',
  `proveedor` varchar(45) NOT NULL COMMENT 'Nombre del proveedor de firma',
  `proveedor_forzado` tinyint(1) DEFAULT '0' COMMENT 'Si solo habia un proveedor o si la aplicacion forzo que se usase ese',
  `correcta` tinyint(1) DEFAULT '0' COMMENT 'Si termino correctamente o no',
  `tamanno` int(11) DEFAULT '0' COMMENT 'Tamano total de los datos procesados',
  `total` int(11) DEFAULT '0' COMMENT 'Numero de transacciones con esta configuracion',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4;


CREATE TABLE `tb_roles` (
  `id` int(11) NOT NULL,
  `nombre_rol` varchar(45) NOT NULL,
  `permisos` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;


CREATE TABLE `tb_responsable_de_aplicaciones` (
  `id_responsables` int(11) NOT NULL,
  `id_aplicaciones` varchar(48) NOT NULL,
  PRIMARY KEY (`id_responsables`,`id_aplicaciones`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;


CREATE TABLE `tb_certificados_de_aplicacion` (
  `id_certificados` int(11) NOT NULL,
  `id_aplicaciones` varchar(48) NOT NULL,
  PRIMARY KEY (`id_certificados`,`id_aplicaciones`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;


-- Creamos la relacion entre las tablas de usuario y roles
ALTER TABLE `tb_usuarios` 
ADD CONSTRAINT `fk_rol`
FOREIGN KEY (`fk_rol`)
  REFERENCES `tb_roles` (`id`)
ON DELETE RESTRICT
 ON UPDATE CASCADE;


CREATE TABLE `tb_audit_transacciones` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL,
  `id_aplicacion` varchar(48) NOT NULL,
  `nombre_aplicacion` varchar(48) NOT NULL,
  `id_transaccion` varchar(45) NOT NULL,
  `operacion` varchar(10) NOT NULL,
  `operacion_criptografica` varchar(10) NOT NULL,
  `formato` varchar(20) NOT NULL, 
  `formato_actualizado` varchar(20),
  `algoritmo` varchar(20) NOT NULL, 
  `proveedor` varchar(45) NOT NULL,
  `proveedor_forzado` tinyint(1) NOT NULL,
  `navegador` varchar(20) NOT NULL, 
  `tamanno` int(11) DEFAULT 0,
  `nodo` varchar(45),
  `resultado` tinyint(1) NOT NULL,
  `error_detalle` varchar(150),
  PRIMARY KEY (`id`),
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4;

CREATE TABLE `tb_audit_firmas` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_transaccion` varchar(45) NOT NULL,
  `operacion_criptografica` varchar(10) NOT NULL,
  `formato` varchar(20) NOT NULL, 
  `formato_actualizado` varchar(20),
  `tamanno` int(11) DEFAULT 0,
  `resultado` tinyint(1) NOT NULL,
  `error_detalle` varchar(150),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4;