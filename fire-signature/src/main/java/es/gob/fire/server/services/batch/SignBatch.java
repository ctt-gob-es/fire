/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.batch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.gob.afirma.core.signers.TriphaseData;
import es.gob.fire.server.services.internal.SecurityUtils;

/** Lote de firmas electr&oacute;nicas.
 * Un ejemplo de representaci&oacute;n XML de un lote podr&iacute;a ser:
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8" ?&gt;
 * &lt;signbatch stoponerror="false" algorithm="SHA256withRSA" concurrenttimeout="9223372036854775807" Id="LOTE001"&gt;
 *  &lt;singlesign Id="7725374e-728d-4a33-9db9-3a4efea4cead"&gt;
 *   &lt;datasource&gt;http://google.com&lt;/datasource&gt;
 *   &lt;format&gt;XAdES&lt;/format&gt;
 *   &lt;suboperation&gt;sign&lt;/suboperation&gt;
 *   &lt;extraparams&gt;Iw0KI1RodSBKYW4gMTQgMTU6Mzc6MTcgQ0VUIDIwMTYNClNpZ25hdHVyZUlkPTc3MjUzNzRlLTcyOGQtNGEzMy05ZGI5LTNhNGVmZWE0Y2VhZA0K&lt;/extraparams&gt;
 *   &lt;signsaver&gt;
 *    &lt;class&gt;es.gob.afirma.signers.batch.SignSaverFile&lt;/class&gt;
 *    &lt;config&gt;Iw0KI1RodSBKYW4gMTQgMTU6Mzc6MTcgQ0VUIDIwMTYNCkZpbGVOYW1lPUNcOlxcVXNlcnNcXHRvbWFzXFxBcHBEYXRhXFxMb2NhbFxcVGVtcFxcRklSTUExLnhtbA0K&lt;/config&gt;
 *   &lt;/signsaver&gt;
 *  &lt;/singlesign&gt;
 *  &lt;singlesign Id="93d1531c-cd32-4c8e-8cc8-1f1cfe66f64a"&gt;
 *   &lt;datasource&gt;SG9sYSBNdW5kbw==&lt;/datasource&gt;
 *   &lt;format&gt;CAdES&lt;/format&gt;
 *   &lt;suboperation&gt;sign&lt;/suboperation&gt;
 *   &lt;extraparams&gt;Iw0KI1RodSBKYW4gMTQgMTU6Mzc6MTcgQ0VUIDIwMTYNClNpZ25hdHVyZUlkPTkzZDE1MzFjLWNkMzItNGM4ZS04Y2M4LTFmMWNmZTY2ZjY0YQ0K&lt;/extraparams&gt;
 *   &lt;signsaver&gt;
 *    &lt;class&gt;es.gob.afirma.signers.batch.SignSaverFile&lt;/class&gt;
 *    &lt;config&gt;Iw0KI1RodSBKYW4gMTQgMTU6Mzc6MTcgQ0VUIDIwMTYNCkZpbGVOYW1lPUNcOlxcVXNlcnNcXHRvbWFzXFxBcHBEYXRhXFxMb2NhbFxcVGVtcFxcRklSTUEyLnhtbA0K&lt;/config&gt;
 *   &lt;/signsaver&gt;
 *  &lt;/singlesign&gt;
 * &lt;/signbatch&gt;
 * </pre>
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public abstract class SignBatch {

	protected static final Logger LOGGER = Logger.getLogger(SignBatch.class.getName());

	protected final List<SingleSign> signs;
	protected final SingleSignConstants.SignAlgorithm algorithm;

	private String id;
	String getId() {
		return this.id;
	}
	void setId(final String i) {
		if (i != null) {
			this.id = i;
		}
	}

	protected long concurrentTimeout = Long.MAX_VALUE;

	/** Obtiene el algoritmo de firma.
	 * @return Algoritmo de firma. */
	public SingleSignConstants.SignAlgorithm getSignAlgorithm() {
		return this.algorithm;
	}

	protected boolean stopOnError = false;

	/** Crea un lote de firmas a partir de su definici&oacute;n XML.
	 * @param xml XML de definici&oacute;n de lote de firmas (<a href="./doc-files/batch-scheme.html">descripci&oacute;n
	 *            del formato</a>).
	 * @throws IOException Si hay problemas en el tratamiento de datoso en el an&aacute;lisis del XML. */
	protected SignBatch(final byte[] xml) throws IOException {

		if (xml == null || xml.length < 1) {
			throw new IllegalArgumentException(
				"El XML de definicion de lote de firmas no puede ser nulo ni vacio" //$NON-NLS-1$
			);
		}

		// ****************************************************
		// *********** Carga del XML **************************
		final Document doc;
		try (InputStream is = new ByteArrayInputStream(xml)) {
			try {
				doc = SecurityUtils.getDocumentBuilder().parse(is);
			}
			catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error al cargar el fichero XML de definicion de lote", e); //$NON-NLS-1$
				if (LOGGER.isLoggable(Level.FINE)) {
					LOGGER.fine("XML del lote:\n" + new String(xml)); //$NON-NLS-1$
				}
				throw new IOException("Error al cargar el fichero XML de definicion de lote", e); //$NON-NLS-1$
			}
		}
		// *********** Fin carga del XML **********************
		// ****************************************************

		final Node signBatchNode = doc.getDocumentElement();
		if (!"signbatch".equalsIgnoreCase(signBatchNode.getNodeName())) { //$NON-NLS-1$
			throw new IllegalArgumentException("No se encontro el nodo 'signbatch' en el XML proporcionado"); //$NON-NLS-1$
		}

		// ****************************************************
		// ****** Analisis opciones generales del XML *********
		this.stopOnError = true;
		final NamedNodeMap nnm = signBatchNode.getAttributes();
		if (nnm != null) {
			Node tmpNode = nnm.getNamedItem("stoponerror"); //$NON-NLS-1$
			if (tmpNode != null) {
				this.stopOnError = !"false".equalsIgnoreCase(tmpNode.getNodeValue()); //$NON-NLS-1$
			}
			tmpNode = nnm.getNamedItem("algorithm"); //$NON-NLS-1$
			if (tmpNode != null) {
				this.algorithm = SingleSignConstants.SignAlgorithm.getAlgorithm(tmpNode.getNodeValue());
			}
			else {
				throw new IllegalArgumentException(
					"El nodo 'signbatch' debe contener al manos el atributo de algoritmo" //$NON-NLS-1$
				);
			}
			tmpNode = nnm.getNamedItem("concurrenttimeout"); //$NON-NLS-1$
			if (tmpNode != null) {
				try {
					this.concurrentTimeout = Long.parseLong(tmpNode.getNodeValue());
				}
				catch(final Exception e) {
					LOGGER.severe(
						"Se ha especificado un valor invalido para la espera maxima (" + tmpNode.getNodeValue() + "), se usara el valor por defecto (" + Long.MAX_VALUE + "): " + e //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					);
				}
			}

			this.id = UUID.randomUUID().toString();
			tmpNode = nnm.getNamedItem("Id"); //$NON-NLS-1$
			if (tmpNode != null) {
				this.id = tmpNode.getNodeValue();
			}
		}
		else {
			throw new IllegalArgumentException(
				"El nodo 'signbatch' debe contener al manos el atributo de algoritmo" //$NON-NLS-1$
			);
		}
		// ****** Fin analisis opciones generales del XML *****
		// ****************************************************

		// ****************************************************
		// ****** Analisis firmas individuales del XML ********
		this.signs = parseSignBatchNode(signBatchNode);
		// ****** Fin analisis firmas individuales del XML ****
		// ****************************************************

	}

	protected SignBatch(final List<SingleSign> signatures,
			            final SingleSignConstants.SignAlgorithm algo,
			            final boolean soe) {

		if (signatures == null) {
			throw new IllegalArgumentException(
				"La lista de firmas del lote no puede ser nula" //$NON-NLS-1$
			);
		}
		if (algo == null) {
			throw new IllegalArgumentException(
				"El algoritmo de firma no puede ser nulo" //$NON-NLS-1$
			);
		}
		this.signs = signatures;
		this.stopOnError = soe;
		this.algorithm = algo;
		this.id = UUID.randomUUID().toString();
	}


	/** Ejecuta el preproceso de firma por lote.
	 * @param certChain Cadena de certificados del firmante.
	 * @return Datos trif&aacute;sicos de pre-firma del lote.
	 * @throws BatchException Si hay errores irrecuperables en el proceso. */
	public abstract String doPreBatch(final X509Certificate[] certChain) throws BatchException;

	/** Ejecuta el postproceso de firma por lote.
	 * @param certChain Cadena de certificados del firmante.
	 * @param td Datos trif&aacute;sicos del preproceso.
	 *           Debe contener los datos de todas y cada una de las firmas del lote.
	 * @return Registro del resultado general del proceso por lote, en un XML (<a href="../doc-files/resultlog-scheme.html">descripci&oacute;n
	 *         del formato</a>).
	 * @throws BatchException Si hay errores irrecuperables en el postproceso. */
	public abstract String doPostBatch(final X509Certificate[] certChain,
                                       final TriphaseData td) throws BatchException;

	private static List<SingleSign> parseSignBatchNode(final Node n) throws DOMException, IOException {
		final NodeList childNodes = n.getChildNodes();
		final List<SingleSign> ret = new ArrayList<>();
		int idx = nextNodeElementIndex(childNodes, 0);
		while (idx != -1) {
			ret.add(new SingleSign(childNodes.item(idx)));
			idx = nextNodeElementIndex(childNodes, idx + 1);
		}
		return ret;
	}

	/** Recupera el &iacute;ndice del siguiente nodo de la lista de tipo <code>Element</code>.
	 * Empieza a comprobar los nodos a partir del &iacute;ndice marcado. Si no encuentra un
	 * nodo de tipo <i>elemento</i> devuelve -1.
	 * @param nodes Listado de nodos.
	 * @param currentIndex &Iacute;ndice del listado a partir del cual se empieza la comprobaci&oacute;n.
	 * @return &Iacute;ndice del siguiente node de tipo Element o -1 si no se encontr&oacute;. */
	private static int nextNodeElementIndex(final NodeList nodes, final int currentIndex) {
		Node node;
		int i = currentIndex;
		while (i < nodes.getLength()) {
			node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/** Indica si el proceso por lote debe detenerse cuando se encuentre un error.
	 * @param soe <code>true</code> si el proceso por lote debe detenerse cuando se encuentre un error,
	 *            <code>false</code> si se debe continuar con el siguiente elemento del lote cuando se
	 *            produzca un error. */
	public void setStopOnError(final boolean soe) {
		this.stopOnError = soe;
	}

	protected String getResultLog() {
		// Iniciamos el log de retorno
		final StringBuilder ret = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<signs>\n"); //$NON-NLS-1$
		for (final SingleSign ss : this.signs) {
			ret.append(" "); //$NON-NLS-1$
			ret.append(ss.getProcessResult().toString());
			ret.append("\n"); //$NON-NLS-1$
		}
		ret.append("</signs>"); //$NON-NLS-1$
		return ret.toString();
	}
}
