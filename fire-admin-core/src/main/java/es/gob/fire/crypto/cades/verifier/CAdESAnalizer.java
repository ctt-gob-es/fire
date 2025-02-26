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
 * <b>File:</b><p>es.gob.afirma.crypto.cades.verifier.CAdESAnalizer.CAdESAnalizer.java.</p>
 * <b>Description:</b><p> Clase para el analisis y validaci&oacute;n de una firma CAdES.</p>
  * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p>17/05/2023.</p>
 * @author Gobierno de España.
 * @version 1.0, 17/05/2023.
 */
package es.gob.fire.crypto.cades.verifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

/** 
 * <p>Clase para el analisis y validaci&oacute;n de una firma CAdES.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * @version 1.0, 17/05/2023.
 */
public class CAdESAnalizer {

	private byte[] signature = null;
	private byte[] content = null;
	private List<SignerInfo> signers = null;
	private CertificateFactory certFactory = null;

	/**
	 * Inicializa el analizador con una firma.
	 * @param cadesSignature Firma CAdES.
	 * @throws IOException Cuando no se puedan obtener los certificados de la firma.
	 * @throws CertificateException Cuando no se puedan componer los certificados de la firma despu&eacute;s de obtenerlos.
	 */
	public void init(final byte[] cadesSignature) throws IOException, CertificateException {
		this.signature = cadesSignature;
		this.content = null;
		this.signers = null;

		if (this.certFactory == null) {
			this.certFactory = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
		}

		analize();
	}

	/**
	 * Analiza la firma y extrae la informaci&oacute;n que se necesita de ella.
	 * @throws IOException Cuando no se puedan obtener los certificados de la firma.
	 * @throws CertificateException Cuando no se puedan componer los certificados de la firma despu&eacute;s de obtenerlos.
	 */
	private void analize() throws IOException, CertificateException {

		CMSSignedData signedData;
		try {
			signedData = new CMSSignedData(this.signature);
		}
		catch (final Exception e) {
			throw new IllegalArgumentException("Los datos no se corresponden con una firma binaria", e); //$NON-NLS-1$
		}

		this.content = getContent(signedData);

		this.signers = getSigners(signedData);
	}

	/**
	 * Obtiene los datos firmados de una firma.
	 * @param sd Datos firmados.
	 * @return Contenido firmado o {@code null} si la firma no lo incluye.
	 */
	private static byte[] getContent(final CMSSignedData sd) {

		final CMSProcessable signedContent = sd.getSignedContent();
		return signedContent != null
				? (byte[]) signedContent.getContent() : null;
	}

	private List<SignerInfo> getSigners(final CMSSignedData signedData)
			throws CertificateException, IOException {

		final List<SignerInfo> signersList = new ArrayList<>();

		final Store store = signedData.getCertificates();

		for (final Object si : signedData.getSignerInfos().getSigners()) {

			final SignerInformation signer = (SignerInformation) si;

			final X509Certificate cert = getCertificateFromSigner(store, signer);

			signersList.add(new SignerInfo(signer, cert));
		}

		return signersList;
	}


	/**
	 * Obtiene el certificado asociado a un firmante.
	 * @param store Almac&eacute;n con los certificados de los firmantes.
	 * @param signer Informaci&oacute;n de la firma individual de un firmante.
	 * @return Certificado del firmante.
	 * @throws CertificateException Cuando no se puede codificar un certificado.
	 * @throws IOException Cuando no se puede recuperar un certificado.
	 */
	private X509Certificate getCertificateFromSigner(final Store store, final SignerInformation signer)
			throws CertificateException, IOException {

		final Iterator<?> certIt = store.getMatches(new CertHolderBySignerIdSelector(signer.getSID())).iterator();
        final X509Certificate cert = (X509Certificate) this.certFactory.generateCertificate(
    		new ByteArrayInputStream(
				((X509CertificateHolder) certIt.next()).getEncoded()
			)
		);

		return cert;
	}

