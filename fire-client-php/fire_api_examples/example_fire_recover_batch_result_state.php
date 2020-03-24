<html>
 <head>
  <title>Ejemplo de recoverBatchResultState</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include '../fire_api.php';
	
	$appId = "B244E473466F";	// Identificador de aplicacion
	$subjectId = "00001";		// DNI de la persona
	$transactionId = "ba8ef7d0-c3ec-4e21-8f61-6d539da1c4b4";	// Identificador de la transaccion
	
	// Funcion del API de Clave Firma para cargar los datos a firmar
	$signatureB64;
	try {
		$signatureB64 = recoverBatchResultState(
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
	echo "<br><b>Firma:</b><br>".$signatureB64;

 ?>
 </body>
</html>