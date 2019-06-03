-- ********************************************************
-- **************** Inserción datos ********************
-- ********************************************************

-- INSERTAR DATOS--------------

-- USUARIO POR DEFECTO --------
insert into tb_usuarios (nombre_usuario,clave,nombre,apellidos,usu_defecto) 
values('admin','D/4avRoIIVNTwjPW4AlhPpXuxCU4Mqdhryj/N6xaFQw=','default name','default surnames',1);

-- ROLES INICIALES --------
insert into tb_roles (nombre_rol,permisos) 
values('admin','1,2');

insert into tb_roles (nombre_rol,permisos) 
values('responsible','1');

insert into tb_roles (nombre_rol) 
values('contact');

COMMIT;
