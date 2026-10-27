<html>
 <head>
  <title>Recuperar firma del lote de firmas</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include '../fire_client.php';
	
	$transactionId = $_GET["trid"];	// Identificador de la transaccion
	
	session_start();
	$appId = $_SESSION["appid"];		// Identificador de la aplicacion dada de alta previamente en el sistema
	$subjectId = $_SESSION["sid"];		// DNI de la persona
	
	// Creamos el cliente (tambien podriamos reutilizar uno anterior si lo tuviesemos ya creado)
	$fireClient = new FireClient($appId);
	
	// Recuperamos la primera firma del lote
	$docId = "0001";	// Identificador que se le asigno a la primera firma
	try {
		// Recuperamos la firma del lote
		$transactionResult = $fireClient->recoverBatchSign(
			$subjectId,			// DNI de la persona
			$transactionId,		// Identificador de transaccion
			$docId				// Identificador del documento
		);
		// Mostramos los datos obtenidos
		echo "<b>Firma del documento ", $docId, ":</b><br>", base64_encode($transactionResult->result), "<br>";
	}
	catch(Exception $e) {
		echo "<b>Error al recuperar el documento ", $docId, ":</b><br>Error ", $e->getCode(), ": ", $e->getMessage(), "<br>";   
	}

	// Resultado de la segunda firma
	$docId = "0002";	// Identificador que se le asigno a la segunda firma
	try {
		$transactionResult = $fireClient->recoverBatchSign(
			$subjectId,			// DNI de la persona
			$transactionId,		// Identificador de transaccion
			$docId				// Identificador del documento
		);
		// Mostramos los datos obtenidos
		echo "<b>Firma del documento ", $docId, ":</b><br>" , base64_encode($transactionResult->result), "<br>";
	}
	catch(Exception $e) {
		echo "<b>Error al recuperar el documento ", $docId, ":</b><br>Error ", $e->getCode(), ": ", $e->getMessage(), "<br>";   
	}
	
	echo "<br><br><br><a href=\"example_fire_create_batch.php\">Iniciar nueva transaccion >></a>";
 ?>
 </body>
</html>