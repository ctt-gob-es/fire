-- 1 Crear la tablas nuevas 
CREATE TABLE `tb_aplicaciones_new` (
  `id` varchar(48) NOT NULL,
  `nombre` varchar(45) NOT NULL,
  `responsable` varchar(45) NOT NULL,
  `resp_correo` varchar(45) DEFAULT NULL,
  `resp_telefono` varchar(30) DEFAULT NULL,
  `fecha_alta` datetime NOT NULL,
  `fk_certificado` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tb_certificados` (
  `id_certificado` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_cert` varchar(45) NOT NULL,
  `fec_alta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cert_principal` varchar(5000) DEFAULT NULL,
  `cert_backup` varchar(5000) DEFAULT NULL,
  `huella_principal` varchar(45) DEFAULT NULL,
  `huella_backup` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id_certificado`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='tabla de certificados';


CREATE TABLE `tb_usuarios` (
  `id_usuario` int(11) NOT NULL AUTO_INCREMENT COMMENT 'auto-numérico identificativo único',
  `nombre_usuario` varchar(30) NOT NULL COMMENT 'Nombre con el que se identifica en la aplicación',
  `clave` varchar(45) NOT NULL COMMENT 'clave condificada con SHA256, con la que se registra el usuario',
  `nombre` varchar(45) NOT NULL COMMENT 'Nombre completo del usuario',
  `apellidos` varchar(120) NOT NULL COMMENT 'Apellidos del usuario',
  `correo_elec` varchar(45) DEFAULT NULL COMMENT 'Correo electrónico',
  `fec_alta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'fecha de alta del usuario.',
  `telf_contacto` varchar(45) DEFAULT NULL COMMENT 'Teléfono de contacto',
  `rol` varchar(45) NOT NULL DEFAULT 'admin' COMMENT 'Papel (rol) que desempeña dentro de la aplicación',
  `usu_defecto` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `nombre_usuario_UNIQUE` (`nombre_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8;


---FIN CREAR TABLAS NUEVAS----------

----INSERTAR DATOS--------------

-----USUARIO POR DEFECTO --------
insert into tb_usuarios (nombre_usuario,clave,nombre,apellidos,usu_defecto) 

values('admin_pass','D/4avRoIIVNTwjPW4AlhPpXuxCU4Mqdhryj/N6xaFQw=','default name','default surnames',1);

---RESTO USUARIOS -------
insert into tb_usuarios (nombre_usuario,clave,nombre,apellidos)

SELECT parametro,valor,concat(parametro,"_name") as nombre,concat(parametro,"_surname") as apellidos 

FROM tb_configuracion WHERE parametro <> 'admin_pass';

------------CERTIFICADOS-------
insert into tb_certificados (nombre_cert,fec_alta,cert_principal,huella_principal) 
select concat("CERT_",a.id)as nombre_cert,a.fecha_alta,a.cer,a.huella from tb_aplicaciones a

--------APLICACIONES NEW-----------

INSERT INTO tb_aplicaciones_new (id,nombre,fecha_alta,responsable,resp_correo,resp_telefono,fk_certificado)

SELECT a.id, a.nombre,a.fecha_alta,a.responsable,a.resp_correo,a.resp_telefono ,c.id_certificado 

FROM tb_aplicaciones a, tb_certificados c 

WHERE  SUBSTRING_INDEX(c.nombre_cert,'_',-1)=a.id;

CREATE TABLE tb_aplicaciones_old SELECT * FROM tb_aplicaciones;

DROP table tb_aplicaciones ;

ALTER TABLE `fire`.`tb_aplicaciones_new` 
RENAME TO  `fire`.`tb_aplicaciones` ;


ALTER TABLE `fire`.`tb_aplicaciones` 

ADD INDEX `fk_certificado_idx` (`fk_certificado` ASC);

ALTER TABLE `fire`.`tb_aplicaciones` 
ADD CONSTRAINT `fk_certificado`
  
FOREIGN KEY (`fk_certificado`)
  REFERENCES `fire`.`tb_certificados` (`id_certificado`)
  
ON DELETE RESTRICT
  ON UPDATE CASCADE;

ALTER TABLE `fire`.`tb_aplicaciones` DROP FOREIGN KEY `fk_certificado`;

ALTER TABLE `fire`.`tb_aplicaciones` 
CHANGE COLUMN `fk_certificado` `fk_certificado` INT(11) NOT NULL ;

ALTER TABLE `fire`.`tb_aplicaciones` 
ADD CONSTRAINT `fk_certificado`
  FOREIGN KEY (`fk_certificado`)
  
REFERENCES `fire`.`tb_certificados` (`id_certificado`)
  ON UPDATE CASCADE;
