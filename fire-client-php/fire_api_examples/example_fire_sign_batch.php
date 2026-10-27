<html>
 <head>
  <title>Firmar lote de firmas</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include '../fire_api.php';
	
	session_start();
	$appId = $_SESSION["appid"];		// Identificador de la aplicacion (dada de alta previamente en el sistema)
	$subjectId = $_SESSION["sid"];		// DNI de la persona
	$transactionId = $_SESSION["trid"];	// Identificador de la transaccion

	// Iniciamos la firma del lote
	$transaction;
	try {
		$transaction = signBatch(
			$appId,				// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$subjectId,			// DNI de la persona
			$transactionId,		// Identificador de la transaccion (obtenido de la operacion createBatch)
			false				// Detener en caso de error 
		);
	}
	catch(Exception $e) {
		echo "Error ", $e->getCode(), ": ", $e->getMessage(), "\n";
		return;
	}
	
	echo "<b>Id transaccion:</b><br>".$transaction->transactionId."<br><br><br>";
	echo "<b>URL de redireccion:</b><br><a href=\"".$transaction->redirectUrl."\">".$transaction->redirectUrl."</a><br><br><br>";
 ?>
 </body>
</html>
