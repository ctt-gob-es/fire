<html>
 <head>
  <title>Recuperar firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include 'fire_client.php';

	$transactionId = "03c08981-97b8-4ae8-af39-6111e853f410";	// Identificador de la transaccion	
	
	//$appId = "7BA5453995EC";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - PREPRODUCCION
	$appId = "B244E473466F";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - LOCAL
	$subjectId = "00001";		// DNI de la persona
	$upgradeFormat = null;		//Formato de firma longeva (T-LEVEL, LT-LEVEL...)

	$fireClient = new FireClient($appId); // Identificador de la aplicacion (dada de alta previamente en el sistema)
	$transactionResult;
	try {
		$transactionResult = $fireClient->recoverSign(
			$subjectId,			// Identificador del usuario
			$transactionId,		// Identificador de transaccion recuperado en la operacion sign()
			$upgradeFormat		// Formato de actualizacion de firma
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	// Mostramos los datos obtenidos
	echo "<br><b>Proveedor:</b><br>".$transactionResult->providerName;
	echo "<br><b>Certificado:</b><br>".$transactionResult->signingCert;
	if (isset($transactionResult->result)) {
		echo "<br><b>Firma:</b><br>".(base64_encode($transactionResult->result));
	}
	if (isset($transactionResult->gracePeriod)) {
		echo "<br><b>Periodo de gracia:</b><br>ID: ".($transactionResult->gracePeriod->id)."<br>Fecha: ".($transactionResult->gracePeriod->date->format('Y-m-d H:i:sP'));
	}
 ?>
 </body>
</html>