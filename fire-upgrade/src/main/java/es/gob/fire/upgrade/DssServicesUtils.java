/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.upgrade;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.ws.security.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Utilidades para actualizaci&oacute;n de firmas. */
final class DssServicesUtils {

    private static final Logger LOGGER = Logger.getLogger(DssServicesUtils.class.getName());

    /** Etiqueta de los nodos firma de los XML firmados. */
    private static final String XML_SIGNATURE_TAG = "Signature"; //$NON-NLS-1$

    private static final String AFIRMA_TAG = "AFIRMA"; //$NON-NLS-1$

    /** Constructor privado para no permir la instanciaci&oacute;n. */
    private DssServicesUtils() {
        // no instanciable
    }

    static String createSignUpgradeDss(final byte[] firma,
            final UpgradeTarget formato, final String afirmaAppName) {
        boolean isBinary = false;
        Document firmaXml = null;
        try {
            firmaXml = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(firma));
            LOGGER.info("La firma es XML"); //$NON-NLS-1$
        } catch (final Exception e) {
            isBinary = true;
            LOGGER.info("La firma no es XML: " + e); //$NON-NLS-1$
        }

        // Definimos el tipo de nodo en el que se declara la firma
        String firmaNode;
        if (isBinary) {
            LOGGER.info("La firma es binaria"); //$NON-NLS-1$
            firmaNode = "<dss:Base64Signature>" + Base64.encode(firma) + "</dss:Base64Signature>"; //$NON-NLS-1$ //$NON-NLS-2$
        } else if (firmaXml != null && isEnveloping(firmaXml)) {
            LOGGER.info("La firma es XML enveloping"); //$NON-NLS-1$
            // Quitamos la cabecera XML
            final String encoding = firmaXml.getInputEncoding();
            try {
                firmaNode = encoding != null ? new String(firma, encoding)
                        : new String(firma);
            } catch (final UnsupportedEncodingException e) {
                LOGGER.warning("No se pudo aplicar la codificacion del XML para su conversion a texto, se usara la por defecto: " + e); //$NON-NLS-1$
                firmaNode = new String(firma);
            }
            firmaNode = removeXmlHeader(firmaNode);
        } else {
            LOGGER.info("La firma es XML enveloped o detached"); //$NON-NLS-1$
            firmaNode = "<dss:Base64XML>" + Base64.encode(firma) + "</dss:Base64XML>"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Creamos la peticion segun el tipo de firma que se actualiza
        final StringBuilder dss = new StringBuilder(1000);
        if (isBinary || isEnveloping(firmaXml)) {
            dss.append(
                    "<dss:VerifyRequest Profile='urn:afirma:dss:1.0:profile:XSS' xmlns:dss='urn:oasis:names:tc:dss:1.0:core:schema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='urn:oasis:names:tc:dss:1.0:core:schema http://docs.oasis-open.org/dss/v1.0/oasis-dss-core-schema-v1.0-os.xsd'>") //$NON-NLS-1$
                    .append("<dss:OptionalInputs>") //$NON-NLS-1$
                    .append("<dss:ClaimedIdentity><dss:Name>") //$NON-NLS-1$
                    .append(afirmaAppName)
                    .append("</dss:Name></dss:ClaimedIdentity>") //$NON-NLS-1$
                    .append("<dss:ReturnUpdatedSignature Type='") //$NON-NLS-1$
                    .append(formato.getFormatUrn())
                    .append("'></dss:ReturnUpdatedSignature>") //$NON-NLS-1$
                    .append("</dss:OptionalInputs>") //$NON-NLS-1$
                    .append("<dss:SignatureObject>") //$NON-NLS-1$
                    .append(firmaNode).append("</dss:SignatureObject>") //$NON-NLS-1$
                    .append("</dss:VerifyRequest>"); //$NON-NLS-1$
        } else {
            dss.append(
                    "<dss:VerifyRequest Profile='urn:afirma:dss:1.0:profile:XSS' xmlns:dss='urn:oasis:names:tc:dss:1.0:core:schema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='urn:oasis:names:tc:dss:1.0:core:schema http://docs.oasis-open.org/dss/v1.0/oasis-dss-core-schema-v1.0-os.xsd'>") //$NON-NLS-1$
                    .append("<dss:InputDocuments>") //$NON-NLS-1$
                    .append("<dss:Document ID='1'>") //$NON-NLS-1$
                    .append(firmaNode)
                    .append("</dss:Document>") //$NON-NLS-1$
                    .append("</dss:InputDocuments>") //$NON-NLS-1$
                    .append("<dss:OptionalInputs>") //$NON-NLS-1$
                    .append("<dss:ClaimedIdentity><dss:Name>") //$NON-NLS-1$
                    .append(afirmaAppName)
                    .append("</dss:Name></dss:ClaimedIdentity>") //$NON-NLS-1$
                    .append("<dss:ReturnUpdatedSignature Type='") //$NON-NLS-1$
                    .append(formato.getFormatUrn())
                    .append("'></dss:ReturnUpdatedSignature>") //$NON-NLS-1$
                    .append("</dss:OptionalInputs>") //$NON-NLS-1$
                    .append("<dss:SignatureObject>") //$NON-NLS-1$
                    .append("<dss:SignaturePtr WhichDocument='1'/>") //$NON-NLS-1$
                    .append("</dss:SignatureObject>") //$NON-NLS-1$
                    .append("</dss:VerifyRequest>"); //$NON-NLS-1$
        }

        return dss.toString();
    }

    private static boolean isEnveloping(final Document xml) {
        if (xml == null) {
            return false;
        }
        final Element element = xml.getDocumentElement();
        return XML_SIGNATURE_TAG.equals(element.getNodeName())
                || element.getNodeName().endsWith(":" + XML_SIGNATURE_TAG) //$NON-NLS-1$
                || AFIRMA_TAG.equals(element.getNodeName())
                && (XML_SIGNATURE_TAG.equals(element.getFirstChild()
                        .getNodeName()) || element.getNodeName().endsWith(
                        ":" + XML_SIGNATURE_TAG)); //$NON-NLS-1$
    }

    /**
     * Elimina la cabecera de un XML.
     *
     * @param xml
     *            Documento XML.
     * @return XML sin la cabecera.
     */
    private static String removeXmlHeader(final String xml) {
        int index = xml.indexOf('<');
        if ("<?".equals(xml.substring(index, index + 2))) { //$NON-NLS-1$
            index = xml.indexOf('<', index + 1);
        }
        return xml.substring(index);
    }

    /**
     * Obtiene la clave de un <i>resultMajor</i> o un <i>resultMinor</i> de un
     * mensaje de respuesta DSS, donde la clave viene en el tipo
     * <code>urn:afirma:dss:1.0:profile:XSS:resultminor:Clave</code>.
     *
     * @param result
     *            Mensaje DSS de respuesta.
     * @return Clave del mensaje.
     */
    static String cleanDssResult(final String result) {
        return result != null ? result.substring(result.lastIndexOf(":") + 1) : null; //$NON-NLS-1$
    }

}
