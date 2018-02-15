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
	$appId = "B5DD7690A7FA";	// Entorno local
	
	// Funcion del API de Clave Firma para cargar los datos a firmar
	$transactionResult;
	try {
		$transactionResult = recoverSign(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			"8b62bdd7-1a3a-453b-a7f4-a452eb833ba0",		// Identificador de transaccion recuperado en la operacion sign()
			null
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	// Mostramos los datos obtenidos
	echo "<br><b>Proveedor:</b><br>".$transactionResult->providerName;
	echo "<br><b>Firma:</b><br>".(base64_encode($transactionResult->result));

 ?>
 </body>
</html>