<html>
 <head>
  <title>Recuperar error</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include '../fire_client.php';

	$appId = $_GET["appid"];		// Identificador de la aplicacion (dada de alta previamente en el sistema)
	$subjectId = $_GET["sid"];		// DNI de la persona
	
	session_start();
	$transactionId = $_SESSION["trid"]; // Identificador de la transaccion
	unset($_SESSION["trid"]);
	
	$fireClient = new FireClient($appId); // Identificador de la aplicacion (dada de alta previamente en el sistema)
	
	// Funcion del API de FIRe para recuperar informacion del error producido en la transaccion
	$error;
	try {
		$error = $fireClient->recoverError(
			$subjectId,			// DNI de la persona
			$transactionId		// Identificador de transaccion recuperado en la operacion sign()
		);
	}
	catch(Exception $e) {
		echo "Error ", $e->getCode(), ": ", $e->getMessage(), "\n";
		return;
	}

	// Mostramos los datos obtenidos
	echo "<br><b>Proveedor:</b><br>".$error->providerName;
	echo "<br><b>Codigo de error:</b><br>".$error->errorCode;
	echo "<br><b>Mensaje de error:</b><br>".$error->errorMessage;

	echo "<br><br><br>";
	echo "<a href=\"example_fire_sign.php\">Nueva firma >></a><br>";
	echo "<a href=\"example_fire_create_batch.php\">Nueva firma de lote >></a>";
 ?>
 </body>
</html>