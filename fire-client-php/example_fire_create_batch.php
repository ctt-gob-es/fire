<html>
 <head>
  <title>Generar lote de firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include 'fire_client.php';
		

	//$appId = "7BA5453995EC";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - PREPRODUCCION
	$appId = "B244E473466F";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - LOCAL
	$subjectId = "00001";		// DNI de la persona
	$conf = "redirectOkUrl=http://www.google.es"."\n".	// URL a la que llegara si el usuario se autentica correctamente
			"redirectErrorUrl=http://www.ibm.com";		// URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
	$confB64 = base64_encode($conf);

	
	$fireClient = new FireClient($appId); // Identificador de la aplicacion (dada de alta previamente en el sistema)
	$batch;
	try {
		$batch = $fireClient->createBatchProcess(
			$subjectId,		// DNI de la persona
			"sign",			// Operacion criptografica (sign, cosign o countersign)
			"CAdES",		// Formato de firma (CAdES, XAdES, PAdES...)
			"SHA1withRSA",	// Algoritmo de firma (Actualmente solo se permite SHA1withRSA)
			null,			// Configuracion del formato de firma en base 64. El equivalente al extraParams del MiniApplet de @firma
			null,			// Actualizacion
			$confB64		// Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}
	
	// Mostramos los datos obtenidos
	echo "<br><b>Id de transaccion:</b><br>".$batch->transactionId;

 ?>
 </body>
</html>