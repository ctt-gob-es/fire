package es.gob.fire.server.services.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import es.gob.afirma.core.misc.Base64;

public class PropertiesUtils {

	/** Juego de caracteres por defecto. */
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	/**
	 * Convierte una cadena Base64 en un objeto de propiedades.
	 * @param base64 Base64 que descodificado es un fichero de propiedades en texto plano.
	 * @return Objeto de propiedades.
	 * @throws IOException Si hay problemas en el proceso.
	 */
    public static Properties base642Properties(final String base64) throws IOException {
    	final Properties p = new Properties();
    	if (base64 == null || base64.isEmpty()) {
    		return p;
    	}

    	p.load(new InputStreamReader(
    			new ByteArrayInputStream(
    					Base64.decode(base64, base64.indexOf('-') > -1 || base64.indexOf('_') > -1)
    					),
    			DEFAULT_CHARSET));

    	return p;
    }

    /**
     * Convierte un objeto de propiedades en una cadena Base64 con todos sus elementos.
	 * @param p Objeto de propiedades.
	 * @return Cadena Base64.
	 */
    public static String properties2Base64(final Properties p) {

    	if (p == null) {
    		return ""; //$NON-NLS-1$
    	}
    	return Base64.encode(propertiesToStringBuilder(p).toString().getBytes(DEFAULT_CHARSET));
    }

    /**
     * Convierte un objeto de propiedades en una cadena Base64 con todos sus elementos.
	 * @param p Objeto de propiedades.
	 * @return Cadena Base64.
	 */
    public static String properties2String(final Properties p) {

    	if (p == null) {
    		return ""; //$NON-NLS-1$
    	}
    	return propertiesToStringBuilder(p).toString();
    }

    private static StringBuilder propertiesToStringBuilder(final Properties p) {
    	final StringBuilder buffer = new StringBuilder();
    	final String[] keys = p.keySet().toArray(new String[p.size()]);
    	for (int i = 0; i < keys.length; i++) {
    		final String key = keys[i];
    		buffer.append(key).append("=").append(p.getProperty(key)); //$NON-NLS-1$
    		if (i < keys.length -1) {
    			buffer.append("\n"); //$NON-NLS-1$
    		}
    	}
    	return buffer;
    }
}
