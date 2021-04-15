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
INSERT INTO tb_usuarios (nombre_usuario,clave,nombre,apellidos,usu_defecto,fk_rol) 
VALUES('admin','$2y$12$JfP4bTV0i29Mnb3XBPOQl.L8JdbTrpn4fQljv8EEJKIp6NRZLB5TC','default name','default surnames',1,1);


COMMIT;
