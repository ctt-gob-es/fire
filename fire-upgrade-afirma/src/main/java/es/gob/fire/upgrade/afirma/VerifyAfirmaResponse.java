package es.gob.fire.upgrade.afirma;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Respuesta del servicio de validaci&oacute;n de firma.
 */
public class VerifyAfirmaResponse {

	private static final String RESULT_MAJOR_VALID_SIGNATURE = "ValidSignature"; //$NON-NLS-1$

	private final boolean ok;

    private String majorCode = null;
    private String minorCode = null;
    private String description = null;

	/**
	 * Construye el objeto de respuesta en base a los datos remitidos por la
	 * Plataforma @firma.
	 * @param response Respuesta del metodo de validaci&oacute;n de firma de
	 * de la Plataforma @firma.
	 * @throws SAXException Cuando no se recibe un XML v&aacute;lido.
	 * @throws ParserConfigurationException Cuando el parser XML no esta correctamente configurado.
	 * @throws IOException Cuando se produce un error de lectura del XML.
	 */
	public VerifyAfirmaResponse(final byte[] xml) throws ParserConfigurationException, SAXException, IOException {

		if (xml == null) {
            throw new IllegalArgumentException(
                    "El XML de entrada no puede ser nulo" //$NON-NLS-1$
            );
        }

        final SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        final SAXParser saxParser = spf.newSAXParser();
        final XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(new CustomHandler(this));
        xmlReader.parse(new InputSource(new ByteArrayInputStream(xml)));

        this.ok = RESULT_MAJOR_VALID_SIGNATURE.equals(this.majorCode);
	}

	public String getMajorCode() {
		return this.majorCode;
	}

	public void setMajorCode(final String majorCode) {
		this.majorCode = majorCode;
	}

	public String getMinorCode() {
		return this.minorCode;
	}

	public void setMinorCode(final String minorCode) {
		this.minorCode = minorCode;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}


	/**
	 * Indica si la firma es v&aacute;lida.
	 * @return {@code true} si la firma es v&aacute;lida, {@code false} en caso contrario.
	 */
	public boolean isOk() {
		return this.ok;
	}

	/**
	 * Manejador encargado de procesar el XML de respuesta de validaci&oacute;n de firmas.
	 */
	private static class CustomHandler extends DefaultHandler {

		private static final String TAG_RESULT = "Result"; //$NON-NLS-1$
		private static final String TAG_RESULT_MAJOR = "ResultMajor"; //$NON-NLS-1$
		private static final String TAG_RESULT_MINOR = "ResultMinor"; //$NON-NLS-1$
		private static final String TAG_RESULT_MESSAGE = "ResultMessage"; //$NON-NLS-1$

		private final VerifyAfirmaResponse verifyResponse;

		private boolean resultProcessed = false;
		private boolean overResultMajor = false;
		private boolean overResultMinor = false;
		private boolean overResultMessage = false;

		CustomHandler(final VerifyAfirmaResponse vr) {
			this.verifyResponse = vr;
		}

		@Override
		public void startElement(final String namespaceURI, final String localName,
				final String qName, final Attributes atts) throws SAXException {

			// Una vez hemos recogido el resultado global de la validacion,
			// ignoramos todos los nodos
			if (this.resultProcessed) {
				return;
			}

			if (TAG_RESULT_MAJOR.equals(localName)) {
				this.overResultMajor = true;
				this.overResultMinor = false;
				this.overResultMessage = false;
			} else if (TAG_RESULT_MINOR.equals(localName)) {
				this.overResultMajor = false;
				this.overResultMinor = true;
				this.overResultMessage = false;
			} else if (TAG_RESULT_MESSAGE.equals(localName)) {
				this.overResultMajor = false;
				this.overResultMinor = false;
				this.overResultMessage = true;
			}
		}

		@Override
		public void endElement(final String namespaceURI, final String localName, final String qName)
				throws SAXException {

			if (TAG_RESULT.equals(localName)) {
				this.resultProcessed = true;
			}
		}

		@Override
		public void characters(final char[] ch, final int start, final int length) {

			// Una vez hemos recogido el resultado global de la validacion,
			// ignoramos todos los nodos
			if (this.resultProcessed) {
				return;
			}

			final String value = new String(ch, start, length).trim();

			if (this.overResultMajor) {
				this.verifyResponse.setMajorCode(DssServicesUtils.cleanDssResult(value));
				this.overResultMajor = false;
			} else if (this.overResultMinor) {
				this.verifyResponse.setMinorCode(value);
				this.overResultMinor = false;
			} else if (this.overResultMessage) {
				this.verifyResponse.setDescription(value);
				this.overResultMessage = false;
			}
		}
	}
}
