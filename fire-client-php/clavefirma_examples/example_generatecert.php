<html>
 <head>
  <title>Ejemplo de generacion de un nuevo certificado de firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include '../clavefirma.php';
	
	// Identificador de la aplicacion (dada de alta previamente en el sistema)
	//$appId = "7BA5453995EC";	// Entorno preproduccion
	$appId = "B244E473466F";	// Entorno local
	
	$conf = "redirectOkUrl=http://www.google.es"."\n".	// URL a la que llegara si el usuario se autentica correctamente
			"redirectErrorUrl=http://www.ibm.com";		// URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
	$confB64 = base64_encode($conf);
	
	$provider = "clavefirmatest";	// Proveedor de firma en la nube
	
	// Funcion del API de Clave Firma para generar un nuevo certificado
	$generateResult;
	try {
		$generateResult = generateCertificateWithProvider(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			"00002",		// DNI de la persona
			$confB64,		// Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
			$provider
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	// Muestra informacion sobre el el resultado obtenido
	//var_dump ($generateResult);
	
	// Mostramos los datos obtenidos
	echo "<br><b>Id de transaccion:</b> ".$generateResult->transactionId;
	echo "<br><b>URL a la que redirigir al usuario:</b><br>".$generateResult->redirectUrl;

 ?>
 </body>
</html>