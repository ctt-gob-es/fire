package es.gob.fire.server.admin.service;

import java.util.logging.Logger;

public class LoggerInfo {


	private static final Logger LOGGER = Logger.getLogger(RolePermissions.class.getName());



	public static String getInstance() {

		final LogUtils InformationLogger = new LogUtils();
		String src = null;

		src = src.replaceAll("[\r\n]","");  //$NON-NLS-1$//$NON-NLS-2$




		return src;
	}


}
