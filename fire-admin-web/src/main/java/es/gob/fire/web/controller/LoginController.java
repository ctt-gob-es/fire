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
 * <b>File:</b><p>es.gob.fire.web.controller.LoginController.java.</p>
 * <b>Description:</b><p> Class that enables and configures the security of the Valet application.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * <b>Date:</b><p>1.0, 27/01/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.1, 22/01/2025.
 */
package es.gob.fire.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import es.gob.fire.web.clave.sp.request.RequestClave;
import es.gob.fire.web.exception.WebAdminException;

/**
 * <p>
 * Class that manages the requests related to the Login.
 * </p>
 * <b>Project:</b>
 * <p>
 * Application for monitoring services of @firma suite systems.
 * </p>
 *
 * @version 1.1, 22/01/2025.
 */
@Controller
public class LoginController {

	@GetMapping("/login-error")
    public String login(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        String errorMessage = null;
        if (session != null) {
            AuthenticationException ex = (AuthenticationException) session
                    .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (ex != null) {
                errorMessage = ex.getMessage();
            }
        }
        model.addAttribute("errorMessage", errorMessage);
        return "login.html";
    }

	/**
     * Method that maps the root request for the application to the controller to the login view.  
     * @return String that represents the name of the view to forward.
     */
    @RequestMapping(value = "/loginClave", method = RequestMethod.POST)
    public String loginWithClave(final Model model, HttpServletRequest request) {
    	String samlRequestB64;
    	
    	try {
    		// Construimos la petición SAML en codificada en B64
			samlRequestB64 = RequestClave.constructRequestSAML();
		} catch (WebAdminException e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "login.html";
		}
        
    	model.addAttribute("samlRequest", samlRequestB64);
    	model.addAttribute("relayState", RequestClave.relayState);
    	model.addAttribute("nodeServiceUrl", RequestClave.nodeServiceUrl);
    	
        return "loginClave.html";
    }
}