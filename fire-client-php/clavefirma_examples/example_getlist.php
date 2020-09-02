<html>
 <head>
  <title>Ejemplo de listado de certificados</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include '../clavefirma.php';
	
	// Identificador de la aplicacion (dada de alta previamente en el sistema)
	//$appId = "7BA5453995EC";	// Entorno preproduccion
	$appId = "B244E473466F";	// Entorno local
	
	$subjectId = "00001";			// DNI de la persona
	
	$provider = "clavefirmatest";	// Proveedor de firma en la nube
	
	// Funcion del API de Clave Firma para listar certificados
	$certs;
	try {
		$certs = getListWithProvider($appId, $subjectId, $provider);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}
	
	if (count($certs) == 0) {
		echo "El usuario no tiene certificados de firma";
		return;
	}
	
	// Recorremos el listado de certificados
	foreach($certs as $certB64){
		
		echo "<b>Certificado Base64:</b><br>".$certB64."<br><br><br>";
		
		$cert = parseCertificate($certB64);
				
		echo "<b>".($cert['subject']['CN'])."</b>";
		echo "<br>Emisor: ".($cert['issuer']['CN']);
		$validTo = date_parse_from_format("ymdHisT", $cert['validTo']);
		echo "<br>Fecha caducidad: ".($validTo['day'])."/".($validTo['month'])."/".($validTo['year']);
	}
 ?>
 </body>
</html>