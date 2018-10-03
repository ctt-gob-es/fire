package es.gob.log.consumer.service;

public class EchoServiceManager {

	private static final String SERVICE_TITLE = "Gestion Logs v%s"; //$NON-NLS-1$

	private static final String SERVICE_VERSION = "1.0"; //$NON-NLS-1$

	/**
	 * Obtiene una cadena de bytes predefinida
	 * @return cadena de bytes con formato "Gestion Logs v%s"
	 */
	public static byte[] process() {

		//TODO: El numero de version del servicio habria que cogerlo del Manifest
		return String.format(SERVICE_TITLE, SERVICE_VERSION).getBytes();
	}
}
