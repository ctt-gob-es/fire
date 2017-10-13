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


