<html>
 <head>
  <title>Agregar documentos al lote de firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include 'fire_client.php';
	
	//$appId = "7BA5453995EC";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - PREPRODUCCION
	$appId = "B244E473466F";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - LOCAL
	$subjectId = "00001";		// DNI de la persona
	$transactionId = "1bf458ac-6940-4be9-bd26-86d6ba82f898";	// Identificador de la transaccion
	$documentB64 = base64_encode("Hola Mundo!!");				// Simulacion de documento para agregarlo al batch
	$confB64 = null;
	
	$fireClient = new FireClient($appId); // Identificador de la aplicacion (dada de alta previamente en el sistema)
	$generateResult;
	try {
		$fireClient->addDocumentToBatch(
			$subjectId,		// DNI de la persona
			$transactionId,	// Identificador de transaccion recuperado en la operacion createBatch()
			"0001",			// Identificador del documento
			$documentB64,	// Documento a incluir
			$confB64		// Configuracion del servicio en base 64
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
			"CAdES",		// Formato de firma (CAdES, XAdES, PAdES...)
			null,			// Configuracion del formato de firma en base 64 (propiedades). El equivalente al extraParams del MiniApplet de @firma
			null,			// Actualizacion
			$confB64		// Configuracion del servicio en base 64
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