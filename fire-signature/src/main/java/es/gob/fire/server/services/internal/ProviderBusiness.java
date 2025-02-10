package es.gob.fire.server.services.internal;

import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.CertificateBlockedException;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.connector.WeakRegistryException;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.LogUtils;
import es.gob.fire.server.services.Responser;
import es.gob.fire.signature.ConfigManager;

/**
 * Clase con la l&oacute;gica de operacion de los proveedores de firma en la nube.
 */
public class ProviderBusiness {

	private static final Logger LOGGER = Logger.getLogger(ProviderBusiness.class.getName());

	/**
	 * Ejecuta y gestiona el flujo de firma con el Cliente @firma.
	 *
	 * @param session      Datos de la transacci&oacute;n.
	 * @param request      Petici&oacute;n HTTP realizada al servicio.
	 * @param response     Objeto HTTP de respuesta del servicio.
	 * @param trAux        Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 */
	static void signWithLocalProvider(final FireSession session,
			final HttpServletRequest request, final HttpServletResponse response,
			final TransactionAuxParams trAux) {

		final boolean skipSelection = Boolean.parseBoolean(session.getString(ServiceParams.SESSION_PARAM_SKIP_CERT_SELECTION));

		// Si se debe saltar la seleccion de certificado, configuramos que el cliente lo
		// seleccione automaticamente cuando solo haya uno
		if (skipSelection) {
			final Properties props = (Properties) session.getObject(ServiceParams.SESSION_PARAM_EXTRA_PARAM);
			props.setProperty(MiniAppletHelper.AFIRMA_EXTRAPARAM_HEADLESS, Boolean.TRUE.toString());
			session.setAttribute(ServiceParams.SESSION_PARAM_EXTRA_PARAM, props);
		}

		// Registramos los datos guardados
		SessionCollector.commit(session, trAux);
		session.saveIntoHttpSession(request.getSession());

		Responser.redirectToUrl(FirePages.PG_CLIENTE_AFIRMA, request, response, trAux);
	}

	/**
	 * Redirige el flujo de ejecuci&oacute;n para la firma con los certificados
	 * en la nube del proveedor indicado.
	 *
	 * @param providerName Nombre del proveedor de firma en la nube que se debe
	 *                     utilizar.
	 * @param trId         Identificador de la transacci&oacute;n.
	 * @param session      Datos de la transacci&oacute;n.
	 * @param request      Petici&oacute;n HTTP realizada al servicio.
	 * @param response     Objeto HTTP de respuesta del servicio.
	 * @param trAux        Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 */
	static void signWithCloudProvider(final String providerName,
			final String trId, final FireSession session,
			final HttpServletRequest request, final HttpServletResponse response,
			final TransactionAuxParams trAux) {

		final LogTransactionFormatter logF = trAux.getLogFormatter();

		final String subjectId = session.getString(ServiceParams.SESSION_PARAM_SUBJECT_ID);
		final TransactionConfig connConfig = (TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
		final boolean forcedProvider = Boolean.parseBoolean(
				session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED));

		// Listamos los certificados del usuario
		X509Certificate[] certificates = null;
		try {
			certificates = ProviderBusiness.getCertificates(providerName, subjectId, connConfig, trAux);
		} catch (final BusinessException e) {
			ErrorManager.setErrorToSession(session, e.getFireError(), false, trAux);
			redirectToErrorPage(forcedProvider, connConfig, request, response, trAux);
			return;
		}

		String redirectUrl;

		// Si no ha certificados, vamos a la pagina para permitir generarlos (si no se pudiese, habria
		// fallado antes
		if (certificates == null || certificates.length == 0) {
			LOGGER.info(logF.f(
					"El usuario no dispone de certificados, pero el conector le permite generarlos")); //$NON-NLS-1$
			redirectUrl = FirePages.PG_CHOOSE_CERTIFICATE_NOCERT;
		}
		else {
			final boolean skipSelection = connConfig.isAppSkipCertSelection() != null
					? connConfig.isAppSkipCertSelection().booleanValue()
							: ConfigManager.isSkipCertSelection();

			// Si solo hay un certificado y se ha pedido que se seleccione automáticamente,
			// reenviamos directamente a la operación de firma. Debemos entonces establecer
			// ese certifiado y la URL a la que redirigir en caso de error
			if (certificates.length == 1 && skipSelection) {
				try {
					request.setAttribute(ServiceParams.HTTP_ATTR_CERT, Base64.encode(certificates[0].getEncoded(), true));
					request.setAttribute(ServiceParams.HTTP_ATTR_ERROR_URL, connConfig.getRedirectErrorUrl());
				} catch (final Exception e) {
					LOGGER.log(Level.SEVERE, logF.f("Error al codificar el certificado en Base64"), e); //$NON-NLS-1$
					ErrorManager.setErrorToSession(session, FIReError.SIGNING, false, trAux);
					redirectToErrorPage(forcedProvider, connConfig, request, response, trAux);
					return;
				}
				redirectUrl = ServiceNames.PUBLIC_SERVICE_PRESIGN;
			} else {
				// Adjuntamos los certificados a la sesion
				session.setAttribute(trId + "-certs", certificates); //$NON-NLS-1$
				redirectUrl = FirePages.PG_CHOOSE_CERTIFICATE;
			}
		}

		// Guardamos en la sesion compartida los datos agregados hasta ahora
		session.saveIntoHttpSession(request.getSession());
		SessionCollector.commit(session, trAux);

		Responser.redirectToUrl(redirectUrl, request, response, trAux);
	}

