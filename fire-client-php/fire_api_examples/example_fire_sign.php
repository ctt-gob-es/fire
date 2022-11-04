<html>
 <head>
  <title>Firmar documento</title>
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
	
	// Identificador de la aplicacion (dada de alta previamente en el sistema)
	$appId = "B244E473466F";	// Entorno local
	$subjectId = "00001";		// DNI de la persona
	$dataB64 = base64_encode("Hola Mundo!!");
	$extraParams = base64_encode("mode=implicit\nexpPolicy=FirmaAGE");

	$conf = "redirectOkUrl=".$urlBase."example_fire_recover_sign.php?appid=". $appId. "&sid=". $subjectId."\n".	// URL a la que llegara si el usuario se autentica correctamente
			"redirectErrorUrl=".$urlBase."example_fire_recover_error.php?appid=". $appId. "&sid=". $subjectId;		// URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente		
	$confB64 = base64_encode($conf);
	   
	// Funcion para firmar
	$tr;
	try {
		$tr = sign(
			$appId,			// Identificador de la aplicacion (dada de alta previamente en el sistema)
			$subjectId,		// DNI de la persona
			"sign",			// Operacion criptografica (sign, cosign o countersign)
			"CAdES",		// Formato de firma (CAdES, XAdES, PAdES...)
			"SHA1withRSA",	// Algoritmo de firma (Actualmente solo se permite SHA1withRSA)
			$extraParams,	// Configuracion del formato de firma en base 64. El equivalente al extraParams del MiniApplet de @firma
			$dataB64,		// Datos a firmar
			$confB64		// Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
		);
	}
	catch(Exception $e) {
		echo "Error ", $e->getCode(), ": ", $e->getMessage(), "\n";
		return;
	}
	
	session_start();
	$_SESSION["trid"]=$tr->transactionId;
	
	echo "<b>Id transaccion:</b><br>".$tr->transactionId."<br><br><br>";
	echo "<b>URL de redireccion:</b><br><a href=\"".$tr->redirectUrl."\">".$tr->redirectUrl."</a><br><br><br>";
 ?>
 </body>
</html>
