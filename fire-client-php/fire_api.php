<?php

/** Copyright (C) 2019 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 29/11/2019
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */

 /* ================= Componente distribuido PHP de FIRe (Version 2.4) ================= */
 
	// Definimos la url del servicio de FIRe
	define ("SERVICEURL","https://127.0.0.1:8443/fire-signature/fireService");
	
	// Definimos los parametros de conexion SSL (https://curl.haxx.se/libcurl/c/easy_setopt_options.html)
	$client_ssl_curl_options = array(
		CURLOPT_SSLCERT => "C:/Users/carlos.gamuci/Documents/FIRe/Ficheros_Despliegue/client_ssl_new_public_cert.pem",
		CURLOPT_SSLCERTTYPE => "PEM",
		CURLOPT_SSLKEY => "C:/Users/carlos.gamuci/Documents/FIRe/Ficheros_Despliegue/client_ssl_new_private_key.pem",
		CURLOPT_SSLKEYTYPE => "PEM",
		CURLOPT_KEYPASSWD => "12341234",
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
			"appid" => $this->appId,
			"subjectid" => $subjectId,
			"cop" => $op,
			"format" => $ft,
			"algorithm" => $algth,
			"properties" => $propB64us,
			"dat" => $dataB64us,
			"config" => $confB64us
		);

		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);

		// Procesamos la respuesta
		return new SignOperationResult($response);
	}

	/**
	 * Compone la firma electronica y la devuelve.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $subjectId Identificador del usuario (numero de DNI).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a sign()).
	 * @param $upgrade Formato longevo al que actualizar la firma. Por defecto, nulo.
	 * @param $upgradeConfigB64 Properties codificado en base 64 con la configuracion para la plataforma de
	 * 			validacion y actualizacion. Por defecto, nulo.
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function recoverSign($appId, $subjectId, $transactionId, $upgrade=null, $upgradeConfigB64=null){
		
		// Comprobamos las variables de entrada
		if (empty($subjectId)) {
            throw new InvalidArgumentException("El identificador de usuario no puede ser nulo");
		}
		if (empty($transactionId)) {
		    throw new InvalidArgumentException("El id de la transaccion no puede ser nulo");
		}
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 2, // El tipo de operacion solicitada es RECOVER_SIGN (2)
			"appid" => $appId,
			"subjectid" => $subjectId,
			"transactionid" => $transactionId
		);
		
		$configB64us = ($upgradeConfigB64 != null) ? str_replace(array("+", "/"), array("-", "_"), $upgradeConfigB64) : "";

		if (!empty($upgrade)) {
			$URL_SERVICE_PARAMS["upgrade"] = $upgrade;
		}
		if (!empty($configB64us)) {
			$URL_SERVICE_PARAMS["config"] = $configB64us;
		}									 
		
		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
		
		// Si la respuesta notifica un error, senala que hay que esperar un periodo de gracia para obtener el
		// binario firmado o si ya incluye la firma, se devuelve
		$transaction = new TransactionResult($response);
		if (isset($transaction->errorCode) || isset($transaction->gracePeriod) || isset($transaction->result)) {
			return $transaction;
		}
		
		// Si no tenemos el binario resultante, lo pedimos
		$URL_SERVICE_PARAMS = array(
			"op" => 11, // El tipo de operacion solicitada es RECOVER_SIGN_RESULT (11)
			"appid" => $appId,
			"subjectid" => $subjectId,
			"transactionid" => $transactionId				
		);
		
		// Llamamos al servicio
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
		// Agregamos el resultado recibido como firma resultante de la operacion
		$transaction->result = $response;
		
		return $transaction;
	}
	
	/**
	 * Obtiene el error obtenido al realizar la firma.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $subjectId Identificador del usuario (numero de DNI).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a sign()).
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function recoverError($appId, $subjectId, $transactionId){
		// Comprobamos las variables de entrada
		if (empty($subjectId)) {
			throw new InvalidArgumentException("El identificador de usuario no puede ser nulo");
		}
		if (empty($transactionId)) {
		    throw new InvalidArgumentException("El id de la transaccion no puede ser nulo");
		}
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 99, // El tipo de operacion solicitada es RECOVER_ERROR (99)
			"appid" => $appId,
			"subjectid" => $subjectId,
			"transactionid" => $transactionId
							
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
			"appid" => $this->appId,
			"subjectid" => $subjectId,
			"cop" => $op,
			"format" => $ft,
			"algorithm" => $algth,
			"properties" => $propB64us,
			"config" => $confB64us
		);

		// Si se ha indicado un formato de upgrade, lo actualizamos
        if (isset($upgrade) && !empty($upgrade)) {
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
	 * @param $subjectId Identificador del usuario (numero de DNI).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a createBatch()).
	 * @param $documentId Identificador unico del documento que se adjunta al lote.
     * @param $documentB64 Datos a firmar como parte del lote codificados en base 64.
	 * @param $confB64 Configuracion de la operacion particular codificada en base 64.
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws NumDocumentsExceededException Cuando se agregan mas documentos de los permitidos a un lote.
	 * @throws DuplicateDocumentException Cuando se agrega a un lote un documento con un identificador ya utilizado en el lote.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function addDocumentToBatch($appId, $subjectId, $transactionId, $documentId, $documentB64, $confB64){
		// Comprobamos las variables de entrada
		if (empty($subjectId)) {
            throw new InvalidArgumentException("El identificador de usuario no puede ser nulo");
		}
		if (empty($transactionId)) {
            throw new InvalidArgumentException("El identificador de la transaccion no puede ser nulo");
		}
		if (empty($documentId)) {
            throw new InvalidArgumentException("El identificador del documento no puede ser nulo");
		}
		if (empty($documentB64)) {
			throw new InvalidArgumentException("El documento no puede ser nulo");
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
			"subjectid" => $subjectId,				
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
	 * @param $subjectId Identificador del usuario (numero de DNI).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a createBatch()).
	 * @param $documentId Identificador unico del documento que se adjunta al lote.
     * @param $documentB64 Datos a firmar como parte del lote codificados en base 64.
	 * @param $op Operacion a realizar ('sign', 'cosign' o 'countersign').
	 * @param $ft Formato de firma ('CAdES', 'XAdES', 'PAdES'...)
	 * @param $propB64 Configuracion de la operacion de firma codificada en base 64. Equivalente al extraParams
	 * del MiniApplet @firma.
	 * @param $upgrade Actualizacion.
     * @param $confB64 Configuracion de la operacion particular codificada en base 64.
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws NumDocumentsExceededException Cuando se agregan mas documentos de los permitidos a un lote.
	 * @throws DuplicateDocumentException Cuando se agrega a un lote un documento con un identificador ya utilizado en el lote.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function addCustomDocumentToBatch($appId, $subjectId, $transactionId, $documentId, $documentB64, $op, $ft, $propB64, $upgrade, $confB64){
		// Comprobamos las variables de entrada
		if (empty($subjectId)) {
            throw new InvalidArgumentException("El identificador de usuario no puede ser nulo");
		}
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
			"subjectid" => $subjectId,
			"transactionid" => $transactionId,
			"docid" => $documentId,
			"dat" => $documentB64us,
			"cop" => $op,
			"format" => $ft,
			"properties" => $propB64us,
			"config" => $confB64us							 
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
	 * @param $subjectId Identificador del usuario (numero de DNI).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a createBatch()).
     * @param $stopOnError Indicador de parar la operacion al producirse un error.
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function signBatch($appId, $subjectId, $transactionId, $stopOnError){
		// Comprobamos las variables de entrada
		if (empty($subjectId)) {
            throw new InvalidArgumentException("El identificador de usuario no puede ser nulo");
		}
		if (empty($transactionId)) {
            throw new InvalidArgumentException("El identificador de la transaccion no puede ser nulo");
		}
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 7, // El tipo de operacion solicitada es SIGN_BATCH (7)
			"appid" => $appId,
			"subjectid" => $subjectId,
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
	 * @param $subjectId Identificador del usuario (numero de DNI).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a createBatch()).
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function recoverBatchResult	($appId, $subjectId, $transactionId){
		
		// Comprobamos las variables de entrada
		if (empty($subjectId)) {
            throw new InvalidArgumentException("El identificador de usuario no puede ser nulo");
		}
		if (empty($transactionId)) {
		    throw new InvalidArgumentException("El id de la transaccion no puede ser nulo");
		}
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 8, // El tipo de operacion solicitada es RECOVER_BATCH (8)
			"appid" => $appId,
			"subjectid" => $subjectId,
			"transactionid" => $transactionId
		);

		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
		
		// Parseamos el json recibido
		$jsonResponse = json_decode($response);
		
		$batchDocuments = $jsonResponse->batch;
		$providerName = $jsonResponse->prov;
		$signingCert = $jsonResponse->cert;
		
		return new BatchResult($batchDocuments, $providerName, $signingCert);
	}
	
	/**
	 * Obtiene el progreso de firma del batch.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $subjectId Identificador del usuario (numero de DNI).
	 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a createBatch()).
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function recoverBatchResultState($appId, $subjectId, $transactionId){
		
		// Comprobamos las variables de entrada
		if (empty($subjectId)) {
			throw new InvalidArgumentException("El identificador de usuario no puede ser nulo");
		}
		if (empty($transactionId)) {
		    throw new InvalidArgumentException("El id de la transaccion no puede ser nulo");
		}
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 9, // El tipo de operacion solicitada es RECOVER_BATCH_STATE (9)
 			"appid" => $appId,
			"subjectid" => $subjectId,
			"transactionid" => $transactionId
		);

		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
	
		return $response;
	}
	
	/**
	 * Obtiene la firma de uno de los documentos del batch.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $subjectId Identificador del usuario (numero de DNI).
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
	function recoverBatchSign($appId, $subjectId, $transactionId, $docId){
		
		// Comprobamos las variables de entrada
		if (empty($subjectId)) {
			throw new InvalidArgumentException("El identificador de usuario no puede ser nulo");
		}
		if (empty($transactionId)) {
		    throw new InvalidArgumentException("El id de la transaccion no puede ser nulo");
		}
		if (empty($docId)) {
            throw new InvalidArgumentException("El identificador de documento no puede ser nulo");
		}
		
		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 10, // El tipo de operacion solicitada es RECOVER_BATCH_SIGN (10)
			"appid" => $appId,
			"subjectid" => $subjectId,				 
			"transactionid" => $transactionId,
			"docid" => $docId
		);

		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
	
		return new TransactionResult($response);
	}
	
	/**
	 * Recupera una firma generada anteriormente y para la que se solicito la espera de un
	 * periodo de gracia antes de su recuperacion.
	 * @param $appId Identificador de la aplicacion (proporcionado por el administrador del servidor central).
	 * @param $docId Identificador del documento obtenido del perioro de gracia.
	 * @param $upgrade Formato longevo al que se solicito actualizar la firma. Si se indica, se
	 * usara para comprobar que la firma se actualizo al formato solicitado.
	 * @param $confB64 Properties de configuracion adicional para la plataforma de validacion codificada en en base64.
	 * @param partial Booleano que indica si se acepta o no una actualizacion parcial de la firma.
	 * @return Resultado con la firma recibida o un nuevo periodo de gracia.
	 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function recoverAsyncSign($appId, $docId, $upgrade, $confB64, $partial){
		
		// Comprobamos las variables de entrada
		if (empty($docId)) {
			throw new InvalidArgumentException("El identificador del documento firmado no puede ser nulo");
		}

		// Recodificamos los parametros que lo necesiten para asegurar la correcta transmision por URL
		$b64SpC = array("+", "/"); 
		$b64UrlSafeSpC = array("-", "_");
		
		$confB64us = ($confB64 != null) ? str_replace($b64SpC, $b64UrlSafeSpC, $confB64) : "";

		// Componemos la URL de llamada al servicio remoto
		$URL_SERVICE = SERVICEURL;
		$URL_SERVICE_PARAMS = array(
			"op" => 70, // El tipo de operacion solicitada es RECOVER_UPDATED_SIGN (70)
			"appid" => $appId,
			"docid" => $docId,
			"config" => $confB64us,
			"partial" => $partial
		);
		
		if (!empty($upgrade)) {
			$URL_SERVICE_PARAMS["upgrade"] = $upgrade;
		}
		
		// Llamamos al servicio remoto
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
		
		// Si la respuesta notifica un error, senala que hay que esperar un periodo de gracia para obtener el
		// binario firmado o si ya incluye la firma, se devuelve
		$transaction = new TransactionResult($response);
		if (isset($transaction->errorCode) || isset($transaction->gracePeriod) || isset($transaction->result)) {
			return $transaction;
		}
		
		// Si no tenemos el binario resultante, lo pedimos
		$URL_SERVICE_PARAMS = array(
			"op" => 71, // El tipo de operacion solicitada es RECOVER_UPDATED_SIGN_RESULT (71)
			"appid" => $appId,
			"docid" => $docId
		);
		
		// Llamamos al servicio
		$response = connect($URL_SERVICE, $URL_SERVICE_PARAMS);
		
		// Agregamos el resultado recibido como firma resultante de la operacion
		$transaction->result = $response;
		
		return $transaction;
	}
	
	/**
	 * Funcion que realiza la conexion a la URL pasada en parametro, enviando los parametros de conexion por POST.
	 * @param $URL La url a la que intentar conectarse.
	 * @param $urlParams Array con los parametros necesarios para el intento de conexion.
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
		$httpResponse = new HttpResponse($response, $ch);
			
		curl_close($ch);

		if (!$httpResponse->isOk()) {
			throwCustomException($httpResponse);
		}

		return $httpResponse->content;
	}
	
	/**
	 * Comprueba el status code devuelto por la llamada a la excepcion se corresponde con
	 * alguno de los codigos de error conocidos. En caso afirmativo, lanza la excepcion
	 * correspondiente. En caso contrario, no hace nada.
	 * @param $httpResponse StatusCode de la respuesta del servidor.
	 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
	 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
	 * @throws NumDocumentsExceededException Cuando se agregan mas documentos de los permitidos a un lote.
	 * @throws DuplicateDocumentException Cuando se agrega a un lote un documento con un identificador ya utilizado en el lote.
	 * @throws BatchNoSignedException Cuando se intenta recuperar el resultado de un lote antes de firmar el lote.
	 * @throws InvalidBatchDocumentException Cuando se solicita un documento que no existe o que no se firmo correctamente en un lote.
	 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
	 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
	 */
	function throwCustomException($httpResponse)
	{
		// Los errores devueltos por el propio FIRe estaran siempre estructurados en JSON
		if ($httpResponse->contentType == "application/json") {

			// Resultado JSON con forma: {c=codigo_error, m=mensaje_error}
			$errorResult = json_decode($httpResponse->content, false);
			$errorCode = $errorResult->c;
			$errorMessage = $errorResult->m;
			
			if (FIReErrors::FORBIDDEN == $errorCode || FIReErrors::UNAUTHORIZED == $errorCode) {
				throw new HttpForbiddenException($errorMessage, $errorCode);
			}
			else if (FIReErrors::UNKNOWN_USER == $errorCode) {
				throw new HttpNoUserException($errorMessage, $errorCode);
			}
			else if (FIReErrors::INVALID_TRANSACTION == $errorCode) {
				throw new InvalidTransactionException($errorMessage, $errorCode);
			}
			else if (FIReErrors::CERTIFICATE_BLOCKED == $errorCode) {
				throw new HttpCertificateBlockedException($errorMessage, $errorCode);
			}
			else if (FIReErrors::CERTIFICATE_WEAK_REGISTRY == $errorCode) {
				throw new HttpWeakRegistryException($errorMessage, $errorCode);
			}
			else if (FIReErrors::BATCH_DUPLICATE_DOCUMENT == $errorCode) {
				throw new DuplicateDocumentException($errorMessage, $errorCode);
			}
			else if (FIReErrors::BATCH_INVALID_DOCUMENT == $errorCode) {
				throw new InvalidBatchDocumentException($errorMessage, $errorCode);
			}
			else if (FIReErrors::BATCH_NUM_DOCUMENTS_EXCEEDED == $errorCode) {
				throw new NumDocumentsExceededException($errorMessage, $errorCode);
			}
			else if (FIReErrors::BATCH_NO_SIGNED == $errorCode) {
				throw new BatchNoSignedException($errorMessage, $errorCode);
			}
			else {
				if ($httpResponse->statusCode == 413) {
					throw new HttpTooLargeContentException("El contenido de la peticion era demasiado grande", FIReErrors::TOO_LARGE_CONTENT);
				}
				throw new HttpOperationException($errorMessage, $errorCode);
			}												  
		}

		// Procesamos los errores devueltos por el servidor, probablemente por un error interno o de
		// comunicacion
		if ($httpResponse->statusCode == 403) {
			throw new HttpForbiddenException("Error HTTP ".$httpResponse->statusCode);
		} else if ($httpResponse->statusCode == 404 || $httpResponse->statusCode == 408) {
			throw new HttpNetworkException("Error HTTP ".$httpResponse->statusCode);
		} else {
			throw new HttpOperationException("Error HTTP ".$httpResponse->statusCode);
		}
	}

	class FIReErrors {
		/** Error en la lectura de los parametros de entrada. */
		const READING_PARAMETERS = 1;
		/** No se ha indicado el identificador de la aplicacion. */
		const PARAMETER_APP_ID_NEEDED = 2;
		/** No se ha indicado la operacion a realizar. */
		const PARAMETER_OPERATION_NEEDED = 3;
		/** Se ha indicado un id de operacion no soportado. */
		const PARAMETER_OPERATION_NOT_SUPPORTED = 5;
		/** No se ha indicado el certificado de autenticacion. */
		const PARAMETER_AUTHENTICATION_CERTIFICATE_NEEDED = 6;
		/** Se ha indicado un certificado de autenticacion mal formado. */
		const PARAMETER_AUTHENTICATION_CERTIFICATE_INVALID = 7;
		/** No se ha indicado el identificador de usuario. */
		const PARAMETER_USER_ID_NEEDED = 8;
		/** No se ha indicado el algoritmo de firma. */
		const PARAMETER_SIGNATURE_ALGORITHM_NEEDED = 9;
		/** No se ha indicado la operacion de firma. */
		const PARAMETER_SIGNATURE_OPERATION_NEEDED = 10;
		/** No se ha indicado el formato de firma. */
		const PARAMETER_SIGNATURE_FORMAT_NEEDED = 11;
		/** No se han indicado los datos que firmar. */
		const PARAMETER_DATA_TO_SIGN_NEEDED = 12;
		/** Se han indicado datos a firmar mal codificados. */
		const PARAMETER_DATA_TO_SIGN_INVALID = 13;
		/** No se han encontrado los datos a firmar. */
		const PARAMETER_DATA_TO_SIGN_NOT_FOUND = 14;
		/** No se ha indicado la configuracion de transaccion. */
		const PARAMETER_CONFIG_TRANSACTION_NEEDED = 15;
		/** Se ha indicado una configuracion de transaccion mal formada. */
		const PARAMETER_CONFIG_TRANSACTION_INVALID = 16;
		/** No se ha indicado la URL de redireccion en caso de error en la configuracion de transaccion. */
		const PARAMETER_URL_ERROR_REDIRECION_NEEDED = 17;
		/** No se ha indicado el identificador de transaccion. */
		const PARAMETER_TRANSACTION_ID_NEEDED = 18;
		/** Se han indicado propiedades de configuracion de fima mal formadas. */
		const PARAMETER_SIGNATURE_PARAMS_INVALID = 20;
		/** El proveedor no tiene dado de alta al usuario indicado. */
		const UNKNOWN_USER = 21;
		/** El usuario ya dispone de un certificado del tipo que se esta solicitando generar. */
		const CERTIFICATE_DUPLICATED = 22;
		/** Error al obtener los certificados del usuario o al generar uno nuevo. */
		const CERTIFICATE_ERROR = 23;
		/** El usuario no puede poseer certificados de firma por haber realizado un registro no fehaciente. */
		const CERTIFICATE_WEAK_REGISTRY = 24;
		/** Error desconocido durante la operacion. */
		const UNDEFINED_ERROR = 25;
		/** Error durante la firma. */
		const SIGNING = 26;
		/** No se selecciono un proveedor de firma. */
		const PROVIDER_NOT_SELECTED = 27;
		/** La firma generada no es valida. */
		const INVALID_SIGNATURE = 31;
		/** Error durante la actualizacion de firma. */
		const UPGRADING_SIGNATURE = 32;
		/** No se ha indicado el identificador de los datos asincronos. */
		const PARAMETER_ASYNC_ID_NEEDED = 34;
		/** Gestor de documentos no valido. */
		const PARAMETER_DOCUMENT_MANAGER_INVALID = 35;
		/** Los certificados del usuario estan bloqueados. */
		const CERTIFICATE_BLOCKED = 38;
		/** El usuario no dispone de certificados y el proveedor no le permite generarlos en este momento. */
		const CERTIFICATE_NO_CERTS = 39;
		/** El identificador de documento ya existe en el lote. */
		const BATCH_DUPLICATE_DOCUMENT = 42;
		/** Se ha excedido el numero maximo de documentos permitidos en el lote. */
		const BATCH_NUM_DOCUMENTS_EXCEEDED = 43;
		/** Se intenta firmar un lote sin documentos. */
		const BATCH_NO_DOCUMENTS = 44;
		/** No se ha indicado el identificador del documento del lote. */
		const PARAMETER_DOCUMENT_ID_NEEDED = 48;
		/** No se ha firmado previamente el lote. */
		const BATCH_NO_SIGNED = 49;
		/** Error al firmar el lote. */
		const BATCH_SIGNING = 50;
		/** La firma se recupero anteriormente. */
		const BATCH_RECOVERED = 51;
		/** Se requiere esperar un periodo de gracia para recuperar el documento. */
		const BATCH_DOCUMENT_GRACE_PERIOD = 52;
		/** El documento no estaba en el lote. */
		const BATCH_INVALID_DOCUMENT = 53;
		/** El resultado del lote se recupero anteriormente. */
		const BATCH_RESULT_RECOVERED = 54;
	
		/** El contenido de la peticion era demasiado grande. */
		const TOO_LARGE_CONTENT = 413;
		/** Error interno del servidor. */
		const INTERNAL_ERROR = 500;
		/** Peticion rechazada. */
		const FORBIDDEN = 501;
		/** No se proporcionaron los parametros de autenticacion o no son correctos. */
		const UNAUTHORIZED = 502;
		/** La transaccion no se ha inicializado o ha caducado. */
		const INVALID_TRANSACTION = 503;
		/** Error detectado despues de llamar a la pasarela externa para autenticar al usuario. */
		const EXTERNAL_SERVICE_ERROR_TO_LOGIN = 504;
		/** Error detectado despues de llamar a la pasarela externa para firmar. */
		const EXTERNAL_SERVICE_ERROR_TO_SIGN = 505;
		/** Operacion cancelada. */
		const OPERATION_CANCELED = 507;
		/** El proveedor de firma devolvio un error. */
		const PROVIDER_ERROR = 508;
		/** No se pudo conectar con el proveedor de firma. */
		const PROVIDER_INACCESIBLE_SERVICE = 510;
	}
	
	class HttpOperationException extends Exception { }
	
	class HttpForbiddenException extends Exception { }
	
	class HttpNetworkException extends Exception { }
	
	class HttpTooLargeContentException extends Exception { }
	
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
	 * Clase que guarda la respuesta de una conexion remota.
	 */
	class HttpResponse {
	
		var $statusCode;
		var $contentType;
		var $content;
	
		function __construct ($response, $ch) {
			if ($response == null){
				error_log("La llamada al servicio de FIRe no devolvio respuesta");
				throw new HttpNetworkException("No se obtuvo respuesta del servidor. Error: '" . curl_error($ch) . "' - Codigo: " . curl_errno($ch));	
			}
			$this->statusCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
			$this->contentType = curl_getinfo($ch, CURLINFO_CONTENT_TYPE);
			if ($this->contentType != NULL) {
				$idx = strpos($this->contentType, ';');
				if ($idx > 0) {
					$this->contentType = substr($this->contentType, 0, $idx);
				}
			}
			$this->content = $response;
		}

		function HttpResponse ($response, $ch) {
			__construct ($response, $ch);
		}
		
		function isOk () {
			return $this->statusCode == 200;
		}
	}
	
	/**
	 * Clase que almacena la respuesta del servicio y obtiene el identificador de la transaccion.
	 */
	class TransactionIdResult {
		var $transactionId;
		
		function __construct ($response) {
			$json = json_decode($response, true);
			$this->transactionId = $json["transactionid"];
			if (empty($this->transactionId)){
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga el identificador de la transacción");
			}
		}
		
		function TransactionIdResult ($response){
			__construct ($response);
		}
	}
		
	/**
	 * Clase que almacena el resultado de una firma de lote.
	 */
	class BatchResult {
		var $providerName;
		var $signingCert;
		var $batch;
		
		function __construct ($batchDocuments, $provName, $cert) {

			if (!isset($batchDocuments)) {
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga el listado de resultados");
			}
			
			if (isset($provName)) {
				$this->providerName = $provName;
			}

			if (isset($cert)) {
				$this->signingCert = $cert;
			}
			
			$this->batch = array();
			foreach ($batchDocuments as $documentResult) {
				$this->batch[] = new BatchSignResult($documentResult);
			}
		}
		
		function BatchResult ($response, $provName, $cert) {
			__construct ($response, $provName, $cert);
		}
	};
	
	/**
	 * Clase que almacena el resultado particular de haber firmado un documento de un lote.
	 */
	class BatchSignResult {
		var $id;
		var $ok = false;
		var $dt;
		var $gracePeriod;
		
		function __construct ($response) {
			if (isset($response->id)) {
				$this->id = $response->id;
			}
			
			if (isset($response->ok)) {
				$this->ok = $response->ok;
			}
			
			if (isset($response->dt)) {
				$this->dt = $response->dt;
			}

			if (isset($response->grace)) {
				$this->gracePeriod = new GracePeriod($response->grace->id, "@".($response->grace->date/1000));
			}

			if (empty($this->id)) {
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga el identificador de cada documento batch");
			}
			if (empty($this->dt) && !$this->ok) {
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga el codigo de error si la firma no se llevo a cabo correctamente");
			}			
		}
		
		function BatchSignResult ($response) {
			__construct ($response);
		}
	};
	
	/**
	 * Clase que almacena la respuesta del servicio de solicitud de firma.
	 */
	 class SignOperationResult{
		var $transactionId;
		var $redirectUrl;
		
		function __construct ($response){
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
		
		function SignOperationResult ($response){
			__construct($response);
		}
	};
	  
	/**
	 * Clase que almacena la respuesta de recuperacion de firma o de error.
	 */
	class TransactionResult{
		
		// Especifica que la transaccion finalizo correctamente.
		const STATE_OK = 0;

		// Especifica que la transaccion no pudo finalizar debido a un error.
		const STATE_ERROR = -1;

		// Especifica que la transaccion aun no ha finalizado y se debera pedir el resultamos
		// mas adelante.
		const STATE_PENDING = 1;

		// Especifica que la transaccion ha finalizado pero que el resultado puede
		// diferir de lo solicitado por la aplicacion. Por ejemplo, puede haberse
		// solicitado una firma LTA-Level y recibirse una T-Level.
		const STATE_PARTIAL = 2;

		var $resultType;
		var $state;
		var $providerName;
		var $signingCert;
		var $upgradeFormat;
		var $gracePeriod;
		var $errorCode;
		var $errorMessage;
		var $result;
		
		function __construct($result){
	
			// Prefijo que antecede a los datos con la informacion de la firma
			$RESULT_PREFIX = "{\"result\":";
		
			$this->state = TransactionResult::STATE_OK;
			
			// Comprobamos si se ha recibido la informacion de la firma, en cuyo caso, la cargamos.
			if (strlen($result) > strlen($RESULT_PREFIX) + 2 && $RESULT_PREFIX == substr($result, 0, strlen($RESULT_PREFIX))) {
				$jsonResponse = json_decode($result);
				if (isset($jsonResponse->result->state)) {
					$this->state = $jsonResponse->result->state;
				}
				if (isset($jsonResponse->result->prov)) {
					$this->providerName = $jsonResponse->result->prov;
				}
				if (isset($jsonResponse->result->cert)) {
					$this->signingCert = $jsonResponse->result->cert;
				}
				if (isset($jsonResponse->result->upgrade)) {
					$this->upgradeFormat = $jsonResponse->result->upgrade;
				}
				if (isset($jsonResponse->result->grace)) {
					$this->gracePeriod = new GracePeriod($jsonResponse->result->grace->id, "@".($jsonResponse->result->grace->date/1000));
				}
				if (isset($jsonResponse->result->ercod)) {
					$this->errorCode = $jsonResponse->result->ercod;
				}
				if (isset($jsonResponse->result->ermsg)) {
					$this->errorMessage = $jsonResponse->result->ermsg;
				}
			}
			// Si no, se considera que lo recibido es el resultado de la firma.
			else {
				$this->result = $result;
			}
		}
		
		function TransactionResult ($result) {
			__construct($result);
		}
	};
	
	/**
	 * Clase que almacena la informacion del periodo de gracia solicitado antes de la recuperacion de la firma.
	 */
	class GracePeriod {
		var $id;
		var $date;

		/**
		 * Construye un objeto con la informacion del periodo de gracia.
		 * $gracePeriodId Identificador para la recuperacion de los datos firmados.
		 * $gracePeriodDate Cadena de texto con marca de tiempo UNIX (segundos desde 1 de enero del 1970 a las 00:00:00).
		 */
		function __construct($gracePeriodId, $gracePeriodDate){
			$this->id = $gracePeriodId;
			$this->date = new DateTime($gracePeriodDate);
		}

		function GracePeriod ($gracePeriodId, $gracePeriodDate) {
			__construct($gracePeriodId, $gracePeriodDate);
		}
	}
?>