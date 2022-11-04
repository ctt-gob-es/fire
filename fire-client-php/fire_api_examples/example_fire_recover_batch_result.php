<html>
 <head>
  <title>Recuperar resultados de la firma del lote de firmas</title>
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
	// Funcion del API de FIRe para recuperar el resultado de un lote de firma
	$batchResult;
	try {
		$batchResult = recoverBatchResult(
			$appId,				// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$subjectId,			// DNI de la persona
			$transactionId		// Identificador de transaccion recuperado en la operacion createBatch()
		);
	}
	catch(Exception $e) {
		echo "Error ", $e->getCode(), ": ", $e->getMessage(), "\n";
		return;
	}

	echo "<br><b>Proveedor:</b><br>".$batchResult->providerName;
	echo "<br><b>Certificado:</b><br>".$batchResult->signingCert;

	// Mostramos los datos obtenidos
	foreach ($batchResult->batch as &$batchDocument) {
		echo "<br><b>Identificador de documento:</b><br>".$batchDocument->id;
		echo "<br><b>Estado de la firma:</b><br>".$batchDocument->ok;
		if (!$batchDocument->ok) {
			echo "<br><b>Codigo de error:</b><br>".$batchDocument->dt;
		}
		if (isset($batchDocument->gracePeriod)) {
			echo "<br><b>Periodo de gracia:</b><br>ID: ".($batchDocument->gracePeriod->id)."<br>Fecha: ".($batchDocument->gracePeriod->date->format('Y-m-d H:i:sP'));
		}
	}

	echo "<br><br><br><a href=\"example_fire_recover_batch_sign.php?appid=", $appId, "&sid=", $subjectId, "&trid=", $transactionId, "\">Recuperar firmas >></a>";
 ?>
 </body>
</html>