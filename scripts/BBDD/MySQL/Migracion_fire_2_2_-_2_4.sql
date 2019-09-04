-- Script de migracion desde FIRe 2.2 o 2.3 a 2.4

-- 1 Crear la tablas nuevas 

-- Tabla para el guardado de las referencias a los servidores de log

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

-- Tabla para el guardado de las estadisticas de las firmas

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
  PRIMARY KEY (`id`,`navegador`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Tabla para el guardado de las estadisticas de las transacciones

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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Tabla para el guardado de los roles de los usuarios

CREATE TABLE `tb_roles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_rol` varchar(45) NOT NULL,
  `permisos` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;








--   Insertamos los permisos del usuario al inicializar la aplicacion
insert into `tb_roles` (`nombre_rol`,`permisos`) 
values('admin','1,2');

insert into `tb_roles` (`nombre_rol`,`permisos`) 
values('responsible','1');

insert into `tb_roles` (`nombre_rol`) 
values('contact');



-- Agregamos campos a una tabla
-- Aplicaciones 

ALTER TABLE `tb_aplicaciones` ADD `fk_responsable` int(11) NOT NULL;
 
ALTER TABLE `tb_aplicaciones` ADD `habilitado` tinyint(4) DEFAULT 0;
 
ALTER TABLE `tb_aplicaciones` 
ADD INDEX `fk_responsable_idx` (`nombre` ASC);

 ALTER TABLE `tb_aplicaciones` 
ADD INDEX `fk_responsable_idx1` (`fk_responsable` ASC);


-- Usuarios

ALTER TABLE `tb_usuarios` ADD `codigo_renovacion` VARCHAR(90) DEFAULT NULL
AFTER `usu_defecto`;
 
ALTER TABLE `tb_usuarios` ADD `fec_renovacion` datetime DEFAULT NULL
 AFTER `codigo_renovacion`;
 
ALTER TABLE `tb_usuarios` ADD `rest_clave` tinyint(4) DEFAULT 0
 AFTER `fec_renovacion`;
 
  
 ALTER TABLE `tb_usuarios`
ADD UNIQUE (`codigo_renovacion`);

-- Agregar campo "fk_rol"

ALTER TABLE `tb_usuarios` ADD `fk_rol` int(11) NOT NULL
 AFTER `usu_defecto`;
 
 
-- Asignar rol administrador a todos los usuarios en el campo "fk_rol"


UPDATE `tb_usuarios`SET `fk_rol`=1;


-- Eliminar campo "rol"

ALTER TABLE `tb_usuarios` DROP COLUMN rol;

-- Modificamos la columna para que sea null

ALTER TABLE `tb_usuarios` MODIFY `clave` VARCHAR (45) NULL;


-- Insertamos campos a una tabla
INSERT INTO `tb_usuarios` (`nombre_usuario`, `nombre`, `apellidos`, `correo_elec`, `telf_contacto`, `fk_rol`)
SELECT `id`, `responsable`, `responsable`, `resp_correo`, `resp_telefono`, 3
FROM `tb_aplicaciones`;

-- Actualizamos los campos de responsable 

UPDATE `tb_aplicaciones`SET `fk_responsable`=1;

-- Creamos las foreign key
 
 ALTER TABLE `tb_aplicaciones`
 ADD CONSTRAINT `fk_responsable`
 FOREIGN KEY (`fk_responsable`)
  REFERENCES `tb_usuarios` (`id_usuario`)
  ON DELETE RESTRICT 
  ON UPDATE CASCADE;
  
  
  
   ALTER TABLE `tb_usuarios` 
ADD CONSTRAINT `fk_rol`
FOREIGN KEY (`fk_rol`)
  REFERENCES `tb_roles` (`id`)
ON DELETE RESTRICT
 ON UPDATE CASCADE;
 
 -- Creamos el idx del rol
 
 ALTER TABLE `tb_usuarios` 
ADD INDEX `fk_rol_idx` (`fk_rol` ASC);


  
-- Elimina campos de una tabla

ALTER TABLE `tb_aplicaciones` DROP COLUMN responsable, DROP COLUMN resp_correo, DROP COLUMN resp_telefono;








