<html>
 <head>
  <title>Ejemplo de addDocumentToBatch</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include 'fire_client.php';
	
	
	//$appId = "7BA5453995EC";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - PREPRODUCCION
	$appId = "B244E473466F";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - LOCAL
	$subjectId = "00001";		// DNI de la persona
	$transactionId = "dd935292-07f2-4d0e-a043-6ce162ed2e73";	// Identificador de la transaccion
	$documentB64 = base64_encode("Hola Mundo!!");				// Simulacion de documento para agregarlo al batch
	$conf = "redirectOkUrl=http://www.google.es"."\n".	// URL a la que llegara si el usuario se autentica correctamente
			"redirectErrorUrl=http://www.ibm.com";		// URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
	$confB64 = base64_encode($conf);
	
	
	$fireClient = new FireClient($appId); // Identificador de la aplicacion (dada de alta previamente en el sistema)
	$generateResult;
	try {
		$fireClient->addDocumentToBatch(
			$subjectId,		// DNI de la persona
			$transactionId,	// Identificador de transaccion recuperado en la operacion createBatch()
			"0001",			// Identificador del documento
			$documentB64,	// Documento a incluir
			$confB64		// Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
		);
		echo "<br><b>Fichero 1 incluido en el batch correctamente</b><br>";
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}
	try {
		$fireClient->addCustomDocumentToBatch(
			$subjectId,		// DNI de la persona
			$transactionId,	// Identificador de transaccion recuperado en la operacion createBatch()
			"0002",			// Identificador del documento
			$documentB64,	// Documento a incluir 
			"sign",			// Operacion criptografica (sign, cosign o countersign)
			"XAdES",		// Formato de firma (CAdES, XAdES, PAdES...)
			null,			// Configuracion del formato de firma en base 64 (propiedades). El equivalente al extraParams del MiniApplet de @firma
			null,			// Actualizacion
			$confB64		// Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
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