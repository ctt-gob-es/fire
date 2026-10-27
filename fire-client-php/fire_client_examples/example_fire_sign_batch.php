<html>
 <head>
  <title>Firmar lote de firmas</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include '../fire_client.php';

	session_start();
	$appId = $_SESSION["appid"];		// Identificador de la aplicacion (dada de alta previamente en el sistema)
	$subjectId = $_SESSION["sid"];		// DNI de la persona
	$transactionId = $_SESSION["trid"];	// Identificador de la transaccion
	
	// Creamos el cliente (tambien podriamos reutilizar uno anterior si lo tuviesemos ya creado)
	$fireClient = new FireClient($appId);

	// Iniciamos la firma del lote
	$signatureB64;
	try {
		$signatureB64 = $fireClient->signBatch(
			$subjectId,			// DNI de la persona
			$transactionId,		// Identificador de la transaccion (obtenido de la operacion createBatch)
			false				// Detener en caso de error 
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
