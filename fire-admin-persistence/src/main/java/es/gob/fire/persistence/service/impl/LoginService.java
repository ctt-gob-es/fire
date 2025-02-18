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
 * <b>Description:</b><p>Class that implements the communication with the operations of the persistence layer.</p>
  * <b>Project:</b><p></p>
 * <b>Date:</b><p>18/02/2025.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 18/02/2025.
 */
package es.gob.fire.persistence.service.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.persistence.entity.ControlAccess;
import es.gob.fire.persistence.repository.ControlAccessRepository;
import es.gob.fire.persistence.service.ILoginService;

/**
 * <p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p></p>
 * @version 1.0, 18/02/2025.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LoginService implements ILoginService {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(LoginService.class);
	
	/**
	 * Attribute that represents the url to service pasarela.
	 */
	private static final String URL_SERVICE_PASARELA = "https://pasarela.clave.gob.es/Proxy2/Certificates";
	
	/**
	 * Attribute that represents the service object for accessing the repository of control access.
	 */
	@Autowired
	private ControlAccessRepository controlAccessRepository;
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#obtainAllControlAccess()
	 */
	@Override
	public List<ControlAccess> obtainAllControlAccess() {
		return controlAccessRepository.findAll();
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#saveControlAccess(ControlAccess)
	 */
	@Override
	public void saveControlAccess(ControlAccess controlAccess) {
		controlAccessRepository.save(controlAccess);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#generateCookieValue()
	 */
	public String generateCookieValue() {
        // Generamos un UUID aleatorio
        String uuid = UUID.randomUUID().toString().replace("-", ""); // Eliminar guiones
        
        // Convertimos UUID a bytes y codificar en Base64 para mayor entropía
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(uuid.getBytes(StandardCharsets.UTF_8));
        
        // Agregamos un número aleatorio al final similar a la estructura del valor
        int randomInt = (int) (Math.random() * Integer.MAX_VALUE);

        // Concatenamos con un símbolo especial
        return encoded + "!-" + randomInt;
    }
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#isPasarelaAvailable()
	 */
	public boolean isPasarelaAvailable() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(URL_SERVICE_PASARELA);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 segundos de timeout
            connection.setReadTimeout(5000);
            connection.connect();

            return connection.getResponseCode() == NumberConstants.NUM200;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#getClientIp(javax.servlet.http.HttpServletRequest)
	 */
    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#deleteControlAccessByIp(java.lang.String)
	 */
    @Transactional
	@Override
	public void deleteControlAccessByIp(String ipUser) {
		// TODO Auto-generated method stub
		controlAccessRepository.deleteAllByIp(ipUser);
	}
}
