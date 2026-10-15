package es.gob.fire.upgrade.afirma.ws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.axis2.AxisFault;
import org.apache.axiom.soap.SOAPEnvelope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility methods for converting Axis2 SOAP envelopes into DOM documents.
 */
public class SoapDocumentUtil {

    private SoapDocumentUtil() {
        // Utility class.
    }

    /**
     * Obtains the DOM document associated with an Axis2 SOAP envelope.
     *
     * <p>The envelope is serialized and parsed with the secure XML builder so
     * that WSS4J always receives a fully built, namespace-aware DOM.</p>
     *
     * @param envelope Axis2 SOAP envelope to convert.
     * @return DOM document representing the SOAP envelope.
     * @throws AxisFault if the envelope is null or cannot be converted to DOM.
     */
    public static Document getDocument(final SOAPEnvelope envelope) throws AxisFault {
        if (envelope == null) {
            throw new AxisFault("El envelope SOAP de Axis2 es nulo"); //$NON-NLS-1$
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            envelope.serialize(output);
            final Document document = SecureXmlBuilder.getDocumentBuilder()
                    .parse(new ByteArrayInputStream(output.toByteArray()));
            if (isUsable(document)) {
                return document;
            }
        } catch (final Exception e) {
            throw new AxisFault("No se pudo convertir el envelope SOAP a DOM", e); //$NON-NLS-1$
        }
        throw new AxisFault("No se pudo convertir el envelope SOAP a DOM"); //$NON-NLS-1$
    }

    private static boolean isUsable(final Document document) {
        if (document == null) {
            return false;
        }

        final Element root = document.getDocumentElement();
        return root != null
                && "Envelope".equals(root.getLocalName())
                && root.getNamespaceURI() != null
                && document.getElementsByTagNameNS(root.getNamespaceURI(), "Body").getLength() == 1;
    }
}