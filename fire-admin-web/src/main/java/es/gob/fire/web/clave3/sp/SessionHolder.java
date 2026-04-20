/* 
/*******************************************************************************
 * Copyright (C) 2024 Secretaría General de la Administración Digital, Gobierno de España
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
 * <b>File:</b><p>es.clave.SP2.SessionHolder.java.</p>
 * <b>Description:</b><p> Class that maintain in the thread the sessions of the authentication cycle mocking the session of the server.</p>
 * <b>Project:</b><p>Citizen identification and authentication platform.</p>
 * <b>Date:</b><p>29/10/2025.</p>
 * @author Gobierno de España.
 * @version 1.0, 29/10/2025.
 */
package es.gob.fire.web.clave3.sp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

/** 
 * <p>Class that maintain in the thread the sessions of the authentication cycle mocking the session of the server.</p>
 * <b>Project:</b><p>Citizen identification and authentication platform.</p>
 * @version 1.0, 29/10/2025.
 */
public class SessionHolder {

	private static final ThreadLocal<HttpSession> sessionHolderMap = new ThreadLocal<>();

	/**
	 * Atributo que mantiene las sesiones abiertas durante el ciclo de autenticación.
	 */
	protected static Map<String, String> sessionsSAML = new ConcurrentHashMap<>(10);

	private SessionHolder() {
	}

	public static void setId(HttpSession identifier) {
		if (null == identifier) {
			// throw some exception
		}
		sessionHolderMap.set(identifier);
	}

	public static HttpSession getId() {
		return sessionHolderMap.get();
	}

	public static void clear() {
		sessionHolderMap.remove();
	}
}
