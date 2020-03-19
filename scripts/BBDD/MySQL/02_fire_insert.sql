-- ********************************************************
-- **************** Inserción datos ********************
-- ********************************************************

-- INSERTAR DATOS--------------

-- ROLES --------
INSERT INTO `tb_roles` (`id`,`nombre_rol`,`permisos`) 
VALUES (1,'admin','1,2'),
	   (2,'responsible','2'),
	   (3,'contact', NULL);

-- USUARIO POR DEFECTO --------
INSERT INTO tb_usuarios (nombre_usuario,clave,nombre,apellidos,usu_defecto,fk_rol) 
VALUES('admin','D/4avRoIIVNTwjPW4AlhPpXuxCU4Mqdhryj/N6xaFQw=','default name','default surnames',1,1);


COMMIT;
