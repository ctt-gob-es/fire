<html>
 <head>
  <title>Firmar documento</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include 'fire_client.php';
	
	// Identificador de la aplicacion (dada de alta previamente en el sistema)
	
	$conf = "redirectOkUrl=http://www.google.es"."\n".	// URL a la que llegara si el usuario se autentica correctamente
			"redirectErrorUrl=http://www.ibm.com"; // URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
			
	$confB64 = base64_encode($conf);
	
	//$appId = "7BA5453995EC";	// Entorno preproduccion
	$appId = "B244E473466F";	// Entorno local
	$subjectId = "00001";		// DNI de la persona
	$dataB64 = base64_encode("Hola Mundo!!");
	$extraParams = base64_encode("mode=implicit\nexpPolicy=FirmaAGE");
	
	$fireClient = new FireClient($appId); // Identificador de la aplicacion (dada de alta previamente en el sistema)
	
	// Funcion del API de Clave Firma para listar certificados
	$signatureB64;
	try {
		$signatureB64 = $fireClient->sign(
			$subjectId,		// DNI de la persona
			"sign",			// Operacion criptografica (sign, cosign o countersign)
			"CAdES",		// Formato de firma (CAdES, XAdES, PAdES...)
			"SHA1withRSA",	// Algoritmo de firma (Actualmente solo se permite SHA1withRSA)
			$extraParams,	// Configuracion del formato de firma en base 64. El equivalente al extraParams del MiniApplet de @firma
			$dataB64,		// Datos a firmar
			$confB64		// Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
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
