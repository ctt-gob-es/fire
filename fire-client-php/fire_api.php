<?php

/** Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */

 /* ================= Componente distribuido PHP de FIRe (Version 2.3) ================= */
 
	// Definimos la url del servicio de FIRe
	define ("SERVICEURL","https://127.0.0.1:8443/fire-signature/fireService");
	
	// Definimos los parametros de conexion SSL (https://curl.haxx.se/libcurl/c/easy_setopt_options.html)
	$client_ssl_curl_options = array(
		CURLOPT_SSLCERT => "C:/Entrada/client_ssl_public_cert.pem",
		CURLOPT_SSLCERTTYPE => "PEM",
		CURLOPT_SSLKEY => "C:/Entrada/client_ssl_private_key.pem",
		CURLOPT_SSLKEYTYPE => "PEM",
		CURLOPT_SSLKEYPASSWD => "12341234",
		CURLOPT_SSL_VERIFYPEER => 0
	);
	
	/**
	 * Realiza la firma de datos en el servidor.
	 * Devuelve un objeto de tipo SignOperationResult con el identificador de la transaccion, la URL a la que
	 * redirigir al usuario para que inserte la contrasena de su clave de firma y el resultado parcial de la
	 * firma trifasica.
	 * @return Objeto SignOperationResult con el identificador de transaccion y la URL de redireccion.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $subjectId Identificador del usuario (numero de DNI).
	 * @param $op Operacion a realizar ('sign', 'cosign' o 'countersign').
	 * @param $ft Formato de firma ('CAdES', 'XAdES', 'PAdES'...)
	 * @param $algth Algoritmo de firma (Debe estar soportado por el servicio de custodia de claves).
	 * @param $propB64 Configuracion de la operacion de firma. Equivalente al extraParams del MiniApplet @firma.
	 * @param $dataB64 Datos en base 64 que se desean firmar.
	 * @param $confB64 Configuracion del servicio. Se debe indicar al menos las URL de redireccion en caso
	 * de exito y error ("redirectOkUrl" y "redirectErrorUrl", respectivamente).
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */              
	function sign($appId, $subjectId, $op, $ft, $algth, $propB64, $dataB64, $confB64){

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
		if (empty($dataB64)) {
            throw new InvalidArgumentException("Los datos a firmar no pueden ser nulos");
		}
		
		// Recodificamos los parametros que lo necesiten para asegurar la correcta transmision por URL
		$b64SpC = array("+", "/"); 
		$b64UrlSafeSpC = array("-", "_");
		
		$propB64us = ($propB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $propB64) : "";
		$dataB64us = ($dataB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $dataB64) : "";
		$confB64us = ($confB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $confB64) : "";
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 1, // El tipo de operacion solicitada es SIGN (1)
			"config" => $confB64us,
			"algorithm" => $algth,
			"properties" => $propB64us,
			"cop" => $op,
			"format" => $ft,
			"dat" => $dataB64us,
			"subjectid" => $subjectId,
			"appid" => $appId
		);

		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);

		// Procesamos la respuesta
		return new SignOperationResult($response);
	}

	/**
	 * Compone la firma electronica y la devuelve.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a sign()).
	 * @param $upgrade Formato longevo al que actualizar la firma.
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function recoverSign($appId, $transactionId, $upgrade){
		
		// Comprobamos las variables de entrada
		if (empty($transactionId)) {
		    throw new InvalidArgumentException("El id de la transaccion no puede ser nulo");
		}
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 2, // El tipo de operacion solicitada es RECOVER_SIGN (2)
			"transactionid" => $transactionId,
			"appid" => $appId
		);
		
		if ($upgrade != null) {
			$URL_SERVICE_PARAMS["upgrade"] = $upgrade;
		}
		
		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
		
		// Si la respuesta notifica un error o incluye los datos procesados, se devuelve
		$transaction = new TransactionResult($response);
		if (isset($transaction->errorCode) || isset($transaction->result)) {
			return $transaction;
		}
		
		// Si no tenemos el binario resultante, lo pedimos
		$URL_SERVICE_PARAMS = array(
			"op" => 11, // El tipo de operacion solicitada es RECOVER_SIGN_RESULT (11)
			"transactionid" => $transactionId,
			"appid" => $appId
		);
		
		// Llamamos al servicio
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
		$transaction->result = $response;
		
		return $transaction;
	}
	
	/**
	 * Obtiene el error obtenido al realizar la firma.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a sign()).
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function recoverError($appId, $transactionId){
		// Comprobamos las variables de entrada
		if (empty($transactionId)) {
		    throw new InvalidArgumentException("El id de la transaccion no puede ser nulo");
		}
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 99, // El tipo de operacion solicitada es RECOVER_ERROR (99)
			"transactionid" => $transactionId,
			"appid" => $appId
		);

		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
		
		// Procesamos la respuesta parseada
		return new TransactionResult($response);
	}

	/**
	 * Crea un batch con varios documentos para su firma posterior.
	 * Devuelve un objeto de tipo TransactionIdResult con el identificador de la transaccion como referenca del batch creado.
	 * @return Objeto SignOperationResult con el identificador de transaccion y la URL de redireccion.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $subjectId Identificador del usuario (numero de DNI).
	 * @param $op Operacion a realizar ('sign', 'cosign' o 'countersign').
	 * @param $ft Formato de firma ('CAdES', 'XAdES', 'PAdES'...)
	 * @param $algth Algoritmo de firma (Debe estar soportado por el servicio de custodia de claves).
	 * @param $propB64 Configuracion de la operacion de firma. Equivalente al extraParams del MiniApplet @firma.
	 * @param $upgrade Actualizacion.
	 * @param $confB64 Configuracion del servicio. Se debe indicar al menos las URL de redireccion en caso
	 * de exito y error ("redirectOkUrl" y "redirectErrorUrl", respectivamente).
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */              
	function createBatchProcess($appId, $subjectId, $op, $ft, $algth, $propB64, $upgrade, $confB64){
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
		
		// Recodificamos los parametros que lo necesiten para asegurar la correcta transmision por URL
		$b64SpC = array("+", "/"); 
		$b64UrlSafeSpC = array("-", "_");
		
		$propB64us = ($propB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $propB64) : "";
		$confB64us = ($confB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $confB64) : "";
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 5, // El tipo de operacion solicitada es CREATE_BATCH (5)
			"config" => $confB64us,
			"algorithm" => $algth,
			"properties" => $propB64us,
			"cop" => $op,
			"format" => $ft,
			"subjectid" => $subjectId,
			"appid" => $appId
		);

		// Si se ha indicado un formato de upgrade, lo actualizamos
        if (!isset($upgrade) && !empty($upgrade)) {
			$URL_SERVICE_PARAMS["upgrade"] = $upgrade;
		}
		
		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);

		// Procesamos la respuesta
		return new TransactionIdResult($response);
	}

	/**
	 * Incluye un documento en el proceso batch.
	 * A partir del identificador de la transaccion incluye un documento en el batch para su posterior firma.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a createBatch()).
	 * @param $documentId Identificador unico del documento que se adjunta al lote.
     * @param $document Datos a firmar como parte del lote.
     * @param $confB64 Configuraci&oacute;n de la operaci&oacute;n.
	 * de exito y error ("redirectOkUrl" y "redirectErrorUrl", respectivamente).
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws NumDocumentsExceededException Cuando se agregan mas documentos de los permitidos a un lote.
	 * @throws DuplicateDocumentException Cuando se agrega a un lote un documento con un identificador ya utilizado en el lote.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function addDocumentToBatch($appId, $transactionId, $documentId, $documentB64, $confB64){
		// Comprobamos las variables de entrada
		if (empty($transactionId)) {
            throw new InvalidArgumentException("El identificador de la transaccion no puede ser nulo");
		}
		if (empty($documentId)) {
            throw new InvalidArgumentException("El identificador del documento no puede ser nulo");
		}
		
		// Recodificamos los parametros que lo necesiten para asegurar la correcta transmision por URL
		$b64SpC = array("+", "/"); 
		$b64UrlSafeSpC = array("-", "_");
		
		$confB64us = ($confB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $confB64) : "";
		$documentB64us = ($documentB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $documentB64) : "";
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 6, // El tipo de operacion solicitada es ADD_DOCUMENT_TO_BATCH (6)
			"appid" => $appId,
			"transactionid" => $transactionId,
			"docid" => $documentId,
			"dat" => $documentB64us,
			"config" => $confB64us
		);

		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
	}
	
	
	/**
	 * Incluye un documento en el proceso batch con una configuracion de firma diferente a la de por defecto.
	 * A partir del identificador de la transaccion incluye un documento en el batch para su posterior firma.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a createBatch()).
	 * @param $documentId Identificador unico del documento que se adjunta al lote.
     * @param $document Datos a firmar como parte del lote.
	 * @param $op Operacion a realizar ('sign', 'cosign' o 'countersign').
	 * @param $ft Formato de firma ('CAdES', 'XAdES', 'PAdES'...)
	 * @param $propB64 Configuracion de la operacion de firma. Equivalente al extraParams del MiniApplet @firma.
	 * @param $upgrade Actualizacion.
     * @param $confB64 Configuraci&oacute;n de la operaci&oacute;n.
	 * de exito y error ("redirectOkUrl" y "redirectErrorUrl", respectivamente).
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws NumDocumentsExceededException Cuando se agregan mas documentos de los permitidos a un lote.
	 * @throws DuplicateDocumentException Cuando se agrega a un lote un documento con un identificador ya utilizado en el lote.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function addCustomDocumentToBatch($appId, $transactionId, $documentId, $documentB64, $op, $ft, $propB64, $upgrade, $confB64){
		// Comprobamos las variables de entrada
		if (empty($transactionId)) {
            throw new InvalidArgumentException("El identificador de la transaccion no puede ser nulo");
		}
		if (empty($documentId)) {
            throw new InvalidArgumentException("El identificador del documento no puede ser nulo");
		}
		if (empty($documentB64)) {
            throw new InvalidArgumentException("El documento no puede ser nulo");
		}
		if (empty($op)) {
            throw new InvalidArgumentException("El tipo de operacion de firma a realizar no puede ser nulo");
		}
		if (empty($ft)) {
            throw new InvalidArgumentException("El formato de firma no puede ser nulo");
		}
		
		// Recodificamos los parametros que lo necesiten para asegurar la correcta transmision por URL
		$b64SpC = array("+", "/"); 
		$b64UrlSafeSpC = array("-", "_");
		
		$confB64us = ($confB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $confB64) : "";
		$propB64us = ($propB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $propB64) : "";
		$documentB64us = ($documentB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $documentB64) : "";
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 6, // El tipo de operacion solicitada es ADD_DOCUMENT_TO_BATCH (6)
			"appid" => $appId,
			"transactionid" => $transactionId,
			"docid" => $documentId,
			"dat" => $documentB64us,
			"config" => $confB64us,
			"cop" => $op,
			"format" => $ft,
			"properties" => $propB64us
		);

		// Si se ha indicado un formato de upgrade, lo actualizamos
        if (!isset($upgrade) && !empty($upgrade)) {
			$URL_SERVICE_PARAMS["upgrade"] = $upgrade;
		}
		
		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
	}
	
	/**
	 * Realiza la firma del batch.
	 * Devuelve un objeto de tipo SignOperationResult con el identificador de la transaccion, la URL a la que
	 * redirigir al usuario para que inserte la contrasena de su clave de firma y el resultado parcial de la
	 * firma trifasica.
	 * @return Objeto SignOperationResult con el identificador de transaccion y la URL de redireccion.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a createBatch()).
     * @param $stopOnError Indicador de parar la operacion al producirse un error.
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function signBatch($appId, $transactionId, $stopOnError){
		// Comprobamos las variables de entrada
		if (empty($transactionId)) {
            throw new InvalidArgumentException("El identificador de la transaccion no puede ser nulo");
		}
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 7, // El tipo de operacion solicitada es SIGN_BATCH (7)
			"appid" => $appId,
			"transactionid" => $transactionId,
			"stoponerror" => $stopOnError
		);

		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
		
		// Procesamos la respuesta
		return new SignOperationResult($response);
	}
	
	/**
	 * Obtiene el resultado de firma del batch.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a createBatch()).
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function recoverBatchResult	($appId, $transactionId){
		
		// Comprobamos las variables de entrada
		if (empty($transactionId)) {
		    throw new InvalidArgumentException("El id de la transaccion no puede ser nulo");
		}
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 8, // El tipo de operacion solicitada es RECOVER_BATCH (8)
			"transactionid" => $transactionId,
			"appid" => $appId
		);

		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
		$jsonResponse = json_decode($response);
		$providerName = $jsonResponse->prov;
		$batchDocuments = $jsonResponse->batch;
		// Parseamos el json recibido
		$allDocuments = array();
		foreach ($batchDocuments as $document) {
			$allDocuments[] = new BatchResult($document, $providerName);
		}		
		return $allDocuments;
	}
	
	/**
	 * Obtiene el progreso de firma del batch.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a createBatch()).
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function recoverBatchResultState($appId, $transactionId){
		
		// Comprobamos las variables de entrada
		if (empty($transactionId)) {
		    throw new InvalidArgumentException("El id de la transaccion no puede ser nulo");
		}
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 9, // El tipo de operacion solicitada es RECOVER_BATCH_STATE (9)
			"transactionid" => $transactionId,
			"appid" => $appId
		);

		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
	
		return $response;
	}
	
	/**
	 * Obtiene la firma de uno de los documentos del batch.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a createBatch()).
	 * @param $docId Identificador del documento del batch cuya firma queremos obtener.
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws BatchNoSignedException Cuando se intenta recuperar el resultado de un lote antes de firmar el lote.
	 * @throws InvalidBatchDocumentException Cuando Se solicita un documento que no existe o que no se firmo correctamente en un lote.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function recoverBatchSign($appId, $transactionId, $docId){
		
		// Comprobamos las variables de entrada
		if (empty($transactionId)) {
		    throw new InvalidArgumentException("El id de la transaccion no puede ser nulo");
		}
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 10, // El tipo de operacion solicitada es RECOVER_BATCH_SIGN (10)
			"transactionid" => $transactionId,
			"appid" => $appId,
			"docid" => $docId
		);

		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
	
		return new TransactionResult($response);
	}
	
	/**
	 * Funcion que realiza la conexion a la URL pasada en parametro, enviando los parametros de conexion por POST.
	 * @param string $URL La url a la que intentar conectarse.
	 * @param array $urlParams Los parametros necesarios para el intento de conexion.
	 * @return string $response Recupera los datos devueltos por la conexion.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws NumDocumentsExceededException Cuando se agregan mas documentos de los permitidos a un lote.
	 * @throws DuplicateDocumentException Cuando se agrega a un lote un documento con un identificador ya utilizado en el lote.
	 * @throws BatchNoSignedException Cuando se intenta recuperar el resultado de un lote antes de firmar el lote.
	 * @throws InvalidBatchDocumentException Cuando Se solicita un documento que no existe o que no se firmo correctamente en un lote.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
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

		if (!$response) {
			echo "Error en la llamada al servicio $URL";
			throw new HttpNetworkException("Error: " . curl_error($ch) . " - Code: " . curl_errno($ch));
		}

		$http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);

		curl_close($ch);

		if ($http_code != 200) {
			throwCustomException($http_code);
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
	 * @throws NumDocumentsExceededException Cuando se agregan mas documentos de los permitidos a un lote.
	 * @throws DuplicateDocumentException Cuando se agrega a un lote un documento con un identificador ya utilizado en el lote.
	 * @throws BatchNoSignedException Cuando se intenta recuperar el resultado de un lote antes de firmar el lote.
	 * @throws InvalidBatchDocumentException Cuando Se solicita un documento que no existe o que no se firmo correctamente en un lote.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function throwCustomException($http_code)
	{
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
		else if ($http_code == 526) {
			throw new NumDocumentsExceededException("Se excedido el numero maximo de documentos permitidos");
		}
		else if ($http_code == 527) {
			throw new DuplicateDocumentException("El identificador de documento ya existe en el lote");
		}
		else if ($http_code == 528) {
			throw new InvalidTransactionException("La transaccion no es valida o ha caducado");
		}
		else if ($http_code == 529) {
			throw new HttpOperationException("El servicio de custodia devolvio un error durante la firma de los datos");
		}
		else if ($http_code == 530) {
			throw new HttpOperationException("Error en la composicion de la firma");
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
			throw new InvalidBatchDocumentException("Fallo la firma del documento que se intenta recuperar");
		}
		else if ($http_code == 537) {
			throw new HttpOperationException("Se intenta firmar un lote sin documentos");
		}
		else if ($http_code / 100 >= 3) {
			throw new HttpOperationException("Error indeterminado (".$http_code.")");
		}
	}

	class HttpOperationException extends Exception { }
	
	class HttpForbiddenException extends HttpOperationException { }
		
	class HttpNetworkException extends HttpOperationException { }
	
	class NumDocumentsExceededException extends HttpOperationException { }
	
	class DuplicateDocumentException extends HttpOperationException { }
	
	class InvalidTransactionException extends HttpOperationException { }
	
	class BatchNoSignedException extends HttpOperationException { }
	
	class InvalidBatchDocumentException extends HttpOperationException { }
		
		
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
	 * Clase que almacena la respuesta del servicio y obtiene el identificador de la transaccion.
	 */
	class TransactionIdResult {
		var $transactionId;
		
		function TransactionIdResult ($response){
			$json = json_decode($response, true);
			$this->transactionId = $json["transactionid"];
			if (empty($this->transactionId)){
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga el identificador de la transacción");
			}
		}
	}
		
		
	/**
	 * Clase que almacena la respuesta en formato json en la firma de un batch
	 */
	class BatchResult{
		var $id;
		var $ok;
		var $dt;
		var $providerName;
		
		function BatchResult ($response, $provName){
			if(isset($response->id)) {
				$this->id = $response->id;
			}
			
			if(isset($response->ok)) {
				$this->ok = $response->ok;
			}
			
			if(isset($response->dt)) {
				$this->dt = $response->dt;
			}

			if(isset($provName)) {
				$this->providerName = $provName;
			}
			
			if (empty($this->id)){
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga el identificador de cada documento batch");
			}
			if (empty($this->ok)){
				throw new InvalidArgumentException("Es obligatorio que el JSON defina si la firma se ha realizado con exito");
			}
			else if (empty($this->dt) && !$this->ok){
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga el codigo de error si la firma no se llevo a cabo correctamente");
			}
		}
	};
	
	/**
	 * Clase que almacena la respuesta del servicio de solicitud de firma.
	 */
	 class SignOperationResult{
		var $transactionId;
		var $redirectUrl;
		
		function SignOperationResult ($response){
			$json = json_decode($response, true);
			$this->transactionId = $json["transactionid"];
			$this->redirectUrl = $json["redirecturl"];
			if (empty($this->transactionId)){
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga el identificador de la transacción");
			}
			if (empty($this->redirectUrl)){
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga la URL a redireccionar al usuario para que se autentique");
			}
		}
	};
	  
	/**
	 * Clase que almacena la respuesta de recuperacion de firma o de error.
	 */
	class TransactionResult{
		var $resultType;
		var $state;
		var $providerName;
		var $errorCode;
		var $errorMessage;
		var $result;
		
		function TransactionResult ($result) {
			// Especifica que la transacci&oacute;n finaliz&oacute; correctamente.
			$STATE_OK = 0;
			// Especifica que la transacci&oacute;n no pudo finalizar debido a un error.
			$STATE_ERROR = -1;
			
			// Prefijo que antecede a los datos con la informacion de la firma
			$RESULT_PREFIX = "{\"result\":";
		
			$this->state = $STATE_OK;
			
			// Comprobamos si se ha recibido la informacion de la firma, en cuyo caso, la cargamos.
			if (strlen($result) > strlen($RESULT_PREFIX) + 2 && $RESULT_PREFIX == substr($result, 0, strlen($RESULT_PREFIX))) {
				$jsonResponse = json_decode($result);
				if (isset($jsonResponse->result->prov)) {
					$this->providerName = $jsonResponse->result->prov;
				}
				if (isset($jsonResponse->result->ercod)) {
					$this->errorCode = $jsonResponse->result->ercod;
					$this->state = $STATE_ERROR;
				}
				if (isset($jsonResponse->result->ermsg)) {
					$this->errorMessage = $jsonResponse->result->ermsg;
				}
			}
			// Si no, se considera quen lo recibido es el resultado de la firma.
			else {
				$this->result = $result;
			}
		}
	};
?>