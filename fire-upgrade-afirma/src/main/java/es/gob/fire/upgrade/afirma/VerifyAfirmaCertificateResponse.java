package es.gob.fire.upgrade.afirma;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import es.gob.fire.upgrade.afirma.ws.SecureXmlBuilder;

/**
 * Respuesta del servicio para verificar un certificado @firma.
 */
public class VerifyAfirmaCertificateResponse {

	private static final String RESULT_MINOR_DEFINITIVE = "Definitive"; //$NON-NLS-1$
	private static final String RESULT_MINOR_BADCERTIFICATEFORMAT = "BadCertificateFormat";
	private static final String RESULT_MINOR_TEMPORAL = "Temporal";
	private static final String RESULT_MINOR_ONHOLD = "OnHold";
	private static final String RESULT_MINOR_REVOKED = "Revoked";
	private static final String RESULT_MINOR_EXPIRED = "Expired";
	private static final String RESULT_MINOR_NOTYEDVALID = "NotYetValid";
	private static final String RESULT_MINOR_PATHVALIDATIONFAILS = "PathValidationFails";
	private static final String RESULT_MINOR_BADCERTIFICATESIGNATURE = "BadCertificateSignature";
	private static final String RESULT_MINOR_REVOKEDWITHOUTTST = "RevokedWithoutTST";
	private static final String RESULT_MAJOR_SUCCESS = "Success";
	
	private final boolean definitive;
	private final boolean badCertificateFormat;
	private final boolean temporal;
	private final boolean onHold;
	private final boolean revoked;
	private final boolean expired;
	private final boolean notYetValid;
	private final boolean pathValidationFails;
	private final boolean badCertificateSignature;
	private final boolean revokedWithoutTST;
	private final boolean success;

    private String majorCode = null;
    private String minorCode = null;
    private String description = null;

	/**
	 * Construye el objeto de respuesta en base a los datos remitidos por la
	 * Plataforma @firma.
	 * @param xml XML de respuesta del metodo de validaci&oacute;n de firma de
	 * de la Plataforma @firma.
	 * @throws SAXException Cuando no se recibe un XML v&aacute;lido.
	 * @throws ParserConfigurationException Cuando el parser XML no esta correctamente configurado.
	 * @throws IOException Cuando se produce un error de lectura del XML.
	 */
	public VerifyAfirmaCertificateResponse(final byte[] xml) throws ParserConfigurationException, SAXException, IOException {

		if (xml == null) {
            throw new IllegalArgumentException(
                    "El XML de entrada no puede ser nulo" //$NON-NLS-1$
            );
        }

        final SAXParser saxParser = SecureXmlBuilder.getSAXParser();
        final XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(new CustomHandler(this));
        xmlReader.parse(new InputSource(new ByteArrayInputStream(xml)));

        this.definitive = RESULT_MINOR_DEFINITIVE.equals(this.minorCode);
        this.badCertificateFormat = RESULT_MINOR_BADCERTIFICATEFORMAT.equals(this.minorCode);
        this.temporal = RESULT_MINOR_TEMPORAL.equals(this.minorCode);
        this.onHold = RESULT_MINOR_ONHOLD.equals(this.minorCode);
        this.revoked = RESULT_MINOR_REVOKED.equals(this.minorCode);
        this.expired = RESULT_MINOR_EXPIRED.equals(this.minorCode);
        this.notYetValid = RESULT_MINOR_NOTYEDVALID.equals(this.minorCode);
        this.pathValidationFails = RESULT_MINOR_PATHVALIDATIONFAILS.equals(this.minorCode);
        this.badCertificateSignature = RESULT_MINOR_BADCERTIFICATESIGNATURE.equals(this.minorCode);
        this.revokedWithoutTST = RESULT_MINOR_REVOKEDWITHOUTTST.equals(this.minorCode);
        this.success = RESULT_MAJOR_SUCCESS.equals(this.majorCode);
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

	public boolean isDefinitive() {
		return this.definitive;
	}

	public boolean isBadCertificateFormat() {
		return badCertificateFormat;
	}

	public boolean isTemporal() {
		return temporal;
	}

	public boolean isOnHold() {
		return onHold;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public boolean isExpired() {
		return expired;
	}

	public boolean isNotYetValid() {
		return notYetValid;
	}

	public boolean isPathValidationFails() {
		return pathValidationFails;
	}

	public boolean isBadCertificateSignature() {
		return badCertificateSignature;
	}

	public boolean isRevokedWithoutTST() {
		return revokedWithoutTST;
	}
	
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Manejador encargado de procesar el XML de respuesta de validaci&oacute;n de firmas.
	 */
	private static class CustomHandler extends DefaultHandler {

		private static final String TAG_RESULT = "Result"; //$NON-NLS-1$
		private static final String TAG_RESULT_MAJOR = "ResultMajor"; //$NON-NLS-1$
		private static final String TAG_RESULT_MINOR = "ResultMinor"; //$NON-NLS-1$
		private static final String TAG_RESULT_MESSAGE = "ResultMessage"; //$NON-NLS-1$

		private final VerifyAfirmaCertificateResponse verifyResponse;

		private boolean resultProcessed = false;
		private boolean overResultMajor = false;
		private boolean overResultMinor = false;
		private boolean overResultMessage = false;

		CustomHandler(final VerifyAfirmaCertificateResponse vr) {
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
				this.verifyResponse.setMinorCode(DssServicesUtils.cleanDssResult(value));
				this.overResultMinor = false;
			} else if (this.overResultMessage) {
				this.verifyResponse.setDescription(value);
				this.overResultMessage = false;
			}
		}
	}
}