	/**
	 * Obtiene los certificados de firma de un proveedor de firma en la nube.
	 * @param providerName Nombre del proveedor de firma en la nube.
	 * @param subjectId    Identificador del usuario.
	 * @param connConfig   Configuraci&oacute;n de la conexion con FIRe.
	 *                     concreto.
	 * @param trAux        Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 * @throws BusinessException Cuando se produce algun error controlado en el proceso.
	 */
	private static X509Certificate[] getCertificates(final String providerName, final String subjectId,
			final TransactionConfig connConfig, final TransactionAuxParams trAux) throws BusinessException {

		final LogTransactionFormatter logF = trAux.getLogFormatter();

		// Listamos los certificados del usuario
		X509Certificate[] certificates = null;
		FIReConnector connector;
		try {
			connector = ProviderManager.getProviderConnector(providerName, connConfig.getProperties(), logF);
			LOGGER.info(logF.f("Se ha cargado el conector " + connector.getClass().getName())); //$NON-NLS-1$
			certificates = connector.getCertificates(subjectId);
		} catch (final FIReConnectorFactoryException e) {
			LOGGER.log(Level.SEVERE,
					logF.f("No se ha podido cargar el conector del proveedor de firma: " + LogUtils.cleanText(providerName)), e); //$NON-NLS-1$
			throw new BusinessException(FIReError.INTERNAL_ERROR);
		} catch (final FIReCertificateException e) {
			LOGGER.log(Level.SEVERE, logF.f("No se han podido recuperar los certificados del usuario " + LogUtils.cleanText(subjectId)), e); //$NON-NLS-1$
			throw new BusinessException(FIReError.CERTIFICATE_ERROR);
		} catch (final FIReConnectorNetworkException e) {
			LOGGER.log(Level.SEVERE, logF.f("No se ha podido conectar con el proveedor de firma en la nube"), e); //$NON-NLS-1$
			AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
			throw new BusinessException(FIReError.PROVIDER_INACCESIBLE_SERVICE);
		} catch (final CertificateBlockedException e) {
			LOGGER.log(Level.WARNING, logF.f("Los certificados del usuario %1s estan bloqueados: ", LogUtils.cleanText(subjectId)) + e); //$NON-NLS-1$
			throw new BusinessException(FIReError.CERTIFICATE_BLOCKED);
		} catch (final WeakRegistryException e) {
			LOGGER.log(Level.WARNING, logF.f("El usuario %1s realizo un registro debil: ", LogUtils.cleanText(subjectId)) + e); //$NON-NLS-1$
			throw new BusinessException(FIReError.CERTIFICATE_WEAK_REGISTRY);
		} catch (final FIReConnectorUnknownUserException e) {
			LOGGER.log(Level.WARNING, logF.f("El usuario %1s no esta registrado en el sistema: ", LogUtils.cleanText(subjectId)) + e); //$NON-NLS-1$
			throw new BusinessException(FIReError.UNKNOWN_USER);
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE,
					logF.f("Error indeterminado al recuperar los certificados del usuario " + subjectId), e); //$NON-NLS-1$
			throw new BusinessException(FIReError.PROVIDER_ERROR);
		} catch (final Error e) {
			LOGGER.log(Level.SEVERE,
					logF.f("Error grave, probablemente relacionado con la inicializacion del conector"), e); //$NON-NLS-1$
			throw new BusinessException(FIReError.INTERNAL_ERROR);
		}

