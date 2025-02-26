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
 * <b>File:</b><p>es.gob.fire.quartz.task.TasksManager.java.</p>
 * <b>Description:</b><p>Class that represents a scheduler task and the action that it realizes.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p>20/10/2021.</p>
 * @author Gobierno de España.
 * @version 1.6, 12/02/2025.
 */
package es.gob.fire.quartz.task;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.i18n.ICoreGeneralMessages;
import es.gob.fire.i18n.ISchedulerIdConstants;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.entity.Planner;
import es.gob.fire.persistence.entity.Scheduler;
import es.gob.fire.quartz.job.FireTaskException;
import es.gob.fire.quartz.planner.IPlanner;
import es.gob.fire.quartz.planner.PlannerDate;
import es.gob.fire.quartz.planner.PlannerPeriod;
import es.gob.fire.quartz.scheduler.FireSchedulerException;
import es.gob.fire.quartz.scheduler.TasksScheduler;
import es.gob.fire.service.impl.SchedulerService;
import es.gob.fire.spring.config.ApplicationContextProvider;

/**
 * <p>Class that manages the named 'Tasks'. This tasks are only managed
 * by administrators of the platform.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * @version 1.6, 12/02/2025.
 */
@Component
public class TasksManager {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(TasksManager.class);

	/**
	 * The {@link ApplicationContextProvider} instance used to access the Spring application context.
	 * 
	 * This field is injected using {@link Autowired} to provide access to the application context,
	 * enabling interaction with Spring-managed beans and services within the application.
	 */
	@Autowired
	ApplicationContextProvider applicationContextProvider;
	
	/**
	 * Initializes the task scheduler after deployment by adding or updating the task for certificate expiration validation.
	 * 
	 * This method is invoked after the deployment process to configure the scheduler with the task for validating expired certificates.
	 * It retrieves the scheduler from the application context and attempts to add or update the task.
	 * 
	 * @throws FireTaskException If there is an error while adding or updating the task in the scheduler.
	 * @throws BeansException If there is an error retrieving the scheduler bean from the application context.
	 */
	@PostConstruct
	public void initializeTaskAfterDeployment() {
		try {
			addOrUpdateTaskScheduler(ApplicationContextProvider.getApplicationContext().getBean(SchedulerService.class).getSchedulerById(ISchedulerIdConstants.ID_VALIDATION_CERTIFICATES_EXPIRED));
		} catch (BeansException | FireTaskException e) {
			LOGGER.error(e);
		}
	}
	
	/**
	 * Adds or update a task in the scheduler.
	 * @param task Task pojo from the data base to add/update in the scheduler.
	 * @throws FireTaskException In case of some error working with the task.
	 */
	public static void addOrUpdateTaskScheduler(Scheduler scheduler) throws FireTaskException {
	
		// Obtenemos el nombre de la tarea.
		String taskName = scheduler.getSchedulerName();
		// Obtenemos su implementación.
		String implementationClassName = scheduler.getClassName();
		
		try {
			
			// Si la tarea existe, la eliminamos del scheduler.
			TasksScheduler tasksScheduler = TasksScheduler.getInstance();

			if (tasksScheduler.checkIfExistsTask(taskName)) {
				tasksScheduler.stopTask(taskName);
			}

			// Si la tarea está habilitada, la generamos de nuevo.
			if (scheduler.getIsActive()) {
				// Llamamos al método auxiliar (para reducir complejidad
				// ciclomática).
				loadSchedulersAux(scheduler, taskName, implementationClassName);
			}
						
		}catch (Exception e) {
			
			
		}
	}
	
	/**
	 * Private auxiliar method to load the tasks and reduce the cyclomatic complexity.
	 * @param task POJO object with the data of the task.
	 * @param taskName Name of the task.
	 * @param implementationClassName Implementation class name of the task.
	 * @throws ClassNotFoundException In case of some error with the class that implements the task.
	 */
	@SuppressWarnings("unchecked")
	private static void loadSchedulersAux(Scheduler scheduler, String schedulerName, String implementationClassName) throws ClassNotFoundException {

		// Cargamos y obtenemos la lista de planificadores
		// asociados.
		Planner planner = scheduler.getPlanner();

		// Si al menos hay un planificador...
		if (planner != null) {

			LOGGER.debug(Language.getFormatResCoreTasks(ICoreGeneralMessages.TASK_MNG_002, new Object[ ] { schedulerName, implementationClassName }));

			TasksScheduler tasksScheduler = TasksScheduler.getInstance();
	
			// Construimos el planificador asociado.
			IPlanner calculatedPlanner = getCalculatedPlannerFromDataBase(planner);
			try {
				tasksScheduler.addOrReplacePlannerInTask(schedulerName, calculatedPlanner, (Class<es.gob.fire.quartz.task.FireTask>) Class.forName(implementationClassName), null);
			} catch (FireSchedulerException e) {
				LOGGER.error(Language.getFormatResCoreTasks(ICoreGeneralMessages.TASK_MNG_003, new Object[ ] { schedulerName }), e);
			}
			

		}

	}
	
	/**
	 * Method that obtains a planner through the data base configuration of it.
	 * @param planner Parameter that represents the information of the planner in the database.
	 * @return an object that represents the calculated planner.
	 */
	public static IPlanner getCalculatedPlannerFromDataBase(Planner planner) {
		IPlanner result = null;
		switch (planner.getPlannerType().getIdPlannerType().intValue()) {
			case (int) IPlanner.PLANNER_TYPE_DIARY:
			case (int) IPlanner.PLANNER_TYPE_PERIOD:
				result = new PlannerPeriod(planner);
				break;
			case (int) IPlanner.PLANNER_TYPE_DATE:
				result = new PlannerDate(planner);
				break;
			default:
				break;
		}
		return result;
	}
}
