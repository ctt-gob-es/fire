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
 * <b>File:</b><p>es.gob.fire.web.controller.ConfigurationController.java.</p>
 * <b>Description:</b><p>Class that manages the requests related to the configuration interface.</p>
 * <b>Project:</b><p>Application for signing documents of FIRe system.</p>
 * <b>Date:</b><p>07/02/2025.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.1, 12/02/2025.
 */
package es.gob.fire.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import SchedulerEditDTO.SchedulerVerifyCertExpiredDTO;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsDate;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.ConstantsDTO;
import es.gob.fire.persistence.dto.GeneralConfigDTO;
import es.gob.fire.persistence.dto.Property;
import es.gob.fire.persistence.dto.SchedulerEditDTO;
import es.gob.fire.persistence.entity.CPlannerType;
import es.gob.fire.persistence.entity.Planner;
import es.gob.fire.persistence.entity.Scheduler;
import es.gob.fire.persistence.service.IPropertyService;
import es.gob.fire.persistence.service.impl.PropertyService;
import es.gob.fire.service.ICPlannerTypeService;
import es.gob.fire.service.ISchedulerService;

/** 
 * <p>Class that manages the requests related to the configuration interface.</p>
 * <b>Project:</b><p>Application for monitoring services of FIRe system.</p>
 * @version 1.1, 12/02/2025.
 */
@Controller
public class ConfigurationController {

	/**
	 * Constant attribute that represents the number to identify the daily planner type.
	 */
	public static final Long PLANNING_TYPE_DAILY = 0L;

	/**
	 * Constant attribute that represents the number to identify the periodic planner type.
	 */
	public static final Long PLANNING_TYPE_PERIODIC = 1L;

	/**
	 * Constant attribute that represents the number to identify the planner type by date.
	 */
	public static final Long PLANNING_TYPE_DATE = 2L;
	
	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private ISchedulerService iSchedulerService;
	
	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private ICPlannerTypeService iCPlannerTypeService;
	
	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private IPropertyService propertyService;
	
	/**
	 * Configures the task validation settings and prepares the model attributes for rendering the task validation page.
	 *
	 * <p>This method retrieves the selected scheduler from the database, loads its associated planner, 
	 * and sets up the necessary attributes for the front-end form. It also determines whether 
	 * the period fields should be disabled based on the planner type.</p>
	 *
	 * @param model The {@link Model} object used to pass attributes to the view.
	 * @return The name of the HTML template to be rendered ("fragments/configtaskvaladmin.html").
	 */
	@RequestMapping(value = "configTaskValidation")
	public String configTaskValidation(final Model model) {
		
		SchedulerEditDTO schedulerForm = new SchedulerVerifyCertExpiredDTO();
		
		Planner planner = new Planner();
		
		Long idScheduler = NumberConstants.NUM_1_LONG;
		
		// se obtiene de persistencia la tarea seleccionada, para obtener el
		// nombre
		Scheduler schedulerSelected = iSchedulerService.getSchedulerById(idScheduler);
		schedulerForm.setIdSchedulerEdit(idScheduler);
		schedulerForm.setNameEdit(schedulerSelected.getSchedulerName());
		schedulerForm.setImplementationClassEdit(schedulerSelected.getClassName());
		
		// se cargan los tipos de planificadores
		List<ConstantsDTO> typePlanners = loadTypePlanner();
		schedulerForm.setListPlannerTypeEdit(typePlanners);
		
		// se obtiene el planificador asociado a la tarea.
		planner = schedulerSelected.getPlanner();
		schedulerForm.setIdPlannerEdit(planner.getIdPlanner());
		// obtenemos el tipo de planificador asociado a la tarea.
		Long idCPlannerType = planner.getPlannerType().getIdPlannerType();
		schedulerForm.setIdPlannerTypeEdit(idCPlannerType);
		
		Boolean isDisabledPeriod = false;
		if (idCPlannerType.equals(PLANNING_TYPE_DAILY) || idCPlannerType.equals(PLANNING_TYPE_DATE)) {
			isDisabledPeriod = true;
		}
				
		// se indica si la tarea esta habilitada o no.
		schedulerForm.setIsEnabledEdit(schedulerSelected.getIsActive());

		// se obtiene la hora, minutos, segundos asociados al planificador
		schedulerForm.setHourPeriodEdit(planner.getHourPeriod());
		schedulerForm.setMinutePeriodEdit(planner.getMinutePeriod());
		schedulerForm.setSecondPeriodEdit(planner.getSecondPeriod());
		
		// se obtiene fecha de inicio del planificador
		SimpleDateFormat sdf = new SimpleDateFormat(UtilsDate.FORMAT_DATE_TIME_STANDARD);
		schedulerForm.setInitDayStringEdit(sdf.format(planner.getInitDay()));
		
		// Obtenemos los valores para la especializacion de tarea de validacion
		SchedulerVerifyCertExpiredDTO schedulerVerifyCertExpiredDTO = (SchedulerVerifyCertExpiredDTO) schedulerForm;
		schedulerVerifyCertExpiredDTO.setDayAdviceNoticeEdit(
				schedulerSelected.getAdvanceNotice() != null ? schedulerSelected.getAdvanceNotice() : NumberConstants.NUM30_LONG
				);
		schedulerVerifyCertExpiredDTO.setPeriodCommunicationEdit(
				schedulerSelected.getPeriodCommunication() != null ? schedulerSelected.getPeriodCommunication() : NumberConstants.NUM5_LONG
				);
		
		model.addAttribute("typePlanners", typePlanners);
		model.addAttribute("typeTask", idScheduler);
		model.addAttribute("isDisabledPeriod", isDisabledPeriod);
		model.addAttribute("taskform", schedulerForm);
		
		return "fragments/configtaskvaladmin.html";
	}
	
