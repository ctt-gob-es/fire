<html>
 <head>
  <title>Firmar lote de firmas</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include '../fire_api.php';
	
	$appId = $_GET["appid"];		// Identificador de la aplicacion (dada de alta previamente en el sistema)
	$subjectId = $_GET["sid"];		// DNI de la persona
	$transactionId = $_GET["trid"];	// Identificador de la transaccion

	// Funcion del API de FIRe para iniciar la firma de un lote
	$signatureB64;
	try {
		$signatureB64 = signBatch(
			$appId,				// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$subjectId,			// DNI de la persona
			$transactionId,		// Identificador de la transaccion (recuperado de la operacion createBatch)
			false				//Detener en caso de error
		);
	}
	catch(Exception $e) {
		echo "Error ", $e->getCode(), ": ", $e->getMessage(), "\n";
		return;
	}
	
	echo "<b>Id transaccion:</b><br>".$signatureB64->transactionId."<br><br><br>";
	echo "<b>URL de redireccion:</b><br><a href=\"".$signatureB64->redirectUrl."\">".$signatureB64->redirectUrl."</a><br><br><br>";
 ?>
 </body>
</html>
