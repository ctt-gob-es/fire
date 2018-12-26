<html>
 <head>
  <title>Ejemplo de recoverBatchResultState</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include 'fire_api.php';
	
	// Identificador de la aplicacion (dada de alta previamente en el sistema)
	//$appId = "7BA5453995EC";	// Entorno preproduccion
	$appId = "B244E473466F";	// Entorno local
	$transactionId = "0b977526-4e0d-4475-819c-5d7da230ba17";
	
	// Funcion del API de Clave Firma para cargar los datos a firmar
	$signatureB64;
	try {
		$signatureB64 = recoverBatchResultState(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$transactionId		// Identificador de transaccion recuperado en la operacion sign()
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	// Mostramos los datos obtenidos
	echo "<br><b>Firma:</b><br>".$signatureB64;

 ?>
 </body>
</html>