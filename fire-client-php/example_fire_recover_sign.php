<html>
 <head>
  <title>Recuperar firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include 'fire_client.php';
	
	
	//$appId = "7BA5453995EC";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - PREPRODUCCION
	$appId = "B244E473466F";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - LOCAL
	$subjectId = "00001";		// DNI de la persona
	$transactionId = "c5ae8969-ca68-4649-b3cc-759a0fd0ecea";	// Identificador de la transaccion


	$fireClient = new FireClient($appId); // Identificador de la aplicacion (dada de alta previamente en el sistema)
	$transactionResult;
	try {
		$transactionResult = $fireClient->recoverSign(
			$subjectId,			// Identificador del usuario
			$transactionId,		// Identificador de transaccion recuperado en la operacion sign()
			null				// Formato de actualizacion de firma
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	// Mostramos los datos obtenidos
	echo "<br><b>Proveedor:</b><br>".$transactionResult->providerName;
	echo "<br><b>Certificado:</b><br>".$transactionResult->signingCert;
	echo "<br><b>Firma:</b><br>".(base64_encode($transactionResult->result));

 ?>
 </body>
</html>