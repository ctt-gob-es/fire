-- ********************************************************
-- *** Tablas para el guardado de sesiones y documentos ***
-- *** en base de datos. Solo necesarias si se activa   ***
-- *** esta opcion en el fichero de configuracion del   ***
-- *** componente central.                              ***
-- ********************************************************

-- Tabla para el guardado temporal de sesiones en BD
CREATE TABLE "TB_SESIONES" (
  "ID" VARCHAR2(64) NOT NULL,
  "F_MODIFICACION" NUMBER (20,0) NOT NULL,
  "SESION" BLOB,
  CONSTRAINT  "TB_SESIONES_PK" PRIMARY KEY ("ID")
);

-- Tabla para el guardado temporal de documentos en BD
CREATE TABLE "TB_DOCUMENTOS" (
  "ID" VARCHAR2(64) NOT NULL,
  "F_MODIFICACION" NUMBER (20,0) NOT NULL,
  "DATOS" BLOB,
  CONSTRAINT  "TB_DOCUMENTOS_PK" PRIMARY KEY ("ID")
);
