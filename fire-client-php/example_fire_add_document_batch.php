<html>
 <head>
  <title>Ejemplo de addDocumentToBatch</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include 'fire_api.php';
		
	$conf = "redirectOkUrl=http://www.google.es"."\n".	// URL a la que llegara si el usuario se autentica correctamente
			"redirectErrorUrl=http://www.ibm.com";		// URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
	$confB64 = base64_encode($conf);
	
	// Identificador de la aplicacion (dada de alta previamente en el sistema)
	//$appId = "7BA5453995EC";	// Entorno preproduccion
	$appId = "B244E473466F";	// Entorno local
	// Identificador de la transaccion
	$transactionId = "962db6f1-46a4-47ce-93ad-5007816aa497";
	// Simulacion de documento para agregarlo al batch
	$documentB64 = base64_encode("Hola Mundo!!");
	// Funcion del API de Clave Firma para generar un nuevo certificado
	$generateResult;
	try {
		addDocumentToBatch(
			$appId,				// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$transactionId,		// Identificador de transaccion recuperado en la operacion createBatch()
			"0001",				// Identificador del documento
			$documentB64,			// Documento a incluir
			$confB64			// Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
		);
		echo "<br><b>Fichero 1 incluido en el batch correctamente</b><br>";
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}
	try {
		addCustomDocumentToBatch(
			$appId,				// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$transactionId,		// Identificador de transaccion recuperado en la operacion createBatch()
			"0002",				// Identificador del documento
			$documentB64,			// Documento a incluir 
			"sign",			// Operacion criptografica (sign, cosign o countersign)
			"CAdES",		// Formato de firma (CAdES, XAdES, PAdES...)
			null,			// Configuracion del formato de firma en base 64 (propiedades). El equivalente al extraParams del MiniApplet de @firma
			null,			// Actualizacion
			$confB64			// Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
		);
		echo "<br><b>Fichero 2 con parametros de firma propios incluido en el batch correctamente</b><br>";
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

 ?>
 </body>
</html>