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
 * <b>File:</b><p>.gob.fire.service.impl.ServerAfirmaService.java.</p>
 * <b>Description:</b><p> Class that implements the communication with the operations of the persistence layer for Server Afirma.</p>
 * <b>Project:</b><p></p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 04/03/2025.
 */
package es.gob.fire.service.impl;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsKeystore;
import es.gob.fire.crypto.aes.AESCipher;
import es.gob.fire.crypto.exceptions.CipherException;
import es.gob.fire.persistence.dto.ServerAfirmaDTO;
import es.gob.fire.persistence.entity.ServerAfirma;
import es.gob.fire.persistence.repository.ServerAfirmaRepository;
import es.gob.fire.service.IServerAfirmaService;

/** 
 * <p>Class that implements the communication with the operations of the persistence layer for Server Afirma.</p>
 * <b>Project:</b><p></p>
 * @version 1.0, 04/03/2025.
 */
@Service("serverAfirmaService")
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ServerAfirmaService implements IServerAfirmaService {

	/**
	 * Constant that represents the parameter log.
	 */
	private static final Logger LOGGER = LogManager.getLogger(ServerAfirmaService.class);
	
	/**
	 * Attribute that represents the injected interface that provides CRUD operations for the persistence.
	 */
	@Autowired
	private ServerAfirmaRepository serverAfirmaRepository;
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#obtainServerAfirmaServiceDTO(java.lang.Long)
	 */
	@Override
	public ServerAfirmaDTO obtainServerAfirmaServiceDTO(Long idServerAfirma) {
		ServerAfirmaDTO serverAfirmaDTO = new ServerAfirmaDTO();
		try {
			ServerAfirma serverAfirma = serverAfirmaRepository.findByIdServerAfirma(idServerAfirma);
			if(null != serverAfirma) {
				serverAfirmaDTO = new ServerAfirmaDTO(serverAfirma.getcAuthenticationType().getIdAuthenticationType(), serverAfirma.getIdServerAfirma(), serverAfirma.getNameApp(), serverAfirma.getTimeout(), serverAfirma.getUrlServer());
				// Si el servidor afirma quiere firmas en las respuestas a partir de un almacen p12 obtenemos su informacion
				if(serverAfirma.getcAuthenticationType().getIdAuthenticationType().equals(NumberConstants.NUM_2_LONG)) {
					String passwordKeystore = AESCipher.getInstance().decryptMessageBC(serverAfirma.getPasswordKeystore());
					byte[] keystorePksc12 = Base64.getDecoder().decode(serverAfirma.getKeystore());
					KeyStore keyStore = UtilsKeystore.loadKsPKCS12(keystorePksc12, passwordKeystore);
					String subject = UtilsKeystore.listAllSubjects(keyStore).get(NumberConstants.NUM0);
					serverAfirmaDTO.setSubject(subject);
					serverAfirmaDTO.setPasswordKeystore(passwordKeystore);
					serverAfirmaDTO.setDisabledKeystore(false);
					serverAfirmaDTO.setDisabledUserPass(true);
					serverAfirmaDTO.setKeystoreB64(serverAfirma.getKeystore());
				} else if(serverAfirma.getcAuthenticationType().getIdAuthenticationType().equals(NumberConstants.NUM_1_LONG)) {
					String password = AESCipher.getInstance().decryptMessageBC(serverAfirma.getPassword());
					serverAfirmaDTO.setUser(serverAfirma.getUser());
					serverAfirmaDTO.setPassword(password);
					serverAfirmaDTO.setDisabledKeystore(true);
					serverAfirmaDTO.setDisabledUserPass(false);
				}
			}
		}catch (CipherException | KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			LOGGER.error(e);
		}
		
		return serverAfirmaDTO;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#obtainServerAfirmaService(java.lang.Long)
	 */
	@Override
	public ServerAfirma obtainServerAfirmaService(Long idServerAfirma) {
		return serverAfirmaRepository.findByIdServerAfirma(idServerAfirma);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#saveServerAfirma(es.gob.fire.persistence.entity.ServerAfirma)
	 */
	@Override
	public void saveServerAfirma(ServerAfirma serverAfirma) {
		serverAfirmaRepository.save(serverAfirma);
	}

}
