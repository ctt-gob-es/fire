package es.gob.fire.server.services.internal;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * M&eacute;todos de utilidad con los que verificar datos y evitar vulnerabilidades.
 */
public class SecurityUtils {

	private static final Logger LOGGER = Logger.getLogger(SecurityUtils.class.getName());

    private static DocumentBuilderFactory SECURE_BUILDER_FACTORY = null;

    /**
     * Obtiene una factor&iacute;a configurada para la creaci&oacute;n de DocumentBuilder
     * preparados para cargar documentos XML que podr&iacute;an ser inseguros. Los cargar&aacute;
     * sin validarlos ni cargar referencias externas.
     * @return Factor&iacute;a para la carga de documentos XML.
     * @throws ParserConfigurationException Cuando no se pueda crear el constructor de documentos.
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
}
