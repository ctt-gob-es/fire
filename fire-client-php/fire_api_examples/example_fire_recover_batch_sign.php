<html>
 <head>
  <title>Ejemplo recoverBatchSign</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include '../fire_api.php';
	
	$dataB64 = base64_encode("Hola Mundo!!");
	
	$appId = "B244E473466F";	// Identificador de aplicacion
	$subjectId = "00001";		// DNI de la persona
	$transactionId = "ba8ef7d0-c3ec-4e21-8f61-6d539da1c4b4";	// Identificador de la transaccion
	
	// Funcion del API de FIRe para cargar los datos a firmar
	$transactionResult;
	
	$docId = "0001";
	try {
		$transactionResult = recoverBatchSign(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$subjectId,		// DNI de la persona
			$transactionId,	// Identificador de transaccion recuperado en la operacion createBatch()
			$docId			// Identificador del documento
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	// Mostramos el resultado obtenido
	echo "<br><b>Firma ".$docId.":</b><br>".(base64_encode($transactionResult->result));
	
	$docId = "0002";
	try {
		$transactionResult = recoverBatchSign(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$subjectId,		// DNI de la persona
			$transactionId,	// Identificador de transaccion recuperado en la operacion createBatch()
			$docId			// Identificador del documento
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}
	
	// Mostramos el resultado obtenido
	echo "<br><b>Firma ".$docId.":</b><br>".(base64_encode($transactionResult->result));
	
 ?>
 </body>
</html>