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
 * <b>File:</b><p>es.clave.SP2.ResponseSAML.java.</p>
 * <b>Description:</b><p>Class with the variables used in the SAML Response.</p>
 * <b>Project:</b><p>Citizen identification and authentication platform.</p>
 * <b>Date:</b><p>29/10/2025.</p>
 * @author Gobierno de España.
 * @version 1.0, 29/10/2025.
 */
package es.gob.fire.web.clave3.sp;

import java.util.Map;

/**
 * <p>Class with the variables used in the SAML Response.</p>
 * <b>Project:</b><p>Citizen identification and authentication platform.</p>
 * @version 1.0, 29/10/2025.
 */
public class ResponseSAML {

	private String relayState;
	private String SAMLResponse;
	private Map<String, String> map;
	private String logoutResponse;

	public String getRelayState() {
		return this.relayState;
	}

	public void setRelayState(final String relayState) {
		this.relayState = relayState;
	}

	public String getSAMLResponse() {
		return this.SAMLResponse;
	}

	public void setSAMLResponse(final String SAMLResponse) {
		this.SAMLResponse = SAMLResponse;
	}

	public Map<String, String> getMap() {
		return this.map;
	}

	public void setMap(final Map<String, String> map) {
		this.map = map;
	}

	public String getLogoutResponse() {
		return this.logoutResponse;
	}

	public void setLogoutResponse(final String logoutResponse) {
		this.logoutResponse = logoutResponse;
	}
}
