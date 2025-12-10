-- Script de migracion desde FIRe 2.4 a 3.0

-- TABLA DE USUARIOS

-- Agregamos los campos necesarios
ALTER TABLE `tb_usuarios`
ADD `dni` VARCHAR(9) NULL,
ADD `fec_ultimo_acceso` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE `tb_usuarios`
SET `dni` = 'X0000000T'
WHERE `id_usuario` = 1;

-- TABLA USUARIOS
ALTER TABLE `tb_usuarios` CHANGE COLUMN `correo_elec` `huella` VARCHAR(45);
ALTER TABLE `tb_usuarios` DROP COLUMN IF EXISTS `nombre_usuario`;
ALTER TABLE `tb_usuarios` DROP COLUMN IF EXISTS `clave`;
ALTER TABLE `tb_usuarios` ADD CONSTRAINT UNIQUE KEY `dni_UNIQUE` (`dni`);

-- TABLA DE CERTIFICADOS
ALTER TABLE `tb_certificados` CHANGE COLUMN `cert_principal` `certificado` VARCHAR(5000);
ALTER TABLE `tb_certificados` CHANGE COLUMN `correo_elec` VARCHAR(60);
ALTER TABLE `tb_certificados` DROP COLUMN `cert_backup`;
ALTER TABLE `tb_certificados` DROP COLUMN `huella_backup`;
ALTER TABLE `tb_certificados` ADD `fec_caducidad` DATETIME NULL;
ALTER TABLE `tb_certificados` ADD `fec_inicio` DATETIME NULL;
ALTER TABLE `tb_certificados` ADD `subject` VARCHAR(4000) NULL;
ALTER TABLE `tb_certificados` ADD `fecha_ultima_comunicacion` DATETIME NULL;

-- TABLA DE CERTIFICADOS DE APLICACION

