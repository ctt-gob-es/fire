<html>
 <head>
  <title>Ejemplo de recuperacion de firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include 'fire_client.php';


	//$appId = "7BA5453995EC";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - PREPRODUCCION
	$appId = "B244E473466F";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - LOCAL
	$subjectId = "00001";		// DNI de la persona
	$transactionId = "9de3fa16-726a-40a9-aba9-97656c5588ea";	// Identificador de la transaccion
	

	$fireClient = new FireClient($appId); // Identificador de la aplicacion (dada de alta previamente en el sistema)
	$error;
	try {
		$error = $fireClient->recoverError(
			$subjectId,			// DNI de la persona
			$transactionId		// Identificador de transaccion recuperado en la operacion sign()
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	// Mostramos los datos obtenidos
	echo "<br><b>Proveedor:</b><br>".$error->providerName;
	echo "<br><b>Codigo de error:</b><br>".$error->errorCode;
	echo "<br><b>Mensaje de error:</b><br>".$error->errorMessage;

 ?>
 </body>
</html>