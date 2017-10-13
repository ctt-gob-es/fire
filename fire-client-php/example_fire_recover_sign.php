<html>
 <head>
  <title>Ejemplo de recuperacion de firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include 'fire_api.php';
	
	// Identificador de la aplicacion (dada de alta previamente en el sistema)
	//$appId = "7BA5453995EC";	// Entorno preproduccion
	$appId = "A418C37E84BA";	// Entorno local
	
	// Funcion del API de Clave Firma para cargar los datos a firmar
	$transactionResult;
	try {
		$transactionResult = recoverSign(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			"b204e98d-0c36-4cb4-9b1b-612d3e3237cc",		// Identificador de transaccion recuperado en la operacion sign()
			null
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