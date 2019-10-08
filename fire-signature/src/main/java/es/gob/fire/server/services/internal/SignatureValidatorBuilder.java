package es.gob.fire.server.services.internal;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.signature.ConfigFileLoader;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.upgrade.SignatureValidator;
import es.gob.fire.upgrade.ValidatorException;

public class SignatureValidatorBuilder {

	private static final String DEFAULT_PLATFORM_FILENAME = "platform.properties"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(SignatureValidatorBuilder.class.getName());

	private static SignatureValidator validator;

	/**
	 * Obtiene una nueva instancia de conector con la plataforma validadora.
	 * @return Objeto para la validaci&oacute;n y mejor de las firmas.
	 * @throws ValidatorException Cuando no se puede cargar el conector con el sistema de
	 * validaci&oacute;n y mejora de firmas.
	 */
	public synchronized static SignatureValidator getSignatureValidator() throws ValidatorException {

		if (validator == null) {
			try {
				final String validatorClassName = ConfigManager.getValidatorClass();
				final Class<?> validatorClass = Class.forName(validatorClassName);
				validator = (SignatureValidator) validatorClass.getConstructor().newInstance();
			}
			catch (final Exception e) {
				validator = null;
				throw new ValidatorException("No se pudo crear el conector con la plataforma de " //$NON-NLS-1$
						+ "validacion y actualizacion de firmas", e); //$NON-NLS-1$
			}

			Properties config;
			try {
				config = ConfigFileLoader.loadConfigFile(DEFAULT_PLATFORM_FILENAME);
			}
			catch (final Exception e) {
				LOGGER.log(Level.WARNING, "No se pudo cargar el fichero " + DEFAULT_PLATFORM_FILENAME //$NON-NLS-1$
						+ " con la configuracion de la plataforma de validacion y actualizacion de firmas. " //$NON-NLS-1$
						+ "Debe agregar al directorio de ficheros de configuracion el fichero " //$NON-NLS-1$
						+ DEFAULT_PLATFORM_FILENAME, e);
				validator = null;
				throw new  ValidatorException("No se pudo cargar el fichero ed configuracion para " //$NON-NLS-1$
						+ "la plataforma de validacion y actualizacion de firmas", e); //$NON-NLS-1$
			}

			try {
				validator.init(config);
			}
			catch (final Exception e) {
				LOGGER.log(Level.WARNING, "Error en la configuracion de la plataforma de validacion", e); //$NON-NLS-1$
				validator = null;
				throw new  ValidatorException("No se pudo cargar el fichero de configuracion de " //$NON-NLS-1$
						+ "la plataforma de validacion y actualizacion de firmas", e); //$NON-NLS-1$
			}
		}

		return validator;
	}
}
