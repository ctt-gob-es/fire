-- ********************************************************
-- **************** Inserci�n datos ********************
-- ********************************************************

-- INSERTAR DATOS--------------

-- ROLES --------
INSERT INTO `tb_roles` (`id`,`nombre_rol`,`permisos`) 
VALUES (1,'admin','1,2'),
	   (2,'responsible','2'),
	   (3,'contact', NULL);

-- USUARIO POR DEFECTO --------
INSERT INTO tb_usuarios (nombre,apellidos,usu_defecto,fk_rol,dni) 
VALUES('default name','default surnames',1,1,'X0000000T');

-- Insertar valores en la tabla TIPO_PLANIFICADOR
INSERT INTO TB_TIPO_PLANIFICADOR (ID_TIPO_PLANIFICADOR, NOMBRE_TOKEN) 
VALUES (0, 'TIPO_PLANIFICADOR00');

INSERT INTO TB_TIPO_PLANIFICADOR (ID_TIPO_PLANIFICADOR, NOMBRE_TOKEN) 
VALUES (1, 'TIPO_PLANIFICADOR01');

INSERT INTO TB_TIPO_PLANIFICADOR (ID_TIPO_PLANIFICADOR, NOMBRE_TOKEN) 
VALUES (2, 'TIPO_PLANIFICADOR02');

-- Insertar valores en la tabla PLANIFICADOR
INSERT INTO TB_PLANIFICADOR (ID_PLANIFICADOR, HORA_PERIODO, MINUTO_PERIODO, SEGUNDO_PERIODO, DIA_INICIO, ID_TIPO_PLANIFICADOR) 
VALUES (1, 24, 0, 0, STR_TO_DATE('01/01/2012 00:00:00', '%m/%d/%Y %H:%i:%s'), 1);

-- Insertar valores en la tabla PROGRAMADOR
INSERT INTO TB_PROGRAMADOR (ID_PROGRAMADOR, NOMBRE_TOKEN, NOMBRE_CLASE, ESTA_ACTIVO, NUM_HILOS, NUM_PROCESOS, PERIODO_EXPIRADO, TIEMPO_REASIGNACION, TIEMPO_REACTIVACION, TIEMPO_COMPROBACION, DIAS_PREAVISO, PERIODO_COMUNICACION, ID_PLANIFICADOR, NOMBRE_PROGRAMADOR) 
VALUES (1, 'PROGRAMADOR01', 'es.gob.fire.control.tasks.TaskVerifyCertExpired', 'N', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'TaskVerifyCertExpired');


COMMIT;
