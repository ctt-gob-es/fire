-- ********************************************************
-- **************** Creación de Tablas ********************
-- ********************************************************

CREATE TABLE tb_aplicaciones (
	id varchar2(48) PRIMARY KEY,
	nombre varchar2(45) NOT NULL,
	responsable varchar2(45) NOT NULL,
	resp_correo varchar2(45) DEFAULT NULL,
	resp_telefono varchar2(30) DEFAULT NULL,
	fecha_alta TIMESTAMP NOT NULL,
	cer varchar2(4000) NOT NULL,
	huella varchar2(28) NOT NULL
);

CREATE TABLE tb_configuracion (
	parametro varchar2(30) PRIMARY KEY,
	valor varchar2(45) DEFAULT NULL
);


