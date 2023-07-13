package es.gob.fire.server.services.internal;

import es.gob.fire.signature.DbManager;

/**
 * Factor&iacute;a para la obtenci&oacute;n del objeto de acceso a datos para la obtenci&oacute;n
 * de informaci&oacute;n de las aplicaciones.
 */
public class ApplicationsDAOFactory {

	private static ApplicationsDAO instance = null;

	/**
	 * Obtiene el objeto activo para el acceso a datos de la informaci&oacute;n
	 * de las aplicaciones registradas en el sistema.
	 * @return Objeto para el acceso a la informacion de las aplicaciones.
	 */
	public static ApplicationsDAO getApplicationsDAO() {
		if (instance == null) {
			instance = loadDAOInstance();
		}
		return instance;
	}

	private static ApplicationsDAO loadDAOInstance() {
		if (DbManager.isConfigured()) {
			return new DBApplicationsDAO();
		}
		return new ConfigFileApplicationsDAO();
	}
}
