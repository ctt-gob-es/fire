/*
/*******************************************************************************
 * Copyright (C) 2024 Secretaría General de la Administración Digital, Gobierno de España
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
 * <b>File:</b><p>es.clave.SP2.CustomErrorController.java.</p>
 * <b>Description:</b><p>Class that processes uncontrolled errors generated in the Kit.</p>
 * <b>Project:</b><p>Citizen identification and authentication platform.</p>
 * <b>Date:</b><p>29/10/2025.</p>
 * @author Gobierno de España.
 * @version 1.0, 29/10/2025.
 */
package es.gob.fire.web.clave3.sp;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * <p>Class that processes uncontrolled errors generated in the Kit.</p>
 * <b>Project:</b><p>Citizen identification and authentication platform.</p>
 * @version 1.0, 29/10/2025.
 */
@Controller
public class CustomErrorController implements ErrorController {

	/**
	 * Function that processes uncontrolled errors generated in the Kit.
	 *
	 * @param model
	 * @return returns the name of the HRTM error page
	 */
	@PostMapping("/error")
	public String handleError(final Model model) {
		model.addAttribute("errorMessageErrorPage", "Ha habido un error en la aplicación. Consulte los logs para obtener más información.");
		return "error";
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}
}
