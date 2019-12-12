package es.gob.fire.server.services;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase que almacena datos del proyecto obtenidos del fichero "version.info", que se rellena con
 * los datos obtenidos durante el empaquetado.
 */
public class ProjectConstants {

	private static final String RESOURCE_NAME = "/version.info"; //$NON-NLS-1$

	private static final String PROPERTY_VERSION = "project.version"; //$NON-NLS-1$

	private static final String PROPERTY_COPYRIGHT_YEAR = "copyright.year"; //$NON-NLS-1$

	public static final String VERSION;

	public static final String COPY_YEAR;

	static {

		final Properties constants = new Properties();
		try (InputStream is = ProjectConstants.class.getResourceAsStream(RESOURCE_NAME);
			 InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
			constants.load(isr);
		}
		catch (final Exception e) {
			Logger.getLogger(ProjectConstants.class.getName()).log(
					Level.WARNING, "No se puede cargar la informacion del proyecto", e); //$NON-NLS-1$
		}

		VERSION = constants.getProperty(PROPERTY_VERSION);
		COPY_YEAR = constants.getProperty(PROPERTY_COPYRIGHT_YEAR);
	}
}
