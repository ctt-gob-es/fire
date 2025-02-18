/* 
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
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
 * <b>File:</b><p>es.gob.afirma.crypto.cades.verifier.CAdESAnalizer.CertHolderBySignerIdSelector.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p>17/05/2023.</p>
 * @author Gobierno de España.
 * @version 1.0, 17/05/2023.
 */
package es.gob.fire.crypto.cades.verifier;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.util.Selector;

/** 
* <p>Class.</p>
* <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
* @version 1.0, 17/05/2023.
*/
final class CertHolderBySignerIdSelector implements Selector {

	private final SignerId signerId;
	CertHolderBySignerIdSelector(final SignerId sid) {
		if (sid == null) {
			throw new IllegalArgumentException("El ID del firmante no puede ser nulo"); //$NON-NLS-1$
		}
		this.signerId = sid;
	}

	/** {@inheritDoc} */
	@Override
	public boolean match(final Object o) {
		return CertHolderBySignerIdSelector.this.signerId.getSerialNumber()
				.equals(((X509CertificateHolder) o).getSerialNumber());
	}

	/** {@inheritDoc} */
	@Override
	public Object clone() {
		throw new UnsupportedOperationException();
	}
}
