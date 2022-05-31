package es.gob.fire.upgrade.afirma.ws;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.SAXException;

/**
 * Constructor de objetos para la carga de docuemntos XML.
 */
public class SecureXmlBuilder {

	private static final Logger LOGGER = Logger.getLogger(SecureXmlBuilder.class.getName());

    private static DocumentBuilderFactory SECURE_BUILDER_FACTORY = null;

    private static SAXParserFactory SAX_FACTORY = null;

	private static TransformerFactory TRANSFORMER_FACTORY = null;

	/**
	 * Obtiene un generador de &aacute;boles DOM con el que crear o cargar un XML.
	 * @return Generador de &aacute;rboles DOM.
	 * @throws ParserConfigurationException Cuando ocurre un error durante la creaci&oacute;n.
	 */
	public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		if (SECURE_BUILDER_FACTORY == null) {
			SECURE_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
			try {
				SECURE_BUILDER_FACTORY.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE.booleanValue());
			}
			catch (final Exception e) {
				LOGGER.log(Level.WARNING, "No se ha podido establecer el procesado seguro en la factoria XML: " + e); //$NON-NLS-1$
			}

			// Los siguientes atributos deberia establececerlos automaticamente la implementacion de
			// la biblioteca al habilitar la caracteristica anterior. Por si acaso, los establecemos
			// expresamente
			final String[] securityProperties = new String[] {
					javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD,
					javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA,
					javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET
			};
			for (final String securityProperty : securityProperties) {
				try {
					SECURE_BUILDER_FACTORY.setAttribute(securityProperty, ""); //$NON-NLS-1$
				}
				catch (final Exception e) {
					// Podemos las trazas en debug ya que estas propiedades son adicionales
					// a la activacion de el procesado seguro
					LOGGER.log(Level.FINE, "No se ha podido establecer una propiedad de seguridad '" + securityProperty + "' en la factoria XML"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			SECURE_BUILDER_FACTORY.setValidating(false);
			SECURE_BUILDER_FACTORY.setNamespaceAware(true);
		}
		return SECURE_BUILDER_FACTORY.newDocumentBuilder();
	}

	/**
     * Construye un parser SAX seguro que no accede a recursos externos.
     * @return Factor&iacute;a segura.
	 * @throws SAXException Cuando ocurre un error de SAX.
	 * @throws ParserConfigurationException Cuando no se puede crear el parser.
     */
	public static SAXParser getSAXParser() throws ParserConfigurationException, SAXException {
		if (SAX_FACTORY == null) {
			SAX_FACTORY = SAXParserFactory.newInstance();
			try {
				SAX_FACTORY.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE.booleanValue());
			}
			catch (final Exception e) {
				LOGGER.log(
						Level.SEVERE,
						"No se ha podido establecer una caracteristica de seguridad en la factoria XML: " + e); //$NON-NLS-1$
			}

			// Desactivamos las caracteristicas que permiten la carga de elementos externos
			try {
				SAX_FACTORY.setFeature("http://xml.org/sax/features/external-general-entities", false); //$NON-NLS-1$
				SAX_FACTORY.setFeature("http://xml.org/sax/features/external-parameter-entities", false); //$NON-NLS-1$
			}
			catch (final Exception e) {
				// Podemos las trazas en debug ya que estas propiedades son adicionales
				// a la activacion de el procesado seguro
				LOGGER.log(
						Level.FINE,
						"No se ha podido establecer una caracteristica de seguridad en la factoria SAX XML: " + e); //$NON-NLS-1$
			}

			SAX_FACTORY.setValidating(false);
			SAX_FACTORY.setNamespaceAware(true);
		}
		return SAX_FACTORY.newSAXParser();
	}

	/**
	 * Obtiene un transformador de &aacute;boles DOM con el que crear o cargar un XML.
	 * @return Transformador de &aacute;rboles DOM.
	 * @throws TransformerConfigurationException Error al crear el transformador.
	 */
	public static Transformer getSecureTransformer() throws TransformerConfigurationException {
		if (TRANSFORMER_FACTORY == null) {
			TRANSFORMER_FACTORY = TransformerFactory.newInstance();
			try {
				TRANSFORMER_FACTORY.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE.booleanValue());
				TRANSFORMER_FACTORY.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); //$NON-NLS-1$
				TRANSFORMER_FACTORY.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, ""); //$NON-NLS-1$
			}
			catch (final Exception e) {
				LOGGER.log(Level.WARNING, "No se ha podido establecer el procesado seguro en la factoria XML: " + e); //$NON-NLS-1$
			}

		}
		return TRANSFORMER_FACTORY.newTransformer();
	}
}
