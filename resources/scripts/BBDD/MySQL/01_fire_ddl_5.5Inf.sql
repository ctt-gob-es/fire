-- ********************************************************
-- **************** Creación de Tablas ********************
-- ********************************************************
SET character_set_client = UTF8MB4 ;
CREATE TABLE `tb_certificados` (
  `id_certificado` INT(11) AUTO_INCREMENT,
  `nombre_cert` VARCHAR(45) NOT NULL,
  `fec_alta` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `certificado` VARCHAR(5000) DEFAULT NULL,
  `huella` VARCHAR(45) DEFAULT NULL,
  `fec_inicio` TIMESTAMP NULL,
  `fec_caducidad` TIMESTAMP NULL,
  `subject` VARCHAR(4000) NULL,
  `fecha_ultima_comunicacion` TIMESTAMP NULL,
  PRIMARY KEY (`id_certificado`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8;


CREATE TABLE `tb_aplicaciones` (
  `id` VARCHAR(48) NOT NULL,
  `nombre` VARCHAR(45) NOT NULL,
  `fecha_alta` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `habilitado` TINYINT(4) DEFAULT '1',
  `organization` VARCHAR(255),
  `dir3_code` VARCHAR(50),
  `proveedor_personalizado` CHAR(1) DEFAULT 'N' NOT NULL,
  `tamano_personalizado` CHAR(1) DEFAULT 'N' NOT NULL,
  `tamano_maximo_documento` BIGINT,
  `tamano_maximo_peticion` BIGINT,
  `cantidad_maxima_documentos` BIGINT,
  
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;


CREATE TABLE `tb_usuarios` (
  `id_usuario` INT(11) NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NOT NULL COMMENT 'Nombre completo del usuario',
  `apellidos` VARCHAR(120) NOT NULL COMMENT 'Apellidos del usuario',
  `correo_elec` VARCHAR(60) DEFAULT NULL COMMENT 'Correo electrónico',
  `telf_contacto` VARCHAR(45) DEFAULT NULL COMMENT 'Teléfono de contacto',
  `fk_rol` INT(11) NOT NULL,
  `fec_alta` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usu_defecto` TINYINT(4) NOT NULL DEFAULT '0',
  `codigo_renovacion` VARCHAR(100) DEFAULT NULL,
  `fec_renovacion` TIMESTAMP DEFAULT NULL,
  `rest_clave` TINYINT(4) DEFAULT '0',
  `dni` VARCHAR(9) NULL,
  `fec_ultimo_acceso` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `dni_UNIQUE` (`dni`),
  UNIQUE KEY `codigo_renovacion_UNIQUE` (`codigo_renovacion`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8;


CREATE TABLE `tb_servidores_log` (
  `id_servidor` INT(11) NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NOT NULL,
  `url_servicio_log` VARCHAR(500) NOT NULL,
  `clave` VARCHAR(45) NOT NULL,
  `verificar_ssl` TINYINT(1) DEFAULT '1',
  PRIMARY KEY (`id_servidor`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre`),
  UNIQUE KEY `url_servicio_log_UNIQUE` (`url_servicio_log`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8;


CREATE TABLE `tb_firmas` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `fecha` TIMESTAMP DEFAULT NULL COMMENT 'Fecha de la operacion',
  `aplicacion` VARCHAR(45) DEFAULT NULL COMMENT 'Aplicacion que solicito la operacion',
  `dir3_code` VARCHAR(50) DEFAULT NULL COMMENT 'Codigo DIR3',
  `organization` VARCHAR(255) DEFAULT NULL COMMENT 'Nombre de organizacion',
  `formato` VARCHAR(20) DEFAULT NULL COMMENT 'Formato de firma',
  `formato_mejorado` VARCHAR(20) DEFAULT NULL COMMENT 'Formato longevo al que actualizar',
  `algoritmo` VARCHAR(20) DEFAULT NULL COMMENT 'Algoritmo de firma',
  `proveedor` VARCHAR(45) DEFAULT NULL COMMENT 'Nombre del proveedor de certificados utilizado',
  `navegador` VARCHAR(20) NOT NULL COMMENT 'Navegador web',
  `correcta` TINYINT(1) DEFAULT NULL COMMENT 'Si la firma es correcta o no',
  `total` INT(11) DEFAULT NULL COMMENT 'Numero de operaciones con la esta configuracion',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8;


CREATE TABLE `tb_transacciones` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `fecha` TIMESTAMP NOT NULL COMMENT 'Fecha de la operacion',
  `aplicacion` VARCHAR(45) NOT NULL COMMENT 'Aplicacion que solicito la operacion',
  `dir3_code` VARCHAR(50) DEFAULT NULL COMMENT 'Codigo DIR3',
  `organization` VARCHAR(255) DEFAULT NULL COMMENT 'Nombre de organizacion',
  `operacion` VARCHAR(10) NOT NULL COMMENT 'Tipo de operacion',
  `proveedor` VARCHAR(45) NOT NULL COMMENT 'Nombre del proveedor de firma',
  `proveedor_forzado` TINYINT(1) DEFAULT '0' COMMENT 'Si solo habia un proveedor o si la aplicacion forzo que se usase ese',
  `correcta` TINYINT(1) DEFAULT '0' COMMENT 'Si termino correctamente o no',
  `tamanno` INT(11) DEFAULT '0' COMMENT 'Tamano total de los datos procesados',
  `total` INT(11) DEFAULT '0' COMMENT 'Numero de transacciones con esta configuracion',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8;


CREATE TABLE `tb_roles` (
  `id` INT(11) NOT NULL,
  `nombre_rol` VARCHAR(45) NOT NULL,
  `permisos` VARCHAR(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;


CREATE TABLE `tb_responsable_de_aplicaciones` (
  `id_responsables` INT(11) NOT NULL,
  `id_aplicaciones` VARCHAR(48) NOT NULL,
  PRIMARY KEY (`id_responsables`,`id_aplicaciones`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;


CREATE TABLE `tb_certificados_de_aplicacion` (
  `id_certificados` INT(11) NOT NULL,
  `id_aplicaciones` VARCHAR(48) NOT NULL,
  PRIMARY KEY (`id_certificados`,`id_aplicaciones`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;


-- Creamos la relacion entre las tablas de usuario y roles
ALTER TABLE `tb_usuarios` 
ADD CONSTRAINT `fk_rol`
FOREIGN KEY (`fk_rol`)
  REFERENCES `tb_roles` (`id`)
ON DELETE RESTRICT
 ON UPDATE CASCADE;


CREATE TABLE `tb_audit_transacciones` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `fecha` TIMESTAMP NOT NULL COMMENT 'Fecha de la operacion',
  `id_aplicacion` VARCHAR(48) NOT NULL COMMENT 'Identificador de la aplicacion',
  `nombre_aplicacion` VARCHAR(48) NOT NULL COMMENT 'Nombre de la operacion',
  `id_transaccion` VARCHAR(45) NOT NULL COMMENT 'Identificador de transaccion',
  `operacion` VARCHAR(10) NOT NULL COMMENT 'Tipo de operacion (simple, de lote...)',
  `operacion_criptografica` VARCHAR(10) NOT NULL COMMENT 'Operacion criptografica (firma, cofirma...)',
  `formato` VARCHAR(20) NOT NULL COMMENT 'Formato de firma', 
  `formato_actualizado` VARCHAR(20) COMMENT 'Formato longevo si se indico',
  `algoritmo` VARCHAR(20) NOT NULL COMMENT 'Algoritmo de firma', 
  `proveedor` VARCHAR(45) NOT NULL COMMENT 'Proveedor de firma seleccionado',
  `proveedor_forzado` TINYINT(1) NOT NULL COMMENT 'Si se forzo al usuario a usar ese proveedor',
  `navegador` VARCHAR(20) NOT NULL COMMENT 'Navegador web del usuario', 
  `tamanno` INT(11) DEFAULT 0 COMMENT 'Tamanyo total de los datos procesados',
  `nodo` VARCHAR(45) COMMENT 'Nombre del nodo en el que finaliza la peticion (si se conoce)',
  `resultado` TINYINT(1) NOT NULL COMMENT 'Si la transaccion finalizo bien o mal',
  `error_detalle` VARCHAR(150) COMMENT 'Mensaje de error si fallo la transaccion',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8;

CREATE TABLE `tb_audit_firmas` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `id_transaccion` VARCHAR(45) NOT NULL COMMENT 'Identificador de la transaccion en la que se proceso la firma',
  `operacion_criptografica` VARCHAR(10) NOT NULL COMMENT 'Operacion criptografica (firma, cofirma...)',
  `formato` VARCHAR(20) NOT NULL COMMENT 'Formato de firma', 
  `formato_actualizado` VARCHAR(20) COMMENT 'Formato longevo si se indico',
  `tamanno` INT(11) DEFAULT 0 COMMENT 'Tamanyo de los datos que se firman',
  `resultado` TINYINT(1) NOT NULL COMMENT 'Si la firma finalizo bien o mal',
  `error_detalle` VARCHAR(150) COMMENT 'Mensaje de error si fallo la firma',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8;

-- Tabla TIPO_PLANIFICADOR
CREATE TABLE `tb_tipo_planificador` (
  `id_tipo_planificador` BIGINT NOT NULL AUTO_INCREMENT,
  `nombre_token` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`id_tipo_planificador`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

-- Tabla PLANIFICADOR
CREATE TABLE `tb_planificador` (
  `id_planificador` BIGINT NOT NULL AUTO_INCREMENT,
  `hora_periodo` INT(3),
  `minuto_periodo` INT(3),
  `segundo_periodo` INT(3),
  `dia_inicio` TIMESTAMP,
  `id_tipo_planificador` BIGINT NOT NULL,
  `aviso_anticipado` INT(3),
  PRIMARY KEY (`id_planificador`),
  CONSTRAINT `fk_tipo_planificador` FOREIGN KEY (`id_tipo_planificador`) REFERENCES `tb_tipo_planificador` (`id_tipo_planificador`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

-- Tabla PROGRAMADOR
CREATE TABLE `tb_programador` (
  `id_programador` BIGINT NOT NULL AUTO_INCREMENT,
  `nombre_token` VARCHAR(30) NOT NULL,
  `nombre_clase` VARCHAR(255) NOT NULL,
  `esta_activo` TINYINT(1) NOT NULL,
  `num_hilos` BIGINT,
  `num_procesos` BIGINT,
  `periodo_expirado` BIGINT,
  `tiempo_reasignacion` BIGINT,
  `tiempo_reactivacion` BIGINT,
  `tiempo_comprobacion` BIGINT,
  `dias_preaviso` BIGINT,
  `periodo_comunicacion` BIGINT,
  `id_planificador` BIGINT NOT NULL,
  `nombre_programador` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id_programador`),
  UNIQUE KEY `unico_nombre_programador` (`nombre_programador`),
  CONSTRAINT `fk_planificador` FOREIGN KEY (`id_planificador`) REFERENCES `tb_planificador` (`id_planificador`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

-- Tabla CONTROL DE ACCESO
CREATE TABLE `tb_control_acceso` (
    `id_control_acceso` BIGINT NOT NULL AUTO_INCREMENT,
    `ip` VARCHAR(45) NOT NULL,
    `fecha_inicio_acceso` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_control_acceso`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

-- Tabla TB_PROVEEDORES
CREATE TABLE `tb_proveedores` (
  `id_proveedor` BIGINT NOT NULL,
  `nombre` VARCHAR(50) NOT NULL,
  `obligatorio` TINYINT(1) DEFAULT 0,
  `habilitado` TINYINT(1) DEFAULT 1,
  `orden` TINYINT(4) NOT NULL,
  
  PRIMARY KEY (`id_proveedor`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

-- Tabla TB_PROVEEDORES_APLICACION
CREATE TABLE `tb_proveedores_aplicacion` (
  `id_proveedor` BIGINT NOT NULL,
  `id_aplicacion` VARCHAR(48) NOT NULL,
  `obligatorio` TINYINT(1) DEFAULT 0,
  `habilitado` TINYINT(1) DEFAULT 1,
  `orden` TINYINT(4) NOT NULL,
  
  PRIMARY KEY (`id_aplicacion`, `id_proveedor`),
  CONSTRAINT `fk_aplicacion` FOREIGN KEY (`id_aplicacion`) REFERENCES `tb_aplicaciones` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_proveedor` FOREIGN KEY (`id_proveedor`) REFERENCES `tb_proveedores` (`id_proveedor`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

-- Tabla TB_PROPIEDADES
CREATE TABLE `tb_propiedades` (
  `clave` VARCHAR(255) NOT NULL,
  `valor_texto` VARCHAR(4000),
  `valor_numerico` DECIMAL(19,4),
  `valor_fecha` TIMESTAMP,
  `tipo` VARCHAR(20) NOT NULL,
  
  PRIMARY KEY (`clave`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

-- Tabla CATALOGO TIPO AUTENTICACION
CREATE TABLE `tb_c_tipo_autenticacion` (
    `id_tipo_autenticacion` TINYINT NOT NULL,
    `nombre_token` VARCHAR(45) NOT NULL,
    PRIMARY KEY (`id_tipo_autenticacion`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

-- Tabla SERVIDOR_AFIRMA
CREATE TABLE `tb_servidor_afirma` (
    `id_servidor_afirma` BIGINT NOT NULL,
    `url_servidor` VARCHAR(255) NOT NULL,
    `fin_conexion` BIGINT NOT NULL,
    `nombre_aplicacion` VARCHAR(45) NOT NULL,
    `id_tipo_autenticacion` TINYINT NOT NULL,
    `usuario` VARCHAR(45) NULL,
    `password` TEXT NULL,
    `truststore` LONGBLOB NULL,
    `truststore_password` TEXT NULL,
    `truststore_type` VARCHAR(16) NULL,
    `keystore` LONGBLOB NULL,
    `ks_password` TEXT NULL,
    `ks_type` VARCHAR(16) NULL,
    `ks_cert_alias` VARCHAR(255) NULL,
    `ks_cert_password` TEXT NULL,
    `auth_truststore` LONGBLOB NULL,
    `auth_ts_password` TEXT NULL,
    `auth_ts_type` VARCHAR(16) NULL,
    `auth_cert_alias` VARCHAR(255) NULL,
    `keystore_version` BIGINT NULL,
    `truststore_version` BIGINT NULL,
    `authentication_version` BIGINT NULL,
    `fecha_ultima_comunicacion` TIMESTAMP NULL,
    PRIMARY KEY (`id_servidor_afirma`),
    CONSTRAINT `fk_id_tipo_autenticacion`
        FOREIGN KEY (`id_tipo_autenticacion`)
        REFERENCES `tb_c_tipo_autenticacion`(`id_tipo_autenticacion`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;
