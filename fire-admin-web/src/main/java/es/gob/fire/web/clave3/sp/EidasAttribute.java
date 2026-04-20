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
 * <b>File:</b><p>es.clave.SP2.SubmitRequestController.java.</p>
 * <b>Description:</b><p>Class with information to build a series of attributes that are included in the SAML request from the Kit to Proxy2.</p>
 * <b>Project:</b><p>Citizen identification and authentication platform.</p>
 * <b>Date:</b><p>29/10/2025.</p>
 * @author Gobierno de España.
 * @version 1.0, 29/10/2025.
 */
package es.gob.fire.web.clave3.sp;

/**
 * <p>Class with information to build a series of attributes that are included in the SAML request from the Kit to Proxy2.</p>
 * <b>Project:</b><p>Citizen identification and authentication platform.</p>
 * @version 1.0, 29/10/2025.
 */
public enum EidasAttribute {

    AFIRMA("AFirmaIdP", "http://es.minhafp.clave/AFirmaIdP", false), //$NON-NLS-1$ //$NON-NLS-2$
    GISS("GISSIdP", "http://es.minhafp.clave/GISSIdP", false), //$NON-NLS-1$ //$NON-NLS-2$
    EIDAS("EIDASIdP", "http://es.minhafp.clave/EIDASIdP", false), //$NON-NLS-1$ //$NON-NLS-2$
    MOVIL("CLVMOVILIdP", "http://es.minhafp.clave/CLVMOVILIdP", false), //$NON-NLS-1$ //$NON-NLS-2$
    RELAY_STATE("RelayState", "http://es.minhafp.clave/RelayState", false); //$NON-NLS-1$ //$NON-NLS-2$

	private final String friendlyName;
	private final String name;
	private final boolean required;

	EidasAttribute(final String friendlyName, final String name, final boolean required) {
		this.friendlyName = friendlyName;
		this.name = name;
		this.required = required;
	}

	public static EidasAttribute fromString(final String str) {
		for (final EidasAttribute b: EidasAttribute.values()) {
			if (b.friendlyName.equalsIgnoreCase(str)) {
				return b;
			}
		}
		throw new IllegalArgumentException("No constant with friendlyName '" + str + "' found"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getFriendlyName() {
		return this.friendlyName;
	}

	public String getName() {
		return this.name;
	}

	public boolean isRequired() {
		return this.required;
	}
}
