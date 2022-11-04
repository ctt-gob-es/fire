<html>
 <head>
  <title>Generar lote de firma</title>
 </head>
 <body>
 <?php 
	// Cargamos el componente distribuido de FIRe
	include '../fire_api.php';
		
	// Calculamos la URL base de la web para poder configurar las URL de retorno
	$urlBase = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on') ? "https://" : "http://";   
    $urlBase.= $_SERVER['HTTP_HOST'];   
    $pos = strripos($_SERVER['REQUEST_URI'], "/");
	$urlBase.= $pos > -1 ? substr($_SERVER['REQUEST_URI'], 0, $pos + 1) : $_SERVER['REQUEST_URI'];   
    
	$appId = "B244E473466F";	// Identificador de la aplicacion (dada de alta previamente en el sistema) - LOCAL
	$subjectId = "00001";		// DNI de la persona
	$extraParams = base64_encode("mode=implicit\nexpPolicy=FirmaAGE");
	$upgradeFormat = "T-Level";		//Formato de firma longeva (T-Level, LTA-Level...)
	$conf = "redirectOkUrl=".$urlBase."example_fire_recover_batch_result.php?appid=". $appId. "&sid=". $subjectId."\n".	// URL a la que llegara si el usuario se autentica correctamente
			"redirectErrorUrl=".$urlBase."example_fire_recover_error.php?appid=". $appId. "&sid=". $subjectId;		// URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
	$confB64 = base64_encode($conf);
	
	// Funcion del API de FIRe para crear un lote de firma
	$batch;
	try {
		$batch = createBatchProcess(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$subjectId,		// DNI de la persona
			"sign",			// Operacion criptografica (sign, cosign o countersign)
			"CAdES",		// Formato de firma (CAdES, XAdES, PAdES...)
			"SHA1withRSA",	// Algoritmo de firma (Actualmente solo se permite SHA1withRSA)
			$extraParams,	// Configuracion del formato de firma en base 64. El equivalente al extraParams del MiniApplet de @firma
			$upgradeFormat,	// Formato de actualizacion
			$confB64		// Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
		);
	}
	catch(Exception $e) {
		echo 'Error: ',  $e->getMessage(), "\n";
		return;
	}
	
	session_start();
	$_SESSION["trid"]=$batch->transactionId;
	
	// Mostramos los datos obtenidos
	echo "<br><b>Id de transaccion:</b><br>", $batch->transactionId, "<br><br>";

	echo "<a href=\"example_fire_add_document_batch.php?appid=", $appId, "&sid=", $subjectId, "&trid=", $batch->transactionId, "\">Agregar documentos >></a>";
 ?>
 </body>
</html>