<html>
 <head>
  <title>Ejemplo de recuperacion del certificado recien generado</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include '../clavefirma.php';
	
	// Identificador de la aplicacion (dada de alta previamente en el sistema)
	//$appId = "7BA5453995EC";	// Entorno preproduccion
	$appId = "B244E473466F";	// Entorno local
	
	$transactionId = "0acdbf53-22cf-452e-928f-05f1a6103567";	// Identificador de transaccion
	
	$provider = "clavefirmatest";	// Proveedor de firma en la nube
	
	// Funcion del API de Clave Firma para recuperar el certificado recien generado
	$certB64;
	try {
		$certB64 = recoverCertificateWithProvider(
			$appId,				// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$transactionId,		// Identificador de transaccion recuperado en la operacion loadData()
			$provider
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	// Mostramos los datos obtenidos
	echo "<b>Certificado Base64:</b><br>".$certB64."<br><br><br>";
	
	$cert = parseCertificate($certB64);
		
	echo "<b>".($cert['subject']['CN'])."</b>";
	echo "<br>Emisor: ".($cert['issuer']['CN']);
	$validTo = date_parse_from_format("ymdHisT", $cert['validTo']);
	echo "<br>Fecha caducidad: ".($validTo['day'])."/".($validTo['month'])."/".($validTo['year']);
 ?>
 </body>
</html>