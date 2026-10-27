<html>
 <head>
  <title>Recuperar estado de procesamiento del lote de firmas</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include '../fire_api.php';
	
	$appId = "B244E473466F";	// Identificador de la aplicacion
	$subjectId = "00001";		// DNI de la persona
	$transactionId = "cebe6a97-be7d-4b4c-a069-321d798076b5";	// Identificador de la transaccion
	
	// Recuperamos el estado de la operacion de firma de lote
	$state;
	try {
		$state = recoverBatchResultState(
			$appId,				// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$subjectId,			// DNI de la persona
			$transactionId		// Identificador de transaccion recuperado en la operacion sign()
		);
	}
	catch(Exception $e) {
		echo "Error ", $e->getCode(), ": ", $e->getMessage(), "\n";
		return;
	}

	// Mostramos los datos obtenidos
	echo "<br><b>Estado:</b> ", $state;
 ?>
 </body>
</html>