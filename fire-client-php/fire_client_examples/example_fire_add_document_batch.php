<html>
 <head>
  <title>Agregar documentos al lote de firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include '../fire_client.php';
	
	session_start();
	$appId = $_SESSION["appid"];		// Identificador de la aplicacion dada de alta previamente en el sistema
	$subjectId = $_SESSION["sid"];		// DNI de la persona
	$transactionId = $_SESSION["trid"];	// Identificador de la transaccion
	$confB64 = null;
	
	// Creamos el cliente (tambien podriamos reutilizar uno anterior si lo tuviesemos ya creado)
	$fireClient = new FireClient($appId);
	
	// Agregamos un documento al lote usando la configuracion por defecto del lote
	$documentB64 = base64_encode("Hola Mundo!!");				// Simulacion de documento para agregarlo al lote
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

	// Agregamos un segundo documento al lote, pero ahora aplicamos una configuracion propia
	$documentB64 = base64_encode("Prueba de firma XAdES)");
	try {
		$extraParams = base64_encode("format=XAdES Detached");
		$fireClient->addCustomDocumentToBatch(
			$subjectId,		// DNI de la persona
			$transactionId,	// Identificador de transaccion recuperado en la operacion createBatch()
			"0002",			// Identificador del documento
			$documentB64,	// Documento a incluir 
			"sign",			// Operacion criptografica (sign, cosign o countersign)
			"XAdES",		// Formato de firma (CAdES, XAdES, PAdES...)
			$extraParams,	// Configuracion del formato de firma en base 64 (propiedades). El equivalente al extraParams de Autofirma
			null,			// Formato de actualizacion
			$confB64		// Configuracion del servicio en base 64
		);
		echo "<br><b>Fichero 2 con parametros de firma propios incluido en el batch correctamente</b><br>";
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	echo "<a href=\"example_fire_sign_batch.php\">Firmar lote >></a>";
 ?>
 </body>
</html>