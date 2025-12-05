// Copyright (C) 2012-13 MINHAP, Gobierno de Espana
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
package es.gob.fire.upgrade.afirma.ws;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * X509TrustManager that logs server certificates while delegating validation
 * to a standard trust store.
 */
public class LoggingTrustManager implements X509TrustManager {

	/**
	 * The default X509TrustManager to which certificate checks are delegated.
	 */
	private final X509TrustManager defaultTM;

    /**
     * Constructs a LoggingTrustManager using the provided KeyStore.
     * Delegates certificate validation to the default X509TrustManager
     * initialized with the given trust store.
     *
     * @param trustStore the KeyStore containing trusted certificates
     * @throws Exception if initializing the TrustManagerFactory fails
     */
    public LoggingTrustManager(final KeyStore trustStore) throws Exception {

        final TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        this.defaultTM = (X509TrustManager) tmf.getTrustManagers()[0];
    }

    /**
     * Delegates the client certificate validation to the default trust manager.
     *
     * @param chain the client certificate chain
     * @param authType the authentication type based on the client certificate
     * @throws java.security.cert.CertificateException if the certificate chain is not trusted
     */
    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType)
            throws java.security.cert.CertificateException {
        this.defaultTM.checkClientTrusted(chain, authType);
    }

    /**
     * Delegates the server certificate validation to the default trust manager,
     * using the provided trust store for actual verification.
     *
     * @param chain the server certificate chain
     * @param authType the key exchange algorithm used
     * @throws java.security.cert.CertificateException if the server certificate chain is not trusted
     */
    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType)
            throws java.security.cert.CertificateException {

        // Validacion REAL usando truestore propio
        this.defaultTM.checkServerTrusted(chain, authType);
    }

    /**
     * Returns the list of certificate authority certificates that are trusted for authenticating peers.
     * Delegates to the underlying default trust manager.
     *
     * @return an array of accepted X509Certificates
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return this.defaultTM.getAcceptedIssuers();
    }
}