	/**
	 * Method that loads types planners.
	 * @return List of constants that represents the different types of planners.
	 */
	private List<ConstantsDTO> loadTypePlanner() {
		List<ConstantsDTO> listPlannerType = new ArrayList<ConstantsDTO>();
		// obtenemos los tipos de planificadores.
		List<CPlannerType> listCPlannerType = iCPlannerTypeService.getAllPlannerType();
		for (CPlannerType typePlanner: listCPlannerType) {
			ConstantsDTO item = new ConstantsDTO(typePlanner.getIdPlannerType(), getConstantsValue(typePlanner.getTokenName()));
			listPlannerType.add(item);
		}

		return listPlannerType;
	}
	
	/**
	 * Method that gets string constant from multilanguage file.
	 *
	 * @param key Key for getting constant string from multilanguage file.
	 * @return Constants string.
	 */
	private String getConstantsValue(String key) {
		return Language.getResPersistenceConstants(key);
	}
	
	@GetMapping(value = "configGeneral")
	public String configGeneral(final Model model) {
		GeneralConfigDTO generalConfigForm = new GeneralConfigDTO();
		
		List<Property> properties = propertyService.getAllProperties();
		
		for (Property prop : properties) {
			if (prop.getKey().equalsIgnoreCase(PropertyService.PROPERTY_NAME_MAX_SIZE_DOC) && prop.getType().equalsIgnoreCase(PropertyService.PROPERTY_DATA_TYPE_NUMERIC)) {
				generalConfigForm.setMaxSizeDoc(prop.getNumericValue());
			}
			
			if (prop.getKey().equalsIgnoreCase(PropertyService.PROPERTY_NAME_MAX_SIZE_PETITION) && prop.getType().equalsIgnoreCase(PropertyService.PROPERTY_DATA_TYPE_NUMERIC)) {
				generalConfigForm.setMaxSizePetition(prop.getNumericValue());
			}
			
			if (prop.getKey().equalsIgnoreCase(PropertyService.PROPERTY_NAME_MAX_AMOUNT_DOCS) && prop.getType().equalsIgnoreCase(PropertyService.PROPERTY_DATA_TYPE_NUMERIC)) {
				generalConfigForm.setMaxAmountDocs(prop.getNumericValue());
			}
		}
		
		model.addAttribute("generalConfig", generalConfigForm);
		
		return "fragments/configGeneral.html";
	}
}
