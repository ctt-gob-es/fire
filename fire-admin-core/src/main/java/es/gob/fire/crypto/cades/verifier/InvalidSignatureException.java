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
 * <b>File:</b><p>es.gob.afirma.crypto.cades.verifier.CAdESAnalizer.InvalidSignatureException.java.</p>
 * <b>Description:</b><p> Indica un error al validar una firma electr&oacute;nica.</p>
  * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p>17/05/2023.</p>
 * @author Gobierno de España.
 * @version 1.0, 17/05/2023.
 */
package es.gob.fire.crypto.cades.verifier;


/** 
 * <p>Indica un error al validar una firma electr&oacute;nica.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * @version 1.0, 17/05/2023.
 */
public class InvalidSignatureException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = 1827813739580107751L;

	/**
	 * Construye la excepci&oacute;n.
	 * @param message Mensaje que identifica el error encontrado
	 * en la validaci&oacute;n.
	 */
	public InvalidSignatureException(final String message) {
		super(message);
	}

	/**
	 * Construye la excepci&oacute;n.
	 * @param message Mensaje que identifica el error encontrado
	 * en la validaci&oacute;n.
	 * @param cause Motivo por el que se origin&oacute; el error.
	 */
	public InvalidSignatureException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
