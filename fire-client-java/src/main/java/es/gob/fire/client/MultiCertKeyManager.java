package es.gob.fire.client;

import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;

/**
 * Gestor de claves SSL que permite la selecci&oacute;n expresa de una clave
 * por medio de su alias.
 */
public class MultiCertKeyManager implements X509KeyManager {

	private final List<X509KeyManager> keyManagers;

	private final String selectedAlias;

	/**
	 * Construye el gestor de claves.
	 * @param keystore Almac&eacute;n de claves.
	 * @param password Contrase&ntilde;a del almac&eacute;n.
	 * @param alias Alias a utilizar.
	 * @throws GeneralSecurityException Cuando ocurre un error al inicializar el almac&eacute;n.
	 */
	public MultiCertKeyManager(final KeyStore keystore, final char[] password, final String alias) throws GeneralSecurityException {

		this.selectedAlias = alias;

		KeyManagerFactory factory;
		try {
			factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			factory.init(keystore, password);
		} catch (final GeneralSecurityException e) {
			throw new GeneralSecurityException("No se pudo inicializar el gestor de claves", e); //$NON-NLS-1$
		}

		final List<X509KeyManager> kmList = new ArrayList<X509KeyManager>();
		for (final KeyManager km : factory.getKeyManagers()) {
			if (km instanceof X509KeyManager) {
				kmList.add((X509KeyManager) km);
			}
		}
		this.keyManagers = kmList.isEmpty() ? null : kmList;
	}

	@Override
	public String chooseClientAlias(final String[] keyType, final Principal[] issuers, final Socket socket) {

		// Si se selecciono el alias a utilizar, se utiliza directamente
		if (this.selectedAlias != null) {
			return this.selectedAlias;
		}

		// Si no, se busca el alias con el gestor subyacente
		for (final X509KeyManager keyManager : this.keyManagers) {
			final String alias = keyManager.chooseClientAlias(keyType, issuers, socket);
			if (alias != null) {
				return alias;
			}
		}
		return null;
	}

	@Override
	public String chooseServerAlias(final String keyType, final Principal[] issuers, final Socket socket) {
		for (final X509KeyManager keyManager : this.keyManagers) {
			final String alias = keyManager.chooseServerAlias(keyType, issuers, socket);
			if (alias != null) {
				return alias;
			}
		}
		return null;
	}

	@Override
	public X509Certificate[] getCertificateChain(final String alias) {
		for (final X509KeyManager keyManager : this.keyManagers) {
			final X509Certificate[] chain = keyManager.getCertificateChain(alias);
			if (chain != null && chain.length > 0) {
				return chain;
			}
		}
		return null;
	}

	@Override
	public PrivateKey getPrivateKey(final String alias) {
		for (final X509KeyManager keyManager : this.keyManagers) {
			final PrivateKey privateKey = keyManager.getPrivateKey(alias);
			if (privateKey != null) {
				return privateKey;
			}
		}
		return null;
	}

	@Override
	public String[] getClientAliases(final String keyType, final Principal[] issuers) {

		// Si se selecciono el alias a utilizar, se utiliza directamente
		if (this.selectedAlias != null) {
			return new String[] { this.selectedAlias };
		}

		final List<String> aliases = new ArrayList<String>();
		for (final X509KeyManager keyManager : this.keyManagers) {
			for (final String alias : keyManager.getClientAliases(keyType, issuers)) {
				aliases.add(alias);
			}
		}
		return aliases.isEmpty() ? null : aliases.toArray(new String[0]);
	}

	@Override
	public String[] getServerAliases(final String keyType, final Principal[] issuers) {
	    final List<String> aliases = new ArrayList<String>();
		for (final X509KeyManager keyManager : this.keyManagers) {
			for (final String alias : keyManager.getServerAliases(keyType, issuers)) {
				aliases.add(alias);
			}
		}
		return aliases.isEmpty() ? null : aliases.toArray(new String[0]);
	}


}
