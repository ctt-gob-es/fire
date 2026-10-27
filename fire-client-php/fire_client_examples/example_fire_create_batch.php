<html>
 <head>
  <title>Generar lote de firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include '../fire_client.php';
	
	// Datos basicos para la operacion
	$appId = "87B35606B6A1";	// Identificador de la aplicacion dada de alta previamente en el sistema
	$subjectId = "00001";		// DNI de la persona
	
	// Calculamos la URL base de la web para poder configurar las URL de retorno
	$urlBase = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on') ? "https://" : "http://";   
    $urlBase.= $_SERVER['HTTP_HOST'];   
    $pos = strripos($_SERVER['REQUEST_URI'], "/");
	$urlBase.= $pos > -1 ? substr($_SERVER['REQUEST_URI'], 0, $pos + 1) : $_SERVER['REQUEST_URI'];   
    
	// Parametros de configuracion de la transaccion
	$extraParams = base64_encode("mode=implicit\nexpPolicy=FirmaAGE");
	$upgradeFormat = "T-Level";		//Formato de firma longeva (T-Level, LTA-Level...)
	$conf = "redirectOkUrl=".$urlBase."example_fire_recover_batch_result.php\n".	// URL a la que llegara si el usuario se autentica correctamente
			"redirectErrorUrl=".$urlBase."example_fire_recover_error.php";		// URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
	$confB64 = base64_encode($conf);

	// Creamos el cliente
	$fireClient = new FireClient($appId);

	// Creamos un lote de firma
	$batch;
	try {
		$batch = $fireClient->createBatchProcess(
			$subjectId,		// DNI de la persona
			"sign",			// Operacion criptografica (sign, cosign o countersign)
			"CAdES",		// Formato de firma (CAdES, XAdES, PAdES...)
			"SHA256withRSA",// Algoritmo de hash que se aplicara en la firma
			$extraParams,	// Configuracion del formato de firma en base 64. El equivalente al extraParams de Autofirma
			$upgradeFormat,	// Formato de actualizacion
			$confB64		// Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}
	
	session_start();
	$_SESSION["appid"]=$appId;
	$_SESSION["sid"]=$subjectId;
	$_SESSION["trid"]=$batch->transactionId;
	
	// Mostramos los datos obtenidos
	echo "<br><b>Id de transaccion:</b><br>", $batch->transactionId, "<br><br>";

	echo "<a href=\"example_fire_add_document_batch.php\">Agregar documentos >></a>";
 ?>
 </body>
</html>