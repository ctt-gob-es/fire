-- ********************************************************
-- **************** Creación de Tablas ********************
-- ********************************************************

CREATE TABLE tb_aplicaciones (
	id varchar(48) PRIMARY KEY,
	nombre varchar(45) NOT NULL,
	responsable varchar(45) NOT NULL,
	resp_correo varchar(45) DEFAULT NULL,
	resp_telefono varchar(30) DEFAULT NULL,
	fecha_alta datetime NOT NULL,
	cer varchar(5000) NOT NULL,
	huella varchar(28) NOT NULL
);

CREATE TABLE tb_configuracion (
	parametro varchar(30) PRIMARY KEY,
	valor varchar(45) DEFAULT NULL
);

CREATE TABLE `tb_usuarios` 
(
  	`id_usuario` int(11) NOT NULL AUTO_INCREMENT COMMENT 'auto-numérico identificativo único',
  
	`nombre_usuario` varchar(45) NOT NULL COMMENT 'Nombre con el que se identifica en la aplicación',
  
	`clave` varchar(45) NOT NULL COMMENT 'clave condificada con SHA256, con la que se registra el usuario',
  
	`nombre` varchar(45) NOT NULL COMMENT 'Nombre completo del usuario',
  
	`apellidos` varchar(120) NOT NULL COMMENT 'Apellidos del usuario',
  
	`correo_elec` varchar(80) DEFAULT NULL COMMENT 'Correo electrónico',
  
	`fec_alta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'fecha de alta del usuario.',
  
	`telf_contacto` varchar(20) DEFAULT NULL COMMENT 'Teléfono de contacto',
  
	PRIMARY KEY (`id_usuario`),
  
	UNIQUE KEY `nombre_usuario_UNIQUE` (`nombre_usuario`)
) 
	ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


