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
 * <b>File:</b><p>es.gob.fire.service.IServerAfirmaService.java.</p>
 * <b>Description:</b><p>Interface that provides communication with the operations of the persistence layer related to Server Afirma.</p>
 * <b>Project:</b><p></p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 04/03/2025.
 */
package es.gob.fire.service;

import es.gob.fire.persistence.dto.ServerAfirmaDTO;
import es.gob.fire.persistence.entity.ServerAfirma;

/**
 * <p>Interface that provides communication with the operations of the persistence layer related to Planner.</p>
 * <b>Project:</b><p>Interface that provides communication with the operations of the persistence layer related to Server Afirma.</p>
 * @version 1.0, 04/03/2025.
 */
public interface IServerAfirmaService {
	
	/**
	 * Retrieves the server configuration details as a {@link ServerAfirmaDTO} based on the given server ID.
	 *
	 * <p>This method fetches the server configuration from the repository and maps it to a DTO object.
	 * If the authentication type requires a keystore (PKCS12), it decrypts and extracts the necessary 
	 * information such as subject and password. Otherwise, it retrieves user credentials.
	 *
	 * @param idServerAfirma the unique identifier of the server configuration.
	 * @return a {@link ServerAfirmaDTO} containing the server configuration details.
	 *         If an error occurs during processing, an empty DTO is returned.
	 * @throws CipherException if an error occurs while decrypting the keystore or password.
	 * @throws KeyStoreException if there is an issue loading the keystore.
	 * @throws NoSuchAlgorithmException if the cryptographic algorithm is not available.
	 * @throws CertificateException if an issue occurs while handling certificates.
	 * @throws IOException if an I/O error occurs while processing the keystore.
	 */
	ServerAfirmaDTO obtainServerAfirmaServiceDTO(Long idServerAfirma);
	
	/**
	 * Retrieves the server configuration details from the repository based on the given server ID.
	 *
	 * <p>This method queries the repository to find the {@link ServerAfirma} entity associated 
	 * with the provided identifier.</p>
	 *
	 * @param idServerAfirma the unique identifier of the server configuration.
	 * @return a {@link ServerAfirma} object containing the server configuration details,
	 *         or {@code null} if no matching record is found.
	 */
	ServerAfirma obtainServerAfirmaService(Long idServerAfirma);

	/**
	 * Saves or updates the given server configuration in the repository.
	 *
	 * <p>If the {@link ServerAfirma} entity already exists, it will be updated;
	 * otherwise, a new entry will be created.</p>
	 *
	 * @param serverAfirma the {@link ServerAfirma} entity containing the server configuration details to be saved.
	 */
	void saveServerAfirma(ServerAfirma serverAfirma);
}
