-- ********************************************************
-- **************** Insercion datos ********************
-- ********************************************************

--   Insertamos los permisos de los roles
INSERT INTO TB_ROLES ("ID","NOMBRE_ROL","PERMISOS") 
VALUES(1,'admin','1,2');

INSERT INTO TB_ROLES ("ID","NOMBRE_ROL","PERMISOS") 
VALUES(2,'responsible','2');

INSERT INTO TB_ROLES ("ID","NOMBRE_ROL","PERMISOS") 
VALUES(3,'contact',NULL);

-- USUARIO POR DEFECTO --------
INSERT INTO TB_USUARIOS ("NOMBRE_USUARIO", "CLAVE", "NOMBRE", "APELLIDOS", "USU_DEFECTO", "FK_ROL")
VALUES('admin','$2y$12$JfP4bTV0i29Mnb3XBPOQl.L8JdbTrpn4fQljv8EEJKIp6NRZLB5TC','default name','default surnames',1,1);


COMMIT;
