# FIRe
FIRe es una solución que simplifica la integración de procesos de firma electrónica de usuario en trámites web al concentrar en un solo componente todos los requisitos de creación de firmas basadas tanto en certificados locales como en certificados en la nube. 

## Licencia

FIRe es software libre y se publica con licencia [GPL 2+](https://www.gnu.org/licenses/old-licenses/gpl-2.0.html) y [EUPL 1.1](http://ec.europa.eu/idabc/servlets/Docb4f4.pdf). Puede consular más información sobre el producto en el  [Portal de Administración electrónica](https://administracionelectronica.gob.es/ctt/fire).

## Notas de construcción

* El proyecto FIRe está preparado para ser construído mediante la herramienta Apache Maven.

* Aunque FIRe es un proyecto de fuentes abiertas, algunos de los conectores para la conexión con los servicios de firma en la nube pueden requerir bibliotecas propietarias que no se encuentran en los repositorios públicos de Maven. Los conectores conocidos que requieren bibliotecas privadas son:

	* Cl@ve Firma: Las bibliotecas de conexión con el sistema de custodia de Cl@ve Firma no se encuentran en repositorios públicos ni se atienen a la licencia de este producto. Los organismos públicos que requieran la compilación de este conector pueden solicitar las dependencias necesarias a los responsables del proyecto o tomar las bibliotecas del empaquetado que ellos proporcionan.
		* La dependencia Maven del conector al servicio de Cl@ve Firma es:
			* `es.gob.fnmt:fnmt-fire-connector`
		* La dependencia Maven necesaria para poder compilar y empaquetar el conector con Cl@ve Firma es:
			* `com.openlandsw.rss:gateway-api`

	* CloudId: El conector para el acceso al servicio de firma de la FNMT ha sido desarrollado por la propia entidad y se requiere autorización de esta para su uso.
		* La dependencia Maven del conector al servicio de la FNMT es:
			* `es.gob.fnmt:fnmt-fire-connector`

* Para construir los artefactos comunes deberá usarse el perfil "main" o no indicar perfil: `mvn clean install`

* Para construir los servicios de FIRe deberá usarse el perfil "services": `mvn clean install -Pservices`

* Para construir el conector de Cl@ve Firma (requiere bibliotecas privadas) deberá usarse el perfil "claveFirma": `mvn clean install -Pclavefirma`

* Para construir el conector de CloudId (requiere bibliotecas privadas) deberá usarse el perfil "fnmt": `mvn clean install -Pfnmt`

* Para construir el servicio de FIRe con distintos componentes, se puede acceder al directorio del módulo **fire-signature** e indicar los componentes durante la contrucción:

	* Para la construcción de todos los componentes: `mvn clean install -Dall`
	
	* Para la construcción con el conector de Cl@ve Firma se debe agregar el perfil "clavefirma": `mvn clean install -Pclavefirma`
	
	* Para la construcción con el conector de CloudId se debe agregar el perfil "fnmt": `mvn clean install -Pfnmt`
	
	* Para la construcción con el módulo para la validación y actualización de firmas con @firma se debe agregar el perfil "afirma": `mvn clean install -Pafirma`
	
	* Para la construcción con el módulo para el envío de alertas a GrayLog se debe agregar el perfil "graylog": `mvn clean install -Pgraylog`
	
	* Para la construcción con el módulo para el envío de alertas a Monitoriz@ se debe agregar el perfil "eventmanager": `mvn clean install -Peventmanager`

* Para evitar la ejecución de las pruebas unitarias durante la contrucción de cualquier módulo, se debe agregar al comando en cuestión el parámetro '-DskipTests'.

## Arquitectura

El sistema FIRe está compuesto principalmente por dos elementos:

1. Componente central

   Es el servidor en el que las aplicaciones delegan todas las labores de firma. Está a su vez compuesto por varios subcomponentes que se despliegan en un contenedor de aplicaciones java
   
    - Componente de firma **(fire-signature)**
    - Simulador de Cl@veFirma **(clavefirma-test-services)**
    - Aplicación de administración **(fire-admin-web)**
   
2. Cliente distruido

   Está formado por las librerías de integración **(fire-client)** que utilizan las aplicaciones para conectarse al componente central. Las librerías se proporcionan en los lenguajes Java, .Net y PHP.
   
Junto a estos elementos se distribuye una aplicación de prueba en lenguaje java **(fire-test-jsp)** que permite demostrar las funcionalidades del sistema y que puede servir de ejemplo para desarrollos propios. También se pueden encontrar páginas de prueba ASP y PHP en los módulos de los correspondientes componentes distribuidos (**fire-client-dotnet** y **fire-client-php**).
   

## Release notes

Principales cambios en la historia de versiones:

### v2.4-SNAPSHOT (listado temporalmente incompleto)

#### Componente central

 - [RFE] Se integra un nuevo sistema de gestión de logs que permite la salida de los logs a un fichero independiente.
 - [RFE] Cambios en los logs del sistema para un mejor seguimiento de las transacciones.
 - [RFE] Como resultado de las operaciones de firma simple y firma de lote, también se envía a las aplicaciones el certificado utilizado para firmar.
 - [RFE] Se sustituye el envío de estadísticas a Google Analytics por el guardado de datos para su explotación desde el módulo de administración.
 - [RFE] Se permite configurar si no se quieren generar los datos de estadísticas, si sólo se desean almacenar los datos en disco o si se desean guardar en disco y realizar un volcado diario a base de datos.
 - [RFE] El conector de Cl@ve Firma convierte los Id de usuario a mayúsculas para facilitar su reconocimiento por el proveedor.
 - [RFE] Se permite configurar múltiples veces un mismo conector para poder acceder con él a distintos proveedores de firma en la nube.
 - [RFE] Se deniegan los accesos de las aplicaciones desactivadas desde la interfaz de administración.
 - [RFE] Se permite utilizar un fichero de configuracion para el gestor de documentos por defecto.
 - [RFE] Se restaura la conexión con la BD cuando está disponible aún a pesar de haberse producido errores durante el arranque del componente.
 - [RFE] Se permite configurar plataformas de actualización y validación de firmas distintas a la Plataforma @firma (la lógica de conexión debe implementarla el organismo interesado).
 - [RFE] Se introduce la operación de validación de las firmas generadas como alternativa a la actualización de las firmas a formatos longevos (la actualización ya implica la validación).
 - [RFE] Se permite que las aplicaciones configuren que se obvie el periodo de gracia de las firmas.
 - [RFE] Se integra el soporte de actualización asíncrona de firmas (aquellas que requieren la espera de un periodo de gracia para actualizar).
 - [RFE] Se permite configurar la recuperación de las firmas parcialmente actualizadas.
 - [RFE] Se permite restringir el acceso a gestores de documentos por parte de aplicaciones.
 - [RFE] Se incluyen nuevas interfaces y clases para la creación de gestores de documentos compatibles con la generación de firmas asíncronas y firmas parcialmente actualizadas.
 - [RFE] Se permite limitar qué aplicaciones pueden hacer uso de un gestor de documentos o cualquier no pueden usarlo.
 - [RFE] Se actualiza al Cliente @firma 1.7 (con lo que se elimina el soporte del MiniApplet y AutoFirma WebStart).
 - [RFE] Se corrige la carga del Cliente @firma cuando se configura expresamente la URL pública de acceso.
 - [RFE] Se muestra el número de versión de FIRe en el pie de las página web accesibles por los usuarios.
 - [RFE] Se permite configurar un usuario/contraseña para la autenticación frente a un proxy de red a través de las variables de entorno de Java.
 - [BUG] Se corrige el conector con el servicio de pruebas, para que no sea necesario que se le configure un almacén para la autenticación SSL cliente, aun cuando puede ser no necesaria esta autenticación.
 - [BUG] Se permite el uso del parámetro "headless" para la selección automática de un certificado local durante la operación de firma de lotes.
 - [BUG] Se corrige el que se estableciese la política de firma de la AGE cuando se indicaba el parámetro "expPolicy" y un formato de firma que no la soportase (FacturaE, NONE, CAdES-ASiC o XAdES-ASiC).
 - [BUG] Se mejora de la compatibilidad con los clientes móviles que no devuelven el certificado de firma. Ahora no da error al recuperar las firmas, pero aún pueden fallar si el DocumentManager lo exije.
 - [BUG] Corrección de la compatibilidad para la actualización a formatos T-Level, LT-Level y LTA-Level.
 - [BUG] Se corrige el error en los parámetros de configuración (extraParams) utilizados cuando se ejecutaban firmas con certificado local.
 - [BUG] Se corrige error al notificar el fin de la transacción a Cl@ve Firma.
 - [BUG] Se corrige el que se redirigiese a la página de error de FIRe cuando fallaba la operación de firma en la nube y se hubiese establecido el uso de un proveedor de firma concreto.


#### Módulo de administración.

 - [RFE] Nuevo diseño de la interfaz gráfica.
 - [RFE] Se integra un nuevo apartado para la consulta de logs del componente central.
 - [RFE] Se integra un nuevo apartado para la visualización de informes predefinidos sobre los datos estadísticos almacenados.
 - [RFE] Se introduce un mecanismo para la recuperación de contraseña de los administradores.
 - [RFE] Se introducen los roles de usuario.
 - [RFE] Mejoras genéricas en la asociación y presentación de la información de aplicaciones, certificados y usuarios.


#### Aplicación de carga de estadísticas

 - [RFE] Se agrega una aplicación independiente para la carga en base de datos de las estadísticas generadas por el componente central.
 
 #### Clientes distribuidos (General)
 - [RFE] Se obtiene más información de las firmas recuperadas: certificado utilizado para firmar, formato longevo al que se actualizaron y periodo de gracia que hay que esperar.
 - [RFE] Se permite establecer configuración adicional para la configuración de la Plataforma de validación y actualización de firmas.
 - [RFE] Se incluye un método para la recuperación asíncrona de firmas (firmas para las que se ha pedido esperar un periodo de gracia).
 - [RFE] Se identifica cuando el error en la firma se originó en el gestor de documentos del servidor.
 - [RFE] Se incluye en el antiguo API para la operación directa con el proveedor de firma en la nube un parámetro para la selección del proveedor.
 - [BUG] Se recupera el funcionamiento del antiguo API para la operación directa con el proveedor de firma en la nube.


#### Cliente distribuido Java

 - [RFE] Se permite indicar el alias del certificado del almacén de certificados cliente para la conexión con el componente central.


#### Cliente distribuido .NET

 - [RFE] Se permite indicar el alias del certificado del almacén de certificados cliente para la conexión con el componente central.
 - [RFE] Se actualizan las páginas de prueba .NET y se incluye una página de prueba para la recuperación asíncrona de una firma.


#### Cliente distribuido PHP

 - [RFE] Se actualiza al modelo de PHP 5 y superiores
 - [RFE] Se agrega el cliente "fire_client.php" con el mismo modelo de cliente que ya existía en los componentes Java y .NET.
 - [RFE] Se crean páginas de prueba PHP con el nuevo "fire_client.php" y se incluye una página de prueba para la recuperación asíncrona de una firma.
 - [BUG] Se actualiza el cliente "fire_api.php" para corregir los problemas de compatibilidad con los despliegues de alta compatibilidad (requiere cambios en la aplicación que integra el cliente).
 - [BUG] Se actualizan las páginas de prueba PHP con los cambios del API.
 - [BUG] Se corrige que no se aplicase el formato de actualización configurado.


#### Servicio simulador de Cl@ve Firma

 - [RFE] Se incluyen certificados de prueba para los usuarios de prueba de Cl@ve.

#### Aplicación de pruebas

 - [RFE] Se simplifica a los desarrolladores la configuración de los parametros de firma (extraParms) en la página de firma simple.
 - [RFE] Se incluye el soporte de firmas con periodo de gracia.

----------

### v2.3

#### Componente central

 - [RFE] Se permite que los valores de las propiedades de los ficheros de configuración aparezcan cifrados.
 - [RFE] Se permite la configuración de una clase encargada de descifrar los valores cifrados de los ficheros de configuración.
 - [RFE] Se busca por defecto el certificado cliente de las peticiones en el atributo de cabecera "x-clientcert" cuando no se recibe directamente o a través de AJP. El valor establecido en la propiedad "http.cert.attr" sustituye a este valor por defecto.
 - [RFE] Nuevos mensajes de log para la identificación de errores.
 - [BUG] Se corrige el uso de filtros de certificados con el proveedor de AutoFirma en los procesos de firma de lotes.
 - [BUG] Se corrige la redirección errónea cuando se genera/renueva un certificado en la nube y se tiene diferenciados por URL los contextos público y privado de FIRe con la propiedad "pages.public.url".
 - [BUG] Se corrige la redirección a un error 403 cuando está activado el sistema de despliegue en múltiples nodos y no se pueden recuperar los certificados del usuario (el usuario no está dado de alta, certificados bloqueados,...)
 - [BUG] Se eliminan las conexiones contra el dominio "soapinterop.org".
 - [BUG] Se eliminan periódicamente los ficheros temporales que se quedaban huérfanos tras interrumpirse inesperadamente la sesión a la que pertenecían.
 - [BUG] Se corrige la visualización del botón "Cancelar" de la pantalla de selección de proveedor cuando se utiliza Internet Explorer.

#### Cliente distribuido Java

 - [RFE] Se permite que las contraseñas de las propiedades de configuración aparezcan cifradas.
 - [RFE] Se permite la configuración de una clase encargada de descifrar los valores de las propiedades de configuración.
 - [RFE] Se sustituye el uso de la Java Logging API por SLF4J para que el integrador seleccione el sistema de logs a utilizar.

#### Módulo de administración.

 - [RFE] Se permite que los valores de las propiedades de los ficheros de configuración aparezcan cifrados.
 - [RFE] Se permite la configuración de una clase encargada de descifrar los valores cifrados de los ficheros de configuración.

#### Aplicación de pruebas

 - [RFE] El fichero de configuración app_config.properties se busca primeramente en el directorio designado por la variable de entorno "fire.config.path".
 - [RFE] Se sustituye el uso de la Java Logging API por SLF4J. Se sigue utilizando Java Logging API como implementación.

----------

### v2.2

#### Componente central

 - [RFE] Se agrega la compatibilidad con múltiples proveedores de firma.
 - [RFE] Se incorpora el proveedor de firma de la Fábrica Nacional de Moneda y Timbre para firma de funcionarios públicos.
 - [RFE] Se permite configurar la URL pública que se devolverá a los componentes distribuidos para dar acceso al usuario a las páginas de FIRe.
 - [RFE] Se permite configurar si el proveedor de Cl@ve Firma permite a los usuarios emitir nuevos certificados de firma.
 - [RFE] Se permite configurar si el simulador de Cl@ve Firma permite a los usuarios emitir nuevos certificados de firma.
 - [RFE] Se permite configurar desde las aplicaciones cliente el listado de proveedores que que se desea mostrar a los usuarios.
 - [RFE] Se permite configurar desde las aplicaciones cliente si debe utilizarse AutoFirma WebStart o AutoFirma nativo para la firma con certificado local.
 - [RFE] Se permite configurar un atributo de las cabeceras de las peticiones HTTP del que recoger el certificado de autenticación de los componentes distribuidos. Esto permite el despliegue de FIRe en algunos entornos complejos.
 - [RFE] Se muestra un diálogo de espera después de pulsar el botón firmar con certificado local.
 - [RFE] Cuando ocurre un error al conectar con un proveedor de firmas o al recuperar sus certificados, se muestra el error y se permite seleccionar otro proveedor.

#### Componentes ditribuidos

 - [RFE] Se obtiene como parte del resultado de las operaciones de firma simple y firma de lotes, el nombre del proveedor utilizado.

#### Servicio simulador de Cl@ve Firma

 - [RFE] Se muestran los nombres y títulos de fichero en la página que emula a la pasarela de Cl@ve Firma.

#### Módulo de administración.

 - [RFE] Se crean pantallas de gestión de usuarios, altas, bajas, modificación y consulta 
 - [RFE] Se permite cambio de contraseña del usuario logado.
 - [RFE] Se crea menú principal de navegación.
 - [RFE] Se crea icono de cierre de sesión de usuario.
 - [RFE] Se crean pantallas de gestión de certificados, altas, bajas,modificaciones y consulta. Con previsualización del certificado, carga del certificado mediante fichero y descarga del certificado en archivo .cer
 - [RFE] Se modifica la gestión de aplicaciones adaptado a la nueva gestion de certificados.
 - [RFE] Se mejora la validación de campos obligatorios y formato en los formularios.
 - [RFE] Se mejora la visualización de los listados mediate tablas paginadas.

----------

### v2.1.1

#### Componente central

 - [RFE] Se refirman el MiniApplet 1.6 y AutoFirma WebStart 1.6 para evitar problemas de ejecución.
 - [BUG] Se corrije un error por el que las sesiones en displiegues del componente central sobre múltiples nodos cuando las transacciones se iniciaban en un nodo, se continuaban en otro y despúes volvían al primero.

----------

### v2.1

#### Componente central
  
 - [RFE] Se introduce la posibilidad de introducir y configurar un gestor de documentos que le indique a las operaciones de firma y lotes de donde tomar y donde guardar los datos en lugar de recibirlos directamente.
 - [RFE] Se adminte el uso del formato "NONE" que permite realizar una firma digital PKCS#1 sobre los datos indicados.
 - [RFE] Se actualiza el MiniApplet y AutoFirma WebStart a las versiones 1.6.
 - [RFE] Actualización de los servicios trifásicos con los cambios del Cliente @firma 1.6.
 - [RFE] Se incluye un nuevo tipo de error para detectar cuando se produce un fallo después de llamar a la pasarela de autorización o emisión de certificado.
 - [RFE] Se crea un mecanismo para la gestión compartida de sesiones a través de disco por varios nodos balanceados (necesario cuando se despliega el componente central en varios servidores balanceados sin clúster).
 - [RFE] Mejora en la gestión del borrado de temporales.
 - [RFE] Se comprueba el formato longevo al que se actualizado una firma para identificar si la mejora no fue completa con respecto a lo que se solicitó.
 - [BUG] Corrección del error en el tratamiento de los nombres y título de los documentos enviados a firmar cuando tienen caracteres no ANSI básicos (tíldes, eñes...). Se hace obligatorio el uso de la codificación UTF-8.
 - [BUG] La página de carga del Cliente @firma ignora las mayúsculas en la selección del método de firma (sign, cosign o countersign). Esto resuelve el error de selección de la operación con el método de firma con enumerados.
 
#### Cliente distribuido Java

 - [RFE] Se introduce un nuevo API que permite proporcionar directamente la configuración de la conexión con el componente central.
 - [RFE] Se agregan nuevas excepciones para describir errores durante la recuperación de la firma de un lote.
 
#### Cliente distribuido .NET

 - [RFE] Se introduce un nuevo API que permite proporcionar directamente la configuración de la conexión con el componente central.
 - [RFE] Se agregan nuevas excepciones para describir errores al agregar documentos a un lote.
 - [RFE] Se agregan nuevas excepciones para describir errores durante la recuperación de la firma de un lote.
 - [RFE] e modifica el formato de devolución de los datos de firma para que se puedan obtener como binarios.
 - [BUG] Se agrega al método de recuperación de firma el parámetro necesario para permitir su actualización a formatos longevos.
 - [BUG] Corrección en la carga de la configuración del registro de la conexión SSL.
 - [BUG] Se corrige el envío de los parámetros extra para la configuración de las firmas.
 - [BUG] Se corrige el envío de la configuración del comportamiento de la firma de lote cuando se encuentra un error. Siempre se continuaba con el resto de firmas.
 
#### Cliente distribuido PHP

 - [RFE] Se agregan nuevas excepciones para describir errores al agregar documentos a un lote.
 - [RFE] Se agregan nuevas excepciones para describir errores durante la recuperación de la firma de un lote.
 - [BUG] Se corrige la declaración de excepciones para eliminar algunas que nunca se lanzaban.
 - [BUG] Se agrega al método de recuperación de firma el parámetro necesario para permitir su actualización a formatos longevos.
 
#### Aplicación de pruebas

 - [RFE] Se modifica para hacer uso del nuevo componente distribuido Java.

----------

### v2.0.1

#### Componente central

 - [RFE] Se sustituye el MiniApplet 1.5 incluida por la misma versión firmada por el MinHAFP.
 - [RFE] Se modifica la configuración por defecto para que se utilice AutoFirma nativo para las firmas con certificado local.

----------

### v2.0

Se cambia el nombre del proyecto a FIRe

#### Componente central

 - [RFE] Se integra la opción de incluir la selección de certificado como parte del proceso de firma.
 - [RFE] Se integra el despliegue del MiniApplet @firma y AutoFirma vía JNLP para su uso como parte del proceso de firma.
 - [RFE] Se integran las funciones de firma de lotes para la firma de múltiples documentos.
 - [RFE] Se integran páginas web para la selección del origen del certificado, selección del certificado en la nube, expedición de certificados y firma con certificado cliente.

#### Componente distribuido

 - [RFE] Se agrega un nuevo API desde el que invocar las nuevas funciones de firma (simples y de lotes), independiente del origen del certificado.
 - [RFE] Se agrega un método para obtener los errores internos producidos en servidor durante la ejecución de la transacción.
 - [RFE] [java] Se elimina la dependencia con el módulo core del Cliente @firma (afirma-core.jar).

#### Aplicación de pruebas

 - [RFE] Aplicación de pruebas;		Uso del nuevo componente distribuido Java.
 - [RFE] Aplicación de pruebas;		Uso del modelo de selección de certificado integrada en el proceso de firma.
 - [RFE] Aplicación de pruebas;		Selección del origen del certificado (ClaveFirma o certificado local) automático y mediante configuración.
 - [RFE] Aplicación de pruebas;		Carga de los datos a firmar en forma de fichero.

----------

### v1.0

Versión inicial con Cl@veFirma
