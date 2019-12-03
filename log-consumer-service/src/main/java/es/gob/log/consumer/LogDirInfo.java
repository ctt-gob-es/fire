package es.gob.log.consumer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Informaci&oacute;n especifica que permite gestionar el directorio de logs.
 */
public class LogDirInfo implements Serializable {

	/** Serial ID. */
	private static final long serialVersionUID = 1L;

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private static final String PROPERTY_OMIT_FILES = "files.ignore";

	private static final String PATTERN_SEPARATOR = ","; //$NON-NLS-1$

	private static final String PROPERTY_FILE_INFO_PREFIX = "files.loginfo.";

	private String[] hiddenPatterns = null;

	private HashMap<String, String[]> fileInfoAssociation = null;

	/**
	 * Carga la informaci&oacute;n del log.
	 * @param is Flujo de entrada con la informaci&oacute;n del log.
	 * @throws IOException Cuando ocurre alg&uacute;n error durante la carga.
	 */
	public void load(final InputStream is) throws IOException {

		final Properties config = new Properties();

		try (final InputStreamReader reader = new InputStreamReader(is, DEFAULT_CHARSET); ) {
			config.load(reader);
		}

		// Cargamos el listado de patrones de fichero que hay que omitir
		final String ommitFiles = config.getProperty(PROPERTY_OMIT_FILES);
		if (ommitFiles != null && !ommitFiles.isEmpty()) {
			final String[] patterns = ommitFiles.split(PATTERN_SEPARATOR);
			final Set<String> patternList = new HashSet<>();
			for (final String pattern : patterns) {
				if (!pattern.isEmpty()) {
					patternList.add(pattern);
				}
			}
			if (!patternList.isEmpty()) {
				this.hiddenPatterns = patternList.toArray(new String[0]);
			}
		}

		// Cargamos el mapeo de asociaciones entre ficheros loginfo y fichero de log
		final Enumeration<?> propNames = config.keys();
		this.fileInfoAssociation = new HashMap<>();
		while (propNames.hasMoreElements()) {
			final String key = (String) propNames.nextElement();
			if (key.startsWith(PROPERTY_FILE_INFO_PREFIX) && !key.equals(PROPERTY_FILE_INFO_PREFIX)) {
				final String patterns = config.getProperty(key);
				if (patterns != null && !patterns.isEmpty()) {
					this.fileInfoAssociation.put(key.substring(PROPERTY_FILE_INFO_PREFIX.length()),
					                             patterns.split(PATTERN_SEPARATOR));
				}
			}
		}
		if (this.fileInfoAssociation.isEmpty()) {
			this.fileInfoAssociation = null;
		}
	}

	/**
	 * Indica si se ha encontrado configuraci&oacute;n relativa al directorio en el
	 * fichero.
	 * @return {@code true} si se encontraba configurada alguna de las propiedades del directorio,
	 * {@code false} en caso contrario.
	 */
	public boolean hasConfiguration() {
		return this.hiddenPatterns != null || this.fileInfoAssociation != null;
	}

	/**
	 * Lista los patrones de nombre de fichero que no deben mostrarse al listar
	 * los logs ni cargarse.
	 * @return Lista de patrones simples o {@code null} si no hay ninguno asociado.
	 */
	public String[] getHiddenPatterns() {
		return this.hiddenPatterns;
	}

	/**
	 * Obtenemos la asociaci&oacute;n especifica que se realiza para ficheros de
	 * configuraci&oacute;n de logs. Este es el mapeado entre ficheros de
	 * configuraci&oacute;n de logs (loginfo) y el patr&oacute;n que identifica los
	 * nombres de los ficheros de log que ficheros de log que deben usarlos.
	 * @return Mapeado de ficheros o {@code null} si no hay ninguno asociado.
	 */
	public HashMap<String, String[]> getFileInfoAssociation() {
		return this.fileInfoAssociation;
	}
}
