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
	$appId = "A418C37E84BA";	// Entorno local
	
	// Funcion del API de Clave Firma para cargar los datos a firmar
	$error;
	try {
		$error = recoverError(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			"3a591496-d8e3-415f-9742-928f811fd6d8"		// Identificador de transaccion recuperado en la operacion sign()
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}

	// Mostramos los datos obtenidos
	echo "<br><b>Codigo de error:</b><br>".$error->errorCode;
	echo "<br><b>Mensaje de error:</b><br>".$error->errorMessage;

 ?>
 </body>
</html>