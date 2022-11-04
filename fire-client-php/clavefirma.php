<?php
	// Definimos las url con las que invocar cada servicio
	define ("CERTIFICATESURL","https://127.0.0.1:8443/fire-signature/getCertificates");
	define ("SIGNURL", "https://127.0.0.1:8443/fire-signature/sign");
	define ("LOADURL", "https://127.0.0.1:8443/fire-signature/loadData");
	define ("GENERATECERTURL", "https://127.0.0.1:8443/fire-signature/generateCertificate");
	define ("RECOVERCERTURL", "https://127.0.0.1:8443/fire-signature/recoverCertificate");

	// Definimos los parametros de conexion SSL (https://curl.haxx.se/libcurl/c/easy_setopt_options.html)
	$client_ssl_curl_options = array(
		CURLOPT_SSLCERT => "C:/Users/carlos.gamuci/Documents/FIRe/Ficheros_Despliegue/client_ssl_new_public_cert.pem",
		CURLOPT_SSLCERTTYPE => "PEM",
		CURLOPT_SSLKEY => "C:/Users/carlos.gamuci/Documents/FIRe/Ficheros_Despliegue/client_ssl_new_private_key.pem",
		CURLOPT_SSLKEYTYPE => "PEM",
		CURLOPT_SSLKEYPASSWD => "12341234",
		CURLOPT_SSL_VERIFYPEER => 0
	);
	
	/**
	* Obtiene el listado de certificados de firma de un usuario. Los certificados se obtienen del
	* proveedor de firma en la nube por defecto.
	* @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	* @param $subjectId Identificador del usuario (numero de DNI).
	* @return Listado de certificados disponibles o un listado vacio si no hay ninguno pero se pueden generar.
    * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	* @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	* @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	* @throws HttpNoUserException Cuando el usuario indicado no existe.
	* @throws HttpCertificateBlockedException Cuando existen certificados de firma vigentes pero estan bloqueados.
	* @throws HttpWeakRegistryException Cuando el usuario realizo un registro debil y no tiene ni puede tener certificados.
	* @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	*/
	function getList($appId, $subjectId){
		return getListWithProvider($appId, $subjectId, null);
	}
	
	/**
	* Obtiene el listado de certificados de firma de un usuario. Los certificados se obtienen del
	* proveedor de firma en la nube indicado o, si no se indica, del proveedor por defecto del
	* componente central.
	* @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	* @param $subjectId Identificador del usuario (numero de DNI).
	* @param $provider Nombre del proveedor de firma en la nube del componente central.
	* @return Listado de certificados disponibles o un listado vacio si no hay ninguno pero se pueden generar.
    * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	* @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	* @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	* @throws HttpNoUserException Cuando el usuario indicado no existe.
	* @throws HttpCertificateBlockedException Cuando existen certificados de firma vigentes pero estan bloqueados.
	* @throws HttpWeakRegistryException Cuando el usuario realizo un registro debil y no tiene ni puede tener certificados.
	* @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	*/
	function getListWithProvider($appId, $subjectId, $provider){

		// Comprobamos las variables de entrada
		if (empty($subjectId)) {
            throw new InvalidArgumentException("El identificador de usuario no puede ser nulo");
		}
	
		// Componemos la URL de llamada al servicio remoto
		$URL = CERTIFICATESURL;
		$URL_PARAMS = array(
			"subjectId" => $subjectId,
			"appId" => $appId,
		);
		
		// Configuramos el proveedor de firma en la nube si se ha indicado
		if (!empty($provider)) {
			$URL_PARAMS["certorigin"] = $provider;
		}
		
		// Llamamos al servicio remoto
		$response =  connect($URL, $URL_PARAMS);
		
		// Parseamos el JSON de respuesta
		$json = json_decode($response, true);
				
		// Extraemos los certificados
		$result = array();
		if (!empty($json)) {
			foreach($json["certificates"] as $certB64){
				$result[] = $certB64;
			}
		}
		return $result;
	};
	
	/**
	 * Realiza la carga de los datos y su prefirma. La firma se realizara con el proveedor por defecto del componente central.
	 * Devuelve un objeto de tipo ClaveFirmaLoadResult con el identificador de la transaccion, la URL a la que
	 * redirigir al usuario para que inserte la contrasena de su clave de firma y el resultado parcial de la
	 * firma trifasica.
	 * @return Objeto ClaveFirmaLoadResult con el identificador de transaccion, la URL de redireccion y los datos trifasicos pregenerados.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $subjectId Identificador del usuario (numero de DNI).
	 * @param $op Operacion a realizar ('sign', 'cosign' o 'countersign').
	 * @param $ft Formato de firma ('CAdES', 'XAdES', 'PAdES'...)
	 * @param $algth Algoritmo de firma (Debe estar soportado por el servicio de custodia de claves).
	 * @param $propB64 Configuracion de la operacion de firma. Equivalente al extraParams del MiniApplet @firma.
	 * @param $certB64 Certificado en base 64 a utilizar, tal como los devuelve el metodo getList().
	 * @param $dataB64 Datos en base 64 que se desean firmar.
	 * @param $confB64 Configuracion del servicio. Se debe indicar al menos las URL de redireccion en caso
	 * de exito y error ("redirectOkUrl" y "redirectErrorUrl", respectivamente).
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws HttpNoUserException Cuando el usuario indicado no existe.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function loadData($appId, $subjectId, $op, $ft, $algth, $propB64, $certB64, $dataB64, $confB64){
		return loadDataWithProvider($appId, $subjectId, $op, $ft, $algth, $propB64, $certB64, $dataB64, $confB64, null);
	}
	
	/**
	 * Realiza la carga de los datos y su prefirma. La firma se realizara con el proveedor de firma en la nube
	 * indicado o, si no se indica, del proveedor por defecto del componente central.
	 * Devuelve un objeto de tipo ClaveFirmaLoadResult con el identificador de la transaccion, la URL a la que
	 * redirigir al usuario para que inserte la contrasena de su clave de firma y el resultado parcial de la
	 * firma trifasica.
	 * @return Objeto ClaveFirmaLoadResult con el identificador de transaccion, la URL de redireccion y los datos trifasicos pregenerados.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $subjectId Identificador del usuario (numero de DNI).
	 * @param $op Operacion a realizar ('sign', 'cosign' o 'countersign').
	 * @param $ft Formato de firma ('CAdES', 'XAdES', 'PAdES'...)
	 * @param $algth Algoritmo de firma (Debe estar soportado por el servicio de custodia de claves).
	 * @param $propB64 Configuracion de la operacion de firma. Equivalente al extraParams del MiniApplet @firma.
	 * @param $certB64 Certificado en base 64 a utilizar, tal como los devuelve el metodo getList().
	 * @param $dataB64 Datos en base 64 que se desean firmar.
	 * @param $confB64 Configuracion del servicio. Se debe indicar al menos las URL de redireccion en caso
	 * de exito y error ("redirectOkUrl" y "redirectErrorUrl", respectivamente).
	 * @param $provider Nombre del proveedor de firma en la nube del componente central.
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws HttpNoUserException Cuando el usuario indicado no existe.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function loadDataWithProvider($appId, $subjectId, $op, $ft, $algth, $propB64, $certB64, $dataB64, $confB64, $provider){

		// Comprobamos las variables de entrada
		if (empty($subjectId)) {
            throw new InvalidArgumentException("El identificador de usuario no puede ser nulo");
		}
		if (empty($op)) {
            throw new InvalidArgumentException("El tipo de operacion de firma a realizar no puede ser nulo");
		}
		if (empty($ft)) {
            throw new InvalidArgumentException("El formato de firma no puede ser nulo");
		}
		if (empty($algth)) {
            throw new InvalidArgumentException("El algoritmo de firma no puede ser nulo");
		}
		if (empty($certB64)) {
            throw new InvalidArgumentException("El certificado del firmante no puede ser nulo");
		}
		if (empty($dataB64)) {
            throw new InvalidArgumentException("Los datos a firmar no pueden ser nulos");
		}
		
		// Recodificamos los parametros que lo necesiten para asegurar la correcta transmision por URL
		$b64SpC = array("+", "/"); 
		$b64UrlSafeSpC = array("-", "_");
		
		$propB64us = ($propB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $propB64) : "";
		$dataB64us = ($dataB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $dataB64) : "";
		$confB64us = ($confB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $confB64) : "";
		$certB64us = ($certB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $certB64) : "";
		
		// Componemos la URL de llamada al servicio remoto
		$URL_LOAD_SERVICE = LOADURL;
		$URL_LOAD_SERVICE_PARAMS = array(
			"config" => $confB64us,
			"algorithm" => $algth,
			"cert" => $certB64us,
			"properties" => $propB64us,
			"operation" => $op,
			"format" => $ft,
			"dat" => $dataB64us,
			"subjectId" => $subjectId,
			"appId" => $appId,
		);

		// Configuramos el proveedor de firma en la nube si se ha indicado
		if (!empty($provider)) {
			$URL_LOAD_SERVICE_PARAMS["certorigin"] = $provider;
		}

		// Llamamos al servicio remoto
		$response = connect($URL_LOAD_SERVICE, $URL_LOAD_SERVICE_PARAMS);

		// Procesamos la respuesta
		return new ClaveFirmaLoadResult($response);
	}

	/**
	 * Compone la firma electronica y la devuelve en base64. La firma se realizara con el proveedor de firma
	 * por defecto del componente central.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a loadData()).
	 * @param $op Operacion a realizar ('sign', 'cosign' o 'countersign').
	 * @param $ft Formato de firma ('CAdES', 'XAdES', 'PAdES'...)
	 * @param $algth Algoritmo de firma (Debe estar soportado por el servicio de custodia de claves).
	 * @param $propB64 Configuracion de la operacion de firma. Equivalente al extraParams del MiniApplet @firma.
	 * @param $certB64 Certificado en base 64 a utilizar, tal como los devuelve el metodo getList().
	 * @param $dataB64 Datos en base 64 que se desean firmar.
	 * @param $tdB64 Resultado parcial de la operacion de firma trifasica (devuelto en la llamada a loadData()).
	 * @param $upgrade Formato al que actualizar la firma (ES-T, ES-LTV...).
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function sign($appId, $transactionId, $op, $ft, $algth, $propB64, $certB64, $dataB64, $tdB64, $upgrade) {
		return signWithProvider($appId, $transactionId, $op, $ft, $algth, $propB64, $certB64, $dataB64, $tdB64, $upgrade, null);
	}

	/**
	 * Compone la firma electronica y la devuelve en base64. La firma se realizara con el proveedor de firma
	 * en la nube indicado o, si no se indica, del proveedor por defecto del componente central.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a loadData()).
	 * @param $op Operacion a realizar ('sign', 'cosign' o 'countersign').
	 * @param $ft Formato de firma ('CAdES', 'XAdES', 'PAdES'...)
	 * @param $algth Algoritmo de firma (Debe estar soportado por el servicio de custodia de claves).
	 * @param $propB64 Configuracion de la operacion de firma. Equivalente al extraParams del MiniApplet @firma.
	 * @param $certB64 Certificado en base 64 a utilizar, tal como los devuelve el metodo getList().
	 * @param $dataB64 Datos en base 64 que se desean firmar.
	 * @param $tdB64 Resultado parcial de la operacion de firma trifasica (devuelto en la llamada a loadData()).
	 * @param $upgrade Formato al que actualizar la firma (ES-T, ES-LTV...).
	 * @param $provider Nombre del proveedor de firma en la nube del componente central.
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function signWithProvider($appId, $transactionId, $op, $ft, $algth, $propB64, $certB64, $dataB64, $tdB64, $upgrade, $provider){
		
		// Comprobamos las variables de entrada
		if (empty($transactionId)) {
		    throw new InvalidArgumentException("El id de la transaccion no puede ser nulo");
		}
		if (empty($op)) {
            throw new InvalidArgumentException("El tipo de operacion de firma a realizar no puede ser nulo");
		}
		if (empty($ft)) {
            throw new InvalidArgumentException("El formato de firma no puede ser nulo");
		}
		if (empty($algth)) {
            throw new InvalidArgumentException("El algoritmo de firma no puede ser nulo");
		}
		if (empty($certB64)) {
            throw new InvalidArgumentException("El certificado del firmante no puede ser nulo");
		}
		if (empty($dataB64)) {
            throw new InvalidArgumentException("Los datos a firmar no pueden ser nulos");
		}
		if (empty($tdB64)) {
            throw new InvalidArgumentException("Los datos de la operacion trifasica no pueden ser nulos");
		}
		
		// Recodificamos los parametros que lo necesiten para asegurar la correcta transmision por URL
		$b64SpC = array("+", "/");
		$b64UrlSafeSpC = array("-", "_");
		
		$propB64us = ($propB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $propB64) : "";
		$dataB64us = ($dataB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $dataB64) : "";
		$tdB64us = ($tdB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $tdB64) : "";
		$certB64us = ($certB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $certB64) : "";
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SIGN_PROCESS = SIGNURL;
		$URL_SIGN_PROCESS_PARAMS = array(
			"transactionid" => $transactionId,
			"appId" => $appId,
			"operation" => $op,
			"algorithm" => $algth,
			"format" => $ft,
			"cert" => $certB64us,
			"data" => $dataB64us,
			"tri" => $tdB64us,
		);

		// Configuramos el proveedor de firma en la nube si se ha indicado
		if (!empty($provider)) {
			$URL_SIGN_PROCESS_PARAMS["certorigin"] = $provider;
		}

		if (!empty($upgrade)) {
			$URL_SIGN_PROCESS_PARAMS["upgrade"] = $upgrade;
		}
		if (!empty($propB64us)) {
			$URL_SIGN_PROCESS_PARAMS["properties"] = $propB64us;
		}

		// Llamamos al servicio remoto
		$response = connect($URL_SIGN_PROCESS, $URL_SIGN_PROCESS_PARAMS);
		
		// Procesamos la respuesta
		return base64_encode($response);
	}
	
	/**
	 * Genera un nuevo certificado para el usuario usando el proveedor de firma por defecto del componente central.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $subjectId Identificador del usuario (numero de DNI).
	 * @param $confB64 Configuracion de la llamada en base64 (incluye las URLs: redirectOkUrl y redirectErrorUrl).
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws HttpNoUserException Cuando el usuario indicado no existe.
	 * @throws HttpCertificateAvailableException Cuando se intenta generar un certificado para un usuario que ya tiene.
	 * @throws HttpWeakRegistryException Cuando el usuario realizo un registro debil y no tiene ni puede tener certificados.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function generateCertificate($appId, $subjectId, $confB64) {
		return generateCertificateWithProvider($appId, $subjectId, $confB64, null);
	}
	
	/**
	 * Genera un nuevo certificado para el usuario usando el proveedor de firma indicado o, si no se indica,
	 * el por defecto del componente central.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $subjectId Identificador del usuario (numero de DNI).
	 * @param $confB64 Configuracion de la llamada en base64 (incluye las URLs: redirectOkUrl y redirectErrorUrl).
	 * @param $provider Nombre del proveedor de firma en la nube del componente central.
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws HttpNoUserException Cuando el usuario indicado no existe.
	 * @throws HttpCertificateAvailableException Cuando se intenta generar un certificado para un usuario que ya tiene.
	 * @throws HttpWeakRegistryException Cuando el usuario realizo un registro debil y no tiene ni puede tener certificados.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function generateCertificateWithProvider($appId, $subjectId, $confB64, $provider) {

		// Comprobamos las variables de entrada
		if (empty($subjectId)) {
            throw new InvalidArgumentException("El identificador de usuario no puede ser nulo");
		}
	
		// Cambiamos la codificacion de los Base64 a URL SAFE
		$confB64us = ($confB64 != null) ? str_replace(array("+", "/"), array("-", "_"), $confB64) : "";
		
		// Construimos la URL de la peticion
		$URL_GENERATECERT_SERVICE = GENERATECERTURL;
		$URL_GENERATECERT_SERVICE_PARAMS = array(
			"config" => $confB64us,
			"subjectId" => $subjectId,
			"appId" => $appId,
		);

		// Configuramos el proveedor de firma en la nube si se ha indicado
		if (!empty($provider)) {
			$URL_GENERATECERT_SERVICE_PARAMS["certorigin"] = $provider;
		}

		// Realizamos la llamada al servicio
		$response = connect($URL_GENERATECERT_SERVICE, $URL_GENERATECERT_SERVICE_PARAMS);

		// Procesamos la respuesta
		return new GenerateCertificateResult($response);
	}

	/**
	 * Recupera en base64 el certificado de firma recien generado usando el proveedor de firma por defecto del
	 * componente central.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a generateCertificate()).
	 * @param $provider Nombre del proveedor de firma en la nube del componente central.
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function recoverCertificate($appId, $transactionId){
		return recoverCertificateWithProvider($appId, $transactionId, null);
	}

	/**
	 * Recupera en base64 el certificado de firma recien generado usando el proveedor de firma indicado o, si no
	 * se indico, el por defecto del componente central.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a generateCertificate()).
	 * @param $provider Nombre del proveedor de firma en la nube del componente central.
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function recoverCertificateWithProvider($appId, $transactionId, $provider){
		
		// Comprobamos las variables de entrada
		if (empty($transactionId)) {
		    throw new InvalidArgumentException("El id de la transaccion no puede ser nulo");
		}

		// Construimos la URL de la peticion
		$URL_RECOVERCERT_PROCESS = RECOVERCERTURL;
		$URL_RECOVERCERT_PROCESS_PARAMS = array(
			"transactionId" => $transactionId,
			"appId" => $appId,
		);

		// Configuramos el proveedor de firma en la nube si se ha indicado
		if (!empty($provider)) {
			$URL_RECOVERCERT_PROCESS_PARAMS["certorigin"] = $provider;
		}

		// Realizamos la llamada al servicio
		$response = connect($URL_RECOVERCERT_PROCESS, $URL_RECOVERCERT_PROCESS_PARAMS);
		
		// Procesamos la respuesta
		return base64_encode($response);
	}

	/**
	 * Funcion que realiza la conexion a la URL pasada en parametro, enviando los parametros de conexion por POST.
	 * @param string $URL La url a la que intentar conectarse.
	 * @param array $urlParams Los parametros necesarios para el intento de conexion.
	 * @return string $response Recupera los datos devueltos por la conexion.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws HttpNoUserException Cuando el usuario indicado no existe.
	 * @throws HttpCertificateBlockedException Cuando existen certificados de firma vigentes pero estan bloqueados.
	 * @throws HttpCertificateAvailableException Cuando se intenta generar un certificado para un usuario que ya tiene.
	 * @throws HttpWeakRegistryException Cuando el usuario realizo un registro debil y no tiene ni puede tener certificados.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function connect($URL, $urlParams)
	{
		$ch = curl_init();

		$param_string = NULL;
		foreach ($urlParams as $keyp => $valuep) {
			$param_string .= $param_string == NULL ? $keyp . "=" . $valuep : "&" . $keyp . "=" . $valuep;
		}

		$curl_options = array(
			CURLOPT_RETURNTRANSFER => 1,
			CURLOPT_URL => $URL,
			CURLOPT_POST => count($urlParams),
			CURLOPT_POSTFIELDS => $param_string
		);
		
		global $client_ssl_curl_options;
		if (!empty($client_ssl_curl_options)) {
			foreach ($client_ssl_curl_options as $keyp => $valuep) {
				$curl_options[$keyp] = $valuep;
			}
		}

		curl_setopt_array($ch, $curl_options);
		
		$response = curl_exec($ch);
		$http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
				
		curl_close($ch);

		if ($http_code != 200) {
			throwClaveFirmaException($http_code);
		}
		
		if (!$response) {
			error_log("La llamada al servicio de FIRe no devolvio respuesta");
			throw new HttpNetworkException("No se obtuvo respuesta del servidor. Error: '" . curl_error($ch) . "' - Codigo: " . curl_errno($ch));
		}
		
		return $response;
	}
	
	/**
	 * Comprueba el status code devuelto por la llamada a la excepcion se corresponde con
	 * alguno de los codigos de error conocidos. En caso afirmativo, lanza la excepcion
	 * correspondiente. En caso contrario, no hace nada.
	 * @param $http_code StatusCode de la respuesta del servidor.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws HttpNoUserException Cuando el usuario indicado no existe.
	 * @throws HttpCertificateBlockedException Cuando existen certificados de firma vigentes pero estan bloqueados.
	 * @throws HttpCertificateAvailableException Cuando se intenta generar un certificado para un usuario que ya tiene.
	 * @throws HttpWeakRegistryException Cuando el usuario realizo un registro debil y no tiene ni puede tener certificados.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function throwClaveFirmaException($http_code)
	{
		error_log("Se obtuvo un error de la llamada al servicio de FIRe. StatusCode: ".$http_code);
		
		if ($http_code == 403) {
			throw new HttpForbiddenException("Acceso no autorizado");
		}
		else if ($http_code == 404 || $http_code == 408) {
			throw new HttpNetworkException("No se pudo conectar con el servidor de destino");
		}
		else if ($http_code == 522) {
			// El usuario no tiene certificados. No hacemos nada.
			return;
		}
		else if ($http_code == 523) {
			throw new HttpNoUserException("Usuario no valido");
		}
		else if ($http_code == 524) {
			throw new HttpCertificateBlockedException("Usuario con certificados bloqueados");
		}
		else if ($http_code == 525) {
			throw new HttpCertificateAvailableException("El usuario ya tiene certificados de firma");
		}
		else if ($http_code == 530) {
			throw new HttpWeakRegistryException("El usuario realizo un registro debil y no puede tener certificados de firma");
		}
		else if ($http_code == 532) {
			throw new HttpOperationException("Error durante la actualizacion de firma");
		}
		else if ($http_code == 533) {
			throw new HttpOperationException("Error al guardar la firma en servidor");
		}
		else if ($http_code == 534) {
			throw new BatchNoSignedException("El lote no se ha firmado");
		}
		else if ($http_code == 535) {
			throw new InvalidBatchDocumentException("El documento no existe en el lote");
		}
		else if ($http_code == 536) {
			throw new InvalidBatchDocumentException("La firma solicitada no se encuentra disponible");
		}
		else if ($http_code == 537) {
			throw new HttpOperationException("Se intenta firmar un lote sin documentos");
		}
		else if ($http_code == 538) {
			throw new HttpOperationException("La firma generada no es valida");
		}
		else if ($http_code == 539) {
			throw new HttpOperationException("Gestor de documentos no valido");
		}
		else if ($http_code == 540) {
			throw new HttpOperationException("Error al obtener un documento a traves del gestor en el servidor");
		}
		else if ($http_code / 100 >= 3) {
			throw new HttpOperationException("Error indeterminado (".$http_code.")");
		}
	}

	class HttpForbiddenException extends Exception { }
	
	class HttpNoUserException extends Exception { }
	
	class HttpCertificateBlockedException extends Exception { }
	
	class HttpWeakRegistryException extends Exception { }
	
	class HttpNetworkException extends Exception { }
	
	class HttpCertificateAvailableException extends Exception { }
	
	class HttpOperationException extends Exception { }
		
	/* =================================================================== */
	/* ====================== Funciones de utilidad ====================== */
	/* =================================================================== */
	
	/**
	* Funcion de utilidad para obtener un objeto de certificado a partir de su codificacion en base 64
	* (tal como lo devuelve la funcion getList).
	* @param $certB64 Certificado codificado en base 64 sin saltos de linea.
	*/
	function parseCertificate($certB64){
		
		// Agregamos los saltos de linea al certificado y la cabecera y pie para poder parsearlo
		$offset = 0;
		$b64 ="";
		do {
			$b64.= substr($certB64, $offset, 64)."\n";
			$offset  += 64;
		} while($offset  < strlen($certB64));
		
		$beginpem = "-----BEGIN CERTIFICATE-----\n";
		$endpem = "-----END CERTIFICATE-----\n";	
		
		return openssl_x509_parse( $beginpem.$b64.$endpem );
	}
		
	/**
	 * Clase que almacena la respuesta del servicio de carga de datos.
	 */
	class ClaveFirmaLoadResult{
		var $transactionId;
		var $redirectUrl;
		var $triphaseData;
		
		function __construct ($response){
			$json = json_decode($response, true);
			$this->transactionId = $json["transacionid"];
			$this->redirectUrl = $json["redirecturl"];
			$this->triphaseData = $json["triphasedata"];
			if (empty($this->transactionId)){
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga el identificador de la transacción");
			}
			if (empty($this->redirectUrl )){
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga la URL a redireccionar al usuario para que se autentique");
			}
			if (empty($this->triphaseData)){ 
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga los datos de la sesion trifasica");
			}
		}
		
		function ClaveFirmaLoadResult ($response){
			__construct($response);
		}
	};
	
	/**
	 * Clase que almacena la respuesta del servicio de generacion de certificado.
	 */
	class GenerateCertificateResult{
		var $transactionId;
		var $redirectUrl;
		
		function __construct ($response){
			$json = json_decode($response, true);
			$this->transactionId = $json["transacionid"];
			$this->redirectUrl = $json["redirecturl"];
			if (empty($this->transactionId)){
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga el identificador de la transacción");
			}
			if (empty($this->redirectUrl )){
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga la URL a redireccionar al usuario para que se autentique");
			}
		}
		
		function GenerateCertificateResult ($response){
			__construct($response);
		}
	};
?>