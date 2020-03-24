<html>
 <head>
  <title>Ejemplo de recuperacion de firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include '../fire_api.php';
	
	$appId = "B244E473466F";	// Identificador de aplicacion
	$subjectId = "00001";		// DNI de la persona
	$transactionId = "71a0077b-3869-4374-b8ce-f64244ce9c50";	// Identificador de la transaccion
	
	// Funcion del API de Clave Firma para cargar los datos a firmar
	$transactionResult;
	try {
		$transactionResult = recoverSign(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$subjectId,		// Identificador del usuario
			$transactionId,		// Identificador de transaccion recuperado en la operacion sign()
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