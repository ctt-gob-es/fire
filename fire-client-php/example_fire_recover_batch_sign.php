<html>
 <head>
  <title>Ejemplo recoverBatchSign</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include 'fire_api.php';
	
	$dataB64 = base64_encode("Hola Mundo!!");
	
	// Identificador de la aplicacion (dada de alta previamente en el sistema)
	//$appId = "7BA5453995EC";	// Entorno preproduccion
	$appId = "A418C37E84BA";	// Entorno local
	$transactionId = "0b977526-4e0d-4475-819c-5d7da230ba17";
	
	// Funcion del API de Clave Firma para cargar los datos a firmar
	$transactionResult;
	try {
		$transactionResult = recoverBatchSign(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$transactionId,		// Identificador de transaccion recuperado en la operacion createBatch()
			"0001"				// Identificador del documento
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	// Mostramos los datos obtenidos
	echo "<br><b>Firma:</b><br>".(base64_encode($transactionResult->result));

 ?>
 </body>
</html>