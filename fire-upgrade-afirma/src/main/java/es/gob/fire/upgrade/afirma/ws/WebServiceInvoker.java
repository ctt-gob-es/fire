// Copyright (C) 2020 MINHAP, Gobierno de Espana
// This program is licensed and may be used, modified and redistributed under the terms
// of the European Public License (EUPL), either version 1.1 or (at your
// option) any later version as soon as they are approved by the European Commission.
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// or implied. See the License for the specific language governing permissions and
// more details.
// You should have received a copy of the EUPL1.1 license
// along with this program; if not, you may find it at
// http://joinup.ec.europa.eu/software/page/eupl/licence-eupl

/**
 * Class that manages the invoke of @Firma and eVisor web services.
 * @author Gobierno de Espana.
 */
package es.gob.fire.upgrade.afirma.ws;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.transport.http.HTTPConstants;

/**
 * <p>Class that manages the invoke of @Firma and eVisor web services.</p>
 * <b>Project:</b><p>Library for the integration with the services of @Firma, eVisor and TS@.</p>
 * @version 1.5, 22/06/2020.
 */
public class WebServiceInvoker {

	/**
	 * Object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(WebServiceInvoker.class.getName());

	/**
	 * Security Axis2 phase name.
	 */
	private static final String PHASE_NAME_SECURITY = "Security"; //$NON-NLS-1$

	/**
	 * Properties defined on the configuration file.
	 */
	private final WebServiceInvokerConfig config;

	/**
	 * Certificate used to sign the WebServices response.
	 */
	private X509Certificate signingCert;

	/**
	 * List of handlers added to the Axis engine.
	 */
	private static List<String> handlerAdded = new ArrayList<>();