		// Si no se recuperan certificados ni se pueden generar, se lanza una excepcion
		if ((certificates == null || certificates.length == 0) && !connector.allowRequestNewCerts()) {
			LOGGER.log(Level.WARNING,
					logF.f("El usuario no dispone de certificados y el conector no le permite generarlos")); //$NON-NLS-1$
			throw new BusinessException(FIReError.CERTIFICATE_NO_CERTS);
		}
		return certificates;
	}

	static void preprocessSignWithCloudProvider(final String provName, final String trId,
			final FireSession session, final HttpServletRequest request, final HttpServletResponse response,
			final TransactionAuxParams trAux) {

		final String subjectId = session.getString(ServiceParams.SESSION_PARAM_SUBJECT_ID);
		final TransactionConfig connConfig = (TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);

		String redirectUrl;

		final String baseUrl = PublicContext.getPublicContext(request);
		try {
			redirectUrl = ProviderBusiness.getUserPreprocess(provName, subjectId,
					trId, connConfig, session, baseUrl, trAux);
		}
		catch (final BusinessException e) {
			ErrorManager.setErrorToSession(session, e.getFireError(), true, trAux);
			redirectToErrorPage(true, connConfig, request, response, trAux);
			return;
		}

		// Registramos los datos guardados
		session.saveIntoHttpSession(request.getSession());
		SessionCollector.commit(session, trAux);

		Responser.redirectToExternalUrl(redirectUrl, request, response, trAux);
	}

	/**
	 * Redirige el flujo de ejecuci&oacute;n para que el proveedor de firma en la nube
	 * preprocese la operaci&oacute;n autenticando al usuario, seleccionando los certificados
	 * desde su p&aacute;gina o lo que sea necesario.
	 *
	 * @param provName	   Nombre del proveedor de firma en la nube.
	 * @param subjectId    Identificador del usuario.
	 * @param trId         Identificador de la transacci&oacute;n,.
	 * @param connConfig   Configuraci&oacute;n de la conexion con FIRe.
	 *                     concreto.
	 * @param session	   Configuraci&oacute;n de la sesi&oacute;n con los datos de la transacci&oacute;n.
	 * @para baseUrl	   Fragmento base de la URL del servicio de FIRe.
	 * @param trAux        Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 */
	private static String getUserPreprocess(final String provName, final String subjectId,
			final String trId, final TransactionConfig connConfig, final FireSession session,
			final String baseUrl, final TransactionAuxParams trAux) throws BusinessException {

		final LogTransactionFormatter logF = trAux.getLogFormatter();

		// Listamos los certificados del usuario
		String providerUrl;
		try {
			final FIReConnector connector = ProviderManager.getProviderConnector(provName, connConfig.getProperties(), logF);
			LOGGER.info(logF.f("Se ha cargado el conector " + connector.getClass().getName())); //$NON-NLS-1$

			final String subjectRef = session.getString(ServiceParams.SESSION_PARAM_SUBJECT_REF);
			final String errorUrl = URLEncoder.encode(connConfig.getRedirectErrorUrl(), "utf-8"); //$NON-NLS-1$

			final String okRedirectUrl = baseUrl
					+ ServiceNames.PUBLIC_SERVICE_AUTH_USER
					+ "?" + ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + trId //$NON-NLS-1$ //$NON-NLS-2$
					+ "&" + ServiceParams.HTTP_PARAM_SUBJECT_REF + "=" + subjectRef //$NON-NLS-1$ //$NON-NLS-2$
					+ "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + errorUrl; //$NON-NLS-1$ //$NON-NLS-2$

			final String errorRedirectUrl = baseUrl
					+ ServiceNames.PUBLIC_SERVICE_EXTERNAL_ERROR
					+ "?" + ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + trId //$NON-NLS-1$ //$NON-NLS-2$
					+ "&" + ServiceParams.HTTP_PARAM_SUBJECT_REF + "=" + subjectRef //$NON-NLS-1$ //$NON-NLS-2$
					+ "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + errorUrl; //$NON-NLS-1$ //$NON-NLS-2$

			providerUrl = connector.userAutentication(subjectId, okRedirectUrl, errorRedirectUrl);
		} catch (final FIReConnectorFactoryException e) {
			LOGGER.log(Level.SEVERE,
					logF.f("No se ha podido cargar el conector del proveedor de firma: %1s", provName), e); //$NON-NLS-1$
			throw new BusinessException(FIReError.INTERNAL_ERROR);
		} catch (final FIReConnectorNetworkException e) {
			LOGGER.log(Level.SEVERE, logF.f("No se ha podido conectar con el proveedor de firma en la nube"), e); //$NON-NLS-1$
			AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, provName);
			throw new BusinessException(FIReError.PROVIDER_INACCESIBLE_SERVICE);
		} catch (final FIReConnectorUnknownUserException e) {
			LOGGER.log(Level.WARNING, logF.f("El usuario %1s no esta registrado en el sistema: %2s", subjectId, e)); //$NON-NLS-1$
			throw new BusinessException(FIReError.UNKNOWN_USER);
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE,
					logF.f("Error indeterminado al autenticar al usuario %1s en el proveedor", subjectId), e); //$NON-NLS-1$
			throw new BusinessException(FIReError.PROVIDER_ERROR);
		} catch (final Error e) {
			LOGGER.log(Level.SEVERE,
					logF.f("Error grave, probablemente relacionado con la inicializacion del conector"), e); //$NON-NLS-1$
			throw new BusinessException(FIReError.INTERNAL_ERROR);
		}

		return providerUrl;
	}

	/**
	 * Redirige a una p&aacute;gina de error. La p&aacute;gina sera de de error de
	 * firma, si existe la posibilidad de
	 * que se pueda reintentar la operaci&oacute;n, o la p&aacute;gina de error
	 * proporcionada por el usuario.
	 *
	 * @param forcedProvider Indica si era obligatorio el uso de un proveedor de firma
	 *                     concreto.
	 * @param connConfig   Configuraci&oacute;n de la transacci&oacute;n.
	 * @param request      Objeto de petici&oacute;n al servlet.
	 * @param response     Objeto de respuesta del servlet.
	 * @param trAux        Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 */
	private static void redirectToErrorPage(final boolean forcedProvider, final TransactionConfig connConfig,
			final HttpServletRequest request, final HttpServletResponse response, final TransactionAuxParams trAux) {
		if (forcedProvider) {
			Responser.redirectToExternalUrl(connConfig.getRedirectErrorUrl(), request, response, trAux);
		} else {
			Responser.redirectToUrl(FirePages.PG_SIGNATURE_ERROR, request, response, trAux);
		}
	}
}
