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
	$appId = "B244E473466F";	// Entorno local
	$transactionId = "962db6f1-46a4-47ce-93ad-5007816aa497";
	
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
		echo "<br><b>Proveedor:</b><br>".$batchDocument->providerName;
		echo "<br><b>Identificador de documento:</b><br>".$batchDocument->id;
		echo "<br><b>Estado de la firma:</b><br>".$batchDocument->ok;
		if(!$batchDocument->ok) {
			echo "<br><b>Codigo de error:</b><br>".$batchDocument->dt;
		}
	}
	
 ?>
 </body>
</html>