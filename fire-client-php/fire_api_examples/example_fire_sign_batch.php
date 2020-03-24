<html>
 <head>
  <title>Ejemplo de signBatch</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include '../fire_api.php';
	
	// Identificador de la aplicacion (dada de alta previamente en el sistema)
	
	$conf = "redirectOkUrl=http://www.google.es"."\n".	// URL a la que llegara si el usuario se autentica correctamente
			"redirectErrorUrl=http://www.ibm.com";		// URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
	$confB64 = base64_encode($conf);
	
	$appId = "B244E473466F";	// Identificador de aplicacion
	$subjectId = "00001";		// DNI de la persona
	$transactionId = "ba8ef7d0-c3ec-4e21-8f61-6d539da1c4b4";	// Identificador de la transaccion

	// Funcion del API de Clave Firma para listar certificados
	$signatureB64;
	try {
		$signatureB64 = signBatch(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$subjectId,		// DNI de la persona
			$transactionId,	// Identificador de la transaccion
			false			// Operacion criptografica (sign, cosign o countersign)
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
