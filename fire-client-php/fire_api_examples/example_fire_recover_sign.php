<html>
 <head>
  <title>Recuperar firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include '../fire_api.php';
	
	$appId = $_GET["appid"];		// Identificador de la aplicacion (dada de alta previamente en el sistema)
	$subjectId = $_GET["sid"];		// DNI de la persona
																							
	session_start();
	$transactionId = $_SESSION["trid"]; // Identificador de la transaccion
	unset($_SESSION["trid"]);
	
	$upgradeFormat = null;		// Formato de firma longeva o identificador para la validacion de la firma (VERIFY, T-LEVEL, LT-LEVEL...)
	$upgradeConfig = "updater.ignoreGracePeriod=true\nupdater.allowPartialUpgrade=true";		// Configuracion para la plataforma validadora
	$upgradeConfigB64 = base64_encode($upgradeConfig);
	
	$transactionResult;
	try {
		$transactionResult = recoverSign(
			$appId,				// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$subjectId,			// Identificador del usuario
			$transactionId,		// Identificador de transaccion recuperado en la operacion sign()
			$upgradeFormat,		// Formato de actualizacion de firma
			$upgradeConfigB64	// Configuracion para la plataforma de actualizacion										
		);
	}
	catch(Exception $e) {
		echo "Error", $e->getCode(), ": ",  $e->getMessage(), "\n";
		return;
	}

	// Mostramos los datos obtenidos
	echo "<br><b>Estado:</b><br>", $transactionResult->state;
	echo "<br><b>Proveedor:</b><br>", $transactionResult->providerName;
	echo "<br><b>Certificado:</b><br>", $transactionResult->signingCert;
	echo "<br><b>Formato de actualizacion:</b><br>", $transactionResult->upgradeFormat;
	if (isset($transactionResult->result)) {
		echo "<br><b>Firma:</b><br>", base64_encode($transactionResult->result);
	}
	if (isset($transactionResult->gracePeriod)) {
		echo "<br><b>Periodo de gracia:</b><br>ID: ", $transactionResult->gracePeriod->id, "<br>Fecha: ", $transactionResult->gracePeriod->date->format('Y-m-d H:i:sP');
	}

	echo "<br><br><br><a href=\"example_fire_sign.php\">Volver >></a>";
 ?>
 </body>
</html>