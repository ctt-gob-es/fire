<html>
 <head>
  <title>Firmar lote de firmas</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include 'fire_client.php';

	
	//$appId = "7BA5453995EC";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - PREPRODUCCION
	$appId = "B244E473466F";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - LOCAL
	$subjectId = "00001";		// DNI de la persona
	$transactionId = "1bf458ac-6940-4be9-bd26-86d6ba82f898";	// Identificador de la transaccion
	$conf = "redirectOkUrl=http://www.google.es"."\n".	// URL a la que llegara si el usuario se autentica correctamente
			"redirectErrorUrl=http://www.ibm.com";		// URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
	$confB64 = base64_encode($conf);

	
	$fireClient = new FireClient($appId); // Identificador de la aplicacion (dada de alta previamente en el sistema)
	$signatureB64;
	try {
		$signatureB64 = $fireClient->signBatch(
			$subjectId,			// DNI de la persona
			$transactionId,		// Identificador de transaccion recuperado en la operacion createBatch()
			false				// Detener error 
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}
	
	echo "<b>Id transaccion:</b><br>".$signatureB64->transactionId."<br><br><br>";
	echo "<b>URL de redireccion:</b><br>".$signatureB64->redirectUrl."<br><br><br>";

 ?>
 </body>
</html>
