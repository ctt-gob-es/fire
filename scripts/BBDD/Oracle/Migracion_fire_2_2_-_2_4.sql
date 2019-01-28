
-- Script de migracion desde FIRe 2.2 o 2.3 a 2.4

-- 1 Crear la tablas nuevas 

-- Tabla para el guardado de las referencias a los servidores de log

CREATE TABLE "TB_SERVIDORES_LOG" (
  "id_servidor" NUMBER NOT NULL ENABLE,
  "nombre" VARCHAR2(45) NOT NULL ENABLE, 
  "url_servicio_log" VARCHAR2(500) NOT NULL ENABLE, 
  "clave" VARCHAR2(45) NOT NULL ENABLE, 
  "verificar_ssl" NUMBER(1,0) NOT NULL ENABLE,
  CONSTRAINT "TB_SERVIDORES_LOG_PK" PRIMARY KEY ("id_servidor") ENABLE, 
  CONSTRAINT "TB_SERVIDORES_LOG_UK1" UNIQUE ("nombre") ENABLE,
  CONSTRAINT "TB_SERVIDORES_LOG_UK2" UNIQUE ("url_servicio_log") ENABLE
);

-- Tabla para el guardado de las estadisticas de las firmas

CREATE TABLE "TB_FIRMAS" (
  "id" NUMBER NOT NULL ENABLE,
  "fecha" TIMESTAMP (6) NOT NULL ENABLE,
  "aplicacion" VARCHAR2(45) NOT NULL ENABLE, 
  "formato" VARCHAR2(20) NOT NULL ENABLE, 
  "formato_mejorado" VARCHAR2(20) ENABLE,
  "algoritmo" VARCHAR2(20) NOT NULL ENABLE, 
  "proveedor" VARCHAR2(45) NOT NULL ENABLE, 
  "navegador" VARCHAR2(45) NOT NULL ENABLE, 
  "correcta" NUMBER(1,0) NOT NULL ENABLE,
  "total" NUMBER DEFAULT 0 NOT NULL ENABLE,
  CONSTRAINT "TB_FIRMAS_PK" PRIMARY KEY ("id") ENABLE
);

-- Tabla para el guardado de las estadisticas de las transacciones

CREATE TABLE "TB_TRANSACCIONES" (
  "id" NUMBER NOT NULL ENABLE,
  "fecha" TIMESTAMP (6) NOT NULL ENABLE,
  "aplicacion" VARCHAR2(45) NOT NULL ENABLE,
  "operacion" VARCHAR2(10) NOT NULL ENABLE,
  "proveedor" VARCHAR2(45) NOT NULL ENABLE,
  "proveedor_forzado" NUMBER(1,0) NOT NULL ENABLE,
  "correcta" NUMBER(1,0) NOT NULL ENABLE,
  "tamanno" NUMBER DEFAULT 0 NOT NULL ENABLE,
  "total" NUMBER DEFAULT 0 NOT NULL ENABLE,
  CONSTRAINT "TB_TRANSACCIONES_PK" PRIMARY KEY ("id") ENABLE
);