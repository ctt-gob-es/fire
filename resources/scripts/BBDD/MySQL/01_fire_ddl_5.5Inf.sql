-- ********************************************************
-- **************** Creación de Tablas ********************
-- ********************************************************
SET character_set_client = utf8mb4 ;
CREATE TABLE `tb_certificados` (
  `id_certificado` int(11) AUTO_INCREMENT,
  `nombre_cert` varchar(45) NOT NULL,
  `fec_alta` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `certificado` varchar(5000) DEFAULT NULL,
  `huella` varchar(45) DEFAULT NULL,
  `fec_inicio` datetime NULL,
  `fec_caducidad` datetime NULL,
  `subject` varchar(4000) NULL,
  `fecha_ultima_comunicacion` datetime NULL,
  PRIMARY KEY (`id_certificado`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='tabla de certificados';


CREATE TABLE `tb_aplicaciones` (
  `id` varchar(48) NOT NULL,
  `nombre` varchar(45) NOT NULL,
  `fecha_alta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `habilitado` tinyint(4) DEFAULT '1',
  `organization` varchar(255),
  `dir3_code` varchar(50),
  `proveedor_personalizado` char(1) DEFAULT 'N' NOT NULL,
  `tamano_personalizado` char(1) DEFAULT 'N' NOT NULL,
  `tamano_maximo_documento` bigint,
  `tamano_maximo_peticion` bigint,
  `cantidad_maxima_documentos` bigint,
  
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
  `fec_alta` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de alta del usuario.',
  `usu_defecto` tinyint(4) NOT NULL DEFAULT '0',
  `codigo_renovacion` varchar(100) DEFAULT NULL,
  `fec_renovacion` TIMESTAMP DEFAULT NULL,
  `rest_clave` tinyint(4) DEFAULT '0',
  `dni` varchar(9) NULL,
  `fec_ultimo_acceso` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `nombre_usuario_UNIQUE` (`nombre_usuario`),
  UNIQUE KEY `codigo_renovacion_UNIQUE` (`codigo_renovacion`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


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
  `fecha` TIMESTAMP NOT NULL COMMENT 'Fecha de la operacion',
  `aplicacion` varchar(45) NOT NULL COMMENT 'Aplicacion que solicito la operacion',
  `operacion` varchar(10) NOT NULL COMMENT 'Tipo de operacion',
  `proveedor` varchar(45) NOT NULL COMMENT 'Nombre del proveedor de firma',
  `proveedor_forzado` tinyint(1) DEFAULT '0' COMMENT 'Si solo habia un proveedor o si la aplicacion forzo que se usase ese',
  `correcta` tinyint(1) DEFAULT '0' COMMENT 'Si termino correctamente o no',
  `tamanno` int(11) DEFAULT '0' COMMENT 'Tamano total de los datos procesados',
  `total` int(11) DEFAULT '0' COMMENT 'Numero de transacciones con esta configuracion',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE `tb_roles` (
  `id` int(11) NOT NULL,
  `nombre_rol` varchar(45) NOT NULL,
  `permisos` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `tb_responsable_de_aplicaciones` (
  `id_responsables` int(11) NOT NULL,
  `id_aplicaciones` varchar(48) NOT NULL,
  PRIMARY KEY (`id_responsables`,`id_aplicaciones`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
  `fecha` TIMESTAMP NOT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Tabla TIPO_PLANIFICADOR
CREATE TABLE TB_TIPO_PLANIFICADOR (
  ID_TIPO_PLANIFICADOR BIGINT NOT NULL AUTO_INCREMENT,
  NOMBRE_TOKEN VARCHAR(30) NOT NULL,
  PRIMARY KEY (ID_TIPO_PLANIFICADOR)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla PLANIFICADOR
CREATE TABLE TB_PLANIFICADOR (
  ID_PLANIFICADOR BIGINT NOT NULL AUTO_INCREMENT,
  HORA_PERIODO INT(3),
  MINUTO_PERIODO INT(3),
  SEGUNDO_PERIODO INT(3),
  DIA_INICIO DATETIME,
  ID_TIPO_PLANIFICADOR BIGINT NOT NULL,
  AVISO_ANTICIPADO INT(3),
  PRIMARY KEY (ID_PLANIFICADOR),
  CONSTRAINT FK_TIPO_PLANIFICADOR FOREIGN KEY (ID_TIPO_PLANIFICADOR) REFERENCES TB_TIPO_PLANIFICADOR (ID_TIPO_PLANIFICADOR) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla PROGRAMADOR
CREATE TABLE TB_PROGRAMADOR (
  ID_PROGRAMADOR BIGINT NOT NULL AUTO_INCREMENT,
  NOMBRE_TOKEN VARCHAR(30) NOT NULL,
  NOMBRE_CLASE VARCHAR(255) NOT NULL,
  ESTA_ACTIVO CHAR(1) NOT NULL,
  NUM_HILOS BIGINT,
  NUM_PROCESOS BIGINT,
  PERIODO_EXPIRADO BIGINT,
  TIEMPO_REASIGNACION BIGINT,
  TIEMPO_REACTIVACION BIGINT,
  TIEMPO_COMPROBACION BIGINT,
  DIAS_PREAVISO BIGINT,
  PERIODO_COMUNICACION BIGINT,
  ID_PLANIFICADOR BIGINT NOT NULL,
  NOMBRE_PROGRAMADOR VARCHAR(50) NOT NULL,
  PRIMARY KEY (ID_PROGRAMADOR),
  UNIQUE KEY UNICO_NOMBRE_PROGRAMADOR (NOMBRE_PROGRAMADOR),
  CONSTRAINT FK_PLANIFICADOR FOREIGN KEY (ID_PLANIFICADOR) REFERENCES TB_PLANIFICADOR (ID_PLANIFICADOR) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla CONTROL DE ACCESO
CREATE TABLE TB_CONTROL_ACCESO (
    ID_CONTROL_ACCESO BIGINT NOT NULL AUTO_INCREMENT,
    IP VARCHAR(45) NOT NULL,
    FECHA_INICIO_ACCESO TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ID_CONTROL_ACCESO)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla TB_PROVEEDORES
CREATE TABLE `tb_proveedores` (
  `id_proveedor` bigint NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `obligatorio` char(1) NOT NULL,
  `habilitado` char(1) NOT NULL,
  `orden` tinyint(4) NOT NULL,
  
  PRIMARY KEY (`id_proveedor`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- Tabla TB_PROVEEDORES_APLICACION
CREATE TABLE `tb_proveedores_aplicacion` (
  `id_proveedor` bigint NOT NULL,
  `id_aplicacion` varchar(48) NOT NULL,
  `obligatorio` char(1) NOT NULL,
  `habilitado` char(1) NOT NULL,
  `orden` tinyint(4) NOT NULL,
  
  PRIMARY KEY (`id_aplicacion`, `id_proveedor`),
  CONSTRAINT `fk_aplicacion` FOREIGN KEY (`id_aplicacion`) REFERENCES `tb_aplicaciones` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_proveedor` FOREIGN KEY (`id_proveedor`) REFERENCES `tb_proveedores` (`id_proveedor`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- Tabla TB_PROPIEDADES
CREATE TABLE `tb_propiedades` (
  `clave` varchar(255) NOT NULL,
  `valor_texto` varchar(4000),
  `valor_numerico` decimal(19,4),
  `valor_fecha` datetime,
  `tipo` varchar(20) NOT NULL,
  
  PRIMARY KEY (`clave`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;