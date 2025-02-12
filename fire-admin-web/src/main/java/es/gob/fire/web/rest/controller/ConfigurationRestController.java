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
 * <b>File:</b><p>es.gob.fire.web.controller.ConfigurationRestController.java.</p>
 * <b>Description:</b><p>Class that manages the REST requests related to the Configuration administration and JSON communication.</p>
 * <b>Project:</b><p>Application for signing documents of FIRe system.</p>
 * <b>Date:</b><p>07/02/2025.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 07/02/2025.
 */
package es.gob.fire.web.rest.controller;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import SchedulerEditDTO.SchedulerVerifyCertExpiredDTO;
import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.UtilsDate;
import es.gob.fire.i18n.ISchedulerIdConstants;
import es.gob.fire.i18n.IWebLogMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.SchedulerEditDTO;
import es.gob.fire.persistence.entity.CPlannerType;
import es.gob.fire.persistence.entity.Planner;
import es.gob.fire.persistence.entity.Scheduler;
import es.gob.fire.quartz.task.TasksManager;
import es.gob.fire.service.ICPlannerTypeService;
import es.gob.fire.service.IPlannerService;
import es.gob.fire.service.ISchedulerService;

/** 
 * <p>Class that manages the REST requests related to the Configuration administration and JSON communication.</p>
 * <b>Project:</b><p>Application for monitoring services of FIRe system.</p>
 * @version 1.0, 07/02/2025.
 */
@RestController
public class ConfigurationRestController {
	
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ConfigurationRestController.class);
	
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
	private IPlannerService iPlannerService;
	
	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private ICPlannerTypeService iCPlannerTypeService;
	
	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private ISchedulerService iSchedulerService;
	
	/**
	 * Method to update the task.
	 * @param taskForm Parameter that represents the backing form for editing a Task
	 * @return Modified task.
	 */
	@RequestMapping(value = "/updatescheduler", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody SchedulerEditDTO updateScheduler(@RequestBody SchedulerVerifyCertExpiredDTO taskForm) {
		
		try {
			if (!validateInitDate(taskForm.getInitDayStringEdit())) {
				taskForm.setErrorEdit(Language.getResWebFire(IWebLogMessages.ERROR_VALIDATE_DATE));
			} else {
				// se obtiene el planificador
				Planner planner = iPlannerService.getPlannerById(taskForm.getIdPlannerEdit());
				
				// se obtiene el tipo de planificador seleccionado
				Long idCPlannerType = taskForm.getIdPlannerTypeEdit();
				
				// se actualizan los campos horas, minutos y segundos si el
				// planificador es tipo periodico.
				if (idCPlannerType.equals(PLANNING_TYPE_PERIODIC)) {
					// se actualizan los campos horas, minutos y segundos
					planner.setHourPeriod(taskForm.getHourPeriodEdit());
					planner.setMinutePeriod(taskForm.getMinutePeriodEdit());
					planner.setSecondPeriod(taskForm.getSecondPeriodEdit());
				}
				
				// se actualiza la fecha inicial por si se ha modificado.
				Date initDay = UtilsDate.transformDate(taskForm.getInitDayStringEdit(), UtilsDate.FORMAT_DATE_TIME_STANDARD);
				planner.setInitDay(initDay);
				
				CPlannerType plannerType = iCPlannerTypeService.getCPlannerTypeById(idCPlannerType);
				planner.setPlannerType(plannerType);
				
				Scheduler scheduler = iSchedulerService.getSchedulerById(taskForm.getIdSchedulerEdit());
				
				// se actualiza la tarea indicando si esta habilitada o no.
				scheduler.setIsActive(taskForm.getIsEnabledEdit());
				
				// actualizamos los dias de preaviso y de periodo de comunicacion
				scheduler.setAdvanceNotice(taskForm.getDayAdviceNoticeEdit());
				scheduler.setPeriodCommunication(taskForm.getPeriodCommunicationEdit());
				
				// persistimos los cambios del planificador y de la tarea.
				Scheduler updatedTask = iSchedulerService.saveScheduler(scheduler);
				iPlannerService.savePlanner(planner);
				
				if (updatedTask.getIdScheduler() == ISchedulerIdConstants.ID_VALIDATION_CERTIFICATES_EXPIRED) {
					TasksManager.addOrUpdateTaskScheduler(updatedTask);
				}
				
				String taskName = scheduler.getSchedulerName();
				String infoMsg = Language.getFormatResWebFire(IWebLogMessages.INFO_UPDATE_TASK_OK, new Object[ ] { taskName });
				LOGGER.info(infoMsg);
				taskForm.setMsgOkEdit(infoMsg);
			}
		} catch (ParseException e) {
			LOGGER.error(Language.getFormatResWebFire(IWebLogMessages.ERROR_PARSE_DATE, new Object[ ] { e.getMessage() }));
			taskForm.setErrorEdit(Language.getResWebFire(IWebLogMessages.ERROR_UPDATE_TASK_WEB));
		} catch (Exception e) {
			LOGGER.error(Language.getFormatResWebFire(IWebLogMessages.ERROR_UPDATE_TASK_WEB, new Object[ ] { e.getMessage() }));
			taskForm.setErrorEdit(Language.getResWebFire(IWebLogMessages.ERROR_UPDATE_TASK_WEB));
		}
		
		return taskForm;
		
	}
	
	/**
	 * Method to validate the date indicated for the planning of the task.
	 * @param taskForm Parameter that represents the backing form for editing a Task
	 * @return true, if the date is correct.
	 */
	private Boolean validateInitDate(String date) {
		Boolean result = true;
		// se comprueba que la fecha indicada no sea anterior a la actual
		LocalDate now = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(UtilsDate.FORMAT_DATE_TIME_STANDARD);
		if(date!=null && !date.isEmpty()){
			LocalDate initDay = LocalDate.parse(date, formatter);
			if (initDay == null || initDay.isBefore(now)) {
				result = false;
			}
		}
		else{
			result = false;
		}

		return result;
	}
}
