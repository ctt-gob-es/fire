<?php

/** Copyright (C) 2019 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
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
		CURLOPT_SSLKEYPASSWD => "12341234",
		CURLOPT_SSL_VERIFYPEER => 0
	);
	
	/**
	 * Clase cliente para la conexion con el componente central de FIRe y la ejecucion de operaciones de firma.
	 */
	 class FireClient {

		var $appId;
		
		/**
		 * Construye el cliente para la ejecucion de operaciones de firma con FIRe.
		 * @param $id Identificador de la aplicacion (proporcionado por el administrador del servidor de FIRe).
		 */
		function __construct ($id){
			$this->appId = $id;
		}
	
		/**
		 * Realiza la firma de datos en el servidor.
		 * Devuelve un objeto de tipo SignOperationResult con el identificador de la transaccion, la URL a la que
		 * redirigir al usuario para que inserte la contrasena de su clave de firma y el resultado parcial de la
		 * firma trifasica.
		 * @return Objeto SignOperationResult con el identificador de transaccion y la URL de redireccion.
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
		function sign($subjectId, $op, $ft, $algth, $propB64, $dataB64, $confB64){

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
			$response = $this->connect($URL_SERVICE, $URL_SERVICE_PARAMS);

			// Procesamos la respuesta
			return new SignOperationResult($response);
		}

		/**
		 * Compone la firma electronica y la devuelve.
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
		function recoverSign($subjectId, $transactionId, $upgrade=null, $upgradeConfigB64=null){
			
			// Comprobamos las variables de entrada
			if (empty($transactionId)) {
				throw new InvalidArgumentException("El id de la transaccion no puede ser nulo");
			}

			if (empty($subjectId)) {
				throw new InvalidArgumentException("El identificador de usuario no puede ser nulo");
			}
			
			// Componemos la URL de llamada al servicio remoto
			$URL_SERVICE = SERVICEURL;
			$URL_SERVICE_PARAMS = array(
				"op" => 2, // El tipo de operacion solicitada es RECOVER_SIGN (2)
				"appid" => $this->appId,
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
			$response = $this->connect($URL_SERVICE, $URL_SERVICE_PARAMS);
			
			// Si la respuesta notifica un error, senala que hay que esperar un periodo de gracia para obtener el
			// binario firmado o si ya incluye la firma, se devuelve
			$transaction = new TransactionResult($response);
			if (isset($transaction->errorCode) || isset($transaction->gracePeriod) || isset($transaction->result)) {
				return $transaction;
			}
			
			// Si no tenemos el binario resultante, lo pedimos
			$URL_SERVICE_PARAMS = array(
				"op" => 11, // El tipo de operacion solicitada es RECOVER_SIGN_RESULT (11)
				"appid" => $this->appId,
				"transactionid" => $transactionId,
				"subjectid" => $subjectId
			);
			
			// Llamamos al servicio
			$response = $this->connect($URL_SERVICE, $URL_SERVICE_PARAMS);
			
			// Agregamos el resultado recibido como firma resultante de la operacion
			$transaction->result = $response;
			
			return $transaction;
		}
		
		/**
		 * Obtiene el error obtenido al realizar la firma.
		 * @param $subjectId Identificador del usuario (numero de DNI).
		 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a sign()).
		 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
		 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
		 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
		 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
		 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
		 */
		function recoverError($subjectId, $transactionId) {
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
				"appid" => $this->appId,
				"subjectid" => $subjectId,
				"transactionid" => $transactionId			
			);

			// Llamamos al servicio remoto
			$response = $this->connect($URL_SERVICE, $URL_SERVICE_PARAMS);
			
			// Procesamos la respuesta parseada
			return new TransactionResult($response);
		}

		/**
		 * Crea un batch con varios documentos para su firma posterior.
		 * Devuelve un objeto de tipo TransactionIdResult con el identificador de la transaccion como referenca del batch creado.
		 * @return Objeto SignOperationResult con el identificador de transaccion y la URL de redireccion.
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
		function createBatchProcess($subjectId, $op, $ft, $algth, $propB64, $upgrade, $confB64){
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
			$response = $this->connect($URL_SERVICE, $URL_SERVICE_PARAMS);

			// Procesamos la respuesta
			return new TransactionIdResult($response);
		}

		/**
		 * Incluye un documento en el proceso batch.
		 * A partir del identificador de la transaccion incluye un documento en el batch para su posterior firma.
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
		function addDocumentToBatch($subjectId, $transactionId, $documentId, $documentB64, $confB64){
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
				"appid" => $this->appId,
				"subjectid" => $subjectId,
				"transactionid" => $transactionId,
				"docid" => $documentId,
				"dat" => $documentB64us,
				"config" => $confB64us
			);

			// Llamamos al servicio remoto
			$response = $this->connect($URL_SERVICE, $URL_SERVICE_PARAMS);
		}
		
		
		/**
		 * Incluye un documento en el proceso batch con una configuracion de firma diferente a la de por defecto.
		 * A partir del identificador de la transaccion incluye un documento en el batch para su posterior firma.
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
		function addCustomDocumentToBatch($subjectId, $transactionId, $documentId, $documentB64, $op, $ft, $propB64, $upgrade, $confB64){
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
				"appid" => $this->appId,
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
			$response = $this->connect($URL_SERVICE, $URL_SERVICE_PARAMS);
		}
		
		/**
		 * Realiza la firma del batch.
		 * Devuelve un objeto de tipo SignOperationResult con el identificador de la transaccion, la URL a la que
		 * redirigir al usuario para que inserte la contrasena de su clave de firma y el resultado parcial de la
		 * firma trifasica.
		 * @return Objeto SignOperationResult con el identificador de transaccion y la URL de redireccion.
		 * @param $subjectId Identificador del usuario (numero de DNI).
		 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a createBatch()).
		 * @param $stopOnError Indicador de parar la operacion al producirse un error.
		 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
		 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
		 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
		 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
		 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
		 */
		function signBatch($subjectId, $transactionId, $stopOnError){
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
				"appid" => $this->appId,
				"subjectid" => $subjectId,
				"transactionid" => $transactionId,
				"stoponerror" => $stopOnError
			);

			// Llamamos al servicio remoto
			$response = $this->connect($URL_SERVICE, $URL_SERVICE_PARAMS);
			
			// Procesamos la respuesta
			return new SignOperationResult($response);
		}
		
		/**
		 * Obtiene el resultado de firma del batch.
		 * @param $subjectId Identificador del usuario (numero de DNI).
		 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a createBatch()).
		 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
		 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
		 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
		 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
		 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
		 */
		function recoverBatchResult	($subjectId, $transactionId){
			
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
				"appid" => $this->appId,
				"subjectid" => $subjectId,
				"transactionid" => $transactionId
			);

			// Llamamos al servicio remoto
			$response = $this->connect($URL_SERVICE, $URL_SERVICE_PARAMS);

			// Parseamos el json recibido
			$jsonResponse = json_decode($response);
						
			$batchDocuments = $jsonResponse->batch;
			$providerName = $jsonResponse->prov;
			$signingCert = $jsonResponse->cert;

			return new BatchResult($batchDocuments, $providerName, $signingCert);
		}
		
		/**
		 * Obtiene el progreso de firma del batch.
		 * @param $subjectId Identificador del usuario (numero de DNI).
		 * @param $transactionId Identificador de la transaccion (devuelto en la llamada a createBatch()).
		 * @throws InvalidArgumentException Cuando no se indica un parametro obligatorio.
		 * @throws HttpForbiddenException Cuando no se enviaron datos de autenticacion o estos no son correctos.
		 * @throws HttpNetworkException Cuando ocurre un problema en la comunicacion.
		 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
		 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
		 */
		function recoverBatchResultState($subjectId, $transactionId){
			
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
				"appid" => $this->appId,
				"subjectid" => $subjectId,
				"transactionid" => $transactionId
			);

			// Llamamos al servicio remoto
			$response = $this->connect($URL_SERVICE, $URL_SERVICE_PARAMS);
		
			return $response;
		}
		
		/**
		 * Obtiene la firma de uno de los documentos del batch.
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
		function recoverBatchSign($subjectId, $transactionId, $docId){
			
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
				"op" => 10, // El tipo de operacion solicitada es RECOVER_BATCH_SIGN (10)
				"appid" => $this->appId,
				"subjectid" => $subjectId,
				"transactionid" => $transactionId,
				"docid" => $docId
			);

			// Llamamos al servicio remoto
			$response = $this->connect($URL_SERVICE, $URL_SERVICE_PARAMS);
		
			return new TransactionResult($response);
		}
		
		/**
		 * Recupera una firma generada anteriormente y para la que se solicito la espera de un
		 * periodo de gracia antes de su recuperacion.
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
		function recoverAsyncSign($docId, $upgrade, $confB64, $partial){
			
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
				"appid" => $this->appId,
				"docid" => $docId,
				"config" => $confB64us,
				"partial" => $partial
			);
			
			if (!empty($upgrade)) {
				$URL_SERVICE_PARAMS["upgrade"] = $upgrade;
			}
			
			// Llamamos al servicio remoto
			$response = $this->connect($URL_SERVICE, $URL_SERVICE_PARAMS);
			
			// Si la respuesta notifica un error, senala que hay que esperar un periodo de gracia para obtener el
			// binario firmado o si ya incluye la firma, se devuelve
			$transaction = new TransactionResult($response);
			if (isset($transaction->errorCode) || isset($transaction->gracePeriod) || isset($transaction->result)) {
				return $transaction;
			}
			
			// Si no tenemos el binario resultante, lo pedimos
			$URL_SERVICE_PARAMS = array(
				"op" => 71, // El tipo de operacion solicitada es RECOVER_UPDATED_SIGN_RESULT (71)
				"appid" => $this->appId,
				"docid" => $docId
			);
			
			// Llamamos al servicio
			$response = $this->connect($URL_SERVICE, $URL_SERVICE_PARAMS);
			
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

			$http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);

			curl_close($ch);

			if ($http_code != 200) {
				$this->throwCustomException($http_code);
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
		 * @throws NumDocumentsExceededException Cuando se agregan mas documentos de los permitidos a un lote.
		 * @throws DuplicateDocumentException Cuando se agrega a un lote un documento con un identificador ya utilizado en el lote.
		 * @throws BatchNoSignedException Cuando se intenta recuperar el resultado de un lote antes de firmar el lote.
		 * @throws InvalidBatchDocumentException Cuando se solicita un documento que no existe o que no se firmo correctamente en un lote.
		 * @throws InvalidTransactionException Cuando se solicita operar con una transaccion no valida o ya caducada.
		 * @throws HttpOperationException Cuando se produce un error indeterminado en servidor durante la ejecucion de la operacion.
		 */
		function throwCustomException($http_code)
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
	}

	class HttpOperationException extends Exception { }
	
	class HttpForbiddenException extends Exception { }
		
	class HttpNetworkException extends Exception { }
	
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
	class BatchResult{
		var $providerName;
		var $signingCert;
		var $batch;
		
		function __construct ($batchDocuments, $provName, $cert){

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
		
		function BatchResult ($response, $provName, $cert){
			__construct ($response, $provName, $cert);
		}
	};
	
	/**
	 * Clase que almacena el resultado particular de haber firmado un documento de un lote.
	 */
	class BatchSignResult{
		var $id;
		var $ok = false;
		var $dt;
		var $gracePeriod;
		
		function __construct ($response){
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

			if (empty($this->id)){
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga el identificador de cada documento batch");
			}
			if (empty($this->dt) && !$this->ok){
				throw new InvalidArgumentException("Es obligatorio que el JSON contenga el codigo de error si la firma no se llevo a cabo correctamente");
			}			
		}
		
		function BatchSignResult ($response){
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
		public static $STATE_OK = 0;
		
		// Especifica que la transaccion no pudo finalizar debido a un error.
		public static $STATE_ERROR = -1;
			
		// Especifica que la transaccion aun no ha finalizado y se debera pedir el resultamos
		// mas adelante.
		public static $STATE_PENDING = 1;
			
		// Especifica que la transaccion ha finalizado pero que el resultado puede
		// diferir de lo solicitado por la aplicacion. Por ejemplo, puede haberse
		// solicitado una firma ES-A y recibirse una ES-T.
		public static $STATE_PARTIAL = 2;
		
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
		
			$this->state = TransactionResult::$STATE_OK;
			
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
			// Si no, se considera quen lo recibido es el resultado de la firma.
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
	class GracePeriod{
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