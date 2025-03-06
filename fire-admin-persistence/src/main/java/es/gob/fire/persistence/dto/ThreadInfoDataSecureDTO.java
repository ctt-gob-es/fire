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
 * <b>File:</b><p>es.gob.fire.persistence.dto.ThreadInfoDataSecureDTO.java.</p>
 * <b>Description:</b><p>A component that stores user session data in a thread-local map. Each thread has its own instance of the map.</p>
 * <b>Project:</b><p></p>
 * <b>Date:</b><p>24/02/2025.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 24/02/2025.
 */
package es.gob.fire.persistence.dto;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * <p>
 * A component that stores user session data in a thread-local map. Each thread
 * has its own instance of the map.
 * </p>
 * <b>Project:</b>
 * <p></p>
 * 
 * @version 1.0, 24/02/2025.
 */
@Component
public class ThreadInfoDataSecureDTO {

	// Thread-local variable to store session data
	private static final ThreadLocal<Map<String, String>> userSession = ThreadLocal.withInitial(HashMap::new);

	/**
	 * Sets the random string login for the current thread.
	 * 
	 * @param value
	 *            the random string login
	 */
	public void setRandomStringLogin(String value) {
		userSession.get().put("randomStringLogin", value);
	}

	/**
	 * Sets the limit sign generation value for the current thread.
	 * 
	 * @param value
	 *            the limit sign generation value
	 */
	public void setLimitSignGen(String value) {
		userSession.get().put("limitSignGen", value);
	}

	/**
	 * Gets the random string login for the current thread.
	 * 
	 * @return the random string login, or null if not set
	 */
	public String getRandomStringLogin() {
		return userSession.get().get("randomStringLogin");
	}

	/**
	 * Gets the limit sign generation value for the current thread.
	 * 
	 * @return the limit sign generation value, or null if not set
	 */
	public String getLimitSignGen() {
		return userSession.get().get("limitSignGen");
	}

	/**
	 * Clears the thread-local data for the current thread.
	 */
	public void clear() {
		userSession.remove();
	}

}
