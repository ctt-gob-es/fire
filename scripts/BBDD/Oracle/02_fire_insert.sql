-- ********************************************************
-- **************** Insercion datos ********************
-- ********************************************************

-- ROLES --------
INSERT INTO TB_ROLES ("id","nombre_rol","permisos") 
VALUES(1,'admin','1,2');

INSERT INTO TB_ROLES ("id","nombre_rol","permisos") 
VALUES(2,'responsible','2');

INSERT INTO TB_ROLES ("id","nombre_rol") 
VALUES(3,'contact');

-- USUARIO POR DEFECTO --------
INSERT INTO TB_USUARIOS ("nombre_usuario","clave","nombre","apellidos","usu_defecto","fk_rol") 
VALUES('admin','D/4avRoIIVNTwjPW4AlhPpXuxCU4Mqdhryj/N6xaFQw=','default name','default surnames',1,1);


COMMIT;
