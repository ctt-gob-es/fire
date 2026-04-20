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
 * <b>File:</b><p>es.gob.fire.web.controller.AppController.java.</p>
 * <b>Description:</b><p>Controller for handling HTTP GET requests and navigating between views.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * <b>Date:</b><p>22/01/2025.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.1, 24/02/2025.
 */
package es.gob.fire.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.gob.fire.web.config.VersionProperties;

/** 
 * <p>Controller for handling HTTP GET requests and navigating between views.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.1, 24/02/2025.
 */
@Controller
public class AppController {
	
	@Autowired
    private VersionProperties versionProperties;
	
	@GetMapping({"/"})
	public String index(final Model model) {
		return "login";
	}
	
	@GetMapping("/inicio")
	public String inicio(final Model model) {
		model.addAttribute("appVersion", versionProperties.getProjectVersion());
        model.addAttribute("copyrightYear", versionProperties.getCopyrightYear());
		return "inicio";
	}
	@GetMapping("/user-form")
	public String user() {
		return "user-form";
	}

	@GetMapping("/application")
	public String application() {
		return "application";
	}

	@GetMapping("/certificates")
	public String certificates() {
		return "certificates";
	}
	
}
