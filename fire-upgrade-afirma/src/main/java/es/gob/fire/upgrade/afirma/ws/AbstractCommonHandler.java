// Copyright (C) 2012-13 MINHAP, Gobierno de Espa&ntilde;a
// This program is licensed and may be used, modified and redistributed under the terms
// of the European Public License (EUPL), either version 1.1 or (at your
// option) any later version as soon as they are approved by the European Commission.
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// or implied. See the License for the specific language governing permissions and
// more details.
// You should have received a copy of the EUPL1.1 license
// along with this program; if not, you may find it at
// http://joinup.ec.europa.eu/software/page/eupl/licence-eupl

/**
 * <b>File:</b><p>es.gob.afirma.wsServiceInvoker.ws.AbstractCommonHandler.java.</p>
 * <b>Description:</b><p>Class that represents handlers used in the service invoker.</p>
 * <b>Project:</b><p>Library for the integration with the services of @Firma, eVisor and TS@.</p>
 * <b>Date:</b><p>03/10/2011.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.1, 04/03/2020.
 */
package es.gob.fire.upgrade.afirma.ws;

import java.util.Properties;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;

/**
 * <p>Class that represents handlers used in the service invoker.</p>
 * <b>Project:</b><p>Library for the integration with the services of @Firma, eVisor and TS@.</p>
 * @version 1.1, 04/03/2020.
 */
public class AbstractCommonHandler extends AbstractHandler {

	/**
	 * Attribute that represents the user name to authenticate the request with UserNameToken, or the alias of the private key defined to to authenticate the
	 * request with BinarySecurityToken.
	 */
	private String userAlias = ""; //$NON-NLS-1$

	/**
	 * Attribute that represents the user password to authenticate the request with UserNameToken, or the password of the private key defined to authenticate
	 * the request with BinarySecurityToken.
	 */
	private String password = ""; //$NON-NLS-1$

	/**
	 * Attribute that represents type of password.
	 */
	private String passwordType = WSConstants.PASSWORD_TEXT;

	/**
	 * Attribute that represents user Keystore.
	 */
	private String keystore;

	/**
	 * Attribute that represents user Keystore Pass.
	 */
	private String keystorePass;

	/**
	 * Attribute that represents user Keystore Type.
	 */
	private String keystoreType;

	/**
	 * {@inheritDoc}
	 * @see org.apache.axis2.engine.Handler#invoke(org.apache.axis2.context.MessageContext)
	 */
	@Override
	public InvocationResponse invoke(final MessageContext msgContext) throws AxisFault {
		return InvocationResponse.CONTINUE;
	}

	/**
	 * Method that configures the properties related to WSS4J cryptographic manager.
	 * @return the configured properties related to WSS4J cryptographic manager.
	 * @throws WSSecurityException If there is an error in loading the cryptographic properties.
	 */
	final Crypto getCryptoInstance() throws WSSecurityException {

		final Properties properties = new Properties();

		// Configuracion del certificado SSL Cliente para el acceso al servidor
		properties.setProperty("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin"); //$NON-NLS-1$ //$NON-NLS-2$
		if (this.keystore != null) {
			properties.setProperty("org.apache.ws.security.crypto.merlin.file", this.keystore); //$NON-NLS-1$
		}
		if (this.keystoreType != null) {
			properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", this.keystoreType); //$NON-NLS-1$
		}
		if (this.keystorePass != null) {
			properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", this.keystorePass); //$NON-NLS-1$
		}
		if (this.userAlias != null) {
			properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias", this.userAlias); //$NON-NLS-1$
		}
		if (this.password != null) {
			properties.setProperty("org.apache.ws.security.crypto.merlin.alias.password", this.password); //$NON-NLS-1$
		}

		// Nota: Existen tambien las propiedades:
		//  - org.apache.ws.security.crypto.merlin.truststore.file
		//  - org.apache.ws.security.crypto.merlin.truststore.type
		//  - org.apache.ws.security.crypto.merlin.truststore.password
		// Sin embargo, estas propiedades no permiten establecer el almacen de confianza en el que
		// consultar si es seguro el certificado SSL del servidor, sino un almacen independiente en
		// el que se encuentren los certificados emisores del certificado cliente usado para establecer
		// la conexion en caso de que estos no estuviesen en el mismo almacen del certificado cliente.

		Crypto crypto;
		try {
			crypto = CryptoFactory.getInstance(properties);
		}
		catch (final Exception e) {
			throw new WSSecurityException("Error al configurar el certificado cliente SSL o el almacen de confianza", e); //$NON-NLS-1$
		}
		return crypto;
	}

	/**
	 * Gets the value of the attribute {@link #password}.
	 * @return the value of the attribute {@link #password}.
	 */
	public final String getPassword() {
		return this.password;
	}

	/**
	 * Sets the value of the attribute {@link #password}.
	 * @param passParam The value for the attribute {@link #password}.
	 */
	public final void setPassword(final String passParam) {
		this.password = passParam;
	}

	/**
	 * Gets the value of the attribute {@link #passwordType}.
	 * @return the value of the attribute {@link #passwordType}.
	 */
	public final String getPasswordType() {
		return this.passwordType;
	}

	/**
	 * Sets the value of the attribute {@link #passwordType}.
	 * @param passTypeParam The value for the attribute {@link #passwordType}.
	 */
	public final void setPasswordType(final String passTypeParam) {
		if ("digest".equalsIgnoreCase(passTypeParam)) { //$NON-NLS-1$
			this.passwordType = WSConstants.PASSWORD_DIGEST;
		} else if ("clear".equalsIgnoreCase(passTypeParam)) { //$NON-NLS-1$
			this.passwordType = WSConstants.PASSWORD_TEXT;
		}
	}

	/**
	 * Gets the value of the attribute {@link #keystore}.
	 * @return the value of the attribute {@link #keystore}.
	 */
	public final String getKeystore() {
		return this.keystore;
	}

	/**
	 * Sets the value of the attribute {@link #keystore}.
	 * @param keystore The value for the attribute {@link #keystore}.
	 */
	public final void setKeystore(final String keystore) {
		this.keystore = keystore;
	}

	/**
	 * Gets the value of the attribute {@link #keystorePass}.
	 * @return the value of the attribute {@link #keystorePass}.
	 */
	public final String getKeystorePass() {
		return this.keystorePass;
	}

	/**
	 * Sets the value of the attribute {@link #keystorePass}.
	 * @param keystorePass The value for the attribute {@link #keystorePass}.
	 */
	final void setKeystorePass(final String keystorePass) {
		this.keystorePass = keystorePass;
	}

	/**
	 * Gets the value of the attribute {@link #keystoreType}.
	 * @return the value of the attribute {@link #keystoreType}.
	 */
	final String getKeystoreType() {
		return this.keystoreType;
	}

	/**
	 * Sets the value of the attribute {@link #keystoreType}.
	 * @param keystoreType The value for the attribute {@link #keystoreType}.
	 */
	public final void setKeystoreType(final String keystoreType) {
		this.keystoreType = keystoreType;
	}

	/**
	 * Gets the value of the attribute {@link #userAlias}.
	 * @return the value of the attribute {@link #userAlias}.
	 */
	public final String getUserAlias() {
		return this.userAlias;
	}

	/**
	 * Sets the value of the attribute {@link #userAlias}.
	 * @param userAliasParam The value for the attribute {@link #userAlias}.
	 */
	public final void setUserAlias(final String userAliasParam) {
		this.userAlias = userAliasParam;
	}
}
