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
 * <b>File:</b><p>es.gob.fire.quartz.scheduler.AbstractFireQuartzScheduler.java.</p>
 * <b>Description:</b><p>Class that represents an abstract non clustered quartz scheduler for FIRe.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p>20/10/2021.</p>
 * @author Gobierno de España.
 * @version 1.0, 20/10/2021.
 */
package es.gob.fire.quartz.scheduler;

import java.util.Date;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.exceptions.IFireException;
import es.gob.fire.i18n.IQuartzGeneralMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.quartz.job.AbstractFireTaskQuartzJob;
import es.gob.fire.quartz.planner.IPlanner;

/**
 * <p>Class that represents an abstract quartz scheduler for FIRe.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * @version 1.1, 14/03/2023.
 */
public abstract class AbstractFireQuartzScheduler extends AbstractQuartzScheduler {
	
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(AbstractFireQuartzScheduler.class);

	
	/**
	 * Constant attribute that represents a dummy time in milliseconds for the period of
	 * execution of a task. It constant must be asigned when the period in the planner
	 * be negative.
	 */
	private static final long DUMMY_MILLIS = 3600000;

	/**
	 * Method that adds/replace a planner for an FIRe Task in the scheduler.
	 * @param taskNameParam Name of/for the task.
	 * @param planner Planner.
	 * @param taskClass Class that define the task to do. This class must implements {@link Job}.
	 * @param dataForTheTask Map with the parameters to pass to the task in even execution.
	 * @return <code>true</code> if the planner has been added/replaced in the task. Otherwise <code>false</code>.
	 * @throws FireSchedulerException In case of some error while is adding or updating the task.
	 */
	public final boolean addOrReplacePlannerInTask(final String taskNameParam, final IPlanner planner, final Class<? extends AbstractFireTaskQuartzJob> taskClass, final Map<String, Object> dataForTheTask) throws FireSchedulerException {

		boolean result = false;

		if (planner != null) {

			Date nextExecutionDate = planner.getNextExecutionDate();

			if (nextExecutionDate == null) {

				LOGGER.warn(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ01, new Object[ ] { taskNameParam }));

			} else {

				JobDataMap jobDataMap = null;
				if (dataForTheTask != null) {
					jobDataMap = new JobDataMap(dataForTheTask);
				}

				// Consultamos si el periodo de ejecución es negativo, en cuyo
				// caso,
				// se le asigna un periodo dummy y un número de repeticiones
				// igual a 1.
				long periodInMillis = planner.getPeriodInMilliSeconds();
				int numberOfReps = planner.getNumberOfRepetitions();
				if (periodInMillis <= 0) {
					periodInMillis = DUMMY_MILLIS;
					numberOfReps = 1;
				}

				try {
					result = addOrReplaceJobTrigger(nextExecutionDate, periodInMillis, numberOfReps, taskNameParam, planner.getIdentifier(), taskClass, jobDataMap);
				} catch (SchedulerException e) {
					String errorMsg = Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ02, new Object[ ] { taskNameParam, getSchedulerGroup() });
					throw new FireSchedulerException(IFireException.COD_184, errorMsg, e);
				}

			}

		}

		return result;

	}

	/**
	 * Stop and remove the task with all his planners.
	 * @param taskName The name of the task to stop.
	 * @return <code>true</code> if the task has been stopped, otherwise <code>false</code>.
	 * @throws FireSchedulerException In case of some error while is removing the task.
	 */
	public final boolean stopTask(final String taskName) throws FireSchedulerException {

		try {
			return removeJob(taskName);
		} catch (SchedulerException e) {
			String errorMsg = Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ03, new Object[ ] { taskName, getSchedulerGroup() });
			throw new FireSchedulerException(IFireException.COD_184, errorMsg, e);
		}

	}

	/**
	 * Stop and remove the planner associated to a task.
	 * @param taskName Name of the task with the planner.
	 * @param plannerId Identifier of the planner to stop.
	 * @return <code>true</code> if the planner has been stopped in the task, otherwise <code>false</code>.
	 * @throws FireSchedulerException In case of some error while is removing a planner from a concrete task.
	 */
	public final boolean stopPlannerOfTheTask(final String taskName, final String plannerId) throws FireSchedulerException {

		try {
			return removeTrigger(taskName, plannerId);
		} catch (SchedulerException e) {
			String errorMsg = Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ04, new Object[ ] { taskName, getSchedulerGroup() });
			throw new FireSchedulerException(IFireException.COD_184, errorMsg, e);
		}

	}

	/**
	 * Method that stop all the tasks and the scheduler that manage those.
	 * @param taskManagerName Name for task manager
	 * @return <code>true</code> if the scheduler has been halted,
	 * otherwise <code>false</code>.
	 */
	public final boolean stopAllTheTasks(final String taskManagerName) {

		return stopScheduler(taskManagerName);

	}

	/**
	 * Method that stop all the tasks and the scheduler that manage those, and
	 * after that, block the Scheduler.
	 * @param taskManagerName Name for task manager
	 * @return <code>false</code>.
	 */
	public final boolean stopAllTheTasksAndBlockScheduler(final String taskManagerName) {

		boolean result = stopAllTheTasks(taskManagerName);
		if (result) {
			blockStartAndStopOperations();
		}
		return result;

	}

	/**
	 * Method that unblock the Scheduler and initalizes it.
	 * @return <code>true</code> if the Scheduler has been unblocked and initialized,
	 * otherwise <code>false</code>.
	 */
	public final boolean unblockAndStartScheduler() {

		unblockStartAndStopOperations();
		return startScheduler();

	}

	/**
	 * Method that checks if the task exists.
	 * @param taskName Name of the task that is going to check.
	 * @return <code>true</code> if the task is already defined, otherwise <code>false</code>.
	 * @throws FireSchedulerException In case of some error while is checking the existence
	 * of the task in the scheduler.
	 */
	public final boolean checkIfExistsTask(final String taskName) throws FireSchedulerException {

		try {
			return checkIfExistJob(taskName);
		} catch (SchedulerException e) {
			String errorMsg = Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ05, new Object[ ] { taskName, getSchedulerGroup() });
			throw new FireSchedulerException(IFireException.COD_185, errorMsg, e);
		}

	}

}
