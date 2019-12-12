<html>
 <head>
  <title>Recuperar firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include 'fire_client.php';

	$docId = "1573733664278249577";	// Identificador del documento asincrono que queremos recuperar
	
	//$appId = "7BA5453995EC";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - PREPRODUCCION
	$appId = "B244E473466F";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - LOCAL
	$upgradeFormat = "ES-T";	//Formato de firma longeva (T-LEVEL, LT-LEVEL...)

	$fireClient = new FireClient($appId); // Identificador de la aplicacion (dada de alta previamente en el sistema)
	$transactionResult;
	try {
		$transactionResult = $fireClient->recoverAsyncSign(
			$docId,				// Identificador del documento que se obtuvo al recibir el periodo de gracia
			$upgradeFormat,		// Formato de actualizacion de firma
			null,
			true
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	// Mostramos el estado de la transaccion
	echo "<br><b>Estado:</b> ", $transactionResult->state;
	
	// Mostramos el formato al que se ha actualizado la firma
	echo "<br><b>Formato actualizado:</b> ", $transactionResult->upgradeFormat;

	// Mostramos los datos obtenidos
	if (isset($transactionResult->errorMessage)) {
		echo "<br><b>Error ", $transactionResult->errorCode, ":</b> ", $transactionResult->errorMessage;
	}
	else if (isset($transactionResult->result)) {
		echo "<br><b>Firma:</b><br>", base64_encode($transactionResult->result);
	}
	else if (isset($transactionResult->gracePeriod)) {
		echo "<br><b>Periodo de gracia:</b><br>ID: ", $transactionResult->gracePeriod->id, "<br>Fecha: ", $transactionResult->gracePeriod->date->format('Y-m-d H:i:sP');
	}
 ?>
 </body>
</html>