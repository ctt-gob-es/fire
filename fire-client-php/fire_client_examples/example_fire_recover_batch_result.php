<html>
 <head>
  <title>Recuperar resultados de la firma del lote de firmas</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include '../fire_client.php';

	session_start();
	$appId = $_SESSION["appid"];		// Identificador de la aplicacion dada de alta previamente en el sistema
	$subjectId = $_SESSION["sid"];		// DNI de la persona
	$transactionId = $_SESSION["trid"]; // Identificador de la transaccion
	
	// Damos por finalizada la transaccion (aunque aun utilizaremos este valor para recuperar los resultados)
	unset($_SESSION["trid"]);	
	
	// Creamos el cliente (tambien podriamos reutilizar uno anterior si lo tuviesemos ya creado)
	$fireClient = new FireClient($appId);

	// Recuperamos el resultado de un lote de firma
	$batchResult;
	try {
		$batchResult = $fireClient->recoverBatchResult(
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

	echo "<br><br><br><a href=\"example_fire_recover_batch_sign.php?trid=", $transactionId, "\">Recuperar firmas >></a>";
 ?>
 </body>
</html>