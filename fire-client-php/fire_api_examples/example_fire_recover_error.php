<html>
 <head>
  <title>Ejemplo de recuperacion de error</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include '../fire_api.php';
	
	$dataB64 = base64_encode("Hola Mundo!!");
	
	$appId = "B244E473466F";	// Identificador de aplicacion
	$subjectId = "00001";		// DNI de la persona
	$transactionId = "71a0077b-3869-4374-b8ce-f64244ce9c50";	// Identificador de la transaccion
	
	// Funcion del API de Clave Firma para cargar los datos a firmar
	$error;
	try {
		$error = recoverError(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$subjectId,		// DNI de la persona
			$transactionId	// Identificador de transaccion recuperado en la operacion sign()
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