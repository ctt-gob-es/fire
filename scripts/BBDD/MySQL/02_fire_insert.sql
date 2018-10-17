-- ********************************************************
-- **************** Inserción datos ********************
-- ********************************************************

-- INSERTAR DATOS--------------

-- USUARIO POR DEFECTO --------
insert into tb_usuarios (nombre_usuario,clave,nombre,apellidos,usu_defecto) 

values('admin','D/4avRoIIVNTwjPW4AlhPpXuxCU4Mqdhryj/N6xaFQw=','default name','default surnames',1);

-- RELLENA TABLA DE ALGORITMOS POR DEFECTO -- 
INSERT INTO `tb_algoritmos` (`id_algoritmo`,`nombre`) VALUES (1,'SHA1withRSA');
INSERT INTO `tb_algoritmos` (`id_algoritmo`,`nombre`) VALUES (2,'SHA256withRSA');
INSERT INTO `tb_algoritmos` (`id_algoritmo`,`nombre`) VALUES (3,'SHA384withRSA');
INSERT INTO `tb_algoritmos` (`id_algoritmo`,`nombre`) VALUES (4,'SHA512withRSA');
INSERT INTO `tb_algoritmos` (`id_algoritmo`,`nombre`) VALUES (99,'Otro');

-- RELLENA TABLA DE FORMATOS POR DEFECTO --

INSERT INTO `tb_formatos` (`id_formato`,`nombre`) VALUES (1,'CAdES');
INSERT INTO `tb_formatos` (`id_formato`,`nombre`) VALUES (2,'XAdES');
INSERT INTO `tb_formatos` (`id_formato`,`nombre`) VALUES (3,'PAdES');
INSERT INTO `tb_formatos` (`id_formato`,`nombre`) VALUES (4,'FacturaE');
INSERT INTO `tb_formatos` (`id_formato`,`nombre`) VALUES (5,'CAdES-ASiC-S');
INSERT INTO `tb_formatos` (`id_formato`,`nombre`) VALUES (6,'XAdES-ASiC-S');
INSERT INTO `tb_formatos` (`id_formato`,`nombre`) VALUES (99,'Otro');

-- RELLENA TABLA DE FORMATOS  MEJORADOS POR DEFECTO --

INSERT INTO `tb_formatos_mejorados` (`id_formato_mejorado`,`nombre`) VALUES (1,'ES-A');
INSERT INTO `tb_formatos_mejorados` (`id_formato_mejorado`,`nombre`) VALUES (2,'ES-T');
INSERT INTO `tb_formatos_mejorados` (`id_formato_mejorado`,`nombre`) VALUES (3,'ES-C');
INSERT INTO `tb_formatos_mejorados` (`id_formato_mejorado`,`nombre`) VALUES (4,'ES-X');
INSERT INTO `tb_formatos_mejorados` (`id_formato_mejorado`,`nombre`) VALUES (5,'ES-X-1');
INSERT INTO `tb_formatos_mejorados` (`id_formato_mejorado`,`nombre`) VALUES (6,'ES-X-2');
INSERT INTO `tb_formatos_mejorados` (`id_formato_mejorado`,`nombre`) VALUES (7,'ES-X-L-1');
INSERT INTO `tb_formatos_mejorados` (`id_formato_mejorado`,`nombre`) VALUES (8,'ES-X-L-2');
INSERT INTO `tb_formatos_mejorados` (`id_formato_mejorado`,`nombre`) VALUES (9,'ES-LTV');
INSERT INTO `tb_formatos_mejorados` (`id_formato_mejorado`,`nombre`) VALUES (10,'T-LEVEL');
INSERT INTO `tb_formatos_mejorados` (`id_formato_mejorado`,`nombre`) VALUES (11,'LT-LEVEL');
INSERT INTO `tb_formatos_mejorados` (`id_formato_mejorado`,`nombre`) VALUES (12,'LTA-LEVEL');
INSERT INTO `tb_formatos_mejorados` (`id_formato_mejorado`,`nombre`) VALUES (99,'OTROS');

-- RELLENA TABLA DE PROVEEDORES POR DEFECTO , estos datos deberán coincidir con los que se indiquenn en el fichero config.properties del componente central --
INSERT INTO `tb_proveedores` (`id_proveedor`,`nombre`,`conector`) VALUES (1,'clavefirmatest','es.gob.fire.server.connector.test.TestConnector');
INSERT INTO `tb_proveedores` (`id_proveedor`,`nombre`,`conector`) VALUES (2,'local',NULL);
INSERT INTO `tb_proveedores` (`id_proveedor`,`nombre`,`conector`) VALUES (99,'Otro',NULL);

-- RELLENA TABLA DE NAVEGADORES  POR DEFECTO --

INSERT INTO `tb_navegadores` (`id_navegador`,`nombre`) VALUES (1,'Internet Explorer');
INSERT INTO `tb_navegadores` (`id_navegador`,`nombre`) VALUES (2,'Edge');
INSERT INTO `tb_navegadores` (`id_navegador`,`nombre`) VALUES (3,'Firefox');
INSERT INTO `tb_navegadores` (`id_navegador`,`nombre`) VALUES (4,'Chrome');
INSERT INTO `tb_navegadores` (`id_navegador`,`nombre`) VALUES (5,'Safari');
INSERT INTO `tb_navegadores` (`id_navegador`,`nombre`) VALUES (6,'Opera');
INSERT INTO `tb_navegadores` (`id_navegador`,`nombre`) VALUES (99,'Otro');

-- RELLENA TABLA DE OPWERACIONES  POR DEFECTO --
INSERT INTO `tb_operaciones` (`id_operacion`,`nombre`) VALUES (1,'Firma simple');
INSERT INTO `tb_operaciones` (`id_operacion`,`nombre`) VALUES (2,'Firma de  lotes');
INSERT INTO `tb_operaciones` (`id_operacion`,`nombre`) VALUES (99,'Otros');

COMMIT;
