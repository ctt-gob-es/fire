-- Script de migracion desde FIRe 2.2/2.3 a 2.4


-- Tabla para el guardado de las referencias a los servidores de log
SET character_set_client = UTF8MB4 ;
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

-- Tabla de las estadisticas de las firmas

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

-- Tabla de estadisticas de las transacciones

CREATE TABLE `tb_transacciones` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fecha` datetime DEFAULT NULL COMMENT 'Fecha de la operacion',
  `aplicacion` varchar(45) DEFAULT NULL COMMENT 'Aplicacion que solicito la operacion',
  `operacion` varchar(10) NOT NULL COMMENT 'Tipo de operacion',
  `proveedor` varchar(45) DEFAULT NULL COMMENT 'Nombre del proveedor de firma',
  `proveedor_forzado` tinyint(1) DEFAULT '0' COMMENT 'Si solo habia un proveedor o si la aplicacion forzo que se usase ese',
  `correcta` tinyint(1) DEFAULT '0' COMMENT 'Si termino correctamente o no',
  `tamanno` int(11) DEFAULT '0' COMMENT 'Tamano total de los datos procesados',
  `total` int(11) DEFAULT '1' COMMENT 'Numero de transacciones con esta configuracion',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4;


-- Tabla de roles

CREATE TABLE `tb_roles` (
  `id` int(11) NOT NULL,
  `nombre_rol` varchar(45) NOT NULL,
  `permisos` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4;


--   Insertamos los permisos de los roles
INSERT INTO `tb_roles` (`id`,`nombre_rol`,`permisos`) 
VALUES (1,'admin','1,2'),
	   (2,'responsible','2'),
	   (3,'contact', NULL);



-- TABLA DE USUARIOS

-- Agregamos los campos necesarios
ALTER TABLE `tb_usuarios`
ADD `fk_rol` int(11) AFTER `telf_contacto`,
ADD `codigo_renovacion` VARCHAR(100) UNIQUE DEFAULT NULL,
ADD `fec_renovacion` datetime DEFAULT NULL,
ADD `rest_clave` tinyint(4) DEFAULT 0;

UPDATE `tb_usuarios`
SET `fk_rol` = 1;

ALTER TABLE `tb_usuarios`
MODIFY `fk_rol` int(11) NOT NULL;
 
-- Eliminamos los campos sobrantes
ALTER TABLE `tb_usuarios`
DROP rol;

-- Modificamos la columna de clave para que pueda ser nula (los usuarios que no tengan permisos de acceso)
ALTER TABLE `tb_usuarios`
MODIFY `clave` VARCHAR (2000) NULL COMMENT 'clave condificada con SHA256, con la que se registra el usuario';

-- Creamos la clave foranea con la tabla de roles
ALTER TABLE `tb_usuarios` 
ADD CONSTRAINT `fk_rol`
FOREIGN KEY (`fk_rol`)
  REFERENCES `tb_roles` (`id`)
ON DELETE RESTRICT
 ON UPDATE CASCADE;

-- Insertamos como usuarios a los responsables declarados en las aplicaciones
INSERT INTO `tb_usuarios` (`nombre_usuario`, `nombre`, `apellidos`, `correo_elec`, `telf_contacto`, `fec_alta`, `fk_rol`)
SELECT SUBSTRING(`id`, 1, 30), `responsable`, `responsable`, `resp_correo`, `resp_telefono`, `fecha_alta`, 2
FROM `tb_aplicaciones`;


-- Tabla de relacion de responsables y aplicaciones

SET character_set_client = utf8mb4 ;
CREATE TABLE `tb_responsable_de_aplicaciones` (
  `id_responsables` int(11) NOT NULL ,
  `id_aplicaciones` varchar(48) NOT NULL,
  PRIMARY KEY (`id_responsables`,`id_aplicaciones`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4;

-- Agregamos la relacion entre las aplicaciones y los usuarios responsables de ellas
INSERT INTO `tb_responsable_de_aplicaciones` (`id_responsables`, `id_aplicaciones`)
SELECT `id_usuario`, `nombre_usuario`
FROM `tb_usuarios`
WHERE `fk_rol` = 2;



-- TABLA APLICACIONES
  
-- Eliminamos los campos del responsable
ALTER TABLE `tb_aplicaciones`
DROP responsable,
DROP resp_correo,
DROP resp_telefono;

-- Agregamos el campo de 'habilitado' dejando las aplicaciones habilitadas por defecto
ALTER TABLE `tb_aplicaciones`
ADD `habilitado` tinyint(4) DEFAULT 1;

ALTER TABLE `tb_aplicaciones`
ADD PRIMARY KEY (`id`);







