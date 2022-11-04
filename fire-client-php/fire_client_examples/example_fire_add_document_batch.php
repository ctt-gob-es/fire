<html>
 <head>
  <title>Agregar documentos al lote de firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include '../fire_client.php';
	
	$appId = $_GET["appid"];		// Identificador de la aplicacion (dada de alta previamente en el sistema)
	$subjectId = $_GET["sid"];		// DNI de la persona
	$transactionId = $_GET["trid"];	// Identificador de la transaccion
	$documentB64 = base64_encode("Hola Mundo!!");				// Simulacion de documento para agregarlo al lote
	$confB64 = null;
	
	$fireClient = new FireClient($appId); // Identificador de la aplicacion (dada de alta previamente en el sistema)
	// Funcion del API de FIRe para agregar documentos al lote
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
		echo "Error ", $e->getCode(), ": ", $e->getMessage(), "\n";
		return;
	}

	$documentB64 = base64_encode("Prueba de firma con contenido implicito (Attached)");

	try {
		$extraParams = base64_encode("mode=implicit\nfilters=keyusage.nonrepudiation:true;nonexpired:\nuseManifest=true\nuri1=urn:id:3086\nmd1=4hrn/3Y9c/fn/uyq12w+D9A2aKc=\nmimeType1=plain/xml\nprecalculatedHashAlgorithm=SHA-1");
		$fireClient->addCustomDocumentToBatch(
			$subjectId,		// DNI de la persona
			$transactionId,	// Identificador de transaccion recuperado en la operacion createBatch()
			"0002",			// Identificador del documento
			$documentB64,	// Documento a incluir 
			"sign",			// Operacion criptografica (sign, cosign o countersign)
			"XAdES",		// Formato de firma (CAdES, XAdES, PAdES...)
			$extraParams,	// Configuracion del formato de firma en base 64 (propiedades). El equivalente al extraParams del Cliente @firma
			null,			// Formato de actualizacion
			$confB64		// Configuracion del servicio en base 64
		);
		echo "<br><b>Fichero 2 con parametros de firma propios incluido en el batch correctamente</b><br>";
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	echo "<a href=\"example_fire_sign_batch.php?appid=", $appId, "&sid=", $subjectId, "&trid=", $transactionId, "\">Firmar lote >></a>";
 ?>
 </body>
</html>