<html>
 <head>
  <title>Recuperar error</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include '../fire_client.php';
	
	session_start();
	$appId = $_SESSION["appid"];		// Identificador de la aplicacion dada de alta previamente en el sistema
	$subjectId = $_SESSION["sid"];		// DNI de la persona
	$transactionId = $_SESSION["trid"]; // Identificador de la transaccion
	
	// Damos por finalizada la transaccion
	unset($_SESSION["trid"]);
	
	// Creamos el cliente (tambien podriamos reutilizar uno anterior si lo tuviesemos ya creado)
	$fireClient = new FireClient($appId);
	
	// Recuperar informacion del error producido en la transaccion
	$error;
	try {
		$error = $fireClient->recoverError(
			$subjectId,			// DNI de la persona
			$transactionId		// Identificador de transaccion
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