package es.gob.fire.signature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Logger;

public class FileConfigLoader extends TempConfigLoader {

	/** Variable de entorno que determina el directorio en el que buscar el fichero de configuraci&oacute;n. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR = "fire.config.path"; //$NON-NLS-1$

	/** Variable de entorno antigua que determinaba el directorio en el que buscar el fichero
	 * de configuraci&oacute;n. Se utiliza si no se ha establecido la nueva variable. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR_OLD = "clavefirma.config.path"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(FileConfigLoader.class.getName());

	private final String filename;
	private final String configDirPath;

	public FileConfigLoader(final String filename) {
		this.filename = filename;

		String dirPath = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR);
		if (dirPath == null) {
			dirPath = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR_OLD);
		}
		this.configDirPath = dirPath;

		// Imprimimos en el log el directorio de carga del fichero
		String msg = "Se cargara el fichero de configuracion " + filename; //$NON-NLS-1$
		if (dirPath != null) {
			msg += " del directorio " + dirPath; //$NON-NLS-1$
		} else {
			msg += " del classpath"; //$NON-NLS-1$
		}
		LOGGER.info(msg);
	}

	/**
	 * Carga un fichero de configuraci&oacute;n del directorio configurado
	 * o del classpath si no se configur&oacute;.
	 * @param configFilename Nombre del fichero de configuraci&oacute;n.
	 * @return Propiedades de fichero de configuraci&oacute;n.
	 * @throws IOException Cuando no se puede cargar el fichero de configuraci&oacute;n.
	 * @throws FileNotFoundException Cuando no se encuentra el fichero de configuraci&oacute;n.
	 */
	@Override
	public Properties loadConfiguration() throws IOException {

		boolean loaded = false;
		final Properties config = new Properties();
		try {
			if (this.configDirPath != null) {
				final File configDir = new File(this.configDirPath).getCanonicalFile();
				final File configFile = new File(configDir, this.filename).getCanonicalFile();
				// Comprobamos que se trate de un fichero sobre el que tengamos permisos y que no
				// nos hayamos salido del directorio de configuracion indicado
				if (configFile.isFile() && configFile.canRead()
						&& configDir.equals(configFile.getParentFile())) {
					try (final InputStream is = new FileInputStream(configFile);
						 final Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
							config.load(reader);
					}
					loaded = true;
				}
				else {
					LOGGER.warning(
							"El fichero " + this.filename + " no existe o no pudo leerse del directorio configurado en la variable " + //$NON-NLS-1$ //$NON-NLS-2$
									ENVIRONMENT_VAR_CONFIG_DIR + ". El fichero debia encontrase en el directorio " + configDir); //$NON-NLS-1$
				}
			}

			// Cargamos el fichero desde el classpath si no se cargo de otro sitio
			if (!loaded) {
				try (final InputStream is = ConfigFileLoader.class.getResourceAsStream('/' + this.filename);) {
					if (is == null) {
						throw new FileNotFoundException("No se ha encontrado el fichero de configuracion " + this.filename); //$NON-NLS-1$
					}
					LOGGER.info("Se ha cargado desde el classpath el fichero de configuracion " + this.filename); //$NON-NLS-1$
					try (final Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
						config.load(reader);
					}
				}
			}
		}
		catch(final FileNotFoundException e){
			throw e;
		}
		catch(final Exception e){
			throw new IOException("No se ha podido cargar el fichero de configuracion " + this.filename, e); //$NON-NLS-1$
		}

		return config;
	}
}
