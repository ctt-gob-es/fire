/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espa&ntilde;a
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
 * <b>File:</b><p>es.gob.fire.service.ILoginService.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p></p>
 * <b>Date:</b><p>18/02/2025.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 18/02/2025.
 */
package es.gob.fire.persistence.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import es.gob.fire.persistence.entity.ControlAccess;

/**
 * <p>Interface that provides communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p></p>
 * @version 1.0, 18/02/2025.
 */
public interface ILoginService {

	/**
	 * Retrieves all control access records from the repository.
	 * This method fetches all entries from the {@link ControlAccessRepository}.
	 *
	 * @return a {@link List} containing all {@link ControlAccess} records.
	 */
	List<ControlAccess> obtainAllControlAccess();

	/**
     * Saves the provided {@link ControlAccess} object into the repository.
     * <p>If the {@code controlAccess} object is new, it will be inserted. If it already exists, it will be updated.</p>
     *
     * @param controlAccess The {@link ControlAccess} object to be saved. It must not be {@code null}.
     */
	void saveControlAccess(ControlAccess controlAccess);

    /**
     * Retrieves the IP address of the client making the request.
     * <p>This method first checks the HTTP headers for common proxy-related headers 
     * to retrieve the original client IP, such as 'X-Forwarded-For', 'Proxy-Client-IP', and 'WL-Proxy-Client-IP'. 
     * If none of these headers are found, it falls back to using the remote address of the client.</p>
     *
     * @param request the {@link HttpServletRequest} object containing the client's request information
     * @return the IP address of the client making the request
     */
	String getClientIp(HttpServletRequest request);

	/**
    * Checks if the Pasarela service is available by sending a GET request to its URL.
    * <p>This method attempts to establish a connection to the Pasarela service and checks if the response code is 200 (OK). 
    * If successful, it returns {@code true}, otherwise, it returns {@code false}.</p>
    *
    * @return {@code true} if the Pasarela service responds with a status code of 200, otherwise {@code false}.
    */
	boolean isPasarelaAvailable();

	/**
     * Generates a unique cookie value.
     * <p>This method generates a random UUID, encodes it in Base64, and appends a random integer to it. The result is a unique string that can be used as a cookie value.</p>
     *
     * @return A string representing the generated cookie value, which includes a Base64-encoded UUID and a random integer.
     */
	String generateCookieValue();

	 /**
     * Deletes all control access records associated with the given IP address.
     * <p>This method will remove all records from the database that have the specified IP address.</p>
     *
     * @param ipUser the IP address whose associated control access records should be deleted
     */
	void deleteControlAccessByIp(String ipUser);
}
