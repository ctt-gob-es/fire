/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espa&ntilde;a
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/

/** 
 * <b>File:</b><p>es.gob.fire.commons.utils.UtilsServer.java.</p>
 * <b>Description:</b><p>Utility class for server properties and others.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p> 22/01/2025.</p>
 * @author Gobierno de España.
 * @version 1.0, 22/01/2025.
 */
package es.gob.fire.commons.utils;

import java.io.File;

/** 
 * <p>Utility class for server properties and others.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * @version 1.0, 22/01/2025.
 */
public final class UtilsServer {

	/**
	 * Constant attribute that represents the property key fire.config.path.
	 */
	public static final String PROP_SERVER_CONFIG_DIR = "fire.config.path";
	
	/**
     * Directory name where message-related files are stored.
     */
    public static final String MESSAGES_DIRECTORY = "messages";

    /**
     * Directory name where Cl@ve-related files are stored.
     */
    public static final String CLAVE_DIRECTORY = "clave";
	
	/**
	 * Constructor method for the class UtilsServer.java.
	 */
	private UtilsServer() {

	}

	/**
	 * Method that returns the value of the system property fire.config.path.
	 * @return Value of the system property fire.config.path. Null if not exist.
	 */
	public static String getServerConfigDir() {
		return System.getProperty(PROP_SERVER_CONFIG_DIR);
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

	/**
	 * Method that returns the value of the system property fire.config.path.
	 * @return Value of the system property fire.config.path. Null if not exist.
	 */
	public static String getMessagesDir() {
		return FileUtilsDirectory.createAbsolutePath(getServerConfigDir(), Constants.MESSAGES);
	}

}