	/**
	 * Constructor method.
	 * @param config Configuration object with the parameters to connect to the web services.
	 */
	public WebServiceInvoker(final WebServiceInvokerConfig config) {
		this.config = config;

		final String tsPath = this.config.getSigningCertStorePath();
		final String tsPass = this.config.getSigningCertStorePass();
		final String tsType = this.config.getSigningCertStoreType();
		final String tsAlias = this.config.getSigningCertAlias();
		if (tsPath == null || tsPass == null || tsType == null || tsAlias == null) {
			LOGGER.fine("No se creara manejador para las respuestas firmadas."); //$NON-NLS-1$
		}
		else {
			LOGGER.fine("Configurando el manejador para las respuestas firmadas.\nAlmac\u00E9n de certificados [" //$NON-NLS-1$
				+ tsPath + "]. Tipo [" + tsType + "]. Alias de certificado [" + tsAlias + "]."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			try {
				this.signingCert = loadCert(tsPath, tsPass, tsType, tsAlias);
			} catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "No se ha podido cargar el certificado para la validacion de las respuestas del servicio @firma", e); //$NON-NLS-1$
			}
		}
	}


	/**
	 * Cargar un certificado.
	 * @param signingCertStorePath Ruta del almac&eacute;n de confianza.
	 * @param signingCertStorePass Contrase&ntilde;a del almac&eacute;n de confianza.
	 * @param signingCertStoreType Tipo de almac&eacute;n de confianza.
	 * @param signingCertAlias Alias del certificado con el que debe estar firmada la respuesta.
	 * @throws AxisFault Cuando no se puede cargar el certificado para la validaci&oacute;n de la
	 * respuesta.
	 */
	private static X509Certificate loadCert(final String signingCertStorePath, final String signingCertStorePass, final String signingCertStoreType, final String signingCertAlias) throws AxisFault {

		X509Certificate cert;
		try (InputStream is = new FileInputStream(signingCertStorePath);) {
			final KeyStore ks = KeyStore.getInstance(signingCertStoreType);
			ks.load(is, signingCertStorePass.toCharArray());
			cert = (X509Certificate) ks.getCertificate(signingCertAlias);
			if (cert == null) {
				throw new KeyStoreException("No se ha encontrado en el almacen de certificados"); //$NON-NLS-1$
			}
		}
		catch (final Exception e) {
			throw new AxisFault("Error al recuperar el certificado", e); //$NON-NLS-1$
		}
		return cert;
	}

	/**
	 * Method that performs the invocation to a method from @Firma web services.
	 * @param reqBody Request body XML.
	 * @param service Parameter that represents the name of service.
	 * @param method Parameter that represents the name of the method to invoke.
	 * @return the response of the web service.
	 * @throws WSServiceInvokerException If the method fails.
	 */
	public final Object performCall(final String reqBody, final String service, final String method) throws WSServiceInvokerException {

		Object res = null;
		ServiceClient client = null;
		try {
			final String endPoint = this.config.getEndpoint();

			final String serviceUrl = endPoint
					+ (endPoint.endsWith("/") ? "" : "/") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ service;

			final String securityOption = this.config.getAuthMethod();
			final ClientHandler requestHandler = newRequestHandler(securityOption);
			ResponseHandler responseHandler = null;
			if (this.signingCert != null) {
				responseHandler = new ResponseHandler(this.signingCert);
			}

			LOGGER.fine("Metodo a invocar: " + method); //$NON-NLS-1$

			// Creamos la factoria de objetos XML de AXIS2.
			final OMFactory fac = OMAbstractFactory.getOMFactory();

			// Creamos el namespace de la peticion.
			final OMNamespace ns = createNamespace(fac, service);
			// Creamos el elemento XML raiz del SOAP body que indica la
			// operacion a realizar.
			final OMElement operationElem = fac.createOMElement(method, ns);
			// Creamos el elemento XML que contendra la peticion SOAP completa.
			final OMElement inputParamElem = fac.createOMElement("arg0", ns); //$NON-NLS-1$
			// Anadimos la peticion al parametro de entrada principal.
			inputParamElem.setText(reqBody);
			// Incluimos el parametro a la operacion para formar el body del
			// SOAP
			// completamente.
			operationElem.addChild(inputParamElem);

			// Creamos un objeto Option que albergara la configuracion de
			// conexion al servicio.
			final Options options = new Options();
			options.setTimeOutInMilliSeconds(this.config.getTimeout());
			options.setTo(new EndpointReference(serviceUrl));

			// Desactivamos el chunked.
			options.setProperty(HTTPConstants.CHUNKED, Boolean.FALSE.toString().toLowerCase());

			//TODO: Buscar un modo de poder asignar el almacen de confianza exclusivamente para esta conexion
			// y no como configuracion generar de la JVM
			if (this.config.getTruststorePath() != null) {
				System.setProperty("javax.net.ssl.trustStore", this.config.getTruststorePath()); //$NON-NLS-1$
			}
			if (this.config.getTruststorePass() != null) {
				System.setProperty("javax.net.ssl.trustStorePassword", this.config.getTruststorePass()); //$NON-NLS-1$
			}
			if (this.config.getTruststoreType() != null) {
				System.setProperty("javax.net.ssl.trustStoreType", this.config.getTruststoreType()); //$NON-NLS-1$
			}

			// Creamos el cliente y le anadimos la configuracion anterior.
			client = new ServiceClient();
			client.setOptions(options);

			// Anadimos los handler generados al flujo de handlers de Axis2.
			addHandlers(client, requestHandler, responseHandler);

			// Realizamos la llamada.
			LOGGER.fine("Realizando la llamada al servicio web..."); //$NON-NLS-1$
			final OMElement result = client.sendReceive(operationElem);
			if (result != null && result.getFirstElement() != null && !result.getFirstElement().getText().isEmpty()) {
				res = result.getFirstElement().getText();
			}
		} catch (final WSServiceInvokerException e) {
			throw e;
		} catch (final Exception e) {
			throw new WSServiceInvokerException(e);
		} finally {
			removeHandlers(client);
		}

		return res;
	}

	/**
	 * Auxiliary method that adds the generated handlers to the 'phases' of Axis2.
	 * @param client Service client.
	 * @param requestHandler Request handler.
	 * @param responseHandler Response handler.
	 */
	private static void addHandlers(final ServiceClient client, final ClientHandler requestHandler, final ResponseHandler responseHandler) {

		// Anadimos el handler de seguridad de salida.
		final AxisConfiguration config = client.getAxisConfiguration();
		final List<Phase> phasesOut = config.getOutFlowPhases();
		for (final Phase phase: phasesOut) {
			if (PHASE_NAME_SECURITY.equals(phase.getPhaseName())) {
				try {
					addHandler(phase, requestHandler, 2);
					break;
				} catch (final PhaseException e) {
					LOGGER.log(Level.SEVERE, "Ha ocurrido un error al anadir el handler de seguridad de salida. Excepcion: " + e); //$NON-NLS-1$
				}
			}
		}

		// Anadimos el handler de seguridad de entrada.
		if (responseHandler != null) {
			final List<Phase> phasesIn = config.getInFlowPhases();
			for (final Phase phase: phasesIn) {
				if (PHASE_NAME_SECURITY.equals(phase.getPhaseName())) {
					try {
						addHandler(phase, responseHandler, 2);
						break;
					} catch (final PhaseException e) {
						LOGGER.log(Level.SEVERE, "Ha ocurrido un error al anadir el handler de seguridad de entrada. Excepcion: " + e); //$NON-NLS-1$
					}
				}
			}
		}
	}

	/**
	 * Method that removes the added handler from the axis engine.
	 * @param client Axis service client.
	 */
	private static void removeHandlers(final ServiceClient client) {
		if (client != null && !handlerAdded.isEmpty()) {
			final AxisConfiguration config = client.getAxisConfiguration();

			// Recorremos las phases de salida.
			final List<Phase> phasesOut = config.getOutFlowPhases();
			for (final Phase phase: phasesOut) {
				removeHandler(phase);
			}

			// Recorremos las phases de entrada.
			final List<Phase> phasesIn = config.getInFlowPhases();
			for (final Phase phase: phasesIn) {
				removeHandler(phase);
			}

			// Reiniciamos la lista de handlers.
			handlerAdded = new ArrayList<>();
		}

	}

	/**
	 * Auxiliary method that removes the added handler from the given phase.
	 * @param phase Axis phase where the handlers are.
	 */
	private static void removeHandler(final Phase phase) {
		if (phase != null) {
			final List<Handler> handlers = phase.getHandlers();
			for (final Handler handler: handlers) {
				if (handlerAdded.contains(handler.getName())) {
					handler.getHandlerDesc().setHandler(handler);
					phase.removeHandler(handler.getHandlerDesc());
				}
			}
		}
	}

	/**
	 * Auxiliary method that add a handler into an AXIS2 phase.
	 * @param phase AXIS2 phase.
	 * @param handler Handler to add.
	 * @param position Indicates if the handler is added in the first place of the list (0), at the end (2) or is indifferent (1).
	 * @throws PhaseException if it is not possible to add the handler to the phase.
	 */
	private static void addHandler(final Phase phase, final Handler handler, final int position) throws PhaseException {
		if (position == 0 && !isHandlerInPhase(phase, handler)) {
			phase.setPhaseFirst(handler);
			handlerAdded.add(handler.getName());
			return;
		}
		if (position == 1 && !isHandlerInPhase(phase, handler)) {
			phase.addHandler(handler);
			handlerAdded.add(handler.getName());
			return;
		}
		if (position == 2 && !isHandlerInPhase(phase, handler)) {
			phase.setPhaseLast(handler);
			handlerAdded.add(handler.getName());
			return;
		}
	}

	/**
	 * Method that creates a new instance of {@link ClientHandler}.
	 * @param securityOption Parameter that represents the security options.
	 * @return the created instance of {@link ClientHandler}.
	 * @throws WSServiceInvokerException If the method fails.
	 */
	private ClientHandler newRequestHandler(final String securityOption) throws WSServiceInvokerException {

		ClientHandler sender;
		if (ClientHandler.USERNAMEOPTION.equalsIgnoreCase(securityOption)) {
			sender = new ClientHandler(ClientHandler.USERNAMEOPTION);
			sender.setUserAlias(this.config.getUserName());
			sender.setPassword(this.config.getUserPass());
			sender.setPasswordType(this.config.getUserPassType());
		}
		else if (ClientHandler.CERTIFICATEOPTION.equalsIgnoreCase(securityOption)) {
			sender = new ClientHandler(ClientHandler.CERTIFICATEOPTION);
			sender.setKeystore(this.config.getKeystorePath());
			sender.setKeystorePass(this.config.getKeystorePass());
			sender.setKeystoreType(this.config.getKeystoreType());
			sender.setUserAlias(this.config.getKeystoreCertAlias());
			sender.setPassword(this.config.getKeystoreCertPass());
		}
		else {
			sender = new ClientHandler(ClientHandler.NONEOPTION);
		}

		return sender;
	}

	/**
	 * Auxiliary method that create the specific namespace for the specific service.
	 * @param fac OM factory.
	 * @param afirmaService service name.
	 * @return the target namespace of the service.
	 */
	private static OMNamespace createNamespace(final OMFactory fac, final String method) {
		return fac.createOMNamespace("http://afirmaws/services/" + method, "ns1"); //$NON-NLS-1$ //$NON-NLS-2$
	}

    /**
     * Method that checks if the handlers is already included in the phase.
     * @param phase axis2 phase.
     * @param handler handler to check.
     * @return <i>true</i> if the handler is already in the handler and <i>false</i> if not.
     */
	public static boolean isHandlerInPhase(final Phase phase, final Handler handler) {
		boolean res = false;
		final List<Handler> handlers = phase.getHandlers();
		for (final Handler h: handlers) {
			if (h.getClass().equals(handler.getClass())) {
				if (h.getName().equals(handler.getName())) {
					res = true;
					break;
				}
			}
		}
		return res;
	}
}
