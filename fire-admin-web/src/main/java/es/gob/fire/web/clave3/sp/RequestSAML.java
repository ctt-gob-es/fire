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
 * <b>File:</b><p>es.clave.SP2.RequestSAML.java.</p>
 * <b>Description:</b><p>Class with the variables used in the SAML Request.</p>
 * <b>Project:</b><p>Citizen identification and authentication platform.</p>
 * <b>Date:</b><p>29/10/2025.</p>
 * @author Gobierno de España.
 * @version 1.0, 29/10/2025.
 */
package es.gob.fire.web.clave3.sp;

/**
 * <p>Class with the variables used in the SAML Request.</p>
 * <b>Project:</b><p>Citizen identification and authentication platform.</p>
 * @version 1.0, 29/10/2025.
 */
public class RequestSAML {

	private String id;
	private String providerName;
	private String url;
	private String returnURL;
	private String application;
	private boolean forceCheck;
	private String eidasloa;
	private String nameIDPolicy;
	private boolean afirmaCheck;
	private boolean gissCheck;
	private boolean eidasCheck;
	private boolean mobileCheck;
	private String relayState;
	private String SAMLRequest;
	private String logoutRequest;

	public String getProviderName() {
		return this.providerName;
	}

	public void setProviderName(final String providerName) {
		this.providerName = providerName;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getReturnURL() {
		return this.returnURL;
	}

	public void setReturnURL(final String returnURL) {
		this.returnURL = returnURL;
	}

	public String getApplication() {
		return this.application;
	}

	public void setApplication(final String application) {
		this.application = application;
	}

	public boolean getForceCheck() {
		return this.forceCheck;
	}

	public void setForceCheck(final boolean forceCheck) {
		this.forceCheck = forceCheck;
	}

	public String getEidasloa() {
		return this.eidasloa;
	}

	public void setEidasloa(final String eidasloa) {
		this.eidasloa = eidasloa;
	}

	public String getNameIDPolicy() {
		return this.nameIDPolicy;
	}

	public void setNameIDPolicy(final String nameIDPolicy) {
		this.nameIDPolicy = nameIDPolicy;
	}

	public boolean getAfirmaCheck() {
		return this.afirmaCheck;
	}

	public void setAfirmaCheck(final boolean afirmaCheck) {
		this.afirmaCheck = afirmaCheck;
	}

	public boolean getGissCheck() {
		return this.gissCheck;
	}

	public void setGissCheck(final boolean gissCheck) {
		this.gissCheck = gissCheck;
	}

	public boolean getEidasCheck() {
		return this.eidasCheck;
	}

	public void setEidasCheck(final boolean eidasCheck) {
		this.eidasCheck = eidasCheck;
	}

	public boolean getMobileCheck() {
		return this.mobileCheck;
	}

	public void setMobileCheck(final boolean mobileCheck) {
		this.mobileCheck = mobileCheck;
	}

	public String getRelayState() {
		return this.relayState;
	}

	public void setRelayState(final String relayState) {
		this.relayState = relayState;
	}

	public String getSAMLRequest() {
		return this.SAMLRequest;
	}

	public void setSAMLRequest(final String SAMLRequest) {
		this.SAMLRequest = SAMLRequest;
	}

	public String getLogoutRequest() {
		return this.logoutRequest;
	}

	public void setLogoutRequest(final String logoutRequest) {
		this.logoutRequest = logoutRequest;
	}

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}
}