	/**
	 * Valida la integridad de la firma. NOTA: No comprueba la vigencia ni
	 * validez de los certificados de firma.
	 * @throws InvalidSignatureException Cuando alguna de las firmas hayan
	 * sido modificadas.
	 */
	public void verify() throws InvalidSignatureException {
		checkInit();
		for (final SignerInfo signerInfo : this.signers) {
			if (!verifySigner(signerInfo.getSigner(), signerInfo.getCert())) {
				throw new InvalidSignatureException("El firmante no es valido"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Valida que la informaci&oacute;n firmada por un firmante es integra y se
	 * firm&oacute; con el certificado indicdo.
	 * @param signer Informaci&oacute;n del firmante.
	 * @param cert Certificado con el que comprobar la firma.
	 * @return {@code true} si la informaci&oacute;n del firmante es correcta,
	 * {@code false} en caso contrario.
	 * @throws InvalidSignatureException Cuando la firma del firmante no sea
	 * v&aacute;lida.
	 */
	private static boolean verifySigner(final SignerInformation signer, final X509Certificate cert) throws InvalidSignatureException {
		try {
			
			// Con la nueva versión de Bouncycastle, la llamada al método verify cambia.
			// Es necesario instanciar un objeto SignerInformationVerifier.
			JcaContentVerifierProviderBuilder jcaContentVerifierProviderBuilder = new JcaContentVerifierProviderBuilder();
			jcaContentVerifierProviderBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
			
			ContentVerifierProvider contentVerifierProvider = jcaContentVerifierProviderBuilder.build(cert);

			JcaDigestCalculatorProviderBuilder digestCalculatorProviderBuilder = new JcaDigestCalculatorProviderBuilder();
			digestCalculatorProviderBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
			DigestCalculatorProvider digestCalculatorProvider = digestCalculatorProviderBuilder.build();
							
			return signer.verify(
					// En la nueva versión de Bouncycastle, la signatura del constructor SignerInformationVerifier, es:
					// public SignerInformationVerifier(CMSSignatureAlgorithmNameGenerator sigNameGenerator, SignatureAlgorithmIdentifierFinder sigAlgorithmFinder, ContentVerifierProvider verifierProvider, DigestCalculatorProvider digestProvider)
//					new SignerInformationVerifier(
//							new JcaContentVerifierProviderBuilder().setProvider(new BouncyCastleProvider()).build(cert),
//							new BcDigestCalculatorProvider()));
					new SignerInformationVerifier(
			            	new	DefaultCMSSignatureAlgorithmNameGenerator(),
			            	new DefaultSignatureAlgorithmIdentifierFinder(),
			            	contentVerifierProvider,
			            	digestCalculatorProvider));
		} catch (OperatorCreationException | CMSException e) {
			throw new InvalidSignatureException("Error durante la validacion de un firmante", e); //$NON-NLS-1$
		}
	}

	/**
	 * Valida la integridad de la firma y comprueba que todas las firmas que
	 * contiene est&eacute;n realizadas con el certificado adjunto. NOTA: No
	 * comprueba la vigencia ni validez del certificado.
	 * @param cert Certificado usado para firmar.
	 * @throws InvalidSignatureException Cuando las firmas no hayan sido realizadas con el certificado
	 * indicado.
	 */
	public void verify(final X509Certificate cert) throws InvalidSignatureException {
		checkInit();
		for (final SignerInfo signerInfo : this.signers) {
			if (!verifySigner(signerInfo.getSigner(), cert)) {
				throw new InvalidSignatureException("El firmante no es valido"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Comprueba que se haya inicializado el analizador con una firma.
	 * @throws IllegalStateException Cuando no se ha inicializado el
	 * analizador.
	 */
	private void checkInit() throws IllegalStateException {
		if (this.signature == null) {
			throw new IllegalStateException("No se ha inicializado el analizador"); //$NON-NLS-1$
		}
	}

	/**
	 * Obtiene la firma analizada.
	 * @return Firma electr&oacute;nica.
	 */
	public byte[] getSignature() {
		checkInit();
		return this.signature;
	}

	/**
	 * Obtiene los certificados de firma utilizados.
	 * @return Listado de certificados con los que se firmaron los datos.
	 */
	public List<X509Certificate> getSigningCertificates() {
		checkInit();
		final List<X509Certificate> signingCertificates = new ArrayList<>();
		for (final SignerInfo signerInfo : this.signers) {
			signingCertificates.add(signerInfo.getCert());
		}
		return signingCertificates;
	}

	/**
	 * Obtiene el contenido firmado.
	 * @return Binario firmado o {@code null} si la firma no inclu&iacute;a
	 * esta informaci&oacute;n.
	 */
	public byte[] getContent() {
		checkInit();
		return this.content;
	}

	/**
	 * Informaci&oacute;n de un firmante, lo cual incluye la informaci&oacute;n
	 * que declara y el certificado de firma utilizado.
	 */
	static class SignerInfo {

		final SignerInformation signer;
		final X509Certificate cert;

		public SignerInfo(final SignerInformation signer, final X509Certificate cert) {
			this.signer = signer;
			this.cert = cert;
		}

		public SignerInformation getSigner() {
			return this.signer;
		}

		public X509Certificate getCert() {
			return this.cert;
		}
	}

}
