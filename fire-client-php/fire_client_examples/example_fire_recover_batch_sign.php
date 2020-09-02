<html>
 <head>
  <title>Recuperar firma del lote de firmas</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include '../fire_client.php';
	
	
	//$appId = "7BA5453995EC";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - PREPRODUCCION
	$appId = "B244E473466F";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - LOCAL
	$subjectId = "00001";		// DNI de la persona
	$transactionId = "1bf458ac-6940-4be9-bd26-86d6ba82f898";	// Identificador de la transaccion
	$dataB64 = base64_encode("Hola Mundo!!");
	
	
	$fireClient = new FireClient($appId); // Identificador de la aplicacion (dada de alta previamente en el sistema)
	
	// Resultado de la primera firma
	$docId = "0001";
	try {
		$transactionResult = $fireClient->recoverBatchSign(
			$subjectId,			// DNI de la persona
			$transactionId,		// Identificador de transaccion recuperado en la operacion createBatch()
			$docId				// Identificador del documento
		);
		// Mostramos los datos obtenidos
		echo "<b>Firma del documento ", $docId, ":</b><br>" , base64_encode($transactionResult->result), "<br>";
	}
	catch(Exception $e) {
		echo "<b>Firma del documento ", $docId, ":</b><br>" , $e->getMessage(), "<br>";
	}

	// Resultado de la segunda firma
	$docId = "0002";
	try {
		$transactionResult = $fireClient->recoverBatchSign(
			$subjectId,			// DNI de la persona
			$transactionId,		// Identificador de transaccion recuperado en la operacion createBatch()
			$docId				// Identificador del documento
		);
		// Mostramos los datos obtenidos
		echo "<b>Firma del documento ", $docId, ":</b><br>" , base64_encode($transactionResult->result), "<br>";
	}
	catch(Exception $e) {
		echo "<b>Firma del documento ", $docId, ":</b><br>" , $e->getMessage(), "<br>";
	}
 ?>
 </body>
</html>