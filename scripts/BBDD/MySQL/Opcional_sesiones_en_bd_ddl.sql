-- ********************************************************
-- *** Tablas para el guardado de sesiones y documentos ***
-- *** en base de datos. Solo necesarias si se activa   ***
-- *** esta opcion en el fichero de configuracion del   ***
-- *** componente central.                              ***
-- ********************************************************

-- Tabla para el guardado temporal de sesiones en BD
CREATE TABLE `tb_sesiones` (
  `id` varchar(64) NOT NULL,
  `f_modificacion` bigint(10) NOT NULL,
  `sesion` blob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- Tabla para el guardado temporal de documentos en BD
CREATE TABLE `tb_documentos` (
  `id` varchar(64) NOT NULL,
  `f_modificacion` bigint(10) NOT NULL,
  `datos` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;
