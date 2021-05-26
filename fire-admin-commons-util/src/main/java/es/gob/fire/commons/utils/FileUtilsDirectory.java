/** 
 * <b>File:</b><p>es.gob.fire.commons.utils.FileUtilsDirectory.java.</p>
 * <b>Description:</b><p>Utility class to manage files.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * <b>Date:</b><p>23/05/2020.</p>
 * @version 1.0, 23/05/2020.
 */
package es.gob.fire.commons.utils;

import java.io.File;

/** 
 * <p>Utility class to manage files.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 23/05/2020.
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
