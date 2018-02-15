<html>
 <head>
  <title>Ejemplo de recuperacion de firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de Clave Firma
	include 'fire_api.php';
	
	$dataB64 = base64_encode("Hola Mundo!!");
	
	// Identificador de la aplicacion (dada de alta previamente en el sistema)
	//$appId = "7BA5453995EC";	// Entorno preproduccion
	$appId = "B5DD7690A7FA";	// Entorno local
	
	// Funcion del API de Clave Firma para cargar los datos a firmar
	$error;
	try {
		$error = recoverError(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			"30ee59ef-cf4f-418e-83db-0a9e71fcc0c8"		// Identificador de transaccion recuperado en la operacion sign()
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	// Mostramos los datos obtenidos
	echo "<br><b>Proveedor:</b><br>".$error->providerName;
	echo "<br><b>Codigo de error:</b><br>".$error->errorCode;
	echo "<br><b>Mensaje de error:</b><br>".$error->errorMessage;

 ?>
 </body>
</html>