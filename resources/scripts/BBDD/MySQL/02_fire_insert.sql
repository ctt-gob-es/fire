-- ********************************************************
-- **************** Inserci�n datos ********************
-- ********************************************************

-- INSERTAR DATOS--------------

-- ROLES --------
INSERT INTO `tb_roles` (`id`, `nombre_rol`, `permisos`) 
VALUES (1, 'admin', '1,2'),
	   (2, 'responsible', '2'),
	   (3, 'contact', NULL);

-- USUARIO POR DEFECTO --------
INSERT INTO `tb_usuarios` (`nombre`, `apellidos`, `usu_defecto`, `fk_rol`, `dni`) 
VALUES('default name', 'default surnames', 1, 1, 'X0000000T');

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
