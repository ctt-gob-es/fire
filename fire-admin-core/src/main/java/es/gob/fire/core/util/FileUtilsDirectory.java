
/** 
 * <b>File:</b><p>es.juntadeandalucia.justicia.biosign.integrationserver.core.util.FileUtilsDirectory.java.</p>
 * <b>Description:</b><p>Utility class to manage files.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electrónica para el Ámbito Judicial.</p>
 * <b>Date:</b><p>23 oct. 2019.</p>
 * @author Consejería de Turismo, Regeneración, Justicia y Administración Local de la Junta de Andalucía.
 * @version 1.0, 23 oct. 2019.
 */
package es.gob.fire.core.util;

import java.io.File;

/** 
 * <p>Utility class to manage files.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electrónica para el Ámbito Judicial.</p>
 * @version 1.0, 23 oct. 2019.
 */
public final class FileUtilsDirectory {
	
	/**
	 * Constructor method for the class FileUtilsDirectory.java. 
	 */
	private FileUtilsDirectory() {
		
	}
	
	/**
	 * Auxiliar method to create an absolute path to a file.
	 * @param pathDir Directory absolute path that contains the file.
	 * @param filename Name of the file.
	 * @return Absolute path of the file.
	 */
	public static String createAbsolutePath(String pathDir, String filename) {
		return pathDir + File.separator + filename;
	}
}
