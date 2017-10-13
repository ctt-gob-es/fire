<html>
 <head>
  <title>Ejemplo de recoverBatchResult</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include 'fire_api.php';
	
	// Identificador de la aplicacion (dada de alta previamente en el sistema)
	//$appId = "7BA5453995EC";	// Entorno preproduccion
	$appId = "A418C37E84BA";	// Entorno local
	$transactionId = "0b977526-4e0d-4475-819c-5d7da230ba17";
	
	// Funcion del API de Clave Firma para cargar los datos a firmar
	$arrayBatchResponse;
	try {
		$arrayBatchResponse = recoverBatchResult(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$transactionId		// Identificador de transaccion recuperado en la operacion sign()
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	// Mostramos los datos obtenidos
	foreach ($arrayBatchResponse as &$batchDocument) {
		echo "<br><b>Identificador de documento:</b><br>".$batchDocument->id;
		echo "<br><b>Estado de la firma:</b><br>".$batchDocument->ok;
		if(!$batchDocument->ok) {
			echo "<br><b>Codigo de error:</b><br>".$batchDocument->dt;
		}
	}
	
 ?>
 </body>
</html>