CREATE TABLE `tb_certificados_de_aplicacion` (
  `id_certificados` INT(11) NOT NULL,
  `id_aplicaciones` VARCHAR(48) NOT NULL,
  PRIMARY KEY (`id_certificados`,`id_aplicaciones`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- TABLA DE APLICACIONES

ALTER TABLE `tb_aplicaciones` DROP COLUMN `fk_certificado`;
ALTER TABLE `tb_aplicaciones` ADD `organization` VARCHAR(255) NULL;
ALTER TABLE `tb_aplicaciones` ADD `dir3_code` VARCHAR(50) NULL;
ALTER TABLE `tb_aplicaciones` ADD `proveedor_personalizado` CHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE `tb_aplicaciones` ADD `tamano_personalizado` CHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE `tb_aplicaciones` ADD `tamano_maximo_documento` BIGINT;
ALTER TABLE `tb_aplicaciones` ADD `tamano_maximo_peticion` BIGINT;
ALTER TABLE `tb_aplicaciones` ADD `cantidad_maxima_documentos` BIGINT;


-- Tabla TIPO_PLANIFICADOR
CREATE TABLE `tb_tipo_planificador` (
  `id_tipo_planificador` BIGINT NOT NULL,
  `nombre_token` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`id_tipo_planificador`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- Tabla PLANIFICADOR
CREATE TABLE `tb_planificador` (
  `id_planificador` BIGINT NOT NULL AUTO_INCREMENT,
  `hora_periodo` INT(3),
  `minuto_periodo` INT(3),
  `segundo_periodo` INT(3),
  `dia_inicio` DATETIME,
  `id_tipo_planificador` BIGINT NOT NULL,
  `aviso_anticipado` INT(3),
  PRIMARY KEY (`id_planificador`),
  CONSTRAINT `fk_tipo_planificador` FOREIGN KEY (`id_tipo_planificador`) REFERENCES `tb_tipo_planificador` (`id_tipo_planificador`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

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
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- Tabla CONTROL DE ACCESO
CREATE TABLE `tb_control_acceso` (
    `id_control_acceso` BIGINT NOT NULL AUTO_INCREMENT,
    `ip` VARCHAR(45) NOT NULL,
    `fecha_inicio_acceso` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_control_acceso`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- Tabla TB_PROVEEDORES
CREATE TABLE `tb_proveedores` (
  `id_proveedor` BIGINT NOT NULL,
  `nombre` VARCHAR(50) NOT NULL,
  `obligatorio` TINYINT(1) DEFAULT 0,
  `habilitado` TINYINT(1) DEFAULT 1,
  `orden` TINYINT(4) NOT NULL,
  PRIMARY KEY (`id_proveedor`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

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
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- Tabla TB_PROPIEDADES
CREATE TABLE `tb_propiedades` (
  `clave` VARCHAR(255) NOT NULL,
  `valor_texto` VARCHAR(4000),
  `valor_numerico` DECIMAL(19,4),
  `valor_fecha` DATETIME,
  `tipo` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`clave`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- Tabla CATALOGO TIPO AUTENTICACION
CREATE TABLE `tb_c_tipo_autenticacion` (
    `id_tipo_autenticacion` TINYINT NOT NULL,
    `nombre_token` VARCHAR(45) NOT NULL,
    PRIMARY KEY (`id_tipo_autenticacion`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

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
    `fecha_ultima_comunicacion` DATETIME NULL,
    PRIMARY KEY (`id_servidor_afirma`),
    CONSTRAINT `fk_id_tipo_autenticacion`
        FOREIGN KEY (`id_tipo_autenticacion`)
        REFERENCES `tb_c_tipo_autenticacion`(`id_tipo_autenticacion`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- TABLA TB_FIRMAS
ALTER TABLE `tb_firmas` ADD `dir3_code` VARCHAR(50) NULL;
ALTER TABLE `tb_firmas` ADD `organization` VARCHAR(255) NULL;

-- TABLA TB_TRANSACCIONES
ALTER TABLE `tb_transacciones` ADD `dir3_code` VARCHAR(50) NULL;
ALTER TABLE `tb_transacciones` ADD `organization` VARCHAR(255) NULL;

-- Proveedores por defecto --
INSERT INTO `tb_proveedores` (`id_proveedor`, `nombre`, `orden`)
VALUES ('clavefirma', 'Cl@ve Firma', 1),
       ('clavefirmatest', 'Simulador Cl@ve Firma', 2),
       ('fnmt', 'CloudID', 3),
       ('local', 'Firma local', 4);

-- Insertar valores en la tabla TIPO_PLANIFICADOR
INSERT INTO `tb_tipo_planificador` (`id_tipo_planificador`, `nombre_token`)
VALUES (0, 'TIPO_PLANIFICADOR00'),
       (1, 'TIPO_PLANIFICADOR01'),
       (2, 'TIPO_PLANIFICADOR02');

-- Insertar valores en la tabla PLANIFICADOR
INSERT INTO `tb_planificador` (`id_planificador`, `hora_periodo`, `minuto_periodo`, `segundo_periodo`, `dia_inicio`, `id_tipo_planificador`) 
VALUES (1, 24, 0, 0, STR_TO_DATE('01/01/2012 00:00:00', '%m/%d/%Y %H:%i:%s'), 1);

-- Insertar valores en la tabla PROGRAMADOR
INSERT INTO `tb_programador` (`id_programador`, `nombre_token`, `nombre_clase`, `esta_activo`, `num_hilos`, `num_procesos`, `periodo_expirado`, `tiempo_reasignacion`, `tiempo_reactivacion`, `tiempo_comprobacion`, `dias_preaviso`, `periodo_comunicacion`, `id_planificador`, `nombre_programador`) 
VALUES (1, 'PROGRAMADOR01', 'es.gob.fire.control.tasks.TaskVerifyCertExpired', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'TaskVerifyCertExpired');

-- Insertar valores en la tabla TB_C_TIPO_AUTENTICACION
INSERT INTO `tb_c_tipo_autenticacion` (`id_tipo_autenticacion`, `nombre_token`)
VALUES (0, 'AUTHENTICATION_TYPE00'),
       (1, 'AUTHENTICATION_TYPE01'),
       (2, 'AUTHENTICATION_TYPE02');

COMMIT;