package es.gob.log.consumer;

/**
 * Clase que define las constantes generales utilizadas en los servicios.
 * @author Adolfo.Navarro
 *
 */
public class LogConstants {


	/** Indica la extensi&oacute;n de lo ficheros de configuraci&oacute;n de log*/
	public static final String FILE_EXT_LOGINFO =".loginfo"; //$NON-NLS-1$

	/** Indica la extensi&oacute;n de lo ficheros de log*/
	public static final String FILE_EXT_LOG =".log"; //$NON-NLS-1$

	/**Indica la extensi&oacute;n de lo ficheros de log en uso*/
	public static final String FILE_EXT_LCK=".lck"; //$NON-NLS-1$

	/** Indica la extensi&oacute;n de los ficheros comprimidos*/
	public static final String FILE_EXT_ZIP = ".zip"; //$NON-NLS-1$

	/** Indica el tama&ntilde;o de las partes que se dividen los ficheros log en la lectura*/
	public static final int PART_SIZE = 5120000;


}
