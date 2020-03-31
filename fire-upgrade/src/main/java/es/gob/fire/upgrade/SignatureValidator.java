package es.gob.fire.upgrade;

import java.io.IOException;
import java.util.Properties;

/**
 * Conector para el uso de las funciones de validaci&oacute;n y actualizaci&oacute;n de
 * firmas de una plataforma.
 */
public interface SignatureValidator {

	/**
	 * Inicializa el validador y le proporciona las propiedades establecidas en el fichero de
	 * configuraci&oacute;n para la conexi&oacute;n y uso de la plataforma.
	 * @param config Configuraci&oacute;n de la plataforma.
	 */
	void init(Properties config);

	/**
	 * Actualiza una firma electr&oacute;nica a un formato longevo.
	 * @param signature Firma electr&oacute;nica a actualizar.
	 * @param upgradeFormat Nombre del formato longevo.
	 * @param config Opciones de configuraci&oacute;n adicionales para la operaci&oacute;n.
	 * @return Resultado de la operaci&oacute;n de actualizaci&oacute;n.
	 * @throws UpgradeException Cuando ocurre un error durante la actualizaci&oacute;n de la firma.
	 * @throws VerifyException Cuando la firma que se desea actualizar no es v&aacute;lida.
	 */
	UpgradeResult upgradeSignature(byte[] signature, String upgradeFormat, Properties config)
			throws UpgradeException, VerifyException;

	/**
	 * Recupera una firma enviada a actualizar previamente y para la que se solicit&oacute; un
	 * periodo de gracia antes de su actualizaci&oacute;n.
	 * @param docId Identificador del documento que se devolvi&oacute; al enviar a actualizar.
	 * @param upgradeFormat Nombre del formato longevo que se solicit&oacute;.
	 * @param config Opciones de configuraci&oacute;n adicionales para la operaci&oacute;n.
	 * @return Resultado de la operaci&oacute;n de actualizaci&oacute;n.
	 * @throws UpgradeException Cuando ocurre un error al recuperar la firma.
	 * @throws IOException Cuando ocurre un error al conectar con la plataforma de
	 * actualizaci&oacute;n.
	 */
	UpgradeResult recoverUpgradedSignature(String docId, String upgradeFormat, Properties config)
			throws UpgradeException, IOException;

	/**
	 * Valida una firma electr&oacute;nica.
	 * @param signature Firma electr&oacute;nica a validar.
	 * @param config Opciones de configuraci&oacute;n adicionales para la operaci&oacute;n.
	 * @return Resultado de la operaci&oacute;n de actualizaci&oacute;n.
	 * @throws VerifyException Cuando ocurre un error durante la validaci&oacute;n de la firma.
	 */
	VerifyResult validateSignature(byte[] signature, Properties config) throws VerifyException;
}
